/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.ModelPart
 */
package net.farmingtales.ftboss.client.saya;

import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.client.model.SayaPlayerModel;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.farmingtales.ftboss.saya.SayaCastMotion;
import net.minecraft.client.model.geom.ModelPart;

public final class SayaCastingPose {
    private static final Map<TeaTreeBossEntity, Memory> CACHE = new WeakHashMap<TeaTreeBossEntity, Memory>();

    private SayaCastingPose() {
    }

    public static boolean apply(SayaPlayerModel sayaPlayerModel, TeaTreeBossEntity teaTreeBossEntity2, float f) {
        ModelPart[] modelPartArray;
        if (!teaTreeBossEntity2.m_6084_() || !teaTreeBossEntity2.signatureCastAction().active()) {
            return false;
        }
        String string = teaTreeBossEntity2.signatureCastAction().name();
        Memory memory = CACHE.computeIfAbsent(teaTreeBossEntity2, teaTreeBossEntity -> new Memory());
        if (!string.equals(memory.action)) {
            String[] stringArray = SayaCastMotion.decode(string);
            if (stringArray == null) {
                return false;
            }
            memory.action = string;
            memory.index = SayaCastMotion.index(stringArray[0]);
            memory.castTicks = Integer.parseInt(stringArray[1]);
        }
        int n = memory.castTicks;
        double d = teaTreeBossEntity2.signatureCastAction().elapsed(f - (float)teaTreeBossEntity2.f_19797_);
        double[] dArray = memory.pose;
        SayaCastMotion.sample(memory.index, d, n, dArray);
        float f2 = (float)SayaCastMotion.blend(d, n);
        if (memory.model == sayaPlayerModel) {
            modelPartArray = memory.bones;
        } else {
            ModelPart[] modelPartArray2 = new ModelPart[6];
            modelPartArray2[0] = sayaPlayerModel.f_102808_;
            modelPartArray2[1] = sayaPlayerModel.f_102810_;
            modelPartArray2[2] = sayaPlayerModel.f_102811_;
            modelPartArray2[3] = sayaPlayerModel.f_102812_;
            modelPartArray2[4] = sayaPlayerModel.f_102813_;
            modelPartArray = modelPartArray2;
            modelPartArray2[5] = sayaPlayerModel.f_102814_;
        }
        ModelPart[] modelPartArray3 = modelPartArray;
        memory.model = sayaPlayerModel;
        memory.bones = modelPartArray3;
        for (int i = 0; i < modelPartArray3.length; ++i) {
            ModelPart modelPart = modelPartArray3[i];
            if (i == 0) {
                modelPart.f_104203_ += (float)dArray[0];
                continue;
            }
            modelPart.f_104203_ = modelPart.f_104203_ * (1.0f - f2) + (float)dArray[i * 3] * f2;
            modelPart.f_104204_ = modelPart.f_104204_ * (1.0f - f2) + (float)dArray[i * 3 + 1] * f2;
            modelPart.f_104205_ = modelPart.f_104205_ * (1.0f - f2) + (float)dArray[i * 3 + 2] * f2;
        }
        sayaPlayerModel.f_102809_.m_104315_(sayaPlayerModel.f_102808_);
        sayaPlayerModel.f_103378_.m_104315_(sayaPlayerModel.f_102810_);
        sayaPlayerModel.f_103374_.m_104315_(sayaPlayerModel.f_102812_);
        sayaPlayerModel.f_103375_.m_104315_(sayaPlayerModel.f_102811_);
        sayaPlayerModel.f_103376_.m_104315_(sayaPlayerModel.f_102814_);
        sayaPlayerModel.f_103377_.m_104315_(sayaPlayerModel.f_102813_);
        return true;
    }

    private static final class Memory {
        private String action = "";
        private int index;
        private int castTicks;
        private final double[] pose = new double[18];
        private SayaPlayerModel model;
        private ModelPart[] bones;

        private Memory() {
        }
    }
}

