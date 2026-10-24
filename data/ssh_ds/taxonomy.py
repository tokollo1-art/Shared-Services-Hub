"""Skill taxonomy and skill similarity.

Categories are the same labels the SSH frontend already uses for tasks
(Digital Operations, Marketing & Creative, ...), plus three more.
`Youth.skills` is free text in the database, so `parse_skills` turns
"Excel, Data entry, WhatsApp Business" into canonical tags.
"""
import re

SKILLS_BY_CATEGORY = {
    "Digital Operations": ["data_entry", "excel", "filing", "typing",
                           "email_admin", "google_workspace", "scheduling"],
    "Marketing & Creative": ["canva", "social_media", "copywriting",
                             "photography", "video_editing", "graphic_design"],
    "Accounting & Finance": ["bookkeeping", "cash_handling", "invoicing",
                             "payroll_basics", "vat_basics"],
    "Customer Support": ["customer_service", "call_handling",
                         "whatsapp_business", "complaint_handling"],
    "IT & Technical": ["web_basics", "html_css", "mobile_support",
                       "hardware_support", "troubleshooting",
                       "networking_basics", "python_basics"],
    "Retail & Sales": ["sales", "inventory", "merchandising", "packing"],
    "Education & Training": ["tutoring", "mathematics", "english", "childcare"],
    "Logistics & Events": ["delivery", "event_support", "food_service",
                           "time_management"],
}

CATEGORIES = list(SKILLS_BY_CATEGORY)
SKILL_CATEGORY = {s: c for c, ss in SKILLS_BY_CATEGORY.items() for s in ss}
ALL_SKILLS = sorted(SKILL_CATEGORY)

# Explicit cross-category links: partial credit for closely related skills.
_RELATED_PAIRS = [
    ("excel", "bookkeeping"), ("data_entry", "excel"), ("data_entry", "typing"),
    ("data_entry", "bookkeeping"), ("data_entry", "inventory"),
    ("filing", "email_admin"), ("google_workspace", "email_admin"),
    ("canva", "graphic_design"), ("canva", "social_media"),
    ("social_media", "copywriting"), ("social_media", "whatsapp_business"),
    ("photography", "video_editing"), ("photography", "social_media"),
    ("graphic_design", "web_basics"), ("customer_service", "sales"),
    ("customer_service", "cash_handling"), ("whatsapp_business", "sales"),
    ("sales", "merchandising"), ("inventory", "packing"),
    ("inventory", "merchandising"), ("cash_handling", "bookkeeping"),
    ("invoicing", "bookkeeping"), ("invoicing", "excel"),
    ("payroll_basics", "excel"), ("vat_basics", "bookkeeping"),
    ("tutoring", "mathematics"), ("tutoring", "english"),
    ("tutoring", "childcare"), ("delivery", "time_management"),
    ("event_support", "food_service"), ("event_support", "customer_service"),
    ("mobile_support", "customer_service"), ("troubleshooting", "customer_service"),
    ("html_css", "web_basics"), ("python_basics", "excel"),
    ("email_admin", "english"), ("scheduling", "time_management"),
    ("call_handling", "complaint_handling"),
]
RELATED = {frozenset(p) for p in _RELATED_PAIRS}

SAME_CREDIT = 1.0
RELATED_CREDIT = 0.6
SAME_CATEGORY_CREDIT = 0.35

_DISPLAY = {"html_css": "HTML/CSS", "whatsapp_business": "WhatsApp Business",
            "vat_basics": "VAT Basics", "python_basics": "Python Basics",
            "email_admin": "Email Admin"}

_ALIASES = {
    "ms_excel": "excel", "microsoft_excel": "excel", "spreadsheets": "excel",
    "social_media_management": "social_media", "social_media_marketing": "social_media",
    "bookkeeping_basics": "bookkeeping", "customer_care": "customer_service",
    "customer_support": "customer_service", "whatsapp": "whatsapp_business",
    "html": "html_css", "css": "html_css", "python": "python_basics",
    "maths": "mathematics", "math": "mathematics", "data_capture": "data_entry",
    "stock_control": "inventory", "driving": "delivery",
}


def _normalise(text):
    return re.sub(r"[^a-z0-9]+", "_", text.lower()).strip("_")


def display_skill(tag):
    """Human-readable skill name, e.g. 'data_entry' -> 'Data Entry'."""
    return _DISPLAY.get(tag, tag.replace("_", " ").title())


def parse_skills(value):
    """Turn free text or a list into a de-duplicated list of canonical tags.

    Unknown skills are ignored. Accepts "Excel, Data Entry" or a list.
    """
    if not value:
        return []
    items = re.split(r"[,;\n|]", value) if isinstance(value, str) else list(value)
    tags = []
    for item in items:
        tag = _normalise(str(item))
        tag = _ALIASES.get(tag, tag)
        if tag in SKILL_CATEGORY and tag not in tags:
            tags.append(tag)
    return tags


def skill_similarity(a, b):
    """Similarity of two skill tags in [0, 1]."""
    if a == b:
        return SAME_CREDIT
    if frozenset((a, b)) in RELATED:
        return RELATED_CREDIT
    ca, cb = SKILL_CATEGORY.get(a), SKILL_CATEGORY.get(b)
    if ca is not None and ca == cb:
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
