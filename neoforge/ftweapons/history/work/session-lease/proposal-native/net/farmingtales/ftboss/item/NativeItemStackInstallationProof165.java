package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.farmingtales.ftboss.boss.ai.NativeBossLivingComposerSupportV2;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

public final class NativeItemStackInstallationProof165 implements Opcodes {
    private static final String PREFIX = "net/farmingtales/ftboss/mixin/";
    private static final String STACK = "net/minecraft/world/item/ItemStack";
    private static final String CI = "org/spongepowered/asm/mixin/injection/callback/CallbackInfo";
    private static final List<String> SOURCE_NAMES = List.of("OwnerFullRepairItemStackMixin", "OwnerStatueWearItemStackMixin", "NativeItemComponentStackFieldsMixin", "NativeWearTokenMixin", "WeaponMaterialDurabilityMixin", "OwnerStatueWearRawDamageMixin", "NativeAppliedDamageEntryMixin", "OwnerRevocationNodeMixin", "FoodWeaponItemUseMixin");
    private final Map<String, ClassNode> TRUSTED = new LinkedHashMap<>();
    private final Map<String, String> REPAIR_HANDLERS = new LinkedHashMap<>();
    private final Map<String, Integer> WEAR_KINDS = new LinkedHashMap<>();
    private static final List<String> READ_CUT = List.of("setDamageValue", "set", "update", "remove", "applyComponentsAndValidate", "applyComponents", "enchant", "setCount", "grow", "shrink");
    private final ClassNode PRISTINE;
    private final String SESSION;

    private static void require(boolean condition, String reason) { if (!condition) throw new IllegalStateException(reason); }
    private static ClassNode read(byte[] bytes) { ClassNode result = new ClassNode(); new ClassReader(bytes).accept(result, 0); return result; }
    private static ClassNode copy(ClassNode source) { ClassNode result = new ClassNode(); source.accept(result); for (MethodNode method : result.methods) for (AbstractInsnNode instruction : method.instructions) if (instruction instanceof InvokeDynamicInsnNode dynamic) dynamic.bsmArgs = dynamic.bsmArgs.clone(); return result; }
    private static MethodNode method(ClassNode host, String key) { List<MethodNode> found = host.methods.stream().filter(candidate -> (candidate.name + candidate.desc).equals(key)).toList(); require(found.size() == 1, "Unique exact method " + key); return found.getFirst(); }
    private static List<AbstractInsnNode> code(MethodNode method) { return Arrays.stream(method.instructions.toArray()).filter(instruction -> instruction.getOpcode() >= 0).toList(); }
    private static AnnotationNode annotation(MethodNode method, String suffix) { List<AnnotationNode> values = new ArrayList<>(); if (method.visibleAnnotations != null) values.addAll(method.visibleAnnotations); if (method.invisibleAnnotations != null) values.addAll(method.invisibleAnnotations); List<AnnotationNode> found = values.stream().filter(value -> value.desc.endsWith("/" + suffix + ";")).toList(); require(found.size() == 1, "Exact annotation " + suffix); return found.getFirst(); }
    private static Object value(AnnotationNode annotation, String name) { int index = annotation.values.indexOf(name); return index < 0 ? null : annotation.values.get(index + 1); }
    private static String shape(MethodNode source) { MethodNode result = NativeBossLivingComposerSupportV2.copy(source); result.visibleAnnotations = null; result.invisibleAnnotations = null; result.visibleTypeAnnotations = null; result.invisibleTypeAnnotations = null; return NativeBossLivingComposerSupportV2.methodFingerprint(result); }
    private static String sourceName(String name) { return name.equals("GeckoItemStackMixin") ? "software/bernie/geckolib/mixin/common/ItemStackMixin" : PREFIX + name; }
    private static String fingerprint(ClassNode source) { return NativeBossLivingComposerSupportV2.fingerprint(source); }
    private static byte[] bytes(ClassNode source) { org.objectweb.asm.ClassWriter writer = new org.objectweb.asm.ClassWriter(0); source.accept(writer); return writer.toByteArray(); }

