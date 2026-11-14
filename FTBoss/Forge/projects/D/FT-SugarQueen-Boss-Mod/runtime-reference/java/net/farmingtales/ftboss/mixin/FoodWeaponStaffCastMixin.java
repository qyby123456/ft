/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.events.SpellOnCastEvent
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.api.spells.AbstractSpell
 *  io.redspace.ironsspellbooks.api.spells.CastSource
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.eventbus.api.Event
 *  net.minecraftforge.eventbus.api.IEventBus
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.farmingtales.ftboss.mixin;

import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import java.util.ArrayDeque;
import java.util.Deque;
import net.farmingtales.ftboss.integ.FoodWeaponSpellSession;
import net.farmingtales.ftboss.integ.FoodWeaponStaffCasting;
import net.farmingtales.ftboss.integ.NativeIronCastObservation;
import net.farmingtales.ftboss.integ.NativeIronExecution;
import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets={"io.redspace.ironsspellbooks.api.spells.AbstractSpell"}, remap=false)
public abstract class FoodWeaponStaffCastMixin {
    @Unique
    private static final ThreadLocal<Deque<NativeIronExecution.CallbackFrame>> ftboss$v9$castFrames = ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method={"attemptInitiateCast(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lio/redspace/ironsspellbooks/api/spells/CastSource;ZLjava/lang/String;)Z"}, at={@At(value="HEAD")}, require=1, remap=false)
    private void ftboss$v9$ironAttemptBegin(ItemStack itemStack, int n, Level level, Player player, CastSource castSource, boolean bl, String string, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        NativeIronExecution.attempt(FoodWeaponSourceLedger.nativeIronAttemptBegin((AbstractSpell)this, itemStack, n, player, castSource, string), (AbstractSpell)this, castSource, string, n);
    }

    @Inject(method={"castSpell(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V"}, at={@At(value="HEAD")}, require=1, remap=false)
    private void ftboss$v9$ironCastBegin(Level level, int n, ServerPlayer serverPlayer, CastSource castSource, boolean bl, CallbackInfo callbackInfo) {
        ftboss$v9$castFrames.get().push(new NativeIronExecution.CallbackFrame(NativeIronExecution.stored(MagicData.getPlayerMagicData((LivingEntity)serverPlayer))));
    }

    @Inject(method={"castSpell(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V"}, at={@At(value="RETURN")}, require=1, remap=false)
    private void ftboss$v9$ironCastReturn(Level level, int n, ServerPlayer serverPlayer, CastSource castSource, boolean bl, CallbackInfo callbackInfo) {
        NativeIronExecution.CallbackFrame callbackFrame = ftboss$v9$castFrames.get().peek();
        if (callbackFrame != null && callbackFrame.frame() != null) {
            callbackFrame.frame().receipt(FoodWeaponSourceLedger.nativeIronCastReturned(callbackFrame.frame().payment()));
        }
        if (callbackFrame != null) {
            ftboss$v9$castFrames.get().remove(callbackFrame);
        }
        if (ftboss$v9$castFrames.get().isEmpty()) {
            ftboss$v9$castFrames.remove();
        }
    }

    @Unique
    private static void ftboss$v9$ironAttemptUnwind() {
        NativeIronExecution.abortAttempt();
    }

    @Unique
    private static void ftboss$v9$ironCastUnwind() {
        NativeIronExecution.CallbackFrame callbackFrame = ftboss$v9$castFrames.get().poll();
        try {
            if (callbackFrame != null) {
                NativeIronExecution.cancel(callbackFrame.frame());
            }
        }
        finally {
            if (ftboss$v9$castFrames.get().isEmpty()) {
                ftboss$v9$castFrames.remove();
            }
        }
    }

    @Redirect(method={"attemptInitiateCast(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lio/redspace/ironsspellbooks/api/spells/CastSource;ZLjava/lang/String;)Z"}, at=@At(value="INVOKE", target="Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;getEffectiveCastTime(ILnet/minecraft/world/entity/LivingEntity;)I"), require=1, expect=1, allow=1, remap=false)
    private int nativeEffectiveTime(AbstractSpell abstractSpell, int n, LivingEntity livingEntity) {
        return NativeIronCastObservation.effectiveTime(abstractSpell, n, livingEntity);
    }

    @Inject(method={"attemptInitiateCast(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lio/redspace/ironsspellbooks/api/spells/CastSource;ZLjava/lang/String;)Z"}, at={@At(value="RETURN")}, require=1, remap=false)
    private void ftboss$v9$ironAttemptReturn(ItemStack itemStack, int n, Level level, Player player, CastSource castSource, boolean bl, String string, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        NativeIronCastObservation.attemptReturned((AbstractSpell)this, itemStack, n, (LivingEntity)player, castSource, string, (Boolean)callbackInfoReturnable.getReturnValue());
        NativeIronExecution.CastFrame castFrame = NativeIronExecution.attemptFrame();
        if (castFrame == null) {
            return;
        }
        FoodWeaponSourceLedger.nativeIronAttemptReturned(castFrame.cast(), callbackInfoReturnable);
        if (!Boolean.TRUE.equals(callbackInfoReturnable.getReturnValue())) {
            NativeIronExecution.cancel(castFrame);
        } else {
            NativeIronExecution.returned(castFrame);
        }
    }

    @Redirect(method={"attemptInitiateCast(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lio/redspace/ironsspellbooks/api/spells/CastSource;ZLjava/lang/String;)Z"}, at=@At(value="INVOKE", target="Lio/redspace/ironsspellbooks/api/magic/MagicData;setPlayerCastingItem(Lnet/minecraft/world/item/ItemStack;)V"), remap=false)
    private void ftboss$bindSession(MagicData magicData, ItemStack itemStack, ItemStack itemStack2, int n, Level level, Player player, CastSource castSource, boolean bl, String string) {
        magicData.setPlayerCastingItem(itemStack);
        NativeIronExecution.bind(magicData);
        NativeIronCastObservation.bind(magicData, (AbstractSpell)this, (LivingEntity)player, itemStack, n, castSource, string);
        FoodWeaponSpellSession.bind(magicData, (LivingEntity)player, itemStack, castSource);
    }

    @Redirect(method={"attemptInitiateCast(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lio/redspace/ironsspellbooks/api/spells/CastSource;ZLjava/lang/String;)Z"}, at=@At(value="INVOKE", target="Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;onServerPreCast(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/magic/MagicData;)V"), remap=false)
    private void ftboss$preCast(AbstractSpell abstractSpell, Level level, int n, LivingEntity livingEntity, MagicData magicData) {
        FoodWeaponSpellSession.callback(magicData, livingEntity, false, () -> abstractSpell.onServerPreCast(level, n, livingEntity, magicData));
    }

    @Inject(method={"attemptInitiateCast(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lio/redspace/ironsspellbooks/api/spells/CastSource;ZLjava/lang/String;)Z"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private void ftboss$staffBegin(ItemStack itemStack, int n, Level level, Player player, CastSource castSource, boolean bl, String string, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        ServerPlayer serverPlayer;
        if (castSource == CastSource.SWORD && player instanceof ServerPlayer && !FoodWeaponRanged.canCast(serverPlayer = (ServerPlayer)player, itemStack)) {
            callbackInfoReturnable.setReturnValue((Object)false);
        }
    }

    @Inject(method={"castSpell(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V"}, at={@At(value="HEAD")}, cancellable=true, remap=false)
    private void ftboss$staffFinish(Level level, int n, ServerPlayer serverPlayer, CastSource castSource, boolean bl, CallbackInfo callbackInfo) {
        if (castSource == CastSource.SPELLBOOK || castSource == CastSource.SCROLL) {
            return;
        }
        if (!FoodWeaponStaffCasting.canFinish(serverPlayer, castSource)) {
            callbackInfo.cancel();
        }
    }

    @Redirect(method={"castSpell(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V"}, at=@At(value="INVOKE", target="Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z"), remap=false)
    private boolean ftboss$v9$ironPost(IEventBus iEventBus, Event event, Level level, int n, ServerPlayer serverPlayer, CastSource castSource, boolean bl) {
        boolean bl2 = NativeIronCastObservation.postCost(iEventBus, event, (LivingEntity)serverPlayer);
        NativeIronExecution.CallbackFrame callbackFrame = ftboss$v9$castFrames.get().peek();
        if (callbackFrame != null && callbackFrame.frame() != null) {
            if (bl2 || !(event instanceof SpellOnCastEvent)) {
                NativeIronExecution.cancel(callbackFrame.frame());
            } else {
                SpellOnCastEvent spellOnCastEvent = (SpellOnCastEvent)event;
                MagicData magicData = MagicData.getPlayerMagicData((LivingEntity)serverPlayer);
                callbackFrame.frame().payment(FoodWeaponSourceLedger.nativeIronPostEvent(callbackFrame.frame().cast(), (AbstractSpell)this, castSource, magicData, spellOnCastEvent, spellOnCastEvent.getSpellLevel(), spellOnCastEvent.getManaCost(), magicData.getMana()));
            }
        }
        return bl2;
    }

    @Redirect(method={"castSpell(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V"}, at=@At(value="INVOKE", target="Lio/redspace/ironsspellbooks/api/magic/MagicData;setMana(F)V"), require=1, expect=1, allow=1, remap=false)
    private void ftboss$v9$ironMana(MagicData magicData, float f) {
        magicData.setMana(f);
        NativeIronExecution.CallbackFrame callbackFrame = ftboss$v9$castFrames.get().peek();
        if (callbackFrame != null && callbackFrame.frame() != null) {
            FoodWeaponSourceLedger.nativeIronManaReturned(callbackFrame.frame().payment(), magicData, magicData.getMana());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Redirect(method={"castSpell(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V"}, at=@At(value="INVOKE", target="Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;onCast(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/spells/CastSource;Lio/redspace/ironsspellbooks/api/magic/MagicData;)V"), remap=false)
    private void ftboss$v9$ironCast(AbstractSpell abstractSpell, Level level, int n, LivingEntity livingEntity, CastSource castSource, MagicData magicData) {
        NativeIronExecution.CallbackFrame callbackFrame = ftboss$v9$castFrames.get().peek();
        FoodWeaponSourceLedger.NativeIronPaymentToken nativeIronPaymentToken = callbackFrame == null || callbackFrame.frame() == null ? null : callbackFrame.frame().payment();
        FoodWeaponSourceLedger.NativeIronCallbackToken nativeIronCallbackToken = FoodWeaponSourceLedger.nativeIronCallbackBegin(nativeIronPaymentToken, abstractSpell, livingEntity, magicData, castSource, n);
        try {
            NativeIronCastObservation.cast(abstractSpell, level, n, livingEntity, castSource, magicData);
            FoodWeaponSourceLedger.nativeIronCastCallbackReturned(nativeIronPaymentToken, abstractSpell, magicData, castSource, n);
            FoodWeaponSourceLedger.nativeIronCallbackReturned(nativeIronCallbackToken);
        }
        finally {
            FoodWeaponSourceLedger.nativeIronCallbackClose(nativeIronCallbackToken);
        }
    }
}

