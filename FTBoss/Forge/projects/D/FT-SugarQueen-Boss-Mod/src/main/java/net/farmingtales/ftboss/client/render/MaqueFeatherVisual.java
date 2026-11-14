package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.item.weapons.MaqueBlessingFeatherItem;
import net.farmingtales.ftboss.skill.MaqueFeatherPacket;
import net.farmingtales.ftboss.util.MaqueFeatherRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class MaqueFeatherVisual {
    private static final DustParticleOptions MINT = new DustParticleOptions(new Vector3f(0.65F, 0.95F, 0.76F), 0.8F);
    private static final DustParticleOptions IVORY = new DustParticleOptions(new Vector3f(0.96F, 0.97F, 0.8F), 0.65F);
    private static final List<Burst> ACTIVE = new ArrayList<>();
    private static final int MAX_BURSTS = 8;
    private static final double VIEW_DISTANCE_SQUARED = 48.0 * 48.0;
    private static ClientLevel activeLevel;
    private static int particleBudget;

    private record Burst(MaqueFeatherPacket packet, long startTick) {}

    private MaqueFeatherVisual() {}

    public static void receive(MaqueFeatherPacket packet) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || !client.level.dimension().location().equals(packet.dimension())
                || !Double.isFinite(packet.position().x) || !Double.isFinite(packet.position().y)
                || !Double.isFinite(packet.position().z)) return;
        if (activeLevel != client.level) {
            ACTIVE.clear();
            activeLevel = client.level;
        }
        if (client.getCameraEntity() == null || client.getCameraEntity().distanceToSqr(packet.position()) > VIEW_DISTANCE_SQUARED) return;
        if (ACTIVE.size() >= MAX_BURSTS) ACTIVE.remove(0);
        ACTIVE.add(new Burst(packet, client.level.getGameTime()));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft client = Minecraft.getInstance();
        if (activeLevel != client.level) {
            ACTIVE.clear();
            activeLevel = client.level;
        }
        if (client.level == null || client.player == null || client.isPaused() || client.getCameraEntity() == null) return;
        long now = client.level.getGameTime();
        ACTIVE.removeIf(burst -> now - burst.startTick() >= (burst.packet().wind() ? 40 : 28) || now < burst.startTick());
        ParticleStatus setting = client.options.particles().get();
        if (setting == ParticleStatus.MINIMAL) return;
        particleBudget = setting == ParticleStatus.DECREASED ? 32 : 64;
        int holders = 0;
        for (Player player : client.level.players()) {
            if (!player.isAlive() || player.isSpectator() || player.isInvisibleTo(client.player)
                    || player.distanceToSqr(client.getCameraEntity()) > VIEW_DISTANCE_SQUARED) continue;
            boolean main = player.getMainHandItem().getItem() instanceof MaqueBlessingFeatherItem;
            boolean off = player.getOffhandItem().getItem() instanceof MaqueBlessingFeatherItem;
            if ((!main && !off) || (player.tickCount + player.getId()) % 5 != 0) continue;
            Vec3 hand = handPosition(client, player, main);
            double phase = player.tickCount * 0.18;
            emit(client.level, MINT, hand.add(Math.cos(phase) * 0.07, 0.08, Math.sin(phase) * 0.07), 0.014);
            emit(client.level, ParticleTypes.END_ROD, hand, 0.006);
            if (++holders >= 4) break;
        }
        for (Burst burst : ACTIVE) {
            if (particleBudget <= 0) break;
            if (client.getCameraEntity().distanceToSqr(burst.packet().position()) > VIEW_DISTANCE_SQUARED) continue;
            int age = (int) (now - burst.startTick());
            if ((age & 1) != 0) continue;
            if (burst.packet().wind()) wind(client.level, burst, age);
            else blessing(client.level, burst.packet().position(), age);
        }
    }

    private static Vec3 handPosition(Minecraft client, Player player, boolean main) {
        HumanoidArm arm = main ? player.getMainArm() : player.getMainArm().getOpposite();
        double side = arm == HumanoidArm.RIGHT ? 1 : -1;
        double yaw = Math.toRadians(player.yBodyRot);
        Vec3 right = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));
        if (player == client.player && client.options.getCameraType().isFirstPerson()) {
            return player.getEyePosition().add(player.getLookAngle().scale(0.65)).add(right.scale(side * 0.32)).add(0, -0.25, 0);
        }
        Vec3 forward = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        return player.position().add(0, player.isCrouching() ? 0.85 : 1.05, 0)
                .add(right.scale(side * 0.36)).add(forward.scale(0.24));
    }

    private static void blessing(ClientLevel level, Vec3 center, int age) {
        double radius = MaqueFeatherRules.RADIUS * (age + 1) / 28.0;
        for (int segment = 0; segment < 24; segment++) {
            double angle = segment * Math.PI * 2 / 24 + age * 0.05;
            Vec3 point = center.add(Math.cos(angle) * radius, 0.12 + age * 0.008, Math.sin(angle) * radius);
            emit(level, segment % 6 == 0 ? ParticleTypes.END_ROD : MINT, point, 0.014);
        }
        for (int stream = 0; stream < 4; stream++) {
            double angle = age * 0.22 + stream * Math.PI / 2;
            emit(level, IVORY, center.add(Math.cos(angle) * 0.6, 0.4 + age * 0.06, Math.sin(angle) * 0.6), 0.025);
        }
    }

    private static void wind(ClientLevel level, Burst burst, int age) {
        Entity source = level.getEntity(burst.packet().casterId());
        Vec3 center = source instanceof Player && source.isAlive() ? source.position() : burst.packet().position();
        double yaw = Math.toRadians(source instanceof Player player ? player.yBodyRot : 0);
        Vec3 right = new Vec3(Math.cos(yaw), 0, Math.sin(yaw));
        Vec3 back = new Vec3(Math.sin(yaw), 0, -Math.cos(yaw));
        if (age <= 14) {
            for (int side : new int[]{-1, 1}) for (int feather = 0; feather < 6; feather++) {
                double spread = 0.25 + feather * 0.24;
                Vec3 point = center.add(right.scale(side * spread)).add(back.scale(0.3 + spread * 0.12))
                        .add(0, 1.25 + Math.sin(feather * 0.38) * 0.6, 0);
                emit(level, ParticleTypes.END_ROD, point, 0.02);
                emit(level, MINT, point.add(right.scale(side * 0.1)).add(0, -0.25 - feather * 0.055, 0), 0.035);
            }
        }
        for (int stream = 0; stream < 4; stream++) {
            double angle = age * 0.28 + stream * Math.PI / 2;
            emit(level, stream % 2 == 0 ? IVORY : ParticleTypes.END_ROD,
                    center.add(Math.cos(angle) * 0.6, 0.15 + (age % 20) * 0.1, Math.sin(angle) * 0.6), 0.035);
        }
    }

    private static void emit(ClientLevel level, ParticleOptions particle, Vec3 position, double rise) {
        if (particleBudget <= 0) return;
        particleBudget--;
        level.addParticle(particle, position.x, position.y, position.z, 0, rise, 0);
    }
}
