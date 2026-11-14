/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.Arrays;

public final class XuanlanTrailHistory {
    private final Point[] points = new Point[9];
    private int head;
    private int size;

    public int size() {
        return this.size;
    }

    public Point get(int age) {
        if (age < 0 || age >= this.size) {
            throw new IndexOutOfBoundsException(age);
        }
        return this.points[Math.floorMod(this.head - 1 - age, this.points.length)];
    }

    public void add(double x, double y, double z, long tick) {
        if (!(Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z))) {
            return;
        }
        if (this.size > 0) {
            Point previous = this.get(0);
            double distance = Math.pow(x - previous.x, 2.0) + Math.pow(y - previous.y, 2.0) + Math.pow(z - previous.z, 2.0);
            if (tick < previous.tick || tick - previous.tick > 2L || distance > 36.0) {
                this.clear();
            } else if (tick == previous.tick) {
                return;
            }
        }
        this.points[this.head] = new Point(x, y, z, tick);
        this.head = (this.head + 1) % this.points.length;
        this.size = Math.min(this.size + 1, this.points.length);
    }

    public void clear() {
        Arrays.fill(this.points, null);
        this.head = 0;
        this.size = 0;
    }

    public record Point(double x, double y, double z, long tick) {
    }
}

