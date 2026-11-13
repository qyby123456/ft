import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Map;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CheckOne {
    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path root = Path.of(arguments[0]);
        Method inspect = VerifyDeliveries.class.getDeclaredMethod("inspect", Path.class);
        inspect.setAccessible(true);
        var metadata = JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject();
        Object room = inspect.invoke(null, root);
        var fuelChecks = new ArrayList<Object>();
        var palette = NbtIo.readCompressed(root.resolve("room.nbt").toFile()).getList("palette", 10);
        for (Method method : FireBlock.class.getDeclaredMethods()) {
            if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == BlockState.class && method.getReturnType() == int.class && method.getName().toLowerCase().contains("odds")) {
                method.setAccessible(true);
                for (int stateIndex = 0; stateIndex < palette.size(); stateIndex++) {
                    var state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), palette.getCompound(stateIndex));
                    int odds = (int) method.invoke(Blocks.FIRE, state);
                    if (odds != 0) throw new IllegalStateException("Flammable palette material " + state + ": " + method.getName() + "=" + odds);
                }
                fuelChecks.add(Map.of("method", method.getName(), "palette_states", palette.size(), "all_odds_zero", true));
            }
        }
        if (fuelChecks.isEmpty()) throw new IllegalStateException("No native FireBlock state odds method evaluated");
        var results = new ArrayList<Object>();
        var targets = metadata.getAsJsonArray("validation_targets");
        for (int index = 0; index < targets.size(); index++) {
            Path scratch = root.resolve("qa/collision-targets/target-" + index);
            Files.createDirectories(scratch);
            Files.copy(root.resolve("room.nbt"), scratch.resolve("room.nbt"), StandardCopyOption.REPLACE_EXISTING);
            var test = metadata.deepCopy();
            test.add("interior", targets.get(index));
            Files.writeString(scratch.resolve("room.json"), test.toString());
            inspect.invoke(null, scratch);
            results.add(Map.of("point", targets.get(index), "status", "PASS"));
        }
        Files.writeString(root.resolve("qa/native.json"), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status", "PASS", "room", room, "native_collision_targets", results, "native_fire_palette_checks", fuelChecks, "scope", "Java 1.20.1 registry, block states, StructureTemplate load/save, native stair-compatible paths, FireBlock native state fuel odds. Not Forge placement/tick/gameplay or coordinator acceptance.")));
        System.out.println("PASS: one actual NBT and " + targets.size() + " native collision targets");
    }
}
