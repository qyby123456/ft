import hashlib
import json
import subprocess
import sys
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/sparrow-standalone-attacker-loaded-audit-v2-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):(OUT/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run(name,command):
    result=subprocess.run(command,capture_output=True,timeout=120,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.txt')).write_bytes(result.stdout+result.stderr);save(name+'-terminal.json',{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.txt'));return result.stdout.decode('utf-8').strip()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids'] and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
report_path=Path(sys.argv[1]).resolve();native=load(report_path);probe=native['probe'];assert native['standaloneNativeAttackerAndPeriodicPruneAccepted'] and probe['failures']==0 and probe['completedModes']==len(probe['cases'])==7 and not native['productChanged']
assert probe['standaloneNativeAttackerEventAccepted'] and probe['nativePeriodicAppearancePruneAccepted'] and probe['naturalRetentionBoundaryAccepted']
assert all(not probe[key] for key in ['directEntityCreationByObserver','directBossOrHelperHealthWritesByObserver','attackerFightOwnerOrAppearanceFieldsWrittenByObserver','privateGameplayCallbacksInvoked','manualEntityTicksInvoked','worldTimeOverridden','fakePlayerUsed','authenticated','actualMultiplayerGameplayAccepted','nativeWorldRestartAccepted'])
assert probe['publicVanillaConsoleSummonFixture'] and probe['playerFixtureProtection']['positiveTargetPreModifier']==16 and probe['playerFixtureProtection']['nativePreDamageCap']==0.01
modes={row['mode']:row for row in probe['cases']};first=modes['standalone-positive-first'];repeat=modes['standalone-positive-repeat'];assert len(first['after']['attackers'])==len(repeat['after']['attackers'])==1
assert first['after']['attackers'][0]['id']==repeat['after']['attackers'][0]['id'] and repeat['after']['attackers'][0]['at']>first['after']['attackers'][0]['at']
for row in probe['cases']:
    assert row['actualPlayerAttackEvents']==1
    if 'positive' in row['mode']:assert row['nativePreEvents']==row['nativePostEvents']==1 and row['nativePreAmount']==16 and row['nativePostAmount']>0 and row['after']['health']<row['before']['health']
    else:assert row['nativePostEvents']==0 and row['before']['health']==row['after']['health'] and row['before']['attackers']==row['after']['attackers']
    if row['mode'].startswith('managed'):assert row['after']['managed'] and not row['after']['attackers']
before=probe['retentionBeforeExpiry'];after=probe['retentionAfterExpiry'];assert before['age']==1200 and len(before['state']['attackers'])==1 and after['age']==1201 and not after['state']['attackers']
periodic=probe['periodicPrune'];retired=probe['retiredAppearanceBeforePeriodicPrune'];assert periodic['observedAt']==periodic['expectedBoundary']+1 and periodic['expectedBoundary']%1200==0 and periodic['recordAbsent'] and not periodic['pruneOrTickCallbackInvokedByObserver'] and periodic['actualRetiredFight']==retired['fight']
assert all(native['terminal'][key] for key in ['clientProcessReaped','integratedWorldCompleteSave','inputHashesUnchanged','finalDefinedClassesCaptured']) and native['terminal']['exitCode']==0
for path,digest in native['inputHashes'].items():assert Path(path).is_file() and sha(path)==digest
built_path=WORK/'build/boss-sparrow-original-multiplayer-v1-20261114/candidate-build-receipt.json';built=load(built_path);candidate=Path(built['jar']);assert sha(candidate)==built['jarSha256']=='9d713622cc8c8aa3d2e5b5fa340053a7eb45f8128247346c3152a14c975b886e'
source=WORK/'tests/SparrowStandaloneAttackerNativeV2.java';text=source.read_text(encoding='utf-8')
for forbidden in ['.setVariant(','.addFreshEntity(','.create(level)','.putUUID(','.putLong(','.getDeclaredMethod(','.invoke(','.setGameTime(','.onDamage(','.onTick(','.prune(']:assert forbidden not in text,forbidden
directory=Path(native['attempt'])/'captured-defined-classes';required=['net/farmingtales/ftboss/boss/SparrowSupport','net/farmingtales/ftboss/boss/SparrowMultiplayerSupport','net/farmingtales/ftboss/boss/SparrowAppearanceData','net/farmingtales/ftboss/boss/SparrowAppearanceData$Entry','net/farmingtales/ftboss/util/SparrowMultiplayerRules','net/farmingtales/ftboss/boss/NativeSparrowSavedDataAccess'];captured={}
with zipfile.ZipFile(candidate) as jar:
    for name in required:
        path=directory/(name.replace('/','_')+'.class');assert path.is_file() and path.read_bytes()==jar.read(name+'.class'),name;captured[name]=sha(path)
