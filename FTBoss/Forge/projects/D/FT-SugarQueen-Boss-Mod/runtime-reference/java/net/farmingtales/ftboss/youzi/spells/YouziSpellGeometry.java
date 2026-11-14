/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package net.farmingtales.ftboss.youzi.spells;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.farmingtales.ftboss.youzi.YouziHomingFlight;

public final class YouziSpellGeometry {
    private static final YouziHomingFlight.Vector ZERO = new YouziHomingFlight.Vector(0.0, 0.0, 0.0);
    private static final YouziHomingFlight.Vector ONE = new YouziHomingFlight.Vector(1.0, 1.0, 1.0);
    private final Map<String, Model> models = new LinkedHashMap<String, Model>();

    public YouziSpellGeometry(JsonObject jsonObject, JsonObject jsonObject2, JsonObject jsonObject3) {
        if (!"youzi-authored-spells/v1".equals(jsonObject.get("schema").getAsString())) {
            throw new IllegalArgumentException("Unsupported Youzi geometry");
        }
        double d = jsonObject2.get("size").getAsDouble();
        for (JsonElement jsonElement : jsonObject.getAsJsonArray("models")) {
            Object object;
            JsonObject jsonObject42;
            Object object22;
            Object object32;
            Object object42;
            JsonObject jsonObject5 = jsonElement.getAsJsonObject();
            String string = jsonObject5.get("id").getAsString();
            ArrayList<Face> arrayList = new ArrayList<Face>();
            for (Object object42 : jsonObject5.getAsJsonArray("faces")) {
                object32 = object42.getAsJsonObject();
                object22 = jsonObject2.getAsJsonObject("tiles").getAsJsonObject(object32.get("texture").getAsString());
                jsonObject42 = new YouziHomingFlight.Vector[4];
                object = new double[4][2];
                for (int i = 0; i < 4; ++i) {
                    jsonObject42[i] = YouziSpellGeometry.vector(object32.getAsJsonArray("points").get(i));
                    Object object5 = object32.getAsJsonArray("uv").get(i).getAsJsonArray();
                    object[i][0] = (object22.get("x").getAsDouble() + object5.get(0).getAsDouble()) / d;
                    object[i][1] = (object22.get("y").getAsDouble() + object5.get(1).getAsDouble()) / d;
                }
                double d2 = object32.get("opacity").getAsDouble();
                double d3 = object32.get("emission").getAsDouble();
                if (!Double.isFinite(d2) || d2 < 0.0 || d2 > 1.0 || !Double.isFinite(d3)) {
                    throw new IllegalArgumentException("Invalid face material");
                }
                arrayList.add(new Face((YouziHomingFlight.Vector[])jsonObject42, (double[][])object, d2, d3));
            }
            ArrayList arrayList2 = new ArrayList();
            for (Object object32 : jsonObject5.getAsJsonArray("pieces")) {
                object22 = object32.getAsJsonObject();
                jsonObject42 = new ArrayList();
                object = object22.getAsJsonArray("faces").iterator();
                while (object.hasNext()) {
                    JsonElement jsonElement2 = (JsonElement)object.next();
                    jsonObject42.add((Face)arrayList.get(jsonElement2.getAsInt()));
                }
                object = object22.get("name").getAsString();
                arrayList2.add(new Piece((String)object, YouziSpellGeometry.vector(object22.get("pivot")), List.copyOf(jsonObject42), "accent".equals(object22.get("layer").getAsString()), ((String)object).startsWith("star_bolt_")));
            }
            object42 = new LinkedHashMap();
            for (Object object22 : jsonObject5.getAsJsonArray("animations")) {
                jsonObject42 = object22.getAsJsonObject();
                object = new LinkedHashMap();
                for (Object object5 : jsonObject42.getAsJsonObject("pieces").entrySet()) {
                    object.put((String)object5.getKey(), new Track(YouziSpellGeometry.keys((JsonElement)object5.getValue(), "position"), YouziSpellGeometry.keys((JsonElement)object5.getValue(), "rotation"), YouziSpellGeometry.keys((JsonElement)object5.getValue(), "scale")));
                }
                String string2 = jsonObject42.get("name").getAsString();
                object42.put(string2.substring(string2.lastIndexOf(46) + 1), new Clip(jsonObject42.get("length").getAsDouble(), "loop".equals(jsonObject42.get("loop").getAsString()), Map.copyOf(object)));
            }
            object32 = null;
            for (JsonObject jsonObject42 : jsonObject3.getAsJsonArray("models")) {
                if (!string.equals(jsonObject42.getAsJsonObject().get("id").getAsString())) continue;
                object32 = jsonObject42.getAsJsonObject();
            }
            if (object32 == null || this.models.containsKey(string)) {
                throw new IllegalArgumentException("Missing or duplicate placement");
            }
            object22 = YouziSpellGeometry.vector(object32.get("suggestedOffsetForProxyTest"));
            this.models.put(string, new Model(List.copyOf(arrayList2), Map.copyOf(object42), object32.get("worldScale").getAsDouble(), (YouziHomingFlight.Vector)object22, YouziSpellGeometry.vector(object32.get("animationEnvelopeMinimum")).subtract((YouziHomingFlight.Vector)object22), YouziSpellGeometry.vector(object32.get("animationEnvelopeMaximum")).subtract((YouziHomingFlight.Vector)object22)));
        }
        if (this.models.size() != 3) {
            throw new IllegalArgumentException("All three Youzi spells required");
        }
    }

