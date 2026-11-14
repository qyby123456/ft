package net.farmingtales.ftboss.item;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

final class NativeStackFieldRequirementProof172 {
    private static final Map<String, String> OBSERVATIONS = Map.ofEntries(
        Map.entry("NativeCapabilityRegistrationFields", "46193f006668fa91268ad37c3c03b44ee4b455a6fd534bed48d8655c04bec318"),
        Map.entry("NativeItemCapabilityObservation$Capability", "92bcfb5de7439396458d4094ad112eb6ae4624b24aef1db87af6f05eab20d215"),
        Map.entry("NativeItemCapabilityObservation$Providers", "2c33efb9222fad81096c5a6a5d3ff2f158912d73708b11a950cc89c6b2561588"),
        Map.entry("NativeItemCapabilityObservation", "0a7901dba5b23f5eb154648b6f44c96e06fa6babed45465d9adde7b3c0c9c136"),
        Map.entry("NativeItemComponentFields$CustomData", "86ae7c0231b489d4b7c598cab0c485e4b4d7e2768ed49fffcb5944fa24c5bd9e"),
        Map.entry("NativeItemComponentFields$Stack", "b0707789de9fc1b5f8eb8a03352068d82376af37d06041ed5b81b20bf01ad441"),
        Map.entry("NativeItemComponentFields$Storage", "56aa616d7af73957788b778fff07149984b3931f86c3f2f129df3678db5e09e1"),
        Map.entry("NativeItemComponentFields", "34cfa577e0b88c596b216a4776ea0c95382e2fe21845094606d1c6535a2f626f"),
        Map.entry("NativeItemComponentObservation", "636882dd227f5b8cd797098d2a88bb2b14da96539bbd84e3ab39c6eeb30b4659"),
        Map.entry("NativeNbtGraphObservation$Node", "27ec7391d927ac87500a80e07eb30c826351af84b5d2d1e3fdb07970bbf30a54"),
        Map.entry("NativeNbtGraphObservation", "d309629913aeb4119ab04f5bf60070ab734de7d59305bb47055aa5a4c23ab35a"),
        Map.entry("NativeNbtMutationFields", "e3ed49ee0ce2a0d22487b90400bc7e78a493017abd7ecd7d9b76417737d25d6e"),
        Map.entry("NativeNestedItemFields$Items", "0e8ea603d8b2bca4dadefb0579ef518c9fa0b29584a12a05a6bb9820097bcee4"),
        Map.entry("NativeNestedItemFields", "aa596ea54cf9974f0eb841c1ff1d89eb619899e78fc04ad78dccaa1269540d1f"),
        Map.entry("NativeNestedItemGraph$Context", "bc6b276937045f97b403e700f6f80e184dd7e3f4e3f04d7dcfb2991278251e9f"),
        Map.entry("NativeNestedItemGraph$Edge", "dd6ec4b6bc5850f87822581e3de8c5f5eabd1c8a830b3e54e6c95f52f658498b"),
        Map.entry("NativeNestedItemGraph", "d97bad8b7a71dde8c681103f70555db393f710bd72c5b5e1de03585030edc294"),
        Map.entry("NativePotionEffectFields$Blend", "e749beee2da342a5c911f22c94bd02fcfd362a0288bac87a4dd9f65eea42567a"),
        Map.entry("NativePotionEffectFields$BlendState", "453d32afe0bb24ad6cfb9cbcdf7b921afc554da744cf0b42a153ae008df035d1"),
        Map.entry("NativePotionEffectFields$Effect", "e7efbdadcab49a692794d19e33d32d08634a62d9558dc34561e5155964d41c6e"),
        Map.entry("NativePotionEffectFields$EffectState", "fc83a27381912075957648d290f0be84f2aa27ef9d63a7119bd2d9cf1f721e93"),
        Map.entry("NativePotionEffectFields$Potion", "3907a455dc8afa21997852c97521eb4ea50153975aa011178ae620bf0d4d4d78"),
        Map.entry("NativePotionEffectFields", "16e42768a9455cc1320c85870026250318ab157287df31bb50ac0a563d6d8b8d"),
        Map.entry("NativePotionEffectGraph$Node", "37af511bfbbcf3f516292a7638d84e8e41f99ea6e2b06f8606491dbd45b761d0"),
        Map.entry("NativePotionEffectGraph", "be99bba433bd3e2550128b4fbb5a0be130c96f487a96f8daa77ffff2481fe3b8"),
        Map.entry("NativeExpectedFieldViews$Actor", "415a9b0bef53b11ddc598d120fc187c0ad602df631d62033644ef0a7a4995e62"),
        Map.entry("NativeExpectedFieldViews$Bytes", "060de3bf85ed076377d9c8b18825c62ffce87a559771c7f36c49d604dc7b027f"),
        Map.entry("NativeExpectedFieldViews$ByteValue", "f67151941a4f6dcc333ca6907af5962a552ac6f9de3b1581dae61e40224a2799"),
        Map.entry("NativeExpectedFieldViews$Compound", "3091ad71870d5191860a437d87675493a2836f6a5aa99a45ed79612edfd0b185"),
        Map.entry("NativeExpectedFieldViews$DoubleValue", "4c0bf95fa95dd91b559bc9cb7ed4efc2acbce24541514160d37b97df24b29527"),
        Map.entry("NativeExpectedFieldViews$FloatValue", "c13379c89963c43d33c21f3af4efc8a39d8aca412c9f6e15e9c6ab23558ca169"),
        Map.entry("NativeExpectedFieldViews$Ints", "565f7efe0148832bfe59588d5170a322863eb725fadfa70e4ed5d3d2946d5169"),
        Map.entry("NativeExpectedFieldViews$IntValue", "0bfb8e624c2014900a123c7d07e60c075ea07ee1211caa1eefb1759d32d3727e"),
        Map.entry("NativeExpectedFieldViews$Longs", "5d07a1cfefa4ead48c58b8d666e1f5022e3d3692c3f8cb6da1b73aef25fa8bfa"),
        Map.entry("NativeExpectedFieldViews$LongValue", "b9345e68ffbaed4086bcb6aa189d16b173a3fabb48965a0f6a7ff035c919afc9"),
        Map.entry("NativeExpectedFieldViews$Provider", "1f1005ffddefb09dda61e47db321ca95964fc54d3c74a87d74f11aab259f4b8a"),
        Map.entry("NativeExpectedFieldViews$Sequence", "9e006f9716369c2fb1734b3177c119b3b1b1f02b7ca41b64ae386b4c0c2be1e5"),
        Map.entry("NativeExpectedFieldViews$ShortValue", "f0c89f5afe44e34976812cf6bb3bdd216fed8dd6aaf93eb408883a16ae28a3c0"),
        Map.entry("NativeExpectedFieldViews$Stack", "ea6c254dbedb2f42c2582a369e946f52522e5342576aa55c93d54b9f12ad4b5c"),
        Map.entry("NativeExpectedFieldViews$TextValue", "a226a52b6b751a41c1034e4522e27d2ff500eb7da6b487e0b1ea3a8b664facbf"),
        Map.entry("NativeExpectedFieldViews", "dac907ff20972777e95f794379b7138f9edddb00db667c7750433a56466e531e"),
        Map.entry("NativeExpectedRawFields", "35dec317de020d4972fac4906882e3689463581c139d6826e4bba533cfb409be")
    );
    private NativeStackFieldRequirementProof172() {
    }

    static boolean complete(String[] originalRequirement) {
        if (originalRequirement == null) return false;
        List<String> expected = List.of("net.farmingtales.ftboss.mixin.NativeExpectedStackFieldsMixin", "net.minecraft.world.item.ItemStack", "ftboss$rawStackTag", "ftboss$rawStackCaps", "ftboss$rawStackCount", "ftboss$rawStackItem");
        return Arrays.asList(originalRequirement).equals(expected)
            && OwnerFieldCoverageRegistry.nativeItemStackCertified165()
            && OwnerFieldCoverageRegistry.nativeComponentsCertified()
            && OwnerFieldCoverageRegistry.nativeCapabilityCertified170()
            && observations();
    }

    private static boolean observations() {
        for (var entry : OBSERVATIONS.entrySet()) {
            try (var stream = NativeStackFieldRequirementProof172.class.getResourceAsStream("/net/farmingtales/ftboss/item/" + entry.getKey() + ".class")) {
                if (stream == null || !HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())).equals(entry.getValue())) return false;
            } catch (IOException | NoSuchAlgorithmException failure) {
                return false;
            }
        }
        return true;
    }
}
