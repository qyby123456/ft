import hashlib
import json
import subprocess
from pathlib import Path
from audit_s8_submission import load

workspace = Path('C:/Users/YOUR_USERNAME/.codex/visualizations/2026/11/04/01a1a685-045b-76d2-aa2b-2a3f24520603')
root = workspace / 'preserved-review-snapshots/S8-interaction-revision-20261107-1355'
current = root / 'access-revision-r4'
destination = workspace / 's8-independent-revision-recipes-20261107-1600'
assert b'ENOENT' in (destination / 'console.log').read_bytes()
(destination / 'deployment-pack/data/s8_takeover/structures/parts').mkdir(parents=True, exist_ok=True)
process = subprocess.run(['C:/Program Files/nodejs/node.exe', str(destination / 'build.mjs')], capture_output=True, timeout=240)
(destination / 'recovery-console.log').write_bytes(process.stdout + process.stderr)
assert process.returncode == 0, process.stderr.decode('utf-8', errors='replace')
outputs = [current / 'assembly/s8-functional-access-revision.nbt', *sorted((current / 'assembly/parts').glob('*.nbt')), *sorted(current.glob('S8-*/*.nbt'))]
assert len(outputs) == 31
for source in outputs:
    assert source.read_bytes() == (destination / source.relative_to(current)).read_bytes(), source
for item in load(root / 'submission-snapshot.json')['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
result = dict(accepted=False, status='INDEPENDENT_CURRENT31NBT_BINARY_EXACT_FROM_PRIOR_PARENT_RECIPES_AND29PATCHES', source_sha256=load(current / 'REVISION.json')['sha256'], outputs=31, directory=str(destination), all31_binary_equal=True, current_candidate_nbt_used_as_recipe_input=False, only_input_path_relocated=True, recovery='Initial NBT build completed but package copy failed because captured original deployment-plan contains only plan.json. Created missing output package parts directory and repeated same build. Initial failure log preserved. Package function verification is separate.')
(root / 'independent-current-reproduction.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps(result, ensure_ascii=False), flush=True)
