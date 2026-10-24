"""Static catalogue used by the seed generator: SMEs, task templates, names."""

# (business name, industry, description, categories it posts, work mode, area,
#  employees, B-BBEE level). All businesses are fictional.
SME_SEED = [
    ("Kasi Bites Catering (Pty) Ltd", "Catering & Hospitality",
     "Township catering and event food supplier.",
     ["Logistics & Events", "Customer Support", "Retail & Sales"], "ONSITE", "Soweto", 9, "Level 1"),
    ("Ubuntu Tutoring Centre (Pty) Ltd", "Education",
     "After-school tutoring for Grades 4 to 12.",
     ["Education & Training", "Digital Operations"], "HYBRID", "Alexandra", 6, "Level 2"),
    ("Joburg Threads Apparel (Pty) Ltd", "Fashion & Retail",
     "Local streetwear label with a small online shop.",
     ["Retail & Sales", "Marketing & Creative", "Logistics & Events"], "HYBRID", "Johannesburg CBD", 8, "Level 2"),
    ("Tembisa Tech Repairs (Pty) Ltd", "IT Services",
     "Phone and laptop repairs and small-office IT support.",
     ["IT & Technical", "Customer Support"], "ONSITE", "Tembisa", 5, "Level 1"),
    ("Sandton Books & Tax (Pty) Ltd", "Accounting",
     "Bookkeeping and tax practice for small businesses.",
     ["Accounting & Finance", "Digital Operations"], "HYBRID", "Sandton", 7, "Level 4"),
    ("Midrand Print & Signs (Pty) Ltd", "Printing & Signage",
     "Banners, business cards and vehicle branding.",
     ["Marketing & Creative", "Retail & Sales", "Logistics & Events"], "ONSITE", "Midrand", 12, "Level 3"),
    ("Mamelodi Fresh Produce Co-op (Pty) Ltd", "Agriculture & Food",
     "Fresh produce supplier to spaza shops and schools.",
     ["Retail & Sales", "Logistics & Events", "Accounting & Finance"], "ONSITE", "Mamelodi", 15, "Level 1"),
    ("Centurion Web Studio (Pty) Ltd", "Digital Agency",
     "Websites and social media for local businesses.",
     ["IT & Technical", "Marketing & Creative", "Digital Operations"], "ONLINE", "Centurion", 6, "Level 4"),
    ("Katlehong Auto Spares (Pty) Ltd", "Automotive Retail",
     "Spare parts shop serving local mechanics.",
     ["Retail & Sales", "Accounting & Finance", "Customer Support"], "ONSITE", "Katlehong", 10, "Level 2"),
    ("Randburg Event Hire (Pty) Ltd", "Events",
     "Tents, chairs and sound equipment for events.",
     ["Logistics & Events", "Marketing & Creative", "Customer Support"], "ONSITE", "Randburg", 14, "Level 3"),
    ("Pretoria Legal Support Services (Pty) Ltd", "Professional Services",
     "Document preparation and admin for small law firms.",
     ["Digital Operations", "Accounting & Finance", "Customer Support"], "HYBRID", "Pretoria CBD", 8, "Level 4"),
    ("Diepsloot Daycare & Learning (Pty) Ltd", "Education",
     "Early childhood development centre.",
     ["Education & Training", "Digital Operations"], "ONSITE", "Diepsloot", 7, "Level 1"),
    ("Benoni Courier Express (Pty) Ltd", "Courier & Delivery",
     "Same-day parcel delivery across the East Rand.",
     ["Logistics & Events", "Customer Support", "Digital Operations"], "HYBRID", "Benoni", 18, "Level 2"),
    ("Roodepoort Solar Installers (Pty) Ltd", "Energy",
     "Home and small-business solar installations.",
     ["IT & Technical", "Customer Support", "Marketing & Creative"], "ONSITE", "Roodepoort", 11, "Level 3"),
    ("Orange Farm Beauty Studio (Pty) Ltd", "Beauty & Wellness",
     "Hair and nails studio with a growing online following.",
     ["Marketing & Creative", "Customer Support", "Retail & Sales"], "ONSITE", "Orange Farm", 5, "Level 1"),
]

