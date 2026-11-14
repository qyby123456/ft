/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class ManorDefenseRules {
    public static final int WARNING_TICKS = 12;
    public static final int RECOVERY_TICKS = 24;
    public static final int HIT_WINDOW_TICKS = 80;
    public static final int ENTRY_GRACE_TICKS = 100;
    public static final int HITS_TO_TRIGGER = 3;
    public static final int HEARTBEAT_TICKS = 20;
    public static final int CLIENT_TIMEOUT_TICKS = 60;
    public static final int MAX_CLIENT_STATES = 64;
    public static final int IDLE = 0;
    public static final int WARNING = 1;
    public static final int GUARD = 2;
    public static final int RECOVERY = 3;
    public static final int NATIVE = 4;
    private static final Profile[] PROFILES = new Profile[]{new Profile(0, "youzi", "\u679c\u56ed\u679d\u76fe", 60, 400, 0.2f, 0.2f, -1.0, false), new Profile(1, "dandan", "\u672b\u5f71\u6298\u68f1", 44, 360, 0.35f, 0.35f, 0.34, false), new Profile(2, "duola", "\u6298\u53e0\u63a9\u4f53", 60, 440, 0.15f, 0.45f, 0.0, false), new Profile(3, "gongdi", "\u9ed1\u94c1\u622a\u52bf", 50, 400, 0.35f, 0.2f, 0.26, false), new Profile(4, "ningmengmao", "\u94f8\u9505\u62a4\u7f69", 60, 420, 0.25f, 0.25f, -1.0, false), new Profile(5, "qingyi", "\u6f6e\u7532\u56de\u6f9c", 50, 460, 0.2f, 0.2f, -1.0, false), new Profile(6, "wenxuanlan", "\u5929\u67a2\u7ffc\u67b6", 0, 0, 0.0f, 0.0f, -1.0, true), new Profile(7, "zheergeng", "\u5f71\u6839\u7ed3\u754c", 50, 440, 0.25f, 0.25f, -1.0, false), new Profile(8, "zicai", "\u76d0\u6676\u82d4\u5e55", 60, 440, 0.25f, 0.25f, -1.0, false), new Profile(10, "xueli", "\u5973\u4ec6\u65f6\u5323", 0, 0, 0.0f, 0.0f, -1.0, true), new Profile(11, "qizai", "\u661f\u7ecf\u4e66\u969c", 0, 0, 0.0f, 0.0f, -1.0, true)};

    private ManorDefenseRules() {
    }

    public static Profile profile(int variant) {
        for (Profile profile : PROFILES) {
            if (profile.variant() != variant) continue;
            return profile;
        }
        return null;
    }

    public static float reduce(Profile profile, int phase, float amount, boolean projectile, double frontDot, boolean bypass) {
        if (profile == null || phase != 2 || bypass || !Float.isFinite(amount) || amount <= 0.0f || !Double.isFinite(frontDot) || frontDot < profile.frontCosine()) {
            return amount;
        }
        float reduction = projectile ? profile.projectileReduction() : profile.meleeReduction();
        return amount * (1.0f - reduction);
    }

    public static float smooth(float value) {
        float clamped = Math.max(0.0f, Math.min(1.0f, value));
        return clamped * clamped * (3.0f - 2.0f * clamped);
    }

    public static float openness(int phase, float progress) {
        return phase == 1 ? 0.35f + 0.65f * ManorDefenseRules.smooth(progress) : (phase == 3 ? 1.0f - 0.65f * ManorDefenseRules.smooth(progress) : 1.0f);
    }

    public static float opacity(int phase, float progress) {
        return phase == 1 ? 0.18f + 0.32f * ManorDefenseRules.smooth(progress) : (phase == 3 ? 0.65f * (1.0f - ManorDefenseRules.smooth(progress)) : 0.65f);
    }

    public static int visualPhase(int phase, int adaptiveKind, float age, long duration) {
        boolean adaptive;
        boolean bl = adaptive = adaptiveKind == 1 || adaptiveKind == 2;
        if (phase == 0 || phase != 4 && age > (float)duration) {
            return adaptive ? 2 : 0;
        }
        if (adaptive && (phase == 1 || phase == 3)) {
            return 2;
        }
        return phase == 4 && age < 12.0f ? 1 : phase;
    }

    public record Profile(int variant, String model, String name, int guardTicks, int cooldownTicks, float meleeReduction, float projectileReduction, double frontCosine, boolean nativeOnly) {
    }

    public static final class State {
        public final Profile profile;
        public int phase;
        public long start;
        public long end;
        public long ready;
        private long lastTick;
        private long firstHit;
        private int hits;

        public State(Profile profile, long now) {
            this.profile = profile;
            this.lastTick = now;
            this.ready = now + 100L;
        }

        public void advance(long now, boolean nativeProtection) {
            if (now < this.lastTick) {
                this.clear(now);
                this.ready = now + 100L;
            }
            this.lastTick = now;
            if (nativeProtection) {
                if (this.phase != 4) {
                    this.transition(4, now, Long.MAX_VALUE);
                }
                this.hits = 0;
                return;
            }
            if (this.phase == 4) {
                this.transition(3, now, now + 24L);
                this.ready = Math.max(this.ready, now + 24L);
            }
            if (this.phase == 1 && now >= this.end) {
                long activation = this.end;
                this.transition(2, activation, activation + (long)this.profile.guardTicks());
                this.ready = activation + (long)this.profile.cooldownTicks();
            }
            if (this.phase == 2 && now >= this.end) {
                this.transition(3, this.end, this.end + 24L);
            }
            if (this.phase == 3 && now >= this.end) {
                this.transition(0, now, now);
            }
            if (this.hits > 0 && now - this.firstHit > 80L) {
                this.hits = 0;
            }
        }

        public boolean confirmedHit(long now) {
            if (this.profile.nativeOnly() || this.phase != 0 || now < this.ready) {
                return false;
            }
            if (this.hits == 0 || now - this.firstHit > 80L) {
                this.firstHit = now;
                this.hits = 0;
            }
            if (++this.hits < 3) {
                return false;
            }
            this.hits = 0;
            this.transition(1, now, now + 12L);
            return true;
        }

        public void clear(long now) {
            this.hits = 0;
            this.transition(0, now, now);
        }

        private void transition(int next, long from, long to) {
            this.phase = next;
            this.start = from;
            this.end = to;
        }
    }
}

