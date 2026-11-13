import hashlib
import json
import os
from pathlib import Path
import subprocess
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/github-native-ignore-coverage-v1'
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with path.open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
assert not OUT.exists() and not (ROOT/'.git').exists();OUT.mkdir();checker=OUT/'checker';checker.mkdir();empty=OUT/'empty.gitconfig';empty.write_text('',encoding='utf-8')
environment=dict(os.environ);environment['GIT_CONFIG_NOSYSTEM']='1';environment['GIT_CONFIG_GLOBAL']=str(empty)
command=['git','init','--quiet',str(checker)];result=subprocess.run(command,cwd=ROOT,env=environment,capture_output=True);save(OUT/'init-command.json',command);save(OUT/'init-terminal.json',{'exitCode':result.returncode,'realDevelopmentRepositoryCreated':False,'temporaryIgnoreCheckerOnly':True});assert result.returncode==0
base=['git','--git-dir='+str(checker/'.git'),'--work-tree='+str(ROOT),'-c','core.excludesfile='+str(empty)]
cases={'src/main/java/Boss.java':False,'work/session-boss/tools/build_boss.py':False,'src/main/resources/boss.json':False,'tests/fixtures/boss.json':False,'github-sync/checkpoint/manifest.json':False,'work/session-boss/build/observer.jar':True,'work/session-boss/reports/native.json':True,'work/session-lease/reports/signed-session.json':True,'reference/forge-current.jar':True,'.env':True,'launcher_accounts.json':True,'credentials.properties':True,'keys/server.pem':True,'data/world/region/r.0.0.mca':True}
command=base+['check-ignore','--no-index','--stdin','-z'];result=subprocess.run(command,cwd=ROOT,env=environment,input=('\0'.join(cases)+'\0').encode('utf-8'),capture_output=True);assert result.returncode in [0,1];actual={part.decode('utf-8') for part in result.stdout.split(b'\0') if part};assert all((name in actual)==expected for name,expected in cases.items()),{'mismatched':[name for name,expected in cases.items() if (name in actual)!=expected]};save(OUT/'ignore-control-cases.json',[{'path':name,'ignored':name in actual,'expectedIgnored':expected} for name,expected in cases.items()])
command=base+['ls-files','--others','--exclude-standard','-z'];save(OUT/'enumerate-command.json',command);result=subprocess.run(command,cwd=ROOT,env=environment,capture_output=True);save(OUT/'enumerate-terminal.json',{'exitCode':result.returncode});assert result.returncode==0
names=sorted(part.decode('utf-8') for part in result.stdout.split(b'\0') if part)
initial=json.loads((ROOT/'github-sync/checkpoint-20261113-final/source-inventory.json').read_text(encoding='utf-8'));known={row['path']:row for row in initial['files']};files=[];unscanned=[];changed=[];extensions={};large=[];binary=[]
for name in names:
    path=(ROOT/name).resolve();assert path.is_relative_to(ROOT) and path.is_file();size=path.stat().st_size;digest=sha(path);prior=known.get(name);row={'path':name,'bytes':size,'sha256':digest,'coveredByPreviousTextScan':prior is not None and prior['sha256']==digest,'publishReviewed':False};files.append(row);extensions[path.suffix.lower()]=extensions.get(path.suffix.lower(),0)+1
    if not row['coveredByPreviousTextScan']:unscanned.append(row)
    if prior and prior['sha256']!=digest:changed.append(name)
    if size>20*1024*1024:large.append(row)
    with path.open('rb') as stream:prefix=stream.read(8192)
    if b'\0' in prefix:binary.append(row)
assert not (ROOT/'.git').exists();save(OUT/'actual-git-candidate-inventory.json',{'files':files,'count':len(files),'extensions':extensions,'ordinaryGitSourceWorkspaceStillUninitialized':True,'actualGitIgnoreApplied':True,'noCommitsBranchesRemotesOrUploadsCreated':True});save(OUT/'coverage-gaps.json',{'unscanned':unscanned,'changedSincePriorScan':changed,'largeOrdinaryGitCandidates':large,'binaryOrdinaryGitCandidates':binary,'counts':{'actualGitCandidates':len(files),'unscanned':len(unscanned),'large':len(large),'binary':len(binary)},'allUploadClearanceStillFalse':True})
print(json.dumps({'actualGitCandidates':len(files),'previousScanCount':len(known),'unscanned':len(unscanned),'large':len(large),'binary':len(binary),'extensions':extensions,'controlsPassed':len(cases),'commits':0,'uploads':0}),flush=True)
