package net.farmingtales.ftboss.item;

import java.util.EnumMap;
import java.util.Map;
import net.farmingtales.ftboss.util.BossRelicRules.Kind;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public final class BossRelicItems {
    private static final Map<Kind, RegistryObject<Item>> RELICS = new EnumMap<>(Kind.class);
    public static final RegistryObject<Item> FROST_QUEEN_STAFF =
            FTBossItems.ITEMS.register("frost_queen_staff", FrostQueenStaffItem::new);

    static {
        for (Kind kind : Kind.values()) {
            RELICS.put(kind, FTBossItems.ITEMS.register(kind.item, () -> new BossRelicItem(kind)));
        }
    }

    private BossRelicItems() {}

    public static void init() {
        WeaponBossMaterialItems.init();
    }

    public static Item get(Kind kind) {
        return RELICS.get(kind).get();
    }

    public static void addTo(net.minecraft.world.item.CreativeModeTab.Output output) {
        output.accept(FROST_QUEEN_STAFF.get());
        for (Kind kind : Kind.values()) output.accept(get(kind));
        WeaponBossMaterialItems.addTo(output);
    }
}
