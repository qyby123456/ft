package net.farmingtales.ftboss.boss.ai;

public final class GongdiGuillotineCooldown {
   public static final int COOLDOWN_TICKS = 600;
   public static final int RESET_WINDOW_TICKS = 200;
   private long resetExpiresAt;
   private boolean resetAvailable;

   public static long eligibleAt(long var0, GongdiGuillotineCooldown var2, long var3) {
      return var3;
   }

   public static long triggered(long var0, GongdiGuillotineCooldown var2) {
      var2.clear();
      return 0L;
   }

   public void grant(long var1) {
      this.resetExpiresAt = var1 + 200L;
      this.resetAvailable = true;
   }

   public void clear() {
      this.resetAvailable = false;
      this.resetExpiresAt = 0L;
   }
}
