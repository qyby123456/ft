import collections
import hashlib
import json
from pathlib import Path
import shutil
import sys
import zipfile

from PIL import Image

ROOT = Path(__file__).resolve().parent
ROOM = ROOT / 'incoming/S7R2-06'
ACCEPTANCE = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S7-05-ACCEPTANCE.json')
sys.path.insert(0, 'D:/ft工程文件/血棘祭炉祭坛厅地牢_20261101/tools')
from structure_io import load_structure


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def structural_graph(template):
    palette = template['palette']
    ignored = {'air', 'water', 'vine', 'glow_lichen', 'red_mushroom', 'brown_mushroom', 'amethyst_cluster', 'potted_red_mushroom', 'lantern', 'soul_lantern'}
    solids = {tuple(record['pos']) for record in template['blocks'] if palette[record['state']]['Name'].split(':')[-1] not in ignored}
    reached = {(0, 0, 0)}
    pending = collections.deque(reached)
    while pending:
        point = pending.popleft()
        for direction in [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)]:
            following = tuple(point[axis] + direction[axis] for axis in range(3))
            if following in solids and following not in reached:
                reached.add(following)
                pending.append(following)
    assert reached == solids, 'Detached structural block group'
    write(ROOT / 'qa/structural-connectivity.json', {'status': 'PASS_STATIC_FACE_ADJACENCY', 'structural_positions': len(solids), 'foundation_connected': len(reached), 'ignored_nonstructural_ids': sorted(ignored), 'limits': 'Discrete authored-block adjacency, not real engineering stresses, block update test or world physics.'})


