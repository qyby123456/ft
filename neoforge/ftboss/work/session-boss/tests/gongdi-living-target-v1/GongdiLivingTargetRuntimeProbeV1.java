package ftboss.bossprobe;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.boss.ai.GongdiSkills;
import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@Mod("ftboss_bossprobe")
public final class GongdiLivingTargetRuntimeProbeV1 {
    private static final List<String> checks=new ArrayList<>();
    private static final List<Map<String,Object>> rows=new ArrayList<>();
    private static void require(boolean value,String name) {if(!value) throw new AssertionError(name);checks.add(name);}
    public GongdiLivingTargetRuntimeProbeV1() {NeoForge.EVENT_BUS.addListener(GongdiLivingTargetRuntimeProbeV1::start);}
    private static void start(ServerStartedEvent event) {
        Map<String,Object> report=new LinkedHashMap<>();Throwable failure=null;
        boolean candidate=System.getProperty("ftboss.gongditarget.mode","baseline").equals("candidate");
        try {
            ServerLevel level=event.getServer().overworld();level.setChunkForced(0,0,true);level.getChunk(0,0);level.getChunk(1,0);
            require(event.getServer().getPlayerList().getPlayerCount()==0,"NoAuthenticatedPlayersOrFakePlayerFixture");
            for(double distance:new double[]{4.0,12.0,15.0}) {
                EasterBossEntity boss=FTEntities.EASTER_BOSS.get().create(level);require(boss!=null,"ActualRegisteredBossFactory:"+distance);
                boss.setVariant(3);boss.setNoAi(false);boss.setNoGravity(true);boss.moveTo(1.0,100.0,1.0,0.0F,0.0F);
                Cow target=EntityType.COW.create(level);require(target!=null,"ActualVanillaLivingTargetFactory:"+distance);
                target.setNoAi(true);target.setNoGravity(true);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);target.setHealth(1000.0F);target.moveTo(1.0+distance,100.0,1.0,0.0F,0.0F);
                require(level.addFreshEntity(target),"ActualTargetNativeAdmission:"+distance);
                require(ManorLivingTargets.valid(boss,target),"OriginalLivingTargetPolicyAcceptsRealCow:"+distance);
                require(boss.hasLineOfSight(target),"ActualOriginalTargetLineOfSight:"+distance);
                GongdiSkills skills=new GongdiSkills(boss);boolean started=skills.tryStart(level,target);
                require(started,"ControllerReturnClaimsOriginalTargetHandled:"+distance);
                require(skills.isCasting()==candidate,"ActualControllerCastStartedForLivingTarget:"+distance+":"+skills.isCasting());
                require(boss.getSkillCue().contains("gongdiSkill")==candidate,"ActualNativeSkillCueForLivingTarget:"+distance);
                if(candidate) require(boss.getSkillCue().getString("gongdiSkill").equals(distance<10.125?"guillotine":"apprehend"),"OriginalSkillSelectionByLivingTargetRange:"+distance);
                Map<String,Object> row=new LinkedHashMap<>();row.put("distance",distance);row.put("targetUuid",target.getUUID().toString());row.put("actualPolicyValid",true);row.put("hasLineOfSight",true);row.put("returnedHandled",started);row.put("actuallyCasting",skills.isCasting());row.put("actualCue",boss.getSkillCue().toString());rows.add(row);
                skills.reset();target.discard();
            }
            report.put("actualOriginalLivingTargetGapDemonstrated",!candidate);report.put("livingTargetEntryAccepted",candidate);
        } catch(Throwable problem) {failure=problem;problem.printStackTrace();}
        try {
            report.put("cases",checks);report.put("rows",rows);report.put("failures",failure==null?0:1);if(failure!=null)report.put("failure",failure.toString());
            report.put("scope","Directed actual vanilla Cow target policy/LOS/native admission and public Gongdi controller tryStart/cue,three original ranges. No manual entity ticks,reflection,fake player or authentication claim. Full skill damage/natural encounter acceptance separate.");
            report.put("fullBossMigrationComplete",false);report.put("deployed",false);
            Files.writeString(Path.of("actual-gongdi-target-probe.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch(Exception writing) {writing.printStackTrace();}
    }
}
