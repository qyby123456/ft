package ftboss.bossprobe;
import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;
import net.farmingtales.ftboss.balance.ManorBalanceHooks;
import net.farmingtales.ftboss.zheergenphantom.CastReadability;
import net.farmingtales.ftboss.boss.ManorDifficulty;
import net.farmingtales.ftboss.boss.ai.ManorFiveSkills;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.util.ManorFiveCombatRules;
import net.farmingtales.ftboss.util.ManorFiveCombatRules.Skill;
import net.farmingtales.ftboss.util.EasterCombatMath;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
@Mod("ftboss_bossprobe")
public final class ManorOriginalTimingRuntimeProbeV1 {
    private static final List<String> checks=new ArrayList<>();
    private static final List<Map<String,Object>> rows=new ArrayList<>();
    private static void require(boolean value,String text){if(!value)throw new AssertionError(text);checks.add(text);}
    public ManorOriginalTimingRuntimeProbeV1(){NeoForge.EVENT_BUS.addListener(ManorOriginalTimingRuntimeProbeV1::start);}
    private static void start(ServerStartedEvent event){Map<String,Object> result=new LinkedHashMap<>();Throwable failure=null;int differences=0;boolean candidate=System.getProperty("ftboss.manortiming.mode","baseline").equals("candidate");try{
        ServerLevel level=event.getServer().overworld();level.setChunkForced(0,0,true);level.getChunk(0,0);require(event.getServer().getPlayerList().getPlayerCount()==0,"NoAuthenticatedOrFakePlayerFixture");require(ManorDifficulty.values().length==2,"ActualNormalAndImpossibleDifficulties");
        for(ManorDifficulty difficulty:ManorDifficulty.values())for(int variant=0;variant<9;variant++)if(ManorFiveCombatRules.hasExpandedSkills(variant))for(int rotation=0;rotation<ManorFiveCombatRules.rotationLength(variant);rotation++){
            Skill skill=ManorFiveCombatRules.select(variant,rotation);if(skill==null)continue;String label=difficulty+":"+skill;EasterBossEntity boss=FTEntities.EASTER_BOSS.get().create(level);require(boss!=null,"ActualBossFactory:"+label);boss.setVariant(variant);boss.setChallengeDifficulty(difficulty);require(boss.getChallengeDifficulty()==difficulty,"ActualPublicDifficulty:"+label);boss.setNoAi(false);boss.setNoGravity(true);boss.moveTo(8,320,8,0,0);
            Cow target=EntityType.COW.create(level);require(target!=null,"ActualCowFactory:"+label);target.setNoAi(true);target.setNoGravity(true);target.moveTo(12,320,8,0,0);require(level.addFreshEntity(target),"ActualCowNativeAdmission:"+label);target.setOnGround(true);require(!net.farmingtales.ftboss.boss.ai.ManorLivingTargets.airborne(target),"ExplicitGroundedTargetChoiceIdentity:"+label);boss.setTarget(target);
            int oldAge=skill.windup-difficulty.acceleratedTicks(skill.windup);int originalAge=ManorBalanceHooks.fiveAge(CastReadability.fiveAge(oldAge,skill),skill);int observedAge=candidate?originalAge:oldAge;ManorFiveSkills controller=new ManorFiveSkills(boss);controller.restoreRotation(rotation);require(controller.tryStart(level,target)&&controller.isCasting(),"ActualControllerStartsCurrentSkill:"+label);float progress=boss.getActionProgress(1.0F);float expected=EasterCombatMath.progress(1.0F,Math.max(1,skill.duration()-observedAge),false);require(Math.abs(progress-expected)<0.000001F,"ActualPublicAnimationBindsCastTiming:"+label);
            if(oldAge!=originalAge){differences++;float originalProgress=EasterCombatMath.progress(1.0F,Math.max(1,skill.duration()-originalAge),false);require((Math.abs(progress-originalProgress)<0.000001F)==candidate,"ActualOriginalTimingGapOrRestoration:"+label);}else require(observedAge==originalAge,"UnaffectedSkillTimingIdentity:"+label);
            Map<String,Object> row=new LinkedHashMap<>();row.put("difficulty",difficulty.name());row.put("variant",variant);row.put("rotation",rotation);row.put("skill",skill.name());row.put("stage20Age",oldAge);row.put("originalAge",originalAge);row.put("timingDiffersFromStage20",oldAge!=originalAge);row.put("actualPublicAnimationProgress",progress);rows.add(row);controller.reset();target.discard();
        }
        require(rows.size()==26,"All13SkillsAcrossBothDifficulties");require(differences>0,"ActualOriginalTimingDifferenceObserved");result.put("actualManorOriginalTimingGapDemonstrated",!candidate);result.put("manorOriginalTimingAccepted",candidate);
    }catch(Throwable problem){failure=problem;problem.printStackTrace();}try{result.put("cases",checks);result.put("rows",rows);result.put("timingDifferences",differences);result.put("failures",failure==null?0:1);if(failure!=null)result.put("failure",failure.toString());result.put("scope","Actual native public controller casts and public animation timing for all13expanded skill types across normal/impossible;explicit public grounded-state fixture selects each skill. Original helper timing compared;no private reflection,manual Entity.tick,authenticated or full natural Boss battle claim.");result.put("fullBossMigrationComplete",false);Files.writeString(Path.of("actual-manor-timing-probe.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));}catch(Exception writing){writing.printStackTrace();}}
}
