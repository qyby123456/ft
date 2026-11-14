/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.boss.ai.FoodBossInventoryRewards;
import net.farmingtales.ftboss.boss.ai.NonManorLootCoverage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class NonManorLootCrateDrops {
    public static final String CRATE_TYPE = "ftboss_non_manor";
    private static final ResourceLocation LOOT_CRATE_ITEM = new ResourceLocation("ftbquests", "lootcrate");
    private static final String REWARDED_TAG = "ftboss:non_manor_loot_crate_rewarded";
    private static boolean missingItemLogged;

    private NonManorLootCrateDrops() {
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onBossDeath(LivingDeathEvent livingDeathEvent) {
        FoodBossInventoryRewards.onBossDeath(livingDeathEvent);
    }

    static boolean eligible(LivingEntity livingEntity) {
        return NonManorLootCoverage.eligible(livingEntity);
    }
}

