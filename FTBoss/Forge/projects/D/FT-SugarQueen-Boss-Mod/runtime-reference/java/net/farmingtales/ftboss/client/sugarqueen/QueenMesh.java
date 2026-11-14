/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonArray
 *  org.joml.Matrix3f
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.farmingtales.ftboss.client.sugarqueen;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class QueenMesh {
    private static final Map<String, QueenMesh> CACHE = new HashMap<String, QueenMesh>();
    public String id;
    public String anchor;
    public double duration;
    public List<Bone> bones;
    public List<Part> parts;

    public static QueenMesh load(String string2) {
        return CACHE.computeIfAbsent(string2, string -> {
            QueenMesh queenMesh;
            block9: {
                String string2 = "/assets/ftboss/sugarqueen/" + string + ".json";
                InputStream inputStream = QueenMesh.class.getResourceAsStream(string2);
                try {
                    if (inputStream == null) {
                        throw new IllegalStateException("Missing Queen mesh " + string2);
                    }
                    QueenMesh queenMesh2 = (QueenMesh)new Gson().fromJson((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8), QueenMesh.class);
                    queenMesh2.prepare();
                    queenMesh = queenMesh2;
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
                        throw new IllegalStateException("Cannot read Queen mesh " + string2, iOException);
                    }
                }
                inputStream.close();
            }
            return queenMesh;
        });
    }

    public Map<String, Matrix4f> pose(float f) {
        this.updatePose(f);
        HashMap<String, Matrix4f> hashMap = new HashMap<String, Matrix4f>();
        for (Bone bone : this.bones) {
            hashMap.put(bone.name, new Matrix4f((Matrix4fc)bone.transform));
        }
        return hashMap;
    }

    public void updatePose(float f) {
        for (Bone bone : this.bones) {
            QueenMesh.sampleInto(bone, f, bone.sampled);
            double[] dArray = bone.pivot;
            double[] dArray2 = bone.sampled[0];
            double[] dArray3 = bone.sampled[1];
            double[] dArray4 = bone.sampled[2];
            bone.visible = Math.max(dArray4[0], Math.max(dArray4[1], dArray4[2])) >= 0.006;
            bone.transform.translation((float)(dArray[0] + dArray2[0]), (float)(dArray[1] + dArray2[1]), (float)(dArray[2] + dArray2[2])).rotateXYZ((float)Math.toRadians(dArray3[0]), (float)Math.toRadians(dArray3[1]), (float)Math.toRadians(dArray3[2])).scale((float)dArray4[0], (float)dArray4[1], (float)dArray4[2]).translate((float)(-dArray[0]), (float)(-dArray[1]), (float)(-dArray[2]));
        }
    }

    public static double[][] sample(Bone bone, float f) {
        double[][] dArray = new double[3][3];
        QueenMesh.sampleInto(bone, f, dArray);
        return dArray;
    }

    private static void sampleInto(Bone bone, float f, double[][] dArray) {
        double d;
        int n = 0;
        int n2 = bone.times.length - 1;
        for (int i = 1; i < bone.times.length; ++i) {
            if (!((double)f <= bone.times[i])) continue;
            n = i - 1;
            n2 = i;
            break;
        }
        double d2 = (d = bone.times[n2] - bone.times[n]) <= 0.0 ? 0.0 : Math.max(0.0, Math.min(1.0, ((double)f - bone.times[n]) / d));
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                double d3 = bone.channels[n][i][j];
                double d4 = bone.channels[n2][i][j];
                dArray[i][j] = d3 + (d4 - d3) * d2;
            }
        }
    }

    private void prepare() {
        HashMap<String, Bone> hashMap = new HashMap<String, Bone>();
        for (Bone object : this.bones) {
            if (hashMap.put(object.name, object) != null) {
                throw new IllegalStateException("Duplicate Queen bone " + object.name);
            }
            object.times = new double[object.frames.size()];
            object.channels = new double[object.frames.size()][3][3];
            for (int i = 0; i < object.frames.size(); ++i) {
                JsonArray jsonArray = object.frames.get(i).getAsJsonArray();
                object.times[i] = jsonArray.get(0).getAsDouble();
                for (int j = 0; j < 3; ++j) {
                    for (int k = 0; k < 3; ++k) {
                        object.channels[i][j][k] = jsonArray.get(j + 1).getAsJsonArray().get(k).getAsDouble();
                    }
                }
            }
        }
        for (Part part : this.parts) {
            part.binding = (Bone)hashMap.get(part.bone);
            if (part.binding == null) {
                throw new IllegalStateException("Missing Queen bone " + part.bone);
            }
            part.cameraVertices = new Vector3f[part.vertices.length];
            part.viewVertices = new Vector3f[part.vertices.length];
            for (int i = 0; i < part.vertices.length; ++i) {
                part.cameraVertices[i] = new Vector3f();
                part.viewVertices[i] = new Vector3f();
            }
            for (Face face : part.faces) {
                Vector3f vector3f = QueenMesh.vertex(part.vertices[face.indices[0]], new Matrix4f());
                Vector3f vector3f2 = QueenMesh.vertex(part.vertices[face.indices[1]], new Matrix4f());
                Vector3f vector3f3 = QueenMesh.vertex(part.vertices[face.indices[2]], new Matrix4f());
                face.localNormal = vector3f2.sub((Vector3fc)vector3f).cross((Vector3fc)vector3f3.sub((Vector3fc)vector3f)).normalize();
                face.normalizedUv = new float[face.indices.length][2];
                for (int i = 0; i < face.indices.length; ++i) {
                    face.normalizedUv[i][0] = (float)(face.uv[i][0] / 256.0);
                    face.normalizedUv[i][1] = (float)(face.uv[i][1] / 128.0);
                }
            }
        }
    }

    public static void transformPart(Part part, Matrix4f matrix4f, Matrix4f matrix4f2) {
        part.transform.set((Matrix4fc)matrix4f).mul((Matrix4fc)part.binding.transform);
        part.viewTransform.set((Matrix4fc)matrix4f2).mul((Matrix4fc)part.transform);
        part.normalTransform.set((Matrix4fc)part.transform).invert().transpose();
        for (int i = 0; i < part.vertices.length; ++i) {
            double[] dArray = part.vertices[i];
            part.cameraVertices[i].set((float)dArray[0], (float)dArray[1], (float)dArray[2]);
            part.viewVertices[i].set((Vector3fc)part.cameraVertices[i]);
            part.transform.transformPosition(part.cameraVertices[i]);
            part.viewTransform.transformPosition(part.viewVertices[i]);
        }
    }

    public static Vector3f vertex(double[] dArray, Matrix4f matrix4f) {
        return matrix4f.transformPosition(new Vector3f((float)dArray[0], (float)dArray[1], (float)dArray[2]));
    }

    public static final class Bone {
        public String name;
        public double[] pivot;
        public JsonArray frames;
        public transient double[] times;
        public transient double[][][] channels;
        public transient double[][] sampled = new double[3][3];
        public transient Matrix4f transform = new Matrix4f();
        public transient boolean visible;
    }

    public static final class Part {
        public String name;
        public String bone;
        public double[][] vertices;
        public List<Face> faces;
        public transient Bone binding;
        public transient Vector3f[] cameraVertices;
        public transient Vector3f[] viewVertices;
        public transient Matrix4f transform = new Matrix4f();
        public transient Matrix4f viewTransform = new Matrix4f();
        public transient Matrix3f normalTransform = new Matrix3f();
    }

    public static final class Face {
        public int[] indices;
        public double[][] uv;
        public String material;
        public transient Vector3f localNormal;
        public transient float[][] normalizedUv;
    }
}

