/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.block.ManorLootChestBlockEntity;
import net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public final class NonManorLootCollector {
    public static final double RADIUS = 8.0;
    public static final int MAX_AGE_TICKS = 60;

    private NonManorLootCollector() {
    }

    public static void absorb(ServerLevel serverLevel, ManorLootChestBlockEntity manorLootChestBlockEntity, LivingEntity livingEntity) {
        if (serverLevel != null && manorLootChestBlockEntity != null && livingEntity != null) {
            NonManorBossLootRouter.bind(manorLootChestBlockEntity, livingEntity);
        }
    }

    static ItemStack insert(ManorLootChestBlockEntity manorLootChestBlockEntity, ItemStack itemStack) {
        ItemStack itemStack2;
        int n;
        int n2 = manorLootChestBlockEntity.m_6643_();
        ItemStack itemStack3 = itemStack;
        for (n = 0; n < n2 && !itemStack3.m_41619_(); ++n) {
            int n3;
            int n4;
            itemStack2 = manorLootChestBlockEntity.m_8020_(n);
            if (itemStack2.m_41619_() || !ItemStack.m_150942_((ItemStack)itemStack2, (ItemStack)itemStack3) || (n4 = (n3 = Math.min(itemStack2.m_41776_(), manorLootChestBlockEntity.m_6893_())) - itemStack2.m_41613_()) <= 0) continue;
            int n5 = Math.min(n4, itemStack3.m_41613_());
            itemStack2.m_41769_(n5);
            itemStack3.m_41774_(n5);
            manorLootChestBlockEntity.m_6836_(n, itemStack2);
        }
        for (n = 0; n < n2 && !itemStack3.m_41619_(); ++n) {
            if (!manorLootChestBlockEntity.m_8020_(n).m_41619_()) continue;
            itemStack2 = itemStack3.m_41777_();
            manorLootChestBlockEntity.m_6836_(n, itemStack2);
            itemStack3 = ItemStack.f_41583_;
        }
        return itemStack3;
    }

    private static /* synthetic */ boolean lambda$absorb$0(ItemEntity itemEntity) {
        return itemEntity.m_6084_() && itemEntity.f_19797_ <= 60 && itemEntity.m_19749_() == null;
    }
}

