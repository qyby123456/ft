from collections import Counter
import importlib.util
from pathlib import Path
import json
import zipfile

from prepare import ROOT, TARGET, AIR, Structure, cut, digest, save
from nbtio import plain


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def mask(audit,path):
    template = audit.decode(path)
    size = template['size']
    points = {tuple(entry['pos']) for entry in template['blocks'] if template['palette'][entry['state']]['Name'] not in audit.AIR}
    interior = {point for point in points if all(1 < point[axis] < size[axis]-2 for axis in range(3))}
    return [audit.occupancy(interior,size,rotation) for rotation in range(4)]


def main():
    source = TARGET/'room.nbt'
    sha = digest(source)
    assert sha == 'd83bb005a47dc3e49b58d4901417654badc91cd827b5664ac05660c55968206a'
    metadata = load(TARGET/'room.json')
    assert metadata['sha256'] == sha and digest(Path(metadata['reference'])) == metadata['reference_sha256']
    native = load(ROOT/'qa/native-review.json')
    survival = load(ROOT/'qa/independent-native-frozen.json')[0]
    routes = load(ROOT/'qa/independent-routes.json')[0]
    assert native['status'] == 'PASS' and native['rooms'][0]['sha256'] == sha
    room = native['rooms'][0]
    assert room['audited_targets'] == 21 and room['loaded_block_entities'] == 3
    assert len(room['chest_checks']) == 3 and all(not item['native_isChestBlockedAt'] and item['two_air_above'] and item['front_standing_clear'] for item in room['chest_checks'])
    assert survival['sha256'] == routes['sha256'] == sha
    assert survival['registry_properties'] == survival['template_roundtrip_block_count'] == 'PASS'
    assert survival['vanilla_block_tags_bound'] and survival['survival_checked'] == 20
    assert not survival['survival_failures'] and not survival['survival_unsupported']
    assert routes['start_clear'] and len(routes['targets']) == 21
    assert all(item['reachable'] and item['standing_clear'] for item in routes['targets'])
    structure = Structure.read(source)
    assert dict(Counter(state.name for state in structure.blocks.values() if state != AIR)) == load(TARGET/'materials.json')
    assert structure.data_version == 3465 and len(structure.blocks) == 29*18*29
    derivation = load(ROOT/'derivation.json')
    assert derivation['source_sha256'] == sha
    for entry in derivation['derivatives']:
        path = Path(entry['derived'])
        assert digest(path) == entry['derived_sha256'] and entry['source_sha256'] == sha
        preview = Structure.read(path)
        assert preview.size == structure.size
        changed = []
        for point,state in structure.blocks.items():
            if preview.get(point) != state:
                assert preview.get(point) == AIR and cut(entry['direction'],point)
                changed.append(point)
        assert len(changed) == entry['removed_nonair']
        assert set(changed) == {tuple(point) for point in entry['removed_positions']}
        assert {point:plain(data) for point,data in preview.block_entities.items()} == {point:plain(data) for point,data in structure.block_entities.items()}
    screenshots = load(ROOT/'qa/lodestone-screenshots.json')
    assert len(screenshots) == 7 and {item['direction'] for item in screenshots} == {'exterior','reverse','detail','north','south','west','east'}
    for entry in screenshots:
        assert not entry['errors'] and entry['source_sha256'] == sha
        assert digest(Path(entry['screenshot'])) == entry['screenshot_sha256']
        assert digest(Path(entry['rendered'])) == entry['rendered_sha256']
        if entry['direction'] in {'exterior','reverse','detail'}:
            assert Path(entry['rendered']) == source and not entry['preview_not_deployable']
        else:
            assert entry['preview_not_deployable']
            assert any(Path(item['derived']) == Path(entry['rendered']) and item['derived_sha256'] == entry['rendered_sha256'] for item in derivation['derivatives'])
    mapped = Path('D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar')
    verification = mapped.with_name('vanilla-1.20.1-local-verification.jar')
    with zipfile.ZipFile(mapped) as original, zipfile.ZipFile(verification) as local:
        classes = [name for name in original.namelist() if name.endswith('.class')]
        assert set(classes) == {name for name in local.namelist() if name.endswith('.class')}
        assert all(original.read(name) == local.read(name) for name in classes)
        removed = sorted(set(original.namelist())-set(local.namelist()))
        assert all(name.startswith('META-INF/') for name in removed)
    save(ROOT/'qa/native-runtime-provenance.json',{'mapped_source':str(mapped),'mapped_sha256':digest(mapped),'verification_runtime':str(verification),'runtime_sha256':digest(verification),'class_count':len(classes),'all_class_bytes_identical':True,'source_only_metadata':removed,'reason':'Existing local verification jar avoids stale remap signature digest error while retaining all original mapped class bytes. Read-only comparison; neither jar modified.','chest_context':'Immutable LevelAccessor proxy, real native classes, empty cat/entity query; no Level world, game menu or animation'})
    reviewer = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685')
    previous = load(ROOT.parent/'S2-08/registration.json')['previous_accepted_sources']
    assert len(previous) == 7
    acceptance = load(reviewer/'S2-08-independent/independent-details.json')
    prior = ROOT.parent/'S2-08/incoming/G2B-08/room.nbt'
    assert acceptance['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION' and digest(prior) == acceptance['sha256'] == 'eddfe98c172b340520cf869aaec58a3745c6828e1a0656dd897318369ec90cb9'
    previous.append({'image':8,'source':str(prior),'sha256':acceptance['sha256'],'acceptance_basis':str(reviewer/'S2-08-independent/independent-details.json'),'source_unchanged':True})
    for item in previous:
        assert digest(Path(item['source'])) == item['sha256']
    specification = importlib.util.spec_from_file_location('reviewer_audit',reviewer/'audit.py')
    audit = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(audit)
    peers = [(f"S2-0{item['image']}-accepted",Path(item['source'])) for item in previous] + [(f'S{group}-peer',audit.BASE/relative/candidate) for group,relative,candidate,unused in audit.GROUPS if group != 2]
    own = mask(audit,source)[0]
    comparisons = [{'peer':label,'path':str(path),'sha256':digest(path),'normalized_interior_jaccard_best_y_rotation':max(len(own & other)/len(own | other) for other in mask(audit,path))} for label,path in peers]
    save(ROOT/'qa/duplicate-selfcheck.json',{'source_sha256':sha,'scope':'Maker screening only; material-independent interior normalized occupancy excludes outer two blocks and fluids, four Y rotations. Not independent acceptance or semantic originality certification.','comparisons':comparisons})
    qa = load(TARGET/'qa.json')
    qa.update({'status':'SELF_CHECK_PASS_PENDING_INDEPENDENT_ACCEPTANCE','native_bound_tags_survival':'PASS_20_STATIC_POINTS_8_LANTERNS_6_POTS_6_BUSHES','player_volume_route_targets':21,'player_volume_routes':'PASS_OFFLINE_SHAPES_NOT_GAME_PHYSICS','chest_entities_and_inventory':'PASS_3_CHESTS_10_STACKS','chest_lid_static':'PASS_NATIVE_ISCHESTBLOCKEDAT_FALSE_3_AND_TWO_AIR_ABOVE','validator_provenance':str(reviewer),'independent_acceptance':'NOT_GRANTED','source_and_preview_hashes_verified':True})
    save(TARGET/'qa.json',qa)
    save(ROOT/'qa/chest-audit.json',{'sha256':sha,'chests':room['chest_checks'],'expected':metadata['chests'],'declared_source_entities':{','.join(map(str,point)):plain(data) for point,data in structure.block_entities.items()},'scope':room['chest_scope'],'dynamic_open_and_menus':'NOT_RUN','loot_balance':'NOT_ASSESSED_FIXED_EXAMPLES_NO_LOOTTABLE'})
    save(ROOT/'qa/dynamic-boundaries.json',{'sha256':sha,'roof':metadata['roof'],'footprint':metadata['footprint'],'explicit_air_placement':'Entire 29x18x29 envelope may overwrite blocks including sky and margins; full-volume placement audit required.','fluids':0,'hazard_blocks':0,'containers':3,'chest_static':'Native entities and ten stacks loaded; actual native isChestBlockedAt false in immutable no-entity context; two blocks air above every chest; three-block air height at each front; front targets pass body routes.','chest_dynamic':'No in-game lid animation, cat obstruction, menu opening, item extraction, networking or multiplayer performed. Fixed inventories not balanced.','sand_support':qa['supported_sand'],'native_fragile_points':20,'light_support':qa['lantern_connections'],'solid_graph':{'solid_count':qa['six_face_solids'],'reached_y0':qa['six_face_connected_to_y0'],'disconnected':qa['detached_solids']},'interfaces':metadata['connections'],'route_scope':'Three port centers and18 targets, including both dais steps, three front stand points and side access; 0.6x1.8 body, half-grid, max0.5 step, no jumps; immutable offline native shapes, not all trajectories or game physics.','brightness':'canSurvive at fixed brightness15 with real tags; no propagated natural light','world_neighbor_random_ticks_physics_interaction':'NOT_RUN','weather_snow_light_spawning':'NOT_RUN_OPEN_RUIN','rotation_mirror':'NOT_TESTED','deployment':'NOT_RUN_NOT_AUTHORIZED'})
    save(ROOT/'registration.json',{'group':2,'image':9,'id':'G2B-09','name':metadata['name'],'status':'READY_FOR_INDEPENDENT_REVIEW_NOT_ACCEPTED','candidate':str(source),'sha256':sha,'size':metadata['size'],'reference':metadata['reference'],'reference_sha256':metadata['reference_sha256'],'connections':metadata['connections'],'hall_feet_y':3,'dais_feet_y':5,'roof':metadata['roof'],'metadata':str(TARGET/'room.json'),'qa':str(TARGET/'qa.json'),'chest_audit':str(ROOT/'qa/chest-audit.json'),'assembly':str(ROOT/'assembly.json'),'derivation':str(ROOT/'derivation.json'),'screenshots':str(ROOT/'qa/lodestone-screenshots.json'),'full_source_detail':str(TARGET/'lodestone-detail.png'),'dynamic_boundaries':str(ROOT/'qa/dynamic-boundaries.json'),'native_runtime_provenance':str(ROOT/'qa/native-runtime-provenance.json'),'duplicate_selfcheck':str(ROOT/'qa/duplicate-selfcheck.json'),'previous_accepted_sources':previous,'active_reviewer_thread':'01a1a685-045b-76d2-aa2b-2a3f24520603','reviewer_messaged':False,'next_image':'S2-10 LOCKED_NOT_STARTED','world_ticks_interaction_gameplay_deployment':'NOT_RUN'})
    files = [{'path':str(path.relative_to(ROOT)),'sha256':digest(path)} for path in sorted(ROOT.rglob('*')) if path.is_file() and path.name != 'FILE_MANIFEST.json' and '__pycache__' not in path.parts]
    save(ROOT/'FILE_MANIFEST.json',{'source_sha256':sha,'files':files})
    assert all(digest(ROOT/item['path']) == item['sha256'] for item in files)
    print(f'FINALIZED {sha}; {len(files)} hashes;21 body targets;20 survival;3 native chests;PENDING independent acceptance')
    print('Duplicate screening:',[(item['peer'],round(item['normalized_interior_jaccard_best_y_rotation'],4)) for item in comparisons])


if __name__ == '__main__':
    main()
