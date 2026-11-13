import hashlib
import json
from datetime import datetime
from pathlib import Path

root = Path(__file__).resolve().parent
world_dir = root / 'worldqa-r3'
ladder_dir = root / 'ladderqa-r2'
world = json.loads((world_dir / 'world-report.json').read_text(encoding='utf-8'))
ladder = json.loads((ladder_dir / 'ladder-physics.json').read_text(encoding='utf-8'))
captures = json.loads((world_dir / 'capture-audit.json').read_text(encoding='utf-8'))
ready = json.loads((root / 'ready-for-review.json').read_text(encoding='utf-8'))
baseline = {}

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def inspect(value):
    if isinstance(value, dict):
        if 'path' in value and 'sha256' in value:
            path = Path(value['path'])
            assert digest(path) == value['sha256'], f'Frozen file drift: {path}'
            baseline[str(path)] = value['sha256']
        for item in value.values():
            inspect(item)
    elif isinstance(value, list):
        for item in value:
            inspect(item)

inspect(ready)
rooms = world['candidates']
assert len(rooms) == len(captures) == 11
assert all(not room['support_failures'] and not room['dropped_items'] and room['walk']['failures'] == 0 for room in rooms)
assert all(all(lock['empty_hand_denied'] and lock['wrong_name_denied'] and lock['correct_key_opened_menu'] for lock in room['locks']) for room in rooms)
assert all(capture['captured_records'] == capture['full_volume'] and not any(capture['issues'].values()) for capture in captures)
assert all(json.loads((folder / 'exit.json').read_text(encoding='utf-8'))['exit_code'] == 0 for folder in (world_dir, ladder_dir))
doors = next(room for room in rooms if room['id'] == 's3-12')
assert len(doors['door_control']) == 6
assert all(door['open'] and not door['powered'] for door in doors['doors_before_interaction'])
assert all(door['lever_use_powered_open'] and door['lever_use_unpowered_closed'] and door['restored_open'] for door in doors['door_control'])
assert all(ladder[key] for key in ('top_landed', 'climbable_observed', 'descent_climbable_observed', 'returned_to_base'))
portal = rooms[-1]['entry_exit']
assert portal['entry_clear'] and portal['exit_clear'] and portal['entry_command_result'] == portal['exit_command_result'] == 1
total_targets = sum(len(room['walk']['targets']) for room in rooms)
assert total_targets == 81
locks = sum(len(room['locks']) for room in rooms)
files = []
for folder in (world_dir, ladder_dir):
    files.extend(folder.glob('*.json'))
    files.extend(folder.glob('*.xml'))
    files.extend(folder.glob('*.nbt'))
    files.extend(folder.glob('*.log'))
    files.append(folder / 'run.ps1')
    files.extend(folder.glob('native-test/*.gradle'))
    files.extend(folder.glob('native-test/*.properties'))
    files.extend(path for path in (folder / 'native-test/src').rglob('*') if path.is_file())
