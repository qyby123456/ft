import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import plain, read_nbt

AIR = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}
LIQUID = {'minecraft:water', 'minecraft:lava'}


def describe(path):
    data = plain(read_nbt(path)[1])
    size = data['size']
    names = [entry['Name'] for entry in data['palette']]
    occupied = {tuple(record['pos']) for record in data['blocks'] if names[record['state']] not in AIR}
    blocks = {tuple(record['pos']): names[record['state']] for record in data['blocks']}
    signature = hashlib.sha256(json.dumps(sorted(occupied)).encode()).hexdigest()
    normalized = set()
    for east in range(2, 14):
        for south in range(2, 14):
            for up in range(1, 8):
                point = (min(size[0] - 1, int((east + 0.5) * size[0] / 16)), min(size[1] - 1, int((up + 0.5) * size[1] / 8)), min(size[2] - 1, int((south + 0.5) * size[2] / 16)))
                name = blocks.get(point, 'minecraft:air')
                if name not in AIR and name not in LIQUID:
                    normalized.add((east, up, south))
    return size, signature, normalized


def main():
    current = ROOT / 'incoming/S4-09/room.nbt'
    current_size, current_signature, current_mask = describe(current)
    delivery = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries')
    roots = [folder for folder in delivery.iterdir() if folder.is_dir() and ('nature' in folder.name.lower() or 'redo' in folder.name.lower() or 'rebuild' in folder.name.lower())]
    roots += [folder for folder in Path('D:/ft工程文件').iterdir() if folder.is_dir() and '地牢' in folder.name and ('第' in folder.name or '自然环境' in folder.name) and folder != ROOT.parent]
    roots.extend([ROOT.parent / "S4-01", ROOT.parent / "S4-02", ROOT.parent / "S4-03", ROOT.parent / "S4-04", ROOT.parent / "S4-05", ROOT.parent / "S4-06", ROOT.parent / "S4-07", ROOT.parent / "S4-08"])
    paths = sorted({path for folder in roots for path in folder.rglob("*.nbt")})
    comparisons = []
    errors = []
    fingerprints = set()
    for path in paths:
        try:
            digest = hashlib.sha256(path.read_bytes()).hexdigest()
            if digest in fingerprints:
                continue
            fingerprints.add(digest)
            size, signature, mask = describe(path)
            rotations = [mask]
            for turn in range(3):
                rotations.append({(15 - south, up, east) for east, up, south in rotations[-1]})
            scores = [len(current_mask & rotated) / max(1, len(current_mask | rotated)) for rotated in rotations]
            comparisons.append({'path': str(path), 'size': size, 'sha256': digest, 'same_geometry_ignoring_material': current_size == size and current_signature == signature, 'interior_normalized_solid_jaccard_best_y_rotation': round(max(scores), 4)})
        except Exception as error:
            errors.append({'path': str(path), 'error': str(error)})
    comparisons.sort(key=lambda entry: entry['interior_normalized_solid_jaccard_best_y_rotation'], reverse=True)
    exact = [entry for entry in comparisons if entry['same_geometry_ignoring_material']]
    result = {'status': 'FAIL' if exact or errors or not comparisons else 'PASS_EXACT_DUPLICATE_CHECK', 'sha256': hashlib.sha256(current.read_bytes()).hexdigest(), 'scope': 'Available local nature-group NBT snapshot only; normalized interior solid occupancy is a coarse signal, not proof of visual originality. No old file is used as a build input.', 'current': str(current), 'compared_unique_nbt_count': len(comparisons), 'exact_duplicates': exact, 'errors': errors, 'comparisons': comparisons}
    (ROOT / 'repetition-check.json').write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({key: value for key, value in result.items() if key != 'comparisons'}, ensure_ascii=True, indent=2))
    print(json.dumps(comparisons[:5], ensure_ascii=True, indent=2))
    if result['status'] == 'FAIL':
        raise SystemExit(1)


if __name__ == '__main__':
    main()






