import ast
import hashlib
import json
import os
import shutil
import subprocess
import sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
REPO=ROOT/'github-sync/repositories/ft'
RECOVERY=ROOT/'github-sync/repositories/ft-checkpoint-recovery-20261113'
BRANCH='codex/neoforge-source-20261113'
PREFIX='neoforge/ftboss/'
configuration_path=Path(sys.argv[1]).resolve();configuration=json.loads(configuration_path.read_text(encoding='utf-8'))
OUT=WORK/'reports/github-nonmanor-ordinary-native-publication-v1-20261114'
owner=json.loads((ROOT/'WRITE_OWNER.json').read_text(encoding='utf-8-sig'))
assert owner['activeWriterThreadId']==json.loads((ROOT/'WRITE_OWNER.claim').read_text(encoding='utf-8-sig'))['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and not owner['activeRuntimePids']
OUT.mkdir()
shared=WORK/'tools/publish_curios_lifecycle_increment_v1.py';selected={'load','save','sha','git','present','network','stage','publish'}
syntax=ast.parse(shared.read_text(encoding='utf-8'));functions=[node for node in syntax.body if isinstance(node,ast.FunctionDef) and node.name in selected];assert {node.name for node in functions}==selected
exec(compile(ast.Module(body=functions,type_ignores=[]),str(shared),'exec'),globals())
helper=WORK/'tools/prepare_boss_blood_tide_stage10.py';namespace={'__file__':str(helper),'__name__':'patch_helper_only'}
exec(compile(helper.read_text(encoding='utf-8').split('probe=(WORK/')[0],str(helper),'exec'),namespace);create=namespace['create']
proxy=os.environ['FT_TASK_PROXY'];assert proxy.startswith('http://127.0.0.1:')
assert git(REPO,'rev-parse','HEAD').decode().strip()==git(RECOVERY,'rev-parse','HEAD').decode().strip()==configuration['parentSourceCommit'];assert not git(REPO,'status','--porcelain').strip()
query="Get-CimInstance Win32_Process -Filter \"Name='java.exe' OR Name='javaw.exe'\" | Where-Object { $_.CommandLine -match 'session-boss|session-lease' } | Select-Object -ExpandProperty ProcessId"
assert not subprocess.check_output(['powershell','-NoProfile','-Command',query],encoding='utf-8-sig').strip()
admission=load(ROOT/configuration['admissionReport']);reload=load(ROOT/configuration['reloadReport']);failed=load(ROOT/configuration['failedReloadReport'])
assert admission['actualNormalBossAdmissionBaselineAccepted'] and admission['probe']['checks']==2189 and len(admission['probe']['cases'])==20
assert failed['probe']['failures']==1 and not failed['ordinaryNoPlayerNewJvmEntityReloadAccepted'] and 'public clone state persisted' in failed['probe']['failure']
assert reload['ordinaryNoPlayerEighteenEntityReloadAccepted'] and not reload['originalTwentyEntityStrictReloadAccepted'] and not reload['publicCloneFixturePersistenceAccepted']
assert reload['probe']['failures']==0 and reload['probe']['checks']==configuration['acceptedReloadChecks'] and len(reload['probe']['cases'])==20 and len(reload['probe']['serverStoppingSnapshots'])==18 and reload['probe']['naturalTicksAfterAllTwentyLoaded']==40
for data in [admission,reload,failed]:
    terminal=data['terminal'];assert terminal['exitCode']==0 and terminal['processReaped'] and terminal['completeSave'] and terminal['inputHashesUnchanged'] and terminal['criticalServerConfigurationPreserved']
    for name,expected in data['inputHashes'].items():assert sha(name)==expected,name
assert reload['terminal']['originalAcceptedSeedWorldHashesUnchanged'] and reload['probe']['actualPlayerCount']==0
archive_receipt=load(ROOT/configuration['archiveReceipt']);assert archive_receipt['allReferencedPayloadHashesVerified'] and archive_receipt['allLocalInputPathsRehashedAfterArchive']
assert sha(ROOT/archive_receipt['archiveRelativePath'])==archive_receipt['archiveSha256']
for relative,expected in configuration['formalReadOnlyHashes'].items():assert sha(Path(relative))==expected,relative
assert len(set(configuration['sources']))==len(configuration['sources']);assert set(configuration['pruneSources']).issubset(configuration['sources'])
checkpoint_relative='github-sync/checkpoint-nonmanor-ordinary-native-source-20261114.json'
plan_relative='github-sync/checkpoint-nonmanor-ordinary-native-cleanup-plan-20261114.json'
checkpoint={'format':'ftboss-nonmanor-ordinary-native-source-checkpoint-v1','branch':BRANCH,'parentSourceCommit':configuration['parentSourceCommit'],'ordinaryNoPlayerEntityJoinAccepted':True,'ordinaryEntityJoinCases':20,'ordinaryEntityJoinChecks':2189,'ordinaryNoPlayerEighteenEntityPersistenceAccepted':True,'ordinaryPersistenceCases':18,'ordinaryReloadChecks':configuration['acceptedReloadChecks'],'allOriginalTwentyEntityPersistenceAccepted':False,'publicCloneFixturePersistenceAccepted':False,'firstStrictReloadFailurePreserved':True,'nativeNumericBitsComparedExactly':True,'originalRuntimeClonePersistenceClaimed':False,'productDefectProven':False,'archive':archive_receipt,'sourceFiles':{relative:{'bytes':(ROOT/relative).stat().st_size,'sha256':sha(ROOT/relative)} for relative in configuration['sources']},'authenticatedSummonAccepted':False,'actualImpossibleAdmissionAccepted':False,'playerCountScalingChangeAccepted':False,'curiosActualPlayerNewJvmLoginAccepted':False,'actualUserConfirmationGateNotRetried':True,'productChanged':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False,'formalInstancesReadOnly':True,'weaponsAndSessionPaused':True}
create(ROOT/checkpoint_relative,json.dumps(checkpoint,ensure_ascii=False,indent=2))
keep=set(present());keep.update(PREFIX+relative for relative in configuration['sources'] if relative not in configuration['pruneSources']);keep.update([PREFIX+checkpoint_relative,PREFIX+plan_relative])
plan={'format':'ftboss-verified-source-cache-cleanup-plan-v3','branch':BRANCH,'targetRelativePath':'github-sync/repositories/ft','previousPlanRelativePaths':configuration['previousPlans'],'deleteFiles':{PREFIX+relative:{'sha256':sha(ROOT/relative),'bytes':(ROOT/relative).stat().st_size} for relative in configuration['pruneSources']},'mandatoryKeepFiles':sorted(keep),'canonicalFrozenSourcesFailedWorldsAndRawArtifactsExcluded':True,'largeArtifactsRemoteRecoveryVerified':False}
create(ROOT/plan_relative,json.dumps(plan,ensure_ascii=False,indent=2))
stage(configuration['sources']+[checkpoint_relative,plan_relative],'feature');proof_path,feature=publish('feature','Archive ordinary Boss admission and scoped native persistence with clone counterexamples preserved')
prune=OUT/'cache-prune';subprocess.run([sys.executable,'-B',str(WORK/'tools/prune_github_verified_source_cache_v3.py'),str(ROOT/plan_relative),str(proof_path),'--apply','--output',str(prune)],check=True)
cleanup=load(prune/'cleanup-receipt.json');guard=load(prune/'handles-terminal.json');assert guard['exitCode']==0;handles=json.loads(guard['stdout']);assert handles['referencingProcesses']==handles['restartManagerFileHandleOwners']==0
result_relative='github-sync/checkpoint-nonmanor-ordinary-native-cleanup-result-20261114.json'
result={'format':'ftboss-verified-source-cache-cleanup-result-v3','bindingSourceCommit':feature['commit'],'branch':BRANCH,'planRelativePath':plan_relative,'planSha256':sha(ROOT/plan_relative),'remoteProofSha256':sha(proof_path),'deletionReceiptSha256':sha(prune/'cleanup-receipt.json'),'deletedFiles':cleanup['deletedFiles'],'deletedFileCount':cleanup['deletedFileCount'],'logicalFreedBytes':cleanup['logicalFreedBytes'],'observedSystemFreeSpaceDeltaBytes':cleanup['observedSystemFreeSpaceDeltaBytes'],'observedSpaceIncludesOtherApplicationNoise':True,'referencingProcesses':0,'fileHandleOwners':0,'unexpectedRemovedPaths':cleanup['unexpectedRemovedPaths'],'canonicalSourcesAndAllFailedEvidenceRetained':True,'formalInstancesReadOnly':True,'installable':False,'deployed':False}
create(ROOT/result_relative,json.dumps(result,ensure_ascii=False,indent=2));stage([result_relative],'cleanup');final_path,final=publish('cleanup','Record independently recovered ordinary Boss source cache cleanup')
summary={'sourceIncrementPublished':True,'featureCommit':feature['commit'],'finalCommit':final['commit'],'finalTree':final['tree'],'finalRemoteProofRelativePath':final_path.relative_to(ROOT).as_posix(),'trackedFiles':final['allRemoteTrackedFilesPathSizeSha256Verified'],'canonicalFiles':final['currentCanonicalSourceFilesVerified'],'presentCacheFiles':final['sparsePresentFilesVerified'],'absentCacheFiles':final['plannedAbsentPathsVerified'],'archive':archive_receipt,'cleanupPlanRelativePath':plan_relative,'deletedDuplicateSourceFiles':cleanup['deletedFileCount'],'ordinaryNoPlayerJoinCases':20,'ordinaryNoPlayerPersistenceCases':18,'allTwentyPersistenceAccepted':False,'allFailedWorldsAndRawArtifactsRetained':True,'productChanged':False,'curiosActualPlayerNewJvmLoginAccepted':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False}
save(OUT/'publication-closeout.json',summary);print(json.dumps(summary),flush=True)