    private MethodNode handler(ClassNode host, Map<String, ClassNode> sources, String family, String suffix) {
        ClassNode source = sources.get(family);
        require(source != null && fingerprint(source).equals(fingerprint(TRUSTED.get(family))), "Whole authentic compiled source identity " + family);
        List<MethodNode> original = source.methods.stream().filter(candidate -> candidate.name.equals(suffix)).toList();
        require(original.size() == 1, "Unique authentic source handler " + suffix);
        MethodNode before = original.getFirst();
        List<MethodNode> matches = host.methods.stream().filter(candidate -> candidate.name.equals(suffix) || candidate.name.endsWith("$" + suffix)).filter(candidate -> {
            try { return source.name.replace('/', '.').equals(value(annotation(candidate, "MixinMerged"), "mixin")); } catch (RuntimeException failure) { return false; }
        }).toList();
        require(matches.size() == 1, "Unique genuine merged handler " + family + ":" + suffix);
        MethodNode actual = matches.getFirst();
        AnnotationNode merged = annotation(actual, "MixinMerged");
        require(merged.values.size() == 6 && Objects.equals(value(merged, "priority"), 1000) && Objects.equals(value(merged, "sessionId"), SESSION), "Exact merged source priority/session " + suffix);
        require(actual.access == before.access && actual.maxLocals == before.maxLocals && actual.signature == null && actual.desc.equals(before.desc) && actual.exceptions.equals(before.exceptions), "Exact handler flags/locals/signature/exceptions " + suffix);
        require(nativeUseLambdaBody(before, actual, source, host), "Complete source handler, lambda and Throwable identity " + suffix);
        try { new Analyzer<>(new BasicVerifier()).analyze(host.name, NativeBossLivingComposerSupportV2.copy(actual)); } catch (org.objectweb.asm.tree.analysis.AnalyzerException failure) { throw new IllegalStateException("Actual callback ASM stack/locals invalid", failure); }
        return actual;
    }

    private static boolean nativeUseLambdaBody(MethodNode before, MethodNode actual, ClassNode source, ClassNode host) {
        if (!source.name.equals(PREFIX + "FoodWeaponItemUseMixin") || !before.name.equals("ftboss$useSrg")) return OwnerFieldInstallationProof.nativeComponentLambdaBody(before, actual, source, host);
        ClassNode projected = copy(source);
        MethodNode expected = method(projected, before.name + before.desc);
        List<InvokeDynamicInsnNode> factories = code(expected).stream().filter(instruction -> instruction instanceof InvokeDynamicInsnNode).map(instruction -> (InvokeDynamicInsnNode)instruction).toList();
        require(factories.size() == 1, "Exactly one authentic native use lambda factory");
        InvokeDynamicInsnNode factory = factories.getFirst();
        String parameters = "Lnet/minecraft/world/item/Item;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Ljava/util/function/Supplier;";
        require(factory.name.equals("get") && factory.desc.equals("(L" + source.name + ";" + parameters), "Exact authentic native use captured receiver and arguments");
        factory.desc = "(L" + host.name + ";" + parameters;
        return OwnerFieldInstallationProof.nativeComponentLambdaBody(expected, actual, projected, host);
    }

    private static InsnList invokeHead(ClassNode host, MethodNode callback, boolean arguments, MethodNode parent) {
        InsnList result = new InsnList(); result.add(new VarInsnNode(ALOAD, 0));
        if (arguments) { int local = 1; for (Type type : Type.getArgumentTypes(parent.desc)) { result.add(new VarInsnNode(type.getOpcode(ILOAD), local)); local += type.getSize(); } }
        result.add(new InsnNode(ACONST_NULL)); result.add(new MethodInsnNode((callback.access & ACC_PUBLIC) != 0 ? INVOKEVIRTUAL : INVOKESPECIAL, host.name, callback.name, callback.desc, false)); return result;
    }

    private static void beforeReturns(MethodNode parent, java.util.function.Supplier<InsnList> instructions) { for (AbstractInsnNode instruction : parent.instructions.toArray()) if (instruction.getOpcode() >= IRETURN && instruction.getOpcode() <= RETURN) parent.instructions.insertBefore(instruction, instructions.get()); }
    private static void modifyArgument(ClassNode host, MethodNode parent, MethodNode callback) { InsnList result = new InsnList(); result.add(new VarInsnNode(ALOAD, 0)); result.add(new VarInsnNode(ILOAD, 1)); result.add(new MethodInsnNode(INVOKESPECIAL, host.name, callback.name, callback.desc, false)); result.add(new VarInsnNode(ISTORE, 1)); parent.instructions.insert(result); }
    private static MethodInsnNode originalCall(MethodNode parent, String target) { List<MethodInsnNode> matches = code(parent).stream().filter(instruction -> instruction instanceof MethodInsnNode call && ("L" + call.owner + ";" + call.name + call.desc).equals(target)).map(instruction -> (MethodInsnNode)instruction).toList(); require(matches.size() == 1, "Exact unique genuine invocation " + target); return matches.getFirst(); }

