package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.boss.ai.PlayerWeaponTargeting;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class FrostQueenStaffItem extends Item {
    public static final double RAY_RANGE = 24;
    public static final double NOVA_RADIUS = 5;
    public static final float RAY_DAMAGE = 14;
    public static final float NOVA_DAMAGE = 10;
    public static final int RAY_COOLDOWN = 30;
    public static final int NOVA_COOLDOWN = 160;
    private static final int MAX_NOVA_TARGETS = 16;

    public FrostQueenStaffItem() {
        super(new Properties().durability(1024).rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || !player.isAlive() || player.isSpectator()
                || FoodWeaponLifecycle.isBroken(stack) || player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        if (!(player instanceof ServerPlayer caster)) return InteractionResultHolder.sidedSuccess(stack, true);
        boolean nova = player.isShiftKeyDown();
        int cooldown = FoodWeaponFamilyBuild.chargeTicks(stack, nova ? NOVA_COOLDOWN : RAY_COOLDOWN);
        FoodWeaponRanged.withStaffCast(caster, stack, nova ? 4 : 1, () -> {
            if (nova) nova(caster);
            else ray(caster);
        });
        player.getCooldowns().addCooldown(this, cooldown);
        player.swing(hand, true);
        player.causeFoodExhaustion(nova ? 0.2F : 0.05F);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    private static boolean canTarget(ServerPlayer player, LivingEntity target) {
        return PlayerWeaponTargeting.canTarget(player, target) && !target.isRemoved()
                && !player.isPassengerOfSameVehicle(target)
                && !(target instanceof AbstractHorse horse && horse.isTamed());
    }

    private static void ray(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 start = player.getEyePosition();
        double range = RAY_RANGE * FoodWeaponFamilyBuild.rangeMultiplier(player.getMainHandItem());
        Vec3 end = level.clip(new ClipContext(start, start.add(player.getLookAngle().scale(range)),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getLocation();
        LivingEntity nearest = null;
        Vec3 impact = end;
        double nearestDistance = start.distanceToSqr(end);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(start, end).inflate(0.3),
                candidate -> canTarget(player, candidate))) {
            AABB hitbox = target.getBoundingBox().inflate(0.15);
            Vec3 hit = hitbox.contains(start) ? start : hitbox.clip(start, end).orElse(null);
            if (hit != null && start.distanceToSqr(hit) < nearestDistance) {
                nearestDistance = start.distanceToSqr(hit);
                nearest = target;
                impact = hit;
            }
        }
        if (nearest != null) strike(player, nearest,
                RAY_DAMAGE * (float) FoodWeaponFamilyBuild.damageMultiplier(player.getMainHandItem()), 60, 2);
        Vec3 direction = impact.subtract(start);
        int steps = Math.max(1, (int) Math.ceil(direction.length() / 0.35));
        for (int step = 0; step <= steps; step++) {
            Vec3 position = start.add(direction.scale((double) step / steps));
            level.sendParticles(ParticleTypes.SNOWFLAKE, position.x, position.y, position.z, 1, 0.03, 0.03, 0.03, 0);
            if (step % 3 == 0) level.sendParticles(ParticleTypes.END_ROD,
                    position.x, position.y, position.z, 1, 0, 0, 0, 0);
        }
        level.sendParticles(ParticleTypes.SNOWFLAKE, impact.x, impact.y, impact.z, 18, 0.25, 0.25, 0.25, 0.025);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1, 1.4F);
    }

    private static void nova(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 center = player.position().add(0, 0.2, 0);
        double radius = NOVA_RADIUS * FoodWeaponFamilyBuild.rangeMultiplier(player.getMainHandItem());
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(radius), target -> canTarget(player, target)
                        && player.distanceToSqr(target) <= radius * radius);
        targets.sort(Comparator.comparingDouble(player::distanceToSqr));
        int hits = 0;
        for (LivingEntity target : targets) {
            Vec3 targetCenter = target.getBoundingBox().getCenter();
            if (level.clip(new ClipContext(player.getEyePosition(), targetCenter, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, player)).getType() != HitResult.Type.MISS) continue;
            strike(player, target, NOVA_DAMAGE * (float) FoodWeaponFamilyBuild.damageMultiplier(player.getMainHandItem()), 100, 1);
            if (++hits >= MAX_NOVA_TARGETS) break;
        }
        for (int ring = 1; ring <= 3; ring++) {
            double ringRadius = radius * ring / 3;
            for (int point = 0; point < 48; point++) {
                double angle = Math.PI * 2 * point / 48;
                level.sendParticles(ParticleTypes.SNOWFLAKE, center.x + Math.cos(angle) * ringRadius,
                        center.y, center.z + Math.sin(angle) * ringRadius, 1, 0.04, 0.15, 0.04, 0.015);
            }
        }
        level.playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8F, 1.3F);
    }

    private static void strike(ServerPlayer player, LivingEntity target, float damage, int duration, int amplifier) {
        if (target.hurt(player.damageSources().indirectMagic(player, player), damage)) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, amplifier));
        }
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack ingredient) {
        return ingredient.is(Items.AMETHYST_SHARD) || super.isValidRepairItem(stack, ingredient);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ftboss.frost_queen_staff.ray",
                (int) RAY_RANGE, (int) RAY_DAMAGE, RAY_COOLDOWN / 20.0F).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.ftboss.frost_queen_staff.nova",
                (int) NOVA_RADIUS, (int) NOVA_DAMAGE, NOVA_COOLDOWN / 20).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.ftboss.frost_queen_staff.rules").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ftboss.frost_queen_staff.source").withStyle(ChatFormatting.DARK_GRAY));
    }
}
