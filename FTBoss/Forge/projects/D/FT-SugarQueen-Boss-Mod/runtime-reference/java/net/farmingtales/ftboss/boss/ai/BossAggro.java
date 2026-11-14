/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.PotionItem
 *  net.minecraft.world.item.alchemy.PotionUtils
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.boss.BossBars;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.BossThreatTable;
import net.farmingtales.ftboss.boss.ai.ManorCombatLanes;
import net.farmingtales.ftboss.boss.ai.ManorFinaleCombat;
import net.farmingtales.ftboss.boss.ai.ManorMultiplayerBalance;
import net.farmingtales.ftboss.boss.ai.ManorTargetPolicy;
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
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public final class BossAggro {
    public static final double MAX_DIST = 64.0;
    private static final Map<UUID, BossThreatTable> MEMORY = new HashMap<UUID, BossThreatTable>();

    private BossAggro() {
    }

    public static void record(LivingEntity victim, Entity attacker, double damage, long gameTime) {
        LivingEntity living;
        if (!(BossAggro.supportsThreat(victim) && attacker instanceof LivingEntity && BossAggro.validTarget(victim, living = (LivingEntity)attacker) && Double.isFinite(damage) && !(damage <= 0.0))) {
            return;
        }
        BossAggro.add(victim, living, Math.min(damage, (double)victim.m_21223_()), gameTime);
    }

    public static void consume(LivingEntity user, ItemStack stack) {
        double amount;
        ServerLevel level;
        block5: {
            block4: {
                Level level2 = user.m_9236_();
                if (!(level2 instanceof ServerLevel)) break block4;
                level = (ServerLevel)level2;
                if (BossTargets.valid(user)) break block5;
            }
            return;
        }
        double d = stack.m_41720_() instanceof PotionItem && !PotionUtils.m_43547_((ItemStack)stack).isEmpty() ? 8.0 : (amount = stack.m_41614_() ? 5.0 : 0.0);
        if (amount <= 0.0) {
            return;
        }
        BossBars.forEach(level.m_7654_(), boss -> {
            if (!BossAggro.supportsThreat(boss) || !BossAggro.validTarget(boss, user)) {
                return;
            }
            Mob mob = (Mob)boss;
            LivingEntity threat = BossAggro.highest(boss);
            if (threat != null || BossAggro.validTarget(boss, mob.m_5448_())) {
                BossAggro.add(boss, user, amount, boss.m_9236_().m_46467_());
            }
        });
    }

    private static void add(LivingEntity boss, LivingEntity target, double amount, long now) {
        MEMORY.computeIfAbsent(boss.m_20148_(), ignored -> new BossThreatTable()).add(target.m_20148_(), amount, now);
        BossAggro.apply((Mob)boss);
    }

    public static LivingEntity highest(LivingEntity boss) {
        LivingEntity living;
        Entity entity;
        if (!BossAggro.supportsThreat(boss)) {
            return null;
        }
        BossThreatTable table = MEMORY.get(boss.m_20148_());
        if (table == null) {
            return null;
        }
        ServerLevel level = (ServerLevel)boss.m_9236_();
        LivingEntity current = ((Mob)boss).m_5448_();
        UUID target = ManorCombatLanes.selectThreatId(table, level.m_46467_(), current == null ? null : current.m_20148_(), id -> {
            LivingEntity living;
            Entity patt2770$temp = level.m_8791_(id);
            return patt2770$temp instanceof LivingEntity && BossAggro.validTarget(boss, living = (LivingEntity)patt2770$temp);
        }, boss);
        if (table.isEmpty()) {
            MEMORY.remove(boss.m_20148_());
        }
        return target != null && (entity = level.m_8791_(target)) instanceof LivingEntity ? (living = (LivingEntity)entity) : null;
    }

    public static void tick(MinecraftServer server) {
        if (server == null || MEMORY.isEmpty()) {
            return;
        }
        for (UUID bossId : new ArrayList<UUID>(MEMORY.keySet())) {
            LivingEntity boss = BossAggro.find(server, bossId);
            if (!BossAggro.supportsThreat(boss)) {
                BossAggro.forget(bossId);
                continue;
            }
            BossAggro.apply((Mob)boss);
        }
    }

    private static void apply(Mob mob) {
        Mob boss;
        if (ManorFinaleCombat.keepCommittedTarget(mob)) {
            return;
        }
        BossThreatTable table = MEMORY.get(boss.m_20148_());
        LivingEntity previous = boss.m_5448_();
        boolean tracked = table != null && previous != null && ManorTargetPolicy.shouldClearTracked(table.contains(previous.m_20148_()), boss, previous);
        LivingEntity target = BossAggro.highest((LivingEntity)boss);
        if (boss.m_21525_()) {
            return;
        }
        if (target != null && target != previous) {
            boss.m_6710_(target);
            boss.m_21573_().m_26573_();
        } else if (target == null && (tracked || previous != null && !BossAggro.validTarget((LivingEntity)boss, previous))) {
            boss.m_6710_(null);
            boss.m_21573_().m_26573_();
        }
    }

    private static boolean supportsThreat(LivingEntity boss) {
        EasterBossEntity easter;
        return boss instanceof Mob && boss.m_9236_() instanceof ServerLevel && BossTargets.valid(boss) && BossAggro.isBossSide((Entity)boss) && (!(boss instanceof EasterBossEntity) || (easter = (EasterBossEntity)boss).getVariant() != 9);
    }

    /*
     * WARNING - void declaration
     */
    private static boolean validTarget(LivingEntity livingEntity, LivingEntity livingEntity2) {
        LivingEntity boss;
        void target;
        if (ManorFinaleCombat.manages(livingEntity)) {
            return ManorTargetPolicy.filterThreat(ManorFinaleCombat.validTarget(livingEntity, livingEntity2), livingEntity, livingEntity2);
        }
        if (ManorMultiplayerBalance.validManorTarget(livingEntity, livingEntity2)) {
            return ManorTargetPolicy.filterThreat(true, livingEntity, livingEntity2);
        }
        return ManorTargetPolicy.filterThreat(BossTargets.valid((LivingEntity)target) && target != boss && target.m_9236_() == boss.m_9236_() && !BossAggro.isBossSide((Entity)target) && boss.m_20280_((Entity)target) <= 4096.0, boss, (LivingEntity)target);
    }

    public static void forget(UUID bossId) {
        MEMORY.remove(bossId);
    }

    public static void forgetEntity(UUID entityId) {
        BossAggro.forget(entityId);
        MEMORY.values().forEach(table -> table.remove(entityId));
    }

    public static void clear() {
        MEMORY.clear();
    }

    private static LivingEntity find(MinecraftServer server, UUID id) {
        for (ServerLevel level : server.m_129785_()) {
            Entity entity = level.m_8791_(id);
            if (!(entity instanceof LivingEntity)) continue;
            LivingEntity living = (LivingEntity)entity;
            return living;
        }
        return null;
    }

    public static boolean isBossSide(Entity entity) {
        if (entity == null) {
            return false;
        }
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey((Object)entity.m_6095_());
        return key != null && "ftboss".equals(key.m_135827_());
    }
}

