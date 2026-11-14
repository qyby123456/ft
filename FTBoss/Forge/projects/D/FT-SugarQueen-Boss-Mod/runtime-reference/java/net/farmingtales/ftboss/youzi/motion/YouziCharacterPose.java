/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package net.farmingtales.ftboss.youzi.motion;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;

public final class YouziCharacterPose {
    private static final String[] CHANNELS = new String[]{"body", "rightArm", "leftArm", "head"};
    private static final Vector ZERO = new Vector(0.0, 0.0, 0.0);
    private static final Vector HIP = new Vector(0.0, 12.0, 0.0);
    private static final Vector RIGHT_SHOULDER = new Vector(-5.0, 2.5, 0.0);
    private static final Vector LEFT_SHOULDER = new Vector(5.0, 2.5, 0.0);
    private static final Bone RIGHT_LEG = new Bone(new Vector(-1.9, 12.0, 0.0), ZERO);
    private static final Bone LEFT_LEG = new Bone(new Vector(1.9, 12.0, 0.0), ZERO);
    private final Map<String, Clip> clips = new LinkedHashMap<String, Clip>();
    private final Map<String, String> weapons = new LinkedHashMap<String, String>();

    public YouziCharacterPose(JsonObject jsonObject) {
        if (jsonObject == null || !jsonObject.has("schema") || !"youzi-character-motion/v1".equals(jsonObject.get("schema").getAsString())) {
            throw new IllegalArgumentException("Unsupported Youzi pose schema");
        }
        if (!jsonObject.has("clips") || !jsonObject.get("clips").isJsonArray() || jsonObject.getAsJsonArray("clips").size() == 0) {
            throw new IllegalArgumentException("Missing Youzi clips");
        }
        for (JsonElement jsonElement : jsonObject.getAsJsonArray("clips")) {
            double d;
            int n;
            boolean bl;
            JsonObject jsonObject2 = jsonElement.getAsJsonObject();
            String string = jsonObject2.get("id").getAsString();
            String string2 = jsonObject2.get("weapon").getAsString();
            double d2 = jsonObject2.get("duration").getAsDouble();
            double d3 = jsonObject2.get("impactPhase").getAsDouble();
            boolean bl2 = bl = !jsonObject2.has("category") || jsonObject2.get("category").getAsString().equals("melee");
            if (string.isBlank() || string2.isBlank() || this.clips.containsKey(string) || bl && this.weapons.containsKey(string2) || !Double.isFinite(d2) || d2 <= 0.0 || !Double.isFinite(d3) || d3 <= 0.0 || d3 >= 1.0) {
                throw new IllegalArgumentException("Invalid or duplicate Youzi clip");
            }
            JsonArray jsonArray = jsonObject2.getAsJsonArray("keys");
            if (jsonArray == null || jsonArray.size() < 2) {
                throw new IllegalArgumentException("Insufficient Youzi keyframes");
            }
            double[] dArray = new double[jsonArray.size()];
            Vector[][] vectorArray = new Vector[CHANNELS.length][jsonArray.size()];
            Vector[][] vectorArray2 = new Vector[CHANNELS.length][jsonArray.size()];
            for (n = 0; n < jsonArray.size(); ++n) {
                JsonObject jsonObject3 = jsonArray.get(n).getAsJsonObject();
                double d4 = jsonObject3.get("phase").getAsDouble();
                if (!Double.isFinite(d4) || d4 < 0.0 || d4 > 1.0 || n > 0 && d4 <= dArray[n - 1]) {
                    throw new IllegalArgumentException("Invalid Youzi phase ordering");
                }
                dArray[n] = d4;
                for (int i = 0; i < CHANNELS.length; ++i) {
                    vectorArray[i][n] = YouziCharacterPose.vector(jsonObject3.get(CHANNELS[i]));
                }
            }
            if (dArray[0] != 0.0 || dArray[dArray.length - 1] != 1.0) {
                throw new IllegalArgumentException("Youzi keys must cover the complete clip");
            }
            for (n = 0; n < CHANNELS.length; ++n) {
                for (int i = 0; i < jsonArray.size(); ++i) {
                    vectorArray2[n][i] = YouziCharacterPose.tangent(dArray, vectorArray[n], i);
                }
            }
            double d5 = d = jsonObject2.has("chargeEnd") ? jsonObject2.get("chargeEnd").getAsDouble() : d3;
            if (!Double.isFinite(d) || d <= 0.0 || d >= 1.0) {
                throw new IllegalArgumentException("Invalid charge split");
            }
            this.clips.put(string, new Clip(d3, d2, d, dArray, vectorArray, vectorArray2));
            if (!bl) continue;
            this.weapons.put(string2, string);
        }
    }

    public String forWeapon(String string) {
        return this.weapons.get(string);
    }

    public double impactPhase(String string) {
        return this.clip(string).impactPhase();
    }

    public double duration(String string) {
        return this.clip(string).duration();
    }

    public double chargeEnd(String string) {
        return this.clip(string).chargeEnd();
    }

    public Vector readyRotation(String string) {
        return this.clip(string).values()[1][0];
    }

    private Clip clip(String string) {
        Clip clip = this.clips.get(string);
        if (clip == null) {
            throw new IllegalArgumentException("Unknown Youzi character clip");
        }
        return clip;
    }

    private static Vector vector(JsonElement jsonElement) {
        if (jsonElement == null || !jsonElement.isJsonArray() || jsonElement.getAsJsonArray().size() != 3) {
            throw new IllegalArgumentException("Expected three bone coordinates");
        }
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        return new Vector(jsonArray.get(0).getAsDouble(), jsonArray.get(1).getAsDouble(), jsonArray.get(2).getAsDouble());
    }

