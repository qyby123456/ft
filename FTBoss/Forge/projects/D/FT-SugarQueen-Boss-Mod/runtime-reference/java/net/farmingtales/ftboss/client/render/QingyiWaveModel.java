/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

public final class QingyiWaveModel {
    private static final Asset ASSET = QingyiWaveModel.load("four_tides");
    private static final Map<String, Asset> ASSETS = new ConcurrentHashMap<String, Asset>();

    private QingyiWaveModel() {
    }

    public static Asset asset() {
        return ASSET;
    }

    public static Asset asset(String string) {
        return "four_tides".equals(string) ? ASSET : ASSETS.computeIfAbsent(string, QingyiWaveModel::load);
    }

    private static float[] numbers(JsonArray jsonArray, int n) {
        if (jsonArray == null || jsonArray.size() != n) {
            throw new IllegalArgumentException("Invalid Qingyi vector");
        }
        float[] fArray = new float[n];
        for (int i = 0; i < n; ++i) {
            fArray[i] = jsonArray.get(i).getAsFloat();
            if (Float.isFinite(fArray[i])) continue;
            throw new IllegalArgumentException("Non-finite Qingyi vector");
        }
        return fArray;
    }

    private static Key[] keys(JsonObject jsonObject, String string) {
        if (!jsonObject.has(string)) {
            return new Key[0];
        }
        JsonArray jsonArray = jsonObject.getAsJsonArray(string);
        Key[] keyArray = new Key[jsonArray.size()];
        float f = -1.0f;
        for (int i = 0; i < keyArray.length; ++i) {
            JsonObject jsonObject2 = jsonArray.get(i).getAsJsonObject();
            float f2 = jsonObject2.get("time").getAsFloat();
            if (!Float.isFinite(f2) || f2 < f) {
                throw new IllegalArgumentException("Unordered Qingyi animation");
            }
            keyArray[i] = new Key(f2, QingyiWaveModel.numbers(jsonObject2.getAsJsonArray("values"), 3));
            f = f2;
        }
        return keyArray;
    }

