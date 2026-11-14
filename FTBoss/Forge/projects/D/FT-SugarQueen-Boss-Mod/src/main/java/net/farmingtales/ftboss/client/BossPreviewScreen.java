package net.farmingtales.ftboss.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.menu.BossPreviewMenu;
import net.farmingtales.ftboss.util.BossPreviewRules;
import net.farmingtales.ftboss.util.BossPreviewLayout;
import net.farmingtales.ftboss.util.BossPreviewSummaryRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Quaternionf;

/** 模型与简短定位同页图鉴；2026-10-25 按目录实体身份绘制主题，保留投影、个人击杀统计与庄园切换。 */
public final class BossPreviewScreen extends AbstractContainerScreen<BossPreviewMenu> implements StatueThemeSource {
    private static final ResourceLocation CATALOG = new ResourceLocation(FTBossMod.MOD_ID, "boss_preview/catalog.json");
    private static final String SEALED_HEADER = "R7xQ9mK4vZ2nT8pL";
    private static final String SEALED_TEXT = "R7xQ9mK4vZ2nT8pL 6bY3hW5sD1cF0jA\nP4uG8eN2rV6xC9kM 3qJ7tB5zH1wS0dL\nX6aR2vK8mF4pT9nQ 5cZ1yG7hW3sD0jB";
    private final List<JsonObject> entries = new ArrayList<>();
    private List<FormattedCharSequence> guide = List.of();
    private LivingEntity projection;
    private int active = -3;
    private int manorSelection;
    private float yaw = 25.0F;
    private float pitch = -8.0F;
    private float zoom = 1.0F;
    private boolean spinning;
    private boolean draggingProjection;
    private Button previous;
    private Button next;
    private Button spin;

    public BossPreviewScreen(BossPreviewMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 320;
        imageHeight = 232;
        inventoryLabelX = 148;
        inventoryLabelY = 134;
    }

    @Override
    public StatueUiTheme statueTheme() {
        if (active < 0 || active >= entries.size()) return StatueUiTheme.forTint(0);
        JsonObject entry = entries.get(active);
        int tint = switch (entry.get("entity").getAsString()) {
            case "ftboss:sugar_queen" -> 1;
            case "ftboss:meat_mountain" -> 2;
            case "ftboss:tea_tree" -> 3;
            case "ftboss:wine_barrel" -> 4;
            case "ftboss:rat_boss" -> 5;
            case "ftboss:defect_core" -> 6;
            case "ftboss:compost_boss" -> 7;
            case "ftboss:apple_boss" -> 8;
            case "ftboss:tide_fish_boss" -> 9;
            case "ftboss:benger" -> 10;
            case "ftboss:easter_boss" -> switch (entry.has("variant") ? entry.get("variant").getAsInt() : -1) {
                case 0 -> 8;
                case 1, 5, 11 -> 6;
                case 2, 6 -> 9;
                case 3 -> 2;
                case 4 -> 5;
                case 7 -> 7;
                case 8 -> 4;
                case 9 -> 10;
                case 10 -> 1;
                default -> 0;
            };
            default -> 0;
        };
        return StatueUiTheme.forTint(tint);
    }

    @Override
    protected void init() {
        imageWidth = BossPreviewLayout.modelWidth(width);
        super.init();
        entries.clear();
        try (var reader = new InputStreamReader(minecraft.getResourceManager().open(CATALOG), StandardCharsets.UTF_8)) {
            JsonArray catalog = JsonParser.parseReader(reader).getAsJsonArray();
            for (var entry : catalog) entries.add(entry.getAsJsonObject());
        } catch (Exception exception) {
            FTBossMod.LOGGER.error("[FTBoss][BossPreview] Cannot load catalogue", exception);
        }
        previous = addRenderableWidget(StatueThemeButton.create(Component.literal("<"), button -> selectManor(-1),
                leftPos + BossPreviewLayout.previousX(imageWidth), topPos + 114, 20, 18, this::statueTheme));
        next = addRenderableWidget(StatueThemeButton.create(Component.literal(">"), button -> selectManor(1),
                leftPos + BossPreviewLayout.nextX(imageWidth), topPos + 114, 20, 18, this::statueTheme));
        addRenderableWidget(StatueThemeButton.create(Component.translatable("ui.ftboss.preview.reset"), button -> resetView(),
                leftPos + 9, topPos + 114, 48, 18, this::statueTheme));
        spin = addRenderableWidget(StatueThemeButton.create(Component.translatable("ui.ftboss.preview.rotate"), button -> spinning = !spinning,
                leftPos + 61, topPos + 114, 56, 18, this::statueTheme));
        active = -3;
        refreshProjection();
        addBuildInspectorEntry();
        addRenderableWidget(StatueWeaponBondGuideEntry.create(this));
    }

