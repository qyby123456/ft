import importlib.util
import json
from pathlib import Path
import shutil

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
    assert sha == 'e9297e226f5142de25a99b3907ec0b186bf150aa676b30584ee0e6c40278f97e'
    metadata = load(TARGET/'room.json')
    assert metadata['sha256'] == sha
    native = load(ROOT/'qa/native-review.json')
    survival = load(ROOT/'qa/independent-native-frozen.json')[0]
    routes = load(ROOT/'qa/independent-routes.json')[0]
    assert native['status'] == 'PASS' and native['rooms'][0]['sha256'] == sha
    assert native['rooms'][0]['audited_targets'] == 16 and native['rooms'][0]['loaded_block_entities'] == 0
    assert survival['sha256'] == routes['sha256'] == sha
    assert survival['registry_properties'] == survival['template_roundtrip_block_count'] == 'PASS'
    assert survival['vanilla_block_tags_bound'] and survival['survival_checked'] == 11
    assert not survival['survival_failures'] and not survival['survival_unsupported']
    assert routes['start_clear'] and len(routes['targets']) == 16
    assert all(target['reachable'] and target['standing_clear'] for target in routes['targets'])
    structure = Structure.read(source)
    derivation = load(ROOT/'derivation.json')
    assert derivation['source_sha256'] == sha
    for entry in derivation['derivatives']:
        path = Path(entry['derived'])
        assert digest(path) == entry['derived_sha256'] and entry['source_sha256'] == sha
        preview = Structure.read(path)
        assert preview.size == structure.size
        changed = 0
        for point,state in structure.blocks.items():
            if preview.get(point) == state:
                continue
            assert preview.get(point) == AIR and cut(entry['direction'],point)
            changed += 1
        assert changed == entry['removed_nonair']
    screenshots = load(ROOT/'qa/lodestone-screenshots.json')
    assert len(screenshots) == 6
    assert {entry['direction'] for entry in screenshots} == {'exterior','corner','north','south','west','east'}
    for entry in screenshots:
        assert not entry['errors'] and entry['source_sha256'] == sha
        assert digest(Path(entry['screenshot'])) == entry['screenshot_sha256']
        assert digest(Path(entry['rendered'])) == entry['rendered_sha256']
        if entry['direction'] in {'exterior','corner'}:
            assert Path(entry['rendered']) == source and not entry['preview_not_deployable']
    shutil.copyfile(TARGET/'lodestone-corner.png',TARGET/'preview.png')
    reviewer = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685')
    specification = importlib.util.spec_from_file_location('readonly_audit',reviewer/'audit.py')
    audit = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(audit)
    sources = [ROOT.parent/'S2-01/revision-02/incoming/G2B-01-R2/room.nbt',ROOT.parent/'S2-02/revision-02/incoming/G2B-02/room.nbt',ROOT.parent/'S2-03/incoming/G2B-03/room.nbt',ROOT.parent/'S2-04/incoming/G2B-04/room.nbt',ROOT.parent/'S2-05/incoming/G2B-05/room.nbt']
    accepted = []
    for image,path in enumerate(sources,start=1):
        if image < 5:
            acceptance_path = reviewer/f'S2-0{image}-ACCEPTANCE.json'
            acceptance = load(acceptance_path)
            expected = acceptance.get('nbt_sha256',acceptance.get('sha256'))
            basis = str(acceptance_path)
        else:
            expected = '4131474bbc0f61feaf6c4f245c480cca391d802bb4e1f0965b69d3fb44aa730a'
            basis = 'Sequential acceptance supplied in current user delegation from reviewer; directory S2-05-independent has check evidence, not a standalone ACCEPTANCE.json'
            assert load(reviewer/'S2-05-independent/file-audit.json')['groups'][0]['sha256'] == expected
        assert digest(path) == expected, (path,expected)
        accepted.append({'image':image,'source':str(path),'sha256':expected,'acceptance_basis':basis,'source_unchanged':True})
    peers = [(f'S2-0{image}-accepted',path) for image,path in enumerate(sources,start=1)] + [(f'S{group}-peer',audit.BASE/relative/candidate) for group,relative,candidate,unused_preview in audit.GROUPS if group != 2]
    own = mask(audit,source)[0]
    comparisons = []
    for label,peer in peers:
        peer_masks = mask(audit,peer)
        score = max(len(own & other)/len(own | other) for other in peer_masks)
        comparisons.append({'peer':label,'path':str(peer),'sha256':digest(peer),'normalized_interior_jaccard_best_y_rotation':score})
    save(ROOT/'qa/duplicate-selfcheck.json',{'source_sha256':sha,'scope':'Maker screening only. Material-independent interior normalized occupancy, excludes outer two blocks and fluids; four Y rotations. Not semantic uniqueness certification or independent acceptance.','comparisons':comparisons})
    qa_path = TARGET/'qa.json'
    qa = load(qa_path)
    qa.update({'status':'SELF_CHECK_PASS_PENDING_INDEPENDENT_ACCEPTANCE','native_bound_tags_survival':'PASS_11_STATIC_POINTS_3_LANTERNS_4_POTS_4_BUSHES','player_volume_route_targets':16,'player_volume_routes':'PASS_OFFLINE_SHAPES_NOT_GAME_PHYSICS','validator_provenance':str(reviewer),'independent_acceptance':'NOT_GRANTED','source_and_preview_hashes_verified':True})
    save(qa_path,qa)
    save(ROOT/'qa/dynamic-boundaries.json',{'sha256':sha,'roof':metadata['roof'],'footprint':metadata['footprint'],'explicit_air_placement':'Entire 25x13x25 envelope may overwrite blocks, including northeast missing quadrant and margin. Full-volume occupancy review required before any integration.','fluid_blocks':0,'hazard_blocks':0,'containers':0,'falling_blocks':qa['supported_sand'],'fragile_points':'3 hanging lanterns,4 pots,4 dead bushes; native tagged static survival PASS','lighting_connections':qa['lantern_connections'],'solid_graph':{'solid_count':qa['six_face_solids'],'reached_y0':qa['six_face_connected_to_y0'],'disconnected':qa['detached_solids']},'interfaces':metadata['connections'],'static_clearance':'Two perpendicular 5x4 supported boundary apertures. Bent strip x6..8,y2..5,z0..18 UNION x6..24,y2..5,z16..18 is air with support. South straight continuation blocked. Offline body routes 2 port centers plus14 targets, not all trajectories.','tags_brightness':'Native canSurvive with real tags,fixed brightness15; no real light propagation','world_neighbor_updates_fluid_random_ticks_physics_interaction':'NOT_RUN','weather_snow_light_spawning_overhead_terrain':'NOT_RUN_REQUIRES_INTEGRATION_REVIEW_SKYLIGHT','rotation_and_mirroring':'NOT_TESTED','deployment':'NOT_RUN_NOT_AUTHORIZED'})
    save(ROOT/'registration.json',{'group':2,'image':6,'id':'G2B-06','name':metadata['name'],'status':'READY_FOR_INDEPENDENT_REVIEW_NOT_ACCEPTED','candidate':str(source),'sha256':sha,'size':metadata['size'],'reference':metadata['reference'],'reference_sha256':metadata['reference_sha256'],'connections':metadata['connections'],'hall_feet_y':2,'footprint':metadata['footprint'],'roof':metadata['roof'],'metadata':str(TARGET/'room.json'),'qa':str(qa_path),'assembly':str(ROOT/'assembly.json'),'derivation':str(ROOT/'derivation.json'),'screenshots':str(ROOT/'qa/lodestone-screenshots.json'),'corner_full_source_screenshot':str(TARGET/'lodestone-corner.png'),'dynamic_boundaries':str(ROOT/'qa/dynamic-boundaries.json'),'duplicate_selfcheck':str(ROOT/'qa/duplicate-selfcheck.json'),'previous_accepted_sources':accepted,'active_reviewer_thread':'01a1a685-045b-76d2-aa2b-2a3f24520603','reviewer_messaged':False,'next_image':'S2-07 LOCKED_NOT_STARTED','world_ticks_interaction_gameplay_deployment':'NOT_RUN'})
    files = [{'path':str(path.relative_to(ROOT)),'sha256':digest(path)} for path in sorted(ROOT.rglob('*')) if path.is_file() and path.name != 'FILE_MANIFEST.json' and '__pycache__' not in path.parts]
    save(ROOT/'FILE_MANIFEST.json',{'source_sha256':sha,'files':files})
    assert all(digest(ROOT/item['path']) == item['sha256'] for item in files)
    print(f'FINALIZED {sha}; {len(files)} file hashes;16 routes;11 survival checks;independent acceptance PENDING')
    print('Duplicate screening:',[(item['peer'],round(item['normalized_interior_jaccard_best_y_rotation'],4)) for item in comparisons])


if __name__ == '__main__':
    main()
