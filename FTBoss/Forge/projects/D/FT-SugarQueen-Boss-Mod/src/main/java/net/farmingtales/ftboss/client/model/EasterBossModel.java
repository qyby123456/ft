package net.farmingtales.ftboss.client.model;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.item.XuanlanMoonWeaponItem;
import net.farmingtales.ftboss.util.EasterCombatMath;
import net.farmingtales.ftboss.util.HiddenBossCastRules;
import net.farmingtales.ftboss.util.XuanlanGunPoseMath;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

/**
 * 【彩蛋 Boss 人形模型管线】(2026-09-12)
 *
 * <p>基底是**原版玩家模型**(Alex/Steve 两套), 因此天然兼容所有玩家动作:
 * 走路摆臂、跑动、潜行、游泳、受击、攻击挥臂 —— 全部由 {@code super.setupAnim(...)} 提供 ✓
 * 我们只在此基础上**叠加**自己的层:
 * <ol>
 *   <li><b>武器攻击</b>: 按攻击进度(0..1)覆盖右臂为"劈砍/突刺/横扫"三种招式;</li>
 *   <li><b>施法姿势</b>: 复刻沙耶那套五风格(双手蓄力/引导/单指/上举/护胸), 读法术类型切换;</li>
 *   <li><b>附加骨骼</b>: 呆毛(ahoge)/长发(hair)/披风(cape)/光环(halo) —— 用独立的
 *       {@link #LAYER} 图层烘焙, 采样玩家皮肤的**帽子覆盖层**(32,0~64,16), 所以只要皮肤在那块画了像素就会显示;</li>
 *   <li>附加骨骼带**物理感摆动**(随移动/俯仰/蓄力摆动)。</li>
 * </ol>
 *
 * <p>说明: 铁魔法的玩家施法动画由 playeranimator 驱动, 其 mixin 只作用于真正的玩家实体,
 * 自定义实体拿不到; 所以这里沿用本项目已验证的做法 —— **按 5 种施法风格手工复刻姿势**。
 */
public class EasterBossModel extends PlayerModel<EasterBossEntity> {

