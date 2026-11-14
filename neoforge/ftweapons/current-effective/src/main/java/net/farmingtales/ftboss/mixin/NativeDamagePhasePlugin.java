package net.farmingtales.ftboss.mixin;

import java.util.List;
import java.util.Set;
import net.farmingtales.ftboss.item.NativeDamagePhaseInstaller;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class NativeDamagePhasePlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return mixinClassName.equals("net.farmingtales.ftboss.mixin.NativeDamagePhaseMixin")
            && Set.of("net.minecraft.world.entity.LivingEntity", "net.minecraft.world.entity.player.Player").contains(targetClassName);
    }

    @Override
    public void acceptTargets(Set<String> ownTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return List.of("NativeDamagePhaseMixin");
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (!shouldApplyMixin(targetClassName, mixinClassName)) {
            throw new IllegalStateException("Unexpected native damage phase installation");
        }
        NativeDamagePhaseInstaller.install(targetClass);
        NativeDamagePhaseInstaller.verifyInstalled(targetClass);
    }
}
