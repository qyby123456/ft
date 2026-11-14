/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.network.StatueMenuNetwork;
import net.farmingtales.ftboss.transaction.FourWeaponClientApi;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class FourWeaponCavityPresentation {
    private static final int[] AXIS_COLORS = new int[]{-3561859, -5586481, -2048643, -5714522, -5003826};

    private FourWeaponCavityPresentation() {
    }

    public static boolean usesCavity(ItemStack itemStack) {
        return FourWeaponFoundationAdapter.applies(itemStack) || SwordCavityBuild.applies(itemStack);
    }

    public static boolean selectableWeapon(ItemStack itemStack) {
        return FourWeaponCavityPresentation.usesCavity(itemStack) || FoodWeaponEvolution.isEvolutionCandidate(itemStack);
    }

    public static boolean openContainer(BlockPos blockPos, ItemStack itemStack) {
        return FourWeaponFoundationAdapter.applies(itemStack) ? FourWeaponClientApi.openContainer(itemStack, blockPos) : StatueMenuNetwork.CavityOpenAction.send(blockPos, itemStack);
    }

    public static Component axisName(FourWeaponFoundationRules.Profile profile, int n, boolean bl) {
        String string = profile.axes().get(n);
        String string2 = bl ? string.substring(0, Math.min(3, string.indexOf(95) < 0 ? string.length() : string.indexOf(95))) : string.replace('_', ' ');
        return Component.m_264568_((String)("function.ftboss." + profile.family() + "." + (bl ? "short." : "") + string), (String)string2);
    }

    public static int axisColor(int n) {
        return AXIS_COLORS[n];
    }

    public static boolean sufficient(FourWeaponFoundationAdapter.View view, int n) {
        return view != null && view.valid() && view.status().total().at(n) + 1.0E-9 >= view.profile().minimum().at(n);
    }

    public static int cellColor(FourWeaponFoundationAdapter.View view, int n) {
        int n2;
        if (view == null || !view.valid()) {
            return -15656673;
        }
        FourWeaponFoundationRules.Values values = view.profile().weights().get(n);
        int n3 = 0;
        for (n2 = 1; n2 < 5; ++n2) {
            if (!(values.at(n2) > values.at(n3))) continue;
            n3 = n2;
        }
        n2 = AXIS_COLORS[n3];
        return 0xFF000000 | (n2 >> 16 & 0xFF) / 3 << 16 | (n2 >> 8 & 0xFF) / 3 << 8 | (n2 & 0xFF) / 3;
    }

    public static ItemStack displayItem(FourWeaponFoundationAdapter.View view, int n) {
        if (view == null || !view.valid()) {
            return ItemStack.f_41583_;
        }
        FourWeaponFoundationAdapter.Entry entry = view.entries().get(n);
        if (entry == null) {
            return ItemStack.f_41583_;
        }
        if (entry.paid()) {
            return entry.storedItem();
        }
        ResourceLocation resourceLocation = ResourceLocation.m_135820_((String)entry.value().itemId());
        return resourceLocation == null || !BuiltInRegistries.f_257033_.m_7804_(resourceLocation) ? ItemStack.f_41583_ : new ItemStack((ItemLike)BuiltInRegistries.f_257033_.m_7745_(resourceLocation));
    }

    public static boolean reference(FourWeaponFoundationAdapter.View view, int n) {
        if (view == null || !view.valid()) {
            return false;
        }
        FourWeaponFoundationAdapter.Entry entry = view.entries().get(n);
        return entry != null && !entry.paid();
    }

    public static boolean unlocked(FourWeaponFoundationAdapter.View view, int n) {
        return view != null && view.valid() && FourWeaponFoundationRules.unlocked(view.profile(), n, view.level());
    }

    public static List<ItemStack> unlockedPaid(FourWeaponFoundationAdapter.View view) {
        if (view == null || !view.valid()) {
            return List.of();
        }
        ArrayList<ItemStack> arrayList = new ArrayList<ItemStack>();
        for (Map.Entry entry : view.entries().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            if (!((FourWeaponFoundationAdapter.Entry)entry.getValue()).paid() || !FourWeaponFoundationRules.unlocked(view.profile(), (Integer)entry.getKey(), view.level())) continue;
            arrayList.add(((FourWeaponFoundationAdapter.Entry)entry.getValue()).storedItem());
        }
        return List.copyOf(arrayList);
    }

    public static List<Component> materialRoles(FourWeaponFoundationAdapter.View view, int n) {
        if (view == null || !view.valid()) {
            return List.of();
        }
        FourWeaponFoundationAdapter.Entry entry = view.entries().get(n);
        if (entry == null) {
            return List.of();
        }
        FourWeaponFoundationRules.Values values = view.profile().items().getOrDefault(entry.value().itemId(), view.profile().materials().get((Object)entry.value().material()));
        if (values == null) {
            return List.of();
        }
        ArrayList<Component> arrayList = new ArrayList<Component>();
        for (int i = 0; i < 5; ++i) {
            if (!(values.at(i) >= 0.6)) continue;
            arrayList.add(FourWeaponCavityPresentation.axisName(view.profile(), i, false));
        }
        return List.copyOf(arrayList);
    }
}

