package net.farmingtales.ftboss.util;

public final class ManorVictoryGeometry {
    public static final int DURATION = 120;
    @FunctionalInterface
    public interface Sink { void vertex(double horizontal, double height, double depth, int color, float alpha); }
    private ManorVictoryGeometry() {}

    public static void draw(double age, boolean reduced, Sink sink) {
        ManorCeremonyGeometry.victory(age, reduced, sink::vertex);
    }
}
