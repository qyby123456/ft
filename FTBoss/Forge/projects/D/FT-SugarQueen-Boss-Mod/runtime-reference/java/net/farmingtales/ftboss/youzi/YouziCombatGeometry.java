/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package net.farmingtales.ftboss.youzi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.youzi.YouziHomingFlight;

public final class YouziCombatGeometry {
    private final JsonObject specification;
    private final Map<String, JsonObject> effects = new LinkedHashMap<String, JsonObject>();

    public YouziCombatGeometry(JsonObject jsonObject) {
        if (!"youzi-combat-fx/v1".equals(jsonObject.get("schema").getAsString())) {
            throw new IllegalArgumentException("Unsupported Youzi combat schema");
        }
        this.specification = jsonObject;
        for (JsonElement jsonElement : jsonObject.getAsJsonArray("effects")) {
            JsonObject jsonObject2 = jsonElement.getAsJsonObject();
            if (this.effects.put(jsonObject2.get("id").getAsString(), jsonObject2) == null) continue;
            throw new IllegalArgumentException("Duplicate combat effect");
        }
    }

    public JsonObject effect(String string) {
        JsonObject jsonObject = this.effects.get(string);
        if (jsonObject == null) {
            throw new IllegalArgumentException("Unknown Youzi effect: " + string);
        }
        return jsonObject;
    }

    public double duration(String string) {
        return this.effect(string).get("duration").getAsDouble();
    }

