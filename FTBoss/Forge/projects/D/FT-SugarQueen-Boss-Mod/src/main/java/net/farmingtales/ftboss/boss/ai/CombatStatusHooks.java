package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.effect.CombatStatusEffect;
import net.farmingtales.ftboss.effect.CombatStatusEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 共享状态的服务端控制、净化、恢复与技能组合。2026-10-26：复用既有禁疗入口。 */
@Mod.EventBusSubscriber(modid = "ftboss", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CombatStatusHooks {
    private static final Map<LivingEntity, Anchor> ANCHORS = new WeakHashMap<>();
    private static final Map<LivingEntity, Integer> VALOR_TICKS = new WeakHashMap<>();
    private static final String VALOR_NEXT = "ftboss_shared_valor_next";

    private CombatStatusHooks() {}

    public static boolean sharedHarmful(MobEffect effect) {
        return effect instanceof CombatStatusEffect && !effect.isBeneficial();
    }

    public static boolean apply(LivingEntity target, MobEffect effect, int requested) {
        if (target.level().isClientSide || !target.isAlive()) return false;
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) return false;
        int duration = CombatStatusRules.duration(((CombatStatusEffect) effect).id(), requested);
        MobEffectInstance current = target.getEffect(effect);
        if (current != null && (current.isInfiniteDuration() || current.getDuration() >= duration)) return true;
        return target.addEffect(new MobEffectInstance(effect, duration, 0, false, true, true));
    }

    public static void xueliHit(LivingEntity target, String attack) {
        switch (attack) {
            case "pot" -> {
                apply(target, CombatStatusEffects.ARMOR_BREAK, 120);
                apply(target, CombatStatusEffects.BONE_FRACTURE, 120);
            }
            case "pan" -> {
                apply(target, CombatStatusEffects.IGNITE, 80);
                apply(target, CombatStatusEffects.AIRBORNE, 20);
            }
            case "roller" -> {
                apply(target, CombatStatusEffects.BONE_FRACTURE, 120);
                apply(target, CombatStatusEffects.ROOTED, 40);
            }
            case "slam" -> {
                apply(target, CombatStatusEffects.ARMOR_BREAK, 120);
                apply(target, CombatStatusEffects.STUN, 20);
            }
            case "knife" -> {
                apply(target, CombatStatusEffects.GRIEVOUS_WOUND, 120);
                apply(target, CombatStatusEffects.FLICKERING_DARKNESS, 100);
            }
            case "bomb" -> {
                apply(target, CombatStatusEffects.IGNITE, 80);
                apply(target, CombatStatusEffects.FLICKERING_DARKNESS, 100);
            }
            default -> throw new IllegalArgumentException(attack);
        }
    }

    public static void grantValor(LivingEntity entity) {
        if (entity.level().isClientSide || !entity.isAlive()) return;
        long now = entity.level().getGameTime();
        if (entity.getPersistentData().getLong(VALOR_NEXT) > now) return;
        entity.getPersistentData().putLong(VALOR_NEXT, now + CombatStatusRules.VALOR_COOLDOWN);
        entity.addEffect(new MobEffectInstance(CombatStatusEffects.VALOR, CombatStatusRules.VALOR_DURATION, 0, false, true, true));
    }

    public static boolean grievous(LivingEntity entity) {
        return entity != null && entity.hasEffect(CombatStatusEffects.GRIEVOUS_WOUND);
    }

    public static boolean stunned(LivingEntity entity) { return entity.hasEffect(CombatStatusEffects.STUN); }

    public static boolean immobilized(LivingEntity entity) {
        return stunned(entity) || entity.hasEffect(CombatStatusEffects.ROOTED);
    }

    @SubscribeEvent
    public static void added(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide || event.getOldEffectInstance() != null) return;
        MobEffect effect = event.getEffectInstance().getEffect();
        if ((effect == CombatStatusEffects.STUN || effect == CombatStatusEffects.ROOTED) && !immobilized(entity)) {
            ANCHORS.put(entity, new Anchor(entity.level(), entity.position(), entity.getYRot(), entity.getXRot()));
        }
        if (effect == CombatStatusEffects.AIRBORNE && !immobilized(entity)) {
            Vec3 velocity = entity.getDeltaMovement();
            entity.setDeltaMovement(velocity.x, Math.max(0.9, velocity.y), velocity.z);
            entity.hasImpulse = true;
            entity.hurtMarked = true;
        }
        if (effect == CombatStatusEffects.VALOR) {
            VALOR_TICKS.put(entity, 0);
            for (MobEffectInstance active : new ArrayList<>(entity.getActiveEffects())) {
                if (active.getEffect().getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL) {
                    entity.removeEffect(active.getEffect());
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void tick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        if (!entity.isAlive()) {
            ANCHORS.remove(entity);
            VALOR_TICKS.remove(entity);
            return;
        }
        hold(entity);
        if (entity.hasEffect(CombatStatusEffects.VALOR)) {
            int elapsed = VALOR_TICKS.getOrDefault(entity, 0) + 1;
            if (elapsed >= CombatStatusRules.SECOND) {
                entity.heal(CombatStatusRules.VALOR_HEAL);
                elapsed = 0;
            }
            VALOR_TICKS.put(entity, elapsed);
        } else {
            VALOR_TICKS.remove(entity);
        }
    }

    private static void hold(LivingEntity entity) {
        if (!immobilized(entity) || !entity.isAlive()) {
            ANCHORS.remove(entity);
            return;
        }
        Anchor anchor = ANCHORS.get(entity);
        if (anchor == null || anchor.level() != entity.level()) {
            anchor = new Anchor(entity.level(), entity.position(), entity.getYRot(), entity.getXRot());
            ANCHORS.put(entity, anchor);
        }
        entity.setDeltaMovement(Vec3.ZERO);
        entity.setSprinting(false);
        entity.fallDistance = 0;
        if (entity instanceof Mob mob) mob.getNavigation().stop();
        boolean stun = stunned(entity);
        if (entity.position().distanceToSqr(anchor.position()) > 0.000001
                || stun && (entity.getYRot() != anchor.yaw() || entity.getXRot() != anchor.pitch())) {
            if (entity instanceof ServerPlayer player) {
                player.connection.teleport(anchor.position().x, anchor.position().y, anchor.position().z,
                        stun ? anchor.yaw() : entity.getYRot(), stun ? anchor.pitch() : entity.getXRot());
            } else {
                entity.teleportTo(anchor.position().x, anchor.position().y, anchor.position().z);
                if (stun) { entity.setYRot(anchor.yaw()); entity.setXRot(anchor.pitch()); }
            }
        }
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) hold(event.player);
    }

    @SubscribeEvent
    public static void attack(AttackEntityEvent event) {
        if (stunned(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void damage(LivingAttackEvent event) {
        if (event.getSource().getEntity() instanceof LivingEntity attacker && stunned(attacker)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void interact(PlayerInteractEvent event) {
        if (event.isCancelable() && stunned(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void use(LivingEntityUseItemEvent event) {
        if (event.isCancelable() && stunned(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void breakBlock(BlockEvent.BreakEvent event) {
        if (stunned(event.getPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void teleport(EntityTeleportEvent event) {
        if (event.isCancelable() && event.getEntity() instanceof LivingEntity living && immobilized(living)) event.setCanceled(true);
    }

    private record Anchor(Level level, Vec3 position, float yaw, float pitch) {}
}
