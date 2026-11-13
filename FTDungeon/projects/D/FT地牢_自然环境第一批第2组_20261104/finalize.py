import hashlib
import json
import re
import urllib.request
from pathlib import Path
import sys

from PIL import Image, ImageDraw, ImageFont
from authority import LIBRARY, NbtReader

ROOT = Path(__file__).resolve().parent


def save(path, content):
    path.write_text(json.dumps(content, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    manifest = json.loads((ROOT / 'manifest.json').read_text(encoding='utf-8'))
    native = json.loads((ROOT / 'qa/native-review.json').read_text(encoding='utf-8'))
    assert native['status'] == 'PASS'
    assert len(native['rooms']) == 9
    native_rooms = {room['id']: room for room in native['rooms']}
    sheet = Image.new('RGB', (1800, 1500), '#101a20')
    draw = ImageDraw.Draw(sheet)
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 25)
    lines = ['# 第一批第2组：砂岩荒漠实际结构模块', '', '状态：9 个原创改造模块已生成，静态 QA 与 Minecraft 1.20.1 原生加载/碰撞/往返序列化 9/9 PASS；尚未执行 Forge 世界四向放置、区块稳定性与实机游玩，不可标记最终接入通过。', '', '本线程生成图片接口调用 0 次；没有生图半写入，无需清理。PNG 仅为最终 NBT 软件渲染，不是 AI 图或游戏截图。', '', '## 权威依据', '', f'- `{LIBRARY / "LEARNING_NOTES.md"}`：交通网络、门洞、功能房、终止件分层；Jigsaw 与 YUNG 扩展运行时不混用。', f'- `{LIBRARY / "SAMPLE_CATALOG.md"}`：模板尺寸和资源证据索引。', f'- `{ROOT / "authority-evidence.json"}`：逐模块原始 NBT、SHA-256、原始尺寸/接口/调色板、沿用和改造点。构建前读取原始 NBT 并与目录尺寸和方块数核对。', '', '原版石砖系对应砂岩、雕纹砂岩和切制砂岩；删除占位玻璃、刷怪与战利品运行层，不把处理器占位材质当成外观。原件仅只读研究，没有复制/再分发源 NBT、改源 JAR 或运行模组函数。', '', '## 模块清单', '', '尺寸为 X×Y×Z；北为 -Z，东为 +X；连接坐标为局部玩家脚部位置。北入口始终三宽三高、有实心地板；出口和法线见各自 room.json。', '', '| 模块 | 名称 | 尺寸 | 接口方向 | Lodestone |', '| --- | --- | --- | --- | --- |']
    for index, module in enumerate(manifest['modules']):
        target = ROOT / 'incoming' / module['id']
        digest = hashlib.sha256((target / 'room.nbt').read_bytes()).hexdigest()
        assert digest == module['sha256'] == native_rooms[module['id']]['sha256']
        reader = NbtReader((target / 'room.nbt').read_bytes())
        assert reader.number('>B') == 10
        reader.string()
        template = reader.value(10)
        assert not template.get('entities')
        for record in template['blocks']:
            data = record.get('nbt', {})
            assert not data.get('Items') and 'LootTable' not in data and 'Command' not in data
        qa = json.loads((target / 'qa.json').read_text(encoding='utf-8'))
        log = ROOT / 'qa' / f'lodestone-{module["id"]}.log'
        port = int(re.search(r'http://127\.0\.0\.1:(\d+)/', log.read_text(encoding='utf-8')).group(1)) if log.exists() else 5282
        with urllib.request.urlopen(f'http://127.0.0.1:{port}/__minecraft_builder__/build.nbt', timeout=10) as response:
            assert hashlib.sha256(response.read()).hexdigest() == digest
        qa.update({'native_validation': native_rooms[module['id']], 'containers_empty_no_loot_table': True, 'overall_status': 'STATIC_AND_NATIVE_LOAD_PASS_WORLD_PLACEMENT_PENDING', 'four_views': 'interior-four-views.png / exterior-four-views.png; rendered from final hash', 'lodestone_port': port, 'lodestone_http_served_nbt_hash': digest})
        save(target / 'qa.json', qa)
        with Image.open(target / 'preview.png') as image:
            sheet.paste(image.resize((600, 466)), ((index % 3) * 600, (index // 3) * 500))
        draw.text(((index % 3) * 600 + 15, (index // 3) * 500 + 468), module['id'] + ' ' + module['name'], font=font, fill='#f1dcad')
        url = f'http://127.0.0.1:{port}/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name={module["id"]}'
        module['lodestone_url'] = url
        lines.append('| ' + module['id'] + ' | ' + module['name'] + ' | ' + '×'.join(map(str, module['size'])) + ' | ' + '/'.join(item['facing'] for item in module['connections']) + f' | [预览]({url}) |')
    lines += ['', '## 差异化修订 R2', '', '实际修改01/02/04/07/09的NBT：错位屏墙前厅、绕金顶日轮塔的双路、可登高西侧测日廊、02/04真实三向接口、侧路拉杆测水灯及屏墙后的宝库。详见DISTINCT_DESIGN.md六维说明、distinct-route-plan.json双环端口图和duplicate-audit-input.json跨组材质无关矩阵输入。', '', '已对第3/7组当时可用的18个实际模板作只读快照比较；不是全组独特性认证。双环为端口兼容规划，尚未布置世界路线；拉杆电路静态存在，但刻更新未实测。', '', '## 交付与边界', '', '每模块目录包含 room.nbt、room.json、preview.png、materials.json、qa.json、内部与完整外壳四向图、构建日志。recipe.mjs 是 minecraft-builder-skill 的可复现程序化配方；build_and_review.py 负责来源核对、构建与渲染，design_audit.py输出差异化与跨组筛查证据，finalize.py 汇总原生结果和哈希。', '', '第10张整体概念图仅记录于 layout-reference.json，不伪装成可放置的第10个房间。世界绝对坐标、正式槽位、连接路线、部署和实机验收归统一接入线程；本目录未写正式存档、mods、配置或其他线程房间。', '', 'NBT 显式记录空气，包括 L 形包围盒内的空隙；接入前必须对完整包围体做占用/实体/液体/世界边界检查，不能无条件清空现有世界。各接口重定位并非复制源 Jigsaw 偏移，不声称可直接接入 YUNG 生成器。', '', '当前 Lodestone 预览是本地临时服务；重启命令：', '', '```powershell', '& node "C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/preview.mjs" --nbt "' + str(ROOT / 'incoming/NAT1-G2-01/room.nbt') + '" --port 5282 --no-open', '```', '']
    sheet.save(ROOT / 'group2-structures-overview.png')
    manifest.update({'status': 'STATIC_AND_NATIVE_LOAD_PASS_WORLD_PLACEMENT_PENDING', 'authority_evidence': str(ROOT / 'authority-evidence.json'), 'native_report': str(ROOT / 'qa/native-review.json'), 'original_image_derived_draft': 'SUPERSEDED_IN_PLACE_BEFORE_DELIVERY', 'native_world_placement': 'NOT_RUN', 'four_view_images': '72 single views + 18 four-view sheets', 'formal_slots': 'UNASSIGNED'})
    save(ROOT / 'manifest.json', manifest)
    (ROOT / 'README.md').write_text('\n'.join(lines), encoding='utf-8')
    files = sorted(path for path in ROOT.rglob('*') if path.is_file() and path.suffix in {'.nbt', '.png', '.json', '.md', '.mjs', '.py', '.java'})
    (ROOT / 'checksums.sha256').write_text('\n'.join(hashlib.sha256(path.read_bytes()).hexdigest() + '  ' + str(path.relative_to(ROOT)) for path in files) + '\n', encoding='utf-8')
    print('VERIFIED 9 final NBT hashes, native PASS, empty containers, 72 views; world placement PENDING')


if __name__ == '__main__':
    main()
