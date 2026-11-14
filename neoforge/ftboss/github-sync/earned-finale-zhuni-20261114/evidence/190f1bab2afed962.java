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

   public static int warningTick(int strikeIndex) {
      return strikeIndex * 26 / 3;
   }

   public static int attackTicks() {
      return warningTick(59) + 10 + 1;
   }

   public static boolean ready(int uses, float health, float maximum) {
      return uses >= 0
         && uses < 2
         && Float.isFinite(health)
         && Float.isFinite(maximum)
         && health > 0.0F
         && maximum > 0.0F
         && health <= maximum * (uses == 0 ? 0.7F : 0.35F);
   }

   public static double rise(double progress) {
      double bounded = Math.max(0.0, Math.min(1.0, progress));
      return bounded * bounded * (3.0 - 2.0 * bounded);
   }
}
