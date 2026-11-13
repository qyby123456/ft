import hashlib
import json
import subprocess
from pathlib import Path
from check import READER

root=Path(__file__).resolve().parent
qa=root/'qa/room17-storage-fix'
before=root/'qa/before-upper-barrel-fix'
digest=lambda path:hashlib.sha256(path.read_bytes()).hexdigest()
def records(path):
    data=READER.decode(path)
    return {tuple(entry['pos']):(data['palette'][entry['state']],entry.get('nbt')) for entry in data['blocks']}
changes={}
for identifier,filename in [('S5-17','s5-17.nbt'),('merged','merged.nbt')]:
    previous=records(before/identifier/filename);current=records(root/identifier/filename)
    changes[identifier]=[{'position':position,'before':previous[position],'after':current[position]} for position in previous if previous[position]!=current[position]]
    assert len(changes[identifier])==6
unchanged=[]
for entry in json.loads((before/'candidate-index.json').read_text(encoding='utf-8')):
    if entry['id']=='S5-17':continue
    assert digest(Path(entry['candidate']))==entry['sha256']
    unchanged.append(entry['id'])
old_parts=json.loads((before/'merged/parts.json').read_text(encoding='utf-8'))
new_parts=json.loads((root/'merged/parts.json').read_text(encoding='utf-8'))
changed_parts=[index+1 for index,(old,new) in enumerate(zip(old_parts['parts'],new_parts['parts'])) if old['sha256']!=new['sha256']]
assert changed_parts==[8]
supportqa=qa/'support'
supportqa.mkdir(exist_ok=True)
specifications=[{'group':number,'candidate':str(root/identifier/filename),'sha256':digest(root/identifier/filename)} for number,identifier,filename in [(517,'S5-17','s5-17.nbt'),(500,'merged','merged.nbt')]]
(supportqa/'route-input.json').write_text(json.dumps(specifications,ensure_ascii=False,indent=2),encoding='utf-8')
(supportqa/'vanilla-block-tags.json').write_bytes((root/'S5-11/qa/vanilla-block-tags.json').read_bytes())
classpath='D:/ft工程文件/FT地牢_第二轮独立验收_20261106/classes;'+';'.join(Path('D:/ft工程文件/FT地牢_第7组逐图重做_20261104/10-overview/qa/isolated-run-05/classpath.txt').read_text(encoding='utf-8-sig').strip().splitlines())
process=subprocess.run(['C:/Program Files/Java/jdk-25.0.2/bin/java.exe','-Xmx1536m','-cp',classpath,'IndependentAllSupportAudit',str(supportqa)],cwd=supportqa,capture_output=True,timeout=360)
(supportqa/'support.log').write_bytes(process.stdout+process.stderr)
assert process.returncode==0
support=json.loads((supportqa/'independent-all-support.json').read_text(encoding='utf-8'))
assert all(not entry['failures'] and not entry['unsupported'] for entry in support)
report={'source_sha256':digest(root/'S5-17/s5-17.nbt'),'merged_sha256':digest(root/'merged/merged.nbt'),'changes':changes,'unchanged_source_ids':unchanged,'changed_parts':changed_parts,'support_passed':True,'scope':'Producer focused regression; six changed state/payload positions in source17 and merged, only part08 hash changes, nine unchanged sources inherit frozen evidence.'}
(qa/'change-and-support-report.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'source_changes':len(changes['S5-17']),'merged_changes':len(changes['merged']),'unchanged_sources':len(unchanged),'changed_parts':changed_parts,'support_passed':True}))
