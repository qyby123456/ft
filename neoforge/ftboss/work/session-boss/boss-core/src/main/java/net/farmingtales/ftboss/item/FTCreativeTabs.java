package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.block.FTBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FTCreativeTabs {
   public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "ftboss");
   private static final ResourceKey<CreativeModeTab> SPAWN_EGGS = ResourceKey.create(
      Registries.CREATIVE_MODE_TAB, ResourceLocation.withDefaultNamespace("spawn_eggs")
   );
   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register(
      "main",
      () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ftboss.main"))
            .icon(() -> new ItemStack((ItemLike)FTBossItems.SUGAR_QUEEN_CRYSTAL.get()))
            .withTabsBefore(new ResourceKey[]{SPAWN_EGGS})
            .withSearchBar()
            .displayItems((var0, var1) -> {
               var1.accept((ItemLike)FTBossItems.MANOR_STOP_TOKEN.get());
               var1.accept((ItemLike)FTBossItems.SUGAR_QUEEN_CRYSTAL.get());
               var1.accept((ItemLike)FTBossItems.TIDE_BANQUET.get());
               var1.accept((ItemLike)FTBossItems.MEAT_HEART.get());
               var1.accept((ItemLike)FTBossItems.TEA_SEED.get());
               var1.accept((ItemLike)FTBossItems.WINE_CHARM.get());
               var1.accept((ItemLike)FTBossItems.RAT_BAIT.get());
               var1.accept((ItemLike)FTBossItems.DEFECT_CORE.get());
               var1.accept((ItemLike)FTBossItems.COMPOST_SUMMON.get());
               var1.accept((ItemLike)FTBossItems.FORBIDDEN_APPLE.get());
               var1.accept((ItemLike)FTBossItems.BENGER_WHISTLE.get());
               var1.accept((ItemLike)FTBossItems.GOURMET_BLANK_BLADE.get());
               var1.accept((ItemLike)FTBossItems.DEFECT_CLAW.get());
               var1.accept((ItemLike)FTBossItems.TURKEY_LEGS.get());
               var1.accept((ItemLike)FTBossItems.WINE_BOTTLE_MACE.get());
               var1.accept((ItemLike)FTBossItems.CHEESE_WHEEL_MACE.get());
               var1.accept((ItemLike)FTBossItems.COMPOST_CROWN.get());
               var1.accept((ItemLike)FTBossItems.CANDY_CANE.get());
               var1.accept((ItemLike)FTBossItems.BLOOMING_THORNS.get());
               var1.accept((ItemLike)FTBossItems.THORN_CROWN.get());
               var1.accept((ItemLike)FTBossItems.THORN_CHESTPLATE.get());
               var1.accept((ItemLike)FTBossItems.THORN_LEGGINGS.get());
               var1.accept((ItemLike)FTBossItems.THORN_BOOTS.get());
               var1.accept((ItemLike)FTBossItems.SILVER_KEY.get());
               var1.accept((ItemLike)FTBossItems.FOOD_APPRAISAL_MONOCLE.get());
               var1.accept((ItemLike)FTBossItems.APPLE_STEM.get());
               var1.accept((ItemLike)FTBossItems.TIDE_TRIDENT.get());
               BossRelicItems.addTo(var1);
               BossMaterialDrops.addTo(var1);
               var1.accept((ItemLike)FTBossItems.DOG_BEATING_STICK.get());
               ManorWeaponCatalog.addTo(var1);
               var1.accept((ItemLike)FTBossItems.QINGYI_CONFLUENCE_TRIDENT.get());
               var1.accept((ItemLike)FTBlocks.MANOR_LOOT_ITEM.get());
               var1.accept((ItemLike)FTBossItems.BARREL_CROSSBOW.get());
               var1.accept((ItemLike)FTBossItems.BARREL_CROSSBOW_READY.get());
               var1.accept(((Block)FTBlocks.STATUE_SUGAR_QUEEN.get()).asItem());
               var1.accept(((Block)FTBlocks.STATUE_BUTCHER.get()).asItem());
               var1.accept(((Block)FTBlocks.STATUE_SAYA.get()).asItem());
               var1.accept(((Block)FTBlocks.STATUE_WINE_BARREL.get()).asItem());
               var1.accept(((Block)FTBlocks.STATUE_RAT_SWARM.get()).asItem());
               var1.accept(((Block)FTBlocks.STATUE_DEFECT.get()).asItem());
               var1.accept(((Block)FTBlocks.STATUE_COMPOST.get()).asItem());
               var1.accept(((Block)FTBlocks.STATUE_APPLE.get()).asItem());
               var1.accept(((Block)FTBlocks.STATUE_TIDE_FISH.get()).asItem());
               var1.accept(((Block)FTBlocks.STATUE_BENGER.get()).asItem());
               var1.accept((ItemLike)FTBossItems.TIDE_PEARL.get());
               var1.accept((ItemLike)FTBossItems.BENGER_SPAWN_EGG.get());

               for (DeferredHolder var3 : FTBossItems.EASTER_EGGS) {
                  var1.accept((ItemLike)var3.get());
               }

               var1.accept((ItemLike)FTBossItems.MUSIC_DISC_MANOR_CUE.get());
               var1.accept((ItemLike)FTBossItems.MUSIC_DISC_MANOR_BATTLE.get());
               var1.accept((ItemLike)FTBossItems.MUSIC_DISC_MANOR_DUAL.get());
               var1.accept((ItemLike)FTBossItems.MUSIC_DISC_MANOR_GUIGNOL.get());
               var1.accept((ItemLike)FTBossItems.MUSIC_DISC_BENGER.get());

               for (DeferredHolder var5 : FTBossItems.FOOD_MUSIC_DISCS) {
                  var1.accept((ItemLike)var5.get());
               }
            })
            .build()
   );

   private FTCreativeTabs() {
   }

   public static void register(IEventBus var0) {
      TABS.register(var0);
   }
}
