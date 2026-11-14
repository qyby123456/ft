package net.farmingtales.ftboss.mixin;

import java.util.List;
import java.util.Set;
import net.farmingtales.ftboss.item.NativeHookUnwind;
import net.farmingtales.ftboss.boss.ai.NativeManorRaidInstaller;
import net.farmingtales.ftboss.boss.ai.NativeManorDefenseInstaller;
import net.farmingtales.ftboss.boss.ai.NativeManorFinalDamageInstaller;
import net.farmingtales.ftboss.boss.ai.NativeManorSharedDamageInstaller;
import net.farmingtales.ftboss.boss.ai.NativeManorMultiplayerInstaller;
import net.farmingtales.ftboss.boss.ai.NativeManorBasicInstaller;
import net.farmingtales.ftboss.boss.ai.NativeManorCadenceInstaller;
import net.farmingtales.ftboss.boss.ai.NativeManorCastStateInstaller;
import net.farmingtales.ftboss.boss.ai.NativeManorLifecycleInstaller;
import net.farmingtales.ftboss.boss.ai.NativeManorTargetInstaller;
import net.farmingtales.ftboss.boss.ai.NativeManorCrossGuardInstaller;
import net.farmingtales.ftboss.boss.ai.NativeManorTurretInstaller;
import net.farmingtales.ftboss.boss.ai.NativeManorGroundDomainInstaller;
import net.farmingtales.ftboss.boss.ai.NativeManorCancelInstaller;
import net.farmingtales.ftboss.boss.ai.NativeModelMotionAccessInstaller;
import net.farmingtales.ftboss.boss.ai.NativeAnimationEmitterInstaller;
import net.farmingtales.ftboss.boss.ai.NativeDoraBurstReloadInstaller;
import net.farmingtales.ftboss.boss.ai.NativeShadowStrikeStartInstallerV3;
import net.farmingtales.ftboss.boss.ai.NativeXueliPanStartInstaller;
import net.farmingtales.ftboss.boss.ai.NativeQingyiRollStartInstaller;
import net.farmingtales.ftboss.boss.ai.NativeBeamComboInstallerV2;
import net.farmingtales.ftboss.item.NativeMapPutLateProofExtension;
import net.farmingtales.ftboss.item.NativeRangedComposition;
import net.farmingtales.ftboss.item.NativeReadCutHooks;
import net.farmingtales.ftboss.item.NativeComponentReadCutHooks;
import net.farmingtales.ftboss.item.OwnerFieldCoverageRegistry;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class NativeOptionalMixinPlugin implements IMixinConfigPlugin {
    public static boolean available(ClassLoader var0, String var1) {
        if (var1.endsWith("FoodWeaponInfuserRepairMixin")) {
            return var0.getResource("fuzs/enchantinginfuser/world/inventory/InfuserMenu.class") != null;
        } else if (var1.endsWith("NativeIronActualDamageMixin")) {
            return var0.getResource("io/redspace/ironsspellbooks/damage/DamageSources.class") != null;
        } else if (var1.endsWith("NativeUseTickIngressMixin")) {
            return var0.getResource("net/gqstavo/armamentarium/item/EnderMaidenShortbowItem.class") != null
                || var0.getResource("com/github/L_Ender/cataclysm/items/Meat_Shredder.class") != null;
        } else if (var1.endsWith("NativeReleaseIngressMixin")
            || var1.endsWith("NativeLeftClickIngressMixin")
            || var1.endsWith("NativeAncientMeleeIngressMixin")
            || var1.endsWith("NativeRushTokenFieldsMixin")) {
            return var0.getResource("com/github/L_Ender/cataclysm/items/Soul_Render.class") != null;
        } else if (var1.endsWith("NativeIronTokenFieldsMixin") || var1.endsWith("NativeRecastTokenCleanupMixin")) {
            return var0.getResource("io/redspace/ironsspellbooks/api/spells/AbstractSpell.class") != null;
        } else if (var1.endsWith("NativeMiteCallerMixin") || var1.endsWith("NativeMiteAnonymousHurtMixin")) {
            return var0.getResource("net/gqstavo/armamentarium/entity/MiteArrowEntity.class") != null;
        } else if (var1.endsWith("NativeMitePelletCallerMixin")) {
            return var0.getResource("net/gqstavo/armamentarium/entity/MitePelletEntity.class") != null;
        } else if (var1.endsWith("NativeCatLeftClickMixin")
            || var1.endsWith("NativeCatLocalLeftClickMixin")
            || var1.endsWith("NativeSoulRushMixin")
            || var1.endsWith("NativeDelayedActorMixin")
            || var1.endsWith("NativeSpecialDamageCallerMixin")
            || var1.endsWith("NativeTentacleDamageCallerMixin")
            || var1.endsWith("NativeMeatDamageCallerMixin")) {
            return var0.getResource("com/github/L_Ender/cataclysm/event/ServerEventHandler.class") != null;
        } else if (var1.endsWith("NativeSpecialWearMixin")) {
            return var0.getResource("com/github/L_Ender/cataclysm/items/Ancient_Spear.class") != null
                || var0.getResource("net/gqstavo/armamentarium/item/EnderMaidenShortbowItem.class") != null;
        } else if (var1.endsWith("FoodWeaponStaffCastMixin")
            || var1.endsWith("FoodWeaponContinuousCastMixin")
            || var1.endsWith("FoodWeaponMagicResetMixin")
            || var1.endsWith("FoundationStaffCastMixin")
            || var1.endsWith("FoundationStaffTickMixin")
            || var1.endsWith("NativeIronSelectionMixin")) {
            return var0.getResource("io/redspace/ironsspellbooks/api/spells/AbstractSpell.class") != null;
        } else {
            return var1.endsWith("NativeMiteImpactObservationMixin")
                ? var0.getResource("net/gqstavo/armamentarium/procedures/MiteArrowProjectileHitsLivingEntityProcedure.class") != null
                : true;
        }
    }

    public boolean shouldApplyMixin(String var1, String var2) {
        if (var2.endsWith("NativeAnimationEmitterMixin") && var1.equals("net.farmingtales.ftboss.integ.spells.ManorSignatureSpell")) {
            return this.getClass().getClassLoader().getResource("io/redspace/ironsspellbooks/api/spells/AbstractSpell.class") != null;
        }
        return available(this.getClass().getClassLoader(), var2);
    }

    public void onLoad(String var1) {
        NativeMapPutLateProofExtension.register();
    }

    public String getRefMapperConfig() {
        return null;
    }

    public void acceptTargets(Set<String> var1, Set<String> var2) {
    }

    public List<String> getMixins() {
        return List.of();
    }

    public void preApply(String var1, ClassNode var2, String var3, IMixinInfo var4) {
    }

    public void postApply(String var1, ClassNode var2, String var3, IMixinInfo var4) {
        if (var3.equals("net.farmingtales.ftboss.mixin.NativeAnimationEmitterMixin")) {
            NativeAnimationEmitterInstaller.install(var2);
            NativeAnimationEmitterInstaller.verifyInstalled(var2);
            if (var1.equals("net.farmingtales.ftboss.entity.EasterBossEntity")) {
                NativeDoraBurstReloadInstaller.install(var2);
                NativeDoraBurstReloadInstaller.verifyInstalled(var2);
                NativeShadowStrikeStartInstallerV3.install(var2);
                NativeShadowStrikeStartInstallerV3.verifyInstalled(var2);
                NativeXueliPanStartInstaller.install(var2);
                NativeXueliPanStartInstaller.verifyInstalled(var2);
                NativeQingyiRollStartInstaller.install(var2);
                NativeQingyiRollStartInstaller.verifyInstalled(var2);
                NativeBeamComboInstallerV2.install(var2);
                NativeBeamComboInstallerV2.verifyInstalled(var2);
            }
            return;
        }
        if (var3.equals("net.farmingtales.ftboss.mixin.NativeManorBasicGoalMixin")) {
            NativeManorBasicInstaller.install(var2);
            NativeManorBasicInstaller.verifyInstalled(var2);
            NativeManorCadenceInstaller.install(var2);
            NativeManorCadenceInstaller.verifyInstalled(var2);
            return;
        }
        if (var3.equals("net.farmingtales.ftboss.mixin.NativeManorRaidMixin")) {
            NativeManorRaidInstaller.install(var2);
            NativeManorRaidInstaller.verifyInstalled(var2);
            if (var1.equals("net.farmingtales.ftboss.entity.EasterBossEntity")) {
                NativeManorDefenseInstaller.install(var2);
                NativeManorDefenseInstaller.verifyInstalled(var2);
                NativeManorFinalDamageInstaller.install(var2);
                NativeManorFinalDamageInstaller.verifyInstalled(var2);
                NativeManorSharedDamageInstaller.install(var2);
                NativeManorSharedDamageInstaller.verifyInstalled(var2);
                NativeManorBasicInstaller.install(var2);
                NativeManorBasicInstaller.verifyInstalled(var2);
                NativeManorCadenceInstaller.install(var2);
                NativeManorCadenceInstaller.verifyInstalled(var2);
                NativeManorCastStateInstaller.install(var2);
                NativeManorCastStateInstaller.verifyInstalled(var2);
                NativeManorLifecycleInstaller.install(var2);
                NativeManorLifecycleInstaller.verifyInstalled(var2);
                NativeManorTargetInstaller.install(var2);
                NativeManorTargetInstaller.verifyInstalled(var2);
                NativeManorCrossGuardInstaller.install(var2);
                NativeManorCrossGuardInstaller.verifyInstalled(var2);
                NativeManorTurretInstaller.install(var2);
                NativeManorTurretInstaller.verifyInstalled(var2);
                NativeManorGroundDomainInstaller.install(var2);
                NativeManorGroundDomainInstaller.verifyInstalled(var2);
                NativeManorCancelInstaller.install(var2);
                NativeManorCancelInstaller.verifyInstalled(var2);
                NativeModelMotionAccessInstaller.install(var2);
                NativeModelMotionAccessInstaller.verifyInstalled(var2);
                NativeAnimationEmitterInstaller.install(var2);
                NativeAnimationEmitterInstaller.verifyInstalled(var2);
                NativeDoraBurstReloadInstaller.install(var2);
                NativeDoraBurstReloadInstaller.verifyInstalled(var2);
                NativeShadowStrikeStartInstallerV3.install(var2);
                NativeShadowStrikeStartInstallerV3.verifyInstalled(var2);
                NativeXueliPanStartInstaller.install(var2);
                NativeXueliPanStartInstaller.verifyInstalled(var2);
                NativeQingyiRollStartInstaller.install(var2);
                NativeQingyiRollStartInstaller.verifyInstalled(var2);
                NativeBeamComboInstallerV2.install(var2);
                NativeBeamComboInstallerV2.verifyInstalled(var2);
            } else if (var1.equals("net.farmingtales.ftboss.boss.ai.BossScaling")) {
                NativeManorMultiplayerInstaller.install(var2);
                NativeManorMultiplayerInstaller.verifyInstalled(var2);
            }
            return;
        }
        NativeRangedComposition.apply(var2, var3);
        NativeHookUnwind.apply(var2, var3);
        if (var3.endsWith("NativeReadCutCapabilityProviderMixin")) {
            NativeReadCutHooks.apply(var2, var3);
        } else {
            NativeComponentReadCutHooks.apply(var2, var3);
        }
        OwnerFieldCoverageRegistry.applied(var1, var2, var3, var4);
    }
}
