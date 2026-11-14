import ast
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
OUT=WORK/'reports/github-attended-reload-v18-diagnostic-publication-v1-20261114'
configuration=json.loads((WORK/'reports/zicai-attended-reload-preflight-v1-20261114/diagnostic-publication.json').read_text(encoding='utf-8'))
shared=WORK/'tools/publish_curios_lifecycle_increment_v1.py'
selected={'load','save','sha','git','present','network','stage','publish'}
syntax=ast.parse(shared.read_text(encoding='utf-8'))
functions=[node for node in syntax.body if isinstance(node,ast.FunctionDef) and node.name in selected]
assert {node.name for node in functions}==selected
exec(compile(ast.Module(body=functions,type_ignores=[]),str(shared),'exec'),globals())
helper=WORK/'tools/prepare_boss_blood_tide_stage10.py'
namespace={'__file__':str(helper),'__name__':'patch_helper_only'}
exec(compile(helper.read_text(encoding='utf-8').split('probe=(WORK/')[0],str(helper),'exec'),namespace)
create=namespace['create']
proxy=os.environ['FT_TASK_PROXY'];assert proxy.startswith('http://127.0.0.1:')
owner=load(ROOT/'WRITE_OWNER.json')
assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['weaponValidationPaused'] and not owner['activeRuntimePids']
if sys.argv[1:]==['--resume-after-preparation']:
    initial='df8c8875f4dbe2bf308e827c056c5ff68135dced'
    assert git(REPO,'rev-parse','HEAD').decode().strip()==git(RECOVERY,'rev-parse','HEAD').decode().strip()==initial
    assert not git(REPO,'status','--porcelain').strip() and not (REPO/'.git/index.lock').exists()
    report=load(ROOT/configuration['nativeReport'])
    assert report['probe']['failures']==1 and report['terminal']['clientProcessReaped'] and report['terminal']['inputHashesUnchanged']
    for name,expected in report['inputHashes'].items():assert sha(name)==expected
    network(RECOVERY,'fetch-resume-prepared','fetch','--no-tags','origin','refs/heads/'+BRANCH,public=True)
    assert network(RECOVERY,'head-resume-prepared','ls-remote','origin','refs/heads/'+BRANCH,public=True).split()[0]==initial
    assert git(RECOVERY,'rev-parse','FETCH_HEAD').decode().strip()==initial
    proof_path=OUT/'remote-proof-feature-before-cache-normalization.json';proof=load(proof_path)
    assert proof['commit']==initial and proof['sourceRemoteRecoveryVerified']
    files=proof['files'];previous={}
    for relative in configuration['previousPlans']:previous.update(load(ROOT/relative)['deleteFiles'])
    assert set(files)-set(present())==set(previous)
    for name,path in present().items():assert path.stat().st_size==files[name]['bytes'] and sha(path)==files[name]['sha256']
    reintroduced=PREFIX+'work/session-boss/BOSS_MIGRATION_REQUIREMENTS_20261113.md'
    plan_relative='github-sync/checkpoint-curios-attended-reload-v18-cleanup-plan-20261114.json'
    for name,row in files.items():
        if name in ['README.md',PREFIX+'github-sync/import-manifest-ft-20261113.json']:continue
        path=ROOT/name.removeprefix(PREFIX)
        if name==PREFIX+plan_relative:
            expected_plan=json.loads(git(RECOVERY,'show',initial+':'+name));expected_plan['mandatoryKeepFiles'].remove(reintroduced)
            assert load(path)==expected_plan
        else:assert path.stat().st_size==row['bytes'] and sha(path)==row['sha256']
    snapshot_relative='github-sync/checkpoint-curios-attended-reload-v18-absent-snapshot-20261114.json'
    normal_plan_relative='github-sync/checkpoint-curios-attended-reload-v18-document-normalization-plan-20261114.json'
    normal_out=OUT/'cache-document-normalization';normal_receipt=load(normal_out/'cleanup-receipt.json')
    assert normal_receipt['commit']==initial and normal_receipt['deletedFileCount']==1 and set(normal_receipt['deletedFiles'])=={reintroduced} and not normal_receipt['unexpectedRemovedPaths']
    correction_relative='github-sync/checkpoint-curios-attended-reload-v18-publication-recovery-20261114.json'
    correction=load(ROOT/correction_relative)
    assert correction['remoteProofSha256']==sha(proof_path) and correction['normalizationReceiptSha256']==sha(normal_out/'cleanup-receipt.json') and correction['nativeAccepted'] is False
    source_paths=[Path(__file__).relative_to(ROOT).as_posix(),snapshot_relative,normal_plan_relative,plan_relative,correction_relative]
    original_archive_receipt=load(OUT/'recovery-archive-receipt.json')
    assert sha(ROOT/original_archive_receipt['archiveRelativePath'])==original_archive_receipt['archiveSha256']
    archive=WORK/'archives/boss-curios-attended-reload-v18-publication-recovery-v2-20261114.zip'
    base=WORK/'archives/boss-core-stage28-zicai-curio-scoped-evidence-v3-20261113.zip'
    assert sha(base)=='aca9e96def7887e0cf52637152df8922174326d5970376918d42b08bee864929'
    archive_files={ROOT/relative for relative in source_paths}
    archive_files.update(path for path in OUT.rglob('*') if path.is_file())
    with zipfile.ZipFile(base) as bundle:available=set(bundle.namelist())
    rows={};unique={}
    for path in sorted(archive_files):
        digest=sha(path);layer='base' if 'blobs/'+digest in available else 'self';rows[path.relative_to(ROOT).as_posix()]={'bytes':path.stat().st_size,'sha256':digest,'layer':layer}
        if layer=='self':unique[digest]=path
    with zipfile.ZipFile(archive,'x',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as bundle:
        bundle.writestr('ARCHIVE_MANIFEST.json',json.dumps({'format':'ftboss-content-addressed-increment-v1','baseArchiveSha256':sha(base),'files':rows},ensure_ascii=False,indent=2).encode('utf-8'))
        for digest,path in sorted(unique.items()):bundle.write(path,'blobs/'+digest)
    with (OUT/'recovery-v2-archive-validation.log').open('xb') as stream:subprocess.run([sys.executable,'-B',str(WORK/'tools/restore_boss_zicai_curio_archive_v2.py'),str(archive),'--base',str(base)],stdout=stream,stderr=subprocess.STDOUT,check=True)
    recovery_archive={'archiveRelativePath':archive.relative_to(ROOT).as_posix(),'archiveSha256':sha(archive),'archiveBytes':archive.stat().st_size,'paths':len(rows),'allReferencedPayloadHashesVerified':True,'fullPhysicalRestoreClaimed':False,'rawArtifactsRemoteRecoveryVerified':False}
    save(OUT/'recovery-v2-archive-receipt.json',recovery_archive)
else:
    initial='df8c8875f4dbe2bf308e827c056c5ff68135dced'
    assert git(REPO,'rev-parse','HEAD').decode().strip()==git(RECOVERY,'rev-parse','HEAD').decode().strip()==initial
    assert not git(REPO,'status','--porcelain').strip()
    assert not (REPO/'.git/index.lock').exists()
    report=load(ROOT/configuration['nativeReport'])
    assert report['probe']['failures']==1 and not report['probe']['confirmationFlowContinued']
    assert report['terminal']['exitCode']==0 and report['terminal']['clientProcessReaped'] and report['terminal']['inputHashesUnchanged']
    for name,expected in report['inputHashes'].items():assert sha(name)==expected
    if not sys.argv[1:]:
        network(RECOVERY,'fetch-resume-initial','fetch','--no-tags','origin','refs/heads/'+BRANCH,public=True)
        assert network(RECOVERY,'head-resume-initial','ls-remote','origin','refs/heads/'+BRANCH,public=True).split()[0]==initial
        assert git(RECOVERY,'rev-parse','FETCH_HEAD').decode().strip()==initial
        assert not (RECOVERY/'.git/objects/info/alternates').exists()
        assert git(RECOVERY,'remote','get-url','origin').decode().strip()=='https://github.com/qyby123456/ft.git'
        with (OUT/'fsck-resume-initial.log').open('xb') as stream:subprocess.run(['git','-C',str(RECOVERY),'fsck','--full'],stdout=stream,stderr=subprocess.STDOUT,check=True)
        entries=[]
        for entry in git(RECOVERY,'ls-tree','-r','-z','HEAD').split(bytes([0])):
            if not entry:continue
            metadata,name=entry.split(bytes([9]),1);mode,kind,object_id=metadata.split()
            assert mode==b'100644' and kind==b'blob';entries.append((name.decode('utf-8'),object_id))
        payload=subprocess.run(['git','-C',str(RECOVERY),'cat-file','--batch'],input=bytes([10]).join(object_id for name,object_id in entries)+bytes([10]),capture_output=True,check=True).stdout
        cursor=0;files={}
        for name,object_id in entries:
            end=payload.index(bytes([10]),cursor);header=payload[cursor:end].split()
            assert header[:2]==[object_id,b'blob'];size=int(header[2]);data=payload[end+1:end+1+size];cursor=end+size+2
            digest=hashlib.sha256(data).hexdigest();files[name]={'bytes':size,'sha256':digest}
            if name==PREFIX+'github-sync/import-manifest-ft-20261113.json':expected=git(RECOVERY,'show','204b13de017c6cb2c8b6e06513f4436e550ff412:'+name)
            elif name=='README.md':expected=git(RECOVERY,'show','27d20d5e1d2eb960f4a64cde9bd21230efae766c:README.md')
            else:assert name.startswith(PREFIX);expected=(ROOT/name.removeprefix(PREFIX)).read_bytes()
            assert len(expected)==size and hashlib.sha256(expected).hexdigest()==digest,name
        assert cursor==len(payload)
        actual=present();previous={}
        for relative in configuration['previousPlans']:previous.update(load(ROOT/relative)['deleteFiles'])
        reintroduced=PREFIX+'work/session-boss/BOSS_MIGRATION_REQUIREMENTS_20261113.md'
        assert set(files)-set(actual)==set(previous)-{reintroduced}
        assert reintroduced in previous and reintroduced in actual
        for name,path in actual.items():assert files[name]['bytes']==path.stat().st_size and files[name]['sha256']==sha(path)
        proof={'commit':initial,'parent':configuration['parentSourceCommit'],'tree':git(RECOVERY,'rev-parse','HEAD^{tree}').decode().strip(),'branch':BRANCH,'sourceRemoteRecoveryVerified':True,'remoteHeadMatchesPushedCommit':True,'independentAuthoritativeRemoteFetchNoLocalAlternates':True,'allRemoteTrackedFilesPathSizeSha256Verified':len(files),'currentCanonicalSourceFilesVerified':len(files)-2,'sparsePresentFilesVerified':len(actual),'plannedAbsentPathsVerified':len(set(previous)-{reintroduced}),'explicitUpdatedPreviouslyPrunedFileRetainedForGuardedNormalization':{reintroduced:files[reintroduced]},'originalBaseReadmeAndHistoricalManifestVerified':True,'gitFsckFullPassed':True,'largeArtifactsRemoteRecoveryVerified':False,'formalInstancesReadOnly':True,'installable':False,'deployed':False,'files':files}
        proof_path=OUT/'remote-proof-feature-before-cache-normalization.json';save(proof_path,proof)
        snapshot_relative='github-sync/checkpoint-curios-attended-reload-v18-absent-snapshot-20261114.json'
        snapshot={'format':'ftboss-source-cache-absence-snapshot-v1','branch':BRANCH,'priorPlanRelativePaths':configuration['previousPlans'],'bindingRemoteSourceCommit':initial,'onlyPriorMissingPathExplicitlyReintroduced':{reintroduced:files[reintroduced]},'deleteFiles':{name:row for name,row in previous.items() if name!=reintroduced},'snapshotOnlyNoDeletionPerformed':True}
        create(ROOT/snapshot_relative,json.dumps(snapshot,ensure_ascii=False,indent=2))
        normal_plan_relative='github-sync/checkpoint-curios-attended-reload-v18-document-normalization-plan-20261114.json'
        normal_plan={'format':'ftboss-verified-source-cache-cleanup-plan-v3','branch':BRANCH,'targetRelativePath':'github-sync/repositories/ft','previousPlanRelativePaths':[snapshot_relative],'deleteFiles':{reintroduced:files[reintroduced]},'mandatoryKeepFiles':sorted(set(actual)-{reintroduced}),'canonicalFrozenSourcesFailedWorldsAndRawArtifactsExcluded':True,'largeArtifactsRemoteRecoveryVerified':False}
        create(ROOT/normal_plan_relative,json.dumps(normal_plan,ensure_ascii=False,indent=2))
        normal_out=OUT/'cache-document-normalization'
        subprocess.run([sys.executable,'-B',str(WORK/'tools/prune_github_verified_source_cache_v3.py'),str(ROOT/normal_plan_relative),str(proof_path),'--apply','--output',str(normal_out)],check=True)
        normal_receipt=load(normal_out/'cleanup-receipt.json')
        assert normal_receipt['deletedFileCount']==1 and not normal_receipt['unexpectedRemovedPaths']
        assert set(files)-set(present())==set(previous)
    else:
        assert sys.argv[1:]==['--resume-after-document-normalization']
        plan_already_corrected=PREFIX+'work/session-boss/BOSS_MIGRATION_REQUIREMENTS_20261113.md' not in load(ROOT/'github-sync/checkpoint-curios-attended-reload-v18-cleanup-plan-20261114.json')['mandatoryKeepFiles']
        phase='resume-after-plan-correction' if plan_already_corrected else 'resume-after-normalization'
        network(RECOVERY,'fetch-'+phase,'fetch','--no-tags','origin','refs/heads/'+BRANCH,public=True)
        assert network(RECOVERY,'head-'+phase,'ls-remote','origin','refs/heads/'+BRANCH,public=True).split()[0]==initial
        assert git(RECOVERY,'rev-parse','FETCH_HEAD').decode().strip()==initial
        proof_path=OUT/'remote-proof-feature-before-cache-normalization.json';proof=load(proof_path)
        assert proof['commit']==initial and proof['sourceRemoteRecoveryVerified'] and proof['remoteHeadMatchesPushedCommit']
        files=proof['files'];previous={}
        for relative in configuration['previousPlans']:previous.update(load(ROOT/relative)['deleteFiles'])
        reintroduced=PREFIX+'work/session-boss/BOSS_MIGRATION_REQUIREMENTS_20261113.md'
        snapshot_relative='github-sync/checkpoint-curios-attended-reload-v18-absent-snapshot-20261114.json'
        normal_plan_relative='github-sync/checkpoint-curios-attended-reload-v18-document-normalization-plan-20261114.json'
        normal_out=OUT/'cache-document-normalization';normal_receipt=load(normal_out/'cleanup-receipt.json')
        assert normal_receipt['commit']==initial and normal_receipt['deletedFileCount']==1 and set(normal_receipt['deletedFiles'])=={reintroduced} and not normal_receipt['unexpectedRemovedPaths']
        assert set(files)-set(present())==set(previous)
        for name,row in files.items():
            if name in ['README.md',PREFIX+'github-sync/import-manifest-ft-20261113.json']:continue
            path=ROOT/name.removeprefix(PREFIX)
            if plan_already_corrected and name==PREFIX+'github-sync/checkpoint-curios-attended-reload-v18-cleanup-plan-20261114.json':
                expected_plan=json.loads(git(RECOVERY,'show',initial+':'+name));expected_plan['mandatoryKeepFiles'].remove(reintroduced)
                assert load(path)==expected_plan
            else:assert path.stat().st_size==row['bytes'] and sha(path)==row['sha256']
        for name,path in present().items():assert path.stat().st_size==files[name]['bytes'] and sha(path)==files[name]['sha256']
    plan_relative='github-sync/checkpoint-curios-attended-reload-v18-cleanup-plan-20261114.json'
    plan=load(ROOT/plan_relative)
    if reintroduced in plan['mandatoryKeepFiles']:plan['mandatoryKeepFiles'].remove(reintroduced)
    else:
        expected_plan=json.loads(git(RECOVERY,'show',initial+':'+PREFIX+plan_relative));expected_plan['mandatoryKeepFiles'].remove(reintroduced)
        assert plan==expected_plan
    replacement_helper=ROOT/'work/session-lease/tools/reactivate_goal_after_boss_stage12.py'
    replacement_namespace={'__file__':str(replacement_helper),'__name__':'patch_helper_only'}
    exec(compile(replacement_helper.read_text(encoding='utf-8').split('assert not OUT.exists()')[0],str(replacement_helper),'exec'),replacement_namespace)
    plan_before=(ROOT/plan_relative).read_text(encoding='utf-8');plan_after=json.dumps(plan,ensure_ascii=False,indent=2)+'\n'
    if plan_before!=plan_after:replacement_namespace['replace_file'](ROOT/plan_relative,plan_before,plan_after)
    correction_relative='github-sync/checkpoint-curios-attended-reload-v18-publication-recovery-20261114.json'
    correction={'format':'ftboss-diagnostic-publication-recovery-v1','interruptedFeatureCommit':initial,'failure':'Sparse cache equality assertion rejected the explicitly updated previously pruned requirements document; remote push had succeeded; no cleanup had occurred.','originalFailureRetained':True,'allInitialRemotePathsBytesSha256IndependentlyVerified':len(files),'remoteProofSha256':sha(proof_path),'normalizationSnapshotRelativePath':snapshot_relative,'normalizationPlanRelativePath':normal_plan_relative,'normalizationReceiptSha256':sha(normal_out/'cleanup-receipt.json'),'onlyVerifiedDuplicateDocumentRemoved':True,'canonicalDocumentAndAllRawEvidenceRetained':True,'nativeAccepted':False,'installable':False,'deployed':False,'initialOneShotDriverNotRerun':True,'resumeToolRelativePath':Path(__file__).relative_to(ROOT).as_posix()}
    create(ROOT/correction_relative,json.dumps(correction,ensure_ascii=False,indent=2))
    source_paths=[Path(__file__).relative_to(ROOT).as_posix(),snapshot_relative,normal_plan_relative,plan_relative,correction_relative]
    archive=WORK/'archives/boss-curios-attended-reload-v18-publication-recovery-v1-20261114.zip'
    base=WORK/'archives/boss-core-stage28-zicai-curio-scoped-evidence-v3-20261113.zip'
    assert sha(base)=='aca9e96def7887e0cf52637152df8922174326d5970376918d42b08bee864929'
    archive_files={ROOT/relative for relative in source_paths}
    archive_files.update(path for path in OUT.rglob('*') if path.is_file())
    with zipfile.ZipFile(base) as bundle:available=set(bundle.namelist())
    rows={};unique={}
    for path in sorted(archive_files):
        digest=sha(path);layer='base' if 'blobs/'+digest in available else 'self';rows[path.relative_to(ROOT).as_posix()]={'bytes':path.stat().st_size,'sha256':digest,'layer':layer}
        if layer=='self':unique[digest]=path
    with zipfile.ZipFile(archive,'x',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as bundle:
        bundle.writestr('ARCHIVE_MANIFEST.json',json.dumps({'format':'ftboss-content-addressed-increment-v1','baseArchiveSha256':sha(base),'files':rows},ensure_ascii=False,indent=2).encode('utf-8'))
        for digest,path in sorted(unique.items()):bundle.write(path,'blobs/'+digest)
    with (OUT/'recovery-archive-validation.log').open('xb') as stream:subprocess.run([sys.executable,'-B',str(WORK/'tools/restore_boss_zicai_curio_archive_v2.py'),str(archive),'--base',str(base)],stdout=stream,stderr=subprocess.STDOUT,check=True)
    recovery_archive={'archiveRelativePath':archive.relative_to(ROOT).as_posix(),'archiveSha256':sha(archive),'archiveBytes':archive.stat().st_size,'paths':len(rows),'allReferencedPayloadHashesVerified':True,'fullPhysicalRestoreClaimed':False,'rawArtifactsRemoteRecoveryVerified':False}
    save(OUT/'recovery-archive-receipt.json',recovery_archive)
stage(source_paths,'correction');corrected_path,corrected=publish('correction','Recover diagnostic publication after updated document cache reintroduction')
prune=OUT/'cache-prune'
subprocess.run([sys.executable,'-B',str(WORK/'tools/prune_github_verified_source_cache_v3.py'),str(ROOT/plan_relative),str(corrected_path),'--apply','--output',str(prune)],check=True)
cleanup=load(prune/'cleanup-receipt.json')
result_relative='github-sync/checkpoint-curios-attended-reload-v18-cleanup-result-20261114.json'
result={'format':'ftboss-verified-source-cache-cleanup-result-v3','bindingSourceCommit':corrected['commit'],'branch':BRANCH,'planRelativePath':plan_relative,'planSha256':sha(ROOT/plan_relative),'remoteProofSha256':sha(corrected_path),'deletionReceiptSha256':sha(prune/'cleanup-receipt.json'),'deletedFiles':cleanup['deletedFiles'],'deletedFileCount':cleanup['deletedFileCount'],'logicalFreedBytes':cleanup['logicalFreedBytes'],'observedSystemFreeSpaceDeltaBytes':cleanup['observedSystemFreeSpaceDeltaBytes'],'observedSpaceIncludesOtherApplicationNoise':True,'referencingProcesses':0,'fileHandleOwners':0,'unexpectedRemovedPaths':cleanup['unexpectedRemovedPaths'],'previouslyReintroducedDocumentNormalizationReceiptSha256':sha(normal_out/'cleanup-receipt.json'),'totalVerifiedDuplicateFilesRemovedIncludingNormalization':3,'canonicalSourcesAndFailedEvidenceRetained':True,'nativeAcceptanceClaimed':False,'formalInstancesReadOnly':True,'installable':False,'deployed':False}
for folder in [normal_out,prune]:
    guard=load(folder/'handles-terminal.json');assert guard['exitCode']==0
    handles=json.loads(guard['stdout']);assert handles['referencingProcesses']==handles['restartManagerFileHandleOwners']==0
create(ROOT/result_relative,json.dumps(result,ensure_ascii=False,indent=2))
stage([result_relative],'cleanup');final_path,final=publish('cleanup','Record guarded diagnostic cache cleanup and retained strict reload failure')
summary={'sourceDiagnosticPublished':True,'nativeAccepted':False,'remainingActualUserInteractionGate':True,'featureCommit':initial,'correctionCommit':corrected['commit'],'finalCommit':final['commit'],'finalTree':final['tree'],'finalRemoteProofRelativePath':final_path.relative_to(ROOT).as_posix(),'trackedFiles':final['allRemoteTrackedFilesPathSizeSha256Verified'],'canonicalFiles':final['currentCanonicalSourceFilesVerified'],'presentCacheFiles':final['sparsePresentFilesVerified'],'absentCacheFiles':final['plannedAbsentPathsVerified'],'archive':load(OUT/'archive-receipt.json'),'publicationRecoveryArchive':recovery_archive,'cleanupPlanRelativePath':plan_relative,'deletedDuplicateSourceFiles':cleanup['deletedFileCount']+normal_receipt['deletedFileCount'],'logicalFreedBytes':cleanup['logicalFreedBytes']+normal_receipt['logicalFreedBytes'],'observedFreeSpaceDeltaBytes':cleanup['observedSystemFreeSpaceDeltaBytes']+normal_receipt['observedSystemFreeSpaceDeltaBytes'],'allFailedWorldsAndRawArtifactsRetained':True,'currentWaitingWindowExists':False,'installable':False,'deployed':False}
save(OUT/'publication-closeout.json',summary);print(json.dumps(summary),flush=True)
