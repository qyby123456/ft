import collections
import hashlib
import json
from pathlib import Path
import shutil
import sys
import zipfile

from PIL import Image

ROOT = Path(__file__).resolve().parent
ROOM = ROOT / 'incoming/S7R2-07'
ACCEPTANCE = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S7-06-independent/independent-details.json')
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
    assert sha(ROOT.parent / '06-corner/incoming/S7R2-06/room.nbt') == acceptance['sha256']
    (ROOT / 'evidence').mkdir(exist_ok=True)
    shutil.copyfile(ACCEPTANCE, ROOT / 'evidence/S7-06-ACCEPTANCE.snapshot.json')
    latest = json.loads((ROOT / 'qa/latest-native-run.json').read_text(encoding='utf-8'))
    native_path = Path(latest['directory']) / 'native-result.json'
    native = json.loads(native_path.read_text(encoding='utf-8'))
    assert native['status'] == 'PASS_OFFLINE_NATIVE_ONLY' and native['sha256'] == digest
    assert len(native['target_paths']) == 17 and len(native['static_survival']) == 47
    historical = []
    for candidate in sorted((ROOT / 'qa').glob('isolated-run-*/native-result.json')):
        report = json.loads(candidate.read_text(encoding='utf-8'))
        historical.append({'report': str(candidate), 'nbt_sha256': report['sha256'], 'matches_final_candidate': report['sha256'] == digest, 'role': 'FINAL_SOURCE_CHECK' if report['sha256'] == digest else 'HISTORICAL_SUPERSEDED_GEOMETRY_NOT_A_CANDIDATE'})
    write(ROOT / 'qa/historical-runs.json', {'source_sha256': digest, 'authoritative_run': latest['directory'], 'runs': historical, 'warning': 'Only incoming/S7R2-07/room.nbt is a candidate; never register isolated test copies.'})
    assert all(entry['can_survive'] for entry in native['static_survival'])
    media = json.loads((ROOT / 'qa/lodestone.json').read_text(encoding='utf-8'))
    assert len(media) == 5
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
        width, height = port['opening']
        start_x = feet_x - (1 if width == 3 else 0)
        floor_cells = [[start_x + offset, feet_y - 1, feet_z] for offset in range(width)]
        air_cells = [[point[0], block_y, point[2]] for point in floor_cells for block_y in range(feet_y, feet_y + height)]
        connections.append({**port, 'air_cells': air_cells, 'floor_cells': floor_cells, 'player_center': [feet_x + .5, feet_y, feet_z + .5], 'outside_unknown': True})
    write(ROOT / 'connections.json', {'source_sha256': digest, 'local_origin': [0, 0, 0], 'world_origin': None, 'ports': connections, 'gate': metadata['gate'], 'stairs': metadata['stairs'], 'target_feet': metadata['test_targets'], 'full_body_paths': str(native_path), 'warning': 'North 3x4 feet y2, exterior south approach 2x3 feet y1; actual open double gate at z25 feet y2. Do not connect as same-height identical apertures.'})
    rotations = {}
    for turns in range(4):
        ports = []
        for port in connections:
            rotated = dict(port)
            def turn(point, normal=False):
                block_x, block_y, block_z = point
                width, depth = 27, 31
                for unused in range(turns):
                    block_x, block_z = (-block_z if normal else depth - 1 - block_z), block_x
                    width, depth = depth, width
                return [block_x, block_y, block_z]
            for key in ['feet', 'normal', 'outside_feet']:
                rotated[key] = turn(port[key], key == 'normal')
            for key in ['air_cells', 'floor_cells']:
                rotated[key] = [turn(point) for point in port[key]]
            rotated['player_center'] = [rotated['feet'][0] + .5, rotated['feet'][1], rotated['feet'][2] + .5]
            rotated['direction'] = {(0, 0, -1): 'north', (1, 0, 0): 'east', (0, 0, 1): 'south', (-1, 0, 0): 'west'}[tuple(rotated['normal'])]
            ports.append(rotated)
        rotations[str(turns * 90)] = {'size': [27, 16, 31] if turns % 2 == 0 else [31, 16, 27], 'ports': ports}
    write(ROOT / 'rotated-connectors.json', {'source_sha256': digest, 'rule': 'clockwise XZ x=depth-1-z,z=old_x; swap width/depth EACH quarter turn. Normals x=-z,z=old_x; y unchanged; local nonnegative envelope, not world pivot', 'rotations': rotations, 'world_rotated_template_placement': 'NOT_RUN'})
    for rotation in rotations.values():
        width, height, depth = rotation['size']
        assert len(rotation['ports']) == 2
        for port in rotation['ports']:
            assert len(port['air_cells']) == port['opening'][0] * port['opening'][1] and len(port['floor_cells']) == port['opening'][0]
            assert all(0 <= point[0] < width and 0 <= point[1] < height and 0 <= point[2] < depth for point in port['air_cells'] + port['floor_cells'])
            assert port['outside_feet'][0] in [-1, width] or port['outside_feet'][2] in [-1, depth]
            assert [port['feet'][axis] + port['normal'][axis] for axis in range(3)] == port['outside_feet']
    write(ROOT / 'qa/rotation-coordinate-check.json', {'status': 'PASS_LOCAL_COORDINATES_ONLY', 'source_sha256': digest, 'rotations': 4, 'ports_per_rotation': 2, 'non_square_dimensions_swapped': True, 'all_cells_in_envelope': True, 'outside_points_one_step_beyond_boundary': True, 'runtime_rotated_placement': 'NOT_RUN'})
    write(ROOT / 'assembly.json', {'split': False, 'reason': '27x16x31 single entrance envelope, 27x26 roofed hall and 5-deep open forecourt', 'world_origin': None, 'local_origin': [0, 0, 0], 'assembly_order': [{'step': 1, 'id': metadata['id'], 'relative_origin': [0, 0, 0], 'template': str(ROOM / 'room.nbt')}], 'collision_policy': 'Reserve full bounding volume including authored air. Reject occupied conflict, no unconditional clearing.', 'permanent_previous_S7_06_boundary': 'Its L-outside quadrant is explicit air in rectangular NBT and will clear existing terrain on vanilla placement; preserve full envelope reservation or use separately implemented skip-air-quadrant placer. No such placer implemented/deployed.'})
    software = [{'path': str(path), 'sha256': sha(path), 'source_sha256': digest, 'label': 'SOFTWARE_BLOCK_RENDER_NOT_LODESTONE_NOT_GAMEPLAY'} for path in sorted((ROOT / 'views').glob('*.png'))]
    write(ROOT / 'qa/media-bindings.json', {'source_sha256': digest, 'lodestone': media, 'derivative': derivative, 'software': software})
    gate_report = json.loads((Path(latest['directory']) / 'gate-static.json').read_text(encoding='utf-8'))
    assert gate_report['source_sha256'] == digest and gate_report['closed_door_collision_blocks_entry'] and gate_report['door_halves'] == 4
    for mode in ['furnishing-eye', 'left-guard']:
        camera = json.loads((ROOT / ('qa/' + mode + '-camera.json')).read_text(encoding='utf-8'))
        assert camera['source_sha256'] == digest and not camera['installed_skill_modified'] and not camera['structure_modified']
        assert next(entry for entry in media if entry['mode'] == mode)['displayed_nbt_sha256'] == digest
    write(ROOT / 'qa/prior-boundary-permanent.json', {'prior_image': 'S7-06', 'prior_sha256': acceptance['sha256'], 'retained': True, 'boundary': 'L-outside explicit air in rectangular bounding volume erases existing terrain on vanilla placement; reserve full 25x14x25 or implement custom quadrant skip. No deployment.'})
    write(ROOT / 'qa/visual-review.json', {'status': 'VIEWED_WITH_DOCUMENTED_DEVIATIONS', 'source_sha256': digest, 'viewed': ['reference S7-07', 'Lodestone cutaway interior', 'Lodestone complete exterior', 'Lodestone front-detail', 'complete source right-board furnishing-eye', 'complete source left-guard'], 'retained': ['actual dark oak double gate with stone piers and exterior threshold step', 'rear doorway leading deeper', 'two side guard alcoves with overhead giant red mushrooms', 'left red banner and empty barrel crates; right geometric notice board and desk', 'unlit raised brazier and right-front shallow water'], 'deviations': ['double gate authored open for passage, unlike reference closed front gate', 'vanilla wooden doors are two blocks tall, not oversized custom heavy leaves', 'plain geometric board is not an actual map or readable signage', 'brazier unlit campfire over shroomlight approximates fire without active flame', 'side furniture and map texture simplified; no cyan emissive fungi, purple dynamic light, spores or concept fog'], 'not_an_acceptance': True})
    write(ROOT / 'qa/stability-boundaries.json', {'source_sha256': digest, 'static_survival': '47 points PASS, vanilla block tags bound, brightness assumption15', 'water': {'source_blocks': 3, 'pools': ['one right-front 3x1 pool'], 'bottom_and_sides_static': 'PASS', 'placement_neighbor_fluid_ticks': 'NOT_RUN', 'external_block_breaking_water_buckets': 'NOT_RUN'}, 'vegetation': {'can_spread': ['red/brown mushroom', 'vine', 'mycelium'], 'random_ticks_long_term_growth': 'NOT_RUN', 'bone_meal': 'NOT_RUN', 'support_breaking': 'NOT_RUN'}, 'light_propagation': 'NOT_RUN', 'four_rotations_world_placement': 'NOT_RUN', 'gameplay_and_deployment': 'NOT_RUN', 'registration_acceptance': 'PENDING'})
    qa = {'status': 'PASS_PRODUCER_SELF_CHECK_ONLY', 'sha256': digest, 'static': 'PASS', 'native_load_block_entities_tags': 'PASS (six empty barrels, one banner and one unlit campfire block entity)', 'native_survival_points': 47, 'native_route_targets': 17, 'routes_model': 'actual native 0.6x1.8 body, variable feet y1/1.5/2, half-grid, <=0.5 steps lift-horizontal-lower sweep and reverse, dry supported endpoints', 'native_run': latest['directory'], 'lodestone': '5/5 PASS', 'duplicates_compared': len(duplicate['compared']), 'exact_geometry_matches': 0, 'world_neighbor_random_fluid_ticks': 'NOT_RUN', 'registration_accepted': False, 'next_image_allowed': False}
    write(ROOM / 'qa.json', qa)
    pending = {'status': 'AWAITING_INDEPENDENT_REGISTRATION_ACCEPTANCE', 'group': 7, 'image': 7, 'thread_id': '01a1a5df-d735-7b43-9d3b-2939bfc6c0ed', 'reviewer_thread': '01a1a685-045b-76d2-aa2b-2a3f24520603', 'previous_image_acceptance': str(ACCEPTANCE), 'previous_image_acceptance_sha256': sha(ACCEPTANCE), 'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'], 'nbt': str(ROOM / 'room.nbt'), 'nbt_sha256': digest, 'size': metadata['size'], 'registration_accepted': False, 'next_image': 8, 'next_image_authorized': False, 'next_image_started': False, 'next_image_allowed': False, 'runtime_slot': None, 'deployed': False, 'rule': 'Wait for explicit independent acceptance of S7-07 before any S7-08 work. No pre-generation, cross-group or batch geometry reuse.'}
    write(ROOT / 'REGISTRATION_PENDING.json', pending)
    gate = ROOT.parent / 'SERIAL_GATE.json'
    if gate.exists() and not (ROOT / 'evidence/previous-serial-gate.snapshot.json').exists():
        shutil.copyfile(gate, ROOT / 'evidence/previous-serial-gate.snapshot.json')
    write(gate, pending)
    write(ROOT / 'manifest.json', {'status': pending['status'], 'rooms': [metadata], 'qa': qa, 'geometry_origin': 'Original single-image S7-07 entrance antechamber with double doors and half-block approach step recipe, previous room geometry not reused', 'previous_acceptance': str(ACCEPTANCE), 'reference_analysis': str(ROOT / 'REFERENCE_ANALYSIS.md'), 'assembly': str(ROOT / 'assembly.json'), 'connections': str(ROOT / 'connections.json'), 'preview_only_derivative': str(ROOT / 'views/PREVIEW_ONLY_cutaway.nbt'), 'next_image_started': False})
    files = sorted(path for path in ROOT.rglob('*') if path.is_file() and path.name != 'SHA256SUMS.json' and path.suffix != '.zip' and '__pycache__' not in path.parts and 'classes' not in path.parts)
    write(ROOT / 'SHA256SUMS.json', {path.relative_to(ROOT).as_posix(): sha(path) for path in files})
    archive = ROOT / 'S7R2-07-single-image-review.zip'
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
