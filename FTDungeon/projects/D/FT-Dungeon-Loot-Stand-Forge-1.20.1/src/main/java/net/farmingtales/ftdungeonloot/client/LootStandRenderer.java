package net.farmingtales.ftdungeonloot.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.farmingtales.ftdungeonloot.LootStandBlock;
import net.farmingtales.ftdungeonloot.LootStandEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;

public final class LootStandRenderer implements BlockEntityRenderer<LootStandEntity> {
    public LootStandRenderer(BlockEntityRendererProvider.Context context) {}
    @Override
    public void render(LootStandEntity stand, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var reward = stand.reward();
        pose.pushPose();
        pose.translate(.5, 0, .5);
        pose.mulPose(Axis.YP.rotationDegrees(-stand.getBlockState().getValue(LootStandBlock.FACING).toYRot() + 180));
        if (!stand.sold() && !reward.isEmpty()) {
            pose.pushPose();
            pose.translate(0, 1.4, .155);
            pose.scale(.55F, .55F, .55F);
            Minecraft.getInstance().getItemRenderer().renderStatic(reward, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY,
                    pose, buffers, Minecraft.getInstance().level, stand.getBlockPos().hashCode());
            pose.popPose();
        }
        Component label = stand.sold() ? Component.translatable("label.ftdungeonloot.sold_out") :
                reward.isEmpty() ? Component.translatable("label.ftdungeonloot.empty") : Component.translatable("label.ftdungeonloot.price", stand.price());
        Font font = Minecraft.getInstance().font;
        pose.translate(.039, .59, -.514);
        pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.scale(.005F, -.005F, .005F);
        font.drawInBatch(label, -font.width(label) / 2.0F, 0, 0x392A1D, false, pose.last().pose(), buffers, Font.DisplayMode.NORMAL, 0, light);
        pose.popPose();
    }
    @Override
    public int getViewDistance() { return 32; }
}
