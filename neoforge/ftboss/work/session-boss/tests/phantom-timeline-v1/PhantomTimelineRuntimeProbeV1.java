package ftboss.bossprobe;
import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;
import net.farmingtales.ftboss.entity.*;
import net.farmingtales.ftboss.boss.ai.ManorFiveSkills;
import net.farmingtales.ftboss.boss.ManorDifficulty;
import net.farmingtales.ftboss.util.ManorFiveCombatRules;
import net.farmingtales.ftboss.util.ManorFiveCombatRules.Skill;
import net.farmingtales.ftboss.zheergenphantom.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
@Mod("ftboss_bossprobe")
public final class PhantomTimelineRuntimeProbeV1 {
    private record Plan(String name,ManorDifficulty difficulty,int rotation,String action) {}
    private static final List<Plan> plans=new ArrayList<>();
    private static final List<String> checks=new ArrayList<>();
    private static final List<Map<String,Object>> cases=new ArrayList<>();
    private static ServerLevel level,foreign;
    private static EasterBossEntity boss;
    private static Cow target,control;
    private static ManorFiveSkills controller;
    private static int index,callbacks;
    private static long started;
    private static boolean ready,finished,acted,reverted;
    private static Plan plan;
    public PhantomTimelineRuntimeProbeV1(){NeoForge.EVENT_BUS.addListener(PhantomTimelineRuntimeProbeV1::start);NeoForge.EVENT_BUS.addListener(PhantomTimelineRuntimeProbeV1::tick);}
    private static void require(boolean condition,String name){if(!condition)throw new AssertionError(name);checks.add(name);}
    private static void platform(ServerLevel world){
        world.setChunkForced(0,0,true);world.setChunkForced(1,0,true);world.getChunk(0,0);world.getChunk(1,0);
        for(int horizontal=4;horizontal<=25;horizontal++)for(int depth=1;depth<=14;depth++)world.setBlockAndUpdate(new BlockPos(horizontal,239,depth),Blocks.STONE.defaultBlockState());
    }
    private static void start(ServerStartedEvent event){try{
        level=event.getServer().overworld();foreign=event.getServer().getLevel(Level.NETHER);
        require(foreign!=null&&event.getServer().getPlayerList().getPlayerCount()==0,"ActualNativeDimensionsWithoutAuthenticatedOrFakePlayers");
        platform(level);platform(foreign);
        for(ManorDifficulty difficulty:ManorDifficulty.values())for(int rotation:new int[]{0,2})plans.add(new Plan(difficulty.name()+"-"+rotation,difficulty,rotation,"normal"));
        for(String action:new String[]{"cancel","queue","variant","giant","fallback","missing","discard","dimension"})plans.add(new Plan(action,ManorDifficulty.NORMAL,0,action));
        require(plans.size()==12,"TwelveDirectedCaptureNaturalTimelineCases");ready=true;
    }catch(Throwable failure){finish(failure);}}
    private static Cow cow(double horizontal,double depth){
        Cow animal=EntityType.COW.create(level);require(animal!=null,"ActualCowFactory");animal.setNoAi(true);animal.setNoGravity(true);animal.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);animal.setHealth(1000);animal.moveTo(horizontal,240,depth,0,0);animal.setOnGround(true);require(level.addFreshEntity(animal),"ActualCowAdmission");return animal;
    }
    private static void cast(int rotation){
        boss.setNoAi(false);boss.moveTo(8,240,8,0,0);target.moveTo(12,240,8,0,0);target.setOnGround(true);boss.setTarget(target);
        controller=new ManorFiveSkills(boss);controller.restoreRotation(rotation);controller.refreshAttackBonus();boss.getRandom().setSeed(4096L);
        require(boss.hasLineOfSight(target),"ActualDirectedStartLineOfSight");
        require(controller.tryStart(level,target)&&controller.isCasting(),"ActualOriginalControllerCapturesSeededShadowSkill");
        boss.setNoAi(true);
        TimelineObservation.Capture capture=TimelineObservation.captures.getLast();
        require(capture.snapshot().delay()==84&&capture.snapshot().skill().equals(rotation==0?Skill.SHADOW_CROSS.id():Skill.SHADOW_CAGE.id()),"ActualSeededOriginalDelayAndSkill");
        require(capture.snapshot().target().equals(target.getUUID())&&capture.cleared(),"ActualOriginalCaptureTransfersAndClears");
        positionTarget();
    }
    private static void prepare(){
        plan=plans.get(index);TimelineObservation.reset();acted=false;reverted=false;
        boss=FTEntities.EASTER_BOSS.get().create(level);require(boss!=null,"ActualBossFactory");boss.setVariant(7);boss.setChallengeDifficulty(plan.difficulty());boss.setNoGravity(true);boss.setNoAi(true);boss.moveTo(8,240,8,0,0);
        TimelineObservation.tracked.add(boss.getUUID());require(level.addFreshEntity(boss)&&level.getEntity(boss.getUUID())==boss,"BossActuallyAdmittedForNaturalEntityTicks");
        target=cow(12,8);control=cow(24,13);started=level.getGameTime();cast(plan.rotation());
    }
    private static void positionTarget(){
        if(target==null||target.isRemoved()||TimelineObservation.captures.isEmpty())return;
        TimelineObservation.Capture capture=TimelineObservation.captures.getLast();Skill skill=Skill.byId(capture.snapshot().skill());
        double forward=0;while(forward<24&&!ManorFiveCombatRules.contains(skill,0,forward,0,0,target.getBbWidth()*0.5))forward+=0.25;
        require(forward<24,"PublicOriginalEchoShapeHasActualLivingPoint");
        target.moveTo(capture.snapshot().horizontal()+forward,capture.snapshot().vertical(),capture.snapshot().depth(),0,0);
        target.setDeltaMovement(Vec3.ZERO);target.setHealth(target.getMaxHealth());target.invulnerableTime=0;
    }
    private static void actions(long age){
        if(!acted&&age>=5){acted=true;
            switch(plan.action()){
                case "cancel" -> {controller.cancel();require(!controller.isCasting(),"PublicControllerCancellationBeforeEchoDue");}
                case "queue" -> cast(2);
                case "variant" -> boss.setVariant(0);
                case "giant" -> boss.setGiant(true);
                case "fallback" -> {target.discard();target=cow(12,8);boss.setTarget(target);positionTarget();}
                case "missing" -> {target.discard();target=null;boss.setTarget(null);}
                case "discard" -> {boss.discard();require(level.getEntity(boss.getUUID())==null,"RemovedOwnerNoLongerInLevel");}
                case "dimension" -> {
                    var moved=boss.changeDimension(new DimensionTransition(foreign,new Vec3(8,240,8),Vec3.ZERO,0,0,DimensionTransition.DO_NOTHING));
                    require(moved instanceof EasterBossEntity&&moved.level()==foreign,"ActualPublicBossDimensionTransfer");boss=(EasterBossEntity)moved;boss.setNoAi(true);boss.setNoGravity(true);TimelineObservation.tracked.add(boss.getUUID());
                    require(level.getEntity(boss.getUUID())==null&&foreign.getEntity(boss.getUUID())==boss,"ActualTransferredOwnerOnlyInDestination");
                }
                default -> {}
            }
        }
        long due=TimelineObservation.captures.getLast().due();
        if(!reverted&&level.getGameTime()>due+2){reverted=true;if(plan.action().equals("variant"))boss.setVariant(7);if(plan.action().equals("giant"))boss.setGiant(false);}
    }
    private static void verifyCase(){
        int expected=switch(plan.action()){case "variant","giant","discard","dimension" -> 0;case "queue" -> 2;default -> 1;};
        require(TimelineObservation.violations.isEmpty(),"ObservedNativeTimelineInvariants:"+TimelineObservation.violations);
        require(TimelineObservation.captures.size()==(plan.action().equals("queue")?2:1),"OriginalCapturedQueueLength");
        List<TimelineObservation.Light> echoes=TimelineObservation.lights.stream().filter(value->value.kind()==9).toList();
        require(echoes.size()==expected,"OriginalExactlyOnceOrInvalidatedEchoCount:"+plan.name()+":"+echoes.size());
        require(TimelineObservation.lights.stream().allMatch(TimelineObservation.Light::codec),"EveryOriginalLightActualNativeCodecRoundtrip");
        require(TimelineObservation.lights.size()==expected*2,"OneEchoAndOneOriginalHitShapePayloadPerDueEntry");
        for(int sequence=0;sequence<echoes.size();sequence++){
            var capture=TimelineObservation.captures.get(sequence);var echo=echoes.get(sequence);
            require(echo.now()==capture.due()&&echo.owner().equals(capture.owner()),"NaturalNativeGameTimeDueAndStableQueueOrder");
            require(echo.origin().equals(new Vec3(capture.snapshot().horizontal(),capture.snapshot().vertical(),capture.snapshot().depth()).toString()),"OriginalCapturedOriginSurvivesCountdown");
            int pulseKind=capture.snapshot().skill().equals(Skill.SHADOW_CROSS.id())?1:3;
            require(TimelineObservation.lights.stream().anyMatch(value->value.kind()==pulseKind&&value.now()==echo.now()&&value.index()==0),"OriginalSinglePulseZeroEchoShape");
        }
        int expectedHits=plan.action().equals("missing")?0:expected;
        require(TimelineObservation.damages.size()==expectedHits,"OriginalLivingDamageOrRemovedTargetControl:"+plan.name()+":"+TimelineObservation.damages);
        for(var damage:TimelineObservation.damages){require(damage.returned()&&damage.before()>damage.after(),"ActualNativeEchoReducesLivingHealth");require(Math.abs(damage.amount()-damage.expected())<0.001,"OriginalQuarterMultiplierAtUnchangedAdaptiveHelperBoundary");require(target!=null&&damage.victim().equals(target.getUUID()),"ActualOriginalLivingTargetOrEligibleReplacementIdentity");}
        require(control.getHealth()==1000,"UnrelatedLivingControlUnaffected");
        require(TimelineObservation.ticks.values().stream().mapToInt(Integer::intValue).sum()>=4,"OriginalHookObservedOnlyOnAdmittedNaturallyTickedBoss");
        Map<String,Object> row=new LinkedHashMap<>();row.put("name",plan.name());row.put("difficulty",plan.difficulty().name());row.put("action",plan.action());row.put("captures",List.copyOf(TimelineObservation.captures));row.put("lights",List.copyOf(TimelineObservation.lights));row.put("damages",List.copyOf(TimelineObservation.damages));row.put("nativeHookCounts",new LinkedHashMap<>(TimelineObservation.ticks));row.put("bossTickCount",boss.tickCount);row.put("tailAfterLastDue",level.getGameTime()-TimelineObservation.captures.getLast().due());cases.add(row);
        boss.discard();if(target!=null)target.discard();control.discard();index++;
        if(index==plans.size())finish(null);else prepare();
    }
    private static void tick(ServerTickEvent.Post event){if(finished||!ready)return;try{
        callbacks++;require(callbacks<4000,"BoundedNaturalServerTickFixture");
        if(boss==null){if(!level.isPositionEntityTicking(new BlockPos(8,240,8))||!foreign.isPositionEntityTicking(new BlockPos(8,240,8)))return;prepare();return;}
        long age=level.getGameTime()-started;actions(age);positionTarget();
        for(var echo:TimelineObservation.lights)require(TimelineObservation.captures.stream().anyMatch(capture->capture.due()==echo.now()),"NoEarlyOrUnscheduledReplay");
        if(level.getGameTime()>=TimelineObservation.captures.getLast().due()+15)verifyCase();
    }catch(Throwable failure){finish(failure);}}
    private static void finish(Throwable failure){
        if(finished)return;finished=true;Map<String,Object> report=new LinkedHashMap<>();report.put("failures",failure==null?0:1);report.put("failure",failure==null?null:failure.toString());report.put("checks",checks);report.put("cases",cases);report.put("completedCases",index);report.put("naturalPostCallbacks",callbacks);report.put("partialCaptures",TimelineObservation.captures);report.put("partialLights",TimelineObservation.lights);report.put("partialDamages",TimelineObservation.damages);report.put("violations",TimelineObservation.violations);report.put("naturalEntityTimelineSchedulingAccepted",failure==null&&index==12);report.put("manualTickRestoreReplayOrPhantomHookInvocation",false);report.put("naturalBossAiLoopAccepted",false);report.put("authenticatedMultiplayerAccepted",false);report.put("clientDeliveryOrRenderingAccepted",false);report.put("fullBossMigrationComplete",false);report.put("scope","Actual admitted Boss natural entity tick and original PhantomHooks queue/countdown; public seeded controller entry,NoAI,platform,health/hurt reset fixtures. Original NoAI/cancel continuation,stable multiple queue entries,variant/giant invalidation,target fallback/removal,owner discard and real public dimension transfer. No direct tick,restore,replay or phantom hook call;no authenticated or full AI/battle claim.");
        try{Files.writeString(Path.of("actual-phantom-timeline-probe.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}catch(Exception problem){problem.printStackTrace();}
    }
}
