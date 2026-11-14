from __future__ import annotations

import sys
from pathlib import Path


PROJECT = Path(__file__).resolve().parents[4]
sys.path.insert(0, str(PROJECT / "tools/model3d/boss_materials_refine/spore"))
from validate import validate


if __name__ == "__main__":
    validate(work=PROJECT / "_work/boss-relics-refine-20261021/spore",
             identifier="rainbow_spore_charm", folder="boss_relics",
             bb_path=PROJECT / "art/boss_relics/blockbench/rainbow_spore_charm.bbmodel")
