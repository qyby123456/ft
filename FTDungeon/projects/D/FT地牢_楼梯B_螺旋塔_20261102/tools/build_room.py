import hashlib
import json
import math
from collections import Counter
from pathlib import Path

from nbtio import AIR, Structure, Tag, compound, integer, state, string


ROOT = Path(__file__).resolve().parents[1]
REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-stair-rooms-20261102/out/stair-spiral-tower.png')
REFERENCE_HASH = 'eb765a30eb267b5d3602f62be563e907ea516cb5a8c5b469013a3d57a97fdda8'
SIZE = (27, 27, 27)
STONE = state('stone_bricks')
TRIM = state('polished_andesite')
FULL = {'stone_bricks', 'mossy_stone_bricks', 'cracked_stone_bricks', 'chiseled_stone_bricks', 'polished_andesite', 'polished_deepslate', 'stripped_oak_wood'}


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def write_json(path, value):
    Path(path).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def stone(position):
    pos_x, pos_y, pos_z = position
    sample = (pos_x * 73 + pos_y * 31 + pos_z * 17) % 101
    return state('mossy_stone_bricks' if sample < 9 else 'cracked_stone_bricks' if sample < 15 else 'stone_bricks')


def main():
    assert digest(REFERENCE) == REFERENCE_HASH
    room = Structure(SIZE)
    for pos_y in range(27):
        for pos_z in range(27):
            for pos_x in range(27):
                room.set((pos_x, pos_y, pos_z), AIR)
                radius = math.hypot(pos_x - 13, pos_z - 13)
                if radius <= 13.25 and (pos_y in (0, 7, 26) or radius >= 11.5):
                    room.set((pos_x, pos_y, pos_z), stone((pos_x, pos_y, pos_z)))
    for pos_y in (0, 7, 18, 25):
        for pos_z in range(27):
            for pos_x in range(27):
                radius = math.hypot(pos_x - 13, pos_z - 13)
                if 11.5 <= radius <= 13.25:
                    room.set((pos_x, pos_y, pos_z), TRIM)
    for pos_x, pos_z in [(5, 3), (21, 3), (3, 5), (23, 5), (3, 21), (23, 21), (5, 23), (21, 23)]:
        for pos_y in range(27):
            room.set((pos_x, pos_y, pos_z), state('chiseled_stone_bricks') if pos_y in (1, 7, 18, 25) else TRIM)
    core = []
    for pos_y in range(26):
        for pos_z in range(11, 16):
            for pos_x in range(11, 16):
                if (pos_x, pos_z) not in ((11, 11), (11, 15), (15, 11), (15, 15)):
                    position = (pos_x, pos_y, pos_z)
                    room.set(position, state('chiseled_stone_bricks') if pos_y in (7, 18, 25) else stone(position))
                    core.append(list(position))
    stairs = []
    supports = set()
    rails = set()

    def deck(start, end, floor_y):
        for pos_x in range(start[0], end[0] + 1):
            for pos_z in range(start[1], end[1] + 1):
                for pos_y in (floor_y - 1, floor_y):
                    room.set((pos_x, pos_y, pos_z), stone((pos_x, pos_y, pos_z)))
                    supports.add((pos_x, pos_y, pos_z))

    def railing(pos_x, floor_y, pos_z):
        if room.get((pos_x, floor_y + 1, pos_z)).name.removeprefix('minecraft:') in FULL:
            return
        room.set((pos_x, floor_y, pos_z), STONE)
        supports.add((pos_x, floor_y, pos_z))
        room.set((pos_x, floor_y + 1, pos_z), state('iron_bars', north=False, south=False, east=False, west=False, waterlogged=False))
        rails.add((pos_x, floor_y + 1, pos_z))

    def tread(position, facing, kind):
        room.set(position, state('stone_brick_stairs', facing=facing, half='bottom', shape='straight', waterlogged=False))
        below = (position[0], position[1] - 1, position[2])
        room.set(below, STONE)
        supports.add(below)
        stairs.append({'pos': list(position), 'facing': facing, 'kind': kind, 'support': list(below)})

    for start, end, floor_y in [((8, 8), (10, 10), 10), ((8, 16), (10, 18), 13), ((16, 16), (18, 18), 16), ((16, 8), (18, 10), 19)]:
        deck(start, end, floor_y)
        for pos_x in range(start[0], end[0] + 1):
            railing(pos_x, floor_y, 7 if start[1] == 8 else 19)
        for pos_z in range(start[1], end[1] + 1):
            railing(7 if start[0] == 8 else 19, floor_y, pos_z)
        railing(7 if start[0] == 8 else 19, floor_y, 7 if start[1] == 8 else 19)
    flights = [('north', 'west', [15, 14, 13, 12, 11], 8), ('west', 'south', [11, 12, 13, 14, 15], 11), ('south', 'east', [11, 12, 13, 14, 15], 14), ('east', 'north', [15, 14, 13, 12, 11], 17)]
    for side, facing, coordinates, base_y in flights:
        for index, coordinate in enumerate(coordinates):
            floor_y = base_y + min(index, 2)
            for radial in range(8, 11):
                position = (coordinate, floor_y, radial if side == 'north' else radial + 8) if side in ('north', 'south') else (radial if side == 'west' else radial + 8, floor_y, coordinate)
                if index < 3:
                    tread(position, facing, 'spiral_up')
                else:
                    deck((position[0], position[2]), (position[0], position[2]), floor_y)
            rail_x, rail_z = (coordinate, 7 if side == 'north' else 19) if side in ('north', 'south') else (7 if side == 'west' else 19, coordinate)
            room.set((rail_x, floor_y - 1, rail_z), STONE)
            supports.add((rail_x, floor_y - 1, rail_z))
            railing(rail_x, floor_y, rail_z)
    wall_bearings = []
    for start_x, end_x, pos_z, pos_y in [(2, 8, 9, 9), (2, 8, 17, 12), (18, 24, 17, 15), (18, 24, 9, 18)]:
        rib = []
        for pos_x in range(start_x, end_x + 1):
            position = (pos_x, pos_y, pos_z)
            room.set(position, STONE)
            supports.add(position)
            rib.append(list(position))
        wall_bearings.append(rib)
    inner_corner_guards = []
    for pos_x, pos_z, top_y in [(11, 11, 11), (11, 15, 14), (15, 15, 17), (15, 11, 19)]:
        for pos_y in range(7, top_y + 1):
            room.set((pos_x, pos_y, pos_z), STONE)
            supports.add((pos_x, pos_y, pos_z))
        railing(pos_x, top_y, pos_z)
        inner_corner_guards.append([pos_x, top_y + 1, pos_z])
    deck((11, 2), (18, 5), 19)
    deck((16, 6), (18, 7), 19)
    for pos_x in range(16, 19):
        room.set((pos_x, 20, 7), AIR)
        rails.discard((pos_x, 20, 7))
    for pos_z in range(2, 8):
        railing(19, 19, pos_z)
        if pos_z >= 6:
            railing(15, 19, pos_z)
    for pos_x in range(10, 16):
        railing(pos_x, 19, 6)
    for pos_z in range(2, 6):
        railing(10, 19, pos_z)
    for pos_y in range(1, 8):
        for pos_z in range(5, 13):
            for pos_x in range(8, 11):
                room.set((pos_x, pos_y, pos_z), AIR)
    for pos_z in range(6, 13):
        stair_y = pos_z - 5
        for pos_x in range(8, 11):
            room.box((pos_x, 0, pos_z), (pos_x, stair_y - 1, pos_z), STONE)
            tread((pos_x, stair_y, pos_z), 'south', 'straight_down')
    for pos_z in range(5, 13):
        for pos_x in (7, 11):
            for pos_y in range(1, 8):
                if room.get((pos_x, pos_y, pos_z)) == AIR:
                    room.set((pos_x, pos_y, pos_z), stone((pos_x, pos_y, pos_z)))
            railing(pos_x, 7, pos_z)
    for pos_x in range(7, 12):
        railing(pos_x, 7, 4)
    openings = []
    for name, level, side in [('main_north', 8, 'north'), ('upper_north', 20, 'north'), ('lower_south', 1, 'south')]:
        for pos_x in range(12, 15):
            for pos_y in range(level, level + 3):
                for pos_z in (range(0, 4) if side == 'north' else range(23, 27)):
                    room.set((pos_x, pos_y, pos_z), AIR)
                    openings.append((pos_x, pos_y, pos_z))
            for pos_z in (range(0, 4) if side == 'north' else range(23, 27)):
                room.set((pos_x, level - 1, pos_z), TRIM)
        for pos_x in (11, 15):
            for pos_y in range(level, level + 4):
                room.set((pos_x, pos_y, 2 if side == 'north' else 24), state('chiseled_stone_bricks'))
        for pos_x in range(11, 16):
            room.set((pos_x, level + 3, 2 if side == 'north' else 24), STONE)
    for floor_y in (8, 20):
        for pos_x in range(12, 15):
            for pos_z in (4, 5):
                room.set((pos_x, floor_y, pos_z), state('red_carpet'))
    lanterns = []
    for pos_x, pos_z in [(6, 17), (20, 10)]:
        for pos_y in range(22, 26):
            room.set((pos_x, pos_y, pos_z), state('chain', axis='y', waterlogged=False))
        room.set((pos_x, 21, pos_z), state('lantern', hanging=True, waterlogged=False))
        lanterns.append({'lantern': [pos_x, 21, pos_z], 'chains': [[pos_x, pos_y, pos_z] for pos_y in range(22, 26)], 'anchor': [pos_x, 26, pos_z]})
    for pos_y in (4, 11, 23):
        for pos_x, pos_z, facing in [(2, 13, 'east'), (24, 13, 'west'), (13, 2, 'south'), (13, 24, 'north')]:
            room.set((pos_x, pos_y, pos_z), state('wall_torch', facing=facing))
    webs = [(5, 24, 5), (21, 24, 5), (5, 24, 21)]
    for position in webs:
        room.set(position, state('cobweb'))
    for pos_z in (14, 15):
        position = (4, 8, pos_z)
        room.set(position, state('barrel', facing='up', open=False), compound(id=string('minecraft:barrel'), x=integer(4), y=integer(8), z=integer(pos_z), Items=Tag(9, [], 10)))
    room.set((4, 8, 16), state('stripped_oak_wood', axis='y'))
    directions = {'north': (0, -1), 'south': (0, 1), 'east': (1, 0), 'west': (-1, 0)}
    for position in sorted(rails):
        properties = {'waterlogged': False}
        for facing, (delta_x, delta_z) in directions.items():
            neighbor = room.get((position[0] + delta_x, position[1], position[2] + delta_z))
            properties[facing] = neighbor.name.removeprefix('minecraft:') in FULL | {'iron_bars'}
        room.set(position, state('iron_bars', **properties))
    up_route = [[13, 8, 0], [13, 8, 4], [17, 8, 4], [17, 8, 9], [16, 8, 9], [15, 9, 9], [14, 10, 9], [13, 11, 9], [12, 11, 9], [11, 11, 9], [9, 11, 9], [9, 11, 10], [9, 12, 11], [9, 13, 12], [9, 14, 13], [9, 14, 15], [9, 14, 17], [10, 14, 17], [11, 15, 17], [12, 16, 17], [13, 17, 17], [15, 17, 17], [17, 17, 17], [17, 17, 16], [17, 18, 15], [17, 19, 14], [17, 20, 13], [17, 20, 11], [17, 20, 9], [17, 20, 4], [13, 20, 4], [13, 20, 0]]
    down_route = [[13, 8, 0], [13, 8, 3], [18, 8, 3], [18, 8, 21], [6, 8, 21], [6, 8, 14], [9, 8, 14], [9, 8, 13], [9, 8, 12], [9, 7, 11], [9, 6, 10], [9, 5, 9], [9, 4, 8], [9, 3, 7], [9, 2, 6], [9, 1, 5], [9, 1, 4], [6, 1, 4], [6, 1, 20], [13, 1, 20], [13, 1, 26]]
    blueprint = {'size': list(SIZE), 'levels_feet_y': [1, 8, 20], 'core_blocks': core, 'stairs': stairs, 'structural_supports': [list(position) for position in sorted(supports)], 'guardrails': [list(position) for position in sorted(rails)], 'lanterns': lanterns, 'cobwebs': [list(position) for position in webs], 'openings': [list(position) for position in openings], 'routes': {'spiral_up': up_route, 'straight_down': down_route}, 'safe_points': [[13, 8, 3], [17, 8, 6], [9, 1, 4], [13, 1, 20], [13, 20, 3]], 'clear_stair_width_blocks': 3, 'stair_rise_blocks': {'spiral_up': 12, 'straight_down': 7}}
    blueprint.update(wall_bearing_ribs=wall_bearings, inner_corner_guards=inner_corner_guards)
    room.write(ROOT / 'out/room.nbt')
    metadata = {'id': 'STAIR_B', 'name': '螺旋楼梯塔', 'size': list(SIZE), 'entrance': [13, 8, 0], 'interior': [13, 8, 3], 'facing': 'north', 'sha256': digest(ROOT / 'out/room.nbt'), 'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH, 'ports': [{'id': 'main_north', 'feet': [13, 8, 0], 'facing': 'north', 'width': 3, 'height': 3}, {'id': 'upper_north', 'feet': [13, 20, 0], 'facing': 'north', 'width': 3, 'height': 3}, {'id': 'lower_south', 'feet': [13, 1, 26], 'facing': 'south', 'width': 3, 'height': 3}], 'placement_contract': 'Origin must allow main entrance floor offset +7. Three external ports need explicit core layout adapters; no automatic FT catalog registration.', 'air_policy': 'All 19683 positions explicit, including air. Require occupancy/border/entity preflight; never clear an occupied target.', 'entities': 0, 'empty_containers': 2, 'design_translation': 'Voxel octagonal tower; four-flight square spiral around octagonal central pier; three-block-wide treads and landings; complete roof and shell.', 'deployment': 'NOT_DEPLOYED'}
    write_json(ROOT / 'out/room.json', metadata)
    write_json(ROOT / 'out/blueprint.json', blueprint)
    write_json(ROOT / 'qa/materials.json', dict(sorted(Counter(block.name for block in room.blocks.values()).items())))
    print(json.dumps({'status': 'BUILT', 'size': SIZE, 'records': len(room.blocks), 'stairs': len(stairs), 'sha256': metadata['sha256']}, ensure_ascii=False))


if __name__ == '__main__':
    main()
