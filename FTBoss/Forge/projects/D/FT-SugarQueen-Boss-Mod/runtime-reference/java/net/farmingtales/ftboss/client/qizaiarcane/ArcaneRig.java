/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package net.farmingtales.ftboss.client.qizaiarcane;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ArcaneRig {
    private ArcaneRig() {
    }

    private static double finite(double d) {
        if (!Double.isFinite(d) || Math.abs(d) > 64.0) {
            throw new IllegalArgumentException("Invalid arcane scalar");
        }
        return d;
    }

    private static double[] vector(JsonArray jsonArray, int n) {
        if (jsonArray.size() != n) {
            throw new IllegalArgumentException("Invalid arcane vector");
        }
        double[] dArray = new double[n];
        for (int i = 0; i < n; ++i) {
            dArray[i] = ArcaneRig.finite(jsonArray.get(i).getAsDouble());
        }
        return dArray;
    }

    private static List<Moment> motion(JsonArray jsonArray, int n, boolean bl) {
        ArrayList<Moment> arrayList = new ArrayList<Moment>();
        double d = -1.0;
        for (JsonElement jsonElement : jsonArray) {
            JsonArray jsonArray2 = jsonElement.getAsJsonArray();
            double d2 = ArcaneRig.finite(jsonArray2.get(0).getAsDouble());
            double[] dArray = ArcaneRig.vector(jsonArray2.get(1).getAsJsonArray(), n);
            if (d2 < d || d2 < 0.0 || d2 > 3.0) {
                throw new IllegalArgumentException("Unordered arcane timing");
            }
            if (bl && (dArray[0] < 0.0 || dArray[0] > 1.0)) {
                throw new IllegalArgumentException("Opacity outside range");
            }
            arrayList.add(new Moment(d2, dArray));
            d = d2;
        }
        return List.copyOf(arrayList);
    }

    public static Effect read(Reader reader) {
        JsonObject jsonObject = JsonParser.parseReader((Reader)reader).getAsJsonObject();
        if (jsonObject.get("format").getAsInt() != 3) {
            throw new IllegalArgumentException("Expected ritual format 3");
        }
        ArrayList<Bone> arrayList = new ArrayList<Bone>();
        int n = 0;
        for (JsonElement jsonElement : jsonObject.getAsJsonArray("parts")) {
            Object object;
            Object object2;
            JsonObject jsonObject2;
            JsonObject jsonObject4 = jsonElement.getAsJsonObject();
            HashMap<String, Motion> hashMap = new HashMap<String, Motion>();
            for (String object32 : List.of("charge", "idle", "release")) {
                JsonObject jsonObject3 = jsonObject4.getAsJsonObject("tracks").getAsJsonObject(object32);
                jsonObject2 = ArcaneRig.spin(jsonObject3.getAsJsonArray("rotation"));
                object2 = ArcaneRig.motion(jsonObject3.getAsJsonArray("scale"), 3, false);
                object = object2.iterator();
                while (object.hasNext()) {
                    Moment n2 = (Moment)object.next();
                    for (double d : n2.data()) {
                        if (!(d <= 0.0) && !(d > 2.0)) continue;
                        throw new IllegalArgumentException("Invalid arcane scale");
                    }
                }
                hashMap.put(object32, new Motion(ArcaneRig.motion(jsonObject3.getAsJsonArray("position"), 3, false), (List<Moment>)jsonObject2, (List<Moment>)object2, ArcaneRig.motion(jsonObject3.getAsJsonArray("opacity"), 1, true)));
            }
            ArrayList arrayList2 = new ArrayList();
            for (JsonObject jsonObject3 : jsonObject4.getAsJsonArray("faces")) {
                double[] dArray;
                double d;
                jsonObject2 = jsonObject3.getAsJsonObject();
                object2 = new double[4][3];
                object = new double[4][2];
                int n2 = jsonObject2.get("material").getAsInt();
                if (n2 < 0 || n2 > 15) {
                    throw new IllegalArgumentException("Invalid arcane tile");
                }
                for (int i = 0; i < 4; ++i) {
                    object2[i] = ArcaneRig.vector(jsonObject2.getAsJsonArray("points").get(i).getAsJsonArray(), 3);
                    dArray = ArcaneRig.vector(jsonObject2.getAsJsonArray("uv").get(i).getAsJsonArray(), 2);
                    for (double d2 : dArray) {
                        if (!(d2 < 0.0) && !(d2 > 1.0)) continue;
                        throw new IllegalArgumentException("Invalid arcane UV");
                    }
                    object[i][0] = ((double)(n2 % 4 * 16 + 2) + dArray[0] * 11.0) / 64.0;
                    object[i][1] = ((double)(n2 / 4 * 16 + 2) + dArray[1] * 11.0) / 64.0;
                }
                double[] i = ArcaneRig.difference((double[])object2[1], (double[])object2[0]);
                double[] dArray2 = ArcaneRig.cross(i, dArray = ArcaneRig.difference((double[])object2[2], (double[])object2[0]));
                d = Math.sqrt(ArcaneRig.dot(dArray2, dArray2));
                if (d < 1.0E-10) {
                    throw new IllegalArgumentException("Degenerate arcane quad");
                }
                int d2 = 0;
                while (d2 < 3) {
                    int n3 = d2++;
                    dArray2[n3] = dArray2[n3] / d;
                }
                arrayList2.add(new Quad((double[][])object2, (double[][])object, dArray2));
                ++n;
            }
            double d = ArcaneRig.finite(jsonObject4.get("opacity").getAsDouble());
            if (d < 0.0 || d > 1.0) {
                throw new IllegalArgumentException("Invalid base opacity");
            }
            arrayList.add(new Bone(jsonObject4.get("name").getAsString(), ArcaneRig.vector(jsonObject4.getAsJsonArray("pivot"), 3), jsonObject4.get("detail").getAsBoolean(), jsonObject4.get("cue").getAsBoolean(), jsonObject4.get("emissive").getAsBoolean(), d, Map.copyOf(hashMap), List.copyOf(arrayList2)));
        }
        if (arrayList.isEmpty() || arrayList.size() > 96 || n > 4000) {
            throw new IllegalArgumentException("Arcane asset over budget");
        }
        return new Effect(jsonObject.get("id").getAsString(), jsonObject.get("texture").getAsString(), jsonObject.get("cue").getAsInt(), ArcaneRig.finite(jsonObject.get("radius").getAsDouble()), List.copyOf(arrayList));
    }

    public static double[] sample(List<Moment> list, double d, double[] dArray) {
        if (list.isEmpty()) {
            return (double[])dArray.clone();
        }
        Moment moment = list.get(0);
        if (d <= moment.time()) {
            return (double[])moment.data().clone();
        }
        for (int i = 1; i < list.size(); ++i) {
            Moment moment2 = list.get(i);
            if (!(d > moment2.time())) {
                double d2 = (d - moment.time()) / Math.max(1.0E-10, moment2.time() - moment.time());
                double[] dArray2 = new double[dArray.length];
                for (int j = 0; j < dArray2.length; ++j) {
                    dArray2[j] = moment.data()[j] + d2 * (moment2.data()[j] - moment.data()[j]);
                }
                return dArray2;
            }
            moment = moment2;
        }
        return (double[])list.get(list.size() - 1).data().clone();
    }

    public static int emit(Effect effect, String string, double d, boolean bl, boolean bl2, boolean bl3, int n, Emitter emitter) {
        int n2 = 0;
        for (boolean bl4 : new boolean[]{true, false}) {
            for (Bone bone : effect.bones()) {
                if (bone.cue() != bl4 || bone.cue() && !bl2 || !bone.cue() && !bl3 || bone.detail() && !bl) continue;
                Motion motion = bone.motion().get(string);
                double[] dArray = ArcaneRig.sample(motion.movement(), d, new double[3]);
                double[] dArray2 = ArcaneRig.sample(motion.rotation(), d, new double[3]);
                double[] dArray3 = ArcaneRig.sample(motion.size(), d, new double[]{1.0, 1.0, 1.0});
                float f = (float)(bone.opacity() * ArcaneRig.sample(motion.opacity(), d, new double[]{1.0})[0]);
                if ((double)f < 0.003 || bone.quads().size() > n - n2) continue;
                double[][] dArray4 = ArcaneRig.rotationMatrix(dArray2);
                for (int i = 0; i < bone.quads().size(); ++i) {
                    int n3;
                    Quad quad = bone.quads().get(i);
                    double[][] dArray5 = new double[4][3];
                    double[] dArray6 = new double[3];
                    double d2 = 0.0;
                    for (n3 = 0; n3 < 3; ++n3) {
                        dArray6[n3] = quad.normal()[n3] / dArray3[n3];
                        d2 += dArray6[n3] * dArray6[n3];
                    }
                    for (n3 = 0; n3 < 4; ++n3) {
                        double d3 = (quad.points()[n3][0] - bone.pivot()[0]) * dArray3[0];
                        double d4 = (quad.points()[n3][1] - bone.pivot()[1]) * dArray3[1];
                        double d5 = (quad.points()[n3][2] - bone.pivot()[2]) * dArray3[2];
                        for (int j = 0; j < 3; ++j) {
                            dArray5[n3][j] = dArray4[j][0] * d3 + dArray4[j][1] * d4 + dArray4[j][2] * d5 + bone.pivot()[j] + dArray[j] / 16.0;
                        }
                    }
                    d2 = Math.sqrt(d2);
                    double d6 = dArray6[0] / d2;
                    double d7 = dArray6[1] / d2;
                    double d8 = dArray6[2] / d2;
                    for (int j = 0; j < 3; ++j) {
                        dArray6[j] = dArray4[j][0] * d6 + dArray4[j][1] * d7 + dArray4[j][2] * d8;
                    }
                    emitter.quad(bone.name(), i, dArray5, quad.uv(), dArray6, f, bone.cue(), bone.light());
                    ++n2;
                }
            }
        }
        return n2;
    }

    public static double dot(double[] dArray, double[] dArray2) {
        return dArray[0] * dArray2[0] + dArray[1] * dArray2[1] + dArray[2] * dArray2[2];
    }

    private static List<Moment> spin(JsonArray jsonArray) {
        ArrayList<Moment> arrayList = new ArrayList<Moment>();
        double d = -1.0;
        for (JsonElement jsonElement : jsonArray) {
            JsonArray jsonArray2 = jsonElement.getAsJsonArray();
            double d2 = ArcaneRig.finite(jsonArray2.get(0).getAsDouble());
            JsonArray jsonArray3 = jsonArray2.get(1).getAsJsonArray();
            if (d2 < d || d2 < 0.0 || d2 > 3.0 || jsonArray3.size() != 3) {
                throw new IllegalArgumentException("Invalid spin timing");
            }
            double[] dArray = new double[3];
            for (int i = 0; i < 3; ++i) {
                dArray[i] = jsonArray3.get(i).getAsDouble();
                if (Double.isFinite(dArray[i]) && !(Math.abs(dArray[i]) > 720.0)) continue;
                throw new IllegalArgumentException("Invalid spin angle");
            }
            arrayList.add(new Moment(d2, dArray));
            d = d2;
        }
        return List.copyOf(arrayList);
    }

    public static double[] rotate(double[] dArray, double[] dArray2) {
        return ArcaneRig.rotate(dArray, ArcaneRig.rotationMatrix(dArray2));
    }

    private static double[] rotate(double[] dArray, double[][] dArray2) {
        double[] dArray3 = new double[3];
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                int n = i;
                dArray3[n] = dArray3[n] + dArray2[i][j] * dArray[j];
            }
        }
        return dArray3;
    }

    private static double[][] rotationMatrix(double[] dArray) {
        double d = Math.toRadians(dArray[0]);
        double d2 = Math.toRadians(dArray[1]);
        double d3 = Math.toRadians(dArray[2]);
        double d4 = Math.sin(d);
        double d5 = Math.cos(d);
        double d6 = Math.sin(d2);
        double d7 = Math.cos(d2);
        double d8 = Math.sin(d3);
        double d9 = Math.cos(d3);
        return new double[][]{{d9 * d7, d9 * d6 * d4 - d8 * d5, d9 * d6 * d5 + d8 * d4}, {d8 * d7, d8 * d6 * d4 + d9 * d5, d8 * d6 * d5 - d9 * d4}, {-d6, d7 * d4, d7 * d5}};
    }

    public static double[] difference(double[] dArray, double[] dArray2) {
        return new double[]{dArray[0] - dArray2[0], dArray[1] - dArray2[1], dArray[2] - dArray2[2]};
    }

    public static double[] cross(double[] dArray, double[] dArray2) {
        return new double[]{dArray[1] * dArray2[2] - dArray[2] * dArray2[1], dArray[2] * dArray2[0] - dArray[0] * dArray2[2], dArray[0] * dArray2[1] - dArray[1] * dArray2[0]};
    }

    public record Moment(double time, double[] data) {
    }

    public record Motion(List<Moment> movement, List<Moment> rotation, List<Moment> size, List<Moment> opacity) {
    }

    public record Quad(double[][] points, double[][] uv, double[] normal) {
    }

    public record Bone(String name, double[] pivot, boolean detail, boolean cue, boolean light, double opacity, Map<String, Motion> motion, List<Quad> quads) {
    }

    public record Effect(String id, String texture, int cue, double radius, List<Bone> bones) {
    }

    @FunctionalInterface
    public static interface Emitter {
        public void quad(String var1, int var2, double[][] var3, double[][] var4, double[] var5, float var6, boolean var7, boolean var8);
    }
}

