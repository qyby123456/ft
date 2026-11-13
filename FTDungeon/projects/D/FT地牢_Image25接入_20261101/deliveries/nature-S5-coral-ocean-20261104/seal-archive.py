import datetime
import hashlib
import json
from pathlib import Path

root = Path(__file__).resolve().parent
target = root / 'ARCHIVED-NOT-CANDIDATE.json'
records = []
for path in sorted(root.rglob('*')):
    if path.is_file() and path != target:
        records.append(dict(path=str(path),relative_path=path.relative_to(root).as_posix(),size=path.stat().st_size,sha256=hashlib.sha256(path.read_bytes()).hexdigest()))
record = dict(status='ARCHIVED_NOT_CANDIDATE',candidate_eligible=False,include_descendants=True,root=str(root),thread='01a1a5df-a60b-7f02-877f-856a3b8f501e',sealed_at=datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=8))).isoformat(),reason='Entire batch must be redone; await assigned new batch and specific reference before creating any structure.',supersedes_all_prior_candidate_or_pass_claims=True,source_outputs_preserved_in_place=True,next_action='WAIT_NEW_BATCH_AND_SPECIFIC_REFERENCE',files=records)
with target.open('x',encoding='utf-8') as output:
    json.dump(record,output,ensure_ascii=False,indent=2)
    output.write('\n')
print('Archived in place, excluded from candidates:',len(records),'files;',sum(item['relative_path'].endswith('.nbt') for item in records),'NBT files.')
