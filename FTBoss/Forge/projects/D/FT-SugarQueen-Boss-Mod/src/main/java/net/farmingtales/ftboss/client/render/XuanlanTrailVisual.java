package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.LinkedHashMap;
import java.util.Map;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.CombatVisualEntities;
import net.farmingtales.ftboss.entity.PlayerMoonChakramEntity;
import net.farmingtales.ftboss.util.XuanlanMotionRules;
import net.farmingtales.ftboss.util.XuanlanTrailHistory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, value = Dist.CLIENT)
public final class XuanlanTrailVisual {
    private record Trail(Entity entity, XuanlanTrailHistory history) {}
    private static final Map<Integer, Trail> TRAILS = new LinkedHashMap<>();
    private static Level currentLevel;

    private XuanlanTrailVisual() {}

    private static void useLevel(Level level) {
        if (currentLevel == level) return;
        TRAILS.clear();
        currentLevel = level;
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft client = Minecraft.getInstance();
        useLevel(client.level);
        if (client.level == null || client.isPaused() || client.getCameraEntity() == null) return;
        Entity camera = client.getCameraEntity();
        TRAILS.values().removeIf(trail -> trail.entity.isRemoved() || trail.entity.level() != client.level
                || client.level.getEntity(trail.entity.getId()) != trail.entity || trail.entity.distanceToSqr(camera) > 48 * 48);
        for (Entity entity : CombatVisualEntities.get(client.level).flywheels()) {
            if (entity.isRemoved() || entity.distanceToSqr(camera) > 48 * 48) continue;
            Trail trail = TRAILS.get(entity.getId());
            if (trail == null) {
                if (TRAILS.size() >= XuanlanMotionRules.MAX_TRAILS) continue;
                trail = new Trail(entity, new XuanlanTrailHistory());
                TRAILS.put(entity.getId(), trail);
            }
            trail.history.add(entity.getX(), entity.getY(), entity.getZ(), client.level.getGameTime());
        }
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() == currentLevel) useLevel(null);
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft client = Minecraft.getInstance();
        useLevel(client.level);
        if (client.level == null || TRAILS.isEmpty()) return;
        Vec3 camera = event.getCamera().getPosition();
        Vec3 right = new Vec3(event.getCamera().getLeftVector());
        Vec3 up = new Vec3(event.getCamera().getUpVector());
        ParticleStatus setting = client.options.particles().get();
        float partial = event.getPartialTick();
        PoseStack pose = event.getPoseStack();
        var buffers = client.renderBuffers().bufferSource();
        var vertices = buffers.getBuffer(XuanlanGlow.TYPE);
        int segments = setting == ParticleStatus.MINIMAL ? 96 : setting == ParticleStatus.DECREASED ? 192 : 384;
        int echoes = setting == ParticleStatus.MINIMAL ? 0 : setting == ParticleStatus.DECREASED ? 6 : 12;
        pose.pushPose();
        try {
            pose.translate(-camera.x, -camera.y, -camera.z);
            for (Trail trail : TRAILS.values()) {
                Entity entity = trail.entity;
                if (segments <= 0) break;
                if (entity.isRemoved() || entity.distanceToSqr(camera) > 48 * 48 || trail.history.size() < 2) continue;
                boolean returning = entity instanceof PlayerMoonChakramEntity blade && blade.isReturning();
                double projectileScale = entity instanceof PlayerMoonChakramEntity ? PlayerMoonChakramRenderer.PROJECTILE_SCALE : 1.0;
                int color = returning ? 0x67FFCC : 0xDBEEFF;
                boolean near = entity.distanceToSqr(camera) < 24 * 24;
                int count = Math.min(trail.history.size(), setting == ParticleStatus.MINIMAL ? 3 : near ? 8 : 4);
                Vec3 previous = entity.getPosition(partial);
                for (int age = 1; age < count && segments > 0; age++) {
                    var sample = trail.history.get(age);
                    Vec3 point = new Vec3(sample.x(), sample.y(), sample.z());
                    float fade = Math.max(0, 1 - (client.level.getGameTime() - sample.tick() + partial) / 9.0F);
                    if (previous.distanceToSqr(point) > 36) break;
                    if (previous.distanceToSqr(point) > 0.0001 && fade > 0) {
                        XuanlanWeaponVisual.line(pose, vertices, previous, point, camera, 0.07 * fade * projectileScale, color, 0.24F * fade);
                        XuanlanWeaponVisual.line(pose, vertices, previous, point, camera, 0.017 * fade * projectileScale, 0xF1FFFF, 0.55F * fade);
                        segments--;
                        if (near && echoes > 0 && age % 3 == 0) {
                            XuanlanWeaponVisual.ring(pose, vertices, point, right, up, 0.22 * fade * projectileScale, 0.013 * projectileScale,
                                    color, 0.28F * fade, (entity.tickCount - age) * 0.55, true);
                            echoes--;
                        }
                    }
                    previous = point;
                }
            }
        } finally {
            pose.popPose();
            buffers.endBatch(XuanlanGlow.TYPE);
        }
    }
}
