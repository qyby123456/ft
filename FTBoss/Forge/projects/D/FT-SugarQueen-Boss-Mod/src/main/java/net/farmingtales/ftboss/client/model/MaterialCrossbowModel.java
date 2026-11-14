package net.farmingtales.ftboss.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.MaterialCrossbows;
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
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class MaterialCrossbowModel extends BakedModelWrapper<BakedModel> {
    private static final int POSE_STEPS = 32;
    private static final float LIMB_ANGLE = 22.5F;
    private static final float LATCH_TRAVEL = 9.729312F;
    private static final float PIVOT_LEFT_X = 6.0F;
    private static final float PIVOT_RIGHT_X = 10.0F;
    private static final float PIVOT_Z = -2.5F;
    private static final float TIP_LEFT_X = -6.786898F;
    private static final float TIP_RIGHT_X = 22.786898F;
    private static final float TIP_Z = -0.675171F;
    private static final float STRING_INNER_LEFT_X = 6.895213F;
    private static final float STRING_INNER_RIGHT_X = 9.104787F;
    private static final float STRING_INNER_Z = 9.054141F;
    private static final float STRING_CENTER_X = 8.0F;
    private static final List<String> PARTS = List.of("body", "left", "right", "latch", "string_left", "string_right", "arrow", "firework");
    private final List<List<BakedQuad>> parts;
    private final BakedModel[][] poses = new BakedModel[3][POSE_STEPS + 1];
    private final ItemOverrides overrides;

    private MaterialCrossbowModel(BakedModel original, List<List<BakedQuad>> parts) {
        super(original);
        this.parts = parts;
        this.overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
                                      @Nullable LivingEntity entity, int seed) {
                boolean charged = CrossbowItem.isCharged(stack);
                float progress = charged ? 1.0F : 0.0F;
                if (!charged && entity != null && entity.isUsingItem() && entity.getUseItem() == stack) {
                    progress = (entity.getTicksUsingItem() + Minecraft.getInstance().getFrameTime())
                            / Math.max(1, CrossbowItem.getChargeDuration(stack));
                }
                int ammunition = charged ? (CrossbowItem.containsChargedProjectile(stack, Items.FIREWORK_ROCKET) ? 2 : 1) : 0;
                return pose(Math.round(Mth.clamp(progress, 0.0F, 1.0F) * POSE_STEPS), ammunition);
            }
        };
    }

    @SubscribeEvent
    public static void registerParts(ModelEvent.RegisterAdditional event) {
        for (String id : MaterialCrossbows.IDS) {
            for (String part : PARTS) event.register(partId(id, part));
        }
    }

    @SubscribeEvent
    public static void bake(ModelEvent.ModifyBakingResult event) {
        Map<ResourceLocation, BakedModel> models = event.getModels();
        for (String id : MaterialCrossbows.IDS) {
            ModelResourceLocation inventory = new ModelResourceLocation(FTBossMod.MOD_ID, id, "inventory");
            BakedModel original = models.get(inventory);
            if (original == null) throw new IllegalStateException("Missing material crossbow: " + id);
            List<List<BakedQuad>> parts = new ArrayList<>();
            for (String part : PARTS) {
                BakedModel baked = models.get(partId(id, part));
                if (baked == null) throw new IllegalStateException("Missing material crossbow part: " + id + "/" + part);
                parts.add(List.copyOf(baked.getQuads(null, null, RandomSource.create(0L))));
            }
            models.put(inventory, new MaterialCrossbowModel(original, List.copyOf(parts)));
        }
    }

    private static ResourceLocation partId(String id, String part) {
        return new ResourceLocation(FTBossMod.MOD_ID, "item/" + id + "_parts/" + part);
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrides;
    }

    private BakedModel pose(int step, int ammunition) {
        if (this.poses[ammunition][step] == null) {
            float progress = step / (float) POSE_STEPS;
            progress = progress * progress * (3.0F - 2.0F * progress);
            List<BakedQuad> quads = new ArrayList<>(this.parts.get(0));
            for (int part = 1; part <= 5; part++) {
                for (BakedQuad quad : this.parts.get(part)) quads.add(transform(quad, part, progress));
            }
            if (ammunition != 0) quads.addAll(this.parts.get(ammunition == 1 ? 6 : 7));
            this.poses[ammunition][step] = new PoseModel(this.originalModel, List.copyOf(quads));
        }
        return this.poses[ammunition][step];
    }

    private static BakedQuad transform(BakedQuad quad, int part, float progress) {
        int[] vertices = quad.getVertices().clone();
        int stride = vertices.length / 4;
        boolean left = part == 1 || part == 4;
        float side = left ? 1.0F : -1.0F;
        float angle = side * LIMB_ANGLE * Mth.DEG_TO_RAD * progress;
        float cosine = Mth.cos(angle);
        float sine = Mth.sin(angle);
        float pivotX = left ? PIVOT_LEFT_X : PIVOT_RIGHT_X;
        float tipX = left ? TIP_LEFT_X : TIP_RIGHT_X;
        float innerX = left ? STRING_INNER_LEFT_X : STRING_INNER_RIGHT_X;
        float localX = tipX - pivotX;
        float relativeTipZ = TIP_Z - PIVOT_Z;
        float tipNowZ = PIVOT_Z - localX * sine + relativeTipZ * cosine;
        float tipNowX = pivotX + localX * cosine + relativeTipZ * sine;
        float latchZ = STRING_INNER_Z + LATCH_TRAVEL * (progress - 1.0F);
        float normalAngle = part <= 2 ? angle
                : part >= 4 ? (float) Math.atan2(side * (tipNowZ - latchZ), Math.abs(tipNowX - STRING_CENTER_X))
                : 0.0F;
        float normalCosine = Mth.cos(normalAngle);
        float normalSine = Mth.sin(normalAngle);
        Direction direction = quad.getDirection();
        Direction facing = Direction.getNearest(direction.getStepX() * normalCosine + direction.getStepZ() * normalSine,
                direction.getStepY(), -direction.getStepX() * normalSine + direction.getStepZ() * normalCosine);
        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * stride;
            float pointX = Float.intBitsToFloat(vertices[offset]) * 16.0F;
            float pointZ = Float.intBitsToFloat(vertices[offset + 2]) * 16.0F;
            if (part <= 2) {
                float relativeX = pointX - pivotX;
                float relativeZ = pointZ - PIVOT_Z;
                pointX = pivotX + relativeX * cosine + relativeZ * sine;
                pointZ = PIVOT_Z - relativeX * sine + relativeZ * cosine;
            } else if (part == 3) {
                pointZ += LATCH_TRAVEL * progress;
            } else {
                float along = (pointX - tipX) / (innerX - tipX);
                pointX = Mth.lerp(along, tipNowX, innerX);
                pointZ = Mth.lerp(along, tipNowZ, latchZ) + (pointZ - TIP_Z);
            }
            vertices[offset] = Float.floatToRawIntBits(pointX / 16.0F);
            vertices[offset + 2] = Float.floatToRawIntBits(pointZ / 16.0F);
            int normal = vertices[offset + 7];
            float normalX = (byte) normal;
            float normalZ = (byte) (normal >> 16);
            vertices[offset + 7] = (normal & 0xFF00FF00)
                    | (Math.round(normalX * normalCosine + normalZ * normalSine) & 255)
                    | ((Math.round(-normalX * normalSine + normalZ * normalCosine) & 255) << 16);
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
