package net.farmingtales.ftdungeonmobs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public final class DungeonEncounterPool {
    public enum Rank { NORMAL, ELITE }

    private static final Map<ResourceLocation, Rank> RANKS = new LinkedHashMap<>();

    static {
        register(Rank.NORMAL, "ftdungeonmobs", "skeleton_warrior", "skeleton_archer", "skeleton_mage");
        register(Rank.NORMAL, "minecraft", "zombie", "husk", "drowned", "skeleton", "stray", "spider",
                "cave_spider", "silverfish", "endermite", "pillager", "creeper");
        register(Rank.ELITE, "minecraft", "witch", "vindicator", "evoker", "piglin_brute", "wither_skeleton",
                "blaze", "ravager");
        register(Rank.NORMAL, "cataclysm", "endermaptera", "deepling", "deepling_brute", "deepling_angler",
                "deepling_priest", "deepling_warlock", "the_watcher", "koboleton", "ignited_berserker",
                "draugr", "royal_draugr", "drowned_host");
        register(Rank.ELITE, "cataclysm", "elite_draugr", "ender_golem", "ignited_revenant", "coral_golem",
                "amethyst_crab", "the_prowler", "coralssus", "kobolediator", "wadjet", "aptrgangr");
    }

    private DungeonEncounterPool() {}

    private static void register(Rank rank, String namespace, String... identities) {
        for (String identity : identities) {
            RANKS.put(new ResourceLocation(namespace, identity), rank);
        }
    }

    public static Rank roomRank(String identity) {
        return identity.matches("[ML][0-9]+") || identity.equals("LEGACY_31") || identity.startsWith("CHALLENGE_")
                ? Rank.ELITE : Rank.NORMAL;
    }

    public static Rank rank(ResourceLocation identity) {
        return RANKS.get(identity);
    }

    public static ResourceLocation identity(String value) {
        return ResourceLocation.tryParse(value.indexOf(':') < 0 ? FTDungeonMobs.ID + ":" + value : value);
    }

    public static List<String> defaults(Rank rank) {
        return RANKS.entrySet().stream().filter(entry -> entry.getValue() == rank)
                .map(entry -> entry.getKey().toString()).toList();
    }

    public static List<ResourceLocation> available(Rank rank) {
        List<ResourceLocation> identities = new ArrayList<>();
        for (String value : DungeonSpawnConfig.mobPool(rank)) {
            ResourceLocation identity = identity(value);
            if (rank(identity) == rank && type(identity) != null && !identities.contains(identity)) {
                identities.add(identity);
            }
        }
        if (identities.isEmpty()) {
            for (String value : defaults(rank)) {
                ResourceLocation identity = identity(value);
                if (type(identity) != null) {
                    identities.add(identity);
                }
            }
        }
        return List.copyOf(identities);
    }

    public static EntityType<?> type(ResourceLocation identity) {
        return identity != null && ForgeRegistries.ENTITY_TYPES.containsKey(identity)
                ? ForgeRegistries.ENTITY_TYPES.getValue(identity) : null;
    }
}
