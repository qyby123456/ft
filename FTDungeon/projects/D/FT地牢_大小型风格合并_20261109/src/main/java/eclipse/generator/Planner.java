package eclipse.generator;

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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.phys.AABB;

public final class Planner {
    public static final class Plan {
        public String style, styleFingerprint, roomSetFingerprint;
        public String id, fingerprint;
        public int version = 1;
        public long seed, layoutSeed;
        public int[] anchor, region, entry, exit, bossOrigin;
        public int edges;
        public List<Piece> pieces = new ArrayList<>();
        public List<int[]> caps = new ArrayList<>();
    }
    public static final class Piece {
        public int index, rotation, pieceSeed;
        public long forkSeed;
        public String key, template, child;
        public int[] origin, size, grid;
        public List<Catalog.Socket> sockets = new ArrayList<>();
        public List<Catalog.Port> ports = new ArrayList<>();
    }

    public static Plan create(long seed, BlockPos spawnOrigin) {
        return create(Profiles.DEFAULT, seed, spawnOrigin);
    }

    public static Plan create(String styleId, long seed, BlockPos spawnOrigin) {
        Profiles.Style style = Profiles.get(styleId);
        Catalog catalog = Catalog.get();
        Catalog.require(Math.abs((long)spawnOrigin.getX()) < 29000000 && Math.abs((long)spawnOrigin.getZ()) < 29000000, "World-coordinate bounds");
        Plan plan = new Plan();
        plan.style = style.id;
        plan.styleFingerprint = style.fingerprint;
        plan.roomSetFingerprint = RoomSets.fingerprint(style.id);
        plan.seed = seed;
        plan.anchor = Catalog.vec(spawnOrigin);
        BlockPos region = new BlockPos(Math.round((float)spawnOrigin.getX() / 1024) * 1024, 0, Math.round((float)spawnOrigin.getZ() / 1024) * 1024);
        plan.region = Catalog.vec(region);
        plan.layoutSeed = region.hashCode() ^ seed;
        plan.fingerprint = catalog.fingerprint;
        String identity = plan.roomSetFingerprint == null ? "style-v1:" + style.id + ":" + style.fingerprint
            : "rooms-v1:" + style.id + ":" + style.fingerprint + ":" + plan.roomSetFingerprint;
        plan.id = Catalog.hash((identity + ":" + seed + ":" + spawnOrigin.asLong() + ":" + plan.fingerprint).getBytes(StandardCharsets.UTF_8)).substring(0, 16);
        PrisonGridGenerator generator = new PrisonGridGenerator(0, 0);
        generator.addRooms(RandomSource.create(plan.layoutSeed));
        Map<String, Integer> layoutUses = new HashMap<>();
        for (GridPiecesGenerator.RoomData record : generator.rooms) {
            BlockPos origin = spawnOrigin.offset((record.pos.getX() - 32) * 16 + record.offset.getX(), (record.pos.getY() - 15) * 16 + record.offset.getY() + 2, (record.pos.getZ() - 32) * 16 + record.offset.getZ());
            int rotation = record.rotation.ordinal();
            BlockPos starting = origin.offset(rotation == 1 || rotation == 2 ? 15 : 0, 0, rotation == 2 || rotation == 3 ? 15 : 0);
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
                if (RoomSets.balancedLayouts(style.id)) {
                    int minimumUses = candidates.stream().mapToInt(candidate -> layoutUses.getOrDefault(candidate, 0)).min().orElseThrow();
                    child = candidates.stream().filter(candidate -> layoutUses.getOrDefault(candidate, 0) == minimumUses).findFirst().orElseThrow();
                    layoutUses.merge(child, 1, Integer::sum);
                }
                key += "_" + child.substring(child.lastIndexOf('/') + 1);
            }
            if (template.startsWith("spawn/outside_")) origin = origin.above(2);
            Piece piece = piece(plan.pieces.size(), key, template, child, origin, rotation);
            piece.grid = new int[] {record.pos.getX(), record.pos.getY(), record.pos.getZ()};
            piece.pieceSeed = pieceSeed;
            piece.forkSeed = forkSeed;
            plan.pieces.add(piece);
            if (template.equals("promenade")) plan.exit = Catalog.vec(transform(new int[] {7, 2, 11}, piece.size, rotation, piece.origin));
        }
        plan.entry = Catalog.vec(spawnOrigin.offset(7, 4, 8));
        int east = plan.pieces.stream().mapToInt(piece -> (int)bounds(piece).maxX).max().orElseThrow();
        plan.bossOrigin = new int[] {east + 64, spawnOrigin.getY() - 48, spawnOrigin.getZ()};
        plan.pieces.add(piece(plan.pieces.size(), "boss/eclipse_keeper", "boss/eclipse_keeper", null, Catalog.pos(plan.bossOrigin), 0));
        Catalog.require(plan.exit != null, "Missing promenade");
        validate(plan);
        for (Piece selected : plan.pieces) Profiles.blocks(plan, selected.key);
        return plan;
    }

    private static Piece piece(int index, String key, String template, String child, BlockPos origin, int rotation) {
        Catalog.Template definition = Catalog.get().templates.get(key);
        Catalog.require(definition != null, "Unknown runtime variant: " + key);
        Piece piece = new Piece();
        piece.index = index; piece.key = key; piece.template = template; piece.child = child;
        piece.origin = Catalog.vec(origin); piece.size = definition.size.clone(); piece.rotation = rotation;
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
        int minimum = Catalog.get().templates.get(piece.key).minimum_y;
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
        for (Piece piece : plan.pieces) for (Catalog.Port port : piece.ports) if (!connected.contains(port)) plan.caps.addAll(port.cells);
    }
}
