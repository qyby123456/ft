package ftboss.bossprobe;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.boss.ai.GongdiSkills;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("ftboss_bossprobe")
public final class GongdiLifecycleRuntimeProbeV2 {
    private static final List<String> checks=new ArrayList<>();
    private static final Map<String,Object> report=new LinkedHashMap<>();
    private static ServerLevel level;
    private static boolean candidate;
    public GongdiLifecycleRuntimeProbeV2() {NeoForge.EVENT_BUS.addListener(GongdiLifecycleRuntimeProbeV2::start);}
    private static void require(boolean condition,String name) {if(!condition) throw new AssertionError(name);checks.add(name);}
    private static EasterBossEntity boss(int variant) {
        EasterBossEntity entity=FTEntities.EASTER_BOSS.get().create(level);
        require(entity!=null,"ActualRegisteredBossFactory");entity.setVariant(variant);entity.setNoAi(false);
        entity.moveTo(2.0,100.0,2.0,0.0F,0.0F);entity.setNoGravity(true);return entity;
    }
    private static void begin(EasterBossEntity entity) {entity.beginGongdiCast("strike",20,4.5,new Vec3(1.0,0.0,0.0));}
    private static boolean pending(EasterBossEntity entity) {return entity.getSkillCue().contains("gongdiSkill") && !entity.getSkillCue().getBoolean("released");}
    private static void start(ServerStartedEvent event) {
        Throwable failure=null;
        try {
            candidate=System.getProperty("ftboss.gongdilifecycle.mode","baseline").equals("candidate");
            level=event.getServer().overworld();level.getChunk(0,0);
            require(event.getServer().getPlayerList().getPlayerCount()==0,"NoAuthenticatedPlayersClaimed");
            EasterBossEntity positive=boss(3);begin(positive);require(pending(positive),"EligibleOriginalCastStarts");
            require(positive.getSkillCue().getString("gongdiSkill").equals("strike"),"OriginalSkillNamePreserved");
            require(positive.getSkillCue().getDouble("directionX")==1.0 && positive.getSkillCue().getDouble("directionZ")==0.0,"OriginalDirectionPreserved");
            positive.finishGongdiCast();require(positive.getSkillCue().getBoolean("released"),"EligibleOriginalCastReleases");
            new GongdiSkills(positive).reset();require(positive.getSkillCue().getBoolean("released"),"ReleasedCastNotCancelledByReset");
            EasterBossEntity wrong=boss(2);CompoundTag before=wrong.getSkillCue().copy();begin(wrong);
            require(candidate?before.equals(wrong.getSkillCue()):pending(wrong),"WrongVariantStartBoundary");
            EasterBossEntity mini=boss(3);mini.setMini(true);before=mini.getSkillCue().copy();begin(mini);
            require(candidate?before.equals(mini.getSkillCue()):pending(mini),"MiniStartBoundary");
            EasterBossEntity disabled=boss(3);disabled.setNoAi(true);before=disabled.getSkillCue().copy();begin(disabled);
            require(candidate?before.equals(disabled.getSkillCue()):pending(disabled),"NoAiStartBoundary");
            EasterBossEntity dead=boss(3);dead.setHealth(0.0F);before=dead.getSkillCue().copy();begin(dead);
            require(candidate?before.equals(dead.getSkillCue()):pending(dead),"DeadStartBoundary");
            EasterBossEntity idle=boss(3);before=idle.getSkillCue().copy();idle.finishGongdiCast();
            require(before.equals(idle.getSkillCue()),"IdleFinishDoesNotCreateCue");
            require(idle.getActionTicks()==(candidate?0:18),"IdleFinishDoesNotPlayImpactAction");
            EasterBossEntity reset=boss(3);begin(reset);require(pending(reset),"PendingCastBeforeReset");new GongdiSkills(reset).reset();
            require(candidate?reset.getSkillCue().isEmpty():pending(reset),"ControllerResetCancelsUnreleasedCue");
            EasterBossEntity interrupted=boss(3);begin(interrupted);interrupted.setNoAi(true);interrupted.finishGongdiCast();
            require(candidate?interrupted.getSkillCue().isEmpty():interrupted.getSkillCue().getBoolean("released"),"DisabledMidCastFinishCancelsInsteadOfRelease");
            EasterBossEntity ticked=boss(3);begin(ticked);ticked.setNoAi(true);new GongdiSkills(ticked).tick(level);
            require(candidate?ticked.getSkillCue().isEmpty():pending(ticked),"ControllerTickInvalidActorCancelsPendingCue");
            EasterBossEntity foreign=boss(3);begin(foreign);foreign.setNoAi(true);before=foreign.getSkillCue().copy();
            ServerLevel other=event.getServer().getLevel(Level.NETHER);require(other!=null && other!=level,"ActualOtherServerLevel");new GongdiSkills(foreign).tick(other);
            require(before.equals(foreign.getSkillCue()),"ForeignLevelControllerDoesNotMutateCue");
            Cow target=new Cow(net.minecraft.world.entity.EntityType.COW,level);target.moveTo(3.0,100.0,2.0,0.0F,0.0F);
            EasterBossEntity startGuard=boss(3);startGuard.setNoAi(true);before=startGuard.getSkillCue().copy();
            require(new GongdiSkills(startGuard).tryStart(level,target)==!candidate,"DisabledControllerTryStartRejects");
            require(before.equals(startGuard.getSkillCue()),"RejectedControllerStartDoesNotMutateCue");
            require(new GongdiSkills(boss(3)).tryStart(level,null)==!candidate,"NullTargetControllerTryStartRejects");
            require(new GongdiSkills(boss(3)).tryStart(other,target)==!candidate,"ForeignLevelControllerTryStartRejects");
            report.put("actualOriginalLifecycleGapDemonstrated",!candidate);report.put("lifecycleFeatureAccepted",candidate);
        } catch(Throwable problem) {failure=problem;problem.printStackTrace();}
        try {
            report.put("failures",failure==null?0:1);if(failure!=null)report.put("failure",failure.toString());
            report.put("scope","Original lifecycle guards only;public cast/controller methods,no manual Entity.tick/reflection/authenticated or full fight claim. Living-target widening and skill damage parity remain pending.");
            report.put("mode",candidate?"candidate":"baseline");report.put("cases",checks);report.put("fullBossMigrationComplete",false);report.put("deployed",false);
            Files.writeString(Path.of("actual-gongdi-lifecycle-probe.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch(Exception writing) {writing.printStackTrace();}
    }
}
