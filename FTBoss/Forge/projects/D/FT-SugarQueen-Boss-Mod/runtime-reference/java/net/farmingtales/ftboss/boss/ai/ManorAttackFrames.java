/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.entity.EntityStruckByLightningEvent
 *  net.minecraftforge.event.entity.living.LivingAttackEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ManorMercy;
import net.farmingtales.ftboss.boss.ai.MinionBlessing;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ManorAttackFrames {
    private ManorAttackFrames() {
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onAttack(LivingAttackEvent event) {
        block5: {
            block4: {
                LivingEntity livingEntity;
                if (event.isCanceled() || !Float.isFinite(event.getAmount()) || event.getAmount() <= 0.0f || !((livingEntity = event.getEntity()) instanceof Player)) break block4;
                Player player = (Player)livingEntity;
                if (!player.m_9236_().f_46443_ && player.m_6084_() && !player.m_7500_() && !player.m_5833_() && !player.m_6673_(event.getSource())) break block5;
            }
            return;
        }
        if (ManorAttackFrames.isManorAttack(event.getSource())) {
            player.f_19802_ = 0;
        }
    }

    public static boolean isManorAttack(DamageSource source) {
        Entity owner = source.m_7639_();
        if (owner == null) {
            owner = source.m_7640_();
        }
        if (owner == null) {
            owner = ManorMercy.damageOwner();
        }
        return ManorAttackFrames.isManorOwned(owner);
    }

    private static boolean isManorOwned(Entity entity) {
        for (int depth = 0; entity != null && depth < 8; ++depth) {
            Level level;
            if (entity instanceof EasterBossEntity) {
                return true;
            }
            Entity parent = ManorFightMembers.owner(entity);
            if (parent == null && (level = entity.m_9236_()) instanceof ServerLevel) {
                ServerLevel level2 = (ServerLevel)level;
                UUID owner = ManorFightMembers.ownerId(entity);
                if (owner == null) {
                    owner = MinionBlessing.ownerId(entity);
                }
                if (owner != null) {
                    parent = level2.m_8791_(owner);
                }
            }
            if (parent == null || parent == entity) {
                return false;
            }
            entity = parent;
        }
        return false;
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onLightning(EntityStruckByLightningEvent event) {
        ServerLevel level;
        Player player;
        block3: {
            block2: {
                Entity entity;
                if (event.isCanceled() || !((entity = event.getEntity()) instanceof Player) || !((entity = (player = (Player)entity).m_9236_()) instanceof ServerLevel)) break block2;
                level = (ServerLevel)entity;
                if (ManorAttackFrames.isManorOwned((Entity)event.getLightning())) break block3;
            }
            return;
        }
        event.setCanceled(true);
        ManorMercy.withOwner((Entity)event.getLightning(), () -> {
            player.m_8038_(level, event.getLightning());
            return true;
        });
    }
}

