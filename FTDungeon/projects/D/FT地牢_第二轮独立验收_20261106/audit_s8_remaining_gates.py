import hashlib
import json
import math
from datetime import datetime
from pathlib import Path
from audit_s8_submission import load

review = Path('D:/ft工程文件/FT地牢_第二轮独立验收_20261106')
workspace = Path('C:/Users/YOUR_USERNAME/.codex/visualizations/2026/11/04/01a1a685-045b-76d2-aa2b-2a3f24520603')
snapshots = workspace / 'preserved-review-snapshots'
prior = snapshots / 'S8-submission-20261107-104351-507543'
root = snapshots / 'S8-interaction-revision-20261107-1355'
followup = snapshots / 'S8-followup-20261107-1600'
runtime = snapshots / 'S8-runtime-supplement-20261107-1340'
revision = load(root / 'access-revision-r4/REVISION.json')
digest = revision['sha256']
frozen_counts = {}
for folder, names in [(prior, ['submission-snapshot.json', 'supplemental-snapshot.json']), (root, ['submission-snapshot.json']), (followup, ['submission-snapshot.json']), (runtime, ['submission-snapshot.json']), (snapshots / 'S8-install-supplement-20261107-1125', ['submission-snapshot.json'])]:
    count = 0
    for name in names:
        snapshot = load(folder / name)
        for item in snapshot['files']:
            path = Path(item['snapshot'])
            assert hashlib.sha256(path.read_bytes()).hexdigest() == item['sha256'], path
            count += 1
    frozen_counts[folder.name] = count
assert sorted(frozen_counts.values()) == [103, 129, 205, 216, 255]
reproduction = load(root / 'independent-current-reproduction.json')
assert reproduction['all31_binary_equal'] and reproduction['source_sha256'] == digest and not reproduction['current_candidate_nbt_used_as_recipe_input']
parts = load(root / 'independent-parts-targets.json')
assert parts['source_sha256'] == digest and parts['parts'] == 20 and parts['boundary'] == 90282 and parts['full_partition_volume'] == 1009200
changes = load(root / 'independent-changes-inspection.json')
assert changes['source_sha256'] == digest and changes['all_original_nonair_and_be_preserved']
interactions = load(root / 'independent-interaction-inspection.json')
assert interactions['source_sha256'] == digest and interactions['functional_targets'] == 105 and interactions['ray_checks'] == 168
assert interactions['door_open_reset_pairs'] == 6 and interactions['menu_open_count'] == 38
natural = load(followup / 'independent-followup-inspection.json')
assert natural['source_sha256'] == digest and natural['natural_travel_updates'] == 1082 and len(natural['all29_new_blocks_mapped']) == 29
world = load(prior / 'independent-world-inspection.json')
assert world['source_sha256'] == revision['frozen_sha256'] and world['zero_type_changes'] and world['natural_wait_ticks'] == 600
assert world['waypoints'] == 4413 and world['returned_to_start']
qa = prior / 'independent-qa'
assert sum(row['nonair_checked'] for row in load(qa / 'independent-all-support.json')) == 240460
assert all(not row['failures'] and not row['unsupported'] for row in load(qa / 'independent-all-support.json'))
assert all(row['registry_properties'] == row['template_roundtrip_block_count'] == 'PASS' for row in load(qa / 'independent-native-frozen.json'))
assert sum(len(row['targets']) for row in load(qa / 'independent-routes.json')) == 96
assert all(row['start_clear'] and all(target['reachable'] and target['standing_clear'] for target in row['targets']) for row in load(qa / 'independent-routes.json'))
geometry = []
modules = {module['id']: module for module in revision['modules']}
for identifier in modules:
    report = load(prior / 'independent-geometry' / identifier / 'geometry-report.json')
    assert all(sample['native_clear'] for sample in report['samples'])
    for sample in report['samples']:
        if 'declared full ' not in sample['label'] or not sample['label'].endswith(' aperture'):
            continue
        bounds = sample['bounds']
        assert len(bounds) == 6
        for change in revision['changes']:
            if change['room'] == identifier:
                point = change['pos']
                assert not all(max(point[axis], bounds[axis]) < min(point[axis] + 1, bounds[axis + 3]) for axis in range(3)), (identifier, sample, change)
    geometry.append(dict(room=identifier, samples=len(report['samples']), declared_apertures=sum('declared full ' in sample['label'] and sample['label'].endswith(' aperture') for sample in report['samples']), aperture_labels=[sample['label'] for sample in report['samples'] if 'declared full ' in sample['label']]))
