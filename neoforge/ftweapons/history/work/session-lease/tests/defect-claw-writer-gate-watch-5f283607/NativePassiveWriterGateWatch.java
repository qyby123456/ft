import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class NativePassiveWriterGateWatch {
    private record Site(String owner,String name,String descriptor,int[] offsets) {}
    private static final Site[] SITES={
        new Site("net.farmingtales.ftboss.item.FoodWeaponSourceLedger","writerPlanningUnchanged","(Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterPublication;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$WriterPlanningCut;)Z",new int[]{77}),
        new Site("net.farmingtales.ftboss.item.FoodWeaponSourceLedger","writerPublicationUnchanged","(Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterPublication;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$WriterPlanningCut;)Z",new int[]{43,131}),
        new Site("net.farmingtales.ftboss.item.FoodWeaponSourceLedger","ownerWriterPublicationContinuation","(Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterPublication;)Z",new int[]{74}),
        new Site("net.farmingtales.ftboss.item.FoodWeaponSourceLedger","projectedBindingLive","(Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OriginalInteractionBinding;)Z",new int[]{62}),
        new Site("net.farmingtales.ftboss.item.FoodWeaponSourceLedger","projectedCurrentBindingLive","(Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OriginalInteractionBinding;)Z",new int[]{30,101,331}),
        new Site("net.farmingtales.ftboss.item.FoodWeaponSourceLedger","writerCaller","(Ljava/lang/Class;)Z",new int[]{}),
        new Site("net.farmingtales.ftboss.item.FoodWeaponSourceLedger","selectOwnerWriterLeaf","(Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterPublication;Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/Object;I)Z",new int[]{113,194,280,282}),
        new Site("net.farmingtales.ftboss.item.FoodWeaponSourceLedger","ownerWriterPublicationLive","(Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OwnerWriterPublication;)Z",new int[]{130}),
        new Site("net.farmingtales.ftboss.item.FoodWeaponFinalDamage","localPublishingIdentity","(Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$PublishingCleanup;Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$PublishingPhase;)Z",new int[]{495,499}),
        new Site("net.farmingtales.ftboss.item.FoodWeaponFinalDamage","matchesOwnerWriterPublication","(Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$OffenseReceipt;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$OriginalInteractionBinding;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$AttemptBatchQuote;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$AttemptBatchAdmission;Lnet/farmingtales/ftboss/item/FoodWeaponSourceLedger$AttemptBatchCommit;)Z",new int[]{14,28,157}),
        new Site("net.farmingtales.ftboss.item.FoodWeaponFinalDamage","localOwnerWriterSelection","(Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$OffenseReceipt;Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/Object;I)Z",new int[]{134,249,319,373,427,467,472,477}),
        new Site("net.farmingtales.ftboss.item.FoodWeaponFinalDamage","matchesOwnerWriterSelection","(Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$OffenseReceipt;Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/Object;I)Z",new int[]{30,46,157}),
        new Site("net.farmingtales.ftboss.item.FoodWeaponFinalDamage","selectPublishingWriter","(Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$PublishingCleanup;)Z",new int[]{23,68}),
        new Site("net.farmingtales.ftboss.item.FoodWeaponFinalDamage","originalPublicationLive","(Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$Publication;)Z",new int[]{25}),
        new Site("net.farmingtales.ftboss.item.FoodWeaponFinalDamage","extraOwnerAuthorityLive","(Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$Publication;)Z",new int[]{49}),
        new Site("net.farmingtales.ftboss.item.FoodWeaponFinalDamage","revokeExtraAuthority","(Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$Publication;Ljava/lang/Throwable;)V",new int[]{})
    };
    private static final String FINAL="net.farmingtales.ftboss.item.FoodWeaponFinalDamage";
    private static final List<BreakpointRequest> gates=new ArrayList<>();
    private static ObjectReference cleanup;
    private static ThreadReference ownerThread;
    private static boolean armed;
    private static boolean captured;
    private static int errors;
    private static int falseReturns;

    private static Value field(Value receiver,String name) {
        if (!(receiver instanceof ObjectReference object)) return null;
        Field member=object.referenceType().fieldByName(name);
        return member==null?null:object.getValue(member);
    }

    private static String value(Value member) {
        if (member==null) return "null";
        if (member instanceof PrimitiveValue) return member.toString();
        if (member instanceof StringReference text) return text.value().replace('\n',' ').replace('\r',' ');
        if (member instanceof ObjectReference object) return object.referenceType().name()+"#"+object.uniqueID();
        return member.type().name();
    }

    private static void describe(String label,Value receiver,String... names) {
        System.out.println(label+"="+value(receiver));
        for (String name:names) System.out.println(label+"."+name+"="+value(field(receiver,name)));
    }

    private static void snapshot(ThreadReference thread) throws Exception {
        Value publication=field(cleanup,"publication");
        Value writer=field(publication,"writerPublication");
        describe("cleanup",cleanup,"phase","producer","ordinal","consumerPrepared","thread");
        describe("publication",publication,"closed","extraAuthorityRevoked","currentStateProducer","currentInvocation","originalSequence");
        describe("writer",writer,"closed","producer","producerOrdinal","leafOrdinal","commit","leaf","selection","repairObservation","playerWriter","fieldSequenceBinding");
        describe("expectation",field(writer,"expectation"),"closed","active","repairGeneration");
        describe("selection",field(writer,"selection"),"revoked","producer","producerOrdinal","commit","leaf","leafOrdinal");
        describe("prepared",field(writer,"fieldPreparation"),"unsupported","sealed","bound","retired");
        describe("session",field(publication,"originalSequence"),"traceRevoked","coverageInstalled","active","next","expectedCursor");
        for (StackFrame frame:thread.frames(0,Math.min(18,thread.frameCount()))) System.out.println("FRAME "+frame.location());
    }

    private static void request(VirtualMachine machine,Method method,long offset,String tag) {
        BreakpointRequest point=machine.eventRequestManager().createBreakpointRequest(method.locationOfCodeIndex(offset));
        point.putProperty("tag",tag);point.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);point.enable();gates.add(point);
    }

    private static void installArm(VirtualMachine machine,ReferenceType type) {
        Method method=type.methodsByName("selectPublishingWriter","(Lnet/farmingtales/ftboss/item/FoodWeaponFinalDamage$PublishingCleanup;)Z").getFirst();
        BreakpointRequest point=machine.eventRequestManager().createBreakpointRequest(method.location());
        point.putProperty("tag","ARM");point.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);point.enable();
    }

    private static void enter(BreakpointEvent event) throws Exception {
        String tag=(String)event.request().getProperty("tag");
        if ("ARM".equals(tag)) {
            if (armed) return;
            Value argument=event.thread().frame(0).getArgumentValues().getFirst();
            Value producer=field(argument,"producer");
            if (!(argument instanceof ObjectReference object) || !(producer instanceof ObjectReference selected)
                || !selected.referenceType().name().equals(FINAL+"$PassiveProducer")) return;
            cleanup=object;ownerThread=event.thread();armed=true;
            VirtualMachine machine=event.virtualMachine();
            for (Site site:SITES) {
                ReferenceType type=machine.classesByName(site.owner()).getFirst();
                Method method=type.methodsByName(site.name(),site.descriptor()).getFirst();
                if (site.name().equals("revokeExtraAuthority")) request(machine,method,0,"REVOKE");
                else {
                    byte[] bytes=method.bytecodes();
                    for (int offset:site.offsets()) {
                        if (offset+1<bytes.length && bytes[offset]==3 && Byte.toUnsignedInt(bytes[offset+1])==172)
                            request(machine,method,offset,"FALSE_RETURN");
                    }
                    if (site.name().equals("selectOwnerWriterLeaf")) request(machine,method,0,"SELECT_ENTRY");
                }
            }
            System.out.println("PASSIVE_WRITER_GATES_ARMED exactOriginalCleanup="+value(cleanup)+" gates="+gates.size());snapshot(ownerThread);System.out.flush();
        } else if (armed && !captured && event.thread().equals(ownerThread)) {
            if ("FALSE_RETURN".equals(tag)) falseReturns++;
            System.out.println(tag+" method="+event.location().method().name()+" descriptor="+event.location().method().signature()+" bci="+event.location().codeIndex()+" line="+event.location().lineNumber());
            if ("SELECT_ENTRY".equals(tag)) System.out.println("SELECT_ARGUMENTS "+event.thread().frame(0).getArgumentValues().stream().map(NativePassiveWriterGateWatch::value).toList());
            snapshot(event.thread());System.out.flush();
            if ("REVOKE".equals(tag) || falseReturns>48) {captured=true;for (BreakpointRequest point:gates) point.disable();}
        }
    }

    public static void main(String[] arguments) throws Exception {
        AttachingConnector connector=Bootstrap.virtualMachineManager().attachingConnectors().stream().filter(item->item.name().equals("com.sun.jdi.SocketAttach")).findFirst().orElseThrow();
        var options=connector.defaultArguments();options.get("hostname").setValue("127.0.0.1");options.get("port").setValue(arguments[0]);
        VirtualMachine machine=null;
        for (int attempt=0;attempt<300 && machine==null;attempt++) {
            try {machine=connector.attach(options);}catch(java.io.IOException unavailable){if(attempt==299)throw unavailable;Thread.sleep(100);}
        }
        try {
            ClassPrepareRequest prepare=machine.eventRequestManager().createClassPrepareRequest();prepare.addClassFilter(FINAL);prepare.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);prepare.enable();
            for (ReferenceType type:machine.classesByName(FINAL)) installArm(machine,type);
            machine.resume();boolean terminal=false;
            while (!terminal) {
                EventSet events=machine.eventQueue().remove();
                try {
                    for (Event event:events) {
                        if (event instanceof ClassPrepareEvent prepared) installArm(machine,prepared.referenceType());
                        if (event instanceof BreakpointEvent entered) enter(entered);
                        if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) terminal=true;
                    }
                } catch (VMDisconnectedException complete) {terminal=true;}
                catch (Exception failure) {errors++;System.out.println("DIAGNOSTIC_INSPECTION_ERROR "+failure);failure.printStackTrace();for(BreakpointRequest point:gates)point.disable();captured=true;}
                finally {try{events.resume();}catch(VMDisconnectedException complete){terminal=true;}}
            }
        } catch (VMDisconnectedException complete) {System.out.println("OWNED_NATIVE_JVM_DISCONNECTED");}
        String report="{\"readOnly\":true,\"methodInvocations\":false,\"authorityWrites\":false,\"armed\":"+armed+",\"captured\":"+captured+",\"falseReturns\":"+falseReturns+",\"diagnosticErrors\":"+errors+"}";
        Files.writeString(Path.of(arguments[1]).resolve("domain-retirement-watch.json"),report);
        Files.writeString(Path.of(arguments[1]).resolve("domain-value-diff.json"),"null");
        Files.writeString(Path.of(arguments[1]).resolve("domain-alias-at-arm.json"),"null");
        System.out.println(report);
        if(errors!=0)System.exit(1);
    }
}
