import hashlib
import json
import shutil
import subprocess
import sys
from pathlib import Path

from audit_s8_submission import load

workspace=Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603')
root=workspace/'preserved-review-snapshots/S3-access-revision-20261107-1103'
prior=Path(__file__).parent/'S3-submission-20261107-072954-274564'
destination=workspace/'s3-independent-recipes-access-20261107-1120'
destination.mkdir()
for name in ('common.mjs','room16.mjs','assemble.mjs','split-verify.mjs'):
    shutil.copyfile(root/name,destination/name)
for number in range(11,21):
    identifier=f's3-{number}'
    folder=destination/'rooms'/identifier
    folder.mkdir(parents=True)
    if number!=16:
        rebuilt=prior/'independent-reproduction/rooms'/identifier/(identifier+'.nbt')
        assert rebuilt.read_bytes()==(root/'rooms'/identifier/(identifier+'.nbt')).read_bytes()
        shutil.copyfile(rebuilt,folder/rebuilt.name)
    else:
        command=[r'C:\Program Files\nodejs\node.exe',r'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill\scripts\run-build.mjs',str(destination/'room16.mjs'),'--out-dir',str(folder)]
        process=subprocess.run(command,capture_output=True,timeout=180)
        (destination/'room16.log').write_bytes(process.stdout+process.stderr)
        assert process.returncode==0
        assert (folder/'s3-16.nbt').read_bytes()==(root/'rooms/s3-16/s3-16.nbt').read_bytes()
    for name in ('design.json','qa/report.json','qa/visual-review.json'):
        target=folder/name;target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(root/'rooms'/identifier/name,target)
merged_folder=destination/'rooms/s3-merged'
command=[r'C:\Program Files\nodejs\node.exe',r'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill\scripts\run-build.mjs',str(destination/'assemble.mjs'),'--out-dir',str(merged_folder),'--no-block-state-updates']
process=subprocess.run(command,capture_output=True,timeout=180)
(destination/'assembly.log').write_bytes(process.stdout+process.stderr)
assert process.returncode==0
assert (merged_folder/'s3-merged.nbt').read_bytes()==(root/'rooms/s3-merged/s3-merged.nbt').read_bytes()
shutil.copyfile(merged_folder/'design.json',destination/'assembly.json')
(merged_folder/'qa').mkdir(exist_ok=True)
process=subprocess.run([r'C:\Program Files\nodejs\node.exe',str(destination/'split-verify.mjs')],cwd=destination,capture_output=True,timeout=180)
(destination/'parts.log').write_bytes(process.stdout+process.stderr)
assert process.returncode==0,process.stderr.decode('utf-8',errors='replace')
parts=load(root/'parts/manifest.json')['parts']
for part in parts:
    name=Path(part['path']).name
    assert (destination/'parts'/name).read_bytes()==(root/'parts'/name).read_bytes()
for item in load(root/'submission-snapshot.json')['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest()==item['sha256']
result={'accepted':False,'status':'PARENT_CURRENT16_MERGED25PARTS_EXACT_REPRODUCTION','directory':str(destination),'room16_generated_from_recipe':True,'other_nine_rooms_from_prior_parent_recipe_outputs_verified_exact':True,'current16_and_merged_nbt_not_used_as_generation_input':True,'merged_binary_equal':True,'parts_binary_equal':25,'source_sha256':hashlib.sha256((merged_folder/'s3-merged.nbt').read_bytes()).hexdigest()}
(root/'independent-reproduction.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(result,ensure_ascii=False))