    public List<Quad> sample(String string, double d, YouziHomingFlight.Vector vector, YouziHomingFlight.Vector vector2, double d2, double d3) {
        if (!Double.isFinite(d) || !Double.isFinite(d2) || d2 <= 0.0 || !Double.isFinite(d3)) {
            throw new IllegalArgumentException("Invalid effect transform");
        }
        JsonObject jsonObject = this.effect(string);
        YouziHomingFlight.Vector vector3 = YouziCombatGeometry.rotate(vector2.subtract(vector), -d3, 0.0).multiply(1.0 / d2);
        Emitter emitter = new Emitter(vector, d2, d3);
        block14: for (JsonElement jsonElement : jsonObject.getAsJsonArray("primitives")) {
            double d4;
            double d5;
            JsonObject jsonObject2 = jsonElement.getAsJsonObject();
            double d6 = YouziCombatGeometry.number(jsonObject2, "start", 0.0);
            double d7 = YouziCombatGeometry.number(jsonObject2, "end", 0.0);
            if (d < d6 || d >= d7 || (d5 = YouziCombatGeometry.clamp((d4 = (d - d6) / (d7 - d6)) / 0.08) * YouziCombatGeometry.clamp((1.0 - d4) / 0.32)) < 1.0E-5) continue;
            double d8 = YouziCombatGeometry.bool(jsonObject2, "collapse") ? 1.0 - d4 * 0.85 : 1.0 - YouziCombatGeometry.number(jsonObject2, "growth", 0.0) * Math.pow(1.0 - d4, 2.0);
            YouziHomingFlight.Vector vector4 = (YouziCombatGeometry.bool(jsonObject2, "atTarget") ? vector3 : new YouziHomingFlight.Vector(0.0, 0.0, 0.0)).add(YouziCombatGeometry.vector(jsonObject2.getAsJsonArray("position")));
            YouziHomingFlight.Vector vector5 = YouziCombatGeometry.vector(this.specification.getAsJsonObject("palette").getAsJsonArray(jsonObject2.get("color").getAsString()));
            double d9 = YouziCombatGeometry.number(jsonObject2, "spin", 0.0) * d + YouziCombatGeometry.number(jsonObject2, "angle", 0.0);
            String string2 = jsonObject2.get("shape").getAsString();
            int n = (int)YouziCombatGeometry.number(jsonObject2, "segments", 0.0);
            switch (string2) {
                case "star": {
                    emitter.diamond(vector4, YouziCombatGeometry.number(jsonObject2, "size", 0.0) * d8 * (0.65 + 0.35 * Math.sin(Math.PI * d4)), vector5, d5, d9);
                    break;
                }
                case "motes": {
                    double d10;
                    double d11;
                    int n2;
                    int n3 = (int)YouziCombatGeometry.number(jsonObject2, "count", 0.0);
                    for (n2 = 0; n2 < n3; ++n2) {
                        d11 = (double)n2 * 2.3999632297 + Math.toRadians(d9);
                        d10 = YouziCombatGeometry.number(jsonObject2, "radius", 0.0) * (0.4 + 0.6 * ((double)(n2 * 7 % 13) / 12.0)) * d8;
                        double d12 = YouziCombatGeometry.number(jsonObject2, "height", 0.0) * (double)(n2 % 7) / 6.0 + YouziCombatGeometry.number(jsonObject2, "rise", 0.0) * d4;
                        YouziHomingFlight.Vector vector6 = vector4.add(new YouziHomingFlight.Vector(Math.cos(d11) * d10, d12, Math.sin(d11) * d10));
                        emitter.diamond(vector6, YouziCombatGeometry.number(jsonObject2, "size", 0.0) * (0.7 + 0.3 * Math.sin(Math.PI * d4)), vector5, d5 * (0.6 + 0.4 * (double)(n2 % 3) / 2.0), d9 + (double)(n2 * 37));
                    }
                    continue block14;
                }
                case "arc": {
                    double d10;
                    double d11;
                    int n2;
                    ArrayList<YouziHomingFlight.Vector> arrayList = new ArrayList<YouziHomingFlight.Vector>();
                    for (n2 = 0; n2 <= n; ++n2) {
                        d11 = Math.toRadians(d9 + YouziCombatGeometry.number(jsonObject2, "sweep", 0.0) * (double)n2 / (double)n);
                        d10 = YouziCombatGeometry.number(jsonObject2, "radius", 0.0) * d8;
                        arrayList.add(vector4.add(YouziCombatGeometry.rotate(new YouziHomingFlight.Vector(Math.cos(d11) * d10, 0.0, Math.sin(d11) * d10), 0.0, YouziCombatGeometry.number(jsonObject2, "tilt", 0.0))));
                    }
                    emitter.ribbon(arrayList, YouziCombatGeometry.number(jsonObject2, "width", 0.0), vector5, d5);
                    break;
                }
                case "helix": {
                    double d13;
                    for (int i = 0; i < (int)YouziCombatGeometry.number(jsonObject2, "strands", 0.0); ++i) {
                        ArrayList<YouziHomingFlight.Vector> arrayList = new ArrayList<YouziHomingFlight.Vector>();
                        for (int j = 0; j <= n; ++j) {
                            double d14 = (double)j / (double)n;
                            d13 = d14 * YouziCombatGeometry.number(jsonObject2, "turns", 0.0) * Math.PI * 2.0 + Math.toRadians(d9) + (double)i * Math.PI;
                            double d15 = YouziCombatGeometry.number(jsonObject2, "radius", 0.0) * d8;
                            arrayList.add(vector4.add(new YouziHomingFlight.Vector(Math.cos(d13) * d15, d14 * YouziCombatGeometry.number(jsonObject2, "height", 0.0) * d8, Math.sin(d13) * d15)));
                        }
                        emitter.ribbon(arrayList, YouziCombatGeometry.number(jsonObject2, "width", 0.0), vector5, d5);
                    }
                    continue block14;
                }
                case "link": {
                    double d13;
                    double d11;
                    int n2;
                    ArrayList<YouziHomingFlight.Vector> arrayList = new ArrayList<YouziHomingFlight.Vector>();
                    for (n2 = 0; n2 <= n; ++n2) {
                        d11 = (double)n2 / (double)n;
                        YouziHomingFlight.Vector vector7 = vector3.multiply(d11);
                        d13 = YouziCombatGeometry.number(jsonObject2, "curve", 0.0) * Math.sin(Math.PI * d11) + (string.equals("echo_trace") ? 0.0 : 0.75);
                        arrayList.add(vector7.add(new YouziHomingFlight.Vector(0.0, d13, 0.0)));
                    }
                    emitter.ribbon(arrayList, YouziCombatGeometry.number(jsonObject2, "width", 0.0), vector5, d5);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Unknown combat primitive: " + string2);
                }
            }
        }
        if (emitter.quads.size() > this.specification.get("maximumQuadsPerEffect").getAsInt()) {
            throw new IllegalStateException("Combat geometry exceeds bounded buffer");
        }
        return List.copyOf(emitter.quads);
    }

    private static double number(JsonObject jsonObject, String string, double d) {
        return jsonObject.has(string) ? jsonObject.get(string).getAsDouble() : d;
    }

    private static boolean bool(JsonObject jsonObject, String string) {
        return jsonObject.has(string) && jsonObject.get(string).getAsBoolean();
    }

    private static YouziHomingFlight.Vector vector(JsonArray jsonArray) {
        return jsonArray == null ? new YouziHomingFlight.Vector(0.0, 0.0, 0.0) : new YouziHomingFlight.Vector(jsonArray.get(0).getAsDouble(), jsonArray.get(1).getAsDouble(), jsonArray.get(2).getAsDouble());
    }

    private static double clamp(double d) {
        return Math.max(0.0, Math.min(1.0, d));
    }

