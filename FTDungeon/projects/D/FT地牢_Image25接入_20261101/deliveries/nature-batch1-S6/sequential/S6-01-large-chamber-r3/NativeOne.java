import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

public class NativeOne {
    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Path root = Path.of(arguments[0]);
        Method inspect = VerifyDeliveries.class.getDeclaredMethod("inspect", Path.class);
        inspect.setAccessible(true);
        var metadata = JsonParser.parseString(Files.readString(root.resolve("room.json"))).getAsJsonObject();
        Object room = inspect.invoke(null, root);
        var targetResults = new ArrayList<Object>();
        var targets = metadata.getAsJsonArray("validation_targets");
        for (int index = 0; index < targets.size(); index++) {
            Path scratch = root.resolve("qa/native-targets/target-" + index);
            Files.createDirectories(scratch);
            Files.copy(root.resolve("room.nbt"), scratch.resolve("room.nbt"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            var candidate = metadata.deepCopy();
            candidate.add("interior", targets.get(index));
            Files.writeString(scratch.resolve("room.json"), candidate.toString());
            inspect.invoke(null, scratch);
            targetResults.add(Map.of("target", targets.get(index), "status", "PASS"));
        }
        Files.writeString(root.resolve("qa/native-review.json"), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status", "PASS", "room", room, "targets", targetResults, "scope", "Actual vanilla Minecraft 1.20.1 states, collision and stair-compatible paths to all exits, four circulation sides and altar; not Forge placement or gameplay")));
        System.out.println("PASS: one reference, one structure, " + targets.size() + " native path targets");
    }
}
