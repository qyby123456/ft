/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.renderer.RenderStateShard$EmptyTextureStateShard
 *  net.minecraft.client.renderer.RenderStateShard$ShaderStateShard
 *  net.minecraft.client.renderer.RenderStateShard$TextureStateShard
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.RenderType$CompositeState
 *  net.minecraft.client.renderer.ShaderInstance
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RegisterShadersEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.client.maque;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT}, bus=Mod.EventBusSubscriber.Bus.MOD)
public final class MaqueArcaneLayers
extends RenderType {
    private static ShaderInstance shader;
    private static ShaderInstance refractionShader;
    private static final ResourceLocation ATLAS;
    public static final RenderType RADIANCE;
    public static final RenderType REFRACTION;

    private MaqueArcaneLayers() {
        super("maque_arcane_layer_access", DefaultVertexFormat.f_85812_, VertexFormat.Mode.QUADS, 256, false, false, () -> {}, () -> {});
    }

    private static RenderType layer(String string, boolean bl) {
        return MaqueArcaneLayers.m_173215_((String)string, (VertexFormat)DefaultVertexFormat.f_85812_, (VertexFormat.Mode)VertexFormat.Mode.QUADS, (int)32768, (boolean)false, (!bl ? 1 : 0) != 0, (RenderType.CompositeState)RenderType.CompositeState.m_110628_().m_173292_(new RenderStateShard.ShaderStateShard(() -> bl ? shader : refractionShader)).m_173290_((RenderStateShard.EmptyTextureStateShard)new RenderStateShard.TextureStateShard(ATLAS, false, false)).m_110685_(bl ? f_110135_ : f_110139_).m_110663_(f_110113_).m_110661_(f_110158_).m_110687_(f_110115_).m_110671_(f_110153_).m_110677_(f_110155_).m_110691_(false));
    }

    public static boolean ready() {
        return shader != null && refractionShader != null;
    }

    @SubscribeEvent
    public static void shaders(RegisterShadersEvent registerShadersEvent) throws IOException {
        registerShadersEvent.registerShader(new ShaderInstance(registerShadersEvent.getResourceProvider(), new ResourceLocation("ftboss", "maque_arcane/light"), DefaultVertexFormat.f_85812_), shaderInstance -> {
            shader = shaderInstance;
        });
        registerShadersEvent.registerShader(new ShaderInstance(registerShadersEvent.getResourceProvider(), new ResourceLocation("ftboss", "maque_arcane/refraction"), DefaultVertexFormat.f_85812_), shaderInstance -> {
            refractionShader = shaderInstance;
        });
    }

    static {
        ATLAS = new ResourceLocation("ftboss", "maque_arcane/arcane_atlas.png");
        RADIANCE = MaqueArcaneLayers.layer("maque_arcane_radiance", true);
        REFRACTION = MaqueArcaneLayers.layer("maque_arcane_refraction", false);
    }
}

