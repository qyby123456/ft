/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelLayerLocation
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.client.model.geom.builders.CubeDeformation
 *  net.minecraft.client.model.geom.builders.LayerDefinition
 *  net.minecraft.client.model.geom.builders.MeshDefinition
 *  net.minecraft.client.model.geom.builders.PartDefinition
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item
 */
package net.farmingtales.ftboss.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.FTBossMod;
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
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.FTBossItems;
import net.farmingtales.ftboss.item.XuanlanMoonWeaponItem;
import net.farmingtales.ftboss.lemoncasting.client.LemonCastingActor;
import net.farmingtales.ftboss.lemonmotion.client.LemonMotionActor;
import net.farmingtales.ftboss.manorrun.RunAnimator;
import net.farmingtales.ftboss.util.EasterCombatMath;
import net.farmingtales.ftboss.util.XuanlanGunPoseMath;
import net.farmingtales.ftboss.youzi.motion.YouziCharacterAdapter;
import net.farmingtales.ftboss.zheergenbindingbody.client.BindingAnimator;
import net.farmingtales.ftboss.zheergennightfall.client.NightfallAnimator;
import net.farmingtales.ftboss.zheergenremainingbody.client.SkillBodyAnimator;
import net.farmingtales.ftboss.zheergenvoidbody.client.VoidAnimator;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

