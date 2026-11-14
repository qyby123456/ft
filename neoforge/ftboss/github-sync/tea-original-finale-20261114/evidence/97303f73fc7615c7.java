package net.farmingtales.ftboss.util;

public final class SayaFleshVisionRules {
   public static final int TEXTURE_WIDTH = 1024;
   public static final int TEXTURE_HEIGHT = 576;
   public static final int PERIOD_TICKS = 36;
   public static final int MAX_ARMOR_PIECES = 4;

   private SayaFleshVisionRules() {
   }

   public static int clampPieces(int pieces) {
      return Math.max(0, Math.min(4, pieces));
   }

   public static float intensity(int pieces) {
      return (float)clampPieces(pieces) / 4.0F;
   }

   public static int bloodTint(int pieces) {
      int alpha = Math.round(12.0F * intensity(pieces));
      return alpha << 24 | 6821414;
   }

   public static double heartbeat(double ticks) {
      if (!Double.isFinite(ticks)) {
         return 0.0;
      } else {
         double phase = (ticks % 36.0 + 36.0) % 36.0 / 36.0;
         double primary = (phase - 0.23) / 0.075;
         double secondary = (phase - 0.43) / 0.09;
         return Math.min(1.0, Math.exp(-primary * primary) + 0.42 * Math.exp(-secondary * secondary));
      }
   }

   public static float fleshAlpha(double ticks, int pieces) {
      float intensity = intensity(pieces);
      return intensity <= 0.0F ? 0.0F : (float)(0.14 + 0.79 * (double)intensity + 0.05 * (double)intensity * heartbeat(ticks));
   }

   public static float veinAlpha(double ticks, int pieces) {
      float intensity = intensity(pieces);
      return intensity <= 0.0F ? 0.0F : (float)(0.06 + 0.52 * (double)intensity + 0.18 * (double)intensity * heartbeat(ticks));
   }

   public static float fleshAlpha(double ticks) {
      return fleshAlpha(ticks, 4);
   }

   public static float veinAlpha(double ticks) {
      return veinAlpha(ticks, 4);
   }
}
