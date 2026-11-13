package net.farmingtales.ftdungeonmobs;

public final class DungeonGateMotion {
    public static final int DURATION = 40;
    public static final int SINK_UNITS = 49;
    public static final float FLOOR = 3.0F / 16.0F;
    private int startProgress;
    private boolean targetOpen;
    private long startTime;

    public DungeonGateMotion(int progress, boolean targetOpen, long time) {
        reset(progress, targetOpen, time);
    }

    public void reset(int progress, boolean opening, long time) {
        startProgress = Math.max(0, Math.min(DURATION, progress));
        targetOpen = opening;
        startTime = time;
    }

    public boolean targetOpen() {
        return targetOpen;
    }

    public int storedProgress() {
        return startProgress;
    }

    public int progress(long time) {
        return Math.round(progress((double) time));
    }

    public float progress(double time) {
        double elapsed = Math.max(0, Math.min(DURATION, time - startTime));
        return (float) (targetOpen ? Math.min(DURATION, startProgress + elapsed)
                : Math.max(0, startProgress - elapsed));
    }

    public boolean active() {
        return startProgress != (targetOpen ? DURATION : 0);
    }

    public boolean finished(long time) {
        return progress(time) == (targetOpen ? DURATION : 0);
    }

    public void retarget(boolean opening, long time) {
        reset(progress(time), opening, time);
    }

    public static float[] clipFloor(float[] vertices) {
        if (vertices.length != 20) {
            throw new IllegalArgumentException("A gate quad requires four XYZUV vertices");
        }
        float minimum = Float.POSITIVE_INFINITY;
        float maximum = Float.NEGATIVE_INFINITY;
        for (int vertex = 0; vertex < 4; vertex++) {
            minimum = Math.min(minimum, vertices[vertex * 5 + 1]);
            maximum = Math.max(maximum, vertices[vertex * 5 + 1]);
        }
        if (maximum <= FLOOR) {
            return null;
        }
        if (minimum >= FLOOR) {
            return vertices;
        }
        float[] clipped = vertices.clone();
        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * 5;
            if (vertices[offset + 1] >= FLOOR) {
                continue;
            }
            for (int opposite = 0; opposite < 4; opposite++) {
                int other = opposite * 5;
                if (vertices[other + 1] > FLOOR && vertices[offset] == vertices[other]
                        && vertices[offset + 2] == vertices[other + 2]) {
                    float fraction = (FLOOR - vertices[offset + 1]) / (vertices[other + 1] - vertices[offset + 1]);
                    clipped[offset + 1] = FLOOR;
                    clipped[offset + 3] = vertices[offset + 3] + fraction * (vertices[other + 3] - vertices[offset + 3]);
                    clipped[offset + 4] = vertices[offset + 4] + fraction * (vertices[other + 4] - vertices[offset + 4]);
                    break;
                }
            }
        }
        return clipped;
    }
}
