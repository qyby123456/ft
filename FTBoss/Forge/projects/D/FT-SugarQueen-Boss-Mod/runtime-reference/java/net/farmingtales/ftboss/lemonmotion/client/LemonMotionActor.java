/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.world.entity.HumanoidArm
 */
package net.farmingtales.ftboss.lemonmotion.client;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.lemonmotion.LemonMotionAccess;
import net.farmingtales.ftboss.lemonmotion.client.LemonMotionCurves;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;

public final class LemonMotionActor {
    private static final double RAD = Math.PI / 180;
    private static final ThreadLocal<Base> BASE = new ThreadLocal();

    private LemonMotionActor() {
    }

    private static ModelPart[] parts(PlayerModel<?> playerModel) {
        return new ModelPart[]{playerModel.f_102808_, playerModel.f_102810_, playerModel.f_102811_, playerModel.f_102812_, playerModel.f_102813_, playerModel.f_102814_};
    }

    public static void capture(PlayerModel<?> playerModel, EasterBossEntity easterBossEntity) {
        BASE.remove();
        if (easterBossEntity.getVariant() != 4 || easterBossEntity.isMini()) {
            return;
        }
        ModelPart[] modelPartArray = LemonMotionActor.parts(playerModel);
        float[][] fArrayArray = new float[6][];
        for (int i = 0; i < 6; ++i) {
            ModelPart modelPart = modelPartArray[i];
            fArrayArray[i] = new float[]{modelPart.f_104200_, modelPart.f_104201_, modelPart.f_104202_, modelPart.f_104203_, modelPart.f_104204_, modelPart.f_104205_};
        }
        BASE.set(new Base(playerModel, easterBossEntity, fArrayArray));
    }

    public static boolean animate(PlayerModel<?> playerModel, EasterBossEntity easterBossEntity, float f, float f2, float f3, float f4, float f5, ModelPart modelPart, ModelPart modelPart2, ModelPart modelPart3) {
        Object object;
        Object object2;
        Object object3;
        Base base = BASE.get();
        BASE.remove();
        if (base == null || base.model() != playerModel || base.owner() != easterBossEntity) {
            return false;
        }
        if (easterBossEntity.getVariant() != 4 || easterBossEntity.isMini()) {
            return false;
        }
        if (easterBossEntity.isCastingLike() && (!(easterBossEntity instanceof LemonMotionAccess) || LemonMotionCurves.clip(LemonMotionActor.ftboss$basicBodyClip((object3 = (LemonMotionAccess)easterBossEntity).lemonMotionData().m_128461_("lemon_motion_v1"))) == null)) {
            return false;
        }
        object3 = LemonMotionActor.parts(playerModel);
        for (int i = 0; i < 6; ++i) {
            object2 = object3[i];
            object = base.poses()[i];
            ((ModelPart)object2).f_104200_ = object[0];
            ((ModelPart)object2).f_104201_ = object[1];
            ((ModelPart)object2).f_104202_ = object[2];
            ((ModelPart)object2).f_104203_ = object[3];
            ((ModelPart)object2).f_104204_ = object[4];
            ((ModelPart)object2).f_104205_ = object[5];
        }
        modelPart.m_233569_();
        modelPart2.m_233569_();
        modelPart3.m_233569_();
        if (easterBossEntity.m_6084_() && easterBossEntity instanceof LemonMotionAccess) {
            LemonMotionAccess lemonMotionAccess = easterBossEntity;
            object2 = lemonMotionAccess.lemonMotionData();
            object = LemonMotionActor.ftboss$basicBodyClip(object2.m_128461_("lemon_motion_v1"));
            int n = object2.m_128451_("duration");
            double d = LemonMotionCurves.clamp(f3 - (float)easterBossEntity.f_19797_, 0.0, 1.0);
            double d2 = easterBossEntity.getCombatAnimationAge((float)d);
            if (object2.m_128451_("lemon_motion_schema") == 1 && LemonMotionCurves.clip((String)object) != null && n > 0 && n <= 2400 && d2 >= 0.0 && d2 < (double)n) {
                int n2 = easterBossEntity.m_5737_() == HumanoidArm.RIGHT ? 1 : -1;
                double d3 = LemonMotionCurves.weight(d2, n);
                double[][] dArray = LemonMotionCurves.pose((String)object, d2, n, n2, f, f2, f4, f5);
                double[][] dArray2 = LemonMotionCurves.pose((String)object, 0.0, n, n2, f, f2, f4, f5);
                Object object4 = object3;
                for (int n3 : new int[]{0, 1, 2, 3}) {
                    Object object5 = object4[n3];
                    ((ModelPart)object5).f_104200_ += (float)(dArray[n3][0] - LemonMotionCurves.PIVOTS[n3][0]);
                    ((ModelPart)object5).f_104201_ += (float)(dArray[n3][1] - LemonMotionCurves.PIVOTS[n3][1]);
                    ((ModelPart)object5).f_104202_ += (float)(dArray[n3][2] - LemonMotionCurves.PIVOTS[n3][2]);
                    double[] dArray3 = new double[]{(double)((ModelPart)object5).f_104203_ / (Math.PI / 180), (double)((ModelPart)object5).f_104204_ / (Math.PI / 180), (double)((ModelPart)object5).f_104205_ / (Math.PI / 180)};
                    ((ModelPart)object5).f_104203_ = (float)((dArray3[0] * (1.0 - d3) + dArray[n3][3] - dArray2[n3][3] * (1.0 - d3)) * (Math.PI / 180));
                    ((ModelPart)object5).f_104204_ = (float)((dArray3[1] * (1.0 - d3) + dArray[n3][4] - dArray2[n3][4] * (1.0 - d3)) * (Math.PI / 180));
                    ((ModelPart)object5).f_104205_ = (float)((dArray3[2] * (1.0 - d3) + dArray[n3][5] - dArray2[n3][5] * (1.0 - d3)) * (Math.PI / 180));
                }
                double d4 = Math.sin(Math.PI * LemonMotionCurves.clamp(d2 / (double)n - 0.055, 0.0, 1.0)) * d3;
                modelPart2.f_104203_ += (float)(d4 * 0.07);
                modelPart.f_104205_ += (float)(d4 * 0.018 * (double)n2);
                modelPart3.f_104203_ += (float)(d4 * 0.03);
            }
        }
        playerModel.f_102809_.m_104315_(playerModel.f_102808_);
        playerModel.f_103378_.m_104315_(playerModel.f_102810_);
        playerModel.f_103374_.m_104315_(playerModel.f_102812_);
        playerModel.f_103375_.m_104315_(playerModel.f_102811_);
        playerModel.f_103376_.m_104315_(playerModel.f_102814_);
        playerModel.f_103377_.m_104315_(playerModel.f_102813_);
        return true;
    }

    private static String ftboss$basicBodyClip(String string) {
        if ("strike".equals(string)) {
            return "";
        }
        return string;
    }

    private record Base(PlayerModel<?> model, EasterBossEntity owner, float[][] poses) {
    }
}

