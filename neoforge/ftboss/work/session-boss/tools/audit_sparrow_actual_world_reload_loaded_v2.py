import hashlib
import json
import re
import subprocess
import sys
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/sparrow-actual-world-reload-loaded-audit-v2-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):(OUT/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run(name,command):
    result=subprocess.run(command,capture_output=True,timeout=120,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.txt')).write_bytes(result.stdout+result.stderr);save(name+'-terminal.json',{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.txt'));return result.stdout.decode('utf-8').strip()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids'] and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
report_path=Path(sys.argv[1]).resolve();native=load(report_path);probe=native['probe'];assert native['actualNativeWorldReloadAndPeriodicPruneAccepted'] and probe['actualNativeWorldReloadAndPeriodicPruneAccepted'] and probe['actualOriginalRecordLoadedByNativeSavedData'] and probe['nativeWorldRestartAccepted'] and probe['failures']==0 and not native['productChanged']
assert probe['publicOpenWorldInvoked'] and probe['copiedActualPriorNativeWorld'] and probe['actualClientAndIntegratedServer']
assert probe['actualNativeBackupConfirmation']['screenClass']=='net.minecraft.client.gui.screens.BackupConfirmScreen'
assert any(row['key']=='selectWorld.backupJoinSkipButton' and row['active'] and row['visible'] for row in probe['actualNativeBackupConfirmation']['buttons'])
assert probe['actualVanillaBackupContinueButton']['key']=='selectWorld.backupJoinSkipButton' and probe['actualVanillaBackupContinueButton']['publicButtonOnPress'] and probe['actualVanillaBackupContinueButton']['onlyCachedCopyOpened']
assert not any(probe[key] for key in ['privateGameplayOrRewardCallbacksInvoked','manualEntityTicksInvoked','worldTimeOverridden','appearanceIdentityOrQuotaWrittenByObserver','observerDirectStorageLoadSaveOrPruneInvoked','directEntityCreationByObserver','fakePlayerUsed','authenticated','actualMultiplayerGameplayAccepted','curiosReloadAcceptanceClaimed','productChanged'])
assert probe['firstWorldTime']>=847 and probe['firstWorldTime']<1200 and probe['lastPresentTime']==1199 and probe['firstAbsentTime']==probe['expectedPruneTime']==1200 and probe['loadedRecordObservations']>0
assert all(native['terminal'][key] for key in ['clientProcessReaped','integratedWorldCompleteSave','inputHashesUnchanged','finalDefinedClassesCaptured']) and native['terminal']['exitCode']==0
for path,digest in native['inputHashes'].items():assert Path(path).is_file() and sha(path)==digest
attempt=Path(native['attempt']);manifest_path=attempt/'initial-native-world-copy-metadata.json';manifest=load(manifest_path);assert len(manifest['files'])==31 and all(row['independentCopy'] for row in manifest['files']) and manifest['resourcePayloadsLocalOnly']
source_audit_path=Path(manifest['sourceAudit']);source_audit=load(source_audit_path);source_disk=Path(source_audit['nativeSavedFilePath']);assert sha(source_disk)==source_audit['nativeSavedFileSha256']=='76c61e0315fe99efcc0fab4c557123eee05e81bb4ba98a57be35ebc8456a6175'
expected_path=attempt/'game/expected-original-appearance.json';expected=load(expected_path)
for row in probe['recordSnapshots']:
    for key in ['owner','stage','players','used','lastAppearance']:assert row[key]==expected[key]
    assert row['birds']==sorted(expected['birds']) and expected['used']==expected['players']==1 and expected['lastAppearance']==131
assert probe['recordSnapshots'][0]['worldTime']==probe['firstWorldTime'] and probe['recordSnapshots'][-1]['worldTime']==1199
for row in manifest['files']:
    original_file=Path(manifest['sourceWorld'])/row['relativePath'];assert original_file.is_file() and original_file.stat().st_size==row['bytes'] and sha(original_file)==row['sha256']
