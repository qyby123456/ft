import collections
import hashlib
import json
from pathlib import Path
import shutil
import sys
import zipfile

from PIL import Image

ROOT = Path(__file__).resolve().parent
ROOM = ROOT / 'incoming/S7R2-08'
ACCEPTANCE = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S7-07-independent/independent-details.json')
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
    assert sha(ROOT.parent / '07-entrance/incoming/S7R2-07/room.nbt') == acceptance['sha256']
    (ROOT / 'evidence').mkdir(exist_ok=True)
    shutil.copyfile(ACCEPTANCE, ROOT / 'evidence/S7-07-ACCEPTANCE.snapshot.json')
    latest = json.loads((ROOT / 'qa/latest-native-run.json').read_text(encoding='utf-8'))
    native_path = Path(latest['directory']) / 'native-result.json'
    native = json.loads(native_path.read_text(encoding='utf-8'))
    assert native['status'] == 'PASS_OFFLINE_NATIVE_ONLY' and native['sha256'] == digest
    assert len(native['target_paths']) == 16 and len(native['static_survival']) == 87
    historical = []
    for candidate in sorted((ROOT / 'qa').glob('isolated-run-*/native-result.json')):
        report = json.loads(candidate.read_text(encoding='utf-8'))
        historical.append({'report': str(candidate), 'nbt_sha256': report['sha256'], 'matches_final_candidate': report['sha256'] == digest, 'role': 'FINAL_SOURCE_CHECK' if report['sha256'] == digest else 'HISTORICAL_SUPERSEDED_GEOMETRY_NOT_A_CANDIDATE'})
    write(ROOT / 'qa/historical-runs.json', {'source_sha256': digest, 'authoritative_run': latest['directory'], 'runs': historical, 'warning': 'Only incoming/S7R2-08/room.nbt is a candidate; never register isolated test copies.'})
    assert all(entry['can_survive'] for entry in native['static_survival'])
    media = json.loads((ROOT / 'qa/lodestone.json').read_text(encoding='utf-8'))
    assert len(media) == 4
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
    write(ROOT / 'connections.json', {'source_sha256': digest, 'local_origin': [0, 0, 0], 'world_origin': None, 'ports': connections, 'target_feet': metadata['test_targets'], 'full_body_paths': str(native_path), 'warning': 'Two ground-level 3x4 ports, north and east; local engineering orientation. Upper U ledge has no authored ground stairs; upper routes NOT_RUN. World outside unknown.'})
    rotations = {}
    for turns in range(4):
        ports = []
        for port in connections:
            rotated = dict(port)
            def turn(point, normal=False):
                block_x, block_y, block_z = point
                width, depth = 33, 33
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
        rotations[str(turns * 90)] = {'size': [33, 23, 33] if turns % 2 == 0 else [33, 23, 33], 'ports': ports}
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
    write(ROOT / 'assembly.json', {'split': False, 'reason': 'Single 33x23x33 open courtyard with 17x17 authored skylight', 'world_origin': None, 'local_origin': [0, 0, 0], 'assembly_order': [{'step': 1, 'id': metadata['id'], 'relative_origin': [0, 0, 0], 'template': str(ROOM / 'room.nbt')}], 'collision_policy': 'Reserve/check full envelope including explicit air; reject conflicts. Whether external world above skylight is open remains unknown.', 'permanent_S7_06_boundary': 'S7-06 rectangular envelope contains explicit air outside L footprint; vanilla placement clears terrain there. Reserve full envelope or implement separately verified quadrant skipping.'})
    software = [{'path': str(path), 'sha256': sha(path), 'source_sha256': digest, 'label': 'SOFTWARE_BLOCK_RENDER_NOT_LODESTONE_NOT_GAMEPLAY'} for path in sorted((ROOT / 'views').glob('*.png'))]
    write(ROOT / 'qa/media-bindings.json', {'source_sha256': digest, 'lodestone': media, 'derivative': derivative, 'software': software})
    template = load_structure(ROOM / 'room.nbt')
    palette = template['palette']
    records = {tuple(record['pos']): palette[record['state']]['Name'] for record in template['blocks']}
    sky = [[block_x, 22, block_z] for block_x in range(8, 25) for block_z in range(8, 25)]
    assert len(sky) == 289 and all(records[tuple(point)] == 'minecraft:air' for point in sky)
    assert sum(name != 'minecraft:air' for point, name in records.items() if point[1] == 22) == 800
    assert sum(name == 'minecraft:water' for name in records.values()) == 100
    upper = json.loads((Path(latest['directory']) / 'upper-ledge-support.json').read_text(encoding='utf-8'))
    assert upper['source_sha256'] == digest and upper['native_sturdy_fence_floors'] == 71
    camera = json.loads((ROOT / 'qa/courtyard-camera.json').read_text(encoding='utf-8'))
    assert camera['source_sha256'] == digest and not camera['structure_modified'] and not camera['installed_skill_modified']
    assert any(entry['mode'] == 'courtyard-eye' and entry['displayed_nbt_sha256'] == digest for entry in media)
    write(ROOT / 'qa/skylight.json', {'status': 'PASS_AUTHORED_SOURCE_AIR', 'source_sha256': digest, 'top_y': 22, 'air_cells': sky, 'air_count': 289, 'solid_top_rim_count': 800, 'source_low_camera': 'lodestone-courtyard-eye.png', 'skylight_created_by_preview_cut': False, 'external_world_above': 'UNKNOWN', 'upper_ledge': upper})
    write(ROOT / 'qa/visual-review.json', {'status': 'VIEWED_WITH_DOCUMENTED_DEVIATIONS', 'source_sha256': digest, 'viewed': ['reference S7-08', 'Lodestone air-only interior', 'Lodestone complete exterior', 'Lodestone complete-source skylight detail', 'Lodestone complete-source courtyard eye'], 'retained': ['open skylight over courtyard', 'tiered central crystal spire and ring water basin', 'U-shaped high ledges with wood railings', 'large red shelf mushrooms at several heights', 'two ground arches and separate shallow puddle'], 'deviations': ['angular ring basin and rectangular 4x4 puddle simplify organic water contours', 'vanilla amethyst substitutes illustrated purple luminous growth, no dynamic purple light', 'no cyan emissive vines, particles, fog or shader beams', 'upper ledges have no ground stairs because reference shows none; ground-to-upper access not claimed', 'high west vines can look detached in air-only wall-cut preview; source walls and native survival verified'], 'not_an_acceptance': True})
    write(ROOT / 'qa/stability-boundaries.json', {'source_sha256': digest, 'static_survival': '87 points PASS, vanilla block tags bound, brightness assumption15', 'water': {'source_blocks': 100, 'pools': ['central ring basin and separate 4x4 shallow pool'], 'bottom_and_sides_static': 'PASS', 'placement_neighbor_fluid_ticks': 'NOT_RUN', 'external_block_breaking_water_buckets': 'NOT_RUN'}, 'vegetation': {'can_spread': ['red/brown mushroom', 'vine', 'mycelium'], 'random_ticks_long_term_growth': 'NOT_RUN', 'bone_meal': 'NOT_RUN', 'support_breaking': 'NOT_RUN'}, 'light_propagation': 'NOT_RUN', 'four_rotations_world_placement': 'NOT_RUN', 'gameplay_and_deployment': 'NOT_RUN', 'registration_acceptance': 'PENDING'})
    qa = {'status': 'PASS_PRODUCER_SELF_CHECK_ONLY', 'sha256': digest, 'static': 'PASS', 'native_load_block_entities_tags': 'PASS (zero block entities or loot containers)', 'native_survival_points': 87, 'native_route_targets': 16, 'routes_model': 'actual native 0.6x1.8 body, y2 planar half-grid, each edge swept and reversed, dry supported endpoints', 'native_run': latest['directory'], 'lodestone': '4/4 PASS', 'duplicates_compared': len(duplicate['compared']), 'exact_geometry_matches': 0, 'world_neighbor_random_fluid_ticks': 'NOT_RUN', 'registration_accepted': False, 'next_image_allowed': False}
    write(ROOM / 'qa.json', qa)
    pending = {'status': 'AWAITING_INDEPENDENT_REGISTRATION_ACCEPTANCE', 'group': 7, 'image': 8, 'thread_id': '01a1a5df-d735-7b43-9d3b-2939bfc6c0ed', 'reviewer_thread': '01a1a685-045b-76d2-aa2b-2a3f24520603', 'previous_image_acceptance': str(ACCEPTANCE), 'previous_image_acceptance_sha256': sha(ACCEPTANCE), 'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'], 'nbt': str(ROOM / 'room.nbt'), 'nbt_sha256': digest, 'size': metadata['size'], 'registration_accepted': False, 'next_image': 9, 'next_image_authorized': False, 'next_image_started': False, 'next_image_allowed': False, 'runtime_slot': None, 'deployed': False, 'rule': 'Wait for explicit independent acceptance of S7-08 before any S7-09 work. No pre-generation, cross-group or batch geometry reuse.'}
    write(ROOT / 'REGISTRATION_PENDING.json', pending)
    gate = ROOT.parent / 'SERIAL_GATE.json'
    if gate.exists() and not (ROOT / 'evidence/previous-serial-gate.snapshot.json').exists():
        shutil.copyfile(gate, ROOT / 'evidence/previous-serial-gate.snapshot.json')
    write(gate, pending)
    write(ROOT / 'manifest.json', {'status': pending['status'], 'rooms': [metadata], 'qa': qa, 'geometry_origin': 'Original single-image S7-08 open skylight crystal fountain courtyard recipe, previous room geometry not reused', 'previous_acceptance': str(ACCEPTANCE), 'reference_analysis': str(ROOT / 'REFERENCE_ANALYSIS.md'), 'assembly': str(ROOT / 'assembly.json'), 'connections': str(ROOT / 'connections.json'), 'preview_only_derivative': str(ROOT / 'views/PREVIEW_ONLY_cutaway.nbt'), 'next_image_started': False})
    files = sorted(path for path in ROOT.rglob('*') if path.is_file() and path.name != 'SHA256SUMS.json' and path.suffix != '.zip' and '__pycache__' not in path.parts and 'classes' not in path.parts)
    write(ROOT / 'SHA256SUMS.json', {path.relative_to(ROOT).as_posix(): sha(path) for path in files})
    archive = ROOT / 'S7R2-08-single-image-review.zip'
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
