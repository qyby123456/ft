package eclipse.generator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("fteclipsegen")
@PrefixGameTestTemplate(false)
public final class PlannerSeedTests {
    @GameTest(template = "empty", timeoutTicks = 400)
    public static void seedExtremesRegionsAndCompleteCatalog(GameTestHelper helper) {
        long[] seeds = {0, 1, 2, 42, 123456789, 20261105, 20261106, -1, Long.MAX_VALUE, Long.MIN_VALUE};
        Set<Integer> counts = new HashSet<>();
        Set<String> selected = new HashSet<>();
        for (long seed : seeds) {
            for (BlockPos anchor : List.of(new BlockPos(0, 100, 0), new BlockPos(-1536, 100, -512))) {
                Planner.Plan plan = Planner.create(seed, anchor);
                helper.assertTrue(plan.pieces.stream().noneMatch(piece -> piece.template.equals("straight/bridge")), "Strict pool never selects bridge");
                helper.assertTrue(plan.pieces.stream().filter(piece -> piece.template.equals("promenade")).count() == 1, "One final promenade");
                helper.assertTrue(plan.pieces.stream().filter(piece -> piece.template.equals("boss/eclipse_keeper")).count() == 1, "One independent boss");
                helper.assertTrue(plan.pieces.stream().filter(piece -> piece.template.startsWith("stairs/")).count() == 3, "Exactly three inter-floor stairs");
                helper.assertTrue(plan.layoutSeed == (Catalog.pos(plan.region).hashCode() ^ seed), "Native closest-region seed");
                if (anchor.getX() == -1536) helper.assertTrue(plan.region[0] == -1024 && plan.region[2] == 0, "Java negative half-tie rounding, not floor or bankers rounding");
                long playable = plan.pieces.stream().filter(piece -> !piece.ports.isEmpty()).count();
                helper.assertTrue(plan.edges == playable - 1, "Uncapped connected four-floor tree");
                counts.add(plan.pieces.size());
                plan.pieces.forEach(piece -> selected.add(piece.key));
            }
        }
        helper.assertTrue(counts.size() > 3, "Native topology room count is not fixed");
        int cells = 0;
        for (Catalog.Template template : Catalog.get().templates.values()) {
            var blocks = Catalog.blocks(template.key);
            helper.assertTrue(blocks.size() == template.size[0] * (template.size[1] - template.minimum_y) * template.size[2], "Complete dense packaged template " + template.key);
            cells += blocks.size();
        }
        helper.assertTrue(selected.size() >= 20 && cells > 100000, "Broad actual room and source coverage");
        System.out.println("ECLIPSE_GENERATOR_SEEDS_PASS:20 plans;27 decoded variants;cells=" + cells + ";counts=" + counts);
        helper.succeed();
    }
}
