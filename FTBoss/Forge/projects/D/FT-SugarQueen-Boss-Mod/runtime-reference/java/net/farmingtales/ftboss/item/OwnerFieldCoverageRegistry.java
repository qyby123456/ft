/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.objectweb.asm.tree.ClassNode
 *  org.spongepowered.asm.mixin.extensibility.IMixinConfig
 *  org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
 *  org.spongepowered.asm.mixin.extensibility.IMixinInfo
 *  org.spongepowered.asm.mixin.transformer.ext.IExtension
 *  org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext
 */
package net.farmingtales.ftboss.item;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.farmingtales.ftboss.item.FoodWeaponExpectedTrace;
import net.farmingtales.ftboss.item.NativeMapPutLateProofExtension;
import net.farmingtales.ftboss.item.OwnerFieldInstallationProof;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

public final class OwnerFieldCoverageRegistry {
    private static final String[][] REQUIRED = new String[][]{{"net.farmingtales.ftboss.mixin.NativeExplicitTimerPutMixin", "net.farmingtales.ftboss.item.FoodWeaponEvolution", "ftboss$r2$timerPut", "ftboss$r2$timerPutSrg"}, {"net.farmingtales.ftboss.mixin.OwnerTimerLocalOriginMixin", "net.farmingtales.ftboss.item.FoodWeaponEvolution", "ftboss$ownerLocalDataCopy", "ftboss$ownerLocalBaselineCopy", "ftboss$ownerTargetReceiver", "ftboss$ownerTargetWrite"}, {"net.farmingtales.ftboss.mixin.OwnerTimerSemanticConsumerMixin", "net.farmingtales.ftboss.item.FoodWeaponHitPlan", "ftboss$ownerSemanticLeaf"}, {"net.farmingtales.ftboss.mixin.OwnerTimerRangedGuardMixin", "net.farmingtales.ftboss.item.FoodWeaponRanged", "ftboss$ownerMaterialGuard"}, {"net.farmingtales.ftboss.mixin.NativeExpectedActorFieldsMixin", "net.minecraft.world.entity.Entity", "ftboss$rawPersistentData"}, {"net.farmingtales.ftboss.mixin.OwnerFieldExpectedEntityMixin", "net.minecraft.world.entity.Entity", "ftboss$ownerFieldConstructed", "ftboss$ownerFieldPersistentExposed", "ftboss$ownerFieldRawLevel", "ftboss$ownerFieldDomain", "ftboss$ownerFieldInstallDomain", "ftboss$ownerFieldLoadQuoted", "ftboss$ownerFieldLoadAssigned"}, {"net.farmingtales.ftboss.mixin.OwnerFieldSourceAccessMixin", "net.minecraft.world.damagesource.DamageSource", "ftboss$ownerFieldDirectEntity"}, {"net.farmingtales.ftboss.mixin.OwnerFullRepairByteArrayTagMixin", "net.minecraft.nbt.ByteArrayTag", "ftboss$fullRepairVoidMutation", "ftboss$fullRepairReturnedMutation", "ftboss$fullRepairAlias"}, {"net.farmingtales.ftboss.mixin.OwnerFullRepairCapabilityDispatcherMixin", "net.minecraftforge.common.capabilities.CapabilityDispatcher", "ftboss$fullRepairVoidMutation", "ftboss$fullRepairAlias"}, {"net.farmingtales.ftboss.mixin.OwnerFullRepairCapabilityProviderMixin", "net.minecraftforge.common.capabilities.CapabilityProvider", "ftboss$fullRepairVoidMutation", "ftboss$fullRepairReturnedMutation", "ftboss$fullRepairAlias"}, {"net.farmingtales.ftboss.mixin.OwnerFullRepairCompoundTagMixin", "net.minecraft.nbt.CompoundTag", "ftboss$fullRepairVoidMutation", "ftboss$fullRepairReturnedMutation", "ftboss$fullRepairAlias"}, {"net.farmingtales.ftboss.mixin.OwnerFullRepairIntArrayTagMixin", "net.minecraft.nbt.IntArrayTag", "ftboss$fullRepairVoidMutation", "ftboss$fullRepairReturnedMutation", "ftboss$fullRepairAlias"}, {"net.farmingtales.ftboss.mixin.OwnerFullRepairItemStackMixin", "net.minecraft.world.item.ItemStack", "ftboss$fullRepairVoidMutation", "ftboss$fullRepairReturnedMutation", "ftboss$fullRepairAlias", "ftboss$fullRepairGetOrCreateTag", "ftboss$fullRepairGetOrCreateElement"}, {"net.farmingtales.ftboss.mixin.OwnerFullRepairListTagMixin", "net.minecraft.nbt.ListTag", "ftboss$fullRepairVoidMutation", "ftboss$fullRepairReturnedMutation", "ftboss$fullRepairAlias"}, {"net.farmingtales.ftboss.mixin.OwnerFullRepairLongArrayTagMixin", "net.minecraft.nbt.LongArrayTag", "ftboss$fullRepairVoidMutation", "ftboss$fullRepairReturnedMutation", "ftboss$fullRepairAlias"}, {"net.farmingtales.ftboss.mixin.OwnerNativeMaterialEntityIdentityMixin", "net.minecraft.world.entity.Entity", "ftboss$ownerMaterialEntityIdentity"}, {"net.farmingtales.ftboss.mixin.OwnerRepairCapabilityFieldsMixin", "net.minecraftforge.common.capabilities.CapabilityProvider", "ftboss$ownerRepairInitializedNoProviders"}, {"net.farmingtales.ftboss.mixin.OwnerRepairStackFieldsMixin", "net.minecraft.world.item.ItemStack", "ftboss$ownerRepairRawItem", "ftboss$ownerRepairRawCapNbtAbsent", "ftboss$ownerRepairWasRetired", "ftboss$ownerRepairRetire"}, {"net.farmingtales.ftboss.mixin.NativeExpectedByteArrayTagFieldsMixin", "net.minecraft.nbt.ByteArrayTag", "ftboss$rawBytes"}, {"net.farmingtales.ftboss.mixin.NativeExpectedByteTagFieldsMixin", "net.minecraft.nbt.ByteTag", "ftboss$rawByte"}, {"net.farmingtales.ftboss.mixin.NativeExpectedCompoundTagFieldsMixin", "net.minecraft.nbt.CompoundTag", "ftboss$rawCompoundFields"}, {"net.farmingtales.ftboss.mixin.NativeExpectedDoubleTagFieldsMixin", "net.minecraft.nbt.DoubleTag", "ftboss$rawDouble"}, {"net.farmingtales.ftboss.mixin.NativeExpectedFloatTagFieldsMixin", "net.minecraft.nbt.FloatTag", "ftboss$rawFloat"}, {"net.farmingtales.ftboss.mixin.NativeExpectedIntArrayTagFieldsMixin", "net.minecraft.nbt.IntArrayTag", "ftboss$rawInts"}, {"net.farmingtales.ftboss.mixin.NativeExpectedIntTagFieldsMixin", "net.minecraft.nbt.IntTag", "ftboss$rawInt"}, {"net.farmingtales.ftboss.mixin.NativeExpectedListTagFieldsMixin", "net.minecraft.nbt.ListTag", "ftboss$rawListFields", "ftboss$rawListKind"}, {"net.farmingtales.ftboss.mixin.NativeExpectedLongArrayTagFieldsMixin", "net.minecraft.nbt.LongArrayTag", "ftboss$rawLongs"}, {"net.farmingtales.ftboss.mixin.NativeExpectedLongTagFieldsMixin", "net.minecraft.nbt.LongTag", "ftboss$rawLong"}, {"net.farmingtales.ftboss.mixin.NativeExpectedProviderFieldsMixin", "net.minecraftforge.common.capabilities.CapabilityProvider", "ftboss$rawDispatcher", "ftboss$rawCapsValid", "ftboss$rawCapsLazy", "ftboss$rawCapsInitialized", "ftboss$rawCapsParentSupplier", "ftboss$rawCapsLazyData"}, {"net.farmingtales.ftboss.mixin.NativeExpectedShortTagFieldsMixin", "net.minecraft.nbt.ShortTag", "ftboss$rawShort"}, {"net.farmingtales.ftboss.mixin.NativeExpectedStackFieldsMixin", "net.minecraft.world.item.ItemStack", "ftboss$rawStackTag", "ftboss$rawStackCaps", "ftboss$rawStackCount", "ftboss$rawStackItem"}, {"net.farmingtales.ftboss.mixin.NativeExpectedStringTagFieldsMixin", "net.minecraft.nbt.StringTag", "ftboss$rawText"}, {"net.farmingtales.ftboss.mixin.OwnerOriginalPublicationFieldsMixin", "net.farmingtales.ftboss.item.FoodWeaponFinalDamage$Publication", "ftboss$ownerOriginalProducers"}, {"net.farmingtales.ftboss.mixin.OwnerEvolutionPlanFieldsMixin", "net.farmingtales.ftboss.item.FoodWeaponFinalDamage$EvolutionProducer", "ftboss$ownerOriginalPlan"}, {"net.farmingtales.ftboss.mixin.OwnerOriginalPlanFieldsMixin", "net.farmingtales.ftboss.item.FoodWeaponHitPlan", "ftboss$ownerOriginalLeaves"}, {"net.farmingtales.ftboss.mixin.OwnerTimerQuoteMixin", "net.farmingtales.ftboss.item.FoodWeaponHitPlan$TimerSaveLeaf", "ftboss$ownerTimerQuoted", "ftboss$ownerTimerQuote"}, {"net.farmingtales.ftboss.mixin.OwnerOriginalPublicationAdmissionMixin", "net.farmingtales.ftboss.item.FoodWeaponFinalDamage", "ftboss$ownerOriginalPublicationAssigned"}, {"net.farmingtales.ftboss.mixin.OwnerOriginalRepairContinuationMixin", "net.farmingtales.ftboss.item.FoodWeaponHitPlan$RepairLeaf", "ftboss$ownerRepairGetter", "ftboss$ownerRepairSetter"}, {"net.farmingtales.ftboss.mixin.OwnerTridentOriginalCopyMixin", "net.minecraft.world.entity.projectile.ThrownTrident", "ftboss$ownerTridentOriginalCopy", "ftboss$ownerTridentLineage", "ftboss$ownerInstallTridentLineage"}, {"net.farmingtales.ftboss.mixin.NativeTridentFieldCarrierMixin", "net.minecraft.world.entity.projectile.ThrownTrident", "ftboss$originalTridentStack"}, {"net.farmingtales.ftboss.mixin.OwnerSnapshotLeaseCopyMixin", "net.minecraft.nbt.CompoundTag", "ftboss$ownerSnapshotCopy"}, {"net.farmingtales.ftboss.mixin.OwnerRevocationNodeMixin", "net.minecraft.nbt.CompoundTag", "ftboss$ownerRevocationNode", "ftboss$ownerInstallRevocationNode"}, {"net.farmingtales.ftboss.mixin.OwnerRevocationNodeMixin", "net.minecraft.nbt.ListTag", "ftboss$ownerRevocationNode", "ftboss$ownerInstallRevocationNode"}, {"net.farmingtales.ftboss.mixin.OwnerRevocationNodeMixin", "net.minecraft.nbt.ByteArrayTag", "ftboss$ownerRevocationNode", "ftboss$ownerInstallRevocationNode"}, {"net.farmingtales.ftboss.mixin.OwnerRevocationNodeMixin", "net.minecraft.nbt.IntArrayTag", "ftboss$ownerRevocationNode", "ftboss$ownerInstallRevocationNode"}, {"net.farmingtales.ftboss.mixin.OwnerRevocationNodeMixin", "net.minecraft.nbt.LongArrayTag", "ftboss$ownerRevocationNode", "ftboss$ownerInstallRevocationNode"}, {"net.farmingtales.ftboss.mixin.OwnerRevocationNodeMixin", "net.minecraftforge.common.capabilities.CapabilityProvider", "ftboss$ownerRevocationNode", "ftboss$ownerInstallRevocationNode"}, {"net.farmingtales.ftboss.mixin.OwnerRevocationNodeMixin", "net.minecraftforge.common.capabilities.CapabilityDispatcher", "ftboss$ownerRevocationNode", "ftboss$ownerInstallRevocationNode"}, {"net.farmingtales.ftboss.mixin.OwnerRawFieldExposureMixin", "net.minecraft.nbt.CompoundTag", "ftboss$ownerRawExposed"}, {"net.farmingtales.ftboss.mixin.OwnerRawFieldExposureMixin", "net.minecraft.nbt.ListTag", "ftboss$ownerRawExposed"}, {"net.farmingtales.ftboss.mixin.OwnerRawFieldExposureMixin", "net.minecraft.nbt.ByteArrayTag", "ftboss$ownerRawExposed"}, {"net.farmingtales.ftboss.mixin.OwnerRawFieldExposureMixin", "net.minecraft.nbt.IntArrayTag", "ftboss$ownerRawExposed"}, {"net.farmingtales.ftboss.mixin.OwnerRawFieldExposureMixin", "net.minecraft.nbt.LongArrayTag", "ftboss$ownerRawExposed"}, {"net.farmingtales.ftboss.mixin.OwnerRawFieldExposureMixin", "net.minecraft.world.item.ItemStack", "ftboss$ownerRawExposed"}, {"net.farmingtales.ftboss.mixin.OwnerRawFieldExposureMixin", "net.minecraft.world.entity.Entity", "ftboss$ownerRawExposed"}, {"net.farmingtales.ftboss.mixin.OwnerRawFieldExposureMixin", "net.minecraftforge.common.capabilities.CapabilityProvider", "ftboss$ownerRawExposed"}, {"net.farmingtales.ftboss.mixin.NativeExpectedActualStepMixin", "net.farmingtales.ftboss.item.FoodWeaponHitPlan", "ftboss$actualReturnedTimerStep"}, {"net.farmingtales.ftboss.mixin.NativeExpectedAliasReturnMixin", "net.minecraft.nbt.CompoundTag", "ftboss$originalAliasReturned"}, {"net.farmingtales.ftboss.mixin.NativeExpectedCaptureBoundaryMixin", "net.farmingtales.ftboss.item.FoodWeaponUseBridge", "ftboss$originalCapture"}, {"net.farmingtales.ftboss.mixin.NativeExpectedMapWriteMixin", "net.minecraft.nbt.CompoundTag", "ftboss$originalFieldMapPut"}, {"net.farmingtales.ftboss.mixin.NativeExpectedProviderObservationMixin", "net.minecraftforge.common.capabilities.CapabilityProvider", "ftboss$originalProviderBefore", "ftboss$originalProviderAfter"}, {"net.farmingtales.ftboss.mixin.NativeExpectedRangedScopeMixin", "net.farmingtales.ftboss.item.FoodWeaponHitPlan$TimerSaveLeaf", "ftboss$originalRangedScope"}, {"net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin", "net.farmingtales.ftboss.item.FoodWeaponRanged", "ftboss$originalRangedPersistentData", "ftboss$originalRangedCompound", "ftboss$originalRangedCompoundSrg", "ftboss$originalRangedLong", "ftboss$originalRangedLongSrg", "ftboss$originalRangedRoot", "ftboss$originalRangedRootSrg"}, {"net.farmingtales.ftboss.mixin.NativeExpectedTagAssignmentMixin", "net.minecraft.world.item.ItemStack", "ftboss$originalTagAssigned"}, {"net.farmingtales.ftboss.mixin.NativeExpectedTimerCopyMixin", "net.farmingtales.ftboss.item.FoodWeaponEvolution", "ftboss$actualOriginalCopy", "ftboss$actualOriginalCopySrg"}, {"net.farmingtales.ftboss.mixin.NativeExpectedTimerReceiverMixin", "net.farmingtales.ftboss.item.FoodWeaponEvolution", "ftboss$actualOriginalReceiver"}, {"net.farmingtales.ftboss.mixin.NativeExpectedTimerScopeMixin", "net.farmingtales.ftboss.item.FoodWeaponHitPlan$TimerSaveLeaf", "ftboss$originalTimerScope"}, {"net.farmingtales.ftboss.mixin.OwnerNativeWeaponInputMixin", "net.minecraft.world.item.ItemStack", "ftboss$ownerNativeWeaponInput", "ftboss$ownerInstallNativeWeaponInput", "ftboss$ownerNativeLoadedInput"}, {"net.farmingtales.ftboss.mixin.OwnerNativeLeafFieldsMixin", "net.farmingtales.ftboss.item.FoodWeaponHitPlan$TypedLeaf", "ftboss$ownerNativeAttempt"}, {"net.farmingtales.ftboss.mixin.OwnerNativePutQuoteMixin", "net.farmingtales.ftboss.item.FoodWeaponHitPlan$TagLeaf", "ftboss$ownerNativePutQuote", "ftboss$ownerNativeOriginalValue", "ftboss$ownerNativePutQuoted"}, {"net.farmingtales.ftboss.mixin.OwnerNativePutConsumerMixin", "net.farmingtales.ftboss.item.FoodWeaponHitPlan$TagLeaf", "ftboss$ownerNativeCopy", "ftboss$ownerNativePut"}, {"net.farmingtales.ftboss.mixin.OwnerNativePutQuoteMixin", "net.farmingtales.ftboss.item.FoodWeaponHitPlan$OutcomeTagLeaf", "ftboss$ownerNativePutQuote", "ftboss$ownerNativeOriginalValue", "ftboss$ownerNativePutQuoted"}, {"net.farmingtales.ftboss.mixin.OwnerNativePutConsumerMixin", "net.farmingtales.ftboss.item.FoodWeaponHitPlan$OutcomeTagLeaf", "ftboss$ownerNativeCopy", "ftboss$ownerNativePut"}, {"net.farmingtales.ftboss.mixin.OwnerInventoryTickContinuityMixin", "net.minecraft.world.item.ItemStack", "ftboss$ownerInventoryTickCallback"}};
    private static final boolean[] INSTALLED = new boolean[REQUIRED.length];
    private static final IMixinInfo[] OBSERVED = new IMixinInfo[REQUIRED.length];
    private static final ClassNode[] TARGETS = new ClassNode[REQUIRED.length];
    private static IMixinConfig installationConfig;
    private static IMixinConfigPlugin installationPlugin;
    private static final Object GENERATION;
    private static boolean invalid;
    private static final List<WeakReference<ITargetClassContext>> lateContexts;
    private static final Thread[] lateThreads;
    private static final ClassLoader[] lateLoaders;
    private static final byte[] lateStages;
    private static final List<List<IExtension>> lateOrders;
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
    private static final List<List<List<String>>> ownerParentBaselines;
    private static final ClassNode[] ownerParentTargets;
    private static final IMixinInfo[] ownerParentSources;
    private static final ClassLoader[] ownerParentLoaders;
    private static final List<List<IExtension>> ownerParentOrders;

