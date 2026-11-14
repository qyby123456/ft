import ast
import ctypes
from ctypes import wintypes
import datetime
import hashlib
import json
import os
import subprocess
import sys
import time
import traceback
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
OUT=WORK/'reports/native-signed-acceptance-pipeline-v2-cf217105'
CANDIDATE=WORK/'reports/native-repair-material-proc-integration-candidate-cf217105.json'
ORIGINAL=WORK/'build/repair-runtime-b3299174'
ORIGINAL_REPORT=WORK/'reports/native-repair-runtime-material-proc-whole-nineteen-cf217105.json'
CANCEL=OUT/'cancel-request.json'
PYTHON='C:/Users/qyby/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe'
THREAD='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
FORMAL={
 'Forge':('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar','7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3'),
 'NeoForge':('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_NeoForge 机械动力/mods/qys-bosses-1.21.1.jar','58002a729b48fba41bd4f4832b967760879114b214b93ff0729dfc8680483dc3')}

def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
 with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
 with Path(path).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
def now():return datetime.datetime.now().astimezone().isoformat()

kernel=ctypes.WinDLL('kernel32',use_last_error=True)
kernel.OpenProcess.argtypes=[wintypes.DWORD,wintypes.BOOL,wintypes.DWORD];kernel.OpenProcess.restype=wintypes.HANDLE
kernel.WaitForSingleObject.argtypes=[wintypes.HANDLE,wintypes.DWORD];kernel.WaitForSingleObject.restype=wintypes.DWORD
kernel.CloseHandle.argtypes=[wintypes.HANDLE];kernel.CloseHandle.restype=wintypes.BOOL

def process_handle(pid):return kernel.OpenProcess(0x00100000,False,int(pid))
def live(handle):return bool(handle) and kernel.WaitForSingleObject(handle,0)==258

def allowed():
 owner=load(ROOT/'WRITE_OWNER.json')
 assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']==THREAD
 assert owner['currentGoalStatus']=='active' and owner['noNewWeaponRuns'] is False
 assert owner['currentFormalInstancePolicy']=='read_only_current_goal_no_deployment'
 assert not CANCEL.exists(),'Explicit cancellation: no further stage may start'

def state(stage,**facts):
 value={'updatedAt':now(),'pid':os.getpid(),'stage':stage,'goalStatus':'active','installable':False,'deployed':False,**facts}
 temporary=OUT/'status.next.json'
 temporary.write_text(json.dumps(value,ensure_ascii=False,indent=2),encoding='utf-8');temporary.replace(OUT/'status.json')
 with (OUT/'events.jsonl').open('a',encoding='utf-8') as stream:stream.write(json.dumps(value,ensure_ascii=False)+'\n')
 print(json.dumps(value,ensure_ascii=False),flush=True)

def verify_candidate():
 assert sha(CANDIDATE)=='78eedf330a3e8da7f3823b1bd3e5566ef7d3f65937f801896c7ba8c41064f586'
 bound=load(CANDIDATE)
 for name,wanted in bound['inputHashes'].items():assert sha(name)==wanted,name
 for path,wanted in FORMAL.values():assert sha(path)==wanted,path


