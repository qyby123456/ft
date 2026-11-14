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
            try { NativeExpectedFieldObservation.fingerprintCompoundForRead416(null,new LinkedHashMap<String,Tag>()); }
            catch (IllegalStateException denied) { rejected++; }
            var trace=Class.forName("net.farmingtales.ftboss.item.FoodWeaponExpectedTrace");
            var value=Class.forName("net.farmingtales.ftboss.item.FoodWeaponExpectedTrace$Value");
            var entry=trace.getDeclaredMethod("timerExpectationForRead",value);
            entry.setAccessible(true);
            try { entry.invoke(null,new Object[]{null}); }
            catch (InvocationTargetException denied) { if (denied.getCause() instanceof IllegalStateException) rejected++; else throw denied; }
            var trust=Class.forName("net.farmingtales.ftboss.item.NativeFreshFactoryTrust298");
            var registered=trust.getDeclaredField("rangedTimerMethod");
            registered.setAccessible(true);
            Object registeredBefore=registered.get(null);
            if (!(registeredBefore instanceof String methodName) || methodName.isEmpty()) throw new AssertionError("Certified native ranged timer method was not installed");
            boolean physical=java.util.Arrays.stream(net.minecraft.nbt.CompoundTag.class.getDeclaredMethods()).anyMatch(method->method.getName().equals(methodName)
                && method.getDeclaringClass()==net.minecraft.nbt.CompoundTag.class && java.lang.reflect.Modifier.isPrivate(method.getModifiers())
                && method.getReturnType()==void.class && java.util.Arrays.equals(method.getParameterTypes(),new Class<?>[]{String.class,long.class}));
            if (!physical) throw new AssertionError("Registered timer original is not the actual private CompoundTag method");
            var install=trust.getDeclaredMethod("installRangedTimer",Class.forName("org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext"));
            install.setAccessible(true);
            try { install.invoke(null,new Object[]{null}); }
            catch (InvocationTargetException denied) { if (denied.getCause() instanceof IllegalStateException) rejected++; else throw denied; }
            var frameCheck=trust.getDeclaredMethod("rangedTimerMutationFrame",StackWalker.StackFrame.class);
            frameCheck.setAccessible(true);
            var foreign=StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames->frames.findFirst().orElseThrow());
            if (!Boolean.FALSE.equals(frameCheck.invoke(null,foreign)) || registered.get(null)!=registeredBefore) throw new AssertionError("Foreign frame admitted or registry identity changed");
            result.put("certifiedActualRangedTimerMethodInstalled",true);
            result.put("actualPrivateMethodDescriptorVerified",true);
            result.put("foreignRangedTimerInstallerRejected",true);
            result.put("foreignStackFrameRejected",true);
            result.put("registeredTimerMethodIdentityUnchanged",true);
            if (rejected!=4 || domains.get(null)!=before || ((Map<?,?>)before).size()!=sizeBefore) throw new AssertionError("Foreign private-read callers were admitted or altered domains");
            result.put("foreignFingerprintFactoryRejected",true);
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
