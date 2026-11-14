/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Registry
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageType
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.skill;

import java.util.List;
import net.farmingtales.ftboss.util.DoraSniperDamageRules;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class DoraSniperDamage {
    private static final List<Hit> TABLE = DoraSniperDamageRules.TABLE.stream().map(hit -> new Hit((ResourceKey<DamageType>)ResourceKey.m_135785_((ResourceKey)Registries.f_268580_, (ResourceLocation)new ResourceLocation("ftboss", hit.type())), hit.amount())).toList();

    private DoraSniperDamage() {
    }

    public static boolean isIndependent(DamageSource source) {
        for (Hit hit : TABLE) {
            if (!source.m_276093_(hit.type())) continue;
            return true;
        }
        return false;
    }

    public static boolean hurt(LivingEntity target, LivingEntity owner) {
        if (target.m_9236_().f_46443_ || !target.m_6084_()) {
            return false;
        }
        Registry registry = target.m_9236_().m_9598_().m_175515_(Registries.f_268580_);
        boolean damaged = false;
        for (Hit hit : TABLE) {
            DamageSource source = new DamageSource((Holder)registry.m_246971_(hit.type()), (Entity)owner, (Entity)owner);
            damaged |= target.m_6469_(source, hit.amount());
        }
        return damaged;
    }

    private record Hit(ResourceKey<DamageType> type, float amount) {
    }
}

