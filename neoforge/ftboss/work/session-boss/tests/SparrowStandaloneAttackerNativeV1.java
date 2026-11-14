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
public final class SparrowStandaloneAttackerNativeV1 {
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static final Map<String,Object> report=new LinkedHashMap<>();
    private static final List<Object> cases=new ArrayList<>(),events=new ArrayList<>();
    private static final List<String> MODES=List.of("standalone-pre-zero-first","standalone-positive-first","standalone-positive-repeat","standalone-incoming-cancel","standalone-pre-zero-after","managed-positive","managed-pre-zero");
    private static ServerPlayer player;
    private static ServerLevel level;
    private static EasterBossEntity primary;
    private static UUID managedFight;
    private static boolean worldRequested,clientReady,observing,ttlInclusiveChecked,cleanupChecked;
    private static int checks,clientTicks,serverTicks,modeIndex,phase,actualAttackCount,preCount,postCount,protectedHits,entityTicks;
    private static long lastAction,lastRecordAt,pruneBoundary,surrenderAt;
    private static String mode="setup";
    private static float finalPre,finalPost;
    public SparrowStandaloneAttackerNativeV1(){
        NeoForge.EVENT_BUS.addListener(SparrowStandaloneAttackerNativeV1::clientTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,false,SparrowStandaloneAttackerNativeV1::serverTick);
        NeoForge.EVENT_BUS.addListener(SparrowStandaloneAttackerNativeV1::entityTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,SparrowStandaloneAttackerNativeV1::incoming);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,SparrowStandaloneAttackerNativeV1::modifyPre);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,SparrowStandaloneAttackerNativeV1::attackEvent);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,SparrowStandaloneAttackerNativeV1::preEvent);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,SparrowStandaloneAttackerNativeV1::postEvent);
    }
    private static void require(boolean value,String message){checks++;if(!value)throw new AssertionError(message+" ["+mode+"]");}
    private static net.minecraft.nbt.ListTag attackers(){return primary.getPersistentData().getList("ftboss_sparrow_attackers",10).copy();}
    private static boolean eligible(){return primary.isAlive()&&!primary.isRemoved()&&!primary.isMini()&&!primary.isImpossibleChallenge()&&!HiddenBossIntro.isActing(primary.getUUID())&&!primary.isInvulnerable();}
    private static Map<String,Object> snapshot(){var tag=attackers();List<Object> rows=new ArrayList<>();for(int index=0;index<tag.size();index++){var row=tag.getCompound(index);rows.add(Map.of("id",row.hasUUID("Id")?row.getUUID("Id").toString():"missing","at",row.getLong("At")));}return Map.of("time",level.getGameTime(),"entity",primary.getUUID().toString(),"variant",primary.getVariant(),"managed",ManorFight.isManaged(primary),"health",primary.getHealth(),"attackers",rows);}
    private static void incoming(LivingIncomingDamageEvent event){if(observing&&event.getEntity()==primary){require(event.getSource().getEntity()==player&&event.getSource().getDirectEntity()==player,"Actual direct player damage source");if(mode.equals("standalone-incoming-cancel"))event.setCanceled(true);}}
    private static void modifyPre(LivingDamageEvent.Pre event){if(event.getEntity()==player){if(event.getNewDamage()>0.01F){protectedHits++;event.setNewDamage(0.01F);}return;}if(observing&&event.getEntity()==primary)event.setNewDamage(mode.contains("pre-zero")?0.0F:16.0F);}
    private static void attackEvent(AttackEntityEvent event){if(observing&&event.getEntity()==player&&event.getTarget()==primary){require(!event.isCanceled(),"Actual attack event is not canceled");actualAttackCount++;}}
    private static void preEvent(LivingDamageEvent.Pre event){if(observing&&event.getEntity()==primary){preCount++;finalPre=event.getNewDamage();events.add(Map.of("mode",mode,"phase","nativePre","amount",finalPre,"time",level.getGameTime(),"actualPlayerSource",event.getSource().getEntity()==player));}}
    private static void postEvent(LivingDamageEvent.Post event){if(observing&&event.getEntity()==primary){postCount++;finalPost=event.getNewDamage();events.add(Map.of("mode",mode,"phase","nativePost","amount",finalPost,"time",level.getGameTime()));}}
    private static void entityTick(EntityTickEvent.Post event){if(event.getEntity()==primary)entityTicks++;}
    private static void setup(){
        require(!(player instanceof FakePlayer)&&!player.isCreative()&&!player.isSpectator(),"Real survival integrated-server player");level=player.serverLevel();int ground=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,0,0);
        for(int blockX=-24;blockX<=24;blockX++)for(int blockZ=-24;blockZ<=24;blockZ++)level.setBlockAndUpdate(new BlockPos(blockX,ground-1,blockZ),Blocks.STONE.defaultBlockState());
        for(int chunkX=-1;chunkX<=1;chunkX++)for(int chunkZ=-1;chunkZ<=1;chunkZ++)level.setChunkForced(chunkX,chunkZ,true);
        player.getInventory().clearContent();player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0D);player.setHealth(1000.0F);player.teleportTo(0.5,ground+0.1,0.5);
        Set<UUID> before=new HashSet<>();for(var entity:level.getAllEntities())before.add(entity.getUUID());
        String key=net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(FTEntities.EASTER_BOSS.get()).toString();String command="summon "+key+" 2.5 "+(ground+0.1)+" 0.5";
        level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack().withSuppressedOutput(),command);
        List<EasterBossEntity> created=new ArrayList<>();for(var entity:level.getAllEntities())if(entity instanceof EasterBossEntity boss&&!before.contains(boss.getUUID()))created.add(boss);
        require(created.size()==1,"Actual registered vanilla summon command creates exactly one Boss without NBT overrides");primary=created.getFirst();
        require(primary.getVariant()==0&&!primary.isMini()&&!ManorFight.isManaged(primary)&&ManorFight.currentVariant(net.farmingtales.ftboss.boss.ManorFightMembers.fightId(primary))<0,"Original default Youzi is genuinely standalone");
        require(attackers().isEmpty(),"New standalone has no injected attacker identities");report.put("standaloneIngress",Map.of("command",command,"publicEntry","Commands.performPrefixedCommand","consoleCommandFixture",true,"nbtOverrides",false,"state",snapshot()));lastAction=level.getGameTime();
    }
    private static void exercise(){
        mode=MODES.get(modeIndex);require(eligible(),"Actual original target eligible after natural intro");require(!player.isCreative()&&!player.isSpectator()&&player.getMainHandItem().isEmpty(),"Actual survival empty-hand attack");
        var before=attackers();float health=primary.getHealth();Map<String,Object> row=new LinkedHashMap<>();row.put("mode",mode);row.put("before",snapshot());actualAttackCount=preCount=postCount=0;finalPre=finalPost=0.0F;
        player.teleportTo(primary.getX()+1.7,primary.getY(),primary.getZ());require(player.getAttackStrengthScale(0.5F)>=0.999F,"Natural full attack recharge");var attribute=player.getAttribute(Attributes.ATTACK_DAMAGE);double base=attribute.getBaseValue();
        try{double lower=-1000000000.0D,upper=1000000000.0D;attribute.setBaseValue(lower);double minimum=attribute.getValue();attribute.setBaseValue(upper);double maximum=attribute.getValue();require(minimum<64&&maximum>=64,"Original modifiers retained during public base calibration");for(int iteration=0;iteration<64;iteration++){double midpoint=lower+(upper-lower)*0.5D;attribute.setBaseValue(midpoint);if(attribute.getValue()<64)lower=midpoint;else upper=midpoint;}attribute.setBaseValue(lower+(upper-lower)*0.5D);require(Math.abs(attribute.getValue()-64)<0.01,"Real attack attribute calibrated only through public base");observing=true;try{player.attack(primary);}finally{observing=false;}}finally{attribute.setBaseValue(base);}
        var after=attackers();require(actualAttackCount==1,"Exactly one actual ServerPlayer.attack event");
        boolean positive=mode.contains("positive");if(positive){require(preCount==1&&finalPre==16.0F&&postCount==1&&finalPost>0&&primary.getHealth()<health,"Actual positive native damage reaches real health write");}else{require(postCount==0&&primary.getHealth()==health,"Canceled or native zero causes no target health write");if(mode.contains("pre-zero"))require(preCount==1&&finalPre==0,"Actual native Pre zero guard exercised");else require(preCount==0,"Incoming cancellation stops before native Pre");}
        if(positive&&mode.startsWith("standalone")){require(after.size()==1&&after.getCompound(0).hasUUID("Id")&&after.getCompound(0).getUUID("Id").equals(player.getUUID()),"Original native subscriber records exactly actual player and deduplicates repeats");long recorded=after.getCompound(0).getLong("At");require(recorded==level.getGameTime()&&recorded>lastRecordAt,"Original subscriber refreshes actual natural tick timestamp");lastRecordAt=recorded;}else require(after.equals(before),"Managed/canceled/zero route preserves original attacker records");
        if(mode.startsWith("managed"))require(after.isEmpty()&&ManorFight.isManaged(primary),"Managed original fight never enters standalone attacker table");
        row.put("after",snapshot());row.put("actualPlayerAttackEvents",actualAttackCount);row.put("nativePreEvents",preCount);row.put("nativePreAmount",finalPre);row.put("nativePostEvents",postCount);row.put("nativePostAmount",finalPost);cases.add(row);modeIndex++;lastAction=level.getGameTime();
        if(modeIndex==5)phase=1;if(modeIndex==7)phase=3;
    }
    private static void setupManaged(){
        require(!ManorFight.hasActive(level),"Standalone did not manufacture active managed fight");var egg=net.minecraft.core.registries.BuiltInRegistries.ITEM.stream().filter(item->item instanceof EasterSpawnEggItem value&&value.variant()==0).findFirst().orElseThrow();ItemStack stack=new ItemStack(egg);player.setItemInHand(InteractionHand.MAIN_HAND,stack);BlockPos clicked=primary.blockPosition().below();level.setBlockAndUpdate(clicked,Blocks.STONE.defaultBlockState());BlockHitResult hit=new BlockHitResult(Vec3.atCenterOf(clicked).add(0,0.5,0),Direction.UP,clicked,false);InteractionResult result=player.gameMode.useItemOn(player,level,stack,InteractionHand.MAIN_HAND,hit);require(result.consumesAction()&&ManorFight.hasActive(level),"Actual original registered egg establishes managed control");player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        for(var entity:level.getAllEntities())if(entity instanceof EasterBossEntity boss&&boss.getVariant()==0&&!boss.isMini()&&ManorFight.isManaged(boss)){primary=boss;break;}
        managedFight=ManorFight.fightId(primary);require(managedFight!=null&&player.getUUID().equals(ManorFight.summoner(managedFight))&&primary.getUUID().equals(ManorFight.currentBoss(managedFight)),"Actual original summoner/current boss/fight authority");report.put("managedIngress",Map.of("registeredItem",net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(egg).toString(),"fight",managedFight.toString(),"state",snapshot()));phase=2;lastAction=level.getGameTime();
    }
    private static void serverTick(ServerTickEvent.Post event){if(finished.get()||!clientReady)return;try{require(++serverTicks<5000,"Bounded natural observation");if(player==null){var players=event.getServer().getPlayerList().getPlayers();if(players.isEmpty())return;player=players.getFirst();setup();return;}require(player.isAlive(),"Real protected fixture player stays alive");long now=level.getGameTime();
        if((phase==0||phase==2)&&now-lastAction>=40&&eligible()){exercise();return;}
        if(phase==1){if(!ttlInclusiveChecked&&now-lastRecordAt>=1199){require(now-lastRecordAt<=1200,"Natural exact retention boundary observed without skipping");int participants=net.farmingtales.ftboss.boss.SparrowMultiplayerSupport.participants(level,primary);require(attackers().size()==1,"Original attacker retained through inclusive 1200 ticks");report.put("retentionBeforeExpiry",Map.of("age",now-lastRecordAt,"participants",participants,"state",snapshot()));ttlInclusiveChecked=true;}if(now-lastRecordAt>1200){require(ttlInclusiveChecked,"Pre-expiry native boundary observed");int participants=net.farmingtales.ftboss.boss.SparrowMultiplayerSupport.participants(level,primary);require(attackers().isEmpty(),"Original public participants prunes actual expired attacker after natural ticks");report.put("retentionAfterExpiry",Map.of("age",now-lastRecordAt,"participants",participants,"state",snapshot()));setupManaged();}return;}
        if(phase==3){if(now%1200>1120)return;var data=net.farmingtales.ftboss.boss.SparrowAppearanceData.get(event.getServer());var entry=data.find(managedFight,0);require(entry!=null&&entry.owner.equals(managedFight)&&entry.players==1,"Original managed callbacks create actual appearance record before retirement");require(ManorFight.canSurrender(player,managedFight)&&ManorFight.surrender(player,managedFight),"Actual summoner uses guarded original surrender");surrenderAt=now;pruneBoundary=(now/1200+1)*1200;report.put("retiredAppearanceBeforePeriodicPrune",Map.of("fight",managedFight.toString(),"stage",entry.stage,"players",entry.players,"used",entry.used,"retiredAt",now,"expectedNaturalPruneBoundary",pruneBoundary));phase=4;return;}
        if(phase==4){var data=net.farmingtales.ftboss.boss.SparrowAppearanceData.get(event.getServer());if(!cleanupChecked&&now-surrenderAt>=40){require(primary.isRemoved()&&!ManorFight.hasActive(managedFight)&&net.farmingtales.ftboss.boss.ManorFightData.get(event.getServer()).retired(managedFight),"Original fight retired and naturally cleaned after forty ticks");require(data.find(managedFight,0)!=null,"Actual retired record persists until original periodic event");cleanupChecked=true;}if(now>pruneBoundary){require(cleanupChecked&&data.find(managedFight,0)==null,"Actual native ServerTick.Post original subscriber prunes retired record at next 1200 tick boundary");report.put("periodicPrune",Map.of("observedAt",now,"expectedBoundary",pruneBoundary,"actualRetiredFight",managedFight.toString(),"recordAbsent",true,"pruneOrTickCallbackInvokedByObserver",false));report.put("standaloneNativeAttackerEventAccepted",true);report.put("nativePeriodicAppearancePruneAccepted",true);report.put("naturalRetentionBoundaryAccepted",true);finish(null);}return;}
    }catch(Throwable failure){finish(failure);}}
    private static void clientTick(ClientTickEvent.Post event){if(finished.get())return;try{Minecraft client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;if(client.player!=null&&client.level!=null)clientReady=true;if(!worldRequested&&client.level==null&&client.getOverlay()==null&&client.screen instanceof TitleScreen){worldRequested=true;GameRules rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);LevelSettings settings=new LevelSettings("Original Sparrow native event and retention proof",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);client.createWorldOpenFlows().createFreshLevel("sparrow-standalone-attacker-native-v1",settings,new WorldOptions(14113301L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);}require(++clientTicks<16000,"Bounded native client observation");if(Files.exists(Path.of("normal-stop-request.json")))throw new AssertionError("Explicit normal stop requested");}catch(Throwable failure){finish(failure);}}
    private static void finish(Throwable failure){if(!finished.compareAndSet(false,true))return;if(failure!=null){StringWriter trace=new StringWriter();failure.printStackTrace(new PrintWriter(trace));report.put("failure",trace.toString());System.err.print(trace);}Minecraft.getInstance().execute(()->{try{report.put("checks",checks);report.put("failures",failure==null?0:1);report.put("completedModes",modeIndex);report.put("requiredModes",MODES);report.put("cases",cases);report.put("events",events);report.put("naturalServerTicks",serverTicks);report.put("naturalPrimaryEntityTicks",entityTicks);report.put("playerFixtureProtection",Map.of("nativePreDamageCap",0.01F,"cappedHits",protectedHits,"positiveTargetPreModifier",16.0F,"creative",player==null?false:player.isCreative(),"survivabilityAcceptanceClaimed",false));report.put("directEntityCreationByObserver",false);report.put("publicVanillaConsoleSummonFixture",true);report.put("directBossOrHelperHealthWritesByObserver",false);report.put("publicPlayerFixtureHealthInitialized",true);report.put("attackerFightOwnerOrAppearanceFieldsWrittenByObserver",false);report.put("privateGameplayCallbacksInvoked",false);report.put("manualEntityTicksInvoked",false);report.put("worldTimeOverridden",false);report.put("fakePlayerUsed",false);report.put("actualClientAndIntegratedServer",true);report.put("authenticated",false);report.put("actualMultiplayerGameplayAccepted",false);report.put("nativeWorldRestartAccepted",false);report.put("productChanged",false);report.put("fullBossMigrationComplete",false);report.put("installable",false);report.put("deployed",false);Files.writeString(Path.of("actual-sparrow-standalone-attacker.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}catch(Exception problem){problem.printStackTrace();}Minecraft.getInstance().stop();});}
}
