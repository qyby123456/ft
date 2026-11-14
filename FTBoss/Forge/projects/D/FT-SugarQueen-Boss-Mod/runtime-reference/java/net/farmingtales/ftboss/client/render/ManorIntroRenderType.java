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

public final class ManorIntroRenderType
extends RenderType {
    public static final RenderType TYPE = ManorIntroRenderType.m_173215_((String)"ftboss_manor_intro", (VertexFormat)DefaultVertexFormat.f_85812_, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)262144, (boolean)false, (boolean)false, (RenderType.CompositeState)RenderType.CompositeState.m_110628_().m_173292_(f_173073_).m_173290_((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(new ResourceLocation("ftboss", "textures/fx/manor_intro_white.png"), false, false)).m_110685_(f_110135_).m_110661_(f_110110_).m_110687_(f_110115_).m_110691_(false));

    private ManorIntroRenderType() {
        super("ftboss_manor_intro", DefaultVertexFormat.f_85812_, VertexFormat.Mode.QUADS, 262144, false, false, () -> {}, () -> {});
    }
}

