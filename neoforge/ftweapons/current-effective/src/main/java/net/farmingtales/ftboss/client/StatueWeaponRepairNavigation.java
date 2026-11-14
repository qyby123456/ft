package net.farmingtales.ftboss.client;

import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.menu.SwordCavityMenu;
import net.farmingtales.ftboss.transaction.FourWeaponClientApi;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions.Snapshot;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class StatueWeaponRepairNavigation {
    private static final long OPEN_TIMEOUT_MILLIS = 10000L;
    private static SwordCavityMenu pendingMenu;
    private static UUID pendingWeapon;
    private static long pendingUntil;

    private StatueWeaponRepairNavigation() {
    }

    public static Button hubEntry(StatueHubScreen var0, int var1, int var2, int var3) {
        return Button.builder(Component.translatable("screen.ftboss.repair.open"), var1x -> var0.openWeaponRepair())
            .bounds(var1, var2, var3, 18)
            .tooltip(Tooltip.create(Component.translatable("screen.ftboss.repair.binding_hint")))
            .build();
    }

    public static void bootstrapHub(BlockPos var0) {
        if (Minecraft.getInstance().screen instanceof StatueHubScreen) {
            Snapshot var1 = FourWeaponClientApi.authority();
            if (var1 != null && var1.allowed().stream().anyMatch(FourWeaponTransactions::menuOperation)) {
                FourWeaponClientApi.close();
            }

            FourWeaponClientApi.bootstrapHub(var0);
        }
    }

    public static Button inspectorEntry(Screen var0, BlockPos var1, Supplier<ItemStack> var2, Runnable var3, BooleanSupplier var4, int var5, int var6, int var7) {
        Button var8 = StatueBuildInspectorScreen.entry(var0, var1, var2, var3, var4, var5, var6 + 20, var7);
        var8.setHeight(18);
        return var8;
    }

    public static Button cavityEntry(SwordCavityMenu var0, int var1, int var2, int var3) {
        return Button.builder(Component.translatable("screen.ftboss.repair.open"), var1x -> {
            Minecraft var2x = Minecraft.getInstance();
            UUID var3x = FoodWeaponLifecycle.weaponId(var0.weapon());
            if (var2x.player != null && var2x.gameMode != null && var2x.player.containerMenu == var0 && var3x != null) {
                pendingMenu = var0;
                pendingWeapon = var3x;
                pendingUntil = Util.getMillis() + 10000L;
                var1x.active = false;
                var2x.gameMode.handleInventoryButtonClick(var0.containerId, 27);
            }
        }).bounds(var1, var2, var3, 20).tooltip(Tooltip.create(Component.translatable("screen.ftboss.repair.binding_hint"))).build();
    }

    public static void openPending(StatueHubScreen var0, BlockPos var1, ItemStack var2) {
        boolean var3 = pendingMenu != null
            && Util.getMillis() <= pendingUntil
            && pendingMenu.matches(var1)
            && pendingWeapon != null
            && pendingWeapon.equals(FoodWeaponLifecycle.weaponId(var2));
        pendingMenu = null;
        pendingWeapon = null;
        pendingUntil = 0L;
        if (var3) {
            var0.openWeaponRepair();
        }
    }

    public static Button hubEntry(OrdinaryStatueHubScreen var0, int var1, int var2, int var3) {
            return Button.builder(Component.translatable("screen.ftboss.repair.open"), var1x -> var0.openWeaponRepair())
                .bounds(var1, var2, var3, 18)
                .tooltip(Tooltip.create(Component.translatable("screen.ftboss.repair.binding_hint")))
                .build();
        }

    public static void openPending(OrdinaryStatueHubScreen var0, BlockPos var1, ItemStack var2) {
            boolean var3 = pendingMenu != null
                && Util.getMillis() <= pendingUntil
                && pendingMenu.matches(var1)
                && pendingWeapon != null
                && pendingWeapon.equals(FoodWeaponLifecycle.weaponId(var2));
            pendingMenu = null;
            pendingWeapon = null;
            pendingUntil = 0L;
            if (var3) {
                var0.openWeaponRepair();
            }
        }

    public static void bootstrapOrdinaryHub(BlockPos var0) {
            if (Minecraft.getInstance().screen instanceof OrdinaryStatueHubScreen) {
                Snapshot var1 = FourWeaponClientApi.authority();
                if (var1 != null && var1.allowed().stream().anyMatch(FourWeaponTransactions::menuOperation)) {
                    FourWeaponClientApi.close();
                }
    
                FourWeaponClientApi.bootstrapHub(var0);
            }
        }
    public static Button cavityEntry(SwordCavityMenu var0, int var1, int var2, int var3, int returnButton) {
        if (returnButton != 27 && returnButton != 28) {
            throw new IllegalArgumentException("Unknown statue Hub return button");
        }
        return Button.builder(Component.translatable("screen.ftboss.repair.open"), var1x -> {
            Minecraft var2x = Minecraft.getInstance();
            UUID var3x = FoodWeaponLifecycle.weaponId(var0.weapon());
            if (var2x.player != null && var2x.gameMode != null && var2x.player.containerMenu == var0 && var3x != null) {
                pendingMenu = var0;
                pendingWeapon = var3x;
                pendingUntil = Util.getMillis() + 10000L;
                var1x.active = false;
                var2x.gameMode.handleInventoryButtonClick(var0.containerId, returnButton);
            }
        }).bounds(var1, var2, var3, 20).tooltip(Tooltip.create(Component.translatable("screen.ftboss.repair.binding_hint"))).build();
    }

    public static int returnButton(SwordCavityMenu menu, Screen previous) {
        if (previous instanceof OrdinaryStatueHubScreen hub && hub.matchesMenu(menu)) {
            return 28;
        }
        if (previous instanceof OrdinaryStatueSwordCavityScreen cavity && cavity.matchesMenu(menu)) {
            return 28;
        }
        return 27;
    }

}
