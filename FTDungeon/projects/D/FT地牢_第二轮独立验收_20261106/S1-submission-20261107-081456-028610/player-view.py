import json
import pathlib

ROOT=pathlib.Path(__file__).parent
path=ROOT/'merged'/'design.json'
design=json.loads(path.read_text(encoding='utf-8'))
design['player_views']=[{'id':'player-watch-forest','position':[402.5,15.62,83.5],'target':[409,18,25],'fov':72}]
path.write_text(json.dumps(design,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
