import hashlib
import json
import shutil
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image

from nbtio import Structure, plain


ROOT = Path(__file__).resolve().parents[1]


def read(name):
    return json.loads((ROOT / name).read_text(encoding='utf-8'))


def write(name, value):
    (ROOT / name).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    source = Structure.read(ROOT / 'room.nbt')
    fingerprint = digest(ROOT / 'room.nbt')
    metadata = read('room.json')
    static = read('qa/static.json')
    native = read('qa/forge-native.json')
    renders = read('qa/render-manifest.json')
    visual = read('qa/visual-review.json')
    assert all(report['structure_sha256'] == fingerprint for report in (static, native, renders, visual))
    assert metadata['sha256'] == fingerprint and static['status'] == native['status'] == visual['status'] == 'PASS'
    assert native['stabilization_ticks'] == 200 and native['chunks_forced_to_tick'] and len(native['rotations']) == 4
    captures = []
    for result in native['rotations']:
        target = ROOT / result['world_capture']
        captured = Structure.read(target)
        assert result['status'] == 'PASS' and result['dropped_entities'] == 0
        assert captured.size == source.size and captured.data_version == source.data_version
        assert captured.blocks == source.blocks and captured.entities == source.entities
        assert {position: plain(tag) for position, tag in captured.block_entities.items()} == {position: plain(tag) for position, tag in source.block_entities.items()}
        assert digest(target) == result['capture_sha256']
        captures.append({'rotation': result['rotation'], 'path': target.relative_to(ROOT).as_posix(), 'sha256': digest(target), 'records': len(captured.blocks), 'blockstate_mismatches': 0, 'block_entity_mismatches': 0, 'status': 'PASS'})
    write('qa/capture-review.json', {'id': 'zone-M03', 'status': 'PASS', 'structure_sha256': fingerprint, 'method': 'Independent Python NBT decoder compares each inverse-rotated native world capture after 200 ticks against final room.nbt, including complete states and empty barrel NBT.', 'records_checked': sum(result['records'] for result in captures), 'rotations': captures})
    image_paths = []
    for result in renders['records']:
        target = ROOT / result['path']
        assert digest(target) == result['sha256']
        with Image.open(target) as image:
            assert list(image.size) == result['size']
            image.verify()
        image_paths.append(result['path'])
    assert len([record for record in renders['records'] if record['kind'] == 'interior' and record['direction'] != 'sheet']) == 5
    assert len([record for record in renders['records'] if record['kind'] == 'exterior' and record['direction'] != 'sheet']) == 4
    raw_log = ROOT / 'forge/run/logs/latest.log'
    shutil.copyfile(raw_log, ROOT / 'qa/forge-latest.log')
    log = (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    assert 'BUILD SUCCESSFUL' in log and 'All 1 required tests passed' in log and 'ZONE_M03_FORGE_PASS' in log
    palette = sorted(static['block_counts'].items(), key=lambda entry: (-entry[1], entry[0]))
    materials = ['# zone-M03 材料', '', '统计来自最终 room.nbt，显式空气不计入实体材料。', '', '| 方块 | 数量 |', '| --- | ---: |']
    materials.extend(f'| {name} | {count} |' for name, count in palette if name != 'minecraft:air')
    (ROOT / 'MATERIALS.md').write_text('\n'.join(materials) + '\n', encoding='utf-8')
    qa = {
        'id': 'zone-M03', 'status': 'PASS', 'ready_for_unified_integration': True,
        'time': datetime.now(timezone(timedelta(hours=8))).isoformat(), 'structure_sha256': fingerprint,
        'reference_sha256': metadata['reference_sha256'], 'size': metadata['size'],
        'entrance': metadata['entrance'], 'interior': metadata['interior'], 'exit': metadata['exit'],
        'summary': {'static': 'PASS', 'records': 6900, 'non_air_blocks': static['non_air_blocks'], 'main_lane': '5 blocks wide, 4 blocks clear height, 25 blocks long', 'entrance_safe_point_exit_connected': True, 'roof': 'two complete layers, y=10 and y=11', 'detached_components': 0, 'grounded_main_frames': 4, 'grounded_extra_side_posts': 2, 'supported_lanterns': 8, 'supported_rails': 8, 'empty_barrels': 6, 'fluids_gravity_blocks_hazards_entities_commands': 0, 'forge_placements': '4/4 PASS', 'forced_chunk_ticks': 200, 'native_records_checked_after_ticks': 27600, 'world_capture_semantic_mismatches': 0, 'negative_support_probes': 8, 'negative_blocked_route_probes': 4, 'interior_views': 4, 'exterior_views': 4, 'visual_review': 'PASS'},
        'evidence': {'builder': 'builder/summary.json', 'static': 'qa/static.json', 'forge': 'qa/forge-native.json', 'forge_log': 'qa/forge-gametest.log', 'forge_raw_log': 'qa/forge-latest.log', 'capture_comparison': 'qa/capture-review.json', 'visual_review': 'qa/visual-review.json', 'render_manifest': 'qa/render-manifest.json', 'interior_sheet': 'views/interior-four-directions.png', 'exterior_sheet': 'views/exterior-four-directions.png', 'four_direction_review': 'views/four-direction-review.png', 'lodestone': 'qa/lodestone-preview.json'},
        'scope': {'deployed': False, 'production_source_modified': False, 'player_saves_touched': False, 'parallel_delivery_modified': False, 'neoforge_kubejs_resourcepack_config_modified': False, 'owned_output_root': str(ROOT), 'isolated_test_world': 'forge/run/world', 'integration_slot': None},
        'native_log_notes': ['First disposable fixture generated its own server.properties and Forge configs.', 'Forge update-check timeout, ResourceLocation/Gradle deprecation, missing framework mods.toml notices, and startup lag are retained in raw logs. The GameTest and Gradle run ended successfully.'],
        'limitations': ['Software/Lodestone renders are not Minecraft client screenshots; software face shading is not a light simulation.', 'No full modpack client, multiplayer, performance, production slot/roadway or persistent integration checks were performed.', 'Explicit air does not authorize clearing occupied terrain. The integration owner must validate the full placement volume, liquids and entities before use.', 'Reference entity minecart is represented by an empty cauldron mine hopper; no entities are included.']
    }
    write('qa.json', qa)
    readme = f'''# zone-M03 · 塌方矿道

独立地牢分区房间，尺寸 **23×12×25**，Minecraft Java **1.20.1 / DataVersion 3465**。本 ID 与 `corridor-M03` 完全独立。

四件套：`room.nbt`、`room.json`、`preview.png`、`qa.json`。

- 北入口脚位 `[11,1,0]`，内部安全点 `[11,1,4]`，南出口 `[11,1,24]`；入口五格宽、四格高。
- 北南主路 `x=9..13, z=0..24` 连通，净宽五格、净高四格。塌方仅占西侧 `x=2..7, z=13..22` 支巷。
- 四组落地木架、额外支巷护架、八盏梁链吊灯、短残轨、空矿斗、煤铁矿面、空木桶及工作台保留采矿主题。无液体、重力方块、命令方块、刷怪或预置实体。
- `y=10..11` 为完整双层屋顶；内部图隐藏屋顶与近侧外墙仅发生在渲染阶段。

## 参考和适配

参考 PNG：`{metadata['reference']}`。

参考 SHA-256：`{metadata['reference_sha256']}`。

本次使用已给定图片，无付费生图调用。概念图的斜断梁改成有石堆承托的阶梯原木；矿车用空炼药锅表现报废矿斗；塌方安排在侧巷，增加南北通路以满足接入契约。完整变化记录见 `room.json/reference_adaptations`。

## 验证证据

- 静态 `qa/static.json`：6900 坐标、完整地板/双层屋顶、五格主路、入口/安全点/出口连通、支撑和空库存均通过，非空气方块全部连至基础。
- `qa/forge-native.json`：隔离 Forge 47.4.10 GameTest 实际放置 0°/90°/180°/270° 全通过；强制区块更新 200 tick 后逐条验证 27600 条记录；无掉落实体、灯轨脱落、方块变化或库存变化。
- `qa/captures/*.nbt` 为真实世界回读，逆旋转回原始坐标；独立 Python 解码逐方块及方块实体比对为零差异，见 `qa/capture-review.json`。
- 8 次缺支撑及 4 次堵路负向探针成功被检测。Forge 结束标记及 Gradle `BUILD SUCCESSFUL` 见 `qa/forge-gametest.log`；原始日志单独保留。
- 最终 NBT 生成北南西东内部剖视与完整外壳八图，见 `views/` 和 `qa/render-manifest.json`。人工图检见 `qa/visual-review.json`。`preview.png` 为真实 NBT 原版材质软件渲染，非 AI 参考图、非实机截图。
- Lodestone Viewer 已加载原始最终 NBT，截图 `qa/lodestone-exterior.png`，加载状态与指纹记录 `qa/lodestone-preview.json`。

## 重现

1. PowerShell 运行 `tools/build.ps1`；Minecraft Builder 技能在其安装目录运行，原版 gzip NBT 往返校验。
2. 使用带 Pillow 的 Python 运行 `tools/inspect_static.py` 和 `tools/render_views.py`。
3. 运行 `tools/run_native.ps1`；仅使用 `forge/run` 内的一次性 GameTest 世界。
4. 检查八张图后更新图检记录，再运行 `tools/finalize.py`。

最终 `room.nbt` SHA-256：`{fingerprint}`。

状态：**PASS，可交统一接入负责人审阅；未部署、未分配正式槽位。** 未修改正式 JAR、源码、玩家存档、NeoForge、KubeJS、资源包或其它房间。没有执行整合包客户端、多人、性能或正式接入道路/持久化测试。正式施工前须验证整个体积占用、液体及实体；NBT 中显式空气不是清场授权。
'''
    (ROOT / 'README.md').write_text(readme, encoding='utf-8')
    required = ['room.nbt', 'room.json', 'preview.png', 'qa.json']
    evidence = required + ['README.md', 'MATERIALS.md', 'builder/summary.json', 'qa/static.json', 'qa/forge-native.json', 'qa/forge-gametest.log', 'qa/forge-latest.log', 'qa/capture-review.json', 'qa/render-manifest.json', 'qa/visual-review.json', 'qa/lodestone-preview.json', 'qa/lodestone-exterior.png'] + image_paths + [entry['path'] for entry in captures]
    entries = [{'path': name, 'sha256': digest(ROOT / name), 'bytes': (ROOT / name).stat().st_size} for name in sorted(set(evidence))]
    write('delivery-manifest.json', {'id': 'zone-M03', 'status': 'PASS', 'required_four': required, 'structure_sha256': fingerprint, 'reference_sha256': metadata['reference_sha256'], 'files': entries, 'deployed': False})
    print(json.dumps({'id': 'zone-M03', 'status': 'PASS', 'sha256': fingerprint, 'manifest_files': len(entries), 'captures': len(captures)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