    /** 自定义图层(比原版玩家模型多了 4 根附加骨骼) */
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            new ResourceLocation(FTBossMod.MOD_ID, "easter_boss"), "main");

    /** 施法风格(与沙耶保持一致, 便于日后统一) */
    public static final int STYLE_MELEE = -1;
    public static final int STYLE_TWO_HAND = 0;
    public static final int STYLE_CHANNEL = 1;
    public static final int STYLE_POINT = 2;
    public static final int STYLE_RAISE = 3;
    public static final int STYLE_EMBRACE = 4;

    /* ========================================================================
     * 手持「噬影之刃·裂瞳」时右臂的目标姿态（唯一可调项，改完 build 即可）
     *
     * ★ 2026-09-15 四轮修正，把结论钉在这里（离线算过，见 tools/model3d/boss_arm_axis.cjs）：
     *
     *   手掌端点相对躯干中线的位置（格，负=偏左；静止时是 x=-0.375 z=-0.044）：
     *     原始 fwd=0.785 in=0.30 → x=-0.163  z=+0.528   ← 手伸到身前 0.53 格（看着"飘"）
     *     当前 fwd=0.120 yaw/roll → x=-0.163  z=+0.110   ← 手收回 0.42 格
     *
     *   ① "往外飘"的主因是 **FORWARD（xRot 前抬）**，不是左右：
     *      前抬 45° 会把手推到身前半格，2D 截图里就看成"飘出去"。
     *   ② **YAW 与 ROLL 必须解耦**：之前两者共用一个 inward 值，而它们对"内外"的作用
     *      方向相反，叠加后互相抵消还额外把手往下推 —— 这正是"加大内收反而更靠外"的原因。
     *   ③ 内外关系（右臂，离线算过，见 tools/model3d/boss_arm_axis.cjs）：
     *        FORWARD 正 = 手往前伸（z 增大）
     *        YAW / ROLL **必须成对给**：roll=0 时单独改 yaw 对手掌的 x 毫无影响
     *        （x 恒为 -0.375）—— 因为 Z 旋转已把手臂转进 x-y 平面，再绕 Y 转只是把它推进 z。
     *        成对给时（如 yaw=+0.15 / roll=-0.15）手会**往外**偏 0.11 格；
     *        要"手正好在肩下"就取 **yaw=0 / roll=0**。
     *
     * 当前取值 = 最保守基线：手正好留在肩的正下方（x=-0.375，与静止一致），
     *   只有 0.15 格的前伸（原值是 0.43 格）。要"举剑"的感觉就把 FORWARD 往上加。
     *
     * 换算成 ModelPart：xRot = -FORWARD, yRot = -YAW, zRot = ROLL
     *   （与 arm() 助手的 -sign*inward / sign*inward 一致，sign=+1 为右臂）
     *
     * 运行时覆盖（不必重新构建，改启动器 JVM 参数重启即可）：
     *   -Dftboss.swordPose.fwd=0.20  -Dftboss.swordPose.yaw=0  -Dftboss.swordPose.roll=0
     * ===================================================================== */
    private static final float SWORD_POSE_FORWARD = 0.20f;   // 前抬（原 0.785）
    private static final float SWORD_POSE_YAW = 0.0f;        // 0 = 手正好留在肩的正下方
    private static final float SWORD_POSE_ROLL = 0.0f;       // 0 = 完全不下垂以外地张开

    /**
     * ★ 2026-09-15 最终结论：手的位置**不该在这里调**。
     *
     * 用户反馈"手的位置还是不对，做成正常握着算了"之后，把物品 display 换成了工程里
     * **11 把武器共用**的那套标准值（item/shadow_sword.json）：
     *     thirdperson  rot [0,-90,55]  trans [0,4,0.5]   scale 0.85
     *     firstperson  rot [0,-90,25]  trans [1.13,3.2,1.13] scale 0.68
     * 原值是 rot z=28 / trans [1,3.5,0.2] / scale 0.78，其中 **trans.x 左右手同向**
     * （右手 +1、左手 -1，因左右手各自带镜像，屏幕上是同一个方向）—— 这是握点偏离手心的主因。
     *
     * 所以这里只保留"把手留在肩下"的保守姿态，**不要再靠手臂去补握点**：
     * 手臂改的是手在哪，display 改的才是剑在手里的位置，混着调永远收敛不了。
     */

    /**
     * 运行时覆盖：三个角度都能用系统属性在不重新构建的情况下改。
     * 为什么要有它：这几轮"往内/往外"靠截图判不准，每试一次却要 30 秒构建 + 重启。
     */
    private static float prop(String key, float fallback) {
        try {
            String v = System.getProperty(key);
            return v == null || v.isEmpty() ? fallback : Float.parseFloat(v.trim());
        } catch (NumberFormatException e) {
            FTBossMod.LOGGER.warn("[FTBoss][剑姿态] 系统属性 {} 不是数字，用默认值 {}", key, fallback);
            return fallback;
        }
    }

    private static final float POSE_FWD = prop("ftboss.swordPose.fwd", SWORD_POSE_FORWARD);
    private static final float POSE_YAW = prop("ftboss.swordPose.yaw", SWORD_POSE_YAW);
    private static final float POSE_ROLL = prop("ftboss.swordPose.roll", SWORD_POSE_ROLL);

    /** 只在第一次套用时打一行日志，方便确认参数到底吃进去没有。 */
    private static boolean poseLogged = false;

    private final ModelPart ahoge;
    private final ModelPart hair;
    private final ModelPart cape;
    private final ModelPart halo;

    public EasterBossModel(ModelPart root) {
        super(root, true);   // 2026-09-12: 细臂(Alex), 与图层注册保持一致
        /* 防御式取附加骨骼: 万一图层没烘焙出来(或别处改了图层名), 也用空部件兜底,
           绝不让渲染器在构造期抛异常 —— 否则会被 Lightspeed 换成空渲染器, 表现就是"实体完全看不见"。 */
        this.ahoge = childOrEmpty(root, "easter_ahoge");
        this.hair = childOrEmpty(root, "easter_hair");
        this.cape = childOrEmpty(root, "easter_cape");
        this.halo = childOrEmpty(root, "easter_halo");
    }

    /** 取子部件, 取不到就给一个空部件(不可见) */
    private static ModelPart childOrEmpty(ModelPart root, String name) {
        try {
            return root.hasChild(name) ? root.getChild(name) : new ModelPart(java.util.List.of(), java.util.Map.of());
        } catch (Throwable t) {
            return new ModelPart(java.util.List.of(), java.util.Map.of());
        }
    }

    /**
     * 图层定义: 复制原版玩家网格 + 追加 4 根附加骨骼。
     * 附加骨骼统一采样**帽子覆盖层**(纹理 32,0~64,16 那块), 皮肤画了就有, 没画就是透明。
     */
    public static LayerDefinition createBodyLayer(boolean slim) {
        MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, slim);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        PartDefinition body = root.getChild("body");

        /* 2026-09-12 按用户反馈: **不再追加任何附加骨骼** —— 之前 4 根(呆毛/长发/披风/光环)会采样皮肤的
           帽子层像素, 在这些自定义皮肤上表现为"多出来的贴图块"。现在图层 = 纯原版玩家网格, 只有玩家本体。 */
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(EasterBossEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.resetPose();
        this.body.resetPose();
        this.rightArm.resetPose();
        this.leftArm.resetPose();
        this.rightLeg.resetPose();
        this.leftLeg.resetPose();
        /* ① 原版玩家动作(走路/跑/潜行/游泳/受击) —— 完全交给父类, 这就是"兼容玩家动作" */
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        net.farmingtales.ftboss.client.manorrun.RunAnimator.prepare(this, entity, limbSwing, limbSwingAmount, ageInTicks);
        float partialTick = Mth.clamp(ageInTicks - entity.tickCount, 0.0f, 1.0f);
        float weight = entity.getCombatAnimationWeight(partialTick);
        boolean casting = entity.isCastingLike();
        float actionBlend = casting ? 0.0f : weight;
        float castBlend = casting ? weight : 0.0f;

        /* ② 施法姿势(优先级高于武器动作) */
        if (entity.getVariant() == EasterBossEntity.QIZAI_VARIANT) {
            boolean prayer = entity.getSkillCue().getInt("qizai") == net.farmingtales.ftboss.util.QizaiRules.PRAYER;
            this.rightArm.xRot = Mth.lerp(castBlend, -0.18F, prayer ? -1.1F : -1.7F);
            this.rightArm.zRot = Mth.lerp(castBlend, 0.08F, prayer ? 0.35F : 0.1F);
            this.leftArm.xRot = Mth.lerp(castBlend, this.leftArm.xRot, -1.05F);
            this.leftArm.yRot = castBlend * (prayer ? 0.45F : -0.25F);
            this.leftArm.zRot = -castBlend * 0.3F;
        }
        else if (entity.getVariant() == 6 && entity.getMainHandItem().is(FTBossItems.WENXUANLAN_FLYWHEEL.get())
                && !XuanlanMoonWeaponItem.isWhite(entity.getMainHandItem())) {
            applyRangedPose(entity, partialTick, netHeadYaw, headPitch);
        }
        else if (castBlend > 0f) applyCastPose(entity, entity.getCastStyle(), ageInTicks, castBlend);
        /* ③ 武器招式: 劈砍 / 突刺 / 横扫 */
        else if (actionBlend > 0f) {
            applyWeaponPose(entity.getWeaponStyle(), entity.getActionProgress(partialTick), actionBlend);
        }

        if (entity.getVariant() == EasterBossEntity.DORA_VARIANT) {
            this.applyDoraDualPose(entity, partialTick, ageInTicks, actionBlend);
        }

        if (entity.getVariant() == 3 && weight > 0.0f) {
            var cue = entity.getSkillCue();
            if (cue.contains("gongdiSkill")) {
                float progress = EasterCombatMath.clamp((entity.level().getGameTime() - cue.getLong("start") + partialTick)
                        / Math.max(1, cue.getInt("duration")));
                float gongdiWeight = cue.getBoolean("released") ? weight
                        : EasterCombatMath.smooth((entity.getCombatAnimationAge(partialTick) + 1.0f) / HiddenBossCastRules.POSE_BLEND_TICKS);
                applyGongdiPose(cue.getString("gongdiSkill"), cue.getBoolean("released"), progress, gongdiWeight);
            }
        }

        /* ④ 附加骨骼摆动: 移动越快/俯仰越大, 摇得越厉害(带一点延迟感) */
        float sway = Mth.sin(ageInTicks * 0.12f) * 0.18f;
        float swing = Mth.sin(limbSwing * 0.6662F) * limbSwingAmount * 0.5f;
        this.ahoge.xRot = -0.15f + sway * 0.9f + swing * 0.5f;
        this.ahoge.zRot = Mth.sin(ageInTicks * 0.21f) * 0.22f;
        this.hair.xRot = 0.05f + swing * 0.35f + actionBlend * 0.25f;
        this.cape.xRot = 0.06f + swing * 0.8f - actionBlend * 0.35f + castBlend * 0.15f;
        this.halo.yRot = ageInTicks * 0.03f;
        this.halo.y = -12.5f + Mth.sin(ageInTicks * 0.1f) * 0.35f;

        this.hat.visible = true;
        this.jacket.visible = true;
        this.leftSleeve.visible = true;
        this.rightSleeve.visible = true;
        this.leftPants.visible = true;
        this.rightPants.visible = true;
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);

        /* 附加骨骼的显隐由变体决定(皮肤上没画也不影响主体) */
        boolean[] f = entity.getExtraBones();
        this.ahoge.visible = f[0];
        this.hair.visible = f[1];
        this.cape.visible = f[2];
        this.halo.visible = f[3];
        net.farmingtales.ftboss.client.manorrun.RunAnimator.apply(this, entity, ageInTicks);
        net.farmingtales.ftboss.client.manorrun.ManorRunAnimation.apply(this, entity, ageInTicks);
    }

    private void applyGongdiPose(String skill, boolean released, float progress, float weight) {
        float wind = released ? 1.0f : EasterCombatMath.smooth(progress * 1.6f);
        float strike = released ? 1.0f + 0.12f * Mth.sin(progress * Mth.PI)
                : EasterCombatMath.smooth((progress - 0.82f) / 0.18f);
        if (skill.equals("decimate")) {
            this.rightArm.xRot = Mth.lerp(weight, this.rightArm.xRot, -0.9f - 0.4f * wind);
            this.rightArm.yRot = Mth.lerp(weight, this.rightArm.yRot, -1.1f * wind + 2.0f * strike);
            this.rightArm.zRot = Mth.lerp(weight, this.rightArm.zRot, -0.35f);
            this.leftArm.xRot = Mth.lerp(weight, this.leftArm.xRot, -0.8f);
            this.body.yRot = Mth.lerp(weight, this.body.yRot, -0.35f * wind + 0.8f * strike);
        } else if (skill.equals("apprehend")) {
            this.rightArm.xRot = Mth.lerp(weight, this.rightArm.xRot, -1.55f * wind + 1.0f * strike);
            this.rightArm.yRot = Mth.lerp(weight, this.rightArm.yRot, -0.35f + 0.8f * strike);
            this.leftArm.xRot = Mth.lerp(weight, this.leftArm.xRot, -1.1f * wind + 0.8f * strike);
            this.body.xRot = Mth.lerp(weight, this.body.xRot, 0.12f * wind - 0.2f * strike);
        } else {
            float height = skill.equals("guillotine") ? 2.9f : 2.2f;
            this.rightArm.xRot = Mth.lerp(weight, this.rightArm.xRot, -height * wind + (height - 0.45f) * strike);
            this.rightArm.yRot = Mth.lerp(weight, this.rightArm.yRot, 0.18f);
            this.leftArm.xRot = Mth.lerp(weight, this.leftArm.xRot, -1.7f * wind + 1.1f * strike);
            this.body.xRot = Mth.lerp(weight, this.body.xRot, -0.12f * wind + 0.3f * strike);
        }
        this.rightLeg.xRot = Mth.lerp(weight, this.rightLeg.xRot, -0.18f);
        this.leftLeg.xRot = Mth.lerp(weight, this.leftLeg.xRot, 0.18f);
    }

    /** 武器招式: 0=劈砍 1=突刺 2=横扫 (p = 动作进度 0..1) */
    private void applyWeaponPose(int style, float progress, float blend) {
        float wind = EasterCombatMath.smooth(progress / 0.35f);
        float strike = EasterCombatMath.smooth((progress - 0.35f) / 0.2f);
        float back = 1f - strike;
        float strength = blend * (0.5f + wind * 0.5f);
        switch (style) {
            case 1 -> {   // 突刺: 右臂前推, 身体前倾
                arm(this.rightArm, true, 0.5f + strike * 1.2f, 0.15f, strength);
                arm(this.leftArm, false, 0.35f, 0.2f, strength * 0.7f);
                this.body.xRot = Mth.lerp(strength, this.body.xRot, 0.12f + strike * 0.12f);
            }
            case 2 -> {   // 横扫: 右臂横向挥过
                arm(this.rightArm, true, 1.45f, -0.65f + strike * 1.1f, strength);
                arm(this.leftArm, false, 0.6f, 0.4f - strike * 0.4f, strength * 0.6f);
                this.body.yRot = Mth.lerp(strength, this.body.yRot, -0.18f + strike * 0.36f);
            }
            default -> {  // 劈砍: 高举 -> 下劈
                arm(this.rightArm, true, Mth.lerp(strike, 2.5f * wind + 0.4f, 0.65f), 0.2f, strength);
                arm(this.leftArm, false, 0.9f * wind - 0.45f * strike, 0.25f, strength * 0.6f);
                this.body.xRot = Mth.lerp(strength, this.body.xRot, -0.09f * back + 0.18f * strike);
                this.head.xRot = Mth.lerp(strength, this.head.xRot, 0.10f * strike);
            }
        }
        /* 双腿站定: 出手时前后分开 */
        this.rightLeg.xRot = Mth.lerp(strength, this.rightLeg.xRot, 0.18f * strike - 0.1f * wind);
        this.leftLeg.xRot = Mth.lerp(strength, this.leftLeg.xRot, -0.18f * strike + 0.1f * wind);
    }

    private void applyRangedPose(EasterBossEntity entity, float partialTick, float netHeadYaw, float headPitch) {
        float aimPitch = headPitch * Mth.DEG_TO_RAD;
        float aimYaw = netHeadYaw * Mth.DEG_TO_RAD;
        var cue = entity.getSkillCue();
        float cueAge = entity.level().getGameTime() - cue.getLong("start") + partialTick;
        boolean beam = cue.getBoolean("beam") && XuanlanGunPoseMath.activeCue(cueAge, cue.getInt("duration"));
        if (beam) {
            double directionX = cue.getDouble("endX") - cue.getDouble("x");
            double directionY = cue.getDouble("endY") - cue.getDouble("y");
            double directionZ = cue.getDouble("endZ") - cue.getDouble("z");
            if (directionX * directionX + directionY * directionY + directionZ * directionZ > 1.0E-6) {
                aimPitch = XuanlanGunPoseMath.beamPitch(directionX, directionY, directionZ);
                aimYaw = XuanlanGunPoseMath.beamYaw(directionX, directionZ,
                        Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot));
            }
        }
        boolean aiming = entity.isAlive() && (beam || entity.isCastingLike() || entity.getActionTicks() > 0);
        boolean rightHanded = entity.getMainArm() == HumanoidArm.RIGHT;
        ModelPart gunArm = rightHanded ? this.rightArm : this.leftArm;
        ModelPart freeArm = rightHanded ? this.leftArm : this.rightArm;
        this.rightArm.resetPose();
        this.leftArm.resetPose();
        gunArm.xRot = aiming ? XuanlanGunPoseMath.armPitch(aimPitch) : XuanlanGunPoseMath.REST_ARM_PITCH;
        gunArm.yRot = aiming ? aimYaw : 0.0f;
        gunArm.zRot = 0.0f;
        freeArm.xRot = aiming ? -0.65f : -0.15f;
        freeArm.yRot = rightHanded ? 0.25f : -0.25f;
        freeArm.zRot = 0.0f;
        this.body.xRot = 0.0f;
        this.body.yRot = 0.0f;
        this.head.xRot = aimPitch;
        this.head.yRot = aimYaw;
    }

    private void applyDoraDualPose(EasterBossEntity entity, float partialTick, float age, float actionBlend) {
        var cue = entity.getSkillCue();
        float cueAge = entity.level().getGameTime() - cue.getLong("start") + partialTick;
        if (cue.getBoolean("doraSniper") && cueAge >= 0 && cueAge < cue.getInt("duration")) {
            this.applyDoraSniperPose(cue.getBoolean("firing"), Mth.clamp(cueAge / cue.getInt("duration"), 0.0F, 1.0F));
            float thrust = Mth.clamp((entity.getDoraSpearTicks() - partialTick) / 4.0F, 0.0F, 1.0F);
            this.leftArm.xRot = Mth.lerp(thrust, -0.20F, -1.70F);
            this.leftArm.yRot = -0.18F + this.head.yRot * thrust;
            return;
        }
        boolean reloading = entity.isDoraReloading();
        boolean shooting = !reloading && entity.getActionTicks() > 0;
        float recoil = shooting ? Mth.sin(age * 2.1f) * 0.055f : 0.0f;
        float thrust = Mth.clamp((entity.getDoraSpearTicks() - partialTick) / 4.0f, 0.0f, 1.0f);
        this.rightArm.xRot = shooting ? -1.48f + this.head.xRot + recoil : reloading ? -0.8f : -0.35f;
        this.rightArm.yRot = shooting ? this.head.yRot - 0.08f : -0.10f;
        this.rightArm.zRot = 0.03f;
        this.leftArm.xRot = Mth.lerp(thrust, reloading ? -0.95f : -0.45f, -1.70f);
        this.leftArm.yRot = -0.18f + this.head.yRot * thrust;
        this.leftArm.zRot = -0.10f;
        this.body.xRot = reloading ? 0.16f : 0.06f * actionBlend + 0.10f * thrust;
        this.body.yRot = 0.0f;
    }

    private void applyDoraSniperPose(boolean fired, float progress) {
        float recoil = fired ? Mth.sin(progress * (float) Math.PI) * 0.28F : 0.0F;
        this.rightArm.xRot = -1.52F + this.head.xRot - recoil;
        this.rightArm.yRot = this.head.yRot - 0.11F;
        this.rightArm.zRot = 0.045F;
        this.leftArm.xRot = -0.20F;
        this.leftArm.yRot = -0.16F;
        this.leftArm.zRot = -0.10F;
        this.body.xRot = 0.04F - recoil * 0.15F;
        this.body.yRot = -0.06F;
        this.rightLeg.xRot = 0.07F;
        this.leftLeg.xRot = -0.12F;
    }

    /**
     * 施法姿势(与沙耶的五风格一致, 数值略调紧以适应 Boss 体型)。
     *
     * @param entity 需要它来判断"主手是不是暗影剑"（那一枝有专属举剑姿态）
     */
    private void applyCastPose(EasterBossEntity entity, int style, float age, float b) {
        float bob = Mth.sin(age * 0.18f) * 0.05f;
        float sway = Mth.sin(age * 0.08f) * 0.06f;
        float pulse = Mth.sin(age * 0.35f) * 0.07f;
        float k = b;
        float aFwdR = 2.30f, aInR = 0.30f, aFwdL = 2.30f, aInL = 0.30f;
        float bodyX = 0.07f, headX = -0.18f, legR = 0.06f, legL = -0.06f;
        switch (style) {
            case STYLE_CHANNEL -> {
                aFwdR = 1.62f + pulse; aInR = 0.14f;
                aFwdL = 1.62f - pulse; aInL = 0.14f;
                bodyX = 0.16f; headX = -0.06f; legR = 0.11f; legL = -0.11f;
            }
            case STYLE_POINT -> {
                aFwdR = 1.58f; aInR = -0.06f;
                aFwdL = 0.42f; aInL = 0.14f;
                bodyX = 0.05f; headX = -0.10f; legR = 0.13f; legL = -0.13f;
            }
            case STYLE_RAISE -> {
                aFwdR = 2.95f + bob; aInR = -0.55f;
                aFwdL = 2.95f - bob; aInL = -0.55f;
                bodyX = -0.09f; headX = -0.42f; legR = 0.02f; legL = -0.02f;
            }
            case STYLE_EMBRACE -> {
                aFwdR = 1.10f + bob * 0.5f; aInR = 0.62f;
                aFwdL = 1.10f - bob * 0.5f; aInL = 0.62f;
                bodyX = 0.18f; headX = 0.24f; legR = 0.05f; legL = -0.05f;
            }
            default -> {
                aFwdR = 2.30f + bob; aInR = 0.30f + sway;
                aFwdL = 2.30f - bob; aInL = 0.30f + sway;
            }
        }
        arm(this.rightArm, true, aFwdR, aInR + sway * 0.5f, k);
        arm(this.leftArm, false, aFwdL, aInL + sway * 0.5f, k);
        switch (entity.getVariant()) {
            case 3 -> {
                arm(this.rightArm, true, 2.65f + bob, 0.12f, k);
                arm(this.leftArm, false, 0.75f, 0.15f, k);
            }
            case 4 -> {
                arm(this.rightArm, true, 0.85f + bob, 0.12f, k);
                arm(this.leftArm, false, 1.25f - bob, 0.18f, k);
            }
            case 8 -> {
                arm(this.rightArm, true, 1.1f + pulse, -0.18f, k);
                arm(this.leftArm, false, 0.6f - pulse, -0.15f, k);
            }
            case 9 -> {
                arm(this.rightArm, true, 2.15f + bob, 0.08f, k);
                arm(this.leftArm, false, 0.8f, 0.35f, k);
            }
            default -> {}
        }
        /* 2026-09-15 用户要求: 手持「噬影之刃·裂瞳」的 Boss（折耳根）抬臂举剑。
           用"手里拿的是不是这把剑"判定, 不依赖 variant 号 -> 只影响折耳根这一位。

           ★ 2026-09-15 二次修正（用户实机反馈"剑往外飘 + 角度不对"）：
             根因不是物品 display，而是这里的手臂姿态。原值是 xRot=-0.785(前上 45°)
             + 向外 0.30，而刃相对手臂只有 28°，两者叠加 -> 剑变成"横着往外戳"，
             手也被抬到身前外侧（看着像剑飘在手外面）。
             举剑时**前臂本来就该基本下垂**，剑的立/斜由 display 的 rot.z 决定；
             手臂再前抬 45° 只会把剑推成水平。
             所以把"前抬"降到很小、横向也收窄，让刃回到接近竖直的观感。
             下面三个常量就是全部可调项，改完 `gradle build` 进游戏看效果。 */
        if (entity.getMainHandItem().is(net.farmingtales.ftboss.item.FTBossItems.SHADOW_SWORD.get())) {
            /* 换算：xRot = -FORWARD, yRot = -YAW, zRot = ROLL（右臂 sign=+1） */
            this.rightArm.xRot = Mth.lerp(k, this.rightArm.xRot, -POSE_FWD);
            this.rightArm.yRot = Mth.lerp(k, this.rightArm.yRot, -POSE_YAW);
            this.rightArm.zRot = Mth.lerp(k, this.rightArm.zRot, POSE_ROLL);
            this.leftArm.xRot = Mth.lerp(k, this.leftArm.xRot, -0.15f);

            if (!poseLogged) {
                poseLogged = true;
                FTBossMod.LOGGER.info(
                        "[FTBoss][剑姿态] 生效 fwd={} yaw={} roll={}（默认 {}/{}/{}；" +
                        "可用 -Dftboss.swordPose.fwd/yaw/roll 覆盖）",
                        POSE_FWD, POSE_YAW, POSE_ROLL,
                        SWORD_POSE_FORWARD, SWORD_POSE_YAW, SWORD_POSE_ROLL);
            }
        }
        this.body.xRot = Mth.lerp(k, this.body.xRot, bodyX);
        this.head.xRot = Mth.lerp(k, this.head.xRot, headX + bob * 0.5f);
        this.rightLeg.xRot = Mth.lerp(k, this.rightLeg.xRot, legR);
        this.leftLeg.xRot = Mth.lerp(k, this.leftLeg.xRot, legL);
    }

    /** 单臂: 从父类算好的玩家动作平滑过渡到目标姿势 */
    private void arm(ModelPart p, boolean right, float forward, float inward, float k) {
        float sign = right ? 1f : -1f;
        p.xRot = Mth.lerp(k, p.xRot, -forward);
        p.yRot = Mth.lerp(k, p.yRot, -sign * inward);
        p.zRot = Mth.lerp(k, p.zRot, sign * inward);
    }
}
