package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.item.weapons.ShadowSwordItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/**
 * 【噬影之刃 · 裂瞳】GeckoLib 物品渲染器（手持骨骼动画 + 裂瞳回响贴图态）。
 *
 * 说明（API 从 libs/[前置]geckolib-forge-1.20.1-4.8.4.jar javap 核对过，不是照文档猜的）：
 *  · {@link GeoItemRenderer} 本身 extends BlockEntityWithoutLevelRenderer，所以
 *    {@code IClientItemExtensions#getCustomRenderer()} 直接返回它即可；
 *  · {@link GeoModel} 只需实现三个 ResourceLocation（模型/贴图/动画）。
 *
 * ★ 裂瞳回响（2026-09-15）：冷却完成 -> 换用亮一档的 {@code shadow_sword_ready.png}。
 *   ready 贴图是**逐像素派生**出来的（只提亮 b-r>20 的偏紫像素，中性暗色剑身一个像素都没动），
 *   所以切换时轮廓不会跳变 ✓
 *
 * ⚠ 已知局限（不谎报）：
 *  · 冷却状态取自**本地玩家**（{@code Minecraft.getInstance().player}）——
 *    因此别人手里/展示框里的剑会跟随本地玩家的冷却状态显示 ✗（物品栏/掉落物同理）。
 *    要精确到"每个渲染对象自己的状态"，需要把 ready 写进 ItemStack 的 NBT 或同步数据 ✗（下一步）。
 *  · 本类整体只在客户端加载（仅被 ShadowSwordItem#initializeClient 引用），专服安全 ✓
 */
public class ShadowSwordGeoRenderer extends GeoItemRenderer<ShadowSwordItem> {

    public ShadowSwordGeoRenderer() {
        super(new Model());

        /* 2026-09-16 诊断：用户报「武器位置又歪了」。查日志发现**只有黑切**打了
           「调用链② getCustomRenderer -> 构造 GeoItemRenderer」，暗影剑一条都没有 ——
           要么它压根没走到这里（于是模型是 builtin/entity 的暗影剑在手里什么都不画），
           要么只是当时没加日志。加一条构造日志，一次就能分清。
           排查完可以删掉这一行。 */
        net.farmingtales.ftboss.FTBossMod.LOGGER.info(
                "[FTBoss][暗影剑] 调用链② 构造 ShadowSwordGeoRenderer（模型=ftboss:geo/shadow_sword.geo.json）");

        /*
         * 实机标定层：默认不画（读系统属性 ftboss.debugSwordAxes）。
         * 用途见 SwordAxisDebugLayer 的注释 —— 用它在游戏里画出"display 原点 + 三轴 + 刻度"，
         * 再和 tools/model3d/shadow_sword_pos_audit.cjs 的两种读法对照，
         * 才能确定 trans 到底该往哪边调（离线算式缺了手部局部空间的基，差一个常数）。
         *
         * 用 of(...) 由 this 的具体类型推断层的类型参数，避免在这里写类型参数或强转。
         */
        addRenderLayer(SwordAxisDebugLayer.of(this));
    }

    /**
     * ★ 裂瞳回响：读**这把剑自己的** NBT 标记（服务端按持有者冷却写入，随背包同步下发）选贴图。
     *   放在这里而不是 Model 里，是因为只有渲染器实例才能拿到 getCurrentItemStack()。
     *   于是每把剑 / 展示框 / 别人手里的剑都各自独立，不再跟随本地玩家 ✓
     */
    @Override
    public ResourceLocation getTextureLocation(ShadowSwordItem animatable) {
        ItemStack stack = getCurrentItemStack();
        if (stack != null && stack.hasTag() && stack.getTag().getBoolean(ShadowSwordItem.TAG_READY)) {
            return Model.READY;
        }
        return Model.NORMAL;
    }

    /** 模型定义：三个资源路径 + 随冷却切换的贴图。 */
    public static class Model extends GeoModel<ShadowSwordItem> {

        private static final ResourceLocation NORMAL =
                new ResourceLocation("ftboss", "textures/item/shadow_sword.png");
        private static final ResourceLocation READY =
                new ResourceLocation("ftboss", "textures/item/shadow_sword_ready.png");

        @Override
        public ResourceLocation getModelResource(ShadowSwordItem animatable) {
            return new ResourceLocation("ftboss", "geo/shadow_sword.geo.json");
        }

        @Override
        public ResourceLocation getAnimationResource(ShadowSwordItem animatable) {
            return new ResourceLocation("ftboss", "animations/shadow_sword.animation.json");
        }

        /** 冷却完成（技能可用）时用 ready 贴图；冷却中或拿不到玩家时用常态贴图。 */
        /** 默认贴图。"技能已就绪"的切换在外层渲染器的 getTextureLocation 里做
            —— 静态嵌套类里拿不到 getCurrentItemStack()（编译期就会报静态上下文错误）。 */
        @Override
        public ResourceLocation getTextureResource(ShadowSwordItem animatable) {
            return NORMAL;
        }
    }
}
