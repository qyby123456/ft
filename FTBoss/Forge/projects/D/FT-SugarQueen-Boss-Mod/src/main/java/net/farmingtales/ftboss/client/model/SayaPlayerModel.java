package net.farmingtales.ftboss.client.model;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastType;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * 沙耶专用玩家模型: 在 Alex 人形基础上加入「铁魔法施法姿态」。
 *
 * <p><b>为什么不用铁魔法自带的那套动画</b>: 铁魔法对玩家的施法动作由 playeranimator 库驱动,
 * 该库的 mixin 目标只有 {@code Player} / {@code PlayerModel} / {@code PlayerRenderer}; 沙耶是自定义实体,
 * 拿不到那套骨骼动画。{@code AbstractSpellCastingMob} 那套 GeckoLib 钩子
 * ({@code shouldPointArmsWhileCasting} / {@code SpellAnimations}) 又要求实体提供 GeckoLib 模型,
 * 而沙耶用的是原版人形模型, 同样用不上。所以这里**按施法状态手动复刻**, 并把姿态做细。
 *
 * <p><b>2026-09-10 完善</b>: 从"只有一种双手前举"升级为**六套按法术区分的姿态** + 三段相位:
 * <ol>
 *   <li>风格来自客户端已同步的 {@link MagicData}(施法法术 id + {@link CastType}), 取不到就回落到
 *       {@link #STYLE_TWO_HAND} —— 与旧版姿势一致, 保证任何情况下都不难看;</li>
 *   <li>相位: 抬手(0.3s) → 保持(带呼吸/振颤) → 收势(0.4s 前推余韵), 抬手时姿势从站立渐变到位;</li>
 *   <li>施法时双腿站定、身体/头部配合(前倾、抬头、拧腰), 收势有前推。</li>
 * </ol>
 * 数值都为"可调常量", 手感不对时只改 {@link #applyPose} 里的数字即可。
 */
public class SayaPlayerModel extends PlayerModel<TeaTreeBossEntity> {

    @Override
    public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack pose, com.mojang.blaze3d.vertex.VertexConsumer buffer,
                               int light, int overlay, float red, float green, float blue, float alpha) {
        if (ImportedBossActions.renderSaya(this, pose, buffer, light, overlay, red, green, blue, alpha)) return;
        super.renderToBuffer(pose, buffer, light, overlay, red, green, blue, alpha);
    }

    // ===== 施法风格 =====
    /** 双手前举蓄力(LONG 吟唱) —— 也是缺数据时的兜底 */
    public static final int STYLE_TWO_HAND = 0;
    /** 双手前推持续引导(CONTINUOUS) */
    public static final int STYLE_CHANNEL = 1;
    /** 单手前指瞬发(INSTANT 弹道) */
    public static final int STYLE_POINT = 2;
    /** 双手上举召唤(召唤/守护类) */
    public static final int STYLE_RAISE = 3;
    /** 收手护身(自施法/治疗/增益) */
    public static final int STYLE_EMBRACE = 4;
    /** 大范围仪式施法：双臂外展后向中心收束 */
    public static final int STYLE_RITUAL = 5;

    /** 抬手时长(tick): 0.3s */
    private static final float WINDUP_TICKS = 6.0F;
    /** 收势时长(tick): 0.4s */
    private static final float RELEASE_TICKS = 8.0F;

    private static final class CastPoseMemory {
        private boolean wasCasting;
        private float releaseAge = -1000;
        private int style = STYLE_TWO_HAND;
    }

    private final java.util.Map<TeaTreeBossEntity, CastPoseMemory> castPoses = new java.util.WeakHashMap<>();

    public SayaPlayerModel(ModelPart root) {
        super(root, true); // Alex 细手臂
    }

    @Override
    public void setupAnim(TeaTreeBossEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        if (ImportedBossActions.saya(this, entity, ageInTicks)) return;
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        var signature = entity.signatureCastAction();
        if (signature.active()) {
            float age = signature.elapsed(ageInTicks - entity.tickCount);
            float windup = Mth.clamp(age / 7.0f, 0, 1);
            float recovery = Mth.clamp((signature.duration() - age) / 8.0f, 0, 1);
            float weight = Math.min(windup, recovery);
            weight = weight * weight * (3 - 2 * weight);
            int style = "signature_self_cast".equals(signature.name()) ? STYLE_EMBRACE : STYLE_RITUAL;
            applyPose(style, ageInTicks, weight, windup, 1 - recovery);
            this.hat.copyFrom(this.head);
            this.jacket.copyFrom(this.body);
            this.leftSleeve.copyFrom(this.leftArm);
            this.rightSleeve.copyFrom(this.rightArm);
            this.leftPants.copyFrom(this.leftLeg);
            this.rightPants.copyFrom(this.rightLeg);
            return;
        }

        var nativeAction = entity.nativeAction();
        if (nativeAction.active()) {
            float age = nativeAction.elapsed(ageInTicks - entity.tickCount);
            boolean roots = "roots".equals(nativeAction.name());
            float release = roots ? 12 : ("melee".equals(nativeAction.name()) ? 6 : 8);
            float windup = Mth.clamp(age / release, 0, 1);
            float recovery = Mth.clamp((nativeAction.duration() - age) / 8.0f, 0, 1);
            applyPose(roots ? STYLE_RAISE : STYLE_POINT, ageInTicks, recovery, windup,
                    age >= release ? recovery : 0);
            this.hat.copyFrom(this.head);
            this.jacket.copyFrom(this.body);
            this.leftSleeve.copyFrom(this.leftArm);
            this.rightSleeve.copyFrom(this.rightArm);
            this.leftPants.copyFrom(this.leftLeg);
            this.rightPants.copyFrom(this.rightLeg);
            return;
        }

        if (!entity.isAlive()) return;
        CastPoseMemory memory = this.castPoses.computeIfAbsent(entity, ignored -> new CastPoseMemory());
        boolean casting = entity.isCasting();
        if (!casting && memory.wasCasting) memory.releaseAge = ageInTicks;
        memory.wasCasting = casting;
        float after = Mth.clamp(1 - (ageInTicks - memory.releaseAge) / RELEASE_TICKS, 0, 1);
        if (!casting && after <= 0) return;
        float phase = 1;
        if (casting) {
            memory.style = styleOf(entity);
            MagicData magic = entity.getMagicData();
            float elapsed = magic.getCastDuration() - magic.getCastDurationRemaining() + ageInTicks - entity.tickCount;
            phase = Mth.clamp(elapsed / WINDUP_TICKS, 0f, 1f);
        }
        float blend = casting ? phase * phase * (3 - 2 * phase) : after;
        applyPose(memory.style, ageInTicks, blend, phase, casting ? 0 : after);
        this.hat.copyFrom(this.head);
        this.jacket.copyFrom(this.body);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
    }

    /**
     * 应用某一风格的姿态。
     *
     * @param b     施法混合值(0..1)
     * @param phase 抬手进度(0=刚起手, 1=姿势到位)
     * @param after 收势余韵(1=刚结束, 0=已恢复)
     */
    private void applyPose(int style, float age, float b, float phase, float after) {
        // 起手时幅度先小后大; 收势时保留一部分
        float k = b * Mth.lerp(phase, 0.45f, 1.0f);
        float bob = Mth.sin(age * 0.18f) * 0.05f;      // 蓄力微抖
        float sway = Mth.sin(age * 0.08f) * 0.06f;     // 慢速摆动
        float pulse = Mth.sin(age * 0.35f) * 0.07f;    // 引导脉冲
        float push = after * 0.32f;                    // 收势前推

        // 默认(起身)值
        float aFwdR = 2.30f, aInR = 0.30f;   // 右臂: 前抬量 / 内收量
        float aFwdL = 2.30f, aInL = 0.30f;
        float bodyX = 0.07f, bodyY = 0f, headX = -0.18f;
        float legR = 0.06f, legL = -0.06f;

        switch (style) {
            case STYLE_CHANNEL -> {         // 持续引导: 双手前推, 有节奏地"输送"
                aFwdR = 1.62f + pulse; aInR = 0.14f;
                aFwdL = 1.62f - pulse; aInL = 0.14f;
                bodyX = 0.16f; headX = -0.06f;
                legR = 0.11f; legL = -0.11f;
            }
            case STYLE_POINT -> {           // 瞬发弹道: 右手前指, 左手压在后腰, 拧腰
                aFwdR = 1.58f + after * 0.22f; aInR = -0.06f;
                aFwdL = 0.42f; aInL = 0.14f;
                bodyX = 0.05f; bodyY = -0.22f; headX = -0.10f;
                legR = 0.13f; legL = -0.13f;
            }
            case STYLE_RAISE -> {           // 召唤/守护: 双手上举外张, 仰头
                aFwdR = 2.95f + bob; aInR = -0.55f;
                aFwdL = 2.95f - bob; aInL = -0.55f;
                bodyX = -0.09f; headX = -0.42f;
                legR = 0.02f; legL = -0.02f;
            }
            case STYLE_EMBRACE -> {         // 自施法/治疗: 收手护在胸前, 低头
                aFwdR = 1.10f + bob * 0.5f; aInR = 0.62f;
                aFwdL = 1.10f - bob * 0.5f; aInL = 0.62f;
                bodyX = 0.18f; headX = 0.24f;
                legR = 0.05f; legL = -0.05f;
            }
            case STYLE_RITUAL -> {          // 大范围仪式: 外展聚能 -> 双手向中央压合
                aFwdR = 2.72f + bob; aInR = -0.46f + sway;
                aFwdL = 2.72f - bob; aInL = -0.46f - sway;
                bodyX = -0.05f; bodyY = sway * 1.4f; headX = -0.34f;
                legR = 0.14f; legL = -0.14f;
            }
            default -> {                    // TWO_HAND: 双手前举蓄力(与旧版一致)
                aFwdR = 2.30f + bob + push; aInR = 0.30f + sway;
                aFwdL = 2.30f - bob - push; aInL = 0.30f + sway;
            }
        }
        if (style != STYLE_POINT) {         // 非指向型: 双手一起前推
            aFwdR += push; aFwdL += push;
        }
        aFwdR += bob * 0.6f;                // 所有风格保留一点天然颤动
        aFwdL -= bob * 0.6f;

        // ---- 手臂: xRot 负=向前抬起; 内收量对右臂取负 yRot/正 zRot, 左臂镜像 ----
        arm(this.rightArm, true, aFwdR, aInR + sway * 0.5f, k);
        arm(this.leftArm, false, aFwdL, aInL + sway * 0.5f, k);

        // ---- 身体/头/腿 ----
        this.body.xRot = Mth.lerp(k, this.body.xRot, bodyX + after * 0.10f);
        this.body.yRot = Mth.lerp(k, this.body.yRot, bodyY);
        this.head.xRot = Mth.lerp(k, this.head.xRot, headX + bob * 0.5f);
        this.rightLeg.xRot = Mth.lerp(k, this.rightLeg.xRot, legR);
        this.leftLeg.xRot = Mth.lerp(k, this.leftLeg.xRot, legL);
    }

    /**
     * 单臂姿态(从 super() 算出的走路摆臂平滑过渡到施法姿势, 收势时同理会平滑退回)。
     *
     * @param forward 前抬量(弧度, 1.57≈水平前伸, 2.3≈前上方)
     * @param inward  &gt;0 双手向身体中线靠拢, &lt;0 向外张开
     * @param k       混合权重(0=完全用走路姿势, 1=完全用施法姿势)
     */
    private void arm(ModelPart p, boolean right, float forward, float inward, float k) {
        float sign = right ? 1f : -1f;
        p.xRot = Mth.lerp(k, p.xRot, -forward);
        p.yRot = Mth.lerp(k, p.yRot, -sign * inward);
        p.zRot = Mth.lerp(k, p.zRot, sign * inward);
    }

    /**
     * 由客户端已同步的铁魔法数据推断施法风格。
     * 取不到数据(例如刚起手那一瞬)时回落到双手前举, 不会摆出怪姿势。
     */
    private static int styleOf(TeaTreeBossEntity entity) {
        try {
            MagicData md = entity.getMagicData();
            if (md == null) return STYLE_TWO_HAND;
            String id = md.getCastingSpellId();
            CastType type = md.getCastType();
            String low = id == null ? "" : id.toLowerCase(java.util.Locale.ROOT);
            boolean summonish = low.contains("summon") || low.contains("ward") || low.contains("raise")
                    || low.contains("bud") || low.contains("root") || low.contains("tree");
            boolean selfish = low.contains("heal") || low.contains("absorption") || low.contains("haste")
                    || low.contains("charge") || low.contains("shield") || low.contains("renew");
            boolean ritualish = low.contains("storm") || low.contains("vortex") || low.contains("barrage")
                    || low.contains("starfall") || low.contains("black_hole") || low.contains("wall")
                    || low.contains("sanctuary") || low.contains("procession") || low.contains("lattice");
            if (type == CastType.CONTINUOUS) return STYLE_CHANNEL;
            if (summonish) return STYLE_RAISE;
            if (selfish) return STYLE_EMBRACE;
            if (ritualish) return STYLE_RITUAL;
            if (type == CastType.LONG) return STYLE_TWO_HAND;
            return STYLE_POINT;
        } catch (Throwable ignored) {
            return STYLE_TWO_HAND;
        }
    }
}
