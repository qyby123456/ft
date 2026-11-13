import hashlib
import json
import sys
from pathlib import Path

sys.dont_write_bytecode = True
sys.path.insert(0,str(Path(__file__).resolve().parent))
from qa import ROOT, Structure, save
from nbtio import Tag, string, integer

def main():
    number=int(sys.argv[1])
    room=Path(sys.argv[2]).resolve() if len(sys.argv)>2 else ROOT/f'S7-{number}'
    summary=json.loads((room/'summary.json').read_text(encoding='utf-8'))
    candidate=Path(summary['nbtPath'])
    structure=Structure.read(candidate)
    from nbtio import state
    for position,block in list(structure.blocks.items()):
        if block.name=='minecraft:red_mushroom':
            east,up,south=position
            structure.set((east,up-1,south),state('mycelium',snowy=False))
    structure.write(candidate)
    if number==14:
        structure=Structure.read(candidate)
        for east,metal in [(16,'iron'),(20,'golden'),(27,'iron')]:
            south=6 if east!=27 else 12
            for dx in [-1,0,1]:
                for up in [4,5,6,7]:
                    if east!=27:
                        structure.set((east+dx,up,south),__import__('nbtio').AIR)
            position=(east+0.5,4 if east!=27 else 3,south+0.5)
            armor=[]
            for slot in ['boots','leggings','chestplate','helmet']:
                armor.append(Tag(10,{'id':string(f'minecraft:{metal}_{slot}'),'Count':Tag(1,1)}))
            nbt=Tag(10,{'id':string('minecraft:armor_stand'),'Pos':Tag(9,[Tag(6,value) for value in position],6),'Rotation':Tag(9,[Tag(5,0.0),Tag(5,0.0)],5),'ArmorItems':Tag(9,armor,10),'HandItems':Tag(9,[Tag(10,{'id':string('minecraft:iron_sword'),'Count':Tag(1,1)}),Tag(10,{})],10),'NoGravity':Tag(1,1),'Invulnerable':Tag(1,1),'ShowArms':Tag(1,1),'DisabledSlots':integer(4144959)})
            structure.entities.append(Tag(10,{'pos':Tag(9,[Tag(6,value) for value in position],6),'blockPos':Tag(9,[integer(int(value)) for value in position],3),'nbt':nbt}))
        structure.write(candidate)
        summary['postprocess']='python -X utf8 revise.py 14: three equipped vanilla armor_stand entities; Lodestone does not render structure entities'
        save(room/'summary.json',summary)
    design=json.loads((room/'design.json').read_text(encoding='utf-8'))
    design['revision_2']={'reason':'Independent visual preliminary REQUEST_CHANGES','preserved_evidence':str(ROOT/'history/pre-visual-request-changes'/room.name),'changes':['Continuous irregular floor patches replace per-cell checkerboard','Wave-based clustered cave palette, projecting upper ledges, supported hanging roots and mushrooms','Stone-and-log layered frames at unchanged clear ports','Feature-detail render supplements overview; no targets removed']}
    if number==14:
        design['differences']=[item for item in design['differences'] if '盔甲' not in item]
        design['differences'].append('三座实际装备盔甲架实体（铁/金/铁），带铁剑；Lodestone结构渲染不显示实体，以NBT和游戏世界放置验证。末地烛仅为墙面兵器架轮廓。')
    if number==19:
        design['differences']=[item for item in design['differences'] if '大洞景' not in item]
        design['differences'].append('左侧观察窗清除遮挡平墙，透明玻璃外为有菌菇的封闭景深洞龛；不是无限洞穴。柱间石墙栏杆保留防坠。')
    if number==20:
        design['revision_2']['changes'].append('Vault roof stepped with mycelium crown and red mushrooms; explicit display-only vault roof cut plus full vault near-view')
    save(room/'design.json',design)
    print(number,hashlib.sha256(candidate.read_bytes()).hexdigest())

if __name__=='__main__':
    main()
