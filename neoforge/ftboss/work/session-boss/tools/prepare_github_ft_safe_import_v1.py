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
BRANCH='codex/neoforge-source-20261113'
OUT=WORK/'reports/github-ft-source-import-v1'
def sha(data):return hashlib.sha256(data).hexdigest()
def save(path,value):
    with path.open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
parser=argparse.ArgumentParser();parser.add_argument('source_manifest',type=Path);args=parser.parse_args();manifest_path=args.source_manifest.resolve();assert manifest_path.is_relative_to((WORK/'reports').resolve());manifest=json.loads(manifest_path.read_text(encoding='utf-8'))
review=json.loads((WORK/'reports/github-ft-remote-review-v1/repository-review.json').read_text(encoding='utf-8'));assert review['head']=='27d20d5e1d2eb960f4a64cde9bd21230efae766c' and review['existingFiles']==1 and not review['remoteUpdated']
assert not OUT.exists() and not (CHECKOUT/PREFIX).exists() and not (ROOT/'.git').exists();OUT.mkdir();environment=dict(os.environ);environment.update({'GIT_TERMINAL_PROMPT':'0','GCM_INTERACTIVE':'Never','GIT_LFS_SKIP_SMUDGE':'1'});hooks=WORK/'reports/github-ft-remote-review-v1/empty-hooks'
def git(label,arguments):
    command=['git','-c','core.hooksPath='+str(hooks),'-c','core.autocrlf=false','-C',str(CHECKOUT),*arguments];result=subprocess.run(command,env=environment,capture_output=True);save(OUT/(label+'-terminal.json'),{'command':command,'exitCode':result.returncode,'stderr':result.stderr.decode('utf-8',errors='replace')});assert result.returncode==0,label;return result.stdout
assert git('head-before',['rev-parse','HEAD']).decode().strip()==review['head'];readme=git('readme-before',['show','HEAD:README.md']);readme_sha=sha(readme)
git('restore-existing-readme',['restore','--source=HEAD','--staged','--worktree','--','README.md']);git('create-authorized-branch',['switch','--no-track','-c',BRANCH,'origin/main']);assert git('branch',['branch','--show-current']).decode().strip()==BRANCH
copied={}
for row in manifest['files']:
    relative=row['path'];source=(ROOT/relative).resolve();target=(CHECKOUT/PREFIX/relative).resolve();assert source.is_relative_to(ROOT) and target.is_relative_to((CHECKOUT/PREFIX).resolve()) and not source.is_relative_to((ROOT/'github-sync/repositories').resolve());data=source.read_bytes();assert sha(data)==row['sha256'] and len(data)==row['bytes'] and row['issues']==0
    target.parent.mkdir(parents=True,exist_ok=True);assert not target.exists();shutil.copyfile(source,target);assert sha(target.read_bytes())==row['sha256'];copied[PREFIX+'/'+relative]=row
extra=CHECKOUT/PREFIX/'github-sync/import-manifest-ft-20261113.json';extra.parent.mkdir(parents=True,exist_ok=True);assert not extra.exists();export={'repository':'https://github.com/qyby123456/ft.git','baseCommit':review['head'],'branch':BRANCH,'sourcePrefix':PREFIX,'sourceFiles':len(copied),'sourceSnapshot':{name[len(PREFIX)+1:]:{'sha256':row['sha256'],'bytes':row['bytes']} for name,row in copied.items()},'sourceBytesPreserved':True,'detector':'verify_github_source_snapshot_v2.py;13positive/negative detector and2exact-review controls','reviewedMetadataMatches':3,'rawBuildRuntimePrivateDataAndLargeArtifactsExcluded':True,'mainAndExistingReadmePreserved':True,'commitsCreated':0,'pushesPerformed':0,'remoteRecoveryVerified':False,'authenticationAndExplicitAuthorIdentityPending':True,'formalInstancesReadOnly':True,'fullBossMigrationComplete':False,'weaponsPaused':True};save(extra,export);copied[extra.relative_to(CHECKOUT).as_posix()]={'sha256':sha(extra.read_bytes()),'bytes':extra.stat().st_size}
git('stage-authorized-import',['add','--',PREFIX]);staged=git('staged-files',['diff','--cached','--name-only','--diff-filter=ACMR','-z']);names={value.decode('utf-8') for value in staged.split(b'\0') if value};assert names==set(copied)
entries=git('index-entries',['ls-files','--stage','-z']);index={}
for entry in entries.split(b'\0'):
    if not entry:continue
    header,name=entry.split(b'\t',1);mode,object_id,stage=header.split();assert stage==b'0';index[name.decode('utf-8')]=object_id.decode()
command=['git','-C',str(CHECKOUT),'cat-file','--batch'];process=subprocess.Popen(command,stdin=subprocess.PIPE,stdout=subprocess.PIPE,stderr=subprocess.PIPE,env=environment)
verified=0
for name,row in copied.items():
    process.stdin.write((index[name]+'\n').encode());process.stdin.flush();header=process.stdout.readline().decode().strip().split();assert header[1]=='blob';size=int(header[2]);data=process.stdout.read(size);assert len(data)==size and process.stdout.read(1)==b'\n';assert sha(data)==row['sha256'] and size==row['bytes'],name;verified+=1
process.stdin.close();process.wait(timeout=30);assert process.returncode==0
assert sha((CHECKOUT/'README.md').read_bytes())==readme_sha and git('head-after',['rev-parse','HEAD']).decode().strip()==review['head'] and git('main-after',['rev-parse','refs/heads/main']).decode().strip()==review['head']
name_configured=subprocess.run(['git','-C',str(CHECKOUT),'config','--get','user.name'],capture_output=True).returncode==0;email_configured=subprocess.run(['git','-C',str(CHECKOUT),'config','--get','user.email'],capture_output=True).returncode==0
receipt={'repository':'https://github.com/qyby123456/ft.git','checkout':str(CHECKOUT),'branch':BRANCH,'importPrefix':PREFIX,'baseCommit':review['head'],'sourceFilesCopied':len(manifest['files']),'stagedImportFiles':len(copied),'everyStagedBlobSourceSha256Verified':verified,'mainAndReadmeByteIdentical':True,'nameConfigured':name_configured,'emailConfigured':email_configured,'commitsCreated':0,'pushesPerformed':0,'remoteRecoveryVerified':False,'formalInstancesUntouched':True,'weaponsPaused':True,'fullBossGoalStatus':'active'};save(OUT/'source-import-receipt.json',receipt);print(json.dumps(receipt),flush=True)
