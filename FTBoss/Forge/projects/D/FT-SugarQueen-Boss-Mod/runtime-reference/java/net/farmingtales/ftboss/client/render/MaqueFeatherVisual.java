/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.HumanoidArm
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.client.render;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.client.maque.MaqueSupportRenderer;
import net.farmingtales.ftboss.skill.MaqueFeatherPacket;
import net.minecraft.client.Minecraft;
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

public final class MaqueFeatherVisual {
    private static final DustParticleOptions MINT = new DustParticleOptions(new Vector3f(0.65f, 0.95f, 0.76f), 0.8f);
    private static final DustParticleOptions IVORY = new DustParticleOptions(new Vector3f(0.96f, 0.97f, 0.8f), 0.65f);
    private static final List<Burst> ACTIVE = new ArrayList<Burst>();
    private static final int MAX_BURSTS = 8;
    private static final double VIEW_DISTANCE_SQUARED = 2304.0;
    private static ClientLevel activeLevel;
    private static int particleBudget;

    private MaqueFeatherVisual() {
    }

    public static void receive(MaqueFeatherPacket maqueFeatherPacket) {
        MaqueSupportRenderer.receiveFeather(maqueFeatherPacket);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent clientTickEvent) {
    }

    private static Vec3 handPosition(Minecraft client, Player player, boolean main) {
        HumanoidArm arm = main ? player.m_5737_() : player.m_5737_().m_20828_();
        double side = arm == HumanoidArm.RIGHT ? 1.0 : -1.0;
        double yaw = Math.toRadians(player.f_20883_);
        Vec3 right = new Vec3(Math.cos(yaw), 0.0, Math.sin(yaw));
        if (player == client.f_91074_ && client.f_91066_.m_92176_().m_90612_()) {
            return player.m_146892_().m_82549_(player.m_20154_().m_82490_(0.65)).m_82549_(right.m_82490_(side * 0.32)).m_82520_(0.0, -0.25, 0.0);
        }
        Vec3 forward = new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
        return player.m_20182_().m_82520_(0.0, player.m_6047_() ? 0.85 : 1.05, 0.0).m_82549_(right.m_82490_(side * 0.36)).m_82549_(forward.m_82490_(0.24));
    }

    private static void blessing(ClientLevel level, Vec3 center, int age) {
        double angle;
        double radius = 6.0 * (double)(age + 1) / 28.0;
        for (int segment = 0; segment < 24; ++segment) {
            angle = (double)segment * Math.PI * 2.0 / 24.0 + (double)age * 0.05;
            Vec3 point = center.m_82520_(Math.cos(angle) * radius, 0.12 + (double)age * 0.008, Math.sin(angle) * radius);
            MaqueFeatherVisual.emit(level, (ParticleOptions)(segment % 6 == 0 ? ParticleTypes.f_123810_ : MINT), point, 0.014);
        }
        for (int stream = 0; stream < 4; ++stream) {
            angle = (double)age * 0.22 + (double)stream * Math.PI / 2.0;
            MaqueFeatherVisual.emit(level, (ParticleOptions)IVORY, center.m_82520_(Math.cos(angle) * 0.6, 0.4 + (double)age * 0.06, Math.sin(angle) * 0.6), 0.025);
        }
    }

    private static void wind(ClientLevel level, Burst burst, int age) {
        double d;
        Vec3 center;
        Entity source = level.m_6815_(burst.packet().casterId());
        Vec3 vec3 = center = source instanceof Player && source.m_6084_() ? source.m_20182_() : burst.packet().position();
        if (source instanceof Player) {
            Player player = (Player)source;
            d = player.f_20883_;
        } else {
            d = 0.0;
        }
        double yaw = Math.toRadians(d);
        Vec3 right = new Vec3(Math.cos(yaw), 0.0, Math.sin(yaw));
        Vec3 back = new Vec3(Math.sin(yaw), 0.0, -Math.cos(yaw));
        if (age <= 14) {
            for (int side : new int[]{-1, 1}) {
                for (int feather = 0; feather < 6; ++feather) {
                    double spread = 0.25 + (double)feather * 0.24;
                    Vec3 point = center.m_82549_(right.m_82490_((double)side * spread)).m_82549_(back.m_82490_(0.3 + spread * 0.12)).m_82520_(0.0, 1.25 + Math.sin((double)feather * 0.38) * 0.6, 0.0);
                    MaqueFeatherVisual.emit(level, (ParticleOptions)ParticleTypes.f_123810_, point, 0.02);
                    MaqueFeatherVisual.emit(level, (ParticleOptions)MINT, point.m_82549_(right.m_82490_((double)side * 0.1)).m_82520_(0.0, -0.25 - (double)feather * 0.055, 0.0), 0.035);
                }
            }
        }
        for (int stream = 0; stream < 4; ++stream) {
            double angle = (double)age * 0.28 + (double)stream * Math.PI / 2.0;
            MaqueFeatherVisual.emit(level, (ParticleOptions)(stream % 2 == 0 ? IVORY : ParticleTypes.f_123810_), center.m_82520_(Math.cos(angle) * 0.6, 0.15 + (double)(age % 20) * 0.1, Math.sin(angle) * 0.6), 0.035);
        }
    }

    private static void emit(ClientLevel level, ParticleOptions particle, Vec3 position, double rise) {
        if (particleBudget <= 0) {
            return;
        }
        --particleBudget;
        level.m_7106_(particle, position.f_82479_, position.f_82480_, position.f_82481_, 0.0, rise, 0.0);
    }

    private static /* synthetic */ boolean lambda$onClientTick$0(long now, Burst burst) {
        return now - burst.startTick() >= (long)(burst.packet().wind() ? 40 : 28) || now < burst.startTick();
    }

    private record Burst(MaqueFeatherPacket packet, long startTick) {
    }
}