def accepted(report,stage):
 result=load(report);attempt=Path(result['attempt']);terminal=load(attempt/'terminal308.json');probe=result['probe']
 assert attempt.resolve().is_relative_to(WORK/'build') and sha(CANDIDATE)=='78eedf330a3e8da7f3823b1bd3e5566ef7d3f65937f801896c7ba8c41064f586'
 assert result['inputHashes'].get(str(CANDIDATE))==sha(CANDIDATE),'Runtime must bind the exact current candidate'
 if stage=='whole-nineteen':
  observer=WORK/'tests/private-image-failure-diagnostic-acf29373/PositiveWeapon436.java'
  assert attempt==ORIGINAL and sha(observer)=='4aefe0f2652b52aa5c64ca64da2210a00d0e9589c1e6bbd74e15976582131ad9'
  seed_report=WORK/'reports/native-repair-runtime-exact-melee-configured-paid-849680bb.json'
 elif stage=='new-jvm':
  observer=WORK/'tests/material-proc-nineteen-restart-cf217105/PositiveWeapon436.java'
  seed_report=ORIGINAL_REPORT
  assert attempt!=ORIGINAL
  preparation=load(WORK/'reports/material-proc-nineteen-restart-preparation-cf217105.json')
  assert preparation['inputHashes'][str(observer)]==sha(observer)
 else:
  assert stage=='foreign-controls'
  observer=WORK/'tests/exact-melee-configured-controls-849680bb/PositiveWeapon436.java'
  assert sha(observer)=='5f69724855898aee57da364a44c8422b7f92c39c6546811e9ec0846e30251a4c'
  seed_report=WORK/'reports/native-repair-runtime-exact-melee-configured-paid-849680bb.json'
 assert result['inputHashes'].get(str(observer))==sha(observer),'Original observer binding differs'
 assert result['inputHashes'].get(str(seed_report))==sha(seed_report),'Original accepted seed receipt differs'
 assert Path(load(attempt/'restart-world-seed.json')['seedAttempt'])==Path(load(seed_report)['attempt'])

 assert result['exitCode']==0 and probe['failures']==0 and result['privateReadGuardProbe']['failures']==0
 assert terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave']
 assert probe['authorityWrittenByReflection'] is False and probe['installable'] is False
 ids=[row['id'] for row in load(WORK/'reports/native-remaining-variant-gap-audit-3320e085.json')['groups']['otherExactOriginalClassesStillNeedActualMatrix']]
 assert len(ids)==19 and [row['id'] for row in probe['cases']]==ids
 for name,wanted in result['inputHashes'].items():assert sha(name)==wanted,name
 if stage=='whole-nineteen':
  assert probe['nineteenConfiguredOriginalMeleeGenerationTwoFullSignedSessions'] is True
  for case in probe['cases']:
   assert case['genuineNonNullLeaseAndGenerationBeforeAttack'] and case['sameLeaseAndGenerationAfterAttack']
   assert case['actualOriginalPostCombatSaveAuthenticatedCommitted'] and case['publications']
   for row in case['publications']:
    assert row['traceComplete'] and row['session_coverageInstalled'] and not row['session_traceRevoked']
    assert row['prepared_sealed'] and row['prepared_bound'] and not row['prepared_unsupported'] and not row['prepared_retired']
    assert row['session_next']==row['originalExpectedReturnedStepCount']>0
    assert row['originalQuotedNativePutCount']==row['originalReturnedNativeLeafCount']
    assert row['originalQuotedTargetWriteCount']==row['originalReturnedTargetLeafCount']
    assert row['exactOriginalNativeBindingAndTargetReturnIdentityCoverage']
    assert all(group['done'] and group['exactQuotedKeyCoverage'] for group in row['actualOriginalCompletedSemanticGroups'])
 elif stage=='new-jvm':
  assert probe['nineteenConfiguredOriginalMeleePostCombatContinuationAcrossNewProcess'] is True
  for case in probe['cases']:
   assert all(case[key] is True for key in ['originalWholeItemAndWearCoherent','genuineNonNullLeaseAndGeneration','sameEpochGenerationTwoToThree','consumedReplayDeniedWithoutChangingFirstSuccessor','actualOriginalGenerationThreeSaveCommitted'])
 else:
  assert stage=='foreign-controls'
  for case in probe['cases']:
   assert all(case[key] is True for key in ['originalForeignCallbackExactlyOnce','unknownNonNativeMaterialStillOriginalAndAllSlotRulesReject','noPrivatePermissionOrAuthorityReissued','originalCallbackExceptionIdentityPreservedWithLiveUnchangedBeforeMutation','foreignOriginalMutationRetiresGenuineAuthority','genuineGenerationTwoBeforeControl'])
 return result


