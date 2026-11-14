package net.farmingtales.ftboss.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import java.util.Optional;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.WeaponDetailView;
import net.farmingtales.ftboss.item.FoodWeaponSkillLoadout;
import net.farmingtales.ftboss.network.StatueMenuNetwork;
import net.farmingtales.ftboss.util.WeaponSkillLoadoutRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** 食武多技能轮盘；2026-10-12，按住可改绑的 G 选择，松开确认，沿用 J 施放与原技能冷却。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, value = Dist.CLIENT)
public final class FoodWeaponSkillWheel extends Screen {
    private static final int RADIUS = 82;
    private static final int DEAD_ZONE = 24;
    private static final int RING_POINTS = 80;
    private static final KeyMapping WHEEL = new KeyMapping("key.ftboss.skill_wheel", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.ftboss");
    private final ItemStack weapon;
    private final int hotbarSlot;
    private final List<WeaponDetailView> skills;
    private int choice;
    private int lastMouseX = Integer.MIN_VALUE;
    private int lastMouseY = Integer.MIN_VALUE;

    private FoodWeaponSkillWheel(ItemStack weapon, int hotbarSlot) {
        super(Component.translatable("screen.ftboss.skills.wheel"));
        this.weapon = weapon.copy();
        this.hotbarSlot = hotbarSlot;
        this.skills = FoodWeaponSkillLoadout.carriedSkills(weapon);
        int active = FoodWeaponSkillLoadout.activeIndex(weapon);
        for (int index = 0; index < this.skills.size(); index++) {
            if (this.skills.get(index).gourmetSkillIndex() == active) this.choice = index;
        }
    }

    public static Component keyName() { return WHEEL.getTranslatedKeyMessage(); }

    @SubscribeEvent
    public static void openWheel(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        boolean pressed = false;
        while (WHEEL.consumeClick()) pressed = true;
        Minecraft minecraft = Minecraft.getInstance();
        if (!pressed || minecraft.screen != null || minecraft.player == null || minecraft.level == null
                || minecraft.getConnection() == null || !minecraft.isWindowActive() || !minecraft.player.isAlive()
                || minecraft.player.isSpectator() || minecraft.player.isUsingItem() || minecraft.player.isSleeping()) return;
        ItemStack weapon = minecraft.player.getMainHandItem();
        if (FoodWeaponSkillLoadout.carriedSkills(weapon).isEmpty()) return;
        minecraft.setScreen(new FoodWeaponSkillWheel(weapon, minecraft.player.getInventory().selected));
    }

    private boolean sameWeapon() {
        return this.minecraft != null && this.minecraft.player != null && this.minecraft.level != null
                && this.minecraft.player.isAlive() && !this.minecraft.player.isSpectator() && !this.minecraft.player.isUsingItem()
                && this.minecraft.player.getInventory().selected == this.hotbarSlot
                && ItemStack.matches(this.weapon, this.minecraft.player.getMainHandItem());
    }

    private boolean heldKey() {
        InputConstants.Key key = WHEEL.getKey();
        long window = this.minecraft.getWindow().getWindow();
        return key.getType() == InputConstants.Type.MOUSE ? GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS
                : key.getType() == InputConstants.Type.KEYSYM && key.getValue() >= 0 && InputConstants.isKeyDown(window, key.getValue());
    }

    @Override
    public void tick() {
        super.tick();
        if (!sameWeapon() || !this.minecraft.isWindowActive()) this.onClose();
        else if (!heldKey()) confirm();
    }

    private void confirm() {
        if (sameWeapon() && this.choice >= 0 && this.choice < this.skills.size()) {
            int index = this.skills.get(this.choice).gourmetSkillIndex();
            if (index != FoodWeaponSkillLoadout.activeIndex(this.weapon)) {
                StatueMenuNetwork.SkillSelection.send(this.weapon, this.hotbarSlot, index);
            }
        }
        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        int centerX = this.width / 2;
        int centerY = this.height / 2 - 8;
        int hovered = WeaponSkillLoadoutRules.wheelIndex(this.skills.size(), mouseX - centerX, mouseY - centerY, DEAD_ZONE);
        if (hovered >= 0 && (mouseX != this.lastMouseX || mouseY != this.lastMouseY)) this.choice = hovered;
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        graphics.drawCenteredString(this.font, this.title, centerX, centerY - 112, 0xFFFFD77A);
        for (int pointIndex = 0; pointIndex < RING_POINTS; pointIndex++) {
            double angle = pointIndex * Math.PI * 2.0 / RING_POINTS;
            int pointX = centerX + (int) Math.round(Math.sin(angle) * RADIUS);
            int pointY = centerY - (int) Math.round(Math.cos(angle) * RADIUS);
            int sector = WeaponSkillLoadoutRules.wheelIndex(this.skills.size(), pointX - centerX, pointY - centerY, 0);
            graphics.fill(pointX - 2, pointY - 2, pointX + 2, pointY + 2, sector == this.choice ? 0xFFFFCF4A : 0xFF625568);
        }
        for (int index = 0; index < this.skills.size(); index++) {
            double angle = index * Math.PI * 2.0 / this.skills.size();
            int nodeX = centerX + (int) Math.round(Math.sin(angle) * RADIUS);
            int nodeY = centerY - (int) Math.round(Math.cos(angle) * RADIUS);
            graphics.fill(nodeX - 16, nodeY - 11, nodeX + 16, nodeY + 11, index == this.choice ? 0xEF7B5A28 : 0xEF29232E);
            graphics.drawCenteredString(this.font, "#" + (this.skills.get(index).gourmetSkillIndex() + 1), nodeX, nodeY - 4,
                    index == this.choice ? 0xFFFFE4A3 : 0xFFE8E1D1);
        }
        if (!this.skills.isEmpty()) {
            WeaponDetailView selected = this.skills.get(this.choice);
            drawCenteredLines(graphics, selected.title(), centerX, centerY - 18, 145, 0xFFFFD77A, 3);
            drawCenteredLines(graphics, selected.description(), centerX, centerY + 18, 145, 0xFFC8B7E8, 3);
            if (hovered >= 0) graphics.renderTooltip(this.font, List.of(selected.title(), selected.description()), Optional.empty(), mouseX, mouseY);
        }
        drawCenteredLines(graphics, Component.translatable("screen.ftboss.skills.wheel_hint", keyName()), centerX,
                centerY + 106, Math.min(320, this.width - 24), 0xFFB8B0BF, 2);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawCenteredLines(GuiGraphics graphics, Component text, int centerX, int startY, int width, int color, int limit) {
        List<FormattedCharSequence> lines = this.font.split(text, width);
        for (int index = 0; index < Math.min(limit, lines.size()); index++) {
            FormattedCharSequence line = lines.get(index);
            graphics.drawString(this.font, line, centerX - this.font.width(line) / 2, startY + index * 10, color, false);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!this.skills.isEmpty() && delta != 0) {
            this.lastMouseX = (int) mouseX;
            this.lastMouseY = (int) mouseY;
            this.choice = Math.floorMod(this.choice + (delta < 0 ? 1 : -1), this.skills.size());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { confirm(); return true; }
        if (button == 1) { this.onClose(); return true; }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        int carried = FoodWeaponSkillLoadout.carriedSkillCount(event.getItemStack());
        if (carried > 0) event.getToolTip().add(Component.translatable("tooltip.ftboss.skills.carried", carried, keyName())
                .withStyle(ChatFormatting.GOLD));
    }

    @Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent event) { event.register(WHEEL); }
    }
}
