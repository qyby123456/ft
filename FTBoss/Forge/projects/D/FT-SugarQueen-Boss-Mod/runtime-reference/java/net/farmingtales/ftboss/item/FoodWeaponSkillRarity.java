/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.item.ItemStack
 */
package net.farmingtales.ftboss.item;

import java.util.Locale;
import net.farmingtales.ftboss.item.FoodWeaponEvolution;
import net.farmingtales.ftboss.item.FoodWeaponProgression;
import net.farmingtales.ftboss.util.FoodWeaponSkillRarityRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public final class FoodWeaponSkillRarity {
    private static final String POWER = "SkillPower";

    private FoodWeaponSkillRarity() {
    }

    public static View preview(ItemStack itemStack) {
        int n = FoodWeaponProgression.quality(itemStack);
        FoodWeaponSkillRarityRules.Range range = FoodWeaponSkillRarityRules.range(n);
        return new View((Component)Component.m_237115_((String)"rarity.ftboss.skill.random"), n, range.minimum(), range.maximum(), 1.0, false);
    }

    public static View stageView(ItemStack itemStack, int n) {
        if (n < 0 || n >= FoodWeaponEvolution.evolutionLevel(itemStack)) {
            return FoodWeaponSkillRarity.legacy();
        }
        CompoundTag compoundTag = itemStack.m_41737_("FTBossFoodEvolution");
        ListTag listTag = compoundTag == null ? new ListTag() : compoundTag.m_128437_("StageHistory", 10);
        return n < listTag.size() ? FoodWeaponSkillRarity.read(listTag.m_128728_(n).m_128469_(POWER)) : FoodWeaponSkillRarity.legacy();
    }

    public static View activeView(ItemStack itemStack) {
        int n = FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack) ? FoodWeaponEvolution.activeGourmetSkillIndex(itemStack) : FoodWeaponProgression.selectedStage(itemStack);
        return FoodWeaponSkillRarity.stageView(itemStack, n);
    }

    public static double passiveMultiplier(ItemStack itemStack, String string) {
        return FoodWeaponSkillRarity.passiveView(itemStack, string).multiplier();
    }

    public static View passiveView(ItemStack itemStack, String string) {
        CompoundTag compoundTag = itemStack.m_41737_("FTBossFoodEvolution");
        if (compoundTag == null || FoodWeaponEvolution.evolutionLevel(itemStack) <= 0) {
            return FoodWeaponSkillRarity.legacy();
        }
        if (FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack)) {
            ListTag listTag = compoundTag.m_128437_("EvolutionSkills", 10);
            for (int i = 0; i < Math.min(8, listTag.size()); ++i) {
                CompoundTag compoundTag2 = listTag.m_128728_(i);
                if (!string.equals(compoundTag2.m_128461_("EvolutionSkill"))) continue;
                return FoodWeaponSkillRarity.read(compoundTag2.m_128469_(POWER));
            }
        } else {
            ListTag listTag = compoundTag.m_128437_("StageHistory", 10);
            for (int i = 0; i < Math.min(FoodWeaponEvolution.evolutionLevel(itemStack), listTag.size()); ++i) {
                ListTag listTag2 = listTag.m_128728_(i).m_128437_("LearnedPowers", 10);
                for (int j = 0; j < Math.min(2, listTag2.size()); ++j) {
                    CompoundTag compoundTag3 = listTag2.m_128728_(j);
                    if (!string.equals(compoundTag3.m_128461_("Skill"))) continue;
                    return FoodWeaponSkillRarity.read(compoundTag3.m_128469_(POWER));
                }
            }
        }
        return FoodWeaponSkillRarity.legacy();
    }

    public static Component passiveDescription(ItemStack itemStack, String string, Component component) {
        View view = FoodWeaponSkillRarity.passiveView(itemStack, string);
        return component.m_6881_().m_7220_((Component)Component.m_237110_((String)"screen.ftboss.rarity.passive_value", (Object[])new Object[]{view.rarity(), String.format(Locale.ROOT, "%.3f", view.multiplier())}));
    }

    static void recordSuccess(ItemStack itemStack, CompoundTag compoundTag, int n, RandomSource randomSource) {
        CompoundTag compoundTag2 = FoodWeaponSkillRarity.draw(n, randomSource);
        compoundTag.m_128365_(POWER, (Tag)compoundTag2);
        if (FoodWeaponEvolution.isGourmetEvolutionWeapon(itemStack)) {
            CompoundTag compoundTag3 = itemStack.m_41698_("FTBossFoodEvolution");
            ListTag listTag = compoundTag3.m_128437_("EvolutionSkills", 10);
            if (!listTag.isEmpty()) {
                listTag.m_128728_(listTag.size() - 1).m_128365_(POWER, (Tag)compoundTag2.m_6426_());
            }
        } else {
            ListTag listTag = compoundTag.m_128437_("Learned", 8);
            ListTag listTag2 = new ListTag();
            for (int i = 0; i < Math.min(2, listTag.size()); ++i) {
                CompoundTag compoundTag4 = new CompoundTag();
                compoundTag4.m_128359_("Skill", listTag.m_128778_(i));
                compoundTag4.m_128365_(POWER, (Tag)FoodWeaponSkillRarity.draw(n, randomSource));
                listTag2.add((Object)compoundTag4);
            }
            compoundTag.m_128365_("LearnedPowers", (Tag)listTag2);
        }
    }

    private static CompoundTag draw(int n, RandomSource randomSource) {
        FoodWeaponSkillRarityRules.Roll roll = FoodWeaponSkillRarityRules.roll(n, randomSource.m_188503_(100), randomSource.m_188500_());
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128405_("Quality", roll.quality());
        compoundTag.m_128359_("Rarity", roll.rarity().id());
        compoundTag.m_128347_("Multiplier", roll.multiplier());
        return compoundTag;
    }

    private static View read(CompoundTag compoundTag) {
        FoodWeaponSkillRarityRules.Rarity rarity;
        if (!compoundTag.m_128425_("Quality", 3) || !compoundTag.m_128425_("Multiplier", 99)) {
            return FoodWeaponSkillRarity.legacy();
        }
        int n = compoundTag.m_128451_("Quality");
        try {
            rarity = FoodWeaponSkillRarityRules.Rarity.valueOf(compoundTag.m_128461_("Rarity").toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException illegalArgumentException) {
            return FoodWeaponSkillRarity.legacy();
        }
        double d = compoundTag.m_128459_("Multiplier");
        if (n < 0 || n > 3 || !Double.isFinite(d)) {
            return FoodWeaponSkillRarity.legacy();
        }
        FoodWeaponSkillRarityRules.Range range = FoodWeaponSkillRarityRules.rarityRange(n, rarity);
        if (d < range.minimum() - 1.0E-9 || d > range.maximum() + 1.0E-9) {
            return FoodWeaponSkillRarity.legacy();
        }
        double d2 = FoodWeaponSkillRarityRules.validate(n, rarity, d);
        if (d2 != d) {
            return FoodWeaponSkillRarity.legacy();
        }
        FoodWeaponSkillRarityRules.Range range2 = FoodWeaponSkillRarityRules.range(n);
        return new View((Component)Component.m_237115_((String)("rarity.ftboss.skill." + rarity.id())), n, range2.minimum(), range2.maximum(), d2, false);
    }

    private static View legacy() {
        return new View((Component)Component.m_237115_((String)"rarity.ftboss.skill.legacy"), -1, 1.0, 1.0, 1.0, true);
    }

    public record View(Component rarity, int quality, double minimum, double maximum, double multiplier, boolean legacy) {
    }
}