    private static YouziHomingFlight.Vector rotate(YouziHomingFlight.Vector vector, double d, double d2) {
        double d3 = Math.toRadians(d2);
        double d4 = Math.toRadians(d);
        double d5 = vector.coordinateY() * Math.cos(d3) - vector.coordinateZ() * Math.sin(d3);
        double d6 = vector.coordinateY() * Math.sin(d3) + vector.coordinateZ() * Math.cos(d3);
        return new YouziHomingFlight.Vector(vector.coordinateX() * Math.cos(d4) + d6 * Math.sin(d4), d5, -vector.coordinateX() * Math.sin(d4) + d6 * Math.cos(d4));
    }

    private static final class Emitter {
        private final YouziHomingFlight.Vector origin;
        private final double scale;
        private final double yaw;
        private final List<Quad> quads = new ArrayList<Quad>();

        private Emitter(YouziHomingFlight.Vector vector, double d, double d2) {
            this.origin = vector;
            this.scale = d;
            this.yaw = d2;
        }

        private YouziHomingFlight.Vector world(YouziHomingFlight.Vector vector) {
            return this.origin.add(YouziCombatGeometry.rotate(vector, this.yaw, 0.0).multiply(this.scale));
        }

        private void emit(YouziHomingFlight.Vector vector, YouziHomingFlight.Vector vector2, YouziHomingFlight.Vector vector3, YouziHomingFlight.Vector vector4, YouziHomingFlight.Vector vector5, double d) {
            this.quads.add(new Quad(this.world(vector), this.world(vector2), this.world(vector3), this.world(vector4), vector5, d));
        }

        private void diamond(YouziHomingFlight.Vector vector, double d, YouziHomingFlight.Vector vector2, double d2, double d3) {
            YouziHomingFlight.Vector[] vectorArray = new YouziHomingFlight.Vector[]{new YouziHomingFlight.Vector(0.0, d, 0.0), new YouziHomingFlight.Vector(0.0, -d, 0.0)};
            YouziHomingFlight.Vector[] vectorArray2 = new YouziHomingFlight.Vector[]{new YouziHomingFlight.Vector(d * 0.62, 0.0, 0.0), new YouziHomingFlight.Vector(0.0, 0.0, d * 0.62), new YouziHomingFlight.Vector(-d * 0.62, 0.0, 0.0), new YouziHomingFlight.Vector(0.0, 0.0, -d * 0.62)};
            for (YouziHomingFlight.Vector vector3 : vectorArray) {
                for (int i = 0; i < 4; ++i) {
                    YouziHomingFlight.Vector vector4 = vectorArray2[i];
                    YouziHomingFlight.Vector vector5 = vectorArray2[(i + 1) % 4];
                    YouziHomingFlight.Vector vector6 = vector4.add(vector5).multiply(0.5);
                    this.emit(vector.add(YouziCombatGeometry.rotate(vector3, d3, 0.0)), vector.add(YouziCombatGeometry.rotate(vector4, d3, 0.0)), vector.add(YouziCombatGeometry.rotate(vector6, d3, 0.0)), vector.add(YouziCombatGeometry.rotate(vector5, d3, 0.0)), vector2, d2);
                }
            }
        }

        private void ribbon(List<YouziHomingFlight.Vector> list, double d, YouziHomingFlight.Vector vector, double d2) {
            for (int i = 0; i < list.size() - 1; ++i) {
                YouziHomingFlight.Vector vector2;
                YouziHomingFlight.Vector vector3 = vector2.cross(Math.abs((vector2 = list.get(i + 1).subtract(list.get(i)).normalize()).coordinateY()) > 0.95 ? new YouziHomingFlight.Vector(1.0, 0.0, 0.0) : new YouziHomingFlight.Vector(0.0, 1.0, 0.0)).normalize();
                YouziHomingFlight.Vector vector4 = vector2.cross(vector3).normalize();
                double d3 = Math.sin(Math.PI * ((double)i + 0.5) / (double)(list.size() - 1)) * 0.75 + 0.25;
                for (YouziHomingFlight.Vector vector5 : new YouziHomingFlight.Vector[]{vector3, vector4}) {
                    YouziHomingFlight.Vector vector6 = vector5.multiply(d * d3 * 0.5);
                    this.emit(list.get(i).subtract(vector6), list.get(i).add(vector6), list.get(i + 1).add(vector6), list.get(i + 1).subtract(vector6), vector, d2);
                }
            }
        }
    }

    public record Quad(YouziHomingFlight.Vector first, YouziHomingFlight.Vector second, YouziHomingFlight.Vector third, YouziHomingFlight.Vector fourth, YouziHomingFlight.Vector color, double opacity) {
    }
}

