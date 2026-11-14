package net.farmingtales.ftboss.item;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.farmingtales.ftboss.boss.ai.NativeBossLivingComposerSupportV2;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class NativeComponentInstallationProof163 implements Opcodes {
    private static final String PREFIX = "net/farmingtales/ftboss/mixin/";
    private static final String CALLBACK = "org/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable";
    private static final List<String> HOSTS = List.of("net/minecraft/world/item/ItemStack", "net/minecraft/core/component/PatchedDataComponentMap", "net/minecraft/world/item/component/CustomData");
    private NativeComponentInstallationProof163() { }
    static int host(ClassNode target) { return target == null ? -1 : HOSTS.indexOf(target.name); }
    static List<String> requiredSources(ClassNode target) {
        return switch (host(target)) {
            case 0 -> List.of("OwnerFullRepairItemStackMixin", "OwnerRevocationNodeMixin");
            case 1 -> List.of("OwnerFullRepairComponentStorageMixin", "NativeItemComponentStorageFieldsMixin", "OwnerRevocationNodeMixin");
            case 2 -> List.of("OwnerFullRepairCustomDataMixin", "OwnerRevocationNodeMixin");
            default -> List.of();
        };
    }
    static List<String> parents(ClassNode target) {
        return switch (host(target)) {
            case 0 -> List.of("getComponents()Lnet/minecraft/core/component/DataComponentMap;", "getComponentsPatch()Lnet/minecraft/core/component/DataComponentPatch;");
            case 1 -> List.of("set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;", "remove(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;", "applyPatch(Lnet/minecraft/core/component/DataComponentPatch;)V", "applyPatch(Lnet/minecraft/core/component/DataComponentType;Ljava/util/Optional;)V", "restorePatch(Lnet/minecraft/core/component/DataComponentPatch;)V", "setAll(Lnet/minecraft/core/component/DataComponentMap;)V");
            case 2 -> List.of("getUnsafe()Lnet/minecraft/nbt/CompoundTag;");
            default -> List.of();
        };
    }
    static Map<String, String> snapshot(ClassNode target) {
        require(host(target) >= 0, "Exact native component target required"); Map<String, String> result = new LinkedHashMap<>();
        for (String key : parents(target)) { MethodNode parent = method(target, key); require((parent.access & (ACC_STATIC | ACC_NATIVE | ACC_ABSTRACT)) == 0, "Original instance native parent required"); result.put(key, parent.maxLocals + ":" + shape(parent)); }
        return Map.copyOf(result);
    }
    private static ClassNode authenticSource(String name) {
        try (var input = NativeComponentInstallationProof163.class.getResourceAsStream("/" + PREFIX + name + ".class")) {
            require(input != null, "Compiled authentic Mixin source required"); ClassNode result = new ClassNode(); new ClassReader(input).accept(result, 0); return result;
        } catch (java.io.IOException failure) { throw new IllegalStateException("Cannot read authentic component Mixin source", failure); }
    }
    private static void require(boolean condition, String reason) { if (!condition) throw new IllegalStateException(reason); }
    private static ClassNode read(Path base, String name) throws Exception { ClassNode result = new ClassNode(); new ClassReader(Files.readAllBytes(base.resolve(name + ".class"))).accept(result, 0); return result; }
    private static ClassNode copy(ClassNode source) { ClassNode result = new ClassNode(); source.accept(result); for (MethodNode method : result.methods) for (AbstractInsnNode instruction : method.instructions) if (instruction instanceof InvokeDynamicInsnNode dynamic) dynamic.bsmArgs = dynamic.bsmArgs.clone(); return result; }
    private static byte[] bytes(ClassNode owner) { ClassWriter writer = new ClassWriter(0); owner.accept(writer); return writer.toByteArray(); }
    private static MethodNode method(ClassNode owner, String key) { List<MethodNode> found = owner.methods.stream().filter(value -> (value.name + value.desc).equals(key)).toList(); require(found.size() == 1, "Unique parent " + key); return found.getFirst(); }
    private static MethodNode handler(ClassNode owner, String suffix) { List<MethodNode> found = owner.methods.stream().filter(value -> value.name.equals(suffix) || value.name.endsWith("$" + suffix)).toList(); require(found.size() == 1, "Unique handler " + suffix); return found.getFirst(); }
    private static List<AbstractInsnNode> code(MethodNode method) { return Arrays.stream(method.instructions.toArray()).filter(value -> value.getOpcode() >= 0).toList(); }
    private static Object value(AnnotationNode annotation, String key) { require(annotation != null && annotation.values != null, "Annotation required " + key); int index = annotation.values.indexOf(key); require(index >= 0 && index % 2 == 0, "Annotation key " + key); return annotation.values.get(index + 1); }
    private static void value(AnnotationNode annotation, String key, Object replacement) { int index = annotation.values.indexOf(key); require(index >= 0, "Existing annotation key " + key); annotation.values.set(index + 1, replacement); }
    private static AnnotationNode annotation(MethodNode method, String descriptor) { List<AnnotationNode> all = new ArrayList<>(); if (method.visibleAnnotations != null) all.addAll(method.visibleAnnotations); if (method.invisibleAnnotations != null) all.addAll(method.invisibleAnnotations); List<AnnotationNode> found = all.stream().filter(item -> item.desc.endsWith("/" + descriptor + ";")).toList(); require(found.size() == 1, "Exact annotation " + descriptor); return found.getFirst(); }
    private static String shape(MethodNode source) { MethodNode result = NativeBossLivingComposerSupportV2.copy(source); result.visibleAnnotations = null; result.invisibleAnnotations = null; result.visibleTypeAnnotations = null; result.invisibleTypeAnnotations = null; return NativeBossLivingComposerSupportV2.methodFingerprint(result); }
    private static void variable(AbstractInsnNode instruction, int opcode, int local) { require(instruction instanceof VarInsnNode variable && variable.getOpcode() == opcode && variable.var == local, "Exact variable " + opcode + ":" + local); }
    private static void remove(MethodNode parent, List<AbstractInsnNode> instructions, int start, int end) { for (int index = start; index < end; index++) parent.instructions.remove(instructions.get(index)); }
    private static void stripHead(ClassNode host, MethodNode parent, String suffix) { MethodNode callback = handler(host, suffix); List<AbstractInsnNode> instructions = code(parent); List<Integer> positions = new ArrayList<>(); for (int index = 0; index < instructions.size(); index++) if (instructions.get(index) instanceof MethodInsnNode call && call.owner.equals(host.name) && call.name.equals(callback.name)) positions.add(index); require(positions.size() == 1, "One complete HEAD hook " + suffix); int position = positions.getFirst(); require(position >= 2, "HEAD receiver"); variable(instructions.get(position - 2), ALOAD, 0); require(instructions.get(position - 1).getOpcode() == ACONST_NULL, "HEAD null noncancellable callback"); MethodInsnNode call = (MethodInsnNode)instructions.get(position); require(call.getOpcode() == INVOKESPECIAL && call.desc.equals(callback.desc) && !call.itf, "Exact HEAD call"); remove(parent, instructions, position - 2, position + 1); }
    private static void stripAlias(ClassNode host, MethodNode parent, String suffix, int local) {
        MethodNode callback = handler(host, suffix); List<AbstractInsnNode> instructions = code(parent); int end = instructions.size() - 1;
        require(end >= 10 && instructions.get(end).getOpcode() == ARETURN, "Single object return tail");
        require(instructions.stream().filter(instruction -> instruction.getOpcode() == ARETURN).count() == 1, "One native object return");
        boolean custom = host.name.endsWith("/CustomData"); int start = end - (custom ? 10 : 12); require(start >= 0 && instructions.get(start).getOpcode() == DUP, "Original return identity duplicated");
        variable(instructions.get(start + 1), ASTORE, local); int cursor = start + 2;
        if (custom) variable(instructions.get(cursor++), ALOAD, 0);
        require(instructions.get(cursor) instanceof TypeInsnNode type && type.getOpcode() == NEW && type.desc.equals(CALLBACK), "Real return callback"); cursor++;
        require(instructions.get(cursor++).getOpcode() == DUP, "Callback construction DUP");
        require(instructions.get(cursor) instanceof LdcInsnNode constant && constant.cst.equals(parent.name), "Exact original callback identifier"); cursor++;
        require(instructions.get(cursor++).getOpcode() == ICONST_0, "Not cancellable"); variable(instructions.get(cursor++), ALOAD, local);
        require(instructions.get(cursor) instanceof MethodInsnNode constructor && constructor.getOpcode() == INVOKESPECIAL && constructor.owner.equals(CALLBACK) && constructor.name.equals("<init>") && constructor.desc.equals("(Ljava/lang/String;ZLjava/lang/Object;)V") && !constructor.itf, "Exact CallbackInfoReturnable constructor"); cursor++;
        if (!custom) { variable(instructions.get(cursor++), ASTORE, local); variable(instructions.get(cursor++), ALOAD, 0); variable(instructions.get(cursor++), ALOAD, local); }
        require(cursor == end - 1 && instructions.get(cursor) instanceof MethodInsnNode call && call.getOpcode() == INVOKESPECIAL && call.owner.equals(host.name) && call.name.equals(callback.name) && call.desc.equals(callback.desc) && !call.itf, "Exact return observer receiver and invocation");
        remove(parent, instructions, start, end);
    }

    private static void completeHandler(ClassNode target, ClassNode source, String name, String session) {
        MethodNode original = handler(source, name), actual = handler(target, name);
        require(original.access == ACC_PRIVATE && actual.access == original.access && actual.maxLocals == original.maxLocals && actual.signature == null && actual.exceptions.equals(original.exceptions) && actual.desc.equals(original.desc), "Exact component handler flags/locals/signature/exceptions: " + name);
        AnnotationNode merged = annotation(actual, "MixinMerged"); require(merged.values.size() == 6 && value(merged, "mixin").equals(source.name.replace('/', '.')) && value(merged, "priority").equals(1000) && value(merged, "sessionId").equals(session), "Exact component actual Mixin origin/session");
        require(OwnerFieldInstallationProof.nativeComponentLambdaBody(original, actual, source, target), "Whole component handler and same Throwable region: " + name);
    }
    static void verify(ClassNode target, Map<String, ClassNode> sources, Map<String, String> baseline) {
        require(host(target) >= 0 && baseline != null && baseline.keySet().equals(Set.copyOf(parents(target))) && sources.keySet().equals(Set.copyOf(requiredSources(target))), "Complete component parents/source set");
        for (String name : requiredSources(target)) { ClassNode source = sources.get(name); require(source != null && source.name.equals(PREFIX + name) && NativeBossLivingComposerSupportV2.fingerprint(source).equals(NativeBossLivingComposerSupportV2.fingerprint(authenticSource(name))), "Whole authentic component source: " + name); }
        ClassNode nodeSource = sources.get("OwnerRevocationNodeMixin"); MethodNode nodeGetter = handler(target, "ftboss$ownerRevocationNode"); String session = (String)value(annotation(nodeGetter, "MixinMerged"), "sessionId"); java.util.UUID.fromString(session);
        require(OwnerFieldInstallationProof.complete(new String[]{PREFIX.replace('/', '.') + "OwnerRevocationNodeMixin", target.name.replace('/', '.'), "ftboss$ownerRevocationNode", "ftboss$ownerInstallRevocationNode"}, target, nodeSource), "Complete original revocation getter/guarded installer including Lambda and source fields");
        for (String name : List.of("ftboss$ownerRevocationNode", "ftboss$ownerInstallRevocationNode")) {
            MethodNode original = handler(nodeSource, name), actual = handler(target, name); AnnotationNode merged = annotation(actual, "MixinMerged");
            require(actual.access == original.access && actual.maxLocals == original.maxLocals && actual.exceptions.equals(original.exceptions) && Objects.equals(actual.signature, original.signature) && merged.values.size() == 6 && value(merged, "mixin").equals(nodeSource.name.replace('/', '.')) && value(merged, "priority").equals(1000) && value(merged, "sessionId").equals(session), "Exact guarded revocation node flags/locals/signature/source/session: " + name);
        }
        require(target.interfaces.stream().filter("net/farmingtales/ftboss/item/OwnerRevocationNodeAccess"::equals).count() == 1, "Unique actual revocation node interface");
        List<FieldNode> fields = target.fields.stream().filter(field -> field.name.equals("ftboss$ownerRevocationNode")).toList(); require(fields.size() == 1 && fields.getFirst().access == ACC_PRIVATE && fields.getFirst().desc.equals("Lnet/farmingtales/ftboss/item/OwnerFullRepairGuard$Node;") && fields.getFirst().value == null, "Exact actual private revocation node field");
        String repairSource = switch (host(target)) { case 0 -> "OwnerFullRepairItemStackMixin"; case 1 -> "OwnerFullRepairComponentStorageMixin"; default -> "OwnerFullRepairCustomDataMixin"; };
        List<String> handlers = host(target) == 1 ? List.of("ftboss$fullRepairStorageValue", "ftboss$fullRepairStorageMap") : List.of(host(target) == 0 ? "ftboss$fullRepairAlias" : "ftboss$fullRepairCustomDataAlias");
        for (String name : handlers) completeHandler(target, sources.get(repairSource), name, session);
        if (host(target) == 1) { completeHandler(target, sources.get("NativeItemComponentStorageFieldsMixin"), "ftboss$nativeComponentValueWrite", session); completeHandler(target, sources.get("NativeItemComponentStorageFieldsMixin"), "ftboss$nativeComponentMapWrite", session); }
        for (String key : parents(target)) {
            MethodNode actual = method(target, key), stripped = NativeBossLivingComposerSupportV2.copy(actual); String expected = baseline.get(key); int split = expected.indexOf(':'); int locals = Integer.parseInt(expected.substring(0, split));
            if (host(target) == 1) {
                boolean value = key.startsWith("set(") || key.startsWith("remove("); stripHead(target, stripped, value ? "ftboss$fullRepairStorageValue" : "ftboss$fullRepairStorageMap");
                if (!key.startsWith("applyPatch(Lnet/minecraft/core/component/DataComponentType;")) stripHead(target, stripped, value ? "ftboss$nativeComponentValueWrite" : "ftboss$nativeComponentMapWrite");
                require(actual.maxLocals == locals, "Native component HEAD preserves all original locals");
            } else { stripAlias(target, stripped, host(target) == 0 ? "ftboss$fullRepairAlias" : "ftboss$fullRepairCustomDataAlias", locals); require(actual.maxLocals == locals + 1, "Only exact original return observer local added"); }
            require(shape(stripped).equals(expected.substring(split + 1)), "Whole pristine native component parent restored, including every original branch/API: " + key);
        }
        for (String name : handlers) {
            MethodNode actual = handler(target, name); long expected = host(target) == 1 ? (name.endsWith("Value") ? 2 : 4) : host(target) == 0 ? 2 : 1;
            long calls = target.methods.stream().flatMap(parent -> code(parent).stream()).filter(instruction -> instruction instanceof MethodInsnNode call && call.owner.equals(target.name) && call.name.equals(actual.name)).count(); require(calls == expected, "No duplicate or foreign component repair callback: " + name);
        }
    }
}

