package net.farmingtales.ftboss.integ.tridents;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.mixin.SanguineBondEntityAccess;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent.Remove;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import org.joml.Vector3f;

public final class SanguineBondHooks {
    public static final ResourceLocation ITEM_ID = ResourceLocation.fromNamespaceAndPath("tridents_n_stuff", "sanguine_bond");
    public static final ResourceLocation BLEEDING_ID = ResourceLocation.fromNamespaceAndPath("tridents_n_stuff", "bleeding");
    private static final String SKILL_ONE_READY = "ftboss:SanguineSkillOneReady";
    private static final String SKILL_TWO_READY = "ftboss:SanguineSkillTwoReady";
    private static final int MAX_AMPLIFIER = 4;
    private static final int BLEED_TICKS = 400;
    private static final int SKILL_ONE_COOLDOWN = 160;
    private static final int SKILL_TWO_COOLDOWN = 240;
    private static final DustParticleOptions BLOOD = new DustParticleOptions(new Vector3f(0.82F, 0.035F, 0.08F), 1.35F);

    private SanguineBondHooks() {
    }

    public static boolean isSanguine(ItemStack stack) {
        return stack != null && !stack.isEmpty() && ITEM_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public static boolean isBleeding(Holder<MobEffect> effect) {
        return effect != null && BLEEDING_ID.equals(BuiltInRegistries.MOB_EFFECT.getKey(effect.value()));
    }

    public static ItemStack entityTridentStack(Entity entity) {
        return entity instanceof SanguineBondEntityAccess access ? access.ftboss$getTridentStack() : ItemStack.EMPTY;
    }

    @SubscribeEvent(
        priority = EventPriority.HIGHEST
    )
    public static void onBloodSkillUse(RightClickItem event) {
        if (!event.getLevel().isClientSide()
            && event.getHand() == InteractionHand.MAIN_HAND
            && event.getEntity().isShiftKeyDown()
            && isSanguine(event.getItemStack())) {
            if (event.getEntity() instanceof ServerPlayer player) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
                long var6 = player.level().getGameTime();
                long ready = player.getPersistentData().getLong("ftboss:SanguineSkillOneReady");
                if (ready > var6) {
                    player.displayClientMessage(Component.literal("§c血潮突刺冷却中 §7(" + (ready - var6 + 19L) / 20L + "秒)"), true);
                } else {
                    player.getPersistentData().putLong("ftboss:SanguineSkillOneReady", var6 + 160L);
                    castBloodLance(player);
                }
            }
        }
    }

