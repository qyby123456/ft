package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.util.QizaiGeometry;
import net.farmingtales.ftboss.util.QizaiPrismRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

/** Uses QizaiMagicVisual's one per-frame lightning buffer. */
public final class QizaiPrismVisual {
    private QizaiPrismVisual() {}
    public static void render(CompoundTag cue, double age, boolean reduced, PoseStack pose, VertexConsumer vertices) {
        render(cue, age, reduced, pose, vertices, false);
    }

    public static void render(CompoundTag cue, double age, boolean reduced, PoseStack pose, VertexConsumer vertices, boolean modelsOnly) {
        int track = Math.max(1, cue.getInt("trackTicks"));
        int lock = Math.max(1, cue.getInt("lockTicks"));
        int gap = Math.max(1, cue.getInt("fireGap"));
        int fadeTicks = Math.max(1, cue.getInt("fadeTicks"));
        boolean locked = cue.getBoolean("locked");
        double chargeProgress = Math.max(0, Math.min(1, age / track));
        var list = cue.getList("beams", Tag.TAG_COMPOUND);
        int count = Math.min(QizaiPrismRules.BEAMS, list.size());
        for (int index = 0; index < count; index++) {
            CompoundTag entry = list.getCompound(index);
            Vec3 origin = point(entry, ""), end = point(entry, "end");
            if (!valid(origin) || !valid(end) || origin.distanceToSqr(end) > 32 * 32) continue;
            double firedAge = age - QizaiPrismRules.fireTick(index, track, lock, gap);
            if (modelsOnly) {
                QizaiCrystalModelVisual.render(origin, age + index * 21, 1.15,
                        firedAge < 0 ? 1 : 1 - firedAge / fadeTicks, pose, vertices);
                continue;
            }
            if (firedAge < 0) {
                beam(origin, end, locked ? 0.12 : 0.075, locked ? 0.95F : 0.7F + 0.15F * (float) chargeProgress,
                        QizaiGeometry.WARNING, pose, vertices);
                if (locked) beam(origin, end, 0.04, 0.95F, QizaiGeometry.WHITE, pose, vertices);
            } else if (firedAge < fadeTicks) {
                float fade = (float) Math.max(0, 1 - firedAge / fadeTicks);
                beam(origin, end, QizaiPrismRules.BEAM_RADIUS, fade * 0.65F, QizaiGeometry.ROSE, pose, vertices);
                beam(origin, end, 0.14, fade, QizaiGeometry.WHITE, pose, vertices);
            }
        }
        Vec3 target = point(cue, "target");
        if (!modelsOnly && valid(target) && count > 0 && age < QizaiPrismRules.lastFireTick(track, lock, gap) + fadeTicks) {
            pose.pushPose();
            pose.translate(target.x, target.y, target.z);
            QizaiGeometry.sigil(locked ? 0.9 + Math.sin(age * 0.55) * 0.15 : 0.45 + chargeProgress * 0.35,
                    locked ? 1 : chargeProgress, age >= track + lock, reduced, QizaiMagicVisual.sink(pose, vertices));
            pose.popPose();
        }
    }
    private static Vec3 point(CompoundTag tag, String prefix) {
        return new Vec3(tag.getDouble(prefix + "X"), tag.getDouble(prefix + "Y"), tag.getDouble(prefix + "Z"));
    }
    private static boolean valid(Vec3 point) { return Double.isFinite(point.x + point.y + point.z); }
    private static QizaiGeometry.Point vertex(Vec3 point) { return new QizaiGeometry.Point(point.x, point.y, point.z); }
    private static void beam(Vec3 origin, Vec3 end, double width, float alpha, int color, PoseStack pose, VertexConsumer vertices) {
        Vec3 forward = end.subtract(origin);
        if (forward.lengthSqr() < 1.0E-8 || width <= 0 || alpha <= 0) return;
        forward = forward.normalize();
        Vec3 side = forward.cross(new Vec3(0, 1, 0)).normalize();
        if (side.lengthSqr() < 0.01) side = new Vec3(1, 0, 0);
        Vec3 up = forward.cross(side).normalize();
        var sink = QizaiMagicVisual.sink(pose, vertices);
        for (Vec3 offset : new Vec3[]{side.scale(width), up.scale(width)}) {
            sink.quad(vertex(origin.add(offset)), vertex(end.add(offset)), vertex(end.subtract(offset)), vertex(origin.subtract(offset)), color, alpha);
        }
    }
}
