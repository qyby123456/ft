import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.List;

public final class ActiveFaultDebugger {
    private static Value field(ObjectReference owner, String name) {
        return owner.getValue(owner.referenceType().fieldByName(name));
    }

    private static Value invoke(ObjectReference owner, ThreadReference thread, String name, String signature, Value... arguments) throws Exception {
        Method method = owner.referenceType().methodsByName(name, signature).getFirst();
        return owner.invokeMethod(thread, method, List.of(arguments), ObjectReference.INVOKE_SINGLE_THREADED);
    }

    private static void install(VirtualMachine machine, ReferenceType target) {
        Method method = target.methodsByName("putLong", "(Ljava/lang/String;J)V").getFirst();
        byte[] code = method.bytecodes();
        if (code[code.length - 1] != (byte)177) throw new IllegalStateException("putLong does not end in RETURN");
        BreakpointRequest request = machine.eventRequestManager().createBreakpointRequest(method.locationOfCodeIndex(code.length - 1));
        request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
        request.enable();
        System.out.println("Breakpoint installed at original putLong return: " + (code.length - 1));
    }

    private static void inject(VirtualMachine machine, BreakpointEvent event) throws Exception {
        List<ReferenceType> fixtures = machine.classesByName("ftboss.storage308.ActiveWeapon436");
        if (fixtures.isEmpty()) return;
        ClassType fixture = (ClassType)fixtures.getFirst();
        Value modeValue = fixture.getValue(fixture.fieldByName("faultMode"));
        if (!(modeValue instanceof StringReference mode)) return;
        ThreadReference thread = event.thread();
        StackFrame frame = thread.frame(0);
        List<Value> arguments = frame.getArgumentValues();
        if (!(arguments.getFirst() instanceof StringReference key) || !key.value().equals("LastSkillProc.blood_feast")) return;
        ObjectReference receiver = frame.thisObject();
        BreakpointRequest request = (BreakpointRequest)event.request();
        request.disable();
        try {
            ReferenceType observation = machine.classesByName("net.farmingtales.ftboss.item.NativeExpectedFieldObservation").getFirst();
            ObjectReference current = (ObjectReference)observation.getValue(observation.fieldByName("CURRENT"));
            ObjectReference scope = (ObjectReference)invoke(current, thread, "get", "()Ljava/lang/Object;");
            if (scope == null) return;
            ObjectReference fact = (ObjectReference)field(scope, "active");
            if (fact == null || !((StringReference)field(fact, "site")).value().equals("RANGED_LONG")
                || !receiver.equals(field(fact, "receiver"))) return;
            ObjectReference sequence = (ObjectReference)field(scope, "sequence");
            boolean coverage = ((BooleanValue)field(sequence, "coverageInstalled")).value();
            boolean revoked = ((BooleanValue)field(sequence, "traceRevoked")).value();
            if (!coverage || revoked) throw new IllegalStateException("Fault injection requires a live covered Session");
            String selected = mode.value();
            fixture.setValue(fixture.fieldByName("faultMode"), null);
            fixture.setValue(fixture.fieldByName("faultInjected"), machine.mirrorOf(true));
            Value value = arguments.get(1);
            switch (selected) {
                case "wrong_key" -> invoke(receiver, thread, "putLong", "(Ljava/lang/String;J)V", machine.mirrorOf("LastSkillProc.foreign"), value);
                case "wrong_receiver" -> invoke((ObjectReference)field(scope, "persistentRoot"), thread, "putLong", "(Ljava/lang/String;J)V", key, value);
                case "alias_round_trip" -> {
                    ObjectReference alias = (ObjectReference)invoke(receiver, thread, "getAllKeys", "()Ljava/util/Set;");
                    invoke(alias, thread, "remove", "(Ljava/lang/Object;)Z", key);
                    invoke(receiver, thread, "putLong", "(Ljava/lang/String;J)V", key, value);
                }
                case "exception" -> thread.stop((ObjectReference)fixture.getValue(fixture.fieldByName("faultException")));
                default -> throw new IllegalStateException("Unknown active fault: " + selected);
            }
            System.out.println("Injected " + selected + " with coverageInstalled=" + coverage + ", traceRevokedBefore=" + revoked);
        } finally {
            request.enable();
        }
    }

    public static void main(String[] arguments) throws Exception {
        AttachingConnector connector = Bootstrap.virtualMachineManager().attachingConnectors().stream()
            .filter(candidate -> candidate.name().equals("com.sun.jdi.SocketAttach")).findFirst().orElseThrow();
        var settings = connector.defaultArguments();
        settings.get("hostname").setValue("127.0.0.1");
        settings.get("port").setValue(arguments[0]);
        VirtualMachine machine = null;
        for (int attempt = 0; attempt < 120 && machine == null; attempt++) {
            try { machine = connector.attach(settings); }
            catch (java.io.IOException waiting) { Thread.sleep(250); }
        }
        if (machine == null) throw new IllegalStateException("Cannot attach to isolated test server");
        ClassPrepareRequest prepare = machine.eventRequestManager().createClassPrepareRequest();
        prepare.addClassFilter("net.minecraft.nbt.CompoundTag");
        prepare.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        prepare.enable();
        for (ReferenceType target : machine.classesByName("net.minecraft.nbt.CompoundTag")) install(machine, target);
        machine.resume();
        try {
            while (true) {
                EventSet events = machine.eventQueue().remove();
                for (Event event : events) {
                    if (event instanceof ClassPrepareEvent prepared) install(machine, prepared.referenceType());
                    else if (event instanceof BreakpointEvent breakpoint) inject(machine, breakpoint);
                    else if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) return;
                }
                events.resume();
            }
        } catch (VMDisconnectedException ended) {
            System.out.println("Isolated test VM disconnected");
        }
    }
}
