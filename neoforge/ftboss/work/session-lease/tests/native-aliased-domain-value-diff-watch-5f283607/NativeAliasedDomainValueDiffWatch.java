import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class NativeAliasedDomainValueDiffWatch {
    private static final String TARGET="net.farmingtales.ftboss.item.NativeExpectedFieldObservation";
    private static ObjectReference domain;
    private static ModificationWatchpointRequest watch;
    private static boolean captured;
    private static int lookupReturns;
    private static String differenceReport="null";
    private static String aliasReport="null";
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
        var carrier=field(prepared,"carrier");var actualRoot=field(domain,"originalPersistent");
        var carrierRoot=carrier instanceof ObjectReference object?field(object,"originalPersistent"):null;
        var builder=(ObjectReference)field(prepared,"builder");var planned=(ObjectReference)field(builder,"expected");
        aliasReport="{\"readOnly\":true,\"methodInvocations\":false,\"authorityWrites\":false,\"sameDomainBefore\":"+domain.equals(carrier)+",\"sameNonNullPersistentRoot\":"+(actualRoot!=null && actualRoot.equals(carrierRoot))+",\"plannedAliasFact\":"+value(field(planned,"playerCarrierAliased"))+"}";
        System.out.println("EXACT_DOMAIN_ALIAS_AT_ARM "+aliasReport);System.out.flush();
        if (!(field(domain,"retired") instanceof BooleanValue retired) || retired.value()) throw new IllegalStateException("Actual domain already retired at bound original ranged-save entry");
        var machine=thread.virtualMachine();
        if (!machine.canWatchFieldModification() || !machine.canUseInstanceFilters()) throw new IllegalStateException("Exact instance modification watchpoints unavailable");
        watch=machine.eventRequestManager().createModificationWatchpointRequest(domain.referenceType().fieldByName("retired"));
        watch.addInstanceFilter(domain);watch.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);watch.enable();
        event.request().disable();
        System.out.println("ARMED_EXACT_NONRETIRED_DOMAIN originalSession="+value(session)+" domain="+value(domain)+" thread="+thread.uniqueID());
        describe("domainAtArm",domain,"retired","originated","loading","originalPersistent","expected");
        System.out.flush();
    }

    private static void retirement(ModificationWatchpointEvent event) throws Exception {
        System.out.println("EXACT_DOMAIN_MODIFICATION object="+value(event.object())+" old="+value(event.valueCurrent())+" new="+value(event.valueToBe()));
        if (!event.object().equals(domain) || !(event.valueCurrent() instanceof BooleanValue old) || old.value()
            || !(event.valueToBe() instanceof BooleanValue next) || !next.value()) return;
        captured=true;watch.disable();
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
        captureDifference(event.thread(),scope);
        var frames=event.thread().frames();
        for (int index=0;index<Math.min(frames.size(),22);index++) {
            var frame=frames.get(index).location();
            System.out.println("STACK "+index+" "+frame.declaringType().name()+"."+frame.method().name()+frame.method().signature()+" bci="+frame.codeIndex()+" line="+frame.lineNumber());
        }
        try {System.out.println("RETIRE_METHOD_ARGUMENTS "+event.thread().frame(0).getArgumentValues().stream().map(NativeAliasedDomainValueDiffWatch::value).toList());}
        catch (RuntimeException unavailable) {System.out.println("OPTIONAL_ARGUMENT_READ_UNAVAILABLE "+unavailable);}
        System.out.flush();
    }


    private record Node(int kind,long bits,String text,List<String> keys,List<Node> children,List<Long> elements) {}

    private static final class TreeReader {
        private int nodes;
        private final Set<Long> active=new HashSet<>();

        private static ObjectReference object(Value value) {
            if (!(value instanceof ObjectReference object)) throw new IllegalArgumentException("Expected nonnull object");
            return object;
        }

        private static long integer(Value value) {
            if (!(value instanceof PrimitiveValue number)) throw new IllegalArgumentException("Expected primitive");
            return number.longValue();
        }

        private static List<Value> array(Value value) {
            if (value==null) return null;
            if (!(value instanceof ArrayReference array) || array.length()>1048576) throw new IllegalArgumentException("Unsupported array");
            return array.getValues();
        }

        private void enter(ObjectReference value,int depth) {
            if (depth>64 || ++nodes>65536 || !active.add(value.uniqueID())) throw new IllegalArgumentException("Tree over budget or cyclic");
        }

        Node expected(ObjectReference value,int depth) {
            if (value==null) return null;
            if (!value.referenceType().name().equals("net.farmingtales.ftboss.item.FoodWeaponExpectedTrace$Value")) throw new IllegalArgumentException("Unexpected immutable Value class");
            enter(value,depth);
            try {
                List<String> keys=null;List<Node> children=null;List<Long> elements=null;
                var keyValues=array(field(value,"keys"));
                if (keyValues!=null) {keys=new ArrayList<>();for(var key:keyValues) keys.add(((StringReference)key).value());}
                var childValues=array(field(value,"children"));
                if (childValues!=null) {children=new ArrayList<>();for(var child:childValues) children.add(expected(object(child),depth+1));}
                var elementValues=array(field(value,"elements"));
                if (elementValues!=null) {elements=new ArrayList<>();for(var element:elementValues) elements.add(integer(element));}
                var text=field(value,"string");
                return new Node((int)integer(field(value,"kind")),integer(field(value,"bits")),text==null?null:((StringReference)text).value(),keys,children,elements);
            } finally {active.remove(value.uniqueID());}
        }

        private static SortedMap<String,ObjectReference> rawMap(ObjectReference map) {
            if (map.referenceType().name().equals("net.farmingtales.ftboss.item.NativeMutationMap276")) map=object(field(map,"delegate"));
            if (!Set.of("java.util.HashMap","java.util.LinkedHashMap").contains(map.referenceType().name())) throw new IllegalArgumentException("Unsupported native map: "+map.referenceType().name());
            var result=new TreeMap<String,ObjectReference>();var table=array(field(map,"table"));var seen=new HashSet<Long>();
            if (table!=null) for(var entry:table) {
                while(entry instanceof ObjectReference current) {
                    if (!seen.add(current.uniqueID()) || seen.size()>65536) throw new IllegalArgumentException("Native map cycle or budget");
                    var key=field(current,"key");
                    if (!(key instanceof StringReference text) || result.put(text.value(),object(field(current,"value")))!=null) throw new IllegalArgumentException("Native compound key");
                    entry=field(current,"next");
                }
            }
            if (result.size()!=integer(field(map,"size"))) throw new IllegalArgumentException("Incomplete native map read");
            return result;
        }

        Node raw(ObjectReference value,int depth) {
            if(value==null) return new Node(10,0,null,List.of(),List.of(),null);
            enter(value,depth);
            try {
                String type=value.referenceType().name();
                if (!type.startsWith("net.minecraft.nbt.")) throw new IllegalArgumentException("Unsupported native tag class: "+type);
                String name=type.substring("net.minecraft.nbt.".length());
                if (name.equals("CompoundTag")) {
                    var fields=rawMap(object(field(value,"tags")));var children=new ArrayList<Node>();
                    for(var child:fields.values()) children.add(raw(child,depth+1));
                    return new Node(10,0,null,new ArrayList<>(fields.keySet()),children,null);
                }
                if (name.equals("ListTag")) {
                    var list=object(field(value,"list"));
                    if(!list.referenceType().name().equals("java.util.ArrayList")) throw new IllegalArgumentException("Unsupported native list");
                    int size=(int)integer(field(list,"size"));var storage=(ArrayReference)field(list,"elementData");
                    if(size<0 || size>65536 || size>storage.length()) throw new IllegalArgumentException("Native list budget");
                    var children=new ArrayList<Node>();for(var child:storage.getValues(0,size)) children.add(raw(object(child),depth+1));
                    return new Node(9,integer(field(value,"type")),null,null,children,null);
                }
                if(name.equals("EndTag")) return new Node(0,0,null,null,null,null);
                var data=field(value,"data");
                if(name.equals("StringTag")) return new Node(8,0,((StringReference)data).value(),null,null,null);
                int kind=switch(name) {case "ByteTag"->1;case "ShortTag"->2;case "IntTag"->3;case "LongTag"->4;case "FloatTag"->5;case "DoubleTag"->6;case "ByteArrayTag"->7;case "IntArrayTag"->11;case "LongArrayTag"->12;default->throw new IllegalArgumentException("Unsupported native tag: "+name);};
                if(kind==7 || kind==11 || kind==12) {
                    var elements=new ArrayList<Long>();for(var element:array(data)) elements.add(integer(element));
                    return new Node(kind,0,null,null,null,elements);
                }
                long bits=kind==5?(Float.floatToRawIntBits(((FloatValue)data).value())&0xffffffffL):kind==6?Double.doubleToRawLongBits(((DoubleValue)data).value()):integer(data);
                return new Node(kind,bits,null,null,null,null);
            } finally {active.remove(value.uniqueID());}
        }
    }

    private static String json(String text) {
        var result=new StringBuilder("\"");
        for(int index=0;index<text.length();index++) {
            char next=text.charAt(index);
            if(next=='"' || next=='\\') result.append('\\').append(next);
            else if(next<32) result.append(String.format("\\u%04x",(int)next));
            else result.append(next);
        }
        return result.append('"').toString();
    }

    private static String summary(Node node) throws Exception {
        if(node==null) return "null";
        var digest=java.security.MessageDigest.getInstance("SHA-256").digest(node.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return "{\"kind\":"+node.kind()+",\"childCount\":"+(node.children()==null?0:node.children().size())+",\"textLength\":"+(node.text()==null?-1:node.text().length())+",\"arrayLength\":"+(node.elements()==null?-1:node.elements().size())+",\"sha256\":"+json(HexFormat.of().formatHex(digest))+"}";
    }

    private static void diff(String path,Node expected,Node actual,List<String> differences) throws Exception {
        if(Objects.equals(expected,actual) || differences.size()>=128) return;
        if(expected!=null && actual!=null && expected.kind()==10 && actual.kind()==10) {
            var keys=new TreeSet<String>();keys.addAll(expected.keys());keys.addAll(actual.keys());
            for(String key:keys) {
                int left=expected.keys().indexOf(key),right=actual.keys().indexOf(key);
                diff(path+"/"+key.replace("~","~0").replace("/","~1"),left<0?null:expected.children().get(left),right<0?null:actual.children().get(right),differences);
            }
            return;
        }
        if(expected!=null && actual!=null && expected.kind()==9 && actual.kind()==9 && expected.bits()==actual.bits()) {
            for(int index=0;index<Math.max(expected.children().size(),actual.children().size());index++) diff(path+"/"+index,index>=expected.children().size()?null:expected.children().get(index),index>=actual.children().size()?null:actual.children().get(index),differences);
            return;
        }
        differences.add("{\"path\":"+json(path)+",\"expected\":"+summary(expected)+",\"actual\":"+summary(actual)+"}");
    }

    private static Node expectedTree(Value value) {return value==null?null:new TreeReader().expected((ObjectReference)value,0);}
    private static Node actualTree(Value value) {return new TreeReader().raw((ObjectReference)value,0);}

    private static void captureDifference(ThreadReference thread,ObjectReference scope) throws Exception {
        var arguments=thread.frame(0).getArgumentValues();
        if(arguments.size()!=2 || !(arguments.get(0) instanceof ObjectReference session) || !(arguments.get(1) instanceof ObjectReference step)) throw new IllegalArgumentException("Expected original advance Session/Step");
        var operation=(ObjectReference)field(step,"expected");var before=(ObjectReference)field(operation,"before");var after=(ObjectReference)field(operation,"after");
        var lineage=(ObjectReference)field(session,"lineage");var issuer=(ObjectReference)field(lineage,"issuer");var prepared=(ObjectReference)field(issuer,"fieldPreparation");var carrier=(ObjectReference)field(prepared,"carrier");
        var beforePlayer=expectedTree(field(before,"player"));var afterPlayer=expectedTree(field(after,"player"));var actualPlayer=actualTree(field(domain,"originalPersistent"));
        var beforeCarrier=expectedTree(field(before,"carrier"));var afterCarrier=expectedTree(field(after,"carrier"));var actualCarrier=actualTree(field(carrier,"originalPersistent"));
        var differences=new ArrayList<String>();diff("after/player",afterPlayer,actualPlayer,differences);diff("after/carrier",afterCarrier,actualCarrier,differences);
        var planned=new ArrayList<String>();diff("planned/player",beforePlayer,afterPlayer,planned);diff("planned/carrier",beforeCarrier,afterCarrier,planned);
        differenceReport="{\"readOnly\":true,\"methodInvocations\":false,\"authorityWrites\":false,\"capturedBeforeRetirementAssignment\":true,\"actualRootOperation\":"+json(value(field(operation,"kind")))+",\"expectedBeforePlayer\":"+summary(beforePlayer)+",\"expectedAfterPlayer\":"+summary(afterPlayer)+",\"actualPlayer\":"+summary(actualPlayer)+",\"expectedAfterCarrier\":"+summary(afterCarrier)+",\"actualCarrier\":"+summary(actualCarrier)+",\"differences\":["+String.join(",",differences)+"],\"plannedChanges\":["+String.join(",",planned)+"]}";
        System.out.println("EXACT_AFTER_IMAGE_DIFFERENCES "+differenceReport);System.out.flush();
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
                        if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) terminal=true;
                    }
                } catch (VMDisconnectedException complete) {terminal=true;}
                catch (Exception inspectionFailure) {errors++;System.out.println("DIAGNOSTIC_INSPECTION_ERROR "+inspectionFailure);System.out.flush();}
                finally {try {events.resume();}catch(VMDisconnectedException complete){terminal=true;}}
            }
        } catch (VMDisconnectedException complete) {System.out.println("OWNED_NATIVE_JVM_DISCONNECTED");}
        String report="{\"readOnly\":true,\"methodInvocations\":false,\"authorityWrites\":false,\"armed\":"+(domain!=null)+",\"firstExactRetirementCaptured\":"+captured+",\"certifiedFrameLookupReturns\":"+lookupReturns+",\"diagnosticErrors\":"+errors+"}";
        Files.writeString(Path.of(arguments[1]).resolve("domain-alias-at-arm.json"),aliasReport);
        Files.writeString(Path.of(arguments[1]).resolve("domain-value-diff.json"),differenceReport);
        Files.writeString(Path.of(arguments[1]).resolve("domain-retirement-watch.json"),report);
        System.out.println(report);
        if (errors!=0) System.exit(1);
    }
}
