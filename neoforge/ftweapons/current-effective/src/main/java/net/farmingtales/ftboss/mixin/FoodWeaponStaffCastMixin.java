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
import net.farmingtales.ftboss.integ.NativeIronExecution.CallbackFrame;
import net.farmingtales.ftboss.integ.NativeIronExecution.CastFrame;
import net.farmingtales.ftboss.item.FoodWeaponRanged;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger.NativeIronCallbackToken;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger.NativeIronPaymentToken;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(
    targets = {"io.redspace.ironsspellbooks.api.spells.AbstractSpell"},
    remap = false
)
public abstract class FoodWeaponStaffCastMixin {
    @Unique
    private static final ThreadLocal<Deque<CallbackFrame>> ftboss$v9$castFrames = ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(
        method = {"attemptInitiateCast(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lio/redspace/ironsspellbooks/api/spells/CastSource;ZLjava/lang/String;)Z"},
        at = {@At("HEAD")},
        require = 1,
        remap = false
    )
    private void ftboss$v9$ironAttemptBegin(
        ItemStack var1, int var2, Level var3, Player var4, CastSource var5, boolean var6, String var7, CallbackInfoReturnable<Boolean> var8
    ) {
        NativeIronExecution.attempt(
            FoodWeaponSourceLedger.nativeIronAttemptBegin((AbstractSpell)(Object)this, var1, var2, var4, var5, var7), (AbstractSpell)(Object)this, var5, var7, var2
        );
    }

    @Inject(
        method = {"castSpell(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V"},
        at = {@At("HEAD")},
        require = 1,
        remap = false
    )
    private void ftboss$v9$ironCastBegin(Level var1, int var2, ServerPlayer var3, CastSource var4, boolean var5, CallbackInfo var6) {
        ftboss$v9$castFrames.get().push(new CallbackFrame(NativeIronExecution.stored(MagicData.getPlayerMagicData(var3))));
    }

