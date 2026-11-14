package net.farmingtales.ftboss.util;

/** 庄园防御纯规则：2026-10-13，集中保守数值；有限减伤不修改属性、攻击或原生免伤。 */
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

    public record Profile(int variant, String model, String name, int guardTicks, int cooldownTicks,
                          float meleeReduction, float projectileReduction, double frontCosine, boolean nativeOnly) {}

    private static final Profile[] PROFILES = {
            new Profile(0, "youzi", "果园枝盾", 60, 400, 0.20F, 0.20F, -1.0, false),
            new Profile(1, "dandan", "末影折棱", 44, 360, 0.35F, 0.35F, 0.34, false),
            new Profile(2, "duola", "折叠掩体", 60, 440, 0.15F, 0.45F, 0.0, false),
            new Profile(3, "gongdi", "黑铁截势", 50, 400, 0.35F, 0.20F, 0.26, false),
            new Profile(4, "ningmengmao", "铸锅护罩", 60, 420, 0.25F, 0.25F, -1.0, false),
            new Profile(5, "qingyi", "潮甲回澜", 50, 460, 0.20F, 0.20F, -1.0, false),
            new Profile(6, "wenxuanlan", "天枢翼架", 0, 0, 0.0F, 0.0F, -1.0, true),
            new Profile(7, "zheergeng", "影根结界", 50, 440, 0.25F, 0.25F, -1.0, false),
            new Profile(8, "zicai", "盐晶苔幕", 60, 440, 0.25F, 0.25F, -1.0, false),
            new Profile(10, "xueli", "女仆时匣", 0, 0, 0.0F, 0.0F, -1.0, true),
            new Profile(11, "qizai", "星经书障", 0, 0, 0.0F, 0.0F, -1.0, true)
    };

    private ManorDefenseRules() {}

    public static Profile profile(int variant) {
        for (Profile profile : PROFILES) if (profile.variant() == variant) return profile;
        return null;
    }

    public static float reduce(Profile profile, int phase, float amount, boolean projectile, double frontDot, boolean bypass) {
        if (profile == null || phase != GUARD || bypass || !Float.isFinite(amount) || amount <= 0.0F
                || !Double.isFinite(frontDot) || frontDot < profile.frontCosine()) return amount;
        float reduction = projectile ? profile.projectileReduction() : profile.meleeReduction();
        return amount * (1.0F - reduction);
    }

    public static float smooth(float value) {
        float clamped = Math.max(0.0F, Math.min(1.0F, value));
        return clamped * clamped * (3.0F - 2.0F * clamped);
    }

    public static float openness(int phase, float progress) {
        return phase == WARNING ? 0.35F + 0.65F * smooth(progress)
                : phase == RECOVERY ? 1.0F - 0.65F * smooth(progress) : 1.0F;
    }

    public static float opacity(int phase, float progress) {
        return phase == WARNING ? 0.18F + 0.32F * smooth(progress)
                : phase == RECOVERY ? 0.65F * (1.0F - smooth(progress)) : 0.65F;
    }

    public static int visualPhase(int phase, int adaptiveKind, float age, long duration) {
        boolean adaptive = adaptiveKind == 1 || adaptiveKind == 2;
        if (phase == IDLE || phase != NATIVE && age > duration) return adaptive ? GUARD : IDLE;
        if (adaptive && (phase == WARNING || phase == RECOVERY)) return GUARD;
        return phase == NATIVE && age < WARNING_TICKS ? WARNING : phase;
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
            this.ready = now + ENTRY_GRACE_TICKS;
        }

        public void advance(long now, boolean nativeProtection) {
            if (now < lastTick) {
                clear(now);
                ready = now + ENTRY_GRACE_TICKS;
            }
            lastTick = now;
            if (nativeProtection) {
                if (phase != NATIVE) transition(NATIVE, now, Long.MAX_VALUE);
                hits = 0;
                return;
            }
            if (phase == NATIVE) {
                transition(RECOVERY, now, now + RECOVERY_TICKS);
                ready = Math.max(ready, now + RECOVERY_TICKS);
            }
            if (phase == WARNING && now >= end) {
                long activation = end;
                transition(GUARD, activation, activation + profile.guardTicks());
                ready = activation + profile.cooldownTicks();
            }
            if (phase == GUARD && now >= end) transition(RECOVERY, end, end + RECOVERY_TICKS);
            if (phase == RECOVERY && now >= end) transition(IDLE, now, now);
            if (hits > 0 && now - firstHit > HIT_WINDOW_TICKS) hits = 0;
        }

        public boolean confirmedHit(long now) {
            if (profile.nativeOnly() || phase != IDLE || now < ready) return false;
            if (hits == 0 || now - firstHit > HIT_WINDOW_TICKS) {
                firstHit = now;
                hits = 0;
            }
            if (++hits < HITS_TO_TRIGGER) return false;
            hits = 0;
            transition(WARNING, now, now + WARNING_TICKS);
            return true;
        }

        public void clear(long now) {
            hits = 0;
            transition(IDLE, now, now);
        }

        private void transition(int next, long from, long to) {
            phase = next;
            start = from;
            end = to;
        }
    }
}
