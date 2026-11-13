package moss.generator;

import com.google.gson.Gson;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.farmingtales.ftdungeonmobs.generator.DungeonGenerators;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftstylesqa")
@PrefixGameTestTemplate(false)
public final class StyleProfilesGameTests {
    private static void rejected(GameTestHelper helper, Runnable operation, String message) {
        boolean failed = false;
        try { operation.run(); } catch (IllegalArgumentException | IllegalStateException expected) { failed = true; }
        helper.assertTrue(failed, message);
    }

    @GameTest(template = "empty", timeoutTicks = 1400)
    public static void startupJsonFixtureAndInvalidProfilesFailClosed(GameTestHelper helper) throws Exception {
        var fixture = Profiles.get("ftmossgen:obsidian");
        var path = Profiles.directory().resolve("demo.json");
        helper.assertTrue(Files.isRegularFile(path), "Fourth profile came from actual isolated startup config JSON, not a test Java provider");
        helper.assertTrue(fixture.title.equals("数据包黑曜石") && fixture.fingerprint.equals(Catalog.hash(Files.readAllBytes(path))), "Fourth startup JSON contents and fingerprint match actual file");
        helper.assertTrue(Profiles.all().size() == 4, "Three builtin profiles plus exactly one external JSON fixture");
        helper.assertTrue(DungeonGenerators.find(new ResourceLocation(fixture.id)).isPresent(), "New config-only style automatically registers a production provider");
        var plan = Planner.create(fixture.id, 20261107, new BlockPos(0, 100, 0));
        helper.assertTrue(plan.style.equals(fixture.id), "Fourth JSON profile plans through same engine without a new Java generator");
        StylePlannerGameTests.portalChoice(helper, plan);
        rejected(helper, () -> Profiles.get("ftmossgen:does_not_exist"), "Unknown style lookup never falls back to forest");
        Gson gson = new Gson();
        var original = Profiles.get("ftmossgen:forest");
        for (var style : Profiles.all()) {
            String expected = Catalog.hash((Catalog.hash(Profiles.bytes(style.catalog, ".json")) + ":" + style.fingerprint)
                .getBytes(java.nio.charset.StandardCharsets.UTF_8));
            helper.assertTrue(Catalog.get(style.id).fingerprint.equals(expected), "Frozen catalog identity includes actual profile byte fingerprint, so changed JSON cannot resume as same catalog");
        }
        for (String material : List.of("minecraft:not_a_real_block", "minecraft:air", "minecraft:water", "minecraft:stone[not_a_property=true]")) {
            var bad = gson.fromJson(gson.toJson(original), Profiles.Style.class);
            bad.id = "ftmossgen:invalid_fixture";
            bad.palette.put("minecraft:mossy_stone_bricks", material);
            rejected(helper, () -> Profiles.validate(bad), "Invalid palette rejected before any world placement: " + material);
        }
        for (String material : List.of("minecraft:air", "minecraft:stone_slab[type=bottom,waterlogged=false]", "minecraft:water", "minecraft:chest[facing=north,type=single,waterlogged=false]")) {
            var badCap = gson.fromJson(gson.toJson(original), Profiles.Style.class);
            badCap.capBlock = material;
            rejected(helper, () -> Profiles.validate(badCap), "Unsafe cap material rejected: " + material);
            var badBarrier = gson.fromJson(gson.toJson(original), Profiles.Style.class);
            badBarrier.barrierBlock = material;
            rejected(helper, () -> Profiles.validate(badBarrier), "Unsafe barrier material rejected: " + material);
        }
        statefulAirGeometry(helper, gson, original);
        helper.assertTrue(Profiles.get(original.id) == original && Profiles.all().size() == 4, "Detached invalid validation cannot corrupt live startup registry");
        System.out.println("STYLE_CONFIG_PASS:fourth startup JSON;actual config fingerprint;automatic provider;shared planner;invalid cap/barrier/palette failclosed;stateful AIR-only geometry");
        helper.succeed();
    }

    private static void statefulAirGeometry(GameTestHelper helper, Gson gson, Profiles.Style original) {
        var styled = gson.fromJson(gson.toJson(original), Profiles.Style.class);
        var decoration = new Profiles.Decoration();
        decoration.block = "minecraft:oak_log[axis=x]";
        decoration.role = "room";
        decoration.from = new int[]{1, 10, 1};
        decoration.to = new int[]{4, 10, 1};
        styled.decorations = List.of(decoration);
        Profiles.validate(styled);
        var template = new Catalog.Template();
        template.key = "straight/room_state_fixture";
        template.size = new int[]{8, 14, 8};
        helper.assertTrue(template.ports == null && template.sockets == null, "Synthetic geometry fixture exercises absent optional port/socket metadata");
        var air = Profiles.state("minecraft:air");
        var stone = Profiles.state("minecraft:stone");
        var tagged = new net.minecraft.nbt.CompoundTag();
        tagged.putString("fixture", "must-remain-unchanged");
        var source = List.of(
            new Catalog.Cell(new BlockPos(1, 11, 1), stone, null),
            new Catalog.Cell(new BlockPos(1, 10, 1), air, null),
            new Catalog.Cell(new BlockPos(2, 10, 1), stone, null),
            new Catalog.Cell(new BlockPos(4, 10, 1), air, tagged),
            new Catalog.Cell(new BlockPos(1, 9, 1), air, null),
            new Catalog.Cell(new BlockPos(1, 8, 1), air, null),
            new Catalog.Cell(new BlockPos(1, 7, 1), air, null));
        Map<BlockPos, Catalog.Cell> before = source.stream().collect(Collectors.toMap(Catalog.Cell::position, Function.identity()));
        var result = StyleGeometry.process(styled, template, source);
        Map<BlockPos, Catalog.Cell> after = result.stream().collect(Collectors.toMap(Catalog.Cell::position, Function.identity()));
        helper.assertTrue(after.size() == before.size() && after.keySet().equals(before.keySet()), "Decoration only changes explicitly supplied cells");
        int changed = 0;
        for (var entry : before.entrySet()) {
            var prior = entry.getValue();
            var next = after.get(entry.getKey());
            if (!prior.equals(next)) {
                changed++;
                helper.assertTrue(prior.state().isAir() && prior.nbt() == null && next.nbt() == null
                    && next.state().equals(Profiles.state(decoration.block)), "Every geometry delta replaces untagged AIR with exact configured blockstate");
            }
        }
        helper.assertTrue(changed == 1 && after.get(new BlockPos(1, 10, 1)).state().equals(Profiles.state("minecraft:oak_log[axis=x]")), "Forest profile applies stateful decoration and preserves axis=x");
        helper.assertTrue(after.get(new BlockPos(2, 10, 1)).equals(before.get(new BlockPos(2, 10, 1)))
            && after.get(new BlockPos(4, 10, 1)).equals(before.get(new BlockPos(4, 10, 1)))
            && !after.containsKey(new BlockPos(3, 10, 1)), "Solid, tagged AIR and absent cells are never overwritten or introduced");
        helper.assertTrue(source.get(1).state().isAir() && source.get(3).nbt().equals(tagged), "Geometry processing leaves source cells untouched");
    }
}
