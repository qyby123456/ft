package ftboss.bossprobe;
import java.util.*;
import net.minecraft.core.particles.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.*;
import net.minecraft.world.phys.Vec3;
public final class EntryFxObservation {
    public record Particle(String type,Vec3 origin,int count,double spreadX,double spreadY,double spreadZ,double speed){}
    public record Sound(String type,Vec3 origin,String source,float volume,float pitch){}
    private static boolean active;
    private static final List<Particle> particles=new ArrayList<>();
    private static final List<Sound> sounds=new ArrayList<>();
    public static final List<String> checks=new ArrayList<>();
    public static final List<Map<String,Object>> samples=new ArrayList<>();
    public static void begin(){particles.clear();sounds.clear();active=true;}
    public static void end(){active=false;}
    public static void particle(ParticleOptions type,double x,double y,double z,int count,double spreadX,double spreadY,double spreadZ,double speed){if(active)particles.add(new Particle(BuiltInRegistries.PARTICLE_TYPE.getKey(type.getType()).toString(),new Vec3(x,y,z),count,spreadX,spreadY,spreadZ,speed));}
    public static void sound(SoundEvent type,double x,double y,double z,SoundSource source,float volume,float pitch){if(active)sounds.add(new Sound(type.getLocation().toString(),new Vec3(x,y,z),source.name(),volume,pitch));}
    private static void require(boolean value,String name){if(!value)throw new AssertionError(name);checks.add(name);}
    public static void verifyShadow(boolean candidate){List<Particle> ink=particles.stream().filter(value->value.type.equals("minecraft:squid_ink")&&value.count==30).toList(),smoke=particles.stream().filter(value->value.type.equals("minecraft:smoke")&&value.count==20).toList();List<Sound> teleport=sounds.stream().filter(value->value.type.equals("minecraft:entity.enderman.teleport")&&value.volume==1.0F&&value.pitch==0.7F).toList();require(ink.size()==(candidate?2:0)&&smoke.size()==(candidate?2:0)&&teleport.size()==(candidate?1:0),"ActualOriginalCastStartVanillaParticleAndSoundCounts");if(candidate){for(int index=0;index<2;index++){Vec3 expected=index==0?new Vec3(8,321,8):new Vec3(12,321,5.8);Particle first=ink.get(index),second=smoke.get(index);require(first.origin.distanceToSqr(expected)<0.00001&&first.spreadX==0.6&&first.spreadY==0.8&&first.spreadZ==0.6&&first.speed==0.05,"OriginalTeleportSquidInkGeometryAndMotion");require(second.origin.distanceToSqr(expected)<0.00001&&second.spreadX==0.4&&second.spreadY==0.6&&second.spreadZ==0.4&&second.speed==0.03,"OriginalTeleportSmokeGeometryAndMotion");}require(teleport.getFirst().origin.equals(new Vec3(8,320,8))&&teleport.getFirst().source.equals("HOSTILE"),"OriginalTeleportHostileSoundPosition");}samples.add(Map.of("particles",List.copyOf(ink),"smoke",List.copyOf(smoke),"teleportSounds",List.copyOf(teleport)));}
}
