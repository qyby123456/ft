/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.tags.TagKey
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageType
 *  net.minecraft.world.damagesource.DamageTypes
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;

public final class ManorDamageKind {
    private static final TagKey<DamageType> FORGE_MAGIC = TagKey.m_203882_((ResourceKey)Registries.f_268580_, (ResourceLocation)new ResourceLocation("forge", "is_magic"));
    private static final TagKey<DamageType> NEOFORGE_MAGIC = TagKey.m_203882_((ResourceKey)Registries.f_268580_, (ResourceLocation)new ResourceLocation("neoforge", "is_magic"));
    private static final TagKey<DamageType> PHYSICAL = TagKey.m_203882_((ResourceKey)Registries.f_268580_, (ResourceLocation)new ResourceLocation("ftboss", "physical_damage"));

    private ManorDamageKind() {
    }

    public static int classify(DamageSource source) {
        if (source == null || source.m_269533_(DamageTypeTags.f_268738_)) {
            return 0;
        }
        if (source.m_269533_(FORGE_MAGIC) || source.m_269533_(NEOFORGE_MAGIC) || source.m_276093_(DamageTypes.f_268515_) || source.m_276093_(DamageTypes.f_268530_) || source.m_276093_(DamageTypes.f_268493_) || source.m_276093_(DamageTypes.f_268679_)) {
            return 2;
        }
        Optional type = source.m_269150_().m_203543_();
        if (type.isPresent() && ((ResourceKey)type.get()).m_135782_().m_135827_().equals("irons_spellbooks")) {
            return 2;
        }
        if (source.m_269533_(PHYSICAL) || source.m_269533_(DamageTypeTags.f_268524_) || source.m_269533_(DamageTypeTags.f_268415_) || source.m_276093_(DamageTypes.f_268464_) || source.m_276093_(DamageTypes.f_268566_) || source.m_276093_(DamageTypes.f_268511_) || source.m_276093_(DamageTypes.f_268656_) || source.m_276093_(DamageTypes.f_268659_) || source.m_276093_(DamageTypes.f_268526_) || source.m_276093_(DamageTypes.f_268513_)) {
            return 1;
        }
        return 0;
    }
}

