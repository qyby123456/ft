import json
import pathlib
ROOT=pathlib.Path(__file__).parent
focus={
'11-library':[[27,9,14],[22,6,3]],
'12-prison':[[15,10,20],[9,4,16]],
'13-kitchen':[[24,11,18],[16,5,4]],
'14-armory':[[25,10,16],[18,4,6]],
'15-training':[[30,11,30],[23,3,24]],
'16-cistern':[[25,13,26],[14,7,14]],
'17-workshop':[[16,9,13],[7,3,4]],
'18-crypt':[[25,13,15],[16,9,3]],
'19-watch':[[32,16,30],[20,6,20]],
'20-vault':[[27,10,18],[21,5,4]]}
for name,(position,target) in focus.items():
    file=ROOT/name/'design.json'
    design=json.loads(file.read_text(encoding='utf-8'))
    design['focus_views']=[{'id':'focus','position':position,'target':target,'fov':53},{'id':'function-detail','position':[target[0]+5,target[1]+4,target[2]+8],'target':target,'fov':48}]
    design['revision']='R2: independent visual/support changes; targets retained'
    if name=='14-armory':
        design['differences']=[entry for entry in design['differences'] if not entry.startswith('Armor figures')]
        design['differences'].append('Two iron-equipped armor_stand entities in NBT, entity preview unsupported by Lodestone; world rendering pending')
    file.write_text(json.dumps(design,ensure_ascii=False,indent=2),encoding='utf-8')
