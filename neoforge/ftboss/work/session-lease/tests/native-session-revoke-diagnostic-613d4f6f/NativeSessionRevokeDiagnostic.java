import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.event.*;
import com.sun.jdi.request.EventRequest;
import java.util.Map;

public final class NativeSessionRevokeDiagnostic {
    private static final String TARGET="net.farmingtales.ftboss.item.FoodWeaponOriginalSequence$Session";
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
            if (member instanceof ArrayReference array && array.length()<=16) System.out.println(label+"."+name+".values="+array.getValues().stream().map(NativeSessionRevokeDiagnostic::value).toList());
        }
    }

    private static void observe(ThreadReference thread) throws Exception {
        if (++observations>12) return;
        var arguments=thread.frame(0).getArgumentValues();
        var session=thread.frame(0).thisObject();
        System.out.println("SESSION_REVOKE index="+observations+" phase="+(arguments.size()>1?value(arguments.get(1)):"missing"));
        describe("session",session,"next","traceRevoked","coverageInstalled","active","expectedCursor","lineage","firstFailedStep","firstObservationFailure","firstRevocationSite434");
        if (!arguments.isEmpty() && arguments.getFirst() instanceof ObjectReference step) {
            describe("step",step,"expected","entered","originalReturned","closed","phase","failedPhase","originalFailure","observationFailure","nativeTimerPrefixesPrepared","nativeTimerComponentAssigned","writeObserved","copyVerified","observedPrefixes");
            if (field(step,"expected") instanceof ObjectReference operation) describe("operation",operation,"kind","key","argument","before","after","write");
        }
        if (arguments.size()>2 && arguments.get(2) instanceof ObjectReference failure) describe("failure",failure,"detailMessage");
        var frames=thread.frames();
        for (int index=0;index<Math.min(frames.size(),24);index++) {
            var location=frames.get(index).location();
            System.out.println("STACK "+index+" "+location.declaringType().name()+"."+location.method().name()+location.method().signature()+" line="+location.lineNumber()+" bci="+location.codeIndex());
        }
        System.out.flush();
    }

    private static void install(VirtualMachine machine,ReferenceType type) {
        for (var method:type.methodsByName(type.name().equals(TARGET)?"revoke":"advance")) {
            var request=machine.eventRequestManager().createBreakpointRequest(method.location());
            request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
            request.enable();
            System.out.println("INSTALLED "+type.name()+"."+method.name()+method.signature());
        }
        System.out.flush();
    }

    public static void main(String[] arguments) throws Exception {
        String authority="net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority";
        AttachingConnector connector=Bootstrap.virtualMachineManager().attachingConnectors().stream().filter(value->value.name().equals("com.sun.jdi.SocketAttach")).findFirst().orElseThrow();
        Map<String,com.sun.jdi.connect.Connector.Argument> options=connector.defaultArguments();
        options.get("hostname").setValue("127.0.0.1");
        options.get("port").setValue(arguments[0]);
        VirtualMachine machine=null;
        for (int attempt=0;attempt<300 && machine==null;attempt++) {
            try { machine=connector.attach(options); } catch (java.io.IOException unavailable) { if (attempt==299) throw unavailable; Thread.sleep(100); }
        }
        try {
            var exits=machine.eventRequestManager().createMethodExitRequest();
            exits.addClassFilter(authority);
            exits.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
            for (String name:new String[]{TARGET,authority}) {
                var prepare=machine.eventRequestManager().createClassPrepareRequest();
                prepare.addClassFilter(name);
                prepare.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
                prepare.enable();
            }
            for (var type:machine.allClasses()) if (type.name().equals(TARGET) || type.name().equals(authority)) install(machine,type);
            machine.resume();
            while (true) {
                var events=machine.eventQueue().remove();
                for (var event:events) {
                    if (event instanceof ClassPrepareEvent prepared) install(machine,prepared.referenceType());
                    if (event instanceof BreakpointEvent entered) {
                        if (entered.location().method().name().equals("revoke")) observe(entered.thread());
                        else {
                            System.out.println("FIELD_ADVANCE_ENTER");
                            exits.enable();
                        }
                    }
                    if (event instanceof MethodExitEvent exited && exited.method().name().equals("advance")) {
                        System.out.println("FIELD_ADVANCE_RETURN result="+exited.returnValue()+" bci="+exited.location().codeIndex()+" line="+exited.location().lineNumber());
                        exits.disable();
                        System.out.flush();
                    }
                    if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) { System.out.println("OWNED_JVM_TERMINAL observations="+observations); return; }
                }
                events.resume();
            }
        } catch (VMDisconnectedException complete) { System.out.println("OWNED_JVM_DISCONNECTED observations="+observations); }
    }
}
