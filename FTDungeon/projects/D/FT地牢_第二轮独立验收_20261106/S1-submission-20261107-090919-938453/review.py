import json
import pathlib
import sys
from datetime import datetime

root = pathlib.Path(__file__).parent
folder = root / sys.argv[1]
design = json.loads((folder / 'design.json').read_text(encoding='utf-8'))
check = json.loads((folder / 'self-check.json').read_text(encoding='utf-8'))
record = {'source_sha256':check['source_sha256'],'reviewed_at':datetime.now().isoformat(),'reviewer':'maker_agent_not_independent','viewed':['complete.png','cutaway.png'],'notes':sys.argv[2:],'status':'maker_visual_review_complete_waiting_independent_review'}
(folder / 'visual-review.json').write_text(json.dumps(record,ensure_ascii=False,indent=2),encoding='utf-8')
progress = json.loads((root / 'progress.json').read_text(encoding='utf-8'))
progress['rooms'][design['id']]['visual_review'] = record['status']
progress['rooms'][design['id']]['next'] = 'next reference, independently reviewed later'
(root / 'progress.json').write_text(json.dumps(progress,ensure_ascii=False,indent=2),encoding='utf-8')
