package cn.farmingtales.qysevent.client;

import cn.farmingtales.qysevent.QysEventMod;
import cn.farmingtales.qysevent.mixin.SlotPositionAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = QysEventMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WishingWellShopScreen extends AbstractContainerScreen<ChestMenu> {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(QysEventMod.MOD_ID,
            "textures/gui/wishing_well_shop.png");
    private static final int GOLD = 0x745019;
    private static final int TEXT = 0x263127;
    private static final int MUTED = 0x404C41;
    private static final int[] FRAME_SOURCE = {0, 5, 27, 32};
    private static final ResourceLocation[] FRAMES = {
            frame("normal"), frame("hover"), frame("selected"), frame("disabled")};

    private static ResourceLocation frame(String state) {
        return ResourceLocation.fromNamespaceAndPath(QysEventMod.MOD_ID, "textures/gui/wishing_well_socket_" + state + ".png");
    }

    public WishingWellShopScreen(ChestMenu menu, Inventory inventory) {
        super(menu, inventory, Component.translatable("screen.qys_event.wishing_well.title"));
        imageWidth = WishingWellShopLayout.WIDTH;
        imageHeight = WishingWellShopLayout.HEIGHT;
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (!(event.getNewScreen() instanceof ContainerScreen chest)) return;
        if (chest.getMenu().getRowCount() != 4
                || !WishingWellShopLayout.MENU_TITLE.equals(chest.getTitle().getString())) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            event.setNewScreen(new WishingWellShopScreen(chest.getMenu(), minecraft.player.getInventory()));
        }
    }

    @Override
    protected void init() {
        super.init();
        slotColor = 0x305CDBAD;
        for (int index = 0; index < menu.slots.size(); index++) {
            WishingWellShopLayout.Position position = WishingWellShopLayout.position(index);
            SlotPositionAccessor accessor = (SlotPositionAccessor) menu.slots.get(index);
            accessor.qysEvent$setX(position.x());
            accessor.qysEvent$setY(position.y());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        Slot product = productAt(mouseX, mouseY);
        if (product != null && product.hasItem()) {
            graphics.renderTooltip(font, product.getItem(), mouseX, mouseY);
        } else {
            renderTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        if (!manorAvailable()) {
            graphics.fill(leftPos + 120, topPos + 100, leftPos + 289, topPos + 143, 0xFFC6C5C3);
        }
        for (int index = 0; index < 36; index++) {
            WishingWellShopLayout.Bounds bounds = WishingWellShopLayout.productBounds(index);
            if (bounds == null) continue;
            Slot entry = menu.slots.get(index);
            if (index >= 30 && !entry.hasItem()) continue;
            boolean hovered = bounds.contains(mouseX - leftPos, mouseY - topPos);
            int state = !entry.hasItem() ? 3 : hovered ? 1 : index >= 30 ? 2 : 0;
            border(graphics, leftPos + bounds.x(), topPos + bounds.y(), bounds.width(), bounds.height(), state);
        }
        Slot product = productAt(mouseX, mouseY);
        if (product != null && product.hasItem()) {
            WishingWellShopLayout.Bounds bounds = WishingWellShopLayout.productBounds(product.index);
            int left = leftPos + bounds.x();
            int top = topPos + bounds.y();
            graphics.fill(left + 1, top + 1, left + bounds.width() - 1, top + bounds.height() - 1, 0x305CDBAD);
            graphics.fill(left, top, left + bounds.width(), top + 1, 0xFF6C591F);
            graphics.fill(left, top + bounds.height() - 1, left + bounds.width(), top + bounds.height(), 0xFF6C591F);
        }
    }

    private void border(GuiGraphics graphics, int left, int top, int width, int height, int state) {
        for (int row = 0; row < 3; row++) {
            int firstY = top + WishingWellShopLayout.frameEdge(row, height);
            int lastY = top + WishingWellShopLayout.frameEdge(row + 1, height);
            for (int column = 0; column < 3; column++) {
                int firstX = left + WishingWellShopLayout.frameEdge(column, width);
                int lastX = left + WishingWellShopLayout.frameEdge(column + 1, width);
                graphics.blit(FRAMES[state], firstX, firstY, lastX - firstX, lastY - firstY,
                        FRAME_SOURCE[column], FRAME_SOURCE[row], FRAME_SOURCE[column + 1] - FRAME_SOURCE[column],
                        FRAME_SOURCE[row + 1] - FRAME_SOURCE[row], 32, 32);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        label(graphics, "title", 15, 12, GOLD);
        label(graphics, "subtitle", 15, 26, MUTED);
        smallLabel(graphics, "level", 236, 14, 0x344F2B, 0.8F);
        smallLabel(graphics, "summons", 124, 41, TEXT, 0.85F);
        smallLabel(graphics, "summon_price", 220, 42, GOLD, 0.7F);
        if (manorAvailable()) smallLabel(graphics, "manor", 124, 103, TEXT, 0.8F);
        if (menu.slots.get(WishingWellShopLayout.WEAPON_SLOT).hasItem()) {
            smallLabel(graphics, "weapon", 151, 119, TEXT, 0.7F);
            smallLabel(graphics, "weapon_price", 151, 130, GOLD, 0.65F);
        }
        if (menu.slots.get(WishingWellShopLayout.SPELL_SLOT).hasItem()) {
            smallLabel(graphics, "spell", 235, 119, TEXT, 0.7F);
            smallLabel(graphics, "spell_price", 235, 130, GOLD, 0.65F);
        }
        smallLabel(graphics, "inventory", 124, 145, MUTED, 0.8F);
        smallLabel(graphics, "purchase", 219, 145, MUTED, 0.7F);
        smallLabel(graphics, "blessing", 24, 135, GOLD, 0.85F);
        smallLabel(graphics, "unlocked", 24, 148, MUTED, 0.75F);
        smallLabel(graphics, "chance", 47, 164, TEXT, 0.75F);
        smallLabel(graphics, "chance_value", 47, 176, GOLD, 0.8F);
        smallLabel(graphics, "wish", 24, 199, TEXT, 0.75F);
        smallLabel(graphics, "gold_wish", 24, 212, MUTED, 0.7F);
    }

    private void label(GuiGraphics graphics, String key, int left, int top, int color) {
        graphics.drawString(font, Component.translatable("screen.qys_event.wishing_well." + key), left, top, color, false);
    }

    private void smallLabel(GuiGraphics graphics, String key, int left, int top, int color, float scale) {
        graphics.pose().pushPose();
        graphics.pose().translate(left, top, 0);
        graphics.pose().scale(scale, scale, 1.0F);
        label(graphics, key, 0, 0, color);
        graphics.pose().popPose();
    }

    private boolean manorAvailable() {
        return menu.slots.get(WishingWellShopLayout.WEAPON_SLOT).hasItem()
                || menu.slots.get(WishingWellShopLayout.SPELL_SLOT).hasItem();
    }

    private Slot productAt(double mouseX, double mouseY) {
        for (int index = 0; index < 36; index++) {
            if (index >= 30 && !menu.slots.get(index).hasItem()) continue;
            WishingWellShopLayout.Bounds bounds = WishingWellShopLayout.productBounds(index);
            if (bounds != null && bounds.contains(mouseX - leftPos, mouseY - topPos)) return menu.slots.get(index);
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Slot product = productAt(mouseX, mouseY);
        if (product != null) {
            if (button == 0 && product.hasItem()) slotClicked(product, product.index, 0, ClickType.PICKUP);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
