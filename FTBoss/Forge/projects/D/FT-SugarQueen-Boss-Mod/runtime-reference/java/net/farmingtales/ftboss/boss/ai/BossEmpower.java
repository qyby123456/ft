/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.BossAggro;
import net.farmingtales.ftboss.boss.ai.BossRage;
import net.farmingtales.ftboss.boss.ai.RatSwarmEncounter;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.WineBarrelDecoyEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public final class BossEmpower {
    private static final UUID ID_SPEED = UUID.fromString("a1b2c3d4-0001-4a01-9001-000000000001");
    private static final UUID ID_ATTACK = UUID.fromString("a1b2c3d4-0002-4a01-9001-000000000002");
    private static final UUID ID_ARMOR = UUID.fromString("a1b2c3d4-0003-4a01-9001-000000000003");
    private static final UUID ID_KB = UUID.fromString("a1b2c3d4-0004-4a01-9001-000000000004");
    private static final UUID ID_RAGE_SPEED = UUID.fromString("a1b2c3d4-0005-4a01-9001-000000000005");
    private static final UUID ID_RAGE_ATK = UUID.fromString("a1b2c3d4-0006-4a01-9001-000000000006");
    private static final double SPEED_MUL = 0.25;
    private static final double ATTACK_MUL = 0.3;
    private static final double ARMOR_ADD = 4.0;
    private static final double KB_ADD = 0.0;
    private static final int EFFECT_TICKS = 520;

    private BossEmpower() {
    }

    public static boolean isEmpowered(LivingEntity e) {
        if (e == null) {
            return false;
        }
        try {
            EntityType t = e.m_6095_();
            return t == FTEntities.SUGAR_QUEEN.get() || t == FTEntities.MEAT_MOUNTAIN.get() || t == FTEntities.RAT_BOSS.get() || t == FTEntities.WINE_BARREL.get() || t == FTEntities.COMPOST_BOSS.get();
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    public static int spellBonusFor(Object caster) {
        LivingEntity le;
        return caster instanceof LivingEntity && BossEmpower.isEmpowered(le = (LivingEntity)caster) ? 1 : 0;
    }

    public static void tick(LivingEntity livingEntity) {
        WineBarrelDecoyEntity decoy;
        ServerLevel sl;
        int have;
        AttributeInstance spd;
        LivingEntity boss;
        if (RatSwarmEncounter.skipEmpower(livingEntity)) {
            return;
        }
        if (boss == null || boss.m_9236_().f_46443_ || !BossEmpower.isEmpowered(boss)) {
            return;
        }
        BossEmpower.mod(boss, Attributes.f_22279_, ID_SPEED, "ftboss_empower_speed", 0.25, AttributeModifier.Operation.MULTIPLY_TOTAL);
        BossEmpower.mod(boss, Attributes.f_22281_, ID_ATTACK, "ftboss_empower_attack", 0.3, AttributeModifier.Operation.MULTIPLY_TOTAL);
        BossEmpower.mod(boss, Attributes.f_22284_, ID_ARMOR, "ftboss_empower_armor", 4.0, AttributeModifier.Operation.ADDITION);
        BossEmpower.mod(boss, Attributes.f_22278_, ID_KB, "ftboss_empower_kb", 0.0, AttributeModifier.Operation.ADDITION);
        if (boss.m_6095_() == FTEntities.MEAT_MOUNTAIN.get()) {
            Level level;
            LivingEntity cur;
            boolean raging = BossRage.tierOf(boss) >= 1;
            spd = boss.m_21051_(Attributes.f_22279_);
            AttributeInstance atk = boss.m_21051_(Attributes.f_22281_);
            if (raging) {
                if (spd != null && spd.m_22111_(ID_RAGE_SPEED) == null) {
                    spd.m_22118_(new AttributeModifier(ID_RAGE_SPEED, "ftboss_rage_speed", 0.5, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                if (atk != null && atk.m_22111_(ID_RAGE_ATK) == null) {
                    atk.m_22118_(new AttributeModifier(ID_RAGE_ATK, "ftboss_rage_atk", 2.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
                if (boss.f_19797_ % 40 < 20) {
                    boss.m_7292_(new MobEffectInstance(MobEffects.f_19596_, 100, 1, false, false, true));
                }
            }
            if (boss instanceof Mob) {
                Mob m0 = (Mob)boss;
                v0 = m0.m_5448_();
            } else {
                v0 = cur = null;
            }
            if ((cur == null || !cur.m_6084_()) && (level = boss.m_9236_()) instanceof ServerLevel) {
                ServerLevel sl2 = (ServerLevel)level;
                LivingEntity best = null;
                double bd = 1024.0;
                for (LivingEntity le : sl2.m_45976_(LivingEntity.class, boss.m_20191_().m_82400_(32.0))) {
                    double d;
                    if (le == boss || !le.m_6084_() || BossAggro.isBossSide((Entity)le) || !((d = boss.m_20280_((Entity)le)) < bd)) continue;
                    bd = d;
                    best = le;
                }
                if (best != null && boss instanceof Mob) {
                    Mob m1 = (Mob)boss;
                    m1.m_6710_(best);
                }
            }
        }
        if (boss.m_6095_() == FTEntities.WINE_BARREL.get() && boss.f_19797_ % 600 < 20 && boss.m_21223_() < boss.m_21233_() * 0.5f && (spd = boss.m_9236_()) instanceof ServerLevel && (have = (sl = (ServerLevel)spd).m_45976_(WineBarrelDecoyEntity.class, boss.m_20191_().m_82400_(24.0)).size()) < 2 && (decoy = (WineBarrelDecoyEntity)((EntityType)FTEntities.WINE_BARREL_DECOY.get()).m_20615_((Level)sl)) != null) {
            decoy.m_21557_(true);
            decoy.m_21153_(1.0f);
            decoy.m_6593_((Component)Component.m_237113_((String)"\u00a77\u9152\u6876\u738b\u7684\u5206\u8eab"));
            decoy.m_20340_(true);
            decoy.m_21530_();
            decoy.m_7678_(boss.m_20185_() + (boss.m_217043_().m_188500_() - 0.5) * 4.0, boss.m_20186_(), boss.m_20189_() + (boss.m_217043_().m_188500_() - 0.5) * 4.0, 0.0f, 0.0f);
            ManorFightMembers.inherit((Entity)boss, (Entity)decoy);
            sl.m_7967_((Entity)decoy);
            FTBossMod.LOGGER.info("[FTBoss][\u9152\u6876\u738b] \u5206\u8eab\u51fa\u73b0(1 \u8840, \u4e0d\u653b\u51fb)");
        }
        if (boss.f_19797_ % 400 < 20) {
            boss.m_7292_(new MobEffectInstance(MobEffects.f_19596_, 520, 0, false, false, true));
            boss.m_7292_(new MobEffectInstance(MobEffects.f_19606_, 520, 0, false, false, true));
            boss.m_7292_(new MobEffectInstance(MobEffects.f_19607_, 520, 0, false, false, true));
            boss.m_7292_(new MobEffectInstance(MobEffects.f_19600_, 520, 1, false, false, true));
        }
    }

    private static void mod(LivingEntity e, Attribute attr, UUID id, String name, double amount, AttributeModifier.Operation op) {
        AttributeInstance inst = e.m_21051_(attr);
        if (inst == null) {
            return;
        }
        if (inst.m_22111_(id) == null) {
            inst.m_22118_(new AttributeModifier(id, name, amount, op));
        }
    }
}

