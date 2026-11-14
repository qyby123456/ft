package net.farmingtales.ftboss.youzi.spells;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.farmingtales.ftboss.youzi.YouziHomingFlight.Vector;

public final class YouziSpellGeometry {
    private static final Vector ZERO = new Vector(0.0, 0.0, 0.0);
    private static final Vector ONE = new Vector(1.0, 1.0, 1.0);
    private final Map<String, YouziSpellGeometry.Model> models = new LinkedHashMap<>();

    public YouziSpellGeometry(JsonObject var1, JsonObject var2, JsonObject var3) {
        if (!"youzi-authored-spells/v1".equals(var1.get("schema").getAsString())) {
            throw new IllegalArgumentException("Unsupported Youzi geometry");
        } else {
            double var4 = var2.get("size").getAsDouble();

            for (JsonElement var7 : var1.getAsJsonArray("models")) {
                JsonObject var8 = var7.getAsJsonObject();
                String var9 = var8.get("id").getAsString();
                ArrayList var10 = new ArrayList();

                for (JsonElement var12 : var8.getAsJsonArray("faces")) {
                    JsonObject var13 = var12.getAsJsonObject();
                    JsonObject var14 = var2.getAsJsonObject("tiles").getAsJsonObject(var13.get("texture").getAsString());
                    Vector[] var15 = new Vector[4];
                    double[][] var16 = new double[4][2];

                    for (int var17 = 0; var17 < 4; var17++) {
                        var15[var17] = vector(var13.getAsJsonArray("points").get(var17));
                        JsonArray var18 = var13.getAsJsonArray("uv").get(var17).getAsJsonArray();
                        var16[var17][0] = (var14.get("x").getAsDouble() + var18.get(0).getAsDouble()) / var4;
                        var16[var17][1] = (var14.get("y").getAsDouble() + var18.get(1).getAsDouble()) / var4;
                    }

                    double var37 = var13.get("opacity").getAsDouble();
                    double var19 = var13.get("emission").getAsDouble();
                    if (!Double.isFinite(var37) || var37 < 0.0 || var37 > 1.0 || !Double.isFinite(var19)) {
                        throw new IllegalArgumentException("Invalid face material");
                    }

                    var10.add(new YouziSpellGeometry.Face(var15, var16, var37, var19));
                }

                ArrayList var21 = new ArrayList();

                for (JsonElement var24 : var8.getAsJsonArray("pieces")) {
                    JsonObject var27 = var24.getAsJsonObject();
                    ArrayList var31 = new ArrayList();

                    for (JsonElement var38 : var27.getAsJsonArray("faces")) {
                        var31.add((YouziSpellGeometry.Face)var10.get(var38.getAsInt()));
                    }

                    String var35 = var27.get("name").getAsString();
                    var21.add(
                        new YouziSpellGeometry.Piece(
                            var35,
                            vector(var27.get("pivot")),
                            List.copyOf(var31),
                            "accent".equals(var27.get("layer").getAsString()),
                            var35.startsWith("star_bolt_")
                        )
                    );
                }

                LinkedHashMap var23 = new LinkedHashMap();

                for (JsonElement var28 : var8.getAsJsonArray("animations")) {
                    JsonObject var32 = var28.getAsJsonObject();
                    LinkedHashMap var36 = new LinkedHashMap();

                    for (Entry var41 : var32.getAsJsonObject("pieces").entrySet()) {
                        var36.put(
                            (String)var41.getKey(),
                            new YouziSpellGeometry.Track(
                                keys((JsonElement)var41.getValue(), "position"),
                                keys((JsonElement)var41.getValue(), "rotation"),
                                keys((JsonElement)var41.getValue(), "scale")
                            )
                        );
                    }

                    String var40 = var32.get("name").getAsString();
                    var23.put(
                        var40.substring(var40.lastIndexOf(46) + 1),
                        new YouziSpellGeometry.Clip(var32.get("length").getAsDouble(), "loop".equals(var32.get("loop").getAsString()), Map.copyOf(var36))
                    );
                }

                JsonObject var26 = null;

                for (JsonElement var33 : var3.getAsJsonArray("models")) {
                    if (var9.equals(var33.getAsJsonObject().get("id").getAsString())) {
                        var26 = var33.getAsJsonObject();
                    }
                }

                if (var26 == null || this.models.containsKey(var9)) {
                    throw new IllegalArgumentException("Missing or duplicate placement");
                }

                Vector var30 = vector(var26.get("suggestedOffsetForProxyTest"));
                this.models
                    .put(
                        var9,
                        new YouziSpellGeometry.Model(
                            List.copyOf(var21),
                            Map.copyOf(var23),
                            var26.get("worldScale").getAsDouble(),
                            var30,
                            vector(var26.get("animationEnvelopeMinimum")).subtract(var30),
                            vector(var26.get("animationEnvelopeMaximum")).subtract(var30)
                        )
                    );
            }

            if (this.models.size() != 3) {
                throw new IllegalArgumentException("All three Youzi spells required");
            }
        }
    }

