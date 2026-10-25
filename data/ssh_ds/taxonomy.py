"""Skill taxonomy: the backend's curated skills, keyed by slug.

The list is a copy of backend/Artificial Intelligence Agents/app/skills/
esco_subset.json (99 skills, with ESCO URI and OFO code). It is the same list
the `skills` table should hold. Slug rule: lowercase label, runs of anything
that is not a letter or digit become one hyphen ("cash-up procedures" ->
"cash-up-procedures"). `youth_skills` and `task_skills` link to skills by id;
the APIs and the agents see slugs.

Categories below are for grouping in filters (`skills.category`). The source
file has none, so they are defined here.
"""
import json
import re
from pathlib import Path

_FILE = Path(__file__).with_name("esco_subset.json")


def slugify(text):
    return re.sub(r"[^a-z0-9]+", "-", str(text).lower()).strip("-")


SKILLS = []
for _e in json.loads(_FILE.read_text(encoding="utf-8")):
    SKILLS.append({"slug": slugify(_e["label"]), "label": _e["label"],
                   "esco_uri": _e.get("esco_uri"), "ofo_code": _e.get("ofo_code"),
                   "ofo_label": _e.get("ofo_label")})

_CATEGORY_SLUGS = {
    "retail": "inventory-coordination stock-taking sales-support inventory-forecasting "
              "order-fulfilment returns-handling mystery-shopping stock-replenishment "
              "visual-merchandising point-of-sale-operation upselling product-demonstration "
              "sales-reporting",
    "customer-service": "customer-service phone-etiquette customer-relationship-management "
                        "customer-feedback-collection complaint-handling",
    "finance": "cash-handling basic-bookkeeping invoice-processing payment-reconciliation "
               "budget-tracking expense-recording petty-cash-handling bank-reconciliation "
               "cash-up-procedures",
    "marketing": "social-media-management content-writing graphic-design photography "
                 "video-editing digital-marketing email-marketing community-management "
                 "copywriting proofreading market-research survey-design",
    "logistics": "delivery-coordination driving logistics-coordination packaging "
                 "warehouse-support procurement-support supplier-communication "
                 "event-coordination travel-booking",
    "admin": "email-correspondence scheduling document-management calendar-management "
             "report-writing file-organisation digital-filing photocopying-and-scanning "
             "office-equipment-operation meeting-coordination minute-taking "
             "document-drafting template-formatting project-coordination",
    "data": "data-entry database-entry data-analysis spreadsheet-management "
            "data-visualisation data-cleaning basic-statistical-analysis research "
            "search-engine-research",
    "tech": "basic-coding website-management digital-literacy data-privacy-awareness "
            "online-security-basics cloud-file-management",
    "trades": "basic-repairs gardening cleaning-and-hygiene food-preparation waste-management",
    "health-safety": "first-aid health-and-safety-compliance quality-checking "
                     "sustainability-practices",
    "soft-skills": "time-management communication teamwork problem-solving "
                   "attention-to-detail presentation-skills negotiation leadership "
                   "mentoring conflict-resolution stakeholder-communication "
                   "task-prioritisation",
    "language": "translation",
}
SKILL_CATEGORY = {s: c for c, slugs in _CATEGORY_SLUGS.items() for s in slugs.split()}
for _s in SKILLS:
    _s["category"] = SKILL_CATEGORY[_s["slug"]]   # KeyError here = a skill needs a category

ALL_SKILLS = sorted(s["slug"] for s in SKILLS)
SKILL_BY_SLUG = {s["slug"]: s for s in SKILLS}
CATEGORIES = sorted(_CATEGORY_SLUGS)

