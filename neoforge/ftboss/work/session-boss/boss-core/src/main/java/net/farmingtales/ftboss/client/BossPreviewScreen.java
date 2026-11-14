package net.farmingtales.ftboss.client;

import net.farmingtales.ftboss.menu.BossPreviewMenu;
import net.farmingtales.ftboss.util.BossPreviewRules;
import net.farmingtales.ftboss.util.BossPreviewSummaryRules;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public final class BossPreviewScreen extends AbstractContainerScreen<BossPreviewMenu> {
    public BossPreviewScreen(BossPreviewMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 320;
        this.imageHeight = 228;
        this.titleLabelX = 16;
        this.titleLabelY = 12;
        this.inventoryLabelX = 148;
        this.inventoryLabelY = 133;
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xFF292733);
        graphics.fill(this.leftPos + 8, this.topPos + 29, this.leftPos + 312, this.topPos + 126, 0xFF191821);
        for (Slot slot : this.menu.slots) {
            int left = this.leftPos + slot.x;
            int top = this.topPos + slot.y;
            graphics.fill(left - 1, top - 1, left + 17, top + 17, 0xFF77717F);
            graphics.fill(left, top, left + 16, top + 16, 0xFF15131B);
        }
    }

    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFF1EAF5, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFCFC9D4, false);
        int profile = this.menu.profile();
        Component message = Component.translatable("screen.ftboss.boss_preview.insert");
        if (BossPreviewSummaryRules.showsKills(profile)) {
            var entity = BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(BossPreviewSummaryRules.entityId(profile)));
            if (entity.isPresent()) {
                graphics.drawString(this.font, entity.get().getDescription(), 18, 42, 0xFFFFD48C, false);
            }
            message = Component.translatable("screen.ftboss.boss_preview.kills", this.menu.playerKills());
        } else if (profile == BossPreviewRules.MANOR) {
            message = Component.translatable("screen.ftboss.boss_preview.manor");
        } else if (profile == BossPreviewRules.TITLE_REQUIRED) {
            message = Component.translatable("screen.ftboss.boss_preview.title_required");
        }
        graphics.drawWordWrap(this.font, message, 18, 64, 280, 0xFFE2DCE8);
        graphics.drawWordWrap(this.font, Component.translatable("screen.ftboss.boss_preview.slot"), 18, 184, 112, 0xFFCFC9D4);
    }
}
