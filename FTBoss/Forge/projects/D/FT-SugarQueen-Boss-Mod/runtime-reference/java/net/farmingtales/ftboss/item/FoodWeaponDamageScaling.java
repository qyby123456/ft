/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.item;

import java.util.List;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponFamilyBuild;
import net.farmingtales.ftboss.item.FoodWeaponLifecycle;
import net.farmingtales.ftboss.item.FoodWeaponMaterialFingerprint;
import net.farmingtales.ftboss.item.FoodWeaponSkillRarity;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import net.farmingtales.ftboss.item.FourWeaponFoundationAdapter;
import net.farmingtales.ftboss.item.WeaponMaterialBuild;
import net.farmingtales.ftboss.item.weapons.GourmetEvolutionWeapon;
import net.farmingtales.ftboss.util.FoodWeaponDamageAccounting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public final class FoodWeaponDamageScaling {
    public static final int VERSION = 2;
    public static final String PURPOSE = "DamagePurpose";
    public static final String GENERIC_J = "GENERIC_J";

    private FoodWeaponDamageScaling() {
    }

    public static CompoundTag captureGeneric(ServerPlayer serverPlayer, ItemStack itemStack) {
        if (serverPlayer == null || itemStack != serverPlayer.m_21205_() || itemStack.m_41613_() != 1 || !itemStack.m_41763_() && !(itemStack.m_41720_() instanceof GourmetEvolutionWeapon) || !FoodWeaponEvolution.isEvolutionCandidate(itemStack) || FoodWeaponLifecycle.isBroken(itemStack)) {
            return new CompoundTag();
        }
        if (FourWeaponFoundationAdapter.applies(itemStack) && FoodWeaponMaterialFingerprint.foundationSnapshot(itemStack).m_128456_() || FoodWeaponEvolution.evolutionLevel(itemStack) <= 0 || itemStack.m_41783_() != null && !FoodWeaponSourceLedger.canonicalInputValid((Tag)itemStack.m_41783_())) {
            return new CompoundTag();
        }
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128359_("Family", FoodWeaponFamilyBuild.kind(itemStack).name());
        compoundTag.m_128359_(PURPOSE, GENERIC_J);
        FoodWeaponDamageScaling.freezeFactors(itemStack, compoundTag, 1.0, 1.0, 1.0);
        return FoodWeaponSourceLedger.decorate(serverPlayer, itemStack, compoundTag, InteractionHand.MAIN_HAND);
    }

    public static void freezeFactors(ItemStack itemStack, CompoundTag compoundTag, double d, double d2, double d3) {
        if (compoundTag.m_128441_("ServerSignature")) {
            throw new IllegalArgumentException("Factors must precede HMAC");
        }
        double d4 = WeaponMaterialBuild.build(itemStack).damage();
        double d5 = FoodWeaponEvolution.damageEvolutionMultiplier(itemStack);
        double d6 = GENERIC_J.equals(compoundTag.m_128461_(PURPOSE)) ? FoodWeaponSkillRarity.activeView(itemStack).multiplier() : 1.0;
        FoodWeaponDamageAccounting.ledger(1.0, d4, d5, d6, d, d2, d3);
        compoundTag.m_128405_("DamageAccountingVersion", 2);
        compoundTag.m_128347_("MaterialFactor", d4);
        compoundTag.m_128347_("EvolutionFactor", d5);
        compoundTag.m_128347_("RarityFactor", d6);
        compoundTag.m_128347_("AppliedMaterial", d);
        compoundTag.m_128347_("AppliedEvolution", d2);
        compoundTag.m_128347_("AppliedRarity", d3);
    }

    public static double residual(CompoundTag compoundTag) {
        if (compoundTag.m_128451_("DamageAccountingVersion") != 2) {
            return Double.NaN;
        }
        for (String string : List.of("MaterialFactor", "EvolutionFactor", "RarityFactor", "AppliedMaterial", "AppliedEvolution", "AppliedRarity")) {
            if (compoundTag.m_128425_(string, 6) && FoodWeaponDamageAccounting.positive(compoundTag.m_128459_(string))) continue;
            return Double.NaN;
        }
        try {
            return FoodWeaponDamageAccounting.ledger(1.0, compoundTag.m_128459_("MaterialFactor"), compoundTag.m_128459_("EvolutionFactor"), compoundTag.m_128459_("RarityFactor"), compoundTag.m_128459_("AppliedMaterial"), compoundTag.m_128459_("AppliedEvolution"), compoundTag.m_128459_("AppliedRarity")).residual();
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return Double.NaN;
        }
    }

    public static double nativeAttack(ServerPlayer serverPlayer) {
        AttributeInstance attributeInstance2 = serverPlayer.m_21051_(Attributes.f_22281_);
        if (attributeInstance2 == null) {
            return 0.0;
        }
        AttributeInstance attributeInstance3 = new AttributeInstance(Attributes.f_22281_, attributeInstance -> {});
        attributeInstance3.m_22100_(attributeInstance2.m_22115_());
        for (AttributeModifier attributeModifier : attributeInstance2.m_22122_()) {
            if (attributeModifier.m_22214_().equals("ftboss.material.damage") || attributeModifier.m_22214_().equals("ftboss.food_evolution.damage")) continue;
            attributeInstance3.m_22118_(attributeModifier);
        }
        return Math.max(0.0, attributeInstance3.m_22135_());
    }
}

