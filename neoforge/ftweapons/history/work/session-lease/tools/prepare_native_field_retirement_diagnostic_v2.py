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
source=(WORK/'tests/native-field-retirement-diagnostic-613d4f6f/NativeFieldRetirementDiagnostic.java').read_text(encoding='utf-8')
source=source.replace('NativeFieldRetirementDiagnostic','NativeFieldRetirementDiagnosticV2')
source=module.replace(source,'    private static int observations;','''    private static int observations;
    private static final java.util.List<com.sun.jdi.request.BreakpointRequest> retireRequests=new java.util.ArrayList<>();
    private static final java.util.List<com.sun.jdi.request.BreakpointRequest> returnRequests=new java.util.ArrayList<>();
    private static final java.util.List<com.sun.jdi.request.BreakpointRequest> entryRequests=new java.util.ArrayList<>();
    private static long armedThread=-1L;
    private static int plannedDepth=-1;
''')
start=source.index('    private static void install(')
end=source.index('    public static void main(',start)
source=source[:start]+'''    private static com.sun.jdi.request.BreakpointRequest breakpoint(VirtualMachine machine,Location location,String mode,boolean enabled) {
        var request=machine.eventRequestManager().createBreakpointRequest(location);
        request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
        request.putProperty("mode",mode);
        if (enabled) request.enable();
        return request;
    }

    private static void install(VirtualMachine machine,ReferenceType type) {
        boolean authority=type.name().equals(AUTHORITY);
        var mapping=authority?RETIRE:RANGED;
        for (var method:type.methods()) {
            var offsets=mapping.get(method.name()+method.signature());
            if (offsets==null) continue;
            for (long offset:offsets) {
                var request=breakpoint(machine,method.locationOfCodeIndex(offset),authority?"retire":"plan-return",authority && armedThread!=-1L);
                (authority?retireRequests:returnRequests).add(request);
            }
        }
        if (!authority) {
            for (var method:type.methodsByName("rangedSave")) breakpoint(machine,method.location(),"arm",true);
            for (var method:type.methodsByName("plannedRangedFieldMutation")) entryRequests.add(breakpoint(machine,method.location(),"plan-enter",armedThread!=-1L));
        }
        System.out.println("INSTALLED_DISABLED_UNTIL_ORIGINAL_RANGED_SAVE "+type.name());
        System.out.flush();
    }

    private static void dispatch(BreakpointEvent event) throws Exception {
        String mode=(String)event.request().getProperty("mode");
        var thread=event.thread();
        if (mode.equals("arm")) {
            if (armedThread!=-1L) return;
            var arguments=thread.frame(0).getArgumentValues();
            if (arguments.size()<6 || !(arguments.get(4) instanceof ObjectReference writer)
                || !(field(writer,"fieldPreparation") instanceof ObjectReference prepared)
                || !(field(prepared,"session") instanceof ObjectReference session)
                || !(field(writer,"closed") instanceof BooleanValue closed) || closed.value()
                || !(field(session,"coverageInstalled") instanceof BooleanValue covered) || !covered.value()
                || !(field(session,"traceRevoked") instanceof BooleanValue revoked) || revoked.value()) return;
            armedThread=thread.uniqueID();
            for (var request:retireRequests) request.enable();
            for (var request:entryRequests) request.enable();
            System.out.println("ARMED_AFTER_ACTUAL_BOUND_ORIGINAL_RANGED_SAVE thread="+armedThread);
            System.out.flush();
            return;
        }
        if (thread.uniqueID()!=armedThread) return;
        if (mode.equals("retire")) {
            observe(thread);
            return;
        }
        if (mode.equals("plan-enter")) {
            if (plannedDepth!=-1) return;
            var scope=current(thread);
            var fact=scope!=null && field(scope,"active") instanceof ObjectReference value?value:null;
            if (fact==null || !(field(fact,"site") instanceof StringReference site) || !java.util.List.of("RANGED_LONG","RANGED_ROOT").contains(site.value()) || !(field(fact,"originalStep") instanceof ObjectReference)) return;
            plannedDepth=thread.frameCount();
            for (var request:returnRequests) request.enable();
            return;
        }
        if (mode.equals("plan-return") && plannedDepth==thread.frameCount()) {
            observe(thread);
            for (var request:returnRequests) request.disable();
            plannedDepth=-1;
        }
    }

'''+source[end:]
source=module.replace(source,'                    if (event instanceof BreakpointEvent entered) observe(entered.thread());','                    if (event instanceof BreakpointEvent entered) dispatch(entered);')
source=module.replace(source,'        var location=thread.frame(0).location();','''        var location=thread.frame(0).location();
        System.out.println("METHOD_ARGUMENTS "+thread.frame(0).getArgumentValues().stream().map(NativeFieldRetirementDiagnosticV2::value).toList());''')
source=module.replace(source,'"site","receiver","key","argument","parent","attempted"','"site","thread","receiver","key","argument","parent","attempted"')
source=module.replace(source,'            var frame=frames.get(index).location();','''            if (index<4) System.out.println("FRAME_ARGUMENTS "+index+" "+frames.get(index).getArgumentValues().stream().map(NativeFieldRetirementDiagnosticV2::value).toList());
            var frame=frames.get(index).location();''')
module.create(WORK/'tests/native-field-retirement-diagnostic-v2-613d4f6f/NativeFieldRetirementDiagnosticV2.java',source)
runner=(WORK/'tools/run_native_field_retirement_613d4f6f.py').read_text(encoding='utf-8')
for old,new in [
    ('tests/native-field-retirement-diagnostic-613d4f6f/NativeFieldRetirementDiagnostic.java','tests/native-field-retirement-diagnostic-v2-613d4f6f/NativeFieldRetirementDiagnosticV2.java'),
    ('reports/native-repair-runtime-field-retirement-613d4f6f.json','reports/native-repair-runtime-field-retirement-v2-613d4f6f.json'),
    ('field-retirement-diagnostic.log','field-retirement-v2-diagnostic.log'),
]:runner=module.replace(runner,old,new)
runner=runner.replace("'NativeFieldRetirementDiagnostic'","'NativeFieldRetirementDiagnosticV2'")
module.create(WORK/'tools/run_native_field_retirement_v2_613d4f6f.py',runner)
print(json.dumps({'prepared':True,'startupNbtReturnBreakpointsDisabled':True,'armRequiresActualBoundOriginalRangedSave':True,'returnBreakpointsLimitedToActualOriginalRangedMutation':True,'candidateUnchanged':True,'originalAssertionsRetained':True,'readOnlyJdi':True}),flush=True)