# Task templates per category: (title, detail, required skills, tiers it suits)
TASK_TEMPLATES = {
    "Digital Operations": [
        ("Capture customer records into a spreadsheet", "Type 150 handwritten customer cards into a clean Excel sheet.", ["data_entry", "typing", "excel"], (0,)),
        ("Organise a shared Google Drive", "Sort scanned documents into a clear folder structure.", ["filing", "google_workspace"], (0, 1)),
        ("Clean up a supplier contact list", "Remove duplicates and fix formatting in a supplier list.", ["data_entry", "excel"], (0, 1)),
        ("Set up a weekly booking schedule", "Build a shared calendar and booking sheet for staff.", ["scheduling", "google_workspace", "email_admin"], (1, 2)),
        ("Build a monthly reporting spreadsheet", "Create a sales summary with formulas and charts.", ["excel", "data_entry"], (2,)),
    ],
    "Marketing & Creative": [
        ("Design five Instagram posts for a promotion", "Create branded posts in Canva for an upcoming promotion.", ["canva", "social_media", "graphic_design"], (0, 1)),
        ("Take product photos for the catalogue", "Photograph 25 products on a clean background.", ["photography"], (0, 1)),
        ("Write product descriptions for 20 items", "Write short, clear descriptions for the online shop.", ["copywriting"], (0, 1)),
        ("Edit a 60-second promo video", "Cut and caption a short promo clip for social media.", ["video_editing", "social_media"], (1, 2)),
        ("Plan a month of social media content", "Plan and schedule a month of posts across two platforms.", ["social_media", "copywriting", "canva"], (2,)),
    ],
    "Accounting & Finance": [
        ("Capture a month of receipts", "Capture receipts into the bookkeeping spreadsheet.", ["data_entry", "bookkeeping"], (0, 1)),
        ("Prepare customer invoices", "Prepare and send this month's customer invoices.", ["invoicing", "excel"], (0, 1)),
        ("Reconcile the petty cash book", "Match petty cash slips to the cash book and flag gaps.", ["cash_handling", "bookkeeping"], (1,)),
        ("Update VAT records for the quarter", "Sort invoices and update VAT records for the quarter.", ["vat_basics", "bookkeeping", "excel"], (2,)),
        ("Prepare a monthly payroll summary", "Compile hours and payments into a payroll summary.", ["payroll_basics", "excel"], (2,)),
    ],
    "Customer Support": [
        ("Answer customer WhatsApp enquiries for a week", "Reply to customer messages using quick replies.", ["whatsapp_business", "customer_service"], (0, 1)),
        ("Call 40 customers for a satisfaction survey", "Phone customers and record their feedback.", ["call_handling", "customer_service"], (0, 1)),
        ("Run the front desk on a busy weekend", "Greet customers, answer questions and take payments.", ["customer_service", "cash_handling"], (0, 1)),
        ("Handle complaints and log resolutions", "Respond to complaints and keep a resolution log.", ["complaint_handling", "customer_service"], (1, 2)),
        ("Set up a WhatsApp Business catalogue", "Create a product catalogue and quick replies.", ["whatsapp_business", "customer_service"], (1, 2)),
    ],
    "IT & Technical": [
        ("Set up email accounts for staff", "Create email accounts and shared drives for the team.", ["google_workspace", "troubleshooting"], (0, 1)),
        ("Phone and laptop troubleshooting day", "Fix common problems on staff phones and laptops.", ["troubleshooting", "mobile_support"], (0, 1)),
        ("Build a one-page business website", "Build and publish a simple one-page site.", ["html_css", "web_basics"], (1, 2)),
        ("Clean up and reinstall five office computers", "Back up, wipe and reinstall five computers.", ["hardware_support", "troubleshooting"], (1,)),
        ("Set up office Wi-Fi and a shared printer", "Install and secure the network and printer.", ["networking_basics", "hardware_support"], (2,)),
        ("Write a script to tidy a CSV of orders", "Clean and summarise a monthly orders file.", ["python_basics", "excel"], (2,)),
    ],
    "Retail & Sales": [
        ("Count and label stock", "Count shelf stock and label items by section.", ["inventory", "packing"], (0,)),
        ("Arrange the shop floor display", "Set up a clear, attractive display for the weekend.", ["merchandising"], (0, 1)),
        ("Run a weekend market stall", "Sell at a stall and handle cash.", ["sales", "cash_handling", "customer_service"], (0, 1)),
        ("Do a full stock take and reconcile", "Reconcile physical stock against the stock sheet.", ["inventory", "excel"], (1, 2)),
        ("Follow up leads and close ten sales", "Follow up warm leads over WhatsApp and phone.", ["sales", "whatsapp_business"], (2,)),
    ],
    "Education & Training": [
        ("Help Grade 8 learners with maths homework", "Support a small group with weekly maths homework.", ["tutoring", "mathematics"], (0, 1)),
        ("Run an after-school reading hour", "Lead a reading hour for primary school learners.", ["tutoring", "english", "childcare"], (0, 1)),
        ("Mark and capture test results", "Mark short tests and capture results.", ["data_entry", "mathematics"], (0,)),
        ("Prepare weekly lesson worksheets", "Prepare clear worksheets for next week's lessons.", ["english", "mathematics", "canva"], (1, 2)),
        ("Lead a Grade 11 maths revision camp", "Run a two-day revision camp before exams.", ["tutoring", "mathematics"], (2,)),
    ],
    "Logistics & Events": [
        ("Deliver parcels around the township", "Deliver a day's parcels on a planned route.", ["delivery", "time_management"], (0, 1)),
        ("Pack and label 100 customer orders", "Pack, label and stage orders for collection.", ["packing", "inventory"], (0,)),
        ("Set up and serve at a community event", "Help set up, serve food and pack down.", ["event_support", "food_service"], (0, 1)),
        ("Coordinate a small product launch day", "Coordinate suppliers, guests and timing on the day.", ["event_support", "time_management", "customer_service"], (1, 2)),
        ("Plan weekly delivery routes", "Plan efficient routes and track deliveries in a sheet.", ["delivery", "time_management", "excel"], (2,)),
    ],
}

