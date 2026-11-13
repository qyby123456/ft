import hashlib
import html
import json
import os
import shutil
import sys
import zipfile
from pathlib import Path
from xml.etree import ElementTree

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(Path(os.environ['APPDATA']) / '.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools'))
from nbtio import Structure, plain

ROTATIONS = ['none', 'clockwise_90', 'clockwise_180', 'counterclockwise_90']
DIRECTIONS = ['north', 'east', 'south', 'west']


def load(path):
    return json.loads(path.read_text(encoding='utf-8'))


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def record(path):
    return {'path': str(path.resolve()), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest(), 'bytes': path.stat().st_size}


def captures(folder):
    source = Structure.read(folder / 'room.nbt')
    width, height, depth = source.size
    results = []
    for turns, rotation in enumerate(ROTATIONS):
        capture_path = folder / ('native-placed-' + rotation + '.nbt')
        capture = Structure.read(capture_path)
        assert len(capture.blocks) == len(source.blocks) and not capture.entities
        for point, state in source.blocks.items():
            across, vertical, forward = point
            target = [(across, vertical, forward), (depth - 1 - forward, vertical, across), (width - 1 - across, vertical, depth - 1 - forward), (forward, vertical, width - 1 - across)][turns]
            properties = dict(state.properties)
            if properties.get('facing') in DIRECTIONS:
                properties['facing'] = DIRECTIONS[(DIRECTIONS.index(properties['facing']) + turns) % 4]
            if properties.get('axis') in ['x', 'z'] and turns % 2:
                properties['axis'] = 'z' if properties['axis'] == 'x' else 'x'
            for step in range(turns):
                previous = properties.copy()
                for index, direction in enumerate(DIRECTIONS):
                    if direction in previous: properties[DIRECTIONS[(index + 1) % 4]] = previous[direction]
            actual = capture.blocks.get(target)
            assert actual is not None and actual.name == state.name and dict(actual.properties) == properties, str((folder.name, rotation, point, state, actual))
            if point in source.block_entities:
                saved = plain(capture.block_entities[target])
                assert saved['id'] == plain(source.block_entities[point])['id'] and not saved.get('Items') and not saved.get('LootTable')
            else:
                assert target not in capture.block_entities
        results.append({'rotation': rotation, **record(capture_path), 'matches_rotated_final_nbt': True})
    return results