    private static Vector vector(JsonElement var0) {
        JsonArray var1 = var0.getAsJsonArray();
        if (var1.size() != 3) {
            throw new IllegalArgumentException("Three coordinates required");
        } else {
            return new Vector(var1.get(0).getAsDouble(), var1.get(1).getAsDouble(), var1.get(2).getAsDouble());
        }
    }

    private static List<YouziSpellGeometry.Key> keys(JsonElement var0, String var1) {
        ArrayList var2 = new ArrayList();

        for (JsonElement var4 : var0.getAsJsonArray()) {
            JsonObject var5 = var4.getAsJsonObject();
            if (var1.equals(var5.get("channel").getAsString())) {
                JsonObject var6 = var5.getAsJsonArray("data_points").get(0).getAsJsonObject();
                var2.add(
                    new YouziSpellGeometry.Key(
                        var5.get("time").getAsDouble(), new Vector(var6.get("x").getAsDouble(), var6.get("y").getAsDouble(), var6.get("z").getAsDouble())
                    )
                );
            }
        }

        var2.sort(Comparator.comparingDouble(YouziSpellGeometry.Key::time));
        return List.copyOf(var2);
    }

    private static Vector sample(List<YouziSpellGeometry.Key> var0, double var1, Vector var3) {
        if (var0.isEmpty()) {
            return var3;
        } else if (var1 <= ((YouziSpellGeometry.Key)var0.get(0)).time()) {
            return ((YouziSpellGeometry.Key)var0.get(0)).value();
        } else {
            int var4 = 0;
            int var5 = var0.size() - 1;
            if (var1 >= ((YouziSpellGeometry.Key)var0.get(var5)).time()) {
                return ((YouziSpellGeometry.Key)var0.get(var5)).value();
            } else {
                while (var5 - var4 > 1) {
                    int var6 = var4 + var5 >>> 1;
                    if (((YouziSpellGeometry.Key)var0.get(var6)).time() < var1) {
                        var4 = var6;
                    } else {
                        var5 = var6;
                    }
                }

                YouziSpellGeometry.Key var8 = (YouziSpellGeometry.Key)var0.get(var4);
                YouziSpellGeometry.Key var7 = (YouziSpellGeometry.Key)var0.get(var5);
                return var8.value().lerp(var7.value(), (var1 - var8.time()) / Math.max(1.0E-9, var7.time() - var8.time()));
            }
        }
    }

    public static Vector rotate(Vector var0, Vector var1) {
        double[] var2 = new double[]{var0.coordinateX(), var0.coordinateY(), var0.coordinateZ()};
        double[] var3 = new double[]{var1.coordinateX(), var1.coordinateY(), var1.coordinateZ()};

        for (int var4 = 0; var4 < 3; var4++) {
            double var5 = Math.toRadians(var3[var4]);
            double var7 = Math.cos(var5);
            double var9 = Math.sin(var5);
            int var11 = (var4 + 1) % 3;
            int var12 = (var4 + 2) % 3;
            double var13 = var2[var11];
            double var15 = var2[var12];
            var2[var11] = var13 * var7 - var15 * var9;
            var2[var12] = var13 * var9 + var15 * var7;
        }

        return new Vector(var2[0], var2[1], var2[2]);
    }

    public double length(String var1, String var2) {
        return this.model(var1).clips().get(var2).length();
    }

    private YouziSpellGeometry.Model model(String var1) {
        YouziSpellGeometry.Model var2 = this.models.get(var1);
        if (var2 == null) {
            throw new IllegalArgumentException("Unknown spell");
        } else {
            return var2;
        }
    }

    public Vector offset(String var1, double var2, boolean var4) {
        YouziSpellGeometry.Model var5 = this.model(var1);
        return var4
            ? new Vector(
                -(var5.minimum().coordinateX() + var5.maximum().coordinateX()) * var2 / 2.0,
                0.2 - var5.minimum().coordinateY() * var2,
                -(var5.minimum().coordinateZ() + var5.maximum().coordinateZ()) * var2 / 2.0
            )
            : var5.offset()
                .add(
                    new Vector(
                        (1.0 - var2) * var5.minimum().coordinateX(),
                        Math.max(0.0, 0.2 - (var5.offset().coordinateY() + var5.minimum().coordinateY() * var2)),
                        (1.0 - var2) * var5.maximum().coordinateZ()
                    )
                );
    }

