/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.util;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.registries.ForgeRegistries;

public final class ManorSpellRules {
    public static final float HIT_LIMIT = 50.0f;
    public static final float WINDOW_LIMIT = 50.0f;
    public static final float HARD_LIMIT = 20.0f;
    public static final float ESCALATE_TOTAL = 400.0f;
    public static final int WINDOW_TICKS = 20;
    public static final String STUPOR_EFFECT = "bountifulfares:stupor";
    private static final String K_TOTAL = "ftboss_spell_total";
    private static final String K_WINDOW = "ftboss_spell_window";
    private static final String K_WINDOW_START = "ftboss_spell_window_start";
    private static final float TOTAL_CEILING = 1000000.0f;
    private static final String[] SPELL_DAMAGE_HINTS = new String[]{"spell", "magic", "arcane", "sorcery", "hex", "curse", "mana", "runic", "rune"};
    private static final String[] SPELL_NAMESPACES = new String[]{"irons_spellbooks", "irons_lib"};
    public static final int MAX_LEVEL = 1;
    public static final int FULL_POWER_LEVEL = 5;
    public static final int MAX_TARGETS = 12;
    public static final int MAX_FIELDS = 64;
    public static final double RANGE = 24.0;
    public static final String LOCK_KEY = "ftboss:manor_spell_until";

    private ManorSpellRules() {
    }

    public static boolean stupor(MobEffectInstance mobEffectInstance) {
        return mobEffectInstance != null && ManorSpellRules.stupor(mobEffectInstance.m_19544_());
    }

    public static boolean stupor(MobEffect mobEffect) {
        if (mobEffect == null) {
            return false;
        }
        ResourceLocation resourceLocation = ForgeRegistries.MOB_EFFECTS.getKey((Object)mobEffect);
        return resourceLocation != null && STUPOR_EFFECT.equals(resourceLocation.toString());
    }

    public static float cap(EasterBossEntity easterBossEntity, DamageSource damageSource, float f) {
        if (easterBossEntity == null || damageSource == null) {
            return f;
        }
        if (!Float.isFinite(f) || f <= 0.0f) {
            return f;
        }
        if (!ManorSpellRules.spellDamage(damageSource)) {
            return f;
        }
        float f2 = ManorSpellRules.spellLimit(easterBossEntity);
        float f3 = Math.min(f, f2);
        float f4 = ManorSpellRules.windowRemaining(easterBossEntity, f2);
        if (f4 <= 0.0f) {
            return Float.NaN;
        }
        if ((f3 = Math.min(f3, f4)) <= 0.0f) {
            return Float.NaN;
        }
        ManorSpellRules.record(easterBossEntity, f3);
        return f3;
    }

    public static float spellLimit(EasterBossEntity easterBossEntity) {
        return ManorSpellRules.total(easterBossEntity) > 400.0f ? 20.0f : 50.0f;
    }

    public static float total(EasterBossEntity easterBossEntity) {
        return easterBossEntity.getPersistentData().m_128457_(K_TOTAL);
    }

    public static void reset(EasterBossEntity easterBossEntity) {
        CompoundTag compoundTag = easterBossEntity.getPersistentData();
        compoundTag.m_128473_(K_TOTAL);
        compoundTag.m_128473_(K_WINDOW);
        compoundTag.m_128473_(K_WINDOW_START);
    }

    public static boolean spellDamage(DamageSource damageSource) {
        if (damageSource == null) {
            return false;
        }
        if (damageSource.m_269533_(DamageTypeTags.f_268738_)) {
            return ManorSpellRules.causedByPlayer(damageSource);
        }
        ResourceLocation resourceLocation = ManorSpellRules.typeId(damageSource);
        if (resourceLocation == null) {
            return false;
        }
        for (String string : SPELL_NAMESPACES) {
            if (!string.equals(resourceLocation.m_135827_())) continue;
            return ManorSpellRules.causedByPlayer(damageSource);
        }
        String string = resourceLocation.m_135815_().toLowerCase(Locale.ROOT);
        for (String string2 : SPELL_DAMAGE_HINTS) {
            if (!string.contains(string2)) continue;
            return ManorSpellRules.causedByPlayer(damageSource);
        }
        return false;
    }

    private static boolean causedByPlayer(DamageSource damageSource) {
        if (damageSource.m_7639_() instanceof Player) {
            return true;
        }
        Entity entity = damageSource.m_7640_();
        int n = 0;
        while (entity instanceof Projectile) {
            Entity entity2;
            Projectile projectile = (Projectile)entity;
            if (n >= 8 || (entity2 = projectile.m_19749_()) == null || entity2 == entity) break;
            if (entity2 instanceof Player) {
                return true;
            }
            entity = entity2;
            ++n;
        }
        return false;
    }

