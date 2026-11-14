package net.farmingtales.ftboss.item;

import net.farmingtales.ftboss.util.FoodWeaponSkillRarityRules;
import net.farmingtales.ftboss.util.FoodWeaponSkillRarityRules.Rarity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

/** 进化技能品质存档；只在成功进化时抽样，旧记录保持一倍，2026-10-13 新增独立损伤倍率。 */
public final class FoodWeaponSkillRarity {
    private static final String POWER = "SkillPower";

    public record View(Component rarity, int quality, double minimum, double maximum, double multiplier, boolean legacy) {}

    private FoodWeaponSkillRarity() {}

    public static View preview(ItemStack offering) {
        int quality = FoodWeaponProgression.quality(offering);
        var range = FoodWeaponSkillRarityRules.range(quality);
        return new View(Component.translatable("rarity.ftboss.skill.random"), quality, range.minimum(), range.maximum(), 1, false);
    }

    public static View stageView(ItemStack weapon, int index) {
        if (index < 0 || index >= FoodWeaponEvolution.evolutionLevel(weapon)) return legacy();
        CompoundTag data = weapon.getTagElement(FoodWeaponProgression.ROOT);
        ListTag history = data == null ? new ListTag() : data.getList("StageHistory", Tag.TAG_COMPOUND);
        return index < history.size() ? read(history.getCompound(index).getCompound(POWER)) : legacy();
    }

    public static View activeView(ItemStack weapon) {
        int index = FoodWeaponEvolution.isGourmetEvolutionWeapon(weapon)
                ? FoodWeaponEvolution.activeGourmetSkillIndex(weapon) : FoodWeaponProgression.selectedStage(weapon);
        return stageView(weapon, index);
    }

    public static double passiveMultiplier(ItemStack weapon, String skill) {
        return passiveView(weapon, skill).multiplier();
    }

    public static View passiveView(ItemStack weapon, String skill) {
        CompoundTag data = weapon.getTagElement(FoodWeaponProgression.ROOT);
        if (data == null || FoodWeaponEvolution.evolutionLevel(weapon) <= 0) return legacy();
        if (FoodWeaponEvolution.isGourmetEvolutionWeapon(weapon)) {
            ListTag acquired = data.getList("EvolutionSkills", Tag.TAG_COMPOUND);
            for (int index = 0; index < Math.min(8, acquired.size()); index++) {
                CompoundTag entry = acquired.getCompound(index);
                if (skill.equals(entry.getString("EvolutionSkill"))) return read(entry.getCompound(POWER));
            }
        } else {
            ListTag history = data.getList("StageHistory", Tag.TAG_COMPOUND);
            for (int index = 0; index < Math.min(FoodWeaponEvolution.evolutionLevel(weapon), history.size()); index++) {
                ListTag powers = history.getCompound(index).getList("LearnedPowers", Tag.TAG_COMPOUND);
                for (int learned = 0; learned < Math.min(2, powers.size()); learned++) {
                    CompoundTag entry = powers.getCompound(learned);
                    if (skill.equals(entry.getString("Skill"))) return read(entry.getCompound(POWER));
                }
            }
        }
        return legacy();
    }

    public static Component passiveDescription(ItemStack weapon, String skill, Component description) {
        View power = passiveView(weapon, skill);
        return description.copy().append(Component.translatable("screen.ftboss.rarity.passive_value", power.rarity(),
                String.format(java.util.Locale.ROOT, "%.3f", power.multiplier())));
    }

    static void recordSuccess(ItemStack weapon, CompoundTag record, int quality, RandomSource random) {
        CompoundTag power = draw(quality, random);
        record.put(POWER, power);
        if (FoodWeaponEvolution.isGourmetEvolutionWeapon(weapon)) {
            CompoundTag data = weapon.getOrCreateTagElement(FoodWeaponProgression.ROOT);
            ListTag acquired = data.getList("EvolutionSkills", Tag.TAG_COMPOUND);
            if (!acquired.isEmpty()) acquired.getCompound(acquired.size() - 1).put(POWER, power.copy());
        } else {
            ListTag learned = record.getList("Learned", Tag.TAG_STRING);
            ListTag powers = new ListTag();
            for (int index = 0; index < Math.min(2, learned.size()); index++) {
                CompoundTag entry = new CompoundTag();
                entry.putString("Skill", learned.getString(index));
                entry.put(POWER, draw(quality, random));
                powers.add(entry);
            }
            record.put("LearnedPowers", powers);
        }
    }

    private static CompoundTag draw(int quality, RandomSource random) {
        var roll = FoodWeaponSkillRarityRules.roll(quality, random.nextInt(100), random.nextDouble());
        CompoundTag result = new CompoundTag();
        result.putInt("Quality", roll.quality());
        result.putString("Rarity", roll.rarity().id());
        result.putDouble("Multiplier", roll.multiplier());
        return result;
    }

    private static View read(CompoundTag power) {
        if (!power.contains("Quality", Tag.TAG_INT) || !power.contains("Multiplier", Tag.TAG_ANY_NUMERIC)) return legacy();
        int quality = power.getInt("Quality");
        Rarity rarity;
        try { rarity = Rarity.valueOf(power.getString("Rarity").toUpperCase(java.util.Locale.ROOT)); }
        catch (IllegalArgumentException invalid) { return legacy(); }
        double raw = power.getDouble("Multiplier");
        if (quality < 0 || quality > 3 || !Double.isFinite(raw)) return legacy();
        var band = FoodWeaponSkillRarityRules.rarityRange(quality, rarity);
        if (raw < band.minimum() - 1.0E-9 || raw > band.maximum() + 1.0E-9) return legacy();
        double value = FoodWeaponSkillRarityRules.validate(quality, rarity, raw);
        if (value != raw) return legacy();
        var range = FoodWeaponSkillRarityRules.range(quality);
        return new View(Component.translatable("rarity.ftboss.skill." + rarity.id()), quality, range.minimum(), range.maximum(), value, false);
    }

    private static View legacy() {
        return new View(Component.translatable("rarity.ftboss.skill.legacy"), -1, 1, 1, 1, true);
    }
}