def main():
    group = load(ROOT / 'group.json')
    tests = list(ElementTree.parse(ROOT / 'native-tests.xml').iter('testcase'))
    assert len(tests) == 36 and all(not any(test.find(tag) is not None for tag in ['failure', 'error', 'skipped']) for test in tests)
    assert 'BUILD SUCCESSFUL' in (ROOT / 'qa/native-console.log').read_text(encoding='utf-8')
    audit = load(ROOT / 'audit/summary.json')
    assert not audit['current_group_high_similarity'] and not audit['errors']
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 24)
    small = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 17)
    preview_root = ROOT / 'preview'
    preview_root.mkdir(exist_ok=True)
    overview = Image.new('RGB', (1800, 1580), '#101720')
    overview_draw = ImageDraw.Draw(overview)
    overview_draw.text((28, 18), '苔藓古林 · 第一批第一组 · 9 个实际 Minecraft NBT 模块', font=font, fill='#e5eddc')
    overview_draw.text((28, 57), '真实解包输入适配 / Lodestone 软件预览 / 非游戏截图 / 未部署 / 全量跨组终审待完成', font=small, fill='#a8baa1')
    cards = []
    rows = []
    packaged = []
    for index, item in enumerate(group['rooms']):
        folder = ROOT / item['id']
        metadata = load(folder / 'room.json')
        fingerprint = record(folder / 'room.nbt')['sha256']
        assert fingerprint == metadata['sha256'] == item['sha256']
        render = load(folder / 'qa/render.json')
        assert render['source_nbt_sha256'] == fingerprint and render['source_nbt_unchanged']
        native_reports = []
        for rotation in ROTATIONS:
            path = folder / ('forge-' + rotation + '.json')
            report = load(path)
            assert report['status'] == 'PASS' and report['source_nbt_sha256'] == fingerprint and report['stabilization_ticks'] == 400
            native_reports.append(record(path))
        captured = captures(folder)
        view_sheet = Image.new('RGB', (1600, 700), '#101720')
        draw = ImageDraw.Draw(view_sheet)
        for view_index, view in enumerate([entry for entry in render['views'] if entry['id'] != 'hero']):
            path = folder / view['file']
            assert record(path)['sha256'] == view['sha256']
            with Image.open(path) as image:
                image.load()
                image.thumbnail((400, 300))
                left, top = view_index % 4 * 400, view_index // 4 * 350 + 35
                view_sheet.paste(image.convert('RGB'), (left, top))
                draw.text((left + 8, top - 29), view['id'], font=small, fill='white')
        view_sheet.save(folder / 'views/eight-views.png')
        left, top = index % 3 * 600, index // 3 * 490 + 105
        with Image.open(folder / 'preview.png') as image:
            image.load()
            image.thumbnail((590, 425))
            overview.paste(image.convert('RGB'), (left + 5, top))
        overview_draw.text((left + 18, top + 427), f'{metadata["number"]} {metadata["name"]}  ' + '×'.join(map(str, metadata['size'])), font=font, fill='#e5eddc')
        overview_draw.text((left + 18, top + 462), '/'.join(port['side'] for port in metadata['ports']), font=small, fill='#9db48f')
        qa = {'status': 'PRODUCER_PASS_RECEIVER_AND_GLOBAL_AUDIT_PENDING', 'id': item['id'], 'sha256': fingerprint, 'static': load(folder / 'qa/static.json'), 'native': {'status': 'PASS', 'rotations': [0, 90, 180, 270], 'stability_ticks': 400, 'reports': native_reports, 'captures': captured, 'random_tick_speed': 0, 'fluid_ticks_forced': True, 'player_saves_accessed': False}, 'render': render, 'diversity': {'status': 'CURRENT_SNAPSHOT_NO_HIGH_PAIR_NOT_FINAL', 'summary': record(ROOT / 'audit/summary.json')}, 'limits': ['No actual Minecraft client or multiplayer playtest.', 'No FT hall integration, occupied-volume refusal or idempotence test in this delivery.', 'No puzzle, enemy wave or loot runtime is implemented.', 'Global final diversity audit awaits all groups; static proxies do not replace experience/sightline review.'], 'ready_for_unified_integration': False, 'deployed': False}
        save(folder / 'qa.json', qa)
        destination = ROOT / 'delivery' / item['id']
        destination.mkdir(parents=True, exist_ok=True)
        for name in ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'evidence.json', 'spec.json']:
            shutil.copyfile(folder / name, destination / name)
        for name in ['qa/static.json', 'qa/render.json', 'views/eight-views.png', *[view['file'] for view in render['views'] if view['file'] != 'preview.png'], *['forge-' + rotation + '.json' for rotation in ROTATIONS], *['native-placed-' + rotation + '.nbt' for rotation in ROTATIONS]]:
            target = destination / name
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(folder / name, target)
        packaged.append({'id': item['id'], 'directory': str(destination), 'sha256': fingerprint, 'producer_pass': True, 'ready_for_unified_integration': False})
        rows.append(f'| {item["id"]} | {item["name"]} | {"×".join(map(str, metadata["size"]))} | {"/".join(port["side"] for port in metadata["ports"])} | `{fingerprint}` |')
        cards.append(f'<article><h2>{html.escape(metadata["number"] + " " + metadata["name"])}</h2><img src="{item["id"]}/preview.png"><p>{" × ".join(map(str, metadata["size"]))} · feetY=2 · 4 rotations / 400 tick</p><a href="{item["id"]}/views/eight-views.png">八视图</a> · <a href="{item["id"]}/room.nbt">room.nbt</a> · <a href="{item["id"]}/qa.json">QA</a></article>')
    overview.save(preview_root / 'group-overview.png')
    page = '<!doctype html><html lang="zh-CN"><meta charset="utf-8"><title>苔藓古林实际地牢 NBT</title><style>body{background:#101720;color:#e5eddc;font:16px system-ui;margin:28px}main{display:grid;grid-template-columns:repeat(3,minmax(250px,1fr));gap:16px}article{background:#17212d;padding:14px;border-radius:12px}img{width:100%}a{color:#a9d98a}p{color:#aabb9c}h2{font-size:18px}@media(max-width:900px){main{grid-template-columns:1fr}}</style><h1>苔藓古林 · 第一批第一组</h1><p>实际 Java 1.20.1 NBT · 9个模块 · 制作方静态QA及36项四向原生放置通过 · Lodestone软件预览非游戏截图</p><p>尺寸与模块语义依据解包 Mod 的真实输入；连接端口为显式 FT 适配。未部署，接收方复验及所有线程完成后的跨组终审仍待执行。</p><main>' + ''.join(cards) + '</main></html>'
    (ROOT / 'index.html').write_text(page, encoding='utf-8')
    lines = ['# 苔藓古林 · 第一批第一组（实际 NBT 交付候选）', '', '当前聊天直接制作，未另派线程，未调用付费图片生成。首批 S1-01～S1-09 对应9个结构，S1-10仅为整体构图参考，不冒充第10间房。', '', '## 权威依据', '', '- 先读 `evidence/INPUTS.md` 和 `evidence/input-manifest.json`：9个真实 Mod NBT、12份总结/池/处理器/结构/代码文档及源JAR SHA-256。', '- 原始解包 NBT 已逐字节对照源JAR。程序尺寸由测量值及显式适配规则计算；不复制原建筑布局，不声称兼容原YUNG运行时。', '- 风格图仅为苔石、根须、荧光藤的次要主题参考，不能替代真实输入。', '', '## 交付模块', '', '| ID | 名称 | XYZ尺寸 | 出口 | NBT SHA-256 |', '|---|---|---|---|---|', *rows, '', '## 放置和连接', '', '- Minecraft Java 1.20.1，DataVersion=3465，gzip单调色板结构；只有原版方块。', '- 坐标是模板原点相对XYZ；北为-Z，入口为北侧中央，feetY=2，下方地板Y=1。每个开口3宽3高且干燥，安全点见room.json。', '- 连接时仅配对相反朝向端口；将两个端口脚位对齐到同一世界高度，以3宽3高、有实底和顶盖的连接件跨越间隙。', '- 原生结构旋转基点是[0,0,0]而非房间中心；旋转后重算包围盒和端口，禁止假定原点位置不变。', '- 未使用端口由接入方显式封口；不能把彼此独立模板直接重叠粘贴。', '- 施工前检查整个包围体积的占用、实体和液体，不自动清空既有世界。', '- 只有庭院中央7×7为声明的天井；其他模块屋顶完整，剖切只影响预览。', '', '## 真实验证', '', '- 9/9 静态校验；36/36 Minecraft 1.20.1 Forge 47.4.10 GameTests，每方向400 tick；强制流体/邻居更新，检查精确状态、支撑、容器可达、空库存、入口路线、屋顶和边界泄漏。', '- 四向实际世界回捕NBT逐方块对照最终NBT；所有36份回捕与对应旋转状态一致。', '- 所有模块以最终NBT经Lodestone渲染：主预览＋北南西东内部及完整外壳，不是实机截图。', '- 修正了树叶距离传播及门庭石墙邻接状态；旧失败运行日志保留qa/native-attempt*.log，不掩盖失败历史。', '- randomTickSpeed=0：未验证长期随机生长。未做客户端/多人游玩、FT大厅接通、占用拒绝或持久幂等测试。', '', '## 重复度门槛', '', f'- 当前快照比较{audit["rooms"]}个已交付NBT、{audit["pairs"]}对，输出audit/similarity-matrix.csv和成对分项；本组未触发高重复阈值。', '- 泉室采用直通主路＋横支与隔间，宝殿采用前厅→门洞→终端高台，避免复用同一环路体验。', '- 这不是所有线程完成后的最终跨组验收；仍需补审完整拓扑骨架、轮廓、机关/谜题、波次、射线视线、地标与战利品运行时。高度同构必须返工，换皮无效。', '', '## 状态与边界', '', '- 制作方PASS；统一ID/槽位未分配，接收方四视图及复验待做，不得标记已统一接入。', '- 不写正式存档、JAR、资源包或配置；没有部署，也没有实现战斗波次或奖励分发。', '- 领取目录：delivery/；可复现脚本：source/；证据：evidence/、qa/、audit/。', '- 仅限当前用户的本地工程使用；不将原模组素材再分发授权作扩大解释。', '']
    (ROOT / 'README.md').write_text('\n'.join(lines), encoding='utf-8')
    save(ROOT / 'delivery/manifest.json', {'group': group['id'], 'group_fingerprint': group['group_fingerprint'], 'producer_status': 'PASS', 'native_tests': 36, 'native_stability_ticks': 400, 'rooms': packaged, 'receiver_status': 'PENDING', 'global_final_diversity': 'PENDING', 'deployed': False})
    archive = ROOT / 'nature-b1-s1-vanilla-nbt-candidate.zip'
    with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED) as stream:
        for directory in ['delivery', 'source', 'evidence', 'audit']:
            for path in sorted((ROOT / directory).rglob('*')):
                if path.is_file() and '__pycache__' not in path.parts: stream.write(path, path.relative_to(ROOT).as_posix())
        for path in [ROOT / 'README.md', ROOT / 'group.json', ROOT / 'native-tests.xml', ROOT / 'qa/native-console.log', preview_root / 'group-overview.png']:
            stream.write(path, path.relative_to(ROOT).as_posix())
    save(ROOT / 'delivery-summary.json', {'producer_status': 'PASS', 'rooms': 9, 'native_tests': 36, 'stable_ticks': 400, 'capture_comparisons': 36, 'package': record(archive), 'input_manifest': record(ROOT / 'evidence/input-manifest.json'), 'receiver_status': 'PENDING', 'global_final_diversity': 'PENDING', 'deployed': False})
    print('FINALIZED', archive, record(archive)['sha256'])


if __name__ == '__main__':
    main()
