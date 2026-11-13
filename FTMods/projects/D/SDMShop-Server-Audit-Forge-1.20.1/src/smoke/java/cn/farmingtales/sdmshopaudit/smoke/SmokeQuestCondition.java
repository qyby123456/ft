package cn.farmingtales.sdmshopaudit.smoke;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import net.minecraft.nbt.CompoundTag;
import net.sixik.sdmshoprework.api.shop.AbstractShopEntryCondition;

public final class SmokeQuestCondition extends AbstractShopEntryCondition {
    private CompoundTag data = new CompoundTag();

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public AbstractShopEntryCondition copy() {
        SmokeQuestCondition copy = new SmokeQuestCondition();
        copy.data = data.copy();
        return copy;
    }

    @Override
    public void getConfig(ConfigGroup group) {
    }

    @Override
    public CompoundTag serializeNBT() {
        return data.copy();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        data = tag.copy();
    }

    @Override
    public String getId() {
        return "ftbquestConditions";
    }

    @Override
    public String getModId() {
        return "ftbquests";
    }
}
