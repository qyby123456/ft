package net.farmingtales.ftboss.item;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.IEventBus;

public final class NativeDamageEventPosting {
    private NativeDamageEventPosting() {
    }

    public static boolean post(IEventBus bus, Event event) {
        bus.post(event);
        return event instanceof ICancellableEvent cancellable && cancellable.isCanceled();
    }
}
