package net.farmingtales.ftboss.youzi.motion;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;

public final class YouziCharacterPose {
    private static final String[] CHANNELS = new String[]{"body", "rightArm", "leftArm", "head"};
    private static final YouziCharacterPose.Vector ZERO = new YouziCharacterPose.Vector(0.0, 0.0, 0.0);
    private static final YouziCharacterPose.Vector HIP = new YouziCharacterPose.Vector(0.0, 12.0, 0.0);
    private static final YouziCharacterPose.Vector RIGHT_SHOULDER = new YouziCharacterPose.Vector(-5.0, 2.5, 0.0);
    private static final YouziCharacterPose.Vector LEFT_SHOULDER = new YouziCharacterPose.Vector(5.0, 2.5, 0.0);
    private static final YouziCharacterPose.Bone RIGHT_LEG = new YouziCharacterPose.Bone(new YouziCharacterPose.Vector(-1.9, 12.0, 0.0), ZERO);
    private static final YouziCharacterPose.Bone LEFT_LEG = new YouziCharacterPose.Bone(new YouziCharacterPose.Vector(1.9, 12.0, 0.0), ZERO);
    private final Map<String, YouziCharacterPose.Clip> clips = new LinkedHashMap<>();
    private final Map<String, String> weapons = new LinkedHashMap<>();

    public YouziCharacterPose(JsonObject var1) {
        if (var1 != null && var1.has("schema") && "youzi-character-motion/v1".equals(var1.get("schema").getAsString())) {
            if (var1.has("clips") && var1.get("clips").isJsonArray() && var1.getAsJsonArray("clips").size() != 0) {
                for (JsonElement var3 : var1.getAsJsonArray("clips")) {
                    JsonObject var4 = var3.getAsJsonObject();
                    String var5 = var4.get("id").getAsString();
                    String var6 = var4.get("weapon").getAsString();
                    double var7 = var4.get("duration").getAsDouble();
                    double var9 = var4.get("impactPhase").getAsDouble();
                    boolean var11 = !var4.has("category") || var4.get("category").getAsString().equals("melee");
                    if (!var5.isBlank()
                        && !var6.isBlank()
                        && !this.clips.containsKey(var5)
                        && (!var11 || !this.weapons.containsKey(var6))
                        && Double.isFinite(var7)
                        && !(var7 <= 0.0)
                        && Double.isFinite(var9)
                        && !(var9 <= 0.0)
                        && !(var9 >= 1.0)) {
                        JsonArray var12 = var4.getAsJsonArray("keys");
                        if (var12 != null && var12.size() >= 2) {
                            double[] var13 = new double[var12.size()];
                            YouziCharacterPose.Vector[][] var14 = new YouziCharacterPose.Vector[CHANNELS.length][var12.size()];
                            YouziCharacterPose.Vector[][] var15 = new YouziCharacterPose.Vector[CHANNELS.length][var12.size()];

                            for (int var16 = 0; var16 < var12.size(); var16++) {
                                JsonObject var17 = var12.get(var16).getAsJsonObject();
                                double var18 = var17.get("phase").getAsDouble();
                                if (!Double.isFinite(var18) || var18 < 0.0 || var18 > 1.0 || var16 > 0 && var18 <= var13[var16 - 1]) {
                                    throw new IllegalArgumentException("Invalid Youzi phase ordering");
                                }

                                var13[var16] = var18;

                                for (int var20 = 0; var20 < CHANNELS.length; var20++) {
                                    var14[var20][var16] = vector(var17.get(CHANNELS[var20]));
                                }
                            }

                            if (var13[0] == 0.0 && var13[var13.length - 1] == 1.0) {
                                for (int var21 = 0; var21 < CHANNELS.length; var21++) {
                                    for (int var23 = 0; var23 < var12.size(); var23++) {
                                        var15[var21][var23] = tangent(var13, var14[var21], var23);
                                    }
                                }

                                double var22 = var4.has("chargeEnd") ? var4.get("chargeEnd").getAsDouble() : var9;
                                if (Double.isFinite(var22) && !(var22 <= 0.0) && !(var22 >= 1.0)) {
                                    this.clips.put(var5, new YouziCharacterPose.Clip(var9, var7, var22, var13, var14, var15));
                                    if (var11) {
                                        this.weapons.put(var6, var5);
                                    }
                                    continue;
                                }

                                throw new IllegalArgumentException("Invalid charge split");
                            }

                            throw new IllegalArgumentException("Youzi keys must cover the complete clip");
                        }

                        throw new IllegalArgumentException("Insufficient Youzi keyframes");
                    }

                    throw new IllegalArgumentException("Invalid or duplicate Youzi clip");
                }
            } else {
                throw new IllegalArgumentException("Missing Youzi clips");
            }
        } else {
            throw new IllegalArgumentException("Unsupported Youzi pose schema");
        }
    }

