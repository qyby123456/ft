import java.util.UUID;
import net.farmingtales.ftboss.util.DoraSniperCycle;
import net.farmingtales.ftboss.util.DoraSniperCycle.Step;

public final class DoraSniperProbe {
    private static int checks;

    public static void main(String[] arguments) {
        UUID player = new UUID(0, 1);
        UUID other = new UUID(0, 2);
        DoraSniperCycle cycle = new DoraSniperCycle();
        require(!cycle.busy() && !cycle.charging() && !cycle.locked(), "idle flags");
        require(cycle.tick(player, true) == Step.IDLE, "idle has no attacks");
        require(!cycle.begin(null), "null target rejected");
        for (int round = 0; round < 100; round++) {
            int recovery = round % 2 == 0 ? 12 : 6;
            int duration = 50 + recovery;
            require(cycle.begin(player, round % 2 != 0), "begin round");
            require(!cycle.begin(other), "cannot replace an active target");
            int shots = 0;
            int lockEvents = 0;
            for (int tick = 1; tick <= duration; tick++) {
                Step step = cycle.tick(player, true);
                Step expected = tick < 35 ? Step.TRACKING : tick == 35 ? Step.LOCK : tick < 50 ? Step.LOCKED
                        : tick == 50 ? Step.FIRE : tick < duration ? Step.RECOVERY : Step.FINISHED;
                require(step == expected, "phase at tick " + tick);
                require(cycle.busy() == (tick < duration), "busy gates all phases");
                require(cycle.charging() == (tick < 50), "charge flag");
                require(cycle.locked() == (tick >= 35 && tick < duration), "lock flag");
                if (step == Step.FIRE) shots++;
                if (step == Step.LOCK) lockEvents++;
            }
            require(shots == 1 && lockEvents == 1, "one shot and one lock per cycle");
            require(cycle.tick(player, true) == Step.IDLE, "finished cannot fire again");
        }
        for (int cancelAt = 0; cancelAt < 62; cancelAt++) {
            for (boolean changedTarget : new boolean[]{false, true}) {
                require(cycle.begin(player), "begin cancellation test");
                for (int tick = 0; tick < cancelAt; tick++) cycle.tick(player, true);
                require(cycle.tick(changedTarget ? other : player, changedTarget) == Step.CANCELLED, "cancel any phase");
                require(!cycle.busy() && !cycle.locked() && !cycle.charging(), "cancel clears state");
                require(cycle.tick(player, true) == Step.IDLE, "cancelled shot never resumes");
            }
        }
        require(cycle.begin(player), "explicit reset begin");
        cycle.reset();
        require(cycle.tick(player, true) == Step.IDLE, "explicit reset");
        require(DoraSniperCycle.CHARGE_TICKS - DoraSniperCycle.TRACK_TICKS == 15, "fixed dodge window");
        require(DoraSniperCycle.RADIUS == 1.44, "doubled beam collision and telegraph radius");
        require(DoraSniperCycle.cueTicks(false, false) == 50 && DoraSniperCycle.cueTicks(false, true) == 50,
                "normal and accelerated charge cues both last 2.5 seconds");
        require(DoraSniperCycle.cueTicks(true, false) == 12 && DoraSniperCycle.cueTicks(true, true) == 6,
                "recovery cues retain difficulty scaling");
        require(DoraSniperCycle.inRange(0) && DoraSniperCycle.inRange(36 * 36), "inclusive range");
        for (double invalid : new double[]{-1, 36 * 36 + 0.01, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}) {
            require(!DoraSniperCycle.inRange(invalid), "invalid range");
        }
        System.out.println("PASS: " + checks + " Dora sniper phase, cancellation, lock and range assertions");
    }

    private static void require(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
}
