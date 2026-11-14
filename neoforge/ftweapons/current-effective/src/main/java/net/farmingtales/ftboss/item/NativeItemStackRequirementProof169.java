package net.farmingtales.ftboss.item;

import java.util.Arrays;
import java.util.List;

final class NativeItemStackRequirementProof169 {
    private NativeItemStackRequirementProof169() {
    }

    static boolean complete(String[] originalRequirement) {
        if (originalRequirement == null) return false;
        List<String> expected = List.of("net.farmingtales.ftboss.mixin.OwnerFullRepairItemStackMixin", "net.minecraft.world.item.ItemStack", "ftboss$fullRepairVoidMutation", "ftboss$fullRepairReturnedMutation", "ftboss$fullRepairAlias", "ftboss$fullRepairGetOrCreateTag", "ftboss$fullRepairGetOrCreateElement");
        return Arrays.asList(originalRequirement).equals(expected)
            && OwnerFieldCoverageRegistry.nativeItemStackCertified165()
            && OwnerFieldCoverageRegistry.nativeComponentsCertified();
    }
}
