/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  org.joml.Quaternionf
 */
package net.farmingtales.ftboss.client.qizaiarcane;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneAssets;
import net.farmingtales.ftboss.client.qizaiarcane.ArcanePainter;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneRig;
import net.farmingtales.ftboss.client.qizaiarcane.ArcaneSigilPolicy;
import net.farmingtales.ftboss.client.render.QizaiContrast;
import net.farmingtales.ftboss.client.render.QizaiMagicVisual;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.QizaiSigilEntity;
import net.farmingtales.ftboss.util.QizaiGeometry;
import net.farmingtales.ftboss.util.QizaiRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class ArcaneSigilGlow {
    private static final Map<String, Trace> traces = new LinkedHashMap<String, Trace>();
    private static final ArrayList<Vec3> shown = new ArrayList();
    private static ClientLevel scene;
    private static double lastTime;
    private static ArcaneRig.Effect material;
    private static ArcaneRig.Effect ornament;

    private ArcaneSigilGlow() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void originalPattern(QizaiSigilEntity qizaiSigilEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        poseStack.m_85836_();
        try {
            VertexConsumer vertexConsumer = multiBufferSource.m_6299_(QizaiContrast.TYPE);
            QizaiGeometry.sigil(QizaiRules.radius(qizaiSigilEntity.kind()), qizaiSigilEntity.progress(f), qizaiSigilEntity.released(), Minecraft.m_91087_().f_91066_.m_231929_().m_231551_() != ParticleStatus.ALL, QizaiMagicVisual.sink(poseStack, vertexConsumer));
        }
        finally {
            poseStack.m_85849_();
        }
    }

    private static boolean ownerValid(LivingEntity livingEntity) {
        Mob mob;
        return livingEntity.m_6084_() && !livingEntity.m_213877_() && !livingEntity.m_5833_() && livingEntity.m_9236_() == scene && (!(livingEntity instanceof Mob) || !(mob = (Mob)livingEntity).m_21525_());
    }

    private static boolean pointValid(Vec3 vec3) {
        return Double.isFinite(vec3.f_82479_) && Double.isFinite(vec3.f_82480_) && Double.isFinite(vec3.f_82481_);
    }

    private static void remember(String string, Entity entity, LivingEntity livingEntity, Vec3 vec3, double d, int n, int n2, boolean bl, double d2) {
        if (!ArcaneSigilGlow.pointValid(vec3) || !ArcaneSigilPolicy.retain(d2, d, n, true)) {
            return;
        }
        if (traces.containsKey(string)) {
            return;
        }
        if (traces.size() >= 96) {
            traces.remove(traces.keySet().iterator().next());
        }
        traces.put(string, new Trace(entity, livingEntity, livingEntity.m_20182_(), vec3, d, n, n2, bl));
    }

    private static boolean active(Trace trace) {
        if (trace.source().m_213877_()) {
            return false;
        }
        if (trace.field()) {
            QizaiSigilEntity qizaiSigilEntity;
            Entity entity = trace.source();
            return entity instanceof QizaiSigilEntity && (qizaiSigilEntity = (QizaiSigilEntity)entity).released() && qizaiSigilEntity.kind() == trace.kind() && qizaiSigilEntity.duration() == trace.duration();
        }
        Entity entity = trace.source();
        if (!(entity instanceof EasterBossEntity)) {
            return false;
        }
        EasterBossEntity easterBossEntity = (EasterBossEntity)entity;
        entity = easterBossEntity.getSkillCue();
        return entity.m_128451_("qizai") == trace.kind() && entity.m_128471_("released") && entity.m_128454_("start") == Math.round(trace.start()) && entity.m_128451_("duration") == trace.duration();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static int show(Vec3 vec3, double d, float f, RenderLevelStageEvent renderLevelStageEvent, int n) {
        Vec3 vec322;
        if (n <= 0 || f <= 0.003f || !ArcaneSigilGlow.pointValid(vec3)) {
            return 0;
        }
        Vec3 vec33 = renderLevelStageEvent.getCamera().m_90583_();
        if (vec3.m_82557_(vec33) > 4096.0 || !renderLevelStageEvent.getFrustum().m_113029_(new AABB(vec3, vec3).m_82400_(1.8))) {
            return 0;
        }
        for (Vec3 vec322 : shown) {
            if (!(vec322.m_82557_(vec3) < 0.04)) continue;
            return 0;
        }
        Vec3 vec34 = vec3.m_82520_(0.0, 0.14, 0.0);
        vec322 = renderLevelStageEvent.getPoseStack();
        vec322.m_85836_();
        try {
            vec322.m_85837_(vec34.f_82479_ - vec33.f_82479_, vec34.f_82480_ - vec33.f_82480_, vec34.f_82481_ - vec33.f_82481_);
            vec322.m_85841_(1.4f, 0.5f, 1.4f);
            int n2 = ArcanePainter.scaled(ornament, "idle", d / 20.0 % 2.4, (PoseStack)vec322, (MultiBufferSource)Minecraft.m_91087_().m_91269_().m_110104_(), vec34, new Quaternionf(), true, false, true, Math.min(420, n), 0xF000F0, f, new Vec3(1.4, 0.5, 1.4));
            if (n2 > 0) {
                shown.add(vec3);
            }
            int n3 = n2;
            return n3;
        }
        finally {
            vec322.m_85849_();
        }
    }

    private static int collectBoss(EasterBossEntity easterBossEntity, double d, RenderLevelStageEvent renderLevelStageEvent, int n) {
        CompoundTag compoundTag = easterBossEntity.getSkillCue();
        int n2 = compoundTag.m_128451_("qizai");
        int n3 = compoundTag.m_128451_("duration");
        double d2 = compoundTag.m_128454_("start");
        if (n2 < 1 || n2 > 6 || !ArcaneSigilPolicy.valid(d, d2, n3) || d >= d2 + (double)n3) {
            return n;
        }
        boolean bl = compoundTag.m_128471_("released");
        ListTag listTag = compoundTag.m_128437_("marks", 10);
        int n4 = Math.min(5, listTag.size());
        for (int i = 0; i < Math.max(1, n4); ++i) {
            Vec3 vec3 = easterBossEntity.m_20318_(renderLevelStageEvent.getPartialTick());
            if (n4 > 0) {
                CompoundTag compoundTag2 = listTag.m_128728_(i);
                vec3 = new Vec3(compoundTag2.m_128459_("x"), compoundTag2.m_128459_("y"), compoundTag2.m_128459_("z"));
            }
            if (bl) {
                ArcaneSigilGlow.remember("boss:" + String.valueOf(easterBossEntity.m_20148_()) + ":" + n2 + ":" + (long)d2 + ":" + i, (Entity)easterBossEntity, (LivingEntity)easterBossEntity, vec3, d2, n3, n2, false, d);
                continue;
            }
            n -= ArcaneSigilGlow.show(vec3, d, 0.32f, renderLevelStageEvent, n);
        }
        return n;
    }

    @SubscribeEvent
    public static void unloaded(LevelEvent.Unload unload) {
        if (unload.getLevel() == scene) {
            traces.clear();
            shown.clear();
            scene = null;
            lastTime = Double.NaN;
            material = null;
            ornament = null;
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void draw(RenderLevelStageEvent renderLevelStageEvent) {
        Object object;
        Object object22;
        Object object32;
        if (renderLevelStageEvent.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.m_91087_();
        ClientLevel clientLevel = minecraft.f_91073_;
        if (clientLevel == null) {
            traces.clear();
            scene = null;
            lastTime = Double.NaN;
            return;
        }
        double d = (float)clientLevel.m_46467_() + renderLevelStageEvent.getPartialTick();
        if (scene != clientLevel || Double.isFinite(lastTime) && d < lastTime - 0.01) {
            traces.clear();
            scene = clientLevel;
        }
        lastTime = d;
        ArcaneRig.Effect effect = ArcaneAssets.get("qizai_resonance_emission");
        if (effect == null) {
            traces.clear();
            material = null;
            ornament = null;
            return;
        }
        if (material != effect) {
            traces.clear();
            material = effect;
            ornament = new ArcaneRig.Effect(effect.id(), effect.texture(), 0, 0.0, effect.bones().stream().filter(bone -> !bone.cue() && (bone.name().equals("emission_orbit_2") || bone.name().equals("emission_resonance_script") || bone.name().equals("emission_micro_sparks"))).toList());
        }
        traces.entrySet().removeIf(entry -> {
            Trace trace = (Trace)entry.getValue();
            return !ArcaneSigilGlow.ownerValid(trace.owner()) || !ArcaneSigilPolicy.retain(d, trace.start(), trace.duration(), ArcaneSigilGlow.active(trace));
        });
        shown.clear();
        int n = 2520;
        Vec3 vec3 = renderLevelStageEvent.getCamera().m_90583_();
        ArrayList<Object> arrayList = new ArrayList<Object>();
        for (Object object32 : clientLevel.m_104735_()) {
            if (!(object32.m_20238_(vec3) < 4096.0) || !(object32 instanceof QizaiSigilEntity) && (!(object32 instanceof EasterBossEntity) || ((EasterBossEntity)(object22 = (EasterBossEntity)object32)).getVariant() != 11)) continue;
            arrayList.add(object32);
        }
        arrayList.sort(Comparator.comparingDouble(entity -> entity.m_20238_(vec3)));
        int n2 = 0;
        for (Object object22 : arrayList) {
            EasterBossEntity easterBossEntity;
            if (n2++ >= 24) break;
            if (object22 instanceof QizaiSigilEntity) {
                LivingEntity livingEntity;
                Entity entity2;
                object = (QizaiSigilEntity)((Object)object22);
                if (object.kind() < 1 || object.kind() > 6 || !((entity2 = object.m_19749_()) instanceof LivingEntity) || !ArcaneSigilGlow.ownerValid(livingEntity = (LivingEntity)entity2) || object.m_213877_()) continue;
                if (object.released()) {
                    if (!(object.progress(renderLevelStageEvent.getPartialTick()) < 1.0)) continue;
                    ArcaneSigilGlow.remember("field:" + String.valueOf(object.m_20148_()), (Entity)object, livingEntity, object.m_20318_(renderLevelStageEvent.getPartialTick()), Math.round(d - object.progress(renderLevelStageEvent.getPartialTick()) * (double)object.duration()), object.duration(), object.kind(), true, d);
                    continue;
                }
                n -= ArcaneSigilGlow.show(object.m_20318_(renderLevelStageEvent.getPartialTick()), d, 0.32f, renderLevelStageEvent, n);
                continue;
            }
            if (!(object22 instanceof EasterBossEntity) || !ArcaneSigilGlow.ownerValid((LivingEntity)(easterBossEntity = (EasterBossEntity)object22))) continue;
            n = ArcaneSigilGlow.collectBoss(easterBossEntity, d, renderLevelStageEvent, n);
        }
        object32 = new ArrayList<Trace>(traces.values());
        ((ArrayList)object32).sort(Comparator.comparingDouble(trace -> trace.origin().m_82557_(vec3)));
        object22 = ((ArrayList)object32).iterator();
        while (object22.hasNext()) {
            object = (Trace)object22.next();
            if (!ArcaneSigilGlow.ownerValid(((Trace)object).owner()) || !(((Trace)object).owner().m_20182_().m_82557_(((Trace)object).ownerAnchor()) <= 1024.0)) continue;
            n -= ArcaneSigilGlow.show(((Trace)object).origin(), d, ArcaneSigilPolicy.opacity(d, ((Trace)object).start(), ((Trace)object).duration()), renderLevelStageEvent, n);
        }
        minecraft.m_91269_().m_110104_().m_109912_(RenderType.m_110470_((ResourceLocation)new ResourceLocation(ornament.texture())));
    }

    static {
        lastTime = Double.NaN;
    }

    private record Trace(Entity source, LivingEntity owner, Vec3 ownerAnchor, Vec3 origin, double start, int duration, int kind, boolean field) {
    }
}

