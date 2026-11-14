package net.farmingtales.ftboss.skill;

import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.util.DoraSniperDamageRules;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;

public final class DoraSniperDamage {
    private record Hit(ResourceKey<DamageType> type, float amount) {}

    private static final List<Hit> TABLE = DoraSniperDamageRules.TABLE.stream()
            .map(hit -> new Hit(ResourceKey.create(Registries.DAMAGE_TYPE,
                    new ResourceLocation(FTBossMod.MOD_ID, hit.type())), hit.amount()))
            .toList();

    private DoraSniperDamage() {}

    public static boolean isIndependent(DamageSource source) {
        for (Hit hit : TABLE) {
            if (source.is(hit.type())) return true;
        }
        return false;
    }

    public static boolean hurt(LivingEntity target, LivingEntity owner) {
        if (target.level().isClientSide || !target.isAlive()) return false;
        var registry = target.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        boolean damaged = false;
        for (Hit hit : TABLE) {
            DamageSource source = new DamageSource(registry.getHolderOrThrow(hit.type()), owner, owner);
            damaged |= target.hurt(source, hit.amount());
        }
        return damaged;
    }
}