    private void addBuildInspectorEntry() {
        int buttonWidth = 132;
        int buttonHeight = 16;
        int insideY = topPos + imageHeight - 21;
        int belowY = topPos + imageHeight + 6;
        int aboveY = topPos - buttonHeight - 6;
        int buttonY = belowY + buttonHeight <= height ? belowY : aboveY >= 0 ? aboveY : insideY;
        addRenderableWidget(StatueBuildInspectorScreen.previewEntry(menu.statuePos(), this::inspectedWeapon,
                this::canInspectBuild, leftPos + 8, buttonY, buttonWidth));
    }

    private ItemStack inspectedWeapon() {
        if (minecraft == null || minecraft.player == null) return ItemStack.EMPTY;
        ItemStack main = minecraft.player.getMainHandItem();
        if (FourWeaponFoundationAdapter.applies(main) || SwordCavityBuild.applies(main)
                || FoodWeaponEvolution.isEvolutionCandidate(main)) return main;
        ItemStack off = minecraft.player.getOffhandItem();
        return FourWeaponFoundationAdapter.applies(off) || SwordCavityBuild.applies(off)
                || FoodWeaponEvolution.isEvolutionCandidate(off) ? off : ItemStack.EMPTY;
    }

    private boolean canInspectBuild() {
        return minecraft != null && minecraft.player != null
                && minecraft.player.containerMenu == menu
                && menu.getCarried().isEmpty();
    }

    private void updateModelControls() {
        previous.visible = menu.profile() == BossPreviewRules.MANOR;
        next.visible = previous.visible;
    }

    private void resetView() {
        yaw = 25.0F;
        pitch = -8.0F;
        zoom = 1.0F;
        spinning = false;
    }

    private void selectManor(int offset) {
        manorSelection = Math.floorMod(manorSelection + offset, Math.max(1, entries.size() - BossPreviewRules.MANOR));
        refreshProjection();
    }

