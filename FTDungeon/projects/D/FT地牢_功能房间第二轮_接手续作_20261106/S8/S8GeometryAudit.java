import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.world.phys.AABB;

public final class S8GeometryAudit {
    public static void main(String[] arguments) throws Exception {
        Path root = Path.of(arguments[0]);
        IndependentRouteAudit.main(new String[]{root.toString()});
        var clear = IndependentRouteAudit.class.getDeclaredMethod("clear", AABB.class);
        clear.setAccessible(true);
        var supported = IndependentRouteAudit.class.getDeclaredMethod("supported", double.class, double.class, double.class);
        supported.setAccessible(true);
        var specification = JsonParser.parseString(Files.readString(root.resolve("geometry-input.json"))).getAsJsonObject();
        List<Object> samples = new ArrayList<>();
        for (var element : specification.getAsJsonArray("volumes")) {
            var volume = element.getAsJsonObject();
            var bounds = volume.getAsJsonArray("bounds");
            AABB box = new AABB(bounds.get(0).getAsDouble(), bounds.get(1).getAsDouble(), bounds.get(2).getAsDouble(), bounds.get(3).getAsDouble(), bounds.get(4).getAsDouble(), bounds.get(5).getAsDouble());
            boolean free = (boolean) clear.invoke(null, box);
            samples.add(Map.of("label", volume.get("label").getAsString(), "bounds", bounds, "native_clear", free));
        }
        for (var element : specification.getAsJsonArray("stances")) {
            var stance = element.getAsJsonObject();
            var position = stance.getAsJsonArray("position");
            double east = position.get(0).getAsDouble(), up = position.get(1).getAsDouble(), south = position.get(2).getAsDouble();
            boolean free = (boolean) clear.invoke(null, new AABB(east-.299,up+.0001,south-.299,east+.299,up+1.7999,south+.299));
            boolean floor = (boolean) supported.invoke(null,east,up,south);
            samples.add(Map.of("label", stance.get("label").getAsString(), "position", position, "native_clear", free, "native_supported", floor));
        }
        var report = Map.of("sha256", specification.get("sha256").getAsString(), "samples", samples, "scope", "Actual vanilla static collision shapes. Volumes are continuous AABB tests. Ladder clearances and landing support do not certify climbing physics or door interactions.");
        Files.writeString(root.resolve("geometry-report.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
    }
}
