/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants
 *  com.mojang.blaze3d.platform.InputConstants$Key
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RegisterKeyMappingsEvent
 *  net.minecraftforge.client.settings.IKeyConflictContext
 *  net.minecraftforge.client.settings.KeyConflictContext
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.entity.player.ItemTooltipEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.lwjgl.glfw.GLFW
 */
package net.farmingtales.ftboss.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import java.util.Optional;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponSkillLoadout;
import net.farmingtales.ftboss.network.StatueMenuNetwork;
import net.farmingtales.ftboss.util.WeaponSkillLoadoutRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class FoodWeaponSkillWheel
extends Screen {
    private static final int RADIUS = 82;
    private static final int DEAD_ZONE = 24;
    private static final int RING_POINTS = 80;
    private static final KeyMapping WHEEL = new KeyMapping("key.ftboss.skill_wheel", (IKeyConflictContext)KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, 71, "key.categories.ftboss");
    private final ItemStack weapon;
    private final int hotbarSlot;
    private final List<FoodWeaponEvolution.WeaponDetailView> skills;
    private int choice;
    private int lastMouseX = Integer.MIN_VALUE;
    private int lastMouseY = Integer.MIN_VALUE;

    private FoodWeaponSkillWheel(ItemStack itemStack, int n) {
        super((Component)Component.m_237115_((String)"screen.ftboss.skills.wheel"));
        this.weapon = itemStack.m_41777_();
        this.hotbarSlot = n;
        this.skills = FoodWeaponSkillLoadout.carriedSkills(itemStack);
        int n2 = FoodWeaponSkillLoadout.activeIndex(itemStack);
        for (int i = 0; i < this.skills.size(); ++i) {
            if (this.skills.get(i).gourmetSkillIndex() != n2) continue;
            this.choice = i;
        }
    }

    public static Component keyName() {
        return WHEEL.m_90863_();
    }

    @SubscribeEvent
    public static void openWheel(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        boolean bl = false;
        while (WHEEL.m_90859_()) {
            bl = true;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (!bl || minecraft.f_91080_ != null || minecraft.f_91074_ == null || minecraft.f_91073_ == null || minecraft.m_91403_() == null || !minecraft.m_91302_() || !minecraft.f_91074_.m_6084_() || minecraft.f_91074_.m_5833_() || minecraft.f_91074_.m_6117_() || minecraft.f_91074_.m_5803_()) {
            return;
        }
        ItemStack itemStack = minecraft.f_91074_.m_21205_();
        if (FoodWeaponSkillLoadout.carriedSkills(itemStack).isEmpty()) {
            return;
        }
        minecraft.m_91152_((Screen)new FoodWeaponSkillWheel(itemStack, minecraft.f_91074_.m_150109_().f_35977_));
    }

    private boolean sameWeapon() {
        return this.f_96541_ != null && this.f_96541_.f_91074_ != null && this.f_96541_.f_91073_ != null && this.f_96541_.f_91074_.m_6084_() && !this.f_96541_.f_91074_.m_5833_() && !this.f_96541_.f_91074_.m_6117_() && this.f_96541_.f_91074_.m_150109_().f_35977_ == this.hotbarSlot && ItemStack.m_41728_((ItemStack)this.weapon, (ItemStack)this.f_96541_.f_91074_.m_21205_());
    }

    private boolean heldKey() {
        InputConstants.Key key = WHEEL.getKey();
        long l = this.f_96541_.m_91268_().m_85439_();
        return key.m_84868_() == InputConstants.Type.MOUSE ? GLFW.glfwGetMouseButton((long)l, (int)key.m_84873_()) == 1 : key.m_84868_() == InputConstants.Type.KEYSYM && key.m_84873_() >= 0 && InputConstants.m_84830_((long)l, (int)key.m_84873_());
    }

    public void m_86600_() {
        super.m_86600_();
        if (!this.sameWeapon() || !this.f_96541_.m_91302_()) {
            this.m_7379_();
        } else if (!this.heldKey()) {
            this.confirm();
        }
    }

    private void confirm() {
        int n;
        if (this.sameWeapon() && this.choice >= 0 && this.choice < this.skills.size() && (n = this.skills.get(this.choice).gourmetSkillIndex()) != FoodWeaponSkillLoadout.activeIndex(this.weapon)) {
            StatueMenuNetwork.SkillSelection.send(this.weapon, this.hotbarSlot, n);
        }
        this.m_7379_();
    }

    public void m_88315_(GuiGraphics guiGraphics, int n, int n2, float f) {
        int n3;
        int n4;
        double d;
        int n5;
        this.m_280273_(guiGraphics);
        int n6 = this.f_96543_ / 2;
        int n7 = this.f_96544_ / 2 - 8;
        int n8 = WeaponSkillLoadoutRules.wheelIndex(this.skills.size(), n - n6, n2 - n7, 24.0);
        if (n8 >= 0 && (n != this.lastMouseX || n2 != this.lastMouseY)) {
            this.choice = n8;
        }
        this.lastMouseX = n;
        this.lastMouseY = n2;
        guiGraphics.m_280653_(this.f_96547_, this.f_96539_, n6, n7 - 112, -10374);
        for (n5 = 0; n5 < 80; ++n5) {
            d = (double)n5 * Math.PI * 2.0 / 80.0;
            n4 = n6 + (int)Math.round(Math.sin(d) * 82.0);
            n3 = n7 - (int)Math.round(Math.cos(d) * 82.0);
            int n9 = WeaponSkillLoadoutRules.wheelIndex(this.skills.size(), n4 - n6, n3 - n7, 0.0);
            guiGraphics.m_280509_(n4 - 2, n3 - 2, n4 + 2, n3 + 2, n9 == this.choice ? -12470 : -10332824);
        }
        for (n5 = 0; n5 < this.skills.size(); ++n5) {
            d = (double)n5 * Math.PI * 2.0 / (double)this.skills.size();
            n4 = n6 + (int)Math.round(Math.sin(d) * 82.0);
            n3 = n7 - (int)Math.round(Math.cos(d) * 82.0);
            guiGraphics.m_280509_(n4 - 16, n3 - 11, n4 + 16, n3 + 11, n5 == this.choice ? -277128664 : -282516690);
            guiGraphics.m_280137_(this.f_96547_, "#" + (this.skills.get(n5).gourmetSkillIndex() + 1), n4, n3 - 4, n5 == this.choice ? -7005 : -1515055);
        }
        if (!this.skills.isEmpty()) {
            FoodWeaponEvolution.WeaponDetailView weaponDetailView = this.skills.get(this.choice);
            this.drawCenteredLines(guiGraphics, weaponDetailView.title(), n6, n7 - 18, 145, -10374, 3);
            this.drawCenteredLines(guiGraphics, weaponDetailView.description(), n6, n7 + 18, 145, -3622936, 3);
            if (n8 >= 0) {
                guiGraphics.m_280677_(this.f_96547_, List.of(weaponDetailView.title(), weaponDetailView.description()), Optional.empty(), n, n2);
            }
        }
        this.drawCenteredLines(guiGraphics, (Component)Component.m_237110_((String)"screen.ftboss.skills.wheel_hint", (Object[])new Object[]{FoodWeaponSkillWheel.keyName()}), n6, n7 + 106, Math.min(320, this.f_96543_ - 24), -4673345, 2);
        super.m_88315_(guiGraphics, n, n2, f);
    }

    private void drawCenteredLines(GuiGraphics guiGraphics, Component component, int n, int n2, int n3, int n4, int n5) {
        List list = this.f_96547_.m_92923_((FormattedText)component, n3);
        for (int i = 0; i < Math.min(n5, list.size()); ++i) {
            FormattedCharSequence formattedCharSequence = (FormattedCharSequence)list.get(i);
            guiGraphics.m_280649_(this.f_96547_, formattedCharSequence, n - this.f_96547_.m_92724_(formattedCharSequence) / 2, n2 + i * 10, n4, false);
        }
    }

    public boolean m_6050_(double d, double d2, double d3) {
        if (!this.skills.isEmpty() && d3 != 0.0) {
            this.lastMouseX = (int)d;
            this.lastMouseY = (int)d2;
            this.choice = Math.floorMod(this.choice + (d3 < 0.0 ? 1 : -1), this.skills.size());
            return true;
        }
        return false;
    }

    public boolean m_6375_(double d, double d2, int n) {
        if (n == 0) {
            this.confirm();
            return true;
        }
        if (n == 1) {
            this.m_7379_();
            return true;
        }
        return super.m_6375_(d, d2, n);
    }

    public boolean m_7043_() {
        return false;
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent itemTooltipEvent) {
        int n = FoodWeaponSkillLoadout.carriedSkills(itemTooltipEvent.getItemStack()).size();
        if (n > 0) {
            itemTooltipEvent.getToolTip().add(Component.m_237110_((String)"tooltip.ftboss.skills.carried", (Object[])new Object[]{n, FoodWeaponSkillWheel.keyName()}).m_130940_(ChatFormatting.GOLD));
        }
    }

    @Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT}, bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent registerKeyMappingsEvent) {
            registerKeyMappingsEvent.register(WHEEL);
        }
    }
}

