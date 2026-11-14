package net.farmingtales.ftboss.item;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class NativeWeaponLifecycleMixinPlugin implements IMixinConfigPlugin {
    public void onLoad(String mixinPackage) {
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
        int installed = NativeHookUnwind.apply(owner, mixin);
        int expected = mixin.endsWith("NativeIronSelectionMixin") ? 1 : mixin.endsWith("FoodWeaponStaffCastMixin") ? 2 : mixin.endsWith("NativeRecastTokenCleanupMixin") ? 3 : mixin.endsWith("NativeWearTokenMixin") ? 1 : 0;
        if (installed != expected) throw new IllegalStateException("Unexpected native lifecycle unwind closure " + target + ":" + mixin + ":" + installed + "/" + expected);
        NativeWeaponIngressSites.applied(target, owner, mixin, info);
    }
}
