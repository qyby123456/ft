/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.client.render;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.item.weapons.DandanScytheItem;
import net.farmingtales.ftboss.skill.DandanScytheFxPacket;
import net.farmingtales.ftboss.util.DandanScytheMotionRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class DandanScytheMotion {
    private static final Map<UUID, Action> ACTIONS = new LinkedHashMap<UUID, Action>();
    private static Level currentLevel;

    private DandanScytheMotion() {
    }

    public static void reset(Level level) {
        if (currentLevel == level) {
            return;
        }
        ACTIONS.clear();
        currentLevel = level;
    }

    public static void receive(DandanScytheFxPacket packet) {
        Player owner;
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        DandanScytheMotion.reset((Level)level);
        if (level == null || !level.m_46472_().m_135782_().equals((Object)packet.dimension())) {
            return;
        }
        Entity entity = level.m_6815_(packet.targetId());
        if (!(entity instanceof Player) || !((owner = (Player)entity).m_21205_().m_41720_() instanceof DandanScytheItem)) {
            return;
        }
        if (!ACTIONS.containsKey(owner.m_20148_()) && ACTIONS.size() >= 64) {
            ACTIONS.remove(ACTIONS.keySet().iterator().next());
        }
        Action action = new Action(owner, level.m_46467_());
        action.start = level.m_46467_();
        action.blink = packet.kind() == 5;
        ACTIONS.put(owner.m_20148_(), action);
    }

    public static void tick(Level level) {
        DandanScytheMotion.reset(level);
        if (level == null) {
            return;
        }
        long now = level.m_46467_();
        ACTIONS.entrySet().removeIf(entry -> {
            Action action = (Action)entry.getValue();
            return now < action.touched || now - action.touched >= 28L;
        });
        for (Player owner : level.m_6907_()) {
            boolean using;
            Action action = ACTIONS.get(owner.m_20148_());
            boolean bl = using = owner.m_6084_() && owner.m_6117_() && owner.m_21211_() == owner.m_21205_() && owner.m_21205_().m_41720_() instanceof DandanScytheItem;
            if (!(action == null || owner.m_6084_() && action.matches(owner))) {
                ACTIONS.remove(owner.m_20148_());
                action = null;
            }
            if (action == null && using && ACTIONS.size() < 64) {
                action = new Action(owner, now);
                ACTIONS.put(owner.m_20148_(), action);
            }
            if (action == null) continue;
            if (using) {
                action.chargeTicks = owner.m_21252_();
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
        float swing;
        float charge;
        Action action;
        boolean main;
        float clock;
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        DandanScytheMotion.reset((Level)level);
        float f = clock = level == null ? 0.0f : (float)(level.m_46467_() % 24000L) + partial;
        if (owner == null || owner.m_9236_() != level || !owner.m_6084_() || owner.m_21205_() != stack && owner.m_21206_() != stack) {
            return DandanScytheMotionRules.sample(0.0f, 0.0f, -1.0f, false, 0.0f);
        }
        boolean using = owner.m_6117_() && owner.m_21211_() == stack;
        boolean bl = main = owner.m_21205_() == stack;
        if (main && owner instanceof Player) {
            Player player = (Player)owner;
            v2 = ACTIONS.get(player.m_20148_());
        } else {
            v2 = action = null;
        }
        if (action != null && !action.matches((Player)owner)) {
            action = null;
        }
        float age = action == null || action.start == Long.MIN_VALUE ? -1.0f : (float)(level.m_46467_() - action.start) + partial;
        float f2 = charge = using ? (float)owner.m_21252_() + partial : 0.0f;
        if (!using && action != null && action.canceled != Long.MIN_VALUE && age < 0.0f) {
            float cancelAge = (float)(level.m_46467_() - action.canceled) + partial;
            charge = action.chargeTicks * (1.0f - DandanScytheMotionRules.smooth(cancelAge / 6.0f));
        }
        float f3 = swing = main ? owner.m_21324_(partial) : 0.0f;
        return DandanScytheMotionRules.sample(charge, swing, age, using ? owner.m_6144_() : action != null && action.blink, clock);
    }

    private static final class Action {
        ItemStack stack;
        int slot;
        long start = Long.MIN_VALUE;
        long touched;
        long canceled = Long.MIN_VALUE;
        float chargeTicks;
        boolean using;
        boolean blink;

        Action(Player player, long l) {
            this.stack = player.m_21205_().m_41777_();
            this.stack.m_41784_().m_128405_("Damage", 0);
            this.slot = player.m_150109_().f_35977_;
            this.touched = l;
        }

        boolean matches(Player player) {
            if (this.slot != player.m_150109_().f_35977_ || !(player.m_21205_().m_41720_() instanceof DandanScytheItem)) {
                return false;
            }
            ItemStack itemStack = player.m_21205_().m_41777_();
            itemStack.m_41784_().m_128405_("Damage", 0);
            return ItemStack.m_150942_((ItemStack)this.stack, (ItemStack)itemStack);
        }
    }
}

