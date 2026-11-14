/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 */
package net.farmingtales.ftboss.client.maque;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import net.farmingtales.ftboss.client.maque.MaqueArcaneAssets;

public final class MaqueSupportAssets {
    private static final Effect[] EFFECTS = MaqueSupportAssets.read();

    private MaqueSupportAssets() {
    }

    public static Effect get(int n) {
        return EFFECTS[n];
    }

    private static Effect[] read() {
        Effect[] effectArray;
        block18: {
            InputStream inputStream = MaqueSupportAssets.class.getResourceAsStream("/assets/ftboss/maque_arcane/support_models.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Missing authored support effects");
                }
                Library library = (Library)new Gson().fromJson((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8), Library.class);
                if (library.schema != 1 || library.models.length != 12) {
                    throw new IllegalStateException("Incomplete support effects");
                }
                for (Effect effect : library.models) {
                    if (effect.duration < 20 || effect.duration > 120 || effect.bones.length < 1 || effect.bones.length > 8 || effect.faces.length > 9000 || effect.lite.length >= effect.faces.length || effect.lite.length < 1) {
                        throw new IllegalStateException("Invalid support budget");
                    }
                    MaqueArcaneAssets.Surface[][] surfaceArrayArray = new MaqueArcaneAssets.Surface[][]{effect.faces, effect.lite};
                    int n = surfaceArrayArray.length;
                    for (int i = 0; i < n; ++i) {
                        MaqueArcaneAssets.Surface[] surfaceArray;
                        for (MaqueArcaneAssets.Surface surface : surfaceArray = surfaceArrayArray[i]) {
                            if (surface.points().length != 4 || surface.bone() < 0 || surface.bone() >= effect.bones.length || surface.material() < 0 || surface.material() > 5 || !Double.isFinite(surface.opacity()) || surface.opacity() <= 0.0 || surface.opacity() > 1.0) {
                                throw new IllegalStateException("Invalid support surface");
                            }
                            for (double[] dArray : surface.points()) {
                                if (dArray.length != 3) {
                                    throw new IllegalStateException("Invalid support point");
                                }
                                for (double d : dArray) {
                                    if (Double.isFinite(d) && !(Math.abs(d) > 16.0)) continue;
                                    throw new IllegalStateException("Invalid support coordinate");
                                }
                            }
                        }
                    }
                    MaqueArcaneAssets.groupMaterials(effect.faces);
                    MaqueArcaneAssets.groupMaterials(effect.lite);
                }
                effectArray = library.models;
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
                    throw new IllegalStateException("Cannot load new Maque support art", exception);
                }
            }
            inputStream.close();
        }
        return effectArray;
    }

    public record Effect(String id, String title, int duration, MaqueArcaneAssets.Bone[] bones, MaqueArcaneAssets.Surface[] faces, MaqueArcaneAssets.Surface[] lite) {
    }

    private record Library(int schema, Effect[] models) {
    }
}

