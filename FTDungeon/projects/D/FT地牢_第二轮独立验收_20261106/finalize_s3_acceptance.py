import hashlib
import json
from datetime import datetime
from pathlib import Path

from audit_s8_submission import load

review=Path(__file__).parent
workspace=Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603')
root=workspace/'preserved-review-snapshots/S3-access-revision-20261107-1103'
prior=review/'S3-submission-20261107-072954-274564'
supplement=review/'S3-supplement-20261107-094835-560734'
for folder,name,count in ((root,'submission-snapshot.json',725),(prior,'submission-snapshot.json',213),(supplement,'supplement-snapshot.json',92)):
    records=load(folder/name)['files']
    assert len(records)==count
    for item in records:
        assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest()==item['sha256']
digest='f99d5a2a04a510b8bc46bbe7a0e8b75bc4cea94ee1886aa37f14fdf3eaede1d1'
assert hashlib.sha256((root/'rooms/s3-merged/s3-merged.nbt').read_bytes()).hexdigest()==digest
payload=load(root/'independent-payload-inspection.json')
assert payload['source_sha256']==digest and payload['parts']==25 and payload['full_volume']==1572165
assert payload['barrier_cells']==117690 and payload['all_original_nonair_and_be_retained'] and payload['original_targets_retained']
assert len(payload['merged_changes'])==3 and len(payload['rooms'])==10
assert all(not item['added_air_cells'] for item in payload['rooms'] if item['id']!='s3-16')
reproduction=load(root/'independent-reproduction.json')
assert reproduction['room16_generated_from_recipe'] and reproduction['merged_binary_equal'] and reproduction['parts_binary_equal']==25
assert reproduction['other_nine_rooms_from_prior_parent_recipe_outputs_verified_exact']
assert reproduction['current16_and_merged_nbt_not_used_as_generation_input']
assert len(load(prior/'independent-qa/reproduction.json')['outputs'])==11
support=load(prior/'independent-qa/independent-all-support.json')
assert len(support)==11 and sum(item['nonair_checked'] for item in support)==288108
assert all(not item['failures'] and not item['unsupported'] for item in support)
current_support=load(root/'independent-qa/independent-all-support.json')
assert sum(item['nonair_checked'] for item in current_support)==213822
assert all(not item['failures'] and not item['unsupported'] for item in current_support)
for folder in (prior,root):
    native=load(folder/'independent-qa/independent-native-frozen.json')
    assert all(item['registry_properties']==item['template_roundtrip_block_count']=='PASS' for item in native)
    routes=load(folder/'independent-qa/independent-routes.json')
    assert all(item['start_clear'] for item in routes)
    assert all(target['reachable'] and target['standing_clear'] for item in routes for target in item['targets'])
inherited_runtime=load(supplement/'independent-runtime-inspection.json')
assert inherited_runtime['normal_ladder_climb_and_descent'] and inherited_runtime['ladder_ticks']==360
assert len(inherited_runtime['world_captures'])==11 and inherited_runtime['lock_checks']==18
assert inherited_runtime['temporary_lever_checks']==12
runtime=load(root/'independent-runtime-inspection.json')
assert runtime['source_sha256']==digest and runtime['natural_ticks']==27811
assert runtime['functional_blocks']==44 and runtime['ray_use_attempts']==50 and runtime['original_targets']==41
assert runtime['returned_to_start'] and runtime['hanging_barrel']['attempts'][0]['menu_opened']
assert load(root/'independent-run.json')['exit_code']==0
installed=load(root/'independent-installed-inspection.json')
assert installed['full_cells']==1572165 and installed['zero_type_changes'] and installed['source_be_fields_preserved']
assert installed['empty_inventories'] and installed['producer_capture_binary_equal']
world=Path(runtime['runtime'])
deployment=load(world/'deployment-report.json')
assert deployment['seeded_solid_cells']==1572165 and deployment['air_clear_errors']==0
assert deployment['block_type_errors']==deployment['declared_be_errors']==0
assert all(deployment['functions_entry_exit'].values())
for source in (root/'deployment/datapack').rglob('*'):
    if source.is_file():
        assert source.read_bytes()==(world/'server/world/datapacks/fts3'/source.relative_to(root/'deployment/datapack')).read_bytes()
result={'group':'S3','status':'ACCEPTED_FOR_SECOND_ROUND_ROOM_SCOPE','accepted':True,'completed_at':datetime.now().isoformat(),'source_sha256':digest,'room16_sha256':'b51428a00afd2bd3bdd5b753d7710d7e5716b4175c1b0051d7ce125c0defb0de','frozen_counts':{'current':725,'prior_source':213,'prior_world_ladder':92},'rooms':10,'parts':25,'full_volume':1572165,'barrier_cells':117690,'source_targets':40,'merged_targets':41,'current_support_checks':213822,'prior_support_checks':288108,'natural_travel_updates':27811,'functional_blocks':44,'actual_ray_use_attempts':50,'hanging_barrel_reach':runtime['max_reach'],'returned_to_entry':True,'requirements':{'visual':'Prior actual ten-room comparison inspection and19/20depth views retained; only3air-to-slab access additions, currentbucket close-up inspected','source_and_parts':'All ten original nonair/BE/goals retained;25parts full-volume exact reconstruction','reproduction':'Prior ten-room recipe rebuild inherited for unchanged9; current16,merged and25parts independently rebuilt exact','native_routes_support':'Prior all11 plus current16/merged native/support/routes pass','runtime_safety':'Prior parent11world1200tick and360tickladder unchanged9; current full install snapshot classified and27811naturaltravel','functional_interactions':'44 naturally reached targets,50 actual eye rays/use; original3locks rejectwrong/empty andopencorrectkey','external_install_access':'Actual packaged clear/install full1572165 plus named-player enter/end-restricted exit;117690barrier preserved','scope_documented':'Frozen README,QA limitations and reproduction flags retained'},'limits':['Unrotated finite isolated Forge tests; no production-pack deployment or real TCP client certification','Prior1200tick evidence binds old hash; inheritance uses exactonly3newslabs and unchanged source payload, current install/travel independently tested','Optional six-lever overlay inherited only for old hash; current source iron doors intentionally constant-open, no claim new overlay retest','Well and water channels are frozen blue ice, bucket static; no water collection mechanic','Combat/spawners/elites/boss/rewards outside current room scope'],'production_deployed':False}
(root/'ACCEPTANCE.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(result,ensure_ascii=False,indent=2))
