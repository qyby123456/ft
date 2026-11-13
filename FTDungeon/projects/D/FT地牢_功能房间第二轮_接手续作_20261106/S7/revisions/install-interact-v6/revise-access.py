import json
import shutil
import sys
from pathlib import Path

root = Path(__file__).resolve().parent
parent = root.parent / 'local-relief-v4'
sys.dont_write_bytecode = True
sys.path.insert(0, str(parent))
from qa import Structure, save
sys.path.insert(0, r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\output\dungeon-expansion-20261101\tools')
from nbtio import state

for number in range(11,21):
    room = root / f'S7-{number}'
    if not room.exists():
        shutil.copytree(parent / room.name, room)
    summary = json.loads((room / 'summary.json').read_text(encoding='utf-8'))
    source = parent / room.name / Path(summary['nbtPath']).name
    destination = room / source.name
    if number in [15,16]:
        structure = Structure.read(source)
        if number == 15:
            additions = [((28,3,14),state('dark_oak_slab',type='bottom',waterlogged=False)), ((29,3,14),state('dark_oak_planks')), ((31,3,22),state('dark_oak_planks')), ((30,3,22),state('dark_oak_slab',type='bottom',waterlogged=False))]
        else:
            additions = [((east,5,south),state('dark_oak_planks')) for east in range(19,23) for south in [18,19]]
        for position, block in additions:
            assert structure.get(position).name == 'minecraft:air'
            structure.set(position,block)
        structure.write(destination)
        design = json.loads((parent / room.name / 'design.json').read_text(encoding='utf-8'))
        if number == 15:
            design['targets'] += [{'label':'beam first step','position':[28,3.5,14]}, {'label':'beam upper step','position':[29,4,14]}, {'label':'hurdle approach step','position':[31,4,22]}]
        else:
            design['targets'] += [{'label':'hanging barrel service deck','position':[20,6,18]}]
        design['revision_6'] = {'reason':'Natural input ticks showed two-block training logs need approaches; first-hit eye search found suspended barrel unreachable. Add access only; all original goals and furniture retained.', 'added_blocks':[list(position) for position,block in additions]}
        save(room / 'design.json',design)
        recipe = (parent / room.name / 'recipe.mjs').read_text(encoding='utf-8')
        snippet = " put([28,3,14],'dark_oak_slab',{type:'bottom',waterlogged:'false'});put([29,3,14],'dark_oak_planks');put([31,3,22],'dark_oak_planks');put([30,3,22],'dark_oak_slab',{type:'bottom',waterlogged:'false'})\n" if number == 15 else " box([19,5,18],[22,5,19],'dark_oak_planks')\n"
        recipe = recipe.replace(' const ports=',snippet+' const ports=')
        (room / 'recipe.mjs').write_text(recipe,encoding='utf-8')
    summary['nbtPath'] = str(destination)
    save(room / 'summary.json',summary)
for helper in ['common.mjs','qa.py','revise.py','render.cjs','assemble.py','routecoverage.py','clearance.py','reproduce.py','continuous.py','ContinuousConnectorAudit.java']:
    if not (root / helper).exists():
        shutil.copyfile(parent / helper,root / helper)
print('15 and 16 access revised; all other room candidates exact parent copies')
