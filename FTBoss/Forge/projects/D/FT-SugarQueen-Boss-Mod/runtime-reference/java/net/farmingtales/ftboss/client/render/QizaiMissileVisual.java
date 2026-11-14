/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  com.mojang.math.Axis
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.farmingtales.ftboss.client.render.QizaiContrast;
import net.farmingtales.ftboss.client.render.QizaiMagicVisual;
import net.farmingtales.ftboss.util.QizaiMissileModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public final class QizaiMissileVisual {
    private QizaiMissileVisual() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(Entity missile, float partial, boolean arsenal, PoseStack pose, MultiBufferSource buffers) {
        pose.m_85836_();
        try {
            pose.m_252781_(Axis.f_252436_.m_252977_(Mth.m_14189_((float)partial, (float)missile.f_19859_, (float)missile.m_146908_())));
            pose.m_252781_(Axis.f_252529_.m_252977_(-Mth.m_14179_((float)partial, (float)missile.f_19860_, (float)missile.m_146909_())));
            double age = (float)missile.f_19797_ + partial;
            pose.m_252781_(Axis.f_252403_.m_252977_((float)(age * 1.7 + (double)(missile.m_19879_() * 37 % 360))));
            VertexConsumer body = buffers.m_6299_(Hull.TYPE);
            QizaiMissileModel.body(arsenal, QizaiMagicVisual.sink(pose, body));
            VertexConsumer glow = buffers.m_6299_(QizaiContrast.TYPE);
            QizaiMissileModel.glow(age, arsenal, QizaiMagicVisual.sink(pose, glow));
        }
        finally {
            pose.m_85849_();
        }
    }

    private static final class Hull
    extends RenderType {
        private static final RenderType TYPE = Hull.m_173215_((String)"ftboss_qizai_missile_hull", (VertexFormat)DefaultVertexFormat.f_85815_, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)8192, (boolean)false, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.m_110628_().m_173292_(f_173104_).m_110685_(f_110134_).m_110663_(f_110113_).m_110687_(f_110114_).m_110661_(f_110110_).m_110691_(false));

        private Hull() {
            super("ftboss_qizai_missile_hull", DefaultVertexFormat.f_85815_, VertexFormat.Mode.QUADS, 8192, false, false, () -> {}, () -> {});
        }
    }
}

