/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.HumanoidModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.entity.ShadowPhantomEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public class ShadowPhantomModel
extends HumanoidModel<ShadowPhantomEntity> {
    public ShadowPhantomModel(ModelPart root) {
        super(root);
    }

    public void setupAnim(ShadowPhantomEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.m_6973_((LivingEntity)entity, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        this.f_102608_ = 0.0f;
        this.f_102817_ = false;
        this.f_102609_ = false;
        this.f_102610_ = false;
        this.f_102818_ = 0.0f;
    }
}

