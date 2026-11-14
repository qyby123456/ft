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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
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
public final class ManorUnmanagedDamageRoutingNativeV1 {
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static final List<String> MODES=List.of("sparrow-control","variant-one-noai","variant-one-ai","variant-one-with-sparrow","variant-zero-noai","vanilla-cow-control");
    private static final Map<String,Object> report=new LinkedHashMap<>();
    private static final List<Object> cases=new ArrayList<>(),events=new ArrayList<>();
    private static final Map<String,Integer> counts=new LinkedHashMap<>();
    private static ServerPlayer player;
    private static ServerLevel level;
    private static LivingEntity target;
    private static EasterBossEntity support;
    private static boolean worldRequested,observing;
    private static volatile boolean clientReady;
    private static int checks,clientTicks,serverTicks,playerTicks,entityTicks,modeIndex;
    private static long spawned;
    private static float postAmount;
    private static String mode;
    public ManorUnmanagedDamageRoutingNativeV1(){
        NeoForge.EVENT_BUS.addListener(ManorUnmanagedDamageRoutingNativeV1::clientTick);NeoForge.EVENT_BUS.addListener(ManorUnmanagedDamageRoutingNativeV1::serverTick);NeoForge.EVENT_BUS.addListener(ManorUnmanagedDamageRoutingNativeV1::playerTick);NeoForge.EVENT_BUS.addListener(ManorUnmanagedDamageRoutingNativeV1::entityTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,ManorUnmanagedDamageRoutingNativeV1::attack);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,ManorUnmanagedDamageRoutingNativeV1::incoming);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,ManorUnmanagedDamageRoutingNativeV1::pre);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,ManorUnmanagedDamageRoutingNativeV1::post);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,ManorUnmanagedDamageRoutingNativeV1::originalMitigation);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,ManorUnmanagedDamageRoutingNativeV1::originalHealth);
    }
    private static void require(boolean value,String label){checks++;if(!value)throw new AssertionError(label+" ["+mode+"]");}
    private static boolean selected(LivingEntity entity){return observing&&(entity==target||entity==support);}
    private static int count(String key){return counts.getOrDefault(key,0);}
    private static void record(String phase,LivingEntity entity,DamageSource source,float amount,boolean canceled){
        if(!selected(entity))return;String role=entity==target?"target":"support";counts.merge(role+"."+phase,1,Integer::sum);require(source.getEntity()==player,"All observed damage retains actual player source");Map<String,Object> row=new LinkedHashMap<>();row.put("mode",mode);row.put("phase",role+"."+phase);row.put("uuid",entity.getUUID().toString());row.put("amount",amount);row.put("canceled",canceled);row.put("targetHealth",target.getHealth());if(entity instanceof NativeManorDamageScopeAccess access)row.put("actualDamageStackDepth",access.ftboss$manorDamageContainers().size());events.add(row);
    }
    private static void attack(AttackEntityEvent event){if(observing&&event.getEntity()==player&&event.getTarget()==target){counts.merge("actualPlayerAttackEvent",1,Integer::sum);events.add(Map.of("mode",mode,"phase","actualPlayerAttackEvent","canceled",event.isCanceled()));}}
    private static void incoming(LivingIncomingDamageEvent event){record("incoming",event.getEntity(),event.getSource(),event.getAmount(),event.isCanceled());}
    private static void pre(LivingDamageEvent.Pre event){record("pre",event.getEntity(),event.getSource(),event.getNewDamage(),false);}
    private static void post(LivingDamageEvent.Post event){record("post",event.getEntity(),event.getSource(),event.getNewDamage(),false);if(observing&&event.getEntity()==target)postAmount=event.getNewDamage();}
    private static void originalMitigation(NativeWeaponDamageEvent.BeforeMitigation event){record("originalMitigation",event.getEntity(),event.getSource(),event.getAmount(),event.isCanceled());}
    private static void originalHealth(NativeWeaponDamageEvent.BeforeHealth event){record("originalHealth",event.getEntity(),event.getSource(),event.getAmount(),event.isCanceled());}
    private static Map<String,Object> guards(){Map<String,Object> values=new LinkedHashMap<>();values.put("health",target.getHealth());values.put("invulnerable",target.isInvulnerable());values.put("invulnerableTime",target.invulnerableTime);values.put("alive",target.isAlive());values.put("removed",target.isRemoved());if(target instanceof Mob mob){values.put("noAI",mob.isNoAi());values.put("hasTarget",mob.getTarget()!=null);}if(target instanceof EasterBossEntity boss){values.put("variant",boss.getVariant());values.put("mini",boss.isMini());values.put("managed",ManorFight.isManaged(boss));values.put("finaleMember",ManorFight.isFinaleMember(boss));values.put("hiddenIntroActing",HiddenBossIntro.isActing(boss.getUUID()));values.put("introHidden",boss.isManorIntroHidden());values.put("impossible",boss.isImpossibleChallenge());}return values;}
    private static EasterBossEntity createBoss(int variant,double offset,boolean ai){EasterBossEntity entity=FTEntities.EASTER_BOSS.get().create(level);require(entity!=null,"Actual registered Boss created");entity.setVariant(variant);entity.setNoAi(!ai);entity.moveTo(player.getX()+offset,player.getY(),player.getZ(),180.0F,0.0F);require(level.addFreshEntity(entity),"Actual ordinary entity admission");if(ai)entity.setTarget(player);require(!ManorFight.isManaged(entity),"No managed fight authority fabricated");return entity;}
    private static void spawn(){mode=MODES.get(modeIndex);counts.clear();postAmount=0.0F;support=null;if(mode.equals("vanilla-cow-control")){Mob cow=EntityType.COW.create(level);require(cow!=null,"Actual vanilla cow created");cow.setNoAi(true);cow.moveTo(player.getX()+1.7,player.getY(),player.getZ(),180.0F,0.0F);require(level.addFreshEntity(cow),"Actual vanilla cow admission");target=cow;}else target=createBoss(mode.equals("sparrow-control")?9:mode.equals("variant-zero-noai")?0:1,1.7,mode.equals("variant-one-ai"));spawned=level.getGameTime();}
    private static void setup(){require(!(player instanceof FakePlayer)&&!player.isCreative()&&!player.isSpectator(),"Actual survival ServerPlayer");level=player.serverLevel();int ground=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,0,0);for(int blockX=-16;blockX<=24;blockX++)for(int blockZ=-16;blockZ<=16;blockZ++)level.setBlockAndUpdate(new BlockPos(blockX,ground-1,blockZ),Blocks.STONE.defaultBlockState());level.setChunkForced(-1,0,true);level.setChunkForced(0,0,true);level.setChunkForced(0,-1,true);level.setChunkForced(-1,-1,true);player.getInventory().clearContent();player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);player.setHealth(1000.0F);player.teleportTo(0.5,ground+0.1,0.5);spawn();}
    private static void exercise()throws Exception {
        if(mode.equals("variant-one-with-sparrow"))support=createBoss(9,7.0,false);
        require(player.getMainHandItem().isEmpty()&&target.distanceToSqr(player)<=16.0,"Actual empty-hand near-melee attack");float before=target.getHealth(),supportBefore=support==null?0.0F:support.getHealth();Map<String,Object> beforeGuards=guards();observing=true;try{player.attack(target);}finally{observing=false;}
        Map<String,Object> row=new LinkedHashMap<>();row.put("mode",mode);row.put("beforeGuards",beforeGuards);row.put("afterGuards",guards());row.put("beforeHealth",before);row.put("afterHealth",target.getHealth());row.put("postAmount",postAmount);row.put("counts",new LinkedHashMap<>(counts));if(support!=null){row.put("supportBeforeHealth",supportBefore);row.put("supportAfterHealth",support.getHealth());row.put("supportRemoved",support.isRemoved());}report.put("lastObservedCaseBeforeAssertions",row);
        if(mode.equals("sparrow-control")||mode.equals("vanilla-cow-control"))require(before>target.getHealth()&&count("target.incoming")==1&&count("target.pre")==1&&count("target.post")==1,"Actual Boss/native vanilla positive controls validate same uncanceled observers");
        if(target instanceof NativeManorDamageScopeAccess access)require(access.ftboss$manorDamageContainers().isEmpty(),"Actual Boss stack restored");if(support!=null)require(((NativeManorDamageScopeAccess)support).ftboss$manorDamageContainers().isEmpty(),"Actual support stack empty");Field field=NativeManorDamageBridge.class.getDeclaredField("FRAMES");field.setAccessible(true);require(((ThreadLocal<?>)field.get(null)).get()==null,"No leaked native owned frames");
        cases.add(row);if(support!=null&&!support.isRemoved())support.discard();target.discard();modeIndex++;if(modeIndex==MODES.size()){report.put("actualUnmanagedBossDamageRoutingDiagnosticCompleted",true);report.put("actualSameListenerBossAndVanillaPositiveControlsAccepted",true);finish(null);}else spawn();
    }
    private static void serverTick(ServerTickEvent.Post event){if(finished.get()||!clientReady)return;try{require(++serverTicks<1200,"Bounded natural server observation");if(player==null){List<ServerPlayer> players=event.getServer().getPlayerList().getPlayers();if(players.isEmpty())return;player=players.getFirst();setup();return;}require(player.isAlive(),"Actual player stays alive");if(level.getGameTime()-spawned>=40)exercise();}catch(Throwable failure){finish(failure);}}
    private static void playerTick(PlayerTickEvent.Post event){if(!finished.get()&&event.getEntity()==player)playerTicks++;}
    private static void entityTick(EntityTickEvent.Post event){if(!finished.get()&&event.getEntity()==target)entityTicks++;}
    private static void clientTick(ClientTickEvent.Post event){if(finished.get())return;try{Minecraft client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;if(client.player!=null&&client.level!=null)clientReady=true;if(!worldRequested&&client.level==null&&client.getOverlay()==null&&client.screen instanceof TitleScreen){worldRequested=true;GameRules rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);LevelSettings settings=new LevelSettings("Actual unmanaged Boss damage routing diagnostic",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);client.createWorldOpenFlows().createFreshLevel("manor-unmanaged-damage-routing-native-v1",settings,new WorldOptions(14113202L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);}require(++clientTicks<10000,"Bounded native client observation");if(Files.exists(Path.of("normal-stop-request.json")))throw new AssertionError("Explicit normal stop requested");}catch(Throwable failure){finish(failure);}}
    private static void finish(Throwable failure){if(!finished.compareAndSet(false,true))return;if(failure!=null){StringWriter trace=new StringWriter();failure.printStackTrace(new PrintWriter(trace));report.put("failure",trace.toString());System.err.print(trace);}Minecraft.getInstance().execute(()->{try{report.put("checks",checks);report.put("failures",failure==null?0:1);report.put("cases",cases);report.put("events",events);report.put("completedModes",modeIndex);report.put("requiredModes",MODES);report.put("naturalPlayerPostTicks",playerTicks);report.put("naturalPrimaryPostTicks",entityTicks);report.put("supportCreatedImmediatelyBeforeActualAttack",true);report.put("diagnosticOnly",true);report.put("sharingAccepted",false);report.put("authenticated",false);report.put("actualClientAndIntegratedServer",true);report.put("fakePlayerUsed",false);report.put("privateGameplayCallbacksInvoked",false);report.put("manualEntityTicksInvoked",false);report.put("productChanged",false);report.put("fullBossMigrationComplete",false);report.put("installable",false);report.put("deployed",false);Files.writeString(Path.of("actual-manor-unmanaged-damage-routing.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}catch(Exception problem){problem.printStackTrace();}Minecraft.getInstance().stop();});}
}
