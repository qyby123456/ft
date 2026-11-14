package net.farmingtales.ftboss.util;

import java.util.ArrayList;
import java.util.List;

public final class ZicaiVoidRules {
    public static final String SPIRAL = "faded_void_spiral";
    public static final String FOUNTAIN = "void_fountain_cascade";
    public static final int SPOKES = 24;
    public static final int BEAMS_PER_SPOKE = 8;
    public static final int SPIRAL_WARNING = 16;
    public static final int FOUNTAIN_WARNING = 30;
    public static final int HIT_INTERVAL = 8;
    public static final int MAX_MARKS = SPOKES * BEAMS_PER_SPOKE;
    public static final int MAX_FIELDS = 64;
    public static final int TAIL_TICKS = 26;
    public static final double VIEW_RANGE = 96;

    private ZicaiVoidRules() {}

    public static boolean matches(String id) { return SPIRAL.equals(id) || FOUNTAIN.equals(id); }

    public static List<Mark> layout(boolean fountain, double angle, double distance) {
        List<Mark> marks = new ArrayList<>();
        if (fountain) {
            double base = Math.max(4, Math.min(12, distance));
            marks.add(new Mark(Math.cos(angle) * distance, Math.sin(angle) * distance, 0, FOUNTAIN_WARNING));
            int[] counts = {5, 8, 12};
            for (int wave = 0; wave < counts.length; wave++) {
                double radius = base * (wave + 1) * 0.5;
                for (int point = 0; point < counts[wave]; point++) {
                    double heading = angle + point * Math.PI * 2 / counts[wave];
                    marks.add(new Mark(Math.cos(heading) * radius, Math.sin(heading) * radius,
                            wave * 10, FOUNTAIN_WARNING + wave * 10));
                }
            }
        } else {
            for (int spoke = 0; spoke < SPOKES; spoke++) {
                double heading = angle - spoke * Math.PI * 2 / SPOKES;
                for (int beam = 0; beam < BEAMS_PER_SPOKE; beam++) {
                    double radius = 1.5 + beam * 2;
                    marks.add(new Mark(Math.cos(heading) * radius, Math.sin(heading) * radius,
                            spoke * 3, SPIRAL_WARNING + spoke * 3));
                }
            }
        }
        return List.copyOf(marks);
    }

    public static boolean hits(boolean fountain, int age, int strike) {
        int elapsed = age - strike;
        return elapsed == 0 || fountain && (elapsed == 10 || elapsed == 20);
    }

    public static double radius(boolean fountain) { return fountain ? 1.4 : 0.7; }

    public static double height(boolean fountain) { return fountain ? 6 : 4; }

    public record Mark(double x, double z, int warning, int strike) {}
}
