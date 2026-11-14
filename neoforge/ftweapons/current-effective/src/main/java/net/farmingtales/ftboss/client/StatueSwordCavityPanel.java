package net.farmingtales.ftboss.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.farmingtales.ftboss.item.SwordCavityBuild;
import net.farmingtales.ftboss.item.SwordNativeMaterials;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules;
import net.farmingtales.ftboss.util.SwordCavityRules.Cell;
import net.farmingtales.ftboss.util.SwordCavityRules.Slot;
import net.farmingtales.ftboss.util.SwordCavityRules.State;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Origin;
import net.farmingtales.ftboss.util.SwordNativeMaterialRules.Piece;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class StatueSwordCavityPanel {
    private static final int DIAGRAM_X = 14;
    private static final int DIAGRAM_Y = 39;
    private static final int DETAIL_X = 150;
    private static final int WARNING = -26000;
    private Slot selected;
    public static final int MUTED = -5594189;
    public static final int PITCH = 32;
    public static final int CARD = -13619915;
    public static final int TEXT = -1515055;

    public Slot hit(double var1, double var3, int var5, int var6) {
        for (Slot var10 : Slot.values()) {
            if (inside(
                var1,
                var3,
                var5 + SwordNativeMaterialRules.visualColumn(var10.ordinal()) * 32,
                var6 + SwordNativeMaterialRules.visualRow(var10.ordinal()) * 32,
                32,
                32
            )) {
                return var10;
            }
        }

        return null;
    }

    public List<Component> render(GuiGraphics var1, Font var2, ItemStack var3, Slot var4, Slot var5, int var6, int var7, double var8, double var10) {
        return this.render(var1, var2, var3, var4, var5, var6, var7, var8, var10, SwordNativeMaterials.parts(var3));
    }

    public List<Component> render(
        GuiGraphics var1, Font var2, ItemStack var3, Slot var4, Slot var5, int var6, int var7, double var8, double var10, List<Piece> var12
    ) {
        List var13 = SwordCavityBuild.cells(var3, var12);
        Slot var14 = this.hit(var8, var10, var6, var7);

        for (Slot var18 : Slot.values()) {
            Cell var19 = (Cell)var13.get(var18.ordinal());
            int var20 = var6 + SwordNativeMaterialRules.visualColumn(var18.ordinal()) * 32;
            int var21 = var7 + SwordNativeMaterialRules.visualRow(var18.ordinal()) * 32;
            ItemStack var22 = SwordCavityBuild.displayItem(var3, var18, var12);
            if (!var22.isEmpty()) {
                var22 = var22.copy();
                var22.setCount(1);
            }

            item(var1, var2, var22, var20, var21, var18 == var4, false);
            if (var18 == var5) {
                var1.fill(var20 + 2, var21 + 2, var20 + 32 - 2, var21 + 3, -8199511);
                var1.fill(var20 + 2, var21 + 32 - 3, var20 + 32 - 2, var21 + 32 - 2, -8199511);
            }

            String var23 = var19.state() == State.NATIVE
                ? "native"
                : (var19.state() == State.INSTALLED ? "installed" : (var19.state() == State.MISSING ? "missing" : ""));
            if (var19.state() == State.NATIVE && SwordNativeMaterialRules.hasPiece(var12, var18.ordinal())) {
                Origin var24 = ((Piece)var12.get(var18.ordinal())).origin();
                if (var24 != Origin.RECIPE) {
                    var23 = var24 == Origin.SHARED ? "shared" : "theme";
                }
            }

            if (!var23.isEmpty()) {
                var1.fill(var20 + 32 - 13, var21 + 2, var20 + 32 - 2, var21 + 12, -300477409);
                var1.drawCenteredString(
                    var2,
                    Component.translatable("screen.ftboss.cavity.badge." + var23),
                    var20 + 32 - 8,
                    var21 + 3,
                    var19.state() == State.MISSING ? -31370 : -1515055
                );
            }
        }

        return var14 == null ? List.of() : tooltip(var3, var14, var12);
    }

    public static List<Component> tooltip(ItemStack var0, Slot var1) {
        return tooltip(var0, var1, SwordNativeMaterials.parts(var0));
    }

    public static List<Component> tooltip(ItemStack var0, Slot var1, List<Piece> var2) {
        Cell var3 = (Cell)SwordCavityBuild.cells(var0, var2).get(var1.ordinal());
        ArrayList var4 = new ArrayList();
        if (var3.present()) {
            ItemStack var5 = SwordCavityBuild.displayItem(var0, var1, var2);
            var4.add(var5.isEmpty() ? WeaponMaterialBuild.materialName(var3.material()) : var5.getHoverName());
            var4.add(Component.translatable(var3.state() == State.NATIVE ? "screen.ftboss.cavity.native" : "screen.ftboss.cavity.installed"));
            if (var3.state() == State.NATIVE && SwordNativeMaterialRules.hasPiece(var2, var1.ordinal())) {
                Piece var6 = (Piece)var2.get(var1.ordinal());
                var4.add(Component.translatable("screen.ftboss.cavity.native." + var6.origin().name().toLowerCase(Locale.ROOT)));
            }

            if (var3.state() == State.INSTALLED) {
                var4.addAll(SwordCavityBuild.materialEffects(var3.material()));
            }
        } else {
            var4.add(
                Component.translatable(
                    var3.state() == State.EMPTY ? "screen.ftboss.cavity.empty" : (var1.required ? "screen.ftboss.cavity.missing" : "screen.ftboss.cavity.lost")
                )
            );
        }

        return List.copyOf(var4);
    }

    public static void item(GuiGraphics var0, Font var1, ItemStack var2, int var3, int var4, boolean var5, boolean var6) {
        var0.fill(var3 + 1, var4 + 1, var3 + 32 - 1, var4 + 32 - 1, var5 ? -10374 : -9147261);
        var0.fill(var3 + 2, var4 + 2, var3 + 32 - 2, var4 + 32 - 2, -13619915);
        if (!var2.isEmpty()) {
            var0.pose().pushPose();
            var0.pose().translate((float)(var3 + 4), (float)(var4 + 4), 0.0F);
            var0.pose().scale(1.5F, 1.5F, 1.0F);
            var0.renderItem(var2, 0, 0);
            var0.pose().popPose();
            if (var2.getCount() > 1) {
                var0.drawString(
                    var1, Integer.toString(var2.getCount()), var3 + 32 - 4 - var1.width(Integer.toString(var2.getCount())), var4 + 32 - 11, -1515055, true
                );
            }

            if (var6) {
                var0.fill(var3 + 2, var4 + 2, var3 + 32 - 2, var4 + 32 - 2, -2145971169);
            }
        }
    }

    public static boolean inside(double var0, double var2, int var4, int var5, int var6, int var7) {
        return var0 >= (double)var4 && var0 < (double)(var4 + var6) && var2 >= (double)var5 && var2 < (double)(var5 + var7);
    }
}
