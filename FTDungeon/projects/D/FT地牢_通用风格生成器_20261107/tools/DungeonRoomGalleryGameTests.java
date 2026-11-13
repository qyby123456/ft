package net.farmingtales.ftdungeonmobs;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftdungeonmobs")
@PrefixGameTestTemplate(false)
public final class DungeonRoomGalleryGameTests {
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    private DungeonRoomGalleryGameTests() {}

    @GameTest(template = "empty", batch = "image25_gallery", timeoutTicks = 1200)
    public static void galleryLifecycle(GameTestHelper helper) throws Exception {
        ServerLevel level = helper.getLevel().getServer().getLevel(DungeonGateTravel.DUNGEON);
        helper.assertTrue(level != null && level.getServer() instanceof GameTestServer, "Only the isolated GameTest world may be cleaned");
        List<DungeonRoomCatalog.Entry> entries = new ArrayList<>();
        List<DungeonRoomCatalog.Definition> rooms = new ArrayList<>();
        List<DungeonRoomCatalog.Entry> catalog = DungeonRoomCatalog.entries(level.getServer().getResourceManager());
        for (DungeonRoomCatalog.Entry entry : catalog) {
            ResourceLocation metadata = new ResourceLocation("ftdungeonmobs", "dungeon_rooms/image25/" + entry.id().toLowerCase(Locale.ROOT) + ".json");
            if (level.getServer().getResourceManager().getResource(metadata).isPresent()) {
                entries.add(entry);
                rooms.add(DungeonRoomCatalog.definition(level.getServer().getResourceManager(), entry));
            }
        }
        helper.assertTrue(!rooms.isEmpty(), "At least one delivered room is required");
        List<String> rejected = new ArrayList<>();
        for (DungeonRoomCatalog.Definition room : rooms) {
            try (InputStream input = level.getServer().getResourceManager().open(new ResourceLocation("ftdungeonmobs", "structures/" + room.template().getPath() + ".nbt"))) {
                CompoundTag tag = NbtIo.readCompressed(input);
                Map<BlockPos, BlockState> states = DungeonRooms.readStates(new DungeonRooms.Room(room.id(), room.origin(), room.size()), tag);
                DungeonRoomLayout.validate(room, tag, states);
                System.out.println("GALLERY_TEMPLATE_AUDIT_PASS: " + room.id());
            } catch (Exception exception) {
                String reason = room.id() + ": " + exception.getMessage();
                rejected.add(reason);
                System.out.println("GALLERY_TEMPLATE_AUDIT_FAIL: " + reason);
            }
        }
        for (int index = rooms.size() - 1; index >= 0; index--) {
            String identity = rooms.get(index).id();
            if (rejected.stream().anyMatch(reason -> reason.startsWith(identity + ":"))) {
                rooms.remove(index);
                entries.remove(index);
            }
        }
        helper.assertTrue(!rooms.isEmpty(), "At least one valid template is required: " + rejected);
        for (DungeonRoomCatalog.Definition room : rooms) {
            BlockPos minimum = DungeonRoomLayout.minimum(room);
            BlockPos maximum = DungeonRoomLayout.maximum(room);
            for (BlockPos position : BlockPos.betweenClosed(minimum, maximum)) {
                clear(level, position);
            }
            level.getEntities(null, new AABB(minimum, maximum.offset(1, 1, 1))).forEach(entity -> entity.discard());
            for (Map.Entry<BlockPos, Integer> column : columns(DungeonRoomLayout.paths(room)).entrySet()) {
                for (int blockY = 64; blockY <= column.getValue() + 3; blockY++) {
                    clear(level, column.getKey().atY(blockY));
                }
                BlockPos bottom = column.getKey().atY(64);
                BlockPos top = column.getKey().atY(column.getValue() + 4).offset(1, 0, 1);
                level.getEntities(null, new AABB(bottom, top)).forEach(entity -> entity.discard());
            }
        }
        CompoundTag saved = DungeonRoomsData.get(level).save(new CompoundTag());
        boolean legacyReady = DungeonRoomsData.get(level).ready();
        saved.remove("Rooms");
        level.getDataStorage().set("ftdungeonmobs_rooms", DungeonRoomsData.load(saved));
        BlockPos entityFixture = rooms.get(0).origin().offset(rooms.get(0).entrance());
        level.setChunkForced(entityFixture.getX() >> 4, entityFixture.getZ() >> 4, true);
        helper.runAfterDelay(10, () -> {
            try {
                verifyLifecycle(helper, level, entries, rooms, catalog, rejected, legacyReady);
            } catch (Exception exception) {
                throw new IllegalStateException("Gallery lifecycle verification failed", exception);
            }
        });
    }

