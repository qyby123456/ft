package net.farmingtales.ftboss.client;

import net.farmingtales.ftboss.util.BossBarTheme;
import net.minecraft.client.gui.GuiGraphics;

public final class BossBarArt {
   private BossBarArt() {
   }

   public static void rect(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
      if (graphics != null) {
         int left = Math.min(x1, x2);
         int top = Math.min(y1, y2);
         int right = Math.max(x1, x2);
         int bottom = Math.max(y1, y2);
         if (right > left && bottom > top) {
            graphics.fill(left, top, right, bottom, color);
         }
      }
   }

   public static void gradient(GuiGraphics graphics, int x1, int y1, int x2, int y2, int topColor, int bottomColor) {
      int left = Math.min(x1, x2);
      int right = Math.max(x1, x2);
      int top = Math.min(y1, y2);
      int bottom = Math.max(y1, y2);
      int height = bottom - top;
      if (graphics != null && right > left && height > 0) {
         for (int y = 0; y < height; y++) {
            float t = height <= 1 ? 0.0F : (float)y / (float)(height - 1);
            graphics.fill(left, top + y, right, top + y + 1, mix(topColor, bottomColor, t));
         }
      }
   }

   public static void rainbow(GuiGraphics graphics, int x1, int y1, int x2, int y2, long now) {
      int left = Math.min(x1, x2);
      int right = Math.max(x1, x2);
      int top = Math.min(y1, y2);
      int bottom = Math.max(y1, y2);
      int height = bottom - top;
      if (graphics != null && right > left && height > 0) {
         float base = (float)(now % 2400L) / 2400.0F;

         for (int y = 0; y < height; y++) {
            float t = height <= 1 ? 0.0F : (float)y / (float)(height - 1);
            graphics.fill(left, top + y, right, top + y + 1, hsv(base + t * 0.55F, 0.8F, 1.0F));
         }
      }
   }

   public static void emblem(GuiGraphics graphics, BossBarTheme.Emblem emblem, int x, int y, int scale, int color) {
      if (graphics != null && emblem != null) {
         int pixel = Math.max(1, scale);
         String[] rows = emblem.rows();

         for (int row = 0; row < rows.length; row++) {
            String line = rows[row];

            for (int column = 0; column < line.length(); column++) {
               if (line.charAt(column) == '#') {
                  rect(graphics, x + column * pixel, y + row * pixel, x + (column + 1) * pixel, y + (row + 1) * pixel, color);
               }
            }
         }
      }
   }

   public static void glassPanes(GuiGraphics graphics, int x1, int y1, int x2, int y2, byte[] variants, int defeatedMask) {
      if (graphics != null && variants != null && variants.length != 0) {
         int left = Math.min(x1, x2);
         int right = Math.max(x1, x2);
         int top = Math.min(y1, y2);
         int bottom = Math.max(y1, y2);
         int width = right - left;
         if (width > 0 && bottom > top) {
            for (int i = 0; i < variants.length; i++) {
               int variant = variants[i] & 255;
               int from = left + (int)Math.floor((double)width * ((double)i / (double)variants.length));
               int to = left + (int)Math.floor((double)width * ((double)(i + 1) / (double)variants.length));
               if (to <= from) {
                  to = from + 1;
               }

               boolean defeated = (defeatedMask & 1 << variant) != 0;
               int color = BossBarTheme.manorVariantColor(variant);
               rect(graphics, from, top, to, bottom, alpha(color, defeated ? 0.12F : 0.28F));
               if (defeated) {
                  rect(graphics, from, top, to, bottom, 1711276032);
               }

               rect(graphics, to - 1, top, to, bottom, 637534207);
            }
         }
      }
   }

   public static int emblems(GuiGraphics graphics, int x, int y, int size, byte[] variants, int defeatedMask, int currentVariant, long now) {
      if (graphics != null && variants != null && variants.length != 0) {
         int step = size + 2;
         float beat = pulse(now, 900L);

         for (int i = 0; i < variants.length; i++) {
            int variant = variants[i] & 255;
            int px = x + i * step;
            int color = BossBarTheme.manorVariantColor(variant);
            boolean defeated = (defeatedMask & 1 << variant) != 0;
            rect(graphics, px, y, px + size, y + size, -871297000);
            rect(graphics, px + 1, y + 1, px + size - 1, y + size - 1, alpha(color, defeated ? 0.95F : 0.3F));
            if (defeated) {
               rect(graphics, px + 2, y + size / 2, px + size - 2, y + size / 2 + 1, -570952728);
            }

            if (variant == currentVariant) {
               int glow = alpha(color, 0.4F + 0.55F * beat);
               outline(graphics, px - 1, y - 1, px + size + 1, y + size + 1, glow);
            }
         }

         return variants.length * step;
      } else {
         return 0;
      }
   }

   public static void outline(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
      rect(graphics, x1, y1, x2, y1 + 1, color);
      rect(graphics, x1, y2 - 1, x2, y2, color);
      rect(graphics, x1, y1, x1 + 1, y2, color);
      rect(graphics, x2 - 1, y1, x2, y2, color);
   }

   public static float pulse(long now, long period) {
      if (period <= 0L) {
         return 1.0F;
      } else {
         long phase = Math.floorMod(now, period);
         float half = (float)period / 2.0F;
         return BossBarTheme.clamp01((float)phase <= half ? (float)phase / half : (float)(period - phase) / half);
      }
   }

   public static int alpha(int color, float multiplier) {
      int a = color >>> 24 & 0xFF;
      int adjusted = (int)((float)a * BossBarTheme.clamp01(multiplier));
      return adjusted << 24 | color & 16777215;
   }

   public static int mix(int first, int second, float amount) {
      float t = BossBarTheme.clamp01(amount);
      int a = lerpChannel(first >>> 24, second >>> 24, t);
      int r = lerpChannel(first >>> 16 & 0xFF, second >>> 16 & 0xFF, t);
      int g = lerpChannel(first >>> 8 & 0xFF, second >>> 8 & 0xFF, t);
      int b = lerpChannel(first & 0xFF, second & 0xFF, t);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private static int lerpChannel(int a, int b, float t) {
      return Math.max(0, Math.min(255, Math.round((float)a + (float)(b - a) * t)));
   }

   public static int hsv(float hue, float saturation, float value) {
      float h = (hue - (float)Math.floor((double)hue)) * 6.0F;
      float s = BossBarTheme.clamp01(saturation);
      float v = BossBarTheme.clamp01(value);
      int sector = (int)h;
      float f = h - (float)sector;
      float p = v * (1.0F - s);
      float q = v * (1.0F - s * f);
      float t = v * (1.0F - s * (1.0F - f));
      float r;
      float g;
      float b;
      switch (sector % 6) {
         case 0:
            r = v;
            g = t;
            b = p;
            break;
         case 1:
            r = q;
            g = v;
            b = p;
            break;
         case 2:
            r = p;
            g = v;
            b = t;
            break;
         case 3:
            r = p;
            g = q;
            b = v;
            break;
         case 4:
            r = t;
            g = p;
            b = v;
            break;
         default:
            r = v;
            g = p;
            b = q;
      }

      return 0xFF000000 | channel(r) << 16 | channel(g) << 8 | channel(b);
   }

   private static int channel(float value) {
      return Math.max(0, Math.min(255, Math.round(value * 255.0F)));
   }
}
