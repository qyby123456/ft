/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.youzi;

import java.util.ArrayList;
import java.util.List;

public final class YouziHomingFlight {
    private final Parameters parameters;
    private Vector position;
    private Vector heading;
    private double age;
    private double distance;
    private double speed;
    private boolean targetLost;
    private EndReason endReason = EndReason.ACTIVE;

    public YouziHomingFlight(Parameters parameters, Vector vector, Vector vector2) {
        if (parameters == null || vector == null || vector2 == null) {
            throw new IllegalArgumentException("Missing flight state");
        }
        this.parameters = parameters;
        this.position = vector;
        this.heading = vector2.normalize();
        this.speed = parameters.initialSpeed();
    }

    public Snapshot snapshot() {
        return new Snapshot(this.position, this.heading, this.age, this.distance, this.speed, this.targetLost, this.endReason);
    }

    public List<Segment> advance(double d, TargetProvider targetProvider, CollisionProbe collisionProbe) {
        return this.advance(d, targetProvider, collisionProbe, true);
    }

    public List<Segment> advance(double d, TargetProvider targetProvider, CollisionProbe collisionProbe, boolean bl) {
        YouziHomingFlight.requireFinite(d);
        if (d < 0.0 || targetProvider == null || collisionProbe == null) {
            throw new IllegalArgumentException("Invalid flight step");
        }
        ArrayList<Segment> arrayList = new ArrayList<Segment>();
        double d2 = d;
        while (d2 > 1.0E-10 && this.endReason == EndReason.ACTIVE) {
            double d3;
            Target target;
            double d4 = Math.min(Math.min(this.parameters.fixedStepSeconds(), d2), this.parameters.maximumLifetime() - this.age);
            if (d4 <= 1.0E-10) {
                this.endReason = EndReason.LIFETIME;
                break;
            }
            Vector vector = this.position;
            double d5 = this.age;
            Target target2 = target = this.targetLost ? null : targetProvider.at(this.age);
            if (target == null) {
                this.targetLost = true;
            }
            this.speed = this.parameters.initialSpeed() + (this.parameters.maximumSpeed() - this.parameters.initialSpeed()) * Math.min(1.0, (this.age + d4 * 0.5) / this.parameters.accelerationSeconds());
            if (target != null) {
                d3 = Math.min(this.parameters.predictionSeconds(), target.position().subtract(this.position).length() / this.speed);
                Vector vector2 = target.position().add(target.velocity().multiply(d3));
                this.heading = YouziHomingFlight.turnTowards(this.heading, vector2.subtract(this.position), Math.toRadians(this.parameters.maximumTurnDegreesPerSecond()) * d4);
            }
            d3 = Math.min(this.speed * d4, this.parameters.maximumDistance() - this.distance);
            double d6 = d3 / this.speed;
            Vector vector3 = this.position.add(this.heading.multiply(d3));
            Target target3 = target == null ? null : targetProvider.at(this.age + d6);
            Double d7 = !bl || target3 == null ? null : YouziHomingFlight.sweptSphereFraction(vector, vector3, target.position(), target3.position(), target.radius() + this.parameters.projectileRadius());
            Impact impact = collisionProbe.firstImpact(vector, vector3, this.parameters.projectileRadius());
            double d8 = 1.0;
            if (d7 != null) {
                d8 = d7;
                this.endReason = EndReason.TARGET;
            }
            if (impact != null && impact.fraction() <= d8) {
                d8 = impact.fraction();
                this.endReason = impact.reason();
            }
            this.position = vector.lerp(vector3, d8);
            this.age += d6 * d8;
            this.distance += d3 * d8;
            arrayList.add(new Segment(vector, this.position, d5, this.age));
            d2 -= d4;
            if (this.endReason == EndReason.ACTIVE && this.distance >= this.parameters.maximumDistance() - 1.0E-10) {
                this.endReason = EndReason.RANGE;
            }
            if (this.endReason != EndReason.ACTIVE || !(this.age >= this.parameters.maximumLifetime() - 1.0E-10)) continue;
            this.endReason = EndReason.LIFETIME;
        }
        return List.copyOf(arrayList);
    }

    public static Vector turnTowards(Vector vector, Vector vector2, double d) {
        Vector vector3;
        Vector vector4 = vector.normalize();
        double d2 = Math.acos(Math.max(-1.0, Math.min(1.0, vector4.dot(vector3 = vector2.normalize()))));
        if (d2 <= d || d2 < 1.0E-10) {
            return vector3;
        }
        Vector vector5 = vector4.cross(vector3);
        if (vector5.length() < 1.0E-8) {
            vector5 = vector4.cross(Math.abs(vector4.coordinateX()) < 0.8 ? new Vector(1.0, 0.0, 0.0) : new Vector(0.0, 1.0, 0.0));
        }
        vector5 = vector5.normalize();
        return vector4.multiply(Math.cos(d)).add(vector5.cross(vector4).multiply(Math.sin(d))).add(vector5.multiply(vector5.dot(vector4) * (1.0 - Math.cos(d)))).normalize();
    }

