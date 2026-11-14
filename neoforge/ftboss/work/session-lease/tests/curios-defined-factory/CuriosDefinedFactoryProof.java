package ftboss.provenance;

import com.google.gson.GsonBuilder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;

public final class CuriosDefinedFactoryProof implements Opcodes {
    private static final String CURIOS = "top/theillusivec4/curios/Curios";
    private static final String HOOKS = "top/theillusivec4/curios/mixin/CuriosImplMixinHooks";
    private static final String ITEM = "net/minecraft/world/item/Item";
    private static final String STACK = "net/minecraft/world/item/ItemStack";
    private static final String CURIO_ITEM = "top/theillusivec4/curios/api/type/capability/ICurioItem";
    private static final String CURIO = "top/theillusivec4/curios/api/type/capability/ICurio";
    private static final String WRAPPER = "top/theillusivec4/curios/common/capability/ItemizedCurioCapability";
    private static final String EVENT = "net/neoforged/neoforge/capabilities/RegisterCapabilitiesEvent";
    private static final String OBSERVATION = "net/farmingtales/ftboss/item/NativeItemCapabilityObservation";
    private static final String TRACKED = "net/farmingtales/ftboss/item/NativeCuriosProviderFields260";
    private static final String FACTORY_DESC = "(L" + ITEM + ";L" + STACK + ";Ljava/lang/Void;)L" + CURIO + ";";
    private static final String PROVIDER_DESC = "(L" + ITEM + ";)Lnet/neoforged/neoforge/capabilities/ICapabilityProvider;";

    private static void require(boolean accepted, String message) {
        if (!accepted) throw new AssertionError(message);
    }

    private static List<AbstractInsnNode> instructions(MethodNode method) {
        return Arrays.stream(method.instructions.toArray()).filter(node -> node.getOpcode() >= 0).toList();
    }

    private static String token(AbstractInsnNode node, List<AbstractInsnNode> code) {
        String opcode = Printer.OPCODES[node.getOpcode()];
        if (node instanceof VarInsnNode variable) return opcode + " " + variable.var;
        if (node instanceof MethodInsnNode call) return opcode + " " + call.owner + "." + call.name + call.desc;
        if (node instanceof FieldInsnNode field) return opcode + " " + field.owner + "." + field.name + " " + field.desc;
        if (node instanceof TypeInsnNode type) return opcode + " " + type.desc;
        if (node instanceof JumpInsnNode jump) {
            AbstractInsnNode target = jump.label;
            while (target != null && target.getOpcode() < 0) target = target.getNext();
            return opcode + " " + code.indexOf(target);
        }
        if (node instanceof InvokeDynamicInsnNode dynamic) return opcode + " " + dynamic.name + dynamic.desc;
        throw new AssertionError("Unexpected instruction operand: " + node.getClass().getName());
    }

    private static List<String> tokens(MethodNode method) {
        List<AbstractInsnNode> code = instructions(method);
        return code.stream().map(node -> node.getType() == AbstractInsnNode.INSN ? Printer.OPCODES[node.getOpcode()] : token(node, code)).toList();
    }

    private static MethodNode method(ClassNode owner, String name) {
        List<MethodNode> matches = owner.methods.stream().filter(value -> value.name.equals(name)).toList();
        require(matches.size() == 1, "Expected unique method " + owner.name + "." + name);
        return matches.get(0);
    }

    private static MethodNode suffix(ClassNode owner, String suffix) {
        List<MethodNode> matches = owner.methods.stream().filter(value -> value.name.endsWith(suffix)).toList();
        require(matches.size() == 1, "Expected unique transformed helper " + suffix);
        return matches.get(0);
    }

    private static void exact(MethodNode method, List<String> expected) {
        List<String> actual = tokens(method);
        require(actual.equals(expected), method.name + " opcode graph differs: " + actual);
        require(method.tryCatchBlocks.isEmpty(), method.name + " has unexpected exception edges");
    }

