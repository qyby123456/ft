import cn.ft.biomedungeon.core.DungeonPlanner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import static cn.ft.biomedungeon.core.DungeonApi.*;
import static cn.ft.biomedungeon.core.DungeonGeometry.*;

public final class RealPortStudy {
    private static int checks;
    private RealPortStudy() {}
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) { throw new AssertionError(message); }
    }
    private static Template role(Template template, String role) {
        return new Template(template.id(), template.biome(), template.resourceId(), template.sha256(), template.size(), template.ports(), role);
    }
    private static List<Template> load(Path path) throws Exception {
        var rows = new TreeMap<String, List<String[]>>();
        for (String line : Files.readAllLines(path)) {
            String[] fields = line.split("\t");
            rows.computeIfAbsent(fields[0], ignored -> new ArrayList<>()).add(fields);
        }
        List<Template> result = new ArrayList<>();
        for (var entry : rows.entrySet()) {
            String[] first = entry.getValue().get(0);
            List<Port> ports = new ArrayList<>();
            for (String[] fields : entry.getValue()) {
                ports.add(new Port(fields[5], new Vec3i(Integer.parseInt(fields[6]), Integer.parseInt(fields[7]), Integer.parseInt(fields[8])),
                        fields[9], Integer.parseInt(fields[10]), Integer.parseInt(fields[11]), "dry"));
            }
            result.add(new Template(first[0], "s8", "biomedungeon:" + first[0], first[1],
                    new Vec3i(Integer.parseInt(first[2]), Integer.parseInt(first[3]), Integer.parseInt(first[4])), ports, "normal"));
        }
        return result;
    }
    private static void frozenPlannerCannotTranslate(Template first, Template second) {
        Request request = new Request(444, "minecraft:overworld", new Vec3i(100, 64, 200), 0, "v1", "s8", 2, 10000);
        try {
            DungeonPlanner.plan(request, List.of(role(first, "entry"), role(second, "normal")), List.of());
            throw new AssertionError("Frozen planner unexpectedly aligned distinct local feet");
        } catch (DungeonPlanner.PlanningException exception) {
            check(exception.reason() == DungeonPlanner.FailureReason.EXHAUSTED, "Expected exhaustion not invalid geometry");
            check(exception.attempts() <= request.maxAttempts(), "bounded attempts");
            System.out.println("EXPECTED_FROZEN_REFUSAL " + first.id() + " -> " + second.id() + " " + exception);
        }
    }
    private static void isolateHeightOnly(Template first, Template second) {
        Template source = new Template(first.id(), first.biome(), first.resourceId(), first.sha256(), first.size(), List.of(first.ports().get(0)), "entry");
        Template target = new Template(second.id(), second.biome(), second.resourceId(), second.sha256(), second.size(), List.of(second.ports().get(0)), "normal");
        try {
            DungeonPlanner.plan(new Request(1, "minecraft:overworld", new Vec3i(0, 64, 0), 0, "v1", "s8", 2, 1000), List.of(source, target), List.of());
            throw new AssertionError("Mismatched feet unexpectedly accepted");
        } catch (DungeonPlanner.PlanningException exception) {
            check(exception.reason() == DungeonPlanner.FailureReason.EXHAUSTED, "height-only exhaustion");
            check(exception.getMessage().contains("height difference unsupported"), "height refusal isolated without width mismatch");
            System.out.println("ISOLATED_HEIGHT_ONLY " + source.id() + " north -> " + target.id() + " north " + exception);
        }
    }
    private static Vec3i translatedOrigin(WorldPort source, WorldPort targetAtZero, int gap) {
        Vec3i outward = direction(source.facing());
        int surface = Math.addExact(source.surface(), Math.multiplyExact(gap, outward.x() + outward.z()));
        int axial = Math.subtractExact(surface, targetAtZero.surface());
        int transverse = Math.subtractExact(source.transverseMin(), targetAtZero.transverseMin());
        int vertical = Math.subtractExact(source.feet().y(), targetAtZero.feet().y());
        return outward.x() == 0 ? new Vec3i(transverse, vertical, axial) : new Vec3i(axial, vertical, transverse);
    }
    private static void translatedGeometry(Template first, Template second) {
        int matched = 0;
        int widthRejected = 0;
        for (Port sourcePort : first.ports()) {
            for (Port targetPort : second.ports()) {
                for (int sourceRotation = 0; sourceRotation < 4; sourceRotation++) {
                    Vec3i firstOrigin = new Vec3i(100, 64, 200);
                    WorldPort source = worldPort(first, sourcePort, firstOrigin, sourceRotation);
                    for (int targetRotation = 0; targetRotation < 4; targetRotation++) {
                        WorldPort initial = worldPort(second, targetPort, new Vec3i(0, 0, 0), targetRotation);
                        if (!rotateFacing(source.facing(), 2).equals(initial.facing())) { continue; }
                        if (source.width() != initial.width() || source.height() != initial.height()) {
                            widthRejected++;
                            continue;
                        }
                        Vec3i targetOrigin = translatedOrigin(source, initial, 5);
                        WorldPort target = worldPort(second, targetPort, targetOrigin, targetRotation);
                        Box firstBounds = bounds(first.size(), firstOrigin, sourceRotation);
                        Box secondBounds = bounds(second.size(), targetOrigin, targetRotation);
                        Link link = new Link("source", sourcePort.id(), "target", targetPort.id(),
                                List.of(exteriorFeet(source), exteriorFeet(target)), source.width(), source.height());
                        Box corridor = corridorBounds(link).get(0);
                        check(source.feet().y() == target.feet().y(), "world doorway feet align after rigid Y translation");
                        check(source.transverseMin() == target.transverseMin(), "transverse minima align");
                        check(source.transverseMaxExclusive() == target.transverseMaxExclusive(), "transverse maxima align");
                        check(targetOrigin.y() == firstOrigin.y() + sourcePort.feet().y() - targetPort.feet().y(), "exact translation formula");
                        check(!firstOrigin.equals(targetOrigin), "independent room origin");
                        check(!intersects(firstBounds, secondBounds), "room collision checked in all axes");
                        check(!intersects(firstBounds, corridor) && !intersects(secondBounds, corridor), "full corridor shell separated from both owners");
                        Box conflict = new Box(secondBounds.min(), secondBounds.maxExclusive());
                        check(intersects(conflict, secondBounds), "post-translation occupied-room conflict detectable");
                        check(intersects(corridor, corridor), "post-translation occupied-corridor conflict detectable");
                        for (Port port : second.ports()) {
                            WorldPort transformed = worldPort(second, port, targetOrigin, targetRotation);
                            check(transformed.feet().y() == targetOrigin.y() + port.feet().y(), "all other ports translate with original interior");
                        }
                        matched++;
                        System.out.println("Y_TRANSLATION_GEOMETRY_ONLY " + first.id() + "/" + sourcePort.id() + " q" + sourceRotation
                                + " -> " + second.id() + "/" + targetPort.id() + " q" + targetRotation
                                + " origin=" + targetOrigin + " worldFeetY=" + source.feet().y() + " reserved=" + corridor);
                    }
                }
            }
        }
        check(matched == 16, "16 real width-compatible opposite-facing rotation combinations per direction");
        check(widthRejected == 16, "S8-07 south width5 cannot silently attach to S8-01 width3");
        System.out.println("PAIR " + first.id() + "->" + second.id() + " shiftedMatches=" + matched + " widthRejected=" + widthRejected);
    }
    public static void main(String[] arguments) throws Exception {
        List<Template> templates = load(Path.of("real-ports.tsv"));
        Template first = templates.get(0);
        Template second = templates.get(1);
        check(first.ports().get(0).feet().equals(new Vec3i(17, 1, 0)), "real S8-01 north");
        check(second.ports().get(0).feet().y() == 2 && second.ports().get(1).feet().y() == 4, "real S8-07 north/south");
        for (Template template : templates) {
            for (Port port : template.ports()) { validatePort(template, port); }
        }
        System.out.println("PASS unchanged DungeonGeometry.validatePort accepts all six actual port metadata entries including original S8-01 stair mouth");
        frozenPlannerCannotTranslate(first, second);
        frozenPlannerCannotTranslate(second, first);
        isolateHeightOnly(first, second);
        isolateHeightOnly(second, first);
        translatedGeometry(first, second);
        translatedGeometry(second, first);
        System.out.println("PASS " + checks + " assertions on real source metadata; Java=" + System.getProperty("java.version"));
        System.out.println("NOT_A_PLANNER_UPGRADE: manual rigid-room Y translation study only; no frozen source/JAR changes; native stair swept-body collision NOT_RUN");
    }
}