def archive_stage(stage,report):
 verify_candidate();result=accepted(report,stage);attempt=Path(result['attempt'])
 directory=OUT/stage;directory.mkdir()
 rows={Path(__file__),CANDIDATE,report,ROOT/'AGENTS.md',ROOT/'WRITE_OWNER.json',ROOT/'WRITE_OWNER.claim',WORK/'MATERIAL_PROC_NATIVE_INTEGRATION_20261112.md',WORK/'reports/material-proc-native-implementation-checkpoint-20261112/archive-receipt.json'}
 rows.update(path for path in attempt.iterdir() if path.is_file())
 rows.update(path for path in (attempt/'server').glob('actual-*.json'))
 for name in ['logs/latest.log','logs/debug.log']:
  path=attempt/'server'/name
  if path.is_file():rows.add(path)
 world=attempt/'server/world'
 rows.update(path for path in world.rglob('*') if path.is_file())
 for name in result['inputHashes']:
  path=Path(name)
  if path.resolve().is_relative_to(ROOT):rows.add(path)
 facts={'createdAt':now(),'stage':stage,'acceptedScope':'original nineteen configured melee items only','runtimeReport':str(report),
  'runtimeReportSha256':sha(report),'acceptedSave':stage!='foreign-controls','worldMaySeedOnlyMatchingAcceptedScope':stage!='foreign-controls',
  'fullSixFamilyFinalAcceptance':False,'fullGoalComplete':False,'installable':False,'deployed':False,
  'frozenClassGraphArchiveReceipt':'work/session-lease/reports/material-proc-native-implementation-checkpoint-20261112/archive-receipt.json'}
 save(directory/'acceptance.json',facts);rows.add(directory/'acceptance.json')
 archive=WORK/'archives'/('material-proc-cf217105-'+stage+'-20261112.zip');manifest={}
 state('archiving-'+stage,attempt=str(attempt),worldFiles=sum(1 for path in world.rglob('*') if path.is_file()))
 with zipfile.ZipFile(archive,'x',compression=zipfile.ZIP_DEFLATED) as bundle:
  for path in sorted(rows):
   assert path.is_file() and path.resolve().is_relative_to(ROOT) and not path.is_symlink(),path
   name=path.relative_to(ROOT).as_posix();manifest[name]={'sha256':sha(path),'bytes':path.stat().st_size};bundle.write(path,name)
  encoded=json.dumps(manifest,ensure_ascii=False,indent=2).encode('utf-8');bundle.writestr('ARCHIVE_MANIFEST.json',encoded)
 with zipfile.ZipFile(archive) as bundle:
  assert bundle.testzip() is None and bundle.read('ARCHIVE_MANIFEST.json')==encoded
  for name,entry in manifest.items():assert hashlib.sha256(bundle.read(name)).hexdigest()==entry['sha256'],name
 receipt={'archive':str(archive),'sha256':sha(archive),'fileCount':len(manifest),'failures':0,'allMembersReadBackAndHashVerified':True,**facts}
 save(directory/'archive-receipt.json',receipt)
 state('archived-'+stage,archive=str(archive),fileCount=len(manifest),fullGoalComplete=False)
 return receipt