    private static void wrapOperation(ClassNode host, MethodNode parent, MethodNode actualParent, MethodNode callback, MethodNode originalCallback) throws Exception {
        AnnotationNode wrap = annotation(originalCallback, "WrapOperation"); List<?> selectors = (List<?>)value(wrap, "method"); require(selectors.contains(parent.name) || selectors.contains(parent.name + parent.desc), "Authentic wrap selector");
        List<?> points = (List<?>)value(wrap, "at"); require(points.size() == 1 && points.getFirst() instanceof AnnotationNode, "One authentic invocation point"); AnnotationNode point = (AnnotationNode)points.getFirst(); require("INVOKE".equals(value(point, "value")), "Authentic invocation injection");
        MethodInsnNode original = originalCall(parent, (String)value(point, "target"));
        List<Type> types = new ArrayList<>(); if (original.getOpcode() != INVOKESTATIC) types.add(Type.getObjectType(original.owner)); types.addAll(List.of(Type.getArgumentTypes(original.desc)));
        String[] argumentTypes = types.stream().map(type -> type.getSort() == Type.INT ? "int" : type.getInternalName()).toArray(String[]::new);
        Type returnType = Type.getReturnType(original.desc); String result = returnType.getSort() == Type.VOID ? "Ljava/lang/Void;" : returnType.getDescriptor();
        List<InvokeDynamicInsnNode> factories = new ArrayList<>();
        List<AbstractInsnNode> actualCode = code(actualParent);
        for (int index = 0; index < actualCode.size(); index++) if (actualCode.get(index) instanceof MethodInsnNode call && call.owner.equals(host.name) && call.name.equals(callback.name)) {
            List<InvokeDynamicInsnNode> prefix = actualCode.subList(0, index).stream().filter(instruction -> instruction instanceof InvokeDynamicInsnNode dynamic && dynamic.name.equals("call")).map(instruction -> (InvokeDynamicInsnNode)instruction).toList(); require(!prefix.isEmpty(), "Actual operation lambda before authentic handler"); factories.add(prefix.getLast());
        }
        require(factories.size() == 1, "Exactly one operation factory per genuine native call"); InvokeDynamicInsnNode dynamic = factories.getFirst();
        require(OwnerFieldInstallationProof.nativeItemStackOperationBridge165(host, dynamic, argumentTypes, result, original.getOpcode() + ":" + original.owner + ":" + original.name + ":" + original.desc + ":" + original.itf) != null, "Complete real Operation argument/count/type/member bridge " + callback.name);
        int[] locals = new int[types.size()]; int next = parent.maxLocals;
        for (int index = 0; index < types.size(); index++) { locals[index] = next; next += types.get(index).getSize(); }
        InsnList replacement = new InsnList();
        for (int index = types.size() - 1; index >= 0; index--) replacement.add(new VarInsnNode(types.get(index).getOpcode(ISTORE), locals[index]));
        replacement.add(new VarInsnNode(ALOAD, 0));
        for (int index = 0; index < types.size(); index++) replacement.add(new VarInsnNode(types.get(index).getOpcode(ILOAD), locals[index]));
        replacement.add(new InvokeDynamicInsnNode(dynamic.name, dynamic.desc, dynamic.bsm, dynamic.bsmArgs.clone()));
        List<Type> callbackTypes = List.of(Type.getArgumentTypes(callback.desc)); int operationIndex = types.size(); require(callbackTypes.get(operationIndex).getDescriptor().equals("Lcom/llamalad7/mixinextras/injector/wrapoperation/Operation;"), "Exact authentic operation position");
        List<Type> captured = callbackTypes.subList(operationIndex + 1, callbackTypes.size());
        if (!captured.isEmpty()) { require(captured.equals(List.of(Type.getArgumentTypes(parent.desc))), "Only exact authentic parent argument captures"); int local = 1; for (Type type : captured) { replacement.add(new VarInsnNode(type.getOpcode(ILOAD), local)); local += type.getSize(); } }
        replacement.add(new MethodInsnNode((callback.access & ACC_PUBLIC) != 0 ? INVOKEVIRTUAL : INVOKESPECIAL, host.name, callback.name, callback.desc, false));
        parent.instructions.insertBefore(original, replacement); parent.instructions.remove(original); parent.maxLocals = next;
    }

