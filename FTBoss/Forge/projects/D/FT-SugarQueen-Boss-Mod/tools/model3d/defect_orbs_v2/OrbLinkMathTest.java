import net.farmingtales.ftboss.util.OrbLinkMath;

public final class OrbLinkMathTest {
    private static void close(double actual, double expected) {
        if (Math.abs(actual - expected) > 0.000001) throw new AssertionError(actual + " != " + expected);
    }

    public static void main(String[] arguments) {
        close(OrbLinkMath.progress(-1, 10), 0);
        close(OrbLinkMath.progress(50, 10), 1);
        close(OrbLinkMath.progress(0, 0), 0);
        close(OrbLinkMath.arc(2, 12, 0, 0.6), 2);
        close(OrbLinkMath.arc(2, 12, 1, 0.6), 12);
        close(OrbLinkMath.arc(2, 12, 0.5, 0.6), 7.6);
        close(OrbLinkMath.smooth(-2), 0);
        close(OrbLinkMath.smooth(2), 1);
        for (String type : new String[]{"glass", "lightning", "frost", "ion", "dark"}) {
            int ticks = OrbLinkMath.releaseTicks(type);
            close(OrbLinkMath.progress(ticks, ticks), 1);
            double previous = 0;
            for (int tick = 0; tick <= ticks; tick++) {
                double position = OrbLinkMath.arc(0, 10, OrbLinkMath.progress(tick, ticks), 0);
                if (position < previous || position > 10) throw new AssertionError(type + " trajectory overshoot");
                previous = position;
            }
        }
        assert OrbLinkMath.releaseTicks("glass") == 11;
        assert OrbLinkMath.releaseTicks("lightning") == 9;
        assert OrbLinkMath.releaseTicks("frost") == 6;
        assert OrbLinkMath.releaseTicks("ion") == 6;
        assert OrbLinkMath.releaseTicks("dark") == 16;
        boolean rejected = false;
        try { OrbLinkMath.releaseTicks("missing"); } catch (IllegalArgumentException expected) { rejected = true; }
        assert rejected;
        System.out.println("OrbLinkMath: interpolation, clamping and five release ticks passed.");
    }
}
