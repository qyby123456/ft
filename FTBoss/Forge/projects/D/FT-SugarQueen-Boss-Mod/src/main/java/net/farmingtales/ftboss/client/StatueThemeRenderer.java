package net.farmingtales.ftboss.client;

import net.minecraft.client.gui.GuiGraphics;

/** 石像像素面板与纹章绘制；2026-10-24 九宫格保留边角轮廓，正文区域保持安静。 */
public final class StatueThemeRenderer {
    private static final int FRAME_TEXTURE_SIZE = 32;
    private static final int FRAME_INSET = 6;
    private static final int CARD_TEXTURE_SIZE = 16;
    private static final int CARD_INSET = 3;
    private static final int CREST_SIZE = 32;
    private static final int BUTTON_TEXTURE_WIDTH = 64;
    private static final int BUTTON_TEXTURE_HEIGHT = 20;

    private StatueThemeRenderer() {
    }

    public static void frame(GuiGraphics graphics, int x, int y, int width, int height, StatueUiTheme theme) {
        if (width <= 0 || height <= 0) return;
        graphics.blitNineSliced(theme.atlasTexture, x, y, width, height, FRAME_INSET,
                FRAME_TEXTURE_SIZE, FRAME_TEXTURE_SIZE, 0, 0);
    }

    public static void card(GuiGraphics graphics, int x, int y, int width, int height, StatueUiTheme theme, boolean selected) {
        if (width <= 0 || height <= 0) return;
        graphics.blitNineSliced(theme.atlasTexture, x, y, width, height, CARD_INSET,
                CARD_TEXTURE_SIZE, CARD_TEXTURE_SIZE, selected ? 60 : 40, 0);
    }

    public static void crest(GuiGraphics graphics, int x, int y, int size, StatueUiTheme theme) {
        if (size <= 0) return;
        graphics.blit(theme.texture, x, y, size, size, 0.0F, 0.0F, CREST_SIZE, CREST_SIZE, CREST_SIZE, CREST_SIZE);
    }

    public static void line(GuiGraphics graphics, int x, int y, int width, StatueUiTheme theme) {
        if (width <= 0) return;
        graphics.fill(x, y, x + width, y + 1, theme.border);
        graphics.fill(x, y + 1, x + width, y + 2, theme.background);
        int cap = Math.min(3, width);
        graphics.fill(x, y, x + cap, y + 1, theme.accent);
        graphics.fill(x + width - cap, y, x + width, y + 1, theme.accent);
    }

    static void button(GuiGraphics graphics, int x, int y, int width, int height, StatueUiTheme theme, int state) {
        graphics.blitNineSliced(theme.atlasTexture, x, y, width, height, CARD_INSET,
                BUTTON_TEXTURE_WIDTH, BUTTON_TEXTURE_HEIGHT, 0, 40 + state * BUTTON_TEXTURE_HEIGHT);
    }
}
