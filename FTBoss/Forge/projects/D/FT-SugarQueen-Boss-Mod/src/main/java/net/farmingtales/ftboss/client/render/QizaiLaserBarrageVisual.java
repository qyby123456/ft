package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.util.QizaiGeometry;
import net.farmingtales.ftboss.util.QizaiLaserBarrageRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

/** Renders the barrage in an opaque projector pass and a transparent dedicated laser pass. */
public final class QizaiLaserBarrageVisual {
    private QizaiLaserBarrageVisual() {}

    public static void render(CompoundTag cue, double age, boolean reduced, PoseStack pose, VertexConsumer vertices) {
        render(cue, age, reduced, pose, vertices, false);
    }

    public static void render(CompoundTag cue, double age, boolean reduced, PoseStack pose, VertexConsumer vertices, boolean modelsOnly) {
        double radiusScale = net.farmingtales.ftboss.util.ManorSpellAreaRules.cueScale(cue.getDouble("radiusScale"));
        int fadeTicks = Math.max(1, cue.getInt("fadeTicks"));
        var list = cue.getList("beams", Tag.TAG_COMPOUND);
        int count = Math.min(QizaiLaserBarrageRules.MAX_BEAMS_PER_ROUND, list.size());
        for (int index = 0; index < count; index++) {
            CompoundTag entry = list.getCompound(index);
            Vec3 origin = point(entry, ""), end = point(entry, "end");
            if (!valid(origin) || !valid(end) || origin.distanceToSqr(end) > 48 * 48 * radiusScale * radiusScale) continue;
            double spawnedAge = age - entry.getInt("spawn");
            double firedAge = age - entry.getInt("fire");
            if (spawnedAge < 0 || firedAge >= fadeTicks) continue;
            boolean targeted = entry.getBoolean("targeted");
            float pulse = 0.58F + 0.24F * (float) Math.sin(age * 0.85 + index * 0.7);
            var sink = QizaiMagicVisual.sink(pose, vertices);
            if (modelsOnly) {
                float modelFade = firedAge < 0 ? 1.0F : (float) Math.max(0, 1 - firedAge / fadeTicks);
                QizaiLaserBarrageModel.emitter(vertex(origin), vertex(end), targeted, firedAge >= 0,
                        age, index, reduced, modelFade, sink);
                continue;
            }
            if (firedAge < 0) {
                float warningAlpha = (targeted ? 0.82F : 0.58F) * pulse;
                QizaiLaserBarrageModel.warning(vertex(origin), vertex(end), targeted, age, index,
                        reduced, warningAlpha, sink);
            } else {
                float fade = (float) Math.max(0, 1 - firedAge / fadeTicks);
                QizaiLaserBarrageModel.firing(vertex(origin), vertex(end), targeted, age, index,
                        reduced, fade, sink);
            }
        }
        Vec3 center = point(cue, "center");
        int activeTicks = Math.max(1, cue.getInt("activeTicks"));
        double roundAge = age - cue.getInt("roundStart");
        if (!modelsOnly && valid(center) && roundAge >= 0 && roundAge < activeTicks && !reduced) {
            pose.pushPose();
            pose.translate(center.x, center.y + 0.04, center.z);
            QizaiGeometry.sigil(QizaiLaserBarrageRules.FIELD_RADIUS * radiusScale, Math.min(1, roundAge / activeTicks), false, true,
                    QizaiMagicVisual.sink(pose, vertices));
            pose.popPose();
        }
    }

    private static Vec3 point(CompoundTag tag, String prefix) {
        return new Vec3(tag.getDouble(prefix + "X"), tag.getDouble(prefix + "Y"), tag.getDouble(prefix + "Z"));
    }
    private static boolean valid(Vec3 point) { return Double.isFinite(point.x + point.y + point.z); }
    private static QizaiGeometry.Point vertex(Vec3 point) { return new QizaiGeometry.Point(point.x, point.y, point.z); }
}
