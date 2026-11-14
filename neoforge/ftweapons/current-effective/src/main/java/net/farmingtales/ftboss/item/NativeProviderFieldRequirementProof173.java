package net.farmingtales.ftboss.item;

import java.util.Arrays;
import java.util.List;

final class NativeProviderFieldRequirementProof173 {
    private NativeProviderFieldRequirementProof173() {
    }

    static boolean complete(String[] originalRequirement) {
        if (originalRequirement == null) return false;
        List<String> expected = List.of("net.farmingtales.ftboss.mixin.NativeExpectedProviderFieldsMixin", "net.minecraftforge.common.capabilities.CapabilityProvider", "ftboss$rawDispatcher", "ftboss$rawCapsValid", "ftboss$rawCapsLazy", "ftboss$rawCapsInitialized", "ftboss$rawCapsParentSupplier", "ftboss$rawCapsLazyData");
        return Arrays.asList(originalRequirement).equals(expected)
            && OwnerFieldCoverageRegistry.nativeCapabilityCertified170()
            && stackFields();
    }

    private static boolean stackFields() {
        return OwnerFieldCoverageRegistry.nativeOriginalStackFieldsRequirementCertified172(30);
    }
    static boolean completeRawExposure189(String[] originalRequirement) {
        if (originalRequirement == null) return false;
        List<String> expected = List.of("net.farmingtales.ftboss.mixin.OwnerRawFieldExposureMixin", "net.minecraftforge.common.capabilities.CapabilityProvider", "ftboss$ownerRawExposed");
        return Arrays.asList(originalRequirement).equals(expected)
            && OwnerFieldCoverageRegistry.nativeCapabilityCertified170()
            && providerFields189();
    }

    private static boolean providerFields189() {
        return OwnerFieldCoverageRegistry.nativeOriginalProviderFieldsRequirementCertified173(28);
    }

    static boolean completeRevocationNode191(String[] originalRequirement) {
        if (originalRequirement == null) return false;
        List<String> expected = List.of("net.farmingtales.ftboss.mixin.OwnerRevocationNodeMixin", "net.minecraftforge.common.capabilities.CapabilityProvider", "ftboss$ownerRevocationNode", "ftboss$ownerInstallRevocationNode");
        return Arrays.asList(originalRequirement).equals(expected)
            && OwnerFieldCoverageRegistry.nativeCapabilityCertified170()
            && providerFields191();
    }
    private static boolean providerFields191() {
        return OwnerFieldCoverageRegistry.nativeOriginalProviderFieldsRequirementCertified173(28);
    }

    static boolean completeDispatcherNode193(String[] originalRequirement) {
        if (originalRequirement == null) return false;
        List<String> expected = List.of("net.farmingtales.ftboss.mixin.OwnerRevocationNodeMixin", "net.minecraftforge.common.capabilities.CapabilityDispatcher", "ftboss$ownerRevocationNode", "ftboss$ownerInstallRevocationNode");
        return Arrays.asList(originalRequirement).equals(expected)
            && OwnerFieldCoverageRegistry.nativeCapabilityCertified170()
            && dispatcherFields193();
    }
    private static boolean dispatcherFields193() {
        return OwnerFieldCoverageRegistry.nativeOriginalProviderFieldsRequirementCertified173(28);
    }

}
