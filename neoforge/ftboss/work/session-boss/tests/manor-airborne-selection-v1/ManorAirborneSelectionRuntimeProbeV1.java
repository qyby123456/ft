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
import net.farmingtales.ftboss.util.EasterCombatMath;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
@Mod("ftboss_bossprobe")
public final class ManorAirborneSelectionRuntimeProbeV1 {
    private static final List<String> checks=new ArrayList<>();
    private static final List<Map<String,Object>> rows=new ArrayList<>();
    private static void require(boolean value,String text){if(!value)throw new AssertionError(text);checks.add(text);}
    private static int duration(EasterBossEntity boss,Skill skill){int age=skill.windup-boss.getChallengeDifficulty().acceleratedTicks(skill.windup);return Math.max(1,skill.duration()-age);}
    public ManorAirborneSelectionRuntimeProbeV1(){NeoForge.EVENT_BUS.addListener(ManorAirborneSelectionRuntimeProbeV1::start);}
    private static void start(ServerStartedEvent event){Map<String,Object> result=new LinkedHashMap<>();Throwable failure=null;int differences=0,emptyDifferences=0,casts=0;Set<Integer> variants=new TreeSet<>();boolean candidate=System.getProperty("ftboss.manorairborne.mode","baseline").equals("candidate");try{
        ServerLevel level=event.getServer().overworld();level.setChunkForced(0,0,true);level.getChunk(0,0);require(event.getServer().getPlayerList().getPlayerCount()==0,"NoAuthenticatedOrFakePlayerFixture");
        for(int variant=0;variant<9;variant++)if(ManorFiveCombatRules.hasExpandedSkills(variant))for(int rotation=0;rotation<ManorFiveCombatRules.rotationLength(variant);rotation++)for(boolean grounded:new boolean[]{true,false}){
            variants.add(variant);String label=variant+":"+rotation+":"+(grounded?"grounded":"airborne");EasterBossEntity boss=FTEntities.EASTER_BOSS.get().create(level);require(boss!=null,"ActualBossFactory:"+label);boss.setVariant(variant);boss.setNoAi(false);boss.setNoGravity(true);boss.moveTo(8,320,8,0,0);
            Cow target=EntityType.COW.create(level);require(target!=null,"ActualCowFactory:"+label);target.setNoAi(true);target.setNoGravity(true);target.moveTo(12,320,8,0,0);require(level.addFreshEntity(target),"ActualCowNativeAdmission:"+label);target.setOnGround(grounded);require(ManorLivingTargets.airborne(target)==!grounded,"ActualPublicGroundFlagAndNativeFluidClimbPredicate:"+label);require(ManorLivingTargets.valid(boss,target)&&boss.hasLineOfSight(target),"OriginalPolicyAndActualLineOfSight:"+label);boss.setTarget(target);
            Skill selected=ManorFiveCombatRules.select(variant,rotation);Skill original=ManorLivingTargets.airborneFive(selected,target,variant);Skill expected=candidate?original:selected;ManorFiveSkills controller=new ManorFiveSkills(boss);controller.restoreRotation(rotation);boolean started=controller.tryStart(level,target);require(started==(expected!=null)&&controller.isCasting()==started,"ActualOriginalAirborneEntryBoundary:"+label);require(controller.rotation()==(rotation+1)%ManorFiveCombatRules.rotationLength(variant),"OriginalRotationAdvanceIncludingEmptySlots:"+label);
            float progress=boss.getActionProgress(1.0F);if(expected!=null){casts++;float wanted=EasterCombatMath.progress(1.0F,duration(boss,expected),false);require(Math.abs(progress-wanted)<0.000001F,"ActualPublicAnimationDurationIdentifiesSelectedSkill:"+label);boolean cue=expected.shape==ManorFiveCombatRules.Shape.CIRCLE||expected.shape==ManorFiveCombatRules.Shape.WAVE;require(!boss.getSkillCue().isEmpty()==cue,"ActualSelectedSkillOriginalShapeCue:"+label);}
            if(selected!=original){differences++;boolean actualDiff=started!=(original!=null)||(original!=null&&Math.abs(progress-EasterCombatMath.progress(1.0F,duration(boss,original),false))>0.000001F);require(actualDiff!=candidate,"ActualBaselineGapOrRestoredOriginalSelection:"+label);if(selected==null){emptyDifferences++;require(started==candidate,"ActualAirborneFillsOriginalEmptySlot:"+label);}}
            if(grounded)require(selected==original,"GroundedOriginalSelectionIdentity:"+label);Map<String,Object> row=new LinkedHashMap<>();row.put("variant",variant);row.put("rotation",rotation);row.put("groundedPublicFixture",grounded);row.put("baselineSkill",selected==null?null:selected.name());row.put("originalSkill",original==null?null:original.name());row.put("actualStarted",started);row.put("actualPublicAnimationProgress",progress);row.put("selectionDiffersFromStage19",selected!=original);rows.add(row);controller.reset();target.discard();
        }
        require(variants.equals(Set.of(0,1,4,5,6,7)),"AllSixExpandedVariants");require(rows.size()==52,"AllGroundedAndAirborneOriginalRotationSlots");require(differences==12&&emptyDifferences==8,"OriginalTwelveSelectionDifferencesAndEightAirborneEmptySlots");require(casts==(candidate?34:26),"ActualExpectedNativeCastCount");
        EasterBossEntity boss=FTEntities.EASTER_BOSS.get().create(level);require(boss!=null,"NegativeActualBossFactory");boss.setVariant(0);boss.setNoAi(false);boss.moveTo(8,320,8,0,0);ManorFiveSkills skills=new ManorFiveSkills(boss);require(!skills.tryStart(level,null)&&!skills.tryStart(level,boss),"OriginalNullAndSelfPolicyStillRejects");require(skills.rotation()==0&&!skills.isCasting(),"RejectedTargetsDoNotAdvanceOriginalRotation");
        result.put("actualManorAirborneSelectionGapDemonstrated",!candidate);result.put("manorAirborneSelectionAccepted",candidate);
    }catch(Throwable problem){failure=problem;problem.printStackTrace();}try{result.put("cases",checks);result.put("rows",rows);result.put("selectionDifferences",differences);result.put("emptyAirborneDifferences",emptyDifferences);result.put("actualCasts",casts);result.put("failures",failure==null?0:1);if(failure!=null)result.put("failure",failure.toString());result.put("scope","Original airborneFive selection across all52 grounded/airborne rotation slots and6variants;actual native Cow admission,public setOnGround state fixture,native fluid/climb predicates,public controller entry and actual public animation/cue observations. No private reflection,manual Entity.tick,authenticated fixture or natural Boss AI/full battle claim.");result.put("fullBossMigrationComplete",false);Files.writeString(Path.of("actual-manor-airborne-probe.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));}catch(Exception writing){writing.printStackTrace();}}
}
