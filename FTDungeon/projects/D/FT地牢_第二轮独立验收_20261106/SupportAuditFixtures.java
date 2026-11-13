import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

public final class SupportAuditFixtures {
    private static ListTag integers(int... values) {
        ListTag result = new ListTag();
        for (int value : values) result.add(IntTag.valueOf(value));
        return result;
    }

    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path root = Path.of(arguments[0]);
        Files.createDirectories(root);
        List<Object> routes = new ArrayList<>();
        for (boolean supported : List.of(true, false)) {
            Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
            blocks.put(new BlockPos(3, 3, 3), Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.WEST));
            blocks.put(new BlockPos(6, 2, 6), Blocks.MOSS_CARPET.defaultBlockState());
            blocks.put(new BlockPos(8, 2, 8), Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.WALL).setValue(LeverBlock.FACING, Direction.SOUTH));
            blocks.put(new BlockPos(2, 2, 8), Blocks.WARPED_FUNGUS.defaultBlockState());
            blocks.put(new BlockPos(8, 4, 2), Blocks.CAVE_VINES_PLANT.defaultBlockState());
            blocks.put(new BlockPos(8, 3, 2), Blocks.CAVE_VINES.defaultBlockState());
            blocks.put(new BlockPos(8, 5, 2), supported ? Blocks.STONE.defaultBlockState() : Blocks.OAK_LEAVES.defaultBlockState());
            blocks.put(new BlockPos(2, 1, 8), supported ? Blocks.WARPED_NYLIUM.defaultBlockState() : Blocks.SHROOMLIGHT.defaultBlockState());
            if (supported) {
                blocks.put(new BlockPos(4, 3, 3), Blocks.STONE.defaultBlockState());
                blocks.put(new BlockPos(6, 1, 6), Blocks.STONE.defaultBlockState());
                blocks.put(new BlockPos(8, 2, 7), Blocks.STONE.defaultBlockState());
            }
            CompoundTag data = new CompoundTag();
            data.putInt("DataVersion", 3465);
            data.put("size", integers(12, 8, 12));
            ListTag palette = new ListTag();
            ListTag entries = new ListTag();
            for (var block : blocks.entrySet()) {
                CompoundTag entry = new CompoundTag();
                entry.put("pos", integers(block.getKey().getX(), block.getKey().getY(), block.getKey().getZ()));
                entry.putInt("state", palette.size());
                palette.add(NbtUtils.writeBlockState(block.getValue()));
                entries.add(entry);
            }
            data.put("palette", palette);
            data.put("blocks", entries);
            data.put("entities", new ListTag());
            Path candidate = root.resolve(supported ? "supported.nbt" : "unsupported-attachments.nbt");
            NbtIo.writeCompressed(data, candidate.toFile());
            String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(candidate)));
            routes.add(Map.of("candidate", candidate.toString(), "sha256", digest, "group", supported ? 1 : 2));
        }
        Files.writeString(root.resolve("route-input.json"), new GsonBuilder().setPrettyPrinting().create().toJson(routes));
    }
}
