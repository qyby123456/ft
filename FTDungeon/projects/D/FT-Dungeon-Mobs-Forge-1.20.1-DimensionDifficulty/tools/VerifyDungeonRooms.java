package net.farmingtales.ftdungeonmobs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ftdungeonmobs", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class VerifyDungeonRooms {
    private VerifyDungeonRooms() {}

    public static void main(String[] arguments) throws Exception {
        verify(Path.of(arguments[0]), false);
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        try {
            verify(Path.of(System.getProperty("ftdungeonmobs.verifyResources")), true);
        } catch (Exception exception) {
            throw new IllegalStateException("Dungeon room runtime verification failed", exception);
        }
    }

    private static void verify(Path resources, boolean runtime) throws Exception {
        Map<String, String> hashes = Map.of("30_1", "7fbebcac1a2fa99dad419a057f09b85c856e6f5005fc945adce1c44e0b7b35d0",
                "31", "d28d9a41b7a920c891546368f2d2a78c65ccc864b9e6741c77ecec880ca6bd82");
        Map<BlockPos, BlockState> additions = runtime ? DungeonRooms.blueprint() : Map.of();
        Set<BlockPos> occupied = new HashSet<>(additions.keySet());
        CompoundTag small = null;
        for (DungeonRooms.Room room : DungeonRooms.ROOMS) {
            Path path = resources.resolve("data/ftdungeonmobs/structures/rooms/" + room.name() + ".nbt");
            require(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path)))
                    .equals(hashes.get(room.name())), "Original NBT must remain byte-for-byte unchanged");
            CompoundTag tag = NbtIo.readCompressed(path.toFile());
            require(tag.getInt("DataVersion") == 3465, "Minecraft 1.20.1 structure");
            ListTag records = tag.getList("blocks", Tag.TAG_COMPOUND);
            require(records.size() == (room.name().equals("31") ? 24888 : 447), "Imported record count");
            ListTag palette = tag.getList("palette", Tag.TAG_COMPOUND);
            for (Tag value : records) {
                CompoundTag record = (CompoundTag) value;
                ListTag coordinates = record.getList("pos", Tag.TAG_INT);
                BlockPos position = room.origin().offset(coordinates.getInt(0), coordinates.getInt(1), coordinates.getInt(2));
                require(occupied.add(position), "Templates, retaining wall and paths must not overwrite one another");
                CompoundTag blockEntity = record.getCompound("nbt");
                require(!blockEntity.contains("LootTable") && blockEntity.getList("Items", Tag.TAG_COMPOUND).isEmpty(),
                        "Do not invent container rewards");
                CompoundTag state = palette.getCompound(record.getInt("state"));
                String blockName = state.getString("Name");
                if (runtime && (blockName.equals("minecraft:water") || blockName.equals("minecraft:seagrass")
                        || state.getCompound("Properties").getString("waterlogged").equals("true"))) {
                    for (Direction direction : Direction.Plane.HORIZONTAL) {
                        BlockPos neighbour = position.relative(direction);
                        BlockPos local = neighbour.subtract(room.origin());
                        if (local.getX() < 0 || local.getX() >= room.size().getX() || local.getZ() < 0 || local.getZ() >= room.size().getZ()) {
                            BlockState barrier = additions.get(neighbour);
                            require(barrier != null && (barrier.is(Blocks.MOSSY_STONE_BRICKS) || barrier.is(Blocks.POLISHED_BLACKSTONE_BRICKS)),
                                    "Boundary water requires a full retaining block at " + neighbour.toShortString());
                        }
                    }
                }
            }
            if (runtime) {
                require(DungeonRooms.readStates(room, tag).size() == records.size(), "Native registry/property validation");
                StructureTemplate template = new StructureTemplate();
                template.load(BuiltInRegistries.BLOCK.asLookup(), tag);
                require(template.save(new CompoundTag()).getList("blocks", Tag.TAG_COMPOUND).size() == records.size(),
                        "Native structure round trip must retain every record, including Lootr");
            }
            if (room.name().equals("30_1")) {
                small = tag;
            }
        }
        if (!runtime) {
            System.out.println("DUNGEON_ROOM_FILES_VERIFIED: original binary hashes, 25335 raw records, disjoint template coordinates and empty containers; native geometry/registries verified in Forge runData");
            return;
        }
        require(additions.keySet().stream().allMatch(position -> position.getZ() >= 16 && position.getY() >= 63),
                "Existing arrival hall and gate must not be modified");
        for (int blockZ = 16; blockZ <= 31; blockZ++) {
            int height = blockZ < 28 ? 63 : Math.min(66, blockZ + 36);
            require(additions.containsKey(new BlockPos(0, height, blockZ)), "Continuous main path");
            require(!additions.containsKey(new BlockPos(0, height + 1, blockZ))
                    && !additions.containsKey(new BlockPos(0, height + 2, blockZ)), "Two blocks of path headroom");
        }
        require(additions.get(new BlockPos(49, 65, 32)).is(Blocks.POLISHED_BLACKSTONE_BRICKS), "Small room connects across omitted first row");
        for (DungeonRooms.Room room : DungeonRooms.ROOMS) {
            require(additions.get(room.origin().below()).is(Blocks.MOSS_BLOCK), "Imported bottom plants require a separate soil foundation");
        }
        require(DungeonRooms.sameState(Blocks.AIR.defaultBlockState(), Blocks.VOID_AIR.defaultBlockState()), "Empty chunk sections canonicalize air");
        require(!DungeonRooms.sameState(Blocks.AIR.defaultBlockState(), Blocks.CHEST.defaultBlockState()), "Air normalization must not hide missing solid blocks");
        DungeonRooms.Room smallRoom = DungeonRooms.ROOMS.get(1);
        CompoundTag duplicate = small.copy();
        duplicate.getList("blocks", Tag.TAG_COMPOUND).add(duplicate.getList("blocks", Tag.TAG_COMPOUND).get(0).copy());
        reject(smallRoom, duplicate);
        CompoundTag variants = small.copy();
        variants.put("palettes", new ListTag());
        reject(smallRoom, variants);
        CompoundTag invalidState = small.copy();
        invalidState.getList("blocks", Tag.TAG_COMPOUND).getCompound(0).putInt("state", 9999);
        reject(smallRoom, invalidState);
        CompoundTag missing = small.copy();
        missing.getList("palette", Tag.TAG_COMPOUND).getCompound(0).putString("Name", "ftdungeonmobs:missing_room_block");
        reject(smallRoom, missing);
        DungeonRoomsData initial = new DungeonRoomsData();
        require(!initial.ready(), "Old worlds without new room data remain eligible");
        initial.markBuilt();
        require(DungeonRoomsData.load(initial.save(new CompoundTag())).ready(), "Persistent one-time generation");
        initial.markRoomBuilt("L1", hashes.get("31"));
        DungeonRoomsData independent = DungeonRoomsData.load(initial.save(new CompoundTag()));
        require(independent.roomFingerprint("L1").equals(hashes.get("31")) && independent.roomFingerprint("L2") == null
                && independent.ready(), "Independent room markers retain legacy state and leave future rooms eligible");
        CompoundTag future = new CompoundTag();
        future.putInt("LayoutVersion", 99);
        require(DungeonRoomsData.load(future).ready(), "Downgrade must not regenerate a newer layout");
        System.out.println("DUNGEON_ROOMS_VERIFIED: original hashes, dimensions, sparse masks, disjoint paths, water containment, malformed NBT and persistence; runtimeLootr=" + runtime);
    }

    private static void reject(DungeonRooms.Room room, CompoundTag tag) {
        try {
            DungeonRooms.readStates(room, tag);
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new IllegalStateException("Malformed template was accepted");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
