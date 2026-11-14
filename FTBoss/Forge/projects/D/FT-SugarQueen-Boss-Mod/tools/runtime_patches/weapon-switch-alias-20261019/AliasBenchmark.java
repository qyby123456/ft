package net.farmingtales.ftboss.item;

import java.util.Arrays;
import net.minecraft.nbt.CompoundTag;

public final class AliasBenchmark {
    public static void main(String[] arguments) throws Exception {
        Object parent = new Object();
        Object[] children = {null, new Object(), new CompoundTag(), "test", Integer.valueOf(7), new byte[4], new long[4], new int[4]};
        for (Object child : children) {
            if (OwnerRepairContinuation.identityAlias(parent, child)) throw new AssertionError("Untrusted alias accepted");
            OwnerFullRepairGuard.alias(parent, child, true);
        }
        for (int iteration = 0; iteration < 20000; iteration++) OwnerFullRepairGuard.alias(parent, children[2], true);
        long[] samples = new long[7];
        for (int sample = 0; sample < samples.length; sample++) {
            long start = System.nanoTime();
            for (int iteration = 0; iteration < 50000; iteration++) OwnerFullRepairGuard.alias(parent, children[2], true);
            samples[sample] = System.nanoTime() - start;
        }
        Arrays.sort(samples);
        System.out.println("{\"untrustedTypesRejected\":" + children.length + ",\"aliasCallsPerSample\":50000,\"medianNanos\":"
                + samples[samples.length / 2] + ",\"samplesNanos\":" + Arrays.toString(samples) + "}");
    }
}
