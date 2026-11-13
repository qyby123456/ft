import collections
import hashlib
import json
import sys
from datetime import datetime, timezone
from pathlib import Path

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parent
OLD = Path(r'D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S7')
sys.path.insert(0,str(ROOT))
from qa import Structure, plain, save

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

assembly = json.loads((ROOT/'assembly/assembly.json').read_text(encoding='utf-8'))
world = json.loads((ROOT/'worldqa/world-report.json').read_text(encoding='utf-8'))
reproduction = json.loads((ROOT/'reproduction.json').read_text(encoding='utf-8'))
width_routes = json.loads((ROOT/'assembly/qa/full-width-routes/summary.json').read_text(encoding='utf-8'))
assert width_routes['status']=='SELF_CHECK_PASS'
assert all(record['semantic_payload_equal'] for record in reproduction)
assert world['status']=='EXECUTED_NO_LISTED_FAILURES'
rows=[]
for number in range(11,21):
    room=ROOT/f'S7-{number}'
    summary=json.loads((room/'summary.json').read_text(encoding='utf-8'))
    candidate=Path(summary['nbtPath'])
    sha=digest(candidate)
    check=json.loads((room/'qa/self-check.json').read_text(encoding='utf-8'))
    assert check['sha256']==sha and check['status']=='SELF_CHECK_PASS'
    live=next(entry for entry in world['rooms'] if entry['id']==room.name)
    assert live['sha256']==sha
    check['world_validation']={'report':str(ROOT/'worldqa/world-report.json'),'source_sha256':sha,'ticks':live['ticks'],'author_executed':True,'independent_acceptance':False}
    check['not_certified']=['Player input and client interactions','Ladder ascent/descent physics','Combat and survival completion','Production-world deployment']
    save(room/'qa/self-check.json',check)
    design=json.loads((room/'design.json').read_text(encoding='utf-8'))
    if 'revision_2' in design:
        design['revision_2']['preserved_evidence']=str(OLD/'history/pre-visual-request-changes'/room.name)
    design['takeover_source_manifest']=str(ROOT/'takeover-source-manifest.json')
    save(room/'design.json',design)
    support=json.loads((room/'qa/independent-all-support.json').read_text(encoding='utf-8'))[0]
    route=json.loads((room/'qa/independent-routes.json').read_text(encoding='utf-8'))[0]
    rows.append({'id':room.name,'candidate':str(candidate),'sha256':sha,'size':summary['size'],'recipe':str(room/'recipe.mjs'),'postprocess':['python -X utf8 revise.py '+str(number)],'qa':str(room/'qa/self-check.json'),'nonair_support_checked':support['nonair_checked'],'targets':len(route['targets']),'world_ticks':live['ticks'],'images':[str(path) for path in room.glob('*.png')],'independent_acceptance':False})
    for filename in ['full.png','cutaway.png','interior.png']:
        assert (room/filename).exists()
    structure=Structure.read(candidate)
    if number in [14,18,20]:
        save(room/'qa/functional-payloads.json',{'sha256':sha,'block_entities':[{'position':position,'nbt':plain(entity)} for position,entity in structure.block_entities.items()],'entities':[plain(entity) for entity in structure.entities],'live_world':live})
for part in assembly['parts']:
    assert digest(Path(part['path']))==part['sha256'] and max(part['size'])<=48
