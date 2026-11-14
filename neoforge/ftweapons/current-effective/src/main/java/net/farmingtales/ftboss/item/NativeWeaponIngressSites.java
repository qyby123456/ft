package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class NativeWeaponIngressSites {
    private static final String MIXIN = "net.farmingtales.ftboss.mixin.";
    private static final String SPELL = "io.redspace.ironsspellbooks.api.spells.AbstractSpell";
    private static final String ATTEMPT = "(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lio/redspace/ironsspellbooks/api/spells/CastSource;ZLjava/lang/String;)Z";
    private static final String CAST = "(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V";
    private static final String WEAR = "(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V";
    private static final String EVENT = "Lnet/neoforged/neoforge/event/entity/player/PlayerInteractEvent$RightClickItem;";
    private static final List<Site> REQUIRED = List.of(
        new Site("NativeIronSelectionMixin", "io.redspace.ironsspellbooks.player.ServerPlayerEvents", "ftboss$v9$ironUseBegin", "(" + EVENT + "Lnet/farmingtales/ftboss/integ/NativeIronExecution$UseFrame;)V", "onUseItem", "(" + EVENT + ")V"),
        new Site("FoodWeaponStaffCastMixin", SPELL, "ftboss$v9$ironAttemptBegin", ATTEMPT.substring(0, ATTEMPT.indexOf(')')) + "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V", "attemptInitiateCast", ATTEMPT),
        new Site("FoodWeaponStaffCastMixin", SPELL, "ftboss$v9$ironAttemptReturn", ATTEMPT.substring(0, ATTEMPT.indexOf(')')) + "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V", "attemptInitiateCast", ATTEMPT),
        new Site("FoodWeaponStaffCastMixin", SPELL, "ftboss$v9$ironPost", "(Lnet/neoforged/bus/api/IEventBus;Lnet/neoforged/bus/api/Event;Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)Lnet/neoforged/bus/api/Event;", "castSpell", CAST),
        new Site("FoodWeaponStaffCastMixin", SPELL, "ftboss$v9$ironMana", "(Lio/redspace/ironsspellbooks/api/magic/MagicData;F)V", "castSpell", CAST),
        new Site("FoodWeaponStaffCastMixin", SPELL, "ftboss$v9$ironCast", "(Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/spells/CastSource;Lio/redspace/ironsspellbooks/api/magic/MagicData;)V", "castSpell", CAST),
        new Site("FoodWeaponStaffCastMixin", SPELL, "ftboss$v9$ironCastReturn", CAST.substring(0, CAST.indexOf(')')) + "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V", "castSpell", CAST),
        new Site("NativeIronActualDamageMixin", "io.redspace.ironsspellbooks.damage.DamageSources", "ftboss$v10$ironLivingHurt", "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/damagesource/DamageSource;F)Z", "applyDamage", "(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;)Z"),
        new Site("NativeIronActualDamageMixin", "io.redspace.ironsspellbooks.damage.DamageSources", "ftboss$v10$ironEntityHurt", "(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)Z", "applyDamage", "(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;)Z"),
        new Site("NativeWearTokenMixin", "net.minecraft.world.item.ItemStack", "ftboss$v9$nativeWearBegin", WEAR.substring(0, WEAR.indexOf(')')) + "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V", "hurtAndBreak", WEAR),
        new Site("NativeWearTokenMixin", "net.minecraft.world.item.ItemStack", "ftboss$v9$nativeWearReturn", WEAR.substring(0, WEAR.indexOf(')')) + "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V", "hurtAndBreak", WEAR)
    );
    private static final Map<Site,String> INSTALLED = new LinkedHashMap<>();
    private static IMixinConfig configuration;

    private NativeWeaponIngressSites() {
    }

    private static boolean trusted(IMixinInfo info) {
        if (info == null) return false;
        return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(frames -> {
            List<StackFrame> callers = frames.filter(frame -> frame.getDeclaringClass() != NativeWeaponIngressSites.class).limit(3).toList();
            if (callers.size() != 3 || callers.stream().anyMatch(frame -> !frame.getMethodName().equals("postApply"))) return false;
            try {
                ClassLoader loader = IMixinInfo.class.getClassLoader();
                IMixinConfig config = info.getConfig();
                return callers.get(0).getDeclaringClass() == NativeWeaponLifecycleMixinPlugin.class
                    && callers.get(0).getDescriptor().equals("(Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;Ljava/lang/String;Lorg/spongepowered/asm/mixin/extensibility/IMixinInfo;)V")
                    && callers.get(1).getDeclaringClass() == Class.forName("org.spongepowered.asm.mixin.transformer.PluginHandle", false, loader)
                    && callers.get(1).getDescriptor().equals("(Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;Ljava/lang/String;Lorg/spongepowered/asm/mixin/transformer/MixinInfo;)V")
                    && callers.get(2).getDeclaringClass() == Class.forName("org.spongepowered.asm.mixin.transformer.MixinInfo", false, loader)
                    && callers.get(2).getDescriptor().equals("(Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;)V")
                    && info.getClass() == callers.get(2).getDeclaringClass()
                    && config != null && config.getClass() == Class.forName("org.spongepowered.asm.mixin.transformer.MixinConfig", false, loader)
                    && config.getName().equals("ftboss.weapon-lifecycle.mixins.json") && config.getMixinPackage().equals(MIXIN) && config.isRequired()
                    && config.getPlugin() != null && config.getPlugin().getClass() == NativeWeaponLifecycleMixinPlugin.class
                    && (configuration == null || config == configuration);
            } catch (ClassNotFoundException failure) {
                return false;
            }
        });
    }

    private static boolean merged(MethodNode method, String mixin) {
        if (method.visibleAnnotations == null) return false;
        for (AnnotationNode annotation : method.visibleAnnotations) {
            if (!annotation.desc.equals("Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;") || annotation.values == null) continue;
            String owner = null;
            String session = null;
            for (int index = 0; index < annotation.values.size(); index += 2) {
                if (annotation.values.get(index).equals("mixin")) owner = String.valueOf(annotation.values.get(index+1));
                if (annotation.values.get(index).equals("sessionId")) session = String.valueOf(annotation.values.get(index+1));
            }
            if (mixin.equals(owner) && session != null && !session.isBlank()) return true;
        }
        return false;
    }

    public static synchronized void applied(String target, ClassNode owner, String mixin, IMixinInfo info) {
        if (!trusted(info) || owner == null || !owner.name.replace('/', '.').equals(target) || !info.getClassName().equals(mixin)) throw new IllegalStateException("Untrusted native ingress site registration");
        for (Site site : REQUIRED) {
            if (!(MIXIN + site.mixin()).equals(mixin)) continue;
            if (!site.target().equals(target)) throw new IllegalStateException("Wrong native ingress target " + mixin + ":" + target);
            List<MethodNode> handlers = owner.methods.stream().filter(method -> method.desc.equals(site.descriptor()) && merged(method, mixin) && (method.name.equals(site.logical()) || method.name.endsWith("$" + site.logical()))).toList();
            if (handlers.size() != 1) throw new IllegalStateException("Missing unique merged native ingress " + site.logical() + ":" + handlers.size());
            List<MethodNode> parents = owner.methods.stream().filter(method -> method.name.equals(site.parent()) && method.desc.equals(site.parentDescriptor())).toList();
            if (parents.size() != 1) throw new IllegalStateException("Missing original native ingress parent " + site.parent());
            boolean called = false;
            for (var instruction : parents.getFirst().instructions) if (instruction instanceof MethodInsnNode call && call.owner.equals(owner.name) && call.name.equals(handlers.getFirst().name) && call.desc.equals(site.descriptor())) called = true;
            if (!called) throw new IllegalStateException("Merged native ingress not called by exact original parent " + site.logical());
            String previous = INSTALLED.putIfAbsent(site, handlers.getFirst().name);
            if (previous != null && !previous.equals(handlers.getFirst().name)) throw new IllegalStateException("Changed native ingress identity " + site.logical());
        }
        configuration = info.getConfig();
    }

    public static synchronized boolean matches(StackFrame frame, String target, String logical, String descriptor) {
        if (frame == null || configuration == null || !frame.getClassName().equals(target) || !frame.getDescriptor().equals(descriptor)) return false;
        for (var entry : INSTALLED.entrySet()) {
            Site site = entry.getKey();
            if (site.target().equals(target) && site.logical().equals(logical) && site.descriptor().equals(descriptor) && entry.getValue().equals(frame.getMethodName())) return true;
        }
        return false;
    }

    public static synchronized int installedCount() {
        return INSTALLED.size();
    }

    private record Site(String mixin, String target, String logical, String descriptor, String parent, String parentDescriptor) {
    }
}
