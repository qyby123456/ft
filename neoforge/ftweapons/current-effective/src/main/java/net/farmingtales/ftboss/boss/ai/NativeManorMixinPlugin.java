package net.farmingtales.ftboss.boss.ai;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class NativeManorMixinPlugin implements IMixinConfigPlugin {
    public void onLoad(String mixinPackage) {
    }

    public String getRefMapperConfig() {
        return null;
    }

    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return !mixinClassName.endsWith("NativeAnimationEmitterMixin")
            || !targetClassName.equals("net.farmingtales.ftboss.integ.spells.ManorSignatureSpell")
            || getClass().getClassLoader().getResource("io/redspace/ironsspellbooks/api/spells/AbstractSpell.class") != null;
    }

    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    public List<String> getMixins() {
        return List.of("NativeManorRaidMixin", "NativeManorBasicGoalMixin", "NativeAnimationEmitterMixin");
    }

    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (mixinClassName.equals("net.farmingtales.ftboss.mixin.NativeAnimationEmitterMixin")) {
            NativeAnimationEmitterInstaller.install(targetClass);
            NativeAnimationEmitterInstaller.verifyInstalled(targetClass);
            if (targetClassName.equals("net.farmingtales.ftboss.entity.EasterBossEntity")) {
                NativeBossFallbackMembers161.install(targetClass);
                NativeBossFallbackMembers161.verifyInstalled(targetClass);
                NativeDoraBurstReloadInstaller.install(targetClass);
                NativeDoraBurstReloadInstaller.verifyInstalled(targetClass);
                NativeShadowStrikeStartInstallerV3.install(targetClass);
                NativeShadowStrikeStartInstallerV3.verifyInstalled(targetClass);
                NativeXueliPanStartInstaller.install(targetClass);
                NativeXueliPanStartInstaller.verifyInstalled(targetClass);
                NativeQingyiRollStartInstaller.install(targetClass);
                NativeQingyiRollStartInstaller.verifyInstalled(targetClass);
                NativeBeamComboInstallerV2.install(targetClass);
                NativeBeamComboInstallerV2.verifyInstalled(targetClass);
            }
            return;
        }
        if (mixinClassName.equals("net.farmingtales.ftboss.mixin.NativeManorBasicGoalMixin")) {
            NativeManorBasicInstaller.install(targetClass);
            NativeManorBasicInstaller.verifyInstalled(targetClass);
            NativeManorCadenceInstaller.install(targetClass);
            NativeManorCadenceInstaller.verifyInstalled(targetClass);
            return;
        }
        if (mixinClassName.equals("net.farmingtales.ftboss.mixin.NativeManorRaidMixin")) {
            if (targetClassName.equals("net.farmingtales.ftboss.boss.ai.ManorFiveSkills")) {
                NativePhantomEchoSkillMembers161.install(targetClass);
                NativePhantomEchoSkillMembers161.verifyInstalled(targetClass);
                return;
            }
            NativeManorRaidInstaller.install(targetClass);
            NativeManorRaidInstaller.verifyInstalled(targetClass);
            if (targetClassName.equals("net.farmingtales.ftboss.entity.EasterBossEntity")) {
                NativeManorDefenseInstaller.install(targetClass);
                NativeManorDefenseInstaller.verifyInstalled(targetClass);
                NativeManorFinalDamageInstaller.install(targetClass);
                NativeManorFinalDamageInstaller.verifyInstalled(targetClass);
                NativeManorSharedDamageInstaller.install(targetClass);
                NativeManorSharedDamageInstaller.verifyInstalled(targetClass);
                NativeManorBasicInstaller.install(targetClass);
                NativeManorBasicInstaller.verifyInstalled(targetClass);
                NativeManorCadenceInstaller.install(targetClass);
                NativeManorCadenceInstaller.verifyInstalled(targetClass);
                NativeManorCastStateInstaller.install(targetClass);
                NativeManorCastStateInstaller.verifyInstalled(targetClass);
                NativeManorLifecycleInstaller.install(targetClass);
                NativeManorLifecycleInstaller.verifyInstalled(targetClass);
                NativeManorTargetInstaller.install(targetClass);
                NativeManorTargetInstaller.verifyInstalled(targetClass);
                NativeManorCrossGuardInstaller.install(targetClass);
                NativeManorCrossGuardInstaller.verifyInstalled(targetClass);
                NativeManorTurretInstaller.install(targetClass);
                NativeManorTurretInstaller.verifyInstalled(targetClass);
                NativeManorGroundDomainInstaller.install(targetClass);
                NativeManorGroundDomainInstaller.verifyInstalled(targetClass);
                NativeManorCancelInstaller.install(targetClass);
                NativeManorCancelInstaller.verifyInstalled(targetClass);
                NativeModelMotionAccessInstaller.install(targetClass);
                NativeModelMotionAccessInstaller.verifyInstalled(targetClass);
                NativeAnimationEmitterInstaller.install(targetClass);
                NativeAnimationEmitterInstaller.verifyInstalled(targetClass);
                NativeBossFallbackMembers161.install(targetClass);
                NativeBossFallbackMembers161.verifyInstalled(targetClass);
                NativeDoraBurstReloadInstaller.install(targetClass);
                NativeDoraBurstReloadInstaller.verifyInstalled(targetClass);
                NativeShadowStrikeStartInstallerV3.install(targetClass);
                NativeShadowStrikeStartInstallerV3.verifyInstalled(targetClass);
                NativeXueliPanStartInstaller.install(targetClass);
                NativeXueliPanStartInstaller.verifyInstalled(targetClass);
                NativeQingyiRollStartInstaller.install(targetClass);
                NativeQingyiRollStartInstaller.verifyInstalled(targetClass);
                NativeBeamComboInstallerV2.install(targetClass);
                NativeBeamComboInstallerV2.verifyInstalled(targetClass);
            } else if (targetClassName.equals("net.farmingtales.ftboss.boss.ai.BossScaling")) {
                NativeManorMultiplayerInstaller.install(targetClass);
                NativeManorMultiplayerInstaller.verifyInstalled(targetClass);
            }
            return;
        }
        if (!List.of("TequilaDamageEventsMixin", "ThrownTridentItemSyncMixin", "FinalBossDamageMixin", "ThrownTridentRendererMixin")
            .contains(mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1))) {
            throw new IllegalArgumentException("Unexpected manor mixin " + mixinClassName);
        }
    }
}

