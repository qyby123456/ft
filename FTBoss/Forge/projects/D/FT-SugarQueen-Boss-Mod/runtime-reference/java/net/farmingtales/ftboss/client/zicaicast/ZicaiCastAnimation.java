/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.client.zicaicast;

import net.farmingtales.ftboss.animation.zicaicast.ZicaiCastAccess;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.client.zicaicast.ZicaiCastPose;
import net.farmingtales.ftboss.client.zicaicast.ZicaiCastTiming;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class ZicaiCastAnimation {
    private ZicaiCastAnimation() {
    }

    public static void apply(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity, float f, float f2, float f3, float f4, float f5) {
        CompoundTag compoundTag;
        if (easterBossEntity.getVariant() != 8 || !easterBossEntity.m_6084_() || !(easterBossEntity instanceof ZicaiCastAccess)) {
            return;
        }
        ZicaiCastAccess zicaiCastAccess = easterBossEntity;
        ItemStack itemStack = easterBossEntity.m_21205_();
        if (!(itemStack.m_41619_() || (compoundTag = ForgeRegistries.ITEMS.getKey((Object)itemStack.m_41720_())) != null && "ftboss:puffer_bat".equals(compoundTag.toString()))) {
            return;
        }
        compoundTag = zicaiCastAccess.zicaiCastState();
        if (compoundTag.m_128456_()) {
            return;
        }
        int n = compoundTag.m_128451_("track");
        int n2 = compoundTag.m_128451_("duration");
        int n3 = compoundTag.m_128451_("stage");
        long l = compoundTag.m_128454_("start");
        long l2 = compoundTag.m_128454_("stop");
        long l3 = easterBossEntity.m_9236_().m_46467_();
        double d = Math.max(0.0f, Math.min(1.0f, f3 - (float)easterBossEntity.f_19797_));
        if (n3 != 0 && l2 < l) {
            return;
        }
        long l4 = l3 - (long)Math.round(easterBossEntity.getCombatAnimationAge(0.0f));
        if (easterBossEntity.getCombatAnimationWeight((float)d) > 0.0f && l4 > l && !easterBossEntity.isCastingLike()) {
            return;
        }
        ZicaiCastTiming.Sample sample = ZicaiCastTiming.sample(n, (double)(n3 == 0 ? l3 - l : l3 - l2) + d, n2, n3, l2 - l);
        if (sample.weight() <= 0.0) {
            return;
        }
        ZicaiCastPose.Pose pose = ZicaiCastPose.sample(n, sample.progress(), 1.0, easterBossEntity.m_5737_() == HumanoidArm.LEFT, f, f2, (double)f4 * Math.PI / 180.0, (double)f5 * Math.PI / 180.0);
        ModelPart[] modelPartArray = new ModelPart[]{easterBossModel.f_102808_, easterBossModel.f_102810_, easterBossModel.f_102811_, easterBossModel.f_102812_, easterBossModel.f_102813_, easterBossModel.f_102814_};
        ZicaiCastPose.Pose pose2 = new ZicaiCastPose.Pose();
        for (int i = 0; i < modelPartArray.length; ++i) {
            ModelPart modelPart = modelPartArray[i];
            pose2.position[i][0] = modelPart.f_104200_;
            pose2.position[i][1] = modelPart.f_104201_;
            pose2.position[i][2] = modelPart.f_104202_;
            pose2.rotation[i][0] = modelPart.f_104203_;
            pose2.rotation[i][1] = modelPart.f_104204_;
            pose2.rotation[i][2] = modelPart.f_104205_;
        }
        ZicaiCastPose.Pose pose3 = ZicaiCastPose.blend(pose, pose2, sample.weight(), f2);
        for (int i = 0; i < modelPartArray.length; ++i) {
            ModelPart modelPart = modelPartArray[i];
            modelPart.f_104200_ = (float)pose3.position[i][0];
            modelPart.f_104201_ = (float)pose3.position[i][1];
            modelPart.f_104202_ = (float)pose3.position[i][2];
            modelPart.f_104203_ = (float)pose3.rotation[i][0];
            modelPart.f_104204_ = (float)pose3.rotation[i][1];
            modelPart.f_104205_ = (float)pose3.rotation[i][2];
        }
        easterBossModel.f_102809_.m_104315_(easterBossModel.f_102808_);
        easterBossModel.f_103378_.m_104315_(easterBossModel.f_102810_);
        easterBossModel.f_103375_.m_104315_(easterBossModel.f_102811_);
        easterBossModel.f_103374_.m_104315_(easterBossModel.f_102812_);
        easterBossModel.f_103377_.m_104315_(easterBossModel.f_102813_);
        easterBossModel.f_103376_.m_104315_(easterBossModel.f_102814_);
    }
}

