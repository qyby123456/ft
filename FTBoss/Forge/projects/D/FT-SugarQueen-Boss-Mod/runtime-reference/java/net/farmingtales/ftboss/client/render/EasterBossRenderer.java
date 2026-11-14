/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.model.EntityModel
 *  net.minecraft.client.renderer.culling.Frustum
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.MobRenderer
 *  net.minecraft.client.renderer.entity.RenderLayerParent
 *  net.minecraft.client.renderer.entity.layers.RenderLayer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Mob
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.client.render.QizaiHeldItemLayer;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class EasterBossRenderer
extends MobRenderer<EasterBossEntity, EasterBossModel> {
    private static final Map<Integer, ResourceLocation> TEX_CACHE = new HashMap<Integer, ResourceLocation>();

    public EasterBossRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, (EntityModel)new EasterBossModel(ctx.m_174023_(EasterBossModel.LAYER)), 0.6f);
        this.m_115326_((RenderLayer)new QizaiHeldItemLayer((RenderLayerParent<EasterBossEntity, EasterBossModel>)this, ctx.m_234598_()));
    }

    public boolean shouldRender(EasterBossEntity entity, Frustum frustum, double cameraX, double cameraY, double cameraZ) {
        return !entity.isManorIntroHidden() && !entity.isXuanlanSkyHidden() && super.m_5523_((Mob)entity, frustum, cameraX, cameraY, cameraZ);
    }

    public ResourceLocation getTextureLocation(EasterBossEntity entity) {
        int v = entity.getVariant();
        return TEX_CACHE.computeIfAbsent(v, k -> new ResourceLocation("ftboss", "textures/entity/easter/" + EasterBossEntity.SKINS[Math.max(0, Math.min(11, k))] + ".png"));
    }

    protected void scale(EasterBossEntity entity, PoseStack poseStack, float partialTick) {
        if (entity.isMini()) {
            poseStack.m_85841_(0.55f, 0.55f, 0.55f);
        }
    }
}

