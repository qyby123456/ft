import hashlib
import json
import zipfile
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo

from build_m7 import DELIVERY, REFERENCE_SHA256, ROOT
from structure_io import write_json


def load(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    metadata = load('delivery/M7/room.json')
    static = load('qa/static.json')
    native = load('qa/native-placement.json')
    rendering = load('qa/render.json')
    actual_hash = digest(DELIVERY / 'room.nbt')
    assert static['status'] == native['status'] == rendering['status'] == 'PASS'
    assert actual_hash == metadata['sha256'] == static['structure_sha256'] == native['structure_sha256'] == rendering['source_structure_sha256']
    assert digest(DELIVERY / 'preview.png') == rendering['preview_sha256']
    assert len(native['rotations']) == 4 and all(rotation['status'] == 'PASS' for rotation in native['rotations'])
    log_path = ROOT / 'qa/forge-gametest-final.log'
    log = log_path.read_text(encoding='utf-8', errors='replace')
    assert 'M7_NATIVE_PLACEMENT_PASS' in log and actual_hash in log
    assert 'All 1 required tests passed' in log and 'BUILD SUCCESSFUL' in log
    timestamp = datetime.now(ZoneInfo('Asia/Shanghai')).isoformat(timespec='seconds')
    result = {'status': 'PASS', 'id': 'M7', 'name': metadata['name'], 'completed_at': timestamp, 'size': metadata['size'], 'entrance': metadata['entrance'], 'interior': metadata['interior'], 'reference_sha256': REFERENCE_SHA256, 'structure_sha256': actual_hash, 'static': {'status': 'PASS', 'evidence': (ROOT / 'qa/static.json').as_posix(), 'checks': len(static['checks']), 'records': static['block_records'], 'non_air_blocks': static['non_air_blocks'], 'reachable_floor_positions': static['reachable_floor_positions']}, 'native_placement': {'status': 'PASS', 'minecraft': '1.20.1', 'forge': '47.4.10', 'rotations': 4, 'ticks': native['scheduled_tick_delay'], 'evidence': (ROOT / 'qa/native-placement.json').as_posix(), 'log': log_path.as_posix(), 'log_sha256': digest(log_path), 'required_gametests_passed': 1, 'build_successful': True}, 'preview': {'status': 'PASS', 'kind': 'software_from_exact_nbt_not_game_screenshot', 'sha256': digest(DELIVERY / 'preview.png'), 'four_views': (ROOT / 'preview/four-views.png').as_posix()}, 'reward_inventory_items': 0, 'spawned_entities': 0, 'deployment': 'NOT_DEPLOYED', 'integration': 'AWAITING_INTEGRATOR_ACCEPTANCE_AND_SLOT', 'boundaries': ['All authored assets, scripts and QA stay in the independent M7 project; Gradle uses existing shared dependency caches', 'No other room templates or integration catalog were modified', 'No player saves or installed JARs were opened for construction', 'No paid image generation was requested'], 'not_claimed': ['full-modpack client screenshot or playtest', 'multiplayer acceptance', 'formal slot assignment', 'lobby route and return portal integration', 'runtime idempotence and occupancy rejection for formal deployment']}
    write_json(DELIVERY / 'qa.json', result)
    handoff = ROOT / 'handoff'
    handoff.mkdir(exist_ok=True)
    report = f'''# M7 接入验收报告

目标接入聊天：01a19758-b0e5-75c2-822a-4860527a1e8b
生成时间：{timestamp}
状态：结构制作、静态 QA、四向 Forge 原生实际放置 QA 均 PASS；未部署，未写统一 incoming，未分配正式槽位。

## 指纹

- 参考图 SHA-256：`{REFERENCE_SHA256}`
- room.nbt SHA-256：`{actual_hash}`
- preview.png SHA-256：`{digest(DELIVERY / 'preview.png')}`
- 最终原生日志 SHA-256：`{digest(log_path)}`

## 交付

- 最小交付目录：`{DELIVERY.as_posix()}`
- 文件：room.nbt、room.json、preview.png、qa.json
- 尺寸 XYZ：{metadata['size']}；入口脚位：{metadata['entrance']}；安全内点：{metadata['interior']}；北向3×3开口。
- 预定资源：ftdungeonmobs:image25/m7；接入负责人另行分配槽位和连接道路。
- 原图13×13高8格，落地19×19高14格；六柱三拱肋、四悬灯、双横链、红玻璃祭池、玻璃下32格密封熔岩、两座隔离营火盆、六旗四头骨十组黑烛。几何原创，只用原版1.20.1方块。

## 验证证据

- `{(ROOT / 'qa/static.json').as_posix()}`：{len(static['checks'])}项静态断言；{static['block_records']}条显式记录；{static['reachable_floor_positions']}个同层安全可达脚位。
- `{(ROOT / 'qa/native-placement.json').as_posix()}`：四向原生 StructureTemplate 真实放置、逐格状态与方块实体、canSurvive、邻接形状、真实碰撞路径、120 tick后状态稳定与流体封闭。
- `{log_path.as_posix()}`：M7_NATIVE_PLACEMENT_PASS、All 1 required tests passed、BUILD SUCCESSFUL。
- `{(ROOT / 'preview/four-views.png').as_posix()}`：同哈希最终NBT四向软件剖切。不是实机截图，最终NBT仍含完整屋顶。
- 无战利品表、可领取库存、刷怪实体、活动传送门；正式核心、其他房间、已部署JAR与玩家存档均未改动。

## 接入门槛

接收后仍须完整体积/实体/边界预检、占用拒绝、独立持久生成标记、幂等保护、大厅至北口道路、返程门和五档难度回归。不要把本次隔离QA称为完整整合包客户端或多人验收。不要直接部署本工程的验证模组。

本文件是交接材料，不代表跨聊天消息已发送，也不是正式部署许可。
'''
    (handoff / 'TO_INTEGRATION.md').write_text(report, encoding='utf-8')
    output = ROOT / 'out'
    output.mkdir(exist_ok=True)
    archive = output / 'M7_暗铁献祭地窖_交付_20261102.zip'
    paths = [*DELIVERY.glob('*'), ROOT / 'README.md', *handoff.glob('*.md'), *ROOT.joinpath('qa').glob('*.json'), log_path, *ROOT.joinpath('preview').glob('*.png'), *ROOT.joinpath('tools').glob('*.py'), ROOT / 'build.gradle', ROOT / 'settings.gradle', ROOT / 'gradle.properties', *ROOT.joinpath('src').rglob('*')]
    with zipfile.ZipFile(archive, 'w', compression=zipfile.ZIP_DEFLATED) as package:
        for path in sorted(set(paths)):
            if path.is_file():
                package.write(path, path.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(archive) as package:
        assert package.testzip() is None
        assert hashlib.sha256(package.read('delivery/M7/room.nbt')).hexdigest() == actual_hash
    write_json(output / 'package-verification.json', {'status': 'PASS', 'archive': archive.as_posix(), 'archive_sha256': digest(archive), 'room_sha256': actual_hash, 'finalized_at': timestamp, 'deployment': 'NOT_DEPLOYED'})
    print(f'M7_FINAL_DELIVERY_PASS structure_sha256={actual_hash} archive_sha256={digest(archive)}')


if __name__ == '__main__':
    main()
