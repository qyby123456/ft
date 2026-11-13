import collections
import hashlib
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont
from deliver import ROOT, ROOM, digest, load, save
from duplicate_check import SOURCES

AIR = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air', 'minecraft:light'}
FONT = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 16)


def rotate(points, size):
    return {(size[2] - 1 - block_z, block_y, block_x) for block_x, block_y, block_z in points}, [size[2], size[1], size[0]]


def components(points):
    remaining = set(points)
    result = []
    while remaining:
        seed = remaining.pop()
        pending = [seed]
        found = {seed}
        while pending:
            block_x, block_z = pending.pop()
            for adjacent in [(block_x + 1, block_z), (block_x - 1, block_z), (block_x, block_z + 1), (block_x, block_z - 1)]:
                if adjacent in remaining:
                    remaining.remove(adjacent)
                    found.add(adjacent)
                    pending.append(adjacent)
        result.append(found)
    return result


def describe(target):
    before = digest(target)
    data = load(target)
    palette = data.get('palette') or data['palettes'][0]
    size = data['size']
    occupied = {tuple(record['pos']) for record in data['blocks'] if palette[record['state']]['Name'] not in AIR}
    planes = []
    for feet_y in range(1, size[1] - 1):
        walk = {(block_x, block_z) for block_x in range(1, size[0] - 1) for block_z in range(1, size[2] - 1) if (block_x, feet_y - 1, block_z) in occupied and (block_x, feet_y, block_z) not in occupied and (block_x, feet_y + 1, block_z) not in occupied}
        planes.append((len(walk), -feet_y, walk))
    count, negative_y, walk = max(planes, key=lambda item: item[:2])
    feet_y = -negative_y
    interior = {(block_x, block_z) for block_x in range(1, size[0] - 1) for block_z in range(1, size[2] - 1)}
    holes = [component for component in components(interior - walk) if len(component) >= 4 and all(block_x not in (1, size[0] - 2) and block_z not in (1, size[2] - 2) for block_x, block_z in component)]
    topology = {'dominant_floor_feet_y': feet_y, 'conservative_supported_clearance_cells': count, 'walk_components': len(components(walk)), 'bounded_obstacles_area_at_least_4': sorted([len(component) for component in holes], reverse=True)}
    exact = set()
    volumes = []
    ground = []
    heightfields = []
    for turn in range(4):
        exact.add(hashlib.sha256(json.dumps([size, sorted(occupied)], separators=(',', ':')).encode()).hexdigest())
        field = set()
        for sample_x in range(23):
            block_x = 2 + min(size[0] - 5, int((sample_x + 0.5) * (size[0] - 4) / 23))
            for sample_z in range(23):
                block_z = 2 + min(size[2] - 5, int((sample_z + 0.5) * (size[2] - 4) / 23))
                for sample_y in range(15):
                    block_y = feet_y + min(size[1] - feet_y - 2, int((sample_y + 0.5) * (size[1] - feet_y - 1) / 15))
                    if (block_x, block_y, block_z) in occupied:
                        field.add((sample_x, sample_y, sample_z))
        volumes.append(field)
        mask = {(sample_x, sample_z) for sample_x in range(31) for sample_z in range(31) if all((min(size[0] - 2, 1 + int((sample_x + 0.5) * (size[0] - 2) / 31)), level, min(size[2] - 2, 1 + int((sample_z + 0.5) * (size[2] - 2) / 31))) not in occupied for level in (feet_y, feet_y + 1))}
        ground.append(mask)
        heightfields.append({(sample_x, sample_z): sum((sample_x, sample_y, sample_z) in field for sample_y in range(15)) for sample_x in range(23) for sample_z in range(23)})
        occupied, size = rotate(occupied, size)
    if digest(target) != before:
        raise ValueError('Source changed during read')
    return {'path': str(target), 'sha256': before, 'size': data['size'], 'topology': topology, 'exact': exact, 'volume': volumes, 'ground': ground, 'heights': heightfields}


def iou(first, second):
    return len(first & second) / max(1, len(first | second))


def picture(candidate, comparisons):
    panels = [(candidate, 0, 'S8-01 R2: 35x21x35')]
    for other, record in comparisons[:3]:
        panels.append((other, record['best_interior_rotation'], Path(other['path']).parent.name + ' / ' + str(other['size'])))
    image = Image.new('RGB', (1200, 690), '#111923')
    draw = ImageDraw.Draw(image)
    draw.text((20, 12), '材质无关对照：上=主层净空，深色为障碍；下=归一化内部实心高度', font=FONT, fill='white')
    for index, (item, turn, label) in enumerate(panels):
        left = index * 300 + 12
        draw.text((left, 52), label, font=FONT, fill='white')
        for sample_x in range(31):
            for sample_z in range(31):
                color = '#9dccd6' if (sample_x, sample_z) in item['ground'][turn] else '#303947'
                draw.rectangle((left + sample_x * 8, 90 + sample_z * 8, left + sample_x * 8 + 7, 90 + sample_z * 8 + 7), fill=color)
        for sample_x in range(23):
            for sample_z in range(23):
                height = item['heights'][turn][sample_x, sample_z]
                color = (35 + height * 13, 40 + height * 10, 45 + height * 8)
                draw.rectangle((left + sample_x * 11, 390 + sample_z * 11, left + sample_x * 11 + 10, 390 + sample_z * 11 + 10), fill=color)
    image.save(ROOT / 'qa/takeover-geometry-comparison.png')