assert not OUT.exists();OUT.mkdir();classes=OUT/'audit-classes';classes.mkdir();prior=load(WORK/'build/boss-manor-damage-context-v1-20261114/compile-proof-command.json');cp=str(candidate)+';'+prior[prior.index('-cp')+1]
actual=directory/'net_farmingtales_ftboss_entity_EasterBossEntity.class';simulation=WORK/'build/boss-youzi-original-summon-v4-20261114/simulated-after.class';actual_source=WORK/'tests/YouziOriginalSummonLoadedAuditV2.java';storage_source=WORK/'tests/SparrowRetiredNativeStorageProofV1.java'
run('compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(actual_source),str(storage_source)])
loaded=run('actual-original-boss-graphs',[str(JAVA/'java.exe'),'-Xmx512m','-cp',str(classes)+';'+cp,'YouziOriginalSummonLoadedAuditV2',str(simulation),str(actual)])
disk=Path(native['attempt'])/'game/saves/sparrow-standalone-attacker-native-v2/data/ftboss_sparrow_multiplayer.dat';disk_before=sha(disk)
storage=run('native-retired-record-storage',[str(JAVA/'java.exe'),'-Xmx256m','-cp',str(classes)+';'+cp,'SparrowRetiredNativeStorageProofV1',str(disk),retired['fight']]);assert sha(disk)==disk_before
inputs=[Path(__file__),report_path,built_path,candidate,source,actual_source,storage_source,actual,simulation,disk,*[directory/(name.replace('/','_')+'.class') for name in required]]
receipt={'nativeReport':str(report_path),'candidate':str(candidate),'candidateJarSha256':sha(candidate),'productChanged':False,'nativeCases':7,'nativeChecks':probe['checks'],'actualOriginalAttackerEventAccepted':True,'actualStandaloneDedupAndTimestampRefreshAccepted':True,'actualIncomingCancelAndNativeZeroGuardsAccepted':True,'actualManagedPathExclusionAccepted':True,'nativePreExpiryObservedAge':before['age'],'nativePostExpiryObservedAge':after['age'],'exact1200InclusiveRuntimeObservationClaimed':before['age']==1200,'fullOriginalInclusive1200SourceGraphPreviouslyProven':True,'nativePeriodicServerTickPostPruneAccepted':True,'actualPeriodicPrune':periodic,'nativeRetiredRecordRemovalPersisted':True,'actualRequiredHelperBytesExactlyCandidate':captured,'all342ActualBossGraphsEqualPriorProvenComposition':True,'actualBossProof':loaded,'nativeRetiredStorageProof':storage,'nativeSavedFileReadOnlyDuringAudit':True,'nativeSavedFilePath':str(disk),'nativeSavedFileSha256':disk_before,'observerHasNoPrivateCallbacksOrStateIdentityWrites':True,'publicVanillaConsoleSummonFixture':True,'publicOriginalParticipantsReadPrunesExpiredRecords':True,'fixture':probe['playerFixtureProtection'],'completeCombatOrSurvivabilityClaimed':False,'actualMultiplayerGameplayAccepted':False,'authenticated':False,'nativeWorldRestartAccepted':False,'modelOrMusicUploaded':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False,'inputHashes':{str(p):sha(p) for p in inputs}}
save('receipt.json',receipt);print(json.dumps({'nativeCases':7,'nativeChecks':probe['checks'],'requiredActualClasses':6,'actualBossProof':loaded,'nativeStorageProof':storage,'expiryAges':[before['age'],after['age']],'nativePeriodicPruneAccepted':True}),flush=True)
