package net.farmingtales.ftboss.item;

import java.util.Arrays;
import java.util.List;

final class NativeOwnerRequirementProof167 {
    private NativeOwnerRequirementProof167() {
    }

    static boolean complete(int index, String[] originalRequirement) {
        if (originalRequirement == null) return false;
        List<String> expected;
        if (index == 38) {
            expected = List.of("net.farmingtales.ftboss.mixin.OwnerTridentOriginalCopyMixin", "net.minecraft.world.entity.projectile.ThrownTrident", "ftboss$ownerTridentOriginalCopy", "ftboss$ownerTridentLineage", "ftboss$ownerInstallTridentLineage");
        } else if (index == 39) {
            expected = List.of("net.farmingtales.ftboss.mixin.NativeTridentFieldCarrierMixin", "net.minecraft.world.entity.projectile.ThrownTrident", "ftboss$originalTridentStack");
        } else {
            return false;
        }
        return Arrays.asList(originalRequirement).equals(expected) && OwnerFieldCoverageRegistry.nativeTridentCertified();
    }
}
