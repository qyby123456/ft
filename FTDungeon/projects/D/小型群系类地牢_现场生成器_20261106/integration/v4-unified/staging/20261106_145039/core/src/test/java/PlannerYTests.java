import cn.ft.biomedungeon.core.DungeonPlanner;
import cn.ft.biomedungeon.core.next.DungeonYPlanner;
import cn.ft.biomedungeon.core.next.DungeonYPlanner.Limits;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import static cn.ft.biomedungeon.core.DungeonApi.*;
import static cn.ft.biomedungeon.core.DungeonGeometry.*;

public final class PlannerYTests {
    private static int assertions;
    private static final String HASH = "a".repeat(64);
    private PlannerYTests() {}
    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) { throw new AssertionError(message); }
    }
    private static void expectFailure(DungeonYPlanner.FailureReason reason, Runnable action) {
        try { action.run(); throw new AssertionError("Expected " + reason); }
        catch (DungeonYPlanner.PlanningException failure) {
            check(failure.reason() == reason, "Wrong refusal " + failure);
            System.out.println("EXPECTED " + failure);
        }
    }
    private static Request request(long seed, int count, int attempts) {
        return new Request(seed, "minecraft:overworld", new Vec3i(0, 64, 0), 7, "planner-y-v2-experimental", "s8", count, attempts);
    }
    private static List<Template> load() throws Exception {
        Map<String, List<String[]>> rows = new TreeMap<>();
        for (String line : Files.readAllLines(Path.of("inputs/real-nine-ports.tsv"))) {
            String[] fields = line.split("\t");
            rows.computeIfAbsent(fields[0], ignored -> new ArrayList<>()).add(fields);
        }
        List<Template> templates = new ArrayList<>();
        for (List<String[]> room : rows.values()) {
            String[] first = room.get(0);
            List<Port> ports = new ArrayList<>();
            for (String[] fields : room) {
                ports.add(new Port(fields[8], new Vec3i(Integer.parseInt(fields[9]), Integer.parseInt(fields[10]), Integer.parseInt(fields[11])),
                        fields[12], Integer.parseInt(fields[13]), Integer.parseInt(fields[14]), fields[15]));
            }
            templates.add(new Template(first[0], first[1], first[2], first[3],
                    new Vec3i(Integer.parseInt(first[5]), Integer.parseInt(first[6]), Integer.parseInt(first[7])), ports, first[4]));
        }
        return templates;
    }
    private static Template withPorts(Template template, List<Port> ports, String role) {
        return new Template(template.id(), template.biome(), template.resourceId(), template.sha256(), template.size(), ports, role);
    }
    private static void verify(Plan plan, List<Template> templates, List<Box> occupied, Limits limits) {
        Map<String, Template> byTemplate = new TreeMap<>();
        templates.forEach(template -> byTemplate.put(template.id(), template));
        Map<String, PlacedRoom> nodes = new TreeMap<>();
        Set<String> selected = new HashSet<>();
        List<Box> reserved = new ArrayList<>(occupied);
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (PlacedRoom room : plan.rooms()) {
            check(nodes.put(room.nodeId(), room) == null, "unique nodes");
            if (limits.requireEveryTemplateOnce()) { check(selected.add(room.templateId()), "unique original room not repeats"); }
            Template template = byTemplate.get(room.templateId());
            check(room.bounds().equals(bounds(template.size(), room.origin(), room.quarterTurns())), "actual translated eight-corner bounds");
            for (Box other : reserved) { check(!intersects(room.bounds(), other), "room3D collision"); }
            for (Port port : template.ports()) {
                check(worldPort(template, port, room.origin(), room.quarterTurns()).feet().y() == room.origin().y() + port.feet().y(), "original local elevation retained");
            }
            for (int localX : List.of(0, template.size().x() - 1)) {
                for (int localY : List.of(0, template.size().y() - 1)) {
                    for (int localZ : List.of(0, template.size().z() - 1)) {
                        check(contains(room.bounds(), blockPosition(new Vec3i(localX, localY, localZ), room.origin(), room.quarterTurns())), "actual rotated voxel within owned box");
                    }
                }
            }
            check(room.bounds().min().y() >= limits.minY() && room.bounds().maxExclusive().y() <= limits.maxYExclusive(), "room world height limit");
            minY = Math.min(minY, room.bounds().min().y()); maxY = Math.max(maxY, room.bounds().maxExclusive().y());
            reserved.add(room.bounds());
        }
        if (limits.requireEveryTemplateOnce()) { check(selected.equals(byTemplate.keySet()), "all requested templates present exactly once"); }
        check(plan.links().size() == plan.rooms().size() - 1, "tree edge count");
        Set<String> connected = new HashSet<>();
        Set<String> used = new HashSet<>();
        connected.add(plan.rooms().get(0).nodeId());
        for (Link link : plan.links()) {
            check(connected.contains(link.fromNode()) && connected.add(link.toNode()), "connected acyclic tree");
            PlacedRoom parent = nodes.get(link.fromNode());
            PlacedRoom child = nodes.get(link.toNode());
            Template sourceTemplate = byTemplate.get(parent.templateId());
            Template targetTemplate = byTemplate.get(child.templateId());
            Port sourcePort = sourceTemplate.ports().stream().filter(port -> port.id().equals(link.fromPort())).findFirst().orElseThrow();
            Port targetPort = targetTemplate.ports().stream().filter(port -> port.id().equals(link.toPort())).findFirst().orElseThrow();
            WorldPort source = worldPort(sourceTemplate, sourcePort, parent.origin(), parent.quarterTurns());
            WorldPort target = worldPort(targetTemplate, targetPort, child.origin(), child.quarterTurns());
            check(used.add(parent.nodeId() + "/" + sourcePort.id()) && used.add(child.nodeId() + "/" + targetPort.id()), "port reuse rejected");
            check(source.width() == target.width() && source.width() == link.width(), "exact width3/5 compatibility");
            check(source.height() == target.height() && source.height() == link.height(), "exact height compatibility");
            check(source.medium().equals(target.medium()), "exact medium compatibility");
            check(source.feet().y() == target.feet().y(), "actual world feet align after Y solve");
            check(child.origin().y() == parent.origin().y() + sourcePort.feet().y() - targetPort.feet().y(), "recursive rigid-room Y solve");
            check(source.facing().equals(rotateFacing(target.facing(), 2)), "opposing facing");
            check(source.transverseMin() == target.transverseMin() && source.transverseMaxExclusive() == target.transverseMaxExclusive(), "true aperture intervals align");
            check(link.waypoints().equals(List.of(exteriorFeet(source), exteriorFeet(target))), "actual level exterior waypoints");
            check(Math.abs(source.surface() - target.surface()) >= 3 && Math.abs(source.surface() - target.surface()) <= 10, "gap bounds");
            for (Box shell : corridorBounds(link)) {
                for (Box other : reserved) { check(!intersects(shell, other), "full translated corridor shell3D collision"); }
                check(shell.min().y() >= limits.minY() && shell.maxExclusive().y() <= limits.maxYExclusive(), "corridor floor/head height limit");
                minY = Math.min(minY, shell.min().y()); maxY = Math.max(maxY, shell.maxExclusive().y());
                reserved.add(shell);
            }
        }
        check(connected.size() == plan.rooms().size(), "all rooms connected both directions geometrically");
        check((long) maxY - minY <= limits.maxGraphHeight(), "whole graph envelope, not only local edges");
    }
    private static boolean contains(Box box, Vec3i point) {
        return point.x() >= box.min().x() && point.x() < box.maxExclusive().x()
                && point.y() >= box.min().y() && point.y() < box.maxExclusive().y()
                && point.z() >= box.min().z() && point.z() < box.maxExclusive().z();
    }
    private static void realNine(List<Template> templates) throws Exception {
        Set<String> unique = new HashSet<>();
        Set<Integer> allRotations = new HashSet<>();
        Set<String> nonGeometricVariants = new HashSet<>();
        int successes = 0;
        int failures = 0;
        int hallMixedLevels = 0;
        int shiftedGraphs = 0;
        List<String> outcomes = new ArrayList<>();
        Files.createDirectories(Path.of("results/plans"));
        for (int rootRotation = 0; rootRotation < 4; rootRotation++) {
            int rotationSuccesses = 0;
            Set<String> rotationLayouts = new HashSet<>();
            for (int seed = 0; seed < 32; seed++) {
                Limits limits = new Limits(-64, 320, 64, true, rootRotation);
                Request request = request(seed, 9, 100000);
                try {
                    Plan plan = DungeonYPlanner.plan(request, templates, List.of(), limits);
                    verify(plan, templates, List.of(), limits);
                    check(plan.rooms().get(0).templateId().equals("s8_07"), "actual entrance07 root");
                    check(plan.rooms().get(0).quarterTurns() == rootRotation, "forced root rotation actually obeyed");
                    check(plan.rooms().get(8).templateId().equals("s8_09"), "actual treasury09 terminal");
                    if (plan.rooms().stream().anyMatch(room -> room.origin().y() != 64)) { shiftedGraphs++; }
                    Set<Integer> hallFeet = new HashSet<>();
                    PlacedRoom hall = plan.rooms().stream().filter(room -> room.templateId().equals("s8_04")).findFirst().orElseThrow();
                    Template originalHall = templates.stream().filter(template -> template.id().equals("s8_04")).findFirst().orElseThrow();
                    for (Link link : plan.links()) {
                        String portId = link.fromNode().equals(hall.nodeId()) ? link.fromPort() : link.toNode().equals(hall.nodeId()) ? link.toPort() : null;
                        if (portId != null) { hallFeet.add(originalHall.ports().stream().filter(port -> port.id().equals(portId)).findFirst().orElseThrow().feet().y()); }
                    }
                    if (hallFeet.size() > 1) { hallMixedLevels++; }
                    plan.rooms().forEach(room -> allRotations.add(room.quarterTurns()));
                    unique.add(plan.rooms().toString() + plan.links());
                    rotationLayouts.add(plan.rooms().toString() + plan.links());
                    nonGeometricVariants.add(plan.rooms().stream().map(PlacedRoom::templateId).toList().toString()
                            + plan.links().stream().map(link -> link.fromNode() + "/" + link.fromPort() + "->" + link.toNode() + "/" + link.toPort()).toList());
                    Files.writeString(Path.of("results/plans/q" + rootRotation + "-seed" + seed + ".json"), json(plan));
                    outcomes.add("{\"seed\":" + seed + ",\"rootQuarterTurns\":" + rootRotation + ",\"result\":\"PASS_NINE_UNIQUE_GEOMETRY_ONLY\",\"instanceId\":\"" + plan.instanceId() + "\"}");
                    successes++; rotationSuccesses++;
                    if (seed == 0) {
                        check(plan.equals(DungeonYPlanner.plan(request, templates, List.of(), limits)), "same seed stable");
                        List<Template> shuffledInput = new ArrayList<>();
                        for (Template template : templates) {
                            List<Port> ports = new ArrayList<>(template.ports()); Collections.reverse(ports);
                            shuffledInput.add(withPorts(template, ports, template.role()));
                        }
                        Collections.reverse(shuffledInput);
                        check(plan.equals(DungeonYPlanner.plan(request, shuffledInput, List.of(), limits)), "pool/port insertion independent");
                        System.out.println("NINE_ROOM_REPLAY q=" + rootRotation + " seed=" + seed + " id=" + plan.instanceId());
                        System.out.println("ROOMS " + plan.rooms());
                    }
                } catch (DungeonYPlanner.PlanningException failure) {
                    failures++;
                    outcomes.add("{\"seed\":" + seed + ",\"rootQuarterTurns\":" + rootRotation + ",\"result\":\"" + failure.reason() + "\",\"attempts\":" + failure.attempts() + "}");
                    System.out.println("NINE_ROOM_FAILURE q=" + rootRotation + " seed=" + seed + " " + failure);
                }
            }
            check(rotationSuccesses > 0, "at least one actual full nine-room tree per root rotation");
            check(rotationLayouts.size() > 16, "seed variation within fixed root rotation, not whole-graph rotation counting");
            System.out.println("ROOT_ROTATION q=" + rootRotation + " fullNineSuccesses=" + rotationSuccesses + "/32");
        }
        check(unique.size() > 32, "nontrivial seed-driven nine-room layouts");
        check(allRotations.size() == 4, "all four rotations on actual rooms");
        check(hallMixedLevels > 0, "real04 uses multiple local foot elevations in actual full graphs");
        check(nonGeometricVariants.size() > 16, "template and port tree choices vary, not merely offsets/gaps/orientations");
        check(shiftedGraphs == successes, "all full graphs genuinely use candidate Y solve");
        Files.writeString(Path.of("results/real-nine-results.json"), "{\"geometryOnly\":true,\"productionPromotion\":false,\"seedCount\":32,\"rootRotations\":4,\"successes\":" + successes
                + ",\"failures\":" + failures + ",\"distinctLayouts\":" + unique.size() + ",\"hall04UsesDifferentLocalFeetGraphs\":" + hallMixedLevels
                + ",\"shiftedGraphs\":" + shiftedGraphs + ",\"templateAndPortTreeVariants\":" + nonGeometricVariants.size() + ",\"world\":\"NOT_RUN\",\"outcomes\":[" + String.join(",", outcomes) + "]}\n");
        System.out.println("REAL_NINE_SUMMARY successes=" + successes + "/128 failures=" + failures + " distinct=" + unique.size() + " mixedHall04=" + hallMixedLevels + " Yshifted=" + shiftedGraphs + " templatePortVariants=" + nonGeometricVariants.size());
    }
    private static String vector(Vec3i point) { return "{\"x\":" + point.x() + ",\"y\":" + point.y() + ",\"z\":" + point.z() + "}"; }
    private static String box(Box box) { return "{\"min\":" + vector(box.min()) + ",\"maxExclusive\":" + vector(box.maxExclusive()) + "}"; }
    private static String json(Plan plan) {
        List<String> rooms = new ArrayList<>();
        List<String> links = new ArrayList<>();
        List<String> shells = new ArrayList<>();
        for (PlacedRoom room : plan.rooms()) {
            rooms.add("{\"nodeId\":\"" + room.nodeId() + "\",\"templateId\":\"" + room.templateId() + "\",\"origin\":" + vector(room.origin()) + ",\"quarterTurns\":" + room.quarterTurns() + ",\"bounds\":" + box(room.bounds()) + "}");
        }
        for (Link link : plan.links()) {
            links.add("{\"fromNode\":\"" + link.fromNode() + "\",\"fromPort\":\"" + link.fromPort() + "\",\"toNode\":\"" + link.toNode() + "\",\"toPort\":\"" + link.toPort()
                    + "\",\"waypoints\":[" + String.join(",", link.waypoints().stream().map(PlannerYTests::vector).toList()) + "],\"width\":" + link.width() + ",\"height\":" + link.height() + "}");
            corridorBounds(link).forEach(shell -> shells.add(box(shell)));
        }
        return "{\"instanceId\":\"" + plan.instanceId() + "\",\"seed\":" + plan.seed() + ",\"generatorVersion\":\"" + plan.generatorVersion()
                + "\",\"rooms\":[" + String.join(",", rooms) + "],\"links\":[" + String.join(",", links) + "],\"reservedShells\":[" + String.join(",", shells) + "],\"nativeWorldTest\":\"NOT_RUN\"}\n";
    }
    private static Template synthetic(String id, String role, List<Port> ports) {
        return new Template(id, "s8", "biomedungeon:" + id, HASH, new Vec3i(11, 10, 11), ports, role);
    }
    private static List<Template> risingChain() {
        return List.of(synthetic("root", "entrance", List.of(new Port("out", new Vec3i(10, 5, 5), "east", 3, 3, "dry"))),
                synthetic("middle", "normal", List.of(new Port("in", new Vec3i(0, 1, 5), "west", 3, 3, "dry"), new Port("out", new Vec3i(10, 5, 5), "east", 5, 3, "dry"))),
                synthetic("end", "terminal", List.of(new Port("in", new Vec3i(0, 1, 5), "west", 5, 3, "dry"))));
    }
    private static void heightAndCollision() {
        List<Template> templates = risingChain();
        Limits broad = new Limits(0, 256, 32, true, 0);
        Plan plan = DungeonYPlanner.plan(request(78, 3, 10000), templates, List.of(), broad);
        verify(plan, templates, List.of(), broad);
        check(plan.rooms().stream().map(room -> room.origin().y()).toList().equals(List.of(64, 68, 72)), "cumulative recursive Y, not reset per edge");
        expectFailure(DungeonYPlanner.FailureReason.EXHAUSTED, () -> DungeonYPlanner.plan(request(78, 3, 10000), templates, List.of(), new Limits(0, 256, 14, true, 0)));
        expectFailure(DungeonYPlanner.FailureReason.EXHAUSTED, () -> DungeonYPlanner.plan(request(78, 3, 10000), templates, List.of(), new Limits(64, 80, 32, true, 0)));
        expectFailure(DungeonYPlanner.FailureReason.EXHAUSTED, () -> DungeonYPlanner.plan(request(78, 3, 10000), templates, List.of(), new Limits(65, 256, 32, true, 0)));
        verify(DungeonYPlanner.plan(request(78, 3, 10000), templates, List.of(), new Limits(64, 82, 18, true, 0)), templates, List.of(), new Limits(64, 82, 18, true, 0));
        Box shell = corridorBounds(plan.links().get(0)).get(0);
        expectFailure(DungeonYPlanner.FailureReason.EXHAUSTED, () -> DungeonYPlanner.plan(request(78, 3, 10000), templates, List.of(shell), broad));
        Box target = plan.rooms().get(1).bounds();
        List<Box> below = List.of(new Box(new Vec3i(target.min().x(), 0, target.min().z()), new Vec3i(target.maxExclusive().x(), 64, target.maxExclusive().z())));
        Plan above = DungeonYPlanner.plan(request(78, 3, 10000), templates, below, broad);
        verify(above, templates, below, broad);
        check(above.rooms().equals(plan.rooms()) && above.links().equals(plan.links()), "sameXZ but disjointY not false collision");
        Box allAtTargetHeight = new Box(new Vec3i(-10000, 74, -10000), new Vec3i(10000, 83, 10000));
        expectFailure(DungeonYPlanner.FailureReason.EXHAUSTED, () -> DungeonYPlanner.plan(request(78, 3, 10000), templates, List.of(allAtTargetHeight), broad));
        System.out.println("PASS cumulative height, inclusive/exclusive height boundaries, graph span and shifted3D occupied/shell tests");
    }
    private static void refusalAndReplay(List<Template> real) throws Exception {
        Template first = real.get(0);
        Template seventh = real.get(6);
        List<Template> pair = List.of(withPorts(first, List.of(first.ports().get(0)), "entry"), withPorts(seventh, List.of(seventh.ports().get(0)), "terminal"));
        Plan shifted = DungeonYPlanner.plan(request(1, 2, 10000), pair, List.of());
        verify(shifted, pair, List.of(), Limits.defaults());
        try { DungeonPlanner.plan(request(1, 2, 10000), pair, List.of()); throw new AssertionError("baseline unexpectedly translates"); }
        catch (DungeonPlanner.PlanningException failure) { check(failure.getMessage().contains("height difference unsupported"), "baseline behavior retained"); }
        List<Template> wrongWidth = List.of(pair.get(0), withPorts(seventh, List.of(seventh.ports().get(1)), "terminal"));
        expectFailure(DungeonYPlanner.FailureReason.EXHAUSTED, () -> DungeonYPlanner.plan(request(1, 2, 1000), wrongWidth, List.of()));
        Port dry = pair.get(1).ports().get(0);
        Port wet = new Port(dry.id(), dry.feet(), dry.facing(), dry.width(), dry.height(), "wet");
        expectFailure(DungeonYPlanner.FailureReason.EXHAUSTED, () -> DungeonYPlanner.plan(request(1, 2, 1000), List.of(pair.get(0), withPorts(pair.get(1), List.of(wet), "terminal")), List.of()));
        expectFailure(DungeonYPlanner.FailureReason.EXHAUSTED, () -> DungeonYPlanner.plan(request(1, 9, 1), real, List.of(), new Limits(-64, 320, 128, true, 0)));
        expectFailure(DungeonYPlanner.FailureReason.INVALID_POLICY, () -> DungeonYPlanner.plan(request(1, 8, 100), real, List.of(), new Limits(-64, 320, 128, true, 0)));
        expectFailure(DungeonYPlanner.FailureReason.EMPTY_POOL, () -> DungeonYPlanner.plan(request(1, 2, 100), List.of(), List.of()));
        expectFailure(DungeonYPlanner.FailureReason.DUPLICATE_TEMPLATE, () -> DungeonYPlanner.plan(request(1, 2, 100), List.of(first, first), List.of()));
        Request overflow = new Request(1, "minecraft:overworld", new Vec3i(Integer.MAX_VALUE, 64, Integer.MAX_VALUE), 0, "v2", "s8", 9, 100);
        expectFailure(DungeonYPlanner.FailureReason.COORDINATE_OVERFLOW, () -> DungeonYPlanner.plan(overflow, real, List.of()));
        Limits limits = new Limits(-64, 320, 64, true, 0);
        List<Box> obstacles = List.of(new Box(new Vec3i(5000, 0, 0), new Vec3i(5010, 200, 10)), new Box(new Vec3i(-5000, 0, 0), new Vec3i(-4990, 200, 10)));
        Plan plan = DungeonYPlanner.plan(request(9, 9, 100000), real, obstacles, limits);
        Box blockedShell = corridorBounds(plan.links().get(plan.links().size() - 1)).get(0);
        Plan rerouted = DungeonYPlanner.plan(request(9, 9, 100000), real, List.of(blockedShell), limits);
        verify(rerouted, real, List.of(blockedShell), limits);
        check(!plan.rooms().equals(rerouted.rooms()) || !plan.links().equals(rerouted.links()), "real nine-room rerouting around shifted corridor reservation");
        check(plan.equals(DungeonYPlanner.plan(request(9, 9, 100000), real, List.of(obstacles.get(1), obstacles.get(0)), limits)), "occupied insertion order independent");
        check(!plan.instanceId().equals(DungeonYPlanner.plan(request(9, 9, 100000), real, obstacles, new Limits(-64, 320, 65, true, 0)).instanceId()), "identity includes whole graph policy");
        var executor = java.util.concurrent.Executors.newFixedThreadPool(4);
        try {
            List<java.util.concurrent.Callable<Plan>> jobs = new ArrayList<>();
            for (int index = 0; index < 12; index++) { jobs.add(() -> DungeonYPlanner.plan(request(9, 9, 100000), real, obstacles, limits)); }
            for (var result : executor.invokeAll(jobs)) { check(plan.equals(result.get()), "concurrent repeatable search"); }
        } finally { executor.shutdownNow(); }
        System.out.println("PASS exact width/medium refusal, budget, policy-bound identity, ordering and concurrent replay");
    }
    public static void main(String[] arguments) throws Exception {
        List<Template> real = load();
        check(real.size() == 9, "nine distinct original room sources");
        realNine(real);
        heightAndCollision();
        refusalAndReplay(real);
        Files.writeString(Path.of("results/test-summary.json"), "{\"result\":\"PASS\",\"assertions\":" + assertions + ",\"java\":\"" + System.getProperty("java.version") + "\",\"productionPromotion\":false,\"world\":\"NOT_RUN\"}\n");
        System.out.println("PASS ALL Y PLANNER: " + assertions + " assertions; Java=" + System.getProperty("java.version"));
    }
}
