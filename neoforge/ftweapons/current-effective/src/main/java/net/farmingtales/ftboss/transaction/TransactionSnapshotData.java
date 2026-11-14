package net.farmingtales.ftboss.transaction;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

public class TransactionSnapshotData {
    private final UUID request;
    private final CompoundTag data;

    public TransactionSnapshotData(UUID request, CompoundTag data) {
        this.request = Objects.requireNonNull(request);
        this.data = data.copy();
        if (!this.data.hasUUID("Session") || this.data.getList("Cells", 10).size() != 27 || this.data.getList("Axes", 10).size() != 5) {
            throw new IllegalArgumentException("UI snapshot shape");
        }
    }

    public final UUID request() {
        return this.request;
    }

    public final UUID session() {
        return this.data.getUUID("Session");
    }

    public final CompoundTag data() {
        return this.data.copy();
    }
}
