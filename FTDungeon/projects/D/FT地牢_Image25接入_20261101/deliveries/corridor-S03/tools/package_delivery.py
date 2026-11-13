import argparse
import json
import shutil
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont, ImageOps

from qa_static import ROOT, REFERENCE, REFERENCE_HASH, digest, dump


BACKGROUND = '#101d1c'
PANEL = '#172a27'
TEXT = '#e3ede3'
MUTED = '#a6c1b6'
ACCENT = '#90d6b4'


def font(size):
    return ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', size)


def paste_fit(canvas, path, box):
    image = Image.open(path).convert('RGB')
    image = ImageOps.contain(image, (box[2], box[3]), Image.Resampling.LANCZOS)
    canvas.paste(image, (box[0] + (box[2] - image.width) // 2, box[1] + (box[3] - image.height) // 2))


def compose():
    canvas = Image.new('RGB', (1920, 1280), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((56, 34), 'S03  /  蔓生废墟 · 魂灯吊笼短廊', font=font(44), fill=TEXT)
    draw.text((60, 103), 'SHORT CORRIDOR     11 × 10 × 16     JAVA 1.20.1     INDEPENDENT DELIVERY', font=font(23), fill=ACCENT)
    draw.line((56, 154, 1864, 154), fill='#38574b', width=2)
    paste_fit(canvas, ROOT / 'qa/views/interior.png', (48, 187, 1280, 880))
    draw.rounded_rectangle((1360, 187, 1868, 1067), radius=15, fill=PANEL)
    draw.text((1396, 218), '一字贯通 / 16 格长', font=font(31), fill=TEXT)
    draw.text((1396, 279), '北口 (5, 1, 0)', font=font(25), fill=ACCENT)
    draw.text((1396, 324), '南口 (5, 1, 15)', font=font(25), fill=ACCENT)
    draw.text((1396, 392), '3 格宽 × 3 格高净通路', font=font(25), fill=TEXT)
    draw.text((1396, 438), '两端 5 格宽 × 4 格高开放口', font=font(24), fill=MUTED)
    draw.text((1396, 522), '8 盏灵魂灯 / 3 组空吊笼', font=font(25), fill=TEXT)
    draw.text((1396, 568), '苔石、裂砖、藤蔓与深色木梁', font=font(24), fill=MUTED)
    draw.text((1396, 650), '完整屋顶 · 无岔路 · 无通行楼梯', font=font(23), fill=TEXT)
    draw.text((1396, 695), '无实体 / 无容器 / 无流体危险', font=font(23), fill=MUTED)
    draw.line((1396, 758, 1830, 758), fill='#38574b', width=2)
    draw.text((1396, 795), '实际 NBT / Lodestone 软件渲染', font=font(23), fill=ACCENT)
    draw.text((1396, 844), '主图未剖切，未用 AI 图替代成品', font=font(22), fill=MUTED)
    draw.text((1396, 903), 'Forge 隔离放置证据见 qa.json', font=font(23), fill=TEXT)
    draw.text((1396, 951), '不等于整合包客户端游玩验收', font=font(22), fill=MUTED)
    draw.text((56, 1104), '原图末端关闭门改为开放口，以保证南北直通。剖切仅用于另附四视图，不改交付结构。', font=font(26), fill=TEXT)
    draw.text((56, 1160), '独立模板 · 未部署 · 未访问玩家存档    |    QA: 静态 / 四视图 / 原生四旋转 / 200 tick', font=font(24), fill=ACCENT)
    draw.text((56, 1211), 'NBT SHA-256  ' + digest(ROOT / 'room.nbt'), font=font(21), fill=MUTED)
    canvas.save(ROOT / 'preview.png')

    sheet = Image.new('RGB', (2200, 1740), BACKGROUND)
    draw = ImageDraw.Draw(sheet)
    draw.text((52, 31), 'S03  /  四视图 · 同一交付 NBT', font=font(44), fill=TEXT)
    draw.text((54, 100), '北 / 南：完整结构；西 / 东：仅在渲染副本中移除近侧双层墙与屋顶。实体 NBT 保持完整。', font=font(25), fill=MUTED)
    entries = [('north', 'N / 北面 · 完整结构'), ('south', 'S / 南面 · 完整结构'), ('west', 'W / 西面 · 墙顶剖切'), ('east', 'E / 东面 · 墙顶剖切')]
    for index, (name, label) in enumerate(entries):
        left = 40 + index % 2 * 1090
        top = 160 + index // 2 * 750
        draw.rounded_rectangle((left, top, left + 1060, top + 718), radius=13, fill=PANEL)
        draw.text((left + 23, top + 18), label, font=font(29), fill=ACCENT)
        paste_fit(sheet, ROOT / f'qa/views/{name}.png', (left + 15, top + 67, 1030, 635))
    draw.text((53, 1687), 'Lodestone 0.6.0 · WebGL 软件结构预览，非 Minecraft 客户端截图  |  ' + digest(ROOT / 'room.nbt')[:24] + '…', font=font(22), fill=MUTED)
    sheet.save(ROOT / 'qa/four-views.png')


def finalize():
    fingerprint = digest(ROOT / 'room.nbt')
    reports = {}
    for name in ['static-qa', 'native-registry-qa', 'native-world-qa', 'preview-provenance', 'visual-review']:
        report = json.loads((ROOT / f'qa/{name}.json').read_text(encoding='utf-8'))
        assert report['status'] == 'PASS', name
        assert report['nbt_sha256'] == fingerprint, name
        reports[name] = report
    native = reports['native-world-qa']
    assert len(native['rotations']) == 4 and native['stable_ticks'] == 200
    assert all(entry['status'] == 'PASS' and entry['after_200_ticks']['status'] == 'PASS' and entry['fixture_rollback'] == 'PASS' for entry in native['rotations'])
    assert 'S03_NATIVE_QA_PASS' in (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    assert digest(REFERENCE) == REFERENCE_HASH
    for name in ['preview.png', 'qa/four-views.png']:
        with Image.open(ROOT / name) as image:
            image.verify()
    reference_copy = ROOT / 'evidence/reference.png'
    reference_copy.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(REFERENCE, reference_copy)
    batch = REFERENCE.parents[1]
    provenance = {'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH, 'copy': 'evidence/reference.png', 'copied_sha256': digest(reference_copy), 'source_documents': {name: digest(batch / name) for name in ['README.md', 'manifest.json', 'SPEC.md', 'out/corridor_index.json', 'prompts/S03-overgrown-ruins-overgrown.txt']}, 'paid_image_api_called': False}
    dump(ROOT / 'evidence/reference-provenance.json', provenance)
    qa = {
        'id': 'corridor-S03', 'status': 'PASS', 'nbt_sha256': fingerprint, 'reference_sha256': REFERENCE_HASH,
        'size': [11, 10, 16], 'length_blocks': 16, 'records': 1760, 'non_air_blocks': 1181, 'native_palette_states': 26,
        'checks': {'static': 'PASS', 'four_view_visual_review': 'PASS', 'native_1_20_1_registry_properties_and_roundtrip': 'PASS', 'forge_47_4_10_actual_placement_four_rotations': 'PASS', 'native_block_survival': 'PASS', 'three_wide_player_collision_route_both_directions': 'PASS', 'complete_floor_and_roof': 'PASS', 'post_neighbor_updates_and_200_ticks': 'PASS', 'four_explicit_vine_random_tick_sweeps': 'PASS', 'initially_empty_fixture_restoration': 'PASS', 'reference_fingerprint': 'PASS'},
        'evidence': {name: f'qa/{name}.json' for name in reports},
        'visuals': {'preview': 'preview.png', 'four_views': 'qa/four-views.png', 'raw_views': 'qa/views/'},
        'native_log': 'qa/forge-gametest.log', 'stability_ticks': 200, 'rotations': ['NONE', 'CLOCKWISE_90', 'CLOCKWISE_180', 'COUNTERCLOCKWISE_90'],
        'vine_growth_note': '原生随机刻允许藤蔓自然生长或面状态更新；四方向分别有1/4/3/6处自然变化，无结构方块损失或改动。每次复测变化数可不同。',
        'scope_limits': ['软件 WebGL 四视图不是 Minecraft 客户端截图。', 'Forge 原生隔离 GameTest 不是完整整合包客户端、多人与性能验收。', '未接入 FT 路网、未修改核心源码、未部署模组、未访问玩家存档。', '模板含显式空气；此 QA 不提供生产覆盖权限，接入前必须拒绝整块体积的占用、流体与实体。'],
        'player_worlds_opened': False, 'production_integration_tested': False, 'client_gameplay_tested': False, 'deployed': False,
    }
    dump(ROOT / 'qa.json', qa)
    counts = json.loads((ROOT / 'qa/materials.json').read_text(encoding='utf-8'))['counts']
    table = ['# S03 材料表', '', '| 方块 | 数量 |', '| --- | ---: |']
    table.extend(f'| `{name}` | {count} |' for name, count in counts.items())
    table.extend(['', '1760 条全体积记录；1181 个非空气方块，579 条显式空气。', '吊笼为空装饰：12 个铁栏杆 + 24 个铁活板门 + 3 条锚定锁链；不含实体或战利品。', '实际原版碰撞与支撑形状见 `qa/native-registry-qa.json`。', ''])
    (ROOT / 'MATERIALS.md').write_text('\n'.join(table), encoding='utf-8')
    excluded_parts = {'.gradle', '__pycache__', 'build', 'run'}
    files = sorted(path for path in ROOT.rglob('*') if path.is_file() and not any(part in excluded_parts for part in path.relative_to(ROOT).parts) and path.name not in {'delivery-manifest.json', 'corridor-S03-independent.zip'})
    manifest = {'id': 'corridor-S03', 'status': 'PASS', 'nbt_sha256': fingerprint, 'core_files': ['room.nbt', 'room.json', 'preview.png', 'qa.json'], 'deployment': 'not_deployed', 'files': [{'path': path.relative_to(ROOT).as_posix(), 'bytes': path.stat().st_size, 'sha256': digest(path)} for path in files]}
    dump(ROOT / 'delivery-manifest.json', manifest)
    archive_path = ROOT / 'corridor-S03-independent.zip'
    with zipfile.ZipFile(archive_path, 'w', zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
        for path in [*files, ROOT / 'delivery-manifest.json']:
            archive.write(path, path.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(archive_path) as archive:
        assert archive.testzip() is None
        assert all(name in archive.namelist() for name in manifest['core_files'])
    print(json.dumps({'status': 'PASS', 'nbt_sha256': fingerprint, 'zip': str(archive_path), 'zip_sha256': digest(archive_path), 'files': len(files) + 1}, ensure_ascii=False))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--compose-only', action='store_true')
    arguments = parser.parse_args()
    compose()
    if not arguments.compose_only:
        finalize()


if __name__ == '__main__':
    main()
