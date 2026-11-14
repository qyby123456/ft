/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.CreativeModeTab$Output
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.registries.RegistryObject
 */
package net.farmingtales.ftboss.item;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.item.WeaponBossMaterialItem;
import net.farmingtales.ftboss.util.BossRelicRules;
import net.farmingtales.ftboss.util.WeaponBossMaterialRules;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

public final class WeaponBossMaterialItems {
    private static final Map<BossRelicRules.Kind, RegistryObject<Item>> MATERIALS = new EnumMap<BossRelicRules.Kind, RegistryObject<Item>>(BossRelicRules.Kind.class);

    private WeaponBossMaterialItems() {
    }

    public static void init() {
    }

    public static Item get(BossRelicRules.Kind kind) {
        return (Item)MATERIALS.get((Object)kind).get();
    }

    public static void reward(BossRelicRules.Kind kind, Consumer<ItemStack> consumer) {
        if (WeaponBossMaterialRules.count(kind) == 0) {
            return;
        }
        ItemStack itemStack = new ItemStack((ItemLike)WeaponBossMaterialItems.get(kind), WeaponBossMaterialRules.count(kind));
        if (itemStack.m_41619_()) {
            throw new IllegalStateException("Missing Boss weapon component: " + kind);
        }
        consumer.accept(itemStack);
        FTBossMod.LOGGER.info("[FTBoss][WeaponComponent] {} x{} from ftboss:{}", new Object[]{WeaponBossMaterialRules.componentId(kind), itemStack.m_41613_(), kind.entity});
    }

    public static void addTo(CreativeModeTab.Output output) {
        for (BossRelicRules.Kind kind : BossRelicRules.Kind.values()) {
            output.m_246326_((ItemLike)WeaponBossMaterialItems.get(kind));
        }
    }

    static {
        for (BossRelicRules.Kind kind : BossRelicRules.Kind.values()) {
            MATERIALS.put(kind, (RegistryObject<Item>)FTBossItems.ITEMS.register(WeaponBossMaterialRules.itemId(kind), () -> new WeaponBossMaterialItem(kind)));
        }
    }
}

