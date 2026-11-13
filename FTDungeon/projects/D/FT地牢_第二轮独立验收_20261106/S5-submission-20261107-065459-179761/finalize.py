import hashlib
import json
from pathlib import Path

root = Path(__file__).resolve().parent
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding='utf-8')
references = json.loads((root/'references.json').read_text(encoding='utf-8-sig'))
references['output_directory'] = str(root)
for reference in references['references']:
    reference['prompt_sha256'] = digest(Path(reference['prompt_path']))
    assert digest(Path(reference['image_path'])) == reference['sha256']
save(root/'references.json', references)
rooms = []
for reference in references['references']:
    identifier = reference['id']
    folder = root/identifier
    candidate = folder/(identifier.lower()+'.nbt')
    report = json.loads((folder/'qa/self-check.json').read_text(encoding='utf-8'))
    render = json.loads((folder/'qa/render.json').read_text(encoding='utf-8'))
    sha256 = digest(candidate)
    rooms.append({'id':identifier,'candidate':str(candidate),'sha256':sha256,'self_check_passed':report['sha256']==sha256 and report['self_check_passed'],'render_bound':render['sha256']==sha256,'recipe':str(folder/'recipe.mjs'),'reference':reference,'independent_acceptance':False})
save(root/'candidate-index.json', rooms)
save(root/'progress.json', {'group':'S5','current':'merged','completed':[room['id'] for room in rooms if room['self_check_passed'] and room['render_bound']],'merged':False,'independent_acceptance':False,'source_manifest':'inheritance-source-manifest.json','pending':['world natural ticks and door cycles','merged generation QA renders','parts reconstruction including payload','independent visual and technical review'],'limitations':['no ocean flooded placement','no oxygen depletion simulation','no survival player test']})
print(json.dumps(rooms,ensure_ascii=False))
