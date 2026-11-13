package eclipse.generator;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

final class IncrementalCheckpoint<T> {
    private final List<T> entries;
    private int cursor;
    private boolean complete;

    IncrementalCheckpoint(List<T> entries) {
        this.entries = List.copyOf(entries);
    }

    boolean step(long deadline, LongSupplier clock, Consumer<T> save, Runnable flush) {
        if (complete) return true;
        if (clock.getAsLong() >= deadline) return false;
        if (cursor < entries.size()) {
            save.accept(entries.get(cursor));
            cursor++;
            return false;
        }
        flush.run();
        complete = true;
        return true;
    }
}
