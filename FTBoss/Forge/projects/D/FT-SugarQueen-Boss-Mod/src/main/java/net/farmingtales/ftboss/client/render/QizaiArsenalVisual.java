package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.QizaiArsenalRules;
import net.farmingtales.ftboss.util.QizaiGeometry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

public final class QizaiArsenalVisual {
    private QizaiArsenalVisual() {}

    public static void render(EasterBossEntity boss, double time, boolean reduced, PoseStack pose, VertexConsumer vertices) {
        render(boss, time, reduced, pose, vertices, false);
    }

    public static void render(EasterBossEntity boss, double time, boolean reduced, PoseStack pose, VertexConsumer vertices, boolean modelsOnly) {
        CompoundTag cue = boss.getQizaiArsenalCue();
        int primary = boss.getSkillCue().getInt("qizai");
        if (!modelsOnly && cue.getBoolean("shield") && primary != net.farmingtales.ftboss.util.QizaiRules.SWEEP
                && primary != net.farmingtales.ftboss.util.QizaiRules.SKY_NOVA) shield(boss, time, reduced, pose, vertices);
        var salvos = cue.getList("salvos", Tag.TAG_COMPOUND);
        for (int index = 0; index < Math.min(QizaiArsenalRules.MAX_SALVOS, salvos.size()); index++) {
            var salvo = salvos.getCompound(index);
            double age = time - salvo.getLong("start");
            if (age < 0 || age >= QizaiArsenalRules.SALVO_DURATION) continue;
            var crystals = salvo.getList("crystals", Tag.TAG_COMPOUND);
            for (int crystalIndex = 0; crystalIndex < Math.min(QizaiArsenalRules.CRYSTALS, crystals.size()); crystalIndex++) {
                var crystal = crystals.getCompound(crystalIndex);
                Vec3 origin = point(crystal, ""), end = point(crystal, "end");
                if (!valid(origin) || !valid(end) || origin.distanceToSqr(end) > 34 * 34
                        || boss.distanceToSqr(origin) > 64 * 64) continue;
                double fired = age - QizaiArsenalRules.CHARGE - crystalIndex * QizaiArsenalRules.LASER_GAP;
                float alpha = fired < 0 ? 0.65F : (float) Math.max(0, 1 - fired / QizaiArsenalRules.LASER_FADE);
                if (modelsOnly) {
                    QizaiCrystalModelVisual.render(origin, age + crystalIndex * 21, 1, fired < 0 ? 1 : alpha, pose, vertices);
                    continue;
                }
                if (alpha <= 0) continue;
                if (fired < 0) beam(origin, end, 0.085, 0.85F, QizaiGeometry.WARNING, pose, vertices);
                else {
                    beam(origin, end, QizaiArsenalRules.LASER_WIDTH, alpha * 0.6F, QizaiGeometry.ROSE, pose, vertices);
                    beam(origin, end, 0.12, alpha, QizaiGeometry.WHITE, pose, vertices);
                }
            }
        }
        if (!modelsOnly && cue.contains("push", Tag.TAG_COMPOUND)) {
            var push = cue.getCompound("push");
            Vec3 origin = point(push, "");
            boolean released = push.getBoolean("released");
            double age = time - push.getLong("start");
            int duration = released ? QizaiArsenalRules.PUSH_FADE : QizaiArsenalRules.PUSH_WINDUP;
            if (valid(origin) && age >= 0 && age < duration) {
                pose.pushPose();
                pose.translate(origin.x, origin.y + 0.08, origin.z);
                QizaiGeometry.sigil(QizaiArsenalRules.PUSH_RADIUS, age / duration, released, reduced,
                        QizaiMagicVisual.sink(pose, vertices));
                pose.popPose();
            }
        }
    }

    public static void shield(EasterBossEntity boss, double time, boolean reduced, PoseStack pose, VertexConsumer vertices) {
        pose.pushPose();
        pose.translate(boss.getX(), boss.getY() + 1.1, boss.getZ());
        int count = reduced ? 4 : 8;
        for (int index = 0; index < count; index++) {
            double angle = time * 0.06 + index * Math.PI * 2 / count;
            pose.pushPose();
            pose.translate(Math.cos(angle) * 1.25, Math.sin(angle * 2) * 0.3, Math.sin(angle) * 1.25);
            QizaiGeometry.crystal(0.12, 0.4, -angle, 0.7F, QizaiMagicVisual.sink(pose, vertices));
            pose.popPose();
        }
        pose.popPose();
    }

    private static Vec3 point(CompoundTag cue, String prefix) {
        return new Vec3(cue.getDouble(prefix + "x"), cue.getDouble(prefix + "y"), cue.getDouble(prefix + "z"));
    }

    private static boolean valid(Vec3 point) { return Double.isFinite(point.x + point.y + point.z); }

    private static QizaiGeometry.Point vertex(Vec3 point) { return new QizaiGeometry.Point(point.x, point.y, point.z); }

    private static void beam(Vec3 origin, Vec3 end, double width, float alpha, int color, PoseStack pose, VertexConsumer vertices) {
        Vec3 forward = end.subtract(origin).normalize();
        Vec3 side = forward.cross(new Vec3(0, 1, 0)).normalize();
        if (side.lengthSqr() < 0.01) side = new Vec3(1, 0, 0);
        Vec3 up = forward.cross(side).normalize();
        var sink = QizaiMagicVisual.sink(pose, vertices);
        for (Vec3 offset : new Vec3[]{side.scale(width), up.scale(width)}) {
            sink.quad(vertex(origin.add(offset)), vertex(end.add(offset)), vertex(end.subtract(offset)),
                    vertex(origin.subtract(offset)), color, alpha);
        }
    }
}
