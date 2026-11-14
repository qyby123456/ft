import importlib.util
import json
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
helper=ROOT/'work/session-boss/tools/prepare_boss_blood_tide_stage10.py'
spec=importlib.util.spec_from_file_location('bounded_patch_helper',helper)
module=importlib.util.module_from_spec(spec)
text=helper.read_text(encoding='utf-8')
exec(compile(text[:text.index('probe=(WORK/')],str(helper),'exec'),module.__dict__)
source=(WORK/'tests/native-session-revoke-diagnostic-613d4f6f/NativeSessionRevokeDiagnostic.java').read_text(encoding='utf-8')
source=source.replace('NativeSessionRevokeDiagnostic','NativeFieldGateDiagnostic')
extra='''    private static int gateObservations;
    private static int compared;

    private static boolean identity(Value first,Value second) {
        return first==null?second==null:second!=null && first.equals(second);
    }

    private static String difference(ObjectReference first,ObjectReference second,String path) {
        if (identity(first,second)) return null;
        if (first==null || second==null) return path+"/null";
        if (++compared>16384) return path+"/diagnostic-budget";
        for (String name:new String[]{"kind","bits","string"}) {
            if (!value(field(first,name)).equals(value(field(second,name)))) return path+"/"+name;
        }
        for (String name:new String[]{"keys","elements","children"}) {
            Value left=field(first,name);
            Value right=field(second,name);
            if (identity(left,right)) continue;
            if (!(left instanceof ArrayReference leftArray) || !(right instanceof ArrayReference rightArray) || leftArray.length()!=rightArray.length()) return path+"/"+name+"/shape";
            for (int index=0;index<leftArray.length();index++) {
                Value leftValue=leftArray.getValue(index);
                Value rightValue=rightArray.getValue(index);
                if (name.equals("children")) {
                    String child=difference((ObjectReference)leftValue,(ObjectReference)rightValue,path+"/child["+index+"]");
                    if (child!=null) return child;
                } else if (!value(leftValue).equals(value(rightValue))) return path+"/"+name+"["+index+"]";
            }
        }
        return null;
    }

    private static void observeGate(ThreadReference thread) throws Exception {
        if (++gateObservations>4) return;
        var arguments=thread.frame(0).getArgumentValues();
        var session=(ObjectReference)arguments.get(0);
        var step=(ObjectReference)arguments.get(1);
        var lineage=(ObjectReference)field(session,"lineage");
        var issuer=(ObjectReference)field(lineage,"issuer");
        var prepared=(ObjectReference)field(issuer,"fieldPreparation");
        System.out.println("FALSE_FIELD_GATE index="+gateObservations+" bci="+thread.frame(0).location().codeIndex()+" line="+thread.frame(0).location().lineNumber());
        describe("prepared",prepared,"retired","bound","sealed","thread","lineage","session","player","carrier","issuer");
        describe("issuer",issuer,"closed","thread","fieldPreparation");
        describe("sessionAtGate",session,"traceRevoked","coverageInstalled","next","lineage");
        var operation=(ObjectReference)field(step,"expected");
        var before=(ObjectReference)field(operation,"before");
        describe("gateOperation",operation,"kind","key");
        for (String name:new String[]{"player","carrier"}) {
            var domain=(ObjectReference)field(prepared,name);
            describe(name+"Domain",domain,"retired","originated","loading","thread","originalPersistent","expected","entity","world","generation");
            if (domain==null) continue;
            compared=0;
            String mismatch=difference((ObjectReference)field(domain,"expected"),(ObjectReference)field(before,name),name);
            System.out.println(name+"ExpectedBeforeDifference="+mismatch+" compared="+compared);
            if (field(domain,"entity") instanceof ObjectReference weak && field(weak,"referent") instanceof ObjectReference entity) {
                describe(name+"Entity",entity,"persistentData","level","ftboss$ownerFieldDomain");
                System.out.println(name+"RawPersistentIdentity="+identity(field(entity,"persistentData"),field(domain,"originalPersistent")));
                System.out.println(name+"RawWorldIdentity="+identity(field(entity,"level"),field(domain,"world")));
            }
            System.out.println(name+"ThreadIdentity="+identity(field(domain,"thread"),thread));
        }
        System.out.println("preparedSessionIdentity="+identity(field(prepared,"session"),session));
        System.out.println("preparedLineageIdentity="+identity(field(prepared,"lineage"),lineage));
        System.out.flush();
    }

'''
source=module.replace(source,'    private static void observe(ThreadReference thread) throws Exception {',extra+'    private static void observe(ThreadReference thread) throws Exception {')
source=module.replace(source,'createBreakpointRequest(method.location())','createBreakpointRequest(method.name().equals("advance")?method.locationOfCodeIndex(155L):method.location())')
start=source.index('            var exits=machine.eventRequestManager().createMethodExitRequest();')
end=source.index('            for (String name:new String[]{TARGET,authority})',start)
source=source[:start]+source[end:]
source=module.replace(source,'                            System.out.println("FIELD_ADVANCE_ENTER");\n                            exits.enable();','                            observeGate(entered.thread());')
start=source.index('                    if (event instanceof MethodExitEvent exited')
end=source.index('                    if (event instanceof VMDeathEvent',start)
source=source[:start]+source[end:]
assert 'exits.' not in source and 'invokeMethod' not in source and 'setValue(' not in source[:source.index('    public static void main')]
module.create(WORK/'tests/native-field-gate-diagnostic-613d4f6f/NativeFieldGateDiagnostic.java',source)
runner=(WORK/'tools/run_native_session_revoke_613d4f6f.py').read_text(encoding='utf-8')
for old,new in [
    ('tests/native-session-revoke-diagnostic-613d4f6f/NativeSessionRevokeDiagnostic.java','tests/native-field-gate-diagnostic-613d4f6f/NativeFieldGateDiagnostic.java'),
    ('reports/native-repair-runtime-session-revoke-613d4f6f.json','reports/native-repair-runtime-field-gate-613d4f6f.json'),
    ('session-revoke-diagnostic.log','field-gate-diagnostic.log'),
]:runner=module.replace(runner,old,new)
runner=runner.replace('NativeSessionRevokeDiagnostic','NativeFieldGateDiagnostic')
module.create(WORK/'tools/run_native_field_gate_613d4f6f.py',runner)
print(json.dumps({'prepared':True,'candidateUnchanged':True,'exactPreviouslyObservedFalseReturnBci':155,'originalAssertionsRetained':True,'readOnlyJdi':True,'methodExitEventsRemoved':True,'authorityWrites':False}),flush=True)
