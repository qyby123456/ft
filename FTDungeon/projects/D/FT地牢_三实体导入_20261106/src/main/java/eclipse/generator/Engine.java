package eclipse.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.nio.charset.StandardCharsets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class Engine {
    private static final Map<Job, Specification> CACHE = new WeakHashMap<>();
    private static final int MAX_BATCH = 8192;
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;

    public static final class Job {
        public Planner.Plan plan;
        public String stage = "PLANNED";
        public int pieceCursor;
        public int cellCursor;
        public String error;
        public Gameplay.State gameplay;
        public String resumeStage;
        public int writeCursor;
        public int checkPiece;
        public long checkCell;
        public int pendingStart = -1;
        public int pendingEnd;
        public int engineVersion = 1;
        public String planHash;
        public transient int batchCursor = -1;
    }

    private record Write(BlockPos position, BlockState state, CompoundTag nbt, Rotation rotation, int piece, int cell) {}
    private record Specification(List<Write> writes, Map<BlockPos, Write> cells) {}

    private Engine() {}

    static boolean active(Job job) {
        return "CHECKING".equals(job.stage) || "BUILDING".equals(job.stage) || "INITIALIZING".equals(job.stage);
    }

    private static void dimension(ServerLevel level) {
        if (!level.dimension().equals(EclipseMod.DIMENSION)) throw new IllegalArgumentException("Eclipse requires its dedicated dimension");
        if (!level.getServer().isSameThread()) throw new IllegalStateException("Eclipse mutation requires server thread");
    }

    public static Job plan(ServerLevel level, long seed, BlockPos anchor) {
        dimension(level);
        Planner.Plan selected = Planner.create(seed, anchor);
        validatePlan(selected);
        Store store = Store.get(level);
        Job existing = store.jobs.get(selected.id);
        if (existing != null) {
            if (existing.plan.seed != seed || !Catalog.pos(existing.plan.anchor).equals(anchor)
                || !existing.plan.fingerprint.equals(selected.fingerprint)) throw new IllegalStateException("Plan ID collision");
            return existing;
        }
        validateWorld(level, selected);
        for (Job other : store.jobs.values()) assertSeparate(selected, other.plan);
        Job job = new Job();
        job.plan = selected;
        specification(level, job, true);
        job.planHash = planHash(selected);
        store.jobs.put(selected.id, job);
        Store.flush(level);
        return job;
    }

    public static void start(ServerLevel level, String id) {
        dimension(level);
        Store store = Store.get(level);
        Job job = require(store, id);
        if ("COMPLETE".equals(job.stage)) return;
        if ("FAILED".equals(job.stage)) throw new IllegalStateException("Failed job reserved for inspection: " + job.error);
        for (Job other : store.jobs.values()) if (other != job && active(other)) throw new IllegalStateException("Another job is active");
        validateWorld(level, job.plan);
        validateFrozen(job);
        validateReservations(store);
        specification(level, job, false);
        if ("PAUSED".equals(job.stage)) {
            if (job.resumeStage == null || !Set.of("CHECKING", "BUILDING", "INITIALIZING").contains(job.resumeStage)) {
                throw new IllegalStateException("Invalid paused job stage");
            }
            job.stage = job.resumeStage;
            if ("CHECKING".equals(job.stage)) { job.checkPiece = 0; job.checkCell = 0; }
        } else if ("PLANNED".equals(job.stage)) {
            job.stage = "CHECKING";
            job.checkPiece = 0;
            job.checkCell = 0;
        }
        Store.flush(level);
    }

    public static void pause(ServerLevel level, String id) {
        dimension(level);
        Job job = require(Store.get(level), id);
        if (!active(job)) return;
        job.resumeStage = job.stage;
        job.stage = "PAUSED";
        Store.flush(level);
    }

    public static void tick(ServerLevel level, int blockBudget) {
        if (!level.dimension().equals(EclipseMod.DIMENSION) || blockBudget <= 0) return;
        dimension(level);
        for (Job job : Store.get(level).jobs.values()) {
            if (!active(job)) continue;
            try {
                validateWorld(level, job.plan);
                validateFrozen(job);
                if ("CHECKING".equals(job.stage)) check(level, job, blockBudget);
                else if ("BUILDING".equals(job.stage)) build(level, job, Math.min(blockBudget, MAX_BATCH));
                else initialize(level, job);
            } catch (RuntimeException exception) {
                job.error = exception.getClass().getSimpleName() + ": " + exception.getMessage();
                job.stage = "FAILED";
                Store.flush(level);
            }
            break;
        }
    }

    private static void check(ServerLevel level, Job job, int budget) {
        while (job.checkPiece < job.plan.pieces.size() && budget > 0) {
            AABB bounds = Planner.bounds(job.plan.pieces.get(job.checkPiece));
            if (!loadVolume(level, bounds)) return;
            if (!level.getEntities((Entity) null, bounds, entity -> !entity.isRemoved()).isEmpty()) {
                throw new IllegalStateException("Entity/player in reserved build volume");
            }
            int width = (int) (bounds.maxX - bounds.minX);
            int depth = (int) (bounds.maxZ - bounds.minZ);
            long volume = (long) width * depth * (int) (bounds.maxY - bounds.minY);
            while (job.checkCell < volume && budget-- > 0) {
                long offset = job.checkCell;
                BlockPos position = new BlockPos((int) bounds.minX + (int) (offset % width),
                    (int) bounds.minY + (int) (offset / ((long) width * depth)),
                    (int) bounds.minZ + (int) ((offset / width) % depth));
                requireAir(level, position);
                job.checkCell++;
            }
            if (job.checkCell == volume) {
                job.checkPiece++;
                job.checkCell = 0;
            }
        }
        if (job.checkPiece == job.plan.pieces.size()) {
            job.stage = "BUILDING";
            job.writeCursor = 0;
        }
        Store.flush(level);
    }

    private static void build(ServerLevel level, Job job, int budget) {
        List<Write> writes = specification(level, job, false).writes();
        if (job.writeCursor < 0 || job.writeCursor > writes.size()) throw new IllegalStateException("Invalid build cursor");
        if (job.pendingStart < 0 && job.writeCursor == writes.size()) {
            job.stage = "INITIALIZING";
            Store.flush(level);
            return;
        }
        boolean replay = job.pendingStart >= 0;
        if (!replay) {
            job.pendingStart = job.writeCursor;
            job.pendingEnd = Math.min(writes.size(), job.writeCursor + budget);
            for (int index = job.pendingStart; index < job.pendingEnd; index++) requireAir(level, writes.get(index).position());
            Store.flush(level);
        }
        if (job.pendingStart != job.writeCursor || job.pendingEnd > writes.size()
            || job.pendingEnd <= job.pendingStart || job.pendingEnd - job.pendingStart > MAX_BATCH) {
            throw new IllegalStateException("Corrupt owned batch journal");
        }
        for (Planner.Piece piece : job.plan.pieces) {
            if (!loadVolume(level, Planner.bounds(piece))) return;
            if (!level.getEntities((Entity) null, Planner.bounds(piece), entity -> !entity.isRemoved()).isEmpty()) {
                throw new IllegalStateException("Entity/player entered reserved build volume");
            }
        }
        for (int index = job.pendingStart; index < job.pendingEnd; index++) {
            Write write = writes.get(index);
            BlockState current = level.getBlockState(write.position());
            if (!current.isAir() && (!replay || !current.equals(write.state()))) {
                throw new IllegalStateException("Non-owned block in batch at " + write.position());
            }
            if (current.isAir() && level.getBlockEntity(write.position()) != null) {
                throw new IllegalStateException("Unexpected block entity at " + write.position());
            }
        }
        if (job.batchCursor < job.pendingStart || job.batchCursor > job.pendingEnd) job.batchCursor = job.pendingStart;
        int end = Math.min(job.pendingEnd, job.batchCursor + budget);
        for (int index = job.batchCursor; index < end; index++) place(level, writes.get(index));
        job.batchCursor = end;
        if (end < job.pendingEnd) return;
        level.getChunkSource().save(true);
        level.getChunkSource().chunkMap.flushWorker();
        job.writeCursor = job.pendingEnd;
        Write last = writes.get(job.writeCursor - 1);
        job.pieceCursor = last.piece();
        job.cellCursor = last.cell() + 1;
        job.pendingStart = -1;
        job.pendingEnd = 0;
        job.batchCursor = -1;
        if (job.writeCursor == writes.size()) job.stage = "INITIALIZING";
        Store.flush(level);
    }

    private static void place(ServerLevel level, Write write) {
        BlockPos position = write.position();
        BlockState current = level.getBlockState(position);
        if (!current.equals(write.state()) && !level.setBlock(position, write.state(), FLAGS)) {
            throw new IllegalStateException("Block placement refused at " + position);
        }
        if (write.nbt() != null) {
            BlockEntity entity = level.getBlockEntity(position);
            if (entity == null) throw new IllegalStateException("Missing template block entity at " + position);
            CompoundTag payload = write.nbt().copy();
            payload.putInt("x", position.getX());
            payload.putInt("y", position.getY());
            payload.putInt("z", position.getZ());
            entity.load(payload);
            if (entity instanceof StructureBlockEntity structure) {
                structure.setRotation(structure.getRotation().getRotated(write.rotation()));
            }
            entity.setChanged();
        }
        if (!level.getBlockState(position).equals(write.state())) throw new IllegalStateException("Placed state differs at " + position);
    }

    private static Specification specification(ServerLevel level, Job job, boolean resolveSpawns) {
        Specification cached = CACHE.get(job);
        if (cached != null) return cached;
        if (job.engineVersion != 1) throw new IllegalStateException("Unsupported engine operation schema");
        validatePlan(job.plan);
        LinkedHashMap<BlockPos, Write> cells = new LinkedHashMap<>();
        int pieceIndex = 0;
        for (Planner.Piece piece : job.plan.pieces) {
            Rotation rotation = Rotation.values()[piece.rotation];
            int cellIndex = 0;
            for (Catalog.Cell cell : Catalog.blocks(piece.key)) {
                BlockPos destination = Planner.transform(Catalog.vec(cell.position()), piece.size, piece.rotation, piece.origin);
                if (!contains(Planner.bounds(piece), destination)) throw new IllegalStateException("Cell outside reserved piece");
                BlockState state = cell.state().rotate(rotation);
                if (!state.isAir()) {
                    Write previous = cells.put(destination, new Write(destination, state,
                        cell.nbt() == null ? null : cell.nbt().copy(), rotation, pieceIndex, cellIndex));
                    if (previous != null) throw new IllegalStateException("Overlapping template blocks");
                }
                cellIndex++;
            }
            pieceIndex++;
        }
        Set<BlockPos> capPositions = new HashSet<>();
        for (int[] cap : job.plan.caps) capPositions.add(Catalog.pos(cap));
        Set<BlockPos> acceptedCaps = new HashSet<>();
        int finishIndex = 0;
        for (Planner.Piece piece : job.plan.pieces) {
            for (Catalog.Port port : piece.ports) {
                for (int[] point : port.cells) {
                    BlockPos feet = Catalog.pos(point);
                    if (!capPositions.contains(feet)) continue;
                    for (int rise = 0; rise < 5; rise++) {
                        BlockPos position = feet.above(rise);
                        if (cells.containsKey(position)) throw new IllegalStateException("Port cap intersects template");
                        BlockState state = (rise == 0 || rise == 4 ? Blocks.DEEPSLATE_BRICKS : Blocks.PURPLE_STAINED_GLASS).defaultBlockState();
                        cells.put(position, new Write(position, state, null, Rotation.NONE, pieceIndex, finishIndex++));
                        acceptedCaps.add(position);
                    }
                }
            }
        }
        if (!acceptedCaps.containsAll(capPositions)) throw new IllegalStateException("Cap outside declared port boundary");
        Planner.Piece boss = job.plan.pieces.get(job.plan.pieces.size() - 1);
        for (int localX = 15; localX <= 18; localX++) {
            for (int localY = 2; localY <= 7; localY++) {
                BlockPos position = Planner.transform(new int[]{localX, localY, 22}, boss.size, boss.rotation, boss.origin);
                cells.put(position, new Write(position, Blocks.PURPLE_STAINED_GLASS.defaultBlockState(), null, Rotation.NONE, pieceIndex, finishIndex++));
            }
        }
        Set<BlockPos> runes = new HashSet<>();
        for (Planner.Piece piece : job.plan.pieces) {
            for (Catalog.Socket socket : piece.sockets) {
                BlockPos position = Catalog.pos(socket.position);
                BlockPos support = Catalog.pos(socket.support);
                if (!position.below().equals(support) || cells.containsKey(position)
                    || !expected(cells, support).isFaceSturdy(level, support, Direction.UP) || !runes.add(position)) {
                    throw new IllegalStateException("Rune is not AIR over full support at " + position);
                }
                cells.put(position, new Write(position, EclipseMod.RUNE.get().defaultBlockState(), null, Rotation.NONE, pieceIndex, finishIndex++));
            }
        }
        for (Planner.Piece piece : job.plan.pieces) {
            Set<BlockPos> spawnPoints = new HashSet<>();
            for (Catalog.Socket socket : piece.sockets) {
                BlockPos selected = resolveSpawns ? findSpawn(level, cells, piece, Catalog.pos(socket.position), spawnPoints)
                    : Catalog.pos(socket.spawn_position);
                if (!safeExpectedSpawn(level, cells, selected) || runes.contains(selected) || !spawnPoints.add(selected)
                    || !contains(Planner.bounds(piece), selected) || !contains(Planner.bounds(piece), selected.above())) {
                    throw new IllegalStateException("No independent safe spawn for rune " + Catalog.pos(socket.position));
                }
                if (resolveSpawns) socket.spawn_position = Catalog.vec(selected);
            }
        }
        for (BlockPos position : cells.keySet()) {
            boolean owned = false;
            for (Planner.Piece piece : job.plan.pieces) if (contains(Planner.bounds(piece), position)) { owned = true; break; }
            if (!owned) throw new IllegalStateException("Finish cell outside reservation");
        }
        Specification result = new Specification(new ArrayList<>(cells.values()), cells);
        CACHE.put(job, result);
        return result;
    }

    private static BlockPos findSpawn(ServerLevel level, Map<BlockPos, Write> cells, Planner.Piece piece, BlockPos rune, Set<BlockPos> used) {
        for (int radius = 1; radius <= 6; radius++) {
            for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                for (int offsetX = -radius; offsetX <= radius; offsetX++) {
                    if (Math.max(Math.abs(offsetX), Math.abs(offsetZ)) != radius) continue;
                    BlockPos candidate = rune.offset(offsetX, 0, offsetZ);
                    if (!used.contains(candidate) && contains(Planner.bounds(piece), candidate)
                        && contains(Planner.bounds(piece), candidate.above()) && safeExpectedSpawn(level, cells, candidate)) return candidate;
                }
            }
        }
        throw new IllegalStateException("No legal independent entity feet near " + rune);
    }

    private static BlockState expected(Map<BlockPos, Write> cells, BlockPos position) {
        Write write = cells.get(position);
        return write == null ? Blocks.AIR.defaultBlockState() : write.state();
    }

    private static boolean safeExpectedSpawn(ServerLevel level, Map<BlockPos, Write> cells, BlockPos position) {
        return expected(cells, position).isAir() && expected(cells, position.above()).isAir()
            && expected(cells, position.below()).isFaceSturdy(level, position.below(), Direction.UP);
    }

    private static boolean safeWorldSpawn(ServerLevel level, BlockPos position) {
        return level.getBlockState(position).isAir() && level.getBlockState(position.above()).isAir()
            && level.getFluidState(position).isEmpty() && level.getFluidState(position.above()).isEmpty()
            && level.getBlockState(position.below()).isFaceSturdy(level, position.below(), Direction.UP)
            && !level.getBlockCollisions(null, new AABB(position.getX() + 0.2, position.getY(), position.getZ() + 0.2,
                position.getX() + 0.8, position.getY() + 1.95, position.getZ() + 0.8)).iterator().hasNext();
    }

    private static void requireAir(ServerLevel level, BlockPos position) {
        if (!level.getBlockState(position).isAir() || level.getBlockEntity(position) != null || !level.getFluidState(position).isEmpty()) {
            throw new IllegalStateException("AIR-only preflight/placement refused at " + position);
        }
    }

    public static void enter(ServerPlayer player, String id) {
        ServerLevel level = player.server.getLevel(EclipseMod.DIMENSION);
        if (level == null) throw new IllegalStateException("Eclipse dimension unavailable");
        Job job = require(Store.get(level), id);
        if (!"COMPLETE".equals(job.stage)) throw new IllegalStateException("Instance is not COMPLETE");
        BlockPos entry = Catalog.pos(job.plan.entry);
        level.getChunk(entry);
        if (!safeWorldSpawn(level, entry)) throw new IllegalStateException("Instance entry obstructed");
        EclipseMod.enter(player, id);
    }

    public static boolean protectedAt(ServerLevel level, BlockPos position) {
        if (!level.dimension().equals(EclipseMod.DIMENSION)) return false;
        for (Job job : Store.get(level).jobs.values()) {
            for (Planner.Piece piece : job.plan.pieces) if (contains(Planner.bounds(piece), position)) return true;
        }
        return false;
    }

    private static boolean contains(AABB bounds, BlockPos position) {
        return bounds.contains(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5);
    }

    private static Job require(Store store, String id) {
        Job job = store.jobs.get(id);
        if (job == null) throw new IllegalArgumentException("Unknown Eclipse instance " + id);
        return job;
    }

    static void validatePlan(Planner.Plan plan) {
        if (plan == null || plan.version != 1 || plan.id == null || plan.id.isBlank()
            || !Catalog.get().fingerprint.equals(plan.fingerprint) || plan.pieces == null || plan.pieces.isEmpty()
            || plan.caps == null || !vector(plan.anchor) || !vector(plan.entry) || !vector(plan.bossOrigin)
            || !vector(plan.region) || !vector(plan.exit)) throw new IllegalStateException("Invalid frozen plan");
        int index = 0;
        for (Planner.Piece piece : plan.pieces) {
            if (piece == null || piece.index != index++ || !vector(piece.origin) || !vector(piece.size)
                || piece.rotation < 0 || piece.rotation > 3 || piece.sockets == null || piece.ports == null
                || piece.size[0] <= 0 || piece.size[1] <= 0 || piece.size[2] <= 0
                || piece.size[0] > 512 || piece.size[1] > 512 || piece.size[2] > 512
                || Math.abs((long) piece.origin[0]) > 29999000 || Math.abs((long) piece.origin[2]) > 29999000
                || Math.abs((long) piece.origin[1]) > 2048) throw new IllegalStateException("Invalid frozen piece");
            Catalog.Template template = Catalog.get().templates.get(piece.key);
            if (template == null || !java.util.Arrays.equals(template.size, piece.size)
                || !java.util.Objects.equals(template.template, piece.template)
                || !java.util.Objects.equals(template.child, piece.child)) throw new IllegalStateException("Frozen piece/catalog mismatch");
            for (Catalog.Socket socket : piece.sockets) {
                if (socket == null || !vector(socket.position) || !vector(socket.support) || !vector(socket.spawn_position)) {
                    throw new IllegalStateException("Invalid frozen socket");
                }
            }
            for (Catalog.Port port : piece.ports) {
                if (port == null || port.cells == null || port.cells.isEmpty()) throw new IllegalStateException("Invalid frozen port");
                for (int[] cell : port.cells) if (!vector(cell)) throw new IllegalStateException("Invalid frozen port position");
            }
        }
        Planner.Piece boss = plan.pieces.get(plan.pieces.size() - 1);
        if (!"boss/eclipse_keeper".equals(boss.template) || !java.util.Arrays.equals(boss.origin, plan.bossOrigin)) {
            throw new IllegalStateException("Missing independent Boss piece");
        }
        for (int[] cap : plan.caps) if (!vector(cap)) throw new IllegalStateException("Invalid cap position");
        for (int first = 0; first < plan.pieces.size(); first++) {
            for (int second = first + 1; second < plan.pieces.size(); second++) {
                if (Planner.bounds(plan.pieces.get(first)).intersects(Planner.bounds(plan.pieces.get(second)))) {
                    throw new IllegalStateException("Frozen plan has overlapping reservations");
                }
            }
        }
    }

    private static boolean vector(int[] value) { return value != null && value.length == 3; }

    private static String planHash(Planner.Plan plan) {
        return Catalog.hash(Catalog.GSON.toJson(plan).getBytes(StandardCharsets.UTF_8));
    }

    static void validateFrozen(Job job) {
        if (job.engineVersion != 1 || job.planHash == null || !job.planHash.equals(planHash(job.plan))) {
            throw new IllegalStateException("Frozen plan integrity mismatch");
        }
    }

    static void validateCursors(Job job) {
        if (job.pieceCursor > job.plan.pieces.size() || job.checkPiece > job.plan.pieces.size()
            || job.pendingEnd - job.pendingStart > MAX_BATCH && job.pendingStart >= 0
            || job.pendingStart < 0 && job.pendingEnd != 0
            || "PAUSED".equals(job.stage) && (job.resumeStage == null
                || !Set.of("CHECKING", "BUILDING", "INITIALIZING").contains(job.resumeStage))) {
            throw new IllegalStateException("Invalid saved job cursor/stage");
        }
        if (job.checkPiece < job.plan.pieces.size()) {
            AABB bounds = Planner.bounds(job.plan.pieces.get(job.checkPiece));
            long volume = (long) (bounds.maxX - bounds.minX) * (long) (bounds.maxY - bounds.minY) * (long) (bounds.maxZ - bounds.minZ);
            if (job.checkCell > volume) throw new IllegalStateException("Invalid AIR preflight cursor");
        } else if (job.checkCell != 0) throw new IllegalStateException("Invalid finished preflight cursor");
        if ("BUILDING".equals(job.stage) || "INITIALIZING".equals(job.stage) || "COMPLETE".equals(job.stage)
            || "PAUSED".equals(job.stage) && !"CHECKING".equals(job.resumeStage)) {
            if (job.checkPiece != job.plan.pieces.size()) throw new IllegalStateException("Build without completed AIR preflight");
        }
    }

    private static boolean loadVolume(ServerLevel level, AABB bounds) {
        boolean ready = true;
        for (int chunkX = ((int) bounds.minX) >> 4; chunkX <= (((int) bounds.maxX - 1) >> 4); chunkX++) {
            for (int chunkZ = ((int) bounds.minZ) >> 4; chunkZ <= (((int) bounds.maxZ - 1) >> 4); chunkZ++) {
                level.getChunk(chunkX, chunkZ);
                if (!level.areEntitiesLoaded(ChunkPos.asLong(chunkX, chunkZ))) ready = false;
            }
        }
        return ready;
    }

    private static void validateWorld(ServerLevel level, Planner.Plan plan) {
        for (Planner.Piece piece : plan.pieces) {
            AABB bounds = Planner.bounds(piece);
            BlockPos minimum = new BlockPos((int) bounds.minX, (int) bounds.minY, (int) bounds.minZ);
            BlockPos maximum = new BlockPos((int) bounds.maxX - 1, (int) bounds.maxY - 1, (int) bounds.maxZ - 1);
            if (minimum.getY() < level.getMinBuildHeight() || maximum.getY() >= level.getMaxBuildHeight()
                || !level.getWorldBorder().isWithinBounds(minimum) || !level.getWorldBorder().isWithinBounds(maximum)) {
                throw new IllegalStateException("Plan outside build height/world border");
            }
        }
    }

    private static void assertSeparate(Planner.Plan first, Planner.Plan second) {
        for (Planner.Piece left : first.pieces) {
            for (Planner.Piece right : second.pieces) {
                if (Planner.bounds(left).intersects(Planner.bounds(right))) throw new IllegalStateException("Reserved instance overlap");
            }
        }
    }

    static void validateReservations(Store store) {
        List<Job> jobs = new ArrayList<>(store.jobs.values());
        for (int first = 0; first < jobs.size(); first++) {
            for (int second = first + 1; second < jobs.size(); second++) assertSeparate(jobs.get(first).plan, jobs.get(second).plan);
        }
    }

    private static void initialize(ServerLevel level, Job job) {
        for (Write write : specification(level, job, false).writes()) {
            if (!level.getBlockState(write.position()).equals(write.state())) {
                throw new IllegalStateException("Geometry changed before initialization at " + write.position());
            }
        }
        for (Planner.Piece piece : job.plan.pieces) {
            for (Catalog.Socket socket : piece.sockets) {
                BlockPos rune = Catalog.pos(socket.position);
                if (!level.getBlockState(rune).is(EclipseMod.RUNE.get())
                    || !level.getBlockState(rune.below()).isFaceSturdy(level, rune.below(), Direction.UP)
                    || rune.equals(Catalog.pos(socket.spawn_position)) || !safeWorldSpawn(level, Catalog.pos(socket.spawn_position))) {
                    throw new IllegalStateException("Unsafe completed socket at " + rune);
                }
            }
        }
        if (!safeWorldSpawn(level, Catalog.pos(job.plan.entry))) throw new IllegalStateException("Unsafe instance entry");
        Gameplay.initialize(level, job);
        if (job.gameplay == null) throw new IllegalStateException("Gameplay initialization did not create state");
        level.getChunkSource().save(true);
        level.getChunkSource().chunkMap.flushWorker();
        job.stage = "COMPLETE";
        job.pieceCursor = job.plan.pieces.size();
        job.cellCursor = 0;
        Store.flush(level);
        CACHE.remove(job);
    }
}
