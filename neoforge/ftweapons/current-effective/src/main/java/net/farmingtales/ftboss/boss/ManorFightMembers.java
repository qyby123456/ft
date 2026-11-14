package net.farmingtales.ftboss.boss;

import io.redspace.ironsspellbooks.api.events.SetSummonOwnerEvent;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ai.LemonSacrifice;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(
    modid = "ftboss"
)
public final class ManorFightMembers {
    private static final String FIGHT = "ftboss_manor_fight";
    private static final String OWNER = "ftboss_manor_owner";

    private ManorFightMembers() {
    }

    public static UUID fightId(Entity entity) {
        return entity != null && entity.getPersistentData().hasUUID("ftboss_manor_fight") ? entity.getPersistentData().getUUID("ftboss_manor_fight") : null;
    }

    public static UUID ownerId(Entity entity) {
        return entity != null && entity.getPersistentData().hasUUID("ftboss_manor_owner") ? entity.getPersistentData().getUUID("ftboss_manor_owner") : null;
    }

    public static void bind(Entity entity, UUID fight) {
        entity.getPersistentData().putUUID("ftboss_manor_fight", fight);
    }

    public static void inherit(Entity owner, Entity child) {
        UUID fight = fightId(owner);
        if (owner != null && child != null && child != owner) {
            LemonSacrifice.inheritLifetime(owner, child);
            UUID root = ownerId(owner);
            net.farmingtales.ftboss.item.FoodWeaponSourceLedger.nativeManorOwner(owner, child, root == null ? owner.getUUID() : root);
            if (fight != null) {
                bind(child, fight);
                if (child instanceof EasterBossEntity boss) {
                    boss.setChallengeDifficulty(owner instanceof EasterBossEntity parent ? parent.getChallengeDifficulty() : ManorFight.difficulty(fight));
                    boss.setManorSummonerId(owner instanceof EasterBossEntity parentx ? parentx.getManorSummonerId() : ManorFight.summoner(fight));
                }
            }
        }
    }

    public static Entity owner(Entity entity) {
        if (entity == null) {
            return null;
        } else if (entity instanceof Projectile projectile) {
            return projectile.getOwner();
        } else if (entity instanceof TraceableEntity traceable) {
            return traceable.getOwner();
        } else if (entity instanceof OwnableEntity ownable) {
            return ownable.getOwner();
        } else {
            UUID parent = ownerId(entity);
            return parent != null && entity.level() instanceof ServerLevel level ? level.getEntity(parent) : null;
        }
    }

    public static Entity resolve(Entity entity) {
        for (int depth = 0; entity != null && depth < 8; depth++) {
            if (fightId(entity) != null) {
                return entity;
            }

            Entity parent = owner(entity);
            if (parent == null || parent == entity) {
                break;
            }

            entity = parent;
        }

        return entity;
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            Entity entity = event.getEntity();
            Entity parent = owner(entity);
            if (parent != null) {
                inherit(resolve(parent), entity);
            }

            UUID fight = fightId(entity);
            ManorFightData data = ManorFightData.get(level.getServer());
            if (data.retired(fight) || data.retired(entity.getUUID())) {
                event.setCanceled(true);
            }
        }
    }

    public static final class IronSummons {
        @SubscribeEvent
        public static void onSummonOwner(SetSummonOwnerEvent event) {
            if (event.getSummon() != null && !event.getSummon().level().isClientSide) {
                ManorFightMembers.inherit(ManorFightMembers.resolve(event.getOwner()), event.getSummon());
            }
        }
    }
}