def run_stage(stage,script,report):
 allowed();verify_candidate()
 assert not report.exists(),str(report)
 command=[PYTHON,'-B',str(script)];save(OUT/(stage+'-command.json'),command)
 environment=dict(os.environ);environment['PYTHONIOENCODING']='utf-8';environment['PYTHONDONTWRITEBYTECODE']='1'
 log=OUT/(stage+'-supervisor.log')
 with log.open('xb') as stream:
  child=subprocess.Popen(command,cwd=ROOT,env=environment,stdout=stream,stderr=subprocess.STDOUT)
  save(OUT/(stage+'-supervisor.json'),{'pid':child.pid,'script':str(script),'startedAt':now()})
  state('running-'+stage,supervisorPid=child.pid,script=str(script),log=str(log))
  published=False
  while child.poll() is None:
   if not published:
    for line in log.read_text(encoding='utf-8',errors='replace').splitlines():
     try:value=ast.literal_eval(line)
     except (SyntaxError,ValueError):continue
     if isinstance(value,dict) and 'attempt' in value and 'pid' in value:
      state('running-'+stage,supervisorPid=child.pid,nativePid=value['pid'],attempt=value['attempt'],log=str(log));published=True;break
   time.sleep(2)
 save(OUT/(stage+'-supervisor-terminal.json'),{'exitCode':child.returncode,'finishedAt':now()})
 assert child.returncode==0,str(log)
 accepted(report,stage)
 return archive_stage(stage,report)


assert not OUT.exists(),str(OUT)
allowed();verify_candidate();OUT.mkdir()
save(OUT/'pipeline.claim',{'pid':os.getpid(),'threadId':THREAD,'createdAt':now(),'oneHeavyNativeJvmAtATime':True})
handles=[]
try:
 handles=[process_handle(58040),process_handle(7984)]
 save(OUT/'inputs.json',{'candidate':str(CANDIDATE),'candidateSha256':sha(CANDIDATE),'scriptSha256':sha(Path(__file__)),
  'existingAttempt':str(ORIGINAL),'existingNativePid':58040,'existingSupervisorPid':7984,'classGraphArchiveReceipt':str(WORK/'reports/material-proc-native-implementation-checkpoint-20261112/archive-receipt.json')})
 state('waiting-original-whole-nineteen',nativePid=58040,supervisorPid=7984,attempt=str(ORIGINAL),actualNativeLive=live(handles[0]),actualSupervisorLive=live(handles[1]))
 while any(live(handle) for handle in handles):
  allowed()
  if live(handles[0]) and not live(handles[1]):raise RuntimeError('Original native remains live after supervisor ended; preserve it and inspect, do not restart')
  time.sleep(3)
 assert ORIGINAL_REPORT.is_file() and (ORIGINAL/'terminal308.json').is_file(),'Original run ended without its exact terminal evidence'
 accepted(ORIGINAL_REPORT,'whole-nineteen');whole=archive_stage('whole-nineteen',ORIGINAL_REPORT)
 new_jvm=run_stage('new-jvm',WORK/'tools/run_native_material_proc_nineteen_restart_cf217105.py',WORK/'reports/native-repair-runtime-material-proc-nineteen-restart-cf217105.json')
 controls=run_stage('foreign-controls',WORK/'tools/run_native_original_nineteen_controls_cf217105.py',WORK/'reports/native-repair-runtime-original-nineteen-controls-cf217105.json')
 summary={'failures':0,'finishedAt':now(),'originalNineteenCompleteSignedSessions':True,'originalNineteenNewJvmContinuationAndConsumedReplay':True,
  'originalNineteenForeignControls':True,'archiveReceipts':[whole,new_jvm,controls],'remainingFinalAcceptanceAudit':'work/session-lease/reports/final-acceptance-audit-5f283607-20261112/requirements-and-evidence.json',
  'authenticatedMultiplayerAccepted':False,'allSixFamiliesAnd103VariantsAccepted':False,'fullGoalComplete':False,'installable':False,'deployed':False}
 verify_candidate();save(OUT/'terminal.json',summary);state('three-original-nineteen-stages-complete',fullGoalComplete=False)
except BaseException as failure:
 save(OUT/'terminal.json',{'failures':1,'finishedAt':now(),'error':str(failure),'trace':traceback.format_exc(),'noFailedOrPartialWorldUsedAsNewSeed':True,'fullGoalComplete':False,'installable':False,'deployed':False})
 state('stopped-on-exact-stage-failure',error=str(failure),fullGoalComplete=False)
 raise
finally:
 for handle in handles:
  if handle:kernel.CloseHandle(handle)
