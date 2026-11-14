package ftboss.storage308;

import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import net.farmingtales.ftboss.item.NativeExpectedFieldObservation;
import net.minecraft.nbt.Tag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

@EventBusSubscriber(modid="ftboss_storage308")
public final class NativeExpectedImageReadGuardProbe {
    @SubscribeEvent
    public static void started(ServerStartedEvent event) {
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("failures",0);
        result.put("authorityWrittenByReflection",false);
        result.put("playersCreated",false);
        try {
            var domains=NativeExpectedFieldObservation.class.getDeclaredField("DOMAINS");
            domains.setAccessible(true);
            Object before=domains.get(null);
            int sizeBefore=((Map<?,?>)before).size();
            int rejected=0;
            try { NativeExpectedFieldObservation.timerExpectationCompoundForRead(new LinkedHashMap<String,Tag>()); }
            catch (IllegalStateException denied) { rejected++; }
            var trace=Class.forName("net.farmingtales.ftboss.item.FoodWeaponExpectedTrace");
            var value=Class.forName("net.farmingtales.ftboss.item.FoodWeaponExpectedTrace$Value");
            var entry=trace.getDeclaredMethod("timerExpectationForRead",value);
            entry.setAccessible(true);
            try { entry.invoke(null,new Object[]{null}); }
            catch (InvocationTargetException denied) { if (denied.getCause() instanceof IllegalStateException) rejected++; else throw denied; }
            if (rejected!=2 || domains.get(null)!=before || ((Map<?,?>)before).size()!=sizeBefore) throw new AssertionError("Foreign private-read callers were admitted or altered domains");
            result.put("foreignCompoundFactoryRejected",true);
            result.put("foreignExpectedImageCallerRejected",true);
            result.put("domainMapIdentityAndSizeUnchanged",true);
            result.put("serverStartedEventObserved",true);
        } catch (Throwable failure) {
            result.put("failures",1);
            result.put("failure",failure.toString());
        }
        try { Files.writeString(Path.of("actual-private-read-guard.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result)); }
        catch (Exception failure) { throw new RuntimeException(failure); }
    }
}
