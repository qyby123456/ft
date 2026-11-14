/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.client.dorabasic;

import net.farmingtales.ftboss.client.dorabasic.DoraBasicPose;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.registries.ForgeRegistries;

public final class DoraBasicAnimation {
    private DoraBasicAnimation() {
    }

    public static boolean accepts(int n, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5) {
        return n == 2 && bl && !bl2 && !bl3 && !bl4 && !bl5;
    }

    public static boolean accepts(int n, boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6, boolean bl7) {
        return DoraBasicAnimation.accepts(n, bl, bl2, bl3, bl4, bl5) && !bl6 && !bl7;
    }

    public static void apply(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity, float f, float f2, float f3, float f4, float f5) {
        Object object;
        double d;
        boolean bl;
        if (DoraBasicAnimation.armMeleeAnimationDisabled()) {
            return;
        }
        if (easterBossEntity.getVariant() != 2 || !easterBossEntity.m_6084_() || easterBossEntity.isMini() || easterBossEntity.isCastingLike() || easterBossEntity.isDoraReloading() || easterBossEntity.m_21525_() || easterBossEntity.m_20159_()) {
            return;
        }
        CompoundTag compoundTag = easterBossEntity.getSkillCue();
        if (compoundTag.m_128471_("doraSniper") || !compoundTag.m_128441_("doraBasic")) {
            return;
        }
        ResourceLocation resourceLocation = ForgeRegistries.ITEMS.getKey((Object)easterBossEntity.m_21205_().m_41720_());
        ResourceLocation resourceLocation2 = ForgeRegistries.ITEMS.getKey((Object)easterBossEntity.m_21206_().m_41720_());
        boolean bl2 = resourceLocation != null && "ftboss:dora_nova_launcher".equals(resourceLocation.toString());
        boolean bl3 = bl = resourceLocation2 != null && "ftboss:dora_lance".equals(resourceLocation2.toString());
        if (!bl2 && !bl) {
            return;
        }
        CompoundTag compoundTag2 = compoundTag.m_128469_("doraBasic");
        double d2 = Math.max(0.0f, Math.min(1.0f, f3 - (float)easterBossEntity.f_19797_));
        long l = easterBossEntity.m_9236_().m_46467_();
        double d3 = compoundTag2.m_128441_("gunTick") ? (double)(l - compoundTag2.m_128454_("gunTick")) + d2 : -1.0;
        double d4 = compoundTag2.m_128441_("spearTick") ? (double)(l - compoundTag2.m_128454_("spearTick")) + d2 : -1.0;
        double d5 = bl2 ? DoraBasicPose.weight(true, d3) : 0.0;
        double d6 = d = bl ? DoraBasicPose.weight(false, d4) : 0.0;
        if (d5 == 0.0 && d == 0.0) {
            return;
        }
        boolean bl4 = easterBossEntity.m_5737_() == HumanoidArm.LEFT;
        int n = Math.floorMod(compoundTag2.m_128451_("gunSequence") - 1, 3);
        int n2 = 3 + Math.floorMod(compoundTag2.m_128451_("spearSequence") - 1, 3);
        ModelPart[] modelPartArray = new ModelPart[]{easterBossModel.f_102808_, easterBossModel.f_102810_, easterBossModel.f_102811_, easterBossModel.f_102812_};
        double[][] dArray = new double[6][3];
        int n3 = bl4 ? 3 : 2;
        int n4 = bl4 ? 2 : 3;
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 3; ++j) {
                object = modelPartArray[i];
                dArray[i][j] = Math.toDegrees(j == 0 ? (double)object.f_104203_ : (j == 1 ? (double)object.f_104204_ : (double)object.f_104205_));
            }
        }
        DoraBasicPose.Pose pose = DoraBasicPose.overlay(dArray, n, n2, bl2 ? d3 : -1.0, bl ? d4 : -1.0, bl4, f4, f5);
        double[][] dArray2 = pose.angles();
        object = pose.positions();
        for (int i = 0; i < modelPartArray.length; ++i) {
            if (i == n3 && !bl2 || i == n4 && !bl) continue;
            ModelPart modelPart = modelPartArray[i];
            modelPart.f_104200_ = (float)object[i][0];
            modelPart.f_104201_ = (float)object[i][1];
            modelPart.f_104202_ = (float)object[i][2];
            modelPart.f_104203_ = (float)Math.toRadians(dArray2[i][0]);
            modelPart.f_104204_ = (float)Math.toRadians(dArray2[i][1]);
            modelPart.f_104205_ = (float)Math.toRadians(dArray2[i][2]);
        }
        easterBossModel.f_102809_.m_104315_(easterBossModel.f_102808_);
        easterBossModel.f_103378_.m_104315_(easterBossModel.f_102810_);
        easterBossModel.f_103375_.m_104315_(easterBossModel.f_102811_);
        easterBossModel.f_103374_.m_104315_(easterBossModel.f_102812_);
    }

    public static final boolean armMeleeAnimationDisabled() {
        return true;
    }
}

