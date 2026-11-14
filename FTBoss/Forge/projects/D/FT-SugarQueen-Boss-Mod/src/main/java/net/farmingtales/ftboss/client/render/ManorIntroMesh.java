package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.util.ManorRitualGeometry;

public final class ManorIntroMesh {
    @FunctionalInterface
    public interface Sink { void vertex(double posX, double posY, double posZ, int color, float alpha); }
    private ManorIntroMesh() {}

    public static void emit(double elapsedMillis, boolean reduced, Sink sink) {
        ManorRitualGeometry.emit(elapsedMillis, reduced, sink::vertex);
    }
}
