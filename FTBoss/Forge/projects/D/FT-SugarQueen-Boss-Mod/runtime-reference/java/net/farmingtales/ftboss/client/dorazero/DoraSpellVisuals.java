/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.PoseStack$Pose
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.server.packs.resources.ResourceManagerReloadListener
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ClientPlayerNetworkEvent$LoggingOut
 *  net.minecraftforge.client.event.RegisterClientReloadListenersEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 */
package net.farmingtales.ftboss.client.dorazero;

import com.google.gson.Gson;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.dorazero.DoraMotion;
import net.farmingtales.ftboss.client.dorazero.DoraTimeline;
import net.farmingtales.ftboss.client.render.FirstPersonSpellVisibility;
import net.farmingtales.ftboss.client.render.bloom.SpellBloomRenderer;
import net.farmingtales.ftboss.integ.spells.ManorIronSpells;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.skill.SpellVisuals;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class DoraSpellVisuals {
    public static final int FACE_BUDGET = 6000;
    private static final ResourceLocation ATLAS = new ResourceLocation("ftboss", "dora_zero/materials.png");
    private static final RenderType MATERIAL = RenderType.m_110470_((ResourceLocation)ATLAS);
    private static final RenderType EMISSIVE = RenderType.m_234338_((ResourceLocation)ATLAS);
    private static final DoraTimeline TIMELINE = new DoraTimeline();
    private static final Map<String, DoraMotion.Prepared> MODELS = new HashMap<String, DoraMotion.Prepared>();
    private static Object levelIdentity;
    private static boolean failed;

    private DoraSpellVisuals() {
    }

    public static boolean accepts(String string) {
        return string.equals("arcane_fusillade") || string.equals("breach_shell") || string.equals("prism_battery");
    }

    public static boolean accept(SpellVisualPacket spellVisualPacket) {
        int n;
        if (spellVisualPacket == null || !spellVisualPacket.valid() || !DoraSpellVisuals.accepts(spellVisualPacket.spell().id)) {
            return false;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (failed) {
            return false;
        }
        if (!minecraft.m_18695_()) {
            minecraft.execute(() -> DoraSpellVisuals.accept(spellVisualPacket));
            return true;
        }
        if (minecraft.f_91073_ == null || !minecraft.f_91073_.m_46472_().m_135782_().equals((Object)spellVisualPacket.dimension())) {
            return true;
        }
        DoraSpellVisuals.resetWorld(minecraft);
        Entity entity = minecraft.f_91073_.m_6815_(spellVisualPacket.ownerId());
        if (entity != null && !entity.m_20148_().equals(spellVisualPacket.owner())) {
            return true;
        }
        String string = spellVisualPacket.spell().id;
        String string2 = spellVisualPacket.phase().name();
        if (string2.equals("BEAM") && spellVisualPacket.start().m_82557_(spellVisualPacket.end()) < 1.0E-6) {
            return true;
        }
        double d = entity == null ? 1.0 : Math.max(0.5, Math.min(3.0, SpellVisuals.radiusScale(entity, string)));
        switch (string2) {
            case "CHARGE": {
                int n2;
                if (string.equals("arcane_fusillade")) {
                    n2 = 24;
                    break;
                }
                n2 = 32;
                break;
            }
            case "RELEASE": {
                int n2;
                if (string.equals("arcane_fusillade")) {
                    n2 = 52;
                    break;
                }
                if (string.equals("breach_shell")) {
                    n2 = 30;
                    break;
                }
                n2 = 68;
                break;
            }
            case "PULSE": {
                int n2 = 12;
                break;
            }
            default: {
                int n2 = n = 6;
            }
        }
        if (string2.equals("CHARGE") && entity instanceof LivingEntity) {
            String string3 = (LivingEntity)entity;
            ManorSpellRules.Kind kind = string.equals("arcane_fusillade") ? ManorSpellRules.Kind.ARCANE_FUSILLADE : (string.equals("breach_shell") ? ManorSpellRules.Kind.BREACH_SHELL : ManorSpellRules.Kind.PRISM_BATTERY);
            n = Math.max(1, ManorIronSpells.spell(kind).getEffectiveCastTime(5, (LivingEntity)string3));
        }
        long l = minecraft.f_91073_.m_46467_();
        TIMELINE.accept(new DoraTimeline.Message(spellVisualPacket.owner(), spellVisualPacket.ownerId(), string, string2, DoraSpellVisuals.array(spellVisualPacket.start()), DoraSpellVisuals.array(spellVisualPacket.end()), d, n, l));
        if (string2.equals("BEAM") && string.equals("arcane_fusillade")) {
            TIMELINE.accept(new DoraTimeline.Message(spellVisualPacket.owner(), spellVisualPacket.ownerId(), string, "PULSE", DoraSpellVisuals.array(spellVisualPacket.start()), DoraSpellVisuals.array(spellVisualPacket.end()), d, 7, l));
        }
        return true;
    }

    private static void resetWorld(Minecraft minecraft) {
        if (levelIdentity != minecraft.f_91073_) {
            TIMELINE.clear();
            levelIdentity = minecraft.f_91073_;
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent clientTickEvent) {
        if (clientTickEvent.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        DoraSpellVisuals.resetWorld(minecraft);
        if (minecraft.f_91073_ == null) {
            return;
        }
        TIMELINE.expire(minecraft.f_91073_.m_46467_());
        TIMELINE.active().removeIf(effect -> {
            Entity entity = minecraft.f_91073_.m_6815_(effect.message.ownerId());
            return entity != null && (!entity.m_20148_().equals(effect.message.owner()) || !entity.m_6084_());
        });
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut loggingOut) {
        TIMELINE.clear();
        levelIdentity = null;
    }

    private static DoraMotion.Prepared model(Minecraft minecraft, String string) {
        DoraMotion.Prepared prepared;
        if (failed) {
            return null;
        }
        if (MODELS.containsKey(string)) {
            return MODELS.get(string);
        }
        InputStreamReader inputStreamReader = new InputStreamReader(minecraft.m_91098_().m_215593_(new ResourceLocation("ftboss", "dora_zero/" + string + ".json")).m_215507_(), StandardCharsets.UTF_8);
        try {
            DoraMotion.Model model = (DoraMotion.Model)new Gson().fromJson((Reader)inputStreamReader, DoraMotion.Model.class);
            DoraMotion.Prepared prepared2 = DoraMotion.prepare(model);
            if (!string.equals(model.id())) {
                throw new IllegalArgumentException("Dora model id mismatch");
            }
            MODELS.put(string, prepared2);
            prepared = prepared2;
        }
        catch (Throwable throwable) {
            try {
                try {
                    inputStreamReader.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (Exception exception) {
                failed = true;
                FTBossMod.LOGGER.error("[FTBoss][DoraZero] Visual resources unavailable; gameplay unchanged", (Throwable)exception);
                return null;
            }
        }
        inputStreamReader.close();
        return prepared;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void render(RenderLevelStageEvent renderLevelStageEvent) {
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || TIMELINE.active().isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        if (minecraft.f_91073_ == null) {
            return;
        }
        DoraSpellVisuals.resetWorld(minecraft);
        boolean bl = SpellBloomRenderer.begin(renderLevelStageEvent, true);
        try {
            MultiBufferSource.BufferSource bufferSource = minecraft.m_91269_().m_110104_();
            Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
            Vector3f vector3f = renderLevelStageEvent.getCamera().m_253058_();
            double[] dArray = new double[]{vector3f.x(), vector3f.y(), vector3f.z()};
            boolean bl2 = minecraft.f_91066_.m_92176_().m_90612_();
            Vector4f vector4f = new Vector4f();
            double[] dArray2 = new double[3];
            double[] dArray3 = DoraSpellVisuals.array(vec3);
            int n = 0;
            block9: for (DoraTimeline.Effect effect : TIMELINE.active()) {
                double d;
                LivingEntity livingEntity;
                if (n >= 6000) break;
                DoraTimeline.Message message = effect.message;
                DoraMotion.Prepared prepared = DoraSpellVisuals.model(minecraft, message.spell());
                if (prepared == null) continue;
                DoraMotion.Model model = prepared.model();
                double d2 = (float)(minecraft.f_91073_.m_46467_() - effect.born) + renderLevelStageEvent.getPartialTick();
                double d3 = Math.min(1.0, d2 / (double)message.duration());
                boolean bl3 = message.phase().equals("BEAM");
                Vec3 vec32 = new Vec3(message.start()[0], message.start()[1], message.start()[2]);
                Vec3 vec33 = new Vec3(message.end()[0], message.end()[1], message.end()[2]);
                Entity entity = minecraft.f_91073_.m_6815_(message.ownerId());
                if (message.spell().equals("arcane_fusillade") && !bl3 && !message.phase().equals("PULSE") && entity instanceof LivingEntity) {
                    livingEntity = (LivingEntity)entity;
                    vec32 = livingEntity.m_20318_(renderLevelStageEvent.getPartialTick()).m_82520_(0.0, (double)livingEntity.m_20192_() - 1.3, 0.0);
                }
                if (!bl3 ? vec3.m_82557_(vec32) > 5184.0 : DoraSpellVisuals.segmentDistance(vec3, vec32, vec33) > 5184.0) continue;
                livingEntity = bl3 ? vec33.m_82546_(vec32) : vec33.m_82546_(new Vec3(message.start()[0], message.start()[1], message.start()[2])).m_82542_(1.0, 0.0, 1.0);
                double d4 = d = bl3 ? livingEntity.m_82553_() : 1.0;
                if (livingEntity.m_82556_() < 1.0E-6) {
                    livingEntity = new Vec3(0.0, 0.0, 1.0);
                }
                PoseStack poseStack = renderLevelStageEvent.getPoseStack();
                Quaternionf quaternionf = bl3 ? new Quaternionf().rotationTo((Vector3fc)new Vector3f(0.0f, 0.0f, 1.0f), (Vector3fc)new Vector3f((float)livingEntity.f_82479_, (float)livingEntity.f_82480_, (float)livingEntity.f_82481_).normalize()) : new Quaternionf().rotationY((float)Math.atan2(livingEntity.f_82479_, livingEntity.f_82481_));
                Matrix4f matrix4f = new Matrix4f().rotate((Quaternionfc)quaternionf);
                poseStack.m_85836_();
                try {
                    poseStack.m_85837_(vec32.f_82479_ - vec3.f_82479_, vec32.f_82480_ - vec3.f_82480_, vec32.f_82481_ - vec3.f_82481_);
                    poseStack.m_252781_(quaternionf);
                    block10: for (DoraMotion.Part part : prepared.phases().get(message.phase())) {
                        if (n >= 6000) continue block9;
                        DoraMotion.Pose pose = DoraMotion.sample(model.tracks().get(part.name()).get(message.phase()), d3);
                        if (pose.alpha() <= 0.006) continue;
                        double[] dArray4 = DoraMotion.pivotScale(part.scaleMode(), message.radius());
                        double[] dArray5 = DoraMotion.localScale(part.scaleMode(), message.radius());
                        if (bl3) {
                            dArray4 = new double[]{1.0, 1.0, d};
                            dArray5 = new double[]{Math.min(message.radius(), 1.4), Math.min(message.radius(), 1.4), d};
                        }
                        poseStack.m_85836_();
                        try {
                            float f = (float)((part.pivot()[0] + pose.position()[0]) * dArray4[0]);
                            float f2 = (float)((part.pivot()[1] + pose.position()[1]) * dArray4[1]);
                            float f3 = (float)((part.pivot()[2] + pose.position()[2]) * dArray4[2]);
                            if (part.scaleMode().equals("formation") && effect.pulseAt >= 0L) {
                                double d5 = Math.max(0.0, 1.0 - (double)((float)(minecraft.f_91073_.m_46467_() - effect.pulseAt) + renderLevelStageEvent.getPartialTick()) / 6.0);
                                double d6 = Math.max(0.001, Math.hypot(part.pivot()[0], part.pivot()[2]));
                                f += (float)(part.pivot()[0] / d6 * 0.055 * d5);
                                f3 += (float)(part.pivot()[2] / d6 * 0.055 * d5);
                            }
                            Quaternionf quaternionf2 = new Quaternionf().rotationZYX((float)Math.toRadians(pose.rotation()[2]), (float)Math.toRadians(pose.rotation()[1]), (float)Math.toRadians(pose.rotation()[0]));
                            float f4 = (float)(pose.scale()[0] * dArray5[0]);
                            float f5 = (float)(pose.scale()[1] * dArray5[1]);
                            float f6 = (float)(pose.scale()[2] * dArray5[2]);
                            poseStack.m_252880_(f, f2, f3);
                            poseStack.m_252781_(quaternionf2);
                            poseStack.m_85841_(f4, f5, f6);
                            Matrix4f matrix4f2 = new Matrix4f((Matrix4fc)matrix4f).translate(f, f2, f3).rotate((Quaternionfc)quaternionf2).scale(f4, f5, f6);
                            VertexConsumer vertexConsumer = bufferSource.m_6299_(part.material() >= 6 && part.material() <= 10 ? EMISSIVE : MATERIAL);
                            double d7 = pose.alpha() * (part.material() == 11 ? 0.74 : (part.name().equals("beam_body") ? 0.5 : 1.0));
                            for (DoraMotion.Face face : part.faces()) {
                                if (n++ >= 6000) continue block10;
                                DoraSpellVisuals.emit(vertexConsumer, poseStack.m_85850_(), matrix4f2, vec32, part, face, d7, vector4f, dArray2, dArray3, dArray, bl2);
                            }
                        }
                        finally {
                            poseStack.m_85849_();
                        }
                    }
                }
                finally {
                    poseStack.m_85849_();
                }
            }
            bufferSource.m_109912_(MATERIAL);
            bufferSource.m_109912_(EMISSIVE);
        }
        finally {
            SpellBloomRenderer.end(bl);
        }
    }

    private static void emit(VertexConsumer vertexConsumer, PoseStack.Pose pose, Matrix4f matrix4f, Vec3 vec3, DoraMotion.Part part, DoraMotion.Face face, double d, Vector4f vector4f, double[] dArray, double[] dArray2, double[] dArray3, boolean bl) {
        double[][] dArray4 = part.vertices();
        for (int i = 0; i < 4; ++i) {
            int n = Math.min(i, face.vertices().length - 1);
            double[] dArray5 = dArray4[face.vertices()[n]];
            vector4f.set((float)dArray5[0], (float)dArray5[1], (float)dArray5[2], 1.0f).mul((Matrix4fc)matrix4f);
            dArray[0] = (double)vector4f.x + vec3.f_82479_;
            dArray[1] = (double)vector4f.y + vec3.f_82480_;
            dArray[2] = (double)vector4f.z + vec3.f_82481_;
            double d2 = FirstPersonSpellVisibility.featherOpacity(dArray, dArray2, dArray3, bl);
            vertexConsumer.m_252986_(pose.m_252922_(), (float)dArray5[0], (float)dArray5[1], (float)dArray5[2]).m_85950_(1.0f, 1.0f, 1.0f, (float)(d * d2)).m_7421_((float)(face.uv()[n][0] / 256.0), (float)(face.uv()[n][1] / 256.0)).m_86008_(OverlayTexture.f_118083_).m_85969_(0xF000F0).m_252939_(pose.m_252943_(), (float)face.normal()[0], (float)face.normal()[1], (float)face.normal()[2]).m_5752_();
        }
    }

    private static double segmentDistance(Vec3 vec3, Vec3 vec32, Vec3 vec33) {
        Vec3 vec34 = vec33.m_82546_(vec32);
        double d = Math.max(0.0, Math.min(1.0, vec3.m_82546_(vec32).m_82526_(vec34) / Math.max(1.0E-6, vec34.m_82556_())));
        return vec3.m_82557_(vec32.m_82549_(vec34.m_82490_(d)));
    }

    private static double[] array(Vec3 vec3) {
        return new double[]{vec3.f_82479_, vec3.f_82480_, vec3.f_82481_};
    }

    @Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT}, bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Reload {
        @SubscribeEvent
        public static void register(RegisterClientReloadListenersEvent registerClientReloadListenersEvent) {
            registerClientReloadListenersEvent.registerReloadListener((PreparableReloadListener)((ResourceManagerReloadListener)resourceManager -> {
                MODELS.clear();
                failed = false;
                TIMELINE.clear();
            }));
        }
    }
}

