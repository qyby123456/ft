package net.farmingtales.ftboss.client;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.network.BossBarFeedPacket;
import net.farmingtales.ftboss.network.BossBarFeedPacket.Entry;
import net.farmingtales.ftboss.util.BossBarTheme;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.BossEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent.Post;
import net.minecraftforge.client.event.RenderGuiOverlayEvent.Pre;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

public final class BossBarOverlay {
   private static final AtomicReference<BossBarFeedPacket> PENDING = new AtomicReference<>();
   private static volatile long lastReceive;
   private static volatile List<Entry> current = List.of();
   private static final Map<Integer, BossBarOverlay.Row> ROWS = new HashMap<>();
   private static final Map<UUID, Object> STASH = new LinkedHashMap<>();
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

   static void onVanillaPre(Pre event) {
      try {
         if (event.getOverlay() == null || !VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id().equals(event.getOverlay().id())) {
            return;
         }

         syncPending();
         restoreStash();
         if (!hasDrawableRows()) {
            return;
         }

         if (!removeFtbossBars()) {
            event.setCanceled(true);
         }
      } catch (Throwable var4) {
         reflectionBroken = true;

         try {
            restoreStash();
         } catch (Throwable var3) {
         }

         if (hasDrawableRows()) {
            event.setCanceled(true);
         }

         logOnce(var4);
      }
   }

   static void onVanillaPost(Post event) {
      try {
         if (event.getOverlay() != null && VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id().equals(event.getOverlay().id())) {
            restoreStash();
         }
      } catch (Throwable var2) {
      }
   }

   private static boolean removeFtbossBars() {
      if (reflectionBroken) {
         return false;
      } else {
         Map<?, ?> events = eventsRaw();
         if (events != null && !events.isEmpty()) {
            for (Object key : new ArrayList(events.keySet())) {
               Object value = events.get(key);
               if (key instanceof UUID) {
                  UUID id = (UUID)key;
                  if (value instanceof BossEvent) {
                     BossEvent bar = (BossEvent)value;
                     if (isFtbossBar(bar.getName())) {
                        STASH.put(id, value);
                     }
                  }
               }
            }

            Map rawEvents = events;

            for (UUID id : STASH.keySet()) {
               rawEvents.remove(id);
            }

            return true;
         } else {
            return true;
         }
      }
   }

   private static void restoreStash() {
      if (!STASH.isEmpty()) {
         Map events = eventsRaw();
         if (events != null) {
            events.putAll(STASH);
            STASH.clear();
         }
      }
   }

   private static Map eventsRaw() {
      try {
         if (!reflectionResolved) {
            Class<?> overlayClass = Class.forName("net.minecraft.client.gui.components.BossHealthOverlay");
            eventsField = net.minecraftforge.fml.util.ObfuscationReflectionHelper.findField(overlayClass, "f_93699_");
            eventsField.setAccessible(true);
            reflectionResolved = true;
         }

         Minecraft minecraft = Minecraft.getInstance();
         if (minecraft != null && minecraft.gui != null && minecraft.gui.getBossOverlay() != null) {
            return eventsField.get(minecraft.gui.getBossOverlay()) instanceof Map map ? map : null;
         } else {
            return null;
         }
      } catch (Throwable var3) {
         reflectionBroken = true;
         logOnce(var3);
         return null;
      }
   }

   private static boolean isFtbossBar(Component component) {
      if (component == null) {
         return false;
      } else {
         try {
            if (hasFtbossKey(component, 0)) {
               return true;
            }

            String text = component.getString();
            if (text == null || text.isEmpty()) {
               return false;
            }

            if (text.contains("甜苹果庄园")) {
               return true;
            }

            for (String name : EasterBossEntity.NAMES) {
               if (name != null && !name.isEmpty() && text.contains(name)) {
                  return true;
               }
            }
         } catch (Throwable var6) {
         }

         return false;
      }
   }

