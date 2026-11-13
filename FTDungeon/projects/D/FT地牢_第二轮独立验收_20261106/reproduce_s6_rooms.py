import hashlib
import json
import shutil
import subprocess
import sys
from pathlib import Path


root = Path(sys.argv[1]).resolve()
scratch = Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603\s6-independent-recipes-20261107-1024')
scratch.mkdir()
shutil.copy2(root / 'lib.mjs', scratch / 'lib.mjs')
manifest = json.loads((root / 'ready-for-review.json').read_text(encoding='utf-8-sig'))
results = []
for room in manifest['rooms']:
    original = Path(room['candidate'])
    folder_name = original.parent.name
    folder = scratch / folder_name
    folder.mkdir()
    shutil.copy2(root / folder_name / 'recipe.mjs', folder / 'recipe.mjs')
    command = [r'C:\Program Files\nodejs\node.exe', r'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill\scripts\run-build.mjs', str(folder / 'recipe.mjs'), '--out-dir', str(folder / 'output'), '--data-version', '3465']
    with (folder / 'console.log').open('wb') as output:
        subprocess.run(command, cwd=scratch, stdout=output, stderr=subprocess.STDOUT, check=True)
    produced = list((folder / 'output').glob('*.nbt'))
    assert len(produced) == 1
    candidate = root / folder_name / original.name
    assert produced[0].read_bytes() == candidate.read_bytes()
    digest = hashlib.sha256(candidate.read_bytes()).hexdigest()
    assert digest == room['sha256']
    results.append(dict(id=room['id'], sha256=digest, binary_equal=True, output=str(produced[0]), command=command))
    print(room['id'] + ' byte-exact', flush=True)
report = dict(status='PASS', stage='PARENT_INDEPENDENT_ALL_TEN_ROOM_RECIPE_EXECUTION', copied_candidate_nbt_as_generation_input=False, scratch=str(scratch), results=results, accepted=False)
(root / 'independent-room-reproduction.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
print('All ten original room recipes byte-exact; merged reproduction inherited separately', flush=True)
