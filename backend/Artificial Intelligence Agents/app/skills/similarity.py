from __future__ import annotations

from app.schemas import SimilarityMatch, SimilarityResponse


def skill_similarity(
    a_tags: list[str],
    b_tags: list[str],
) -> SimilarityResponse:
    """
    Compare two lists of skill labels and return an aggregate similarity
    plus per-pair matches.

    The comparison is exact-label and word-overlap based. It is the
    Basket 1 implementation; Basket 2 replaces the body with embedding
    similarity, keeping the same signature.
    """
    a = [t.strip().lower() for t in a_tags if t and t.strip()]
    b = [t.strip().lower() for t in b_tags if t and t.strip()]
    if not a or not b:
        return SimilarityResponse(similarity=0.0, matches=[])

    matches: list[SimilarityMatch] = []
    seen: set[tuple[str, str]] = set()
    total = 0.0
    for x in a:
        best_score = 0.0
        best_match: tuple[str, str] | None = None
        for y in b:
            pair = (x, y)
            score = _pair_score(x, y)
            if score > best_score:
                best_score = score
                best_match = pair
        if best_match is not None:
            if best_match not in seen:
                seen.add(best_match)
                matches.append(
                    SimilarityMatch(a=best_match[0], b=best_match[1], score=round(best_score,4))
                )
            total += best_score

    aggregate = total / max(len(a), len(b))
    return SimilarityResponse(similarity=round(min(aggregate, 1.0), 4), matches=matches)


def _pair_score(a: str, b: str) -> float:
    if a == b:
        return 1.0
    a_words = {w for w in a.split() if len(w) > 2}
    b_words = {w for w in b.split() if len(w) > 2}
    if not a_words or not b_words:
        return 0.0
    overlap = len(a_words & b_words)
    union = len(a_words | b_words)
    return overlap / union if union else 0.0
