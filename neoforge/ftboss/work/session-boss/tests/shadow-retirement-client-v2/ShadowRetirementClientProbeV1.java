package ftboss.clientfxprobe;
import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import net.farmingtales.ftboss.entity.*;
import net.farmingtales.ftboss.effect.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
@Mod(value="ftboss_clientfxprobe",dist=Dist.CLIENT)
public final class ShadowRetirementClientProbeV1 {
    private record Plan(String name,int shadowDuration,int companionDuration,int blindAmplifier,int darkAmplifier,boolean visible,boolean icon,boolean shadow,boolean apply,boolean removeNow,boolean expectedCompanion) {}
    private static final boolean candidate=System.getProperty("ftboss.shadowretire.mode","baseline").equals("candidate");
    private static final List<String> checks=new CopyOnWriteArrayList<>();
    private static final List<Map<String,Object>> helperRows=new CopyOnWriteArrayList<>(),playerRows=new CopyOnWriteArrayList<>(),cowRows=new CopyOnWriteArrayList<>();
    private static final List<Plan> plans=new ArrayList<>();
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static volatile boolean prepared,serverComplete;
    private static volatile Map<String,Boolean> clientExpected;
    private static volatile int clientExpectationIndex=-1,clientAck=-1;
    private static int callbacks,clientTicks,index,phase,waitTicks,gaps;
    private static boolean worldRequested;
    private static ServerPlayer player;
    private static ServerLevel level;
    private static Plan plan;
    public ShadowRetirementClientProbeV1(){NeoForge.EVENT_BUS.addListener(ShadowRetirementClientProbeV1::clientTick);NeoForge.EVENT_BUS.addListener(ShadowRetirementClientProbeV1::serverTick);}
    private static void require(boolean condition,String label){if(!condition)throw new AssertionError(label);checks.add(label);}
    private static Cow cow(){Cow animal=EntityType.COW.create(level);require(animal!=null,"ActualCowFactory");animal.setNoAi(true);animal.setNoGravity(true);animal.moveTo(12,70,8,0,0);require(level.addFreshEntity(animal),"ActualCowAdmission");return animal;}
    private static void helpers(){
        helperRows.addAll(EffectPolicyChecks.verify(level));
        for(int duration:new int[]{-100,-1,0,1,60,Integer.MAX_VALUE}){
            Cow target=cow();ShadowBlindEffect.applyTo(target,duration);boolean actual=target.hasEffect(MobEffects.BLINDNESS)||target.hasEffect(MobEffects.DARKNESS)||target.hasEffect(FTEffects.SHADOW_BLIND);boolean old=duration==-1||duration>0;require(actual==(candidate?false:old),"PublicRetiredBlindEntryOnActualNonPlayer:"+duration);if(actual)gaps++;
            cowRows.add(Map.of("duration",duration,"actualAnyBlindEffect",actual,"originalAnyBlindEffect",false));target.discard();
        }
        for(int duration:new int[]{0,1,59,60,100,-1})for(boolean visible:new boolean[]{false,true})for(boolean icon:new boolean[]{false,true}){
            Cow target=cow();MobEffectInstance instance=new MobEffectInstance(MobEffects.BLINDNESS,duration,1,false,visible,icon);target.addEffect(instance);require(instance.isVisible()==visible&&instance.showIcon()==icon,"MappedMobEffectVisibilityAndIconAccessors");target.discard();
        }
    }
    private static void preparePlans(){
        if(!candidate){plans.add(new Plan("baseline-active-lock",60,60,1,0,false,false,false,true,true,true));return;}
        plans.add(new Plan("matching-hidden",120,120,1,0,false,false,true,false,false,false));
        plans.add(new Plan("visible-preserved",120,120,1,0,true,false,true,false,false,true));
        plans.add(new Plan("icon-preserved",120,120,1,0,false,true,true,false,false,true));
        plans.add(new Plan("wrong-amplifiers-preserved",120,120,2,1,false,false,true,false,false,true));
        plans.add(new Plan("duration-plus2-removed",120,122,1,0,false,false,true,false,false,false));
        plans.add(new Plan("duration-minus2-removed",120,118,1,0,false,false,true,false,false,false));
        plans.add(new Plan("duration-plus3-preserved",120,123,1,0,false,false,true,false,false,true));
        plans.add(new Plan("duration-minus3-preserved",120,117,1,0,false,false,true,false,false,true));
        plans.add(new Plan("both-infinite-removed",-1,-1,1,0,false,false,true,false,false,false));
        plans.add(new Plan("finite-shadow-infinite-companion-preserved",120,-1,1,0,false,false,true,false,false,true));
        plans.add(new Plan("infinite-shadow-finite-companion-preserved",-1,120,1,0,false,false,true,false,false,true));
        plans.add(new Plan("no-shadow-unrelated-preserved",120,120,1,0,false,false,false,false,false,true));
        plans.add(new Plan("public-retire-cleans-matching",120,120,1,0,false,false,true,true,false,false));
        plans.add(new Plan("public-remove-unlocked",120,120,1,0,false,false,true,false,true,true));
        for(int duration:new int[]{-100,-1,0,1,60,Integer.MAX_VALUE})plans.add(new Plan("public-retire-no-new-effect-"+duration,duration,0,1,0,false,false,false,true,false,false));
    }
    private static void clear(){for(Holder<MobEffect> effect:List.of(FTEffects.SHADOW_BLIND,MobEffects.BLINDNESS,MobEffects.DARKNESS,MobEffects.MOVEMENT_SPEED))if(player.hasEffect(effect))require(player.removeEffect(effect),"NormalPublicFixtureEffectRemovalNoBypass");}
    private static void setup(){
        plan=plans.get(index);if(candidate)clear();else require(player.getActiveEffects().isEmpty(),"FreshBaselinePlayerNoPriorEffectSeed");
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,400,0,false,true,true));
        if(plan.shadow())require(player.addEffect(new MobEffectInstance(FTEffects.SHADOW_BLIND,plan.shadowDuration(),1,false,true,true)),"ActualRegisteredLegacyMarkerFixture");
        if(plan.companionDuration()!=0){player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,plan.companionDuration(),plan.blindAmplifier(),false,plan.visible(),plan.icon()));player.addEffect(new MobEffectInstance(MobEffects.DARKNESS,plan.companionDuration(),plan.darkAmplifier(),false,plan.visible(),plan.icon()));}
        if(plan.apply())ShadowBlindEffect.applyTo(player,plan.shadowDuration());
        if(plan.removeNow()){
            boolean removed=player.removeEffect(FTEffects.SHADOW_BLIND);require(removed==candidate,"OriginalEmptyRemoveHandlerOrBaselineCanceledRemoval");if(!removed)gaps++;
        }
        waitTicks=0;phase=1;
    }
    private static void serverTick(ServerTickEvent.Post event){if(finished.get()||!event.getServer().isSingleplayer())return;try{
        callbacks++;require(callbacks<4000,"BoundedNativeIntegratedServerPost");
        if(event.getServer().getPlayerList().getPlayerCount()!=1)return;
        if(!prepared){player=event.getServer().getPlayerList().getPlayers().getFirst();level=player.serverLevel();require(player.getGameProfile().getName().equals("ft_fx_probe")&&!(player instanceof net.neoforged.neoforge.common.util.FakePlayer),"ActualVanillaIntegratedLoginPlayerNotSyntheticServerPlayer");
            level.setChunkForced(0,0,true);level.getChunk(0,0);for(int horizontal=4;horizontal<=16;horizontal++)for(int depth=4;depth<=14;depth++)level.setBlockAndUpdate(new BlockPos(horizontal,69,depth),Blocks.STONE.defaultBlockState());
            player.teleportTo(level,8,70,6,0,0);player.setInvulnerable(true);level.setDayTime(6000);helpers();preparePlans();prepared=true;return;}
        if(phase==0){setup();return;}
        if(phase==1){if(++waitTicks<3)return;
            boolean expectedShadow=!candidate,expectedCompanion=plan.expectedCompanion();
            require(player.hasEffect(FTEffects.SHADOW_BLIND)==expectedShadow,"NaturalPlayerPostRetiresMarkerOrShowsBaselineGap:"+plan.name());
            require(player.hasEffect(MobEffects.BLINDNESS)==expectedCompanion&&player.hasEffect(MobEffects.DARKNESS)==expectedCompanion,"OriginalCompanionMatchingFilter:"+plan.name());
            require(player.hasEffect(MobEffects.MOVEMENT_SPEED),"UnrelatedVisibleEffectPreserved");
            if(!candidate){gaps++;require(player.getEffect(MobEffects.BLINDNESS).getAmplifier()==1&&player.getEffect(MobEffects.DARKNESS).getAmplifier()==0,"BaselineOriginalObsoleteBlindCompanions");}
            clientExpected=Map.of("shadow",expectedShadow,"blind",expectedCompanion,"dark",expectedCompanion,"speed",true);clientExpectationIndex=index;phase=2;waitTicks=0;return;
        }
        if(++waitTicks>180)throw new AssertionError("ActualClientEffectStateDidNotSynchronize:"+plan.name());
        if(clientAck!=index)return;
        playerRows.add(Map.of("name",plan.name(),"serverTick",player.tickCount,"clientActualStateMatched",true,"serverShadow",player.hasEffect(FTEffects.SHADOW_BLIND),"serverBlind",player.hasEffect(MobEffects.BLINDNESS),"serverDark",player.hasEffect(MobEffects.DARKNESS),"speedPreserved",player.hasEffect(MobEffects.MOVEMENT_SPEED)));
        clientExpected=null;index++;if(index==plans.size()){require(candidate?gaps==0:gaps==6,"ExactOriginalRestorationOrBaselineGapCount:"+gaps);serverComplete=true;return;}phase=0;
    }catch(Throwable failure){finish(failure);}}
    private static void clientTick(ClientTickEvent.Post event){if(finished.get())return;try{
        Minecraft client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;
        if(!worldRequested&&client.level==null&&client.getOverlay()==null&&client.screen instanceof TitleScreen){worldRequested=true;GameRules rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);LevelSettings settings=new LevelSettings("Boss original shadow retirement fixture",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);client.createWorldOpenFlows().createFreshLevel("shadow-retirement-native",settings,new WorldOptions(13262026L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);}
        if(++clientTicks>5000)throw new AssertionError("BoundedNativeClientTicks");
        if(clientTicks%100==0)Files.writeString(Path.of("client-progress.json"),new GsonBuilder().create().toJson(Map.of("clientTicks",clientTicks,"serverPrepared",prepared,"caseIndex",index,"phase",phase,"screen",client.screen==null?"none":client.screen.getClass().getName())));
        if(Files.exists(Path.of("normal-stop-request.json")))throw new AssertionError("ExplicitNormalStopRequested");
        Map<String,Boolean> expected=clientExpected;int observedIndex=clientExpectationIndex;
        if(client.player!=null&&expected!=null&&clientAck!=observedIndex){boolean matches=client.player.hasEffect(FTEffects.SHADOW_BLIND)==expected.get("shadow")&&client.player.hasEffect(MobEffects.BLINDNESS)==expected.get("blind")&&client.player.hasEffect(MobEffects.DARKNESS)==expected.get("dark")&&client.player.hasEffect(MobEffects.MOVEMENT_SPEED)==expected.get("speed");if(matches){checks.add("ActualLocalPlayerNativeEffectStateSynchronized:"+observedIndex);clientAck=observedIndex;}}
        if(serverComplete)finish(null);
    }catch(Throwable failure){finish(failure);}}
    private static void finish(Throwable failure){if(!finished.compareAndSet(false,true))return;Minecraft.getInstance().execute(()->{try{
        Map<String,Object> result=new LinkedHashMap<>();result.put("failures",failure==null?0:1);result.put("failure",failure==null?null:failure.toString());result.put("checks",new ArrayList<>(checks));result.put("helperRows",new ArrayList<>(helperRows));result.put("cowRows",new ArrayList<>(cowRows));result.put("playerRows",new ArrayList<>(playerRows));result.put("completedPlayerCases",index);result.put("gaps",gaps);result.put("naturalServerPostCallbacks",callbacks);result.put("clientTicks",clientTicks);result.put("originalShadowRetirementAccepted",candidate&&failure==null&&index==20);result.put("baselineOriginalShadowRetirementGapsDemonstrated",!candidate&&failure==null&&gaps==6);result.put("actualLocalPlayerNativeLoginAndEffectSynchronization",failure==null);result.put("authenticated",false);result.put("fakeOrSyntheticServerPlayerConstructed",false);result.put("privateReflectionDebuggerOrInternalBridgeInvocation",false);result.put("fullBossMigrationComplete",false);result.put("scope","Actual native client creates fresh world and player through public vanilla integrated login. Natural PlayerTick.Post marker/companion cleanup and real local client effect synchronization;public apply/remove fixtures;registered Boss and Cow public ErosionFilter policy controls. No private invocation,removal bypass,authenticated/dedicated multiplayer,natural Boss spell/AI/full battle or framebuffer visibility claim.");Files.writeString(Path.of("actual-shadow-retirement-client.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));}catch(Exception problem){problem.printStackTrace();}Minecraft.getInstance().stop();});}
}
