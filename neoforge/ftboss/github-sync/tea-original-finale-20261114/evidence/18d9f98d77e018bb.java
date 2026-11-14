package net.farmingtales.ftboss.util;

public final class SayaFleshVisionRules {
   public static final int TEXTURE_WIDTH = 1024;
   public static final int TEXTURE_HEIGHT = 576;
   public static final int PERIOD_TICKS = 36;
   public static final int MAX_ARMOR_PIECES = 4;

   private SayaFleshVisionRules() {
   }

   public static int clampPieces(int var0) {
      return Math.max(0, Math.min(4, var0));
   }

   public static float intensity(int var0) {
      return (float)clampPieces(var0) / 4.0F;
   }

   public static int bloodTint(int var0) {
      int var1 = Math.round(12.0F * intensity(var0));
      return var1 << 24 | 6821414;
   }

   public static double heartbeat(double var0) {
      if (!Double.isFinite(var0)) {
         return 0.0;
      } else {
         double var2 = (var0 % 36.0 + 36.0) % 36.0 / 36.0;
         double var4 = (var2 - 0.23) / 0.075;
         double var6 = (var2 - 0.43) / 0.09;
         return Math.min(1.0, Math.exp(-var4 * var4) + 0.42 * Math.exp(-var6 * var6));
      }
   }

   public static float fleshAlpha(double var0, int var2) {
      float var3 = intensity(var2);
      return var3 <= 0.0F ? 0.0F : (float)(0.14 + 0.79 * (double)var3 + 0.05 * (double)var3 * heartbeat(var0));
   }

   public static float veinAlpha(double var0, int var2) {
      float var3 = intensity(var2);
      return var3 <= 0.0F ? 0.0F : (float)(0.06 + 0.52 * (double)var3 + 0.18 * (double)var3 * heartbeat(var0));
   }

   public static float fleshAlpha(double var0) {
      return fleshAlpha(var0, 4);
   }

   public static float veinAlpha(double var0) {
      return veinAlpha(var0, 4);
   }
}
