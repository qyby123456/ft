import argparse
import hashlib
import json
import os
import shutil
import subprocess
import sys
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
REPO=ROOT/'github-sync/repositories/ft'
RECOVERY=ROOT/'github-sync/repositories/ft-checkpoint-recovery-20261113'
BRANCH='codex/neoforge-source-20261113'
PREFIX='neoforge/ftboss/'
parser=argparse.ArgumentParser();parser.add_argument('descriptor',type=Path);arguments=parser.parse_args()
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def save(path,value):
    with path.open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def git(repository,*values):return subprocess.check_output(['git','-C',str(repository),*values])
def present():
    files={}
    for directory,folders,names in os.walk(REPO):
        if Path(directory)==REPO:folders[:]=[name for name in folders if name!='.git']
        for filename in names:
            path=Path(directory)/filename;assert not path.is_symlink();files[path.relative_to(REPO).as_posix()]=path
    return files
configuration=load(arguments.descriptor);label=configuration['label'];assert label.replace('-','').isalnum()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f';assert owner['weaponValidationPaused'] and not owner['activeRuntimePids']
OUT=WORK/'reports'/('github-'+label+'-publication-v1-20261114');OUT.mkdir()
helper=WORK/'tools/prepare_boss_blood_tide_stage10.py';namespace={'__file__':str(helper),'__name__':'patch_helper_only'};exec(compile(helper.read_text(encoding='utf-8').split('probe=(WORK/')[0],str(helper),'exec'),namespace);create=namespace['create']
proxy=os.environ['FT_TASK_PROXY'];assert proxy.startswith('http://127.0.0.1:')
def network(repository,name,*values,public=False):
    options=['-c','http.proxy='+proxy,'-c','http.sslBackend=openssl','-c','http.sslVerify=true','-c','http.version=HTTP/1.1']
    if public:options+=['-c','credential.helper=']
    with (OUT/(name+'.log')).open('xb') as stream:
        process=subprocess.Popen(['git','-C',str(repository),*options,*values],stdout=stream,stderr=subprocess.STDOUT,env=dict(os.environ,GIT_TERMINAL_PROMPT='0',GCM_INTERACTIVE='Never'))
        try:code=process.wait(timeout=50)
        except subprocess.TimeoutExpired:save(OUT/(name+'-timeout.json'),{'ownedPid':process.pid,'requiresOwnedChildAuditBeforeContinuation':True});raise
    assert code==0,(name,code)
    return (OUT/(name+'.log')).read_text(encoding='utf-8',errors='replace')
def stage(paths,suffix):
    assert not git(REPO,'status','--porcelain').strip()
    names=[PREFIX+name for name in paths]
    subprocess.run(['git','-C',str(REPO),'sparse-checkout','add','--stdin'],input=(chr(10).join('/'+name for name in names)+chr(10)).encode(),check=True,capture_output=True)
    for name,relative in zip(names,paths):
        target=REPO/name;target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(ROOT/relative,target);assert target.read_bytes()==(ROOT/relative).read_bytes()
    subprocess.run(['git','-C',str(REPO),'add','--',*names],check=True)
    subprocess.run([sys.executable,'-B',str(WORK/'tools/verify_github_source_snapshot_v3.py'),'--git-dir',str(REPO/'.git'),'--work-tree',str(REPO),'--source-prefix',PREFIX.rstrip('/'),'--reviewed-exceptions',str(ROOT/'github-sync/reviewed-scan-exceptions.json'),'--staged','--output',str(OUT/('scan-'+suffix))],check=True)
    assert not git(REPO,'diff','--cached','--check') and not git(REPO,'diff','--name-only')
