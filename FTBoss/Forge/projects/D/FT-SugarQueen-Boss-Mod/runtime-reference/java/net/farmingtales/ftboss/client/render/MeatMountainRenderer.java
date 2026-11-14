/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.model.geom.ModelLayerLocation
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.client.renderer.entity.MobRenderer
 *  net.minecraft.client.renderer.entity.RenderLayerParent
 *  net.minecraft.client.renderer.entity.layers.ItemInHandLayer
 *  net.minecraft.client.renderer.entity.layers.RenderLayer
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Mob
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.model.ButcherBossModel;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class MeatMountainRenderer
extends MobRenderer<MeatMountainEntity, ButcherBossModel<MeatMountainEntity>> {
    public static final ModelLayerLocation LAYER = ButcherBossModel.LAYER;
    private static final ResourceLocation BUTCHER_TEXTURE = new ResourceLocation("ftboss", "textures/entity/butcher_v3.png");
    private static boolean loggedBake = false;
    private static final Set<Integer> LOGGED_RENDER = ConcurrentHashMap.newKeySet();

    public MeatMountainRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ButcherBossModel(ctx.m_174023_(LAYER)), 0.9f);
        this.m_115326_((RenderLayer)new ItemInHandLayer((RenderLayerParent)this, ctx.m_234598_()));
        if (!loggedBake) {
            loggedBake = true;
            long parts = -1L;
            try {
                parts = ctx.m_174023_(LAYER).m_171331_().count();
            }
            catch (Throwable t) {
                FTBossMod.LOGGER.warn("[FTBoss][\u5c60\u592b\u8bca\u65ad] \u7edf\u8ba1\u6a21\u578b\u90e8\u4ef6\u5931\u8d25: {}", (Object)t.toString());
            }
            FTBossMod.LOGGER.info("[FTBoss][\u5c60\u592b\u8bca\u65ad] \u6a21\u578b\u5c42 {} \u70d8\u7119\u5b8c\u6210: \u90e8\u4ef6\u6570={} (\u81ea\u5236\u5c60\u592b\u6a21\u578b\u5e94\u4e3a \u5206\u8282\u8089\u817f\u5c60\u592b v3); \u8d34\u56fe={}", new Object[]{LAYER, parts, BUTCHER_TEXTURE});
        }
    }

    public void render(MeatMountainEntity entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        if (LOGGED_RENDER.size() < 64 && LOGGED_RENDER.add(entity.m_19879_())) {
            FTBossMod.LOGGER.info("[FTBoss][\u5c60\u592b\u8bca\u65ad] \u9996\u6b21\u6e32\u67d3(\u5b9e\u4f53id={}): \u540d\u79f0={} \u8840\u91cf={}/{} \u9690\u8eab={} \u5e7c\u5e74={} \u4e3b\u624b={} \u4f4d\u7f6e=({}, {}, {})", new Object[]{entity.m_19879_(), entity.m_7755_().getString(), Float.valueOf(entity.m_21223_()), Float.valueOf(entity.m_21233_()), entity.m_20145_(), entity.m_6162_(), entity.m_21205_(), String.format("%.1f", entity.m_20185_()), String.format("%.1f", entity.m_20186_()), String.format("%.1f", entity.m_20189_())});
        }
        super.m_7392_((Mob)entity, yaw, partialTick, pose, buffers, light);
    }

    public ResourceLocation getTextureLocation(MeatMountainEntity entity) {
        return BUTCHER_TEXTURE;
    }
}

