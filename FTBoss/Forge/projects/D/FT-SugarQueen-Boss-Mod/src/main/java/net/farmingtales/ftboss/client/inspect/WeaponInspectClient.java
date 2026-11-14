package net.farmingtales.ftboss.client.inspect;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** 第一人称武器视检；2026-09-17，纯客户端，不调用 use、不改 NBT、不占用攻击/施法。 */
public final class WeaponInspectClient {
    private static final ResourceLocation MOTION = new ResourceLocation(FTBossMod.MOD_ID, "inspect/weapon_inspect.json");
    private static final KeyMapping INSPECT = new KeyMapping("key.ftboss.inspect", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N, "key.categories.ftboss");
    private static final WeaponInspectPlayback PLAYBACK = new WeaponInspectPlayback();
    private static WeaponInspectMotion library = WeaponInspectMotion.EMPTY;
    private static ItemStack inspectedStack = ItemStack.EMPTY;
    private static InteractionHand inspectedHand = InteractionHand.MAIN_HAND;
    private static LocalPlayer owner;
    private static Object inspectedLevel;
    private static int inspectedSlot;
    private static HumanoidArm inspectedArm;

    private WeaponInspectClient() {}

    private static WeaponInspectMotion.Clip clip(ItemStack stack) {
        if (stack.isEmpty()) return null;
        ResourceLocation identifier = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return identifier == null ? null : library.forItem(identifier.toString(), (name, type) -> {
            var tag = stack.getTag();
            if (tag == null) return null;
            return switch (type) {
                case "boolean" -> tag.contains(name, Tag.TAG_BYTE) ? Boolean.toString(tag.getBoolean(name)) : null;
                case "int" -> tag.contains(name, Tag.TAG_INT) ? Integer.toString(tag.getInt(name)) : null;
                case "string" -> tag.contains(name, Tag.TAG_STRING) ? tag.getString(name) : null;
                default -> null;
            };
        });
    }

    private static boolean canInspect(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        return player != null && minecraft.level != null && minecraft.screen == null && minecraft.isWindowActive()
                && minecraft.getCameraEntity() == player && minecraft.options.getCameraType().isFirstPerson()
                && !minecraft.options.hideGui && player.isAlive() && !player.isSpectator() && !player.isSleeping()
                && !player.isUsingItem() && !player.swinging && !player.isSprinting() && !player.isSwimming()
                && !player.isFallFlying() && !minecraft.options.keyAttack.isDown() && !minecraft.options.keyUse.isDown();
    }

    private static boolean stillHolding(Minecraft minecraft) {
        return minecraft.player == owner && minecraft.level == inspectedLevel && owner != null
                && owner.getInventory().selected == inspectedSlot && owner.getMainArm() == inspectedArm
                && ItemStack.isSameItemSameTags(inspectedStack, owner.getItemInHand(inspectedHand));
    }

    private static void clear() {
        PLAYBACK.clear();
        inspectedStack = ItemStack.EMPTY;
        owner = null;
        inspectedLevel = null;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        boolean pressed = false;
        while (INSPECT.consumeClick()) pressed = true;
        if (!canInspect(minecraft) || (PLAYBACK.active() && !stillHolding(minecraft))) {
            clear();
            return;
        }
        if (minecraft.isPaused()) return;
        if (pressed) {
            if (PLAYBACK.active()) {
                PLAYBACK.finish(minecraft.getFrameTime());
            } else {
                LocalPlayer player = minecraft.player;
                InteractionHand hand = clip(player.getMainHandItem()) != null ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                ItemStack stack = player.getItemInHand(hand);
                WeaponInspectMotion.Clip selected = clip(stack);
                if (selected != null) {
                    owner = player;
                    inspectedLevel = minecraft.level;
                    inspectedSlot = player.getInventory().selected;
                    inspectedArm = player.getMainArm();
                    inspectedStack = stack.copy();
                    inspectedHand = hand;
                    PLAYBACK.start(selected);
                }
            }
        }
        PLAYBACK.tick();
        if (!PLAYBACK.active()) clear();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack() || event.isUseItem() || event.isPickBlock()) clear();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void renderHand(RenderHandEvent event) {
        if (!PLAYBACK.active()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (!canInspect(minecraft) || !stillHolding(minecraft)) {
            clear();
            return;
        }
        if (event.getHand() != inspectedHand || !ItemStack.isSameItemSameTags(inspectedStack, event.getItemStack())) return;
        HumanoidArm arm = inspectedHand == InteractionHand.MAIN_HAND ? owner.getMainArm() : owner.getMainArm().getOpposite();
        WeaponInspectMotion.Pose motion = PLAYBACK.sample(event.getPartialTick());
        if (arm == HumanoidArm.LEFT) motion = motion.mirror();
        double gripX = arm == HumanoidArm.RIGHT ? 0.56 : -0.56;
        double gripY = -0.52 - event.getEquipProgress() * 0.6;
        double gripZ = -0.72;
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        try {
            pose.translate(gripX + motion.moveX(), gripY + motion.moveY(), gripZ + motion.moveZ());
            pose.mulPose(Axis.XP.rotationDegrees((float) motion.pitch()));
            pose.mulPose(Axis.YP.rotationDegrees((float) motion.yaw()));
            pose.mulPose(Axis.ZP.rotationDegrees((float) motion.roll()));
            pose.translate(-gripX, -gripY, -gripZ);
            minecraft.gameRenderer.itemInHandRenderer.renderArmWithItem(owner, event.getPartialTick(), event.getInterpolatedPitch(),
                    event.getHand(), event.getSwingProgress(), event.getItemStack(), event.getEquipProgress(),
                    pose, event.getMultiBufferSource(), event.getPackedLight());
        } finally {
            pose.popPose();
        }
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void tooltip(ItemTooltipEvent event) {
        if (clip(event.getItemStack()) != null) {
            event.getToolTip().add(Component.translatable("tooltip.ftboss.inspect", INSPECT.getTranslatedKeyMessage())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static void reload(ResourceManager resources) {
        clear();
        try (var reader = new InputStreamReader(resources.getResourceOrThrow(MOTION).open(), StandardCharsets.UTF_8)) {
            library = WeaponInspectMotion.read(reader);
            FTBossMod.LOGGER.info("[FTBoss][Inspect] Loaded {} weapon profiles for {} items", library.clips().size(), library.items().size());
        } catch (Exception exception) {
            library = WeaponInspectMotion.EMPTY;
            FTBossMod.LOGGER.error("[FTBoss][Inspect] Invalid motion resource; inspection disabled", exception);
        }
    }

    @Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
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
            event.registerReloadListener((ResourceManagerReloadListener) WeaponInspectClient::reload);
        }
    }
}
