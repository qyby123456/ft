import hashlib
import json
import re
import subprocess
import sys
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/manor-original-entry-loaded-audit-v1-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):(OUT/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run(name,command):
    result=subprocess.run(command,capture_output=True,timeout=120,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.txt')).write_bytes(result.stdout+result.stderr);save(name+'-terminal.json',{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.txt'));return result.stdout.decode('utf-8').strip()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids'] and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
paths=[Path(value).resolve() for value in sys.argv[1:]];assert len(paths)==2
runs=[load(path) for path in paths];all_inputs=[]
for expectation,result in zip(['baseline','fixed'],runs):
    assert result['expectation']==expectation and result['actualRawGuardBoundaryCasesAccepted']
    probe=result['probe'];assert probe['failures']==0 and probe['completedModes']==len(probe['requiredModes'])==len(probe['cases'])==15 and probe['originalNegativeProtectionControlsAccepted'] and probe['actualRawGuardBoundaryCasesAccepted']
    assert probe['actualClientAndIntegratedServer'] and not probe['authenticated'] and not probe['fakePlayerUsed'] and not probe['privateGameplayCallbacksInvoked'] and not probe['manualEntityTicksInvoked']
    terminal=result['terminal'];assert terminal['exitCode']==0 and terminal['clientProcessReaped'] and terminal['integratedWorldCompleteSave'] and terminal['inputHashesUnchanged'] and terminal['finalDefinedClassesCaptured']
    assert all(Path(path).is_file() and sha(path)==value for path,value in result['inputHashes'].items());all_inputs.extend(result['inputHashes'].keys())
    for case in probe['cases']:
        if case['ingress']=='actual-survival-ServerPlayer.attack':assert case['beforeGuards']['variant']==9 and not case['beforeGuards']['invulnerable'] and case['beforeGuards']['playerAttackStrength']>=0.999
        else:assert case['counts']=={} and not case['returned'] and case['beforeHealth']==case['afterHealth']
assert runs[0]['probe']['baselineOver500RejectionReproduced'] and runs[1]['probe']['fixedOver500PositiveAttacksAccepted']
shared=set(runs[0]['inputHashes'])&set(runs[1]['inputHashes']);assert all(runs[0]['inputHashes'][path]==runs[1]['inputHashes'][path] for path in shared)
mods=[{Path(path).name:value for path,value in item['inputHashes'].items() if Path(path).parent==Path(item['attempt'])/'game/mods'} for item in runs];assert len(mods[0])==len(mods[1])==28 and mods[0].keys()==mods[1].keys();changed_dependencies=[name for name in mods[0] if mods[0][name]!=mods[1][name]];assert len(changed_dependencies)==1
assert mods[0][changed_dependencies[0]]=='e63617ebd89d5e78055fef649db3392304a678abcafa4d74996a025d93f79c6c' and mods[1][changed_dependencies[0]]=='a49f1772a60d72a4240eb3f293b29fb38629a2112ad326120e81377eeeeca1ec'
observer=WORK/'tests/ManorRawGuardBoundaryNativeV1.java';agent=WORK/'tests/LoadedBossClassCaptureV5.java'
for item in [observer,agent]:assert runs[0]['inputHashes'][str(item)]==runs[1]['inputHashes'][str(item)]==sha(item)
comparison=[]
for before,after in zip(runs[0]['probe']['cases'],runs[1]['probe']['cases']):
    assert before['mode']==after['mode'] and before['requestedRaw']==after['requestedRaw']
    comparison.append({'mode':before['mode'],'requestedRaw':before['requestedRaw'],'baselineIncoming':before['counts'].get('incoming',0),'fixedIncoming':after['counts'].get('incoming',0),'baselineLoss':before['beforeHealth']-before['afterHealth'],'fixedLoss':after['beforeHealth']-after['afterHealth'],'ingress':before['ingress']})
base_dir=Path(runs[0]['attempt'])/'captured-defined-classes';fixed_dir=Path(runs[1]['attempt'])/'captured-defined-classes';selected=re.findall(r'"([^\"]+)"',agent.read_text(encoding='utf-8').split('Set<String> selected=Set.of(')[1].split(');')[0]);assert len(selected)==14
captured=[];unchanged_helpers=[]
for name in selected:
    before=base_dir/(name.replace('/','_')+'.class');after=fixed_dir/(name.replace('/','_')+'.class');assert before.is_file() and after.is_file();all_inputs.extend([str(before),str(after)]);row={'name':name,'baselineSha256':sha(before),'fixedSha256':sha(after)}
    if name not in ['net/minecraft/world/entity/LivingEntity','net/farmingtales/ftboss/entity/EasterBossEntity']:assert sha(before)==sha(after);unchanged_helpers.append(name)
    captured.append(row)
