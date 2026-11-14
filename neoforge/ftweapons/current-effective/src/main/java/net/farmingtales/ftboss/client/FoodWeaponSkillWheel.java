package net.farmingtales.ftboss.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import java.util.List;
import java.util.Optional;
import net.farmingtales.ftboss.item.FoodWeaponSkillLoadout;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.WeaponDetailView;
import net.farmingtales.ftboss.network.StatueMenuNetwork.SkillSelection;
import net.farmingtales.ftboss.util.WeaponSkillLoadoutRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(
    modid = "ftboss",
    value = {Dist.CLIENT}
)
public final class FoodWeaponSkillWheel extends Screen {
    private static final int RADIUS = 82;
    private static final int DEAD_ZONE = 24;
    private static final int RING_POINTS = 80;
    private static final KeyMapping WHEEL = new KeyMapping("key.ftboss.skill_wheel", KeyConflictContext.IN_GAME, Type.KEYSYM, 71, "key.categories.ftboss");
    private final ItemStack weapon;
    private final int hotbarSlot;
    private final List<WeaponDetailView> skills;
    private int choice;
    private int lastMouseX = Integer.MIN_VALUE;
    private int lastMouseY = Integer.MIN_VALUE;

    private FoodWeaponSkillWheel(ItemStack var1, int var2) {
        super(Component.translatable("screen.ftboss.skills.wheel"));
        this.weapon = var1.copy();
        this.hotbarSlot = var2;
        this.skills = FoodWeaponSkillLoadout.carriedSkills(var1);
        int var3 = FoodWeaponSkillLoadout.activeIndex(var1);

        for (int var4 = 0; var4 < this.skills.size(); var4++) {
            if (this.skills.get(var4).gourmetSkillIndex() == var3) {
                this.choice = var4;
            }
        }
    }

    public static Component keyName() {
        return WHEEL.getTranslatedKeyMessage();
    }

    @SubscribeEvent
    public static void openWheel(Post var0) {
        {
            boolean var1 = false;

            while (WHEEL.consumeClick()) {
                var1 = true;
            }

            Minecraft var2 = Minecraft.getInstance();
            if (var1
                && var2.screen == null
                && var2.player != null
                && var2.level != null
                && var2.getConnection() != null
                && var2.isWindowActive()
                && var2.player.isAlive()
                && !var2.player.isSpectator()
                && !var2.player.isUsingItem()
                && !var2.player.isSleeping()) {
                ItemStack var3 = var2.player.getMainHandItem();
                if (!FoodWeaponSkillLoadout.carriedSkills(var3).isEmpty()) {
                    var2.setScreen(new FoodWeaponSkillWheel(var3, var2.player.getInventory().selected));
                }
            }
        }
    }

    private boolean sameWeapon() {
        return this.minecraft != null
            && this.minecraft.player != null
            && this.minecraft.level != null
            && this.minecraft.player.isAlive()
            && !this.minecraft.player.isSpectator()
            && !this.minecraft.player.isUsingItem()
            && this.minecraft.player.getInventory().selected == this.hotbarSlot
            && ItemStack.matches(this.weapon, this.minecraft.player.getMainHandItem());
    }

