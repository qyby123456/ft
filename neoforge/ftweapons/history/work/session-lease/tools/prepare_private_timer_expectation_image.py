import hashlib
import importlib.util
import json
import re
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def digest(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()

runtime=WORK/'build/repair-runtime-061b8ac0'
terminal=load(runtime/'terminal308.json')
debugger=load(runtime/'diagnostic-terminal.json')
assert terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave']
assert debugger['exitCode']==0 and debugger['readOnly'] and not debugger['methodInvocations'] and not debugger['authorityWrites']
evidence=(runtime/'native-map-rejection.log').read_text(encoding='utf-8')
assert 'scope.kind.name=TIMER' in evidence and 'fact0.key=EvolutionSkill' in evidence
assert 'FoodWeaponExpectedTrace.materialize' in evidence and 'FoodWeaponSourceLedger.nativeTimerExpectationReturned' in evidence
assert 'fact1.site=TIMER' in evidence and 'scope.retired=false' in evidence
helper=ROOT/'work/session-boss/tools/prepare_boss_blood_tide_stage10.py'
spec=importlib.util.spec_from_file_location('bounded_patch_helper',helper)
module=importlib.util.module_from_spec(spec)
text=helper.read_text(encoding='utf-8');exec(compile(text[:text.index('probe=(WORK/')],str(helper),'exec'),module.__dict__)
candidate=WORK/'reports/native-repair-session-timer-expectation-candidate-73d460d2.json'
parent=Path(load(candidate)['attempt'])
arguments=load(parent/'complete-arguments316.json')
sources=[Path(value) for value in arguments if value.endswith('.java')]
expected=next(path for path in sources if path.name=='FoodWeaponExpectedTrace.java')
observation=next(path for path in sources if path.name=='NativeExpectedFieldObservation.java')
ledger=next(path for path in sources if path.name=='FoodWeaponSourceLedger.java')
text=expected.read_text(encoding='utf-8-sig')
start=text.index('    static Tag materialize(Value value) {')
end=text.index('\n    static Value emptyCompound()',start)
materializer=text[start:end].rstrip()
materializer=module.replace(materializer,'static Tag materialize(Value value)','private static Tag materializeTimerExpectation(Value value)')
materializer=materializer.replace('FoodWeaponExpectedTrace.materialize(','materializeTimerExpectation(')
for name in ('EndTag','ByteTag','ShortTag','IntTag','FloatTag','DoubleTag','StringTag','ByteArrayTag','IntArrayTag','LongArrayTag','ListTag'):
    materializer=re.sub(r'\b'+name+r'\b','net.minecraft.nbt.'+name,materializer)
materializer=module.replace(materializer,'CompoundTag compound = new CompoundTag();','CompoundTag compound = newReadCompound();\n                Map<String, Tag> privateFields = fields(compound);')
materializer=module.replace(materializer,'compound.put(value.keys[fieldIndex], materializeTimerExpectation(value.children[fieldIndex]));','privateFields.put(value.keys[fieldIndex], materializeTimerExpectation(value.children[fieldIndex]));')
wrapper='''    public static Tag timerExpectationForRead(Value value) {
        requireReadCaller(FoodWeaponSourceLedger.class, "nativeTimerExpectationReturned",
            "(Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Session;Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Step;)Z");
        return materializeTimerExpectation(value);
    }

'''
observation_text=observation.read_text(encoding='utf-8-sig')
position=observation_text.rfind('\n}')
assert position>0 and observation_text[position:].strip()=='}'
updated=observation_text[:position]+'\n\n'+wrapper+materializer+'\n'+observation_text[position:]
proposal=WORK/'proposal-private-timer-expectation/net/farmingtales/ftboss/item'
module.create(proposal/'NativeExpectedFieldObservation.java',updated)
ledger_text=ledger.read_text(encoding='utf-8-sig')
for name in ('before','after'):
    ledger_text=module.replace(ledger_text,'FoodWeaponExpectedTrace.materialize(operation.'+name+'.weapon)','NativeExpectedFieldObservation.timerExpectationForRead(operation.'+name+'.weapon)')
module.create(proposal/'FoodWeaponSourceLedger.java',ledger_text)
builder=(WORK/'tools/build_native_session_timer_expectation.py').read_text(encoding='utf-8')
builder=module.replace(builder,'reports/native-repair-ranged-use-candidate-dd1ad841.json','reports/native-repair-session-timer-expectation-candidate-73d460d2.json')
builder=builder.replace('proposal-session-timer-expectation','proposal-private-timer-expectation').replace('reports/native-repair-session-timer-expectation-candidate.json','reports/native-repair-private-timer-image-candidate.json')
module.create(WORK/'tools/build_private_timer_expectation_image.py',builder)
report={'scope':'Actual original TIMER rejection caused by independently planned expected-image materialization entering production CompoundTag.put observation','diagnosticRuntime':str(runtime),'nativeJavaExitCode':0,'normalSave':True,'debuggerExitCode':0,'debuggerReadOnly':True,'originalStepKind':'TIMER','rejectedDetachedExpectedField':'EvolutionSkill','rejectedValue':'blood_feast','physicalWriteCaller':'FoodWeaponExpectedTrace.materialize -> FoodWeaponSourceLedger.nativeTimerExpectationReturned -> original Session.close','leaseLossObserved':False,'guardAndAssertionsUnchanged':True,'fixDesign':'Exact-caller restricted private read image factory; recursively materialize immutable expected values using existing newReadCompound/raw-private-fields pattern, never an observed real stack, domain reset or global write exemption','parentCandidate':str(candidate),'parentCandidateSha256':digest(candidate),'frozenSourceHashes':{str(path):digest(path) for path in (expected,observation,ledger)},'diagnosticEvidenceHashes':{str(runtime/name):digest(runtime/name) for name in ('native-map-rejection.log','terminal308.json','diagnostic-terminal.json')},'newSources':{str(path):digest(path) for path in proposal.glob('*.java')},'wholeNineteenAccepted':False,'installable':False,'deployed':False}
output=WORK/'reports/private-timer-expectation-image-preparation-20261112.json'
assert not output.exists()
output.write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'actualFailureDiagnosed':True,'preparedSources':2,'globalGuardChanged':False,'authorityWritten':False,'runtimeAcceptance':False}),flush=True)
