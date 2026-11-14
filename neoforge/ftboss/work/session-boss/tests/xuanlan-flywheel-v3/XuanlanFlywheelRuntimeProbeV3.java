package ftboss.bossprobe;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.FlywheelProjectileEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod("ftboss_bossprobe")
public final class XuanlanFlywheelRuntimeProbeV3 {
    private static final List<String> checks=new ArrayList<>();
    private static final Map<String,Object> report=new LinkedHashMap<>();
    private static ServerLevel level;
    private static EasterBossEntity pruningOwner;
    private static EasterBossEntity transitionedSupport;
    private static List<FlywheelProjectileEntity> pruningWheels;
    private static long started;
    private static int callbacks;
    private static boolean finished;
    private static boolean candidate;

    public XuanlanFlywheelRuntimeProbeV3() {
        NeoForge.EVENT_BUS.addListener(XuanlanFlywheelRuntimeProbeV3::start);
        NeoForge.EVENT_BUS.addListener(XuanlanFlywheelRuntimeProbeV3::tick);
    }

    private static void require(boolean condition,String name) {
        if(!condition) throw new AssertionError(name);
        checks.add(name);
    }

    private static EasterBossEntity owner(int variant,double horizontal) {
        EasterBossEntity owner=FTEntities.EASTER_BOSS.get().create(level);
        require(owner!=null,"ActualRegisteredOwnerFactory:"+variant);
        owner.setVariant(variant);owner.setNoAi(true);owner.setNoGravity(true);owner.setInvulnerable(true);
        owner.moveTo(horizontal,90.0,2.0,0.0F,0.0F);
        require(level.addFreshEntity(owner),"ActualOwnerAdmitted:"+owner.getUUID());
        return owner;
    }

    private static FlywheelProjectileEntity wheel(EasterBossEntity owner,int index) {
        FlywheelProjectileEntity wheel=new FlywheelProjectileEntity(level,owner);
        wheel.moveTo(1.0+index*0.3,100.0,5.0+index*0.2,0.0F,0.0F);
        wheel.setNoGravity(true);wheel.setDeltaMovement(Vec3.ZERO);
        return wheel;
    }

    private static List<FlywheelProjectileEntity> wheels(EasterBossEntity owner) {
        List<FlywheelProjectileEntity> result=new ArrayList<>();
        for(Entity entity:level.getAllEntities()) if(entity instanceof FlywheelProjectileEntity wheel && !wheel.isRemoved() && wheel.getOwner()!=null && wheel.getOwner().getUUID().equals(owner.getUUID())) result.add(wheel);
        result.sort(Comparator.comparingInt(Entity::getId));
        return result;
    }

    private static List<FlywheelProjectileEntity> admit(EasterBossEntity owner,int count) {
        List<FlywheelProjectileEntity> accepted=new ArrayList<>();
        for(int index=0;index<count;index++) {
            FlywheelProjectileEntity wheel=wheel(owner,index);
            boolean added=level.addFreshEntity(wheel);
            if(added) accepted.add(wheel);
            require(added==(!candidate || owner.getVariant()!=6 || index<15),"NativeAdmissionOwner"+owner.getVariant()+"Index"+index+":"+added);
        }
        return accepted;
    }

    private static EasterBossEntity support(EasterBossEntity owner,int variant) {
        EasterBossEntity child=FTEntities.EASTER_BOSS.get().create(level);
        require(child!=null,"ActualRegisteredSupportFactory");
        child.setVariant(variant);child.setNoAi(true);child.setNoGravity(true);child.setInvulnerable(true);
        child.moveTo(10.0,95.0,10.0,0.0F,0.0F);
        net.farmingtales.ftboss.boss.ManorFightMembers.inherit(owner,child);
        require(owner.getUUID().equals(net.farmingtales.ftboss.boss.ManorFightMembers.ownerId(child)),"ActualPublicOwnerInheritance");
        return child;
    }

