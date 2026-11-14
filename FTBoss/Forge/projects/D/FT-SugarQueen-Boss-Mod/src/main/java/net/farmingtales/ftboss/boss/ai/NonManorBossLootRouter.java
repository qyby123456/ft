package net.farmingtales.ftboss.boss.ai;

import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.block.ManorLootChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ftboss")
public final class NonManorBossLootRouter {
    private static final String CHEST = "ftboss:non_manor_loot_chest";
    private static final String ORIGIN = "ftboss:non_manor_loot_origin";
    private static final Map<LivingEntity, List<ItemEntity>> PENDING = new WeakHashMap<>();

    private NonManorBossLootRouter() {}

    public static void bind(ManorLootChestBlockEntity chest, LivingEntity boss) {
        if (chest == null || boss == null) return;
        if (!(boss.level() instanceof ServerLevel level) || !NonManorLootCrateDrops.eligible(boss)
                || !chest.belongsTo(boss.getUUID())) return;
        boss.getPersistentData().putLong(CHEST, chest.getBlockPos().asLong());
        List<ItemEntity> pending = PENDING.remove(boss);
        if (pending != null) {
            for (ItemEntity item : pending) if (item.isAlive()) collect(item, boss);
        }
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, boss.getBoundingBox().inflate(8.0),
                entity -> entity.isAlive() && entity.getPersistentData().hasUUID(ORIGIN)
                        && boss.getUUID().equals(entity.getPersistentData().getUUID(ORIGIN)))) {
            collect(item, boss);
        }
        FTBossMod.LOGGER.info("[FTBoss][FoodReward] bound boss {} to loot chest {}", boss.getUUID(), chest.getBlockPos());
    }

    private static ManorLootChestBlockEntity chest(LivingEntity boss) {
        if (!(boss.level() instanceof ServerLevel level) || !NonManorLootCrateDrops.eligible(boss)) return null;
        CompoundTag data = boss.getPersistentData();
        if (!data.contains(CHEST)) return null;
        BlockPos position = BlockPos.of(data.getLong(CHEST));
        if (!level.hasChunkAt(position)) return null;
        return level.getBlockEntity(position) instanceof ManorLootChestBlockEntity chest
                && chest.belongsTo(boss.getUUID()) ? chest : null;
    }

    private static boolean collect(ItemEntity entity, LivingEntity boss) {
        if (!NonManorLootCrateDrops.eligible(boss) || !(boss.level() instanceof ServerLevel)) return false;
        entity.getPersistentData().putUUID(ORIGIN, boss.getUUID());
        ManorLootChestBlockEntity chest = chest(boss);
        if (chest == null) {
            List<ItemEntity> pending = PENDING.computeIfAbsent(boss, ignored -> new ArrayList<>());
            if (!pending.contains(entity)) pending.add(entity);
            return false;
        }
        ItemStack original = entity.getItem();
        ItemStack remainder = insert(chest, original);
        int moved = original.getCount() - remainder.getCount();
        if (moved <= 0) return false;
        entity.setItem(remainder);
        chest.setChanged();
        FTBossMod.LOGGER.info("[FTBoss][FoodReward] collected {} boss-drop items for {}", moved, boss.getUUID());
        if (!remainder.isEmpty()) return false;
        entity.discard();
        return true;
    }

    public static ItemEntity spawnAtBoss(LivingEntity boss, ItemStack stack) {
        if (!(boss.level() instanceof ServerLevel level) || !NonManorLootCrateDrops.eligible(boss)) return boss.spawnAtLocation(stack);
        if (stack.isEmpty()) return null;
        ItemEntity item = new ItemEntity(level, boss.getX(), boss.getY(), boss.getZ(), stack);
        item.setDefaultPickUpDelay();
        if (!collect(item, boss)) {
            Collection<ItemEntity> captured = boss.captureDrops();
            if (captured != null) captured.add(item);
            else level.addFreshEntity(item);
        }
        return item;
    }

    public static ItemEntity spawnAtBossLike(LivingEntity boss, net.minecraft.world.level.ItemLike item) {
        return spawnAtBoss(boss, new ItemStack(item));
    }

    public static boolean spawn(Level level, Entity entity, LivingEntity boss) {
        if (entity instanceof ItemEntity item && collect(item, boss)) return true;
        return level.addFreshEntity(entity);
    }

    public static boolean append(Collection<ItemEntity> drops, ItemEntity item, LivingEntity boss) {
        return collect(item, boss) || drops.add(item);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDrops(LivingDropsEvent event) {
        LivingEntity boss = event.getEntity();
        if (!(boss.level() instanceof ServerLevel) || !NonManorLootCrateDrops.eligible(boss)) return;
        Iterator<ItemEntity> iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemEntity item = iterator.next();
            if (item.getItem().isEmpty() || collect(item, boss)) iterator.remove();
        }
    }

    static ItemStack insert(Container chest, ItemStack stack) {
        ItemStack remainder = stack.copy();
        for (int slot = 0; slot < chest.getContainerSize() && !remainder.isEmpty(); slot++) {
            ItemStack existing = chest.getItem(slot);
            if (existing.isEmpty() || !ItemStack.isSameItemSameTags(existing, remainder)) continue;
            int room = Math.min(existing.getMaxStackSize(), chest.getMaxStackSize()) - existing.getCount();
            int moved = Math.min(Math.max(0, room), remainder.getCount());
            if (moved == 0) continue;
            ItemStack merged = existing.copy();
            merged.grow(moved);
            chest.setItem(slot, merged);
            remainder.shrink(moved);
        }
        for (int slot = 0; slot < chest.getContainerSize() && !remainder.isEmpty(); slot++) {
            if (!chest.getItem(slot).isEmpty()) continue;
            int moved = Math.min(Math.min(remainder.getMaxStackSize(), chest.getMaxStackSize()), remainder.getCount());
            if (moved <= 0) continue;
            ItemStack placed = remainder.copy();
            placed.setCount(moved);
            chest.setItem(slot, placed);
            remainder.shrink(moved);
        }
        return remainder;
    }
}
