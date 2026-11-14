package net.farmingtales.ftboss.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Vector3f;

public final class NativeLegacyVertex {
    private NativeLegacyVertex() {
    }

    public static VertexConsumer normal(VertexConsumer consumer, Matrix3f matrix, float horizontal, float vertical, float depth) {
        Vector3f transformed = matrix.transform(new Vector3f(horizontal, vertical, depth));
        return consumer.setNormal(transformed.x(), transformed.y(), transformed.z());
    }

    public static VertexConsumer normal(VertexConsumer consumer, PoseStack.Pose pose, float horizontal, float vertical, float depth) {
        return normal(consumer, pose.normal(), horizontal, vertical, depth);
    }
}