    private static void cancelBeforeMaxDamage(ClassNode host, MethodNode parent, MethodNode callback) {
        MethodInsnNode target = originalCall(parent, "L" + STACK + ";getMaxDamage()I"); int local = parent.maxLocals++; LabelNode continued = new LabelNode(); InsnList result = new InsnList();
        result.add(new TypeInsnNode(NEW, CI)); result.add(new InsnNode(DUP)); result.add(new LdcInsnNode(parent.name)); result.add(new InsnNode(ICONST_1)); result.add(new MethodInsnNode(INVOKESPECIAL, CI, "<init>", "(Ljava/lang/String;Z)V", false)); result.add(new VarInsnNode(ASTORE, local)); result.add(new VarInsnNode(ALOAD, 0)); int argumentLocal = 1;
        for (Type type : Type.getArgumentTypes(parent.desc)) { result.add(new VarInsnNode(type.getOpcode(ILOAD), argumentLocal)); argumentLocal += type.getSize(); }
        result.add(new VarInsnNode(ALOAD, local)); result.add(new MethodInsnNode(INVOKESPECIAL, host.name, callback.name, callback.desc, false)); result.add(new VarInsnNode(ALOAD, local)); result.add(new MethodInsnNode(INVOKEVIRTUAL, CI, "isCancelled", "()Z", false)); result.add(new JumpInsnNode(IFEQ, continued)); result.add(new InsnNode(RETURN)); result.add(continued); parent.instructions.insertBefore(target, result);
    }

    private static void wear(ClassNode host, MethodNode parent, int kind) { OwnerStatueWearBoundary.nativeItemStackReplay165(host, parent, kind); }
    private static ClassNode context(ClassNode actual, MethodNode parent) { ClassNode result = copy(actual); result.methods.removeIf(method -> (method.name + method.desc).equals(parent.name + parent.desc)); result.methods.add(parent); return result; }
    private static void same(MethodNode expected, MethodNode actual, String reason) { require(expected.maxLocals == actual.maxLocals && shape(expected).equals(shape(actual)), reason); }

    private MethodNode replay(ClassNode actual, Map<String, ClassNode> sources, String selector) throws Exception {
        MethodNode parent = NativeBossLivingComposerSupportV2.copy(method(PRISTINE, selector)); MethodNode actualParent = method(actual, selector); String suffix = REPAIR_HANDLERS.get(selector); require(suffix != null && !suffix.equals("ftboss$fullRepairAlias"), "One exact mutator selector");
        MethodNode repair = handler(actual, sources, "OwnerFullRepairItemStackMixin", suffix); parent.instructions.insert(invokeHead(actual, repair, false, parent));
        Integer kind = WEAR_KINDS.get(selector);
        if (parent.name.equals("setCount")) { MethodNode count = handler(actual, sources, "NativeItemComponentStackFieldsMixin", "ftboss$nativeStackCountWrite"); AbstractInsnNode originalHead = code(parent).get(3); parent.instructions.insertBefore(originalHead, invokeHead(actual, count, false, parent)); }
        if (selector.equals("setDamageValue(I)V")) {
            modifyArgument(actual, parent, handler(actual, sources, "WeaponMaterialDurabilityMixin", "ftboss$protectedDamage"));
            MethodNode returned = handler(actual, sources, "WeaponMaterialDurabilityMixin", "ftboss$wearLoss"); beforeReturns(parent, () -> invokeHead(actual, returned, true, parent));
        }
        if (kind != null && kind == 8) { modifyArgument(actual, parent, handler(actual, sources, "WeaponMaterialDurabilityMixin", "ftboss$materialWear")); cancelBeforeMaxDamage(actual, parent, handler(actual, sources, "WeaponMaterialDurabilityMixin", "ftboss$keepExhausted")); }
        if (kind != null) parent.instructions.insert(invokeHead(actual, handler(actual, sources, "OwnerStatueWearItemStackMixin", "ftboss$statueWear$anchor" + String.format(java.util.Locale.ROOT, "%02d", kind)), false, parent));
        if (kind != null && kind == 7) {
            parent.instructions.insert(invokeHead(actual, handler(actual, sources, "NativeWearTokenMixin", "ftboss$v9$nativeWearBegin"), true, parent));
            MethodNode returned = handler(actual, sources, "NativeWearTokenMixin", "ftboss$v9$nativeWearReturn"); beforeReturns(parent, () -> invokeHead(actual, returned, true, parent));
            handler(actual, sources, "NativeWearTokenMixin", "ftboss$v9$wearUnwind");
            ClassNode projected = context(actual, parent); projected.methods.removeIf(method -> method.name.startsWith("ftboss$v9$unwindGuard$")); require(NativeHookUnwind.apply(projected, "net.farmingtales.ftboss.mixin.NativeWearTokenMixin") == 1, "Exact native wear unwind projection");
            for (MethodNode guard : projected.methods) if (guard.name.startsWith("ftboss$v9$unwindGuard$")) same(guard, method(actual, guard.name + guard.desc), "Whole native unwind guard, including Throwable region");
        }
        if (selector.equals("set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;")) nativeCustomDataReturn(actual,parent,sources);
        if (selector.equals("setCount(I)V")) nativeChildCount246(actual, parent, sources);
        if (READ_CUT.contains(parent.name)) require(NativeReadCutHooks.install(actual, parent, "weapon"), "Complete genuine read-cut projection");
        if (selector.equals("setDamageValue(I)V")) { String family = "OwnerStatueWearRawDamageMixin", name = "ftboss$statueRawDamage"; MethodNode callback = handler(actual, sources, family, name); wrapOperation(actual, parent, actualParent, callback, sources.get(family).methods.stream().filter(method -> method.name.equals(name)).findFirst().orElseThrow()); }
        if (kind != null && kind == 8) { String family = "NativeAppliedDamageEntryMixin", name = "ftboss$v6$applied"; MethodNode callback = handler(actual, sources, family, name); wrapOperation(actual, parent, actualParent, callback, sources.get(family).methods.stream().filter(method -> method.name.equals(name)).findFirst().orElseThrow()); }
        if (parent.name.equals("split")) { String family = "GeckoItemStackMixin", name = "geckolib$removeGeckolibIdOnCopy"; MethodNode callback = handler(actual, sources, family, name); wrapOperation(actual, parent, actualParent, callback, sources.get(family).methods.stream().filter(method -> method.name.equals(name)).findFirst().orElseThrow()); }
        nativeUseRedirect(actual, parent, sources);
        if (parent.name.equals("use")) frostUseObservers(actual, parent);
        if (kind != null) wear(actual, parent, kind);
        return parent;
    }

