import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class NativeDomainRetirementWatch {
    private static final String TARGET="net.farmingtales.ftboss.item.NativeExpectedFieldObservation";
    private static ObjectReference domain;
    private static ModificationWatchpointRequest watch;
    private static MethodExitRequest exits;
    private static boolean captured;
    private static int lookupReturns;
    private static int errors;

    private static Value field(ObjectReference owner,String name) {
        if (owner==null) return null;
        var member=owner.referenceType().fieldByName(name);
        return member==null?null:owner.getValue(member);
    }

    private static String value(Value member) {
        if (member==null) return "null";
        if (member instanceof StringReference text) return text.value().replace('\n',' ').replace('\r',' ');
        if (member instanceof PrimitiveValue) return member.toString();
        if (member instanceof ObjectReference object) return object.referenceType().name()+"#"+object.uniqueID();
        return member.type().name();
    }

    private static void describe(String label,ObjectReference owner,String... names) {
        System.out.println(label+"="+value(owner));
        if (owner==null) return;
        for (String name:names) {
            var member=field(owner,name);System.out.println(label+"."+name+"="+value(member));
            if (member instanceof ObjectReference object && name.equals("kind")) System.out.println(label+".kind.name="+value(field(object,"name")));
        }
    }

    private static ObjectReference current(ThreadReference thread) {
        var types=thread.virtualMachine().classesByName(TARGET);
        if (types.isEmpty()) return null;
        var type=types.getFirst();var key=type.getValue(type.fieldByName("CURRENT"));
        if (!(field(thread,"threadLocals") instanceof ObjectReference locals) || !(field(locals,"table") instanceof ArrayReference table)) return null;
        for (Value member:table.getValues()) if (member instanceof ObjectReference entry && key.equals(field(entry,"referent")) && field(entry,"value") instanceof ObjectReference scope) return scope;
        return null;
    }

    private static void install(VirtualMachine machine,ReferenceType type) {
        for (var method:type.methodsByName("rangedSave")) {
            var request=machine.eventRequestManager().createBreakpointRequest(method.location());
            request.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);request.enable();
        }
        System.out.println("ARM_BREAKPOINT_ONLY "+type.name());System.out.flush();
    }

    private static void arm(BreakpointEvent event) throws Exception {
        if (domain!=null) return;
        var thread=event.thread();var arguments=thread.frame(0).getArgumentValues();
        if (arguments.size()<6 || !(arguments.get(4) instanceof ObjectReference commit)
            || !(field(commit,"originalSequence") instanceof ObjectReference session)
            || !(field(session,"lineage") instanceof ObjectReference lineage)
            || !(field(lineage,"issuer") instanceof ObjectReference writer)
            || !(field(writer,"fieldPreparation") instanceof ObjectReference prepared)
            || !session.equals(field(prepared,"session")) || !thread.equals(field(writer,"thread"))
            || !(field(writer,"closed") instanceof BooleanValue closed) || closed.value()
            || !(field(session,"coverageInstalled") instanceof BooleanValue covered) || !covered.value()
            || !(field(session,"traceRevoked") instanceof BooleanValue revoked) || revoked.value()
            || !(field(prepared,"player") instanceof ObjectReference actual)) return;
        domain=actual;
        if (!(field(domain,"retired") instanceof BooleanValue retired) || retired.value()) throw new IllegalStateException("Actual domain already retired at bound original ranged-save entry");
        var machine=thread.virtualMachine();
        if (!machine.canWatchFieldModification() || !machine.canUseInstanceFilters()) throw new IllegalStateException("Exact instance modification watchpoints unavailable");
        watch=machine.eventRequestManager().createModificationWatchpointRequest(domain.referenceType().fieldByName("retired"));
        watch.addInstanceFilter(domain);watch.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);watch.enable();
        if (machine.canGetMethodReturnValues()) {
            exits=machine.eventRequestManager().createMethodExitRequest();
            exits.addClassFilter("net.farmingtales.ftboss.item.NativeFreshFactoryTrust298");exits.addThreadFilter(thread);
            exits.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);exits.enable();
        }
        event.request().disable();
        System.out.println("ARMED_EXACT_NONRETIRED_DOMAIN originalSession="+value(session)+" domain="+value(domain)+" thread="+thread.uniqueID());
        describe("domainAtArm",domain,"retired","originated","loading","originalPersistent","expected");
        System.out.flush();
    }

    private static void retirement(ModificationWatchpointEvent event) throws Exception {
        System.out.println("EXACT_DOMAIN_MODIFICATION object="+value(event.object())+" old="+value(event.valueCurrent())+" new="+value(event.valueToBe()));
        if (!event.object().equals(domain) || !(event.valueCurrent() instanceof BooleanValue old) || old.value()
            || !(event.valueToBe() instanceof BooleanValue next) || !next.value()) return;
        captured=true;watch.disable();if (exits!=null) exits.disable();
        var location=event.location();
        System.out.println("FIRST_EXACT_RETIRE method="+location.declaringType().name()+"."+location.method().name()+location.method().signature()+" bci="+location.codeIndex()+" line="+location.lineNumber());
        var scope=current(event.thread());
        describe("scope",scope,"kind","closed","sequence","step","active","lastBinding","acquisitionRoot","persistentRoot","rangedBuffer","player");
        if (field(scope,"active") instanceof ObjectReference fact) {
            describe("fact",fact,"site","receiver","key","argument","parent","attempted","returned","originalFailure","originalStep","bindingBefore","directMapChildren");
            if (field(fact,"originalStep") instanceof ObjectReference step && field(step,"expected") instanceof ObjectReference operation) {
                describe("operation",operation,"kind","key","argument");
                if (field(operation,"before") instanceof ObjectReference before) describe("before",before,"rangedAcquired","rangedAttached","rangedPlayerIdentity","rangedBufferIdentity","player","rangedBuffer");
            }
        }
        describe("watchedDomain",domain,"retired","originated","loading","originalPersistent","expected");
        var frames=event.thread().frames();
        for (int index=0;index<Math.min(frames.size(),22);index++) {
            var frame=frames.get(index).location();
            System.out.println("STACK "+index+" "+frame.declaringType().name()+"."+frame.method().name()+frame.method().signature()+" bci="+frame.codeIndex()+" line="+frame.lineNumber());
        }
        try {System.out.println("RETIRE_METHOD_ARGUMENTS "+event.thread().frame(0).getArgumentValues().stream().map(NativeDomainRetirementWatch::value).toList());}
        catch (RuntimeException unavailable) {System.out.println("OPTIONAL_ARGUMENT_READ_UNAVAILABLE "+unavailable);}
        System.out.flush();
    }

    public static void main(String[] arguments) throws Exception {
        AttachingConnector connector=Bootstrap.virtualMachineManager().attachingConnectors().stream().filter(item->item.name().equals("com.sun.jdi.SocketAttach")).findFirst().orElseThrow();
        var options=connector.defaultArguments();options.get("hostname").setValue("127.0.0.1");options.get("port").setValue(arguments[0]);
        VirtualMachine machine=null;
        for (int attempt=0;attempt<300 && machine==null;attempt++) {
            try {machine=connector.attach(options);}catch(java.io.IOException unavailable){if(attempt==299)throw unavailable;Thread.sleep(100);}
        }
        try {
            var prepare=machine.eventRequestManager().createClassPrepareRequest();prepare.addClassFilter(TARGET);prepare.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);prepare.enable();
            for (var type:machine.classesByName(TARGET)) install(machine,type);
            machine.resume();boolean terminal=false;
            while (!terminal) {
                var events=machine.eventQueue().remove();
                try {
                    for (var event:events) {
                        if (event instanceof ClassPrepareEvent prepared) install(machine,prepared.referenceType());
                        if (event instanceof BreakpointEvent entered) arm(entered);
                        if (event instanceof ModificationWatchpointEvent changed) retirement(changed);
                        if (event instanceof MethodExitEvent returned && returned.method().name().equals("rangedTimerMutationFrame")) {lookupReturns++;System.out.println("CERTIFIED_RANGED_FRAME_RETURN "+value(returned.returnValue()));System.out.flush();}
                        if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) terminal=true;
                    }
                } catch (VMDisconnectedException complete) {terminal=true;}
                catch (Exception inspectionFailure) {errors++;System.out.println("DIAGNOSTIC_INSPECTION_ERROR "+inspectionFailure);System.out.flush();}
                finally {try {events.resume();}catch(VMDisconnectedException complete){terminal=true;}}
            }
        } catch (VMDisconnectedException complete) {System.out.println("OWNED_NATIVE_JVM_DISCONNECTED");}
        String report="{\"readOnly\":true,\"methodInvocations\":false,\"authorityWrites\":false,\"armed\":"+(domain!=null)+",\"firstExactRetirementCaptured\":"+captured+",\"certifiedFrameLookupReturns\":"+lookupReturns+",\"diagnosticErrors\":"+errors+"}";
        Files.writeString(Path.of(arguments[1]).resolve("domain-retirement-watch.json"),report);
        System.out.println(report);
        if (errors!=0) System.exit(1);
    }
}
