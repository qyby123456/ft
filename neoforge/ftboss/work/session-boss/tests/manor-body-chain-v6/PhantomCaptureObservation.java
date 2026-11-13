package ftboss.bossprobe;
import java.util.*;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.zheergenphantom.*;
public final class PhantomCaptureObservation {
    private static final Map<UUID,PhantomSnapshot> captures=new HashMap<>();
    public static void observe(EasterBossEntity actor){PhantomSnapshot snapshot=((PhantomAccess)actor).ftboss$phantomSnapshot();if(snapshot.skill()!=null)captures.put(actor.getUUID(),snapshot);}
    public static PhantomSnapshot captured(UUID owner){return captures.get(owner);}
}
