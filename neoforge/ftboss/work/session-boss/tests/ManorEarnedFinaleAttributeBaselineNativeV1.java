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
public final class ManorEarnedFinaleAttributeBaselineNativeV1 {
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static final Map<String,Object> report=new LinkedHashMap<>();
    private static final List<Map<String,Object>> stages=new ArrayList<>(),actions=new ArrayList<>(),events=new ArrayList<>(),attributes=new ArrayList<>();
    private static final Map<UUID,Integer> primaryTicks=new HashMap<>();
    private static final Set<UUID> stageIds=new LinkedHashSet<>();
    private static final net.minecraft.resources.ResourceLocation ADDED=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ftboss_probe","earned_finale_add_five");
    private static final net.minecraft.resources.ResourceLocation DOUBLE=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ftboss","non_manor_finale_double");
    private static boolean worldRequested,clientReady,observing,baselinePending;
    private static int checks,clientTicks,serverTicks,protectedHits,preEvents,attackEvents,baselineIndex,positiveHits;
    private static long lastAction,stageStarted,baselineStarted;
    private static ServerPlayer player;
    private static ServerLevel level;
    private static EasterBossEntity primary;
    private static LivingEntity baselineBoss;
    private static UUID fight;
    private static int phase;
    public ManorEarnedFinaleAttributeBaselineNativeV1(){
        NeoForge.EVENT_BUS.addListener(ManorEarnedFinaleAttributeBaselineNativeV1::clientTick);
        NeoForge.EVENT_BUS.addListener(ManorEarnedFinaleAttributeBaselineNativeV1::serverTick);
        NeoForge.EVENT_BUS.addListener(ManorEarnedFinaleAttributeBaselineNativeV1::entityTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,ManorEarnedFinaleAttributeBaselineNativeV1::modifyPre);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,ManorEarnedFinaleAttributeBaselineNativeV1::pre);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,ManorEarnedFinaleAttributeBaselineNativeV1::attackEvent);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,true,ManorEarnedFinaleAttributeBaselineNativeV1::death);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,ManorEarnedFinaleAttributeBaselineNativeV1::baselineJoin);
    }
    private static void require(boolean condition,String label){checks++;if(!condition)throw new AssertionError(label+" [phase="+phase+",stage="+(primary==null?-1:primary.getVariant())+",baseline="+baselineIndex+"]");}
    private static Map<String,Object> state(EasterBossEntity entity){var row=new LinkedHashMap<String,Object>();row.put("uuid",entity.getUUID().toString());row.put("variant",entity.getVariant());row.put("mini",entity.isMini());row.put("health",entity.getHealth());row.put("maxHealth",entity.getMaxHealth());row.put("alive",entity.isAlive());row.put("removed",entity.isRemoved());row.put("invulnerable",entity.isInvulnerable());row.put("introActing",HiddenBossIntro.isActing(entity.getUUID()));row.put("managed",ManorFight.isManaged(entity));row.put("fightId",String.valueOf(ManorFight.fightId(entity)));row.put("summoner",String.valueOf(entity.getManorSummonerId()));row.put("naturalEntityTicks",primaryTicks.getOrDefault(entity.getUUID(),0));row.put("time",level.getGameTime());return row;}
    private static void modifyPre(LivingDamageEvent.Pre event){if(event.getEntity()==player){if(event.getNewDamage()>0.01F){event.setNewDamage(0.01F);protectedHits++;}return;}if(observing&&event.getEntity()==primary)event.setNewDamage(250.0F);}
    private static void pre(LivingDamageEvent.Pre event){if(observing&&event.getEntity()==primary){preEvents++;require(event.getSource().getEntity()==player,"Actual managed primary native damage source is real player");}}
    private static void attackEvent(AttackEntityEvent event){if(observing&&event.getEntity()==player&&event.getTarget()==primary){require(!event.isCanceled(),"Actual managed attack event uncanceled");attackEvents++;}}
    private static void death(LivingDeathEvent event){if(event.getEntity() instanceof EasterBossEntity entity&&fight!=null&&fight.equals(ManorFight.fightId(entity)))events.add(Map.of("phase","actualManagedDeath","state",state(entity),"canceled",event.isCanceled(),"realPlayerSource",event.getSource().getEntity()==player,"realPlayerKillCredit",entity.getKillCredit()==player));}
    private static void entityTick(EntityTickEvent.Post event){if(event.getEntity() instanceof EasterBossEntity entity&&fight!=null&&fight.equals(ManorFight.fightId(entity)))primaryTicks.merge(entity.getUUID(),1,Integer::sum);}
    private static void setup(){
        require(!(player instanceof FakePlayer)&&!player.isCreative()&&!player.isSpectator(),"Actual survival player on new native client world");level=player.serverLevel();require(!net.farmingtales.ftboss.boss.ManorVictoryData.get(level.getServer()).hasConquered(player.getUUID())&&!net.farmingtales.ftboss.boss.ManorDifficulty.unlocked(player),"Actual fresh player has no original earned conquest");
        require(!net.farmingtales.ftboss.boss.BossChallengePreference.select(player,net.farmingtales.ftboss.boss.ManorDifficulty.IMPOSSIBLE)&&net.farmingtales.ftboss.boss.BossChallengePreference.selected(player)==net.farmingtales.ftboss.boss.ManorDifficulty.NORMAL,"Original public impossible selection rejects before earned normal victory");
        int ground=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,0,0);level.setDefaultSpawnPos(new BlockPos(0,ground,0),0);for(int chunkX=-5;chunkX<=5;chunkX++)for(int chunkZ=-5;chunkZ<=5;chunkZ++)level.setChunkForced(chunkX,chunkZ,true);
        player.getInventory().clearContent();player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0D);player.setHealth(1000.0F);player.teleportTo(0.5,ground+0.1,0.5);
        var eggs=net.minecraft.core.registries.BuiltInRegistries.ITEM.stream().filter(item->item instanceof EasterSpawnEggItem egg&&egg.variant()==0).toList();require(eggs.size()==1,"One actual registered normal Youzi egg");ItemStack stack=new ItemStack(eggs.getFirst(),2);player.setItemInHand(InteractionHand.MAIN_HAND,stack);BlockPos clicked=new BlockPos(2,ground-1,0);BlockHitResult hit=new BlockHitResult(Vec3.atCenterOf(clicked).add(0,0.5,0),Direction.UP,clicked,false);InteractionResult result=player.gameMode.useItemOn(player,level,stack,InteractionHand.MAIN_HAND,hit);require(result.consumesAction()&&ManorFight.hasActive(level),"Actual original registered egg establishes native normal fight");player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        for(var entity:level.getAllEntities())if(entity instanceof EasterBossEntity boss&&!boss.isMini()&&boss.getVariant()==0&&ManorFight.isManaged(boss)){primary=boss;break;}require(primary!=null,"Actual original managed first primary present");fight=ManorFight.fightId(primary);require(fight!=null&&player.getUUID().equals(ManorFight.summoner(fight))&&ManorFight.difficulty(fight)==net.farmingtales.ftboss.boss.ManorDifficulty.NORMAL,"Original actual owner and normal difficulty retained");report.put("ingress",Map.of("egg",net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(eggs.getFirst()).toString(),"fight",fight.toString(),"owner",player.getUUID().toString(),"originalImpossibleSelectionRejectedBeforeVictory",true));phase=1;observeStage();
    }
    private static void observeStage(){if(stageIds.add(primary.getUUID())){require(!primary.isMini()&&fight.equals(ManorFight.fightId(primary))&&player.getUUID().equals(primary.getManorSummonerId()),"Every primary naturally retains original same fight and actual summoner");stageStarted=level.getGameTime();lastAction=stageStarted;stages.add(state(primary));System.out.println("EARNED_FINALE_NORMAL_STAGE "+primary.getVariant()+" "+primary.getHealth()+"/"+primary.getMaxHealth());}}
    private static void attack(){
        var row=new LinkedHashMap<String,Object>();row.put("before",state(primary));preEvents=attackEvents=0;player.teleportTo(primary.getX()+1.7,primary.getY(),primary.getZ());if(player.getAttackStrengthScale(0.5F)<0.999F)return;
        var attribute=player.getAttribute(Attributes.ATTACK_DAMAGE);double base=attribute.getBaseValue();try{
            double lower=-1000000000.0D,upper=1000000000.0D;attribute.setBaseValue(lower);double minimum=attribute.getValue();attribute.setBaseValue(upper);double maximum=attribute.getValue();require(minimum<128&&maximum>=128,"Original public modifiers admit calibrated raw attack 128");for(int iteration=0;iteration<64;iteration++){double midpoint=lower+(upper-lower)*0.5D;attribute.setBaseValue(midpoint);if(attribute.getValue()<128)lower=midpoint;else upper=midpoint;}attribute.setBaseValue(lower+(upper-lower)*0.5D);observing=true;try{player.attack(primary);}finally{observing=false;}
        }finally{attribute.setBaseValue(base);}require(attackEvents==1,"One actual public player attack per action");if(preEvents>0)positiveHits++;row.put("after",state(primary));row.put("actualAttackEvents",attackEvents);row.put("nativePreEvents",preEvents);actions.add(row);report.put("lastAction",row);lastAction=level.getGameTime();
    }
    private static void baselineJoin(EntityJoinLevelEvent event){if(!baselinePending||!(event.getLevel() instanceof ServerLevel)||!(event.getEntity() instanceof net.farmingtales.ftboss.entity.RatBossEntity boss)||boss.isMinion())return;require(!event.loadedFromDisk(),"Actual new regular Boss admission");require(baselineBoss==null,"One requested baseline Boss admitted");baselineBoss=boss;var attack=boss.getAttribute(Attributes.ATTACK_DAMAGE);require(attack!=null,"Actual regular Boss attack attribute exists before original handler");if(baselineIndex%2==1)attack.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(ADDED,5.0D,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));events.add(Map.of("phase","actualRegularBossHighestJoin","case",baselineIndex,"uuid",boss.getUUID().toString(),"base",attack.getBaseValue(),"valueBeforeFinaleHandler",attack.getValue(),"preexistingAddValue",baselineIndex%2==1,"selectedDifficulty",net.farmingtales.ftboss.boss.BossChallengePreference.selected(player).id()));}
    private static void startBaseline(){
        boolean finale=baselineIndex>=2;require(net.farmingtales.ftboss.boss.BossChallengePreference.select(player,finale?net.farmingtales.ftboss.boss.ManorDifficulty.IMPOSSIBLE:net.farmingtales.ftboss.boss.ManorDifficulty.NORMAL),"Original public selected difficulty allowed only after earned unlock");
        double horizontal=20+baselineIndex*40;int ground=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(int)horizontal,0);player.teleportTo(horizontal,ground+0.1,0.5);for(int chunkX=((int)horizontal>>4)-2;chunkX<=((int)horizontal>>4)+2;chunkX++)for(int chunkZ=-2;chunkZ<=2;chunkZ++)level.setChunkForced(chunkX,chunkZ,true);
        baselineBoss=null;baselinePending=true;level.getServer().getCommands().performPrefixedCommand(level.getServer().createCommandSourceStack().withLevel(level),"summon ftboss:rat_boss "+(horizontal+2)+" "+(ground+0.1)+" 0.5");baselinePending=false;require(baselineBoss!=null,"Actual vanilla regular Boss summon passed original admission");baselineStarted=level.getGameTime();phase=3;
    }
    private static void finishBaseline(){
        var attack=baselineBoss.getAttribute(Attributes.ATTACK_DAMAGE);boolean finale=baselineIndex>=2,added=baselineIndex%2==1;require(net.farmingtales.ftboss.boss.NonManorFinale.isFinale(baselineBoss)==finale,"Actual original public preference admits expected normal or earned finale");require((attack.getModifier(DOUBLE)!=null)==finale,"Current native finale modifier physically present only after legitimate admission");double originalExpected=attack.getBaseValue()*(finale?2:1)+(added?5:0);var row=new LinkedHashMap<String,Object>();row.put("case",baselineIndex);row.put("uuid",baselineBoss.getUUID().toString());row.put("finale",finale);row.put("preexistingAddValue",added);row.put("base",attack.getBaseValue());row.put("actualValue",attack.getValue());row.put("originalBaseDoublingExpectedValue",originalExpected);row.put("matchesOriginalBaseDoubling",Math.abs(attack.getValue()-originalExpected)<0.001);row.put("health",baselineBoss.getHealth());row.put("maximumHealth",baselineBoss.getMaxHealth());row.put("naturalTicksObserved",level.getGameTime()-baselineStarted);row.put("parallelSpells",net.farmingtales.ftboss.boss.NonManorFinale.parallelSpells(baselineBoss));row.put("acceleratedTicks99",net.farmingtales.ftboss.boss.NonManorFinale.acceleratedTicks(baselineBoss,99));attributes.add(row);System.out.println("EARNED_FINALE_ATTRIBUTE_BASELINE "+baselineIndex+" actual="+attack.getValue()+" original="+originalExpected);
        if(++baselineIndex==4){require(attributes.stream().filter(item->!Boolean.TRUE.equals(item.get("matchesOriginalBaseDoubling"))).count()==1,"Actual eligible ADD_VALUE case distinguishes original and current arithmetic exactly once");report.put("actualEarnedUnlockAndFinaleAttributeBaselineAccepted",true);report.put("actualEligibleFinaleAddValueParityGapObserved",true);finish(null);}else startBaseline();
    }
    private static void serverTick(ServerTickEvent.Post event){if(finished.get()||!clientReady)return;try{
        require(++serverTicks<36000,"Bounded natural normal victory and finale baseline");if(player==null){var players=event.getServer().getPlayerList().getPlayers();if(players.isEmpty())return;player=players.getFirst();setup();return;}require(player.isAlive(),"Actual protected survival player remains alive");long now=level.getGameTime();
        if(phase==1){
            if(net.farmingtales.ftboss.boss.ManorVictoryData.get(level.getServer()).hasConquered(player.getUUID())){require(!ManorFight.hasActive(fight)&&net.farmingtales.ftboss.boss.ManorDifficulty.unlocked(player),"Original normal victory grants actual conquest and unlock without observer writes");require(stageIds.size()==EasterBossEntity.MANOR_COUNT&&positiveHits>0,"All original managed primary stages naturally observed before conquest");report.put("actualNormalVictoryEarnedUnlock",Map.of("time",now,"stageCount",stageIds.size(),"originalManorCount",EasterBossEntity.MANOR_COUNT,"player",player.getUUID().toString(),"positiveNativeHits",positiveHits));System.out.println("EARNED_FINALE_ORIGINAL_NORMAL_VICTORY "+now);phase=2;baselineStarted=now;return;}
            require(ManorFight.hasActive(fight),"Original fight remains active until real victory");UUID current=ManorFight.currentBoss(fight);if(current!=null&&level.getEntity(current) instanceof EasterBossEntity boss&&boss.isAlive()&&!boss.isRemoved()){primary=boss;observeStage();require(now-stageStarted<2400,"Each original stage must advance within two natural minutes");if(now-lastAction>=40&&!HiddenBossIntro.isActing(primary.getUUID())&&(!primary.isInvulnerable()||ManorFight.isFinaleMember(primary)))attack();}
        }else if(phase==2){if(now-baselineStarted>=40)startBaseline();}
        else if(phase==3){if(now-baselineStarted>=40)finishBaseline();}
    }catch(Throwable failure){finish(failure);}}
    private static void clientTick(ClientTickEvent.Post event){if(finished.get())return;try{Minecraft client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;if(client.player!=null&&client.level!=null)clientReady=true;if(!worldRequested&&client.level==null&&client.getOverlay()==null&&client.screen instanceof TitleScreen){worldRequested=true;GameRules rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);LevelSettings settings=new LevelSettings("Actual original normal victory earns finale attribute baseline",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);client.createWorldOpenFlows().createFreshLevel("earned-finale-attribute-baseline-native-v1",settings,new WorldOptions(14113301L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);}require(++clientTicks<120000,"Bounded native client observation");if(Files.exists(Path.of("normal-stop-request.json")))throw new AssertionError("Explicit normal stop requested");}catch(Throwable failure){finish(failure);}}
    private static void finish(Throwable failure){if(!finished.compareAndSet(false,true))return;if(failure!=null){StringWriter trace=new StringWriter();failure.printStackTrace(new PrintWriter(trace));report.put("failure",trace.toString());System.err.print(trace);}Minecraft.getInstance().execute(()->{try{
        report.put("checks",checks);report.put("failures",failure==null?0:1);report.put("observedPrimaryStages",stages);report.put("actions",actions);report.put("events",events);report.put("attributeCases",attributes);report.put("naturalServerTicks",serverTicks);report.put("positiveNativeHits",positiveHits);report.put("actualPlayerUuid",player==null?"none":player.getUUID().toString());report.put("phase",phase);
        report.put("playerFixtureProtection",Map.of("nativePreDamageCap",0.01F,"cappedHits",protectedHits,"targetPreDamageFixture",250.0F,"actualRawAttackAttribute",128.0D,"survivabilityAcceptanceClaimed",false));report.put("directBossOrHelperHealthWritesByObserver",false);report.put("victoryUnlockOrChallengeIdentityWrittenByObserver",false);report.put("actualPublicChallengeSelectionWithOriginalGate",true);report.put("privateGameplayOrRewardCallbacksInvoked",false);report.put("manualEntityTicksInvoked",false);report.put("worldTimeOverridden",false);report.put("directEntityCreationByObserver",false);report.put("fakePlayerUsed",false);report.put("authenticated",false);report.put("authenticatedFullBattleAccepted",false);report.put("actualMultiplayerGameplayAccepted",false);report.put("fullBossCombatOrSurvivabilityAcceptanceClaimed",false);report.put("nativeWorldRestartAccepted",false);report.put("productChanged",false);report.put("fullBossMigrationComplete",false);report.put("installable",false);report.put("deployed",false);
        Files.writeString(Path.of("actual-earned-finale-attribute-baseline.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
    }catch(Exception problem){problem.printStackTrace();}Minecraft.getInstance().stop();});}
}
