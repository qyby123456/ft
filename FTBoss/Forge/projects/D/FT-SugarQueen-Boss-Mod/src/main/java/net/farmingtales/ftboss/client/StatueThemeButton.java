package net.farmingtales.ftboss.client;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/** 石像主题按钮；2026-10-24 仅替换绘制，保留原版点击、声音、键盘、焦点、提示及叙述。 */
public final class StatueThemeButton extends Button {
    private final Supplier<StatueUiTheme> theme;

    private StatueThemeButton(Component label, OnPress action, int x, int y, int width, int height, Supplier<StatueUiTheme> theme) {
        super(x, y, width, height, label, action, DEFAULT_NARRATION);
        this.theme = theme;
    }

    public static Button create(Component label, OnPress action, int x, int y, int width, int height, Supplier<StatueUiTheme> theme) {
        return new StatueThemeButton(label, action, x, y, width, height, theme);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        StatueUiTheme current = this.theme.get();
        int state = !this.active ? 3 : this.isFocused() ? 2 : this.isHovered() ? 1 : 0;
        graphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        StatueThemeRenderer.button(graphics, this.getX(), this.getY(), this.width, this.height, current, state);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int color = this.active ? current.text : current.muted;
        color = color & 0xFFFFFF | Mth.ceil(this.alpha * 255.0F) << 24;
        this.renderScrollingString(graphics, Minecraft.getInstance().font, 4, color);
    }
}
