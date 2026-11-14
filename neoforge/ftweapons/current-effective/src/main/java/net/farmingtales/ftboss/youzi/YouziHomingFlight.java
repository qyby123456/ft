package net.farmingtales.ftboss.youzi;

import java.util.ArrayList;
import java.util.List;

public final class YouziHomingFlight {
    private final YouziHomingFlight.Parameters parameters;
    private YouziHomingFlight.Vector position;
    private YouziHomingFlight.Vector heading;
    private double age;
    private double distance;
    private double speed;
    private boolean targetLost;
    private YouziHomingFlight.EndReason endReason = YouziHomingFlight.EndReason.ACTIVE;

    public YouziHomingFlight(YouziHomingFlight.Parameters var1, YouziHomingFlight.Vector var2, YouziHomingFlight.Vector var3) {
        if (var1 != null && var2 != null && var3 != null) {
            this.parameters = var1;
            this.position = var2;
            this.heading = var3.normalize();
            this.speed = var1.initialSpeed();
        } else {
            throw new IllegalArgumentException("Missing flight state");
        }
    }

    public YouziHomingFlight.Snapshot snapshot() {
        return new YouziHomingFlight.Snapshot(this.position, this.heading, this.age, this.distance, this.speed, this.targetLost, this.endReason);
    }

    public List<YouziHomingFlight.Segment> advance(double var1, YouziHomingFlight.TargetProvider var3, YouziHomingFlight.CollisionProbe var4) {
        return this.advance(var1, var3, var4, true);
    }

    public List<YouziHomingFlight.Segment> advance(double var1, YouziHomingFlight.TargetProvider var3, YouziHomingFlight.CollisionProbe var4, boolean var5) {
        requireFinite(var1);
        if (!(var1 < 0.0) && var3 != null && var4 != null) {
            List<YouziHomingFlight.Segment> var6 = new ArrayList<>();
            double var7 = var1;

            while (var7 > 1.0E-10 && this.endReason == YouziHomingFlight.EndReason.ACTIVE) {
                double var9 = Math.min(Math.min(this.parameters.fixedStepSeconds(), var7), this.parameters.maximumLifetime() - this.age);
                if (var9 <= 1.0E-10) {
                    this.endReason = YouziHomingFlight.EndReason.LIFETIME;
                    break;
                }

                YouziHomingFlight.Vector var11 = this.position;
                double var12 = this.age;
                YouziHomingFlight.Target var14 = this.targetLost ? null : var3.at(this.age);
                if (var14 == null) {
                    this.targetLost = true;
                }

                this.speed = this.parameters.initialSpeed()
                    + (this.parameters.maximumSpeed() - this.parameters.initialSpeed())
                        * Math.min(1.0, (this.age + var9 * 0.5) / this.parameters.accelerationSeconds());
                if (var14 != null) {
                    double var15 = Math.min(this.parameters.predictionSeconds(), var14.position().subtract(this.position).length() / this.speed);
                    YouziHomingFlight.Vector var17 = var14.position().add(var14.velocity().multiply(var15));
                    this.heading = turnTowards(
                        this.heading, var17.subtract(this.position), Math.toRadians(this.parameters.maximumTurnDegreesPerSecond()) * var9
                    );
                }

                double var25 = Math.min(this.speed * var9, this.parameters.maximumDistance() - this.distance);
                double var26 = var25 / this.speed;
                YouziHomingFlight.Vector var19 = this.position.add(this.heading.multiply(var25));
                YouziHomingFlight.Target var20 = var14 == null ? null : var3.at(this.age + var26);
                Double var21 = var5 && var20 != null
                    ? sweptSphereFraction(var11, var19, var14.position(), var20.position(), var14.radius() + this.parameters.projectileRadius())
                    : null;
                YouziHomingFlight.Impact var22 = var4.firstImpact(var11, var19, this.parameters.projectileRadius());
                double var23 = 1.0;
                if (var21 != null) {
                    var23 = var21;
                    this.endReason = YouziHomingFlight.EndReason.TARGET;
                }

                if (var22 != null && var22.fraction() <= var23) {
                    var23 = var22.fraction();
                    this.endReason = var22.reason();
                }

                this.position = var11.lerp(var19, var23);
                this.age += var26 * var23;
                this.distance += var25 * var23;
                var6.add(new YouziHomingFlight.Segment(var11, this.position, var12, this.age));
                var7 -= var9;
                if (this.endReason == YouziHomingFlight.EndReason.ACTIVE && this.distance >= this.parameters.maximumDistance() - 1.0E-10) {
                    this.endReason = YouziHomingFlight.EndReason.RANGE;
                }

                if (this.endReason == YouziHomingFlight.EndReason.ACTIVE && this.age >= this.parameters.maximumLifetime() - 1.0E-10) {
                    this.endReason = YouziHomingFlight.EndReason.LIFETIME;
                }
            }

            return List.copyOf(var6);
        } else {
            throw new IllegalArgumentException("Invalid flight step");
        }
    }

