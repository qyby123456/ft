package net.farmingtales.ftboss.turret.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.farmingtales.ftboss.turret.XuanlanTurrets;
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

/** 竹鸟与一次性飞轮使用同一套庄园体素贴图、骨骼关键帧和发光层渲染。 */
@Mod.EventBusSubscriber(modid = "ftboss", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ZhuniSupportRenderer extends EntityRenderer<Entity> {
    private final String modelId;

    public ZhuniSupportRenderer(EntityRendererProvider.Context context, String id) {
        super(context);
        modelId = id;
        shadowRadius = id.equals("zhuni") ? 0.3F : 0.15F;
    }

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(XuanlanTurrets.BIRD, context -> new ZhuniSupportRenderer(context, "zhuni"));
        event.registerEntityRenderer(XuanlanTurrets.FLYWHEEL, context -> new ZhuniSupportRenderer(context, "turret_flywheel"));
    }

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return new ResourceLocation("ftboss", "textures/entity/" + modelId + ".png");
    }

    @Override
    public void render(Entity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        XuanlanTurretModel model = XuanlanTurretModel.get(modelId);
        if (model == null) return;
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        float age = entity.tickCount + partialTick;
        model.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(model.texture)), age, 0, false, light);
        model.render(pose, buffers.getBuffer(RenderType.entityTranslucent(model.emission)), age, 0, true, light);
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }
}
