/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.LevelAccessor
 */
package net.farmingtales.ftboss.item;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.NativeActionObservation;
import net.farmingtales.ftboss.item.NativeCapabilityCarrier;
import net.farmingtales.ftboss.item.NativeIngressCarrier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;

public final class NativeSpecialCaller {
    private static final ThreadLocal<Hit> HIT = new ThreadLocal();

    private NativeSpecialCaller() {
    }

    public static Object call(Object object, String string, Class<?>[] classArray, Object ... objectArray) {
        try {
            Class clazz;
            GenericDeclaration genericDeclaration;
            if (object instanceof Class) {
                genericDeclaration = (Class)object;
                clazz = genericDeclaration;
            } else {
                clazz = object.getClass();
            }
            Class clazz2 = clazz;
            genericDeclaration = clazz2.getMethod(string, classArray);
            return ((Method)genericDeclaration).invoke(object instanceof Class ? null : object, objectArray);
        }
        catch (InvocationTargetException invocationTargetException) {
            Throwable throwable = invocationTargetException.getCause();
            if (throwable instanceof RuntimeException) {
                RuntimeException runtimeException = (RuntimeException)throwable;
                throw runtimeException;
            }
            if (throwable instanceof Error) {
                Error error = (Error)throwable;
                throw error;
            }
            throw new IllegalStateException(throwable);
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            throw new IllegalStateException("Exact native caller unavailable: " + string, reflectiveOperationException);
        }
    }

    public static void mite(AbstractArrow abstractArrow, String string, LevelAccessor levelAccessor, double d, double d2, double d3, Entity entity) {
        boolean bl;
        String string2 = abstractArrow.getClass().getName();
        boolean bl2 = bl = string2.equals("net.gqstavo.armamentarium.entity.MiteArrowEntity") && string.equals("net.gqstavo.armamentarium.procedures.MiteArrowProjectileHitsLivingEntityProcedure") || string2.equals("net.gqstavo.armamentarium.entity.MitePelletEntity") && string.equals("net.gqstavo.armamentarium.procedures.MitePelletProjectileHitsLivingEntityProcedure");
        if (!bl) {
            throw new IllegalArgumentException("Mismatched native Mite caller");
        }
        Hit hit = HIT.get();
        HIT.set(new Hit(abstractArrow, entity));
        try {
            Class<?> clazz = Class.forName(string, false, abstractArrow.getClass().getClassLoader());
            NativeSpecialCaller.call(clazz, "execute", new Class[]{LevelAccessor.class, Double.TYPE, Double.TYPE, Double.TYPE, Entity.class}, levelAccessor, d, d2, d3, entity);
        }
        catch (ClassNotFoundException classNotFoundException) {
            throw new IllegalStateException(classNotFoundException);
        }
        finally {
            if (hit == null) {
                HIT.remove();
            } else {
                HIT.set(hit);
            }
        }
    }

    public static boolean miteHurt(Entity entity, DamageSource damageSource, float f) {
        NativeIngressCarrier nativeIngressCarrier;
        AbstractArrow abstractArrow;
        Hit hit = HIT.get();
        DamageSource damageSource2 = damageSource;
        if (hit != null && hit.target() == entity && !hit.projectile().m_9236_().f_46443_ && (abstractArrow = hit.projectile().m_19749_()) instanceof ServerPlayer && (nativeIngressCarrier = (ServerPlayer)abstractArrow).m_6084_() && !nativeIngressCarrier.m_213877_() && hit.projectile().m_9236_() == nativeIngressCarrier.m_9236_() && nativeIngressCarrier.m_9236_() == entity.m_9236_() && damageSource.m_7639_() == null && damageSource.m_7640_() == null && f == 8.0f && damageSource.m_269150_().m_203543_().map(resourceKey -> resourceKey.m_135782_().toString().equals("armamentarium:cacophony")).orElse(false).booleanValue()) {
            damageSource2 = new DamageSource(damageSource.m_269150_(), (Entity)hit.projectile(), (Entity)nativeIngressCarrier);
        }
        if (hit != null && (abstractArrow = hit.projectile()) instanceof NativeIngressCarrier) {
            nativeIngressCarrier = (NativeIngressCarrier)abstractArrow;
            return FoodWeaponSourceLedger.nativeCarrierHurt(nativeIngressCarrier.ftboss$nativeIngressReceipt(), nativeIngressCarrier.ftboss$nativeCarrierToken(), entity, damageSource2, f);
        }
        return entity.m_6469_(damageSource2, f);
    }

