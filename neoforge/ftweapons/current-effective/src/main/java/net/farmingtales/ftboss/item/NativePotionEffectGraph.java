package net.farmingtales.ftboss.item;

import com.google.common.collect.ImmutableList;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.common.extensions.IMobEffectExtension;

public final class NativePotionEffectGraph {
    private static final Field BLEND = blendField();
    private final PotionContents potion;
    private final List<MobEffectInstance> custom;
    private final List<MobEffectInstance> base;
    private final MobEffectInstance[] customValues;
    private final MobEffectInstance[] baseValues;
    private final Map<MobEffectInstance, Node> nodes = new IdentityHashMap<>();

    private NativePotionEffectGraph(PotionContents potion) {
        this.potion = potion;
        this.custom = potion == null ? List.of() : ((NativePotionEffectFields.Potion)(Object)potion).ftboss$nativePotionEffects();
        this.base = potion == null || potion.potion().isEmpty() ? List.of() : potion.potion().orElseThrow().value().getEffects();
        this.customValues = captureList(this.custom);
        this.baseValues = captureList(this.base);
        if (!live()) throw new IllegalStateException("Potion effect graph changed during capture");
    }

    public static NativePotionEffectGraph capture(DataComponentMap components) {
        return new NativePotionEffectGraph(components.get(DataComponents.POTION_CONTENTS));
    }

    private static Field blendField() {
        try {
            Field field = MobEffectInstance.class.getDeclaredField("blendState");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException failure) {
            throw new ExceptionInInitializerError(failure);
        }
    }

    private static Object blend(MobEffectInstance effect) {
        try {
            return BLEND.get(effect);
        } catch (IllegalAccessException failure) {
            throw new IllegalStateException("Actual private blend field unavailable", failure);
        }
    }

    private static boolean immutable(List<?> values) {
        return values instanceof ImmutableList<?> || values.getClass() == List.of().getClass()
            || values.getClass() == List.of(1).getClass() || values.getClass() == List.of(1, 2, 3).getClass();
    }

    private MobEffectInstance[] captureList(List<MobEffectInstance> values) {
        if (!immutable(values) || values.size() > 65536) throw new IllegalArgumentException("Retained mutable potion constructor list is not physical authority");
        MobEffectInstance[] captured = values.toArray(MobEffectInstance[]::new);
        for (MobEffectInstance effect : captured) walk(effect, Collections.newSetFromMap(new IdentityHashMap<>()), 0);
        return captured;
    }

    private void walk(MobEffectInstance effect, Set<MobEffectInstance> path, int depth) {
        if (effect == null || effect.getClass() != MobEffectInstance.class || depth > 64 || !path.add(effect)) {
            throw new IllegalArgumentException("Unsupported, cyclic or excessive native potion effect graph");
        }
        if (this.nodes.containsKey(effect)) {
            path.remove(effect);
            return;
        }
        if (this.nodes.size() >= 65536) throw new IllegalArgumentException("Native potion effect node budget exceeded");
        Node node = new Node(effect);
        this.nodes.put(effect, node);
        if (node.state.hidden() != null) walk(node.state.hidden(), path, depth + 1);
        path.remove(effect);
    }

    private static boolean listSame(List<MobEffectInstance> actual, MobEffectInstance[] expected) {
        if (actual.size() != expected.length) return false;
        for (int index = 0; index < expected.length; index++) if (actual.get(index) != expected[index]) return false;
        return true;
    }

    public boolean live() {
        if (this.potion != null && ((NativePotionEffectFields.Potion)(Object)this.potion).ftboss$nativePotionEffects() != this.custom) return false;
        if (!listSame(this.custom, this.customValues) || !listSame(this.base, this.baseValues)) return false;
        for (Node node : this.nodes.values()) if (!node.live()) return false;
        return true;
    }

    public boolean same(NativePotionEffectGraph current) {
        if (current == null || this.potion != current.potion || this.custom != current.custom || this.base != current.base
            || !this.nodes.keySet().equals(current.nodes.keySet()) || !live() || !current.live()) return false;
        for (MobEffectInstance effect : this.nodes.keySet()) if (!this.nodes.get(effect).same(current.nodes.get(effect))) return false;
        return true;
    }

    public void observeNodes(Consumer<Object> observer) {
        if (this.potion != null) observer.accept(this.potion);
        this.nodes.forEach((effect, node) -> {
            observer.accept(effect);
            observer.accept(node.blend);
        });
    }

    private static final class Node {
        private final MobEffectInstance effect;
        private final NativePotionEffectFields.EffectState state;
        private final Object blend;
        private final NativePotionEffectFields.BlendState blendState;
        private final Set<EffectCure> cures;

        private Node(MobEffectInstance effect) {
            this.effect = effect;
            this.state = ((NativePotionEffectFields.Effect)(Object)effect).ftboss$nativeEffectState();
            this.blend = blend(effect);
            this.blendState = ((NativePotionEffectFields.Blend)this.blend).ftboss$nativeBlendState();
            this.cures = Set.copyOf(this.state.cures());
            try {
                if (effect.getEffect().value().getClass().getMethod("fillEffectCures", Set.class, MobEffectInstance.class).getDeclaringClass() != IMobEffectExtension.class) {
                    throw new IllegalArgumentException("Unknown native cure initializer can retain the physical cure set");
                }
            } catch (ReflectiveOperationException failure) {
                throw new IllegalArgumentException("Actual native cure initializer unavailable", failure);
            }
            if (this.state.escaped() || this.state.version() == Long.MAX_VALUE || this.blendState.version() == Long.MAX_VALUE) {
                throw new IllegalArgumentException("Escaped or exhausted native effect storage");
            }
        }

        private boolean live() {
            var current = ((NativePotionEffectFields.Effect)(Object)this.effect).ftboss$nativeEffectState();
            return !current.escaped() && this.state.effect() == current.effect() && this.state.duration() == current.duration()
                && this.state.amplifier() == current.amplifier() && this.state.ambient() == current.ambient() && this.state.visible() == current.visible()
                && this.state.showIcon() == current.showIcon() && this.state.hidden() == current.hidden() && this.state.cures() == current.cures()
                && this.state.version() == current.version() && this.cures.equals(current.cures()) && this.blend == blend(this.effect)
                && this.blendState.equals(((NativePotionEffectFields.Blend)this.blend).ftboss$nativeBlendState());
        }

        private boolean same(Node current) {
            return this.effect == current.effect && this.state.equals(current.state) && this.blend == current.blend
                && this.blendState.equals(current.blendState) && this.cures.equals(current.cures);
        }
    }
}