    private static ResourceLocation typeId(DamageSource damageSource) {
        ResourceLocation resourceLocation = ManorSpellRules.typeIdOrNull(damageSource);
        if (resourceLocation != null) {
            return resourceLocation;
        }
        String string = damageSource.m_19385_();
        if (string == null) {
            return null;
        }
        String string2 = string.toLowerCase(Locale.ROOT);
        return ManorSpellRules.lowercasedDamageType(string2);
    }

    private static ResourceLocation typeIdOrNull(DamageSource damageSource) {
        Holder holder = damageSource.m_269150_();
        if (holder == null || holder.m_203543_().isEmpty()) {
            return null;
        }
        return ((ResourceKey)holder.m_203543_().get()).m_135782_();
    }

    private static ResourceLocation lowercasedDamageType(String string) {
        for (String string2 : SPELL_DAMAGE_HINTS) {
            if (!string.contains(string2)) continue;
            return ResourceLocation.m_135820_((String)("minecraft:player_spell_" + string2));
        }
        return null;
    }

    private static float windowRemaining(EasterBossEntity easterBossEntity, float f) {
        CompoundTag compoundTag = easterBossEntity.getPersistentData();
        long l = easterBossEntity.m_9236_().m_46467_();
        long l2 = compoundTag.m_128454_(K_WINDOW_START);
        float f2 = compoundTag.m_128457_(K_WINDOW);
        if (l2 == 0L || l < l2 || l - l2 >= 20L) {
            compoundTag.m_128356_(K_WINDOW_START, l);
            compoundTag.m_128350_(K_WINDOW, 0.0f);
            return f;
        }
        return f - f2;
    }

    private static void record(EasterBossEntity easterBossEntity, float f) {
        if (!Float.isFinite(f) || f <= 0.0f) {
            return;
        }
        CompoundTag compoundTag = easterBossEntity.getPersistentData();
        compoundTag.m_128350_(K_WINDOW, Math.min(1000000.0f, compoundTag.m_128457_(K_WINDOW) + f));
        compoundTag.m_128350_(K_TOTAL, Math.min(1000000.0f, compoundTag.m_128457_(K_TOTAL) + f));
    }

    public static List<Kind> pair(int variant) {
        return Arrays.stream(Kind.values()).filter(kind -> kind.variant == variant).toList();
    }

    public static int level(int value) {
        return 5;
    }

    public static double extraRadius(Kind kind) {
        return switch (kind) {
            case Kind.CANDLE_ECLIPSE, Kind.BLOOD_VERDICT, Kind.NIGHTFALL_BLADES, Kind.WINTER_CLOCK -> 3.5;
            default -> 4.0;
        };
    }

    public static int extraDelay(Kind kind) {
        return switch (kind) {
            case Kind.BLOOD_VERDICT, Kind.SERAPH_DESCENT -> 20;
            case Kind.CANDLE_ECLIPSE -> 24;
            default -> 12;
        };
    }

    public static boolean extraPulse(Kind kind, int age) {
        if (!kind.additional() || kind.support()) {
            return false;
        }
        int count = switch (kind) {
            case Kind.WINTER_CLOCK, Kind.EFFIGY_PROCESSION, Kind.PRISM_BATTERY -> 3;
            case Kind.MAGNETIC_STORM -> 4;
            case Kind.NIGHTFALL_BLADES -> 2;
            default -> 1;
        };
        return ManorSpellRules.pulse(age - ManorSpellRules.extraDelay(kind), 20, count);
    }

    public static boolean validPoint(double distanceSquared) {
        return Double.isFinite(distanceSquared) && distanceSquared >= 0.0 && distanceSquared <= 1024.0;
    }

    public static boolean cone(double forward, double sideways, double length) {
        return Double.isFinite(forward) && Double.isFinite(sideways) && forward >= 0.0 && forward <= length && Math.abs(sideways) <= 0.8 + forward * 0.7;
    }

    public static boolean pulse(int age, int interval, int count) {
        return age >= 0 && interval > 0 && count > 0 && age % interval == 0 && age / interval < count;
    }

    public static boolean eligible(boolean mini, boolean giant, boolean noAi, boolean finale) {
        return !mini && !giant && !noAi && !finale;
    }

