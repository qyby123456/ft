package ftboss.clientfxprobe;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
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
public final class ManorRawGuardBoundaryNativeV1 {
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static final List<String> MODES=List.of("ordinary7","below499.5","at500","above500.5","above501","above1024","cancel600","zero","negative","nan","positive-infinity","negative-infinity","non-player-source","invulnerable-target","final-positive7");
    private static final Map<String,Object> report=new LinkedHashMap<>();
    private static final List<Object> cases=new ArrayList<>(),events=new ArrayList<>();
    private static final Map<String,Integer> counts=new LinkedHashMap<>();
    private static ServerPlayer player;
    private static ServerLevel level;
    private static EasterBossEntity target;
    private static boolean worldRequested,observing,fixed;
    private static volatile boolean clientReady;
    private static int checks,clientTicks,serverTicks,playerTicks,entityTicks,modeIndex;
    private static long spawned;
    private static float incomingAmount,postAmount;
    private static String mode;
    public ManorRawGuardBoundaryNativeV1()throws Exception{
        String expectation=JsonParser.parseString(Files.readString(Path.of("raw-guard-expectation.json"))).getAsJsonObject().get("expectation").getAsString();
        if(!List.of("baseline","fixed").contains(expectation))throw new IllegalArgumentException("Explicit baseline/fixed configuration required");fixed=expectation.equals("fixed");report.put("expectation",expectation);
        NeoForge.EVENT_BUS.addListener(ManorRawGuardBoundaryNativeV1::clientTick);NeoForge.EVENT_BUS.addListener(ManorRawGuardBoundaryNativeV1::serverTick);NeoForge.EVENT_BUS.addListener(ManorRawGuardBoundaryNativeV1::playerTick);NeoForge.EVENT_BUS.addListener(ManorRawGuardBoundaryNativeV1::entityTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,ManorRawGuardBoundaryNativeV1::cancelIncoming);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,ManorRawGuardBoundaryNativeV1::attack);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,ManorRawGuardBoundaryNativeV1::incoming);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,ManorRawGuardBoundaryNativeV1::pre);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,ManorRawGuardBoundaryNativeV1::post);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,ManorRawGuardBoundaryNativeV1::originalMitigation);NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,ManorRawGuardBoundaryNativeV1::originalHealth);
    }
    private static void require(boolean value,String label){checks++;if(!value)throw new AssertionError(label+" ["+mode+"]");}
    private static int count(String key){return counts.getOrDefault(key,0);}
    private static void record(String phase,LivingEntity entity,DamageSource source,float amount,boolean canceled){
        if(!observing||entity!=target)return;counts.merge(phase,1,Integer::sum);require(source.getEntity()==player,"Every admitted damage phase retains actual player source");require(Float.isFinite(amount),"No invalid amount reaches native phases");Map<String,Object> row=new LinkedHashMap<>();row.put("mode",mode);row.put("phase",phase);row.put("amount",amount);row.put("canceled",canceled);row.put("actualDamageStackDepth",((NativeManorDamageScopeAccess)target).ftboss$manorDamageContainers().size());row.put("targetHealth",target.getHealth());events.add(row);
    }
    private static void attack(AttackEntityEvent event){if(observing&&event.getEntity()==player&&event.getTarget()==target){counts.merge("actualPlayerAttackEvent",1,Integer::sum);require(!event.isCanceled(),"Actual public player attack event is uncanceled");events.add(Map.of("mode",mode,"phase","actualPlayerAttackEvent","canceled",event.isCanceled(),"actualAttackDamage",player.getAttributeValue(Attributes.ATTACK_DAMAGE),"actualStrength",player.getAttackStrengthScale(0.5F)));}}
    private static void cancelIncoming(LivingIncomingDamageEvent event){if(observing&&event.getEntity()==target&&mode.equals("cancel600"))event.setCanceled(true);}
    private static void incoming(LivingIncomingDamageEvent event){record("incoming",event.getEntity(),event.getSource(),event.getAmount(),event.isCanceled());if(observing&&event.getEntity()==target)incomingAmount=event.getAmount();}
    private static void pre(LivingDamageEvent.Pre event){record("pre",event.getEntity(),event.getSource(),event.getNewDamage(),false);}
    private static void post(LivingDamageEvent.Post event){record("post",event.getEntity(),event.getSource(),event.getNewDamage(),false);if(observing&&event.getEntity()==target)postAmount=event.getNewDamage();}
    private static void originalMitigation(NativeWeaponDamageEvent.BeforeMitigation event){record("originalMitigation",event.getEntity(),event.getSource(),event.getAmount(),event.isCanceled());}
    private static void originalHealth(NativeWeaponDamageEvent.BeforeHealth event){record("originalHealth",event.getEntity(),event.getSource(),event.getAmount(),event.isCanceled());}
    private static Map<String,Object> guards(){Map<String,Object> values=new LinkedHashMap<>();values.put("playerAttackDamage",player.getAttributeValue(Attributes.ATTACK_DAMAGE));values.put("playerAttackStrength",player.getAttackStrengthScale(0.5F));values.put("playerCreative",player.isCreative());values.put("playerSpectator",player.isSpectator());values.put("playerGameMode",player.gameMode.getGameModeForPlayer().getName());values.put("playerEffects",player.getActiveEffects().stream().map(effect->Map.of("effect",net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()).toString(),"duration",effect.getDuration(),"amplifier",effect.getAmplifier())).toList());values.put("health",target.getHealth());values.put("invulnerable",target.isInvulnerable());values.put("invulnerableTime",target.invulnerableTime);values.put("alive",target.isAlive());values.put("removed",target.isRemoved());if(target instanceof Mob mob){values.put("noAI",mob.isNoAi());values.put("hasTarget",mob.getTarget()!=null);}if(target instanceof EasterBossEntity boss){values.put("variant",boss.getVariant());values.put("mini",boss.isMini());values.put("managed",ManorFight.isManaged(boss));values.put("finaleMember",ManorFight.isFinaleMember(boss));values.put("hiddenIntroActing",HiddenBossIntro.isActing(boss.getUUID()));values.put("introHidden",boss.isManorIntroHidden());values.put("impossible",boss.isImpossibleChallenge());}return values;}
    private static void spawn(){
        mode=MODES.get(modeIndex);target=FTEntities.EASTER_BOSS.get().create(level);require(target!=null,"Actual registered Sparrow created");target.setVariant(9);target.setNoAi(true);target.moveTo(player.getX()+1.7,player.getY(),player.getZ(),180.0F,0.0F);require(level.addFreshEntity(target),"Actual ordinary registered entity admission");require(!ManorFight.isManaged(target),"No managed authority fabricated");spawned=level.getGameTime();
    }
    private static void setup(){
        require(!(player instanceof FakePlayer)&&!player.isCreative()&&!player.isSpectator(),"Actual survival ServerPlayer");level=player.serverLevel();int ground=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,0,0);for(int blockX=-16;blockX<=24;blockX++)for(int blockZ=-16;blockZ<=16;blockZ++)level.setBlockAndUpdate(new BlockPos(blockX,ground-1,blockZ),Blocks.STONE.defaultBlockState());level.setChunkForced(-1,0,true);level.setChunkForced(0,0,true);level.setChunkForced(0,-1,true);level.setChunkForced(-1,-1,true);player.getInventory().clearContent();player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);player.setHealth(1000.0F);player.teleportTo(0.5,ground+0.1,0.5);spawn();
    }
    private static float requested(){return switch(mode){case "below499.5"->499.5F;case "at500"->500.0F;case "above500.5"->500.5F;case "above501"->501.0F;case "above1024"->1024.0F;case "cancel600"->600.0F;case "zero"->0.0F;case "negative"->-1.0F;case "nan"->Float.NaN;case "positive-infinity"->Float.POSITIVE_INFINITY;case "negative-infinity"->Float.NEGATIVE_INFINITY;default->7.0F;};}
    private static boolean publicControl(){return modeIndex>=7&&modeIndex<=13;}
    private static void cleanStack()throws Exception{
        require(((NativeManorDamageScopeAccess)target).ftboss$manorDamageContainers().isEmpty(),"Actual native Boss damage stack restored");Field field=NativeManorDamageBridge.class.getDeclaredField("FRAMES");field.setAccessible(true);require(((ThreadLocal<?>)field.get(null)).get()==null,"No leaked owned native frame");
    }
    private static void exercise()throws Exception{
        counts.clear();incomingAmount=postAmount=0.0F;float requested=requested(),before=target.getHealth();boolean direct=publicControl();Map<String,Object> row=new LinkedHashMap<>();row.put("mode",mode);row.put("requestedRaw",Float.isFinite(requested)?requested:Float.toString(requested));row.put("ingress",direct?"public-damage-api-negative-control":"actual-survival-ServerPlayer.attack");row.put("beforeHealth",before);
        if(direct){
            if(mode.equals("invulnerable-target"))target.setInvulnerable(true);row.put("beforeGuards",guards());DamageSource source=mode.equals("non-player-source")?target.damageSources().fall():target.damageSources().playerAttack(player);boolean returned;observing=true;try{returned=target.hurt(source,requested);}finally{observing=false;}row.put("returned",returned);row.put("afterHealth",target.getHealth());row.put("counts",new LinkedHashMap<>(counts));report.put("lastObservedCaseBeforeAssertions",row);require(!returned&&before==target.getHealth()&&counts.isEmpty(),"Original invalid/source/invulnerable protection rejects before all phases");
        }else{
            require(player.getMainHandItem().isEmpty()&&target.distanceToSqr(player)<=16.0,"Actual empty-hand nearby attack");require(player.getAttackStrengthScale(0.5F)>=0.999F,"Naturally full attack recharge");var attribute=player.getAttribute(Attributes.ATTACK_DAMAGE);double base=attribute.getBaseValue(),value=attribute.getValue();
            try{attribute.setBaseValue(base+1.0D);double slope=attribute.getValue()-value;require(Double.isFinite(slope)&&slope>0.0D,"Existing attack modifiers have finite positive public-attribute slope");attribute.setBaseValue(base+(requested-value)/slope);require(Math.abs(attribute.getValue()-requested)<0.001D,"Calibrated actual raw attribute without removing original effects/modifiers");row.put("originalAttackBase",base);row.put("originalAttackValue",value);row.put("actualAttributeSlope",slope);row.put("beforeGuards",guards());observing=true;try{player.attack(target);}finally{observing=false;}}finally{attribute.setBaseValue(base);}
            row.put("afterHealth",target.getHealth());row.put("incomingAmount",incomingAmount);row.put("postAmount",postAmount);row.put("counts",new LinkedHashMap<>(counts));row.put("afterGuards",guards());report.put("lastObservedCaseBeforeAssertions",row);require(count("actualPlayerAttackEvent")==1,"Exactly one real public survival attack");boolean admitted=fixed||requested<=500.0F;
            if(!admitted)require(count("incoming")==0&&count("pre")==0&&count("post")==0&&before==target.getHealth(),"Old candidate reproduces raw over-500 rejection");
            else if(mode.equals("cancel600"))require(count("incoming")==1&&Math.abs(incomingAmount-requested)<0.01F&&count("pre")==0&&count("post")==0&&before==target.getHealth(),"Corrected high-raw attack respects actual native cancellation");
            else require(count("incoming")==1&&Math.abs(incomingAmount-requested)<0.01F&&count("pre")==1&&count("post")==1&&postAmount>0.0F&&before>target.getHealth(),"Original high-raw admission reaches native phases and real positive health write");
        }
        cleanStack();row.put("afterHealth",target.getHealth());cases.add(row);target.discard();modeIndex++;if(modeIndex==MODES.size()){report.put("actualRawGuardBoundaryCasesAccepted",true);report.put("originalNegativeProtectionControlsAccepted",true);report.put("baselineOver500RejectionReproduced",!fixed);report.put("fixedOver500PositiveAttacksAccepted",fixed);finish(null);}else spawn();
    }
    private static void serverTick(ServerTickEvent.Post event){if(finished.get()||!clientReady)return;try{require(++serverTicks<1600,"Bounded natural server observation");if(player==null){List<ServerPlayer> players=event.getServer().getPlayerList().getPlayers();if(players.isEmpty())return;player=players.getFirst();setup();return;}require(player.isAlive(),"Actual player remains alive");if(level.getGameTime()-spawned>=40)exercise();}catch(Throwable failure){finish(failure);}}
    private static void playerTick(PlayerTickEvent.Post event){if(!finished.get()&&event.getEntity()==player)playerTicks++;}
    private static void entityTick(EntityTickEvent.Post event){if(!finished.get()&&event.getEntity()==target)entityTicks++;}
    private static void clientTick(ClientTickEvent.Post event){if(finished.get())return;try{Minecraft client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;if(client.player!=null&&client.level!=null)clientReady=true;if(!worldRequested&&client.level==null&&client.getOverlay()==null&&client.screen instanceof TitleScreen){worldRequested=true;GameRules rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);LevelSettings settings=new LevelSettings("Actual original Boss raw guard boundary",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);client.createWorldOpenFlows().createFreshLevel("manor-raw-guard-boundary-native-v1",settings,new WorldOptions(14113204L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);}require(++clientTicks<15000,"Bounded actual client observation");if(Files.exists(Path.of("normal-stop-request.json")))throw new AssertionError("Explicit normal stop requested");}catch(Throwable failure){finish(failure);}}
    private static void finish(Throwable failure){if(!finished.compareAndSet(false,true))return;if(failure!=null){StringWriter trace=new StringWriter();failure.printStackTrace(new PrintWriter(trace));report.put("failure",trace.toString());System.err.print(trace);}Minecraft.getInstance().execute(()->{try{report.put("checks",checks);report.put("failures",failure==null?0:1);report.put("cases",cases);report.put("events",events);report.put("completedModes",modeIndex);report.put("requiredModes",MODES);report.put("naturalPlayerPostTicks",playerTicks);report.put("naturalPrimaryPostTicks",entityTicks);report.put("originalEffectsAndModifiersRetained",true);report.put("authenticated",false);report.put("actualClientAndIntegratedServer",true);report.put("fakePlayerUsed",false);report.put("privateGameplayCallbacksInvoked",false);report.put("manualEntityTicksInvoked",false);report.put("installable",false);report.put("deployed",false);report.put("fullBossMigrationComplete",false);Files.writeString(Path.of("actual-manor-raw-guard-boundary.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}catch(Exception problem){problem.printStackTrace();}Minecraft.getInstance().stop();});}
}
