package net.farmingtales.ftboss.item;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class NativeWeaponObservationMixinPlugin implements IMixinConfigPlugin {
    public void onLoad(String mixinPackage) {
    }

    public String getRefMapperConfig() {
        return null;
    }

    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    public List<String> getMixins() {
        return List.of("NativeReadCutItemStackMixin", "NativeReadCutEntityMixin", "NativeReadCutLivingEntityMixin",
            "NativeReadCutPlayerMixin", "NativeReadCutServerPlayerMixin", "NativeReadCutProjectileMixin", "NativeReadCutMinecraftServerMixin");
    }

    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (targetClassName.equals("net.farmingtales.ftboss.item.FrostQueenStaffItem")
            && mixinClassName.equals("net.farmingtales.ftboss.mixin.NativeReadCutLivingEntityMixin")) {
            NativeComponentReadCutHooks.verifyFrostIngressEarly(targetClass, mixinClassName);
            return;
        }
        int expected = switch (mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1)) {
            case "NativeReadCutItemStackMixin" -> 12;
            case "NativeReadCutEntityMixin" -> 5;
            case "NativeReadCutLivingEntityMixin" -> 2;
            case "NativeReadCutPlayerMixin" -> 3;
            case "NativeReadCutServerPlayerMixin" -> 8;
            case "NativeReadCutProjectileMixin" -> 1;
            case "NativeReadCutMinecraftServerMixin" -> 3;
            default -> throw new IllegalArgumentException("Unexpected native observation mixin " + mixinClassName);
        };
        if (!targetClass.name.replace('/', '.').equals(targetClassName)
            || NativeComponentReadCutHooks.apply(targetClass, mixinClassName) != expected) {
            throw new IllegalStateException("Native observation lifecycle did not install all exact mutator sites: " + targetClassName);
        }
    }
}

