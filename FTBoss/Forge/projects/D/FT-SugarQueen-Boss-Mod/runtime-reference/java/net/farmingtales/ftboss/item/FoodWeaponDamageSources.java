/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageType
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.item;

import java.util.function.Supplier;
import net.farmingtales.ftboss.item.FoodWeaponFinalDamage;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class FoodWeaponDamageSources {
    public static final ResourceKey<DamageType> SECONDARY = ResourceKey.m_135785_((ResourceKey)Registries.f_268580_, (ResourceLocation)new ResourceLocation("ftboss", "food_weapon_secondary"));
    public static final ResourceKey<DamageType> PRIMARY = ResourceKey.m_135785_((ResourceKey)Registries.f_268580_, (ResourceLocation)new ResourceLocation("ftboss", "food_weapon_skill"));
    private static final ThreadLocal<Integer> SECONDARY_DEPTH = ThreadLocal.withInitial(() -> 0);

    private FoodWeaponDamageSources() {
    }

    public static DamageSource secondary(RegistryAccess registryAccess, LivingEntity livingEntity) {
        return registryAccess.m_6632_(Registries.f_268580_).flatMap(registry -> registry.m_203636_(SECONDARY)).map(reference -> new DamageSource((Holder)reference, null, (Entity)livingEntity)).orElse(null);
    }

    public static DamageSource attributeSecondary(ServerPlayer serverPlayer) {
        return serverPlayer == null ? null : FoodWeaponDamageSources.secondary(serverPlayer.m_9236_().m_9598_(), (LivingEntity)serverPlayer);
    }

    public static DamageSource genericPrimary(ServerPlayer serverPlayer) {
        if (serverPlayer == null) {
            return null;
        }
        return serverPlayer.m_9236_().m_9598_().m_6632_(Registries.f_268580_).flatMap(registry -> registry.m_203636_(PRIMARY)).map(reference -> new DamageSource((Holder)reference, (Entity)serverPlayer, (Entity)serverPlayer)).orElse(null);
    }

    public static boolean isAttributeSecondary(DamageSource damageSource) {
        return SECONDARY_DEPTH.get() > 0 || damageSource != null && damageSource.m_276093_(SECONDARY);
    }

    public static <Result> Result withSecondary(Supplier<Result> supplier) {
        int n = SECONDARY_DEPTH.get();
        SECONDARY_DEPTH.set(n + 1);
        try {
            Result Result2 = supplier.get();
            return Result2;
        }
        finally {
            if (n == 0) {
                SECONDARY_DEPTH.remove();
            } else {
                SECONDARY_DEPTH.set(n);
            }
        }
    }

    public static boolean hurtSecondary(ServerPlayer serverPlayer, LivingEntity livingEntity, float f) {
        if (serverPlayer == null || livingEntity == null || !Float.isFinite(f) || f <= 0.0f || !serverPlayer.m_6084_() || !livingEntity.m_6084_() || serverPlayer.m_9236_() != livingEntity.m_9236_()) {
            return false;
        }
        DamageSource damageSource = FoodWeaponDamageSources.attributeSecondary(serverPlayer);
        return damageSource != null && FoodWeaponDamageSources.withSecondary(() -> livingEntity.m_6469_(damageSource, f)) != false;
    }

    static boolean hurtListedSecondary(ServerPlayer serverPlayer, LivingEntity livingEntity, float f, FoodWeaponFinalDamage.ChildHurtBridge childHurtBridge) {
        if (serverPlayer == null || livingEntity == null || !Float.isFinite(f) || f <= 0.0f || !serverPlayer.m_6084_() || !livingEntity.m_6084_() || serverPlayer.m_9236_() != livingEntity.m_9236_()) {
            return false;
        }
        DamageSource damageSource = FoodWeaponDamageSources.attributeSecondary(serverPlayer);
        return damageSource != null && FoodWeaponDamageSources.withSecondary(() -> FoodWeaponFinalDamage.hurtListedChild(childHurtBridge, livingEntity, damageSource, f)) != false;
    }
}

