import hashlib
import json
import shutil
import subprocess
from pathlib import Path
from audit_s8_submission import load

workspace = Path('C:/Users/YOUR_USERNAME/.codex/visualizations/2026/11/04/01a1a685-045b-76d2-aa2b-2a3f24520603')
current = workspace / 'preserved-review-snapshots/S8-interaction-revision-20261107-1355/access-revision-r4'
prior = workspace / 'preserved-review-snapshots/S8-submission-20261107-104351-507543'
destination = workspace / 's8-independent-visual-review-20261107-1600'
destination.mkdir()
shutil.copyfile(current / 'render-access.cjs', destination / 'render.cjs')
records = []
for identifier in ('S8-13', 'S8-16', 'S8-19', 'S8-20'):
    folder = destination / identifier
    folder.mkdir()
    source = next((current / identifier).glob('*.nbt'))
    digest = hashlib.sha256(source.read_bytes()).hexdigest()
    shutil.copyfile(source, folder / source.name)
    if identifier in ('S8-13', 'S8-16'):
        cameras = {name: {} for name in ('complete', 'cutaway', 'interior')}
        cameras.update({view['mode']: view['camera'] for view in load(current / identifier / 'render-evidence.json')['views']})
        (folder / 'cameras.json').write_text(json.dumps(cameras), encoding='utf-8')
        command = ['C:/Program Files/nodejs/node.exe', str(destination / 'render.cjs'), str(folder), str(folder / 'cameras.json')]
        process = subprocess.run(command, capture_output=True, timeout=360)
        (folder / 'render-console.log').write_bytes(process.stdout + process.stderr)
        assert process.returncode == 0, process.stderr.decode('utf-8', errors='replace')
    else:
        assert source.read_bytes() == next((prior / identifier).glob('*.nbt')).read_bytes()
        for path in (prior / identifier).glob('*.png'):
            shutil.copyfile(path, folder / path.name)
        shutil.copyfile(prior / identifier / 'render-evidence.json', folder / 'render-evidence.json')
    assert load(folder / 'render-evidence.json')['sha256'] == digest
    reference = next(row for row in load(prior / 'references.json')['references'] if row['id'] == identifier)
    image = Path(reference['image_path'])
    assert hashlib.sha256(image.read_bytes()).hexdigest() == reference['sha256']
    shutil.copyfile(image, folder / 'reference.png')
    shutil.copyfile(reference['prompt_path'], folder / 'reference-prompt.txt')
    records.append(dict(id=identifier, candidate_sha256=digest, reference_sha256=reference['sha256'], rendered_current_source=True, independent_new_render=identifier in ('S8-13', 'S8-16'), visual_inspected=False))
    print(identifier, 'SOURCE_BOUND_IMAGES_READY_NO_VISUAL_APPROVAL', flush=True)
result = dict(accepted=False, source_sha256=load(current / 'REVISION.json')['sha256'], rooms=records, limitation='view_image returned success with no image content; source-bound renders are available, but no visual-quality observation or approval is claimed')
(destination / 'visual-manifest.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