    public static YouziHomingFlight.Vector turnTowards(YouziHomingFlight.Vector var0, YouziHomingFlight.Vector var1, double var2) {
        YouziHomingFlight.Vector var4 = var0.normalize();
        YouziHomingFlight.Vector var5 = var1.normalize();
        double var6 = Math.acos(Math.max(-1.0, Math.min(1.0, var4.dot(var5))));
        if (!(var6 <= var2) && !(var6 < 1.0E-10)) {
            YouziHomingFlight.Vector var8 = var4.cross(var5);
            if (var8.length() < 1.0E-8) {
                var8 = var4.cross(
                    Math.abs(var4.coordinateX()) < 0.8 ? new YouziHomingFlight.Vector(1.0, 0.0, 0.0) : new YouziHomingFlight.Vector(0.0, 1.0, 0.0)
                );
            }

            var8 = var8.normalize();
            return var4.multiply(Math.cos(var2))
                .add(var8.cross(var4).multiply(Math.sin(var2)))
                .add(var8.multiply(var8.dot(var4) * (1.0 - Math.cos(var2))))
                .normalize();
        } else {
            return var5;
        }
    }

    public static Double sweptSphereFraction(
        YouziHomingFlight.Vector var0, YouziHomingFlight.Vector var1, YouziHomingFlight.Vector var2, YouziHomingFlight.Vector var3, double var4
    ) {
        YouziHomingFlight.Vector var6 = var0.subtract(var2);
        YouziHomingFlight.Vector var7 = var1.subtract(var0).subtract(var3.subtract(var2));
        double var8 = var6.dot(var6) - var4 * var4;
        if (var8 <= 0.0) {
            return 0.0;
        } else {
            double var10 = var7.dot(var7);
            double var12 = 2.0 * var6.dot(var7);
            if (var10 < 1.0E-15) {
                return null;
            } else {
                double var14 = var12 * var12 - 4.0 * var10 * var8;
                if (var14 < 0.0) {
                    return null;
                } else {
                    double var16 = (-var12 - Math.sqrt(var14)) / (2.0 * var10);
                    return var16 >= 0.0 && var16 <= 1.0 ? var16 : null;
                }
            }
        }
    }

    private static void requireFinite(double... var0) {
        for (double var4 : var0) {
            if (!Double.isFinite(var4)) {
                throw new IllegalArgumentException("Non-finite flight value");
            }
        }
    }

    public interface CollisionProbe {
        YouziHomingFlight.Impact firstImpact(YouziHomingFlight.Vector var1, YouziHomingFlight.Vector var2, double var3);
    }

    public static enum EndReason {
        ACTIVE,
        TARGET,
        BLOCK,
        RANGE,
        LIFETIME;
    }

    public static record Impact(double fraction, YouziHomingFlight.EndReason reason) {
        public Impact(double fraction, YouziHomingFlight.EndReason reason) {
            YouziHomingFlight.requireFinite(fraction);
            if (!(fraction < 0.0) && !(fraction > 1.0) && (reason == YouziHomingFlight.EndReason.BLOCK || reason == YouziHomingFlight.EndReason.TARGET)) {
                this.fraction = fraction;
                this.reason = reason;
            } else {
                throw new IllegalArgumentException("Invalid swept impact");
            }
        }
    }

