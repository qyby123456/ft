package net.farmingtales.ftboss.item.weapons;

import java.util.List;
import java.util.function.Consumer;
import net.farmingtales.ftboss.client.render.GourmetBlankBladeRenderer;
import net.farmingtales.ftboss.api.OptionalWeaponModule;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Pre;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class GourmetBlankBladeItem extends Item implements GeoItem, GourmetEvolutionWeapon {
   private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.gourmet_blank_blade.idle");
   private static final RawAnimation EVOLVE = RawAnimation.begin().thenPlay("animation.gourmet_blank_blade.evolve");
   private static final RawAnimation SOCKET = RawAnimation.begin().thenPlay("animation.gourmet_blank_blade.socket");
   private static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.gourmet_blank_blade.attack");
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

   public GourmetBlankBladeItem() {
      super(new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant());
      GeoItem.registerSyncedAnimatable(this);
   }

   public void registerControllers(ControllerRegistrar var1) {
      var1.add(
         new AnimationController(this, "blade", 2, var0 -> var0.setAndContinue(IDLE))
            .triggerableAnim("evolve", EVOLVE)
            .triggerableAnim("socket", SOCKET)
            .triggerableAnim("attack", ATTACK)
      );
   }

   public static void animateEvolution(ServerPlayer var0, ItemStack var1) {
      if (var1.getItem() instanceof GourmetBlankBladeItem var2) {
         var2.triggerAnim(var0, GeoItem.getOrAssignId(var1, var0.serverLevel()), "blade", "evolve");
      }
   }

   public static void animateSocket(ServerPlayer var0, ItemStack var1) {
      if (var1.getItem() instanceof GourmetBlankBladeItem var2) {
         var2.triggerAnim(var0, GeoItem.getOrAssignId(var1, var0.serverLevel()), "blade", "socket");
      }
   }

   public static void animateAttack(ServerPlayer var0, ItemStack var1) {
      if (var1.getItem() instanceof GourmetBlankBladeItem var2) {
         var2.triggerAnim(var0, GeoItem.getOrAssignId(var1, var0.serverLevel()), "blade", "attack");
      }
   }

   public void triggerEvolution(ServerPlayer var1, ItemStack var2) {
      this.triggerAnim(var1, GeoItem.getOrAssignId(var2, var1.serverLevel()), "blade", "evolve");
   }

   public void triggerSocket(ServerPlayer var1, ItemStack var2) {
      this.triggerAnim(var1, GeoItem.getOrAssignId(var2, var1.serverLevel()), "blade", "socket");
   }

   public void triggerActive(ServerPlayer var1, ItemStack var2) {
      this.triggerAnim(var1, GeoItem.getOrAssignId(var2, var1.serverLevel()), "blade", "evolve");
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   public boolean isEnchantable(ItemStack var1) {
      return var1.getCount() == 1;
   }

   public int getEnchantmentValue() {
      return 22;
   }

   public boolean isFoil(ItemStack var1) {
      return true;
   }

   public void inventoryTick(ItemStack var1, Level var2, Entity var3, int var4, boolean var5) {
      super.inventoryTick(var1, var2, var3, var4, var5);
      if (var5 && var2 instanceof ServerLevel var6) {
         if (var3 instanceof ServerPlayer var7 && var7.swinging && var7.swingTime == 1 && !var7.isUsingItem()) {
            animateAttack(var7, var1);
         }

         if (var3.tickCount % 12 == 0) {
            int var8 = OptionalWeaponModule.evolutionLevel(var1);
            var6.sendParticles(
               var8 > 0 ? ParticleTypes.END_ROD : ParticleTypes.ENCHANT,
               var3.getX(),
               var3.getY() + 0.9,
               var3.getZ(),
               1,
               0.22 + (double)var8 * 0.015,
               0.3,
               0.22 + (double)var8 * 0.015,
               0.01
            );
         }
      }
   }

   public boolean onEntityItemUpdate(ItemStack var1, ItemEntity var2) {
      if (var2.level() instanceof ServerLevel var3 && var2.getAge() % 6 == 0) {
         int var5 = OptionalWeaponModule.evolutionLevel(var1);
         var3.sendParticles(
            var5 > 0 ? ParticleTypes.END_ROD : ParticleTypes.ENCHANT, var2.getX(), var2.getY() + 0.28, var2.getZ(), 1 + var5 / 3, 0.18, 0.22, 0.18, 0.015
         );
      }

      return false;
   }

   public static void playPickupEffect(Pre var0) {
      if (var0.getItemEntity().getItem().getItem() instanceof GourmetEvolutionWeapon && var0.getPlayer().level() instanceof ServerLevel var1) {
         ItemEntity var3 = var0.getItemEntity();
         var1.sendParticles(ParticleTypes.END_ROD, var3.getX(), var3.getY() + 0.45, var3.getZ(), 26, 0.45, 0.55, 0.45, 0.09);
         var1.sendParticles(ParticleTypes.ENCHANT, var3.getX(), var3.getY() + 0.45, var3.getZ(), 36, 0.55, 0.65, 0.55, 0.18);
         var1.playSound(
            null, var3.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.85F, 0.8F + var0.getPlayer().getRandom().nextFloat() * 0.35F
         );
      }
   }

   public void appendHoverText(ItemStack var1, TooltipContext var2, List<Component> var3, TooltipFlag var4) {
      super.appendHoverText(var1, var2, var3, var4);
      var3.add(Component.translatable("tooltip.ftboss.gourmet.zero_base").withStyle(ChatFormatting.GRAY));
      var3.add(Component.translatable("tooltip.ftboss.gourmet.capacity", new Object[]{30, 8}).withStyle(ChatFormatting.AQUA));
      var3.add(Component.translatable("tooltip.ftboss.gourmet.use").withStyle(ChatFormatting.DARK_GRAY));
   }

   public void initializeClient(Consumer<IClientItemExtensions> var1) {
      var1.accept(new IClientItemExtensions() {
         private BlockEntityWithoutLevelRenderer renderer;

         public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (this.renderer == null) {
               this.renderer = new GourmetBlankBladeRenderer();
            }

            return this.renderer;
         }
      });
   }

   public boolean supportsEnchantment(ItemStack var1, Holder var2) {
      return GourmetBladeEnchantments.allowed(var1, var2);
   }

   public boolean isPrimaryItemFor(ItemStack var1, Holder var2) {
      return GourmetBladeEnchantments.allowed(var1, var2);
   }

   public boolean isBookEnchantable(ItemStack var1, ItemStack var2) {
      return GourmetBladeEnchantments.book(var1, var2);
   }
}
