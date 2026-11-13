package net.farmingtales.ftdungeonloot;

import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public final class DungeonChests {
    private static final String MARKER = "FTDungeonLoot";

    public static boolean singleContainer(RandomizableContainerBlockEntity chest) {
        if (chest instanceof BarrelBlockEntity) return true;
        return chest instanceof ChestBlockEntity && chest.getBlockState().getValue(ChestBlock.TYPE) == ChestType.SINGLE;
    }

    static void bind(ServerLevel level, BlockPos position, LootProfile profile) {
        if (!profile.dimension().equals(level.dimension().location())) throw new IllegalArgumentException("Wrong dimension for profile");
        if (!(level.getBlockEntity(position) instanceof RandomizableContainerBlockEntity chest) || !singleContainer(chest)) {
            throw new IllegalArgumentException("Only single chests and barrels can be bound");
        }
        if (chest.getPersistentData().contains(MARKER) || chest.saveWithoutMetadata().contains("LootTable") || !chest.isEmpty()) {
            throw new IllegalArgumentException("Chest must be empty, unbound and have no existing loot table");
        }
        CompoundTag marker = new CompoundTag();
        marker.putString("Profile", profile.id().toString());
        chest.getPersistentData().put(MARKER, marker);
        chest.setChanged();
    }

    static String fill(ServerLevel level, BlockPos position) {
        if (!(level.getBlockEntity(position) instanceof RandomizableContainerBlockEntity chest)
                || !chest.getPersistentData().contains(MARKER)) return "unbound";
        if (!singleContainer(chest)) return "double_chest";
        CompoundTag marker = chest.getPersistentData().getCompound(MARKER);
        ResourceLocation profileId = ResourceLocation.tryParse(marker.getString("Profile"));
        LootProfile profile = profileId == null ? null : LootProfiles.find(profileId).orElse(null);
        if (profile == null || !profile.dimension().equals(level.dimension().location()) || profile.currencyItem() == null) return "missing_profile";
        UUID run = DungeonRounds.current(level, profile);
        if (run == null) return "inactive_round";
        if (marker.hasUUID("Run") && marker.getUUID("Run").equals(run)) return "already";
        if (chest.saveWithoutMetadata().contains("LootTable") || !chest.isEmpty()) return "occupied";
        long seed = position.asLong() ^ run.getMostSignificantBits() ^ run.getLeastSignificantBits();
        var rewards = new ArrayList<>(LootRoller.roll(level, position, profile, seed));
        if (rewards.isEmpty()) return "empty_pool";
        var random = net.minecraft.util.RandomSource.create(seed ^ 0x47A1425L);
        int coins = profile.coinMin() + random.nextInt(profile.coinMax() - profile.coinMin() + 1);
        while (coins > 0) {
            int count = Math.min(coins, profile.currencyItem().getMaxStackSize());
            rewards.add(new ItemStack(profile.currencyItem(), count));
            coins -= count;
        }
        SimpleContainer planned = new SimpleContainer(chest.getContainerSize());
        for (ItemStack stack : rewards) if (!planned.addItem(stack.copy()).isEmpty()) return "overflow";
        for (int slot = 0; slot < planned.getContainerSize(); slot++) chest.setItem(slot, planned.getItem(slot).copy());
        marker.putUUID("Run", run);
        chest.setChanged();
        level.sendBlockUpdated(position, chest.getBlockState(), chest.getBlockState(), 3);
        return "filled";
    }

    public static void interact(PlayerInteractEvent.RightClickBlock event) {
        if (event.isCanceled() || event.getHand() != InteractionHand.MAIN_HAND || event.getEntity().isSpectator()
                || event.getUseBlock() == net.minecraftforge.eventbus.api.Event.Result.DENY || !event.getEntity().isAlive()
                || !(event.getLevel() instanceof ServerLevel level) || !level.mayInteract(event.getEntity(), event.getPos())
                || event.getEntity().distanceToSqr(event.getPos().getX() + .5, event.getPos().getY() + .5, event.getPos().getZ() + .5) > 64) return;
        String result = fill(level, event.getPos());
        if (!result.equals("unbound") && !result.equals("filled") && !result.equals("already")) {
            event.getEntity().sendSystemMessage(Component.translatable("message.ftdungeonloot.chest_" + result));
        }
    }
}