assert sum(row['samples'] for row in geometry) == 47 and sum(row['declared_apertures'] for row in geometry) == 20
ladder_directory = Path(load(runtime / 'independent-runtime-r4.json')['runtime'])
ladder = load(ladder_directory / 'ladder-physics.json')
room11 = next((root / 'access-revision-r4/S8-11').glob('*.nbt'))
assert ladder['source_sha256'] == hashlib.sha256(room11.read_bytes()).hexdigest()
assert ladder['top_landed'] and ladder['returned_to_base'] and ladder['climbable_observed'] and ladder['descent_climbable_observed']
assert len(ladder['trace']) == 70 and ladder['trace'][-1]['tick'] == 280 and ladder['highest_y'] > 8
assert ladder['trace'][-1]['on_ground'] and math.dist(ladder['trace'][0]['position'], ladder['trace'][-1]['position']) < .6
accepted = []
for path in [*review.glob('S*-submission-*/ACCEPTANCE.json'), snapshots / 'S3-access-revision-20261107-1103/ACCEPTANCE.json']:
    item = load(path)
    assert item['accepted']
    source_digest = item['source_sha256']
    files = load(path.parent / 'submission-snapshot.json')['files']
    matching = [record for record in files if record['sha256'] == source_digest]
    assert matching and any(hashlib.sha256(Path(record['snapshot']).read_bytes()).hexdigest() == source_digest for record in matching), path
    accepted.append(dict(group=item['group'], acceptance=str(path), acceptance_sha256=hashlib.sha256(path.read_bytes()).hexdigest(), source_sha256=source_digest))