    private static void verifyLifecycle(GameTestHelper helper, ServerLevel level, List<DungeonRoomCatalog.Entry> entries,
            List<DungeonRoomCatalog.Definition> rooms, List<DungeonRoomCatalog.Entry> catalog, List<String> rejected,
            boolean legacyReady) throws Exception {
        for (DungeonRoomCatalog.Definition room : rooms) {
            level.getEntities(null, new AABB(DungeonRoomLayout.minimum(room), DungeonRoomLayout.maximum(room).offset(1, 1, 1)))
                    .forEach(entity -> entity.discard());
            for (Map.Entry<BlockPos, Integer> column : columns(DungeonRoomLayout.paths(room)).entrySet()) {
                level.getEntities(null, new AABB(column.getKey().atY(64), column.getKey().atY(column.getValue() + 4).offset(1, 0, 1)))
                        .forEach(entity -> entity.discard());
            }
        }
        DungeonRoomCatalog.Definition first = rooms.get(0);
        BlockPos obstruction = first.origin().offset(first.entrance());
        level.setBlock(obstruction, Blocks.GOLD_BLOCK.defaultBlockState(), FLAGS);
        DungeonRoomGallery.Report occupied = DungeonRoomGallery.prepareOne(level, entries.get(0), false);
        helper.assertTrue(occupied.code().equals("occupied") && level.getBlockState(first.origin().below()).isAir(), "Full air-volume obstruction must prevent all writes: " + occupied);
        clear(level, obstruction);
        BlockPos borderObstruction = DungeonRoomLayout.maximum(first);
        level.setBlock(borderObstruction, Blocks.WATER.defaultBlockState(), FLAGS);
        helper.assertTrue(DungeonRoomGallery.prepareOne(level, entries.get(0), false).code().equals("occupied")
                && DungeonRoomsData.get(level).roomFingerprint(first.id()) == null, "Protect outer buffer fluids without marking a room built");
        clear(level, borderObstruction);
        BlockPos pathObstruction = new BlockPos(64, 65, 23);
        level.setBlock(pathObstruction, Blocks.GOLD_BLOCK.defaultBlockState(), FLAGS);
        helper.assertTrue(DungeonRoomGallery.prepareOne(level, entries.get(0), false).code().equals("path"), "Protect shared approach headroom");
        clear(level, pathObstruction);
        ArmorStand occupant = new ArmorStand(level, obstruction.getX() + 0.5, obstruction.getY(), obstruction.getZ() + 0.5);
        helper.assertTrue(level.addFreshEntity(occupant), "Create fixture entity");
        DungeonRoomGallery.Report entityBlocked = DungeonRoomGallery.prepareOne(level, entries.get(0), false);
        helper.assertTrue(entityBlocked.code().equals("entity"), "Protect room and path entities: " + entityBlocked);
        occupant.discard();
        int recordCount = 0;
        for (int index = 0; index < entries.size(); index++) {
            DungeonRoomCatalog.Definition room = rooms.get(index);
            DungeonRoomGallery.Report report = DungeonRoomGallery.prepareOne(level, entries.get(index), false);
            if (!report.code().equals("built")) {
                rejected.add(room.id() + ": " + report);
                System.out.println("GALLERY_PLACEMENT_FAIL: " + report);
                helper.assertTrue(DungeonRoomsData.get(level).roomFingerprint(room.id()) == null, "Rejected room must not persist a generated marker");
                continue;
            }
            System.out.println("GALLERY_PLACEMENT_PASS: " + room.id());
            try (InputStream input = level.getServer().getResourceManager().open(new ResourceLocation("ftdungeonmobs", "structures/" + room.template().getPath() + ".nbt"))) {
                CompoundTag tag = NbtIo.readCompressed(input);
                Map<BlockPos, BlockState> states = DungeonRooms.readStates(new DungeonRooms.Room(room.id(), room.origin(), room.size()), tag);
                recordCount += states.size();
                for (Map.Entry<BlockPos, BlockState> block : states.entrySet()) {
                    helper.assertTrue(DungeonRooms.sameState(level.getBlockState(block.getKey()), block.getValue()), room.id() + " native template mismatch at " + block.getKey());
                }
            }
            Map<BlockPos, BlockState> path = DungeonRoomLayout.paths(room);
            for (Map.Entry<BlockPos, BlockState> block : path.entrySet()) {
                helper.assertTrue(level.getBlockState(block.getKey()).equals(block.getValue()), "Connected route mismatch: " + block.getKey());
            }
            for (Map.Entry<BlockPos, Integer> column : columns(path).entrySet()) {
                for (int height = 1; height <= 3; height++) {
                    helper.assertTrue(level.getBlockState(column.getKey().atY(column.getValue() + height)).isAir(), "Route requires three blocks of headroom");
                }
            }
            assertWalkRoute(helper, level, room);
            helper.assertTrue(DungeonRoomsData.get(level).roomFingerprint(room.id()).equals(room.fingerprint()), "Independent room marker");
            helper.assertTrue(DungeonRoomsData.load(DungeonRoomsData.get(level).save(new CompoundTag())).roomFingerprint(room.id()).equals(room.fingerprint()), "Room marker survives serialization");
        }
        helper.assertTrue(DungeonRoomsData.get(level).ready() == legacyReady, "Catalog must not change legacy 31/30_1 marker");
        BlockPos edit = first.origin().offset(first.entrance()).below();
        if (DungeonRoomsData.get(level).roomFingerprint(first.id()) != null) {
            level.setBlock(edit, Blocks.DIAMOND_BLOCK.defaultBlockState(), FLAGS);
            helper.assertTrue(DungeonRoomGallery.prepareOne(level, entries.get(0), false).code().equals("ready")
                    && level.getBlockState(edit).is(Blocks.DIAMOND_BLOCK), "New entries must not rebuild existing rooms");
        }
        int verifiedRecords = recordCount;
        helper.runAfterDelay(40, () -> {
            for (DungeonRoomCatalog.Definition room : rooms) {
                BlockPos minimum = DungeonRoomLayout.minimum(room);
                BlockPos maximum = DungeonRoomLayout.maximum(room);
                for (BlockPos position : BlockPos.betweenClosed(minimum, maximum)) {
                    if (position.getX() == minimum.getX() || position.getX() == maximum.getX()
                            || position.getZ() == minimum.getZ() || position.getZ() == maximum.getZ()) {
                        helper.assertTrue(level.getFluidState(position).isEmpty(), room.id() + " fluid escaped at " + position);
                    }
                }
            }
            System.out.println("GALLERY_PLACEMENT_SUMMARY: validated=" + rooms.size() + "/" + catalog.size()
                    + ", records=" + verifiedRecords + ", rejected=" + rejected);
            helper.assertTrue(rejected.isEmpty(), "Native template audit: " + rejected);
            System.out.println("GALLERY_GAME_TEST_PASS: rooms=" + rooms.size() + "/" + catalog.size() + ", records=" + verifiedRecords
                    + "; real FT dimension, original states, north/interior walking, shared routes, independent persistence, block/path/entity refusal, idempotence and 40-tick fluid containment");
            BlockPos entityFixture = first.origin().offset(first.entrance());
            level.setChunkForced(entityFixture.getX() >> 4, entityFixture.getZ() >> 4, false);
            helper.succeed();
        });
    }

