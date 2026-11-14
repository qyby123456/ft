/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.EntityRenderersEvent$RegisterRenderers
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package net.farmingtales.ftboss.youzi.spells.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.youzi.spells.YouziSpellRegistry;
import net.farmingtales.ftboss.youzi.spells.YouziStarProjectile;
import net.farmingtales.ftboss.youzi.spells.client.YouziSpellVisual;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public final class YouziStarRenderer
extends EntityRenderer<YouziStarProjectile> {
    public YouziStarRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public ResourceLocation m_5478_(YouziStarProjectile youziStarProjectile) {
        return YouziSpellVisual.ATLAS;
    }

    public void m_7392_(YouziStarProjectile youziStarProjectile, float f, float f2, PoseStack poseStack, MultiBufferSource multiBufferSource, int n) {
        YouziSpellVisual.queue(youziStarProjectile, f2);
    }

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers registerRenderers) {
        registerRenderers.registerEntityRenderer(YouziSpellRegistry.star(), YouziStarRenderer::new);
    }
}