# Explicit links between closely related skills: partial credit.
_RELATED_PAIRS = [
    ("data-entry", "database-entry"), ("data-entry", "spreadsheet-management"),
    ("data-entry", "data-cleaning"), ("database-entry", "spreadsheet-management"),
    ("spreadsheet-management", "data-visualisation"),
    ("spreadsheet-management", "basic-bookkeeping"), ("spreadsheet-management", "budget-tracking"),
    ("spreadsheet-management", "invoice-processing"), ("data-cleaning", "data-analysis"),
    ("data-analysis", "basic-statistical-analysis"), ("data-analysis", "data-visualisation"),
    ("digital-filing", "file-organisation"), ("digital-filing", "cloud-file-management"),
    ("file-organisation", "document-management"), ("document-management", "document-drafting"),
    ("document-drafting", "template-formatting"), ("document-drafting", "report-writing"),
    ("email-correspondence", "communication"), ("scheduling", "calendar-management"),
    ("scheduling", "meeting-coordination"), ("calendar-management", "meeting-coordination"),
    ("meeting-coordination", "minute-taking"),
    ("basic-bookkeeping", "expense-recording"), ("basic-bookkeeping", "bank-reconciliation"),
    ("basic-bookkeeping", "invoice-processing"), ("payment-reconciliation", "bank-reconciliation"),
    ("cash-handling", "cash-up-procedures"), ("cash-handling", "petty-cash-handling"),
    ("cash-handling", "point-of-sale-operation"), ("petty-cash-handling", "expense-recording"),
    ("social-media-management", "community-management"),
    ("social-media-management", "digital-marketing"), ("social-media-management", "copywriting"),
    ("graphic-design", "visual-merchandising"), ("graphic-design", "photography"),
    ("photography", "video-editing"), ("copywriting", "content-writing"),
    ("copywriting", "proofreading"), ("digital-marketing", "email-marketing"),
    ("market-research", "survey-design"), ("customer-feedback-collection", "survey-design"),
    ("customer-service", "complaint-handling"), ("customer-service", "phone-etiquette"),
    ("customer-service", "customer-relationship-management"),
    ("customer-service", "sales-support"), ("sales-support", "upselling"),
    ("sales-support", "product-demonstration"), ("sales-support", "sales-reporting"),
    ("inventory-coordination", "stock-taking"), ("inventory-coordination", "stock-replenishment"),
    ("inventory-coordination", "inventory-forecasting"), ("stock-taking", "stock-replenishment"),
    ("warehouse-support", "order-fulfilment"), ("warehouse-support", "packaging"),
    ("order-fulfilment", "packaging"), ("delivery-coordination", "driving"),
    ("delivery-coordination", "logistics-coordination"), ("event-coordination", "project-coordination"),
    ("logistics-coordination", "procurement-support"), ("procurement-support", "supplier-communication"),
    ("basic-coding", "website-management"), ("online-security-basics", "data-privacy-awareness"),
    ("digital-literacy", "cloud-file-management"), ("basic-repairs", "problem-solving"),
    ("mentoring", "communication"), ("conflict-resolution", "complaint-handling"),
    ("stakeholder-communication", "communication"), ("time-management", "task-prioritisation"),
]
RELATED = {frozenset(p) for p in _RELATED_PAIRS}

SAME_CREDIT = 1.0
RELATED_CREDIT = 0.6
SAME_CATEGORY_CREDIT = 0.35
NO_CATEGORY_CREDIT = {"soft-skills"}   # two soft skills are not "the same field"

# Older spellings and the skill tags the first version of this package used.
_ALIASES = {
    "excel": "spreadsheet-management", "ms-excel": "spreadsheet-management",
    "microsoft-excel": "spreadsheet-management", "spreadsheets": "spreadsheet-management",
    "canva": "graphic-design", "social-media": "social-media-management",
    "social-media-marketing": "social-media-management", "bookkeeping": "basic-bookkeeping",
    "bookkeeping-basics": "basic-bookkeeping", "customer-support": "customer-service",
    "customer-care": "customer-service", "stock-control": "stock-taking",
    "inventory": "inventory-coordination", "data-capture": "data-entry",
    "sales": "sales-support",
}


def display_skill(slug):
    """Label for a slug, e.g. 'data-entry' -> 'data entry'. Unknown slugs are spaced out."""
    s = SKILL_BY_SLUG.get(slug)
    return s["label"] if s else str(slug).replace("-", " ")


def parse_skills(value):
    """Turn free text or a list into a de-duplicated list of skill slugs.

    Accepts slugs, labels, or the free text in the youth.skills column
    ("Data entry, Spreadsheet management"). Unknown skills are ignored.
    """
    if not value:
        return []
    items = re.split(r"[,;\n|]", value) if isinstance(value, str) else list(value)
    tags = []
    for item in items:
        tag = slugify(item)
        tag = _ALIASES.get(tag, tag)
        if tag in SKILL_BY_SLUG and tag not in tags:
            tags.append(tag)
    return tags


def skill_similarity(a, b):
    """Similarity of two skill slugs in [0, 1]."""
    if a == b:
        return SAME_CREDIT
    if frozenset((a, b)) in RELATED:
        return RELATED_CREDIT
    ca, cb = SKILL_CATEGORY.get(a), SKILL_CATEGORY.get(b)
    if ca is not None and ca == cb and ca not in NO_CATEGORY_CREDIT:
        return SAME_CATEGORY_CREDIT
    return 0.0


def skill_coverage(youth_skills, required_skills):
    """How well a youth's skills cover what a task needs.

    Each required skill takes the best-matching youth skill. Returns
    (coverage in [0, 1], [(required, best_youth_skill, similarity), ...]).
    A task with no required skills is covered by anyone.
    """
    required = list(required_skills)
    if not required:
        return 1.0, []
    details = []
    for req in required:
        best, best_sim = None, 0.0
        for ys in youth_skills:
            sim = skill_similarity(ys, req)
            if sim > best_sim:
                best, best_sim = ys, sim
        details.append((req, best, best_sim))
    return sum(d[2] for d in details) / len(details), details
