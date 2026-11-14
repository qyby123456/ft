/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.Item
 *  net.minecraftforge.event.entity.living.LivingDropsEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.item.FTBossItems;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid="ftboss")
public final class DoraSmgDrops {
    private DoraSmgDrops() {
    }

    @SubscribeEvent
    public static void drop(LivingDropsEvent event) {
    }

    private static /* synthetic */ boolean lambda$drop$1(ItemEntity drop) {
        return drop.m_32055_().m_150930_((Item)FTBossItems.DORA_NOVA_LAUNCHER.get());
    }

    private static /* synthetic */ boolean lambda$drop$0(ItemEntity drop) {
        return "bren:machine_gun".equals(String.valueOf(ForgeRegistries.ITEMS.getKey((Object)drop.m_32055_().m_41720_())));
    }
}

