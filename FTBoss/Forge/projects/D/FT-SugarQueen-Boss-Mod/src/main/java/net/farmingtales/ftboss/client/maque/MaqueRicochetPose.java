package net.farmingtales.ftboss.client.maque;

public final class MaqueRicochetPose {
    private MaqueRicochetPose() {}

    public static double[] transform(double[] point, double[] origin, double[] center, double[] velocity) {
        double length = Math.sqrt(velocity[0] * velocity[0] + velocity[1] * velocity[1] + velocity[2] * velocity[2]);
        double[] forward = length > 1e-8 ? new double[]{velocity[0] / length, velocity[1] / length, velocity[2] / length} : new double[]{0, 0, 1};
        double horizontal = Math.hypot(forward[0], forward[2]);
        double[] right = horizontal > 1e-8 ? new double[]{forward[2] / horizontal, 0, -forward[0] / horizontal} : new double[]{1, 0, 0};
        double[] up = {forward[1] * right[2] - forward[2] * right[1], forward[2] * right[0] - forward[0] * right[2], forward[0] * right[1] - forward[1] * right[0]};
        double[] result = new double[3];
        for (int axis = 0; axis < 3; axis++) result[axis] = center[axis] + (point[0] - origin[0]) * right[axis]
                + (point[1] - origin[1]) * up[axis] + (point[2] - origin[2]) * forward[axis];
        return result;
    }

    public static double trailAlpha(int segment, int length) { return length <= 0 ? 0 : 0.5 * Math.pow(1 - (double) segment / length, 2); }

    public static double[][] trailQuad(double[] head, double[] tail, int segment, int length, int plane) {
        double width = 0.07 * Math.max(0, 1 - (double) segment / length);
        double tailWidth = 0.07 * Math.max(0, 1 - (double) (segment + 1) / length);
        double[][] quad = {head.clone(), head.clone(), tail.clone(), tail.clone()};
        quad[0][plane] += width;
        quad[1][plane] -= width;
        quad[2][plane] -= tailWidth;
        quad[3][plane] += tailWidth;
        return quad;
    }
}
