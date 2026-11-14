/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class XueliDialogueState {
    public static final String OPENING = "\u543c\u543c\uff0c\u5c45\u7136\u6ca1\u6709\u9009\u62e9\u9003\u8dd1\uff0c\u53cd\u800c\u5411\u6211\u8fd9\u8fb9\u8d70\u6765\u4e86\u5417\uff1f";
    public static final String HIGH = "\u6211\u771f\u662fhigh\u5230\u4e0d\u884c\u4e86\u5440";
    public static final String HALF_HEALTH = "\u8d8a\u662f\u73a9\u5f04\u9634\u8c0b\u8be1\u8ba1\uff0c\u5c31\u8d8a\u662f\u610f\u8bc6\u5230\u4eba\u7c7b\u7684\u5f31\u5c0f\uff0c\u6240\u4ee5\uff0c\u6211\u4e0d\u505a\u4eba\u4e86\uff01";
    public static final String ROLLER = "\u538b\u8def\u673a\u6765\u54af\uff5e";
    public static final String SHIELD = "\u6728\u5927\u6728\u5927\u6728\u5927\u6728\u5927\u6728\u5927\uff01";
    public static final String SLAM = "\u4e4c\u9e26\u5750\u98de\u673a\uff01";
    public static final String CLONES = "\u542f\u52a8\u540e\u5907\u9690\u85cf\u80fd\u6e90\uff01";
    public static final int STAGE_GAP = 60;
    public static final int RANDOM_MIN_DELAY = 400;
    public static final int RANDOM_MAX_DELAY = 700;
    private static final String[] RANDOM_LINES = new String[]{"\u4f60\u7684\u6012\u706b\uff0c\u4e5f\u5c31\u8fd9\u70b9\u7a0b\u5ea6\uff01", "\u4f60\u4e0d\u8fc7\u662f\u82df\u5ef6\u6b8b\u5598\u4e86\u51e0\u79d2\u800c\u5df2\u3002", "\u80dc\u5229\u4e4b\u98ce\u6b63\u4ece\u6211\u8eab\u540e\u5439\u6765\uff01"};
    private int announcedStages;
    private int reachedStages = 1;
    private int lastRandomIndex = -1;
    private long nextStageAt;
    private long nextRandomAt;

    public String nextStage(long now, float healthRatio) {
        int pending;
        if (healthRatio <= 0.75f) {
            this.reachedStages |= 2;
        }
        if (healthRatio <= 0.5f) {
            this.reachedStages |= 4;
        }
        if ((pending = this.reachedStages & ~this.announcedStages) == 0 || now < this.nextStageAt) {
            return null;
        }
        int stage = Integer.lowestOneBit(pending);
        this.announcedStages |= stage;
        this.nextStageAt = now + 60L;
        if (stage == 1) {
            this.nextRandomAt = now + 400L;
        }
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
        if (this.lastRandomIndex >= 0 && index >= this.lastRandomIndex) {
            ++index;
        }
        this.lastRandomIndex = index;
        return RANDOM_LINES[index];
    }

    public void deferRandom(long now) {
        this.nextRandomAt = Math.max(this.nextRandomAt, now + 100L);
    }

    public void scheduleRandom(long now, int delay) {
        this.nextRandomAt = now + (long)Math.max(400, Math.min(700, delay));
    }

    public int progress() {
        return this.announcedStages | this.reachedStages << 3;
    }

    public void restore(int progress, long now) {
        this.announcedStages = progress & 7;
        this.reachedStages = 1 | progress >>> 3 & 7 | this.announcedStages;
        this.lastRandomIndex = -1;
        this.nextStageAt = now;
        this.nextRandomAt = now + 400L;
    }
}

