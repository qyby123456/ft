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
import net.minecraft.core.registries.BuiltInRegistries;

public final class ManorSpellRules {
    public static final float HIT_LIMIT = 50.0F;
    public static final float WINDOW_LIMIT = 50.0F;
    public static final float HARD_LIMIT = 20.0F;
    public static final float ESCALATE_TOTAL = 400.0F;
    public static final int WINDOW_TICKS = 20;
    public static final String STUPOR_EFFECT = "bountifulfares:stupor";
    private static final String K_TOTAL = "ftboss_spell_total";
    private static final String K_WINDOW = "ftboss_spell_window";
    private static final String K_WINDOW_START = "ftboss_spell_window_start";
    private static final float TOTAL_CEILING = 1000000.0F;
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

    public static boolean stupor(MobEffectInstance var0) {
        return var0 != null && stupor(var0.getEffect().value());
    }

    public static boolean stupor(MobEffect var0) {
        if (var0 == null) {
            return false;
        } else {
            ResourceLocation var1 = BuiltInRegistries.MOB_EFFECT.getKey(var0);
            return var1 != null && "bountifulfares:stupor".equals(var1.toString());
        }
    }

    public static float cap(EasterBossEntity var0, DamageSource var1, float var2) {
        if (var0 == null || var1 == null) {
            return var2;
        } else if (!Float.isFinite(var2) || var2 <= 0.0F) {
            return var2;
        } else if (!spellDamage(var1)) {
            return var2;
        } else {
            float var3 = spellLimit(var0);
            float var4 = Math.min(var2, var3);
            float var5 = windowRemaining(var0, var3);
            if (var5 <= 0.0F) {
                return Float.NaN;
            } else {
                var4 = Math.min(var4, var5);
                if (var4 <= 0.0F) {
                    return Float.NaN;
                } else {
                    record(var0, var4);
                    return var4;
                }
            }
        }
    }

    public static float spellLimit(EasterBossEntity var0) {
        return total(var0) > 400.0F ? 20.0F : 50.0F;
    }

    public static float total(EasterBossEntity var0) {
        return var0.getPersistentData().getFloat("ftboss_spell_total");
    }

    public static void reset(EasterBossEntity var0) {
        CompoundTag var1 = var0.getPersistentData();
        var1.remove("ftboss_spell_total");
        var1.remove("ftboss_spell_window");
        var1.remove("ftboss_spell_window_start");
    }

    public static boolean spellDamage(DamageSource var0) {
        if (var0 == null) {
            return false;
        } else if (var0.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return causedByPlayer(var0);
        } else {
            ResourceLocation var1 = typeId(var0);
            if (var1 == null) {
                return false;
            } else {
                for (String var5 : SPELL_NAMESPACES) {
                    if (var5.equals(var1.getNamespace())) {
                        return causedByPlayer(var0);
                    }
                }

                String var7 = var1.getPath().toLowerCase(Locale.ROOT);

                for (String var6 : SPELL_DAMAGE_HINTS) {
                    if (var7.contains(var6)) {
                        return causedByPlayer(var0);
                    }
                }

                return false;
            }
        }
    }

    private static boolean causedByPlayer(DamageSource var0) {
        if (var0.getEntity() instanceof Player) {
            return true;
        } else {
            Entity var1 = var0.getDirectEntity();

            for (int var3 = 0; var1 instanceof Projectile; var3++) {
                Projectile var2 = (Projectile)var1;
                if (var3 >= 8) {
                    break;
                }

                Entity var4 = var2.getOwner();
                if (var4 == null || var4 == var1) {
                    break;
                }

                if (var4 instanceof Player) {
                    return true;
                }

                var1 = var4;
            }

            return false;
        }
    }

    private static ResourceLocation typeId(DamageSource var0) {
        ResourceLocation var1 = typeIdOrNull(var0);
        if (var1 != null) {
            return var1;
        } else {
            String var2 = var0.getMsgId();
            if (var2 == null) {
                return null;
            } else {
                String var3 = var2.toLowerCase(Locale.ROOT);
                return lowercasedDamageType(var3);
            }
        }
    }

