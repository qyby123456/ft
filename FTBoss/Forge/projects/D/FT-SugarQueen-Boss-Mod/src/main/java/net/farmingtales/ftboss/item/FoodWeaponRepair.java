package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.util.FoodWeaponLifecycleRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** 绑定石像修复交付任务；每轮三项、进度随武器持久化，2026-10-13 新增服务端消耗与防重复领取。 */
public final class FoodWeaponRepair {
    private static final String QUEST = "RepairQuest";
    private static final String REVISION = "RepairRevision";
    private static final int QUEST_VERSION = 1;

    public record TaskView(int index, Component name, int progress, int required, boolean complete) {}
    public record View(boolean eligible, boolean correctStatue, boolean active, boolean ready,
                       int completed, int total, List<TaskView> tasks) {
        public View { tasks = List.copyOf(tasks); }
    }

    private FoodWeaponRepair() {}

    public static int revision(ItemStack weapon) {
        CompoundTag state = weapon.getTagElement(FoodWeaponLifecycle.ROOT);
        return state == null ? 0 : Math.max(0, state.getInt(REVISION));
    }

    private static CompoundTag activeQuest(ItemStack weapon) {
        CompoundTag state = weapon.getTagElement(FoodWeaponLifecycle.ROOT);
        CompoundTag quest = state == null ? new CompoundTag() : state.getCompound(QUEST);
        int level = FoodWeaponEvolution.evolutionLevel(weapon);
        if (quest.getInt("Version") != QUEST_VERSION || quest.getInt("Level") != level
                || !FoodWeaponLifecycle.repairProfile(weapon).equals(quest.getString("Profile"))) return new CompoundTag();
        int[] progress = quest.getIntArray("Progress");
        if (progress.length != FoodWeaponLifecycleRules.REPAIR_TASKS) return new CompoundTag();
        for (int task = 0; task < progress.length; task++) {
            if (progress[task] < 0 || progress[task] > FoodWeaponLifecycleRules.repairRequired(level, task)) return new CompoundTag();
        }
        return quest;
    }

    public static View view(ItemStack weapon, int tint) {
        String profile = FoodWeaponLifecycle.repairProfile(weapon);
        boolean correct = !profile.isEmpty() && profile.equals(FoodWeaponEvolution.profileId(tint));
        boolean eligible = FoodWeaponLifecycle.managed(weapon) && weapon.getDamageValue() > 0;
        CompoundTag quest = activeQuest(weapon);
        boolean active = !quest.isEmpty();
        int level = FoodWeaponEvolution.evolutionLevel(weapon);
        int[] progress = active ? quest.getIntArray("Progress") : new int[FoodWeaponLifecycleRules.REPAIR_TASKS];
        List<TaskView> tasks = new ArrayList<>();
        int completed = 0;
        for (int task = 0; task < FoodWeaponLifecycleRules.REPAIR_TASKS; task++) {
            int required = FoodWeaponLifecycleRules.repairRequired(level, task);
            Component name = task == 0 ? Component.translatable("screen.ftboss.repair.food",
                    Component.translatable("profile.ftboss.evolution." + profile))
                    : (task == 1 ? Items.IRON_INGOT : Items.LAPIS_LAZULI).getDescription();
            boolean complete = progress[task] == required;
            if (complete) completed++;
            tasks.add(new TaskView(task, name, progress[task], required, complete));
        }
        return new View(eligible, correct, active, active && FoodWeaponLifecycleRules.ready(progress, level),
                completed, FoodWeaponLifecycleRules.REPAIR_TASKS, tasks);
    }

    private static void advance(ItemStack weapon) {
        weapon.getOrCreateTagElement(FoodWeaponLifecycle.ROOT).putInt(REVISION, revision(weapon) == Integer.MAX_VALUE
                ? 0 : revision(weapon) + 1);
    }

    static void invalidateForProgression(ItemStack weapon) {
        CompoundTag state = weapon.getTagElement(FoodWeaponLifecycle.ROOT);
        if (state == null || !state.contains(QUEST)) return;
        state.remove(QUEST);
        advance(weapon);
    }

    private static boolean valid(ServerPlayer player, ItemStack weapon, int tint) {
        View view = view(weapon, tint);
        return player.isAlive() && !player.isSpectator() && view.eligible && view.correctStatue;
    }

    public static boolean start(ServerPlayer player, ItemStack weapon, int tint) {
        if (!valid(player, weapon, tint) || view(weapon, tint).active) return false;
        FoodWeaponLifecycle.ensureTracked(weapon);
        CompoundTag quest = new CompoundTag();
        quest.putInt("Version", QUEST_VERSION);
        quest.putInt("Level", FoodWeaponEvolution.evolutionLevel(weapon));
        quest.putString("Profile", FoodWeaponLifecycle.repairProfile(weapon));
        quest.putIntArray("Progress", new int[FoodWeaponLifecycleRules.REPAIR_TASKS]);
        weapon.getOrCreateTagElement(FoodWeaponLifecycle.ROOT).put(QUEST, quest);
        advance(weapon);
        player.displayClientMessage(Component.translatable("message.ftboss.repair.started"), true);
        return true;
    }

    public static boolean submit(ServerPlayer player, ItemStack weapon, int tint, int task) {
        if (!valid(player, weapon, tint) || task < 0 || task >= FoodWeaponLifecycleRules.REPAIR_TASKS) return false;
        CompoundTag quest = activeQuest(weapon);
        if (quest.isEmpty()) return false;
        int[] progress = quest.getIntArray("Progress");
        int required = FoodWeaponLifecycleRules.repairRequired(quest.getInt("Level"), task);
        int remaining = required - progress[task];
        if (remaining <= 0) return false;
        int consumed = 0;
        for (int slot = 0; slot < 36 && consumed < remaining; slot++) {
            ItemStack supply = player.getInventory().getItem(slot);
            if (supply.isEmpty() || supply == weapon || FoodWeaponEvolution.isEvolutionCandidate(supply)) continue;
            boolean matches = task == 0 ? FoodWeaponEvolution.matchesStatueFood(supply, tint)
                    : supply.is(task == 1 ? Items.IRON_INGOT : Items.LAPIS_LAZULI);
            if (!matches) continue;
            int count = Math.min(supply.getCount(), remaining - consumed);
            supply.shrink(count);
            consumed += count;
        }
        if (consumed == 0) {
            player.displayClientMessage(Component.translatable("message.ftboss.repair.supply_required"), true);
            return false;
        }
        progress[task] += consumed;
        quest.putIntArray("Progress", progress);
        advance(weapon);
        player.displayClientMessage(Component.translatable("message.ftboss.repair.submitted", consumed), true);
        return true;
    }

    public static boolean claim(ServerPlayer player, ItemStack weapon, int tint) {
        if (!valid(player, weapon, tint) || !view(weapon, tint).ready) return false;
        weapon.getOrCreateTagElement(FoodWeaponLifecycle.ROOT).remove(QUEST);
        FoodWeaponLifecycle.restoreAfterQuest(weapon);
        advance(weapon);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.7F, 1.0F);
        player.displayClientMessage(Component.translatable("message.ftboss.repair.complete"), true);
        net.farmingtales.ftboss.FTBossMod.LOGGER.info("[FTBoss][食武石像修复] {} weapon={} profile={} max={}",
                player.getName().getString(), FoodWeaponLifecycle.weaponId(weapon), FoodWeaponLifecycle.repairProfile(weapon), weapon.getMaxDamage());
        return true;
    }
}
