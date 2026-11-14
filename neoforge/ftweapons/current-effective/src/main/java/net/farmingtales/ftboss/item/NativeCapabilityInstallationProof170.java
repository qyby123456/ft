package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.HexFormat;
import java.security.MessageDigest;
import net.farmingtales.ftboss.boss.ai.NativeBossLivingComposerSupportV2;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicVerifier;

final class NativeCapabilityInstallationProof170 implements Opcodes {
    private static final String PREFIX = "net/farmingtales/ftboss/mixin/";
    private static final String CALLBACK = "org/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable";
    private static final List<String> HOSTS = List.of("net/minecraft/world/item/ItemStack", "net/neoforged/neoforge/capabilities/ItemCapability", "net/neoforged/neoforge/capabilities/RegisterCapabilitiesEvent");
    private static final String REGISTER = "registerItem(Lnet/neoforged/neoforge/capabilities/ItemCapability;Lnet/neoforged/neoforge/capabilities/ICapabilityProvider;[Lnet/minecraft/world/level/ItemLike;)V";
    private static final String PREQUERY_SOURCE_SHA199 = "ea1a57e2ca27ccd4ad192582f51d741c5254c81f4483717816b82a30752b612c";
    private static final Map<String, String> GUARDS = Map.ofEntries(
        Map.entry("FoodWeaponOriginalSequence$Session", "28af8ab6b516d170e0d794ef9e6014dd3ddc6632d35452c7268e9c9b0daab421"),
        Map.entry("FoodWeaponExpectedTrace$Builder", "568f34ca02c50b8f9216827d335aeb39f591ca33da66485f41c60cd169996fbb"),
        Map.entry("NativeFreshFactoryTrust298", "e1a332c2a45a11a948cad0588168f96bf7b69219bc6f3f9a50e35b733a9652ab"),
        Map.entry("OwnerTimerSemanticAuthority$Book", "cdabe5684e06591835441434df16cdd78d78e2e87aabf4215c60cb97dee9affd"),
        Map.entry("transaction/NativeRecipeEvidenceData", "c75f54da8e1fc6770f270ab1babaedf82381adbf98fb3aaf0c736e36f1c49c41"),
        Map.entry("FoodWeaponSourceLedger$PrivateRead423", "b245dd992393df8dd2879c9d9bb81021494213a000d30a31709649116605b7c0"),
        Map.entry("transaction/FoundationApprovedRules", "1f1d433f150a6d37e762baac56415a9b621150f8d598451bdec3a97507f31666"),
        Map.entry("transaction/FoundationApprovedRules$PrivateRead423", "410a39a21645cfad418706ab41fbab379e0388aff4d902c2da4ca339b81b752b"),
        Map.entry("util/foundations/FoundationAuthorRuleSnapshots", "19d5604aae4dcc5b74ef107a96436312a13a996f7a5cc6aacb8878bb9006a52b"),
        Map.entry("util/foundations/FoundationAuthorRuleSnapshots$FrozenRules", "2d0379401e5d686b78002a353a2026bd4a87671f0c378bbcc7203a00bda97ab5"),
        Map.entry("util/foundations/FoundationAuthorRuleSnapshots$MaterialSnapshot", "8846dbf7818a0f22f1f0fa78c21d7693b9ea492ad5ff35bde266667225449ff4"),
        Map.entry("util/foundations/FoundationAuthorRuleSnapshots$PrivateFingerprint416", "66a357c26eb2d50c44c6662181f30408d4696d7bccf0670d20e66e9f6869d31b"),
        Map.entry("util/foundations/FoundationCanonicalDigest$PrivateFingerprint416", "26e535c24c3d90aa570fd815f4a88b43fc0cba30fc253a97c641c8683d7dc3f5"),
        Map.entry("FoodWeaponMaterialFingerprint", "0d369d2d3faf94649657cbda60356121b506717f7b82adbb4c436e42ef1fc6ea"),
        Map.entry("FoodWeaponMaterialFingerprint$PrivateFingerprint416", "e41e28e667c13a28318857eac8e443d15bd82a8367d11021ad57cbb591d7d6db"),
        Map.entry("WeaponSystemScope410", "1a433c97862e647b9167bfe56965984a928b056945bd0952e974af435bb3b3a6"),
        Map.entry("WeaponAttachmentPolicy", "60465f18c6a3598ec95f15001c20b1dd4bebdd1c45a282f6c5d9e3ac14c79ac4"),
        Map.entry("FourWeaponFoundationAdapter", "d5839f7049ff0c5da97ddf86730025aca89f3e086229ea3becea94472aa12bdb"),
        Map.entry("FourWeaponFoundationAdapter$PrivateSnapshot407", "95f35d8cae46f5a2aed0058372ad7f8d4e8f705f60191e3fa691dda9f1f0569b"),
        Map.entry("util/foundations/FoundationCanonicalDigest", "dbe5bce86f1eb241a3846642f5a8923a521fc9739a864be5dc4912dc9fbaffc2"),
        Map.entry("util/foundations/FoundationCanonicalDigest$PrivateCompound", "462c94d806bd1050ebdc1ad7dcfeb165ec3bc995af86a98efbfbb7718c9957d8"),
        Map.entry("NativeComponentMergeBoundary391", "6c68e24593d83fc0ccb423caae9b1116e018e9b9c76db9784550a28b390b5eda"),
        Map.entry("NativeComponentMergeBoundary391$Write", "b3627925f6714639afe909149ad66747321f12865231b8c40944b0aae89b7fe5"),
        Map.entry("NativeExpectedFieldObservation$ReadOps", "3c91e9748fda833c427481ef47138b206a9879259df63543087182b2c4f29cdf"),
        Map.entry("NativeExpectedFieldObservation$ReadBuilder", "7a371a888ae447de79fa76727ab083f36aa529a0307c872abe3a844242c15029"),
        Map.entry("NativeExpectedFieldObservation$ReadRegistries", "5925ba0e161f81b5a5e917e92e6dbd824b4c7b79622cb54b3e5ee7f81300a7e3"),
        Map.entry("NativeFoundationItemData", "0a1faf4ca43396366964cb9ed5b572f124f004d91e7140e6860d9e3b94480511"),
        Map.entry("NativeItemComponentObservation", "636882dd227f5b8cd797098d2a88bb2b14da96539bbd84e3ab39c6eeb30b4659"),
        Map.entry("mixin/NativeItemStackHandlerLifetimeMixin232", "99c1e9ab66a6f724b209affb1c35fff4846eade8f17f618577b47a2a27105aa9"),
        Map.entry("NativeCuriosProviderFields260", "a2c3f97e0b9b3f3b7b8be2194b96792ab3d4095e27e619a1cafec4ede2f61b9f"),
        Map.entry("NativeCuriosProviderFields260$Frame", "234c16d816b03fc599c54d606c0b278a4c40c76408503eb63f99965d15013d38"),
        Map.entry("NativeCuriosProviderFields260$Snapshot", "a77260b5f8aa990c5a393ed2b1f169c5c9d0197ce2f62186cdecc5fd8c3bbdb6"),
        Map.entry("NativeCuriosProviderFields260$State", "31ea0d3b82bf4db47a568da1e05a0c917438585cacd7e822b27df996735877d2"),
        Map.entry("mixin/NativeCuriosProviderStateMixin257", "87eb68d159b058ee76eb6ba7b6c9e4b28101b9bab0030f790900cf792245173b"),
        Map.entry("FoodWeaponBossPassive", "6f68d479553f5fd14abab02279767c07862a8d08a5e7c907ce17c48de3a875fd"),
        Map.entry("FoodSocketPowers", "39d0feb257146f44e4bfe67ce78d25a05dd8549b3939ae5e671a849c73ad539d"),
        Map.entry("WeaponMaterialBuild", "280c88b7d893f6e06981cbb646db795411730c55212086e75b63c4cc3c0bf654"),
        Map.entry("FoodWeaponFinalDamage", "5ad112ab463bb24279c6b022fd8948f258521ba0a3f3349492185e2550e40900"),
        Map.entry("FoodWeaponEvolution", "9a4283e60df47dae3e0623e50ed45f170a136e86405f06886909a73ea906035c"),
        Map.entry("FoodWeaponUseBridge", "4dceb2ab75c1dbfef99aee3eca0cdd3d1547b1c7f273cd41733acf380a8fb8cf"),
        Map.entry("NativeCapabilityLifecycleRequirement202", "e9139dbd30f987c57ac4cbc921a9165faaf599d462cecc719f153abace927086"),
        Map.entry("OwnerFieldCoverageRegistry", "fff451cf420318f8e8e75810632fbaa0b849bcd373ec41ab33593816cddf7a46"),
        Map.entry("NativeOpaqueProviderReadFence201$Watch", "18cc9bb46ffe5162bb12538b25bb3ffc76e5ac2365852889de7cb0e581058408"),
        Map.entry("NativeOpaqueProviderReadFence201$Value", "0752113b7b2130419deba4d93c43421f4395b11f4bb8a637aecfc97cbf06fe23"),
        Map.entry("NativeOpaqueProviderReadFence201$Graph", "e525b05ec00f0cf0451c2acad4028a8e234423556f18f7a32580c98fb729c324"),
        Map.entry("NativeOpaqueProviderReadFence201", "5e20d244fcbdbb1ea7e85f83c44c074a2f4b2566df7248abedd4286a513a54c4"),
        Map.entry("NativePreQueryCapabilityLifetime199$Notification", "0b6ebfe82a8536c7228e7e2875526149d4a6c91e82d5eff687575001311a3352"),
        Map.entry("NativePreQueryCapabilityLifetime199$Watch", "b60de13697d1f2e83ce6c8c7869dfc5827180a917d74fcaf1adcf8699adb9a54"),
        Map.entry("NativePreQueryCapabilityLifetime199", "6d5baa83de72801da898de80b71c72df6fd9e0b8c1a56aad83f0b8a95239d0e7"),
        Map.entry("NativeCapabilityOwnerLinks194", "8d1165fa1b508850c626ecda18e5ef0eb22beadf6a80977677ab075b0ae4be4f"),
        Map.entry("OwnerFieldInstallationCatalogue", "fc7c40d4fdc889ea5e04a3201913fca7d7c463c13ac10feb605cd84e2f53f963"),
        Map.entry("OwnerFieldInstallationProof", "c64178c238eeba29259651668bff7123dd211fad7a6b736bd923a7260b3bc0d3"),
        Map.entry("FoodWeaponSourceLedger$AttemptBatchAdmission", "40ffcb92ea8574bd47ea0af2f4a8f20184248bb3f5bccf9862c96b0a70815859"),
        Map.entry("FoodWeaponSourceLedger$AttemptBatchAdmissionStatus", "ae345ff1cd1d3174b42920064f14dbf175765ae400d40f0395fa3db720634394"),
        Map.entry("FoodWeaponSourceLedger$AttemptBatchCommit", "9dcb1da33e7ddcd2009318317bb9898f26f464e248e08b05e5500a8a1326b06b"),
        Map.entry("FoodWeaponSourceLedger$AttemptBatchCommitStatus", "98d5c2c885c40cb06ddddf761ed25f555237d3a2b7b085d0a568cd95b0345df0"),
        Map.entry("FoodWeaponSourceLedger$AttemptBatchQuote", "bb88c05e589353acc0402a219d41c13059d83cddfa71f53c732acc5bee4abdae"),
        Map.entry("FoodWeaponSourceLedger$AttemptBatchStatus", "f03f33c59a70e7260e43ef9ea07c6999ce7164854ccf6a550187db571063497c"),
        Map.entry("FoodWeaponSourceLedger$AttemptCommit", "42410ca27eb43b30c115885977478836773893d797f802fe2c8574516164ce09"),
        Map.entry("FoodWeaponSourceLedger$AttemptDomain", "c57377181b6948c3187b853b95cb672842750d429347f42f916de27d233e9fe1"),
        Map.entry("FoodWeaponSourceLedger$AttemptQuote", "cccc1e64042325c85bf1f9e12956a0a0297386b35996dd54f49007a88471e579"),
        Map.entry("FoodWeaponSourceLedger$AttemptRequest", "97e42a95c287e69fb8afe10301e278f999db088f179687d8da0bde7d9840970a"),
        Map.entry("FoodWeaponSourceLedger$AttemptResult", "20b9c36c9d2a9d510efb69cc9716ad88f219a17194d1db3e479ebdbfe3bee728"),
        Map.entry("FoodWeaponSourceLedger$AttemptStatus", "d75ee4ecd22a4351085331a3d03cca11efa47ac1387af70b2c4fbb1210bcc920"),
        Map.entry("FoodWeaponSourceLedger$BatchPreparation", "d613b5d22524b61a73e862082c6998dead76e52ffe5e862e9477594c461fb32a"),
        Map.entry("FoodWeaponSourceLedger$Claim", "f607b5d47e80396bd71720f51e64642b6405269c7430c06f7c0e581db1350b57"),
        Map.entry("FoodWeaponSourceLedger$Commit", "6827ae5126f4170199e849bf1849b6e55e0cc9d404adb1f2b5c4edc59fd5ba66"),
        Map.entry("FoodWeaponSourceLedger$DomainKind", "f3945b66264d8b308abc27cac7d3c9007a8f0a0892f887f1623d2a4cc30dffa6"),
        Map.entry("FoodWeaponSourceLedger$EffectDomain", "d39c2bd246d13c5f10ca6bb5fec26742bafbbff114f289e6fc09ccbc9b68a28f"),
        Map.entry("FoodWeaponSourceLedger$FinalListedChildScope", "2a16e39d8e9b1523f255897e54cef7c382106ac8f0f6a0de4e2e9bff37a99093"),
        Map.entry("FoodWeaponSourceLedger$ImageCursor", "4b93d7a50052dd30e636616387813252890eb37c32ae6a32e26d6780457ddb25"),
        Map.entry("FoodWeaponSourceLedger$InteractionRoot", "28783d50bad803434b1246d7ac3b5ad95204b03d8387c77fe6f619bceed3cd68"),
        Map.entry("FoodWeaponSourceLedger$IssuerCut", "d557d1c369f9f04cc76fde948bab88a33a6c739767e0c8efe1e6669ca7fe6d1d"),
        Map.entry("FoodWeaponSourceLedger$NativeCapabilityToken", "b6c8fe0e006caf5ae935ef01109c9043a9ffae6eff71b9e09990390363f3ff16"),
        Map.entry("FoodWeaponSourceLedger$NativeCarrierToken", "8106ed44610a53a8a6c17827507b6ca75d82749f30c671967d7ffb8420bde033"),
        Map.entry("FoodWeaponSourceLedger$NativeEvergaleAction", "b39fbd7766cfb0da3829e65a9b6adaba865aa0d23c16a9304c747958bc5ad45e"),
        Map.entry("FoodWeaponSourceLedger$NativeEvergaleInteraction", "57a5951baed18d6cad421c36908da07f63524d6f13ba51d0fc26209fef572497"),
        Map.entry("FoodWeaponSourceLedger$NativeEvergaleSpawnWitness", "2e2e0c6e2223d90e4615bb54c8a9389ee548526d3d98dd716f00ff90a954132b"),
        Map.entry("FoodWeaponSourceLedger$NativeFailureRecord", "203ffc6abe8d430b661aa2372e9e70cc70ec99cc8767186c7a7b39355efd1e5d"),
        Map.entry("FoodWeaponSourceLedger$NativeHurtScope", "8f069c6422e045562e09bfbf5ed24dcfefbddb6e25bfe758ac60f05528025542"),
        Map.entry("FoodWeaponSourceLedger$NativeIngressKind", "65c827b8210acac9e524bec778e70169e2f17fa15807e3d741ea9effd7709233"),
        Map.entry("FoodWeaponSourceLedger$NativeIngressReceipt", "836db6acb4e42f55caac3e2e93a07685156a12754181f0b037baacac52f5d29f"),
        Map.entry("FoodWeaponSourceLedger$NativeIngressToken", "e1c7abc6a38741e992481156a6880e8a4df7befa713021b93e87b0f52013a3c2"),
        Map.entry("FoodWeaponSourceLedger$NativeIronCallbackToken", "ecf20895e1d1f52004aba572f5a14e98985a7a64c4c38b505fe28fd811f63187"),
        Map.entry("FoodWeaponSourceLedger$NativeIronCastToken", "1b62b77db96832d31b5bb7e97f619ab8b427bf6125d5b6ab5e091b163e922aa1"),
        Map.entry("FoodWeaponSourceLedger$NativeIronPaymentToken", "a4ea1f740bc50cd801ec5dd15c44427adf127b8abea75bca554203dfdd8ec697"),
        Map.entry("FoodWeaponSourceLedger$NativeIronReceipt", "6911d2a766b978c95407865487393f7ab063d7e78672ad26fb08d81462a10395"),
        Map.entry("FoodWeaponSourceLedger$NativeIronUseToken", "d93428bf3bf85efde82e47cb5fdde0d9ecc5e4995e255cd6067f61d97817fc48"),
        Map.entry("FoodWeaponSourceLedger$NativeIssuerCustody", "13e2b25f70e6ff9e76d3172fb856b37762f50a5b5fdad488c183f782296fede6"),
        Map.entry("FoodWeaponSourceLedger$NativeIssueWindow", "516f68c3714993f6bc853a77fa70dee95f98d6d022f6de0587952031735dc8d2"),
        Map.entry("FoodWeaponSourceLedger$NativeKind", "023fd74851f1c025eced7134dbca63abd38a8cbd8975e790fefbac142aa7131c"),
        Map.entry("FoodWeaponSourceLedger$NativeMaterialCapture", "3b718961a170edc02bf328ddac3984eaaf9d7542fe1675c63ffe64becee6cdf0"),
        Map.entry("FoodWeaponSourceLedger$NativeMaterialPhase", "914957ccb838b0bff32722d9c1012c0317d9047e2367d8a11394c445d0b5fdd9"),
        Map.entry("FoodWeaponSourceLedger$NativeMaterialPublication", "b10990d03e5ddaa5dc3609884edadf71ab86ebf6b2ec4cfa2421ae832251808b"),
        Map.entry("FoodWeaponSourceLedger$NativeMaterialTransfer", "c5805f3aefc4db05cb72c399e672324b3736831adb8b7a2c07a7abfb35c0604a"),
        Map.entry("FoodWeaponSourceLedger$NativeObservationCut", "3855038777b9c22161feaaa6dd95996b033cbd7822c0bbc8016478acd18b71f2"),
        Map.entry("FoodWeaponSourceLedger$NativeOperation", "a5020f6b14a288b59c9ff3d1d37477dfc3e361def1c5816b0bcdd5863d6819cd"),
        Map.entry("FoodWeaponSourceLedger$NativePaidWeapon", "d8b9a078411ae78a55db6e8214d7fce3b7eb7e1d033ad8feb6d6f255ebc4b679"),
        Map.entry("FoodWeaponSourceLedger$NativeProjectileInteraction", "f4546b27b2fdb3ac3193efc292ff101c52c8f7c66a74d6e36422d17b0297b661"),
        Map.entry("FoodWeaponSourceLedger$NativeProjectileObservation", "52bd0cfbef6d0971c3d71f9fd61ca1d1304fafbd826a3dc30b2208fe31f4e03f"),
        Map.entry("FoodWeaponSourceLedger$NativeReceipt", "1f9d0eca73794b8e4511e2a42e7ed9a721639513730485e7621c20471ab185f0"),
        Map.entry("FoodWeaponSourceLedger$NativeRootObservation", "e2b5a011a0a8562d2f7fd6bf99b6be4743c1b57c02aa5204602f690fd7f14097"),
        Map.entry("FoodWeaponSourceLedger$NativeWearToken", "434655f4c120a8d4775e25d3864eed0ae18a548f0cc1d16f8f919d79e83259b1"),
        Map.entry("FoodWeaponSourceLedger$ObserverFailures", "c3a69980de2accc1f1e27c30bc82465f889ffed5b1a752dba1784f15c07c37dd"),
        Map.entry("FoodWeaponSourceLedger$OriginalInteractionBinding", "6e2301c3b16dfcbd227843bdcab73670326836d75aec4c1cf617e80febbfcbc9"),
        Map.entry("FoodWeaponSourceLedger$OriginalRepairCall", "3a4b4648b35f596cf584722e6b5193d8b34f749aeb12f97ca272b14176f0f761"),
        Map.entry("FoodWeaponSourceLedger$OwnerFieldSequenceBinding", "fac42c3273e1cadcf56dd69738df3e4322337ab9cac3b4ab648c726be27e4ea4"),
        Map.entry("FoodWeaponSourceLedger$OwnerPlayerStep", "ad634a8d0c5283ebad3485ec5b05fcf4a43d9f29c97829b7ab03aa38e934601d"),
        Map.entry("FoodWeaponSourceLedger$OwnerPlayerWriter", "34b30998f7829cff16aa1ab5f5132f205a689ee871fcc3445e25fa017f597545"),
        Map.entry("FoodWeaponSourceLedger$OwnerRepairObservation", "05436991330987d1331f59c326a9a5b182abb91fa04840161084b1700cafb5bf"),
        Map.entry("FoodWeaponSourceLedger$OwnerState", "d6f0ecf36422719394cf0764aab5473729b76ea6bba586778d23a671240aa820"),
        Map.entry("FoodWeaponSourceLedger$OwnerWriterKind", "9a5726ed20a4aeec7d88a83d33342286b2583d23e23ad9108b3e774b111d3e2a"),
        Map.entry("FoodWeaponSourceLedger$OwnerWriterPublication", "c9a9fe9a58390319cf160a0bf6bf9f5c2829fac14a1ae8f4102837912da4a5ab"),
        Map.entry("FoodWeaponSourceLedger$OwnerWriterStep", "7783befe2de0c4d61ae07259ab8463f49b13a067aa2eb72819e22e8a5a220a00"),
        Map.entry("FoodWeaponSourceLedger$PendingEchoInteraction", "5fbf5911b40ff167b2f2e3687a5e61a9f7096f3db771b86a496da1c933e9d4b6"),
        Map.entry("FoodWeaponSourceLedger$PendingEchoLineage", "db4928e79ff50d5484324cfe43af477c5ccf502be587e6fc001c4ba0ade83b71"),
        Map.entry("FoodWeaponSourceLedger$PlayerImageDelta", "3014717ed15d210fd06360a6f53735978b2dc9ec4dcf01c14241e3a9778b5bdf"),
        Map.entry("FoodWeaponSourceLedger$PlayerImagePlan", "1391b2e73711734a787f28f2b9430a4d74456819a43b31dbed3f83accf20b3ed"),
        Map.entry("FoodWeaponSourceLedger$PlayerWriterArgument", "af20bf67d07e7550a4b66a5ad96f5698be3c49bf0862b8f2d4e4de442f7462b3"),
        Map.entry("FoodWeaponSourceLedger$PlayerWriterArgumentKind", "29a02324921ece905bb932e29eee51f3a3a59e8d8a0cb02a36188082c77b7dbf"),
        Map.entry("FoodWeaponSourceLedger$PrimaryReadWindow", "239437dab77f4ba3bc05c89c33b586edadb8dd5b8264be99dc2b4fca4090d37c"),
        Map.entry("FoodWeaponSourceLedger$ReadAliases", "00f2147155fff345bef1b3d3cba4893dd9fd45200bc02626a37e14b61fbdbd0a"),
        Map.entry("FoodWeaponSourceLedger$ReadCut", "daa67adcb622fcbb1a203996d87bb8d043147d21ed9833c4ad204132b4a591f3"),
        Map.entry("FoodWeaponSourceLedger$ReadGeneration", "abf21902508b60e037852f3396ec1684d07707b7ccf4854c3d452651e1d6892c"),
        Map.entry("FoodWeaponSourceLedger$RepairOriginalTrace", "1a6e1b099bd2ad22be0974286e6f3c5822454b10f096f0a9173537112c378a3a"),
        Map.entry("FoodWeaponSourceLedger$RootSite", "2c12b9853accbc4a66b76ab847e7e88b675e98537cd091eb3e1396d01f217e7c"),
        Map.entry("FoodWeaponSourceLedger$ServerState", "ebd5a79fcb7ef518c8ae23bfc9fb1c0bf6bfe5b3a24209ffbd319bf30a0bfa44"),
        Map.entry("FoodWeaponSourceLedger$SourceTicket", "097bce762226aba96632fce555ce43fd3f6e156931306d68f6b79278183231aa"),
        Map.entry("FoodWeaponSourceLedger$TargetCommit", "9fc79089ade09dfa8fde80af4a39bc5d9fb88d9ebe55abc604862a5243eb4bec"),
        Map.entry("FoodWeaponSourceLedger$TargetEffectDomain", "d3d71b8797e4e056c430a9fa3e8abeca159a24d75e23227724c81cfe614a748a"),
        Map.entry("FoodWeaponSourceLedger$TargetQuote", "285e780c1c3845a0da6b44be4a271833e0305219436234d7839c40b02471f8a7"),
        Map.entry("FoodWeaponSourceLedger$TargetRequest", "5c5f0e093f9e9fb0c971a87676c14cf2a94ce298d837b4ab164aec8012012891"),
        Map.entry("FoodWeaponSourceLedger$TargetResult", "cb2d8ef3e0e999a8893988c005d39bb6b25e681fffd5430d8a121a9a56595448"),
        Map.entry("FoodWeaponSourceLedger$TargetStatus", "8339c7ba8fe3ec4861659c35889515936b9f44ed390133303a24d401c5f3ae78"),
        Map.entry("FoodWeaponSourceLedger$TicketState", "04fc8c9aeb4219cb869babf1184b6f07fc137c72b71dca7b241665073c6f98d8"),
        Map.entry("FoodWeaponSourceLedger$TridentCarrierLineage", "423b0e71146daabc1fec1e1709253bddea83a9908acc7cbaa99eee08c01d11d9"),
        Map.entry("FoodWeaponSourceLedger$TridentTicketBefore", "762b2d1857147f0643ea8569bd00996ba6d92df8d64765398184902db1e163a1"),
        Map.entry("FoodWeaponSourceLedger$WeaponExpectation", "55ec34ce5f81a95ba8be9b51511d8fdae273c3de486bfc39dbe18160b8a096bb"),
        Map.entry("FoodWeaponSourceLedger$WriterPlanningCut", "44c17f4bf7e11ba3a8adf9ebba5bdd10485ca39a8473a472fb3bb2b56a14c922"),
        Map.entry("FoodWeaponSourceLedger$WriterSelection", "9b31d7cd04ebbd93e19cf7de279c902a4fe1a5a54f04a702df2f0ea41b530c65"),
        Map.entry("FoodWeaponSourceLedger", "2ba9168d4a1299f3d31c20e0fa33b2ea265d0d536d6c046d87db2d904367d0e1"),
        Map.entry("NativeObservationCarrier", "846e3294b6b46406a7a95dac985b31d0e416a445f83d289f87c8ff3614b5f392"),
        Map.entry("NativeObservationFrame", "4bc18739e8662b415bc18e6454b0b64bebeddf18ddc85ead3d7e58ac645b2b98"),
        Map.entry("NativeObservationState", "ab7fd903b050c1e150f25de9ffb689b9a5d444acd6085b717d39e6fb349288e8"),
        Map.entry("NativeReadCutObserver", "ef9c157f4ad541d3eb4e26a69d1d9ba2b4661f039de3653c32a20c80287643e9"),
        Map.entry("OwnerFieldExpectedAuthority$DomainBefore", "46d568e998f0c92cf1f1c8737b7b406fcdc9012c66916aced7e44aa7b65f8610"),
        Map.entry("OwnerFieldExpectedAuthority$Prepared", "a043c443b58d994f7b73cec4417f75fefeae96eef3f43eb6d86c1d6c30bdc88c"),
        Map.entry("OwnerFieldExpectedAuthority$TimerQuote", "5b403a700a0dcecda728ee159ccd1a6856d485a6c85051c12e5d017dffba93b8"),
        Map.entry("OwnerFieldExpectedAuthority", "d85839109dbdb4be410cff65c6376478aa930e55ccd4d4c6fee4c195cf67adec"),
        Map.entry("OwnerFullRepairGuard$AliasHandoff", "4f7abf35e2887652b3c6866837e355e97c17d302c4cfee72301232b417d61699"),
        Map.entry("OwnerFullRepairGuard$Cut", "46e99d9d9b213f9a7672733ca14600ee79b779e9e6a45aa6e7c832de0dbcf33a"),
        Map.entry("OwnerFullRepairGuard$Mutation", "1531f2de7183cbb306326b230673b33d71bd4088dc02b72c750830b7951fd54f"),
        Map.entry("OwnerFullRepairGuard$Node", "1d0efb0650fc2c6e23e0030364025c7896219074c168cf3a04f15a7e9d688726"),
        Map.entry("OwnerFullRepairGuard$Record", "acbe6261e24da2fb079c7a1bff624e3bc69e2aab5f3fac3aa3d83e84c2e36351"),
        Map.entry("OwnerFullRepairGuard$StorageView", "91058e2969209651c653a2968fe4a34e1e34254b50174bab55e7b96ec1dc1571"),
        Map.entry("OwnerFullRepairGuard$TickCut", "c3e46dc661d45e1fdc9a0bfb9eed5913839fcee670a8a2b6c1bffd7f9c6e96f0"),
        Map.entry("OwnerFullRepairGuard", "f680b9ffa25bca39ce119f43169c6a64f9bc3ca140ef18fddadf243965dfdfbd"),
        Map.entry("OwnerLineageRevocationStore$Advance", "18d4e089e5e282d78c3ef3292f5499b9218b5aaae956490634d5a0a81b04f786"),
        Map.entry("OwnerLineageRevocationStore$Lease", "56428b63be0b805285fde53b12905bc939e62b95c20298f9b60c089d152c7d7f"),
        Map.entry("OwnerLineageRevocationStore$Record", "bcac53d6a8e74919189738d154e5da7f951c7a1fbb7a314e3374ae1e40170f47"),
        Map.entry("OwnerLineageRevocationStore$TaskReservation", "fef579995ea9ad28a3ce3bf88106909f09ec5af85a416964064b188ca71fd36e"),
        Map.entry("OwnerLineageRevocationStore", "fae400e74c2ee6b10061171a020398eeef1a2ba1c0968dc67e80c302760a1b88"),
        Map.entry("OwnerRepairContinuation$Binding", "a5ae02b35dac173c16ef843258230d8c21d0d4b7df469aa45a81874e80d761ba"),
        Map.entry("OwnerRepairContinuation$CarryWrite", "a592d9ea05d20352df6967d713ad97aec48bec6fc57743d44f8c294775ed92dd"),
        Map.entry("OwnerRepairContinuation$Entry", "31c1cb6bfa2b7b7d8bc3df8c32774928a18fe211f40ae47451d57a98be90e7fb"),
        Map.entry("OwnerRepairContinuation$IdentityRead", "ddd76a83cfdbea4ad71010c0b3e96b656959769fe875cfd2f50cd557ba207edd"),
        Map.entry("OwnerRepairContinuation$Track", "31c63654567f984df2db006c7270c8c055eecfaea7a463e73c218bd45a950a9e"),
        Map.entry("OwnerRepairContinuation$TrackPhase", "4de81b0faa37aa7b4fabd7f090c00df086f7381d30f2a9fe99e2b6af8d334ebf"),
        Map.entry("OwnerRepairContinuation", "5974c3d35e3db47b49da06bf573af8d88b6ed3c75dab8e587752c198d7419ffe"),
        Map.entry("OwnerRepairGenerationAuthority$Generation", "a230e496c7cb7c5be9b66d5218773a1cf9921b32e6150554d45b70887b329e26"),
        Map.entry("OwnerRepairGenerationAuthority$Intent", "64d2cea24d80bf6a4ad46d6e1113a9fb82ba7b09d582899a7bd23dd35667d096"),
        Map.entry("OwnerRepairGenerationAuthority", "3abe4844426c3452cdf510d45ce7ebfdf21fc8f54b06cc3ff82c94f74c3d48c1"),
        Map.entry("OwnerRevocationNodeAccess", "f189570d2d830553b612b46e11b8b8ca257c8a63dae05ab8ca9f86a305b14621"),
        Map.entry("OwnerStatueTaskOrigin$Call", "5eca5cb40ce55d3fe03b6fa91eaf8aee1f0f365066de97435fdd4690e287948e"),
        Map.entry("OwnerStatueTaskOrigin$Phase", "324631c513a816016fad8ecacadf8bdc2c0620e256559107eb5278811bc65d52"),
        Map.entry("OwnerStatueTaskOrigin$ProgressRead", "f3cef116bea2b3b22ca20dd9e1c4b96867e29f75bd55a900c02b346bf881e43f"),
        Map.entry("OwnerStatueTaskOrigin$Publication", "f3769b8e5b818564b37b96f0bc09e1c3c075775f6d3281745422c242e91820cc"),
        Map.entry("OwnerStatueTaskOrigin$PublishedPredecessor", "1cc593bfd24bc14001c9d7c852221b2e83e3f28acfd686deb746fd6698cbdd5b"),
        Map.entry("OwnerStatueTaskOrigin$Session", "a46f6c54e383ce631e613a6c26440c6ff7ad4ad9367e89da1ffe7be279797f1c"),
        Map.entry("OwnerStatueTaskOrigin$Write", "b116c14470bbca83c44ecaa7e2299072c4728dde81d46e9fa7ab37a5b4fac104"),
        Map.entry("OwnerStatueTaskOrigin", "e9048d0c4fae793f8508ccf09ef74b31cb8f0b35eaa1768c518b27b2c6fdf130"),
        Map.entry("OwnerStatueWearAdmission$Accepted", "1631a433452b61c42c2af5b1805f85b55c83f72a5042173b1c9d05a5de5f6106"),
        Map.entry("OwnerStatueWearAdmission$Frame", "5ebe5e4249192555f56d58b9920cbf1853288ace0f2edf768978d3e7edff23e5"),
        Map.entry("OwnerStatueWearAdmission$Load", "e3ebd79f2b34117fbf68f988990c85b2c6f956da3481e5c7400ea5f355b453e6"),
        Map.entry("OwnerStatueWearAdmission$Origin", "6caf2a6cef65d24f18bf7f7272ac555186de7abe82ba5b089f0aac0b829cbc02"),
        Map.entry("OwnerStatueWearAdmission$Phase", "0312a82cc41d8bae9d2c6f76ec657fb632ab87e11716b45c7e6147bc9c0e7217"),
        Map.entry("OwnerStatueWearAdmission$Reading", "25620c4c41d8781a5b84407003a8549833494287943afc47b91f5bbe9692b688"),
        Map.entry("OwnerStatueWearAdmission$Root", "c0f94d9d504b33b62d33eac384720e0e3763ce45de28978e2839df05217ff755"),
        Map.entry("OwnerStatueWearAdmission$Terminal", "7d47699516339a987d7b7cd90a1591824bde8e6cd7be6cac19608ba1026ad8ac"),
        Map.entry("OwnerStatueWearAdmission$Write", "3d69d6060098b8f69a5eccaea2c945ed87a5f28bffbbdcf65cec7aee39d08183"),
        Map.entry("OwnerStatueWearAdmission", "afe790423d41d7430c27f3452ee4305d0d6f53059bdd6a8e91ac286979226a26"),
        Map.entry("NativeExpectedFieldObservation", "f907669ffb3498ccc7d0ab82a8b97665ae683bb289b4c2b06ce91967312e7f92"),
        Map.entry("NativeExpectedFieldObservation$Fact", "8d64615ea31685b7560a3c7c04d28d28208d359190a03b92c82e50d75dcd4ce1"),
        Map.entry("NativeCapabilityRegistrationFields", "46193f006668fa91268ad37c3c03b44ee4b455a6fd534bed48d8655c04bec318"),
        Map.entry("NativeItemCapabilityFields", "b989ab06efc0638b83f497a63c4380500c0f3b44f735b2f655ebcbe7a8cfa6a0"),
        Map.entry("NativeItemCapabilityObservation$Capability", "92bcfb5de7439396458d4094ad112eb6ae4624b24aef1db87af6f05eab20d215"),
        Map.entry("NativeItemCapabilityObservation$Providers", "2c33efb9222fad81096c5a6a5d3ff2f158912d73708b11a950cc89c6b2561588"),
        Map.entry("NativeItemCapabilityObservation", "0a7901dba5b23f5eb154648b6f44c96e06fa6babed45465d9adde7b3c0c9c136"),
        Map.entry("NativeItemCapabilityState", "6444ca24932551c151e3ee3c369202c1f1f0f55d67128af2c63eaed1debedea4"),
        Map.entry("NativeItemComponentFields$CustomData", "86ae7c0231b489d4b7c598cab0c485e4b4d7e2768ed49fffcb5944fa24c5bd9e"),
        Map.entry("NativeItemComponentFields$Stack", "b0707789de9fc1b5f8eb8a03352068d82376af37d06041ed5b81b20bf01ad441"),
        Map.entry("NativeItemComponentFields$Storage", "56aa616d7af73957788b778fff07149984b3931f86c3f2f129df3678db5e09e1"),
        Map.entry("NativeItemComponentFields", "34cfa577e0b88c596b216a4776ea0c95382e2fe21845094606d1c6535a2f626f"),
        Map.entry("NativeItemHandlerLifetime232", "c1fcafe9cd761ea8425b24396b1ab2c43a39817b354a5676c8cd1059f6741df2"),
        Map.entry("NativeItemHandlerLifetime232$Watch", "a2450c88b4b7ffee21031f5bb52ccce7b73e903e1ad693eda61e87b13e6cbcf6"),
        Map.entry("NativeItemHandlerLifetime232$ChildWatch", "1ee02f8f3dfbb69be37ffe27e44d52516dfe4daddf90de80aaf088891fd85039"),
        Map.entry("NativeItemStackInstallationProof165", "27fe94153422f1e1722e59e8c39cc3a003e89cb5a9afa2abf517a64096ecff27"),
        Map.entry("mixin/NativeHandlerChildCountMixin244", "44da2fd2f4de6b9eccb1d0b25a8c567dac6e3b3d58a2901a1658a5bc5e9ae2f7")
    );
    private NativeCapabilityInstallationProof170() {
    }
    static int host(ClassNode target) { return target == null ? -1 : HOSTS.indexOf(target.name); }
    static List<String> required(ClassNode target) {
        return switch (host(target)) {
            case 0 -> List.of("NativeItemComponentStackFieldsMixin", "OwnerRepairCapabilityFieldsMixin", "OwnerRepairStackFieldsMixin", "OwnerRawFieldExposureMixin");
            case 1 -> List.of("NativeItemCapabilityFieldsMixin", "NativeExpectedProviderFieldsMixin", "OwnerRawFieldExposureMixin", "OwnerRevocationNodeMixin");
            case 2 -> List.of("NativeExpectedProviderObservationMixin");
            default -> List.of();
        };
    }
    private static List<String> methods(String family) {
        return switch (family) {
            case "NativeItemComponentStackFieldsMixin" -> List.of("ftboss$nativeItem", "ftboss$nativeCount", "ftboss$nativeComponents", "ftboss$nativeStackMutationVersion", "ftboss$nativeStackCountWrite");
            case "OwnerRepairCapabilityFieldsMixin" -> List.of("ftboss$ownerRepairInitializedNoProviders");
            case "OwnerRepairStackFieldsMixin" -> List.of("ftboss$ownerRepairRawItem", "ftboss$ownerRepairRawCapNbtAbsent");
            case "NativeItemCapabilityFieldsMixin" -> List.of("ftboss$nativeHasItemProviders");
            case "NativeExpectedProviderFieldsMixin" -> List.of("ftboss$rawNativeProviders", "ftboss$nativeCapabilityVersion", "ftboss$nativeCapabilityRegistration", "ftboss$nativeProviderQuery", "ftboss$nativeProviderRepairBefore", "ftboss$nativeProviderRepairAfter", "ftboss$nativeProviderReadFailure", "ftboss$nativeProviderReadObserved", "ftboss$nativeProviderReadBefore", "ftboss$nativeProviderReadFailed", "ftboss$nativeProviderOwnerBefore");
            case "OwnerRawFieldExposureMixin" -> List.of("ftboss$ownerRawExposed");
            case "NativeExpectedProviderObservationMixin" -> List.of("ftboss$originalProviderBefore", "ftboss$originalProviderAfter", "ftboss$nativeProviderRevocationBefore");
            case "OwnerRevocationNodeMixin" -> List.of("ftboss$ownerRevocationNode", "ftboss$ownerInstallRevocationNode");
            default -> throw new IllegalStateException("Unknown authentic source");
        };
    }
    private static void require(boolean value, String reason) { if (!value) throw new IllegalStateException(reason); }
    static ClassNode copy(ClassNode source) { ClassNode target = new ClassNode();source.accept(target);return target; }
    static ClassNode resource(String name) {
        try (var stream = NativeCapabilityInstallationProof170.class.getResourceAsStream("/" + name + ".class")) {
            require(stream != null, "Missing actual compiled authority " + name);ClassNode node = new ClassNode();new ClassReader(stream).accept(node, 0);return node;
        } catch (java.io.IOException failure) { throw new IllegalStateException("Cannot read authentic authority", failure); }
    }
    static void guards() {
        for (var entry : GUARDS.entrySet()) try (var stream = NativeCapabilityInstallationProof170.class.getResourceAsStream("/net/farmingtales/ftboss/" + ((entry.getKey().startsWith("mixin/") || entry.getKey().equals("transaction/NativeRecipeEvidenceData") || entry.getKey().equals("transaction/FoundationApprovedRules") || entry.getKey().equals("transaction/FoundationApprovedRules$PrivateRead423") || entry.getKey().equals("util/foundations/FoundationCanonicalDigest") || entry.getKey().equals("util/foundations/FoundationCanonicalDigest$PrivateCompound") || entry.getKey().equals("util/foundations/FoundationAuthorRuleSnapshots") || entry.getKey().equals("util/foundations/FoundationAuthorRuleSnapshots$FrozenRules") || entry.getKey().equals("util/foundations/FoundationAuthorRuleSnapshots$MaterialSnapshot") || entry.getKey().equals("util/foundations/FoundationAuthorRuleSnapshots$PrivateFingerprint416") || entry.getKey().equals("util/foundations/FoundationCanonicalDigest$PrivateFingerprint416")) ? entry.getKey() : "item/" + entry.getKey()) + ".class")) {
            require(stream != null && HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())).equals(entry.getValue()), "Actual strict physical guard changed: " + entry.getKey());
        } catch (java.io.IOException | java.security.NoSuchAlgorithmException failure) { throw new IllegalStateException("Actual frozen guard unavailable", failure); }
    }
    static String restorePreQuery199(ClassNode target, MethodNode parent) {
        require(target.name.equals(HOSTS.get(2)) && (parent.name + parent.desc).equals(REGISTER), "Exact pre-query registration host and parent");
        String sourceName = PREFIX + "NativePreQueryRegistrationMixin199";
        try (var stream = NativeCapabilityInstallationProof170.class.getResourceAsStream("/" + sourceName + ".class")) {
            require(stream != null && HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(stream.readAllBytes())).equals(PREQUERY_SOURCE_SHA199), "Whole authentic pre-query source including redirect annotation");
        } catch (java.io.IOException | java.security.NoSuchAlgorithmException failure) { throw new IllegalStateException("Pre-query authority unavailable", failure); }
        ClassNode source = resource(sourceName);
        MethodNode original = method(source, "ftboss$nativeLifetimeItem"), actual = method(target, "ftboss$nativeLifetimeItem");
        MethodNode anchor = method(target, "ftboss$originalProviderBefore");
        String session = (String)value(merged(anchor), "sessionId");
        UUID.fromString(session);
        require(original.access == ACC_PRIVATE && actual.access == ACC_PRIVATE, "Private nonstatic pre-query bridge only");
        handler(target, source, "ftboss$nativeLifetimeItem", session);
        int references = 0;
        for (MethodNode owner : target.methods) for (AbstractInsnNode instruction : owner.instructions) {
            if (instruction instanceof MethodInsnNode call && call.owner.equals(target.name) && call.name.equals(actual.name)) {
                require(owner.name.equals(parent.name) && owner.desc.equals(parent.desc) && call.getOpcode() == INVOKESPECIAL && !call.itf && call.desc.equals(actual.desc), "Only original registration may invoke exact bridge");
                references++;
            }
            if (instruction instanceof InvokeDynamicInsnNode dynamic) for (Object argument : dynamic.bsmArgs) {
                require(!(argument instanceof org.objectweb.asm.Handle handle) || !handle.getOwner().equals(target.name) || !handle.getName().equals(actual.name), "No alternate pre-query method handle entry");
            }
            require(!(instruction instanceof LdcInsnNode constant) || !(constant.cst instanceof org.objectweb.asm.Handle handle) || !handle.getOwner().equals(target.name) || !handle.getName().equals(actual.name), "No constant pre-query method handle entry");
        }
        require(references == 1, "Exactly one pre-query bridge invocation");
        List<AbstractInsnNode> code = new ArrayList<>();
        for (AbstractInsnNode instruction : parent.instructions) if (instruction.getOpcode() >= 0) code.add(instruction);
        int position = -1;
        for (int index = 0; index < code.size(); index++) if (code.get(index) instanceof MethodInsnNode call && call.owner.equals(target.name) && call.name.equals(actual.name)) {
            require(position == -1 && call.getOpcode() == INVOKESPECIAL && !call.itf && call.desc.equals(actual.desc), "Unique exact cloned bridge invocation");position = index;
        }
        require(position >= 6, "Complete original receiver spill and captured registration arguments");
        require(code.get(position - 6) instanceof VarInsnNode, "Real original receiver spill");
        int spill = ((VarInsnNode)code.get(position - 6)).var;
        require(spill >= 4 && (parent.maxLocals == spill + 1 || parent.maxLocals == spill + 5), "Exact emitted or live-Mixin reserved local layout");
        int[] opcodes = {ASTORE, ALOAD, ALOAD, ALOAD, ALOAD, ALOAD};
        int[] locals = {spill, 0, spill, 1, 2, 3};
        for (int index = 0; index < 6; index++) require(code.get(position - 6 + index) instanceof VarInsnNode variable && variable.getOpcode() == opcodes[index] && variable.var == locals[index], "Exact original ItemLike spill and original context arguments");
        int spillUses = 0;
        for (AbstractInsnNode instruction : parent.instructions) {
            if (instruction instanceof VarInsnNode variable && variable.var >= spill) { require(variable.var == spill, "No extra pre-query locals");spillUses++; }
            require(!(instruction instanceof IincInsnNode increment) || increment.var < spill, "No pre-query local increments");
        }
        require(spillUses == 2, "Only one original receiver spill and reload");
        parent.instructions.set(code.get(position), new MethodInsnNode(INVOKEINTERFACE, "net/minecraft/world/level/ItemLike", "asItem", "()Lnet/minecraft/world/item/Item;", true));
        for (int index = 0; index < 6; index++) parent.instructions.remove(code.get(position - 6 + index));
        parent.maxLocals = spill;
        return actual.name + actual.desc;
    }
    private static MethodNode method(ClassNode node, String name) { List<MethodNode> values = node.methods.stream().filter(value -> value.name.equals(name) || value.name.endsWith("$" + name)).toList();require(values.size() == 1, "Unique exact method " + name);return values.getFirst(); }
    private static MethodNode key(ClassNode node, String key) { return node.methods.stream().filter(value -> (value.name + value.desc).equals(key)).findFirst().orElseThrow(); }
    private static Object value(AnnotationNode annotation, String key) { int index = annotation.values.indexOf(key);require(index >= 0, "Exact merged key " + key);return annotation.values.get(index + 1); }
    private static AnnotationNode merged(MethodNode method) { require(method.visibleAnnotations != null, "Real Mixin merged annotation");List<AnnotationNode> values = method.visibleAnnotations.stream().filter(value -> value.desc.equals("Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;")).toList();require(values.size() == 1, "Exactly one merged source");return values.getFirst(); }
    private static String shape(MethodNode source) { MethodNode node = NativeBossLivingComposerSupportV2.copy(source);node.visibleAnnotations = null;node.invisibleAnnotations = null;node.visibleTypeAnnotations = null;node.invisibleTypeAnnotations = null;return NativeBossLivingComposerSupportV2.methodFingerprint(node); }
    private static void same(MethodNode actual, MethodNode expected, String reason) { require(actual.maxLocals == expected.maxLocals && shape(actual).equals(shape(expected)), "Whole native body/flags/locals/signature/exception/Throwable shape: " + reason); }
    private static InsnList alias(ClassNode target, MethodNode getter) {
        int local = getter.maxLocals;getter.maxLocals++;
        MethodNode handler = method(target, "ftboss$ownerRawExposed");InsnList code = new InsnList();code.add(new InsnNode(DUP));code.add(new VarInsnNode(ASTORE, local));code.add(new VarInsnNode(ALOAD, 0));code.add(new TypeInsnNode(NEW, CALLBACK));code.add(new InsnNode(DUP));code.add(new LdcInsnNode(getter.name));code.add(new InsnNode(ICONST_0));code.add(new VarInsnNode(ALOAD, local));code.add(new MethodInsnNode(INVOKESPECIAL, CALLBACK, "<init>", "(Ljava/lang/String;ZLjava/lang/Object;)V", false));code.add(new MethodInsnNode(INVOKESPECIAL, target.name, handler.name, handler.desc, false));return code;
    }
    private static void handler(ClassNode target, ClassNode source, String name, String session) {
        MethodNode original = method(source, name), actual = method(target, name);AnnotationNode annotation = merged(actual);
        require(annotation.values.size() == 6 && value(annotation, "mixin").equals(source.name.replace('/', '.')) && value(annotation, "priority").equals(1000) && value(annotation, "sessionId").equals(session), "Exact real source/priority/session for " + name);
        ClassNode remapped = new ClassNode();source.accept(new ClassRemapper(remapped, new SimpleRemapper(source.name, target.name)));MethodNode expected = NativeBossLivingComposerSupportV2.copy(method(remapped, name));expected.name = actual.name;expected.signature = null;
        for (AbstractInsnNode instruction : expected.instructions.toArray()) if ((expected.access & ACC_STATIC) == 0 && instruction instanceof TypeInsnNode cast && cast.getOpcode() == CHECKCAST && cast.desc.equals(target.name) && cast.getPrevious() instanceof VarInsnNode load && load.getOpcode() == ALOAD && load.var == 0) expected.instructions.remove(cast);
        if (name.equals("ftboss$nativeComponents") || name.equals("ftboss$rawNativeProviders")) {
            List<AbstractInsnNode> returns = new ArrayList<>();for (AbstractInsnNode instruction : expected.instructions) if (instruction.getOpcode() == ARETURN) returns.add(instruction);require(returns.size() == 1, "One authentic raw field return");expected.instructions.insertBefore(returns.getFirst(), alias(target, expected));
        }
        same(actual, expected, name);
    }
    private static void field(ClassNode target, String name, String descriptor, Integer flags) {
        List<FieldNode> values = target.fields.stream().filter(value -> value.name.equals(name)).toList();require(values.size() == 1 && values.getFirst().desc.equals(descriptor) && (flags == null || values.getFirst().access == flags), "Exact real native field " + name);
    }
    private static void originalFields(ClassNode target, ClassNode pristine) {
        for (FieldNode original : pristine.fields) { FieldNode actual = target.fields.stream().filter(value -> value.name.equals(original.name)).findFirst().orElseThrow();require(actual.access == original.access && actual.desc.equals(original.desc) && java.util.Objects.equals(actual.signature, original.signature) && java.util.Objects.equals(actual.value, original.value), "Original genuine field retained " + original.name); }
    }
    private static InsnList registration(ClassNode target, String name) {
        MethodNode handler = method(target, name);InsnList code = new InsnList();for (int local = 0; local < 4; local++) code.add(new VarInsnNode(ALOAD, local));code.add(new InsnNode(ACONST_NULL));code.add(new MethodInsnNode(INVOKESPECIAL, target.name, handler.name, handler.desc, false));return code;
    }
    private static void providerQuery(ClassNode target, MethodNode expected) {
        List<MethodInsnNode> calls = new ArrayList<>();
        for (AbstractInsnNode instruction : expected.instructions) {
            if (instruction instanceof MethodInsnNode call && call.getOpcode() == INVOKEINTERFACE
                && call.owner.equals("net/neoforged/neoforge/capabilities/ICapabilityProvider")
                && call.name.equals("getCapability") && call.desc.equals("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;") && call.itf) calls.add(call);
        }
        require(calls.size() == 1, "One exact genuine provider invocation required");
        MethodNode handler = method(target, "ftboss$nativeProviderQuery");
        int local = expected.maxLocals;
        InsnList replacement = new InsnList();
        replacement.add(new VarInsnNode(ASTORE, local + 2));
        replacement.add(new VarInsnNode(ASTORE, local + 1));
        replacement.add(new VarInsnNode(ASTORE, local));
        replacement.add(new VarInsnNode(ALOAD, 0));
        replacement.add(new VarInsnNode(ALOAD, local));
        replacement.add(new VarInsnNode(ALOAD, local + 1));
        replacement.add(new VarInsnNode(ALOAD, local + 2));
        replacement.add(new MethodInsnNode(INVOKESPECIAL, target.name, handler.name, handler.desc, false));
        expected.instructions.insertBefore(calls.getFirst(), replacement);
        expected.instructions.remove(calls.getFirst());
        expected.maxLocals = local + 4;
        expected.maxStack = Math.max(expected.maxStack, 4);
    }
    static void verify(ClassNode target, Map<String, ClassNode> sources, ClassNode pristine) {
        require(host(target) >= 0 && pristine != null && target.name.equals(pristine.name) && sources.keySet().equals(Set.copyOf(required(target))), "Complete actual native target, preimage and source set");guards();
        String session = null;Set<String> newMethods = new HashSet<>();
        for (String family : required(target)) {
            ClassNode source = sources.get(family);require(source != null && source.name.equals(PREFIX + family) && NativeBossLivingComposerSupportV2.fingerprint(source).equals(NativeBossLivingComposerSupportV2.fingerprint(resource(PREFIX + family))), "Whole authentic source with every selector and field " + family);
            for (String name : methods(family)) {
                MethodNode actual = method(target, name);String nextSession = (String)value(merged(actual), "sessionId");UUID.fromString(nextSession);if (session == null) session = nextSession;require(session.equals(nextSession), "Same genuine target transformation session");if (!family.equals("OwnerRevocationNodeMixin")) handler(target, source, name, session);newMethods.add(actual.name + actual.desc);
            }
        }
        if (host(target) == 1) newMethods.addAll(revocationNodes191(target, sources.get("OwnerRevocationNodeMixin"), session));
        if (host(target) == 2) newMethods.add(restorePreQuery199(target, NativeBossLivingComposerSupportV2.copy(key(target, REGISTER))));
        originalFields(target, pristine);
        if (host(target) == 0) {
            field(target, "item", "Lnet/minecraft/world/item/Item;", null);field(target, "count", "I", null);field(target, "components", "Lnet/minecraft/core/component/PatchedDataComponentMap;", null);field(target, "ftboss$nativeStackVersion", "J", ACC_PRIVATE);
            require(target.interfaces.containsAll(List.of("net/farmingtales/ftboss/item/NativeItemComponentFields$Stack", "net/farmingtales/ftboss/item/OwnerRepairCapabilityFields", "net/farmingtales/ftboss/item/OwnerRepairStackFields")), "All native physical stack field interfaces");
            long writes = target.methods.stream().flatMap(parent -> java.util.Arrays.stream(parent.instructions.toArray())).filter(instruction -> instruction instanceof FieldInsnNode written && written.getOpcode() == PUTFIELD && written.owner.equals(target.name) && written.name.equals("ftboss$nativeStackVersion")).count();require(writes == 1, "Only exact saturating count observer writes native version");
        } else {
            Set<String> expectedMethods = new HashSet<>(newMethods);for (MethodNode original : pristine.methods) {
                MethodNode expected = NativeBossLivingComposerSupportV2.copy(original), actual = key(target, original.name + original.desc);expectedMethods.add(original.name + original.desc);
                if (host(target) == 2 && (original.name + original.desc).equals(REGISTER)) {
                    expected.instructions.insert(registration(target, "ftboss$originalProviderBefore"));List<AbstractInsnNode> returns = new ArrayList<>();for (AbstractInsnNode instruction : expected.instructions) if (instruction.getOpcode() == RETURN) returns.add(instruction);require(returns.size() == 1, "Exact original registration completion");expected.instructions.insertBefore(returns.getFirst(), registration(target, "ftboss$originalProviderAfter"));
                }
                if (host(target) == 1 && (original.name + original.desc).equals("getCapability(Lnet/minecraft/world/item/ItemStack;Ljava/lang/Object;)Ljava/lang/Object;")) providerQuery(target, expected);
                if (host(target) == 2 && (original.name + original.desc).equals(REGISTER)) {
                    actual = NativeBossLivingComposerSupportV2.copy(actual);
                    restorePreQuery199(target, actual);
                }
                same(actual, expected, original.name + original.desc);
            }
            require(target.methods.size() == expectedMethods.size() && target.methods.stream().allMatch(value -> expectedMethods.contains(value.name + value.desc)), "No extra native capability or registration method");
            Set<String> interfaces = new HashSet<>(pristine.interfaces);if (host(target) == 1) interfaces.addAll(List.of("net/farmingtales/ftboss/item/NativeItemCapabilityFields", "net/farmingtales/ftboss/item/NativeCapabilityRegistrationFields", "net/farmingtales/ftboss/item/OwnerRevocationNodeAccess"));require(target.interfaces.size() == interfaces.size() && new HashSet<>(target.interfaces).equals(interfaces), "Only proved physical capability interfaces");
            require(target.fields.size() == pristine.fields.size() + (host(target) == 1 ? 2 : 0), "Only actual version field may be added");if (host(target) == 1) {field(target, "providers", "Ljava/util/Map;", ACC_FINAL);field(target, "ftboss$nativeCapabilityRegistrationVersion", "J", ACC_PRIVATE);}
        }
        for (String name : newMethods) try { new Analyzer<>(new BasicVerifier()).analyze(target.name, key(target, name)); } catch (org.objectweb.asm.tree.analysis.AnalyzerException failure) { throw new IllegalStateException("Actual native handler ASM invalid", failure); }
    }
    private static MethodNode revocationMethod191(ClassNode target, MethodNode original) {
        List<MethodNode> candidates = new ArrayList<>();
        for (MethodNode actual : target.methods) {
            if (actual.desc.equals(original.desc) && (actual.name.equals(original.name) || original.name.startsWith("lambda$") && actual.name.contains("$" + original.name + "$"))) candidates.add(actual);
        }
        require(candidates.size() == 1, "Unique real original revocation method including renamed lambda");
        return candidates.getFirst();
    }
    private static Set<String> revocationNodes191(ClassNode target, ClassNode source, String session) {
        require(OwnerFieldInstallationProof.complete(new String[]{source.name.replace('/', '.'), target.name.replace('/', '.'), "ftboss$ownerRevocationNode", "ftboss$ownerInstallRevocationNode"}, target, source), "Complete original guarded revocation installation certificate");
        Set<String> result = new HashSet<>();
        for (MethodNode original : source.methods) {
            if (original.name.equals("<init>")) continue;
            MethodNode actual = revocationMethod191(target, original);
            AnnotationNode annotation = merged(actual);
            require(annotation.values.size() == 6 && value(annotation, "mixin").equals(source.name.replace('/', '.')) && value(annotation, "priority").equals(1000) && value(annotation, "sessionId").equals(session), "Same authentic revocation source, priority and session");
            require(actual.access == original.access && actual.maxLocals == original.maxLocals && java.util.Objects.equals(actual.signature, original.signature) && actual.exceptions.equals(original.exceptions) && OwnerFieldInstallationProof.nativeComponentLambdaBody(original, actual, source, target), "Whole original guarded node and lambda body with exact flags, locals, signatures and exceptions");
            result.add(actual.name + actual.desc);
        }
        require(result.size() == 4, "All four original revocation methods, including both guarded lambdas");
        field(target, "ftboss$ownerRevocationNode", "Lnet/farmingtales/ftboss/item/OwnerFullRepairGuard$Node;", ACC_PRIVATE);
        FieldNode physical = null;
        for (FieldNode candidate : target.fields) if (candidate.name.equals("ftboss$ownerRevocationNode")) physical = candidate;
        int interfaces = 0;
        for (String candidate : target.interfaces) if (candidate.equals("net/farmingtales/ftboss/item/OwnerRevocationNodeAccess")) interfaces++;
        require(physical != null && physical.value == null && physical.signature == null && interfaces == 1, "Unique original private null node field and interface");
        long writes = 0;
        for (MethodNode actual : target.methods) for (AbstractInsnNode instruction : actual.instructions) {
            if (instruction instanceof FieldInsnNode field && field.owner.equals(target.name) && field.name.equals("ftboss$ownerRevocationNode") && field.getOpcode() == PUTFIELD) {
                require(actual.name.equals("ftboss$ownerInstallRevocationNode"), "Only original guarded installer may write node");
                writes++;
            }
        }
        require(writes == 1, "Exactly one guarded original revocation node write");
        return result;
    }

}
