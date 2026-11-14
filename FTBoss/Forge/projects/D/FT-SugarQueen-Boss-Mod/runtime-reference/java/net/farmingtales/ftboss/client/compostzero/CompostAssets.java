/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  net.minecraft.client.Minecraft
 *  net.minecraft.resources.ResourceLocation
 *  org.slf4j.LoggerFactory
 */
package net.farmingtales.ftboss.client.compostzero;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.LoggerFactory;

public final class CompostAssets {
    public static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath((String)"ftboss", (String)"compost_zero/materials.png");
    private static Map<String, Model> models;

    private CompostAssets() {
    }

    public static Model get(String string) {
        if (models == null) {
            HashMap<String, Model> hashMap = new HashMap<String, Model>();
            try (BufferedReader bufferedReader = Minecraft.m_91087_().m_91098_().m_215597_(ResourceLocation.fromNamespaceAndPath((String)"ftboss", (String)"compost_zero/models.json"));){
                Catalogue catalogue = (Catalogue)new Gson().fromJson((Reader)bufferedReader, Catalogue.class);
                if (catalogue == null || catalogue.models == null) {
                    throw new IOException("Missing compost catalogue");
                }
                for (Model model : catalogue.models) {
                    if (model.id == null || model.parts == null || model.parts.length > 80) {
                        throw new IOException("Invalid compost geometry");
                    }
                    for (Part part : model.parts) {
                        if (part.pivot.length != 3 || part.faces.length > 1600) {
                            throw new IOException("Invalid compost part");
                        }
                        for (Face face : part.faces) {
                            if (face.points.length < 3 || face.points.length > 4 || face.material < 0 || face.material >= 12 || !Double.isFinite(face.alpha)) {
                                throw new IOException("Invalid compost face");
                            }
                            double[][] dArray = face.points;
                            int n = dArray.length;
                            for (int i = 0; i < n; ++i) {
                                double[] dArray2;
                                for (double d : dArray2 = dArray[i]) {
                                    if (Double.isFinite(d) && !(Math.abs(d) > 32.0)) continue;
                                    throw new IOException("Invalid compost vertex");
                                }
                            }
                        }
                    }
                    hashMap.put(model.id, model);
                }
            }
            catch (IOException | RuntimeException exception) {
                LoggerFactory.getLogger((String)"ftboss-compost-zero").error("Cannot load independently authored compost spell resources", (Throwable)exception);
            }
            models = hashMap;
        }
        return models.get(string);
    }

    public static void reset() {
        models = null;
    }

    public static final class Catalogue {
        public Model[] models;
    }

    public static final class Model {
        public String id;
        public Part[] parts;
        public int faces;
        public int liteFaces;
    }

    public static final class Part {
        public String name;
        public String motion;
        public double[] pivot;
        public double[] params;
        public boolean detail;
        public Face[] faces;
    }

    public static final class Face {
        public double[][] points;
        public int material;
        public boolean glow;
        public double alpha;
    }
}

