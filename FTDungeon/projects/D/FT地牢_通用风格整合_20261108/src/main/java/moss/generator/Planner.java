package moss.generator;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.phys.AABB;

public final class Planner {
    public static final class Plan {
        public String style = Profiles.DEFAULT;
        public String id, fingerprint;
        public int version = 1;
        public long seed, layoutSeed;
        public int[] anchor, region, entry, exit, bossOrigin;
        public int edges;
        public int bossPortalPiece = -1, bossPortalFacing, portalDistance;
        public int[] bossPortal;
        public List<int[]> graphEdges = new ArrayList<>();
        public Map<Integer, Integer> roomDistances = new java.util.TreeMap<>();
        public List<Piece> pieces = new ArrayList<>();
        public List<int[]> caps = new ArrayList<>();
    }
    public static final class Piece {
        public String style = Profiles.DEFAULT;
        public int index, rotation, pieceSeed;
        public long forkSeed;
        public int[] portal, portalTrigger;
        public int portalFacing;
        public String key, template, child;
        public int[] origin, size, grid;
        public List<Catalog.Socket> sockets = new ArrayList<>();
        public List<Catalog.Port> ports = new ArrayList<>();
    }

    public static Plan create(long seed, BlockPos spawnOrigin) {
        return create(Profiles.DEFAULT, seed, spawnOrigin);
    }