    public Map<String, Bone> sample(String string, double d, double d2, Vector vector) {
        Object object;
        Vector vector2;
        int n;
        if (!Double.isFinite(d) || !Double.isFinite(d2) || d2 < 0.0 || d2 > 1.0 || vector == null) {
            throw new IllegalArgumentException("Invalid pose phase/blend/look");
        }
        double d3 = Math.max(0.0, Math.min(1.0, d));
        Clip clip = this.clip(string);
        for (n = 0; n < clip.phases().length - 1 && clip.phases()[n] < d3; ++n) {
        }
        int n2 = Math.max(0, n - 1);
        double d4 = clip.phases()[n] - clip.phases()[n2];
        double d5 = d4 == 0.0 ? 0.0 : (d3 - clip.phases()[n2]) / d4;
        double d6 = d5 * d5;
        double d7 = d6 * d5;
        Vector[] vectorArray = new Vector[CHANNELS.length];
        for (int i = 0; i < CHANNELS.length; ++i) {
            vector2 = clip.values()[i][n2];
            object = clip.values()[i][n];
            Vector vector3 = vector2.multiply(2.0 * d7 - 3.0 * d6 + 1.0).add(clip.tangents()[i][n2].multiply((d7 - 2.0 * d6 + d5) * d4)).add(((Vector)object).multiply(-2.0 * d7 + 3.0 * d6)).add(clip.tangents()[i][n].multiply((d7 - d6) * d4));
            Vector vector4 = i == 1 ? clip.values()[i][0] : ZERO;
            vectorArray[i] = vector4.add(vector3.subtract(vector4).multiply(d2));
        }
        Vector vector5 = vectorArray[0];
        vector2 = HIP.subtract(YouziCharacterPose.rotate(HIP, vector5));
        object = new LinkedHashMap();
        object.put("body", new Bone(vector2, vector5));
        object.put("head", new Bone(vector2, YouziCharacterPose.compose(vector5, vectorArray[3].add(vector))));
        object.put("rightArm", new Bone(vector2.add(YouziCharacterPose.rotate(RIGHT_SHOULDER, vector5)), YouziCharacterPose.compose(vector5, vectorArray[1])));
        object.put("leftArm", new Bone(vector2.add(YouziCharacterPose.rotate(LEFT_SHOULDER, vector5)), YouziCharacterPose.compose(vector5, vectorArray[2])));
        object.put("rightLeg", RIGHT_LEG);
        object.put("leftLeg", LEFT_LEG);
        return object;
    }

    private static Vector tangent(double[] dArray, Vector[] vectorArray, int n) {
        if (n <= 0 || n >= dArray.length - 1) {
            return ZERO;
        }
        double d = dArray[n] - dArray[n - 1];
        double d2 = dArray[n + 1] - dArray[n];
        Vector vector = vectorArray[n].subtract(vectorArray[n - 1]).multiply(1.0 / d);
        Vector vector2 = vectorArray[n + 1].subtract(vectorArray[n]).multiply(1.0 / d2);
        return new Vector(YouziCharacterPose.tangent(vector.coordinateX(), vector2.coordinateX(), d, d2), YouziCharacterPose.tangent(vector.coordinateY(), vector2.coordinateY(), d, d2), YouziCharacterPose.tangent(vector.coordinateZ(), vector2.coordinateZ(), d, d2));
    }

    private static double tangent(double d, double d2, double d3, double d4) {
        if (d * d2 <= 0.0) {
            return 0.0;
        }
        double d5 = 2.0 * d4 + d3;
        double d6 = d4 + 2.0 * d3;
        return (d5 + d6) / (d5 / d + d6 / d2);
    }

    public static Vector rotate(Vector vector, Vector vector2) {
        double[] dArray = new double[]{vector.coordinateX(), vector.coordinateY(), vector.coordinateZ()};
        double[] dArray2 = new double[]{vector2.coordinateX(), vector2.coordinateY(), vector2.coordinateZ()};
        for (int i = 0; i < 3; ++i) {
            double d = Math.toRadians(dArray2[i]);
            double d2 = Math.cos(d);
            double d3 = Math.sin(d);
            int n = (i + 1) % 3;
            int n2 = (i + 2) % 3;
            double d4 = dArray[n];
            double d5 = dArray[n2];
            dArray[n] = d4 * d2 - d5 * d3;
            dArray[n2] = d4 * d3 + d5 * d2;
        }
        return new Vector(dArray[0], dArray[1], dArray[2]);
    }

    private static Vector compose(Vector vector, Vector vector2) {
        Vector vector3 = YouziCharacterPose.rotate(YouziCharacterPose.rotate(new Vector(1.0, 0.0, 0.0), vector2), vector);
        Vector vector4 = YouziCharacterPose.rotate(YouziCharacterPose.rotate(new Vector(0.0, 1.0, 0.0), vector2), vector);
        Vector vector5 = YouziCharacterPose.rotate(YouziCharacterPose.rotate(new Vector(0.0, 0.0, 1.0), vector2), vector);
        return new Vector(Math.toDegrees(Math.atan2(vector4.coordinateZ(), vector5.coordinateZ())), Math.toDegrees(Math.asin(Math.max(-1.0, Math.min(1.0, -vector3.coordinateZ())))), Math.toDegrees(Math.atan2(vector3.coordinateY(), vector3.coordinateX())));
    }

    public record Vector(double coordinateX, double coordinateY, double coordinateZ) {
        public Vector {
            if (!(Double.isFinite(d) && Double.isFinite(d2) && Double.isFinite(d3))) {
                throw new IllegalArgumentException("Non-finite bone vector");
            }
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
    }

    private record Clip(double impactPhase, double duration, double chargeEnd, double[] phases, Vector[][] values, Vector[][] tangents) {
    }

    public record Bone(Vector position, Vector rotation) {
    }
}

