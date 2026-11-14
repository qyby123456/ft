/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  org.joml.Quaterniond
 *  org.joml.Quaterniondc
 *  org.joml.Vector3d
 */
package net.farmingtales.ftboss.client.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import net.farmingtales.ftboss.util.EasterCombatMath;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;

public final class GongdiBasicAttackSampler {
    private GongdiBasicAttackSampler() {
    }

    private static double[] numbers(JsonArray jsonArray, int n) {
        if (jsonArray.size() != n) {
            throw new IllegalArgumentException("Invalid bone channel width");
        }
        double[] dArray = new double[n];
        for (int i = 0; i < n; ++i) {
            dArray[i] = jsonArray.get(i).getAsDouble();
            if (Double.isFinite(dArray[i])) continue;
            throw new IllegalArgumentException("Nonfinite rig coordinate");
        }
        return dArray;
    }

    private static Map<String, Frame[]> load() {
        Map map;
        block13: {
            InputStream inputStream = GongdiBasicAttackSampler.class.getResourceAsStream("/assets/ftboss/fx/gongdi_basic_attacks.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Gongdi basic rig resource missing");
                }
                JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                LinkedHashMap<String, Frame[]> linkedHashMap = new LinkedHashMap<String, Frame[]>();
                for (JsonElement jsonElement : jsonObject.getAsJsonArray("clips")) {
                    JsonObject jsonObject2 = jsonElement.getAsJsonObject();
                    JsonArray jsonArray = jsonObject2.getAsJsonArray("frames");
                    Frame[] frameArray = new Frame[jsonArray.size()];
                    double d = -1.0;
                    for (int i = 0; i < frameArray.length; ++i) {
                        JsonObject jsonObject3 = jsonArray.get(i).getAsJsonObject();
                        double d2 = jsonObject3.get("time").getAsDouble();
                        if (d2 <= d || d2 < 0.0 || d2 > 1.0) {
                            throw new IllegalArgumentException("Invalid rig timeline");
                        }
                        d = d2;
                        LinkedHashMap<String, Bone> linkedHashMap2 = new LinkedHashMap<String, Bone>();
                        for (Map.Entry entry : jsonObject3.getAsJsonObject("bones").entrySet()) {
                            JsonObject jsonObject4 = ((JsonElement)entry.getValue()).getAsJsonObject();
                            linkedHashMap2.put((String)entry.getKey(), new Bone(GongdiBasicAttackSampler.numbers(jsonObject4.getAsJsonArray("position"), 3), GongdiBasicAttackSampler.numbers(jsonObject4.getAsJsonArray("quaternion"), 4)));
                        }
                        frameArray[i] = new Frame(d2, Map.copyOf(linkedHashMap2));
                    }
                    linkedHashMap.put(jsonObject2.get("id").getAsString(), frameArray);
                }
                map = Map.copyOf(linkedHashMap);
                if (inputStream == null) break block13;
            }
            catch (Throwable throwable) {
                try {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Exception exception) {
                    throw new IllegalStateException("Cannot load Gongdi basic attack prototype", exception);
                }
            }
            inputStream.close();
        }
        return map;
    }

    public static Frame sample(String string, double d) {
        int n;
        Frame[] frameArray = Library.CLIPS.getOrDefault(string, Library.CLIPS.get("held_idle"));
        if (!Double.isFinite(d)) {
            d = 0.0;
        }
        d = Math.max(0.0, Math.min(1.0, d));
        for (n = 1; n < frameArray.length - 1 && d > frameArray[n].time(); ++n) {
        }
        Frame frame = frameArray[n - 1];
        Frame frame2 = frameArray[n];
        double d2 = (d - frame.time()) / Math.max(1.0E-6, frame2.time() - frame.time());
        LinkedHashMap<String, Bone> linkedHashMap = new LinkedHashMap<String, Bone>();
        for (Map.Entry<String, Bone> entry : frame.bones().entrySet()) {
            Bone bone = entry.getValue();
            Bone bone2 = frame2.bones().get(entry.getKey());
            double[] dArray = new double[3];
            for (int i = 0; i < 3; ++i) {
                dArray[i] = bone.position()[i] + (bone2.position()[i] - bone.position()[i]) * d2;
            }
            Quaterniond quaterniond = new Quaterniond(bone.quaternion()[0], bone.quaternion()[1], bone.quaternion()[2], bone.quaternion()[3]).slerp((Quaterniondc)new Quaterniond(bone2.quaternion()[0], bone2.quaternion()[1], bone2.quaternion()[2], bone2.quaternion()[3]), d2);
            linkedHashMap.put(entry.getKey(), new Bone(dArray, new double[]{quaterniond.x, quaterniond.y, quaterniond.z, quaterniond.w}));
        }
        return new Frame(d, Map.copyOf(linkedHashMap));
    }

    public static Frame fromExistingImpactCue(String string, float f, int n, boolean bl) {
        return GongdiBasicAttackSampler.sample(string, EasterCombatMath.progress(f, n, bl));
    }

    public static double[] transform(double[] dArray, Bone bone) {
        Vector3d vector3d = new Vector3d(dArray[0], dArray[1], dArray[2]);
        new Quaterniond(bone.quaternion()[0], bone.quaternion()[1], bone.quaternion()[2], bone.quaternion()[3]).transform(vector3d);
        return new double[]{vector3d.x + bone.position()[0], vector3d.y + bone.position()[1], vector3d.z + bone.position()[2]};
    }

    public record Frame(double time, Map<String, Bone> bones) {
    }

    public record Bone(double[] position, double[] quaternion) {
    }

    private static final class Library {
        private static final Map<String, Frame[]> CLIPS = GongdiBasicAttackSampler.load();

        private Library() {
        }
    }
}

