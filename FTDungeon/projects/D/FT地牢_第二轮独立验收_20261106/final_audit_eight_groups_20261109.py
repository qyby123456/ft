import hashlib
import json
from datetime import datetime
from pathlib import Path
from PIL import Image
from audit_s8_submission import decode, load

review = Path('D:/ft工程文件/FT地牢_第二轮独立验收_20261106')
workspace = Path('C:/Users/YOUR_USERNAME/.codex/visualizations/2026/11/04/01a1a685-045b-76d2-aa2b-2a3f24520603')
seals = load(review/'SEVEN-GROUPS-SEAL-RECHECK-20261108.json')
assert seals['accepted_groups']==7 and seals['total_current_frozen_files_rehashed']==5411
reference_audit = load(Path('D:/ft工程文件/FT地牢_功能房间第二轮_20261106/reference-audit.json'))
expected_references = {item['id']:item for group in reference_audit['groups'] for item in group['references']}
assert len(expected_references)==80
comparison_references = {}
comparisons = []
for identifier in ['S1','S2','S3','S4','S5','S6','S7','S8']:
    paths = sorted(review.glob(identifier+'-*/comparison-index.json'),reverse=True)
    assert paths,identifier
    selected = paths[0]
    rows = load(selected)
    assert len(rows)==5
    for row in rows:
        page = Path(row['path'])
        assert hashlib.sha256(page.read_bytes()).hexdigest()==row['sha256'],page
        with Image.open(page) as image:
            image.verify()
        assert len(row['pairs'])==2
        for pair in row['pairs']:
            reference = pair['reference']
            reference_id = reference['id']
            assert reference['sha256']==expected_references[reference_id]['sha256']
            assert reference_id not in comparison_references
            comparison_references[reference_id]=dict(page=str(page),page_sha256=row['sha256'],reference_sha256=reference['sha256'],comparison_candidate_sha256=pair['candidate_sha256'],scope='Historical source-bound reference/render comparison; later candidate revisions inherit only their explicitly audited scopes, not an unchanged-source claim')
        comparisons.append(dict(group=identifier,path=str(page),sha256=row['sha256']))
assert set(comparison_references)==set(expected_references)
groups = []
rooms = []
for expected in seals['groups']:
    acceptance_path = Path(expected['acceptance'])
    assert hashlib.sha256(acceptance_path.read_bytes()).hexdigest()==expected['acceptance_sha256']
    acceptance = load(acceptance_path)
    assert acceptance['accepted'] and not acceptance.get('unresolved_required_items')
    directory = acceptance_path.parent
    records = []
    for manifest in expected['manifests']:
        assert hashlib.sha256(Path(manifest['path']).read_bytes()).hexdigest()==manifest['sha256']
        records.extend(load(Path(manifest['path']))['files'])
    merged = next(item for item in records if item['sha256']==acceptance['source_sha256'])
    assert hashlib.sha256(Path(merged['snapshot']).read_bytes()).hexdigest()==acceptance['source_sha256']
    if expected['group']=='S3':
        declared = [dict(id='S3-'+str(number),candidate=str(directory/'rooms'/('s3-'+str(number))/('s3-'+str(number)+'.nbt'))) for number in range(11,21)]
    else:
        declared = load(directory/'ready-for-review.json')['rooms']
    assert len(declared)==10
    for room in declared:
        if expected['group']=='S3':
            source = Path(room['candidate'])
            digest = hashlib.sha256(source.read_bytes()).hexdigest()
            assert any(item['snapshot']==str(source) and item['sha256']==digest for item in records)
            if room['id']=='S3-16':
                assert digest==acceptance['room16_sha256']
        else:
            digest = room['sha256']
            source = Path(next(item['snapshot'] for item in records if item['sha256']==digest and Path(item['snapshot']).suffix=='.nbt'))
        assert hashlib.sha256(source.read_bytes()).hexdigest()==digest
        data,blocks = decode(source)
        rooms.append(dict(id=room['id'],source=str(source),sha256=digest,size=data['size'],records=len(blocks),reference=expected_references[room['id']]['sha256'],reference_comparison=comparison_references[room['id']]))
    requirements = acceptance['requirements']
    evidence_files = []
    if isinstance(requirements,list):
        for requirement in requirements:
            assert requirement['result']=='PASS'
            for filename in requirement.get('evidence',[]):
                evidence = Path(filename)
                assert evidence.is_file(),evidence
                evidence_files.append(dict(path=str(evidence),sha256=hashlib.sha256(evidence.read_bytes()).hexdigest()))
    else:
        assert isinstance(requirements,dict) and all(requirements.values())
    groups.append(dict(group=expected['group'],accepted=True,source_sha256=acceptance['source_sha256'],merged_source=merged['snapshot'],acceptance=str(acceptance_path),acceptance_sha256=expected['acceptance_sha256'],requirements=requirements,evidence_files=evidence_files,limits=acceptance['limits']))
