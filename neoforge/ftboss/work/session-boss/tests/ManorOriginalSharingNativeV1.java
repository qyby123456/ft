package ftboss.clientfxprobe;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import net.farmingtales.ftboss.boss.ManorFight;
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
public final class ManorOriginalSharingNativeV1 {
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static final List<String> MODES=List.of("solo-control","sparrow","sparrow-far","sparrow-blocked","sparrow-lethal","sparrow-incoming-half","sparrow-pre-half","sparrow-custom-half","sparrow-custom-cancel","sparrow-incoming-cancel","youzi-control","youzi-one","youzi-two","youzi-wrong-variant","youzi-far","youzi-blocked","youzi-lethal","combined","combined-custom-cancel","combined-absorption");
    private static final Map<String,Object> report=new LinkedHashMap<>();
    private static final List<Object> cases=new ArrayList<>(),events=new ArrayList<>();
    private static final Map<String,Integer> counts=new LinkedHashMap<>();
    private static final List<EasterBossEntity> supports=new ArrayList<>();
    private static final Map<EasterBossEntity,Float> supportBefore=new IdentityHashMap<>();
    private static ServerPlayer player;
    private static ServerLevel level;
    private static EasterBossEntity boss,sparrow;
    private static DamageContainer container;
    private static boolean worldRequested,observing;
    private static volatile boolean clientReady;
    private static int checks,clientTicks,serverTicks,playerTicks,entityTicks,modeIndex;
    private static long spawned;
    private static float basis,customAmount,postAmount,postAbsorption,soloDamage,youziDamage;
    private static String mode;
    public ManorOriginalSharingNativeV1(){
        NeoForge.EVENT_BUS.addListener(ManorOriginalSharingNativeV1::clientTick);NeoForge.EVENT_BUS.addListener(ManorOriginalSharingNativeV1::serverTick);
        NeoForge.EVENT_BUS.addListener(ManorOriginalSharingNativeV1::playerTick);NeoForge.EVENT_BUS.addListener(ManorOriginalSharingNativeV1::entityTick);
        NeoForge.EVENT_BUS.addListener(ManorOriginalSharingNativeV1::incoming);NeoForge.EVENT_BUS.addListener(ManorOriginalSharingNativeV1::pre);NeoForge.EVENT_BUS.addListener(ManorOriginalSharingNativeV1::post);
        NeoForge.EVENT_BUS.addListener(ManorOriginalSharingNativeV1::originalMitigation);NeoForge.EVENT_BUS.addListener(ManorOriginalSharingNativeV1::originalHealth);
    }
    private static void require(boolean value,String label){checks++;if(!value)throw new AssertionError(label+" ["+mode+"]");}
    private static Stack<DamageContainer> stack(EasterBossEntity entity){return ((NativeManorDamageScopeAccess)entity).ftboss$manorDamageContainers();}
    private static boolean selected(LivingEntity entity){return observing&&(entity==boss||supports.contains(entity));}
    private static int count(String name){return counts.getOrDefault(name,0);}
    private static void record(String phase,LivingEntity entity,DamageSource source,float amount){
        if(!selected(entity))return;String role=entity==boss?"boss":"support";counts.merge(role+"."+phase,1,Integer::sum);
        require(source.getEntity()==player,"Actual player damage-source identity preserved");if(entity==boss)require(stack(boss).size()==1,"Exactly one actual owned primary context");
        events.add(Map.of("mode",mode,"phase",role+"."+phase,"uuid",entity.getUUID().toString(),"amount",amount,"primaryHealthAtEvent",boss.getHealth()));
    }
    private static void incoming(LivingIncomingDamageEvent event){
        if(!selected(event.getEntity()))return;record("incoming",event.getEntity(),event.getSource(),event.getAmount());if(event.getEntity()!=boss)return;
        container=event.getContainer();require(stack(boss).peek()==container,"Actual primary stack owns incoming context");
        if(mode.equals("sparrow-incoming-half"))event.setAmount(event.getAmount()*0.5F);if(mode.endsWith("incoming-cancel"))event.setCanceled(true);
    }
    private static void pre(LivingDamageEvent.Pre event){
        if(!selected(event.getEntity()))return;record("pre",event.getEntity(),event.getSource(),event.getNewDamage());if(event.getEntity()!=boss)return;
        require(event.getContainer()==container,"Native phases share one primary container");if(mode.equals("sparrow-pre-half"))event.setNewDamage(event.getNewDamage()*0.5F);
    }
    private static void originalMitigation(NativeWeaponDamageEvent.BeforeMitigation event){if(!selected(event.getEntity()))return;record("originalMitigation",event.getEntity(),event.getSource(),event.getAmount());if(event.getEntity()==boss)basis=event.getAmount();}
    private static void originalHealth(NativeWeaponDamageEvent.BeforeHealth event){
        if(!selected(event.getEntity()))return;record("originalHealth",event.getEntity(),event.getSource(),event.getAmount());if(event.getEntity()!=boss)return;
        if(mode.endsWith("custom-cancel"))event.setCanceled(true);if(mode.equals("sparrow-custom-half"))event.setAmount(event.getAmount()*0.5F);customAmount=event.getAmount();
        for(EasterBossEntity support:supports)require(support.getHealth()==supportBefore.get(support),"Original sharing has not run before mutable before-health event");
    }
    private static void post(LivingDamageEvent.Post event){
        if(!selected(event.getEntity()))return;record("post",event.getEntity(),event.getSource(),event.getNewDamage());if(event.getEntity()!=boss)return;
        postAmount=event.getNewDamage();postAbsorption=event.getReduction(Reduction.ABSORPTION);require(event.getOriginalDamage()==container.getOriginalDamage(),"Native Post retains actual original attack amount");
        report.put("latestPostSupportState",supportState());
    }
    private static List<Object> supportState(){List<Object> rows=new ArrayList<>();for(EasterBossEntity support:supports)rows.add(Map.of("uuid",support.getUUID().toString(),"variant",support.getVariant(),"mini",support.isMini(),"beforeHealth",supportBefore.get(support),"health",support.getHealth(),"removed",support.isRemoved(),"invulnerable",support.isInvulnerable(),"distanceSquared",boss.distanceToSqr(support)));return rows;}
    private static EasterBossEntity create(int variant,boolean mini,double offset){
        EasterBossEntity entity=FTEntities.EASTER_BOSS.get().create(level);require(entity!=null,"Actual registered entity created");entity.setVariant(variant);if(mini)entity.setMini(true);entity.setNoAi(true);entity.moveTo(player.getX()+offset,player.getY(),player.getZ(),180.0F,0.0F);require(level.addFreshEntity(entity),"Ordinary native entity admission");require(!ManorFight.isManaged(entity)&&!ManorFight.isFinaleMember(entity),"No fabricated managed fight or finale authority");return entity;
    }
    private static void spawn(){mode=MODES.get(modeIndex);counts.clear();supports.clear();supportBefore.clear();container=null;sparrow=null;basis=customAmount=postAmount=postAbsorption=0.0F;boss=create(mode.startsWith("youzi")||mode.startsWith("combined")?0:1,false,1.7);spawned=level.getGameTime();}
    private static void fixtures(){
        if(mode.startsWith("sparrow")||mode.startsWith("combined")){sparrow=create(9,false,mode.equals("sparrow-far")?82.0:7.0);if(mode.equals("sparrow-blocked"))sparrow.setInvulnerable(true);if(mode.equals("sparrow-lethal"))sparrow.setHealth(0.01F);supports.add(sparrow);}
        if((mode.startsWith("youzi")&&!mode.equals("youzi-control"))||mode.startsWith("combined")){
            int total=mode.equals("youzi-two")||mode.startsWith("combined")?2:1;
            for(int index=0;index<total;index++){EasterBossEntity mini=create(mode.equals("youzi-wrong-variant")?1:0,true,mode.equals("youzi-far")?84.0:10.0+index*3.0);if(mode.equals("youzi-blocked"))mini.setInvulnerable(true);if(mode.equals("youzi-lethal"))mini.setHealth(0.01F);supports.add(mini);}
        }
        for(EasterBossEntity support:supports)supportBefore.put(support,support.getHealth());
        if(mode.equals("combined-absorption")){boss.getAttribute(Attributes.MAX_ABSORPTION).setBaseValue(2.0);boss.setAbsorptionAmount(youziDamage*0.25F);require(boss.getAbsorptionAmount()>0.0F,"Actual absorption fixture admitted within native limit");}
    }
    private static void setup(){
        require(!(player instanceof FakePlayer)&&!player.isCreative()&&!player.isSpectator(),"Actual survival ServerPlayer");level=player.serverLevel();int ground=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,0,0);
        for(int blockX=-16;blockX<=24;blockX++)for(int blockZ=-16;blockZ<=16;blockZ++)level.setBlockAndUpdate(new BlockPos(blockX,ground-1,blockZ),Blocks.STONE.defaultBlockState());
        level.setChunkForced(-1,0,true);level.setChunkForced(0,0,true);level.setChunkForced(0,-1,true);level.setChunkForced(-1,-1,true);level.setChunkForced(5,0,true);player.getInventory().clearContent();player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);player.setHealth(1000.0F);player.teleportTo(0.5,ground+0.1,0.5);spawn();
    }
    private static void exercise()throws Exception {
        fixtures();require(player.getMainHandItem().isEmpty()&&boss.distanceToSqr(player)<=16.0,"Actual empty-hand near-melee attack");float before=boss.getHealth(),absorption=boss.getAbsorptionAmount();observing=true;
        try{player.attack(boss);}finally{observing=false;}
        Map<String,Object> observed=new LinkedHashMap<>();observed.put("mode",mode);observed.put("beforeHealth",before);observed.put("afterHealth",boss.getHealth());observed.put("beforeAbsorption",absorption);observed.put("afterAbsorption",boss.getAbsorptionAmount());observed.put("sharingBasisBeforeMitigation",basis);observed.put("customAmount",customAmount);observed.put("postAmount",postAmount);observed.put("counts",new LinkedHashMap<>(counts));observed.put("supports",supportState());report.put("lastObservedScenarioBeforeAssertions",observed);
        require(stack(boss).isEmpty(),"Actual primary owned stack restored");for(EasterBossEntity support:supports)require(stack(support).isEmpty(),"Support never retains a fabricated damage context");Field field=NativeManorDamageBridge.class.getDeclaredField("FRAMES");field.setAccessible(true);require(((ThreadLocal<?>)field.get(null)).get()==null,"All native invocation frames cleaned");
        boolean canceled=mode.endsWith("cancel");float loss=before-boss.getHealth();require(count("boss.incoming")==1,"Exactly one actual native incoming event");require(count("support.incoming")==0&&count("support.pre")==0&&count("support.post")==0,"Original direct support health writes do not become extra hurt callbacks");
        if(canceled){require(loss==0.0F&&count("boss.post")==0&&boss.getAbsorptionAmount()==absorption,"Cancellation prevents actual primary health, native Post and absorption consumption");for(EasterBossEntity support:supports)require(support.getHealth()==supportBefore.get(support)&&!support.isRemoved(),"Cancellation prevents all original sharing side effects");}
        else {
            require(loss>0.0F&&count("boss.pre")==1&&count("boss.originalHealth")==1&&count("boss.post")==1,"Unique native/custom chain and actual positive health loss");require(Math.abs(loss-postAmount)<0.00025F&&Math.abs(loss-customAmount)<0.00025F,"Original primary settlement remains independent of discarded share return and native Post reports real loss");
            require(Math.abs(basis-(mode.equals("sparrow-incoming-half")?3.5F:7.0F))<0.0001F,"Original raw capped sharing basis is verified for real empty-hand input");
            float sparrowShare=sparrow!=null&&!mode.equals("sparrow-far")&&!mode.equals("sparrow-blocked")?basis*0.5F:0.0F;int minis=0;
            for(EasterBossEntity support:supports)if(support.isMini()&&!mode.equals("youzi-wrong-variant")&&!mode.equals("youzi-far")&&!mode.equals("youzi-blocked"))minis++;
            for(EasterBossEntity support:supports){float expected=support==sparrow?sparrowShare:minis>0?(basis-sparrowShare)*0.1F/minis:0.0F;float expectedHealth=Math.max(0.0F,supportBefore.get(support)-expected);require(Math.abs(support.getHealth()-expectedHealth)<0.00025F,"Original raw-basis ordered share and exact per-support health change");require(support.isRemoved()==(expected>supportBefore.get(support)),"Original lethal sharing immediately removes helper");}
            if(mode.equals("solo-control"))soloDamage=loss;if(mode.equals("youzi-control"))youziDamage=loss;
            float baseline=boss.getVariant()==0?youziDamage:soloDamage;if(mode.equals("sparrow-pre-half")||mode.equals("sparrow-custom-half"))require(Math.abs(loss-baseline*0.5F)<0.00025F,"Mutable final amount changes primary but leaves original raw sharing basis intact");
            else if(mode.equals("sparrow-incoming-half"))require(loss<soloDamage,"Native incoming modification affects actual primary and sharing basis");
            else if(mode.equals("combined-absorption"))require(Math.abs(loss-youziDamage*0.75F)<0.00025F&&Math.abs(postAbsorption-absorption)<0.0001F&&boss.getAbsorptionAmount()==0.0F,"Actual absorption reduces only original primary settlement and preserves raw sharing basis");
            else require(Math.abs(loss-baseline)<0.00025F,"Original sharing does not silently replace original primary settlement");
        }
        cases.add(observed);for(EasterBossEntity support:supports)if(!support.isRemoved())support.discard();boss.discard();modeIndex++;
        if(modeIndex==MODES.size()){report.put("actualOriginalSharingNativeScopedAccepted",true);finish(null);}else spawn();
    }
    private static void serverTick(ServerTickEvent.Post event){if(finished.get()||!clientReady)return;try{require(++serverTicks<2400,"Bounded natural server observation");if(player==null){List<ServerPlayer> players=event.getServer().getPlayerList().getPlayers();if(players.isEmpty())return;player=players.getFirst();setup();return;}require(player.isAlive(),"Actual player remains alive");if(level.getGameTime()-spawned>=40)exercise();}catch(Throwable failure){finish(failure);}}
    private static void playerTick(PlayerTickEvent.Post event){if(!finished.get()&&event.getEntity()==player)playerTicks++;}
    private static void entityTick(EntityTickEvent.Post event){if(!finished.get()&&event.getEntity()==boss)entityTicks++;}
    private static void clientTick(ClientTickEvent.Post event){if(finished.get())return;try{Minecraft client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;if(client.player!=null&&client.level!=null)clientReady=true;
        if(!worldRequested&&client.level==null&&client.getOverlay()==null&&client.screen instanceof TitleScreen){worldRequested=true;GameRules rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);LevelSettings settings=new LevelSettings("Actual original Manor sharing matrix",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);client.createWorldOpenFlows().createFreshLevel("manor-original-sharing-native-v1",settings,new WorldOptions(14113201L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);}
        require(++clientTicks<10000,"Bounded native client observer");if(Files.exists(Path.of("normal-stop-request.json")))throw new AssertionError("Explicit normal stop requested");}catch(Throwable failure){finish(failure);}}
    private static void finish(Throwable failure){if(!finished.compareAndSet(false,true))return;if(failure!=null){StringWriter trace=new StringWriter();failure.printStackTrace(new PrintWriter(trace));report.put("failure",trace.toString());System.err.print(trace);}Minecraft.getInstance().execute(()->{try{report.put("checks",checks);report.put("failures",failure==null?0:1);report.put("cases",cases);report.put("events",events);report.put("completedModes",modeIndex);report.put("requiredModes",MODES);report.put("naturalPlayerPostTicks",playerTicks);report.put("naturalPrimaryPostTicks",entityTicks);report.put("supportFixturesAddedImmediatelyBeforeAttack",true);report.put("supportNaturalSummonLifetimeAccepted",false);report.put("actualClientAndIntegratedServer",true);report.put("authenticated",false);report.put("fakePlayerUsed",false);report.put("privateGameplayCallbacksInvoked",false);report.put("manualEntityTicksInvoked",false);report.put("readOnlyPrivateContextObservation",true);report.put("productChanged",false);report.put("fullBossMigrationComplete",false);report.put("installable",false);report.put("deployed",false);Files.writeString(Path.of("actual-manor-original-sharing.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}catch(Exception problem){problem.printStackTrace();}Minecraft.getInstance().stop();});}
}
