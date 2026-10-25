# Puts this folder (the one holding ssh_ds/) on sys.path, so `python -m pytest`
# works from here, from the repo root, or from any other directory.
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
