package net.farmingtales.ftboss.item;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.BossRelicRules.Kind;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class BossMaterialDrops {
    private record Definition(Kind kind, String material) {}

    private static final List<Definition> DEFINITIONS = List.of(
        new Definition(Kind.FROST, "frost"),
        new Definition(Kind.BUTCHER, "butcher"),
        new Definition(Kind.TEA, "tea"),
        new Definition(Kind.WINE, "wine"),
        new Definition(Kind.RAT, "cheese"),
        new Definition(Kind.DEFECT, "capacitor"),
        new Definition(Kind.COMPOST, "spore"),
        new Definition(Kind.APPLE, "apple"),
        new Definition(Kind.BENGER, "shepherd"),
        new Definition(Kind.TIDE, "tide")
    );
    private static final Map<Kind, DeferredHolder<Item, Item>> MATERIALS = new EnumMap<>(Kind.class);

    static {
        for (Definition definition : DEFINITIONS) {
            MATERIALS.put(definition.kind(), FTBossItems.ITEMS.register(definition.material() + "_material",
                () -> new MaterialItem(definition)));
        }
    }

    private BossMaterialDrops() {}

    public static void init() {}

    public static Item get(Kind kind) {
        return MATERIALS.get(kind).get();
    }

    public static void addTo(CreativeModeTab.Output output) {
        for (Kind kind : Kind.values()) output.accept(get(kind));
    }

    public static void reward(Kind kind, Consumer<ItemStack> output) {
        if (kind == null) return;
        ItemStack stack = new ItemStack(get(kind), 1);
        if (stack.isEmpty()) throw new IllegalStateException("Missing Boss material: " + kind);
        output.accept(stack);
        FTBossMod.LOGGER.info("[FTBoss][WeaponComponent] {} x{} from ftboss:{}",
            net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()), stack.getCount(), kind.entity);
    }

    private static final class MaterialItem extends Item {
        private final Definition definition;

        private MaterialItem(Definition definition) {
            super(new Item.Properties().stacksTo(64).rarity(Rarity.RARE).fireResistant());
            this.definition = definition;
        }

        public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("tooltip.ftboss.boss_material",
                Component.translatable("material.ftboss.weapon.boss_" + this.definition.material())).withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.translatable("tooltip.ftboss.boss_material.source",
                Component.translatable("entity.ftboss." + this.definition.kind().entity)).withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.translatable("tooltip.ftboss.boss_material.consume").withStyle(ChatFormatting.GRAY));
        }
    }
}
