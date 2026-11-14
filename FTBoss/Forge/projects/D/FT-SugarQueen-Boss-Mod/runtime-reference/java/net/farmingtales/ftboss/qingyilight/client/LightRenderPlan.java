/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 */
package net.farmingtales.ftboss.qingyilight.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.farmingtales.ftboss.qingyilight.client.LightClient;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class LightRenderPlan {
    private LightRenderPlan() {
    }

    public static List<Prepared> prepare(List<LightClient.Draw> list, Vec3 vec3, Predicate<AABB> predicate) {
        ArrayList<Prepared> arrayList = new ArrayList<Prepared>(list.size());
        for (LightClient.Draw draw : list) {
            double d;
            double d2 = draw.anchor().m_82557_(vec3);
            if (d2 > 4096.0) continue;
            double d3 = d = draw.packet().entityId() >= 0 ? 28.0 : Math.max(4.0, draw.packet().radius() + 1.0);
            if (predicate != null && !predicate.test(new AABB(draw.anchor(), draw.anchor()).m_82400_(d))) continue;
            double d4 = Math.atan2(draw.direction().f_82479_, draw.direction().f_82481_);
            Matrix4f matrix4f = new Matrix4f().translation((float)(draw.anchor().f_82479_ - vec3.f_82479_), (float)(draw.anchor().f_82480_ - vec3.f_82480_), (float)(draw.anchor().f_82481_ - vec3.f_82481_)).rotateY((float)d4);
            arrayList.add(new Prepared(draw, d2, matrix4f));
        }
        arrayList.sort(Comparator.comparingDouble(Prepared::distanceSquared));
        return arrayList;
    }

    public record Prepared(LightClient.Draw scene, double distanceSquared, Matrix4f placement) {
    }
}

