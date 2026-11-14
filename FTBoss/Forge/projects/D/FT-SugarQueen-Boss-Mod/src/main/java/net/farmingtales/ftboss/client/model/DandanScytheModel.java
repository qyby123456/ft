package net.farmingtales.ftboss.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.client.render.DandanScytheMotion;
import net.farmingtales.ftboss.client.render.DandanScytheRenderer;
import net.farmingtales.ftboss.util.DandanScytheMotionRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class DandanScytheModel extends BakedModelWrapper<BakedModel> {
    private final ItemOverrides overrides;

    private DandanScytheModel(BakedModel original) {
        super(original);
        overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
                                      @Nullable LivingEntity owner, int seed) {
                BakedModel selected = original.getOverrides().resolve(original, stack, level, owner, seed);
                return new HeldModel(selected == null ? original : selected, stack,
                        DandanScytheMotion.sample(owner, stack, Minecraft.getInstance().getFrameTime()));
            }
        };
    }

    @Override
    public ItemOverrides getOverrides() { return overrides; }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void bake(ModelEvent.ModifyBakingResult event) {
        var id = new ModelResourceLocation(FTBossMod.MOD_ID, "dandan_scythe", "inventory");
        BakedModel original = event.getModels().get(id);
        if (original != null) event.getModels().put(id, new DandanScytheModel(original));
        DandanScytheRenderer.clearPrepared();
        DandanScytheMotion.reset(null);
    }

    private static final class HeldModel extends BakedModelWrapper<BakedModel> {
        private final ItemStack stack;
        private final DandanScytheMotionRules.Pose motion;

        private HeldModel(BakedModel original, ItemStack stack, DandanScytheMotionRules.Pose motion) {
            super(original);
            this.stack = stack;
            this.motion = motion;
        }

        @Override
        public BakedModel applyTransform(ItemDisplayContext context, PoseStack pose, boolean leftHand) {
            BakedModel transformed = originalModel.applyTransform(context, pose, leftHand);
            if (context.firstPerson() || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                    || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
                DandanScytheRenderer.prepare(stack, motion);
                return this;
            }
            DandanScytheRenderer.clearPrepared();
            return transformed;
        }

        @Override
        public boolean isCustomRenderer() { return true; }
    }
}
