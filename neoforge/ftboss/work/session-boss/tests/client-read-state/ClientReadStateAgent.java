import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClientReadStateAgent {
    public static void agentmain(String argument, Instrumentation instrumentation) throws Exception {
        Class<?> minecraftType = null;
        Class<?> observerType = null;
        for (Class<?> loaded : instrumentation.getAllLoadedClasses()) {
            if (loaded.getName().equals("net.minecraft.client.Minecraft")) minecraftType = loaded;
            if (loaded.getName().equals("ftboss.clientfxprobe.BossClientSummonFxProbe")) observerType = loaded;
        }
        if (minecraftType == null) throw new IllegalStateException("Actual Minecraft client is not loaded");
        final Class<?> clientClass = minecraftType;
        final Class<?> probeClass = observerType;
        final Object client = clientClass.getMethod("getInstance").invoke(null);
        clientClass.getMethod("execute", Runnable.class).invoke(client, (Runnable) () -> {
            try {
                Object screen = clientClass.getField("screen").get(client);
                Object overlay = clientClass.getMethod("getOverlay").invoke(client);
                StringBuilder result = new StringBuilder("{\"readOnlyInspection\":true,\"authenticationOrAuthorityModified\":false");
                result.append(",\"screenClass\":\"").append(screen == null ? "null" : screen.getClass().getName()).append("\"");
                result.append(",\"overlayClass\":\"").append(overlay == null ? "null" : overlay.getClass().getName()).append("\"");
                result.append(",\"hasLevel\":").append(clientClass.getField("level").get(client) != null);
                result.append(",\"hasPlayer\":").append(clientClass.getField("player").get(client) != null);
                result.append(",\"observerLoaded\":").append(probeClass != null);
                if (probeClass != null) {
                    for (String name : new String[]{"clientTicks", "stableTicks", "serverPrepared", "baselineCaptured", "spawned", "received", "expectedPackets", "serverComplete", "summonAge", "renderedActiveFrames", "finished"}) {
                        var field = probeClass.getDeclaredField(name);
                        field.setAccessible(true);
                        result.append(",\"").append(name).append("\":\"").append(field.get(null)).append("\"");
                    }
                }
                result.append('}');
                Files.writeString(Path.of(argument), result.toString());
            } catch (Exception failure) {
                throw new IllegalStateException("Read-only client inspection failed", failure);
            }
        });
    }
}
