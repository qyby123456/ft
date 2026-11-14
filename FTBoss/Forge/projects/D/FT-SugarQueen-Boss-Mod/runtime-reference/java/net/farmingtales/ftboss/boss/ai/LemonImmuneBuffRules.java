/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraftforge.registries.ForgeRegistries
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

public final class LemonImmuneBuffRules {
    public static final int MAX_RESISTANCE_AMPLIFIER = 3;
    public static final List<String> FORBIDDEN_BUFFS = List.of("kaleidoscope_cookery:satiated_shield", "farmersdelight:nourishment", "farm_and_charm:satiation", "irons_spellbooks:heartstop", "irons_spellbooks:oakskin", "irons_spellbooks:black_hole", "irons_spellbooks:antigravity", "apotheosis:invulnerability", "apotheosis:damage_immunity");
    public static final List<String> FORBIDDEN_NAME_FRAGMENTS = List.of("invulnerab", "invincib", "damage_immunity", "heartstop");

    private LemonImmuneBuffRules() {
    }

    public static boolean isForbidden(MobEffect mobEffect) {
        if (mobEffect == null) {
            return false;
        }
        ResourceLocation resourceLocation = ForgeRegistries.MOB_EFFECTS.getKey((Object)mobEffect);
        if (resourceLocation == null) {
            return false;
        }
        String string = resourceLocation.toString();
        if (FORBIDDEN_BUFFS.contains(string)) {
            return true;
        }
        String string2 = resourceLocation.m_135815_();
        for (String string3 : FORBIDDEN_NAME_FRAGMENTS) {
            if (!string2.contains(string3)) continue;
            return true;
        }
        return false;
    }

    public static boolean tooMuchResistance(MobEffectInstance mobEffectInstance) {
        return mobEffectInstance != null && mobEffectInstance.m_19544_() == MobEffects.f_19606_ && mobEffectInstance.m_19564_() >= 3;
    }

    public static boolean shouldPurge(MobEffectInstance mobEffectInstance) {
        if (mobEffectInstance == null) {
            return false;
        }
        MobEffect mobEffect = mobEffectInstance.m_19544_();
        if (!mobEffect.m_19486_()) {
            return false;
        }
        return LemonImmuneBuffRules.isForbidden(mobEffect) || LemonImmuneBuffRules.tooMuchResistance(mobEffectInstance);
    }

    public static int purge(LivingEntity livingEntity) {
        if (livingEntity == null || livingEntity.m_9236_().f_46443_ || livingEntity.m_21220_().isEmpty()) {
            return 0;
        }
        ArrayList<MobEffectInstance> arrayList = new ArrayList<MobEffectInstance>();
        for (Object object : livingEntity.m_21220_()) {
            if (!LemonImmuneBuffRules.shouldPurge((MobEffectInstance)object)) continue;
            arrayList.add((MobEffectInstance)object);
        }
        if (arrayList.isEmpty()) {
            return 0;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (MobEffectInstance mobEffectInstance : arrayList) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append('/');
            }
            stringBuilder.append(mobEffectInstance.m_19544_().m_19482_().getString());
            if (mobEffectInstance.m_19564_() > 0) {
                stringBuilder.append(' ').append(LemonImmuneBuffRules.roman(mobEffectInstance.m_19564_() + 1));
            }
            livingEntity.m_21195_(mobEffectInstance.m_19544_());
        }
        FTBossMod.LOGGER.info("[FTBoss][Lemon] buff \u68c0\u6d4b: \u6e05\u6389 {} \u6761\u4f1a\u8ba9\u67e0\u6aac\u732b\u6253\u4e0d\u6b7b\u7684\u589e\u76ca [{}]", (Object)arrayList.size(), (Object)stringBuilder);
        return arrayList.size();
    }

    private static String roman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            default -> String.valueOf(n);
        };
    }
}

