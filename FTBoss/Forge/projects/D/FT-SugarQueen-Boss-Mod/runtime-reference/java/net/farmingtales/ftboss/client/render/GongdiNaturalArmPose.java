/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  org.joml.Quaterniond
 *  org.joml.Quaterniondc
 *  org.joml.Vector3d
 *  org.joml.Vector3dc
 */
package net.farmingtales.ftboss.client.render;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import net.farmingtales.ftboss.client.render.GongdiBasicAttackSampler;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public final class GongdiNaturalArmPose {
    private static Vector3d position(GongdiBasicAttackSampler.Bone bone) {
        return new Vector3d(bone.position());
    }

    private static Quaterniond rotation(GongdiBasicAttackSampler.Bone bone) {
        return new Quaterniond(bone.quaternion()[0], bone.quaternion()[1], bone.quaternion()[2], bone.quaternion()[3]).normalize();
    }

    private static GongdiBasicAttackSampler.Bone bone(Vector3d vector3d, Quaterniond quaterniond) {
        quaterniond.normalize();
        return new GongdiBasicAttackSampler.Bone(new double[]{vector3d.x, vector3d.y, vector3d.z}, new double[]{quaterniond.x, quaterniond.y, quaterniond.z, quaterniond.w});
    }

    private static Vector3d elbow(Vector3d vector3d, Vector3d vector3d2, Quaterniond quaterniond, double d, double d2, boolean bl) {
        Quaterniond quaterniond2 = new Quaterniond((Quaterniondc)quaterniond).invert();
        Vector3d vector3d3 = new Vector3d((Vector3dc)vector3d2).sub((Vector3dc)vector3d).rotate((Quaterniondc)quaterniond2);
        double d3 = vector3d3.length();
        if (d3 <= Math.abs(d - d2) + 1.0E-7 || d3 > d + d2 + 1.0E-7) {
            throw new IllegalStateException("Natural arm target outside authored reach");
        }
        Vector3d vector3d4 = new Vector3d((Vector3dc)vector3d3).div(d3);
        double d4 = (d * d - d2 * d2 + d3 * d3) / (2.0 * d3);
        double d5 = Math.sqrt(Math.max(0.0, d * d - d4 * d4));
        Vector3d vector3d5 = new Vector3d((Vector3dc)vector3d4).mul(d4);
        Vector3d vector3d6 = new Vector3d(1.0, 0.0, 0.0).sub((Vector3dc)new Vector3d((Vector3dc)vector3d4).mul(vector3d4.x));
        double d6 = vector3d6.length();
        if (d6 < 1.0E-7 || d5 < 1.0E-7) {
            return vector3d5.rotate((Quaterniondc)quaterniond).add((Vector3dc)vector3d);
        }
        vector3d6.div(d6);
        Vector3d vector3d7 = new Vector3d((Vector3dc)vector3d4).cross((Vector3dc)vector3d6).normalize();
        double d7 = bl ? -0.015 : 0.008;
        double d8 = Math.max(-1.0, Math.min(1.0, (d7 - vector3d5.x) / (d5 * d6)));
        double d9 = Math.sqrt(Math.max(0.0, 1.0 - d8 * d8));
        Vector3d vector3d8 = new Vector3d((Vector3dc)vector3d5).add((Vector3dc)new Vector3d((Vector3dc)vector3d6).mul(d5 * d8));
        Vector3d vector3d9 = new Vector3d((Vector3dc)vector3d8).add((Vector3dc)new Vector3d((Vector3dc)vector3d7).mul(d5 * d9));
        Vector3d vector3d10 = new Vector3d((Vector3dc)vector3d8).sub((Vector3dc)new Vector3d((Vector3dc)vector3d7).mul(d5 * d9));
        Vector3d vector3d11 = bl ? (vector3d9.y <= vector3d10.y ? vector3d9 : vector3d10) : (vector3d9.z <= vector3d10.z ? vector3d9 : vector3d10);
        return vector3d11.rotate((Quaterniondc)quaterniond).add((Vector3dc)vector3d);
    }

    public static GongdiBasicAttackSampler.Frame rebuild(GongdiBasicAttackSampler.Frame frame, double d, double d2) {
        LinkedHashMap<String, GongdiBasicAttackSampler.Bone> linkedHashMap = new LinkedHashMap<String, GongdiBasicAttackSampler.Bone>(frame.bones());
        Quaterniond quaterniond = GongdiNaturalArmPose.rotation(linkedHashMap.get("torso"));
        Vector3d vector3d = new Vector3d(0.0, -1.0, 0.0).rotate((Quaterniondc)quaterniond);
        for (String string : new String[]{"right", "left"}) {
            boolean bl = string.equals("right");
            Vector3d vector3d2 = GongdiNaturalArmPose.position(linkedHashMap.get(string + "_upper_arm"));
            Vector3d vector3d3 = GongdiNaturalArmPose.position(linkedHashMap.get(bl ? "weapon" : "left_wrist"));
            Vector3d vector3d4 = GongdiNaturalArmPose.elbow(vector3d2, vector3d3, quaterniond, d, d2, bl);
            Vector3d vector3d5 = new Vector3d((Vector3dc)vector3d4).sub((Vector3dc)vector3d2).normalize();
            Vector3d vector3d6 = new Vector3d((Vector3dc)vector3d3).sub((Vector3dc)vector3d4).normalize();
            Quaterniond quaterniond2 = new Quaterniond().rotationTo((Vector3dc)vector3d, (Vector3dc)vector3d5).mul((Quaterniondc)quaterniond).normalize();
            Quaterniond quaterniond3 = new Quaterniond().rotationTo((Vector3dc)vector3d5, (Vector3dc)vector3d6).mul((Quaterniondc)quaterniond2).normalize();
            linkedHashMap.put(string + "_upper_arm", GongdiNaturalArmPose.bone(vector3d2, quaterniond2));
            linkedHashMap.put(string + "_forearm", GongdiNaturalArmPose.bone(vector3d4, quaterniond3));
            linkedHashMap.put(string + "_wrist", GongdiNaturalArmPose.bone(vector3d3, quaterniond3));
        }
        return new GongdiBasicAttackSampler.Frame(frame.time(), Map.copyOf(linkedHashMap));
    }

    public static GongdiBasicAttackSampler.Frame apply(GongdiBasicAttackSampler.Frame frame) {
        return GongdiNaturalArmPose.rebuild(frame, Lengths.UPPER, Lengths.LOWER);
    }

    private GongdiNaturalArmPose() {
    }

    private static final class Lengths {
        static final double UPPER;
        static final double LOWER;

        private Lengths() {
        }

        static {
            try (InputStream inputStream = GongdiNaturalArmPose.class.getResourceAsStream("/assets/ftboss/fx/gongdi_basic_attacks.json");){
                if (inputStream == null) {
                    throw new IllegalStateException("Missing Gongdi limb rest geometry");
                }
                JsonObject jsonObject = JsonParser.parseReader((Reader)new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("rest");
                UPPER = jsonObject.getAsJsonArray("right_upper_arm").get(1).getAsDouble() - jsonObject.getAsJsonArray("right_forearm").get(1).getAsDouble();
                LOWER = jsonObject.getAsJsonArray("right_forearm").get(1).getAsDouble() - jsonObject.getAsJsonArray("right_wrist").get(1).getAsDouble();
                if (Math.abs(UPPER - 0.3125) > 1.0E-8 || LOWER < 0.2 || LOWER > 0.32) {
                    throw new IllegalStateException("Unknown Gongdi limb dimensions");
                }
            }
            catch (Exception exception) {
                throw new IllegalStateException("Cannot resolve Gongdi limb rest geometry", exception);
            }
        }
    }
}

