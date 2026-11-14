package net.farmingtales.ftboss.item;

import java.util.Locale;
import net.farmingtales.ftboss.util.FoodWeaponSkillRarityRules;
import net.farmingtales.ftboss.util.FoodWeaponSkillRarityRules.Range;
import net.farmingtales.ftboss.util.FoodWeaponSkillRarityRules.Rarity;
import net.farmingtales.ftboss.util.FoodWeaponSkillRarityRules.Roll;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public final class FoodWeaponSkillRarity {
    private static final String POWER = "SkillPower";

    private FoodWeaponSkillRarity() {
    }

    public static FoodWeaponSkillRarity.View preview(ItemStack var0) {
        int var1 = FoodWeaponProgression.quality(var0);
        Range var2 = FoodWeaponSkillRarityRules.range(var1);
        return new FoodWeaponSkillRarity.View(Component.translatable("rarity.ftboss.skill.random"), var1, var2.minimum(), var2.maximum(), 1.0, false);
    }

    public static FoodWeaponSkillRarity.View stageView(ItemStack var0, int var1) {
        if (var1 >= 0 && var1 < FoodWeaponEvolution.evolutionLevel(var0)) {
            CompoundTag var2 = FoodWeaponData.read(var0, "FTBossFoodEvolution");
            ListTag var3 = var2 == null ? new ListTag() : var2.getList("StageHistory", 10);
            return var1 < var3.size() ? read(var3.getCompound(var1).getCompound("SkillPower")) : legacy();
        } else {
            return legacy();
        }
    }

    public static FoodWeaponSkillRarity.View activeView(ItemStack var0) {
        int var1 = FoodWeaponEvolution.isGourmetEvolutionWeapon(var0)
            ? FoodWeaponEvolution.activeGourmetSkillIndex(var0)
            : FoodWeaponProgression.selectedStage(var0);
        return stageView(var0, var1);
    }

    public static double passiveMultiplier(ItemStack var0, String var1) {
        return passiveView(var0, var1).multiplier();
    }

    public static FoodWeaponSkillRarity.View passiveView(ItemStack var0, String var1) {
        CompoundTag var2 = FoodWeaponData.read(var0, "FTBossFoodEvolution");
        if (var2 != null && FoodWeaponEvolution.evolutionLevel(var0) > 0) {
            if (FoodWeaponEvolution.isGourmetEvolutionWeapon(var0)) {
                ListTag var3 = var2.getList("EvolutionSkills", 10);

                for (int var4 = 0; var4 < Math.min(8, var3.size()); var4++) {
                    CompoundTag var5 = var3.getCompound(var4);
                    if (var1.equals(var5.getString("EvolutionSkill"))) {
                        return read(var5.getCompound("SkillPower"));
                    }
                }
            } else {
                ListTag var8 = var2.getList("StageHistory", 10);

                for (int var9 = 0; var9 < Math.min(FoodWeaponEvolution.evolutionLevel(var0), var8.size()); var9++) {
                    ListTag var10 = var8.getCompound(var9).getList("LearnedPowers", 10);

                    for (int var6 = 0; var6 < Math.min(2, var10.size()); var6++) {
                        CompoundTag var7 = var10.getCompound(var6);
                        if (var1.equals(var7.getString("Skill"))) {
                            return read(var7.getCompound("SkillPower"));
                        }
                    }
                }
            }

            return legacy();
        } else {
            return legacy();
        }
    }

    public static Component passiveDescription(ItemStack var0, String var1, Component var2) {
        FoodWeaponSkillRarity.View var3 = passiveView(var0, var1);
        return var2.copy()
            .append(Component.translatable("screen.ftboss.rarity.passive_value", var3.rarity(), String.format(Locale.ROOT, "%.3f", var3.multiplier())));
    }

    static void recordSuccess(ItemStack var0, CompoundTag var1, int var2, RandomSource var3) {
        CompoundTag var4 = draw(var2, var3);
        var1.put("SkillPower", var4);
        if (FoodWeaponEvolution.isGourmetEvolutionWeapon(var0)) {
            FoodWeaponData.edit(var0, "FTBossFoodEvolution", evolutionData -> {
                ListTag skills = evolutionData.getList("EvolutionSkills", 10);
                if (!skills.isEmpty()) {
                    skills.getCompound(skills.size() - 1).put("SkillPower", var4.copy());
                }
            });
        } else {
            ListTag var9 = var1.getList("Learned", 8);
            ListTag var10 = new ListTag();

            for (int var7 = 0; var7 < Math.min(2, var9.size()); var7++) {
                CompoundTag var8 = new CompoundTag();
                var8.putString("Skill", var9.getString(var7));
                var8.put("SkillPower", draw(var2, var3));
                var10.add(var8);
            }

            var1.put("LearnedPowers", var10);
        }
    }

    private static CompoundTag draw(int var0, RandomSource var1) {
        Roll var2 = FoodWeaponSkillRarityRules.roll(var0, var1.nextInt(100), var1.nextDouble());
        CompoundTag var3 = new CompoundTag();
        var3.putInt("Quality", var2.quality());
        var3.putString("Rarity", var2.rarity().id());
        var3.putDouble("Multiplier", var2.multiplier());
        return var3;
    }

    private static FoodWeaponSkillRarity.View read(CompoundTag var0) {
        if (var0.contains("Quality", 3) && var0.contains("Multiplier", 99)) {
            int var1 = var0.getInt("Quality");

            Rarity var2;
            try {
                var2 = Rarity.valueOf(var0.getString("Rarity").toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException var9) {
                return legacy();
            }

            double var3 = var0.getDouble("Multiplier");
            if (var1 >= 0 && var1 <= 3 && Double.isFinite(var3)) {
                Range var5 = FoodWeaponSkillRarityRules.rarityRange(var1, var2);
                if (!(var3 < var5.minimum() - 1.0E-9) && !(var3 > var5.maximum() + 1.0E-9)) {
                    double var6 = FoodWeaponSkillRarityRules.validate(var1, var2, var3);
                    if (var6 != var3) {
                        return legacy();
                    } else {
                        Range var8 = FoodWeaponSkillRarityRules.range(var1);
                        return new FoodWeaponSkillRarity.View(
                            Component.translatable("rarity.ftboss.skill." + var2.id()), var1, var8.minimum(), var8.maximum(), var6, false
                        );
                    }
                } else {
                    return legacy();
                }
            } else {
                return legacy();
            }
        } else {
            return legacy();
        }
    }

    private static FoodWeaponSkillRarity.View legacy() {
        return new FoodWeaponSkillRarity.View(Component.translatable("rarity.ftboss.skill.legacy"), -1, 1.0, 1.0, 1.0, true);
    }

    public static record View(Component rarity, int quality, double minimum, double maximum, double multiplier, boolean legacy) {
    }

    public static FoodWeaponSkillRarity.View ftboss$legacyOrdinary$preview(ItemStack var0) {
            int var1 = FoodWeaponProgression.ftboss$legacyOrdinary$quality(var0);
            Range var2 = FoodWeaponSkillRarityRules.range(var1);
            return new FoodWeaponSkillRarity.View(Component.translatable("rarity.ftboss.skill.random"), var1, var2.minimum(), var2.maximum(), 1.0, false);
        }

    public static FoodWeaponSkillRarity.View ftboss$legacyOrdinary$stageView(ItemStack var0, int var1) {
            if (var1 >= 0 && var1 < FoodWeaponEvolution.ftboss$legacyOrdinary$evolutionLevel(var0)) {
                CompoundTag var2 = FoodWeaponData.read(var0, "FTBossFoodEvolution");
                ListTag var3 = var2 == null ? new ListTag() : var2.getList("StageHistory", 10);
                return var1 < var3.size() ? ftboss$legacyOrdinary$read(var3.getCompound(var1).getCompound("SkillPower")) : ftboss$legacyOrdinary$legacy();
            } else {
                return ftboss$legacyOrdinary$legacy();
            }
        }

    public static FoodWeaponSkillRarity.View ftboss$legacyOrdinary$activeView(ItemStack var0) {
            int var1 = FoodWeaponEvolution.ftboss$legacyOrdinary$isGourmetEvolutionWeapon(var0)
                ? FoodWeaponEvolution.ftboss$legacyOrdinary$activeGourmetSkillIndex(var0)
                : FoodWeaponProgression.ftboss$legacyOrdinary$selectedStage(var0);
            return ftboss$legacyOrdinary$stageView(var0, var1);
        }

    public static double ftboss$legacyOrdinary$passiveMultiplier(ItemStack var0, String var1) {
            return ftboss$legacyOrdinary$passiveView(var0, var1).multiplier();
        }

    public static FoodWeaponSkillRarity.View ftboss$legacyOrdinary$passiveView(ItemStack var0, String var1) {
            CompoundTag var2 = FoodWeaponData.read(var0, "FTBossFoodEvolution");
            if (var2 != null && FoodWeaponEvolution.ftboss$legacyOrdinary$evolutionLevel(var0) > 0) {
                if (FoodWeaponEvolution.ftboss$legacyOrdinary$isGourmetEvolutionWeapon(var0)) {
                    ListTag var3 = var2.getList("EvolutionSkills", 10);
    
                    for (int var4 = 0; var4 < Math.min(8, var3.size()); var4++) {
                        CompoundTag var5 = var3.getCompound(var4);
                        if (var1.equals(var5.getString("EvolutionSkill"))) {
                            return ftboss$legacyOrdinary$read(var5.getCompound("SkillPower"));
                        }
                    }
                } else {
                    ListTag var8 = var2.getList("StageHistory", 10);
    
                    for (int var9 = 0; var9 < Math.min(FoodWeaponEvolution.ftboss$legacyOrdinary$evolutionLevel(var0), var8.size()); var9++) {
                        ListTag var10 = var8.getCompound(var9).getList("LearnedPowers", 10);
    
                        for (int var6 = 0; var6 < Math.min(2, var10.size()); var6++) {
                            CompoundTag var7 = var10.getCompound(var6);
                            if (var1.equals(var7.getString("Skill"))) {
                                return ftboss$legacyOrdinary$read(var7.getCompound("SkillPower"));
                            }
                        }
                    }
                }
    
                return ftboss$legacyOrdinary$legacy();
            } else {
                return ftboss$legacyOrdinary$legacy();
            }
        }

    public static Component ftboss$legacyOrdinary$passiveDescription(ItemStack var0, String var1, Component var2) {
            FoodWeaponSkillRarity.View var3 = ftboss$legacyOrdinary$passiveView(var0, var1);
            return var2.copy()
                .append(Component.translatable("screen.ftboss.rarity.passive_value", var3.rarity(), String.format(Locale.ROOT, "%.3f", var3.multiplier())));
        }

    static void ftboss$legacyOrdinary$recordSuccess(ItemStack var0, CompoundTag var1, int var2, RandomSource var3) {
            CompoundTag var4 = ftboss$legacyOrdinary$draw(var2, var3);
            var1.put("SkillPower", var4);
            if (FoodWeaponEvolution.ftboss$legacyOrdinary$isGourmetEvolutionWeapon(var0)) {
                FoodWeaponData.edit(var0, "FTBossFoodEvolution", evolutionData -> {
                    ListTag skills = evolutionData.getList("EvolutionSkills", 10);
                    if (!skills.isEmpty()) {
                        skills.getCompound(skills.size() - 1).put("SkillPower", var4.copy());
                    }
                });
            } else {
                ListTag var9 = var1.getList("Learned", 8);
                ListTag var10 = new ListTag();
    
                for (int var7 = 0; var7 < Math.min(2, var9.size()); var7++) {
                    CompoundTag var8 = new CompoundTag();
                    var8.putString("Skill", var9.getString(var7));
                    var8.put("SkillPower", ftboss$legacyOrdinary$draw(var2, var3));
                    var10.add(var8);
                }
    
                var1.put("LearnedPowers", var10);
            }
        }

    private static CompoundTag ftboss$legacyOrdinary$draw(int var0, RandomSource var1) {
            Roll var2 = FoodWeaponSkillRarityRules.roll(var0, var1.nextInt(100), var1.nextDouble());
            CompoundTag var3 = new CompoundTag();
            var3.putInt("Quality", var2.quality());
            var3.putString("Rarity", var2.rarity().id());
            var3.putDouble("Multiplier", var2.multiplier());
            return var3;
        }

    private static FoodWeaponSkillRarity.View ftboss$legacyOrdinary$read(CompoundTag var0) {
            if (var0.contains("Quality", 3) && var0.contains("Multiplier", 99)) {
                int var1 = var0.getInt("Quality");
    
                Rarity var2;
                try {
                    var2 = Rarity.valueOf(var0.getString("Rarity").toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException var9) {
                    return ftboss$legacyOrdinary$legacy();
                }
    
                double var3 = var0.getDouble("Multiplier");
                if (var1 >= 0 && var1 <= 3 && Double.isFinite(var3)) {
                    Range var5 = FoodWeaponSkillRarityRules.rarityRange(var1, var2);
                    if (!(var3 < var5.minimum() - 1.0E-9) && !(var3 > var5.maximum() + 1.0E-9)) {
                        double var6 = FoodWeaponSkillRarityRules.validate(var1, var2, var3);
                        if (var6 != var3) {
                            return ftboss$legacyOrdinary$legacy();
                        } else {
                            Range var8 = FoodWeaponSkillRarityRules.range(var1);
                            return new FoodWeaponSkillRarity.View(
                                Component.translatable("rarity.ftboss.skill." + var2.id()), var1, var8.minimum(), var8.maximum(), var6, false
                            );
                        }
                    } else {
                        return ftboss$legacyOrdinary$legacy();
                    }
                } else {
                    return ftboss$legacyOrdinary$legacy();
                }
            } else {
                return ftboss$legacyOrdinary$legacy();
            }
        }

    private static FoodWeaponSkillRarity.View ftboss$legacyOrdinary$legacy() {
            return new FoodWeaponSkillRarity.View(Component.translatable("rarity.ftboss.skill.legacy"), -1, 1.0, 1.0, 1.0, true);
        }
}
