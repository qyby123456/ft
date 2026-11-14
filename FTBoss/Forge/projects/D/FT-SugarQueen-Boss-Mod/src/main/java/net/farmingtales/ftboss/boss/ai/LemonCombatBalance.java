package net.farmingtales.ftboss.boss.ai;

import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.util.LemonBalanceRules;
import net.farmingtales.ftboss.util.ManorDamageLimitRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class LemonCombatBalance {
    private static final String HEAL_STATE = "ftboss:lemon_healing";
    private static final UUID ABSORBED_HEALTH = UUID.fromString("70c39fb7-4f59-4c67-8b52-0c49080a852e");

    private LemonCombatBalance() {}

    public static int players(EasterBossEntity lemon) {
        return ManorMultiplayerBalance.isManagedManorBoss(lemon)
                ? Math.max(1, lemon.getPersistentData().getInt("ftboss_manor_player_count"))
                : BossScaling.playersInFight(lemon);
    }

    public static int requiredBosses(EasterBossEntity lemon) {
        return LemonBalanceRules.requiredBosses(players(lemon));
    }

    public static void refillBossPool(List<EntityType<?>> types) {
        if (!types.isEmpty()) return;
        types.add(FTEntities.SUGAR_QUEEN.get());
        types.add(FTEntities.MEAT_MOUNTAIN.get());
        types.add(FTEntities.TEA_TREE.get());
        types.add(FTEntities.WINE_BARREL.get());
        types.add(FTEntities.RAT_BOSS.get());
    }

    public static void absorbMaximumHealth(EasterBossEntity lemon, List<Mob> victims) {
        var maximum = lemon.getAttribute(Attributes.MAX_HEALTH);
        if (maximum == null || maximum.getModifier(ABSORBED_HEALTH) != null) return;
        double absorbed = victims.stream().filter(victim -> !(victim instanceof RatBossEntity)).mapToDouble(Mob::getMaxHealth)
                .filter(health -> Double.isFinite(health) && health > 0.0).sum();
        if (!Double.isFinite(absorbed) || absorbed <= 0.0) return;
        lemon.lemonSacrifice().state().putDouble("absorbed_max_health", absorbed);
        maximum.addPermanentModifier(new AttributeModifier(ABSORBED_HEALTH,
                "Lemon absorbed boss maximum health", absorbed, AttributeModifier.Operation.ADDITION));
    }

    private static ManorDamageLimitRules.Window healingWindow(CompoundTag state) {
        return new ManorDamageLimitRules.Window(state.getLongArray("ticks"),
                state.getLongArray("amounts"), state.getLong("last_tick"));
    }

    public static float limitHealing(EasterBossEntity lemon, float requested) {
        if (lemon.getVariant() != EasterBossEntity.LEMONCAT_VARIANT || lemon.level().isClientSide) return requested;
        float available = Math.max(0.0F, lemon.getMaxHealth() - lemon.getHealth());
        return healingWindow(lemon.getPersistentData().getCompound(HEAL_STATE)).take(lemon.level().getGameTime(),
                ManorDamageLimitRules.OTHER_DAMAGE, Math.min(requested, available),
                LemonBalanceRules.MAX_HEAL_PER_SECOND, LemonBalanceRules.MAX_HEAL_PER_SECOND,
                LemonBalanceRules.MAX_HEAL_PER_SECOND);
    }

    public static void recordHealing(EasterBossEntity lemon, float before) {
        if (lemon.getVariant() != EasterBossEntity.LEMONCAT_VARIANT || lemon.level().isClientSide) return;
        CompoundTag state = lemon.getPersistentData().getCompound(HEAL_STATE);
        ManorDamageLimitRules.Window window = healingWindow(state);
        window.take(lemon.level().getGameTime(), ManorDamageLimitRules.OTHER_DAMAGE,
                lemon.getHealth() - before, LemonBalanceRules.MAX_HEAL_PER_SECOND,
                LemonBalanceRules.MAX_HEAL_PER_SECOND, LemonBalanceRules.MAX_HEAL_PER_SECOND);
        state.putLongArray("ticks", window.ticks());
        state.putLongArray("amounts", window.amounts());
        state.putLong("last_tick", window.lastTick());
        lemon.getPersistentData().put(HEAL_STATE, state);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void capHealing(LivingHealEvent event) {
        if (event.getEntity() instanceof EasterBossEntity lemon) {
            event.setAmount(limitHealing(lemon, event.getAmount()));
        }
    }
}