    public String forWeapon(String var1) {
        return this.weapons.get(var1);
    }

    public double impactPhase(String var1) {
        return this.clip(var1).impactPhase();
    }

    public double duration(String var1) {
        return this.clip(var1).duration();
    }

    public double chargeEnd(String var1) {
        return this.clip(var1).chargeEnd();
    }

    public YouziCharacterPose.Vector readyRotation(String var1) {
        return this.clip(var1).values()[1][0];
    }

    private YouziCharacterPose.Clip clip(String var1) {
        YouziCharacterPose.Clip var2 = this.clips.get(var1);
        if (var2 == null) {
            throw new IllegalArgumentException("Unknown Youzi character clip");
        } else {
            return var2;
        }
    }

    private static YouziCharacterPose.Vector vector(JsonElement var0) {
        if (var0 != null && var0.isJsonArray() && var0.getAsJsonArray().size() == 3) {
            JsonArray var1 = var0.getAsJsonArray();
            return new YouziCharacterPose.Vector(var1.get(0).getAsDouble(), var1.get(1).getAsDouble(), var1.get(2).getAsDouble());
        } else {
            throw new IllegalArgumentException("Expected three bone coordinates");
        }
    }

    public Map<String, YouziCharacterPose.Bone> sample(String var1, double var2, double var4, YouziCharacterPose.Vector var6) {
        if (Double.isFinite(var2) && Double.isFinite(var4) && !(var4 < 0.0) && !(var4 > 1.0) && var6 != null) {
            double var7 = Math.max(0.0, Math.min(1.0, var2));
            YouziCharacterPose.Clip var9 = this.clip(var1);
            int var10 = 0;

            while (var10 < var9.phases().length - 1 && var9.phases()[var10] < var7) {
                var10++;
            }

            int var11 = Math.max(0, var10 - 1);
            double var12 = var9.phases()[var10] - var9.phases()[var11];
            double var14 = var12 == 0.0 ? 0.0 : (var7 - var9.phases()[var11]) / var12;
            double var16 = var14 * var14;
            double var18 = var16 * var14;
            YouziCharacterPose.Vector[] var20 = new YouziCharacterPose.Vector[CHANNELS.length];

            for (int var21 = 0; var21 < CHANNELS.length; var21++) {
                YouziCharacterPose.Vector var22 = var9.values()[var21][var11];
                YouziCharacterPose.Vector var23 = var9.values()[var21][var10];
                YouziCharacterPose.Vector var24 = var22.multiply(2.0 * var18 - 3.0 * var16 + 1.0)
                    .add(var9.tangents()[var21][var11].multiply((var18 - 2.0 * var16 + var14) * var12))
                    .add(var23.multiply(-2.0 * var18 + 3.0 * var16))
                    .add(var9.tangents()[var21][var10].multiply((var18 - var16) * var12));
                YouziCharacterPose.Vector var25 = var21 == 1 ? var9.values()[var21][0] : ZERO;
                var20[var21] = var25.add(var24.subtract(var25).multiply(var4));
            }

            YouziCharacterPose.Vector var26 = var20[0];
            YouziCharacterPose.Vector var27 = HIP.subtract(rotate(HIP, var26));
            LinkedHashMap var28 = new LinkedHashMap();
            var28.put("body", new YouziCharacterPose.Bone(var27, var26));
            var28.put("head", new YouziCharacterPose.Bone(var27, compose(var26, var20[3].add(var6))));
            var28.put("rightArm", new YouziCharacterPose.Bone(var27.add(rotate(RIGHT_SHOULDER, var26)), compose(var26, var20[1])));
            var28.put("leftArm", new YouziCharacterPose.Bone(var27.add(rotate(LEFT_SHOULDER, var26)), compose(var26, var20[2])));
            var28.put("rightLeg", RIGHT_LEG);
            var28.put("leftLeg", LEFT_LEG);
            return var28;
        } else {
            throw new IllegalArgumentException("Invalid pose phase/blend/look");
        }
    }

