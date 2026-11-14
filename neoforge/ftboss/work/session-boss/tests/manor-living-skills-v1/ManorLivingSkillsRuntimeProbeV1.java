package ftboss.bossprobe;
import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;
import net.farmingtales.ftboss.boss.ai.ManorFiveSkills;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.util.ManorFiveCombatRules;
import net.farmingtales.ftboss.util.ManorFiveCombatRules.Skill;
import net.farmingtales.ftboss.util.ManorFiveCombatRules.Shape;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
@Mod("ftboss_bossprobe")
public final class ManorLivingSkillsRuntimeProbeV1 {
    private record Plan(int variant,int rotation,Skill skill){}
    private static final List<Plan> plans=new ArrayList<>();
    private static final List<String> checks=new ArrayList<>();
    private static final List<Map<String,Object>> rows=new ArrayList<>();
    private static ServerLevel level;
    private static EasterBossEntity boss;
    private static Cow target,control;
    private static ManorFiveSkills controller;
    private static Plan plan;
    private static Vec3 origin;
    private static int index,age,callbacks,hits,phase;
    private static float damageTotal;
    private static boolean finished;
    public ManorLivingSkillsRuntimeProbeV1(){NeoForge.EVENT_BUS.addListener(ManorLivingSkillsRuntimeProbeV1::start);NeoForge.EVENT_BUS.addListener(ManorLivingSkillsRuntimeProbeV1::tick);}
    private static void require(boolean value,String name){if(!value)throw new AssertionError(name);checks.add(name);}
    private static Cow cow(double x,double z){Cow entity=EntityType.COW.create(level);require(entity!=null,"ActualCowFactory");entity.setNoAi(true);entity.setNoGravity(true);entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);entity.setHealth(1000.0F);entity.moveTo(x,100.0,z,0,0);require(level.addFreshEntity(entity),"ActualCowNativeAdmission");return entity;}
    private static void prepare(){plan=plans.get(index);boss=FTEntities.EASTER_BOSS.get().create(level);require(boss!=null,"ActualBossFactory:"+plan.skill);boss.setVariant(plan.variant);boss.setNoAi(false);boss.setNoGravity(true);boss.moveTo(8,100,8,0,0);target=cow(12,8);control=cow(48,48);boss.setTarget(target);control.setTarget(boss);controller=new ManorFiveSkills(boss);controller.restoreRotation(plan.rotation);controller.refreshAttackBonus();require(ManorLivingTargets.valid(boss,target),"OriginalLivingPolicyAccepts:"+plan.skill);require(level.getEntity(target.getUUID())==target,"ActualUuidResolvesTarget:"+plan.skill);require(ManorLivingTargets.nearby(level,boss).contains(target)&&ManorLivingTargets.nearby(level,boss).contains(control),"ActualNearbyIncludesTargetAndControl:"+plan.skill);require(boss.hasLineOfSight(target),"ActualTargetLineOfSight:"+plan.skill);require(controller.tryStart(level,target)&&controller.isCasting(),"OriginalLivingSkillCastStarts:"+plan.skill);age=plan.skill.windup-boss.getChallengeDifficulty().acceleratedTicks(plan.skill.windup);origin=plan.skill.shape==Shape.WAVE||plan.skill.shape==Shape.BEAM||plan.skill.shape==Shape.CONE?boss.position():target.position();hits=0;damageTotal=0;}
    private static void finish(Throwable failure){if(finished)return;finished=true;Map<String,Object> report=new LinkedHashMap<>();report.put("cases",checks);report.put("rows",rows);report.put("plans",plans.stream().map(value->value.skill.name()).toList());report.put("completedSkills",rows.size());report.put("naturalPostCallbacks",callbacks);report.put("failures",failure==null?0:1);if(failure!=null){failure.printStackTrace();report.put("failure",failure.toString());}report.put("manorLivingSkillExecutionAccepted",failure==null);report.put("scope","Actual admitted Cow targets/native UUID and original combat-query inclusion,each current public expanded skill rotation across six variants,public controller tick on natural ServerTick.Post callbacks;actual damage with public health reset before directed pulse checks,all pulses,effects/range control,completion and removed/noAI interruption. Existing numeric/cadence/shape instructions preserved by ASM;original FX/echo/airborne/balance parity remains separate. No manual Entity.tick,private reflection,FakPlayer/authenticated or natural Boss AI claim.");report.put("fullBossMigrationComplete",false);try{Files.writeString(Path.of("actual-manor-skills-probe.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}catch(Exception problem){problem.printStackTrace();}}
    private static void policyControls(ServerStartedEvent event){EasterBossEntity actor=FTEntities.EASTER_BOSS.get().create(level);require(actor!=null,"PolicyControlActualBossFactory");actor.setVariant(0);actor.setNoAi(false);actor.moveTo(8,100,8,0,0);ManorFiveSkills skills=new ManorFiveSkills(actor);require(!skills.tryStart(level,null),"OriginalPolicyRejectsNull");require(!skills.tryStart(level,actor),"OriginalPolicyRejectsSelf");ArmorStand stand=EntityType.ARMOR_STAND.create(level);require(stand!=null,"ActualArmorStandFactory");stand.moveTo(12,100,8,0,0);require(!skills.tryStart(level,stand),"OriginalPolicyRejectsArmorStand");ServerLevel foreign=event.getServer().getLevel(net.minecraft.world.level.Level.NETHER);require(foreign!=null,"ActualOtherNativeLevel");Cow foreignCow=EntityType.COW.create(foreign);require(foreignCow!=null,"ActualForeignCowFactory");require(!skills.tryStart(level,foreignCow),"OriginalPolicyRejectsForeignLevelLiving");Cow eligible=cow(12,8);actor.setTarget(eligible);require(ManorLivingTargets.valid(actor,eligible),"PolicyPositiveCowIsActuallyValid");actor.setNoAi(true);require(!skills.tryStart(level,eligible),"ExistingNoAiRejectsLivingEntry");require(!skills.isCasting()&&skills.rotation()==0&&actor.getSkillCue().isEmpty(),"RejectedTargetsDoNotAdvanceRotationOrCreateCue");eligible.discard();stand.discard();foreignCow.discard();}
    private static void start(ServerStartedEvent event){try{level=event.getServer().overworld();for(int[] chunk:new int[][]{{0,0},{1,0},{3,3}}){level.setChunkForced(chunk[0],chunk[1],true);level.getChunk(chunk[0],chunk[1]);}require(event.getServer().getPlayerList().getPlayerCount()==0,"NoAuthenticatedOrFakePlayerFixture");for(int variant=0;variant<9;variant++)if(ManorFiveCombatRules.hasExpandedSkills(variant))for(int rotation=0;rotation<ManorFiveCombatRules.rotationLength(variant);rotation++){Skill selected=ManorFiveCombatRules.select(variant,rotation);if(selected!=null)plans.add(new Plan(variant,rotation,selected));}require(plans.stream().map(value->value.variant).distinct().count()==6,"ActualExpandedSixVariantMatrix");policyControls(event);prepare();}catch(Throwable failure){finish(failure);}}
    private static Vec3 point(int pulse){Vec3 center=origin;if(plan.skill.shape==Shape.ECHO){double angle=pulse*Math.PI*2/3;center=center.add(Math.cos(angle)*3,0,Math.sin(angle)*3);}double limit=Math.max(32,plan.skill.radius*2+16);for(double forward=0;forward<=limit;forward+=0.25)if(ManorFiveCombatRules.contains(plan.skill,pulse,forward,0,0,target.getBbWidth()*0.5))return center.add(forward,0,0);throw new AssertionError("No public-rule geometry point:"+plan.skill+":"+pulse);}
    private static void tick(ServerTickEvent.Post event){if(finished||level==null)return;try{callbacks++;require(callbacks<5000,"BoundedNaturalServerPost:"+callbacks);
        if(phase==0){age++;int pulse=ManorFiveCombatRules.pulseAt(plan.skill,age);Vec3 position=pulse>=0?point(pulse):new Vec3(12,100,8);target.moveTo(position.x,position.y,position.z,0,0);target.setDeltaMovement(Vec3.ZERO);target.setHealth(target.getMaxHealth());float before=target.getHealth();controller.tick(level);if(pulse>=0){float actual=before-target.getHealth();require(actual>0,"ActualLivingSkillDamage:"+plan.skill+":"+pulse);require(!target.getActiveEffects().isEmpty(),"ActualLivingSkillEffects:"+plan.skill+":"+pulse);damageTotal+=actual;hits++;}require(control.getHealth()==1000.0F&&control.getActiveEffects().isEmpty(),"OutsideOriginalRangeControlUnaffected:"+plan.skill+":"+age);if(!controller.isCasting()){require(hits==plan.skill.pulses,"EveryOriginalCurrentPulseHitsActualLiving:"+plan.skill+":"+hits);require(age>=plan.skill.duration(),"ActualControllerCompletesAtExistingDuration:"+plan.skill);Map<String,Object> row=new LinkedHashMap<>();row.put("variant",plan.variant);row.put("rotation",plan.rotation);row.put("skill",plan.skill.name());row.put("actualPulseHits",hits);row.put("expectedExistingPulses",plan.skill.pulses);row.put("actualSkillDamage",damageTotal);row.put("existingDurationAge",age);rows.add(row);target.discard();control.discard();index++;if(index<plans.size())prepare();else{index=0;prepare();target.discard();phase=1;}}}
        else if(phase==1){controller.tick(level);require(!controller.isCasting(),"RemovedLivingTargetCancelsExpandedCast");control.discard();prepare();boss.setNoAi(true);phase=2;}
        else{float before=target.getHealth();controller.tick(level);require(!controller.isCasting(),"NoAiCancelsExpandedLivingCast");require(target.getHealth()==before,"InterruptedExpandedCastNoLivingDamage");target.discard();control.discard();finish(null);}
    }catch(Throwable failure){finish(failure);}}
}