def publish(suffix,message):
    previous=git(REPO,'rev-parse','HEAD').decode().strip();assert git(RECOVERY,'rev-parse','HEAD').decode().strip()==previous
    result=subprocess.run(['git','-C',str(REPO),'commit','-m',message],capture_output=True);assert result.returncode==0,result.stderr
    head=git(REPO,'rev-parse','HEAD').decode().strip();network(REPO,'push-'+suffix,'push','origin','HEAD:refs/heads/'+BRANCH)
    network(RECOVERY,'fetch-'+suffix,'fetch','--no-tags','origin','refs/heads/'+BRANCH,public=True)
    assert network(RECOVERY,'head-'+suffix,'ls-remote','origin','refs/heads/'+BRANCH,public=True).split()[0]==head
    assert git(RECOVERY,'rev-parse','FETCH_HEAD').decode().strip()==head
    assert not (RECOVERY/'.git/objects/info/alternates').exists();assert git(RECOVERY,'remote','get-url','origin').decode().strip()=='https://github.com/qyby123456/ft.git'
    git(RECOVERY,'update-ref','refs/heads/'+BRANCH,head,previous)
    for ancestor in [previous,'27d20d5e1d2eb960f4a64cde9bd21230efae766c']:subprocess.run(['git','-C',str(RECOVERY),'merge-base','--is-ancestor',ancestor,head],check=True)
    with (OUT/('fsck-'+suffix+'.log')).open('xb') as stream:subprocess.run(['git','-C',str(RECOVERY),'fsck','--full'],stdout=stream,stderr=subprocess.STDOUT,check=True)
    entries=[]
    for entry in git(RECOVERY,'ls-tree','-r','-z','HEAD').split(bytes([0])):
        if not entry:continue
        metadata,name=entry.split(bytes([9]),1);mode,kind,object_id=metadata.split();assert mode==b'100644' and kind==b'blob';entries.append((name.decode('utf-8'),object_id))
    payload=subprocess.run(['git','-C',str(RECOVERY),'cat-file','--batch'],input=bytes([10]).join(object_id for name,object_id in entries)+bytes([10]),capture_output=True,check=True).stdout
    cursor=0;files={}
    for name,object_id in entries:
        end=payload.index(bytes([10]),cursor);header=payload[cursor:end].split();assert header[:2]==[object_id,b'blob'];size=int(header[2]);data=payload[end+1:end+1+size];cursor=end+size+2;digest=hashlib.sha256(data).hexdigest();files[name]={'bytes':size,'sha256':digest}
        if name==PREFIX+'github-sync/import-manifest-ft-20261113.json':expected=git(RECOVERY,'show','204b13de017c6cb2c8b6e06513f4436e550ff412:'+name)
        elif name=='README.md':expected=git(RECOVERY,'show','27d20d5e1d2eb960f4a64cde9bd21230efae766c:README.md')
        else:assert name.startswith(PREFIX);expected=(ROOT/name.removeprefix(PREFIX)).read_bytes()
        assert len(expected)==size and hashlib.sha256(expected).hexdigest()==digest,name
    assert cursor==len(payload)
    actual=present()
    for name,path in actual.items():assert files[name]['bytes']==path.stat().st_size and files[name]['sha256']==sha(path),name
    absent=set()
    for relative in configuration['previousPlans']+([plan_relative] if suffix=='cleanup' else []):absent.update(load(ROOT/relative)['deleteFiles'])
    assert set(files)-set(actual)==absent;assert not git(REPO,'status','--porcelain').strip()
    receipt={'commit':head,'parent':previous,'tree':git(RECOVERY,'rev-parse','HEAD^{tree}').decode().strip(),'branch':BRANCH,'sourceRemoteRecoveryVerified':True,'remoteHeadMatchesPushedCommit':True,'independentAuthoritativeRemoteFetchNoLocalAlternates':True,'allRemoteTrackedFilesPathSizeSha256Verified':len(files),'currentCanonicalSourceFilesVerified':len(files)-2,'sparsePresentFilesVerified':len(actual),'plannedAbsentPathsVerified':len(absent),'originalBaseReadmeAndHistoricalManifestVerified':True,'gitFsckFullPassed':True,'largeArtifactsRemoteRecoveryVerified':False,'formalInstancesReadOnly':True,'installable':False,'deployed':False,'files':files}
    path=OUT/('remote-proof-'+suffix+'.json');save(path,receipt);return path,receipt