    public static Double sweptSphereFraction(Vector vector, Vector vector2, Vector vector3, Vector vector4, double d) {
        Vector vector5 = vector.subtract(vector3);
        Vector vector6 = vector2.subtract(vector).subtract(vector4.subtract(vector3));
        double d2 = vector5.dot(vector5) - d * d;
        if (d2 <= 0.0) {
            return 0.0;
        }
        double d3 = vector6.dot(vector6);
        double d4 = 2.0 * vector5.dot(vector6);
        if (d3 < 1.0E-15) {
            return null;
        }
        double d5 = d4 * d4 - 4.0 * d3 * d2;
        if (d5 < 0.0) {
            return null;
        }
        double d6 = (-d4 - Math.sqrt(d5)) / (2.0 * d3);
        return d6 >= 0.0 && d6 <= 1.0 ? Double.valueOf(d6) : null;
    }

    private static void requireFinite(double ... dArray) {
        for (double d : dArray) {
            if (Double.isFinite(d)) continue;
            throw new IllegalArgumentException("Non-finite flight value");
        }
    }

    public static enum EndReason {
        ACTIVE,
        TARGET,
        BLOCK,
        RANGE,
        LIFETIME;

    }

    public record Parameters(double initialSpeed, double maximumSpeed, double accelerationSeconds, double maximumTurnDegreesPerSecond, double predictionSeconds, double maximumDistance, double maximumLifetime, double fixedStepSeconds, double projectileRadius) {
        public Parameters {
            YouziHomingFlight.requireFinite(d, d2, d3, d4, d5, d6, d7, d8, d9);
            if (d <= 0.0 || d2 < d || d3 <= 0.0 || d4 <= 0.0 || d5 < 0.0 || d6 <= 0.0 || d7 <= 0.0 || d8 <= 0.0 || d9 < 0.0) {
                throw new IllegalArgumentException("Invalid Youzi flight parameters");
            }
        }
    }

    public record Vector(double coordinateX, double coordinateY, double coordinateZ) {
        public Vector {
            YouziHomingFlight.requireFinite(d, d2, d3);
        }

        public Vector add(Vector vector) {
            return new Vector(this.coordinateX + vector.coordinateX, this.coordinateY + vector.coordinateY, this.coordinateZ + vector.coordinateZ);
        }

        public Vector subtract(Vector vector) {
            return new Vector(this.coordinateX - vector.coordinateX, this.coordinateY - vector.coordinateY, this.coordinateZ - vector.coordinateZ);
        }

        public Vector multiply(double d) {
            return new Vector(this.coordinateX * d, this.coordinateY * d, this.coordinateZ * d);
        }

        public double dot(Vector vector) {
            return this.coordinateX * vector.coordinateX + this.coordinateY * vector.coordinateY + this.coordinateZ * vector.coordinateZ;
        }

        public Vector cross(Vector vector) {
            return new Vector(this.coordinateY * vector.coordinateZ - this.coordinateZ * vector.coordinateY, this.coordinateZ * vector.coordinateX - this.coordinateX * vector.coordinateZ, this.coordinateX * vector.coordinateY - this.coordinateY * vector.coordinateX);
        }

        public double length() {
            return Math.sqrt(this.dot(this));
        }

        public Vector normalize() {
            double d = this.length();
            return d > 1.0E-10 ? this.multiply(1.0 / d) : new Vector(0.0, 0.0, -1.0);
        }

        public Vector lerp(Vector vector, double d) {
            return this.add(vector.subtract(this).multiply(d));
        }
    }

    public record Snapshot(Vector position, Vector heading, double age, double distance, double speed, boolean targetLost, EndReason endReason) {
    }

    public static interface TargetProvider {
        public Target at(double var1);
    }

    public static interface CollisionProbe {
        public Impact firstImpact(Vector var1, Vector var2, double var3);
    }

    public record Target(Vector position, Vector velocity, double radius) {
        public Target {
            YouziHomingFlight.requireFinite(d);
            if (vector == null || vector2 == null || d < 0.0) {
                throw new IllegalArgumentException("Invalid target");
            }
        }
    }

    public record Impact(double fraction, EndReason reason) {
        public Impact {
            YouziHomingFlight.requireFinite(d);
            if (d < 0.0 || d > 1.0 || endReason != EndReason.BLOCK && endReason != EndReason.TARGET) {
                throw new IllegalArgumentException("Invalid swept impact");
            }
        }
    }

    public record Segment(Vector start, Vector end, double startAge, double endAge) {
    }
}

