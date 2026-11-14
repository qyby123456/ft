package net.farmingtales.ftboss.util;

public final class XuanlanSkyRules {
   public static final int MAX_USES = 2;
   public static final int STRIKES = 60;
   public static final int ASCEND_TICKS = 20;
   public static final int WARNING_TICKS = 10;
   public static final int STRIKE_PERIOD_TICKS = 26;
   public static final int STRIKES_PER_PERIOD = 3;
   public static final int DESCEND_TICKS = 20;
   public static final int RECOVERY_TICKS = 200;
   public static final double HEIGHT = 24.0;
   public static final double RADIUS = 3.0;
   public static final double RANGE = 64.0;
   public static final float DAMAGE = 230.0F;

   private XuanlanSkyRules() {
   }

   public static int warningTick(int var0) {
      return var0 * 26 / 3;
   }

   public static int attackTicks() {
      return warningTick(59) + 10 + 1;
   }

   public static boolean ready(int var0, float var1, float var2) {
      return var0 >= 0 && var0 < 2 && Float.isFinite(var1) && Float.isFinite(var2) && var1 > 0.0F && var2 > 0.0F && var1 <= var2 * (var0 == 0 ? 0.7F : 0.35F);
   }

   public static double rise(double var0) {
      double var2 = Math.max(0.0, Math.min(1.0, var0));
      return var2 * var2 * (3.0 - 2.0 * var2);
   }
}
