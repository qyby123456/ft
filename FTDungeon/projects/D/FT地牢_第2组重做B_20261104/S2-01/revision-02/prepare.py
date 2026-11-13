import collections
import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
TARGET = ROOT / 'incoming/G2B-01-R2'
sys.path.insert(0, 'D:/ft工程文件/FT地牢_Image25接入_20261101/tools')
import review_four_views as views


def save(path, data):
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    reference = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S2-sandstone-desert/out/S2-01-large-chamber.png')
    digest = hashlib.sha256((TARGET / 'room.nbt').read_bytes()).hexdigest()
    connections = [{'facing': direction, 'position': point, 'width': width, 'height': 4, 'anchor_type': 'external lower landing' if direction == 'north' else 'raised hall boundary', 'outward_normal': normal} for direction, point, normal, width in [('north', [12, 1, 0], [0, 0, -1], 7), ('south', [12, 4, 30], [0, 0, 1], 5), ('west', [0, 4, 18], [-1, 0, 0], 5), ('east', [24, 4, 18], [1, 0, 0], 5)]]
    metadata = {'id': 'G2B-01-R2', 'name': '砂岩祭台厅·入口高差与厚墙修订', 'size': [25, 18, 31], 'entrance': [12, 1, 0], 'interior': [12, 4, 10], 'facing': 'north', 'connections': connections, 'reference': str(reference), 'reference_sha256': hashlib.sha256(reference.read_bytes()).hexdigest(), 'sha256': digest, 'altar_target': [12, 7, 17], 'ring_route_waypoints': [[4, 4, 18], [20, 4, 18], [12, 4, 10], [12, 4, 26]], 'entrance_route': [[12, 1, 0], [12, 1, 1], [12, 2, 2], [12, 3, 3], [12, 4, 4], [12, 4, 5], [12, 4, 6], [12, 4, 10]], 'roof': 'full roof y16 over hall; preview-only cutaway not deployable', 'registration': 'NEEDS_INDEPENDENT_REVIEW_NOT_ACCEPTED', 'supersedes_sha256': 'd7ea7cbc119a45d10a47ad1f2cfa25d0b0399aed4790923c8e26673221b66c57', 'world_placement': 'NOT_RUN', 'runtime_slot': None}
    save(TARGET / 'room.json', metadata)
    structure = views.Structure.read(TARGET / 'room.nbt')
    materials = collections.Counter(block.name for block in structure.blocks.values() if block.name != 'minecraft:air')
    plants = []
    for position, block in structure.blocks.items():
        if block.name == 'minecraft:dead_bush':
            below = structure.blocks[(position[0], position[1] - 1, position[2])].name
            assert below == 'minecraft:sand'
            plants.append({'position': position, 'support': below, 'tag_check': 'minecraft:dead_bush_may_place_on includes #minecraft:sand; Bootstrap tags unavailable; not a runtime survival claim'})
        if block.name in {'minecraft:lantern', 'minecraft:flower_pot', 'minecraft:sand'}:
            assert structure.blocks[(position[0], position[1] - 1, position[2])].name not in {'minecraft:air', 'minecraft:water'}
    assert len(plants) == 4
    assert not materials.get('minecraft:water') and not materials.get('minecraft:lava')
    assert all(structure.blocks[(east, 16, south)].name != 'minecraft:air' for east in range(25) for south in range(6, 31))
    save(TARGET / 'materials.json', dict(materials))
    save(TARGET / 'qa.json', {'status': 'STATIC_PASS_NATIVE_PENDING', 'sha256': digest, 'plant_checks': plants, 'liquid_boundary': 'NO_FLUIDS', 'roof': 'PASS', 'native_can_survive_with_loaded_tags': 'NOT_EVALUATED', 'world_placement': 'NOT_RUN'})
    original_filter = views.filtered
    def filtered(structure, metadata, camera, exterior):
        visible = original_filter(structure, metadata, camera, exterior)
        if exterior:
            return visible
        return {point: block for point, block in visible.items() if point[1] < 4 or not ((point[0] <= 3 if camera[0] < 0 else point[0] >= 21) or (6 <= point[2] <= 9 if camera[1] < 0 else point[2] >= 27))}
    views.filtered = filtered
    for exterior in (False, True):
        mode = 'exterior' if exterior else 'interior'
        sheet = views.Image.new('RGB', (2400, 1800), '#101a20')
        for index, (direction, title, camera) in enumerate(views.VIEWS):
            image = views.render(structure, metadata, camera, title, exterior, size=(1200, 900))
            image.save(TARGET / f'{mode}-{direction}.png')
            sheet.paste(image, ((index % 2) * 1200, (index // 2) * 900))
            if index == 0 and not exterior:
                image.save(TARGET / 'software-check.png')
        sheet.save(TARGET / f'{mode}-four-views.png')
    save(ROOT / 'assembly.json', {'origin': [0, 0, 0], 'bounds': [[0, 0, 0], [24, 17, 30]], 'world_origin': None, 'single_template': True, 'order': [{'step': 1, 'file': str(TARGET / 'room.nbt'), 'offset': [0, 0, 0]}], 'connections': connections, 'hall_floor_y': 3, 'hall_feet_y': 4, 'north_external_feet_y': 1, 'height_difference': 3, 'warning': 'North connects to lower landing, not hall elevation. Validate whole bounding volume including explicit air. Preview-only NBT must not be placed.'})
    print('PREPARED ' + digest)


if __name__ == '__main__':
    main()
