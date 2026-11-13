package ftboss.bossprobe;
import java.util.*;
import net.minecraft.world.entity.*;
public final class EchoDamageObservation {
    public record Entry(UUID victim,UUID owner,float amount){}
    private static boolean active;
    public static final List<Entry> entries=new ArrayList<>();
    public static void begin(){entries.clear();active=true;}
    public static void end(){active=false;}
    public static void observe(LivingEntity victim,Entity owner,float amount){if(active)entries.add(new Entry(victim.getUUID(),owner.getUUID(),amount));}
}
