package net.farmingtales.ftboss.item;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class NativeMapFactoryProof287 implements Opcodes {
    private static final String HOST = "net/minecraft/nbt/CompoundTag";
    private static final String SOURCE = "net/farmingtales/ftboss/mixin/NativeCompoundTagMutationVersionMixin";
    private static final String MAP_CTOR = "(Ljava/util/Map;)V";
    private static final String MAP_HANDLER = "ftboss$observeConstructorMap280";
    private static final String MERGED = "Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;";

    private NativeMapFactoryProof287() { }

    private static String sha(byte[] bytes) throws Exception { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)); }

    private static ClassNode parse(byte[] bytes) { ClassNode node = new ClassNode(); new ClassReader(bytes).accept(node, 0); return node; }

    private static Object value(AnnotationNode annotation, String key) {
        if (annotation == null || annotation.values == null) return null;
        for (int index = 0; index < annotation.values.size(); index += 2) if (annotation.values.get(index).equals(key)) return annotation.values.get(index + 1);
        return null;
    }

    private static AnnotationNode annotation(MethodNode method, String descriptor) {
        List<AnnotationNode> found = new ArrayList<>();
        if (method.visibleAnnotations != null) for (AnnotationNode item : method.visibleAnnotations) if (item.desc.equals(descriptor)) found.add(item);
        if (method.invisibleAnnotations != null) for (AnnotationNode item : method.invisibleAnnotations) if (item.desc.equals(descriptor)) found.add(item);
        return found.size() == 1 ? found.getFirst() : null;
    }

    private static MethodNode method(ClassNode node, String name, String descriptor) {
        List<MethodNode> found = node.methods.stream().filter(item -> item.name.equals(name) && item.desc.equals(descriptor)).toList();
        return found.size() == 1 ? found.getFirst() : null;
    }

    private static List<AbstractInsnNode> executable(MethodNode method) {
        List<AbstractInsnNode> result = new ArrayList<>();
        for (AbstractInsnNode instruction : method.instructions) if (instruction.getOpcode() >= 0) result.add(instruction);
        return result;
    }

    private static Handle normalized(Handle handle, Map<String, String> methods) {
        if (!handle.getOwner().equals(HOST)) return handle;
        String name = methods.get(handle.getName() + handle.getDesc());
        return name == null ? handle : new Handle(handle.getTag(), SOURCE, name, handle.getDesc(), handle.isInterface());
    }

    private static String code(MethodNode original, boolean normalize, Map<String, String> methods) {
        MethodNode clone = new MethodNode(); original.accept(clone);
        clone.visibleAnnotations = null; clone.invisibleAnnotations = null;
        clone.visibleTypeAnnotations = null; clone.invisibleTypeAnnotations = null;
        clone.localVariables = null; clone.visibleLocalVariableAnnotations = null; clone.invisibleLocalVariableAnnotations = null;
        List<AbstractInsnNode> remove = new ArrayList<>();
        for (AbstractInsnNode instruction : clone.instructions) {
            if (instruction.getType() == AbstractInsnNode.FRAME || instruction.getType() == AbstractInsnNode.LINE) remove.add(instruction);
            if (!normalize) continue;
            if (instruction instanceof FieldInsnNode field && field.owner.equals(HOST)) field.owner = SOURCE;
            if (instruction instanceof MethodInsnNode call && call.owner.equals(HOST)) {
                String name = methods.get(call.name + call.desc);
                if (name != null) { call.owner = SOURCE; call.name = name; }
            }
            if (instruction instanceof InvokeDynamicInsnNode dynamic) {
                dynamic.bsmArgs = dynamic.bsmArgs.clone();
                dynamic.bsm = normalized(dynamic.bsm, methods);
                for (int index = 0; index < dynamic.bsmArgs.length; index++) if (dynamic.bsmArgs[index] instanceof Handle handle) dynamic.bsmArgs[index] = normalized(handle, methods);
                dynamic.desc = dynamic.desc.replace("L" + HOST + ";", "L" + SOURCE + ";");
            }
        }
        for (AbstractInsnNode instruction : remove) clone.instructions.remove(instruction);
        clone.maxStack = 0; clone.maxLocals = 0;
        Textifier printer = new Textifier(); clone.accept(new TraceMethodVisitor(printer));
        StringWriter result = new StringWriter(); printer.print(new PrintWriter(result)); return result.toString();
    }

    private static boolean variable(AbstractInsnNode instruction, int opcode, int index) {
        return instruction instanceof VarInsnNode variable && variable.getOpcode() == opcode && variable.var == index;
    }

    private static boolean call(AbstractInsnNode instruction, int opcode, String owner, String name, String descriptor) {
        return instruction instanceof MethodInsnNode method && method.getOpcode() == opcode && method.owner.equals(owner) && method.name.equals(name) && method.desc.equals(descriptor) && !method.itf;
    }

    private static void require(boolean condition, String message) { if (!condition) throw new IllegalArgumentException(message); }

    public static Map<String, Object> verify(byte[] rawBytes, byte[] loaderBytes, byte[] sourceBytes, ClassNode actual) throws Exception {
        require(sha(rawBytes).equals("93a9643a11aa61b666871cadeb14be32ff4f813bf97b05951bae389bada15fe6"), "Actual SDK CompoundTag source differs");
        require(sha(loaderBytes).equals("f05fa08db790436311050d87471954ab47cf94fde75a936aa73d833c7955f4ea"), "Actual SDK loader source differs");
        require(sha(sourceBytes).equals("278c4c7fff0a5a8cd815176f16d3a361672e6e7f204af22d873b731d05a850d4"), "Actual version bridge source differs");
        require(actual != null && actual.name.equals(HOST) && actual.superName.equals("java/lang/Object"), "Actual merged host differs");
        ClassNode raw = parse(rawBytes), source = parse(sourceBytes);
        Map<String, String> mapped = new LinkedHashMap<>();
        String session = null;
        for (MethodNode original : source.methods) {
            if (original.name.equals("<init>")) continue;
            List<MethodNode> found = actual.methods.stream().filter(item -> item.desc.equals(original.desc) && (item.name.equals(original.name) || item.name.endsWith("$" + original.name))).toList();
            require(found.size() == 1, "Missing or duplicated merged source method " + original.name);
            MethodNode merged = found.getFirst(); AnnotationNode metadata = annotation(merged, MERGED);
            require(metadata != null && SOURCE.replace('/', '.').equals(value(metadata, "mixin")) && Integer.valueOf(1000).equals(value(metadata, "priority")), "Merged method source origin differs " + original.name);
            Object id = value(metadata, "sessionId"); require(id instanceof String && !((String)id).isBlank(), "Missing merged source session");
            if (session == null) session = (String)id; else require(session.equals(id), "Mixed Sponge sessions");
            require(merged.access == original.access, "Merged source access differs " + original.name);
            mapped.put(merged.name + merged.desc, original.name);
        }
        for (MethodNode original : source.methods) {
            if (original.name.equals("<init>")) continue;
            MethodNode merged = actual.methods.stream().filter(item -> original.name.equals(mapped.get(item.name + item.desc))).findFirst().orElseThrow();
            require(code(original, false, Map.of()).equals(code(merged, true, mapped)), "Whole merged source executable body differs " + original.name);
        }
        List<org.objectweb.asm.tree.FieldNode> backing = actual.fields.stream().filter(field -> field.name.equals("tags")).toList();
        require(backing.size() == 1 && backing.getFirst().desc.equals("Ljava/util/Map;") && backing.getFirst().access == (ACC_PUBLIC | ACC_FINAL), "Actual KJS backing field contract differs");
        require(actual.methods.stream().filter(item -> item.name.equals("<init>")).map(item -> item.desc).sorted().toList().equals(List.of("()V", "(I)V", "(Ljava/util/Map;)V")), "Unexpected constructor overload");
        int writes = 0;
        for (MethodNode candidate : actual.methods) for (AbstractInsnNode instruction : candidate.instructions) {
            if (instruction instanceof FieldInsnNode field && field.getOpcode() == PUTFIELD && field.owner.equals(HOST) && field.name.equals("tags")) {
                require(candidate.name.equals("<init>") && candidate.desc.equals(MAP_CTOR), "Backing map reassigned outside sole constructor");
                writes++;
            }
        }
        require(writes == 1, "Backing field assignment count differs");
        MethodNode constructor = method(actual, "<init>", MAP_CTOR);
        require(constructor != null && constructor.access == ACC_PROTECTED && constructor.tryCatchBlocks.isEmpty(), "Protected constructor shape differs");
        List<AbstractInsnNode> instructions = executable(constructor);
        require(instructions.size() == 10, "Protected constructor executable count differs");
        require(variable(instructions.get(0), ALOAD, 0) && call(instructions.get(1), INVOKESPECIAL, "java/lang/Object", "<init>", "()V"), "Constructor Object initialization differs");
        require(variable(instructions.get(2), ALOAD, 0) && variable(instructions.get(3), ALOAD, 1), "Constructor observer receiver or input differs");
        require(instructions.get(4) instanceof MethodInsnNode handler && handler.getOpcode() == INVOKESPECIAL && handler.owner.equals(HOST) && MAP_HANDLER.equals(mapped.get(handler.name + handler.desc)), "Constructor observer target differs");
        require(variable(instructions.get(5), ASTORE, 1) && variable(instructions.get(6), ALOAD, 0) && variable(instructions.get(7), ALOAD, 1), "Constructor observed map assignment differs");
        require(instructions.get(8) instanceof FieldInsnNode field && field.getOpcode() == PUTFIELD && field.owner.equals(HOST) && field.name.equals("tags") && field.desc.equals("Ljava/util/Map;"), "Constructor final tags assignment differs");
        require(instructions.get(9).getOpcode() == RETURN, "Constructor return differs");
        List<Map<String, Object>> factories = new ArrayList<>();
        for (String selector : List.of("<init>()V", "<init>(I)V", "shallowCopy()Lnet/minecraft/nbt/CompoundTag;", "copy()Lnet/minecraft/nbt/CompoundTag;")) {
            int bracket = selector.indexOf('('); String name = selector.substring(0, bracket), descriptor = selector.substring(bracket);
            MethodNode baseline = method(raw, name, descriptor);
            List<MethodNode> candidates = actual.methods.stream().filter(item -> item.desc.equals(descriptor) && (name.equals("copy") ? item.name.startsWith("copy$mixinextras$wrapped$") : item.name.equals(name))).toList();
            require(candidates.size() == 1, "Native factory missing or duplicated " + selector);
            MethodNode candidate = candidates.getFirst();
            require(candidate.access == (name.equals("copy") ? ACC_PRIVATE : baseline.access), "Native factory access differs " + selector);
            require(code(baseline, false, Map.of()).equals(code(candidate, false, Map.of())), "Whole native fresh-map factory differs " + selector);
            require(candidate.tryCatchBlocks.isEmpty(), "Unexpected factory exception route");
            long calls = executable(candidate).stream().filter(instruction -> call(instruction, INVOKESPECIAL, HOST, "<init>", MAP_CTOR)).count();
            require(calls == 1, "Native factory constructor site count differs");
            factories.add(Map.of("baselineSelector", selector, "actualName", candidate.name, "actualDescriptor", candidate.desc, "wholeExecutableBodyAndExceptionTableExact", true, "actualProtectedMapConstructorSites", calls));
        }
        Map<String, Object> result = new LinkedHashMap<>(); result.put("failures", 0); result.put("actualFreshFactoryMethods", List.copyOf(factories));
        result.put("allVersionBridgeSourceMethodsExactIncludingExceptionRoutes", mapped.size()); result.put("sameSpongeSession", session);
        result.put("loaderRawSourcePinnedButRuntimeLoaderClassNotCertified", true); result.put("runtimeCallSiteOrInstallationAuthorityGranted", false);
        result.put("unknownOrderedMapInputAccepted", false); result.put("installable", false); return Map.copyOf(result);
    }
}
