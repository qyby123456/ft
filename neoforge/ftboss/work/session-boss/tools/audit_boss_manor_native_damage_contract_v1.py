import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/manor-native-damage-event-contract-audit-v1-20261114.json'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin/javap.exe')
def load(path):return json.loads(path.read_text(encoding='utf-8-sig'))
def sha(data):return hashlib.sha256(data).hexdigest()
def dump(classpath,name):
    result=subprocess.run([str(JAVA),'-p','-c','-classpath',classpath,name],capture_output=True,check=True,timeout=60)
    return result.stdout.decode('utf-8',errors='strict')
def calls(text,method):
    active=False;lines=[]
    for line in text.splitlines():
        if line.startswith('  ') and not line.startswith('    ') and '(' in line and line.endswith(';'):
            active=method+'(' in line
            if active:lines.append(line.strip())
        elif active and ('invoke' in line or 'new ' in line or ('Field' in line and 'EVENT_BUS' in line)):lines.append(line.strip())
    assert lines,method
    return lines
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert not owner['activeRuntimePids'] and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
assert not OUT.exists()
original=ROOT/'reference/forge-current.jar';receipt=WORK/'build/boss-sparrow-evasion-v2-20261114/candidate-build-receipt.json';candidate=Path(load(receipt)['jar'])
assert sha(original.read_bytes())=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
assert sha(candidate.read_bytes())==load(receipt)['jarSha256']=='877309d1ff2332ab6d407cd1b456d39987109d17e8f6a8e98184abe55703c25b'
settlement='net.farmingtales.ftboss.boss.ai.ManorFinalDamage'
bridge='net.farmingtales.ftboss.event.NativeManorDamageEvents'
phases='net.farmingtales.ftboss.event.NativeWeaponDamagePhases'
original_calls=calls(dump(str(original),settlement),'settle')
current_calls=calls(dump(str(candidate),settlement),'settle')
bridge_dump=dump(str(candidate),bridge);phase_dump=dump(str(candidate),phases)
bridge_calls={name:calls(bridge_dump,name) for name in ['onLivingHurt','damageEvent','post']}
phase_calls={name:calls(phase_dump,name) for name in ['beforeMitigation','beforeHealth']}
assert any('net/minecraftforge/event/entity/living/LivingDamageEvent.' in line for line in original_calls)
assert any('net/minecraftforge/eventbus/api/IEventBus.post' in line for line in original_calls)
assert any('NativeManorDamageEvents.damageEvent' in line for line in current_calls)
assert not any('LivingIncomingDamageEvent' in line or 'LivingDamageEvent$Pre' in line or 'LivingDamageEvent$Post' in line for rows in [current_calls,*bridge_calls.values(),*phase_calls.values()] for line in rows)
context=load(WORK/'build/boss-flight-ban-v1/compile-context.json')
sdk=[Path(name) for name in context['classpath'].split(';') if Path(name).is_file() and any(tag in Path(name).name for tag in ['neoforge-21.1.233-client.jar','neoforge-21.1.233-universal.jar'])]
assert len(sdk)==2
api=[]
for name in ['net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent','net.neoforged.neoforge.event.entity.living.LivingDamageEvent$Pre','net.neoforged.neoforge.event.entity.living.LivingDamageEvent$Post','net.neoforged.neoforge.common.damagesource.DamageContainer']:
    text=dump(';'.join(str(path) for path in sdk),name)
    public=[line.strip() for line in text.splitlines() if line.startswith('  public ') and line.rstrip().endswith(';')]
    entry=name.replace('.','/')+'.class';data=None
    for jar in sdk:
        with zipfile.ZipFile(jar) as bundle:
            if entry in bundle.namelist():data=bundle.read(entry);break
    assert data is not None and public
    api.append({'class':name,'classSha256':sha(data),'publicSignatures':public})
report_path=WORK/'reports/manor-native-damage-event-baseline-v1-552e8f09.json';runtime=load(report_path)
assert runtime['actualBossNativeDamageEventGapProven'] and runtime['probe']['sameListenerVanillaPositiveControlAccepted'] and runtime['probe']['failures']==0
assert runtime['terminal']['clientProcessReaped'] and runtime['terminal']['integratedWorldCompleteSave'] and runtime['terminal']['inputHashesUnchanged']
assert runtime['probe']['eventCounts']=={'boss.originalBeforeMitigation':1,'boss.originalBeforeHealth':1,'vanilla.incoming':1,'vanilla.pre':1,'vanilla.post':1}
for attack in runtime['probe']['attacks']:assert attack['afterHealth']<attack['beforeHealth']
classes={}
for jar,names in [(original,[settlement]),(candidate,[settlement,bridge,phases])]:
    with zipfile.ZipFile(jar) as bundle:
        for name in names:classes[('original:' if jar==original else 'current:')+name]=sha(bundle.read(name.replace('.','/')+'.class'))
actual=Path(runtime['attempt'])/'captured-defined-classes/net_farmingtales_ftboss_boss_ai_ManorFinalDamage.class'
with zipfile.ZipFile(candidate) as bundle:assert actual.read_bytes()==bundle.read(settlement.replace('.','/')+'.class')
report={'scope':'Static original/public API audit bound to actual unchanged Stage30 native positive/vanilla-control baseline; no product modification.','candidateSha256':load(receipt)['jarSha256'],'classHashes':classes,'originalSettleCalls':original_calls,'currentSettleCalls':current_calls,'currentBridgeCalls':bridge_calls,'currentCustomPhaseCalls':phase_calls,'actualDefinedSettlementMatchesCandidateBytes':True,'officialLocalNativeApi':api,'nativeBaselineReport':str(report_path),'nativeBaselineSha256':sha(report_path.read_bytes()),'nativeBaselineChecks':runtime['probe']['checks'],'nativeEventGapProven':True,'apiCompatibilityNotAccepted':True,'implementationRequirements':['Preserve original event mutability/cancellation, amount order and exact before-health settlement semantics.','Use a coherent per-invocation original-damage/reduction container with nested caller/exception restoration.','Honor incoming cancellation, amount changes, reduction modifiers and invulnerability semantics; do not merely emit synthetic notifications.','Emit native Pre at the real pre-health phase and Post only after actual settled health application, never on rejected/canceled paths.','Keep original custom phases and accepted Stage29/Sparrow behavior; verify listener ordering, nested/throwing observers and no duplicate callbacks.','Do not modify the paused weapon source tree or independent-event product.'],'modelOrMusicUploaded':False,'productChanged':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False}
OUT.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'nativeBaselineChecks':report['nativeBaselineChecks'],'sameListenerVanillaControlAccepted':True,'nativeEventGapProven':True,'actualDefinedSettlementMatchesCandidateBytes':True,'productChanged':False}),flush=True)
