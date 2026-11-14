import hashlib
import json
import re
import subprocess
import sys
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/youzi-original-summon-loaded-audit-v2-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):(OUT/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run(name,command):
    result=subprocess.run(command,capture_output=True,timeout=120,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.txt')).write_bytes(result.stdout+result.stderr);save(name+'-terminal.json',{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.txt'));return result.stdout.decode('utf-8').strip()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids'] and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
path=Path(sys.argv[1]).resolve();result=load(path);assert result['actualYouziOriginalSummonScopedAccepted'];probe=result['probe'];assert probe['failures']==0 and probe['completedModes']==len(probe['requiredModes'])==len(probe['cases'])==9 and probe['naturalOriginalMiniSummonsAccepted'] and probe['originalRegisteredEggIngressAndDuplicateGuardAccepted'] and probe['originalGuardedSurrenderCleanupAccepted'] and probe['pureOriginalScalingBoundariesAccepted']
assert not probe['authenticated'] and not probe['fakePlayerUsed'] and not probe['privateGameplayCallbacksInvoked'] and not probe['manualEntityTicksInvoked'] and not probe['directBossOrHelperHealthWritesByObserver'] and not probe['directEntityCreationByObserver'] and not probe['fightOrOwnerFieldsWrittenByObserver']
terminal=result['terminal'];assert terminal['exitCode']==0 and terminal['clientProcessReaped'] and terminal['integratedWorldCompleteSave'] and terminal['inputHashesUnchanged'] and terminal['finalDefinedClassesCaptured']
assert all(Path(name).is_file() and sha(name)==value for name,value in result['inputHashes'].items())
built_path=WORK/'build/boss-youzi-original-summon-v4-20261114/candidate-build-receipt.json';built=load(built_path);candidate=Path(built['jar']);assert sha(candidate)=='d691bdb1a2b5f540d9bcd8149e6d04baa5c2e1df1a6c01578b4b32b75cfdb69c'
agent=WORK/'tests/LoadedBossClassCaptureV6.java';source=WORK/'tests/YouziOriginalSummonLoadedAuditV1.java';audit=WORK/'tests/ManorOriginalDamageRoutingMappedAuditV1.java';mapping=WORK/'build/boss-manor-routing-platform-audit-v1-20261114/mapping-proof.json';original=ROOT/'reference/forge-current.jar';directory=Path(result['attempt'])/'captured-defined-classes';selected=re.findall(r'"([^\"]+)"',agent.read_text(encoding='utf-8').split('Set<String> selected=Set.of(')[1].split(');')[0]);assert len(selected)==18
captured=[];missing=[];exact=[]
with zipfile.ZipFile(candidate) as jar:
    for name in selected:
        actual=directory/(name.replace('/','_')+'.class')
        if not actual.exists():missing.append(name);continue
        captured.append({'class':name,'sha256':sha(actual)})
        if name in ['net/farmingtales/ftboss/youzi/YouziSummonScaling','net/farmingtales/ftboss/util/YouziSummonRules','net/farmingtales/ftboss/youzi/motion/YouziBodyNetwork','net/farmingtales/ftboss/boss/ai/NativeAnimationEmitterInstaller'] or name.startswith('net/farmingtales/ftboss/boss/ai/NativeManor') or name=='net/farmingtales/ftboss/event/NativeManorDamageEventFactory':
            assert actual.read_bytes()==jar.read(name+'.class'),name;exact.append(name)
assert all(name in exact for name in ['net/farmingtales/ftboss/youzi/YouziSummonScaling','net/farmingtales/ftboss/util/YouziSummonRules','net/farmingtales/ftboss/youzi/motion/YouziBodyNetwork','net/farmingtales/ftboss/boss/ai/NativeAnimationEmitterInstaller']);assert set(missing).issubset({'net/farmingtales/ftboss/maque/support/MaqueSupportHooks'})
assert not OUT.exists();OUT.mkdir();classes=OUT/'audit-classes';classes.mkdir();prior=load(WORK/'build/boss-manor-damage-context-v1-20261114/compile-proof-command.json');cp=str(candidate)+';'+prior[prior.index('-cp')+1]
run('compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(source),str(audit)])
actual=directory/'net_farmingtales_ftboss_entity_EasterBossEntity.class';simulation=Path(built['jar']).parent/'simulated-after.class'
proof=run('loaded',[str(JAVA/'java.exe'),'-Xmx512m','-cp',str(classes)+';'+cp,'YouziOriginalSummonLoadedAuditV1',str(simulation),str(actual)])
original_proof=run('original-six-methods',[str(JAVA/'java.exe'),'-Xmx256m','-cp',str(classes)+';'+cp,'ManorOriginalDamageRoutingMappedAuditV1',str(original),str(mapping),str(actual)]);assert 'completeMappedOriginalMethods=6;' in original_proof
inputs=[Path(__file__),path,built_path,candidate,agent,source,audit,mapping,original,simulation,*[directory/(value['class'].replace('/','_')+'.class') for value in captured]]
save('receipt.json',{'proof':proof,'originalSixMethodProof':original_proof,'nativeReport':str(path),'candidateJarSha256':sha(candidate),'actualDefinedClasses':captured,'requestedCaptureNames':selected,'notNaturallyLoaded':missing,'noAdditionalClassForcedLoaded':True,'actualHelpersExactlyCandidate':exact,'all342ActualMethodGraphsEqualProvenComposition':True,'inputHashes':{str(name):sha(name) for name in inputs},'authenticated':False,'actualMultiplayerGameplayAccepted':False,'fullManagedSparrowSharingMatrixAccepted':False,'modelOrMusicUploaded':False,'installable':False,'deployed':False,'fullBossMigrationComplete':False});print(json.dumps({'proof':proof,'originalSixMethods':original_proof.splitlines()[-1],'captured':len(captured),'exactHelpers':len(exact)}),flush=True)
