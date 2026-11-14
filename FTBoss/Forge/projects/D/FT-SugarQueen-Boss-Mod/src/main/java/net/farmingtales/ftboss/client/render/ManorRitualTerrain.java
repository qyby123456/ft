package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.util.ManorCeremonyModel.Sink;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;

final class ManorRitualTerrain implements Sink {
    private final Map<Long, Double> heights = new HashMap<>();
    private final double[] posX = new double[4];
    private final double[] posY = new double[4];
    private final double[] posZ = new double[4];
    private final int[] colors = new int[4];
    private final float[] opacity = new float[4];
    private ClientLevel level;
    private Vec3 origin;
    private long epoch = Long.MIN_VALUE;
    private int count;
    private Sink output;

    void begin(ClientLevel nextLevel, Vec3 nextOrigin, long millis, Sink sink) {
        long nextEpoch = millis / 1000;
        if (level != nextLevel || origin == null || !origin.equals(nextOrigin) || epoch != nextEpoch) heights.clear();
        level = nextLevel;
        origin = nextOrigin;
        epoch = nextEpoch;
        output = sink;
        count = 0;
    }

    @Override
    public void vertex(double nextX, double nextY, double nextZ, int color, float alpha) {
        posX[count] = nextX;
        posY[count] = nextY;
        posZ[count] = nextZ;
        colors[count] = color;
        opacity[count] = alpha;
        if (++count != 4) return;
        count = 0;
        double highestVertex = Math.max(Math.max(posY[0], posY[1]), Math.max(posY[2], posY[3]));
        double offset = 0;
        if (highestVertex <= 0.52) {
            double minimum = Double.POSITIVE_INFINITY, maximum = Double.NEGATIVE_INFINITY;
            double centerX = 0, centerZ = 0;
            for (int corner = 0; corner < 4; corner++) {
                double height = ground(posX[corner], posZ[corner]);
                if (!Double.isFinite(height)) return;
                minimum = Math.min(minimum, height);
                maximum = Math.max(maximum, height);
                centerX += posX[corner] * 0.25;
                centerZ += posZ[corner] * 0.25;
            }
            double middle = ground(centerX, centerZ);
            if (!Double.isFinite(middle)) return;
            minimum = Math.min(minimum, middle);
            maximum = Math.max(maximum, middle);
            if (maximum - minimum > 0.55) return;
            offset = maximum;
        }
        for (int corner = 0; corner < 4; corner++) {
            output.vertex(posX[corner], posY[corner] + offset, posZ[corner], colors[corner], opacity[corner]);
        }
    }

    private double ground(double localX, double localZ) {
        BlockPos column = BlockPos.containing(origin.x + localX, origin.y, origin.z + localZ);
        long key = BlockPos.asLong(column.getX(), 0, column.getZ());
        return heights.computeIfAbsent(key, ignored -> surface(column));
    }

    private double surface(BlockPos column) {
        if (!level.hasChunkAt(column)) return Double.NaN;
        for (int offset = 1; offset >= -2; offset--) {
            BlockPos block = column.offset(0, offset, 0);
            if (!level.getFluidState(block).isEmpty()) return Double.NaN;
            VoxelShape shape = level.getBlockState(block).getCollisionShape(level, block);
            if (shape.isEmpty()) continue;
            double height = block.getY() + shape.max(Direction.Axis.Y) - origin.y;
            if (Math.abs(height) > 1.25) return Double.NaN;
            return height;
        }
        return Double.NaN;
    }
}
