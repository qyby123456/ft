import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
REVISION = ROOT.parent
SOURCE = REVISION / 'incoming/G2B-01-R2/room.nbt'
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import AIR, Structure


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    expected = '336edadd450ee6bd1bf52abe737a483110823b1cf8625e3687d866a26a1aeb4a'
    assert digest(SOURCE) == expected
    manifest = json.loads((REVISION / 'FILE_MANIFEST.json').read_text(encoding='utf-8'))
    assert len(manifest) == 33
    assert all(digest(REVISION / path) == sha for path, sha in manifest.items())
    rules = {'north': 'y>=4 and 6<=z<=9', 'south': 'y>=4 and z>=27', 'west': 'y>=4 and x<=3 and z>=6', 'east': 'y>=4 and x>=21 and z>=6'}
    reports = []
    for direction in rules:
        source = Structure.read(SOURCE)
        removed = []
        for position, block in list(source.blocks.items()):
            east, up, south = position
            roof = up >= 16 and south >= 6
            near = up >= 4 and {'north': 6 <= south <= 9, 'south': south >= 27, 'west': east <= 3 and south >= 6, 'east': east >= 21 and south >= 6}[direction]
            if (roof or near) and block != AIR:
                removed.append(list(position))
                source.set(position, AIR)
        target = ROOT / f'preview-only-{direction}.nbt'
        source.write(target)
        reread = Structure.read(target)
        original = Structure.read(SOURCE)
        changes = [point for point in original.blocks if original.get(point) != reread.get(point)]
        assert len(changes) == len(removed)
        assert all(reread.get(point) == AIR for point in changes)
        assert all(original.get(point) == reread.get(point) for point in original.blocks if point[2] < 6)
        reports.append({'direction': direction, 'source': str(SOURCE), 'source_sha256': expected, 'derived': str(target), 'derived_sha256': digest(target), 'rules': ['remove roof/parapet y>=16 within hall z>=6', 'remove ONLY near-side shell band: ' + rules[direction]], 'removed_nonair_blocks': len(removed), 'only_air_replacements': True, 'entrance_stairs_landing_rails_unchanged': True, 'opposite_three_walls_unchanged_except_shared_roof_and_cut_corner_strips': True, 'deployable': False})
    record = {'status': 'SUPPLEMENT_ONLY_NOT_ACCEPTANCE', 'source_sha256': expected, 'original_manifest_sha256': digest(REVISION / 'FILE_MANIFEST.json'), 'original_33_entries_verified_unchanged': True, 'derivatives': reports, 'next_image': 'S2-02 LOCKED'}
    (ROOT / 'derivation.json').write_text(json.dumps(record, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print('FOUR_DIRECTION_DERIVATIVES_ONLY; original 33 entries unchanged')


if __name__ == '__main__':
    main()
