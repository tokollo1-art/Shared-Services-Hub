"""Gauteng areas with approximate centre coordinates (lat, lon).

Coordinates are approximate area centres, good enough for ranking by
distance. They are not for navigation.
"""
import math

AREAS = {
    "Soweto": (-26.2678, 27.8585),
    "Tembisa": (-25.9964, 28.2268),
    "Alexandra": (-26.1038, 28.0963),
    "Johannesburg CBD": (-26.2041, 28.0473),
    "Sandton": (-26.1076, 28.0567),
    "Randburg": (-26.0936, 28.0064),
    "Midrand": (-25.9992, 28.1263),
    "Diepsloot": (-25.9300, 28.0100),
    "Katlehong": (-26.3350, 28.1500),
    "Mamelodi": (-25.7100, 28.3600),
    "Soshanguve": (-25.5000, 28.1000),
    "Pretoria CBD": (-25.7479, 28.2293),
    "Centurion": (-25.8603, 28.1894),
    "Benoni": (-26.1885, 28.3208),
    "Germiston": (-26.2178, 28.1670),
    "Roodepoort": (-26.1625, 27.8725),
    "Krugersdorp": (-26.1000, 27.7700),
    "Orange Farm": (-26.4700, 27.8800),
    "Vereeniging": (-26.6731, 27.9261),
}


def haversine_km(lat1, lon1, lat2, lon2):
    r = 6371.0
    p1, p2 = math.radians(lat1), math.radians(lat2)
    dp, dl = p2 - p1, math.radians(lon2 - lon1)
    a = math.sin(dp / 2) ** 2 + math.cos(p1) * math.cos(p2) * math.sin(dl / 2) ** 2
    return 2 * r * math.asin(math.sqrt(a))


def coords(area):
    """Coordinates for an area name (case-insensitive), or None if unknown."""
    if not area:
        return None
    key = area.strip().lower()
    for name, c in AREAS.items():
        if name.lower() == key:
            return c
    return None


def distance_km(area_a, area_b):
    """Distance between two area names in km, or None if either is unknown."""
    a, b = coords(area_a), coords(area_b)
    if a is None or b is None:
        return None
    return haversine_km(a[0], a[1], b[0], b[1])
