package net.farmingtales.ftboss.client.render;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** 切换贴地高度缓存：仅读取已加载区块，拒绝悬空和过大落差；2026-10-25新增。 */
public final class ManorTransitionSurface implements ManorTransitionMesh.Surface {
    private final Map<Long, Double> heights = new HashMap<>();
    private final ClientLevel level;
    private final double centerX;
    private final double centerY;
    private final double centerZ;

    public ManorTransitionSurface(ClientLevel level, double centerX, double centerY, double centerZ) {
        this.level = level;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
    }

    @Override
    public double height(double posX, double posZ) {
        int blockX = (int) Math.floor(centerX + posX);
        int blockZ = (int) Math.floor(centerZ + posZ);
        long key = ((long) blockX << 32) ^ (blockZ & 0xffffffffL);
        Double cached = heights.get(key);
        if (cached != null) return cached;
        double sampled = sample(blockX, blockZ);
        if (heights.size() < 1024) heights.put(key, sampled);
        return sampled;
    }

    private double sample(int blockX, int blockZ) {
        int topY = (int) Math.floor(centerY + 0.7);
        for (int blockY = topY; blockY >= topY - 3; blockY--) {
            BlockPos block = new BlockPos(blockX, blockY, blockZ);
            if (!level.hasChunkAt(block)) return Double.NaN;
            var shape = level.getBlockState(block).getCollisionShape(level, block);
            if (shape.isEmpty()) continue;
            double surfaceY = blockY + shape.max(Direction.Axis.Y);
            if (surfaceY > centerY + 0.75 || surfaceY < centerY - 1.25) return Double.NaN;
            return surfaceY - centerY;
        }
        return Double.NaN;
    }
}
