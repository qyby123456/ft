import java.nio.file.*;
import java.util.*;
import net.minecraft.nbt.*;
public final class EarnedNormalVictorySavedProofV1 {
    private static int checks;
    private static void require(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] arguments)throws Exception{
        UUID player=UUID.fromString(arguments[1]);CompoundTag root=NbtIo.readCompressed(Path.of(arguments[0]),NbtAccounter.unlimitedHeap());CompoundTag payload=root.getCompound("data");require(payload.contains("Conquerors",9),"Actual normal victory native saved conqueror list exists");ListTag entries=payload.getList("Conquerors",10);int matches=0;for(int index=0;index<entries.size();index++){CompoundTag entry=entries.getCompound(index);require(entry.hasUUID("Id"),"Actual native conqueror row retains original UUID schema");if(entry.getUUID("Id").equals(player))matches++;}require(matches==1,"Actual native normal victory saved real player conquest exactly once");System.out.println("checks="+checks+";actualNativeNormalConquestPlayerSavedOnce=true;worldInputReadOnly=true;victoryOrUnlockWritesByProof=false;");
    }
}