root = workspace/'preserved-review-snapshots/S8-vault-shell-revision-20261108'
s8 = load(root/'ACCEPTANCE.json')
assert s8['accepted'] and not s8['unresolved_required_items'] and len(s8['requirements'])==12
assert all(item['status'].startswith('PASS') for item in s8['requirements'])
revision = load(root/'REVISION.json')
for module in revision['modules']:
    source = next((root/module['id']).glob('*.nbt'))
    assert hashlib.sha256(source.read_bytes()).hexdigest()==module['sha256']
    data,blocks = decode(source)
    rooms.append(dict(id=module['id'],source=str(source),sha256=module['sha256'],size=data['size'],records=len(blocks),reference=expected_references[module['id']]['sha256'],reference_comparison=comparison_references[module['id']]))
groups.append(dict(group='S8',accepted=True,source_sha256=s8['source_sha256'],merged_source=str(root/'assembly/s8-functional-vault-shell.nbt'),acceptance=str(root/'ACCEPTANCE.json'),acceptance_sha256=hashlib.sha256((root/'ACCEPTANCE.json').read_bytes()).hexdigest(),requirements=s8['requirements'],limits=s8['limits']))
assert len(groups)==8 and len(rooms)==len({room['id'] for room in rooms})==80
for group in groups:
    assert len([room for room in rooms if room['id'].startswith(group['group']+'-')])==10
report = dict(status='EIGHT_GROUPS_SECOND_ROUND_ACCEPTED',accepted=True,completed_at=datetime.now().isoformat(),objective='8个线程地牢生成完成后验收',scope='Second-round S1-S8 rooms11-20:80 functional rooms,8 connected builds, recipes/previews/native QA/full parts/barriers and explicitly scoped world/access/interaction/natural-motion followups under original WORK_ORDER; previous01-10 builds remain read-only',accepted_group_count=8,accepted_room_count=80,unresolved_required_items=[],groups=groups,rooms=rooms,original_reference_comparisons_verified=80,comparison_pages_verified=40,reference_retention=dict(exact_original_png_bytes_available=len(seals['references']),original_png_paths_missing=len(seals['missing_references']),all80_original_reference_hash_bindings_and_comparison_images_retained=True,audit=str(review/'SEVEN-GROUPS-SEAL-RECHECK-20261108.json'),limitation='Lossless original PNG retention is incomplete. Historical reference/render comparisons, their image hashes, original reference hash bindings and actual visual review records remain readable and verified. Do not claim all80 original PNG bytes were rehashed or restored; this is recorded as an input-archive limitation, not a substitute for missing room/render/recipe/QA deliverables.'),production_deployed=False,generator_owned_by_other_thread=True)
(review/'FINAL-80-ROOM-ACCEPTANCE-20261109.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
lines=['# 第二轮八组地牢建筑独立验收完成','','8/8组通过：S1—S8，每组11—20号十间功能房，共80间、8个连通合并建筑。20号均为密道藏宝，不是总览。旧01—10号80房只读；本文件不冒称重新生成160房。','','| 组 | 精确合并SHA-256 | 验收签章 |','|---|---|---|']
for group in groups:
    lines.append('| '+group['group']+' | '+group['source_sha256']+' | [ACCEPTANCE]('+Path(group['acceptance']).as_posix()+') |')
lines.extend(['','本轮最后关闭S8-20上部岩壳/晶体冠视觉整改。新源31NBT独立复现、189050原生支撑和70路线目标、1009200格全包清空安装、150关服存档区块及56实体/90282屏障通过；旧交互和自然运动仅在精确差异依赖证明后继承。','','七组5411冻结文件及S8当前197/继承908文件复核；80房NBT索引、各组具体要求/证据和限制见同名JSON。','','## 原参考图归档情况','','40张历史参考/实际渲染对照页及80个原参考哈希绑定仍可读并核验，与各组实际视觉观察记录共同保留。原图库路径已失效，现有精确原PNG副本'+str(len(seals['references']))+'张，另'+str(len(seals['missing_references']))+'张未找到原始字节。未拿近似图、生图或重压缩结果冒充原PNG；该输入归档限制单列，不声称80原PNG全部恢复。','','本轮交付要求为各房NBT、配方、真实预览、原生/世界检查及合并/分片/屏障，均已按各组签章范围完成；原图留存审计与历史对照可继续用于引用。','','## 边界','','这是建筑交付验收，不是新的正式部署或完整战斗认证。刷怪/精英/Boss、生成器与正式玩家实例由独立生成器线程负责；本线程未写正式saves、mods、config或KubeJS。客户端人实际游玩、多朝向、多副本和无限时间稳定性以各组限制为准，未作扩大声明。'])
(review/'FINAL-80-ROOM-ACCEPTANCE-20261109.md').write_text(chr(10).join(lines),encoding='utf-8')
print(json.dumps(dict(status=report['status'],groups=8,rooms=80,comparison_pages=40,reference_comparisons=80,exact_png_copies=len(seals['references']),missing_original_pngs=len(seals['missing_references'])),ensure_ascii=False))
