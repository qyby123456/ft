/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.client;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.SwordNativeMaterials;
import net.farmingtales.ftboss.util.SwordFunctionRules;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;
import net.farmingtales.ftboss.util.foundations.FourWeaponFoundationRules;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class CavityPresentationCache {
    private static final Map<Object, Frame> FRAMES = new WeakHashMap<Object, Frame>();

    private CavityPresentationCache() {
    }

    public static Frame frame(Object object, ItemStack itemStack, Level level) {
        Frame frame = FRAMES.get(object);
        if (frame != null && frame.weapon().m_41613_() == itemStack.m_41613_() && ItemStack.m_150942_((ItemStack)frame.weapon(), (ItemStack)itemStack)) {
            return frame;
        }
        FourWeaponFoundationAdapter.View view = FourWeaponFoundationAdapter.applies(itemStack) ? FourWeaponFoundationAdapter.read(itemStack) : null;
        FourWeaponFoundationRules.Status status = view != null && view.valid() ? view.status() : null;
        List<SwordNativeMaterialRules.Piece> list = view == null ? SwordNativeMaterials.parts(itemStack, level) : List.of();
        SwordFunctionRules.Status status2 = view == null ? SwordCavityBuild.functionStatus(itemStack) : null;
        Frame frame2 = new Frame(itemStack.m_41777_(), view, status, list, status2);
        FRAMES.put(object, frame2);
        return frame2;
    }

    public record Frame(ItemStack weapon, FourWeaponFoundationAdapter.View foundation, FourWeaponFoundationRules.Status status, List<SwordNativeMaterialRules.Piece> nativeParts, SwordFunctionRules.Status swordStatus) {
    }
}

