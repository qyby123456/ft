/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.objectweb.asm.tree.ClassNode
 *  org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
 *  org.spongepowered.asm.mixin.extensibility.IMixinInfo
 */
package net.farmingtales.ftboss.mixin;

import java.util.List;
import java.util.Set;
import net.farmingtales.ftboss.item.NativeHookUnwind;
import net.farmingtales.ftboss.item.NativeMapPutLateProofExtension;
import net.farmingtales.ftboss.item.NativeRangedComposition;
import net.farmingtales.ftboss.item.NativeReadCutHooks;
import net.farmingtales.ftboss.item.OwnerFieldCoverageRegistry;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class NativeOptionalMixinPlugin
implements IMixinConfigPlugin {
    public static boolean available(ClassLoader classLoader, String string) {
        if (string.endsWith("FoodWeaponInfuserRepairMixin")) {
            return classLoader.getResource("fuzs/enchantinginfuser/world/inventory/InfuserMenu.class") != null;
        }
        if (string.endsWith("NativeIronActualDamageMixin")) {
            return classLoader.getResource("io/redspace/ironsspellbooks/damage/DamageSources.class") != null;
        }
        if (string.endsWith("NativeUseTickIngressMixin")) {
            return classLoader.getResource("net/gqstavo/armamentarium/item/EnderMaidenShortbowItem.class") != null || classLoader.getResource("com/github/L_Ender/cataclysm/items/Meat_Shredder.class") != null;
        }
        if (string.endsWith("NativeReleaseIngressMixin") || string.endsWith("NativeLeftClickIngressMixin") || string.endsWith("NativeAncientMeleeIngressMixin") || string.endsWith("NativeRushTokenFieldsMixin")) {
            return classLoader.getResource("com/github/L_Ender/cataclysm/items/Soul_Render.class") != null;
        }
        if (string.endsWith("NativeIronTokenFieldsMixin") || string.endsWith("NativeRecastTokenCleanupMixin")) {
            return classLoader.getResource("io/redspace/ironsspellbooks/api/spells/AbstractSpell.class") != null;
        }
        if (string.endsWith("NativeMiteCallerMixin") || string.endsWith("NativeMiteAnonymousHurtMixin")) {
            return classLoader.getResource("net/gqstavo/armamentarium/entity/MiteArrowEntity.class") != null;
        }
        if (string.endsWith("NativeMitePelletCallerMixin")) {
            return classLoader.getResource("net/gqstavo/armamentarium/entity/MitePelletEntity.class") != null;
        }
        if (string.endsWith("NativeCatLeftClickMixin") || string.endsWith("NativeCatLocalLeftClickMixin") || string.endsWith("NativeSoulRushMixin") || string.endsWith("NativeDelayedActorMixin") || string.endsWith("NativeSpecialDamageCallerMixin") || string.endsWith("NativeTentacleDamageCallerMixin") || string.endsWith("NativeMeatDamageCallerMixin")) {
            return classLoader.getResource("com/github/L_Ender/cataclysm/event/ServerEventHandler.class") != null;
        }
        if (string.endsWith("NativeSpecialWearMixin")) {
            return classLoader.getResource("com/github/L_Ender/cataclysm/items/Ancient_Spear.class") != null || classLoader.getResource("net/gqstavo/armamentarium/item/EnderMaidenShortbowItem.class") != null;
        }
        if (string.endsWith("FoodWeaponStaffCastMixin") || string.endsWith("FoodWeaponContinuousCastMixin") || string.endsWith("FoodWeaponMagicResetMixin") || string.endsWith("FoundationStaffCastMixin") || string.endsWith("FoundationStaffTickMixin") || string.endsWith("NativeIronSelectionMixin")) {
            return classLoader.getResource("io/redspace/ironsspellbooks/api/spells/AbstractSpell.class") != null;
        }
        if (string.endsWith("NativeMiteImpactObservationMixin")) {
            return classLoader.getResource("net/gqstavo/armamentarium/procedures/MiteArrowProjectileHitsLivingEntityProcedure.class") != null;
        }
        return true;
    }

    public boolean shouldApplyMixin(String string, String string2) {
        return NativeOptionalMixinPlugin.available(this.getClass().getClassLoader(), string2);
    }

    public void onLoad(String string) {
        NativeMapPutLateProofExtension.register();
    }

    public String getRefMapperConfig() {
        return null;
    }

    public void acceptTargets(Set<String> set, Set<String> set2) {
    }

    public List<String> getMixins() {
        return List.of("NativeReadCutItemStackMixin", "NativeReadCutCapabilityProviderMixin", "NativeReadCutEntityMixin", "NativeReadCutLivingEntityMixin", "NativeReadCutPlayerMixin", "NativeReadCutServerPlayerMixin", "NativeReadCutProjectileMixin", "NativeReadCutMinecraftServerMixin", "OwnerStatueWearInventoryMixin", "OwnerStatueWearItemStackMixin", "OwnerStatueWearRawDamageMixin", "OwnerStatueWearCustodyMixin");
    }

    public void preApply(String string, ClassNode classNode, String string2, IMixinInfo iMixinInfo) {
    }

    public void postApply(String string, ClassNode classNode, String string2, IMixinInfo iMixinInfo) {
        NativeRangedComposition.apply(classNode, string2);
        NativeHookUnwind.apply(classNode, string2);
        NativeReadCutHooks.apply(classNode, string2);
        OwnerFieldCoverageRegistry.applied(string, classNode, string2, iMixinInfo);
    }
}