    private static List<String> factoryGraph() {
        return List.of("ALOAD 1", "INVOKEVIRTUAL " + STACK + ".getItem()L" + ITEM + ";", "ASTORE 3", "ALOAD 0",
            "INVOKESTATIC " + HOOKS + ".getCurioFromRegistry(L" + ITEM + ";)Ljava/util/Optional;", "ACONST_NULL",
            "INVOKEVIRTUAL java/util/Optional.orElse(Ljava/lang/Object;)Ljava/lang/Object;", "CHECKCAST " + CURIO_ITEM,
            "ASTORE 4", "ALOAD 4", "IFNONNULL 19", "ALOAD 3", "INSTANCEOF " + CURIO_ITEM, "IFEQ 19", "ALOAD 3",
            "CHECKCAST " + CURIO_ITEM, "ASTORE 5", "ALOAD 5", "ASTORE 4", "ALOAD 4", "IFNULL 31", "ALOAD 4", "ALOAD 1",
            "INVOKEINTERFACE " + CURIO_ITEM + ".hasCurioCapability(L" + STACK + ";)Z", "IFEQ 31", "NEW " + WRAPPER,
            "DUP", "ALOAD 4", "ALOAD 1", "INVOKESPECIAL " + WRAPPER + ".<init>(L" + CURIO_ITEM + ";L" + STACK + ";)V",
            "ARETURN", "ACONST_NULL", "ARETURN");
    }

    private static InvokeDynamicInsnNode factorySite(ClassNode curios) {
        List<InvokeDynamicInsnNode> sites = instructions(method(curios, "registerCaps")).stream()
            .filter(node -> node instanceof InvokeDynamicInsnNode).map(node -> (InvokeDynamicInsnNode)node)
            .filter(node -> node.desc.equals(PROVIDER_DESC)).toList();
        require(sites.size() == 1, "Expected unique original item provider factory site");
        return sites.get(0);
    }

    private static void bootstrap(InvokeDynamicInsnNode site) {
        require(site.name.equals("getCapability"), "Wrong SAM name");
        require(site.bsm.equals(new Handle(H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory",
            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false)), "Wrong lambda bootstrap");
        require(site.bsmArgs.length == 3, "Unexpected lambda bootstrap arguments");
        require(site.bsmArgs[0].equals(Type.getMethodType("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")), "Wrong erased provider SAM");
        require(site.bsmArgs[1].equals(new Handle(H_INVOKESTATIC, CURIOS, "lambda$registerCaps$2", FACTORY_DESC, false)), "Wrong original factory implementation handle");
        require(site.bsmArgs[2].equals(Type.getMethodType("(L" + STACK + ";Ljava/lang/Void;)L" + CURIO + ";")), "Wrong concrete provider SAM");
    }

    private static void originalRegistration(ClassNode curios) {
        MethodNode registration = method(curios, "registerCaps");
        require(registration.tryCatchBlocks.isEmpty(), "Unexpected registration exception edges");
        List<AbstractInsnNode> code = instructions(registration);
        InvokeDynamicInsnNode site = factorySite(curios);
        int index = code.indexOf(site);
        List<String> expected = List.of("ALOAD 1",
            "GETSTATIC top/theillusivec4/curios/api/CuriosCapability.ITEM Lnet/neoforged/neoforge/capabilities/ItemCapability;",
            "ALOAD 3", "INVOKEDYNAMIC getCapability" + PROVIDER_DESC, "ICONST_1", "ANEWARRAY net/minecraft/world/level/ItemLike",
            "DUP", "ICONST_0", "ALOAD 3", "AASTORE", "INVOKEVIRTUAL " + EVENT + ".registerItem(Lnet/neoforged/neoforge/capabilities/ItemCapability;Lnet/neoforged/neoforge/capabilities/ICapabilityProvider;[Lnet/minecraft/world/level/ItemLike;)V");
        List<String> actual = code.subList(index - 3, index + 8).stream()
            .map(node -> node.getType() == AbstractInsnNode.INSN ? Printer.OPCODES[node.getOpcode()] : token(node, code)).toList();
        require(actual.equals(expected), "Factory capture and actual item-array operand identities differ");
        bootstrap(site);
    }

    private static void registry(ClassNode hooks) {
        exact(method(hooks, "getCurioFromRegistry"), List.of("GETSTATIC " + HOOKS + ".REGISTRY Ljava/util/Map;", "ALOAD 0",
            "INVOKEINTERFACE java/util/Map.get(Ljava/lang/Object;)Ljava/lang/Object;", "CHECKCAST " + CURIO_ITEM,
            "INVOKESTATIC java/util/Optional.ofNullable(Ljava/lang/Object;)Ljava/util/Optional;", "ARETURN"));
        MethodNode redirected = suffix(hooks, "$ftboss$trackedOriginalRegistry");
        exact(redirected, List.of("INVOKESTATIC " + TRACKED + ".originalRegistry()Ljava/util/concurrent/ConcurrentHashMap;", "ARETURN"));
        List<AbstractInsnNode> initializer = instructions(method(hooks, "<clinit>"));
        require(initializer.get(0) instanceof MethodInsnNode call && call.getOpcode() == INVOKESTATIC
            && call.owner.equals(HOOKS) && call.name.equals(redirected.name) && call.desc.equals("()Ljava/util/concurrent/ConcurrentHashMap;"), "Original registry allocation was not redirected");
        require(initializer.get(1).getOpcode() == DUP && initializer.get(2).getOpcode() == IFNONNULL, "Unexpected redirected-constructor null guard");
        AbstractInsnNode target = ((JumpInsnNode)initializer.get(2)).label;
        while (target.getOpcode() < 0) target = target.getNext();
        require(target instanceof FieldInsnNode field && field.getOpcode() == PUTSTATIC && field.owner.equals(HOOKS)
            && field.name.equals("REGISTRY") && field.desc.equals("Ljava/util/Map;"), "Redirected original registry not installed directly");
        long writes = initializer.stream().filter(node -> node instanceof FieldInsnNode field && field.getOpcode() == PUTSTATIC
            && field.owner.equals(HOOKS) && field.name.equals("REGISTRY")).count();
        require(writes == 1, "Multiple original registry initializer writes");
    }

