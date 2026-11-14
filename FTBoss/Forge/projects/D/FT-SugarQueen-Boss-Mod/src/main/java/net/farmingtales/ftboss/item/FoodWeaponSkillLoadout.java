package net.farmingtales.ftboss.item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.item.FoodWeaponEvolution.WeaponDetailView;
import net.farmingtales.ftboss.util.WeaponEvolutionSkillPool;
import net.farmingtales.ftboss.util.WeaponSkillLoadoutRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 石像首次绑定与多技能携带；2026-10-12，独立于进化历史和冷却保存，不改写主核心。 */
@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class FoodWeaponSkillLoadout {
    public static final String CARRIED = "FTBossCarriedEvolutionSkills";
    private static final int RECONCILE_RETRY_TICKS = 20;
    private static final Map<ServerPlayer, Integer> FAILED_RECONCILE = new WeakHashMap<>();

    private FoodWeaponSkillLoadout() {}

    public static boolean canBind(ItemStack stack) {
        if (stack.isEmpty() || !FoodWeaponEvolution.isEvolutionCandidate(stack) || FoodWeaponEvolution.isNativeWeapon(stack)) return false;
        CompoundTag data = stack.getTagElement(FoodWeaponProgression.ROOT);
        return data == null || WeaponEvolutionSkillPool.Aspect.fromId(data.getString("Profile")) == null;
    }

    public static boolean bindFromUi(ServerPlayer player, int tint) {
        if (!player.isAlive() || player.isSpectator()) return false;
        ItemStack weapon = FoodWeaponEvolution.heldEvolutionWeapon(player);
        String profile = FoodWeaponEvolution.profileId(tint);
        if (profile.isEmpty() || !canBind(weapon)) return false;
        weapon.getOrCreateTagElement(FoodWeaponProgression.ROOT).putString("Profile", profile);
        FoodWeaponLifecycle.ensureTracked(weapon);
        NativeFoodWeaponWear.manage(player, weapon);
        player.sendSystemMessage(Component.translatable("message.ftboss.evolution.attuned", weapon.getHoverName(),
                Component.translatable("profile.ftboss.evolution." + profile)));
        return true;
    }

    public static List<WeaponDetailView> skills(ItemStack stack) {
        LinkedHashMap<Integer, WeaponDetailView> entries = new LinkedHashMap<>();
        for (WeaponDetailView view : FoodWeaponEvolution.weaponDetailViews(stack)) {
            if (view.selectable() && view.gourmetSkillIndex() < WeaponSkillLoadoutRules.MAX_SKILLS) {
                entries.putIfAbsent(view.gourmetSkillIndex(), view);
            }
        }
        return List.copyOf(entries.values());
    }

    public static List<Integer> carried(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        int[] stored = tag != null && tag.contains(CARRIED, Tag.TAG_INT_ARRAY) ? tag.getIntArray(CARRIED) : null;
        return WeaponSkillLoadoutRules.carried(FoodWeaponEvolution.evolutionLevel(stack), stored);
    }

    public static List<WeaponDetailView> carriedSkills(ItemStack stack) {
        List<Integer> carried = carried(stack);
        return skills(stack).stream().filter(view -> carried.contains(view.gourmetSkillIndex())).toList();
    }

    public static int carriedSkillCount(ItemStack stack) {
        int available = FoodWeaponEvolution.selectableSkillCount(stack);
        if (available <= 0) return 0;
        int count = 0;
        for (int index : carried(stack)) if (index < available) count++;
        return count;
    }

    public static int activeIndex(ItemStack stack) {
        return FoodWeaponEvolution.isGourmetEvolutionWeapon(stack) ? FoodWeaponEvolution.activeGourmetSkillIndex(stack)
                : FoodWeaponProgression.selectedStage(stack);
    }

    private static void save(ItemStack stack, List<Integer> selected) {
        stack.getOrCreateTag().putIntArray(CARRIED, selected.stream().mapToInt(Integer::intValue).toArray());
    }

    private static boolean prune(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(CARRIED, Tag.TAG_INT_ARRAY) || !FoodWeaponEvolution.isEvolutionCandidate(stack)) return false;
        int unlocked = Math.min(WeaponSkillLoadoutRules.MAX_SKILLS, FoodWeaponEvolution.evolutionLevel(stack));
        int[] stored = tag.getIntArray(CARRIED);
        boolean valid = stored.length > 0 || unlocked == 0;
        int previous = -1;
        for (int index : stored) {
            if (index <= previous || index < 0 || index >= unlocked) { valid = false; break; }
            previous = index;
        }
        if (valid) return false;
        save(stack, WeaponSkillLoadoutRules.carried(unlocked, stored));
        return true;
    }

    public static boolean select(ServerPlayer player, int index, boolean onlyCarried) {
        if (!player.isAlive() || player.isSpectator()) return false;
        ItemStack weapon = FoodWeaponEvolution.heldEvolutionWeapon(player);
        if (skills(weapon).stream().noneMatch(view -> view.gourmetSkillIndex() == index)) return false;
        List<Integer> selected = new ArrayList<>(carried(weapon));
        if (onlyCarried && !selected.contains(index)) return false;
        if (!FoodWeaponEvolution.selectGourmetSkillFromUi(player, index)) return false;
        if (!selected.contains(index)) {
            selected.add(index);
            selected.sort(Integer::compareTo);
            save(weapon, selected);
        }
        return true;
    }

    public static boolean toggleFromUi(ServerPlayer player, int index) {
        if (!player.isAlive() || player.isSpectator()) return false;
        ItemStack weapon = FoodWeaponEvolution.heldEvolutionWeapon(player);
        if (skills(weapon).stream().noneMatch(view -> view.gourmetSkillIndex() == index)) return false;
        List<Integer> before = carried(weapon);
        List<Integer> after = WeaponSkillLoadoutRules.toggle(FoodWeaponEvolution.evolutionLevel(weapon),
                before.stream().mapToInt(Integer::intValue).toArray(), index);
        if (before.equals(after)) {
            player.displayClientMessage(Component.translatable("message.ftboss.skills.keep_one"), true);
            return false;
        }
        int active = after.contains(activeIndex(weapon)) ? activeIndex(weapon) : after.get(0);
        if (!FoodWeaponEvolution.selectGourmetSkillFromUi(player, active)) return false;
        save(weapon, after);
        return true;
    }

    @SubscribeEvent
    public static void reconcile(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || !player.isAlive() || player.isSpectator()) return;
        boolean changed = false;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            changed |= prune(player.getInventory().getItem(slot));
        }
        if (changed) {
            player.getInventory().setChanged();
            player.inventoryMenu.broadcastChanges();
            player.containerMenu.broadcastChanges();
        }
        ItemStack weapon = player.getMainHandItem();
        if (!weapon.hasTag() || !weapon.getTag().contains(CARRIED, Tag.TAG_INT_ARRAY)) return;
        List<Integer> available = carried(weapon);
        if (available.isEmpty() || available.contains(activeIndex(weapon))) return;
        Integer failed = FAILED_RECONCILE.get(player);
        if (failed != null && player.tickCount - failed < RECONCILE_RETRY_TICKS) return;
        if (FoodWeaponEvolution.selectGourmetSkillFromUi(player, available.get(0))) {
            FAILED_RECONCILE.remove(player);
            player.getInventory().setChanged();
            player.inventoryMenu.broadcastChanges();
            player.containerMenu.broadcastChanges();
        } else FAILED_RECONCILE.put(player, player.tickCount);
    }
}