def main():
    metadata = json.loads((ROOM / 'room.json').read_text(encoding='utf-8'))
    digest = sha(ROOM / 'room.nbt')
    assert metadata['sha256'] == digest
    acceptance = json.loads(ACCEPTANCE.read_text(encoding='utf-8'))
    assert acceptance['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION'
    assert sha(ROOT.parent / '05-corridor/incoming/S7R2-05/room.nbt') == acceptance['nbt_sha256']
    (ROOT / 'evidence').mkdir(exist_ok=True)
    shutil.copyfile(ACCEPTANCE, ROOT / 'evidence/S7-05-ACCEPTANCE.snapshot.json')
    latest = json.loads((ROOT / 'qa/latest-native-run.json').read_text(encoding='utf-8'))
    native_path = Path(latest['directory']) / 'native-result.json'
    native = json.loads(native_path.read_text(encoding='utf-8'))
    assert native['status'] == 'PASS_OFFLINE_NATIVE_ONLY' and native['sha256'] == digest
    assert len(native['target_paths']) == 17 and len(native['static_survival']) == 49
    historical = []
    for candidate in sorted((ROOT / 'qa').glob('isolated-run-*/native-result.json')):
        report = json.loads(candidate.read_text(encoding='utf-8'))
        historical.append({'report': str(candidate), 'nbt_sha256': report['sha256'], 'matches_final_candidate': report['sha256'] == digest, 'role': 'FINAL_SOURCE_CHECK' if report['sha256'] == digest else 'HISTORICAL_SUPERSEDED_GEOMETRY_NOT_A_CANDIDATE'})
    write(ROOT / 'qa/historical-runs.json', {'source_sha256': digest, 'authoritative_run': latest['directory'], 'runs': historical, 'warning': 'Only incoming/S7R2-06/room.nbt is a candidate; never register isolated test copies.'})
    assert all(entry['can_survive'] for entry in native['static_survival'])
    media = json.loads((ROOT / 'qa/lodestone.json').read_text(encoding='utf-8'))
    assert len(media) == 3
    for entry in media:
        assert entry['status'] == 'PASS' and not entry['errors'] and entry['source_nbt_sha256'] == digest
        assert sha(Path(entry['displayed_nbt'])) == entry['displayed_nbt_sha256'] and sha(Path(entry['screenshot'])) == entry['screenshot_sha256']
        with Image.open(entry['screenshot']) as image:
            image.verify()
    derivative = json.loads((ROOT / 'views/derivative.json').read_text(encoding='utf-8'))
    assert derivative['status'] == 'PASS_AIR_ONLY_DERIVATION' and derivative['source_sha256'] == digest
    assert derivative['derivative_sha256'] == sha(ROOT / 'views/PREVIEW_ONLY_cutaway.nbt')
    duplicate = json.loads((ROOT / 'qa/duplicate-review.json').read_text(encoding='utf-8'))
    assert not any(entry['exact_material_independent_geometry_match'] for entry in duplicate['compared'])
    assert len(list(ROOT.glob('incoming/*/room.nbt'))) == 1
    structural_graph(load_structure(ROOM / 'room.nbt'))
    shutil.copyfile(ROOT / 'lodestone-interior.png', ROOM / 'preview.png')
    connections = []
    for port in metadata['connections']:
        feet_x, feet_y, feet_z = port['feet']
        along_x = port['normal'][2] != 0
        floor_cells = [[feet_x + (offset if along_x else 0), 1, feet_z + (0 if along_x else offset)] for offset in [-1, 0, 1]]
        air_cells = [[point[0], block_y, point[2]] for point in floor_cells for block_y in [2, 3, 4, 5]]
        connections.append({**port, 'air_cells': air_cells, 'floor_cells': floor_cells, 'player_center': [feet_x + .5, 2, feet_z + .5], 'outside_unknown': True})
    write(ROOT / 'connections.json', {'source_sha256': digest, 'local_origin': [0, 0, 0], 'world_origin': None, 'ports': connections, 'target_feet': metadata['test_targets'], 'full_body_paths': str(native_path), 'warning': 'Exactly two perpendicular 3x4 ports, north and east; orientation selected locally from image L-turn. No third branch. World outside unknown.'})
    rotations = {}
    for turns in range(4):
        ports = []
        for port in connections:
            rotated = dict(port)
            def turn(point, normal=False):
                block_x, block_y, block_z = point
                width, depth = 25, 25
                for unused in range(turns):
                    block_x, block_z = (-block_z if normal else depth - 1 - block_z), block_x
                    width, depth = depth, width
                return [block_x, block_y, block_z]
            for key in ['feet', 'normal', 'outside_feet']:
                rotated[key] = turn(port[key], key == 'normal')
            for key in ['air_cells', 'floor_cells']:
                rotated[key] = [turn(point) for point in port[key]]
            rotated['player_center'] = [rotated['feet'][0] + .5, 2, rotated['feet'][2] + .5]
            rotated['direction'] = {(0, 0, -1): 'north', (1, 0, 0): 'east', (0, 0, 1): 'south', (-1, 0, 0): 'west'}[tuple(rotated['normal'])]
            ports.append(rotated)
        rotations[str(turns * 90)] = {'size': [25, 14, 25] if turns % 2 == 0 else [25, 14, 25], 'ports': ports}
    write(ROOT / 'rotated-connectors.json', {'source_sha256': digest, 'rule': 'clockwise XZ x=depth-1-z,z=old_x; swap width/depth EACH quarter turn. Normals x=-z,z=old_x; y unchanged; local nonnegative envelope, not world pivot', 'rotations': rotations, 'world_rotated_template_placement': 'NOT_RUN'})
    for rotation in rotations.values():
        width, height, depth = rotation['size']
        assert len(rotation['ports']) == 2
        for port in rotation['ports']:
            assert len(port['air_cells']) == 12 and len(port['floor_cells']) == 3
            assert all(0 <= point[0] < width and 0 <= point[1] < height and 0 <= point[2] < depth for point in port['air_cells'] + port['floor_cells'])
            assert port['outside_feet'][0] in [-1, width] or port['outside_feet'][2] in [-1, depth]
            assert [port['feet'][axis] + port['normal'][axis] for axis in range(3)] == port['outside_feet']
    write(ROOT / 'qa/rotation-coordinate-check.json', {'status': 'PASS_LOCAL_COORDINATES_ONLY', 'source_sha256': digest, 'rotations': 4, 'ports_per_rotation': 2, 'square_dimensions_preserved': True, 'all_cells_in_envelope': True, 'outside_points_one_step_beyond_boundary': True, 'runtime_rotated_placement': 'NOT_RUN'})
    write(ROOT / 'assembly.json', {'split': False, 'reason': '25x14x25 envelope contains real 456-column L footprint; unused quadrant remains explicit air, not square room; single template', 'world_origin': None, 'local_origin': [0, 0, 0], 'assembly_order': [{'step': 1, 'id': metadata['id'], 'relative_origin': [0, 0, 0], 'template': str(ROOM / 'room.nbt')}], 'collision_policy': 'Reserve/check full bounding envelope including unused quadrant explicit air; reject conflicts, never unconditional clearing. Air outside L would otherwise erase terrain under vanilla placement.'})
    software = [{'path': str(path), 'sha256': sha(path), 'source_sha256': digest, 'label': 'SOFTWARE_BLOCK_RENDER_NOT_LODESTONE_NOT_GAMEPLAY'} for path in sorted((ROOT / 'views').glob('*.png'))]
    write(ROOT / 'qa/media-bindings.json', {'source_sha256': digest, 'lodestone': media, 'derivative': derivative, 'software': software})
    template = load_structure(ROOM / 'room.nbt')
    palette = template['palette']
    footprint = {(block_x, block_z) for block_x in range(25) for block_z in range(25) if block_x <= 11 or block_z >= 13}
    outside_records = [record for record in template['blocks'] if (record['pos'][0], record['pos'][2]) not in footprint]
    assert len(footprint) == 456 and len(outside_records) == 169 * 14
    assert all(palette[record['state']]['Name'] == 'minecraft:air' for record in outside_records)
    roof = [record for record in template['blocks'] if record['pos'][1] == 13 and palette[record['state']]['Name'] != 'minecraft:air']
    assert len(roof) == 456
    write(ROOT / 'qa/footprint.json', {'source_sha256': digest, 'status': 'PASS_STATIC_L_FOOTPRINT', 'bounding_size': [25, 14, 25], 'occupied_projection_columns': 456, 'roof_columns': len(roof), 'unused_quadrant_columns': 169, 'unused_quadrant_explicit_air_records': len(outside_records), 'rule': 'x<=11 or z>=13; unused x>=12,z<=12 all air', 'air_placement_risk': 'Vanilla template includes explicit air even outside L. Do not place blindly: full bounding volume reservation required unless custom placer explicitly skips unused quadrant air.'})
    write(ROOT / 'qa/visual-review.json', {'status': 'VIEWED_WITH_DOCUMENTED_DEVIATIONS', 'source_sha256': digest, 'viewed': ['reference S7-06', 'Lodestone air-only cutaway interior', 'Lodestone complete L exterior', 'Lodestone turn-detail'], 'retained': ['two perpendicular corridor arms and thick inner corner wall', 'stepped corner marker with shroomlight', 'large elevated inner-wall red mushroom and secondary shelf mushrooms', 'two outer-edge shallow pools and low amethyst accents', 'fungal soil ledges, small mushrooms, high wall vines'], 'deviations': ['rectangular pools instead of irregular illustrated puddles', 'amethyst substitutes purple luminous fungi without purple dynamic light', 'no cyan emissive hanging growth, spores or concept fog', 'square voxel ledges simplify reference organic layering', 'north-to-east orientation selected for engineering; not a world orientation inferred from image'], 'not_an_acceptance': True})
    write(ROOT / 'qa/stability-boundaries.json', {'source_sha256': digest, 'static_survival': '49 points PASS, vanilla block tags bound, brightness assumption15', 'water': {'source_blocks': 8, 'pools': ['two outer-side 2x2 pools'], 'bottom_and_sides_static': 'PASS', 'placement_neighbor_fluid_ticks': 'NOT_RUN', 'external_block_breaking_water_buckets': 'NOT_RUN'}, 'vegetation': {'can_spread': ['red/brown mushroom', 'vine', 'mycelium'], 'random_ticks_long_term_growth': 'NOT_RUN', 'bone_meal': 'NOT_RUN', 'support_breaking': 'NOT_RUN'}, 'light_propagation': 'NOT_RUN', 'four_rotations_world_placement': 'NOT_RUN', 'gameplay_and_deployment': 'NOT_RUN', 'registration_acceptance': 'PENDING'})
    qa = {'status': 'PASS_PRODUCER_SELF_CHECK_ONLY', 'sha256': digest, 'static': 'PASS', 'native_load_block_entities_tags': 'PASS (zero block entities or loot containers)', 'native_survival_points': 49, 'native_route_targets': 17, 'routes_model': 'actual native 0.6x1.8 body, y2 planar half-grid, each edge swept and reversed, dry supported endpoints', 'native_run': latest['directory'], 'lodestone': '3/3 PASS', 'duplicates_compared': len(duplicate['compared']), 'exact_geometry_matches': 0, 'world_neighbor_random_fluid_ticks': 'NOT_RUN', 'registration_accepted': False, 'next_image_allowed': False}
    write(ROOM / 'qa.json', qa)
    pending = {'status': 'AWAITING_INDEPENDENT_REGISTRATION_ACCEPTANCE', 'group': 7, 'image': 6, 'thread_id': '01a1a5df-d735-7b43-9d3b-2939bfc6c0ed', 'reviewer_thread': '01a1a685-045b-76d2-aa2b-2a3f24520603', 'previous_image_acceptance': str(ACCEPTANCE), 'previous_image_acceptance_sha256': sha(ACCEPTANCE), 'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'], 'nbt': str(ROOM / 'room.nbt'), 'nbt_sha256': digest, 'size': metadata['size'], 'registration_accepted': False, 'next_image': 7, 'next_image_authorized': False, 'next_image_started': False, 'next_image_allowed': False, 'runtime_slot': None, 'deployed': False, 'rule': 'Wait for explicit independent acceptance of S7-06 before any S7-07 work. No pre-generation, cross-group or batch geometry reuse.'}
    write(ROOT / 'REGISTRATION_PENDING.json', pending)
    gate = ROOT.parent / 'SERIAL_GATE.json'
    if gate.exists() and not (ROOT / 'evidence/previous-serial-gate.snapshot.json').exists():
        shutil.copyfile(gate, ROOT / 'evidence/previous-serial-gate.snapshot.json')
    write(gate, pending)
    write(ROOT / 'manifest.json', {'status': pending['status'], 'rooms': [metadata], 'qa': qa, 'geometry_origin': 'Original single-image S7-06 L-shaped single-floor turning corridor recipe, previous room geometry not reused', 'previous_acceptance': str(ACCEPTANCE), 'reference_analysis': str(ROOT / 'REFERENCE_ANALYSIS.md'), 'assembly': str(ROOT / 'assembly.json'), 'connections': str(ROOT / 'connections.json'), 'preview_only_derivative': str(ROOT / 'views/PREVIEW_ONLY_cutaway.nbt'), 'next_image_started': False})
    files = sorted(path for path in ROOT.rglob('*') if path.is_file() and path.name != 'SHA256SUMS.json' and path.suffix != '.zip' and '__pycache__' not in path.parts and 'classes' not in path.parts)
    write(ROOT / 'SHA256SUMS.json', {path.relative_to(ROOT).as_posix(): sha(path) for path in files})
    archive = ROOT / 'S7R2-06-single-image-review.zip'
    with zipfile.ZipFile(archive, 'w', compression=zipfile.ZIP_DEFLATED) as package:
        for path in [*files, ROOT / 'SHA256SUMS.json']:
            package.write(path, path.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(archive) as package:
        assert package.testzip() is None
        sums = json.loads(package.read('SHA256SUMS.json'))
        assert all(hashlib.sha256(package.read(filename)).hexdigest() == expected for filename, expected in sums.items())
    print(json.dumps({'status': pending['status'], 'nbt_sha256': digest, 'archive': str(archive), 'archive_sha256': sha(archive), 'checksummed_files': len(files), 'next_image_started': False}, ensure_ascii=False))


if __name__ == '__main__':
    main()
