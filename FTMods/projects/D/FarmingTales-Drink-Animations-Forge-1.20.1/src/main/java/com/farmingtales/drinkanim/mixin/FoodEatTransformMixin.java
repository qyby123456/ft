package com.farmingtales.drinkanim.mixin;

import com.farmingtales.drinkanim.client.DrinkController;
import com.farmingtales.drinkanim.math.BambooEggAnimations;
import com.farmingtales.drinkanim.math.BottleMotion;
import com.farmingtales.drinkanim.math.DrinkProfile;
import com.farmingtales.drinkanim.math.PlateFoodAnimations;
import com.farmingtales.drinkanim.math.StickyRiceCakeAnimations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The authored held-food paths replace vanilla's whole-item chewing oscillation. */
@Mixin(ItemInHandRenderer.class)
public abstract class FoodEatTransformMixin {
    @Inject(method="applyEatTransform",at=@At("HEAD"),cancellable=true)
    private void farmingtales$keepFoodInTheAuthoredRig(PoseStack pose,float partialTick,
            HumanoidArm arm,ItemStack stack,CallbackInfo ci) {
        if(DrinkController.active()&&DrinkController.action()==BottleMotion.Action.DRINK
            &&owns(DrinkController.profile(stack)))ci.cancel();
    }
    private static boolean owns(DrinkProfile profile) {
        return BambooEggAnimations.owns(profile)||StickyRiceCakeAnimations.owns(profile)||PlateFoodAnimations.owns(profile);
    }
}
