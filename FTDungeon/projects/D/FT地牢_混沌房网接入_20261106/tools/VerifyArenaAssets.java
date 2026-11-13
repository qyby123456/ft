import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.renderer.block.model.BlockModel;

public final class VerifyArenaAssets {
    private VerifyArenaAssets() {}

    public static void main(String[] arguments) throws Exception {
        Path resources = Path.of(arguments[0]);
        Path project = resources.getParent().getParent().getParent();
        Path assets = resources.resolve("assets/ftdungeonmobs");
        Path java = project.resolve("src/main/java/net/farmingtales/ftdungeonmobs");
        for (String retired : new String[]{"geo/arena_brazier.geo.json", "animations/arena_brazier.animation.json",
                "textures/block/arena_torch_body.png", "textures/block/arena_torch_body_t1.png",
                "textures/block/arena_torch_body_t2.png", "textures/block/arena_torch_body_t3.png",
                "textures/block/arena_torch_body_t4.png", "textures/block/arena_torch_body_t5.png"}) {
            require(!Files.exists(assets.resolve(retired)), "Old DSH art must not ship: " + retired);
        }
        for (String retired : new String[]{"client/ArenaTorchModel.java", "client/ArenaTorchRenderer.java",
                "client/ArenaTorchEmissiveLayer.java"}) {
            require(!Files.exists(java.resolve(retired)), "Old custom rendering code must be removed");
        }
        require(!Files.exists(project.resolve("model_sources/arena_torch.bbmodel")), "Old active model source");
        require(!Files.exists(resources.resolve("data/ftdungeonmobs/recipes/arena_torch.json")), "Old craft recipe");
        JsonObject states = read(assets.resolve("blockstates/arena_torch.json")).getAsJsonObject("variants");
        require(states.size() == 12, "Keep saved legacy states and add both unlit half states");
        require(states.getAsJsonObject("half=lower,tier=0").get("model").getAsString()
                .equals("ftdungeonmobs:block/bloodthorn_brazier_unlit"), "Legacy lower unlit state");
        require(states.getAsJsonObject("half=upper,tier=0").get("model").getAsString()
                .equals("ftdungeonmobs:block/arena_torch"), "Legacy upper unlit state stays hidden");
        for (int tier = 1; tier <= 5; tier++) {
            String expected = "ftdungeonmobs:block/bloodthorn_brazier" + (tier == 5 ? "" : "_t" + tier);
            require(states.getAsJsonObject("half=lower,tier=" + tier).get("model").getAsString().equals(expected),
                    "Legacy lower half must use current brazier geometry");
            require(states.getAsJsonObject("half=upper,tier=" + tier).get("model").getAsString()
                    .equals("ftdungeonmobs:block/arena_torch"), "Legacy upper half must stay invisible");
        }
        JsonObject placeholder = read(assets.resolve("models/block/arena_torch.json"));
        require(placeholder.getAsJsonArray("elements").isEmpty(), "No retired geometry in upper placeholder");
        require(placeholder.getAsJsonObject("textures").get("particle").getAsString()
                .equals("ftdungeonmobs:block/bloodthorn_brazier"), "No retired particle textures");
        try (Reader reader = Files.newBufferedReader(assets.resolve("models/block/arena_torch.json"), StandardCharsets.UTF_8)) {
            require(BlockModel.fromStream(reader).getElements().isEmpty(), "Minecraft upper placeholder parsing");
        }
        JsonObject item = read(assets.resolve("models/item/arena_torch.json"));
        require(!item.has("elements") && item.get("parent").getAsString()
                .equals("ftdungeonmobs:block/bloodthorn_brazier_unlit"), "Legacy item uses the new unlit brazier");
        JsonObject pool = read(resources.resolve("data/ftdungeonmobs/loot_tables/blocks/arena_torch.json"))
                .getAsJsonArray("pools").get(0).getAsJsonObject();
        require(pool.getAsJsonArray("entries").get(0).getAsJsonObject().get("name").getAsString()
                .equals("ftdungeonmobs:bloodthorn_brazier"), "Old placed blocks drop the new brazier");
        require(pool.getAsJsonArray("conditions").get(1).getAsJsonObject().getAsJsonObject("properties")
                .get("half").getAsString().equals("lower"), "Old two-block structure must drop once");
        String registry = Files.readString(java.resolve("FTDungeonMobs.java"));
        String client = Files.readString(java.resolve("client/DungeonClient.java"));
        String legacy = Files.readString(java.resolve("ArenaTorchBlock.java"));
        String entity = Files.readString(java.resolve("ArenaTorchBlockEntity.java"));
        require(!registry.contains("output.accept(ARENA_TORCH_ITEM.get())")
                && !client.contains("event.accept(FTDungeonMobs.ARENA_TORCH_ITEM)"), "No old creative entries");
        require(!client.contains("ARENA_TORCH_ENTITY") && !client.contains("ArenaTorchRenderer"),
                "No retired renderer registration");
        require(!entity.contains("GeoBlockEntity") && !entity.contains("software.bernie")
                && entity.contains("extends BlockEntity"), "Only inert saved block-entity compatibility remains");
        require(!legacy.contains("ENTITYBLOCK_ANIMATED") && !legacy.contains("animateTick")
                && legacy.contains("RenderShape.MODEL") && legacy.contains("RenderShape.INVISIBLE"),
                "Old animation and particles retired");
        require(registry.contains("new BlockItem(BLOODTHORN_BRAZIER.get()"), "Legacy items place new one-block brazier");
        System.out.println("ARENA_RETIREMENT_VERIFIED: old geo, animation, six textures, custom renderer and active "
                + "source removed; recipe/creative entries removed; 12 legacy states map to shared new geometry "
                + "or invisible upper placeholder; inert saved BE compatibility; canonical new block loot/item placement.");
    }

    private static JsonObject read(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("ARENA_RETIREMENT_FAILURE: " + message);
        }
    }
}
