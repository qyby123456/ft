/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

public final class DandanScytheGlow
extends RenderType {
    public static final RenderType TYPE = DandanScytheGlow.m_173215_((String)"ftboss_dandan_candle", (VertexFormat)DefaultVertexFormat.f_85815_, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)8192, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.m_110628_().m_173292_(f_173104_).m_110685_(f_110139_).m_110663_(f_110113_).m_110687_(f_110115_).m_110661_(f_110110_).m_110691_(false));

    private DandanScytheGlow() {
        super("ftboss_dandan_candle", DefaultVertexFormat.f_85815_, VertexFormat.Mode.QUADS, 8192, false, true, () -> {}, () -> {});
    }
}

