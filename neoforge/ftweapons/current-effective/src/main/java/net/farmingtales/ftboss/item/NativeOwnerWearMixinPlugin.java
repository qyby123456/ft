package net.farmingtales.ftboss.item;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class NativeOwnerWearMixinPlugin implements IMixinConfigPlugin {
    public void onLoad(String mixinPackage) {
        NativeMapPutLateProofExtension.register();
    }

    public String getRefMapperConfig() {
        return null;
    }

    public boolean shouldApplyMixin(String target, String mixin) {
        return true;
    }

    public void acceptTargets(Set<String> ownTargets, Set<String> otherTargets) {
    }

    public List<String> getMixins() {
        return List.of();
    }

    public void preApply(String target, ClassNode owner, String mixin, IMixinInfo info) {
    }

    public void postApply(String target, ClassNode owner, String mixin, IMixinInfo info) {
        if (info.getConfig().getPlugin() != this || !info.getConfig().isRequired() || !info.getConfig().getName().equals("ftboss.owner-wear.mixins.json") || !info.getClassName().equals(mixin) || !owner.name.replace('/', '.').equals(target)) throw new IllegalStateException("Changed required native owner wear mixin context");
    }
}
