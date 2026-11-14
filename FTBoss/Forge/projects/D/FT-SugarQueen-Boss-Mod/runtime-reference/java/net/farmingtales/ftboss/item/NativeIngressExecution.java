/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.item;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeCapabilityCarrier;
import net.farmingtales.ftboss.item.NativeIngressCarrier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class NativeIngressExecution {
    private static final ThreadLocal<Deque<Frame>> FRAMES = ThreadLocal.withInitial(ArrayDeque::new);

    private NativeIngressExecution() {
    }

    public static InteractionHand exactHeldHand(LivingEntity livingEntity, ItemStack itemStack) {
        boolean bl;
        boolean bl2 = livingEntity.m_21205_() == itemStack;
        boolean bl3 = bl = livingEntity.m_21206_() == itemStack;
        return bl2 == bl ? null : (bl2 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
    }

    public static Frame open(FoodWeaponSourceLedger.NativeIngressToken nativeIngressToken) {
        Frame frame = new Frame(nativeIngressToken);
        FRAMES.get().push(frame);
        return frame;
    }

    public static void carrier(Entity entity, FoodWeaponSourceLedger.NativeCarrierToken nativeCarrierToken) {
        Frame frame = FRAMES.get().peek();
        if (frame == null || frame.closed || frame.token == null || nativeCarrierToken == null || !(entity instanceof NativeIngressCarrier)) {
            return;
        }
        NativeIngressCarrier nativeIngressCarrier = (NativeIngressCarrier)entity;
        nativeIngressCarrier.ftboss$bindNativeCarrier(nativeCarrierToken);
        if (!frame.carriers.contains(nativeIngressCarrier)) {
            frame.carriers.add(nativeIngressCarrier);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void finish(Frame frame, FoodWeaponSourceLedger.NativeIngressReceipt nativeIngressReceipt) {
        if (frame == null || frame.closed) {
            return;
        }
        try {
            for (NativeIngressCarrier object : frame.carriers) {
                if (nativeIngressReceipt == null) {
                    object.ftboss$clearNativeCarrier();
                    continue;
                }
                object.ftboss$bindNativeReceipt(nativeIngressReceipt);
            }
            for (NativeCapabilityCarrier nativeCapabilityCarrier : frame.capabilities) {
                if (nativeIngressReceipt == null) {
                    FoodWeaponSourceLedger.cancelNativeCapability(nativeCapabilityCarrier.ftboss$nativeCapabilityToken());
                    nativeCapabilityCarrier.ftboss$clearNativeCapability();
                    continue;
                }
                nativeCapabilityCarrier.ftboss$bindNativeCapabilityReceipt(nativeIngressReceipt);
            }
        }
        finally {
            try {
                if (nativeIngressReceipt == null) {
                    FoodWeaponSourceLedger.cancelNativeIngress(frame.token);
                }
            }
            finally {
                frame.closed = true;
                FRAMES.get().remove(frame);
                if (FRAMES.get().isEmpty()) {
                    FRAMES.remove();
                }
            }
        }
    }

    public static void abort(Frame frame) {
        NativeIngressExecution.finish(frame, null);
    }

    public static void capability(Object object, FoodWeaponSourceLedger.NativeCapabilityToken nativeCapabilityToken) {
        Frame frame = FRAMES.get().peek();
        if (frame == null || frame.closed || frame.token == null || nativeCapabilityToken == null || !(object instanceof NativeCapabilityCarrier)) {
            return;
        }
        NativeCapabilityCarrier nativeCapabilityCarrier = (NativeCapabilityCarrier)object;
        nativeCapabilityCarrier.ftboss$bindNativeCapability(nativeCapabilityToken);
        if (!frame.capabilities.contains(nativeCapabilityCarrier)) {
            frame.capabilities.add(nativeCapabilityCarrier);
        }
    }

    public static final class Frame {
        private final FoodWeaponSourceLedger.NativeIngressToken token;
        private final List<NativeIngressCarrier> carriers = new ArrayList<NativeIngressCarrier>();
        private final List<NativeCapabilityCarrier> capabilities = new ArrayList<NativeCapabilityCarrier>();
        private boolean closed;

        private Frame(FoodWeaponSourceLedger.NativeIngressToken nativeIngressToken) {
            this.token = nativeIngressToken;
        }

        public FoodWeaponSourceLedger.NativeIngressToken token() {
            return this.closed ? null : this.token;
        }
    }

    public record WearFrame(FoodWeaponSourceLedger.NativeWearToken token) {
    }
}

