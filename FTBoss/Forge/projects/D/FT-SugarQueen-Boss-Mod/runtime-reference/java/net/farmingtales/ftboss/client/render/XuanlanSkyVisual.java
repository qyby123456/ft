/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.client.render.XuanlanWeaponVisual;
import net.farmingtales.ftboss.skill.XuanlanFxPacket;
import net.farmingtales.ftboss.util.XuanlanSkyRules;
import net.minecraft.world.phys.Vec3;

public final class XuanlanSkyVisual {
    private static final int CYAN = 4709631;
    private static final int WHITE = 0xE6FFFF;
    private static final int WARNING = 16744530;
    private static final Vec3 EAST = new Vec3(1.0, 0.0, 0.0);
    private static final Vec3 SOUTH = new Vec3(0.0, 0.0, 1.0);

    private XuanlanSkyVisual() {
    }

    public static boolean handles(int kind) {
        return kind >= 20 && kind <= 24;
    }

    public static int lifetime(int kind) {
        return switch (kind) {
            case 21 -> 10;
            case 20 -> 20;
            case 23 -> 20;
            default -> 14;
        };
    }

    public static void render(PoseStack pose, VertexConsumer vertices, XuanlanFxPacket packet, Vec3 camera, float age, float life) {
        float progress = Math.max(0.0f, Math.min(1.0f, age / life));
        float fade = 1.0f - progress;
        Vec3 floor = packet.from();
        double radius = 3.0;
        if (packet.kind() == 21) {
            float pulse = 0.65f + 0.25f * (float)Math.sin((double)age * 0.9);
            XuanlanSkyVisual.ring(pose, vertices, floor, radius, 0.06, 16744530, pulse, 0.0, false);
            XuanlanSkyVisual.ring(pose, vertices, floor.m_82520_(0.0, 0.015, 0.0), radius * 0.9, 0.035, 4709631, 0.8f, (double)(-age) * 0.045, true);
            XuanlanSkyVisual.ring(pose, vertices, floor.m_82520_(0.0, 0.03, 0.0), radius * (double)fade, 0.045, 0xE6FFFF, 0.9f, 0.0, false);
            for (int spoke = 0; spoke < 4; ++spoke) {
                double angle = (double)spoke * Math.PI / 2.0;
                Vec3 direction = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
                XuanlanWeaponVisual.line(pose, vertices, floor.m_82549_(direction.m_82490_(0.3)), floor.m_82549_(direction.m_82490_(0.85)), camera, 0.035, 16744530, pulse);
            }
            XuanlanWeaponVisual.line(pose, vertices, floor, packet.to(), camera, 0.018, 4709631, 0.18f + progress * 0.25f);
        } else if (packet.kind() == 22) {
            XuanlanWeaponVisual.line(pose, vertices, floor, packet.to(), camera, radius, 4709631, fade * 0.12f);
            XuanlanWeaponVisual.line(pose, vertices, floor, packet.to(), camera, 0.65 * (double)fade, 4709631, fade * 0.7f);
            XuanlanWeaponVisual.line(pose, vertices, floor, packet.to(), camera, 0.2 * (double)fade, 0xE6FFFF, fade);
            for (int strand = 0; strand < 8; ++strand) {
                double angle = (double)strand * Math.PI / 4.0 + (double)age * 0.04;
                Vec3 offset = new Vec3(Math.cos(angle) * radius, 0.0, Math.sin(angle) * radius);
                XuanlanWeaponVisual.line(pose, vertices, floor.m_82549_(offset), packet.to().m_82549_(offset), camera, 0.018 * (double)fade, 4709631, fade * 0.35f);
            }
            XuanlanSkyVisual.ring(pose, vertices, floor, radius, 0.12 * (double)fade, 0xE6FFFF, fade, 0.0, false);
            XuanlanSkyVisual.ring(pose, vertices, floor.m_82520_(0.0, 0.06, 0.0), radius * (0.3 + (double)progress * 1.25), 0.07, 4709631, fade, (double)age * 0.1, true);
            XuanlanSkyVisual.ring(pose, vertices, floor.m_82520_(0.0, 0.1 + (double)age * 0.17, 0.0), radius * (double)fade, 0.05, 0xE6FFFF, fade * 0.7f, 0.0, false);
        } else {
            boolean ascent = packet.kind() == 20;
            double height = 24.0 * XuanlanSkyRules.rise(ascent ? (double)progress : (double)fade);
            for (int tier = 0; tier < 3; ++tier) {
                Vec3 center = floor.m_82520_(0.0, height + (double)tier * 0.45, 0.0);
                XuanlanSkyVisual.ring(pose, vertices, center, 0.7 + (double)tier * 0.3, 0.045, tier == 1 ? 0xE6FFFF : 4709631, 0.8f * (1.0f - (float)tier * 0.18f), (double)age * 0.13 * (double)(tier == 1 ? -1 : 1), true);
            }
            XuanlanSkyVisual.ring(pose, vertices, floor, 0.5 + (double)progress * 2.8, 0.055, 4709631, fade, (double)age * 0.06, true);
            XuanlanWeaponVisual.line(pose, vertices, floor, floor.m_82520_(0.0, height, 0.0), camera, 0.16, 4709631, fade * 0.4f);
        }
    }

    private static void ring(PoseStack pose, VertexConsumer vertices, Vec3 center, double radius, double thickness, int color, float alpha, double rotation, boolean broken) {
        XuanlanWeaponVisual.ring(pose, vertices, center, EAST, SOUTH, radius, thickness, color, alpha, rotation, broken);
    }
}