    private static void assertWalkRoute(GameTestHelper helper, ServerLevel level, DungeonRoomCatalog.Definition room) {
        List<BlockPos> feet = new ArrayList<>();
        for (int blockZ = 15; blockZ <= 23; blockZ++) {
            feet.add(new BlockPos(0, 64, blockZ));
        }
        for (int blockX = 1; blockX <= 80; blockX++) {
            feet.add(new BlockPos(blockX, blockX < 62 ? 64 : 65, 23));
        }
        for (int blockZ = 24; blockZ <= room.rowZ(); blockZ++) {
            feet.add(new BlockPos(80, 65, blockZ));
        }
        int entryX = room.origin().getX() + room.entrance().getX();
        for (int blockX = 81; blockX <= entryX; blockX++) {
            feet.add(new BlockPos(blockX, 65, room.rowZ()));
        }
        int rampStart = room.origin().getZ() - room.entrance().getY() - 1;
        for (int blockZ = room.rowZ() + 1; blockZ < room.origin().getZ(); blockZ++) {
            int rise = Math.min(room.entrance().getY(), Math.max(0, blockZ - rampStart + 1));
            feet.add(new BlockPos(entryX, 65 + rise, blockZ));
        }
        feet.add(room.origin().offset(room.entrance()));
        BlockPos previous = null;
        for (BlockPos position : feet) {
            BlockState floor = level.getBlockState(position.below());
            helper.assertTrue(floor.isFaceSturdy(level, position.below(), Direction.UP) || floor.getBlock() instanceof StairBlock,
                    room.id() + " walkway lacks support: " + position);
            for (int height = 0; height < 2; height++) {
                BlockPos body = position.above(height);
                helper.assertTrue(level.getBlockState(body).getCollisionShape(level, body).isEmpty() && level.getFluidState(body).isEmpty(),
                        room.id() + " walkway body obstructed: " + body);
            }
            if (previous != null) {
                int rise = position.getY() - previous.getY();
                helper.assertTrue(Math.abs(position.getX() - previous.getX()) + Math.abs(position.getZ() - previous.getZ()) == 1
                        && rise >= 0 && rise <= 1 && (rise == 0 || floor.getBlock() instanceof StairBlock),
                        room.id() + " walkway has a gap or unsupported step: " + previous + " to " + position);
            }
            previous = position;
        }
        System.out.println("GALLERY_ROUTE_PASS: " + room.id() + "; lobby=(0,64,15), north=" + room.origin().offset(room.entrance()).toShortString());
    }

    private static Map<BlockPos, Integer> columns(Map<BlockPos, BlockState> path) {
        Map<BlockPos, Integer> result = new LinkedHashMap<>();
        path.keySet().forEach(position -> result.merge(new BlockPos(position.getX(), 0, position.getZ()), position.getY(), Math::max));
        return result;
    }

    private static void clear(ServerLevel level, BlockPos position) {
        if (!level.getBlockState(position).isAir()) {
            level.setBlock(position, Blocks.AIR.defaultBlockState(), FLAGS);
        }
    }
}
