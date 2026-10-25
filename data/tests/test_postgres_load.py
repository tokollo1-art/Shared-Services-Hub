"""Loads the generated seed.sql into a real PostgreSQL and checks it.

Uses SSH_TEST_PG_DSN (a libpq DSN for an EMPTY scratch database) when set.
Otherwise it starts a throwaway cluster from a local PostgreSQL install, and
skips if there is none or psycopg2 is missing.
"""
import os
import shutil
import subprocess
import tempfile
from pathlib import Path

import pytest

from ssh_ds.seed import generate, to_sql

psycopg2 = pytest.importorskip("psycopg2")
SCHEMA = Path(__file__).with_name("schema") / "entities.sql"


def _pg_bin():
    for d in sorted(Path("/usr/lib/postgresql").glob("*/bin"), reverse=True):
        if (d / "initdb").exists():
            return d
    found = shutil.which("initdb")
    return Path(found).parent if found else None


@pytest.fixture(scope="module")
def conn():
    dsn = os.environ.get("SSH_TEST_PG_DSN")
    cluster = None
    if not dsn:
        bindir = _pg_bin()
        if bindir is None:
            pytest.skip("no PostgreSQL available")
        tmp = Path(tempfile.mkdtemp(prefix="sshpg"))
        run = ["runuser", "-u", "postgres", "--"] if os.geteuid() == 0 else []
        if run:
            os.chown(tmp, shutil.which("id") and __import__("pwd").getpwnam("postgres").pw_uid, -1)
        data, port = tmp / "data", 55000 + os.getpid() % 1000
        subprocess.run(run + [str(bindir / "initdb"), "-D", str(data), "-A", "trust"],
                       check=True, capture_output=True)
        subprocess.run(run + [str(bindir / "pg_ctl"), "-D", str(data), "-o", f"-p {port} -k {tmp}",
                              "-l", str(tmp / "log"), "-w", "start"], check=True, capture_output=True)
        cluster = (run, bindir, data, tmp)
        dsn = f"host={tmp} port={port} user=postgres dbname=postgres"
    c = psycopg2.connect(dsn)
    c.autocommit = True
    yield c
    c.close()
    if cluster:
        run, bindir, data, tmp = cluster
        subprocess.run(run + [str(bindir / "pg_ctl"), "-D", str(data), "-m", "immediate", "stop"],
                       capture_output=True)
        shutil.rmtree(tmp, ignore_errors=True)


@pytest.fixture(scope="module")
def loaded(conn):
    cur = conn.cursor()
    cur.execute("DROP SCHEMA public CASCADE; CREATE SCHEMA public;")
    cur.execute(SCHEMA.read_text())
    cur.execute(to_sql(generate(42)))        # the whole file is one transaction
    return cur


def one(cur, sql):
    cur.execute(sql)
    return cur.fetchone()[0]


def test_row_counts(loaded):
    counts = {t: one(loaded, f"SELECT count(*) FROM {t}") for t in
              ("users", "smes", "youth", "skills", "tasks", "experiences", "youth_skills",
               "task_skills", "ledger_entries", "payments", "wallets", "wallet_transactions")}
    assert counts["users"] == 69 and counts["youth"] == 50 and counts["smes"] == 15
    assert counts["skills"] == 99 and counts["tasks"] == 150 and counts["experiences"] == 104
    assert counts["ledger_entries"] == counts["payments"] == 100 and counts["wallets"] == 1


def test_tier_of_every_youth_matches_tier_java(loaded):
    loaded.execute("""SELECT count(*) FROM (
        SELECT y.id, count(e.id) FILTER (WHERE e.is_verified) AS n FROM youth y
        LEFT JOIN experiences e ON e.youth_id = y.id GROUP BY y.id) s WHERE n >= 5""")
    assert loaded.fetchone()[0] == 6


def test_identity_sequences_continue_after_the_seed(loaded):
    loaded.execute("INSERT INTO users (name, email, password, role, is_active) "
                   "VALUES ('New', 'new@example.com', 'x', 'YOUTH', true) RETURNING id")
    assert loaded.fetchone()[0] == 70
    loaded.execute("INSERT INTO skills (slug, label) VALUES ('brand-new', 'brand new') RETURNING id")
    assert loaded.fetchone()[0] == 100


def test_the_database_chain_verifies_after_a_round_trip(loaded):
    from ssh_ds.ledger import verify_chain
    loaded.execute("""SELECT entry_sequence, experience_id, task_id, youth_id, rating, feedback, reference,
                      verified_at, previous_hash, hash FROM ledger_entries ORDER BY entry_sequence""")
    keys = ("sequence", "experience_id", "task_id", "youth_id", "rating", "feedback", "reference",
            "verified_at", "previous_hash", "hash")
    entries = [dict(zip(keys, r)) for r in loaded.fetchall()]
    assert verify_chain(entries) == (True, None)


def test_wallet_balance_matches_its_transactions(loaded):
    assert one(loaded, "SELECT balance FROM wallets") == pytest.approx(one(
        loaded, "SELECT sum(CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END) "
                "FROM wallet_transactions"))
