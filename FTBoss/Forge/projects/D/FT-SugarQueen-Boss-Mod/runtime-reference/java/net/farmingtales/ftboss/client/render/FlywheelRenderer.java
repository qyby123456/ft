/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.ItemRenderer
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.client.xuanlannative.NativeScene;
import net.farmingtales.ftboss.entity.FlywheelProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class FlywheelRenderer
extends EntityRenderer<FlywheelProjectileEntity> {
    public static final float PROJECTILE_SCALE = 0.75f;
    private final ItemRenderer itemRenderer;

    public FlywheelRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.itemRenderer = ctx.m_174025_();
    }

    public void render(FlywheelProjectileEntity flywheelProjectileEntity, float f, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        NativeScene.flywheel(flywheelProjectileEntity, f, f2, poseStack, multiBufferSource, n);
        super.m_7392_((Entity)flywheelProjectileEntity, f, f2, poseStack, multiBufferSource, n);
    }

    public ResourceLocation getTextureLocation(FlywheelProjectileEntity entity) {
        return TextureAtlas.f_118259_;
    }
}

