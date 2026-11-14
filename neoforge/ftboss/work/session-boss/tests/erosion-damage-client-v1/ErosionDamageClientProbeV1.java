package ftboss.clientfxprobe;
import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import net.farmingtales.ftboss.effect.*;
import net.farmingtales.ftboss.boss.ai.BossOutgoingDamageNerfs;
import net.farmingtales.ftboss.zheergenphantom.ErosionRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
@Mod(value="ftboss_clientfxprobe",dist=Dist.CLIENT)
public final class ErosionDamageClientProbeV1 {
    private record Subject(String name,LivingEntity entity,int amplifier,int duration,int expectedCalls) {}
    private static final boolean candidate=System.getProperty("ftboss.erosion.mode","baseline").equals("candidate");
    private static final List<String> checks=new CopyOnWriteArrayList<>();
    private static final List<Map<String,Object>> damageRows=new CopyOnWriteArrayList<>(),subjectRows=new CopyOnWriteArrayList<>();
    private static final List<Subject> subjects=new ArrayList<>();
    private static final Map<LivingEntity,List<Float>> incoming=new IdentityHashMap<>();
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static volatile boolean prepared,serverComplete,clientSawErosion,clientSawCategory,clientSawExpiry;
    private static volatile float expectedClientHealth;
    private static int callbacks,clientTicks,preparedAt,gaps;
    private static boolean worldRequested;
    private static ServerLevel level;
    private static ServerPlayer player;
    public ErosionDamageClientProbeV1(){NeoForge.EVENT_BUS.addListener(ErosionDamageClientProbeV1::clientTick);NeoForge.EVENT_BUS.addListener(ErosionDamageClientProbeV1::serverTick);NeoForge.EVENT_BUS.addListener(ErosionDamageClientProbeV1::damage);}
    private static void require(boolean condition,String label){if(!condition)throw new AssertionError(label);checks.add(label);}
    private static float originalDamage(int amplifier){return ErosionRules.unscaledDamage(4.0F*(amplifier+1));}
    private static float expectedDamage(int amplifier){return candidate?originalDamage(amplifier):BossOutgoingDamageNerfs.zheergenDamage(4.0F*(amplifier+1));}
    private static void subject(String name,LivingEntity entity,int amplifier,int duration,int calls){
        entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);entity.setHealth(1000);entity.setNoGravity(true);
        Subject subject=new Subject(name,entity,amplifier,duration,calls);subjects.add(subject);incoming.put(entity,new ArrayList<>());
        require(entity.addEffect(new MobEffectInstance(FTEffects.SHADOW_EROSION,duration,amplifier,false,true,true)),"ActualRegisteredErosionAdmission:"+name);
        require(entity.getEffect(FTEffects.SHADOW_EROSION).getAmplifier()==amplifier,"ActualErosionAmplifier:"+name);
    }
    private static Cow cow(int index){Cow animal=EntityType.COW.create(level);require(animal!=null,"ActualCowFactory");animal.setNoAi(true);animal.setNoGravity(true);animal.moveTo(5+index%5*2,70,5+index/5*2,0,0);require(level.addFreshEntity(animal),"ActualCowAdmission");return animal;}
    private static void damage(LivingIncomingDamageEvent event){if(finished.get())return;try{
        List<Float> amounts=incoming.get(event.getEntity());if(amounts==null)return;
        Subject subject=subjects.stream().filter(value->value.entity()==event.getEntity()).findFirst().orElseThrow();
        require(event.getSource().is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD),"OriginalErosionDamageSource:"+subject.name());
        float actual=event.getAmount(),wanted=expectedDamage(subject.amplifier());require(Float.floatToRawIntBits(actual)==Float.floatToRawIntBits(wanted),"NaturalRegisteredErosionIncomingDamage:"+subject.name());
        if(Float.floatToRawIntBits(actual)!=Float.floatToRawIntBits(originalDamage(subject.amplifier())))gaps++;
        amounts.add(actual);damageRows.add(Map.of("subject",subject.name(),"amplifier",subject.amplifier(),"nativeEntityTick",event.getEntity().tickCount,"actualIncomingAmount",actual,"originalIncomingAmount",originalDamage(subject.amplifier())));
    }catch(Throwable failure){finish(failure);}}
    private static void serverTick(ServerTickEvent.Post event){if(finished.get()||!event.getServer().isSingleplayer())return;try{
        callbacks++;require(callbacks<2000,"BoundedNaturalServerPost");if(event.getServer().getPlayerList().getPlayerCount()!=1)return;
        if(!prepared){player=event.getServer().getPlayerList().getPlayers().getFirst();level=player.serverLevel();require(player.getGameProfile().getName().equals("ft_fx_probe")&&!(player instanceof net.neoforged.neoforge.common.util.FakePlayer),"ActualVanillaIntegratedLoginPlayer");
            level.setChunkForced(0,0,true);level.getChunk(0,0);for(int horizontal=3;horizontal<=17;horizontal++)for(int depth=3;depth<=15;depth++)level.setBlockAndUpdate(new BlockPos(horizontal,69,depth),Blocks.STONE.defaultBlockState());player.teleportTo(8,70,13);player.setNoGravity(true);player.getFoodData().setFoodLevel(10);player.getFoodData().setSaturation(0);
            MobEffect effect=FTEffects.SHADOW_EROSION.get();require(effect.getCategory()==(candidate?MobEffectCategory.BENEFICIAL:MobEffectCategory.HARMFUL)&&effect.isBeneficial()==candidate,"OriginalErosionCategoryOrBaselineGap");if(!effect.isBeneficial())gaps++;
            for(int amplifier=0;amplifier<8;amplifier++)subject("cow-amp-"+amplifier,cow(amplifier),amplifier,61,3);
            int ordinal=8;for(int duration:new int[]{19,20,21,39,40,41})subject("cow-duration-"+duration,cow(ordinal++),2,duration,duration/20);
            subject("real-player-amp-4",player,4,61,3);preparedAt=callbacks;prepared=true;return;
        }
        if(serverComplete||callbacks-preparedAt<68)return;
        int total=0;for(Subject subject:subjects){List<Float> amounts=incoming.get(subject.entity());require(amounts.size()==subject.expectedCalls(),"OriginalNaturalTwentyTickCadence:"+subject.name());require(!subject.entity().hasEffect(FTEffects.SHADOW_EROSION),"NativeNaturalEffectExpiry:"+subject.name());float wanted=1000-subject.expectedCalls()*expectedDamage(subject.amplifier());require(Math.abs(subject.entity().getHealth()-wanted)<0.001F,"ActualHealthDeltaFromNaturalErosion:"+subject.name());
            List<Integer> times=new ArrayList<>();for(Map<String,Object> row:damageRows)if(row.get("subject").equals(subject.name()))times.add((Integer)row.get("nativeEntityTick"));for(int index=1;index<times.size();index++)require(times.get(index)-times.get(index-1)==20,"ExactNaturalTwentyTickDamageSpacing:"+subject.name());
            subjectRows.add(Map.of("name",subject.name(),"amplifier",subject.amplifier(),"duration",subject.duration(),"nativeDamageCalls",amounts.size(),"nativeDamageTicks",times,"actualFinalHealth",subject.entity().getHealth(),"expectedCurrentFinalHealth",wanted,"originalFinalHealth",1000-subject.expectedCalls()*originalDamage(subject.amplifier()),"naturallyExpired",true));total+=amounts.size();}
        require(total==34,"AllOriginalNaturalDamageCalls");require(candidate?gaps==0:gaps==35,"OriginalContractGapCount:"+gaps);expectedClientHealth=player.getHealth();serverComplete=true;
    }catch(Throwable failure){finish(failure);}}
    private static void clientTick(ClientTickEvent.Post event){if(finished.get())return;try{
        Minecraft client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;
        if(!worldRequested&&client.level==null&&client.getOverlay()==null&&client.screen instanceof TitleScreen){worldRequested=true;GameRules rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);LevelSettings settings=new LevelSettings("Boss original erosion damage fixture",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);client.createWorldOpenFlows().createFreshLevel("erosion-damage-native",settings,new WorldOptions(13272026L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);}
        if(++clientTicks>4000)throw new AssertionError("BoundedNativeClientTicks");if(Files.exists(Path.of("normal-stop-request.json")))throw new AssertionError("ExplicitNormalStopRequested");
        if(client.player!=null&&client.player.hasEffect(FTEffects.SHADOW_EROSION)){clientSawErosion=true;require(client.player.getEffect(FTEffects.SHADOW_EROSION).getAmplifier()==4,"ActualClientErosionAmplifier");require(FTEffects.SHADOW_EROSION.get().isBeneficial()==candidate,"ActualClientErosionCategory");clientSawCategory=true;}
        if(serverComplete&&client.player!=null&&!client.player.hasEffect(FTEffects.SHADOW_EROSION)&&Math.abs(client.player.getHealth()-expectedClientHealth)<0.001F){require(clientSawErosion&&clientSawCategory,"ActualClientObservedLiveEffectBeforeExpiry");clientSawExpiry=true;finish(null);}
    }catch(Throwable failure){finish(failure);}}
    private static void finish(Throwable failure){if(!finished.compareAndSet(false,true))return;Minecraft.getInstance().execute(()->{try{
        Map<String,Object> result=new LinkedHashMap<>();result.put("failures",failure==null?0:1);result.put("failure",failure==null?null:failure.toString());result.put("checks",new ArrayList<>(checks));result.put("damageRows",new ArrayList<>(damageRows));result.put("subjectRows",new ArrayList<>(subjectRows));result.put("originalContractGaps",gaps);result.put("naturalServerPostCallbacks",callbacks);result.put("clientTicks",clientTicks);result.put("originalErosionDamageAccepted",candidate&&failure==null);result.put("baselineOriginalErosionDamageGapsDemonstrated",!candidate&&failure==null);result.put("actualLocalPlayerNativeLoginAndEffectSynchronization",failure==null&&clientSawErosion&&clientSawCategory&&clientSawExpiry);result.put("authenticated",false);result.put("fakeOrSyntheticServerPlayerConstructed",false);result.put("privateReflectionDebuggerOrInternalBridgeInvocation",false);result.put("fullBossMigrationComplete",false);result.put("scope","Actual native client public fresh world/login;registered erosion naturally ticks on14admitted NoAI cows and1real integrated player.8amplifiers,six duration boundaries,34native damage events,actual health deltas,cadence,natural expiry and actual local client effect/category/health synchronization. Normal public attribute/food/position fixtures;no manual Entity.tick/effect callback,no synthetic player/private invocation/authentication/full Boss battle or framebuffer-render claim.");Files.writeString(Path.of("actual-erosion-damage-client.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));}catch(Exception problem){problem.printStackTrace();}Minecraft.getInstance().stop();});}
}
