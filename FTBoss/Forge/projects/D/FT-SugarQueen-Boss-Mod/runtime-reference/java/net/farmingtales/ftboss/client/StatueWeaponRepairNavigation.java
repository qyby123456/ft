/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.Util
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.components.Button
 *  net.minecraft.client.gui.components.Tooltip
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.client;

import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.farmingtales.ftboss.client.StatueBuildInspectorScreen;
import net.farmingtales.ftboss.client.StatueHubScreen;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.menu.SwordCavityMenu;
import net.farmingtales.ftboss.transaction.FourWeaponClientApi;
import net.farmingtales.ftboss.transaction.FourWeaponTransactions;
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

    public static Button hubEntry(StatueHubScreen statueHubScreen, int n, int n2, int n3) {
        return Button.m_253074_((Component)Component.m_237115_((String)"screen.ftboss.repair.open"), button -> statueHubScreen.openWeaponRepair()).m_252987_(n, n2, n3, 18).m_257505_(Tooltip.m_257550_((Component)Component.m_237115_((String)"screen.ftboss.repair.binding_hint"))).m_253136_();
    }

    public static void bootstrapHub(BlockPos blockPos) {
        if (!(Minecraft.m_91087_().f_91080_ instanceof StatueHubScreen)) {
            return;
        }
        FourWeaponTransactions.Snapshot snapshot = FourWeaponClientApi.authority();
        if (snapshot != null && snapshot.allowed().stream().anyMatch(FourWeaponTransactions::menuOperation)) {
            FourWeaponClientApi.close();
        }
        FourWeaponClientApi.bootstrapHub(blockPos);
    }

    public static Button inspectorEntry(Screen screen, BlockPos blockPos, Supplier<ItemStack> supplier, Runnable runnable, BooleanSupplier booleanSupplier, int n, int n2, int n3) {
        Button button = StatueBuildInspectorScreen.entry(screen, blockPos, supplier, runnable, booleanSupplier, n, n2 + 20, n3);
        button.setHeight(18);
        return button;
    }

    public static Button cavityEntry(SwordCavityMenu swordCavityMenu, int n, int n2, int n3) {
        return Button.m_253074_((Component)Component.m_237115_((String)"screen.ftboss.repair.open"), button -> {
            Minecraft minecraft = Minecraft.m_91087_();
            UUID uUID = FoodWeaponLifecycle.weaponId(swordCavityMenu.weapon());
            if (minecraft.f_91074_ == null || minecraft.f_91072_ == null || minecraft.f_91074_.f_36096_ != swordCavityMenu || uUID == null) {
                return;
            }
            pendingMenu = swordCavityMenu;
            pendingWeapon = uUID;
            pendingUntil = Util.m_137550_() + 10000L;
            button.f_93623_ = false;
            minecraft.f_91072_.m_105208_(swordCavityMenu.f_38840_, 27);
        }).m_252987_(n, n2, n3, 20).m_257505_(Tooltip.m_257550_((Component)Component.m_237115_((String)"screen.ftboss.repair.binding_hint"))).m_253136_();
    }

    public static void openPending(StatueHubScreen statueHubScreen, BlockPos blockPos, ItemStack itemStack) {
        boolean bl = pendingMenu != null && Util.m_137550_() <= pendingUntil && pendingMenu.matches(blockPos) && pendingWeapon != null && pendingWeapon.equals(FoodWeaponLifecycle.weaponId(itemStack));
        pendingMenu = null;
        pendingWeapon = null;
        pendingUntil = 0L;
        if (bl) {
            statueHubScreen.openWeaponRepair();
        }
    }
}

