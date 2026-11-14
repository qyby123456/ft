import java.util.UUID;
import net.farmingtales.ftboss.util.DoraCombatTempo;

public final class DoraTempoProbe {
    private static int checks;

    public static void main(String[] arguments) {
        UUID player = new UUID(0, 1);
        UUID other = new UUID(0, 2);
        DoraCombatTempo tempo = new DoraCombatTempo();
        require(tempo.hit(player, 0) == 0, "first hit");
        require(tempo.hit(player, 1) == 0, "second hit");
        tempo.miss();
        require(tempo.hit(player, 2) == 0, "miss breaks buildup");
        require(tempo.hit(player, 3) == 0, "two hits after miss");
        require(tempo.hit(player, 4) == 1, "third consecutive hit activates");
        for (int stack = 2; stack <= 6; stack++) require(tempo.hit(player, 4 + stack) == stack, "stack gain");
        require(tempo.hit(player, 11) == 6, "stack cap");
        require(tempo.attackSpeedMultiplier() == 1.75, "attack speed cap");
        require(Math.abs(tempo.cooldownMultiplier() - 0.7) < 1.0E-9, "cooldown cap");
        require(tempo.burstShots(32) == 56, "extra burst attacks");
        require(tempo.spearInterval() == 3, "spear interval cap");
        tempo.miss();
        require(tempo.stacks() == 6, "miss does not immediately remove active buff");
        tempo.tick(130);
        require(tempo.stacks() == 6, "buff alive before deadline");
        tempo.tick(131);
        require(tempo.stacks() == 0, "six-second expiration");
        require(tempo.attackSpeedMultiplier() == 1.0 && tempo.burstShots(32) == 32, "expire restores base stats");
        require(tempo.hit(player, 132) == 0, "new buildup after expiry");
        require(tempo.hit(player, 192) == 0, "three-second buildup timeout");
        require(tempo.hit(player, 193) == 0 && tempo.hit(player, 194) == 1, "new combo after timeout");
        require(tempo.hit(other, 195) == 0, "target change resets buff");
        require(tempo.hit(other, 196) == 0 && tempo.hit(other, 197) == 1, "other player needs fresh combo");
        tempo.reset();
        require(tempo.stacks() == 0 && tempo.spearInterval() == 5, "reset restores spear interval");
        for (int stack = 0; stack <= 6; stack++) {
            tempo.reset();
            for (int hit = 0; hit < (stack == 0 ? 0 : stack + 2); hit++) tempo.hit(player, hit);
            require(tempo.stacks() == stack, "stack setup");
            double accumulator = 0;
            int shots = 0;
            int maxPerTick = 0;
            for (int tick = 0; tick < 120; tick++) {
                accumulator += 4.0 * tempo.attackSpeedMultiplier();
                int thisTick = 0;
                while (accumulator >= 6) {
                    accumulator -= 6;
                    shots++;
                    thisTick++;
                }
                maxPerTick = Math.max(maxPerTick, thisTick);
            }
            require(shots == 80 + stack * 10, "fractional firing rate is not clamped to one shot per tick");
            require(maxPerTick <= 2, "bounded per-tick burst load");
            int reloadTicks = 0;
            int remaining = 60;
            double progress = 0;
            while (remaining > 0) {
                progress += 1.0 / tempo.cooldownMultiplier();
                int elapsed = (int) (progress + 1.0E-9);
                progress -= elapsed;
                remaining -= elapsed;
                reloadTicks++;
            }
            require(reloadTicks == 60 - stack * 3, "reload cooldown reduction");
        }
        System.out.println("PASS: " + checks + " Dora combo, cap, expiry, target, fire-rate and cooldown checks");
    }

    private static void require(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
}
