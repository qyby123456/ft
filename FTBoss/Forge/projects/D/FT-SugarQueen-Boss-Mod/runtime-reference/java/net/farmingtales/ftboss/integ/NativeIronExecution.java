/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.api.spells.AbstractSpell
 *  io.redspace.ironsspellbooks.api.spells.CastSource
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.item.ItemStack
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package net.farmingtales.ftboss.integ;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import net.farmingtales.ftboss.integ.NativeIronTokenCarrier;
import net.farmingtales.ftboss.integ.NativeUseCleanup;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public final class NativeIronExecution {
    private static final ThreadLocal<Deque<CastFrame>> ATTEMPTS = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Deque<UseFrame>> USE = ThreadLocal.withInitial(ArrayDeque::new);

    private NativeIronExecution() {
    }

    public static void use(UseFrame useFrame) {
        if (useFrame == null || useFrame.thread != Thread.currentThread()) {
            return;
        }
        useFrame.cleanup.prepare();
        USE.get().push(useFrame);
    }

    public static void endUse(UseFrame useFrame) throws Throwable {
        if (useFrame == null || useFrame.thread != Thread.currentThread()) {
            return;
        }
        useFrame.cleanup.end();
    }

    private static void clearUse(UseFrame useFrame) {
        if (useFrame == null || useFrame.thread != Thread.currentThread()) {
            return;
        }
        Deque<UseFrame> deque = USE.get();
        Iterator<UseFrame> iterator = deque.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() != useFrame) continue;
            iterator.remove();
        }
        if (deque.isEmpty()) {
            USE.remove();
        }
    }

    public static CastFrame attempt(FoodWeaponSourceLedger.NativeIronCastToken nativeIronCastToken, AbstractSpell abstractSpell, CastSource castSource, String string, int n) {
        UseFrame useFrame = USE.get().peek();
        if (useFrame != null && !useFrame.cleanup.acceptsAttempt()) {
            useFrame = null;
        }
        CastFrame castFrame = new CastFrame(nativeIronCastToken, useFrame, abstractSpell, castSource, string, n);
        if (useFrame != null) {
            useFrame.attempt = castFrame;
        }
        ATTEMPTS.get().push(castFrame);
        return castFrame;
    }

    public static CastFrame attemptFrame() {
        return ATTEMPTS.get().peek();
    }

    public static void bind(MagicData magicData) {
        CastFrame castFrame = NativeIronExecution.attemptFrame();
        if (castFrame != null && !castFrame.closed && magicData instanceof NativeIronTokenCarrier) {
            NativeIronTokenCarrier nativeIronTokenCarrier = (NativeIronTokenCarrier)magicData;
            castFrame.data = magicData;
            nativeIronTokenCarrier.ftboss$bindNativeIronFrame(castFrame);
        }
    }

    public static CastFrame stored(MagicData magicData) {
        CastFrame castFrame;
        if (magicData instanceof NativeIronTokenCarrier) {
            NativeIronTokenCarrier nativeIronTokenCarrier = (NativeIronTokenCarrier)magicData;
            castFrame = nativeIronTokenCarrier.ftboss$nativeIronFrame();
        } else {
            castFrame = null;
        }
        return castFrame;
    }

    public static void returned(CastFrame castFrame) {
        ATTEMPTS.get().remove(castFrame);
        if (ATTEMPTS.get().isEmpty()) {
            ATTEMPTS.remove();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void cancel(CastFrame castFrame) {
        if (castFrame == null || castFrame.closed) {
            return;
        }
        try {
            FoodWeaponSourceLedger.cancelNativeIron(castFrame.cast);
        }
        finally {
            castFrame.closed = true;
            castFrame.payment = null;
            castFrame.receipt = null;
            try {
                NativeIronTokenCarrier nativeIronTokenCarrier;
                MagicData magicData = castFrame.data;
                if (magicData instanceof NativeIronTokenCarrier && (nativeIronTokenCarrier = (NativeIronTokenCarrier)magicData).ftboss$nativeIronFrame() == castFrame) {
                    nativeIronTokenCarrier.ftboss$clearNativeIronFrame();
                }
            }
            finally {
                try {
                    NativeIronExecution.returned(castFrame);
                }
                finally {
                    if (castFrame.use != null && castFrame.use.attempt == castFrame) {
                        castFrame.use.attempt = null;
                    }
                    NativeIronExecution.clearUse(castFrame.use);
                }
            }
        }
    }

    public static void cancelStored(MagicData magicData) {
        NativeIronExecution.cancel(NativeIronExecution.stored(magicData));
    }

    public static void abortAttempt() {
        NativeIronExecution.cancel(NativeIronExecution.attemptFrame());
    }

    public static void abortAttempt(UseFrame useFrame) {
        if (useFrame == null || useFrame.thread != Thread.currentThread()) {
            return;
        }
        CastFrame castFrame = useFrame.attempt;
        if (castFrame != null && castFrame.use == useFrame && ATTEMPTS.get().contains(castFrame)) {
            NativeIronExecution.cancel(castFrame);
        }
    }

    public static final class UseFrame
    extends CallbackInfoReturnable<InteractionResultHolder<ItemStack>> {
        private final Thread thread = Thread.currentThread();
        private CastFrame attempt;
        private final UseCleanup cleanup = new UseCleanup(this);

        public UseFrame() {
            super("ftboss$ironUseFrame", false);
        }

        public void capture(FoodWeaponSourceLedger.NativeIronUseToken nativeIronUseToken) {
            this.cleanup.capture(nativeIronUseToken);
        }

        public NativeUseCleanup.Snapshot cleanupSnapshot() {
            return this.cleanup.snapshot();
        }
    }

    private static final class UseCleanup
    extends NativeUseCleanup<FoodWeaponSourceLedger.NativeIronUseToken> {
        private final UseFrame frame;

        private UseCleanup(UseFrame useFrame) {
            this.frame = useFrame;
        }

        @Override
        protected void closeOwner(FoodWeaponSourceLedger.NativeIronUseToken nativeIronUseToken) {
            FoodWeaponSourceLedger.nativeIronUseEnd(nativeIronUseToken);
        }

        @Override
        protected void removeNative() {
            NativeIronExecution.clearUse(this.frame);
        }
    }

    public static final class CastFrame {
        private final FoodWeaponSourceLedger.NativeIronCastToken cast;
        private final UseFrame use;
        private final AbstractSpell spell;
        private final CastSource source;
        private final String slot;
        private final int selectedLevel;
        private FoodWeaponSourceLedger.NativeIronPaymentToken payment;
        private FoodWeaponSourceLedger.NativeIronReceipt receipt;
        private MagicData data;
        private boolean closed;

        private CastFrame(FoodWeaponSourceLedger.NativeIronCastToken nativeIronCastToken, UseFrame useFrame, AbstractSpell abstractSpell, CastSource castSource, String string, int n) {
            this.use = useFrame;
            this.cast = nativeIronCastToken;
            this.spell = abstractSpell;
            this.source = castSource;
            this.slot = string;
            this.selectedLevel = n;
        }

        public boolean matchesRecast(String string, CastSource castSource, int n) {
            return !this.closed && this.spell != null && this.spell.getSpellId().equals(string) && this.source == castSource && this.selectedLevel == n;
        }

        public boolean matchesSpell(String string) {
            return !this.closed && this.spell != null && this.spell.getSpellId().equals(string);
        }

        public FoodWeaponSourceLedger.NativeIronCastToken cast() {
            return this.closed ? null : this.cast;
        }

        public FoodWeaponSourceLedger.NativeIronPaymentToken payment() {
            return this.closed ? null : this.payment;
        }

        public FoodWeaponSourceLedger.NativeIronReceipt receipt() {
            return this.closed ? null : this.receipt;
        }

        public void payment(FoodWeaponSourceLedger.NativeIronPaymentToken nativeIronPaymentToken) {
            if (!this.closed) {
                this.payment = nativeIronPaymentToken;
            }
        }

        public void receipt(FoodWeaponSourceLedger.NativeIronReceipt nativeIronReceipt) {
            if (!this.closed) {
                this.receipt = nativeIronReceipt;
            }
        }
    }

    public record CallbackFrame(CastFrame frame) {
    }
}

