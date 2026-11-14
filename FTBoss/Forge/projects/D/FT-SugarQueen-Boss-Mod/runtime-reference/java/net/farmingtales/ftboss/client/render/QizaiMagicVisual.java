/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.List;
import net.farmingtales.ftboss.client.CombatVisualEntities;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneCompletedVisuals;
import net.farmingtales.ftboss.client.render.QizaiArsenalVisual;
import net.farmingtales.ftboss.client.render.QizaiContrast;
import net.farmingtales.ftboss.client.render.QizaiLaserBarrageVisual;
import net.farmingtales.ftboss.client.render.QizaiPrismVisual;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.QizaiAdvancedGeometry;
import net.farmingtales.ftboss.util.QizaiGeometry;
import net.farmingtales.ftboss.util.QizaiRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class QizaiMagicVisual {
    private QizaiMagicVisual() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        Minecraft client = Minecraft.m_91087_();
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || client.f_91073_ == null) {
            return;
        }
        List<EasterBossEntity> bosses = CombatVisualEntities.get(client.f_91073_).hiddenBosses();
        if (bosses.stream().noneMatch(boss -> boss.getVariant() == 11)) {
            return;
        }
        QizaiMagicVisual.renderCrystalModels(event, client);
        MultiBufferSource.BufferSource buffers = client.m_91269_().m_110104_();
        VertexConsumer vertices = buffers.m_6299_(QizaiContrast.TYPE);
        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().m_90583_();
        boolean reduced = client.f_91066_.m_231929_().m_231551_() != ParticleStatus.ALL;
        pose.m_85836_();
        try {
            pose.m_85837_(-camera.f_82479_, -camera.f_82480_, -camera.f_82481_);
            int rendered = 0;
            for (EasterBossEntity boss2 : bosses) {
                if (boss2.getVariant() != 11 || !boss2.m_6084_() || boss2.m_213877_() || boss2.m_20145_() || boss2.m_20238_(camera) > 9216.0 || rendered++ >= 12) continue;
                QizaiArsenalVisual.render(boss2, (float)client.f_91073_.m_46467_() + event.getPartialTick(), reduced, pose, vertices);
                CompoundTag cue = ArcaneCompletedVisuals.legacyCue(boss2);
                int kind = cue.m_128451_("qizai");
                double age = (float)(client.f_91073_.m_46467_() - cue.m_128454_("start")) + event.getPartialTick();
                int duration = cue.m_128451_("duration");
                if (kind == 0 || age < 0.0 || duration <= 0 || age >= (double)duration) continue;
                double progress = age / (double)duration;
                boolean released = cue.m_128471_("released");
                if (kind == 7 || kind == 8) {
                    QizaiArsenalVisual.shield(boss2, (float)client.f_91073_.m_46467_() + event.getPartialTick(), reduced, pose, vertices);
                }
                if (kind == 7) {
                    double angle = cue.m_128459_("initialAngle");
                    double elapsed = cue.m_128451_("elapsed");
                    ListTag distances = cue.m_128437_("lengths", 6);
                    double[] lengths = new double[distances.size()];
                    for (int arm = 0; arm < lengths.length; ++arm) {
                        lengths[arm] = distances.m_128772_(arm);
                    }
                    double windupAge = (float)(client.f_91073_.m_46467_() - cue.m_128454_("windupStart")) + event.getPartialTick();
                    double windup = QizaiAdvancedGeometry.progress(windupAge, 0.0, cue.m_128451_("windupDuration"));
                    Vec3 position = new Vec3(cue.m_128459_("anchorX"), cue.m_128459_("anchorY"), cue.m_128459_("anchorZ"));
                    pose.m_85836_();
                    pose.m_85837_(position.f_82479_, position.f_82480_, position.f_82481_);
                    QizaiAdvancedGeometry.sweep(angle, elapsed, cue.m_128451_("round"), lengths, windup, released, 32.0, 1.3, 0.9, 2.1, reduced, QizaiMagicVisual.sink(pose, vertices));
                    pose.m_85849_();
                    continue;
                }
                if (kind == 9) {
                    QizaiPrismVisual.render(cue, age, reduced, pose, vertices);
                    continue;
                }
                if (kind == 10) {
                    QizaiLaserBarrageVisual.render(cue, age, reduced, pose, vertices);
                    continue;
                }
                ListTag marks = cue.m_128437_("marks", 10);
                if (kind == 8) {
                    double apexY;
                    double groundZ;
                    double groundY;
                    double groundX = cue.m_128459_("x");
                    if (!Double.isFinite(groundX + (groundY = cue.m_128459_("y")) + (groundZ = cue.m_128459_("z")) + (apexY = cue.m_128459_("apex"))) || !(boss2.m_20275_(groundX, groundY, groundZ) <= 16384.0)) continue;
                    pose.m_85836_();
                    pose.m_85837_(groundX, groundY, groundZ);
                    QizaiAdvancedGeometry.nova(age, released, 48.0, apexY - groundY, 20, 100, 20, reduced, QizaiMagicVisual.sink(pose, vertices));
                    pose.m_85849_();
                    continue;
                }
                for (int mark = 0; mark < Math.min(5, marks.size()) && !QizaiRules.skyNova(kind); ++mark) {
                    double posZ;
                    double posY;
                    CompoundTag point;
                    double posX;
                    if (QizaiRules.constellation(kind) && released && (kind == 5 || age >= (double)(mark * 8)) || !Double.isFinite((posX = (point = marks.m_128728_(mark)).m_128459_("x")) + (posY = point.m_128459_("y")) + (posZ = point.m_128459_("z"))) || boss2.m_20275_(posX, posY, posZ) > 4096.0) continue;
                    pose.m_85836_();
                    pose.m_85837_(posX, posY, posZ);
                    if (kind == 5) {
                        QizaiGeometry.cross(progress, false, QizaiMagicVisual.sink(pose, vertices));
                        pose.m_85849_();
                        continue;
                    }
                    boolean falling = kind == 6;
                    double fallProgress = falling && released ? Math.max(0.0, 1.0 - ((double)(mark * 8) - age) / 8.0) : progress;
                    QizaiGeometry.sigil(kind == 4 ? 0.8 : QizaiRules.radius(kind), fallProgress, released && !falling, reduced, QizaiMagicVisual.sink(pose, vertices));
                    if (kind != 4) {
                        pose.m_85837_(0.0, falling ? 0.4 + (1.0 - fallProgress) * 7.0 : (released ? 0.3 + progress * 1.7 : 0.4 + (1.0 - progress) * 4.0), 0.0);
                        QizaiGeometry.crystal(kind == 3 ? 0.6 : 0.4, kind == 3 ? 1.3 : 0.8, age * 0.055, released ? (float)(1.0 - progress) : 0.35f + (float)progress * 0.5f, QizaiMagicVisual.sink(pose, vertices));
                    }
                    pose.m_85849_();
                }
                Vec3 position = boss2.m_20318_(event.getPartialTick());
                pose.m_85836_();
                pose.m_85837_(position.f_82479_, position.f_82480_ + 1.2, position.f_82481_);
                pose.m_252781_(Axis.f_252436_.m_252977_(-boss2.m_146908_()));
                pose.m_85837_(0.0, 0.0, 0.9);
                pose.m_252781_(Axis.f_252529_.m_252977_(90.0f));
                double radius = kind == 3 ? 1.2 : 0.48;
                QizaiGeometry.sigil(radius, progress, released, reduced, QizaiMagicVisual.sink(pose, vertices));
                pose.m_85849_();
            }
        }
        finally {
            pose.m_85849_();
            buffers.m_109912_(QizaiContrast.TYPE);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void renderCrystalModels(RenderLevelStageEvent event, Minecraft client) {
        MultiBufferSource.BufferSource buffers = client.m_91269_().m_110104_();
        VertexConsumer bodies = buffers.m_6299_(QizaiContrast.MODEL);
        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().m_90583_();
        double time = (float)client.f_91073_.m_46467_() + event.getPartialTick();
        pose.m_85836_();
        try {
            pose.m_85837_(-camera.f_82479_, -camera.f_82480_, -camera.f_82481_);
            int rendered = 0;
            for (EasterBossEntity boss : CombatVisualEntities.get(client.f_91073_).hiddenBosses()) {
                if (boss.getVariant() != 11 || !boss.m_6084_() || boss.m_213877_() || boss.m_20145_() || boss.m_20238_(camera) > 9216.0 || rendered++ >= 12) continue;
                QizaiArsenalVisual.render(boss, time, true, pose, bodies, true);
                CompoundTag cue = ArcaneCompletedVisuals.legacyCue(boss);
                double age = time - (double)cue.m_128454_("start");
                if (age < 0.0 || age >= (double)cue.m_128451_("duration")) continue;
                if (cue.m_128451_("qizai") == 9) {
                    QizaiPrismVisual.render(cue, age, true, pose, bodies, true);
                    continue;
                }
                if (cue.m_128451_("qizai") != 10) continue;
                QizaiLaserBarrageVisual.render(cue, age, true, pose, bodies, true);
            }
        }
        finally {
            pose.m_85849_();
            buffers.m_109912_(QizaiContrast.MODEL);
        }
    }

    public static QizaiGeometry.Sink sink(PoseStack pose, VertexConsumer vertices) {
        return (first, second, third, fourth, color, alpha) -> {
            QizaiMagicVisual.vertex(pose, vertices, first, color, alpha);
            QizaiMagicVisual.vertex(pose, vertices, second, color, alpha);
            QizaiMagicVisual.vertex(pose, vertices, third, color, alpha);
            QizaiMagicVisual.vertex(pose, vertices, fourth, color, alpha);
        };
    }

    private static void vertex(PoseStack pose, VertexConsumer vertices, QizaiGeometry.Point point, int color, float alpha) {
        vertices.m_252986_(pose.m_85850_().m_252922_(), (float)point.x(), (float)point.y(), (float)point.z()).m_6122_(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, Math.max(0, Math.min(255, (int)(alpha * 255.0f)))).m_5752_();
    }
}

