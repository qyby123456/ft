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
        Map.entry("FourWeaponFoundationAdapter", "847532ac106303a545b2a5a04db0a98379963c7a2383abea2dc7dc5777f88629"),
        Map.entry("FourWeaponFoundationAdapter$PrivateSnapshot407", "95f35d8cae46f5a2aed0058372ad7f8d4e8f705f60191e3fa691dda9f1f0569b"),
        Map.entry("util/foundations/FoundationCanonicalDigest", "6abc507e60f7a074d830dbea195faa54151fe61f791118744e1c2f6b67280492"),
        Map.entry("util/foundations/FoundationCanonicalDigest$PrivateCompound", "462c94d806bd1050ebdc1ad7dcfeb165ec3bc995af86a98efbfbb7718c9957d8"),
        Map.entry("NativeComponentMergeBoundary391", "f293931c8fbec84d0d8b7f97d1ce1e347e66c11e9130203194fc3f5e419dd1a0"),
        Map.entry("NativeComponentMergeBoundary391$Write", "783c2937c1337f3f62f5fea3b954117add7a2202bc5005d5918e36924fac07b4"),
        Map.entry("NativeExpectedFieldObservation$ReadOps", "867b505c7b8c5d686f93f0143c65ca50ce48846f8ac803d7d6aa375bd469cf71"),
        Map.entry("NativeExpectedFieldObservation$ReadBuilder", "441df85107006d2a41f0848707841944067f5d39de1bd6302a6de1fe2888ee9b"),
        Map.entry("NativeExpectedFieldObservation$ReadRegistries", "212e53ce33c22911615830f91e8043c24f5167a221d878768691cadd764b0cc4"),
        Map.entry("NativeFoundationItemData", "0a1faf4ca43396366964cb9ed5b572f124f004d91e7140e6860d9e3b94480511"),
        Map.entry("NativeItemComponentObservation", "7994a2a96a836e91447c89c75cd905edd942792edad320f3e7256537f31b5c38"),
        Map.entry("mixin/NativeItemStackHandlerLifetimeMixin232", "99c1e9ab66a6f724b209affb1c35fff4846eade8f17f618577b47a2a27105aa9"),
        Map.entry("NativeCuriosProviderFields260", "c9a6cf28d9a3a8c66261dad3b185b50083818c26536d1039e9d90ab22a44cbdf"),
        Map.entry("NativeCuriosProviderFields260$Frame", "22fae74f222c802f97a3cbf7343a3c480551bb792414bff1cf36120ba306788d"),
        Map.entry("NativeCuriosProviderFields260$Snapshot", "1514a7da19d201f5b2cedbb1de4a4ff7f4b1663df1d30461d31f667c0d178422"),
        Map.entry("NativeCuriosProviderFields260$State", "45bef35590b0664d976594508063248f3e04f9fcb16b6d3d7d7eaf52927bca00"),
        Map.entry("mixin/NativeCuriosProviderStateMixin257", "fd7ffbbcafb17828c5196b47adcd48451409a94c85f89048066522adcd38b7ac"),
        Map.entry("FoodWeaponBossPassive", "9ecbd6ec8c329491dc9bc2477df318279b917fbce6622b97161af940eac9e9e1"),
        Map.entry("FoodSocketPowers", "39d0feb257146f44e4bfe67ce78d25a05dd8549b3939ae5e671a849c73ad539d"),
        Map.entry("WeaponMaterialBuild", "af034d9828eba0f9f977fc73ec98c96d85672dff94f80c95bc273580c316bbd8"),
        Map.entry("FoodWeaponFinalDamage", "9cc908a85310680e087bbf892a33feb07634be0228212167631691cd8906be79"),
        Map.entry("FoodWeaponEvolution", "4cd5bb2b19b3b2df0d7c915cffe2969fc2d98893a11b02d2bdfc6644c8e668eb"),
        Map.entry("FoodWeaponUseBridge", "a732225b134edaa61849e977df51cdb8a0236d22229e4c21f612dda9cb2f6685"),
        Map.entry("NativeCapabilityLifecycleRequirement202", "e9139dbd30f987c57ac4cbc921a9165faaf599d462cecc719f153abace927086"),
        Map.entry("OwnerFieldCoverageRegistry", "509c4b9b9c45408e3a9127ce8433f4005d9e801a9dedf1a29ad5b8d514baa573"),
        Map.entry("NativeOpaqueProviderReadFence201$Watch", "18cc9bb46ffe5162bb12538b25bb3ffc76e5ac2365852889de7cb0e581058408"),
        Map.entry("NativeOpaqueProviderReadFence201$Value", "0752113b7b2130419deba4d93c43421f4395b11f4bb8a637aecfc97cbf06fe23"),
        Map.entry("NativeOpaqueProviderReadFence201$Graph", "e525b05ec00f0cf0451c2acad4028a8e234423556f18f7a32580c98fb729c324"),
        Map.entry("NativeOpaqueProviderReadFence201", "5e20d244fcbdbb1ea7e85f83c44c074a2f4b2566df7248abedd4286a513a54c4"),
        Map.entry("NativePreQueryCapabilityLifetime199$Notification", "0b6ebfe82a8536c7228e7e2875526149d4a6c91e82d5eff687575001311a3352"),
        Map.entry("NativePreQueryCapabilityLifetime199$Watch", "b60de13697d1f2e83ce6c8c7869dfc5827180a917d74fcaf1adcf8699adb9a54"),
        Map.entry("NativePreQueryCapabilityLifetime199", "6d5baa83de72801da898de80b71c72df6fd9e0b8c1a56aad83f0b8a95239d0e7"),
        Map.entry("NativeCapabilityOwnerLinks194", "8d1165fa1b508850c626ecda18e5ef0eb22beadf6a80977677ab075b0ae4be4f"),
        Map.entry("OwnerFieldInstallationCatalogue", "8125461184d4b99498dd1aec615fdd4d6eb3e705a83853d8de5282e60f8dfe92"),
        Map.entry("OwnerFieldInstallationProof", "f7f334df9bed29af1e3d937d0b559a40b228f9a5e9728b9ba3cd1de3e2cdda8b"),
        Map.entry("FoodWeaponSourceLedger$AttemptBatchAdmission", "c544f9a835973a8db0f11b161576ab32c7172b73e6ee82d864f05ab6ae303901"),
        Map.entry("FoodWeaponSourceLedger$AttemptBatchAdmissionStatus", "834a8e13289ce2f39bf3d66dd4e5f8af0ad5c17eac190ceca7a514be73d6e5c5"),
        Map.entry("FoodWeaponSourceLedger$AttemptBatchCommit", "6114a97932d5f7f8aff6a5988f66769befad33810b20b46483f7f9baac74e295"),
        Map.entry("FoodWeaponSourceLedger$AttemptBatchCommitStatus", "614bfdc137550f392ab0bab0a5aa1e670c53dd2da36f15e845c82473777a435c"),
        Map.entry("FoodWeaponSourceLedger$AttemptBatchQuote", "c572b62febd57cc1735c47adede464cafc18478aba2ec31c088eb996bf80ede8"),
        Map.entry("FoodWeaponSourceLedger$AttemptBatchStatus", "f2b06d681bb16ffaa6f62234cfc97dd8ca6127cd6581ec9f21156aa5da3a0889"),
        Map.entry("FoodWeaponSourceLedger$AttemptCommit", "1c8390b1fd8867a078e682e11603a159e7eaa850a735f36f496e822a661ef028"),
        Map.entry("FoodWeaponSourceLedger$AttemptDomain", "9cc722a32a243d4dd328a4e32d2a0ecca1b1e61bc71ceede57172d044dbe17b2"),
        Map.entry("FoodWeaponSourceLedger$AttemptQuote", "47411149a2346ea43a7c91b8f0ea84739bd78935ac1c53fbc8356410ea85ee67"),
        Map.entry("FoodWeaponSourceLedger$AttemptRequest", "ed6a5533d4dae8b61fd3748e24f0904c1642bb2dc00332a94f805a0d5994ba6c"),
        Map.entry("FoodWeaponSourceLedger$AttemptResult", "be871272c2ae9e25189fae749db0bedc46948f1df6cde8f7225670ae5fcc01d6"),
        Map.entry("FoodWeaponSourceLedger$AttemptStatus", "f8bd7ecdae292feb389a706f25bbe2f34e8a379abb769555973b9b412c46e76c"),
        Map.entry("FoodWeaponSourceLedger$BatchPreparation", "89d136339c2ffef4bb830ac44e4612538469868e511b02aa4b6e1456129cbb81"),
        Map.entry("FoodWeaponSourceLedger$Claim", "6fd20ee9b56a0f3d7c02cacca76915f4d07465f3e6cf98b9bf09edf61650e274"),
        Map.entry("FoodWeaponSourceLedger$Commit", "16f3c8ce1fb49033ac7746a9be5686dc411fffbd28a5e0b44c415d7a114b483b"),
        Map.entry("FoodWeaponSourceLedger$DomainKind", "42d37f2bc629dfac37b7f80aa10657682fe968b94d870450682d74c35e979152"),
        Map.entry("FoodWeaponSourceLedger$EffectDomain", "f3ff3ef1432b1ded03e0e635c0bcd24c392075d89ac32114fece1ec89a53bd28"),
        Map.entry("FoodWeaponSourceLedger$FinalListedChildScope", "2b2e1c0df72285001e933923fd38f44794b3e7b2b3d90e249e095448086327e9"),
        Map.entry("FoodWeaponSourceLedger$ImageCursor", "fe997bb53365874a9dedf9baaaf4b586aa086a0ebbc3b956df47c4c9ddd07029"),
        Map.entry("FoodWeaponSourceLedger$InteractionRoot", "7a72794b5ff0fd960f7c8d5b3614fde93a5913bcbfe894c7a81f0a3935895e46"),
        Map.entry("FoodWeaponSourceLedger$IssuerCut", "ce7cdb8b7843d5844edd414f46391d5031ba113939de8d41bcadfe9bf66c2703"),
        Map.entry("FoodWeaponSourceLedger$NativeCapabilityToken", "e800e6d0c90cacbc52ec69992720b82e1f5f1a89b8ebdbd8db7814e930658027"),
        Map.entry("FoodWeaponSourceLedger$NativeCarrierToken", "924e9da4cae111fd993408903e718d1bae971307545ff65376434da9747c61d2"),
        Map.entry("FoodWeaponSourceLedger$NativeEvergaleAction", "048cc6440256bc5b9c715cfc1942c6a7907774063f6e81c5cb33d962d1b818fc"),
        Map.entry("FoodWeaponSourceLedger$NativeEvergaleInteraction", "f3c15a11aaddfd197f48253373fab8e59f1b75c520b073688ac74c0411546f8b"),
        Map.entry("FoodWeaponSourceLedger$NativeEvergaleSpawnWitness", "5170df7af0a8762f6938f98d669366d84c5536974e115c441217eef8e9028515"),
        Map.entry("FoodWeaponSourceLedger$NativeFailureRecord", "a414e052bf0faf849093e5cd71f48ab12b8473809ef490fca1ccd231baef7927"),
        Map.entry("FoodWeaponSourceLedger$NativeHurtScope", "29e3a213c01f8fe7acffea272158422948ecdfb8d39ec90354ffa19a573ef7a2"),
        Map.entry("FoodWeaponSourceLedger$NativeIngressKind", "aa350241bd0d04e0d01cf0730701a4e184424aca1acce56cbfab60c0a93e65db"),
        Map.entry("FoodWeaponSourceLedger$NativeIngressReceipt", "35e1309d2ccfbf0e07117610d7117b7b1289f67314c9ffe5ca07f4604b54691c"),
        Map.entry("FoodWeaponSourceLedger$NativeIngressToken", "9d28367f6aebca35f71c97c41687058c4ada1a584c38beb98a772c1f9b552507"),
        Map.entry("FoodWeaponSourceLedger$NativeIronCallbackToken", "ca6ebdeb8e05b190f9ceff126abc8496cce06fbaceda9f3f66576952aa7648d6"),
        Map.entry("FoodWeaponSourceLedger$NativeIronCastToken", "fc2982f4bfeb4b6fb92f55c24de50cd5bbb346ce7985e9d19752546abe5a15bd"),
        Map.entry("FoodWeaponSourceLedger$NativeIronPaymentToken", "883a25f688927b82f6f98bd4cf365278c6884e8f5505ea7daf072060f07ebdee"),
        Map.entry("FoodWeaponSourceLedger$NativeIronReceipt", "a4cb4d150e4e59d51654676b5625dbb34e801f92b900ac58d6294c65c69f2f2a"),
        Map.entry("FoodWeaponSourceLedger$NativeIronUseToken", "c43de617dea787183f5178835ae7d7dca7425576666cdbd755682feacce82451"),
        Map.entry("FoodWeaponSourceLedger$NativeIssuerCustody", "b3e46e1fc9ccc6bf87e0e5db9bb8ca0d7b643c95ffd6245ffac7adf991180230"),
        Map.entry("FoodWeaponSourceLedger$NativeIssueWindow", "0b45579db25c9cf3023837624a033b0276261fc2a03b2c1d5ad444f3f3afdbb9"),
        Map.entry("FoodWeaponSourceLedger$NativeKind", "78510c6b265b57e95276e13afc0ff3ade8d0544f19f9c3100e62480230092cbc"),
        Map.entry("FoodWeaponSourceLedger$NativeMaterialCapture", "eda1badebb176ab224c33f12ad1dbeb257737a4aee557f23308a5c18ed3bdd26"),
        Map.entry("FoodWeaponSourceLedger$NativeMaterialPhase", "c73441eaadcc52c51161b9a29e5191a50cbdc8b58d505d96ab6e35ddfe7e1e03"),
        Map.entry("FoodWeaponSourceLedger$NativeMaterialPublication", "49783494a07c849b4851c1ff14a635181031a2c3c1061eda1dc2991e5f2763d0"),
        Map.entry("FoodWeaponSourceLedger$NativeMaterialTransfer", "7d7cb4e26dd6740988abe59c758d2b06e2eccfc9bab852301ecfa0e37109d6f2"),
        Map.entry("FoodWeaponSourceLedger$NativeObservationCut", "2cb322d0ebfce18d650f0e51614b0047e9f73568a57df6f36674f0e83bd89707"),
        Map.entry("FoodWeaponSourceLedger$NativeOperation", "01fb7e56204a662e6cd4b990b8ad9332c0138e631f30b23ce007c91718dfa031"),
        Map.entry("FoodWeaponSourceLedger$NativePaidWeapon", "f7d823fe82cfa1309731ab840765109ddcd5d1d423ba1bdfa5de0398c25360c0"),
        Map.entry("FoodWeaponSourceLedger$NativeProjectileInteraction", "dc0ef8493b67f4116aec28d7cb1ec95526cecf4eacdf20bb476a2f7fd192e4d5"),
        Map.entry("FoodWeaponSourceLedger$NativeProjectileObservation", "bd34225ce538725927a4c603262b7218b4d1b768728df2ec08617d639f1d09e6"),
        Map.entry("FoodWeaponSourceLedger$NativeReceipt", "98700b51553d6bc109da7711939632e2b579c0e71000902bf7d81aade2a74c9f"),
        Map.entry("FoodWeaponSourceLedger$NativeRootObservation", "4c19a745fb948ecf82e016a1c378f05d1ebe2d62595a77116f36b903f7014774"),
        Map.entry("FoodWeaponSourceLedger$NativeWearToken", "360fb9e7389623072af40295b22a39a818128c3f4b835f0623e12e38d82be683"),
        Map.entry("FoodWeaponSourceLedger$ObserverFailures", "9b81c8d128c2ee71d121ec5fee265c05a8b491c12265d42a55af783282288ede"),
        Map.entry("FoodWeaponSourceLedger$OriginalInteractionBinding", "e58e8ce6371dce3c4ea728cae2c674d7b967f20535ca6f8728e11901962468af"),
        Map.entry("FoodWeaponSourceLedger$OriginalRepairCall", "89d71a117e777b36e9253ed3232d616811de4a462349aba4dbd86ab89302f64c"),
        Map.entry("FoodWeaponSourceLedger$OwnerFieldSequenceBinding", "bab27406f942f3a1ef47c09e35e887cf9293794ae0e19f63fe6fefcf4d8a9d39"),
        Map.entry("FoodWeaponSourceLedger$OwnerPlayerStep", "5fc75923395336f29d513e377e40e7c9250d51f4983cab22320fbd8cb9750bc7"),
        Map.entry("FoodWeaponSourceLedger$OwnerPlayerWriter", "a8140a8b8a4c5211f5ed718213be7571f736b0e60630a8b50007fe769077679c"),
        Map.entry("FoodWeaponSourceLedger$OwnerRepairObservation", "a11907a135dc0fcace5a397fb679057061a3730d62217ecba0a6c700ce931acd"),
        Map.entry("FoodWeaponSourceLedger$OwnerState", "115c67659035967a2c1d108e6dd9f0170b9fd42abac6f7184054828ce81d909a"),
        Map.entry("FoodWeaponSourceLedger$OwnerWriterKind", "cea0bd8ced915210edba14a1288a1e67b0543e341ba73500ffdb1c98647adb5a"),
        Map.entry("FoodWeaponSourceLedger$OwnerWriterPublication", "fc10fc68c6b70ab825c4caa07b0df0c3fb6b5b936d41e0f4cc2ad6825c960195"),
        Map.entry("FoodWeaponSourceLedger$OwnerWriterStep", "4431f747ba6950dde06407cc6f968cc5754da004945c4d68d05c74c2e35590d9"),
        Map.entry("FoodWeaponSourceLedger$PendingEchoInteraction", "5dd892eed416a0dffe14d575fb80e504e54c4d71a167b6e69abe94f921cbb49f"),
        Map.entry("FoodWeaponSourceLedger$PendingEchoLineage", "c3ddea3a8f47182ec9d67a625307f2c8d43f47b483c0038e2f706014023a8849"),
        Map.entry("FoodWeaponSourceLedger$PlayerImageDelta", "4b6647e42c2c40481162a7d3ee52361cc37cfbddbd439fd3af96adbecf798bfe"),
        Map.entry("FoodWeaponSourceLedger$PlayerImagePlan", "eb36146705e115f362eb221493e1c750951848bac564f80ab13a89cae6b59001"),
        Map.entry("FoodWeaponSourceLedger$PlayerWriterArgument", "06defdb68430157db570ae52e73b33d153d18ebdfecf1e00cf64ff3ffb472458"),
        Map.entry("FoodWeaponSourceLedger$PlayerWriterArgumentKind", "25a8bff01ddf77004161995994e03bdb9214daf503ef1fd7a3b937bcd97db28c"),
        Map.entry("FoodWeaponSourceLedger$PrimaryReadWindow", "ce1cc1e4b8b304fe333aec5b28fdb3e1abccdd1ac0f066c1dab6625e4424a69c"),
        Map.entry("FoodWeaponSourceLedger$ReadAliases", "48d8ff696a0d82957c77a10545535f9828b275740756750597ba628fb9d12e5b"),
        Map.entry("FoodWeaponSourceLedger$ReadCut", "1c246300bacdfc1b75e5abc9a00fef2825a4ede95d89a839c536c5c84f822455"),
        Map.entry("FoodWeaponSourceLedger$ReadGeneration", "630cac65793dbb9e3098f8df32b582ab0ffd06219ac360dea6ad29209ab21a3e"),
        Map.entry("FoodWeaponSourceLedger$RepairOriginalTrace", "869fe4fff76959b6388176bd3e1f326ecc95a41e8167f14e08ca9dfaf530d47f"),
        Map.entry("FoodWeaponSourceLedger$RootSite", "e449bfe7db56c0457a1e6ddb68350024404e2ae897acb63a4ba187da5cf9340b"),
        Map.entry("FoodWeaponSourceLedger$ServerState", "3e3f0c5970ac737fff2d21f3b9822087af48bc738333e50d6f4c0993acf52f7d"),
        Map.entry("FoodWeaponSourceLedger$SourceTicket", "bb1f67330b9b99f0b3bda26b61fa584012d9e66e0c741aab91b8fdc4c391b53e"),
        Map.entry("FoodWeaponSourceLedger$TargetCommit", "b7c290567d96bbbdc6a6753afb6fc91e4879bad10233231afdfeb79aa9509bdc"),
        Map.entry("FoodWeaponSourceLedger$TargetEffectDomain", "390e9a2d24bcb62c3d6b79caaf00f1e213d0c3655645016a9c9ab5b2eac741e2"),
        Map.entry("FoodWeaponSourceLedger$TargetQuote", "d341975e392cc5638d905e1a8cb0d8f97f1b7283fa0a72d3a9b8a07ff925d377"),
        Map.entry("FoodWeaponSourceLedger$TargetRequest", "4b588e29bdf521b2601d28bf1f1030b86295dc2050bd46cd90e3d4fe213ae277"),
        Map.entry("FoodWeaponSourceLedger$TargetResult", "aef58babcaaff31b1c4fbfe6f7180ee3ca100a6c84f07b691dfabef3b8680097"),
        Map.entry("FoodWeaponSourceLedger$TargetStatus", "138ba1f86632372345b906a991ff08cf79e312dd8f819371dbddeeaa9e5700e1"),
        Map.entry("FoodWeaponSourceLedger$TicketState", "cef037c7b65e447559859905ed6c07de4c90bf8e0463d777700f9ce41a9cda2a"),
        Map.entry("FoodWeaponSourceLedger$TridentCarrierLineage", "4a5251f0ee9368065aaadda4e76063cca8c8a32199c3cce82c71ac42b7a117b8"),
        Map.entry("FoodWeaponSourceLedger$TridentTicketBefore", "585c60ad0544c73cba910822375073af4bb272a7c567435bfeb789c180538936"),
        Map.entry("FoodWeaponSourceLedger$WeaponExpectation", "2e3e731b766cea1669e2546ccdfe8031a2d85c0b775237b3a09cacb569161903"),
        Map.entry("FoodWeaponSourceLedger$WriterPlanningCut", "bbfe11962f2a43f024e8a88b10d14f3d75c1da0939b05a5a13086c2ba062723b"),
        Map.entry("FoodWeaponSourceLedger$WriterSelection", "39196cd6872b3353fc3532126766ccf7af76c35b6250127c14a547b31b6f3cc3"),
        Map.entry("FoodWeaponSourceLedger", "038e3d6baedc7d71515463e25db675a7fada529b22c2fc7ce0eae761ed069945"),
        Map.entry("NativeObservationCarrier", "846e3294b6b46406a7a95dac985b31d0e416a445f83d289f87c8ff3614b5f392"),
        Map.entry("NativeObservationFrame", "4bc18739e8662b415bc18e6454b0b64bebeddf18ddc85ead3d7e58ac645b2b98"),
        Map.entry("NativeObservationState", "ab7fd903b050c1e150f25de9ffb689b9a5d444acd6085b717d39e6fb349288e8"),
        Map.entry("NativeReadCutObserver", "ef9c157f4ad541d3eb4e26a69d1d9ba2b4661f039de3653c32a20c80287643e9"),
        Map.entry("OwnerFieldExpectedAuthority$DomainBefore", "fb534e2670f9f4a7580c59be726c5cc6e2a03367c0772c4112153b52901f6099"),
        Map.entry("OwnerFieldExpectedAuthority$Prepared", "9b0a25e49d35a132e82875071a48c80ce72bd9cddaeece396706369208ffc349"),
        Map.entry("OwnerFieldExpectedAuthority$TimerQuote", "cdaa78c7a688c00ec1e003ad4628a5cf955f4ef327f31fb7dbe7cf6ced90287d"),
        Map.entry("OwnerFieldExpectedAuthority", "eeba4176d3cfc51ffd19009ff3634f742b0b59325f3d35475f8df816a882901d"),
        Map.entry("OwnerFullRepairGuard$AliasHandoff", "d097524139c34f8985324a42d597abb5c7fa0e7b88cc40ce0e2b6a9e41d2fa13"),
        Map.entry("OwnerFullRepairGuard$Cut", "23f5b4d9b39e7c164e95843cf3932e98e88d5ba57b3120982d604f909d3422ba"),
        Map.entry("OwnerFullRepairGuard$Mutation", "4c80ae236659b2303f9372420427c8c1cee09ba553ceeb637fcad9048bd787b6"),
        Map.entry("OwnerFullRepairGuard$Node", "6d2a1105eb7591526909eb0a4327acdd5ed1ae7de35340c3ffea3f2ff0cb69c5"),
        Map.entry("OwnerFullRepairGuard$Record", "6fb1b7df429ac341abde928304b22b920419272a08daaae7eeb762644f19db63"),
        Map.entry("OwnerFullRepairGuard$StorageView", "994441223f07b4be6fefa2b7fcbed4376599bb885c4a92cbaff3f8e72ddd9577"),
        Map.entry("OwnerFullRepairGuard$TickCut", "82308c086d20c08fb67af52ded64c937c9f774c83e856221293137843265a2ac"),
        Map.entry("OwnerFullRepairGuard", "7a89c0e027336a13f8aaa3017ad35d9e29ad914023a0d4136449949464c7030c"),
        Map.entry("OwnerLineageRevocationStore$Advance", "de037f808c91d6f2c184ca4caf8277a4c4249c754930764d221292cac052dc83"),
        Map.entry("OwnerLineageRevocationStore$Lease", "a4b383af57e946472531c141dd783789211740b42b42c3ce47fd7ff678b94620"),
        Map.entry("OwnerLineageRevocationStore$Record", "adb379037bfca0cb372bfb5165600b0c7b7c3a1dc51a089856e73a602b33a4d4"),
        Map.entry("OwnerLineageRevocationStore$TaskReservation", "34ff0f73e7414872da78c6cd5a3e1b174fd31fd9d90d4c88545a2c906e33af14"),
        Map.entry("OwnerLineageRevocationStore", "fb234a768f83ab7ca2f34d1073df88943614897d82e434a7180e458dc7c6b4f1"),
        Map.entry("OwnerRepairContinuation$Binding", "a5ae02b35dac173c16ef843258230d8c21d0d4b7df469aa45a81874e80d761ba"),
        Map.entry("OwnerRepairContinuation$CarryWrite", "a592d9ea05d20352df6967d713ad97aec48bec6fc57743d44f8c294775ed92dd"),
        Map.entry("OwnerRepairContinuation$Entry", "31c1cb6bfa2b7b7d8bc3df8c32774928a18fe211f40ae47451d57a98be90e7fb"),
        Map.entry("OwnerRepairContinuation$IdentityRead", "ddd76a83cfdbea4ad71010c0b3e96b656959769fe875cfd2f50cd557ba207edd"),
        Map.entry("OwnerRepairContinuation$Track", "31c63654567f984df2db006c7270c8c055eecfaea7a463e73c218bd45a950a9e"),
        Map.entry("OwnerRepairContinuation$TrackPhase", "4de81b0faa37aa7b4fabd7f090c00df086f7381d30f2a9fe99e2b6af8d334ebf"),
        Map.entry("OwnerRepairContinuation", "5974c3d35e3db47b49da06bf573af8d88b6ed3c75dab8e587752c198d7419ffe"),
        Map.entry("OwnerRepairGenerationAuthority$Generation", "094134e8a2b8dc92022be51313c4158ddcc797c88d18a626f35675938b20d8ea"),
        Map.entry("OwnerRepairGenerationAuthority$Intent", "b76ad292a38cae29c1ec2e26da469f32cb31087ad53c60b8333df461243641fa"),
        Map.entry("OwnerRepairGenerationAuthority", "606e369442245bfa643ba32c88d59f0bbd72a48fbb6f432d9f711ca02c246de2"),
        Map.entry("OwnerRevocationNodeAccess", "f189570d2d830553b612b46e11b8b8ca257c8a63dae05ab8ca9f86a305b14621"),
        Map.entry("OwnerStatueTaskOrigin$Call", "5eca5cb40ce55d3fe03b6fa91eaf8aee1f0f365066de97435fdd4690e287948e"),
        Map.entry("OwnerStatueTaskOrigin$Phase", "324631c513a816016fad8ecacadf8bdc2c0620e256559107eb5278811bc65d52"),
        Map.entry("OwnerStatueTaskOrigin$ProgressRead", "f3cef116bea2b3b22ca20dd9e1c4b96867e29f75bd55a900c02b346bf881e43f"),
        Map.entry("OwnerStatueTaskOrigin$Publication", "f3769b8e5b818564b37b96f0bc09e1c3c075775f6d3281745422c242e91820cc"),
        Map.entry("OwnerStatueTaskOrigin$PublishedPredecessor", "1cc593bfd24bc14001c9d7c852221b2e83e3f28acfd686deb746fd6698cbdd5b"),
        Map.entry("OwnerStatueTaskOrigin$Session", "a46f6c54e383ce631e613a6c26440c6ff7ad4ad9367e89da1ffe7be279797f1c"),
        Map.entry("OwnerStatueTaskOrigin$Write", "b116c14470bbca83c44ecaa7e2299072c4728dde81d46e9fa7ab37a5b4fac104"),
        Map.entry("OwnerStatueTaskOrigin", "e9048d0c4fae793f8508ccf09ef74b31cb8f0b35eaa1768c518b27b2c6fdf130"),
        Map.entry("OwnerStatueWearAdmission$Accepted", "06127b7f49a37a5ad1df8f3804c753c8964eea16aace6efcb42153e506a62a71"),
        Map.entry("OwnerStatueWearAdmission$Frame", "72da480251988d15f12877e0fe08a25fc64288f04f06f116819d22a5c07ee7e7"),
        Map.entry("OwnerStatueWearAdmission$Load", "aecefbd323c70b96459ccf5e3c7a1130c8ec8b1d2b272f66bb2b2c8b030d7d1c"),
        Map.entry("OwnerStatueWearAdmission$Origin", "5ba22ce7b0a1a75f4ffb1a72e22e0287f2fff80377abffd5528da8ca9883b1ba"),
        Map.entry("OwnerStatueWearAdmission$Phase", "3526fe3626a15a42b895ee04d5434ea7de1390d3404794e6791bd8fd97972433"),
        Map.entry("OwnerStatueWearAdmission$Reading", "25bb5fee75d8a5a87d1bfdc2165282503e5b39e64f8551d44a998844bfc3b305"),
        Map.entry("OwnerStatueWearAdmission$Root", "4fce5135641100eb058cbdf2cb2045d8d3f70c5efa63eafd8db822ace70ed9cc"),
        Map.entry("OwnerStatueWearAdmission$Terminal", "b17f76e2a62952adb08991b8ed5504aa6a046d9e2964798360f77c1ba99a819b"),
        Map.entry("OwnerStatueWearAdmission$Write", "6df13adf9575fc4661c84ca17c9a888c0f0c6ab639eb6dedc79a9d2eb27944e4"),
        Map.entry("OwnerStatueWearAdmission", "753d6262bdb521cc235943a9a4966afb6b20d65b02abb69f0ec9d039aaf131fb"),
        Map.entry("NativeExpectedFieldObservation", "adbf5bf4716d8377c3f66a3c47f9a96e4672616428af92c120e7059523884a3b"),
        Map.entry("NativeExpectedFieldObservation$Fact", "a59fadbba32dc20664b240aff784e07ba746f526fbeeeeaa6ee8bb956addfadf"),
        Map.entry("NativeCapabilityRegistrationFields", "46193f006668fa91268ad37c3c03b44ee4b455a6fd534bed48d8655c04bec318"),
        Map.entry("NativeItemCapabilityFields", "b989ab06efc0638b83f497a63c4380500c0f3b44f735b2f655ebcbe7a8cfa6a0"),
        Map.entry("NativeItemCapabilityObservation$Capability", "194be3cf14cd262bfc2a62f366dd97de8d6e412451eb9583e4ca31320d49addc"),
        Map.entry("NativeItemCapabilityObservation$Providers", "17ecdee4a018fe9c9e0c81d90e064d3c71731eac02e99c26822473f1a0261030"),
        Map.entry("NativeItemCapabilityObservation", "c547fe0abc3c602313b3d7f9dbff2620cca0f20e73b9c7a9ab162f0d7a448186"),
        Map.entry("NativeItemCapabilityState", "6444ca24932551c151e3ee3c369202c1f1f0f55d67128af2c63eaed1debedea4"),
        Map.entry("NativeItemComponentFields$CustomData", "86ae7c0231b489d4b7c598cab0c485e4b4d7e2768ed49fffcb5944fa24c5bd9e"),
        Map.entry("NativeItemComponentFields$Stack", "b0707789de9fc1b5f8eb8a03352068d82376af37d06041ed5b81b20bf01ad441"),
        Map.entry("NativeItemComponentFields$Storage", "56aa616d7af73957788b778fff07149984b3931f86c3f2f129df3678db5e09e1"),
        Map.entry("NativeItemComponentFields", "34cfa577e0b88c596b216a4776ea0c95382e2fe21845094606d1c6535a2f626f"),
        Map.entry("NativeItemHandlerLifetime232", "c1fcafe9cd761ea8425b24396b1ab2c43a39817b354a5676c8cd1059f6741df2"),
        Map.entry("NativeItemHandlerLifetime232$Watch", "a2450c88b4b7ffee21031f5bb52ccce7b73e903e1ad693eda61e87b13e6cbcf6"),
        Map.entry("NativeItemHandlerLifetime232$ChildWatch", "1ee02f8f3dfbb69be37ffe27e44d52516dfe4daddf90de80aaf088891fd85039"),
        Map.entry("NativeItemStackInstallationProof165", "3ad59e8594251413017afcd85331b5f9d7a1714157e69a64586422776d7b9ee5"),
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
        for (var entry : GUARDS.entrySet()) try (var stream = NativeCapabilityInstallationProof170.class.getResourceAsStream("/net/farmingtales/ftboss/" + ((entry.getKey().startsWith("mixin/") || entry.getKey().equals("util/foundations/FoundationCanonicalDigest") || entry.getKey().equals("util/foundations/FoundationCanonicalDigest$PrivateCompound")) ? entry.getKey() : "item/" + entry.getKey()) + ".class")) {
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
