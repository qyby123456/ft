/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.ParticleStatus
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.level.LevelEvent$Unload
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.LinkedHashMap;
import java.util.Map;
import net.farmingtales.ftboss.client.CombatVisualEntities;
import net.farmingtales.ftboss.client.render.XuanlanGlow;
import net.farmingtales.ftboss.client.render.XuanlanWeaponVisual;
import net.farmingtales.ftboss.client.xuanlannative.NativeScene;
import net.farmingtales.ftboss.entity.PlayerMoonChakramEntity;
import net.farmingtales.ftboss.util.XuanlanTrailHistory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss", value={Dist.CLIENT})
public final class XuanlanTrailVisual {
    private static final Map<Integer, Trail> TRAILS = new LinkedHashMap<Integer, Trail>();
    private static Level currentLevel;

    private XuanlanTrailVisual() {
    }

    private static void useLevel(Level level) {
        if (currentLevel == level) {
            return;
        }
        TRAILS.clear();
        currentLevel = level;
    }

    /*
     * WARNING - void declaration
     */
    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft client = Minecraft.m_91087_();
        XuanlanTrailVisual.useLevel((Level)client.f_91073_);
        if (client.f_91073_ == null || client.m_91104_() || client.m_91288_() == null) {
            return;
        }
        Entity camera = client.m_91288_();
        TRAILS.values().removeIf(trail -> trail.entity.m_213877_() || trail.entity.m_9236_() != client.f_91073_ || client.f_91073_.m_6815_(trail.entity.m_19879_()) != trail.entity || trail.entity.m_20280_(camera) > 2304.0);
        for (Entity entity : CombatVisualEntities.get(client.f_91073_).flywheels()) {
            void entity2;
            if (NativeScene.ownsProjectile(entity) || entity2.m_213877_() || entity2.m_20280_(camera) > 2304.0) continue;
            Trail trail2 = TRAILS.get(entity2.m_19879_());
            if (trail2 == null) {
                if (TRAILS.size() >= 64) continue;
                trail2 = new Trail((Entity)entity2, new XuanlanTrailHistory());
                TRAILS.put(entity2.m_19879_(), trail2);
            }
            trail2.history.add(entity2.m_20185_(), entity2.m_20186_(), entity2.m_20189_(), client.f_91073_.m_46467_());
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() == currentLevel) {
            XuanlanTrailVisual.useLevel(null);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        int segments;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft client = Minecraft.m_91087_();
        XuanlanTrailVisual.useLevel((Level)client.f_91073_);
        if (client.f_91073_ == null || TRAILS.isEmpty()) {
            return;
        }
        Vec3 camera = event.getCamera().m_90583_();
        Vec3 right = new Vec3(event.getCamera().m_252775_());
        Vec3 up = new Vec3(event.getCamera().m_253028_());
        ParticleStatus setting = (ParticleStatus)client.f_91066_.m_231929_().m_231551_();
        float partial = event.getPartialTick();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = client.m_91269_().m_110104_();
        VertexConsumer vertices = buffers.m_6299_(XuanlanGlow.TYPE);
        int n = setting == ParticleStatus.MINIMAL ? 96 : (segments = setting == ParticleStatus.DECREASED ? 192 : 384);
        int echoes = setting == ParticleStatus.MINIMAL ? 0 : (setting == ParticleStatus.DECREASED ? 6 : 12);
        pose.m_85836_();
        try {
            pose.m_85837_(-camera.f_82479_, -camera.f_82480_, -camera.f_82481_);
            block3: for (Trail trail : TRAILS.values()) {
                PlayerMoonChakramEntity blade;
                Entity entity = trail.entity;
                if (segments <= 0) {
                    break;
                }
                if (entity.m_213877_() || entity.m_20238_(camera) > 2304.0 || trail.history.size() < 2) continue;
                boolean returning = entity instanceof PlayerMoonChakramEntity && (blade = (PlayerMoonChakramEntity)entity).isReturning();
                int color = returning ? 6815692 : 14413567;
                boolean near = entity.m_20238_(camera) < 576.0;
                int count = Math.min(trail.history.size(), setting == ParticleStatus.MINIMAL ? 3 : (near ? 8 : 4));
                Vec3 previous = entity.m_20318_(partial);
                for (int age = 1; age < count && segments > 0; ++age) {
                    XuanlanTrailHistory.Point sample = trail.history.get(age);
                    Vec3 point = new Vec3(sample.x(), sample.y(), sample.z());
                    float fade = Math.max(0.0f, 1.0f - ((float)(client.f_91073_.m_46467_() - sample.tick()) + partial) / 9.0f);
                    if (previous.m_82557_(point) > 36.0) continue block3;
                    if (previous.m_82557_(point) > 1.0E-4 && fade > 0.0f) {
                        XuanlanWeaponVisual.line(pose, vertices, previous, point, camera, 0.07 * (double)fade, color, 0.24f * fade);
                        XuanlanWeaponVisual.line(pose, vertices, previous, point, camera, 0.017 * (double)fade, 0xF1FFFF, 0.55f * fade);
                        --segments;
                        if (near && echoes > 0 && age % 3 == 0) {
                            XuanlanWeaponVisual.ring(pose, vertices, point, right, up, 0.22 * (double)fade, 0.013, color, 0.28f * fade, (double)(entity.f_19797_ - age) * 0.55, true);
                            --echoes;
                        }
                    }
                    previous = point;
                }
            }
        }
        finally {
            pose.m_85849_();
            buffers.m_109912_(XuanlanGlow.TYPE);
        }
    }

    private record Trail(Entity entity, XuanlanTrailHistory history) {
    }
}

