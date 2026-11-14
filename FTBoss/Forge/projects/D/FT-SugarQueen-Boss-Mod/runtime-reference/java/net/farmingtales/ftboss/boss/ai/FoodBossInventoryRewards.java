/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  dev.ftb.mods.ftbquests.quest.Quest
 *  dev.ftb.mods.ftbquests.quest.loot.LootCrate
 *  dev.ftb.mods.ftbquests.quest.loot.WeightedReward
 *  dev.ftb.mods.ftbquests.quest.reward.ItemReward
 *  dev.ftb.mods.ftbquests.quest.reward.Reward
 *  dev.ftb.mods.ftbquests.quest.reward.RewardType
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.NonNullList
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.Component$Serializer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.ContainerHelper
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.GameRules
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$RightClickItem
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.ModList
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.boss.ai;

import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.block.FTBlocks;
import net.farmingtales.ftboss.block.ManorLootChestBlock;
import net.farmingtales.ftboss.block.ManorLootChestBlockEntity;
import net.farmingtales.ftboss.boss.ai.BossMercy;
import net.farmingtales.ftboss.boss.ai.FoodRewardClaims;
import net.farmingtales.ftboss.boss.ai.FoodRewardFallback;
import net.farmingtales.ftboss.boss.ai.NonManorLootCollector;
import net.farmingtales.ftboss.boss.ai.NonManorLootCoverage;
import net.farmingtales.ftboss.boss.ai.NonManorLootCrateDrops;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class FoodBossInventoryRewards {
    private static final String REWARDED = "ftboss:non_manor_loot_crate_rewarded";
    private static final String BUSY = "ftboss:food_reward_generating";
    private static final String VOUCHER = "ftboss:food_reward";
    private static final String CLAIMS = "ftboss_food_reward_claims";
    private static final int SEARCH_RADIUS = 6;

    private FoodBossInventoryRewards() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void onBossDeath(LivingDeathEvent livingDeathEvent) {
        ServerPlayer serverPlayer;
        Object object;
        Object object2;
        ServerLevel serverLevel;
        Level level;
        LivingEntity livingEntity;
        block22: {
            block21: {
                livingEntity = livingDeathEvent.getEntity();
                level = livingEntity.m_9236_();
                if (!(level instanceof ServerLevel)) break block21;
                serverLevel = (ServerLevel)level;
                if (NonManorLootCrateDrops.eligible(livingEntity) && serverLevel.m_46469_().m_46207_(GameRules.f_46135_)) break block22;
            }
            return;
        }
        Object object3 = livingDeathEvent.getSource().m_7639_();
        if (object3 instanceof ServerPlayer) {
            object2 = (ServerPlayer)object3;
            object = object2;
        } else {
            object3 = livingEntity.m_21232_();
            object = object3 instanceof ServerPlayer ? (serverPlayer = (ServerPlayer)object3) : null;
        }
        level = object;
        level = FoodRewardFallback.resolve((ServerPlayer)level, livingEntity);
        if (level == null || livingEntity.getPersistentData().m_128471_(REWARDED) || livingEntity.getPersistentData().m_128471_(BUSY)) {
            return;
        }
        if (!ModList.get().isLoaded("ftbquests")) {
            return;
        }
        serverPlayer = (LootCrate)LootCrate.LOOT_CRATES.get("ftboss_non_manor");
        if (serverPlayer == null) {
            FTBossMod.LOGGER.warn("[FTBoss][FoodReward] reward table {} unavailable", (Object)"ftboss_non_manor");
            return;
        }
        livingEntity.getPersistentData().m_128379_(BUSY, true);
        try {
            ItemReward itemReward;
            Object object4;
            object2 = new ArrayList();
            for (WeightedReward weightedReward : serverPlayer.getTable().generateWeightedRandomRewards(level.m_217043_(), 1, true)) {
                object4 = weightedReward.getReward();
                if (object4 instanceof ItemReward) {
                    itemReward = (ItemReward)object4;
                    itemReward.automatedClaimPre(null, (List)object2, level.m_217043_(), level.m_20148_(), (ServerPlayer)level);
                    continue;
                }
                CompoundTag compoundTag = new CompoundTag();
                object4.writeData(compoundTag);
                CompoundTag compoundTag2 = new CompoundTag();
                compoundTag2.m_128362_("Id", UUID.randomUUID());
                compoundTag2.m_128359_("Type", object4.getType().getTypeForNBT());
                compoundTag2.m_128365_("Data", (Tag)compoundTag);
                ItemStack itemStack = new ItemStack((ItemLike)Items.f_42516_);
                itemStack.m_41784_().m_128365_(VOUCHER, (Tag)compoundTag2);
                itemStack.m_41714_((Component)Component.m_237113_((String)"\u98df\u7269 Boss \u5956\u52b1\u51ed\u8bc1\uff08\u5f00\u7bb1\u6216\u53f3\u952e\u9886\u53d6\uff09").m_130946_(" \u2014 ").m_7220_(object4.getTitle()));
                object2.add(itemStack);
            }
            object2.isEmpty();
            object3 = FoodBossInventoryRewards.position(serverLevel, livingEntity.m_20183_(), (ServerPlayer)level);
            int n = 0;
            if (object3 != null && serverLevel.m_7731_((BlockPos)object3, (BlockState)((Block)FTBlocks.MANOR_LOOT_CHEST.get()).m_49966_().m_61124_((Property)ManorLootChestBlock.FACING, (Comparable)level.m_6350_().m_122424_()), 3)) {
                itemReward = serverLevel.m_7702_((BlockPos)object3);
                if (!(itemReward instanceof ManorLootChestBlockEntity)) {
                    throw new IllegalStateException("Placed reward chest has no inventory");
                }
                object4 = (ManorLootChestBlockEntity)itemReward;
                ((ManorLootChestBlockEntity)((Object)object4)).setVictory(livingEntity.m_20148_());
                object4.m_58638_((Component)Component.m_237113_((String)"\u98df\u7269 Boss \u6218\u5229\u54c1"));
                n = Math.min(object2.size(), ((ManorLootChestBlockEntity)((Object)object4)).m_6643_());
                for (int i = 0; i < n; ++i) {
                    object4.m_6836_(i, ((ItemStack)object2.get(i)).m_41777_());
                }
                NonManorLootCollector.absorb(serverLevel, (ManorLootChestBlockEntity)((Object)object4), livingEntity);
                object4.m_6596_();
                livingEntity.getPersistentData().m_128379_(REWARDED, true);
            }
            while (n < object2.size()) {
                int n2 = Math.min(n + 54, object2.size());
                FoodBossInventoryRewards.givePacked(serverLevel, (ServerPlayer)level, FoodBossInventoryRewards.packed(object2.subList(n, n2)));
                livingEntity.getPersistentData().m_128379_(REWARDED, true);
                n = n2;
            }
            FTBossMod.LOGGER.info("[FTBoss][FoodReward] delivered inventory reward for {} at {}", (Object)BossMercy.typeOf((Entity)livingEntity), object3);
        }
        catch (RuntimeException runtimeException) {
            FTBossMod.LOGGER.error("[FTBoss][FoodReward] inventory reward failed for {}", (Object)livingEntity.m_20148_(), (Object)runtimeException);
        }
        finally {
            livingEntity.getPersistentData().m_128473_(BUSY);
        }
    }

    private static BlockPos position(ServerLevel serverLevel, BlockPos blockPos, ServerPlayer serverPlayer) {
        return NonManorLootCoverage.position(serverLevel, blockPos, serverPlayer);
    }

    private static ItemStack packed(List<ItemStack> list) {
        NonNullList nonNullList = NonNullList.m_122780_((int)54, (Object)ItemStack.f_41583_);
        for (int i = 0; i < list.size(); ++i) {
            nonNullList.set(i, (Object)list.get(i).m_41777_());
        }
        CompoundTag compoundTag = new CompoundTag();
        ContainerHelper.m_18973_((CompoundTag)compoundTag, (NonNullList)nonNullList);
        compoundTag.m_128362_("ManorVictory", UUID.randomUUID());
        compoundTag.m_128359_("CustomName", Component.Serializer.m_130703_((Component)Component.m_237113_((String)"\u98df\u7269 Boss \u6218\u5229\u54c1")));
        ItemStack itemStack = new ItemStack((ItemLike)FTBlocks.MANOR_LOOT_ITEM.get());
        itemStack.m_41784_().m_128365_("BlockEntityTag", (Tag)compoundTag);
        itemStack.m_41714_((Component)Component.m_237113_((String)"\u98df\u7269 Boss \u5e93\u5b58\u7bb1\uff08\u653e\u7f6e\u540e\u6253\u5f00\uff09"));
        return itemStack;
    }

    private static void givePacked(ServerLevel serverLevel, ServerPlayer serverPlayer, ItemStack itemStack) {
        serverPlayer.m_150109_().m_36054_(itemStack);
        if (itemStack.m_41619_()) {
            return;
        }
        ItemEntity itemEntity = new ItemEntity((Level)serverLevel, serverPlayer.m_20185_(), serverPlayer.m_20186_() + 0.5, serverPlayer.m_20189_(), itemStack);
        itemEntity.m_32060_();
        if (!serverLevel.m_7967_((Entity)itemEntity)) {
            throw new IllegalStateException("Reward chest delivery rejected");
        }
    }

    public static void claimChest(ManorLootChestBlockEntity manorLootChestBlockEntity, Player player) {
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        for (int i = 0; i < manorLootChestBlockEntity.m_6643_(); ++i) {
            FoodBossInventoryRewards.claimStack(manorLootChestBlockEntity.m_8020_(i), serverPlayer);
        }
        manorLootChestBlockEntity.m_6596_();
    }

    private static boolean claimStack(ItemStack itemStack, ServerPlayer serverPlayer) {
        Reward reward;
        if (!(itemStack.m_150930_(Items.f_42516_) && itemStack.m_41782_() && itemStack.m_41783_().m_128441_(VOUCHER))) {
            return false;
        }
        if (!ModList.get().isLoaded("ftbquests")) {
            return false;
        }
        CompoundTag compoundTag = itemStack.m_41783_().m_128469_(VOUCHER);
        if (!compoundTag.m_128403_("Id")) {
            return false;
        }
        LootCrate lootCrate = (LootCrate)LootCrate.LOOT_CRATES.get("ftboss_non_manor");
        if (lootCrate == null) {
            return false;
        }
        try {
            reward = RewardType.createReward((long)0L, (Quest)lootCrate.getTable().getFakeQuest(), (String)compoundTag.m_128461_("Type"));
            if (reward == null || reward instanceof ItemReward) {
                return false;
            }
            reward.readData(compoundTag.m_128469_("Data"));
        }
        catch (RuntimeException runtimeException) {
            FTBossMod.LOGGER.warn("[FTBoss][FoodReward] cannot read reward voucher", (Throwable)runtimeException);
            return false;
        }
        FoodRewardClaims foodRewardClaims = (FoodRewardClaims)serverPlayer.f_8924_.m_129783_().m_8895_().m_164861_(FoodRewardClaims::load, FoodRewardClaims::new, CLAIMS);
        UUID uUID = compoundTag.m_128342_("Id");
        boolean bl = foodRewardClaims.claim(uUID);
        itemStack.m_41774_(1);
        if (bl) {
            try {
                reward.claim(serverPlayer, true);
            }
            catch (RuntimeException runtimeException) {
                FTBossMod.LOGGER.error("[FTBoss][FoodReward] claim {} failed; snapshot={}, automatic retry disabled", new Object[]{uUID, compoundTag, runtimeException});
                serverPlayer.m_213846_((Component)Component.m_237113_((String)("\u5956\u52b1\u9886\u53d6\u53d1\u751f\u9519\u8bef\uff0c\u8bf7\u5411\u7ba1\u7406\u5458\u63d0\u4f9b\u51ed\u8bc1\u7f16\u53f7\uff1a" + uUID)));
            }
        }
        return true;
    }

    @SubscribeEvent
    public static void useVoucher(PlayerInteractEvent.RightClickItem rightClickItem) {
        Player player = rightClickItem.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)player;
            if (FoodBossInventoryRewards.claimStack(rightClickItem.getItemStack(), serverPlayer)) {
                rightClickItem.setCancellationResult(InteractionResult.SUCCESS);
                rightClickItem.setCanceled(true);
            }
        }
    }
}

