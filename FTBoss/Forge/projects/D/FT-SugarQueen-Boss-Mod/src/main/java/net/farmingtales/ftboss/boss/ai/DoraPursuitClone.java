package net.farmingtales.ftboss.boss.ai;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.util.DoraCloneRules;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class DoraPursuitClone {
    private static final String CLONE = "ftboss_dora_pursuit_clone";
    private static final String PARENT = "ftboss_dora_clone_parent";
    private static final String ACTIVE = "ftboss_dora_active_clone";
    private static final String POSITION = "ftboss_dora_clone_position";
    private static final String DEATH = "ftboss_dora_clone_death_tick";
    private static final String MISSING = "ftboss_dora_clone_missing_since";

    private DoraPursuitClone() {}

    public static boolean isClone(Entity entity) {
        return entity instanceof EasterBossEntity && entity.getPersistentData().getBoolean(CLONE);
    }

    public static boolean skipRecovery(EasterBossEntity boss) {
        return boss.isMini() || isClone(boss);
    }

    private static boolean ownerEligible(EasterBossEntity boss) {
        return boss.getVariant() == 2 && !boss.isMini() && !boss.isGiant() && !isClone(boss)
                && boss.isAlive() && !boss.isRemoved();
    }

    private static EasterBossEntity owner(ServerLevel level, EasterBossEntity clone) {
        var data = clone.getPersistentData();
        return data.hasUUID(PARENT) && level.getEntity(data.getUUID(PARENT)) instanceof EasterBossEntity boss
                && ownerEligible(boss) ? boss : null;
    }

    private static List<ServerPlayer> players(ServerLevel level, EasterBossEntity boss) {
        UUID fight = ManorFightMembers.fightId(boss);
        Vec3 anchor = ManorFight.anchorOf(fight);
        if (anchor == null) anchor = boss.position();
        Vec3 center = anchor;
        double range = ManorFight.ATTACH_RANGE;
        return level.players().stream().filter(BossTargets::valid)
                .filter(player -> player.distanceToSqr(center) <= range * range).toList();
    }

    private static ServerPlayer target(ServerLevel level, EasterBossEntity boss, EasterBossEntity clone) {
        List<ServerPlayer> players = players(level, boss);
        if (players.size() < 2) return null;
        Entity primary = boss.getTarget();
        if (!(primary instanceof ServerPlayer) || !players.contains(primary)) {
            primary = players.stream().min(Comparator.comparingDouble(boss::distanceToSqr)).orElse(null);
        }
        Entity excluded = primary;
        Entity current = clone.getTarget();
        if (current instanceof ServerPlayer player && current != excluded && players.contains(player)) return player;
        return players.stream().filter(player -> player != excluded)
                .min(Comparator.comparingDouble(clone::distanceToSqr)).orElse(null);
    }

    public static boolean redirectTarget(LivingChangeTargetEvent event) {
        if (!isClone(event.getEntity()) || !(event.getEntity().level() instanceof ServerLevel level)) return false;
        EasterBossEntity clone = (EasterBossEntity) event.getEntity();
        EasterBossEntity boss = owner(level, clone);
        event.setNewTarget(boss == null ? null : target(level, boss, clone));
        return true;
    }

    private static void fixedHealth(EasterBossEntity clone) {
        var maximum = clone.getAttribute(Attributes.MAX_HEALTH);
        if (maximum == null) return;
        maximum.removeModifiers();
        maximum.setBaseValue(DoraCloneRules.HEALTH);
        if (clone.getHealth() > DoraCloneRules.HEALTH) clone.setHealth(DoraCloneRules.HEALTH);
        BossBars.untrack(clone, clone.getBossBar());
    }

    private static void clearSlot(EasterBossEntity boss) {
        var data = boss.getPersistentData();
        data.remove(ACTIVE);
        data.remove(POSITION);
        data.remove(MISSING);
    }

    private static void retire(ServerLevel level, EasterBossEntity boss) {
        var data = boss.getPersistentData();
        if (data.hasUUID(ACTIVE) && level.getEntity(data.getUUID(ACTIVE)) instanceof EasterBossEntity clone
                && isClone(clone)) clone.discard();
        clearSlot(boss);
    }

    private static void tickOwner(ServerLevel level, EasterBossEntity boss) {
        var data = boss.getPersistentData();
        if (boss.isNoAi() || boss.isInvulnerable()) return;
        List<ServerPlayer> players = players(level, boss);
        if (data.hasUUID(ACTIVE)) {
            Entity active = level.getEntity(data.getUUID(ACTIVE));
            if (active == null && data.contains(POSITION)) {
                level.getChunkAt(BlockPos.of(data.getLong(POSITION)));
                active = level.getEntity(data.getUUID(ACTIVE));
            }
            if (active != null && active.isAlive() && !active.isRemoved() && isClone(active)) {
                data.putLong(POSITION, active.blockPosition().asLong());
                data.remove(MISSING);
                return;
            }
            long now = level.getGameTime();
            if (!data.contains(MISSING)) data.putLong(MISSING, now);
            if (now - data.getLong(MISSING) < 200) return;
            clearSlot(boss);
            data.putLong(DEATH, now);
        }
        long deathTick = data.contains(DEATH) ? data.getLong(DEATH) : -1;
        if (!DoraCloneRules.canSummon(players.size(), false, level.getGameTime(), deathTick)) return;
        EasterBossEntity clone = FTEntities.EASTER_BOSS.get().create(level);
        if (clone == null) return;
        clone.getPersistentData().putBoolean(CLONE, true);
        clone.getPersistentData().putUUID(PARENT, boss.getUUID());
        clone.setVariant(2);
        ManorFightMembers.inherit(boss, clone);
        clone.setChallengeDifficulty(boss.getChallengeDifficulty());
        clone.setCustomName(Component.literal("朵拉的追击分身"));
        clone.setPersistenceRequired();
        clone.setCanPickUpLoot(false);
        for (EquipmentSlot slot : EquipmentSlot.values()) clone.setDropChance(slot, 0.0F);
        clone.moveTo(boss.getX(), boss.getY(), boss.getZ(), boss.getYRot(), 0.0F);
        fixedHealth(clone);
        clone.setHealth(DoraCloneRules.HEALTH);
        data.putUUID(ACTIVE, clone.getUUID());
        data.putLong(POSITION, clone.blockPosition().asLong());
        if (!level.addFreshEntity(clone)) {
            clearSlot(boss);
            clone.discard();
            return;
        }
        fixedHealth(clone);
        clone.setTarget(target(level, boss, clone));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof EasterBossEntity entity) || !(entity.level() instanceof ServerLevel level)
                || !entity.isAlive() || entity.isRemoved()) return;
        if (!isClone(entity)) {
            if (ownerEligible(entity) && entity.tickCount % 20 == 0) tickOwner(level, entity);
            return;
        }
        fixedHealth(entity);
        EasterBossEntity boss = owner(level, entity);
        if (boss == null) {
            if (entity.getTarget() != null) entity.setTarget(null);
            entity.getNavigation().stop();
            if (entity.tickCount >= 200) entity.discard();
            return;
        }
        var data = boss.getPersistentData();
        if (!data.hasUUID(ACTIVE) || !entity.getUUID().equals(data.getUUID(ACTIVE))) {
            entity.discard();
            return;
        }
        ServerPlayer target = target(level, boss, entity);
        if (entity.getTarget() != target) entity.setTarget(target);
        if (target == null) entity.getNavigation().stop();
        else if (entity.tickCount % 10 == 0) entity.getNavigation().moveTo(target, 1.25);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJoin(EntityJoinLevelEvent event) {
        if (isClone(event.getEntity()) && event.getEntity() instanceof EasterBossEntity clone
                && !event.getLevel().isClientSide()) fixedHealth(clone);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof EasterBossEntity entity) || !(entity.level() instanceof ServerLevel level)) return;
        if (!isClone(entity)) {
            if (ownerEligible(entity) || entity.getPersistentData().hasUUID(ACTIVE)) retire(level, entity);
            return;
        }
        EasterBossEntity boss = owner(level, entity);
        if (boss == null || !boss.getPersistentData().hasUUID(ACTIVE)
                || !entity.getUUID().equals(boss.getPersistentData().getUUID(ACTIVE))) return;
        clearSlot(boss);
        boss.getPersistentData().putLong(DEATH, level.getGameTime());
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (!(event.getEntity() instanceof EasterBossEntity entity) || !(event.getLevel() instanceof ServerLevel level)) return;
        Entity.RemovalReason reason = entity.getRemovalReason();
        if (!isClone(entity) && reason != null && reason.shouldDestroy()) retire(level, entity);
        if (isClone(entity) && reason != null && reason.shouldDestroy()) {
            EasterBossEntity boss = owner(level, entity);
            if (boss != null && boss.getPersistentData().hasUUID(ACTIVE)
                    && entity.getUUID().equals(boss.getPersistentData().getUUID(ACTIVE))) {
                clearSlot(boss);
                boss.getPersistentData().putLong(DEATH, level.getGameTime());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDrops(LivingDropsEvent event) {
        if (isClone(event.getEntity())) event.getDrops().clear();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onExperience(LivingExperienceDropEvent event) {
        if (isClone(event.getEntity())) event.setDroppedExperience(0);
    }
}
