import importlib.util
import json
from pathlib import Path
import shutil

from prepare import ROOT,TARGET,AIR,Structure,cut,digest,save


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def mask(audit,path):
    template=audit.decode(path)
    size=template['size']
    points={tuple(entry['pos']) for entry in template['blocks'] if template['palette'][entry['state']]['Name'] not in audit.AIR}
    interior={point for point in points if all(1<point[axis]<size[axis]-2 for axis in range(3))}
    return [audit.occupancy(interior,size,rotation) for rotation in range(4)]


def main():
    source=TARGET/'room.nbt'
    sha=digest(source)
    assert sha=='f8b42d13917e5bfd1a2e8e25b82d9146d89f96d1a5ca8421b34234f0c913d6cd'
    metadata=load(TARGET/'room.json')
    assert metadata['sha256']==sha
    native=load(ROOT/'qa/native-review.json')
    assert native['status']=='PASS' and native['rooms'][0]['sha256']==sha
    assert native['rooms'][0]['audited_targets']==0 and native['rooms'][0]['loaded_block_entities']==7
    assert native['rooms'][0]['gate_state']=='CLOSED_EXPECT_ROUTE_BLOCKED'
    survival=load(ROOT/'qa/independent-native-frozen.json')[0]
    assert survival['sha256']==sha and survival['survival_checked']==23 and survival['vanilla_block_tags_bound']
    assert survival['registry_properties']==survival['template_roundtrip_block_count']=='PASS'
    assert not survival['survival_failures'] and not survival['survival_unsupported']
    gate=load(ROOT/'gate-state-derivation.json')
    assert gate['source_sha256']==sha
    opened_path=Path(gate['derived'])
    opened_sha=digest(opened_path)
    assert opened_sha==gate['derived_sha256']=='97be7d4d6755a024436cbddd4f03a6a022f8e6256cae305b5abf514f2398e06d'
    structure=Structure.read(source)
    opened=Structure.read(opened_path)
    assert structure.size==opened.size and structure.entities==opened.entities and structure.block_entities==opened.block_entities
    expected={(east,up,4) for east in [12,13] for up in [3,4]}
    changed=set()
    for point,state in structure.blocks.items():
        other=opened.get(point)
        if other==state:
            continue
        assert point in expected and state.name==other.name=='minecraft:dark_oak_door'
        props=dict(state.properties)
        assert props['open']=='false'
        props['open']='true'
        assert props==dict(other.properties)
        changed.add(point)
    assert changed==expected
    open_native=load(ROOT/'analysis-only/qa/native-review.json')
    assert open_native['status']=='PASS' and open_native['rooms'][0]['sha256']==opened_sha
    assert open_native['rooms'][0]['audited_targets']==16
    closed_routes=load(ROOT/'qa/independent-routes.json')[0]
    open_routes=load(ROOT/'qa/open-gate/independent-routes.json')[0]
    assert closed_routes['sha256']==sha and open_routes['sha256']==opened_sha
    assert closed_routes['start_clear'] and open_routes['start_clear']
    assert len(closed_routes['targets'])==len(open_routes['targets'])==16
    assert all(target['reachable'] and target['standing_clear'] for target in open_routes['targets'])
    external={'north','step-one','step-two','gate-landing'}
    for target in closed_routes['targets']:
        assert target['standing_clear'] and target['reachable']==(target['label'] in external)
    derivation=load(ROOT/'derivation.json')
    assert derivation['source_sha256']==sha
    for entry in derivation['derivatives']:
        path=Path(entry['derived'])
        assert digest(path)==entry['derived_sha256'] and entry['source_sha256']==sha
        preview=Structure.read(path)
        assert preview.size==structure.size
        count=0
        for point,state in structure.blocks.items():
            if preview.get(point)==state:
                continue
            assert preview.get(point)==AIR and cut(entry['direction'],point)
            count+=1
        assert count==entry['removed_nonair']
    screenshots=load(ROOT/'qa/lodestone-screenshots.json')
    assert len(screenshots)==6 and {entry['direction'] for entry in screenshots}=={'exterior','entrance','north','south','west','east'}
    for entry in screenshots:
        assert not entry['errors'] and entry['source_sha256']==sha
        assert digest(Path(entry['screenshot']))==entry['screenshot_sha256']
        assert digest(Path(entry['rendered']))==entry['rendered_sha256']
        if entry['direction'] in {'exterior','entrance'}:
            assert Path(entry['rendered'])==source and not entry['preview_not_deployable']
    shutil.copyfile(TARGET/'lodestone-entrance.png',TARGET/'preview.png')
    reviewer=Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685')
    specification=importlib.util.spec_from_file_location('readonly_audit',reviewer/'audit.py')
    audit=importlib.util.module_from_spec(specification)
    specification.loader.exec_module(audit)
    sources=[ROOT.parent/'S2-01/revision-02/incoming/G2B-01-R2/room.nbt',ROOT.parent/'S2-02/revision-02/incoming/G2B-02/room.nbt']+[ROOT.parent/f'S2-0{image}/incoming/G2B-0{image}/room.nbt' for image in range(3,7)]
    accepted=[]
    for image,path in enumerate(sources,start=1):
        if image<5:
            acceptance_path=reviewer/f'S2-0{image}-ACCEPTANCE.json'
            acceptance=load(acceptance_path)
            expected_sha=acceptance.get('nbt_sha256',acceptance.get('sha256'))
            basis=str(acceptance_path)
        else:
            expected_sha={5:'4131474bbc0f61feaf6c4f245c480cca391d802bb4e1f0965b69d3fb44aa730a',6:'e9297e226f5142de25a99b3907ec0b186bf150aa676b30584ee0e6c40278f97e'}[image]
            basis=f'Sequential acceptance supplied in user delegation from reviewer; S2-0{image}-independent is check evidence,not standalone acceptance file'
            assert load(reviewer/f'S2-0{image}-independent/file-audit.json')['groups'][0]['sha256']==expected_sha
        assert digest(path)==expected_sha
        accepted.append({'image':image,'source':str(path),'sha256':expected_sha,'acceptance_basis':basis,'source_unchanged':True})
    peers=[(f'S2-0{image}-accepted',path) for image,path in enumerate(sources,start=1)]+[(f'S{group}-peer',audit.BASE/relative/candidate) for group,relative,candidate,unused_preview in audit.GROUPS if group!=2]
    own=mask(audit,source)[0]
    comparisons=[]
    for label,peer in peers:
        score=max(len(own&other)/len(own|other) for other in mask(audit,peer))
        comparisons.append({'peer':label,'path':str(peer),'sha256':digest(peer),'normalized_interior_jaccard_best_y_rotation':score})
    save(ROOT/'qa/duplicate-selfcheck.json',{'source_sha256':sha,'scope':'Maker normalized material-independent interior occupancy screening, excludes outer two blocks and fluids,4 Y rotations; not semantic uniqueness or independent acceptance.','comparisons':comparisons})
    save(ROOT/'qa/gate-route-summary.json',{'closed_source_sha256':sha,'closed_expected_obstruction':'PASS_4_OUTSIDE_TARGETS_REACHABLE_12_INSIDE_TARGETS_BLOCKED','closed_native_target_scope':'Only exterior landing native-grid route,not through-route PASS','open_analysis_sha256':opened_sha,'exact_open_property_changes':4,'open_grid_targets':16,'open_body_targets':16,'open_body_routes':'PASS_OFFLINE_SHAPES','gate_hinge_support_half_pairs':'Static property/pair inspection and4 native canSurvive door half checks PASS','source_closed_and_open_geometry_otherwise_identical':True,'actual_interaction_neighbor_update_sync':'NOT_RUN','deployment':'NOT_RUN'})
    qa_path=TARGET/'qa.json'
    qa=load(qa_path)
    qa.update({'status':'SELF_CHECK_PASS_PENDING_INDEPENDENT_ACCEPTANCE','native_bound_tags_survival':'PASS_23_POINTS_4_DOOR_HALVES_8_VINES_3_LANTERNS_4_POTS_4_BUSHES','closed_gate_routes':'EXPECTED_BLOCKED_INSIDE','open_gate_analysis_body_targets':16,'open_gate_analysis_routes':'PASS_NOT_ACTUAL_INTERACTION','validator_provenance':str(reviewer),'local_survival_extension':'qa/IndependentNativeAudit.java copied read-only then extended to select door halves; reviewer original unchanged','independent_acceptance':'NOT_GRANTED','source_and_preview_hashes_verified':True})
    save(qa_path,qa)
    save(ROOT/'qa/dynamic-boundaries.json',{'sha256':sha,'roof':metadata['roof'],'closed_gate':metadata['gate'],'gate_route_summary':str(ROOT/'qa/gate-route-summary.json'),'lit_campfire':{'position':[19,4,15],'support':'3x3 sandstone pedestal atY3','hazard':'Real lit campfire; burn damage,smoke,extinguishing/interactions NOT_RUN. Avoid pedestal in primary routes.'},'fluid_blocks':0,'empty_barrels':6,'lantern_connections':qa['lantern_connections'],'backed_vines':qa['backed_vines'],'solid_graph':{'solid_count':qa['six_face_solids'],'reached_y0':qa['six_face_connected_to_y0'],'disconnected':qa['detached_solids']},'interfaces':metadata['connections'],'gate_aperture':'Two wide X12..13 atZ4,Y3..4,closed. Four-wide north exterior pad is NOT gate width. Open-state analysis separate.','tags_brightness':'Native canSurvive with real tags,fixed brightness15;not real light propagation','world_neighbor_updates_door_interaction_campfire_damage_physics':'NOT_RUN','weather_snow_light_spawning_overhead_terrain':'NOT_RUN_OPEN_COURTYARD','rotation_and_mirroring':'NOT_TESTED','full_envelope_placement':'Explicit-air27x17x29 envelope can clear terrain; full-volume integration review required','deployment':'NOT_RUN_NOT_AUTHORIZED'})
    save(ROOT/'registration.json',{'group':2,'image':7,'id':'G2B-07','name':metadata['name'],'status':'READY_FOR_INDEPENDENT_REVIEW_NOT_ACCEPTED','candidate':str(source),'sha256':sha,'size':metadata['size'],'reference':metadata['reference'],'reference_sha256':metadata['reference_sha256'],'connections':metadata['connections'],'hall_feet_y':3,'gate':metadata['gate'],'roof':metadata['roof'],'metadata':str(TARGET/'room.json'),'qa':str(qa_path),'assembly':str(ROOT/'assembly.json'),'derivation':str(ROOT/'derivation.json'),'gate_state_derivation':str(ROOT/'gate-state-derivation.json'),'gate_route_summary':str(ROOT/'qa/gate-route-summary.json'),'screenshots':str(ROOT/'qa/lodestone-screenshots.json'),'dynamic_boundaries':str(ROOT/'qa/dynamic-boundaries.json'),'duplicate_selfcheck':str(ROOT/'qa/duplicate-selfcheck.json'),'previous_accepted_sources':accepted,'active_reviewer_thread':'01a1a685-045b-76d2-aa2b-2a3f24520603','reviewer_messaged':False,'next_image':'S2-08 LOCKED_NOT_STARTED','world_ticks_interaction_gameplay_deployment':'NOT_RUN'})
    files=[{'path':str(path.relative_to(ROOT)),'sha256':digest(path)} for path in sorted(ROOT.rglob('*')) if path.is_file() and path.name!='FILE_MANIFEST.json' and '__pycache__' not in path.parts]
    save(ROOT/'FILE_MANIFEST.json',{'source_sha256':sha,'files':files})
    assert all(digest(ROOT/item['path'])==item['sha256'] for item in files)
    print(f'FINALIZED {sha};{len(files)} hashes;23 static survival points;closed gate expected block;open state16 body routes PASS;acceptance PENDING')
    print('Duplicate screening:',[(item['peer'],round(item['normalized_interior_jaccard_best_y_rotation'],4)) for item in comparisons])


if __name__=='__main__':
    main()
