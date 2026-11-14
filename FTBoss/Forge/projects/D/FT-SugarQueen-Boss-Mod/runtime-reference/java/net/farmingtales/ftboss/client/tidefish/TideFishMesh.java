/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  org.joml.Matrix4f
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.client.tidefish;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class TideFishMesh {
    public final List<Part> parts;
    private final JsonObject animations;
    private static final String[] FACES = new String[]{"north", "south", "west", "east", "up", "down"};
    private static final int[][] CORNERS = new int[][]{{0, 3, 2, 1}, {4, 5, 6, 7}, {0, 4, 7, 3}, {1, 2, 6, 5}, {3, 7, 6, 2}, {0, 1, 5, 4}};
    private static final float[][] NORMALS = new float[][]{{0.0f, 0.0f, -1.0f}, {0.0f, 0.0f, 1.0f}, {-1.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 0.0f}, {0.0f, 1.0f, 0.0f}, {0.0f, -1.0f, 0.0f}};

    public TideFishMesh(Reader reader, Reader reader2) {
        JsonObject jsonObject = JsonParser.parseReader((Reader)reader).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
        float f = jsonObject.getAsJsonObject("description").get("texture_width").getAsFloat();
        float f2 = jsonObject.getAsJsonObject("description").get("texture_height").getAsFloat();
        ArrayList<Part> arrayList = new ArrayList<Part>();
        for (JsonElement object : jsonObject.getAsJsonArray("bones")) {
            JsonObject jsonObject2 = object.getAsJsonObject();
            if (!jsonObject2.has("cubes")) continue;
            ArrayList<Quad> arrayList2 = new ArrayList<Quad>();
            for (JsonElement jsonElement : jsonObject2.getAsJsonArray("cubes")) {
                Object object2;
                int n;
                JsonObject jsonObject3 = jsonElement.getAsJsonObject();
                float[] fArray = TideFishMesh.vector(jsonObject3.getAsJsonArray("origin"));
                float[] fArray2 = TideFishMesh.vector(jsonObject3.getAsJsonArray("size"));
                float[] fArray3 = TideFishMesh.vector(jsonObject3.getAsJsonArray("pivot"));
                float[] fArray4 = TideFishMesh.vector(jsonObject3.getAsJsonArray("rotation"));
                if (fArray2[0] <= 0.0f || fArray2[1] <= 0.0f || fArray2[2] <= 0.0f) {
                    throw new IllegalArgumentException("Non-positive cube");
                }
                Matrix4f matrix4f = new Matrix4f().translation(fArray3[0] / 16.0f, fArray3[1] / 16.0f, fArray3[2] / 16.0f).rotateXYZ(TideFishMesh.radians(fArray4[0]), TideFishMesh.radians(fArray4[1]), TideFishMesh.radians(fArray4[2])).translate(-fArray3[0] / 16.0f, -fArray3[1] / 16.0f, -fArray3[2] / 16.0f);
                float[][] fArray5 = new float[8][3];
                for (n = 0; n < 8; ++n) {
                    boolean bl = n == 2 || n == 3 || n == 6 || n == 7;
                    boolean bl2 = n == 1 || n == 2 || n == 5 || n == 6;
                    object2 = matrix4f.transformPosition(new Vector3f((fArray[0] + (bl2 ? fArray2[0] : 0.0f)) / 16.0f, (fArray[1] + (bl ? fArray2[1] : 0.0f)) / 16.0f, (fArray[2] + (n >= 4 ? fArray2[2] : 0.0f)) / 16.0f));
                    fArray5[n] = new float[]{object2.x, object2.y, object2.z};
                }
                for (n = 0; n < FACES.length; ++n) {
                    JsonObject jsonObject4 = jsonObject3.getAsJsonObject("uv").getAsJsonObject(FACES[n]);
                    float[] fArray6 = TideFishMesh.vector2(jsonObject4.getAsJsonArray("uv"));
                    object2 = TideFishMesh.vector2(jsonObject4.getAsJsonArray("uv_size"));
                    if (fArray6[0] < 0.0f || fArray6[1] < 0.0f || fArray6[0] + object2[0] > f || fArray6[1] + object2[1] > f2) {
                        throw new IllegalArgumentException("UV out of bounds");
                    }
                    float[][] fArray7 = new float[4][3];
                    for (int i = 0; i < 4; ++i) {
                        fArray7[i] = fArray5[CORNERS[n][i]];
                    }
                    float f3 = fArray6[0] / f;
                    float f4 = fArray6[1] / f2;
                    float f5 = (fArray6[0] + object2[0]) / f;
                    float f6 = (fArray6[1] + object2[1]) / f2;
                    Vector3f vector3f = matrix4f.transformDirection(new Vector3f(NORMALS[n]));
                    arrayList2.add(new Quad(fArray7, new float[][]{{f3, f6}, {f5, f6}, {f5, f4}, {f3, f4}}, new float[]{vector3f.x, vector3f.y, vector3f.z}));
                }
            }
            arrayList.add(new Part(jsonObject2.get("name").getAsString(), List.copyOf(arrayList2)));
        }
        this.parts = List.copyOf(arrayList);
        this.animations = JsonParser.parseReader((Reader)reader2).getAsJsonObject().getAsJsonObject("animations");
        for (String string : List.of("charge", "release", "pulse", "beam")) {
            if (this.animations.has("animation.tidefish." + string)) continue;
            throw new IllegalArgumentException("Missing phase: " + string);
        }
    }

    public Matrix4f pose(String string, String string2, float f) {
        JsonObject jsonObject = this.animations.getAsJsonObject("animation.tidefish." + string).getAsJsonObject("bones");
        Matrix4f matrix4f = new Matrix4f();
        TideFishMesh.apply(matrix4f, jsonObject.getAsJsonObject("root"), f);
        TideFishMesh.apply(matrix4f, jsonObject.getAsJsonObject(string2), f);
        return matrix4f;
    }

    private static void apply(Matrix4f matrix4f, JsonObject jsonObject, float f) {
        if (jsonObject == null) {
            return;
        }
        float[] fArray = TideFishMesh.sample(jsonObject, "position", f, 0.0f);
        float[] fArray2 = TideFishMesh.sample(jsonObject, "rotation", f, 0.0f);
        float[] fArray3 = TideFishMesh.sample(jsonObject, "scale", f, 1.0f);
        matrix4f.translate(fArray[0] / 16.0f, fArray[1] / 16.0f, fArray[2] / 16.0f).rotateXYZ(TideFishMesh.radians(fArray2[0]), TideFishMesh.radians(fArray2[1]), TideFishMesh.radians(fArray2[2])).scale(fArray3[0], fArray3[1], fArray3[2]);
    }

    private static float[] sample(JsonObject jsonObject, String string, float f, float f2) {
        if (!jsonObject.has(string)) {
            return new float[]{f2, f2, f2};
        }
        JsonElement jsonElement = jsonObject.get(string);
        if (jsonElement.isJsonArray()) {
            return TideFishMesh.vector(jsonElement.getAsJsonArray());
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        jsonElement.getAsJsonObject().entrySet().stream().sorted((entry, entry2) -> Float.compare(Float.parseFloat((String)entry.getKey()), Float.parseFloat((String)entry2.getKey()))).forEach(entry -> linkedHashMap.put(Float.valueOf(Float.parseFloat((String)entry.getKey())), TideFishMesh.vector(((JsonElement)entry.getValue()).getAsJsonArray())));
        float f3 = ((Float)linkedHashMap.keySet().iterator().next()).floatValue();
        float[] fArray = (float[])linkedHashMap.get(Float.valueOf(f3));
        if (f <= f3) {
            return fArray;
        }
        for (Map.Entry entry3 : linkedHashMap.entrySet()) {
            if (((Float)entry3.getKey()).floatValue() >= f) {
                float f4 = (f - f3) / (((Float)entry3.getKey()).floatValue() - f3);
                return new float[]{fArray[0] + (((float[])entry3.getValue())[0] - fArray[0]) * f4, fArray[1] + (((float[])entry3.getValue())[1] - fArray[1]) * f4, fArray[2] + (((float[])entry3.getValue())[2] - fArray[2]) * f4};
            }
            f3 = ((Float)entry3.getKey()).floatValue();
            fArray = (float[])entry3.getValue();
        }
        return fArray;
    }

    private static float radians(float f) {
        return f * (float)Math.PI / 180.0f;
    }

    private static float[] vector(JsonArray jsonArray) {
        float[] fArray;
        for (float f : fArray = new float[]{jsonArray.get(0).getAsFloat(), jsonArray.get(1).getAsFloat(), jsonArray.get(2).getAsFloat()}) {
            if (Float.isFinite(f)) continue;
            throw new IllegalArgumentException("Non-finite vector");
        }
        return fArray;
    }

    private static float[] vector2(JsonArray jsonArray) {
        return new float[]{jsonArray.get(0).getAsFloat(), jsonArray.get(1).getAsFloat()};
    }

    public record Quad(float[][] points, float[][] uv, float[] normal) {
    }

    public record Part(String name, List<Quad> quads) {
    }
}

