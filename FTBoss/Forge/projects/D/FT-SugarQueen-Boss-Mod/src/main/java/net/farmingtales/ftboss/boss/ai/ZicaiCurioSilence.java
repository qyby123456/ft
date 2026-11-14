package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.boss.ManorMercy;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ZicaiCurioSilenceRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioEquipEvent;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@Mod.EventBusSubscriber(modid = "ftboss", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ZicaiCurioSilence {
    private static final String PENDING = "ftboss:ZicaiCurioSilence";
    private static final Map<UUID, Map<UUID, Long>> LAST_HITS = new HashMap<>();

    private record Choice(String slot, int index, ItemStack stack, CompoundTag pending) {}

    private ZicaiCurioSilence() {}

    private static long clock(ServerPlayer player) {
        return player.getServer().overworld().getGameTime();
    }

    private static ListTag pending(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getList(PENDING, Tag.TAG_COMPOUND);
    }

    private static void save(ServerPlayer player, ListTag entries) {
        CompoundTag data = player.getPersistentData();
        CompoundTag persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
        if (entries.isEmpty()) persisted.remove(PENDING);
        else persisted.put(PENDING, entries);
        if (!persisted.isEmpty()) data.put(Player.PERSISTED_NBT_TAG, persisted);
        else data.remove(Player.PERSISTED_NBT_TAG);
    }

    private static ICuriosItemHandler inventory(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player).resolve().orElse(null);
    }

    private static IDynamicStackHandler slot(ICuriosItemHandler inventory, String identifier, int index) {
        if (inventory == null) return null;
        var handler = inventory.getCurios().get(identifier);
        return handler != null && index >= 0 && index < handler.getStacks().getSlots() ? handler.getStacks() : null;
    }

    private static boolean same(CompoundTag entry, String identifier, int index) {
        return entry.getString("Slot").equals(identifier) && entry.getInt("Index") == index;
    }

    private static EasterBossEntity owner(LivingDamageEvent event) {
        Entity source = event.getSource().getEntity();
        if (source == null) source = event.getSource().getDirectEntity();
        if (source == null) source = ManorMercy.damageOwner();
        for (int depth = 0; source instanceof Projectile projectile && depth < 8; depth++) {
            Entity next = projectile.getOwner();
            if (next == source) return null;
            source = next;
        }
        return source instanceof EasterBossEntity boss
                && ZicaiCurioSilenceRules.eligible(boss.getVariant(), boss.isMini(), boss.isGiant()) ? boss : null;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamage(LivingDamageEvent event) {
        if (event.isCanceled() || event.getAmount() <= 0 || !(event.getEntity() instanceof ServerPlayer player)
                || player.isCreative() || player.isSpectator()) return;
        EasterBossEntity boss = owner(event);
        if (boss == null) return;
        long now = clock(player);
        Map<UUID, Long> hits = LAST_HITS.computeIfAbsent(player.getUUID(), ignored -> new HashMap<>());
        Long previous = hits.put(boss.getUUID(), now);
        if (previous != null && previous == now) return;
        silence(player, now);
    }

    private static void silence(ServerPlayer player, long now) {
        ICuriosItemHandler inventory = inventory(player);
        if (inventory == null) return;
        ListTag entries = pending(player);
        var choices = new ArrayList<Choice>();
        for (var group : inventory.getCurios().entrySet()) {
            IDynamicStackHandler stacks = group.getValue().getStacks();
            for (int index = 0; index < stacks.getSlots(); index++) {
                ItemStack stack = stacks.getStackInSlot(index);
                if (!stack.isEmpty()) choices.add(new Choice(group.getKey(), index, stack, null));
            }
        }
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            IDynamicStackHandler stacks = slot(inventory, entry.getString("Slot"), entry.getInt("Index"));
            if (stacks != null && stacks.getStackInSlot(entry.getInt("Index")).isEmpty()
                    && ZicaiCurioSilenceRules.active(now, entry.getLong("Until"))) {
                ItemStack stack = ItemStack.of(entry.getCompound("Stack"));
                if (!stack.isEmpty()) choices.add(new Choice(entry.getString("Slot"), entry.getInt("Index"), stack, entry));
            }
        }
        int selected = ZicaiCurioSilenceRules.choose(choices.size(), player.getRandom()::nextInt);
        if (selected < 0) return;
        Choice choice = choices.get(selected);
        CompoundTag entry = choice.pending();
        if (entry == null) {
            entry = new CompoundTag();
            entry.putString("Slot", choice.slot());
            entry.putInt("Index", choice.index());
            entry.put("Stack", choice.stack().save(new CompoundTag()));
            entries.add(entry);
        }
        entry.putLong("Until", ZicaiCurioSilenceRules.expires(now));
        save(player, entries);
        if (choice.pending() == null) inventory.getCurios().get(choice.slot()).getStacks().setStackInSlot(choice.index(), ItemStack.EMPTY);
        player.displayClientMessage(Component.literal("§5紫菜 §7使 ").append(choice.stack().getHoverName()).append("§7 沉默 5 秒"), true);
    }

    private static void restore(ServerPlayer player, boolean force) {
        ListTag entries = pending(player);
        if (entries.isEmpty()) return;
        long now = clock(player);
        ICuriosItemHandler inventory = inventory(player);
        for (int index = entries.size() - 1; index >= 0; index--) {
            CompoundTag entry = entries.getCompound(index);
            if (!force && ZicaiCurioSilenceRules.active(now, entry.getLong("Until"))) continue;
            ItemStack stack = ItemStack.of(entry.getCompound("Stack"));
            if (stack.isEmpty()) {
                entries.remove(index);
                continue;
            }
            IDynamicStackHandler stacks = slot(inventory, entry.getString("Slot"), entry.getInt("Index"));
            if (stacks != null && stacks.getStackInSlot(entry.getInt("Index")).isEmpty()) {
                stacks.setStackInSlot(entry.getInt("Index"), stack);
                entries.remove(index);
            } else {
                player.getInventory().add(stack);
                if (stack.isEmpty()) entries.remove(index);
                else entry.put("Stack", stack.save(new CompoundTag()));
            }
        }
        save(player, entries);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        restore(player, false);
        Map<UUID, Long> hits = LAST_HITS.get(player.getUUID());
        if (hits != null) {
            long now = clock(player);
            hits.values().removeIf(time -> time != now);
            if (hits.isEmpty()) LAST_HITS.remove(player.getUUID());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEquip(CurioEquipEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getSlotContext().cosmetic()) return;
        var context = event.getSlotContext();
        ListTag entries = pending(player);
        long now = clock(player);
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            if (same(entry, context.identifier(), context.index()) && ZicaiCurioSilenceRules.active(now, entry.getLong("Until"))) {
                event.setResult(Event.Result.DENY);
                return;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) restore(player, true);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) restore(player, true);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            restore(player, true);
            LAST_HITS.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) restore(player, true);
    }

    @SubscribeEvent
    public static void onStopped(ServerStoppedEvent event) {
        LAST_HITS.clear();
    }
}
