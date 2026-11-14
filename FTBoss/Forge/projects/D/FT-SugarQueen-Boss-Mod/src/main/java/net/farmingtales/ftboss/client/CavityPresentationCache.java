package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.api.SwordCavityExtensions;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.SwordNativeMaterials;
import net.farmingtales.ftboss.util.SwordCavityRules;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Piece;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** 配件界面只读呈现缓存；2026-10-20 打开或完整物品状态变化时计算，不参与服务器交易与权限判定。 */
public final class CavityPresentationCache {
    private static final Map<Object, Frame> FRAMES = new WeakHashMap<>();

    public record Frame(ItemStack weapon, FourWeaponFoundationAdapter.View foundation, FourWeaponFoundationRules.Status status,
                        List<Piece> nativeParts, SwordFunctionRules.Status swordStatus,
                        SwordFunctionRules.Profile profile, List<Cell> cells) {}

    public record Cell(boolean unlocked, boolean reference, ItemStack icon, int stateColor) {}

    private CavityPresentationCache() {}

    public static Frame frame(Object screen, ItemStack weapon, Level level) {
        Frame cached = FRAMES.get(screen);
        if (cached != null && cached.weapon().getCount() == weapon.getCount() && ItemStack.isSameItemSameTags(cached.weapon(), weapon)) return cached;
        var foundation = FourWeaponFoundationAdapter.applies(weapon) ? FourWeaponFoundationAdapter.read(weapon) : null;
        var status = foundation != null && foundation.valid() ? foundation.status() : null;
        var nativeParts = foundation == null ? SwordNativeMaterials.parts(weapon, level) : List.<Piece>of();
        var swordStatus = foundation == null ? SwordCavityBuild.functionStatus(weapon) : null;
        var profile = foundation == null ? SwordCavityExtensions.functionProfile(weapon) : null;
        var nativeCells = foundation == null ? SwordCavityBuild.cells(weapon, nativeParts) : List.<SwordCavityRules.Cell>of();
        int evolution = foundation == null ? FoodWeaponEvolution.evolutionLevel(weapon) : 0;
        List<Cell> cells = new ArrayList<>(SwordCavityRules.CELL_COUNT);
        for (int index = 0; index < SwordCavityRules.CELL_COUNT; index++) {
            if (foundation != null) {
                boolean unlocked = FourWeaponCavityPresentation.unlocked(foundation, index);
                var entry = foundation.entries().get(index);
                ItemStack icon = FourWeaponCavityPresentation.displayItem(foundation, index);
                int state = !unlocked ? 0xFFE2BD68 : entry == null ? 0 : entry.paid() ? 0xFF72D99A : icon.isEmpty() ? 0 : 0xFF7FB3D9;
                cells.add(new Cell(unlocked, FourWeaponCavityPresentation.reference(foundation, index), icon, state));
            } else {
                var cell = index < nativeCells.size() ? nativeCells.get(index) : null;
                boolean unlocked = cell != null && SwordCavityBuild.cellUnlocked(weapon, index);
                ItemStack icon = cell == null ? ItemStack.EMPTY : SwordCavityBuild.displayItem(weapon, SwordCavityRules.Slot.byIndex(index), nativeParts);
                int state = cell == null ? 0 : !unlocked ? 0xFFE2BD68 : switch (cell.state()) {
                    case INSTALLED -> 0xFF72D99A;
                    case NATIVE -> 0xFF7FB3D9;
                    case MISSING -> 0xFFFF9180;
                    default -> 0;
                };
                cells.add(new Cell(unlocked, cell != null && cell.present(), icon, state));
            }
        }
        Frame current = new Frame(weapon.copy(), foundation, status, nativeParts, swordStatus, profile, List.copyOf(cells));
        FRAMES.put(screen, current);
        return current;
    }
}
