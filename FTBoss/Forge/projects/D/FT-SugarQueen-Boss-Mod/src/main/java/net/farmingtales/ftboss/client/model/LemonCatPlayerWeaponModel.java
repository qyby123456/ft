package net.farmingtales.ftboss.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.weapons.LemonCatWeaponItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
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

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class LemonCatPlayerWeaponModel extends BakedModelWrapper<BakedModel> {
    private final ItemOverrides overrides;

    private LemonCatPlayerWeaponModel(BakedModel original, BakedModel overlay) {
        super(original);
        BakedModel glowing = new ReadyModel(original, overlay);
        this.overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
                                      @Nullable LivingEntity entity, int seed) {
                if (entity instanceof Player player && !player.isSpectator()
                        && (player.getMainHandItem() == stack || player.getOffhandItem() == stack)
                        && !LemonCatWeaponItem.isCoolingDown(player)) return glowing;
                return original;
            }
        };
    }

    private static ResourceLocation overlayId(String identifier) {
        return new ResourceLocation(FTBossMod.MOD_ID, "item/lemoncat/" + identifier + "_glow");
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(overlayId("skillet"));
        event.register(overlayId("kitchen_shovel"));
    }

    @SubscribeEvent
    public static void bake(ModelEvent.ModifyBakingResult event) {
        for (String identifier : List.of("skillet", "spatula")) {
            var itemId = new ModelResourceLocation(FTBossMod.MOD_ID, "lemoncat_" + identifier, "inventory");
            BakedModel original = event.getModels().get(itemId);
            BakedModel overlay = event.getModels().get(overlayId(identifier.equals("skillet") ? "skillet" : "kitchen_shovel"));
            if (original == null || overlay == null) throw new IllegalStateException("Missing Lemon Cat player model: " + identifier);
            event.getModels().put(itemId, new LemonCatPlayerWeaponModel(original, overlay));
        }
    }

    @Override
    public ItemOverrides getOverrides() { return this.overrides; }

    private static final class ReadyModel extends BakedModelWrapper<BakedModel> {
        private final List<BakedQuad> quads;

        private ReadyModel(BakedModel original, BakedModel overlay) {
            super(original);
            List<BakedQuad> result = new ArrayList<>(original.getQuads(null, null, RandomSource.create(0L)));
            for (BakedQuad quad : overlay.getQuads(null, null, RandomSource.create(0L))) {
                int[] vertices = quad.getVertices().clone();
                int stride = vertices.length / 4;
                for (int vertex = 0; vertex < 4; vertex++) vertices[vertex * stride + 6] = LightTexture.FULL_BRIGHT;
                result.add(new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), false));
            }
            this.quads = List.copyOf(result);
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
        public List<BakedModel> getRenderPasses(ItemStack stack, boolean fabulous) { return List.of(this); }

        @Override
        public BakedModel applyTransform(ItemDisplayContext context, PoseStack pose, boolean leftHand) {
            this.originalModel.applyTransform(context, pose, leftHand);
            return context.firstPerson() || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                    || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND ? this : this.originalModel;
        }
    }
}
