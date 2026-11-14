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
configuration=json.loads(Path(sys.argv[1]).read_text(encoding='utf-8'))
OUT=WORK/'reports/github-nonmanor-finale-effective-audit-publication-v1-20261114'
owner=json.loads((ROOT/'WRITE_OWNER.json').read_text(encoding='utf-8-sig'))
assert owner['activeWriterThreadId']==json.loads((ROOT/'WRITE_OWNER.claim').read_text(encoding='utf-8-sig'))['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and not owner['activeRuntimePids']
OUT.mkdir()
shared=WORK/'tools/publish_curios_lifecycle_increment_v1.py';selected={'load','save','sha','git','present','network','stage','publish'}
syntax=ast.parse(shared.read_text(encoding='utf-8'));functions=[node for node in syntax.body if isinstance(node,ast.FunctionDef) and node.name in selected]
assert {node.name for node in functions}==selected
exec(compile(ast.Module(body=functions,type_ignores=[]),str(shared),'exec'),globals())
helper=WORK/'tools/prepare_boss_blood_tide_stage10.py';namespace={'__file__':str(helper),'__name__':'patch_helper_only'}
exec(compile(helper.read_text(encoding='utf-8').split('probe=(WORK/')[0],str(helper),'exec'),namespace);create=namespace['create']
proxy=os.environ['FT_TASK_PROXY'];assert proxy.startswith('http://127.0.0.1:')
assert git(REPO,'rev-parse','HEAD').decode().strip()==git(RECOVERY,'rev-parse','HEAD').decode().strip()==configuration['parentSourceCommit']
assert not git(REPO,'status','--porcelain').strip()
query="Get-CimInstance Win32_Process -Filter \"Name='java.exe' OR Name='javaw.exe'\" | Where-Object { $_.CommandLine -match 'session-boss|session-lease' } | Select-Object -ExpandProperty ProcessId"
assert not subprocess.check_output(['powershell','-NoProfile','-Command',query],encoding='utf-8-sig').strip()
report=load(ROOT/configuration['nativeReport']);assert report['nativeArithmeticAccepted'] and not report['actualBossAdmissionAccepted']
assert report['probe']['failures']==0 and report['probe']['checks']==24 and len(report['probe']['cases'])==6
assert report['terminal']['exitCode']==0 and report['terminal']['completeSave'] and report['terminal']['clientOrServerProcessReaped'] and report['terminal']['inputHashesUnchanged'] and report['terminal']['serverConfigurationSemanticsPreserved']
for name,expected in report['inputHashes'].items():assert sha(name)==expected,name
assert report['nativeLoaderContext']['dedicatedServer'] and report['nativeLoaderContext']['actualPlayerCount']==0 and report['nativeLoaderContext']['neoforgeVersion']=='21.1.233'
audit=WORK/'build/boss-nonmanor-finale-effective-v1-20261114';mapping=load(audit/'attribute-collection-mapping-proof.json')
assert mapping['resolvedOriginalMethod']=='AttributeMap.getSyncableAttributes()' and not mapping['originalAndCurrentCollectionScopeDiffer']
scan=load(audit/'callsites-and-mixin-targets.json');assert len(scan['declaredRuntimeArchiveMixinTargets'])==470 and not scan['unresolvedRuntimeArchiveMixinDeclarations']
original=ROOT/'reference/forge-current.jar';built=load(WORK/'build/boss-zicai-curio-silence-v1-20261113/candidate-build-receipt.json')
assert sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
assert sha(built['jar'])=='c45cc10ffb7b572d876e869b692a75496c88c337434ed7140225df298b7bb65e'
paths={Path(__file__),Path(sys.argv[1]).resolve(),ROOT/configuration['nativeReport']}
paths.update(path for path in audit.rglob('*') if path.is_file())
for relative in configuration['sources']:paths.add(ROOT/relative)
for relative in configuration['nativeReports']:
    native_path=ROOT/relative;paths.add(native_path)
    paths.update(path for path in Path(load(native_path)['attempt']).rglob('*') if path.is_file())
base=WORK/'archives/boss-core-stage28-zicai-curio-scoped-evidence-v3-20261113.zip';assert sha(base)=='aca9e96def7887e0cf52637152df8922174326d5970376918d42b08bee864929'
with zipfile.ZipFile(base) as bundle:available=set(bundle.namelist())
rows={};unique={}
for path in sorted(paths):
    assert path.is_relative_to(ROOT) and not path.is_symlink();digest=sha(path);layer='base' if 'blobs/'+digest in available else 'self'
    rows[path.relative_to(ROOT).as_posix()]={'bytes':path.stat().st_size,'sha256':digest,'layer':layer}
    if layer=='self':unique[digest]=path
archive=WORK/'archives/boss-nonmanor-finale-effective-audit-v1-20261114.zip'
with zipfile.ZipFile(archive,'x',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as bundle:
    bundle.writestr('ARCHIVE_MANIFEST.json',json.dumps({'format':'ftboss-content-addressed-increment-v1','baseArchiveSha256':sha(base),'files':rows},ensure_ascii=False,indent=2).encode('utf-8'))
    for digest,path in sorted(unique.items()):bundle.write(path,'blobs/'+digest)
with (OUT/'archive-validation.log').open('xb') as stream:subprocess.run([sys.executable,'-B',str(WORK/'tools/restore_boss_zicai_curio_archive_v2.py'),str(archive),'--base',str(base)],stdout=stream,stderr=subprocess.STDOUT,check=True)
archive_receipt={'archiveRelativePath':archive.relative_to(ROOT).as_posix(),'archiveSha256':sha(archive),'archiveBytes':archive.stat().st_size,'baseArchiveRelativePath':base.relative_to(ROOT).as_posix(),'baseArchiveSha256':sha(base),'paths':len(rows),'allReferencedPayloadHashesVerified':True,'fullPhysicalRestoreClaimed':False,'rawArtifactsRemoteRecoveryVerified':False,'allOriginalsAndFailedAttemptsRetained':True};save(OUT/'archive-receipt.json',archive_receipt)
checkpoint_relative='github-sync/checkpoint-nonmanor-finale-audit-source-20261114.json'
plan_relative='github-sync/checkpoint-nonmanor-finale-audit-cleanup-plan-20261114.json'
checkpoint={'format':'ftboss-nonmanor-finale-audit-source-checkpoint-v1','branch':BRANCH,'parentSourceCommit':configuration['parentSourceCommit'],'nativeArithmeticAccepted':True,'nativeArithmeticChecks':24,'actualBossAdmissionOrRuntimeDefectAccepted':False,'mappingScopeHypothesisCorrected':True,'nativeReportRelativePath':configuration['nativeReport'],'nativeReportSha256':sha(ROOT/configuration['nativeReport']),'archive':archive_receipt,'sourceFiles':{relative:{'bytes':(ROOT/relative).stat().st_size,'sha256':sha(ROOT/relative)} for relative in configuration['sources']},'productChanged':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False,'formalInstancesReadOnly':True,'weaponsAndSessionPaused':True,'actualUserConfirmationGateNotRetried':True}
create(ROOT/checkpoint_relative,json.dumps(checkpoint,ensure_ascii=False,indent=2))
keep=set(present());keep.update(PREFIX+relative for relative in configuration['sources'] if relative not in configuration['pruneSources']);keep.update([PREFIX+checkpoint_relative,PREFIX+plan_relative])
plan={'format':'ftboss-verified-source-cache-cleanup-plan-v3','branch':BRANCH,'targetRelativePath':'github-sync/repositories/ft','previousPlanRelativePaths':configuration['previousPlans'],'deleteFiles':{PREFIX+relative:{'sha256':sha(ROOT/relative),'bytes':(ROOT/relative).stat().st_size} for relative in configuration['pruneSources']},'mandatoryKeepFiles':sorted(keep),'canonicalFrozenSourcesFailedWorldsAndRawArtifactsExcluded':True,'largeArtifactsRemoteRecoveryVerified':False}
create(ROOT/plan_relative,json.dumps(plan,ensure_ascii=False,indent=2))
stage(configuration['sources']+[checkpoint_relative,plan_relative],'feature');proof_path,feature=publish('feature','Correct finale attribute mapping hypothesis and preserve native arithmetic evidence')
prune=OUT/'cache-prune';subprocess.run([sys.executable,'-B',str(WORK/'tools/prune_github_verified_source_cache_v3.py'),str(ROOT/plan_relative),str(proof_path),'--apply','--output',str(prune)],check=True)
cleanup=load(prune/'cleanup-receipt.json');guard=load(prune/'handles-terminal.json');assert guard['exitCode']==0;handles=json.loads(guard['stdout']);assert handles['referencingProcesses']==handles['restartManagerFileHandleOwners']==0
result_relative='github-sync/checkpoint-nonmanor-finale-audit-cleanup-result-20261114.json'
result={'format':'ftboss-verified-source-cache-cleanup-result-v3','bindingSourceCommit':feature['commit'],'branch':BRANCH,'planRelativePath':plan_relative,'planSha256':sha(ROOT/plan_relative),'remoteProofSha256':sha(proof_path),'deletionReceiptSha256':sha(prune/'cleanup-receipt.json'),'deletedFiles':cleanup['deletedFiles'],'deletedFileCount':cleanup['deletedFileCount'],'logicalFreedBytes':cleanup['logicalFreedBytes'],'observedSystemFreeSpaceDeltaBytes':cleanup['observedSystemFreeSpaceDeltaBytes'],'observedSpaceIncludesOtherApplicationNoise':True,'referencingProcesses':0,'fileHandleOwners':0,'unexpectedRemovedPaths':cleanup['unexpectedRemovedPaths'],'canonicalSourcesAndAllFailedEvidenceRetained':True,'actualBossAdmissionAccepted':False,'nativeArithmeticOnlyAccepted':True,'formalInstancesReadOnly':True,'installable':False,'deployed':False}
create(ROOT/result_relative,json.dumps(result,ensure_ascii=False,indent=2))
stage([result_relative],'cleanup');final_path,final=publish('cleanup','Record verified finale audit source cache cleanup with original evidence retained')
summary={'sourceAuditPublished':True,'featureCommit':feature['commit'],'finalCommit':final['commit'],'finalTree':final['tree'],'finalRemoteProofRelativePath':final_path.relative_to(ROOT).as_posix(),'trackedFiles':final['allRemoteTrackedFilesPathSizeSha256Verified'],'canonicalFiles':final['currentCanonicalSourceFilesVerified'],'presentCacheFiles':final['sparsePresentFilesVerified'],'absentCacheFiles':final['plannedAbsentPathsVerified'],'archive':archive_receipt,'cleanupPlanRelativePath':plan_relative,'deletedDuplicateSourceFiles':cleanup['deletedFileCount'],'logicalFreedBytes':cleanup['logicalFreedBytes'],'observedFreeSpaceDeltaBytes':cleanup['observedSystemFreeSpaceDeltaBytes'],'nativeArithmeticChecks':24,'nativeArithmeticAccepted':True,'actualBossAdmissionAccepted':False,'actualBossRuntimeDefectProven':False,'productChanged':False,'newJvmUserConfirmationGateNotRetried':True,'allFailedWorldsAndRawArtifactsRetained':True,'fullBossMigrationComplete':False,'installable':False,'deployed':False}
save(OUT/'publication-closeout.json',summary);print(json.dumps(summary),flush=True)
