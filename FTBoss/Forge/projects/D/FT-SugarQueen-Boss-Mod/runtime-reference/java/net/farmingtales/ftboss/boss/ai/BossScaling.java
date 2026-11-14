/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.boss.NonManorFinaleDifficulty;
import net.farmingtales.ftboss.boss.ai.ButcherPhases;
import net.farmingtales.ftboss.boss.ai.ManorHealthBalance;
import net.farmingtales.ftboss.boss.ai.ManorMultiplayerBalance;
import net.farmingtales.ftboss.boss.ai.ManorRaidCombat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public final class BossScaling {
    public static final double RADIUS = 48.0;
    public static final double HP_PER_PLAYER = 1.25;
    public static final double DMG_PER_PLAYER = 0.15;
    private static final String K_BASE_HP = "ftboss_base_hp";
    private static final String K_BASE_ATK = "ftboss_base_atk";
    private static final String K_DMG_MUL = "ftboss_dmg_mul";
    private static final String K_FINALE_HP_FIXED = "ftboss_finale_hp_fixed";

    private BossScaling() {
    }

    public static int playersInFight(LivingEntity boss) {
        Level level = boss.m_9236_();
        if (!(level instanceof ServerLevel)) {
            return 1;
        }
        ServerLevel sl = (ServerLevel)level;
        int n = 0;
        for (ServerPlayer p : sl.m_45976_(ServerPlayer.class, boss.m_20191_().m_82400_(48.0))) {
            if (!p.m_6084_() || p.m_7500_() || p.m_5833_()) continue;
            ++n;
        }
        return Math.max(1, n);
    }

    public static void tick(LivingEntity livingEntity) {
        AttributeInstance atk;
        LivingEntity boss;
        ManorHealthBalance.normalize(livingEntity);
        if (ManorMultiplayerBalance.tick(livingEntity)) {
            return;
        }
        if (boss == null || boss.m_9236_().f_46443_ || boss.m_21224_()) {
            return;
        }
        CompoundTag data = boss.getPersistentData();
        boolean finale = NonManorFinaleDifficulty.isFinale(boss);
        boolean hadBaseHealth = data.m_128441_(K_BASE_HP);
        if (!hadBaseHealth) {
            AttributeInstance hp0 = boss.m_21051_(Attributes.f_22276_);
            data.m_128347_(K_BASE_HP, hp0 != null ? hp0.m_22115_() : (double)boss.m_21233_());
            AttributeInstance atk0 = boss.m_21051_(Attributes.f_22281_);
            data.m_128347_(K_BASE_ATK, atk0 != null ? atk0.m_22115_() : 0.0);
        }
        if (finale && !data.m_128471_(K_FINALE_HP_FIXED)) {
            AttributeInstance maximumHealth = boss.m_21051_(Attributes.f_22276_);
            if (maximumHealth != null) {
                data.m_128347_(K_BASE_HP, maximumHealth.m_22115_());
            }
            data.m_128379_(K_FINALE_HP_FIXED, true);
        }
        int n = BossScaling.playersInFight(boss);
        double hpMul = Math.pow(1.25, n - 1);
        double dmgMul = 1.0 + 0.15 * (double)(n - 1);
        data.m_128347_(K_DMG_MUL, dmgMul);
        AttributeInstance maxHp = boss.m_21051_(Attributes.f_22276_);
        if (maxHp != null) {
            double base = data.m_128459_(K_BASE_HP);
            double want = ManorRaidCombat.scaleMaximumHealth(base * hpMul, boss);
            if (Math.abs(maxHp.m_22115_() - want) > 0.01) {
                float ratio = boss.m_21233_() > 0.0f ? boss.m_21223_() / boss.m_21233_() : 1.0f;
                maxHp.m_22100_(want);
                LivingEntity livingEntity2 = boss;
                livingEntity2.m_21153_(ButcherPhases.adjustScaledHealth(livingEntity2, (float)Math.max(1.0, Math.min(want, (double)ratio * want))));
            }
        }
        if ((atk = boss.m_21051_(Attributes.f_22281_)) != null) {
            double base = data.m_128459_(K_BASE_ATK);
            double want = base * dmgMul;
            if (Math.abs(atk.m_22115_() - want) > 0.01) {
                atk.m_22100_(want);
            }
        }
    }

    public static float dmg(LivingEntity boss, float base) {
        if (boss == null) {
            return base;
        }
        double m = boss.getPersistentData().m_128459_(K_DMG_MUL);
        return (float)((double)base * (m <= 0.0 ? 1.0 : m));
    }

    public static double dmgMul(LivingEntity boss) {
        double m = boss.getPersistentData().m_128459_(K_DMG_MUL);
        return m <= 0.0 ? 1.0 : m;
    }
}

