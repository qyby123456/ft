package net.farmingtales.ftboss.util;

public final class XueliDialogueState {
    public static final String OPENING = "吼吼，居然没有选择逃跑，反而向我这边走来了吗？";
    public static final String HIGH = "我真是high到不行了呀";
    public static final String HALF_HEALTH = "越是玩弄阴谋诡计，就越是意识到人类的弱小，所以，我不做人了！";
    public static final String ROLLER = "压路机来咯～";
    public static final String SHIELD = "木大木大木大木大木大！";
    public static final String SLAM = "乌鸦坐飞机！";
    public static final String CLONES = "启动后备隐藏能源！";
    public static final int STAGE_GAP = 3 * 20;
    public static final int RANDOM_MIN_DELAY = 20 * 20;
    public static final int RANDOM_MAX_DELAY = 35 * 20;
    private static final String[] RANDOM_LINES = {
            "你的怒火，也就这点程度！",
            "你不过是苟延残喘了几秒而已。",
            "胜利之风正从我身后吹来！"
    };
    private int announcedStages;
    private int reachedStages = 1;
    private int lastRandomIndex = -1;
    private long nextStageAt;
    private long nextRandomAt;

    public String nextStage(long now, float healthRatio) {
        if (healthRatio <= 0.75f) this.reachedStages |= 2;
        if (healthRatio <= 0.50f) this.reachedStages |= 4;
        int pending = this.reachedStages & ~this.announcedStages;
        if (pending == 0 || now < this.nextStageAt) return null;
        int stage = Integer.lowestOneBit(pending);
        this.announcedStages |= stage;
        this.nextStageAt = now + STAGE_GAP;
        if (stage == 1) this.nextRandomAt = now + RANDOM_MIN_DELAY;
        return switch (stage) {
            case 1 -> OPENING;
            case 2 -> HIGH;
            default -> HALF_HEALTH;
        };
    }

    public boolean randomReady(long now) {
        return (this.reachedStages & ~this.announcedStages) == 0 && now >= this.nextRandomAt;
    }

    public int randomChoiceCount() {
        return this.lastRandomIndex < 0 ? RANDOM_LINES.length : RANDOM_LINES.length - 1;
    }

    public String randomLine(int choice) {
        int index = Math.floorMod(choice, this.randomChoiceCount());
        if (this.lastRandomIndex >= 0 && index >= this.lastRandomIndex) index++;
        this.lastRandomIndex = index;
        return RANDOM_LINES[index];
    }

    public void deferRandom(long now) {
        this.nextRandomAt = Math.max(this.nextRandomAt, now + 5 * 20);
    }

    public void scheduleRandom(long now, int delay) {
        this.nextRandomAt = now + Math.max(RANDOM_MIN_DELAY, Math.min(RANDOM_MAX_DELAY, delay));
    }

    public int progress() {
        return this.announcedStages | (this.reachedStages << 3);
    }

    public void restore(int progress, long now) {
        this.announcedStages = progress & 7;
        this.reachedStages = 1 | ((progress >>> 3) & 7) | this.announcedStages;
        this.lastRandomIndex = -1;
        this.nextStageAt = now;
        this.nextRandomAt = now + RANDOM_MIN_DELAY;
    }
}