    @Inject(
        method = {"castSpell(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V"},
        at = {@At("RETURN")},
        require = 1,
        remap = false
    )
    private void ftboss$v9$ironCastReturn(Level var1, int var2, ServerPlayer var3, CastSource var4, boolean var5, CallbackInfo var6) {
        CallbackFrame var7 = ftboss$v9$castFrames.get().peek();
        if (var7 != null && var7.frame() != null) {
            var7.frame().receipt(FoodWeaponSourceLedger.nativeIronCastReturned(var7.frame().payment()));
        }

        if (var7 != null) {
            ftboss$v9$castFrames.get().remove(var7);
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
        CallbackFrame var0 = ftboss$v9$castFrames.get().poll();

        try {
            if (var0 != null) {
                NativeIronExecution.cancel(var0.frame());
            }
        } finally {
            if (ftboss$v9$castFrames.get().isEmpty()) {
                ftboss$v9$castFrames.remove();
            }
        }
    }

    @Redirect(
        method = {"attemptInitiateCast(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lio/redspace/ironsspellbooks/api/spells/CastSource;ZLjava/lang/String;)Z"},
        at = @At(
            value = "INVOKE",
            target = "Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;getEffectiveCastTime(ILnet/minecraft/world/entity/LivingEntity;)I"
        ),
        require = 1,
        expect = 1,
        allow = 1,
        remap = false
    )
    private int nativeEffectiveTime(AbstractSpell var1, int var2, LivingEntity var3) {
        return NativeIronCastObservation.effectiveTime(var1, var2, var3);
    }

    @Inject(
        method = {"attemptInitiateCast(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lio/redspace/ironsspellbooks/api/spells/CastSource;ZLjava/lang/String;)Z"},
        at = {@At("RETURN")},
        require = 1,
        remap = false
    )
    private void ftboss$v9$ironAttemptReturn(
        ItemStack var1, int var2, Level var3, Player var4, CastSource var5, boolean var6, String var7, CallbackInfoReturnable<Boolean> var8
    ) {
        NativeIronCastObservation.attemptReturned((AbstractSpell)(Object)this, var1, var2, var4, var5, var7, (Boolean)var8.getReturnValue());
        CastFrame var9 = NativeIronExecution.attemptFrame();
        if (var9 != null) {
            FoodWeaponSourceLedger.nativeIronAttemptReturned(var9.cast(), var8);
            if (!Boolean.TRUE.equals(var8.getReturnValue())) {
                NativeIronExecution.cancel(var9);
            } else {
                NativeIronExecution.returned(var9);
            }
        }
    }

    @Redirect(
        method = {"attemptInitiateCast(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lio/redspace/ironsspellbooks/api/spells/CastSource;ZLjava/lang/String;)Z"},
        at = @At(
            value = "INVOKE",
            target = "Lio/redspace/ironsspellbooks/api/magic/MagicData;setPlayerCastingItem(Lnet/minecraft/world/item/ItemStack;)V"
        ),
        remap = false
    )
    private void ftboss$bindSession(
        MagicData var1, ItemStack var2, ItemStack var3, int var4, Level var5, Player var6, CastSource var7, boolean var8, String var9
    ) {
        var1.setPlayerCastingItem(var2);
        NativeIronExecution.bind(var1);
        NativeIronCastObservation.bind(var1, (AbstractSpell)(Object)this, var6, var2, var4, var7, var9);
        FoodWeaponSpellSession.bind(var1, var6, var2, var7);
    }

    @Redirect(
        method = {"attemptInitiateCast(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lio/redspace/ironsspellbooks/api/spells/CastSource;ZLjava/lang/String;)Z"},
        at = @At(
            value = "INVOKE",
            target = "Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;onServerPreCast(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/magic/MagicData;)V"
        ),
        remap = false
    )
    private void ftboss$preCast(AbstractSpell var1, Level var2, int var3, LivingEntity var4, MagicData var5) {
        FoodWeaponSpellSession.callback(var5, var4, false, () -> var1.onServerPreCast(var2, var3, var4, var5));
    }

    @Inject(
        method = {"attemptInitiateCast(Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lio/redspace/ironsspellbooks/api/spells/CastSource;ZLjava/lang/String;)Z"},
        at = {@At("HEAD")},
        cancellable = true,
        remap = false
    )
    private void ftboss$staffBegin(
        ItemStack var1, int var2, Level var3, Player var4, CastSource var5, boolean var6, String var7, CallbackInfoReturnable<Boolean> var8
    ) {
        if (var5 == CastSource.SWORD && var4 instanceof ServerPlayer var9 && !FoodWeaponRanged.canCast(var9, var1)) {
            var8.setReturnValue(false);
        }
    }

    @Inject(
        method = {"castSpell(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V"},
        at = {@At("HEAD")},
        cancellable = true,
        remap = false
    )
    private void ftboss$staffFinish(Level var1, int var2, ServerPlayer var3, CastSource var4, boolean var5, CallbackInfo var6) {
        if (var4 != CastSource.SPELLBOOK && var4 != CastSource.SCROLL) {
            if (!FoodWeaponStaffCasting.canFinish(var3, var4)) {
                var6.cancel();
            }
        }
    }

    @Redirect(
        method = {"castSpell(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/neoforged/bus/api/IEventBus;post(Lnet/neoforged/bus/api/Event;)Lnet/neoforged/bus/api/Event;"
        ),
        remap = false
    )
    private Event ftboss$v9$ironPost(IEventBus var1, Event var2, Level var3, int var4, ServerPlayer var5, CastSource var6, boolean var7) {
        boolean var8 = NativeIronCastObservation.postCost(var1, var2, var5);
        CallbackFrame var9 = ftboss$v9$castFrames.get().peek();
        if (var9 != null && var9.frame() != null) {
            if (!var8 && var2 instanceof SpellOnCastEvent var10) {
                MagicData var11 = MagicData.getPlayerMagicData(var5);
                var9.frame()
                    .payment(
                        FoodWeaponSourceLedger.nativeIronPostEvent(
                            var9.frame().cast(), (AbstractSpell)(Object)this, var6, var11, var10, var10.getSpellLevel(), var10.getManaCost(), var11.getMana()
                        )
                    );
            } else {
                NativeIronExecution.cancel(var9.frame());
            }
        }

        return var2;
    }

    @Redirect(
        method = {"castSpell(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V"},
        at = @At(
            value = "INVOKE",
            target = "Lio/redspace/ironsspellbooks/api/magic/MagicData;setMana(F)V"
        ),
        require = 1,
        expect = 1,
        allow = 1,
        remap = false
    )
    private void ftboss$v9$ironMana(MagicData var1, float var2) {
        var1.setMana(var2);
        CallbackFrame var3 = ftboss$v9$castFrames.get().peek();
        if (var3 != null && var3.frame() != null) {
            FoodWeaponSourceLedger.nativeIronManaReturned(var3.frame().payment(), var1, var1.getMana());
        }
    }

    @Redirect(
        method = {"castSpell(Lnet/minecraft/world/level/Level;ILnet/minecraft/server/level/ServerPlayer;Lio/redspace/ironsspellbooks/api/spells/CastSource;Z)V"},
        at = @At(
            value = "INVOKE",
            target = "Lio/redspace/ironsspellbooks/api/spells/AbstractSpell;onCast(Lnet/minecraft/world/level/Level;ILnet/minecraft/world/entity/LivingEntity;Lio/redspace/ironsspellbooks/api/spells/CastSource;Lio/redspace/ironsspellbooks/api/magic/MagicData;)V"
        ),
        remap = false
    )
    private void ftboss$v9$ironCast(AbstractSpell var1, Level var2, int var3, LivingEntity var4, CastSource var5, MagicData var6) {
        CallbackFrame var7 = ftboss$v9$castFrames.get().peek();
        NativeIronPaymentToken var8 = var7 != null && var7.frame() != null ? var7.frame().payment() : null;
        NativeIronCallbackToken var9 = FoodWeaponSourceLedger.nativeIronCallbackBegin(var8, var1, var4, var6, var5, var3);

        try {
            NativeIronCastObservation.cast(var1, var2, var3, var4, var5, var6);
            FoodWeaponSourceLedger.nativeIronCastCallbackReturned(var8, var1, var6, var5, var3);
            FoodWeaponSourceLedger.nativeIronCallbackReturned(var9);
        } finally {
            FoodWeaponSourceLedger.nativeIronCallbackClose(var9);
        }
    }
}
