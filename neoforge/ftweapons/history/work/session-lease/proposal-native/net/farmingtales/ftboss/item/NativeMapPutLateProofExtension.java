package net.farmingtales.ftboss.item;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import com.llamalad7.mixinextras.injector.LateInjectionApplicatorExtension;
import com.llamalad7.mixinextras.utils.MixinInternals;
import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;
import org.spongepowered.asm.mixin.transformer.ext.extensions.ExtensionCheckClass;

public final class NativeMapPutLateProofExtension implements IExtension {
    private static final NativeMapPutLateProofExtension INSTANCE = new NativeMapPutLateProofExtension();
    private static final ThreadLocal<Map<ClassNode, WeakReference<ITargetClassContext>>> ACTIVE = ThreadLocal.withInitial(WeakHashMap::new);
    private static final ThreadLocal<Map<ClassNode, List<IExtension>>> ORDERS = ThreadLocal.withInitial(WeakHashMap::new);
    private static final ThreadLocal<Map<ClassNode, List<String>>> PARENTS = ThreadLocal.withInitial(WeakHashMap::new);
    private static final ThreadLocal<Map<ClassNode, List<List<String>>>> OWNER_PARENTS = ThreadLocal.withInitial(WeakHashMap::new);
    private static final ThreadLocal<Map<ClassNode, Map<String, String>>> COMPONENT_PARENTS = ThreadLocal.withInitial(WeakHashMap::new);
    private static final ThreadLocal<Map<ClassNode, ClassNode>> ITEMSTACK_PARENTS165 = ThreadLocal.withInitial(WeakHashMap::new);
    private static volatile Extensions extensions;
    private static volatile Class<?> registrationPlugin;

    private NativeMapPutLateProofExtension() {
    }

    public static synchronized void register() {
        Class var0 = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
            .walk(
                var0x -> {
                    List var1x = var0x.filter(var0xx -> var0xx.getDeclaringClass() != NativeMapPutLateProofExtension.class).limit(3L).toList();
                    if (var1x.size() == 3
                        && ((StackFrame)var1x.get(0)).getMethodName().equals("onLoad")
                        && ((StackFrame)var1x.get(0)).getDescriptor().equals("(Ljava/lang/String;)V")
                        && ((StackFrame)var1x.get(1)).getMethodName().equals("onLoad")
                        && ((StackFrame)var1x.get(1)).getDescriptor().equals("(Ljava/lang/String;)V")
                        && ((StackFrame)var1x.get(2)).getMethodName().equals("onSelect")
                        && ((StackFrame)var1x.get(2)).getDescriptor().equals("()V")) {
                        try {
                            ClassLoader var2 = IMixinInfo.class.getClassLoader();
                            if (((StackFrame)var1x.get(1)).getDeclaringClass()
                                    == Class.forName("org.spongepowered.asm.mixin.transformer.PluginHandle", false, var2)
                                && ((StackFrame)var1x.get(2)).getDeclaringClass()
                                    == Class.forName("org.spongepowered.asm.mixin.transformer.MixinConfig", false, var2)) {
                                Class var3 = ((StackFrame)var1x.get(0)).getDeclaringClass();
                                return (var3.getName().equals("net.farmingtales.ftboss.mixin.NativeOptionalMixinPlugin")
                                        || var3 == NativeOwnerWearMixinPlugin.class)
                                        && IMixinConfigPlugin.class.isAssignableFrom(var3)
                                    ? var3
                                    : null;
                            } else {
                                return null;
                            }
                        } catch (ClassNotFoundException var4) {
                            return null;
                        }
                    } else {
                        return null;
                    }
                }
            );
        if (var0 != null && (registrationPlugin == null || registrationPlugin == var0)) {
            MixinExtrasBootstrap.init();
            Extensions var1 = MixinInternals.getExtensions();
            if (extensions == null) {
                MixinInternals.registerExtension(INSTANCE);
                extensions = var1;
                if (!ordered()) {
                    throw new IllegalStateException("Late proof extension is not active after MixinExtras");
                } else {
                    registrationPlugin = var0;
                }
            } else if (extensions != var1 || !var1.getExtensions().contains(INSTANCE)) {
                throw new IllegalStateException("Changed extension registry");
            }
        } else {
            throw new IllegalStateException("Untrusted late proof registration");
        }
    }

