/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.OwnableEntity
 *  net.minecraft.world.entity.TamableAnimal
 *  net.minecraft.world.entity.animal.horse.AbstractHorse
 *  net.minecraft.world.entity.decoration.ArmorStand
 *  net.minecraft.world.entity.monster.Enemy
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.item.weapons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.util.PufferBatRules;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class PufferBatCombat {
    private static final Map<UUID, List<Cloud>> CLOUDS = new HashMap<UUID, List<Cloud>>();

    private PufferBatCombat() {
    }

    public static boolean canTarget(ServerPlayer player, LivingEntity target) {
        Mob mob;
        AbstractHorse horse;
        OwnableEntity owned;
        if (target == player || !target.m_6084_() || target.m_20147_() || target.m_5833_() || target instanceof ArmorStand || player.m_7307_((Entity)target) || target.m_7307_((Entity)player)) {
            return false;
        }
        if (target instanceof OwnableEntity && (owned = (OwnableEntity)target).m_21805_() != null) {
            return false;
        }
        if (target instanceof AbstractHorse && (horse = (AbstractHorse)target).m_30614_()) {
            return false;
        }
        if (target instanceof Player) {
            Player other = (Player)target;
            return !other.m_7500_() && player.f_8924_.m_129799_() && player.m_7099_(other);
        }
        return target instanceof Enemy || target instanceof EasterBossEntity || target instanceof Mob && ((mob = (Mob)target).m_5448_() == player || player.m_21214_() == target);
    }

    private static boolean tamedByAnyone(LivingEntity target) {
        TamableAnimal animal;
        return target instanceof TamableAnimal && (animal = (TamableAnimal)target).m_21824_();
    }

    private static boolean visible(ServerPlayer player, LivingEntity target) {
        Vec3 from = player.m_146892_();
        Vec3 to = target.m_20191_().m_82399_();
        return player.m_284548_().m_45547_(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player)).m_6662_() == HitResult.Type.MISS;
    }

    public static boolean cast(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        ServerLevel level = player.m_284548_();
        if (PufferBatCombat.activeClouds(player) >= 4) {
            return false;
        }
        Vec3 eye = player.m_146892_();
        Vec3 look = player.m_20154_();
        Vec3 reach = eye.m_82549_(look.m_82490_(6.0));
        Vec3 hit = level.m_45547_(new ClipContext(eye, reach, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player)).m_82450_();
        double[] point = PufferBatRules.castPoint(eye.f_82479_, eye.f_82480_, eye.f_82481_, hit.f_82479_, hit.f_82480_, hit.f_82481_);
        if (point == null) {
            return false;
        }
        long now = level.m_46467_();
        CLOUDS.computeIfAbsent(player.m_20148_(), key -> new ArrayList()).add(new Cloud(level, player.m_20148_(), point[0], point[1], point[2], now));
        Vec3 center = new Vec3(point[0], point[1] + 0.08, point[2]);
        WeaponFx.send(level, WeaponFxRules.Kind.PUFFER_BURST, WeaponFxRules.Theme.COMPOST, center, center, 4.5, player.m_146908_());
        WeaponFx.send(level, WeaponFxRules.Kind.PUFFER_CLOUD, WeaponFxRules.Theme.COMPOST, center, center, 4.5, player.m_146908_());
        level.m_6263_(null, point[0], point[1], point[2], SoundEvents.f_12290_, SoundSource.PLAYERS, 1.0f, 0.85f);
        level.m_6263_(null, point[0], point[1], point[2], SoundEvents.f_11773_, SoundSource.PLAYERS, 0.8f, 0.7f);
        stack.m_41622_(3, (LivingEntity)player, owner -> owner.m_21190_(hand));
        player.m_5661_((Component)Component.m_237110_((String)"message.ftboss.puffer_bat.cast", (Object[])new Object[]{4, 5, (int)PufferBatRules.totalDamage()}), true);
        FTBossMod.LOGGER.info("[FTBoss][\u6cb3\u8c5a\u68d2] {} \u653e\u51fa\u80c0\u6c14\u6bd2\u96fe: {} \u683c / {} \u79d2 / \u6bcf\u8df3 {} \u70b9", new Object[]{player.m_36316_().getName(), 4.5, 5, Float.valueOf(PufferBatRules.tickDamage())});
        return true;
    }

    public static boolean shockwave(ServerPlayer player, ItemStack stack, InteractionHand hand) {
        ServerLevel level = player.m_284548_();
        Vec3 center = player.m_20182_().m_82520_(0.0, 0.08, 0.0);
        double radius = 5.5;
        AABB bounds = player.m_20191_().m_82377_(radius, 3.0, radius);
        int hits = 0;
        int poisonedHits = 0;
        for (LivingEntity target : level.m_45976_(LivingEntity.class, bounds)) {
            if (PufferBatCombat.tamedByAnyone(target) || !PufferBatCombat.canTarget(player, target) || !PufferBatCombat.visible(player, target)) continue;
            Vec3 targetCenter = target.m_20191_().m_82399_();
            Vec3 offset = targetCenter.m_82546_(center);
            if (!PufferBatRules.inside(offset.f_82479_, offset.f_82480_, offset.f_82481_, radius)) continue;
            boolean poisoned = target.m_21023_(MobEffects.f_19614_);
            if (!target.m_6469_(level.m_269111_().m_269104_((Entity)player, (Entity)player), PufferBatRules.shockwaveDamage(poisoned))) continue;
            target.m_147207_(new MobEffectInstance(MobEffects.f_19614_, 120, 1, false, true, true), (Entity)player);
            Vec3 push = new Vec3(offset.f_82479_, 0.0, offset.f_82481_);
            if (push.m_82556_() < 1.0E-6) {
                push = player.m_20154_().m_82542_(1.0, 0.0, 1.0);
            }
            if (push.m_82556_() < 1.0E-6) {
                push = new Vec3(0.0, 0.0, 1.0);
            }
            push = push.m_82541_().m_82490_(1.15);
            target.m_5997_(push.f_82479_, 0.25, push.f_82481_);
            target.f_19864_ = true;
            ++hits;
            if (!poisoned) continue;
            ++poisonedHits;
        }
        WeaponFx.send(level, WeaponFxRules.Kind.PUFFER_SHOCKWAVE, WeaponFxRules.Theme.COMPOST, center, center, radius, player.m_146908_());
        level.m_5594_(null, player.m_20183_(), SoundEvents.f_12290_, SoundSource.PLAYERS, 1.15f, 0.62f);
        level.m_5594_(null, player.m_20183_(), SoundEvents.f_12518_, SoundSource.PLAYERS, 0.85f, 1.25f);
        stack.m_41622_(4, (LivingEntity)player, owner -> owner.m_21190_(hand));
        player.m_36399_(0.2f);
        player.m_5661_((Component)Component.m_237110_((String)"message.ftboss.puffer_bat.shockwave", (Object[])new Object[]{hits, poisonedHits}), true);
        FTBossMod.LOGGER.info("[FTBoss][\u6cb3\u8c5a\u68d2] {} \u53d1\u52a8\u6bd2\u523a\u81a8\u7206: \u547d\u4e2d {}\uff0c\u4e2d\u6bd2\u589e\u4f24 {}", new Object[]{player.m_36316_().getName(), hits, poisonedHits});
        return true;
    }

    private static int activeClouds(ServerPlayer player) {
        List<Cloud> mine = CLOUDS.get(player.m_20148_());
        return mine == null ? 0 : mine.size();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || CLOUDS.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<UUID, List<Cloud>>> owners = CLOUDS.entrySet().iterator();
        while (owners.hasNext()) {
            Map.Entry<UUID, List<Cloud>> entry = owners.next();
            List<Cloud> mine = entry.getValue();
            ServerPlayer owner = PufferBatCombat.findOwner(mine, entry.getKey());
            mine.removeIf(cloud -> PufferBatCombat.tick(cloud, owner));
            if (!mine.isEmpty()) continue;
            owners.remove();
        }
    }

    private static ServerPlayer findOwner(List<Cloud> clouds, UUID ownerId) {
        for (Cloud cloud : clouds) {
            ServerPlayer player = cloud.level.m_7654_().m_6846_().m_11259_(ownerId);
            if (player == null) continue;
            return player;
        }
        return null;
    }

    private static boolean tick(Cloud cloud, ServerPlayer owner) {
        boolean ownerHere;
        long now = cloud.level.m_46467_();
        if (cloud.expired(now)) {
            return true;
        }
        if (now >= cloud.nextVisual) {
            Vec3 center = new Vec3(cloud.x, cloud.y + 0.08, cloud.z);
            WeaponFx.send(cloud.level, WeaponFxRules.Kind.PUFFER_CLOUD, WeaponFxRules.Theme.COMPOST, center, center, 4.5, 0.0f);
            cloud.nextVisual = now + 20L;
        }
        if (now < cloud.nextDamage) {
            return false;
        }
        cloud.nextDamage = now + 20L;
        boolean bl = ownerHere = owner != null && owner.m_9236_() == cloud.level && owner.m_6084_() && !owner.m_5833_();
        if (!ownerHere) {
            return false;
        }
        AABB box = new AABB(cloud.x - 4.5, cloud.y - 3.0, cloud.z - 4.5, cloud.x + 4.5, cloud.y + 3.0, cloud.z + 4.5);
        int hits = 0;
        for (LivingEntity target : cloud.level.m_45976_(LivingEntity.class, box)) {
            if (PufferBatCombat.tamedByAnyone(target) || !PufferBatCombat.canTarget(owner, target)) continue;
            double dx = target.m_20185_() - cloud.x;
            double dz = target.m_20189_() - cloud.z;
            double dy = target.m_20186_() - cloud.y;
            if (!PufferBatRules.inside(dx, dy, dz, 4.5) || !target.m_6469_(cloud.level.m_269111_().m_269104_((Entity)owner, (Entity)owner), PufferBatRules.tickDamage())) continue;
            target.m_147207_(new MobEffectInstance(MobEffects.f_19614_, 200, 1, false, true, true), (Entity)owner);
            target.m_147207_(new MobEffectInstance(MobEffects.f_19597_, 120, 0, false, true, true), (Entity)owner);
            ++hits;
        }
        if (hits > 0) {
            cloud.level.m_6263_(null, cloud.x, cloud.y, cloud.z, SoundEvents.f_12295_, SoundSource.PLAYERS, 0.7f, 1.1f);
        }
        return false;
    }

    private static final class Cloud {
        final ServerLevel level;
        final UUID owner;
        final double x;
        final double y;
        final double z;
        final long born;
        long nextDamage;
        long nextVisual;

        Cloud(ServerLevel level, UUID owner, double x, double y, double z, long born) {
            this.level = level;
            this.owner = owner;
            this.x = x;
            this.y = y;
            this.z = z;
            this.born = born;
            this.nextDamage = born;
            this.nextVisual = born + 20L;
        }

        boolean expired(long now) {
            return now - this.born >= 100L;
        }
    }
}

