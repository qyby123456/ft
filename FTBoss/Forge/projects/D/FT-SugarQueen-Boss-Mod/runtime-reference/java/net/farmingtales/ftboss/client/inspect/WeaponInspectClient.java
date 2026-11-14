/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.server.packs.resources.ResourceManager
 *  net.minecraft.server.packs.resources.ResourceManagerReloadListener
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.InputEvent$InteractionKeyMappingTriggered
 *  net.minecraftforge.client.event.RegisterClientReloadListenersEvent
 *  net.minecraftforge.client.event.RegisterKeyMappingsEvent
 *  net.minecraftforge.client.event.RenderHandEvent
 *  net.minecraftforge.client.settings.IKeyConflictContext
 *  net.minecraftforge.client.settings.KeyConflictContext
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.entity.player.ItemTooltipEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.client.inspect;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.inspect.PunchyInspectCompat;
import net.farmingtales.ftboss.client.inspect.WeaponInspectMotion;
import net.farmingtales.ftboss.client.inspect.WeaponInspectPlayback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class WeaponInspectClient {
    private static final ResourceLocation MOTION = new ResourceLocation("ftboss", "inspect/weapon_inspect.json");
    private static final KeyMapping INSPECT = new KeyMapping("key.ftboss.inspect", (IKeyConflictContext)KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, 78, "key.categories.ftboss");
    private static final WeaponInspectPlayback PLAYBACK = new WeaponInspectPlayback();
    private static WeaponInspectMotion library = WeaponInspectMotion.EMPTY;
    private static ItemStack inspectedStack = ItemStack.f_41583_;
    private static InteractionHand inspectedHand = InteractionHand.MAIN_HAND;
    private static LocalPlayer owner;
    private static Object inspectedLevel;
    private static int inspectedSlot;
    private static HumanoidArm inspectedArm;

    private WeaponInspectClient() {
    }

    private static WeaponInspectMotion.Clip clip(ItemStack stack) {
        if (stack.m_41619_()) {
            return null;
        }
        ResourceLocation identifier = ForgeRegistries.ITEMS.getKey((Object)stack.m_41720_());
        return identifier == null ? null : library.forItem(identifier.toString(), (name, type) -> {
            CompoundTag tag = stack.m_41783_();
            if (tag == null) {
                return null;
            }
            return switch (type) {
                case "boolean" -> {
                    if (tag.m_128425_(name, 1)) {
                        yield Boolean.toString(tag.m_128471_(name));
                    }
                    yield null;
                }
                case "int" -> {
                    if (tag.m_128425_(name, 3)) {
                        yield Integer.toString(tag.m_128451_(name));
                    }
                    yield null;
                }
                case "string" -> {
                    if (tag.m_128425_(name, 8)) {
                        yield tag.m_128461_(name);
                    }
                    yield null;
                }
                default -> null;
            };
        });
    }

    private static boolean canInspect(Minecraft minecraft) {
        LocalPlayer player = minecraft.f_91074_;
        return player != null && minecraft.f_91073_ != null && minecraft.f_91080_ == null && minecraft.m_91302_() && minecraft.m_91288_() == player && minecraft.f_91066_.m_92176_().m_90612_() && !minecraft.f_91066_.f_92062_ && player.m_6084_() && !player.m_5833_() && !player.m_5803_() && !player.m_6117_() && !player.f_20911_ && !player.m_20142_() && !player.m_6069_() && !player.m_21255_() && !minecraft.f_91066_.f_92096_.m_90857_() && !minecraft.f_91066_.f_92095_.m_90857_();
    }

    private static boolean stillHolding(Minecraft minecraft) {
        return minecraft.f_91074_ == owner && minecraft.f_91073_ == inspectedLevel && owner != null && WeaponInspectClient.owner.m_150109_().f_35977_ == inspectedSlot && owner.m_5737_() == inspectedArm && ItemStack.m_150942_((ItemStack)inspectedStack, (ItemStack)owner.m_21120_(inspectedHand));
    }

    private static void clear() {
        PLAYBACK.clear();
        inspectedStack = ItemStack.f_41583_;
        owner = null;
        inspectedLevel = null;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        TickEvent.ClientTickEvent event;
        PunchyInspectCompat.ensureInstalled(library);
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        boolean pressed = false;
        while (INSPECT.m_90859_()) {
            pressed = true;
        }
        if (!WeaponInspectClient.canInspect(minecraft) || PLAYBACK.active() && !WeaponInspectClient.stillHolding(minecraft)) {
            WeaponInspectClient.clear();
            return;
        }
        if (minecraft.m_91104_()) {
            return;
        }
        if (pressed) {
            if (PLAYBACK.active()) {
                PLAYBACK.finish(minecraft.m_91296_());
            } else {
                LocalPlayer player;
                InteractionHand hand = WeaponInspectClient.clip((player = minecraft.f_91074_).m_21205_()) != null ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                ItemStack stack = player.m_21120_(hand);
                WeaponInspectMotion.Clip selected = WeaponInspectClient.clip(stack);
                if (selected != null) {
                    owner = player;
                    inspectedLevel = minecraft.f_91073_;
                    inspectedSlot = player.m_150109_().f_35977_;
                    inspectedArm = player.m_5737_();
                    inspectedStack = stack.m_41777_();
                    inspectedHand = hand;
                    PLAYBACK.start(selected);
                }
            }
        }
        PLAYBACK.tick();
        if (!PLAYBACK.active()) {
            WeaponInspectClient.clear();
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack() || event.isUseItem() || event.isPickBlock()) {
            WeaponInspectClient.clear();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void renderHand(RenderHandEvent event) {
        if (!PLAYBACK.active()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (!WeaponInspectClient.canInspect(minecraft) || !WeaponInspectClient.stillHolding(minecraft)) {
            WeaponInspectClient.clear();
            return;
        }
        if (event.getHand() != inspectedHand || !ItemStack.m_150942_((ItemStack)inspectedStack, (ItemStack)event.getItemStack())) {
            return;
        }
        HumanoidArm arm = inspectedHand == InteractionHand.MAIN_HAND ? owner.m_5737_() : owner.m_5737_().m_20828_();
        WeaponInspectMotion.Pose motion = PLAYBACK.sample(event.getPartialTick());
        if (arm == HumanoidArm.LEFT) {
            motion = motion.mirror();
        }
        double gripX = arm == HumanoidArm.RIGHT ? 0.56 : -0.56;
        double gripY = -0.52 - (double)event.getEquipProgress() * 0.6;
        double gripZ = -0.72;
        PoseStack pose = event.getPoseStack();
        pose.m_85836_();
        try {
            pose.m_85837_(gripX + motion.moveX(), gripY + motion.moveY(), gripZ + motion.moveZ());
            pose.m_252781_(Axis.f_252529_.m_252977_((float)motion.pitch()));
            pose.m_252781_(Axis.f_252436_.m_252977_((float)motion.yaw()));
            pose.m_252781_(Axis.f_252403_.m_252977_((float)motion.roll()));
            pose.m_85837_(-gripX, -gripY, -gripZ);
            minecraft.f_91063_.f_109055_.m_109371_((AbstractClientPlayer)owner, event.getPartialTick(), event.getInterpolatedPitch(), event.getHand(), event.getSwingProgress(), event.getItemStack(), event.getEquipProgress(), pose, event.getMultiBufferSource(), event.getPackedLight());
        }
        finally {
            pose.m_85849_();
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        if (WeaponInspectClient.clip(event.getItemStack()) != null) {
            event.getToolTip().add(Component.m_237110_((String)"tooltip.ftboss.inspect", (Object[])new Object[]{INSPECT.m_90863_()}).m_130940_(ChatFormatting.DARK_GRAY));
        }
    }

    private static void reload(ResourceManager resources) {
        WeaponInspectClient.clear();
        try (InputStreamReader reader = new InputStreamReader(resources.m_215593_(MOTION).m_215507_(), StandardCharsets.UTF_8);){
            library = WeaponInspectMotion.read(reader);
            FTBossMod.LOGGER.info("[FTBoss][Inspect] Loaded {} weapon profiles for {} items", (Object)library.clips().size(), (Object)library.items().size());
        }
        catch (Exception exception) {
            library = WeaponInspectMotion.EMPTY;
            FTBossMod.LOGGER.error("[FTBoss][Inspect] Invalid motion resource; inspection disabled", (Throwable)exception);
        }
    }

    @Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT}, bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent
        public static void setup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> MinecraftForge.EVENT_BUS.register(WeaponInspectClient.class));
        }

        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent event) {
            event.register(INSPECT);
        }

        @SubscribeEvent
        public static void resources(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((PreparableReloadListener)((ResourceManagerReloadListener)WeaponInspectClient::reload));
        }
    }
}

