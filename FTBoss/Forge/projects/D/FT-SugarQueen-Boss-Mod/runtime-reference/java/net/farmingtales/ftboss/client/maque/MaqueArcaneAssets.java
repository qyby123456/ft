/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.annotations.SerializedName
 */
package net.farmingtales.ftboss.client.maque;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public final class MaqueArcaneAssets {
    private static final Map<String, Art> LIBRARY = MaqueArcaneAssets.read();

    private MaqueArcaneAssets() {
    }

    public static Art get(String string) {
        return LIBRARY.get(string);
    }

    static void groupMaterials(Surface[] surfaceArray) {
        Arrays.sort(surfaceArray, Comparator.comparingInt(surface -> surface.material() >= 4 ? 0 : 1));
    }

    private static Map<String, Art> read() {
        Map<String, Art> map;
        block19: {
            InputStream inputStream = MaqueArcaneAssets.class.getResourceAsStream("/assets/ftboss/maque_arcane/arcane_models.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Missing arcane assets");
                }
                Library library = (Library)new Gson().fromJson((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8), Library.class);
                if (library.schema != 2 || library.models.length != 3) {
                    throw new IllegalStateException("Invalid arcane library");
                }
                HashMap<String, Art> hashMap = new HashMap<String, Art>();
                for (Art art : library.models) {
                    if (art.bones.length < 1 || art.bones.length > 8 || art.faces.length > 14000 || art.lite.length >= art.faces.length || art.lite.length == 0) {
                        throw new IllegalStateException("Invalid art budget");
                    }
                    MaqueArcaneAssets.requirePoint(art.offset);
                    if (!Double.isFinite(art.scale) || art.scale < 0.5 || art.scale > 1.5) {
                        throw new IllegalStateException("Invalid art scale");
                    }
                    for (Bone bone : art.bones) {
                        MaqueArcaneAssets.requirePoint(bone.pivot);
                        if (bone.name != null && bone.role != null && bone.role.kind != null) continue;
                        throw new IllegalStateException("Invalid bone");
                    }
                    Bone[] boneArray = new Surface[][]{art.faces, art.lite};
                    int n = boneArray.length;
                    for (int i = 0; i < n; ++i) {
                        Bone bone;
                        for (Bone bone2 : bone = boneArray[i]) {
                            if (((Surface)((Object)bone2)).points.length != 4 || ((Surface)((Object)bone2)).bone < 0 || ((Surface)((Object)bone2)).bone >= art.bones.length || ((Surface)((Object)bone2)).material < 0 || ((Surface)((Object)bone2)).material > 5 || !Double.isFinite(((Surface)((Object)bone2)).opacity) || ((Surface)((Object)bone2)).opacity <= 0.0 || ((Surface)((Object)bone2)).opacity > 1.0) {
                                throw new IllegalStateException("Invalid luminous surface");
                            }
                            for (double[] dArray : ((Surface)((Object)bone2)).points) {
                                MaqueArcaneAssets.requirePoint(dArray);
                            }
                        }
                    }
                    MaqueArcaneAssets.groupMaterials(art.faces);
                    MaqueArcaneAssets.groupMaterials(art.lite);
                    if (hashMap.put(art.id, art) == null) continue;
                    throw new IllegalStateException("Duplicate arcane spell");
                }
                for (String string : new String[]{"feather_fan", "wingstep", "feather_refuge"}) {
                    if (hashMap.containsKey(string)) continue;
                    throw new IllegalStateException("Incomplete arcane library");
                }
                map = Map.copyOf(hashMap);
                if (inputStream == null) break block19;
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
                    throw new IllegalStateException("Cannot load independently rebuilt Maque arcane art", exception);
                }
            }
            inputStream.close();
        }
        return map;
    }

    private static void requirePoint(double[] dArray) {
        if (dArray == null || dArray.length != 3) {
            throw new IllegalStateException("Invalid coordinate");
        }
        for (double d : dArray) {
            if (Double.isFinite(d) && !(Math.abs(d) > 16.0)) continue;
            throw new IllegalStateException("Nonfinite coordinate");
        }
    }

    public record Art(String id, String title, double scale, double[] offset, String motion, Bone[] bones, Surface[] faces, Surface[] lite) {
    }

    private record Library(int schema, String revision, Art[] models) {
    }

    public record Bone(String name, double[] pivot, Role role) {
    }

    public record Surface(@SerializedName(value="p") double[][] points, @SerializedName(value="m") int material, @SerializedName(value="a") double opacity, @SerializedName(value="b") int bone, String part) {
    }

    public record Role(String kind, int side, int lane) {
    }
}

