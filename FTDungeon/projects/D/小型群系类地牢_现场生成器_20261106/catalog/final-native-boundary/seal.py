import hashlib
import json
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def load(path):
    return json.loads(path.read_text(encoding='utf-8'))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(name, value):
    (ROOT / name).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    lock = load(ROOT / 'input-lock.json')
    snapshot = Path(lock['snapshot'])
    for entry in lock['sourceSnapshotFilesVerified']:
        assert digest(snapshot / entry['component'] / entry['path']) == entry['sha256'], entry['path']
    immutable = [entry for entry in lock['copiedFiles'] if not entry['path'].startswith('run/world/')]
    for entry in immutable:
        assert digest(ROOT / entry['path']) == entry['sha256'], entry['path']
    runtime = load(snapshot / 'runtime/ready.json')
    artifact = snapshot / 'runtime' / Path(runtime['artifact'].replace('\\', '/'))
    assert digest(artifact) == lock['runtimeJarSha256']
    report = load(ROOT / 'native-boundary-report.json')
    assert (report['attempted'], report['passed'], report['directed']) == (352, 352, 704)
    assert (report['singletonAtoms'], report['openIntervalAtoms']) == (192, 160)
    assert len(report['sceneChecks']) == 24
    assert all(list(entry.values()).count('PASS') == 1 for entry in report['sceneChecks'])
    assert all(entry['pass'] and entry['forward']['pass'] and entry['returned']['pass'] for entry in report['checks'])
    assert report['completeContinuousMotionProof'] is False
    progress = load(ROOT / 'progress.json')
    assert progress['exitCode'] == 0
    log = (ROOT / 'build.log').read_text(encoding='utf-8', errors='replace')
    assert 'All 1 required tests passed' in log and 'BUILD SUCCESSFUL' in log
    for name in ('launch.json', 'launch-incomplete-attempt2.json'):
        assert set(load(ROOT / name).get('environment', {})) <= {'MOD_CLASSES'}
    class_entries = []
    for folder in ('project/build/classes/java/main', 'project/build/classes/java/gameTest'):
        for path in sorted((ROOT / folder).rglob('*.class')):
            class_entries.append({'path': path.relative_to(ROOT).as_posix(), 'sha256': digest(path)})
    save('executed-classes.json', class_entries)
    world_entries = [{'path': path.relative_to(ROOT).as_posix(), 'sha256': digest(path)} for path in sorted((ROOT / 'run/world').rglob('*')) if path.is_file()]
    save('world-final-hashes.json', world_entries)
    verdict = {
        'status': 'PASS_FULL_DECLARED_WIDTH_CLEARANCE_WITH_FINITE_NATIVE_STEP_CONTEXT_EVIDENCE',
        'taskScope': '12 declared strips, exact width 0.6, headroom 3, contract min/max feet, step <=0.6',
        'geometryEvidence': {'nativeRecords': 4416, 'exactCoverageTiles': 4620, 'certificateStatus': 'CONDITIONAL_FINITE_COVERAGE_PASS', 'certificateSha256': digest(ROOT / 'inputs/finite-certificate.json')},
        'nativeEvidence': {key: report[key] for key in ('status', 'attempted', 'passed', 'directed', 'singletonAtoms', 'openIntervalAtoms', 'contextChecks', 'sourceShapeChecks')},
        'nativeReportSha256': digest(ROOT / 'native-boundary-report.json'),
        'runtimeJarSha256': lock['runtimeJarSha256'],
        'runtimeExecution': 'Forge dev run compiled from unchanged final snapshot main/core sources; frozen reobfuscated production JAR is an input identity, not claimed as directly loaded bytecode',
        'snapshotFilesReverified': len(lock['sourceSnapshotFilesVerified']),
        'copiedImmutableFilesReverified': len(immutable),
        'completeContinuousMotionProof': False,
        'finiteReduction': [
            'Exact rational partition retains every singleton and open interval at collision-box footprint critical coordinates; geometric checks cover all real coordinates in each tile.',
            'Within each declared open x atom the active box-overlap set and support envelope do not gain a box boundary. Motion is along one cardinal axis, with x fixed in the source frame. Fixed boxes give the same vertical and travel-axis clipping constraints throughout that atom in real arithmetic.',
            'All 352 native step discontinuity representatives run both directions (704); singleton endpoints are executed separately, not discarded. Per-move/settle context shape, state, fluid and exact declared body clearance are checked. Initial/final full readback covers all 12 scenes.',
            'These jointly close the required finite declared-strip clearance and representative native step/context obligations. No concrete remaining geometric blocker was observed for these inputs. Requiring an infinite JVM coordinate enumeration is not a task gate.'
        ],
        'limits': [
            'This is not a universal IEEE754 branch-equivalence theorem for every real or machine coordinate. Native width and maxUpStep are 0.6000000238418579; declared geometric width is exactly 0.6. The representative executions retain actual native values.',
            'Per-move context checks plus fixed state/shape evidence establish invariance on executed trajectories; arbitrary external context changes and arbitrary player states are outside this certificate.',
            'Native execution uses horizontal subdivision <=0.005 plus vanilla move and gravity, not an unrestricted speed, diagonal motion, arbitrary approach, or all future world mutation guarantee.',
            'The side doublegate does not close the central arch. Sidegate interaction, whole dungeon generation, persistence/restart and protection remain separate integration gates.',
            'Experimental derived room remains accepted=false; production promotion=false.'
        ],
        'concreteRemainingDeclaredGeometryBlockers': [],
        'productionPromotion': False,
        'finishedAt': datetime.now(timezone.utc).isoformat()
    }
    save('verdict.json', verdict)
    note = '''# Catalog native boundary result

判定：FULL_DECLARED_WIDTH_CLEARANCE 在本批 12 条有限声明条带内通过。4416 条完整原生碰撞记录的精确有理数 4620 tile 覆盖，与本轮 352/352 边界、704/704 双向原生 step/context 执行共同支撑这一结论。192 个 singleton 与 160 个 open interval 均保留，24 次条带初末全量回读通过；23784 次 context 检查、370364 次来源状态/形状检查通过。实际 Forge GameTest 显示 All 1 required tests passed，Gradle exit 0，并正常保存退出。

有限归约理由：每个开区间内没有新增声明宽度 footprint/box 临界边界，参与碰撞的集合与支撑高度固定；固定横坐标、沿旋转后基数方向移动时，垂直及行进轴的固定 box 裁剪约束在实数模型内相同。几何覆盖检查整个区间，原生代表执行验证各实际 step 分支及双向落差，singleton 独立执行，途中逐步核对 context/state/shape/fluid。当前没有具体可复现的声明条带几何阻断，不再把无穷坐标 JVM 遍历设为额外门槛。

completeContinuousMotionProof=false 仍如实保留：没有声称所有浮点输入的原生分支完全等价。实际原生宽度/stepHeight 为 0.6000000238418579，声明宽度为精确 0.6；原生记录保留这一差异。context 不变性是执行轨迹逐步检查加固定状态/形状证据，非任意实体状态或未来世界修改的保证。移动采用 <=0.005 水平细分、原生 move 与重力；没有内部传送，也没有缩小碰撞体通过。源码里的 PENDING 是输入要求原文，执行结果以各 forward/returned.pass 和顶层状态为准。

运行身份：标准 Gradle runGameTestServer 从最终快照未改动的 main/core 源码编译运行；最终发布 JAR 的哈希用于锁定输入身份，不宣称开发映射 class 与重混淆 JAR 是同一字节码。529 个最终快照文件以及复制后的不可变输入再次校验。executed-classes.json 锁定实际编译 class，world-final-hashes.json 锁定测试后世界。

单独保留的范围：侧双门不封闭中央拱道，交互、全生成、重启持久化与保护检查由 integration 独立合并。本证据不修改任何原房间、其他组件、integration 世界或已安装游戏。派生房间 accepted=false，productionPromotion=false。

证据入口：ready.json、verdict.json、native-boundary-report.json、build.log、input-lock.json、final-snapshot-lock.json、verification/、inputs/。build-attempt1/2/3 与 launch-incomplete-attempt2 是实际启动之前的失败/捕获尝试，不能算原生通过；launch 文件只保存 MOD_CLASSES 环境白名单。
'''
    (ROOT / 'README.md').write_text(note, encoding='utf-8')
    names = ['README.md', 'verdict.json', 'native-boundary-report.json', 'input-lock.json', 'final-snapshot-lock.json', 'progress.json', 'executed-classes.json', 'world-final-hashes.json', 'build.log', 'build-attempt1.log', 'build-attempt2.log', 'build-attempt3.log', 'failed-native.log', 'launch.json', 'launch-incomplete-attempt2.json', 'prepare.py', 'run.py', 'seal.py']
    paths = [ROOT / name for name in names]
    for folder in ('inputs', 'verification', 'failed-verification', 'run/logs'):
        paths.extend(path for path in (ROOT / folder).rglob('*') if path.is_file())
    inventory = {path.relative_to(ROOT).as_posix(): {'sha256': digest(path), 'bytes': path.stat().st_size} for path in sorted(set(paths))}
    save('evidence-inventory.json', inventory)
    save('ready.json', {'component': 'catalog-final-native-boundary', 'status': verdict['status'], 'verdict': 'verdict.json', 'report': 'native-boundary-report.json', 'inventory': 'evidence-inventory.json', 'inventorySha256': digest(ROOT / 'evidence-inventory.json'), 'files': len(inventory), 'runtimeJarSha256': lock['runtimeJarSha256'], 'completeContinuousMotionProof': False, 'productionPromotion': False, 'serverExited': True})
    print(json.dumps(load(ROOT / 'ready.json'), ensure_ascii=False))


if __name__ == '__main__':
    main()
