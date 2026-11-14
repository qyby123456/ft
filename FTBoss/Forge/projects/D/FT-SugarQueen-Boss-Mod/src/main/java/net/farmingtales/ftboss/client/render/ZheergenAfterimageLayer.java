package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "ftboss", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ZheergenAfterimageLayer extends RenderLayer<EasterBossEntity, EasterBossModel> {
    public ZheergenAfterimageLayer(EasterBossRenderer parent) { super(parent); }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        EasterBossRenderer renderer = event.getRenderer(FTEntities.EASTER_BOSS.get());
        if (renderer != null) renderer.addLayer(new ZheergenAfterimageLayer(renderer));
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, EasterBossEntity boss,
            float limbSwing, float limbAmount, float partialTick, float age, float headYaw, float headPitch) {
        ZheergenAfterimageVisual.capture(boss, getParentModel(), pose);
    }
}
