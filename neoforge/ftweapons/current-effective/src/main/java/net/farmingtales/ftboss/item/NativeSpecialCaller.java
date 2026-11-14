package net.farmingtales.ftboss.item;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import net.farmingtales.ftboss.item.NativeActionObservation.Action;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;

public final class NativeSpecialCaller {
    private static final ThreadLocal<NativeSpecialCaller.Hit> HIT = new ThreadLocal<>();

    private NativeSpecialCaller() {
    }

    public static Object call(Object var0, String var1, Class<?>[] var2, Object... var3) {
        try {
            Class var4 = var0 instanceof Class var9 ? var9 : var0.getClass();
            Method var10 = var4.getMethod(var1, var2);
            return var10.invoke(var0 instanceof Class ? null : var0, var3);
        } catch (InvocationTargetException var7) {
            Throwable var5 = var7.getCause();
            if (var5 instanceof RuntimeException var11) {
                throw var11;
            } else if (var5 instanceof Error var6) {
                throw var6;
            } else {
                throw new IllegalStateException(var5);
            }
        } catch (ReflectiveOperationException var8) {
            throw new IllegalStateException("Exact native caller unavailable: " + var1, var8);
        }
    }

    public static void mite(AbstractArrow var0, String var1, LevelAccessor var2, double var3, double var5, double var7, Entity var9) {
        String var10 = var0.getClass().getName();
        boolean var11 = var10.equals("net.gqstavo.armamentarium.entity.MiteArrowEntity")
                && var1.equals("net.gqstavo.armamentarium.procedures.MiteArrowProjectileHitsLivingEntityProcedure")
            || var10.equals("net.gqstavo.armamentarium.entity.MitePelletEntity")
                && var1.equals("net.gqstavo.armamentarium.procedures.MitePelletProjectileHitsLivingEntityProcedure");
        if (!var11) {
            throw new IllegalArgumentException("Mismatched native Mite caller");
        } else {
            NativeSpecialCaller.Hit var12 = HIT.get();
            HIT.set(new NativeSpecialCaller.Hit(var0, var9));

            try {
                Class var13 = Class.forName(var1, false, var0.getClass().getClassLoader());
                call(var13, "execute", new Class[]{LevelAccessor.class, double.class, double.class, double.class, Entity.class}, var2, var3, var5, var7, var9);
            } catch (ClassNotFoundException var17) {
                throw new IllegalStateException(var17);
            } finally {
                if (var12 == null) {
                    HIT.remove();
                } else {
                    HIT.set(var12);
                }
            }
        }
    }

    public static boolean miteHurt(Entity var0, DamageSource var1, float var2) {
        NativeSpecialCaller.Hit var3 = HIT.get();
        DamageSource var4 = var1;
        if (var3 != null
            && var3.target() == var0
            && !var3.projectile().level().isClientSide
            && var3.projectile().getOwner() instanceof ServerPlayer var5
            && var5.isAlive()
            && !var5.isRemoved()
            && var3.projectile().level() == var5.level()
            && var5.level() == var0.level()
            && var1.getEntity() == null
            && var1.getDirectEntity() == null
            && var2 == 8.0F
            && var1.typeHolder().unwrapKey().map(var0x -> var0x.location().toString().equals("armamentarium:cacophony")).orElse(false)) {
            var4 = new DamageSource(var1.typeHolder(), var3.projectile(), var5);
        }

        return var3 != null && var3.projectile() instanceof NativeIngressCarrier var7
            ? FoodWeaponSourceLedger.nativeCarrierHurt(var7.ftboss$nativeIngressReceipt(), var7.ftboss$nativeCarrierToken(), var0, var4, var2)
            : var0.hurt(var4, var2);
    }

    public static boolean leftClick(Object var0, ItemStack var1, LivingEntity var2) {
        if (var1.getItem() != var0) {
            throw new IllegalArgumentException("Exact native item receiver required");
        } else {
            InteractionHand var3 = null;
            boolean var4 = var2.getMainHandItem() == var1;
            boolean var5 = var2.getOffhandItem() == var1;
            if (var4 != var5) {
                var3 = var4 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            }

            return NativeActionObservation.invoke(
                Action.LEFT_CLICK, var1, var2, var3, () -> (Boolean)call(var0, "onLeftClick", new Class[]{ItemStack.class, LivingEntity.class}, var1, var2)
            );
        }
    }

    public static void rush(Object var0, boolean var1) {
        call(var0, "setRush", new Class[]{boolean.class}, var1);
        if (var1) {
            NativeActionObservation.remember(var0);
        } else {
            NativeActionObservation.forget(var0);
        }
    }

    public static void rushTick(Object var0, LivingEntity var1) {
        boolean var2 = false;

        try {
            NativeActionObservation.delayed(var0, var1, () -> call(var0, "tick", new Class[]{LivingEntity.class}, var1));
            var2 = true;
            if (!(Boolean)call(var0, "isRush", new Class[0])) {
                NativeActionObservation.forget(var0);
                if (var0 instanceof NativeCapabilityCarrier var3) {
                    FoodWeaponSourceLedger.cancelNativeCapability(var3.ftboss$nativeCapabilityToken());
                    var3.ftboss$clearNativeCapability();
                }
            }
        } finally {
            if (!var2 && var0 instanceof NativeCapabilityCarrier var5) {
                FoodWeaponSourceLedger.cancelNativeCapability(var5.ftboss$nativeCapabilityToken());
                var5.ftboss$clearNativeCapability();
            }
        }
    }

    public static void tentacleChild(Entity var0, UUID var1, Entity var2) {
        call(var0, "setCreatorEntityUUID", new Class[]{UUID.class}, var1);
        if (var2 instanceof NativeIngressCarrier var3 && var0 instanceof NativeIngressCarrier var4) {
            FoodWeaponSourceLedger.NativeCarrierToken var5 = FoodWeaponSourceLedger.nativeIngressChild(
                var3.ftboss$nativeIngressReceipt(), var3.ftboss$nativeCarrierToken(), var0, var1
            );
            if (var5 != null) {
                var4.ftboss$bindNativeCarrier(var5);
                var4.ftboss$bindNativeReceipt(var3.ftboss$nativeIngressReceipt());
            }
        }

        NativeActionObservation.inherit(var0, var2, var1);
    }

    public static boolean delayedHurt(Entity var0, Entity var1, DamageSource var2, float var3) {
        if (var0 instanceof NativeIngressCarrier var4 && var4.ftboss$nativeIngressReceipt() != null && var4.ftboss$nativeCarrierToken() != null) {
            return FoodWeaponSourceLedger.nativeCarrierHurt(var4.ftboss$nativeIngressReceipt(), var4.ftboss$nativeCarrierToken(), var1, var2, var3);
        }

        return var2.getEntity() instanceof LivingEntity var6 ? NativeActionObservation.delayed(var0, var6, () -> var1.hurt(var2, var3)) : var1.hurt(var2, var3);
    }

    private static record Hit(AbstractArrow projectile, Entity target) {
    }
}
