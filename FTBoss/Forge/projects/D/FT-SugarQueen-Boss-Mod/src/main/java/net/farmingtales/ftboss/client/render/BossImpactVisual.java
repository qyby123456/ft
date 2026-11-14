package net.farmingtales.ftboss.client.render;

import java.util.ArrayDeque;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.skill.BossImpactPacket;
import net.farmingtales.ftboss.util.BossImpactGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, value = Dist.CLIENT)
public final class BossImpactVisual {
    private record Impact(BossImpactPacket packet, long started) {}
    private static final ArrayDeque<Impact> ACTIVE = new ArrayDeque<>();
    private static ClientLevel activeLevel;
    private static long lastSound = Long.MIN_VALUE;

    private BossImpactVisual() {}

    private static void clear() { ACTIVE.clear(); activeLevel = null; lastSound = Long.MIN_VALUE; }

    public static void receive(BossImpactPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (!packet.valid() || level == null || !level.dimension().location().equals(packet.dimension())) return;
        if (activeLevel != level) { clear(); activeLevel = level; }
        if (minecraft.player == null || minecraft.player.distanceToSqr(packet.position()) > 48 * 48) return;
        if (ACTIVE.size() >= BossImpactGeometry.MAX_ACTIVE) ACTIVE.removeFirst();
        long now = level.getGameTime();
        ACTIVE.addLast(new Impact(packet, now));
        if (minecraft.player.distanceToSqr(packet.position()) > 16 * 16
                || lastSound != Long.MIN_VALUE && now >= lastSound && now - lastSound < 6) return;
        lastSound = now;
        boolean heavy = packet.strength() >= 0.75f;
        var sound = packet.shield() ? SoundEvents.AMETHYST_BLOCK_HIT
                : heavy ? SoundEvents.PLAYER_ATTACK_STRONG : SoundEvents.PLAYER_ATTACK_WEAK;
        Vec3 position = packet.position();
        level.playLocalSound(position.x, position.y, position.z, sound, SoundSource.HOSTILE,
                packet.shield() ? 0.24f : heavy ? 0.32f : 0.16f, 0.92f + level.random.nextFloat() * 0.14f, false);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.level != activeLevel) { clear(); return; }
        if (!minecraft.isPaused()) ACTIVE.removeIf(impact -> !BossImpactGeometry.alive(minecraft.level.getGameTime() - impact.started));
    }

    @SubscribeEvent
    public static void unload(LevelEvent.Unload event) { if (event.getLevel().isClientSide()) clear(); }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { clear(); }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.level != activeLevel) return;
        Vec3 camera = event.getCamera().getPosition();
        var buffers = minecraft.renderBuffers().bufferSource();
        var vertices = buffers.getBuffer(BossImpactRenderType.TYPE);
        var matrix = event.getPoseStack().last();
        for (Impact impact : ACTIVE) {
            BossImpactPacket packet = impact.packet;
            if (camera.distanceToSqr(packet.position()) > 48 * 48 || event.getFrustum() != null
                    && !event.getFrustum().isVisible(new AABB(packet.position(), packet.position()).inflate(1.8))) continue;
            double age = minecraft.level.getGameTime() - impact.started + (minecraft.isPaused() ? 0 : event.getPartialTick());
            Vec3 normal = packet.normal();
            Vec3 reference = Math.abs(normal.y) > 0.95 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
            Vec3 right = reference.cross(normal).normalize();
            Vec3 up = normal.cross(right).normalize();
            Vec3 origin = packet.position().subtract(camera);
            int detail = minecraft.options.particles().get().ordinal();
            if (camera.distanceToSqr(packet.position()) > 24 * 24) detail = Math.max(1, detail);
            BossImpactGeometry.draw(age, packet.strength(), packet.shield(), packet.color(), detail,
                    (horizontal, vertical, color, alpha) -> {
                        Vec3 point = origin.add(right.scale(horizontal * packet.size())).add(up.scale(vertical * packet.size()));
                        vertices.vertex(matrix.pose(), (float) point.x, (float) point.y, (float) point.z)
                                .color((color >> 16 & 255) / 255f * alpha, (color >> 8 & 255) / 255f * alpha, (color & 255) / 255f * alpha, 1)
                                .uv(0.5f, 0.5f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                                .normal(matrix.normal(), (float) normal.x, (float) normal.y, (float) normal.z).endVertex();
                    });
        }
        buffers.endBatch(BossImpactRenderType.TYPE);
    }
}