report=load(ROOT/configuration['nativeReport']);assert report['probe']['failures']==0 and report['terminal']['clientProcessReaped'] and report['terminal']['integratedWorldCompleteSave'] and report['terminal']['inputHashesUnchanged']
for name,expected in report['inputHashes'].items():assert sha(name)==expected
base=WORK/'archives/boss-core-stage28-zicai-curio-scoped-evidence-v3-20261113.zip';base_sha='aca9e96def7887e0cf52637152df8922174326d5970376918d42b08bee864929';assert sha(base)==base_sha
paths={ROOT/configuration['nativeReport'],arguments.descriptor.resolve(),Path(__file__)}
paths.update(path for path in Path(report['attempt']).rglob('*') if path.is_file())
for relative in configuration['sources']+configuration.get('extraEvidence',[]):
    path=ROOT/relative;paths.update([path] if path.is_file() else [child for child in path.rglob('*') if child.is_file()])
rows={};unique={}
with zipfile.ZipFile(base) as bundle:available=set(bundle.namelist())
for path in sorted(paths):
    assert path.is_relative_to(ROOT) and not path.is_symlink();digest=sha(path);layer='base' if 'blobs/'+digest in available else 'self';rows[path.relative_to(ROOT).as_posix()]={'bytes':path.stat().st_size,'sha256':digest,'layer':layer}
    if layer=='self':unique[digest]=path