    private void refreshProjection() {
        boolean manor = menu.profile() == BossPreviewRules.MANOR;
        int requested = manor ? BossPreviewRules.MANOR + manorSelection : menu.profile();
        spin.active = requested >= 0;
        if (requested == active) {
            updateModelControls();
            return;
        }
        active = requested;
        projection = null;
        guide = List.of();
        draggingProjection = false;
        setDragging(false);
        resetView();
        if (active < 0 || active >= entries.size()) {
            Component message = active == BossPreviewRules.TITLE_REQUIRED ? encryptedText(SEALED_TEXT)
                    : Component.translatable("ui.ftboss.preview.empty");
            guide = font.split(message, BossPreviewLayout.unscaledWidth(imageWidth - 24));
            updateModelControls();
            return;
        }
        JsonObject entry = entries.get(active);
        Component summary = entry.has("summary") ? Component.literal(entry.get("summary").getAsString())
                : Component.translatable("ui.ftboss.preview.no_summary");
        guide = font.split(summary, BossPreviewLayout.unscaledWidth(BossPreviewLayout.summaryWidth(imageWidth)));
        updateModelControls();
        try {
            var type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entry.get("entity").getAsString()));
            if (type != null && type.create(minecraft.level) instanceof LivingEntity living) {
                projection = living;
                if (living instanceof EasterBossEntity easter) easter.setVariant(entry.get("variant").getAsInt());
                if (entry.has("weapon")) {
                    var weapon = ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry.get("weapon").getAsString()));
                    if (weapon != null) living.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weapon));
                }
                if (living instanceof Mob mob) mob.setNoAi(true);
                living.setSilent(true);
                living.setCustomNameVisible(false);
            }
        } catch (Exception exception) {
            FTBossMod.LOGGER.error("[FTBoss][BossPreview] Cannot create local projection {}", active, exception);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        refreshProjection();
        if (spinning) yaw = Mth.wrapDegrees(yaw + 1.5F);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        refreshProjection();
        StatueUiTheme theme = statueTheme();
        StatueThemeRenderer.frame(graphics, leftPos, topPos, imageWidth, imageHeight, theme);
        StatueUiDecor.catalogue(graphics, leftPos + 8, topPos + 3, 18);
        StatueThemeRenderer.line(graphics, leftPos + 8, topPos + 23, imageWidth - 16, theme);
        StatueThemeRenderer.card(graphics, leftPos + 5, topPos + 26, BossPreviewLayout.projectionRight(imageWidth) - 3, 86, theme, false);
        StatueThemeRenderer.card(graphics, leftPos + BossPreviewLayout.summaryX(imageWidth) - 6, topPos + 26,
                BossPreviewLayout.summaryWidth(imageWidth) + 11, 86, theme, false);
        StatueThemeRenderer.line(graphics, leftPos + 25, topPos + 106, BossPreviewLayout.projectionRight(imageWidth) - 44, theme);
        StatueThemeRenderer.card(graphics, leftPos + 6, topPos + 136, 134, 89, theme, false);
        StatueThemeRenderer.card(graphics, leftPos + 144, topPos + 142, 168, 81, theme, false);
        for (var slot : menu.slots) {
            graphics.fill(leftPos + slot.x - 1, topPos + slot.y - 1, leftPos + slot.x + 17, topPos + slot.y + 17, theme.border);
            graphics.fill(leftPos + slot.x, topPos + slot.y, leftPos + slot.x + 16, topPos + slot.y + 16, theme.background);
            graphics.fill(leftPos + slot.x, topPos + slot.y, leftPos + slot.x + 16, topPos + slot.y + 1, 0xFF080B0D);
            if (slot.index == 0) graphics.fill(leftPos + slot.x, topPos + slot.y + 15, leftPos + slot.x + 16, topPos + slot.y + 16, theme.accentBright);
            boolean hovered = mouseX >= leftPos + slot.x && mouseX < leftPos + slot.x + 16
                    && mouseY >= topPos + slot.y && mouseY < topPos + slot.y + 16;
            StatueUiDecor.socket(graphics, leftPos + slot.x - 1, topPos + slot.y - 1, 18, hovered ? 1 : slot.index == 0 ? 2 : 0);
        }
        if (projection != null) {
            StatueUiDecor.pedestal(graphics, leftPos + (6 + BossPreviewLayout.projectionRight(imageWidth)) / 2, topPos + 85);
            float extent = Math.max(projection.getBbHeight(), projection.getBbWidth() * 1.4F);
            int scale = Math.max(1, Math.round(65.0F / Math.max(1.8F, extent) * zoom));
            graphics.enableScissor(leftPos + 6, topPos + 27, leftPos + BossPreviewLayout.projectionRight(imageWidth), topPos + 111);
            try {
                projection.yBodyRot = 180.0F;
                projection.setYRot(180.0F);
                projection.yHeadRot = 180.0F;
                projection.yHeadRotO = 180.0F;
                Quaternionf pose = new Quaternionf().rotationZ((float) Math.PI)
                        .rotateX(pitch * Mth.DEG_TO_RAD).rotateY(yaw * Mth.DEG_TO_RAD);
                InventoryScreen.renderEntityInInventory(graphics, leftPos + (6 + BossPreviewLayout.projectionRight(imageWidth)) / 2,
                        topPos + 101, scale, pose, null, projection);
            } finally {
                graphics.disableScissor();
            }
        }
        if (active < 0) {
            for (int line = 0; line < Math.min(guide.size(), 6); line++) {
                drawSmallText(graphics, guide.get(line), leftPos + 12, topPos + 38 + line * BossPreviewLayout.LINE_HEIGHT, theme.muted);
            }
        }
        if (active >= 0 && active < entries.size()) {
            int summaryLeft = leftPos + BossPreviewLayout.summaryX(imageWidth);
            drawSmallText(graphics, Component.translatable("ui.ftboss.preview.role").getVisualOrderText(), summaryLeft, topPos + 33, theme.accentBright);
            StatueThemeRenderer.line(graphics, summaryLeft, topPos + 43, BossPreviewLayout.summaryWidth(imageWidth), theme);
            for (int line = 0; line < guide.size(); line++) {
                drawSmallText(graphics, guide.get(line), summaryLeft, topPos + 48 + line * BossPreviewLayout.LINE_HEIGHT, theme.text);
            }
        }
        drawSmallText(graphics, Component.translatable("ui.ftboss.preview.input").getVisualOrderText(), leftPos + 12, topPos + 141, theme.accentBright);
        List<FormattedCharSequence> help = font.split(Component.translatable("ui.ftboss.preview.help"), BossPreviewLayout.unscaledWidth(132));
        for (int line = 0; line < help.size(); line++) drawSmallText(graphics, help.get(line), leftPos + 9,
                topPos + 179 + line * BossPreviewLayout.LINE_HEIGHT, theme.muted);
        if (menu.profile() == BossPreviewRules.MANOR) drawSmallCentered(graphics,
                (manorSelection + 1) + " / " + (entries.size() - BossPreviewRules.MANOR),
                leftPos + BossPreviewLayout.pageCenterX(imageWidth), topPos + 119, 44, theme.accentBright);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String name = active >= 0 && active < entries.size() ? entries.get(active).get("name").getAsString() : title.getString();
        boolean showKills = BossPreviewSummaryRules.showsKills(active);
        Component heading = active == BossPreviewRules.TITLE_REQUIRED ? encryptedText(SEALED_HEADER)
                : Component.literal(font.plainSubstrByWidth(name, BossPreviewLayout.unscaledWidth(imageWidth - (showKills ? 174 : 48))));
        StatueUiTheme theme = statueTheme();
        drawSmallText(graphics, heading.getVisualOrderText(), 32, 8, theme.text);
        if (showKills) drawSmallCentered(graphics, Component.translatable("ui.ftboss.preview.kills", menu.playerKills()).getString(),
                imageWidth - 72, 8, 130, theme.accentBright);
        drawSmallText(graphics, playerInventoryTitle.getVisualOrderText(), inventoryLabelX, inventoryLabelY, theme.muted);
    }

    private boolean overProjection(double mouseX, double mouseY) {
        return mouseX >= leftPos + 6 && mouseX < leftPos + BossPreviewLayout.projectionRight(imageWidth)
                && mouseY >= topPos + 27 && mouseY < topPos + 112;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && overProjection(mouseX, mouseY) && menu.getCarried().isEmpty()) {
            draggingProjection = true;
            setDragging(true);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && draggingProjection) {
            spinning = false;
            yaw = Mth.wrapDegrees(yaw + (float) deltaX * 2.0F);
            pitch = Mth.clamp(pitch + (float) deltaY, -50.0F, 50.0F);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingProjection = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (overProjection(mouseX, mouseY)) {
            zoom = Mth.clamp(zoom + (float) delta * 0.1F, 0.6F, 1.8F);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private static Component encryptedText(String text) {
        return Component.literal(text).withStyle(ChatFormatting.OBFUSCATED);
    }

    private void drawSmallText(GuiGraphics graphics, FormattedCharSequence text, int positionX, int positionY, int color) {
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(positionX, positionY, 0);
            graphics.pose().scale(BossPreviewLayout.TEXT_SCALE, BossPreviewLayout.TEXT_SCALE, 1);
            graphics.drawString(font, text, 0, 0, color, false);
        } finally {
            graphics.pose().popPose();
        }
    }

    private void drawSmallCentered(GuiGraphics graphics, String text, int centerX, int positionY, int availableWidth, int color) {
        String fitted = font.plainSubstrByWidth(text, BossPreviewLayout.unscaledWidth(availableWidth));
        int textWidth = (int) Math.ceil(font.width(fitted) * BossPreviewLayout.TEXT_SCALE);
        drawSmallText(graphics, Component.literal(fitted).getVisualOrderText(), centerX - textWidth / 2, positionY, color);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        refreshProjection();
        super.render(graphics, mouseX, mouseY, partialTick);
        if (spinning) graphics.fill(spin.getX() + 3, spin.getY() + spin.getHeight() - 3,
                spin.getX() + spin.getWidth() - 3, spin.getY() + spin.getHeight() - 1, statueTheme().accentBright);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