assert digest(Path(assembly['candidate']))==assembly['sha256']
state_changes=collections.Counter((entry['before'].split('[')[0],entry['after'].split('[')[0]) for room in world['rooms'] for entry in room['state_changes'])
save(ROOT/'worldqa/change-classification.json',{'bound_report_sha256':digest(ROOT/'worldqa/world-report.json'),'counts':[{'before_block':before,'after_block':after,'count':count} for (before,after),count in state_changes.items()],'all_changes_preserve_block_id':all(before==after for before,after in state_changes),'meaning':'Mushroom face culling and wall/bar connections changed; no block-id loss. Original per-position before/after retained in world-report.json.'})
review_notes={
 '11':'L形书架与木石门框近景已存在；攀梯上层完整玩家运动仍待实际交互复核。',
 '12':'六牢门在隔离世界执行开关往返；看守台独立近景仍可追加，当前室内与配方可查。',
 '13':'feature-detail显示铁链、悬锅与炉口；无燃烧方块，以菌光体表达炉光；备餐台见配方及库存清单。',
 '14':'三具穿甲盔甲架在NBT及1200刻后的世界中均有真实装备，functional-payloads与world-report可查；Lodestone不显示这些实体。',
 '15':'靶面近景存在；草人横臂及长凳围栏保留，独立视觉仍待复判。',
 '16':'井池、横梁、吊桶与前渠保留；普通流体刻已执行且无新增流体格，侧阶及小菌群视觉仍待复判。',
 '17':'补L形侧架、浅木半成品框架与两凳，project-detail和砂轮feature-detail已实际目检。',
 '18':'新增祭坛周边菌群；头颅/蜡烛均为真实方块，龛架近景及载荷文件已核验。',
 '19':'重建不规则地面/洞壁、观景窗后菌菇景深及柱间栏杆，保留6格高差与12半步级；补回两处根须支撑，全支撑通过。',
 '20':'保留密道、拉杆、宝箱及金堆；增加拉杆北侧石背板、木石龛框和顶缘根须，新支撑通过；vault-detail已目检。拉杆不控制暗门，宝箱为空。'
}
save(ROOT/'review-response.json',{'latest_external_review_read':r'D:\ft工程文件\FT地牢_第二轮独立验收_20261106\S7-preliminary-20261107-004405-272572\visual-review.md','responses':review_notes,'claim':'Evidence response only, does not close independent visual findings by author assertion.'})
unverified=['Current final hashes require fresh independent static/visual/merge review','Ladder climb and upper archive access using real player inputs','Full player interactions, chest GUI and furnace operation','Merged assembly placement and neighbor/random/fluid tick simulation','Combat, survival completion and production-world deployment','Long-duration and multiple-rotation updates beyond single-orientation 1200-tick individual test']
save(ROOT/'progress.json',{'style':'S7','state':'READY_FOR_INDEPENDENT_REVIEW','current':'assembly','completed_author_checks':[row['id'] for row in rows],'pending':unverified,'group_accepted':False,'independent_acceptance':'REQUEST_CHANGES_ON_OLDER_SNAPSHOT; FINAL_HASH_REVIEW_PENDING','production_writes':False,'assembly_sha256':assembly['sha256'],'next':'Independent reviewer reads ready-for-review.json; retain all earlier evidence and use exact hashes.'})
lines=['# S7 蘑菇真菌 11—20 接手续作交付候选','','状态：制作方检查与合并完成，等待当前最终哈希的独立复核；group_accepted=false。旧根只读，不写正式saves/mods/config/KubeJS。','','|房间|最终候选 SHA-256|全非空气支撑格|原指定目标|','|---|---|---:|---:|']
lines += [f"|{row['id']}|{row['sha256']}|{row['nonair_support_checked']}|{row['targets']}|" for row in rows]
lines += ['',f"合并：`assembly/s7-fungal-functions-connected.nbt`，SHA-256 `{assembly['sha256']}`，尺寸 {assembly['size']}，{len(assembly['parts'])} 片，每轴≤48。反拼 {assembly['partition_comparison']['block_records']} 条方块记录、46 方块实体、3 具装备实体及坐标/完整载荷逐项相同。",'',f"合并原95目标及所有连接步点的三格宽截面，共 {width_routes['targets']} 目标均通过0.6×1.8原生碰撞检查；176370格全非空气支撑无失败。外侧屏障 {assembly['barrier_count']} 格位于房间和走廊联合包络外，不进入室内；两端外出口有屏障封口，此为离线候选，不是世界部署入口。",'', '隔离原生1.20.1行为测试：Forge 47.4.10仅作GameTest载体，十房各1200刻，randomTickSpeed=3、doFireTick=true，显式邻居/流体调度；无支撑失败、掉落物、火和新流体格。六牢门API往返开关，14号铁/金/铁三具装备实体及铁剑实际保留。所有状态变化及逐点前后值保留在worldqa/world-report.json；蘑菇贴面、铁栏/石墙连接发生原生属性变化，方块ID未损失。此为制作方测试，不是独立验收。', '', '复现：node <skill>/scripts/run-build.mjs <room>/recipe.mjs --out-dir <room> --json，然后运行工单Python -X utf8 revise.py <编号>，最后render.cjs及qa.py。reproduce.py将十房生成到reproduction，八房文件哈希完全相同，11/12字节哈希不同但逐格状态、尺寸、DataVersion、方块实体和实体载荷完全相同；两个哈希均保留，不冒称字节一致。assemble.py从最终十房重建合并；routecoverage.py覆盖走廊每个步点和三格宽截面。', '', '真实Lodestone图：每房full/cutaway/interior与feature-detail；17另有project-detail，20另有vault-detail。assembly含完整、剖切、室内和plan-cutaway。剖切仅显示副本，源NBT保留壳体。', '', '逐房复核回应见review-response.json，17/18/19/20新候选的作者目检不等于独立视觉通过。旧候选和失败证据位置见takeover-source-manifest.json及旧根history。', '', '未验证项：']
lines += ['- '+item for item in unverified]
(ROOT/'README.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
manifest=[]
for entry in rows:
    paths=[Path(entry['candidate']),Path(entry['recipe']),Path(entry['qa'])]+[Path(path) for path in entry['images']]
    for path in paths:
        manifest.append({'path':str(path),'sha256':digest(path)})
for path in [ROOT/'common.mjs',ROOT/'revise.py',ROOT/'assemble.py',ROOT/'render.cjs',ROOT/'qa.py',ROOT/'worldqa/world-report.json',ROOT/'assembly/assembly.json',ROOT/'assembly/qa/self-check.json',ROOT/'assembly/qa/full-width-routes/summary.json']:
    manifest.append({'path':str(path),'sha256':digest(path)})
save(ROOT/'ready-for-review.json',{'schema':1,'created_utc':datetime.now(timezone.utc).isoformat(),'group':'S7','status':'READY_FOR_INDEPENDENT_REVIEW','group_accepted':False,'scope':'Author-checked final ten candidates, assembly, <=48 partitions and full payload reconstruction; not an independent pass.','rooms':rows,'assembly':assembly,'full_width_routes':str(ROOT/'assembly/qa/full-width-routes/summary.json'),'world_report':str(ROOT/'worldqa/world-report.json'),'reproduction':str(ROOT/'reproduction.json'),'review_response':str(ROOT/'review-response.json'),'unverified':unverified,'evidence_manifest':manifest})
print('READY_FOR_INDEPENDENT_REVIEW',assembly['sha256'],len(rows),'rooms',len(assembly['parts']),'parts')
