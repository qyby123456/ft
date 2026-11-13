import hashlib
import json
from pathlib import Path

root = Path(__file__).resolve().parent
def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def save(path, value):
    path.write_text(json.dumps(value,ensure_ascii=False,indent=2),encoding='utf-8')
rooms = load(root/'candidate-index.json')
natural = load(root/'qa/world-revision2/natural-results.json')
support = load(root/'qa/all-support-final/independent-all-support.json')
focused=root/'qa/room17-storage-verified'
focused_natural=load(focused/'natural-results.json')
assert focused_natural['passed'] and load(focused/'exit.json')['exit_code']==0
for identifier in ['S5-17','merged']:natural[identifier]=focused_natural[identifier]
support=[result for result in support if result['group'] not in [500,517]]+load(root/'qa/room17-storage-fix/support/independent-all-support.json')
assert natural['passed']
assert all(not result['failures'] and not result['unsupported'] for result in support)
for room in rooms:
    candidate = Path(room['candidate'])
    assert digest(candidate)==room['sha256']
    assert room['self_check_passed'] and room['render_bound']
    assert natural[room['id']]['sha256']==room['sha256'] and natural[room['id']]['passed']
    assert any(result['sha256']==room['sha256'] for result in support)
merged = root/'merged/merged.nbt'
merged_sha256 = digest(merged)
interaction=load(focused/'combined-summary.json')
access=load(root/'qa/access-native-verified/access-results.json')
assert interaction['usable_subset_routes_passed'] and interaction['sha256']==merged_sha256
assert interaction['passed'] and interaction['usable_interactions']==37 and not interaction['interaction_failures']
assert access['passed'] and access['candidate_sha256']=='eb8ac4bccc0829e591cd03310cd6bee221321a6724e822c9368e8c303b729bb7'
assert load(root/'qa/access-native-verified/exit.json')['exit_code']==0
access_package=load(root/'access-package/package-verification.json')
assert access_package['passed'] and access_package['merged_hash_unchanged']
assert digest(root/'access-package/s5access.zip')==access_package['zip_sha256']
checks = {name:load(root/'merged'/name) for name in ['parts-reconstruction.json','source-integrity.json','qa/self-check.json','qa/render.json']}
assert checks['parts-reconstruction.json']['source_sha256']==merged_sha256
assert checks['source-integrity.json']['sha256']==merged_sha256
assert checks['qa/self-check.json']['sha256']==merged_sha256 and checks['qa/self-check.json']['self_check_passed']
assert checks['qa/render.json']['sha256']==merged_sha256
assert any(result['sha256']==merged_sha256 for result in support)
readiness = {'group':'S5','status':'READY_FOR_INDEPENDENT_REVIEW','independent_acceptance':False,'group_accepted':False,'reviewer_thread':'01a1a685-045b-76d2-aa2b-2a3f24520603','rooms':rooms,'merged':{'candidate':str(merged),'sha256':merged_sha256,'size':[217,29,94],'part_count':10,'part_dimensions_max':48,'parts_manifest':str(root/'merged/parts.json'),'block_records':591542,'block_entity_payloads':27,'source_integrity':str(root/'merged/source-integrity.json')},'self_checks':{'natural_server':str(root/'qa/world-delivery/natural-results.json'),'ticks':1200,'randomTickSpeed':3,'doors_tested':6,'all_nonair_canSurvive':str(root/'qa/all-support-final/independent-all-support.json'),'room_route_targets':sum(len(load(root/room['id']/'qa/self-check.json')['routes']['targets']) for room in rooms),'merged_route_targets':len(checks['qa/self-check.json']['routes']['targets']),'parts_reconstruction':str(root/'merged/parts-reconstruction.json')},'review_inputs':[str(root/'README.md'),str(root/'VISUAL-SELF-REVIEW.md'),str(root/'inheritance-source-manifest.json')],'unverified':['Independent final visual/technical acceptance','Merged world placement and natural ticks','Flooded external ocean placement and oxygen depletion','Survival player traversal, ladder climbing and container interaction','Secret lever is supported decoration; no redstone secret-door mechanism','Ocean scenery outside balcony is not included'],'previous_review':'D:\\ft工程文件\\FT地牢_第二轮独立验收_20261106\\S5-preliminary-20261107-025045-361444\\visual-review.md'}
readiness['self_checks']['natural_server']=str(focused/'natural-results.json')
readiness['self_checks']['natural_server_inherited_nine_unchanged']=str(root/'qa/world-revision2/natural-results.json')
readiness['self_checks']['all_nonair_canSurvive_changed']=str(root/'qa/room17-storage-fix/support/independent-all-support.json')
readiness['self_checks']['post_tick_payload_comparison']=str(focused/'full-state-payload-comparison.json')
readiness['self_checks']['post_tick_bidirectional_886_point_coverage']=str(focused/'post-tick-routes/bidirectional-coverage.json')
readiness['self_checks']['reproduction']=str(root/'qa/reproduction-storage-fix-report.json')
readiness['self_checks']['block_use_scope']='37/37 usable reachable dry supported stands; native eye/ray first-hit <=4.5, 13 lever toggles/restores and 24 menu/use checks. 74 standing targets reachable from entry and finish. No live survival-player movement certification.'
readiness['self_checks']['interaction_stands_distance_raycast']=str(focused/'combined-summary.json')
readiness['self_checks']['external_access_runtime']=str(root/'qa/access-native-verified/access-results.json')
readiness['external_access_package']=str(root/'access-package/s5access.zip')
readiness['external_access_zip_sha256']=access_package['zip_sha256']
readiness['external_access_instructions']=str(root/'access-package/README.md')
readiness['review_inputs'].append(str(root/'UPPER-BARRELS-FIX.md'))
readiness['known_failures']=interaction['interaction_failures']
readiness['interaction_all_passed']=interaction['passed']
readiness['status']='READY_FOR_INDEPENDENT_REVIEW'
readiness['external_access_runtime_scope']=access_package['scope']
readiness['interface_contract']=load(root/'merged/design.json')['interface_contract']
readiness['retired_interfaces']=load(root/'merged/design.json')['retired_interfaces']
readiness['unverified']=['Independent final visual/technical acceptance','Flooded external ocean placement and oxygen depletion','Connected client movement, ladder physics and survival gameplay','Secret lever has no redstone secret-door mechanism','External access pack has not been installed into a formal save']
readiness['packaged_environment']={'id':'S5-19','depth_blocks':13,'in_candidate_nbt':True,'preview':str(root/'S5-19/platform-sightline.png')}
readiness['previous_review']='D:/ft工程文件/FT地牢_第二轮独立验收_20261106/S5-submission-20261107-080041-052971/INTERACTION-ACCESS-REVIEW.md'
save(root/'ready-for-review.json',readiness)
save(root/'progress.json',{'group':'S5','current':'awaiting_independent_review','status':readiness['status'],'completed':[room['id'] for room in rooms]+['merged','parts-reconstruction','self-qa'],'merged':True,'independent_acceptance':False,'group_accepted':False,'merged_sha256':merged_sha256,'source_manifest':'inheritance-source-manifest.json','pending':['independent final review of targeted barrel fix'],'unverified':readiness['unverified']})
excluded={'world','build','.gradle','logs','libraries','assets','natives','__pycache__'}
manifest = [{'path':str(path.relative_to(root)),'sha256':digest(path),'bytes':path.stat().st_size} for path in root.rglob('*') if path.is_file() and path.name!='delivery-manifest.json' and not excluded.intersection(path.relative_to(root).parts)]
for reference in load(root/'references.json')['references']:
    for key in ['image_path','prompt_path']:
        path=Path(reference[key]);manifest.append({'path':str(path),'sha256':digest(path),'bytes':path.stat().st_size,'role':'read-only original reference'})
for path in [Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs'),Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs'),Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/package.json'),Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/npm-shrinkwrap.json'),Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/audit.py')]:
    manifest.append({'path':str(path),'sha256':digest(path),'bytes':path.stat().st_size,'role':'read-only external tool'})
save(root/'delivery-manifest.json',manifest)
print(json.dumps({'status':readiness['status'],'merged_sha256':merged_sha256,'rooms':len(rooms),'parts':10,'all_support_nonair':sum(result['nonair_checked'] for result in support),'route_targets':readiness['self_checks']['room_route_targets'],'merged_targets':readiness['self_checks']['merged_route_targets']},ensure_ascii=False))
