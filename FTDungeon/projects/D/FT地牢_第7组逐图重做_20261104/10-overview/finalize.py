import collections
import copy
import hashlib
import json
from pathlib import Path
import shutil
import sys
import zipfile

from PIL import Image

ROOT = Path(__file__).resolve().parent
ROOM = ROOT / 'incoming/S7R2-10'
sys.path.insert(0, str(ROOT))
import verify

sha = verify.sha
write = verify.write
load_structure = verify.load_structure


def structural_graph(template, digest):
    ignored = {'air', 'water', 'vine', 'glow_lichen', 'red_mushroom', 'brown_mushroom', 'amethyst_cluster', 'potted_red_mushroom', 'potted_brown_mushroom', 'lantern', 'soul_lantern'}
    solids = {tuple(record['pos']) for record in template['blocks'] if template['palette'][record['state']]['Name'].split(':')[-1] not in ignored}
    reached = {min(solids)}
    pending = collections.deque(reached)
    while pending:
        point = pending.popleft()
        for direction in [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)]:
            following = tuple(point[axis] + direction[axis] for axis in range(3))
            if following in solids and following not in reached:
                reached.add(following)
                pending.append(following)
    assert reached == solids, sorted(solids - reached)[:15]
    write(ROOT / 'qa/structural-connectivity.json', {'status': 'PASS_STATIC_FACE_ADJACENCY', 'source_sha256': digest, 'positions': len(solids), 'connected_positions': len(reached), 'seed': list(min(solids)), 'ignored': sorted(ignored), 'limits': 'Discrete block adjacency only, not stresses, neighbor updates or world physics'})


def connectors(metadata, native_path):
    ports = []
    for port in metadata['connections']:
        feet_x, feet_y, feet_z = port['feet']
        along_x = port['normal'][2] != 0
        floor_cells = [[feet_x + (offset if along_x else 0), feet_y - 1, feet_z + (0 if along_x else offset)] for offset in [-1, 0, 1]]
        air_cells = [[point[0], block_y, point[2]] for point in floor_cells for block_y in range(feet_y, feet_y + 4)]
        ports.append({**port, 'air_cells': air_cells, 'floor_cells': floor_cells, 'player_center': [feet_x + .5, feet_y, feet_z + .5], 'outside_unknown': bool(port.get('external'))})
    digest = metadata['sha256']
    write(ROOT / 'connections.json', {'source_sha256': digest, 'local_origin': [0, 0, 0], 'world_origin': None, 'ports': ports, 'target_feet': metadata['test_targets'], 'full_body_paths': str(native_path), 'limits': '11 internal zone ports directly checked across3 lanes; one external entry verified up to boundary, external world unknown. Corridor junction targets verified from common entrance.'})
    rotations = {}
    for turns in range(4):
        rotated_ports = []
        for port in ports:
            rotated = copy.deepcopy(port)
            def turn(point, normal=False):
                block_x, block_y, block_z = point
                width, depth = 79, 81
                for unused in range(turns):
                    block_x, block_z = (-block_z if normal else depth - 1 - block_z), block_x
                    width, depth = depth, width
                return [block_x, block_y, block_z]
            for key in ['feet', 'inside_feet', 'normal', 'outside_feet']:
                rotated[key] = turn(port[key], key == 'normal')
            for key in ['air_cells', 'floor_cells']:
                rotated[key] = [turn(point) for point in port[key]]
            rotated['player_center'] = [rotated['feet'][0] + .5, rotated['feet'][1], rotated['feet'][2] + .5]
            rotated['direction'] = {(0, 0, -1): 'north', (1, 0, 0): 'east', (0, 0, 1): 'south', (-1, 0, 0): 'west'}[tuple(rotated['normal'])]
            rotated_ports.append(rotated)
        size = [79, 19, 81] if turns % 2 == 0 else [81, 19, 79]
        for port in rotated_ports:
            assert all(0 <= point[axis] < size[axis] for point in port['air_cells'] + port['floor_cells'] for axis in range(3))
            assert [port['feet'][axis] + port['normal'][axis] for axis in range(3)] == port['outside_feet']
            if port.get('external'):
                assert port['outside_feet'][0] in [-1, size[0]] or port['outside_feet'][2] in [-1, size[2]]
            else:
                assert all(0 <= port['outside_feet'][axis] < size[axis] for axis in range(3))
        rotations[str(turns * 90)] = {'size': size, 'ports': rotated_ports}
    write(ROOT / 'rotated-connectors.json', {'source_sha256': digest, 'rule': 'Whole-envelope clockwise x=depth-1-z,z=old_x each quarter turn; normals x=-z,z=old_x. Swap width/depth every turn; y unchanged.', 'rotations': rotations, 'scope': 'Local connector coordinates only; not rotated block-state NBT or independently rotated parts', 'runtime_rotated_placement': 'NOT_RUN'})
    write(ROOT / 'qa/rotation-coordinate-check.json', {'source_sha256': digest, 'status': 'PASS_LOCAL_COORDINATES_ONLY', 'rotations': 4, 'ports_per_rotation': 12, 'internal_ports_stay_in_envelope': True, 'one_external_port_outside_each_rotation': True, 'part_origins_for_rotated_runtime': 'NOT_AUTHORED_NOT_CERTIFIED', 'runtime': 'NOT_RUN'})
    graph = [('entrance', 'relic-garden', 'south-link'), ('entrance', 'courtyard', 'courtyard-bend-a+b'), ('courtyard', 'supplies', 'east-link'), ('supplies', 'north-grove', 'north-cross+grove-link'), ('courtyard', 'large-hall', 'north-cross+west-spine'), ('large-hall', 'relic-garden', 'west-spine')]
    write(ROOT / 'qa/topology.json', {'source_sha256': digest, 'zones': metadata['layout']['zones'], 'corridor_rectangles': metadata['layout']['corridor_rectangles'], 'logical_edges': graph, 'proof': 'All zone key points, junction targets, port plane and internal side endpoints reached by the same native body BFS; all3 lanes direct swept both directions. This edge list describes that geometry, not a replacement for path evidence.', 'native_evidence': str(native_path.parent / 'zone-connectivity.json')})


