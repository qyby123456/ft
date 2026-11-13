package net.farmingtales.ftdungeonloot;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public final class LootProfiles extends SimpleJsonResourceReloadListener {
    public static final LootProfiles INSTANCE = new LootProfiles();
    private static Map<ResourceLocation, LootProfile> profiles = Map.of();
    private static long revision;

    private LootProfiles() { super(new Gson(), "loot_stands"); }
    public static Optional<LootProfile> find(ResourceLocation id) { return Optional.ofNullable(profiles.get(id)); }
    public static long revision() { return revision; }
    public static java.util.Set<ResourceLocation> ids() { return profiles.keySet(); }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, LootProfile> parsed = new LinkedHashMap<>();
        entries.forEach((id, value) -> {
            try { parsed.put(id, LootProfile.parse(id, value.getAsJsonObject())); }
            catch (RuntimeException exception) { FTDungeonLoot.LOGGER.error("Disabled invalid ordinary loot profile {}", id, exception); }
        });
        profiles = Map.copyOf(parsed);
        revision++;
        FTDungeonLoot.LOGGER.info("Loaded {} ordinary chest loot profiles", profiles.size());
    }
}
