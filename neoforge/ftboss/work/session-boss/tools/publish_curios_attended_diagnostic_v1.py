import hashlib
import json
import os
import subprocess
import sys
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
shared=WORK/'tools/publish_curios_lifecycle_increment_v1.py'
marker="report=load(ROOT/configuration['nativeReport']);"
shared_text=shared.read_text(encoding='utf-8');assert shared_text.count(marker)==1
namespace={'__file__':str(shared),'__name__':'source_publication_primitives_only'}
exec(compile(shared_text.split(marker)[0],str(shared),'exec'),namespace)
configuration=namespace['configuration'];OUT=namespace['OUT'];load=namespace['load'];save=namespace['save'];sha=namespace['sha'];create=namespace['create'];stage=namespace['stage'];publish=namespace['publish'];git=namespace['git'];present=namespace['present'];PREFIX=namespace['PREFIX'];REPO=namespace['REPO'];BRANCH=namespace['BRANCH']
report_path=ROOT/configuration['nativeReport'];report=load(report_path);probe=report['probe'];terminal=report['terminal']
assert probe['failures']==1 and probe['failure']=='java.lang.AssertionError: ActualUserExperimentalConfirmationNotReceivedWithin180Seconds'
assert probe['experimentalScreenObserved'] and not probe['confirmationFlowContinued'] and probe['naturalPlayerPostCallbacks']==0
assert terminal['exitCode']==0 and terminal['clientProcessReaped'] and terminal['inputHashesUnchanged'] and not terminal['integratedWorldCompleteSave']
for name,expected in report['inputHashes'].items():assert sha(name)==expected
assert git(REPO,'rev-parse','HEAD').decode().strip()==configuration['parentSourceCommit']
base=WORK/'archives/boss-core-stage28-zicai-curio-scoped-evidence-v3-20261113.zip';base_sha='aca9e96def7887e0cf52637152df8922174326d5970376918d42b08bee864929';assert sha(base)==base_sha
paths={report_path,Path(__file__),Path(sys.argv[1]).resolve()}
paths.update(path for path in Path(report['attempt']).rglob('*') if path.is_file())
for relative in configuration['sources']+configuration['extraEvidence']:
    path=ROOT/relative;paths.update([path] if path.is_file() else [child for child in path.rglob('*') if child.is_file()])
with zipfile.ZipFile(base) as bundle:available=set(bundle.namelist())
rows={};unique={}
for path in sorted(paths):
    assert path.is_relative_to(ROOT) and not path.is_symlink();digest=sha(path);layer='base' if 'blobs/'+digest in available else 'self';rows[path.relative_to(ROOT).as_posix()]={'bytes':path.stat().st_size,'sha256':digest,'layer':layer}
    if layer=='self':unique[digest]=path
