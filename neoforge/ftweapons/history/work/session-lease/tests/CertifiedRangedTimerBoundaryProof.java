package net.farmingtales.ftboss.item;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.Consumer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class CertifiedRangedTimerBoundaryProof implements Opcodes {
    private static final String PREFIX="net/farmingtales/ftboss/item/";
    private static final List<String> CHANGED=List.of("NativeCapabilityInstallationProof170","NativeExpectedFieldObservation","NativeFreshFactoryTrust298","OwnerFieldCoverageRegistry","OwnerFieldInstallationProof");
    private static final Map<String,String> hashes=new LinkedHashMap<>();
    private static int preserved;
    private static int added;
    private static int registeredCalls;
    private static int negativeCases;
    private static final List<String> cases=new ArrayList<>();

    private static void require(boolean value,String reason) {
        if (!value) throw new AssertionError(reason);
    }

    private static ClassNode parse(byte[] bytes) {
        var node=new ClassNode();
        new ClassReader(bytes).accept(node,0);
        return node;
    }

    private static String sha(byte[] bytes) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    }

    private static MethodNode method(ClassNode node,String name,String descriptor) {
        return node.methods.stream().filter(value->value.name.equals(name) && value.desc.equals(descriptor)).findFirst().orElseThrow();
    }

    private static String canonical(String value) {
        for (var entry:hashes.entrySet()) value=value.replace(entry.getKey(),entry.getValue());
        return value;
    }

    private static String body(MethodNode source,boolean normalizeRegistry) {
        var copy=new MethodNode(source.access,source.name,source.desc,source.signature,source.exceptions.toArray(String[]::new));
        source.accept(copy);
        if (normalizeRegistry) {
            var operations=new ArrayList<AbstractInsnNode>();
            for (var instruction:copy.instructions) if (instruction.getOpcode()>=0) operations.add(instruction);
            for (int index=0;index<operations.size();index++) {
                if (!(operations.get(index) instanceof MethodInsnNode call) || !call.owner.equals(PREFIX+"NativeFreshFactoryTrust298") || !call.name.equals("installRangedTimer")) continue;
                require(call.getOpcode()==INVOKESTATIC && call.desc.equals("(Lorg/spongepowered/asm/mixin/transformer/ext/ITargetClassContext;)V"),"Registry callback target differs");
                require(index>=3 && operations.get(index-3) instanceof VarInsnNode first && first.getOpcode()==ILOAD && first.var==2,"Registry callback lost exact late slot");
                require(operations.get(index-2) instanceof JumpInsnNode branch && branch.getOpcode()==IFNE,"Registry callback not limited to slot zero");
                require(operations.get(index-1) instanceof VarInsnNode argument && argument.getOpcode()==ALOAD && argument.var==0,"Registry callback context differs");
                for (int offset=index-3;offset<=index;offset++) copy.instructions.remove(operations.get(offset));
                registeredCalls++;
            }
        }
        Set<LabelNode> used=Collections.newSetFromMap(new IdentityHashMap<>());
        for (var instruction:copy.instructions) {
            if (instruction instanceof JumpInsnNode jump) used.add(jump.label);
            if (instruction instanceof LookupSwitchInsnNode selection) {used.add(selection.dflt);used.addAll(selection.labels);}
            if (instruction instanceof TableSwitchInsnNode selection) {used.add(selection.dflt);used.addAll(selection.labels);}
        }
        for (var block:copy.tryCatchBlocks) {used.add(block.start);used.add(block.end);used.add(block.handler);}
        for (var instruction:copy.instructions.toArray()) if (instruction instanceof FrameNode || instruction instanceof LineNumberNode || instruction instanceof LabelNode label && !used.contains(label)) copy.instructions.remove(instruction);
        var printer=new Textifier();
        copy.instructions.accept(new TraceMethodVisitor(printer));
        return canonical(printer.getText().toString());
    }

    private static ClassNode cloneHost(ClassNode node) {
        var writer=new ClassWriter(0);node.accept(writer);return parse(writer.toByteArray());
    }

    private static MethodNode original(ClassNode node) {
        return node.methods.stream().filter(value->value.name.startsWith("putLong$mixinextras$wrapped$") && value.desc.equals("(Ljava/lang/String;J)V")).findFirst().orElseThrow();
    }

    private static void denied(ClassNode base,String label,Consumer<ClassNode> mutation) {
        var changed=cloneHost(base);mutation.accept(changed);
        require(OwnerFieldInstallationProof.nativeRangedLongMethod(changed)==null,label);
        negativeCases++;cases.add(label);
    }

    public static void main(String[] arguments) throws Exception {
        Path before=Path.of(arguments[0]),after=Path.of(arguments[1]);
        for (String name:CHANGED) {
            String relative=PREFIX+name+".class";
            hashes.put(sha(Files.readAllBytes(before.resolve(relative))),"@CLASS:"+name);
            hashes.put(sha(Files.readAllBytes(after.resolve(relative))),"@CLASS:"+name);
        }
        int changedFiles=0,total=0;
        try (var files=Files.walk(before)) {
            for (var path:files.filter(value->value.toString().endsWith(".class")).toList()) {
                String relative=before.relativize(path).toString().replace('\\','/');total++;
                if (!Arrays.equals(Files.readAllBytes(path),Files.readAllBytes(after.resolve(relative)))) {
                    require(CHANGED.stream().anyMatch(name->relative.equals(PREFIX+name+".class")),"Unrelated class changed: "+relative);changedFiles++;
                }
            }
        }
        require(total==1381 && changedFiles==5,"Cumulative class set drifted");
        for (String name:CHANGED) {
            var old=parse(Files.readAllBytes(before.resolve(PREFIX+name+".class")));
            var current=parse(Files.readAllBytes(after.resolve(PREFIX+name+".class")));
            require(old.fields.stream().map(value->value.name+value.desc+value.access).toList().equals(current.fields.stream().filter(value->!name.equals("NativeFreshFactoryTrust298") || !value.name.equals("rangedTimerMethod")).map(value->value.name+value.desc+value.access).toList()),"Existing state fields changed");
            for (var prior:old.methods) {
                var next=method(current,prior.name,prior.desc);
                require(prior.access==next.access && Objects.equals(prior.signature,next.signature) && prior.exceptions.equals(next.exceptions),"Method contract changed");
                new Analyzer<>(new BasicVerifier()).analyze(current.name,next);
                if (name.equals("NativeExpectedFieldObservation") && next.name.equals("plannedRangedFieldMutation")) {
                    long count=Arrays.stream(next.instructions.toArray()).filter(value->value instanceof MethodInsnNode call && call.owner.equals(PREFIX+"NativeFreshFactoryTrust298") && call.name.equals("rangedTimerMutationFrame")).count();
                    require(count==1,"Exactly one authenticated frame lookup expected");continue;
                }
                require(body(prior,false).equals(body(next,name.equals("OwnerFieldCoverageRegistry") && next.name.equals("lateApplied"))),"Existing method changed: "+name+"."+next.name);
                preserved++;
            }
            for (var next:current.methods) if (old.methods.stream().noneMatch(value->value.name.equals(next.name) && value.desc.equals(next.desc))) {
                require(name.equals("OwnerFieldInstallationProof") && next.name.equals("nativeRangedLongMethod") || name.equals("NativeFreshFactoryTrust298") && (List.of("installRangedTimer","rangedTimerMutationFrame").contains(next.name) || next.name.startsWith("lambda$installRangedTimer$")),"Unexpected new method: "+next.name);
                new Analyzer<>(new BasicVerifier()).analyze(current.name,next);added++;
            }
        }
        String oldSource=Files.readString(Path.of(arguments[3])),newSource=Files.readString(Path.of(arguments[4]));
        String oldLine="            || !frames.get(2).getMethodName().equals(timer ? \"putLong\" : \"put\")";
        String newLine="            || !(timer ? NativeFreshFactoryTrust298.rangedTimerMutationFrame(frames.get(2)) : frames.get(2).getMethodName().equals(\"put\"))";
        require(oldSource.contains(oldLine) && oldSource.replace(oldLine,newLine).equals(newSource),"Observation guard changed beyond exact certified alias predicate");
        var host=parse(Files.readAllBytes(Path.of(arguments[2])));
        String actual=original(host).name;
        require(actual.equals(OwnerFieldInstallationProof.nativeRangedLongMethod(host)),"Actual transformed chain not recognized");
        var renamed=cloneHost(host);original(renamed).name="putLong$mixinextras$wrapped$91001";
        for (var entry:renamed.methods) for (var instruction:entry.instructions) if (instruction instanceof MethodInsnNode call && call.owner.equals(renamed.name) && call.name.equals(actual)) call.name="putLong$mixinextras$wrapped$91001";
        require("putLong$mixinextras$wrapped$91001".equals(OwnerFieldInstallationProof.nativeRangedLongMethod(renamed)),"Ordinal must derive from the proven actual bridge");
        denied(host,"ForeignHostDenied",value->value.name="foreign/CompoundTag");
        denied(host,"MissingPublicEntryDenied",value->method(value,"putLong","(Ljava/lang/String;J)V").name="foreignEntry");
        denied(host,"WrongPrivateAccessDenied",value->original(value).access=ACC_PUBLIC);
        denied(host,"UnboundMatchingNameDenied",value->original(value).name="putLong$mixinextras$wrapped$91002");
        denied(host,"DuplicateWrappedOriginalDenied",value->{var source=original(value);var copy=new MethodNode(source.access,source.name,source.desc,source.signature,source.exceptions.toArray(String[]::new));source.accept(copy);copy.name="putLong$mixinextras$wrapped$91002";value.methods.add(copy);});
        denied(host,"AlteredPublicWrapperDenied",value->{for(var instruction:method(value,"putLong","(Ljava/lang/String;J)V").instructions) if(instruction instanceof MethodInsnNode call)call.owner="foreign/Owner";});
        denied(host,"AlteredPrivateBridgeDenied",value->{for(var entry:value.methods)for(var instruction:entry.instructions)if(instruction instanceof MethodInsnNode call && call.name.equals(actual))call.name="foreignOriginal";});
        denied(host,"MissingActualHeaderDenied",value->{var entry=original(value);var instructions=Arrays.stream(entry.instructions.toArray()).filter(item->item.getOpcode()>=0).toList();for(int index=0;index<3;index++)entry.instructions.remove(instructions.get(index));});
        denied(host,"AlteredWrapperDelegateDenied",value->{for(var entry:value.methods)if(entry.name.endsWith("$ftboss$nativeFxCooldown"))for(var instruction:entry.instructions)if(instruction instanceof MethodInsnNode call)call.owner="foreign/Authority";});
        require(registeredCalls==1 && added==5,"Unexpected registration or helper count");
        System.out.println("failures=0;existingMethodsPreserved="+preserved+";addedMethods="+added+";exactRegistryCallback=1;changedClasses=5;actualWrappedChainVerified=true;dynamicOrdinalVerified=true;negativeChainCases="+negativeCases+";allOtherClassesIdentical=true;originalObservationScopeAndValueGatesPreserved=true");
        System.out.println("negativeCases="+cases);
    }
}
