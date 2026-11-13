import json
from pathlib import Path
from collections import Counter
import audit
from verify_dense_partitions import records, verify

root = audit.BASE / 'FT地牢_Image25接入_20261101/deliveries/S8-rebuild-single-20261104-10'
case = Path('S8-10-independent')
source = root / 'incoming/S8R_IMG10/overview.nbt'
size, blocks = records(source)
assert audit.digest(source) == audit.digest(case / 'room.nbt')
layout = json.loads((root / 'layout.json').read_text(encoding='utf-8'))
roofs = set()
for region in layout['regions']:
    left, north, right, south = region['box']
    for east in range(left, right + 1):
        for depth in range(north, south + 1):
            assert blocks[east, region['roof'], depth]['state']['Name'] not in audit.AIR
            roofs.add((east, depth))
assert len(roofs) == 2600
for east in range(23, 44):
    for south in range(65, 73):
        for height in range(13, 19):
            assert blocks[east, height, south]['state']['Name'] == 'minecraft:air'
entities = {position: record['nbt'] for position, record in blocks.items() if record['nbt'] is not None}
assert Counter(payload['id'] for payload in entities.values()) == {'minecraft:chest': 2, 'minecraft:barrel': 2}
for position, payload in entities.items():
    assert payload['Items'] == [] and not payload.get('LootTable') and not payload.get('Lock')
    if payload['id'] == 'minecraft:chest':
        east, height, south = position
        assert all(blocks[east, level, south]['state']['Name'] == 'minecraft:air' for level in (height + 1, height + 2))
for east in range(31, 36):
    assert blocks[east, 1, 72]['state']['Name'] not in audit.AIR
    assert all(blocks[east, height, 72]['state']['Name'] == 'minecraft:air' for height in range(2, 5))
assembly = json.loads((root / 'assembly.json').read_text(encoding='utf-8'))
normalized = {'source_sha256': assembly['source_sha256'], 'size': assembly['size'], 'part_count': len(assembly['parts']), 'parts': [{'step': part['order'], 'path': str(root / part['path']), 'relative_origin': part['origin_offset'], 'size': part['size'], 'sha256': part['sha256'], 'blocks': part['explicit_cells']} for part in assembly['parts']]}
assembly_path = case / 'normalized-assembly.json'
assembly_path.write_text(json.dumps(normalized, indent=2), encoding='utf-8')
partition = verify(source, assembly_path)
provenance = json.loads((root / 'qa/cutaway-provenance.json').read_text(encoding='utf-8'))
assert provenance['source_nbt_sha256'] == audit.digest(source)
derivative = root / provenance['derivative'].replace('\\', '/')
assert audit.digest(derivative) == provenance['derivative_sha256']
cut_size, cut = records(derivative)
assert cut_size == size
changed = 0
for position, record in blocks.items():
    removed = any(all(first[axis] <= position[axis] <= last[axis] for axis in range(3)) for first, last in provenance['boxes'])
    if removed:
        assert cut[position] == {'state': {'Name': 'minecraft:air'}, 'nbt': None}
        changed += record['state']['Name'] != 'minecraft:air'
    else:
        assert cut[position] == record
native = json.loads((case / 'independent-native-frozen.json').read_text(encoding='utf-8'))[0]
route = json.loads((case / 'independent-routes.json').read_text(encoding='utf-8'))[0]
boundary = json.loads((case / 'independent-boundaries.json').read_text(encoding='utf-8'))[0]
assert native['registry_properties'] == native['template_roundtrip_block_count'] == 'PASS'
assert not native['survival_failures'] and not native['survival_unsupported']
assert route['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in route['targets'])
assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and boundary['fluid_counts'] == {'minecraft:water': 40} and boundary['fluid_outside_or_air_neighbors_excluding_up_count'] == 0
reference = audit.BASE / 'image2.5/nature-dungeon-styles-20261104/S8-crystal-geode/out/S8-10-overview.png'
report_path = case / 'file-audit.json'
report = json.loads(report_path.read_text(encoding='utf-8'))
report['groups'][0].update(reference=str(reference), reference_sha256=audit.digest(reference), reference_correction='Generic helper fallback initially chose S8-01; independently replaced with the actual S8-10 reference. Only reference bookkeeping changed.')
report_path.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
from PIL import Image
canvas = Image.new('RGB', (1600, 700), '#171d23')
for index, path in enumerate((reference, root / 'views/inspection-overview-front.png')):
    image = Image.open(path).convert('RGB')
    image.thumbnail((800, 700))
    canvas.paste(image, (index * 800 + (800-image.width)//2, (700-image.height)//2))
canvas.save(case / 'S8-10-correct-reference-comparison.jpg')
result = {'status': 'PASS', 'source_sha256': audit.digest(source), 'size': size, 'roof_columns': len(roofs), 'courtyard_sky_columns_above_y12': 168, 'empty_chests': 2, 'empty_barrels': 2, 'sealed_water': 40, 'native_survival_checked': native['survival_checked'], 'body_targets': len(route['targets']), 'exact_roof_only_cut_changes': changed, 'partition': partition, 'world_ticks_gameplay_deployment': 'NOT_RUN'}
(case / 'independent-details.json').write_text(json.dumps(result, indent=2), encoding='utf-8')
print(json.dumps({key: value for key, value in result.items() if key != 'partition'}, indent=2))
