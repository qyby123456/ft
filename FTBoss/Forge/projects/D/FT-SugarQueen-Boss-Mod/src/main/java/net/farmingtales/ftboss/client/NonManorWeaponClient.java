package net.farmingtales.ftboss.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.skill.FTNetwork;
import net.farmingtales.ftboss.skill.NonManorWeaponPacket;
import net.farmingtales.ftboss.skill.WeaponEvolutionSkills;
import net.farmingtales.ftboss.util.NonManorWeaponRules;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

public final class NonManorWeaponClient {
    private static final KeyMapping SKILL = new KeyMapping("key.ftboss.non_manor_skill", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.categories.ftboss");

    private NonManorWeaponClient() {}

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        boolean pressed = false;
        while (SKILL.consumeClick()) pressed = true;
        if (!pressed) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null
                || !minecraft.isWindowActive() || !minecraft.player.isAlive() || minecraft.player.isSpectator()
                || minecraft.player.isSleeping() || minecraft.player.isUsingItem() || minecraft.getConnection() == null) return;
        var stack = minecraft.player.getMainHandItem();
        ResourceLocation item = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (item == null || NonManorWeaponRules.forItem(item.toString()) == null
                && !WeaponEvolutionSkills.supports(stack)) return;
        if (FTNetwork.CHANNEL.isRemotePresent(minecraft.getConnection().getConnection())) {
            FTNetwork.CHANNEL.sendToServer(new NonManorWeaponPacket(item));
        }
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        ResourceLocation item = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
        var skill = item == null ? null : NonManorWeaponRules.forItem(item.toString());
        if (skill == null) return;
        event.getToolTip().add(Component.translatable("tooltip.ftboss.non_manor_skill",
                SKILL.getTranslatedKeyMessage(), Component.translatable(skill.translationKey()),
                        net.farmingtales.ftboss.item.FoodWeaponEvolution.skillCooldown(event.getItemStack(), skill.cooldownTicks) / 20)
                .withStyle(ChatFormatting.GOLD));
        event.getToolTip().add(Component.translatable(skill.translationKey() + ".description").withStyle(ChatFormatting.GRAY));
        event.getToolTip().add(Component.translatable("tooltip.ftboss.non_manor_skill.cost",
                NonManorWeaponRules.DURABILITY_COST).withStyle(ChatFormatting.DARK_GRAY));
    }

    @Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
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
