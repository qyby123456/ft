package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.util.LemonBalanceRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class LemonSummonLimit {
    private static final String OWNER = "ftboss:lemon_summon_root";
    private static final String CONVERTED = "ftboss:lemon_summon_converted";
    private static final String OVERFLOW = "ftboss:lemon_summon_overflow";
    private static final UUID ATTACK = UUID.fromString("b186caaf-35cb-4c91-aacc-cb45912d45f0");
    private static final UUID ARMOR = UUID.fromString("83ea0d21-82e8-40a3-930f-561dd160a92f");

    private LemonSummonLimit() {}

    private static boolean isLemon(Entity entity) {
        return entity instanceof EasterBossEntity boss && boss.getVariant() == EasterBossEntity.LEMONCAT_VARIANT
                && !boss.isMini() && !boss.isGiant();
    }

    public static EasterBossEntity root(Entity entity) {
        if (entity == null || !(entity.level() instanceof ServerLevel level)) return null;
        ArrayDeque<Entity> pending = new ArrayDeque<>();
        Set<UUID> visited = new HashSet<>();
        List<Entity> ancestry = new ArrayList<>();
        pending.add(entity);
        while (!pending.isEmpty()) {
            Entity current = pending.removeFirst();
            if (!visited.add(current.getUUID())) continue;
            ancestry.add(current);
            if (isLemon(current)) return (EasterBossEntity) current;
            if (current.getPersistentData().hasUUID(OWNER)) {
                Entity owner = level.getEntity(current.getPersistentData().getUUID(OWNER));
                if (isLemon(owner)) return (EasterBossEntity) owner;
            }
            UUID blessing = MinionBlessing.ownerId(current);
            Entity parent = blessing == null ? null : level.getEntity(blessing);
            if (parent != null) pending.add(parent);
            parent = ManorFightMembers.owner(current);
            if (parent != null) pending.add(parent);
        }
        if (ancestry.size() > 1 || entity.getPersistentData().getBoolean(LemonSummonRewards.NO_REWARDS_TAG)) {
            for (Entity candidate : level.getAllEntities()) {
                if (!isLemon(candidate)) continue;
                for (Entity ancestor : ancestry) {
                    if (((EasterBossEntity) candidate).isLemonDirectSummon(ancestor)) return (EasterBossEntity) candidate;
                }
            }
        }
        return null;
    }

    public static void inherit(Entity parent, Entity child) {
        if (parent == null || child == null || parent == child) return;
        EasterBossEntity lemon = root(parent);
        if (lemon != null) child.getPersistentData().putUUID(OWNER, lemon.getUUID());
    }

    public static boolean isBoss(Entity entity) {
        if (entity instanceof RatBossEntity rat) return !rat.isMinion();
        if (entity instanceof EasterBossEntity boss) {
            return !boss.isMini() && !boss.isGiant() && boss.getVariant() != EasterBossEntity.SPARROW_VARIANT;
        }
        return entity.getType() == FTEntities.SUGAR_QUEEN.get() || entity.getType() == FTEntities.MEAT_MOUNTAIN.get()
                || entity.getType() == FTEntities.TEA_TREE.get() || entity.getType() == FTEntities.WINE_BARREL.get();
    }

    public static List<LivingEntity> summons(EasterBossEntity lemon) {
        List<LivingEntity> result = new ArrayList<>();
        if (!(lemon.level() instanceof ServerLevel level)) return result;
        for (Entity entity : level.getAllEntities()) {
            if (entity == lemon || !(entity instanceof LivingEntity living) || !living.isAlive() || living.isRemoved()) continue;
            if (root(entity) != lemon) continue;
            entity.getPersistentData().putUUID(OWNER, lemon.getUUID());
            result.add(living);
        }
        return result;
    }

    private static void modifier(AttributeInstance attribute, UUID identifier, int amount) {
        if (attribute == null) return;
        AttributeModifier previous = attribute.getModifier(identifier);
        if (previous != null && previous.getAmount() == amount) return;
        attribute.removeModifier(identifier);
        attribute.addPermanentModifier(new AttributeModifier(identifier, "Lemon excess summons", amount,
                AttributeModifier.Operation.ADDITION));
    }

    public static void restoreBonus(EasterBossEntity lemon) {
        int amount = Math.max(0, lemon.getPersistentData().getInt(OVERFLOW));
        if (amount == 0) return;
        modifier(lemon.getAttribute(Attributes.ATTACK_DAMAGE), ATTACK, amount);
        modifier(lemon.getAttribute(Attributes.ARMOR), ARMOR, amount);
    }

    private static void convert(EasterBossEntity lemon, Entity summon) {
        if (summon.getPersistentData().getBoolean(CONVERTED)) {
            summon.discard();
            return;
        }
        summon.getPersistentData().putBoolean(CONVERTED, true);
        int amount = (int) Math.min(Integer.MAX_VALUE, Math.max(0L, lemon.getPersistentData().getInt(OVERFLOW)) + 1L);
        lemon.getPersistentData().putInt(OVERFLOW, amount);
        restoreBonus(lemon);
        summon.discard();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onJoin(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (!(event.getLevel() instanceof ServerLevel) || !(entity instanceof LivingEntity living)
                || !living.isAlive() || isLemon(entity)) return;
        EasterBossEntity lemon = root(entity);
        if (lemon == null || !lemon.isAlive() || lemon.isRemoved()) return;
        entity.getPersistentData().putUUID(OWNER, lemon.getUUID());
        List<LivingEntity> active = summons(lemon);
        active.remove(entity);
        int bosses = (int) active.stream().filter(LemonSummonLimit::isBoss).count();
        if (LemonBalanceRules.exceedsSummonLimit(active.size(), bosses, isBoss(entity))) {
            event.setCanceled(true);
            convert(lemon, entity);
        }
    }

    @SubscribeEvent
    public static void onTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof EasterBossEntity lemon) || !isLemon(lemon)
                || !(lemon.level() instanceof ServerLevel) || !lemon.isAlive() || lemon.isRemoved()
                || lemon.tickCount % 20 != 0) return;
        restoreBonus(lemon);
        List<LivingEntity> active = summons(lemon);
        active.sort((left, right) -> Boolean.compare(isBoss(right), isBoss(left)));
        int count = 0;
        int bosses = 0;
        for (LivingEntity summon : active) {
            if (!summon.isAlive() || summon.isRemoved()) continue;
            boolean boss = isBoss(summon);
            if (LemonBalanceRules.exceedsSummonLimit(count, bosses, boss)) convert(lemon, summon);
            else {
                count++;
                if (boss) bosses++;
            }
        }
    }
}
