package net.farmingtales.ftdungeonmobs;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public final class VerifyBrazierStates {
    private VerifyBrazierStates() {}

    public static void verify() {
        BloodthornBrazierBlock brazier = FTDungeonMobs.BLOODTHORN_BRAZIER.get();
        ArenaTorchBlock legacy = FTDungeonMobs.ARENA_TORCH.get();
        require(brazier.defaultBlockState().getValue(BloodthornBrazierBlock.TIER) == 0,
                "New braziers must start unlit");
        require(brazier.getStateDefinition().getPossibleStates().size() == 6, "Six brazier states");
        require(legacy.getStateDefinition().getPossibleStates().size() == 12, "Twelve legacy half/tier states");
        BlockPos position = new BlockPos(-17, 70, 31);
        for (int tier = 0; tier <= 5; tier++) {
            BlockState state = brazier.defaultBlockState().setValue(BloodthornBrazierBlock.TIER, tier);
            require(state.getLightEmission() == (tier == 0 ? 0 : 14), "Brazier tier light");
            BlockState lower = legacy.defaultBlockState().setValue(ArenaTorchBlock.TIER, tier);
            require(lower.getLightEmission() == (tier == 0 ? 0 : 14), "Legacy tier light");
            require(lower.setValue(ArenaTorchBlock.HALF, DoubleBlockHalf.UPPER).getLightEmission() == 0,
                    "Legacy upper half stays dark");
            ArenaDifficultyApi.ArenaData original = new ArenaDifficultyApi.ArenaData();
            original.put(position, tier);
            CompoundTag saved = original.save(new CompoundTag());
            ListTag entries = saved.getList("Torches", Tag.TAG_COMPOUND);
            require(entries.size() == (tier == 0 ? 0 : 1), "Unlit braziers are not active difficulty sources");
            require(ArenaDifficultyApi.ArenaData.load(saved).save(new CompoundTag()).equals(saved),
                    "Active difficulty source persistence");
            original.put(position, 0);
            require(original.save(new CompoundTag()).getList("Torches", Tag.TAG_COMPOUND).isEmpty(),
                    "Extinguishing removes the difficulty source");
        }
        CompoundTag stale = new CompoundTag();
        ListTag entries = new ListTag();
        CompoundTag unlit = new CompoundTag();
        unlit.putLong("Position", position.asLong());
        unlit.putInt("Tier", 0);
        entries.add(unlit);
        stale.put("Torches", entries);
        require(ArenaDifficultyApi.ArenaData.load(stale).save(new CompoundTag())
                .getList("Torches", Tag.TAG_COMPOUND).isEmpty(), "Stored inactive sources are ignored");
        System.out.println("BRAZIER_STATES_VERIFIED: default unlit; six states; light 0/14; legacy 12 states; "
                + "inactive source removal and tier 1..5 SavedData round trips. No client/server world launched.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
