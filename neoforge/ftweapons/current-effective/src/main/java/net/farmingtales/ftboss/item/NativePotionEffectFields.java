package net.farmingtales.ftboss.item;

import java.lang.StackWalker.Option;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.EffectCure;

public final class NativePotionEffectFields {
    private NativePotionEffectFields() {}

    public static void requireObserver(Class<?> target, String member) {
        var frames = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE).walk(stream -> stream.skip(1L).limit(2L).toList());
        if (frames.size() != 2 || frames.get(0).getDeclaringClass() != target || !frames.get(0).getMethodName().equals(member)
            || frames.get(1).getDeclaringClass().getNestHost() != NativePotionEffectGraph.class) {
            throw new IllegalStateException("Native potion effect physical fields denied");
        }
    }

    public interface Potion {
        List<MobEffectInstance> ftboss$nativePotionEffects();
    }

    public interface Effect {
        EffectState ftboss$nativeEffectState();
    }

    public interface Blend {
        BlendState ftboss$nativeBlendState();
    }

    public record EffectState(Holder<MobEffect> effect, int duration, int amplifier, boolean ambient, boolean visible,
        boolean showIcon, MobEffectInstance hidden, Set<EffectCure> cures, long version, boolean escaped) {}

    public record BlendState(int factorBits, int previousBits, long version) {}
}
