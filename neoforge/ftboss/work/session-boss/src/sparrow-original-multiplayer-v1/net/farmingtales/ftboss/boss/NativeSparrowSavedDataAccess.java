package net.farmingtales.ftboss.boss;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
public final class NativeSparrowSavedDataAccess {
    private NativeSparrowSavedDataAccess() {}
    public static SavedData computeIfAbsent(DimensionDataStorage storage,Function<CompoundTag,? extends SavedData> reader,Supplier<? extends SavedData> constructor,String id) {
        SavedData.Factory<SavedData> factory=new SavedData.Factory<>(()->constructor.get(),(tag,lookup)->reader.apply(tag),null);
        return storage.computeIfAbsent(factory,id);
    }
}