    private void frostUseObservers(ClassNode actual, MethodNode parent) throws Exception {
        String family = "NativeReadCutItemStackMixin";
        ClassNode source = authentic(family);
        for (String suffix : List.of("ftboss$nativeFrostUseEntered", "ftboss$nativeFrostUseReturned")) {
            MethodNode before = source.methods.stream().filter(method -> method.name.equals(suffix)).reduce((first, second) -> { throw new IllegalStateException("Duplicate Frost use source handler"); }).orElseThrow();
            AnnotationNode inject = annotation(before, "Inject");
            boolean head = suffix.endsWith("Entered");
            require(List.of(parent.name + parent.desc).equals(value(inject, "method")), "Exact original Frost use observer selector");
            List<?> points = (List<?>)value(inject, "at");
            require(points.size() == 1 && points.getFirst() instanceof AnnotationNode && Objects.equals(value((AnnotationNode)points.getFirst(), "value"), head ? "HEAD" : "RETURN")
                && Objects.equals(value(inject, "require"), 1) && (!head || Objects.equals(value(inject, "order"), 900)), "Exact original Frost use observer injection contract");
            List<MethodNode> found = actual.methods.stream().filter(method -> method.name.endsWith("$" + suffix) && method.desc.equals(before.desc)).filter(method -> {
                try { return Objects.equals(value(annotation(method, "MixinMerged"), "mixin"), source.name.replace('/', '.')); }
                catch (RuntimeException failure) { return false; }
            }).toList();
            require(found.size() == 1, "Unique actual Frost use observer handler");
            MethodNode callback = found.getFirst();
            AnnotationNode merged = annotation(callback, "MixinMerged");
            require(merged.values.size() == 6 && Objects.equals(value(merged, "priority"), 1) && Objects.equals(value(merged, "sessionId"), SESSION)
                && callback.access == before.access && callback.maxLocals == before.maxLocals && callback.signature == null
                && callback.exceptions.equals(before.exceptions) && OwnerFieldInstallationProof.nativeComponentLambdaBody(before, callback, source, actual), "Whole authentic Frost use callback and actual installation provenance");
            new Analyzer<>(new BasicVerifier()).analyze(actual.name, NativeBossLivingComposerSupportV2.copy(callback));
            long expectedCalls = head ? 1 : code(parent).stream().filter(instruction -> instruction.getOpcode() == ARETURN).count();
            long actualCalls = actual.methods.stream().flatMap(method -> code(method).stream()).filter(instruction -> instruction instanceof MethodInsnNode call
                && call.owner.equals(actual.name) && call.name.equals(callback.name) && call.desc.equals(callback.desc)).count();
            require(actualCalls == expectedCalls, "No missing, duplicated or foreign Frost use observer invocation");
            if (head) parent.instructions.insert(invokeHead(actual, callback, true, parent));
            else beforeReturns(parent, () -> invokeHead(actual, callback, true, parent));
        }
    }

