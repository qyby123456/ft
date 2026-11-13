import collections
import hashlib
import json
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parent
GROUP = ROOT.parents[1]
TOOLS = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
SKILL = Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill')
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S2-sandstone-desert/out/S2-01-large-chamber.png')
sys.path.insert(0, str(TOOLS))
import review_four_views as views
sys.path.insert(0, str(GROUP))
from authority import NbtReader


def save(path, content):
    path.write_text(json.dumps(content, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    target = ROOT / 'incoming/IMG-S2-01'
    target.mkdir(parents=True, exist_ok=True)
    result = subprocess.run(['node', str(SKILL / 'scripts/run-build.mjs'), str(ROOT / 'recipe.mjs'), '--out-dir', str(target), '--name', 'room', '--data-version', '3465'], cwd=SKILL, capture_output=True, text=True, encoding='utf-8')
    (ROOT / 'build.log').write_text(result.stdout + result.stderr, encoding='utf-8')
    result.check_returncode()
    digest = hashlib.sha256((target / 'room.nbt').read_bytes()).hexdigest()
    connections = [{'facing': direction, 'position': position, 'width': 5, 'height': 3, 'coordinate_system': 'local XYZ feet; north=-Z'} for direction, position in [('north', [11, 1, 0]), ('south', [11, 1, 22]), ('west', [0, 1, 11]), ('east', [22, 1, 11])]]
    metadata = {'id': 'IMG-S2-01', 'name': '砂岩阶梯日轮祭台厅·按图R3', 'size': [23, 13, 23], 'entrance': [11, 1, 0], 'interior': [11, 1, 3], 'facing': 'north', 'connections': connections, 'reference': str(REFERENCE), 'reference_sha256': hashlib.sha256(REFERENCE.read_bytes()).hexdigest(), 'sha256': digest, 'roof': 'enclosed; removed in render only', 'altar_feet': [11, 3, 10], 'ring_route_waypoints': [[4, 1, 11], [18, 1, 11], [11, 1, 4], [11, 1, 18]], 'authority': 'Reference image controls layout and appearance; unpacked Mod data is format compatibility only', 'analysis': str(ROOT / 'REFERENCE_ANALYSIS.md'), 'supersedes_for_this_image': str(GROUP / 'incoming/NAT1-G2-01/room.nbt'), 'intentional_deviations': ['23x13x23 is an estimated build grid, not a measured image dimension.', 'Closed roof retained for final dungeon safety; image cutaway only used for preview.', 'North entry is level with FT floor instead of exterior image stairs.', 'Flower pot over terracotta approximates irregular concept pottery.', 'No obelisk, raised gallery, gold cap or redstone added.'], 'world_placement': 'NOT_RUN', 'runtime_slot': None}
    save(target / 'room.json', metadata)
    structure = views.Structure.read(target / 'room.nbt')
    assert tuple(structure.size) == (23, 13, 23)
    materials = collections.Counter(block.name for block in structure.blocks.values() if block.name != 'minecraft:air')
    assert not any(materials.get('minecraft:' + material) for material in ['gold_block', 'spawner', 'water', 'lava', 'fire', 'redstone_lamp', 'chest'])
    for blockX in range(23):
        for blockZ in range(23):
            assert structure.blocks[(blockX, 12, blockZ)].name == 'minecraft:cut_sandstone'
    for position, block in structure.blocks.items():
        if block.name in {'minecraft:lantern', 'minecraft:flower_pot', 'minecraft:dead_bush', 'minecraft:sand'}:
            below = (position[0], position[1] - 1, position[2])
            assert structure.blocks[below].name != 'minecraft:air'
    assert materials['minecraft:lantern'] == 20
    save(target / 'materials.json', dict(materials))
    save(target / 'qa.json', {'status': 'STATIC_PASS_NATIVE_PENDING', 'sha256': digest, 'full_roof': True, 'supported_decorations': True, 'landmark_lamps': 4, 'wall_lamps': 16, 'no_old_obelisk_or_gallery': True, 'one_reference_only': str(REFERENCE), 'native_world_placement': 'NOT_RUN'})
    for exterior in (False, True):
        mode = 'exterior' if exterior else 'interior'
        sheet = views.Image.new('RGB', (2400, 1800), '#101a20')
        for index, (direction, title, camera) in enumerate(views.VIEWS):
            image = views.render(structure, metadata, camera, title, exterior, size=(1200, 900))
            image.save(target / f'{mode}-{direction}.png')
            sheet.paste(image, ((index % 2) * 1200, (index // 2) * 900))
            if index == 0 and not exterior:
                image.save(target / 'preview.png')
        sheet.save(target / f'{mode}-four-views.png')
    print('ONLY S2-01 BUILT AND RENDERED ' + digest)


if __name__ == '__main__':
    main()
