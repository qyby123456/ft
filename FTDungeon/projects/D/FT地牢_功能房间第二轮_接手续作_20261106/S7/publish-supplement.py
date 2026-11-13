import hashlib
import json
import shutil
from datetime import datetime, timezone
from pathlib import Path

ROOT=Path(__file__).resolve().parent
BASE=ROOT/'ready-for-review.json'
old_bytes=BASE.read_bytes()
base=json.loads(old_bytes)
stamp=datetime.now().strftime('%Y%m%d-%H%M%S')
output=ROOT/'submissions'/('evidence-supplement-'+stamp)
output.mkdir(parents=True,exist_ok=False)
(output/'prior-ready-for-review.json').write_bytes(old_bytes)
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
paths={Path(entry['path']) for entry in base['evidence_manifest']}
for number in range(11,21):
    room=ROOT/f'S7-{number}'
    paths.update(room.glob('*.png'))
    paths.add(room/'qa/render.json')
for part in base['assembly']['parts']:
    paths.add(Path(part['path']))
paths.update([Path(base['assembly']['candidate']),ROOT/'reproduction.json',ROOT/'review-response.json',ROOT/'independent-evidence-update.json'])
for name in ['README.md','finalize.py','record-independent.py','routecoverage.py','worldqa/change-classification.json','worldqa/inputs.json','worldqa/exit.json','worldqa/tests.xml','assembly/qa/render.json','assembly/qa/full-width-routes/independent-routes.json']:
    paths.add(ROOT/name)
changed=[]
for entry in base['evidence_manifest']:
    actual=digest(Path(entry['path']))
    if actual!=entry['sha256']:
        changed.append({'path':entry['path'],'prior_declared_sha256':entry['sha256'],'new_actual_sha256':actual})
mapping={}
manifest=[]
for source in sorted(paths):
    destination=output/source.relative_to(ROOT)
    destination.parent.mkdir(parents=True,exist_ok=True)
    shutil.copyfile(source,destination)
    assert source.read_bytes()==destination.read_bytes()
    mapping[str(source)]=str(destination)
    manifest.append({'path':str(destination),'sha256':digest(destination),'source_at_capture':str(source)})
def remap(value):
    if isinstance(value,str):
        return mapping.get(value,value)
    if isinstance(value,list):
        return [remap(entry) for entry in value]
    if isinstance(value,dict):
        return {key:remap(entry) for key,entry in value.items()}
    return value
new=remap(base)
new['created_utc']=datetime.now(timezone.utc).isoformat()
new['submission_kind']='SUPPLEMENTAL_RENDER_AND_INDEPENDENT_EVIDENCE; STRUCTURE_NBT_UNCHANGED'
new['version_directory']=str(output)
new['previous_ready_sha256']=hashlib.sha256(old_bytes).hexdigest()
new['previous_ready_preserved']=str(output/'prior-ready-for-review.json')
new['changes_from_prior_manifest']=changed
new['independent_evidence_update']=str(output/'independent-evidence-update.json')
new['evidence_manifest']=manifest
new['unverified']=list(dict.fromkeys(new['unverified']+['S7-16 side stairs and smaller fungal cluster visual revision remains open','Fire/fluid observations cover source records; no external-barrier propagation certification','Door setOpen API tests are not real player inputs or redstone operation']))
for room in new['rooms']:
    number=room['id'].split('-')[1]
    room['images']=[str(path) for path in sorted((output/f'S7-{number}').glob('*.png'))]
new['group_accepted']=False
assert base['assembly']['sha256']==digest(Path(base['assembly']['candidate']))
for room in base['rooms']:
    assert digest(Path(room['candidate']))==room['sha256']
notes=['# S7 补充证据提交','','本次不改变十房NBT、合并NBT或15个分片；合并SHA-256仍为 '+base['assembly']['sha256']+'。', '', '上一清单声明的渲染脚本哈希与补近景后脚本不同，旧清单原字节保存在prior-ready-for-review.json。changes_from_prior_manifest逐项记录旧声明与当前捕获哈希，不将其伪称一致。独立验收方已经冻结的03:46证据不修改。', '', '新增12 guard-detail、13 prep-detail、15 dummy-detail、16 channel-detail，均为原候选的真实Lodestone渲染，已实际目检；无新NBT候选。该版本目录中的证据为本次捕获副本，后续整改必须另建版本目录。', '', '20号拉杆旧缺陷已由独立复核关闭；14号三实体和装备证据保留。独立报告原路径与哈希见independent-evidence-update.json。', '', '范围纠正：totalIssues不统计state_changes，火/流体只遍历源显式记录；独立前后NBT检查补足的是结构内部方块类型/遗漏格，没有外部传播证明。setOpen是API往返，非玩家输入/拉杆操作；世界测试未被父线程独立重跑。', '', '仍待：独立最终视觉与合并验收、11上层玩家攀梯、16侧阶/小菌群整改、真实玩家开箱与完整往返、合并结构动态更新。group_accepted=false。']
(output/'SUPPLEMENT.md').write_text('\n'.join(notes)+'\n',encoding='utf-8')
new['evidence_manifest'].append({'path':str(output/'SUPPLEMENT.md'),'sha256':digest(output/'SUPPLEMENT.md')})
encoded=json.dumps(new,ensure_ascii=False,indent=2)
(output/'ready-for-review.json').write_text(encoded,encoding='utf-8')
BASE.write_text(encoded,encoding='utf-8')
progress=json.loads((ROOT/'progress.json').read_text(encoding='utf-8'))
progress['latest_submission']=str(output/'ready-for-review.json')
progress['independent_lever_issue']='CLOSED_FOR_6a7f9c3c_BY_EXTERNAL_REVIEW'
progress['pending']=new['unverified']
(ROOT/'progress.json').write_text(json.dumps(progress,ensure_ascii=False,indent=2),encoding='utf-8')
with (ROOT/'README.md').open('a',encoding='utf-8') as stream:
    stream.write('\n## 补充证据版本\n\n最新清单：'+str(output/'ready-for-review.json')+'\n\n'+ '\n'.join(notes[2:])+'\n')
assert all(digest(Path(entry['path']))==entry['sha256'] for entry in new['evidence_manifest'])
print(json.dumps({'version':str(output),'evidence_files':len(manifest),'changed_from_prior':changed,'all_evidence_hashes_match':True,'all_candidate_hashes_unchanged':True},ensure_ascii=False,indent=2))
