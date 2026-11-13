import hashlib
import json
from datetime import datetime, timezone
from pathlib import Path

root=Path(__file__).resolve().parent
parent=root.parent.parent
def load(path):
    return json.loads(path.read_text(encoding='utf-8'))
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def save(path,data):
    path.write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf-8')
ancestry=load(root/'revision-parent.json')
assert all(digest(Path(entry['source']))==entry['sha256'] for entry in ancestry['sources'])
assembly=load(root/'assembly/assembly.json')
routes=load(root/'assembly/qa/full-width-routes/summary.json')
assert routes['status']=='SELF_CHECK_PASS' and routes['sha256']==assembly['sha256']
assert digest(Path(assembly['candidate']))==assembly['sha256']
rows=[]
for number in range(11,21):
    room=root/f'S7-{number}'
    summary=load(room/'summary.json')
    candidate=Path(summary['nbtPath'])
    sha=digest(candidate)
    check=load(room/'qa/self-check.json')
    assert check['sha256']==sha and check['status']=='SELF_CHECK_PASS'
    changed=number in [15,19,20]
    if not changed:
        original=next(entry for entry in ancestry['sources'] if entry['id']==room.name)
        assert original['sha256']==sha
    rows.append({'id':room.name,'candidate':str(candidate),'sha256':sha,'recipe':str(room/'recipe.mjs'),'size':summary['size'],'changed_in_v3':changed,'qa':str(room/'qa/self-check.json'),'qa_provenance':'New author check on current source' if changed else 'Inherited exact-hash source check; old report paths retained as provenance','world_test':'NOT_RUN_FOR_NEW_HASH' if changed else 'Exact parent source previously tested in isolated world; see independent evidence report','images':[str(path) for path in sorted(room.glob('*.png'))]})
