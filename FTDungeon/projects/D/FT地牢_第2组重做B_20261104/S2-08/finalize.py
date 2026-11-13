import importlib.util
import json
from pathlib import Path

from prepare import ROOT, TARGET, AIR, Structure, cut, digest, save


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def mask(audit, path):
    template = audit.decode(path)
    size = template['size']
    points = {tuple(entry['pos']) for entry in template['blocks'] if template['palette'][entry['state']]['Name'] not in audit.AIR}
    interior = {point for point in points if all(1 < point[axis] < size[axis]-2 for axis in range(3))}
    return [audit.occupancy(interior,size,rotation) for rotation in range(4)]


def main():
    source = TARGET/'room.nbt'
    sha = digest(source)
    assert sha == 'eddfe98c172b340520cf869aaec58a3745c6828e1a0656dd897318369ec90cb9'
    metadata = load(TARGET/'room.json')
    assert metadata['sha256'] == sha and digest(Path(metadata['reference'])) == metadata['reference_sha256']
    native = load(ROOT/'qa/native-review.json')
    survival = load(ROOT/'qa/independent-native-frozen.json')[0]
    routes = load(ROOT/'qa/independent-routes.json')[0]
    assert native['status'] == 'PASS' and native['rooms'][0]['sha256'] == sha
    assert native['rooms'][0]['audited_targets'] == 22 and native['rooms'][0]['loaded_block_entities'] == 0
    assert survival['sha256'] == routes['sha256'] == sha
    assert survival['registry_properties'] == survival['template_roundtrip_block_count'] == 'PASS'
    assert survival['vanilla_block_tags_bound'] and survival['survival_checked'] == 35
    assert not survival['survival_failures'] and not survival['survival_unsupported']
    assert routes['start_clear'] and len(routes['targets']) == 22
    assert all(target['reachable'] and target['standing_clear'] for target in routes['targets'])
    structure = Structure.read(source)
    assert dict(__import__('collections').Counter(state.name for state in structure.blocks.values() if state != AIR)) == load(TARGET/'materials.json')
    derivation = load(ROOT/'derivation.json')
    assert derivation['source_sha256'] == sha
    for entry in derivation['derivatives']:
        path = Path(entry['derived'])
        assert digest(path) == entry['derived_sha256'] and entry['source_sha256'] == sha
        preview = Structure.read(path)
        assert preview.size == structure.size
        changed = []
        for point,state in structure.blocks.items():
            if preview.get(point) == state:
                continue
            assert preview.get(point) == AIR and cut(entry['direction'],point)
            changed.append(point)
        assert len(changed) == entry['removed_nonair']
        assert set(changed) == {tuple(point) for point in entry['removed_positions']}
    screenshots = load(ROOT/'qa/lodestone-screenshots.json')
    assert len(screenshots) == 7
    assert {entry['direction'] for entry in screenshots} == {'exterior','reverse','detail','north','south','west','east'}
    for entry in screenshots:
        assert not entry['errors'] and entry['source_sha256'] == sha
        assert digest(Path(entry['screenshot'])) == entry['screenshot_sha256']
        assert digest(Path(entry['rendered'])) == entry['rendered_sha256']
        if entry['direction'] in {'exterior','reverse','detail'}:
            assert Path(entry['rendered']) == source and not entry['preview_not_deployable']
        else:
            assert entry['preview_not_deployable']
            assert any(Path(item['derived']) == Path(entry['rendered']) and item['derived_sha256'] == entry['rendered_sha256'] for item in derivation['derivatives'])
    reviewer = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685')
    specification = importlib.util.spec_from_file_location('reviewer_audit',reviewer/'audit.py')
    audit = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(audit)
    sources = [ROOT.parent/'S2-01/revision-02/incoming/G2B-01-R2/room.nbt',ROOT.parent/'S2-02/revision-02/incoming/G2B-02/room.nbt'] + [ROOT.parent/f'S2-0{image}/incoming/G2B-0{image}/room.nbt' for image in range(3,8)]
    hashes = ['336edadd450ee6bd1bf52abe737a483110823b1cf8625e3687d866a26a1aeb4a','8640ec26e1f4e7fc9fc6a6696845db48cff215692f817ad7193b13a6989a979b','e15d04c1769ebc03bc28fb516f14fe8eb5b699cb3bcc1b762db25370fa68beca','e51ce958dac41a27803aa5443b09ac8b1c19d1e0a8f2d6ceb493130e5b97d3aa','4131474bbc0f61feaf6c4f245c480cca391d802bb4e1f0965b69d3fb44aa730a','e9297e226f5142de25a99b3907ec0b186bf150aa676b30584ee0e6c40278f97e','f8b42d13917e5bfd1a2e8e25b82d9146d89f96d1a5ca8421b34234f0c913d6cd']
    accepted = []
    for image,(path,expected) in enumerate(zip(sources,hashes),start=1):
        if image < 5:
            acceptance_path = reviewer/f'S2-0{image}-ACCEPTANCE.json'
            acceptance = load(acceptance_path)
            assert acceptance.get('nbt_sha256',acceptance.get('sha256')) == expected
            basis = str(acceptance_path)
        else:
            evidence = reviewer/f'S2-0{image}-independent/file-audit.json'
            assert load(evidence)['groups'][0]['sha256'] == expected
            basis = 'Sequential acceptance supplied in user delegation; read-only file evidence '+str(evidence)+'; no standalone acceptance file invented'
        assert digest(path) == expected
        accepted.append({'image':image,'source':str(path),'sha256':expected,'acceptance_basis':basis,'source_unchanged':True})
    peers = [(f'S2-0{image}-accepted',path) for image,path in enumerate(sources,start=1)] + [(f'S{group}-peer',audit.BASE/relative/candidate) for group,relative,candidate,unused_preview in audit.GROUPS if group != 2]
    own = mask(audit,source)[0]
    comparisons = []
    for label,peer in peers:
        score = max(len(own & other)/len(own | other) for other in mask(audit,peer))
        comparisons.append({'peer':label,'path':str(peer),'sha256':digest(peer),'normalized_interior_jaccard_best_y_rotation':score})
    save(ROOT/'qa/duplicate-selfcheck.json',{'source_sha256':sha,'scope':'Maker screening only. Material-independent interior normalized occupancy, excludes outer two blocks and fluids; four Y rotations. Not semantic uniqueness certification or independent acceptance.','comparisons':comparisons})
    qa = load(TARGET/'qa.json')
    qa.update({'status':'SELF_CHECK_PASS_PENDING_INDEPENDENT_ACCEPTANCE','native_bound_tags_survival':'PASS_35_STATIC_POINTS_3_LANTERNS_4_POTS_4_BUSHES_4_PADS_20_VINES','player_volume_route_targets':22,'player_volume_routes':'PASS_OFFLINE_SHAPES_NOT_GAME_PHYSICS','validator_provenance':str(reviewer),'independent_acceptance':'NOT_GRANTED','source_and_preview_hashes_verified':True})
    save(TARGET/'qa.json',qa)
    save(ROOT/'qa/dynamic-boundaries.json',{'sha256':sha,'roof':metadata['roof'],'footprint':metadata['footprint'],'explicit_air_placement':'Entire 33x24x35 envelope may overwrite blocks, including margins and open sky. Full-volume occupancy review required before integration.','water_states':qa['water_states'],'fluid_static_checks':'All water initial projections in inner basin x13..21,z13..21. At Y3 water has solid cut-sandstone floor and no horizontal air leak. Elevated source and falling column exist as NBT states.','fluid_dynamic_limit':'NOT_RUN: high spout spreads sideways before dropping; spread timing, generated sources, basin equilibrium, lily pad persistence under flow, flooding after neighboring changes, server placement fluid scheduling are untested. NOT claimed fluid-stable.','leaf_limit':{'persistent_count':qa['persistent_leaves'],'static_root_connection':'all non-fluid non-pad blocks connect to Y0 via six faces','world_random_neighbor_fire_light_ticks':'NOT_RUN'},'containers':0,'falling_sand_support':qa['supported_sand'],'native_fragile_points':35,'lighting_connections':qa['lantern_connections'],'solid_graph':{'solid_count_excludes_fluids_and_lily_pads':qa['six_face_solids_excluding_fluids_and_pads'],'reached_y0':qa['six_face_connected_to_y0'],'disconnected':qa['detached_solids'],'pads':'four pads supported directly by source water; deliberately excluded from solid-only graph'},'interfaces':metadata['connections'],'static_clearance':'North 7x4 feetY1 with two stairs to courtY3; other three 5x4 feetY3. Upper gallery feetY11 via 8 stairs. Four port centers and18 authored targets pass offline 0.6x1.8 body routes, no jumps, max step0.5; not all trajectories or fall safety.','tags_brightness':'Native canSurvive uses real vanilla tags and fixed brightness15; no actual light propagation. Leaves not in canSurvive selection.','world_neighbor_updates_fluid_random_ticks_physics_interaction':'NOT_RUN','weather_snow_light_spawning_overhead_terrain':'NOT_RUN_OPEN_COURTYARD','rotation_and_mirroring':'NOT_TESTED','deployment':'NOT_RUN_NOT_AUTHORIZED'})
    save(ROOT/'registration.json',{'group':2,'image':8,'id':'G2B-08','name':metadata['name'],'status':'READY_FOR_INDEPENDENT_REVIEW_NOT_ACCEPTED','candidate':str(source),'sha256':sha,'size':metadata['size'],'reference':metadata['reference'],'reference_sha256':metadata['reference_sha256'],'connections':metadata['connections'],'hall_feet_y':3,'upper_gallery_feet_y':11,'footprint':metadata['footprint'],'roof':metadata['roof'],'metadata':str(TARGET/'room.json'),'qa':str(TARGET/'qa.json'),'assembly':str(ROOT/'assembly.json'),'derivation':str(ROOT/'derivation.json'),'screenshots':str(ROOT/'qa/lodestone-screenshots.json'),'full_source_detail':str(TARGET/'lodestone-detail.png'),'dynamic_boundaries':str(ROOT/'qa/dynamic-boundaries.json'),'duplicate_selfcheck':str(ROOT/'qa/duplicate-selfcheck.json'),'previous_accepted_sources':accepted,'active_reviewer_thread':'01a1a685-045b-76d2-aa2b-2a3f24520603','reviewer_messaged':False,'next_image':'S2-09 LOCKED_NOT_STARTED','world_ticks_interaction_gameplay_deployment':'NOT_RUN'})
    files = [{'path':str(path.relative_to(ROOT)),'sha256':digest(path)} for path in sorted(ROOT.rglob('*')) if path.is_file() and path.name != 'FILE_MANIFEST.json' and '__pycache__' not in path.parts]
    save(ROOT/'FILE_MANIFEST.json',{'source_sha256':sha,'files':files})
    assert all(digest(ROOT/item['path']) == item['sha256'] for item in files)
    print(f'FINALIZED {sha}; {len(files)} file hashes;22 routes;35 survival checks;independent acceptance PENDING')
    print('Duplicate screening:',[(item['peer'],round(item['normalized_interior_jaccard_best_y_rotation'],4)) for item in comparisons])


if __name__ == '__main__':
    main()
