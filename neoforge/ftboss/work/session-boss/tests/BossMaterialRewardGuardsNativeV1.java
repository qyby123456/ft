package ftboss.clientfxprobe;
import com.google.gson.GsonBuilder;
import net.farmingtales.ftboss.item.EasterSpawnEggItem;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
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
public final class BossMaterialRewardGuardsNativeV1 {
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static final Map<String,Object> report=new LinkedHashMap<>();
    private static final List<Map<String,Object>> cases=new ArrayList<>(),events=new ArrayList<>(),attacks=new ArrayList<>();
    private static final List<String> MODES=List.of("NO_PLAYER","MOB_LOOT_OFF","PLAYER_CREDIT_FALLBACK","CANCELED_DEATH","CANCELED_DROPS");
    private static ServerPlayer player;
    private static ServerLevel level;
    private static LivingEntity primary;
    private static net.farmingtales.ftboss.util.BossRelicRules.Kind kind;
    private static String mode;
    private static final Set<UUID> beforeSummon=new HashSet<>();
    private static final List<ItemStack> materialJoins=new ArrayList<>();
    private static boolean worldRequested,clientReady,observing,commandDamage,deathCanceled,dropsCanceled;
    private static int checks,clientTicks,serverTicks,caseIndex,phase,protectedHits,deathEvents,dropsEvents,playerHits,commandHits,preEvents,attackEvents,naturalEntityTicks;
    private static long issuedAt,lastAction,settledAt=-1;
    public BossMaterialRewardGuardsNativeV1(){
        NeoForge.EVENT_BUS.addListener(BossMaterialRewardGuardsNativeV1::clientTick);
        NeoForge.EVENT_BUS.addListener(BossMaterialRewardGuardsNativeV1::serverTick);
        NeoForge.EVENT_BUS.addListener(BossMaterialRewardGuardsNativeV1::entityTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,BossMaterialRewardGuardsNativeV1::modifyPre);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,BossMaterialRewardGuardsNativeV1::pre);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,BossMaterialRewardGuardsNativeV1::attackEvent);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,BossMaterialRewardGuardsNativeV1::cancelDeath);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,BossMaterialRewardGuardsNativeV1::cancelDrops);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,BossMaterialRewardGuardsNativeV1::death);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,BossMaterialRewardGuardsNativeV1::drops);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,BossMaterialRewardGuardsNativeV1::joined);
    }
    private static void require(boolean value,String message){checks++;if(!value)throw new AssertionError(message+" ["+caseIndex+":"+mode+":"+kind+"]");}
    private static String key(net.minecraft.world.item.Item item){return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString();}
    private static boolean rewarded(){return primary.getPersistentData().getBoolean("ftboss:boss_relic_rewarded");}
    private static Map<String,Object> state(){return Map.of("uuid",primary.getUUID().toString(),"type",net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(primary.getType()).toString(),"health",primary.getHealth(),"alive",primary.isAlive(),"removed",primary.isRemoved(),"rewarded",rewarded(),"playerKillCredit",primary.getKillCredit()==player,"time",level.getGameTime());}
    private static void addEvent(String name,Map<String,Object> values){var row=new LinkedHashMap<String,Object>(values);row.put("phase",name);row.put("case",caseIndex);row.put("mode",mode);row.put("kind",kind.name());row.put("time",level.getGameTime());events.add(row);}
    private static void modifyPre(LivingDamageEvent.Pre event){if(event.getEntity()==player){if(event.getNewDamage()>0.01F){protectedHits++;event.setNewDamage(0.01F);}return;}if(observing&&event.getEntity()==primary)event.setNewDamage(250.0F);}
    private static void pre(LivingDamageEvent.Pre event){if(observing&&event.getEntity()==primary){preEvents++;require(commandDamage?event.getSource().getEntity()==null:event.getSource().getEntity()==player,"Native direct damage source matches actual command or player action");addEvent("actualNativePre",Map.of("playerSource",event.getSource().getEntity()==player,"noEntitySource",event.getSource().getEntity()==null,"newDamage",event.getNewDamage(),"state",state()));}}
    private static void attackEvent(AttackEntityEvent event){if(observing&&event.getEntity()==player&&event.getTarget()==primary){require(!event.isCanceled(),"Actual public player attack event");attackEvents++;}}
    private static void entityTick(EntityTickEvent.Post event){if(event.getEntity()==primary)naturalEntityTicks++;}
    private static void cancelDeath(LivingDeathEvent event){if(event.getEntity()==primary&&mode.equals("CANCELED_DEATH")){event.setCanceled(true);deathCanceled=true;addEvent("actualDeathCancellation",Map.of("state",state(),"playerSource",event.getSource().getEntity()==player));}}
    private static void cancelDrops(LivingDropsEvent event){if(event.getEntity()==primary&&mode.equals("CANCELED_DROPS")){event.setCanceled(true);dropsCanceled=true;addEvent("actualDropsCancellation",Map.of("state",state(),"playerSource",event.getSource().getEntity()==player));}}
    private static void death(LivingDeathEvent event){if(event.getEntity()==primary){deathEvents++;addEvent("actualLivingDeath",Map.of("canceled",event.isCanceled(),"playerSource",event.getSource().getEntity()==player,"noEntitySource",event.getSource().getEntity()==null,"playerKillCredit",primary.getKillCredit()==player,"mobLoot",level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT),"state",state()));}}
    private static void drops(LivingDropsEvent event){if(event.getEntity()==primary){dropsEvents++;addEvent("actualLivingDrops",Map.of("canceled",event.isCanceled(),"playerSource",event.getSource().getEntity()==player,"noEntitySource",event.getSource().getEntity()==null,"playerKillCredit",primary.getKillCredit()==player,"mobLoot",level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT),"drops",event.getDrops().stream().map(item->Map.of("id",key(item.getItem().getItem()),"count",item.getItem().getCount())).toList(),"state",state()));}}
    private static void joined(EntityJoinLevelEvent event){if(level==null||event.getLevel().isClientSide()||event.isCanceled()||primary==null||!(event.getEntity() instanceof net.minecraft.world.entity.item.ItemEntity item)||item.getItem().getItem()!=net.farmingtales.ftboss.item.BossMaterialDrops.get(kind))return;materialJoins.add(item.getItem().copy());addEvent("actualMaterialJoin",Map.of("entity",item.getUUID().toString(),"item",key(item.getItem().getItem()),"count",item.getItem().getCount(),"state",state(),"productStack",Arrays.stream(Thread.currentThread().getStackTrace()).filter(frame->frame.getClassName().startsWith("net.farmingtales.ftboss.")).map(frame->frame.getClassName()+"."+frame.getMethodName()).toList()));}
    private static void setup(){
        require(!(player instanceof FakePlayer)&&!player.isCreative()&&!player.isSpectator(),"Actual survival player");
        level=player.serverLevel();level.setDefaultSpawnPos(new BlockPos(0,level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,0,0),0),0);
        for(int chunkX=-5;chunkX<=5;chunkX++)for(int chunkZ=-5;chunkZ<=5;chunkZ++)level.setChunkForced(chunkX,chunkZ,true);
        player.getInventory().clearContent();player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0D);player.setHealth(1000.0F);
        player.teleportTo(0.5,level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,0,0)+0.1,0.5);startCase();
    }
    private static void startCase(){
        kind=caseIndex%2==0?net.farmingtales.ftboss.util.BossRelicRules.Kind.FROST:net.farmingtales.ftboss.util.BossRelicRules.Kind.WINE;mode=MODES.get(caseIndex/2);
        primary=null;beforeSummon.clear();for(var entity:level.getAllEntities())beforeSummon.add(entity.getUUID());materialJoins.clear();deathEvents=dropsEvents=playerHits=commandHits=0;deathCanceled=dropsCanceled=false;settledAt=-1;
        level.getGameRules().getRule(GameRules.RULE_DOMOBLOOT).set(!mode.equals("MOB_LOOT_OFF"),level.getServer());
        String command="summon ftboss:"+kind.entity+" 2.5 "+(level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,2,0)+0.1)+" 0.5";
        level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack().withLevel(level),command);issuedAt=lastAction=level.getGameTime();phase=0;
    }
    private static void discover(){
        List<LivingEntity> found=new ArrayList<>();for(var entity:level.getAllEntities())if(entity instanceof LivingEntity living&&!beforeSummon.contains(living.getUUID())&&net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(living.getType()).toString().equals("ftboss:"+kind.entity))found.add(living);
        if(found.isEmpty()){require(level.getGameTime()-issuedAt<200,"Actual vanilla summon naturally admitted");return;}require(found.size()==1,"One new requested Boss");primary=found.getFirst();require(primary.isAlive()&&!rewarded()&&primary.getKillCredit()==null,"Fresh Boss has no forged player attribution or reward state");addEvent("actualBossIngress",Map.of("state",state()));lastAction=level.getGameTime();phase=1;
    }
    private static void action(){
        Map<String,Object> row=new LinkedHashMap<>();row.put("case",caseIndex);row.put("mode",mode);row.put("kind",kind.name());row.put("before",state());preEvents=attackEvents=0;
        boolean useCommand=mode.equals("NO_PLAYER")||(mode.equals("PLAYER_CREDIT_FALLBACK")&&playerHits>0);commandDamage=useCommand;
        observing=true;
        try{
            if(useCommand){
                String command="damage "+primary.getUUID()+" 128 minecraft:generic";row.put("vanillaCommand",command);
                level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack().withLevel(level),command);commandHits++;
            }else{
                player.teleportTo(primary.getX()+1.7,primary.getY(),primary.getZ());require(player.getAttackStrengthScale(0.5F)>=0.999F,"Naturally recharged actual player attack");
                var attribute=player.getAttribute(Attributes.ATTACK_DAMAGE);double base=attribute.getBaseValue();try{
                    double lower=-1000000000.0D,upper=1000000000.0D;attribute.setBaseValue(lower);double minimum=attribute.getValue();attribute.setBaseValue(upper);double maximum=attribute.getValue();require(minimum<128&&maximum>=128,"Existing public modifiers admit raw attack 128");
                    for(int iteration=0;iteration<64;iteration++){double midpoint=lower+(upper-lower)*0.5D;attribute.setBaseValue(midpoint);if(attribute.getValue()<128)lower=midpoint;else upper=midpoint;}
                    attribute.setBaseValue(lower+(upper-lower)*0.5D);player.attack(primary);
                }finally{attribute.setBaseValue(base);}require(attackEvents==1,"One actual public player attack");playerHits++;
            }
        }finally{observing=false;commandDamage=false;}
        row.put("after",state());row.put("nativePreEvents",preEvents);row.put("actualPlayerAttackEvents",attackEvents);attacks.add(row);report.put("lastAction",row);lastAction=level.getGameTime();
        if(mode.equals("PLAYER_CREDIT_FALLBACK")&&playerHits==1&&commandHits==0)require(primary.isAlive()&&primary.getKillCredit()==player&&preEvents>0,"Actual nonlethal player hit establishes original native kill credit before source-free command");
    }
    private static void verifyCase(){
        require(deathEvents>0,"Actual native death event observed");
        int expected=mode.equals("PLAYER_CREDIT_FALLBACK")||(mode.equals("CANCELED_DROPS")&&kind==net.farmingtales.ftboss.util.BossRelicRules.Kind.FROST)?1:0;
        require(materialJoins.size()==expected&&(expected==0||materialJoins.getFirst().getCount()==1),"Original material count across callbacks and forty natural ticks");require(rewarded()==(expected==1),"Original rewarded flag only on accepted material callback");
        if(mode.equals("NO_PLAYER"))require(playerHits==0&&commandHits>0&&primary.getKillCredit()==null,"No entity damage source and no real player kill credit");
        if(mode.equals("MOB_LOOT_OFF"))require(playerHits>0&&primary.getKillCredit()==player&&!level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT),"Real player kill rejected under actual mobLoot false");
        if(mode.equals("PLAYER_CREDIT_FALLBACK"))require(playerHits==1&&commandHits>0&&primary.getKillCredit()==player,"Original native player-credit fallback accepted");
        if(mode.equals("CANCELED_DEATH"))require(deathCanceled&&playerHits>0,"Actual cancellable death event blocks material");
        if(mode.equals("CANCELED_DROPS"))require(dropsCanceled&&dropsEvents>0,"Actual native drops cancellation observed with original scripted versus ordinary ordering");
        var row=new LinkedHashMap<String,Object>();row.put("case",caseIndex);row.put("mode",mode);row.put("kind",kind.name());row.put("material",key(net.farmingtales.ftboss.item.BossMaterialDrops.get(kind)));row.put("expectedMaterials",expected);row.put("actualMaterials",materialJoins.size());row.put("playerActions",playerHits);row.put("sourceFreeVanillaCommands",commandHits);row.put("deathEvents",deathEvents);row.put("dropsEvents",dropsEvents);row.put("deathCanceled",deathCanceled);row.put("dropsCanceled",dropsCanceled);row.put("state",state());row.put("naturalTicksAfterTerminal",level.getGameTime()-settledAt);cases.add(row);System.out.println("BOSS_REWARD_GUARD_CASE "+caseIndex+" "+mode+" "+kind+" materials="+materialJoins.size());
        level.getGameRules().getRule(GameRules.RULE_DOMOBLOOT).set(true,level.getServer());if(++caseIndex==10){report.put("actualBossMaterialRewardGuardsAccepted",true);finish(null);}else startCase();
    }
    private static void serverTick(ServerTickEvent.Post event){if(finished.get()||!clientReady)return;try{
        require(++serverTicks<18000,"Bounded actual material reward guard observation");if(player==null){var players=event.getServer().getPlayerList().getPlayers();if(players.isEmpty())return;player=players.getFirst();setup();return;}
        require(player.isAlive(),"Actual fixture player remains alive");long now=level.getGameTime();if(phase==0){discover();return;}require(now-issuedAt<1800,"Each actual reward guard settles within ninety natural seconds");
        if(deathEvents>0&&(deathCanceled||!primary.isAlive()||primary.isRemoved())){if(settledAt<0)settledAt=now;if(now-settledAt>=40)verifyCase();return;}
        if(now-lastAction>=40)action();
    }catch(Throwable failure){finish(failure);}}
    private static void clientTick(ClientTickEvent.Post event){if(finished.get())return;try{Minecraft client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;if(client.player!=null&&client.level!=null)clientReady=true;if(!worldRequested&&client.level==null&&client.getOverlay()==null&&client.screen instanceof TitleScreen){worldRequested=true;GameRules rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);LevelSettings settings=new LevelSettings("Original Boss material reward guard cases",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);client.createWorldOpenFlows().createFreshLevel("boss-material-reward-guards-native-v1",settings,new WorldOptions(14113301L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);}require(++clientTicks<16000,"Bounded native client observation");if(Files.exists(Path.of("normal-stop-request.json")))throw new AssertionError("Explicit normal stop requested");}catch(Throwable failure){finish(failure);}}
    private static void finish(Throwable failure){if(!finished.compareAndSet(false,true))return;if(failure!=null){StringWriter trace=new StringWriter();failure.printStackTrace(new PrintWriter(trace));report.put("failure",trace.toString());System.err.print(trace);}Minecraft.getInstance().execute(()->{try{
        report.put("checks",checks);report.put("failures",failure==null?0:1);report.put("completedCases",caseIndex);report.put("cases",cases);report.put("events",events);report.put("actions",attacks);report.put("naturalServerTicks",serverTicks);report.put("naturalPrimaryEntityTicks",naturalEntityTicks);
        report.put("playerFixtureProtection",Map.of("nativePreDamageCap",0.01F,"cappedHits",protectedHits,"targetPreDamageFixture",250.0F,"actualRawAttackAttribute",128.0D,"survivabilityAcceptanceClaimed",false));
        report.put("publicVanillaConsoleSummonFixture",true);report.put("publicVanillaDamageCommandFixture",true);report.put("directEntityCreationByObserver",false);report.put("directBossOrHelperHealthWritesByObserver",false);report.put("bossRewardIdentityOrEligibilityFieldsWrittenByObserver",false);report.put("privateGameplayOrRewardCallbacksInvoked",false);report.put("manualEntityTicksInvoked",false);report.put("worldTimeOverridden",false);report.put("fakePlayerUsed",false);report.put("actualClientAndIntegratedServer",true);report.put("authenticated",false);report.put("authenticatedKillAccepted",false);report.put("actualMultiplayerGameplayAccepted",false);report.put("nativeWorldRestartAccepted",false);report.put("productChanged",false);report.put("fullBossMigrationComplete",false);report.put("installable",false);report.put("deployed",false);
        Files.writeString(Path.of("actual-boss-material-reward-guards.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
    }catch(Exception problem){problem.printStackTrace();}Minecraft.getInstance().stop();});}
}