    private OwnerFieldCoverageRegistry() {
    }

    private static Class<?> installationCaller(IMixinInfo iMixinInfo) {
        if (iMixinInfo == null) {
            return null;
        }
        return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> {
            List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass() != OwnerFieldCoverageRegistry.class).limit(3L).toList();
            if (list.size() != 3 || list.stream().anyMatch(stackFrame -> !stackFrame.getMethodName().equals("postApply")) || !list.get(0).getDescriptor().equals("(Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;Ljava/lang/String;Lorg/spongepowered/asm/mixin/extensibility/IMixinInfo;)V") || !list.get(1).getDescriptor().equals("(Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;Ljava/lang/String;Lorg/spongepowered/asm/mixin/transformer/MixinInfo;)V") || !list.get(2).getDescriptor().equals("(Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;)V")) {
                return null;
            }
            try {
                IMixinConfigPlugin iMixinConfigPlugin;
                ClassLoader classLoader = IMixinInfo.class.getClassLoader();
                Class<?> clazz = Class.forName("org.spongepowered.asm.mixin.transformer.MixinInfo", false, classLoader);
                Class<?> clazz2 = Class.forName("org.spongepowered.asm.mixin.transformer.PluginHandle", false, classLoader);
                Class<?> clazz3 = Class.forName("org.spongepowered.asm.mixin.transformer.MixinConfig", false, classLoader);
                if (iMixinInfo.getClass() != clazz || list.get(1).getDeclaringClass() != clazz2 || list.get(2).getDeclaringClass() != clazz) {
                    return null;
                }
                Class<?> clazz4 = list.get(0).getDeclaringClass();
                IMixinConfig iMixinConfig = iMixinInfo.getConfig();
                IMixinConfigPlugin iMixinConfigPlugin2 = iMixinConfigPlugin = iMixinConfig == null ? null : iMixinConfig.getPlugin();
                if (!(NativeMapPutLateProofExtension.registeredPlugin(clazz4) && iMixinConfig != null && iMixinConfig.getClass() == clazz3 && "ftboss.mixins.json".equals(iMixinConfig.getName()) && "net.farmingtales.ftboss.mixin.".equals(iMixinConfig.getMixinPackage()) && iMixinConfig.isRequired() && iMixinConfigPlugin != null && iMixinConfigPlugin.getClass() == clazz4 && (installationConfig == null || OwnerFieldCoverageRegistry.registeredConfiguration(iMixinInfo)))) {
                    return null;
                }
                return clazz4;
            }
            catch (ClassNotFoundException classNotFoundException) {
                return null;
            }
        });
    }

    private static boolean registeredConfiguration(IMixinInfo iMixinInfo) {
        return iMixinInfo != null && installationConfig != null && installationPlugin != null && iMixinInfo.getConfig() == installationConfig && installationConfig.getPlugin() == installationPlugin && NativeMapPutLateProofExtension.registeredPlugin(installationPlugin.getClass());
    }

    public static synchronized void applied(String string, ClassNode classNode, String string3, IMixinInfo iMixinInfo) {
        Class<?> clazz = OwnerFieldCoverageRegistry.installationCaller(iMixinInfo);
        if (clazz == null || string == null || string3 == null || !string3.equals(iMixinInfo.getClassName())) {
            return;
        }
        for (int i = 0; i < REQUIRED.length; ++i) {
            String[] stringArray = REQUIRED[i];
            if (!stringArray[0].equals(string3) || !stringArray[1].equals(string)) continue;
            if (classNode == null || !string.replace('.', '/').equals(classNode.name) || iMixinInfo.getTargetClasses().stream().noneMatch(string2 -> string.equals(string2.replace('/', '.'))) || iMixinInfo.getConfig().getTargets().stream().noneMatch(string2 -> string.equals(string2.replace('/', '.'))) || OBSERVED[i] != null) {
                invalid = true;
                return;
            }
            if (installationConfig == null) {
                installationConfig = iMixinInfo.getConfig();
                installationPlugin = installationConfig.getPlugin();
            }
            if (!OwnerFieldCoverageRegistry.registeredConfiguration(iMixinInfo)) {
                invalid = true;
                return;
            }
            OwnerFieldCoverageRegistry.OBSERVED[i] = iMixinInfo;
            OwnerFieldCoverageRegistry.TARGETS[i] = classNode;
            int n = OwnerFieldInstallationProof.lateSlot(stringArray);
            if (n >= 0) {
                ITargetClassContext iTargetClassContext = NativeMapPutLateProofExtension.current(classNode);
                OwnerFieldCoverageRegistry.lateStages[n] = iTargetClassContext == null ? 3 : 1;
                lateContexts.set(n, iTargetClassContext == null ? null : new WeakReference<ITargetClassContext>(iTargetClassContext));
                OwnerFieldCoverageRegistry.lateThreads[n] = iTargetClassContext == null ? null : Thread.currentThread();
                OwnerFieldCoverageRegistry.lateLoaders[n] = iTargetClassContext == null ? null : clazz.getClassLoader();
                OwnerFieldCoverageRegistry.INSTALLED[i] = false;
                lateOrders.set(n, null);
                return;
            }
            if (OwnerFieldInstallationProof.canonicalAliasSite(stringArray)) {
                OwnerFieldCoverageRegistry.INSTALLED[i] = false;
                return;
            }
            try {
                OwnerFieldCoverageRegistry.INSTALLED[i] = OwnerFieldInstallationProof.complete(stringArray, classNode, iMixinInfo.getClassNode(0));
            }
            catch (Throwable throwable) {
                OwnerFieldCoverageRegistry.INSTALLED[i] = false;
            }
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * WARNING - void declaration
     */
    static synchronized void lateApplied(ITargetClassContext iTargetClassContext) {
        List<String> list;
        Object object;
        List<List<String>> list2;
        IMixinInfo iMixinInfo;
        int n;
        if (!NativeMapPutLateProofExtension.finishing(iTargetClassContext)) {
            return;
        }
        for (n = 0; n < REQUIRED.length; ++n) {
            int n2;
            block33: {
                n2 = OwnerFieldInstallationProof.lateSlot(REQUIRED[n]);
                if (n2 < 0 || TARGETS[n] != iTargetClassContext.getClassNode() || OBSERVED[n] == null || !OwnerFieldCoverageRegistry.registeredConfiguration(iMixinInfo = OBSERVED[n])) continue;
                Class<?> clazz = installationPlugin.getClass();
                list2 = lateContexts.get(n2);
                if (lateStages[n2] != 1 || list2 == null || ((Reference)((Object)list2)).get() != iTargetClassContext || lateThreads[n2] != Thread.currentThread() || lateLoaders[n2] != clazz.getClassLoader()) continue;
                OwnerFieldCoverageRegistry.lateStages[n2] = 3;
                try {
                    void list3;
                    List<List<String>> list4;
                    lateOrders.set(n2, NativeMapPutLateProofExtension.order());
                    object = REQUIRED[n][1];
                    if (iMixinInfo.getClass().getClassLoader() != IMixinInfo.class.getClassLoader() || !REQUIRED[n][0].equals(iMixinInfo.getClassName()) || !OwnerFieldCoverageRegistry.registeredConfiguration(iMixinInfo) || !iMixinInfo.getTargetClasses().stream().anyMatch(arg_0 -> OwnerFieldCoverageRegistry.lambda$lateApplied$5((String)object, arg_0)) || !iMixinInfo.getConfig().getTargets().stream().anyMatch(arg_0 -> OwnerFieldCoverageRegistry.lambda$lateApplied$6((String)object, arg_0))) break block33;
                    List<String> list5 = list = n2 == 1 ? NativeMapPutLateProofExtension.parent(TARGETS[n]) : null;
                    if (list != null) {
                        list = List.copyOf(list);
                    }
                    List<List<String>> list6 = list4 = n2 >= 2 ? NativeMapPutLateProofExtension.ownerParents(TARGETS[n]) : null;
                    if (list4 != null) {
                        ArrayList<List<String>> arrayList = new ArrayList<List<String>>();
                        for (List<String> list7 : list4) {
                            arrayList.add(List.copyOf(list7));
                        }
                        List list8 = List.copyOf(arrayList);
                    }
                    boolean bl = n2 >= 2 ? OwnerFieldInstallationProof.ownerWrapper(REQUIRED[n], TARGETS[n], iMixinInfo.getClassNode(0), (List<List<String>>)list3) : (OwnerFieldCoverageRegistry.INSTALLED[n] = n2 == 1 ? OwnerFieldInstallationProof.timerWrapper(REQUIRED[n], TARGETS[n], iMixinInfo.getClassNode(0), list) : OwnerFieldInstallationProof.complete(REQUIRED[n], TARGETS[n], iMixinInfo.getClassNode(0)));
                    if (INSTALLED[n] && lateOrders.get(n2) != null && lateOrders.get(n2) == NativeMapPutLateProofExtension.order()) {
                        if (n2 == 1) {
                            timerParentBaseline = list;
                            timerParentTarget = TARGETS[n];
                            timerParentSource = iMixinInfo;
                            timerParentLoader = lateLoaders[n2];
                            timerParentOrder = lateOrders.get(n2);
                        } else if (n2 >= 2) {
                            int n3 = n2 - 2;
                            ownerParentBaselines.set(n3, (List<List<String>>)list3);
                            OwnerFieldCoverageRegistry.ownerParentTargets[n3] = TARGETS[n];
                            OwnerFieldCoverageRegistry.ownerParentSources[n3] = iMixinInfo;
                            OwnerFieldCoverageRegistry.ownerParentLoaders[n3] = lateLoaders[n2];
                            ownerParentOrders.set(n3, lateOrders.get(n2));
                        }
                        OwnerFieldCoverageRegistry.lateStages[n2] = 2;
                        break block33;
                    }
                    OwnerFieldCoverageRegistry.INSTALLED[n] = false;
                }
                catch (Throwable throwable) {
                    try {
                        OwnerFieldCoverageRegistry.INSTALLED[n] = false;
                    }
                    catch (Throwable throwable2) {
                        if (n2 == 1 && lateStages[n2] != 2) {
                            timerParentBaseline = null;
                            timerParentTarget = null;
                            timerParentSource = null;
                            timerParentLoader = null;
                            timerParentOrder = null;
                        } else if (n2 >= 2 && lateStages[n2] != 2) {
                            int n4 = n2 - 2;
                            ownerParentBaselines.set(n4, null);
                            OwnerFieldCoverageRegistry.ownerParentTargets[n4] = null;
                            OwnerFieldCoverageRegistry.ownerParentSources[n4] = null;
                            OwnerFieldCoverageRegistry.ownerParentLoaders[n4] = null;
                            ownerParentOrders.set(n4, null);
                        }
                        lateContexts.set(n2, null);
                        OwnerFieldCoverageRegistry.lateThreads[n2] = null;
                        OwnerFieldCoverageRegistry.lateLoaders[n2] = null;
                        throw throwable2;
                    }
                    if (n2 == 1 && lateStages[n2] != 2) {
                        timerParentBaseline = null;
                        timerParentTarget = null;
                        timerParentSource = null;
                        timerParentLoader = null;
                        timerParentOrder = null;
                    } else if (n2 >= 2 && lateStages[n2] != 2) {
                        int n5 = n2 - 2;
                        ownerParentBaselines.set(n5, null);
                        OwnerFieldCoverageRegistry.ownerParentTargets[n5] = null;
                        OwnerFieldCoverageRegistry.ownerParentSources[n5] = null;
                        OwnerFieldCoverageRegistry.ownerParentLoaders[n5] = null;
                        ownerParentOrders.set(n5, null);
                    }
                    lateContexts.set(n2, null);
                    OwnerFieldCoverageRegistry.lateThreads[n2] = null;
                    OwnerFieldCoverageRegistry.lateLoaders[n2] = null;
                    continue;
                }
            }
            if (n2 == 1 && lateStages[n2] != 2) {
                timerParentBaseline = null;
                timerParentTarget = null;
                timerParentSource = null;
                timerParentLoader = null;
                timerParentOrder = null;
            } else if (n2 >= 2 && lateStages[n2] != 2) {
                int n6 = n2 - 2;
                ownerParentBaselines.set(n6, null);
                OwnerFieldCoverageRegistry.ownerParentTargets[n6] = null;
                OwnerFieldCoverageRegistry.ownerParentSources[n6] = null;
                OwnerFieldCoverageRegistry.ownerParentLoaders[n6] = null;
                ownerParentOrders.set(n6, null);
            }
            lateContexts.set(n2, null);
            OwnerFieldCoverageRegistry.lateThreads[n2] = null;
            OwnerFieldCoverageRegistry.lateLoaders[n2] = null;
            continue;
        }
        for (n = 0; n < REQUIRED.length; ++n) {
            String[] stringArray = REQUIRED[n];
            if (!OwnerFieldInstallationProof.canonicalAliasSite(stringArray) || TARGETS[n] != iTargetClassContext.getClassNode() || OBSERVED[n] == null) continue;
            iMixinInfo = OBSERVED[n];
            boolean bl = stringArray[0].equals("net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin");
            OwnerFieldCoverageRegistry.INSTALLED[n] = false;
            try {
                if (!OwnerFieldCoverageRegistry.registeredConfiguration(iMixinInfo) || iMixinInfo.getClass().getClassLoader() != IMixinInfo.class.getClassLoader() || !stringArray[0].equals(iMixinInfo.getClassName()) || !stringArray[1].equals(OwnerFieldCoverageRegistry.TARGETS[n].name.replace('/', '.')) || iMixinInfo.getTargetClasses().stream().noneMatch(string -> stringArray[1].equals(string.replace('/', '.'))) || iMixinInfo.getConfig().getTargets().stream().noneMatch(string -> stringArray[1].equals(string.replace('/', '.')))) continue;
                if (bl) {
                    list2 = NativeMapPutLateProofExtension.ownerParents(TARGETS[n]);
                    if (list2 == null) continue;
                    object = new ArrayList();
                    for (List list9 : list2) {
                        object.add(List.copyOf(list9));
                    }
                    list2 = List.copyOf(object);
                    list = NativeMapPutLateProofExtension.order();
                    boolean bl2 = OwnerFieldCoverageRegistry.INSTALLED[n] = list != null && OwnerFieldInstallationProof.canonicalRangedWrapper(stringArray, TARGETS[n], iMixinInfo.getClassNode(0), list2);
                    if (!INSTALLED[n]) continue;
                    rangedParentBaseline = list2;
                    rangedParentTarget = TARGETS[n];
                    rangedParentSource = iMixinInfo;
                    rangedParentLoader = installationPlugin.getClass().getClassLoader();
                    rangedParentOrder = list;
                    continue;
                }
                OwnerFieldCoverageRegistry.INSTALLED[n] = OwnerFieldInstallationProof.complete(stringArray, TARGETS[n], iMixinInfo.getClassNode(0));
                continue;
            }
            catch (Throwable throwable) {
                OwnerFieldCoverageRegistry.INSTALLED[n] = false;
                continue;
            }
            finally {
                if (bl && !INSTALLED[n]) {
                    rangedParentBaseline = null;
                    rangedParentTarget = null;
                    rangedParentSource = null;
                    rangedParentLoader = null;
                    rangedParentOrder = null;
                }
            }
        }
    }

    static synchronized List<String> timerParent(ClassNode classNode, ClassNode classNode2) {
        if (invalid || classNode == null || classNode2 == null || lateStages[1] != 2 || timerParentBaseline == null || timerParentTarget != classNode || timerParentSource == null || !OwnerFieldCoverageRegistry.registeredConfiguration(timerParentSource) || timerParentLoader != installationPlugin.getClass().getClassLoader() || timerParentOrder == null || timerParentOrder != lateOrders.get(1) || timerParentOrder != NativeMapPutLateProofExtension.order()) {
            return null;
        }
        for (int i = 0; i < REQUIRED.length; ++i) {
            if (!OwnerFieldInstallationProof.lateTimerSite(REQUIRED[i])) continue;
            String string = REQUIRED[i][1];
            if (!(INSTALLED[i] && TARGETS[i] == classNode && OBSERVED[i] == timerParentSource && REQUIRED[i][0].equals(classNode2.name.replace('/', '.')) && REQUIRED[i][1].equals(classNode.name.replace('/', '.')) && REQUIRED[i][0].equals(timerParentSource.getClassName()) && timerParentSource.getClass().getClassLoader() == IMixinInfo.class.getClassLoader() && !timerParentSource.getTargetClasses().stream().noneMatch(string2 -> string.equals(string2.replace('/', '.'))))) {
                return null;
            }
            return timerParentBaseline;
        }
        return null;
    }

    static synchronized List<List<String>> ownerParents(String[] stringArray, ClassNode classNode, ClassNode classNode2) {
        int n = OwnerFieldInstallationProof.ownerSlot(stringArray);
        if (invalid || n < 2 || classNode == null || classNode2 == null || lateStages[n] != 2) {
            return null;
        }
        int n2 = n - 2;
        IMixinInfo iMixinInfo = ownerParentSources[n2];
        List<IExtension> list = ownerParentOrders.get(n2);
        String string = stringArray[1];
        if (!(ownerParentBaselines.get(n2) != null && ownerParentTargets[n2] == classNode && iMixinInfo != null && OwnerFieldCoverageRegistry.registeredConfiguration(iMixinInfo) && ownerParentLoaders[n2] == installationPlugin.getClass().getClassLoader() && list != null && list == lateOrders.get(n) && list == NativeMapPutLateProofExtension.order() && stringArray[0].equals(classNode2.name.replace('/', '.')) && string.equals(classNode.name.replace('/', '.')) && stringArray[0].equals(iMixinInfo.getClassName()) && iMixinInfo.getClass().getClassLoader() == IMixinInfo.class.getClassLoader() && !iMixinInfo.getTargetClasses().stream().noneMatch(string2 -> string.equals(string2.replace('/', '.'))))) {
            return null;
        }
        for (int i = 0; i < REQUIRED.length; ++i) {
            if (REQUIRED[i] != stringArray) continue;
            return INSTALLED[i] && TARGETS[i] == classNode && OBSERVED[i] == iMixinInfo ? ownerParentBaselines.get(n2) : null;
        }
        return null;
    }

    static synchronized List<List<String>> rangedParents(String[] stringArray, ClassNode classNode, ClassNode classNode2) {
        if (!(!invalid && OwnerFieldInstallationProof.canonicalAliasSite(stringArray) && stringArray[0].equals("net.farmingtales.ftboss.mixin.NativeExpectedRangedSitesMixin") && classNode != null && classNode2 != null && rangedParentBaseline != null && rangedParentTarget == classNode && rangedParentSource != null && OwnerFieldCoverageRegistry.registeredConfiguration(rangedParentSource) && rangedParentLoader == installationPlugin.getClass().getClassLoader() && rangedParentOrder != null && rangedParentOrder == NativeMapPutLateProofExtension.order() && stringArray[0].equals(classNode2.name.replace('/', '.')) && stringArray[1].equals(classNode.name.replace('/', '.')) && stringArray[0].equals(rangedParentSource.getClassName()) && rangedParentSource.getClass().getClassLoader() == IMixinInfo.class.getClassLoader() && !rangedParentSource.getTargetClasses().stream().noneMatch(string -> stringArray[1].equals(string.replace('/', '.'))))) {
            return null;
        }
        for (int i = 0; i < REQUIRED.length; ++i) {
            if (REQUIRED[i] != stringArray) continue;
            return INSTALLED[i] && TARGETS[i] == classNode && OBSERVED[i] == rangedParentSource ? rangedParentBaseline : null;
        }
        return null;
    }

    static synchronized ClassNode timerSource(String string, ClassNode classNode) {
        for (int i = 0; i < REQUIRED.length; ++i) {
            if (!REQUIRED[i][0].equals(string) || TARGETS[i] != classNode || OBSERVED[i] == null) continue;
            return OBSERVED[i].getClassNode(0);
        }
        return null;
    }

    static synchronized boolean pending(FoodWeaponExpectedTrace.Plan plan) {
        if (invalid || plan == null) {
            return false;
        }
        for (boolean bl : INSTALLED) {
            if (bl) continue;
            return true;
        }
        return false;
    }

    static synchronized Object generation(FoodWeaponExpectedTrace.Plan plan) {
        int n;
        if (invalid || plan == null) {
            return null;
        }
        for (n = 0; n < lateStages.length; ++n) {
            if (lateStages[n] == 2 && (lateOrders.get(n) == null || lateOrders.get(n) != NativeMapPutLateProofExtension.order())) {
                return null;
            }
            if (lateStages[n] != 1 || lateContexts.get(n) != null && lateContexts.get(n).get() != null) continue;
            OwnerFieldCoverageRegistry.lateStages[n] = 3;
            lateContexts.set(n, null);
            OwnerFieldCoverageRegistry.lateThreads[n] = null;
            OwnerFieldCoverageRegistry.lateLoaders[n] = null;
        }
        for (n = 0; n < INSTALLED.length; ++n) {
            int n2 = OwnerFieldInstallationProof.lateSlot(REQUIRED[n]);
            if (n2 >= 0 && lateStages[n2] != 2) {
                return null;
            }
            if (!INSTALLED[n] || OBSERVED[n] == null || TARGETS[n] == null || !OwnerFieldCoverageRegistry.registeredConfiguration(OBSERVED[n])) {
                return null;
            }
            try {
                if (OwnerFieldInstallationProof.complete(REQUIRED[n], TARGETS[n], OBSERVED[n].getClassNode(0))) continue;
                return null;
            }
            catch (Throwable throwable) {
                return null;
            }
        }
        return GENERATION;
    }

    static synchronized boolean current(Object object, FoodWeaponExpectedTrace.Plan plan) {
        return object != null && object == OwnerFieldCoverageRegistry.generation(plan);
    }

    private static /* synthetic */ boolean lambda$lateApplied$6(String string, String string2) {
        return string.equals(string2.replace('/', '.'));
    }

    private static /* synthetic */ boolean lambda$lateApplied$5(String string, String string2) {
        return string.equals(string2.replace('/', '.'));
    }

    static {
        GENERATION = new Object();
        lateContexts = new ArrayList<Object>(Collections.nCopies(4, null));
        lateThreads = new Thread[4];
        lateLoaders = new ClassLoader[4];
        lateStages = new byte[4];
        lateOrders = new ArrayList<Object>(Collections.nCopies(4, null));
        ownerParentBaselines = new ArrayList<Object>(Collections.nCopies(2, null));
        ownerParentTargets = new ClassNode[2];
        ownerParentSources = new IMixinInfo[2];
        ownerParentLoaders = new ClassLoader[2];
        ownerParentOrders = new ArrayList<Object>(Collections.nCopies(2, null));
    }
}

