package net.farmingtales.ftdungeonmobs;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public final class DungeonBossGatherEvent extends Event {
    private final ServerLevel level;
    private final DungeonBossGateApi.Binding binding;
    private final List<ServerPlayer> players;
    private final Map<UUID, Vec3> landings;
    private final UUID roundId;
    private final int tier;

    public DungeonBossGatherEvent(ServerLevel level, DungeonBossGateApi.Binding binding, List<ServerPlayer> players,
            Map<UUID, Vec3> landings, UUID roundId, int tier) {
        this.level = level;
        this.binding = binding;
        this.players = List.copyOf(players);
        this.landings = Map.copyOf(landings);
        this.roundId = roundId;
        this.tier = tier;
    }

    public ServerLevel level() { return level; }
    public DungeonBossGateApi.Binding binding() { return binding; }
    public List<ServerPlayer> players() { return players; }
    public Map<UUID, Vec3> landings() { return landings; }
    public UUID roundId() { return roundId; }
    public int tier() { return tier; }
}
