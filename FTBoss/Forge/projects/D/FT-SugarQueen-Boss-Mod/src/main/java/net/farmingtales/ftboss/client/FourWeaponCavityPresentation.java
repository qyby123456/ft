package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.network.StatueMenuNetwork;
import net.farmingtales.ftboss.transaction.FourWeaponClientApi;
import net.minecraft.core.BlockPos;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/** 四类武器构成的只读呈现；2026-10-14 独立五轴、单件图标、锁格和作者羁绊共用真实账本。 */
public final class FourWeaponCavityPresentation {
    private static final int[] AXIS_COLORS = {0xFFC9A67D, 0xFFAAC1CF, 0xFFE0BD7D, 0xFFA8CDA6, 0xFFB3A5CE};

    private FourWeaponCavityPresentation() {}

    public static boolean usesCavity(ItemStack weapon) {
        return FourWeaponFoundationAdapter.applies(weapon) || SwordCavityBuild.applies(weapon);
    }

    public static boolean selectableWeapon(ItemStack weapon) {
        return usesCavity(weapon) || FoodWeaponEvolution.isEvolutionCandidate(weapon);
    }

    public static boolean openContainer(BlockPos pos, ItemStack weapon) {
        return FourWeaponFoundationAdapter.applies(weapon) ? FourWeaponClientApi.openContainer(weapon, pos)
                : StatueMenuNetwork.CavityOpenAction.send(pos, weapon);
    }

    public static Component axisName(FourWeaponFoundationRules.Profile profile, int axis, boolean compact) {
        String name = profile.axes().get(axis);
        String fallback = compact ? name.substring(0, Math.min(3, name.indexOf('_') < 0 ? name.length() : name.indexOf('_')))
                : name.replace('_', ' ');
        return Component.translatableWithFallback("function.ftboss." + profile.family() + "." + (compact ? "short." : "") + name, fallback);
    }

    public static int axisColor(int axis) { return AXIS_COLORS[axis]; }

    public static boolean sufficient(FourWeaponFoundationAdapter.View view, int axis) {
        return view != null && view.valid() && view.status().total().at(axis) + 1.0e-9 >= view.profile().minimum().at(axis);
    }

    public static int cellColor(FourWeaponFoundationAdapter.View view, int index) {
        if (view == null || !view.valid()) return 0xFF11191F;
        var weight = view.profile().weights().get(index);
        int strongest = 0;
        for (int axis = 1; axis < 5; axis++) if (weight.at(axis) > weight.at(strongest)) strongest = axis;
        int color = AXIS_COLORS[strongest];
        return 0xFF000000 | (((color >> 16) & 255) / 3 << 16) | (((color >> 8) & 255) / 3 << 8) | (color & 255) / 3;
    }

    public static ItemStack displayItem(FourWeaponFoundationAdapter.View view, int index) {
        if (view == null || !view.valid()) return ItemStack.EMPTY;
        var entry = view.entries().get(index);
        if (entry == null) return ItemStack.EMPTY;
        if (entry.paid()) return entry.storedItem();
        ResourceLocation id = ResourceLocation.tryParse(entry.value().itemId());
        return id == null || !BuiltInRegistries.ITEM.containsKey(id) ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.get(id));
    }

    public static boolean reference(FourWeaponFoundationAdapter.View view, int index) {
        if (view == null || !view.valid()) return false;
        var entry = view.entries().get(index);
        return entry != null && !entry.paid();
    }

    public static boolean unlocked(FourWeaponFoundationAdapter.View view, int index) {
        return view != null && view.valid() && FourWeaponFoundationRules.unlocked(view.profile(), index, view.level());
    }

    public static List<ItemStack> unlockedPaid(FourWeaponFoundationAdapter.View view) {
        if (view == null || !view.valid()) return List.of();
        List<ItemStack> paid = new ArrayList<>();
        for (var slot : view.entries().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            if (slot.getValue().paid() && FourWeaponFoundationRules.unlocked(view.profile(), slot.getKey(), view.level())) {
                paid.add(slot.getValue().storedItem());
            }
        }
        return List.copyOf(paid);
    }

    public static List<Component> materialRoles(FourWeaponFoundationAdapter.View view, int index) {
        if (view == null || !view.valid()) return List.of();
        var entry = view.entries().get(index);
        if (entry == null) return List.of();
        var values = view.profile().items().getOrDefault(entry.value().itemId(), view.profile().materials().get(entry.value().material()));
        if (values == null) return List.of();
        List<Component> roles = new ArrayList<>();
        for (int axis = 0; axis < 5; axis++) if (values.at(axis) >= 0.6) roles.add(axisName(view.profile(), axis, false));
        return List.copyOf(roles);
    }
}