   private static boolean hasFtbossKey(Component component, int depth) {
      if (component != null && depth <= 6) {
         if (component.getContents() instanceof TranslatableContents contents) {
            String key = contents.getKey();
            if (key != null && (key.startsWith("entity.ftboss.") || key.startsWith("boss.ftboss.") || key.startsWith("ftboss.bar."))) {
               return true;
            }
         }

         for (Component sibling : component.getSiblings()) {
            if (hasFtbossKey(sibling, depth + 1)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static void render(GuiGraphics graphics) {
      Minecraft minecraft = Minecraft.getInstance();

      try {
         if (minecraft == null || minecraft.player == null || minecraft.level == null) {
            current = List.of();
            ROWS.clear();
            return;
         }

         syncPending();
         drawAll(graphics, minecraft);
      } catch (Throwable var3) {
         logOnce(var3);
      }
   }

   private static void syncPending() {
      BossBarFeedPacket packet = PENDING.getAndSet(null);
      if (packet != null) {
         ArrayList<Entry> rows = new ArrayList<>(packet.entries());
         rows.sort(Comparator.comparingInt(Entry::entityId));
         current = List.copyOf(rows);
         lastReceive = Util.getMillis();
      } else if (Util.getMillis() - lastReceive > 3000L) {
         current = List.of();
      }
   }

   private static boolean hasDrawableRows() {
      for (Entry entry : current) {
         if (entry != null && BossBarTheme.byId(entry.themeId()) != null) {
            return true;
         }
      }

      return false;
   }

   private static void drawAll(GuiGraphics graphics, Minecraft minecraft) {
      List<Entry> entries = current;
      if (entries.isEmpty()) {
         ROWS.clear();
      } else {
         long now = Util.getMillis();
         float frameMillis = lastFrame == 0L ? 16.0F : Math.min(250.0F, Math.max(1.0F, (float)(now - lastFrame)));
         lastFrame = now;
         Font font = minecraft.font;
         int screenWidth = graphics.guiWidth();
         int screenHeight = graphics.guiHeight();
         if (screenWidth > 0 && screenHeight > 0 && font != null) {
            int width = Math.max(182, Math.min(320, screenWidth / 3));
            int x = screenWidth / 2 - width / 2;
            int y = 12;

            for (Entry entry : entries) {
               BossBarTheme theme = entry == null ? null : BossBarTheme.byId(entry.themeId());
               if (theme != null) {
                  BossBarOverlay.Row row = ROWS.computeIfAbsent(entry.entityId(), ignored -> new BossBarOverlay.Row(BossBarTheme.clamp01(entry.ratio())));
                  row.target = BossBarTheme.clamp01(entry.ratio());
                  row.lastSeen = now;
                  boolean manor = entry.manorVariant() >= 0 && entry.manorVariants() != null && entry.manorVariants().length > 0;
                  drawBar(graphics, font, theme, entry, row, x, y, width, now, frameMillis, manor);
                  y += 22 + (entry.cast() > 0.0F ? 5 : 0) + (manor ? 9 : 0) + 3;
                  if (y > screenHeight / 3) {
                     break;
                  }
               }
            }

            ROWS.entrySet().removeIf(entryx -> now - ((BossBarOverlay.Row)entryx.getValue()).lastSeen > 500L);
         }
      }
   }

   private static void drawBar(
      GuiGraphics graphics,
      Font font,
      BossBarTheme theme,
      Entry entry,
      BossBarOverlay.Row row,
      int x,
      int y,
      int width,
      long now,
      float frameMillis,
      boolean manor
   ) {
      int right = x + width;
      int barTop = y + 9;
      int barBottom = barTop + 11;
      int innerLeft = x + 1;
      int innerRight = right - 1;
      int innerTop = barTop + 1;
      int innerBottom = barBottom - 1;
      int innerWidth = innerRight - innerLeft;
      if (innerWidth > 0) {
         float ease = Math.min(1.0F, frameMillis / 140.0F);
         row.shown = row.shown + (row.target - row.shown) * ease;
         if (!Float.isNaN(row.lastTarget) && row.target < row.lastTarget - 5.0E-4F) {
            row.flashUntil = now + 220L;
         }

         row.lastTarget = row.target;
         float shown = BossBarTheme.clamp01(row.shown);
         float beat = BossBarArt.pulse(now, 1100L);
         int frame = shown < 0.25F ? BossBarArt.alpha(theme.frameColor(), 0.55F + 0.45F * beat) : theme.frameColor();
         BossBarArt.rect(graphics, x + 1, barTop + 2, right + 1, barBottom + 2, 1711276032);
         BossBarArt.rect(graphics, x, barTop, right, barBottom, frame);
         BossBarArt.rect(graphics, innerLeft, innerTop, innerRight, innerBottom, -585690084);
         cornerBrackets(graphics, x, barTop, right, barBottom, BossBarArt.alpha(theme.accent(), 0.92F));
         int manorTint = 0;
         if (manor) {
            int targetTint = BossBarTheme.manorVariantColor(entry.manorVariant());
            if (row.lastVariant != entry.manorVariant()) {
               row.tintFrom = row.tint == 0 ? targetTint : row.tint;
               row.tintStart = now;
               row.lastVariant = entry.manorVariant();
            }

            row.tint = manorTint = BossBarArt.mix(row.tintFrom, targetTint, BossBarTheme.clamp01((float)(now - row.tintStart) / 650.0F));
            BossBarArt.glassPanes(graphics, innerLeft, innerTop, innerRight, innerBottom, entry.manorVariants(), entry.defeatedMask());
         }

         int filled = Math.round((float)innerWidth * shown);
         if (filled > 0) {
            int fillRight = innerLeft + filled;
            if (theme.rainbowFill()) {
               BossBarArt.rainbow(graphics, innerLeft, innerTop, fillRight, innerBottom, now);
            } else {
               int top = theme.fillTop();
               int bottom = theme.fillBottom();
               if (manor) {
                  top = BossBarArt.mix(top, manorTint, 0.62F);
                  bottom = BossBarArt.mix(bottom, manorTint, 0.62F);
               }

               BossBarArt.gradient(graphics, innerLeft, innerTop, fillRight, innerBottom, top, bottom);
            }

            BossBarArt.rect(graphics, innerLeft, innerTop, fillRight, innerTop + 1, 1308622847);
            BossBarArt.rect(graphics, fillRight - 1, innerTop, fillRight, innerBottom, BossBarArt.alpha(theme.accent(), 0.85F));
            if (now < row.flashUntil) {
               float fade = (float)(row.flashUntil - now) / 220.0F;
               BossBarArt.rect(graphics, innerLeft, innerTop, fillRight, innerBottom, BossBarArt.alpha(-1, 0.3F * BossBarTheme.clamp01(fade)));
            }
         }

         drawPhases(graphics, theme, entry.phase(), innerLeft, innerTop, innerRight, innerBottom);
         drawText(graphics, font, theme, entry, x, right, y, manor);
         if (entry.cast() > 0.0F) {
            drawCast(graphics, theme, entry.cast(), innerLeft, barBottom + 2, innerWidth, now);
         }

         if (manor) {
            int emblemY = barBottom + (entry.cast() > 0.0F ? 5 : 2);
            BossBarArt.emblems(graphics, innerLeft, emblemY, 7, entry.manorVariants(), entry.defeatedMask(), entry.manorVariant(), now);
         }
      }
   }

   private static void drawPhases(GuiGraphics graphics, BossBarTheme theme, int phase, int left, int top, int right, int bottom) {
      if (theme.hasPhaseTicks()) {
         int width = right - left;
         float low = phase >= theme.phaseCount() ? 0.0F : theme.phaseTick(phase);
         float high = phase <= 0 ? 1.0F : theme.phaseTick(phase - 1);
         BossBarArt.rect(
            graphics, left + Math.round((float)width * low), top, left + Math.round((float)width * high), bottom, BossBarArt.alpha(theme.accent(), 0.14F)
         );

         for (int index = 0; index < theme.phaseCount(); index++) {
            int marker = left + Math.round((float)width * theme.phaseTick(index));
            BossBarArt.rect(graphics, marker, top, marker + 1, bottom, BossBarArt.alpha(theme.accent(), 0.88F));
         }
      }
   }

   private static void drawText(GuiGraphics graphics, Font font, BossBarTheme theme, Entry entry, int left, int right, int y, boolean manor) {
      BossBarArt.emblem(graphics, theme.emblem(), left, y + 1, 1, theme.accent());
      Component title = Component.translatable(theme.displayKey());
      int titleX = left + 10;
      Component health = Component.literal("❤ " + entry.hp() + "/" + entry.maxHp() + "  " + Math.round(BossBarTheme.clamp01(entry.ratio()) * 100.0F) + "%");
      int healthWidth = font.width(health);
      int titleLimit = Math.max(0, right - healthWidth - 6 - titleX);
      if (font.width(title) > titleLimit) {
         title = Component.literal(font.plainSubstrByWidth(title.getString(), titleLimit));
      }

      graphics.drawString(font, title, titleX, y, theme.accent(), true);
      graphics.drawString(font, health, right - healthWidth, y, -986896, true);
      int badgeX = titleX + font.width(title) + 6;
      int badgeEnd = right - healthWidth - 6;

      for (Component badge : badges(theme, entry, manor)) {
         int badgeWidth = font.width(badge);
         if (badgeX + badgeWidth > badgeEnd) {
            break;
         }

         graphics.drawString(font, badge, badgeX, y, -5723504, true);
         badgeX += badgeWidth + 6;
      }
   }

   private static List<Component> badges(BossBarTheme theme, Entry entry, boolean manor) {
      ArrayList<Component> badges = new ArrayList<>(4);
      String phaseName = theme.phaseNameKey(entry.phase());
      if (phaseName != null) {
         badges.add(Component.translatable(phaseName));
      }

      if (entry.minions() > 0) {
         badges.add(Component.translatable("ftboss.bar.minions", new Object[]{entry.minions()}));
      }

      if (entry.finale()) {
         badges.add(Component.translatable("ftboss.bar.finale"));
      }

      if (manor) {
         badges.add(Component.translatable("ftboss.bar.variant", new Object[]{Component.translatable("ui.ftboss.manor.owner." + entry.manorVariant())}));
         badges.add(Component.translatable("ftboss.bar.emblems", new Object[]{Integer.bitCount(entry.defeatedMask() & manorMask(entry)), entry.manorCount()}));
      }

      return badges;
   }

   private static int manorMask(Entry entry) {
      int mask = 0;

      for (byte variant : entry.manorVariants()) {
         mask |= 1 << (variant & 255);
      }

      return mask;
   }

   private static void drawCast(GuiGraphics graphics, BossBarTheme theme, float cast, int x, int y, int width, long now) {
      BossBarArt.rect(graphics, x, y, x + width, y + 3, -1072623596);
      int filled = Math.round((float)width * BossBarTheme.clamp01(cast));
      float pulse = 0.55F + 0.45F * BossBarArt.pulse(now, 420L);
      if (filled > 0) {
         BossBarArt.rect(graphics, x, y, x + filled, y + 3, BossBarArt.alpha(theme.accent(), pulse));
         BossBarArt.rect(graphics, x + filled - 1, y, x + filled, y + 3, -1);
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
         FTBossMod.LOGGER.warn("[FTBoss][血条] 主题血条绘制异常，已降级为原版显示: {}", throwable.toString());
      }
   }

   @EventBusSubscriber(
      modid = "ftboss",
      bus = Bus.FORGE,
      value = {Dist.CLIENT}
   )
   public static final class ForgeEvents {
      private ForgeEvents() {
      }

      @SubscribeEvent
      public static void pre(Pre event) {
         BossBarOverlay.onVanillaPre(event);
      }

      @SubscribeEvent
      public static void post(Post event) {
         BossBarOverlay.onVanillaPost(event);
      }
   }

   @EventBusSubscriber(
      modid = "ftboss",
      bus = Bus.MOD,
      value = {Dist.CLIENT}
   )
   public static final class Registration {
      private Registration() {
      }

      @SubscribeEvent
      public static void register(RegisterGuiOverlaysEvent event) {
         try {
            event.registerAbove(
               VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id(), "themed_boss_bars", (gui, graphics, partialTick, width, height) -> BossBarOverlay.render(graphics)
            );
         } catch (IllegalArgumentException duplicate) {
            String message = duplicate.getMessage();
            if (message == null || !message.startsWith("Overlay already registered:") || !message.contains("themed_boss_bars")) {
               throw duplicate;
            }
         }
      }
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
}
