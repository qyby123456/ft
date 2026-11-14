package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.client.model.ButcherBossModel;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * 屠夫渲染(V2: 自持模型)。
 * <p><b>2026-09-10 重做</b>：旧版是「原版卫道士几何 + scale 1.7」的套皮，本版换成
 * {@link ButcherBossModel}(自制 LayerDefinition, 不依赖任何原版模型层)。
 * <ul>
 *   <li>尺寸 1.81 x 2.56 x 1.11 格, 脚底正好在 y=24, 因此**没有任何 scale/translate**，
 *       从根上消除了"模型比碰撞箱高/低半格"那类错位问题;</li>
 *   <li>手持肉腿仍由 {@link ItemInHandLayer} 渲染(几何里**没有**烘焙菜刀)，落点由
 *       {@code ButcherBossModel.translateToHand} 给出；</li>
 *   <li>贴图换新文件名 {@code butcher_v3.png}，规避 Lightspeed 同名旧索引。</li>
 * </ul>
 * <p>关于 EMF：反编译确认屠夫的层是 {@code ftboss:butcher#main}(命名空间非 minecraft)，
 * 在 {@code allowedCEM=KNOWN_ONLY} 下 EMF 会直接 return、根本不接管，所以本问题与 EMF 无关。
 * 下面两条一次性诊断日志用于继续定位"模型消失"，<b>请保留</b>。
 */
public class MeatMountainRenderer extends MobRenderer<MeatMountainEntity, ButcherBossModel<MeatMountainEntity>> {

    /** 兼容旧引用：实际层定义在模型类里 */
    public static final ModelLayerLocation LAYER = ButcherBossModel.LAYER;

    private static final ResourceLocation BUTCHER_TEXTURE =
            new ResourceLocation("ftboss", "textures/entity/butcher_v3.png");

    /** 诊断用: 只打一次, 避免刷屏 */
    private static boolean loggedBake = false;
    /**
     * 每个实体第一次被渲染时打一条(按实体 id 去重)。
     * 2026-09-10: 原来用单个 static boolean, 一局里只有"第一个被渲染的屠夫"会留痕,
     * 导致"召唤出来的那只到底渲染了没有"无法判断 —— 现在每一只都有自己的记录。
     */
    private static final java.util.Set<Integer> LOGGED_RENDER = java.util.concurrent.ConcurrentHashMap.newKeySet();

    public MeatMountainRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ButcherBossModel<>(ctx.bakeLayer(LAYER)), 0.9F);
        // 手持肉腿可见(ButcherBossModel 实现了 ArmedModel, 物品落到手心)
        this.addLayer(new ItemInHandLayer<>(this, ctx.getItemInHandRenderer()));
        if (!loggedBake) {
            loggedBake = true;
            long parts = -1L;
            try {
                parts = ctx.bakeLayer(LAYER).getAllParts().count();
            } catch (Throwable t) {
                net.farmingtales.ftboss.FTBossMod.LOGGER.warn("[FTBoss][屠夫诊断] 统计模型部件失败: {}", t.toString());
            }
            net.farmingtales.ftboss.FTBossMod.LOGGER.info(
                    "[FTBoss][屠夫诊断] 模型层 {} 烘焙完成: 部件数={} (自制屠夫模型应为 分节肉腿屠夫 v3); 贴图={}",
                    LAYER, parts, BUTCHER_TEXTURE);
        }
    }

    @Override
    public void render(MeatMountainEntity entity, float yaw, float partialTick, PoseStack pose,
                       net.minecraft.client.renderer.MultiBufferSource buffers, int light) {
        if (LOGGED_RENDER.size() < 64 && LOGGED_RENDER.add(entity.getId())) {
            net.farmingtales.ftboss.FTBossMod.LOGGER.info(
                    "[FTBoss][屠夫诊断] 首次渲染(实体id={}): 名称={} 血量={}/{} 隐身={} 幼年={} 主手={} 位置=({}, {}, {})",
                    entity.getId(), entity.getName().getString(), entity.getHealth(), entity.getMaxHealth(),
                    entity.isInvisible(), entity.isBaby(), entity.getMainHandItem(),
                    String.format("%.1f", entity.getX()), String.format("%.1f", entity.getY()), String.format("%.1f", entity.getZ()));
        }
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(MeatMountainEntity entity) {
        return BUTCHER_TEXTURE;
    }

    // 注意: 不再 override scale() —— V2 模型按 1:1 尺寸烘焙, 任何缩放都会与碰撞箱错位
}