assert len(unchanged_helpers)==12
build=WORK/'build/boss-manor-original-entry-v2-20261114';built_path=build/'candidate-build-receipt.json';built=load(built_path);candidate=Path(built['jar']);assert sha(candidate)==built['jarSha256']=='a49f1772a60d72a4240eb3f293b29fb38629a2112ad326120e81377eeeeca1ec'
raw_helpers=['net/farmingtales/ftboss/boss/ai/'+name for name in ['ManorFinalDamage','NativeManorDamageBridge','NativeManorDamageBridge$Frame','NativeManorDamageScopeAccess','NativeManorDamageBridgeInstaller','NativeManorFinalDamageInstaller']]+['net/farmingtales/ftboss/event/NativeManorDamageEventFactory','net/farmingtales/ftboss/maque/support/MaqueSupportHooks','net/farmingtales/ftboss/youzi/YouziCombatEvents']
with zipfile.ZipFile(candidate) as jar:
    for name in raw_helpers:assert hashlib.sha256(jar.read(name+'.class')).hexdigest()==sha(fixed_dir/(name.replace('/','_')+'.class'))
assert not OUT.exists();OUT.mkdir();classes=OUT/'audit-classes';classes.mkdir();cp_input=load(WORK/'build/boss-manor-damage-context-v1-20261114/compile-proof-command.json');cp=str(candidate)+';'+cp_input[cp_input.index('-cp')+1];source=WORK/'tests/ManorOriginalEntryLoadedAuditV1.java';audit=WORK/'tests/ManorOriginalDamageRoutingMappedAuditV1.java'
run('compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(source),str(audit)])
actual_name='net_farmingtales_ftboss_entity_EasterBossEntity.class';proof=run('loaded-graphs',[str(JAVA/'java.exe'),'-Xmx256m','-cp',str(classes)+';'+cp,'ManorOriginalEntryLoadedAuditV1',str(build/'simulated-before.class'),str(build/'simulated-after.class'),str(base_dir/actual_name),str(fixed_dir/actual_name)]);assert 'fullActualHurtMatchesOriginal=true;' in proof
mapping=WORK/'build/boss-manor-routing-platform-audit-v1-20261114/mapping-proof.json';original=ROOT/'reference/forge-current.jar';assert sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942' and sha(mapping)==built['inputHashes'][str(mapping)];original_proof=run('original-six-methods',[str(JAVA/'java.exe'),'-Xmx256m','-cp',str(classes)+';'+cp,'ManorOriginalDamageRoutingMappedAuditV1',str(original),str(mapping),str(fixed_dir/actual_name)]);assert 'completeMappedOriginalMethods=6;' in original_proof
all_inputs.extend([str(path) for path in [Path(__file__),source,audit,mapping,original,candidate,built_path,*paths]])
save('boundary-comparison.json',comparison);save('actual-definition-comparison.json',captured)
save('receipt.json',{'proof':proof,'originalSixMethodProof':original_proof,'nativeBaselineReport':str(paths[0]),'nativeFixedReport':str(paths[1]),'candidateJarSha256':sha(candidate),'pairedCases':15,'actualPlayerAttackModesPerRun':8,'publicDamageApiNegativeControlModesPerRun':7,'actualDefinedClassesCapturedEach':14,'unchangedOtherCapturedBossClasses':12,'actualHelpersExactlyCandidate':len(raw_helpers),'unchangedDependencyJars':27,'sameFrozenObserverAndAgent':True,'livingEntityCrossJvmEqualityClaimed':False,'inputHashes':{path:sha(path) for path in sorted(set(all_inputs))},'originalRaw500EntryDivergenceCorrectedAndNativeVerified':True,'originalNegativeGuardsNativeVerified':True,'fullManagedFightSharingAccepted':False,'authenticated':False,'modelOrMusicUploaded':False,'installable':False,'deployed':False,'fullBossMigrationComplete':False});print(json.dumps({'loadedProof':proof,'originalProof':original_proof.splitlines()[-1],'pairedCases':15,'actualHelpersExactlyCandidate':len(raw_helpers)}),flush=True)
