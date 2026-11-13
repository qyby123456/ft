import json
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image

from build_room import REFERENCE_HASH, digest, write_json
from nbtio import Structure


ROOT = Path(__file__).resolve().parents[1]


def load(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def main():
    metadata = load('room.json')
    static = load('qa/static.json')
    native = load('qa/native.json')
    render = load('qa/render-manifest.json')
    fingerprint = digest(ROOT / 'room.nbt')
    assert static['status'] == native['status'] == render['status'] == 'PASS'
    assert fingerprint == metadata['sha256'] == static['structure_sha256'] == native['structure_sha256'] == render['source_nbt_sha256']
    assert len(native['rotations']) == 4 and {value['rotation'] for value in native['rotations']} == {'NONE', 'CLOCKWISE_90', 'CLOCKWISE_180', 'COUNTERCLOCKWISE_90'}
    assert native['stabilization_ticks'] == 100 and native['records_checked_after_ticks'] == 12716
    assert native['forced_ticking_chunks'] > 0 and native['random_tick_speed'] == 3
    assert all(value['status'] == 'PASS' and value['records_checked'] == 3179 and value['block_entities_checked'] == 2
               and value['unexpected_entities'] == 0 and value['roof_cells'] == 289 for value in native['rotations'])
    assert len(render['native_captures']) == 4
    log = (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    assert 'R5_STONEMASON_NATIVE_PASS:' in log and 'BUILD SUCCESSFUL' in log
    assert digest(ROOT / 'preview.png') == render['preview_sha256']
    assert Image.open(ROOT / 'preview.png').size == (1600, 1250)
    for result in native['rotations']:
        assert digest(ROOT / result['world_capture']) == result['world_capture_sha256']
        capture = Structure.read(ROOT / result['world_capture'])
        assert capture.size == (17, 11, 17) and len(capture.blocks) == 3179 and len(capture.block_entities) == 2
    source = Structure.read(ROOT / 'room.nbt')
    capture = Structure.read(ROOT / 'qa/native-placed-none.nbt')
    assert source.blocks == capture.blocks
    stamp = datetime.now(timezone(timedelta(hours=8))).isoformat(timespec='seconds')
    write_json(ROOT / 'qa.json', {
        'id': 'R5', 'name': '石匠工坊', 'status': 'PASS', 'timestamp': stamp,
        'structure_sha256': fingerprint, 'reference_sha256': REFERENCE_HASH, 'size': metadata['size'],
        'static': static,
        'native': native,
        'render': render,
        'final_audit': {'status': 'PASS', 'identity_rotation_capture_block_states_equal_source': True,
                        'all_four_native_capture_hashes_verified': True, 'native_success_log_verified': True,
                        'preview_dimensions': [1600, 1250], 'preview_sha256': digest(ROOT / 'preview.png')},
        'evidence': {relative: {'sha256': digest(ROOT / relative), 'bytes': (ROOT / relative).stat().st_size}
                     for relative in ['qa/static.json', 'qa/native.json', 'qa/forge-gametest.log', 'qa/render-manifest.json']},
        'deployment': {'status': 'NOT_DEPLOYED', 'ft_core_modified': False, 'player_save_opened': False,
                       'other_rooms_modified': False, 'image_api_called': False},
        'limitations': ['原生隔离验收不等于整合包客户端游玩、多人测试或FPS结论。',
                        '没有修改FT目录槽位、大厅连通、幂等、占用拒绝或部署；由唯一核心负责人另行验收。',
                        '概念图采用原版方块近似：磨石作车轮、墙饰作石翼兽、拉杆作工具；房间尺寸因安全动线调整。',
                        '预览仅软件内存剖切，最终结构屋顶完整；两桶无库存，炼药锅水为块内静态内容，无游离流体。'],
    })
    primary = ['room.nbt', 'room.json', 'preview.png', 'qa.json']
    hashes = {relative: digest(ROOT / relative) for relative in primary}
    hash_table = '\n'.join(f'| `{relative}` | `{fingerprint}` |' for relative, fingerprint in hashes.items())
    readme = f'''# R5 石匠工坊 · 独立交付

状态：静态 QA、Minecraft 1.20.1 / Forge 47.4.10 / Java 17 原生四向实际放置与 100 tick 稳定性 QA 全部通过。**未部署、未改核心、未打开正式玩家存档。**

## 交付
- `room.nbt`：gzip / 单一 palette / DataVersion 3465，17×11×17，完整记录 3179 格（含显式空气），非空气 {static['non_air_blocks']} 格。
- `room.json`：R5，北入口脚位 `(8,1,0)`，内部安全脚位 `(8,1,4)`；北口净空 3×3。
- `preview.png`：实际 NBT 与原版 1.20.1 模型、纹理软件渲染，1600×1250；不是概念图或客户端截图。
- `qa.json`：汇总静态、原生、回采和预览指纹证据。

## 造型与支撑
未完工人形石像和四柱脚手架、贴实木背板的梯子；四腿雕刻工作台、切石机与磨石；落在双层石座上的石翼兽；地面手推石料车；贴墙工具板与石料搁架；两只空木桶、块内静态水缸和两盏天花木梁固定的链灯。中央 X=7…9、Z=0…15 连续三格宽、三格高，不穿家具。工作台、石料、石像座、脚手架柱、梯背、链灯锚点均有专门支撑断言。

## 原图
`{metadata['reference']}`，1536×1024。

SHA-256：`{REFERENCE_HASH}`。原图只读，副本位于 `reference/`；没有重新调用付费生图接口。设计约 13×15×13 并非坐标蓝图；本次采用 17×11×17 保留主题和净空。全原版方块近似，不声称逐像素还原；工具用拉杆/按钮、车轮用磨石、石翼兽用楼梯/墙饰表达。

## 验收证据
- `qa/static.json`：NBT 版本/范围/唯一性、完整屋顶地板各289格、连续主路、工作站访问、全部非空气块地面连通、空容器、无实体/奖励/危险块/游离流体、确定性重建。
- `qa/native.json`：4/4 旋转 `NONE/CLOCKWISE_90/CLOCKWISE_180/COUNTERCLOCKWISE_90`，每向原生注册表与属性、方块实体、实际放置状态、碰撞、canSurvive、入口与内部脚位往返、工作站路径通过。
- 各向立即放置与强制加载 {native['forced_ticking_chunks']} 个区块、默认 randomTickSpeed=3、邻块更新后100 tick再次检查；最终12716格全部一致，每向2个空方块实体、0库存、0掉落/意外实体，{native['rotations'][0]['reachable_foot_positions']}个可达脚位。
- 17项反例：每向拆工作台腿、拆灯链、堵入口、挖屋顶均被检测，另1次人为放入钻石的库存反例被拒绝。
- `qa/native-placed-*.nbt`：四向100 tick后从真实隔离世界回采；零旋转3179个方块状态与交付逐项相等。
- `qa/forge-gametest.log`：包含 `R5_STONEMASON_NATIVE_PASS` 与 `BUILD SUCCESSFUL`。测试夹具只允许 `GameTestServer`，不用于正式实例。
- `preview/four-views.png`、`preview/exterior.png`、`preview/plan.png`、`preview/native-placed-*.png`：四视角、完整屋顶、脚部平面及回采预览。剖切仅作用于渲染内存。

## 修正历史
首轮原生测试如实失败：石翼兽两侧圆石墙角的自动连边从none变low。生成器改为与真实邻接一致的状态后重建。第二轮复用测试世界，清理旧夹具产生了2个按钮掉落物，被实体断言拒绝。修正为每次时间戳命名的全新隔离世界、Y=240空中夹具、初始化不触发邻块形状掉落；不是放置后清除掉落物掩盖问题。两轮报告、日志保存在 `qa/previous-*`，最终重新运行全套四向与稳定性验收。最终结果以当前指纹和当前 `qa/native.json` 为准。

## 复现
使用带Pillow的Python依次执行 `tools/build_room.py`、`tools/qa_static.py`、`tools/run_native.py`、`tools/render_room.py`、`tools/finalize_delivery.py`。原生工具使用已缓存Gradle8.8与Java17，仅启动工程 `qa/forge-runs/<时间戳>/` 的独立无界面Forge GameTestServer，不启动玩家客户端。原生复现后须重新渲染再汇总。

## 并行边界与限制
只写本目录，外部仅只读参考图/既有工具/原版缓存；没有修改正式mod、FT核心、共享目录、接入表或任何其它房间。构建缓存为Gradle正常行为。测试夹具不是发行模组，不安装。尚未执行FT大厅连接、目录槽位、幂等/占用拒绝、生产部署、整合包客户端/多人和FPS测试，不能据此声称正式接入完成。100 tick是有界稳定性证据而非长期运行保证。

## 最终 SHA-256
| 文件 | SHA-256 |
| --- | --- |
{hash_table}

完整文件哈希与大小另见 `manifest.json`。交付时间：{stamp}（Asia/Shanghai）。
'''
    (ROOT / 'README.md').write_text(readme, encoding='utf-8')
    evidence = primary + ['README.md', 'qa/static.json', 'qa/native.json', 'qa/forge-gametest.log', 'qa/render-manifest.json', 'qa/blueprint.json', 'preview/four-views.png', 'preview/exterior.png', 'preview/plan.png']
    evidence += [str(path.relative_to(ROOT)) for path in sorted((ROOT / 'qa').glob('native-placed-*.nbt'))]
    write_json(ROOT / 'manifest.json', {
        'id': 'R5', 'name': '石匠工坊', 'status': 'PASS', 'root': str(ROOT), 'timestamp': stamp,
        'size': metadata['size'], 'entrance': metadata['entrance'], 'interior': metadata['interior'],
        'reference_sha256': REFERENCE_HASH, 'deployment': 'NOT_DEPLOYED',
        'files': {relative: {'sha256': digest(ROOT / relative), 'bytes': (ROOT / relative).stat().st_size} for relative in evidence},
    })
    print(json.dumps({'status': 'PASS', 'root': str(ROOT), 'sha256': hashes, 'manifest_sha256': digest(ROOT / 'manifest.json')}, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
