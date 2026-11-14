package ftboss.bossprobe;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import net.farmingtales.ftboss.boss.ai.GongdiSkills;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.util.GongdiCombatRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod("ftboss_bossprobe")
public final class GongdiSkillsRuntimeProbeV1 {
    private static final List<String> checks=new ArrayList<>();
    private static final List<Map<String,Object>> rows=new ArrayList<>();
    private static ServerLevel level;
    private static EasterBossEntity boss;
    private static Cow target;
    private static Cow outside;
    private static GongdiSkills skills;
    private static GongdiCombatRules.Bleed oracle;
    private static int phase,ticks,phaseTicks;
    private static float beforeHealth,expectedDamage,oracleDamage;
    private static boolean done;
    public GongdiSkillsRuntimeProbeV1(){NeoForge.EVENT_BUS.addListener(GongdiSkillsRuntimeProbeV1::start);NeoForge.EVENT_BUS.addListener(GongdiSkillsRuntimeProbeV1::tick);}
    private static void require(boolean value,String name){if(!value)throw new AssertionError(name);checks.add(name);}
    private static Cow cow(double distance){Cow entity=EntityType.COW.create(level);require(entity!=null,"ActualCowFactory");entity.setNoAi(true);entity.setNoGravity(true);entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);entity.setHealth(1000.0F);entity.moveTo(2.0+distance,100.0,2.0,0.0F,0.0F);require(level.addFreshEntity(entity),"ActualLivingNativeAdmission");return entity;}
    private static void position(double distance){target.moveTo(2.0+distance,100.0,2.0,0.0F,0.0F);target.setDeltaMovement(Vec3.ZERO);}
    private static void cast(String expected){require(skills.tryStart(level,target),"OriginalSkillStarts:"+expected);require(skills.isCasting(),"ActualControllerCasting:"+expected);require(boss.getSkillCue().getString("gongdiSkill").equals(expected),"ActualCueSkill:"+expected);phaseTicks=0;}
    private static void row(String name,float actual,float expected){Map<String,Object> entry=new LinkedHashMap<>();entry.put("phase",name);entry.put("actual",actual);entry.put("expected",expected);entry.put("naturalGameTime",level.getGameTime());entry.put("postCallbacks",ticks);rows.add(entry);}
    private static void finish(Throwable failure){if(done)return;done=true;Map<String,Object> report=new LinkedHashMap<>();report.put("cases",checks);report.put("rows",rows);report.put("failures",failure==null?0:1);if(failure!=null){failure.printStackTrace();report.put("failure",failure.toString());}report.put("livingTargetSkillExecutionAccepted",failure==null);report.put("naturalPostCallbacks",ticks);report.put("noManualEntityTicks",true);report.put("controllerTickViaPublicApiOnNaturalServerPost",true);report.put("naturalBossAiLoopAccepted",false);report.put("authenticatedPlayerAccepted",false);report.put("fullBossMigrationComplete",false);try{Files.writeString(Path.of("actual-gongdi-skills-probe.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}catch(Exception problem){problem.printStackTrace();}}
    private static void start(ServerStartedEvent event){try{level=event.getServer().overworld();level.setChunkForced(0,0,true);level.getChunk(0,0);require(event.getServer().getPlayerList().getPlayerCount()==0,"NoAuthenticatedOrFakePlayerFixture");boss=FTEntities.EASTER_BOSS.get().create(level);require(boss!=null,"ActualRegisteredBossFactory");boss.setVariant(3);boss.setNoAi(false);boss.setNoGravity(true);boss.moveTo(2.0,100.0,2.0,0.0F,0.0F);target=cow(4.0);outside=cow(-12.0);skills=new GongdiSkills(boss);expectedDamage=GongdiCombatRules.executeDamage(boss.gongdiMeleeDamage(target),0);beforeHealth=target.getHealth();cast("guillotine");}catch(Throwable failure){finish(failure);}}
    private static void tick(ServerTickEvent.Post event){if(done||level==null)return;try{ticks++;phaseTicks++;require(ticks<500,"BoundedNaturalServerObservation:"+ticks);
        if(phase==0||phase==1||phase==2){position(phase==2?12.0:4.0);skills.tick(level);if(!skills.isCasting()){
            require(boss.getSkillCue().getBoolean("released"),"ActualReleaseCue:"+phase);
            if(phase==0){float damage=beforeHealth-target.getHealth();row("guillotine",damage,expectedDamage);require(Math.abs(damage-expectedDamage)<0.01F,"ActualLivingGuillotineDamageMatchesOriginalRules");beforeHealth=target.getHealth();expectedDamage=GongdiCombatRules.strikeDamage(boss.gongdiMeleeDamage(target));phase=1;cast("crippling_strike");}
            else if(phase==1){float damage=beforeHealth-target.getHealth();row("strike",damage,expectedDamage);require(Math.abs(damage-expectedDamage)<0.01F,"ActualLivingStrikeDamageMatchesOriginalRules");require(target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)&&target.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier()==3,"ActualLivingStrikeSlowAmplifier3");target.removeAllEffects();position(12.0);phase=2;cast("apprehend");}
            else{require(target.getDeltaMovement().x<0.0&&target.getDeltaMovement().y>0.0,"ActualLivingPullVelocityTowardBoss");require(target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),"ActualLivingPullSlow");require(!outside.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)&&outside.getDeltaMovement().lengthSqr()<0.001,"OutsideConeLivingTargetUnaffected");skills.reset();target.removeAllEffects();target.setHealth(1000.0F);target.invulnerableTime=0;position(4.0);oracle=new GongdiCombatRules.Bleed();for(int index=0;index<5;index++){skills.onHit(target);oracle.addHit(level.getGameTime(),boss.isGongdiBloodRageFull());}beforeHealth=target.getHealth();oracleDamage=0.0F;phase=3;phaseTicks=0;}
        }}else if(phase==3){position(4.0);oracleDamage+=oracle.pulse(level.getGameTime());skills.tick(level);if(phaseTicks==45){float damage=beforeHealth-target.getHealth();row("bleed",damage,oracleDamage);require(oracleDamage>0.0F&&Math.abs(damage-oracleDamage)<0.01F,"ActualAdmittedLivingBleedMatchesOriginalPulseRules");skills.reset();beforeHealth=target.getHealth();phase=4;phaseTicks=0;}}
        else if(phase==4){position(4.0);skills.tick(level);if(phaseTicks==25){require(target.getHealth()==beforeHealth,"ResetStopsLivingTargetBleed");cast("guillotine");boss.beginGongdiCast("foreign-skill",20,4.0,new Vec3(1,0,0));skills.tick(level);require(!skills.isCasting(),"ChangedCueCancelsPendingCast");require(target.getHealth()==beforeHealth,"ChangedCueNoLivingDamage");skills.reset();cast("guillotine");target.discard();skills.tick(level);require(!skills.isCasting(),"DiscardedLivingTargetCancelsPendingCast");require(!boss.getSkillCue().contains("gongdiSkill"),"DiscardedTargetCueCancelled");finish(null);}}
    }catch(Throwable failure){finish(failure);}}
}
