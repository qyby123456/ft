import hashlib
import importlib.util
import json
import re
import subprocess
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
helper=ROOT/'work/session-boss/tools/prepare_boss_blood_tide_stage10.py'
spec=importlib.util.spec_from_file_location('bounded_patch_helper',helper)
module=importlib.util.module_from_spec(spec)
text=helper.read_text(encoding='utf-8')
exec(compile(text[:text.index('probe=(WORK/')],str(helper),'exec'),module.__dict__)
build=WORK/'build/native-field-retirement-diagnostic-613d4f6f'
build.mkdir()
classes=WORK/'build/native-repair-613d4f6f/complete-classes'
retire={}
ranged={}
for name in ['OwnerFieldExpectedAuthority','NativeExpectedFieldObservation']:
    command=['C:/Program Files/Zulu/zulu-21/bin/javap.exe','-classpath',str(classes),'-p','-c','-l','-s','net.farmingtales.ftboss.item.'+name]
    result=subprocess.run(command,capture_output=True,text=True,encoding='utf-8',errors='replace')
    (build/(name+'.javap')).write_text(result.stdout,encoding='utf-8')
    assert result.returncode==0,result.stderr[-500:]
    method=None
    signature=None
    previous=None
    for line in result.stdout.splitlines():
        if line.startswith('  ') and not line.startswith('    ') and '(' in line and line.endswith(';'):
            method=line.split('(',1)[0].split()[-1].rsplit('.',1)[-1]
            signature=None
            previous=None
        elif line.strip().startswith('descriptor:'):
            signature=line.split('descriptor:',1)[1].strip()
        instruction=re.match(r'\s*(\d+):\s+(\w+)\s*(.*)',line)
        if not instruction or method is None or signature is None:continue
        offset,opcode,operand=instruction.groups()
        key=method+signature
        if name=='OwnerFieldExpectedAuthority' and opcode=='putfield' and '$DomainBefore.retired:Z' in operand:
            assert previous=='iconst_1',(key,offset,previous)
            retire.setdefault(key,[]).append(int(offset))
        if name=='NativeExpectedFieldObservation' and method=='plannedRangedFieldMutation' and opcode=='ireturn':ranged.setdefault(key,[]).append(int(offset))
        previous=opcode
assert retire and ranged
(build/'breakpoints.json').write_text(json.dumps({'retirementWrites':retire,'plannedRangedReturns':ranged,'classHashes':{name:hashlib.sha256((classes/'net/farmingtales/ftboss/item'/(name+'.class')).read_bytes()).hexdigest() for name in ['OwnerFieldExpectedAuthority','NativeExpectedFieldObservation']}},indent=2)+'\n',encoding='utf-8')
def entries(mapping):
    return ','.join('Map.entry('+json.dumps(key)+',new long[]{'+','.join(map(str,values))+'})' for key,values in mapping.items())