    private static void eventHooks(ClassNode event) {
        MethodNode before = suffix(event, "$ftboss$originalProviderBefore");
        MethodNode after = suffix(event, "$ftboss$originalProviderAfter");
        for (MethodNode helper : List.of(before, after)) {
            List<String> actual = tokensPrefix(helper, 5);
            require(actual.equals(List.of("ALOAD 1", "ALOAD 2", "ALOAD 3", helper == before ? "ICONST_1" : "ICONST_0",
                "INVOKESTATIC " + OBSERVATION + ".nativeRepairProviders(Lnet/neoforged/neoforge/capabilities/ItemCapability;Ljava/lang/Object;[Lnet/minecraft/world/level/ItemLike;Z)V")), "Original registration identity arguments differ");
        }
        List<AbstractInsnNode> code = instructions(method(event, "registerItem"));
        long beforeCalls = code.stream().filter(node -> node instanceof MethodInsnNode call && call.owner.equals(EVENT) && call.name.equals(before.name)).count();
        long afterCalls = code.stream().filter(node -> node instanceof MethodInsnNode call && call.owner.equals(EVENT) && call.name.equals(after.name)).count();
        require(beforeCalls == 1 && afterCalls == 1, "Original registerItem does not call both provenance hooks once");
        int additions = 0;
        for (int index = 1; index < code.size(); index++) {
            if (code.get(index) instanceof MethodInsnNode call && call.owner.equals("java/util/List") && call.name.equals("add")) {
                require(code.get(index - 1) instanceof VarInsnNode variable && variable.getOpcode() == ALOAD && variable.var == 2,
                    "Original provider was replaced before insertion");
                additions++;
            }
        }
        require(additions == 1, "Expected exactly one original provider insertion instruction");
    }

    private static List<String> tokensPrefix(MethodNode method, int count) {
        List<AbstractInsnNode> code = instructions(method);
        return code.subList(0, count).stream().map(node -> node.getType() == AbstractInsnNode.INSN
            ? Printer.OPCODES[node.getOpcode()] : token(node, code)).toList();
    }

    private static void passiveAgent(Path classes) throws Exception {
        int returns = 0;
        int registrations = 0;
        try (var paths = Files.walk(classes)) {
            for (Path path : paths.filter(value -> value.toString().endsWith(".class")).toList()) {
                ClassNode owner = read(path);
                for (MethodNode method : owner.methods) {
                    require((method.access & ACC_NATIVE) == 0, "Observer contains native code");
                    List<AbstractInsnNode> code = instructions(method);
                    for (int index = 0; index < code.size(); index++) {
                        AbstractInsnNode node = code.get(index);
                        require(node.getOpcode() != BASTORE, "Observer mutates a byte-array element");
                        if (node instanceof MethodInsnNode call) {
                            require(!List.of("redefineClasses", "retransformClasses", "removeTransformer", "setNativeMethodPrefix", "defineClass").contains(call.name), "Observer can change class definitions");
                            if (call.name.equals("addTransformer")) {
                                require(call.owner.equals("java/lang/instrument/Instrumentation") && call.desc.equals("(Ljava/lang/instrument/ClassFileTransformer;Z)V")
                                    && code.get(index - 1).getOpcode() == ICONST_0, "Observer registers a retransformable transformer");
                                registrations++;
                            }
                        }
                        if (method.name.equals("transform") && node.getOpcode() == ARETURN) {
                            require(code.get(index - 1).getOpcode() == ACONST_NULL, "Observer returns replacement bytes");
                            returns++;
                        }
                    }
                }
            }
        }
        require(returns == 2 && registrations == 1, "Unexpected observer transform return/registration count");
    }

