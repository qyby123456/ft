import json
import shutil
from datetime import datetime, timedelta, timezone

from build_room import REFERENCE_HASH, ROOT, digest, write_json


def main():
    metadata = json.loads((ROOT / 'out/room.json').read_text(encoding='utf-8'))
    static = json.loads((ROOT / 'qa/static.json').read_text(encoding='utf-8'))
    native = json.loads((ROOT / 'qa/native.json').read_text(encoding='utf-8'))
    renders = json.loads((ROOT / 'qa/render-manifest.json').read_text(encoding='utf-8'))
    captures = json.loads((ROOT / 'qa/captures.json').read_text(encoding='utf-8'))
    fingerprint = digest(ROOT / 'out/room.nbt')
    assert fingerprint == metadata['sha256'] == static['structure_sha256'] == native['structure_sha256'] == renders['source_nbt_sha256']
    assert all(report['status'] == 'PASS' for report in (static, native, renders, captures))
    assert captures['structure_sha256'] == fingerprint
    assert len(native['rotations']) == 4 and native['records_after_ticks'] == 78732 and native['stabilization_ticks'] == 200
    log = (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    assert 'BUILD SUCCESSFUL' in log and 'SPIRAL_TOWER_NATIVE_PASS rotations=4/4 records=78732 ticks=200 source=' + fingerprint in log
    route_reports = [route for placement in native['rotations'] for route in placement['routes']]
    headroom = {name: min(route['min_headroom_blocks'] for route in route_reports if route['route'] == name) for name in ('spiral_up', 'straight_down')}
    timestamp = datetime.now(timezone(timedelta(hours=8))).isoformat(timespec='seconds')
    report = {'id': 'STAIR_B', 'name': metadata['name'], 'status': 'PASS', 'generated_at': timestamp, 'timezone': 'Asia/Shanghai', 'structure_sha256': fingerprint, 'reference_sha256': REFERENCE_HASH, 'deployment': 'NOT_DEPLOYED', 'scope': 'Standalone authoring and isolated Forge QA only; no production save, instance mods, shared FT core, other author folders or API settings written.', 'format': {'minecraft': 'Java 1.20.1', 'forge': '47.4.10', 'size': metadata['size'], 'DataVersion': 3465, 'single_palette_gzip': True, 'explicit_records': 19683, 'entities': 0, 'empty_barrels': 2}, 'checks': static['checks'], 'native': {'status': 'PASS', 'rotations': 4, 'records_per_stage': 78732, 'stages': ['immediate', 'after_200_ticks'], 'stabilization_ticks': 200, 'native_registry_properties_roundtrip': 'PASS', 'all_record_states_unchanged': 'PASS', 'can_survive_all_records': 'PASS', 'three_lane_treads_checked_per_rotation': 57, 'clear_tread_width_blocks': 3, 'player_collision_box': [0.6, 1.8], 'route_sampling_interval_blocks': 0.125, 'minimum_headroom_blocks': headroom, 'maximum_measured_step_blocks': max(route['max_step_blocks'] for route in route_reports), 'routes_both_directions_per_rotation': 4, 'route_samples_per_stage': sum(route['samples'] for route in route_reports), 'negative_probes': {'missing_tread_support': native['negative_missing_tread_support'], 'blocked_spiral_route': native['negative_blocked_spiral_route'], 'missing_lantern_chain': native['negative_missing_lantern_chain']}, 'forced_chunks': native['fixture_chunks_forced'], 'world_y': native['fixture_origin_world_y'], 'random_tick_speed': 0, 'neighbor_scheduled_ticks': 'ENABLED'}, 'ports': metadata['ports'], 'evidence': {'static': 'qa/static.json', 'native': 'qa/native.json', 'log': 'qa/forge-gametest.log', 'render_manifest': 'qa/render-manifest.json', 'blueprint': 'out/blueprint.json', 'native_snapshots': ['qa/native-placed-' + suffix + '.nbt' for suffix in ('none', 'clockwise_90', 'clockwise_180', 'counterclockwise_90')], 'preview': 'preview/four-views.png', 'exterior': 'preview/exterior.png', 'floor_plans': 'preview/plans.png'}, 'history': ['Initial terrain-level fixture passed immediate four-way routes, but failed stabilization due to natural gravel falling into template air.', 'Test fixture moved to isolated world Y=180 with forced chunks; baseline passed full four-way 200-tick QA.', 'Final version extends pier to foundation, adds four wall-bearing ribs and inner corner protection, then reruns static/native/render audits. Previous reports and baseline retained under qa.'], 'limitations': native['limitations'] + renders['limitations'] + ['The three ports are independent authoring interfaces, not connected FT runtime corridors. Main entrance feet Y=8 requires an explicit +7 floor-offset adapter; do not assume template Y=0 floor.', 'No new monster/loot/trade/trigger behavior or production deployment.', 'Software visual inspection is not a live full-modpack player walkthrough.']}
    report['evidence']['capture_state_audit'] = 'qa/captures.json'
    report['native']['world_snapshot_records_independently_compared'] = captures['records_checked']
    report['visual_review'] = {'status': 'PASS', 'views_reviewed': ['preview/four-views.png', 'preview/exterior.png', 'preview/lower-floor.png'], 'observations': ['Continuous four-flight spiral and upper threshold are visible across the four complementary cutaways.', 'Lower straight stair, solid side cheeks, floor opening guards and two-level interfaces are retained.', 'Complete roof and outer shell are present in exterior view; render cutaways do not alter exported NBT.'], 'evidence_level': 'Software NBT preview inspection, not a game client screenshot.'}
    for filename in ('room.nbt', 'room.json', 'preview.png'):
        shutil.copy2(ROOT / 'out' / filename, ROOT / filename)
    write_json(ROOT / 'out/qa.json', report)
    shutil.copy2(ROOT / 'out/qa.json', ROOT / 'qa.json')
    readme = f'''# B · 螺旋楼梯塔 · 独立交付

生成时间：{timestamp}（Asia/Shanghai）。仅本独立目录写入；**未部署，未写玩家存档、正式实例或共享 FT 核心**。

## 正式四件

- 根目录 room.nbt、room.json、preview.png、qa.json 为正式交付；out/ 内保留相同文件的制作版本。
- Minecraft Java 1.20.1 / Forge 47.4.10；27×27×27，DataVersion=3465，gzip 单一调色板，19,683 条显式坐标（含空气）。
- NBT SHA-256：{fingerprint}
- 参考 PNG SHA-256：{REFERENCE_HASH}（已核验 1536×1024；未再次调用生图 API）。

## 结构与原图转译

八角近圆石砖塔、贯穿地下至屋顶的八角中柱、绕柱一周四转折上行梯道、独立直行下行梯段、红毯上层门台、外圈扶壁/承托肋、铁栏、壁灯、双吊链灯、顶角蛛网、两只空木桶与木箱点缀。原图连续圆弧按原版整数方块转译为方形环绕螺旋，不冒充逐像素复刻。

- 上行 4 段，每段升 3 格，累计升 12 格；下行直梯降 7 格；每段台阶和转角平台净宽 3 格。
- 36 块上行台阶 + 21 块下行台阶；每块有完整底承托；四个外墙承托肋将梯台连向塔壁，中柱直达地基。
- 梯台外缘、下行洞口与中柱切角有护栏/实心挡边，不封住梯道入口；上下门均保持开放，不放关闭门阻路。
- 非通道高处的蛛网不进入主路。两只桶为空；无实体、刷怪笼、战利品、命令/结构/拼图方块，无水火熔岩。
- 最终 NBT 保留完整屋顶、塔壁与地基，只有声明的三个接口开口。剖切仅在预览内存中操作。

## 三接口与 FT 接入注意

| 接口 | 玩家脚部坐标 | 方向 | 净开口 |
| --- | --- | --- | --- |
| 主层入口 | (13,8,0) | 北 | 3宽×3高 |
| 上层出口 | (13,20,0) | 北 | 3宽×3高 |
| 下层出口 | (13,1,26) | 南 | 3宽×3高 |

内部安全点为 (13,8,3)。主层脚高 +8，意味着入口地板在模板 Y=7；**不能套用入口脚高 +1 的平面房槽位**。由唯一核心负责人显式配置原点高度与上下层走廊。先核验整模板体积（包含显式空气）及边界/实体/液体占用；占用就拒绝，禁止清空正式场地。三个边界口需要外部走廊连接，不是已接入可游玩的 FT 路线。未测试目录注册、核心幂等、回城门、五档难度或多人流程。

## 真实 QA 证据

- 静态：单调色板/版本/指纹/坐标/空库存、4,352 个外壳单元、三口 3×3、全部台阶底承托、连续落地支撑图、外墙承托肋、护栏、中柱和吊链锚点通过。
- 原生：Forge 隔离 GameTest 4/4 旋转；每阶段 78,732 条方块状态逐项核对；立即与 200 tick 后两阶段均通过；检查原生注册表属性、模板读写、全部方块 canSurvive 和空方块实体。
- 通行：原生 0.6×1.8 AABB、1/8 格采样、最大步高限制 0.6；上下路线往返各一次/旋转；实测最大步高 0.5，上行最小净高 {headroom['spiral_up']}，下行最小净高 {headroom['straight_down']}。不是用两格空气的简单连通性冒充楼梯碰撞。
- 负例：每向均能检测移除台阶承托、堵塞螺旋路线、拆除灯下第一段吊链，共12项。
- 稳定性：隔离世界 Y=180，强制加载测试区；邻居更新和调度 tick 开启，随机 tick 关闭。初次地形落砾干扰失败如实保留，未掩盖。
- preview/ 包含真实 NBT 四视角、完整外壳、地下层、三层平面和原生回采预览。它们是原版模型/贴图软件渲染，**不是客户端实机截图或动态光/FPS验收**。
- 原始证据：qa/static.json、qa/native.json、qa/forge-gametest.log、qa/native-placed-*.nbt、qa/render-manifest.json。qa/baseline-before-support-refinement/ 为非最终基线。

## 重现

使用已安装且带 Pillow 的 Python 依序执行 tools/build_room.py、tools/qa_static.py、tools/run_native.py、tools/render_room.py、tools/audit_snapshots.py、tools/finalize_delivery.py。原生脚本默认使用本机 Java 17 和已缓存 Gradle 8.8/Forge 依赖；仅运行此工程的 qa/forge-run/ 世界。独立 QA 模组绝不能安装到玩家实例。render_engine.py、nbtio.py、run_native.py 的初始工具代码从已完成的 H 房间工程只读复用；渲染器改动只在本目录。未改 API 配置、共享工具或并行交付。
'''
    (ROOT / 'README.md').write_text(readme, encoding='utf-8')
    files = [ROOT / filename for filename in ('room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md')]
    files += list((ROOT / 'preview').glob('*.png'))
    files += [ROOT / relative for relative in ('qa/static.json', 'qa/native.json', 'qa/captures.json', 'qa/forge-gametest.log', 'qa/render-manifest.json', 'out/blueprint.json')]
    files += list((ROOT / 'qa').glob('native-placed-*.nbt'))
    files += list((ROOT / 'tools').glob('*.py'))
    files += list((ROOT / 'src').rglob('*.java'))
    manifest = {'id': 'STAIR_B', 'status': 'PASS', 'generated_at': timestamp, 'files': [{'file': path.relative_to(ROOT).as_posix(), 'bytes': path.stat().st_size, 'sha256': digest(path)} for path in files]}
    write_json(ROOT / 'manifest.json', manifest)
    (ROOT / 'SHA256SUMS.txt').write_text(''.join(entry['sha256'] + '  ' + entry['file'] + '\n' for entry in manifest['files']), encoding='utf-8')
    print(json.dumps({'status': 'DELIVERED_NOT_DEPLOYED', 'sha256': fingerprint, 'files': len(files), 'headroom': headroom}, ensure_ascii=False))


if __name__ == '__main__':
    main()
