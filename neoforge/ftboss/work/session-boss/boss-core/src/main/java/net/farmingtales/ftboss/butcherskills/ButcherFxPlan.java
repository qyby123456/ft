package net.farmingtales.ftboss.butcherskills;

import java.util.ArrayList;
import java.util.List;

public final class ButcherFxPlan {
   private static final List<ButcherFxPlan.Point>[][] SPELLS = createPlans(ButcherSkillRules.Skill.values().length);
   private static final List<ButcherFxPlan.Point>[][] SUMMONS;

   private ButcherFxPlan() {
   }

   private static List<ButcherFxPlan.Point>[][] createPlans(int var0) {
      return new List[var0][33];
   }

   private static void ring(List<ButcherFxPlan.Point> var0, double var1, int var3, boolean var4) {
      for (int var5 = 0; var5 < var3; var5++) {
         double var6 = (double)var5 * Math.PI * 2.0 / (double)var3;
         var0.add(new ButcherFxPlan.Point(Math.cos(var6) * var1, 0.12, Math.sin(var6) * var1, var4));
      }
   }

   private static void cone(List<ButcherFxPlan.Point> var0, double var1, double var3, double var5, boolean var7, boolean var8) {
      for (int var9 = 0; var9 <= 16; var9++) {
         double var10 = -var3 + var3 * 2.0 * (double)var9 / 16.0;
         var0.add(new ButcherFxPlan.Point(Math.cos(var10) * var1, var5, Math.sin(var10) * var1, var7));
      }

      if (var8) {
         for (int var16 = 1; var16 <= 4; var16++) {
            for (int var13 : new int[]{-1, 1}) {
               double var14 = (double)var13 * var3;
               var0.add(new ButcherFxPlan.Point(Math.cos(var14) * var1 * (double)var16 / 5.0, 0.15, Math.sin(var14) * var1 * (double)var16 / 5.0, var7));
            }
         }
      }
   }

   private static List<ButcherFxPlan.Point> makeSpell(ButcherSkillRules.Skill var0, int var1) {
      ArrayList var2 = new ArrayList();
      boolean var3 = var1 >= var0.windup;
      if (var0 != ButcherSkillRules.Skill.CLEAVER_SWEEP && var0 != ButcherSkillRules.Skill.BLOOD_TIDE) {
         ring(var2, var0.radius, var3 ? 24 : 20, var0.melee());
         if (!var3) {
            ring(var2, 0.25 + ButcherSkillRules.progress((long)var1, var0.windup) * 0.8, 8, false);
         } else {
            for (int var8 = 0; var8 < 8; var8++) {
               double var5 = (double)var8 * Math.PI * 2.0 / 8.0;

               for (int var7 = 0; var7 < 3; var7++) {
                  var2.add(
                     new ButcherFxPlan.Point(
                        Math.cos(var5) * var0.radius * 0.7, 0.2 + (double)var7 * 0.35, Math.sin(var5) * var0.radius * 0.7, var7 == 2 || var0.melee()
                     )
                  );
               }
            }
         }
      } else {
         double var4 = var0 == ButcherSkillRules.Skill.CLEAVER_SWEEP ? Math.PI / 3 : Math.PI / 4;
         if (!var3) {
            cone(var2, var0.radius, var4, 0.15, var0.melee(), true);
         } else {
            for (int var6 = 1; var6 <= 3; var6++) {
               cone(var2, var0.radius * (double)var6 / 3.0, var4, (double)var6 * 0.3, var0.melee(), false);
            }
         }
      }

      return List.copyOf(var2);
   }

   private static List<ButcherFxPlan.Point> makeSummon(boolean var0, int var1) {
      ArrayList var2 = new ArrayList();
      double var3 = ButcherSkillRules.progress((long)var1, 32);
      double var5 = (var0 ? 2.3 : 1.0) * (0.5 + Math.sin(var3 * Math.PI) * 0.5);
      ring(var2, var5, var0 ? 20 : 12, false);
      int var7 = var0 ? 8 : 4;

      for (int var8 = 0; var8 < var7; var8++) {
         double var9 = var3 * Math.PI * 3.0 + (double)var8 * Math.PI * 2.0 / (double)var7;
         var2.add(
            new ButcherFxPlan.Point(Math.cos(var9) * var5 * (1.0 - var3 * 0.4), var3 * (var0 ? 2.2 : 1.1), Math.sin(var9) * var5 * (1.0 - var3 * 0.4), true)
         );
      }

      return List.copyOf(var2);
   }

   public static List<ButcherFxPlan.Point> spell(ButcherSkillRules.Skill var0, int var1) {
      return SPELLS[var0.ordinal()][Math.max(0, Math.min(32, var1))];
   }

   public static List<ButcherFxPlan.Point> summon(boolean var0, int var1) {
      return SUMMONS[var0 ? 1 : 0][Math.max(0, Math.min(32, var1))];
   }

   static {
      for (ButcherSkillRules.Skill var3 : ButcherSkillRules.Skill.values()) {
         for (int var4 = 0; var4 <= 32; var4++) {
            SPELLS[var3.ordinal()][var4] = makeSpell(var3, var4);
         }
      }

      SUMMONS = createPlans(2);

      for (int var5 = 0; var5 < 2; var5++) {
         for (int var6 = 0; var6 <= 32; var6++) {
            SUMMONS[var5][var6] = makeSummon(var5 == 1, var6);
         }
      }
   }

   public static record Point(double offsetX, double height, double offsetZ, boolean bone) {
   }
}
