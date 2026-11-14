/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.events.SetSummonOwnerEvent
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.OwnableEntity
 *  net.minecraft.world.entity.TraceableEntity
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.boss;

import io.redspace.ironsspellbooks.api.events.SetSummonOwnerEvent;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorDifficulty;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightData;
import net.farmingtales.ftboss.boss.ai.LemonSacrifice;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class ManorFightMembers {
    private static final String FIGHT = "ftboss_manor_fight";
    private static final String OWNER = "ftboss_manor_owner";

    private ManorFightMembers() {
    }

    public static UUID fightId(Entity entity) {
        return entity != null && entity.getPersistentData().m_128403_(FIGHT) ? entity.getPersistentData().m_128342_(FIGHT) : null;
    }

    public static UUID ownerId(Entity entity) {
        return entity != null && entity.getPersistentData().m_128403_(OWNER) ? entity.getPersistentData().m_128342_(OWNER) : null;
    }

    public static void bind(Entity entity, UUID fight) {
        entity.getPersistentData().m_128362_(FIGHT, fight);
    }

    public static void inherit(Entity owner, Entity child) {
        UUID fight = ManorFightMembers.fightId(owner);
        if (owner == null || child == null || child == owner) {
            return;
        }
        LemonSacrifice.inheritLifetime(owner, child);
        UUID root = ManorFightMembers.ownerId(owner);
        child.getPersistentData().m_128362_(OWNER, root == null ? owner.m_20148_() : root);
        if (fight == null) {
            return;
        }
        ManorFightMembers.bind(child, fight);
        if (child instanceof EasterBossEntity) {
            UUID uUID;
            ManorDifficulty manorDifficulty;
            EasterBossEntity parent;
            EasterBossEntity boss = (EasterBossEntity)child;
            if (owner instanceof EasterBossEntity) {
                parent = (EasterBossEntity)owner;
                manorDifficulty = parent.getChallengeDifficulty();
            } else {
                manorDifficulty = ManorFight.difficulty(fight);
            }
            boss.setChallengeDifficulty(manorDifficulty);
            if (owner instanceof EasterBossEntity) {
                parent = (EasterBossEntity)owner;
                uUID = parent.getManorSummonerId();
            } else {
                uUID = ManorFight.summoner(fight);
            }
            boss.setManorSummonerId(uUID);
        }
    }

    public static Entity owner(Entity entity) {
        Entity entity2;
        Level level;
        if (entity == null) {
            return null;
        }
        if (entity instanceof Projectile) {
            Projectile projectile = (Projectile)entity;
            return projectile.m_19749_();
        }
        if (entity instanceof TraceableEntity) {
            TraceableEntity traceable = (TraceableEntity)entity;
            return traceable.m_19749_();
        }
        if (entity instanceof OwnableEntity) {
            OwnableEntity ownable = (OwnableEntity)entity;
            return ownable.m_269323_();
        }
        UUID parent = ManorFightMembers.ownerId(entity);
        if (parent != null && (level = entity.m_9236_()) instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            entity2 = level2.m_8791_(parent);
        } else {
            entity2 = null;
        }
        return entity2;
    }

    public static Entity resolve(Entity entity) {
        for (int depth = 0; entity != null && depth < 8; ++depth) {
            if (ManorFightMembers.fightId(entity) != null) {
                return entity;
            }
            Entity parent = ManorFightMembers.owner(entity);
            if (parent == null || parent == entity) break;
            entity = parent;
        }
        return entity;
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onJoin(EntityJoinLevelEvent event) {
        Level level = event.getLevel();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        Entity entity = event.getEntity();
        Entity parent = ManorFightMembers.owner(entity);
        if (parent != null) {
            ManorFightMembers.inherit(ManorFightMembers.resolve(parent), entity);
        }
        UUID fight = ManorFightMembers.fightId(entity);
        ManorFightData data = ManorFightData.get(level2.m_7654_());
        if (data.retired(fight) || data.retired(entity.m_20148_())) {
            event.setCanceled(true);
        }
    }

    public static final class IronSummons {
        @SubscribeEvent
        public static void onSummonOwner(SetSummonOwnerEvent event) {
            if (event.getSummon() != null && !event.getSummon().m_9236_().f_46443_) {
                ManorFightMembers.inherit(ManorFightMembers.resolve(event.getOwner()), event.getSummon());
            }
        }
    }
}

