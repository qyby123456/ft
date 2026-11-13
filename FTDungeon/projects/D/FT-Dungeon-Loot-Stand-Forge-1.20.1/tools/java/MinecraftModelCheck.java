import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.world.item.ItemDisplayContext;

public final class MinecraftModelCheck {
    public static void main(String[] arguments) throws Exception {
        Path root = Path.of(arguments[0]);
        Path assets = root.resolve("src/main/resources/assets/ftdungeonloot");
        BlockModel block = BlockModel.fromString(Files.readString(assets.resolve("models/block/loot_stand.json")));
        BlockModel item = BlockModel.fromString(Files.readString(assets.resolve("models/item/loot_stand.json")));
        if (block.getElements().size() != 71) throw new IllegalStateException("Unexpected element count");
        int faces = block.getElements().stream().mapToInt(element -> element.faces.size()).sum();
        if (faces > 426) throw new IllegalStateException("Unexpected face cost");
        int displays = 0;
        for (ItemDisplayContext context : ItemDisplayContext.values()) {
            if (context == ItemDisplayContext.NONE) continue;
            if (!block.getTransforms().hasTransform(context)) throw new IllegalStateException("Missing transform " + context);
            displays++;
        }
        if (item == null) throw new IllegalStateException("Item model failed to parse");
        JsonObject report = new JsonObject();
        report.addProperty("passed", true);
        report.addProperty("parser", "Minecraft 1.20.1 BlockModel.fromString / Forge 47.4.10");
        report.addProperty("elements", block.getElements().size());
        report.addProperty("faces", faces);
        report.addProperty("display_slots", displays);
        report.addProperty("game_client_render_tested", false);
        Files.createDirectories(root.resolve("verification"));
        Files.writeString(root.resolve("verification/minecraft-model-parser.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        System.out.println(report);
    }
}
