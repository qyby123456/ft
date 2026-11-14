package net.farmingtales.ftboss.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.WineBarrelBossEntity;
import net.farmingtales.ftboss.item.BarrelCrossbowItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BarrelCrossbowModel extends BakedModelWrapper<BakedModel> {
    private static final float PIVOT_X = 2.22703F;
    private static final float PIVOT_Z = -9.79505F;
    private static final float LIMB_ANGLE = 22.5F;
    private static final float LATCH_TRAVEL = 16.0F;
    private static final int POSE_STEPS = 100;
    private static final List<String> PARTS = List.of("body", "left", "right", "latch", "string_left", "string_right");
    private final List<List<BakedQuad>> parts;
    private final BakedModel[] poses = new BakedModel[POSE_STEPS + 1];
    private final ItemOverrides overrides;

    private BarrelCrossbowModel(BakedModel original, List<List<BakedQuad>> parts, boolean alwaysLoaded) {
        super(original);
        this.parts = parts;
        this.overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
                                      @Nullable LivingEntity entity, int seed) {
                float progress = alwaysLoaded || BarrelCrossbowItem.isLoaded(stack) ? 1.0F : 0.0F;
                if (entity instanceof WineBarrelBossEntity boss && entity.getMainHandItem() == stack) {
                    progress = boss.getCrossbowPull(Minecraft.getInstance().getFrameTime());
                } else if (progress == 0.0F && entity != null && entity.isUsingItem() && entity.getUseItem() == stack) {
                    progress = (entity.getTicksUsingItem() + Minecraft.getInstance().getFrameTime())
                            / BarrelCrossbowItem.chargeTicks(stack);
                }
                return pose(Math.round(Mth.clamp(progress, 0.0F, 1.0F) * POSE_STEPS));
            }
        };
    }

    @SubscribeEvent
    public static void registerParts(ModelEvent.RegisterAdditional event) {
        for (String part : PARTS) event.register(partId(part));
    }

    @SubscribeEvent
    public static void bake(ModelEvent.ModifyBakingResult event) {
        Map<ResourceLocation, BakedModel> models = event.getModels();
        ModelResourceLocation emptyId = new ModelResourceLocation(FTBossMod.MOD_ID, "barrel_crossbow", "inventory");
        ModelResourceLocation readyId = new ModelResourceLocation(FTBossMod.MOD_ID, "barrel_crossbow_ready", "inventory");
        BakedModel original = models.get(emptyId);
        if (original == null) throw new IllegalStateException("Missing barrel crossbow base model");
        List<List<BakedQuad>> parts = new ArrayList<>();
        for (String part : PARTS) {
            BakedModel bakedPart = models.get(partId(part));
            if (bakedPart == null) throw new IllegalStateException("Missing barrel crossbow part: " + part);
            parts.add(List.copyOf(bakedPart.getQuads(null, null, RandomSource.create(0L))));
        }
        models.put(emptyId, new BarrelCrossbowModel(original, parts, false));
        models.put(readyId, new BarrelCrossbowModel(original, parts, true));
    }

    private static ResourceLocation partId(String part) {
        return new ResourceLocation(FTBossMod.MOD_ID, "item/barrel_crossbow_parts/" + part);
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrides;
    }

    private BakedModel pose(int step) {
        if (this.poses[step] == null) {
            float progress = step / (float) POSE_STEPS;
            progress = progress * progress * (3.0F - 2.0F * progress);
            List<BakedQuad> quads = new ArrayList<>(this.parts.get(0));
            for (int part = 1; part < PARTS.size(); part++) {
                for (BakedQuad quad : this.parts.get(part)) quads.add(transform(quad, part, progress));
            }
            this.poses[step] = new PoseModel(this.originalModel, List.copyOf(quads));
        }
        return this.poses[step];
    }

    private static BakedQuad transform(BakedQuad quad, int part, float progress) {
        int[] vertices = quad.getVertices().clone();
        int stride = vertices.length / 4;
        float angle = (part == 2 ? -1.0F : 1.0F) * LIMB_ANGLE * Mth.DEG_TO_RAD * progress;
        float cosine = Mth.cos(angle);
        float sine = Mth.sin(angle);
        Direction facing = quad.getDirection();
        if (part <= 2) {
            facing = Direction.getNearest(facing.getStepX() * cosine + facing.getStepZ() * sine,
                    facing.getStepY(), -facing.getStepX() * sine + facing.getStepZ() * cosine);
        }
        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * stride;
            float pointX = Float.intBitsToFloat(vertices[offset]) * 16.0F;
            float pointZ = Float.intBitsToFloat(vertices[offset + 2]) * 16.0F;
            if (part <= 2) {
                float pivotX = part == 1 ? PIVOT_X : 16.0F - PIVOT_X;
                float localX = pointX - pivotX;
                float localZ = pointZ - PIVOT_Z;
                pointX = pivotX + localX * cosine + localZ * sine;
                pointZ = PIVOT_Z - localX * sine + localZ * cosine;
                int normal = vertices[offset + 7];
                float normalX = (byte) normal;
                float normalZ = (byte) (normal >> 16);
                vertices[offset + 7] = (normal & 0xFF00FF00)
                        | (Math.round(normalX * cosine + normalZ * sine) & 255)
                        | ((Math.round(-normalX * sine + normalZ * cosine) & 255) << 16);
            } else if (part == 3) {
                pointZ += LATCH_TRAVEL * progress;
            } else {
                boolean left = part == 4;
                float side = left ? 1.0F : -1.0F;
                float pivotX = left ? PIVOT_X : 16.0F - PIVOT_X;
                float tipX = left ? -4.0F : 20.0F;
                float tipZ = -1.0F;
                float localX = tipX - pivotX;
                float localZ = tipZ - PIVOT_Z;
                tipX = pivotX + localX * cosine + localZ * sine * side;
                tipZ = PIVOT_Z - localX * sine * side + localZ * cosine;
                float along = left ? (pointX + 4.0F) / 12.0F : (20.0F - pointX) / 12.0F;
                pointX = Mth.lerp(along, tipX, 8.0F);
                pointZ += Mth.lerp(along, tipZ + 1.0F, LATCH_TRAVEL * progress);
            }
            vertices[offset] = Float.floatToRawIntBits(pointX / 16.0F);
            vertices[offset + 2] = Float.floatToRawIntBits(pointZ / 16.0F);
        }
        return new BakedQuad(vertices, quad.getTintIndex(), facing, quad.getSprite(), quad.isShade());
    }

    private static final class PoseModel extends BakedModelWrapper<BakedModel> {
        private final List<BakedQuad> quads;

        private PoseModel(BakedModel original, List<BakedQuad> quads) {
            super(original);
            this.quads = quads;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
            return side == null ? this.quads : List.of();
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random,
                                        ModelData data, @Nullable RenderType renderType) {
            return getQuads(state, side, random);
        }

        @Override
        public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
            return List.of(this);
        }

        @Override
        public BakedModel applyTransform(ItemDisplayContext context, PoseStack poseStack, boolean leftHand) {
            this.originalModel.applyTransform(context, poseStack, leftHand);
            return this;
        }
    }
}
