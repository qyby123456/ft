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
import net.farmingtales.ftboss.client.render.QizaiLaserBarrageModel;
import net.farmingtales.ftboss.client.render.QizaiMagicVisual;
import net.farmingtales.ftboss.util.QizaiGeometry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.Vec3;

public final class QizaiLaserBarrageVisual {
    private QizaiLaserBarrageVisual() {
    }

    public static void render(CompoundTag cue, double age, boolean reduced, PoseStack pose, VertexConsumer vertices) {
        QizaiLaserBarrageVisual.render(cue, age, reduced, pose, vertices, false);
    }

    public static void render(CompoundTag cue, double age, boolean reduced, PoseStack pose, VertexConsumer vertices, boolean modelsOnly) {
        int fadeTicks = Math.max(1, cue.m_128451_("fadeTicks"));
        ListTag list = cue.m_128437_("beams", 10);
        int count = Math.min(120, list.size());
        for (int index = 0; index < count; ++index) {
            CompoundTag entry = list.m_128728_(index);
            Vec3 origin = QizaiLaserBarrageVisual.point(entry, "");
            Vec3 end = QizaiLaserBarrageVisual.point(entry, "end");
            if (!QizaiLaserBarrageVisual.valid(origin) || !QizaiLaserBarrageVisual.valid(end) || origin.m_82557_(end) > 2304.0) continue;
            double spawnedAge = age - (double)entry.m_128451_("spawn");
            double firedAge = age - (double)entry.m_128451_("fire");
            if (spawnedAge < 0.0 || firedAge >= (double)fadeTicks) continue;
            boolean targeted = entry.m_128471_("targeted");
            float pulse = 0.58f + 0.24f * (float)Math.sin(age * 0.85 + (double)index * 0.7);
            QizaiGeometry.Sink sink = QizaiMagicVisual.sink(pose, vertices);
            if (modelsOnly) {
                float modelFade = firedAge < 0.0 ? 1.0f : (float)Math.max(0.0, 1.0 - firedAge / (double)fadeTicks);
                QizaiLaserBarrageModel.emitter(QizaiLaserBarrageVisual.vertex(origin), QizaiLaserBarrageVisual.vertex(end), targeted, firedAge >= 0.0, age, index, reduced, modelFade, sink);
                continue;
            }
            if (firedAge < 0.0) {
                float warningAlpha = (targeted ? 0.82f : 0.58f) * pulse;
                QizaiLaserBarrageModel.warning(QizaiLaserBarrageVisual.vertex(origin), QizaiLaserBarrageVisual.vertex(end), targeted, age, index, reduced, warningAlpha, sink);
                continue;
            }
            float fade = (float)Math.max(0.0, 1.0 - firedAge / (double)fadeTicks);
            QizaiLaserBarrageModel.firing(QizaiLaserBarrageVisual.vertex(origin), QizaiLaserBarrageVisual.vertex(end), targeted, age, index, reduced, fade, sink);
        }
        Vec3 center = QizaiLaserBarrageVisual.point(cue, "center");
        int activeTicks = Math.max(1, cue.m_128451_("activeTicks"));
        double roundAge = age - (double)cue.m_128451_("roundStart");
        if (!modelsOnly && QizaiLaserBarrageVisual.valid(center) && roundAge >= 0.0 && roundAge < (double)activeTicks && !reduced) {
            pose.m_85836_();
            pose.m_85837_(center.f_82479_, center.f_82480_ + 0.04, center.f_82481_);
            QizaiGeometry.sigil(24.0, Math.min(1.0, roundAge / (double)activeTicks), false, true, QizaiMagicVisual.sink(pose, vertices));
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
}

