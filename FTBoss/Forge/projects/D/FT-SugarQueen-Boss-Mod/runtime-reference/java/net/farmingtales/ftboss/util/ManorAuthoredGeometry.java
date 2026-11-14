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
import net.farmingtales.ftboss.util.ManorCeremonyModel;

public final class ManorAuthoredGeometry {
    private static final String[] IDS = new String[]{"youzi", "dandan", "dora", "gongdi", "lemon", "qingyi", "xuanlan", "zheergen", "zicai", "", "xueli", "qizai"};
    private static final Map<String, AuthoredModel> MODELS = ManorAuthoredGeometry.load();

    private ManorAuthoredGeometry() {
    }

    private static Map<String, AuthoredModel> load() {
        Map map;
        block9: {
            InputStream inputStream = ManorAuthoredGeometry.class.getResourceAsStream("/assets/ftboss/manor/authored_geometry.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Missing Blockbench-authored manor geometry");
                }
                map = (Map)new Gson().fromJson((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8), new TypeToken<Map<String, AuthoredModel>>(){}.getType());
                if (inputStream == null) break block9;
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
                catch (IOException iOException) {
                    throw new IllegalStateException("Cannot read manor geometry", iOException);
                }
            }
            inputStream.close();
        }
        return map;
    }

    public static boolean supports(int n) {
        return n >= 0 && n < IDS.length && !IDS[n].isEmpty();
    }

    public static void gate(double d, int n, boolean bl, float f, ManorCeremonyModel.Sink sink) {
        ManorAuthoredGeometry.draw("ceremony_gate", Math.max(0.0, Math.min(1.0, d)), 0.0, bl, f, sink);
        if (ManorAuthoredGeometry.supports(n)) {
            ManorAuthoredGeometry.crest(n, f, ManorCeremonyModel.pose(0.0, 5.9, -0.44, 0.0, 0.0, 0.0, 0.55, sink));
        }
    }

    public static void crown(boolean bl, float f, ManorCeremonyModel.Sink sink) {
        ManorAuthoredGeometry.draw("ceremony_crown", 0.0, 0.0, bl, f, sink);
    }

    public static void apple(float f, ManorCeremonyModel.Sink sink) {
        ManorAuthoredGeometry.draw("ceremony_apple", 0.0, 0.0, false, f, sink);
    }

    public static void chest(double d, float f, ManorCeremonyModel.Sink sink) {
        ManorAuthoredGeometry.draw("ceremony_chest", Math.max(0.0, Math.min(1.0, d)), 0.0, false, f, sink);
    }

    public static void crest(int n, float f, ManorCeremonyModel.Sink sink) {
        if (ManorAuthoredGeometry.supports(n)) {
            ManorAuthoredGeometry.draw("crest_" + IDS[n], 0.0, 0.0, false, f, sink);
        } else {
            ManorAuthoredGeometry.crown(false, f, sink);
        }
    }

    public static void domain(int n, double d, boolean bl, float f, ManorCeremonyModel.Sink sink) {
        if (ManorAuthoredGeometry.supports(n)) {
            ManorAuthoredGeometry.draw("domain_" + IDS[n], 0.0, d, bl, f, sink);
        }
    }

    private static void draw(String string, double d, double d2, boolean bl, float f, ManorCeremonyModel.Sink sink) {
        if (!Float.isFinite(f) || f <= 0.001f) {
            return;
        }
        AuthoredModel authoredModel = MODELS.get(string);
        if (authoredModel == null) {
            return;
        }
        for (Map.Entry<String, AuthoredGroup> entry : authoredModel.groups.entrySet()) {
            String string2 = entry.getKey();
            if (bl && string2.equals("detail")) continue;
            AuthoredGroup authoredGroup = entry.getValue();
            double d3 = string2.equals("outer") ? d2 * 0.002 : (string2.equals("inner") ? -d2 * 0.003 : (string2.equals("door_left") ? -d * 1.35 : (string2.equals("door_right") ? d * 1.35 : 0.0)));
            double d4 = string2.equals("lid") ? d * 1.3 : 0.0;
            ManorCeremonyModel.Sink sink2 = ManorCeremonyModel.pose(authoredGroup.origin[0], authoredGroup.origin[1], authoredGroup.origin[2], d3, d4, 0.0, 1.0, sink);
            int n = 0;
            int n2 = 0;
            while (n < authoredGroup.colors.length) {
                sink2.vertex(authoredGroup.vertices[n2] - authoredGroup.origin[0], authoredGroup.vertices[n2 + 1] - authoredGroup.origin[1], authoredGroup.vertices[n2 + 2] - authoredGroup.origin[2], authoredGroup.colors[n], f);
                ++n;
                n2 += 3;
            }
        }
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

