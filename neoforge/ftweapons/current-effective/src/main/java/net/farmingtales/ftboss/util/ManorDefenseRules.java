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
    private static final ManorDefenseRules.Profile[] PROFILES = new ManorDefenseRules.Profile[]{
        new ManorDefenseRules.Profile(0, "youzi", "果园枝盾", 60, 400, 0.2F, 0.2F, -1.0, false),
        new ManorDefenseRules.Profile(1, "dandan", "末影折棱", 44, 360, 0.35F, 0.35F, 0.34, false),
        new ManorDefenseRules.Profile(2, "duola", "折叠掩体", 60, 440, 0.15F, 0.45F, 0.0, false),
        new ManorDefenseRules.Profile(3, "gongdi", "黑铁截势", 50, 400, 0.35F, 0.2F, 0.26, false),
        new ManorDefenseRules.Profile(4, "ningmengmao", "铸锅护罩", 60, 420, 0.25F, 0.25F, -1.0, false),
        new ManorDefenseRules.Profile(5, "qingyi", "潮甲回澜", 50, 460, 0.2F, 0.2F, -1.0, false),
        new ManorDefenseRules.Profile(6, "wenxuanlan", "天枢翼架", 0, 0, 0.0F, 0.0F, -1.0, true),
        new ManorDefenseRules.Profile(7, "zheergeng", "影根结界", 50, 440, 0.25F, 0.25F, -1.0, false),
        new ManorDefenseRules.Profile(8, "zicai", "盐晶苔幕", 60, 440, 0.25F, 0.25F, -1.0, false),
        new ManorDefenseRules.Profile(10, "xueli", "女仆时匣", 0, 0, 0.0F, 0.0F, -1.0, true),
        new ManorDefenseRules.Profile(11, "qizai", "星经书障", 0, 0, 0.0F, 0.0F, -1.0, true)
    };

    private ManorDefenseRules() {
    }

    public static ManorDefenseRules.Profile profile(int variant) {
        for (ManorDefenseRules.Profile profile : PROFILES) {
            if (profile.variant() == variant) {
                return profile;
            }
        }

        return null;
    }

    public static float reduce(ManorDefenseRules.Profile profile, int phase, float amount, boolean projectile, double frontDot, boolean bypass) {
        if (profile != null
            && phase == 2
            && !bypass
            && Float.isFinite(amount)
            && !(amount <= 0.0F)
            && Double.isFinite(frontDot)
            && !(frontDot < profile.frontCosine())) {
            float reduction = projectile ? profile.projectileReduction() : profile.meleeReduction();
            return amount * (1.0F - reduction);
        } else {
            return amount;
        }
    }

    public static float smooth(float value) {
        float clamped = Math.max(0.0F, Math.min(1.0F, value));
        return clamped * clamped * (3.0F - 2.0F * clamped);
    }

    public static float openness(int phase, float progress) {
        return phase == 1 ? 0.35F + 0.65F * smooth(progress) : (phase == 3 ? 1.0F - 0.65F * smooth(progress) : 1.0F);
    }

    public static float opacity(int phase, float progress) {
        return phase == 1 ? 0.18F + 0.32F * smooth(progress) : (phase == 3 ? 0.65F * (1.0F - smooth(progress)) : 0.65F);
    }

    public static int visualPhase(int phase, int adaptiveKind, float age, long duration) {
        boolean adaptive = adaptiveKind == 1 || adaptiveKind == 2;
        if (phase != 0 && (phase == 4 || !(age > (float)duration))) {
            if (!adaptive || phase != 1 && phase != 3) {
                return phase == 4 && age < 12.0F ? 1 : phase;
            } else {
                return 2;
            }
        } else {
            return adaptive ? 2 : 0;
        }
    }

    public static record Profile(
        int variant,
        String model,
        String name,
        int guardTicks,
        int cooldownTicks,
        float meleeReduction,
        float projectileReduction,
        double frontCosine,
        boolean nativeOnly
    ) {
    }

    public static final class State {
        public final ManorDefenseRules.Profile profile;
        public int phase;
        public long start;
        public long end;
        public long ready;
        private long lastTick;
        private long firstHit;
        private int hits;

        public State(ManorDefenseRules.Profile profile, long now) {
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
            } else {
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
        }

        public boolean confirmedHit(long now) {
            if (!this.profile.nativeOnly() && this.phase == 0 && now >= this.ready) {
                if (this.hits == 0 || now - this.firstHit > 80L) {
                    this.firstHit = now;
                    this.hits = 0;
                }

                if (++this.hits < 3) {
                    return false;
                } else {
                    this.hits = 0;
                    this.transition(1, now, now + 12L);
                    return true;
                }
            } else {
                return false;
            }
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
