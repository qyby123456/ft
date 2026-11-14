/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Util
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.ComponentContents
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.contents.TranslatableContents
 *  net.minecraft.world.BossEvent
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RegisterGuiOverlaysEvent
 *  net.minecraftforge.client.event.RenderGuiOverlayEvent$Post
 *  net.minecraftforge.client.event.RenderGuiOverlayEvent$Pre
 *  net.minecraftforge.client.gui.overlay.VanillaGuiOverlay
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.client;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.BossBarArt;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.network.BossBarFeedPacket;
import net.farmingtales.ftboss.util.BossBarTheme;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.BossEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public final class BossBarOverlay {
    private static final long TIMEOUT_MILLIS = 3000L;
    private static final long FLASH_MILLIS = 220L;
    private static final long TINT_MILLIS = 650L;
    private static final float EASE_MILLIS = 140.0f;
    private static final long BEAT_MILLIS = 1100L;
    private static final int BAR_HEIGHT = 6;
    private static final int BASE_ROW = 17;
    private static final int CAST_ROW = 4;
    private static final int EMBLEM_ROW = 9;
    private static final int PIP_SIZE = 7;
    private static final int MIN_BAR_WIDTH = 182;
    private static final int MAX_BAR_WIDTH = 320;
    private static final int TOP_MARGIN = 12;
    private static final AtomicReference<BossBarFeedPacket> PENDING = new AtomicReference();
    private static volatile long lastReceive;
    private static volatile List<BossBarFeedPacket.Entry> current;
    private static final Map<Integer, Row> ROWS;
    private static final Map<UUID, Object> STASH;
    private static Field eventsField;
    private static boolean reflectionResolved;
    private static boolean reflectionBroken;
    private static long lastFrame;
    private static boolean failureLogged;

    private BossBarOverlay() {
    }

    public static void receive(BossBarFeedPacket packet) {
        if (packet != null) {
            PENDING.set(packet);
        }
    }

    static void onVanillaPre(RenderGuiOverlayEvent.Pre event) {
        try {
            if (event.getOverlay() == null || !VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id().equals((Object)event.getOverlay().id())) {
                return;
            }
            BossBarOverlay.syncPending();
            BossBarOverlay.restoreStash();
            if (!BossBarOverlay.hasDrawableRows()) {
                return;
            }
            if (!BossBarOverlay.removeFtbossBars()) {
                event.setCanceled(true);
            }
        }
        catch (Throwable var4) {
            reflectionBroken = true;
            try {
                BossBarOverlay.restoreStash();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
            if (BossBarOverlay.hasDrawableRows()) {
                event.setCanceled(true);
            }
            BossBarOverlay.logOnce(var4);
        }
    }

    static void onVanillaPost(RenderGuiOverlayEvent.Post event) {
        try {
            if (event.getOverlay() != null && VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id().equals((Object)event.getOverlay().id())) {
                BossBarOverlay.restoreStash();
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static boolean removeFtbossBars() {
        if (reflectionBroken) {
            return false;
        }
        Map events = BossBarOverlay.eventsRaw();
        if (events != null && !events.isEmpty()) {
            for (Object key : new ArrayList(events.keySet())) {
                BossEvent bar;
                Object value = events.get(key);
                if (!(key instanceof UUID)) continue;
                UUID id = (UUID)key;
                if (!(value instanceof BossEvent) || !BossBarOverlay.isFtbossBar((bar = (BossEvent)value).m_18861_())) continue;
                STASH.put(id, value);
            }
            Map rawEvents = events;
            for (UUID id : STASH.keySet()) {
                rawEvents.remove(id);
            }
            return true;
        }
        return true;
    }

    private static void restoreStash() {
        Map events;
        if (!STASH.isEmpty() && (events = BossBarOverlay.eventsRaw()) != null) {
            events.putAll(STASH);
            STASH.clear();
        }
    }

    private static Map eventsRaw() {
        try {
            Minecraft minecraft;
            if (!reflectionResolved) {
                Class<?> overlayClass = Class.forName("net.minecraft.client.gui.components.BossHealthOverlay");
                eventsField = overlayClass.getDeclaredField("events");
                eventsField.setAccessible(true);
                reflectionResolved = true;
            }
            if ((minecraft = Minecraft.m_91087_()) != null && minecraft.f_91065_ != null && minecraft.f_91065_.m_93090_() != null) {
                Map map;
                Object object = eventsField.get(minecraft.f_91065_.m_93090_());
                return object instanceof Map ? (map = (Map)object) : null;
            }
            return null;
        }
        catch (Throwable var3) {
            reflectionBroken = true;
            BossBarOverlay.logOnce(var3);
            return null;
        }
    }

    private static boolean isFtbossBar(Component component) {
        if (component == null) {
            return false;
        }
        try {
            if (BossBarOverlay.hasFtbossKey(component, 0)) {
                return true;
            }
            String text = component.getString();
            if (text == null || text.isEmpty()) {
                return false;
            }
            if (text.contains("\u751c\u82f9\u679c\u5e84\u56ed")) {
                return true;
            }
            for (String name : EasterBossEntity.NAMES) {
                if (name == null || name.isEmpty() || !text.contains(name)) continue;
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    private static boolean hasFtbossKey(Component component, int depth) {
        if (component != null && depth <= 6) {
            TranslatableContents contents;
            String key;
            ComponentContents componentContents = component.m_214077_();
            if (componentContents instanceof TranslatableContents && (key = (contents = (TranslatableContents)componentContents).m_237508_()) != null && (key.startsWith("entity.ftboss.") || key.startsWith("boss.ftboss.") || key.startsWith("ftboss.bar."))) {
                return true;
            }
            for (Component sibling : component.m_7360_()) {
                if (!BossBarOverlay.hasFtbossKey(sibling, depth + 1)) continue;
                return true;
            }
            return false;
        }
        return false;
    }

    private static void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.m_91087_();
        try {
            if (minecraft == null || minecraft.f_91074_ == null || minecraft.f_91073_ == null) {
                current = List.of();
                ROWS.clear();
                return;
            }
            BossBarOverlay.syncPending();
            BossBarOverlay.drawAll(graphics, minecraft);
        }
        catch (Throwable var3) {
            BossBarOverlay.logOnce(var3);
        }
    }

    private static void syncPending() {
        BossBarFeedPacket packet = PENDING.getAndSet(null);
        if (packet != null) {
            ArrayList<BossBarFeedPacket.Entry> rows = new ArrayList<BossBarFeedPacket.Entry>(packet.entries());
            rows.sort(Comparator.comparingInt(BossBarFeedPacket.Entry::entityId));
            current = List.copyOf(rows);
            lastReceive = Util.m_137550_();
        } else if (Util.m_137550_() - lastReceive > 3000L) {
            current = List.of();
        }
    }

    private static boolean hasDrawableRows() {
        for (BossBarFeedPacket.Entry entry : current) {
            if (entry == null || BossBarTheme.byId(entry.themeId()) == null) continue;
            return true;
        }
        return false;
    }

    private static void drawAll(GuiGraphics graphics, Minecraft minecraft) {
        List<BossBarFeedPacket.Entry> entries = current;
        if (entries.isEmpty()) {
            ROWS.clear();
        } else {
            long now = Util.m_137550_();
            float frameMillis = lastFrame == 0L ? 16.0f : Math.min(250.0f, Math.max(1.0f, (float)(now - lastFrame)));
            lastFrame = now;
            Font font = minecraft.f_91062_;
            int screenWidth = graphics.m_280182_();
            int screenHeight = graphics.m_280206_();
            if (screenWidth > 0 && screenHeight > 0 && font != null) {
                int width = Math.max(182, Math.min(320, screenWidth / 3));
                int x = screenWidth / 2 - width / 2;
                int y = 12;
                for (BossBarFeedPacket.Entry entry : entries) {
                    BossBarTheme theme = entry == null ? null : BossBarTheme.byId(entry.themeId());
                    if (theme == null) continue;
                    Row row = ROWS.computeIfAbsent(entry.entityId(), ignored -> new Row(BossBarTheme.clamp01(entry.ratio())));
                    row.target = BossBarTheme.clamp01(entry.ratio());
                    row.lastSeen = now;
                    boolean manor = entry.manorVariant() >= 0 && entry.manorVariants() != null && entry.manorVariants().length > 0;
                    BossBarOverlay.drawBar(graphics, font, theme, entry, row, x, y, width, now, frameMillis, manor);
                    if ((y += 17 + (entry.cast() > 0.0f ? 4 : 0) + (manor ? 9 : 0) + 3) <= screenHeight / 3) continue;
                    break;
                }
                ROWS.entrySet().removeIf(entryx -> now - ((Row)entryx.getValue()).lastSeen > 500L);
            }
        }
    }

    private static void drawBar(GuiGraphics graphics, Font font, BossBarTheme theme, BossBarFeedPacket.Entry entry, Row row, int x, int y, int width, long now, float frameMillis, boolean manor) {
        int right = x + width;
        int barTop = y + 9;
        int barBottom = barTop + 6;
        int innerLeft = x + 1;
        int innerRight = right - 1;
        int innerTop = barTop + 1;
        int innerBottom = barBottom - 1;
        int innerWidth = innerRight - innerLeft;
        if (innerWidth > 0) {
            int filled;
            float ease = Math.min(1.0f, frameMillis / 140.0f);
            row.shown += (row.target - row.shown) * ease;
            if (!Float.isNaN(row.lastTarget) && row.target < row.lastTarget - 5.0E-4f) {
                row.flashUntil = now + 220L;
            }
            row.lastTarget = row.target;
            float shown = BossBarTheme.clamp01(row.shown);
            float beat = BossBarArt.pulse(now, 1100L);
            int frame = shown < 0.25f ? BossBarArt.alpha(theme.frameColor(), 0.55f + 0.45f * beat) : theme.frameColor();
            BossBarArt.rect(graphics, x + 1, barTop + 2, right + 1, barBottom + 2, 0x66000000);
            BossBarArt.rect(graphics, x, barTop, right, barBottom, frame);
            BossBarArt.rect(graphics, innerLeft, innerTop, innerRight, innerBottom, -585690084);
            BossBarOverlay.cornerBrackets(graphics, x, barTop, right, barBottom, BossBarArt.alpha(theme.accent(), 0.92f));
            int manorTint = 0;
            if (manor) {
                int targetTint = BossBarTheme.manorVariantColor(entry.manorVariant());
                if (row.lastVariant != entry.manorVariant()) {
                    row.tintFrom = row.tint == 0 ? targetTint : row.tint;
                    row.tintStart = now;
                    row.lastVariant = entry.manorVariant();
                }
                row.tint = manorTint = BossBarArt.mix(row.tintFrom, targetTint, BossBarTheme.clamp01((float)(now - row.tintStart) / 650.0f));
                BossBarArt.glassPanes(graphics, innerLeft, innerTop, innerRight, innerBottom, entry.manorVariants(), entry.defeatedMask());
            }
            if ((filled = Math.round((float)innerWidth * shown)) > 0) {
                int fillRight = innerLeft + filled;
                if (theme.rainbowFill()) {
                    BossBarArt.rainbow(graphics, innerLeft, innerTop, fillRight, innerBottom, now);
                } else {
                    int top = theme.fillTop();
                    int bottom = theme.fillBottom();
                    if (manor) {
                        top = BossBarArt.mix(top, manorTint, 0.62f);
                        bottom = BossBarArt.mix(bottom, manorTint, 0.62f);
                    }
                    BossBarArt.gradient(graphics, innerLeft, innerTop, fillRight, innerBottom, top, bottom);
                }
                BossBarArt.rect(graphics, innerLeft, innerTop, fillRight, innerTop + 1, 0x4DFFFFFF);
                BossBarArt.rect(graphics, fillRight - 1, innerTop, fillRight, innerBottom, BossBarArt.alpha(theme.accent(), 0.85f));
                if (now < row.flashUntil) {
                    float fade = (float)(row.flashUntil - now) / 220.0f;
                    BossBarArt.rect(graphics, innerLeft, innerTop, fillRight, innerBottom, BossBarArt.alpha(-1, 0.3f * BossBarTheme.clamp01(fade)));
                }
            }
            BossBarOverlay.drawPhases(graphics, theme, entry.phase(), innerLeft, innerTop, innerRight, innerBottom);
            BossBarOverlay.drawText(graphics, font, theme, entry, x, right, y, manor);
            if (entry.cast() > 0.0f) {
                BossBarOverlay.drawCast(graphics, theme, entry.cast(), innerLeft, barBottom + 2, innerWidth, now);
            }
            if (manor) {
                int emblemY = barBottom + (entry.cast() > 0.0f ? 5 : 2);
                BossBarArt.emblems(graphics, innerLeft, emblemY, 7, entry.manorVariants(), entry.defeatedMask(), entry.manorVariant(), now);
            }
        }
    }

    private static void drawPhases(GuiGraphics graphics, BossBarTheme theme, int phase, int left, int top, int right, int bottom) {
        if (theme.hasPhaseTicks()) {
            int width = right - left;
            float low = phase >= theme.phaseCount() ? 0.0f : theme.phaseTick(phase);
            float high = phase <= 0 ? 1.0f : theme.phaseTick(phase - 1);
            BossBarArt.rect(graphics, left + Math.round((float)width * low), top, left + Math.round((float)width * high), bottom, BossBarArt.alpha(theme.accent(), 0.14f));
            for (int index = 0; index < theme.phaseCount(); ++index) {
                int marker = left + Math.round((float)width * theme.phaseTick(index));
                BossBarArt.rect(graphics, marker, top, marker + 1, bottom, BossBarArt.alpha(theme.accent(), 0.88f));
            }
        }
    }

    private static void drawText(GuiGraphics graphics, Font font, BossBarTheme theme, BossBarFeedPacket.Entry entry, int left, int right, int y, boolean manor) {
        Component badge;
        int badgeWidth;
        BossBarArt.emblem(graphics, theme.emblem(), left, y + 1, 1, theme.accent());
        MutableComponent title = Component.m_237115_((String)theme.displayKey());
        int titleX = left + 10;
        MutableComponent health = Component.m_237113_((String)("\u2764 " + entry.hp() + "/" + entry.maxHp() + "  " + Math.round(BossBarTheme.clamp01(entry.ratio()) * 100.0f) + "%"));
        int healthWidth = font.m_92852_((FormattedText)health);
        int titleLimit = Math.max(0, right - healthWidth - 6 - titleX);
        if (font.m_92852_((FormattedText)title) > titleLimit) {
            title = Component.m_237113_((String)font.m_92834_(title.getString(), titleLimit));
        }
        graphics.m_280614_(font, (Component)title, titleX, y, theme.accent(), true);
        graphics.m_280614_(font, (Component)health, right - healthWidth, y, -986896, true);
        int badgeX = titleX + font.m_92852_((FormattedText)title) + 6;
        int badgeEnd = right - healthWidth - 6;
        Iterator<Component> iterator = BossBarOverlay.badges(theme, entry, manor).iterator();
        while (iterator.hasNext() && badgeX + (badgeWidth = font.m_92852_((FormattedText)(badge = iterator.next()))) <= badgeEnd) {
            graphics.m_280614_(font, badge, badgeX, y, -5723504, true);
            badgeX += badgeWidth + 6;
        }
    }

    private static List<Component> badges(BossBarTheme theme, BossBarFeedPacket.Entry entry, boolean manor) {
        ArrayList<Component> badges = new ArrayList<Component>(4);
        String phaseName = theme.phaseNameKey(entry.phase());
        if (phaseName != null) {
            badges.add((Component)Component.m_237115_((String)phaseName));
        }
        if (entry.minions() > 0) {
            badges.add((Component)Component.m_237110_((String)"ftboss.bar.minions", (Object[])new Object[]{entry.minions()}));
        }
        if (entry.finale()) {
            badges.add((Component)Component.m_237115_((String)"ftboss.bar.finale"));
        }
        if (manor) {
            badges.add((Component)Component.m_237110_((String)"ftboss.bar.variant", (Object[])new Object[]{Component.m_237115_((String)("ui.ftboss.manor.owner." + entry.manorVariant()))}));
            badges.add((Component)Component.m_237110_((String)"ftboss.bar.emblems", (Object[])new Object[]{Integer.bitCount(entry.defeatedMask() & BossBarOverlay.manorMask(entry)), entry.manorCount()}));
        }
        return badges;
    }

    private static int manorMask(BossBarFeedPacket.Entry entry) {
        int mask = 0;
        for (byte variant : entry.manorVariants()) {
            mask |= 1 << (variant & 0xFF);
        }
        return mask;
    }

    private static void drawCast(GuiGraphics graphics, BossBarTheme theme, float cast, int x, int y, int width, long now) {
        BossBarArt.rect(graphics, x, y, x + width, y + 2, -1072623596);
        int filled = Math.round((float)width * BossBarTheme.clamp01(cast));
        float pulse = 0.55f + 0.45f * BossBarArt.pulse(now, 420L);
        if (filled > 0) {
            BossBarArt.rect(graphics, x, y, x + filled, y + 2, BossBarArt.alpha(theme.accent(), pulse));
            BossBarArt.rect(graphics, x + filled - 1, y, x + filled, y + 2, -1);
        }
    }

    private static void cornerBrackets(GuiGraphics graphics, int left, int top, int right, int bottom, int color) {
        int length = 3;
        BossBarArt.rect(graphics, left, top, left + length, top + 1, color);
        BossBarArt.rect(graphics, left, top, left + 1, top + length, color);
        BossBarArt.rect(graphics, right - length, top, right, top + 1, color);
        BossBarArt.rect(graphics, right - 1, top, right, top + length, color);
        BossBarArt.rect(graphics, left, bottom - 1, left + length, bottom, color);
        BossBarArt.rect(graphics, left, bottom - length, left + 1, bottom, color);
        BossBarArt.rect(graphics, right - length, bottom - 1, right, bottom, color);
        BossBarArt.rect(graphics, right - 1, bottom - length, right, bottom, color);
    }

    private static void logOnce(Throwable throwable) {
        if (!failureLogged) {
            failureLogged = true;
            FTBossMod.LOGGER.warn("[FTBoss][\u8840\u6761] \u4e3b\u9898\u8840\u6761\u7ed8\u5236\u5f02\u5e38\uff0c\u5df2\u964d\u7ea7\u4e3a\u539f\u7248\u663e\u793a: {}", (Object)throwable.toString());
        }
    }

    static {
        current = List.of();
        ROWS = new HashMap<Integer, Row>();
        STASH = new LinkedHashMap<UUID, Object>();
    }

    private static final class Row {
        float shown;
        float target;
        float lastTarget = Float.NaN;
        long flashUntil;
        long lastSeen;
        int lastVariant = Integer.MIN_VALUE;
        int tintFrom;
        int tint;
        long tintStart;

        Row(float ratio) {
            this.shown = this.target = ratio;
        }
    }

    @Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
    public static final class Registration {
        private Registration() {
        }

        @SubscribeEvent
        public static void register(RegisterGuiOverlaysEvent event) {
            block2: {
                try {
                    event.registerAbove(VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id(), "themed_boss_bars", (gui, graphics, partialTick, width, height) -> BossBarOverlay.render(graphics));
                }
                catch (IllegalArgumentException duplicate) {
                    String message = duplicate.getMessage();
                    if (message != null && message.startsWith("Overlay already registered:") && message.contains("themed_boss_bars")) break block2;
                    throw duplicate;
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
    public static final class ForgeEvents {
        private ForgeEvents() {
        }

        @SubscribeEvent
        public static void pre(RenderGuiOverlayEvent.Pre event) {
            BossBarOverlay.onVanillaPre(event);
        }

        @SubscribeEvent
        public static void post(RenderGuiOverlayEvent.Post event) {
            BossBarOverlay.onVanillaPost(event);
        }
    }
}

