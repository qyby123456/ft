package net.farmingtales.ftboss.boss;

import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class ManorFightMembers {
    private static final String FIGHT = "ftboss_manor_fight";
    private static final String OWNER = "ftboss_manor_owner";

    private ManorFightMembers() {}

    public static UUID fightId(Entity entity) {
        return entity != null && entity.getPersistentData().hasUUID(FIGHT) ? entity.getPersistentData().getUUID(FIGHT) : null;
    }

    public static UUID ownerId(Entity entity) {
        return entity != null && entity.getPersistentData().hasUUID(OWNER) ? entity.getPersistentData().getUUID(OWNER) : null;
    }

    public static void bind(Entity entity, UUID fight) {
        entity.getPersistentData().putUUID(FIGHT, fight);
    }

    public static void inherit(Entity owner, Entity child) {
        net.farmingtales.ftboss.boss.ai.LemonSummonLimit.inherit(owner, child);
        UUID fight = fightId(owner);
        if (owner == null || child == null || child == owner) return;
        net.farmingtales.ftboss.boss.ai.LemonSacrifice.inheritLifetime(owner, child);
        UUID root = ownerId(owner);
        child.getPersistentData().putUUID(OWNER, root == null ? owner.getUUID() : root);
        if (fight == null) return;
        bind(child, fight);
        if (child instanceof EasterBossEntity boss) {
            boss.setChallengeDifficulty(owner instanceof EasterBossEntity parent
                    ? parent.getChallengeDifficulty() : ManorFight.difficulty(fight));
            boss.setManorSummonerId(owner instanceof EasterBossEntity parent ? parent.getManorSummonerId() : ManorFight.summoner(fight));
        }
    }

    public static Entity owner(Entity entity) {
        if (entity == null) return null;
        if (entity instanceof Projectile projectile) return projectile.getOwner();
        if (entity instanceof net.minecraft.world.entity.TraceableEntity traceable) return traceable.getOwner();
        if (entity instanceof OwnableEntity ownable) return ownable.getOwner();
        UUID parent = ownerId(entity);
        return parent != null && entity.level() instanceof ServerLevel level ? level.getEntity(parent) : null;
    }

    public static Entity resolve(Entity entity) {
        for (int depth = 0; entity != null && depth < 8; depth++) {
            if (fightId(entity) != null) return entity;
            Entity parent = owner(entity);
            if (parent == null || parent == entity) break;
            entity = parent;
        }
        return entity;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Entity entity = event.getEntity();
        Entity parent = owner(entity);
        if (parent != null) inherit(resolve(parent), entity);
        UUID fight = fightId(entity);
        ManorFightData data = ManorFightData.get(level.getServer());
        if (data.retired(fight) || data.retired(entity.getUUID())) event.setCanceled(true);
    }

    public static final class IronSummons {
        @SubscribeEvent
        public static void onSummonOwner(io.redspace.ironsspellbooks.api.events.SetSummonOwnerEvent event) {
            if (event.getSummon() != null && !event.getSummon().level().isClientSide) inherit(resolve(event.getOwner()), event.getSummon());
        }
    }
}
