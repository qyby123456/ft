package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.CombatVisualEntities;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.QizaiGeometry;
import net.farmingtales.ftboss.util.QizaiAdvancedGeometry;
import net.farmingtales.ftboss.util.QizaiRules;
import net.farmingtales.ftboss.util.QizaiSkyNovaRules;
import net.farmingtales.ftboss.util.QizaiSweepRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, value = Dist.CLIENT)
public final class QizaiMagicVisual {
    private QizaiMagicVisual() {}

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        Minecraft client = Minecraft.getInstance();
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || client.level == null) return;
        var bosses = CombatVisualEntities.get(client.level).hiddenBosses();
        if (bosses.stream().noneMatch(boss -> boss.getVariant() == EasterBossEntity.QIZAI_VARIANT)) return;
        renderCrystalModels(event, client);
        var buffers = client.renderBuffers().bufferSource();
        VertexConsumer vertices = buffers.getBuffer(QizaiContrast.TYPE);
        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        boolean reduced = client.options.particles().get() != ParticleStatus.ALL;
        pose.pushPose();
        try {
            pose.translate(-camera.x, -camera.y, -camera.z);
            int rendered = 0;
            for (EasterBossEntity boss : bosses) {
                if (boss.getVariant() != EasterBossEntity.QIZAI_VARIANT || !boss.isAlive() || boss.isRemoved()
                        || boss.isInvisible() || boss.distanceToSqr(camera) > 96 * 96 || rendered++ >= 12) continue;
                QizaiArsenalVisual.render(boss, client.level.getGameTime() + event.getPartialTick(), reduced, pose, vertices);
                var cue = boss.getSkillCue();
                int kind = cue.getInt("qizai");
                double age = client.level.getGameTime() - cue.getLong("start") + event.getPartialTick();
                int duration = cue.getInt("duration");
                if (kind == 0 || age < 0 || duration <= 0 || age >= duration) continue;
                double progress = age / duration;
                boolean released = cue.getBoolean("released");
                if (kind == QizaiRules.SWEEP || kind == QizaiRules.SKY_NOVA) {
                    QizaiArsenalVisual.shield(boss, client.level.getGameTime() + event.getPartialTick(), reduced, pose, vertices);
                }
                if (kind == QizaiRules.SWEEP) {
                    double angle = cue.getDouble("initialAngle");
                    double elapsed = cue.getInt("elapsed");
                    var distances = cue.getList("lengths", Tag.TAG_DOUBLE);
                    double[] lengths = new double[distances.size()];
                    for (int arm = 0; arm < lengths.length; arm++) lengths[arm] = distances.getDouble(arm);
                    double windupAge = client.level.getGameTime() - cue.getLong("windupStart") + event.getPartialTick();
                    double windup = QizaiAdvancedGeometry.progress(windupAge, 0, cue.getInt("windupDuration"));
                    Vec3 position = new Vec3(cue.getDouble("anchorX"), cue.getDouble("anchorY"), cue.getDouble("anchorZ"));
                    pose.pushPose();
                    pose.translate(position.x, position.y, position.z);
                    QizaiAdvancedGeometry.sweep(angle, elapsed, cue.getInt("round"), lengths, windup, released, QizaiSweepRules.RADIUS,
                            QizaiSweepRules.BEAM_HALF_WIDTH, QizaiSweepRules.LOW_HEIGHT,
                            QizaiSweepRules.HIGH_HEIGHT, reduced, sink(pose, vertices));
                    pose.popPose();
                    continue;
                }
                if (kind == QizaiRules.PRISM_LOCK) {
                    QizaiPrismVisual.render(cue, age, reduced, pose, vertices);
                    continue;
                }
                if (kind == QizaiRules.LASER_BARRAGE) {
                    QizaiLaserBarrageVisual.render(cue, age, reduced, pose, vertices);
                    continue;
                }
                var marks = cue.getList("marks", Tag.TAG_COMPOUND);
                /* ★ v189：第八招「天陨星裁」是**单点巨型演出**（升空 + 48 格预警圈 + 天降光柱），
                   不吃下面那套逐 mark 的通用画法，所以在这里单独接一支。
                   注意仍然复用**同一个** VertexConsumer（本方法的 vertices），
                   绝不另取 getBuffer —— 顶点缓冲铁律（每种 RenderType 每帧只能取一次）。 */
                if (kind == QizaiRules.SKY_NOVA) {
                    double groundX = cue.getDouble("x");
                    double groundY = cue.getDouble("y");
                    double groundZ = cue.getDouble("z");
                    double apexY = cue.getDouble("apex");
                    if (Double.isFinite(groundX + groundY + groundZ + apexY)
                            && boss.distanceToSqr(groundX, groundY, groundZ) <= 128 * 128) {
                        pose.pushPose();
                        pose.translate(groundX, groundY, groundZ);
                        QizaiAdvancedGeometry.nova(age, released, QizaiSkyNovaRules.RADIUS, apexY - groundY,
                                QizaiSkyNovaRules.RISE_TICKS, QizaiSkyNovaRules.CHARGE_TICKS,
                                QizaiSkyNovaRules.RECOVERY_TICKS, reduced, sink(pose, vertices));
                        pose.popPose();
                    }
                    continue;
                }
                for (int mark = 0; mark < Math.min(5, marks.size()); mark++) {
                    if (QizaiRules.skyNova(kind)) break;   // 天陨星裁已单独画完，跳过通用画法
                    if (QizaiRules.constellation(kind) && released
                            && (kind == QizaiRules.STAR_CROSS || age >= mark * QizaiRules.STARFALL_INTERVAL)) continue;
                    var point = marks.getCompound(mark);
                    double posX = point.getDouble("x"), posY = point.getDouble("y"), posZ = point.getDouble("z");
                    if (!Double.isFinite(posX + posY + posZ) || boss.distanceToSqr(posX, posY, posZ) > 64 * 64) continue;
                    pose.pushPose();
                    pose.translate(posX, posY, posZ);
                    if (kind == QizaiRules.STAR_CROSS) {
                        QizaiGeometry.cross(progress, false, sink(pose, vertices));
                        pose.popPose();
                        continue;
                    }
                    boolean falling = kind == QizaiRules.ROSE_STARFALL;
                    double fallProgress = falling && released ? Math.max(0, 1 - (mark * QizaiRules.STARFALL_INTERVAL - age) / 8) : progress;
                    QizaiGeometry.sigil(kind == QizaiRules.STEP ? 0.8 : QizaiRules.radius(kind), fallProgress, released && !falling, reduced, sink(pose, vertices));
                    if (kind != QizaiRules.STEP) {
                        pose.translate(0, falling ? 0.4 + (1 - fallProgress) * 7 : released ? 0.3 + progress * 1.7 : 0.4 + (1 - progress) * 4, 0);
                        QizaiGeometry.crystal(kind == QizaiRules.PRAYER ? 0.6 : 0.4, kind == QizaiRules.PRAYER ? 1.3 : 0.8,
                                age * 0.055, released ? (float) (1 - progress) : 0.35F + (float) progress * 0.5F, sink(pose, vertices));
                    }
                    pose.popPose();
                }
                Vec3 position = boss.getPosition(event.getPartialTick());
                pose.pushPose();
                pose.translate(position.x, position.y + 1.2, position.z);
                pose.mulPose(Axis.YP.rotationDegrees(-boss.getYRot()));
                pose.translate(0, 0, 0.9);
                pose.mulPose(Axis.XP.rotationDegrees(90));
                double radius = kind == QizaiRules.PRAYER ? 1.2 : 0.48;
                QizaiGeometry.sigil(radius, progress, released, reduced, sink(pose, vertices));
                pose.popPose();
            }
        } finally {
            pose.popPose();
            buffers.endBatch(QizaiContrast.TYPE);
        }
    }

    private static void renderCrystalModels(RenderLevelStageEvent event, Minecraft client) {
        var buffers = client.renderBuffers().bufferSource();
        VertexConsumer bodies = buffers.getBuffer(QizaiContrast.MODEL);
        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        double time = client.level.getGameTime() + event.getPartialTick();
        pose.pushPose();
        try {
            pose.translate(-camera.x, -camera.y, -camera.z);
            int rendered = 0;
            for (EasterBossEntity boss : CombatVisualEntities.get(client.level).hiddenBosses()) {
                if (boss.getVariant() != EasterBossEntity.QIZAI_VARIANT || !boss.isAlive() || boss.isRemoved()
                        || boss.isInvisible() || boss.distanceToSqr(camera) > 96 * 96 || rendered++ >= 12) continue;
                QizaiArsenalVisual.render(boss, time, true, pose, bodies, true);
                var cue = boss.getSkillCue();
                double age = time - cue.getLong("start");
                if (age < 0 || age >= cue.getInt("duration")) continue;
                if (cue.getInt("qizai") == QizaiRules.PRISM_LOCK) {
                    QizaiPrismVisual.render(cue, age, true, pose, bodies, true);
                } else if (cue.getInt("qizai") == QizaiRules.LASER_BARRAGE) {
                    QizaiLaserBarrageVisual.render(cue, age, true, pose, bodies, true);
                }
            }
        } finally {
            pose.popPose();
            buffers.endBatch(QizaiContrast.MODEL);
        }
    }

    public static QizaiGeometry.Sink sink(PoseStack pose, VertexConsumer vertices) {
        return (first, second, third, fourth, color, alpha) -> {
            vertex(pose, vertices, first, color, alpha);
            vertex(pose, vertices, second, color, alpha);
            vertex(pose, vertices, third, color, alpha);
            vertex(pose, vertices, fourth, color, alpha);
        };
    }

    private static void vertex(PoseStack pose, VertexConsumer vertices, QizaiGeometry.Point point, int color, float alpha) {
        vertices.vertex(pose.last().pose(), (float) point.x(), (float) point.y(), (float) point.z())
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255, Math.max(0, Math.min(255, (int) (alpha * 255)))).endVertex();
    }
}
