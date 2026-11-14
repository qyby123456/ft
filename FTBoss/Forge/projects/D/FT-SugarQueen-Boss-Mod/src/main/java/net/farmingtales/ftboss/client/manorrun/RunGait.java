package net.farmingtales.ftboss.client.manorrun;

public final class RunGait {
    public static final double START_SPEED = 0.24;
    public static final double FULL_SPEED = 0.38;
    public static final double MAX_STEP = 2.5;
    public static final double STANCE = 0.44;
    public record Profile(String name, double lean, double stride, double clearance, double bob, double arm, double grip, double cadence) {}
    private static final Profile[] PROFILES = {
        new Profile("柚子", .29, 1.04, 1.8, .38, .58, -.48, .96),
        new Profile("蛋蛋", .26, 1.08, 2.1, .43, .67, -.58, 1.00),
        new Profile("朵拉", .12, .96, 1.5, .23, .36, -.90, 1.03),
        new Profile("功底", .34, 1.13, 1.8, .34, .52, -.62, .92),
        new Profile("柠檬猫", .22, .95, 2.4, .63, .76, -.46, 1.08),
        new Profile("青衣", .28, 1.10, 2.0, .40, .70, -.56, 1.00),
        new Profile("文玄蓝", .17, 1.01, 1.8, .29, .45, -.62, 1.02),
        new Profile("折耳根", .31, 1.09, 2.1, .31, .60, -.50, 1.06),
        new Profile("紫菜", .23, .99, 2.3, .51, .71, -.49, 1.04),
        new Profile("麻雀", .18, .93, 2.1, .50, .60, -.55, 1.07),
        new Profile("雪黎", .25, 1.03, 1.9, .35, .62, -.51, 1.01),
        new Profile("祁崽", .15, .96, 1.6, .24, .42, -.92, .98)
    };
    private RunGait() {}
    public static Profile profile(int variant) { return PROFILES[Math.max(0, Math.min(PROFILES.length - 1, variant))]; }
    public static int count() { return PROFILES.length; }
    public static double clamp(double value) { return Math.max(0, Math.min(1, value)); }
    public static double smooth(double value) { double amount = clamp(value); return amount * amount * (3 - 2 * amount); }
    public static double fraction(double value) { return value - Math.floor(value); }
    public static double weight(double speed) { return Double.isFinite(speed) && speed <= MAX_STEP ? smooth((speed - START_SPEED) / (FULL_SPEED - START_SPEED)) : 0; }
    public static double cadence(Profile profile, double speed) { return profile.cadence() * (2.25 + 1.15 * smooth((speed - .28) / .55)); }
    public static double sole(double pitch, double yaw, double roll) {
        double rowX = Math.sin(roll) * Math.cos(yaw);
        double rowY = Math.sin(roll) * Math.sin(yaw) * Math.sin(pitch) + Math.cos(roll) * Math.cos(pitch);
        double rowZ = Math.sin(roll) * Math.sin(yaw) * Math.cos(pitch) - Math.cos(roll) * Math.sin(pitch);
        return 12 * Math.max(0, rowY) + 2 * Math.abs(rowX) + 2 * Math.abs(rowZ);
    }
    public static double channelAllowance(double rotationDifference, double positionDifference) {
        return 1 - smooth(Math.max(Math.abs(rotationDifference) / .30, Math.abs(positionDifference) / 1.25));
    }
    public static void orient(double[] pose, double forward, double sideways) {
        double lean = pose[9] * forward, bob = 12 * (1 - Math.cos(pose[9])) - pose[7];
        double roll = -.10 * sideways;
        double chestY = 12 * (1 - Math.cos(lean) * Math.cos(roll)) - bob;
        double chestZ = -12 * Math.sin(lean);
        pose[0] = pose[6] = 12 * Math.sin(roll);
        pose[7] = chestY;
        pose[1] = chestY + 2 * Math.abs(Math.sin(lean)) + 2 * Math.abs(Math.sin(pose[11] + roll));
        pose[2] = pose[8] = chestZ;
        pose[5] = 0;
        pose[9] = lean;
        pose[11] += roll;
        for (int side = 0; side < 2; side++) {
            int leg = (4 + side) * 6, arm = (2 + side) * 6;
            double lift = 12 - sole(pose[leg + 3], pose[leg + 4], pose[leg + 5]) - pose[leg + 1];
            double originalPitch = pose[leg + 3];
            pose[leg + 3] *= forward;
            pose[leg + 5] += originalPitch * sideways * .52;
            pose[leg + 1] = 12 - sole(pose[leg + 3], pose[leg + 4], pose[leg + 5]) - lift;
            pose[arm] = 9.5 * Math.sin(roll);
            pose[arm + 1] = 9.5 * (1 - Math.cos(lean) * Math.cos(roll)) - bob;
            pose[arm + 2] = -9.5 * Math.sin(lean);
            if (Math.abs(pose[arm + 4]) < .001) pose[arm + 3] = -.18 + (pose[arm + 3] + .18) * forward;
        }
    }
    public static void sample(int variant, double phase, double speed, boolean leftHanded, boolean held, double[] pose) {
        java.util.Arrays.fill(pose, 0);
        Profile profile = profile(variant);
        double clock = fraction(phase), angle = clock * Math.PI * 2;
        double lean = profile.lean() * (.88 + .12 * smooth((speed - .3) / .5));
        double bob = profile.bob() * (.5 - .5 * Math.cos(angle * 2));
        double chestY = 12 * (1 - Math.cos(lean)) - bob;
        double chestZ = -12 * Math.sin(lean);
        pose[1] = chestY + 2 * Math.abs(Math.sin(lean)) + 2 * Math.abs(Math.sin(.018 * Math.sin(angle)));
        pose[2] = chestZ;
        pose[3] = .018 * Math.sin(angle * 2);
        pose[7] = chestY;
        pose[8] = chestZ;
        pose[9] = lean;
        pose[10] = .028 * Math.sin(angle);
        pose[11] = .018 * Math.sin(angle);
        for (int side = 0; side < 2; side++) {
            int leg = (4 + side) * 6, arm = (2 + side) * 6;
            double legClock = fraction(clock + side * .5);
            boolean stance = legClock < STANCE;
            double progress = stance ? legClock / STANCE : (legClock - STANCE) / (1 - STANCE);
            double stride = profile.stride() * (.89 + .11 * smooth((speed - .3) / .5));
            double pitch = stance ? -.70 + 1.57 * smooth(progress) : .87 - 1.57 * smooth(progress);
            pitch *= stride;
            double lift = stance ? 0 : profile.clearance() * Math.pow(Math.sin(progress * Math.PI), 2);
            double roll = (side == 0 ? 1 : -1) * .024 * Math.sin(angle * 2);
            pose[leg + 1] = 12 - sole(pitch, 0, roll) - lift;
            pose[leg + 3] = pitch;
            pose[leg + 5] = roll;
            pose[arm + 1] = 9.5 * (1 - Math.cos(lean)) - bob;
            pose[arm + 2] = -9.5 * Math.sin(lean);
            boolean weaponArm = held && (leftHanded == (side == 1));
            pose[arm + 3] = weaponArm ? profile.grip() + .035 * Math.sin(angle * 2) : -.18 - profile.arm() * pitch;
            pose[arm + 4] = weaponArm ? (side == 0 ? -.12 : .12) : 0;
            pose[arm + 5] = (side == 0 ? 1 : -1) * (weaponArm ? .15 : .075);
        }
    }
    public static final class Clock {
        public double phase, speed, blend;
        private double previousAge = Double.NaN;
        public void update(double age, double observed, boolean grounded, double walkPhase, int variant) {
            if (!Double.isFinite(age) || !Double.isFinite(observed) || observed < 0 || observed > MAX_STEP || !grounded) {
                blend = speed = 0;
                previousAge = age;
                return;
            }
            if (!Double.isFinite(previousAge) || age < previousAge || age - previousAge > 20) {
                previousAge = age;
                speed = observed;
                phase = fraction(walkPhase / (Math.PI * 2));
                blend = 0;
                return;
            }
            double elapsed = Math.min(4, age - previousAge);
            previousAge = age;
            speed += (observed - speed) * (1 - Math.exp(-elapsed / 1.8));
            double target = observed < .01 ? 0 : weight(speed);
            blend += (target - blend) * (1 - Math.exp(-elapsed / (target > blend ? 2.3 : 1.6)));
            if (blend < .001 && target == 0) {
                blend = 0;
                phase = fraction(walkPhase / (Math.PI * 2));
            } else {
                phase = fraction(phase + elapsed * cadence(profile(variant), speed) * smooth(speed / .12) / 20);
            }
        }
    }
}
