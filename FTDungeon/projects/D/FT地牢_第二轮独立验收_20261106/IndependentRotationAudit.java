import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

public final class IndependentRotationAudit {
    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path root = Path.of(arguments[0]);
        List<Object> reports = new ArrayList<>();
        for (var entry : JsonParser.parseString(Files.readString(root.resolve("rotation-input.json"))).getAsJsonArray()) {
            var specification = entry.getAsJsonObject();
            Path source = Path.of(specification.get("candidate").getAsString());
            String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(source)));
            if (!digest.equals(specification.get("sha256").getAsString())) throw new IllegalStateException("Source hash mismatch");
            var data = NbtIo.readCompressed(source.toFile());
            int turns = specification.get("turns_ccw").getAsInt();
            List<String> original = new ArrayList<>();
            List<String> rotated = new ArrayList<>();
            for (var paletteEntry : data.getList("palette", Tag.TAG_COMPOUND)) {
                BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), (net.minecraft.nbt.CompoundTag) paletteEntry);
                original.add(state.toString());
                for (int turn = 0; turn < turns; turn++) state = state.rotate(Rotation.COUNTERCLOCKWISE_90);
                rotated.add(state.toString());
            }
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("id", specification.get("id").getAsString());
            report.put("sha256", digest);
            report.put("turns_ccw", turns);
            report.put("normalized_original_palette", original);
            report.put("normalized_rotated_palette", rotated);
            reports.add(report);
        }
        Files.writeString(root.resolve("native-rotation-palettes.json"), new GsonBuilder().setPrettyPrinting().create().toJson(reports));
    }
}
