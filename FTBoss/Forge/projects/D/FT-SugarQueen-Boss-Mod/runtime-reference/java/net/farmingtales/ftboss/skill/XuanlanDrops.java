/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.Item
 *  net.minecraftforge.event.entity.living.LivingDropsEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.item.FTBossItems;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class XuanlanDrops {
    private XuanlanDrops() {
    }

    @SubscribeEvent
    public static void drop(LivingDropsEvent event) {
    }

    private static /* synthetic */ boolean lambda$drop$0(ItemEntity drop) {
        return drop.m_32055_().m_150930_((Item)FTBossItems.WENXUANLAN_FLYWHEEL.get());
    }
}

