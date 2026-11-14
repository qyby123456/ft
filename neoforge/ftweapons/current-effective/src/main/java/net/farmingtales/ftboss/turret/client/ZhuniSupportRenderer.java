package net.farmingtales.ftboss.turret.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.farmingtales.ftboss.turret.XuanlanTurrets;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
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
public final class ZhuniSupportRenderer extends EntityRenderer<Entity> {
    private final String modelId;

    public ZhuniSupportRenderer(Context context, String id) {
        super(context);
        this.modelId = id;
        this.shadowRadius = id.equals("zhuni") ? 0.3F : 0.15F;
    }

    @SubscribeEvent
    public static void register(RegisterRenderers event) {
        event.registerEntityRenderer(XuanlanTurrets.BIRD, context -> new ZhuniSupportRenderer(context, "zhuni"));
        event.registerEntityRenderer(XuanlanTurrets.FLYWHEEL, context -> new ZhuniSupportRenderer(context, "turret_flywheel"));
    }

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return ResourceLocation.fromNamespaceAndPath("ftboss", "textures/entity/" + this.modelId + ".png");
    }

    @Override
    public void render(Entity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        XuanlanTurretModel model = XuanlanTurretModel.get(this.modelId);
        if (model != null) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
            float age = (float)entity.tickCount + partialTick;
            model.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(model.texture)), age, 0.0F, false, light);
            model.render(pose, buffers.getBuffer(RenderType.entityTranslucent(model.emission)), age, 0.0F, true, light);
            pose.popPose();
            super.render(entity, yaw, partialTick, pose, buffers, light);
        }
    }
}
