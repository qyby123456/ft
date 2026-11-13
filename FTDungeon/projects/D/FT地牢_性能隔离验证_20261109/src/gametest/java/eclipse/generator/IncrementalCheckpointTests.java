package eclipse.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class IncrementalCheckpointTests {
    public static void main(String[] arguments) {
        List<Integer> saved = new ArrayList<>();
        AtomicInteger flushes = new AtomicInteger();
        List<Integer> selected = new ArrayList<>(List.of(11, 23, 37));
        IncrementalCheckpoint<Integer> checkpoint = new IncrementalCheckpoint<>(selected);
        selected.clear();
        require(!checkpoint.step(10, () -> 10, saved::add, flushes::incrementAndGet), "expired deadline yields");
        require(saved.isEmpty() && flushes.get() == 0, "expired deadline does no work");
        for (int index = 0; index < 3; index++) {
            require(!checkpoint.step(10, () -> 0, saved::add, flushes::incrementAndGet), "saving retains journal");
            require(saved.size() == index + 1 && flushes.get() == 0, "one selected chunk per step");
        }
        require(saved.equals(List.of(11, 23, 37)), "snapshot preserves exact selected chunks and order");
        require(checkpoint.step(10, () -> 0, saved::add, flushes::incrementAndGet), "flush permits commit");
        require(flushes.get() == 1, "flush follows all serialized chunks");
        require(checkpoint.step(10, () -> 0, saved::add, flushes::incrementAndGet), "completed checkpoint is stable");
        require(flushes.get() == 1 && saved.size() == 3, "completed checkpoint has no duplicate I/O");

        IncrementalCheckpoint<Integer> interrupted = new IncrementalCheckpoint<>(List.of(42));
        expectFailure(() -> interrupted.step(10, () -> 0, entry -> { throw new IllegalStateException("disk failure"); },
            flushes::incrementAndGet));
        require(!interrupted.step(10, () -> 0, saved::add, flushes::incrementAndGet), "failed serialization retries same chunk");
        require(saved.get(saved.size() - 1) == 42, "failed serialization never skips a chunk");
        expectFailure(() -> interrupted.step(10, () -> 0, saved::add, () -> { throw new IllegalStateException("flush failure"); }));
        require(interrupted.step(10, () -> 0, saved::add, flushes::incrementAndGet), "failed flush never commits and can retry");
        require(flushes.get() == 2, "only successful barriers count");

        IncrementalCheckpoint<Integer> empty = new IncrementalCheckpoint<>(List.of());
        require(empty.step(10, () -> 0, saved::add, flushes::incrementAndGet), "empty checkpoint still requires barrier");
        require(flushes.get() == 3, "empty barrier completed");
        System.out.println("PASS: deadline, one-chunk slice, snapshot isolation, commit barrier, idempotence, serialization failure, flush failure, empty checkpoint");
    }

    private static void expectFailure(Runnable operation) {
        boolean failed = false;
        try {
            operation.run();
        } catch (IllegalStateException expected) {
            failed = true;
        }
        require(failed, "checkpoint propagates persistence failure");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
