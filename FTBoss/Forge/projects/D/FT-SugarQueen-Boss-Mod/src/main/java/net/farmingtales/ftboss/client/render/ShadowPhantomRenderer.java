package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.model.ShadowPhantomModel;
import net.farmingtales.ftboss.entity.ShadowPhantomEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * 【折耳根紫色残影的渲染器】(2026-09-24 按用户要求「独立做一个折耳根人形紫色残影的模型」)。
 *
 * <ul>
 *   <li>模型：{@link ShadowPhantomModel}（标准人形骨架，与本体同比例）；</li>
 *   <li>贴图：{@code textures/entity/easter/phantom_zheergen.png} —— 由折耳根自己的皮肤
 *       重着色成**紫色单色**、并把**透明度烘进 alpha 通道**得到的残影图，
 *       生成器见 {@code tools/model3d/GenPhantomSkin.java}（改皮肤/改色/改透明都重跑它）；</li>
 *   <li>半透明：管线走 {@link RenderType#entityTranslucent} —— 不透明管线
 *       （entityCutoutNoCull）**根本不看 alpha**，这一步是"残影感"的关键。
 *       ★ 透明度**不再**用 {@code RenderSystem.setShaderColor} 给：那是全局状态，
 *       而实体真正画出来是在批次 flush 的时候（比 render() 返回晚得多），
 *       提前复位就白设了 —— 现在把 alpha 烘进贴图，既确定又不碰全局状态；</li>
 *   <li>影子：{@code shadowRadius = 0} —— 残影不该在地上留一团实体阴影。</li>
 * </ul>
 */
public class ShadowPhantomRenderer extends MobRenderer<ShadowPhantomEntity, ShadowPhantomModel> {

    /** 紫色残影贴图（生成物，透明度就在它的 alpha 通道里）。 */
    public static final ResourceLocation TEXTURE =
            new ResourceLocation(FTBossMod.MOD_ID, "textures/entity/easter/phantom_zheergen.png");

    public ShadowPhantomRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ShadowPhantomModel(ctx.bakeLayer(ModelLayers.PLAYER)), 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(ShadowPhantomEntity entity) {
        return TEXTURE;
    }

    @Override
    protected RenderType getRenderType(ShadowPhantomEntity entity, boolean bodyVisible, boolean translucent,
                                       boolean glowing) {
        return RenderType.entityTranslucent(TEXTURE);
    }
}
