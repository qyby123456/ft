/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.CreativeModeTabs
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.RegistryObject
 */
package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.block.FTBlocks;
import net.farmingtales.ftboss.item.BossRelicItems;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.item.ManorWeaponCatalog;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class FTCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create((ResourceKey)Registries.f_279569_, (String)"ftboss");
    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder().m_257941_((Component)Component.m_237115_((String)"itemGroup.ftboss.main")).m_257737_(() -> new ItemStack((ItemLike)FTBossItems.SUGAR_QUEEN_CRYSTAL.get())).withTabsBefore(new ResourceKey[]{CreativeModeTabs.f_256731_}).withSearchBar().m_257501_((params, out) -> {
        out.m_246326_((ItemLike)FTBossItems.MANOR_STOP_TOKEN.get());
        out.m_246326_((ItemLike)FTBossItems.SUGAR_QUEEN_CRYSTAL.get());
        out.m_246326_((ItemLike)FTBossItems.TIDE_BANQUET.get());
        out.m_246326_((ItemLike)FTBossItems.MEAT_HEART.get());
        out.m_246326_((ItemLike)FTBossItems.TEA_SEED.get());
        out.m_246326_((ItemLike)FTBossItems.WINE_CHARM.get());
        out.m_246326_((ItemLike)FTBossItems.RAT_BAIT.get());
        out.m_246326_((ItemLike)FTBossItems.DEFECT_CORE.get());
        out.m_246326_((ItemLike)FTBossItems.COMPOST_SUMMON.get());
        out.m_246326_((ItemLike)FTBossItems.FORBIDDEN_APPLE.get());
        out.m_246326_((ItemLike)FTBossItems.BENGER_WHISTLE.get());
        out.m_246326_((ItemLike)FTBossItems.GOURMET_BLANK_BLADE.get());
        out.m_246326_((ItemLike)FTBossItems.DEFECT_CLAW.get());
        out.m_246326_((ItemLike)FTBossItems.TURKEY_LEGS.get());
        out.m_246326_((ItemLike)FTBossItems.WINE_BOTTLE_MACE.get());
        out.m_246326_((ItemLike)FTBossItems.CHEESE_WHEEL_MACE.get());
        out.m_246326_((ItemLike)FTBossItems.COMPOST_CROWN.get());
        out.m_246326_((ItemLike)FTBossItems.CANDY_CANE.get());
        out.m_246326_((ItemLike)FTBossItems.BLOOMING_THORNS.get());
        out.m_246326_((ItemLike)FTBossItems.THORN_CROWN.get());
        out.m_246326_((ItemLike)FTBossItems.THORN_CHESTPLATE.get());
        out.m_246326_((ItemLike)FTBossItems.THORN_LEGGINGS.get());
        out.m_246326_((ItemLike)FTBossItems.THORN_BOOTS.get());
        out.m_246326_((ItemLike)FTBossItems.SILVER_KEY.get());
        out.m_246326_((ItemLike)FTBossItems.FOOD_APPRAISAL_MONOCLE.get());
        out.m_246326_((ItemLike)FTBossItems.APPLE_STEM.get());
        out.m_246326_((ItemLike)FTBossItems.TIDE_TRIDENT.get());
        BossRelicItems.addTo(out);
        out.m_246326_((ItemLike)FTBossItems.DOG_BEATING_STICK.get());
        ManorWeaponCatalog.addTo(out);
        out.m_246326_((ItemLike)FTBossItems.QINGYI_CONFLUENCE_TRIDENT.get());
        out.m_246326_((ItemLike)FTBlocks.MANOR_LOOT_ITEM.get());
        out.m_246326_((ItemLike)FTBossItems.BARREL_CROSSBOW.get());
        out.m_246326_((ItemLike)FTBossItems.BARREL_CROSSBOW_READY.get());
        out.m_246326_((ItemLike)((Block)FTBlocks.STATUE_SUGAR_QUEEN.get()).m_5456_());
        out.m_246326_((ItemLike)((Block)FTBlocks.STATUE_BUTCHER.get()).m_5456_());
        out.m_246326_((ItemLike)((Block)FTBlocks.STATUE_SAYA.get()).m_5456_());
        out.m_246326_((ItemLike)((Block)FTBlocks.STATUE_WINE_BARREL.get()).m_5456_());
        out.m_246326_((ItemLike)((Block)FTBlocks.STATUE_RAT_SWARM.get()).m_5456_());
        out.m_246326_((ItemLike)((Block)FTBlocks.STATUE_DEFECT.get()).m_5456_());
        out.m_246326_((ItemLike)((Block)FTBlocks.STATUE_COMPOST.get()).m_5456_());
        out.m_246326_((ItemLike)((Block)FTBlocks.STATUE_APPLE.get()).m_5456_());
        out.m_246326_((ItemLike)((Block)FTBlocks.STATUE_TIDE_FISH.get()).m_5456_());
        out.m_246326_((ItemLike)((Block)FTBlocks.STATUE_BENGER.get()).m_5456_());
        out.m_246326_((ItemLike)FTBossItems.TIDE_PEARL.get());
        out.m_246326_((ItemLike)FTBossItems.BENGER_SPAWN_EGG.get());
        for (RegistryObject<Item> egg : FTBossItems.EASTER_EGGS) {
            out.m_246326_((ItemLike)egg.get());
        }
        out.m_246326_((ItemLike)FTBossItems.MUSIC_DISC_MANOR_CUE.get());
        out.m_246326_((ItemLike)FTBossItems.MUSIC_DISC_MANOR_BATTLE.get());
        out.m_246326_((ItemLike)FTBossItems.MUSIC_DISC_MANOR_DUAL.get());
        out.m_246326_((ItemLike)FTBossItems.MUSIC_DISC_MANOR_GUIGNOL.get());
        out.m_246326_((ItemLike)FTBossItems.MUSIC_DISC_BENGER.get());
        for (RegistryObject<Item> foodDisc : FTBossItems.FOOD_MUSIC_DISCS) {
            out.m_246326_((ItemLike)foodDisc.get());
        }
    }).m_257652_());

    private FTCreativeTabs() {
    }

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}

