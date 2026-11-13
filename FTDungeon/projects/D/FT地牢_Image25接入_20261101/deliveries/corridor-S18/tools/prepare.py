import collections
import hashlib
import json
import sys
from pathlib import Path

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import Structure, plain, read_nbt

REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S18-forge-lava-flooded.png')
EXPECTED_REFERENCE = '90f93164051207db586026e4c3b910809ba98e07acfc3529de12e5436f921e9f'
STEPS = ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1))
SOLID = {'polished_blackstone_bricks', 'deepslate_tiles', 'polished_blackstone', 'polished_deepslate', 'chiseled_polished_blackstone', 'cracked_polished_blackstone_bricks', 'mossy_stone_bricks', 'nether_bricks', 'glass'}


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    source = ROOT / 'room.nbt'
    structure = Structure.read(source)
    _, raw = read_nbt(source)
    fingerprint = digest(source)
    assert digest(REFERENCE) == EXPECTED_REFERENCE
    assert source.read_bytes()[:2] == b'\x1f\x8b'
    assert structure.size == (15, 11, 16) and structure.data_version == 3465
    assert 'palette' in raw.value and 'palettes' not in raw.value
    assert len(structure.blocks) == 2640 and len(set(structure.blocks)) == 2640
    assert not structure.entities and not structure.block_entities
    assert all(all(0 <= entry < bound for entry, bound in zip(position, structure.size)) for position in structure.blocks)
    names = collections.Counter(block.name.removeprefix('minecraft:') for block in structure.blocks.values())
    assert set(names) <= SOLID | {'air', 'water', 'lava', 'anvil', 'chain', 'soul_lantern', 'iron_bars', 'polished_blackstone_brick_stairs'}
    block_name = lambda position: structure.get(position).name.removeprefix('minecraft:')
    lava = [position for position in structure.blocks if block_name(position) == 'lava']
    water = [position for position in structure.blocks if block_name(position) == 'water']
    assert len(lava) == 12 and len(water) == 48
    for position in lava:
        assert structure.get(position).get('level') == '0'
        for step in STEPS:
            neighbor = tuple(value + delta for value, delta in zip(position, step))
            assert block_name(neighbor) == 'lava' or block_name(neighbor) in SOLID, (position, neighbor)
    for position in water:
        assert position[1] == 2 and structure.get(position).get('level') == '0'
        for step in STEPS:
            if step == (0, 1, 0):
                continue
            neighbor = tuple(value + delta for value, delta in zip(position, step))
            assert block_name(neighbor) == 'water' or block_name(neighbor) in SOLID, (position, neighbor)
    reachable = []
    for blockZ in range(16):
        for blockX in range(6, 9):
            assert block_name((blockX, 2, blockZ)) in SOLID
            assert all(block_name((blockX, blockY, blockZ)) == 'air' for blockY in range(3, 6))
            reachable.append([blockX, 3, blockZ])
    assert all(block_name((blockX, 10, blockZ)) in SOLID and block_name((blockX, 9, blockZ)) in SOLID for blockX in range(15) for blockZ in range(16))
    assert all(block_name((blockX, blockY, blockZ)) in SOLID for blockX in range(3, 12) for blockY in range(6, 9) for blockZ in (0, 15))
    lamps = [position for position in structure.blocks if block_name(position) == 'soul_lantern']
    chains = [position for position in structure.blocks if block_name(position) == 'chain']
    anvils = [position for position in structure.blocks if block_name(position) == 'anvil']
    assert len(lamps) == 6 and len(chains) == 12 and len(anvils) == 6
    for blockX, blockY, blockZ in lamps:
        assert block_name((blockX, blockY + 1, blockZ)) == 'chain'
        assert block_name((blockX, blockY + 2, blockZ)) == 'chain'
        assert block_name((blockX, blockY + 3, blockZ)) in SOLID
    for blockX, blockY, blockZ in anvils:
        assert block_name((blockX, blockY - 1, blockZ)) in SOLID
    metadata = {
        'id': 'CORRIDOR_S18', 'design_id': 'S18', 'name': '浸水熔铸短廊',
        'size': [15, 11, 16], 'entrance': [7, 3, 0], 'interior': [7, 3, 2],
        'exit': [7, 3, 15], 'facing': 'north', 'axis': 'north-south',
        'sha256': fingerprint, 'reference': str(REFERENCE), 'reference_sha256': EXPECTED_REFERENCE,
        'reference_documents': [str(REFERENCE.parents[1] / name) for name in ('README.md', 'manifest.json', 'SPEC.md', 'prompts/S18-forge-lava-flooded.txt')],
        'minecraft': '1.20.1', 'data_version': 3465,
        'placement': {'foot_y': 3, 'floor_y': 2, 'dry_route_width': 3, 'end_openings': '3x3 minimum at z=0 and z=15', 'length_blocks': 16, 'turns': 0, 'branches': 0, 'stairs_on_route': 0},
        'liquids': {'lava_sources': len(lava), 'water_sources': len(water), 'lava_enclosure': 'full solid blocks and full glass on all six faces', 'water': 'one-block-deep recessed side gutters, fully floored and side-contained'},
        'reference_adaptations': [
            '保留黑石与深板岩、四道拱框、六盏魂灯、六座铁砧、两侧橙色锻炉及苔绿墙面。',
            '全地面浸水改为两侧各两格宽浅水槽；中央保留三格宽、全程无水无跳跃的干燥石路。',
            '裸露熔岩炉槽改为墙内六面封闭的玻璃观察炉；与低位水槽完全隔离。',
            '概念图尽头闭合铁门改为两侧铁栅与开放拱口，保证一字贯通；两端脚位均为 Y=3。',
            '潮湿藻苔用完整苔石砖嵌墙表达，不使用可燃藤蔓；轻损仅为裂砖纹理，不破坏屋顶和梁柱。'
        ],
        'boundary': 'Independent delivery only. No deployment, live world changes, global ID assignment, core source edits, or modpack edits.',
        'entities': 0, 'block_entities': 0, 'containers': 0, 'loot': False
    }
    write_json(ROOT / 'room.json', metadata)
    write_json(ROOT / 'qa/features.json', {'lava': lava, 'water': water, 'lamps': lamps, 'chains': chains, 'anvils': anvils})
    checks = ['reference_sha256', 'gzip_single_palette_java_1_20_1', 'full_volume_2640_unique_records', 'bounds', 'only_vanilla_nonflammable_palette', 'no_entities_no_containers_no_loot', 'three_by_three_both_end_openings', 'dry_three_wide_straight_bidirectional_route_16_blocks', 'complete_two_layer_roof', 'four_arches_join_walls_and_roof', 'twelve_lava_sources_six_side_sealed', 'fortyeight_water_sources_bottom_and_sides_sealed', 'water_lava_no_contact', 'six_lanterns_chains_roof_anchored', 'six_anvils_full_floor_support']
    write_json(ROOT / 'qa/static-qa.json', {'id': 'CORRIDOR_S18', 'status': 'PASS', 'sha256': fingerprint, 'reference_sha256': EXPECTED_REFERENCE, 'checks': {entry: True for entry in checks}, 'material_counts': dict(names), 'reachable_feet_positions': reachable, 'limits': 'Conservative static geometry checks. State registry, collision, support survival and 400-tick physics are tested separately in Forge.'})
    materials = 'block,count\n' + ''.join(f'minecraft:{name},{count}\n' for name, count in sorted(names.items()) if name != 'air')
    (ROOT / 'materials.csv').write_text(materials, encoding='utf-8')
    print(f'S18 static PASS records=2640 lava={len(lava)} water={len(water)} sha256={fingerprint}')


if __name__ == '__main__':
    main()