pending=['Independent review of new 15/19/20 hashes and new merge/parts','11: thick entry, upper ladder real-player access','12: thick cell frame and real door control interaction','13: embedded layered stove and entry structure','14: weapon rack detail and entry frame','16: layered stone-soil base, smaller fungi, side access','17: thick entry and shelf cave relief','18: thicker niche frames and coffin side relief','New source world updates for 15/19/20 and merged structure','Player chest/lever/menu interaction, full traversal and multiple orientations','Independent coverage proof for continuous width at every turn; sampled native checks alone do not prove continuous global clearance','Clearance plan independently inspect and authorize before any deployment; not executed']
save(root/'progress.json',{'state':'V3_PRIORITY_REVISIONS_READY_FOR_REVIEW_REMAINDER_OPEN','current':'assembly','changed_rooms':['S7-15','S7-19','S7-20'],'group_accepted':False,'independent_acceptance':'PENDING_FOR_NEW_HASHES','pending':pending,'old_submission_preserved':True,'production_writes':False,'next':'Independent hash-bound review; continue remaining per-room relief and player interaction in a new revision directory.'})
save(root/'review-response.json',{'external_review':r'D:\ft工程文件\FT地牢_第二轮独立验收_20261106\S7-submission-20261107-034601-369679\REVIEW.md','priority_updates':{'19':{'source_sha256':rows[8]['sha256'],'changes':'18 extra cells of enclosed viewing depth; near/middle/far fungi at x=19/10/3, stepped cave bed, irregular back projection and supported hanging roots; original functional geometry shifted +18 x, stairs and original target labels retained','standing_eye':[28.5,10.62,18.5],'standing_feet':[28.5,9,18.5],'feet_native_target_added':True,'view':'standing-vista.png; actual source displayed without cutaway','limits':'Safe closed viewing cave, not infinite landscape; client/player motion not executed'},'20':{'source_sha256':rows[9]['sha256'],'changes':'Stepped left fungal mass, roof crowns and side buttresses; thick inset frame; chest/gold/lever/secret route retained','view':'vault-massing.png and vault-detail.png','lever':'Retained wall/south, backing [29,4,11]; current author support check passed, old independent closure cannot auto-certify new hash'},'15':{'source_sha256':rows[4]['sha256'],'changes':'Optional supported balance beam, low log hurdle and three half-height stepping stones; four dummies/targets/benches/fence retained','view':'obstacle-detail.png','targets':'Original six plus three approaches all reachable; no jumping certification'},'clearance':'110 nonoverlapping fill cuboids <=32768 each cover full 1969040 block bounds; unexecuted block-only plan, not sparse part footprint'},'group_accepted':False,'remaining':pending})
support=load(root/'assembly/qa/independent-all-support.json')[0]
readme=['# S7 visual-depth-v3 新版本整改候选','','状态：15/19/20优先项已制作与自检，剩余逐房视觉与交互继续开放；不是整组通过。旧0d1d1906合并与其正式清单、冻结证据不修改。', '', '|房间|本版修改|候选SHA-256|','|---|---|---|']
readme += [f"|{row['id']}|{'新候选' if row['changed_in_v3'] else '原哈希继承'}|{row['sha256']}|" for row in rows]
readme += ['',f"新合并SHA-256：{assembly['sha256']}；尺寸{assembly['size']}；{len(assembly['parts'])}个分片。方块、BE与3实体完整装备/位置反拼见assembly.json。原目标保留，19坐标随功能区域+18 X平移；新增高台视点和三器械接近点。初始目标99点，全宽采样{routes['targets']}点均通过；全非空气{support['nonair_checked']}格零失败/不支持。这些是制作方检查，不是独立放行。",'', '19真实高台视点脚点[28.5,9,18.5]、眼位[28.5,10.62,18.5]，作为明确目标通过原生静态碰撞；站位图使用完整NBT，未剖去玻璃/栏杆。原功能台、楼梯及上下6格高差保留，仅扩充窗外受围护的观景洞体。', '', '20保留当前宝箱入口视线、密道、金堆与原拉杆支撑，增加顶部/左右菌岩体块。15增加明确器械但为可选侧线，未以删目标/家具换通行。', '', '清场方案：assembly/clearance/clear-entire-bounds.mcfunction，110条fill覆盖1969040格完整外包，含分片省略的空分区；每条≤32768且不重叠。只生成，不执行；执行锚点需是未来批准的整结构最小角，零旋转，必须先全清场再放任意分片。它只处理方块，不删除世界实体。', '', '复现：node <skill>/scripts/run-build.mjs S7-XX/recipe.mjs --out-dir S7-XX --json；工单Python -X utf8 revise.py XX；node render.cjs <NBT>；Python -X utf8 qa.py <NBT>。assemble.py重建合并与分片，routecoverage.py验证走廊采样，clearance.py重建清场方案。reproduce.py十房均逐格/载荷一致，8房字节哈希一致，11/12记录顺序导致字节哈希差异不冒称一致。', '', '依赖：本目录common/qa/revise/render/assemble，技能安装目录的run-build/preview和固定Lodestone依赖，原生审核类及映射1.20.1 jar，nbtio.py。外部路径与文件哈希见external-dependencies.json。旧7房原生报告保留旧路径作为原哈希证据；15/19/20及新合并均新跑。原世界1200刻测试不覆盖这三个新哈希，不能继承为新版世界通过。', '', '仍未闭合：']
readme += ['- '+item for item in pending]
(root/'README.md').write_text('\n'.join(readme)+'\n',encoding='utf-8')
skill=Path(r'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill')
external=[skill/'scripts/run-build.mjs',skill/'scripts/preview.mjs',skill/'package.json',skill/'npm-shrinkwrap.json',Path(r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\output\dungeon-expansion-20261101\tools\nbtio.py'),Path(r'D:\ft工程文件\锈铜宝库地牢_20261101\qa\vanilla-1.20.1-mapped.jar')]
for directory in [Path(r'D:\ft工程文件\FT地牢_独立复核_20261104_01a1a685\classes'),Path(r'D:\ft工程文件\FT地牢_第二轮独立验收_20261106\classes')]:
    external += list(directory.glob('Independent*Audit*.class'))
save(root/'external-dependencies.json',{'files':[{'path':str(path),'sha256':digest(path)} for path in external if path.exists()],'native_classpath_list':r'D:\ft工程文件\霜雪花园地牢_20261101\build\classpath\runData_minecraftClasspath.txt','environment':'Node runtime / installed skill dependencies and external jars required; not a standalone vendored distribution'})
files=[path for path in root.rglob('*') if path.is_file() and path.name!='ready-for-review.json' and 'reproduction' not in path.parts and '__pycache__' not in path.parts and 'logs' not in path.parts]
manifest=[{'path':str(path),'sha256':digest(path)} for path in sorted(files)]
ready={'schema':2,'group':'S7','revision':'visual-depth-v3','created_utc':datetime.now(timezone.utc).isoformat(),'status':'PRIORITY_REVISION_READY_FOR_REVIEW_REMAINDER_OPEN','group_accepted':False,'read_only_parent':str(parent),'rooms':rows,'assembly':assembly,'clearance_plan':str(root/'assembly/clearance/clearance-plan.json'),'full_width_routes':str(root/'assembly/qa/full-width-routes/summary.json'),'pending':pending,'evidence_manifest':manifest}
save(root/'ready-for-review.json',ready)
assert all(digest(Path(entry['path']))==entry['sha256'] for entry in manifest)
print(json.dumps({'ready':str(root/'ready-for-review.json'),'assembly_sha256':assembly['sha256'],'evidence_files':len(manifest),'parent_unchanged':True,'all_manifest_hashes_match':True},ensure_ascii=False))
