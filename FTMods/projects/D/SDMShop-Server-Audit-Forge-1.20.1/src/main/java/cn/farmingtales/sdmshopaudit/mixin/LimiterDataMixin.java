package cn.farmingtales.sdmshopaudit.mixin;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.sixik.sdmshoprework.common.data.LimiterData", remap = false)
public abstract class LimiterDataMixin {
    @Shadow public Map<UUID, Map<UUID, Integer>> PLAYER_ENTRY_DATA;
    @Shadow public Map<UUID, Map<UUID, Integer>> PLAYER_TAB_DATA;

    @Inject(method = "serializeNBT", at = @At("HEAD"), cancellable = true, remap = false)
    private void sdmAudit$saveLimits(CallbackInfoReturnable<CompoundTag> callback) {
        LinkedHashSet<UUID> players = new LinkedHashSet<>(PLAYER_ENTRY_DATA.keySet());
        players.addAll(PLAYER_TAB_DATA.keySet());
        ListTag records = new ListTag();
        for (UUID player : players) {
            CompoundTag record = new CompoundTag();
            record.putUUID("playerID", player);
            record.put("player_entry", sdmAudit$values(PLAYER_ENTRY_DATA.get(player)));
            record.put("player_tab", sdmAudit$values(PLAYER_TAB_DATA.get(player)));
            records.add(record);
        }
        CompoundTag result = new CompoundTag();
        result.put("players", records);
        callback.setReturnValue(result);
    }

    private static ListTag sdmAudit$values(Map<UUID, Integer> values) {
        ListTag result = new ListTag();
        if (values != null) {
            for (Map.Entry<UUID, Integer> value : values.entrySet()) {
                CompoundTag record = new CompoundTag();
                record.putUUID("id", value.getKey());
                record.putInt("count", value.getValue());
                result.add(record);
            }
        }
        return result;
    }
}
