package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.Entry;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.View;
import net.farmingtales.ftboss.network.NativeStatueCavityNetwork.CavityOpenAction;
import net.farmingtales.ftboss.transaction.FourWeaponClientApi;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Profile;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Values;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class FourWeaponCavityPresentation {
    private static final int[] AXIS_COLORS = new int[]{-3561859, -5586481, -2048643, -5714522, -5003826};

    private FourWeaponCavityPresentation() {
    }

    public static boolean usesCavity(ItemStack var0) {
        return FourWeaponFoundationAdapter.applies(var0) || SwordCavityBuild.applies(var0);
    }

    public static boolean selectableWeapon(ItemStack var0) {
        return usesCavity(var0) || FoodWeaponEvolution.isEvolutionCandidate(var0);
    }

    public static boolean openContainer(BlockPos var0, ItemStack var1) {
        return FourWeaponFoundationAdapter.applies(var1) ? FourWeaponClientApi.openContainer(var1, var0) : CavityOpenAction.send(var0, var1);
    }

    public static Component axisName(Profile var0, int var1, boolean var2) {
        String var3 = var0.axes().get(var1);
        String var4 = var2 ? var3.substring(0, Math.min(3, var3.indexOf(95) < 0 ? var3.length() : var3.indexOf(95))) : var3.replace('_', ' ');
        return Component.translatableWithFallback("function.ftboss." + var0.family() + "." + (var2 ? "short." : "") + var3, var4);
    }

    public static int axisColor(int var0) {
        return AXIS_COLORS[var0];
    }

    public static boolean sufficient(View var0, int var1) {
        return var0 != null && var0.valid() && var0.status().total().at(var1) + 1.0E-9 >= var0.profile().minimum().at(var1);
    }

    public static int cellColor(View var0, int var1) {
        if (var0 != null && var0.valid()) {
            Values var2 = var0.profile().weights().get(var1);
            int var3 = 0;

            for (int var4 = 1; var4 < 5; var4++) {
                if (var2.at(var4) > var2.at(var3)) {
                    var3 = var4;
                }
            }

            int var5 = AXIS_COLORS[var3];
            return 0xFF000000 | (var5 >> 16 & 0xFF) / 3 << 16 | (var5 >> 8 & 0xFF) / 3 << 8 | (var5 & 0xFF) / 3;
        } else {
            return -15656673;
        }
    }

    public static ItemStack displayItem(View var0, int var1) {
        if (var0 != null && var0.valid()) {
            Entry var2 = (Entry)var0.entries().get(var1);
            if (var2 == null) {
                return ItemStack.EMPTY;
            } else if (var2.paid()) {
                return var2.storedItem();
            } else {
                ResourceLocation var3 = ResourceLocation.tryParse(var2.value().itemId());
                return var3 != null && BuiltInRegistries.ITEM.containsKey(var3) ? new ItemStack(BuiltInRegistries.ITEM.get(var3)) : ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    public static boolean reference(View var0, int var1) {
        if (var0 != null && var0.valid()) {
            Entry var2 = (Entry)var0.entries().get(var1);
            return var2 != null && !var2.paid();
        } else {
            return false;
        }
    }

    public static boolean unlocked(View var0, int var1) {
        return var0 != null && var0.valid() && FourWeaponFoundationRules.unlocked(var0.profile(), var1, var0.level());
    }

    public static List<ItemStack> unlockedPaid(View var0) {
        if (var0 != null && var0.valid()) {
            ArrayList var1 = new ArrayList();

            for (java.util.Map.Entry var3 : var0.entries().entrySet().stream().sorted(java.util.Map.Entry.comparingByKey()).toList()) {
                if (((Entry)var3.getValue()).paid() && FourWeaponFoundationRules.unlocked(var0.profile(), (Integer)var3.getKey(), var0.level())) {
                    var1.add(((Entry)var3.getValue()).storedItem());
                }
            }

            return List.copyOf(var1);
        } else {
            return List.of();
        }
    }

    public static List<Component> materialRoles(View var0, int var1) {
        if (var0 != null && var0.valid()) {
            Entry var2 = (Entry)var0.entries().get(var1);
            if (var2 == null) {
                return List.of();
            } else {
                Values var3 = var0.profile().items().getOrDefault(var2.value().itemId(), var0.profile().materials().get(var2.value().material()));
                if (var3 == null) {
                    return List.of();
                } else {
                    ArrayList var4 = new ArrayList();

                    for (int var5 = 0; var5 < 5; var5++) {
                        if (var3.at(var5) >= 0.6) {
                            var4.add(axisName(var0.profile(), var5, false));
                        }
                    }

                    return List.copyOf(var4);
                }
            }
        } else {
            return List.of();
        }
    }
}
