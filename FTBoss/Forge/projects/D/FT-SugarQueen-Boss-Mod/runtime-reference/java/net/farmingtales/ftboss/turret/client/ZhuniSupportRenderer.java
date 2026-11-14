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
public final class ZhuniSupportRenderer
extends EntityRenderer<Entity> {
    private final String modelId;

    public ZhuniSupportRenderer(EntityRendererProvider.Context context, String id) {
        super(context);
        this.modelId = id;
        this.f_114477_ = id.equals("zhuni") ? 0.3f : 0.15f;
    }

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(XuanlanTurrets.BIRD, context -> new ZhuniSupportRenderer(context, "zhuni"));
        event.registerEntityRenderer(XuanlanTurrets.FLYWHEEL, context -> new ZhuniSupportRenderer(context, "turret_flywheel"));
    }

    public ResourceLocation m_5478_(Entity entity) {
        return new ResourceLocation("ftboss", "textures/entity/" + this.modelId + ".png");
    }

    public void m_7392_(Entity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        XuanlanTurretModel model = XuanlanTurretModel.get(this.modelId);
        if (model == null) {
            return;
        }
        pose.m_85836_();
        pose.m_252781_(Axis.f_252436_.m_252977_(180.0f - yaw));
        float age = (float)entity.f_19797_ + partialTick;
        model.render(pose, buffers.m_6299_(RenderType.m_110458_((ResourceLocation)model.texture)), age, 0.0f, false, light);
        model.render(pose, buffers.m_6299_(RenderType.m_110473_((ResourceLocation)model.emission)), age, 0.0f, true, light);
        pose.m_85849_();
        super.m_7392_(entity, yaw, partialTick, pose, buffers, light);
    }
}

