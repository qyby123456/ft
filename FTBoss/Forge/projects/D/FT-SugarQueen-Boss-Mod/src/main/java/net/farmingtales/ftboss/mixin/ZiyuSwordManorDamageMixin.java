package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Pseudo
@Mixin(targets = "com.jinlan.moreornplants.event.ModEvents", remap = false)
public abstract class ZiyuSwordManorDamageMixin {
    private static final ResourceLocation FTBOSS_ZIYU_SWORD =
            new ResourceLocation("more_orn_plants", "ziyu_yuanyang_sword");

    @Redirect(
            method = "onLivingHurt(Lnet/minecraftforge/event/entity/living/LivingHurtEvent;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/living/LivingHurtEvent;setAmount(F)V"),
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lcom/jinlan/moreornplants/config/ModBiomeConfig;ZIYU_YUANYANG_TOOLS_BASE_MULTIPLIER:Lnet/minecraftforge/common/ForgeConfigSpec$ConfigValue;"),
                    to = @At(value = "FIELD", target = "Lcom/jinlan/moreornplants/item/ModItems;ZHUIYUE_SWORD:Lnet/minecraftforge/registries/RegistryObject;")),
            require = 1,
            remap = false)
    private static void ftboss$ziyuSwordManorDamage(LivingHurtEvent event, float boostedDamage) {
        if (event.getEntity() instanceof EasterBossEntity boss && !boss.isMini() && !boss.isGiant()
                && event.getSource().getDirectEntity() instanceof Player player
                && FTBOSS_ZIYU_SWORD.equals(ForgeRegistries.ITEMS.getKey(player.getMainHandItem().getItem()))) {
            return;
        }
        event.setAmount(boostedDamage);
    }
}
