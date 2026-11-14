package net.farmingtales.ftboss.util;

import java.util.UUID;

public final class ManorCombatRules {
   private ManorCombatRules() {
   }

   public static boolean allowsDamage(boolean var0, boolean var1, boolean var2) {
      return !var0 && (var1 || var2);
   }

   public static boolean shouldThaw(boolean var0, boolean var1, boolean var2, boolean var3) {
      return (var0 || var1) && !var2 && !var3;
   }

   public static boolean canSurrender(UUID var0, UUID var1, UUID var2, UUID var3) {
      return var0 != null && var0.equals(var1) && var2 != null && var2.equals(var3);
   }

   public static boolean relocationReady(long var0, long var2, int var4) {
      return var2 == Long.MIN_VALUE || var0 < var2 || var0 - var2 >= (long)var4;
   }

   public static boolean defeated(boolean var0, int var1) {
      return var0;
   }
}
