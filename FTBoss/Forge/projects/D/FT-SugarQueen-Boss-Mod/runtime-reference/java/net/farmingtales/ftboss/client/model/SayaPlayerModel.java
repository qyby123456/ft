/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.api.spells.CastType
 *  net.minecraft.client.model.PlayerModel
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.client.model;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastType;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.boss.ai.BossActionState;
import net.farmingtales.ftboss.client.saya.SayaCastingPose;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class SayaPlayerModel
extends PlayerModel<TeaTreeBossEntity> {
    public static final int STYLE_TWO_HAND = 0;
    public static final int STYLE_CHANNEL = 1;
    public static final int STYLE_POINT = 2;
    public static final int STYLE_RAISE = 3;
    public static final int STYLE_EMBRACE = 4;
    public static final int STYLE_RITUAL = 5;
    private static final float WINDUP_TICKS = 6.0f;
    private static final float RELEASE_TICKS = 8.0f;
    private final Map<TeaTreeBossEntity, CastPoseMemory> castPoses = new WeakHashMap<TeaTreeBossEntity, CastPoseMemory>();

    public SayaPlayerModel(ModelPart root) {
        super(root, true);
    }

    public void setupAnim(TeaTreeBossEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.m_6973_((LivingEntity)entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        if (SayaCastingPose.apply(this, entity, ageInTicks)) {
            return;
        }
        BossActionState signature = entity.signatureCastAction();
        if (signature.active()) {
            float age = signature.elapsed(ageInTicks - (float)entity.f_19797_);
            float windup = Mth.m_14036_((float)(age / 7.0f), (float)0.0f, (float)1.0f);
            float recovery = Mth.m_14036_((float)(((float)signature.duration() - age) / 8.0f), (float)0.0f, (float)1.0f);
            float weight = Math.min(windup, recovery);
            weight = weight * weight * (3.0f - 2.0f * weight);
            int style = "signature_self_cast".equals(signature.name()) ? 4 : 5;
            this.applyPose(style, ageInTicks, weight, windup, 1.0f - recovery);
            this.f_102809_.m_104315_(this.f_102808_);
            this.f_103378_.m_104315_(this.f_102810_);
            this.f_103374_.m_104315_(this.f_102812_);
            this.f_103375_.m_104315_(this.f_102811_);
            this.f_103376_.m_104315_(this.f_102814_);
            this.f_103377_.m_104315_(this.f_102813_);
            return;
        }
        BossActionState nativeAction = entity.nativeAction();
        if (nativeAction.active()) {
            float age = nativeAction.elapsed(ageInTicks - (float)entity.f_19797_);
            boolean roots = "roots".equals(nativeAction.name());
            float release = roots ? 12.0f : (float)("melee".equals(nativeAction.name()) ? 6 : 8);
            float windup = Mth.m_14036_((float)(age / release), (float)0.0f, (float)1.0f);
            float recovery = Mth.m_14036_((float)(((float)nativeAction.duration() - age) / 8.0f), (float)0.0f, (float)1.0f);
            this.applyPose(roots ? 3 : 2, ageInTicks, recovery, windup, age >= release ? recovery : 0.0f);
            this.f_102809_.m_104315_(this.f_102808_);
            this.f_103378_.m_104315_(this.f_102810_);
            this.f_103374_.m_104315_(this.f_102812_);
            this.f_103375_.m_104315_(this.f_102811_);
            this.f_103376_.m_104315_(this.f_102814_);
            this.f_103377_.m_104315_(this.f_102813_);
            return;
        }
        if (!entity.m_6084_()) {
            return;
        }
        CastPoseMemory memory = this.castPoses.computeIfAbsent(entity, ignored -> new CastPoseMemory());
        boolean casting = entity.isCasting();
        if (!casting && memory.wasCasting) {
            memory.releaseAge = ageInTicks;
        }
        memory.wasCasting = casting;
        float after = Mth.m_14036_((float)(1.0f - (ageInTicks - memory.releaseAge) / 8.0f), (float)0.0f, (float)1.0f);
        if (!casting && after <= 0.0f) {
            return;
        }
        float phase = 1.0f;
        if (casting) {
            memory.style = SayaPlayerModel.styleOf(entity);
            MagicData magic = entity.getMagicData();
            float elapsed = (float)(magic.getCastDuration() - magic.getCastDurationRemaining()) + ageInTicks - (float)entity.f_19797_;
            phase = Mth.m_14036_((float)(elapsed / 6.0f), (float)0.0f, (float)1.0f);
        }
        float blend = casting ? phase * phase * (3.0f - 2.0f * phase) : after;
        this.applyPose(memory.style, ageInTicks, blend, phase, casting ? 0.0f : after);
        this.f_102809_.m_104315_(this.f_102808_);
        this.f_103378_.m_104315_(this.f_102810_);
        this.f_103374_.m_104315_(this.f_102812_);
        this.f_103375_.m_104315_(this.f_102811_);
        this.f_103376_.m_104315_(this.f_102814_);
        this.f_103377_.m_104315_(this.f_102813_);
    }

    private void applyPose(int style, float age, float b, float phase, float after) {
        float k = b * Mth.m_14179_((float)phase, (float)0.45f, (float)1.0f);
        float bob = Mth.m_14031_((float)(age * 0.18f)) * 0.05f;
        float sway = Mth.m_14031_((float)(age * 0.08f)) * 0.06f;
        float pulse = Mth.m_14031_((float)(age * 0.35f)) * 0.07f;
        float push = after * 0.32f;
        float aFwdR = 2.3f;
        float aInR = 0.3f;
        float aFwdL = 2.3f;
        float aInL = 0.3f;
        float bodyX = 0.07f;
        float bodyY = 0.0f;
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
                aFwdR = 1.58f + after * 0.22f;
                aInR = -0.06f;
                aFwdL = 0.42f;
                aInL = 0.14f;
                bodyX = 0.05f;
                bodyY = -0.22f;
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
            case 5: {
                aFwdR = 2.72f + bob;
                aInR = -0.46f + sway;
                aFwdL = 2.72f - bob;
                aInL = -0.46f - sway;
                bodyX = -0.05f;
                bodyY = sway * 1.4f;
                headX = -0.34f;
                legR = 0.14f;
                legL = -0.14f;
                break;
            }
            default: {
                aFwdR = 2.3f + bob + push;
                aInR = 0.3f + sway;
                aFwdL = 2.3f - bob - push;
                aInL = 0.3f + sway;
            }
        }
        if (style != 2) {
            aFwdR += push;
            aFwdL += push;
        }
        this.arm(this.f_102811_, true, aFwdR += bob * 0.6f, aInR + sway * 0.5f, k);
        this.arm(this.f_102812_, false, aFwdL -= bob * 0.6f, aInL + sway * 0.5f, k);
        this.f_102810_.f_104203_ = Mth.m_14179_((float)k, (float)this.f_102810_.f_104203_, (float)(bodyX + after * 0.1f));
        this.f_102810_.f_104204_ = Mth.m_14179_((float)k, (float)this.f_102810_.f_104204_, (float)bodyY);
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

    private static int styleOf(TeaTreeBossEntity entity) {
        try {
            boolean ritualish;
            MagicData md = entity.getMagicData();
            if (md == null) {
                return 0;
            }
            String id = md.getCastingSpellId();
            CastType type = md.getCastType();
            String low = id == null ? "" : id.toLowerCase(Locale.ROOT);
            boolean summonish = low.contains("summon") || low.contains("ward") || low.contains("raise") || low.contains("bud") || low.contains("root") || low.contains("tree");
            boolean selfish = low.contains("heal") || low.contains("absorption") || low.contains("haste") || low.contains("charge") || low.contains("shield") || low.contains("renew");
            boolean bl = ritualish = low.contains("storm") || low.contains("vortex") || low.contains("barrage") || low.contains("starfall") || low.contains("black_hole") || low.contains("wall") || low.contains("sanctuary") || low.contains("procession") || low.contains("lattice");
            if (type == CastType.CONTINUOUS) {
                return 1;
            }
            if (summonish) {
                return 3;
            }
            if (selfish) {
                return 4;
            }
            if (ritualish) {
                return 5;
            }
            if (type == CastType.LONG) {
                return 0;
            }
            return 2;
        }
        catch (Throwable ignored) {
            return 0;
        }
    }

    private static final class CastPoseMemory {
        private boolean wasCasting;
        private float releaseAge = -1000.0f;
        private int style = 0;

        private CastPoseMemory() {
        }
    }
}

