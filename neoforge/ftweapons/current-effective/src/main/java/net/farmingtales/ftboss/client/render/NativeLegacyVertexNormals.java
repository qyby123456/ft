package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Vector3f;

public final class NativeLegacyVertexNormals {
    private NativeLegacyVertexNormals() {
    }

    public static VertexConsumer normal(VertexConsumer vertices, Matrix3f matrix, float normalX, float normalY, float normalZ) {
        Vector3f transformed = matrix.transform(new Vector3f(normalX, normalY, normalZ));
        return vertices.setNormal(transformed.x(), transformed.y(), transformed.z());
    }
}