archive=WORK/'archives/boss-curios-attended-reload-v18-diagnostic-v1-20261114.zip'
with zipfile.ZipFile(archive,'x',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as bundle:
    bundle.writestr('ARCHIVE_MANIFEST.json',json.dumps({'format':'ftboss-content-addressed-increment-v1','baseArchiveSha256':base_sha,'files':rows},ensure_ascii=False,indent=2).encode('utf-8'))
    for digest,path in sorted(unique.items()):bundle.write(path,'blobs/'+digest)
with (OUT/'archive-validation.log').open('xb') as stream:subprocess.run([sys.executable,'-B',str(WORK/'tools/restore_boss_zicai_curio_archive_v2.py'),str(archive),'--base',str(base)],stdout=stream,stderr=subprocess.STDOUT,check=True)
archive_receipt={'archiveRelativePath':archive.relative_to(ROOT).as_posix(),'archiveSha256':sha(archive),'archiveBytes':archive.stat().st_size,'baseArchiveRelativePath':base.relative_to(ROOT).as_posix(),'baseArchiveSha256':base_sha,'paths':len(rows),'selfBlobs':len(unique),'allReferencedPayloadHashesVerified':True,'fullPhysicalRestoreClaimed':False,'rawArtifactsRemoteRecoveryVerified':False,'allOriginalsRetained':True};save(OUT/'archive-receipt.json',archive_receipt)
checkpoint_relative='github-sync/checkpoint-curios-attended-reload-v18-20261114.json';plan_relative='github-sync/checkpoint-curios-attended-reload-v18-cleanup-plan-20261114.json';namespace['plan_relative']=plan_relative
checkpoint={'format':'ftboss-attended-reload-diagnostic-source-checkpoint-v1','branch':BRANCH,'parentSourceCommit':configuration['parentSourceCommit'],'nativeReportRelativePath':configuration['nativeReport'],'nativeReportSha256':sha(report_path),'nativeAccepted':False,'nativeFailure':probe['failure'],'attendedWaitSeconds':180,'actualExperimentalConfirmationPageObserved':True,'actualIntegratedServerOrPlayerLoginObserved':False,'nativeProcessNormalExitAndReaped':True,'integratedWorldCompleteSave':False,'worldNeverLoadedThereforeNoSaveAcceptanceClaimed':True,'allFrozenInputHashesUnchanged':True,'noAutomaticConfirmationOrWorldFlagChanges':True,'allPreviousAcceptedControlsReusedWithoutRerun':True,'waitingWindowExists':False,'doNotAutoRetryBeforeUserReady':True,'archive':archive_receipt,'sourceFiles':{relative:{'bytes':(ROOT/relative).stat().st_size,'sha256':sha(ROOT/relative)} for relative in configuration['sources']},'remainingRequiredUserOperation':'User confirms readiness inthischat. Then launch a fresh attended copy ofacceptedV6world;userclicks Create Backup and Load before180seconddeadline. Muststill proveactualnewJVMlogin,exactoneoriginalrelic/no stalepending,120naturalticks/client sync. NeverseedfailedV18world.','nextBossContractReviewRuntimeDefectProven':False,'fullLifecycleAccepted':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False,'weaponsAndSessionPaused':True};create(ROOT/checkpoint_relative,json.dumps(checkpoint,ensure_ascii=False,indent=2))
keep=set(present());keep.update(PREFIX+relative for relative in configuration['sources'] if relative not in configuration['pruneSources']);keep.update([PREFIX+checkpoint_relative,PREFIX+plan_relative])
plan={'format':'ftboss-verified-source-cache-cleanup-plan-v3','branch':BRANCH,'targetRelativePath':'github-sync/repositories/ft','previousPlanRelativePaths':configuration['previousPlans'],'deleteFiles':{PREFIX+relative:{'sha256':sha(ROOT/relative),'bytes':(ROOT/relative).stat().st_size} for relative in configuration['pruneSources']},'mandatoryKeepFiles':sorted(keep),'canonicalFrozenSourcesFailedWorldsAndRawArtifactsExcluded':True,'largeArtifactsRemoteRecoveryVerified':False};create(ROOT/plan_relative,json.dumps(plan,ensure_ascii=False,indent=2))
stage(configuration['sources']+[checkpoint_relative,plan_relative],'feature');proof_path,feature=publish('feature','Preserve attended reload timeout evidence and prepare user interaction gate')
prune=OUT/'cache-prune';subprocess.run([sys.executable,'-B',str(WORK/'tools/prune_github_verified_source_cache_v3.py'),str(ROOT/plan_relative),str(proof_path),'--apply','--output',str(prune)],check=True)
cleanup=load(prune/'cleanup-receipt.json');guard=load(prune/'handles-terminal.json');assert guard['exitCode']==0;handles=json.loads(guard['stdout']);assert handles['referencingProcesses']==handles['restartManagerFileHandleOwners']==0
result_relative='github-sync/checkpoint-curios-attended-reload-v18-cleanup-result-20261114.json';result={'format':'ftboss-verified-source-cache-cleanup-result-v3','bindingSourceCommit':feature['commit'],'branch':BRANCH,'planRelativePath':plan_relative,'planSha256':sha(ROOT/plan_relative),'remoteProofSha256':sha(proof_path),'deletionReceiptSha256':sha(prune/'cleanup-receipt.json'),'deletedFiles':cleanup['deletedFiles'],'deletedFileCount':cleanup['deletedFileCount'],'logicalFreedBytes':cleanup['logicalFreedBytes'],'observedSystemFreeSpaceDeltaBytes':cleanup['observedSystemFreeSpaceDeltaBytes'],'observedSpaceIncludesOtherApplicationNoise':True,'referencingProcesses':0,'fileHandleOwners':0,'unexpectedRemovedPaths':cleanup['unexpectedRemovedPaths'],'canonicalSourcesAndFailedEvidenceRetained':True,'nativeAcceptanceClaimed':False,'formalInstancesReadOnly':True,'installable':False,'deployed':False};create(ROOT/result_relative,json.dumps(result,ensure_ascii=False,indent=2))
stage([result_relative],'cleanup');final_path,final=publish('cleanup','Record verified attended reload diagnostic duplicate-cache cleanup')
summary={'sourceDiagnosticPublished':True,'nativeAccepted':False,'remainingActualUserInteractionGate':True,'featureCommit':feature['commit'],'finalCommit':final['commit'],'finalTree':final['tree'],'finalRemoteProofRelativePath':final_path.relative_to(ROOT).as_posix(),'trackedFiles':final['allRemoteTrackedFilesPathSizeSha256Verified'],'canonicalFiles':final['currentCanonicalSourceFilesVerified'],'presentCacheFiles':final['sparsePresentFilesVerified'],'absentCacheFiles':final['plannedAbsentPathsVerified'],'archive':archive_receipt,'cleanupPlanRelativePath':plan_relative,'deletedDuplicateSourceFiles':cleanup['deletedFileCount'],'logicalFreedBytes':cleanup['logicalFreedBytes'],'observedFreeSpaceDeltaBytes':cleanup['observedSystemFreeSpaceDeltaBytes'],'allFailedWorldsAndRawArtifactsRetained':True,'currentWaitingWindowExists':False,'installable':False,'deployed':False};save(OUT/'publication-closeout.json',summary);print(json.dumps(summary),flush=True)
