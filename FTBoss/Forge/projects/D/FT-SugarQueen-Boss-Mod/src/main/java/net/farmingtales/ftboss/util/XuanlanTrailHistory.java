package net.farmingtales.ftboss.util;

public final class XuanlanTrailHistory {
    public record Point(double x, double y, double z, long tick) {}
    private final Point[] points = new Point[XuanlanMotionRules.TRAIL_POINTS];
    private int head;
    private int size;

    public int size() { return size; }

    public Point get(int age) {
        if (age < 0 || age >= size) throw new IndexOutOfBoundsException(age);
        return points[Math.floorMod(head - 1 - age, points.length)];
    }

    public void add(double x, double y, double z, long tick) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) return;
        if (size > 0) {
            Point previous = get(0);
            double distance = Math.pow(x - previous.x, 2) + Math.pow(y - previous.y, 2) + Math.pow(z - previous.z, 2);
            if (tick < previous.tick || tick - previous.tick > 2 || distance > 36) clear();
            else if (tick == previous.tick) return;
        }
        points[head] = new Point(x, y, z, tick);
        head = (head + 1) % points.length;
        size = Math.min(size + 1, points.length);
    }

    public void clear() {
        java.util.Arrays.fill(points, null);
        head = 0;
        size = 0;
    }
}
