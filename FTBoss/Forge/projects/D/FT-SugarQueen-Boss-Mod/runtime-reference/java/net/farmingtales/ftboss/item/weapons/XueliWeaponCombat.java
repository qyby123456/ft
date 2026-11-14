/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.TamableAnimal
 *  net.minecraft.world.entity.animal.horse.AbstractHorse
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.monster.Enemy
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.entity.living.LivingDropsEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.item.weapons;

import java.util.Comparator;
import java.util.List;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.XueliWeaponItems;
import net.farmingtales.ftboss.item.weapons.XueliWeaponRules;
import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class XueliWeaponCombat {
    private static final String MARK_KEY = "ftboss_xueli_rose";

    private XueliWeaponCombat() {
    }

    public static boolean canTarget(ServerPlayer player, LivingEntity target) {
        Mob mob;
        AbstractHorse horse;
        TamableAnimal animal;
        if (target == player || !target.m_6084_() || target.m_20147_() || target.m_5833_() || player.m_7307_((Entity)target) || target.m_7307_((Entity)player) || !player.m_142582_((Entity)target)) {
            return false;
        }
        if (target instanceof Player) {
            Player other = (Player)target;
            return !other.m_7500_() && player.f_8924_.m_129799_() && player.m_7099_(other);
        }
        if (target instanceof TamableAnimal && (animal = (TamableAnimal)target).m_21824_()) {
            return false;
        }
        if (target instanceof AbstractHorse && (horse = (AbstractHorse)target).m_30614_()) {
            return false;
        }
        return target instanceof Enemy || target instanceof EasterBossEntity || target instanceof Mob && ((mob = (Mob)target).m_5448_() == player || player.m_21214_() == target);
    }

    private static Vec3 direction(Player player) {
        Vec3 look = player.m_20154_();
        Vec3 horizontal = new Vec3(look.f_82479_, 0.0, look.f_82481_);
        if (horizontal.m_82556_() < 1.0E-6) {
            double yaw = Math.toRadians(player.m_146908_());
            return new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
        }
        return horizontal.m_82541_();
    }

    private static List<LivingEntity> candidates(ServerPlayer player, double radius) {
        return player.m_284548_().m_6443_(LivingEntity.class, player.m_20191_().m_82377_(radius, 2.0, radius), target -> XueliWeaponCombat.canTarget(player, target));
    }

    private static void durability(ServerPlayer player, ItemStack stack, int amount) {
        stack.m_41622_(amount, (LivingEntity)player, owner -> owner.m_21190_(InteractionHand.MAIN_HAND));
    }

    private static void sound(ServerPlayer player, SoundEvent sound, float pitch) {
        player.m_284548_().m_6263_(null, player.m_20185_(), player.m_20186_(), player.m_20189_(), sound, SoundSource.PLAYERS, 0.8f, pitch);
    }

    private static void halo(ServerLevel level, Vec3 center, double radius, int count, boolean gold) {
        WeaponFx.ring(level, center, radius, gold ? WeaponFxRules.Theme.GOLD : WeaponFxRules.Theme.ROSE);
    }

    public static void mark(ServerPlayer player, LivingEntity target) {
        CompoundTag rose = new CompoundTag();
        rose.m_128362_("owner", player.m_20148_());
        rose.m_128356_("expires", player.m_284548_().m_46467_() + 80L);
        target.getPersistentData().m_128365_(MARK_KEY, (Tag)rose);
        XueliWeaponCombat.halo(player.m_284548_(), target.m_20182_().m_82520_(0.0, (double)target.m_20206_() * 0.65, 0.0), 0.42, 8, false);
    }

    private static boolean hasMark(ServerPlayer player, LivingEntity target) {
        CompoundTag rose = target.getPersistentData().m_128469_(MARK_KEY);
        return rose.m_128403_("owner") && rose.m_128342_("owner").equals(player.m_20148_()) && rose.m_128454_("expires") > player.m_284548_().m_46467_();
    }

    public static void sweep(ServerPlayer player, ItemStack stack) {
        ServerLevel level = player.m_284548_();
        Vec3 forward = XueliWeaponCombat.direction((Player)player);
        Vec3 side = new Vec3(-forward.f_82481_, 0.0, forward.f_82479_);
        player.m_36335_().m_41524_(stack.m_41720_(), 120);
        List<LivingEntity> targets = XueliWeaponCombat.candidates(player, 3.6).stream().filter(target -> {
            Vec3 offset = target.m_20182_().m_82546_(player.m_20182_());
            return XueliWeaponRules.inSweep(offset.m_82526_(forward), offset.m_82526_(side));
        }).sorted(Comparator.comparingDouble(arg_0 -> ((ServerPlayer)player).m_20280_(arg_0))).limit(12L).toList();
        for (LivingEntity target2 : targets) {
            if (!target2.m_6469_(player.m_269291_().m_269075_((Player)player), XueliWeaponRules.sweepDamage()) || !target2.m_6084_()) continue;
            XueliWeaponCombat.mark(player, target2);
            target2.m_147240_(0.6, player.m_20185_() - target2.m_20185_(), player.m_20189_() - target2.m_20189_());
        }
        Vec3 center = player.m_20182_().m_82520_(0.0, 0.9, 0.0);
        WeaponFx.send(level, WeaponFxRules.Kind.SWEEP, WeaponFxRules.Theme.ROSE, center, center, 3.6, player.m_146908_());
        XueliWeaponCombat.sound(player, SoundEvents.f_12317_, 0.8f);
        XueliWeaponCombat.sound(player, SoundEvents.f_11667_, 1.65f);
        XueliWeaponCombat.durability(player, stack, 3);
        player.m_21011_(InteractionHand.MAIN_HAND, true);
    }

    public static void roll(ServerPlayer player, ItemStack stack, int chargeTicks) {
        ServerLevel level = player.m_284548_();
        Vec3 forward = XueliWeaponCombat.direction((Player)player);
        Vec3 side = new Vec3(-forward.f_82481_, 0.0, forward.f_82479_);
        Vec3 origin = player.m_20182_().m_82520_(0.0, 0.65, 0.0);
        Vec3 end = origin.m_82549_(forward.m_82490_(6.0));
        Vec3 stop = level.m_45547_(new ClipContext(origin, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player)).m_82450_();
        double length = stop.m_82554_(origin);
        player.m_36335_().m_41524_(stack.m_41720_(), 160);
        List<LivingEntity> targets = XueliWeaponCombat.candidates(player, 6.0).stream().filter(target -> {
            Vec3 offset = target.m_20182_().m_82546_(player.m_20182_());
            return XueliWeaponRules.inRoll(offset.m_82526_(forward), offset.m_82526_(side), length);
        }).sorted(Comparator.comparingDouble(arg_0 -> ((ServerPlayer)player).m_20280_(arg_0))).limit(12L).toList();
        for (LivingEntity target2 : targets) {
            boolean combo = XueliWeaponCombat.hasMark(player, target2);
            float damage = XueliWeaponRules.rollDamage(chargeTicks) + (combo ? XueliWeaponRules.comboDamage() : 0.0f);
            if (!target2.m_6469_(player.m_269291_().m_269075_((Player)player), damage)) continue;
            if (target2.m_6084_()) {
                target2.m_147207_(new MobEffectInstance(MobEffects.f_19597_, 60, 1), (Entity)player);
            }
            if (!combo) continue;
            target2.getPersistentData().m_128473_(MARK_KEY);
            XueliWeaponCombat.halo(level, target2.m_20182_().m_82520_(0.0, 0.8, 0.0), 0.9, 16, false);
            level.m_8767_((ParticleOptions)ParticleTypes.f_123815_, target2.m_20185_(), target2.m_20186_() + 0.8, target2.m_20189_(), 8, 0.25, 0.3, 0.25, 0.04);
        }
        Vec3 trackStart = origin.m_82520_(0.0, -0.45, 0.0);
        Vec3 trackEnd = stop.m_82520_(0.0, -0.45, 0.0);
        WeaponFx.beam(level, trackStart.m_82549_(side.m_82490_(0.8)), trackEnd.m_82549_(side.m_82490_(0.8)), WeaponFxRules.Theme.ROSE);
        WeaponFx.beam(level, trackStart.m_82549_(side.m_82490_(-0.8)), trackEnd.m_82549_(side.m_82490_(-0.8)), WeaponFxRules.Theme.ROSE);
        XueliWeaponCombat.halo(level, stop.m_82520_(0.0, -0.4, 0.0), 0.8, 16, true);
        XueliWeaponCombat.sound(player, SoundEvents.f_12057_, 0.8f);
        XueliWeaponCombat.sound(player, SoundEvents.f_11668_, 1.5f);
        XueliWeaponCombat.durability(player, stack, 4);
        player.m_21011_(InteractionHand.MAIN_HAND, true);
    }

    public static void chargeEffect(ServerPlayer player, boolean full) {
        Vec3 center = player.m_20182_().m_82549_(XueliWeaponCombat.direction((Player)player).m_82490_(0.85)).m_82520_(0.0, 1.1, 0.0);
        XueliWeaponCombat.halo(player.m_284548_(), center, full ? 0.55 : 0.3, full ? 16 : 8, full);
        XueliWeaponCombat.sound(player, (SoundEvent)SoundEvents.f_12211_.m_203334_(), full ? 1.6f : 1.1f);
    }

    public static void guardEffect(ServerPlayer player) {
        Vec3 center = player.m_20182_().m_82549_(XueliWeaponCombat.direction((Player)player).m_82490_(0.8)).m_82520_(0.0, 1.0, 0.0);
        XueliWeaponCombat.halo(player.m_284548_(), center, 0.6, 12, false);
        XueliWeaponCombat.sound(player, SoundEvents.f_11677_, 1.3f);
        XueliWeaponCombat.durability(player, player.m_21205_(), 1);
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public static void guard(LivingHurtEvent event) {
        LivingEntity living;
        ServerPlayer player;
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer) || !(player = (ServerPlayer)livingEntity).m_6117_() || event.getAmount() <= 0.0f || player.m_7655_() != InteractionHand.MAIN_HAND || !player.m_21211_().m_150930_(XueliWeaponItems.skillet()) || event.getSource().m_269533_(DamageTypeTags.f_276146_)) {
            return;
        }
        Vec3 source = event.getSource().m_7270_();
        if (source == null || event.getSource().m_7640_() == null) {
            return;
        }
        Vec3 offset = source.m_82546_(player.m_20182_()).m_82542_(1.0, 0.0, 1.0);
        if (offset.m_82556_() < 1.0E-6 || offset.m_82541_().m_82526_(XueliWeaponCombat.direction((Player)player)) < 0.5) {
            return;
        }
        event.setAmount(event.getAmount() * 0.4f);
        ItemStack stack = player.m_21211_();
        player.m_5810_();
        XueliWeaponCombat.durability(player, stack, 2);
        Entity attacker = event.getSource().m_7639_();
        if (attacker instanceof LivingEntity && XueliWeaponCombat.canTarget(player, living = (LivingEntity)attacker) && player.m_20280_((Entity)living) <= 36.0) {
            XueliWeaponCombat.mark(player, living);
        }
        XueliWeaponCombat.halo(player.m_284548_(), player.m_20182_().m_82549_(XueliWeaponCombat.direction((Player)player)).m_82520_(0.0, 1.0, 0.0), 0.8, 20, true);
        XueliWeaponCombat.sound(player, SoundEvents.f_12346_, 1.35f);
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void drops(LivingDropsEvent event) {
    }

    private static /* synthetic */ boolean lambda$drops$4(ItemStack reward, ItemEntity drop) {
        return drop.m_32055_().m_150930_(reward.m_41720_());
    }
}

