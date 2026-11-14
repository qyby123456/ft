package net.farmingtales.ftboss.item.weapons;

import java.util.List;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

/** Shared runtime contract and lightweight effects for the three creative Gourmet evolution weapons. */
public interface GourmetEvolutionWeapon {
    void triggerEvolution(ServerPlayer holder, ItemStack stack);
    void triggerSocket(ServerPlayer holder, ItemStack stack);
    void triggerActive(ServerPlayer holder, ItemStack stack);

    static void animateEvolution(ServerPlayer holder, ItemStack stack) {
        if (stack.getItem() instanceof GourmetEvolutionWeapon weapon) weapon.triggerEvolution(holder, stack);
    }

    static void animateSocket(ServerPlayer holder, ItemStack stack) {
        if (stack.getItem() instanceof GourmetEvolutionWeapon weapon) weapon.triggerSocket(holder, stack);
    }

    static void animateActive(ServerPlayer holder, ItemStack stack) {
        if (stack.getItem() instanceof GourmetEvolutionWeapon weapon) weapon.triggerActive(holder, stack);
    }

    static void inventoryParticles(ItemStack stack, Level level, Entity holder, boolean selected) {
        if (!selected || !(level instanceof ServerLevel server) || holder.tickCount % 12 != 0) return;
        int evolution = FoodWeaponEvolution.evolutionLevel(stack);
        server.sendParticles(evolution > 0 ? ParticleTypes.END_ROD : ParticleTypes.ENCHANT,
                holder.getX(), holder.getY() + 0.9, holder.getZ(), 1,
                0.22 + evolution * 0.015, 0.3, 0.22 + evolution * 0.015, 0.01);
    }

    static void droppedParticles(ItemStack stack, ItemEntity entity) {
        if (!(entity.level() instanceof ServerLevel server) || entity.getAge() % 6 != 0) return;
        int evolution = FoodWeaponEvolution.evolutionLevel(stack);
        server.sendParticles(evolution > 0 ? ParticleTypes.END_ROD : ParticleTypes.ENCHANT,
                entity.getX(), entity.getY() + 0.28, entity.getZ(), 1 + evolution / 3,
                0.18, 0.22, 0.18, 0.015);
    }

    static void playPickupEffect(EntityItemPickupEvent event) {
        if (event.isCanceled() || !(event.getItem().getItem().getItem() instanceof GourmetEvolutionWeapon)
                || !(event.getEntity().level() instanceof ServerLevel level)) return;
        ItemEntity item = event.getItem();
        level.sendParticles(ParticleTypes.END_ROD, item.getX(), item.getY() + 0.45, item.getZ(),
                26, 0.45, 0.55, 0.45, 0.09);
        level.sendParticles(ParticleTypes.ENCHANT, item.getX(), item.getY() + 0.45, item.getZ(),
                36, 0.55, 0.65, 0.55, 0.18);
        level.playSound(null, item.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 0.85F, 0.8F + event.getEntity().getRandom().nextFloat() * 0.35F);
    }

    static void appendSharedTooltip(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.ftboss.gourmet.creative_only")
                .withStyle(ChatFormatting.DARK_PURPLE));
        lines.add(Component.translatable("tooltip.ftboss.gourmet.zero_base")
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.ftboss.gourmet.capacity",
                FoodWeaponEvolution.GOURMET_SOCKET_CAPACITY, FoodWeaponEvolution.GOURMET_MAX_LEVEL)
                .withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable("tooltip.ftboss.gourmet.use").withStyle(ChatFormatting.DARK_GRAY));
    }
}
