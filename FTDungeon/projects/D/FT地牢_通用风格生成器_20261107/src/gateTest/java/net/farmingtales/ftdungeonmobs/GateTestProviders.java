package net.farmingtales.ftdungeonmobs;

import java.util.LinkedHashMap;
import java.util.Map;
import net.farmingtales.ftdungeonmobs.generator.DungeonGenerator;
import net.farmingtales.ftdungeonmobs.generator.DungeonGeneratorRegistrationEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("ftgateintegration")
public final class GateTestProviders {
    public GateTestProviders() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(GateTestProviders::register);
    }
    public static final ResourceLocation ID = new ResourceLocation("ftgateintegration", "isolated");
    public static final Provider PROVIDER = new Provider();
    public static int registrationEvents;

    @SubscribeEvent
    public static void register(DungeonGeneratorRegistrationEvent event) {
        registrationEvents++;
        event.register(PROVIDER);
    }

    public static final class Provider implements DungeonGenerator {
        public final Map<String, Request> requests = new LinkedHashMap<>();
        public final Map<String, Phase> phases = new LinkedHashMap<>();
        public int reserves;
        public int starts;
        public int entries;

        public ResourceLocation id() { return ID; }
        public Component title() { return Component.literal("Isolated gate integration provider"); }
        public ItemStack icon() { return new ItemStack(Items.COMPASS); }

        public Status status(MinecraftServer server, String instance) {
            return new Status(instance.isEmpty() ? Phase.UNPLANNED : phases.getOrDefault(instance, Phase.UNAVAILABLE), Component.literal("test provider"));
        }

        public String reserve(Request request) {
            if (!(request.source().getBlockEntity(request.gate()) instanceof DungeonGateBlockEntity gate)
                || !gate.hasGeneratorSeed() || !gate.generatorId().equals(ID) || gate.generatorSeed() != request.seed()) {
                throw new IllegalStateException("Generator type and seed must be frozen before reserve");
            }
            reserves++;
            String instance = "gate-test-" + request.gate().asLong() + "-" + request.seed();
            if (requests.putIfAbsent(instance, request) != null) {
                throw new IllegalStateException("Repeated prepare attempted to reserve again");
            }
            phases.put(instance, Phase.PLANNED);
            return instance;
        }

        public void start(MinecraftServer server, String instance) {
            Request request = requests.get(instance);
            if (request == null || !(request.source().getBlockEntity(request.gate()) instanceof DungeonGateBlockEntity gate)
                || !instance.equals(gate.generatorInstance()) || gate.generatorTier() != request.tier()) {
                throw new IllegalStateException("Persistent gate binding must precede generator start");
            }
            DungeonGateBlockEntity diskShape = new DungeonGateBlockEntity(gate.getBlockPos(), gate.getBlockState());
            diskShape.load(gate.saveWithoutMetadata());
            if (!instance.equals(diskShape.generatorInstance()) || diskShape.generatorSeed() != request.seed()) {
                throw new IllegalStateException("Binding is missing from actual block-entity NBT before start");
            }
            starts++;
            phases.putIfAbsent(instance, Phase.PLANNED);
        }

        public void enter(ServerPlayer player, String instance) {
            if (!status(player.server, instance).ready()) {
                throw new IllegalStateException("Unready provider entry");
            }
            entries++;
        }
    }
}
