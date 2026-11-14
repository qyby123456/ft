/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaterniond
 */
package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.client.render.GongdiSpellModel;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;

public final class GongdiVerdictPlacement {
    private GongdiVerdictPlacement() {
    }

    private static boolean isWeapon(GongdiSpellModel.Model model, GongdiSpellModel.Part part) {
        return model.id().equals("blood_verdict") && part.name().equals("falling_original_black_cleaver");
    }

    public static boolean avoidActors(boolean bl, GongdiSpellModel.Model model, GongdiSpellModel.Part part) {
        return bl && !GongdiVerdictPlacement.isWeapon(model, part);
    }

    public static GongdiSpellModel.State stateForCamera(GongdiSpellModel.State state, GongdiSpellModel.Model model, GongdiSpellModel.Part part, Vec3 vec3, Vec3 vec32, double d, double d2, boolean bl) {
        if (bl || !GongdiVerdictPlacement.isWeapon(model, part) || state.alpha() < 0.01) {
            return state;
        }
        double[] dArray = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        double[] dArray2 = new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        double d3 = Math.cos(d);
        double d4 = Math.sin(d);
        Quaterniond quaterniond = GongdiSpellModel.rotation(state);
        for (GongdiSpellModel.Face i : part.faces()) {
            for (double[] dArray3 : i.points()) {
                double[] dArray4 = GongdiSpellModel.pose(dArray3, part, state, quaterniond);
                double[] dArray5 = new double[]{vec3.f_82479_ + d2 * (dArray4[0] * d3 + dArray4[2] * d4), vec3.f_82480_ + d2 * dArray4[1], vec3.f_82481_ + d2 * (-dArray4[0] * d4 + dArray4[2] * d3)};
                for (int j = 0; j < 3; ++j) {
                    dArray[j] = Math.min(dArray[j], dArray5[j]);
                    dArray2[j] = Math.max(dArray2[j], dArray5[j]);
                }
            }
        }
        double[] dArray6 = new double[]{vec32.f_82479_, vec32.f_82480_, vec32.f_82481_};
        double d5 = 0.0;
        for (int d7 = 0; d7 < 3; ++d7) {
            double d6 = Math.max(0.0, Math.max(dArray[d7] - dArray6[d7], dArray6[d7] - dArray2[d7]));
            d5 += d6 * d6;
        }
        double d7 = Math.max(0.0, Math.min(1.0, (Math.sqrt(d5) - 0.31) / 0.34));
        double d8 = d7 * d7 * (3.0 - 2.0 * d7);
        if (d8 == 1.0) {
            return state;
        }
        return new GongdiSpellModel.State(state.position(), state.rotation(), state.scale(), state.alpha() * d8);
    }
}