    static boolean registeredPlugin(Class<?> var0) {
        return var0 != null && var0 == registrationPlugin;
    }

    private static boolean ordered() {
        if (extensions != null && extensions == MixinInternals.getExtensions()) {
            List var0 = extensions.getActiveExtensions();
            int var1 = -1;
            int var2 = -1;

            for (int var3 = 0; var3 < var0.size(); var3++) {
                IExtension var4 = (IExtension)var0.get(var3);
                if (var4 == INSTANCE) {
                    if (var1 >= 0) {
                        return false;
                    }

                    var1 = var3;
                }

                if (var4.getClass() == LateInjectionApplicatorExtension.class) {
                    if (var2 >= 0) {
                        return false;
                    }

                    var2 = var3;
                }
            }

            if (var1 >= 0 && var2 >= 0 && var2 < var1) {
                for (int var5 = var1 + 1; var5 < var0.size(); var5++) {
                    if (((IExtension)var0.get(var5)).getClass() != ExtensionCheckClass.class) {
                        return false;
                    }
                }

                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static boolean callback(ITargetClassContext var0, String var1) {
        if (var0 != null && ordered()) {
            try {
                Class var2 = Class.forName("org.spongepowered.asm.mixin.transformer.TargetClassContext", false, IMixinInfo.class.getClassLoader());
                return var0.getClass() != var2
                    ? false
                    : StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
                        .walk(
                            var2x -> {
                                List var3x = var2x.filter(
                                        var0xx -> var0xx.getDeclaringClass() != NativeMapPutLateProofExtension.class
                                                && var0xx.getDeclaringClass() != OwnerFieldCoverageRegistry.class
                                    )
                                    .limit(3L)
                                    .toList();
                                return var3x.size() == 3
                                    && ((StackFrame)var3x.get(0)).getDeclaringClass() == Extensions.class
                                    && ((StackFrame)var3x.get(0)).getMethodName().equals(var1)
                                    && ((StackFrame)var3x.get(0))
                                        .getDescriptor()
                                        .equals("(Lorg/spongepowered/asm/mixin/transformer/ext/ITargetClassContext;)V")
                                    && ((StackFrame)var3x.get(1)).getDeclaringClass() == var2
                                    && ((StackFrame)var3x.get(1)).getMethodName().equals(var1)
                                    && ((StackFrame)var3x.get(1)).getDescriptor().equals("()V")
                                    && ((StackFrame)var3x.get(2)).getDeclaringClass() == var2
                                    && ((StackFrame)var3x.get(2)).getMethodName().equals("applyMixins")
                                    && ((StackFrame)var3x.get(2)).getDescriptor().equals("()V");
                            }
                        );
            } catch (ClassNotFoundException var3) {
                return false;
            }
        } else {
            return false;
        }
    }

    static ITargetClassContext current(ClassNode var0) {
        WeakReference var1 = ACTIVE.get().get(var0);
        ITargetClassContext var2 = var1 == null ? null : (ITargetClassContext)var1.get();
        return var2 != null && var2.getClassNode() == var0 ? var2 : null;
    }

    static boolean finishing(ITargetClassContext var0) {
        return var0 != null
            && current(var0.getClassNode()) == var0
            && callback(var0, "postApply")
            && ORDERS.get().get(var0.getClassNode()) == extensions.getActiveExtensions();
    }

    static List<IExtension> order() {
        return ordered() ? extensions.getActiveExtensions() : null;
    }

    static Map<String, String> componentParents(ClassNode target) {
        return current(target) == null ? null : COMPONENT_PARENTS.get().get(target);
    }

    static List<String> parent(ClassNode var0) {
        return current(var0) == null ? null : PARENTS.get().get(var0);
    }

    static List<List<String>> ownerParents(ClassNode var0) {
        return current(var0) == null ? null : OWNER_PARENTS.get().get(var0);
    }

    public boolean checkActive(MixinEnvironment var1) {
        return true;
    }

    public void preApply(ITargetClassContext var1) {
        if (var1 != null && "net/farmingtales/ftboss/item/FrostQueenStaffItem".equals(var1.getClassNode().name)) {
            if (!callback(var1, "preApply") || ACTIVE.get().size() >= 18 || ACTIVE.get().containsKey(var1.getClassNode())) throw new IllegalStateException("Untrusted Frost final preApply context");
            ACTIVE.get().put(var1.getClassNode(), new WeakReference<>(var1));
            ORDERS.get().put(var1.getClassNode(), extensions.getActiveExtensions());
            return;
        }
        if (registrationPlugin == NativeOwnerWearMixinPlugin.class) {
            return;
        }
        if (var1 != null
            && (
                OwnerFieldInstallationProof.observedFieldHost(var1.getClassNode())
                    || "net/minecraft/nbt/CompoundTag$1".equals(var1.getClassNode().name)
                    || NativeCapabilityInstallationProof170.host(var1.getClassNode()) >= 0
                    || NativeComponentInstallationProof163.host(var1.getClassNode()) >= 0
                    || OwnerFieldInstallationProof.nativeLoadHost(var1.getClassNode()) >= 0
                    || OwnerFieldInstallationProof.nativePutHost(var1.getClassNode()) >= 0
                    || OwnerFieldInstallationProof.nativeTridentHost(var1.getClassNode())
                    || OwnerFieldInstallationProof.nativeCaptureHost(var1.getClassNode())
                    || OwnerFieldInstallationProof.nativeChainHost(var1.getClassNode()) >= 0
                    || "net/farmingtales/ftboss/item/FoodWeaponEvolution".equals(var1.getClassNode().name)
                    || "net/farmingtales/ftboss/item/FoodWeaponRanged".equals(var1.getClassNode().name)
                    || "net/farmingtales/ftboss/item/FoodWeaponHitPlan$TagLeaf".equals(var1.getClassNode().name)
                    || "net/farmingtales/ftboss/item/FoodWeaponHitPlan".equals(var1.getClassNode().name)
            )) {
            if (!callback(var1, "preApply")) {
                throw new IllegalStateException("Untrusted late proof preApply");
            } else {
                Map<ClassNode, WeakReference<ITargetClassContext>> var2 = ACTIVE.get();
                var2.entrySet().removeIf(var0 -> ((WeakReference)var0.getValue()).get() == null);
                ORDERS.get().keySet().retainAll(var2.keySet());
                PARENTS.get().keySet().retainAll(var2.keySet());
                OWNER_PARENTS.get().keySet().retainAll(var2.keySet());
                COMPONENT_PARENTS.get().keySet().retainAll(var2.keySet());
                if (var2.size() < 18 && !var2.containsKey(var1.getClassNode())) {
                    if (NativeComponentInstallationProof163.host(var1.getClassNode()) >= 0) COMPONENT_PARENTS.get().put(var1.getClassNode(), NativeComponentInstallationProof163.snapshot(var1.getClassNode()));
                    if ("net/minecraft/world/item/ItemStack".equals(var1.getClassNode().name)) ITEMSTACK_PARENTS165.get().put(var1.getClassNode(), NativeItemStackInstallationProof165.snapshot(var1.getClassNode()));

                    if (OwnerFieldInstallationProof.nativeTridentHost(var1.getClassNode())) {
                        List<String> parent = OwnerFieldInstallationProof.nativeTridentSnapshot(var1.getClassNode());
                        if (parent == null) throw new IllegalStateException("Unsupported original native arrow pickup constructor");
                        PARENTS.get().put(var1.getClassNode(), parent);
                    }

                    if (OwnerFieldInstallationProof.nativeTickHost(var1.getClassNode())) {
                        List<String> tick = OwnerFieldInstallationProof.nativeTickSnapshot(var1.getClassNode());
                        if (tick == null) throw new IllegalStateException("Unsupported original native inventory tick parent");
                        OWNER_PARENTS.get().put(var1.getClassNode(), List.of(tick));
                    }

                    if (OwnerFieldInstallationProof.nativePutHost(var1.getClassNode()) >= 0) {
                        List<String> constructor = OwnerFieldInstallationProof.nativePutSnapshot(var1.getClassNode(), true);
                        if (constructor == null) throw new IllegalStateException("Unsupported original native put constructor");
                        PARENTS.get().put(var1.getClassNode(), constructor);
                        if (OwnerFieldInstallationProof.nativePutHost(var1.getClassNode()) == 1) {
                            List<String> parent = OwnerFieldInstallationProof.nativePutSnapshot(var1.getClassNode(), false);
                            if (parent == null) throw new IllegalStateException("Unsupported original native outcome put parent");
                            OWNER_PARENTS.get().put(var1.getClassNode(), List.of(parent));
                        }
                    }

                    if (OwnerFieldInstallationProof.nativeCaptureHost(var1.getClassNode())) {
                        List<String> capture = OwnerFieldInstallationProof.nativeCaptureSnapshot(var1.getClassNode());
                        if (capture == null) throw new IllegalStateException("Unsupported original native capture parent");
                        PARENTS.get().put(var1.getClassNode(), capture);
                    }

                    if (OwnerFieldInstallationProof.nativeChainHost(var1.getClassNode()) >= 0) {
                        List<String> chain = OwnerFieldInstallationProof.nativeChainSnapshot(var1.getClassNode());
                        if (chain == null) throw new IllegalStateException("Unsupported original native chain parent");
                        OWNER_PARENTS.get().put(var1.getClassNode(), List.of(chain));
                    }

                    if (OwnerFieldInstallationProof.nativeLoadHost(var1.getClassNode()) >= 0) {
                        List<String> parent = OwnerFieldInstallationProof.nativeLoadSnapshot(var1.getClassNode());
                        if (parent == null) throw new IllegalStateException("Unsupported original native load parent");
                        PARENTS.get().put(var1.getClassNode(), parent);
                    }

                    if ("net/farmingtales/ftboss/item/FoodWeaponHitPlan".equals(var1.getClassNode().name)) {
                        List<String> parent = OwnerFieldInstallationProof.semanticParentSnapshot(var1.getClassNode());
                        if (parent == null) throw new IllegalStateException("Unsupported original native semantic parent");
                        PARENTS.get().put(var1.getClassNode(), parent);
                    }

                    if ("net/minecraft/world/entity/Entity".equals(var1.getClassNode().name)) {
                        List<String> entityParent = OwnerFieldInstallationProof.entityLoadSnapshot(var1.getClassNode());
                        if (entityParent == null) throw new IllegalStateException("Unsupported original native Entity.load parent");
                        PARENTS.get().put(var1.getClassNode(), entityParent);
                    }

                    if ("net/farmingtales/ftboss/item/FoodWeaponEvolution".equals(var1.getClassNode().name)) {
                        List var3 = OwnerFieldInstallationProof.timerParentSnapshot(var1.getClassNode());
                        if (var3 == null) {
                            throw new IllegalStateException("Unsupported original timer parent");
                        }

                        PARENTS.get().put(var1.getClassNode(), var3);
                    }

                    if ("net/farmingtales/ftboss/item/FoodWeaponEvolution".equals(var1.getClassNode().name) || "net/farmingtales/ftboss/item/FoodWeaponRanged".equals(var1.getClassNode().name) || "net/farmingtales/ftboss/item/FoodWeaponHitPlan$TagLeaf".equals(var1.getClassNode().name)) {
                        List var4 = OwnerFieldInstallationProof.ownerParentsSnapshot(var1.getClassNode());
                        if (var4 == null) {
                            throw new IllegalStateException("Unsupported original Owner parent");
                        }

                        OWNER_PARENTS.get().put(var1.getClassNode(), var4);
                    }

                    var2.put(var1.getClassNode(), new WeakReference<>(var1));
                    ORDERS.get().put(var1.getClassNode(), extensions.getActiveExtensions());
                    if (NativeCapabilityInstallationProof170.host(var1.getClassNode()) >= 0) NativeCapabilityCertificate170.begin(var1);
                } else {
                    throw new IllegalStateException("Overlapping late proof context");
                }
            }
        }
    }

    public void postApply(ITargetClassContext var1) {
        if (var1 != null && "net/farmingtales/ftboss/item/FrostQueenStaffItem".equals(var1.getClassNode().name)) {
            try {
                if (!finishing(var1)) throw new IllegalStateException("Untrusted Frost final postApply context");
                NativeComponentReadCutHooks.verifyFrostIngress(var1.getClassNode(), "net.farmingtales.ftboss.mixin.NativeReadCutLivingEntityMixin");
            } finally {
                ACTIVE.get().remove(var1.getClassNode()); ORDERS.get().remove(var1.getClassNode());
                if (ACTIVE.get().isEmpty()) { ACTIVE.remove(); ORDERS.remove(); }
            }
            return;
        }
        if (registrationPlugin == NativeOwnerWearMixinPlugin.class) {
            if (var1 != null && ("net/minecraft/world/item/ItemStack".equals(var1.getClassNode().name) || "net/minecraft/world/entity/player/Inventory".equals(var1.getClassNode().name)
                || "net/minecraft/world/level/storage/PlayerDataStorage".equals(var1.getClassNode().name))) {
                if (!callback(var1, "postApply")) throw new IllegalStateException("Untrusted native owner wear final callback");
                OwnerStatueWearBoundary.install(var1.getClassNode());
            }
            return;
        }
        if (var1 != null && "net/minecraft/nbt/CompoundTag$1".equals(var1.getClassNode().name)) {
            try {
                NativeLoaderInstallation295.finished(var1, finishing(var1));
            } finally {
                Map active = ACTIVE.get();active.remove(var1.getClassNode());ORDERS.get().remove(var1.getClassNode());PARENTS.get().remove(var1.getClassNode());OWNER_PARENTS.get().remove(var1.getClassNode());COMPONENT_PARENTS.get().remove(var1.getClassNode());ITEMSTACK_PARENTS165.get().remove(var1.getClassNode());
                if (active.isEmpty()) {ACTIVE.remove();ORDERS.remove();PARENTS.remove();OWNER_PARENTS.remove();COMPONENT_PARENTS.remove();ITEMSTACK_PARENTS165.remove();}
            }
            return;
        }
        if (var1 != null && OwnerFieldInstallationProof.nativeLoadHost(var1.getClassNode()) >= 0) {
            try {
                if (!finishing(var1)) throw new IllegalStateException("Untrusted native load final context");
                if ("net/minecraft/world/item/ItemStack".equals(var1.getClassNode().name)) OwnerStatueWearBoundary.install(var1.getClassNode());
                if (NativeComponentInstallationProof163.host(var1.getClassNode()) >= 0) OwnerFieldCoverageRegistry.nativeComponentsFinished(var1);
                if ("net/minecraft/world/item/ItemStack".equals(var1.getClassNode().name)) OwnerFieldCoverageRegistry.nativeItemStackFinished165(var1);
                if (NativeCapabilityInstallationProof170.host(var1.getClassNode()) >= 0) OwnerFieldCoverageRegistry.nativeCapabilityFinished170(var1);
                OwnerFieldCoverageRegistry.nativeLoadsFinished(var1);
                if (OwnerFieldInstallationProof.nativeTickHost(var1.getClassNode())) OwnerFieldCoverageRegistry.nativeTickFinished(var1);
                if (OwnerFieldInstallationProof.nativeChainHost(var1.getClassNode()) >= 0) OwnerFieldCoverageRegistry.nativeChainsFinished(var1);
            } finally {
                Map active = ACTIVE.get();active.remove(var1.getClassNode());ORDERS.get().remove(var1.getClassNode());PARENTS.get().remove(var1.getClassNode());OWNER_PARENTS.get().remove(var1.getClassNode());COMPONENT_PARENTS.get().remove(var1.getClassNode());ITEMSTACK_PARENTS165.get().remove(var1.getClassNode());
                if (active.isEmpty()) {ACTIVE.remove();ORDERS.remove();PARENTS.remove();OWNER_PARENTS.remove();COMPONENT_PARENTS.remove();ITEMSTACK_PARENTS165.remove();}
            }
            return;
        }
        if (var1 == null
            || !"net/minecraft/world/item/ItemStack".equals(var1.getClassNode().name)
                && !"net/minecraft/world/entity/player/Inventory".equals(var1.getClassNode().name)
                && !"net/minecraft/world/level/storage/PlayerDataStorage".equals(var1.getClassNode().name)) {
            if (var1 != null
                && (
                    OwnerFieldInstallationProof.observedFieldHost(var1.getClassNode())
                    || NativeCapabilityInstallationProof170.host(var1.getClassNode()) >= 0
                    || NativeComponentInstallationProof163.host(var1.getClassNode()) >= 0
                        || OwnerFieldInstallationProof.nativeTridentHost(var1.getClassNode())
                        || OwnerFieldInstallationProof.nativePutHost(var1.getClassNode()) >= 0
                        || OwnerFieldInstallationProof.nativeCaptureHost(var1.getClassNode())
                    || OwnerFieldInstallationProof.nativeChainHost(var1.getClassNode()) >= 0
                        || "net/farmingtales/ftboss/item/FoodWeaponEvolution".equals(var1.getClassNode().name)
                        || "net/farmingtales/ftboss/item/FoodWeaponRanged".equals(var1.getClassNode().name)
                        || "net/farmingtales/ftboss/item/FoodWeaponHitPlan$TagLeaf".equals(var1.getClassNode().name)
                    || "net/farmingtales/ftboss/item/FoodWeaponHitPlan".equals(var1.getClassNode().name)
                )) {
                try {
                    if (!finishing(var1)) {
                        throw new IllegalStateException("Untrusted late proof postApply");
                    }

                    if (NativeComponentInstallationProof163.host(var1.getClassNode()) >= 0) OwnerFieldCoverageRegistry.nativeComponentsFinished(var1);
                    if (OwnerFieldInstallationProof.nativeChainHost(var1.getClassNode()) >= 0) OwnerFieldCoverageRegistry.nativeChainsFinished(var1);
                    if (OwnerFieldInstallationProof.nativeTridentHost(var1.getClassNode())) OwnerFieldCoverageRegistry.nativeTridentFinished(var1);
                    if (OwnerFieldInstallationProof.nativeCaptureHost(var1.getClassNode())) OwnerFieldCoverageRegistry.nativeCaptureFinished(var1);
                    if (OwnerFieldInstallationProof.nativePutHost(var1.getClassNode()) >= 0) OwnerFieldCoverageRegistry.nativePutsFinished(var1);
                    if (NativeCapabilityInstallationProof170.host(var1.getClassNode()) >= 0) OwnerFieldCoverageRegistry.nativeCapabilityFinished170(var1);
                    if ("net/minecraft/nbt/CompoundTag".equals(var1.getClassNode().name)) NativeMapFactoryInstallation288.finished(var1, finishing(var1));
                    OwnerFieldCoverageRegistry.lateApplied(var1);
                    if ("net/farmingtales/ftboss/item/FoodWeaponHitPlan".equals(var1.getClassNode().name) || "net/farmingtales/ftboss/item/FoodWeaponRanged".equals(var1.getClassNode().name)) OwnerFieldCoverageRegistry.operationsFinished(var1);
                    if (OwnerFieldInstallationProof.observedFieldHost(var1.getClassNode())) OwnerFieldCoverageRegistry.fieldsFinished(var1);
                } finally {
                    Map var4 = ACTIVE.get();
                    var4.remove(var1.getClassNode());
                    ORDERS.get().remove(var1.getClassNode());
                    PARENTS.get().remove(var1.getClassNode());
                    OWNER_PARENTS.get().remove(var1.getClassNode());COMPONENT_PARENTS.get().remove(var1.getClassNode());ITEMSTACK_PARENTS165.get().remove(var1.getClassNode());
                    if (var4.isEmpty()) {
                        ACTIVE.remove();
                        ORDERS.remove();
                        PARENTS.remove();
                        OWNER_PARENTS.remove();COMPONENT_PARENTS.remove();ITEMSTACK_PARENTS165.remove();
                    }
                }
            }
        } else if (!callback(var1, "postApply")) {
            throw new IllegalStateException("Untrusted final original wear boundary");
        } else {
            OwnerStatueWearBoundary.install(var1.getClassNode());
        }
    }

    public void export(MixinEnvironment var1, String var2, boolean var3, ClassNode var4) {
    }
    static ClassNode nativeItemStackParents165(ClassNode target) {
        return current(target) == null ? null : ITEMSTACK_PARENTS165.get().get(target);
    }

}
