import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.world.item.ItemDisplayContext;

public final class MinecraftModelCheck {
    public static void main(String[] arguments) throws Exception {
        Path root = Path.of(arguments[0]);
        Path assets = root.resolve("src/main/resources/assets/ftdungeontrading");
        BlockModel block = BlockModel.fromString(Files.readString(assets.resolve("models/block/trading_table.json")));
        BlockModel item = BlockModel.fromString(Files.readString(assets.resolve("models/item/trading_table.json")));
        if (block.getElements().size() != 57) throw new IllegalStateException("Minecraft parsed an unexpected element count");
        int faces = block.getElements().stream().mapToInt(element -> element.faces.size()).sum();
        if (faces != 290) throw new IllegalStateException("Minecraft parsed an unexpected face count");
        int displays = 0;
        for (ItemDisplayContext context : ItemDisplayContext.values()) {
            if (context == ItemDisplayContext.NONE || context == ItemDisplayContext.HEAD) continue;
            if (!block.getTransforms().hasTransform(context)) throw new IllegalStateException("Missing transform " + context);
            displays++;
        }
        if (item == null) throw new IllegalStateException("Item model did not parse");
        JsonObject report = new JsonObject();
        report.addProperty("passed", true);
        report.addProperty("parser", "Minecraft 1.20.1 BlockModel.fromString, Forge 47.4.10 mapped runtime");
        report.addProperty("elements", block.getElements().size());
        report.addProperty("faces", faces);
        report.addProperty("display_slots", displays);
        report.addProperty("client_render_tested", false);
        Files.writeString(root.resolve("model_sources/qa/minecraft-parser.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        System.out.println(report);
    }
}
