package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.util.GourmetBladeCavityRules;
import net.farmingtales.ftboss.util.ManorWeaponCavityRules;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules.Material;
import net.farmingtales.ftboss.util.SwordCavityRules.Cell;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.farmingtales.ftboss.util.SwordCavityRules.State;
import net.farmingtales.ftboss.util.SwordFunctionRules.Function;
import net.farmingtales.ftboss.util.SwordFunctionRules.Profile;
import net.farmingtales.ftboss.util.SwordFunctionRules.Status;
import net.farmingtales.ftboss.util.SwordFunctionRules.Values;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class GourmetBladeCavity {
    private static final Values BASE = new Values(1.0, 1.0, 1.0, 1.0, 1.0);

    private GourmetBladeCavity() {
    }

    public static boolean applies(ItemStack var0) {
        return identityOnly(var0) || ManorWeaponCavity.applies(var0);
    }

    public static boolean identityOnly(ItemStack var0) {
        return !var0.isEmpty() && var0.getCount() == 1 && FoodWeaponEvolution.isGourmetBlankBlade(var0);
    }

    public static void ensureIdentity(ItemStack var0) {
        MinecraftServer var1 = ServerLifecycleHooks.getCurrentServer();
        if (applies(var0) && var1 != null && var1.isSameThread()) {
            FoodWeaponData.edit(var0, "FTBossWeaponLifecycle", data -> {
                if (!data.hasUUID("Weapon")) {
                    data.putUUID("Weapon", UUID.randomUUID());
                }
            });
        }
    }

    public static boolean hasFunctions(Profile var0, ItemStack var1) {
        return var0 != null || applies(var1);
    }

    public static int maximumCells(Profile var0, ItemStack var1) {
        return applies(var1) ? 27 : SwordFunctionRules.maximumCells(var0);
    }

    public static boolean fixedSlot(Slot var0, ItemStack var1) {
        return var0 != null && fixed(var1, var0.ordinal());
    }

    public static boolean effectiveUnlocked(Profile var0, int var1, int var2, ItemStack var3) {
        return applies(var3) ? unlocked(var3, var1) : SwordFunctionRules.unlocked(var0, var1, var2);
    }

    public static boolean unlocked(ItemStack var0, int var1) {
        return ManorWeaponCavity.applies(var0)
            ? ManorWeaponCavityRules.unlocked(var1, FoodWeaponEvolution.evolutionLevel(var0))
            : GourmetBladeCavityRules.unlocked(var1, FoodWeaponEvolution.evolutionLevel(var0));
    }

    public static Status functionStatus(ItemStack var0) {
        boolean var1 = SwordCavityRules.usable(SwordCavityBuild.cells(var0));
        return new Status(
            var1,
            BASE,
            BASE,
            var1 ? List.of() : List.of(Function.values()),
            ManorWeaponCavity.applies(var0)
                ? ManorWeaponCavityRules.capacity(FoodWeaponEvolution.evolutionLevel(var0))
                : GourmetBladeCavityRules.capacity(FoodWeaponEvolution.evolutionLevel(var0))
        );
    }

    public static boolean fixed(ItemStack var0, int var1) {
        return DiamondAlloyArmorCore.fixed(var0, var1) || applies(var0) && var1 == GourmetBladeCavityRules.CORE;
    }

    public static boolean compatibleContainer(ItemStack var0, List<ItemStack> var1) {
        if (DiamondAlloyArmorCore.applies(var0)) {
            int var2 = DiamondAlloyArmorCore.index(var0, EquipmentWeaponBuild.parts(var0));
            return var2 >= 0 && var1 != null && var1.size() == 27 && var1.get(var2) != null && ((ItemStack)var1.get(var2)).isEmpty();
        } else {
            return !applies(var0)
                || var1 != null
                    && var1.size() == 27
                    && var1.get(GourmetBladeCavityRules.CORE) != null
                    && ((ItemStack)var1.get(GourmetBladeCavityRules.CORE)).isEmpty();
        }
    }

    public static Component referenceHint(ItemStack var0, int var1) {
        if (DiamondAlloyArmorCore.fixed(var0, var1)) {
            return Component.translatable("screen.ftboss.armor.core_fixed").append(" · ").append(Component.translatable("screen.ftboss.armor.core_base"));
        } else {
            return fixed(var0, var1)
                ? Component.translatable(ManorWeaponCavity.applies(var0) ? "screen.ftboss.manor.core_fixed" : "screen.ftboss.gourmet.core_fixed")
                    .append(" · ")
                    .append(Component.translatable(ManorWeaponCavity.applies(var0) ? "screen.ftboss.manor.core_base" : "screen.ftboss.gourmet.core_base"))
                : Component.translatable("screen.ftboss.cavity.reference_short");
        }
    }

    public static List<Cell> cells(ItemStack var0, List<Cell> var1) {
        if (DiamondAlloyArmorCore.applies(var0)) {
            int var4 = DiamondAlloyArmorCore.index(var0, EquipmentWeaponBuild.parts(var0));
            if (var4 < 0) {
                return var1;
            } else {
                ArrayList<Cell> var5 = new ArrayList<>(var1);
                var5.set(var4, new Cell(State.NATIVE, Material.ORIGINAL));
                return List.copyOf(var5);
            }
        } else if (!applies(var0)) {
            return var1;
        } else {
            ArrayList<Cell> var2 = new ArrayList<>(var1);

            for (int var3 = 0; var3 < var2.size(); var3++) {
                if (var3 == GourmetBladeCavityRules.CORE) {
                    var2.set(var3, new Cell(State.NATIVE, Material.ORIGINAL));
                } else if (((Cell)var2.get(var3)).state() == State.NATIVE) {
                    var2.set(var3, new Cell(State.EMPTY, Material.ORIGINAL));
                }
            }

            return List.copyOf(var2);
        }
    }

    public static ItemStack coreDisplay(ItemStack var0) {
        if (DiamondAlloyArmorCore.applies(var0)) {
            return DiamondAlloyArmorCore.display(var0);
        } else {
            ItemStack var1 = new ItemStack(var0.getItem());
            var1.set(DataComponents.CUSTOM_NAME, Component.translatable(ManorWeaponCavity.applies(var0) ? "item.ftboss.manor_weapon_core" : "item.ftboss.gourmet_core"));
            return var1;
        }
    }
}
