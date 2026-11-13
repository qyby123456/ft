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
    public static void cachedPlannerPreservesFrozenPlans(GameTestHelper helper) {
        int[] anchors = {0, 8192, 32768};
        String[] hashes = {
            "0d0db88755ed73f296587d516cfd54bdaf368edf5caf61de2d8cd647b04c3093",
            "d2f309ad282d613126eefb8777f211318a344cb361f93cb3f111b41095595da2",
            "0f201469cf49d7259284a1c882a65ba1e18de978101e11f1541cb4f4587a2ccf"
        };
        for (int index = 0; index < anchors.length; index++) {
            for (int pass = 0; pass < 2; pass++) {
                Planner.Plan plan = Planner.create(20261107L, new BlockPos(anchors[index], 100, 0));
                String hash = Catalog.hash(Catalog.GSON.toJson(plan).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                helper.assertTrue(hash.equals(hashes[index]), "Cached planner exactly preserves pre-optimization frozen plan: " + anchors[index]);
            }
        }
        System.out.println("MOSS_PLANNER_EQUIVALENCE_PASS:3 pre-optimization SHA256 baselines;2 passes each;full frozen plan equality");
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
