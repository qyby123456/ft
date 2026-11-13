package cn.farmingtales.qysevent;

import net.minecraft.world.level.Level;

public final class SeasonIntegrationVerifier {
    public static void verify() {
        CountingLoader loader = new CountingLoader();
        SeasonIntegration missing = new SeasonIntegration(loader, "missing.Helper", "missing.State");
        for (int iteration = 0; iteration < 10000; iteration++) {
            require(missing.atDayOffset(null, iteration) == null, "missing mod must use fallback");
        }
        require(loader.lookups == 1, "missing mod repeatedly searched");
        SeasonIntegration integration = new SeasonIntegration(SeasonIntegrationVerifier.class.getClassLoader(),
                Helper.class.getName(), State.class.getName());
        int comparisons = 0;
        for (int cycle : new int[]{0, 24, 97, 24000, 1728000}) {
            Helper.cycle = cycle;
            for (int duration : new int[]{0, 1, 24000}) {
                Helper.duration = duration;
                for (long ticks : new long[]{-1728001L, -1L, 0L, 23L, 1727999L, Long.MAX_VALUE / 2}) {
                    Helper.ticks = ticks;
                    for (int offset = -40; offset <= 40; offset++) {
                        int safeCycle = Math.max(24, cycle);
                        int safeDuration = Math.max(1, duration);
                        int normalized = (int) Math.floorMod(ticks + (long) offset * safeDuration, (long) safeCycle);
                        int index = Math.min(23, (int) ((long) normalized * 24L / safeCycle));
                        int start = (int) ((long) index * safeCycle / 24L);
                        int end = (int) ((long) (index + 1) * safeCycle / 24L);
                        SolarTerms.TermInfo expected = new SolarTerms.TermInfo(index, SolarTerms.NAMES[index],
                                Math.max(1, (normalized - start) / safeDuration + 1),
                                Math.max(1, (end - start + safeDuration - 1) / safeDuration));
                        require(expected.equals(integration.atDayOffset(null, offset)), "season mapping changed");
                        comparisons++;
                    }
                }
            }
        }
        Helper.throwing = true;
        require(integration.atDayOffset(null, 0) == null, "throwing integration must use fallback");
        Helper.throwing = false;
        require(integration.atDayOffset(null, 0) != null, "integration must retry live state after errors");
        SeasonIntegration malformed = new SeasonIntegration(SeasonIntegrationVerifier.class.getClassLoader(),
                String.class.getName(), State.class.getName());
        require(malformed.atDayOffset(null, 0) == null, "malformed API must use fallback");
        System.out.println("Season integration verified: " + comparisons + " calendar comparisons; absent, malformed, throwing and recovery cases.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    public static final class Helper {
        static int cycle;
        static int duration;
        static long ticks;
        static boolean throwing;
        public static State getSeasonState(Level level) {
            if (throwing) throw new IllegalStateException("fixture");
            return new State();
        }
    }

    public static final class State {
        public int getCycleDuration() { return Helper.cycle; }
        public int getDayDuration() { return Helper.duration; }
        public long getSeasonCycleTicks() { return Helper.ticks; }
    }

    private static final class CountingLoader extends ClassLoader {
        int lookups;
        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.startsWith("missing.")) {
                lookups++;
                throw new ClassNotFoundException(name);
            }
            return super.loadClass(name, resolve);
        }
    }
}
