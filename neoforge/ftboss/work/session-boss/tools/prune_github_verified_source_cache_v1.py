import argparse
import hashlib
import json
import os
import shutil
import subprocess
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
parser=argparse.ArgumentParser()
parser.add_argument('plan',type=Path)
parser.add_argument('remote_proof',type=Path)
parser.add_argument('--apply',action='store_true')
arguments=parser.parse_args()
def load(path):return json.loads(path.read_text(encoding='utf-8-sig'))
def sha(path):
    with path.open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
plan=load(arguments.plan);proof=load(arguments.remote_proof)
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['noNewWeaponRuns'] and owner['weaponValidationPaused'] and not owner.get('activeRuntimePids')
repository=(ROOT/plan['targetRelativePath']).resolve();assert repository==ROOT/'github-sync/repositories/ft' and repository.is_relative_to(ROOT.resolve()) and repository!=ROOT.resolve()
assert proof['sourceRemoteRecoveryVerified'] and proof['remoteHeadMatchesPushedCommit'] and proof['branch']==plan['branch']
def git(*values,input=None):return subprocess.run(['git','-C',str(repository),*values],input=input,capture_output=True,check=True).stdout
head=git('rev-parse','HEAD').decode().strip();assert head==proof['commit'];assert not git('status','--porcelain').strip();assert not (repository/'.git/index.lock').exists()
actual={}
for directory,folders,names in os.walk(repository):
    if Path(directory)==repository:folders[:]=[name for name in folders if name!='.git']
    for name in names:
        path=Path(directory)/name;relative=path.relative_to(repository).as_posix();assert not path.is_symlink() and path.resolve().is_relative_to(repository)
        actual[relative]=path
remote=proof['files'];assert set(actual)==set(remote)
keep=set(actual)-set(plan['deleteFiles']);assert set(plan['deleteFiles'])<=set(actual)
assert all(name in keep for name in plan['mandatoryKeepFiles'])
for name,row in plan['deleteFiles'].items():
    path=actual[name];assert name.startswith('neoforge/ftboss/') and '/.git/' not in name
    assert remote[name]['sha256']==row['sha256'] and remote[name]['bytes']==row['bytes']
    assert path.stat().st_size==row['bytes'] and sha(path)==row['sha256']
    original=ROOT/name.removeprefix('neoforge/ftboss/');assert original.is_file() and sha(original)==row['sha256']
assert all(not any(character in name for character in '*?[]!') for name in keep)
out=WORK/'reports/github-verified-source-cache-prune-v1-20261113';out.mkdir()
with (out/'preflight.json').open('x',encoding='utf-8') as stream:json.dump({'commit':head,'branch':plan['branch'],'target':str(repository),'targetInsideTemporaryWorkspace':True,'planSha256':sha(arguments.plan),'remoteProofSha256':sha(arguments.remote_proof),'deleteFiles':plan['deleteFiles'],'keepFiles':sorted(keep),'logicalDeleteBytes':sum(row['bytes'] for row in plan['deleteFiles'].values()),'sourceOriginalsArchivesBaselinesFailuresFormalDataExcluded':True},stream,ensure_ascii=False,indent=2)
checked=subprocess.run(['powershell','-NoProfile','-File',str(WORK/'tools/check_github_source_cache_handles_v1.ps1'),'-PlanPath',str(arguments.plan.resolve())],capture_output=True,text=True,encoding='utf-8-sig',errors='replace')
with (out/'handles-terminal.json').open('x',encoding='utf-8') as stream:json.dump({'exitCode':checked.returncode,'stdout':checked.stdout,'stderr':checked.stderr},stream,ensure_ascii=False,indent=2)
assert checked.returncode==0,'Fail closed:process/handle guard did not pass'
if not arguments.apply:print(json.dumps({'verifiedDeletionCandidates':len(plan['deleteFiles']),'applied':False}));raise SystemExit(0)
before=shutil.disk_usage(ROOT).free
patterns=(chr(10).join('/'+name for name in sorted(keep))+chr(10)).encode('utf-8')
result=subprocess.run(['git','-C',str(repository),'sparse-checkout','set','--no-cone','--stdin'],input=patterns,capture_output=True)
after_files={}
for directory,folders,names in os.walk(repository):
    if Path(directory)==repository:folders[:]=[name for name in folders if name!='.git']
    for name in names:after_files[(Path(directory)/name).relative_to(repository).as_posix()]=Path(directory)/name
removed=set(actual)-set(after_files);unexpected=removed-set(plan['deleteFiles'])
receipt={'commit':head,'branch':plan['branch'],'target':str(repository),'sparseCommandExitCode':result.returncode,'sparseCommandStdout':result.stdout.decode('utf-8',errors='replace'),'sparseCommandStderr':result.stderr.decode('utf-8',errors='replace'),'deletedFiles':{name:plan['deleteFiles'][name] for name in sorted(removed) if name in plan['deleteFiles']},'deletedFileCount':len(removed),'logicalFreedBytes':sum(plan['deleteFiles'][name]['bytes'] for name in removed if name in plan['deleteFiles']),'systemFreeBytesBefore':before,'systemFreeBytesAfter':shutil.disk_usage(ROOT).free,'unexpectedRemovedPaths':sorted(unexpected),'gitMetadataPreserved':(repository/'.git').is_dir(),'keptRecoveryFiles':sorted(keep),'largeArtifactsOrCanonicalSourcesDeleted':False,'formalInstancesModified':False}
receipt['observedSystemFreeSpaceDeltaBytes']=receipt['systemFreeBytesAfter']-before
with (out/'cleanup-receipt.json').open('x',encoding='utf-8') as stream:json.dump(receipt,stream,ensure_ascii=False,indent=2)
assert result.returncode==0 and removed==set(plan['deleteFiles']) and set(after_files)==keep and not unexpected
for name in keep:assert sha(after_files[name])==remote[name]['sha256']
assert not git('status','--porcelain').strip();assert git('rev-parse','HEAD').decode().strip()==head
print(json.dumps({key:value for key,value in receipt.items() if key not in ['deletedFiles','keptRecoveryFiles','sparseCommandStdout','sparseCommandStderr']},ensure_ascii=False))
