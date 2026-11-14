/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.client.render.QizaiCrystalModelVisual;
import net.farmingtales.ftboss.client.render.QizaiMagicVisual;
import net.farmingtales.ftboss.util.QizaiGeometry;
import net.farmingtales.ftboss.util.QizaiPrismRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.Vec3;

public final class QizaiPrismVisual {
    private QizaiPrismVisual() {
    }

    public static void render(CompoundTag cue, double age, boolean reduced, PoseStack pose, VertexConsumer vertices) {
        QizaiPrismVisual.render(cue, age, reduced, pose, vertices, false);
    }

    public static void render(CompoundTag cue, double age, boolean reduced, PoseStack pose, VertexConsumer vertices, boolean modelsOnly) {
        int track = Math.max(1, cue.m_128451_("trackTicks"));
        int lock = Math.max(1, cue.m_128451_("lockTicks"));
        int gap = Math.max(1, cue.m_128451_("fireGap"));
        int fadeTicks = Math.max(1, cue.m_128451_("fadeTicks"));
        boolean locked = cue.m_128471_("locked");
        double chargeProgress = Math.max(0.0, Math.min(1.0, age / (double)track));
        ListTag list = cue.m_128437_("beams", 10);
        int count = Math.min(6, list.size());
        for (int index = 0; index < count; ++index) {
            CompoundTag entry = list.m_128728_(index);
            Vec3 origin = QizaiPrismVisual.point(entry, "");
            Vec3 end = QizaiPrismVisual.point(entry, "end");
            if (!QizaiPrismVisual.valid(origin) || !QizaiPrismVisual.valid(end) || origin.m_82557_(end) > 1024.0) continue;
            double firedAge = age - (double)QizaiPrismRules.fireTick(index, track, lock, gap);
            if (modelsOnly) {
                QizaiCrystalModelVisual.render(origin, age + (double)(index * 21), 1.15, firedAge < 0.0 ? 1.0 : 1.0 - firedAge / (double)fadeTicks, pose, vertices);
                continue;
            }
            if (firedAge < 0.0) {
                QizaiPrismVisual.beam(origin, end, locked ? 0.12 : 0.075, locked ? 0.95f : 0.7f + 0.15f * (float)chargeProgress, 15219456, pose, vertices);
                if (!locked) continue;
                QizaiPrismVisual.beam(origin, end, 0.04, 0.95f, 16769098, pose, vertices);
                continue;
            }
            if (!(firedAge < (double)fadeTicks)) continue;
            float fade = (float)Math.max(0.0, 1.0 - firedAge / (double)fadeTicks);
            QizaiPrismVisual.beam(origin, end, 0.55, fade * 0.65f, 10821083, pose, vertices);
            QizaiPrismVisual.beam(origin, end, 0.14, fade, 16769098, pose, vertices);
        }
        Vec3 target = QizaiPrismVisual.point(cue, "target");
        if (!modelsOnly && QizaiPrismVisual.valid(target) && count > 0 && age < (double)(QizaiPrismRules.lastFireTick(track, lock, gap) + fadeTicks)) {
            pose.m_85836_();
            pose.m_85837_(target.f_82479_, target.f_82480_, target.f_82481_);
            QizaiGeometry.sigil(locked ? 0.9 + Math.sin(age * 0.55) * 0.15 : 0.45 + chargeProgress * 0.35, locked ? 1.0 : chargeProgress, age >= (double)(track + lock), reduced, QizaiMagicVisual.sink(pose, vertices));
            pose.m_85849_();
        }
    }

    private static Vec3 point(CompoundTag tag, String prefix) {
        return new Vec3(tag.m_128459_(prefix + "X"), tag.m_128459_(prefix + "Y"), tag.m_128459_(prefix + "Z"));
    }

    private static boolean valid(Vec3 point) {
        return Double.isFinite(point.f_82479_ + point.f_82480_ + point.f_82481_);
    }

    private static QizaiGeometry.Point vertex(Vec3 point) {
        return new QizaiGeometry.Point(point.f_82479_, point.f_82480_, point.f_82481_);
    }

    private static void beam(Vec3 origin, Vec3 end, double width, float alpha, int color, PoseStack pose, VertexConsumer vertices) {
        Vec3 forward = end.m_82546_(origin);
        if (forward.m_82556_() < 1.0E-8 || width <= 0.0 || alpha <= 0.0f) {
            return;
        }
        Vec3 side = (forward = forward.m_82541_()).m_82537_(new Vec3(0.0, 1.0, 0.0)).m_82541_();
        if (side.m_82556_() < 0.01) {
            side = new Vec3(1.0, 0.0, 0.0);
        }
        Vec3 up = forward.m_82537_(side).m_82541_();
        QizaiGeometry.Sink sink = QizaiMagicVisual.sink(pose, vertices);
        for (Vec3 offset : new Vec3[]{side.m_82490_(width), up.m_82490_(width)}) {
            sink.quad(QizaiPrismVisual.vertex(origin.m_82549_(offset)), QizaiPrismVisual.vertex(end.m_82549_(offset)), QizaiPrismVisual.vertex(end.m_82546_(offset)), QizaiPrismVisual.vertex(origin.m_82546_(offset)), color, alpha);
        }
    }
}

