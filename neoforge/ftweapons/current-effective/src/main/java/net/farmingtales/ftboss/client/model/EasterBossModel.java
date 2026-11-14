package net.farmingtales.ftboss.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.client.dorabasic.DoraBasicAnimation;
import net.farmingtales.ftboss.client.manorrun.ManorRunAnimation;
import net.farmingtales.ftboss.client.maque.MaqueHumanoidAnimations;
import net.farmingtales.ftboss.client.qingyicast.QingyiCastAnimation;
import net.farmingtales.ftboss.client.qizaiaction.QizaiActionClient;
import net.farmingtales.ftboss.client.render.GongdiHumanoidRenderer;
import net.farmingtales.ftboss.client.xuanlanbody.BodyAnimator;
import net.farmingtales.ftboss.client.xueli_basic.BasicPose;
import net.farmingtales.ftboss.client.xueli_cast_actions.CastPose;
import net.farmingtales.ftboss.client.xueli_run.RunPose;
import net.farmingtales.ftboss.client.zheergenmotion.ShadowSwordAnimation;
import net.farmingtales.ftboss.client.zicaicast.ZicaiCastAnimation;
import net.farmingtales.ftboss.client.zicaimelee.ZicaiHumanoidAnimation;
import net.farmingtales.ftboss.dandanironmotion.client.IronActor;
import net.farmingtales.ftboss.dandanmotion.client.MotionActor;
import net.farmingtales.ftboss.dandanrun.client.RunActor;
import net.farmingtales.ftboss.doramotion.client.DoraAnimator;
import net.farmingtales.ftboss.lemoncasting.client.LemonCastingActor;
import net.farmingtales.ftboss.lemonmotion.client.LemonMotionActor;
import net.farmingtales.ftboss.manorrun.RunAnimator;
import net.farmingtales.ftboss.youzi.motion.YouziCharacterAdapter;
import net.farmingtales.ftboss.zheergenbindingbody.client.BindingAnimator;
import net.farmingtales.ftboss.zheergennightfall.client.NightfallAnimator;
import net.farmingtales.ftboss.zheergenremainingbody.client.SkillBodyAnimator;
import net.farmingtales.ftboss.zheergenvoidbody.client.VoidAnimator;
import net.minecraft.world.item.Item;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.item.XuanlanMoonWeaponItem;
import net.farmingtales.ftboss.util.EasterCombatMath;
import net.farmingtales.ftboss.util.XuanlanGunPoseMath;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

