/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.event.server.ServerStoppedEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.item.weapons;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.effect.ArmorShredEffect;
import net.farmingtales.ftboss.effect.BloodRageFakeEffect;
import net.farmingtales.ftboss.effect.FTEffects;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.item.weapons.BlackCleaverItem;
import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.skill.XuanlanCombat;
import net.farmingtales.ftboss.util.CleaverSlamFx;
import net.farmingtales.ftboss.util.ManorWeaponRules;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class CleaverCombat {
    private static final Map<UUID, Cast> CASTS = new HashMap<UUID, Cast>();

    private CleaverCombat() {
    }

    public static InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack weapon = player.m_21120_(hand);
        if (hand != InteractionHand.MAIN_HAND || player.m_5833_() || player.m_36335_().m_41519_(weapon.m_41720_())) {
            return InteractionResultHolder.m_19100_((Object)weapon);
        }
        if (!(player instanceof ServerPlayer)) {
            return InteractionResultHolder.m_19090_((Object)weapon);
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        if (CASTS.containsKey(player.m_20148_())) {
            return InteractionResultHolder.m_19100_((Object)weapon);
        }
        MobEffectInstance rage = player.m_21124_((MobEffect)FTEffects.BLOOD_RAGE_FAKE.get());
        int stacks = rage == null ? 0 : rage.m_19564_() + 1;
        Vec3 direction = Vec3.m_82498_((float)0.0f, (float)player.m_146908_());
        CASTS.put(player.m_20148_(), new Cast(serverPlayer.m_284548_(), weapon, player.m_20182_(), direction, level.m_46467_() + 9L, stacks));
        player.m_36335_().m_41524_(weapon.m_41720_(), 80);
        ((BlackCleaverItem)weapon.m_41720_()).triggerCleaverAnim((Entity)player, weapon, "slam");
        level.m_5594_(null, player.m_20183_(), SoundEvents.f_12201_, SoundSource.PLAYERS, 0.7f, 0.55f);
        return InteractionResultHolder.m_19096_((Object)weapon);
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        Player player;
        if (event.phase != TickEvent.Phase.END || !((player = event.player) instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        Cast cast = CASTS.get(player2.m_20148_());
        if (cast == null) {
            return;
        }
        if (!player2.m_6084_() || player2.m_5833_() || player2.m_284548_() != cast.level() || player2.m_21205_() != cast.weapon() || !cast.weapon().m_150930_((Item)FTBossItems.BLACK_CLEAVER.get()) || player2.m_20182_().m_82557_(cast.origin()) > 16.0) {
            CASTS.remove(player2.m_20148_());
            return;
        }
        long remaining = cast.impact() - cast.level().m_46467_();
        if (remaining > 0L) {
            if (remaining % 2L == 0L) {
                cast.level().m_8767_((ParticleOptions)ParticleTypes.f_123784_, player2.m_20185_() + cast.direction().f_82479_, player2.m_20186_() + 1.2, player2.m_20189_() + cast.direction().f_82481_, 3, 0.15, 0.2, 0.15, 0.01);
            }
            return;
        }
        CASTS.remove(player2.m_20148_());
        CleaverCombat.impact(player2, cast);
    }

    private static void impact(ServerPlayer player, Cast cast) {
        Vec3 start = player.m_20182_().m_82520_(0.0, 0.6, 0.0);
        Vec3 center = XuanlanCombat.clip(cast.level(), (Entity)player, start, start.m_82549_(cast.direction().m_82490_(1.8)));
        double radius = ManorWeaponRules.cleaverRadius(cast.rage());
        int hits = 0;
        int stacksBefore = cast.rage();
        List targets = cast.level().m_6443_(LivingEntity.class, new AABB(center, center).m_82377_(radius, 2.5, radius), target -> XuanlanCombat.canHit((Player)player, (Entity)target));
        targets.sort(Comparator.comparingDouble(target -> target.m_20238_(center)));
        for (LivingEntity target2 : targets) {
            Vec3 delta = target2.m_20182_().m_82546_(center);
            if (!ManorWeaponRules.insideSlam(delta.f_82479_, delta.f_82480_, delta.f_82481_, cast.rage()) || !player.m_142582_((Entity)target2)) continue;
            target2.f_19802_ = 0;
            float hpBefore = target2.m_21223_();
            boolean dealt = target2.m_6469_(cast.level().m_269111_().m_269104_((Entity)player, (Entity)player), ManorWeaponRules.cleaverDamage(cast.rage()));
            float real = hpBefore - target2.m_21223_();
            if (!dealt && real <= 0.0f) continue;
            ArmorShredEffect.onHit(target2);
            target2.m_7292_(new MobEffectInstance(MobEffects.f_19597_, 40, cast.rage() >= 5 ? 2 : 0));
            target2.m_147240_(cast.rage() >= 5 ? 0.9 : 0.6, -delta.f_82479_, -delta.f_82481_);
            if (++hits < 16) continue;
            break;
        }
        if (hits > 0) {
            if (cast.rage() >= 5) {
                player.m_21195_((MobEffect)FTEffects.BLOOD_RAGE_FAKE.get());
            } else {
                BloodRageFakeEffect.onHit((LivingEntity)player);
            }
            MobEffectInstance inst = player.m_21124_((MobEffect)FTEffects.BLOOD_RAGE_FAKE.get());
            int stacksAfter = inst == null ? 0 : inst.m_19564_() + 1;
            FTBossMod.LOGGER.info("[FTBoss][\u9ed1\u5207] {} \u88c2\u5730\u65a9\u547d\u4e2d {} \u4e2a\u76ee\u6807 -> \u8840\u6012\u00b7\u4f2a {} \u5c42 -> {} \u5c42{}", new Object[]{player.m_7755_().getString(), hits, stacksBefore, stacksAfter, stacksBefore >= 5 ? "\uff08\u6ee1\u5c42\u5df2\u6d88\u8017\uff09" : ""});
        }
        CleaverSlamFx.play((Level)cast.level(), center.f_82479_, player.m_20186_(), center.f_82481_);
        WeaponFx.ring(cast.level(), center, radius, WeaponFxRules.Theme.BLOOD);
        cast.weapon().m_41622_(2, (LivingEntity)player, holder -> holder.m_21190_(InteractionHand.MAIN_HAND));
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        CASTS.remove(event.getEntity().m_20148_());
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        CASTS.clear();
    }

    private record Cast(ServerLevel level, ItemStack weapon, Vec3 origin, Vec3 direction, long impact, int rage) {
    }
}

