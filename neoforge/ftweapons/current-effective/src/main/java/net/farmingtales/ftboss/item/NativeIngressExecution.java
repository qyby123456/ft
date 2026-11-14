package net.farmingtales.ftboss.item;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger.NativeCapabilityToken;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger.NativeCarrierToken;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger.NativeIngressReceipt;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger.NativeIngressToken;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger.NativeWearToken;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class NativeIngressExecution {
    private static final ThreadLocal<Deque<NativeIngressExecution.Frame>> FRAMES = ThreadLocal.withInitial(ArrayDeque::new);

    private NativeIngressExecution() {
    }

    public static InteractionHand exactHeldHand(LivingEntity var0, ItemStack var1) {
        boolean var2 = var0.getMainHandItem() == var1;
        boolean var3 = var0.getOffhandItem() == var1;
        return var2 == var3 ? null : (var2 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
    }

    public static NativeIngressExecution.Frame open(NativeIngressToken var0) {
        NativeIngressExecution.Frame var1 = new NativeIngressExecution.Frame(var0);
        FRAMES.get().push(var1);
        return var1;
    }

    public static void carrier(Entity var0, NativeCarrierToken var1) {
        NativeIngressExecution.Frame var2 = FRAMES.get().peek();
        if (var2 != null && !var2.closed && var2.token != null && var1 != null && var0 instanceof NativeIngressCarrier var3) {
            var3.ftboss$bindNativeCarrier(var1);
            if (!var2.carriers.contains(var3)) {
                var2.carriers.add(var3);
            }
        }
    }

    public static void finish(NativeIngressExecution.Frame var0, NativeIngressReceipt var1) {
        if (var0 != null && !var0.closed) {
            try {
                for (NativeIngressCarrier var3 : var0.carriers) {
                    if (var1 == null) {
                        var3.ftboss$clearNativeCarrier();
                    } else {
                        var3.ftboss$bindNativeReceipt(var1);
                    }
                }

                for (NativeCapabilityCarrier var19 : var0.capabilities) {
                    if (var1 == null) {
                        FoodWeaponSourceLedger.cancelNativeCapability(var19.ftboss$nativeCapabilityToken());
                        var19.ftboss$clearNativeCapability();
                    } else {
                        var19.ftboss$bindNativeCapabilityReceipt(var1);
                    }
                }
            } finally {
                try {
                    if (var1 == null) {
                        FoodWeaponSourceLedger.cancelNativeIngress(var0.token);
                    }
                } finally {
                    var0.closed = true;
                    FRAMES.get().remove(var0);
                    if (FRAMES.get().isEmpty()) {
                        FRAMES.remove();
                    }
                }
            }
        }
    }

    public static void abort(NativeIngressExecution.Frame var0) {
        finish(var0, null);
    }

    public static void capability(Object var0, NativeCapabilityToken var1) {
        NativeIngressExecution.Frame var2 = FRAMES.get().peek();
        if (var2 != null && !var2.closed && var2.token != null && var1 != null && var0 instanceof NativeCapabilityCarrier var3) {
            var3.ftboss$bindNativeCapability(var1);
            if (!var2.capabilities.contains(var3)) {
                var2.capabilities.add(var3);
            }
        }
    }

    public static final class Frame {
        private final NativeIngressToken token;
        private final List<NativeIngressCarrier> carriers = new ArrayList<>();
        private final List<NativeCapabilityCarrier> capabilities = new ArrayList<>();
        private boolean closed;

        private Frame(NativeIngressToken var1) {
            this.token = var1;
        }

        public NativeIngressToken token() {
            return this.closed ? null : this.token;
        }
    }

    public static record WearFrame(NativeWearToken token) {
    }
}
