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
import net.farmingtales.ftboss.item.OwnerFieldCoverageRegistry;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class OwnerFieldCoveragePlugin
implements IMixinConfigPlugin {
    public void onLoad(String string) {
    }

    public String getRefMapperConfig() {
        return null;
    }

    public boolean shouldApplyMixin(String string, String string2) {
        return true;
    }

    public void acceptTargets(Set<String> set, Set<String> set2) {
    }

    public List<String> getMixins() {
        return List.of();
    }

    public void preApply(String string, ClassNode classNode, String string2, IMixinInfo iMixinInfo) {
    }

    public void postApply(String string, ClassNode classNode, String string2, IMixinInfo iMixinInfo) {
        OwnerFieldCoverageRegistry.applied(string, classNode, string2, iMixinInfo);
    }
}