    @SubscribeEvent(
        priority = EventPriority.HIGHEST
    )
    public static void onExecution(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide
            && player.isShiftKeyDown()
            && isSanguine(player.getMainHandItem())
            && event.getTarget() instanceof LivingEntity target
            && isValidTarget(player, target)) {
            Holder<MobEffect> bleeding = bleedingEffect();
            MobEffectInstance mark = bleeding == null ? null : target.getEffect(bleeding);
            if (mark != null) {
                event.setCanceled(true);
                long now = player.level().getGameTime();
                long ready = player.getPersistentData().getLong("ftboss:SanguineSkillTwoReady");
                if (ready > now) {
                    player.displayClientMessage(Component.literal("§c契约处刑冷却中 §7(" + (ready - now + 19L) / 20L + "秒)"), true);
                } else {
                    player.getPersistentData().putLong("ftboss:SanguineSkillTwoReady", now + 240L);
                    execute(player, target, mark.getAmplifier() + 1);
                }
            }
        }
    }

    @SubscribeEvent(
        priority = EventPriority.HIGHEST
    )
    public static void keepBleedingOnCleanse(Remove event) {
        if (isBleeding(event.getEffect())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(
        priority = EventPriority.LOWEST
    )
    public static void onSanguineDamage(LivingDamageEvent.Pre event) {
        if (!event.getEntity().level().isClientSide && !(event.getNewDamage() <= 0.0F)) {
            Entity direct = event.getSource().getDirectEntity();
            ItemStack stack = entityTridentStack(direct);
            if (!isSanguine(stack) && event.getSource().getEntity() instanceof LivingEntity living) {
                ItemStack held = living.getMainHandItem();
                if (isSanguine(held)) {
                    stack = held;
                }
            }

            if (isSanguine(stack)) {
                if (event.getEntity().level() instanceof ServerLevel level) {
                    LivingEntity victim = event.getEntity();
                    level.sendParticles(BLOOD, victim.getX(), victim.getY(0.65), victim.getZ(), 10, 0.28, 0.38, 0.28, 0.05);
                    level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, victim.getX(), victim.getY(0.65), victim.getZ(), 5, 0.22, 0.32, 0.22, 0.1);
                    level.playSound(null, victim.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.65F, 0.72F);
                }
            }
        }
    }

    public static void applyBleeding(LivingEntity target, int extraStacks) {
        Holder<MobEffect> effect = bleedingEffect();
        if (effect != null && !target.level().isClientSide) {
            MobEffectInstance old = target.getEffect(effect);
            int oldAmp = old == null ? -1 : old.getAmplifier();
            int amp = Math.min(4, oldAmp + Math.max(1, extraStacks));
            target.addEffect(new MobEffectInstance(effect, 400, amp));
        }
    }

    private static Holder<MobEffect> bleedingEffect() {
        return BuiltInRegistries.MOB_EFFECT.getHolder(BLEEDING_ID).orElse(null);
    }

    private static void castBloodLance(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(18.0));
        BlockHitResult clip = level.clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, player));
        if (clip.getType() != Type.MISS) {
            end = clip.getLocation();
        }

        int hits = 0;
        AABB area = new AABB(start, end).inflate(1.25);

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, var1x -> isValidTarget(player, var1x))) {
            if (!(distanceToSegment(target.getBoundingBox().getCenter(), start, end) > 1.35) && player.hasLineOfSight(target)) {
                if (target.hurt(level.damageSources().indirectMagic(player, player), 8.0F)) {
                    applyBleeding(target, 1);
                    hits++;
                    bloodBurst(level, target.position().add(0.0, (double)target.getBbHeight() * 0.6, 0.0), 8);
                }

                if (hits >= 16) {
                    break;
                }
            }
        }

        for (int index = 0; index <= 28; index++) {
            Vec3 point = start.lerp(end, (double)index / 28.0);
            level.sendParticles(BLOOD, point.x, point.y, point.z, 2, 0.04, 0.04, 0.04, 0.01);
        }

        level.sendParticles(ParticleTypes.SWEEP_ATTACK, end.x, end.y, end.z, 3, 0.2, 0.2, 0.2, 0.0);
        level.sendParticles(ParticleTypes.DAMAGE_INDICATOR, end.x, end.y, end.z, 12, 0.45, 0.45, 0.45, 0.08);
        level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 0.9F, 1.3F);
        FTBossMod.LOGGER.info("[FTBoss][SanguineBond] skill1 blood lance player={} hits={}", player.getName().getString(), hits);
    }

    private static void execute(Player player, LivingEntity target, int stacks) {
        float damage = 10.0F + (float)Math.min(5, stacks) * 3.0F;
        if (target.hurt(player.level().damageSources().playerAttack(player), damage)) {
            player.heal(Math.min(8.0F, damage * 0.25F));
            if (player.level() instanceof ServerLevel level) {
                Vec3 center = target.position().add(0.0, (double)target.getBbHeight() * 0.55, 0.0);
                bloodBurst(level, center, 28);
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y, center.z, 8, 0.55, 0.55, 0.55, 0.0);
                level.playSound(null, target.blockPosition(), SoundEvents.TRIDENT_HIT, SoundSource.PLAYERS, 1.0F, 0.65F);
            }

            FTBossMod.LOGGER
                .info(
                    "[FTBoss][SanguineBond] skill2 execution player={} target={} stacks={}",
                    new Object[]{player.getName().getString(), target.getName().getString(), stacks}
                );
        }
    }

    private static void bloodBurst(ServerLevel level, Vec3 center, int count) {
        level.sendParticles(BLOOD, center.x, center.y, center.z, count, 0.45, 0.55, 0.45, 0.12);
        level.sendParticles(ParticleTypes.CRIT, center.x, center.y, center.z, count / 2, 0.36, 0.45, 0.36, 0.08);
    }

    private static boolean isValidTarget(LivingEntity source, LivingEntity target) {
        if (target != source
            && target.isAlive()
            && !target.isRemoved()
            && !(target instanceof ArmorStand)
            && !source.isAlliedTo(target)
            && !target.isAlliedTo(source)) {
            if (target instanceof ServerPlayer player && (player.isCreative() || player.isSpectator())) {
                return false;
            }

            if (source instanceof Player player && target instanceof Player other && !player.canHarmPlayer(other)) {
                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    private static double distanceToSegment(Vec3 point, Vec3 start, Vec3 end) {
        Vec3 delta = end.subtract(start);
        double lengthSquared = delta.lengthSqr();
        if (lengthSquared < 1.0E-7) {
            return point.distanceTo(start);
        } else {
            double t = point.subtract(start).dot(delta) / lengthSquared;
            t = Math.max(0.0, Math.min(1.0, t));
            return point.distanceTo(start.lerp(end, t));
        }
    }
}
