/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.renderer.RenderStateShard$EmptyTextureStateShard
 *  net.minecraft.client.renderer.RenderStateShard$TextureStateShard
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  net.minecraft.resources.ResourceLocation
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class GongdiSpellGlowType
extends RenderType {
    private GongdiSpellGlowType() {
        super("ftboss_gongdi_glow", DefaultVertexFormat.f_85812_, VertexFormat.Mode.QUADS, 16384, false, false, () -> {}, () -> {});
    }

    public static RenderType layer(ResourceLocation resourceLocation) {
        return GongdiSpellGlowType.m_173215_((String)"ftboss_gongdi_glow", (VertexFormat)DefaultVertexFormat.f_85812_, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)16384, (boolean)false, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.m_110628_().m_173292_(f_173073_).m_173290_((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(resourceLocation, false, false)).m_110685_(f_110135_).m_110661_(f_110110_).m_110663_(f_110111_).m_110687_(f_110115_).m_110691_(false));
    }
}

