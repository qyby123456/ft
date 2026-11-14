package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.farmingtales.ftboss.skill.XuanlanFxPacket;
import net.farmingtales.ftboss.util.XuanlanSkyRules;
import net.minecraft.world.phys.Vec3;

public final class XuanlanSkyVisual {
    private static final int CYAN = 0x47DCFF;
    private static final int WHITE = 0xE6FFFF;
    private static final int WARNING = 0xFF8052;
    private static final Vec3 EAST = new Vec3(1, 0, 0);
    private static final Vec3 SOUTH = new Vec3(0, 0, 1);

    private XuanlanSkyVisual() {}

    public static boolean handles(int kind) {
        return kind >= XuanlanFxPacket.SKY_ASCEND && kind <= XuanlanFxPacket.SKY_CANCEL;
    }

    public static int lifetime(int kind) {
        return switch (kind) {
            case XuanlanFxPacket.SKY_WARNING -> XuanlanSkyRules.WARNING_TICKS;
            case XuanlanFxPacket.SKY_ASCEND -> XuanlanSkyRules.ASCEND_TICKS;
            case XuanlanFxPacket.SKY_DESCEND -> XuanlanSkyRules.DESCEND_TICKS;
            default -> 14;
        };
    }

    public static void render(PoseStack pose, VertexConsumer vertices, XuanlanFxPacket packet, Vec3 camera,
                              float age, float life) {
        float progress = Math.max(0, Math.min(1, age / life));
        float fade = 1 - progress;
        Vec3 floor = packet.from();
        double radius = XuanlanSkyRules.RADIUS;
        if (packet.kind() == XuanlanFxPacket.SKY_WARNING) {
            float pulse = 0.65F + 0.25F * (float) Math.sin(age * 0.9);
            ring(pose, vertices, floor, radius, 0.06, WARNING, pulse, 0, false);
            ring(pose, vertices, floor.add(0, 0.015, 0), radius * 0.9, 0.035, CYAN, 0.8F, -age * 0.045, true);
            ring(pose, vertices, floor.add(0, 0.03, 0), radius * fade, 0.045, WHITE, 0.9F, 0, false);
            for (int spoke = 0; spoke < 4; spoke++) {
                double angle = spoke * Math.PI / 2;
                Vec3 direction = new Vec3(Math.cos(angle), 0, Math.sin(angle));
                XuanlanWeaponVisual.line(pose, vertices, floor.add(direction.scale(0.3)),
                        floor.add(direction.scale(0.85)), camera, 0.035, WARNING, pulse);
            }
            XuanlanWeaponVisual.line(pose, vertices, floor, packet.to(), camera, 0.018, CYAN, 0.18F + progress * 0.25F);
        } else if (packet.kind() == XuanlanFxPacket.SKY_IMPACT) {
            XuanlanWeaponVisual.line(pose, vertices, floor, packet.to(), camera, radius, CYAN, fade * 0.12F);
            XuanlanWeaponVisual.line(pose, vertices, floor, packet.to(), camera, 0.65 * fade, CYAN, fade * 0.7F);
            XuanlanWeaponVisual.line(pose, vertices, floor, packet.to(), camera, 0.2 * fade, WHITE, fade);
            for (int strand = 0; strand < 8; strand++) {
                double angle = strand * Math.PI / 4 + age * 0.04;
                Vec3 offset = new Vec3(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
                XuanlanWeaponVisual.line(pose, vertices, floor.add(offset), packet.to().add(offset), camera,
                        0.018 * fade, CYAN, fade * 0.35F);
            }
            ring(pose, vertices, floor, radius, 0.12 * fade, WHITE, fade, 0, false);
            ring(pose, vertices, floor.add(0, 0.06, 0), radius * (0.3 + progress * 1.25),
                    0.07, CYAN, fade, age * 0.1, true);
            ring(pose, vertices, floor.add(0, 0.1 + age * 0.17, 0), radius * fade, 0.05, WHITE, fade * 0.7F, 0, false);
        } else {
            boolean ascent = packet.kind() == XuanlanFxPacket.SKY_ASCEND;
            double height = XuanlanSkyRules.HEIGHT * XuanlanSkyRules.rise(ascent ? progress : fade);
            for (int tier = 0; tier < 3; tier++) {
                Vec3 center = floor.add(0, height + tier * 0.45, 0);
                ring(pose, vertices, center, 0.7 + tier * 0.3, 0.045, tier == 1 ? WHITE : CYAN,
                        0.8F * (1 - tier * 0.18F), age * 0.13 * (tier == 1 ? -1 : 1), true);
            }
            ring(pose, vertices, floor, 0.5 + progress * 2.8, 0.055, CYAN, fade, age * 0.06, true);
            XuanlanWeaponVisual.line(pose, vertices, floor, floor.add(0, height, 0), camera, 0.16, CYAN, fade * 0.4F);
        }
    }

    private static void ring(PoseStack pose, VertexConsumer vertices, Vec3 center, double radius, double thickness,
                             int color, float alpha, double rotation, boolean broken) {
        XuanlanWeaponVisual.ring(pose, vertices, center, EAST, SOUTH, radius, thickness, color, alpha, rotation, broken);
    }
}
