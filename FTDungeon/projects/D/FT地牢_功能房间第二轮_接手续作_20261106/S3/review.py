import hashlib
import json
from pathlib import Path
import sys
ROOT=Path(__file__).resolve().parent
room=ROOT/'rooms'/sys.argv[1]
checksum=hashlib.sha256((room/(sys.argv[1]+'.nbt')).read_bytes()).hexdigest()
report=json.loads((room/'qa/report.json').read_text(encoding='utf-8'))
render=json.loads((room/'qa/render.json').read_text(encoding='utf-8'))
support=json.loads((room/'qa/support.json').read_text(encoding='utf-8'))
assert checksum==support['sha256'] and not support['failures']
assert checksum==report['candidate_sha256']==render['sha256']
assert report['self_check']=='PASS'
reference=next(item for item in json.loads((ROOT/'references.json').read_text(encoding='utf-8'))['references'] if item['id'].lower()==sys.argv[1])
assert hashlib.sha256(Path(reference['image_path']).read_bytes()).hexdigest()==reference['sha256']
evidence={'candidate_sha256':checksum,'reference':reference,'prompt_sha256':hashlib.sha256(Path(reference['prompt_path']).read_bytes()).hexdigest(),'review':'制作方已实际查看参考和当前候选Lodestone剖切图','notes':sys.argv[2],'independent_acceptance':'PENDING','preview_sha256':hashlib.sha256((room/'cutaway.png').read_bytes()).hexdigest()}
(room/'qa/visual-review.json').write_text(json.dumps(evidence,ensure_ascii=False,indent=2),encoding='utf-8')
progress=json.loads((ROOT/'progress.json').read_text(encoding='utf-8'))
progress['rooms'][sys.argv[1]]['visual_review']='SELF_REVIEWED'
(ROOT/'progress.json').write_text(json.dumps(progress,ensure_ascii=False,indent=2),encoding='utf-8')
print('Recorded maker visual review',sys.argv[1],checksum)
