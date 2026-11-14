/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.capabilities.magic.RecastInstance
 *  io.redspace.ironsspellbooks.capabilities.magic.RecastResult
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.LivingEntity
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Pseudo
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.farmingtales.ftboss.mixin;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import net.farmingtales.ftboss.integ.NativeIronExecution;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets={"io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts"}, remap=false)
public abstract class NativeRecastTokenCleanupMixin {
    @Shadow(remap=false)
    @Final
    private ServerPlayer serverPlayer;

    @Inject(method={"removeRecast(Lio/redspace/ironsspellbooks/capabilities/magic/RecastInstance;Lio/redspace/ironsspellbooks/capabilities/magic/RecastResult;Z)V"}, at={@At(value="RETURN")}, require=1, remap=false)
    private void ftboss$v9$recastRemoving(RecastInstance recastInstance, RecastResult recastResult, boolean bl, CallbackInfo callbackInfo) {
        if (recastInstance == null || this.serverPlayer == null) {
            return;
        }
        NativeIronExecution.CastFrame castFrame = NativeIronExecution.stored(MagicData.getPlayerMagicData((LivingEntity)this.serverPlayer));
        if (castFrame != null && castFrame.matchesRecast(recastInstance.getSpellId(), recastInstance.getCastSource(), recastInstance.getSpellLevel())) {
            NativeIronExecution.cancel(castFrame);
        }
    }

    @Inject(method={"removeRecast(Ljava/lang/String;)V"}, at={@At(value="RETURN")}, require=1, remap=false)
    private void ftboss$v9$recastRemovingId(String string, CallbackInfo callbackInfo) {
        if (this.serverPlayer == null) {
            return;
        }
        NativeIronExecution.CastFrame castFrame = NativeIronExecution.stored(MagicData.getPlayerMagicData((LivingEntity)this.serverPlayer));
        if (castFrame != null && castFrame.matchesSpell(string)) {
            NativeIronExecution.cancel(castFrame);
        }
    }

    @Unique
    private void ftboss$v9$recastUnwind() {
        if (this.serverPlayer != null) {
            NativeIronExecution.cancelStored(MagicData.getPlayerMagicData((LivingEntity)this.serverPlayer));
        }
    }
}