assert len(accepted) == 7 and {row['group'] for row in accepted} == {'S1', 'S2', 'S3', 'S4', 'S5', 'S6', 'S7'}
requirements = [
    dict(requirement='十个功能房间11至20，20为密道藏宝，原非空气/实体/功能保留', status='PASS', evidence=[str(root / 'independent-changes-inspection.json'), str(prior / 'independent-assembly-parts.json')]),
    dict(requirement='十房配方、当前合并及全部分片可重现', status='PASS', evidence=[str(prior / 'independent-reproduction.json'), str(root / 'independent-current-reproduction.json')]),
    dict(requirement='原生注册、属性、模板往返、支撑和指定路线', status='PASS', evidence=[str(qa), str(root / 'independent-current-qa')], scope='旧全11文件240460支撑/96目标；新13/16及合并184546支撑/66目标，8房字节不变'),
    dict(requirement='20个声明接口全宽净空及旧11梯口转换', status='PASS_WITH_EXPLICIT_INHERITANCE', evidence=[str(prior / 'independent-geometry'), str(ladder_directory / 'ladder-physics.json')], scope='19个3x4及15东口1个5x5，共47原生样本；29新增方块不与原接口矩形相交；11源字节不变，实际280步上下梯'),
    dict(requirement='十房合并连通、全图返回、液体及邻居更新安全', status='PASS_WITH_EXPLICIT_INHERITANCE', evidence=[str(prior / 'independent-world-inspection.json'), str(root / 'independent-interaction-inspection.json'), str(followup / 'independent-followup-inspection.json')], scope='旧4d 600自然等待+4413点遍历返回；新6988路线射线及四必要局部往返，不冒称新SHA全图连续自然走'),
    dict(requirement='所有功能对象实际眼位首命中/use、门控及容器', status='PASS', evidence=[str(root / 'independent-interaction-inspection.json')], scope='105对象168眼位射线、38菜单、6门打开复位；每次use前按已核入口路线重设站位，非全程自然步行'),
    dict(requirement='新增厨房梯/平台/灶阶、水井桥/台阶实际自然上下往返', status='PASS', evidence=[str(followup / 'independent-followup-inspection.json')], scope='4课程1082次travel；29格逐项映射，包括背板和叠层支撑，不冒称背板直接行走'),
    dict(requirement='完整<=48分片、门缝处理、外围屏障不截断内部连接', status='PASS', evidence=[str(root / 'independent-parts-targets.json'), str(root / 'independent-current-qa/independent-routes.json')], scope='20片全体积1009200无重叠无缺口，199790记录与56实体精确反拼，90282外围屏障'),
    dict(requirement='全包真实清空/放置、外部进出及落点安全', status='PASS', evidence=[str(followup / 'independent-followup-inspection.json')], scope='新ZIP全体积实心污染后清空安装；具名管理员及非OP访客管理员托管进出，非权限0自动触发系统'),
    dict(requirement='服务器关闭后实际存档持久化回读', status='PASS', evidence=[str(workspace / 's8-independent-saved-readback-20261107-1600/saved-readback.json')], scope='150区块1009200格，56实体；57合法相邻形状和12床旧color规范化单列'),
    dict(requirement='逐图视觉对照、19晶洞纵深、20阶梯岩壳、新13/16设施', status='PENDING_NOT_PROVEN', evidence=[str(prior / 'REVIEW.md'), str(workspace / 's8-independent-visual-review-20261107-1600')], reason='view_image成功但未返回任何图像内容，不能以文件存在、源哈希或渲染成功代替观察'),
    dict(requirement='README、哈希、复现及限制完整且不修改正式实例', status='PASS', evidence=[str(root / 'access-revision-r4/README.md'), str(followup / 'access-followup-r1/README.md')], scope='仍为隔离建筑验收；刷怪/精英/Boss/奖励及现场生成器归另一线程，不声称本轮已完成玩法或正式部署')
]
report = dict(group='S8', accepted=False, status='TECHNICAL_GATES_VERIFIED_VISUAL_FIDELITY_PENDING', reviewed_at=datetime.now().isoformat(), source_sha256=digest, previous_goal_turn='交接职责已发送并确认，完成管理状态推进；本轮继续房间验收实际证据', frozen_files_verified=frozen_counts, accepted_groups=accepted, geometry_inheritance=geometry, original_ladder_updates=280, requirements=requirements, remaining=['实际查看当前19/20与参考对照，核对原视觉整改是否关闭', '实际查看新13/16四张设施截图与本轮源匹配全景', '视觉证据闭合后重新运行最终整组审计，才能写ACCEPTANCE并将8/8标为完成'], production_deployed=False)
(root / 'completion-audit.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
lines = ['# S8当前6988候选：技术门槛已核，视觉仍待核', '', '本组 accepted=false；八组总进度保持7/8。不得将本文件当作整组验收通过或部署许可。', '', '当前精确源SHA-256：' + digest, '', '## 逐项完成审计', '']
for requirement in requirements:
    lines.extend(['- ' + requirement['status'] + '：' + requirement['requirement'] + '。' + requirement.get('scope', requirement.get('reason', '')), ''])
lines.extend(['## 仍未完成', '', *['- ' + item for item in report['remaining']], '', 'view_image在本轮再次实际调用，返回成功但没有图像内容。独立渲染和哈希只证明图片来源，不证明美观、比例或特征相符。', '', '旧4d测试保留原SHA范围；新6988的29空气新增、单个灶前脚点上抬和21处标签调整均单列。原旧脚点失败记录未删除。', '', '1082次新运动的最大单次位移约0.517146格：该步包含原生0.5格自动迈步与约0.132格水平运动，非瞬移；每步竖向<=0.6、水平<0.3，测试源码仅课程起点写位置。', '', '完整路径、冻结文件重核及原始结果见completion-audit.json。'])
(root / 'REVIEW.md').write_text(chr(10).join(lines), encoding='utf-8')
print(json.dumps(dict(status=report['status'], accepted=False, frozen_counts=frozen_counts, prior_accepted_groups=len(accepted), requirements=len(requirements), pending=sum(item['status'] == 'PENDING_NOT_PROVEN' for item in requirements)), ensure_ascii=False))
