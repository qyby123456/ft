import collections
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import subprocess
import sys

from PIL import Image

ROOT = Path(__file__).resolve().parent
PROJECT = Path('D:/ft工程文件/FT地牢_Image25接入_20261101')
SKILL = Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill')
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S6-volcanic-cavern/out/S6-01-large-chamber.png')
sys.path.insert(0, 'D:/ft工程文件/血棘祭炉祭坛厅地牢_20261101/tools')
from structure_io import load_structure


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def run(command, **options):
    result = subprocess.run(command, capture_output=True, text=True, encoding='utf-8', errors='replace', creationflags=subprocess.CREATE_NO_WINDOW, **options)
    if result.returncode:
        raise RuntimeError(result.stdout + result.stderr)
    return result.stdout + result.stderr


def build():
    assert ROOT.name == 'S6-01-large-chamber-r3'
    analysis = ROOT / 'REFERENCE_ANALYSIS.md'
    assert analysis.exists() and analysis.read_text(encoding='utf-8')
    with Image.open(REFERENCE) as image:
        image.verify()
    print(run(['node', str(SKILL / 'scripts/run-build.mjs'), str(ROOT / 'recipe.mjs'), '--out-dir', str(ROOT), '--name', 'room', '--data-version', '3465'], cwd=SKILL), flush=True)
    metadata = {
        'id': 'NAT6-01-R3', 'name': '火山熔岩大殿·中央祭台',
        'revision': 'S6-01-R3-image-first-single', 'size': [19, 12, 19],
        'entrance': [9, 2, 0], 'interior': [9, 2, 2], 'facing': 'north',
        'sha256': digest(ROOT / 'room.nbt'), 'reference': str(REFERENCE),
        'reference_sha256': digest(REFERENCE), 'analysis_path': str(analysis), 'analysis_sha256': digest(analysis),
        'connectors': [
            {'position': [9, 2, 0], 'facing': 'north', 'width': 3, 'height': 3, 'role': 'entrance restored from cutaway'},
            {'position': [5, 2, 18], 'facing': 'south', 'width': 3, 'height': 3, 'role': 'visible rear left arch'},
            {'position': [13, 2, 18], 'facing': 'south', 'width': 3, 'height': 3, 'role': 'visible rear right arch'},
        ],
        'validation_targets': [[5, 2, 18], [13, 2, 18], [5, 2, 9], [13, 2, 9], [9, 2, 5], [9, 2, 14], [9, 4, 7]],
        'topology': 'central terraced altar with low continuous circulation on all four sides; two rear arches, one restored north entrance',
        'landmarks': ['cut-corner 7x7 base and 5x5 upper altar', 'uneven obsidian peaks and front-view enclosed lava core', 'left/right enclosed lava falls', 'four corner columns with glowstone belts', 'four roof-supported chain lanterns'],
        'image_drives': ['19x19 footprint and 10-12 height estimate from single-image composition and its prompt', 'central altar instead of R2 longitudinal spine', 'perimeter lava and two visible rear arches'],
        'compatibility_only': ['Java 1.20.1 DataVersion3465', 'vanilla states checked against native registry', 'FT 3x3 north opening and full roof except cutaway previews'],
        'adaptations': ['lava fully enclosed behind orange glass; no exposed magma walking surface', 'hidden north entrance and complete roof reconstructed', 'no particles, smoke, mobs, waves, rewards or invented mechanism'],
        'workflow': 'one explicitly selected reference only; no S6-02 work in this revision', 'deployment': 'NOT_PERFORMED',
    }
    save(ROOT / 'room.json', metadata)
    template = load_structure(ROOT / 'room.nbt')
    assert template['size'] == [19, 12, 19] and template['DataVersion'] == 3465 and not template.get('entities')
    palette = template['palette']
    states = {tuple(record['pos']): palette[record['state']] for record in template['blocks']}
    assert len(states) == len(template['blocks']) == 19 * 12 * 19
    assert all(all(0 <= position[axis] < template['size'][axis] for axis in range(3)) for position in states)
    names = {position: state['Name'].removeprefix('minecraft:') for position, state in states.items()}
    assert all(state['Name'].startswith('minecraft:') for state in palette)
    assert not set(names.values()) & {'magma_block', 'spawner', 'command_block', 'structure_block', 'jigsaw', 'fire', 'campfire', 'lever'}
    assert not any(record.get('nbt', {}).get('Items') or record.get('nbt', {}).get('LootTable') for record in template['blocks'])
    def solid(position):
        return position in names and names[position] not in {'air', 'lava', 'water', 'chain', 'lantern'}
    def walk(position):
        blockX, blockY, blockZ = position
        return names.get(position) == 'air' and names.get((blockX, blockY + 1, blockZ)) == 'air' and solid((blockX, blockY - 1, blockZ))
    start = tuple(metadata['entrance'])
    reached = {start}
    pending = collections.deque([start])
    while pending:
        blockX, blockY, blockZ = pending.popleft()
        for deltaX, deltaZ in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            for deltaY in (0, 1, -1):
                following = (blockX + deltaX, blockY + deltaY, blockZ + deltaZ)
                if following not in reached and walk(following):
                    reached.add(following)
                    pending.append(following)
    assert walk(start)
    assert tuple(metadata['interior']) in reached and all(tuple(point) in reached for point in metadata['validation_targets'])
    for connector in metadata['connectors']:
        blockX, blockY, blockZ = connector['position']
        assert all(names[(blockX + horizontal, blockY + vertical, blockZ)] == 'air' for horizontal in (-1, 0, 1) for vertical in range(3))
        assert all(solid((blockX + horizontal, blockY - 1, blockZ)) for horizontal in (-1, 0, 1))
    lava = [point for point, name in names.items() if name == 'lava']
    for blockX, blockY, blockZ in lava:
        for deltaX, deltaY, deltaZ in ((1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1), (0, 1, 0), (0, -1, 0)):
            adjacent = (blockX + deltaX, blockY + deltaY, blockZ + deltaZ)
            assert solid(adjacent) or names.get(adjacent) == 'lava', ('unsealed lava', (blockX, blockY, blockZ), adjacent)
    lanterns = [point for point, name in names.items() if name == 'lantern']
    assert len(lanterns) == 4
    for blockX, blockY, blockZ in lanterns:
        assert states[(blockX, blockY, blockZ)]['Properties']['hanging'] == 'true'
        assert all(names[(blockX, chainY, blockZ)] == 'chain' for chainY in range(blockY + 1, 11))
        assert solid((blockX, 11, blockZ))
    assert all(solid((blockX, 11, blockZ)) for blockX in range(19) for blockZ in range(19))
    save(ROOT / 'materials.json', dict(sorted(collections.Counter(state['Name'] for state in states.values()).items())))
    save(ROOT / 'topology.json', {'source_nbt_sha256': metadata['sha256'], 'reachable_feet_positions': sorted(reached), 'targets': metadata['validation_targets'], 'image_route': metadata['topology'], 'not_from_r2_spine_template': True})
    save(ROOT / 'qa.json', {'status': 'PASS', 'sha256': metadata['sha256'], 'checks': ['19x12x19 dimensions', 'unique full explicit volume', 'vanilla-only/no entities/loot', 'all 3x3 connectors dry and clear', 'all four altar circulation sides reachable', 'upper altar stair approach reachable', 'six-side lava containment', 'four lanterns chain-supported to roof', 'complete actual roof', 'single reference only'], 'sealed_lava_cells': len(lava), 'forge_placement': 'NOT_RUN', 'forge_tick_stability': 'NOT_RUN'})


