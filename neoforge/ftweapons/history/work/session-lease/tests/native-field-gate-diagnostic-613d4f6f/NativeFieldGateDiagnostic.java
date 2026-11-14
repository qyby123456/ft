import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.event.*;
import com.sun.jdi.request.EventRequest;
import java.util.Map;

public final class NativeFieldGateDiagnostic {
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
            if (member instanceof ArrayReference array && array.length()<=16) System.out.println(label+"."+name+".values="+array.getValues().stream().map(NativeFieldGateDiagnostic::value).toList());
        }
    }

    private static int gateObservations;
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
            var request=machine.eventRequestManager().createBreakpointRequest(method.name().equals("advance")?method.locationOfCodeIndex(155L):method.location());
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
                            observeGate(entered.thread());
                        }
                    }
                    if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) { System.out.println("OWNED_JVM_TERMINAL observations="+observations); return; }
                }
                events.resume();
            }
        } catch (VMDisconnectedException complete) { System.out.println("OWNED_JVM_DISCONNECTED observations="+observations); }
    }
}
