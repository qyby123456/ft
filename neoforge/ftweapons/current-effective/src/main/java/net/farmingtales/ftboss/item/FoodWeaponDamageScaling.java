package net.farmingtales.ftboss.item;

import java.util.List;
import net.farmingtales.ftboss.item.weapons.GourmetEvolutionWeapon;
import net.farmingtales.ftboss.util.FoodWeaponDamageAccounting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public final class FoodWeaponDamageScaling {
    public static final int VERSION = 2;
    public static final String PURPOSE = "DamagePurpose";
    public static final String GENERIC_J = "GENERIC_J";

    private FoodWeaponDamageScaling() {
    }

    public static CompoundTag captureGeneric(ServerPlayer var0, ItemStack var1) {
        if (var0 == null
            || var1 != var0.getMainHandItem()
            || var1.getCount() != 1
            || !var1.isDamageableItem() && !(var1.getItem() instanceof GourmetEvolutionWeapon)
            || !FoodWeaponEvolution.isEvolutionCandidate(var1)
            || FoodWeaponLifecycle.isBroken(var1)) {
            return new CompoundTag();
        } else if ((!FourWeaponFoundationAdapter.applies(var1) || !FoodWeaponMaterialFingerprint.foundationSnapshot(var1).isEmpty())
            && FoodWeaponEvolution.evolutionLevel(var1) > 0
            && (var1.get(DataComponents.CUSTOM_DATA) == null || FoodWeaponSourceLedger.canonicalInputValid(var1.get(DataComponents.CUSTOM_DATA).copyTag()))) {
            CompoundTag var2 = new CompoundTag();
            var2.putString("Family", FoodWeaponFamilyBuild.kind(var1).name());
            var2.putString("DamagePurpose", "GENERIC_J");
            freezeFactors(var1, var2, 1.0, 1.0, 1.0);
            return FoodWeaponSourceLedger.decorate(var0, var1, var2, InteractionHand.MAIN_HAND);
        } else {
            return new CompoundTag();
        }
    }

    public static void freezeFactors(ItemStack var0, CompoundTag var1, double var2, double var4, double var6) {
        if (var1.contains("ServerSignature")) {
            throw new IllegalArgumentException("Factors must precede HMAC");
        } else {
            double var8 = WeaponMaterialBuild.build(var0).damage();
            double var10 = FoodWeaponEvolution.damageEvolutionMultiplier(var0);
            double var12 = "GENERIC_J".equals(var1.getString("DamagePurpose")) ? FoodWeaponSkillRarity.activeView(var0).multiplier() : 1.0;
            FoodWeaponDamageAccounting.ledger(1.0, var8, var10, var12, var2, var4, var6);
            var1.putInt("DamageAccountingVersion", 2);
            var1.putDouble("MaterialFactor", var8);
            var1.putDouble("EvolutionFactor", var10);
            var1.putDouble("RarityFactor", var12);
            var1.putDouble("AppliedMaterial", var2);
            var1.putDouble("AppliedEvolution", var4);
            var1.putDouble("AppliedRarity", var6);
        }
    }

    public static double residual(CompoundTag var0) {
        if (var0.getInt("DamageAccountingVersion") != 2) {
            return Double.NaN;
        } else {
            for (String var2 : List.of("MaterialFactor", "EvolutionFactor", "RarityFactor", "AppliedMaterial", "AppliedEvolution", "AppliedRarity")) {
                if (!var0.contains(var2, 6) || !FoodWeaponDamageAccounting.positive(var0.getDouble(var2))) {
                    return Double.NaN;
                }
            }

            try {
                return FoodWeaponDamageAccounting.ledger(
                        1.0,
                        var0.getDouble("MaterialFactor"),
                        var0.getDouble("EvolutionFactor"),
                        var0.getDouble("RarityFactor"),
                        var0.getDouble("AppliedMaterial"),
                        var0.getDouble("AppliedEvolution"),
                        var0.getDouble("AppliedRarity")
                    )
                    .residual();
            } catch (IllegalArgumentException var3) {
                return Double.NaN;
            }
        }
    }

    public static double nativeAttack(ServerPlayer var0) {
        return NativeDamageAttackAttributes.withoutWeaponGrowth(var0.getAttribute(Attributes.ATTACK_DAMAGE));
    }
}
