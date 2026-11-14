package net.farmingtales.ftboss.mixin;

import java.util.List;
import net.farmingtales.ftboss.item.NativePotionEffectFields;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = PotionContents.class, remap = false)
public abstract class NativePotionContentsFieldsMixin implements NativePotionEffectFields.Potion {
    @Shadow @Final private List<MobEffectInstance> customEffects;

    @Override
    public final List<MobEffectInstance> ftboss$nativePotionEffects() {
        NativePotionEffectFields.requireObserver(PotionContents.class, "ftboss$nativePotionEffects");
        return this.customEffects;
    }
}
