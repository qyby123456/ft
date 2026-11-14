/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.model.geom.ModelPart
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.server.packs.resources.ResourceManager
 *  net.minecraft.server.packs.resources.ResourceManagerReloadListener
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.minecraftforge.client.event.RegisterClientReloadListenersEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.client.xuanlanbody;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.body.BodyCueAccess;
import net.farmingtales.ftboss.body.BodyMotion;
import net.farmingtales.ftboss.body.BodyRig;
import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.client.xuanlanbody.BodyModelAngles;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.XuanlanGunPoseMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT}, bus=Mod.EventBusSubscriber.Bus.MOD)
public final class BodyAnimator {
    private static final ResourceLocation MOTIONS = new ResourceLocation("ftboss", "xuanlan_body/motions.json");
    private static BodyMotion.Library library;
    private static boolean attempted;
    private static Object world;
    private static double lastClock;
    private static long lastSweep;
    private static final Map<UUID, Transition> STATES;

    private BodyAnimator() {
    }

    private static void load(ResourceManager resourceManager) {
        attempted = true;
        try (InputStream inputStream = resourceManager.m_215595_(MOTIONS);){
            library = BodyMotion.read(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
            STATES.clear();
        }
        catch (Exception exception) {
            FTBossMod.LOGGER.error("[FTBoss][XuanlanBody] Animation library rejected; retaining last valid library", (Throwable)exception);
        }
    }

    @SubscribeEvent
    public static void register(RegisterClientReloadListenersEvent registerClientReloadListenersEvent) {
        registerClientReloadListenersEvent.registerReloadListener((PreparableReloadListener)((ResourceManagerReloadListener)BodyAnimator::load));
    }

    public static void apply(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity, float f, float f2, float f3, float f4, float f5) {
        BodyMotion.Clip clip;
        double d;
        String string;
        if (easterBossEntity.getVariant() != 6 || !easterBossEntity.m_6084_() || easterBossEntity.isManorIntroHidden() || easterBossEntity.m_20159_() || easterBossEntity.m_5803_()) {
            STATES.remove(easterBossEntity.m_20148_());
            return;
        }
        if (!attempted) {
            BodyAnimator.load(Minecraft.m_91087_().m_91098_());
        }
        if (library == null) {
            return;
        }
        float f6 = Mth.m_14036_((float)(f3 - (float)easterBossEntity.f_19797_), (float)0.0f, (float)1.0f);
        double d2 = (float)easterBossEntity.m_9236_().m_46467_() + f6;
        if (world != easterBossEntity.m_9236_() || d2 + 1.0 < lastClock) {
            STATES.clear();
            world = easterBossEntity.m_9236_();
            lastClock = d2;
            lastSweep = Long.MIN_VALUE;
        }
        lastClock = Math.max(lastClock, d2);
        long l = easterBossEntity.m_9236_().m_46467_();
        if (lastSweep != l) {
            STATES.values().removeIf(transition -> d2 - transition.touched > 200.0 || transition.owner == null || transition.owner.m_213877_());
            lastSweep = l;
        }
        CompoundTag compoundTag = ((BodyCueAccess)easterBossEntity).ftboss$bodyRead();
        CompoundTag compoundTag2 = compoundTag.m_128469_("xuanlanBody");
        CompoundTag compoundTag3 = easterBossEntity.getSkillCue();
        String string2 = compoundTag2.m_128461_("id");
        int n = compoundTag2.m_128451_("windup");
        long l2 = compoundTag2.m_128454_("start");
        double d3 = d2 - (double)l2;
        boolean bl = easterBossEntity.getChallengeDifficulty().acceleratedTicks(30) < 30;
        float f7 = f5;
        float f8 = f4;
        if (string2.isEmpty()) {
            string2 = "ready";
            d3 = f3;
            n = 0;
            l2 = 0L;
        }
        if (string2.equals("ready")) {
            double d4;
            compoundTag.m_128471_("cast");
            if (!true && compoundTag.m_128451_("duration") > 0 && (d4 = d2 - (double)compoundTag.m_128454_("start")) >= 0.0 && d4 < (double)(compoundTag.m_128451_("duration") + 8)) {
                string2 = "contact";
                d3 = d4;
                n = compoundTag.m_128451_("duration");
                l2 = compoundTag.m_128454_("start");
            }
        }
        if (!STATES.containsKey(easterBossEntity.m_20148_()) && STATES.size() >= 64) {
            STATES.remove(STATES.keySet().iterator().next());
        }
        Transition transition2 = STATES.computeIfAbsent(easterBossEntity.m_20148_(), uUID -> new Transition());
        if (transition2.owner != easterBossEntity) {
            transition2 = new Transition();
            transition2.owner = easterBossEntity;
            STATES.put(easterBossEntity.m_20148_(), transition2);
        }
        if (!(string = string2 + ":" + l2).equals(transition2.aimToken)) {
            transition2.aimToken = string;
            transition2.hasAim = false;
        }
        float f9 = Mth.m_14189_((float)f6, (float)easterBossEntity.f_20884_, (float)easterBossEntity.f_20883_);
        if (compoundTag2.m_128471_("lockedAim")) {
            transition2.hasAim = true;
            transition2.pitch = compoundTag2.m_128459_("aimPitch");
            transition2.worldYaw = compoundTag2.m_128459_("aimYaw");
        }
        if (string2.equals("beam") || string2.equals("combo")) {
            double d5;
            double d6;
            double d7 = d2 - (double)compoundTag3.m_128454_("start");
            if (compoundTag3.m_128471_("beam") && d7 >= 0.0 && d7 < (double)compoundTag3.m_128451_("duration") && (d6 = compoundTag3.m_128459_("endX") - compoundTag3.m_128459_("x")) * d6 + (d = compoundTag3.m_128459_("endY") - compoundTag3.m_128459_("y")) * d + (d5 = compoundTag3.m_128459_("endZ") - compoundTag3.m_128459_("z")) * d5 > 1.0E-6) {
                f5 = (float)Math.toDegrees(XuanlanGunPoseMath.beamPitch(d6, d, d5));
                f4 = (float)Math.toDegrees(XuanlanGunPoseMath.beamYaw(d6, d5, f9));
                transition2.hasAim = true;
                transition2.pitch = f5;
                transition2.worldYaw = Math.toDegrees(-Math.atan2(d6, d5));
            }
        }
        if (transition2.hasAim) {
            f5 = (float)transition2.pitch;
            f4 = Mth.m_14177_((float)((float)transition2.worldYaw - f9));
        }
        try {
            clip = BodyMotion.clip(library, string2);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            clip = BodyMotion.clip(library, "ready");
            string2 = "ready";
            d3 = f3;
            n = 0;
            l2 = 0L;
        }
        BodyMotion.Pose pose = BodyMotion.sample(clip, d3, bl, n, f5, f4, easterBossEntity.m_5737_() == HumanoidArm.LEFT);
        if (!clip.loop && d3 > pose.life()) {
            string2 = "ready";
            l2 = 0L;
            transition2.hasAim = false;
            pose = BodyMotion.sample(BodyMotion.clip(library, string2), f3, bl, 0, f7, f8, easterBossEntity.m_5737_() == HumanoidArm.LEFT);
        }
        BodyRig.Bone[] boneArray = BodyRig.solve(pose, f, f2, f3);
        String string3 = string2 + ":" + l2;
        if (!string3.equals(transition2.token)) {
            transition2.token = string3;
            transition2.changed = d2;
            BodyRig.Bone[] boneArray2 = transition2.from = transition2.last == null ? boneArray : transition2.last;
        }
        BodyRig.Bone[] boneArray3 = (d = BodyMotion.smooth((d2 - transition2.changed) / 4.0)) >= 1.0 ? boneArray : BodyRig.blend(transition2.from, boneArray, d, !pose.air() && (double)f2 <= 0.1);
        transition2.last = boneArray3;
        transition2.touched = d2;
        ModelPart[] modelPartArray = new ModelPart[]{easterBossModel.f_102808_, easterBossModel.f_102810_, easterBossModel.f_102811_, easterBossModel.f_102812_, easterBossModel.f_102813_, easterBossModel.f_102814_};
        for (int i = 0; i < modelPartArray.length; ++i) {
            ModelPart modelPart = modelPartArray[i];
            BodyRig.Bone bone = boneArray3[i];
            Vector3f vector3f = BodyModelAngles.zyx(bone.rotation(), new Vector3f());
            modelPart.f_104200_ = bone.position().x;
            modelPart.f_104201_ = bone.position().y;
            modelPart.f_104202_ = bone.position().z;
            modelPart.f_104203_ = vector3f.x;
            modelPart.f_104204_ = vector3f.y;
            modelPart.f_104205_ = vector3f.z;
        }
        easterBossModel.f_102809_.m_104315_(easterBossModel.f_102808_);
        easterBossModel.f_103378_.m_104315_(easterBossModel.f_102810_);
        easterBossModel.f_103375_.m_104315_(easterBossModel.f_102811_);
        easterBossModel.f_103374_.m_104315_(easterBossModel.f_102812_);
        easterBossModel.f_103377_.m_104315_(easterBossModel.f_102813_);
        easterBossModel.f_103376_.m_104315_(easterBossModel.f_102814_);
    }

    static {
        lastClock = -1.0;
        lastSweep = Long.MIN_VALUE;
        STATES = new LinkedHashMap<UUID, Transition>();
    }

    private static final class Transition {
        EasterBossEntity owner;
        String token;
        String aimToken;
        double changed;
        double touched;
        double pitch;
        double worldYaw;
        boolean hasAim;
        BodyRig.Bone[] from;
        BodyRig.Bone[] last;

        private Transition() {
        }
    }

    @Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
    public static final class Cleanup {
        @SubscribeEvent
        public static void logout(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
            STATES.clear();
            world = null;
            lastClock = -1.0;
        }
    }
}

