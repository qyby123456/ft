import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.event.*;
import com.sun.jdi.request.EventRequest;
import java.util.Map;

public final class NativeFieldRetirementDiagnosticV3 {
    private static final String TARGET="net.farmingtales.ftboss.item.NativeExpectedFieldObservation";
    private static final String AUTHORITY="net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority";
    private static final Map<String,long[]> RETIRE=Map.ofEntries(Map.entry("install(Lnet/minecraft/world/entity/Entity;Lnet/farmingtales/ftboss/item/OwnerFieldExpectedAuthority$DomainBefore;)Z",new long[]{33}),Map.entry("loadAssigned(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/nbt/CompoundTag;)V",new long[]{98}),Map.entry("persistentExposed(Lnet/minecraft/world/entity/Entity;Ljava/lang/Object;)V",new long[]{96}),Map.entry("changed(Ljava/lang/Object;)V",new long[]{50}),Map.entry("alias(Ljava/lang/Object;Ljava/lang/Object;Z)V",new long[]{67}),Map.entry("advance(Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Session;Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Step;)Z",new long[]{272,289,321,338}));
    private static final Map<String,long[]> RANGED=Map.ofEntries(Map.entry("plannedRangedFieldMutation(Ljava/lang/Object;I)Z",new long[]{162,254,544,657,812}));
    private static int observations;
    private static final java.util.List<com.sun.jdi.request.BreakpointRequest> retireRequests=new java.util.ArrayList<>();
    private static final java.util.List<com.sun.jdi.request.BreakpointRequest> returnRequests=new java.util.ArrayList<>();
    private static final java.util.List<com.sun.jdi.request.BreakpointRequest> entryRequests=new java.util.ArrayList<>();
    private static long armedThread=-1L;
    private static int plannedDepth=-1;


    private static Value field(ObjectReference object,String name) {
        if (object==null) return null;
        var member=object.referenceType().allFields().stream().filter(value->value.name().equals(name)).findFirst().orElse(null);
        return member==null?null:object.getValue(member);
    }

    private static String value(Value member) {
        if (member==null) return "null";
        if (member instanceof StringReference text) return text.value().replace('\n',' ').replace('\r',' ').substring(0,Math.min(180,text.value().length()));
        if (member instanceof PrimitiveValue) return member.toString();
        if (member instanceof ObjectReference object) return object.referenceType().name()+"#"+object.uniqueID();
        return member.type().name();
    }

    private static void describe(String label,ObjectReference object,String... members) {
        System.out.println(label+"="+value(object));
        if (object==null) return;
        for (String name:members) {
            Value member=field(object,name);
            System.out.println(label+"."+name+"="+value(member));
            if (member instanceof ObjectReference reference && name.equals("kind")) System.out.println(label+"."+name+".name="+value(field(reference,"name")));
            if (member instanceof ArrayReference array && array.length()<=16) System.out.println(label+"."+name+".values="+array.getValues().stream().map(NativeFieldRetirementDiagnosticV3::value).toList());
        }
    }

    private static ObjectReference current(ThreadReference thread) {
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
        System.out.println("METHOD_ARGUMENTS "+thread.frame(0).getArgumentValues().stream().map(NativeFieldRetirementDiagnosticV3::value).toList());
        boolean retirement=location.declaringType().name().equals(AUTHORITY);
        var fact=field(scope,"active") instanceof ObjectReference value?value:null;
        if (!retirement && (fact==null || !(field(fact,"site") instanceof StringReference site) || !java.util.List.of("RANGED_LONG","RANGED_ROOT","MAP_PUT").contains(site.value()))) return;
        if (++observations>24) return;
        System.out.println((retirement?"FIELD_RETIRE_BEFORE_WRITE":"PLANNED_RANGED_RETURN")+" method="+location.method().name()+location.method().signature()+" bci="+location.codeIndex()+" line="+location.lineNumber());
        describe("scope",scope,"kind","retired","closed","sequence","step","active","lastBinding","acquisitionRoot","persistentRoot","rangedBuffer","player");
        describe("fact",fact,"site","thread","receiver","key","argument","parent","attempted","returned","originalFailure","originalStep","bindingBefore","directMapChildren");
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
            if (index<4) System.out.println("FRAME_ARGUMENTS "+index+" "+frames.get(index).getArgumentValues().stream().map(NativeFieldRetirementDiagnosticV3::value).toList());
            var frame=frames.get(index).location();
            System.out.println("STACK "+index+" "+frame.declaringType().name()+"."+frame.method().name()+frame.method().signature()+" line="+frame.lineNumber()+" bci="+frame.codeIndex());
        }
        System.out.flush();
    }

    private static com.sun.jdi.request.BreakpointRequest breakpoint(VirtualMachine machine,Location location,String mode,boolean enabled) {
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
            if (arguments.size()<6 || !(arguments.get(4) instanceof ObjectReference commit)
                || !(field(commit,"originalSequence") instanceof ObjectReference session)
                || !(field(session,"lineage") instanceof ObjectReference lineage)
                || !(field(lineage,"issuer") instanceof ObjectReference writer)
                || !(field(writer,"fieldPreparation") instanceof ObjectReference prepared)
                || !session.equals(field(prepared,"session"))
                || !thread.equals(field(writer,"thread"))
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
                    if (event instanceof BreakpointEvent entered) dispatch(entered);
                    if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) { System.out.println("OWNED_JVM_TERMINAL observations="+observations); return; }
                }
                events.resume();
            }
        } catch (VMDisconnectedException complete) { System.out.println("OWNED_JVM_DISCONNECTED observations="+observations); }
    }
}
