package net.farmingtales.ftdungeonmobs.generator;

import net.minecraftforge.eventbus.api.Event;

public final class DungeonGeneratorRegistrationEvent extends Event {
    public void register(DungeonGenerator generator) {
        DungeonGenerators.register(generator);
    }
}
