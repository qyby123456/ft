package ftboss.bossprobe;
import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;
import net.farmingtales.ftboss.boss.ArmorShred;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.util.GongdiCombatRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
@Mod("ftboss_bossprobe")
public final class GongdiPublicContractRuntimeProbeV1 {
    private static final List<String> checks=new ArrayList<>();
    private static final List<Map<String,Object>> rows=new ArrayList<>();
    public GongdiPublicContractRuntimeProbeV1(){NeoForge.EVENT_BUS.addListener(GongdiPublicContractRuntimeProbeV1::start);}
    private static void require(boolean value,String name){if(!value)throw new AssertionError(name);checks.add(name);}
    private static void start(ServerStartedEvent event){Map<String,Object> report=new LinkedHashMap<>();Throwable failure=null;try{
        ServerLevel level=event.getServer().overworld();level.setChunkForced(0,0,true);level.getChunk(0,0);require(event.getServer().getPlayerList().getPlayerCount()==0,"NoAuthenticatedOrFakePlayerFixture");
        require(GongdiCombatRules.INNER_RADIUS==6.75,"OriginalInnerRadius675");require(GongdiCombatRules.STRIKE_RANGE==7.65,"OriginalStrikeRange765");require(GongdiCombatRules.PULL_RANGE==15.75,"OriginalPullRange1575");require(GongdiCombatRules.EXECUTE_RANGE==10.125,"OriginalExecuteRange10125");require(GongdiCombatRules.EXECUTE_COOLDOWN==600,"OriginalExecuteCooldown600");require(GongdiCombatRules.EXECUTE_RESET==200,"OriginalExecuteReset200");
        EasterBossEntity boss=FTEntities.EASTER_BOSS.get().create(level);require(boss!=null,"ActualRegisteredBossFactory");boss.setVariant(3);boss.setNoAi(false);boss.setNoGravity(true);boss.moveTo(2.0,100.0,2.0,0.0F,0.0F);
        Cow target=EntityType.COW.create(level);require(target!=null,"ActualCowFactory");target.setNoAi(true);target.setNoGravity(true);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);target.setHealth(1000.0F);target.moveTo(4.0,100.0,2.0,0.0F,0.0F);require(level.addFreshEntity(target),"ActualLivingTargetNativeAdmission");boss.setTarget(target);require(ArmorShred.stacks(target)==0,"InitialLivingArmorShredZero");require(!boss.isGongdiBloodRageFull(),"InitialBossBloodRageNotFull");
        for(int index=0;index<6;index++){target.invulnerableTime=0;float before=target.getHealth();require(target.hurt(boss.damageSources().mobAttack(boss),1.0F),"ActualLivingDamageAdmitted:"+index);float damage=before-target.getHealth();require(damage>0.0F,"ActualLivingDamageObserved:"+index);boss.onSignatureCleaveHit(level,(LivingEntity)target,damage);int stacks=ArmorShred.stacks(target);require(stacks==Math.min(index+1,5),"OriginalLivingCleaveHookArmorShred:"+index+":"+stacks);Map<String,Object> row=new LinkedHashMap<>();row.put("hit",index+1);row.put("actualHealthLoss",damage);row.put("armorShredStacks",stacks);row.put("bossBloodRageFull",boss.isGongdiBloodRageFull());rows.add(row);}
        require(boss.isGongdiBloodRageFull(),"OriginalLivingCleaveHookBuildsBloodRageFull");require(target.isAlive(),"ActualLivingTargetSurvivesDirectedHookChecks");ArmorShred.clear(target);target.discard();report.put("publicLivingHookAccepted",true);
    }catch(Throwable problem){failure=problem;problem.printStackTrace();}try{report.put("cases",checks);report.put("rows",rows);report.put("failures",failure==null?0:1);if(failure!=null)report.put("failure",failure.toString());report.put("scope","Original public Living onSignatureCleaveHit after actual six Cow hurt calls,armor shred and blood rage;original public constant values compiled from candidate and independently ASM checked against original class ConstantValue. Existing Player descriptors/methods preserved by exact boundary proof. No private reflection,manual Entity.tick or authenticated/natural Boss AI claim. No existing inlined melee consumer rewrite.");report.put("fullBossMigrationComplete",false);Files.writeString(Path.of("actual-gongdi-contract-probe.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}catch(Exception problem){problem.printStackTrace();}}
}