def main():
    metadata = json.loads((ROOM / 'room.json').read_text(encoding='utf-8'))
    digest = sha(ROOM / 'room.nbt')
    assert metadata['sha256'] == digest
    acceptance = json.loads(verify.ACCEPTANCE.read_text(encoding='utf-8'))
    assert acceptance['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION'
    assert sha(ROOT.parent / '09-treasure/incoming/S7R2-09/room.nbt') == acceptance['sha256']
    latest = json.loads((ROOT / 'qa/latest-native-run.json').read_text(encoding='utf-8'))
    native_path = Path(latest['directory']) / 'native-result.json'
    native = json.loads(native_path.read_text(encoding='utf-8'))
    assert native['status'] == 'PASS_OFFLINE_NATIVE_ONLY' and native['sha256'] == digest
    assert len(native['target_paths']) == 72 and len(native['static_survival']) == 208
    assert native['containers_loaded'] == 11 and len(native['block_entity_roundtrips']) == 11
    assert all(point['can_survive'] for point in native['static_survival'])
    zone = json.loads((native_path.parent / 'zone-connectivity.json').read_text(encoding='utf-8'))
    assert zone['source_sha256'] == digest and len(zone['zones']) == 6 and len(zone['ports']) == 12
    assert all(port['all_three_lanes_direct_bidirectional_sweep'] for port in zone['ports'])
    partition = json.loads((native_path.parent / 'native-part-reassembly.json').read_text(encoding='utf-8'))
    assert partition['source_sha256'] == digest and partition['positions'] == 121581 and len(partition['parts']) == 4
    assert partition['status'] == 'PASS_NATIVE_TEMPLATE_AND_EXACT_REASSEMBLY'
    access = json.loads((native_path.parent / 'container-access.json').read_text(encoding='utf-8'))
    assert access['source_sha256'] == digest and len(access['operations']) == 8 and access['status'] == 'PASS_STATIC_ACCESS_ONLY'
    media = json.loads((ROOT / 'qa/lodestone.json').read_text(encoding='utf-8'))
    assert len(media) == 13
    for entry in media:
        assert entry['status'] == 'PASS' and not entry['errors'] and entry['source_nbt_sha256'] == digest
        assert sha(Path(entry['displayed_nbt'])) == entry['displayed_nbt_sha256'] and sha(Path(entry['screenshot'])) == entry['screenshot_sha256']
        with Image.open(entry['screenshot']) as image:
            image.verify()
    poses = json.loads((native_path.parent / 'human-eye-poses.json').read_text(encoding='utf-8'))
    assert poses['source_sha256'] == digest and len(poses['poses']) == 9
    for definition in metadata['layout']['cameras']:
        mode = definition['mode']
        camera = json.loads((ROOT / ('qa/' + mode + '-camera.json')).read_text(encoding='utf-8'))
        assert camera['source_sha256'] == digest and not camera['structure_modified'] and not camera['installed_skill_modified']
        assert any(camera['camera_position'] == [pose['feet_center'][0], pose['eye_y'], pose['feet_center'][2]] and pose['reachable'] and pose['native_body_clear'] for pose in poses['poses'])
        assert any(entry['mode'] == mode and entry['displayed_nbt_sha256'] == digest for entry in media)
    cut = json.loads((ROOT / 'views/derivative.json').read_text(encoding='utf-8'))
    assert cut['source_sha256'] == digest and cut['status'] == 'PASS_AIR_ONLY_DERIVATION'
    assert cut['derivative_sha256'] == sha(ROOT / 'views/PREVIEW_ONLY_cutaway.nbt')
    original = load_structure(ROOM / 'room.nbt')
    derivative = load_structure(ROOT / 'views/PREVIEW_ONLY_cutaway.nbt')
    original_records = {tuple(record['pos']): record for record in original['blocks']}
    changed = []
    for record in derivative['blocks']:
        before = original_records[tuple(record['pos'])]
        if record != before:
            assert derivative['palette'][record['state']]['Name'] == 'minecraft:air' and not record.get('nbt')
            changed.append({'position': record['pos'], 'before': before, 'after_state': record['state']})
    assert changed == cut['exact_before_records']
    duplicate = json.loads((ROOT / 'qa/duplicate-review.json').read_text(encoding='utf-8'))
    assert not any(entry['exact_material_independent_geometry_match'] for entry in duplicate['compared'])
    assert len(duplicate['compared']) == 36
    structural_graph(original, digest)
    connectors(metadata, native_path)
    finalize_artifacts(metadata, native, latest, media, cut, duplicate, digest)


def finalize_artifacts(metadata, native, latest, media, cut, duplicate, digest):
    evidence = ROOT / 'evidence'
    evidence.mkdir(exist_ok=True)
    shutil.copyfile(verify.ACCEPTANCE, evidence / 'S7-09-ACCEPTANCE.snapshot.json')
    shutil.copyfile(metadata['reference'], evidence / 'S7-10-overview.reference.png')
    prompt = Path(metadata['reference']).parent.parent / 'prompts/S7-10-overview.txt'
    shutil.copyfile(prompt, evidence / 'S7-10-reference-prompt.txt')
    assert sha(evidence / 'S7-10-overview.reference.png') == metadata['reference_sha256']
    write(ROOT / 'qa/reference-binding.json', {'source_sha256': digest, 'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'], 'bundled_reference': 'evidence/S7-10-overview.reference.png', 'prompt_sha256': sha(prompt), 'recipe_sha256': sha(ROOT / 'recipe.mjs'), 'geometry_reuse': 'NONE; no previous room NBT or recipe geometry imported', 'test_tooling': 'Adapted native and file auditing tools; local audit_tools.py included'})
    historical = []
    for directory in sorted((ROOT / 'qa').glob('isolated-run-*')):
        source_hash = sha(directory / 'room.nbt')
        report_path = directory / 'native-result.json'
        report = json.loads(report_path.read_text(encoding='utf-8')) if report_path.exists() else None
        historical.append({'directory': str(directory), 'source_sha256': source_hash, 'same_as_final': source_hash == digest, 'authoritative': str(directory) == latest['directory'], 'status': report['status'] if report else 'FAILED_OR_INCOMPLETE_AUDIT_SEE_COMPILE_AND_RUN_LOGS', 'candidate': False})
    write(ROOT / 'qa/historical-runs.json', {'source_sha256': digest, 'authoritative_run': latest['directory'], 'runs': historical, 'notes': ['run01 was earlier geometry with centerline-only port checks', 'run02 compile mismatch Vec3i/BlockPos; fixed audit tool', 'run03/04 stricter3-lane test found entrance west corner obstruction; final source moved port south2 and widened corridor', 'final passed run is authoritative; old isolated copies are not candidates'], 'only_candidate': str(ROOM / 'room.nbt')})
    shutil.copyfile(ROOT / 'lodestone-interior.png', ROOM / 'preview.png')
    write(ROOT / 'qa/media-bindings.json', {'source_sha256': digest, 'lodestone': media, 'derivative': cut, 'label': 'ACTUAL_LODESTONE_GPU_PREVIEW_NOT_GAMEPLAY', 'internal_panorama': 'AIR_ONLY_DERIVATIVE; complete source shown by external panorama and9 reachable eye views'})
    write(ROOT / 'qa/visual-review.json', {'status': 'VIEWED_WITH_DOCUMENTED_DEVIATIONS', 'source_sha256': digest, 'viewed': ['correct S7-10 reference and prompt', 'complete-source exterior panorama and reverse exterior', 'air-only cutaway panorama and reverse cutaway', 'complete-source six zone eyes plus corner, reverse courtyard and hall stair eyes'], 'retained': ['one connected multiroom complex, differently sized six zones', 'northwest high pillared great hall with floor medallion and descending half-block stairs', 'northeast red/brown mushroom chamber and shallow pool', 'central giant mushroom dais with ring water and four dry causeways under authored courtyard opening', 'east supply room with real containers', 'southwest crystal plinth garden and shallow pool', 'southeast stone arch entry and corner corridors'], 'deviations': ['room and corridor proportions adapted to79x81 grid; spatial arrangement is an engineering interpretation rather than pixel-exact traced plan', 'rooms retain tall source walls; image is cutaway, preview cuts only air with exact ledger', 'regular angular pools, repeatable mosaic floor and block soil simplify organic reference terrain', 'no decorative fence bridge; corridor retains solid walls and clear3-lane portals', 'vanilla amethyst, shroomlights, lanterns and lichen replace cyan/purple dynamic glow, spores and fog', 'no previous room copied; hall is spacious one level, gallery or upper routes not claimed', 'removed roofs/walls can leave hanging lights/vines apparently detached in derivative; source supported and native survival passed', 'native banner Patterns retained; viewer may show simplified fabric'], 'not_an_acceptance': True})
    write(ROOT / 'qa/stability-boundaries.json', {'source_sha256': digest, 'static_survival': '208 selected authored fragile positions PASS; official block tags bound, brightness15 assumption', 'water': {'source_blocks': metadata['water_blocks'], 'bottom_and_sides': 'PASS_STATIC', 'fluid_neighbor_ticks': 'NOT_RUN'}, 'containers': {'empty_chests': 2, 'empty_barrels': 6, 'patterned_banners': 3, 'native_entity_roundtrip': 'PASS11', 'static_operation_points': 'PASS8;reachable,eye-to-face<4.5,sampled101-point ray', 'conservative_lid_full_block_above': 'PASS_BOTH_CHESTS', 'real_ui_and_lid_animation': 'NOT_RUN', 'external_cats_entities': 'UNKNOWN'}, 'vegetation_random_ticks_growth_bone_meal_support_breaking': 'NOT_RUN', 'source_courtyard_skylight': '396 original air cells; whether world above is clear UNKNOWN', 'world_light_propagation': 'NOT_RUN', 'part_assembly': 'EXACT_OFFLINE_REASSEMBLY_ONLY;runtime_world_placement_NOT_RUN', 'rotation_world_placement': 'NOT_RUN', 'gameplay_registration_deployment': 'NOT_RUN', 'permanent_S7_06': 'Explicit air outside L footprint clears terrain; reserve full envelope or separately verify quadrant skipping', 'previous_S7_08_upper_ledge_ground_access': 'NOT_AUTHORED_NOT_ACCEPTED', 'independent_acceptance': 'PENDING'})
    qa = {'status': 'PASS_PRODUCER_SELF_CHECK_ONLY', 'sha256': digest, 'native_survival_points': len(native['static_survival']), 'native_route_targets': len(native['target_paths']), 'reachable_half_grid_nodes': native['reachable_half_grid_nodes'], 'zones': 6, 'ports': 12, 'all_port_three_lanes': 'PASS_BIDIRECTIONAL', 'native_entities': 11, 'native_parts': 4, 'reassembly': 'PASS121581_FULL_RECORDS', 'lodestone': '13/13 PASS', 'duplicates_compared': len(duplicate['compared']), 'geometry_reuse': False, 'native_run': latest['directory'], 'registration_accepted': False, 'next_image_allowed': False, 'world_ticks_gameplay_deployment': 'NOT_RUN'}
    write(ROOM / 'qa.json', qa)
    pending = {'status': 'AWAITING_INDEPENDENT_REGISTRATION_ACCEPTANCE', 'group': 7, 'image': 10, 'thread_id': '01a1a5df-d735-7b43-9d3b-2939bfc6c0ed', 'reviewer_thread': '01a1a685-045b-76d2-aa2b-2a3f24520603', 'previous_image_acceptance': str(verify.ACCEPTANCE), 'previous_image_acceptance_sha256': sha(verify.ACCEPTANCE), 'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'], 'nbt': str(ROOM / 'room.nbt'), 'nbt_sha256': digest, 'size': metadata['size'], 'parts': str(ROOT / 'assembly.json'), 'registration_accepted': False, 'next_image': 11, 'next_image_authorized': False, 'next_image_started': False, 'next_image_allowed': False, 'runtime_slot': None, 'deployed': False, 'rule': 'S7-10 overview delivered for independent acceptance. No11 authorization; no deployment or runtime integration completed.'}
    write(ROOT / 'REGISTRATION_PENDING.json', pending)
    gate = ROOT.parent / 'SERIAL_GATE.json'
    if gate.exists() and not (evidence / 'previous-serial-gate.snapshot.json').exists():
        shutil.copyfile(gate, evidence / 'previous-serial-gate.snapshot.json')
    write(gate, pending)
    write(ROOT / 'manifest.json', {'status': pending['status'], 'complex': metadata, 'qa': qa, 'geometry_origin': metadata['geometry_origin'], 'assembly': str(ROOT / 'assembly.json'), 'connections': str(ROOT / 'connections.json'), 'preview_only_derivative': str(ROOT / 'views/PREVIEW_ONLY_cutaway.nbt'), 'next_image_started': False, 'deployed': False})
    files = sorted(path for path in ROOT.rglob('*') if path.is_file() and path.name != 'SHA256SUMS.json' and path.suffix != '.zip' and '__pycache__' not in path.parts and 'classes' not in path.parts)
    write(ROOT / 'SHA256SUMS.json', {path.relative_to(ROOT).as_posix(): sha(path) for path in files})
    archive = ROOT / 'S7R2-10-overview-review.zip'
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