source=(WORK/'tests/native-map-rejection-diagnostic-20261112/NativeMapRejectionDiagnostic.java').read_text(encoding='utf-8')
source=source[:source.index('    private static void observe(')]
source=source.replace('NativeMapRejectionDiagnostic','NativeFieldRetirementDiagnostic')
source=module.replace(source,'    private static final String TARGET="net.farmingtales.ftboss.item.NativeExpectedFieldObservation";','    private static final String TARGET="net.farmingtales.ftboss.item.NativeExpectedFieldObservation";\n    private static final String AUTHORITY="net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority";\n    private static final Map<String,long[]> RETIRE=Map.ofEntries('+entries(retire)+');\n    private static final Map<String,long[]> RANGED=Map.ofEntries('+entries(ranged)+');')
source+='''    private static ObjectReference current(ThreadReference thread) {
        var types=thread.virtualMachine().classesByName(TARGET);
        if (types.isEmpty()) return null;
        var type=types.getFirst();
        var key=type.getValue(type.fieldByName("CURRENT"));
        if (!(field(thread,"threadLocals") instanceof ObjectReference locals) || !(field(locals,"table") instanceof ArrayReference table)) return null;
        for (Value member:table.getValues()) {
            if (member instanceof ObjectReference entry && key.equals(field(entry,"referent"))) return field(entry,"value") instanceof ObjectReference scope?scope:null;
        }
        return null;
    }

    private static void observe(ThreadReference thread) throws Exception {
        var scope=current(thread);
        if (scope==null) return;
        var location=thread.frame(0).location();
        boolean retirement=location.declaringType().name().equals(AUTHORITY);
        var fact=field(scope,"active") instanceof ObjectReference value?value:null;
        if (!retirement && (fact==null || !(field(fact,"site") instanceof StringReference site) || !java.util.List.of("RANGED_LONG","RANGED_ROOT","MAP_PUT").contains(site.value()))) return;
        if (++observations>24) return;
        System.out.println((retirement?"FIELD_RETIRE_BEFORE_WRITE":"PLANNED_RANGED_RETURN")+" method="+location.method().name()+location.method().signature()+" bci="+location.codeIndex()+" line="+location.lineNumber());
        describe("scope",scope,"kind","retired","closed","sequence","step","active","lastBinding","acquisitionRoot","persistentRoot","rangedBuffer","player");
        describe("fact",fact,"site","receiver","key","argument","parent","attempted","returned","originalFailure","originalStep","bindingBefore","directMapChildren");
        if (fact!=null && field(fact,"bindingBefore") instanceof ObjectReference binding) describe("bindingBefore",binding,"bound","root","buffer");
        if (fact!=null && field(fact,"originalStep") instanceof ObjectReference step && field(step,"expected") instanceof ObjectReference operation) {
            describe("operation",operation,"kind","key","argument");
            if (field(operation,"before") instanceof ObjectReference before) describe("before",before,"rangedAcquired","rangedAttached","rangedPlayerIdentity","rangedBufferIdentity","player","rangedBuffer");
        }
        if (field(scope,"sequence") instanceof ObjectReference session && field(session,"lineage") instanceof ObjectReference lineage && field(lineage,"issuer") instanceof ObjectReference issuer && field(issuer,"fieldPreparation") instanceof ObjectReference prepared) {
            if (field(prepared,"player") instanceof ObjectReference domain) describe("playerDomain",domain,"retired","originated","loading","originalPersistent","expected");
        }
        var frames=thread.frames();
        for (int index=0;index<Math.min(frames.size(),20);index++) {
            var frame=frames.get(index).location();
            System.out.println("STACK "+index+" "+frame.declaringType().name()+"."+frame.method().name()+frame.method().signature()+" line="+frame.lineNumber()+" bci="+frame.codeIndex());
        }
        System.out.flush();
    }

    private static void install(VirtualMachine machine,ReferenceType type) {
        var mapping=type.name().equals(AUTHORITY)?RETIRE:RANGED;
        for (var method:type.methods()) {
            var offsets=mapping.get(method.name()+method.signature());
            if (offsets==null) continue;
            for (long offset:offsets) {
                var request=machine.eventRequestManager().createBreakpointRequest(method.locationOfCodeIndex(offset));
                request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
                request.enable();
                System.out.println("INSTALLED "+type.name()+"."+method.name()+" bci="+offset);
            }
        }
        System.out.flush();
    }

    public static void main(String[] arguments) throws Exception {
        AttachingConnector connector=Bootstrap.virtualMachineManager().attachingConnectors().stream().filter(value->value.name().equals("com.sun.jdi.SocketAttach")).findFirst().orElseThrow();
        Map<String,com.sun.jdi.connect.Connector.Argument> options=connector.defaultArguments();
        options.get("hostname").setValue("127.0.0.1");
        options.get("port").setValue(arguments[0]);
        VirtualMachine machine=null;
        for (int attempt=0;attempt<300 && machine==null;attempt++) {
            try { machine=connector.attach(options); } catch (java.io.IOException unavailable) { if (attempt==299) throw unavailable; Thread.sleep(100); }
        }
        try {
            for (String name:new String[]{TARGET,AUTHORITY}) {
                var prepare=machine.eventRequestManager().createClassPrepareRequest();
                prepare.addClassFilter(name);
                prepare.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
                prepare.enable();
            }
            for (var type:machine.allClasses()) if (type.name().equals(TARGET) || type.name().equals(AUTHORITY)) install(machine,type);
            machine.resume();
            while (true) {
                var events=machine.eventQueue().remove();
                for (var event:events) {
                    if (event instanceof ClassPrepareEvent prepared) install(machine,prepared.referenceType());
                    if (event instanceof BreakpointEvent entered) observe(entered.thread());
                    if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) { System.out.println("OWNED_JVM_TERMINAL observations="+observations); return; }
                }
                events.resume();
            }
        } catch (VMDisconnectedException complete) { System.out.println("OWNED_JVM_DISCONNECTED observations="+observations); }
    }
}
'''
module.create(WORK/'tests/native-field-retirement-diagnostic-613d4f6f/NativeFieldRetirementDiagnostic.java',source)
runner=(WORK/'tools/run_native_field_gate_613d4f6f.py').read_text(encoding='utf-8')
for old,new in [
    ('tests/native-field-gate-diagnostic-613d4f6f/NativeFieldGateDiagnostic.java','tests/native-field-retirement-diagnostic-613d4f6f/NativeFieldRetirementDiagnostic.java'),
    ('reports/native-repair-runtime-field-gate-613d4f6f.json','reports/native-repair-runtime-field-retirement-613d4f6f.json'),
    ('field-gate-diagnostic.log','field-retirement-diagnostic.log'),
]:runner=module.replace(runner,old,new)
runner=runner.replace('NativeFieldGateDiagnostic','NativeFieldRetirementDiagnostic')
module.create(WORK/'tools/run_native_field_retirement_613d4f6f.py',runner)
print(json.dumps({'prepared':True,'retirementWriteMethods':len(retire),'rangedReturnOffsets':ranged,'candidateUnchanged':True,'readOnlyDebugger':True,'methodInvocations':False,'authorityWrites':False}),flush=True)