def native():
    target = ROOT / 'qa'
    target.mkdir(exist_ok=True)
    support = Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt')
    dependencies = [line for line in support.read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]
    classpath = os.pathsep.join(['D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar', *dependencies])
    classes = target / 'native-classes'
    classes.mkdir(exist_ok=True)
    java = Path('C:/Program Files/Common Files/Oracle/Java/javapath')
    output = run([str(java / 'javac.exe'), '-encoding', 'UTF-8', '-classpath', classpath, '-d', str(classes), str(PROJECT / 'tools/VerifyDeliveries.java'), str(ROOT / 'NativeOne.java')], timeout=90)
    (target / 'native-compile.log').write_text(output, encoding='utf-8')
    output = run([str(java / 'java.exe'), '-Djava.awt.headless=true', '-classpath', str(classes) + os.pathsep + classpath, 'NativeOne', str(ROOT)], cwd=ROOT, timeout=120)
    (target / 'native-run.log').write_text(output, encoding='utf-8')
    print(output[-1200:], flush=True)


def render():
    specification = importlib.util.spec_from_file_location('fourviews', PROJECT / 'tools/review_four_views.py')
    renderer = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(renderer)
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    structure = renderer.Structure.read(ROOT / 'room.nbt')
    views = ROOT / 'views'
    views.mkdir(exist_ok=True)
    for exterior in (False, True):
        sheet = Image.new('RGB', (2400, 1800), '#12141b')
        for index, (direction, title, camera) in enumerate(renderer.VIEWS):
            image = renderer.render(structure, metadata, camera, title, exterior, size=(1200, 900))
            image.save(views / f'{"exterior" if exterior else "interior"}-{direction}.png')
            sheet.paste(image, ((index % 2) * 1200, (index // 2) * 900))
            if not exterior and index == 0:
                image.save(ROOT / 'preview.png')
        sheet.save(views / f'{"exterior" if exterior else "interior"}-four-views.png')
    print('RENDERED single image structure in four interior and four exterior directions')


def finalize():
    import urllib.request
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    native_report = json.loads((ROOT / 'qa/native-review.json').read_text(encoding='utf-8'))
    assert native_report['status'] == 'PASS' and native_report['room']['sha256'] == digest(ROOT / 'room.nbt') == metadata['sha256']
    assert len(native_report['targets']) == 7 and len(list((ROOT / 'views').glob('*.png'))) == 10
    files = [ROOT / name for name in ['room.nbt', 'room.json', 'materials.json', 'qa.json', 'topology.json', 'preview.png', 'REFERENCE_ANALYSIS.md', 'qa/native-review.json']] + sorted((ROOT / 'views').glob('*.png'))
    for path in files:
        if path.suffix == '.png':
            with Image.open(path) as image:
                image.verify()
    with urllib.request.urlopen('http://127.0.0.1:5256/__minecraft_builder__/build.nbt', timeout=10) as response:
        assert hashlib.sha256(response.read()).hexdigest() == metadata['sha256']
    save(ROOT / 'manifest.json', {'status': 'PASS_STATIC_AND_NATIVE', 'revision': metadata['revision'], 'reference_count': 1, 'structure_count': 1, 'reference': str(REFERENCE), 'reference_sha256': digest(REFERENCE), 'analysis_sha256': digest(ROOT / 'REFERENCE_ANALYSIS.md'), 'nbt_sha256': metadata['sha256'], 'lodestone_url': 'http://127.0.0.1:5256/?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=S6-01-R3', 'lodestone_serves_final_nbt': True, 'files': [{'path': str(path.relative_to(ROOT)), 'sha256': digest(path)} for path in files], 'next_reference_started': False, 'forge_world_placement': 'NOT_RUN', 'forge_tick_stability': 'NOT_RUN', 'deployment': 'NOT_PERFORMED', 'older_batch': 'R1/R2 are retained legacy candidates, not this image-first revision'})
    print('PASS: one image -> one NBT; 7 native path targets; 11 decoded preview PNGs; final Lodestone SHA256 verified')


if __name__ == '__main__':
    {'build': build, 'native': native, 'render': render, 'finalize': finalize}[sys.argv[1]]()
