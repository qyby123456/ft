package ftboss.bossprobe;
import com.google.gson.GsonBuilder;
import io.netty.buffer.Unpooled;
import java.nio.file.*;
import java.util.*;
import net.farmingtales.ftboss.dandanmotion.MotionAccess;
import net.farmingtales.ftboss.dandanmotion.MotionServer;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.util.ManorFiveCombatRules.Skill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

public final class MotionContractChecksV3 {
    private static final List<String> checks=new ArrayList<>();
    private static final List<Map<String,Object>> rows=new ArrayList<>();
    private static final List<String> keys=List.of("dandan_motion_clip","dandan_motion_start","dandan_motion_ticks","dandan_motion_reverse","dandan_motion_schema");
    private static void require(boolean value,String name){if(!value)throw new AssertionError(name);checks.add(name);}

    private static MotionAccess access(EasterBossEntity boss){return (MotionAccess)boss;}
    private static EasterBossEntity make(ServerLevel level,int variant){EasterBossEntity boss=FTEntities.EASTER_BOSS.get().create(level);require(boss!=null,"ActualRegisteredBossFactory:"+variant);boss.setVariant(variant);boss.setNoAi(true);boss.setNoGravity(true);boss.moveTo(8.0,320.0,8.0,0.0F,0.0F);return boss;}
    private static void motion(EasterBossEntity boss,ServerLevel level,String name,String clip,int ticks,boolean reverse){
        CompoundTag tag=access(boss).dandanMotionData();require(tag.getString(keys.get(0)).equals(clip),name+":ActualMotionClip");require(tag.getLong(keys.get(1))==level.getGameTime(),name+":ActualNativeGameTime");require(tag.getInt(keys.get(2))==ticks,name+":OriginalDuration");require(tag.getBoolean(keys.get(3))==reverse,name+":OriginalReverse");require(tag.getInt(keys.get(4))==1,name+":OriginalSchema");require(tag.getString("probe_marker").equals("preserve"),name+":UnrelatedCombatDataPreserved");rows.add(Map.of("name",name,"clip",clip,"ticks",ticks,"reverse",reverse,"data",tag.toString()));
    }
    private static void carry(EasterBossEntity boss,boolean cast,boolean expected,String name){CompoundTag destination=new CompoundTag();destination.putBoolean("cast",cast);destination.putInt("destination_sentinel",17);CompoundTag before=access(boss).dandanMotionData().copy();MotionServer.carry(boss,destination);for(String key:keys)require(destination.contains(key)==expected,name+":OriginalCarryPresence:"+key);require(destination.getInt("destination_sentinel")==17,name+":DestinationPreserved");require(access(boss).dandanMotionData().equals(before),name+":SourceNotMutated");if(expected)for(String key:keys)require(destination.get(key).equals(before.get(key)),name+":OriginalCarryValue:"+key);}
    private static void candidate(ServerLevel level,EasterBossEntity boss){
        MotionAccess channel=access(boss);require(channel.dandanMotionData().isEmpty(),"ExistingChannelDefaultEmpty");CompoundTag seed=new CompoundTag();seed.putString("probe_marker","preserve");channel.dandanMotionData(seed);require(channel.dandanMotionData()==seed,"OriginalGetterSetterReferenceSemantics");
        for(int duration:new int[]{Integer.MIN_VALUE,-100,0,1,2,200,201,Integer.MAX_VALUE})for(boolean reverse:new boolean[]{false,true}){String name="Clamp:"+duration+":"+reverse;CompoundTag before=channel.dandanMotionData();MotionServer.play(boss,"probe_clip",duration,reverse);motion(boss,level,name,"probe_clip",Math.max(1,Math.min(200,duration)),reverse);require(before!=channel.dandanMotionData(),name+":PlayCopiesSharedTag");}
        for(Skill skill:new Skill[]{Skill.DANDAN_REAP,Skill.DANDAN_STAR}){String prefix=skill==Skill.DANDAN_REAP?"reap":"star";for(int age:new int[]{-2,0,skill.windup-1,skill.windup+3}){MotionServer.start(boss,skill,age);motion(boss,level,"Start:"+skill+":"+age,prefix+"_charge",Math.max(1,Math.min(200,skill.windup-age)),false);}MotionServer.pulse(boss,skill);motion(boss,level,"Pulse:"+skill,prefix.equals("reap")?"reap_cut":"star_release",skill.interval,false);MotionServer.finish(boss);motion(boss,level,"Finish:"+skill,skill==Skill.DANDAN_REAP?"settle":"star_charge",7,skill==Skill.DANDAN_STAR);}
        MotionServer.swap(boss);motion(boss,level,"Swap","swap_fold",9,false);carry(boss,false,false,"NonCastSwapNotCarried");carry(boss,true,true,"CastSwapCarried");
        MotionServer.rush(boss);motion(boss,level,"Rush","rush_cut",5,false);carry(boss,false,false,"NonCastRushNotCarried");
        MotionServer.vortex(boss);motion(boss,level,"Vortex","vortex_open",16,false);carry(boss,false,false,"NonCastVortexNotCarried");
        MotionServer.play(boss,"reap_charge",23,false);carry(boss,false,true,"ValidChargeCarried");carry(boss,true,true,"ValidCastChargeCarried");
        for(String boundary:List.of("expired","future","schema")){CompoundTag tag=channel.dandanMotionData().copy();if(boundary.equals("expired"))tag.putLong(keys.get(1),level.getGameTime()-23);if(boundary.equals("future"))tag.putLong(keys.get(1),level.getGameTime()+1);if(boundary.equals("schema"))tag.putInt(keys.get(4),2);channel.dandanMotionData(tag);carry(boss,true,false,"CarryRejects:"+boundary);MotionServer.play(boss,"reap_charge",23,false);}
        for(int variant:new int[]{0,2,3,4,5,6,7,8,9}){EasterBossEntity other=make(level,variant);CompoundTag tag=new CompoundTag();tag.putInt("control",variant);access(other).dandanMotionData(tag);MotionServer.play(other,"reap_cut",10,false);require(access(other).dandanMotionData().equals(tag),"OtherVariantRejected:"+variant);other.discard();}
        EasterBossEntity mini=make(level,1);mini.setMini(true);MotionServer.play(mini,"reap_cut",10,false);require(access(mini).dandanMotionData().isEmpty(),"MiniBossRejected");mini.discard();EasterBossEntity dead=make(level,1);dead.setHealth(0.0F);MotionServer.play(dead,"reap_cut",10,false);require(access(dead).dandanMotionData().isEmpty(),"DeadBossRejected");dead.discard();
        boss.playCast(21,7);CompoundTag shared=channel.dandanMotionData().copy();shared.putString("probe_marker","preserve");channel.dandanMotionData(shared);MotionServer.start(boss,Skill.DANDAN_REAP,0);require(channel.dandanMotionData().getBoolean("cast"),"ExistingCastDataSharedWithMotion");require(channel.dandanMotionData().getInt("duration")==21,"ExistingCastDurationPreserved");
        var values=boss.getEntityData().getNonDefaultValues();require(values!=null&&!values.isEmpty(),"ActualNativeEntityDataValuesPresent");ClientboundSetEntityDataPacket packet=new ClientboundSetEntityDataPacket(boss.getId(),values);RegistryFriendlyByteBuf buffer=new RegistryFriendlyByteBuf(Unpooled.buffer(),level.registryAccess());try{ClientboundSetEntityDataPacket.STREAM_CODEC.encode(buffer,packet);int bytes=buffer.readableBytes();ClientboundSetEntityDataPacket decoded=ClientboundSetEntityDataPacket.STREAM_CODEC.decode(buffer);require(buffer.readableBytes()==0&&decoded.id()==boss.getId(),"ActualNativeEntityDataPacketCodecRoundtrip");EasterBossEntity mirror=make(level,0);mirror.getEntityData().assignValues(decoded.packedItems());require(access(mirror).dandanMotionData().equals(channel.dandanMotionData()),"DecodedPacketAssignedToActualRegisteredMirror");require(mirror.getActionProgress(0.0F)==boss.getActionProgress(0.0F),"ExistingCastProgressPreservedAcrossPacket");rows.add(Map.of("name","NativePacketRoundtrip","bytes",bytes,"values",decoded.packedItems().size()));mirror.discard();}finally{buffer.release();}
        CompoundTag beforeClear=channel.dandanMotionData().copy();MotionServer.clear(boss);CompoundTag afterClear=channel.dandanMotionData();for(String key:keys)require(!afterClear.contains(key),"OriginalClearRemovesOnlyMotionKeys:"+key);for(String key:beforeClear.getAllKeys())if(!keys.contains(key))require(afterClear.get(key).equals(beforeClear.get(key)),"OriginalClearPreservesOtherCombatKeys:"+key);CompoundTag after=afterClear.copy();MotionServer.clear(boss);require(channel.dandanMotionData().equals(after),"OriginalClearIdempotent");
    }
    public static Map<String,Object> verify(ServerStartedEvent event){ServerLevel level=event.getServer().overworld();require(event.getServer().getPlayerList().getPlayerCount()==0,"NoAuthenticatedPlayersOrFakePlayerFixture");EasterBossEntity boss=make(level,1);require(level.addFreshEntity(boss),"ActualBossNativeAdmission");require(boss instanceof MotionAccess,"ExistingNativeMixinMotionInterfacePresent");candidate(level,boss);boss.discard();return Map.of("cases",checks,"rows",rows,"existingStage21InterfaceNotNewFeature",true);}
}
