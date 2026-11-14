/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.MixinExtrasBootstrap
 *  com.llamalad7.mixinextras.injector.LateInjectionApplicatorExtension
 *  com.llamalad7.mixinextras.utils.MixinInternals
 *  org.objectweb.asm.tree.ClassNode
 *  org.spongepowered.asm.mixin.MixinEnvironment
 *  org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
 *  org.spongepowered.asm.mixin.extensibility.IMixinInfo
 *  org.spongepowered.asm.mixin.transformer.ext.Extensions
 *  org.spongepowered.asm.mixin.transformer.ext.IExtension
 *  org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext
 *  org.spongepowered.asm.mixin.transformer.ext.extensions.ExtensionCheckClass
 */
package net.farmingtales.ftboss.item;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import com.llamalad7.mixinextras.injector.LateInjectionApplicatorExtension;
import com.llamalad7.mixinextras.utils.MixinInternals;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.item.OwnerFieldCoverageRegistry;
import net.farmingtales.ftboss.item.OwnerFieldInstallationProof;
import net.farmingtales.ftboss.item.OwnerStatueWearBoundary;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;
import org.spongepowered.asm.mixin.transformer.ext.extensions.ExtensionCheckClass;

public final class NativeMapPutLateProofExtension
implements IExtension {
    private static final NativeMapPutLateProofExtension INSTANCE = new NativeMapPutLateProofExtension();
    private static final ThreadLocal<Map<ClassNode, WeakReference<ITargetClassContext>>> ACTIVE = ThreadLocal.withInitial(WeakHashMap::new);
    private static final ThreadLocal<Map<ClassNode, List<IExtension>>> ORDERS = ThreadLocal.withInitial(WeakHashMap::new);
    private static final ThreadLocal<Map<ClassNode, List<String>>> PARENTS = ThreadLocal.withInitial(WeakHashMap::new);
    private static final ThreadLocal<Map<ClassNode, List<List<String>>>> OWNER_PARENTS = ThreadLocal.withInitial(WeakHashMap::new);
    private static volatile Extensions extensions;
    private static volatile Class<?> registrationPlugin;

    private NativeMapPutLateProofExtension() {
    }

    public static synchronized void register() {
        Class clazz = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> {
            List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass() != NativeMapPutLateProofExtension.class).limit(3L).toList();
            if (!(list.size() == 3 && list.get(0).getMethodName().equals("onLoad") && list.get(0).getDescriptor().equals("(Ljava/lang/String;)V") && list.get(1).getMethodName().equals("onLoad") && list.get(1).getDescriptor().equals("(Ljava/lang/String;)V") && list.get(2).getMethodName().equals("onSelect") && list.get(2).getDescriptor().equals("()V"))) {
                return null;
            }
            try {
                ClassLoader classLoader = IMixinInfo.class.getClassLoader();
                if (list.get(1).getDeclaringClass() != Class.forName("org.spongepowered.asm.mixin.transformer.PluginHandle", false, classLoader) || list.get(2).getDeclaringClass() != Class.forName("org.spongepowered.asm.mixin.transformer.MixinConfig", false, classLoader)) {
                    return null;
                }
                Class<?> clazz = list.get(0).getDeclaringClass();
                return clazz.getName().equals("net.farmingtales.ftboss.mixin.NativeOptionalMixinPlugin") && IMixinConfigPlugin.class.isAssignableFrom(clazz) ? clazz : null;
            }
            catch (ClassNotFoundException classNotFoundException) {
                return null;
            }
        });
        if (clazz == null || registrationPlugin != null && registrationPlugin != clazz) {
            throw new IllegalStateException("Untrusted late proof registration");
        }
        MixinExtrasBootstrap.init();
        Extensions extensions = MixinInternals.getExtensions();
        if (NativeMapPutLateProofExtension.extensions != null) {
            if (NativeMapPutLateProofExtension.extensions != extensions || !extensions.getExtensions().contains(INSTANCE)) {
                throw new IllegalStateException("Changed extension registry");
            }
            return;
        }
        MixinInternals.registerExtension((IExtension)INSTANCE);
        NativeMapPutLateProofExtension.extensions = extensions;
        if (!NativeMapPutLateProofExtension.ordered()) {
            throw new IllegalStateException("Late proof extension is not active after MixinExtras");
        }
        registrationPlugin = clazz;
    }

    static boolean registeredPlugin(Class<?> clazz) {
        return clazz != null && clazz == registrationPlugin;
    }

    private static boolean ordered() {
        int n;
        if (extensions == null || extensions != MixinInternals.getExtensions()) {
            return false;
        }
        List list = extensions.getActiveExtensions();
        int n2 = -1;
        int n3 = -1;
        for (n = 0; n < list.size(); ++n) {
            IExtension iExtension = (IExtension)list.get(n);
            if (iExtension == INSTANCE) {
                if (n2 >= 0) {
                    return false;
                }
                n2 = n;
            }
            if (iExtension.getClass() != LateInjectionApplicatorExtension.class) continue;
            if (n3 >= 0) {
                return false;
            }
            n3 = n;
        }
        if (n2 < 0 || n3 < 0 || n3 >= n2) {
            return false;
        }
        for (n = n2 + 1; n < list.size(); ++n) {
            if (((IExtension)list.get(n)).getClass() == ExtensionCheckClass.class) continue;
            return false;
        }
        return true;
    }

    private static boolean callback(ITargetClassContext iTargetClassContext, String string) {
        if (iTargetClassContext == null || !NativeMapPutLateProofExtension.ordered()) {
            return false;
        }
        try {
            Class<?> clazz = Class.forName("org.spongepowered.asm.mixin.transformer.TargetClassContext", false, IMixinInfo.class.getClassLoader());
            if (iTargetClassContext.getClass() != clazz) {
                return false;
            }
            return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stream -> {
                List<StackWalker.StackFrame> list = stream.filter(stackFrame -> stackFrame.getDeclaringClass() != NativeMapPutLateProofExtension.class && stackFrame.getDeclaringClass() != OwnerFieldCoverageRegistry.class).limit(3L).toList();
                return list.size() == 3 && list.get(0).getDeclaringClass() == Extensions.class && list.get(0).getMethodName().equals(string) && list.get(0).getDescriptor().equals("(Lorg/spongepowered/asm/mixin/transformer/ext/ITargetClassContext;)V") && list.get(1).getDeclaringClass() == clazz && list.get(1).getMethodName().equals(string) && list.get(1).getDescriptor().equals("()V") && list.get(2).getDeclaringClass() == clazz && list.get(2).getMethodName().equals("applyMixins") && list.get(2).getDescriptor().equals("()V");
            });
        }
        catch (ClassNotFoundException classNotFoundException) {
            return false;
        }
    }

    static ITargetClassContext current(ClassNode classNode) {
        WeakReference<ITargetClassContext> weakReference = ACTIVE.get().get(classNode);
        ITargetClassContext iTargetClassContext = weakReference == null ? null : (ITargetClassContext)weakReference.get();
        return iTargetClassContext != null && iTargetClassContext.getClassNode() == classNode ? iTargetClassContext : null;
    }

    static boolean finishing(ITargetClassContext iTargetClassContext) {
        return iTargetClassContext != null && NativeMapPutLateProofExtension.current(iTargetClassContext.getClassNode()) == iTargetClassContext && NativeMapPutLateProofExtension.callback(iTargetClassContext, "postApply") && ORDERS.get().get(iTargetClassContext.getClassNode()) == extensions.getActiveExtensions();
    }

    static List<IExtension> order() {
        return NativeMapPutLateProofExtension.ordered() ? extensions.getActiveExtensions() : null;
    }

    static List<String> parent(ClassNode classNode) {
        return NativeMapPutLateProofExtension.current(classNode) == null ? null : PARENTS.get().get(classNode);
    }

    static List<List<String>> ownerParents(ClassNode classNode) {
        return NativeMapPutLateProofExtension.current(classNode) == null ? null : OWNER_PARENTS.get().get(classNode);
    }

    public boolean checkActive(MixinEnvironment mixinEnvironment) {
        return true;
    }

    public void preApply(ITargetClassContext iTargetClassContext) {
        List<Object> list;
        if (iTargetClassContext == null || !"net/minecraft/nbt/CompoundTag".equals(iTargetClassContext.getClassNode().name) && !"net/farmingtales/ftboss/item/FoodWeaponEvolution".equals(iTargetClassContext.getClassNode().name) && !"net/farmingtales/ftboss/item/FoodWeaponRanged".equals(iTargetClassContext.getClassNode().name) && !"net/farmingtales/ftboss/item/FoodWeaponHitPlan$TagLeaf".equals(iTargetClassContext.getClassNode().name)) {
            return;
        }
        if (!NativeMapPutLateProofExtension.callback(iTargetClassContext, "preApply")) {
            throw new IllegalStateException("Untrusted late proof preApply");
        }
        Map<ClassNode, WeakReference<ITargetClassContext>> map = ACTIVE.get();
        map.entrySet().removeIf(entry -> ((WeakReference)entry.getValue()).get() == null);
        ORDERS.get().keySet().retainAll(map.keySet());
        PARENTS.get().keySet().retainAll(map.keySet());
        OWNER_PARENTS.get().keySet().retainAll(map.keySet());
        if (map.size() >= 4 || map.containsKey(iTargetClassContext.getClassNode())) {
            throw new IllegalStateException("Overlapping late proof context");
        }
        if ("net/farmingtales/ftboss/item/FoodWeaponEvolution".equals(iTargetClassContext.getClassNode().name)) {
            list = OwnerFieldInstallationProof.timerParentSnapshot(iTargetClassContext.getClassNode());
            if (list == null) {
                throw new IllegalStateException("Unsupported original timer parent");
            }
            PARENTS.get().put(iTargetClassContext.getClassNode(), list);
        }
        if (!"net/minecraft/nbt/CompoundTag".equals(iTargetClassContext.getClassNode().name)) {
            list = OwnerFieldInstallationProof.ownerParentsSnapshot(iTargetClassContext.getClassNode());
            if (list == null) {
                throw new IllegalStateException("Unsupported original Owner parent");
            }
            OWNER_PARENTS.get().put(iTargetClassContext.getClassNode(), list);
        }
        map.put(iTargetClassContext.getClassNode(), new WeakReference<ITargetClassContext>(iTargetClassContext));
        ORDERS.get().put(iTargetClassContext.getClassNode(), extensions.getActiveExtensions());
    }

    public void postApply(ITargetClassContext iTargetClassContext) {
        if (iTargetClassContext != null && ("net/minecraft/world/item/ItemStack".equals(iTargetClassContext.getClassNode().name) || "net/minecraft/world/entity/player/Inventory".equals(iTargetClassContext.getClassNode().name))) {
            if (!NativeMapPutLateProofExtension.callback(iTargetClassContext, "postApply")) {
                throw new IllegalStateException("Untrusted final original wear boundary");
            }
            OwnerStatueWearBoundary.install(iTargetClassContext.getClassNode());
            return;
        }
        if (iTargetClassContext == null || !"net/minecraft/nbt/CompoundTag".equals(iTargetClassContext.getClassNode().name) && !"net/farmingtales/ftboss/item/FoodWeaponEvolution".equals(iTargetClassContext.getClassNode().name) && !"net/farmingtales/ftboss/item/FoodWeaponRanged".equals(iTargetClassContext.getClassNode().name) && !"net/farmingtales/ftboss/item/FoodWeaponHitPlan$TagLeaf".equals(iTargetClassContext.getClassNode().name)) {
            return;
        }
        try {
            if (!NativeMapPutLateProofExtension.finishing(iTargetClassContext)) {
                throw new IllegalStateException("Untrusted late proof postApply");
            }
            OwnerFieldCoverageRegistry.lateApplied(iTargetClassContext);
        }
        finally {
            Map<ClassNode, WeakReference<ITargetClassContext>> map = ACTIVE.get();
            map.remove(iTargetClassContext.getClassNode());
            ORDERS.get().remove(iTargetClassContext.getClassNode());
            PARENTS.get().remove(iTargetClassContext.getClassNode());
            OWNER_PARENTS.get().remove(iTargetClassContext.getClassNode());
            if (map.isEmpty()) {
                ACTIVE.remove();
                ORDERS.remove();
                PARENTS.remove();
                OWNER_PARENTS.remove();
            }
        }
    }

    public void export(MixinEnvironment mixinEnvironment, String string, boolean bl, ClassNode classNode) {
    }
}

