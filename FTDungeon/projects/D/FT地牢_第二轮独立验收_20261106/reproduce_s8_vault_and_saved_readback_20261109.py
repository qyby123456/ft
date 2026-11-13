import hashlib
import json
import shutil
import subprocess
from pathlib import Path
from audit_s8_submission import load

workspace = Path('C:/Users/YOUR_USERNAME/.codex/visualizations/2026/11/04/01a1a685-045b-76d2-aa2b-2a3f24520603')
root = workspace/'preserved-review-snapshots/S8-vault-shell-revision-20261108'
prior = workspace/'preserved-review-snapshots/S8-interaction-revision-20261107-1355/access-revision-r4'
generated_prior = workspace/'s8-independent-revision-recipes-20261107-1600'
destination = workspace/'s8-independent-vault-recipes-20261109'
assert not destination.exists()
(destination/'access-revision-r4/assembly').mkdir(parents=True)
baseline = load(prior/'REVISION.json')
baseline['candidate'] = str(generated_prior/'assembly/s8-functional-access-revision.nbt')
assert hashlib.sha256(Path(baseline['candidate']).read_bytes()).hexdigest() == baseline['sha256']
for module in baseline['modules']:
    source = generated_prior/module['id']/Path(module['source']).name
    assert hashlib.sha256(source.read_bytes()).hexdigest() == module['sha256']
    module['source'] = str(source)
(destination/'access-revision-r4/REVISION.json').write_text(json.dumps(baseline,ensure_ascii=False,indent=2),encoding='utf-8')
shutil.copyfile(prior/'assembly/layout.json',destination/'access-revision-r4/assembly/layout.json')
shutil.copytree(prior/'deployment-pack',destination/'access-revision-r4/deployment-pack')
output = destination/'new-vault'
output.mkdir()
shutil.copyfile(root/'build.mjs',output/'build.mjs')
with (destination/'console.log').open('w',encoding='utf-8') as log:
    process = subprocess.run(['C:/Program Files/nodejs/node.exe',str(output/'build.mjs')],stdout=log,stderr=subprocess.STDOUT,timeout=240)
assert process.returncode == 0
files = [root/'assembly/s8-functional-vault-shell.nbt',*sorted(root.glob('S8-*/*.nbt')),*sorted((root/'assembly/parts').glob('*.nbt'))]
assert len(files) == 31
for source in files:
    assert source.read_bytes() == (output/source.relative_to(root)).read_bytes(),source
report = dict(status='PASS31_BINARY_EXACT_FROM_PREVIOUS_PARENT_RECIPES_AND_FROZEN_DELTA_RECIPE',accepted=False,source_sha256=load(root/'REVISION.json')['sha256'],outputs=31,all31_binary_equal=True,current_candidate_used_as_recipe_input=False,baseline_source='previous parent-thread independently reproduced 6988 build',build_script_unchanged=True,directory=str(destination))
(root/'independent-current-reproduction.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(report,ensure_ascii=False),flush=True)
runtime = workspace/'s8-independent-vault-install-20261109'
assert load(root/'independent-install-run.json')['status'] == 'FINISHED' and load(root/'independent-install-run.json')['exit_code'] == 0
readback = workspace/'s8-independent-vault-saved-readback-20261109'
assert not readback.exists()
readback.mkdir()
shutil.copyfile(root/'saved-readback/SavedReadback.java',readback/'SavedReadback.java')
classpath = (root/'assembly/qa/classpath.txt').read_text(encoding='utf-8').strip()
commands = [(['javac','-encoding','UTF-8','-cp',classpath,'-d',str(readback),str(readback/'SavedReadback.java')],'compile.log'),(['java','-Xmx1536m','-cp',str(readback)+';'+classpath,'SavedReadback',str(runtime),str(readback)],'console.log')]
for command,filename in commands:
    with (readback/filename).open('w',encoding='utf-8') as log:
        process = subprocess.run(command,stdout=log,stderr=subprocess.STDOUT,timeout=240)
    assert process.returncode == 0, filename
saved = load(readback/'saved-readback.json')
assert saved['pass'] and saved['source_sha256'] == report['source_sha256'] and saved['chunks_read'] == 150
assert saved['full_volume_checked'] == 1009200 and not saved['failures']
(root/'independent-persisted-readback.json').write_text(json.dumps(dict(status='PASS_NATIVE_SAVED_CHUNKS',source_sha256=report['source_sha256'],directory=str(readback),report=saved),ensure_ascii=False,indent=2),encoding='utf-8')
print('INDEPENDENT_SAVED_READBACK_PASS',flush=True)
