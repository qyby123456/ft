/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.ItemInHandRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.RenderLayerParent
 *  net.minecraft.client.renderer.entity.layers.ItemInHandLayer
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemDisplayContext
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.client.qizaiaction.QizaiActionClient;
import net.farmingtales.ftboss.client.render.GongdiHumanoidRenderer;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;

public final class QizaiHeldItemLayer
extends ItemInHandLayer<EasterBossEntity, EasterBossModel> {
    public QizaiHeldItemLayer(RenderLayerParent<EasterBossEntity, EasterBossModel> parent, ItemInHandRenderer renderer) {
        super(parent, renderer);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * WARNING - void declaration
     */
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, EasterBossEntity easterBossEntity, float f, float f2, float f3, float f4, float f5, float f6) {
        void age;
        void light;
        void buffers;
        void pose;
        void boss;
        if (GongdiHumanoidRenderer.renderWeapon(easterBossEntity, poseStack, multiBufferSource, n)) {
            return;
        }
        if (QizaiActionClient.attached(easterBossEntity)) {
            super.m_6494_(poseStack, multiBufferSource, n, (LivingEntity)easterBossEntity, f, f2, f3, f4, f5, f6);
            return;
        }
        CompoundTag cue = boss.getSkillCue();
        if (boss.getVariant() != 11 || cue.m_128451_("qizai") != 3 || cue.m_128471_("released")) {
            void pitch;
            void yaw;
            void partial;
            void limbAmount;
            void limbSwing;
            super.m_6494_((PoseStack)pose, (MultiBufferSource)buffers, (int)light, (LivingEntity)boss, (float)limbSwing, (float)limbAmount, (float)partial, (float)age, (float)yaw, (float)pitch);
            return;
        }
        pose.m_85836_();
        try {
            pose.m_85837_(0.0, 0.6 + Math.sin((double)age * 0.1) * 0.035, -0.75);
            pose.m_252781_(Axis.f_252403_.m_252977_(180.0f));
            pose.m_252781_(Axis.f_252436_.m_252977_((float)Math.sin((double)age * 0.06) * 12.0f));
            pose.m_85841_(0.8f, 0.8f, 0.8f);
            Minecraft.m_91087_().m_91291_().m_269491_((LivingEntity)boss, boss.m_21205_(), ItemDisplayContext.NONE, false, (PoseStack)pose, (MultiBufferSource)buffers, boss.m_9236_(), (int)light, OverlayTexture.f_118083_, boss.m_19879_());
        }
        finally {
            pose.m_85849_();
        }
    }
}

