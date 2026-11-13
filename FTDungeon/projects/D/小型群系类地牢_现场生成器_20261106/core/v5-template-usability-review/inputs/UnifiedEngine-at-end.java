package cn.ft.biomedungeon.runtime;

import cn.ft.biomedungeon.core.DungeonApi;
import cn.ft.biomedungeon.core.next.DungeonEnvelopePlanner;
import com.google.gson.Gson;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class UnifiedEngine {
    static final String VERSION = "runtime-v5-explicit-commit1-envelope-v3-derived07";
    static final int BUDGET = 1024;
    static final String CONFIRMATION = "CONFIRM_ISOLATED_AIRSPACE";
    private static final Gson JSON = new Gson();
    private static final TicketType<ChunkPos> TICKET = TicketType.create("ft_biome_dungeon_generator",
            Comparator.comparingLong(ChunkPos::toLong));
    private final ServerLevel level;
    private final Path folder;
    private final Map<String, Job> jobs = new LinkedHashMap<>();
    private Job running;
    private String loadFailure;
    private static final class Job {
        DungeonApi.Plan plan;
        DungeonApi.Request request;
        String manifestHash;
        String operationHash;
        DungeonApi.Box bounds;
        DungeonApi.Phase phase;
        int cursor;
        int ownedThrough;
        int completedThrough;
        String error = "";
        Placement placement;
        int scan;
        boolean preflight;
        String stage = "PLANNED";
        int observedTicks;
        long lastGameTime = -1;
        String observations = "[]";
        String observationHistory = "[]";
        int restartValidations;
        int nativeFluidEvents;
        String commitChallenge;
        String receiptHash = "";
        boolean commitRequested;
        final List<FluidProtocol> fluids = new ArrayList<>();
        final List<ChunkPos> tickets = new ArrayList<>();
    }

    UnifiedEngine(ServerLevel level) {
        this.level = level;
        folder = level.getServer().getWorldPath(LevelResource.ROOT).resolve("ft_biome_dungeon_candidate_v5")
                .resolve(level.dimension().location().getNamespace()).resolve(level.dimension().location().getPath());
        try {
            if (Files.exists(folder)) {
                try (var files = Files.list(folder)) {
                    List<Path> entries = files.filter(path -> path.getFileName().toString().endsWith(".nbt")).sorted().toList();
                    TemplateCatalog.require(entries.size() <= 128, "Journal job cap exceeded");
                    for (Path path : entries) {
                        CompoundTag data = TemplateCatalog.readNbt(JournalFile.read(path));
                        TemplateCatalog.require(data.getInt("schema") == 5 && data.getLong("worldSeed") == level.getSeed()
                                && data.getString("dimension").equals(level.dimension().location().toString()), "Journal world/schema mismatch");
                        Job job = new Job();
                        job.plan = JSON.fromJson(data.getString("plan"), DungeonApi.Plan.class);
                        job.request = JSON.fromJson(data.getString("request"), DungeonApi.Request.class);
                        TemplateCatalog.require(job.request != null && job.plan != null, "Journal request/plan missing");
                        TemplateCatalog.require(VERSION.equals(job.request.generatorVersion()) && VERSION.equals(job.plan.generatorVersion()),
                                "V2 journal generator/operation policy version mismatch");
                        validateId(job.plan.instanceId());
                        TemplateCatalog.require(path.getFileName().toString().equals(job.plan.instanceId() + ".nbt"), "Journal filename mismatch");
                        job.manifestHash = data.getString("manifestHash");
                        job.operationHash = data.getString("operationHash");
                        job.bounds = JSON.fromJson(data.getString("bounds"), DungeonApi.Box.class);
                        job.phase = DungeonApi.Phase.valueOf(data.getString("phase"));
                        job.cursor = data.getInt("cursor");
                        job.ownedThrough = data.getInt("ownedThrough");
                        job.completedThrough = data.getInt("completedThrough");
                        job.error = data.getString("error");
                        job.stage = data.getString("stage");
                        job.observedTicks = data.getInt("observedTicks");
                        job.observations = data.getString("observations");
                        job.observationHistory = data.contains("observationHistory", 8) ? data.getString("observationHistory") : "[]";
                        job.restartValidations = data.getInt("restartValidations") + 1;
                        job.nativeFluidEvents = data.getInt("nativeFluidEvents");
                        job.commitChallenge = data.getString("commitChallenge");
                        job.receiptHash = data.getString("receiptHash");
                        TemplateCatalog.require(!job.commitChallenge.isBlank(), "Missing v5 challenge binding");
                        if (job.phase == DungeonApi.Phase.COMMITTED) {
                            TemplateCatalog.require(job.stage.equals("COMMITTED_INDEPENDENT_EVIDENCE") && !job.receiptHash.isBlank(), "Invalid committed journal");
                            TemplateCatalog.require(job.receiptHash.equals(verifyReceipt(job, job.receiptHash)), "Committed evidence changed after restart");
                        }
                        jobs.put(job.plan.instanceId(), job);
                    }
                }
            }
        } catch (Exception exception) { loadFailure = exception.toString(); }
    }

    String plan(int x, int y, int z, String biome, int rooms, long ordinal) throws Exception {
        requireReady();
        TemplateCatalog.require(jobs.size() < 128, "Journal job cap reached");
        TemplateCatalog.require(Math.abs((long) x) <= 29_000_000 && Math.abs((long) z) <= 29_000_000
                && y >= 164 && y < level.getMaxBuildHeight() - 16 && (rooms == 7 || rooms == 9), "Invalid isolated anchor/count");
        TemplateCatalog catalog = new CandidateCatalog(level, rooms).catalog;
        List<DungeonApi.Box> occupied = jobs.values().stream().map(job -> job.bounds).toList();
        DungeonApi.Request request = new DungeonApi.Request(level.getSeed(), level.dimension().location().toString(),
                new DungeonApi.Vec3i(x, y, z), ordinal, VERSION, biome, rooms, 50_000);
        for (Job previous : jobs.values()) {
            if (previous.request.equals(request)) {
                TemplateCatalog.require(previous.manifestHash.equals(catalog.manifestHash), "Catalog changed for existing request");
                return "Existing request: " + status(previous.plan.instanceId());
            }
        }
        DungeonApi.Plan plan = DungeonEnvelopePlanner.plan(request,
                catalog.templates.values().stream().map(TemplateCatalog.Loaded::metadata).toList(), occupied,
                new DungeonEnvelopePlanner.Limits(164, level.getMaxBuildHeight() - 4, 64, true, (int) Math.floorMod(ordinal, 4)),
                DungeonEnvelopePlanner.EnvelopeBudget.isolatedRuntime());
        validateId(plan.instanceId());
        Job existing = jobs.get(plan.instanceId());
        if (existing != null) { return status(plan.instanceId()); }
        Placement placement = new Placement(plan, catalog);
        checkBounds(placement.bounds);
        for (Job other : jobs.values()) {
            TemplateCatalog.require(!overlap(placement.bounds, other.bounds), "Persistent reservation overlap");
        }
        Job job = new Job();
        job.plan = plan;
        job.request = request;
        job.bounds = placement.bounds;
        job.manifestHash = catalog.manifestHash;
        job.operationHash = placement.digest;
        job.phase = DungeonApi.Phase.PLANNED;
        job.commitChallenge = java.util.UUID.randomUUID().toString();
        job.placement = placement;
        persist(job);
        jobs.put(plan.instanceId(), job);
        return "DRY RUN " + plan.instanceId() + " rooms=" + plan.rooms().size() + " cells=" + placement.operations.size()
                + " anchor=" + x + "," + y + "," + z + " bounds=" + JSON.toJson(job.bounds)
                + "; no world writes. build <id> " + CONFIRMATION;
    }

    String build(String id, String confirmation) throws Exception {
        requireReady();
        TemplateCatalog.require(CONFIRMATION.equals(confirmation), "Explicit isolated-airspace confirmation required");
        Job job = get(id);
        TemplateCatalog.require(job.phase != DungeonApi.Phase.COMMITTED, "Committed job is read-only; no build/replay");
        TemplateCatalog.require(running == null, "One job at a time per dimension");
        TemplateCatalog.require(job.phase != DungeonApi.Phase.ROLLBACK_REQUIRED && job.phase != DungeonApi.Phase.FAILED,
                "Conflict quarantined; manual inspection required, no rollback promised");
        TemplateCatalog catalog = new CandidateCatalog(level, job.request.roomCount()).catalog;
        TemplateCatalog.require(catalog.manifestHash.equals(job.manifestHash), "Manifest changed since plan");
        job.placement = new Placement(job.plan, catalog);
        job.fluids.clear();
        for (var room : job.plan.rooms()) {
            if (room.templateId().equals("s8_08")) {
                job.fluids.add(new FluidProtocol(level, TemplateCatalog.resource("/data/ftbd_candidate/s8_08.nbt", 4_000_000), room.origin(), room.quarterTurns()));
            }
        }
        TemplateCatalog.require(job.placement.digest.equals(job.operationHash) && job.placement.bounds.equals(job.bounds), "Operation digest/version mismatch");
        TemplateCatalog.require(job.cursor >= 0 && job.completedThrough >= job.cursor && job.ownedThrough >= job.completedThrough
                && job.ownedThrough <= job.placement.operations.size(), "Corrupt journal cursor");
        checkBounds(job.bounds);
        acquire(job);
        try {
            TemplateCatalog.require(job.nativeFluidEvents == 0, "Native fluid/fire placement event observed; quarantined without cancellation");
            checkEntities(job);
            job.scan = 0;
            job.preflight = true;
            job.lastGameTime = -1;
            running = job;
            return "Preflight scheduled; zero writes until full scan succeeds. " + id;
        } catch (Exception exception) { release(job); throw exception; }
    }

    String status(String id) {
        Job job = get(id);
        return id + " phase=" + job.phase + " cursor=" + job.cursor + " attempted=" + job.ownedThrough
                + " active=" + (running == job) + " tickets=" + job.tickets.size() + " stage=" + job.stage + " observedTicks=" + job.observedTicks + " error=" + job.error;
    }

    String list() {
        if (loadFailure != null) { return "DISABLED: " + loadFailure; }
        return jobs.isEmpty() ? "No plans" : String.join("\n", jobs.keySet().stream().map(this::status).toList());
    }

    List<String> planIds() { return List.copyOf(jobs.keySet()); }

    String details(String id) { return JSON.toJson(get(id).plan); }

    String pause(String id) throws IOException {
        Job job = get(id);
        if (running == job) {
            job.commitRequested = false;
            job.receiptHash = "";
            persist(job);
            release(job);
            running = null;
        }
        return "Paused without rollback. Use build with confirmation to revalidate and resume: " + status(id);
    }

    void tick() {
        Job job = running;
        if (job == null) { return; }
        try {
            TemplateCatalog.require(job.nativeFluidEvents == 0, "Uncancelled native fluid/fire event observed");
            checkEntities(job);
            if (job.preflight) {
                int end = Math.min(job.scan + BUDGET * 8, job.placement.operations.size());
                while (job.scan < end) {
                    Placement.Operation operation = job.placement.operations.get(job.scan);
                    checkCell(job, operation, job.scan, true);
                    job.scan++;
                }
                if (job.scan == job.placement.operations.size()) {
                    if (job.phase == DungeonApi.Phase.PLANNED || job.phase == DungeonApi.Phase.RESERVED) {
                        job.phase = DungeonApi.Phase.BUILDING;
                        job.stage = "SOLIDS";
                    }
                    if (job.phase == DungeonApi.Phase.COMMITTED) {
                        job.phase = DungeonApi.Phase.VERIFYING;
                        job.scan = 0;
                    }
                    persist(job);
                    job.preflight = false;
                    job.scan = 0;
                }
                return;
            }
            if (job.phase == DungeonApi.Phase.BUILDING) {
                int end = Math.min(job.cursor + BUDGET, job.placement.operations.size());
                for (int index = job.cursor; index < end; index++) {
                    checkCell(job, job.placement.operations.get(index), index, true);
                }
                job.ownedThrough = Math.max(job.ownedThrough, end);
                persist(job);
                while (job.cursor < end) {
                    Placement.Operation operation = job.placement.operations.get(job.cursor);
                    String nextStage = operation.state().is(net.minecraft.world.level.block.Blocks.LAVA) ? "LAVA_LAST"
                            : operation.state().is(net.minecraft.world.level.block.Blocks.VINE) ? "VINES_AFTER_SUPPORTS" : "SOLIDS";
                    if (!nextStage.equals(job.stage)) {
                        job.stage = nextStage;
                        persist(job);
                        if (nextStage.equals("LAVA_LAST")) { validateIgnition(job); }
                    }
                    PayloadWorld.place(level, operation, job.cursor, job.completedThrough, job.ownedThrough);
                    job.cursor++;
                }
                long checkpointStarted = System.nanoTime();
                level.getChunkSource().save(true);
                job.completedThrough = Math.max(job.completedThrough, job.cursor);
                persist(job);
                DungeonMod.LOGGER.info("V5_DURABLE_CHECKPOINT id={} completed={} owned={} saveMillis={}", job.plan.instanceId(), job.completedThrough,
                        job.ownedThrough, (System.nanoTime() - checkpointStarted) / 1_000_000);
                if (job.cursor == job.placement.operations.size()) {
                    job.phase = DungeonApi.Phase.VERIFYING;
                    job.stage = "NATIVE_UPDATES";
                    job.scan = 0;
                    persist(job);
                }
            } else if (job.phase == DungeonApi.Phase.VERIFYING) {
                if (job.stage.equals("NATIVE_UPDATES")) {
                    int end = Math.min(job.scan + BUDGET, job.placement.operations.size());
                    while (job.scan < end) {
                        var position = job.placement.operations.get(job.scan++).position();
                        level.updateNeighborsAt(position, level.getBlockState(position).getBlock());
                        level.getBlockState(position).updateNeighbourShapes(level, position, 3);
                    }
                    persist(job);
                    if (job.scan == job.placement.operations.size()) {
                        for (var fluid : job.fluids) { fluid.schedule(level); }
                        job.stage = "NATURAL_TICK_OBSERVATION";
                        job.scan = 0;
                        persist(job);
                    }
                    return;
                }
                if (job.stage.equals("NATURAL_TICK_OBSERVATION")) {
                    long now = level.getGameTime();
                    if (job.lastGameTime >= 0 && now > job.lastGameTime) { job.observedTicks++; }
                    job.lastGameTime = now;
                    if (job.observedTicks % 100 == 0) { observe(job); persist(job); }
                    if (job.observedTicks >= (job.fluids.isEmpty() ? 100 : 2400)) {
                        observe(job);
                        job.stage = "SEMANTIC_READBACK";
                        job.scan = 0;
                        persist(job);
                    }
                    return;
                }
                int end = Math.min(job.scan + BUDGET * 8, job.placement.operations.size());
                while (job.scan < end) {
                    Placement.Operation operation = job.placement.operations.get(job.scan++);
                    TemplateCatalog.require(semanticMatches(job, operation), "Readback state/BE mismatch at " + operation.position());
                }
                if (job.scan == job.placement.operations.size()) {
                    level.getChunkSource().save(true);
                    TransitionVerifier.verify(level, job.plan);
                    level.getChunkSource().save(true);
                    if (job.commitRequested) {
                        observe(job);
                        TemplateCatalog.require(job.receiptHash.equals(verifyReceipt(job, job.receiptHash)), "Acceptance changed during final readback");
                        job.phase = DungeonApi.Phase.COMMITTED;
                        job.stage = "COMMITTED_INDEPENDENT_EVIDENCE";
                        job.commitRequested = false;
                    } else {
                        job.commitChallenge = java.util.UUID.randomUUID().toString();
                        job.receiptHash = "";
                        job.stage = "PAYLOAD_VERIFIED_ROUTE_INTERACTION_ACCEPTANCE_PENDING";
                    }
                    persist(job);
                    release(job);
                    running = null;
                }
            }
        } catch (Exception exception) {
            job.commitRequested = false;
            job.phase = job.ownedThrough == 0 ? DungeonApi.Phase.FAILED : DungeonApi.Phase.ROLLBACK_REQUIRED;
            job.error = exception.toString();
            try { persist(job); } catch (Exception persistenceFailure) { loadFailure = persistenceFailure.toString(); }
            release(job);
            running = null;
            DungeonMod.LOGGER.error("Dungeon job quarantined {}: {}", job.plan.instanceId(), job.error);
        }
    }

    private void checkCell(Job job, Placement.Operation operation, int index, boolean allowOwned) {
        if (index < job.completedThrough) {
            TemplateCatalog.require(semanticMatches(job, operation), "Completed owned cell changed; never overwrite: " + operation.position());
            return;
        }
        TemplateCatalog.require(PayloadWorld.mayReplay(level, operation, index, job.completedThrough,
                allowOwned ? job.ownedThrough : 0), "Occupied/protected state or BE at " + operation.position());
    }

    private void checkEntities(Job job) {
        DungeonApi.Box box = job.bounds;
        AABB region = new AABB(box.min().x() - 2, box.min().y() - 2, box.min().z() - 2,
                box.maxExclusive().x() + 2, box.maxExclusive().y() + 2, box.maxExclusive().z() + 2);
        TemplateCatalog.require(level.getEntities((net.minecraft.world.entity.Entity) null, region, entity -> true).isEmpty(), "Player/entity intersects protected envelope");
    }

    boolean protectedAt(BlockPos position) {
        if (loadFailure != null) { return true; }
        for (Job job : jobs.values()) {
            if (Placement.contains(job.bounds, position)) { return true; }
        }
        return false;
    }

    boolean committedAt(BlockPos position) {
        return loadFailure == null && jobs.values().stream().anyMatch(job -> job.phase == DungeonApi.Phase.COMMITTED && Placement.contains(job.bounds, position));
    }

    void close() {
        if (running != null) {
            try { persist(running); } catch (Exception exception) { DungeonMod.LOGGER.error("Journal close failed", exception); }
            release(running);
            running = null;
        }
    }

    private void acquire(Job job) {
        try {
            for (int chunkX = job.bounds.min().x() >> 4; chunkX <= (job.bounds.maxExclusive().x() - 1) >> 4; chunkX++) {
                for (int chunkZ = job.bounds.min().z() >> 4; chunkZ <= (job.bounds.maxExclusive().z() - 1) >> 4; chunkZ++) {
                    ChunkPos position = new ChunkPos(chunkX, chunkZ);
                    level.getChunkSource().addRegionTicket(TICKET, position, 2, position, true);
                    job.tickets.add(position);
                    level.getChunk(chunkX, chunkZ);
                }
            }
        } catch (Exception exception) { release(job); throw exception; }
    }

    private void release(Job job) {
        for (ChunkPos position : job.tickets) { level.getChunkSource().removeRegionTicket(TICKET, position, 2, position, true); }
        job.tickets.clear();
    }

    private void persist(Job job) throws IOException {
        CompoundTag data = new CompoundTag();
        data.putInt("schema", 5);
        data.putString("commitChallenge", job.commitChallenge);
        data.putString("receiptHash", job.receiptHash);
        data.putString("stage", job.stage);
        data.putInt("observedTicks", job.observedTicks);
        data.putString("observations", job.observations);
        data.putString("observationHistory", job.observationHistory);
        data.putInt("restartValidations", job.restartValidations);
        data.putInt("nativeFluidEvents", job.nativeFluidEvents);
        data.putLong("worldSeed", level.getSeed());
        data.putString("dimension", level.dimension().location().toString());
        data.putString("plan", JSON.toJson(job.plan));
        data.putString("request", JSON.toJson(job.request));
        data.putString("bounds", JSON.toJson(job.bounds));
        data.putString("manifestHash", job.manifestHash);
        data.putString("operationHash", job.operationHash);
        data.putString("phase", job.phase.name());
        data.putInt("cursor", job.phase == DungeonApi.Phase.BUILDING ? job.completedThrough : job.cursor);
        data.putInt("ownedThrough", job.ownedThrough);
        data.putInt("completedThrough", job.completedThrough);
        data.putString("error", job.error);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        NbtIo.writeCompressed(data, output);
        JournalFile.write(folder.resolve(job.plan.instanceId() + ".nbt"), output.toByteArray());
    }

    private Job get(String id) {
        validateId(id);
        Job job = jobs.get(id);
        TemplateCatalog.require(job != null, "Unknown plan ID");
        return job;
    }

    private void requireReady() throws IOException {
        TemplateCatalog.require(loadFailure == null, "Journal load failed; runtime disabled: " + loadFailure);
        List<String> unsupportedMods = net.minecraftforge.fml.ModList.get().getMods().stream()
                .map(net.minecraftforge.forgespi.language.IModInfo::getModId)
                .filter(id -> !java.util.Set.of("minecraft", "forge", DungeonMod.ID).contains(id)).sorted().toList();
        TemplateCatalog.require(unsupportedMods.isEmpty(), "Unintegrated mods/protection systems: " + unsupportedMods);
        Path world = level.getServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
        for (Path component = world; component != null; component = component.getParent()) {
            TemplateCatalog.require(!Files.isSymbolicLink(component), "Linked world path rejected");
        }
        TemplateCatalog.require(!world.toString().toLowerCase(java.util.Locale.ROOT).contains(".minecraft"), "Formal instance world forbidden");
        TemplateCatalog.require(world.toRealPath().equals(world), "Reparse/redirected world path rejected");
        Path marker = world.resolve("ALLOW_CANDIDATE_EVIDENCE_WORLD_V5");
        TemplateCatalog.require(Files.isRegularFile(marker) && Files.readString(marker, StandardCharsets.UTF_8).trim()
                .equals("PARENT_AUTHORIZED_EIGHT_ORIGINALS_ONE_DERIVED_V5_EVIDENCE_ONLY"), "Strict candidate world authorization marker missing");
        TemplateCatalog.require(level.getGameRules().getInt(net.minecraft.world.level.GameRules.RULE_RANDOMTICKING) == 3
                && level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOFIRETICK), "Natural gamerules required, never suppressed");
    }

    private void checkBounds(DungeonApi.Box bounds) {
        TemplateCatalog.require(bounds.min().y() >= 160 && bounds.maxExclusive().y() < level.getMaxBuildHeight(), "Only isolated high-altitude empty air supported");
        for (int x : new int[]{bounds.min().x(), bounds.maxExclusive().x() - 1}) {
            for (int z : new int[]{bounds.min().z(), bounds.maxExclusive().z() - 1}) {
                TemplateCatalog.require(level.getWorldBorder().isWithinBounds(new BlockPos(x, bounds.min().y(), z)), "Outside world border");
            }
        }
    }

    private static boolean overlap(DungeonApi.Box first, DungeonApi.Box second) {
        return first.min().x() < second.maxExclusive().x() && first.maxExclusive().x() > second.min().x()
                && first.min().y() < second.maxExclusive().y() && first.maxExclusive().y() > second.min().y()
                && first.min().z() < second.maxExclusive().z() && first.maxExclusive().z() > second.min().z();
    }

    private static void validateId(String id) { TemplateCatalog.require(id != null && id.matches("[a-zA-Z0-9_-]{1,128}"), "Unsafe instance ID"); }

    String evidence(String id) {
        Job job = get(id);
        return JSON.toJson(Map.of("stage", job.stage, "ticks", job.observedTicks, "restartValidations", job.restartValidations,
                "observations", job.observations, "manifestHash", job.manifestHash, "operationHash", job.operationHash,
                "independentRoutes", "NOT_RUN", "interactions", "NOT_RUN", "promotion", false));
    }

    String routeAudit(String id) throws Exception {
        requireReady();
        Job job = get(id);
        TemplateCatalog.require(running == null && job.stage.startsWith("PAYLOAD_VERIFIED"), "Payload verification must finish before optional native route audit");
        acquire(job);
        try {
            var report = NativeRouteAudit.run(level, job.plan);
            AtomicFiles.write(folder.resolve(id + "-routes.json"), JSON.toJson(report).getBytes(StandardCharsets.UTF_8));
            return "Native route audit attempted=" + report.get("attempted") + " passed=" + report.get("passed") + "; coordinate semantics review and remaining acceptance pending";
        } finally { release(job); }
    }

    private boolean semanticMatches(Job job, Placement.Operation operation) {
        if (PayloadWorld.matches(level, operation)) { return true; }
        if (operation.payload() != null || level.getBlockEntity(operation.position()) != null) { return false; }
        var actual = level.getBlockState(operation.position());
        boolean nativeStage = job.stage.equals("NATIVE_UPDATES") || job.stage.equals("NATURAL_TICK_OBSERVATION")
                || job.stage.equals("SEMANTIC_READBACK") || job.stage.startsWith("PAYLOAD_VERIFIED") || job.stage.equals("COMMITTED_INDEPENDENT_EVIDENCE");
        if (!nativeStage) { return false; }
        if (actual.is(net.minecraft.world.level.block.Blocks.VINE) && (operation.state().isAir() || operation.state().is(net.minecraft.world.level.block.Blocks.VINE))
                && job.fluids.stream().anyMatch(fluid -> Placement.contains(fluid.bounds, operation.position()))) {
            return actual.canSurvive(level, operation.position());
        }
        if (actual.getBlock() != operation.state().getBlock()) { return false; }
        if (!(actual.getBlock() instanceof net.minecraft.world.level.block.WallBlock)
                && !(actual.getBlock() instanceof net.minecraft.world.level.block.StairBlock)) { return false; }
        var expected = operation.state();
        for (int pass = 0; pass < 2; pass++) {
            for (var direction : net.minecraft.core.Direction.values()) {
                expected = expected.updateShape(direction, level.getBlockState(operation.position().relative(direction)), level,
                        operation.position(), operation.position().relative(direction));
            }
        }
        return expected.equals(actual);
    }

    private void validateIgnition(Job job) {
        for (var fluid : job.fluids) {
            for (var vine : fluid.vines) { TemplateCatalog.require(level.getBlockState(vine).canSurvive(level, vine), "Unsupported original vine before lava"); }
            for (var lava : fluid.lava) {
                for (var direction : net.minecraft.core.Direction.values()) {
                    if (direction == net.minecraft.core.Direction.UP) { continue; }
                    BlockPos near = lava.relative(direction);
                    TemplateCatalog.require(fluid.lava.contains(near) || level.getBlockState(near).is(net.minecraft.world.level.block.Blocks.DEEPSLATE_BRICKS), "Original closed basin changed");
                }
                for (int offsetX = -3; offsetX <= 3; offsetX++) {
                    for (int offsetY = 0; offsetY <= 4; offsetY++) {
                        for (int offsetZ = -3; offsetZ <= 3; offsetZ++) {
                            BlockPos near = lava.offset(offsetX, offsetY, offsetZ);
                            for (var face : net.minecraft.core.Direction.values()) { TemplateCatalog.require(!level.getBlockState(near).isFlammable(level, near, face), "Native ignition fuel near basin"); }
                        }
                    }
                }
            }
        }
    }

    private void observe(Job job) {
        List<Object> observations = new ArrayList<>();
        for (var fluid : job.fluids) {
            var observation = fluid.observe(level);
            TemplateCatalog.require(observation.allTicking() && observation.lavaCount() == 9 && observation.fires() == 0
                    && observation.escapedFluid() == 0 && observation.unsupportedVines() == 0 && observation.foreignBlockChanges() == 0, "Native fluid observation conflict " + observation);
            observations.add(Map.of("worldTime", level.getGameTime(), "observation", observation, "changes", fluid.stateDifferences(level)));
        }
        job.observations = JSON.toJson(observations);
        var history = com.google.gson.JsonParser.parseString(job.observationHistory).getAsJsonArray();
        for (var fluid : job.fluids) {
            history.add(JSON.toJsonTree(Map.of("worldTime", level.getGameTime(), "observedTicks", job.observedTicks, "observation", fluid.observe(level))));
        }
        TemplateCatalog.require(history.size() <= 100, "Bounded observation history cap");
        job.observationHistory = history.toString();
    }

    void nativeFluidEvent(BlockPos position) {
        for (Job job : jobs.values()) {
            if (Placement.contains(job.bounds, position)) {
                job.nativeFluidEvents++;
                if (job.phase == DungeonApi.Phase.COMMITTED) {
                    job.phase = DungeonApi.Phase.ROLLBACK_REQUIRED;
                    job.error = "Native fluid/fire placement after commit; no event canceled";
                }
                try { persist(job); } catch (IOException exception) { loadFailure = exception.toString(); }
            }
        }
    }

    private Path world() { return level.getServer().getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize(); }

    String acceptanceContext(String id) throws Exception {
        requireReady();
        return JSON.toJson(binding(get(id)));
    }

    private Map<String, String> binding(Job job) throws Exception {
        Path artifact = net.minecraftforge.fml.ModList.get().getModFileById(DungeonMod.ID).getFile().getFilePath();
        TemplateCatalog.require(Files.isRegularFile(artifact) && artifact.toString().endsWith(".jar"), "Commit requires installed release JAR, source-run acceptance forbidden");
        Map<String, String> result = new LinkedHashMap<>();
        result.put("instanceId", job.plan.instanceId());
        result.put("worldIdentity", AtomicFiles.sha256((world().toRealPath() + ":" + level.getSeed() + ":" + level.dimension().location()).getBytes(StandardCharsets.UTF_8)));
        result.put("worldSeed", Long.toString(level.getSeed()));
        result.put("dimension", level.dimension().location().toString());
        result.put("runtimeVersion", VERSION);
        result.put("jarSha256", AtomicFiles.sha256(Files.readAllBytes(artifact)));
        result.put("manifestHash", job.manifestHash);
        result.put("operationHash", job.operationHash);
        result.put("planSha256", AtomicFiles.sha256(JSON.toJson(job.plan).getBytes(StandardCharsets.UTF_8)));
        result.put("commitChallenge", job.commitChallenge);
        return result;
    }

    private String verifyReceipt(Job job, String expectedHash) throws Exception {
        return CommitEvidence.verify(world(), job.plan.instanceId(), binding(job), expectedHash);
    }

    String commit(String id, String evidenceSha256, String confirmation) throws Exception {
        requireReady();
        TemplateCatalog.require(confirmation.equals("CONFIRM_INDEPENDENT_EVIDENCE_COMMIT"), "Explicit commit confirmation required");
        Job job = get(id);
        TemplateCatalog.require(running == null && job.phase == DungeonApi.Phase.VERIFYING
                && job.stage.equals("PAYLOAD_VERIFIED_ROUTE_INTERACTION_ACCEPTANCE_PENDING"), "Commit requires idle payload-verified pending job");
        String receipt = verifyReceipt(job, evidenceSha256);
        build(id, CONFIRMATION);
        job.receiptHash = receipt;
        job.commitRequested = true;
        persist(job);
        return "Independent evidence hash admitted by explicit operator command; full readback pending, NOT COMMITTED: " + id;
    }

    void externalChange(BlockPos position, String reason) {
        for (Job job : jobs.values()) {
            if (!Placement.contains(job.bounds, position)) { continue; }
            job.commitRequested = false;
            job.receiptHash = "";
            job.commitChallenge = java.util.UUID.randomUUID().toString();
            job.error = "Protected external action observed: " + reason;
            job.phase = job.ownedThrough == 0 ? DungeonApi.Phase.FAILED : DungeonApi.Phase.ROLLBACK_REQUIRED;
            if (running == job) { release(job); running = null; }
            try { persist(job); } catch (Exception exception) { loadFailure = exception.toString(); }
        }
    }
}