    private void nativeUseRedirect(ClassNode actual, MethodNode parent, Map<String, ClassNode> sources) {
        String suffix = switch (parent.name) {
            case "use" -> "ftboss$useSrg";
            case "releaseUsing" -> "ftboss$releaseSrg";
            case "onUseTick" -> "ftboss$tickSrg";
            case "finishUsingItem" -> "ftboss$finishSrg";
            default -> null;
        };
        if (suffix == null) return;
        ClassNode source = sources.get("FoodWeaponItemUseMixin");
        MethodNode callback = handler(actual, sources, "FoodWeaponItemUseMixin", suffix);
        MethodNode originalCallback = method(source, suffix + callback.desc);
        AnnotationNode redirect = annotation(originalCallback, "Redirect");
        require(List.of(parent.name + parent.desc).equals(value(redirect, "method"))
            && Integer.valueOf(0).equals(value(redirect, "require")) && Boolean.FALSE.equals(value(redirect, "remap")), "Exact authentic native use redirect selector and requirements");
        AnnotationNode group = annotation(originalCallback, "Group");
        require(Integer.valueOf(1).equals(value(group, "min")) && Integer.valueOf(1).equals(value(group, "max")), "Exactly one authentic native use redirect");
        Object pointValue = value(redirect, "at");
        require(pointValue instanceof AnnotationNode, "One authentic native use redirect point");
        AnnotationNode point = (AnnotationNode)pointValue;
        require("INVOKE".equals(value(point, "value")), "Authentic native use invocation redirect");
        MethodInsnNode original = originalCall(parent, (String)value(point, "target"));
        require(original.getOpcode() == INVOKEVIRTUAL && original.owner.equals("net/minecraft/world/item/Item") && !original.itf, "Exact original native Item virtual call");
        List<Type> types = new ArrayList<>();
        types.add(Type.getObjectType(original.owner));
        types.addAll(List.of(Type.getArgumentTypes(original.desc)));
        require(types.equals(List.of(Type.getArgumentTypes(callback.desc)))
            && Type.getReturnType(callback.desc).equals(Type.getReturnType(original.desc)) && callback.access == ACC_PRIVATE, "Exact native redirect receiver, arguments, return and private access");
        int[] locals = new int[types.size()];
        int next = parent.maxLocals;
        for (int index = 0; index < types.size(); index++) { locals[index] = next; next += types.get(index).getSize(); }
        InsnList replacement = new InsnList();
        for (int index = types.size() - 1; index >= 0; index--) replacement.add(new VarInsnNode(types.get(index).getOpcode(ISTORE), locals[index]));
        replacement.add(new VarInsnNode(ALOAD, 0));
        for (int index = 0; index < types.size(); index++) replacement.add(new VarInsnNode(types.get(index).getOpcode(ILOAD), locals[index]));
        replacement.add(new MethodInsnNode(INVOKESPECIAL, actual.name, callback.name, callback.desc, false));
        parent.instructions.insertBefore(original, replacement);
        parent.instructions.remove(original);
        parent.maxLocals = next + 1;
    }

    private void nativeCustomDataReturn(ClassNode actual,MethodNode parent,Map<String,ClassNode> sources) {
        MethodNode callback = handler(actual,sources,"NativeItemComponentStackFieldsMixin","ftboss$nativeCustomDataAssigned");
        MethodNode source = method(sources.get("NativeItemComponentStackFieldsMixin"),"ftboss$nativeCustomDataAssigned(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V");
        AnnotationNode injection = annotation(source,"Inject");
        require(value(injection,"method").equals(List.of("set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Ljava/lang/Object;"))
            && Integer.valueOf(1).equals(value(injection,"require")) && Integer.valueOf(1).equals(value(injection,"allow"))
            && Boolean.FALSE.equals(value(injection,"remap")) && !Boolean.TRUE.equals(value(injection,"cancellable")),
            "One non-cancelling exact native CUSTOM_DATA assignment return handler");
        List<?> points = (List<?>)value(injection,"at");
        require(points.size() == 1 && points.getFirst() instanceof AnnotationNode point && "RETURN".equals(value(point,"value")),
            "Exact native assignment RETURN point");
        List<AbstractInsnNode> returns = new ArrayList<>();
        for (AbstractInsnNode instruction : code(parent)) if (instruction.getOpcode() == ARETURN) returns.add(instruction);
        require(returns.size() == 1,"One original generic ItemStack.set return");
        int returned = parent.maxLocals++;
        InsnList injected = new InsnList();
        injected.add(new InsnNode(DUP));
        injected.add(new VarInsnNode(ASTORE,returned));
        injected.add(new VarInsnNode(ALOAD,0));
        injected.add(new VarInsnNode(ALOAD,1));
        injected.add(new VarInsnNode(ALOAD,2));
        String callbackType = "org/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable";
        injected.add(new TypeInsnNode(NEW,callbackType));
        injected.add(new InsnNode(DUP));
        injected.add(new LdcInsnNode("set"));
        injected.add(new InsnNode(ICONST_0));
        injected.add(new VarInsnNode(ALOAD,returned));
        injected.add(new MethodInsnNode(INVOKESPECIAL,callbackType,"<init>","(Ljava/lang/String;ZLjava/lang/Object;)V",false));
        injected.add(new MethodInsnNode((callback.access & ACC_PUBLIC) != 0 ? INVOKEVIRTUAL : INVOKESPECIAL,actual.name,callback.name,callback.desc,false));
        parent.instructions.insertBefore(returns.getFirst(),injected);
        parent.maxStack = Math.max(parent.maxStack,9);
    }

