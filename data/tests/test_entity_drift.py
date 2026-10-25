"""Guards against the seed drifting from the Java entities.

The backend could not always be built where this package was made, so these
tests read the entity sources directly: every column the seed writes must exist
on the entity, every NOT NULL column must be written, and the test schema
fixture must have exactly the entity's columns.
"""
import re
from pathlib import Path

import pytest

from ssh_ds.seed import TABLES

ROOT = Path(__file__).resolve().parents[2]
ENTITY_DIR = ROOT / "backend" / "src" / "main" / "java" / "com" / "ssh" / "entity"
FIXTURE = Path(__file__).with_name("schema") / "entities.sql"
ENTITY_FILE = {"users": "User", "smes": "SME", "youth": "Youth", "skills": "Skill", "tasks": "Task",
               "experiences": "Experience", "youth_skills": "YouthSkill", "task_skills": "TaskSkill",
               "ledger_entries": "LedgerEntry", "payments": "Payment", "wallets": "Wallet",
               "wallet_transactions": "WalletTransaction"}
PRIMITIVES = {"boolean", "int", "long", "double"}
FIELD = re.compile(r"^\s*private\s+(?!static|final)([\w<>\[\], ?]+?)\s+(\w+)\s*(?:=[^;]*)?;\s*(?://.*)?$")

pytestmark = pytest.mark.skipif(not ENTITY_DIR.exists(), reason="backend sources not in this checkout")


def snake(name):
    return re.sub(r"(?<!^)(?=[A-Z])", "_", name).lower()


def parse_entity(name):
    """(table, {column: not_null}) read from an entity's source."""
    text = (ENTITY_DIR / f"{name}.java").read_text(encoding="utf-8")
    table = re.search(r'@Table\s*\([^)]*?name\s*=\s*"(\w+)"', text).group(1)
    cols, annotations = {}, []
    for line in text.splitlines():
        stripped = line.strip()
        if stripped.startswith("@"):
            annotations.append(stripped)
            continue
        m = FIELD.match(line)
        if m:
            ftype, fname = m.group(1).strip(), m.group(2)
            joined = " ".join(annotations)
            if "mappedBy" in joined or "@Transient" in joined or ftype.startswith("List"):
                annotations = []
                continue
            explicit = re.search(r'name\s*=\s*"(\w+)"', " ".join(
                a for a in annotations if a.startswith(("@Column", "@JoinColumn"))))
            col = explicit.group(1) if explicit else snake(fname)
            not_null = ftype in PRIMITIVES or "nullable = false" in joined or "optional = false" in joined
            cols[col] = not_null
        if stripped and not stripped.startswith(("*", "/*", "//")):
            annotations = []
    return table, cols


@pytest.mark.parametrize("table", [t[1] for t in TABLES])
def test_seed_columns_match_entity(table):
    key = next(k for k, t, _ in TABLES if t == table)
    entity_table, cols = parse_entity(ENTITY_FILE[key])
    assert entity_table == table
    written = next(c for k, t, c in TABLES if t == table)
    assert "id" in written
    unknown = [c for c in written if c != "id" and c not in cols]
    assert not unknown, f"{table}: seed writes columns the entity does not have: {unknown}"
    missing = [c for c, nn in cols.items() if nn and c not in written]
    assert not missing, f"{table}: NOT NULL columns the seed never sets: {missing}"


@pytest.mark.parametrize("table", [t[1] for t in TABLES])
def test_schema_fixture_matches_entity(table):
    key = next(k for k, t, _ in TABLES if t == table)
    _, cols = parse_entity(ENTITY_FILE[key])
    body = re.search(rf"CREATE TABLE {table} \((.*?)\n\);", FIXTURE.read_text(), re.S).group(1)
    declared = {line.split()[0] for line in body.splitlines()
                if line.strip() and not line.strip().startswith(("CONSTRAINT", "UNIQUE"))}
    assert declared == set(cols) | {"id"}
