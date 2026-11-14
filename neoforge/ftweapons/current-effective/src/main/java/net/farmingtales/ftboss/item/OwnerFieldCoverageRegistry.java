package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace.Plan;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

public final class OwnerFieldCoverageRegistry {
    private static final String[][] REQUIRED = new String[][]{
        {
                "net.farmingtales.ftboss.mixin.NativeExplicitTimerPutMixin",
                "net.farmingtales.ftboss.item.FoodWeaponEvolution",
                "ftboss$r2$timerPut",
                "ftboss$r2$timerPutSrg"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerTimerLocalOriginMixin",
                "net.farmingtales.ftboss.item.FoodWeaponEvolution",
                "ftboss$ownerLocalDataCopy",
                "ftboss$ownerLocalBaselineCopy",
                "ftboss$ownerTargetReceiver",
                "ftboss$ownerTargetWrite"
        },
        {"net.farmingtales.ftboss.mixin.OwnerTimerSemanticConsumerMixin", "net.farmingtales.ftboss.item.FoodWeaponHitPlan", "ftboss$ownerSemanticLeaf"},
        {"net.farmingtales.ftboss.mixin.OwnerTimerRangedGuardMixin", "net.farmingtales.ftboss.item.FoodWeaponRanged", "ftboss$ownerMaterialGuard"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedActorFieldsMixin", "net.minecraft.world.entity.Entity", "ftboss$rawPersistentData"},
        {
                "net.farmingtales.ftboss.mixin.OwnerFieldExpectedEntityMixin",
                "net.minecraft.world.entity.Entity",
                "ftboss$ownerFieldConstructed",
                "ftboss$ownerFieldPersistentExposed",
                "ftboss$ownerFieldRawLevel",
                "ftboss$ownerFieldDomain",
                "ftboss$ownerFieldInstallDomain",
                "ftboss$ownerFieldLoadQuoted",
                "ftboss$ownerFieldLoadAssigned"
        },
        {"net.farmingtales.ftboss.mixin.OwnerFieldSourceAccessMixin", "net.minecraft.world.damagesource.DamageSource", "ftboss$ownerFieldDirectEntity"},
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairByteArrayTagMixin",
                "net.minecraft.nbt.ByteArrayTag",
                "ftboss$fullRepairVoidMutation",
                "ftboss$fullRepairReturnedMutation",
                "ftboss$fullRepairAlias"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairCapabilityDispatcherMixin",
                "net.minecraftforge.common.capabilities.CapabilityDispatcher",
                "ftboss$fullRepairVoidMutation",
                "ftboss$fullRepairAlias"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairCapabilityProviderMixin",
                "net.minecraftforge.common.capabilities.CapabilityProvider",
                "ftboss$fullRepairVoidMutation",
                "ftboss$fullRepairReturnedMutation",
                "ftboss$fullRepairAlias"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairCompoundTagMixin",
                "net.minecraft.nbt.CompoundTag",
                "ftboss$fullRepairVoidMutation",
                "ftboss$fullRepairReturnedMutation",
                "ftboss$fullRepairAlias"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairIntArrayTagMixin",
                "net.minecraft.nbt.IntArrayTag",
                "ftboss$fullRepairVoidMutation",
                "ftboss$fullRepairReturnedMutation",
                "ftboss$fullRepairAlias"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairItemStackMixin",
                "net.minecraft.world.item.ItemStack",
                "ftboss$fullRepairVoidMutation",
                "ftboss$fullRepairReturnedMutation",
                "ftboss$fullRepairAlias",
                "ftboss$fullRepairGetOrCreateTag",
                "ftboss$fullRepairGetOrCreateElement"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairListTagMixin",
                "net.minecraft.nbt.ListTag",
                "ftboss$fullRepairVoidMutation",
                "ftboss$fullRepairReturnedMutation",
                "ftboss$fullRepairAlias"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerFullRepairLongArrayTagMixin",
                "net.minecraft.nbt.LongArrayTag",
                "ftboss$fullRepairVoidMutation",
                "ftboss$fullRepairReturnedMutation",
                "ftboss$fullRepairAlias"
        },
        {"net.farmingtales.ftboss.mixin.OwnerNativeMaterialEntityIdentityMixin", "net.minecraft.world.entity.Entity", "ftboss$ownerMaterialEntityIdentity"},
        {
                "net.farmingtales.ftboss.mixin.OwnerRepairCapabilityFieldsMixin",
                "net.minecraftforge.common.capabilities.CapabilityProvider",
                "ftboss$ownerRepairInitializedNoProviders"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerRepairStackFieldsMixin",
                "net.minecraft.world.item.ItemStack",
                "ftboss$ownerRepairRawItem",
                "ftboss$ownerRepairRawCapNbtAbsent",
                "ftboss$ownerRepairWasRetired",
                "ftboss$ownerRepairRetire"
        },
        {"net.farmingtales.ftboss.mixin.NativeExpectedByteArrayTagFieldsMixin", "net.minecraft.nbt.ByteArrayTag", "ftboss$rawBytes"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedByteTagFieldsMixin", "net.minecraft.nbt.ByteTag", "ftboss$rawByte"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedCompoundTagFieldsMixin", "net.minecraft.nbt.CompoundTag", "ftboss$rawCompoundFields"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedDoubleTagFieldsMixin", "net.minecraft.nbt.DoubleTag", "ftboss$rawDouble"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedFloatTagFieldsMixin", "net.minecraft.nbt.FloatTag", "ftboss$rawFloat"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedIntArrayTagFieldsMixin", "net.minecraft.nbt.IntArrayTag", "ftboss$rawInts"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedIntTagFieldsMixin", "net.minecraft.nbt.IntTag", "ftboss$rawInt"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedListTagFieldsMixin", "net.minecraft.nbt.ListTag", "ftboss$rawListFields", "ftboss$rawListKind"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedLongArrayTagFieldsMixin", "net.minecraft.nbt.LongArrayTag", "ftboss$rawLongs"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedLongTagFieldsMixin", "net.minecraft.nbt.LongTag", "ftboss$rawLong"},
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedProviderFieldsMixin",
                "net.minecraftforge.common.capabilities.CapabilityProvider",
                "ftboss$rawDispatcher",
                "ftboss$rawCapsValid",
                "ftboss$rawCapsLazy",
                "ftboss$rawCapsInitialized",
                "ftboss$rawCapsParentSupplier",
                "ftboss$rawCapsLazyData"
        },
        {"net.farmingtales.ftboss.mixin.NativeExpectedShortTagFieldsMixin", "net.minecraft.nbt.ShortTag", "ftboss$rawShort"},
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedStackFieldsMixin",
                "net.minecraft.world.item.ItemStack",
                "ftboss$rawStackTag",
                "ftboss$rawStackCaps",
                "ftboss$rawStackCount",
                "ftboss$rawStackItem"
        },
        {"net.farmingtales.ftboss.mixin.NativeExpectedStringTagFieldsMixin", "net.minecraft.nbt.StringTag", "ftboss$rawText"},
        {
                "net.farmingtales.ftboss.mixin.OwnerOriginalPublicationFieldsMixin",
                "net.farmingtales.ftboss.item.FoodWeaponFinalDamage$Publication",
                "ftboss$ownerOriginalProducers"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerEvolutionPlanFieldsMixin",
                "net.farmingtales.ftboss.item.FoodWeaponFinalDamage$EvolutionProducer",
                "ftboss$ownerOriginalPlan"
        },
        {"net.farmingtales.ftboss.mixin.OwnerOriginalPlanFieldsMixin", "net.farmingtales.ftboss.item.FoodWeaponHitPlan", "ftboss$ownerOriginalLeaves"},
        {
                "net.farmingtales.ftboss.mixin.OwnerTimerQuoteMixin",
                "net.farmingtales.ftboss.item.FoodWeaponHitPlan$TimerSaveLeaf",
                "ftboss$ownerTimerQuoted",
                "ftboss$ownerTimerQuote"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerOriginalPublicationAdmissionMixin",
                "net.farmingtales.ftboss.item.FoodWeaponFinalDamage",
                "ftboss$ownerOriginalPublicationAssigned"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerOriginalRepairContinuationMixin",
                "net.farmingtales.ftboss.item.FoodWeaponHitPlan$RepairLeaf",
                "ftboss$ownerRepairGetter",
                "ftboss$ownerRepairSetter"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerTridentOriginalCopyMixin",
                "net.minecraft.world.entity.projectile.ThrownTrident",
                "ftboss$ownerTridentOriginalCopy",
                "ftboss$ownerTridentLineage",
                "ftboss$ownerInstallTridentLineage"
        },
        {"net.farmingtales.ftboss.mixin.NativeTridentFieldCarrierMixin", "net.minecraft.world.entity.projectile.ThrownTrident", "ftboss$originalTridentStack"},
        {"net.farmingtales.ftboss.mixin.OwnerSnapshotLeaseCopyMixin", "net.minecraft.nbt.CompoundTag", "ftboss$ownerSnapshotCopy"},
        {
                "net.farmingtales.ftboss.mixin.OwnerRevocationNodeMixin",
                "net.minecraft.nbt.CompoundTag",
                "ftboss$ownerRevocationNode",
                "ftboss$ownerInstallRevocationNode"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerRevocationNodeMixin",
                "net.minecraft.nbt.ListTag",
                "ftboss$ownerRevocationNode",
                "ftboss$ownerInstallRevocationNode"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerRevocationNodeMixin",
                "net.minecraft.nbt.ByteArrayTag",
                "ftboss$ownerRevocationNode",
                "ftboss$ownerInstallRevocationNode"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerRevocationNodeMixin",
                "net.minecraft.nbt.IntArrayTag",
                "ftboss$ownerRevocationNode",
                "ftboss$ownerInstallRevocationNode"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerRevocationNodeMixin",
                "net.minecraft.nbt.LongArrayTag",
                "ftboss$ownerRevocationNode",
                "ftboss$ownerInstallRevocationNode"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerRevocationNodeMixin",
                "net.minecraftforge.common.capabilities.CapabilityProvider",
                "ftboss$ownerRevocationNode",
                "ftboss$ownerInstallRevocationNode"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerRevocationNodeMixin",
                "net.minecraftforge.common.capabilities.CapabilityDispatcher",
                "ftboss$ownerRevocationNode",
                "ftboss$ownerInstallRevocationNode"
        },
        {"net.farmingtales.ftboss.mixin.OwnerRawFieldExposureMixin", "net.minecraft.nbt.CompoundTag", "ftboss$ownerRawExposed"},
        {"net.farmingtales.ftboss.mixin.OwnerRawFieldExposureMixin", "net.minecraft.nbt.ListTag", "ftboss$ownerRawExposed"},
        {"net.farmingtales.ftboss.mixin.OwnerRawFieldExposureMixin", "net.minecraft.nbt.ByteArrayTag", "ftboss$ownerRawExposed"},
        {"net.farmingtales.ftboss.mixin.OwnerRawFieldExposureMixin", "net.minecraft.nbt.IntArrayTag", "ftboss$ownerRawExposed"},
        {"net.farmingtales.ftboss.mixin.OwnerRawFieldExposureMixin", "net.minecraft.nbt.LongArrayTag", "ftboss$ownerRawExposed"},
        {"net.farmingtales.ftboss.mixin.OwnerRawFieldExposureMixin", "net.minecraft.world.item.ItemStack", "ftboss$ownerRawExposed"},
        {"net.farmingtales.ftboss.mixin.OwnerRawFieldExposureMixin", "net.minecraft.world.entity.Entity", "ftboss$ownerRawExposed"},
        {"net.farmingtales.ftboss.mixin.OwnerRawFieldExposureMixin", "net.minecraftforge.common.capabilities.CapabilityProvider", "ftboss$ownerRawExposed"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedActualStepMixin", "net.farmingtales.ftboss.item.FoodWeaponHitPlan", "ftboss$actualReturnedTimerStep"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedAliasReturnMixin", "net.minecraft.nbt.CompoundTag", "ftboss$originalAliasReturned"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedCaptureBoundaryMixin", "net.farmingtales.ftboss.item.FoodWeaponUseBridge", "ftboss$originalCapture"},
        {"net.farmingtales.ftboss.mixin.NativeExpectedMapWriteMixin", "net.minecraft.nbt.CompoundTag", "ftboss$originalFieldMapPut"},
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedProviderObservationMixin",
                "net.minecraftforge.common.capabilities.CapabilityProvider",
                "ftboss$originalProviderBefore",
                "ftboss$originalProviderAfter"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedRangedScopeMixin",
                "net.farmingtales.ftboss.item.FoodWeaponHitPlan$TimerSaveLeaf",
                "ftboss$originalRangedScope"
        },
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin",
                "net.farmingtales.ftboss.item.FoodWeaponRanged",
                "ftboss$originalRangedPersistentData",
                "ftboss$originalRangedCompound",
                "ftboss$originalRangedCompoundSrg",
                "ftboss$originalRangedLong",
                "ftboss$originalRangedLongSrg",
                "ftboss$originalRangedRoot",
                "ftboss$originalRangedRootSrg"
        },
        {"net.farmingtales.ftboss.mixin.NativeExpectedTagAssignmentMixin", "net.minecraft.world.item.ItemStack", "ftboss$originalTagAssigned"},
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedTimerCopyMixin",
                "net.farmingtales.ftboss.item.FoodWeaponEvolution",
                "ftboss$actualOriginalCopy",
                "ftboss$actualOriginalCopySrg"
        },
        {"net.farmingtales.ftboss.mixin.NativeExpectedTimerReceiverMixin", "net.farmingtales.ftboss.item.FoodWeaponEvolution", "ftboss$actualOriginalReceiver"},
        {
                "net.farmingtales.ftboss.mixin.NativeExpectedTimerScopeMixin",
                "net.farmingtales.ftboss.item.FoodWeaponHitPlan$TimerSaveLeaf",
                "ftboss$originalTimerScope"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerNativeWeaponInputMixin",
                "net.minecraft.world.item.ItemStack",
                "ftboss$ownerNativeWeaponInput",
                "ftboss$ownerInstallNativeWeaponInput",
                "ftboss$ownerNativeLoadedInput"
        },
        {"net.farmingtales.ftboss.mixin.OwnerNativeLeafFieldsMixin", "net.farmingtales.ftboss.item.FoodWeaponHitPlan$TypedLeaf", "ftboss$ownerNativeAttempt"},
        {
                "net.farmingtales.ftboss.mixin.OwnerNativePutQuoteMixin",
                "net.farmingtales.ftboss.item.FoodWeaponHitPlan$TagLeaf",
                "ftboss$ownerNativePutQuote",
                "ftboss$ownerNativeOriginalValue",
                "ftboss$ownerNativePutQuoted"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerNativePutConsumerMixin",
                "net.farmingtales.ftboss.item.FoodWeaponHitPlan$TagLeaf",
                "ftboss$ownerNativeCopy",
                "ftboss$ownerNativePut"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerNativePutQuoteMixin",
                "net.farmingtales.ftboss.item.FoodWeaponHitPlan$OutcomeTagLeaf",
                "ftboss$ownerNativePutQuote",
                "ftboss$ownerNativeOriginalValue",
                "ftboss$ownerNativePutQuoted"
        },
        {
                "net.farmingtales.ftboss.mixin.OwnerNativePutConsumerMixin",
                "net.farmingtales.ftboss.item.FoodWeaponHitPlan$OutcomeTagLeaf",
                "ftboss$ownerNativeCopy",
                "ftboss$ownerNativePut"
        },
        {"net.farmingtales.ftboss.mixin.OwnerInventoryTickContinuityMixin", "net.minecraft.world.item.ItemStack", "ftboss$ownerInventoryTickCallback"}
    };
    private static final boolean[] INSTALLED = new boolean[REQUIRED.length];
    private static final IMixinInfo[] OBSERVED = new IMixinInfo[REQUIRED.length];
    private static final ClassNode[] TARGETS = new ClassNode[REQUIRED.length];
    private static IMixinConfig installationConfig;
    private static IMixinConfigPlugin installationPlugin;
    private static final Object GENERATION = new Object();
    private static boolean invalid;
    private static final List<List<String>> nativeChainBaselines = new ArrayList<>(Arrays.asList(null, null, null));
    private static final ClassNode[] nativeChainTargets = new ClassNode[3];
    private static final IMixinInfo[] nativeChainSources = new IMixinInfo[3];
    private static final IMixinInfo[] nativeChainCompanions = new IMixinInfo[3];
    private static final ClassLoader[] nativeChainLoaders = new ClassLoader[3];
    private static final List<List<IExtension>> nativeChainOrders = new ArrayList<>(Arrays.asList(null, null, null));
    private static final List<List<String>> nativeLoadBaselines = new ArrayList<>(Arrays.asList(null, null, null, null));
    private static final ClassNode[] nativeLoadTargets = new ClassNode[4];
    private static final IMixinInfo[] nativeLoadSources = new IMixinInfo[4];
    private static final ClassLoader[] nativeLoadLoaders = new ClassLoader[4];
    private static final List<List<IExtension>> nativeLoadOrders = new ArrayList<>(Arrays.asList(null, null, null, null));
    private static IMixinInfo nativeRegistrationInfo;
    private static ClassNode nativeRegistrationTarget;
    private static final List<List<List<String>>> operationBaselines = new ArrayList<>(Arrays.asList(null, null));
    private static final ClassNode[] operationTargets = new ClassNode[2];
    private static final IMixinInfo[] operationSources = new IMixinInfo[2];
    private static final IMixinInfo[] operationCompanions = new IMixinInfo[2];
    private static final ClassLoader[] operationLoaders = new ClassLoader[2];
    private static final List<List<IExtension>> operationOrders = new ArrayList<>(Arrays.asList(null, null));
    private static List<String> entityParentBaseline;
    private static ClassNode entityParentTarget;
    private static IMixinInfo entityParentSource;
    private static ClassLoader entityParentLoader;
    private static List<IExtension> entityParentOrder;
    private static final List<IMixinInfo> fieldObserverSources = new ArrayList<>();
    private static final List<ClassNode> fieldObserverTargets = new ArrayList<>();
    private static final List<WeakReference<ITargetClassContext>> lateContexts = new ArrayList<>(Collections.nCopies(4, null));
    private static final Thread[] lateThreads = new Thread[4];
    private static final ClassLoader[] lateLoaders = new ClassLoader[4];
    private static final byte[] lateStages = new byte[4];
    private static final List<List<IExtension>> lateOrders = new ArrayList<>(Collections.nCopies(4, null));
    private static List<String> timerParentBaseline;
    private static ClassNode timerParentTarget;
    private static IMixinInfo timerParentSource;
    private static ClassLoader timerParentLoader;
    private static List<IExtension> timerParentOrder;
    private static List<List<String>> rangedParentBaseline;
    private static ClassNode rangedParentTarget;
    private static IMixinInfo rangedParentSource;
    private static ClassLoader rangedParentLoader;
    private static List<IExtension> rangedParentOrder;
    private static final List<List<List<String>>> ownerParentBaselines = new ArrayList<>(Collections.nCopies(2, null));
    private static final ClassNode[] ownerParentTargets = new ClassNode[2];
    private static final IMixinInfo[] ownerParentSources = new IMixinInfo[2];
    private static final ClassLoader[] ownerParentLoaders = new ClassLoader[2];
    private static final List<List<IExtension>> ownerParentOrders = new ArrayList<>(Collections.nCopies(2, null));

    private OwnerFieldCoverageRegistry() {
    }

    private static Class<?> installationCaller(IMixinInfo var0) {
        return var0 == null
            ? null
            : StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                .walk(
                    var1 -> {
                        List<StackFrame> var2 = var1.filter(var0xx -> var0xx.getDeclaringClass() != OwnerFieldCoverageRegistry.class).limit(3L).toList();
                        if (var2.size() == 3
                            && !var2.stream().anyMatch(var0xx -> !var0xx.getMethodName().equals("postApply"))
                            && ((StackFrame)var2.get(0))
                                .getDescriptor()
                                .equals(
                                    "(Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;Ljava/lang/String;Lorg/spongepowered/asm/mixin/extensibility/IMixinInfo;)V"
                                )
                            && ((StackFrame)var2.get(1))
                                .getDescriptor()
                                .equals(
                                    "(Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;Ljava/lang/String;Lorg/spongepowered/asm/mixin/transformer/MixinInfo;)V"
                                )
                            && ((StackFrame)var2.get(2)).getDescriptor().equals("(Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;)V")) {
                            try {
                                ClassLoader var3 = IMixinInfo.class.getClassLoader();
                                Class var4 = Class.forName("org.spongepowered.asm.mixin.transformer.MixinInfo", false, var3);
                                Class var5 = Class.forName("org.spongepowered.asm.mixin.transformer.PluginHandle", false, var3);
                                Class var6 = Class.forName("org.spongepowered.asm.mixin.transformer.MixinConfig", false, var3);
                                if (var0.getClass() == var4
                                    && ((StackFrame)var2.get(1)).getDeclaringClass() == var5
                                    && ((StackFrame)var2.get(2)).getDeclaringClass() == var4) {
                                    Class var7 = ((StackFrame)var2.get(0)).getDeclaringClass();
                                    IMixinConfig var8 = var0.getConfig();
                                    IMixinConfigPlugin var9 = var8 == null ? null : var8.getPlugin();
                                    return NativeMapPutLateProofExtension.registeredPlugin(var7)
                                            && var8 != null
                                            && var8.getClass() == var6
                                            && "ftboss.owner-wear.mixins.json".equals(var8.getName())
                                            && "net.farmingtales.ftboss.mixin.".equals(var8.getMixinPackage())
                                            && var8.isRequired()
                                            && var9 != null
                                            && var9.getClass() == var7
                                            && (installationConfig == null || registeredConfiguration(var0))
                                        ? var7
                                        : null;
                                } else {
                                    return null;
                                }
                            } catch (ClassNotFoundException var10) {
                                return null;
                            }
                        } else {
                            return null;
                        }
                    }
                );
    }

    private static boolean registeredConfiguration(IMixinInfo var0) {
        return var0 != null
            && installationConfig != null
            && installationPlugin != null
            && var0.getConfig() == installationConfig
            && installationConfig.getPlugin() == installationPlugin
            && NativeMapPutLateProofExtension.registeredPlugin(installationPlugin.getClass());
    }

    private static boolean fieldTarget(IMixinInfo info, String target) {
        boolean declared = false;
        boolean configured = false;
        for (String name : info.getTargetClasses()) if (target.equals(name.replace('/', '.'))) declared = true;
        for (String name : info.getConfig().getTargets()) if (target.equals(name.replace('/', '.'))) configured = true;
        return declared && configured;
    }

    private static void rememberFieldObserver(String target, ClassNode node, String name, IMixinInfo info) {
        if (!name.equals(OwnerFieldInstallationProof.nativeAliasMixin(target))) return;
        if (node == null || !node.name.replace('/', '.').equals(target) || info.getClass().getClassLoader() != IMixinInfo.class.getClassLoader() || !info.getClassName().equals(name) || !info.getConfig().isRequired() || info.getConfig().getPlugin() == null || !NativeMapPutLateProofExtension.registeredPlugin(info.getConfig().getPlugin().getClass()) || !fieldTarget(info, target)) { invalid = true; return; }
        for (int index = 0; index < fieldObserverTargets.size(); index++) if (fieldObserverTargets.get(index).name.equals(node.name)) {
            if (fieldObserverTargets.get(index) != node || fieldObserverSources.get(index) != info) invalid = true;
            return;
        }
        fieldObserverTargets.add(node); fieldObserverSources.add(info);
    }

    static synchronized List<ClassNode> fieldSources(String[] row, ClassNode node) {
        if (invalid || !OwnerFieldInstallationProof.observedFieldSite(row) || node == null) return null;
        IMixinInfo exposure = null;
        for (int index = 0; index < REQUIRED.length; index++) if (REQUIRED[index][0].equals("net.farmingtales.ftboss.mixin.OwnerRawFieldExposureMixin") && REQUIRED[index][1].equals(row[1]) && TARGETS[index] == node && registeredConfiguration(OBSERVED[index])) exposure = OBSERVED[index];
        if (exposure == null) return null;
        String nativeName = OwnerFieldInstallationProof.nativeAliasMixin(row[1]);
        if (nativeName == null) return List.of(exposure.getClassNode(0));
        for (int index = 0; index < fieldObserverTargets.size(); index++) if (fieldObserverTargets.get(index) == node) {
            IMixinInfo info = fieldObserverSources.get(index);
            if (!registeredConfiguration(info) || !nativeName.equals(info.getClassName()) || info.getClass().getClassLoader() != IMixinInfo.class.getClassLoader() || !fieldTarget(info, row[1])) return null;
            return List.of(info.getClassNode(0), exposure.getClassNode(0));
        }
        return null;
    }

    private static void refreshFieldViews(ClassNode node) {
        if (invalid || node == null) return;
        for (int index = 0; index < REQUIRED.length; index++) if (TARGETS[index] == node && OwnerFieldInstallationProof.observedFieldSite(REQUIRED[index])) {
            INSTALLED[index] = false;
            IMixinInfo info = OBSERVED[index];
            if (registeredConfiguration(info) && info.getClass().getClassLoader() == IMixinInfo.class.getClassLoader() && info.getClassName().equals(REQUIRED[index][0]) && fieldTarget(info, REQUIRED[index][1])) {
                try { INSTALLED[index] = OwnerFieldInstallationProof.complete(REQUIRED[index], node, info.getClassNode(0)); } catch (Throwable failure) { INSTALLED[index] = false; }
            }
        }
    }

    public static synchronized void applied(String var0, ClassNode var1, String var2, IMixinInfo var3) {
        Class var4 = installationCaller(var3);
        if (var4 != null && var0 != null && var2 != null && var2.equals(var3.getClassName())) {
            rememberNativeComponents(var0, var1, var2, var3);
            rememberNativeTrident(var0, var1, var2, var3);
            rememberNativeTickWear(var0, var1, var2, var3);
            rememberNativeRegistration(var0, var1, var2, var3);
            rememberFieldObserver(var0, var1, var2, var3);
            try {
            for (int var5 = 0; var5 < REQUIRED.length; var5++) {
                String[] var6 = REQUIRED[var5];
                if (var6[0].equals(var2) && var6[1].equals(var0)) {
                    if (var1 != null
                        && var0.replace('.', '/').equals(var1.name)
                        && !var3.getTargetClasses().stream().noneMatch(var1x -> var0.equals(var1x.replace('/', '.')))
                        && !var3.getConfig().getTargets().stream().noneMatch(var1x -> var0.equals(var1x.replace('/', '.')))
                        && OBSERVED[var5] == null) {
                        if (installationConfig == null) {
                            installationConfig = var3.getConfig();
                            installationPlugin = installationConfig.getPlugin();
                        }

                        if (!registeredConfiguration(var3)) {
                            invalid = true;
                            return;
                        }

                        OBSERVED[var5] = var3;
                        TARGETS[var5] = var1;
                        int var7 = OwnerFieldInstallationProof.lateSlot(var6);
                        if (var7 >= 0) {
                            ITargetClassContext var8 = NativeMapPutLateProofExtension.current(var1);
                            lateStages[var7] = (byte)(var8 == null ? 3 : 1);
                            lateContexts.set(var7, var8 == null ? null : new WeakReference<>(var8));
                            lateThreads[var7] = var8 == null ? null : Thread.currentThread();
                            lateLoaders[var7] = var8 == null ? null : var4.getClassLoader();
                            INSTALLED[var5] = false;
                            lateOrders.set(var7, null);
                            return;
                        }

                        if (OwnerFieldInstallationProof.canonicalAliasSite(var6)) {
                            INSTALLED[var5] = false;
                            return;
                        }

                        try {
                            INSTALLED[var5] = OwnerFieldInstallationProof.complete(var6, var1, var3.getClassNode(0));
                        } catch (Throwable var9) {
                            INSTALLED[var5] = false;
                        }

                        return;
                    }

                    invalid = true;
                    return;
                }
            }
            } finally {
                refreshFieldViews(var1);
            }
        }
    }

    private static final IMixinInfo[] nativeTridentSources = new IMixinInfo[2];
    private static ClassNode nativeTridentTarget;
    private static List<String> nativeTridentBaseline;
    private static ClassLoader nativeTridentLoader;
    private static List<IExtension> nativeTridentOrder;

    private static void rememberNativeTrident(String targetName, ClassNode target, String sourceName, IMixinInfo info) {
        int slot = sourceName.equals("net.farmingtales.ftboss.mixin.OwnerTridentOriginalCopyMixin") ? 0 : sourceName.equals("net.farmingtales.ftboss.mixin.NativeTridentFieldCarrierMixin") ? 1 : -1;
        if (slot < 0 || !targetName.equals("net.minecraft.world.entity.projectile.AbstractArrow")) return;
        if (!OwnerFieldInstallationProof.nativeTridentHost(target) || info.getClass().getClassLoader() != IMixinInfo.class.getClassLoader() || !fieldTarget(info, targetName) || nativeTridentTarget != null && nativeTridentTarget != target || nativeTridentSources[slot] != null && nativeTridentSources[slot] != info) {invalid = true;return;}
        nativeTridentTarget = target;nativeTridentSources[slot] = info;
    }

    static synchronized void nativeTridentFinished(ITargetClassContext context) {
        ClassNode target = context.getClassNode();nativeTridentBaseline = null;nativeTridentLoader = null;nativeTridentOrder = null;
        if (!OwnerFieldInstallationProof.nativeTridentHost(target) || !NativeMapPutLateProofExtension.finishing(context)) throw new IllegalStateException("Untrusted native trident final callback");
        List<String> baseline = NativeMapPutLateProofExtension.parent(target);List<IExtension> order = NativeMapPutLateProofExtension.order();
        if (invalid || nativeTridentTarget != target || baseline == null || order == null || !operationSource(nativeTridentSources[0], "net.farmingtales.ftboss.mixin.OwnerTridentOriginalCopyMixin", "net.minecraft.world.entity.projectile.AbstractArrow") || !operationSource(nativeTridentSources[1], "net.farmingtales.ftboss.mixin.NativeTridentFieldCarrierMixin", "net.minecraft.world.entity.projectile.AbstractArrow")) return;
        baseline = List.copyOf(baseline);
        if (!OwnerFieldInstallationProof.nativeTridentWrapper(target, nativeTridentSources[0].getClassNode(0), nativeTridentSources[1].getClassNode(0), baseline)) return;
        nativeTridentBaseline = baseline;nativeTridentLoader = installationPlugin.getClass().getClassLoader();nativeTridentOrder = order;
    }

    static synchronized boolean nativeTridentCertified() {
        return !invalid && nativeTridentBaseline != null && nativeTridentOrder != null && nativeTridentOrder == NativeMapPutLateProofExtension.order() && nativeTridentLoader == installationPlugin.getClass().getClassLoader() && operationSource(nativeTridentSources[0], "net.farmingtales.ftboss.mixin.OwnerTridentOriginalCopyMixin", "net.minecraft.world.entity.projectile.AbstractArrow") && operationSource(nativeTridentSources[1], "net.farmingtales.ftboss.mixin.NativeTridentFieldCarrierMixin", "net.minecraft.world.entity.projectile.AbstractArrow") && OwnerFieldInstallationProof.nativeTridentWrapper(nativeTridentTarget, nativeTridentSources[0].getClassNode(0), nativeTridentSources[1].getClassNode(0), nativeTridentBaseline);
    }

    private static IMixinInfo nativeTickWearSource;
    private static ClassNode nativeTickWearTarget;
    private static List<String> nativeTickBaseline;
    private static ClassNode nativeTickTarget;
    private static IMixinInfo nativeTickSource;
    private static ClassLoader nativeTickLoader;
    private static List<IExtension> nativeTickOrder;

    private static void rememberNativeTickWear(String targetName, ClassNode target, String sourceName, IMixinInfo info) {
        if (!targetName.equals("net.minecraft.world.item.ItemStack") || !sourceName.equals("net.farmingtales.ftboss.mixin.OwnerStatueWearItemStackMixin")) return;
        if (target == null || !OwnerFieldInstallationProof.nativeTickHost(target) || info.getClass().getClassLoader() != IMixinInfo.class.getClassLoader() || !fieldTarget(info, targetName) || nativeTickWearSource != null && (nativeTickWearSource != info || nativeTickWearTarget != target)) {invalid = true;return;}
        nativeTickWearSource = info;nativeTickWearTarget = target;
    }

    static synchronized void nativeTickFinished(ITargetClassContext context) {
        ClassNode target = context.getClassNode();
        if (!OwnerFieldInstallationProof.nativeTickHost(target) || !NativeMapPutLateProofExtension.finishing(context)) throw new IllegalStateException("Untrusted native tick final callback");
        nativeTickBaseline = null;nativeTickTarget = null;nativeTickSource = null;nativeTickLoader = null;nativeTickOrder = null;
        int selected = -1;
        for (int index = 0; index < REQUIRED.length; index++) if (OwnerFieldInstallationProof.nativeTickSite(REQUIRED[index]) && TARGETS[index] == target) {INSTALLED[index] = false;if (selected >= 0) return;selected = index;}
        List<List<String>> parents = NativeMapPutLateProofExtension.ownerParents(target);List<IExtension> order = NativeMapPutLateProofExtension.order();
        if (invalid || selected < 0 || parents == null || parents.size() != 1 || parents.getFirst() == null || order == null || nativeTickWearTarget != target) return;
        IMixinInfo info = OBSERVED[selected];
        if (!operationSource(info, REQUIRED[selected][0], REQUIRED[selected][1]) || !operationSource(nativeTickWearSource, "net.farmingtales.ftboss.mixin.OwnerStatueWearItemStackMixin", REQUIRED[selected][1])) return;
        List<String> baseline = List.copyOf(parents.getFirst());
        if (!OwnerFieldInstallationProof.nativeTickWrapper(target, info.getClassNode(0), nativeTickWearSource.getClassNode(0), baseline)) return;
        nativeTickBaseline = baseline;nativeTickTarget = target;nativeTickSource = info;nativeTickLoader = installationPlugin.getClass().getClassLoader();nativeTickOrder = order;INSTALLED[selected] = true;
    }

    static synchronized boolean nativeTickComplete(String[] row, ClassNode target, ClassNode source) {
        if (invalid || !OwnerFieldInstallationProof.nativeTickSite(row) || !OwnerFieldInstallationProof.nativeTickHost(target) || source == null || nativeTickTarget != target || nativeTickWearTarget != target || nativeTickBaseline == null || nativeTickOrder == null || nativeTickOrder != NativeMapPutLateProofExtension.order() || nativeTickLoader != installationPlugin.getClass().getClassLoader() || !source.name.replace('/', '.').equals(row[0]) || !operationSource(nativeTickSource, row[0], row[1]) || !operationSource(nativeTickWearSource, "net.farmingtales.ftboss.mixin.OwnerStatueWearItemStackMixin", row[1])) return false;
        for (int index = 0; index < REQUIRED.length; index++) if (REQUIRED[index] == row) return INSTALLED[index] && TARGETS[index] == target && OBSERVED[index] == nativeTickSource && OwnerFieldInstallationProof.nativeTickWrapper(target, nativeTickSource.getClassNode(0), nativeTickWearSource.getClassNode(0), nativeTickBaseline);
        return false;
    }

    private static final List<List<List<String>>> nativePutBaselines = new ArrayList<>(Arrays.asList(null, null));
    private static final ClassNode[] nativePutTargets = new ClassNode[2];
    private static final IMixinInfo[] nativePutQuotes = new IMixinInfo[2];
    private static final IMixinInfo[] nativePutConsumers = new IMixinInfo[2];
    private static final ClassLoader[] nativePutLoaders = new ClassLoader[2];
    private static final List<List<IExtension>> nativePutOrders = new ArrayList<>(Arrays.asList(null, null));

    static synchronized void nativePutsFinished(ITargetClassContext context) {
        ClassNode target = context.getClassNode();int family = OwnerFieldInstallationProof.nativePutHost(target);
        if (family < 0 || !NativeMapPutLateProofExtension.finishing(context)) throw new IllegalStateException("Untrusted native put final callback");
        nativePutBaselines.set(family, null);nativePutTargets[family] = null;nativePutQuotes[family] = null;nativePutConsumers[family] = null;nativePutLoaders[family] = null;nativePutOrders.set(family, null);
        int quoteIndex = -1, consumerIndex = -1;
        for (int index = 0; index < REQUIRED.length; index++) {
            int slot = OwnerFieldInstallationProof.nativePutSlot(REQUIRED[index]);
            if (slot >= 0 && slot / 2 == family && TARGETS[index] == target) {INSTALLED[index] = false;if (slot % 2 == 0) quoteIndex = index;else consumerIndex = index;}
        }
        List<String> constructor = NativeMapPutLateProofExtension.parent(target);List<List<String>> parents = NativeMapPutLateProofExtension.ownerParents(target);List<IExtension> order = NativeMapPutLateProofExtension.order();
        if (invalid || quoteIndex < 0 || consumerIndex < 0 || constructor == null || parents == null || parents.size() != 1 || parents.getFirst() == null || order == null) return;
        IMixinInfo quote = OBSERVED[quoteIndex], consumer = OBSERVED[consumerIndex];
        if (!operationSource(quote, REQUIRED[quoteIndex][0], REQUIRED[quoteIndex][1]) || !operationSource(consumer, REQUIRED[consumerIndex][0], REQUIRED[consumerIndex][1])) return;
        List<List<String>> baselines = List.of(List.copyOf(constructor), List.copyOf(parents.getFirst()));
        if (!OwnerFieldInstallationProof.nativePutWrapper(target, quote.getClassNode(0), consumer.getClassNode(0), baselines)) return;
        nativePutBaselines.set(family, baselines);nativePutTargets[family] = target;nativePutQuotes[family] = quote;nativePutConsumers[family] = consumer;nativePutLoaders[family] = installationPlugin.getClass().getClassLoader();nativePutOrders.set(family, order);INSTALLED[quoteIndex] = true;INSTALLED[consumerIndex] = true;
    }

    static synchronized boolean nativePutComplete(String[] row, ClassNode target, ClassNode source) {
        int slot = OwnerFieldInstallationProof.nativePutSlot(row), family = slot / 2;
        if (invalid || slot < 0 || target == null || source == null || nativePutTargets[family] != target || nativePutBaselines.get(family) == null || nativePutOrders.get(family) == null || nativePutOrders.get(family) != NativeMapPutLateProofExtension.order() || nativePutLoaders[family] != installationPlugin.getClass().getClassLoader() || !source.name.replace('/', '.').equals(row[0])) return false;
        IMixinInfo quote = nativePutQuotes[family], consumer = nativePutConsumers[family];String targetName = target.name.replace('/', '.');
        if (!operationSource(quote, "net.farmingtales.ftboss.mixin.OwnerNativePutQuoteMixin", targetName) || !operationSource(consumer, "net.farmingtales.ftboss.mixin.OwnerNativePutConsumerMixin", targetName)) return false;
        for (int index = 0; index < REQUIRED.length; index++) if (REQUIRED[index] == row) return INSTALLED[index] && TARGETS[index] == target && OBSERVED[index] == (slot % 2 == 0 ? quote : consumer) && OwnerFieldInstallationProof.nativePutWrapper(target, quote.getClassNode(0), consumer.getClassNode(0), nativePutBaselines.get(family));
        return false;
    }

    private static List<String> nativeCaptureBaseline;
    private static ClassNode nativeCaptureTarget;
    private static IMixinInfo nativeCaptureSource;
    private static ClassLoader nativeCaptureLoader;
    private static List<IExtension> nativeCaptureOrder;

    static synchronized void nativeCaptureFinished(ITargetClassContext context) {
        ClassNode target = context.getClassNode();
        if (!OwnerFieldInstallationProof.nativeCaptureHost(target) || !NativeMapPutLateProofExtension.finishing(context)) throw new IllegalStateException("Untrusted native capture final callback");
        nativeCaptureBaseline = null;nativeCaptureTarget = null;nativeCaptureSource = null;nativeCaptureLoader = null;nativeCaptureOrder = null;
        int selected = -1;
        for (int index = 0; index < REQUIRED.length; index++) if (OwnerFieldInstallationProof.nativeCaptureSite(REQUIRED[index]) && TARGETS[index] == target) {INSTALLED[index] = false;if (selected >= 0) return;selected = index;}
        List<String> parent = NativeMapPutLateProofExtension.parent(target);List<IExtension> order = NativeMapPutLateProofExtension.order();
        if (invalid || selected < 0 || parent == null || order == null) return;
        IMixinInfo info = OBSERVED[selected];
        if (!operationSource(info, REQUIRED[selected][0], REQUIRED[selected][1])) return;
        List<String> baseline = List.copyOf(parent);
        if (!OwnerFieldInstallationProof.nativeCaptureWrapper(target, info.getClassNode(0), baseline)) return;
        nativeCaptureBaseline = baseline;nativeCaptureTarget = target;nativeCaptureSource = info;nativeCaptureLoader = installationPlugin.getClass().getClassLoader();nativeCaptureOrder = order;INSTALLED[selected] = true;
    }

    static synchronized boolean nativeCaptureComplete(String[] row, ClassNode target, ClassNode source) {
        if (invalid || !OwnerFieldInstallationProof.nativeCaptureSite(row) || !OwnerFieldInstallationProof.nativeCaptureHost(target) || source == null || nativeCaptureTarget != target || nativeCaptureBaseline == null || nativeCaptureOrder == null || nativeCaptureOrder != NativeMapPutLateProofExtension.order() || nativeCaptureLoader != installationPlugin.getClass().getClassLoader() || !source.name.replace('/', '.').equals(row[0]) || !operationSource(nativeCaptureSource, row[0], row[1])) return false;
        for (int index = 0; index < REQUIRED.length; index++) if (REQUIRED[index] == row) return INSTALLED[index] && TARGETS[index] == target && OBSERVED[index] == nativeCaptureSource && OwnerFieldInstallationProof.nativeCaptureWrapper(target, nativeCaptureSource.getClassNode(0), nativeCaptureBaseline);
        return false;
    }

    static synchronized void nativeChainsFinished(ITargetClassContext context) {
        ClassNode target = context.getClassNode();int family = OwnerFieldInstallationProof.nativeChainHost(target);
        if (family < 0 || !NativeMapPutLateProofExtension.finishing(context)) throw new IllegalStateException("Untrusted native chain final callback");
        nativeChainBaselines.set(family, null);nativeChainTargets[family] = null;nativeChainSources[family] = null;nativeChainCompanions[family] = null;nativeChainLoaders[family] = null;nativeChainOrders.set(family, null);
        int primary = -1, companion = -1;
        for (int index = 0; index < REQUIRED.length; index++) {
            int slot = OwnerFieldInstallationProof.nativeChainSlot(REQUIRED[index]);
            if (slot >= 0 && (slot > 1 ? 2 : slot) == family && TARGETS[index] == target) {INSTALLED[index] = false;if (slot == 3) companion = index;else primary = index;}
        }
        List<List<String>> parents = NativeMapPutLateProofExtension.ownerParents(target);List<IExtension> order = NativeMapPutLateProofExtension.order();
        if (invalid || primary < 0 || family == 2 && companion < 0 || parents == null || parents.size() != 1 || parents.getFirst() == null || order == null) return;
        IMixinInfo info = OBSERVED[primary], other = family == 2 ? OBSERVED[companion] : null;
        if (!operationSource(info, REQUIRED[primary][0], REQUIRED[primary][1]) || family == 2 && !operationSource(other, REQUIRED[companion][0], REQUIRED[companion][1])) return;
        List<String> baseline = List.copyOf(parents.getFirst());
        if (!OwnerFieldInstallationProof.nativeChainWrapper(family, target, info.getClassNode(0), other == null ? null : other.getClassNode(0), baseline)) return;
        nativeChainBaselines.set(family, baseline);nativeChainTargets[family] = target;nativeChainSources[family] = info;nativeChainCompanions[family] = other;nativeChainLoaders[family] = installationPlugin.getClass().getClassLoader();nativeChainOrders.set(family, order);
        INSTALLED[primary] = true;if (companion >= 0) INSTALLED[companion] = true;
    }

    static synchronized boolean nativeChainComplete(String[] row, ClassNode target, ClassNode source) {
        int slot = OwnerFieldInstallationProof.nativeChainSlot(row), family = slot > 1 ? 2 : slot;
        if (invalid || family < 0 || target == null || source == null || nativeChainTargets[family] != target || nativeChainBaselines.get(family) == null || nativeChainOrders.get(family) == null || nativeChainOrders.get(family) != NativeMapPutLateProofExtension.order() || nativeChainLoaders[family] != installationPlugin.getClass().getClassLoader() || !source.name.replace('/', '.').equals(row[0])) return false;
        IMixinInfo info = nativeChainSources[family], other = nativeChainCompanions[family];
        if (!operationSource(info, OwnerFieldInstallationProof.nativeChainSource(family), target.name.replace('/', '.')) || family == 2 && !operationSource(other, OwnerFieldInstallationProof.nativeChainSource(3), target.name.replace('/', '.'))) return false;
        for (int index = 0; index < REQUIRED.length; index++) if (REQUIRED[index] == row) return INSTALLED[index] && TARGETS[index] == target && OBSERVED[index] == (slot == 3 ? other : info) && OwnerFieldInstallationProof.nativeChainWrapper(family, target, info.getClassNode(0), other == null ? null : other.getClassNode(0), nativeChainBaselines.get(family));
        return false;
    }

    private static void rememberNativeRegistration(String targetName, ClassNode target, String sourceName, IMixinInfo info) {
        if (!targetName.equals("net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent") || !sourceName.equals(OwnerFieldInstallationProof.nativeLoadSource(3))) return;
        if (target == null || OwnerFieldInstallationProof.nativeLoadHost(target) != 3 || info.getClass().getClassLoader() != IMixinInfo.class.getClassLoader() || !fieldTarget(info, targetName) || nativeRegistrationInfo != null && (nativeRegistrationInfo != info || nativeRegistrationTarget != target)) {invalid = true;return;}
        nativeRegistrationInfo = info;nativeRegistrationTarget = target;
    }

    static synchronized void nativeLoadsFinished(ITargetClassContext context) {
        ClassNode target = context.getClassNode();int slot = OwnerFieldInstallationProof.nativeLoadHost(target);
        if (slot < 0 || !NativeMapPutLateProofExtension.finishing(context)) throw new IllegalStateException("Untrusted native load final callback");
        nativeLoadBaselines.set(slot, null);nativeLoadTargets[slot] = null;nativeLoadSources[slot] = null;nativeLoadLoaders[slot] = null;nativeLoadOrders.set(slot, null);
        int required = -1;
        for (int index = 0; index < REQUIRED.length; index++) if (OwnerFieldInstallationProof.nativeLoadSlot(REQUIRED[index]) == slot && TARGETS[index] == target) {INSTALLED[index] = false;required = index;}
        IMixinInfo info = slot == 3 ? nativeRegistrationTarget == target ? nativeRegistrationInfo : null : required < 0 ? null : OBSERVED[required];
        List<String> baseline = NativeMapPutLateProofExtension.parent(target);List<IExtension> order = NativeMapPutLateProofExtension.order();
        if (invalid || !operationSource(info, OwnerFieldInstallationProof.nativeLoadSource(slot), target.name.replace('/', '.')) || baseline == null || order == null) return;
        baseline = List.copyOf(baseline);
        if (!OwnerFieldInstallationProof.nativeLoadWrapper(slot, target, info.getClassNode(0), baseline)) return;
        nativeLoadBaselines.set(slot, baseline);nativeLoadTargets[slot] = target;nativeLoadSources[slot] = info;nativeLoadLoaders[slot] = installationPlugin.getClass().getClassLoader();nativeLoadOrders.set(slot, order);
        if (required >= 0) INSTALLED[required] = true;
    }

    private static boolean nativeLoadCurrent(int slot, ClassNode target) {
        return !invalid && slot >= 0 && slot < 4 && target != null && nativeLoadTargets[slot] == target && nativeLoadBaselines.get(slot) != null && nativeLoadOrders.get(slot) != null && nativeLoadOrders.get(slot) == NativeMapPutLateProofExtension.order() && nativeLoadLoaders[slot] == installationPlugin.getClass().getClassLoader() && operationSource(nativeLoadSources[slot], OwnerFieldInstallationProof.nativeLoadSource(slot), target.name.replace('/', '.'));
    }

    static synchronized boolean nativeLoadComplete(String[] row, ClassNode target, ClassNode source) {
        int slot = OwnerFieldInstallationProof.nativeLoadSlot(row);
        if (slot < 0 || source == null || !source.name.replace('/', '.').equals(row[0]) || !nativeLoadCurrent(slot, target)) return false;
        for (int index = 0; index < REQUIRED.length; index++) if (REQUIRED[index] == row) return INSTALLED[index] && TARGETS[index] == target && OBSERVED[index] == nativeLoadSources[slot] && OwnerFieldInstallationProof.nativeLoadWrapper(slot, target, nativeLoadSources[slot].getClassNode(0), nativeLoadBaselines.get(slot));
        return false;
    }

    static synchronized boolean nativeRegistrationCertified() {
        return nativeLoadCurrent(3, nativeRegistrationTarget) && nativeLoadSources[3] == nativeRegistrationInfo && OwnerFieldInstallationProof.nativeLoadWrapper(3, nativeRegistrationTarget, nativeRegistrationInfo.getClassNode(0), nativeLoadBaselines.get(3));
    }

    private static boolean operationSource(IMixinInfo info, String source, String target) {
        return info != null && registeredConfiguration(info) && info.getClass().getClassLoader() == IMixinInfo.class.getClassLoader() && info.getClassName().equals(source) && fieldTarget(info, target);
    }

    static synchronized void operationsFinished(ITargetClassContext context) {
        ClassNode target = context.getClassNode();
        int slot = target.name.equals("net/farmingtales/ftboss/item/FoodWeaponHitPlan") ? 0 : target.name.equals("net/farmingtales/ftboss/item/FoodWeaponRanged") ? 1 : -1;
        if (slot < 0 || !NativeMapPutLateProofExtension.finishing(context)) throw new IllegalStateException("Untrusted native operation final callback");
        operationBaselines.set(slot, null);operationTargets[slot] = null;operationSources[slot] = null;operationCompanions[slot] = null;operationLoaders[slot] = null;operationOrders.set(slot, null);
        int primary = -1, companion = -1;
        for (int index = 0; index < REQUIRED.length; index++) if (OwnerFieldInstallationProof.nativeOperationSlot(REQUIRED[index]) == slot && TARGETS[index] == target) {
            INSTALLED[index] = false;
            if (REQUIRED[index][0].equals("net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin")) companion = index; else primary = index;
        }
        if (slot == 0) for (int index = 0; index < REQUIRED.length; index++) if (REQUIRED[index][0].equals("net.farmingtales.ftboss.mixin.NativeExpectedActualStepMixin") && TARGETS[index] == target && INSTALLED[index]) companion = index;
        if (invalid || primary < 0 || companion < 0) return;
        IMixinInfo info = OBSERVED[primary], other = companion < 0 ? null : OBSERVED[companion];
        if (!operationSource(info, REQUIRED[primary][0], REQUIRED[primary][1]) || !operationSource(other, REQUIRED[companion][0], REQUIRED[companion][1])) return;
        List<List<String>> baselines;
        if (slot == 0) {List<String> parent = NativeMapPutLateProofExtension.parent(target);if (parent == null) return;baselines = List.of(List.copyOf(parent));}
        else {List<List<String>> parents = NativeMapPutLateProofExtension.ownerParents(target);if (parents == null) return;List<List<String>> copied = new ArrayList<>();for (List<String> parent : parents) copied.add(List.copyOf(parent));baselines = List.copyOf(copied);}
        List<IExtension> order = NativeMapPutLateProofExtension.order();
        if (order == null || !OwnerFieldInstallationProof.nativeOperationWrapper(slot, target, info.getClassNode(0), other == null ? null : other.getClassNode(0), baselines)) return;
        operationBaselines.set(slot, baselines);operationTargets[slot] = target;operationSources[slot] = info;operationCompanions[slot] = other;operationLoaders[slot] = installationPlugin.getClass().getClassLoader();operationOrders.set(slot, order);
        INSTALLED[primary] = true;if (companion >= 0) INSTALLED[companion] = true;
    }

    static synchronized boolean operationComplete(String[] row, ClassNode target, ClassNode source) {
        int slot = OwnerFieldInstallationProof.nativeOperationSlot(row);
        if (invalid || slot < 0 || target == null || source == null || operationBaselines.get(slot) == null || operationTargets[slot] != target || operationLoaders[slot] != installationPlugin.getClass().getClassLoader() || operationOrders.get(slot) == null || operationOrders.get(slot) != NativeMapPutLateProofExtension.order()) return false;
        IMixinInfo info = operationSources[slot], other = operationCompanions[slot];
        String primary = slot == 0 ? "net.farmingtales.ftboss.mixin.OwnerTimerSemanticConsumerMixin" : "net.farmingtales.ftboss.mixin.OwnerTimerRangedGuardMixin";
        if (!operationSource(info, primary, row[1]) || !operationSource(other, slot == 0 ? "net.farmingtales.ftboss.mixin.NativeExpectedActualStepMixin" : "net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin", row[1]) || !source.name.replace('/', '.').equals(row[0])) return false;
        for (int index = 0; index < REQUIRED.length; index++) if (REQUIRED[index] == row) {
            if (!INSTALLED[index] || TARGETS[index] != target || OBSERVED[index] != (row[0].equals(primary) ? info : other)) return false;
            return OwnerFieldInstallationProof.nativeOperationWrapper(slot, target, info.getClassNode(0), other == null ? null : other.getClassNode(0), operationBaselines.get(slot));
        }
        return false;
    }

    private static void entityFinished(ITargetClassContext context) {
        ClassNode target = context.getClassNode();
        if (!target.name.equals("net/minecraft/world/entity/Entity")) return;
        entityParentBaseline = null; entityParentTarget = null; entityParentSource = null; entityParentLoader = null; entityParentOrder = null;
        for (int index = 0; index < REQUIRED.length; index++) if (OwnerFieldInstallationProof.entityOwnerSite(REQUIRED[index]) && TARGETS[index] == target) {
            INSTALLED[index] = false;
            IMixinInfo info = OBSERVED[index];
            List<String> baseline = NativeMapPutLateProofExtension.parent(target);
            if (invalid || !NativeMapPutLateProofExtension.finishing(context) || !registeredConfiguration(info) || info.getClass().getClassLoader() != IMixinInfo.class.getClassLoader() || !info.getClassName().equals(REQUIRED[index][0]) || !fieldTarget(info, REQUIRED[index][1]) || baseline == null) return;
            baseline = List.copyOf(baseline);
            if (!OwnerFieldInstallationProof.entityLoadWrapper(REQUIRED[index], target, info.getClassNode(0), baseline)) return;
            List<IExtension> order = NativeMapPutLateProofExtension.order();
            if (order == null) return;
            entityParentBaseline = baseline; entityParentTarget = target; entityParentSource = info; entityParentLoader = installationPlugin.getClass().getClassLoader(); entityParentOrder = order;
            INSTALLED[index] = true;
        }
    }

    static synchronized List<String> entityParent(String[] row, ClassNode target, ClassNode source) {
        if (invalid || !OwnerFieldInstallationProof.entityOwnerSite(row) || target == null || source == null || entityParentBaseline == null || entityParentTarget != target || entityParentSource == null || !registeredConfiguration(entityParentSource) || entityParentSource.getClass().getClassLoader() != IMixinInfo.class.getClassLoader() || entityParentLoader != installationPlugin.getClass().getClassLoader() || entityParentOrder == null || entityParentOrder != NativeMapPutLateProofExtension.order() || !source.name.replace('/', '.').equals(row[0]) || !target.name.replace('/', '.').equals(row[1]) || !fieldTarget(entityParentSource, row[1])) return null;
        for (int index = 0; index < REQUIRED.length; index++) if (REQUIRED[index] == row) return INSTALLED[index] && TARGETS[index] == target && OBSERVED[index] == entityParentSource ? entityParentBaseline : null;
        return null;
    }

    static synchronized void fieldsFinished(ITargetClassContext context) {
        if (!NativeMapPutLateProofExtension.finishing(context) || !OwnerFieldInstallationProof.observedFieldHost(context.getClassNode())) throw new IllegalStateException("Untrusted native field getter final callback");
        refreshFieldViews(context.getClassNode());
        entityFinished(context);
    }

    static synchronized void lateApplied(ITargetClassContext var0) {
        if (NativeMapPutLateProofExtension.finishing(var0)) {
            for (int var1 = 0; var1 < REQUIRED.length; var1++) {
                int var2 = OwnerFieldInstallationProof.lateSlot(REQUIRED[var1]);
                if (var2 >= 0 && TARGETS[var1] == var0.getClassNode() && OBSERVED[var1] != null) {
                    IMixinInfo var3 = OBSERVED[var1];
                    if (registeredConfiguration(var3)) {
                        Class var4 = installationPlugin.getClass();
                        WeakReference var5 = lateContexts.get(var2);
                        if (lateStages[var2] == 1
                            && var5 != null
                            && var5.get() == var0
                            && lateThreads[var2] == Thread.currentThread()
                            && lateLoaders[var2] == var4.getClassLoader()) {
                            lateStages[var2] = 3;

                            try {
                                lateOrders.set(var2, NativeMapPutLateProofExtension.order());
                                String var6 = REQUIRED[var1][1];
                                if (var3.getClass().getClassLoader() == IMixinInfo.class.getClassLoader()
                                    && REQUIRED[var1][0].equals(var3.getClassName())
                                    && registeredConfiguration(var3)
                                    && var3.getTargetClasses().stream().anyMatch(var1x -> var6.equals(var1x.replace('/', '.')))
                                    && var3.getConfig().getTargets().stream().anyMatch(var1x -> var6.equals(var1x.replace('/', '.')))) {
                                    List<String> var7 = var2 == 1 ? NativeMapPutLateProofExtension.parent(TARGETS[var1]) : null;
                                    if (var7 != null) {
                                        var7 = List.copyOf(var7);
                                    }

                                    List<List<String>> var8 = var2 >= 2 ? NativeMapPutLateProofExtension.ownerParents(TARGETS[var1]) : null;
                                    if (var8 != null) {
                                        ArrayList<List<String>> var9 = new ArrayList<>();

                                        for (List<String> var11 : var8) {
                                            var9.add(List.copyOf(var11));
                                        }

                                        var8 = List.copyOf(var9);
                                    }

                                    INSTALLED[var1] = var2 >= 2
                                        ? OwnerFieldInstallationProof.ownerWrapper(REQUIRED[var1], TARGETS[var1], var3.getClassNode(0), var8)
                                        : (
                                            var2 == 1
                                                ? OwnerFieldInstallationProof.timerWrapper(REQUIRED[var1], TARGETS[var1], var3.getClassNode(0), var7)
                                                : OwnerFieldInstallationProof.complete(REQUIRED[var1], TARGETS[var1], var3.getClassNode(0))
                                        );
                                    if (INSTALLED[var1] && lateOrders.get(var2) != null && lateOrders.get(var2) == NativeMapPutLateProofExtension.order()) {
                                        if (var2 == 1) {
                                            timerParentBaseline = var7;
                                            timerParentTarget = TARGETS[var1];
                                            timerParentSource = var3;
                                            timerParentLoader = lateLoaders[var2];
                                            timerParentOrder = lateOrders.get(var2);
                                        } else if (var2 >= 2) {
                                            int var37 = var2 - 2;
                                            ownerParentBaselines.set(var37, var8);
                                            ownerParentTargets[var37] = TARGETS[var1];
                                            ownerParentSources[var37] = var3;
                                            ownerParentLoaders[var37] = lateLoaders[var2];
                                            ownerParentOrders.set(var37, lateOrders.get(var2));
                                        }

                                        if (var2 == 0) NativeFreshFactoryTrust298.installRangedTimer(var0);
                                        lateStages[var2] = 2;
                                    } else {
                                        INSTALLED[var1] = false;
                                    }
                                }
                            } catch (Throwable var25) {
                                INSTALLED[var1] = false;
                            } finally {
                                if (var2 == 1 && lateStages[var2] != 2) {
                                    timerParentBaseline = null;
                                    timerParentTarget = null;
                                    timerParentSource = null;
                                    timerParentLoader = null;
                                    timerParentOrder = null;
                                } else if (var2 >= 2 && lateStages[var2] != 2) {
                                    int var13 = var2 - 2;
                                    ownerParentBaselines.set(var13, null);
                                    ownerParentTargets[var13] = null;
                                    ownerParentSources[var13] = null;
                                    ownerParentLoaders[var13] = null;
                                    ownerParentOrders.set(var13, null);
                                }

                                lateContexts.set(var2, null);
                                lateThreads[var2] = null;
                                lateLoaders[var2] = null;
                            }
                        }
                    }
                }
            }

            for (int var27 = 0; var27 < REQUIRED.length; var27++) {
                String[] var28 = REQUIRED[var27];
                if (OwnerFieldInstallationProof.canonicalAliasSite(var28) && TARGETS[var27] == var0.getClassNode() && OBSERVED[var27] != null) {
                    IMixinInfo var29 = OBSERVED[var27];
                    boolean var30 = var28[0].equals("net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin");
                    INSTALLED[var27] = false;

                    try {
                        if (registeredConfiguration(var29)
                            && var29.getClass().getClassLoader() == IMixinInfo.class.getClassLoader()
                            && var28[0].equals(var29.getClassName())
                            && var28[1].equals(TARGETS[var27].name.replace('/', '.'))
                            && !var29.getTargetClasses().stream().noneMatch(var1x -> var28[1].equals(var1x.replace('/', '.')))
                            && !var29.getConfig().getTargets().stream().noneMatch(var1x -> var28[1].equals(var1x.replace('/', '.')))) {
                            if (!var30) {
                                INSTALLED[var27] = OwnerFieldInstallationProof.complete(var28, TARGETS[var27], var29.getClassNode(0));
                            } else {
                                List<List<String>> var31 = NativeMapPutLateProofExtension.ownerParents(TARGETS[var27]);
                                if (var31 != null) {
                                    ArrayList<List<String>> var33 = new ArrayList<>();

                                    for (List<String> var36 : var31) {
                                        var33.add(List.copyOf(var36));
                                    }

                                    var31 = List.copyOf(var33);
                                    List var35 = NativeMapPutLateProofExtension.order();
                                    INSTALLED[var27] = var35 != null
                                        && OwnerFieldInstallationProof.canonicalRangedWrapper(var28, TARGETS[var27], var29.getClassNode(0), var31);
                                    if (INSTALLED[var27]) {
                                        rangedParentBaseline = var31;
                                        rangedParentTarget = TARGETS[var27];
                                        rangedParentSource = var29;
                                        rangedParentLoader = installationPlugin.getClass().getClassLoader();
                                        rangedParentOrder = var35;
                                    }
                                }
                            }
                        }
                    } catch (Throwable var23) {
                        INSTALLED[var27] = false;
                    } finally {
                        if (var30 && !INSTALLED[var27]) {
                            rangedParentBaseline = null;
                            rangedParentTarget = null;
                            rangedParentSource = null;
                            rangedParentLoader = null;
                            rangedParentOrder = null;
                        }
                    }
                }
            }
        }
    }

    static synchronized List<String> timerParent(ClassNode var0, ClassNode var1) {
        if (!invalid
            && var0 != null
            && var1 != null
            && lateStages[1] == 2
            && timerParentBaseline != null
            && timerParentTarget == var0
            && timerParentSource != null
            && registeredConfiguration(timerParentSource)
            && timerParentLoader == installationPlugin.getClass().getClassLoader()
            && timerParentOrder != null
            && timerParentOrder == lateOrders.get(1)
            && timerParentOrder == NativeMapPutLateProofExtension.order()) {
            for (int var2 = 0; var2 < REQUIRED.length; var2++) {
                if (OwnerFieldInstallationProof.lateTimerSite(REQUIRED[var2])) {
                    String var3 = REQUIRED[var2][1];
                    if (INSTALLED[var2]
                        && TARGETS[var2] == var0
                        && OBSERVED[var2] == timerParentSource
                        && REQUIRED[var2][0].equals(var1.name.replace('/', '.'))
                        && REQUIRED[var2][1].equals(var0.name.replace('/', '.'))
                        && REQUIRED[var2][0].equals(timerParentSource.getClassName())
                        && timerParentSource.getClass().getClassLoader() == IMixinInfo.class.getClassLoader()
                        && !timerParentSource.getTargetClasses().stream().noneMatch(var1x -> var3.equals(var1x.replace('/', '.')))) {
                        return timerParentBaseline;
                    }

                    return null;
                }
            }

            return null;
        } else {
            return null;
        }
    }

    static synchronized List<List<String>> ownerParents(String[] var0, ClassNode var1, ClassNode var2) {
        int var3 = OwnerFieldInstallationProof.ownerSlot(var0);
        if (!invalid && var3 >= 2 && var1 != null && var2 != null && lateStages[var3] == 2) {
            int var4 = var3 - 2;
            IMixinInfo var5 = ownerParentSources[var4];
            List var6 = ownerParentOrders.get(var4);
            String var7 = var0[1];
            if (ownerParentBaselines.get(var4) != null
                && ownerParentTargets[var4] == var1
                && var5 != null
                && registeredConfiguration(var5)
                && ownerParentLoaders[var4] == installationPlugin.getClass().getClassLoader()
                && var6 != null
                && var6 == lateOrders.get(var3)
                && var6 == NativeMapPutLateProofExtension.order()
                && var0[0].equals(var2.name.replace('/', '.'))
                && var7.equals(var1.name.replace('/', '.'))
                && var0[0].equals(var5.getClassName())
                && var5.getClass().getClassLoader() == IMixinInfo.class.getClassLoader()
                && !var5.getTargetClasses().stream().noneMatch(var1x -> var7.equals(var1x.replace('/', '.')))) {
                for (int var8 = 0; var8 < REQUIRED.length; var8++) {
                    if (REQUIRED[var8] == var0) {
                        return INSTALLED[var8] && TARGETS[var8] == var1 && OBSERVED[var8] == var5 ? ownerParentBaselines.get(var4) : null;
                    }
                }

                return null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    static synchronized List<List<String>> rangedParents(String[] var0, ClassNode var1, ClassNode var2) {
        if (!invalid
            && OwnerFieldInstallationProof.canonicalAliasSite(var0)
            && var0[0].equals("net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin")
            && var1 != null
            && var2 != null
            && rangedParentBaseline != null
            && rangedParentTarget == var1
            && rangedParentSource != null
            && registeredConfiguration(rangedParentSource)
            && rangedParentLoader == installationPlugin.getClass().getClassLoader()
            && rangedParentOrder != null
            && rangedParentOrder == NativeMapPutLateProofExtension.order()
            && var0[0].equals(var2.name.replace('/', '.'))
            && var0[1].equals(var1.name.replace('/', '.'))
            && var0[0].equals(rangedParentSource.getClassName())
            && rangedParentSource.getClass().getClassLoader() == IMixinInfo.class.getClassLoader()
            && !rangedParentSource.getTargetClasses().stream().noneMatch(var1x -> var0[1].equals(var1x.replace('/', '.')))) {
            for (int var3 = 0; var3 < REQUIRED.length; var3++) {
                if (REQUIRED[var3] == var0) {
                    return INSTALLED[var3] && TARGETS[var3] == var1 && OBSERVED[var3] == rangedParentSource ? rangedParentBaseline : null;
                }
            }

            return null;
        } else {
            return null;
        }
    }

    static synchronized ClassNode timerSource(String var0, ClassNode var1) {
        for (int var2 = 0; var2 < REQUIRED.length; var2++) {
            if (REQUIRED[var2][0].equals(var0) && TARGETS[var2] == var1 && OBSERVED[var2] != null) {
                return OBSERVED[var2].getClassNode(0);
            }
        }

        return null;
    }

    static synchronized boolean pending(Plan var0) {
        if (!invalid && var0 != null) {
            for (boolean var4 : INSTALLED) {
                if (!var4) {
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    static synchronized Object generation(Plan var0) {
        if (!invalid && var0 != null) {
            for (int var1 = 0; var1 < lateStages.length; var1++) {
                if (lateStages[var1] == 2 && (lateOrders.get(var1) == null || lateOrders.get(var1) != NativeMapPutLateProofExtension.order())) {
                    return null;
                }

                if (lateStages[var1] == 1 && (lateContexts.get(var1) == null || lateContexts.get(var1).get() == null)) {
                    lateStages[var1] = 3;
                    lateContexts.set(var1, null);
                    lateThreads[var1] = null;
                    lateLoaders[var1] = null;
                }
            }

            for (int var5 = 0; var5 < INSTALLED.length; var5++) {
                int var2 = OwnerFieldInstallationProof.lateSlot(REQUIRED[var5]);
                if (var2 >= 0 && lateStages[var2] != 2) {
                    return null;
                }

                if (nativeCapabilityLifecycleGenerationCertified202(var5)) {
                    continue;
                }

                if (nativeTridentGenerationCertified168(var5)) {
                    continue;
                }

                if (nativeItemStackGenerationCertified169(var5)) {
                    continue;
                }

                if (nativeCapabilityGenerationCertified171(var5)) {
                    continue;
                }

                if (nativeStackFieldsGenerationCertified172(var5)) {
                    continue;
                }

                if (nativeProviderFieldsGenerationCertified173(var5)) {
                    continue;
                }

                if (nativeDispatcherRevocationGenerationCertified193(var5)) {
                    continue;
                }

                if (nativeProviderRevocationGenerationCertified191(var5)) {
                    continue;
                }

                if (nativeProviderRawExposureGenerationCertified189(var5)) {
                    continue;
                }

                if (nativeAssignmentGenerationCertified185(var5)) {
                    continue;
                }

                if (!INSTALLED[var5] || OBSERVED[var5] == null || TARGETS[var5] == null || !registeredConfiguration(OBSERVED[var5])) {
                    return null;
                }

                try {
                    if (!OwnerFieldInstallationProof.complete(REQUIRED[var5], TARGETS[var5], OBSERVED[var5].getClassNode(0))) {
                        return null;
                    }
                } catch (Throwable var4) {
                    return null;
                }
            }

            return GENERATION;
        } else {
            return null;
        }
    }

    static synchronized boolean current(Object var0, Plan var1) {
        return var0 != null && var0 == generation(var1);
    }

    private static final List<Map<String, IMixinInfo>> nativeComponentSources = new ArrayList<>(List.of(new java.util.LinkedHashMap<>(), new java.util.LinkedHashMap<>(), new java.util.LinkedHashMap<>()));
    private static final ClassNode[] nativeComponentTargets = new ClassNode[3];
    private static final List<Map<String, String>> nativeComponentBaselines = new ArrayList<>(Arrays.asList(null, null, null));
    private static final List<List<IExtension>> nativeComponentOrders = new ArrayList<>(Arrays.asList(null, null, null));
    private static final ClassLoader[] nativeComponentLoaders = new ClassLoader[3];
    private static final Thread[] nativeComponentThreads = new Thread[3];

    private static void rememberNativeComponents(String targetName, ClassNode target, String sourceName, IMixinInfo info) {
        int slot = NativeComponentInstallationProof163.host(target);
        if (slot < 0 || !NativeComponentInstallationProof163.requiredSources(target).stream().anyMatch(name -> sourceName.equals("net.farmingtales.ftboss.mixin." + name))) return;
        if (installationConfig == null) { installationConfig = info.getConfig(); installationPlugin = installationConfig.getPlugin(); }
        String name = sourceName.substring(sourceName.lastIndexOf('.') + 1);
        if (!target.name.replace('/', '.').equals(targetName) || !operationSource(info, sourceName, targetName) || nativeComponentTargets[slot] != null && nativeComponentTargets[slot] != target || nativeComponentSources.get(slot).containsKey(name) && nativeComponentSources.get(slot).get(name) != info) { invalid = true; return; }
        nativeComponentTargets[slot] = target; nativeComponentSources.get(slot).put(name, info);
    }

    static synchronized void nativeComponentsFinished(ITargetClassContext context) {
        ClassNode target = context.getClassNode(); int slot = NativeComponentInstallationProof163.host(target);
        if (slot < 0 || !NativeMapPutLateProofExtension.finishing(context)) throw new IllegalStateException("Untrusted actual native component final callback");
        nativeComponentBaselines.set(slot, null); nativeComponentOrders.set(slot, null); nativeComponentLoaders[slot] = null; nativeComponentThreads[slot] = null;
        Map<String, String> baseline = NativeMapPutLateProofExtension.componentParents(target); List<IExtension> order = NativeMapPutLateProofExtension.order();
        if (invalid || nativeComponentTargets[slot] != target || baseline == null || order == null) return;
        Map<String, ClassNode> sources = nativeComponentSourceNodes(slot);
        if (sources == null) return;
        try { NativeComponentInstallationProof163.verify(target, sources, baseline); } catch (RuntimeException failure) { return; }
        nativeComponentBaselines.set(slot, Map.copyOf(baseline)); nativeComponentOrders.set(slot, order); nativeComponentLoaders[slot] = installationPlugin.getClass().getClassLoader(); nativeComponentThreads[slot] = Thread.currentThread();
    }

    private static Map<String, ClassNode> nativeComponentSourceNodes(int slot) {
        ClassNode target = nativeComponentTargets[slot]; if (target == null) return null;
        Map<String, IMixinInfo> observed = nativeComponentSources.get(slot);
        if (!observed.keySet().equals(Set.copyOf(NativeComponentInstallationProof163.requiredSources(target)))) return null;
        Map<String, ClassNode> result = new java.util.LinkedHashMap<>();
        for (var entry : observed.entrySet()) {
            if (!operationSource(entry.getValue(), "net.farmingtales.ftboss.mixin." + entry.getKey(), target.name.replace('/', '.'))) return null;
            result.put(entry.getKey(), entry.getValue().getClassNode(0));
        }
        return result;
    }

    static synchronized boolean nativeComponentsCertified() {
        if (invalid || installationPlugin == null) return false;
        for (int slot = 0; slot < 3; slot++) {
            if (nativeComponentBaselines.get(slot) == null || nativeComponentOrders.get(slot) == null || nativeComponentOrders.get(slot) != NativeMapPutLateProofExtension.order() || nativeComponentLoaders[slot] != installationPlugin.getClass().getClassLoader() || nativeComponentThreads[slot] == null) return false;
            Map<String, ClassNode> sources = nativeComponentSourceNodes(slot); if (sources == null) return false;
            try { NativeComponentInstallationProof163.verify(nativeComponentTargets[slot], sources, nativeComponentBaselines.get(slot)); } catch (RuntimeException failure) { return false; }
        }
        return true;
    }


    private static ClassNode nativeItemStackTarget165;
    private static ClassNode nativeItemStackPristine165;
    private static Map<String, IMixinInfo> nativeItemStackSources165;
    private static final Map<String, IMixinConfig> nativeItemStackConfigs165 = new java.util.LinkedHashMap<>();
    private static final Map<String, IMixinConfigPlugin> nativeItemStackPlugins165 = new java.util.LinkedHashMap<>();
    private static List<IExtension> nativeItemStackOrder165;
    private static ClassLoader nativeItemStackLoader165;
    private static Thread nativeItemStackThread165;
    private static String nativeItemStackFailure165;
    private static IMixinInfo nativeChildCountSource246;
    private static IMixinConfig nativeChildCountConfig246;

    private static ClassNode nativeChildCountNode246() {
        IMixinInfo info = nativeChildCountSource246;
        if (info == null || info.getConfig() != nativeChildCountConfig246 || !nativeItemStackSource165(info, "NativeHandlerChildCountMixin244", false)) return null;
        try (var stream = OwnerFieldCoverageRegistry.class.getResourceAsStream("/net/farmingtales/ftboss/mixin/NativeHandlerChildCountMixin244.class")) {
            if (stream == null) return null;
            ClassNode source = new ClassNode(); new org.objectweb.asm.ClassReader(stream).accept(source, 0);
            ClassNode observed = info.getClassNode(0);
            if (!net.farmingtales.ftboss.boss.ai.NativeBossLivingComposerSupportV2.fingerprint(source).equals(net.farmingtales.ftboss.boss.ai.NativeBossLivingComposerSupportV2.fingerprint(observed))) return null;
            return observed;
        } catch (java.io.IOException failure) { return null; }
    }

    private static boolean nativeItemStackSource165(IMixinInfo info, String family, boolean retained) {
        try {
            ClassLoader loader = IMixinInfo.class.getClassLoader();
            if (info == null || info.getClass() != Class.forName("org.spongepowered.asm.mixin.transformer.MixinInfo", false, loader)
                || !info.getClassName().equals(NativeItemStackInstallationProof165.className(family)) || info.getPriority() != (family.equals("NativeReadCutItemStackMixin") ? 1 : 1000)
                || info.getConfig() == null || info.getConfig().getClass() != Class.forName("org.spongepowered.asm.mixin.transformer.MixinConfig", false, loader)
                || !info.getConfig().isRequired() || !fieldTarget(info, "net.minecraft.world.item.ItemStack")) return false;
            IMixinConfig config = info.getConfig(); IMixinConfigPlugin plugin = config.getPlugin();
            boolean identity;
            if (family.equals("GeckoItemStackMixin")) identity = config.getName().equals("geckolib.mixins.json") && config.getMixinPackage().equals("software.bernie.geckolib.mixin.") && plugin == null;
            else if (family.equals("NativeWearTokenMixin") || family.equals("NativeAppliedDamageEntryMixin")) identity = config.getName().equals("ftboss.weapon-lifecycle.mixins.json") && config.getMixinPackage().equals("net.farmingtales.ftboss.mixin.") && plugin != null && plugin.getClass() == NativeWeaponLifecycleMixinPlugin.class;
            else if (family.equals("NativeReadCutItemStackMixin")) identity = config.getName().equals("ftboss.weapon-observation.mixins.json") && config.getMixinPackage().equals("net.farmingtales.ftboss.mixin.") && plugin != null && plugin.getClass() == NativeWeaponObservationMixinPlugin.class;
            else if (family.equals("FoodWeaponItemUseMixin")) identity = config.getName().equals("ftboss.native-bow320.mixins.json") && config.getMixinPackage().equals("net.farmingtales.ftboss.mixin.") && plugin != null && plugin.getClass() == net.farmingtales.ftboss.mixin.NativeOptionalMixinPlugin.class;
            else if (family.equals("NativeHandlerChildCountMixin244")) identity = config.getName().equals("ftboss.native-item-handler232.mixins.json") && config.getMixinPackage().equals("net.farmingtales.ftboss.mixin.") && plugin == null && info.getClass().getClassLoader() == loader;
            else identity = registeredConfiguration(info);
            if (!identity || plugin != null && plugin.getClass().getClassLoader() != installationPlugin.getClass().getClassLoader()) return false;
            return !retained || nativeItemStackConfigs165.get(family) == config && nativeItemStackPlugins165.containsKey(family) && nativeItemStackPlugins165.get(family) == plugin;
        } catch (ClassNotFoundException failure) { return false; }
    }

    private static Map<String, ClassNode> nativeItemStackSourceNodes165() {
        if (nativeItemStackSources165 == null) return null;
        Map<String, ClassNode> result = new java.util.LinkedHashMap<>();
        for (String family : NativeItemStackInstallationProof165.requiredSources()) {
            IMixinInfo info = nativeItemStackSources165.get(family); if (!nativeItemStackSource165(info, family, true)) return null; result.put(family, info.getClassNode(0));
        }
        IMixinInfo observation = nativeItemStackSources165.get("NativeReadCutItemStackMixin");
        if (!nativeItemStackSource165(observation, "NativeReadCutItemStackMixin", true)) return null;
        try (var stream = OwnerFieldCoverageRegistry.class.getResourceAsStream("/net/farmingtales/ftboss/mixin/NativeReadCutItemStackMixin.class")) {
            if (stream == null) return null; ClassNode original = new ClassNode(); new org.objectweb.asm.ClassReader(stream).accept(original, 0);
            if (!net.farmingtales.ftboss.boss.ai.NativeBossLivingComposerSupportV2.fingerprint(original).equals(net.farmingtales.ftboss.boss.ai.NativeBossLivingComposerSupportV2.fingerprint(observation.getClassNode(0)))) return null;
        } catch (java.io.IOException failure) { return null; }
        ClassNode child = nativeChildCountNode246(); if (child == null) return null;
        result.put("NativeHandlerChildCountMixin244", child);
        return result;
    }

    static synchronized void nativeItemStackFinished165(ITargetClassContext context) {
        if (context == null || !"net/minecraft/world/item/ItemStack".equals(context.getClassNode().name) || !NativeMapPutLateProofExtension.finishing(context)) throw new IllegalStateException("Untrusted real ItemStack final proof context");
        nativeChildCountSource246 = null; nativeChildCountConfig246 = null;
        nativeItemStackTarget165 = null; nativeItemStackPristine165 = null; nativeItemStackSources165 = null; nativeItemStackOrder165 = null; nativeItemStackLoader165 = null; nativeItemStackThread165 = null; nativeItemStackConfigs165.clear(); nativeItemStackPlugins165.clear(); nativeItemStackFailure165 = null;
        ClassNode pristine = NativeMapPutLateProofExtension.nativeItemStackParents165(context.getClassNode()); List<IExtension> order = NativeMapPutLateProofExtension.order();
        if (invalid || installationPlugin == null || pristine == null || order == null) { nativeItemStackFailure165 = "Missing real original context or owner installation"; return; }
        try {
            Map<String, IMixinInfo> observed = new java.util.LinkedHashMap<>(); List<String> required = new ArrayList<>(NativeItemStackInstallationProof165.requiredSources()); required.add("NativeReadCutItemStackMixin");
            for (var pair : com.llamalad7.mixinextras.utils.MixinInternals.getMixinsFor(context)) for (String family : required) if (pair.getLeft().getClassName().equals(NativeItemStackInstallationProof165.className(family))) {
                if (observed.put(family, pair.getLeft()) != null || !nativeItemStackSource165(pair.getLeft(), family, false)) throw new IllegalStateException("Changed or duplicate actual required source/config/plugin " + family);
                nativeItemStackConfigs165.put(family, pair.getLeft().getConfig()); nativeItemStackPlugins165.put(family, pair.getLeft().getConfig().getPlugin());
            }
            if (!observed.keySet().equals(Set.copyOf(required))) throw new IllegalStateException("Missing actual context source identities " + observed.keySet());
            for (var pair : com.llamalad7.mixinextras.utils.MixinInternals.getMixinsFor(context)) if (pair.getLeft().getClassName().equals(NativeItemStackInstallationProof165.className("NativeHandlerChildCountMixin244"))) {
                if (nativeChildCountSource246 != null || !nativeItemStackSource165(pair.getLeft(), "NativeHandlerChildCountMixin244", false)) throw new IllegalStateException("Missing/changed/duplicate native child count source/config");
                nativeChildCountSource246 = pair.getLeft(); nativeChildCountConfig246 = pair.getLeft().getConfig();
            }
            if (nativeChildCountSource246 == null) throw new IllegalStateException("Missing genuine count-write source");
            nativeItemStackSources165 = Map.copyOf(observed); Map<String, ClassNode> sources = nativeItemStackSourceNodes165(); if (sources == null) throw new IllegalStateException("Changed actual source identity nodes");
            NativeItemStackInstallationProof165.verify(context.getClassNode(), sources, pristine);
            nativeItemStackTarget165 = context.getClassNode(); nativeItemStackPristine165 = NativeItemStackInstallationProof165.snapshot(pristine); nativeItemStackOrder165 = order; nativeItemStackLoader165 = installationPlugin.getClass().getClassLoader(); nativeItemStackThread165 = Thread.currentThread();
        } catch (RuntimeException failure) { nativeItemStackFailure165 = failure.toString() + ":" + String.valueOf(failure.getCause()); nativeItemStackTarget165 = null; nativeItemStackPristine165 = null; }
    }

    static synchronized boolean nativeItemStackCertified165() {
        if (invalid || installationPlugin == null || nativeItemStackTarget165 == null || nativeItemStackPristine165 == null || nativeItemStackOrder165 == null || nativeItemStackOrder165 != NativeMapPutLateProofExtension.order() || nativeItemStackLoader165 != installationPlugin.getClass().getClassLoader() || nativeItemStackThread165 == null) return false;
        Map<String, ClassNode> sources = nativeItemStackSourceNodes165(); if (sources == null) return false;
        try { NativeItemStackInstallationProof165.verify(nativeItemStackTarget165, sources, nativeItemStackPristine165); return true; } catch (RuntimeException failure) { return false; }
    }


    static synchronized boolean nativeOriginalRequirementCertified167(int index) {
        return !invalid && index >= 0 && index < REQUIRED.length && NativeOwnerRequirementProof167.complete(index, REQUIRED[index]);
    }

    private static boolean nativeTridentGenerationCertified168(int index) {
        if (index != 38 && index != 39) return false;
        try {
            return nativeOriginalRequirementCertified167(index);
        } catch (Throwable failure) {
            return false;
        }
    }

    static synchronized boolean nativeOriginalItemStackRequirementCertified169(int index) {
        return !invalid && index == 12 && NativeItemStackRequirementProof169.complete(REQUIRED[index]);
    }

    private static boolean nativeItemStackGenerationCertified169(int index) {
        if (index != 12) return false;
        try {
            return nativeOriginalItemStackRequirementCertified169(index);
        } catch (Throwable failure) {
            return false;
        }
    }

    static synchronized void nativeCapabilityFinished170(ITargetClassContext context) {
        if (invalid || installationPlugin == null || !NativeMapPutLateProofExtension.finishing(context)) throw new IllegalStateException("Untrusted native capability final proof context");
        NativeCapabilityCertificate170.finish(context, installationPlugin);
    }

    static synchronized boolean nativeCapabilityCertified170() {
        return !invalid && installationPlugin != null && NativeCapabilityCertificate170.complete(installationPlugin)
            && nativeItemStackCertified165() && nativeComponentsCertified();
    }
    static synchronized boolean nativeOriginalCapabilityRequirementCertified171(int index) {
        return !invalid && index == 16 && NativeCapabilityRequirementProof171.complete(REQUIRED[index]);
    }

    static synchronized boolean nativeOriginalCapabilityLifecycleRequirementCertified202(int index) {
        return !invalid && (index == 8 || index == 9 || index == 60)
            && NativeCapabilityLifecycleRequirement202.complete(index, REQUIRED[index]);
    }

    private static boolean nativeCapabilityLifecycleGenerationCertified202(int index) {
        if (index != 8 && index != 9 && index != 60) return false;
        try {
            return nativeOriginalCapabilityLifecycleRequirementCertified202(index);
        } catch (Throwable failure) {
            return false;
        }
    }

    private static boolean nativeCapabilityGenerationCertified171(int index) {
        if (index != 16) return false;
        try {
            return nativeOriginalCapabilityRequirementCertified171(index);
        } catch (Throwable failure) {
            return false;
        }
    }
    static synchronized boolean nativeOriginalStackFieldsRequirementCertified172(int index) {
        return !invalid && index == 30 && NativeStackFieldRequirementProof172.complete(REQUIRED[index]);
    }

    private static boolean nativeStackFieldsGenerationCertified172(int index) {
        if (index != 30) return false;
        try {
            return nativeOriginalStackFieldsRequirementCertified172(index);
        } catch (Throwable failure) {
            return false;
        }
    }
    static synchronized boolean nativeOriginalProviderFieldsRequirementCertified173(int index) {
        return !invalid && index == 28 && NativeProviderFieldRequirementProof173.complete(REQUIRED[index]);
    }

    static synchronized boolean nativeOriginalAssignmentRequirementCertified185(int index) {
        return !invalid && index == 63 && OwnerFieldInstallationProof.nativeAssignmentObservation185(REQUIRED[index])
            && nativeItemStackCertified165() && nativeComponentsCertified()
            && nativeOriginalStackFieldsRequirementCertified172(30);
    }

    private static boolean nativeAssignmentGenerationCertified185(int index) {
        if (index != 63) return false;
        try {
            return nativeOriginalAssignmentRequirementCertified185(index);
        } catch (Throwable failure) {
            return false;
        }
    }

    private static boolean nativeProviderFieldsGenerationCertified173(int index) {
        if (index != 28) return false;
        try {
            return nativeOriginalProviderFieldsRequirementCertified173(index);
        } catch (Throwable failure) {
            return false;
        }
    }
    static synchronized boolean nativeOriginalProviderRawExposureRequirementCertified189(int index) {
        return !invalid && index == 55 && NativeProviderFieldRequirementProof173.completeRawExposure189(REQUIRED[index]);
    }

    private static boolean nativeProviderRawExposureGenerationCertified189(int index) {
        if (index != 55) return false;
        try {
            return nativeOriginalProviderRawExposureRequirementCertified189(index);
        } catch (Throwable failure) {
            return false;
        }
    }

    static synchronized boolean nativeOriginalProviderRevocationRequirementCertified191(int index) {
        return !invalid && index == 46 && NativeProviderFieldRequirementProof173.completeRevocationNode191(REQUIRED[index]);
    }
    private static boolean nativeProviderRevocationGenerationCertified191(int index) {
        if (index != 46) return false;
        try {
            return nativeOriginalProviderRevocationRequirementCertified191(index);
        } catch (Throwable failure) {
            return false;
        }
    }

    static synchronized boolean nativeOriginalDispatcherRevocationRequirementCertified193(int index) {
        return !invalid && index == 47 && NativeProviderFieldRequirementProof173.completeDispatcherNode193(REQUIRED[index]);
    }
    private static boolean nativeDispatcherRevocationGenerationCertified193(int index) {
        if (index != 47) return false;
        try {
            return nativeOriginalDispatcherRevocationRequirementCertified193(index);
        } catch (Throwable failure) {
            return false;
        }
    }

}