    private void nativeChildCount246(ClassNode actual, MethodNode parent, Map<String, ClassNode> sources) {
        String family = "NativeHandlerChildCountMixin244";
        MethodNode callback = handler(actual, sources, family, "ftboss$nativeChildCountWrite");
        MethodNode helper = handler(actual, sources, family, "ftboss$nativeChildCountChanging");
        require(helper.desc.equals("()V") && callback.desc.equals("(L" + CI + ";)V"), "Exact child-count callback/helper descriptors");
        MethodNode original = method(sources.get(family), "ftboss$nativeChildCountWrite(L" + CI + ";)V");
        AnnotationNode injection = annotation(original, "Inject");
        require(List.of("setCount(I)V").equals(value(injection, "method")) && Integer.valueOf(1).equals(value(injection, "require")) && Integer.valueOf(1).equals(value(injection, "allow")) && Boolean.FALSE.equals(value(injection, "remap")) && !Boolean.TRUE.equals(value(injection, "cancellable")), "One exact noncancelling child-count injection");
        List<?> points = (List<?>)value(injection, "at");
        require(points.size() == 1 && points.getFirst() instanceof AnnotationNode point && "FIELD".equals(value(point, "value")) && "Lnet/minecraft/world/item/ItemStack;count:I".equals(value(point, "target")) && Integer.valueOf(PUTFIELD).equals(value(point, "opcode")) && value(point, "shift") == null, "Exact unshifted native count PUTFIELD source");
        List<FieldInsnNode> writes = code(parent).stream().filter(instruction -> instruction instanceof FieldInsnNode field && field.getOpcode() == PUTFIELD && field.owner.equals(STACK) && field.name.equals("count") && field.desc.equals("I")).map(instruction -> (FieldInsnNode)instruction).toList();
        require(writes.size() == 1, "Unique authentic count field write");
        parent.instructions.insertBefore(writes.getFirst(), invokeHead(actual, callback, false, parent));
        parent.maxStack = Math.max(parent.maxStack, 4);
    }

    private void verifyParent(ClassNode actual, Map<String, ClassNode> sources, String selector) throws Exception {
        verifySources(sources);
        if (WEAR_KINDS.containsKey(selector)) verifyGuards(actual);
        MethodNode parent = method(actual, selector); MethodNode expected = replay(actual, sources, selector); same(expected, parent, "Whole authentic native parent and every original branch/API/Throwable region " + selector);
        new Analyzer<>(new BasicVerifier()).analyze(actual.name, NativeBossLivingComposerSupportV2.copy(parent));
    }

    private NativeItemStackInstallationProof165(ClassNode actual, Map<String, ClassNode> sources, ClassNode pristine) {
        require(actual != null && pristine != null && STACK.equals(actual.name) && STACK.equals(pristine.name), "Exact whole native ItemStack target and pre-Mixin image");
        PRISTINE = pristine;
        for (String name : SOURCE_NAMES) TRUSTED.put(name, authentic(name));
        TRUSTED.put("GeckoItemStackMixin", authentic("GeckoItemStackMixin"));
        TRUSTED.put("NativeHandlerChildCountMixin244", authentic("NativeHandlerChildCountMixin244"));
        verifySources(sources);
        MethodNode node = method(actual, "ftboss$ownerRevocationNode()Lnet/farmingtales/ftboss/item/OwnerFullRepairGuard$Node;"); SESSION = (String)value(annotation(node, "MixinMerged"), "sessionId"); java.util.UUID.fromString(SESSION);
        for (MethodNode method : TRUSTED.get("OwnerFullRepairItemStackMixin").methods) { if (!method.name.startsWith("ftboss$")) continue; for (Object selector : (List<?>)value(annotation(method, "Inject"), "method")) require(REPAIR_HANDLERS.put((String)selector, method.name) == null, "Distinct authentic selectors"); }
        for (MethodNode method : TRUSTED.get("OwnerStatueWearItemStackMixin").methods) { if (!method.name.startsWith("ftboss$statueWear$anchor")) continue; int kind = Integer.parseInt(method.name.substring(method.name.length() - 2)); for (Object selector : (List<?>)value(annotation(method, "Inject"), "method")) WEAR_KINDS.put((String)selector, kind); }
        require(REPAIR_HANDLERS.size() == 43 && WEAR_KINDS.size() == 14, "All43 authentic repair parents and14 original wear specifications retained");
    }

