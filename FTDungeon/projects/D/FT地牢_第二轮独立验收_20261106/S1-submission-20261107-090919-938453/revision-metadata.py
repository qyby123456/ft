import json
import pathlib

ROOT=pathlib.Path(__file__).parent
path=ROOT/'19-watch'/'design.json'
value=json.loads(path.read_text(encoding='utf-8'))
value['revision']='R3 independent review: clear north window with wood/stone reveal, layered forest sidecar delivered in merged package; original platform/routes retained'
value['differences'].append('North viewing window is clear glass with an embedded wood/stone reveal; 64-block deep scenery sidecar is physically included by merge.py. Standalone room requires sidecar placement to show scenery.')
value['scenery']={'recipe':str(ROOT/'scenery.mjs'),'candidate':str(ROOT/'scenery'/'s1-19-forest-depth.nbt'),'merged_offset':[394,4,14],'room_merged_offset':[394,4,78],'player_feet_local':[8.5,10,5.5],'eye_height':1.62,'view_direction':'north','source_geometry_not_cut_for_player_view':True}
path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
