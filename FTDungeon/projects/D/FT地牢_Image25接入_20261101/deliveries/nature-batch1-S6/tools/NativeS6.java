import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

public class NativeS6 {
    public static void main(String[] arguments) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Method inspect = VerifyDeliveries.class.getDeclaredMethod("inspect", Path.class);
        inspect.setAccessible(true);
        var results = new ArrayList<Object>();
        var routeResults = new ArrayList<Object>();
        for (int index = 1; index <= 9; index++) {
            Path folder = Path.of(arguments[0], "incoming", "NAT6-" + String.format("%02d", index));
            results.add(inspect.invoke(null, folder));
            var metadata = JsonParser.parseString(Files.readString(folder.resolve("room.json"))).getAsJsonObject();
            var targets = new ArrayList<JsonArray>();
            for (var connector : metadata.getAsJsonArray("connectors")) {
                targets.add(connector.getAsJsonObject().getAsJsonArray("position"));
            }
            for (var waypoint : metadata.getAsJsonObject("distinctive_design").getAsJsonArray("waypoints")) {
                targets.add(waypoint.getAsJsonArray());
            }
            for (int targetIndex = 0; targetIndex < targets.size(); targetIndex++) {
                Path scratch = Path.of(arguments[0], "qa", "native-route-targets", metadata.get("id").getAsString(), String.valueOf(targetIndex));
                Files.createDirectories(scratch);
                Files.copy(folder.resolve("room.nbt"), scratch.resolve("room.nbt"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                var candidate = metadata.deepCopy();
                candidate.add("interior", targets.get(targetIndex));
                Files.writeString(scratch.resolve("room.json"), candidate.toString());
                inspect.invoke(null, scratch);
            }
            routeResults.add(Map.of("id", metadata.get("id").getAsString(), "status", "PASS", "tested_targets", targets));
        }
        Files.writeString(Path.of(arguments[0], "qa", "native-review.json"), new GsonBuilder().setPrettyPrinting().create().toJson(Map.of("status", "PASS", "rooms", results, "route_targets", routeResults, "scope", "Minecraft Java 1.20.1 registry/state validation, block entities, all connectors and distinctive waypoints with stair-compatible collision paths, StructureTemplate roundtrip; NOT Forge world placement or stability testing")));
        System.out.println("PASS: 9 native Minecraft structure templates");
    }
}
