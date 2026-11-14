package ftboss.storage308;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class Coverage425 {
    private static Object field(Class<?> type, String name) throws Exception {
        var field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(null);
    }

    private static Method method(Class<?> type, String name, Class<?>... parameters) throws Exception {
        Method result = type.getDeclaredMethod(name, parameters);
        result.setAccessible(true);
        return result;
    }

    public static void run(Map<String, Object> row) throws Exception {
        Class<?> registry = Class.forName("net.farmingtales.ftboss.item.OwnerFieldCoverageRegistry");
        Class<?> proof = Class.forName("net.farmingtales.ftboss.item.OwnerFieldInstallationProof");
        String[][] requirements = (String[][])field(registry, "REQUIRED");
        boolean[] installed = (boolean[])field(registry, "INSTALLED");
        IMixinInfo[] observed = (IMixinInfo[])field(registry, "OBSERVED");
        ClassNode[] targets = (ClassNode[])field(registry, "TARGETS");
        byte[] stages = (byte[])field(registry, "lateStages");
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("registryInvalid", field(registry, "invalid"));
        report.put("nativeCapabilityCertified170", method(registry, "nativeCapabilityCertified170").invoke(null));
        Map<String, Object> lifecycle = new LinkedHashMap<>();
        for (int index : List.of(8, 9, 60)) lifecycle.put("requirement" + index, method(registry, "nativeOriginalCapabilityLifecycleRequirementCertified202", int.class).invoke(null, index));
        report.put("actualNativeLifecycle202", lifecycle);

        List<Map<String, Object>> checked = new ArrayList<>();
        for (int index = 0; index < requirements.length; index++) {
            Map<String, Object> check = new LinkedHashMap<>();
            check.put("index", index);
            check.put("requirement", List.of(requirements[index]));
            int slot = (Integer)method(proof, "lateSlot", String[].class).invoke(null, (Object)requirements[index]);
            check.put("lateSlot", slot);
            if (slot >= 0 && stages[slot] != 2) {
                check.put("refusal", "late-stage");
                check.put("stage", stages[slot]);
                checked.add(check);
                break;
            }
            boolean nativeAccepted = false;
            for (String name : List.of("nativeCapabilityLifecycleGenerationCertified202", "nativeTridentGenerationCertified168", "nativeItemStackGenerationCertified169", "nativeCapabilityGenerationCertified171", "nativeStackFieldsGenerationCertified172", "nativeProviderFieldsGenerationCertified173", "nativeDispatcherRevocationGenerationCertified193", "nativeProviderRevocationGenerationCertified191", "nativeProviderRawExposureGenerationCertified189", "nativeAssignmentGenerationCertified185")) {
                if (Boolean.TRUE.equals(method(registry, name, int.class).invoke(null, index))) {
                    check.put("nativeAcceptedBy", name);
                    nativeAccepted = true;
                    break;
                }
            }
            if (!nativeAccepted) {
                check.put("installed", installed[index]);
                check.put("observed", observed[index] != null);
                check.put("target", targets[index] != null);
                if (!installed[index] || observed[index] == null || targets[index] == null) {
                    check.put("refusal", "missing-installed-observed-target");
                    checked.add(check);
                    break;
                }
                boolean config = Boolean.TRUE.equals(method(registry, "registeredConfiguration", IMixinInfo.class).invoke(null, observed[index]));
                check.put("registeredConfig", config);
                boolean complete = config && Boolean.TRUE.equals(method(proof, "complete", String[].class, ClassNode.class, ClassNode.class).invoke(null, requirements[index], targets[index], observed[index].getClassNode(0)));
                check.put("wholeProofComplete", complete);
                if (!complete) {
                    check.put("refusal", "config-or-complete");
                    checked.add(check);
                    break;
                }
            }
            checked.add(check);
        }
        report.put("orderedReadChecks", checked);
        report.put("authorityFieldsWritten", false);
        report.put("generationForged", false);
        row.put("coverageNextRead425", report);
    }
}
