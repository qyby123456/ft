package ftboss.clientfxprobe;
import com.google.gson.GsonBuilder;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
@Mod(value="ftboss_clientfxprobe",dist=Dist.CLIENT)
public final class ManorNativeDamageEventBaselineV1 {
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static final Map<String,Object> report=new LinkedHashMap<>();
    private static final List<Map<String,Object>> events=new ArrayList<>(),attacks=new ArrayList<>();
    private static final Map<String,Integer> counts=new LinkedHashMap<>();
    private static ServerPlayer player;
    private static ServerLevel level;
    private static EasterBossEntity sparrow;
    private static Cow control;
    private static boolean worldRequested,observingAttack;
    private static volatile boolean clientReady;
    private static int checks,clientTicks,playerTicks,entityTicks,serverTicks,phase;
    private static long spawned;
    public ManorNativeDamageEventBaselineV1() {
        NeoForge.EVENT_BUS.addListener(ManorNativeDamageEventBaselineV1::clientTick);
        NeoForge.EVENT_BUS.addListener(ManorNativeDamageEventBaselineV1::serverTick);
        NeoForge.EVENT_BUS.addListener(ManorNativeDamageEventBaselineV1::playerTick);
        NeoForge.EVENT_BUS.addListener(ManorNativeDamageEventBaselineV1::entityTick);
        NeoForge.EVENT_BUS.addListener(ManorNativeDamageEventBaselineV1::incoming);
        NeoForge.EVENT_BUS.addListener(ManorNativeDamageEventBaselineV1::pre);
        NeoForge.EVENT_BUS.addListener(ManorNativeDamageEventBaselineV1::post);
        NeoForge.EVENT_BUS.addListener(ManorNativeDamageEventBaselineV1::originalBeforeMitigation);
        NeoForge.EVENT_BUS.addListener(ManorNativeDamageEventBaselineV1::originalBeforeHealth);
    }
    private static void require(boolean condition,String label){checks++;if(!condition)throw new AssertionError(label);}
    private static void record(String kind,LivingEntity target,net.minecraft.world.damagesource.DamageSource source,float amount) {
        if(!observingAttack||(target!=sparrow&&target!=control))return;
        String fixture=target==sparrow?"boss":"vanilla";String key=fixture+"."+kind;counts.merge(key,1,Integer::sum);
        events.add(Map.of("fixture",fixture,"event",kind,"entityUuid",target.getUUID().toString(),"sourceIsActualPlayer",source.getEntity()==player,"amount",amount,"healthAtEvent",target.getHealth(),"serverTick",level.getGameTime()));
    }
    private static void incoming(LivingIncomingDamageEvent event){record("incoming",event.getEntity(),event.getSource(),event.getAmount());}
    private static void pre(LivingDamageEvent.Pre event){record("pre",event.getEntity(),event.getSource(),event.getNewDamage());}
    private static void post(LivingDamageEvent.Post event){record("post",event.getEntity(),event.getSource(),event.getNewDamage());}
    private static void originalBeforeMitigation(NativeWeaponDamageEvent.BeforeMitigation event){record("originalBeforeMitigation",event.getEntity(),event.getSource(),event.getAmount());}
    private static void originalBeforeHealth(NativeWeaponDamageEvent.BeforeHealth event){record("originalBeforeHealth",event.getEntity(),event.getSource(),event.getAmount());}
    private static int count(String fixture,String event){return counts.getOrDefault(fixture+"."+event,0);}
    private static void setup() {
        require(!(player instanceof FakePlayer)&&!player.isCreative()&&!player.isSpectator(),"Actual native survival player without identity fabrication");
        level=player.serverLevel();int ground=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,0,0);
        for(int blockX=-16;blockX<=24;blockX++)for(int blockZ=-16;blockZ<=16;blockZ++)level.setBlockAndUpdate(new BlockPos(blockX,ground-1,blockZ),Blocks.STONE.defaultBlockState());
        level.setChunkForced(-1,0,true);level.setChunkForced(0,0,true);level.setChunkForced(0,-1,true);level.setChunkForced(-1,-1,true);
        player.getInventory().clearContent();player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);player.setHealth(1000.0F);player.teleportTo(0.5,ground+0.1,0.5);
        sparrow=FTEntities.EASTER_BOSS.get().create(level);require(sparrow!=null,"Actual registered Easter Boss created");sparrow.setVariant(9);sparrow.moveTo(2.2,ground+0.1,0.5,180.0F,0.0F);
        require(level.addFreshEntity(sparrow),"Actual ordinary variant-nine entity admission");sparrow.setTarget(player);spawned=level.getGameTime();phase=1;
        report.put("actualRegisteredSparrowUuid",sparrow.getUUID().toString());report.put("actualServerPlayerUuid",player.getUUID().toString());
    }
    private static void attack(LivingEntity target,String fixture) {
        require(player.getMainHandItem().isEmpty(),"Only actual empty-hand attack used");require(target.isAlive()&&!target.isRemoved(),"Actual live target");
        require(target.distanceToSqr(player)<=16.0,"Attack fixture remains in near-melee range");float before=target.getHealth();
        observingAttack=true;try{player.attack(target);}finally{observingAttack=false;}
        float after=target.getHealth();require(Float.isFinite(before)&&Float.isFinite(after)&&after<before,"Actual ServerPlayer.attack produces positive health loss");
        attacks.add(Map.of("fixture",fixture,"beforeHealth",before,"afterHealth",after,"at",level.getGameTime(),"eventCountsAfterAttack",new LinkedHashMap<>(counts)));
    }
    private static void serverTick(ServerTickEvent.Post event) {
        if(finished.get()||!clientReady)return;
        try {
            require(++serverTicks<1000,"Bounded natural server observation");
            if(player==null){List<ServerPlayer> players=event.getServer().getPlayerList().getPlayers();if(players.isEmpty())return;player=players.getFirst();setup();return;}
            require(player.isAlive()&&!player.isRemoved(),"Actual observer player remains alive");
            if(phase==1&&level.getGameTime()-spawned>=40){
                require(entityTicks>=40,"Original Boss naturally ticks before actual hit");attack(sparrow,"boss");
                require(count("boss","originalBeforeMitigation")==1&&count("boss","originalBeforeHealth")==1,"Original mapped mutable phases fire exactly once for positive Boss hit");
                sparrow.discard();control=EntityType.COW.create(level);require(control!=null,"Actual vanilla registered cow control created");control.setNoAi(true);control.moveTo(player.getX()+1.7,player.getY(),player.getZ(),180.0F,0.0F);require(level.addFreshEntity(control),"Actual vanilla control admitted");
                report.put("actualVanillaControlUuid",control.getUUID().toString());report.put("vanillaControlNoAiOnlyHoldsPosition",true);spawned=level.getGameTime();phase=2;return;
            }
            if(phase==2&&level.getGameTime()-spawned>=40){
                attack(control,"vanilla");
                for(String name:List.of("incoming","pre","post")){require(count("vanilla",name)==1,"Same registered native listener observes exactly one positive vanilla "+name);require(count("boss",name)==0,"Unchanged Boss path bypasses native "+name);}
                for(Map<String,Object> row:events)require(Boolean.TRUE.equals(row.get("sourceIsActualPlayer")),"Every fixture event retains actual source identity");
                require(attacks.size()==2,"Two real positive attacks observed");control.discard();report.put("sameListenerVanillaPositiveControlAccepted",true);report.put("actualBossNativeDamageEventGapProven",true);finish(null);
            }
        }catch(Throwable failure){finish(failure);}
    }
    private static void playerTick(PlayerTickEvent.Post event){if(!finished.get()&&event.getEntity()==player)playerTicks++;}
    private static void entityTick(EntityTickEvent.Post event){if(!finished.get()&&event.getEntity()==sparrow)entityTicks++;}
    private static void clientTick(ClientTickEvent.Post event) {
        if(finished.get())return;
        try {
            Minecraft client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;if(client.player!=null&&client.level!=null)clientReady=true;
            if(!worldRequested&&client.level==null&&client.getOverlay()==null&&client.screen instanceof TitleScreen){
                worldRequested=true;GameRules rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);
                LevelSettings settings=new LevelSettings("Manor native damage event compatibility baseline",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);
                client.createWorldOpenFlows().createFreshLevel("manor-native-damage-event-baseline-v1",settings,new WorldOptions(14113101L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);
            }
            require(++clientTicks<6000,"Bounded actual client observer");if(Files.exists(Path.of("normal-stop-request.json")))throw new AssertionError("Explicit normal stop requested");
        }catch(Throwable failure){finish(failure);}
    }
    private static void finish(Throwable failure) {
        if(!finished.compareAndSet(false,true))return;
        if(failure!=null){StringWriter trace=new StringWriter();failure.printStackTrace(new PrintWriter(trace));report.put("failure",trace.toString());System.err.print(trace);}
        Minecraft.getInstance().execute(()->{
            try {
                report.put("checks",checks);report.put("failures",failure==null?0:1);report.put("events",events);report.put("eventCounts",counts);report.put("attacks",attacks);
                report.put("naturalPlayerPostTicks",playerTicks);report.put("naturalBossPostTicks",entityTicks);report.put("nativeManorDamageEventCompatibilityAccepted",false);
                report.put("actualClientAndIntegratedServer",true);report.put("explicitOfflineFixture",true);report.put("authenticated",false);report.put("fakePlayerUsed",false);report.put("privateGameplayCallbacksInvoked",false);report.put("manualEntityTicksInvoked",false);report.put("productChanged",false);report.put("fullBossMigrationComplete",false);report.put("installable",false);report.put("deployed",false);
                Files.writeString(Path.of("actual-manor-native-damage-event-baseline.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
            }catch(Exception problem){problem.printStackTrace();}
            Minecraft.getInstance().stop();
        });
    }
}