    private static YouziHomingFlight.Vector vector(JsonElement jsonElement) {
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        if (jsonArray.size() != 3) {
            throw new IllegalArgumentException("Three coordinates required");
        }
        return new YouziHomingFlight.Vector(jsonArray.get(0).getAsDouble(), jsonArray.get(1).getAsDouble(), jsonArray.get(2).getAsDouble());
    }

    private static List<Key> keys(JsonElement jsonElement, String string) {
        ArrayList<Key> arrayList = new ArrayList<Key>();
        for (JsonElement jsonElement2 : jsonElement.getAsJsonArray()) {
            JsonObject jsonObject = jsonElement2.getAsJsonObject();
            if (!string.equals(jsonObject.get("channel").getAsString())) continue;
            JsonObject jsonObject2 = jsonObject.getAsJsonArray("data_points").get(0).getAsJsonObject();
            arrayList.add(new Key(jsonObject.get("time").getAsDouble(), new YouziHomingFlight.Vector(jsonObject2.get("x").getAsDouble(), jsonObject2.get("y").getAsDouble(), jsonObject2.get("z").getAsDouble())));
        }
        arrayList.sort(Comparator.comparingDouble(Key::time));
        return List.copyOf(arrayList);
    }

    private static YouziHomingFlight.Vector sample(List<Key> list, double d, YouziHomingFlight.Vector vector) {
        if (list.isEmpty()) {
            return vector;
        }
        if (d <= list.get(0).time()) {
            return list.get(0).value();
        }
        int n = 0;
        int n2 = list.size() - 1;
        if (d >= list.get(n2).time()) {
            return list.get(n2).value();
        }
        while (n2 - n > 1) {
            int n3 = n + n2 >>> 1;
            if (list.get(n3).time() < d) {
                n = n3;
                continue;
            }
            n2 = n3;
        }
        Key key = list.get(n);
        Key key2 = list.get(n2);
        return key.value().lerp(key2.value(), (d - key.time()) / Math.max(1.0E-9, key2.time() - key.time()));
    }

    public static YouziHomingFlight.Vector rotate(YouziHomingFlight.Vector vector, YouziHomingFlight.Vector vector2) {
        double[] dArray = new double[]{vector.coordinateX(), vector.coordinateY(), vector.coordinateZ()};
        double[] dArray2 = new double[]{vector2.coordinateX(), vector2.coordinateY(), vector2.coordinateZ()};
        for (int i = 0; i < 3; ++i) {
            double d = Math.toRadians(dArray2[i]);
            double d2 = Math.cos(d);
            double d3 = Math.sin(d);
            int n = (i + 1) % 3;
            int n2 = (i + 2) % 3;
            double d4 = dArray[n];
            double d5 = dArray[n2];
            dArray[n] = d4 * d2 - d5 * d3;
            dArray[n2] = d4 * d3 + d5 * d2;
        }
        return new YouziHomingFlight.Vector(dArray[0], dArray[1], dArray[2]);
    }

    public double length(String string, String string2) {
        return this.model(string).clips().get(string2).length();
    }

    private Model model(String string) {
        Model model = this.models.get(string);
        if (model == null) {
            throw new IllegalArgumentException("Unknown spell");
        }
        return model;
    }

    public YouziHomingFlight.Vector offset(String string, double d, boolean bl) {
        Model model = this.model(string);
        if (bl) {
            return new YouziHomingFlight.Vector(-(model.minimum().coordinateX() + model.maximum().coordinateX()) * d / 2.0, 0.2 - model.minimum().coordinateY() * d, -(model.minimum().coordinateZ() + model.maximum().coordinateZ()) * d / 2.0);
        }
        return model.offset().add(new YouziHomingFlight.Vector((1.0 - d) * model.minimum().coordinateX(), Math.max(0.0, 0.2 - (model.offset().coordinateY() + model.minimum().coordinateY() * d)), (1.0 - d) * model.maximum().coordinateZ()));
    }

