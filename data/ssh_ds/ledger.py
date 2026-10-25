"""Public ledger hash chain, mirroring backend LedgerService.

Each VERIFIED experience gets one ledger entry. An entry's hash is the SHA-256
of this pipe-joined string (LedgerService.canonical):

    sequence|experienceId|taskId|youthId|rating|feedback|reference|verifiedAt|previousHash

Null values become "", the first previousHash is "GENESIS", and `verifiedAt` is
Java's LocalDateTime.toString (seconds are left out when they are zero) after
truncation to milliseconds. Ratings are Java Double.toString, so 4.0 is "4.0".
The data seeded here has to verify with LedgerService.verifyChain().
"""
import datetime as dt
import hashlib

GENESIS_HASH = "GENESIS"


def java_local_datetime(value):
    """Format like java.time.LocalDateTime.toString() for a datetime or ISO string."""
    if isinstance(value, str):
        value = dt.datetime.fromisoformat(value)
    ms = value.microsecond // 1000          # the backend truncates to milliseconds
    out = value.strftime("%Y-%m-%dT%H:%M")
    if value.second or ms:
        out += value.strftime(":%S")
    if ms:
        out += f".{ms:03d}"
    return out


def _value(v):
    if v is None:
        return ""
    if isinstance(v, float):
        return repr(v)                       # 4.0 -> "4.0", like Double.toString
    return str(v)


def canonical(entry):
    return "|".join([
        str(entry["sequence"]), str(entry["experience_id"]), str(entry["task_id"]),
        str(entry["youth_id"]), _value(entry["rating"]), _value(entry["feedback"]),
        _value(entry["reference"]), java_local_datetime(entry["verified_at"]),
        entry["previous_hash"]])


def sha256(text):
    return hashlib.sha256(text.encode("utf-8")).hexdigest()


def build_chain(experiences):
    """Ledger entries for every VERIFIED experience, in verification order."""
    verified = sorted((e for e in experiences if e["status"] == "VERIFIED"),
                      key=lambda e: (e["verified_at"], e["id"]))
    entries, prev = [], GENESIS_HASH
    for seq, e in enumerate(verified, 1):
        entry = {"id": seq, "sequence": seq, "experience_id": e["id"], "task_id": e["task_id"],
                 "youth_id": e["youth_id"], "rating": e["rating"], "feedback": e["feedback"],
                 "reference": e["reference"], "verified_at": e["verified_at"],
                 "previous_hash": prev, "created_at": e["verified_at"]}
        entry["hash"] = sha256(canonical(entry))
        entries.append(entry)
        prev = entry["hash"]
    return entries


def verify_chain(entries):
    """Same walk as LedgerService.verifyChain. Returns (ok, broken_at_sequence)."""
    prev = GENESIS_HASH
    for entry in sorted(entries, key=lambda e: e["sequence"]):
        if entry["previous_hash"] != prev or sha256(canonical(entry)) != entry["hash"]:
            return False, entry["sequence"]
        prev = entry["hash"]
    return True, None
