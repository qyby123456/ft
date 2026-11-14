package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.EvolutionSkill;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.Profile;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.WeaponDetailView;
import net.farmingtales.ftboss.util.FoodWeaponBuildRules;
import net.farmingtales.ftboss.util.WeaponEvolutionSkillPool;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

/** 四阶段进化与品质献祭；每次消耗一食物并保存回退记录，2026-10-12 新增失败退级和旧存档迁移。 */
public final class FoodWeaponProgression {
    public static final String ROOT = "FTBossFoodEvolution";
    private static final String VERSION = "ProgressionVersion";
    private static final String HISTORY = "StageHistory";
    private static final String SELECTED = "SelectedStage";
    private static final List<String> PROGRESSION_KEYS = List.of("Level", "EvolutionSkill", "SecondProfile",
            "ActiveProfile", "ActiveSkillIndex", "EvolutionSkills", "EvolutionStats", "BlessingWashed", SELECTED);

    private FoodWeaponProgression() {}

    public static int experience(ItemStack stack, int raw) {
        CompoundTag data = stack.getTagElement(ROOT);
        if (data == null || data.getInt(VERSION) >= 2) return Math.max(0, raw);
        int level = FoodWeaponEvolution.evolutionLevel(stack);
        int floor = level <= 0 ? 0 : FoodWeaponBuildRules.evolutionCost(level - 1);
        return Math.max(0, raw - floor);
    }

    static void normalizeExperience(ItemStack stack) {
        int value = FoodWeaponEvolution.evolutionExperience(stack);
        CompoundTag data = stack.getOrCreateTagElement(ROOT);
        data.putInt("Experience", value);
        data.putInt(VERSION, 2);
    }

    public static int quality(ItemStack food) {
        CompoundTag tag = food.getTag();
        if (tag != null && tag.contains("quality_food", Tag.TAG_COMPOUND)
                && tag.getCompound("quality_food").contains("quality", Tag.TAG_ANY_NUMERIC)) {
            return Math.max(0, Math.min(3, tag.getCompound("quality_food").getInt("quality")));
        }
        return Math.max(0, FoodWeaponEvolution.foodQualityTier(food) - 1);
    }

    public static Component qualityName(ItemStack food) {
        return Component.translatable("quality.ftboss.evolution." + quality(food));
    }

    public static boolean isOffering(ItemStack weapon, ItemStack food, int tint) {
        return !food.isEmpty() && food != weapon && FoodWeaponEvolution.isEvolutionCandidate(weapon)
                && !FoodWeaponEvolution.isEvolutionCandidate(food) && FoodWeaponEvolution.matchesStatueFood(food, tint);
    }

    public static int chance(ItemStack weapon, ItemStack food) {
        return FoodWeaponBuildRules.successPercent(FoodWeaponEvolution.evolutionLevel(weapon), quality(food),
                FoodWeaponEvolution.isGourmetBlankBlade(weapon));
    }