REMOTE_OK = {"Digital Operations", "Marketing & Creative", "Accounting & Finance",
             "IT & Technical", "Education & Training", "Customer Support"}

FIRST_NAMES = [
    "Thabo", "Lerato", "Sipho", "Nomsa", "Kagiso", "Palesa", "Thandi", "Mpho",
    "Zanele", "Tshepo", "Naledi", "Bongani", "Lindiwe", "Siyabonga", "Ayanda",
    "Refilwe", "Tumelo", "Karabo", "Neo", "Lwazi", "Nandi", "Themba", "Busisiwe",
    "Katlego", "Ofentse", "Precious", "Sibusiso", "Thulani", "Zodwa", "Mandla",
    "Khanyi", "Dineo", "Rethabile", "Andile", "Boitumelo", "Cebo", "Lebo",
    "Nokuthula", "Pule", "Sandile", "Tebogo", "Vusi", "Yolanda", "Aphiwe",
    "Fikile", "Itumeleng", "Jabu", "Khaya", "Lindani", "Masego",
]
LAST_NAMES = [
    "Dlamini", "Nkosi", "Khumalo", "Mokoena", "Ndlovu", "Molefe", "Sithole",
    "Mahlangu", "Zulu", "Mthembu", "Radebe", "Tshabalala", "Maseko", "Motaung",
    "Baloyi", "Mabaso", "Shabalala", "Mokwena", "Ngcobo", "Naidoo", "Pillay",
    "Chauke", "Mazibuko", "Lekota", "Sibiya", "Mkhize", "Nxumalo", "Tau",
    "Modise", "Ledwaba", "Buthelezi", "Mashinini", "Moloi", "Ntuli", "Cele",
]

EDUCATION = [
    "Matric", "Matric", "Matric",
    "Matric + short course in Business Administration",
    "Matric + Digital Marketing youth programme",
    "Matric + Computer Literacy certificate",
    "NQF 4 Business Administration",
    "NQF 5 Higher Certificate in IT Support",
    "NQF 5 Higher Certificate in Marketing",
    "Matric + Bookkeeping short course",
]

BIO_TEMPLATES = [
    "Youth programme graduate from {area}. Keen to build real work experience in {cat}.",
    "Completed training and ready to work. Based in {area} and interested in {cat}.",
    "Hard-working and reliable. Looking for paid tasks in {cat} to build a track record.",
    "From {area}. Learning fast and looking for my first verified work in {cat}.",
]

FEEDBACK = {
    5: ["Excellent work, finished early and needed no supervision.",
        "Very reliable and careful. We would happily book again.",
        "Great attitude and top quality results."],
    4: ["Good work, delivered on time with only small fixes.",
        "Reliable and willing to learn. Solid result.",
        "Did what was asked well. Would work with again."],
    3: ["Task completed. Needed some guidance along the way.",
        "Fine result, but a little slower than expected.",
        "Okay work with room to grow."],
    2: ["Completed but needed a lot of corrections.",
        "Late and the work needed checking."],
    1: ["Did not meet the brief and needed to be redone."],
}

FUNDERS = [
    ("Highveld Growth Fund", 400000.0, "Youth Work Readiness 2026 cohort"),
    ("Rand Horizon Foundation", 250000.0, "First Experience Initiative"),
    ("Ubuntu Skills Trust", 150000.0, "Township Digital Skills cohort"),
]
