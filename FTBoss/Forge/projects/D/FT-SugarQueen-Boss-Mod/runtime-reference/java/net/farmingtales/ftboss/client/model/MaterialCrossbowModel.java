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
 *  net.minecraft.world.item.CrossbowItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
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
import net.minecraft.world.item.Item;
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

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public final class MaterialCrossbowModel
extends BakedModelWrapper<BakedModel> {
    private static final int POSE_STEPS = 32;
    private static final float LIMB_ANGLE = 22.5f;
    private static final float LATCH_TRAVEL = 9.0f;
    private static final List<String> PARTS = List.of("body", "left", "right", "latch", "string_left", "string_right", "arrow", "firework");
    private final List<List<BakedQuad>> parts;
    private final BakedModel[][] poses = new BakedModel[3][33];
    private final ItemOverrides overrides;

    private MaterialCrossbowModel(BakedModel bakedModel, List<List<BakedQuad>> list) {
        super(bakedModel);
        this.parts = list;
        this.overrides = new ItemOverrides(){

            public BakedModel m_173464_(BakedModel bakedModel, ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n) {
                float f;
                boolean bl = CrossbowItem.m_40932_((ItemStack)itemStack);
                float f2 = f = bl ? 1.0f : 0.0f;
                if (!bl && livingEntity != null && livingEntity.m_6117_() && livingEntity.m_21211_() == itemStack) {
                    f = ((float)livingEntity.m_21252_() + Minecraft.m_91087_().m_91296_()) / (float)Math.max(1, CrossbowItem.m_40939_((ItemStack)itemStack));
                }
                int n2 = bl ? (CrossbowItem.m_40871_((ItemStack)itemStack, (Item)Items.f_42688_) ? 2 : 1) : 0;
                return MaterialCrossbowModel.this.pose(Math.round(Mth.m_14036_((float)f, (float)0.0f, (float)1.0f) * 32.0f), n2);
            }
        };
    }

    @SubscribeEvent
    public static void registerParts(ModelEvent.RegisterAdditional registerAdditional) {
        for (String string : MaterialCrossbows.IDS) {
            for (String string2 : PARTS) {
                registerAdditional.register(MaterialCrossbowModel.partId(string, string2));
            }
        }
    }

    @SubscribeEvent
    public static void bake(ModelEvent.ModifyBakingResult modifyBakingResult) {
        Map map = modifyBakingResult.getModels();
        for (String string : MaterialCrossbows.IDS) {
            ModelResourceLocation modelResourceLocation = new ModelResourceLocation("ftboss", string, "inventory");
            BakedModel bakedModel = (BakedModel)map.get(modelResourceLocation);
            if (bakedModel == null) {
                throw new IllegalStateException("Missing material crossbow: " + string);
            }
            ArrayList arrayList = new ArrayList();
            for (String string2 : PARTS) {
                BakedModel bakedModel2 = (BakedModel)map.get(MaterialCrossbowModel.partId(string, string2));
                if (bakedModel2 == null) {
                    throw new IllegalStateException("Missing material crossbow part: " + string + "/" + string2);
                }
                arrayList.add(List.copyOf(bakedModel2.m_213637_(null, null, RandomSource.m_216335_((long)0L))));
            }
            map.put(modelResourceLocation, new MaterialCrossbowModel(bakedModel, List.copyOf(arrayList)));
        }
    }

    private static ResourceLocation partId(String string, String string2) {
        return new ResourceLocation("ftboss", "item/" + string + "_parts/" + string2);
    }

    public ItemOverrides m_7343_() {
        return this.overrides;
    }

    private BakedModel pose(int n, int n2) {
        if (this.poses[n2][n] == null) {
            float f = (float)n / 32.0f;
            f = f * f * (3.0f - 2.0f * f);
            ArrayList<BakedQuad> arrayList = new ArrayList<BakedQuad>((Collection)this.parts.get(0));
            for (int i = 1; i <= 5; ++i) {
                for (BakedQuad bakedQuad : this.parts.get(i)) {
                    arrayList.add(MaterialCrossbowModel.transform(bakedQuad, i, f));
                }
            }
            if (n2 != 0) {
                arrayList.addAll((Collection)this.parts.get(n2 == 1 ? 6 : 7));
            }
            this.poses[n2][n] = new PoseModel(this.originalModel, List.copyOf(arrayList));
        }
        return this.poses[n2][n];
    }

    private static BakedQuad transform(BakedQuad bakedQuad, int n, float f) {
        int[] nArray = (int[])bakedQuad.m_111303_().clone();
        int n2 = nArray.length / 4;
        boolean bl = n == 1 || n == 4;
        float f2 = bl ? 1.0f : -1.0f;
        float f3 = f2 * 22.5f * ((float)Math.PI / 180) * f;
        float f4 = Mth.m_14089_((float)f3);
        float f5 = Mth.m_14031_((float)f3);
        float f6 = bl ? 4.0f : 12.0f;
        float f7 = bl ? -6.0f : 22.0f;
        float f8 = f7 - f6;
        float f9 = 1.0f - f8 * f5 - 3.0f * f4;
        f7 = f6 + f8 * f4 - 3.0f * f5;
        float f10 = -2.0f + 9.0f * f;
        float f11 = n <= 2 ? f3 : (n >= 4 ? (float)Math.atan2(f2 * (f9 - f10), Math.abs(f7 - 8.0f)) : 0.0f);
        float f12 = Mth.m_14089_((float)f11);
        float f13 = Mth.m_14031_((float)f11);
        Direction direction = bakedQuad.m_111306_();
        Direction direction2 = Direction.m_122372_((float)((float)direction.m_122429_() * f12 + (float)direction.m_122431_() * f13), (float)direction.m_122430_(), (float)((float)(-direction.m_122429_()) * f13 + (float)direction.m_122431_() * f12));
        for (int i = 0; i < 4; ++i) {
            float f14;
            int n3 = i * n2;
            float f15 = Float.intBitsToFloat(nArray[n3]) * 16.0f;
            float f16 = Float.intBitsToFloat(nArray[n3 + 2]) * 16.0f;
            if (n <= 2) {
                float f17 = f15 - f6;
                f14 = f16 - 1.0f;
                f15 = f6 + f17 * f4 + f14 * f5;
                f16 = 1.0f - f17 * f5 + f14 * f4;
            } else if (n == 3) {
                f16 += 9.0f * f;
            } else {
                float f18 = bl ? (f15 + 6.0f) / 14.0f : (22.0f - f15) / 14.0f;
                f15 = Mth.m_14179_((float)f18, (float)f7, (float)8.0f);
                f16 = Mth.m_14179_((float)f18, (float)f9, (float)f10) + f16 + 2.0f;
            }
            nArray[n3] = Float.floatToRawIntBits(f15 / 16.0f);
            nArray[n3 + 2] = Float.floatToRawIntBits(f16 / 16.0f);
            int n4 = nArray[n3 + 7];
            f14 = (byte)n4;
            float f19 = (byte)(n4 >> 16);
            nArray[n3 + 7] = n4 & 0xFF00FF00 | Math.round(f14 * f12 + f19 * f13) & 0xFF | (Math.round(-f14 * f13 + f19 * f12) & 0xFF) << 16;
        }
        return new BakedQuad(nArray, bakedQuad.m_111305_(), direction2, bakedQuad.m_173410_(), bakedQuad.m_111307_());
    }

    private static final class PoseModel
    extends BakedModelWrapper<BakedModel> {
        private final List<BakedQuad> quads;

        private PoseModel(BakedModel bakedModel, List<BakedQuad> list) {
            super(bakedModel);
            this.quads = list;
        }

        public List<BakedQuad> m_213637_(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource) {
            return direction == null ? this.quads : List.of();
        }

        public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource, ModelData modelData, @Nullable RenderType renderType) {
            return this.m_213637_(blockState, direction, randomSource);
        }

        public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean bl) {
            return List.of(this);
        }

        public BakedModel applyTransform(ItemDisplayContext itemDisplayContext, PoseStack poseStack, boolean bl) {
            this.originalModel.applyTransform(itemDisplayContext, poseStack, bl);
            return this;
        }
    }
}

