import argparse
import hashlib
import importlib.util
import json
import os
import subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
REPO=ROOT/'github-sync/repositories/ft'
RECOVERY=ROOT/'github-sync/repositories/ft-checkpoint-recovery-20261113'
BRANCH='codex/neoforge-source-20261113'
PREFIX='neoforge/ftboss/'
def load(path):return json.loads(path.read_text(encoding='utf-8-sig'))
def save(path,value):path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def sha(data):return hashlib.sha256(data).hexdigest()
def git(repository,*arguments,network=False,public=False):
    command=['git','-c','core.longpaths=true','-C',str(repository)]
    if network:command+=['-c','http.proxy='+os.environ['FT_TASK_PROXY'],'-c','http.sslBackend=openssl','-c','http.sslVerify=true','-c','http.version=HTTP/1.1']
    if public:command+=['-c','credential.helper=']
    return subprocess.run(command+list(arguments),capture_output=True,check=True,timeout=90,env=dict(os.environ,GIT_TERMINAL_PROMPT='0',GCM_INTERACTIVE='Never')).stdout
def owned():
    owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
    assert owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and not owner['activeRuntimePids'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
def checked(relative):
    path=(ROOT/relative).resolve();assert path.is_relative_to(ROOT) and not path.is_symlink();return path

def prepare(config,out):
    assert not out.exists()
    dirty=git(REPO,'status','--porcelain','-z','--untracked-files=no')
    if dirty:
        assert config.get('resumeExactPartialCopies') and not git(REPO,'diff','--cached','--name-only').strip()
        selected={PREFIX+name:checked(name) for name in config['files'] if not name.startswith(('work/session-boss/build/','work/session-boss/reports/'))}
        for item in dirty.split(b'\0'):
            if not item:continue
            assert item[:3]==b' M '
            name=item[3:].decode('utf-8');assert name in selected
            assert (REPO/name).read_bytes()==selected[name].read_bytes()
    for relative in config['files']:
        source=checked(relative);assert source.is_file() and source.suffix.lower() in {'.py','.java','.json','.md','.txt'}
        data=source.read_bytes();assert b'\0' not in data;data.decode('utf-8-sig')
    out.mkdir(parents=True)
    rows=[]
    for relative in config['files']:
        source=checked(relative);assert source.suffix.lower() in {'.py','.java','.json','.md','.txt'}
        destination=relative
        if relative.startswith(('work/session-boss/build/','work/session-boss/reports/')):
            destination='github-sync/'+config['key']+'/evidence/'+sha(relative.encode('utf-8'))[:16]+Path(relative).suffix
        git_path=PREFIX+destination;target=REPO/git_path;assert not target.is_symlink();data=source.read_bytes();assert b'\0' not in data;data.decode('utf-8-sig');target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(data)
        rows.append({'source':relative,'gitPath':git_path,'bytes':len(data),'sha256':sha(data)})
    assert len({row['gitPath'] for row in rows})==len(rows)
    metadata_path=PREFIX+'github-sync/checkpoint-'+config['key']+'.json'
    manifest={'previousCommit':git(REPO,'rev-parse','HEAD').decode().strip(),'key':config['key'],'sourceFiles':rows,'checkpointPath':metadata_path,'scope':config['scope'],'acceptance':config['acceptance'],'remaining':config['remaining'],'fullBossMigrationComplete':False,'installable':False,'deployed':False,'modelAndMusicResourcesExcluded':True,'rawResourcesRemainLocalOnly':True}
    save(REPO/metadata_path,manifest);save(out/'manifest.json',manifest)
    git(REPO,'add','--sparse','-f','--',*[row['gitPath'] for row in rows],metadata_path)
    staged=set(git(REPO,'diff','--cached','--name-only').decode().splitlines());assert staged.issubset({row['gitPath'] for row in rows}|{metadata_path}) and staged
    tree=git(REPO,'write-tree').decode().strip();spec=importlib.util.spec_from_file_location('source_guard',WORK/'tools/verify_source_only_upload_v1.py');module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module);audit=module.audit(REPO,tree);save(out/'source-only-audit.json',audit);assert audit['passed']
    result=subprocess.run([os.sys.executable,'-B',str(WORK/'tools/verify_github_source_snapshot_v3.py'),'--git-dir',str(REPO/'.git'),'--work-tree',str(REPO),'--source-prefix','neoforge/ftboss','--staged','--output',str(out/'source-scan')],capture_output=True,timeout=90);(out/'source-scan.log').write_bytes(result.stdout+result.stderr);assert result.returncode==0
    assert git(REPO,'write-tree').decode().strip()==load(out/'source-scan/verification.json')['indexTree']
    save(out/'prepared.json',{'tree':tree,'paths':sorted(staged),'exactSourceOnlyStagedScanPassed':True})
    print(json.dumps({'prepared':True,'files':len(rows)+1,'tree':tree}),flush=True)

