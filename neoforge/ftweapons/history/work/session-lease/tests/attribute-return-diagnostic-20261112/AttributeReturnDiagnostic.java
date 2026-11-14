import com.sun.jdi.Bootstrap;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.event.*;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class AttributeReturnDiagnostic {
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
        MethodEntryRequest entries = machine.eventRequestManager().createMethodEntryRequest();
        entries.addClassFilter(prefix + "FoodSocketAttributePowers");
        entries.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
        entries.enable();
        List<MethodExitRequest> returnRequests = new ArrayList<>();
        for (String name : new String[]{"FoodSocketAttributePowers", "FoodWeaponFinalDamage", "FoodWeaponConsumerSequence$Accepted", "FoodWeaponBondHits", "FoodWeaponSourceLedger", "OwnerRepairGenerationAuthority", "OwnerLineageRevocationStore"}) {
            MethodExitRequest exits = machine.eventRequestManager().createMethodExitRequest();
            exits.addClassFilter(prefix + name);
            exits.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
            returnRequests.add(exits);
        }
        Set<String> names = Set.of("commitPreparedOffense", "offenseContext", "offenseWeaponContinuous", "matchesAttributeReceipt", "matchesOffensePublishing", "publishingIdentity", "localPublishingIdentity", "matchesReceipt", "lifetime", "equipped", "secondary", "target", "validReadOnly", "snapshotAllowed", "snapshotFields", "liveLease", "locallyLive", "foundationContinuous", "damageAuthorized");
        long watchedThread = -1;
        machine.resume();
        System.out.println("attachedReadOnly=true;methodInvocation=false;fieldWrites=false");
        System.out.flush();
        try {
            while (true) {
                EventSet events = machine.eventQueue().remove();
                for (Event event : events) {
                    if (event instanceof MethodEntryEvent entered && entered.method().name().equals("commitPreparedOffense") && entered.method().argumentTypeNames().size() == 4) {
                        watchedThread = entered.thread().uniqueID();
                        for (MethodExitRequest request : returnRequests) request.enable();
                        System.out.println("ENTER " + entered.method().name() + entered.method().signature());
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