public class EasterBossModel
extends PlayerModel<EasterBossEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation("ftboss", "easter_boss"), "main");
    public static final int STYLE_MELEE = -1;
    public static final int STYLE_TWO_HAND = 0;
    public static final int STYLE_CHANNEL = 1;
    public static final int STYLE_POINT = 2;
    public static final int STYLE_RAISE = 3;
    public static final int STYLE_EMBRACE = 4;
    private static final float SWORD_POSE_FORWARD = 0.2f;
    private static final float SWORD_POSE_YAW = 0.0f;
    private static final float SWORD_POSE_ROLL = 0.0f;
    private static final float POSE_FWD = EasterBossModel.prop("ftboss.swordPose.fwd", 0.2f);
    private static final float POSE_YAW = EasterBossModel.prop("ftboss.swordPose.yaw", 0.0f);
    private static final float POSE_ROLL = EasterBossModel.prop("ftboss.swordPose.roll", 0.0f);
    private static boolean poseLogged = false;
    private final ModelPart ahoge;
    private final ModelPart hair;
    private final ModelPart cape;
    private final ModelPart halo;

    private static float prop(String key, float fallback) {
        try {
            String v = System.getProperty(key);
            return v == null || v.isEmpty() ? fallback : Float.parseFloat(v.trim());
        }
        catch (NumberFormatException e) {
            FTBossMod.LOGGER.warn("[FTBoss][\u5251\u59ff\u6001] \u7cfb\u7edf\u5c5e\u6027 {} \u4e0d\u662f\u6570\u5b57\uff0c\u7528\u9ed8\u8ba4\u503c {}", (Object)key, (Object)Float.valueOf(fallback));
            return fallback;
        }
    }

    public EasterBossModel(ModelPart root) {
        super(root, true);
        this.ahoge = EasterBossModel.childOrEmpty(root, "easter_ahoge");
        this.hair = EasterBossModel.childOrEmpty(root, "easter_hair");
        this.cape = EasterBossModel.childOrEmpty(root, "easter_cape");
        this.halo = EasterBossModel.childOrEmpty(root, "easter_halo");
    }

    private static ModelPart childOrEmpty(ModelPart root, String name) {
        try {
            return root.m_233562_(name) ? root.m_171324_(name) : new ModelPart(List.of(), Map.of());
        }
        catch (Throwable t) {
            return new ModelPart(List.of(), Map.of());
        }
    }

    public static LayerDefinition createBodyLayer(boolean slim) {
        MeshDefinition mesh = PlayerModel.m_170825_((CubeDeformation)CubeDeformation.f_171458_, (boolean)slim);
        PartDefinition root = mesh.m_171576_();
        PartDefinition head = root.m_171597_("head");
        PartDefinition body = root.m_171597_("body");
        return LayerDefinition.m_171565_((MeshDefinition)mesh, (int)64, (int)64);
    }

    /*
     * WARNING - void declaration
     */
    public void setupAnim(EasterBossEntity easterBossEntity, float f, float f2, float f3, float f4, float f5) {
        CompoundTag cue;
        float castBlend;
        void headPitch;
        void netHeadYaw;
        void ageInTicks;
        void limbSwingAmount;
        void limbSwing;
        void entity;
        this.f_102608_ = EasterBossModel.ftboss$basicBodySwing(this.f_102608_, easterBossEntity);
        RunAnimator.begin(easterBossEntity);
        this.f_102808_.m_233569_();
        this.f_102810_.m_233569_();
        this.f_102811_.m_233569_();
        this.f_102812_.m_233569_();
        this.f_102813_.m_233569_();
        this.f_102814_.m_233569_();
        super.m_6973_((LivingEntity)entity, (float)limbSwing, (float)limbSwingAmount, (float)ageInTicks, (float)netHeadYaw, (float)headPitch);
        net.farmingtales.ftboss.client.manorrun.RunAnimator.prepare(this, (EasterBossEntity)entity, (float)limbSwing, (float)limbSwingAmount, (float)ageInTicks);
        LemonCastingActor.capture(this, (EasterBossEntity)entity);
        LemonMotionActor.capture(this, (EasterBossEntity)entity);
        float partialTick = Mth.m_14036_((float)(ageInTicks - (float)entity.f_19797_), (float)0.0f, (float)1.0f);
        float weight = entity.getCombatAnimationWeight(partialTick);
        boolean casting = entity.isCastingLike();
        float actionBlend = casting ? 0.0f : weight;
        float f6 = castBlend = casting ? weight : 0.0f;
        if (entity.getVariant() == 11) {
            boolean prayer = entity.getSkillCue().m_128451_("qizai") == 3;
            this.f_102811_.f_104203_ = Mth.m_14179_((float)castBlend, (float)-0.18f, (float)(prayer ? -1.1f : -1.7f));
            this.f_102811_.f_104205_ = Mth.m_14179_((float)castBlend, (float)0.08f, (float)(prayer ? 0.35f : 0.1f));
            this.f_102812_.f_104203_ = Mth.m_14179_((float)castBlend, (float)this.f_102812_.f_104203_, (float)-1.05f);
            this.f_102812_.f_104204_ = castBlend * (prayer ? 0.45f : -0.25f);
            this.f_102812_.f_104205_ = -castBlend * 0.3f;
        } else if (entity.getVariant() == 6 && entity.m_21205_().m_150930_((Item)FTBossItems.WENXUANLAN_FLYWHEEL.get()) && !XuanlanMoonWeaponItem.isWhite(entity.m_21205_())) {
            this.applyRangedPose((EasterBossEntity)entity, partialTick, (float)netHeadYaw, (float)headPitch);
        } else if (castBlend > 0.0f) {
            this.applyCastPose((EasterBossEntity)entity, entity.getCastStyle(), (float)ageInTicks, CastPose.filterGeneric(castBlend, (EasterBossEntity)entity));
        } else if (actionBlend > 0.0f) {
            this.applyWeaponPose(entity.getWeaponStyle(), entity.getActionProgress(partialTick), BasicPose.filterBlend(net.farmingtales.ftboss.client.qingyi_basic.BasicPose.filterGeneric(CastPose.filterGeneric(actionBlend, (EasterBossEntity)entity), (EasterBossEntity)entity), (EasterBossEntity)entity));
        }
        if (entity.getVariant() == 2) {
            this.applyDoraDualPose((EasterBossEntity)entity, partialTick, (float)ageInTicks, actionBlend);
        }
        if (entity.getVariant() == 3 && weight > 0.0f && (cue = entity.getSkillCue()).m_128441_("gongdiSkill")) {
            float progress = EasterCombatMath.clamp(((float)(entity.m_9236_().m_46467_() - cue.m_128454_("start")) + partialTick) / (float)Math.max(1, cue.m_128451_("duration")));
            float gongdiWeight = cue.m_128471_("released") ? weight : EasterCombatMath.smooth((entity.getCombatAnimationAge(partialTick) + 1.0f) / 2.0f);
            this.applyGongdiPose(cue.m_128461_("gongdiSkill"), cue.m_128471_("released"), progress, gongdiWeight);
        }
        float sway = Mth.m_14031_((float)(ageInTicks * 0.12f)) * 0.18f;
        float swing = Mth.m_14031_((float)(limbSwing * 0.6662f)) * limbSwingAmount * 0.5f;
        this.ahoge.f_104203_ = -0.15f + sway * 0.9f + swing * 0.5f;
        this.ahoge.f_104205_ = Mth.m_14031_((float)(ageInTicks * 0.21f)) * 0.22f;
        this.hair.f_104203_ = 0.05f + swing * 0.35f + actionBlend * 0.25f;
        this.cape.f_104203_ = 0.06f + swing * 0.8f - actionBlend * 0.35f + castBlend * 0.15f;
        this.halo.f_104204_ = ageInTicks * 0.03f;
        this.halo.f_104201_ = -12.5f + Mth.m_14031_((float)(ageInTicks * 0.1f)) * 0.35f;
        this.f_102809_.f_104207_ = false;
        this.f_103378_.f_104207_ = false;
        this.f_103374_.f_104207_ = false;
        this.f_103375_.f_104207_ = false;
        this.f_103376_.f_104207_ = false;
        this.f_103377_.f_104207_ = false;
        entity.getVariant();
        this.f_103375_.f_104207_ = true;
        this.f_103374_.f_104207_ = true;
        this.f_103378_.f_104207_ = true;
        this.f_102809_.f_104207_ = true;
        this.f_103377_.f_104207_ = true;
        this.f_103376_.f_104207_ = true;
        this.f_103374_.m_104315_(this.f_102812_);
        this.f_103375_.m_104315_(this.f_102811_);
        boolean[] f7 = entity.getExtraBones();
        this.ahoge.f_104207_ = f7[0];
        this.hair.f_104207_ = f7[1];
        this.cape.f_104207_ = f7[2];
        this.halo.f_104207_ = f7[3];
        ZicaiHumanoidAnimation.apply(this, (EasterBossEntity)entity, (float)limbSwing, (float)limbSwingAmount, (float)ageInTicks, (float)netHeadYaw, (float)headPitch);
        ZicaiCastAnimation.apply(this, (EasterBossEntity)entity, (float)limbSwing, (float)limbSwingAmount, (float)ageInTicks, (float)netHeadYaw, (float)headPitch);
        LemonMotionActor.animate(this, (EasterBossEntity)entity, (float)limbSwing, (float)limbSwingAmount, (float)ageInTicks, (float)netHeadYaw, (float)headPitch, this.hair, this.cape, this.ahoge);
        LemonCastingActor.animate(this, (EasterBossEntity)entity, (float)limbSwing, (float)limbSwingAmount, (float)ageInTicks, (float)netHeadYaw, (float)headPitch, this.hair, this.cape, this.ahoge);
        BodyAnimator.apply(this, (EasterBossEntity)entity, (float)limbSwing, (float)limbSwingAmount, (float)ageInTicks, (float)netHeadYaw, (float)headPitch);
        MotionActor.animate(this, (EasterBossEntity)entity, (float)ageInTicks);
        DoraAnimator.apply(this, (EasterBossEntity)entity, (float)limbSwing, (float)limbSwingAmount, (float)ageInTicks, (float)netHeadYaw, (float)headPitch);
        MaqueHumanoidAnimations.apply(this, (EasterBossEntity)entity, (float)limbSwing, (float)limbSwingAmount, (float)ageInTicks, (float)netHeadYaw, (float)headPitch);
        DoraBasicAnimation.apply(this, (EasterBossEntity)entity, (float)limbSwing, (float)limbSwingAmount, (float)ageInTicks, (float)netHeadYaw, (float)headPitch);
        ShadowSwordAnimation.apply(this, (EasterBossEntity)entity, (float)limbSwing, (float)limbSwingAmount, (float)ageInTicks, (float)netHeadYaw, (float)headPitch);
        NightfallAnimator.apply(this, (EasterBossEntity)entity, (float)limbSwing, (float)limbSwingAmount, (float)ageInTicks, (float)netHeadYaw, (float)headPitch);
        BindingAnimator.apply(this, (EasterBossEntity)entity, (float)limbSwing, (float)limbSwingAmount, (float)ageInTicks, (float)netHeadYaw, (float)headPitch);
        VoidAnimator.apply(this, (EasterBossEntity)entity, (float)limbSwing, (float)limbSwingAmount, (float)ageInTicks, (float)netHeadYaw, (float)headPitch);
        SkillBodyAnimator.apply(this, (EasterBossEntity)entity, (float)limbSwing, (float)limbSwingAmount, (float)ageInTicks, (float)netHeadYaw, (float)headPitch);
        QizaiActionClient.apply(this, (EasterBossEntity)entity, (float)limbSwing, (float)limbSwingAmount, (float)ageInTicks, (float)netHeadYaw, (float)headPitch);
        QingyiCastAnimation.apply(this, (EasterBossEntity)entity, (float)limbSwing, (float)limbSwingAmount, (float)ageInTicks, (float)netHeadYaw, (float)headPitch);
        net.farmingtales.ftboss.client.qingyi_basic.BasicPose.apply(this, (EasterBossEntity)entity, (float)ageInTicks, (float)limbSwingAmount);
        IronActor.animate(this, (EasterBossEntity)entity, (float)ageInTicks);
        GongdiHumanoidRenderer.setup(this, (EasterBossEntity)entity, (float)limbSwing, (float)limbSwingAmount, (float)ageInTicks, (float)netHeadYaw, (float)headPitch);
        RunPose.apply(this, (EasterBossEntity)entity, (float)limbSwing, (float)limbSwingAmount, (float)ageInTicks);
        BasicPose.apply(this, (EasterBossEntity)entity, (float)ageInTicks, (float)limbSwingAmount);
        CastPose.apply(this, (EasterBossEntity)entity, (float)ageInTicks, (float)limbSwingAmount);
        YouziCharacterAdapter.apply(this, (EasterBossEntity)entity, (float)limbSwing, (float)limbSwingAmount, (float)ageInTicks, (float)netHeadYaw, (float)headPitch);
        RunAnimator.generic(this, (EasterBossEntity)entity, (float)ageInTicks);
        RunActor.animate(this, (EasterBossEntity)entity, (float)ageInTicks);
        RunAnimator.finish(this, (EasterBossEntity)entity, (float)limbSwing, (float)limbSwingAmount, (float)ageInTicks, (float)netHeadYaw, (float)headPitch);
        ManorRunAnimation.apply(this, (EasterBossEntity)entity, (float)ageInTicks);
    }

    private void applyGongdiPose(String skill, boolean released, float progress, float weight) {
        float strike;
        float wind = released ? 1.0f : EasterCombatMath.smooth(progress * 1.6f);
        float f = strike = released ? 1.0f + 0.12f * Mth.m_14031_((float)(progress * (float)Math.PI)) : EasterCombatMath.smooth((progress - 0.82f) / 0.18f);
        if (skill.equals("decimate")) {
            this.f_102811_.f_104203_ = Mth.m_14179_((float)weight, (float)this.f_102811_.f_104203_, (float)(-0.9f - 0.4f * wind));
            this.f_102811_.f_104204_ = Mth.m_14179_((float)weight, (float)this.f_102811_.f_104204_, (float)(-1.1f * wind + 2.0f * strike));
            this.f_102811_.f_104205_ = Mth.m_14179_((float)weight, (float)this.f_102811_.f_104205_, (float)-0.35f);
            this.f_102812_.f_104203_ = Mth.m_14179_((float)weight, (float)this.f_102812_.f_104203_, (float)-0.8f);
            this.f_102810_.f_104204_ = Mth.m_14179_((float)weight, (float)this.f_102810_.f_104204_, (float)(-0.35f * wind + 0.8f * strike));
        } else if (skill.equals("apprehend")) {
            this.f_102811_.f_104203_ = Mth.m_14179_((float)weight, (float)this.f_102811_.f_104203_, (float)(-1.55f * wind + 1.0f * strike));
            this.f_102811_.f_104204_ = Mth.m_14179_((float)weight, (float)this.f_102811_.f_104204_, (float)(-0.35f + 0.8f * strike));
            this.f_102812_.f_104203_ = Mth.m_14179_((float)weight, (float)this.f_102812_.f_104203_, (float)(-1.1f * wind + 0.8f * strike));
            this.f_102810_.f_104203_ = Mth.m_14179_((float)weight, (float)this.f_102810_.f_104203_, (float)(0.12f * wind - 0.2f * strike));
        } else {
            float height = skill.equals("guillotine") ? 2.9f : 2.2f;
            this.f_102811_.f_104203_ = Mth.m_14179_((float)weight, (float)this.f_102811_.f_104203_, (float)(-height * wind + (height - 0.45f) * strike));
            this.f_102811_.f_104204_ = Mth.m_14179_((float)weight, (float)this.f_102811_.f_104204_, (float)0.18f);
            this.f_102812_.f_104203_ = Mth.m_14179_((float)weight, (float)this.f_102812_.f_104203_, (float)(-1.7f * wind + 1.1f * strike));
            this.f_102810_.f_104203_ = Mth.m_14179_((float)weight, (float)this.f_102810_.f_104203_, (float)(-0.12f * wind + 0.3f * strike));
        }
        this.f_102813_.f_104203_ = Mth.m_14179_((float)weight, (float)this.f_102813_.f_104203_, (float)-0.18f);
        this.f_102814_.f_104203_ = Mth.m_14179_((float)weight, (float)this.f_102814_.f_104203_, (float)0.18f);
    }

    private void applyWeaponPose(int style, float progress, float blend) {
        float wind = EasterCombatMath.smooth(progress / 0.35f);
        float strike = EasterCombatMath.smooth((progress - 0.35f) / 0.2f);
        float back = 1.0f - strike;
        float strength = blend * (0.5f + wind * 0.5f);
        switch (style) {
            case 1: {
                this.arm(this.f_102811_, true, 0.5f + strike * 1.2f, 0.15f, strength);
                this.arm(this.f_102812_, false, 0.35f, 0.2f, strength * 0.7f);
                this.f_102810_.f_104203_ = Mth.m_14179_((float)strength, (float)this.f_102810_.f_104203_, (float)(0.12f + strike * 0.12f));
                break;
            }
            case 2: {
                this.arm(this.f_102811_, true, 1.45f, -0.65f + strike * 1.1f, strength);
                this.arm(this.f_102812_, false, 0.6f, 0.4f - strike * 0.4f, strength * 0.6f);
                this.f_102810_.f_104204_ = Mth.m_14179_((float)strength, (float)this.f_102810_.f_104204_, (float)(-0.18f + strike * 0.36f));
                break;
            }
            default: {
                this.arm(this.f_102811_, true, Mth.m_14179_((float)strike, (float)(2.5f * wind + 0.4f), (float)0.65f), 0.2f, strength);
                this.arm(this.f_102812_, false, 0.9f * wind - 0.45f * strike, 0.25f, strength * 0.6f);
                this.f_102810_.f_104203_ = Mth.m_14179_((float)strength, (float)this.f_102810_.f_104203_, (float)(-0.09f * back + 0.18f * strike));
                this.f_102808_.f_104203_ = Mth.m_14179_((float)strength, (float)this.f_102808_.f_104203_, (float)(0.1f * strike));
            }
        }
        this.f_102813_.f_104203_ = Mth.m_14179_((float)strength, (float)this.f_102813_.f_104203_, (float)(0.18f * strike - 0.1f * wind));
        this.f_102814_.f_104203_ = Mth.m_14179_((float)strength, (float)this.f_102814_.f_104203_, (float)(-0.18f * strike + 0.1f * wind));
    }

    private void applyRangedPose(EasterBossEntity entity, float partialTick, float netHeadYaw, float headPitch) {
        double directionZ;
        double directionY;
        double directionX;
        boolean beam;
        float aimPitch = headPitch * ((float)Math.PI / 180);
        float aimYaw = netHeadYaw * ((float)Math.PI / 180);
        CompoundTag cue = entity.getSkillCue();
        float cueAge = (float)(entity.m_9236_().m_46467_() - cue.m_128454_("start")) + partialTick;
        boolean bl = beam = cue.m_128471_("beam") && XuanlanGunPoseMath.activeCue(cueAge, cue.m_128451_("duration"));
        if (beam && (directionX = cue.m_128459_("endX") - cue.m_128459_("x")) * directionX + (directionY = cue.m_128459_("endY") - cue.m_128459_("y")) * directionY + (directionZ = cue.m_128459_("endZ") - cue.m_128459_("z")) * directionZ > 1.0E-6) {
            aimPitch = XuanlanGunPoseMath.beamPitch(directionX, directionY, directionZ);
            aimYaw = XuanlanGunPoseMath.beamYaw(directionX, directionZ, Mth.m_14189_((float)partialTick, (float)entity.f_20884_, (float)entity.f_20883_));
        }
        boolean aiming = entity.m_6084_() && (beam || entity.isCastingLike() || entity.getActionTicks() > 0);
        boolean rightHanded = entity.m_5737_() == HumanoidArm.RIGHT;
        ModelPart gunArm = rightHanded ? this.f_102811_ : this.f_102812_;
        ModelPart freeArm = rightHanded ? this.f_102812_ : this.f_102811_;
        this.f_102811_.m_233569_();
        this.f_102812_.m_233569_();
        gunArm.f_104203_ = aiming ? XuanlanGunPoseMath.armPitch(aimPitch) : -0.4f;
        gunArm.f_104204_ = aiming ? aimYaw : 0.0f;
        gunArm.f_104205_ = 0.0f;
        freeArm.f_104203_ = aiming ? -0.65f : -0.15f;
        freeArm.f_104204_ = rightHanded ? 0.25f : -0.25f;
        freeArm.f_104205_ = 0.0f;
        this.f_102810_.f_104203_ = 0.0f;
        this.f_102810_.f_104204_ = 0.0f;
        this.f_102808_.f_104203_ = aimPitch;
        this.f_102808_.f_104204_ = aimYaw;
    }

    private void applyDoraDualPose(EasterBossEntity entity, float partialTick, float age, float actionBlend) {
        CompoundTag cue = entity.getSkillCue();
        float cueAge = (float)(entity.m_9236_().m_46467_() - cue.m_128454_("start")) + partialTick;
        if (cue.m_128471_("doraSniper") && cueAge >= 0.0f && cueAge < (float)cue.m_128451_("duration")) {
            this.applyDoraSniperPose(cue.m_128471_("firing"), Mth.m_14036_((float)(cueAge / (float)cue.m_128451_("duration")), (float)0.0f, (float)1.0f));
            entity.getDoraSpearTicks();
            float thrust = Mth.m_14036_((float)((0.0f - partialTick) / 4.0f), (float)0.0f, (float)1.0f);
            this.f_102812_.f_104203_ = Mth.m_14179_((float)thrust, (float)-0.2f, (float)-1.7f);
            this.f_102812_.f_104204_ = -0.18f + this.f_102808_.f_104204_ * thrust;
            return;
        }
        boolean reloading = entity.isDoraReloading();
        boolean shooting = !reloading && entity.getActionTicks() > 0;
        float recoil = shooting ? Mth.m_14031_((float)(age * 2.1f)) * 0.055f : 0.0f;
        entity.getDoraSpearTicks();
        float thrust = Mth.m_14036_((float)((0.0f - partialTick) / 4.0f), (float)0.0f, (float)1.0f);
        this.f_102811_.f_104203_ = shooting ? -1.48f + this.f_102808_.f_104203_ + recoil : (reloading ? -0.8f : -0.35f);
        this.f_102811_.f_104204_ = shooting ? this.f_102808_.f_104204_ - 0.08f : -0.1f;
        this.f_102811_.f_104205_ = 0.03f;
        this.f_102812_.f_104203_ = Mth.m_14179_((float)thrust, (float)(reloading ? -0.95f : -0.45f), (float)-1.7f);
        this.f_102812_.f_104204_ = -0.18f + this.f_102808_.f_104204_ * thrust;
        this.f_102812_.f_104205_ = -0.1f;
        this.f_102810_.f_104203_ = reloading ? 0.16f : 0.06f * actionBlend + 0.1f * thrust;
        this.f_102810_.f_104204_ = 0.0f;
    }

    private void applyDoraSniperPose(boolean fired, float progress) {
        float recoil = fired ? Mth.m_14031_((float)(progress * (float)Math.PI)) * 0.28f : 0.0f;
        this.f_102811_.f_104203_ = -1.52f + this.f_102808_.f_104203_ - recoil;
        this.f_102811_.f_104204_ = this.f_102808_.f_104204_ - 0.11f;
        this.f_102811_.f_104205_ = 0.045f;
        this.f_102812_.f_104203_ = -0.2f;
        this.f_102812_.f_104204_ = -0.16f;
        this.f_102812_.f_104205_ = -0.1f;
        this.f_102810_.f_104203_ = 0.04f - recoil * 0.15f;
        this.f_102810_.f_104204_ = -0.06f;
        this.f_102813_.f_104203_ = 0.07f;
        this.f_102814_.f_104203_ = -0.12f;
    }

    private void applyCastPose(EasterBossEntity entity, int style, float age, float b) {
        float bob = Mth.m_14031_((float)(age * 0.18f)) * 0.05f;
        float sway = Mth.m_14031_((float)(age * 0.08f)) * 0.06f;
        float pulse = Mth.m_14031_((float)(age * 0.35f)) * 0.07f;
        float k = b;
        float aFwdR = 2.3f;
        float aInR = 0.3f;
        float aFwdL = 2.3f;
        float aInL = 0.3f;
        float bodyX = 0.07f;
        float headX = -0.18f;
        float legR = 0.06f;
        float legL = -0.06f;
        switch (style) {
            case 1: {
                aFwdR = 1.62f + pulse;
                aInR = 0.14f;
                aFwdL = 1.62f - pulse;
                aInL = 0.14f;
                bodyX = 0.16f;
                headX = -0.06f;
                legR = 0.11f;
                legL = -0.11f;
                break;
            }
            case 2: {
                aFwdR = 1.58f;
                aInR = -0.06f;
                aFwdL = 0.42f;
                aInL = 0.14f;
                bodyX = 0.05f;
                headX = -0.1f;
                legR = 0.13f;
                legL = -0.13f;
                break;
            }
            case 3: {
                aFwdR = 2.95f + bob;
                aInR = -0.55f;
                aFwdL = 2.95f - bob;
                aInL = -0.55f;
                bodyX = -0.09f;
                headX = -0.42f;
                legR = 0.02f;
                legL = -0.02f;
                break;
            }
            case 4: {
                aFwdR = 1.1f + bob * 0.5f;
                aInR = 0.62f;
                aFwdL = 1.1f - bob * 0.5f;
                aInL = 0.62f;
                bodyX = 0.18f;
                headX = 0.24f;
                legR = 0.05f;
                legL = -0.05f;
                break;
            }
            default: {
                aFwdR = 2.3f + bob;
                aInR = 0.3f + sway;
                aFwdL = 2.3f - bob;
                aInL = 0.3f + sway;
            }
        }
        this.arm(this.f_102811_, true, aFwdR, aInR + sway * 0.5f, k);
        this.arm(this.f_102812_, false, aFwdL, aInL + sway * 0.5f, k);
        switch (entity.getVariant()) {
            case 3: {
                this.arm(this.f_102811_, true, 2.65f + bob, 0.12f, k);
                this.arm(this.f_102812_, false, 0.75f, 0.15f, k);
                break;
            }
            case 4: {
                this.arm(this.f_102811_, true, 0.85f + bob, 0.12f, k);
                this.arm(this.f_102812_, false, 1.25f - bob, 0.18f, k);
                break;
            }
            case 8: {
                this.arm(this.f_102811_, true, 1.1f + pulse, -0.18f, k);
                this.arm(this.f_102812_, false, 0.6f - pulse, -0.15f, k);
                break;
            }
            case 9: {
                this.arm(this.f_102811_, true, 2.15f + bob, 0.08f, k);
                this.arm(this.f_102812_, false, 0.8f, 0.35f, k);
                break;
            }
        }
        if (entity.m_21205_().m_150930_((Item)FTBossItems.SHADOW_SWORD.get())) {
            this.f_102811_.f_104203_ = Mth.m_14179_((float)k, (float)this.f_102811_.f_104203_, (float)(-POSE_FWD));
            this.f_102811_.f_104204_ = Mth.m_14179_((float)k, (float)this.f_102811_.f_104204_, (float)(-POSE_YAW));
            this.f_102811_.f_104205_ = Mth.m_14179_((float)k, (float)this.f_102811_.f_104205_, (float)POSE_ROLL);
            this.f_102812_.f_104203_ = Mth.m_14179_((float)k, (float)this.f_102812_.f_104203_, (float)-0.15f);
            if (!poseLogged) {
                poseLogged = true;
                FTBossMod.LOGGER.info("[FTBoss][\u5251\u59ff\u6001] \u751f\u6548 fwd={} yaw={} roll={}\uff08\u9ed8\u8ba4 {}/{}/{}\uff1b\u53ef\u7528 -Dftboss.swordPose.fwd/yaw/roll \u8986\u76d6\uff09", new Object[]{Float.valueOf(POSE_FWD), Float.valueOf(POSE_YAW), Float.valueOf(POSE_ROLL), Float.valueOf(0.2f), Float.valueOf(0.0f), Float.valueOf(0.0f)});
            }
        }
        this.f_102810_.f_104203_ = Mth.m_14179_((float)k, (float)this.f_102810_.f_104203_, (float)bodyX);
        this.f_102808_.f_104203_ = Mth.m_14179_((float)k, (float)this.f_102808_.f_104203_, (float)(headX + bob * 0.5f));
        this.f_102813_.f_104203_ = Mth.m_14179_((float)k, (float)this.f_102813_.f_104203_, (float)legR);
        this.f_102814_.f_104203_ = Mth.m_14179_((float)k, (float)this.f_102814_.f_104203_, (float)legL);
    }

    private void arm(ModelPart p, boolean right, float forward, float inward, float k) {
        float sign = right ? 1.0f : -1.0f;
        p.f_104203_ = Mth.m_14179_((float)k, (float)p.f_104203_, (float)(-forward));
        p.f_104204_ = Mth.m_14179_((float)k, (float)p.f_104204_, (float)(-sign * inward));
        p.f_104205_ = Mth.m_14179_((float)k, (float)p.f_104205_, (float)(sign * inward));
    }

    public void m_7695_(PoseStack poseStack, VertexConsumer vertexConsumer, int n, int n2, float f, float f2, float f3, float f4) {
        if (GongdiHumanoidRenderer.renderBody(this, poseStack, vertexConsumer, n, n2, f, f2, f3, f4)) {
            return;
        }
        super.m_7695_(poseStack, vertexConsumer, n, n2, f, f2, f3, f4);
    }

    private static float ftboss$basicBodySwing(float f, EasterBossEntity easterBossEntity) {
        if (EasterBossEntity.isManorVariant(easterBossEntity.getVariant()) && !easterBossEntity.isCastingLike() && !easterBossEntity.isPerformingSkill() && easterBossEntity.getActionTicks() <= 0) {
            return 0.0f;
        }
        return f;
    }
}

