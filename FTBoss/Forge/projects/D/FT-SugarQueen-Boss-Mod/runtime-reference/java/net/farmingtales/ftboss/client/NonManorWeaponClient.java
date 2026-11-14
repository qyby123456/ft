/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RegisterKeyMappingsEvent
 *  net.minecraftforge.client.settings.IKeyConflictContext
 *  net.minecraftforge.client.settings.KeyConflictContext
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.entity.player.ItemTooltipEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.skill.FTNetwork;
import net.farmingtales.ftboss.skill.NonManorWeaponPacket;
import net.farmingtales.ftboss.skill.WeaponEvolutionSkills;
import net.farmingtales.ftboss.util.NonManorWeaponRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class NonManorWeaponClient {
    private static final KeyMapping SKILL = new KeyMapping("key.ftboss.non_manor_skill", (IKeyConflictContext)KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, 74, "key.categories.ftboss");

    private NonManorWeaponClient() {
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        boolean pressed = false;
        while (SKILL.m_90859_()) {
            pressed = true;
        }
        if (!pressed) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91074_ == null || minecraft.f_91073_ == null || minecraft.f_91080_ != null || !minecraft.m_91302_() || !minecraft.f_91074_.m_6084_() || minecraft.f_91074_.m_5833_() || minecraft.f_91074_.m_5803_() || minecraft.f_91074_.m_6117_() || minecraft.m_91403_() == null) {
            return;
        }
        ItemStack stack = minecraft.f_91074_.m_21205_();
        ResourceLocation item = ForgeRegistries.ITEMS.getKey((Object)stack.m_41720_());
        if (item == null || NonManorWeaponRules.forItem(item.toString()) == null && !WeaponEvolutionSkills.supports(stack)) {
            return;
        }
        if (FTNetwork.CHANNEL.isRemotePresent(minecraft.m_91403_().m_104910_())) {
            FTNetwork.CHANNEL.sendToServer((Object)new NonManorWeaponPacket(item));
        }
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        NonManorWeaponRules.Skill skill;
        ResourceLocation item = ForgeRegistries.ITEMS.getKey((Object)event.getItemStack().m_41720_());
        NonManorWeaponRules.Skill skill2 = skill = item == null ? null : NonManorWeaponRules.forItem(item.toString());
        if (skill == null) {
            return;
        }
        event.getToolTip().add(Component.m_237110_((String)"tooltip.ftboss.non_manor_skill", (Object[])new Object[]{SKILL.m_90863_(), Component.m_237115_((String)skill.translationKey()), FoodWeaponEvolution.skillCooldown(event.getItemStack(), skill.cooldownTicks) / 20}).m_130940_(ChatFormatting.GOLD));
        event.getToolTip().add(Component.m_237115_((String)(skill.translationKey() + ".description")).m_130940_(ChatFormatting.GRAY));
        event.getToolTip().add(Component.m_237110_((String)"tooltip.ftboss.non_manor_skill.cost", (Object[])new Object[]{3}).m_130940_(ChatFormatting.DARK_GRAY));
    }

    @Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT}, bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent
        public static void setup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> MinecraftForge.EVENT_BUS.register(NonManorWeaponClient.class));
        }

        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent event) {
            event.register(SKILL);
        }
    }
}

