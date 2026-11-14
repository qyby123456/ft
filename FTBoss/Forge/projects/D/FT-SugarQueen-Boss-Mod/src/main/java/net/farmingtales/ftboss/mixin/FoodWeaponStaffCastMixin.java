package net.farmingtales.ftboss.mixin;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.farmingtales.ftboss.integ.FoodWeaponStaffCasting;
import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 铁魔法存在时才应用的可选法杖Hook；2026-10-13 使用真实物品参数，完整保留onCast的等级/来源与法术主体。 */
@Pseudo
@Mixin(targets = "io.redspace.ironsspellbooks.api.spells.AbstractSpell", remap = false)
public abstract class FoodWeaponStaffCastMixin {
    @Inject(method = "attemptInitiateCast(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lio/redspace/ironsspellbooks/api/spells/CastSource;ZLjava/lang/String;)Z",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void ftboss$staffBegin(ItemStack weapon, int spellLevel, Level level, Player caster, CastSource source,
                                    boolean trigger, String slot, CallbackInfoReturnable<Boolean> callback) {
        if ((source == CastSource.SPELLBOOK || source == CastSource.SWORD)
                && caster instanceof ServerPlayer player && !FoodWeaponRanged.canCast(player, weapon)) callback.setReturnValue(false);
    }

    @Inject(method = "castSpell(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void ftboss$staffFinish(Level level, int spellLevel, ServerPlayer player, CastSource source,
                                     boolean trigger, CallbackInfo callback) {
        if (!FoodWeaponStaffCasting.canFinish(player, source)) callback.cancel();
    }

    @Redirect(method = "castSpell(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z"),
            remap = false)
    private boolean ftboss$staffCost(IEventBus bus, Event event, Level level, int spellLevel,
                                     ServerPlayer player, CastSource source, boolean trigger) {
        FoodWeaponStaffCasting.prepareCost(event, player, source);
        return bus.post(event);
    }

    @Redirect(method = "castSpell(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V",
            at = @At(value = "INVOKE", target = "Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;onCast(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/spells/CastSource;Lio/redspace/ironsspellbooks/api/magic/MagicData;)V"),
            remap = false)
    private void ftboss$staffCast(AbstractSpell spell, Level level, int spellLevel, LivingEntity caster, CastSource source, MagicData data) {
        FoodWeaponStaffCasting.cast(spell, level, spellLevel, caster, source, data);
    }
}
