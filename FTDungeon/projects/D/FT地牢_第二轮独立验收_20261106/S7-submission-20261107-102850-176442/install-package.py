import hashlib
import json
import shutil
from pathlib import Path

root = Path(__file__).resolve().parent
parent = root
assembly = json.loads((parent / 'assembly/assembly.json').read_text(encoding='utf-8'))
pack = root / 'installation/fts7'
functions = pack / 'data/fts7/functions'
functions.mkdir(parents=True, exist_ok=True)
def write(relative, text):
    path = pack / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding='utf-8')
write('pack.mcmeta', json.dumps({'pack': {'pack_format': 15, 'description': 'S7 zero-rotation isolated install and gated return; operator install only'}}, indent=2))
write('data/minecraft/tags/functions/load.json', '{"values":["fts7:load"]}')
write('data/minecraft/tags/functions/tick.json', '{"values":["fts7:tick"]}')
write('data/fts7/functions/load.mcfunction', 'scoreboard objectives add fts7_enter trigger\nscoreboard objectives add fts7_return trigger\n')
write('data/fts7/functions/install.mcfunction', 'execute unless data storage fts7:installation installed run function fts7:install_once\n')
commands = ['execute in minecraft:overworld run forceload add 10000 10000 10325 10150']
clearance = (parent / 'assembly/clearance/clear-entire-bounds.mcfunction').read_text(encoding='utf-8').splitlines()
commands += ['execute in minecraft:overworld positioned 10000 80 10000 run ' + line for line in clearance]
structures = pack / 'data/fts7/structures'
structures.mkdir(parents=True, exist_ok=True)
for index, part in enumerate(assembly['parts']):
    candidate = Path(part['path'])
    assert hashlib.sha256(candidate.read_bytes()).hexdigest() == part['sha256']
    shutil.copyfile(candidate, structures / f'part_{index:02}.nbt')
    east, up, south = part['origin']
    commands.append(f'execute in minecraft:overworld run place template fts7:part_{index:02} {10000+east} {80+up} {10000+south} none none 1.0 0')
commands += ['execute in minecraft:overworld run fill 9990 84 10026 9994 84 10030 minecraft:stone_bricks', 'execute in minecraft:overworld run fill 9990 85 10026 9994 88 10030 minecraft:air', 'data modify storage fts7:installation installed set value 1b', 'execute in minecraft:overworld run forceload remove 10000 10000 10325 10150']
write('data/fts7/functions/install_once.mcfunction', '\n'.join(commands) + '\n')
write('data/fts7/functions/tick.mcfunction', '\n'.join(['execute if data storage fts7:installation installed in minecraft:overworld positioned 9992.5 85 10028.5 run scoreboard players enable @a[distance=..1.5] fts7_enter', 'execute if data storage fts7:installation installed in minecraft:overworld positioned 10014.5 85 10028.5 run scoreboard players enable @a[distance=..1.5] fts7_return', 'execute if data storage fts7:installation installed in minecraft:overworld positioned 9992.5 85 10028.5 as @a[distance=..1.5,scores={fts7_enter=1..}] run function fts7:enter', 'execute if data storage fts7:installation installed in minecraft:overworld positioned 10014.5 85 10028.5 as @a[distance=..1.5,scores={fts7_return=1..}] run function fts7:return', 'scoreboard players set @a[scores={fts7_enter=1..}] fts7_enter 0', 'scoreboard players set @a[scores={fts7_return=1..}] fts7_return 0']) + '\n')
write('data/fts7/functions/enter.mcfunction', 'scoreboard players set @s fts7_enter 0\nexecute in minecraft:overworld run tp @s 10014.5 85 10028.5 -90 0\n')
write('data/fts7/functions/return.mcfunction', 'scoreboard players set @s fts7_return 0\nexecute in minecraft:overworld run tp @s 9992.5 85 10028.5 90 0\n')
manifest = {'dimension': 'minecraft:overworld', 'anchor': [10000, 80, 10000], 'rotation': 'none', 'mirror': 'none', 'size': assembly['size'], 'whole_clear_volume': 1969040, 'clear_commands': len(clearance), 'part_count': len(assembly['parts']), 'part_envelope_volume': 1324800, 'candidate_sha256': assembly['sha256'], 'barriers': 'All 60115 retained; travel across enclosure by gated teleport, no barrier holes', 'outside_pad': {'feet': [9992.5,85,10028.5], 'floor_from': [9990,84,10026], 'floor_to': [9994,84,10030], 'air_from': [9990,85,10026], 'air_to': [9994,88,10030]}, 'inside_feet': [10014.5,85,10028.5], 'permissions': 'Only operator function install. Player triggers enabled only within radius 1.5 of designated pads; scoring elsewhere reset without teleport. Generic function enter/return requires operator command privileges. No automatic load install.', 'installation_safety': 'Fixed anchor; operator must reserve complete 326x40x151 box plus outside pad. Full air clear deletes all blocks/BEs there; no entity cleanup. New empty reserved area required to avoid existing entities. installed storage prevents repeated execution. This is a new offline package, not production permission.', 'deployment': 'NOT_DEPLOYED; package tests pending', 'files': [{'path': str(path.relative_to(pack)), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest()} for path in sorted(pack.rglob('*')) if path.is_file()]}
(root / 'installation/install-plan.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding='utf-8')
shutil.make_archive(str(root / 'installation/fts7-datapack'), 'zip', pack)
print(json.dumps({'pack': str(pack), 'files': len(manifest['files']), 'parts': 15, 'whole_clear_volume': 1969040, 'executed': False}))