    private static void start(ServerStartedEvent event) {
        try {
            candidate=System.getProperty("ftboss.flywheellimit.mode","baseline").equals("candidate");
            level=event.getServer().overworld();
            level.setChunkForced(0,0,true);level.getChunk(0,0);level.getChunk(5,0);
            require(event.getServer().getPlayerList().getPlayerCount()==0,"NoAuthenticatedPlayerClaimed");
            EasterBossEntity first=owner(6,1.0);List<FlywheelProjectileEntity> firstWheels=admit(first,16);
            require(wheels(first).size()==(candidate?15:16),"OwnerAExactNativeCount");
            EasterBossEntity second=owner(6,3.0);admit(second,16);
            require(wheels(second).size()==(candidate?15:16),"OwnerBCapIndependentOfOwnerA");
            EasterBossEntity control=owner(5,5.0);admit(control,16);
            require(wheels(control).size()==16,"OtherVariantNotCapped");
            FlywheelProjectileEntity distant=wheel(first,17);distant.moveTo(2.0,170.0,2.0,0.0F,0.0F);
            require(level.addFreshEntity(distant)==!candidate,"CapScansActualLevelBeyondOwner48BlockBox");
            require(candidate || wheels(first).stream().anyMatch(value->value.getUUID().equals(distant.getUUID())),"DistantWheelActuallyVisibleInForcedNativeChunk");
            report.put("actualLiveOwnerCountBeforeDiscard",wheels(first).size());
            firstWheels.getFirst().discard();require(firstWheels.getFirst().isRemoved(),"PublicDiscardReleasesOneSlot");
            FlywheelProjectileEntity replacement=wheel(first,18);
            require(level.addFreshEntity(replacement),"NativeAdmissionAfterDiscardedSlot");
            report.put("actualLiveOwnerCountAfterReplacement",wheels(first).size());
            require(wheels(first).size()==(candidate?15:17),"ExactLiveOwnerCountAfterReplacement");
            if(candidate) {
                EasterBossEntity rejectedSupport=support(first,9);
                require(!level.addFreshEntity(rejectedSupport) && rejectedSupport.isRemoved(),"NativeJoinRejectsAndDiscardsVariant9SupportOfXuanlan");
                EasterBossEntity unrelatedChild=support(first,8);
                require(level.addFreshEntity(unrelatedChild),"NativeJoinAllowsOtherChildVariantOfXuanlan");unrelatedChild.discard();
                EasterBossEntity ordinarySupport=support(control,9);
                require(level.addFreshEntity(ordinarySupport),"NativeJoinAllowsVariant9SupportOfOtherOwner");ordinarySupport.discard();
                transitionedSupport=support(control,9);
                require(level.addFreshEntity(transitionedSupport),"ActualSupportAdmittedBeforeOwnerTransition");
                net.farmingtales.ftboss.boss.ManorFightMembers.inherit(first,transitionedSupport);
                require(first.getUUID().equals(net.farmingtales.ftboss.boss.ManorFightMembers.ownerId(transitionedSupport)),"ActualPublicTransitionToVariant6Owner");
                report.put("noSyntheticFightOrSessionAuthority",true);
            }
            pruningOwner=owner(5,7.0);pruningWheels=admit(pruningOwner,18);
            require(pruningWheels.size()==18,"PublicOtherVariantAdmitsOverLimitBeforeTransition");
            pruningOwner.setVariant(6);
            started=level.getGameTime();
            report.put("mode",candidate?"candidate":"baseline");report.put("initialAdmittedOwnerA",firstWheels.size());
            report.put("actualVariantTransition",true);report.put("authenticated",false);report.put("fullBossAcceptance",false);
        } catch(Throwable failure) {finish(failure);}
    }

    private static void tick(ServerTickEvent.Post event) {
        if(finished || level==null) return;
        try {
            callbacks++;
            if(level.getGameTime()-started<25) return;
            List<FlywheelProjectileEntity> live=wheels(pruningOwner);
            report.put("naturalServerPostCallbacks",callbacks);
            report.put("naturallySurvivingPruneWheelIds",live.stream().map(Entity::getId).toList());
            require(pruningOwner.tickCount>=20,"ActualNaturalOwnerTickedWithoutObserverTickCalls");
            require(live.size()==(candidate?15:18),"Natural20TickPruneExactLiveCount:"+live.size());
            if(candidate) require(live.stream().map(Entity::getUUID).toList().equals(pruningWheels.subList(0,15).stream().map(Entity::getUUID).toList()),"NaturalPruneKeepsOriginalOldestEntityIds");
            if(candidate) require(transitionedSupport.isRemoved(),"NaturalTickRemovesVariant9AfterActualOwnerTransition");
            report.put("actualOriginal15CapMissingDemonstrated",!candidate);report.put("nativeAdmissionAndPruneAccepted",candidate);
            finish(null);
        } catch(Throwable failure) {finish(failure);}
    }

    private static void finish(Throwable failure) {
        if(finished) return;
        finished=true;
        try {
            report.put("failures",failure==null?0:1);if(failure!=null) {report.put("failure",failure.toString());failure.printStackTrace();}
            report.put("cases",checks);report.put("fullBossMigrationComplete",false);report.put("deployed",false);
            Files.writeString(Path.of("actual-flywheel-limit-probe.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch(Exception writing) {writing.printStackTrace();}
    }
}
