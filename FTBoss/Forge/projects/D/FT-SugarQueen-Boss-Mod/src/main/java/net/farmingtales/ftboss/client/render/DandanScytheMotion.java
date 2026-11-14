package net.farmingtales.ftboss.client.render;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.item.weapons.DandanScytheItem;
import net.farmingtales.ftboss.skill.DandanScytheFxPacket;
import net.farmingtales.ftboss.util.DandanScytheMotionRules;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class DandanScytheMotion {
    private static final class Action {
        ItemStack stack;
        int slot;
        long start = Long.MIN_VALUE;
        long touched;
        long canceled = Long.MIN_VALUE;
        float chargeTicks;
        boolean using;
        boolean blink;

        Action(Player owner, long now) {
            stack = owner.getMainHandItem().copy();
            stack.getOrCreateTag().putInt("Damage", 0);
            slot = owner.getInventory().selected;
            touched = now;
        }

        boolean matches(Player owner) {
            if (slot != owner.getInventory().selected || !(owner.getMainHandItem().getItem() instanceof DandanScytheItem)) return false;
            ItemStack held = owner.getMainHandItem().copy();
            held.getOrCreateTag().putInt("Damage", 0);
            return ItemStack.isSameItemSameTags(stack, held);
        }
    }
    private static final Map<UUID, Action> ACTIONS = new LinkedHashMap<>();
    private static Level currentLevel;

    private DandanScytheMotion() {}

    public static void reset(Level level) {
        if (currentLevel == level) return;
        ACTIONS.clear();
        currentLevel = level;
    }

    public static void receive(DandanScytheFxPacket packet) {
        var level = Minecraft.getInstance().level;
        reset(level);
        if (level == null || !level.dimension().location().equals(packet.dimension())) return;
        if (!(level.getEntity(packet.targetId()) instanceof Player owner)
                || !(owner.getMainHandItem().getItem() instanceof DandanScytheItem)) return;
        if (!ACTIONS.containsKey(owner.getUUID()) && ACTIONS.size() >= DandanScytheMotionRules.MAX_OWNERS) {
            ACTIONS.remove(ACTIONS.keySet().iterator().next());
        }
        Action action = new Action(owner, level.getGameTime());
        action.start = level.getGameTime();
        action.blink = packet.kind() == DandanScytheMotionRules.BLINK_ACTION;
        ACTIONS.put(owner.getUUID(), action);
    }

    public static void tick(Level level) {
        reset(level);
        if (level == null) return;
        long now = level.getGameTime();
        ACTIONS.entrySet().removeIf(entry -> {
            Action action = entry.getValue();
            return now < action.touched || now - action.touched >= DandanScytheMotionRules.RECOVERY_TICKS;
        });
        for (Player owner : level.players()) {
            Action action = ACTIONS.get(owner.getUUID());
            boolean using = owner.isAlive() && owner.isUsingItem() && owner.getUseItem() == owner.getMainHandItem()
                    && owner.getMainHandItem().getItem() instanceof DandanScytheItem;
            if (action != null && (!owner.isAlive() || !action.matches(owner))) {
                ACTIONS.remove(owner.getUUID());
                action = null;
            }
            if (action == null && using && ACTIONS.size() < DandanScytheMotionRules.MAX_OWNERS) {
                action = new Action(owner, now);
                ACTIONS.put(owner.getUUID(), action);
            }
            if (action == null) continue;
            if (using) {
                action.chargeTicks = owner.getTicksUsingItem();
                action.touched = now;
                action.canceled = Long.MIN_VALUE;
            } else if (action.using) {
                action.canceled = now;
                action.touched = now;
            }
            action.using = using;
        }
    }

    public static DandanScytheMotionRules.Pose sample(LivingEntity owner, ItemStack stack, float partial) {
        var level = Minecraft.getInstance().level;
        reset(level);
        float clock = level == null ? 0 : level.getGameTime() % 24000 + partial;
        if (owner == null || owner.level() != level || !owner.isAlive()
                || owner.getMainHandItem() != stack && owner.getOffhandItem() != stack) {
            return DandanScytheMotionRules.sample(0, 0, -1, false, 0);
        }
        boolean using = owner.isUsingItem() && owner.getUseItem() == stack;
        boolean main = owner.getMainHandItem() == stack;
        Action action = main && owner instanceof Player player ? ACTIONS.get(player.getUUID()) : null;
        if (action != null && !action.matches((Player) owner)) action = null;
        float age = action == null || action.start == Long.MIN_VALUE ? -1 : level.getGameTime() - action.start + partial;
        float charge = using ? owner.getTicksUsingItem() + partial : 0;
        if (!using && action != null && action.canceled != Long.MIN_VALUE && age < 0) {
            float cancelAge = level.getGameTime() - action.canceled + partial;
            charge = action.chargeTicks * (1 - DandanScytheMotionRules.smooth(cancelAge / 6));
        }
        float swing = main ? owner.getAttackAnim(partial) : 0;
        return DandanScytheMotionRules.sample(charge, swing,
                age, using ? owner.isShiftKeyDown() : action != null && action.blink, clock);
    }
}
