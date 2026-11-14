import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
CHECKOUT=ROOT/'github-sync/repositories/ft'
PREFIX='neoforge/ftboss'
OUT=WORK/'reports/github-ft-source-sync-v1'
def sha(data):return hashlib.sha256(data).hexdigest()
def save(path,value):
    with path.open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
parser=argparse.ArgumentParser();parser.add_argument('source_manifest',type=Path);args=parser.parse_args();manifest_path=args.source_manifest.resolve();assert manifest_path.is_relative_to((WORK/'reports').resolve());manifest=json.loads(manifest_path.read_text(encoding='utf-8'));assert not OUT.exists();OUT.mkdir()
environment=dict(os.environ);environment.update({'GIT_TERMINAL_PROMPT':'0','GCM_INTERACTIVE':'Never'});hooks=WORK/'reports/github-ft-remote-review-v1/empty-hooks'
def git(arguments):
    result=subprocess.run(['git','-c','core.hooksPath='+str(hooks),'-c','core.autocrlf=false','-C',str(CHECKOUT),*arguments],env=environment,capture_output=True);assert result.returncode==0,arguments;return result.stdout
base='27d20d5e1d2eb960f4a64cde9bd21230efae766c';assert git(['rev-parse','HEAD']).decode().strip()==base and git(['branch','--show-current']).decode().strip()=='codex/neoforge-source-20261113';readme=git(['show','HEAD:README.md']);assert (CHECKOUT/'README.md').read_bytes()==readme
extra=CHECKOUT/PREFIX/'github-sync/import-manifest-ft-20261113.json';previous_bytes=extra.read_bytes();previous=json.loads(previous_bytes);(OUT/'previous-import-manifest.json').write_bytes(previous_bytes);new={row['path']:row for row in manifest['files']};assert set(previous['sourceSnapshot']).issubset(new),'Do not silently delete old imported source while remote recovery is unverified'
updated=0
for relative,row in new.items():
    source=(ROOT/relative).resolve();target=(CHECKOUT/PREFIX/relative).resolve();assert source.is_relative_to(ROOT) and target.is_relative_to((CHECKOUT/PREFIX).resolve()) and not source.is_relative_to((ROOT/'github-sync/repositories').resolve());data=source.read_bytes();assert sha(data)==row['sha256'] and row['issues']==0
    if target.exists():assert relative in previous['sourceSnapshot'] and sha(target.read_bytes())==previous['sourceSnapshot'][relative]['sha256'],'Foreign edit in import target'
    else:assert relative not in previous['sourceSnapshot'];target.parent.mkdir(parents=True,exist_ok=True)
    if not target.exists() or sha(target.read_bytes())!=row['sha256']:shutil.copyfile(source,target);updated+=1
    assert sha(target.read_bytes())==row['sha256']
export={**previous,'sourceFiles':len(new),'sourceSnapshot':{name:{'sha256':row['sha256'],'bytes':row['bytes']} for name,row in new.items()},'sourceManifestTool':'verify_github_source_snapshot_v3.py','previousImportManifestSha256':sha(previous_bytes),'receivePackIpv4DryRunAccepted':True,'explicitGitAuthorIdentityPending':True,'commitsCreated':0,'pushesPerformed':0,'remoteRecoveryVerified':False};extra.write_text(json.dumps(export,ensure_ascii=False,indent=2),encoding='utf-8')
git(['add','--',PREFIX]);expected={PREFIX+'/'+name:{'sha256':row['sha256'],'bytes':row['bytes']} for name,row in new.items()};expected[extra.relative_to(CHECKOUT).as_posix()]={'sha256':sha(extra.read_bytes()),'bytes':extra.stat().st_size};staged={value.decode('utf-8') for value in git(['diff','--cached','--name-only','--diff-filter=ACMR','-z']).split(b'\0') if value};assert staged==set(expected)
index={}
for entry in git(['ls-files','--stage','-z']).split(b'\0'):
    if entry:header,name=entry.split(b'\t',1);mode,object_id,stage=header.split();assert stage==b'0';index[name.decode('utf-8')]=object_id.decode()
process=subprocess.Popen(['git','-C',str(CHECKOUT),'cat-file','--batch'],env=environment,stdin=subprocess.PIPE,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
for name,row in expected.items():
    process.stdin.write((index[name]+'\n').encode());process.stdin.flush();header=process.stdout.readline().decode().strip().split();assert header[0]==index[name] and header[1]=='blob';size=int(header[2]);data=process.stdout.read(size);assert len(data)==size and process.stdout.read(1)==b'\n' and sha(data)==row['sha256'] and size==row['bytes'],name
process.stdin.close();process.wait(timeout=30);assert process.returncode==0;assert git(['rev-parse','HEAD']).decode().strip()==base and git(['rev-parse','refs/heads/main']).decode().strip()==base and (CHECKOUT/'README.md').read_bytes()==readme
receipt={'baseCommit':base,'branch':'codex/neoforge-source-20261113','sourceFiles':len(new),'stagedFiles':len(expected),'updatedOrAddedSourceCopies':updated,'allStagedBlobSourceSha256Verified':True,'sourceManifest':str(manifest_path),'indexTree':git(['write-tree']).decode().strip(),'mainAndReadmePreserved':True,'commitsCreated':0,'pushesPerformed':0,'remoteRecoveryVerified':False,'explicitAuthorIdentityPending':True,'formalInstancesUntouched':True,'fullBossGoalStatus':'active','weaponsPaused':True};save(OUT/'source-sync-receipt.json',receipt);print(json.dumps(receipt),flush=True)
