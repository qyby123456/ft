import json
import pathlib

ROOT=pathlib.Path(__file__).parent
for name in ['ready-for-review.json','handoff-for-independent-review.json']:
    path=ROOT/name
    value=json.loads(path.read_text(encoding='utf-8'))
    value.update(status='revision_in_progress_prior_candidate_superseded',independent_acceptance=False,review_report=r'D:\ft工程文件\FT地牢_第二轮独立验收_20261106\S1-submission-20261107-045549-014527\REVIEW.md',current_changes=['19 clear north viewing window and delivered 64-block forest depth','11 one root voxel cleared from full-width port envelope','merge layout shifted south for scenery; high connector moved east','full corridor envelope audit, merge reproduction and real world runtime evidence in progress'])
    path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
path=ROOT/'progress.json'
value=json.loads(path.read_text(encoding='utf-8'))
value.update(current='review_requested_revision_in_progress',merge='new_candidate_QA_in_progress',independent_acceptance=False)
path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
