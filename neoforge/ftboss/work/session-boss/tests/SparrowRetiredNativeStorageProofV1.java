import java.nio.file.*;
import java.util.*;
import net.minecraft.nbt.*;
import net.farmingtales.ftboss.boss.SparrowAppearanceData;
public final class SparrowRetiredNativeStorageProofV1 {
    private static int checks;
    private static void require(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception {
        UUID retired=UUID.fromString(args[1]);CompoundTag nativeSaved=NbtIo.readCompressed(Path.of(args[0]),NbtAccounter.unlimitedHeap());
        CompoundTag payload=nativeSaved.getCompound("data");require(payload.contains("Appearances",9),"Native SavedData schema exists after normal shutdown");
        ListTag entries=payload.getList("Appearances",10);for(int index=0;index<entries.size();index++){CompoundTag entry=entries.getCompound(index);require(entry.hasUUID("Owner"),"Original serialized owner identity");require(!entry.getUUID("Owner").equals(retired)||entry.getInt("Stage")!=0,"Actual retired stage absent from native saved data");}
        SparrowAppearanceData decoded=SparrowAppearanceData.load(payload);require(decoded.find(retired,0)==null,"Public original loader confirms actual periodic removal persisted");
        CompoundTag saved=decoded.save(new CompoundTag(),null);require(SparrowAppearanceData.load(saved).save(new CompoundTag(),null).equals(saved),"Remaining native records retain stable original serialization");
        System.out.println("checks="+checks+";nativeSavedRetiredStageAbsent=true;publicOriginalSerializationStable=true;nativeWorldRestartClaimed=false;");
    }
}
