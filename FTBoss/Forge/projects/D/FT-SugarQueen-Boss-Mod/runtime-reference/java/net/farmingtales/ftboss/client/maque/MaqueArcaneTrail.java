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
import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.client.maque.MaqueArcaneAssets;
import net.farmingtales.ftboss.client.maque.MaqueArcanePose;

public final class MaqueArcaneTrail {
    private static final Profile PROFILE = MaqueArcaneTrail.read();

    private MaqueArcaneTrail() {
    }

    private static Profile read() {
        Profile throwable2;
        block11: {
            InputStream inputStream = MaqueArcaneTrail.class.getResourceAsStream("/assets/ftboss/maque_arcane/trail_profile.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Missing independent trail profile");
                }
                Profile profile = (Profile)new Gson().fromJson((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8), Profile.class);
                if (!Double.isFinite(profile.lifetimeTicks) || profile.lifetimeTicks <= 0.0 || profile.lifetimeTicks > 10.0 || profile.fullSegments < 4 || profile.fullSegments > 24 || profile.liteSegments < 4 || profile.liteSegments >= profile.fullSegments || profile.sides < 4 || profile.sides > 8 || !Double.isFinite(profile.headWidth) || profile.headWidth <= 0.0 || profile.headWidth > 0.1 || !Double.isFinite(profile.attachment) || Math.abs(profile.attachment) > 1.0 || profile.layers.length != 2) {
                    throw new IllegalStateException("Invalid trail budget");
                }
                for (Layer layer : profile.layers) {
                    if (!(!Double.isFinite(layer.opacity) || layer.opacity <= 0.0 || layer.opacity > 1.0 || !Double.isFinite(layer.radiusScale) || layer.radiusScale <= 0.0 || layer.radiusScale > 1.0 || layer.material < -1 || layer.material > 2 || !Double.isFinite(layer.capExtension) || layer.capExtension < 0.0) && !(layer.capExtension > 0.05)) continue;
                    throw new IllegalStateException("Invalid trail material");
                }
                throwable2 = profile;
                if (inputStream == null) break block11;
            }
            catch (Throwable throwable) {
                try {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        }
                        catch (Throwable throwable3) {
                            throwable.addSuppressed(throwable3);
                        }
                    }
                    throw throwable;
                }
                catch (Exception exception) {
                    throw new IllegalStateException("Cannot load authored trail profile", exception);
                }
            }
            inputStream.close();
        }
        return throwable2;
    }

    public static int maximumFaces(boolean bl) {
        return MaqueArcaneTrail.PROFILE.layers.length * ((bl ? MaqueArcaneTrail.PROFILE.liteSegments : MaqueArcaneTrail.PROFILE.fullSegments) * MaqueArcaneTrail.PROFILE.sides + (MaqueArcaneTrail.PROFILE.sides - 2) * 2);
    }

    public static double endTick() {
        return 12.0 + MaqueArcaneTrail.PROFILE.lifetimeTicks;
    }

    public static double opacity(double d) {
        return MaqueArcanePose.smooth(d / 2.0);
    }

    public static List<Section> sections(MaqueArcaneAssets.Bone bone, double d, double[] dArray, boolean bl, boolean bl2) {
        double d2 = Math.max(0.0, d - MaqueArcaneTrail.PROFILE.lifetimeTicks);
        double d3 = Math.min(d, 12.0);
        if (d <= 0.0 || d >= 12.0 + MaqueArcaneTrail.PROFILE.lifetimeTicks || d3 - d2 < 1.0E-4) {
            return List.of();
        }
        int n = bl2 ? MaqueArcaneTrail.PROFILE.liteSegments : MaqueArcaneTrail.PROFILE.fullSegments;
        ArrayList<Section> arrayList = new ArrayList<Section>(n + 1);
        for (int i = 0; i <= n; ++i) {
            double d4 = d2 + (d3 - d2) * (double)i / (double)n;
            MaqueArcanePose.Flight flight = MaqueArcanePose.flight(bone, d4, dArray, bl);
            double d5 = Math.pow(Math.max(0.0, 1.0 - (d - d4) / MaqueArcaneTrail.PROFILE.lifetimeTicks), 1.6);
            double[] dArray2 = new double[3];
            for (int j = 0; j < 3; ++j) {
                dArray2[j] = flight.center()[j] + flight.forward()[j] * MaqueArcaneTrail.PROFILE.attachment;
            }
            arrayList.add(new Section(d4, d5, dArray2, flight.right(), flight.up(), flight.forward()));
        }
        return arrayList;
    }

    public static List<Face> faces(MaqueArcaneAssets.Bone bone, int n, double d, double[] dArray, boolean bl, boolean bl2) {
        List<Section> list = MaqueArcaneTrail.sections(bone, d, dArray, bl, bl2);
        if (list.size() < 2) {
            return List.of();
        }
        ArrayList<Face> arrayList = new ArrayList<Face>(MaqueArcaneTrail.maximumFaces(bl2));
        for (int i = 0; i < MaqueArcaneTrail.PROFILE.layers.length; ++i) {
            int n2;
            double d2;
            double d3;
            int n3;
            Layer layer = MaqueArcaneTrail.PROFILE.layers[i];
            int n4 = layer.material < 0 ? (n % 2 == 1 ? 2 : 1) : layer.material;
            String string = "flight_history_" + n + "_layer_" + i;
            double[][][] dArray2 = new double[list.size()][MaqueArcaneTrail.PROFILE.sides][3];
            for (n3 = 0; n3 < list.size(); ++n3) {
                Section section = list.get(n3);
                d3 = Math.max(7.5E-4, MaqueArcaneTrail.PROFILE.headWidth * layer.radiusScale * Math.sqrt(section.strength));
                d2 = n3 == 0 ? -layer.capExtension : (n3 == list.size() - 1 ? layer.capExtension : 0.0);
                for (n2 = 0; n2 < MaqueArcaneTrail.PROFILE.sides; ++n2) {
                    double d4 = (double)n2 / (double)MaqueArcaneTrail.PROFILE.sides * Math.PI * 2.0;
                    for (int j = 0; j < 3; ++j) {
                        dArray2[n3][n2][j] = section.center[j] + d2 * section.forward[j] + d3 * (Math.cos(d4) * section.right[j] + Math.sin(d4) * section.up[j]);
                    }
                }
            }
            for (n3 = 0; n3 < dArray2.length - 1; ++n3) {
                double d5 = layer.opacity * list.get((int)n3).strength;
                double d6 = layer.opacity * list.get((int)(n3 + 1)).strength;
                for (int j = 0; j < MaqueArcaneTrail.PROFILE.sides; ++j) {
                    n2 = (j + 1) % MaqueArcaneTrail.PROFILE.sides;
                    arrayList.add(MaqueArcaneTrail.face(new double[][]{dArray2[n3][j], dArray2[n3][n2], dArray2[n3 + 1][n2], dArray2[n3 + 1][j]}, n4, n, string, new double[]{d5, d5, d6, d6}));
                }
            }
            n3 = dArray2.length - 1;
            for (int j = 1; j < MaqueArcaneTrail.PROFILE.sides - 1; ++j) {
                d3 = layer.opacity * list.get((int)0).strength;
                d2 = layer.opacity * list.get((int)n3).strength;
                arrayList.add(MaqueArcaneTrail.face(new double[][]{dArray2[0][0], dArray2[0][j + 1], dArray2[0][j], dArray2[0][j]}, n4, n, string, new double[]{d3, d3, d3, d3}));
                arrayList.add(MaqueArcaneTrail.face(new double[][]{dArray2[n3][0], dArray2[n3][j], dArray2[n3][j + 1], dArray2[n3][j + 1]}, n4, n, string, new double[]{d2, d2, d2, d2}));
            }
        }
        return arrayList;
    }

    private static Face face(double[][] dArray, int n, int n2, String string, double[] dArray2) {
        return new Face(new MaqueArcaneAssets.Surface(dArray, n, 1.0, n2, string), dArray2);
    }

    public record Profile(double lifetimeTicks, int fullSegments, int liteSegments, int sides, double attachment, double headWidth, Layer[] layers) {
    }

    public record Layer(double radiusScale, double opacity, int material, double capExtension) {
    }

    public record Section(double time, double strength, double[] center, double[] right, double[] up, double[] forward) {
    }

    public record Face(MaqueArcaneAssets.Surface surface, double[] alpha) {
    }
}

