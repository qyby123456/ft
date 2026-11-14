package net.farmingtales.ftboss.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

/** 石像子页共用的 image2.5 嵌槽与装饰；2026-10-26：仅绘制，不改变布局和命中坐标。 */
public final class StatueUiDecor {
    private static final ResourceLocation[] SOCKETS = {
            texture("socket_normal"), texture("socket_hover"), texture("socket_selected"), texture("socket_disabled")};
    private static final ResourceLocation GEAR = texture("cavity_gear");
    private static final ResourceLocation CATALOGUE = texture("catalogue");
    private static final ResourceLocation STAR = texture("star_seal");
    private static final ResourceLocation PEDESTAL = texture("pedestal");

    private StatueUiDecor() {}

    private static ResourceLocation texture(String name) {
        return new ResourceLocation("ftboss", "textures/gui/statue_details/" + name + ".png");
    }

    public static void socket(GuiGraphics graphics, int left, int top, int size, int state) {
        draw(graphics, SOCKETS[Math.max(0, Math.min(3, state))], left, top, size, size);
    }

    public static void gear(GuiGraphics graphics, int left, int top, int size) {
        draw(graphics, GEAR, left, top, size, size);
    }

    public static void catalogue(GuiGraphics graphics, int left, int top, int size) {
        draw(graphics, CATALOGUE, left, top, size, size);
    }

    public static void star(GuiGraphics graphics, int left, int top, int size) {
        draw(graphics, STAR, left, top, size, size);
    }

    public static void pedestal(GuiGraphics graphics, int center, int top) {
        draw(graphics, PEDESTAL, center - 40, top, 80, 24);
    }

    private static void draw(GuiGraphics graphics, ResourceLocation texture, int left, int top, int width, int height) {
        graphics.blit(texture, left, top, width, height, 0, 0, 32, 32, 32, 32);
    }
}
