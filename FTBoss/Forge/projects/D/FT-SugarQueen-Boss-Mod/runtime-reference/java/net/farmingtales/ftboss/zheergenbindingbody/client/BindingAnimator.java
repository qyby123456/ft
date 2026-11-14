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
package net.farmingtales.ftboss.zheergenbindingbody.client;

import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.manorrun.RunAnimator;
import net.farmingtales.ftboss.zheergenbindingbody.BindingPhase;
import net.farmingtales.ftboss.zheergenbindingbody.BindingSignals;
import net.farmingtales.ftboss.zheergenbindingbody.client.BindingPose;
import net.farmingtales.ftboss.zheergennightfall.NightfallAccess;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class BindingAnimator {
    private BindingAnimator() {
    }

    public static boolean accepts(int n, boolean bl, boolean bl2, boolean bl3, String string, boolean bl4) {
        return n == 7 && !bl && !bl2 && bl3 && "ftboss:shadow_sword".equals(string) && bl4;
    }

    private static float partialTicks(float f, int n) {
        return Math.max(0.0f, Math.min(1.0f, f - (float)n));
    }

    public static void apply(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity, float f, float f2, float f3, float f4, float f5) {
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
        if (BindingSignals.nightfallActive(compoundTag, easterBossEntity.m_9236_().m_46467_(), BindingAnimator.partialTicks(f3, easterBossEntity.f_19797_)) || !compoundTag.m_128425_("zheergenBindingBody", 10)) {
            return;
        }
        CompoundTag compoundTag2 = compoundTag.m_128469_("zheergenBindingBody");
        if (!"shadow_brand".equals(compoundTag2.m_128461_("id")) || compoundTag2.m_128451_("schema") != 1) {
            return;
        }
        float f6 = Math.max(0.0f, Math.min(1.0f, f3 - (float)easterBossEntity.f_19797_));
        double d = (double)(easterBossEntity.m_9236_().m_46467_() - compoundTag2.m_128454_("start")) + (double)f6;
        BindingPhase bindingPhase = BindingPhase.sample(compoundTag2.m_128461_("phase"), d, compoundTag2.m_128451_("duration"), compoundTag2.m_128459_("frozen"));
        if (!bindingPhase.active()) {
            return;
        }
        boolean bl = bindingPhase.track().equals("release");
        double d2 = bindingPhase.progress();
        double d3 = bindingPhase.weight();
        RunAnimator.reserve(d3, easterBossEntity);
        BindingPose bindingPose = BindingPose.sample(bl, d2, d3, easterBossEntity.m_5737_() == HumanoidArm.LEFT, f, f2, Math.toRadians(f4), Math.toRadians(f5));
        ModelPart[] modelPartArray = new ModelPart[]{easterBossModel.f_102808_, easterBossModel.f_102810_, easterBossModel.f_102811_, easterBossModel.f_102812_, easterBossModel.f_102813_, easterBossModel.f_102814_};
        for (int i = 0; i < modelPartArray.length; ++i) {
            ModelPart modelPart = modelPartArray[i];
            modelPart.f_104200_ = (float)bindingPose.position[i][0];
            modelPart.f_104201_ = (float)bindingPose.position[i][1];
            modelPart.f_104202_ = (float)bindingPose.position[i][2];
            modelPart.f_104203_ = (float)bindingPose.rotation[i][0];
            modelPart.f_104204_ = (float)bindingPose.rotation[i][1];
            modelPart.f_104205_ = (float)bindingPose.rotation[i][2];
        }
        easterBossModel.f_102809_.m_104315_(easterBossModel.f_102808_);
        easterBossModel.f_103378_.m_104315_(easterBossModel.f_102810_);
        easterBossModel.f_103375_.m_104315_(easterBossModel.f_102811_);
        easterBossModel.f_103374_.m_104315_(easterBossModel.f_102812_);
        easterBossModel.f_103377_.m_104315_(easterBossModel.f_102813_);
        easterBossModel.f_103376_.m_104315_(easterBossModel.f_102814_);
    }
}