    private static ResourceLocation typeIdOrNull(DamageSource var0) {
        Holder var1 = var0.typeHolder();
        return var1 != null && !var1.unwrapKey().isEmpty() ? ((ResourceKey)var1.unwrapKey().get()).location() : null;
    }

    private static ResourceLocation lowercasedDamageType(String var0) {
        for (String var4 : SPELL_DAMAGE_HINTS) {
            if (var0.contains(var4)) {
                return ResourceLocation.tryParse("minecraft:player_spell_" + var4);
            }
        }

        return null;
    }

    private static float windowRemaining(EasterBossEntity var0, float var1) {
        CompoundTag var2 = var0.getPersistentData();
        long var3 = var0.level().getGameTime();
        long var5 = var2.getLong("ftboss_spell_window_start");
        float var7 = var2.getFloat("ftboss_spell_window");
        if (var5 != 0L && var3 >= var5 && var3 - var5 < 20L) {
            return var1 - var7;
        } else {
            var2.putLong("ftboss_spell_window_start", var3);
            var2.putFloat("ftboss_spell_window", 0.0F);
            return var1;
        }
    }

    private static void record(EasterBossEntity var0, float var1) {
        if (Float.isFinite(var1) && !(var1 <= 0.0F)) {
            CompoundTag var2 = var0.getPersistentData();
            var2.putFloat("ftboss_spell_window", Math.min(1000000.0F, var2.getFloat("ftboss_spell_window") + var1));
            var2.putFloat("ftboss_spell_total", Math.min(1000000.0F, var2.getFloat("ftboss_spell_total") + var1));
        }
    }

    public static List<ManorSpellRules.Kind> pair(int variant) {
        return Arrays.stream(ManorSpellRules.Kind.values()).filter(kind -> kind.variant == variant).toList();
    }

    public static int level(int value) {
        return 5;
    }

    public static double extraRadius(ManorSpellRules.Kind kind) {
        return switch (kind) {
            case CANDLE_ECLIPSE, BLOOD_VERDICT, NIGHTFALL_BLADES, WINTER_CLOCK -> 3.5;
            default -> 4.0;
        };
    }

    public static int extraDelay(ManorSpellRules.Kind kind) {
        return switch (kind) {
            case CANDLE_ECLIPSE -> 24;
            case BLOOD_VERDICT, SERAPH_DESCENT -> 20;
            default -> 12;
        };
    }

    public static boolean extraPulse(ManorSpellRules.Kind kind, int age) {
        if (kind.additional() && !kind.support()) {
            int count = switch (kind) {
                case NIGHTFALL_BLADES -> 2;
                case WINTER_CLOCK, EFFIGY_PROCESSION, PRISM_BATTERY -> 3;
                default -> 1;
                case MAGNETIC_STORM -> 4;
            };
            return pulse(age - extraDelay(kind), 20, count);
        } else {
            return false;
        }
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
        FEATHER_FAN(9, "feather_fan", "nature", 17, 65, 16, 24, 14403247),
        WINGSTEP(9, "wingstep", "nature", 0, 60, 24, 20, 15656150),
        FROST_NEEDLES(10, "frost_needles", "ice", 16, 75, 18, 28, 11328250),
        WHITEOUT_EDICT(10, "whiteout_edict", "ice", 6, 100, 28, 36, 14149887),
        EFFIGY_PROCESSION(0, "effigy_procession", "evocation", 9, 100, 28, 32, 16034015),
        CANDLE_ECLIPSE(1, "candle_eclipse", "blood", 28, 105, 30, 36, 13017343),
        PRISM_BATTERY(2, "prism_battery", "lightning", 12, 105, 26, 32, 6675956),
        BLOOD_VERDICT(3, "blood_verdict", "blood", 30, 110, 30, 36, 15754371),
        CITRUS_NOVA(4, "citrus_nova", "fire", 24, 100, 26, 32, 16765275),
        TIDAL_LOTUS(5, "tidal_lotus", "holy", 0, 110, 32, 32, 6153160),
        MAGNETIC_STORM(6, "magnetic_storm", "lightning", 8, 110, 30, 36, 6732799),
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
