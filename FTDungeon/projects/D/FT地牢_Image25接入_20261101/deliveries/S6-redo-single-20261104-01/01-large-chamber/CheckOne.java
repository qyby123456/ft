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

public class CheckOne {
    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path root = Path.of(arguments[0]);
        Method inspect = VerifyDeliveries.class.getDeclaredMethod("inspect", Path.class);
        inspect.setAccessible(true);
        var metadata = JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject();
        Object room = inspect.invoke(null, root);
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
        Files.writeString(root.resolve("qa/native.json"), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status", "PASS", "room", room, "native_collision_targets", results, "scope", "Java 1.20.1 registry, block states, StructureTemplate load/save, native stair-compatible paths. Not Forge placement/tick/gameplay or coordinator acceptance.")));
        System.out.println("PASS: one actual NBT and " + targets.size() + " native collision targets");
    }
}
