import collections
import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
TARGET = ROOT / 'incoming/G2B-01'
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S2-sandstone-desert/out/S2-01-large-chamber.png')
sys.path.insert(0, 'D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
import review_four_views as views

BASE_FILTER = views.filtered


def clear_near_shell(structure, metadata, camera, exterior):
    visible = BASE_FILTER(structure, metadata, camera, exterior)
    if exterior:
        return visible
    width, height, depth = structure.size
    return {position: block for position, block in visible.items() if position[1] == 0 or not ((position[0] <= 2 if camera[0] < 0 else position[0] >= width - 3) or (position[2] <= 2 if camera[1] < 0 else position[2] >= depth - 3))}


views.filtered = clear_near_shell


def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def mask(path):
    structure = views.Structure.read(path)
    width, height, depth = structure.size
    result = []
    for south in range(20):
        row = []
        for east in range(20):
            position = (min(width - 1, int((east + 0.5) * width / 20)), 1, min(depth - 1, int((south + 0.5) * depth / 20)))
            row.append(int(any(structure.blocks.get((position[0], up, position[2])) and structure.blocks[(position[0], up, position[2])].name not in {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'} for up in (1, 2))))
        result.append(row)
    return result


def main():
    digest = hashlib.sha256((TARGET / 'room.nbt').read_bytes()).hexdigest()
    connections = [{'facing': direction, 'position': point, 'width': 5, 'height': 4} for direction, point in [('north', [12, 1, 0]), ('south', [12, 1, 24]), ('west', [0, 1, 12]), ('east', [24, 1, 12])]]
    metadata = {'id': 'G2B-01', 'name': '砂岩荒漠日轮阶梯祭台厅', 'size': [25, 14, 25], 'entrance': [12, 1, 0], 'interior': [12, 1, 3], 'facing': 'north', 'connections': connections, 'reference': str(REFERENCE), 'reference_sha256': hashlib.sha256(REFERENCE.read_bytes()).hexdigest(), 'sha256': digest, 'analysis': str(ROOT / 'ANALYSIS.md'), 'altar_target': [12, 4, 11], 'ring_route_waypoints': [[4, 1, 12], [20, 1, 12], [12, 1, 4], [12, 1, 20]], 'roof': 'closed; preview cutaway only', 'runtime_slot': None, 'registration': 'PENDING_COORDINATOR_ACCEPTANCE', 'world_placement': 'NOT_RUN', 'scope': 'Only S2-01; independently authored fresh geometry; old sealed room templates not used as build inputs', 'deviations': ['25x14x25 is an estimated block grid from image composition.', 'Closed safety roof and level north entry differ from illustrative cutaway/exterior steps.', 'Vanilla flower pots approximate cracked pottery; no image-generated bitmap used as a structure preview.']}
    save(TARGET / 'room.json', metadata)
    structure = views.Structure.read(TARGET / 'room.nbt')
    materials = collections.Counter(block.name for block in structure.blocks.values() if block.name != 'minecraft:air')
    assert materials['minecraft:lantern'] == 20
    assert not any(materials.get('minecraft:' + name) for name in ('gold_block', 'chest', 'spawner', 'water', 'lava', 'command_block', 'jigsaw', 'redstone_lamp'))
    for east in range(25):
        for south in range(25):
            assert structure.blocks[(east, 13, south)].name == 'minecraft:cut_sandstone'
    for position, block in structure.blocks.items():
        if block.name in {'minecraft:lantern', 'minecraft:flower_pot', 'minecraft:sand', 'minecraft:dead_bush'}:
            assert structure.blocks[(position[0], position[1] - 1, position[2])].name != 'minecraft:air'
    save(TARGET / 'materials.json', dict(materials))
    for exterior in (False, True):
        mode = 'exterior' if exterior else 'interior'
        sheet = views.Image.new('RGB', (2400, 1800), '#101a20')
        for index, (direction, title, camera) in enumerate(views.VIEWS):
            image = views.render(structure, metadata, camera, title, exterior, size=(1200, 900))
            image.save(TARGET / f'{mode}-{direction}.png')
            sheet.paste(image, ((index % 2) * 1200, (index // 2) * 900))
            if index == 0 and not exterior:
                image.save(TARGET / 'preview.png')
        sheet.save(TARGET / f'{mode}-four-views.png')
    own = mask(TARGET / 'room.nbt')
    comparisons = []
    roots = [Path('D:/ft工程文件/FT地牢_首批第3组_冰原霜穴_20261104'), Path('D:/ft工程文件/FT地牢_自然环境第7组_蘑菇真菌_20261104')]
    for peer_root in roots:
        for peer in sorted((peer_root / 'incoming').rglob('room.nbt')):
            peer_mask = mask(peer)
            agreement = []
            for rotation in range(4):
                agreement.append(sum(own[row][column] == peer_mask[row][column] for row in range(20) for column in range(20)) / 400)
                peer_mask = [list(row) for row in zip(*peer_mask[::-1])]
            comparisons.append({'reference': str(peer), 'sha256': hashlib.sha256(peer.read_bytes()).hexdigest(), 'max_material_blind_mask_agreement': max(agreement)})
    save(ROOT / 'duplicate-self-check.json', {'status': 'SCREENED_NOT_ALL_GROUP_CERTIFICATION', 'own_sha256': digest, 'metric': '20x20 normalized y1/y2 nonair obstacle occupancy, material-blind, four rotations; not a collision model', 'limitations': 'Common ring circulation and square chambers are reference-driven; agreement is not reuse proof. Only locally available group3/7 snapshots checked. Final coordinator matrix required.', 'matches': sorted(comparisons, key=lambda record: -record['max_material_blind_mask_agreement'])})
    save(TARGET / 'qa.json', {'status': 'STATIC_PASS_NATIVE_PENDING', 'sha256': digest, 'closed_roof': True, 'supported_decorations': True, 'altar_lights': 4, 'wall_lights': 16, 'next_reference_started': False, 'native_world_placement': 'NOT_RUN'})
    save(ROOT / 'registration.json', {'status': 'PENDING_NATIVE_AND_COORDINATOR_ACCEPTANCE', 'group': 2, 'image': 1, 'reference': str(REFERENCE), 'room': str(TARGET / 'room.nbt'), 'sha256': digest, 'next_image': 'S2-02 NOT_STARTED; prohibited until current registration accepted', 'old_output': 'remains sealed, not modified'})
    print('SINGLE_REFERENCE_REVIEWED ' + digest)


if __name__ == '__main__':
    main()
