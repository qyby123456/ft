import com.sun.jdi.Bootstrap;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.event.*;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.MethodExitRequest;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class DamageAuthorizationDiagnostic {
    private static Value field(com.sun.jdi.ObjectReference object, String name) {
        var member = object.referenceType().allFields().stream().filter(value -> value.name().equals(name)).findFirst().orElse(null);
        return member == null ? null : object.getValue(member);
    }
    private static void describe(String label, Value value) {
        if (!(value instanceof com.sun.jdi.ObjectReference object)) return;
        System.out.println("STATE " + label + " type=" + object.referenceType().name());
        for (String name : new String[]{"closed", "invalid", "returned", "tick"}) {
            Value member = field(object, name);
            if (member instanceof com.sun.jdi.PrimitiveValue) System.out.println(name + "=" + member);
        }
        Value tags = field(object, "tags");
        if (tags instanceof com.sun.jdi.ObjectReference map && field(map, "table") instanceof com.sun.jdi.ArrayReference table) {
            for (Value bucket : table.getValues()) {
                while (bucket instanceof com.sun.jdi.ObjectReference entry) {
                    Value key = field(entry, "key");
                    if (key instanceof com.sun.jdi.StringReference text && Set.of("DamagePurpose", "RarityFactor", "AppliedMaterial", "AppliedEvolution", "AppliedRarity").contains(text.value())) {
                        Value tag = field(entry, "value");
                        if (tag instanceof com.sun.jdi.ObjectReference compound) {
                            Value data = field(compound, "data");
                            System.out.println(text.value() + "=" + (data instanceof com.sun.jdi.StringReference string ? string.value() : data));
                        }
                    }
                    bucket = field(entry, "next");
                }
            }
        }
        System.out.flush();
    }
    private static void install(VirtualMachine machine, com.sun.jdi.ReferenceType type) {
        for (com.sun.jdi.Method method : type.methods()) {
            if (!Set.of("commitPreparedOffense", "damageAuthorized", "nativeHurtLive", "nativeMeleeReady").contains(method.name())) continue;
            if (method.name().equals("commitPreparedOffense") && method.argumentTypeNames().size() != 4) continue;
            BreakpointRequest breakpoint = machine.eventRequestManager().createBreakpointRequest(method.location());
            breakpoint.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
            breakpoint.enable();
        }
    }

    public static void main(String[] arguments) throws Exception {
        AttachingConnector connector = Bootstrap.virtualMachineManager().attachingConnectors().stream().filter(value -> value.name().equals("com.sun.jdi.SocketAttach")).findFirst().orElseThrow();
        Map<String, com.sun.jdi.connect.Connector.Argument> options = connector.defaultArguments();
        options.get("hostname").setValue("127.0.0.1");
        options.get("port").setValue(arguments[0]);
        VirtualMachine machine = null;
        for (int retry = 0; retry < 200 && machine == null; retry++) {
            try { machine = connector.attach(options); } catch (java.io.IOException missing) { Thread.sleep(100); }
        }
        if (machine == null) throw new IllegalStateException("Owned diagnostic JVM did not open its localhost endpoint");
        String prefix = "net.farmingtales.ftboss.item.";
        ClassPrepareRequest preparation = machine.eventRequestManager().createClassPrepareRequest();
        preparation.addClassFilter(prefix + "Food*");
        preparation.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
        preparation.enable();
        for (String name : new String[]{"FoodSocketAttributePowers", "FoodWeaponSourceLedger"})
            for (com.sun.jdi.ReferenceType type : machine.classesByName(prefix + name)) install(machine, type);
        List<MethodExitRequest> returnRequests = new ArrayList<>();
        for (String name : new String[]{"FoodSocketAttributePowers", "FoodWeaponFinalDamage", "FoodWeaponConsumerSequence$Accepted", "FoodWeaponBondHits", "FoodWeaponSourceLedger", "OwnerRepairGenerationAuthority", "OwnerLineageRevocationStore", "FoodWeaponDamageScaling"}) {
            MethodExitRequest exits = machine.eventRequestManager().createMethodExitRequest();
            exits.addClassFilter(prefix + name);
            exits.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
            returnRequests.add(exits);
        }
        Set<String> names = Set.of("commitPreparedOffense", "offenseContext", "offenseWeaponContinuous", "matchesAttributeReceipt", "matchesOffensePublishing", "publishingIdentity", "localPublishingIdentity", "matchesReceipt", "lifetime", "equipped", "secondary", "target", "validReadOnly", "snapshotAllowed", "snapshotFields", "liveLease", "locallyLive", "foundationContinuous", "damageAuthorized", "nativeHurtLive", "nativeMeleeReady", "expectationMatches", "matches", "residual");
        long watchedThread = -1;
        machine.resume();
        System.out.println("attachedReadOnly=true;methodInvocation=false;fieldWrites=false");
        System.out.flush();
        try {
            while (true) {
                EventSet events = machine.eventQueue().remove();
                for (Event event : events) {
                    if (event instanceof ClassPrepareEvent prepared && Set.of(prefix + "FoodSocketAttributePowers", prefix + "FoodWeaponSourceLedger").contains(prepared.referenceType().name())) install(machine, prepared.referenceType());
                    if (event instanceof BreakpointEvent entered && entered.location().method().name().equals("commitPreparedOffense")) {
                        watchedThread = entered.thread().uniqueID();
                        for (MethodExitRequest request : returnRequests) request.enable();
                        System.out.println("ENTER " + entered.location().method().name() + entered.location().method().signature());
                    }
                    if (event instanceof BreakpointEvent entered && entered.thread().uniqueID() == watchedThread) {
                        var frame = entered.thread().frame(0);
                        if (!frame.getArgumentValues().isEmpty()) describe(entered.location().method().name(), frame.getArgumentValues().get(0));
                    }
                    if (event instanceof MethodExitEvent exited && exited.thread().uniqueID() == watchedThread && names.contains(exited.method().name())) {
                        Value result = exited.returnValue();
                        String value = result == null ? "null" : result instanceof com.sun.jdi.PrimitiveValue ? result.toString() : "object:" + result.type().name();
                        System.out.println(exited.method().declaringType().name() + "." + exited.method().name() + " argc=" + exited.method().argumentTypeNames().size() + " bci=" + exited.location().codeIndex() + " line=" + exited.location().lineNumber() + " result=" + value);
                        if (exited.method().name().equals("commitPreparedOffense") && exited.method().argumentTypeNames().size() == 4) {
                            watchedThread = -1;
                            for (MethodExitRequest request : returnRequests) request.disable();
                        }
                        System.out.flush();
                    }
                    if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) return;
                }
                events.resume();
            }
        } catch (com.sun.jdi.VMDisconnectedException complete) {
            System.out.println("ownedJvmDisconnected=true");
        }
    }
}