    public static Plan create(String style, long seed, BlockPos spawnOrigin) {
        Catalog catalog = Catalog.get(style);
        Catalog.require(Math.abs((long)spawnOrigin.getX()) < 29000000 && Math.abs((long)spawnOrigin.getZ()) < 29000000, "World-coordinate bounds");
        Plan plan = new Plan();
        plan.style = style;
        plan.seed = seed;
        plan.anchor = Catalog.vec(spawnOrigin);
        BlockPos region = new BlockPos(Math.round((float)spawnOrigin.getX() / 1024) * 1024, 0, Math.round((float)spawnOrigin.getZ() / 1024) * 1024);
        plan.region = Catalog.vec(region);
        plan.layoutSeed = region.hashCode() ^ seed;
        plan.fingerprint = catalog.fingerprint;
        plan.id = Catalog.hash(("forest-v1:" + seed + ":" + spawnOrigin.asLong() + ":" + plan.fingerprint).getBytes(StandardCharsets.UTF_8)).substring(0, 16);
        PrisonGridGenerator generator = new PrisonGridGenerator(0, 0);
        generator.addRooms(RandomSource.create(plan.layoutSeed));
        for (GridPiecesGenerator.RoomData record : generator.rooms) {
            BlockPos origin = spawnOrigin.offset((record.pos.getX() - 32) * 48 + record.offset.getX(), (record.pos.getY() - 15) * 32 + record.offset.getY() + 2, (record.pos.getZ() - 32) * 48 + record.offset.getZ());
            int rotation = record.rotation.ordinal();
            BlockPos starting = origin.offset(rotation == 1 || rotation == 2 ? 47 : 0, 0, rotation == 2 || rotation == 3 ? 47 : 0);
            int pieceSeed = (int)seed + origin.hashCode();
            WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0));
            random.setLargeFeatureSeed(pieceSeed, starting.getX() >> 4, starting.getZ() >> 4);
            String pool = record.poolId.getPath().substring("better_prison/".length());
            List<String> expanded = expanded(catalog.pools.get(pool));
            String template = expanded.get(random.nextInt(expanded.size()));
            long forkSeed = random.nextLong();
            String child = null;
            String key = template;
            if (template.equals("straight/room")) {
                RandomSource childRandom = RandomSource.create(forkSeed);
                List<String> candidates = Util.toShuffledList(expanded(catalog.pools.get("straight/room/layout")).stream(), childRandom);
                Rotation.getShuffled(childRandom);
                child = candidates.get(0);
                key += "_" + child.substring(child.lastIndexOf('/') + 1);
            }
            if (template.startsWith("spawn/outside_")) origin = origin.above(2);
            Piece piece = piece(style, plan.pieces.size(), key, template, child, origin, rotation);
            piece.grid = new int[] {record.pos.getX(), record.pos.getY(), record.pos.getZ()};
            piece.pieceSeed = pieceSeed;
            piece.forkSeed = forkSeed;
            plan.pieces.add(piece);
            if (template.equals("promenade")) plan.exit = Catalog.vec(transform(new int[] {7, 2, 11}, piece.size, rotation, piece.origin));
        }
        plan.entry = Catalog.vec(spawnOrigin.offset(23, 4, 24));
        int east = plan.pieces.stream().mapToInt(piece -> (int)bounds(piece).maxX).max().orElseThrow();
        plan.bossOrigin = new int[] {east + 64, spawnOrigin.getY() - 96, spawnOrigin.getZ()};
        plan.pieces.add(piece(style, plan.pieces.size(), "boss/eclipse_keeper", "boss/eclipse_keeper", null, Catalog.pos(plan.bossOrigin), 0));
        Catalog.require(plan.exit != null, "Missing promenade");
        validate(plan);
        choosePortal(plan);
        return plan;
    }

    private static Piece piece(String style, int index, String key, String template, String child, BlockPos origin, int rotation) {
        Catalog.Template definition = Catalog.get(style).templates.get(key);
        Catalog.require(definition != null, "Unknown runtime variant: " + key);
        Piece piece = new Piece();
        piece.style = style;
        piece.index = index; piece.key = key; piece.template = template; piece.child = child;
        piece.origin = Catalog.vec(origin); piece.size = definition.size.clone(); piece.rotation = rotation;
        if (definition.portal != null) {
            piece.portal = Catalog.vec(transform(definition.portal, piece.size, rotation, piece.origin));
            piece.portalTrigger = Catalog.vec(transform(definition.portal_trigger, piece.size, rotation, piece.origin));
            Direction facing = Direction.from2DDataValue(definition.portal_facing);
            for (int turn = 0; turn < rotation; turn++) facing = facing.getClockWise();
            piece.portalFacing = facing.get2DDataValue();
        }
        for (Catalog.Socket local : definition.sockets) {
            Catalog.Socket socket = new Catalog.Socket();
            socket.position = Catalog.vec(transform(local.position, piece.size, rotation, piece.origin));
            socket.support = Catalog.vec(transform(local.support, piece.size, rotation, piece.origin));
            socket.spawn_position = Catalog.vec(transform(local.spawn_position, piece.size, rotation, piece.origin));
            socket.kind = local.kind;
            piece.sockets.add(socket);
        }
        for (Catalog.Port local : definition.ports) {
            Catalog.Port port = new Catalog.Port();
            for (int[] cell : local.cells) port.cells.add(Catalog.vec(transform(cell, piece.size, rotation, piece.origin)));
            piece.ports.add(port);
        }
        return piece;
    }

    private static List<String> expanded(List<Catalog.Weighted> pool) {
        Catalog.require(pool != null && !pool.isEmpty(), "Missing reference pool");
        List<String> result = new ArrayList<>();
        for (Catalog.Weighted item : pool) for (int count = 0; count < item.weight; count++) result.add(item.template);
        return result;
    }

    public static BlockPos transform(int[] local, int[] size, int rotation, int[] origin) {
        int east = local[0], south = local[2], width = size[0], depth = size[2];
        for (int turn = 0; turn < rotation; turn++) {
            int next = depth - 1 - south;
            south = east; east = next;
            int oldWidth = width; width = depth; depth = oldWidth;
        }
        return new BlockPos(origin[0] + east, origin[1] + local[1], origin[2] + south);
    }

    public static AABB bounds(Piece piece) {
        int width = piece.rotation % 2 == 0 ? piece.size[0] : piece.size[2];
        int depth = piece.rotation % 2 == 0 ? piece.size[2] : piece.size[0];
        int minimum = Catalog.get(piece.style).templates.get(piece.key).minimum_y;
        return new AABB(piece.origin[0], piece.origin[1] + minimum, piece.origin[2], piece.origin[0] + width, piece.origin[1] + piece.size[1], piece.origin[2] + depth);
    }

    public static void validate(Plan plan) {
        Set<String> edges = new HashSet<>();
        Map<BlockPos, Piece> owners = new HashMap<>();
        Map<BlockPos, Catalog.Port> ports = new HashMap<>();
        for (Piece piece : plan.pieces) {
            for (Piece other : plan.pieces) if (piece.index < other.index) Catalog.require(!bounds(piece).intersects(bounds(other)), "Room envelopes overlap: " + piece.index + "/" + other.index);
            for (Catalog.Port port : piece.ports) for (int[] cell : port.cells) {
                BlockPos point = Catalog.pos(cell);
                Catalog.require(owners.put(point, piece) == null, "Duplicate port"); ports.put(point, port);
            }
        }
        Set<Catalog.Port> connected = new HashSet<>();
        Map<Integer, Set<Integer>> graph = new HashMap<>();
        for (Map.Entry<BlockPos, Piece> entry : owners.entrySet()) {
            for (BlockPos neighbor : List.of(entry.getKey().north(), entry.getKey().south(), entry.getKey().east(), entry.getKey().west())) {
                Piece other = owners.get(neighbor);
                if (other != null && other.index != entry.getValue().index) {
                    int first = Math.min(other.index, entry.getValue().index), second = Math.max(other.index, entry.getValue().index);
                    edges.add(first + ":" + second);
                    graph.computeIfAbsent(first, unused -> new HashSet<>()).add(second);
                    graph.computeIfAbsent(second, unused -> new HashSet<>()).add(first);
                    connected.add(ports.get(entry.getKey())); connected.add(ports.get(neighbor));
                }
            }
        }
        Set<Integer> reached = new HashSet<>();
        ArrayDeque<Integer> queue = new ArrayDeque<>(); queue.add(0); reached.add(0);
        while (!queue.isEmpty()) for (int neighbor : graph.getOrDefault(queue.remove(), Set.of())) if (reached.add(neighbor)) queue.add(neighbor);
        long playable = plan.pieces.stream().filter(piece -> !piece.ports.isEmpty()).count();
        Catalog.require(reached.size() == playable && edges.size() == playable - 1, "Reference port graph disconnected/cyclic");
        plan.edges = edges.size(); plan.caps.clear();
        plan.graphEdges.clear();
        edges.stream().sorted().forEach(edge -> {
            String[] pair = edge.split(":");
            plan.graphEdges.add(new int[]{Integer.parseInt(pair[0]), Integer.parseInt(pair[1])});
        });
        for (Piece piece : plan.pieces) for (Catalog.Port port : piece.ports) if (!connected.contains(port)) plan.caps.addAll(port.cells);
    }

    private record WalkSurface(List<BlockPos> feet, List<BlockPos> obstacles) {}
    private static final Map<Catalog, Map<String, WalkSurface>> SURFACES = new java.util.WeakHashMap<>();

    private static final class TemplateView implements BlockGetter {
        final Map<BlockPos, BlockState> states = new HashMap<>();
        final int height;
        TemplateView(String style, Catalog.Template template) {
            height = template.size[1];
            for (Catalog.Cell cell : Catalog.blocks(style, template.key)) states.put(cell.position(), cell.state());
        }
        @Override public BlockEntity getBlockEntity(BlockPos position) { return null; }
        @Override public BlockState getBlockState(BlockPos position) { return states.getOrDefault(position, Blocks.AIR.defaultBlockState()); }
        @Override public FluidState getFluidState(BlockPos position) { return getBlockState(position).getFluidState(); }
        @Override public int getHeight() { return height; }
        @Override public int getMinBuildHeight() { return 0; }
        boolean clear(BlockPos position) {
            return getFluidState(position).isEmpty() && getBlockState(position).getCollisionShape(this, position).isEmpty();
        }
    }

    private static WalkSurface walkable(String style, String key) {
        Catalog catalog = Catalog.get(style);
        return SURFACES.computeIfAbsent(catalog, unused -> new HashMap<>()).computeIfAbsent(key, unused -> {
            Catalog.Template template = catalog.templates.get(key);
            TemplateView view = new TemplateView(style, template);
            if (template.portal != null) {
                BlockPos anchor = Catalog.pos(template.portal);
                Direction facing = Direction.from2DDataValue(template.portal_facing);
                for (int across = -1; across <= 1; across++) for (int depth = -2; depth <= 2; depth++) {
                    BlockPos base = anchor.relative(facing.getClockWise(), across).relative(facing, depth);
                    Catalog.require(base.getX() >= 0 && base.getX() < template.size[0] && base.getZ() >= 0 && base.getZ() < template.size[2]
                        && base.getY() > template.minimum_y && base.getY()+3 < template.size[1], "Portal niche outside reservation");
                    Catalog.require(view.getFluidState(base.below()).isEmpty()
                        && view.getBlockState(base.below()).isCollisionShapeFullBlock(view, base.below()), "Unsupported portal niche");
                    for (int rise = 0; rise < 4; rise++) Catalog.require(view.getBlockState(base.above(rise)).isAir(), "Portal niche requires AIR");
                }
            }
            List<BlockPos> result = new ArrayList<>();
            List<BlockPos> obstacles = new ArrayList<>();
            for (Catalog.Cell cell : Catalog.blocks(style, key)) {
                BlockPos position = cell.position();
                if (!view.clear(position)) obstacles.add(position);
                if (position.getY() > template.minimum_y && position.getY()+1 < template.size[1]
                    && view.clear(position) && view.clear(position.above()) && view.getFluidState(position.below()).isEmpty()
                    && view.getBlockState(position.below()).isCollisionShapeFullBlock(view, position.below())) result.add(position);
            }
            return new WalkSurface(List.copyOf(result), List.copyOf(obstacles));
        });
    }

    private static void choosePortal(Plan plan) {
        Set<BlockPos> feet = new HashSet<>();
        Map<BlockPos, Integer> owners = new HashMap<>();
        Set<BlockPos> overhead = new HashSet<>();
        for (Piece piece : plan.pieces) {
            if (piece.ports.isEmpty()) continue;
            WalkSurface surface = walkable(piece.style, piece.key);
            for (BlockPos local : surface.feet()) {
                BlockPos world = transform(Catalog.vec(local), piece.size, piece.rotation, piece.origin);
                feet.add(world);
                owners.put(world, piece.index);
            }
            for (BlockPos local : surface.obstacles()) {
                overhead.add(transform(Catalog.vec(local), piece.size, piece.rotation, piece.origin));
            }
        }
        Set<BlockPos> caps = new HashSet<>();
        for (int[] cap : plan.caps) {
            BlockPos bottom = Catalog.pos(cap);
            for (int rise = 0; rise < 5; rise++) {
                overhead.add(bottom.above(rise));
                caps.add(bottom.above(rise));
                caps.add(bottom.above(rise).below());
            }
        }
        feet.removeAll(caps);
        BlockPos entry = Catalog.pos(plan.entry);
        Catalog.require(feet.contains(entry), "Forest entry is not walkable");
        Map<BlockPos, Integer> distances = new HashMap<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        distances.put(entry, 0); queue.add(entry);
        Set<Integer> reachedPieces = new HashSet<>();
        while (!queue.isEmpty()) {
            BlockPos position = queue.remove();
            reachedPieces.add(owners.get(position));
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = position.relative(direction);
                for (int rise : new int[]{0, 1, -1}) {
                    BlockPos destination = next.offset(0, rise, 0);
                    if (!feet.contains(destination) || distances.containsKey(destination)) continue;
                    if (rise == 1 && overhead.contains(position.above(2))) continue;
                    if (rise == -1 && overhead.contains(destination.above(2))) continue;
                    distances.put(destination, distances.get(position) + 1);
                    queue.add(destination);
                }
            }
        }
        for (Piece piece : plan.pieces) if (!piece.ports.isEmpty()) {
            Catalog.require(reachedPieces.contains(piece.index), "Forest walking graph disconnected at " + piece.index);
        }
        int lowest = plan.pieces.stream().filter(piece -> piece.portal != null)
            .mapToInt(piece -> piece.portalTrigger[1]).min().orElseThrow();
        Piece selected = null;
        int farthest = -1;
        plan.roomDistances.clear();
        for (Piece piece : plan.pieces) {
            if (piece.portal == null) continue;
            Integer distance = distances.get(Catalog.pos(piece.portalTrigger));
            Catalog.require(distance != null, "Forest candidate portal unreachable: " + piece.index);
            plan.roomDistances.put(piece.index, distance);
            if (piece.portalTrigger[1] == lowest && (distance > farthest
                || distance == farthest && (selected == null || piece.index < selected.index))) {
                selected = piece; farthest = distance;
            }
        }
        Catalog.require(selected != null, "No lowest-floor portal room");
        plan.bossPortalPiece = selected.index;
        plan.bossPortal = selected.portal.clone();
        plan.bossPortalFacing = selected.portalFacing;
        plan.exit = selected.portalTrigger.clone();
        plan.portalDistance = farthest;
    }

}