def publish(config,out):
    prepared=load(out/'prepared.json');assert git(REPO,'write-tree').decode().strip()==prepared['tree']
    if not (out/'local-commit.json').exists():
        assert set(git(REPO,'diff','--cached','--name-only').decode().splitlines())==set(prepared['paths'])
        git(REPO,'commit','-m',config['message']);save(out/'local-commit.json',{'commit':git(REPO,'rev-parse','HEAD').decode().strip(),'tree':prepared['tree']})
    commit=load(out/'local-commit.json')['commit'];assert git(REPO,'rev-parse','HEAD').decode().strip()==commit
    git(REPO,'push','origin','HEAD:refs/heads/'+BRANCH,network=True);print(json.dumps({'pushed':True,'commit':commit}),flush=True)

def verify(config,out):
    commit=load(out/'local-commit.json')['commit'];manifest=load(out/'manifest.json');git(RECOVERY,'fetch','--no-tags','origin','refs/heads/'+BRANCH,network=True,public=True)
    assert git(RECOVERY,'rev-parse','FETCH_HEAD').decode().strip()==commit and git(RECOVERY,'ls-remote','origin','refs/heads/'+BRANCH,network=True,public=True).split()[0].decode()==commit
    assert not (RECOVERY/'.git/objects/info/alternates').exists()
    objects={}
    for entry in git(RECOVERY,'ls-tree','-r','-z',commit).split(b'\0'):
        if entry:
            info,name=entry.split(b'\t',1);objects[name.decode('utf-8')]=info.split()[2].decode()
    for row in manifest['sourceFiles']:
        data=git(RECOVERY,'cat-file','blob',objects[row['gitPath']]);assert len(data)==row['bytes'] and sha(data)==row['sha256']
    assert git(RECOVERY,'cat-file','blob',objects[manifest['checkpointPath']])==(REPO/manifest['checkpointPath']).read_bytes()
    for name in ['neoforge/ftweapons','archive-storage','README.md']:assert git(RECOVERY,'rev-parse',manifest['previousCommit']+':'+name)==git(RECOVERY,'rev-parse',commit+':'+name)
    git(RECOVERY,'fsck','--full');old=git(RECOVERY,'rev-parse','HEAD').decode().strip();git(RECOVERY,'update-ref','refs/heads/'+BRANCH,commit,old)
    proof={'commit':commit,'independentRemoteFetchAndBlobRecoveryVerified':True,'files':len(manifest['sourceFiles'])+1,'wholeTreeSourceOnlyGuardPassed':True,'wholeTreeFilesAudited':load(out/'source-only-audit.json')['filesAudited'],'weaponAndArchiveStorageUnchanged':True,'modelOrMusicUploaded':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False};save(out/'remote-proof.json',proof);print(json.dumps(proof),flush=True)

def prune(config,out):
    proof=load(out/'remote-proof.json');manifest=load(out/'manifest.json');assert proof['independentRemoteFetchAndBlobRecoveryVerified'] and git(REPO,'rev-parse','HEAD').decode().strip()==proof['commit']
    assert not (out/'cleanup-result.json').exists()
    files={row['gitPath']:{'bytes':row['bytes'],'sha256':row['sha256']} for row in manifest['sourceFiles'] if row['source'] in config.get('pruneDuplicateSources',[])}
    assert all(Path(name).suffix.lower() in {'.java','.py'} for name in files)
    for name,row in files.items():
        path=(REPO/name).resolve();assert path.is_relative_to(REPO.resolve()) and path.is_file() and not path.is_symlink();assert sha(path.read_bytes())==row['sha256']
    plan={'targetRelativePath':'github-sync/repositories/ft','verifiedGitCommit':proof['commit'],'deleteFiles':files,'onlyDuplicateWorktreeSourceFiles':True,'canonicalCandidateInputsAndResourcesRetained':True};save(out/'cleanup-plan.json',plan)
    guard=subprocess.run(['powershell','-NoProfile','-File',str(WORK/'tools/check_github_source_cache_handles_v1.ps1'),'-PlanPath',str(out/'cleanup-plan.json')],capture_output=True,timeout=60);(out/'cleanup-handles.log').write_bytes(guard.stdout+guard.stderr);assert guard.returncode==0
    if files:
        git(REPO,'update-index','--skip-worktree','--',*files)
        for name,row in files.items():
            path=(REPO/name).resolve();assert path.is_relative_to(REPO.resolve()) and sha(path.read_bytes())==row['sha256'];path.unlink()
    assert not git(REPO,'status','--porcelain','--untracked-files=no').strip()
    result={'verifiedGitCommit':proof['commit'],'deletedFiles':files,'deletedFileCount':len(files),'logicalBytes':sum(row['bytes'] for row in files.values()),'referencingProcesses':0,'fileHandleOwners':0,'canonicalCandidateInputsAndAllResourcesRetained':True,'modelOrMusicUploaded':False};save(out/'cleanup-result.json',result);print(json.dumps({'deletedFileCount':len(files),'logicalBytes':result['logicalBytes']}),flush=True)

parser=argparse.ArgumentParser(description='Resumable source-only Boss checkpoint, independent blob recovery and exact duplicate-cache eviction.')
parser.add_argument('operation',choices=['prepare','publish','verify','prune'])
parser.add_argument('configuration',type=Path)
arguments=parser.parse_args();owned();config=load(arguments.configuration.resolve());out=checked(config['output']);assert config['key'].replace('-','').isalnum();globals()[arguments.operation](config,out)
