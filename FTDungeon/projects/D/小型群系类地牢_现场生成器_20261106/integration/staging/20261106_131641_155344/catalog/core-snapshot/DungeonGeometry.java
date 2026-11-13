package cn.ft.biomedungeon.core;

import java.util.ArrayList;
import java.util.List;
import static cn.ft.biomedungeon.core.DungeonApi.*;

public final class DungeonGeometry {
    private DungeonGeometry() {}
    public record WorldPort(Vec3i feet, String facing, int transverseMin, int transverseMaxExclusive,
                            int surface, int width, int height, String medium) {}

    public static Vec3i rotate(Vec3i point, int quarterTurns) {
        return switch (Math.floorMod(quarterTurns, 4)) {
            case 0 -> point;
            case 1 -> new Vec3i(Math.negateExact(point.z()), point.y(), point.x());
            case 2 -> new Vec3i(Math.negateExact(point.x()), point.y(), Math.negateExact(point.z()));
            default -> new Vec3i(point.z(), point.y(), Math.negateExact(point.x()));
        };
    }
    public static Vec3i add(Vec3i left, Vec3i right) {
        return new Vec3i(Math.addExact(left.x(), right.x()), Math.addExact(left.y(), right.y()),
                Math.addExact(left.z(), right.z()));
    }
    public static String rotateFacing(String facing, int quarterTurns) {
        List<String> directions = List.of("north", "east", "south", "west");
        int index = directions.indexOf(facing);
        if (index < 0) {
            throw new IllegalArgumentException("Invalid facing: " + facing);
        }
        return directions.get(Math.floorMod(index + quarterTurns, 4));
    }
    public static Vec3i direction(String facing) {
        return switch (facing) {
            case "north" -> new Vec3i(0, 0, -1);
            case "east" -> new Vec3i(1, 0, 0);
            case "south" -> new Vec3i(0, 0, 1);
            case "west" -> new Vec3i(-1, 0, 0);
            default -> throw new IllegalArgumentException("Invalid facing: " + facing);
        };
    }
    public static Box bounds(Vec3i size, Vec3i origin, int quarterTurns) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (int localX : new int[] {0, size.x()}) {
            for (int localY : new int[] {0, size.y()}) {
                for (int localZ : new int[] {0, size.z()}) {
                    Vec3i corner = add(origin, rotate(new Vec3i(localX, localY, localZ), quarterTurns));
                    minX = Math.min(minX, corner.x());
                    minY = Math.min(minY, corner.y());
                    minZ = Math.min(minZ, corner.z());
                    maxX = Math.max(maxX, corner.x());
                    maxY = Math.max(maxY, corner.y());
                    maxZ = Math.max(maxZ, corner.z());
                }
            }
        }
        return new Box(new Vec3i(minX, minY, minZ), new Vec3i(maxX, maxY, maxZ));
    }
    public static boolean intersects(Box left, Box right) {
        return left.min().x() < right.maxExclusive().x() && right.min().x() < left.maxExclusive().x()
                && left.min().y() < right.maxExclusive().y() && right.min().y() < left.maxExclusive().y()
                && left.min().z() < right.maxExclusive().z() && right.min().z() < left.maxExclusive().z();
    }
    public static void validatePort(Template template, Port port) {
        Vec3i feet = port.feet();
        Vec3i size = template.size();
        boolean alongX = port.facing().equals("north") || port.facing().equals("south");
        int transverse = alongX ? feet.x() : feet.z();
        int limit = alongX ? size.x() : size.z();
        int lower = (port.width() - 1) / 2;
        boolean boundary = switch (port.facing()) {
            case "north" -> feet.z() == 0;
            case "east" -> feet.x() == size.x() - 1;
            case "south" -> feet.z() == size.z() - 1;
            default -> feet.x() == 0;
        };
        if (!boundary || transverse - lower < 0 || (long) transverse - lower + port.width() > limit
                || feet.y() < 1 || (long) feet.y() + port.height() >= size.y()) {
            throw new IllegalArgumentException("Port is not a boundary opening with floor/ceiling: "
                    + template.id() + "/" + port.id());
        }
    }
    public static WorldPort worldPort(Template template, Port port, Vec3i origin, int quarterTurns) {
        validatePort(template, port);
        boolean alongX = port.facing().equals("north") || port.facing().equals("south");
        int transverse = (alongX ? port.feet().x() : port.feet().z()) - (port.width() - 1) / 2;
        int surface = switch (port.facing()) {
            case "north", "west" -> 0;
            case "east" -> template.size().x();
            default -> template.size().z();
        };
        Vec3i localStart = alongX ? new Vec3i(transverse, port.feet().y(), surface)
                : new Vec3i(surface, port.feet().y(), transverse);
        Vec3i localEnd = alongX ? new Vec3i(transverse + port.width(), port.feet().y(), surface)
                : new Vec3i(surface, port.feet().y(), transverse + port.width());
        Vec3i start = add(origin, rotate(localStart, quarterTurns));
        Vec3i end = add(origin, rotate(localEnd, quarterTurns));
        String facing = rotateFacing(port.facing(), quarterTurns);
        boolean worldAlongX = facing.equals("north") || facing.equals("south");
        int min = worldAlongX ? Math.min(start.x(), end.x()) : Math.min(start.z(), end.z());
        int max = worldAlongX ? Math.max(start.x(), end.x()) : Math.max(start.z(), end.z());
        return new WorldPort(add(origin, rotate(port.feet(), quarterTurns)), facing, min, max,
                worldAlongX ? start.z() : start.x(), port.width(), port.height(), port.medium());
    }
    public static Vec3i exteriorFeet(WorldPort port) {
        int transverse = Math.addExact(port.transverseMin(), (port.width() - 1) / 2);
        int axial = switch (port.facing()) {
            case "north", "west" -> Math.subtractExact(port.surface(), 1);
            default -> port.surface();
        };
        return port.facing().equals("north") || port.facing().equals("south")
                ? new Vec3i(transverse, port.feet().y(), axial)
                : new Vec3i(axial, port.feet().y(), transverse);
    }
    public static List<Box> corridorBounds(Link link) {
        List<Box> boxes = new ArrayList<>();
        for (int index = 1; index < link.waypoints().size(); index++) {
            Vec3i start = link.waypoints().get(index - 1);
            Vec3i end = link.waypoints().get(index);
            if (start.y() != end.y() || (start.x() != end.x() && start.z() != end.z()) || start.equals(end)) {
                throw new IllegalArgumentException("Only nonzero axis-aligned level corridor segments supported");
            }
            boolean alongX = start.x() != end.x();
            int lower = (link.width() - 1) / 2;
            int transverse = alongX ? start.z() : start.x();
            int transverseMin = Math.subtractExact(transverse, lower + 1);
            int transverseMax = Math.addExact(transverseMin, link.width() + 2);
            int axialMin = alongX ? Math.min(start.x(), end.x()) : Math.min(start.z(), end.z());
            int axialMax = Math.addExact(alongX ? Math.max(start.x(), end.x()) : Math.max(start.z(), end.z()), 1);
            int minY = Math.subtractExact(start.y(), 1);
            int maxY = Math.addExact(start.y(), link.height() + 1);
            boxes.add(alongX
                    ? new Box(new Vec3i(axialMin, minY, transverseMin), new Vec3i(axialMax, maxY, transverseMax))
                    : new Box(new Vec3i(transverseMin, minY, axialMin), new Vec3i(transverseMax, maxY, axialMax)));
        }
        return List.copyOf(boxes);
    }
}
