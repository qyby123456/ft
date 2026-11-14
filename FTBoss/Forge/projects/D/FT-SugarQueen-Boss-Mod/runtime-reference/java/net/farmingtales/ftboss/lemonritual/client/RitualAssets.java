/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package net.farmingtales.ftboss.lemonritual.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class RitualAssets {
    private static final List<Model> MODELS = RitualAssets.load();

    private RitualAssets() {
    }

    static JsonObject json(String string) {
        JsonObject jsonObject;
        block9: {
            InputStream inputStream = RitualAssets.class.getResourceAsStream("/assets/ftboss/lemon_ritual/" + string);
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Missing ritual asset: " + string);
                }
                jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
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
                    throw new IllegalStateException(iOException);
                }
            }
            inputStream.close();
        }
        return jsonObject;
    }

    private static List<Model> load() {
        ArrayList<Model> arrayList = new ArrayList<Model>();
        for (JsonElement jsonElement : RitualAssets.json("rituals.json").getAsJsonArray("models")) {
            JsonObject jsonObject;
            JsonObject jsonObject2 = jsonElement.getAsJsonObject();
            ArrayList<Bone> arrayList2 = new ArrayList<Bone>();
            ArrayList<Face> arrayList3 = new ArrayList<Face>();
            for (JsonElement jsonElement2 : jsonObject2.getAsJsonArray("bones")) {
                jsonObject = jsonElement2.getAsJsonObject();
                arrayList2.add(new Bone(jsonObject.get("id").getAsInt(), jsonObject.get("type").getAsString(), RitualAssets.vector(jsonObject.getAsJsonArray("pivot")), jsonObject.get("phase").getAsDouble()));
            }
            for (JsonElement jsonElement2 : jsonObject2.getAsJsonArray("faces")) {
                jsonObject = jsonElement2.getAsJsonObject();
                arrayList3.add(new Face(RitualAssets.matrix(jsonObject.getAsJsonArray("points")), RitualAssets.matrix(jsonObject.getAsJsonArray("uv")), jsonObject.get("bone").getAsInt(), jsonObject.get("opacity").getAsFloat()));
            }
            arrayList.add(new Model(jsonObject2.get("id").getAsString(), jsonObject2.get("life").getAsInt(), List.copyOf(arrayList2), List.copyOf(arrayList3)));
        }
        return List.copyOf(arrayList);
    }

    private static double[] vector(JsonArray jsonArray) {
        double[] dArray = new double[jsonArray.size()];
        for (int i = 0; i < dArray.length; ++i) {
            dArray[i] = jsonArray.get(i).getAsDouble();
        }
        return dArray;
    }

    private static double[][] matrix(JsonArray jsonArray) {
        double[][] dArrayArray = new double[jsonArray.size()][];
        for (int i = 0; i < dArrayArray.length; ++i) {
            dArrayArray[i] = RitualAssets.vector(jsonArray.get(i).getAsJsonArray());
        }
        return dArrayArray;
    }

    public static Model model(boolean bl) {
        return MODELS.get(bl ? 1 : 0);
    }

    public record Bone(int id, String type, double[] pivot, double phase) {
    }

    public record Face(double[][] points, double[][] uv, int bone, float opacity) {
    }

    public record Model(String id, int life, List<Bone> bones, List<Face> faces) {
    }
}

