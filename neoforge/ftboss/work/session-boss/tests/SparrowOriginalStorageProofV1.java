import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.nbt.*;
import net.farmingtales.ftboss.boss.SparrowAppearanceData;
import net.farmingtales.ftboss.util.SparrowMultiplayerRules;
public final class SparrowOriginalStorageProofV1 {
    private static int checks;
    private static void require(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception {
        JsonObject snapshot=JsonParser.parseString(Files.readString(Path.of(args[1]))).getAsJsonObject();
        UUID owner=UUID.fromString(snapshot.get("owner").getAsString());
        CompoundTag disk=NbtIo.readCompressed(Path.of(args[0]),NbtAccounter.unlimitedHeap());
        CompoundTag payload=disk.getCompound("data");require(payload.contains("Appearances",9),"Actual native saved file has original appearance list");
        SparrowAppearanceData data=SparrowAppearanceData.load(payload);
        SparrowAppearanceData.Entry entry=data.find(owner,0);require(entry!=null,"Native saved original owner and stage load");
        require(entry.owner.equals(owner)&&entry.stage==0,"Original actual owner and stage retained");
        require(entry.players==1&&entry.used==1,"Native saved original single-player quota and usage");
        require(entry.lastAppearance==snapshot.get("lastAppearance").getAsLong(),"Original actual appearance time retained");
        Set<UUID> birds=new HashSet<>();for(JsonElement bird:snapshot.getAsJsonArray("birds"))birds.add(UUID.fromString(bird.getAsString()));
        require(entry.birds.equals(birds)&&birds.size()==1,"Original actual support UUID retained after native save");
        CompoundTag saved=data.save(new CompoundTag(),null);
        SparrowAppearanceData restored=SparrowAppearanceData.load(saved);
        require(restored.save(new CompoundTag(),null).equals(saved),"Public original load/save roundtrip stable");
        require(restored.find(owner,0).birds.equals(birds),"Bird identities survive public serialization");
        for(int participants:new int[]{Integer.MIN_VALUE,-1,0,1,2,4,Integer.MAX_VALUE})require(SparrowMultiplayerRules.quota(participants)==Math.max(1,participants),"Quota lower bound and full integer range");
        require(SparrowMultiplayerRules.standaloneChance(1)==0.5,"Original solo standalone chance");
        require(SparrowMultiplayerRules.standaloneChance(2)==0.75,"Original two-player standalone chance");
        require(SparrowMultiplayerRules.additionalStageChance(1)==0.0,"No additional solo stage chance");
        require(SparrowMultiplayerRules.additionalStageChance(2)==0.25,"Original two-player additional stage chance");
        require(!SparrowMultiplayerRules.planned(null,0,true,2),"Null owner cannot gain original plan");
        require(!SparrowMultiplayerRules.planned(owner,Integer.MAX_VALUE,true,2),"Invalid stage cannot gain original plan");
        require(SparrowMultiplayerRules.planned(owner,0,true,1),"Original planned solo stage retained");
        require(!SparrowMultiplayerRules.planned(owner,0,false,1),"Solo does not manufacture extra stage");
        require(SparrowMultiplayerRules.canAppear(0,1)&&!SparrowMultiplayerRules.canAppear(1,1),"Solo appearance quota exact");
        require(SparrowMultiplayerRules.canAppear(1,2)&&!SparrowMultiplayerRules.canAppear(2,2),"Multiplayer appearance quota exact");
        require(SparrowMultiplayerRules.canAppear(-1,0)&&!SparrowMultiplayerRules.canAppear(Integer.MAX_VALUE,Integer.MAX_VALUE),"Quota clamps negatives without overflow");
        SparrowAppearanceData.Entry pure=new SparrowAppearanceData.Entry(owner,0,false);
        require(pure.players==1&&pure.used==0&&pure.lastAppearance==Long.MIN_VALUE,"Pure entry original defaults");
        require(!pure.observe(0)&&pure.observe(4)&&!pure.observe(2)&&pure.players==4,"Participant maximum cannot shrink");
        UUID bird=birds.iterator().next();require(!pure.record(null,false),"Null support identity rejected");
        require(pure.record(bird,false)&&pure.used==1&&!pure.record(bird,false)&&pure.used==1,"Same support consumes quota once");
        UUID second=new UUID(bird.getMostSignificantBits()^1,bird.getLeastSignificantBits());
        require(pure.record(second,true)&&pure.used==1,"Previously visited support records without new usage");
        SparrowAppearanceData.Entry visited=new SparrowAppearanceData.Entry(owner,0,true);require(visited.used==1,"Previously visited stage starts used");
        pure.used=Integer.MAX_VALUE;require(pure.record(new UUID(1,2),false)&&pure.used==Integer.MAX_VALUE,"Pure usage saturates");
        require(!data.onCooldown(null,0),"Missing entry has no cooldown");
        SparrowAppearanceData pureData=new SparrowAppearanceData();SparrowAppearanceData.Entry fresh=pureData.entry(owner,0,false);
        require(!pureData.onCooldown(fresh,100),"Sentinel time has no cooldown");pureData.markAppearance(fresh,100);
        require(pureData.onCooldown(fresh,139)&&!pureData.onCooldown(fresh,140),"Original forty tick cooldown exact");
        CompoundTag malformed=new CompoundTag();ListTag rows=new ListTag();CompoundTag row=new CompoundTag();row.putUUID("Owner",owner);row.putInt("Stage",0);row.putInt("Players",-5);row.putInt("Used",-8);rows.add(row);rows.add(new CompoundTag());CompoundTag invalid=row.copy();invalid.putInt("Stage",Integer.MAX_VALUE);rows.add(invalid);malformed.put("Appearances",rows);
        SparrowAppearanceData sanitized=SparrowAppearanceData.load(malformed);SparrowAppearanceData.Entry clean=sanitized.find(owner,0);
        require(clean.players==1&&clean.used==0&&clean.lastAppearance==Long.MIN_VALUE&&clean.birds.isEmpty(),"Original malformed counters and missing time clamped");
        require(sanitized.find(owner,Integer.MAX_VALUE)==null,"Invalid saved stage ignored");
        System.out.println("checks="+checks+";actualNativeSavedFileDecoded=true;publicOriginalLoadSaveRoundtrip=true;originalRuleBoundaries=true;nativeWorldRestartClaimed=false;actualMultiplayerClaimed=false;");
    }
}
