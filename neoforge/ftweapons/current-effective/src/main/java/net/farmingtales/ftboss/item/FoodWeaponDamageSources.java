package net.farmingtales.ftboss.item;

import java.util.function.Supplier;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;

public final class FoodWeaponDamageSources {
    public static final ResourceKey<DamageType> SECONDARY = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("ftboss", "food_weapon_secondary"));
    public static final ResourceKey<DamageType> PRIMARY = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("ftboss", "food_weapon_skill"));
    private static final ThreadLocal<Integer> SECONDARY_DEPTH = ThreadLocal.withInitial(() -> 0);

    private FoodWeaponDamageSources() {
    }

    public static DamageSource secondary(RegistryAccess var0, LivingEntity var1) {
        return var0.registry(Registries.DAMAGE_TYPE)
            .flatMap(var0x -> var0x.getHolder(SECONDARY))
            .map(var1x -> new DamageSource(var1x, null, var1))
            .orElse(null);
    }

    public static DamageSource attributeSecondary(ServerPlayer var0) {
        return var0 == null ? null : secondary(var0.level().registryAccess(), var0);
    }

    public static DamageSource genericPrimary(ServerPlayer var0) {
        return var0 == null
            ? null
            : var0.level()
                .registryAccess()
                .registry(Registries.DAMAGE_TYPE)
                .flatMap(var0x -> var0x.getHolder(PRIMARY))
                .map(var1 -> new DamageSource(var1, var0, var0))
                .orElse(null);
    }

    public static boolean isAttributeSecondary(DamageSource var0) {
        return SECONDARY_DEPTH.get() > 0 || var0 != null && var0.is(SECONDARY);
    }

    public static <Result> Result withSecondary(Supplier<Result> var0) {
        int var1 = SECONDARY_DEPTH.get();
        SECONDARY_DEPTH.set(var1 + 1);

        Object var2;
        try {
            var2 = var0.get();
        } finally {
            if (var1 == 0) {
                SECONDARY_DEPTH.remove();
            } else {
                SECONDARY_DEPTH.set(var1);
            }
        }

        return (Result)var2;
    }

    public static boolean hurtSecondary(ServerPlayer var0, LivingEntity var1, float var2) {
        if (var0 != null && var1 != null && Float.isFinite(var2) && !(var2 <= 0.0F) && var0.isAlive() && var1.isAlive() && var0.level() == var1.level()) {
            DamageSource var3 = attributeSecondary(var0);
            return var3 != null && withSecondary(() -> var1.hurt(var3, var2));
        } else {
            return false;
        }
    }

    static boolean hurtListedSecondary(ServerPlayer var0, LivingEntity var1, float var2, FoodWeaponFinalDamage.ChildHurtBridge var3) {
        if (var0 != null && var1 != null && Float.isFinite(var2) && !(var2 <= 0.0F) && var0.isAlive() && var1.isAlive() && var0.level() == var1.level()) {
            DamageSource var4 = attributeSecondary(var0);
            return var4 != null && withSecondary(() -> FoodWeaponFinalDamage.hurtListedChild(var3, var1, var4, var2));
        } else {
            return false;
        }
    }
}