    private static ClassNode authentic(String family) {
        Class<?> resourceOwner = family.equals("GeckoItemStackMixin") ? software.bernie.geckolib.GeckoLibConstants.class : NativeItemStackInstallationProof165.class;
        try (var stream = resourceOwner.getResourceAsStream("/" + sourceName(family) + ".class")) { require(stream != null, "Compiled authentic source resource " + family); return read(stream.readAllBytes()); } catch (java.io.IOException failure) { throw new IllegalStateException("Cannot read authentic current source " + family, failure); }
    }

    private void verifySources(Map<String, ClassNode> sources) {
        require(sources.keySet().equals(TRUSTED.keySet()), "Exact full authentic source set, no extra/missing family");
        for (String family : TRUSTED.keySet()) require(sources.get(family) != null && fingerprint(sources.get(family)).equals(fingerprint(TRUSTED.get(family))), "Whole authentic source identity " + family);
    }

    private static void verifyGuards(ClassNode actual) throws Exception {
        ClassNode projection = OwnerStatueWearBoundary.nativeItemStackGuards165(actual);
        for (MethodNode expected : projection.methods) { MethodNode found = method(actual, expected.name + expected.desc); same(expected, found, "Whole authentic wear guard " + expected.name); new Analyzer<>(new BasicVerifier()).analyze(actual.name, NativeBossLivingComposerSupportV2.copy(found)); }
    }

    private void verifyAliases(ClassNode actual, Map<String, ClassNode> sources) {
        verifySources(sources); Map<String, ClassNode> required = new LinkedHashMap<>(); required.put("OwnerFullRepairItemStackMixin", sources.get("OwnerFullRepairItemStackMixin")); required.put("OwnerRevocationNodeMixin", sources.get("OwnerRevocationNodeMixin")); NativeComponentInstallationProof163.verify(actual, required, NativeComponentInstallationProof163.snapshot(PRISTINE));
        handler(actual, sources, "OwnerFullRepairItemStackMixin", "ftboss$fullRepairAlias");
        for (var entry : REPAIR_HANDLERS.entrySet()) if (entry.getValue().equals("ftboss$fullRepairAlias")) { try { new Analyzer<>(new BasicVerifier()).analyze(actual.name, NativeBossLivingComposerSupportV2.copy(method(actual, entry.getKey()))); } catch (org.objectweb.asm.tree.analysis.AnalyzerException failure) { throw new IllegalStateException("Alias parent ASM stack/locals invalid", failure); } }
    }

    static List<String> requiredSources() { List<String> result = new ArrayList<>(SOURCE_NAMES); result.add("GeckoItemStackMixin"); return List.copyOf(result); }

    static String className(String family) { return sourceName(family).replace('/', '.'); }

    static ClassNode snapshot(ClassNode actual) {
        require(actual != null && STACK.equals(actual.name), "Exact original ItemStack preimage required"); ClassNode result = copy(actual); ClassNode source = authentic("OwnerFullRepairItemStackMixin"); List<String> selected = new ArrayList<>();
        for (MethodNode method : source.methods) if (method.name.startsWith("ftboss$")) for (Object selector : (List<?>)value(annotation(method, "Inject"), "method")) selected.add((String)selector);
        require(selected.size() == 43 && selected.stream().distinct().count() == 43, "All43 source selectors before mutation");
        for (String selector : selected) { MethodNode parent = method(result, selector); require((parent.access & (ACC_STATIC | ACC_NATIVE | ACC_ABSTRACT)) == 0, "Original instance native parent"); }
        result.methods.removeIf(method -> !selected.contains(method.name + method.desc)); return result;
    }

    static void verify(ClassNode actual, Map<String, ClassNode> sources, ClassNode pristine) {
        try {
            NativeItemStackInstallationProof165 proof = new NativeItemStackInstallationProof165(actual, sources, pristine); proof.verifyAliases(actual, sources);
            for (var entry : proof.REPAIR_HANDLERS.entrySet()) if (!entry.getValue().equals("ftboss$fullRepairAlias")) proof.verifyParent(actual, sources, entry.getKey());
        } catch (Exception failure) { throw new IllegalStateException("Complete current-native ItemStack final parent proof failed", failure); }
    }
}
