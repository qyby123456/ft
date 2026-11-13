package ftboss.bossprobe;
import java.util.*;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.*;
import net.minecraft.server.level.ServerLevel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.zheergenphantom.*;
import net.farmingtales.ftboss.zheergenfx.LightPacket;
import net.farmingtales.ftboss.util.ManorFiveCombatRules.Skill;
public final class TimelineObservation {
    public record Capture(UUID owner,long now,long due,PhantomSnapshot snapshot,boolean cleared) {}
    public record Light(UUID owner,long now,int kind,int index,String origin,boolean codec) {}
    public record Damage(UUID owner,UUID victim,long now,float amount,float expected,float before,float after,boolean returned) {}
    public static final Set<UUID> tracked=new HashSet<>();
    public static final List<Capture> captures=new ArrayList<>();
    public static final List<Light> lights=new ArrayList<>();
    public static final List<Damage> damages=new ArrayList<>();
    public static final List<String> violations=new ArrayList<>();
    public static final Map<UUID,Integer> ticks=new HashMap<>();
    private static final Map<UUID,PhantomSnapshot> entering=new HashMap<>();
    private static EasterBossEntity context;
    private static int pending=-1;
    public static void reset(){tracked.clear();captures.clear();lights.clear();damages.clear();violations.clear();ticks.clear();entering.clear();context=null;pending=-1;}
    public static void captureHead(EasterBossEntity actor){if(tracked.contains(actor.getUUID()))entering.put(actor.getUUID(),((PhantomAccess)actor).ftboss$phantomSnapshot());}
    public static void captureReturn(EasterBossEntity actor){
        if(!tracked.contains(actor.getUUID()))return;
        PhantomSnapshot snapshot=entering.remove(actor.getUUID());
        if(snapshot==null||snapshot.skill()==null){violations.add("EmptyCapture");return;}
        PhantomSnapshot raw=((PhantomAccess)actor).ftboss$phantomSnapshot();
        long now=actor.level().getGameTime();
        captures.add(new Capture(actor.getUUID(),now,now+PhantomTimeline.readableDelay(snapshot.delay()),snapshot,raw.delay()<0&&raw.skill()==null&&raw.target()==null));
    }
    public static void tickHead(EasterBossEntity actor){
        if(!tracked.contains(actor.getUUID()))return;
        if(context!=null)violations.add("NestedOwnerTick");
        context=actor;ticks.merge(actor.getUUID(),1,Integer::sum);
        if(((ServerLevel)actor.level()).getEntity(actor.getUUID())!=actor||actor.tickCount<=0)violations.add("OwnerNotAdmittedOrUnticked");
    }
    public static void tickReturn(EasterBossEntity actor){
        if(!tracked.contains(actor.getUUID()))return;
        PhantomSnapshot raw=((PhantomAccess)actor).ftboss$phantomSnapshot();
        if(raw.delay()>=0||raw.skill()!=null||raw.target()!=null)violations.add("UnclearedAfterTick");
        if(context!=actor)violations.add("WrongTickContext");context=null;
    }
    public static void light(ServerLevel level,LightPacket packet){
        if(context==null)return;
        FriendlyByteBuf buffer=new FriendlyByteBuf(Unpooled.buffer());boolean codec=false;
        try{LightPacket.STREAM_CODEC.encode(buffer,packet);LightPacket decoded=LightPacket.STREAM_CODEC.decode(buffer);codec=decoded.equals(packet)&&decoded.valid()&&buffer.readableBytes()==0;}finally{buffer.release();}
        if(!packet.owner().equals(context.getUUID())||!packet.dimension().equals(level.dimension().location()))violations.add("WrongLightOwnerOrDimension");
        lights.add(new Light(packet.owner(),level.getGameTime(),packet.kind(),packet.index(),packet.origin().toString(),codec));
    }
    public static void damageHead(LivingEntity victim,Entity owner,float amount){
        if(context==null)return;
        if(owner!=context)violations.add("WrongDamageOwner");
        PhantomSnapshot raw=((PhantomAccess)context).ftboss$phantomSnapshot();Skill skill=Skill.byId(raw.skill());
        float expected=skill==null?-1:(float)(context.manorSkillDamage(victim,skill.damage,skill.meleeMultiplier())*0.25);
        pending=damages.size();damages.add(new Damage(owner.getUUID(),victim.getUUID(),owner.level().getGameTime(),amount,expected,victim.getHealth(),victim.getHealth(),false));
    }
    public static void damageReturn(LivingEntity victim,Entity owner){
        if(context==null)return;
        if(pending<0){violations.add("UnpairedDamageReturn");return;}
        Damage old=damages.get(pending);damages.set(pending,new Damage(old.owner(),old.victim(),old.now(),old.amount(),old.expected(),old.before(),victim.getHealth(),true));pending=-1;
    }
}
