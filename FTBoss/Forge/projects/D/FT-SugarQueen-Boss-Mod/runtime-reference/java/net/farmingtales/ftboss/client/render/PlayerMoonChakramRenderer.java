/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.farmingtales.ftboss.client.render.XuanlanGlow;
import net.farmingtales.ftboss.client.render.XuanlanWeaponRenderer;
import net.farmingtales.ftboss.client.render.XuanlanWeaponVisual;
import net.farmingtales.ftboss.entity.PlayerMoonChakramEntity;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.util.XuanlanMotionRules;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.phys.Vec3;

public final class PlayerMoonChakramRenderer
extends EntityRenderer<PlayerMoonChakramEntity> {
    private final ItemStack model = new ItemStack((ItemLike)FTBossItems.WENXUANLAN_FLYWHEEL.get());

    public PlayerMoonChakramRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(PlayerMoonChakramEntity entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.m_85836_();
        pose.m_252781_(Axis.f_252436_.m_252977_(-yaw));
        pose.m_252781_(Axis.f_252403_.m_252977_(((float)entity.f_19797_ + partial) * 40.0f));
        pose.m_85836_();
        pose.m_85841_(0.5f, 0.5f, 0.5f);
        pose.m_85837_(-0.5, -0.5, -0.5);
        XuanlanWeaponRenderer.renderRig(this.model, true, new XuanlanMotionRules.Pose(0.0f, 0.0f, entity.isReturning() ? 0.35f : 0.0f, 0.0f, (float)entity.f_19797_ + partial), pose, buffers, 0xF000F0, OverlayTexture.f_118083_);
        pose.m_85849_();
        XuanlanWeaponVisual.ring(pose, buffers.m_6299_(XuanlanGlow.TYPE), Vec3.f_82478_, new Vec3(1.0, 0.0, 0.0), new Vec3(0.0, 1.0, 0.0), 0.29, 0.016, entity.isReturning() ? 6815692 : 14413567, 0.65f, 0.0, true);
        pose.m_85849_();
        super.m_7392_((Entity)entity, yaw, partial, pose, buffers, light);
    }

    public ResourceLocation getTextureLocation(PlayerMoonChakramEntity entity) {
        return TextureAtlas.f_118259_;
    }
}

