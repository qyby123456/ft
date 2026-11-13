import argparse
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
EXPECTED = 'b01fdaf300987e299d141f1963530ad2b1861be8fe4c17631cdc879f7c7415d8'


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--origin', nargs=3, type=int, required=True)
    parser.add_argument('--lobby', nargs=3, type=float, required=True)
    parser.add_argument('--dimension', default='minecraft:overworld')
    parser.add_argument('--out', type=Path, required=True)
    arguments = parser.parse_args()
    candidate = ROOT / 'assembly/s4-wetland-connected.nbt'
    assert hashlib.sha256(candidate.read_bytes()).hexdigest() == EXPECTED
    assert arguments.dimension == 'minecraft:overworld', 'This validated installer targets the overworld only.'
    assert arguments.lobby[1].is_integer(), 'Lobby feet Y must be integral.'
    layout = json.loads((ROOT / 'assembly/layout.json').read_text(encoding='utf-8'))
    interior = [axis + shift for axis, shift in zip(arguments.origin, layout['start'])]
    origin = arguments.origin
    lobby = arguments.lobby
    assert -64 <= origin[1] and origin[1] + 24 <= 320
    assert -63 <= lobby[1] <= 316
    assert all(abs(value) < 29999000 for value in (origin[0], origin[2], lobby[0], lobby[2]))
    assert not (origin[0]-4 <= lobby[0] <= origin[0]+419 and origin[2]-4 <= lobby[2] <= origin[2]+71), 'Reserve a lobby outside the full structure footprint.'
    destination = arguments.out.resolve()
    destination.mkdir(parents=True, exist_ok=True)
    def save(relative, text):
        path = destination / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding='utf-8')
    def coordinates(values):
        return ' '.join(f'{value:g}' for value in values)
    save('pack.mcmeta', json.dumps({'pack': {'pack_format': 15, 'description': 'S4 controlled access; candidate SHA256 ' + EXPECTED}}))
    for name in ('load', 'tick'):
        save('data/minecraft/tags/functions/' + name + '.json', json.dumps({'values': ['fts4_access:' + name]}))
    load = '\n'.join('scoreboard objectives add ' + name + ' ' + kind for name, kind in [('fts4_enter', 'trigger'), ('fts4_exit', 'trigger'), ('fts4_inside', 'dummy')])
    save('data/fts4_access/functions/load.mcfunction', load + '\n')
    inside_box = f'x={origin[0]},y={origin[1]},z={origin[2]},dx=414,dy=23,dz=66'
    lobby_box = f'x={lobby[0]-2.5:g},y={lobby[1]-0.5:g},z={lobby[2]-2.5:g},dx=5,dy=3,dz=5'
    tick = [
        'scoreboard players add @a fts4_inside 0',
        f'execute as @a[scores={{fts4_enter=1..,fts4_inside=0}}] at @s if dimension {arguments.dimension} if entity @s[{lobby_box}] run function fts4_access:enter',
        f'execute as @a[scores={{fts4_exit=1..,fts4_inside=1}}] at @s if dimension {arguments.dimension} if entity @s[{inside_box}] run function fts4_access:return',
        f'execute as @a[scores={{fts4_inside=1}}] at @s unless dimension {arguments.dimension} run scoreboard players set @s fts4_inside 0',
        f'execute as @a[scores={{fts4_inside=1}}] at @s if dimension {arguments.dimension} unless entity @s[{inside_box}] run scoreboard players set @s fts4_inside 0',
        'scoreboard players set @a fts4_enter 0', 'scoreboard players set @a fts4_exit 0',
        'scoreboard players enable @a fts4_enter', 'scoreboard players enable @a fts4_exit']
    save('data/fts4_access/functions/tick.mcfunction', '\n'.join(tick) + '\n')
    save('data/fts4_access/functions/enter.mcfunction', f'execute in {arguments.dimension} run teleport @s {coordinates(interior)} -90 0\nscoreboard players set @s fts4_inside 1\n')
    save('data/fts4_access/functions/return.mcfunction', f'execute in {arguments.dimension} run teleport @s {coordinates(lobby)} -90 0\nscoreboard players set @s fts4_inside 0\n')
    east, up, south = int(lobby[0]), int(lobby[1]), int(lobby[2])
    install = [f'execute in {arguments.dimension} run fill {east-3} {up-1} {south-3} {east+3} {up-1} {south+3} minecraft:stone_bricks',
               f'execute in {arguments.dimension} run fill {east-3} {up} {south-3} {east+3} {up+3} {south+3} minecraft:air',
               f'execute in {arguments.dimension} run setblock {east-3} {up} {south-3} minecraft:ochre_froglight',
               f'execute in {arguments.dimension} run setblock {east+3} {up} {south+3} minecraft:ochre_froglight']
    save('data/fts4_access/functions/install.mcfunction', '\n'.join(install) + '\n')
    files = [dict(path=path.relative_to(destination).as_posix(), sha256=hashlib.sha256(path.read_bytes()).hexdigest()) for path in sorted(destination.rglob('*')) if path.is_file() and path.name != 'installation.json']
    specification = dict(candidate=str(candidate), candidate_sha256=EXPECTED, dimension=arguments.dimension, origin=origin, lobby=lobby, interior=interior,
                         entry_region=lobby_box, return_region=inside_box, platform_write_box=[[east-3, up-1, south-3], [east+3, up+3, south+3]],
                         boundary_modified=False, files=files, entry_command='/trigger fts4_enter set 1', return_command='/trigger fts4_exit set 1',
                         scope='Installable vanilla 1.20.1 datapack; reserve the lobby write box first. Per-player inside score gates returns. Single configured installation per world.')
    save('installation.json', json.dumps(specification, ensure_ascii=False, indent=2))
    print(json.dumps(dict(pack=str(destination), candidate_sha256=EXPECTED, interior=interior, lobby=lobby)))


if __name__ == '__main__':
    main()
