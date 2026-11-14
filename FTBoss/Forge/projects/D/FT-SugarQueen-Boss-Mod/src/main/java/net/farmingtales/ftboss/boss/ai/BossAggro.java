package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BossAggro {
    public static final double MAX_DIST = 64.0;
    private static final Map<UUID, BossThreatTable> MEMORY = new HashMap<>();

    private BossAggro() {}

    public static void record(LivingEntity victim, Entity attacker, double damage, long gameTime) {
        if (!supportsThreat(victim) || !(attacker instanceof LivingEntity living)
                || !validTarget(victim, living) || !Double.isFinite(damage) || damage <= 0.0) return;
        add(victim, living, Math.min(damage, victim.getHealth()), gameTime);
    }

    public static void consume(LivingEntity user, ItemStack stack) {
        if (!(user.level() instanceof ServerLevel level) || !BossTargets.valid(user)) return;
        double amount = stack.getItem() instanceof PotionItem && !PotionUtils.getMobEffects(stack).isEmpty()
                ? BossThreatTable.POTION_THREAT : stack.isEdible() ? BossThreatTable.FOOD_THREAT : 0.0;
        if (amount <= 0.0) return;
        BossBars.forEach(level.getServer(), boss -> {
            if (!supportsThreat(boss) || !validTarget(boss, user)) return;
            Mob mob = (Mob) boss;
            LivingEntity threat = highest(boss);
            if (threat != null || validTarget(boss, mob.getTarget())) {
                add(boss, user, amount, boss.level().getGameTime());
            }
        });
    }

    private static void add(LivingEntity boss, LivingEntity target, double amount, long now) {
        MEMORY.computeIfAbsent(boss.getUUID(), ignored -> new BossThreatTable()).add(target.getUUID(), amount, now);
        apply((Mob) boss);
    }

    public static LivingEntity highest(LivingEntity boss) {
        if (!supportsThreat(boss)) return null;
        BossThreatTable table = MEMORY.get(boss.getUUID());
        if (table == null) return null;
        ServerLevel level = (ServerLevel) boss.level();
        LivingEntity current = ((Mob) boss).getTarget();
        UUID target = table.highest(level.getGameTime(), current == null ? null : current.getUUID(),
                id -> level.getEntity(id) instanceof LivingEntity living && validTarget(boss, living));
        if (table.isEmpty()) MEMORY.remove(boss.getUUID());
        return target != null && level.getEntity(target) instanceof LivingEntity living ? living : null;
    }

    public static void tick(MinecraftServer server) {
        if (server == null || MEMORY.isEmpty()) return;
        for (UUID bossId : new ArrayList<>(MEMORY.keySet())) {
            LivingEntity boss = find(server, bossId);
            if (!supportsThreat(boss)) forget(bossId);
            else apply((Mob) boss);
        }
    }

    private static void apply(Mob boss) {
        BossThreatTable table = MEMORY.get(boss.getUUID());
        LivingEntity previous = boss.getTarget();
        boolean tracked = table != null && previous != null && table.contains(previous.getUUID());
        LivingEntity target = highest(boss);
        if (boss.isNoAi()) return;
        if (target != null && target != previous) {
            boss.setTarget(target);
            boss.getNavigation().stop();
        } else if (target == null && (tracked || previous != null && !validTarget(boss, previous))) {
            boss.setTarget(null);
            boss.getNavigation().stop();
        }
    }

    private static boolean supportsThreat(LivingEntity boss) {
        return boss instanceof Mob && boss.level() instanceof ServerLevel && BossTargets.valid(boss)
                && isBossSide(boss) && !(boss instanceof EasterBossEntity easter
                && easter.getVariant() == EasterBossEntity.SPARROW_VARIANT);
    }

    private static boolean validTarget(LivingEntity boss, LivingEntity target) {
        return BossTargets.valid(target) && target != boss && target.level() == boss.level()
                && !isBossSide(target) && boss.distanceToSqr(target) <= MAX_DIST * MAX_DIST;
    }

    public static void forget(UUID bossId) {
        MEMORY.remove(bossId);
    }

    public static void forgetEntity(UUID entityId) {
        forget(entityId);
        MEMORY.values().forEach(table -> table.remove(entityId));
    }

    public static void clear() {
        MEMORY.clear();
    }

    private static LivingEntity find(MinecraftServer server, UUID id) {
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(id) instanceof LivingEntity living) return living;
        }
        return null;
    }

    public static boolean isBossSide(Entity entity) {
        if (entity == null) return false;
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return key != null && "ftboss".equals(key.getNamespace());
    }
}
