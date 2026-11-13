from collections import Counter
from copy import deepcopy
import importlib.util
import json
from pathlib import Path
import zipfile

from prepare import ROOT, TARGET, AIR, Structure, cut, digest, save
from nbtio import plain, integer


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def entities(structure):
    return {point:plain(data) for point,data in structure.block_entities.items()}


def mask(audit,path):
    template = audit.decode(path)
    size = template['size']
    points = {tuple(entry['pos']) for entry in template['blocks'] if template['palette'][entry['state']]['Name'] not in audit.AIR}
    interior = {point for point in points if all(1 < point[axis] < size[axis]-2 for axis in range(3))}
    return [audit.occupancy(interior,size,rotation) for rotation in range(4)]


def main():
    source = TARGET/'room.nbt'
    sha = digest(source)
    assert sha == '77bc56487db554f6e6734701e54c0b8530f8490d813001d63f3d2ab0ebfbe957'
    metadata = load(TARGET/'room.json')
    assert metadata['sha256'] == sha and digest(Path(metadata['reference'])) == metadata['reference_sha256']
    native = load(ROOT/'qa/native-review.json')
    survival = load(ROOT/'qa/independent-native-frozen.json')[0]
    routes = load(ROOT/'qa/independent-routes.json')[0]
    assert native['status'] == 'PASS' and native['rooms'][0]['sha256'] == sha
    room = native['rooms'][0]
    assert room['audited_targets'] == 42 and room['loaded_block_entities'] == 7 and room['empty_barrels'] == 2
    assert len(room['chest_checks']) == 5 and all(not item['native_isChestBlockedAt'] and item['two_air_above'] and item['front_standing_clear'] for item in room['chest_checks'])
    assert survival['sha256'] == routes['sha256'] == sha
    assert survival['registry_properties'] == survival['template_roundtrip_block_count'] == 'PASS'
    assert survival['vanilla_block_tags_bound'] and survival['survival_checked'] == 42
    assert not survival['survival_failures'] and not survival['survival_unsupported']
    assert routes['start_clear'] and len(routes['targets']) == 42 and all(item['reachable'] and item['standing_clear'] for item in routes['targets'])
    structure = Structure.read(source)
    assert dict(Counter(state.name for state in structure.blocks.values() if state != AIR)) == load(TARGET/'materials.json')
    assert structure.data_version == 3465 and len(structure.blocks) == 65*18*67
    for connector in metadata['connections']:
        east,up,south = connector['position']
        for offset in range(-3,4):
            assert structure.get((east+offset,up-1,south)) != AIR
            assert all(structure.get((east+offset,height,south)) == AIR for height in range(up,up+4))
    link_checks = []
    for link in metadata['internal_links']:
        (left,front),(right,back) = link['bounds_xz_inclusive']
        floor_clear = []
        if link['axis'] == 'x':
            points = [(east,south) for east in range(left,right+1) for south in range(front+1,back)]
        else:
            points = [(east,south) for east in range(left+1,right) for south in range(front,back+1)]
        for east,south in points:
            assert structure.get((east,2,south)) != AIR
            assert all(structure.get((east,up,south)) == AIR for up in range(3,8)),(link['id'],east,south)
            floor_clear.append([east,south])
        link_checks.append({'id':link['id'],'clear_floor_columns':len(floor_clear),'height':5,'width':link['clear_width'],'all_columns_clear_supported':True})
    save(ROOT/'qa/internal-links.json',{'sha256':sha,'links':link_checks,'external_port':'7x4 north feetY1 supported; two steps to hallY3','scope':'Every column of each declared internal tunnel walking aperture, five air high and solid floor; native body route through every link center separately passed.'})
    assembly = load(ROOT/'assembly.json')
    assert assembly['source_sha256'] == sha and len(assembly['tiles']) == 4
    rebuilt = Structure(structure.size)
    seen = set()
    for entry in assembly['tiles']:
        path = Path(entry['path'])
        assert digest(path) == entry['sha256']
        tile = Structure.read(path)
        assert list(tile.size) == entry['size'] and max(tile.size) <= 48 and len(tile.blocks) == entry['explicit_cells']
        offset = entry['offset']
        for local,state in tile.blocks.items():
            point = tuple(local[axis]+offset[axis] for axis in range(3))
            assert point not in seen
            seen.add(point)
            entity = deepcopy(tile.block_entities.get(local))
            if entity is not None:
                for axis,value in zip(['x','y','z'],point):
                    if axis in entity.value:
                        entity.value[axis] = integer(value)
            rebuilt.set(point,state,entity)
    assert seen == set(structure.blocks) and rebuilt.blocks == structure.blocks and entities(rebuilt) == entities(structure)
    reconstructed = Path(assembly['reconstruction']['reconstructed'])
    assert digest(reconstructed) == assembly['reconstruction']['reconstructed_sha256']
    check = Structure.read(reconstructed)
    assert check.blocks == structure.blocks and entities(check) == entities(structure)
    save(ROOT/'qa/tile-reconstruction.json',{'source_sha256':sha,'status':'PASS','cells_checked':len(seen),'nonoverlap':True,'no_missing_cells':True,'exact_air_and_nonair_states':True,'all_seven_entities_exact':True,'each_axis_at_most_48':True,'reconstructed_sha256':digest(reconstructed),'source_entities':{','.join(map(str,point)):plain(data) for point,data in structure.block_entities.items()},'scope':'Decoded tiles rebased into source coordinates; exhaustive dictionary equality of all78390 states and all7 entity payloads. Not block placement simulation.'})
    derivation = load(ROOT/'derivation.json')
    assert derivation['source_sha256'] == sha and len(derivation['derivatives']) == 4
    for entry in derivation['derivatives']:
        path = Path(entry['derived'])
        assert digest(path) == entry['derived_sha256'] and entry['source_sha256'] == sha
        preview = Structure.read(path)
        assert preview.size == structure.size and entities(preview) == entities(structure)
        changed = []
        for point,state in structure.blocks.items():
            if preview.get(point) != state:
                assert preview.get(point) == AIR and cut(entry['direction'],point)
                changed.append(point)
        assert len(changed) == entry['removed_nonair'] and set(changed) == {tuple(point) for point in entry['removed_positions']}
    screenshots = load(ROOT/'qa/lodestone-screenshots.json')
    full = {'exterior','reverse','detail','grand','ritual','gallery','shrine'}
    assert len(screenshots) == 11 and {item['direction'] for item in screenshots} == full|{'north','south','west','east'}
    for entry in screenshots:
        assert not entry['errors'] and entry['source_sha256'] == sha
        assert digest(Path(entry['screenshot'])) == entry['screenshot_sha256'] and digest(Path(entry['rendered'])) == entry['rendered_sha256']
        if entry['direction'] in full:
            assert Path(entry['rendered']) == source and not entry['preview_not_deployable']
        else:
            assert entry['preview_not_deployable'] and any(Path(item['derived']) == Path(entry['rendered']) and item['derived_sha256'] == entry['rendered_sha256'] for item in derivation['derivatives'])
    runtime = load(ROOT.parent/'S2-09/qa/native-runtime-provenance.json')
    assert digest(Path(runtime['mapped_source'])) == runtime['mapped_sha256'] and digest(Path(runtime['verification_runtime'])) == runtime['runtime_sha256']
    with zipfile.ZipFile(runtime['mapped_source']) as original, zipfile.ZipFile(runtime['verification_runtime']) as local:
        classes = [name for name in original.namelist() if name.endswith('.class')]
        assert set(classes) == {name for name in local.namelist() if name.endswith('.class')} and all(original.read(name) == local.read(name) for name in classes)
    save(ROOT/'qa/native-runtime-provenance.json',runtime)
    reviewer = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685')
    previous = load(ROOT.parent/'S2-09/registration.json')['previous_accepted_sources']
    assert len(previous) == 8
    prior = ROOT.parent/'S2-09/incoming/G2B-09/room.nbt'
    expected = 'd83bb005a47dc3e49b58d4901417654badc91cd827b5664ac05660c55968206a'
    assert digest(prior) == expected
    details = load(reviewer/'S2-09-independent/independent-details.json')
    assert details['status'] == 'PASS' and details['block_entities'] == 3
    previous.append({'image':9,'source':str(prior),'sha256':expected,'acceptance_basis':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION in user delegation; read-only evidence '+str(reviewer/'S2-09-independent'),'source_unchanged':True})
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
    qa.update({'status':'SELF_CHECK_PASS_PENDING_INDEPENDENT_ACCEPTANCE','native_bound_tags_survival':'PASS_42_SELECTED_POINTS','player_volume_route_targets':42,'player_volume_routes':'PASS_OFFLINE_SHAPES_NOT_GAME_PHYSICS','native_entities':'PASS_5_CHESTS_10_STACKS_2_EMPTY_BARRELS','chest_lid_static':'PASS_NATIVE_ISCHESTBLOCKEDAT_FALSE_5_AND_TWO_AIR_ABOVE','internal_tunnel_apertures':'ALL10_PASS_FULL_WIDTH_HEIGHT5','tile_reconstruction':'PASS_78390_STATES_AND7_ENTITIES_EXACT','validator_provenance':str(reviewer),'independent_acceptance':'NOT_GRANTED','source_and_preview_hashes_verified':True})
    save(TARGET/'qa.json',qa)
    save(ROOT/'qa/chest-audit.json',{'sha256':sha,'chests':room['chest_checks'],'barrels':{'positions':[[55,3,60],[61,3,60]],'native_loaded':2,'slots_each':27,'empty':True,'body_access_targets':[[56,3,60],[60,3,60]]},'expected_chests':metadata['chests'],'scope':room['chest_scope'],'dynamic_open_and_menus':'NOT_RUN','loot_balance':'NOT_ASSESSED_FIXED_EXAMPLES_NO_LOOTTABLE'})
    save(ROOT/'qa/dynamic-boundaries.json',{'sha256':sha,'roof':metadata['roof'],'footprint':metadata['footprint'],'explicit_air_placement':'All65x18x67 cells written, including air in gaps and margins. Four nonoverlap tiles preserve every air cell. Before integration audit entire envelope, not only occupied floors.','fluids':{'water_count':40,'all_level_zero_y3':True,'bottom_cut_sandstone':True,'horizontal_air_edges':0,'central_pillar_dry':True,'no_falling_column':True,'fluid_ticks_source_generation_world_updates':'NOT_RUN','claim':'Initial static pool containment only; not certified stable fluid placement.'},'hazard_blocks':0,'containers':7,'chest_static':'Native entities and10 stacks loaded; isChestBlockedAt false at5 chests in immutable no-entity context; two air above; three air high front targets all body reachable.','container_dynamic':'No lid animation, cat test, menu use, item extraction, networking or world interactions. Barrels checked as empty27-slot native entities with body access.','sand_support':qa['supported_sand'],'native_fragile_points':42,'light_support':qa['lantern_connections'],'solid_graph':{'solid_count_excludes_water':qa['six_face_solids_excludes_water'],'reached_y0':qa['six_face_connected_to_y0'],'disconnected':qa['detached_solids']},'interfaces':metadata['connections'],'route_scope':'External entrance+8 region centers+10 link centers+23 detailed step/pool/container/gallery targets. 0.6x1.8 half-grid native collision, max step0.5, no swimming or jumps. Offline shapes, not game physics. Every10 tunnel clear aperture also statically audited.','brightness':'Static canSurvive at15 with real vanilla tags; no real light propagation','world_neighbor_random_ticks_physics_interaction':'NOT_RUN','weather_snow_light_spawning':'NOT_RUN_OPEN_HALLS_AND_COURT','rotation_mirror':'NOT_TESTED','tile_placement_scheduling_and_updates':'NOT_RUN','deployment':'NOT_RUN_NOT_AUTHORIZED'})
    save(ROOT/'registration.json',{'group':2,'image':10,'id':'G2B-10','name':metadata['name'],'status':'READY_FOR_INDEPENDENT_REVIEW_NOT_ACCEPTED','candidate':str(source),'sha256':sha,'size':metadata['size'],'reference':metadata['reference'],'reference_sha256':metadata['reference_sha256'],'external_connections':metadata['connections'],'region_count':8,'internal_link_count':10,'body_targets':42,'layout':str(ROOT/'layout.json'),'hall_feet_y':3,'dais_feet_y':4,'roof':metadata['roof'],'metadata':str(TARGET/'room.json'),'qa':str(TARGET/'qa.json'),'chest_audit':str(ROOT/'qa/chest-audit.json'),'assembly':str(ROOT/'assembly.json'),'tile_reconstruction':str(ROOT/'qa/tile-reconstruction.json'),'derivation':str(ROOT/'derivation.json'),'screenshots':str(ROOT/'qa/lodestone-screenshots.json'),'full_source_overview':str(TARGET/'lodestone-exterior.png'),'full_source_detail':str(TARGET/'lodestone-detail.png'),'dynamic_boundaries':str(ROOT/'qa/dynamic-boundaries.json'),'native_runtime_provenance':str(ROOT/'qa/native-runtime-provenance.json'),'duplicate_selfcheck':str(ROOT/'qa/duplicate-selfcheck.json'),'previous_accepted_sources':previous,'active_reviewer_thread':'01a1a685-045b-76d2-aa2b-2a3f24520603','reviewer_messaged':False,'next_image':'S2-11 LOCKED_NOT_STARTED','world_ticks_interaction_gameplay_deployment':'NOT_RUN'})
    files = [{'path':str(path.relative_to(ROOT)),'sha256':digest(path)} for path in sorted(ROOT.rglob('*')) if path.is_file() and path.name != 'FILE_MANIFEST.json' and '__pycache__' not in path.parts]
    save(ROOT/'FILE_MANIFEST.json',{'source_sha256':sha,'files':files})
    assert all(digest(ROOT/item['path']) == item['sha256'] for item in files)
    print(f'FINALIZED {sha};{len(files)} hashes;8 regions;10 links;42 body/42survival;4 exact tiles;PENDING independent acceptance')


if __name__ == '__main__':
    main()
