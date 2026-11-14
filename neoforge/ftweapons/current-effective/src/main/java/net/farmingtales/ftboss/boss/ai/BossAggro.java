package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;

public final class BossAggro {
    public static final double MAX_DIST = 64.0;
    private static final Map<UUID, BossThreatTable> MEMORY = new HashMap<>();

    private BossAggro() {
    }

    public static void record(LivingEntity var0, Entity var1, double var2, long var4) {
        if (supportsThreat(var0) && var1 instanceof LivingEntity var6 && validTarget(var0, var6) && Double.isFinite(var2) && !(var2 <= 0.0)) {
            add(var0, var6, Math.min(var2, (double)var0.getHealth()), var4);
            return;
        }
    }

    public static void consume(LivingEntity var0, ItemStack var1) {
        if (var0.level() instanceof ServerLevel var2 && BossTargets.valid(var0)) {
            PotionContents var6 = var1.get(DataComponents.POTION_CONTENTS);
            double var4 = var1.getItem() instanceof PotionItem && var6 != null && var6.hasEffects() ? 8.0 : (var1.has(DataComponents.FOOD) ? 5.0 : 0.0);
            if (var4 <= 0.0) {
                return;
            }

            BossBars.forEach(var2.getServer(), var3 -> {
                if (supportsThreat(var3) && validTarget(var3, var0)) {
                    Mob var4x = (Mob)var3;
                    LivingEntity var5 = highest(var3);
                    if (var5 != null || validTarget(var3, var4x.getTarget())) {
                        add(var3, var0, var4, var3.level().getGameTime());
                    }
                }
            });
            return;
        }
    }

    private static void add(LivingEntity var0, LivingEntity var1, double var2, long var4) {
        MEMORY.computeIfAbsent(var0.getUUID(), var0x -> new BossThreatTable()).add(var1.getUUID(), var2, var4);
        apply((Mob)var0);
    }

    public static LivingEntity highest(LivingEntity boss) {
        if (!supportsThreat(boss)) {
            return null;
        } else {
            BossThreatTable table = MEMORY.get(boss.getUUID());
            if (table == null) {
                return null;
            } else {
                ServerLevel level = (ServerLevel)boss.level();
                LivingEntity current = ((Mob)boss).getTarget();
                UUID target = ManorCombatLanes.selectThreatId(table, level.getGameTime(), current == null ? null : current.getUUID(), id -> {
                    if (level.getEntity(id) instanceof LivingEntity livingx && validTarget(boss, livingx)) {
                        return true;
                    }

                    return false;
                }, boss);
                if (table.isEmpty()) {
                    MEMORY.remove(boss.getUUID());
                }

                return target != null && level.getEntity(target) instanceof LivingEntity living ? living : null;
            }
        }
    }

    public static void tick(MinecraftServer var0) {
        if (var0 != null && !MEMORY.isEmpty()) {
            for (UUID var2 : new ArrayList<>(MEMORY.keySet())) {
                LivingEntity var3 = find(var0, var2);
                if (!supportsThreat(var3)) {
                    forget(var2);
                } else {
                    apply((Mob)var3);
                }
            }
        }
    }

    private static void apply(Mob boss) {
        if (!ManorFinaleCombat.keepCommittedTarget(boss)) {
            BossThreatTable table = MEMORY.get(boss.getUUID());
            LivingEntity previous = boss.getTarget();
            boolean tracked = table != null && previous != null && ManorTargetPolicy.shouldClearTracked(table.contains(previous.getUUID()), boss, previous);
            LivingEntity target = highest(boss);
            if (!boss.isNoAi()) {
                if (target != null && target != previous) {
                    boss.setTarget(target);
                    boss.getNavigation().stop();
                } else if (target == null && (tracked || previous != null && !validTarget(boss, previous))) {
                    boss.setTarget(null);
                    boss.getNavigation().stop();
                }
            }
        }
    }

    private static boolean supportsThreat(LivingEntity var0) {
        return var0 instanceof Mob
            && var0.level() instanceof ServerLevel
            && BossTargets.valid(var0)
            && isBossSide(var0)
            && (!(var0 instanceof EasterBossEntity var1) || var1.getVariant() != 9);
    }

    private static boolean validTarget(LivingEntity boss, LivingEntity target) {
        if (ManorFinaleCombat.manages(boss)) {
            return ManorTargetPolicy.filterThreat(ManorFinaleCombat.validTarget(boss, target), boss, target);
        } else {
            return ManorMultiplayerBalance.validManorTarget(boss, target)
                ? ManorTargetPolicy.filterThreat(true, boss, target)
                : ManorTargetPolicy.filterThreat(
                    BossTargets.valid(target)
                        && target != boss
                        && target.level() == boss.level()
                        && !isBossSide(target)
                        && boss.distanceToSqr(target) <= 4096.0,
                    boss,
                    target
                );
        }
    }

    public static void forget(UUID var0) {
        MEMORY.remove(var0);
    }

    public static void forgetEntity(UUID var0) {
        forget(var0);
        MEMORY.values().forEach(var1 -> var1.remove(var0));
    }

    public static void clear() {
        MEMORY.clear();
    }

    private static LivingEntity find(MinecraftServer var0, UUID var1) {
        for (ServerLevel var3 : var0.getAllLevels()) {
            Entity var5 = var3.getEntity(var1);
            if (var5 instanceof LivingEntity) {
                return (LivingEntity)var5;
            }
        }

        return null;
    }

    public static boolean isBossSide(Entity var0) {
        if (var0 == null) {
            return false;
        } else {
            ResourceLocation var1 = BuiltInRegistries.ENTITY_TYPE.getKey(var0.getType());
            return var1 != null && "ftboss".equals(var1.getNamespace());
        }
    }
}