    public void sample(String var1, String var2, double var3, double var5, boolean var7, boolean var8, int var9, Consumer<YouziSpellGeometry.Quad> var10) {
        if (Double.isFinite(var3) && Double.isFinite(var5) && !(var5 <= 0.0) && !(var5 > 4.0) && var9 >= 1) {
            YouziSpellGeometry.Model var11 = this.model(var1);
            YouziSpellGeometry.Clip var12 = var11.clips().get(var2);
            double var13 = var12.loop() ? Math.max(0.0, var3) % var12.length() : Math.max(0.0, Math.min(var12.length(), var3));
            Vector var15 = this.offset(var1, var5, var7);

            for (YouziSpellGeometry.Piece var17 : var11.pieces()) {
                if (!var17.bolt() || var8) {
                    YouziSpellGeometry.Track var18 = var12.tracks().get(var17.name());
                    Vector var19 = var18 == null ? ZERO : sample(var18.position(), var13, ZERO).multiply(0.0625);
                    Vector var20 = var18 == null ? ZERO : sample(var18.rotation(), var13, ZERO);
                    Vector var21 = rotate(new Vector(1.0, 0.0, 0.0), var20);
                    Vector var22 = rotate(new Vector(0.0, 1.0, 0.0), var20);
                    Vector var23 = rotate(new Vector(0.0, 0.0, 1.0), var20);
                    Vector var24 = var18 == null ? ONE : sample(var18.scale(), var13, ONE);
                    if (!(Math.abs(var24.coordinateX() * var24.coordinateY() * var24.coordinateZ()) < 1.0E-9)) {
                        int var25 = 0;

                        for (YouziSpellGeometry.Face var27 : var17.faces()) {
                            if (!var17.accent() || var25++ % var9 == 0) {
                                Vector[] var28 = new Vector[4];

                                for (int var29 = 0; var29 < 4; var29++) {
                                    Vector var30 = var27.points()[var29].subtract(var17.pivot());
                                    var30 = new Vector(
                                        var30.coordinateX() * var24.coordinateX(),
                                        var30.coordinateY() * var24.coordinateY(),
                                        var30.coordinateZ() * var24.coordinateZ()
                                    );
                                    var28[var29] = var21.multiply(var30.coordinateX())
                                        .add(var22.multiply(var30.coordinateY()))
                                        .add(var23.multiply(var30.coordinateZ()))
                                        .add(var17.pivot())
                                        .add(var19)
                                        .multiply(var11.scale() * var5)
                                        .add(var15);
                                }

                                var10.accept(new YouziSpellGeometry.Quad(var28, var27.uv(), var27.opacity(), var27.emission(), var17.accent()));
                            }
                        }
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid runtime frame");
        }
    }

    public void seed(double var1, Consumer<YouziSpellGeometry.Quad> var3) {
        YouziSpellGeometry.Model var4 = this.model("effigy_volley");
        YouziSpellGeometry.Piece var5 = var4.pieces().stream().filter(var0 -> var0.name().equals("star_bolt_1/cut_crystal_seed")).findFirst().orElseThrow();
        double var6 = 0.0;

        for (YouziSpellGeometry.Face var9 : var5.faces()) {
            for (Vector var13 : var9.points()) {
                var6 = Math.max(var6, var13.subtract(var5.pivot()).length() * var4.scale());
            }
        }

        double var14 = var4.scale() * var1 * Math.min(1.0, 0.1 / Math.max(1.0E-9, var6));

        for (YouziSpellGeometry.Face var16 : var5.faces()) {
            Vector[] var17 = new Vector[4];

            for (int var18 = 0; var18 < 4; var18++) {
                var17[var18] = var16.points()[var18].subtract(var5.pivot()).multiply(var14);
            }

            var3.accept(new YouziSpellGeometry.Quad(var17, var16.uv(), var16.opacity(), var16.emission(), false));
        }
    }

    public Vector launchOrigin(int var1, double var2) {
        YouziSpellGeometry.Model var4 = this.model("effigy_volley");
        String var5 = "star_bolt_" + (var1 + 1) + "/cut_crystal_seed";
        YouziSpellGeometry.Piece var6 = var4.pieces().stream().filter(var1x -> var1x.name().equals(var5)).findFirst().orElseThrow();
        YouziSpellGeometry.Track var7 = var4.clips().get("release").tracks().get(var5);
        Vector var8 = sample(var7.position(), 1.0417 + (double)var1 * 0.25, ZERO).multiply(0.0625);
        return var6.pivot().add(var8).multiply(var4.scale() * var2).add(this.offset("effigy_volley", var2, false));
    }

    private static record Clip(double length, boolean loop, Map<String, YouziSpellGeometry.Track> tracks) {
    }

    private static record Face(Vector[] points, double[][] uv, double opacity, double emission) {
    }

    private static record Key(double time, Vector value) {
    }

    private static record Model(
        List<YouziSpellGeometry.Piece> pieces, Map<String, YouziSpellGeometry.Clip> clips, double scale, Vector offset, Vector minimum, Vector maximum
    ) {
    }

    private static record Piece(String name, Vector pivot, List<YouziSpellGeometry.Face> faces, boolean accent, boolean bolt) {
    }

    public static record Quad(Vector[] points, double[][] uv, double opacity, double emission, boolean accent) {
    }

    private static record Track(List<YouziSpellGeometry.Key> position, List<YouziSpellGeometry.Key> rotation, List<YouziSpellGeometry.Key> scale) {
    }
}
