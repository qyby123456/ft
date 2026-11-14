package net.farmingtales.ftboss.mixin;

import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager.SelectionOption;
import net.farmingtales.ftboss.integ.NativeIronCastObservation;
import net.farmingtales.ftboss.integ.NativeIronExecution;
import net.farmingtales.ftboss.integ.NativeIronExecution.UseFrame;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger.NativeIronUseToken;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "io.redspace.ironsspellbooks.player.ServerPlayerEvents", remap = false)
public abstract class NativeIronSelectionMixin {
    @Unique
    private static void ftboss$v9$ironUseBegin(RightClickItem event, UseFrame frame) throws Throwable {
        NativeIronExecution.use(frame);
        NativeIronUseToken token = FoodWeaponSourceLedger.nativeIronUseBegin(event.getEntity().getItemInHand(event.getHand()), event.getEntity(), event.getHand());
        try {
            frame.capture(token);
        } catch (Throwable failure) {
            try {
                FoodWeaponSourceLedger.nativeIronUseEnd(token);
            } catch (Throwable cleanupFailure) {
            }
            throw failure;
        }
    }

    @Unique
    private static void ftboss$v9$ironUseReturn(RightClickItem event, UseFrame frame) throws Throwable {
        NativeIronExecution.endUse(frame);
    }

    @Unique
    private static void ftboss$v9$ironUseUnwind(UseFrame frame) throws Throwable {
        try {
            NativeIronExecution.abortAttempt(frame);
        } finally {
            NativeIronExecution.endUse(frame);
        }
    }

    @Redirect(method = "onUseItem(Lnet/neoforged/neoforge/event/entity/player/PlayerInteractEvent$RightClickItem;)V", at = @At(value = "INVOKE", target = "Lio/redspace/ironsspellbooks/api/magic/SpellSelectionManager;getSelection()Lio/redspace/ironsspellbooks/api/magic/SpellSelectionManager$SelectionOption;"), require = 1, expect = 1, allow = 1, remap = false)
    private static SelectionOption nativeSelection(SpellSelectionManager manager, RightClickItem event) {
        SelectionOption selection = manager.getSelection();
        NativeIronCastObservation.selection(event.getEntity(), selection);
        return selection;
    }
}