    public static boolean leftClick(Object object, ItemStack itemStack, LivingEntity livingEntity) {
        boolean bl;
        if (itemStack.m_41720_() != object) {
            throw new IllegalArgumentException("Exact native item receiver required");
        }
        InteractionHand interactionHand = null;
        boolean bl2 = livingEntity.m_21205_() == itemStack;
        boolean bl3 = bl = livingEntity.m_21206_() == itemStack;
        if (bl2 != bl) {
            interactionHand = bl2 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        }
        return NativeActionObservation.invoke(NativeActionObservation.Action.LEFT_CLICK, itemStack, livingEntity, interactionHand, () -> (Boolean)NativeSpecialCaller.call(object, "onLeftClick", new Class[]{ItemStack.class, LivingEntity.class}, itemStack, livingEntity));
    }

    public static void rush(Object object, boolean bl) {
        NativeSpecialCaller.call(object, "setRush", new Class[]{Boolean.TYPE}, bl);
        if (bl) {
            NativeActionObservation.remember(object);
        } else {
            NativeActionObservation.forget(object);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void rushTick(Object object, LivingEntity livingEntity) {
        NativeCapabilityCarrier nativeCapabilityCarrier;
        boolean bl = false;
        try {
            NativeActionObservation.delayed(object, livingEntity, () -> NativeSpecialCaller.call(object, "tick", new Class[]{LivingEntity.class}, livingEntity));
            bl = true;
            if (!((Boolean)NativeSpecialCaller.call(object, "isRush", new Class[0], new Object[0])).booleanValue()) {
                NativeActionObservation.forget(object);
                if (object instanceof NativeCapabilityCarrier) {
                    nativeCapabilityCarrier = (NativeCapabilityCarrier)object;
                    FoodWeaponSourceLedger.cancelNativeCapability(nativeCapabilityCarrier.ftboss$nativeCapabilityToken());
                    nativeCapabilityCarrier.ftboss$clearNativeCapability();
                }
            }
        }
        finally {
            if (!bl && object instanceof NativeCapabilityCarrier) {
                nativeCapabilityCarrier = (NativeCapabilityCarrier)object;
                FoodWeaponSourceLedger.cancelNativeCapability(nativeCapabilityCarrier.ftboss$nativeCapabilityToken());
                nativeCapabilityCarrier.ftboss$clearNativeCapability();
            }
        }
    }

    public static void tentacleChild(Entity entity, UUID uUID, Entity entity2) {
        NativeSpecialCaller.call(entity, "setCreatorEntityUUID", new Class[]{UUID.class}, uUID);
        if (entity2 instanceof NativeIngressCarrier) {
            NativeIngressCarrier nativeIngressCarrier = (NativeIngressCarrier)entity2;
            if (entity instanceof NativeIngressCarrier) {
                NativeIngressCarrier nativeIngressCarrier2 = (NativeIngressCarrier)entity;
                FoodWeaponSourceLedger.NativeCarrierToken nativeCarrierToken = FoodWeaponSourceLedger.nativeIngressChild(nativeIngressCarrier.ftboss$nativeIngressReceipt(), nativeIngressCarrier.ftboss$nativeCarrierToken(), entity, uUID);
                if (nativeCarrierToken != null) {
                    nativeIngressCarrier2.ftboss$bindNativeCarrier(nativeCarrierToken);
                    nativeIngressCarrier2.ftboss$bindNativeReceipt(nativeIngressCarrier.ftboss$nativeIngressReceipt());
                }
            }
        }
        NativeActionObservation.inherit(entity, entity2, uUID);
    }

    public static boolean delayedHurt(Entity entity, Entity entity2, DamageSource damageSource, float f) {
        NativeIngressCarrier nativeIngressCarrier;
        if (entity instanceof NativeIngressCarrier && (nativeIngressCarrier = (NativeIngressCarrier)entity).ftboss$nativeIngressReceipt() != null && nativeIngressCarrier.ftboss$nativeCarrierToken() != null) {
            return FoodWeaponSourceLedger.nativeCarrierHurt(nativeIngressCarrier.ftboss$nativeIngressReceipt(), nativeIngressCarrier.ftboss$nativeCarrierToken(), entity2, damageSource, f);
        }
        Entity entity3 = damageSource.m_7639_();
        if (entity3 instanceof LivingEntity) {
            nativeIngressCarrier = (LivingEntity)entity3;
            return NativeActionObservation.delayed(entity, (LivingEntity)nativeIngressCarrier, () -> entity2.m_6469_(damageSource, f));
        }
        return entity2.m_6469_(damageSource, f);
    }

    private record Hit(AbstractArrow projectile, Entity target) {
    }
}

