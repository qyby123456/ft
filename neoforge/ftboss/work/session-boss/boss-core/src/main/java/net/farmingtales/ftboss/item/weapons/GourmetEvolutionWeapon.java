package net.farmingtales.ftboss.item.weapons;

import java.util.List;
import net.farmingtales.ftboss.api.OptionalWeaponModule;
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
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Pre;

public interface GourmetEvolutionWeapon {
   void triggerEvolution(ServerPlayer var1, ItemStack var2);

   void triggerSocket(ServerPlayer var1, ItemStack var2);

   void triggerActive(ServerPlayer var1, ItemStack var2);

   static void animateEvolution(ServerPlayer var0, ItemStack var1) {
      if (var1.getItem() instanceof GourmetEvolutionWeapon var2) {
         var2.triggerEvolution(var0, var1);
      }
   }

   static void animateSocket(ServerPlayer var0, ItemStack var1) {
      if (var1.getItem() instanceof GourmetEvolutionWeapon var2) {
         var2.triggerSocket(var0, var1);
      }
   }

   static void animateActive(ServerPlayer var0, ItemStack var1) {
      if (var1.getItem() instanceof GourmetEvolutionWeapon var2) {
         var2.triggerActive(var0, var1);
      }
   }

   static void inventoryParticles(ItemStack var0, Level var1, Entity var2, boolean var3) {
      if (var3 && var1 instanceof ServerLevel var4 && var2.tickCount % 12 == 0) {
         int var5 = OptionalWeaponModule.evolutionLevel(var0);
         var4.sendParticles(
            var5 > 0 ? ParticleTypes.END_ROD : ParticleTypes.ENCHANT,
            var2.getX(),
            var2.getY() + 0.9,
            var2.getZ(),
            1,
            0.22 + (double)var5 * 0.015,
            0.3,
            0.22 + (double)var5 * 0.015,
            0.01
         );
         return;
      }
   }

   static void droppedParticles(ItemStack var0, ItemEntity var1) {
      if (var1.level() instanceof ServerLevel var2 && var1.getAge() % 6 == 0) {
         int var4 = OptionalWeaponModule.evolutionLevel(var0);
         var2.sendParticles(
            var4 > 0 ? ParticleTypes.END_ROD : ParticleTypes.ENCHANT, var1.getX(), var1.getY() + 0.28, var1.getZ(), 1 + var4 / 3, 0.18, 0.22, 0.18, 0.015
         );
         return;
      }
   }

   static void playPickupEffect(Pre var0) {
      if (var0.getItemEntity().getItem().getItem() instanceof GourmetEvolutionWeapon && var0.getPlayer().level() instanceof ServerLevel var1) {
         ItemEntity var3 = var0.getItemEntity();
         var1.sendParticles(ParticleTypes.END_ROD, var3.getX(), var3.getY() + 0.45, var3.getZ(), 26, 0.45, 0.55, 0.45, 0.09);
         var1.sendParticles(ParticleTypes.ENCHANT, var3.getX(), var3.getY() + 0.45, var3.getZ(), 36, 0.55, 0.65, 0.55, 0.18);
         var1.playSound(
            null, var3.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.85F, 0.8F + var0.getPlayer().getRandom().nextFloat() * 0.35F
         );
      }
   }

   static void appendSharedTooltip(ItemStack var0, Level var1, List<Component> var2, TooltipFlag var3) {
      var2.add(Component.translatable("tooltip.ftboss.gourmet.creative_only").withStyle(ChatFormatting.DARK_PURPLE));
      var2.add(Component.translatable("tooltip.ftboss.gourmet.zero_base").withStyle(ChatFormatting.GRAY));
      var2.add(Component.translatable("tooltip.ftboss.gourmet.capacity", new Object[]{30, 8}).withStyle(ChatFormatting.AQUA));
      var2.add(Component.translatable("tooltip.ftboss.gourmet.use").withStyle(ChatFormatting.DARK_GRAY));
   }
}
