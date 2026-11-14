import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.event.*;
import com.sun.jdi.request.EventRequest;
import java.util.Map;

public final class NativePrivateImageDiagnostic {
    private static final String TARGET="net.farmingtales.ftboss.item.NativeExpectedFieldObservation";
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
            if (member instanceof ArrayReference array && array.length()<=16) System.out.println(label+"."+name+".values="+array.getValues().stream().map(NativePrivateImageDiagnostic::value).toList());
        }
    }

    private static void observe(ThreadReference thread) throws Exception {
        var arguments=thread.frame(0).getArgumentValues();
        if (arguments.size()!=2 || !(arguments.get(1) instanceof ObjectReference failure)) return;
        Value message=field(failure,"detailMessage");
        if (!(message instanceof StringReference text)) return;
        if (++observations>12) return;
        System.out.println("REJECTION index="+observations+" message="+text.value());
        var scope=(ObjectReference)arguments.get(0);
        describe("scope",scope,"kind","sourceA","leaf","writer","commit","finalPublication","domain","sequence","step","active","clock","retired","closed","sourceTag","timerReceiver","timerKey","persistentRoot","acquisitionRoot","rangedBuffer","unsupportedTopology");
        if (field(scope,"domain") instanceof ObjectReference domain) describe("domain",domain,"sourceA","sequence","root","lineage","ownerWriter","generation");
        if (field(scope,"step") instanceof ObjectReference step) {
            describe("step",step,"originalOperation","originalReturned","closed","expected","operation");
            if (field(step,"expected") instanceof ObjectReference expected) describe("expected",expected,"kind","path","key","before","after","writes");
        }
        var frames=thread.frames();
        for (int index=0;index<Math.min(frames.size(),32);index++) {
            var frame=frames.get(index);
            var location=frame.location();
            System.out.println("STACK "+index+" "+location.declaringType().name()+"."+location.method().name()+location.method().signature()+" line="+location.lineNumber()+" bci="+location.codeIndex());
            if (location.declaringType().name().equals(TARGET) && location.method().name().equals("beforeMap") && !frame.getArgumentValues().isEmpty()) {
                var fact=(ObjectReference)frame.getArgumentValues().get(0);
                for (int depth=0;fact!=null && depth<12;depth++) {
                    String label="fact"+depth;
                    describe(label,fact,"site","receiver","physicalOwner","key","argument","originalStep","expectedFieldDomain","path","entry","exit","attempted","returned","directMapChildren","nativeTransitionMatched","nativeTimerBufferMatched");
                    if (field(fact,"argument") instanceof ObjectReference argument) describe(label+".argument",argument,"data");
                    fact=field(fact,"parent") instanceof ObjectReference parent?parent:null;
                }
            }
        }
        System.out.flush();
    }

    private static void install(VirtualMachine machine,ReferenceType type) {
        for (var method:type.methodsByName(type.name().endsWith("FoodWeaponSourceLedger")?"nativeTimerExpectationReturned":"rejectExtra")) {
            var request=machine.eventRequestManager().createBreakpointRequest(method.location());
            request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
            request.enable();
            System.out.println("INSTALLED "+type.name()+"."+method.name()+method.signature());
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
            var exits=machine.eventRequestManager().createMethodExitRequest();
            exits.addClassFilter("net.farmingtales.ftboss.item.FoodWeaponSourceLedger");
            exits.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
            var ledgerPrepare=machine.eventRequestManager().createClassPrepareRequest();
            ledgerPrepare.addClassFilter("net.farmingtales.ftboss.item.FoodWeaponSourceLedger");
            ledgerPrepare.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
            ledgerPrepare.enable();
            var prepare=machine.eventRequestManager().createClassPrepareRequest();
            prepare.addClassFilter(TARGET);
            prepare.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
            prepare.enable();
            for (var type:machine.allClasses()) if (type.name().equals(TARGET) || type.name().equals("net.farmingtales.ftboss.item.FoodWeaponSourceLedger")) install(machine,type);
            machine.resume();
            while (true) {
                var events=machine.eventQueue().remove();
                for (var event:events) {
                    if (event instanceof ClassPrepareEvent prepared) install(machine,prepared.referenceType());
                    if (event instanceof BreakpointEvent entered) {
                        if (entered.location().method().name().equals("nativeTimerExpectationReturned")) {
                            System.out.println("PRIVATE_IMAGE_LEDGER_ENTER");
                            exits.enable();
                        } else observe(entered.thread());
                    }
                    if (event instanceof MethodExitEvent exited && exited.method().name().equals("nativeTimerExpectationReturned")) {
                        System.out.println("PRIVATE_IMAGE_LEDGER_RETURN result="+exited.returnValue()+" bci="+exited.location().codeIndex()+" line="+exited.location().lineNumber());
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
