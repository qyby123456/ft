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

public final class ManorDomainRenderType
extends RenderType {
    public static final RenderType SOLID = ManorDomainRenderType.m_173215_((String)"ftboss_manor_domain_stone_v2", (VertexFormat)DefaultVertexFormat.f_85815_, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)262144, (boolean)false, (boolean)true, (RenderType.CompositeState)RenderType.CompositeState.m_110628_().m_173292_(f_173104_).m_110685_(f_110139_).m_110663_(f_110113_).m_110687_(f_110114_).m_110661_(f_110110_).m_110691_(false));
    public static final RenderType GLOW = ManorDomainRenderType.m_173215_((String)"ftboss_manor_domain_light_v2", (VertexFormat)DefaultVertexFormat.f_85815_, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)262144, (boolean)false, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.m_110628_().m_173292_(f_173104_).m_110685_(f_110136_).m_110663_(f_110113_).m_110687_(f_110115_).m_110661_(f_110110_).m_110691_(false));

    private ManorDomainRenderType() {
        super("ftboss_manor_domain_v2", DefaultVertexFormat.f_85815_, VertexFormat.Mode.QUADS, 262144, false, false, () -> {}, () -> {});
    }
}

