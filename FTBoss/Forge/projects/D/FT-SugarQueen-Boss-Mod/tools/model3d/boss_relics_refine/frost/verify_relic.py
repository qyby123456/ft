from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[4]
sys.path.insert(0, str(ROOT / 'tools/model3d/boss_materials_refine/frost'))
from verify_frost import validate

if __name__ == '__main__':
    validate('frost_crown_charm', 'boss_relics', ROOT / 'art/boss_relics/blockbench/frost_crown_charm.bbmodel', ROOT / '_work/boss-relics-refine-20261021/frost/verification.json')
