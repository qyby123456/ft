package net.farmingtales.ftboss.item;

import java.util.Arrays;
import java.util.List;

final class NativeCapabilityLifecycleRequirement202 {
    private NativeCapabilityLifecycleRequirement202() {
    }

    private static List<String> requirement(int index) {
        return switch (index) {
            case 8 -> List.of("net.farmingtales.ftboss.mixin.OwnerFullRepairCapabilityDispatcherMixin",
                "net.minecraftforge.common.capabilities.CapabilityDispatcher", "ftboss$fullRepairVoidMutation", "ftboss$fullRepairAlias");
            case 9 -> List.of("net.farmingtales.ftboss.mixin.OwnerFullRepairCapabilityProviderMixin",
                "net.minecraftforge.common.capabilities.CapabilityProvider", "ftboss$fullRepairVoidMutation", "ftboss$fullRepairReturnedMutation", "ftboss$fullRepairAlias");
            case 60 -> List.of("net.farmingtales.ftboss.mixin.NativeExpectedProviderObservationMixin",
                "net.minecraftforge.common.capabilities.CapabilityProvider", "ftboss$originalProviderBefore", "ftboss$originalProviderAfter");
            default -> null;
        };
    }

    static boolean complete(int index, String[] originalRequirement) {
        List<String> expected = requirement(index);
        return expected != null && originalRequirement != null && expected.equals(Arrays.asList(originalRequirement))
            && OwnerFieldCoverageRegistry.nativeCapabilityCertified170()
            && OwnerFieldCoverageRegistry.nativeOriginalStackFieldsRequirementCertified172(30)
            && OwnerFieldCoverageRegistry.nativeOriginalProviderFieldsRequirementCertified173(28)
            && OwnerFieldCoverageRegistry.nativeOriginalProviderRawExposureRequirementCertified189(55)
            && OwnerFieldCoverageRegistry.nativeOriginalProviderRevocationRequirementCertified191(46)
            && OwnerFieldCoverageRegistry.nativeOriginalDispatcherRevocationRequirementCertified193(47);
    }
}
