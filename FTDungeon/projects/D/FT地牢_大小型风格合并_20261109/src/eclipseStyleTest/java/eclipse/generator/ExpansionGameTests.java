package eclipse.generator;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("fteclipsestylesqa")
@PrefixGameTestTemplate(false)
public final class ExpansionGameTests {
    private static final class Room {
        String style, key, baseKey, sha256, resource;
    }

    private static final class Inventory {
        List<Room> rooms;
    }

    private static void safe(GameTestHelper helper, BlockPos origin, int[] point, int height, String label) {
        BlockPos position = origin.offset(point[0], point[1], point[2]);
        for (int rise = 0; rise < height; rise++) {
            helper.assertTrue(helper.getLevel().getBlockState(position.above(rise)).isAir(), label + " blocked " + position);
        }
        BlockPos support = position.below();
        helper.assertTrue(helper.getLevel().getBlockState(support).isFaceSturdy(helper.getLevel(), support, Direction.UP), label + " unsupported " + position);
    }

    @GameTest(template = "empty", timeoutTicks = 2400)
    public static void additionalRoomPlacement(GameTestHelper helper) {
        Inventory inventory = Catalog.GSON.fromJson(new String(Catalog.resource("data/fteclipsestylesqa/expansion/inventory.json"), StandardCharsets.UTF_8), Inventory.class);
        helper.assertTrue(inventory.rooms.size() == 80, "Expected eighty added rooms");
        Map<String, Integer> counts = new HashMap<>();
        int placed = 0;
        for (Room room : inventory.rooms) {
            Catalog.Template base = Catalog.get().templates.get(room.baseKey);
            helper.assertTrue(base != null && base.sockets.size() == 4, "Invalid additional room base");
            List<Catalog.Cell> decoded = Catalog.decode(base, room.resource, room.sha256);
            BlockPos origin = new BlockPos(300000 + placed * 32, 100, 300000);
            helper.getLevel().getChunkAt(origin);
            for (Catalog.Cell cell : decoded) {
                BlockPos position = origin.offset(cell.position());
                helper.getLevel().setBlock(position, cell.state(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                helper.assertTrue(helper.getLevel().getBlockState(position).equals(cell.state()), room.style + "/" + room.key + " placement mismatch " + position);
            }
            for (Catalog.Port port : base.ports) {
                for (int[] point : port.cells) safe(helper, origin, point, 5, room.style + "/" + room.key + " port");
            }
            for (Catalog.Socket socket : base.sockets) {
                safe(helper, origin, socket.position, 3, room.style + "/" + room.key + " rune");
                safe(helper, origin, socket.spawn_position, 3, room.style + "/" + room.key + " spawn");
            }
            for (Catalog.Cell cell : decoded) {
                BlockPos position = origin.offset(cell.position());
                BlockState actual = helper.getLevel().getBlockState(position);
                helper.assertTrue(actual.equals(cell.state()), room.style + "/" + room.key + " changed during placement " + position);
                helper.assertTrue(actual.canSurvive(helper.getLevel(), position), room.style + "/" + room.key + " unsupported decoration " + position);
            }
            counts.merge(room.style, 1, Integer::sum);
            placed++;
        }
        helper.assertTrue(counts.size() == 8 && counts.values().stream().allMatch(count -> count == 10), "Expected ten rooms per style");
        System.out.println("FT_ROOM_EXPANSION_PASS:80 rooms;8 styles;native registry/state decoding;actual placed blocks;ports and four supported spawn sockets per room");
        helper.succeed();
    }
}
