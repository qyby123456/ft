/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.culling.Frustum
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.farmingtales.ftboss.client.qizaiarcane;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneAssets;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneFlightTrails;
import net.farmingtales.ftboss.client.qizaiarcane.ArcanePainter;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneRig;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneSigilGlow;
import net.farmingtales.ftboss.client.render.QizaiSigilRenderer;
import net.farmingtales.ftboss.client.render.QizaiStarRenderer;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.QizaiSigilEntity;
import net.farmingtales.ftboss.entity.QizaiStarEntity;
import net.farmingtales.ftboss.util.ManorBossCombatRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class ArcaneEntityVisuals {
    private ArcaneEntityVisuals() {
    }

    public static final class Field
    extends EntityRenderer<QizaiSigilEntity> {
        private final QizaiSigilRenderer fallback;

        public Field(EntityRendererProvider.Context context) {
            super(context);
            this.fallback = new QizaiSigilRenderer(context);
        }

        public ResourceLocation m_5478_(QizaiSigilEntity qizaiSigilEntity) {
            return new ResourceLocation("ftboss", "qizai_arcane/" + ArcaneAssets.IDS.get(Math.max(0, Math.min(5, qizaiSigilEntity.kind() - 1))) + ".png");
        }

        public boolean m_5523_(QizaiSigilEntity qizaiSigilEntity, Frustum frustum, double d, double d2, double d3) {
            EasterBossEntity easterBossEntity;
            ArcaneRig.Effect effect = ArcaneAssets.kind(qizaiSigilEntity.kind());
            Entity entity = qizaiSigilEntity.m_19749_();
            double d4 = entity instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)entity).getVariant() == 11 ? 2 : 1;
            return qizaiSigilEntity.m_20275_(d, d2, d3) < 4096.0 && frustum.m_113029_(qizaiSigilEntity.m_20191_().m_82400_(effect == null ? 6.5 : Math.max(6.5, effect.radius() * d4 + 1.5)));
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void m_7392_(QizaiSigilEntity qizaiSigilEntity, float f, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
            EasterBossEntity easterBossEntity;
            if (qizaiSigilEntity.kind() == 1) {
                ArcaneSigilGlow.originalPattern(qizaiSigilEntity, f2, poseStack, multiBufferSource);
                return;
            }
            ArcaneRig.Effect effect = ArcaneAssets.kind(qizaiSigilEntity.kind());
            if (effect == null) {
                this.fallback.render(qizaiSigilEntity, f, f2, poseStack, multiBufferSource, n);
                return;
            }
            boolean bl = qizaiSigilEntity.released();
            double d = qizaiSigilEntity.progress(f2) * (bl ? 1.1 : 1.4);
            LocalPlayer localPlayer = Minecraft.m_91087_().f_91074_;
            Entity entity = qizaiSigilEntity.m_19749_();
            double d2 = entity instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)entity).getVariant() == 11 ? ManorBossCombatRules.radiusScale(11) : 1.0;
            poseStack.m_85836_();
            try {
                poseStack.m_85841_((float)d2, 1.0f, (float)d2);
                easterBossEntity = new Vec3(d2, 1.0, d2);
                float f3 = bl ? (float)Math.min(1.0, (1.0 - qizaiSigilEntity.progress(f2)) * 4.0) : 1.0f;
                ArcanePainter.scaled(effect, bl ? "release" : "charge", d, poseStack, multiBufferSource, qizaiSigilEntity.m_20318_(f2), new Quaternionf(), false, true, false, 240, n, f3, (Vec3)easterBossEntity);
                ArcanePainter.scaled(effect, bl ? "release" : "charge", d, poseStack, multiBufferSource, qizaiSigilEntity.m_20318_(f2), new Quaternionf(), localPlayer != null && qizaiSigilEntity.m_20280_((Entity)localPlayer) < 196.0, false, true, 3600, n, f3, (Vec3)easterBossEntity);
            }
            finally {
                poseStack.m_85849_();
            }
        }
    }

    public static final class Impulse
    extends EntityRenderer<QizaiStarEntity> {
        private final QizaiStarRenderer unrelatedTracking;

        public Impulse(EntityRendererProvider.Context context) {
            super(context);
            this.unrelatedTracking = new QizaiStarRenderer(context);
        }

        public ResourceLocation m_5478_(QizaiStarEntity qizaiStarEntity) {
            return new ResourceLocation("ftboss", "qizai_arcane/qizai_star_volley.png");
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void m_7392_(QizaiStarEntity qizaiStarEntity, float f, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
            if (qizaiStarEntity.isTrackingMissile()) {
                this.unrelatedTracking.render(qizaiStarEntity, f, f2, poseStack, multiBufferSource, n);
                return;
            }
            ArcaneRig.Effect effect = ArcaneAssets.get("qizai_star_volley");
            if (effect == null) {
                this.unrelatedTracking.render(qizaiStarEntity, f, f2, poseStack, multiBufferSource, n);
                return;
            }
            ArcaneFlightTrails.capture((Entity)qizaiStarEntity, f2);
            Vec3 vec3 = qizaiStarEntity.m_20184_();
            Quaternionf quaternionf = new Quaternionf();
            if (vec3.m_82556_() > 1.0E-8) {
                quaternionf.rotationTo((Vector3fc)new Vector3f(0.0f, 0.0f, 1.0f), (Vector3fc)new Vector3f((float)vec3.f_82479_, (float)vec3.f_82480_, (float)vec3.f_82481_).normalize());
            }
            Minecraft minecraft = Minecraft.m_91087_();
            boolean bl = minecraft.f_91074_ != null && qizaiStarEntity.m_20280_((Entity)minecraft.f_91074_) < 144.0;
            poseStack.m_85836_();
            try {
                poseStack.m_252781_(quaternionf);
                ArcanePainter.effect(effect, "idle", (double)(((float)qizaiStarEntity.f_19797_ + f2) / 20.0f) % 2.4, poseStack, multiBufferSource, qizaiStarEntity.m_20318_(f2), quaternionf, bl, false, true, 900, n, 0.96f);
            }
            finally {
                poseStack.m_85849_();
            }
        }
    }
}

