package net.farmingtales.ftdungeonmobs.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.farmingtales.ftdungeonmobs.DungeonGateBlock;
import net.farmingtales.ftdungeonmobs.DungeonGateBlockEntity;
import net.farmingtales.ftdungeonmobs.DungeonGateMotion;
import net.farmingtales.ftdungeonmobs.FTDungeonMobs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public final class DungeonGateRenderer implements BlockEntityRenderer<DungeonGateBlockEntity> {
    private final RandomSource random = RandomSource.create(0);

    public DungeonGateRenderer(BlockEntityRendererProvider.Context context) {}

    public static ResourceLocation movingModel(int part) {
        return FTDungeonMobs.resource("block/dungeon_gate_moving_" + part);
    }

    @Override
    public void render(DungeonGateBlockEntity entity, float partialTick, PoseStack pose, MultiBufferSource buffers,
            int light, int overlay) {
        if (entity.getLevel() == null || !entity.getBlockState().getValue(DungeonGateBlock.MOVING)) {
            return;
        }
        float sink = entity.progress(partialTick) * DungeonGateMotion.SINK_UNITS / 16.0F;
        Direction facing = entity.getBlockState().getValue(DungeonGateBlock.FACING);
        int turns = switch (facing) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
        pose.pushPose();
        pose.translate(0.5, 0, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-turns * 90));
        pose.translate(-0.5, 0, -0.5);
        VertexConsumer consumer = buffers.getBuffer(RenderType.solid());
        for (int part = 0; part < 12; part++) {
            BakedModel model = Minecraft.getInstance().getModelManager().getModel(movingModel(part));
            random.setSeed(0);
            for (BakedQuad quad : model.getQuads(null, null, random)) {
                renderQuad(quad, part, sink, pose.last(), consumer, light, overlay,
                        worldShade(entity, quad, turns));
            }
            for (Direction side : Direction.values()) {
                random.setSeed(0);
                for (BakedQuad quad : model.getQuads(null, side, random)) {
                    renderQuad(quad, part, sink, pose.last(), consumer, light, overlay,
                            worldShade(entity, quad, turns));
                }
            }
        }
        pose.popPose();
    }

    private static float worldShade(DungeonGateBlockEntity entity, BakedQuad quad, int turns) {
        Direction normal = quad.getDirection();
        for (int turn = 0; turn < turns && normal.getAxis().isHorizontal(); turn++) {
            normal = normal.getClockWise();
        }
        return entity.getLevel().getShade(normal, quad.isShade());
    }

    private static void renderQuad(BakedQuad quad, int part, float sink, PoseStack.Pose pose, VertexConsumer consumer,
            int light, int overlay, float shade) {
        int[] data = quad.getVertices();
        int stride = data.length / 4;
        float[] vertices = new float[20];
        for (int vertex = 0; vertex < 4; vertex++) {
            int source = vertex * stride;
            int target = vertex * 5;
            vertices[target] = Float.intBitsToFloat(data[source]) + part % 3 - 1;
            vertices[target + 1] = Float.intBitsToFloat(data[source + 1]) + part / 3 - sink;
            vertices[target + 2] = Float.intBitsToFloat(data[source + 2]);
            vertices[target + 3] = Float.intBitsToFloat(data[source + 4]);
            vertices[target + 4] = Float.intBitsToFloat(data[source + 5]);
        }
        float[] clipped = DungeonGateMotion.clipFloor(vertices);
        if (clipped == null) {
            return;
        }
        Direction normal = quad.getDirection();
        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * 5;
            consumer.vertex(pose.pose(), clipped[offset], clipped[offset + 1], clipped[offset + 2])
                    .color(shade, shade, shade, 1).uv(clipped[offset + 3], clipped[offset + 4])
                    .overlayCoords(overlay).uv2(light)
                    .normal(pose.normal(), normal.getStepX(), normal.getStepY(), normal.getStepZ()).endVertex();
        }
    }

    @Override
    public int getViewDistance() {
        return 64;
    }
}
