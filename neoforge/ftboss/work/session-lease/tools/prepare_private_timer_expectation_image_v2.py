import difflib
import hashlib
import importlib.util
import json
import shutil
import subprocess
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
PATCH='C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe'

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def stage(path,original,text):
    assert not path.exists()
    path.parent.mkdir(parents=True,exist_ok=True)
    shutil.copyfile(original,path)
    old=path.read_text(encoding='utf-8-sig')
    delta=list(difflib.unified_diff(old.splitlines(),text.splitlines(),lineterm=''))[2:]
    patch='*** Begin Patch\n*** Update File: '+path.relative_to(ROOT).as_posix()+'\n'+'\n'.join('@@' if line.startswith('@@') else line for line in delta)+'\n*** End Patch\n'
    subprocess.run([PATCH,'--codex-run-as-apply-patch',patch],check=True,stdout=subprocess.DEVNULL)
    assert path.read_text(encoding='utf-8-sig')==text

helper=ROOT/'work/session-boss/tools/prepare_boss_blood_tide_stage10.py'
spec=importlib.util.spec_from_file_location('bounded_patch_helper',helper)
module=importlib.util.module_from_spec(spec)
text=helper.read_text(encoding='utf-8');exec(compile(text[:text.index('probe=(WORK/')],str(helper),'exec'),module.__dict__)
parent=Path(load(WORK/'reports/native-repair-session-timer-expectation-candidate-73d460d2.json')['attempt'])
sources=[Path(value) for value in load(parent/'complete-arguments316.json') if value.endswith('.java')]
trace=next(path for path in sources if path.name=='FoodWeaponExpectedTrace.java')
native=next(path for path in sources if path.name=='NativeExpectedFieldObservation.java')
ledger=next(path for path in sources if path.name=='FoodWeaponSourceLedger.java')
proposal=WORK/'proposal-private-timer-expectation-v2/net/farmingtales/ftboss/item'
text=trace.read_text(encoding='utf-8-sig')
start=text.index('    static Tag materialize(Value value) {');end=text.index('\n    static Value emptyCompound()',start)
materializer=text[start:end].rstrip().replace('static Tag materialize(Value value)','private static Tag materializeTimerExpectation(Value value)').replace('FoodWeaponExpectedTrace.materialize(','materializeTimerExpectation(')
materializer=module.replace(materializer,'CompoundTag compound = new CompoundTag();','java.util.LinkedHashMap<String, Tag> fields = new java.util.LinkedHashMap<>();')
materializer=module.replace(materializer,'compound.put(value.keys[fieldIndex], materializeTimerExpectation(value.children[fieldIndex]));','fields.put(value.keys[fieldIndex], materializeTimerExpectation(value.children[fieldIndex]));')
materializer=module.replace(materializer,'yield compound;','yield NativeExpectedFieldObservation.timerExpectationCompoundForRead(fields);')
wrapper='''    static Tag timerExpectationForRead(Value value) {
        boolean caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1L).findFirst()
            .map(frame -> frame.getDeclaringClass() == FoodWeaponSourceLedger.class
                && frame.getMethodName().equals("nativeTimerExpectationReturned")
                && frame.getDescriptor().equals("(Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Session;Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Step;)Z")).orElse(false));
        if (!caller) throw new IllegalStateException("Private timer expected image requires its genuine ledger caller");
        return materializeTimerExpectation(value);
    }

'''
position=text.rfind('\n}')
stage(proposal/'FoodWeaponExpectedTrace.java',trace,text[:position]+'\n\n'+wrapper+materializer+'\n'+text[position:])
factory='''    public static CompoundTag timerExpectationCompoundForRead(Map<String, Tag> values) {
        requireReadCaller(FoodWeaponExpectedTrace.class, "materializeTimerExpectation",
            "(Lnet/farmingtales/ftboss/item/FoodWeaponExpectedTrace$Value;)Lnet/minecraft/nbt/Tag;");
        if (values == null || values.getClass() != java.util.LinkedHashMap.class) {
            throw new IllegalArgumentException("Private timer expected compound requires its unpublished builder");
        }
        CompoundTag output = newReadCompound();
        fields(output).putAll(values);
        return output;
    }
'''
text=native.read_text(encoding='utf-8-sig');position=text.rfind('\n}')
stage(proposal/'NativeExpectedFieldObservation.java',native,text[:position]+'\n\n'+factory+text[position:])
text=ledger.read_text(encoding='utf-8-sig')
for name in ('before','after'):text=module.replace(text,'FoodWeaponExpectedTrace.materialize(operation.'+name+'.weapon)','FoodWeaponExpectedTrace.timerExpectationForRead(operation.'+name+'.weapon)')
stage(proposal/'FoodWeaponSourceLedger.java',ledger,text)
builder=(WORK/'tools/build_private_timer_expectation_image.py').read_text(encoding='utf-8').replace('proposal-private-timer-expectation','proposal-private-timer-expectation-v2').replace('reports/native-repair-private-timer-image-candidate.json','reports/native-repair-private-timer-image-v2-candidate.json')
module.create(WORK/'tools/build_private_timer_expectation_image_v2.py',builder)
report={'firstBuild':'build/native-repair-d3588394','firstBuildCompileFailedBeforeRuntime':True,'reason':'Immutable Value arrays remain private to the trace nest; no visibility widening is allowed','correction':'Keep independent materialization within its existing trace nest and use an exact-caller restricted private native compound factory','changedPrototypeSources':3,'existingGuardsUnchanged':True,'privateValueEncapsulationPreserved':True,'sourceHashes':{str(path):hashlib.sha256(path.read_bytes()).hexdigest() for path in proposal.glob('*.java')},'fullSignedSessionAccepted':False,'installable':False,'deployed':False}
output=WORK/'reports/private-timer-expectation-image-v2-preparation-20261112.json';assert not output.exists();output.write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'preparedV2':True,'privateValueFieldsPreserved':True,'oldCompileFailureFrozen':True,'guardRelaxation':False}),flush=True)
