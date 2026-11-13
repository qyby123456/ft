import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
        JsonObject expected = JsonParser.parseString(Files.readString(root.resolve("model_sources/model-report.json"))).getAsJsonObject();
        if (block.getElements().size() != expected.get("components").getAsInt()) throw new IllegalStateException("Minecraft parsed an unexpected element count");
        int faces = block.getElements().stream().mapToInt(element -> element.faces.size()).sum();
        if (faces != expected.get("declared_faces").getAsInt()) throw new IllegalStateException("Minecraft parsed an unexpected face count");
        int assembledComponents = 0;
        int assembledFaces = 0;
        for (String part : new String[] {"left", "right"}) {
            BlockModel half = BlockModel.fromString(Files.readString(assets.resolve("models/block/trading_table_" + part + ".json")));
            assembledComponents += half.getElements().size();
            assembledFaces += half.getElements().stream().mapToInt(element -> element.faces.size()).sum();
        }
        if (assembledComponents != expected.get("assembled_block_components").getAsInt()
                || assembledFaces != expected.get("assembled_block_faces").getAsInt()) throw new IllegalStateException("Unexpected assembled model geometry");
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
        report.addProperty("assembled_components", assembledComponents);
        report.addProperty("assembled_faces", assembledFaces);
        report.addProperty("display_slots", displays);
        report.addProperty("client_render_tested", false);
        Files.writeString(root.resolve("model_sources/qa/minecraft-parser.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        System.out.println(report);
    }
}
