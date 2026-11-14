package net.farmingtales.ftboss.youzi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class YouziCombatGeometry {
    private final JsonObject specification;
    private final Map<String, JsonObject> effects = new LinkedHashMap<>();

    public YouziCombatGeometry(JsonObject var1) {
        if (!"youzi-combat-fx/v1".equals(var1.get("schema").getAsString())) {
            throw new IllegalArgumentException("Unsupported Youzi combat schema");
        } else {
            this.specification = var1;

            for (JsonElement var3 : var1.getAsJsonArray("effects")) {
                JsonObject var4 = var3.getAsJsonObject();
                if (this.effects.put(var4.get("id").getAsString(), var4) != null) {
                    throw new IllegalArgumentException("Duplicate combat effect");
                }
            }
        }
    }

    public JsonObject effect(String var1) {
        JsonObject var2 = this.effects.get(var1);
        if (var2 == null) {
            throw new IllegalArgumentException("Unknown Youzi effect: " + var1);
        } else {
            return var2;
        }
    }

    public double duration(String var1) {
        return this.effect(var1).get("duration").getAsDouble();
    }

    public List<YouziCombatGeometry.Quad> sample(
        String var1, double var2, YouziHomingFlight.Vector var4, YouziHomingFlight.Vector var5, double var6, double var8
    ) {
        if (Double.isFinite(var2) && Double.isFinite(var6) && !(var6 <= 0.0) && Double.isFinite(var8)) {
            JsonObject var10 = this.effect(var1);
            YouziHomingFlight.Vector var11 = rotate(var5.subtract(var4), -var8, 0.0).multiply(1.0 / var6);
            YouziCombatGeometry.Emitter var12 = new YouziCombatGeometry.Emitter(var4, var6, var8);

            for (JsonElement var14 : var10.getAsJsonArray("primitives")) {
                JsonObject var15 = var14.getAsJsonObject();
                double var16 = number(var15, "start", 0.0);
                double var18 = number(var15, "end", 0.0);
                if (!(var2 < var16) && !(var2 >= var18)) {
                    double var20 = (var2 - var16) / (var18 - var16);
                    double var22 = clamp(var20 / 0.08) * clamp((1.0 - var20) / 0.32);
                    if (!(var22 < 1.0E-5)) {
                        double var24 = bool(var15, "collapse") ? 1.0 - var20 * 0.85 : 1.0 - number(var15, "growth", 0.0) * Math.pow(1.0 - var20, 2.0);
                        YouziHomingFlight.Vector var26 = (bool(var15, "atTarget") ? var11 : new YouziHomingFlight.Vector(0.0, 0.0, 0.0))
                            .add(vector(var15.getAsJsonArray("position")));
                        YouziHomingFlight.Vector var27 = vector(this.specification.getAsJsonObject("palette").getAsJsonArray(var15.get("color").getAsString()));
                        double var28 = number(var15, "spin", 0.0) * var2 + number(var15, "angle", 0.0);
                        String var30 = var15.get("shape").getAsString();
                        int var31 = (int)number(var15, "segments", 0.0);
                        switch (var30) {
                            case "star":
                                var12.diamond(var26, number(var15, "size", 0.0) * var24 * (0.65 + 0.35 * Math.sin(Math.PI * var20)), var27, var22, var28);
                                break;
                            case "motes":
                                int var45 = (int)number(var15, "count", 0.0);

                                for (int var48 = 0; var48 < var45; var48++) {
                                    double var51 = (double)var48 * 2.3999632297 + Math.toRadians(var28);
                                    double var53 = number(var15, "radius", 0.0) * (0.4 + 0.6 * ((double)(var48 * 7 % 13) / 12.0)) * var24;
                                    double var40 = number(var15, "height", 0.0) * (double)(var48 % 7) / 6.0 + number(var15, "rise", 0.0) * var20;
                                    YouziHomingFlight.Vector var42 = var26.add(
                                        new YouziHomingFlight.Vector(Math.cos(var51) * var53, var40, Math.sin(var51) * var53)
                                    );
                                    var12.diamond(
                                        var42,
                                        number(var15, "size", 0.0) * (0.7 + 0.3 * Math.sin(Math.PI * var20)),
                                        var27,
                                        var22 * (0.6 + 0.4 * (double)(var48 % 3) / 2.0),
                                        var28 + (double)(var48 * 37)
                                    );
                                }
                                break;
                            case "arc":
                                List<YouziHomingFlight.Vector> var44 = new ArrayList<>();

                                for (int var47 = 0; var47 <= var31; var47++) {
                                    double var50 = Math.toRadians(var28 + number(var15, "sweep", 0.0) * (double)var47 / (double)var31);
                                    double var52 = number(var15, "radius", 0.0) * var24;
                                    var44.add(
                                        var26.add(
                                            rotate(
                                                new YouziHomingFlight.Vector(Math.cos(var50) * var52, 0.0, Math.sin(var50) * var52),
                                                0.0,
                                                number(var15, "tilt", 0.0)
                                            )
                                        )
                                    );
                                }

                                var12.ribbon(var44, number(var15, "width", 0.0), var27, var22);
                                break;
                            case "helix":
                                for (int var43 = 0; var43 < (int)number(var15, "strands", 0.0); var43++) {
                                    List<YouziHomingFlight.Vector> var46 = new ArrayList<>();

                                    for (int var49 = 0; var49 <= var31; var49++) {
                                        double var37 = (double)var49 / (double)var31;
                                        double var54 = var37 * number(var15, "turns", 0.0) * Math.PI * 2.0 + Math.toRadians(var28) + (double)var43 * Math.PI;
                                        double var41 = number(var15, "radius", 0.0) * var24;
                                        var46.add(
                                            var26.add(
                                                new YouziHomingFlight.Vector(
                                                    Math.cos(var54) * var41, var37 * number(var15, "height", 0.0) * var24, Math.sin(var54) * var41
                                                )
                                            )
                                        );
                                    }

                                    var12.ribbon(var46, number(var15, "width", 0.0), var27, var22);
                                }
                                break;
                            case "link":
                                List<YouziHomingFlight.Vector> var34 = new ArrayList<>();

                                for (int var35 = 0; var35 <= var31; var35++) {
                                    double var36 = (double)var35 / (double)var31;
                                    YouziHomingFlight.Vector var38 = var11.multiply(var36);
                                    double var39 = number(var15, "curve", 0.0) * Math.sin(Math.PI * var36) + (var1.equals("echo_trace") ? 0.0 : 0.75);
                                    var34.add(var38.add(new YouziHomingFlight.Vector(0.0, var39, 0.0)));
                                }

                                var12.ribbon(var34, number(var15, "width", 0.0), var27, var22);
                                break;
                            default:
                                throw new IllegalArgumentException("Unknown combat primitive: " + var30);
                        }
                    }
                }
            }

            if (var12.quads.size() > this.specification.get("maximumQuadsPerEffect").getAsInt()) {
                throw new IllegalStateException("Combat geometry exceeds bounded buffer");
            } else {
                return List.copyOf(var12.quads);
            }
        } else {
            throw new IllegalArgumentException("Invalid effect transform");
        }
    }

    private static double number(JsonObject var0, String var1, double var2) {
        return var0.has(var1) ? var0.get(var1).getAsDouble() : var2;
    }

    private static boolean bool(JsonObject var0, String var1) {
        return var0.has(var1) && var0.get(var1).getAsBoolean();
    }

    private static YouziHomingFlight.Vector vector(JsonArray var0) {
        return var0 == null
            ? new YouziHomingFlight.Vector(0.0, 0.0, 0.0)
            : new YouziHomingFlight.Vector(var0.get(0).getAsDouble(), var0.get(1).getAsDouble(), var0.get(2).getAsDouble());
    }

    private static double clamp(double var0) {
        return Math.max(0.0, Math.min(1.0, var0));
    }

    private static YouziHomingFlight.Vector rotate(YouziHomingFlight.Vector var0, double var1, double var3) {
        double var5 = Math.toRadians(var3);
        double var7 = Math.toRadians(var1);
        double var9 = var0.coordinateY() * Math.cos(var5) - var0.coordinateZ() * Math.sin(var5);
        double var11 = var0.coordinateY() * Math.sin(var5) + var0.coordinateZ() * Math.cos(var5);
        return new YouziHomingFlight.Vector(
            var0.coordinateX() * Math.cos(var7) + var11 * Math.sin(var7), var9, -var0.coordinateX() * Math.sin(var7) + var11 * Math.cos(var7)
        );
    }

    private static final class Emitter {
        private final YouziHomingFlight.Vector origin;
        private final double scale;
        private final double yaw;
        private final List<YouziCombatGeometry.Quad> quads = new ArrayList<>();

        private Emitter(YouziHomingFlight.Vector var1, double var2, double var4) {
            this.origin = var1;
            this.scale = var2;
            this.yaw = var4;
        }

        private YouziHomingFlight.Vector world(YouziHomingFlight.Vector var1) {
            return this.origin.add(YouziCombatGeometry.rotate(var1, this.yaw, 0.0).multiply(this.scale));
        }

        private void emit(
            YouziHomingFlight.Vector var1,
            YouziHomingFlight.Vector var2,
            YouziHomingFlight.Vector var3,
            YouziHomingFlight.Vector var4,
            YouziHomingFlight.Vector var5,
            double var6
        ) {
            this.quads.add(new YouziCombatGeometry.Quad(this.world(var1), this.world(var2), this.world(var3), this.world(var4), var5, var6));
        }

        private void diamond(YouziHomingFlight.Vector var1, double var2, YouziHomingFlight.Vector var4, double var5, double var7) {
            YouziHomingFlight.Vector[] var9 = new YouziHomingFlight.Vector[]{
                new YouziHomingFlight.Vector(0.0, var2, 0.0), new YouziHomingFlight.Vector(0.0, -var2, 0.0)
            };
            YouziHomingFlight.Vector[] var10 = new YouziHomingFlight.Vector[]{
                new YouziHomingFlight.Vector(var2 * 0.62, 0.0, 0.0),
                new YouziHomingFlight.Vector(0.0, 0.0, var2 * 0.62),
                new YouziHomingFlight.Vector(-var2 * 0.62, 0.0, 0.0),
                new YouziHomingFlight.Vector(0.0, 0.0, -var2 * 0.62)
            };

            for (YouziHomingFlight.Vector var14 : var9) {
                for (int var15 = 0; var15 < 4; var15++) {
                    YouziHomingFlight.Vector var16 = var10[var15];
                    YouziHomingFlight.Vector var17 = var10[(var15 + 1) % 4];
                    YouziHomingFlight.Vector var18 = var16.add(var17).multiply(0.5);
                    this.emit(
                        var1.add(YouziCombatGeometry.rotate(var14, var7, 0.0)),
                        var1.add(YouziCombatGeometry.rotate(var16, var7, 0.0)),
                        var1.add(YouziCombatGeometry.rotate(var18, var7, 0.0)),
                        var1.add(YouziCombatGeometry.rotate(var17, var7, 0.0)),
                        var4,
                        var5
                    );
                }
            }
        }

        private void ribbon(List<YouziHomingFlight.Vector> var1, double var2, YouziHomingFlight.Vector var4, double var5) {
            for (int var7 = 0; var7 < var1.size() - 1; var7++) {
                YouziHomingFlight.Vector var8 = ((YouziHomingFlight.Vector)var1.get(var7 + 1)).subtract((YouziHomingFlight.Vector)var1.get(var7)).normalize();
                YouziHomingFlight.Vector var9 = var8.cross(
                        Math.abs(var8.coordinateY()) > 0.95 ? new YouziHomingFlight.Vector(1.0, 0.0, 0.0) : new YouziHomingFlight.Vector(0.0, 1.0, 0.0)
                    )
                    .normalize();
                YouziHomingFlight.Vector var10 = var8.cross(var9).normalize();
                double var11 = Math.sin(Math.PI * ((double)var7 + 0.5) / (double)(var1.size() - 1)) * 0.75 + 0.25;

                for (YouziHomingFlight.Vector var16 : new YouziHomingFlight.Vector[]{var9, var10}) {
                    YouziHomingFlight.Vector var17 = var16.multiply(var2 * var11 * 0.5);
                    this.emit(
                        ((YouziHomingFlight.Vector)var1.get(var7)).subtract(var17),
                        ((YouziHomingFlight.Vector)var1.get(var7)).add(var17),
                        ((YouziHomingFlight.Vector)var1.get(var7 + 1)).add(var17),
                        ((YouziHomingFlight.Vector)var1.get(var7 + 1)).subtract(var17),
                        var4,
                        var5
                    );
                }
            }
        }
    }

    public static record Quad(
        YouziHomingFlight.Vector first,
        YouziHomingFlight.Vector second,
        YouziHomingFlight.Vector third,
        YouziHomingFlight.Vector fourth,
        YouziHomingFlight.Vector color,
        double opacity
    ) {
    }
}
