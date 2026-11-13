import com.google.gson.GsonBuilder;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

public final class FrostNative {
    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Method inspect = VerifyDeliveries.class.getDeclaredMethod("inspect", Path.class);
        inspect.setAccessible(true);
        Path root = Path.of(arguments[0]);
        ArrayList<Object> reports = new ArrayList<>();
        for (int number = 1; number <= 9; number++) {
            Path folder = root.resolve("incoming/FTN3-" + String.format("%02d", number));
            Object result = inspect.invoke(null, folder);
            if (number == 4) {
                CompoundTag template = NbtIo.readCompressed(folder.resolve("room.nbt").toFile());
                ListTag palette = template.getList("palette", Tag.TAG_COMPOUND);
                ListTag records = template.getList("blocks", Tag.TAG_COMPOUND);
                Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
                for (int index = 0; index < records.size(); index++) {
                    CompoundTag record = records.getCompound(index);
                    ListTag position = record.getList("pos", Tag.TAG_INT);
                    blocks.put(new BlockPos(position.getInt(0), position.getInt(1), position.getInt(2)), NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), palette.getCompound(record.getInt("state"))));
                }
                Method path = VerifyDeliveries.class.getDeclaredMethod("checkPath", Map.class, int[].class, BlockPos.class, BlockPos.class);
                path.setAccessible(true);
                path.invoke(null, blocks, new int[]{25, 14, 19}, new BlockPos(12, 1, 0), new BlockPos(12, 3, 9));
                System.out.println("PASS upper deck native collision path at [12,3,9]");
            }
            reports.add(result);
        }
        Files.writeString(root.resolve("qa/native-review.json"), new GsonBuilder().setPrettyPrinting().create().toJson(reports) + "\n", StandardCharsets.UTF_8);
        System.out.println("PASS nine Minecraft 1.20.1 registry/collision/template checks; NOT world placement");
    }
}
