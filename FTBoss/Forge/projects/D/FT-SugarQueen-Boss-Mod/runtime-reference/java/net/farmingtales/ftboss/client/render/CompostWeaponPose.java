/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.math.Axis
 */
package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

public final class CompostWeaponPose {
    public static final float SCALE = 0.63f;
    public static final float GRIP_OFFSET_X = 0.0f;
    public static final float GRIP_OFFSET_Y = -1.8f;
    public static final float GRIP_OFFSET_Z = -0.5f;
    public static final float MODEL_GRIP_X = 8.0f;
    public static final float MODEL_GRIP_Y = -0.5f;
    public static final float MODEL_GRIP_Z = 8.0f;

    private CompostWeaponPose() {
    }

    public static void apply(PoseStack poseStack) {
        poseStack.m_252880_(0.0f, -0.1125f, -0.03125f);
        poseStack.m_252781_(Axis.f_252529_.m_252977_(-90.0f));
        poseStack.m_252781_(Axis.f_252436_.m_252977_(90.0f));
        poseStack.m_85841_(0.63f, 0.63f, 0.63f);
        poseStack.m_252880_(0.0f, 0.53125f, 0.0f);
    }
}

