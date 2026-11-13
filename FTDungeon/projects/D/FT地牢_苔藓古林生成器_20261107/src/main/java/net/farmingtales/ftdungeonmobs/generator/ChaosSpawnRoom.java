package net.farmingtales.ftdungeonmobs.generator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.Vec3;

final class ChaosSpawnRoom {
    static final BlockPos ORIGIN = new BlockPos(-24, 209, 80);
    static final BlockPos LANDING = new BlockPos(-16, 211, 88);
    static final Vec3 TARGET = Vec3.atBottomCenterOf(LANDING);
    private static final ResourceLocation TEMPLATE = new ResourceLocation("ftmaze", "chaos_spawn_room");

    static void prepare(ServerLevel level, CompoundTag saved) {
        if (level == null) throw new IllegalStateException("混沌地牢维度不可用");
        if (saved.getInt("SpawnRoomVersion") >= 1) return;
        var template = level.getStructureManager().get(TEMPLATE)
                .orElseThrow(() -> new IllegalStateException("混沌出生房结构缺失"));
        for (int chunkX = ORIGIN.getX() >> 4; chunkX <= (72 >> 4); chunkX++) {
            for (int chunkZ = ORIGIN.getZ() >> 4; chunkZ <= (96 >> 4); chunkZ++) {
                level.getChunk(chunkX, chunkZ);
            }
        }
        if (!template.placeInWorld(level, ORIGIN, ORIGIN, new StructurePlaceSettings(), level.random,
                Block.UPDATE_ALL)) throw new IllegalStateException("混沌出生房施工失败");
        if (!safe(level, LANDING)) throw new IllegalStateException("混沌出生房落点不安全");
        saved.putInt("SpawnRoomVersion", 1);
    }

    static boolean safe(ServerLevel level, BlockPos feet) {
        return level.getWorldBorder().isWithinBounds(feet)
                && level.getBlockState(feet.below()).isFaceSturdy(level, feet.below(), Direction.UP)
                && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                && level.getFluidState(feet).isEmpty() && level.getFluidState(feet.above()).isEmpty();
    }
}
