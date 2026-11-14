package net.farmingtales.ftboss.client;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.SwordNativeMaterials;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.View;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Piece;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules.Status;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class CavityPresentationCache {
    private static final Map<Object, CavityPresentationCache.Frame> FRAMES = new WeakHashMap<>();

    private CavityPresentationCache() {
    }

    public static CavityPresentationCache.Frame frame(Object var0, ItemStack var1, Level var2) {
        CavityPresentationCache.Frame var3 = FRAMES.get(var0);
        if (var3 != null && var3.weapon().getCount() == var1.getCount() && ItemStack.isSameItemSameComponents(var3.weapon(), var1)) {
            return var3;
        } else {
            View var4 = FourWeaponFoundationAdapter.applies(var1) ? FourWeaponFoundationAdapter.read(var1) : null;
            Status var5 = var4 != null && var4.valid() ? var4.status() : null;
            List var6 = var4 == null ? SwordNativeMaterials.parts(var1, var2) : List.of();
            net.farmingtales.ftboss.util.SwordFunctionRules.Status var7 = var4 == null ? SwordCavityBuild.functionStatus(var1) : null;
            CavityPresentationCache.Frame var8 = new CavityPresentationCache.Frame(var1.copy(), var4, var5, var6, var7);
            FRAMES.put(var0, var8);
            return var8;
        }
    }

    public static record Frame(
        ItemStack weapon, View foundation, Status status, List<Piece> nativeParts, net.farmingtales.ftboss.util.SwordFunctionRules.Status swordStatus
    ) {
    }
}
