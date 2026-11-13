package net.farmingtales.ftdungeonmobs.generator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

public final class DungeonGenerators {
    public static final ResourceLocation LEGACY = new ResourceLocation("ftdungeonmobs", "legacy");
    public static final ResourceLocation CHAOS = new ResourceLocation("ftdungeonmobs", "chaos");
    private static final Map<ResourceLocation, DungeonGenerator> PROVIDERS = new LinkedHashMap<>();
    private static boolean registering;
    private static boolean initialized;

    private DungeonGenerators() {}

    public static void initialize() {
        if (initialized) return;
        initialized = true;
        registering = true;
        try {
            register(new ChaosDungeonGenerator());
            register(new EclipseDungeonGenerator());
            register(new AmethystBiomeGenerator());
            for (moss.generator.Profiles.Style style : moss.generator.Profiles.all()) register(new MossDungeonGenerator(style.id));
            MinecraftForge.EVENT_BUS.addListener(ChaosDungeonGenerator::playerTick);
            MinecraftForge.EVENT_BUS.post(new DungeonGeneratorRegistrationEvent());
        } finally {
            registering = false;
        }
    }

    static void register(DungeonGenerator provider) {
        if (!registering) throw new IllegalStateException("只能在 DungeonGeneratorRegistrationEvent 注册生成器");
        if (provider == null || provider.id() == null || provider.title() == null || provider.icon().isEmpty())
            throw new IllegalArgumentException("生成器必须提供ID、名称和图标");
        if (PROVIDERS.putIfAbsent(provider.id(), provider) != null)
            throw new IllegalArgumentException("重复生成器ID: " + provider.id());
    }

    public static List<DungeonGenerator> all() { return List.copyOf(PROVIDERS.values()); }
    public static Optional<DungeonGenerator> find(ResourceLocation id) { return Optional.ofNullable(PROVIDERS.get(id)); }
}
