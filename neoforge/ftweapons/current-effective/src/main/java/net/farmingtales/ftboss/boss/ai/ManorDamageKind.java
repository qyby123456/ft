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
    private static final TagKey<DamageType> FORGE_MAGIC = TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("forge", "is_magic"));
    private static final TagKey<DamageType> NEOFORGE_MAGIC = TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("neoforge", "is_magic"));
    private static final TagKey<DamageType> PHYSICAL = TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("ftboss", "physical_damage"));

    private ManorDamageKind() {
    }

    public static int classify(DamageSource source) {
        if (source == null || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return 0;
        } else if (!source.is(FORGE_MAGIC)
            && !source.is(NEOFORGE_MAGIC)
            && !source.is(DamageTypes.MAGIC)
            && !source.is(DamageTypes.INDIRECT_MAGIC)
            && !source.is(DamageTypes.WITHER)
            && !source.is(DamageTypes.SONIC_BOOM)) {
            Optional<ResourceKey<DamageType>> type = source.typeHolder().unwrapKey();
            if (type.isPresent() && type.get().location().getNamespace().equals("irons_spellbooks")) {
                return 2;
            } else {
                return !source.is(PHYSICAL)
                        && !source.is(DamageTypeTags.IS_PROJECTILE)
                        && !source.is(DamageTypeTags.IS_EXPLOSION)
                        && !source.is(DamageTypes.PLAYER_ATTACK)
                        && !source.is(DamageTypes.MOB_ATTACK)
                        && !source.is(DamageTypes.MOB_ATTACK_NO_AGGRO)
                        && !source.is(DamageTypes.STING)
                        && !source.is(DamageTypes.FALLING_BLOCK)
                        && !source.is(DamageTypes.FALLING_ANVIL)
                        && !source.is(DamageTypes.FALLING_STALACTITE)
                    ? 0
                    : 1;
            }
        } else {
            return 2;
        }
    }
}
