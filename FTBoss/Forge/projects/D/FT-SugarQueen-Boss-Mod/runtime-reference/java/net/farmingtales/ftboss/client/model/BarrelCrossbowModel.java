/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.block.model.ItemOverrides
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.ModelResourceLocation
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ModelEvent$ModifyBakingResult
 *  net.minecraftforge.client.event.ModelEvent$RegisterAdditional
 *  net.minecraftforge.client.model.BakedModelWrapper
 *  net.minecraftforge.client.model.data.ModelData
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  org.jetbrains.annotations.Nullable
 */
package net.farmingtales.ftboss.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public final class BarrelCrossbowModel
extends BakedModelWrapper<BakedModel> {
    private static final float PIVOT_X = 2.22703f;
    private static final float PIVOT_Z = -9.79505f;
    private static final float LIMB_ANGLE = 22.5f;
    private static final float LATCH_TRAVEL = 16.0f;
    private static final int POSE_STEPS = 100;
    private static final List<String> PARTS = List.of("body", "left", "right", "latch", "string_left", "string_right");
    private final List<List<BakedQuad>> parts;
    private final BakedModel[] poses = new BakedModel[101];
    private final ItemOverrides overrides;

    private BarrelCrossbowModel(BakedModel original, List<List<BakedQuad>> parts, final boolean alwaysLoaded) {
        super(original);
        this.parts = parts;
        this.overrides = new ItemOverrides(){

            /*
             * Enabled aggressive block sorting
             */
            public BakedModel m_173464_(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
                float progress;
                float f = progress = alwaysLoaded || BarrelCrossbowItem.isLoaded(stack) ? 1.0f : 0.0f;
                if (entity instanceof WineBarrelBossEntity) {
                    WineBarrelBossEntity boss = (WineBarrelBossEntity)entity;
                    if (entity.m_21205_() == stack) {
                        progress = boss.getCrossbowPull(Minecraft.m_91087_().m_91296_());
                        return BarrelCrossbowModel.this.pose(Math.round(Mth.m_14036_((float)progress, (float)0.0f, (float)1.0f) * 100.0f));
                    }
                }
                if (progress != 0.0f) return BarrelCrossbowModel.this.pose(Math.round(Mth.m_14036_((float)progress, (float)0.0f, (float)1.0f) * 100.0f));
                if (entity == null) return BarrelCrossbowModel.this.pose(Math.round(Mth.m_14036_((float)progress, (float)0.0f, (float)1.0f) * 100.0f));
                if (!entity.m_6117_()) return BarrelCrossbowModel.this.pose(Math.round(Mth.m_14036_((float)progress, (float)0.0f, (float)1.0f) * 100.0f));
                if (entity.m_21211_() != stack) return BarrelCrossbowModel.this.pose(Math.round(Mth.m_14036_((float)progress, (float)0.0f, (float)1.0f) * 100.0f));
                progress = ((float)entity.m_21252_() + Minecraft.m_91087_().m_91296_()) / (float)BarrelCrossbowItem.chargeTicks(stack);
                return BarrelCrossbowModel.this.pose(Math.round(Mth.m_14036_((float)progress, (float)0.0f, (float)1.0f) * 100.0f));
            }
        };
    }

    @SubscribeEvent
    public static void registerParts(ModelEvent.RegisterAdditional event) {
        for (String part : PARTS) {
            event.register(BarrelCrossbowModel.partId(part));
        }
    }

    @SubscribeEvent
    public static void bake(ModelEvent.ModifyBakingResult event) {
        Map models = event.getModels();
        ModelResourceLocation emptyId = new ModelResourceLocation("ftboss", "barrel_crossbow", "inventory");
        ModelResourceLocation readyId = new ModelResourceLocation("ftboss", "barrel_crossbow_ready", "inventory");
        BakedModel original = (BakedModel)models.get(emptyId);
        if (original == null) {
            throw new IllegalStateException("Missing barrel crossbow base model");
        }
        ArrayList<List<BakedQuad>> parts = new ArrayList<List<BakedQuad>>();
        for (String part : PARTS) {
            BakedModel bakedPart = (BakedModel)models.get(BarrelCrossbowModel.partId(part));
            if (bakedPart == null) {
                throw new IllegalStateException("Missing barrel crossbow part: " + part);
            }
            parts.add(List.copyOf(bakedPart.m_213637_(null, null, RandomSource.m_216335_((long)0L))));
        }
        models.put(emptyId, new BarrelCrossbowModel(original, parts, false));
        models.put(readyId, new BarrelCrossbowModel(original, parts, true));
    }

    private static ResourceLocation partId(String part) {
        return new ResourceLocation("ftboss", "item/barrel_crossbow_parts/" + part);
    }

    public ItemOverrides m_7343_() {
        return this.overrides;
    }

    private BakedModel pose(int step) {
        if (this.poses[step] == null) {
            float progress = (float)step / 100.0f;
            progress = progress * progress * (3.0f - 2.0f * progress);
            ArrayList<BakedQuad> quads = new ArrayList<BakedQuad>((Collection)this.parts.get(0));
            for (int part = 1; part < PARTS.size(); ++part) {
                for (BakedQuad quad : this.parts.get(part)) {
                    quads.add(BarrelCrossbowModel.transform(quad, part, progress));
                }
            }
            this.poses[step] = new PoseModel(this.originalModel, List.copyOf(quads));
        }
        return this.poses[step];
    }

    private static BakedQuad transform(BakedQuad quad, int part, float progress) {
        int[] vertices = (int[])quad.m_111303_().clone();
        int stride = vertices.length / 4;
        float angle = (part == 2 ? -1.0f : 1.0f) * 22.5f * ((float)Math.PI / 180) * progress;
        float cosine = Mth.m_14089_((float)angle);
        float sine = Mth.m_14031_((float)angle);
        Direction facing = quad.m_111306_();
        if (part <= 2) {
            facing = Direction.m_122372_((float)((float)facing.m_122429_() * cosine + (float)facing.m_122431_() * sine), (float)facing.m_122430_(), (float)((float)(-facing.m_122429_()) * sine + (float)facing.m_122431_() * cosine));
        }
        for (int vertex = 0; vertex < 4; ++vertex) {
            int offset = vertex * stride;
            float pointX = Float.intBitsToFloat(vertices[offset]) * 16.0f;
            float pointZ = Float.intBitsToFloat(vertices[offset + 2]) * 16.0f;
            if (part <= 2) {
                float pivotX = part == 1 ? 2.22703f : 13.77297f;
                float localX = pointX - pivotX;
                float localZ = pointZ - -9.79505f;
                pointX = pivotX + localX * cosine + localZ * sine;
                pointZ = -9.79505f - localX * sine + localZ * cosine;
                int normal = vertices[offset + 7];
                float normalX = (byte)normal;
                float normalZ = (byte)(normal >> 16);
                vertices[offset + 7] = normal & 0xFF00FF00 | Math.round(normalX * cosine + normalZ * sine) & 0xFF | (Math.round(-normalX * sine + normalZ * cosine) & 0xFF) << 16;
            } else if (part == 3) {
                pointZ += 16.0f * progress;
            } else {
                boolean left = part == 4;
                float side = left ? 1.0f : -1.0f;
                float pivotX = left ? 2.22703f : 13.77297f;
                float tipX = left ? -4.0f : 20.0f;
                float tipZ = -1.0f;
                float localX = tipX - pivotX;
                float localZ = tipZ - -9.79505f;
                tipX = pivotX + localX * cosine + localZ * sine * side;
                tipZ = -9.79505f - localX * sine * side + localZ * cosine;
                float along = left ? (pointX + 4.0f) / 12.0f : (20.0f - pointX) / 12.0f;
                pointX = Mth.m_14179_((float)along, (float)tipX, (float)8.0f);
                pointZ += Mth.m_14179_((float)along, (float)(tipZ + 1.0f), (float)(16.0f * progress));
            }
            vertices[offset] = Float.floatToRawIntBits(pointX / 16.0f);
            vertices[offset + 2] = Float.floatToRawIntBits(pointZ / 16.0f);
        }
        return new BakedQuad(vertices, quad.m_111305_(), facing, quad.m_173410_(), quad.m_111307_());
    }

    private static final class PoseModel
    extends BakedModelWrapper<BakedModel> {
        private final List<BakedQuad> quads;

        private PoseModel(BakedModel original, List<BakedQuad> quads) {
            super(original);
            this.quads = quads;
        }

        public List<BakedQuad> m_213637_(@Nullable BlockState state, @Nullable Direction side, RandomSource random) {
            return side == null ? this.quads : List.of();
        }

        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random, ModelData data, @Nullable RenderType renderType) {
            return this.m_213637_(state, side, random);
        }

        public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) {
            return List.of(this);
        }

        public BakedModel applyTransform(ItemDisplayContext context, PoseStack poseStack, boolean leftHand) {
            this.originalModel.applyTransform(context, poseStack, leftHand);
            return this;
        }
    }
}

