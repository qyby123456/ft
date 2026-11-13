package cn.ft.biomedungeon.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import static cn.ft.biomedungeon.core.DungeonApi.*;
import static cn.ft.biomedungeon.core.DungeonGeometry.*;

public final class CoreTests {
    private static int checks;
    private static final String HASH = "a".repeat(64);
    private CoreTests() {}

    public static void main(String[] arguments) {
        run("frozen record components", CoreTests::contract);
        run("rotations, bounds and half-open contact", CoreTests::rotations);
        run("all facings, rotations, odd/even apertures", CoreTests::ports);
        run("stable replay and unordered inputs", CoreTests::replay);
        run("200 seeds: connected branching trees, collision-free shells", CoreTests::manySeeds);
        run("occupied root and corridor reservations", CoreTests::obstacles);
        run("empty/invalid/duplicate requests and templates", CoreTests::invalid);
        run("retry cap, dry/wet, dimensions and height refusal", CoreTests::failures);
        run("immutable lists and coordinate overflow", CoreTests::immutability);
        run("maximum room count and concurrent replay", CoreTests::limitsAndConcurrency);
        System.out.println("PASS ALL: " + checks + " assertions; Java=" + System.getProperty("java.version"));
    }
    private static void run(String label, Runnable test) {
        test.run();
        System.out.println("PASS " + label);
    }
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) { throw new AssertionError(message); }
    }
    private static void expect(Class<? extends Throwable> type, Runnable action) {
        checks++;
        try { action.run(); }
        catch (Throwable failure) {
            if (type.isInstance(failure)) { return; }
            throw new AssertionError("Expected " + type.getName() + " but got " + failure, failure);
        }
        throw new AssertionError("Expected " + type.getName());
    }
    private static void refused(DungeonPlanner.FailureReason reason, Runnable action) {
        checks++;
        try { action.run(); }
        catch (DungeonPlanner.PlanningException failure) {
            check(failure.reason() == reason, "Wrong failure reason: " + failure);
            return;
        }
        throw new AssertionError("Expected planning failure " + reason);
    }
    private static Request request(long seed, int count, int attempts) {
        return new Request(seed, "minecraft:overworld", new Vec3i(100, 64, -200), 7, "test-v1", "test", count, attempts);
    }
    private static Template template(String id, String role, int width, int feetY, String medium) {
        return new Template(id, "test", "biomedungeon:" + id, HASH, new Vec3i(13, 8, 11), List.of(
                new Port("n", new Vec3i(6, feetY, 0), "north", width, 3, medium),
                new Port("e", new Vec3i(12, feetY, 5), "east", width, 3, medium),
                new Port("s", new Vec3i(6, feetY, 10), "south", width, 3, medium),
                new Port("w", new Vec3i(0, feetY, 5), "west", width, 3, medium)), role);
    }
    private static List<Template> pool() {
        return List.of(template("entry", "入口", 3, 1, "dry"), template("oak", "普通", 3, 1, "dry"),
                template("stone", "normal", 3, 1, "dry"), template("end", "终局", 3, 1, "dry"));
    }
    private static void contract() {
        check(names(Vec3i.class).equals("x,y,z"), "Vec3i contract");
        check(names(Box.class).equals("min,maxExclusive"), "Box contract");
        check(names(Port.class).equals("id,feet,facing,width,height,medium"), "Port contract");
        check(names(Template.class).equals("id,biome,resourceId,sha256,size,ports,role"), "Template contract");
        check(names(Request.class).equals("worldSeed,dimension,anchor,instanceOrdinal,generatorVersion,biome,roomCount,maxAttempts"), "Request contract");
        check(names(PlacedRoom.class).equals("nodeId,templateId,origin,quarterTurns,bounds"), "PlacedRoom contract");
        check(names(Link.class).equals("fromNode,fromPort,toNode,toPort,waypoints,width,height"), "Link contract");
        check(names(Plan.class).equals("instanceId,seed,generatorVersion,rooms,links"), "Plan contract");
        check(List.of(Phase.values()).toString().equals("[PLANNED, RESERVED, BUILDING, VERIFYING, COMMITTED, FAILED, ROLLBACK_REQUIRED]"), "Phase contract");
    }
    private static String names(Class<?> type) {
        return String.join(",", java.util.Arrays.stream(type.getRecordComponents()).map(java.lang.reflect.RecordComponent::getName).toList());
    }
    private static void rotations() {
        Vec3i point = new Vec3i(2, 3, 5);
        check(rotate(point, 0).equals(point), "rotation 0");
        check(rotate(point, 1).equals(new Vec3i(-5, 3, 2)), "rotation 1");
        check(rotate(point, 2).equals(new Vec3i(-2, 3, -5)), "rotation 2");
        check(rotate(point, 3).equals(new Vec3i(5, 3, -2)), "rotation 3");
        Vec3i origin = new Vec3i(10, 20, 30);
        Vec3i size = new Vec3i(2, 3, 5);
        List<Box> expected = List.of(
                new Box(origin, new Vec3i(12, 23, 35)),
                new Box(new Vec3i(5, 20, 30), new Vec3i(10, 23, 32)),
                new Box(new Vec3i(8, 20, 25), new Vec3i(10, 23, 30)),
                new Box(new Vec3i(10, 20, 28), new Vec3i(15, 23, 30)));
        for (int rotation = 0; rotation < 4; rotation++) {
            check(bounds(size, origin, rotation).equals(expected.get(rotation)), "rotated bounds " + rotation);
            check(rotate(rotate(point, rotation), 4 - rotation).equals(point), "inverse rotation");
            for (int localX = 0; localX < size.x(); localX++) {
                for (int localY = 0; localY < size.y(); localY++) {
                    for (int localZ = 0; localZ < size.z(); localZ++) {
                        check(contains(expected.get(rotation), blockPosition(new Vec3i(localX, localY, localZ), origin, rotation)), "rotated voxel inside half-open bounds");
                    }
                }
            }
        }
        Box left = new Box(new Vec3i(0, 0, 0), new Vec3i(2, 2, 2));
        check(!intersects(left, new Box(new Vec3i(2, 0, 0), new Vec3i(3, 2, 2))), "face touching is not collision");
        check(intersects(left, new Box(new Vec3i(1, 1, 1), new Vec3i(3, 3, 3))), "positive volume collision");
    }
    private static void ports() {
        for (int width : List.of(1, 2, 3, 4)) {
            Template template = template("ports", "normal", width, 1, "dry");
            for (Port port : template.ports()) {
                for (int rotation = 0; rotation < 4; rotation++) {
                    Vec3i origin = new Vec3i(21, 10, -9);
                    WorldPort world = worldPort(template, port, origin, rotation);
                    check(world.feet().equals(add(origin, rotate(port.feet(), rotation))), "actual feet transform");
                    check(world.transverseMaxExclusive() - world.transverseMin() == width, "aperture width preserved");
                    check(world.facing().equals(rotateFacing(port.facing(), rotation)), "facing transform");
                    Box room = bounds(template.size(), origin, rotation);
                    int expectedSurface = switch (world.facing()) {
                        case "north" -> room.min().z();
                        case "east" -> room.maxExclusive().x();
                        case "south" -> room.maxExclusive().z();
                        default -> room.min().x();
                    };
                    check(world.surface() == expectedSurface, "aperture lies on rotated room surface");
                    Vec3i exterior = exteriorFeet(world);
                    check(!contains(room, exterior), "exterior foot must be outside owner");
                    boolean localAlongX = port.facing().equals("north") || port.facing().equals("south");
                    for (int offset = -(width - 1) / 2; offset < width - (width - 1) / 2; offset++) {
                        Vec3i localBlock = add(port.feet(), localAlongX ? new Vec3i(offset, 0, 0) : new Vec3i(0, 0, offset));
                        Vec3i voxel = blockPosition(localBlock, origin, rotation);
                        int transverse = world.facing().equals("north") || world.facing().equals("south") ? voxel.x() : voxel.z();
                        check(transverse >= world.transverseMin() && transverse < world.transverseMaxExclusive(), "all actual doorway voxels inside rotated aperture");
                        check(contains(room, voxel), "doorway voxel belongs to room");
                        check(!contains(room, add(voxel, direction(world.facing()))), "doorway outward voxel outside room");
                    }
                }
            }
            List<Template> evenPool = List.of(template("e", "entry", width, 1, "dry"),
                    template("m", "normal", width, 1, "dry"));
            for (int seed = 0; seed < 12; seed++) {
                verify(DungeonPlanner.plan(request(seed, 8, 20000), evenPool, List.of()), evenPool, List.of());
            }
        }
    }
    private static boolean contains(Box box, Vec3i point) {
        return point.x() >= box.min().x() && point.x() < box.maxExclusive().x()
                && point.y() >= box.min().y() && point.y() < box.maxExclusive().y()
                && point.z() >= box.min().z() && point.z() < box.maxExclusive().z();
    }
    private static void replay() {
        Request request = request(12345, 12, 20000);
        Plan plan = DungeonPlanner.plan(request, pool(), List.of());
        check(plan.seed() == 8131559247039922076L, "frozen SHA-256 seed golden vector");
        check(plan.equals(DungeonPlanner.plan(request, pool(), List.of())), "same input stable");
        List<Template> reversed = new ArrayList<>();
        for (Template template : pool()) {
            List<Port> ports = new ArrayList<>(template.ports());
            Collections.reverse(ports);
            reversed.add(new Template(template.id(), template.biome(), template.resourceId(), template.sha256(), template.size(), ports, template.role()));
        }
        Collections.reverse(reversed);
        check(plan.equals(DungeonPlanner.plan(request, reversed, List.of())), "pool and port input order independent");
        List<Box> obstacles = List.of(new Box(new Vec3i(5000, 0, 0), new Vec3i(5010, 200, 10)),
                new Box(new Vec3i(-5000, 0, 0), new Vec3i(-4990, 200, 10)));
        check(DungeonPlanner.plan(request, pool(), obstacles).equals(DungeonPlanner.plan(request, reversed,
                List.of(obstacles.get(1), obstacles.get(0)))), "occupied insertion order independent");
        check(plan.seed() != DungeonPlanner.deriveSeed(new Request(12345, "minecraft:the_nether", request.anchor(), 7, "test-v1", "test", 12, 20000)), "dimension enters seed");
        System.out.println("REPLAY seed=" + plan.seed() + " instanceId=" + plan.instanceId());
        System.out.println("REPLAY rooms=" + plan.rooms());
        System.out.println("REPLAY links=" + plan.links());
    }
    private static void manySeeds() {
        Set<String> layouts = new HashSet<>();
        Set<String> selected = new HashSet<>();
        int branched = 0;
        for (int seed = 0; seed < 200; seed++) {
            Plan plan = DungeonPlanner.plan(request(seed, 12, 20000), pool(), List.of());
            verify(plan, pool(), List.of());
            layouts.add(plan.rooms().toString());
            plan.rooms().forEach(room -> selected.add(room.templateId()));
            var degree = new TreeMap<String, Integer>();
            for (Link link : plan.links()) {
                degree.merge(link.fromNode(), 1, Integer::sum);
                degree.merge(link.toNode(), 1, Integer::sum);
            }
            if (degree.values().stream().anyMatch(value -> value >= 3)) { branched++; }
            check(plan.rooms().get(0).templateId().equals("entry"), "entrance first");
            check(plan.rooms().get(plan.rooms().size() - 1).templateId().equals("end"), "terminal last");
        }
        check(layouts.size() >= 190, "seed-driven layouts not a fixed row");
        check(branched >= 100, "actual branching, not just turning a row");
        check(selected.containsAll(List.of("oak", "stone")), "room-level template sampling");
        System.out.println("DIVERSITY uniqueLayouts=" + layouts.size() + "/200 branchedTrees=" + branched + "/200");
    }
    private static void verify(Plan plan, List<Template> templates, List<Box> occupied) {
        var byTemplate = new TreeMap<String, Template>();
        templates.forEach(template -> byTemplate.put(template.id(), template));
        var byNode = new TreeMap<String, PlacedRoom>();
        List<Box> all = new ArrayList<>(occupied);
        for (PlacedRoom room : plan.rooms()) {
            check(byNode.put(room.nodeId(), room) == null, "unique node");
            check(room.bounds().equals(bounds(byTemplate.get(room.templateId()).size(), room.origin(), room.quarterTurns())), "bounds from unnormalized origin");
            for (Box other : all) { check(!intersects(room.bounds(), other), "room collision"); }
            all.add(room.bounds());
        }
        Set<String> connected = new HashSet<>();
        Set<String> portsUsed = new HashSet<>();
        connected.add(plan.rooms().get(0).nodeId());
        check(plan.links().size() == plan.rooms().size() - 1, "tree edge count");
        for (Link link : plan.links()) {
            check(connected.contains(link.fromNode()) && !connected.contains(link.toNode()), "tree connected without cycles");
            connected.add(link.toNode());
            check(portsUsed.add(link.fromNode() + "/" + link.fromPort()), "source port reused");
            check(portsUsed.add(link.toNode() + "/" + link.toPort()), "target port reused");
            WorldPort source = lookup(link.fromNode(), link.fromPort(), byNode, byTemplate);
            WorldPort target = lookup(link.toNode(), link.toPort(), byNode, byTemplate);
            check(source.facing().equals(rotateFacing(target.facing(), 2)), "faces oppose");
            check(source.medium().equals(target.medium()), "medium match");
            check(source.feet().y() == target.feet().y(), "level openings");
            check(source.transverseMin() == target.transverseMin() && source.transverseMaxExclusive() == target.transverseMaxExclusive(), "exact opening alignment");
            check(link.waypoints().equals(List.of(exteriorFeet(source), exteriorFeet(target))), "waypoints are actual portal exterior feet");
            int gap = Math.abs(source.surface() - target.surface());
            check(gap >= DungeonPlanner.MIN_GAP && gap <= DungeonPlanner.MAX_GAP, "gap bounded");
            for (Box corridor : corridorBounds(link)) {
                for (Box other : all) { check(!intersects(corridor, other), "reserved shell collision"); }
                all.add(corridor);
            }
        }
        check(connected.size() == plan.rooms().size(), "every room reachable and returnable");
    }
    private static WorldPort lookup(String nodeId, String portId, TreeMap<String, PlacedRoom> nodes, TreeMap<String, Template> templates) {
        PlacedRoom room = nodes.get(nodeId);
        Template template = templates.get(room.templateId());
        Port port = template.ports().stream().filter(candidate -> candidate.id().equals(portId)).findFirst().orElseThrow();
        return worldPort(template, port, room.origin(), room.quarterTurns());
    }
    private static void obstacles() {
        Request request = request(91, 10, 20000);
        Plan original = DungeonPlanner.plan(request, pool(), List.of());
        Box corridor = corridorBounds(original.links().get(0)).get(0);
        Plan rerouted = DungeonPlanner.plan(request, pool(), List.of(corridor));
        verify(rerouted, pool(), List.of(corridor));
        check(!original.rooms().equals(rerouted.rooms()), "occupied corridor forces reroute");
        Box blocked = new Box(new Vec3i(-10000, -1000, -10000), new Vec3i(10000, 1000, 10000));
        refused(DungeonPlanner.FailureReason.EXHAUSTED, () -> DungeonPlanner.plan(request, pool(), List.of(blocked)));
    }
    private static void invalid() {
        expect(IllegalArgumentException.class, () -> request(1, 0, 100));
        expect(IllegalArgumentException.class, () -> request(1, 65, 100));
        expect(IllegalArgumentException.class, () -> request(1, 1, 0));
        expect(IllegalArgumentException.class, () -> request(1, 1, 100001));
        expect(IllegalArgumentException.class, () -> new Box(new Vec3i(0, 0, 0), new Vec3i(0, 2, 2)));
        expect(IllegalArgumentException.class, () -> new Port("bad", new Vec3i(0, 1, 0), "up", 3, 3, "dry"));
        expect(IllegalArgumentException.class, () -> new Port("bad", new Vec3i(0, 1, 0), "north", 0, 3, "dry"));
        expect(IllegalArgumentException.class, () -> new Port("bad", new Vec3i(0, 1, 0), "north", 3, 3, "lava"));
        expect(IllegalArgumentException.class, () -> new Template("bad", "test", "../bad", HASH, new Vec3i(2, 4, 2), List.of(), "normal"));
        refused(DungeonPlanner.FailureReason.EMPTY_POOL, () -> DungeonPlanner.plan(request(1, 2, 100), List.of(), List.of()));
        refused(DungeonPlanner.FailureReason.DUPLICATE_TEMPLATE, () -> DungeonPlanner.plan(request(1, 2, 100), List.of(pool().get(0), pool().get(0)), List.of()));
        Template internal = new Template("internal", "test", "biomedungeon:internal", HASH, new Vec3i(9, 7, 9),
                List.of(new Port("n", new Vec3i(4, 1, 3), "north", 3, 3, "dry")), "normal");
        refused(DungeonPlanner.FailureReason.INVALID_TEMPLATE, () -> DungeonPlanner.plan(request(1, 2, 100), List.of(internal), List.of()));
        Template overflowPort = new Template("overflow_port", "test", "biomedungeon:overflow_port", HASH, new Vec3i(9, 7, 9),
                List.of(new Port("n", new Vec3i(Integer.MIN_VALUE, 1, 0), "north", 3, 3, "dry")), "normal");
        refused(DungeonPlanner.FailureReason.INVALID_TEMPLATE, () -> DungeonPlanner.plan(request(1, 2, 100), List.of(overflowPort), List.of()));
        Link diagonal = new Link("a", "p", "b", "p", List.of(new Vec3i(0, 2, 0), new Vec3i(2, 2, 2)), 3, 3);
        expect(IllegalArgumentException.class, () -> corridorBounds(diagonal));
    }
    private static void failures() {
        try { DungeonPlanner.plan(request(1, 2, 1), pool(), List.of()); throw new AssertionError("cap ignored"); }
        catch (DungeonPlanner.PlanningException failure) { check(failure.attempts() == 1, "hard global attempt cap"); }
        Template entry = template("entry", "entry", 3, 1, "dry");
        for (Template incompatible : List.of(template("wet", "normal", 3, 1, "wet"),
                template("high", "normal", 3, 2, "dry"), template("wide", "normal", 4, 1, "dry"))) {
            refused(DungeonPlanner.FailureReason.EXHAUSTED,
                    () -> DungeonPlanner.plan(request(1, 2, 300), List.of(entry, incompatible), List.of()));
        }
        Template leaf = new Template("leaf", "test", "biomedungeon:leaf", HASH, entry.size(), List.of(entry.ports().get(0)), "normal");
        refused(DungeonPlanner.FailureReason.EXHAUSTED, () -> DungeonPlanner.plan(request(1, 3, 1000), List.of(leaf), List.of()));
    }
    private static void immutability() {
        List<Port> input = new ArrayList<>(pool().get(0).ports());
        Template copied = new Template("copy", "test", "biomedungeon:copy", HASH, pool().get(0).size(), input, "normal");
        input.clear();
        check(copied.ports().size() == 4, "ports defensive copy");
        Plan plan = DungeonPlanner.plan(request(1, 4, 2000), pool(), List.of());
        expect(UnsupportedOperationException.class, () -> plan.rooms().clear());
        expect(UnsupportedOperationException.class, () -> plan.links().clear());
        expect(UnsupportedOperationException.class, () -> plan.links().get(0).waypoints().clear());
        Request overflow = new Request(1, "minecraft:overworld", new Vec3i(Integer.MAX_VALUE, 64, Integer.MAX_VALUE), 0, "v1", "test", 2, 100);
        refused(DungeonPlanner.FailureReason.COORDINATE_OVERFLOW, () -> DungeonPlanner.plan(overflow, pool(), List.of()));
    }
    private static void limitsAndConcurrency() {
        Plan largest = DungeonPlanner.plan(request(844, 64, 100000), pool(), List.of());
        verify(largest, pool(), List.of());
        check(largest.rooms().size() == 64, "maximum supported room count");
        Template solo = new Template("solo", "test", "biomedungeon:solo", HASH, new Vec3i(3, 3, 3), List.of(), "normal");
        check(DungeonPlanner.plan(request(1, 1, 1), List.of(solo), List.of()).links().isEmpty(), "single room may have no ports");
        var executor = java.util.concurrent.Executors.newFixedThreadPool(4);
        try {
            Request request = request(55, 16, 20000);
            Plan expected = DungeonPlanner.plan(request, pool(), List.of());
            List<java.util.concurrent.Callable<Plan>> jobs = new ArrayList<>();
            for (int index = 0; index < 24; index++) { jobs.add(() -> DungeonPlanner.plan(request, pool(), List.of())); }
            for (var future : executor.invokeAll(jobs)) { check(expected.equals(future.get()), "concurrent replay shares no RNG state"); }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        } catch (java.util.concurrent.ExecutionException exception) { throw new AssertionError(exception); }
        finally { executor.shutdownNow(); }
    }
}
