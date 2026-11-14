package net.farmingtales.ftboss.mixin;

import net.farmingtales.ftboss.item.OwnerFieldSourceAccess;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({DamageSource.class})
public interface OwnerFieldSourceAccessMixin extends OwnerFieldSourceAccess {
    @Accessor("directEntity")
    @Override
    Entity ftboss$ownerFieldDirectEntity();
}
