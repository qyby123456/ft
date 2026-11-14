/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.zheergenremainingbody.client;

import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.zheergennightfall.NightfallAccess;
import net.farmingtales.ftboss.zheergenremainingbody.SkillBodySignals;
import net.farmingtales.ftboss.zheergenremainingbody.client.CagePose;
import net.farmingtales.ftboss.zheergenremainingbody.client.CrossPose;
import net.farmingtales.ftboss.zheergenremainingbody.client.StrikePose;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class SkillBodyAnimator {
    private SkillBodyAnimator() {
    }

    private static double ease(double d) {
        double d2 = Math.max(0.0, Math.min(1.0, d));
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static double[] clock(String string, double d, int n, double d2) {
        if (d < 0.0 || n < 1 || n > 200 || !Double.isFinite(d)) {
            return new double[]{0.0, 0.0, 0.0};
        }
        double d3 = Math.max(0.0, Math.min(1.0, d / (double)n));
        if ("charge".equals(string)) {
            return new double[]{d3, d <= (double)(n + 1) ? 1.0 : 0.0, 0.0};
        }
        if ("release".equals(string)) {
            return new double[]{d3, d < (double)n ? 1.0 - SkillBodyAnimator.ease((d3 - 0.72) / 0.28) : 0.0, 1.0};
        }
        if ("cancel".equals(string) && Double.isFinite(d2)) {
            double d4 = 1.0 - d3 * d3 * (3.0 - 2.0 * d3);
            return new double[]{Math.max(0.0, Math.min(1.0, d2)), d < (double)n ? d4 : 0.0, 0.0};
        }
        return new double[]{0.0, 0.0, 0.0};
    }

    public static void apply(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity, float f, float f2, float f3, float f4, float f5) {
        double[][] dArray;
        double[][] dArray2;
        ModelPart[] modelPartArray;
        boolean bl;
        if (easterBossEntity.getVariant() != 7 || easterBossEntity.isMini() || easterBossEntity.isGiant() || !easterBossEntity.m_6084_() || !(easterBossEntity instanceof NightfallAccess)) {
            return;
        }
        NightfallAccess nightfallAccess = easterBossEntity;
        ItemStack itemStack = easterBossEntity.m_21205_();
        if (itemStack.m_41619_()) {
            return;
        }
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_());
        if (resourceLocation == null || !"ftboss:shadow_sword".equals(resourceLocation.toString())) {
            return;
        }
        CompoundTag compoundTag = nightfallAccess.ftboss$nightfallRead();
        if (!compoundTag.m_128425_("ftboss_zheergen_remaining_body", 10)) {
            return;
        }
        CompoundTag compoundTag2 = compoundTag.m_128469_("ftboss_zheergen_remaining_body");
        String string = compoundTag2.m_128461_("id");
        if (compoundTag2.m_128451_("schema") != 1 || !SkillBodySignals.idValid(string)) {
            return;
        }
        double d = (double)(easterBossEntity.m_9236_().m_46467_() - compoundTag2.m_128454_("start")) + (double)Math.max(0.0f, Math.min(1.0f, f3 - (float)easterBossEntity.f_19797_));
        String string2 = compoundTag2.m_128461_("phase");
        double[] dArray3 = SkillBodyAnimator.clock(string2, d, compoundTag2.m_128451_("duration"), compoundTag2.m_128459_("frozen"));
        if (dArray3[1] <= 0.0) {
            return;
        }
        boolean bl2 = dArray3[2] == 1.0 || "cancel".equals(string2) && "release".equals(compoundTag2.m_128461_("track"));
        boolean bl3 = bl = easterBossEntity.m_5737_() == HumanoidArm.LEFT;
        if (bl2 && "cancel".equals(string2)) {
            dArray3[1] = dArray3[1] * (1.0 - SkillBodyAnimator.ease((dArray3[0] - 0.72) / 0.28));
        }
        if ("shadow_cross".equals(string)) {
            modelPartArray = CrossPose.sample(bl2, dArray3[0], dArray3[1], bl, f, f2, Math.toRadians(f4), Math.toRadians(f5));
            dArray2 = modelPartArray.position;
            dArray = modelPartArray.rotation;
        } else if ("shadow_cage".equals(string)) {
            modelPartArray = CagePose.sample(bl2, dArray3[0], dArray3[1], bl, f, f2, Math.toRadians(f4), Math.toRadians(f5));
            dArray2 = modelPartArray.position;
            dArray = modelPartArray.rotation;
        } else {
            modelPartArray = StrikePose.sample(bl2, dArray3[0], dArray3[1], bl, f, f2, Math.toRadians(f4), Math.toRadians(f5));
            dArray2 = modelPartArray.position;
            dArray = modelPartArray.rotation;
        }
        modelPartArray = new ModelPart[]{easterBossModel.f_102808_, easterBossModel.f_102810_, easterBossModel.f_102811_, easterBossModel.f_102812_, easterBossModel.f_102813_, easterBossModel.f_102814_};
        for (int i = 0; i < 6; ++i) {
            ModelPart modelPart = modelPartArray[i];
            modelPart.f_104200_ = (float)dArray2[i][0];
            modelPart.f_104201_ = (float)dArray2[i][1];
            modelPart.f_104202_ = (float)dArray2[i][2];
            modelPart.f_104203_ = (float)dArray[i][0];
            modelPart.f_104204_ = (float)dArray[i][1];
            modelPart.f_104205_ = (float)dArray[i][2];
        }
        easterBossModel.f_102809_.m_104315_(easterBossModel.f_102808_);
        easterBossModel.f_103378_.m_104315_(easterBossModel.f_102810_);
        easterBossModel.f_103375_.m_104315_(easterBossModel.f_102811_);
        easterBossModel.f_103374_.m_104315_(easterBossModel.f_102812_);
        easterBossModel.f_103377_.m_104315_(easterBossModel.f_102813_);
        easterBossModel.f_103376_.m_104315_(easterBossModel.f_102814_);
    }
}

