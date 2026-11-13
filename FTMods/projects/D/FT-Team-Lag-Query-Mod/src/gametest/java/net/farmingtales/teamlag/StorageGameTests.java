package net.farmingtales.teamlag;

import com.refinedmods.refinedstorage.apiimpl.API;
import com.refinedmods.refinedstorage.apiimpl.network.NetworkListener;
import com.refinedmods.refinedstorage.apiimpl.network.node.ImporterNetworkNode;
import com.refinedmods.refinedstorage.blockentity.ControllerBlockEntity;
import com.refinedmods.refinedstorage.blockentity.NetworkNodeBlockEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import observable.Observable;
import observable.Props;
import observable.server.Profiler;
import observable.server.ProfilingData;
import observable.server.TraceMap;

@GameTestHolder(LagQuery.ID)
@PrefixGameTestTemplate(false)
public final class StorageGameTests {
    @GameTest(template = "arena", timeoutTicks = 100, batch = "storage")
    public static void storageLocalTimingWorksWithoutObservableProfile(GameTestHelper helper) throws Exception {
        try (StorageFixture fixture = new StorageFixture(helper); AutoCloseable scope = hasObservable() ? new ObservationScope(false) : null) {
            var node = new CountingImporter(helper.getLevel(), fixture.nodePos, false);
            API.instance().getNetworkNodeManager(helper.getLevel()).setNode(fixture.nodePos, node);
            double controllerBefore = LagQuery.measuredMicros(fixture.controllerKey());
            double nodeBefore = LagQuery.measuredMicros(fixture.nodeKey());
            tickStorage(helper.getLevel());
            helper.assertTrue(node.updates == 1, "original node update called exactly once");
            helper.assertTrue(LagQuery.measuredMicros(fixture.controllerKey()) > controllerBefore, "controller own-chunk time recorded without OBS sampling");
            helper.assertTrue(LagQuery.measuredMicros(fixture.nodeKey()) > nodeBefore, "node own-chunk time recorded without OBS sampling");
        }
        helper.succeed();
    }
    @GameTest(template = "arena", timeoutTicks = 100, batch = "storage")
    public static void storageAppearsInObservableAndKeepsInventory(GameTestHelper helper) {
        if (!hasObservable()) { helper.succeed(); return; }
        try (StorageFixture fixture = new StorageFixture(helper); ObservationScope scope = new ObservationScope(true)) {
            fixture.node.getItemFilters().setStackInSlot(0, new ItemStack(Items.DIAMOND, 64));
            double controllerBefore = LagQuery.measuredMicros(fixture.controllerKey());
            double nodeBefore = LagQuery.measuredMicros(fixture.nodeKey());
            tickStorage(helper.getLevel());
            var timings = scope.profiler.getBlockTimingsMap().get(helper.getLevel().dimension());
            var controller = timings.get(fixture.controllerPos); var node = timings.get(fixture.nodePos);
            helper.assertTrue(controller != null && controller.getTime() > 0 && controller.getTicks() == 1, "RS controller is visible in original OBS overlay");
            helper.assertTrue(node != null && node.getTime() > 0 && node.getTicks() == 1, "RS importer is visible at its own position");
            helper.assertTrue(controller.getName().startsWith("block.refinedstorage.") && node.getName().startsWith("block.refinedstorage."), "real RS block labels retained");
            helper.assertTrue(Math.abs(LagQuery.measuredMicros(fixture.controllerKey()) - controllerBefore - controller.getTime() / 1000.0) < 0.01, "controller OBS and native use identical sample");
            helper.assertTrue(Math.abs(LagQuery.measuredMicros(fixture.nodeKey()) - nodeBefore - node.getTime() / 1000.0) < 0.01, "node sample not charged to controller chunk");
            helper.assertTrue(Props.currentTarget.get() == scope.sentinel, "OBS previous target restored");
            helper.assertTrue(fixture.node.getItemFilters().getStackInSlot(0).getCount() == 64, "no RS inventory confiscation");
        }
        helper.succeed();
    }
    @GameTest(template = "arena", timeoutTicks = 100, batch = "storage")
    public static void storageErrorsAreNotSwallowedOrRetried(GameTestHelper helper) throws Exception {
        try (StorageFixture fixture = new StorageFixture(helper); AutoCloseable scope = hasObservable() ? new ObservationScope(true) : null) {
            var node = new CountingImporter(helper.getLevel(), fixture.nodePos, true);
            API.instance().getNetworkNodeManager(helper.getLevel()).setNode(fixture.nodePos, node);
            boolean thrown = false;
            try { tickStorage(helper.getLevel()); } catch (IllegalStateException error) { thrown = error.getMessage().equals("RS test failure"); }
            helper.assertTrue(thrown && node.updates == 1, "RS original exception propagated without duplicate update");
            if (scope instanceof ObservationScope observation) helper.assertTrue(Props.currentTarget.get() == observation.sentinel, "failing update restores OBS context");
        }
        helper.succeed();
    }
    @GameTest(template = "arena", timeoutTicks = 100, batch = "storage")
    public static void timingNeverLoadsDistantChunksOrRunsOnWorker(GameTestHelper helper) {
        var level = helper.getLevel(); var pos = new BlockPos(-448007, 64, -448009);
        helper.assertTrue(level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) == null, "position initially unloaded");
        helper.assertTrue(RefinedStorageTiming.begin(null, pos, String.class) == null, "client/null world ignored");
        helper.assertTrue(CompletableFuture.supplyAsync(() -> RefinedStorageTiming.begin(level, pos, String.class)).join() == null, "worker thread ignored");
        var sample = RefinedStorageTiming.begin(level, pos, String.class); RefinedStorageTiming.end(sample);
        helper.assertTrue(level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) == null, "timing does not force load");
        helper.succeed();
    }
    @GameTest(template = "arena", timeoutTicks = 160, batch = "storageReport")
    public static void realObservableReportReachesTeamQuery(GameTestHelper helper) {
        if (!hasObservable()) { helper.succeed(); return; }
        int applied = ObservableReports.appliedCount();
        try (StorageFixture fixture = new StorageFixture(helper); ObservationScope scope = new ObservationScope(true)) {
            tickStorage(helper.getLevel());
            var report = ProfilingData.Companion.create(Map.of(), scope.profiler.getBlockTimingsMap(), 100, new TraceMap());
            helper.assertTrue(report.getBlocks().get(helper.getLevel().dimension().location()).size() == 2, "real OBS report contains both RS positions");
        }
        helper.succeedWhen(() -> helper.assertTrue(ObservableReports.appliedCount() > applied && LagQuery.observedChunks() == 2, "OBS report callback updates query samples"));
    }
    private static boolean hasObservable() { return ModList.get().isLoaded("observable"); }
    private static void tickStorage(ServerLevel level) { new NetworkListener().onLevelTick(new TickEvent.LevelTickEvent(LogicalSide.SERVER, TickEvent.Phase.END, level, () -> true)); }
    private static final class StorageFixture implements AutoCloseable {
        final ServerLevel level;
        final BlockPos controllerPos;
        final BlockPos nodePos;
        final ControllerBlockEntity controller;
        final ImporterNetworkNode node;

        StorageFixture(GameTestHelper helper) {
            level = helper.getLevel();
            controllerPos = helper.absolutePos(new BlockPos(1, 2, 1));
            nodePos = helper.absolutePos(new BlockPos(18, 2, 1));
            level.setBlockAndUpdate(controllerPos, ForgeRegistries.BLOCKS.getValue(new ResourceLocation("refinedstorage:creative_controller")).defaultBlockState());
            level.setBlockAndUpdate(nodePos, ForgeRegistries.BLOCKS.getValue(new ResourceLocation("refinedstorage:importer")).defaultBlockState());
            controller = (ControllerBlockEntity) level.getBlockEntity(controllerPos);
            controller.getNetwork();
            node = (ImporterNetworkNode) ((NetworkNodeBlockEntity<?>) level.getBlockEntity(nodePos)).getNode();
        }

        ChunkKey controllerKey() { return ChunkKey.at(level, controllerPos); }
        ChunkKey nodeKey() { return ChunkKey.at(level, nodePos); }

        @Override public void close() {
            level.removeBlock(nodePos, false);
            level.removeBlock(controllerPos, false);
            API.instance().getNetworkNodeManager(level).removeNode(nodePos);
            API.instance().getNetworkManager(level).removeNetwork(controllerPos);
        }
    }

    private static final class CountingImporter extends ImporterNetworkNode {
        private final boolean failing;
        private int updates;

        CountingImporter(ServerLevel level, BlockPos pos, boolean failing) {
            super(level, pos);
            this.failing = failing;
        }

        @Override public void update() {
            updates++;
            if (failing) throw new IllegalStateException("RS test failure");
            super.update();
        }
    }

    private static final class ObservationScope implements AutoCloseable {
        final Profiler profiler = Observable.INSTANCE.getPROFILER();
        final Profiler.TimingData sentinel = new Profiler.TimingData(0L, 0, new TraceMap(), "RS parent test");
        private final boolean inactive = Props.notProcessing;
        private final Profiler.TimingData previous = Props.currentTarget.get();
        private final HashMap<ResourceKey<Level>, HashMap<BlockPos, Profiler.TimingData>> timings = profiler.getBlockTimingsMap();

        ObservationScope(boolean active) {
            profiler.setBlockTimingsMap(new HashMap<>());
            Props.notProcessing = !active;
            Props.currentTarget.set(sentinel);
        }

        @Override public void close() {
            Props.notProcessing = inactive;
            Props.currentTarget.set(previous);
            profiler.setBlockTimingsMap(timings);
        }
    }
}
