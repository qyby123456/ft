package cn.ft.biomedungeon.core.next;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import static cn.ft.biomedungeon.core.DungeonApi.*;
import static cn.ft.biomedungeon.core.DungeonGeometry.*;

public final class DungeonYPlanner {
    public static final String ALGORITHM = "ft-core-y-splitmix64-dfs-v2";
    public static final int MIN_GAP = 3;
    public static final int MAX_GAP = 10;

    public enum FailureReason { EMPTY_POOL, DUPLICATE_TEMPLATE, INVALID_TEMPLATE, EXHAUSTED, COORDINATE_OVERFLOW, INVALID_POLICY }
    public static final class PlanningException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
        private final FailureReason reason;
        private final int attempts;
        public PlanningException(FailureReason reason, int attempts, String message) {
            super(reason + ": " + message + " (attempts=" + attempts + ")");
            this.reason = reason;
            this.attempts = attempts;
        }
        public FailureReason reason() { return reason; }
        public int attempts() { return attempts; }
    }

    public record Limits(int minY, int maxYExclusive, int maxGraphHeight,
                         boolean requireEveryTemplateOnce, int rootQuarterTurns) {
        public Limits {
            if (minY >= maxYExclusive || maxGraphHeight < 1
                    || rootQuarterTurns < -1 || rootQuarterTurns > 3) {
                throw new IllegalArgumentException("Limits require minY < maxYExclusive, positive graph height, rootQuarterTurns -1..3");
            }
        }
        public static Limits defaults() { return new Limits(-64, 320, 128, false, -1); }
    }

    public DungeonYPlanner() {}

    public static Plan plan(Request request, List<Template> templates, List<Box> occupied) {
        return plan(request, templates, occupied, Limits.defaults());
    }

    public static Plan plan(Request request, List<Template> templates, List<Box> occupied, Limits limits) {
        Objects.requireNonNull(limits, "limits");
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(templates, "templates");
        List<Box> obstacles = List.copyOf(occupied);
        var byId = new TreeMap<String, Template>();
        for (Template template : List.copyOf(templates)) {
            if (byId.putIfAbsent(template.id(), template) != null) {
                throw new PlanningException(FailureReason.DUPLICATE_TEMPLATE, 0, template.id());
            }
            try {
                for (Port port : template.ports()) {
                    validatePort(template, port);
                }
            } catch (IllegalArgumentException exception) {
                throw new PlanningException(FailureReason.INVALID_TEMPLATE, 0, exception.getMessage());
            }
        }
        List<Template> pool = byId.values().stream().filter(template -> template.biome().equals(request.biome())).toList();
        if (pool.isEmpty()) {
            throw new PlanningException(FailureReason.EMPTY_POOL, 0, "No templates for biome " + request.biome());
        }
        if (limits.requireEveryTemplateOnce() && request.roomCount() != pool.size()) {
            throw new PlanningException(FailureReason.INVALID_POLICY, 0, "Exact coverage requires roomCount == matching biome pool size");
        }
        if (limits.requireEveryTemplateOnce() && pool.stream().filter(DungeonYPlanner::entrance).count() > 1) {
            throw new PlanningException(FailureReason.INVALID_POLICY, 0, "Exact coverage supports at most one entrance");
        }
        long seed = deriveSeed(request);
        Search search = new Search(request, pool, obstacles, seed, limits);
        try {
            if (!search.run()) {
                throw new PlanningException(FailureReason.EXHAUSTED, search.attempts,
                        "No complete collision-free graph; last rejection=" + search.lastRejection);
            }
            return new Plan(instanceId(request, pool, obstacles, limits), seed, request.generatorVersion(),
                    search.nodes.stream().map(Node::room).toList(), search.links);
        } catch (ArithmeticException exception) {
            throw new PlanningException(FailureReason.COORDINATE_OVERFLOW, search.attempts, exception.getMessage());
        }
    }

    public static long deriveSeed(Request request) {
        return ByteBuffer.wrap(digest(seedMaterial(request))).getLong();
    }
    private static byte[] seedMaterial(Request request) {
        return encode(output -> {
            string(output, ALGORITHM);
            output.writeLong(request.worldSeed());
            string(output, request.dimension());
            vector(output, request.anchor());
            output.writeLong(request.instanceOrdinal());
            string(output, request.generatorVersion());
            string(output, request.biome());
        });
    }

    private record Node(PlacedRoom room, Template template) {}
    private record PortKey(String nodeId, String portId) {}
    private record Frontier(Node node, Port port) {}

    private static final class Search {
        private final Request request;
        private final List<Template> pool;
        private final List<Box> occupied;
        private final Random64 random;
        private final Limits limits;
        private final List<Node> nodes = new ArrayList<>();
        private final List<Link> links = new ArrayList<>();
        private final List<Box> corridors = new ArrayList<>();
        private final Set<PortKey> used = new HashSet<>();
        private int attempts;
        private String lastRejection = "no candidates";

        private Search(Request request, List<Template> pool, List<Box> occupied, long seed, Limits limits) {
            this.request = request;
            this.limits = limits;
            this.pool = pool;
            this.occupied = occupied;
            random = new Random64(seed);
        }
        private boolean run() {
            List<Template> roots = pool.stream().filter(DungeonYPlanner::entrance).toList();
            if (roots.isEmpty()) {
                roots = pool.stream().filter(template -> !terminal(template) || request.roomCount() == 1).toList();
            }
            for (Template template : shuffled(roots, random)) {
                for (int rotation : shuffled(limits.rootQuarterTurns() < 0 ? List.of(0, 1, 2, 3) : List.of(limits.rootQuarterTurns()), random)) {
                    if (!takeAttempt()) { return false; }
                    Box bounds = bounds(template.size(), request.anchor(), rotation);
                    if (!heightAllowed(bounds)) { continue; }
                    if (collides(bounds, occupied)) {
                        lastRejection = "root occupied";
                        continue;
                    }
                    nodes.add(new Node(new PlacedRoom("n0", template.id(), request.anchor(), rotation, bounds), template));
                    if (extend()) { return true; }
                    nodes.remove(nodes.size() - 1);
                }
            }
            return false;
        }
        private boolean takeAttempt() {
            if (attempts >= request.maxAttempts()) { return false; }
            attempts++;
            return true;
        }
        private boolean extend() {
            if (nodes.size() == request.roomCount()) { return true; }
            List<Frontier> frontier = new ArrayList<>();
            for (Node node : nodes) {
                if (terminal(node.template())) { continue; }
                for (Port port : sortedPorts(node.template())) {
                    if (!used.contains(new PortKey(node.room().nodeId(), port.id()))) {
                        frontier.add(new Frontier(node, port));
                    }
                }
            }
            boolean finalNode = nodes.size() == request.roomCount() - 1;
            boolean hasTerminal = pool.stream().anyMatch(DungeonYPlanner::terminal);
            List<Template> candidates = pool.stream().filter(template -> !entrance(template))
                    .filter(template -> !limits.requireEveryTemplateOnce() || nodes.stream().noneMatch(node -> node.template().id().equals(template.id())))
                    .filter(template -> finalNode && hasTerminal ? terminal(template) : !terminal(template)).toList();
            for (Frontier edge : shuffled(frontier, random)) {
                PlacedRoom parent = edge.node().room();
                WorldPort source = worldPort(edge.node().template(), edge.port(), parent.origin(), parent.quarterTurns());
                for (Template template : shuffled(candidates, random)) {
                    for (Port port : shuffled(sortedPorts(template), random)) {
                        for (int rotation : shuffled(List.of(0, 1, 2, 3), random)) {
                            if (!takeAttempt()) { return false; }
                            WorldPort target = worldPort(template, port, new Vec3i(0, 0, 0), rotation);
                            if (!compatible(source, target)) { continue; }
                            for (int gap : shuffled(List.of(3, 4, 5, 6, 7, 8, 9, 10), random)) {
                                if (!takeAttempt()) { return false; }
                                Vec3i origin = alignedOrigin(source, target, gap);
                                Box bounds = bounds(template.size(), origin, rotation);
                                if (!heightAllowed(bounds)) { continue; }
                                if (collides(bounds, occupied) || collides(bounds, corridors)
                                        || nodes.stream().anyMatch(node -> intersects(node.room().bounds(), bounds))) {
                                    lastRejection = "room collision";
                                    continue;
                                }
                                WorldPort aligned = worldPort(template, port, origin, rotation);
                                String nodeId = "n" + nodes.size();
                                Link link = new Link(parent.nodeId(), edge.port().id(), nodeId, port.id(),
                                        List.of(exteriorFeet(source), exteriorFeet(aligned)), source.width(), source.height());
                                Box corridor = corridorBounds(link).get(0);
                                if (!heightAllowed(bounds, corridor)) { continue; }
                                if (intersects(corridor, bounds) || collides(corridor, occupied) || collides(corridor, corridors)
                                        || nodes.stream().anyMatch(node -> intersects(node.room().bounds(), corridor))) {
                                    lastRejection = "corridor shell collision";
                                    continue;
                                }
                                PortKey sourceKey = new PortKey(parent.nodeId(), edge.port().id());
                                PortKey targetKey = new PortKey(nodeId, port.id());
                                nodes.add(new Node(new PlacedRoom(nodeId, template.id(), origin, rotation, bounds), template));
                                links.add(link);
                                corridors.add(corridor);
                                used.add(sourceKey);
                                used.add(targetKey);
                                if (extend()) { return true; }
                                used.remove(sourceKey);
                                used.remove(targetKey);
                                corridors.remove(corridors.size() - 1);
                                links.remove(links.size() - 1);
                                nodes.remove(nodes.size() - 1);
                            }
                        }
                    }
                }
            }
            if (frontier.isEmpty()) { lastRejection = "no unused outward ports"; }
            if (candidates.isEmpty()) { lastRejection = "no eligible normal/terminal templates"; }
            return false;
        }
        private boolean heightAllowed(Box... candidates) {
            long min = Long.MAX_VALUE;
            long max = Long.MIN_VALUE;
            List<Box> all = new ArrayList<>(corridors);
            nodes.forEach(node -> all.add(node.room().bounds()));
            all.addAll(List.of(candidates));
            for (Box box : all) {
                if (box.min().y() < limits.minY() || box.maxExclusive().y() > limits.maxYExclusive()) {
                    lastRejection = "world height limits exceeded";
                    return false;
                }
                min = Math.min(min, box.min().y());
                max = Math.max(max, box.maxExclusive().y());
            }
            if (max - min > limits.maxGraphHeight()) {
                lastRejection = "whole graph height span exceeded";
                return false;
            }
            return true;
        }
        private boolean compatible(WorldPort source, WorldPort target) {
            if (!source.medium().equals(target.medium())) { lastRejection = "dry/wet mismatch"; return false; }
            if (source.width() != target.width() || source.height() != target.height()) {
                lastRejection = "aperture dimensions mismatch"; return false;
            }
            if (!rotateFacing(source.facing(), 2).equals(target.facing())) {
                lastRejection = "ports do not face each other"; return false;
            }
            return true;
        }
    }

    private static Vec3i alignedOrigin(WorldPort source, WorldPort target, int gap) {
        Vec3i direction = direction(source.facing());
        int desiredSurface = Math.addExact(source.surface(), Math.multiplyExact(gap, direction.x() + direction.z()));
        int axial = Math.subtractExact(desiredSurface, target.surface());
        int transverse = Math.subtractExact(source.transverseMin(), target.transverseMin());
        int originY = Math.subtractExact(source.feet().y(), target.feet().y());
        return direction.x() == 0 ? new Vec3i(transverse, originY, axial) : new Vec3i(axial, originY, transverse);
    }
    private static boolean collides(Box box, List<Box> others) {
        return others.stream().anyMatch(other -> intersects(box, other));
    }
    private static boolean entrance(Template template) {
        return List.of("入口", "entrance", "entry").contains(template.role());
    }
    private static boolean terminal(Template template) {
        return List.of("终局", "terminal", "final", "finale").contains(template.role());
    }
    private static List<Port> sortedPorts(Template template) {
        return template.ports().stream().sorted(Comparator.comparing(Port::id)).toList();
    }
    private static <T> List<T> shuffled(List<T> input, Random64 random) {
        List<T> result = new ArrayList<>(input);
        for (int index = result.size() - 1; index > 0; index--) {
            int other = random.nextInt(index + 1);
            T previous = result.get(index);
            result.set(index, result.get(other));
            result.set(other, previous);
        }
        return result;
    }
    private static final class Random64 {
        private long state;
        private Random64(long seed) { state = seed; }
        private long nextLong() {
            long value = (state += 0x9e3779b97f4a7c15L);
            value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
            value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
            return value ^ (value >>> 31);
        }
        private int nextInt(int bound) {
            long bits;
            long value;
            do {
                bits = nextLong() >>> 1;
                value = bits % bound;
            } while (bits - value + (bound - 1) < 0);
            return (int) value;
        }
    }

    private interface Encoder { void write(DataOutputStream output) throws IOException; }
    private static byte[] encode(Encoder encoder) {
        try {
            var bytes = new ByteArrayOutputStream();
            try (var output = new DataOutputStream(bytes)) { encoder.write(output); }
            return bytes.toByteArray();
        } catch (IOException exception) { throw new IllegalStateException(exception); }
    }
    private static void string(DataOutputStream output, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        output.writeInt(bytes.length);
        output.write(bytes);
    }
    private static void vector(DataOutputStream output, Vec3i value) throws IOException {
        output.writeInt(value.x()); output.writeInt(value.y()); output.writeInt(value.z());
    }
    private static byte[] digest(byte[] input) {
        try { return MessageDigest.getInstance("SHA-256").digest(input); }
        catch (NoSuchAlgorithmException exception) { throw new IllegalStateException(exception); }
    }
    private static String instanceId(Request request, List<Template> pool, List<Box> occupied, Limits limits) {
        byte[] canonical = encode(output -> {
            byte[] material = seedMaterial(request);
            output.writeInt(material.length);
            output.write(material);
            output.writeInt(limits.minY()); output.writeInt(limits.maxYExclusive());
            output.writeInt(limits.maxGraphHeight()); output.writeBoolean(limits.requireEveryTemplateOnce());
            output.writeInt(limits.rootQuarterTurns());
            output.writeInt(request.roomCount());
            output.writeInt(request.maxAttempts());
            output.writeInt(pool.size());
            for (Template template : pool) {
                string(output, template.id()); string(output, template.biome()); string(output, template.resourceId());
                string(output, template.sha256()); string(output, template.role()); vector(output, template.size());
                output.writeInt(template.ports().size());
                for (Port port : sortedPorts(template)) {
                    string(output, port.id()); vector(output, port.feet()); string(output, port.facing());
                    output.writeInt(port.width()); output.writeInt(port.height()); string(output, port.medium());
                }
            }
            List<Box> sorted = occupied.stream().sorted(Comparator.comparingInt((Box box) -> box.min().x())
                    .thenComparingInt(box -> box.min().y()).thenComparingInt(box -> box.min().z())
                    .thenComparingInt(box -> box.maxExclusive().x()).thenComparingInt(box -> box.maxExclusive().y())
                    .thenComparingInt(box -> box.maxExclusive().z())).toList();
            output.writeInt(sorted.size());
            for (Box box : sorted) { vector(output, box.min()); vector(output, box.maxExclusive()); }
        });
        return "ftdy-" + HexFormat.of().formatHex(digest(canonical));
    }
}

