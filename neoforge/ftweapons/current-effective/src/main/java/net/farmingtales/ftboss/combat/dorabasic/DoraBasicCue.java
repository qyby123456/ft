package net.farmingtales.ftboss.combat.dorabasic;

import net.minecraft.nbt.CompoundTag;

public final class DoraBasicCue {
    public static final String KEY = "doraBasic";

    private DoraBasicCue() {
    }

    public static CompoundTag stamp(CompoundTag original, int action, long tick, int variant) {
        if (variant != 2) {
            return original;
        } else {
            CompoundTag result = original.copy();
            CompoundTag cue = result.getCompound("doraBasic").copy();
            String prefix = action == 0 ? "gun" : "spear";
            cue.putLong(prefix + "Tick", tick);
            cue.putInt(prefix + "Sequence", Math.floorMod(cue.getInt(prefix + "Sequence"), 3) + 1);
            result.put("doraBasic", cue);
            return result;
        }
    }
}

