package net.farmingtales.ftdungeonmobs.generator;

final class ChaosCleanupSlices {
    private ChaosCleanupSlices() {}

    static int count(int[] box) {
        if (box.length != 6 || box[0] > box[3] || box[1] > box[4] || box[2] > box[5]) {
            throw new IllegalArgumentException("Invalid chaos cleanup box");
        }
        int width = (box[3] >> 4) - (box[0] >> 4) + 1;
        int depth = (box[5] >> 4) - (box[2] >> 4) + 1;
        int height = Math.toIntExact(((long) box[4] - box[1]) / 8 + 1);
        return Math.multiplyExact(Math.multiplyExact(width, depth), height);
    }

    static int[] slice(int[] box, int index) {
        if (index < 0 || index >= count(box)) { throw new IllegalArgumentException("Invalid chaos cleanup cursor"); }
        int depth = (box[5] >> 4) - (box[2] >> 4) + 1;
        int height = Math.toIntExact(((long) box[4] - box[1]) / 8 + 1);
        int chunkX = (box[0] >> 4) + index / height / depth;
        int chunkZ = (box[2] >> 4) + index / height % depth;
        int bottom = box[1] + index % height * 8;
        return new int[]{Math.max(box[0], chunkX << 4), bottom, Math.max(box[2], chunkZ << 4),
                Math.min(box[3], (chunkX << 4) + 15), Math.min(box[4], bottom + 7), Math.min(box[5], (chunkZ << 4) + 15)};
    }
}
