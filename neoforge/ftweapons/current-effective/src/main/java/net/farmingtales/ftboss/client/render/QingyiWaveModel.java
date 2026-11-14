package net.farmingtales.ftboss.client.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class QingyiWaveModel {
    private static final QingyiWaveModel.Asset ASSET = load("four_tides");
    private static final Map<String, QingyiWaveModel.Asset> ASSETS = new ConcurrentHashMap<>();

    private QingyiWaveModel() {
    }

    public static QingyiWaveModel.Asset asset() {
        return ASSET;
    }

    public static QingyiWaveModel.Asset asset(String var0) {
        return "four_tides".equals(var0) ? ASSET : ASSETS.computeIfAbsent(var0, QingyiWaveModel::load);
    }

    private static float[] numbers(JsonArray var0, int var1) {
        if (var0 != null && var0.size() == var1) {
            float[] var2 = new float[var1];

            for (int var3 = 0; var3 < var1; var3++) {
                var2[var3] = var0.get(var3).getAsFloat();
                if (!Float.isFinite(var2[var3])) {
                    throw new IllegalArgumentException("Non-finite Qingyi vector");
                }
            }

            return var2;
        } else {
            throw new IllegalArgumentException("Invalid Qingyi vector");
        }
    }

    private static QingyiWaveModel.Key[] keys(JsonObject var0, String var1) {
        if (!var0.has(var1)) {
            return new QingyiWaveModel.Key[0];
        } else {
            JsonArray var2 = var0.getAsJsonArray(var1);
            QingyiWaveModel.Key[] var3 = new QingyiWaveModel.Key[var2.size()];
            float var4 = -1.0F;

            for (int var5 = 0; var5 < var3.length; var5++) {
                JsonObject var6 = var2.get(var5).getAsJsonObject();
                float var7 = var6.get("time").getAsFloat();
                if (!Float.isFinite(var7) || var7 < var4) {
                    throw new IllegalArgumentException("Unordered Qingyi animation");
                }

                var3[var5] = new QingyiWaveModel.Key(var7, numbers(var6.getAsJsonArray("values"), 3));
                var4 = var7;
            }

            return var3;
        }
    }

    private static QingyiWaveModel.Asset load(String var0) {
        if (!Set.of("four_tides", "elixir_rain", "tidal_lotus", "undertow_snare", "abyssal_carapace", "riptide_corridor").contains(var0)) {
            throw new IllegalArgumentException("Not an owned water asset");
        } else {
            try {
                QingyiWaveModel.Asset var23;
                try (InputStream var1 = QingyiWaveModel.class.getResourceAsStream("/assets/ftboss/qingyi/" + var0 + ".mesh.json")) {
                    if (var1 == null) {
                        throw new IllegalStateException("Missing independent Qingyi wave asset");
                    }

                    JsonObject var2 = JsonParser.parseReader(new InputStreamReader(var1, StandardCharsets.UTF_8)).getAsJsonObject();
                    if (var2.get("version").getAsInt() != 1) {
                        throw new IllegalArgumentException("Unknown Qingyi asset version");
                    }

                    JsonArray var3 = var2.getAsJsonArray("bones");
                    QingyiWaveModel.Bone[] var4 = new QingyiWaveModel.Bone[var3.size()];

                    for (int var5 = 0; var5 < var4.length; var5++) {
                        JsonObject var6 = var3.get(var5).getAsJsonObject();
                        int var7 = var6.get("parent").getAsInt();
                        if (var7 >= var5 || var7 < -1) {
                            throw new IllegalArgumentException("Cyclic Qingyi skeleton");
                        }

                        var4[var5] = new QingyiWaveModel.Bone(var6.get("name").getAsString(), var7, numbers(var6.getAsJsonArray("pivot"), 3));
                    }

                    JsonArray var18 = var2.getAsJsonArray("faces");
                    QingyiWaveModel.Face[] var19 = new QingyiWaveModel.Face[var18.size()];

                    for (int var20 = 0; var20 < var19.length; var20++) {
                        JsonObject var8 = var18.get(var20).getAsJsonObject();
                        int var9 = var8.get("bone").getAsInt();
                        float var10 = var8.get("opacity").getAsFloat();
                        if (var9 < 0 || var9 >= var4.length || !(var10 > 0.0F) || !(var10 <= 1.0F)) {
                            throw new IllegalArgumentException("Invalid Qingyi face");
                        }

                        var19[var20] = new QingyiWaveModel.Face(
                            var9,
                            numbers(var8.getAsJsonArray("vertices"), 12),
                            numbers(var8.getAsJsonArray("uv"), 8),
                            var10,
                            var8.get("detail").getAsBoolean(),
                            var8.get("interior").getAsBoolean(),
                            var8.get("tile").getAsInt(),
                            var8.has("featurePivot") ? numbers(var8.getAsJsonArray("featurePivot"), 3) : null,
                            var8.get("part").getAsString()
                        );
                    }

                    LinkedHashMap var21 = new LinkedHashMap();

                    for (Entry var24 : var2.getAsJsonObject("animations").entrySet()) {
                        JsonObject var25 = ((JsonElement)var24.getValue()).getAsJsonObject();
                        JsonArray var11 = var25.getAsJsonArray("tracks");
                        if (var11.size() != var4.length) {
                            throw new IllegalArgumentException("Incomplete Qingyi tracks");
                        }

                        QingyiWaveModel.Track[] var12 = new QingyiWaveModel.Track[var4.length];

                        for (int var13 = 0; var13 < var12.length; var13++) {
                            JsonObject var14 = var11.get(var13).getAsJsonObject();
                            var12[var13] = new QingyiWaveModel.Track(keys(var14, "position"), keys(var14, "rotation"), keys(var14, "scale"));
                        }

                        var21.put((String)var24.getKey(), new QingyiWaveModel.Clip(var25.get("length").getAsFloat(), var12));
                    }

                    var23 = new QingyiWaveModel.Asset(var2.get("width").getAsFloat(), var4, var19, Map.copyOf(var21));
                }

                return var23;
            } catch (Exception var17) {
                throw new IllegalStateException("Cannot read independent Qingyi model", var17);
            }
        }
    }

    public static float[] sample(QingyiWaveModel.Key[] var0, float var1, float var2) {
        if (var0.length == 0) {
            return new float[]{var2, var2, var2};
        } else if (var1 <= var0[0].time()) {
            return (float[])var0[0].values().clone();
        } else {
            for (int var3 = 1; var3 < var0.length; var3++) {
                if (!(var1 > var0[var3].time())) {
                    QingyiWaveModel.Key var4 = var0[var3 - 1];
                    QingyiWaveModel.Key var5 = var0[var3];
                    float var6 = var5.time() == var4.time() ? 1.0F : (var1 - var4.time()) / (var5.time() - var4.time());
                    float[] var7 = new float[3];

                    for (int var8 = 0; var8 < 3; var8++) {
                        var7[var8] = var4.values()[var8] + (var5.values()[var8] - var4.values()[var8]) * var6;
                    }

                    return var7;
                }
            }

            return (float[])var0[var0.length - 1].values().clone();
        }
    }

    public static Matrix4f[] pose(String var0, float var1) {
        return pose(asset(), var0, var1);
    }

    public static Matrix4f[] pose(QingyiWaveModel.Asset var0, String var1, float var2) {
        QingyiWaveModel.Clip var3 = var0.clips().get(var1);
        if (var3 == null) {
            throw new IllegalArgumentException("Unknown Qingyi clip " + var1);
        } else {
            var2 = Math.max(0.0F, Math.min(var3.length(), var2));
            Matrix4f[] var4 = new Matrix4f[var0.bones().length];

            for (int var5 = 0; var5 < var4.length; var5++) {
                QingyiWaveModel.Bone var6 = var0.bones()[var5];
                QingyiWaveModel.Track var7 = var3.tracks()[var5];
                float[] var8 = sample(var7.position(), var2, 0.0F);
                float[] var9 = sample(var7.rotation(), var2, 0.0F);
                float[] var10 = sample(var7.scale(), var2, 1.0F);
                float[] var11 = var6.pivot();
                Matrix4f var12 = new Matrix4f().translation(var11[0] + var8[0], var11[1] + var8[1], var11[2] + var8[2]);
                var12.rotateZYX((float)Math.toRadians((double)var9[2]), (float)Math.toRadians((double)var9[1]), (float)Math.toRadians((double)var9[0]));
                var12.scale(var10[0], var10[1], var10[2]).translate(-var11[0], -var11[1], -var11[2]);
                var4[var5] = var6.parent() < 0 ? var12 : new Matrix4f(var4[var6.parent()]).mul(var12);
            }

            return var4;
        }
    }

    public static Vector3f vertex(QingyiWaveModel.Face var0, int var1, Matrix4f[] var2, Vector3f var3) {
        int var4 = var1 * 3;
        return var2[var0.bone()].transformPosition(var3.set(var0.vertices()[var4], var0.vertices()[var4 + 1], var0.vertices()[var4 + 2]));
    }

    public static record Asset(float width, QingyiWaveModel.Bone[] bones, QingyiWaveModel.Face[] faces, Map<String, QingyiWaveModel.Clip> clips) {
    }

    public static record Bone(String name, int parent, float[] pivot) {
    }

    public static record Clip(float length, QingyiWaveModel.Track[] tracks) {
    }

    public static record Face(
        int bone, float[] vertices, float[] uv, float opacity, boolean detail, boolean interior, int tile, float[] featurePivot, String part
    ) {
    }

    public static record Key(float time, float[] values) {
    }

    public static record Track(QingyiWaveModel.Key[] position, QingyiWaveModel.Key[] rotation, QingyiWaveModel.Key[] scale) {
    }
}
