package net.farmingtales.ftboss.integ;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import net.farmingtales.ftboss.integ.NativeUseCleanup.Snapshot;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public final class NativeIronExecution {
    private static final ThreadLocal<Deque<NativeIronExecution.CastFrame>> ATTEMPTS = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Deque<NativeIronExecution.UseFrame>> USE = ThreadLocal.withInitial(ArrayDeque::new);

    private NativeIronExecution() {
    }

    public static void use(NativeIronExecution.UseFrame var0) {
        if (var0 != null && var0.thread == Thread.currentThread()) {
            var0.cleanup.prepare();
            USE.get().push(var0);
        }
    }

    public static void endUse(NativeIronExecution.UseFrame var0) throws Throwable {
        if (var0 != null && var0.thread == Thread.currentThread()) {
            var0.cleanup.end();
        }
    }

    private static void clearUse(NativeIronExecution.UseFrame var0) {
        if (var0 != null && var0.thread == Thread.currentThread()) {
            Deque var1 = USE.get();
            Iterator var2 = var1.iterator();

            while (var2.hasNext()) {
                if (var2.next() == var0) {
                    var2.remove();
                }
            }

            if (var1.isEmpty()) {
                USE.remove();
            }
        }
    }

    public static NativeIronExecution.CastFrame attempt(
        FoodWeaponSourceLedger.NativeIronCastToken var0, AbstractSpell var1, CastSource var2, String var3, int var4
    ) {
        NativeIronExecution.UseFrame var5 = USE.get().peek();
        if (var5 != null && !var5.cleanup.acceptsAttempt()) {
            var5 = null;
        }

        NativeIronExecution.CastFrame var6 = new NativeIronExecution.CastFrame(var0, var5, var1, var2, var3, var4);
        if (var5 != null) {
            var5.attempt = var6;
        }

        ATTEMPTS.get().push(var6);
        return var6;
    }

    public static NativeIronExecution.CastFrame attemptFrame() {
        return ATTEMPTS.get().peek();
    }

    public static void bind(MagicData var0) {
        NativeIronExecution.CastFrame var1 = attemptFrame();
        if (var1 != null && !var1.closed && var0 instanceof NativeIronTokenCarrier var2) {
            var1.data = var0;
            var2.ftboss$bindNativeIronFrame(var1);
        }
    }

    public static NativeIronExecution.CastFrame stored(MagicData var0) {
        return var0 instanceof NativeIronTokenCarrier var1 ? var1.ftboss$nativeIronFrame() : null;
    }

    public static void returned(NativeIronExecution.CastFrame var0) {
        ATTEMPTS.get().remove(var0);
        if (ATTEMPTS.get().isEmpty()) {
            ATTEMPTS.remove();
        }
    }

    public static void cancel(NativeIronExecution.CastFrame var0) {
        if (var0 != null && !var0.closed) {
            try {
                FoodWeaponSourceLedger.cancelNativeIron(var0.cast);
            } finally {
                var0.closed = true;
                var0.payment = null;
                var0.receipt = null;

                try {
                    if (var0.data instanceof NativeIronTokenCarrier var7 && var7.ftboss$nativeIronFrame() == var0) {
                        var7.ftboss$clearNativeIronFrame();
                    }
                } finally {
                    try {
                        returned(var0);
                    } finally {
                        if (var0.use != null && var0.use.attempt == var0) {
                            var0.use.attempt = null;
                        }

                        clearUse(var0.use);
                    }
                }
            }
        }
    }

    public static void cancelStored(MagicData var0) {
        cancel(stored(var0));
    }

    public static void abortAttempt() {
        cancel(attemptFrame());
    }

    public static void abortAttempt(NativeIronExecution.UseFrame var0) {
        if (var0 != null && var0.thread == Thread.currentThread()) {
            NativeIronExecution.CastFrame var1 = var0.attempt;
            if (var1 != null && var1.use == var0 && ATTEMPTS.get().contains(var1)) {
                cancel(var1);
            }
        }
    }

    public static record CallbackFrame(NativeIronExecution.CastFrame frame) {
    }

    public static final class CastFrame {
        private final FoodWeaponSourceLedger.NativeIronCastToken cast;
        private final NativeIronExecution.UseFrame use;
        private final AbstractSpell spell;
        private final CastSource source;
        private final String slot;
        private final int selectedLevel;
        private FoodWeaponSourceLedger.NativeIronPaymentToken payment;
        private FoodWeaponSourceLedger.NativeIronReceipt receipt;
        private MagicData data;
        private boolean closed;

        private CastFrame(
            FoodWeaponSourceLedger.NativeIronCastToken var1, NativeIronExecution.UseFrame var2, AbstractSpell var3, CastSource var4, String var5, int var6
        ) {
            this.use = var2;
            this.cast = var1;
            this.spell = var3;
            this.source = var4;
            this.slot = var5;
            this.selectedLevel = var6;
        }

        public boolean matchesRecast(String var1, CastSource var2, int var3) {
            return !this.closed && this.spell != null && this.spell.getSpellId().equals(var1) && this.source == var2 && this.selectedLevel == var3;
        }

        public boolean matchesSpell(String var1) {
            return !this.closed && this.spell != null && this.spell.getSpellId().equals(var1);
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

        public void payment(FoodWeaponSourceLedger.NativeIronPaymentToken var1) {
            if (!this.closed) {
                this.payment = var1;
            }
        }

        public void receipt(FoodWeaponSourceLedger.NativeIronReceipt var1) {
            if (!this.closed) {
                this.receipt = var1;
            }
        }
    }

    private static final class UseCleanup extends NativeUseCleanup<FoodWeaponSourceLedger.NativeIronUseToken> {
        private final NativeIronExecution.UseFrame frame;

        private UseCleanup(NativeIronExecution.UseFrame var1) {
            this.frame = var1;
        }

        protected void closeOwner(FoodWeaponSourceLedger.NativeIronUseToken var1) {
            FoodWeaponSourceLedger.nativeIronUseEnd(var1);
        }

        @Override
        protected void removeNative() {
            NativeIronExecution.clearUse(this.frame);
        }
    }

    public static final class UseFrame extends CallbackInfoReturnable<InteractionResultHolder<ItemStack>> {
        private final Thread thread = Thread.currentThread();
        private NativeIronExecution.CastFrame attempt;
        private final NativeIronExecution.UseCleanup cleanup = new NativeIronExecution.UseCleanup(this);

        public UseFrame() {
            super("ftboss$ironUseFrame", false);
        }

        public void capture(FoodWeaponSourceLedger.NativeIronUseToken var1) {
            this.cleanup.capture(var1);
        }

        public Snapshot cleanupSnapshot() {
            return this.cleanup.snapshot();
        }
    }
}
