package net.farmingtales.ftboss.qingyilight.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class LightRenderPlan {
    private LightRenderPlan() {
    }

    public static List<LightRenderPlan.Prepared> prepare(List<LightClient.Draw> var0, Vec3 var1, Predicate<AABB> var2) {
        ArrayList var3 = new ArrayList(var0.size());

        for (LightClient.Draw var5 : var0) {
            double var6 = var5.anchor().distanceToSqr(var1);
            if (!(var6 > 4096.0)) {
                double var8 = var5.packet().entityId() >= 0 ? 28.0 : Math.max(4.0, var5.packet().radius() + 1.0);
                if (var2 == null || var2.test(new AABB(var5.anchor(), var5.anchor()).inflate(var8))) {
                    double var10 = Math.atan2(var5.direction().x, var5.direction().z);
                    Matrix4f var12 = new Matrix4f()
                        .translation((float)(var5.anchor().x - var1.x), (float)(var5.anchor().y - var1.y), (float)(var5.anchor().z - var1.z))
                        .rotateY((float)var10);
                    var3.add(new LightRenderPlan.Prepared(var5, var6, var12));
                }
            }
        }

        var3.sort(Comparator.comparingDouble(LightRenderPlan.Prepared::distanceSquared));
        return var3;
    }

    public static record Prepared(LightClient.Draw scene, double distanceSquared, Matrix4f placement) {
    }
}
