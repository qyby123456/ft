package moss.generator;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("ftmossgen")
@PrefixGameTestTemplate(false)
public final class MossPerformanceGameTests {
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void cachedPlannerPreservesFrozenPlans(GameTestHelper helper) throws ReflectiveOperationException {
        ServerLevel level = helper.getLevel().getServer().getLevel(MossMod.DIMENSION);
        long started = System.nanoTime();
        Planner.Plan warm = Planner.create(20261107L, new BlockPos(0, 100, 0));
        long warmNanos = System.nanoTime() - started;
        java.lang.reflect.Field surfaces = Planner.class.getDeclaredField("SURFACES");
        surfaces.setAccessible(true);
        ((java.util.Map<?, ?>) surfaces.get(null)).clear();
        Catalog catalog = Catalog.get();
        for (String name : java.util.List.of("decoded", "paletteStates")) {
            java.lang.reflect.Field cache = Catalog.class.getDeclaredField(name);
            cache.setAccessible(true);
            ((java.util.Map<?, ?>) cache.get(catalog)).clear();
        }
        started = System.nanoTime();
        Planner.Plan cold = Planner.create(20261107L, new BlockPos(0, 100, 0));
        long coldNanos = System.nanoTime() - started;
        helper.assertTrue(Catalog.GSON.toJson(warm).equals(Catalog.GSON.toJson(cold)),
                "Identical resources produce identical full planner output after discarding decoded, palette and surface caches");
        System.out.println("MOSS_PLANNER_TIMING:initial_ms=" + warmNanos / 1_000_000.0
                + ";discarded_all_template_caches_ms=" + coldNanos / 1_000_000.0);
        int[] anchors = {0, 8192, 32768};
        String[] hashes = {
            "0d0db88755ed73f296587d516cfd54bdaf368edf5caf61de2d8cd647b04c3093",
            "d2f309ad282d613126eefb8777f211318a344cb361f93cb3f111b41095595da2",
            "0f201469cf49d7259284a1c882a65ba1e18de978101e11f1541cb4f4587a2ccf"
        };
        for (int index = 0; index < anchors.length; index++) {
            for (int pass = 0; pass < 2; pass++) {
                Planner.Plan plan = Engine.plan(level, 20261107L, new BlockPos(anchors[index], 100, 0)).plan;
                String hash = Catalog.hash(Catalog.GSON.toJson(plan).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                helper.assertTrue(hash.equals(hashes[index]), "Cached planner exactly preserves pre-optimization frozen plan: " + anchors[index]
                        + ": expected " + hashes[index] + ", found " + hash);
            }
        }
        System.out.println("MOSS_PLANNER_EQUIVALENCE_PASS:same-process cold/warm full planner equality;3 unchanged pre-optimization Engine.plan SHA256 baselines;2 passes each");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void unloadedChecksAndWholeBuildJournal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel().getServer().getLevel(MossMod.DIMENSION);
        Engine.Job job = Engine.plan(level, 20261109L, new BlockPos(262144, 100, 0));
        BlockPos feet = Catalog.pos(job.plan.entry);
        BlockPos gate = Catalog.pos(job.plan.bossPortal);
        helper.assertTrue(!level.getChunkSource().hasChunk(feet.getX() >> 4, feet.getZ() >> 4), "Remote instance starts unloaded");
        helper.assertTrue(!Gameplay.safeFeet(level, feet), "Unloaded spawn defers safely");
        helper.assertTrue(MossBossPortals.refreshControl(level, gate), "Unloaded owned gate remains handled");
        helper.assertTrue(!level.getChunkSource().hasChunk(feet.getX() >> 4, feet.getZ() >> 4)
                && !level.getChunkSource().hasChunk(gate.getX() >> 4, gate.getZ() >> 4), "Spawn and gate checks never synchronously load remote chunks");
        Store detached = Store.load(Store.get(level).save(new CompoundTag()));
        detached.jobs.keySet().removeIf(instance -> !instance.equals(job.plan.id));
        Engine.Job checkpoint = detached.jobs.get(job.plan.id);
        checkpoint.stage = "BUILDING";
        checkpoint.checkPiece = checkpoint.plan.pieces.size();
        checkpoint.pendingStart = 0;
        checkpoint.pendingEnd = 100000;
        checkpoint.batchCursor = 123;
        Engine.Job recovered = Store.load(detached.save(new CompoundTag())).jobs.get(job.plan.id);
        helper.assertTrue(recovered.pendingStart == 0 && recovered.pendingEnd == 100000
                && recovered.writeCursor == 0 && recovered.batchCursor == -1, "Whole-build ownership journal survives reload and restarts volatile replay cursor");
        System.out.println("MOSS_PERFORMANCE_PASS:unloaded spawn/gate do not load chunks;large journal roundtrip;volatile cursor reset");
        helper.succeed();
    }
}
