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
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.PacketDistributor
 */
package net.farmingtales.ftboss.balance;

import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.balance.ManorBalanceRules;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.weapons.ShadowSwordItem;
import net.farmingtales.ftboss.skill.FTNetwork;
import net.farmingtales.ftboss.skill.ShadowSigilPacket;
import net.farmingtales.ftboss.skill.WeaponFx;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.farmingtales.ftboss.zheergenfx.LightHooks;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public final class ManorShadowBlink {
    private static final Map<EasterBossEntity, Long> LAST_BLINK = new WeakHashMap<EasterBossEntity, Long>();

    private ManorShadowBlink() {
    }

    public static void tick(EasterBossEntity boss) {
        ServerPlayer target;
        ServerLevel level;
        block9: {
            block8: {
                Level level2 = boss.m_9236_();
                if (!(level2 instanceof ServerLevel)) break block8;
                level = (ServerLevel)level2;
                level2 = boss.m_5448_();
                if (level2 instanceof ServerPlayer && ManorBalanceHooks.validPlayer(boss, target = (ServerPlayer)level2) && boss.canTeleportWith((LivingEntity)target)) break block9;
            }
            return;
        }
        long now = level.m_46467_();
        if (!ManorBalanceRules.blinkReady(now, LAST_BLINK.getOrDefault(boss, Long.MIN_VALUE))) {
            return;
        }
        double distance = boss.m_20280_((Entity)target);
        if (distance < 25.0 || distance > 1024.0) {
            return;
        }
        Vec3 behind = target.m_20154_().m_82542_(-1.0, 0.0, -1.0).m_82541_().m_82490_(2.2);
        if (behind.m_82556_() < 0.001) {
            behind = new Vec3(0.0, 0.0, -2.2);
        }
        if (LightHooks.teleport(boss, target.m_20185_() + behind.f_82479_, target.m_20186_(), target.m_20189_() + behind.f_82481_, true)) {
            LAST_BLINK.put(boss, now);
            boss.m_21563_().m_24960_((Entity)target, 60.0f, 60.0f);
        } else {
            LAST_BLINK.put(boss, now - 30L + 10L);
        }
    }

    public static boolean landed(boolean moved, EasterBossEntity boss) {
        Level level;
        if (!(moved && boss.getVariant() == 7 && ManorBalanceHooks.mainBoss(boss) && boss.m_6084_() && (level = boss.m_9236_()) instanceof ServerLevel)) {
            return moved;
        }
        ServerLevel level2 = (ServerLevel)level;
        LAST_BLINK.put(boss, level2.m_46467_());
        Vec3 center = boss.m_20182_();
        for (ServerPlayer player : level2.m_6907_()) {
            double deltaZ;
            double deltaX;
            if (!ManorBalanceHooks.validPlayer(boss, player) || !boss.canTeleportWith((LivingEntity)player) || (deltaX = player.m_20185_() - center.f_82479_) * deltaX + (deltaZ = player.m_20189_() - center.f_82481_) * deltaZ > 9.0 || Math.abs(player.m_20186_() - center.f_82480_) > 2.5) continue;
            player.m_6469_(level2.m_269111_().m_269104_((Entity)boss, (Entity)boss), ShadowSwordItem.skyDamage());
            player.m_7292_(new MobEffectInstance(MobEffects.f_19610_, 120, 0));
            Vec3 away = new Vec3(deltaX, 0.0, deltaZ);
            away = away.m_82556_() < 1.0E-4 ? boss.m_20154_().m_82542_(1.0, 0.0, 1.0).m_82541_() : away.m_82541_();
            player.m_5997_(away.f_82479_ * 1.4, 0.35, away.f_82481_ * 1.4);
            player.f_19864_ = true;
        }
        ShadowSigilPacket packet = new ShadowSigilPacket(center.f_82479_, center.f_82480_, center.f_82481_, true);
        for (ServerPlayer viewer : level2.m_6907_()) {
            if (!(viewer.m_20238_(center) <= 4096.0)) continue;
            FTNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer), (Object)packet);
        }
        WeaponFx.burst(level2, center.m_82520_(0.0, 0.1, 0.0), 3.0, WeaponFxRules.Theme.SHADOW);
        level2.m_8767_((ParticleOptions)ParticleTypes.f_123765_, center.f_82479_, center.f_82480_ + 0.5, center.f_82481_, 10, 0.7, 0.7, 0.7, 0.08);
        level2.m_8767_((ParticleOptions)ParticleTypes.f_123789_, center.f_82479_, center.f_82480_ + 1.2, center.f_82481_, 12, 0.6, 0.6, 0.6, 0.12);
        level2.m_8767_((ParticleOptions)ParticleTypes.f_123796_, center.f_82479_, center.f_82480_ + 0.2, center.f_82481_, 6, 1.0, 1.0, 1.0, 0.06);
        level2.m_8767_((ParticleOptions)ParticleTypes.f_123747_, center.f_82479_, center.f_82480_ + 0.6, center.f_82481_, 1, 0.0, 0.0, 0.0, 0.0);
        level2.m_5594_(null, boss.m_20183_(), SoundEvents.f_11852_, SoundSource.HOSTILE, 1.0f, 0.9f);
        level2.m_5594_(null, boss.m_20183_(), SoundEvents.f_11668_, SoundSource.HOSTILE, 1.1f, 0.55f);
        level2.m_5594_(null, boss.m_20183_(), SoundEvents.f_11913_, SoundSource.HOSTILE, 0.7f, 1.35f);
        return moved;
    }

    public static void clear() {
        LAST_BLINK.clear();
    }
}

