package ftboss.storage308;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class ItemStackProof361 {
    private static Object field(Class<?> owner, String name) throws Exception {
        var field = owner.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(null);
    }

    private static ClassNode copy(ClassNode node) {
        ClassNode result = new ClassNode();
        node.accept(result);
        for (MethodNode method : result.methods) for (var instruction : method.instructions) if (instruction instanceof InvokeDynamicInsnNode dynamic) dynamic.bsmArgs = dynamic.bsmArgs.clone();
        return result;
    }

    private static boolean accepted(Method verifier, ClassNode actual, Map<String, ClassNode> sources, ClassNode pristine) throws Exception {
        try {
            verifier.invoke(null, actual, sources, pristine);
            return true;
        } catch (InvocationTargetException failure) {
            if (failure.getCause() instanceof IllegalStateException) return false;
            throw failure;
        }
    }

    @SuppressWarnings("unchecked")
    public static void run(Map<String, Object> row) throws Exception {
        Class<?> registry = Class.forName("net.farmingtales.ftboss.item.OwnerFieldCoverageRegistry");
        Method certified = registry.getDeclaredMethod("nativeItemStackCertified165");
        certified.setAccessible(true);
        boolean actualCertificate = Boolean.TRUE.equals(certified.invoke(null));
        if (!actualCertificate) throw new AssertionError("Actual retained ItemStack certificate is required");
        ClassNode actual = (ClassNode)field(registry, "nativeItemStackTarget165");
        ClassNode pristine = (ClassNode)field(registry, "nativeItemStackPristine165");
        Method sourceReader = registry.getDeclaredMethod("nativeItemStackSourceNodes165");
        sourceReader.setAccessible(true);
        Map<String, ClassNode> sources = (Map<String, ClassNode>)sourceReader.invoke(null);
        Class<?> proof = Class.forName("net.farmingtales.ftboss.item.NativeItemStackInstallationProof165");
        Method verifier = proof.getDeclaredMethod("verify", ClassNode.class, Map.class, ClassNode.class);
        verifier.setAccessible(true);
        if (!accepted(verifier, actual, sources, pristine)) throw new AssertionError("Actual complete verifier positive failed");
        List<String> selectors = new ArrayList<>();
        for (MethodNode method : sources.get("OwnerFullRepairItemStackMixin").methods) {
            List<AnnotationNode> annotations = new ArrayList<>();
            if (method.visibleAnnotations != null) annotations.addAll(method.visibleAnnotations);
            if (method.invisibleAnnotations != null) annotations.addAll(method.invisibleAnnotations);
            for (AnnotationNode annotation : annotations) if (annotation.desc.endsWith("/Inject;")) {
                int index = annotation.values.indexOf("method");
                if (index >= 0) for (Object value : (List<?>)annotation.values.get(index + 1)) selectors.add((String)value);
            }
        }
        if (selectors.size() != 43 || selectors.stream().distinct().count() != 43) throw new AssertionError("43 genuine parent selectors required");
        List<String> rejected = new ArrayList<>(), admitted = new ArrayList<>();
        for (String selector : selectors) {
            ClassNode changed = copy(actual);
            MethodNode parent = changed.methods.stream().filter(method -> (method.name + method.desc).equals(selector)).findFirst().orElseThrow();
            InsnList extra = new InsnList();
            extra.add(new InsnNode(3));
            extra.add(new InsnNode(87));
            parent.instructions.insert(extra);
            if (accepted(verifier, changed, sources, pristine)) admitted.add(selector);
            else rejected.add(selector);
        }
        ClassNode alteredSource = copy(sources.get("FoodWeaponItemUseMixin"));
        alteredSource.methods.stream().filter(method -> method.name.equals("ftboss$releaseSrg")).findFirst().orElseThrow().instructions.insert(new InsnNode(0));
        Map<String, ClassNode> changedSources = new LinkedHashMap<>(sources);
        changedSources.put("FoodWeaponItemUseMixin", alteredSource);
        boolean sourceRejected = !accepted(verifier, actual, changedSources, pristine);
        row.put("completeParentProofRegression361", Map.of("actualRetainedCertificate", actualCertificate,
            "actualWholeVerifierAccepted", true, "genuineSelectors", selectors.size(), "rejectedParentClones", rejected,
            "admittedParentClones", admitted, "changedAuthenticSourceRejected", sourceRejected,
            "applicationAuthorityWrittenByReflection", false, "fullLifecycleAccepted", false));
        if (!admitted.isEmpty() || !sourceRejected) throw new AssertionError("Whole parent/source tamper accepted " + admitted);
    }
}
