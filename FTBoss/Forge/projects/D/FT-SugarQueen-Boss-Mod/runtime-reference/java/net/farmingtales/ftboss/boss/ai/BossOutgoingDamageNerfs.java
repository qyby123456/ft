/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ManorMercy;
import net.farmingtales.ftboss.boss.ai.MinionBlessing;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class BossOutgoingDamageNerfs {
    public static final float ZHEERGEN_MULTIPLIER = 0.25f;
    public static final float BUTCHER_MULTIPLIER = 0.25f;
    public static final float DORA_MULTIPLIER = 1.5f;
    public static final float XUELI_MULTIPLIER = 1.5f;

    private BossOutgoingDamageNerfs() {
    }

    public static float zheergenDamage(float amount) {
        return amount * 0.25f;
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public static void onHurt(LivingHurtEvent event) {
        if (event.isCanceled()) {
            return;
        }
        float amount = event.getAmount();
        if (!Float.isFinite(amount) || amount <= 0.0f) {
            return;
        }
        try {
            float multiplier = BossOutgoingDamageNerfs.multiplier(BossOutgoingDamageNerfs.source(event));
            if (multiplier != 1.0f) {
                event.setAmount(amount * multiplier);
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static Entity source(LivingHurtEvent event) {
        Entity source = event.getSource().m_7639_();
        if (source == null) {
            source = event.getSource().m_7640_();
        }
        if (source == null) {
            source = ManorMercy.damageOwner();
        }
        return source;
    }

    private static float multiplier(Entity source) {
        Entity current = source;
        for (int depth = 0; current != null && depth < 8; ++depth) {
            Level level;
            EasterBossEntity boss;
            if (current instanceof EasterBossEntity && (boss = (EasterBossEntity)current).getVariant() == 7 && !boss.isMini()) {
                return 0.25f;
            }
            if (current instanceof MeatMountainEntity) {
                return 0.25f;
            }
            if (current instanceof EasterBossEntity) {
                EasterBossEntity manor = (EasterBossEntity)current;
                if (manor.getVariant() == 2) {
                    return 1.5f;
                }
                if (manor.getVariant() == 10) {
                    return 1.5f;
                }
            }
            Entity owner = null;
            UUID marked = MinionBlessing.ownerId(current);
            if (marked != null && (level = current.m_9236_()) instanceof ServerLevel) {
                ServerLevel level2 = (ServerLevel)level;
                owner = level2.m_8791_(marked);
            }
            if (owner == null || owner == current) {
                owner = ManorFightMembers.owner(current);
            }
            if (owner == null || owner == current) {
                try {
                    Entity found;
                    Object summoner = current.getClass().getMethod("getSummoner", new Class[0]).invoke(current, new Object[0]);
                    if (summoner instanceof Entity && (found = (Entity)summoner) != current) {
                        owner = found;
                    }
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
            if (owner == null || owner == current) break;
            current = owner;
        }
        return 1.0f;
    }
}

