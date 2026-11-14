package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 【暗影镇印】——「暗影传送」落地点的地面魔法阵 + 暗影能量柱（世界贴花）。
 *
 * <p><b>这一版是"把已有的东西接上线"</b>：贴图 {@code textures/fx/shadow_sigil.png}（256×256）
 * 与 {@code textures/fx/shadow_pillar.png}（32×128）在 2026-09-15 就由
 * {@code tools/model3d/gen_sigil_fx.cjs} 生成好了，但**从来没有 Java 引用** ——
 * 用户报的「暗影剑的传送法阵未生效」不是参数错，而是**根本没实现**
 * （{@code docs/model-assets.md} 里那句"无 Java 引用，保留待确认"就是这个缺口）。
 *
 * <p><b>为什么必须新增一条管线</b>（顺手把 {@code util/CleaverSlamFx} 里那句
 * "真正逐 tick 扩散需要 tick 调度器，我没有擅自加"的欠账还掉）：
 * 地面魔法阵是**贴花**，不是实体、也不是粒子（粒子是点精灵，画不出阵列贴图）。
 * 服务端能自动同步给客户端的只有粒子与音效，所以贴花需要一个自定义 S2C 包 ——
 * 见 {@link net.farmingtales.ftboss.skill.FTNetwork} 与
 * {@link net.farmingtales.ftboss.skill.ShadowSigilPacket}。
 *
 * <p><b>渲染类型</b>：{@code RenderType.eyes(tex)}（本项目血怒发光层同款）=
 * NEW_ENTITY + QUADS + <b>ADDITIVE_TRANSPARENCY</b> + NO_CULL + LIGHTMAP。
 * 对本图正合适：阵列主体是**低 alpha 的暗紫**（叠加后是很淡的一层雾），
 * 只有轮廓/符文/尖角是亮紫 —— 叠加混合下"只有亮的会发光、暗的几乎看不见"，
 * 正好就是生成器注释里那句"别做成发亮的饼"。顶点 lightmap 传
 * {@link LightTexture#FULL_BRIGHT}，否则进洞里法阵会跟着环境光变暗。
 *
 * <p><b>不碰的东西</b>：不改伤害、不改半径、不改冷却；本类只消费服务端发来的坐标。
 * 加 {@code -Dftboss.debugSigil=true} 可打印每次登记/过期，便于实机验证链路。
 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ShadowSigilVisual {

    /** 法阵贴图：256×256，外环半径 118px ↔ 视觉直径 2.8 格（生成器注释里的规格）。 */
    private static final ResourceLocation SIGIL =
            new ResourceLocation(FTBossMod.MOD_ID, "textures/fx/shadow_sigil.png");
    /** 能量柱贴图：32×128，上浓下淡 + 两侧亮边。 */
    private static final ResourceLocation PILLAR =
            new ResourceLocation(FTBossMod.MOD_ID, "textures/fx/shadow_pillar.png");

    /** 完整生命周期（tick）。20 = 1 秒。 */
    private static final int LIFE = 32;
    /** 淡入 / 淡出各占多少 tick（其余时间是"悬停"）。 */
    private static final int FADE_IN = 5;
    private static final int FADE_OUT = 10;

    /** 法阵外接正方形边长（格）。直径 2.8 = 生成器里外环的视觉直径。 */
    private static final float SIGIL_SIZE = 2.8F;
    /** 法阵半径，用于同点去重与视锥剔除包围盒。 */
    private static final double SIGIL_RADIUS = SIGIL_SIZE * 0.5D;
    /** 整个生命周期的自转角（度）。约 1/4 圈，慢到不晃眼。 */
    private static final float SPIN_DEG = 100.0F;

    /** 能量柱：单面宽 × 高（格）；4 片绕 Y 轴每 45° 一片。 */
    private static final float PILLAR_SIZE = 1.05F;
    private static final float PILLAR_HEIGHT = 2.8F;
    private static final int PILLAR_SLICES = 4;

    /** 抬离地面一点点，避免与方块顶面共面 z-fighting。 */
    private static final double LIFT = 0.03D;

    /** 同一个点在这段时间内重复到达就不再叠一个（传送落点被连续触发时不堆叠）。 */
    private static final int DEDUPE_TICKS = 6;

    private static final List<Sigil> ACTIVE = new ArrayList<>();
    /** 活动表上限：纯装饰，极端情况下宁可少画几个也不能无限涨。 */
    private static final int MAX_ACTIVE = 24;

    private static final boolean DEBUG = Boolean.getBoolean("ftboss.debugSigil");

    private ShadowSigilVisual() {}

    /** 一次法阵：世界坐标 + 出生 tick + 是否有能量柱。 */
    private static final class Sigil {
        final double x;
        final double y;
        final double z;
        final boolean pillar;
        final long spawnTick;

        Sigil(double x, double y, double z, boolean pillar, long spawnTick) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.pillar = pillar;
            this.spawnTick = spawnTick;
        }
    }

    // ---------------------------------------------------------------- 生命周期

    /**
     * 收到服务端包后在**客户端主线程**登记一次法阵。见 {@code ShadowSigilPacket#handle}。
     *
     * @param y 服务端给的落点 y（这里再加 {@link #LIFT} 抬离地面，避免共面闪烁）
     */
    public static void spawn(double x, double y, double z, boolean pillar) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        long now = level.getGameTime();

        /* 同点去重：传送落点被连续触发（或包重发）时不叠一堆同心的阵。 */
        for (Sigil s : ACTIVE) {
            if (now - s.spawnTick > DEDUPE_TICKS) {
                continue;
            }
            double dx = s.x - x;
            double dz = s.z - z;
            double dy = s.y - y;
            if (dx * dx + dz * dz < SIGIL_RADIUS * SIGIL_RADIUS && Math.abs(dy) < 1.0D) {
                return;
            }
        }
        if (ACTIVE.size() >= MAX_ACTIVE) {
            ACTIVE.remove(0);
        }
        ACTIVE.add(new Sigil(x, y, z, pillar, now));
        if (DEBUG) {
            FTBossMod.LOGGER.info("[FTBoss][镇印] 登记: ({}, {}, {}) 能量柱={} 活动数={}",
                    String.format("%.2f", x), String.format("%.2f", y), String.format("%.2f", z),
                    pillar, ACTIVE.size());
        }
    }

    /** 每 tick 推进一次寿命（**不要在渲染回调里推进**，否则速度随帧率变化）。 */
    private static void tick() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            ACTIVE.clear();
            return;
        }
        long now = level.getGameTime();
        for (Iterator<Sigil> it = ACTIVE.iterator(); it.hasNext(); ) {
            Sigil s = it.next();
            if (now - s.spawnTick >= LIFE) {
                it.remove();
                if (DEBUG) {
                    FTBossMod.LOGGER.info("[FTBoss][镇印] 过期移除，剩余 {}", ACTIVE.size());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            tick();
        }
    }

    /** 退出世界时清空，避免下次进存档还残留上一局的阵。 */
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload e) {
        ACTIVE.clear();
    }

    // ---------------------------------------------------------------- 渲染

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent e) {
        if (e.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty()) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Vec3 camPos = e.getCamera().getPosition();
        float partialTick = e.getPartialTick();

        PoseStack pose = e.getPoseStack();
        pose.pushPose();
        try {
            /* 保留事件的视角变换，并将世界坐标平移到相机原点。 */
            pose.translate(-camPos.x, -camPos.y, -camPos.z);
            MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();

            /* ⚠ 每种 RenderType 每帧**只能取一次** buffer。
               2026-09-16 崩报（Description: Unexpected error /
               IllegalStateException: Not filled all elements of the vertex，
               栈顶就是本类 vert() 的 endVertex）就是循环内反复 getBuffer 造成的：
               BufferSource 对同一个 RenderType 返回的是**同一个正在构建中**的 builder，
               在 X 个阵 × Y 个柱片的循环里反复取用会让顶点元素计数错位，
               endVertex 就抛 "Not filled all elements of the vertex"。
               本工程其余渲染类（LemonCatSkillVisual / DandanScytheVisual /
               XuanlanWeaponVisual / YouziStatueVisual）都是"取一次 → 写完 → endBatch 一次"。
               这里用**懒取**而不是无条件取：只画地面阵时不产生能量柱的空批次。 */
            VertexConsumer sigilBuf = null;
            VertexConsumer pillarBuf = null;

            for (Sigil s : ACTIVE) {
                float age = (level.getGameTime() - s.spawnTick) + partialTick;
                float alpha = alphaAt(age);
                if (alpha <= 0.01F) {
                    continue;
                }
                /* 视锥剔除：包围盒覆盖整个阵（有能量柱时算上柱高）。 */
                AABB box = new AABB(s.x - SIGIL_RADIUS, s.y - 0.2D, s.z - SIGIL_RADIUS,
                        s.x + SIGIL_RADIUS, s.y + (s.pillar ? PILLAR_HEIGHT : 0.4D), s.z + SIGIL_RADIUS);
                if (e.getFrustum() != null && !e.getFrustum().isVisible(box)) {
                    continue;
                }
                if (sigilBuf == null) {
                    sigilBuf = buffers.getBuffer(RenderType.eyes(SIGIL));
                }
                if (s.pillar && pillarBuf == null) {
                    pillarBuf = buffers.getBuffer(RenderType.eyes(PILLAR));
                }
                float spin = SPIN_DEG * (age / LIFE);
                pose.pushPose();
                try {
                    pose.translate(s.x, s.y + LIFT, s.z);
                    renderSigil(pose, sigilBuf, spin, alpha);
                    if (s.pillar) {
                        renderPillar(pose, pillarBuf, spin, alpha * 0.85F, age);
                    }
                } finally {
                    pose.popPose();
                }
            }
            /* 只 endBatch 真正取过的 RenderType —— 空批次也会白走一遍管线设置。 */
            if (pillarBuf != null) {
                buffers.endBatch(RenderType.eyes(PILLAR));
            }
            if (sigilBuf != null) {
                buffers.endBatch(RenderType.eyes(SIGIL));
            }
        } finally {
            pose.popPose();
        }
    }

    /** 淡入 → 悬停 → 淡出。返回值直接当顶点 alpha（叠加混合下就是亮度）。 */
    private static float alphaAt(float age) {
        if (age < 0.0F) {
            return 0.0F;
        }
        if (age < FADE_IN) {
            return age / FADE_IN;
        }
        float tail = LIFE - FADE_OUT;
        if (age <= tail) {
            return 1.0F;
        }
        float t = (age - tail) / FADE_OUT;
        return t >= 1.0F ? 0.0F : 1.0F - t;
    }

    /**
     * 贴地的一整片法阵。
     *
     * <p>顶点直接位于 XZ 平面，无需再绕 X 轴旋转。自转前贴图向右为 +X、向下为 +Z。
     *
     * <p><b>缓冲区由调用方传入</b>（每帧每种 RenderType 只 {@code getBuffer} 一次）——
     * 见 {@link #onRenderLevel} 里那段注释，循环内取 buffer 会崩。
     */
    private static void renderSigil(PoseStack pose, VertexConsumer buf, float spinDeg, float alpha) {
        pose.pushPose();
        rotY(pose, spinDeg);     // 先绕世界 Y 自转（阵面）
        PoseStack.Pose poseState = pose.last();

        float h = SIGIL_SIZE * 0.5F;
        /* 画在 XZ 平面上，四角 UV 按"图像向右 = +X、图像向下 = +Z"给。 */
        vert(buf, poseState, -h, 0.0F, -h, 0.0F, 0.0F, alpha, 0.0F, 1.0F, 0.0F);
        vert(buf, poseState, -h, 0.0F, h, 0.0F, 1.0F, alpha, 0.0F, 1.0F, 0.0F);
        vert(buf, poseState, h, 0.0F, h, 1.0F, 1.0F, alpha, 0.0F, 1.0F, 0.0F);
        vert(buf, poseState, h, 0.0F, -h, 1.0F, 0.0F, alpha, 0.0F, 1.0F, 0.0F);
        pose.popPose();
    }

    /**
     * 暗影能量柱：{@link #PILLAR_SLICES} 片竖直面绕 Y 轴均布 + 与地面阵反向自转，
     * 形成"体积感"的光柱（叠加混合下交叉面自然更亮）。贴图本来就只有 32px 宽，
     * 竖直方向按 {@link #PILLAR_HEIGHT} 拉伸即可（是渐变，拉伸没有代价）。
     */
    private static void renderPillar(PoseStack pose, VertexConsumer buf, float spinDeg, float alpha,
                                     float age) {
        pose.pushPose();
        /* 反向自转（−0.6×）让柱体和地面阵不是刚体联动，看着更像"能量在流"。 */
        rotY(pose, -spinDeg * 0.6F);

        float hw = PILLAR_SIZE * 0.5F;
        /* 轻微呼吸：高度在 0.94~1.06 之间起伏，避免整根柱像块静止的板。 */
        float breathe = 1.0F + 0.06F * (float) Math.sin(age * 0.35D);
        float top = PILLAR_HEIGHT * breathe;

        for (int i = 0; i < PILLAR_SLICES; i++) {
            pose.pushPose();
            rotY(pose, 180.0F * i / PILLAR_SLICES);
            PoseStack.Pose poseState = pose.last();
            /* 贴图 v=0（顶端、最浓）放在柱顶。 */
            vert(buf, poseState, -hw, 0.0F, 0.0F, 0.0F, 1.0F, alpha, 0.0F, 0.0F, 1.0F);
            vert(buf, poseState, hw, 0.0F, 0.0F, 1.0F, 1.0F, alpha, 0.0F, 0.0F, 1.0F);
            vert(buf, poseState, hw, top, 0.0F, 1.0F, 0.0F, alpha, 0.0F, 0.0F, 1.0F);
            vert(buf, poseState, -hw, top, 0.0F, 0.0F, 0.0F, alpha, 0.0F, 0.0F, 1.0F);
            pose.popPose();
        }
        pose.popPose();
    }

    /** NEW_ENTITY 顶点格式：position → color → uv → overlay → lightmap → normal。 */
    private static void vert(VertexConsumer vertices, PoseStack.Pose pose,
                             float posX, float posY, float posZ, float texU, float texV, float alpha,
                             float normalX, float normalY, float normalZ) {
        vertices.vertex(pose.pose(), posX, posY, posZ)
                .color(1.0F, 1.0F, 1.0F, alpha)
                .uv(texU, texV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(pose.normal(), normalX, normalY, normalZ)
                .endVertex();
    }

    /* ------------------------------------------------------------------ 旋转
     * ⚠ 1.20.1 的 {@code PoseStack} **没有** {@code rotate(float,float,float,float)}，
     * 也**没有** {@code rotate(Quaternionf)} —— 本项目实际编译用的 API 是
     * {@code translate(double,double,double)} / {@code mulPose(Quaternionf)} /
     * {@code pushPose()} / {@code popPose()} / {@code last()}。
     * （两次编译报错把这两个都试掉了：先报 `rotate(float,float,float,float)`，
     *  换成四元数后报 `rotate(Quaternionf)`。）
     * 所以统一走工程里已有的写法 —— {@code Axis.XP/YP.rotationDegrees()} + {@code mulPose}，
     * 见 {@code BarrelCannonballRenderer} / {@code FlywheelRenderer} / {@code WineBarrelGeoRenderer}。
     */

    /** 绕 Y 轴按度旋转（阵面自转、能量柱分片）。 */
    private static void rotY(PoseStack pose, float deg) {
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(deg));
    }

}
