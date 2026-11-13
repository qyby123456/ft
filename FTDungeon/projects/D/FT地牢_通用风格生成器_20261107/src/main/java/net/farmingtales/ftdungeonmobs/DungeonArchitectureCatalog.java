package net.farmingtales.ftdungeonmobs;

import java.util.List;

public final class DungeonArchitectureCatalog {
    public record Model(String id, int light, String nativeParent, boolean full, int[][] boxes) {}

    public static final List<Model> MODELS = List.of(
            new Model("dungeon_ashlar", 0, "", true, new int[][]{{0, 0, 0, 16, 16, 16}}),
            new Model("dungeon_cracked_tiles", 0, "", true, new int[][]{{0, 0, 0, 16, 16, 16}}),
            new Model("dungeon_mossy_bricks", 0, "", true, new int[][]{{0, 0, 0, 16, 16, 16}}),
            new Model("dungeon_blood_channel", 0, "", false, new int[][]{{0, 0, 0, 16, 9, 16}, {0, 9, 0, 5, 16, 16}, {11, 9, 0, 16, 16, 16}, {5, 9, 0, 11, 10, 16}}),
            new Model("dungeon_bone_mosaic", 0, "", true, new int[][]{{0, 0, 0, 16, 16, 16}}),
            new Model("dungeon_rune_inlay", 3, "", true, new int[][]{{0, 0, 0, 16, 16, 16}}),
            new Model("dungeon_blackstone_stairs", 0, "stairs", false, new int[][]{{0, 0, 0, 16, 8, 16}, {0, 8, 8, 16, 16, 16}}),
            new Model("dungeon_blackstone_slab", 0, "slab", false, new int[][]{{0, 0, 0, 16, 8, 16}}),
            new Model("dungeon_pillar_base", 0, "", false, new int[][]{{0, 0, 0, 16, 4, 16}, {1, 4, 1, 15, 8, 15}, {2, 8, 2, 14, 16, 14}}),
            new Model("dungeon_pillar_shaft", 0, "", false, new int[][]{{3, 0, 3, 13, 16, 13}, {5, 0, 2, 11, 16, 3}, {5, 0, 13, 11, 16, 14}, {2, 0, 5, 3, 16, 11}, {13, 0, 5, 14, 16, 11}}),
            new Model("dungeon_pillar_capital", 0, "", false, new int[][]{{2, 0, 2, 14, 8, 14}, {1, 8, 1, 15, 12, 15}, {0, 12, 0, 16, 16, 16}}),
            new Model("dungeon_arch_left", 0, "", false, new int[][]{{0, 0, 3, 4, 8, 13}, {0, 8, 3, 9, 12, 13}, {0, 12, 3, 16, 16, 13}}),
            new Model("dungeon_arch_right", 0, "", false, new int[][]{{12, 0, 3, 16, 8, 13}, {7, 8, 3, 16, 12, 13}, {0, 12, 3, 16, 16, 13}}),
            new Model("dungeon_arch_keystone", 0, "", false, new int[][]{{0, 10, 3, 16, 16, 13}, {4, 5, 2, 12, 10, 14}, {6, 2, 4, 10, 5, 12}}),
            new Model("dungeon_wall_buttress", 0, "", false, new int[][]{{0, 0, 8, 16, 5, 16}, {2, 5, 10, 14, 10, 16}, {4, 10, 12, 12, 16, 16}}),
            new Model("dungeon_crenellated_parapet", 0, "", false, new int[][]{{0, 0, 7, 16, 9, 16}, {0, 9, 7, 5, 16, 16}, {11, 9, 7, 16, 16, 16}}),
            new Model("dungeon_iron_grate", 0, "", false, new int[][]{{0, 1, 7, 16, 3, 9}, {0, 13, 7, 16, 15, 9}, {1, 0, 7, 3, 16, 9}, {7, 0, 7, 9, 16, 9}, {13, 0, 7, 15, 16, 9}}),
            new Model("dungeon_chain_anchor", 0, "", false, new int[][]{{3, 3, 13, 13, 13, 16}, {4, 4, 8, 6, 12, 11}, {10, 4, 8, 12, 12, 11}, {6, 10, 8, 10, 12, 11}, {6, 4, 8, 10, 6, 11}, {7, 7, 11, 9, 9, 13}}),
            new Model("dungeon_spike_barrier", 0, "", false, new int[][]{{0, 0, 0, 16, 3, 16}, {2, 3, 2, 7, 7, 7}, {3, 7, 3, 6, 11, 6}, {4, 11, 4, 5, 15, 5}, {2, 3, 9, 7, 7, 14}, {3, 7, 10, 6, 11, 13}, {4, 11, 11, 5, 15, 12}, {9, 3, 2, 14, 7, 7}, {10, 7, 3, 13, 11, 6}, {11, 11, 4, 12, 15, 5}, {9, 3, 9, 14, 7, 14}, {10, 7, 10, 13, 11, 13}, {11, 11, 11, 12, 15, 12}}),
            new Model("dungeon_bone_pile", 0, "", false, new int[][]{{1, 0, 2, 15, 2, 14}, {2, 2, 3, 14, 4, 5}, {4, 2, 6, 6, 4, 15}, {7, 4, 5, 15, 6, 7}, {2, 4, 7, 9, 11, 14}, {3, 3, 6, 8, 5, 13}}),
            new Model("dungeon_sarcophagus", 0, "", false, new int[][]{{1, 0, 0, 15, 3, 16}, {2, 3, 1, 14, 10, 15}, {1, 10, 0, 15, 13, 16}, {1, 3, 3, 15, 14, 5}, {1, 3, 11, 15, 14, 13}}),
            new Model("dungeon_soul_lantern", 11, "", false, new int[][]{{3, 0, 3, 13, 3, 13}, {5, 3, 5, 11, 11, 11}, {3, 3, 3, 4, 11, 4}, {3, 3, 12, 4, 11, 13}, {12, 3, 3, 13, 11, 4}, {12, 3, 12, 13, 11, 13}, {3, 11, 3, 13, 13, 13}, {5, 13, 7, 7, 16, 9}, {9, 13, 7, 11, 16, 9}, {7, 15, 7, 9, 16, 9}}),
            new Model("dungeon_wall_cresset", 12, "", false, new int[][]{{4, 3, 14, 12, 15, 16}, {7, 3, 5, 9, 5, 14}, {3, 4, 2, 13, 6, 12}, {3, 6, 2, 5, 9, 12}, {11, 6, 2, 13, 9, 12}, {5, 6, 2, 11, 9, 4}, {5, 6, 10, 11, 9, 12}, {6, 6, 5, 10, 12, 9}, {7, 12, 6, 9, 15, 8}}),
            new Model("dungeon_runic_seal", 5, "", false, new int[][]{{1, 0, 1, 15, 2, 15}, {0, 0, 4, 1, 3, 12}, {15, 0, 4, 16, 3, 12}, {4, 0, 0, 12, 3, 1}, {4, 0, 15, 12, 3, 16}, {3, 2, 3, 13, 4, 13}}));

    private DungeonArchitectureCatalog() {}
}
