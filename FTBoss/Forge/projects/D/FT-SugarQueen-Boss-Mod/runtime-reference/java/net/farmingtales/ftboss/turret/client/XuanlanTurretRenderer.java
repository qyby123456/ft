/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.EntityRenderersEvent$RegisterRenderers
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.turret.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.farmingtales.ftboss.turret.XuanlanTurretEntity;
import net.farmingtales.ftboss.turret.XuanlanTurrets;
import net.farmingtales.ftboss.turret.client.XuanlanTurretModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public final class XuanlanTurretRenderer
extends EntityRenderer<XuanlanTurretEntity> {
    public XuanlanTurretRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.f_114477_ = 0.6f;
    }

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(XuanlanTurrets.TYPE, XuanlanTurretRenderer::new);
    }

    public ResourceLocation getTextureLocation(XuanlanTurretEntity entity) {
        return XuanlanTurretModel.TEXTURE;
    }

    public void render(XuanlanTurretEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        XuanlanTurretModel model = XuanlanTurretModel.get();
        if (model == null) {
            return;
        }
        pose.m_85836_();
        pose.m_252781_(Axis.f_252436_.m_252977_(180.0f - yaw));
        float age = (float)entity.age() + partialTick;
        float opening = Math.min(1.0f, age / 20.0f);
        pose.m_85841_(1.0f, 0.2f + opening * 0.8f, 1.0f);
        model.render(pose, buffers.m_6299_(RenderType.m_110458_((ResourceLocation)XuanlanTurretModel.TEXTURE)), age, entity.recoil(partialTick), false, light);
        model.render(pose, buffers.m_6299_(RenderType.m_110473_((ResourceLocation)XuanlanTurretModel.GLOW)), age, entity.recoil(partialTick), true, light);
        pose.m_85849_();
        super.m_7392_((Entity)entity, yaw, partialTick, pose, buffers, light);
    }
}

