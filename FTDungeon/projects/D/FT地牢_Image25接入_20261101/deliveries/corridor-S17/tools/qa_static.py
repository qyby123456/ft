import collections
import hashlib
import itertools
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
NBT_TOOLS = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
sys.path.insert(0, str(NBT_TOOLS))
from nbtio import Structure, plain, read_nbt, write_nbt, Tag, integer, intlist


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    structure = Structure.read(ROOT / 'room.nbt')
    _, encoded = read_nbt(ROOT / 'room.nbt')
    template = plain(encoded)
    checks = {}

    def require(name, condition):
        checks[name] = bool(condition)
        if not condition:
            raise ValueError(name)

    require('reference_fingerprint', hashlib.sha256(Path(metadata['reference']).read_bytes()).hexdigest() == metadata['reference_sha256'])
    fingerprint = hashlib.sha256((ROOT / 'room.nbt').read_bytes()).hexdigest()
    require('structure_fingerprint', fingerprint == metadata['sha256'])
    require('gzip_single_palette_1201', (ROOT / 'room.nbt').read_bytes()[:2] == b'\x1f\x8b' and template['DataVersion'] == 3465 and 'palette' in template and 'palettes' not in template)
    require('exact_short_dimensions', structure.size == (15, 13, 16) and metadata['size'] == [15, 13, 16])
    require('full_explicit_volume', len(structure.blocks) == 15 * 13 * 16)
    require('coordinates_in_bounds', all(all(0 <= position[axis] < structure.size[axis] for axis in range(3)) for position in structure.blocks))
    require('no_entities_block_entities_inventory', not structure.entities and not structure.block_entities)
    hazards = {'water', 'lava', 'fire', 'soul_fire', 'magma_block', 'cactus', 'powder_snow', 'campfire', 'soul_campfire', 'wither_rose', 'sweet_berry_bush', 'cobweb', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'structure_void', 'jigsaw', 'tnt', 'light'}
    require('no_liquids_hazards_executables_invisible_lights', all(block.name.startswith('minecraft:') and block.name.split(':')[1] not in hazards for block in structure.blocks.values()))
    air = lambda position: structure.get(position).name == 'minecraft:air'
    require('three_wide_three_high_straight_route', all(air((positionX, positionY, positionZ)) for positionX, positionY, positionZ in itertools.product(range(6, 9), range(1, 4), range(16))))
    require('continuous_full_floor', all(not air((positionX, 0, positionZ)) for positionX, positionZ in itertools.product(range(15), range(16))))
    require('complete_roof', all(structure.get((positionX, 12, positionZ)).name == 'minecraft:deepslate_tiles' for positionX, positionZ in itertools.product(range(15), range(16))))
    require('side_walls_sealed_no_branches', all(not air((positionX, positionY, positionZ)) for positionX, positionY, positionZ in itertools.product([0, 14], range(1, 12), range(16))))
    require('two_seven_by_six_portals', all(air((positionX, positionY, positionZ)) for positionX, positionY, positionZ in itertools.product(range(4, 11), range(1, 7), [0, 15])))
    require('canonical_interfaces', metadata['entrance'] == [7, 1, 0] and metadata['exit'] == [7, 1, 15] and metadata['interior'] == [7, 1, 3])
    for torchX, facing, supportX in [(2, 'east', 1), (12, 'west', 13)]:
        for torchZ in [2, 7, 13]:
            block = structure.get((torchX, 5, torchZ))
            require(f'torch_support_{torchX}_{torchZ}', block.name == 'minecraft:wall_torch' and dict(block.properties)['facing'] == facing and not air((supportX, 5, torchZ)))
    cages = []
    for centerX, centerZ in [(4, 3), (10, 3), (4, 12), (10, 12)]:
        require(f'cage_solid_cap_no_chain_gap_{centerX}_{centerZ}', all(structure.get((positionX, 9, positionZ)).name == 'minecraft:polished_blackstone' for positionX, positionZ in itertools.product(range(centerX - 1, centerX + 2), range(centerZ - 1, centerZ + 2))))
        require(f'cage_chain_to_cap_{centerX}_{centerZ}', all(structure.get((centerX, positionY, centerZ)).name == 'minecraft:chain' for positionY in [8, 10, 11]) and not air((centerX, 12, centerZ)))
        require(f'lantern_internal_support_{centerX}_{centerZ}', structure.get((centerX, 7, centerZ)).name == 'minecraft:lantern' and dict(structure.get((centerX, 7, centerZ)).properties)['hanging'] == 'true')
        cages.append({'center_xz': [centerX, centerZ], 'bottom_y': 6, 'lantern_y': 7, 'roof_y': 9, 'suspension_y': [10, 11], 'structural_cap_y': 12})
    reached = {tuple(metadata['entrance'])}
    pending = collections.deque(reached)
    while pending:
        positionX, positionY, positionZ = pending.popleft()
        for deltaX, deltaZ in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
            following = (positionX + deltaX, positionY, positionZ + deltaZ)
            if 0 <= following[0] < 15 and 0 <= following[2] < 16 and following not in reached and air(following) and air((following[0], 2, following[2])) and not air((following[0], 0, following[2])):
                reached.add(following)
                pending.append(following)
    require('entrance_interior_exit_bidirectional_reachable', tuple(metadata['interior']) in reached and tuple(metadata['exit']) in reached)
    counts = collections.Counter(block.name for block in structure.blocks.values())
    save(ROOT / 'materials.json', {'structure_sha256': fingerprint, 'counts': dict(sorted(counts.items())), 'non_air_blocks': sum(count for name, count in counts.items() if name != 'minecraft:air')})
    save(ROOT / 'qa/static.json', {'status': 'PASS', 'structure_sha256': fingerprint, 'checks': checks, 'explicit_records': len(structure.blocks), 'palette_entries': len(template['palette']), 'reachable_foot_positions': len(reached), 'cages': cages, 'scope': 'NBT/schema, conservative block-grid route and authored support checks; actual collision, survival and rotational placement require Forge.', 'deployment_tested': False})
    empty = Tag(10, {'DataVersion': integer(3465), 'size': intlist([1, 1, 1]), 'palette': Tag(9, [Tag(10, {'Name': Tag(8, 'minecraft:air')})], 10), 'blocks': Tag(9, [Tag(10, {'pos': intlist([0, 0, 0]), 'state': integer(0)})], 10), 'entities': Tag(9, [], 10)})
    write_nbt(ROOT / 'forge/src/main/resources/data/fts17qa/structures/empty.nbt', empty)
    print(json.dumps({'status': 'PASS', 'checks': len(checks), 'blocks': len(structure.blocks), 'reachable': len(reached), 'sha256': fingerprint}))


if __name__ == '__main__':
    main()
