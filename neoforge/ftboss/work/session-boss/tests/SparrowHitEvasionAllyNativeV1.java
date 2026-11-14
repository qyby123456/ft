package ftboss.clientfxprobe;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.lang.reflect.Field;
import java.util.UUID;
import java.nio.file.Path;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeHealth;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
@Mod(value="ftboss_clientfxprobe",dist=Dist.CLIENT)
public final class SparrowHitEvasionAllyNativeV1 {
    private static final AtomicBoolean finished=new AtomicBoolean();
    private static final Map<String,Object> report=new LinkedHashMap<>();
    private static final List<Object> samples=new ArrayList<>();
    private static ServerPlayer player;
    private static ServerLevel level;
    private static EasterBossEntity sparrow,ally;
    private static int allyOwnedPathTicks,controlledKitingTicks;
    private static boolean worldRequested;
    private static volatile boolean clientReady;
    private static int checks,clientTicks,callbacks,playerTicks,entityTicks,phase,positivePosts;
    private static long spawned,hitAt;
    private static int activePathTicks,initialPositivePaths;
    private static Object firstOwnedPath;
    private static final List<Object> cases=new ArrayList<>();
    public SparrowHitEvasionAllyNativeV1() {
        NeoForge.EVENT_BUS.addListener(SparrowHitEvasionAllyNativeV1::clientTick);
        NeoForge.EVENT_BUS.addListener(SparrowHitEvasionAllyNativeV1::serverTick);
        NeoForge.EVENT_BUS.addListener(SparrowHitEvasionAllyNativeV1::playerTick);
        NeoForge.EVENT_BUS.addListener(SparrowHitEvasionAllyNativeV1::entityTick);
        NeoForge.EVENT_BUS.addListener(SparrowHitEvasionAllyNativeV1::damage);
        NeoForge.EVENT_BUS.addListener(SparrowHitEvasionAllyNativeV1::nativeDamagePost);
    }
    private static void require(boolean condition,String label) { checks++;if (!condition) throw new AssertionError(label); }
    private static boolean eligible() {
        return sparrow.isAlive() && !sparrow.isRemoved() && sparrow.getVariant()==9 && !sparrow.isMini() && !sparrow.isGiant()
            && !sparrow.isNoAi() && !sparrow.isInvulnerable() && !sparrow.isInvisible() && !sparrow.isManorIntroHidden()
            && !sparrow.isXuanlanSkyHidden() && !HiddenBossIntro.isActing(sparrow.getUUID());
    }
    private static Map<String,Object> snapshot(String label) {
        Map<String,Object> row=new LinkedHashMap<>();row.put("label",label);row.put("time",level.getGameTime());row.put("uuid",sparrow.getUUID().toString());
        row.put("eligibleByMappedOriginalGuards",eligible());row.put("health",sparrow.getHealth());row.put("position",List.of(sparrow.getX(),sparrow.getY(),sparrow.getZ()));
        row.put("playerDistanceSquared",sparrow.distanceToSqr(player));row.put("performingSkill",sparrow.isPerformingSkill());row.put("castingLike",sparrow.isCastingLike());row.put("castsWithoutStopping",sparrow.castsWithoutStopping());
        var path=sparrow.getNavigation().getPath();row.put("hasNavigationPath",path!=null);row.put("navigationDone",sparrow.getNavigation().isDone());
        row.put("pathNodes",path==null?0:path.getNodeCount());
        try{Object observed=state();if(observed!=null){row.put("reactionState",Map.of("last",field(observed,"last"),"until",field(observed,"until"),"nextPath",field(observed,"nextPath"),"controlled",field(observed,"controlled"),"hasOwnedPath",field(observed,"path")!=null));}else row.put("reactionState",Map.of("absent",true));}catch(Exception failure){throw new IllegalStateException(failure);}row.put("naturalEntityPostTicks",entityTicks);row.put("alive",sparrow.isAlive());row.put("removed",sparrow.isRemoved());return row;
    }
    private static void playerTick(PlayerTickEvent.Post event) { if (event.getEntity() instanceof ServerPlayer) playerTicks++; }
    private static void entityTick(EntityTickEvent.Post event) { if (event.getEntity()==sparrow && !event.getEntity().level().isClientSide()) entityTicks++; }
    private static void damage(BeforeHealth event) {
        if (event.getEntity()!=sparrow || event.getAmount()<=0.0F) return;
        try {
            require(event.getSource().getEntity()==player,"original before-health damage event attributed to the real integrated player");
            require(sparrow.isAlive(),"actual original before-health event has a live target");
            positivePosts++;report.put("actualPositiveDamage",event.getAmount());report.put("actualDamageSourcePlayerIdentityMatches",true);samples.add(snapshot("original-before-health-damage-event"));
        } catch (Throwable failure) { finish(failure); }
    }
    private static int nativeDamagePosts;
    private static void nativeDamagePost(LivingDamageEvent.Post event) {
        if(event.getEntity()==sparrow&&event.getNewDamage()>0.0F)nativeDamagePosts++;
    }
    private static void setup() {
        require(!(player instanceof FakePlayer) && !player.isCreative() && !player.isSpectator(),"actual native survival player, no FakePlayer or identity fabrication");
        level=player.serverLevel();int ground=level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,0,0);
        for (int blockX=-16;blockX<=24;blockX++) for (int blockZ=-16;blockZ<=16;blockZ++) level.setBlockAndUpdate(new BlockPos(blockX,ground-1,blockZ),Blocks.STONE.defaultBlockState());
        level.setChunkForced(-1,0,true);level.setChunkForced(0,0,true);level.setChunkForced(0,-1,true);level.setChunkForced(-1,-1,true);
        player.getInventory().clearContent();player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);player.setHealth(1000.0F);player.teleportTo(0.5,ground+0.1,0.5);
        sparrow=FTEntities.EASTER_BOSS.get().create(level);require(sparrow!=null,"actual registered Easter Boss created");sparrow.setVariant(9);sparrow.moveTo(2.2,ground+0.1,0.5,180.0F,0.0F);
        require(level.addFreshEntity(sparrow),"native addFreshEntity admitted ordinary variant nine");sparrow.setTarget(player);spawned=level.getGameTime();phase=1;
        report.put("actualRegisteredSparrowUuid",sparrow.getUUID().toString());report.put("actualServerPlayerUuid",player.getUUID().toString());report.put("noAllyFixture",true);samples.add(snapshot("native-admission"));
    }
    private static Map<?,?> states()throws Exception {
        Class<?> type=Class.forName("net.farmingtales.ftboss.boss.ai.SparrowHitEvasion");Field field=type.getDeclaredField("STATES");field.setAccessible(true);return (Map<?,?>)field.get(null);
    }
    private static Object state()throws Exception{return states().get(sparrow);}
    private static Object field(Object state,String name)throws Exception {Field field=state.getClass().getDeclaredField(name);field.setAccessible(true);return field.get(state);}
    private static void attack(boolean accepted)throws Exception {
        require(player.getMainHandItem().isEmpty(),"Only vanilla empty hand attack used");
        require(sparrow.distanceToSqr(player)<=4096.0,"Original attacker distance bound");
        float before=sparrow.getHealth();int previous=positivePosts;hitAt=level.getGameTime();player.attack(sparrow);
        require(positivePosts==previous+1&&sparrow.getHealth()<before,"Actual ServerPlayer.attack reaches original before-health event and lowers real health");
        Object observed=state();require((observed!=null)==accepted,"Original evasion eligibility controls actual state admission");
        if(accepted){require(field(observed,"attacker").equals(player.getUUID()),"Actual attacker UUID retained");require(((Long)field(observed,"until"))==hitAt+60,"Original sixty tick reaction window");require(((Long)field(observed,"last"))==hitAt,"Original monotonic clock origin");}
        cases.add(Map.of("phase",phase,"beforeHealth",before,"afterHealth",sparrow.getHealth(),"stateAccepted",accepted,"noAi",sparrow.isNoAi(),"at",hitAt));samples.add(snapshot("after-native-player-attack-phase-"+phase));
    }
    private static void spawnGuard(boolean noAi) {
        sparrow=FTEntities.EASTER_BOSS.get().create(level);require(sparrow!=null,"Guard fixture uses actual registered Boss");sparrow.setVariant(9);sparrow.setNoAi(noAi);sparrow.moveTo(player.getX()+1.7,player.getY(),player.getZ(),180.0F,0.0F);require(level.addFreshEntity(sparrow),"Actual guard fixture admission");spawned=level.getGameTime();
    }
    private static void diagnosePathsAfterObservation()throws Exception {
        var active=sparrow.getNavigation().getPath();List<Object> paths=new ArrayList<>();Vec3 away=sparrow.position().subtract(player.position()).multiply(1.0,0.0,1.0);if(away.lengthSqr()<0.000001)away=new Vec3(1.0,0.0,0.0);away=away.normalize();
        for(double turn:new double[]{0.0,0.55,-0.55,1.1,-1.1,1.7,-1.7}) {
            Vec3 rotated=new Vec3(away.x*Math.cos(turn)-away.z*Math.sin(turn),0.0,away.x*Math.sin(turn)+away.z*Math.cos(turn));Vec3 point=sparrow.position().add(rotated.scale(6.0));
            var path=sparrow.getNavigation().createPath(BlockPos.containing(point.x,sparrow.getY(),point.z),0);Map<String,Object> row=new LinkedHashMap<>();row.put("turn",turn);row.put("found",path!=null);
            if(path!=null){row.put("reaches",path.canReach());List<Object> nodes=new ArrayList<>();for(int index=0;index<path.getNodeCount();index++){var node=path.getNode(index);nodes.add(Map.of("x",node.x,"y",node.y,"z",node.z,"type",node.type.toString(),"withinWorldBorder",level.getWorldBorder().isWithinBounds(new BlockPos(node.x,node.y,node.z)),"playerDistance",Math.hypot(node.x+0.5-player.getX(),node.z+0.5-player.getZ())));}row.put("nodes",nodes);var end=path.getEndNode();if(end!=null){Vec3 endpoint=new Vec3(end.x+0.5,end.y,end.z+0.5);row.put("endpointNoCollision",level.noCollision(sparrow,sparrow.getBoundingBox().move(endpoint.subtract(sparrow.position()))));row.put("endpointFluidEmpty",level.getFluidState(BlockPos.containing(endpoint)).isEmpty());}}
            paths.add(row);
        }
        report.put("readOnlyCandidatePathsAfterObservation",paths);report.put("sparrowBoundingBox",sparrow.getBoundingBox().toString());report.put("sparrowWidth",sparrow.getBbWidth());report.put("sparrowHeight",sparrow.getBbHeight());require(sparrow.getNavigation().getPath()==active,"Diagnostic public planning did not install navigation");report.put("cases",cases);
    }
    private static void serverTick(ServerTickEvent.Post event) {
        if(finished.get()||!event.getServer().isSingleplayer())return;
        try {
            require(++callbacks<1400,"Bounded actual allied Sparrow observation");
            if(event.getServer().getPlayerList().getPlayerCount()!=1||!clientReady||playerTicks<100)return;
            player=event.getServer().getPlayerList().getPlayers().getFirst();
            if(phase==0){setup();return;}
            long now=level.getGameTime();require(sparrow.isAlive()&&!sparrow.isRemoved(),"Allied Sparrow remains naturally alive");
            if(phase==1){
                require(now-spawned<240,"Bounded natural allied eligibility");
                if(now-spawned<40||!eligible()||sparrow.isPerformingSkill()||sparrow.isCastingLike()&&!sparrow.castsWithoutStopping())return;
                ally=FTEntities.EASTER_BOSS.get().create(level);require(ally!=null,"Actual registered ally created");ally.setVariant(1);ally.setNoAi(true);ally.moveTo(sparrow.getX()+10.0,sparrow.getY(),sparrow.getZ(),0.0F,0.0F);require(level.addFreshEntity(ally),"Actual ordinary ally admission");
                require(ally.isAlive()&&!ally.isRemoved()&&!ally.isMini()&&ally.getVariant()!=9&&!ally.isInvisible()&&!ally.isManorIntroHidden()&&!ally.isXuanlanSkyHidden()&&!HiddenBossIntro.isActing(ally.getUUID()),"All original actual ally guards hold");
                require(java.util.Objects.equals(ManorFight.fightId(sparrow),ManorFight.fightId(ally))&&java.util.Objects.equals(sparrow.getManorSummonerId(),ally.getManorSummonerId()),"Real same-fight and summoner scope without injecting fight or authority");
                require(sparrow.distanceToSqr(ally)<=2304.0,"Original ally range bound");
                report.put("allyUuid",ally.getUUID().toString());report.put("allyAiDisabledOnlyToKeepReferencePosition",true);report.put("realManagedFightOrAuthenticatedSummonClaimed",false);attack(true);phase=2;return;
            }
            if(phase==2){
                Object observed=state();
                if(observed!=null){
                    require(((Long)field(observed,"until"))==hitAt+60,"Original allied reaction deadline unchanged");
                    Object owned=field(observed,"path");
                    if(owned!=null&&sparrow.getNavigation().getPath()==owned&&!sparrow.getNavigation().isDone())allyOwnedPathTicks++;
                    if(((Long)field(observed,"controlled"))==now){require(owned!=null&&sparrow.getNavigation().getPath()==owned,"Original kiting hook retains its own active evade path in presence of supported ally");controlledKitingTicks++;}
                }
                if(now-hitAt==1||now-hitAt==8||now-hitAt==20||now-hitAt==40||now-hitAt==60||now-hitAt==80)samples.add(snapshot("natural-allied-evasion-after-hit-"+(now-hitAt)));
                if(now-hitAt>=60)require(state()==null,"Original allied reaction expires naturally");
                if(now-hitAt<80)return;
                require(allyOwnedPathTicks>0&&controlledKitingTicks>0,"Actual ally branch uses original owned navigation and kiting short circuit");
                require(positivePosts==1,"Exactly one real confirmed allied player attack");
                sparrow.discard();ally.discard();require(!states().containsKey(sparrow),"Allied fixture native leave cleanup");
                report.put("actualOriginalSparrowEvasionObserved",true);report.put("actualConfirmedHitAndNaturalBaselineObserved",true);report.put("originalAllyCoverObserved",true);report.put("allyOwnedPathNaturalTicks",allyOwnedPathTicks);report.put("controlledOriginalKitingTicks",controlledKitingTicks);report.put("cases",cases);report.put("readOnlyPrivateStateObservationOnly",true);report.put("remainingStateEntriesAfterFixtures",states().size());finish(null);
            }
        }catch(Throwable failure){finish(failure);}
    }
    private static void clientTick(ClientTickEvent.Post event) {
        if (finished.get()) return;
        try {
            Minecraft client=Minecraft.getInstance();client.options.pauseOnLostFocus=false;
            if (client.player!=null && client.level!=null) clientReady=true;
            if (!worldRequested && client.level==null && client.getOverlay()==null && client.screen instanceof TitleScreen) {
                worldRequested=true;GameRules rules=new GameRules();rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false,null);rules.getRule(GameRules.RULE_DAYLIGHT).set(false,null);rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false,null);
                LevelSettings settings=new LevelSettings("Original Sparrow hit evasion native trial",GameType.SURVIVAL,false,Difficulty.NORMAL,true,rules,WorldDataConfiguration.DEFAULT);
                client.createWorldOpenFlows().createFreshLevel("sparrow-hit-evasion-ally-native-v1",settings,new WorldOptions(14113011L,false,false),registryAccess->registryAccess.registryOrThrow(Registries.WORLD_PRESET).getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),client.screen);
            }
            require(++clientTicks<6000,"bounded client observer");if (Files.exists(Path.of("normal-stop-request.json"))) throw new AssertionError("Explicit normal stop requested");
        } catch (Throwable failure) { finish(failure); }
    }
    private static void finish(Throwable failure) {
        if (!finished.compareAndSet(false,true)) return;
        if (failure!=null) { StringWriter trace=new StringWriter();failure.printStackTrace(new PrintWriter(trace));report.put("failure",trace.toString());System.err.print(trace); }
        Minecraft.getInstance().execute(()-> {
            try {
                report.put("checks",checks);report.put("failures",failure==null?0:1);report.put("samples",samples);report.put("naturalPlayerPostTicks",playerTicks);report.put("naturalEntityPostTicks",entityTicks);report.put("originalBeforeHealthDamageEvents",positivePosts);report.put("nativeLivingDamagePostCallbacks",nativeDamagePosts);report.put("nativeLivingDamagePostContractAccepted",false);
                report.put("actualClientAndIntegratedServer",true);report.put("explicitOfflineFixture",true);report.put("authenticated",false);report.put("fakePlayerUsed",false);report.put("privateGameplayCallbacksInvoked",false);report.put("manualEntityTicksInvoked",false);
                report.put("newJvmPlayerLoginAccepted",false);report.put("sparrowEvasionPortAccepted",false);report.put("productChanged",false);report.put("fullBossMigrationComplete",false);report.put("installable",false);report.put("deployed",false);
                Files.writeString(Path.of("actual-sparrow-hit-evasion-ally.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
            } catch (Exception problem) { problem.printStackTrace(); }
            Minecraft.getInstance().stop();
        });
    }
}