    public static record Parameters(
        double initialSpeed,
        double maximumSpeed,
        double accelerationSeconds,
        double maximumTurnDegreesPerSecond,
        double predictionSeconds,
        double maximumDistance,
        double maximumLifetime,
        double fixedStepSeconds,
        double projectileRadius
    ) {
        public Parameters(
            double initialSpeed,
            double maximumSpeed,
            double accelerationSeconds,
            double maximumTurnDegreesPerSecond,
            double predictionSeconds,
            double maximumDistance,
            double maximumLifetime,
            double fixedStepSeconds,
            double projectileRadius
        ) {
            YouziHomingFlight.requireFinite(
                initialSpeed,
                maximumSpeed,
                accelerationSeconds,
                maximumTurnDegreesPerSecond,
                predictionSeconds,
                maximumDistance,
                maximumLifetime,
                fixedStepSeconds,
                projectileRadius
            );
            if (!(initialSpeed <= 0.0)
                && !(maximumSpeed < initialSpeed)
                && !(accelerationSeconds <= 0.0)
                && !(maximumTurnDegreesPerSecond <= 0.0)
                && !(predictionSeconds < 0.0)
                && !(maximumDistance <= 0.0)
                && !(maximumLifetime <= 0.0)
                && !(fixedStepSeconds <= 0.0)
                && !(projectileRadius < 0.0)) {
                this.initialSpeed = initialSpeed;
                this.maximumSpeed = maximumSpeed;
                this.accelerationSeconds = accelerationSeconds;
                this.maximumTurnDegreesPerSecond = maximumTurnDegreesPerSecond;
                this.predictionSeconds = predictionSeconds;
                this.maximumDistance = maximumDistance;
                this.maximumLifetime = maximumLifetime;
                this.fixedStepSeconds = fixedStepSeconds;
                this.projectileRadius = projectileRadius;
            } else {
                throw new IllegalArgumentException("Invalid Youzi flight parameters");
            }
        }
    }

    public static record Segment(YouziHomingFlight.Vector start, YouziHomingFlight.Vector end, double startAge, double endAge) {
    }

    public static record Snapshot(
        YouziHomingFlight.Vector position,
        YouziHomingFlight.Vector heading,
        double age,
        double distance,
        double speed,
        boolean targetLost,
        YouziHomingFlight.EndReason endReason
    ) {
    }

    public static record Target(YouziHomingFlight.Vector position, YouziHomingFlight.Vector velocity, double radius) {
        public Target(YouziHomingFlight.Vector position, YouziHomingFlight.Vector velocity, double radius) {
            YouziHomingFlight.requireFinite(radius);
            if (position != null && velocity != null && !(radius < 0.0)) {
                this.position = position;
                this.velocity = velocity;
                this.radius = radius;
            } else {
                throw new IllegalArgumentException("Invalid target");
            }
        }
    }

    public interface TargetProvider {
        YouziHomingFlight.Target at(double var1);
    }

    public static record Vector(double coordinateX, double coordinateY, double coordinateZ) {
        public Vector(double coordinateX, double coordinateY, double coordinateZ) {
            YouziHomingFlight.requireFinite(coordinateX, coordinateY, coordinateZ);
            this.coordinateX = coordinateX;
            this.coordinateY = coordinateY;
            this.coordinateZ = coordinateZ;
        }

        public YouziHomingFlight.Vector add(YouziHomingFlight.Vector var1) {
            return new YouziHomingFlight.Vector(this.coordinateX + var1.coordinateX, this.coordinateY + var1.coordinateY, this.coordinateZ + var1.coordinateZ);
        }

        public YouziHomingFlight.Vector subtract(YouziHomingFlight.Vector var1) {
            return new YouziHomingFlight.Vector(this.coordinateX - var1.coordinateX, this.coordinateY - var1.coordinateY, this.coordinateZ - var1.coordinateZ);
        }

        public YouziHomingFlight.Vector multiply(double var1) {
            return new YouziHomingFlight.Vector(this.coordinateX * var1, this.coordinateY * var1, this.coordinateZ * var1);
        }

        public double dot(YouziHomingFlight.Vector var1) {
            return this.coordinateX * var1.coordinateX + this.coordinateY * var1.coordinateY + this.coordinateZ * var1.coordinateZ;
        }

        public YouziHomingFlight.Vector cross(YouziHomingFlight.Vector var1) {
            return new YouziHomingFlight.Vector(
                this.coordinateY * var1.coordinateZ - this.coordinateZ * var1.coordinateY,
                this.coordinateZ * var1.coordinateX - this.coordinateX * var1.coordinateZ,
                this.coordinateX * var1.coordinateY - this.coordinateY * var1.coordinateX
            );
        }

        public double length() {
            return Math.sqrt(this.dot(this));
        }

        public YouziHomingFlight.Vector normalize() {
            double var1 = this.length();
            return var1 > 1.0E-10 ? this.multiply(1.0 / var1) : new YouziHomingFlight.Vector(0.0, 0.0, -1.0);
        }

        public YouziHomingFlight.Vector lerp(YouziHomingFlight.Vector var1, double var2) {
            return this.add(var1.subtract(this).multiply(var2));
        }
    }
}
