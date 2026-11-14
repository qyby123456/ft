package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * 【彩蛋 Boss 渲染器】(2026-09-12)
 *
 * <p>用原版玩家模型({@link EasterBossModel}) + 每位一张皮肤(64×64 标准玩家皮肤)。
 * 皮肤按变体缓存, 放大 1.25 倍到 Boss 体型。
 */
public class EasterBossRenderer extends MobRenderer<EasterBossEntity, EasterBossModel> {

    private static final Map<Integer, ResourceLocation> TEX_CACHE = new HashMap<>();

    public EasterBossRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new EasterBossModel(ctx.bakeLayer(EasterBossModel.LAYER)), 0.6F);

        /* ============ 2026-09-13 晚按用户要求: **只隐藏护甲**, 武器照旧显示 ============
         * 用户原话:「把 boss 的装备隐藏起来, 保持效果但是允许玩家看见 boss 皮肤」,
         * 随后澄清:「**隐藏护甲不是隐藏武器**」。
         *
         * 所以这里只摘掉护甲那一层 —— 腾炎套(焰魔 Ignis 的护甲)**照常穿在身上、照常生效**
         * (属性/附魔/保护全在, 服务端那套物品一点没动), 只是不画出来, 让 64×64 的自定义皮肤
         * 完整露出来。**主手武器继续渲染**(劈砍/突刺/横扫时武器跟着手臂动)。
         *
         * ⚠️ 想恢复显示护甲, 把下面那段 HumanoidArmorLayer 取消注释即可。 */
        this.addLayer(new QizaiHeldItemLayer(this, ctx.getItemInHandRenderer()));

        // this.addLayer(new net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer<>(
        //         this,
        //         new net.minecraft.client.model.HumanoidModel<>(ctx.bakeLayer(
        //                 net.minecraft.client.model.geom.ModelLayers.PLAYER_INNER_ARMOR)),
        //         new net.minecraft.client.model.HumanoidModel<>(ctx.bakeLayer(
        //                 net.minecraft.client.model.geom.ModelLayers.PLAYER_OUTER_ARMOR)),
        //         ctx.getModelManager()));
    }

    @Override
    public boolean shouldRender(EasterBossEntity entity, net.minecraft.client.renderer.culling.Frustum frustum,
                                double cameraX, double cameraY, double cameraZ) {
        return !entity.isManorIntroHidden() && !entity.isXuanlanSkyHidden()
                && super.shouldRender(entity, frustum, cameraX, cameraY, cameraZ);
    }

    @Override
    public ResourceLocation getTextureLocation(EasterBossEntity entity) {
        int v = entity.getVariant();
        return TEX_CACHE.computeIfAbsent(v, k -> new ResourceLocation(FTBossMod.MOD_ID,
                "textures/entity/easter/" + EasterBossEntity.SKINS[Math.max(0, Math.min(EasterBossEntity.COUNT - 1, k))] + ".png"));
    }

    @Override
    protected void scale(EasterBossEntity entity, PoseStack poseStack, float partialTick) {
        if (entity.isMini()) {
            poseStack.scale(0.55f, 0.55f, 0.55f);
        }
    }

    /* 2026-09-12 按用户要求: **与玩家同尺寸**(= armamentarium 的 Boss 体型), 所以不放大。
       玩家模型原点就是脚底, 天然对齐碰撞箱底。 */
}
