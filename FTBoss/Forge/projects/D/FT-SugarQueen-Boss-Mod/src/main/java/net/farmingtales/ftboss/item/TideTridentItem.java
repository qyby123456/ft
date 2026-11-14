package net.farmingtales.ftboss.item;

import java.util.List;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.TideBoltEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TideTridentItem extends SwordItem {
    public TideTridentItem(Properties properties) { super(Tiers.NETHERITE, 5, -2.6f, properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        if (!(level instanceof ServerLevel server)) return InteractionResultHolder.success(stack);
        boolean whirlpool = player.isShiftKeyDown();
        if (whirlpool) {
            for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(5))) {
                if (target == player || !target.isAlive() || player.isAlliedTo(target) || !player.hasLineOfSight(target)
                        || player.distanceToSqr(target) > 25) continue;
                if (target instanceof Player other && (!BossTargets.valid(other) || !player.canHarmPlayer(other))) continue;
                if (target.hurt(player.damageSources().playerAttack(player), 16)) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
                    Vec3 pull = player.position().subtract(target.position()).multiply(1, 0, 1).normalize().scale(0.35);
                    target.push(pull.x, 0.1, pull.z);
                    target.hurtMarked = true;
                }
            }
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 200, 0));
            for (int index = 0; index < 72; index++) {
                double angle = index * Math.PI * 2 / 72;
                server.sendParticles(ParticleTypes.SPLASH, player.getX() + Math.cos(angle) * 5,
                        player.getY() + 0.4, player.getZ() + Math.sin(angle) * 5, 2, 0, 0.15, 0, 0.05);
            }
        } else {
            for (int index = -1; index <= 1; index++) {
                TideBoltEntity bolt = new TideBoltEntity(FTEntities.TIDE_BOLT.get(), server);
                bolt.setOwner(player);
                bolt.setDamage(12);
                bolt.setPos(player.getX(), player.getEyeY() - 0.15, player.getZ());
                Vec3 aim = player.getLookAngle().yRot(index * 0.1f);
                bolt.shoot(aim.x, aim.y, aim.z, 1.65f, 0);
                server.addFreshEntity(bolt);
            }
        }
        player.getCooldowns().addCooldown(this, whirlpool ? 200 : 80);
        stack.hurtAndBreak(whirlpool ? 3 : 1, player, owner -> owner.broadcastBreakEvent(hand));
        server.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_1, SoundSource.PLAYERS, 0.9f, 1.1f);
        player.swing(hand, true);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ftboss.tide_trident"));
        tooltip.add(Component.translatable("tooltip.ftboss.tide_trident_shift"));
    }
}
