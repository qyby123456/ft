package net.farmingtales.ftboss.item;

import java.util.Arrays;
import java.util.List;

final class NativeCapabilityRequirementProof171 {
    private NativeCapabilityRequirementProof171() {
    }

    static boolean complete(String[] originalRequirement) {
        if (originalRequirement == null) return false;
        List<String> expected = List.of("net.farmingtales.ftboss.mixin.OwnerRepairCapabilityFieldsMixin", "net.minecraftforge.common.capabilities.CapabilityProvider", "ftboss$ownerRepairInitializedNoProviders");
        return Arrays.asList(originalRequirement).equals(expected)
            && OwnerFieldCoverageRegistry.nativeCapabilityCertified170();
    }
}
