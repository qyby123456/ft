package net.farmingtales.ftboss.client.render;

import net.minecraft.client.Minecraft;

public final class FirstPersonSpellVisibility {
    private FirstPersonSpellVisibility() {}

    public static boolean firstPerson() {
        return !Minecraft.getInstance().gameRenderer.getMainCamera().isDetached();
    }

    public static boolean preserveThirdPerson(boolean protectedView) {
        return !firstPerson() && protectedView;
    }

    public static double cameraFactor(double opacity, boolean firstPerson) {
        return firstPerson ? 1 : opacity;
    }

    public static double cameraDistance(double distance, boolean firstPerson) {
        return firstPerson ? Double.POSITIVE_INFINITY : distance;
    }

    public static float geometryOpacity(double[][] points) {
        for (double[] point : points) {
            for (double coordinate : point) {
                if (!Double.isFinite(coordinate)) return 0;
            }
        }
        return 1;
    }

    public static double smooth(double value) {
        double progress = Math.max(0, Math.min(1, value));
        return progress * progress * (3 - 2 * progress);
    }

    public static double nearOpacity(double distance) {
        return 1;
    }

    public static double featherOpacity(double[] point, double[] camera, double[] look, boolean firstPerson) {
        if (firstPerson) return 1;
        double horizontal = point[0] - camera[0];
        double vertical = point[1] - camera[1];
        double depth = point[2] - camera[2];
        double distance = Math.sqrt(horizontal * horizontal + vertical * vertical + depth * depth);
        return smooth((distance - 0.42) / 0.50);
    }

    public static double forgeOpacity(double[] point, double[] camera, double[] forward, boolean firstPerson, boolean attack) {
        if (firstPerson) return 1;
        double horizontal = point[0] - camera[0];
        double vertical = point[1] - camera[1];
        double depth = point[2] - camera[2];
        double distance = Math.sqrt(horizontal * horizontal + vertical * vertical + depth * depth);
        return Math.max(0, Math.min(1, (distance - 0.55) / 0.35));
    }

    public static boolean intersectsNearViewRay(double[][] points, double[] camera, double[] look) {
        if (firstPerson()) return false;
        return intersectsTriangle(points[0], points[1], points[2], camera, look)
                || intersectsTriangle(points[0], points[2], points[3], camera, look);
    }

    private static boolean intersectsTriangle(double[] first, double[] second, double[] third, double[] origin, double[] direction) {
        double edgeFirstX = second[0] - first[0], edgeFirstY = second[1] - first[1], edgeFirstZ = second[2] - first[2];
        double edgeSecondX = third[0] - first[0], edgeSecondY = third[1] - first[1], edgeSecondZ = third[2] - first[2];
        double perpendicularX = direction[1] * edgeSecondZ - direction[2] * edgeSecondY;
        double perpendicularY = direction[2] * edgeSecondX - direction[0] * edgeSecondZ;
        double perpendicularZ = direction[0] * edgeSecondY - direction[1] * edgeSecondX;
        double determinant = edgeFirstX * perpendicularX + edgeFirstY * perpendicularY + edgeFirstZ * perpendicularZ;
        if (Math.abs(determinant) < 0.00000001) return false;
        double relativeX = origin[0] - first[0], relativeY = origin[1] - first[1], relativeZ = origin[2] - first[2];
        double horizontal = (relativeX * perpendicularX + relativeY * perpendicularY + relativeZ * perpendicularZ) / determinant;
        if (horizontal < 0 || horizontal > 1) return false;
        double otherX = relativeY * edgeFirstZ - relativeZ * edgeFirstY;
        double otherY = relativeZ * edgeFirstX - relativeX * edgeFirstZ;
        double otherZ = relativeX * edgeFirstY - relativeY * edgeFirstX;
        double vertical = (direction[0] * otherX + direction[1] * otherY + direction[2] * otherZ) / determinant;
        if (vertical < 0 || horizontal + vertical > 1) return false;
        double distance = (edgeSecondX * otherX + edgeSecondY * otherY + edgeSecondZ * otherZ) / determinant;
        return distance > 0 && distance <= 2.4;
    }
}
