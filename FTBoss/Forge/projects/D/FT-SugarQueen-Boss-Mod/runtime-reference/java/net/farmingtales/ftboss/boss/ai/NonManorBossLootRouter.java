/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraftforge.event.entity.living.LivingDropsEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.block.ManorLootChestBlockEntity;
import net.farmingtales.ftboss.boss.ai.NonManorLootCrateDrops;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class NonManorBossLootRouter {
    private static final String CHEST = "ftboss:non_manor_loot_chest";
    private static final String ORIGIN = "ftboss:non_manor_loot_origin";
    private static final Map<LivingEntity, List<ItemEntity>> PENDING = new WeakHashMap<LivingEntity, List<ItemEntity>>();

    private NonManorBossLootRouter() {
    }

    public static void bind(ManorLootChestBlockEntity manorLootChestBlockEntity, LivingEntity livingEntity) {
        ServerLevel serverLevel;
        Object object;
        block8: {
            block7: {
                if (manorLootChestBlockEntity == null || livingEntity == null) {
                    return;
                }
                object = livingEntity.m_9236_();
                if (!(object instanceof ServerLevel)) break block7;
                serverLevel = (ServerLevel)object;
                if (NonManorLootCrateDrops.eligible(livingEntity) && manorLootChestBlockEntity.belongsTo(livingEntity.m_20148_())) break block8;
            }
            return;
        }
        livingEntity.getPersistentData().m_128356_(CHEST, manorLootChestBlockEntity.m_58899_().m_121878_());
        object = PENDING.remove(livingEntity);
        if (object != null) {
            Iterator iterator = object.iterator();
            while (iterator.hasNext()) {
                ItemEntity itemEntity2 = (ItemEntity)iterator.next();
                if (!itemEntity2.m_6084_()) continue;
                NonManorBossLootRouter.collect(itemEntity2, livingEntity);
            }
        }
        for (ItemEntity itemEntity2 : serverLevel.m_6443_(ItemEntity.class, livingEntity.m_20191_().m_82400_(8.0), itemEntity -> itemEntity.m_6084_() && itemEntity.getPersistentData().m_128403_(ORIGIN) && livingEntity.m_20148_().equals(itemEntity.getPersistentData().m_128342_(ORIGIN)))) {
            NonManorBossLootRouter.collect(itemEntity2, livingEntity);
        }
        FTBossMod.LOGGER.info("[FTBoss][FoodReward] bound boss {} to loot chest {}", (Object)livingEntity.m_20148_(), (Object)manorLootChestBlockEntity.m_58899_());
    }

    private static ManorLootChestBlockEntity chest(LivingEntity livingEntity) {
        ManorLootChestBlockEntity manorLootChestBlockEntity;
        ServerLevel serverLevel;
        Level level;
        block6: {
            block5: {
                level = livingEntity.m_9236_();
                if (!(level instanceof ServerLevel)) break block5;
                serverLevel = (ServerLevel)level;
                if (NonManorLootCrateDrops.eligible(livingEntity)) break block6;
            }
            return null;
        }
        level = livingEntity.getPersistentData();
        if (!level.m_128441_(CHEST)) {
            return null;
        }
        BlockPos blockPos = BlockPos.m_122022_((long)level.m_128454_(CHEST));
        if (!serverLevel.m_46805_(blockPos)) {
            return null;
        }
        BlockEntity blockEntity = serverLevel.m_7702_(blockPos);
        return blockEntity instanceof ManorLootChestBlockEntity && (manorLootChestBlockEntity = (ManorLootChestBlockEntity)blockEntity).belongsTo(livingEntity.m_20148_()) ? manorLootChestBlockEntity : null;
    }

    private static boolean collect(ItemEntity itemEntity, LivingEntity livingEntity2) {
        if (!NonManorLootCrateDrops.eligible(livingEntity2) || !(livingEntity2.m_9236_() instanceof ServerLevel)) {
            return false;
        }
        itemEntity.getPersistentData().m_128362_(ORIGIN, livingEntity2.m_20148_());
        ManorLootChestBlockEntity manorLootChestBlockEntity = NonManorBossLootRouter.chest(livingEntity2);
        if (manorLootChestBlockEntity == null) {
            List list = PENDING.computeIfAbsent(livingEntity2, livingEntity -> new ArrayList());
            if (!list.contains(itemEntity)) {
                list.add(itemEntity);
            }
            return false;
        }
        ItemStack itemStack = itemEntity.m_32055_();
        ItemStack itemStack2 = NonManorBossLootRouter.insert((Container)manorLootChestBlockEntity, itemStack);
        int n = itemStack.m_41613_() - itemStack2.m_41613_();
        if (n <= 0) {
            return false;
        }
        itemEntity.m_32045_(itemStack2);
        manorLootChestBlockEntity.m_6596_();
        FTBossMod.LOGGER.info("[FTBoss][FoodReward] collected {} boss-drop items for {}", (Object)n, (Object)livingEntity2.m_20148_());
        if (!itemStack2.m_41619_()) {
            return false;
        }
        itemEntity.m_146870_();
        return true;
    }

    public static ItemEntity spawnAtBoss(LivingEntity livingEntity, ItemStack itemStack) {
        ServerLevel serverLevel;
        Level level;
        block8: {
            block7: {
                level = livingEntity.m_9236_();
                if (!(level instanceof ServerLevel)) break block7;
                serverLevel = (ServerLevel)level;
                if (NonManorLootCrateDrops.eligible(livingEntity)) break block8;
            }
            return livingEntity.m_19983_(itemStack);
        }
        if (itemStack.m_41619_()) {
            return null;
        }
        level = new ItemEntity((Level)serverLevel, livingEntity.m_20185_(), livingEntity.m_20186_(), livingEntity.m_20189_(), itemStack);
        level.m_32060_();
        if (!NonManorBossLootRouter.collect((ItemEntity)level, livingEntity)) {
            Collection collection = livingEntity.captureDrops();
            if (collection != null) {
                collection.add(level);
            } else {
                serverLevel.m_7967_((Entity)level);
            }
        }
        return level;
    }

    public static ItemEntity spawnAtBossLike(LivingEntity livingEntity, ItemLike itemLike) {
        return NonManorBossLootRouter.spawnAtBoss(livingEntity, new ItemStack(itemLike));
    }

    public static boolean spawn(Level level, Entity entity, LivingEntity livingEntity) {
        ItemEntity itemEntity;
        if (entity instanceof ItemEntity && NonManorBossLootRouter.collect(itemEntity = (ItemEntity)entity, livingEntity)) {
            return true;
        }
        return level.m_7967_(entity);
    }

    public static boolean append(Collection<ItemEntity> collection, ItemEntity itemEntity, LivingEntity livingEntity) {
        return NonManorBossLootRouter.collect(itemEntity, livingEntity) || collection.add(itemEntity);
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onDrops(LivingDropsEvent livingDropsEvent) {
        LivingEntity livingEntity = livingDropsEvent.getEntity();
        if (!(livingEntity.m_9236_() instanceof ServerLevel) || !NonManorLootCrateDrops.eligible(livingEntity)) {
            return;
        }
        Iterator iterator = livingDropsEvent.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemEntity itemEntity = (ItemEntity)iterator.next();
            if (!itemEntity.m_32055_().m_41619_() && !NonManorBossLootRouter.collect(itemEntity, livingEntity)) continue;
            iterator.remove();
        }
    }

    static ItemStack insert(Container container, ItemStack itemStack) {
        int n;
        ItemStack itemStack2 = itemStack.m_41777_();
        for (n = 0; n < container.m_6643_() && !itemStack2.m_41619_(); ++n) {
            int n2;
            int n3;
            ItemStack itemStack3 = container.m_8020_(n);
            if (itemStack3.m_41619_() || !ItemStack.m_150942_((ItemStack)itemStack3, (ItemStack)itemStack2) || (n3 = Math.min(Math.max(0, n2 = Math.min(itemStack3.m_41741_(), container.m_6893_()) - itemStack3.m_41613_()), itemStack2.m_41613_())) == 0) continue;
            ItemStack itemStack4 = itemStack3.m_41777_();
            itemStack4.m_41769_(n3);
            container.m_6836_(n, itemStack4);
            itemStack2.m_41774_(n3);
        }
        for (n = 0; n < container.m_6643_() && !itemStack2.m_41619_(); ++n) {
            int n4;
            if (!container.m_8020_(n).m_41619_() || (n4 = Math.min(Math.min(itemStack2.m_41741_(), container.m_6893_()), itemStack2.m_41613_())) <= 0) continue;
            ItemStack itemStack5 = itemStack2.m_41777_();
            itemStack5.m_41764_(n4);
            container.m_6836_(n, itemStack5);
            itemStack2.m_41774_(n4);
        }
        return itemStack2;
    }
}

