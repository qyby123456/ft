/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.CreativeModeTab$Output
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.registries.RegistryObject
 */
package net.farmingtales.ftboss.item;

import java.util.EnumMap;
import java.util.Map;
import net.farmingtales.ftboss.item.BossRelicItem;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.item.FrostQueenStaffItem;
import net.farmingtales.ftboss.item.WeaponBossMaterialItems;
import net.farmingtales.ftboss.util.BossRelicRules;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

public final class BossRelicItems {
    private static final Map<BossRelicRules.Kind, RegistryObject<Item>> RELICS = new EnumMap<BossRelicRules.Kind, RegistryObject<Item>>(BossRelicRules.Kind.class);
    public static final RegistryObject<Item> FROST_QUEEN_STAFF = FTBossItems.ITEMS.register("frost_queen_staff", FrostQueenStaffItem::new);

    private BossRelicItems() {
    }

    public static Item get(BossRelicRules.Kind kind) {
        return (Item)RELICS.get((Object)kind).get();
    }

    static {
        for (BossRelicRules.Kind kind : BossRelicRules.Kind.values()) {
            RELICS.put(kind, (RegistryObject<Item>)FTBossItems.ITEMS.register(kind.item, () -> new BossRelicItem(kind)));
        }
    }

    public static void init() {
        WeaponBossMaterialItems.init();
    }

    public static void addTo(CreativeModeTab.Output output) {
        output.m_246326_((ItemLike)FROST_QUEEN_STAFF.get());
        for (BossRelicRules.Kind kind : BossRelicRules.Kind.values()) {
            output.m_246326_((ItemLike)BossRelicItems.get(kind));
        }
        WeaponBossMaterialItems.addTo(output);
    }
}

