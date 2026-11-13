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
natural = load(root/'qa/world-delivery/natural-results.json')
support = load(root/'qa/all-support-final/independent-all-support.json')
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
checks = {name:load(root/'merged'/name) for name in ['parts-reconstruction.json','source-integrity.json','qa/self-check.json','qa/render.json']}
assert checks['parts-reconstruction.json']['source_sha256']==merged_sha256
assert checks['source-integrity.json']['sha256']==merged_sha256
assert checks['qa/self-check.json']['sha256']==merged_sha256 and checks['qa/self-check.json']['self_check_passed']
assert checks['qa/render.json']['sha256']==merged_sha256
assert any(result['sha256']==merged_sha256 for result in support)
readiness = {'group':'S5','status':'READY_FOR_INDEPENDENT_REVIEW','independent_acceptance':False,'group_accepted':False,'reviewer_thread':'01a1a685-045b-76d2-aa2b-2a3f24520603','rooms':rooms,'merged':{'candidate':str(merged),'sha256':merged_sha256,'size':[217,29,94],'part_count':10,'part_dimensions_max':48,'parts_manifest':str(root/'merged/parts.json'),'block_records':591542,'block_entity_payloads':27,'source_integrity':str(root/'merged/source-integrity.json')},'self_checks':{'natural_server':str(root/'qa/world-delivery/natural-results.json'),'ticks':1200,'randomTickSpeed':3,'doors_tested':6,'all_nonair_canSurvive':str(root/'qa/all-support-final/independent-all-support.json'),'room_route_targets':sum(len(load(root/room['id']/'qa/self-check.json')['routes']['targets']) for room in rooms),'merged_route_targets':len(checks['qa/self-check.json']['routes']['targets']),'parts_reconstruction':str(root/'merged/parts-reconstruction.json')},'review_inputs':[str(root/'README.md'),str(root/'VISUAL-SELF-REVIEW.md'),str(root/'inheritance-source-manifest.json')],'unverified':['Independent final visual/technical acceptance','Merged world placement and natural ticks','Flooded external ocean placement and oxygen depletion','Survival player traversal, ladder climbing and container interaction','Secret lever is supported decoration; no redstone secret-door mechanism','Ocean scenery outside balcony is not included'],'previous_review':'D:\\ft工程文件\\FT地牢_第二轮独立验收_20261106\\S5-preliminary-20261107-025045-361444\\visual-review.md'}
save(root/'ready-for-review.json',readiness)
save(root/'progress.json',{'group':'S5','current':'awaiting_independent_review','status':readiness['status'],'completed':[room['id'] for room in rooms]+['merged','parts-reconstruction','self-qa'],'merged':True,'independent_acceptance':False,'group_accepted':False,'merged_sha256':merged_sha256,'source_manifest':'inheritance-source-manifest.json','pending':['independent final review'],'unverified':readiness['unverified']})
manifest = [{'path':str(path.relative_to(root)),'sha256':digest(path),'bytes':path.stat().st_size} for path in root.rglob('*') if path.is_file() and (path.suffix in ['.mjs','.cjs','.py','.ps1','.md'] or path.name in ['ready-for-review.json','candidate-index.json','progress.json','references.json'] or path.suffix=='.nbt' and 'qa' not in path.relative_to(root).parts)]
save(root/'delivery-manifest.json',manifest)
print(json.dumps({'status':readiness['status'],'merged_sha256':merged_sha256,'rooms':len(rooms),'parts':10,'all_support_nonair':sum(result['nonair_checked'] for result in support),'route_targets':readiness['self_checks']['room_route_targets'],'merged_targets':readiness['self_checks']['merged_route_targets']},ensure_ascii=False))
