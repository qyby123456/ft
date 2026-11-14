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
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneArsenalVisuals;
import net.farmingtales.ftboss.client.render.QizaiCrystalModelVisual;
import net.farmingtales.ftboss.client.render.QizaiMagicVisual;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.QizaiGeometry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.Vec3;

public final class QizaiArsenalVisual {
    private QizaiArsenalVisual() {
    }

    public static void render(EasterBossEntity boss, double time, boolean reduced, PoseStack pose, VertexConsumer vertices) {
        QizaiArsenalVisual.render(boss, time, reduced, pose, vertices, false);
    }

    /*
     * WARNING - void declaration
     */
    public static void render(EasterBossEntity easterBossEntity, double d, boolean bl, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl2) {
        void vertices;
        void pose;
        void reduced;
        void time;
        void modelsOnly;
        EasterBossEntity boss;
        if (!bl2 && ArcaneArsenalVisuals.available(easterBossEntity)) {
            return;
        }
        CompoundTag cue = boss.getQizaiArsenalCue();
        int primary = boss.getSkillCue().m_128451_("qizai");
        if (modelsOnly == false && cue.m_128471_("shield") && primary != 7 && primary != 8) {
            QizaiArsenalVisual.shield(boss, (double)time, (boolean)reduced, (PoseStack)pose, (VertexConsumer)vertices);
        }
        ListTag salvos = cue.m_128437_("salvos", 10);
        for (int index = 0; index < Math.min(4, salvos.size()); ++index) {
            CompoundTag salvo = salvos.m_128728_(index);
            void age = time - (double)salvo.m_128454_("start");
            if (age < 0.0 || age >= 42.0) continue;
            ListTag crystals = salvo.m_128437_("crystals", 10);
            for (int crystalIndex = 0; crystalIndex < Math.min(6, crystals.size()); ++crystalIndex) {
                float alpha;
                CompoundTag crystal = crystals.m_128728_(crystalIndex);
                Vec3 origin = QizaiArsenalVisual.point(crystal, "");
                Vec3 end = QizaiArsenalVisual.point(crystal, "end");
                if (!QizaiArsenalVisual.valid(origin) || !QizaiArsenalVisual.valid(end) || origin.m_82557_(end) > 1156.0 || boss.m_20238_(origin) > 4096.0) continue;
                void fired = age - 24.0 - (double)(crystalIndex * 2);
                float f = alpha = fired < 0.0 ? 0.65f : (float)Math.max(0.0, 1.0 - fired / 8.0);
                if (modelsOnly != false) {
                    QizaiCrystalModelVisual.render(origin, (double)(age + (double)(crystalIndex * 21)), 1.0, fired < 0.0 ? 1.0 : (double)alpha, (PoseStack)pose, (VertexConsumer)vertices);
                    continue;
                }
                if (alpha <= 0.0f) continue;
                if (fired < 0.0) {
                    QizaiArsenalVisual.beam(origin, end, 0.085, 0.85f, 15219456, (PoseStack)pose, (VertexConsumer)vertices);
                    continue;
                }
                QizaiArsenalVisual.beam(origin, end, 0.45, alpha * 0.6f, 10821083, (PoseStack)pose, (VertexConsumer)vertices);
                QizaiArsenalVisual.beam(origin, end, 0.12, alpha, 16769098, (PoseStack)pose, (VertexConsumer)vertices);
            }
        }
        if (modelsOnly == false && cue.m_128425_("push", 10)) {
            int duration;
            CompoundTag push = cue.m_128469_("push");
            Vec3 origin = QizaiArsenalVisual.point(push, "");
            boolean released = push.m_128471_("released");
            void age = time - (double)push.m_128454_("start");
            int n = duration = released ? 12 : 16;
            if (QizaiArsenalVisual.valid(origin) && age >= 0.0 && age < (double)duration) {
                pose.m_85836_();
                pose.m_85837_(origin.f_82479_, origin.f_82480_ + 0.08, origin.f_82481_);
                QizaiGeometry.sigil(7.0, (double)(age / (double)duration), released, (boolean)reduced, QizaiMagicVisual.sink((PoseStack)pose, (VertexConsumer)vertices));
                pose.m_85849_();
            }
        }
    }

    public static void shield(EasterBossEntity boss, double time, boolean reduced, PoseStack pose, VertexConsumer vertices) {
        pose.m_85836_();
        pose.m_85837_(boss.m_20185_(), boss.m_20186_() + 1.1, boss.m_20189_());
        int count = reduced ? 4 : 8;
        for (int index = 0; index < count; ++index) {
            double angle = time * 0.06 + (double)index * Math.PI * 2.0 / (double)count;
            pose.m_85836_();
            pose.m_85837_(Math.cos(angle) * 1.25, Math.sin(angle * 2.0) * 0.3, Math.sin(angle) * 1.25);
            QizaiGeometry.crystal(0.12, 0.4, -angle, 0.7f, QizaiMagicVisual.sink(pose, vertices));
            pose.m_85849_();
        }
        pose.m_85849_();
    }

    private static Vec3 point(CompoundTag cue, String prefix) {
        return new Vec3(cue.m_128459_(prefix + "x"), cue.m_128459_(prefix + "y"), cue.m_128459_(prefix + "z"));
    }

    private static boolean valid(Vec3 point) {
        return Double.isFinite(point.f_82479_ + point.f_82480_ + point.f_82481_);
    }

    private static QizaiGeometry.Point vertex(Vec3 point) {
        return new QizaiGeometry.Point(point.f_82479_, point.f_82480_, point.f_82481_);
    }

    private static void beam(Vec3 origin, Vec3 end, double width, float alpha, int color, PoseStack pose, VertexConsumer vertices) {
        Vec3 forward = end.m_82546_(origin).m_82541_();
        Vec3 side = forward.m_82537_(new Vec3(0.0, 1.0, 0.0)).m_82541_();
        if (side.m_82556_() < 0.01) {
            side = new Vec3(1.0, 0.0, 0.0);
        }
        Vec3 up = forward.m_82537_(side).m_82541_();
        QizaiGeometry.Sink sink = QizaiMagicVisual.sink(pose, vertices);
        for (Vec3 offset : new Vec3[]{side.m_82490_(width), up.m_82490_(width)}) {
            sink.quad(QizaiArsenalVisual.vertex(origin.m_82549_(offset)), QizaiArsenalVisual.vertex(end.m_82549_(offset)), QizaiArsenalVisual.vertex(end.m_82546_(offset)), QizaiArsenalVisual.vertex(origin.m_82546_(offset)), color, alpha);
        }
    }
}

