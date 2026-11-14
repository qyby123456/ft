package net.farmingtales.ftboss.client.render;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;

public final class GongdiVerdictPlacement {
    private GongdiVerdictPlacement() {
    }

    private static boolean isWeapon(GongdiSpellModel.Model var0, GongdiSpellModel.Part var1) {
        return var0.id().equals("blood_verdict") && var1.name().equals("falling_original_black_cleaver");
    }

    public static boolean avoidActors(boolean var0, GongdiSpellModel.Model var1, GongdiSpellModel.Part var2) {
        return var0 && !isWeapon(var1, var2);
    }

    public static GongdiSpellModel.State stateForCamera(
        GongdiSpellModel.State var0, GongdiSpellModel.Model var1, GongdiSpellModel.Part var2, Vec3 var3, Vec3 var4, double var5, double var7, boolean var9
    ) {
        if (!var9 && isWeapon(var1, var2) && !(var0.alpha() < 0.01)) {
            double[] var10 = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
            double[] var11 = new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
            double var12 = Math.cos(var5);
            double var14 = Math.sin(var5);
            Quaterniond var16 = GongdiSpellModel.rotation(var0);

            for (GongdiSpellModel.Face var20 : var2.faces()) {
                for (double[] var24 : var20.points()) {
                    double[] var25 = GongdiSpellModel.pose(var24, var2, var0, var16);
                    double[] var26 = new double[]{
                        var3.x + var7 * (var25[0] * var12 + var25[2] * var14), var3.y + var7 * var25[1], var3.z + var7 * (-var25[0] * var14 + var25[2] * var12)
                    };

                    for (int var27 = 0; var27 < 3; var27++) {
                        var10[var27] = Math.min(var10[var27], var26[var27]);
                        var11[var27] = Math.max(var11[var27], var26[var27]);
                    }
                }
            }

            double[] var28 = new double[]{var4.x, var4.y, var4.z};
            double var29 = 0.0;

            for (int var30 = 0; var30 < 3; var30++) {
                double var32 = Math.max(0.0, Math.max(var10[var30] - var28[var30], var28[var30] - var11[var30]));
                var29 += var32 * var32;
            }

            double var31 = Math.max(0.0, Math.min(1.0, (Math.sqrt(var29) - 0.31) / 0.34));
            double var33 = var31 * var31 * (3.0 - 2.0 * var31);
            return var33 == 1.0 ? var0 : new GongdiSpellModel.State(var0.position(), var0.rotation(), var0.scale(), var0.alpha() * var33);
        } else {
            return var0;
        }
    }
}
