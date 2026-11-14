/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  net.minecraft.client.Minecraft
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.Resource
 */
package net.farmingtales.ftboss.client.saya;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

public final class SayaModels {
    public static final Set<String> SPELLS = Set.of("camellia_blood_bloom", "camellia_sanctuary", "briar_procession", "scarlet_pollen", "camellia_rebirth", "flesh_bloom", "sanguine_suture");
    private static final Map<String, Model> CACHE = new HashMap<String, Model>();
    private static final Set<String> FAILED = new HashSet<String>();

    private SayaModels() {
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static Model get(String string) {
        if (!SPELLS.contains(string) || FAILED.contains(string)) {
            return null;
        }
        if (CACHE.containsKey(string)) {
            return CACHE.get(string);
        }
        try (InputStream inputStream = ((Resource)Minecraft.m_91087_().m_91098_().m_213713_(new ResourceLocation("ftboss", "fx/saya/" + string + ".json")).orElseThrow()).m_215507_();){
            Model model;
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);){
                Model model2 = (Model)new Gson().fromJson((Reader)inputStreamReader, Model.class);
                SayaModels.validate(model2);
                CACHE.put(string, model2);
                model = model2;
            }
            return model;
        }
        catch (Exception exception) {
            FAILED.add(string);
            System.err.println("[ftboss/saya] Failed to load " + string + ": " + String.valueOf(exception));
            return null;
        }
    }

    public static void clear() {
        CACHE.clear();
        FAILED.clear();
    }

    /*
     * Could not resolve type clashes
     */
    public static void validate(Model model) {
        if (model == null || !SPELLS.contains(model.id) || model.parts == null || model.parts.length > 300) {
            throw new IllegalArgumentException("Invalid Saya model");
        }
        int n = 0;
        for (Part part : model.parts) {
            if (part.vertices == null || part.faces == null || part.pivot == null || part.pivot.length != 3 || part.tile < 0 || part.tile > 11 || !Double.isFinite(part.delay)) {
                throw new IllegalArgumentException("Invalid part");
            }
            for (double d : part.pivot) {
                if (Double.isFinite(d) && !(Math.abs(d) > 8.0)) continue;
                throw new IllegalArgumentException("Invalid pivot");
            }
            for (double d : (double[])part.vertices) {
                if (((double)d).length != 3) {
                    throw new IllegalArgumentException("Invalid vertex");
                }
                for (void var13_22 : d) {
                    if (Double.isFinite((double)var13_22) && !(Math.abs((double)var13_22) > 8.0)) continue;
                    throw new IllegalArgumentException("Invalid coordinate");
                }
            }
            if (part.faceUvs == null || part.faceUvs.length != part.faces.length) {
                throw new IllegalArgumentException("Missing UVs");
            }
            part.triangleCount = 0;
            part.posedVertices = new double[part.vertices.length][3];
            for (int i = 0; i < part.faces.length; ++i) {
                int[] nArray = part.faces[i];
                if (nArray.length < 3 || nArray.length > 6) {
                    throw new IllegalArgumentException("Invalid face");
                }
                if (part.faceUvs[i].length != nArray.length) {
                    throw new IllegalArgumentException("UV size mismatch");
                }
                for (double[] dArray : part.faceUvs[i]) {
                    if (dArray.length != 2) {
                        throw new IllegalArgumentException("Invalid UV");
                    }
                    for (double d : dArray) {
                        if (Double.isFinite(d) && !(d < 0.0) && !(d > 1.0)) continue;
                        throw new IllegalArgumentException("UV outside atlas");
                    }
                }
                for (double[] dArray : (double[][])nArray) {
                    if (dArray >= 0 && dArray < part.vertices.length) continue;
                    throw new IllegalArgumentException("Invalid face index");
                }
                ++n;
                part.triangleCount += nArray.length - 2;
            }
        }
        if (n > 6000) {
            throw new IllegalArgumentException("Face budget exceeded");
        }
    }

    public static final class Model {
        public String id;
        public Part[] parts;
    }

    public static final class Part {
        public String name;
        public String motion;
        public double delay;
        public double[] pivot;
        public double[][] vertices;
        public int[][] faces;
        public double[][][] faceUvs;
        public int tile;
        public boolean glow;
        public transient int triangleCount;
        public transient double[][] posedVertices;
    }
}

