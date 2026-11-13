package net.farmingtales.ftdungeonloot;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

final class LootRoller {
    static List<ItemStack> roll(ServerLevel level, BlockPos position, LootProfile profile, long seed) {
        RandomSource random = RandomSource.create(seed);
        var tableId = profile.tables().get(random.nextInt(profile.tables().size()));
        var table = level.getServer().getLootData().getLootTable(tableId);
        if (table.getParamSet() != LootContextParamSets.CHEST) return List.of();
        LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(position))
                .create(LootContextParamSets.CHEST);
        return new ArrayList<>(table.getRandomItems(params, random.nextLong()));
    }
}
