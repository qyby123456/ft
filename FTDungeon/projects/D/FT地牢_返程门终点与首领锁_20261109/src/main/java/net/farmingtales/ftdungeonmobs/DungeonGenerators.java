package net.farmingtales.ftdungeonmobs;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class DungeonGenerators {
    public static final ResourceLocation ROOMS = FTDungeonMobs.resource("rooms");
    public static final ResourceLocation AMETHYST_BIOME = FTDungeonMobs.resource("amethyst_biome");
    private static final Map<ResourceLocation, Provider> PROVIDERS = new LinkedHashMap<>();

    public enum State { READY, BUILDING, FAILED }

    public record Preparation(State state, Vec3 landing, Component message) {
        public Preparation {
            Objects.requireNonNull(state);
            Objects.requireNonNull(message);
            if (state == State.READY && (landing == null || !Double.isFinite(landing.x)
                    || !Double.isFinite(landing.y) || !Double.isFinite(landing.z))) {
                throw new IllegalArgumentException("Ready generator requires a finite landing point");
            }
        }
    }

    public interface Provider {
        ResourceLocation id();
        Component name();
        Component description();
        Preparation prepare(ServerLevel destination);
        default void onEntered(ServerPlayer player, ServerLevel destination) {}
        default void tick(net.minecraft.server.MinecraftServer server) {}
    }

    private DungeonGenerators() {}

    public static synchronized void register(Provider provider) {
        Objects.requireNonNull(provider);
        Objects.requireNonNull(provider.id());
        Objects.requireNonNull(provider.name());
        Objects.requireNonNull(provider.description());
        if (PROVIDERS.size() >= 256 || PROVIDERS.putIfAbsent(provider.id(), provider) != null) {
            throw new IllegalArgumentException("Duplicate generator or generator limit reached: " + provider.id());
        }
    }

    public static synchronized List<Provider> entries() {
        return List.copyOf(PROVIDERS.values());
    }

    public static synchronized Provider get(ResourceLocation id) {
        return PROVIDERS.get(id);
    }

    public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) { return; }
        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null) { entries().forEach(provider -> provider.tick(server)); }
    }

    static void bootstrap() {
        register(new Provider() {
            public ResourceLocation id() { return ROOMS; }
            public Component name() { return Component.translatable("generator.ftdungeonmobs.rooms"); }
            public Component description() { return Component.translatable("generator.ftdungeonmobs.rooms.description"); }
            public Preparation prepare(ServerLevel destination) {
                boolean ready = DungeonArrival.prepare(destination, DungeonGateData.get(destination.getServer()));
                return new Preparation(ready ? State.READY : State.FAILED,
                        ready ? Vec3.atBottomCenterOf(DungeonArrival.ARRIVAL) : null,
                        Component.translatable("message.ftdungeonmobs.gate." + (ready ? "generator_ready" : "arrival_blocked")));
            }
            public void onEntered(ServerPlayer player, ServerLevel destination) {
                DungeonRooms.Result rooms = DungeonRooms.prepare(destination);
                if (!rooms.code().equals("ready")) { player.sendSystemMessage(rooms.message()); }
                for (DungeonRoomGallery.Report report : DungeonRoomGallery.prepareAll(destination, false)) {
                    if (!report.code().equals("ready")) { player.sendSystemMessage(report.message()); }
                }
            }
        });
        register(new AmethystDungeonGenerator());
    }
}
