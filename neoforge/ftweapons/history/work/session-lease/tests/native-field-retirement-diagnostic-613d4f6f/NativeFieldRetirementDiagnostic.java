import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.event.*;
import com.sun.jdi.request.EventRequest;
import java.util.Map;

public final class NativeFieldRetirementDiagnostic {
    private static final String TARGET="net.farmingtales.ftboss.item.NativeExpectedFieldObservation";
    private static final String AUTHORITY="net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority";
    private static final Map<String,long[]> RETIRE=Map.ofEntries(Map.entry("install(Lnet/minecraft/world/entity/Entity;Lnet/farmingtales/ftboss/item/OwnerFieldExpectedAuthority$DomainBefore;)Z",new long[]{33}),Map.entry("loadAssigned(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/nbt/CompoundTag;)V",new long[]{98}),Map.entry("persistentExposed(Lnet/minecraft/world/entity/Entity;Ljava/lang/Object;)V",new long[]{96}),Map.entry("changed(Ljava/lang/Object;)V",new long[]{50}),Map.entry("alias(Ljava/lang/Object;Ljava/lang/Object;Z)V",new long[]{67}),Map.entry("advance(Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Session;Lnet/farmingtales/ftboss/item/FoodWeaponOriginalSequence$Step;)Z",new long[]{272,289,321,338}));
    private static final Map<String,long[]> RANGED=Map.ofEntries(Map.entry("plannedRangedFieldMutation(Ljava/lang/Object;I)Z",new long[]{162,254,544,657,812}));
    private static int observations;

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
            if (member instanceof ArrayReference array && array.length()<=16) System.out.println(label+"."+name+".values="+array.getValues().stream().map(NativeFieldRetirementDiagnostic::value).toList());
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
