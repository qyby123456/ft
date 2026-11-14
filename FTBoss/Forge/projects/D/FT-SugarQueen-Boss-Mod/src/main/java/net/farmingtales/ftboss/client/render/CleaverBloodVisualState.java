package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.weapons.BlackCleaverItem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import software.bernie.geckolib.animatable.GeoItem;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** 黑切逐 ItemStack 血怒状态；2026-09-16 使用导出曲线，tick 推进、帧间插值，GUI 不修改手持状态。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, value = Dist.CLIENT)
public final class CleaverBloodVisualState {
    private static final int EVICT_TICKS = 200;
    private static final boolean DEBUG_FORCE = Boolean.getBoolean("ftboss.debugCleaverBlood");
    private static final Map<Long, Entry> STATES = new HashMap<>();
    private static CleaverMotionData data;
    private static boolean attempted;
    private static long tickCounter;
    private static net.minecraft.client.multiplayer.ClientLevel observedLevel;

    private CleaverBloodVisualState() {}

    private static final class Entry {
        final CleaverMotionPlayback playback;
        boolean target;
        long lastSeen;

        Entry(CleaverMotionData library, boolean initialBlood) {
            playback = new CleaverMotionPlayback(library, initialBlood);
            target = initialBlood;
        }
    }

    public static CleaverMotionData data() {
        if (!attempted) reload(Minecraft.getInstance().getResourceManager());
        return data;
    }

    private static void reload(ResourceManager manager) {
        attempted = true;
        clear();
        try (var animations = new InputStreamReader(manager.getResourceOrThrow(new ResourceLocation("ftboss", "animations/black_cleaver.animation.json")).open(), StandardCharsets.UTF_8);
             var effects = new InputStreamReader(manager.getResourceOrThrow(new ResourceLocation("ftboss", "effects/black_cleaver.effects.json")).open(), StandardCharsets.UTF_8)) {
            data = new CleaverMotionData(animations, effects);
            FTBossMod.LOGGER.info("[FTBoss][黑切] 厚刃血怒动画已加载：独立四层发光，单一骨骼姿态控制");
        } catch (Exception exception) {
            data = null;
            FTBossMod.LOGGER.error("[FTBoss][黑切] 动画资源不匹配，暂时使用闭合姿态", exception);
        }
    }

    public static long keyOf(ItemStack stack) {
        if (stack != null && !stack.isEmpty() && stack.hasTag() && stack.getTag().contains(GeoItem.ID_NBT_KEY)) {
            return stack.getTag().getLong(GeoItem.ID_NBT_KEY);
        }
        return stack == null ? 0L : Integer.toUnsignedLong(System.identityHashCode(stack));
    }

    public static void observe(ItemStack stack) {
        CleaverMotionData library = data();
        if (library == null) return;
        boolean flagged = DEBUG_FORCE || (stack.hasTag() && stack.getTag().getBoolean(BlackCleaverItem.TAG_BLOOD));
        Entry entry = STATES.computeIfAbsent(keyOf(stack), ignored -> new Entry(library, flagged));
        entry.target = flagged;
        entry.lastSeen = tickCounter;
    }

    public static CleaverMotionData.Frame frameOf(long key, float partialTick, boolean skillActive) {
        Entry entry = STATES.get(key);
        return entry == null ? data().sample("idle", 0) : entry.playback.sample(partialTick, skillActive);
    }

    public static double amountOf(long key, float partialTick) {
        Entry entry = STATES.get(key);
        return entry == null ? 0 : entry.playback.amount(partialTick);
    }

    public static void clear() {
        STATES.clear();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (observedLevel != minecraft.level) {
            clear();
            observedLevel = minecraft.level;
        }
        if (minecraft.level == null) {
            clear();
            return;
        }
        if (minecraft.isPaused()) return;
        tickCounter++;
        Iterator<Entry> entries = STATES.values().iterator();
        while (entries.hasNext()) {
            Entry entry = entries.next();
            if (tickCounter - entry.lastSeen > EVICT_TICKS) {
                entries.remove();
                continue;
            }
            entry.playback.target(entry.target);
            entry.playback.tick();
        }
    }

    @Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Resources {
        @SubscribeEvent
        public static void registerReload(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((ResourceManagerReloadListener) CleaverBloodVisualState::reload);
        }
    }
}