    public static boolean evolve(ServerPlayer player, int tint, int slot) {
        if (player.isSpectator() || !player.isAlive() || slot < 0 || slot >= 36) return false;
        ItemStack weapon = FoodWeaponEvolution.heldEvolutionWeapon(player);
        ItemStack offering = player.getInventory().getItem(slot);
        Profile profile = Profile.byTint(tint);
        if (profile == null || weapon.isEmpty() || FoodWeaponLifecycle.isBroken(weapon)) return false;
        int level = FoodWeaponEvolution.evolutionLevel(weapon);
        if (level >= FoodWeaponEvolution.maxEvolutionLevel(weapon)) {
            player.displayClientMessage(Component.translatable("message.ftboss.evolution.maxed"), true);
            return false;
        }
        if (!isOffering(weapon, offering, tint)) {
            player.displayClientMessage(Component.translatable("message.ftboss.evolution.offering_required"), true);
            return false;
        }
        int xp = FoodWeaponEvolution.evolutionExperience(weapon);
        int need = FoodWeaponEvolution.nextEvolutionRequirement(weapon);
        if (xp < need) {
            player.displayClientMessage(Component.translatable("message.ftboss.evolution.need_xp", xp, need), true);
            return false;
        }
        if (!validHistory(weapon)) {
            player.displayClientMessage(Component.translatable("message.ftboss.evolution.invalid_history"), true);
            return false;
        }
        int chance = chance(weapon, offering);
        int quality = quality(offering);
        int roll = player.getRandom().nextInt(100);
        boolean success = FoodWeaponBuildRules.succeeds(roll, chance);
        normalizeExperience(weapon);
        CompoundTag data = weapon.getOrCreateTagElement(ROOT);
        if (!data.contains("Profile", Tag.TAG_STRING)) data.putString("Profile", FoodWeaponEvolution.profileId(tint));
        ensureHistory(weapon);
        FoodWeaponLifecycle.ensureTracked(weapon);
        CompoundTag previous = snapshot(data);
        if (!player.getAbilities().instabuild) offering.shrink(1);
        if (success) {
            FoodWeaponEvolution.completeEvolution(player, profile, weapon);
            ListTag history = data.getList(HISTORY, Tag.TAG_COMPOUND);
            CompoundTag record = new CompoundTag();
            record.putString("Statue", FoodWeaponEvolution.profileId(tint));
            record.put("Previous", previous);
            ListTag learned = new ListTag();
            if (!FoodWeaponEvolution.isGourmetEvolutionWeapon(weapon)) {
                var owned = new LinkedHashSet<>(passiveSkills(weapon));
                for (int index = 0; index < 2; index++) {
                    EvolutionSkill skill = FoodWeaponEvolution.drawStageSkill(profile, owned, player.getRandom());
                    if (skill == null) break;
                    owned.add(skill);
                    learned.add(net.minecraft.nbt.StringTag.valueOf(FoodWeaponEvolution.skillId(skill)));
                }
            }
            record.put("Learned", learned);
            FoodWeaponSkillRarity.recordSuccess(weapon, record, quality, player.getRandom());
            history.add(record);
            data.put(HISTORY, history);
            data.putInt(SELECTED, level);
            FoodWeaponLifecycle.markEvolved(weapon);
            player.sendSystemMessage(Component.translatable("message.ftboss.evolution.success_chance", chance)
                    .withStyle(ChatFormatting.GREEN));
        } else {
            rollback(weapon);
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND,
                    SoundSource.PLAYERS, 0.6F, 0.85F);
            player.sendSystemMessage(Component.translatable("message.ftboss.evolution.failed", chance,
                    level, FoodWeaponEvolution.evolutionLevel(weapon)).withStyle(ChatFormatting.RED));
        }
        data.putInt("Experience", 0);
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        net.farmingtales.ftboss.FTBossMod.LOGGER.info("[FTBoss][食武进化献祭] {} level={} result={} roll={} chance={} newLevel={}",
                player.getName().getString(), level, success, roll, chance, FoodWeaponEvolution.evolutionLevel(weapon));
        return true;
    }

    private static CompoundTag snapshot(CompoundTag data) {
        CompoundTag result = new CompoundTag();
        for (String key : PROGRESSION_KEYS) if (data.contains(key)) result.put(key, data.get(key).copy());
        return result;
    }

    private static boolean validHistory(ItemStack stack) {
        CompoundTag data = stack.getTagElement(ROOT);
        if (data == null || !data.contains(HISTORY)) return true;
        Tag stored = data.get(HISTORY);
        if (!(stored instanceof ListTag raw)) return false;
        if (!raw.isEmpty() && raw.getElementType() != Tag.TAG_COMPOUND) return false;
        ListTag history = data.getList(HISTORY, Tag.TAG_COMPOUND);
        if (history.isEmpty()) return true;
        if (history.size() != FoodWeaponEvolution.evolutionLevel(stack)) return false;
        for (int index = 0; index < history.size(); index++) {
            CompoundTag record = history.getCompound(index);
            if (!record.contains("Previous", Tag.TAG_COMPOUND) || record.getCompound("Previous").getInt("Level") != index
                    || Profile.byId(record.getString("Statue")) == null) return false;
        }
        return true;
    }

    static void ensureHistory(ItemStack stack) {
        CompoundTag data = stack.getOrCreateTagElement(ROOT);
        int level = FoodWeaponEvolution.evolutionLevel(stack);
        ListTag history = data.getList(HISTORY, Tag.TAG_COMPOUND);
        if (history.size() == level) return;
        if (!history.isEmpty()) throw new IllegalStateException("evolution history does not match level");
        ListTag gourmetSkills = data.getList("EvolutionSkills", Tag.TAG_COMPOUND);
        for (int index = 0; index < level; index++) {
            CompoundTag previous = snapshot(data);
            previous.putInt("Level", index);
            if (index < 2) previous.remove("SecondProfile");
            if (index == 0) {
                previous.remove("EvolutionSkill");
                previous.remove("ActiveProfile");
                previous.remove("ActiveSkillIndex");
            }
            ListTag shortened = new ListTag();
            for (int skillIndex = 0; skillIndex < Math.min(index, gourmetSkills.size()); skillIndex++) {
                shortened.add(gourmetSkills.get(skillIndex).copy());
            }
            previous.put("EvolutionSkills", shortened);
            CompoundTag stats = data.getCompound("EvolutionStats").copy();
            for (String key : stats.getAllKeys()) stats.putDouble(key, stats.getDouble(key) * index / Math.max(1, level));
            previous.put("EvolutionStats", stats);
            previous.putInt("ActiveSkillIndex", Math.max(0, index - 1));
            previous.putInt(SELECTED, Math.max(0, index - 1));
            String profile = index < gourmetSkills.size() ? gourmetSkills.getCompound(index).getString("Profile")
                    : index == 0 ? data.getString("Profile") : data.getString("SecondProfile");
            if (Profile.byId(profile) == null) profile = FoodWeaponEvolution.firstProfileId(stack);
            CompoundTag record = new CompoundTag();
            record.putString("Statue", profile);
            record.putBoolean("Legacy", true);
            record.put("Previous", previous);
            history.add(record);
        }
        data.put(HISTORY, history);
    }

    static void rollback(ItemStack stack) {
        if (FoodWeaponEvolution.evolutionLevel(stack) > 0) FoodWeaponRepair.invalidateForProgression(stack);
        CompoundTag data = stack.getOrCreateTagElement(ROOT);
        ListTag history = data.getList(HISTORY, Tag.TAG_COMPOUND);
        if (history.isEmpty()) {
            data.putInt("Level", 0);
            return;
        }
        CompoundTag previous = history.getCompound(history.size() - 1).getCompound("Previous");
        history.remove(history.size() - 1);
        for (String key : PROGRESSION_KEYS) {
            data.remove(key);
            if (previous.contains(key)) data.put(key, previous.get(key).copy());
        }
        data.put(HISTORY, history);
    }

    public static int selectedStage(ItemStack stack) {
        int level = FoodWeaponEvolution.evolutionLevel(stack);
        if (level == 0) return -1;
        CompoundTag data = stack.getTagElement(ROOT);
        int selected = data != null && data.contains(SELECTED) ? data.getInt(SELECTED) : level - 1;
        return Math.max(0, Math.min(level - 1, selected));
    }

    public static int activeStage(ItemStack stack) {
        int level = FoodWeaponEvolution.evolutionLevel(stack);
        if (level <= 0) return 0;
        if (FoodWeaponEvolution.isGourmetEvolutionWeapon(stack)) return 1;
        return selectedStage(stack) == 0 ? 1 : 2;
    }

    public static String secondProfile(ItemStack stack, String legacy) {
        if (selectedStage(stack) <= 0) return "";
        CompoundTag data = stack.getTagElement(ROOT);
        ListTag history = data == null ? new ListTag() : data.getList(HISTORY, Tag.TAG_COMPOUND);
        int index = selectedStage(stack);
        return index < history.size() ? history.getCompound(index).getString("Statue") : legacy;
    }

    public static boolean select(ServerPlayer player, int index) {
        ItemStack stack = FoodWeaponEvolution.heldEvolutionWeapon(player);
        if (!player.isAlive() || player.isSpectator() || stack.isEmpty() || index < 0
                || index >= FoodWeaponEvolution.evolutionLevel(stack)) return false;
        if (!validHistory(stack)) {
            player.displayClientMessage(Component.translatable("message.ftboss.evolution.invalid_history"), true);
            return false;
        }
        ensureHistory(stack);
        stack.getOrCreateTagElement(ROOT).putInt(SELECTED, index);
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        return true;
    }

    public static List<EvolutionSkill> passiveSkills(ItemStack stack) {
        var result = new LinkedHashSet<EvolutionSkill>();
        CompoundTag data = stack.getTagElement(ROOT);
        if (data == null || FoodWeaponEvolution.evolutionLevel(stack) == 0) return List.of();
        EvolutionSkill legacy = EvolutionSkill.byId(data.getString("EvolutionSkill"));
        if (legacy != null) result.add(legacy);
        ListTag history = data.getList(HISTORY, Tag.TAG_COMPOUND);
        for (int stage = 0; stage < Math.min(FoodWeaponEvolution.evolutionLevel(stack), history.size()); stage++) {
            ListTag learned = history.getCompound(stage).getList("Learned", Tag.TAG_STRING);
            for (int index = 0; index < Math.min(2, learned.size()); index++) {
                EvolutionSkill skill = EvolutionSkill.byId(learned.getString(index));
                if (skill != null) result.add(skill);
            }
        }
        return List.copyOf(result);
    }

    public static List<WeaponDetailView> details(ItemStack stack) {
        if (FoodWeaponEvolution.isGourmetEvolutionWeapon(stack)) return List.of();
        List<WeaponDetailView> result = new ArrayList<>();
        int level = FoodWeaponEvolution.evolutionLevel(stack);
        CompoundTag data = stack.getTagElement(ROOT);
        ListTag history = data == null ? new ListTag() : data.getList(HISTORY, Tag.TAG_COMPOUND);
        var first = WeaponEvolutionSkillPool.Aspect.fromId(FoodWeaponEvolution.firstProfileId(stack));
        if (first == null) return List.of();
        for (int index = 0; index < level; index++) {
            String id = index < history.size() ? history.getCompound(index).getString("Statue")
                    : data == null ? "" : data.getString("SecondProfile");
            var second = WeaponEvolutionSkillPool.Aspect.fromId(id);
            if (second == null) second = first;
            var fusion = WeaponEvolutionSkillPool.fusion(first, second);
            Component name = index == 0 ? Component.translatable(first.translationKey())
                    : Component.translatable(fusion.primaryTranslationKey());
            Component description = index == 0 ? net.farmingtales.ftboss.skill.WeaponTypeSkills.description(stack, false)
                    : Component.translatable("screen.ftboss.evolution.fusion_pair",
                    Component.translatable(fusion.primaryTranslationKey()), Component.translatable(fusion.secondaryTranslationKey()));
            result.add(new WeaponDetailView(Component.translatable("screen.ftboss.statue_hub.details.category.skill"),
                    Component.translatable("screen.ftboss.evolution.stage_skill", index + 1, name), description,
                    index, selectedStage(stack) == index));
        }
        for (EvolutionSkill skill : passiveSkills(stack)) result.add(new WeaponDetailView(
                Component.translatable("screen.ftboss.evolution.passive"), skill.nameComponent(),
                FoodWeaponSkillRarity.passiveDescription(stack, FoodWeaponEvolution.skillId(skill), FoodWeaponEvolution.skillDescription(skill))));
        return List.copyOf(result);
    }

    static void clear(ItemStack stack) {
        FoodWeaponRepair.invalidateForProgression(stack);
        CompoundTag data = stack.getTagElement(ROOT);
        if (data != null) {
            data.remove(HISTORY);
            data.remove(SELECTED);
            data.putInt(VERSION, 2);
        }
    }
}