    public static enum Kind {
        EFFIGY_VOLLEY(0, "effigy_volley", "evocation", 8, 65, 16, 24, 15118293),
        EFFIGY_WARD(0, "effigy_ward", "holy", 0, 75, 26, 28, 16112311),
        CANDLE_REAP(1, "candle_reap", "blood", 18, 70, 16, 24, 15393225),
        CANDLE_PRISON(1, "candle_prison", "ender", 6, 90, 24, 32, 12493295),
        ARCANE_FUSILLADE(2, "arcane_fusillade", "evocation", 7, 65, 14, 24, 15314276),
        BREACH_SHELL(2, "breach_shell", "fire", 24, 90, 24, 32, 15890519),
        RENDING_CLEAVE(3, "rending_cleave", "blood", 20, 70, 18, 28, 12998000),
        CRIMSON_OATH(3, "crimson_oath", "blood", 0, 80, 30, 28, 15822201),
        SEARING_PAN(4, "searing_pan", "fire", 16, 65, 16, 24, 16762221),
        HEARTH_FEAST(4, "hearth_feast", "holy", 0, 95, 30, 32, 16240269),
        FOUR_TIDES(5, "four_tides", "ice", 18, 75, 18, 28, 6609370),
        ELIXIR_RAIN(5, "elixir_rain", "nature", 5, 90, 24, 32, 9296262),
        ORBITAL_GEARS(6, "orbital_gears", "evocation", 9, 80, 20, 28, 10542311),
        PRESSURE_LANCE(6, "pressure_lance", "lightning", 26, 90, 22, 32, 7978234),
        SHADOW_BRAND(7, "shadow_brand", "ender", 7, 85, 22, 32, 10254798),
        VOID_ECHO(7, "void_echo", "ender", 16, 95, 26, 32, 13341924),
        LUMINOUS_REBUKE(8, "luminous_rebuke", "holy", 18, 70, 18, 24, 15721641),
        HALO_SANCTUARY(8, "halo_sanctuary", "holy", 0, 90, 30, 32, 15851424),
        FEATHER_FAN(9, "feather_fan", "nature", 17, 65, 8, 24, 14403247),
        WINGSTEP(9, "wingstep", "nature", 0, 60, 24, 20, 15656150),
        FROST_NEEDLES(10, "frost_needles", "ice", 16, 75, 18, 28, 11328250),
        WHITEOUT_EDICT(10, "whiteout_edict", "ice", 6, 100, 28, 36, 14149887),
        EFFIGY_PROCESSION(0, "effigy_procession", "evocation", 9, 100, 28, 32, 16034015),
        CANDLE_ECLIPSE(1, "candle_eclipse", "blood", 28, 105, 30, 36, 13017343),
        PRISM_BATTERY(2, "prism_battery", "lightning", 12, 105, 26, 32, 6675956),
        BLOOD_VERDICT(3, "blood_verdict", "blood", 30, 110, 30, 36, 15754371),
        CITRUS_NOVA(4, "citrus_nova", "fire", 24, 100, 26, 32, 16765275),
        TIDAL_LOTUS(5, "tidal_lotus", "holy", 0, 110, 32, 32, 6153160),
        MAGNETIC_STORM(6, "magnetic_storm", "lightning", 8, 110, 30, 36, 0x66BBFF),
        NIGHTFALL_BLADES(7, "nightfall_blades", "ender", 14, 100, 28, 32, 11571199),
        SERAPH_DESCENT(8, "seraph_descent", "holy", 26, 105, 28, 36, 16769952),
        WINTER_CLOCK(10, "winter_clock", "ice", 8, 110, 30, 36, 10411775),
        FEATHER_REFUGE(9, "feather_refuge", "nature", 0, 100, 32, 28, 14996650),
        STAR_CROSS(11, "star_cross", "evocation", 26, 95, 24, 36, 15497651),
        ROSE_STARFALL(11, "rose_starfall", "evocation", 16, 110, 30, 40, 16401276);

        public final int variant;
        public final String id;
        public final String school;
        public final int damage;
        public final int mana;
        public final int cooldown;
        public final int castTicks;
        public final int color;

        private Kind(int variant, String id, String school, int damage, int mana, int cooldown, int castTicks, int color) {
            this.variant = variant;
            this.id = id;
            this.school = school;
            this.damage = damage;
            this.mana = mana;
            this.cooldown = cooldown;
            this.castTicks = castTicks;
            this.color = color;
        }

        public boolean support() {
            return this.damage == 0;
        }

        public boolean additional() {
            return this.ordinal() >= EFFIGY_PROCESSION.ordinal() && this.variant != 11;
        }
    }
}

