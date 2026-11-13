import json
import pathlib
import sys
sys.path.insert(0,r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\output\dungeon-expansion-20261101\tools')
from nbtio import Structure,Tag,compound,string,integer,intlist,state

folder=pathlib.Path(sys.argv[1])
if folder.name=='14-armory':
    candidate=next(folder.glob('*.nbt'))
    structure=Structure.read(candidate)
    entities=[]
    for east in [8,21]:
        for x in range(east-1,east+2):
            for y in range(2,7):
                structure.set((x,y,5),state('air'))
        equipment=[compound(id=string('minecraft:iron_'+part),Count=Tag(1,1)) for part in ['boots','leggings','chestplate','helmet']]
        entity=compound(id=string('minecraft:armor_stand'),Pos=Tag(9,[Tag(6,float(east)+0.5),Tag(6,2.0),Tag(6,5.5)],6),Rotation=Tag(9,[Tag(5,0.0),Tag(5,0.0)],5),ArmorItems=Tag(9,equipment,10),HandItems=Tag(9,[compound(id=string('minecraft:iron_sword'),Count=Tag(1,1)),compound()],10),ShowArms=Tag(1,1),NoGravity=Tag(1,1),Invulnerable=Tag(1,1),DisabledSlots=integer(4144959))
        entities.append(compound(pos=Tag(9,[Tag(6,float(east)+0.5),Tag(6,2.0),Tag(6,5.5)],6),blockPos=intlist([east,2,5]),nbt=entity))
    structure.entities=entities
    structure.write(candidate)
    (folder/'equipment-evidence.json').write_text(json.dumps({'entities':2,'entity_id':'minecraft:armor_stand','positions':[[8.5,2,5.5],[21.5,2,5.5]],'ArmorItems':['iron_boots','iron_leggings','iron_chestplate','iron_helmet'],'HandItems':['iron_sword'],'limitations':'NBT payload inspected; entity renderer not supported in Lodestone preview; actual world rendering and interaction pending'},indent=2),encoding='utf-8')