    public void sample(String string, String string2, double d, double d2, boolean bl, boolean bl2, int n, Consumer<Quad> consumer) {
        if (!Double.isFinite(d) || !Double.isFinite(d2) || d2 <= 0.0 || d2 > 4.0 || n < 1) {
            throw new IllegalArgumentException("Invalid runtime frame");
        }
        Model model = this.model(string);
        Clip clip = model.clips().get(string2);
        double d3 = clip.loop() ? Math.max(0.0, d) % clip.length() : Math.max(0.0, Math.min(clip.length(), d));
        YouziHomingFlight.Vector vector = this.offset(string, d2, bl);
        for (Piece piece : model.pieces()) {
            YouziHomingFlight.Vector vector2;
            if (piece.bolt() && !bl2) continue;
            Track track = clip.tracks().get(piece.name());
            YouziHomingFlight.Vector vector3 = track == null ? ZERO : YouziSpellGeometry.sample(track.position(), d3, ZERO).multiply(0.0625);
            YouziHomingFlight.Vector vector4 = track == null ? ZERO : YouziSpellGeometry.sample(track.rotation(), d3, ZERO);
            YouziHomingFlight.Vector vector5 = YouziSpellGeometry.rotate(new YouziHomingFlight.Vector(1.0, 0.0, 0.0), vector4);
            YouziHomingFlight.Vector vector6 = YouziSpellGeometry.rotate(new YouziHomingFlight.Vector(0.0, 1.0, 0.0), vector4);
            YouziHomingFlight.Vector vector7 = YouziSpellGeometry.rotate(new YouziHomingFlight.Vector(0.0, 0.0, 1.0), vector4);
            YouziHomingFlight.Vector vector8 = vector2 = track == null ? ONE : YouziSpellGeometry.sample(track.scale(), d3, ONE);
            if (Math.abs(vector2.coordinateX() * vector2.coordinateY() * vector2.coordinateZ()) < 1.0E-9) continue;
            int n2 = 0;
            for (Face face : piece.faces()) {
                if (piece.accent() && n2++ % n != 0) continue;
                YouziHomingFlight.Vector[] vectorArray = new YouziHomingFlight.Vector[4];
                for (int i = 0; i < 4; ++i) {
                    YouziHomingFlight.Vector vector9 = face.points()[i].subtract(piece.pivot());
                    vector9 = new YouziHomingFlight.Vector(vector9.coordinateX() * vector2.coordinateX(), vector9.coordinateY() * vector2.coordinateY(), vector9.coordinateZ() * vector2.coordinateZ());
                    vectorArray[i] = vector5.multiply(vector9.coordinateX()).add(vector6.multiply(vector9.coordinateY())).add(vector7.multiply(vector9.coordinateZ())).add(piece.pivot()).add(vector3).multiply(model.scale() * d2).add(vector);
                }
                consumer.accept(new Quad(vectorArray, face.uv(), face.opacity(), face.emission(), piece.accent()));
            }
        }
    }

    public void seed(double d, Consumer<Quad> consumer) {
        Model model = this.model("effigy_volley");
        Piece piece2 = model.pieces().stream().filter(piece -> piece.name().equals("star_bolt_1/cut_crystal_seed")).findFirst().orElseThrow();
        double d2 = 0.0;
        for (Face face : piece2.faces()) {
            for (YouziHomingFlight.Vector vector : face.points()) {
                d2 = Math.max(d2, vector.subtract(piece2.pivot()).length() * model.scale());
            }
        }
        double d3 = model.scale() * d * Math.min(1.0, 0.1 / Math.max(1.0E-9, d2));
        for (Face face : piece2.faces()) {
            YouziHomingFlight.Vector[] vectorArray = new YouziHomingFlight.Vector[4];
            for (int i = 0; i < 4; ++i) {
                vectorArray[i] = face.points()[i].subtract(piece2.pivot()).multiply(d3);
            }
            consumer.accept(new Quad(vectorArray, face.uv(), face.opacity(), face.emission(), false));
        }
    }

    public YouziHomingFlight.Vector launchOrigin(int n, double d) {
        Model model = this.model("effigy_volley");
        String string = "star_bolt_" + (n + 1) + "/cut_crystal_seed";
        Piece piece2 = model.pieces().stream().filter(piece -> piece.name().equals(string)).findFirst().orElseThrow();
        Track track = model.clips().get("release").tracks().get(string);
        YouziHomingFlight.Vector vector = YouziSpellGeometry.sample(track.position(), 1.0417 + (double)n * 0.25, ZERO).multiply(0.0625);
        return piece2.pivot().add(vector).multiply(model.scale() * d).add(this.offset("effigy_volley", d, false));
    }

    private record Face(YouziHomingFlight.Vector[] points, double[][] uv, double opacity, double emission) {
    }

    private record Piece(String name, YouziHomingFlight.Vector pivot, List<Face> faces, boolean accent, boolean bolt) {
    }

    private record Track(List<Key> position, List<Key> rotation, List<Key> scale) {
    }

    private record Clip(double length, boolean loop, Map<String, Track> tracks) {
    }

    private record Model(List<Piece> pieces, Map<String, Clip> clips, double scale, YouziHomingFlight.Vector offset, YouziHomingFlight.Vector minimum, YouziHomingFlight.Vector maximum) {
    }

    private record Key(double time, YouziHomingFlight.Vector value) {
    }

    public record Quad(YouziHomingFlight.Vector[] points, double[][] uv, double opacity, double emission, boolean accent) {
    }
}

