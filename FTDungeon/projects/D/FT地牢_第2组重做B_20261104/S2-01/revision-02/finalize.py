import hashlib
import importlib.util
import json
from pathlib import Path
import shutil

ROOT = Path(__file__).resolve().parent
TARGET = ROOT / 'incoming/G2B-01-R2'


def save(path, data):
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    metadata = json.loads((TARGET / 'room.json').read_text(encoding='utf-8'))
    native = json.loads((ROOT / 'qa/native-review.json').read_text(encoding='utf-8'))
    shots = json.loads((ROOT / 'qa/lodestone-screenshots.json').read_text(encoding='utf-8'))
    sha = digest(TARGET / 'room.nbt')
    assert native['status'] == 'PASS' and native['rooms'][0]['sha256'] == sha == metadata['sha256']
    assert native['rooms'][0]['audited_targets'] == 17
    for shot in shots:
        assert shot['final_nbt_sha256'] == sha
        assert digest(Path(shot['screenshot'])) == shot['screenshot_sha256']
        assert digest(Path(shot['rendered_nbt'])) == shot['rendered_nbt_sha256']
    shutil.copy2(TARGET / 'lodestone-interior.png', TARGET / 'preview.png')
    specification = importlib.util.spec_from_file_location('independent_readonly_tools', 'D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/audit.py')
    audit = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(audit)
    def normalized(path, rotation):
        template = audit.decode(path)
        size, palette = template['size'], template['palette']
        positions = [tuple(record['pos']) for record in template['blocks'] if palette[record['state']]['Name'] not in audit.AIR and all(2 <= record['pos'][axis] < size[axis] - 2 for axis in range(3))]
        return audit.occupancy(positions, size, rotation)
    own = normalized(TARGET / 'room.nbt', 0)
    comparisons = []
    for group, relative, candidate, preview in audit.GROUPS:
        if group == 2:
            continue
        peer = audit.BASE / relative / candidate
        if not peer.exists():
            comparisons.append({'group': group, 'status': 'SOURCE_MISSING', 'path': str(peer)})
            continue
        scores = []
        for rotation in range(4):
            other = normalized(peer, rotation)
            scores.append(len(own & other) / len(own | other) if own | other else 1)
        comparisons.append({'group': group, 'status': 'SCREENED', 'path': str(peer), 'sha256': digest(peer), 'best_rotation_material_blind_jaccard': max(scores)})
    save(ROOT / 'duplicate-self-check.json', {'own_sha256': sha, 'method': 'Read-only reuse of independent auditor decode/occupancy helpers: nonair/nonfluid inner volume with 2-block border excluded, normalized 3D occupancy and four rotations', 'comparisons': comparisons, 'conclusion': 'Screening only; no all-group route or semantic acceptance claimed', 'no_peer_files_modified': True})
    qa = json.loads((TARGET / 'qa.json').read_text(encoding='utf-8'))
    qa.update({'status': 'SELF_CHECK_PASS_PENDING_INDEPENDENT_REVIEW', 'native': native['rooms'][0], 'lodestone': shots, 'registration': 'NOT_ACCEPTED_S2_02_LOCKED'})
    save(TARGET / 'qa.json', qa)
    registration = {'status': 'READY_FOR_INDEPENDENT_RE_REVIEW_NOT_ACCEPTED', 'reviewer_thread': '01a1a685-045b-76d2-aa2b-2a3f24520603', 'obsolete_reviewer_not_used': '01a19c8f-6747-7572-a77a-6e90084bbd34', 'group': 2, 'image': 1, 'candidate_id': metadata['id'], 'nbt': str(TARGET / 'room.nbt'), 'sha256': sha, 'size': metadata['size'], 'metadata': str(TARGET / 'room.json'), 'native': str(ROOT / 'qa/native-review.json'), 'screenshots': str(ROOT / 'qa/lodestone-screenshots.json'), 'assembly': str(ROOT / 'assembly.json'), 'duplicate_check': str(ROOT / 'duplicate-self-check.json'), 'next_image': 'S2-02 LOCKED_NOT_STARTED', 'deployment': 'NOT_DEPLOYED', 'world_placement': 'NOT_RUN', 'old_hash_superseded_not_accepted': metadata['supersedes_sha256']}
    save(ROOT / 'registration.json', registration)
    lines = ['# S2-01 同张修订02：待独立复验', '', '本轮仅修复第2组第1张；未开始S2-02。旧哈希d7ea7cbc…不是已验收，旧候选保留用于对照，不得将旧README的自检完成当作放行。', '', '## 对复核意见的逐项处理', '', '- 恢复结构内部北侧外接平台与三级台阶：入口脚位[12,1,0]，依次升到[12,2,2]、[12,3,3]、[12,4,4]，厅堂脚位Y=4。', '- 北侧门槛[12,4,6]不是外接锚点；南[12,4,30]、西[0,4,18]、东[24,4,18]均明确比北外接锚点高3格。详见room.json和assembly.json。', '- 尺寸由25×14×25扩展为25×18×31，在同一个NBT内保留台阶、平台与基础；没有改正式世界。室内平面未缩水，保留祭坛四侧干燥环行区。', '- 增加连续双层墙体、宽柱脚/柱头、内外柱带、三级水平挑檐和屋面间隔矮垛；拱洞具4格高的5格宽底口及逐层收窄的拱冠，开口贯通墙厚。安全完整屋顶仍保留，概念图精细破损不冒称逐像素复刻。', '- 实际Lodestone完整外壳与剖视截图见incoming/G2B-01-R2/lodestone-exterior.png和lodestone-interior.png；preview.png现在为真实Lodestone截图。software-check.png另标为软件方块检查图。', '- qa/lodestone-screenshots.json同时绑定最终NBT、剖切NBT、截图各自SHA-256；preview-only/仅作渲染副本，严禁放置或接入。', '- 保留4株枯灌木，逐株下方为sand；不把离线Bootstrap标签未绑定造成的canSurvive=false视作缺陷，也不伪称完整数据包标签环境运行通过。灯、沙块、花盆支撑通过，无水/熔岩。', '', '## 验证与状态', '', '- PASS：原版1.20.1注册表和状态、NBT解码、StructureTemplate加载/往返；13950条记录，13种状态。', '- PASS：17项原生碰撞寻路目标，包括4接口、祭台、4象限及8个入口台阶/门槛目标；可达位置463。', '- 重复度：只读核对独立复核清单中第3至8组当时的实际候选并记录哈希和3D材质无关粗筛，duplicate-self-check.json不是全组通过证明。', '- 未执行：ServerLevel实际放置、Forge更新稳定性及实机游玩。没有部署、改mods/config或正式存档。', '- 登记：READY_FOR_INDEPENDENT_RE_REVIEW_NOT_ACCEPTED；由新独立复核聊天判定，S2-02保持锁定。没有依赖旧复核写入，也没有回发其他聊天。', '', '## 文件与原点', '', '- incoming/G2B-01-R2/room.nbt：唯一可复核正式候选；入口/出口都是局部玩家脚部坐标。', '- 原点[0,0,0]为北外接平台包围盒西北底角；世界原点未分配。单NBT拼装顺序仅一步，详见assembly.json。', '- 检查整个25×18×31体积，包括显式空气；发现既有占用、液体、实体或世界边界冲突即拒绝，不得无条件清空。', '', f'最终NBT SHA-256：{sha}', '']
    (ROOT / 'README.md').write_text('\n'.join(lines), encoding='utf-8')
    superseded = {'status': 'SUPERSEDED_NEEDS_REVISION_NOT_ACCEPTED', 'old_nbt_sha256': metadata['supersedes_sha256'], 'current_revision': str(ROOT), 'current_sha256': sha, 'next_image': 'S2-02 LOCKED', 'old_files_preserved': True}
    save(ROOT.parent / 'CURRENT_REVIEW_STATUS.json', superseded)
    files = sorted(path for path in ROOT.rglob('*') if path.is_file() and path.suffix in {'.nbt', '.png', '.json', '.py', '.mjs', '.java', '.md'})
    records = {str(path.relative_to(ROOT)): digest(path) for path in files if path.name != 'FILE_MANIFEST.json'}
    save(ROOT / 'FILE_MANIFEST.json', records)
    assert all(digest(ROOT / path) == expected for path, expected in records.items())
    print(f'FINAL {sha}; {len(records)} manifest entries verified; PENDING_RE_REVIEW; S2-02 LOCKED')


if __name__ == '__main__':
    main()
