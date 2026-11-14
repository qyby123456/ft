package ftboss.clientfxprobe;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import net.farmingtales.ftboss.boss.ai.NativeManorDamageBridge;
import net.farmingtales.ftboss.boss.ai.NativeManorDamageScopeAccess;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.*;
@Mod(value="ftboss_clientfxprobe",dist=Dist.CLIENT)
public final class ManorNativeDamageEventsNativeV2 {
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static final RuntimeException SENTINEL=new RuntimeException("Owned damage observer exception identity control");
    private static final List<String> MODES=List.of("plain","incoming-cancel","incoming-zero","incoming-small","incoming-nan","pre-half","pre-nan","custom-cancel","custom-half","armor-modifier","absorption-modifier","invulnerability","throw-incoming","throw-pre","throw-custom","throw-post","nested");
    private static final Map<String,Object> report=new LinkedHashMap<>();
    private static final List<Object> cases=new ArrayList<>(),events=new ArrayList<>();
    private static final Map<String,Integer> counts=new LinkedHashMap<>();
    private static ServerPlayer player;
    private static ServerLevel level;
    private static EasterBossEntity boss,inner;
    private static DamageContainer container;
    private static boolean worldRequested,observing,nesting;
    private static volatile boolean clientReady;
    private static int checks,clientTicks,serverTicks,playerTicks,entityTicks,modeIndex;
    private static long spawned;
    private static float plainDamage,preAmount,postAmount,postOriginal,postAbsorption;
    private static String mode;
    public ManorNativeDamageEventsNativeV2(){
        NeoForge.EVENT_BUS.addListener(ManorNativeDamageEventsNativeV2::clientTick);NeoForge.EVENT_BUS.addListener(ManorNativeDamageEventsNativeV2::serverTick);
        NeoForge.EVENT_BUS.addListener(ManorNativeDamageEventsNativeV2::playerTick);NeoForge.EVENT_BUS.addListener(ManorNativeDamageEventsNativeV2::entityTick);
        NeoForge.EVENT_BUS.addListener(ManorNativeDamageEventsNativeV2::incoming);NeoForge.EVENT_BUS.addListener(ManorNativeDamageEventsNativeV2::pre);NeoForge.EVENT_BUS.addListener(ManorNativeDamageEventsNativeV2::post);
        NeoForge.EVENT_BUS.addListener(ManorNativeDamageEventsNativeV2::originalMitigation);NeoForge.EVENT_BUS.addListener(ManorNativeDamageEventsNativeV2::originalHealth);
    }
    private static void require(boolean value,String label){checks++;if(!value)throw new AssertionError(label+" ["+mode+"]");}
    private static Stack<DamageContainer> stack(EasterBossEntity entity){return ((NativeManorDamageScopeAccess)entity).ftboss$manorDamageContainers();}
    private static boolean selected(LivingEntity entity){return observing&&(entity==boss||entity==inner);}
    private static int count(String key){return counts.getOrDefault(key,0);}
    private static void record(String event,LivingEntity entity,DamageSource source,float amount){
        if(!selected(entity))return;String key=(entity==boss?"outer.":"inner.")+event;counts.merge(key,1,Integer::sum);
        require(source.getEntity()==player,"Every phase retains actual player source identity");require(stack((EasterBossEntity)entity).size()==1,"Only owned active context outside reduction helper");
        events.add(Map.of("mode",mode,"phase",key,"uuid",entity.getUUID().toString(),"amount",Float.isFinite(amount)?(Object)amount:Float.toString(amount),"healthAtEvent",entity.getHealth()));
    }
    private static void incoming(LivingIncomingDamageEvent event){
        if(!selected(event.getEntity()))return;record("incoming",event.getEntity(),event.getSource(),event.getAmount());require(stack((EasterBossEntity)event.getEntity()).peek()==event.getContainer(),"Incoming uses actual owned stack container");
        if(event.getEntity()!=boss)return;container=event.getContainer();
        switch(mode){
            case "incoming-cancel"->event.setCanceled(true);
            case "incoming-zero"->event.setAmount(0.0F);
            case "incoming-small"->event.setAmount(0.05F);
            case "incoming-nan"->event.setAmount(Float.NaN);
            case "armor-modifier"->event.addReductionModifier(Reduction.ARMOR,(context,reduction)->Math.max(reduction,context.getOriginalDamage()-0.1F));
            case "absorption-modifier"->event.addReductionModifier(Reduction.ABSORPTION,(context,reduction)->reduction*0.5F);
            case "invulnerability"->event.setInvulnerabilityTicks(27);
            case "throw-incoming"->throw SENTINEL;
        }
    }
    private static void pre(LivingDamageEvent.Pre event){
        if(!selected(event.getEntity()))return;record("pre",event.getEntity(),event.getSource(),event.getNewDamage());
        if(event.getEntity()!=boss)return;require(event.getContainer()==container,"Incoming and Pre share original/reduction context");preAmount=event.getNewDamage();
        if(mode.equals("pre-half"))event.setNewDamage(preAmount*0.5F);if(mode.equals("pre-nan"))event.setNewDamage(Float.NaN);if(mode.equals("throw-pre"))throw SENTINEL;
        if(mode.equals("nested")&&!nesting){nesting=true;float before=inner.getHealth();player.attack(inner);require(inner.getHealth()<before,"Nested actual player attack lowers second Boss health");require(stack(inner).isEmpty()&&stack(boss).size()==1&&stack(boss).peek()==container,"Nested call restores live outer context");}
    }
    private static void originalMitigation(NativeWeaponDamageEvent.BeforeMitigation event){if(selected(event.getEntity()))record("originalMitigation",event.getEntity(),event.getSource(),event.getAmount());}
    private static void originalHealth(NativeWeaponDamageEvent.BeforeHealth event){
        if(!selected(event.getEntity()))return;record("originalHealth",event.getEntity(),event.getSource(),event.getAmount());if(event.getEntity()!=boss)return;
        if(mode.equals("custom-cancel"))event.setCanceled(true);if(mode.equals("custom-half"))event.setAmount(event.getAmount()*0.5F);if(mode.equals("throw-custom"))throw SENTINEL;
    }
    private static void post(LivingDamageEvent.Post event){
        if(!selected(event.getEntity()))return;record("post",event.getEntity(),event.getSource(),event.getNewDamage());if(event.getEntity()!=boss)return;
        postAmount=event.getNewDamage();postOriginal=event.getOriginalDamage();postAbsorption=event.getReduction(Reduction.ABSORPTION);require(postOriginal==container.getOriginalDamage(),"Post retains original raw damage");
        if(mode.equals("throw-post"))throw SENTINEL;
    }
    private static EasterBossEntity create(double offset){EasterBossEntity entity=FTEntities.EASTER_BOSS.get().create(level);require(entity!=null,"Actual registered Boss fixture");entity.setVariant(9);entity.moveTo(player.getX()+offset,player.getY(),player.getZ(),180.0F,0.0F);require(level.addFreshEntity(entity),"Native ordinary Boss admission");entity.setTarget(player);return entity;}
    private static void spawn(){mode=MODES.get(modeIndex);counts.clear();container=null;preAmount=postAmount=postOriginal=postAbsorption=0.0F;nesting=false;boss=create(1.7);inner=mode.equals("nested")?create(-1.7):null;if(mode.equals("absorption-modifier"))boss.setAbsorptionAmount(0.2F);if(mode.equals("custom-cancel"))boss.setAbsorptionAmount(0.1F);spawned=level.getGameTime();}
    private static void setup(){
        require(!(player instanceof FakePlayer)&&!player.isCreative()&&!player.isSpectator(),"Actual survival ServerPlayer");level=player.serverLevel();int ground=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,0,0);
        for(int blockX=-16;blockX<=24;blockX++)for(int blockZ=-16;blockZ<=16;blockZ++)level.setBlockAndUpdate(new BlockPos(blockX,ground-1,blockZ),Blocks.STONE.defaultBlockState());
        level.setChunkForced(-1,0,true);level.setChunkForced(0,0,true);level.setChunkForced(0,-1,true);level.setChunkForced(-1,-1,true);player.getInventory().clearContent();player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);player.setHealth(1000.0F);player.teleportTo(0.5,ground+0.1,0.5);spawn();
    }
    private static void exercise()throws Exception {
        if(mode.equals("absorption-modifier")||mode.equals("custom-cancel")){float expected=mode.equals("absorption-modifier")?0.2F:0.1F;boss.getAttribute(Attributes.MAX_ABSORPTION).setBaseValue(2.0);boss.setAbsorptionAmount(expected);require(Math.abs(boss.getAbsorptionAmount()-expected)<0.000001F,"Real bounded absorption fixture admitted before attack");}
        require(player.getMainHandItem().isEmpty()&&boss.distanceToSqr(player)<=16.0,"Actual empty-hand near-melee attack");float before=boss.getHealth(),absorption=boss.getAbsorptionAmount();Throwable thrown=null;observing=true;
        try{player.attack(boss);}catch(Throwable failure){thrown=failure;}finally{observing=false;}
        boolean exception=mode.startsWith("throw-");require(exception?thrown==SENTINEL:thrown==null,"Original observer throwable identity or normal return");
        require(stack(boss).isEmpty()&&(inner==null||stack(inner).isEmpty()),"Owned protected stacks return to caller depth");Field field=NativeManorDamageBridge.class.getDeclaredField("FRAMES");field.setAccessible(true);require(((ThreadLocal<?>)field.get(null)).get()==null,"Thread-local removed after every return/exception");
        Map<String,Object> observed=new LinkedHashMap<>();observed.put("mode",mode);observed.put("beforeHealth",before);observed.put("afterHealth",boss.getHealth());observed.put("beforeAbsorption",absorption);observed.put("afterAbsorption",boss.getAbsorptionAmount());observed.put("postAbsorption",postAbsorption);observed.put("postAmount",postAmount);observed.put("counts",new LinkedHashMap<>(counts));report.put("lastObservedScenarioBeforeAssertions",observed);
        float loss=before-boss.getHealth();boolean canceled=Set.of("incoming-cancel","incoming-zero","incoming-nan","pre-nan","custom-cancel","throw-incoming","throw-pre","throw-custom").contains(mode);
        require(count("outer.incoming")==1,"Exactly one native incoming phase");
        if(canceled){require(loss==0.0F&&count("outer.post")==0,"Rejected/canceled/pre-health exception has no health loss or native Post");require(boss.getAbsorptionAmount()==absorption,"Canceled settlement does not consume planned absorption");}
        else {require(loss>0.0F&&count("outer.pre")==1&&count("outer.originalHealth")==1&&count("outer.post")==1,"Exactly one real pre/custom/post chain and positive health loss");require(Math.abs(loss-postAmount)<0.0002F,"Post damage matches actual completed health write");}
        if(mode.equals("plain")){plainDamage=loss;require(Math.abs(loss-0.28F)<0.0002F,"Original unchanged damage baseline preserved");}
        if(mode.equals("pre-half")||mode.equals("custom-half"))require(Math.abs(loss-plainDamage*0.5F)<0.0002F,"Native/original mutable phase changes actual health loss");
        if(mode.equals("incoming-small")||mode.equals("armor-modifier"))require(loss>0.0F&&loss<plainDamage,"Incoming amount/reduction modifier participates in real settlement");
        if(mode.equals("absorption-modifier"))require(Math.abs(boss.getAbsorptionAmount()-0.1F)<0.0002F&&Math.abs(postAbsorption-0.1F)<0.0002F&&Math.abs(loss-(plainDamage-0.1F))<0.0002F,"Absorption modifier preserves real absorption and reported reduction");
        if(mode.equals("invulnerability"))require(boss.invulnerableTime==27&&container.getPostAttackInvulnerabilityTicks()==27,"Actual target receives native requested invulnerability ticks");
        if(mode.equals("nested"))require(count("inner.incoming")==1&&count("inner.pre")==1&&count("inner.post")==1,"Nested real call has independent native phases");
        cases.add(Map.of("mode",mode,"beforeHealth",before,"afterHealth",boss.getHealth(),"beforeAbsorption",absorption,"afterAbsorption",boss.getAbsorptionAmount(),"counts",new LinkedHashMap<>(counts),"exceptionIdentityAccepted",exception,"callerContextRestored",true));
        boss.discard();if(inner!=null)inner.discard();modeIndex++;if(modeIndex==MODES.size()){report.put("actualNativeManorDamageEventMatrixAccepted",true);report.put("allScenarioCasesAccepted",true);finish(null);}else spawn();
    }
    private static void serverTick(ServerTickEvent.Post event){if(finished.get()||!clientReady)return;try{require(++serverTicks<2000,"Bounded natural server observation");if(player==null){List<ServerPlayer> players=event.getServer().getPlayerList().getPlayers();if(players.isEmpty())return;player=players.getFirst();setup();return;}require(player.isAlive(),"Actual player stays alive");if(level.getGameTime()-spawned>=40)exercise();}catch(Throwable failure){finish(failure);}}
    private static void playerTick(PlayerTickEvent.Post event){if(!finished.get()&&event.getEntity()==player)playerTicks++;}
    private static void entityTick(EntityTickEvent.Post event){if(!finished.get()&&event.getEntity()==boss)entityTicks++;}
    private static void clientTick(ClientTickEvent.Post event){if(finished.get())return;try{Minecraft client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;if(client.player!=null&&client.level!=null)clientReady=true;
        if(!worldRequested&&client.level==null&&client.getOverlay()==null&&client.screen instanceof TitleScreen){worldRequested=true;GameRules rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);LevelSettings settings=new LevelSettings("Actual Manor native damage event matrix",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);client.createWorldOpenFlows().createFreshLevel("manor-native-damage-events-native-v2",settings,new WorldOptions(14113112L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);}
        require(++clientTicks<9000,"Bounded native client observer");if(Files.exists(Path.of("normal-stop-request.json")))throw new AssertionError("Explicit normal stop requested");}catch(Throwable failure){finish(failure);}}
    private static void finish(Throwable failure){if(!finished.compareAndSet(false,true))return;if(failure!=null){StringWriter trace=new StringWriter();failure.printStackTrace(new PrintWriter(trace));report.put("failure",trace.toString());System.err.print(trace);}Minecraft.getInstance().execute(()->{try{report.put("checks",checks);report.put("failures",failure==null?0:1);report.put("cases",cases);report.put("events",events);report.put("completedModes",modeIndex);report.put("requiredModes",MODES);report.put("naturalPlayerPostTicks",playerTicks);report.put("naturalBossPostTicks",entityTicks);report.put("actualClientAndIntegratedServer",true);report.put("authenticated",false);report.put("fakePlayerUsed",false);report.put("privateGameplayCallbacksInvoked",false);report.put("manualEntityTicksInvoked",false);report.put("readOnlyPrivateContextObservation",true);report.put("productChanged",true);report.put("fullBossMigrationComplete",false);report.put("installable",false);report.put("deployed",false);Files.writeString(Path.of("actual-manor-native-damage-events.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}catch(Exception problem){problem.printStackTrace();}Minecraft.getInstance().stop();});}
}
