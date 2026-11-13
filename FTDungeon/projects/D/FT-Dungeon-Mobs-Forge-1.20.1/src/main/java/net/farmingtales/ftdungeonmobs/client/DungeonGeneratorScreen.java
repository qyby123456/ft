package net.farmingtales.ftdungeonmobs.client;

import java.util.List;
import net.farmingtales.ftdungeonmobs.DungeonGeneratorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class DungeonGeneratorScreen extends AbstractContainerScreen<DungeonGeneratorMenu> {
    private int page;
    private int lastSelected = Integer.MIN_VALUE;
    private boolean lastLocked;
    private static final int PAGE_SIZE = 5;

    public DungeonGeneratorScreen(DungeonGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 300;
        imageHeight = 222;
    }

    @Override
    protected void init() {
        super.init();
        buttons();
    }

    private void buttons() {
        lastSelected = menu.selected();
        lastLocked = menu.locked();
        clearWidgets();
        List<DungeonGeneratorMenu.Entry> entries = menu.entries();
        for (int row = 0; row < PAGE_SIZE; row++) {
            int index = page * PAGE_SIZE + row;
            if (index >= entries.size()) { break; }
            var entry = entries.get(index);
            Button button = Button.builder(entry.name(), ignored -> {
                if (minecraft.gameMode != null) { minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index); }
            }).bounds(leftPos + 14, topPos + 49 + row * 25, imageWidth - 28, 20).build();
            button.setTooltip(net.minecraft.client.gui.components.Tooltip.create(entry.description()));
            button.active = !menu.locked() && menu.selected() != index;
            addRenderableWidget(button);
        }
        addRenderableWidget(Button.builder(Component.literal("<"), ignored -> { page--; buttons(); })
                .bounds(leftPos + 14, topPos + 185, 32, 20).build()).active = page > 0;
        addRenderableWidget(Button.builder(Component.literal(">"), ignored -> { page++; buttons(); })
                .bounds(leftPos + 52, topPos + 185, 32, 20).build()).active = (page + 1) * PAGE_SIZE < entries.size();
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), ignored -> onClose())
                .bounds(leftPos + imageWidth - 94, topPos + 185, 80, 20).build());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (lastSelected != menu.selected() || lastLocked != menu.locked()) { buttons(); }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xF0201B29);
        graphics.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + 31, 0xFF40334E);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 14, 12, 0xF0E6FF, false);
        graphics.drawString(font, Component.translatable(menu.locked() ? "menu.ftdungeonmobs.generator.locked"
                : "menu.ftdungeonmobs.generator.hint"), 14, 35, 0xC0B5CE, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
