package net.farmingtales.ftboss.boss.ai;

import java.util.HashSet;
import java.util.Set;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.registries.ForgeRegistries;

public final class ButcherHitEffects {
    private static final String[] GLORY_DEBUFFS = {"vulnerable", "fatigue", "sluggish", "fragile", "impotent", "lethargy"};
    private static final String[] GLORY_BUFFS = {"vigor", "dexterity", "stalwart", "resilience", "nimble", "stamina", "impervious", "perseverance"};
    private static final MobEffect[] FALLBACK_BUFFS = {MobEffects.DAMAGE_BOOST, MobEffects.MOVEMENT_SPEED,
            MobEffects.DAMAGE_RESISTANCE, MobEffects.REGENERATION, MobEffects.ABSORPTION};
    private static final Set<MobEffect> UNSAFE = new HashSet<>();

    private ButcherHitEffects() {}

    public static void apply(Mob owner, LivingEntity target, boolean empowered) {
        applyTargetDebuffs(owner, target, empowered);
        if (empowered && owner.isAlive()) refresh(owner, choose(owner, GLORY_BUFFS,
                FALLBACK_BUFFS[owner.getRandom().nextInt(FALLBACK_BUFFS.length)]), 200, 1);
    }

    public static void applyTargetDebuffs(Mob owner, LivingEntity target, boolean empowered) {
        if (target.isAlive()) {
            refresh(target, find("armamentarium", "vulnerable"), 220, 1);
            MobEffect bleeding = find("legendary_monsters", "bleeding");
            if (bleeding == null) bleeding = find("tridents_n_stuff", "bleeding");
            refresh(target, bleeding, 260, 1);
            refresh(target, find("armamentarium", "blood_loss"), 200, 1);
            refresh(target, MobEffects.WEAKNESS, 200, 1);
            refresh(target, MobEffects.MOVEMENT_SLOWDOWN, 200, 1);
            refresh(target, MobEffects.DIG_SLOWDOWN, 200, 1);
            if (empowered) refresh(target, choose(owner, GLORY_DEBUFFS, MobEffects.WEAKNESS), 160, 0);
        }
    }

    private static MobEffect find(String namespace, String name) {
        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(namespace, name));
        return effect == null || UNSAFE.contains(effect) ? null : effect;
    }

    private static MobEffect choose(Mob owner, String[] names, MobEffect fallback) {
        int start = owner.getRandom().nextInt(names.length);
        for (int offset = 0; offset < names.length; offset++) {
            MobEffect effect = find("armamentarium", names[(start + offset) % names.length]);
            if (effect != null) return effect;
        }
        return fallback;
    }

    private static void refresh(LivingEntity target, MobEffect effect, int duration, int amplifier) {
        if (effect == null || UNSAFE.contains(effect)) return;
        MobEffectInstance current = target.getEffect(effect);
        if (current != null && (current.getAmplifier() > amplifier || current.isInfiniteDuration()
                || current.getAmplifier() == amplifier && current.getDuration() >= duration)) return;
        try {
            if (current != null) target.removeEffect(effect);
            target.addEffect(new MobEffectInstance(effect, duration, amplifier, false, target instanceof ServerPlayer, true));
        } catch (RuntimeException failure) {
            UNSAFE.add(effect);
            FTBossMod.LOGGER.warn("[FTBoss] Butcher skipped incompatible effect {}: {}",
                    ForgeRegistries.MOB_EFFECTS.getKey(effect), failure.toString());
        }
    }
}
