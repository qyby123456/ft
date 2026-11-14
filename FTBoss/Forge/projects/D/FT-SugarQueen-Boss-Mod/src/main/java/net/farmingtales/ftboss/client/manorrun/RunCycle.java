package net.farmingtales.ftboss.client.manorrun;

public final class RunCycle {
    public static final double WALK_ENTER = 0.018, IDLE_ENTER = 0.009;
    public static final double RUN_ENTER = 0.180, RUN_EXIT = 0.145;
    public static final double FAST = 0.320, SPRINT = 0.480;
    private static final double TWO_PI = Math.PI * 2.0;

    public static final class State {
        public int tick = Integer.MIN_VALUE;
        public double lastX, lastZ, speed, phase, previousPhase, weight, previousWeight;
        public double travelSign = 1;
        public int mode;
        private int enterTicks, exitTicks, walkTicks, idleTicks;

        public void update(int now, double positionX, double positionZ, boolean blocked, double forward) {
            if (now == tick) return;
            int elapsed = now - tick;
            if (tick == Integer.MIN_VALUE || elapsed < 1 || elapsed > 5
                    || !Double.isFinite(positionX) || !Double.isFinite(positionZ)) {
                reset(now, positionX, positionZ);
                return;
            }
            double distance = Math.hypot(positionX - lastX, positionZ - lastZ);
            previousPhase = phase;
            previousWeight = weight;
            if (distance > 1.5 * elapsed) {
                reset(now, positionX, positionZ);
                return;
            }
            speed += (distance / elapsed - speed) * (1.0 - Math.pow(0.55, elapsed));
            if (blocked) {
                enterTicks = exitTicks = walkTicks = idleTicks = 0;
                mode = 0;
                weight = previousWeight = 0;
            } else {
                enterTicks = speed >= RUN_ENTER ? enterTicks + elapsed : 0;
                exitTicks = speed <= RUN_EXIT ? exitTicks + elapsed : 0;
                walkTicks = speed >= WALK_ENTER ? walkTicks + elapsed : 0;
                idleTicks = speed <= IDLE_ENTER ? idleTicks + elapsed : 0;
                if (mode != 2 && enterTicks >= 3) mode = 2;
                else if (mode == 2 && exitTicks >= 4) mode = speed >= WALK_ENTER ? 1 : 0;
                else if (mode == 0 && walkTicks >= 3) mode = 1;
                else if (mode == 1 && idleTicks >= 4) mode = 0;
                double target = mode == 2 ? 1.0 : 0.0;
                weight += (target - weight) * (1.0 - Math.pow(target > weight ? 0.72 : 0.76, elapsed));
            }
            if (forward < -distance * 0.35) travelSign = -1.0;
            else if (forward > distance * 0.35) travelSign = 1.0;
            phase += travelSign * Math.min(distance * TWO_PI / 2.8, 0.68 * elapsed);
            if (Math.abs(phase) > 10000) {
                double turns = Math.floor(phase / TWO_PI) * TWO_PI;
                phase -= turns;
                previousPhase -= turns;
            }
            tick = now;
            lastX = positionX;
            lastZ = positionZ;
        }

        private void reset(int now, double positionX, double positionZ) {
            tick = now;
            lastX = Double.isFinite(positionX) ? positionX : 0;
            lastZ = Double.isFinite(positionZ) ? positionZ : 0;
            speed = weight = previousWeight = phase = previousPhase = 0;
            mode = enterTicks = exitTicks = walkTicks = idleTicks = 0;
            travelSign = 1;
        }
    }

    public record Pose(double leg, double lean, double rightArm, double leftArm, double roll) { }

    public static Pose pose(int variant, double speed, double phase) {
        double intensity = clamp((speed - RUN_EXIT) / (SPRINT - RUN_EXIT), 0, 1);
        double stride = Math.sin(phase) * (0.48 + 0.24 * intensity);
        double lag = Math.sin(phase - 0.20);
        double lean = 0.07 + 0.035 * intensity;
        double right = -0.24 - 0.12 * lag, left = -0.18 + 0.35 * lag, roll = 0.035;
        switch (variant) {
            case 0 -> { right = -0.34 - 0.065 * lag; left = -0.18 + 0.24 * lag; }
            case 1 -> { right = -0.36 - 0.07 * lag; left = -0.15 + 0.32 * lag; }
            case 2 -> { right = -0.50 - 0.035 * lag; left = -0.46 + 0.035 * lag; }
            case 3 -> { right = -0.28 - 0.045 * lag; left = -0.18 + 0.28 * lag; roll = 0.015; }
            case 4 -> { right = -0.32 - 0.04 * lag; left = -0.30 + 0.055 * lag; }
            case 5 -> { right = -0.30 - 0.055 * lag; left = -0.20 + 0.30 * lag; }
            case 6 -> { right = -0.65 - 0.03 * lag; left = -0.62 + 0.03 * lag; }
            case 7 -> { right = -0.24 - 0.07 * lag; left = -0.17 + 0.32 * lag; }
            case 8 -> { right = -0.29 - 0.045 * lag; left = -0.19 + 0.26 * lag; }
            case 10 -> { right = -0.32 - 0.04 * lag; left = -0.30 + 0.055 * lag; }
            case 11 -> { right = -0.27 - 0.055 * lag; left = -0.17 + 0.26 * lag; }
            case 12 -> { right = -0.26 - 0.05 * lag; left = -0.18 + 0.30 * lag; }
            default -> { }
        }
        return new Pose(stride, lean, right, left, roll);
    }

    public static Pose settledPose(int variant, double speed, double phase) {
        Pose moving = pose(variant,speed,phase);
        if (speed >= RUN_ENTER) return moving;
        Pose held = pose(variant,0,0);
        double walking = speed >= WALK_ENTER ? clamp(speed / RUN_EXIT,0,1) : 0;
        return new Pose(walking * 0.40 * Math.sin(phase),0,
                held.rightArm() + (moving.rightArm() - held.rightArm()) * walking * 0.65,
                held.leftArm() + (moving.leftArm() - held.leftArm()) * walking * 0.65,held.roll());
    }

    public static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private RunCycle() { }
}
