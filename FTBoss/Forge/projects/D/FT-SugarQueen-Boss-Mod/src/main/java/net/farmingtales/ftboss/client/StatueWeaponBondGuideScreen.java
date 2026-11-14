package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.farmingtales.ftboss.util.WeaponBondGuideLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

public final class StatueWeaponBondGuideScreen extends Screen implements StatueThemeSource {
    private static final String PREFIX = "guide.ftboss.bonds.";
    private static final int CONTENT_TOP = 48;
    private final StatueUiTheme theme;
    private List<FormattedCharSequence> lines = List.of();
    private int scroll;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;

    private StatueWeaponBondGuideScreen(StatueUiTheme theme) {
        super(text("title"));
        this.theme = theme;
    }

    @Override
    public StatueUiTheme statueTheme() { return theme; }

    private static Component text(String key) { return Component.translatable(PREFIX + key); }

    public static Button entry(BooleanSupplier available, int positionX, int positionY, int buttonWidth) {
        return entry(available, positionX, positionY, buttonWidth, () -> StatueUiTheme.fromScreen(Minecraft.getInstance().screen));
    }

    public static Button entry(BooleanSupplier available, int positionX, int positionY, int buttonWidth, Supplier<StatueUiTheme> source) {
        return StatueThemeButton.create(text("entry"), button -> {
            Minecraft client = Minecraft.getInstance();
            if (client.player == null || !available.getAsBoolean()) return;
            StatueUiTheme captured = source.get();
            client.player.closeContainer();
            client.setScreen(new StatueWeaponBondGuideScreen(captured));
        }, positionX, positionY, buttonWidth, 16, source);
    }

    @Override
    protected void init() {
        panelWidth = WeaponBondGuideLayout.panelWidth(width);
        panelHeight = WeaponBondGuideLayout.panelHeight(height);
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        List<FormattedCharSequence> wrapped = new ArrayList<>();
        for (String section : List.of("unlock", "slots", "growth", "status")) {
            wrapped.addAll(font.split(text("intro." + section), Math.max(1, panelWidth - 48)));
            wrapped.add(FormattedCharSequence.EMPTY);
        }
        lines = List.copyOf(wrapped);
        scroll = WeaponBondGuideLayout.scroll(scroll, contentHeight(), viewportHeight());
        addRenderableWidget(StatueThemeButton.create(text("close"), button -> onClose(),
                left + 12, top + panelHeight - 29, 70, 20, this::statueTheme));
    }

    private int contentHeight() { return lines.size() * WeaponBondGuideLayout.LINE_HEIGHT; }

    private int viewportHeight() { return Math.max(1, panelHeight - CONTENT_TOP - 42); }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        StatueThemeRenderer.frame(graphics, left, top, panelWidth, panelHeight, theme);
        StatueThemeRenderer.crest(graphics, left + 12, top + 7, 24, theme);
        graphics.drawString(font, title, left + 44, top + 10, theme.accentBright, false);
        graphics.drawString(font, font.plainSubstrByWidth(text("subtitle").getString(), Math.max(1, panelWidth - 56)),
                left + 44, top + 23, theme.muted, false);
        StatueThemeRenderer.card(graphics, left + 12, top + CONTENT_TOP - 6,
                panelWidth - 24, viewportHeight() + 12, theme, false);
        int viewportTop = top + CONTENT_TOP;
        graphics.enableScissor(left + 18, viewportTop, left + panelWidth - 18, viewportTop + viewportHeight());
        try {
            for (int index = 0; index < lines.size(); index++) {
                int lineY = viewportTop + index * WeaponBondGuideLayout.LINE_HEIGHT - scroll;
                if (lineY + WeaponBondGuideLayout.LINE_HEIGHT > viewportTop && lineY < viewportTop + viewportHeight()) {
                    graphics.drawString(font, lines.get(index), left + 24, lineY, theme.text, false);
                }
            }
        } finally {
            graphics.disableScissor();
        }
        int maximum = Math.max(0, contentHeight() - viewportHeight());
        if (maximum > 0) {
            int thumbHeight = Math.max(12, viewportHeight() * viewportHeight() / contentHeight());
            int thumbY = viewportTop + scroll * (viewportHeight() - thumbHeight) / maximum;
            graphics.fill(left + panelWidth - 16, thumbY, left + panelWidth - 13, thumbY + thumbHeight, theme.accent);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (mouseX >= left + 12 && mouseX < left + panelWidth - 12
                && mouseY >= top + CONTENT_TOP && mouseY < top + CONTENT_TOP + viewportHeight()) {
            scroll = WeaponBondGuideLayout.scroll(scroll - (int) Math.round(amount * 24), contentHeight(), viewportHeight());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_DOWN || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            scroll = WeaponBondGuideLayout.scroll(scroll + (keyCode == GLFW.GLFW_KEY_DOWN ? 24 : viewportHeight()), contentHeight(), viewportHeight());
        } else if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            scroll = WeaponBondGuideLayout.scroll(scroll - (keyCode == GLFW.GLFW_KEY_UP ? 24 : viewportHeight()), contentHeight(), viewportHeight());
        } else if (keyCode == GLFW.GLFW_KEY_HOME) scroll = 0;
        else if (keyCode == GLFW.GLFW_KEY_END) scroll = WeaponBondGuideLayout.scroll(contentHeight(), contentHeight(), viewportHeight());
        else return super.keyPressed(keyCode, scanCode, modifiers);
        return true;
    }

    @Override
    public void tick() {
        if (minecraft.player == null || !minecraft.player.isAlive()) onClose();
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
