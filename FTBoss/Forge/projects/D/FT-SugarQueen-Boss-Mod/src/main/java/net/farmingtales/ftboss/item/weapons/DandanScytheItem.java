package net.farmingtales.ftboss.item.weapons;

import net.farmingtales.ftboss.boss.ai.PlayerWeaponTargeting;
import net.farmingtales.ftboss.item.FTWeaponTier;
import net.farmingtales.ftboss.skill.DandanScytheFxPacket;
import net.farmingtales.ftboss.util.DandanScytheRules;
import net.farmingtales.ftboss.util.DandanScytheMotionRules;
import net.farmingtales.ftboss.util.ManorWeaponBalanceRules;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityTeleportEvent;

import java.util.Comparator;
import java.util.List;

public final class DandanScytheItem extends SwordItem {
    public static final double SWEEP_RADIUS = 4.5;
    public static final double BLINK_DISTANCE = 6.0;
    public static final double BLINK_RADIUS = 2.5;
    public static final float SWEEP_DAMAGE = 20.0F;
    public static final float BLINK_DAMAGE = 16.0F;
    public static final float MARK_BONUS = 8.0F;
    public static final int SWEEP_COOLDOWN = 120;
    public static final int BLINK_COOLDOWN = 200;
    public static final int MARK_TICKS = 100;
    private static final int SLOW_TICKS = 40;
    private static final int MAX_TARGETS = 16;
    private static final String MARK_KEY = "ftboss:DandanCandleMark";

    /** 烛月横斩的实际伤害（庄园技能倍率已计入）。 */
    public static float sweepDamage() {
        return 60.0F;
    }

    /** 烛影跃迁落点环斩的实际伤害（庄园技能倍率已计入）。 */
    public static float blinkDamage() {
        return 48.0F;
    }

    /** 烛痕追加伤害（庄园技能倍率已计入）。 */
    public static float markBonus() {
        return 24.0F;
    }