files += [world_dir / 'server/server.properties', root / 'CaptureAudit.java', root / 'capture-audit.py', root / 'seal-runtime.py', root / 'package-deployment.py', root / 'deployment/package-report.json', root / 'deployment/README.md']
report = {
    'status': 'MAKER_RUNTIME_SUBSET_PASS_PENDING_INDEPENDENT_REVIEW',
    'timestamp': datetime.now().astimezone().isoformat(),
    'source_sha256': rooms[-1]['source_sha256'],
    'source_candidates_unchanged': True,
    'frozen_unique_files_checked': len(baseline),
    'candidates': 11,
    'ordinary_server_ticks_after_placement': 1200,
    'collision_move_roundtrip_targets': total_targets,
    'lock_locations': locks,
    'lock_use_checks': locks * 3,
    'doors_stable_open_after_ticks': 6,
    'temporary_lever_control_checks': 12,
    'ladder_top_and_descent': True,
    'admin_entry_exit_commands': portal,
    'full_capture_audit': captures,
    'limitations': ['Forge 47.4.10 isolated void-flat world; target modpack not launched', 'Walk is Entity.move collision roundtrip; no client keyboard, jumping, gravity or combat certification', 'Ladder separately uses Player.travel each server tick; no injected upward velocity', 'doMobSpawning disabled for isolated fixture; prior terrain entities/clearing drops removed before placement', 'No claim every cell received random ticks; applicability counts are in capture audit', 'Source room12 has no controls; lever use tested as temporary overlay, separately packaged optional installer', 'Admin-led entry/exit mechanism; packaged complete install/place functions not yet executed as a unit', 'Other workstations, full illumination, survival balance and combat not certified'],
    'files': [{'path': str(path), 'sha256': digest(path), 'bytes': path.stat().st_size} for path in sorted(set(files)) if path.is_file()],
}
(root / 'runtime-ready-for-review.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
random_cells = sum(capture['source_random_ticking_cells'] for capture in captures)
notes = f'''# S3 当前精确哈希隔离世界补验

当前合并 SHA256：`{report['source_sha256']}`。全部十房、合并及原静态/视觉证据未变，{len(baseline)}个原清单唯一文件重新哈希一致。19洞深、20箱体可见性不再修改。状态为制作方运行时子集通过、等待独立复核，不代表整组最终验收。

## 已执行

- Forge 47.4.10 / Minecraft 1.20.1 独立GameTest世界；十房与合并逐个校验输入哈希，清空完整包围盒，StructureTemplate以真实邻居更新放置，再逐方块更新邻居，经过1200个服务器刻。无正式实例写入。
- 11份完整刻后NBT及回读清单在 `worldqa-r3`。全部体积记录数一致；方块类型变化、源省略空气区新增非空气方块、支撑失败、掉落实体、非空/战利品容器、锁名称变化均为0。12栏杆有12项连接属性变化、13玻璃片有1项变化；合并共13项，同类方块保持，不是原载荷字节不变。
- 实际ServerLevel碰撞形状搜索及FakePlayer Entity.move往返覆盖原81个目标，零失败。它包含抬升/水平/下降的受碰撞移动，不代表自然键盘或完整重力行走。
- 11/14/20的3种Lock在单房及合并共6处，空手、错误自定义名称均拒绝；正确自定义名称的绊线钩调用真实Block.use后确实打开菜单，共18项检查。
- 12六扇铁门1200刻后保持open=true,powered=false；六个临时地面拉杆均经真实LeverBlock.use验证上电开门、断电关门，测试结束还原常开。原NBT仍无拉杆，部署附加层单独标明。
- 11原梯子在 `ladderqa-r2` 用生存FakePlayer Player.travel逐刻上攀、登顶、返回梯子下降并回到地面。只在开始设置位置，无中途传送或注入上升速度。第一轮下降控制误停梯沿，已修正为x=26.45；临时源修改已完全恢复，原哈希保持一致。
- 实际ServerPlayer及正常服务器传送处理器执行管理员tp @s：从外部安全平台进入arrival、从departure返回平台，命令结果与最终位置/净空均通过。保留外围屏障。第一轮FakePlayer网络处理器禁用了传送，该轮不作进出通过证据；第二轮已使用正常ServerPlayer传送处理器，只屏蔽网络发送。

## 数据包与限制

`deployment/README.md` 给出固定原点0,100,0的可执行进出命令、182条fill覆盖完整1,572,165格体积、25分片安装及可选门控附加层。仅生成交付包，未安装正式世界，也未把整套install函数称为已经执行。

刻后回读中随机刻适用方块记录合计为{random_cells}；randomTickSpeed=3，不能声称每格收到了随机刻。测试环境为显式配置的虚空超平坦世界，关闭生物生成，并在放置前清理原地形实体及清空过程掉落，未移除候选合法植物或家具。前两轮普通地形的清场残留花和种子污染掉落统计，均保留旧日志，但世界放置以r3为有效证据；梯子使用ladderqa-r2。

井池为蓝冰不可取水；其他工作方块的完整使用、客户端照明、生存平衡、战斗与目标整合包兼容性仍未覆盖。独立复核尚未给出当前运行时接受结果。

复现：保留现有世界，复制对应native-test源码、资源、构建文件及run.ps1至本S3下一个新同级目录，并复制worldqa-r3/server/server.properties到新目录server/server.properties，之后执行run.ps1；随后运行capture-audit.py 新目录名。完整证据文件哈希见runtime-ready-for-review.json。不要在旧证据目录重跑覆盖日志。
'''
(root / 'RUNTIME-REVIEW.md').write_text(notes, encoding='utf-8')
progress_path = root / 'progress.json'
progress = json.loads(progress_path.read_text(encoding='utf-8'))
progress['timestamp'] = report['timestamp']
progress['runtime_review'] = str(root / 'RUNTIME-REVIEW.md')
progress['runtime_manifest'] = str(root / 'runtime-ready-for-review.json')
progress['runtime_status'] = report['status']
progress['unclosed_review_items'] = ['当前运行时证据待独立复核', '完整数据包install函数尚未整体执行', '其他工作方块使用、客户端照明、生存与战斗未覆盖']
progress_path.write_text(json.dumps(progress, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps({key: value for key, value in report.items() if key not in ('files', 'full_capture_audit', 'limitations', 'admin_entry_exit_commands')}, ensure_ascii=False, indent=2))
