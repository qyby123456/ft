package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.reward.ItemReward;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.block.FTBlocks;
import net.farmingtales.ftboss.block.ManorLootChestBlock;
import net.farmingtales.ftboss.block.ManorLootChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class FoodBossInventoryRewards {
    private static final String REWARDED = "ftboss:non_manor_loot_crate_rewarded";
    private static final String BUSY = "ftboss:food_reward_generating";
    private static final String VOUCHER = "ftboss:food_reward";
    private static final String CLAIMS = "ftboss_food_reward_claims";
    private static final int SEARCH_RADIUS = 6;

    private FoodBossInventoryRewards() {}

    public static void onBossDeath(LivingDeathEvent event) {
        LivingEntity boss = event.getEntity();
        if (!(boss.level() instanceof ServerLevel level) || !NonManorLootCrateDrops.eligible(boss)
                || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) return;
        ServerPlayer killer = event.getSource().getEntity() instanceof ServerPlayer player ? player
                : boss.getKillCredit() instanceof ServerPlayer credit ? credit : null;
        if (killer == null || boss.getPersistentData().getBoolean(REWARDED) || boss.getPersistentData().getBoolean(BUSY)) return;
        if (!ModList.get().isLoaded("ftbquests")) return;
        LootCrate crate = LootCrate.LOOT_CRATES.get(NonManorLootCrateDrops.CRATE_TYPE);
        if (crate == null) {
            FTBossMod.LOGGER.warn("[FTBoss][FoodReward] reward table {} unavailable", NonManorLootCrateDrops.CRATE_TYPE);
            return;
        }
        boss.getPersistentData().putBoolean(BUSY, true);
        try {
            List<ItemStack> contents = new ArrayList<>();
            for (var weighted : crate.getTable().generateWeightedRandomRewards(killer.getRandom(), 1, true)) {
                Reward reward = weighted.getReward();
                if (reward instanceof ItemReward item) {
                    item.automatedClaimPre(null, contents, killer.getRandom(), killer.getUUID(), killer);
                } else {
                    CompoundTag snapshot = new CompoundTag();
                    reward.writeData(snapshot);
                    CompoundTag token = new CompoundTag();
                    token.putUUID("Id", UUID.randomUUID());
                    token.putString("Type", reward.getType().getTypeForNBT());
                    token.put("Data", snapshot);
                    ItemStack voucher = new ItemStack(Items.PAPER);
                    voucher.getOrCreateTag().put(VOUCHER, token);
                    voucher.setHoverName(Component.literal("食物 Boss 奖励凭证（开箱或右键领取）").append(" — ").append(reward.getTitle()));
                    contents.add(voucher);
                }
            }
            BlockPos position = position(level, boss.blockPosition(), killer);
            int used = 0;
            if (position != null && level.setBlock(position, FTBlocks.MANOR_LOOT_CHEST.get().defaultBlockState()
                    .setValue(ManorLootChestBlock.FACING, killer.getDirection().getOpposite()), 3)) {
                if (!(level.getBlockEntity(position) instanceof ManorLootChestBlockEntity chest)) {
                    throw new IllegalStateException("Placed reward chest has no inventory");
                }
                chest.setVictory(boss.getUUID());
                chest.setCustomName(Component.literal("食物 Boss 战利品"));
                used = Math.min(contents.size(), chest.getContainerSize());
                for (int index = 0; index < used; index++) chest.setItem(index, contents.get(index).copy());
                NonManorBossLootRouter.bind(chest, boss);
                chest.setChanged();
                boss.getPersistentData().putBoolean(REWARDED, true);
            }
            while (used < contents.size()) {
                int end = Math.min(used + ManorLootChestBlockEntity.CAPACITY, contents.size());
                givePacked(level, killer, packed(contents.subList(used, end)));
                boss.getPersistentData().putBoolean(REWARDED, true);
                used = end;
            }
            FTBossMod.LOGGER.info("[FTBoss][FoodReward] delivered inventory reward for {} at {}", BossMercy.typeOf(boss), position);
        } catch (RuntimeException exception) {
            FTBossMod.LOGGER.error("[FTBoss][FoodReward] inventory reward failed for {}", boss.getUUID(), exception);
        } finally {
            boss.getPersistentData().remove(BUSY);
        }
    }

    private static BlockPos position(ServerLevel level, BlockPos origin, ServerPlayer killer) {
        for (int radius = 0; radius <= SEARCH_RADIUS; radius++) {
            for (int vertical = 0; vertical <= 4; vertical++) {
                for (int offsetX = -radius; offsetX <= radius; offsetX++) {
                    for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                        if (Math.max(Math.abs(offsetX), Math.abs(offsetZ)) != radius) continue;
                        BlockPos candidate = origin.offset(offsetX, vertical, offsetZ);
                        if (candidate.getY() <= level.getMinBuildHeight() || candidate.getY() + 1 >= level.getMaxBuildHeight()
                                || !level.hasChunkAt(candidate) || !level.getWorldBorder().isWithinBounds(candidate)
                                || !level.mayInteract(killer, candidate)) continue;
                        if (level.getBlockEntity(candidate) != null || !level.getBlockState(candidate).canBeReplaced()
                                || !level.getFluidState(candidate).isEmpty() || !level.getBlockState(candidate.above()).isAir()) continue;
                        if (level.getBlockState(candidate.below()).isFaceSturdy(level, candidate.below(), Direction.UP)
                                && level.isUnobstructed(FTBlocks.MANOR_LOOT_CHEST.get().defaultBlockState(), candidate, CollisionContext.empty())) {
                            return candidate;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static ItemStack packed(List<ItemStack> contents) {
        NonNullList<ItemStack> slots = NonNullList.withSize(ManorLootChestBlockEntity.CAPACITY, ItemStack.EMPTY);
        for (int index = 0; index < contents.size(); index++) slots.set(index, contents.get(index).copy());
        CompoundTag block = new CompoundTag();
        ContainerHelper.saveAllItems(block, slots);
        block.putUUID("ManorVictory", UUID.randomUUID());
        block.putString("CustomName", Component.Serializer.toJson(Component.literal("食物 Boss 战利品")));
        ItemStack chest = new ItemStack(FTBlocks.MANOR_LOOT_ITEM.get());
        chest.getOrCreateTag().put("BlockEntityTag", block);
        chest.setHoverName(Component.literal("食物 Boss 库存箱（放置后打开）"));
        return chest;
    }

    private static void givePacked(ServerLevel level, ServerPlayer killer, ItemStack stack) {
        killer.getInventory().add(stack);
        if (stack.isEmpty()) return;
        ItemEntity drop = new ItemEntity(level, killer.getX(), killer.getY() + 0.5, killer.getZ(), stack);
        drop.setDefaultPickUpDelay();
        if (!level.addFreshEntity(drop)) throw new IllegalStateException("Reward chest delivery rejected");
    }

    public static void claimChest(ManorLootChestBlockEntity chest, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        for (int slot = 0; slot < chest.getContainerSize(); slot++) claimStack(chest.getItem(slot), serverPlayer);
        chest.setChanged();
    }

    private static boolean claimStack(ItemStack stack, ServerPlayer player) {
        if (!stack.is(Items.PAPER) || !stack.hasTag() || !stack.getTag().contains(VOUCHER)) return false;
        if (!ModList.get().isLoaded("ftbquests")) return false;
        CompoundTag token = stack.getTag().getCompound(VOUCHER);
        if (!token.hasUUID("Id")) return false;
        LootCrate crate = LootCrate.LOOT_CRATES.get(NonManorLootCrateDrops.CRATE_TYPE);
        if (crate == null) return false;
        Reward reward;
        try {
            reward = RewardType.createReward(0, crate.getTable().getFakeQuest(), token.getString("Type"));
            if (reward == null || reward instanceof ItemReward) return false;
            reward.readData(token.getCompound("Data"));
        } catch (RuntimeException exception) {
            FTBossMod.LOGGER.warn("[FTBoss][FoodReward] cannot read reward voucher", exception);
            return false;
        }
        FoodRewardClaims claims = player.server.overworld().getDataStorage().computeIfAbsent(FoodRewardClaims::load, FoodRewardClaims::new, CLAIMS);
        UUID identifier = token.getUUID("Id");
        boolean fresh = claims.claim(identifier);
        stack.shrink(1);
        if (fresh) {
            try {
                reward.claim(player, true);
            } catch (RuntimeException exception) {
                FTBossMod.LOGGER.error("[FTBoss][FoodReward] claim {} failed; snapshot={}, automatic retry disabled", identifier, token, exception);
                player.sendSystemMessage(Component.literal("奖励领取发生错误，请向管理员提供凭证编号：" + identifier));
            }
        }
        return true;
    }

    @SubscribeEvent
    public static void useVoucher(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player && claimStack(event.getItemStack(), player)) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
}
