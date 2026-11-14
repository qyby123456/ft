/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.magic.SpellSelectionManager
 *  io.redspace.ironsspellbooks.api.magic.SpellSelectionManager$SelectionOption
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.farmingtales.ftboss.mixin;

import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import net.farmingtales.ftboss.integ.NativeIronCastObservation;
import net.farmingtales.ftboss.integ.NativeIronExecution;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets={"io.redspace.ironsspellbooks.item.CastingItem"}, remap=false)
public abstract class NativeIronSelectionMixin {
    @Unique
    private void ftboss$v9$ironUseBegin(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> callbackInfoReturnable) throws Throwable {
        NativeIronExecution.UseFrame useFrame = (NativeIronExecution.UseFrame)callbackInfoReturnable;
        NativeIronExecution.use(useFrame);
        FoodWeaponSourceLedger.NativeIronUseToken nativeIronUseToken = FoodWeaponSourceLedger.nativeIronUseBegin(player.m_21120_(interactionHand), player, interactionHand);
        try {
            useFrame.capture(nativeIronUseToken);
        }
        catch (Throwable throwable) {
            try {
                FoodWeaponSourceLedger.nativeIronUseEnd(nativeIronUseToken);
            }
            catch (Throwable throwable2) {
                // empty catch block
            }
            throw throwable;
        }
    }

    @Unique
    private void ftboss$v9$ironUseReturn(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> callbackInfoReturnable) {
        try {
            NativeIronExecution.endUse((NativeIronExecution.UseFrame)callbackInfoReturnable);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @Unique
    private static void ftboss$v9$ironUseUnwind(NativeIronExecution.UseFrame useFrame) throws Throwable {
        try {
            NativeIronExecution.abortAttempt(useFrame);
        }
        finally {
            NativeIronExecution.endUse(useFrame);
        }
    }

    @Redirect(method={"m_7203_(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;"}, at=@At(value="INVOKE", target="Lio/redspace/ironsspellbooks/api/magic/SpellSelectionManager;getSelection()Lio/redspace/ironsspellbooks/api/magic/SpellSelectionManager$SelectionOption;"), require=1, expect=1, allow=1, remap=false)
    private SpellSelectionManager.SelectionOption nativeSelection(SpellSelectionManager spellSelectionManager, Level level, Player player, InteractionHand interactionHand) {
        SpellSelectionManager.SelectionOption selectionOption = spellSelectionManager.getSelection();
        NativeIronCastObservation.selection((LivingEntity)player, selectionOption);
        return selectionOption;
    }
}

