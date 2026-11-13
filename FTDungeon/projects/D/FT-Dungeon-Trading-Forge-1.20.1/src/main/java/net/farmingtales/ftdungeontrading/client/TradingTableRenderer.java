package net.farmingtales.ftdungeontrading.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import net.farmingtales.ftdungeontrading.TradeTableBlock;
import net.farmingtales.ftdungeontrading.TradeTableBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class TradingTableRenderer implements BlockEntityRenderer<TradeTableBlockEntity> {
    private static final float PAYMENT_HORIZONTAL = .255F;
    private static final float GOODS_HORIZONTAL = -.255F;

    public TradingTableRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(TradeTableBlockEntity table, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        pose.translate(.5, 0, .5);
        pose.mulPose(Axis.YP.rotationDegrees(-table.getBlockState().getValue(TradeTableBlock.FACING).toYRot() + 180));
        renderSide(table.displayCosts(), PAYMENT_HORIZONTAL, pose, buffers, light);
        renderSide(table.displayRewards(), GOODS_HORIZONTAL, pose, buffers, light);
        renderLabel(Component.translatable("label.ftdungeontrading.pay"), PAYMENT_HORIZONTAL, pose, buffers, light, 0xE4BD71);
        renderLabel(Component.translatable("label.ftdungeontrading.receive"), GOODS_HORIZONTAL, pose, buffers, light, 0x95D9C3);
        if (table.remainingStock() == 0) renderLabel(Component.translatable("label.ftdungeontrading.sold_out"), 0, pose, buffers, light, 0xFF7C70);
        pose.popPose();
    }

    private static void renderSide(List<ItemStack> stacks, double horizontal, PoseStack pose, MultiBufferSource buffers, int light) {
        for (int index = 0; index < Math.min(3, stacks.size()); index++) {
            pose.pushPose();
            pose.translate(horizontal, .99, (index - (stacks.size() - 1) / 2.0) * .22);
            pose.scale(.29F, .29F, .29F);
            Minecraft.getInstance().getItemRenderer().renderStatic(stacks.get(index), ItemDisplayContext.GROUND,
                    light, OverlayTexture.NO_OVERLAY, pose, buffers, Minecraft.getInstance().level, index);
            pose.popPose();
        }
    }

    private static void renderLabel(Component text, float horizontal, PoseStack pose, MultiBufferSource buffers, int light, int color) {
        Font font = Minecraft.getInstance().font;
        pose.pushPose();
        pose.translate(horizontal, .72, -.508);
        pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.scale(.006F, -.006F, .006F);
        font.drawInBatch(text, -font.width(text) / 2.0F, 0, color, false, pose.last().pose(), buffers,
                Font.DisplayMode.NORMAL, 0, light);
        pose.popPose();
    }

    @Override
    public int getViewDistance() { return 32; }
}
