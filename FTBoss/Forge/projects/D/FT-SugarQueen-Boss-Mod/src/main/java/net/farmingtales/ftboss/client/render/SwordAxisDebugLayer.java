package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * 【实机标定用】在模型的**本地空间**里画坐标轴与刻度尺。
 *
 * <p><b>为什么需要它</b>：物品的 {@code display} 旋转/平移作用在「手部局部空间」里，
 * 那个空间的轴指向由渲染管线的基变换决定，离线算式里没有它 —— 所以"离线算出来的
 * 握持高度"与"游戏里的握持高度"必然差一个常数，只靠离线工具无法定论（见
 * {@code tools/model3d/shadow_sword_pos_audit.cjs} 的 §C）。
 *
 * <p>这一层把三样东西画进游戏画面，一次截图就能把这个常数定出来：
 * <ul>
 *   <li>原点处的白色十字 = {@code display} 的原点 = 握点基准</li>
 *   <li>三根轴，各长 1 格：<b>红=+X、绿=+Y（指向剑尖）、蓝=+Z</b></li>
 *   <li>沿 +Y（刃侧）每 8 模型单位一道白色刻度；沿 -Y（柄侧）每 8 单位一道黄色刻度。
 *       黄刻度那一串才是用来对"手心落在哪"的。</li>
 * </ul>
 *
 * <p><b>怎么用</b>：加 JVM 参数开启，不必改代码：{@code -Dftboss.debugSwordAxes=true}
 * （PCL/启动器里填"JVM 参数"，或 IDE 的 VM options）。
 * 开启后进游戏手持暗影剑、按 F5 切第三人称截图；日志里会同时打出当帧原点坐标。
 *
 * <p><b>注意</b>：默认关闭。画出来的是一格长的线，不是模型的一部分；标定完请去掉该 JVM 参数。
 *
 * <p><b>实测记录（2026-09-15）</b>：第一版刻度臂长只有 3 单位、轴长 1 格，
 * 用户实机截图里**只有几个白点露出来**，三根轴全被剑身埋住 —— 所以现在刻度臂长 12、
 * 轴长保持 1 格但颜色加饱和。改这一层时不要再把它调小。
 */
public class SwordAxisDebugLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {

    /** 开关：读系统属性，默认关闭。 */
    public static final String PROPERTY = "ftboss.debugSwordAxes";

    /** 一格 = 16 模型单位。 */
    private static final float UNIT = 1.0F / 16.0F;

    /** 轴长（格）。 */
    private static final float AXIS_LENGTH = 1.0F;

    /** 刻度：间距（模型单位）与每侧条数。 */
    private static final float TICK_STEP = 8.0F;
    private static final int TICK_COUNT = 4;

    /** 刻度臂长（模型单位）——必须足够长才不会被剑身埋掉。 */
    private static final float TICK_ARM = 12.0F;

    private static final org.slf4j.Logger LOGGER =
            com.mojang.logging.LogUtils.getLogger();

    /** 每个渲染对象只打一次日志，避免刷屏。 */
    private Object reported;

    /**
     * 渲染器引用用**通配符类型**持有：本层只需要"读一眼渲染器"，
     * 不需要写回，所以放宽成 {@code ? extends GeoAnimatable}。
     */
    private final GeoRenderer<? extends GeoAnimatable> debugRenderer;

    /**
     * @param renderer 渲染器（只读引用）
     *
     * <p><b>这里的强转是必要的</b>：GeckoLib 的 {@code GeoRenderLayer<T>(GeoRenderer<T>)}
     * 与字段 {@code protected final GeoRenderer<T> renderer} 都要求**精确的** {@code T}，
     * 而 {@code GeoRenderer<? extends GeoAnimatable>} 是 capture 类型，直接写
     * {@code super(renderer)} 会编译失败：
     * {@code incompatible types: GeoRenderer<CAP#1> cannot be converted to GeoRenderer<T>}。
     * 强转本身安全 —— 调用点 {@link #of} 保证了两边的 {@code T} 是同一个类型。
     */
    @SuppressWarnings("unchecked")
    public SwordAxisDebugLayer(GeoRenderer<? extends GeoAnimatable> renderer) {
        super((GeoRenderer<T>) renderer);
        this.debugRenderer = renderer;
    }

    /**
     * 推断式构造：调用处写 {@code SwordAxisDebugLayer.of(this)}，
     * 由渲染器的具体类型自动推断 {@code T}，渲染器里不必写类型参数、也不必强转。
     */
    public static <R extends GeoAnimatable> SwordAxisDebugLayer<R> of(GeoRenderer<R> renderer) {
        return new SwordAxisDebugLayer<>(renderer);
    }

    /** 通配符引用的只读用点。 */
    public GeoRenderer<? extends GeoAnimatable> getDebugRenderer() {
        return debugRenderer;
    }

