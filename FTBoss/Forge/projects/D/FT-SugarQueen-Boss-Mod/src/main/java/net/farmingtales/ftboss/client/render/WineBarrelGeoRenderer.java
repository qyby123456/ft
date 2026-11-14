package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.entity.WineBarrelBossEntity;
import net.farmingtales.ftboss.item.FTBossItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

/**
 * 酒桶王渲染: GeckoLib 横放巨型酒桶 + **手里的【酒桶弩】**(2026-09-14 v108 按用户要求导入)。
 *
 * <p>GeckoLib 实体不能用原版 {@code ItemInHandLayer}(它的模型不实现 {@code ArmedModel}),
 * 所以手持物一律走 {@link BlockAndItemGeoLayer} —— 本模组的炫彩答辩超人(土豆加农炮)就是这么做的,
 * 这里照抄同一套。手里是"空弦"还是"装填完成"由 {@link WineBarrelBossEntity} 换手决定, 渲染层不用管。
 */
public class WineBarrelGeoRenderer extends GeoEntityRenderer<WineBarrelBossEntity> {

    /* ============================================================================
       ★ 调参区(位置/大小看起来不对时只改这几个数) ★
       · CROSSBOW_BONE —— 弩挂哪根骨骼。酒桶王这套 geo(17 根骨)**没有手/手臂**:
           body / barrel_main / 三圈箍(hoop_*) / 软木塞(cork,cork_handle) /
           左右提手(handle_left,handle_right) / 四条腿 / 酒液(wine_liquid,wine_glow) / 出酒口(spout)。
           默认 **spout**(桶口正前方, pivot 0,14,-20) —— 那儿正是它抛酒桶炮弹的"炮口",
           弩口朝 -Z, 和模型正面一致, 读起来就是"酒桶上架了一把大弩"。
           想换: handle_right(右侧提手) / cork(顶部软木塞) / barrel_main(桶身中心)。
       · OFF_X/Y/Z —— 相对骨骼原点的微调, 单位是**格**(16 = 1 格)。
       · SCALE —— 缩放。用户模型本身约 2.4 格宽、桶身约 2.75 格, 所以 1.0 = 原样导入。
       · 朝向 —— 下面用 {@code ItemDisplayContext.NONE}(不套 Blockbench 里那套"玩家手持"变换),
           所以模型自身坐标原样进场景; 要转向就在 renderStackForBone 里加 poseStack.mulPose(...)。
       ============================================================================ */
    public static final String CROSSBOW_BONE = "spout";
    public static final float OFF_X = 0.0f, OFF_Y = 0.0f, OFF_Z = 0.0f;
    public static final float SCALE = 1.0f;
    /** 朝向微调(绕 Y 轴, 度)。0 = 用模型自身朝向 —— 用户模型的弩口朝 -Z,
     *  而 GeckoLib 渲染时模型的 **-Z 就是实体正面**(桶口/软木塞都在 -Z, 和"喷玩家"的技能一致),
     *  所以 0 就是"弩口对着玩家"。反了就写 180。 */
    public static final float YAW_DEG = 0.0f;

    public WineBarrelGeoRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new net.farmingtales.ftboss.client.model.GiantBarrelModel());
        this.shadowRadius = 1.4F;
        this.addRenderLayer(new CrossbowLayer(this));
    }

    /** 只在指定骨骼上渲染"手里那把酒桶弩"(空弦 / 装填两版都由手里的物品决定) */
    private static final class CrossbowLayer extends BlockAndItemGeoLayer<WineBarrelBossEntity> {

        CrossbowLayer(GeoRenderer<WineBarrelBossEntity> renderer) {
            super(renderer, (bone, animatable) -> {
                if (!CROSSBOW_BONE.equals(bone.getName())) return null;
                ItemStack held = animatable.getMainHandItem();
                /* 只有拿的是酒桶弩时才由这一层渲染 —— 免得以后 Boss 换别的武器时模型叠两份 */
                boolean isCrossbow = held.is(FTBossItems.BARREL_CROSSBOW.get())
                        || held.is(FTBossItems.BARREL_CROSSBOW_READY.get());
                return isCrossbow ? held : null;
            }, (bone, animatable) -> null);
        }

        /** 不套 Blockbench 的"玩家手持"变换: 直接按模型自身坐标摆在骨骼上(方便调参) */
        @Override
        protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, WineBarrelBossEntity animatable) {
            return ItemDisplayContext.NONE;
        }

        /** 微调: 偏移(格) + 缩放 */
        @Override
        protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack,
                                          WineBarrelBossEntity animatable, MultiBufferSource bufferSource,
                                          float partialTick, int packedLight, int packedOverlay) {
            poseStack.pushPose();
            if (OFF_X != 0.0f || OFF_Y != 0.0f || OFF_Z != 0.0f) {
                poseStack.translate(OFF_X / 16.0f, OFF_Y / 16.0f, OFF_Z / 16.0f);
            }
            if (SCALE != 1.0f) poseStack.scale(SCALE, SCALE, SCALE);
            if (YAW_DEG != 0.0f) poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(YAW_DEG));
            super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }
}
