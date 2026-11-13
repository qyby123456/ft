import hashlib
import json
import pathlib
import shutil
import zipfile

ROOT = pathlib.Path(__file__).parent
OUT = ROOT / 'access-package'
PACK = OUT / 's1_access_1_20_1'
FUNCTIONS = PACK / 'data/s1/functions'
FUNCTIONS.mkdir(parents=True, exist_ok=True)

def write(relative, text):
    path = PACK / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text.rstrip() + '\n', encoding='utf-8')

def function(name, lines):
    write('data/s1/functions/' + name + '.mcfunction', '\n'.join(lines))

write('pack.mcmeta', json.dumps({'pack': {'pack_format': 15, 'description': 'S1 controlled entry/return; reserved overworld site 10000 64 10000'}}, ensure_ascii=False))
write('data/minecraft/tags/functions/load.json', '{"values":["s1:load"]}')
write('data/minecraft/tags/functions/tick.json', '{"values":["s1:tick"]}')
function('load', ['scoreboard objectives add s1gate trigger', 'scoreboard objectives add s1_sys dummy'])
chunks = json.loads((ROOT / 'merged/integrity.json').read_text(encoding='utf-8'))['chunks']
install = ['execute in minecraft:overworld run forceload add 9992 9998 10239 10139', 'execute in minecraft:overworld run forceload add 10240 9998 10493 10139']
for east in range(9992, 10494, 16):
    for south in range(9998, 10140, 32):
        install.append(f'execute in minecraft:overworld run fill {east} 61 {south} {min(east+15,10493)} 105 {min(south+31,10139)} minecraft:air')
bindings = []
for index, chunk in enumerate(chunks):
    source = pathlib.Path(chunk['file'])
    name = f'part_{index:02d}'
    target = PACK / f'data/s1/structures/{name}.nbt'
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(source, target)
    offset = chunk['origin']
    install.append(f'execute in minecraft:overworld run place template s1:{name} {10000+offset[0]} {64+offset[1]} {10000+offset[2]} none none 1.0 0')
    bindings.append({'resource': 's1:' + name, 'offset': offset, 'source': str(source), 'sha256': hashlib.sha256(target.read_bytes()).hexdigest()})
install += ['execute in minecraft:overworld run fill 9992 61 10123 9998 61 10132 minecraft:stone_bricks', 'execute in minecraft:overworld run fill 9993 69 10126 9998 69 10132 minecraft:stone_bricks', 'execute in minecraft:overworld run fill 9994 62 10124 9994 69 10124 minecraft:stone_bricks', 'execute in minecraft:overworld run fill 9994 62 10125 9994 69 10125 minecraft:ladder[facing=south]', 'scoreboard players set #installed s1_sys 1', 'function s1:release_chunks', 'tellraw @a {"text":"S1 installed at 10000 64 10000. Ground reception: 9995.5 62 10129.5; /trigger s1gate set 1; inside return: /trigger s1gate set 2"}']
function('install', install)
function('release_chunks', ['execute in minecraft:overworld run forceload remove 9992 9998 10239 10139', 'execute in minecraft:overworld run forceload remove 10240 9998 10493 10139'])
function('enter', ['tag @s add s1_guest', 'tp @s 10005.5 70 10129.5 90 0'])
function('return', ['tp @s 9995.5 70 10129.5 -90 0', 'tag @s remove s1_guest'])
pad = 'x=9992,y=62,z=10123,dx=6,dy=11,dz=9'
inside = 'x=10000,y=64,z=10000,dx=491,dy=39,dz=137'
function('tick', [
    f'execute if score #installed s1_sys matches 1 in minecraft:overworld as @a[{pad}] run scoreboard players enable @s s1gate',
    f'execute if score #installed s1_sys matches 1 in minecraft:overworld as @a[{inside}] run scoreboard players enable @s s1gate',
    f'execute if score #installed s1_sys matches 1 as @a[scores={{s1gate=1}}] at @s if dimension minecraft:overworld if entity @s[{pad}] run function s1:enter',
    f'execute if score #installed s1_sys matches 1 as @a[scores={{s1gate=2}}] at @s if dimension minecraft:overworld if entity @s[{inside}] run function s1:return',
    'scoreboard players set @a[scores={s1gate=1..}] s1gate 0'
])
function('disable', ['scoreboard players set #installed s1_sys 0', 'function s1:release_chunks'])
manifest = {'candidate_sha256': hashlib.sha256((ROOT/'merged/s1-mossy-functional-11-20.nbt').read_bytes()).hexdigest(), 'dimension': 'minecraft:overworld', 'origin': [10000,64,10000], 'clear_inclusive': {'min': [9992,61,9998], 'max': [10493,105,10139]}, 'external_ground_feet': [9995.5,62,10129.5], 'external_pad_feet': [9995.5,70,10129.5], 'interior_feet': [10005.5,70,10129.5], 'external_access': 'Ground foundation at feet y=62 accepts entry trigger; vanilla backed ladder connects it to return platform at feet y=70.', 'chunks': bindings, 'automatic_install': False, 'warning': 'INSTALL CLEARS THE ENTIRE DECLARED BOX. Reserve this empty area; never run install on an occupied site. No formal world has been modified.', 'runtime_loading': 'Install temporarily forces chunks then releases them. Subsequent player entry uses vanilla teleport chunk loading and ordinary server ticks.'}
(OUT/'installation-plan.json').write_text(json.dumps(manifest, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
with zipfile.ZipFile(OUT/'s1_access_1_20_1.zip','w',zipfile.ZIP_DEFLATED) as archive:
    for path in sorted(PACK.rglob('*')):
        if path.is_file():
            archive.write(path, path.relative_to(PACK).as_posix())
print('Built installable datapack; 33 original chunks; explicit clearance plan; no world changed.')