build_path=WORK/'build/boss-independent-material-cumulative-v1-20261114/candidate-build-receipt.json';built=load(build_path);candidate=Path(built['jar']);assert sha(candidate)==built['jarSha256']=='6c0acbcdda6fdecba9db9cc512ebdaceff36f84dd52ddf09b2879563716f54d9'
source=WORK/'tests/SparrowActualWorldReloadNativeV2.java';text=source.read_text(encoding='utf-8')
for forbidden in ['.addFreshEntity(','.create(level)','.putUUID(','.putBoolean(','.getDeclaredMethod(','.invoke(','.setGameTime(','.createFreshLevel(','.prune(','.markAppearance(','.entry(','.record(','.setHealth(','.performPrefixedCommand(','.SparrowAppearanceData.load(','.save(']:assert forbidden not in text,forbidden
assert '.openWorld(' in text and '.SparrowAppearanceData.get(' in text and '.find(' in text
agent=WORK/'tests/LoadedBossClassCaptureV8.java';directory=attempt/'captured-defined-classes';required=['net/farmingtales/ftboss/boss/SparrowSupport','net/farmingtales/ftboss/boss/SparrowMultiplayerSupport','net/farmingtales/ftboss/boss/SparrowAppearanceData','net/farmingtales/ftboss/boss/SparrowAppearanceData$Entry','net/farmingtales/ftboss/util/SparrowMultiplayerRules','net/farmingtales/ftboss/boss/NativeSparrowSavedDataAccess'];captured={}
with zipfile.ZipFile(candidate) as jar:
    for name in required:
        path=directory/(name.replace('/','_')+'.class');assert path.is_file() and path.read_bytes()==jar.read(name+'.class'),name;captured[name]=sha(path)
assert not OUT.exists();OUT.mkdir();classes=OUT/'audit-classes';classes.mkdir();prior=load(WORK/'build/boss-manor-damage-context-v1-20261114/compile-proof-command.json');cp=str(candidate)+';'+prior[prior.index('-cp')+1]
actual=directory/'net_farmingtales_ftboss_entity_EasterBossEntity.class';simulation=WORK/'build/boss-youzi-original-summon-v4-20261114/simulated-after.class';boss_source=WORK/'tests/YouziOriginalSummonLoadedAuditV2.java';storage_source=WORK/'tests/SparrowRetiredNativeStorageProofV1.java'
run('compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(boss_source),str(storage_source)])
loaded=run('actual-original-boss-graphs',[str(JAVA/'java.exe'),'-Xmx512m','-cp',str(classes)+';'+cp,'YouziOriginalSummonLoadedAuditV2',str(simulation),str(actual)])
disk=Path(manifest['targetWorld'])/'data/ftboss_sparrow_multiplayer.dat';disk_before=sha(disk);assert disk_before!=sha(source_disk)
storage=run('native-reloaded-retired-record-resaved',[str(JAVA/'java.exe'),'-Xmx256m','-cp',str(classes)+';'+cp,'SparrowRetiredNativeStorageProofV1',str(disk),expected['owner']]);assert sha(disk)==disk_before
inputs=[Path(__file__),report_path,build_path,candidate,source,agent,manifest_path,expected_path,source_audit_path,source_disk,disk,boss_source,storage_source,actual,simulation,*[directory/(name.replace('/','_')+'.class') for name in required]]
receipt={'nativeReport':str(report_path),'candidate':str(candidate),'candidateJarSha256':sha(candidate),'productChanged':False,'nativeChecks':probe['checks'],'actualNativeSavedWorldNewJvmReloadAccepted':True,'actualObservedVanillaBackupConfirmationHandledByPublicButton':True,'backupConfirmation':probe['actualNativeBackupConfirmation'],'publicBackupContinueButton':probe['actualVanillaBackupContinueButton'],'nativeWorldRestartAccepted':True,'nativeWorldRestartScope':'Single copied actual prior native save; original retired stage appearance loaded then natural periodic prune and native resave','originalSavedWorldInputsReadOnlyAndUnchanged':True,'sourceWorldFilesIndependentlyCopied':31,'sourceSavedFilePath':str(source_disk),'sourceSavedFileSha256':sha(source_disk),'loadedRecordSnapshots':probe['recordSnapshots'],'actualFirstWorldTime':probe['firstWorldTime'],'actualLastPresentTime':1199,'actualFirstAbsentTime':1200,'originalNativePeriodicPruneAccepted':True,'actualNewNativeSaveRetiredRecordAbsent':True,'nativeResavedFilePath':str(disk),'nativeResavedFileSha256':disk_before,'nativeStorageProof':storage,'actualRequiredHelperBytesExactlyCandidate':captured,'all342ActualBossGraphsEqualPriorProvenComposition':True,'actualBossProof':loaded,'observerHasNoPrivateCallbacksOrIdentityQuotaStorageWrites':True,'noManualTicksOrTimeOverrides':True,'curiosReloadAcceptanceClaimed':False,'authenticated':False,'actualMultiplayerGameplayAccepted':False,'fullBossCombatAcceptanceClaimed':False,'fullPersistenceAcrossAllBossesClaimed':False,'modelOrMusicUploaded':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False,'inputHashes':{str(path):sha(path) for path in inputs}}
save('receipt.json',receipt);print(json.dumps({'nativeChecks':probe['checks'],'actualNewJvmNativeWorldReload':True,'lastPresentTime':1199,'firstAbsentTime':1200,'actualResavePrunedRecordAbsent':True,'requiredActualHelperClasses':6,'actualBossProof':loaded,'nativeStorageProof':storage,'curiosReloadClaimed':False,'productChanged':False}),flush=True)
