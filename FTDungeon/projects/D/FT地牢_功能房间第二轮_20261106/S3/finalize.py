import hashlib
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parent
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
def artifact(path):
    return {'path':str(path.resolve()),'sha256':hashlib.sha256(path.read_bytes()).hexdigest(),'bytes':path.stat().st_size}
rooms=[]
for number in range(11,21):
    identifier=f's3-{number}'
    folder=ROOT/'rooms'/identifier
    candidate=artifact(folder/(identifier+'.nbt'))
    for relative,key in [('qa/report.json','candidate_sha256'),('qa/render.json','sha256'),('qa/support.json','sha256'),('qa/visual-review.json','candidate_sha256')]:
        assert read(folder/relative)[key]==candidate['sha256'],identifier+' hash mismatch '+relative
    assert read(folder/'qa/report.json')['self_check']=='PASS'
    assert not read(folder/'qa/support.json')['failures']
    rooms.append({'id':identifier,'candidate':candidate,'recipe':artifact(ROOT/f'room{number}.mjs'),'design':artifact(folder/'design.json'),'previews':[artifact(path) for path in sorted(folder.glob('*.png'))],'qa':[artifact(path) for path in sorted((folder/'qa').glob('*.json'))],'maker_visual_review':artifact(folder/'qa/visual-review.json'),'independent_acceptance':'PENDING'})
folder=ROOT/'rooms/s3-merged'
candidate=artifact(folder/'s3-merged.nbt')
for relative,key in [('qa/report.json','candidate_sha256'),('qa/render.json','sha256'),('qa/support.json','sha256'),('qa/split-source-verification.json','source_sha256')]:
    assert read(folder/relative)[key]==candidate['sha256'],'merged hash mismatch '+relative
assert read(folder/'qa/report.json')['self_check']=='PASS'
assert not read(folder/'qa/support.json')['failures']
split=read(folder/'qa/split-source-verification.json')
assert not split['parts_failures'] and not split['source_module_failures']
assert (folder/'qa/maker-visual-review.json').exists()
manifest={'status':'READY_FOR_INDEPENDENT_REVIEW','style':'S3','room_count':10,'date':'2026-11-06','independent_acceptance':'PENDING','rooms':rooms,'merged':{'candidate':candidate,'recipe':artifact(ROOT/'assemble.mjs'),'assembly':artifact(ROOT/'assembly.json'),'parts_manifest':artifact(ROOT/'parts/manifest.json'),'parts':[artifact(Path(part['path'])) for part in split['parts']],'previews':[artifact(path) for path in sorted(folder.glob('*.png'))],'qa':[artifact(path) for path in sorted((folder/'qa').glob('*.json'))]},'tooling':[artifact(ROOT/name) for name in ['common.mjs','qa.py','SupportAudit.java','support.py','render.cjs','split-verify.mjs','review.py','finalize.py']],'tests':['各10间真实1.20.1注册/属性及StructureTemplate回读','各间全方块canSurvive与声明入口宽高/地板','各间0.6x1.8人体半格网格采样扫掠路线（40目标）','合并完整原生检查、全方块支撑和41目标路线','10个源模板逐记录状态/实体载荷保留比对','20个≤48格分片逐记录重建一致及完整外围屏障','真实Lodestone完整/剖切/关键物件近景实际查看'],'limitations':['全部为制作方自检；父线程此前REQUEST_CHANGES仅针对旧冻结快照，新版本未获独立验收','未运行世界放置、邻居更新、实际门/容器点击、流体/随机tick、真实照明、生存或战斗验收','碰撞使用离线原生形状与采样扫掠，非完整物理模拟，详见原始报告','井/池为冻结蓝冰，未提供取水；望远镜、兵器、盔甲为方块雕塑','Lock容器需正确命名钥匙；所有容器为空且无LootTable','金块为可破坏装饰，不构成生存掉落平衡验证','稀疏结构需事先清空部署体积；外围屏障到达/离开点在包络内，正式传送/部署待统一指令','本轮没有写saves/mods、旧八组或其他线程目录']}
(ROOT/'ready-for-review.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding='utf-8')
progress=read(ROOT/'progress.json')
progress['assembly']='MAKER_CHECKS_PASS_PENDING_INDEPENDENT_REVIEW'
progress['status']='READY_FOR_INDEPENDENT_REVIEW'
progress['ready_manifest']=str(ROOT/'ready-for-review.json')
(ROOT/'progress.json').write_text(json.dumps(progress,ensure_ascii=False,indent=2),encoding='utf-8')
print('Ready for independent review:',candidate['sha256'])
