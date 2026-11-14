package net.farmingtales.ftboss.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Arrays;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class ZheergenAfterimageMesh implements VertexConsumer {
    public static final int STRIDE = 8;
    private final Matrix4f transform;
    private final Matrix3f normals;
    private final Vector3f point = new Vector3f();
    private final Vector3f normal = new Vector3f();
    private float[] data = new float[STRIDE * 256];
    private int size;
    private float textureU;
    private float textureV;

    public ZheergenAfterimageMesh(Matrix4f transform) {
        this.transform = new Matrix4f(transform);
        normals = new Matrix3f(transform).normal();
    }

    public float[] snapshot() { return Arrays.copyOf(data, size); }

    @Override
    public VertexConsumer vertex(double positionX, double positionY, double positionZ) {
        transform.transformPosition(point.set((float) positionX, (float) positionY, (float) positionZ));
        return this;
    }

    @Override
    public VertexConsumer normal(float normalX, float normalY, float normalZ) {
        normals.transform(normal.set(normalX, normalY, normalZ)).normalize();
        return this;
    }

    @Override public VertexConsumer uv(float textureU, float textureV) { this.textureU = textureU; this.textureV = textureV; return this; }
    @Override public VertexConsumer color(int red, int green, int blue, int alpha) { return this; }
    @Override public VertexConsumer overlayCoords(int first, int second) { return this; }
    @Override public VertexConsumer uv2(int first, int second) { return this; }
    @Override public void defaultColor(int red, int green, int blue, int alpha) {}
    @Override public void unsetDefaultColor() {}

    @Override
    public void endVertex() {
        if (size + STRIDE > data.length) data = Arrays.copyOf(data, data.length * 2);
        data[size++] = point.x;
        data[size++] = point.y;
        data[size++] = point.z;
        data[size++] = textureU;
        data[size++] = textureV;
        data[size++] = normal.x;
        data[size++] = normal.y;
        data[size++] = normal.z;
    }
}
