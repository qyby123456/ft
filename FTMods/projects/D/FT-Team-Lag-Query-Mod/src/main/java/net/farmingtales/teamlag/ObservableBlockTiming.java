package net.farmingtales.teamlag;

import java.util.HashMap;
import java.util.LinkedHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import observable.Observable;
import observable.Props;
import observable.server.Profiler;
import observable.server.TraceMap;

final class ObservableBlockTiming {
    private record Observation(Profiler profiler, Object generation, Profiler.TimingData timing, Profiler.TimingData previous) {}

    private ObservableBlockTiming() {}

    static Object begin(ServerLevel level, BlockPos pos, Class<?> source) {
        if (Props.notProcessing) return null;
        Profiler profiler = Observable.INSTANCE.getPROFILER();
        var generation = profiler.getBlockTimingsMap();
        var timings = generation.get(level.dimension());
        Profiler.TimingData timing = timings == null ? null : timings.get(pos);
        if (timing == null) {
            var chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
            if (chunk == null) return null;
            if (timings == null) {
                timings = new HashMap<>();
                generation.put(level.dimension(), timings);
            }
            timing = new Profiler.TimingData(0L, 0,
                    new TraceMap(source.getName(), "update", new LinkedHashMap<>(), 0),
                    chunk.getBlockState(pos).getBlock().getDescriptionId());
            timings.put(pos.immutable(), timing);
        }
        Observation observation = new Observation(profiler, generation, timing, Props.currentTarget.get());
        Props.currentTarget.set(timing);
        return observation;
    }

    static void end(Object token, long elapsed) {
        Observation observation = (Observation) token;
        try {
            if (Props.notProcessing || observation.profiler().getBlockTimingsMap() != observation.generation()) return;
            var timing = observation.timing();
            timing.setTime(timing.getTime() + elapsed);
            timing.setTicks(timing.getTicks() + 1);
        } finally {
            Props.currentTarget.set(observation.previous());
        }
    }
}
