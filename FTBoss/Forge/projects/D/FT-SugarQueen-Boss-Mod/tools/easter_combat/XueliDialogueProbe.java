import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.farmingtales.ftboss.util.XueliDialogueState;

public final class XueliDialogueProbe {
    private static int checks;

    public static void main(String[] arguments) {
        XueliDialogueState state = new XueliDialogueState();
        require(!state.randomReady(0), "opening has priority");
        equal(state.nextStage(0, 1.0f), XueliDialogueState.OPENING, "opening");
        equal(state.nextStage(60, 0.7501f), null, "above 75 percent");
        equal(state.nextStage(61, 0.75f), XueliDialogueState.HIGH, "75 percent boundary");
        equal(state.nextStage(121, 1.0f), null, "healing does not repeat opening");
        equal(state.nextStage(122, 0.75f), null, "75 percent only once");
        equal(state.nextStage(123, 0.5001f), null, "above half health");
        equal(state.nextStage(124, 0.50f), XueliDialogueState.HALF_HEALTH, "half health boundary");
        equal(state.nextStage(300, 0.25f), null, "lower health does not repeat half health");

        XueliDialogueState restored = new XueliDialogueState();
        restored.restore(state.progress(), 1000);
        equal(restored.nextStage(1000, 0.1f), null, "announced stages survive reload");
        require(!restored.randomReady(1399) && restored.randomReady(1400), "reload delays ambient lines");
        restored.restore(0, 2000);
        equal(restored.nextStage(2000, 1.0f), XueliDialogueState.OPENING, "new variant resets dialogue");

        state = new XueliDialogueState();
        equal(state.nextStage(0, 0.4f), XueliDialogueState.OPENING, "opening before crossed thresholds");
        equal(state.nextStage(59, 1.0f), null, "stage spacing");
        equal(state.nextStage(60, 1.0f), XueliDialogueState.HIGH, "75 percent stays queued after healing");
        require(!state.randomReady(10000), "pending half health blocks ambient lines");
        restored.restore(state.progress(), 100);
        equal(restored.nextStage(100, 1.0f), XueliDialogueState.HALF_HEALTH, "pending stage survives reload and healing");
        equal(state.nextStage(119, 1.0f), null, "second stage spacing");
        equal(state.nextStage(120, 1.0f), XueliDialogueState.HALF_HEALTH, "queued half health");

        state = new XueliDialogueState();
        state.nextStage(1000, 1.0f);
        require(!state.randomReady(1399) && state.randomReady(1400), "first ambient line waits 20 seconds");
        state.deferRandom(1350);
        require(!state.randomReady(1449) && state.randomReady(1450), "skill line postpones ambient by five seconds");
        state.scheduleRandom(1450, 700);
        require(!state.randomReady(2149) && state.randomReady(2150), "35 second upper delay");
        state.scheduleRandom(2150, -1);
        require(!state.randomReady(2549) && state.randomReady(2550), "minimum delay clamp");
        state.scheduleRandom(2550, Integer.MAX_VALUE);
        require(!state.randomReady(3249) && state.randomReady(3250), "maximum delay clamp");
        Set<String> allLines = new HashSet<>();
        for (int previous = 0; previous < 3; previous++) {
            Set<String> alternatives = new HashSet<>();
            for (int choice = 0; choice < 2; choice++) {
                XueliDialogueState randomState = new XueliDialogueState();
                require(randomState.randomChoiceCount() == 3, "three initial choices");
                String prior = randomState.randomLine(previous);
                require(randomState.randomChoiceCount() == 2, "two non-repeating choices");
                String next = randomState.randomLine(choice);
                require(!prior.equals(next), "never repeat adjacent ambient lines");
                alternatives.add(next);
                allLines.add(prior);
            }
            require(alternatives.size() == 2, "both alternative lines are reachable");
        }
        equal(allLines, Set.of("你的怒火，也就这点程度！", "你不过是苟延残喘了几秒而已。", "胜利之风正从我身后吹来！"), "exact random text");
        equal(XueliDialogueState.ROLLER, "压路机来咯～", "roller text");
        equal(XueliDialogueState.SHIELD, "木大木大木大木大木大！", "shield text");
        equal(XueliDialogueState.SLAM, "乌鸦坐飞机！", "slam text");
        equal(XueliDialogueState.CLONES, "启动后备隐藏能源！", "clone text");
        System.out.println("PASS: " + checks + " Xueli dialogue checks");
    }

    private static void equal(Object actual, Object expected, String message) {
        require(Objects.equals(actual, expected), message);
    }

    private static void require(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
}
