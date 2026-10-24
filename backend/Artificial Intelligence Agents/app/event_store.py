from __future__ import annotations

import logging
import sqlite3
from abc import ABC, abstractmethod
from datetime import datetime, timezone
from pathlib import Path
from typing import Literal
from uuid import UUID

logger = logging.getLogger(__name__)


class EventStore(ABC):
    @abstractmethod
    def seen(self, event_id: UUID) -> bool: ...

    @abstractmethod
    def mark(self, event_id: UUID) -> None: ...

    @abstractmethod
    def close(self) -> None: ...


class InMemoryEventStore(EventStore):
    """Lost on restart. Use for the demo only."""

    def __init__(self) -> None:
        self._seen: set[str] = set()

    def seen(self, event_id: UUID) -> bool:
        return str(event_id) in self._seen

    def mark(self, event_id: UUID) -> None:
        self._seen.add(str(event_id))

    def close(self) -> None:
        return None


class SqliteEventStore(EventStore):
    """
    Durable dedupe. One table, one file, one row per processed event.

    The file lives on a Docker volume so it survives container restarts.
    Concurrent access is handled by SQLite's own locking plus a
    BEGIN IMMEDIATE around the write so two workers cannot both
    insert the same event.
    """

    def __init__(self, path: str | Path) -> None:
        self._path = Path(path)
        self._path.parent.mkdir(parents=True, exist_ok=True)
        self._conn = sqlite3.connect(
            self._path,
            check_same_thread=False,
            isolation_level=None,
        )
        self._conn.execute("PRAGMA journal_mode=WAL")
        self._conn.execute("PRAGMA synchronous=NORMAL")
        self._conn.execute(
            """
            CREATE TABLE IF NOT EXISTS processed_events (
                                                            event_id   TEXT PRIMARY KEY,
                                                            seen_at    TEXT NOT NULL
            )
            """
        )
        logger.info("SqliteEventStore ready at %s", self._path)

    def seen(self, event_id: UUID) -> bool:
        cur = self._conn.execute(
            "SELECT 1 FROM processed_events WHERE event_id = ? LIMIT 1",
            (str(event_id),),
        )
        return cur.fetchone() is not None

    def mark(self, event_id: UUID) -> None:
        now = datetime.now(timezone.utc).isoformat()
        try:
            self._conn.execute("BEGIN IMMEDIATE")
            self._conn.execute(
                "INSERT OR IGNORE INTO processed_events (event_id, seen_at) VALUES (?, ?)",
                (str(event_id), now),
            )
            self._conn.execute("COMMIT")
        except Exception:
            self._conn.execute("ROLLBACK")
            raise

    def close(self) -> None:
        self._conn.close()


Backend = Literal["memory", "sqlite"]


def build_event_store(backend: Backend, sqlite_path: str) -> EventStore:
    if backend == "sqlite":
        return SqliteEventStore(sqlite_path)
    return InMemoryEventStore()