    private static Asset load(String string) {
        Object object;
        block18: {
            if (!Set.of("four_tides", "elixir_rain", "tidal_lotus", "undertow_snare", "abyssal_carapace", "riptide_corridor").contains(string)) {
                throw new IllegalArgumentException("Not an owned water asset");
            }
            InputStream inputStream = QingyiWaveModel.class.getResourceAsStream("/assets/ftboss/qingyi/" + string + ".mesh.json");
            try {
                int n;
                Face[] faceArray;
                if (inputStream == null) {
                    throw new IllegalStateException("Missing independent Qingyi wave asset");
                }
                JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                if (jsonObject.get("version").getAsInt() != 1) {
                    throw new IllegalArgumentException("Unknown Qingyi asset version");
                }
                JsonArray jsonArray = jsonObject.getAsJsonArray("bones");
                Bone[] boneArray = new Bone[jsonArray.size()];
                for (int i = 0; i < boneArray.length; ++i) {
                    faceArray = jsonArray.get(i).getAsJsonObject();
                    n = faceArray.get("parent").getAsInt();
                    if (n >= i || n < -1) {
                        throw new IllegalArgumentException("Cyclic Qingyi skeleton");
                    }
                    boneArray[i] = new Bone(faceArray.get("name").getAsString(), n, QingyiWaveModel.numbers(faceArray.getAsJsonArray("pivot"), 3));
                }
                JsonArray jsonArray2 = jsonObject.getAsJsonArray("faces");
                faceArray = new Face[jsonArray2.size()];
                for (n = 0; n < faceArray.length; ++n) {
                    object = jsonArray2.get(n).getAsJsonObject();
                    int n2 = object.get("bone").getAsInt();
                    float f = object.get("opacity").getAsFloat();
                    if (n2 < 0 || n2 >= boneArray.length || !(f > 0.0f) || !(f <= 1.0f)) {
                        throw new IllegalArgumentException("Invalid Qingyi face");
                    }
                    faceArray[n] = new Face(n2, QingyiWaveModel.numbers(object.getAsJsonArray("vertices"), 12), QingyiWaveModel.numbers(object.getAsJsonArray("uv"), 8), f, object.get("detail").getAsBoolean(), object.get("interior").getAsBoolean(), object.get("tile").getAsInt(), object.has("featurePivot") ? QingyiWaveModel.numbers(object.getAsJsonArray("featurePivot"), 3) : null, object.get("part").getAsString());
                }
                LinkedHashMap<String, Clip> linkedHashMap = new LinkedHashMap<String, Clip>();
                for (Map.Entry entry : jsonObject.getAsJsonObject("animations").entrySet()) {
                    JsonObject jsonObject2 = ((JsonElement)entry.getValue()).getAsJsonObject();
                    JsonArray jsonArray3 = jsonObject2.getAsJsonArray("tracks");
                    if (jsonArray3.size() != boneArray.length) {
                        throw new IllegalArgumentException("Incomplete Qingyi tracks");
                    }
                    Track[] trackArray = new Track[boneArray.length];
                    for (int i = 0; i < trackArray.length; ++i) {
                        JsonObject jsonObject3 = jsonArray3.get(i).getAsJsonObject();
                        trackArray[i] = new Track(QingyiWaveModel.keys(jsonObject3, "position"), QingyiWaveModel.keys(jsonObject3, "rotation"), QingyiWaveModel.keys(jsonObject3, "scale"));
                    }
                    linkedHashMap.put((String)entry.getKey(), new Clip(jsonObject2.get("length").getAsFloat(), trackArray));
                }
                object = new Asset(jsonObject.get("width").getAsFloat(), boneArray, faceArray, Map.copyOf(linkedHashMap));
                if (inputStream == null) break block18;
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
                    throw new IllegalStateException("Cannot read independent Qingyi model", exception);
                }
            }
            inputStream.close();
        }
        return object;
    }

    public static float[] sample(Key[] keyArray, float f, float f2) {
        if (keyArray.length == 0) {
            return new float[]{f2, f2, f2};
        }
        if (f <= keyArray[0].time()) {
            return (float[])keyArray[0].values().clone();
        }
        for (int i = 1; i < keyArray.length; ++i) {
            if (f > keyArray[i].time()) continue;
            Key key = keyArray[i - 1];
            Key key2 = keyArray[i];
            float f3 = key2.time() == key.time() ? 1.0f : (f - key.time()) / (key2.time() - key.time());
            float[] fArray = new float[3];
            for (int j = 0; j < 3; ++j) {
                fArray[j] = key.values()[j] + (key2.values()[j] - key.values()[j]) * f3;
            }
            return fArray;
        }
        return (float[])keyArray[keyArray.length - 1].values().clone();
    }

    public static Matrix4f[] pose(String string, float f) {
        return QingyiWaveModel.pose(QingyiWaveModel.asset(), string, f);
    }

    public static Matrix4f[] pose(Asset asset, String string, float f) {
        Clip clip = asset.clips().get(string);
        if (clip == null) {
            throw new IllegalArgumentException("Unknown Qingyi clip " + string);
        }
        f = Math.max(0.0f, Math.min(clip.length(), f));
        Matrix4f[] matrix4fArray = new Matrix4f[asset.bones().length];
        for (int i = 0; i < matrix4fArray.length; ++i) {
            Bone bone = asset.bones()[i];
            Track track = clip.tracks()[i];
            float[] fArray = QingyiWaveModel.sample(track.position(), f, 0.0f);
            float[] fArray2 = QingyiWaveModel.sample(track.rotation(), f, 0.0f);
            float[] fArray3 = QingyiWaveModel.sample(track.scale(), f, 1.0f);
            float[] fArray4 = bone.pivot();
            Matrix4f matrix4f = new Matrix4f().translation(fArray4[0] + fArray[0], fArray4[1] + fArray[1], fArray4[2] + fArray[2]);
            matrix4f.rotateZYX((float)Math.toRadians(fArray2[2]), (float)Math.toRadians(fArray2[1]), (float)Math.toRadians(fArray2[0]));
            matrix4f.scale(fArray3[0], fArray3[1], fArray3[2]).translate(-fArray4[0], -fArray4[1], -fArray4[2]);
            matrix4fArray[i] = bone.parent() < 0 ? matrix4f : new Matrix4f((Matrix4fc)matrix4fArray[bone.parent()]).mul((Matrix4fc)matrix4f);
        }
        return matrix4fArray;
    }

    public static Vector3f vertex(Face face, int n, Matrix4f[] matrix4fArray, Vector3f vector3f) {
        int n2 = n * 3;
        return matrix4fArray[face.bone()].transformPosition(vector3f.set(face.vertices()[n2], face.vertices()[n2 + 1], face.vertices()[n2 + 2]));
    }

    public record Asset(float width, Bone[] bones, Face[] faces, Map<String, Clip> clips) {
    }

    public record Key(float time, float[] values) {
    }

    public record Bone(String name, int parent, float[] pivot) {
    }

    public record Face(int bone, float[] vertices, float[] uv, float opacity, boolean detail, boolean interior, int tile, float[] featurePivot, String part) {
    }

    public record Track(Key[] position, Key[] rotation, Key[] scale) {
    }

    public record Clip(float length, Track[] tracks) {
    }
}