    public static boolean enabled() {
        return Boolean.parseBoolean(System.getProperty(PROPERTY, "false"));
    }

    /**
     * 在**渲染到根骨骼时**画：此刻 poseStack 已经在"模型本地空间"，
     * 单位是 geo 文件里的模型单位，原点就是 {@code display} 的原点。
     * 子骨骼不能再画 —— 它们的 poseStack 已经带上了该骨骼的 pivot 与旋转。
     */
    @Override
    public void renderForBone(PoseStack poseStack, T animatable, GeoBone bone,
                              RenderType renderType, MultiBufferSource bufferSource,
                              VertexConsumer buffer, float partialTick,
                              int packedLight, int packedOverlay) {
        if (!enabled() || bone.getParent() != null) {
            return;
        }

        reportOnce(poseStack, animatable, bone);

        VertexConsumer lines = bufferSource.getBuffer(RenderType.lines());

        cross(poseStack, lines);

        axis(poseStack, lines, 1, 0, 0, 1.00F, 0.20F, 0.20F);   // +X 红
        axis(poseStack, lines, 0, 1, 0, 0.20F, 1.00F, 0.30F);   // +Y 绿（剑尖方向）
        axis(poseStack, lines, 0, 0, 1, 0.30F, 0.50F, 1.00F);   // +Z 蓝

        // 刃侧刻度（白）
        for (int i = 1; i <= TICK_COUNT; i++) {
            tick(poseStack, lines, i * TICK_STEP, 0.95F, 0.95F, 0.95F);
        }

        // 柄侧刻度（黄）—— 用来对"手心落在原点下方第几道"
        for (int i = 1; i <= TICK_COUNT; i++) {
            tick(poseStack, lines, -i * TICK_STEP, 1.00F, 0.85F, 0.20F);
        }
    }

    /**
     * 每个渲染对象只报一次：把当帧 poseStack 的平移分量打出来。
     *
     * <p>这里**刻意不用** {@code GeoItemRenderer.itemRenderTranslations} ——
     * 那两个字段是 {@code protected}，跨类读不到（实测报
     * {@code modelRenderTranslations has protected access}）。
     * {@code poseStack.last().pose()} 是公开 API，拿到的就是这个轴所在坐标系的最终矩阵。
     */
    private void reportOnce(PoseStack poseStack, T animatable, GeoBone bone) {
        if (reported == animatable) {
            return;
        }

        reported = animatable;

        org.joml.Matrix4f m = poseStack.last().pose();

        LOGGER.info(
                "[FTBoss][剑坐标轴] {} 根骨骼={} 当帧原点=[{}. {}. {}] "
                        + "读法：白十字=display原点，绿轴=剑尖方向，"
                        + "刻度间距 8 模型单位（半格），白刻度在刃侧、黄刻度在柄侧",
                animatable.getClass().getSimpleName(),
                bone.getName(),
                String.format("%.5f", m.m30()),
                String.format("%.5f", m.m31()),
                String.format("%.5f", m.m32()));
    }

    // ---- 图元 ----

    private static void cross(PoseStack poseStack, VertexConsumer vc) {
        float r = TICK_ARM;

        seg(poseStack, vc, -r, 0, 0, r, 0, 0, 1, 1, 1);
        seg(poseStack, vc, 0, -r, 0, 0, r, 0, 1, 1, 1);
        seg(poseStack, vc, 0, 0, -r, 0, 0, r, 1, 1, 1);
    }

    private static void axis(PoseStack poseStack, VertexConsumer vc,
                             float dx, float dy, float dz,
                             float r, float g, float b) {
        float len = AXIS_LENGTH / UNIT;

        seg(poseStack, vc, 0, 0, 0, dx * len, dy * len, dz * len, r, g, b);
    }

    private static void tick(PoseStack poseStack, VertexConsumer vc,
                             float y, float r, float g, float b) {
        float s = TICK_ARM;

        seg(poseStack, vc, -s, y, 0, s, y, 0, r, g, b);
        seg(poseStack, vc, 0, y, -s, 0, y, s, r, g, b);
    }

    private static void seg(PoseStack poseStack, VertexConsumer vc,
                            float x1, float y1, float z1,
                            float x2, float y2, float z2,
                            float r, float g, float b) {
        PoseStack.Pose pose = poseStack.last();

        float nx = x2 - x1;
        float ny = y2 - y1;
        float nz = z2 - z1;

        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);

        if (len < 1.0E-5F) {
            return;
        }

        nx /= len;
        ny /= len;
        nz /= len;

        vc.vertex(pose.pose(), x1, y1, z1)
                .color(r, g, b, 1.0F)
                .normal(pose.normal(), nx, ny, nz)
                .endVertex();

        vc.vertex(pose.pose(), x2, y2, z2)
                .color(r, g, b, 1.0F)
                .normal(pose.normal(), nx, ny, nz)
                .endVertex();
    }
}