public class EasterBossModel extends PlayerModel<EasterBossEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("ftboss", "easter_boss"), "main");
    public static final int STYLE_MELEE = -1;
    public static final int STYLE_TWO_HAND = 0;
    public static final int STYLE_CHANNEL = 1;
    public static final int STYLE_POINT = 2;
    public static final int STYLE_RAISE = 3;
    public static final int STYLE_EMBRACE = 4;
    private static final float SWORD_POSE_FORWARD = 0.2F;
    private static final float SWORD_POSE_YAW = 0.0F;
    private static final float SWORD_POSE_ROLL = 0.0F;
    private static final float POSE_FWD = prop("ftboss.swordPose.fwd", 0.2F);
    private static final float POSE_YAW = prop("ftboss.swordPose.yaw", 0.0F);
    private static final float POSE_ROLL = prop("ftboss.swordPose.roll", 0.0F);
    private static boolean poseLogged = false;
    private final ModelPart ahoge;
    private final ModelPart hair;
    private final ModelPart cape;
    private final ModelPart halo;

    private static float prop(String var0, float var1) {
        try {
            String var2 = System.getProperty(var0);
            return var2 != null && !var2.isEmpty() ? Float.parseFloat(var2.trim()) : var1;
        } catch (NumberFormatException var3) {
            FTBossMod.LOGGER.warn("[FTBoss][剑姿态] 系统属性 {} 不是数字，用默认值 {}", var0, var1);
            return var1;
        }
    }

    public EasterBossModel(ModelPart var1) {
        super(var1, true);
        this.ahoge = childOrEmpty(var1, "easter_ahoge");
        this.hair = childOrEmpty(var1, "easter_hair");
        this.cape = childOrEmpty(var1, "easter_cape");
        this.halo = childOrEmpty(var1, "easter_halo");
    }

    private static ModelPart childOrEmpty(ModelPart var0, String var1) {
        try {
            return var0.hasChild(var1) ? var0.getChild(var1) : new ModelPart(List.of(), Map.of());
        } catch (Throwable var3) {
            return new ModelPart(List.of(), Map.of());
        }
    }

    public static LayerDefinition createBodyLayer(boolean var0) {
        MeshDefinition var1 = PlayerModel.createMesh(CubeDeformation.NONE, var0);
        PartDefinition var2 = var1.getRoot();
        PartDefinition var3 = var2.getChild("head");
        PartDefinition var4 = var2.getChild("body");
        return LayerDefinition.create(var1, 64, 64);
    }

    public void setupAnim(EasterBossEntity var1, float var2, float var3, float var4, float var5, float var6) {
        this.attackTime = ftboss$basicBodySwing(this.attackTime, var1);
        net.farmingtales.ftboss.manorrun.RunAnimator.begin(var1);
        this.head.resetPose();
        this.body.resetPose();
        this.rightArm.resetPose();
        this.leftArm.resetPose();
        this.rightLeg.resetPose();
        this.leftLeg.resetPose();
        super.setupAnim(var1, var2, var3, var4, var5, var6);
        net.farmingtales.ftboss.client.manorrun.RunAnimator.prepare(this, var1, var2, var3, var4);
        net.farmingtales.ftboss.lemoncasting.client.LemonCastingActor.capture(this, var1);
        net.farmingtales.ftboss.lemonmotion.client.LemonMotionActor.capture(this, var1);
        float var7 = Mth.clamp(var4 - (float)var1.tickCount, 0.0F, 1.0F);
        float var8 = var1.getCombatAnimationWeight(var7);
        boolean var9 = var1.isCastingLike();
        float var10 = var9 ? 0.0F : var8;
        float var11 = var9 ? var8 : 0.0F;
        if (var1.getVariant() == 11) {
            boolean var12 = var1.getSkillCue().getInt("qizai") == 3;
            this.rightArm.xRot = Mth.lerp(var11, -0.18F, var12 ? -1.1F : -1.7F);
            this.rightArm.zRot = Mth.lerp(var11, 0.08F, var12 ? 0.35F : 0.1F);
            this.leftArm.xRot = Mth.lerp(var11, this.leftArm.xRot, -1.05F);
            this.leftArm.yRot = var11 * (var12 ? 0.45F : -0.25F);
            this.leftArm.zRot = -var11 * 0.3F;
        } else if (var1.getVariant() == 6
            && var1.getMainHandItem().is(FTBossItems.WENXUANLAN_FLYWHEEL.get())
            && !XuanlanMoonWeaponItem.isWhite(var1.getMainHandItem())) {
            this.applyRangedPose(var1, var7, var5, var6);
        } else if (var11 > 0.0F) {
            this.applyCastPose(var1, var1.getCastStyle(), var4, CastPose.filterGeneric(var11, var1));
        } else if (var10 > 0.0F) {
            this.applyWeaponPose(var1.getWeaponStyle(), var1.getActionProgress(var7), BasicPose.filterBlend(net.farmingtales.ftboss.client.qingyi_basic.BasicPose.filterGeneric(CastPose.filterGeneric(var10, var1), var1), var1));
        }

        if (var1.getVariant() == 2) {
            this.applyDoraDualPose(var1, var7, var4, var10);
        }

        if (var1.getVariant() == 3 && var8 > 0.0F) {
            CompoundTag var15 = var1.getSkillCue();
            if (var15.contains("gongdiSkill")) {
                float var13 = EasterCombatMath.clamp(
                    ((float)(var1.level().getGameTime() - var15.getLong("start")) + var7) / (float)Math.max(1, var15.getInt("duration"))
                );
                float var14 = var15.getBoolean("released") ? var8 : EasterCombatMath.smooth((var1.getCombatAnimationAge(var7) + 1.0F) / 2.0F);
                this.applyGongdiPose(var15.getString("gongdiSkill"), var15.getBoolean("released"), var13, var14);
            }
        }

        float var16 = Mth.sin(var4 * 0.12F) * 0.18F;
        float var17 = Mth.sin(var2 * 0.6662F) * var3 * 0.5F;
        this.ahoge.xRot = -0.15F + var16 * 0.9F + var17 * 0.5F;
        this.ahoge.zRot = Mth.sin(var4 * 0.21F) * 0.22F;
        this.hair.xRot = 0.05F + var17 * 0.35F + var10 * 0.25F;
        this.cape.xRot = 0.06F + var17 * 0.8F - var10 * 0.35F + var11 * 0.15F;
        this.halo.yRot = var4 * 0.03F;
        this.halo.y = -12.5F + Mth.sin(var4 * 0.1F) * 0.35F;
        this.hat.visible = false;
        this.jacket.visible = false;
        this.leftSleeve.visible = false;
        this.rightSleeve.visible = false;
        this.leftPants.visible = false;
        this.rightPants.visible = false;
        if (var1.getVariant() == 11) {
            this.hat.visible = this.jacket.visible = this.leftSleeve.visible = this.rightSleeve.visible = true;
            this.leftPants.visible = this.rightPants.visible = true;
            this.leftSleeve.copyFrom(this.leftArm);
            this.rightSleeve.copyFrom(this.rightArm);
        }

        boolean[] var18 = var1.getExtraBones();
        this.ahoge.visible = var18[0];
        this.hair.visible = var18[1];
        this.cape.visible = var18[2];
        this.halo.visible = var18[3];
        ZicaiHumanoidAnimation.apply(this, var1, var2, var3, var4, var5, var6);
        ZicaiCastAnimation.apply(this, var1, var2, var3, var4, var5, var6);
        LemonMotionActor.animate(this, var1, var2, var3, var4, var5, var6, this.hair, this.cape, this.ahoge);
        LemonCastingActor.animate(this, var1, var2, var3, var4, var5, var6, this.hair, this.cape, this.ahoge);
        BodyAnimator.apply(this, var1, var2, var3, var4, var5, var6);
        MotionActor.animate(this, var1, var4);
        DoraAnimator.apply(this, var1, var2, var3, var4, var5, var6);
        MaqueHumanoidAnimations.apply(this, var1, var2, var3, var4, var5, var6);
        DoraBasicAnimation.apply(this, var1, var2, var3, var4, var5, var6);
        ShadowSwordAnimation.apply(this, var1, var2, var3, var4, var5, var6);
        NightfallAnimator.apply(this, var1, var2, var3, var4, var5, var6);
        BindingAnimator.apply(this, var1, var2, var3, var4, var5, var6);
        VoidAnimator.apply(this, var1, var2, var3, var4, var5, var6);
        SkillBodyAnimator.apply(this, var1, var2, var3, var4, var5, var6);
        QizaiActionClient.apply(this, var1, var2, var3, var4, var5, var6);
        QingyiCastAnimation.apply(this, var1, var2, var3, var4, var5, var6);
        net.farmingtales.ftboss.client.qingyi_basic.BasicPose.apply(this, var1, var4, var3);
        IronActor.animate(this, var1, var4);
        GongdiHumanoidRenderer.setup(this, var1, var2, var3, var4, var5, var6);
        RunPose.apply(this, var1, var2, var3, var4);
        BasicPose.apply(this, var1, var4, var3);
        CastPose.apply(this, var1, var4, var3);
        YouziCharacterAdapter.apply(this, var1, var2, var3, var4, var5, var6);
        RunAnimator.generic(this, var1, var4);
        RunActor.animate(this, var1, var4);
        RunAnimator.finish(this, var1, var2, var3, var4, var5, var6);
        ManorRunAnimation.apply(this, var1, var4);
    }

    private void applyGongdiPose(String var1, boolean var2, float var3, float var4) {
        float var5 = var2 ? 1.0F : EasterCombatMath.smooth(var3 * 1.6F);
        float var6 = var2 ? 1.0F + 0.12F * Mth.sin(var3 * (float) Math.PI) : EasterCombatMath.smooth((var3 - 0.82F) / 0.18F);
        if (var1.equals("decimate")) {
            this.rightArm.xRot = Mth.lerp(var4, this.rightArm.xRot, -0.9F - 0.4F * var5);
            this.rightArm.yRot = Mth.lerp(var4, this.rightArm.yRot, -1.1F * var5 + 2.0F * var6);
            this.rightArm.zRot = Mth.lerp(var4, this.rightArm.zRot, -0.35F);
            this.leftArm.xRot = Mth.lerp(var4, this.leftArm.xRot, -0.8F);
            this.body.yRot = Mth.lerp(var4, this.body.yRot, -0.35F * var5 + 0.8F * var6);
        } else if (var1.equals("apprehend")) {
            this.rightArm.xRot = Mth.lerp(var4, this.rightArm.xRot, -1.55F * var5 + 1.0F * var6);
            this.rightArm.yRot = Mth.lerp(var4, this.rightArm.yRot, -0.35F + 0.8F * var6);
            this.leftArm.xRot = Mth.lerp(var4, this.leftArm.xRot, -1.1F * var5 + 0.8F * var6);
            this.body.xRot = Mth.lerp(var4, this.body.xRot, 0.12F * var5 - 0.2F * var6);
        } else {
            float var7 = var1.equals("guillotine") ? 2.9F : 2.2F;
            this.rightArm.xRot = Mth.lerp(var4, this.rightArm.xRot, -var7 * var5 + (var7 - 0.45F) * var6);
            this.rightArm.yRot = Mth.lerp(var4, this.rightArm.yRot, 0.18F);
            this.leftArm.xRot = Mth.lerp(var4, this.leftArm.xRot, -1.7F * var5 + 1.1F * var6);
            this.body.xRot = Mth.lerp(var4, this.body.xRot, -0.12F * var5 + 0.3F * var6);
        }

        this.rightLeg.xRot = Mth.lerp(var4, this.rightLeg.xRot, -0.18F);
        this.leftLeg.xRot = Mth.lerp(var4, this.leftLeg.xRot, 0.18F);
    }

    private void applyWeaponPose(int var1, float var2, float var3) {
        float var4 = EasterCombatMath.smooth(var2 / 0.35F);
        float var5 = EasterCombatMath.smooth((var2 - 0.35F) / 0.2F);
        float var6 = 1.0F - var5;
        float var7 = var3 * (0.5F + var4 * 0.5F);
        switch (var1) {
            case 1:
                this.arm(this.rightArm, true, 0.5F + var5 * 1.2F, 0.15F, var7);
                this.arm(this.leftArm, false, 0.35F, 0.2F, var7 * 0.7F);
                this.body.xRot = Mth.lerp(var7, this.body.xRot, 0.12F + var5 * 0.12F);
                break;
            case 2:
                this.arm(this.rightArm, true, 1.45F, -0.65F + var5 * 1.1F, var7);
                this.arm(this.leftArm, false, 0.6F, 0.4F - var5 * 0.4F, var7 * 0.6F);
                this.body.yRot = Mth.lerp(var7, this.body.yRot, -0.18F + var5 * 0.36F);
                break;
            default:
                this.arm(this.rightArm, true, Mth.lerp(var5, 2.5F * var4 + 0.4F, 0.65F), 0.2F, var7);
                this.arm(this.leftArm, false, 0.9F * var4 - 0.45F * var5, 0.25F, var7 * 0.6F);
                this.body.xRot = Mth.lerp(var7, this.body.xRot, -0.09F * var6 + 0.18F * var5);
                this.head.xRot = Mth.lerp(var7, this.head.xRot, 0.1F * var5);
        }

        this.rightLeg.xRot = Mth.lerp(var7, this.rightLeg.xRot, 0.18F * var5 - 0.1F * var4);
        this.leftLeg.xRot = Mth.lerp(var7, this.leftLeg.xRot, -0.18F * var5 + 0.1F * var4);
    }

    private void applyRangedPose(EasterBossEntity var1, float var2, float var3, float var4) {
        float var5 = var4 * (float) (Math.PI / 180.0);
        float var6 = var3 * (float) (Math.PI / 180.0);
        CompoundTag var7 = var1.getSkillCue();
        float var8 = (float)(var1.level().getGameTime() - var7.getLong("start")) + var2;
        boolean var9 = var7.getBoolean("beam") && XuanlanGunPoseMath.activeCue(var8, var7.getInt("duration"));
        if (var9) {
            double var10 = var7.getDouble("endX") - var7.getDouble("x");
            double var12 = var7.getDouble("endY") - var7.getDouble("y");
            double var14 = var7.getDouble("endZ") - var7.getDouble("z");
            if (var10 * var10 + var12 * var12 + var14 * var14 > 1.0E-6) {
                var5 = XuanlanGunPoseMath.beamPitch(var10, var12, var14);
                var6 = XuanlanGunPoseMath.beamYaw(var10, var14, Mth.rotLerp(var2, var1.yBodyRotO, var1.yBodyRot));
            }
        }

        boolean var16 = var1.isAlive() && (var9 || var1.isCastingLike() || var1.getActionTicks() > 0);
        boolean var11 = var1.getMainArm() == HumanoidArm.RIGHT;
        ModelPart var17 = var11 ? this.rightArm : this.leftArm;
        ModelPart var13 = var11 ? this.leftArm : this.rightArm;
        this.rightArm.resetPose();
        this.leftArm.resetPose();
        var17.xRot = var16 ? XuanlanGunPoseMath.armPitch(var5) : -0.4F;
        var17.yRot = var16 ? var6 : 0.0F;
        var17.zRot = 0.0F;
        var13.xRot = var16 ? -0.65F : -0.15F;
        var13.yRot = var11 ? 0.25F : -0.25F;
        var13.zRot = 0.0F;
        this.body.xRot = 0.0F;
        this.body.yRot = 0.0F;
        this.head.xRot = var5;
        this.head.yRot = var6;
    }

    private void applyDoraDualPose(EasterBossEntity var1, float var2, float var3, float var4) {
        CompoundTag var5 = var1.getSkillCue();
        float var6 = (float)(var1.level().getGameTime() - var5.getLong("start")) + var2;
        if (var5.getBoolean("doraSniper") && var6 >= 0.0F && var6 < (float)var5.getInt("duration")) {
            this.applyDoraSniperPose(var5.getBoolean("firing"), Mth.clamp(var6 / (float)var5.getInt("duration"), 0.0F, 1.0F));
            float var11 = Mth.clamp(((float)var1.getDoraSpearTicks() - var2) / 4.0F, 0.0F, 1.0F);
            this.leftArm.xRot = Mth.lerp(var11, -0.2F, -1.7F);
            this.leftArm.yRot = -0.18F + this.head.yRot * var11;
        } else {
            boolean var7 = var1.isDoraReloading();
            boolean var8 = !var7 && var1.getActionTicks() > 0;
            float var9 = var8 ? Mth.sin(var3 * 2.1F) * 0.055F : 0.0F;
            float var10 = Mth.clamp(((float)var1.getDoraSpearTicks() - var2) / 4.0F, 0.0F, 1.0F);
            this.rightArm.xRot = var8 ? -1.48F + this.head.xRot + var9 : (var7 ? -0.8F : -0.35F);
            this.rightArm.yRot = var8 ? this.head.yRot - 0.08F : -0.1F;
            this.rightArm.zRot = 0.03F;
            this.leftArm.xRot = Mth.lerp(var10, var7 ? -0.95F : -0.45F, -1.7F);
            this.leftArm.yRot = -0.18F + this.head.yRot * var10;
            this.leftArm.zRot = -0.1F;
            this.body.xRot = var7 ? 0.16F : 0.06F * var4 + 0.1F * var10;
            this.body.yRot = 0.0F;
        }
    }

    private void applyDoraSniperPose(boolean var1, float var2) {
        float var3 = var1 ? Mth.sin(var2 * (float) Math.PI) * 0.28F : 0.0F;
        this.rightArm.xRot = -1.52F + this.head.xRot - var3;
        this.rightArm.yRot = this.head.yRot - 0.11F;
        this.rightArm.zRot = 0.045F;
        this.leftArm.xRot = -0.2F;
        this.leftArm.yRot = -0.16F;
        this.leftArm.zRot = -0.1F;
        this.body.xRot = 0.04F - var3 * 0.15F;
        this.body.yRot = -0.06F;
        this.rightLeg.xRot = 0.07F;
        this.leftLeg.xRot = -0.12F;
    }

    private void applyCastPose(EasterBossEntity var1, int var2, float var3, float var4) {
        float var5 = Mth.sin(var3 * 0.18F) * 0.05F;
        float var6 = Mth.sin(var3 * 0.08F) * 0.06F;
        float var7 = Mth.sin(var3 * 0.35F) * 0.07F;
        float var9 = 2.3F;
        float var10 = 0.3F;
        float var11 = 2.3F;
        float var12 = 0.3F;
        float var13 = 0.07F;
        float var14 = -0.18F;
        float var15 = 0.06F;
        float var16 = -0.06F;
        switch (var2) {
            case 1:
                var9 = 1.62F + var7;
                var10 = 0.14F;
                var11 = 1.62F - var7;
                var12 = 0.14F;
                var13 = 0.16F;
                var14 = -0.06F;
                var15 = 0.11F;
                var16 = -0.11F;
                break;
            case 2:
                var9 = 1.58F;
                var10 = -0.06F;
                var11 = 0.42F;
                var12 = 0.14F;
                var13 = 0.05F;
                var14 = -0.1F;
                var15 = 0.13F;
                var16 = -0.13F;
                break;
            case 3:
                var9 = 2.95F + var5;
                var10 = -0.55F;
                var11 = 2.95F - var5;
                var12 = -0.55F;
                var13 = -0.09F;
                var14 = -0.42F;
                var15 = 0.02F;
                var16 = -0.02F;
                break;
            case 4:
                var9 = 1.1F + var5 * 0.5F;
                var10 = 0.62F;
                var11 = 1.1F - var5 * 0.5F;
                var12 = 0.62F;
                var13 = 0.18F;
                var14 = 0.24F;
                var15 = 0.05F;
                var16 = -0.05F;
                break;
            default:
                var9 = 2.3F + var5;
                var10 = 0.3F + var6;
                var11 = 2.3F - var5;
                var12 = 0.3F + var6;
        }

        this.arm(this.rightArm, true, var9, var10 + var6 * 0.5F, var4);
        this.arm(this.leftArm, false, var11, var12 + var6 * 0.5F, var4);
        switch (var1.getVariant()) {
            case 3:
                this.arm(this.rightArm, true, 2.65F + var5, 0.12F, var4);
                this.arm(this.leftArm, false, 0.75F, 0.15F, var4);
                break;
            case 4:
                this.arm(this.rightArm, true, 0.85F + var5, 0.12F, var4);
                this.arm(this.leftArm, false, 1.25F - var5, 0.18F, var4);
            case 5:
            case 6:
            case 7:
            default:
                break;
            case 8:
                this.arm(this.rightArm, true, 1.1F + var7, -0.18F, var4);
                this.arm(this.leftArm, false, 0.6F - var7, -0.15F, var4);
                break;
            case 9:
                this.arm(this.rightArm, true, 2.15F + var5, 0.08F, var4);
                this.arm(this.leftArm, false, 0.8F, 0.35F, var4);
        }

        if (var1.getMainHandItem().is(FTBossItems.SHADOW_SWORD.get())) {
            this.rightArm.xRot = Mth.lerp(var4, this.rightArm.xRot, -POSE_FWD);
            this.rightArm.yRot = Mth.lerp(var4, this.rightArm.yRot, -POSE_YAW);
            this.rightArm.zRot = Mth.lerp(var4, this.rightArm.zRot, POSE_ROLL);
            this.leftArm.xRot = Mth.lerp(var4, this.leftArm.xRot, -0.15F);
            if (!poseLogged) {
                poseLogged = true;
                FTBossMod.LOGGER
                    .info(
                        "[FTBoss][剑姿态] 生效 fwd={} yaw={} roll={}（默认 {}/{}/{}；可用 -Dftboss.swordPose.fwd/yaw/roll 覆盖）",
                        POSE_FWD,
                        POSE_YAW,
                        POSE_ROLL,
                        0.2F,
                        0.0F,
                        0.0F
                    );
            }
        }

        this.body.xRot = Mth.lerp(var4, this.body.xRot, var13);
        this.head.xRot = Mth.lerp(var4, this.head.xRot, var14 + var5 * 0.5F);
        this.rightLeg.xRot = Mth.lerp(var4, this.rightLeg.xRot, var15);
        this.leftLeg.xRot = Mth.lerp(var4, this.leftLeg.xRot, var16);
    }

    private void arm(ModelPart var1, boolean var2, float var3, float var4, float var5) {
        float var6 = var2 ? 1.0F : -1.0F;
        var1.xRot = Mth.lerp(var5, var1.xRot, -var3);
        var1.yRot = Mth.lerp(var5, var1.yRot, -var6 * var4);
        var1.zRot = Mth.lerp(var5, var1.zRot, var6 * var4);
    }

    private static float ftboss$basicBodySwing(float var0, EasterBossEntity var1) {
        return EasterBossEntity.isManorVariant(var1.getVariant()) && !var1.isCastingLike() && !var1.isPerformingSkill() && var1.getActionTicks() <= 0
            ? 0.0F
            : var0;
    }

    @Override
    public void renderToBuffer(PoseStack poses, VertexConsumer vertices, int light, int overlay, int color) {
        if (!GongdiHumanoidRenderer.renderBody(this, poses, vertices, light, overlay, ((color >>> 16) & 255) / 255.0F, ((color >>> 8) & 255) / 255.0F, (color & 255) / 255.0F, ((color >>> 24) & 255) / 255.0F)) {
            super.renderToBuffer(poses, vertices, light, overlay, color);
        }
    }
}
