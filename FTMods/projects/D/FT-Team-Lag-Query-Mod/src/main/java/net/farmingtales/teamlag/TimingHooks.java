package net.farmingtales.teamlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraftforge.fml.ModList;

final class TimingHooks {
    private record Hook(String target, String method, String mod) {}
    private static final List<Hook> HOOKS = List.of(
            new Hook("net.minecraft.world.level.chunk.LevelChunk$BoundTickingBlockEntity", "ftteamlagBefore", "minecraft"),
            new Hook("net.minecraft.server.level.ServerLevel", "ftteamlagEntityBefore", "minecraft"),
            new Hook("net.minecraft.server.level.ServerLevel", "ftteamlagPassengerBefore", "minecraft"),
            new Hook("com.farmingtales.machine.NativeMachineScheduler", "ftteamlagBeforeCollection", "farmingtales_magic_barrel"),
            new Hook("com.farmingtales.machine.NativeMachineScheduler", "ftteamlagTick", "farmingtales_magic_barrel"),
            new Hook("com.farmingtales.scheduler.HarvesterTask", "ftteamlagBefore", "farmingtales_magic_barrel"),
            new Hook("com.refinedmods.refinedstorage.apiimpl.network.NetworkListener", "ftteamlagNetworkUpdate", "refinedstorage"),
            new Hook("com.refinedmods.refinedstorage.apiimpl.network.NetworkListener", "ftteamlagNodeUpdate", "refinedstorage"),
            new Hook("observable.server.ProfilingData$Companion", "ftteamlagReport", "observable"));
    private static List<String> missing = List.of();
    private static String status = "计时注入尚未自检";
    private TimingHooks() {}
    static void check() {
        var errors = new ArrayList<String>();
        int expected = 0;
        for (Hook hook : HOOKS) {
            if (!ModList.get().isLoaded(hook.mod())) continue;
            expected++;
            try {
                Class<?> target = Class.forName(hook.target(), false, TimingHooks.class.getClassLoader());
                if (Arrays.stream(target.getDeclaredMethods()).noneMatch(method -> method.getName().contains(hook.method()))) errors.add(hook.method());
            } catch (ReflectiveOperationException | LinkageError error) { errors.add(hook.method()); }
        }
        missing = List.copyOf(errors);
        status = "计时注入自检 " + (expected - errors.size()) + "/" + expected + (errors.isEmpty() ? " 已生效" : "；未生效=" + String.join(",", errors));
        LagQuery.LOGGER.info("[团队卡顿查询] {}", status);
    }
    static List<String> missing() { return missing; }
    static String status() { return status; }
}
