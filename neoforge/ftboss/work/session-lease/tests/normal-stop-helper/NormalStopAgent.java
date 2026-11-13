import java.lang.instrument.Instrumentation;

public final class NormalStopAgent {
    public static void agentmain(String arguments, Instrumentation instrumentation) throws Exception {
        if (!"observation-complete-normal-stop".equals(arguments)) throw new IllegalArgumentException("Explicit observation terminal stop required");
        Class<?> hooks = null;
        for (Class<?> loaded : instrumentation.getAllLoadedClasses()) {
            if (loaded.getName().equals("net.neoforged.neoforge.server.ServerLifecycleHooks")) {
                if (hooks != null) throw new IllegalStateException("Ambiguous actual server lifecycle class");
                hooks = loaded;
            }
        }
        if (hooks == null) throw new IllegalStateException("Actual server lifecycle is absent");
        Object server = hooks.getMethod("getCurrentServer").invoke(null);
        if (server == null) throw new IllegalStateException("Actual server is already absent");
        var halt = server.getClass().getMethod("halt", boolean.class);
        server.getClass().getMethod("execute", Runnable.class).invoke(server, (Runnable)() -> {
            try {
                System.out.println("[FTBoss isolated observer] Completed-observation helper requests original MinecraftServer.halt(false)");
                halt.invoke(server, false);
            } catch (ReflectiveOperationException failure) {
                throw new IllegalStateException("Original normal server stop request failed", failure);
            }
        });
    }
}
