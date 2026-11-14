/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.model.BakedQuad
 *  net.minecraft.client.renderer.block.model.ItemOverrides
 *  net.minecraft.client.resources.model.BakedModel
 *  net.minecraft.client.resources.model.ModelResourceLocation
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
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
import java.util.List;
import net.farmingtales.ftboss.item.weapons.LemonCatWeaponItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
public final class LemonCatPlayerWeaponModel
extends BakedModelWrapper<BakedModel> {
    private final ItemOverrides overrides;

    private LemonCatPlayerWeaponModel(BakedModel original, BakedModel overlay) {
        super(original);
        ReadyModel glowing = new ReadyModel(original, overlay);
        this.overrides = new ItemOverrides((BakedModel)glowing, original){
            final /* synthetic */ BakedModel val$glowing;
            final /* synthetic */ BakedModel val$original;
            {
                this.val$glowing = bakedModel;
                this.val$original = bakedModel2;
            }

            public BakedModel m_173464_(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
                Player player;
                if (!(!(entity instanceof Player) || (player = (Player)entity).m_5833_() || player.m_21205_() != stack && player.m_21206_() != stack || LemonCatWeaponItem.isCoolingDown(player))) {
                    return this.val$glowing;
                }
                return this.val$original;
            }
        };
    }

    private static ResourceLocation overlayId(String identifier) {
        return new ResourceLocation("ftboss", "item/lemoncat/" + identifier + "_glow");
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(LemonCatPlayerWeaponModel.overlayId("skillet"));
        event.register(LemonCatPlayerWeaponModel.overlayId("kitchen_shovel"));
    }

    @SubscribeEvent
    public static void bake(ModelEvent.ModifyBakingResult event) {
        for (String identifier : List.of("skillet", "spatula")) {
            ModelResourceLocation itemId = new ModelResourceLocation("ftboss", "lemoncat_" + identifier, "inventory");
            BakedModel original = (BakedModel)event.getModels().get(itemId);
            BakedModel overlay = (BakedModel)event.getModels().get(LemonCatPlayerWeaponModel.overlayId(identifier.equals("skillet") ? "skillet" : "kitchen_shovel"));
            if (original == null || overlay == null) {
                throw new IllegalStateException("Missing Lemon Cat player model: " + identifier);
            }
            event.getModels().put(itemId, new LemonCatPlayerWeaponModel(original, overlay));
        }
    }

    public ItemOverrides m_7343_() {
        return this.overrides;
    }

    private static final class ReadyModel
    extends BakedModelWrapper<BakedModel> {
        private final List<BakedQuad> quads;

        private ReadyModel(BakedModel original, BakedModel overlay) {
            super(original);
            ArrayList<BakedQuad> result = new ArrayList<BakedQuad>(original.m_213637_(null, null, RandomSource.m_216335_((long)0L)));
            for (BakedQuad quad : overlay.m_213637_(null, null, RandomSource.m_216335_((long)0L))) {
                int[] vertices = (int[])quad.m_111303_().clone();
                int stride = vertices.length / 4;
                for (int vertex = 0; vertex < 4; ++vertex) {
                    vertices[vertex * stride + 6] = 0xF000F0;
                }
                result.add(new BakedQuad(vertices, quad.m_111305_(), quad.m_111306_(), quad.m_173410_(), false));
            }
            this.quads = List.copyOf(result);
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

        public BakedModel applyTransform(ItemDisplayContext context, PoseStack pose, boolean leftHand) {
            this.originalModel.applyTransform(context, pose, leftHand);
            return context.m_269069_() || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND ? this : this.originalModel;
        }
    }
}

