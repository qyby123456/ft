package net.farmingtales.ftboss.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import net.farmingtales.ftboss.util.ManorCeremonyModel.Sink;

public final class ManorAssaultGeometry {
    private static final String[] IDS = new String[]{
        "youzi", "dandan", "dora", "gongdi", "lemon", "qingyi", "xuanlan", "zheergen", "zicai", "", "xueli", "qizai"
    };
    private static final Map<String, ManorAssaultGeometry.AuthoredModel> MODELS = load();

    private ManorAssaultGeometry() {
    }

    private static Map<String, ManorAssaultGeometry.AuthoredModel> load() {
        try {
            Map var1;
            try (InputStream input = ManorAssaultGeometry.class.getResourceAsStream("/assets/ftboss/manor/assault_geometry.json")) {
                if (input == null) {
                    throw new IllegalStateException("Missing Blockbench-authored Manor assault geometry");
                }

                var1 = new Gson()
                    .fromJson(new InputStreamReader(input, StandardCharsets.UTF_8), (new TypeToken<Map<String, ManorAssaultGeometry.AuthoredModel>>() {
                    }).getType());
            }

            return var1;
        } catch (IOException var5) {
            throw new IllegalStateException("Cannot read Manor assault geometry", var5);
        }
    }

    public static boolean supports(int variant) {
        return variant >= 0 && variant < IDS.length && !IDS[variant].isEmpty() && MODELS.containsKey("assault_" + IDS[variant]);
    }

    public static void draw(int variant, double age, boolean release, boolean reduced, float alpha, Sink sink) {
        if (supports(variant) && Float.isFinite(alpha) && !(alpha <= 0.001F)) {
            ManorAssaultGeometry.AuthoredModel model = MODELS.get("assault_" + IDS[variant]);
            double progress = clamp(age / (release ? 12.0 : 20.0));
            double smooth = ManorCeremonyGeometry.smooth(progress);
            double fade = release ? 1.0 - ManorCeremonyGeometry.smooth(Math.max(0.0, (progress - 0.62) / 0.38)) : Math.min(1.0, age / 5.0);
            float opacity = (float)((double)alpha * fade);
            if (!(opacity <= 0.001F)) {
                for (Entry<String, ManorAssaultGeometry.AuthoredGroup> entry : model.groups.entrySet()) {
                    String name = entry.getKey();
                    if (!reduced || !name.equals("inner") && !name.equals("flash")) {
                        ManorAssaultGeometry.AuthoredGroup group = entry.getValue();
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

                        Sink posed = ManorCeremonyModel.pose(group.origin[0], group.origin[1] + y, group.origin[2], yaw, 0.0, 0.0, scale, sink);
                        int vertex = 0;

                        for (int coordinate = 0; vertex < group.colors.length; coordinate += 3) {
                            posed.vertex(
                                group.vertices[coordinate] - group.origin[0],
                                group.vertices[coordinate + 1] - group.origin[1],
                                group.vertices[coordinate + 2] - group.origin[2],
                                group.colors[vertex],
                                opacity
                            );
                            vertex++;
                        }
                    }
                }
            }
        }
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static final class AuthoredGroup {
        double[] origin;
        double[] vertices;
        int[] colors;
    }

    private static final class AuthoredModel {
        Map<String, ManorAssaultGeometry.AuthoredGroup> groups;
    }
}
