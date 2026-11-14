/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraftforge.registries.ForgeRegistries
 */
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
    private static final String[] GLORY_DEBUFFS = new String[]{"vulnerable", "fatigue", "sluggish", "fragile", "impotent", "lethargy"};
    private static final String[] GLORY_BUFFS = new String[]{"vigor", "dexterity", "stalwart", "resilience", "nimble", "stamina", "impervious", "perseverance"};
    private static final MobEffect[] FALLBACK_BUFFS = new MobEffect[]{MobEffects.f_19600_, MobEffects.f_19596_, MobEffects.f_19606_, MobEffects.f_19605_, MobEffects.f_19617_};
    private static final Set<MobEffect> UNSAFE = new HashSet<MobEffect>();

    private ButcherHitEffects() {
    }

    private static MobEffect find(String namespace, String name) {
        MobEffect effect = (MobEffect)ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(namespace, name));
        return effect == null || UNSAFE.contains(effect) ? null : effect;
    }

    private static MobEffect choose(Mob owner, String[] names, MobEffect fallback) {
        int start = owner.m_217043_().m_188503_(names.length);
        for (int offset = 0; offset < names.length; ++offset) {
            MobEffect effect = ButcherHitEffects.find("armamentarium", names[(start + offset) % names.length]);
            if (effect == null) continue;
            return effect;
        }
        return fallback;
    }

    private static void refresh(LivingEntity target, MobEffect effect, int duration, int amplifier) {
        if (effect == null || UNSAFE.contains(effect)) {
            return;
        }
        MobEffectInstance current = target.m_21124_(effect);
        if (current != null && (current.m_19564_() > amplifier || current.m_267577_() || current.m_19564_() == amplifier && current.m_19557_() >= duration)) {
            return;
        }
        try {
            if (current != null) {
                target.m_21195_(effect);
            }
            target.m_7292_(new MobEffectInstance(effect, duration, amplifier, false, target instanceof ServerPlayer, true));
        }
        catch (RuntimeException failure) {
            UNSAFE.add(effect);
            FTBossMod.LOGGER.warn("[FTBoss] Butcher skipped incompatible effect {}: {}", (Object)ForgeRegistries.MOB_EFFECTS.getKey((Object)effect), (Object)failure.toString());
        }
    }

    public static void apply(Mob mob, LivingEntity livingEntity, boolean bl) {
        ButcherHitEffects.applyTargetDebuffs(mob, livingEntity, bl);
        if (bl && mob.m_6084_()) {
            ButcherHitEffects.refresh((LivingEntity)mob, ButcherHitEffects.choose(mob, GLORY_BUFFS, FALLBACK_BUFFS[mob.m_217043_().m_188503_(FALLBACK_BUFFS.length)]), 200, 1);
        }
    }

    public static void applyTargetDebuffs(Mob mob, LivingEntity livingEntity, boolean bl) {
        if (livingEntity.m_6084_()) {
            ButcherHitEffects.refresh(livingEntity, ButcherHitEffects.find("armamentarium", "vulnerable"), 220, 1);
            MobEffect mobEffect = ButcherHitEffects.find("legendary_monsters", "bleeding");
            if (mobEffect == null) {
                mobEffect = ButcherHitEffects.find("tridents_n_stuff", "bleeding");
            }
            ButcherHitEffects.refresh(livingEntity, mobEffect, 260, 1);
            ButcherHitEffects.refresh(livingEntity, ButcherHitEffects.find("armamentarium", "blood_loss"), 200, 1);
            ButcherHitEffects.refresh(livingEntity, MobEffects.f_19613_, 200, 1);
            ButcherHitEffects.refresh(livingEntity, MobEffects.f_19597_, 200, 1);
            ButcherHitEffects.refresh(livingEntity, MobEffects.f_19599_, 200, 1);
            if (bl) {
                ButcherHitEffects.refresh(livingEntity, ButcherHitEffects.choose(mob, GLORY_DEBUFFS, MobEffects.f_19613_), 160, 0);
            }
        }
    }
}

