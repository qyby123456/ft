package net.farmingtales.ftboss.turret.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.farmingtales.ftboss.turret.XuanlanTurretEntity;
import net.farmingtales.ftboss.turret.XuanlanTurrets;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(
    modid = "ftboss",
    bus = Bus.MOD,
    value = {Dist.CLIENT}
)
public final class XuanlanTurretRenderer extends EntityRenderer<XuanlanTurretEntity> {
    public XuanlanTurretRenderer(Context context) {
        super(context);
        this.shadowRadius = 0.6F;
    }

    @SubscribeEvent
    public static void register(RegisterRenderers event) {
        event.registerEntityRenderer(XuanlanTurrets.TYPE, XuanlanTurretRenderer::new);
    }

    public ResourceLocation getTextureLocation(XuanlanTurretEntity entity) {
        return XuanlanTurretModel.TEXTURE;
    }

    public void render(XuanlanTurretEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        XuanlanTurretModel model = XuanlanTurretModel.get();
        if (model != null) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
            float age = (float)entity.age() + partialTick;
            float opening = Math.min(1.0F, age / 20.0F);
            pose.scale(1.0F, 0.2F + opening * 0.8F, 1.0F);
            model.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(XuanlanTurretModel.TEXTURE)), age, entity.recoil(partialTick), false, light);
            model.render(pose, buffers.getBuffer(RenderType.entityTranslucent(XuanlanTurretModel.GLOW)), age, entity.recoil(partialTick), true, light);
            pose.popPose();
            super.render(entity, yaw, partialTick, pose, buffers, light);
        }
    }
}