    private static ClassNode read(Path path) throws Exception {
        ClassNode result = new ClassNode();
        new ClassReader(Files.readAllBytes(path)).accept(result, 0);
        return result;
    }

    private static void rejected(Runnable operation, String label, List<String> controls) {
        boolean rejected = false;
        try { operation.run(); } catch (AssertionError expected) { rejected = true; }
        require(rejected, "Negative proof control was accepted: " + label);
        controls.add(label);
    }

    public static void main(String[] arguments) throws Exception {
        Path captureRoot = Path.of(arguments[0]);
        Map<String, ClassNode> actual = new LinkedHashMap<>();
        try (var paths = Files.list(captureRoot)) {
            for (Path path : paths.filter(value -> value.getFileName().toString().matches("capture-[0-9]+\\.xml")).toList()) {
                Properties metadata = new Properties();
                try (var input = Files.newInputStream(path)) { metadata.loadFromXML(input); }
                String name = metadata.getProperty("internalName");
                require(actual.put(name, read(Path.of(metadata.getProperty("classFile")))) == null, "Duplicate definition for " + name);
            }
        }
        require(actual.size() == 8, "Missing actual JVM definitions");
        ClassNode curios = actual.get(CURIOS);
        MethodNode factory = method(curios, "lambda$registerCaps$2");
        require(factory.desc.equals(FACTORY_DESC) && (factory.access & (ACC_STATIC | ACC_SYNTHETIC)) == (ACC_STATIC | ACC_SYNTHETIC), "Unexpected original factory declaration");
        exact(factory, factoryGraph());
        originalRegistration(curios);
        registry(actual.get(HOOKS));
        eventHooks(actual.get(EVENT));
        passiveAgent(Path.of(arguments[1]));
        List<String> controls = new ArrayList<>();
        AbstractInsnNode nullResult = instructions(factory).get(31);
        factory.instructions.set(nullResult, new InsnNode(ICONST_0));
        rejected(() -> exact(factory, factoryGraph()), "replacement-result-instead-of-null", controls);
        factory.instructions.set(instructions(factory).get(31), nullResult);
        JumpInsnNode jump = (JumpInsnNode)instructions(factory).get(20);
        LabelNode target = jump.label;
        jump.label = ((JumpInsnNode)instructions(factory).get(10)).label;
        rejected(() -> exact(factory, factoryGraph()), "inert-branch-rerouted-to-custom-capability", controls);
        jump.label = target;
        InvokeDynamicInsnNode site = factorySite(curios);
        Object handle = site.bsmArgs[1];
        site.bsmArgs[1] = new Handle(H_INVOKESTATIC, CURIOS, "wrongFactory", FACTORY_DESC, false);
        rejected(() -> bootstrap(site), "same-nest-hidden-provider-different-implementation", controls);
        site.bsmArgs[1] = handle;
        ClassNode hooks = actual.get(HOOKS);
        MethodNode getter = method(hooks, "getCurioFromRegistry");
        InsnNode extra = new InsnNode(NOP);
        getter.instructions.insert(extra);
        rejected(() -> registry(hooks), "registry-lookup-extra-instruction", controls);
        getter.instructions.remove(extra);
        exact(factory, factoryGraph()); originalRegistration(curios); registry(hooks);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("failures", 0);
        report.put("factoryEntire33InstructionGraphAndAllFourConditionalEdgesMatched", true);
        report.put("originalLambdaMetafactoryHandleAndSameItemRegistrationOperandsMatched", true);
        report.put("registryGetterEntireSixInstructionGraphMatched", true);
        report.put("originalRegistryConstructorRedirectDirectInstallationMatched", true);
        report.put("registerItemOriginalProviderInsertionAndBeforeAfterIdentityArgumentsMatched", true);
        report.put("observerBothReturnPathsNullAndNoRedefinitionCallsOrByteArrayStores", true);
        report.put("negativeProofControlsRejected", controls);
        report.put("inertFactoryPath", List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 19, 20, 31, 32));
        report.put("inertCaseAssumptionsVerifiedByActualRuntimeProbe", List.of("actual registered provider and captured item identity", "tracked original registry has no item entry", "actual item is not ICurioItem"));
        report.put("scope", "Recorded isolated run only; no claim for unknown external native agents or future transformed binaries");
        report.put("fullLifecycleAccepted", false); report.put("installable", false);
        Files.writeString(Path.of(arguments[2]), new GsonBuilder().setPrettyPrinting().create().toJson(report) + "\n", StandardCharsets.UTF_8);
    }
}
