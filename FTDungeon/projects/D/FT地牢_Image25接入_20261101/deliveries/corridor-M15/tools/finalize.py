import hashlib
import json
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
FONT = 'C:/Windows/Fonts/msyh.ttc'


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def read(path):
    return json.loads(path.read_text(encoding='utf-8'))


def write(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def make_sheet(kind, labels):
    sheet = Image.new('RGB', (2000, 1500), '#11171c')
    draw = ImageDraw.Draw(sheet)
    draw.text((50, 32), f'M15 · 苔蚀黑石断拱烛廊 | {"四向外观" if kind == "exterior" else "四向内部"}', font=ImageFont.truetype(FONT, 34), fill='#eadfbe')
    subtitle = '实际 room.nbt · Minecraft Builder / Lodestone 渲染 · 非实机截图'
    if kind == 'interior':
        subtitle += ' · 东西为移除近侧墙与屋顶的展示剖切'
    draw.text((50, 88), subtitle, font=ImageFont.truetype(FONT, 21), fill='#9eafa8')
    for index, (direction, label) in enumerate(labels):
        left, top = 40 + (index % 2) * 980, 145 + (index // 2) * 655
        image = Image.open(ROOT / 'views' / f'{direction}-{kind}.png').convert('RGB')
        image.thumbnail((950, 594), Image.Resampling.LANCZOS)
        sheet.paste(image, (left, top + 44))
        draw.text((left + 10, top + 3), label, font=ImageFont.truetype(FONT, 27), fill='#d8e1d2')
    draw.text((50, 1460), '15 × 12 × 40 · Z 轴一字贯通 · 净空 5 × 5 · 北入口 [7,1,0] → 南出口 [7,1,39]', font=ImageFont.truetype(FONT, 20), fill='#a2b29d')
    sheet.save(ROOT / f'four-view-{kind}.png')


def main():
    fingerprint = digest(ROOT / 'room.nbt')
    static = read(ROOT / 'qa/static-qa.json')
    native = read(ROOT / 'qa/native-world-qa.json')
    registry = read(ROOT / 'qa/native-registry-qa.json')
    renders = read(ROOT / 'qa/view-render-qa.json')
    for report in (static, native, registry, renders):
        assert report['status'] == 'PASS' and report['sha256'] == fingerprint, 'All evidence must pass against the current NBT'
    assert len(native['rotations']) == 4 and native['stable_ticks'] >= 200
    assert all(result['fixture_rollback'] == 'PASS' and result['after_200_ticks']['status'] == 'PASS' for result in native['rotations'])
    native_log = (ROOT / 'evidence/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    assert 'M15_NATIVE_QA_PASS' in native_log and 'BUILD SUCCESSFUL' in native_log
    for kind in ('exterior', 'interior'):
        make_sheet(kind, [('north', '北面 / North'), ('south', '南面 / South'), ('east', '东面 / East'), ('west', '西面 / West')])
    static.update({'status': 'PASS', 'native': {'status': 'PASS', 'registry': 'qa/native-registry-qa.json', 'placement_stability': 'qa/native-world-qa.json', 'log': 'evidence/forge-gametest.log', 'rotations': 4, 'stable_ticks': native['stable_ticks'], 'records_per_snapshot': native['native_records_per_snapshot']}, 'views': {'status': 'PASS', 'count': 8, 'report': 'qa/view-render-qa.json', 'interior_sheet': 'four-view-interior.png', 'exterior_sheet': 'four-view-exterior.png'}, 'limits': ['Not a full modpack client or multiplayer playtest.', 'Not deployed, no production integration and no player save access.', 'East/west interior images are view-only cutaways; final NBT roof and long side walls remain complete.', 'Lodestone preview uses presentation lighting and does not simulate flame particles or exact Forge light transport.']})
    write(ROOT / 'qa.json', static)
    output_files = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'materials.json', 'four-view-exterior.png', 'four-view-interior.png', 'qa/static-qa.json', 'qa/native-world-qa.json', 'qa/native-registry-qa.json', 'qa/view-render-qa.json', 'evidence/forge-gametest.log']
    output_files.extend(f'views/{view["name"]}.png' for view in renders['views'])
    output_files.extend(['tools/build-m15.mjs', 'tools/static-qa.py', 'tools/render-views.mjs', 'tools/finalize.py', 'tools/nbtio.py', 'README.md', 'Run-QA.ps1', 'native-test/build.gradle', 'native-test/settings.gradle', 'native-test/gradle.properties', 'native-test/src/main/java/net/farmingtales/m15qa/M15PlacementTests.java', 'native-test/src/main/java/net/farmingtales/m15qa/M15Qa.java', 'native-test/src/main/resources/META-INF/mods.toml', 'native-test/src/main/resources/pack.mcmeta', 'native-test/src/main/resources/data/m15qa/structures/empty.nbt', 'evidence/forge-gametest-attempt1-missing-fixture.log', 'evidence/forge-gametest-attempt2-namespace.log'])
    manifest = {'id': 'corridor-M15', 'status': 'PASS', 'generated_at': datetime.now(ZoneInfo('Asia/Shanghai')).isoformat(), 'structure_sha256': fingerprint, 'reference_sha256': static['checks']['reference_sha256']['detail'], 'deployed': False, 'player_saves_accessed': False, 'files': [{'path': filename, 'bytes': (ROOT / filename).stat().st_size, 'sha256': digest(ROOT / filename)} for filename in output_files]}
    write(ROOT / 'manifest.json', manifest)
    print(json.dumps({'status': 'PASS', 'sha256': fingerprint, 'manifest_files': len(output_files), 'forge_rotations': 4, 'stable_ticks': 200}, ensure_ascii=False))


if __name__ == '__main__':
    main()
