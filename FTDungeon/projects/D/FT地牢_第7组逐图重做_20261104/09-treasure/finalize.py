import collections
import hashlib
import json
from pathlib import Path
import shutil
import sys
import zipfile

from PIL import Image

ROOT = Path(__file__).resolve().parent
ROOM = ROOT / 'incoming/S7R2-09'
ACCEPTANCE = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S7-08-independent/independent-details.json')
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
    assert sha(ROOT.parent / '08-courtyard/incoming/S7R2-08/room.nbt') == acceptance['sha256']
    (ROOT / 'evidence').mkdir(exist_ok=True)
    shutil.copyfile(ACCEPTANCE, ROOT / 'evidence/S7-08-ACCEPTANCE.snapshot.json')
    latest = json.loads((ROOT / 'qa/latest-native-run.json').read_text(encoding='utf-8'))
    native_path = Path(latest['directory']) / 'native-result.json'
    native = json.loads(native_path.read_text(encoding='utf-8'))
    assert native['status'] == 'PASS_OFFLINE_NATIVE_ONLY' and native['sha256'] == digest
    assert len(native['target_paths']) == 24 and len(native['static_survival']) == 90
    historical = []
    for candidate in sorted((ROOT / 'qa').glob('isolated-run-*/native-result.json')):
        report = json.loads(candidate.read_text(encoding='utf-8'))
        historical.append({'report': str(candidate), 'nbt_sha256': report['sha256'], 'matches_final_candidate': report['sha256'] == digest, 'role': 'FINAL_SOURCE_CHECK' if report['sha256'] == digest else 'HISTORICAL_SUPERSEDED_GEOMETRY_NOT_A_CANDIDATE'})
    write(ROOT / 'qa/historical-runs.json', {'source_sha256': digest, 'authoritative_run': latest['directory'], 'runs': historical, 'warning': 'Only incoming/S7R2-09/room.nbt is a candidate; never register isolated test copies.'})
    assert all(entry['can_survive'] for entry in native['static_survival'])
    media = json.loads((ROOT / 'qa/lodestone.json').read_text(encoding='utf-8'))
    assert len(media) == 6
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
        floor_cells = [[feet_x + (offset if along_x else 0), feet_y - 1, feet_z + (0 if along_x else offset)] for offset in [-1, 0, 1]]
        air_cells = [[point[0], block_y, point[2]] for point in floor_cells for block_y in range(feet_y, feet_y + 4)]
        connections.append({**port, 'air_cells': air_cells, 'floor_cells': floor_cells, 'player_center': [feet_x + .5, feet_y, feet_z + .5], 'outside_unknown': True})
    write(ROOT / 'connections.json', {'source_sha256': digest, 'local_origin': [0, 0, 0], 'world_origin': None, 'ports': connections, 'target_feet': metadata['test_targets'], 'full_body_paths': str(native_path), 'warning': 'Three visible ground arches: south y2, west/east y3; variable-height stair routes reach chest operating point y5. Mushroom shelves not claimed accessible. World outside unknown.'})
    rotations = {}
    for turns in range(4):
        ports = []
        for port in connections:
            rotated = dict(port)
            def turn(point, normal=False):
                block_x, block_y, block_z = point
                width, depth = 31, 29
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
        rotations[str(turns * 90)] = {'size': [31, 19, 29] if turns % 2 == 0 else [29, 19, 31], 'ports': ports}
    write(ROOT / 'rotated-connectors.json', {'source_sha256': digest, 'rule': 'clockwise XZ x=depth-1-z,z=old_x; swap width/depth EACH quarter turn. Normals x=-z,z=old_x; y unchanged; local nonnegative envelope, not world pivot', 'rotations': rotations, 'world_rotated_template_placement': 'NOT_RUN'})
    for rotation in rotations.values():
        width, height, depth = rotation['size']
        assert len(rotation['ports']) == 3
        for port in rotation['ports']:
            assert len(port['air_cells']) == 12 and len(port['floor_cells']) == 3
            assert all(0 <= point[0] < width and 0 <= point[1] < height and 0 <= point[2] < depth for point in port['air_cells'] + port['floor_cells'])
            assert port['outside_feet'][0] in [-1, width] or port['outside_feet'][2] in [-1, depth]
            assert [port['feet'][axis] + port['normal'][axis] for axis in range(3)] == port['outside_feet']
    write(ROOT / 'qa/rotation-coordinate-check.json', {'status': 'PASS_LOCAL_COORDINATES_ONLY', 'source_sha256': digest, 'rotations': 4, 'ports_per_rotation': 3, 'rectangular_dimensions_swap_each_quarter_turn': True, 'all_cells_in_envelope': True, 'outside_points_one_step_beyond_boundary': True, 'runtime_rotated_placement': 'NOT_RUN'})
    write(ROOT / 'assembly.json', {'split': False, 'reason': 'Single 31x19x29 closed cave treasure shrine with two-step elevation systems', 'world_origin': None, 'local_origin': [0, 0, 0], 'assembly_order': [{'step': 1, 'id': metadata['id'], 'relative_origin': [0, 0, 0], 'template': str(ROOM / 'room.nbt')}], 'collision_policy': 'Reserve/check full envelope including explicit air; reject conflicts. Three external connectors remain unknown outside template.', 'permanent_S7_06_boundary': 'S7-06 rectangular envelope contains explicit air outside L footprint; vanilla placement clears terrain there. Reserve full envelope or implement separately verified quadrant skipping.'})
    software = [{'path': str(path), 'sha256': sha(path), 'source_sha256': digest, 'label': 'SOFTWARE_BLOCK_RENDER_NOT_LODESTONE_NOT_GAMEPLAY'} for path in sorted((ROOT / 'views').glob('*.png'))]
    write(ROOT / 'qa/media-bindings.json', {'source_sha256': digest, 'lodestone': media, 'derivative': derivative, 'software': software})
    template = load_structure(ROOM / 'room.nbt')
    palette = template['palette']
    records = {tuple(record['pos']): palette[record['state']]['Name'] for record in template['blocks']}
    assert sum(name != 'minecraft:air' for point, name in records.items() if point[1] == 18) == 899
    assert sum(name == 'minecraft:water' for name in records.values()) == 18
    access = json.loads((Path(latest['directory']) / 'container-access.json').read_text(encoding='utf-8'))
    assert access['source_sha256'] == digest and access['status'] == 'PASS_STATIC_ACCESS_ONLY'
    assert len(access['operations']) == 7 and access['loaded_block_entities'] == 11
    assert native['containers_loaded'] == 11 and len(native['block_entity_roundtrips']) == 11
    eye_poses = json.loads((Path(latest['directory']) / 'human-eye-poses.json').read_text(encoding='utf-8'))
    assert eye_poses['source_sha256'] == digest and len(eye_poses['poses']) == 3
    for mode in ['treasure-eye', 'reverse-eye', 'chest-detail']:
        camera = json.loads((ROOT / ('qa/' + mode + '-camera.json')).read_text(encoding='utf-8'))
        assert camera['source_sha256'] == digest and not camera['structure_modified'] and not camera['installed_skill_modified']
        assert any(camera['camera_position'] == [pose['feet_center'][0], pose['eye_y'], pose['feet_center'][2]] and pose['reachable'] and pose['native_body_clear'] for pose in eye_poses['poses'])
        assert any(entry['mode'] == mode and entry['displayed_nbt_sha256'] == digest for entry in media)
    shutil.copyfile(Path(metadata['reference']), ROOT / 'evidence/S7-09-treasure.reference.png')
    prompt = Path(metadata['reference']).parent.parent / 'prompts/09-treasure.txt'
    if not prompt.exists():
        candidates = list(prompt.parent.glob('*09*'))
        assert len(candidates) == 1
        prompt = candidates[0]
    shutil.copyfile(prompt, ROOT / 'evidence/S7-09-reference-prompt.txt')
    assert sha(ROOT / 'evidence/S7-09-treasure.reference.png') == metadata['reference_sha256']
    write(ROOT / 'qa/reference-binding.json', {'source_sha256': digest, 'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'], 'bundled_reference': 'evidence/S7-09-treasure.reference.png', 'original_prompt': str(prompt), 'prompt_sha256': sha(prompt), 'recipe_sha256': sha(ROOT / 'recipe.mjs'), 'previous_geometry_reused': False})
    write(ROOT / 'qa/source-roof-and-access.json', {'status': 'PASS_STATIC_ONLY', 'source_sha256': digest, 'roof_y': 18, 'solid_roof_cells': 899, 'open_skylight': False, 'preview_cut_creates_visible_open_top': True, 'container_access': access, 'body_routes': str(native_path), 'actual_world_use': 'NOT_RUN'})
    write(ROOT / 'qa/visual-review.json', {'status': 'VIEWED_WITH_DOCUMENTED_DEVIATIONS', 'source_sha256': digest, 'viewed': ['correct reference S7-09', 'actual Lodestone air-only interior panorama', 'actual Lodestone complete exterior', 'actual Lodestone reverse exterior', 'actual Lodestone complete-source forward human eye', 'actual Lodestone complete-source reverse human eye', 'actual Lodestone complete-source chest operating-eye detail'], 'retained': ['tiered central single treasure chest shrine with half-block ascent', 'paired stone crystal-light pillars and red/yellow wall banners', 'twin elevated giant red mushrooms and secondary rear mushroom', 'left barrel supplies and right urn-like potted table', 'paired foreground framed shallow pools and three visible arches'], 'deviations': ['standard wood chest instead of illustrated golden chest, deliberately empty with no automatic loot', 'square 3x3 pools and regular full-width stair treads simplify reference organic terrain', 'large fungal shelves use solid stepped soil rather than illustrated overhang layering', 'native patterned banners roundtrip correctly, Lodestone screenshot shows simplified plain fabric', 'no teal glowing fungi, purple dynamic light, spores, fog or shader beam effects', 'vines can appear unsupported in wall-cut derivative; complete-source walls and native survival pass', 'roof and west/south walls retained in source, removed only in air-only derivative'], 'not_an_acceptance': True})
    write(ROOT / 'qa/stability-boundaries.json', {'source_sha256': digest, 'static_survival': '90 points PASS, vanilla tags bound; brightness assumption15', 'water': {'source_blocks': 18, 'pools': ['two framed 3x3 shallow pools'], 'bottom_and_sides_static': 'PASS', 'placement_neighbor_fluid_ticks': 'NOT_RUN', 'external_breaking_buckets': 'NOT_RUN'}, 'containers': {'chest': 1, 'barrels': 6, 'empty_no_loot': True, 'static_access_and_lid_clearance': 'PASS', 'real_player_opening': 'NOT_RUN', 'outside_entities_cats': 'UNKNOWN'}, 'vegetation': {'can_spread': ['red/brown mushroom', 'vine', 'mycelium'], 'random_ticks_long_term_growth': 'NOT_RUN', 'bone_meal': 'NOT_RUN', 'support_breaking': 'NOT_RUN'}, 'high_mushroom_shelves_ground_access': 'NOT_AUTHORED_NOT_CLAIMED', 'light_propagation': 'NOT_RUN', 'four_rotations_world_placement': 'NOT_RUN', 'gameplay_and_deployment': 'NOT_RUN', 'registration_acceptance': 'PENDING'})
    qa = {'status': 'PASS_PRODUCER_SELF_CHECK_ONLY', 'sha256': digest, 'static': 'PASS', 'native_load_block_entities_tags': 'PASS (1 empty chest,6 empty barrels,4 patterned banners;11 native entity roundtrips)', 'native_survival_points': 90, 'native_route_targets': 24, 'routes_model': 'actual native 0.6x1.8 body, variable feet Y2..5 on half-grid, maximum step0.5; raised edge sweep and reverse, dry supported endpoints', 'native_run': latest['directory'], 'lodestone': '6/6 PASS', 'duplicates_compared': len(duplicate['compared']), 'exact_geometry_matches': 0, 'world_neighbor_random_fluid_ticks': 'NOT_RUN', 'registration_accepted': False, 'next_image_allowed': False}
    write(ROOM / 'qa.json', qa)
    pending = {'status': 'AWAITING_INDEPENDENT_REGISTRATION_ACCEPTANCE', 'group': 7, 'image': 9, 'thread_id': '01a1a5df-d735-7b43-9d3b-2939bfc6c0ed', 'reviewer_thread': '01a1a685-045b-76d2-aa2b-2a3f24520603', 'previous_image_acceptance': str(ACCEPTANCE), 'previous_image_acceptance_sha256': sha(ACCEPTANCE), 'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'], 'nbt': str(ROOM / 'room.nbt'), 'nbt_sha256': digest, 'size': metadata['size'], 'registration_accepted': False, 'next_image': 10, 'next_image_authorized': False, 'next_image_started': False, 'next_image_allowed': False, 'runtime_slot': None, 'deployed': False, 'rule': 'Wait for explicit independent acceptance of S7-09 before any S7-10 work. No pre-generation, cross-group or batch geometry reuse.'}
    write(ROOT / 'REGISTRATION_PENDING.json', pending)
    gate = ROOT.parent / 'SERIAL_GATE.json'
    if gate.exists() and not (ROOT / 'evidence/previous-serial-gate.snapshot.json').exists():
        shutil.copyfile(gate, ROOT / 'evidence/previous-serial-gate.snapshot.json')
    write(gate, pending)
    write(ROOT / 'manifest.json', {'status': pending['status'], 'rooms': [metadata], 'qa': qa, 'geometry_origin': 'Original single-image S7-09 stepped chest shrine with two giant mushroom alcoves and three visible ground arches, previous room geometry not reused', 'previous_acceptance': str(ACCEPTANCE), 'reference_analysis': str(ROOT / 'REFERENCE_ANALYSIS.md'), 'assembly': str(ROOT / 'assembly.json'), 'connections': str(ROOT / 'connections.json'), 'preview_only_derivative': str(ROOT / 'views/PREVIEW_ONLY_cutaway.nbt'), 'next_image_started': False})
    files = sorted(path for path in ROOT.rglob('*') if path.is_file() and path.name != 'SHA256SUMS.json' and path.suffix != '.zip' and '__pycache__' not in path.parts and 'classes' not in path.parts)
    write(ROOT / 'SHA256SUMS.json', {path.relative_to(ROOT).as_posix(): sha(path) for path in files})
    archive = ROOT / 'S7R2-09-single-image-review.zip'
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
