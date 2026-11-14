package net.farmingtales.ftboss.boss.ai;

public final class ButcherPhaseRules {
   public static final double BASE_HEALTH = 380.0;
   public static final int REST = 1;
   public static final int ASSAULT = 2;
   public static final int RECOVERY = 3;
   public static final int RECOVERY_DURATION = 1200;
   public static final float REGENERATION_RATIO = 0.05F;
   public static final float DODGE_CHANCE = 0.1F;
   public static final int DODGE_COOLDOWN = 20;

   private ButcherPhaseRules() {
   }

   public static int normalize(int var0) {
      return var0 >= 1 && var0 <= 3 ? var0 : 1;
   }

   public static int afterDefeat(int var0) {
      return normalize(var0) == 3 ? 0 : normalize(var0) + 1;
   }

   public static double healthMultiplier(int var0) {
      return normalize(var0) == 1 ? 1.0 : 0.5;
   }

   public static int interval(int var0) {
      return normalize(var0) == 2 ? 10 : (normalize(var0) == 3 ? 14 : 20);
   }

   public static float damageMultiplier(int var0) {
      return normalize(var0) == 2 ? 2.0F : (normalize(var0) == 3 ? 0.6666667F : 0.33333334F);
   }

   public static boolean expired(int var0, int var1) {
      return var0 == 3 && var1 >= 1200;
   }

   public static float regeneration(int var0, int var1, float var2, float var3) {
      return var0 == 3 && var1 > 0 && var1 % 20 == 0 && !expired(var0, var1) ? Math.max(0.0F, Math.min(var3, var2 * 0.05F)) : 0.0F;
   }

   public static boolean dodge(int var0, int var1, float var2) {
      return var0 == 3 && var1 <= 0 && var2 >= 0.0F && var2 < 0.1F;
   }

   public static float scaledHealth(float var0, float var1) {
      return Math.max(1.0F, Math.min(var1, var0 * var1));
   }
}
