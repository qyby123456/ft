/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  org.joml.Quaterniond
 *  org.joml.Vector3d
 */
package net.farmingtales.ftboss.client.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public final class GongdiSpellModel {
    private static final Map<String, Model> MODELS = GongdiSpellModel.load();

    private GongdiSpellModel() {
    }

    public static Model find(String string) {
        return MODELS.get(string);
    }

    public static Map<String, Model> all() {
        return MODELS;
    }

    private static double[] numbers(JsonArray jsonArray) {
        double[] dArray = new double[jsonArray.size()];
        for (int i = 0; i < dArray.length; ++i) {
            dArray[i] = jsonArray.get(i).getAsDouble();
            if (Double.isFinite(dArray[i]) && !(Math.abs(dArray[i]) > 4096.0)) continue;
            throw new IllegalStateException("Nonfinite Gongdi data");
        }
        return dArray;
    }

    private static double[][] matrix(JsonArray jsonArray) {
        double[][] dArrayArray = new double[jsonArray.size()][];
        for (int i = 0; i < dArrayArray.length; ++i) {
            dArrayArray[i] = GongdiSpellModel.numbers(jsonArray.get(i).getAsJsonArray());
        }
        return dArrayArray;
    }

    private static Map<String, Model> load() {
        Map map;
        block18: {
            InputStream inputStream = GongdiSpellModel.class.getResourceAsStream("/assets/ftboss/fx/gongdi_spells.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Gongdi model resource missing");
                }
                JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
                LinkedHashMap<String, Model> linkedHashMap = new LinkedHashMap<String, Model>();
                for (JsonElement jsonElement : jsonObject.getAsJsonArray("models")) {
                    JsonObject jsonObject2 = jsonElement.getAsJsonObject();
                    JsonArray jsonArray = jsonObject2.getAsJsonArray("parts");
                    Part[] partArray = new Part[jsonArray.size()];
                    for (int i = 0; i < partArray.length; ++i) {
                        JsonObject jsonObject3 = jsonArray.get(i).getAsJsonObject();
                        JsonArray jsonArray2 = jsonObject3.getAsJsonArray("faces");
                        Face[] faceArray = new Face[jsonArray2.size()];
                        for (int j = 0; j < faceArray.length; ++j) {
                            JsonObject jsonObject4 = jsonArray2.get(j).getAsJsonObject();
                            faceArray[j] = new Face(GongdiSpellModel.matrix(jsonObject4.getAsJsonArray("points")), GongdiSpellModel.matrix(jsonObject4.getAsJsonArray("uv")), jsonObject4.get("material").getAsString(), jsonObject4.get("alpha").getAsDouble(), jsonObject4.get("tint").getAsInt());
                            if (faceArray[j].points().length != 4 || faceArray[j].uv().length != 4) {
                                throw new IllegalStateException("Gongdi quad required");
                            }
                            Object object = faceArray[j].uv();
                            int n = ((Object)object).length;
                            for (int k = 0; k < n; ++k) {
                                for (Object object2 : object = object[k]) {
                                    if (!(object2 < 0.0) && !(object2 > 1.0)) continue;
                                    throw new IllegalStateException("Gongdi UV outside texture");
                                }
                            }
                        }
                        LinkedHashMap<String, Key[]> linkedHashMap2 = new LinkedHashMap<String, Key[]>();
                        for (Object object : jsonObject3.getAsJsonObject("motion").entrySet()) {
                            JsonArray jsonArray3 = ((JsonElement)object.getValue()).getAsJsonArray();
                            Key[] keyArray = new Key[jsonArray3.size()];
                            for (int j = 0; j < keyArray.length; ++j) {
                                JsonObject jsonObject4 = jsonArray3.get(j).getAsJsonObject();
                                keyArray[j] = new Key(jsonObject4.get("time").getAsDouble(), GongdiSpellModel.numbers(jsonObject4.getAsJsonArray("position")), GongdiSpellModel.numbers(jsonObject4.getAsJsonArray("rotation")), GongdiSpellModel.numbers(jsonObject4.getAsJsonArray("scale")), jsonObject4.get("alpha").getAsDouble(), jsonObject4.get("easing").getAsString());
                            }
                            linkedHashMap2.put((String)object.getKey(), keyArray);
                        }
                        partArray[i] = new Part(jsonObject3.get("name").getAsString(), GongdiSpellModel.numbers(jsonObject3.getAsJsonArray("pivot")), faceArray, Map.copyOf(linkedHashMap2), jsonObject3.has("sideSign") ? jsonObject3.get("sideSign").getAsInt() : 0, jsonObject3.has("telegraph") && jsonObject3.get("telegraph").getAsBoolean());
                    }
                    String string = jsonObject2.get("id").getAsString();
                    linkedHashMap.put(string, new Model(string, jsonObject2.get("anchor").getAsString(), partArray, jsonObject2.get("span").getAsDouble()));
                }
                if (!linkedHashMap.keySet().equals(Set.of("rending_cleave", "crimson_oath", "blood_verdict"))) {
                    throw new IllegalStateException("Gongdi-only scope mismatch");
                }
                map = Map.copyOf(linkedHashMap);
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
                    throw new IllegalStateException("Cannot load independent Gongdi models", exception);
                }
            }
            inputStream.close();
        }
        return map;
    }

    public static State sample(Part part, String string, double d) {
        Key[] keyArray = part.motion().getOrDefault(string, part.motion().get("RELEASE"));
        Key key = keyArray[0];
        Key key2 = keyArray[keyArray.length - 1];
        for (int i = 1; i < keyArray.length; ++i) {
            if (!(d <= keyArray[i].time())) continue;
            key = keyArray[i - 1];
            key2 = keyArray[i];
            break;
        }
        double d2 = Math.max(0.0, Math.min(1.0, (d - key.time()) / Math.max(1.0E-5, key2.time() - key.time())));
        d2 = switch (key2.easing()) {
            case "easeInOutSine" -> (1.0 - Math.cos(Math.PI * d2)) / 2.0;
            case "easeOutQuad" -> 1.0 - (1.0 - d2) * (1.0 - d2);
            case "easeInQuad" -> d2 * d2;
            default -> d2;
        };
        Object object = new double[3];
        double[] dArray = new double[3];
        double[] dArray2 = new double[3];
        for (int i = 0; i < 3; ++i) {
            object[i] = key.position()[i] + (key2.position()[i] - key.position()[i]) * d2;
            dArray[i] = key.rotation()[i] + (key2.rotation()[i] - key.rotation()[i]) * d2;
            dArray2[i] = key.scale()[i] + (key2.scale()[i] - key.scale()[i]) * d2;
        }
        return new State((double[])object, dArray, dArray2, key.alpha() + (key2.alpha() - key.alpha()) * d2);
    }

    public static Quaterniond rotation(State state) {
        return new Quaterniond().rotationZ(Math.toRadians(state.rotation()[2])).rotateY(Math.toRadians(state.rotation()[1])).rotateX(Math.toRadians(state.rotation()[0]));
    }

    public static double[] pose(double[] dArray, Part part, State state) {
        return GongdiSpellModel.pose(dArray, part, state, GongdiSpellModel.rotation(state));
    }

    public static double[] pose(double[] dArray, Part part, State state, Quaterniond quaterniond) {
        Vector3d vector3d = new Vector3d((dArray[0] - part.pivot()[0]) * state.scale()[0], (dArray[1] - part.pivot()[1]) * state.scale()[1], (dArray[2] - part.pivot()[2]) * state.scale()[2]);
        quaterniond.transform(vector3d);
        return new double[]{vector3d.x + part.pivot()[0] + state.position()[0], vector3d.y + part.pivot()[1] + state.position()[1], vector3d.z + part.pivot()[2] + state.position()[2]};
    }

    public record Model(String id, String anchor, Part[] parts, double span) {
    }

    public record Part(String name, double[] pivot, Face[] faces, Map<String, Key[]> motion, int sideSign, boolean telegraph) {
    }

    public record Face(double[][] points, double[][] uv, String material, double alpha, int tint) {
    }

    public record Key(double time, double[] position, double[] rotation, double[] scale, double alpha, String easing) {
    }

    public record State(double[] position, double[] rotation, double[] scale, double alpha) {
    }
}