    private static YouziCharacterPose.Vector tangent(double[] var0, YouziCharacterPose.Vector[] var1, int var2) {
        if (var2 > 0 && var2 < var0.length - 1) {
            double var3 = var0[var2] - var0[var2 - 1];
            double var5 = var0[var2 + 1] - var0[var2];
            YouziCharacterPose.Vector var7 = var1[var2].subtract(var1[var2 - 1]).multiply(1.0 / var3);
            YouziCharacterPose.Vector var8 = var1[var2 + 1].subtract(var1[var2]).multiply(1.0 / var5);
            return new YouziCharacterPose.Vector(
                tangent(var7.coordinateX(), var8.coordinateX(), var3, var5),
                tangent(var7.coordinateY(), var8.coordinateY(), var3, var5),
                tangent(var7.coordinateZ(), var8.coordinateZ(), var3, var5)
            );
        } else {
            return ZERO;
        }
    }

    private static double tangent(double var0, double var2, double var4, double var6) {
        if (var0 * var2 <= 0.0) {
            return 0.0;
        } else {
            double var8 = 2.0 * var6 + var4;
            double var10 = var6 + 2.0 * var4;
            return (var8 + var10) / (var8 / var0 + var10 / var2);
        }
    }

    public static YouziCharacterPose.Vector rotate(YouziCharacterPose.Vector var0, YouziCharacterPose.Vector var1) {
        double[] var2 = new double[]{var0.coordinateX(), var0.coordinateY(), var0.coordinateZ()};
        double[] var3 = new double[]{var1.coordinateX(), var1.coordinateY(), var1.coordinateZ()};

        for (int var4 = 0; var4 < 3; var4++) {
            double var5 = Math.toRadians(var3[var4]);
            double var7 = Math.cos(var5);
            double var9 = Math.sin(var5);
            int var11 = (var4 + 1) % 3;
            int var12 = (var4 + 2) % 3;
            double var13 = var2[var11];
            double var15 = var2[var12];
            var2[var11] = var13 * var7 - var15 * var9;
            var2[var12] = var13 * var9 + var15 * var7;
        }

        return new YouziCharacterPose.Vector(var2[0], var2[1], var2[2]);
    }

    private static YouziCharacterPose.Vector compose(YouziCharacterPose.Vector var0, YouziCharacterPose.Vector var1) {
        YouziCharacterPose.Vector var2 = rotate(rotate(new YouziCharacterPose.Vector(1.0, 0.0, 0.0), var1), var0);
        YouziCharacterPose.Vector var3 = rotate(rotate(new YouziCharacterPose.Vector(0.0, 1.0, 0.0), var1), var0);
        YouziCharacterPose.Vector var4 = rotate(rotate(new YouziCharacterPose.Vector(0.0, 0.0, 1.0), var1), var0);
        return new YouziCharacterPose.Vector(
            Math.toDegrees(Math.atan2(var3.coordinateZ(), var4.coordinateZ())),
            Math.toDegrees(Math.asin(Math.max(-1.0, Math.min(1.0, -var2.coordinateZ())))),
            Math.toDegrees(Math.atan2(var2.coordinateY(), var2.coordinateX()))
        );
    }

    public static record Bone(YouziCharacterPose.Vector position, YouziCharacterPose.Vector rotation) {
    }

    private static record Clip(
        double impactPhase, double duration, double chargeEnd, double[] phases, YouziCharacterPose.Vector[][] values, YouziCharacterPose.Vector[][] tangents
    ) {
    }

    public static record Vector(double coordinateX, double coordinateY, double coordinateZ) {
        public Vector(double coordinateX, double coordinateY, double coordinateZ) {
            if (Double.isFinite(coordinateX) && Double.isFinite(coordinateY) && Double.isFinite(coordinateZ)) {
                this.coordinateX = coordinateX;
                this.coordinateY = coordinateY;
                this.coordinateZ = coordinateZ;
            } else {
                throw new IllegalArgumentException("Non-finite bone vector");
            }
        }

        public YouziCharacterPose.Vector add(YouziCharacterPose.Vector var1) {
            return new YouziCharacterPose.Vector(this.coordinateX + var1.coordinateX, this.coordinateY + var1.coordinateY, this.coordinateZ + var1.coordinateZ);
        }

        public YouziCharacterPose.Vector subtract(YouziCharacterPose.Vector var1) {
            return new YouziCharacterPose.Vector(this.coordinateX - var1.coordinateX, this.coordinateY - var1.coordinateY, this.coordinateZ - var1.coordinateZ);
        }

        public YouziCharacterPose.Vector multiply(double var1) {
            return new YouziCharacterPose.Vector(this.coordinateX * var1, this.coordinateY * var1, this.coordinateZ * var1);
        }
    }
}