    public DandanScytheItem() {
        super(FTWeaponTier.INSTANCE, 8, -2.8F, new Properties());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (result && attacker instanceof ServerPlayer player && canTarget(player, target)) {
            CompoundTag mark = new CompoundTag();
            mark.putUUID("owner", player.getUUID());
            mark.putLong("expires", target.level().getGameTime() + MARK_TICKS);
            target.getPersistentData().put(MARK_KEY, mark);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOW_TICKS, 0));
            DandanScytheFxPacket.send(player, target.position(), 0, 0.65F, DandanScytheFxPacket.MARK, target.getId());
        }
        return result;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || !player.isAlive() || player.isSpectator()
                || player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return DandanScytheMotionRules.CHARGE_TICKS;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (!(user instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()
                || player.getMainHandItem() != stack || player.getCooldowns().isOnCooldown(this)) return stack;
        ServerPlayer serverPlayer = player;
        InteractionHand hand = InteractionHand.MAIN_HAND;
        int action;
        if (player.isShiftKeyDown()) {
            if (!blink(serverPlayer)) return stack;
            action = DandanScytheMotionRules.BLINK_ACTION;
            player.getCooldowns().addCooldown(this, BLINK_COOLDOWN);
            stack.hurtAndBreak(5, player, holder -> holder.broadcastBreakEvent(hand));
        } else {
            action = DandanScytheMotionRules.SWEEP_ACTION;
            strike(serverPlayer, SWEEP_RADIUS, sweepDamage(), false);
            DandanScytheFxPacket.send(serverPlayer, player.position().add(0, 0.9, 0), player.getYRot(),
                    (float) SWEEP_RADIUS, DandanScytheFxPacket.SWEEP, -1);
            player.getCooldowns().addCooldown(this, SWEEP_COOLDOWN);
            stack.hurtAndBreak(3, player, holder -> holder.broadcastBreakEvent(hand));
            level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.8F);
        }
        player.swing(hand, true);
        player.causeFoodExhaustion(0.15F);
        DandanScytheFxPacket.send(player, player.position(), player.getYRot(), 1, action, player.getId());
        return stack;
    }

    @Override
    public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new net.minecraftforge.client.extensions.common.IClientItemExtensions() {
            private net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new net.farmingtales.ftboss.client.render.DandanScytheRenderer();
                return renderer;
            }
        });
    }

    private static boolean canTarget(ServerPlayer player, LivingEntity target) {
        return PlayerWeaponTargeting.canTarget(player, target);
    }

    private static Vec3 horizontalForward(Player player) {
        double radians = Math.toRadians(player.getYRot());
        return new Vec3(-Math.sin(radians), 0, Math.cos(radians));
    }

    private static boolean visible(ServerPlayer player, LivingEntity target) {
        return PlayerWeaponTargeting.hasClearPath(player, player.getEyePosition(), target.getBoundingBox().getCenter());
    }

    private static void strike(ServerPlayer player, double radius, float damage, boolean fullCircle) {
        ServerLevel level = player.serverLevel();
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(radius, 1.25, radius), target -> canTarget(player, target));
        targets.sort(Comparator.comparingDouble(player::distanceToSqr));
        int affected = 0;
        for (LivingEntity target : targets) {
            Vec3 delta = target.position().subtract(player.position()).multiply(1, 0, 1);
            if (!DandanScytheRules.withinSweep(delta.x, delta.z, player.getYRot(), radius, fullCircle) || !visible(player, target)) continue;
            CompoundTag mark = target.getPersistentData().getCompound(MARK_KEY);
            boolean marked = mark.hasUUID("owner") && mark.getUUID("owner").equals(player.getUUID())
                    && mark.getLong("expires") > level.getGameTime();
            if (target.hurt(player.damageSources().playerAttack(player), damage + (marked ? markBonus() : 0))) {
                if (marked) {
                    target.getPersistentData().remove(MARK_KEY);
                    DandanScytheFxPacket.send(player, target.getBoundingBox().getCenter(), 0, 1.0F,
                            DandanScytheFxPacket.BURST, target.getId());
                    level.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0, target.getZ(),
                            9, 0.3, 0.4, 0.3, 0.03);
                }
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOW_TICKS, 0));
                target.knockback(0.45, player.getX() - target.getX(), player.getZ() - target.getZ());
                if (++affected >= MAX_TARGETS) break;
            }
        }
    }

    private static boolean blink(ServerPlayer player) {
        if (player.isPassenger() || !player.onGround() || player.isFallFlying() || player.isSwimming()) return blocked(player);
        Vec3 start = player.position();
        Vec3 forward = horizontalForward(player);
        Vec3 destination = null;
        for (double distance = 0.25; distance <= BLINK_DISTANCE; distance += 0.25) {
            Vec3 candidate = start.add(forward.scale(distance));
            if (!clearAt(player, candidate)) break;
            if (distance >= 1.25 && supportedAt(player, candidate)) destination = candidate;
        }
        if (destination == null) return blocked(player);
        EntityTeleportEvent event = new EntityTeleportEvent(player, destination.x, destination.y, destination.z);
        if (MinecraftForge.EVENT_BUS.post(event)) return blocked(player);
        Vec3 approved = event.getTarget();
        if (!safePath(player, start, approved)) return blocked(player);
        player.connection.teleport(approved.x, approved.y, approved.z, player.getYRot(), player.getXRot());
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0;
        DandanScytheFxPacket.send(player, start.add(0, 0.08, 0), 0, 1.1F, DandanScytheFxPacket.BLINK, -1);
        DandanScytheFxPacket.send(player, approved.add(0, 0.08, 0), 0, (float) BLINK_RADIUS, DandanScytheFxPacket.BLINK, -1);
        strike(player, BLINK_RADIUS, blinkDamage(), true);
        ServerLevel level = player.serverLevel();
        for (Vec3 position : List.of(start, approved)) {
            level.playSound(null, position.x, position.y, position.z, SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.PLAYERS, 0.65F, 1.25F);
            level.sendParticles(ParticleTypes.END_ROD, position.x, position.y + 0.8, position.z, 14, 0.3, 0.65, 0.3, 0.015);
        }
        return true;
    }

    private static boolean safePath(ServerPlayer player, Vec3 start, Vec3 destination) {
        Vec3 delta = destination.subtract(start);
        if (!DandanScytheRules.validBlinkDelta(delta.x, delta.y, delta.z)) return false;
        int steps = (int) Math.ceil(delta.length() / 0.25);
        for (int step = 1; step <= steps; step++) if (!clearAt(player, start.add(delta.scale((double) step / steps)))) return false;
        return supportedAt(player, destination);
    }

    private static AABB boxAt(ServerPlayer player, Vec3 position) {
        return player.getBoundingBox().move(position.subtract(player.position()));
    }

    private static boolean clearAt(ServerPlayer player, Vec3 position) {
        ServerLevel level = player.serverLevel();
        AABB box = boxAt(player, position);
        if (box.minY < level.getMinBuildHeight() || box.maxY >= level.getMaxBuildHeight()
                || !level.getWorldBorder().isWithinBounds(box)) return false;
        for (BlockPos block : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY - 0.2, box.minZ),
                BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
            if (!level.hasChunkAt(block) || dangerous(level.getBlockState(block))) return false;
        }
        return level.noCollision(player, box) && !level.containsAnyLiquid(box);
    }

    private static boolean supportedAt(ServerPlayer player, Vec3 position) {
        AABB box = boxAt(player, position);
        return !player.serverLevel().noCollision(player,
                new AABB(box.minX + 0.05, box.minY - 0.15, box.minZ + 0.05, box.maxX - 0.05, box.minY, box.maxZ - 0.05));
    }

    private static boolean dangerous(BlockState state) {
        return state.is(BlockTags.FIRE) || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.CACTUS)
                || state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE) || state.is(Blocks.POWDER_SNOW)
                || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(Blocks.WITHER_ROSE) || !state.getFluidState().isEmpty();
    }

    private static boolean blocked(ServerPlayer player) {
        player.displayClientMessage(Component.translatable("message.ftboss.dandan_scythe.blocked").withStyle(ChatFormatting.GRAY), true);
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ftboss.dandan_scythe.mark", MARK_TICKS / 20, (int) markBonus()).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("tooltip.ftboss.dandan_scythe.sweep", SWEEP_RADIUS, (int) sweepDamage(), SWEEP_COOLDOWN / 20).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.ftboss.dandan_scythe.blink", BLINK_DISTANCE, BLINK_RADIUS, (int) blinkDamage(), BLINK_COOLDOWN / 20).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.ftboss.dandan_scythe.rules").withStyle(ChatFormatting.GRAY));
    }
}
