import hashlib
import json
from pathlib import Path

from deliver import ROOT, ROOM, load, digest, save

BASE = ROOT.parents[1]
SOURCES = [BASE / 'deliveries/nature-b1-S8-crystal', BASE / 'deliveries/nature-b1-S8-crystal-r2', BASE / 'deliveries/nature-S8-sequential', BASE / 'deliveries/nature-batch1-group4-swamp', BASE / 'deliveries/nature-batch1-S6', BASE / 'deliveries/nature-S5-coral-ocean-20261104', Path('D:/ft工程文件/FT地牢_首批第3组_冰原霜穴_20261104'), Path('D:/ft工程文件/FT地牢_自然环境第7组_蘑菇真菌_20261104'), Path('D:/ft工程文件/FT地牢_自然环境第一批第2组_20261104')]
AIR = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air', 'minecraft:light'}


def rotated(points, size):
    width, height, depth = size
    return {(depth - 1 - block_z, block_y, block_x) for block_x, block_y, block_z in points}, [depth, height, width]


def signatures(target):
    data = load(target)
    palette = data.get('palette') or data['palettes'][0]
    size = data['size']
    occupied = {tuple(record['pos']) for record in data['blocks'] if palette[record['state']]['Name'] not in AIR}
    shapes = []
    paths = []
    for turn in range(4):
        shapes.append(hashlib.sha256(json.dumps([size, sorted(occupied)], separators=(',', ':')).encode()).hexdigest())
        paths.append({(sample_x, sample_z) for sample_x in range(31) for sample_z in range(31) if all((min(size[0] - 1, int((sample_x + 0.5) * size[0] / 31)), block_y, min(size[2] - 1, int((sample_z + 0.5) * size[2] / 31))) not in occupied for block_y in [1, 2])})
        occupied, size = rotated(occupied, size)
    return set(shapes), paths


def main():
    shape, masks = signatures(ROOM / 'room.nbt')
    records = []
    errors = []
    for root in SOURCES:
        if not root.exists():
            errors.append({'root': str(root), 'error': 'not present'})
            continue
        targets = sorted({*root.glob('incoming/*/room.nbt'), *root.glob('rooms/*/room.nbt'), *root.glob('01-large-chamber/incoming/*/room.nbt')})
        for target in targets:
            try:
                before = digest(target)
                other_shape, other_masks = signatures(target)
                if digest(target) != before:
                    raise ValueError('concurrent source change; retry in final audit')
                scores = [len(masks[0] & mask) / max(1, len(masks[0] | mask)) for mask in other_masks]
                records.append({'path': str(target), 'sha256': before, 'material_independent_exact_geometry_rotation_match': bool(shape & other_shape), 'resampled_ground_clearance_jaccard_max': round(max(scores), 4)})
            except Exception as error:
                errors.append({'path': str(target), 'error': str(error)})
    exact = [record for record in records if record['material_independent_exact_geometry_rotation_match']]
    save(ROOT / 'qa/duplicate-selfcheck.json', {'status': 'EXACT_GEOMETRY_DUPLICATE_FOUND' if exact else 'NO_EXACT_GEOMETRY_DUPLICATE_IN_READ_SNAPSHOT', 'candidate_sha256': digest(ROOM / 'room.nbt'), 'comparison_count': len(records), 'exact_matches': exact, 'highest_ground_clearance_similarities': sorted(records, key=lambda record: -record['resampled_ground_clearance_jaccard_max'])[:10], 'comparisons': records, 'unavailable_or_changed': errors, 'limitations': ['Ground mask uses occupied vs air at y1/y2 and is not engine collision or graph-isomorphism analysis.', '31x31 resampling suppresses dimension-only differences but does not prove design novelty.', 'Conceptual ring layouts may repeat despite geometry differences; final cross-group topology matrix requires coordinator acceptance.', 'Only listed existing folders read; no other group was generated, edited or reserved.'], 'final_duplicate_acceptance': 'AWAITING_COORDINATOR'})
    print('Compared', len(records), 'structures; exact material-independent matches:', len(exact), '; unavailable:', len(errors))
    if exact:
        raise SystemExit(1)


if __name__ == '__main__':
    main()
