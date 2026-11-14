/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  net.minecraft.client.Minecraft
 *  net.minecraft.resources.ResourceLocation
 *  software.bernie.geckolib.cache.object.GeoBone
 *  software.bernie.geckolib.core.animatable.GeoAnimatable
 *  software.bernie.geckolib.core.animation.AnimationState
 *  software.bernie.geckolib.core.state.BoneSnapshot
 */
package net.farmingtales.ftboss.client.defectzero;

import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.farmingtales.ftboss.client.defectzero.DefectSkillHost;
import net.farmingtales.ftboss.client.defectzero.DefectSkillSync;
import net.farmingtales.ftboss.client.model.DefectCoreGeoModel;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.state.BoneSnapshot;

public final class DefectSkillGeoModel
extends DefectCoreGeoModel {
    private static Document data;
    private final Pose scratchPose = new Pose();

    public static void invalidate() {
        data = null;
    }

    public static Document document() {
        if (data == null) {
            try (InputStream inputStream = Minecraft.m_91087_().m_91098_().m_215593_(ResourceLocation.m_214293_((String)"ftboss", (String)"defectzero/skeleton_actions.json")).m_215507_();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);){
                data = (Document)new Gson().fromJson((Reader)inputStreamReader, Document.class);
            }
            catch (Exception exception) {
                throw new IllegalStateException("Cannot load robot skill skeleton", exception);
            }
        }
        return data;
    }

    public void setCustomAnimations(DefectCoreEntity defectCoreEntity, long l, AnimationState<DefectCoreEntity> animationState) {
        DefectSkillHost defectSkillHost;
        Object object;
        block7: {
            block6: {
                super.setCustomAnimations((GeoAnimatable)defectCoreEntity, l, animationState);
                object = defectCoreEntity;
                if (!(object instanceof DefectSkillHost)) break block6;
                defectSkillHost = (DefectSkillHost)object;
                if (!defectCoreEntity.m_21224_()) break block7;
            }
            return;
        }
        object = defectSkillHost.ftDefectAmbientState().name();
        if (((String)object).equals("beam") || ((String)object).equals("exhausted")) {
            return;
        }
        String string = defectSkillHost.ftDefectSkillState().name();
        String[] stringArray = DefectSkillSync.decode(string);
        if (stringArray.length == 0) {
            return;
        }
        Document document = DefectSkillGeoModel.document();
        Phase phase = document.skills.get(stringArray[2]).get(stringArray[1]);
        float f = DefectSkillSync.progress(stringArray, defectSkillHost.ftDefectSkillState().elapsed(animationState.getPartialTick()));
        int n = DefectSkillGeoModel.interval(phase, f);
        float f2 = DefectSkillGeoModel.amount(phase, n, f);
        for (String string2 : document.bones) {
            GeoBone geoBone = this.getBone(string2).orElse(null);
            if (geoBone == null) continue;
            DefectSkillGeoModel.sampleInto(phase, string2, n, f2, this.scratchPose);
            Pose pose = this.scratchPose;
            BoneSnapshot boneSnapshot = geoBone.getInitialSnapshot();
            geoBone.setRotX(boneSnapshot.getRotX() - (float)Math.toRadians(pose.rotation[0]));
            geoBone.setRotY(boneSnapshot.getRotY() - (float)Math.toRadians(pose.rotation[1]));
            geoBone.setRotZ(boneSnapshot.getRotZ() + (float)Math.toRadians(pose.rotation[2]));
            geoBone.setPosX(boneSnapshot.getOffsetX() - pose.position[0]);
            geoBone.setPosY(boneSnapshot.getOffsetY() + pose.position[1]);
            geoBone.setPosZ(boneSnapshot.getOffsetZ() + pose.position[2]);
            geoBone.setScaleX(boneSnapshot.getScaleX() * pose.scale[0]);
            geoBone.setScaleY(boneSnapshot.getScaleY() * pose.scale[1]);
            geoBone.setScaleZ(boneSnapshot.getScaleZ() * pose.scale[2]);
        }
    }

    public static Pose sample(Phase phase, String string, float f) {
        Pose pose = new Pose();
        DefectSkillGeoModel.sampleInto(phase, string, f, pose);
        return pose;
    }

    public static void sampleInto(Phase phase, String string, float f, Pose pose) {
        int n = DefectSkillGeoModel.interval(phase, f);
        DefectSkillGeoModel.sampleInto(phase, string, n, DefectSkillGeoModel.amount(phase, n, f), pose);
    }

    private static int interval(Phase phase, float f) {
        for (int i = 1; i < phase.keyframes.length; ++i) {
            if (!(phase.keyframes[i].t >= f)) continue;
            return i;
        }
        return phase.keyframes.length - 1;
    }

    private static float amount(Phase phase, int n, float f) {
        Frame frame = phase.keyframes[n - 1];
        Frame frame2 = phase.keyframes[n];
        return Math.max(0.0f, Math.min(1.0f, (f - frame.t) / Math.max(1.0E-5f, frame2.t - frame.t)));
    }

    private static void sampleInto(Phase phase, String string, int n, float f, Pose pose) {
        Pose pose2 = phase.keyframes[n - 1].bones.get(string);
        Pose pose3 = phase.keyframes[n].bones.get(string);
        DefectSkillGeoModel.interpolate(pose2.rotation, pose3.rotation, f, pose.rotation);
        DefectSkillGeoModel.interpolate(pose2.position, pose3.position, f, pose.position);
        DefectSkillGeoModel.interpolate(pose2.scale, pose3.scale, f, pose.scale);
    }

    private static void interpolate(float[] fArray, float[] fArray2, float f, float[] fArray3) {
        for (int i = 0; i < 3; ++i) {
            fArray3[i] = fArray[i] + (fArray2[i] - fArray[i]) * f;
        }
    }

    public static final class Pose {
        public float[] rotation = new float[3];
        public float[] position = new float[3];
        public float[] scale = new float[]{1.0f, 1.0f, 1.0f};
    }

    public static final class Document {
        public String[] bones;
        public Map<String, Map<String, Phase>> skills;
    }

    public static final class Phase {
        public float authoredTicks;
        public Frame[] keyframes;
    }

    public static final class Frame {
        public float t;
        public Map<String, Pose> bones;
    }
}

