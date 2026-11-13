package net.farmingtales.ftboss.butcherskills;

public final class ButcherSkillRules {
   public static final int FIRST_CAST_DELAY = 80;
   public static final int GLOBAL_COOLDOWN = 80;
   public static final int SUMMON_DURATION = 32;
   public static final int MAX_SUMMON_PULSES = 24;
   public static final int MAX_PARTICLES_PER_WORLD_TICK = 320;
   public static final int MAX_SUMMON_PARTICLES_PER_WORLD_TICK = 160;

   private ButcherSkillRules() {
   }

   public static boolean inCircle(double var0, double var2, double var4) {
      return var0 <= var4 * var4 && Math.abs(var2) <= 2.5;
   }

   public static boolean inCone(double var0, double var2, double var4, double var6, double var8, double var10) {
      double var12 = var0 * var0 + var2 * var2;
      if (var12 > var8 * var8) {
         return false;
      } else {
         return var12 < 1.0E-4 ? true : (var0 * var4 + var2 * var6) / Math.sqrt(var12) >= var10;
      }
   }

   public static float damage(ButcherSkillRules.Skill var0, double var1, boolean var3) {
      return Double.isFinite(var1) && !(var1 <= 0.0) ? (float)Math.min(Float.MAX_VALUE, (double)var0.baseDamage * (var1 / 5.5) * (var3 ? 1.15 : 1.0)) : 0.0F;
   }

   public static float lifesteal(float var0, float var1) {
      return Float.isFinite(var0) && Float.isFinite(var1) ? Math.max(0.0F, Math.min(var1, var0 * 0.15F)) : 0.0F;
   }

   public static double progress(long var0, int var2) {
      return Math.max(0.0, Math.min(1.0, (double)var0 / (double)var2));
   }

   public static final class ParticleBudget {
      private long tick = Long.MIN_VALUE;
      private int used;
      private int summons;

      public boolean take(long var1, boolean var3) {
         if (var1 != this.tick) {
            this.tick = var1;
            this.used = 0;
            this.summons = 0;
         }

         if (this.used < 320 && (!var3 || this.summons < 160)) {
            this.used++;
            if (var3) {
               this.summons++;
            }

            return true;
         } else {
            return false;
         }
      }
   }

   public static enum Skill {
      BLOOD_BRAND("butcher_blood_brand", 28, 12, 240, 18.0, 3.0, 16.0F),
      BLOOD_TIDE("butcher_blood_tide", 22, 12, 220, 8.0, 8.0, 14.0F),
      CLEAVER_SWEEP("butcher_cleaver_sweep", 14, 10, 160, 4.5, 4.5, 20.0F),
      BONE_CRUSH("butcher_bone_crush", 20, 14, 200, 5.0, 3.0, 28.0F);

      public final String action;
      public final int windup;
      public final int recovery;
      public final int cooldown;
      public final double castRange;
      public final double radius;
      public final float baseDamage;

      private Skill(String var3, int var4, int var5, int var6, double var7, double var9, float var11) {
         this.action = var3;
         this.windup = var4;
         this.recovery = var5;
         this.cooldown = var6;
         this.castRange = var7;
         this.radius = var9;
         this.baseDamage = var11;
      }

      public int duration() {
         return this.windup + this.recovery;
      }

      public boolean melee() {
         return this == CLEAVER_SWEEP || this == BONE_CRUSH;
      }
   }
}
