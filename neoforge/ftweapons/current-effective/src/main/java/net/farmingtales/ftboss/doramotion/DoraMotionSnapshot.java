package net.farmingtales.ftboss.doramotion;

import net.minecraft.nbt.CompoundTag;

public final class DoraMotionSnapshot {
    private DoraMotionSnapshot() {
    }

    public static CompoundTag update(CompoundTag var0, long var1, int var3, int var4, double var5, boolean var7, double var8) {
        var3 = Math.max(0, var3);
        var4 = Math.max(0, var4);
        boolean var10 = var3 > 0 != var0.getInt("shots") > 0 || var4 > 0 != var0.getInt("reload") > 0 || var7 != var0.getBoolean("sniper");
        if (!var10 && var3 == 0 && var4 == 0 && !var7) {
            return var0;
        } else {
            long var11 = var0.contains("captureAt") ? var1 - var0.getLong("captureAt") : Long.MAX_VALUE;
            if (!var10 && var11 >= 0L && var11 < (long)(var4 > 0 ? 2 : 4)) {
                return var0;
            } else {
                CompoundTag var13 = var0.copy();
                var13.putInt("shots", var3);
                var13.putInt("reload", var4);
                var13.putBoolean("sniper", var7);
                if (var4 > 0) {
                    double var14 = Double.isFinite(var5) ? Math.max(0.0, Math.min(1.0, var5)) : 0.0;
                    double var16 = Math.max(0.0, Math.min(1.0, 1.0 - ((double)var4 - var14) / 60.0));
                    long var18 = var0.contains("reloadAt") ? var1 - var0.getLong("reloadAt") : 0L;
                    double var20 = var0.getInt("reload") > 0 && var18 > 0L ? (var16 - var0.getDouble("reloadProgress")) / (double)var18 : var8;
                    if (!Double.isFinite(var20) || var20 <= 0.0) {
                        var20 = Double.isFinite(var8) && var8 > 0.0 ? var8 : 0.016666666666666666;
                    }

                    var13.putDouble("reloadProgress", var16);
                    var13.putDouble("reloadRate", Math.min(1.0, var20));
                    var13.putLong("reloadAt", var1);
                } else if (var0.getInt("reload") > 0) {
                    var13.putDouble("reloadProgress", 1.0);
                    var13.remove("reloadAt");
                    var13.remove("reloadRate");
                }

                boolean var24 = var3 > 0 || var4 > 0 || var7;
                if (var24 || var10) {
                    var13.putLong("sampleAt", var1);
                    var13.putLong("captureAt", var1);
                }

                return var13.equals(var0) ? var0 : var13;
            }
        }
    }

    public static double reloadProgress(CompoundTag var0, long var1, float var3) {
        double var4 = (double)Math.max(0.0F, Math.min(2.0F, (float)(var1 - var0.getLong("reloadAt")) + var3));
        return Math.max(0.0, Math.min(1.0, var0.getDouble("reloadProgress") + var4 * var0.getDouble("reloadRate")));
    }

    public static boolean primary(String var0) {
        return !"offhand".equals(var0);
    }
}
