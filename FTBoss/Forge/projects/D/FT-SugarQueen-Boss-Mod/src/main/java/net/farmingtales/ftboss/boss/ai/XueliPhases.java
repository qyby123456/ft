package net.farmingtales.ftboss.boss.ai;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.XueliPhaseRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 雪黎本体的可回退阶段、瞬时属性和玩家命中回复（2026-10-26）。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class XueliPhases {
    private static final UUID MOVEMENT_MODIFIER = UUID.fromString("841ee31a-a204-407e-873f-b2b3e8b62c7a");
    private static final UUID ATTACK_MODIFIER = UUID.fromString("c1e33f79-29a0-4acb-8937-1c480d8454ae");
    private static final Map<Projectile, Map<UUID, Long>> PROJECTILE_HITS = new WeakHashMap<>();

    private XueliPhases() {}

    public static boolean applies(EasterBossEntity boss) {
        return boss.getVariant() == EasterBossEntity.XUELI_VARIANT && !boss.isMini() && !boss.isGiant()
                && boss.isAlive() && !boss.isDeadOrDying();
    }

    public static int phase(EasterBossEntity boss) {
        return XueliPhaseRules.phase(boss.getHealth(), boss.getMaxHealth());
    }

    public static int cooldown(int original, EasterBossEntity boss) {
        return applies(boss) ? XueliPhaseRules.cooldown(original, phase(boss)) : original;
    }

    public static int skillInterval(int original, EasterBossEntity boss) {
        return applies(boss) ? XueliPhaseRules.skillInterval(original, phase(boss)) : original;
    }

    public static int remainingSkillCooldown(int remaining, EasterBossEntity boss) {
        return applies(boss) ? Math.min(remaining, skillInterval(Integer.MAX_VALUE, boss)) : remaining;
    }

    public static int attackInterval(int original, EasterBossEntity boss) {
        return applies(boss) ? Math.max(1, boss.getChallengeDifficulty().acceleratedTicks(XueliPhaseRules.attackInterval(phase(boss)))) : original;
    }

    public static int remainingMeleeCooldown(int remaining, EasterBossEntity boss) {
        return applies(boss) ? Math.min(remaining, boss.getMeleeAttackInterval()) : remaining;
    }

    public static boolean shouldCast(EasterBossEntity boss) {
        return !applies(boss) || phase(boss) != 1 || XueliPhaseRules.shouldCast(1, boss.getRandom().nextFloat());
    }

    private static void modifier(AttributeInstance attribute, UUID identifier, String name, double bonus) {
        if (attribute == null) return;
        AttributeModifier previous = attribute.getModifier(identifier);
        if (previous != null && previous.getAmount() == bonus) return;
        if (previous != null) attribute.removeModifier(identifier);
        if (bonus > 0.0) attribute.addTransientModifier(new AttributeModifier(identifier, name, bonus, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    private static void refreshAttributes(EasterBossEntity boss) {
        double bonus = applies(boss) ? XueliPhaseRules.speedBonus(phase(boss)) : 0.0;
        modifier(boss.getAttribute(Attributes.MOVEMENT_SPEED), MOVEMENT_MODIFIER, "ftboss.xueli_phase_movement", bonus);
        modifier(boss.getAttribute(Attributes.ATTACK_SPEED), ATTACK_MODIFIER, "ftboss.xueli_phase_attack", bonus);
    }

    @SubscribeEvent
    public static void tick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof EasterBossEntity boss && !boss.level().isClientSide) refreshAttributes(boss);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void hit(LivingDamageEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player) || !Float.isFinite(event.getAmount())
                || event.getAmount() <= 0.0f || !(event.getSource().getEntity() instanceof EasterBossEntity boss)
                || boss.level().isClientSide || !applies(boss)) return;
        if (event.getSource().getDirectEntity() instanceof Projectile projectile) {
            long now = boss.level().getGameTime();
            Long previous = PROJECTILE_HITS.computeIfAbsent(projectile, ignored -> new HashMap<>()).put(player.getUUID(), now);
            if (previous != null && previous == now) return;
        }
        boss.heal(XueliPhaseRules.healing(boss.getHealth(), boss.getMaxHealth()));
        refreshAttributes(boss);
    }
}