archive=WORK/'archives'/('boss-curios-'+label+'-increment-v1-20261114.zip')
with zipfile.ZipFile(archive,'x',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as bundle:
    bundle.writestr('ARCHIVE_MANIFEST.json',json.dumps({'format':'ftboss-content-addressed-increment-v1','baseArchiveSha256':base_sha,'files':rows},ensure_ascii=False,indent=2).encode('utf-8'))
    for digest,path in sorted(unique.items()):bundle.write(path,'blobs/'+digest)
with (OUT/'archive-validation.log').open('xb') as stream:subprocess.run([sys.executable,'-B',str(WORK/'tools/restore_boss_zicai_curio_archive_v2.py'),str(archive),'--base',str(base)],stdout=stream,stderr=subprocess.STDOUT,check=True)
archive_receipt={'archiveRelativePath':archive.relative_to(ROOT).as_posix(),'archiveSha256':sha(archive),'archiveBytes':archive.stat().st_size,'baseArchiveRelativePath':base.relative_to(ROOT).as_posix(),'baseArchiveSha256':base_sha,'paths':len(rows),'selfBlobs':len(unique),'allReferencedPayloadHashesVerified':True,'fullPhysicalRestoreClaimed':False,'rawArtifactsRemoteRecoveryVerified':False,'allOriginalsRetained':True};save(OUT/'archive-receipt.json',archive_receipt)
checkpoint_relative='github-sync/checkpoint-curios-'+label+'-20261114.json';plan_relative='github-sync/checkpoint-curios-'+label+'-cleanup-plan-20261114.json'
checkpoint={'format':'ftboss-scoped-lifecycle-increment-v1','branch':BRANCH,'parentSourceCommit':git(REPO,'rev-parse','HEAD').decode().strip(),'nativeReportRelativePath':configuration['nativeReport'],'nativeReportSha256':sha(ROOT/configuration['nativeReport']),'checks':len(report['probe']['checks']),'failures':0,'acceptance':configuration['acceptance'],'archive':archive_receipt,'sourceFiles':{relative:{'sha256':sha(ROOT/relative),'bytes':(ROOT/relative).stat().st_size} for relative in configuration['sources']},'priorAcceptedControlsRerun':False,'newJvmActualUserConfirmationPending':True,'waitingWindowExists':False,'fullLifecycleAccepted':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False,'weaponsAndSessionPaused':True};create(ROOT/checkpoint_relative,json.dumps(checkpoint,ensure_ascii=False,indent=2))
keep=set(present());keep.update(PREFIX+relative for relative in configuration['sources'] if relative not in configuration['pruneSources']);keep.update([PREFIX+checkpoint_relative,PREFIX+plan_relative])
plan={'format':'ftboss-verified-source-cache-cleanup-plan-v3','branch':BRANCH,'targetRelativePath':'github-sync/repositories/ft','previousPlanRelativePaths':configuration['previousPlans'],'deleteFiles':{PREFIX+relative:{'sha256':sha(ROOT/relative),'bytes':(ROOT/relative).stat().st_size} for relative in configuration['pruneSources']},'mandatoryKeepFiles':sorted(keep),'canonicalFrozenSourcesAndRawArtifactsExcluded':True,'largeArtifactsRemoteRecoveryVerified':False};create(ROOT/plan_relative,json.dumps(plan,ensure_ascii=False,indent=2))
stage(configuration['sources']+[checkpoint_relative,plan_relative],'feature');proof_path,feature=publish('feature',configuration['commitMessage'])
prune=OUT/'cache-prune';subprocess.run([sys.executable,'-B',str(WORK/'tools/prune_github_verified_source_cache_v3.py'),str(ROOT/plan_relative),str(proof_path),'--apply','--output',str(prune)],check=True)
cleanup=load(prune/'cleanup-receipt.json');guard=load(prune/'handles-terminal.json');assert guard['exitCode']==0;handles=json.loads(guard['stdout']);assert handles['referencingProcesses']==handles['restartManagerFileHandleOwners']==0
result_relative='github-sync/checkpoint-curios-'+label+'-cleanup-result-20261114.json';result={'format':'ftboss-verified-source-cache-cleanup-result-v3','bindingSourceCommit':feature['commit'],'branch':BRANCH,'planRelativePath':plan_relative,'planSha256':sha(ROOT/plan_relative),'remoteProofSha256':sha(proof_path),'deletionReceiptSha256':sha(prune/'cleanup-receipt.json'),'deletedFiles':cleanup['deletedFiles'],'deletedFileCount':cleanup['deletedFileCount'],'logicalFreedBytes':cleanup['logicalFreedBytes'],'observedSystemFreeSpaceDeltaBytes':cleanup['observedSystemFreeSpaceDeltaBytes'],'observedSpaceIncludesOtherApplicationNoise':True,'referencingProcesses':0,'fileHandleOwners':0,'unexpectedRemovedPaths':cleanup['unexpectedRemovedPaths'],'canonicalSourcesAndRawArtifactsRetained':True,'formalInstancesReadOnly':True,'installable':False,'deployed':False};create(ROOT/result_relative,json.dumps(result,ensure_ascii=False,indent=2))
stage([result_relative],'cleanup');final_path,final=publish('cleanup','Record verified '+label+' duplicate-cache cleanup')
summary={'featureCommit':feature['commit'],'finalCommit':final['commit'],'finalTree':final['tree'],'finalRemoteProofRelativePath':final_path.relative_to(ROOT).as_posix(),'trackedFiles':final['allRemoteTrackedFilesPathSizeSha256Verified'],'canonicalFiles':final['currentCanonicalSourceFilesVerified'],'presentCacheFiles':final['sparsePresentFilesVerified'],'absentCacheFiles':final['plannedAbsentPathsVerified'],'archive':archive_receipt,'cleanupPlanRelativePath':plan_relative,'deletedDuplicateSourceFiles':cleanup['deletedFileCount'],'logicalFreedBytes':cleanup['logicalFreedBytes'],'observedFreeSpaceDeltaBytes':cleanup['observedSystemFreeSpaceDeltaBytes'],'installable':False,'deployed':False};save(OUT/'publication-closeout.json',summary);print(json.dumps(summary),flush=True)
