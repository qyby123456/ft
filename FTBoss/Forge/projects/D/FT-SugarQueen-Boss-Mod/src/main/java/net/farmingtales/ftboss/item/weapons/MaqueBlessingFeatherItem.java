package net.farmingtales.ftboss.item.weapons;

import net.farmingtales.ftboss.boss.ai.HealBan;
import net.farmingtales.ftboss.skill.MaqueFeatherPacket;
import net.farmingtales.ftboss.util.MaqueFeatherRules;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public final class MaqueBlessingFeatherItem extends Item {
    private static final String COOLDOWN = "ftboss.maque_feather.cooldown";
    private static final String LAST_HELD = "ftboss.maque_feather.last_held";
    private static final String NEXT_PULSE = "ftboss.maque_feather.next_pulse";
    private static final List<MobEffect> CLEANSABLE = List.of(MobEffects.POISON, MobEffects.WITHER,
            MobEffects.WEAKNESS, MobEffects.MOVEMENT_SLOWDOWN, MobEffects.BLINDNESS);

    public MaqueBlessingFeatherItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    private static long clock(ServerPlayer player) {
        return player.serverLevel().getServer().overworld().getGameTime();
    }

    private static ItemStack heldFeather(Player player) {
        if (player.getMainHandItem().getItem() instanceof MaqueBlessingFeatherItem) return player.getMainHandItem();
        if (player.getOffhandItem().getItem() instanceof MaqueBlessingFeatherItem) return player.getOffhandItem();
        return ItemStack.EMPTY;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()
                || stack != heldFeather(player)) return;
        long now = clock(player);
        CompoundTag data = player.getPersistentData();
        int remaining = MaqueFeatherRules.remainingCooldown(now, data.getLong(COOLDOWN));
        if (remaining > 0 && !player.getCooldowns().isOnCooldown(this)) player.getCooldowns().addCooldown(this, remaining);
        if (data.contains(LAST_HELD) && data.getLong(LAST_HELD) == now) return;
        if (!data.contains(LAST_HELD) || !MaqueFeatherRules.continuousHold(now, data.getLong(LAST_HELD))) {
            data.putLong(NEXT_PULSE, now + MaqueFeatherRules.PASSIVE_INTERVAL_TICKS);
        }
        data.putLong(LAST_HELD, now);
        if (now < data.getLong(NEXT_PULSE)) return;
        data.putLong(NEXT_PULSE, now + MaqueFeatherRules.PASSIVE_INTERVAL_TICKS);
        if (player.getHealth() < player.getMaxHealth() && !HealBan.isBanned(player)) {
            player.heal(MaqueFeatherRules.passiveHeal());
            player.serverLevel().sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0,
                    player.getZ(), 3, 0.22, 0.28, 0.22, 0.01);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isAlive() || player.isSpectator()) return InteractionResultHolder.fail(stack);
        if (level.isClientSide) return InteractionResultHolder.sidedSuccess(stack, true);
        if (!(player instanceof ServerPlayer caster)) return InteractionResultHolder.fail(stack);
        long now = clock(caster);
        int remaining = MaqueFeatherRules.remainingCooldown(now, caster.getPersistentData().getLong(COOLDOWN));
        if (remaining > 0 || player.getCooldowns().isOnCooldown(this)) {
            if (remaining > 0) player.getCooldowns().addCooldown(this, remaining);
            caster.displayClientMessage(Component.translatable("message.ftboss.maque_feather.cooldown",
                    Math.max(1, (remaining + 19) / 20)), true);
            return InteractionResultHolder.fail(stack);
        }
        caster.getPersistentData().putLong(COOLDOWN, now + MaqueFeatherRules.COOLDOWN_TICKS);
        player.getCooldowns().addCooldown(this, MaqueFeatherRules.COOLDOWN_TICKS);
        boolean wind = player.isShiftKeyDown();
        if (wind) {
            caster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, MaqueFeatherRules.WIND_TICKS, 1));
            caster.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, MaqueFeatherRules.WIND_TICKS, 0));
            caster.fallDistance = 0;
            caster.displayClientMessage(Component.translatable("message.ftboss.maque_feather.wind"), true);
        } else {
            ServerLevel server = caster.serverLevel();
            List<LivingEntity> targets = server.getEntitiesOfClass(LivingEntity.class,
                    caster.getBoundingBox().inflate(MaqueFeatherRules.RADIUS), target -> target != caster && canSupport(caster, target));
            targets.sort(Comparator.comparingDouble(caster::distanceToSqr));
            bless(server, caster);
            for (LivingEntity target : targets.stream().limit(MaqueFeatherRules.MAX_TARGETS - 1).toList()) bless(server, target);
            caster.displayClientMessage(Component.translatable("message.ftboss.maque_feather.blessing",
                    Math.min(targets.size() + 1, MaqueFeatherRules.MAX_TARGETS)), true);
        }
        MaqueFeatherPacket.send(caster, wind);
        level.playSound(null, caster.blockPosition(), wind ? SoundEvents.ELYTRA_FLYING : SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 0.7F, wind ? 1.3F : 1.15F);
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    private static boolean canSupport(ServerPlayer caster, LivingEntity target) {
        boolean alliedPlayer = target instanceof Player && caster.getTeam() != null && caster.isAlliedTo(target);
        boolean ownedPet = target instanceof TamableAnimal pet && pet.isTame() && caster.getUUID().equals(pet.getOwnerUUID());
        return MaqueFeatherRules.canSupport(target == caster, target.isAlive(), target.isSpectator(), alliedPlayer,
                ownedPet, caster.distanceToSqr(target), target == caster || caster.hasLineOfSight(target));
    }

    private static void bless(ServerLevel level, LivingEntity target) {
        for (MobEffect effect : CLEANSABLE) target.removeEffect(effect);
        if (!HealBan.isBanned(target)) target.heal(MaqueFeatherRules.burstHeal());
        target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, MaqueFeatherRules.BLESSING_TICKS, 1));
        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, MaqueFeatherRules.BLESSING_TICKS, 0));
        level.sendParticles(ParticleTypes.HEART, target.getX(), target.getY() + target.getBbHeight() + 0.2,
                target.getZ(), 3, 0.3, 0.15, 0.3, 0.01);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ftboss.maque_feather.passive", MaqueFeatherRules.PASSIVE_INTERVAL_TICKS / 20)
                .withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("tooltip.ftboss.maque_feather.blessing", (int) MaqueFeatherRules.RADIUS,
                (int) (MaqueFeatherRules.burstHeal() / 2), MaqueFeatherRules.BLESSING_TICKS / 20).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.ftboss.maque_feather.cleanse").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ftboss.maque_feather.wind", MaqueFeatherRules.WIND_TICKS / 20)
                .withStyle(ChatFormatting.WHITE));
        tooltip.add(Component.translatable("tooltip.ftboss.maque_feather.cooldown", MaqueFeatherRules.COOLDOWN_TICKS / 20)
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.ftboss.maque_feather.allies").withStyle(ChatFormatting.DARK_GRAY));
    }
}
