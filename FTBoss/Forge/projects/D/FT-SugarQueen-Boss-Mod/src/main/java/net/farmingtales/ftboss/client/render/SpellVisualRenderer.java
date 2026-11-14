package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.util.ManorStandaloneSpellModels;
import net.farmingtales.ftboss.util.RemainingSpellModels;
import net.farmingtales.ftboss.util.QizaiRoseStarfallModel;
import net.farmingtales.ftboss.util.SpellBeamModels;
import net.farmingtales.ftboss.util.SpellVisualGeometry;
import net.farmingtales.ftboss.util.SpellVisualGeometry.Point;
import net.farmingtales.ftboss.util.SpellVisualRules;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class SpellVisualRenderer {
    private static final ResourceLocation ATLAS = new ResourceLocation(FTBossMod.MOD_ID, "textures/fx/spells/spell_atlas.png");
    private static final RenderType TYPE = RenderType.entityTranslucentEmissive(ATLAS);
    private static final List<Visual> ACTIVE = new ArrayList<>();
    private static final boolean NATIVE_LIGHTS = net.minecraftforge.fml.ModList.get().isLoaded("irons_spellbooks")
            && !Boolean.getBoolean("ftboss.disableNativeSpellLights");
    private static ClientLevel world;

    private SpellVisualRenderer() {}

    public static void accept(SpellVisualPacket packet) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !packet.valid() || !level.dimension().location().equals(packet.dimension())) return;
        if (world != level) {
            ACTIVE.clear();
            world = level;
        }
        if (!RemainingSpellModels.supports(packet.spell()) && IronNativeSpellBridge.accept(packet)) {
            ACTIVE.removeIf(visual -> visual.same(packet) && visual.packet.phase() != Phase.BEAM);
            return;
        }
        long now = level.getGameTime();
        if (packet.phase() == Phase.CHARGE) {
            for (Visual visual : ACTIVE) {
                if (visual.same(packet) && visual.packet.phase() == Phase.CHARGE) {
                    visual.packet = packet;
                    visual.refreshed = now;
                    return;
                }
            }
        } else {
            ACTIVE.removeIf(visual -> visual.same(packet) && (visual.packet.phase() == Phase.CHARGE
                    || packet.phase() != Phase.BEAM && visual.packet.phase() != Phase.BEAM));
        }
        if (ACTIVE.size() >= SpellVisualRules.MAX_ACTIVE) ACTIVE.remove(0);
        ACTIVE.add(new Visual(packet, now, !SpellBeamModels.replaces(packet.spell(), packet.phase())
                && NATIVE_LIGHTS && IronSpellLightParticles.emit(packet)));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || level != world) {
            ACTIVE.clear();
            if (NATIVE_LIGHTS) IronSpellLightParticles.clear();
            world = level;
            return;
        }
        long now = level.getGameTime();
        ACTIVE.removeIf(visual -> {
            long age = now - visual.refreshed;
            Entity owner = owner(level, visual.packet);
            return age < 0 || age >= visual.packet.spell().lifetime(visual.packet.phase())
                    || owner != null && (owner.isRemoved() || owner instanceof LivingEntity living && !living.isAlive());
        });
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) {
        if (event.getLevel() == world) {
            ACTIVE.clear();
            if (NATIVE_LIGHTS) IronSpellLightParticles.clear();
            world = null;
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        ACTIVE.clear();
        if (NATIVE_LIGHTS) IronSpellLightParticles.clear();
        world = null;
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty()) return;
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || level != world) return;
        Vec3 camera = event.getCamera().getPosition();
        float partial = minecraft.isPaused() ? 0 : event.getPartialTick();
        ParticleStatus quality = minecraft.options.particles().get();
        int limit = quality == ParticleStatus.MINIMAL ? 16 : quality == ParticleStatus.DECREASED ? 32 : 64;
        Entity cameraEntity = event.getCamera().getEntity();
        boolean firstPerson = !event.getCamera().isDetached();
        List<Visual> visible = ACTIVE.stream()
                .filter(visual -> !SpellVisualRules.hiddenForCamera(visual.packet.spell(), firstPerson,
                        cameraEntity != null && cameraEntity.getUUID().equals(visual.packet.owner())))
                .sorted(Comparator.comparingDouble(visual -> visual.packet.start().distanceToSqr(camera)))
                .limit(limit).toList();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        renderModels(visible, event, level, camera, partial, quality, buffers);
        VertexConsumer vertices = buffers.getBuffer(TYPE);
        PoseStack poses = event.getPoseStack();
        int quads = 0;
        for (Visual visual : visible) {
            SpellVisualPacket packet = visual.packet;
            if (SpellBeamModels.replaces(packet.spell(), packet.phase()) && !roseStarfallBeam(packet)
                    && !doraFocusLaserBeam(packet)) continue;
            Entity owner = owner(level, packet);
            float radiusScale = (float) net.farmingtales.ftboss.skill.SpellVisuals.radiusScale(owner, packet.spell().id);
            Vec3 origin = packet.spell().followsCaster(packet.phase()) && owner != null ? owner.getPosition(partial) : packet.start();
            AABB bounds = new AABB(origin, packet.phase() == Phase.BEAM ? packet.end() : origin).inflate(14 * radiusScale);
            if (bounds.distanceToSqr(camera) > SpellVisualRules.VIEW_RANGE * SpellVisualRules.VIEW_RANGE || !event.getFrustum().isVisible(bounds)) continue;
            double age = level.getGameTime() - visual.born + partial;
            double refreshedAge = level.getGameTime() - visual.refreshed + partial;
            float opacity = SpellVisualRules.opacity(refreshedAge, packet.spell().lifetime(packet.phase()));
            Vec3 direction = packet.end().subtract(packet.start());
            if (packet.spell().form == SpellVisualRules.Form.WINGS && owner != null) direction = owner.getViewVector(partial);
            if (packet.phase() != Phase.BEAM) direction = direction.multiply(1, 0, 1);
            double length = direction.length();
            if (length < 0.001) direction = new Vec3(0, 0, 1);
            else direction = direction.scale(1 / length);
            poses.pushPose();
            try {
                poses.translate(origin.x - camera.x, origin.y - camera.y, origin.z - camera.z);
                poses.mulPose(new Quaternionf().rotationTo(0, 0, 1, (float) direction.x, (float) direction.y, (float) direction.z));
                poses.scale(radiusScale, radiusScale, packet.phase() == Phase.BEAM ? 1.0F : radiusScale);
                PoseStack.Pose pose = poses.last();
                quads += SpellVisualGeometry.draw(packet.spell(), packet.phase(), age, opacity, length, visual.nativeLight,
                        (first, second, third, fourth, color, alpha, tile) -> {
                            int columns = SpellVisualRules.ATLAS_COLUMNS;
                            int size = SpellVisualRules.ATLAS_SIZE;
                            float minimumU = (tile % columns * 128 + 1F) / size;
                            float minimumV = (tile / columns * 128 + 1F) / size;
                            float maximumU = minimumU + 126F / size;
                            float maximumV = minimumV + 126F / size;
                            if (tile == SpellVisualRules.SOLID_TILE) {
                                minimumU = maximumU = ((columns - 1) * 128 + 64F) / size;
                                minimumV = maximumV = minimumU;
                            }
                            vertex(vertices, pose, first, color, alpha, minimumU, maximumV);
                            vertex(vertices, pose, second, color, alpha, minimumU, minimumV);
                            vertex(vertices, pose, third, color, alpha, maximumU, minimumV);
                            vertex(vertices, pose, fourth, color, alpha, maximumU, maximumV);
                        });
            } finally {
                poses.popPose();
            }
            if (quads >= 16_000) break;
        }
        buffers.endBatch(TYPE);
    }

    private static void renderModels(List<Visual> visible, RenderLevelStageEvent event, ClientLevel level,
                                     Vec3 camera, float partial, ParticleStatus quality,
                                     MultiBufferSource.BufferSource buffers) {
        if (visible.stream().noneMatch(visual -> ManorStandaloneSpellModels.renders(
                visual.packet.spell(), visual.packet.phase()) || RemainingSpellModels.renders(
                visual.packet.spell(), visual.packet.phase()) || SpellBeamModels.renders(
                visual.packet.spell(), visual.packet.phase()) || SpellBeamModels.rendersAccent(
                visual.packet.spell(), visual.packet.phase()) || roseStarfallBeam(visual.packet))) return;
        VertexConsumer vertices = buffers.getBuffer(ManorSpellModelRenderType.TYPE);
        PoseStack poses = event.getPoseStack();
        Entity cameraEntity = event.getCamera().getEntity();
        boolean firstPerson = !event.getCamera().isDetached();
        int limit = quality == ParticleStatus.MINIMAL ? 4 : quality == ParticleStatus.DECREASED
                ? 8 : ManorStandaloneSpellModels.MAX_MODELS_PER_FRAME;
        int rendered = 0;
        try {
            for (Visual visual : visible) {
                SpellVisualPacket packet = visual.packet;
                boolean roseBeam = roseStarfallBeam(packet);
                boolean accentModel = SpellBeamModels.rendersAccent(packet.spell(), packet.phase());
                boolean remainingModel = RemainingSpellModels.renders(packet.spell(), packet.phase());
                if ((!roseBeam && !accentModel && !remainingModel && !ManorStandaloneSpellModels.renders(packet.spell(), packet.phase())) || rendered >= limit) continue;
                if (SpellVisualRules.modelHiddenForCamera(packet.spell(), firstPerson,
                        cameraEntity != null && cameraEntity.getUUID().equals(packet.owner()))) continue;
                Entity owner = owner(level, packet);
                float radiusScale = (float) net.farmingtales.ftboss.skill.SpellVisuals.radiusScale(owner, packet.spell().id);
                Vec3 origin = packet.spell().followsCaster(packet.phase()) && owner != null
                        ? owner.getPosition(partial) : packet.start();
                AABB bounds = new AABB(origin, roseBeam ? packet.end() : origin).inflate(7 * radiusScale);
                if (bounds.distanceToSqr(camera) > SpellVisualRules.VIEW_RANGE * SpellVisualRules.VIEW_RANGE
                        || !event.getFrustum().isVisible(bounds)) continue;
                double age = level.getGameTime() - visual.born + partial;
                double refreshedAge = level.getGameTime() - visual.refreshed + partial;
                float opacity = SpellVisualRules.modelOpacity(refreshedAge, packet.spell().lifetime(packet.phase()));
                if (opacity <= 0) continue;
                Vec3 direction = SpellModelFacing.direction(packet, owner, partial);
                if (!roseBeam) direction = direction.multiply(1, 0, 1);
                double length = direction.length();
                direction = length < 0.001 ? new Vec3(0, 0, 1) : direction.scale(1 / length);
                poses.pushPose();
                try {
                    poses.translate(origin.x - camera.x, origin.y - camera.y, origin.z - camera.z);
                    poses.mulPose(new Quaternionf().rotationTo(0, 0, 1,
                            (float) direction.x, (float) direction.y, (float) direction.z));
                    poses.scale(radiusScale, radiusScale, radiusScale);
                    PoseStack.Pose pose = poses.last();
                    int quads;
                    if (roseBeam) {
                        int[] roseQuads = {0};
                        QizaiRoseStarfallModel.solid(age, length, quality != ParticleStatus.ALL, 1.0F,
                                (first, second, third, fourth, color) -> {
                                    roseModelVertex(vertices, pose, first, color, opacity);
                                    roseModelVertex(vertices, pose, second, color, opacity);
                                    roseModelVertex(vertices, pose, third, color, opacity);
                                    roseModelVertex(vertices, pose, fourth, color, opacity);
                                    roseQuads[0]++;
                                });
                        quads = roseQuads[0];
                    } else if (accentModel) {
                        quads = SpellBeamModels.drawAccent(packet.spell(), packet.phase(), age,
                                (first, second, third, fourth, color) -> {
                                    modelVertex(vertices, pose, first, color, opacity);
                                    modelVertex(vertices, pose, second, color, opacity);
                                    modelVertex(vertices, pose, third, color, opacity);
                                    modelVertex(vertices, pose, fourth, color, opacity);
                                });
                    } else if (remainingModel) {
                        quads = RemainingSpellModels.draw(packet.spell(), packet.phase(), refreshedAge, quality != ParticleStatus.ALL,
                                (first, second, third, fourth, color) -> {
                                    modelVertex(vertices, pose, first, color, opacity);
                                    modelVertex(vertices, pose, second, color, opacity);
                                    modelVertex(vertices, pose, third, color, opacity);
                                    modelVertex(vertices, pose, fourth, color, opacity);
                                });
                    } else {
                        quads = ManorStandaloneSpellModels.draw(packet.spell(), packet.phase(), age,
                                (first, second, third, fourth, color) -> {
                                    modelVertex(vertices, pose, first, color, opacity);
                                    modelVertex(vertices, pose, second, color, opacity);
                                    modelVertex(vertices, pose, third, color, opacity);
                                    modelVertex(vertices, pose, fourth, color, opacity);
                                });
                    }
                    if (quads > 0) rendered++;
                } finally {
                    poses.popPose();
                }
            }
            renderBeamModels(visible, event, level, camera, partial, quality, vertices);
        } finally {
            buffers.endBatch(ManorSpellModelRenderType.TYPE);
        }
    }

    private static void renderBeamModels(List<Visual> visible, RenderLevelStageEvent event, ClientLevel level,
                                         Vec3 camera, float partial, ParticleStatus quality, VertexConsumer vertices) {
        int limit = quality == ParticleStatus.MINIMAL ? 6 : quality == ParticleStatus.DECREASED
                ? 12 : SpellBeamModels.MAX_MODELS_PER_FRAME;
        int rendered = 0;
        PoseStack poses = event.getPoseStack();
        for (Visual visual : visible) {
            SpellVisualPacket packet = visual.packet;
            if (roseStarfallBeam(packet) || !SpellBeamModels.renders(packet.spell(), packet.phase()) || rendered >= limit) continue;
            double age = level.getGameTime() - visual.born + partial;
            float opacity = SpellVisualRules.modelOpacity(age, packet.spell().lifetime(packet.phase()));
            Vec3 direction = packet.end().subtract(packet.start());
            double length = direction.length();
            if (opacity <= 0 || length < 0.015 || length > (packet.spell() == SpellVisualRules.Spell.STAR_CROSS ? 128 : 64)) continue;
            Entity owner = owner(level, packet);
            float radiusScale = (float) net.farmingtales.ftboss.skill.SpellVisuals.radiusScale(owner, packet.spell().id);
            AABB bounds = new AABB(packet.start(), packet.end()).inflate(1.2 * radiusScale);
            if (bounds.distanceToSqr(camera) > SpellVisualRules.VIEW_RANGE * SpellVisualRules.VIEW_RANGE
                    || !event.getFrustum().isVisible(bounds)) continue;
            direction = direction.scale(1 / length);
            poses.pushPose();
            try {
                poses.translate(packet.start().x - camera.x, packet.start().y - camera.y, packet.start().z - camera.z);
                poses.mulPose(new Quaternionf().rotationTo(0, 0, 1, (float) direction.x, (float) direction.y, (float) direction.z));
                poses.scale(radiusScale, radiusScale, 1);
                PoseStack.Pose pose = poses.last();
                int quads = SpellBeamModels.draw(packet.spell(), packet.phase(), age, length, quality != ParticleStatus.ALL,
                        (first, second, third, fourth, color) -> {
                            modelVertex(vertices, pose, first, color, opacity);
                            modelVertex(vertices, pose, second, color, opacity);
                            modelVertex(vertices, pose, third, color, opacity);
                            modelVertex(vertices, pose, fourth, color, opacity);
                        });
                if (quads > 0) rendered++;
            } finally { poses.popPose(); }
        }
    }

    private static boolean doraFocusLaserBeam(SpellVisualPacket packet) {
        return packet.spell() == SpellVisualRules.Spell.ARCANE_FUSILLADE && packet.phase() == Phase.BEAM;
    }

    private static boolean roseStarfallBeam(SpellVisualPacket packet) {
        return packet.spell() == SpellVisualRules.Spell.ROSE_STARFALL && packet.phase() == Phase.BEAM;
    }

    private static void roseModelVertex(VertexConsumer vertices, PoseStack.Pose pose,
                                        QizaiRoseStarfallModel.Point point, int color, float alpha) {
        vertices.vertex(pose.pose(), (float) point.x(), (float) point.y(), (float) point.z())
                .color(color >> 16 & 255, color >> 8 & 255, color & 255, alphaByte(alpha)).endVertex();
    }

    private static void modelVertex(VertexConsumer vertices, PoseStack.Pose pose,
                                    ManorStandaloneSpellModels.Point point, int color, float alpha) {
        vertices.vertex(pose.pose(), (float) point.x(), (float) point.y(), (float) point.z())
                .color(color >> 16 & 255, color >> 8 & 255, color & 255, alphaByte(alpha)).endVertex();
    }

    private static int alphaByte(float alpha) {
        return Math.max(0, Math.min(255, Math.round(alpha * 255)));
    }

    private static Entity owner(ClientLevel level, SpellVisualPacket packet) {
        Entity entity = level.getEntity(packet.ownerId());
        return entity != null && entity.getUUID().equals(packet.owner()) ? entity : null;
    }

    private static void vertex(VertexConsumer vertices, PoseStack.Pose pose, Point point, int color, float alpha, float textureU, float textureV) {
        vertices.vertex(pose.pose(), (float) point.horizontalX(), (float) point.verticalY(), (float) point.horizontalZ())
                .color(color >> 16 & 255, color >> 8 & 255, color & 255, Math.round(alpha * 255))
                .uv(textureU, textureV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(pose.normal(), 0, 1, 0).endVertex();
    }

    private static final class Visual {
        private SpellVisualPacket packet;
        private final long born;
        private final boolean nativeLight;
        private long refreshed;

        private Visual(SpellVisualPacket packet, long now, boolean nativeLight) {
            this.packet = packet; born = now; refreshed = now; this.nativeLight = nativeLight;
        }
        private boolean same(SpellVisualPacket other) {
            return packet.owner().equals(other.owner()) && packet.spell() == other.spell()
                    && (packet.spell() != SpellVisualRules.Spell.ROSE_STARFALL || packet.start().distanceToSqr(other.start()) < 0.01);
        }
    }
}
