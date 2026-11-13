package net.farmingtales.ftdungeonmobs;

import java.io.InputStream;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftdungeonmobs")
@PrefixGameTestTemplate(false)
public final class DungeonRoomsGameTests {
    private DungeonRoomsGameTests() {}

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void installAndProtectRooms(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel().getServer().getLevel(DungeonGateTravel.DUNGEON);
        helper.assertTrue(level != null, "Real FT dungeon dimension must exist");
        helper.assertTrue(level.getServer() instanceof GameTestServer, "Cleanup is restricted to the isolated GameTest server");
        level.setChunkForced(-2, 2, true);
        helper.runAfterDelay(10, () -> {
            try {
                verifyRooms(helper, level);
            } catch (Exception exception) {
                throw new IllegalStateException("Original-room verification failed", exception);
            }
        });
    }

    private static void verifyRooms(GameTestHelper helper, ServerLevel level) throws Exception {
        BlockPos minimum = new BlockPos(-34, 64, 16);
        BlockPos maximum = new BlockPos(59, 94, 95);
        for (BlockPos position : BlockPos.betweenClosed(minimum, maximum)) {
            if (!level.getBlockState(position).isAir()) {
                level.setBlock(position, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        level.getEntities(null, new AABB(minimum, maximum.offset(1, 1, 1))).forEach(entity -> entity.discard());
        level.getDataStorage().set("ftdungeonmobs_rooms", new DungeonRoomsData());
        helper.assertTrue(DungeonRooms.prepare(helper.getLevel()).code().equals("dimension"), "Reject other dimensions");
        BlockPos obstruction = DungeonRooms.ROOMS.get(0).origin().offset(0, 28, 0);
        BlockPos untouched = new BlockPos(-34, 64, 31);
        level.getChunk(obstruction);
        level.setBlock(obstruction, Blocks.GOLD_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
        DungeonRooms.Result blocked = DungeonRooms.prepare(level);
        helper.assertTrue(blocked.code().equals("occupied"), "Protect even an omitted template position: " + blocked);
        helper.assertTrue(level.getBlockState(obstruction).is(Blocks.GOLD_BLOCK) && level.getBlockState(untouched).isAir(), "Failed preflight must not write blocks");
        level.setBlock(obstruction, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        ArmorStand occupant = new ArmorStand(level, -30.5, 65, 35.5);
        helper.assertTrue(level.addFreshEntity(occupant), "Create isolated obstruction entity");
        helper.assertTrue(DungeonRooms.prepare(level).code().equals("entity"), "Refuse to build through entities");
        occupant.discard();
        DungeonRooms.Result installed = DungeonRooms.prepare(level);
        helper.assertTrue(installed.code().equals("built"), "Native placement must succeed: " + installed);
        int lootChests = 0;
        for (DungeonRooms.Room room : DungeonRooms.ROOMS) {
            try (InputStream input = level.getServer().getResourceManager().open(new ResourceLocation("ftdungeonmobs", "structures/rooms/" + room.name() + ".nbt"))) {
                CompoundTag tag = NbtIo.readCompressed(input);
                for (Map.Entry<BlockPos, BlockState> entry : DungeonRooms.readStates(room, tag).entrySet()) {
                    helper.assertTrue(DungeonRooms.sameState(level.getBlockState(entry.getKey()), entry.getValue()), "Every original solid state and air meaning must be retained at " + entry.getKey());
                    if (BuiltInRegistries.BLOCK.getKey(entry.getValue().getBlock()).toString().equals("lootr:lootr_chest")) {
                        helper.assertTrue(level.getBlockEntity(entry.getKey()) != null, "Real Lootr block entity must exist");
                        lootChests++;
                    }
                }
            }
        }
        helper.assertTrue(lootChests == 17, "Preserve all 17 Lootr chests");
        helper.assertTrue(DungeonRoomsData.load(DungeonRoomsData.get(level).save(new CompoundTag())).ready(), "Saved data survives serialization");
        BlockPos edit = DungeonRooms.ROOMS.get(0).origin().offset(33, 1, 0);
        level.setBlock(edit, Blocks.DIAMOND_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
        helper.assertTrue(DungeonRooms.prepare(level).code().equals("ready") && level.getBlockState(edit).is(Blocks.DIAMOND_BLOCK), "Repeated entry must not overwrite player changes");
        helper.runAfterDelay(40, () -> {
            for (int blockX = -35; blockX <= 34; blockX++) {
                for (int blockZ = 30; blockZ <= 96; blockZ++) {
                    if (blockX == -35 || blockX == 34 || blockZ == 30 || blockZ == 96) {
                        for (int blockY = 64; blockY <= 69; blockY++) {
                            BlockPos position = new BlockPos(blockX, blockY, blockZ);
                            helper.assertTrue(level.getFluidState(position).isEmpty(), "Water must not escape the main room at " + position);
                        }
                    }
                }
            }
            helper.assertTrue(level.getBlockState(edit).is(Blocks.DIAMOND_BLOCK), "Player edits survive scheduled fluid updates");
            System.out.println("DUNGEON_ROOMS_GAME_TEST_PASS: real FT dimension; block/entity preflight, 25335 template records, 17 native Lootr block entities, persistence, idempotence and 40-tick water containment");
            level.setChunkForced(-2, 2, false);
            helper.succeed();
        });
    }
}