    private boolean heldKey() {
        Key var1 = WHEEL.getKey();
        long var2 = this.minecraft.getWindow().getWindow();
        return var1.getType() == Type.MOUSE
            ? GLFW.glfwGetMouseButton(var2, var1.getValue()) == 1
            : var1.getType() == Type.KEYSYM && var1.getValue() >= 0 && InputConstants.isKeyDown(var2, var1.getValue());
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.sameWeapon() || !this.minecraft.isWindowActive()) {
            this.onClose();
        } else if (!this.heldKey()) {
            this.confirm();
        }
    }

    private void confirm() {
        if (this.sameWeapon() && this.choice >= 0 && this.choice < this.skills.size()) {
            int var1 = this.skills.get(this.choice).gourmetSkillIndex();
            if (var1 != FoodWeaponSkillLoadout.activeIndex(this.weapon)) {
                SkillSelection.send(this.weapon, this.hotbarSlot, var1);
            }
        }

        this.onClose();
    }

    @Override
    public void render(GuiGraphics var1, int var2, int var3, float var4) {
        this.renderBackground(var1, var2, var3, var4);
        int var5 = this.width / 2;
        int var6 = this.height / 2 - 8;
        int var7 = WeaponSkillLoadoutRules.wheelIndex(this.skills.size(), (double)(var2 - var5), (double)(var3 - var6), 24.0);
        if (var7 >= 0 && (var2 != this.lastMouseX || var3 != this.lastMouseY)) {
            this.choice = var7;
        }

        this.lastMouseX = var2;
        this.lastMouseY = var3;
        var1.drawCenteredString(this.font, this.title, var5, var6 - 112, -10374);

        for (int var8 = 0; var8 < 80; var8++) {
            double var9 = (double)var8 * Math.PI * 2.0 / 80.0;
            int var11 = var5 + (int)Math.round(Math.sin(var9) * 82.0);
            int var12 = var6 - (int)Math.round(Math.cos(var9) * 82.0);
            int var13 = WeaponSkillLoadoutRules.wheelIndex(this.skills.size(), (double)(var11 - var5), (double)(var12 - var6), 0.0);
            var1.fill(var11 - 2, var12 - 2, var11 + 2, var12 + 2, var13 == this.choice ? -12470 : -10332824);
        }

        for (int var14 = 0; var14 < this.skills.size(); var14++) {
            double var16 = (double)var14 * Math.PI * 2.0 / (double)this.skills.size();
            int var17 = var5 + (int)Math.round(Math.sin(var16) * 82.0);
            int var18 = var6 - (int)Math.round(Math.cos(var16) * 82.0);
            var1.fill(var17 - 16, var18 - 11, var17 + 16, var18 + 11, var14 == this.choice ? -277128664 : -282516690);
            var1.drawCenteredString(
                this.font, "#" + (this.skills.get(var14).gourmetSkillIndex() + 1), var17, var18 - 4, var14 == this.choice ? -7005 : -1515055
            );
        }

        if (!this.skills.isEmpty()) {
            WeaponDetailView var15 = this.skills.get(this.choice);
            this.drawCenteredLines(var1, var15.title(), var5, var6 - 18, 145, -10374, 3);
            this.drawCenteredLines(var1, var15.description(), var5, var6 + 18, 145, -3622936, 3);
            if (var7 >= 0) {
                var1.renderTooltip(this.font, List.of(var15.title(), var15.description()), Optional.empty(), var2, var3);
            }
        }

        this.drawCenteredLines(
            var1, Component.translatable("screen.ftboss.skills.wheel_hint", keyName()), var5, var6 + 106, Math.min(320, this.width - 24), -4673345, 2
        );
        super.render(var1, var2, var3, var4);
    }

    private void drawCenteredLines(GuiGraphics var1, Component var2, int var3, int var4, int var5, int var6, int var7) {
        List var8 = this.font.split(var2, var5);

        for (int var9 = 0; var9 < Math.min(var7, var8.size()); var9++) {
            FormattedCharSequence var10 = (FormattedCharSequence)var8.get(var9);
            var1.drawString(this.font, var10, var3 - this.font.width(var10) / 2, var4 + var9 * 10, var6, false);
        }
    }

    public boolean mouseScrolled(double var1, double var3, double horizontalScroll, double var5) {
        if (!this.skills.isEmpty() && var5 != 0.0) {
            this.lastMouseX = (int)var1;
            this.lastMouseY = (int)var3;
            this.choice = Math.floorMod(this.choice + (var5 < 0.0 ? 1 : -1), this.skills.size());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseClicked(double var1, double var3, int var5) {
        if (var5 == 0) {
            this.confirm();
            return true;
        } else if (var5 == 1) {
            this.onClose();
            return true;
        } else {
            return super.mouseClicked(var1, var3, var5);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent var0) {
        int var1 = FoodWeaponSkillLoadout.carriedSkills(var0.getItemStack()).size();
        if (var1 > 0) {
            var0.getToolTip().add(Component.translatable("tooltip.ftboss.skills.carried", var1, keyName()).withStyle(ChatFormatting.GOLD));
        }
    }

    @EventBusSubscriber(
        modid = "ftboss",
        value = {Dist.CLIENT},
        bus = Bus.MOD
    )
    public static final class Registration {
        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent var0) {
            var0.register(FoodWeaponSkillWheel.WHEEL);
        }
    }
}
