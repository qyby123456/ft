package net.farmingtales.ftboss.item;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.BossRelicRules.Kind;
import net.farmingtales.ftboss.util.WeaponBossMaterialRules;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

/** Boss 原料注册与既有胜利链路发放；10 类各 1 件、沿用结算者防重，2026-10-12 新增独立材料。 */
public final class WeaponBossMaterialItems {
    private static final Map<Kind, RegistryObject<Item>> MATERIALS = new EnumMap<>(Kind.class);

    static {
        for (Kind kind : Kind.values()) {
            MATERIALS.put(kind, FTBossItems.ITEMS.register(WeaponBossMaterialRules.itemId(kind), () -> new WeaponBossMaterialItem(kind)));
        }
    }

    private WeaponBossMaterialItems() {}

    public static void init() {}

    public static Item get(Kind kind) {
        return MATERIALS.get(kind).get();
    }

    public static void reward(Kind kind, Consumer<ItemStack> drop) {
        if (WeaponBossMaterialRules.count(kind) == 0) return;
        ItemStack component = new ItemStack(get(kind), WeaponBossMaterialRules.count(kind));
        if (component.isEmpty()) throw new IllegalStateException("Missing Boss weapon component: " + kind);
        drop.accept(component);
        FTBossMod.LOGGER.info("[FTBoss][WeaponComponent] {} x{} from ftboss:{}", WeaponBossMaterialRules.componentId(kind), component.getCount(), kind.entity);
    }

    public static void addTo(CreativeModeTab.Output output) {
        for (Kind kind : Kind.values()) output.accept(get(kind));
    }
}
