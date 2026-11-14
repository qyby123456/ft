/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.doramotion.client;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.doramotion.DoraMotionAccess;
import net.farmingtales.ftboss.doramotion.DoraMotionCurves;
import net.farmingtales.ftboss.doramotion.DoraMotionRig;
import net.farmingtales.ftboss.doramotion.DoraMotionSnapshot;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.level.Level;

public final class DoraAnimator {
    private static final DoraMotionCurves CURVES = DoraAnimator.load();
    private static final Map<Integer, Frame> FRAMES = new LinkedHashMap<Integer, Frame>();
    private static Level world;
    private static long lastTime;
    private static long lastCleanup;

    private DoraAnimator() {
    }

    private static DoraMotionCurves load() {
        DoraMotionCurves doraMotionCurves;
        block9: {
            InputStream inputStream = DoraAnimator.class.getClassLoader().getResourceAsStream("assets/ftboss/dora_motion/motions.json");
            try {
                if (inputStream == null) {
                    throw new IllegalStateException("Dora motion resource missing");
                }
                doraMotionCurves = new DoraMotionCurves(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
                if (inputStream == null) break block9;
            }
            catch (Throwable throwable) {
                try {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Exception exception) {
                    FTBossMod.LOGGER.warn("[DoraMotion] Keeping original poses: {}", (Object)exception.toString());
                    return null;
                }
            }
            inputStream.close();
        }
        return doraMotionCurves;
    }

    private static double age(CompoundTag compoundTag, String string, long l, float f) {
        return compoundTag.m_128441_(string) ? (double)((float)(l - compoundTag.m_128454_(string)) + f) : 100000.0;
    }

    private static double[][] copy(double[][] dArray) {
        double[][] dArrayArray = new double[6][];
        for (int i = 0; i < 6; ++i) {
            dArrayArray[i] = (double[])dArray[i].clone();
        }
        return dArrayArray;
    }

    public static void apply(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity, float f, float f2, float f3, float f4, float f5) {
        boolean bl;
        Frame frame2;
        Object object;
        double d;
        if (CURVES == null || easterBossEntity.getVariant() != 2 || easterBossEntity.isMini() || !easterBossEntity.m_6084_() || easterBossEntity.m_21525_() || easterBossEntity.m_20159_() || !(easterBossEntity instanceof DoraMotionAccess)) {
            return;
        }
        DoraMotionAccess doraMotionAccess = easterBossEntity;
        long l = easterBossEntity.m_9236_().m_46467_();
        if (world != easterBossEntity.m_9236_() || l < lastTime) {
            FRAMES.clear();
            world = easterBossEntity.m_9236_();
            lastCleanup = l;
        }
        lastTime = l;
        CompoundTag compoundTag = doraMotionAccess.doraMotionState();
        if (compoundTag.m_128456_()) {
            return;
        }
        float f6 = Math.max(0.0f, Math.min(1.0f, f3 - (float)easterBossEntity.f_19797_));
        boolean bl2 = easterBossEntity.m_5737_() == HumanoidArm.LEFT;
        int n = bl2 ? 3 : 2;
        int n2 = bl2 ? 2 : 3;
        double[][] dArray = CURVES.sample("ready", f3 % 40.0f / 40.0f, bl2);
        double d2 = DoraAnimator.age(compoundTag, "shotAt", l, f6);
        double d3 = DoraAnimator.age(compoundTag, "fireAt", l, f6);
        boolean bl3 = DoraAnimator.age(compoundTag, "sampleAt", l, f6) <= 8.0;
        boolean bl4 = bl3 && compoundTag.m_128471_("sniper");
        boolean bl5 = bl3 && compoundTag.m_128451_("reload") > 0;
        boolean bl6 = bl3 && compoundTag.m_128451_("shots") > 0 || d2 >= 0.0 && d2 < 4.0;
        Object object2 = "ready";
        double d4 = 0.0;
        int n3 = Math.max(1, compoundTag.m_128451_("recovery"));
        if (bl4 && d3 >= 0.0 && d3 < (double)n3) {
            object2 = "sniper_fire";
            dArray = CURVES.sample((String)object2, d3 / (double)n3, bl2);
            d4 = 1.0 - DoraMotionCurves.smooth((d3 / (double)n3 - 0.42) / 0.58);
        } else if (bl4 && compoundTag.m_128441_("sniperAt")) {
            object2 = "sniper_charge";
            var28_24 = DoraAnimator.age(compoundTag, "sniperAt", l, f6) / (double)Math.max(1, compoundTag.m_128451_("charge"));
            dArray = CURVES.sample((String)object2, var28_24, bl2);
            d4 = DoraMotionCurves.smooth(var28_24 / 0.28);
        } else if (bl5) {
            object2 = "reload";
            var28_24 = DoraMotionSnapshot.reloadProgress(compoundTag, l, f6);
            dArray = CURVES.sample((String)object2, var28_24, bl2);
            d4 = DoraMotionCurves.smooth((var28_24 - 0.63) / 0.25) * (1.0 - DoraMotionCurves.smooth((var28_24 - 0.86) / 0.14));
        } else if (bl6) {
            object2 = "suppress";
            dArray = CURVES.sample((String)object2, 0.5, bl2);
            d4 = 1.0;
            if (d2 >= 0.0 && d2 < 4.0) {
                var28_24 = Math.exp(-d2 * 1.2);
                double[] dArray2 = dArray[n];
                dArray2[0] = dArray2[0] - 5.0 * var28_24;
                double[] dArray3 = dArray[1];
                dArray3[0] = dArray3[0] - 1.4 * var28_24;
            }
        }
        CompoundTag compoundTag2 = compoundTag.m_128469_("spell");
        String string = compoundTag2.m_128461_("id");
        if (string.equals("arcane_fusillade") || string.equals("breach_shell") || string.equals("prism_battery")) {
            double d5;
            double d6;
            int n4 = compoundTag2.m_128451_("phase");
            int n5 = Math.max(1, compoundTag2.m_128451_("duration"));
            d = DoraAnimator.age(compoundTag2, "start", l, f6);
            double d7 = DoraAnimator.age(compoundTag2, "stop", l, f6);
            double d8 = d6 = n4 == 0 ? d / (double)n5 : Math.max(0.0, (double)(compoundTag2.m_128454_("stop") - compoundTag2.m_128454_("start")) / (double)n5);
            double d9 = n4 == 0 && d >= 0.0 && d < (double)(n5 + 4) ? 1.0 : (n4 > 0 && d7 >= 0.0 ? 1.0 - DoraMotionCurves.smooth(d7 / (double)(n4 == 2 ? 4 : 10)) : (d5 = 0.0));
            if (d5 > 0.0) {
                object = CURVES.sample(string, d6, bl2);
                if (n4 == 1) {
                    double d10 = Math.exp(-Math.max(0.0, d7) * 0.65) * (double)(string.equals("breach_shell") ? 13 : 5);
                    double[] dArray4 = object[n];
                    dArray4[0] = dArray4[0] - d10;
                    double[] dArray5 = object[1];
                    dArray5[0] = dArray5[0] - d10 * 0.25;
                }
                if (((String)object2).equals("ready")) {
                    DoraMotionCurves.blend(dArray, object, d5);
                    object2 = string + (n4 == 1 ? ":release" : (n4 == 2 ? ":cancel" : ""));
                    d4 = d5 * DoraMotionCurves.smooth(d6 / 0.65);
                } else {
                    for (int i = 0; i < 3; ++i) {
                        double[] dArray6 = dArray[1];
                        int n6 = i;
                        dArray6[n6] = dArray6[n6] + (object[1][i] - dArray[1][i]) * d5 * 0.15;
                    }
                }
            }
        }
        DoraAnimator.age(compoundTag, "spearAt", l, f6);
        double d11 = 100000.0;
        d = (double)Math.min(1.0f, Math.max(0.0f, f2)) * (bl5 ? 1.0 : 0.35);
        double[] dArray7 = dArray[4];
        dArray7[0] = dArray7[0] + Math.cos((double)f * 0.6662) * 45.0 * d;
        double[] dArray8 = dArray[5];
        dArray8[0] = dArray8[0] - Math.cos((double)f * 0.6662) * 45.0 * d;
        double[] dArray9 = dArray[0];
        dArray9[0] = dArray9[0] + (double)Math.max(-25.0f, Math.min(25.0f, f5));
        double[] dArray10 = dArray[0];
        dArray10[1] = dArray10[1] + (double)Math.max(-25.0f, Math.min(25.0f, f4));
        double[] dArray11 = dArray[n];
        dArray11[0] = dArray11[0] + (double)Math.max(-15.0f, Math.min(15.0f, f5)) * d4;
        double[] dArray12 = dArray[n];
        dArray12[1] = dArray12[1] + (double)Math.max(-15.0f, Math.min(15.0f, f4)) * d4;
        if (l - lastCleanup >= 20L) {
            FRAMES.values().removeIf(frame -> l - frame.lastSeen > 200L);
            lastCleanup = l;
        }
        if ((frame2 = FRAMES.get(easterBossEntity.m_19879_())) == null || !easterBossEntity.m_20148_().equals(frame2.owner)) {
            frame2 = new Frame();
            frame2.owner = easterBossEntity.m_20148_();
            FRAMES.put(easterBossEntity.m_19879_(), frame2);
        }
        frame2.lastSeen = l;
        boolean bl7 = bl = d11 >= 0.0 && d11 < 4.0;
        if (((String)object2).equals("ready") && !bl && frame2.identity.equals("ready") && l - frame2.changedAt >= 3L) {
            frame2.current = null;
            frame2.prior = null;
            return;
        }
        if (!((String)object2).equals(frame2.identity)) {
            frame2.prior = frame2.current == null ? DoraAnimator.copy(dArray) : DoraAnimator.copy(frame2.current);
            frame2.identity = object2;
            frame2.changedAt = l;
        }
        if (frame2.prior != null && !((String)object2).equals("sniper_fire") && !((String)object2).endsWith(":release")) {
            double[][] dArray13 = DoraAnimator.copy(frame2.prior);
            DoraMotionCurves.blend(dArray13, dArray, DoraMotionCurves.smooth(((float)(l - frame2.changedAt) + f6) / 3.0f));
            dArray = dArray13;
        }
        frame2.current = DoraAnimator.copy(dArray);
        if (bl) {
            double[][] dArray14 = CURVES.sample("spear", d11 / 4.0, bl2);
            dArray[n2] = dArray14[n2];
            for (int i = 0; i < 3; ++i) {
                double[] dArray15 = dArray[1];
                int n7 = i;
                dArray15[n7] = dArray15[n7] + (dArray14[1][i] - dArray[1][i]) * 0.3;
            }
            double d12 = 1.0 - DoraMotionCurves.smooth(d11 / 4.0);
            double[] dArray16 = dArray[n2];
            dArray16[0] = dArray16[0] + (double)Math.max(-20.0f, Math.min(20.0f, f5)) * d12;
            double[] dArray17 = dArray[n2];
            dArray17[1] = dArray17[1] + (double)Math.max(-20.0f, Math.min(20.0f, f4)) * d12;
        }
        if (FRAMES.size() > 64) {
            FRAMES.remove(FRAMES.keySet().iterator().next());
        }
        DoraMotionRig.Bone[] boneArray = DoraMotionRig.solve(dArray, easterBossEntity.m_20096_() && !easterBossEntity.m_20159_() && (double)f2 < 0.02, n, d4);
        ModelPart[] modelPartArray = new ModelPart[]{easterBossModel.f_102808_, easterBossModel.f_102810_, easterBossModel.f_102811_, easterBossModel.f_102812_, easterBossModel.f_102813_, easterBossModel.f_102814_};
        for (int i = 0; i < 6; ++i) {
            ModelPart modelPart = modelPartArray[i];
            object = boneArray[i].position();
            float[] fArray = boneArray[i].rotation();
            modelPart.f_104200_ = (float)object[0];
            modelPart.f_104201_ = (float)object[1];
            modelPart.f_104202_ = (float)object[2];
            modelPart.f_104203_ = fArray[0];
            modelPart.f_104204_ = fArray[1];
            modelPart.f_104205_ = fArray[2];
        }
        easterBossModel.f_102809_.m_104315_(easterBossModel.f_102808_);
        easterBossModel.f_103378_.m_104315_(easterBossModel.f_102810_);
        easterBossModel.f_103375_.m_104315_(easterBossModel.f_102811_);
        easterBossModel.f_103374_.m_104315_(easterBossModel.f_102812_);
        easterBossModel.f_103377_.m_104315_(easterBossModel.f_102813_);
        easterBossModel.f_103376_.m_104315_(easterBossModel.f_102814_);
    }

    private static final class Frame {
        UUID owner;
        long lastSeen;
        String identity = "";
        long changedAt;
        double[][] prior;
        double[][] current;

        private Frame() {
        }
    }
}

