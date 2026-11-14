package net.farmingtales.ftboss.skill.qizaicross;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class CompletedCue {
    private CompletedCue() {
    }

    public static void sweep(ServerLevel var0, EasterBossEntity var1, int var2, long var3, int var5, Vec3 var6, int var7, int var8, int var9) {
        if (var2 > 0 && var1.getVariant() == 11 && var1.isAlive() && !var1.isRemoved() && var0 == var1.level() && var5 > 0) {
            CompoundTag var10 = var1.getSkillCue();
            if (var2 < 3) {
                if (var10.getInt("qizai") == 7) {
                    var10.putInt("activeTicks", Math.max(1, var8));
                    var1.setQizaiCue(var10);
                }
            } else if (var10.getInt("qizai") == 0 || var10.getInt("completePhase") == 3 || var10.getInt("completePhase") == 4) {
                if (var2 == 4) {
                    var3 = var0.getGameTime() - (long)Math.max(0, 24 - var9);
                    var5 = 24;
                }

                CompoundTag var11 = new CompoundTag();
                var11.putInt("qizai", 7);
                var11.putLong("start", var3);
                var11.putInt("duration", var5);
                var11.putBoolean("released", true);
                var11.putInt("completePhase", var2);
                var11.putLong("windupStart", var3);
                var11.putInt("windupDuration", var5);
                var11.putInt("activeTicks", Math.max(1, var8));
                var11.putInt("volley", var2 == 4 ? 3 : 1);
                var11.putInt("elapsed", (int)Math.max(0L, var0.getGameTime() - var3));
                var11.putDouble("anchorX", var6.x);
                var11.putDouble("anchorY", var6.y);
                var11.putDouble("anchorZ", var6.z);
                var1.setQizaiCue(var11);
            }
        }
    }
}