def main():
    candidate = describe(ROOM / 'room.nbt')
    roots = [*SOURCES, ROOT.parents[1] / 'deliveries/S6-redo-single-20261104-01']
    targets = {target for source in roots if source.exists() for pattern in ('incoming/*/room.nbt', 'rooms/*/room.nbt', '01-large-chamber/incoming/*/room.nbt') for target in source.glob(pattern)}
    targets.add(ROOT / 'checkpoint-original/incoming/S8R_IMG01/room.nbt')
    records = []
    available = []
    errors = []
    for target in sorted(targets):
        try:
            other = describe(target)
            scores = [iou(candidate['volume'][0], volume) for volume in other['volume']]
            best_turn = max(range(4), key=scores.__getitem__)
            record = {key: other[key] for key in ('path', 'sha256', 'size', 'topology')}
            record.update(exact_material_independent_rotation_match=bool(candidate['exact'] & other['exact']), normalized_interior_solid_iou=round(scores[best_turn], 4), best_interior_rotation=best_turn, normalized_dominant_floor_clearance_iou=round(max(iou(candidate['ground'][0], mask) for mask in other['ground']), 4), candidate_eligibility='EXCLUDED_FAILURE_OR_BASELINE_COMPARISON_ONLY' if 'S8' in str(target) else 'READ_ONLY_OTHER_GROUP_COMPARISON')
            records.append(record)
            available.append((other, record))
        except Exception as error:
            errors.append({'path': str(target), 'error': str(error)})
    records.sort(key=lambda item: -item['normalized_interior_solid_iou'])
    exact = [item for item in records if item['exact_material_independent_rotation_match']]
    high = [item for item in records if item['normalized_interior_solid_iou'] >= 0.9]
    save(ROOT / 'qa/takeover-material-independent.json', {'status': 'NEEDS_REVIEW' if exact or high or errors else 'SCREENING_PASS_NO_EXACT_OR_HIGH_INTERIOR_MATCH', 'candidate_sha256': candidate['sha256'], 'candidate_size': candidate['size'], 'candidate_topology': candidate['topology'], 'comparison_count': len(records), 'exact_matches': exact, 'high_interior_matches_at_0_9': high, 'comparisons': records, 'errors': errors, 'methods': ['Vanilla block names and state values discarded before every geometric comparison.', 'Four rotations of exact full-volume solid coordinates.', 'Dominant supported two-cell clearance plane detected independently per room, not hardcoded y1/y2.', '31x31 horizontal normalization of dominant-floor clearance.', '23x15x23 interior solid occupancy normalization; boundary/foundation/roof excluded to prevent box-shell dominance.', 'Connected floor components and bounded obstacle areas are coarse topology descriptors, not graph-isomorphism proof.'], 'interpretation': '0.9 is a screening trigger only, not a creative-uniqueness guarantee. A shared central-ring motif is required by this reference and may recur. Human composition assessment remains separate.', 'other_groups_written': False, 'old_s8_outputs_are_candidates': False})
    available.sort(key=lambda pair: -pair[1]['normalized_interior_solid_iou'])
    picture(candidate, available)
    final = load(ROOM / 'room.nbt')
    inspection_path = ROOT / 'qa/inspection-only/inspection-cutaway-not-deliverable.nbt'
    inspection = load(inspection_path)
    states = {tuple(item['pos']): final['palette'][item['state']] for item in final['blocks']}
    inspected = {tuple(item['pos']): inspection['palette'][item['state']] for item in inspection['blocks']}
    changes = [position for position in states if states[position] != inspected[position]]
    assert final['size'] == inspection['size'] and set(states) == set(inspected)
    assert all(inspected[position]['Name'] == 'minecraft:air' and (position[1] == 20 or (position[2] <= 1 or position[0] <= 1) and position[1] >= 3 or 4 <= position[0] <= 30 and position[1] == 19 and 4 <= position[2] <= 6 or 4 <= position[2] <= 30 and position[1] == 19 and 4 <= position[0] <= 6) for position in changes)
    browser = json.loads((ROOT / 'qa/takeover-lodestone.json').read_text(encoding='utf-8'))
    for item in browser['records']:
        assert item['served_sha256'] == (digest(ROOM / 'room.nbt') if item['label'] == 'complete' else digest(inspection_path))
        assert not item['page_errors']
        with Image.open(ROOT / item['screenshot']) as image:
            image.verify()
    save(ROOT / 'qa/takeover-preview-provenance.json', {'status': 'PASS', 'final_nbt_sha256': digest(ROOM / 'room.nbt'), 'inspection_nbt_sha256': digest(inspection_path), 'changed_positions': len(changes), 'all_changes_are_explicit_inspection_air_cuts': True, 'final_roof_preserved': True, 'inspection_nbt_deliverable': False, 'browser_hashes_and_images_verified': True})
    print(json.dumps({'compared': len(records), 'exact_matches': len(exact), 'high_interior_matches': len(high), 'errors': len(errors), 'top_three': [{key: item[key] for key in ('path', 'normalized_interior_solid_iou', 'normalized_dominant_floor_clearance_iou')} for item in records[:3]], 'candidate_topology': candidate['topology']}, ensure_ascii=False))
    if exact or high or errors:
        raise SystemExit(1)


if __name__ == '__main__':
    main()
