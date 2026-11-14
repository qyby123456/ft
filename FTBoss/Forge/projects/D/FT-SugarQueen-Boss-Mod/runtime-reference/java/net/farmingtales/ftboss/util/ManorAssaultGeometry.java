/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.reflect.TypeToken
 */
package net.farmingtales.ftboss.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.farmingtales.ftboss.util.ManorCeremonyGeometry;
import net.farmingtales.ftboss.util.ManorCeremonyModel;

public final class ManorAssaultGeometry {
    private static final String[] IDS = new String[]{"youzi", "dandan", "dora", "gongdi", "lemon", "qingyi", "xuanlan", "zheergen", "zicai", "", "xueli", "qizai"};
    private static final Map<String, AuthoredModel> MODELS = ManorAssaultGeometry.load();

    private ManorAssaultGeometry() {
    }

    private static Map<String, AuthoredModel> load() {
        Map map;
        block9: {
            InputStream input = ManorAssaultGeometry.class.getResourceAsStream("/assets/ftboss/manor/assault_geometry.json");
            try {
                if (input == null) {
                    throw new IllegalStateException("Missing Blockbench-authored Manor assault geometry");
                }
                map = (Map)new Gson().fromJson((Reader)new InputStreamReader(input, StandardCharsets.UTF_8), new TypeToken<Map<String, AuthoredModel>>(){}.getType());
                if (input == null) break block9;
            }
            catch (Throwable throwable) {
                try {
                    if (input != null) {
                        try {
                            input.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException exception) {
                    throw new IllegalStateException("Cannot read Manor assault geometry", exception);
                }
            }
            input.close();
        }
        return map;
    }

    public static boolean supports(int variant) {
        return variant >= 0 && variant < IDS.length && !IDS[variant].isEmpty() && MODELS.containsKey("assault_" + IDS[variant]);
    }

    public static void draw(int variant, double age, boolean release, boolean reduced, float alpha, ManorCeremonyModel.Sink sink) {
        if (!ManorAssaultGeometry.supports(variant) || !Float.isFinite(alpha) || alpha <= 0.001f) {
            return;
        }
        AuthoredModel model = MODELS.get("assault_" + IDS[variant]);
        double progress = ManorAssaultGeometry.clamp(age / (release ? 12.0 : 20.0));
        double smooth = ManorCeremonyGeometry.smooth(progress);
        double fade = release ? 1.0 - ManorCeremonyGeometry.smooth(Math.max(0.0, (progress - 0.62) / 0.38)) : Math.min(1.0, age / 5.0);
        float opacity = (float)((double)alpha * fade);
        if (opacity <= 0.001f) {
            return;
        }
        for (Map.Entry<String, AuthoredGroup> entry : model.groups.entrySet()) {
            String name = entry.getKey();
            if (reduced && (name.equals("inner") || name.equals("flash"))) continue;
            AuthoredGroup group = entry.getValue();
            double y = 0.0;
            double yaw = 0.0;
            double scale = 1.0;
            if (!release) {
                if (name.equals("ground")) {
                    scale = 0.08 + 0.92 * ManorCeremonyGeometry.smooth(Math.min(1.0, progress / 0.28));
                } else if (name.equals("outer")) {
                    yaw = smooth * 4.55;
                    y = -0.24 * (1.0 - smooth);
                    scale = 0.12 + 0.88 * smooth;
                } else if (name.equals("inner")) {
                    yaw = -smooth * 5.25;
                    scale = 0.1 + 0.9 * smooth;
                } else if (name.equals("sigil")) {
                    y = -0.5 * (1.0 - smooth);
                    scale = 0.08 + 0.92 * smooth;
                } else if (name.equals("spikes")) {
                    y = -0.62 * (1.0 - smooth);
                    scale = 0.15 + 0.85 * smooth;
                } else if (name.equals("flash")) {
                    scale = 0.18 + 0.16 * Math.sin(age * 0.55) + 0.34 * smooth;
                    y = 0.04 * Math.sin(age * 0.35);
                }
            } else if (name.equals("ground")) {
                scale = 1.0 + 0.12 * smooth;
            } else if (name.equals("outer")) {
                yaw = 4.55 + smooth * 2.0;
                y = smooth * 0.5;
                scale = 1.0 + 0.22 * smooth;
            } else if (name.equals("inner")) {
                yaw = -5.25 - smooth * 2.1;
                scale = Math.max(0.04, 1.0 - smooth);
            } else if (name.equals("sigil")) {
                y = smooth * 1.0;
                scale = 1.05 - 0.85 * smooth;
            } else if (name.equals("spikes")) {
                y = smooth * 1.35;
                scale = 1.0 + 0.45 * Math.sin(Math.PI * progress);
            } else if (name.equals("flash")) {
                y = smooth * 1.2;
                scale = 0.55 + 2.15 * Math.sin(Math.PI * Math.min(1.0, progress * 1.25));
            }
            ManorCeremonyModel.Sink posed = ManorCeremonyModel.pose(group.origin[0], group.origin[1] + y, group.origin[2], yaw, 0.0, 0.0, scale, sink);
            int vertex = 0;
            int coordinate = 0;
            while (vertex < group.colors.length) {
                posed.vertex(group.vertices[coordinate] - group.origin[0], group.vertices[coordinate + 1] - group.origin[1], group.vertices[coordinate + 2] - group.origin[2], group.colors[vertex], opacity);
                ++vertex;
                coordinate += 3;
            }
        }
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static final class AuthoredModel {
        Map<String, AuthoredGroup> groups;

        private AuthoredModel() {
        }
    }

    private static final class AuthoredGroup {
        double[] origin;
        double[] vertices;
        int[] colors;

        private AuthoredGroup() {
        }
    }
}

