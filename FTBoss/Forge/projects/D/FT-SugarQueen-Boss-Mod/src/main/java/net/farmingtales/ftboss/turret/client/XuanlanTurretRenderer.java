package net.farmingtales.ftboss.turret.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.farmingtales.ftboss.turret.XuanlanTurretEntity;
import net.farmingtales.ftboss.turret.XuanlanTurrets;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 月轮炮台渲染：与庄园防御同源体素模型、像素贴图及独立全亮发光层。 */
@Mod.EventBusSubscriber(modid = "ftboss", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class XuanlanTurretRenderer extends EntityRenderer<XuanlanTurretEntity> {
    public XuanlanTurretRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.6F;
    }

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(XuanlanTurrets.TYPE, XuanlanTurretRenderer::new);
    }

    @Override
    public ResourceLocation getTextureLocation(XuanlanTurretEntity entity) { return XuanlanTurretModel.TEXTURE; }

    @Override
    public void render(XuanlanTurretEntity entity, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        XuanlanTurretModel model = XuanlanTurretModel.get();
        if (model == null) return;
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        float age = entity.age() + partialTick;
        float opening = Math.min(1.0F, age / 20.0F);
        pose.scale(1, 0.2F + opening * 0.8F, 1);
        model.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(XuanlanTurretModel.TEXTURE)),
                age, entity.recoil(partialTick), false, light);
        model.render(pose, buffers.getBuffer(RenderType.entityTranslucent(XuanlanTurretModel.GLOW)),
                age, entity.recoil(partialTick), true, light);
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }
}
