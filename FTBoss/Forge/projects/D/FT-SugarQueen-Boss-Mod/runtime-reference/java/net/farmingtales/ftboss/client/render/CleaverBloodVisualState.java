/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.server.packs.resources.ResourceManager
 *  net.minecraft.server.packs.resources.ResourceManagerReloadListener
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RegisterClientReloadListenersEvent
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.client.render;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.render.CleaverMotionData;
import net.farmingtales.ftboss.client.render.CleaverMotionPlayback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class CleaverBloodVisualState {
    private static final int EVICT_TICKS = 200;
    private static final boolean DEBUG_FORCE = Boolean.getBoolean("ftboss.debugCleaverBlood");
    private static final Map<Long, Entry> STATES = new HashMap<Long, Entry>();
    private static CleaverMotionData data;
    private static boolean attempted;
    private static long tickCounter;
    private static ClientLevel observedLevel;

    private CleaverBloodVisualState() {
    }

    public static CleaverMotionData data() {
        if (!attempted) {
            CleaverBloodVisualState.reload(Minecraft.m_91087_().m_91098_());
        }
        return data;
    }

    private static void reload(ResourceManager manager) {
        attempted = true;
        CleaverBloodVisualState.clear();
        try (InputStreamReader animations = new InputStreamReader(manager.m_215593_(new ResourceLocation("ftboss", "animations/black_cleaver.animation.json")).m_215507_(), StandardCharsets.UTF_8);
             InputStreamReader effects = new InputStreamReader(manager.m_215593_(new ResourceLocation("ftboss", "effects/black_cleaver.effects.json")).m_215507_(), StandardCharsets.UTF_8);){
            data = new CleaverMotionData(animations, effects);
            FTBossMod.LOGGER.info("[FTBoss][\u9ed1\u5207] \u539a\u5203\u8840\u6012\u52a8\u753b\u5df2\u52a0\u8f7d\uff1a\u72ec\u7acb\u56db\u5c42\u53d1\u5149\uff0c\u5355\u4e00\u9aa8\u9abc\u59ff\u6001\u63a7\u5236");
        }
        catch (Exception exception) {
            data = null;
            FTBossMod.LOGGER.error("[FTBoss][\u9ed1\u5207] \u52a8\u753b\u8d44\u6e90\u4e0d\u5339\u914d\uff0c\u6682\u65f6\u4f7f\u7528\u95ed\u5408\u59ff\u6001", (Throwable)exception);
        }
    }

    public static long keyOf(ItemStack stack) {
        if (stack != null && !stack.m_41619_() && stack.m_41782_() && stack.m_41783_().m_128441_("GeckoLibID")) {
            return stack.m_41783_().m_128454_("GeckoLibID");
        }
        return stack == null ? 0L : Integer.toUnsignedLong(System.identityHashCode(stack));
    }

    public static void observe(ItemStack stack) {
        CleaverMotionData library = CleaverBloodVisualState.data();
        if (library == null) {
            return;
        }
        boolean flagged = DEBUG_FORCE || stack.m_41782_() && stack.m_41783_().m_128471_("ftboss_blood");
        Entry entry = STATES.computeIfAbsent(CleaverBloodVisualState.keyOf(stack), ignored -> new Entry(library, flagged));
        entry.target = flagged;
        entry.lastSeen = tickCounter;
    }

    public static CleaverMotionData.Frame frameOf(long key, float partialTick, boolean skillActive) {
        Entry entry = STATES.get(key);
        return entry == null ? CleaverBloodVisualState.data().sample("idle", 0.0) : entry.playback.sample(partialTick, skillActive);
    }

    public static double amountOf(long key, float partialTick) {
        Entry entry = STATES.get(key);
        return entry == null ? 0.0 : entry.playback.amount(partialTick);
    }

    public static void clear() {
        STATES.clear();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (observedLevel != minecraft.f_91073_) {
            CleaverBloodVisualState.clear();
            observedLevel = minecraft.f_91073_;
        }
        if (minecraft.f_91073_ == null) {
            CleaverBloodVisualState.clear();
            return;
        }
        if (minecraft.m_91104_()) {
            return;
        }
        ++tickCounter;
        Iterator<Entry> entries = STATES.values().iterator();
        while (entries.hasNext()) {
            Entry entry = entries.next();
            if (tickCounter - entry.lastSeen > 200L) {
                entries.remove();
                continue;
            }
            entry.playback.target(entry.target);
            entry.playback.tick();
        }
    }

    private static final class Entry {
        final CleaverMotionPlayback playback;
        boolean target;
        long lastSeen;

        Entry(CleaverMotionData library, boolean initialBlood) {
            this.playback = new CleaverMotionPlayback(library, initialBlood);
            this.target = initialBlood;
        }
    }

    @Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT}, bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Resources {
        @SubscribeEvent
        public static void registerReload(RegisterClientReloadListenersEvent event) {
            event.registerReloadListener((PreparableReloadListener)((ResourceManagerReloadListener)CleaverBloodVisualState::reload));
        }
    }
}

