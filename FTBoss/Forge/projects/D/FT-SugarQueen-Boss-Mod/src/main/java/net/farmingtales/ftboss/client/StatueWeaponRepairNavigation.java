package net.farmingtales.ftboss.client;

import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
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

/** 修复入口与待返回导航；2026-10-24 替换主题按钮，保留原有容器返回握手与超时。 */
public final class StatueWeaponRepairNavigation {
    private static final long OPEN_TIMEOUT_MILLIS = 10000L;
    private static SwordCavityMenu pendingMenu;
    private static UUID pendingWeapon;
    private static long pendingUntil;

    private StatueWeaponRepairNavigation() {}

    public static Button hubEntry(StatueHubScreen hub, int x, int y, int width) {
        Button entry = StatueThemeButton.create(Component.translatable("screen.ftboss.repair.open"),
                button -> hub.openWeaponRepair(), x, y, width, 18, hub::statueTheme);
        entry.setTooltip(Tooltip.create(Component.translatable("screen.ftboss.repair.binding_hint")));
        return entry;
    }

    public static void bootstrapHub(BlockPos pos) {
        if (!(Minecraft.getInstance().screen instanceof StatueHubScreen)) return;
        var authority = FourWeaponClientApi.authority();
        if (authority != null && authority.allowed().stream().anyMatch(FourWeaponTransactions::menuOperation)) {
            FourWeaponClientApi.close();
        }
        FourWeaponClientApi.bootstrapHub(pos);
    }

    public static Button inspectorEntry(Screen parent, BlockPos pos, Supplier<ItemStack> weapon, Runnable adjust,
                                        BooleanSupplier allowed, int x, int y, int width) {
        Button entry = StatueBuildInspectorScreen.entry(parent, pos, weapon, adjust, allowed, x, y + 20, width);
        entry.setHeight(18);
        return entry;
    }

    public static Button cavityEntry(SwordCavityMenu menu, int x, int y, int width) {
        StatueUiTheme theme = StatueUiTheme.fromScreen(Minecraft.getInstance().screen);
        Button entry = StatueThemeButton.create(Component.translatable("screen.ftboss.repair.open"), button -> {
            Minecraft client = Minecraft.getInstance();
            UUID identity = FoodWeaponLifecycle.weaponId(menu.weapon());
            if (client.player == null || client.gameMode == null || client.player.containerMenu != menu || identity == null) return;
            pendingMenu = menu;
            pendingWeapon = identity;
            pendingUntil = Util.getMillis() + OPEN_TIMEOUT_MILLIS;
            button.active = false;
            client.gameMode.handleInventoryButtonClick(menu.containerId, SwordCavityMenu.BACK_BUTTON);
        }, x, y, width, 20, () -> theme);
        entry.setTooltip(Tooltip.create(Component.translatable("screen.ftboss.repair.binding_hint")));
        return entry;
    }

    public static void openPending(StatueHubScreen hub, BlockPos pos, ItemStack weapon) {
        boolean matches = pendingMenu != null && Util.getMillis() <= pendingUntil && pendingMenu.matches(pos)
                && pendingWeapon != null && pendingWeapon.equals(FoodWeaponLifecycle.weaponId(weapon));
        pendingMenu = null;
        pendingWeapon = null;
        pendingUntil = 0L;
        if (matches) hub.openWeaponRepair();
    }
}
