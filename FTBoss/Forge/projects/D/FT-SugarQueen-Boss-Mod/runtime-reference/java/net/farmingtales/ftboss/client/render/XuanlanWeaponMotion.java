/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.client.render;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.XuanlanMoonWeaponItem;
import net.farmingtales.ftboss.skill.XuanlanFxPacket;
import net.farmingtales.ftboss.util.XuanlanMotionRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class XuanlanWeaponMotion {
    private static final Map<Key, Actions> ACTIONS = new LinkedHashMap<Key, Actions>();
    private static Level currentLevel;

    private XuanlanWeaponMotion() {
    }

    public static void useLevel(Level level) {
        if (currentLevel == level) {
            return;
        }
        ACTIONS.clear();
        currentLevel = level;
    }

    public static void receive(XuanlanFxPacket packet) {
        long now;
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        XuanlanWeaponMotion.useLevel((Level)level);
        if (level == null || !XuanlanMotionRules.action(packet.kind()) || !level.m_46472_().m_135782_().equals((Object)packet.dimension())) {
            return;
        }
        Entity entity = level.m_6815_(packet.entityId());
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        LivingEntity owner = (LivingEntity)entity;
        Key key = new Key(owner.m_20148_(), XuanlanMotionRules.offhand(packet.kind()) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (!ACTIONS.containsKey(key) && ACTIONS.size() >= 128) {
            ACTIONS.remove(ACTIONS.keySet().iterator().next());
        }
        Actions actions = ACTIONS.computeIfAbsent(key, ignored -> new Actions());
        actions.touched = now = level.m_46467_();
        if (packet.kind() <= 7) {
            actions.shot = now;
        } else {
            int slot = XuanlanMotionRules.bladeSlot(packet.kind());
            if (XuanlanMotionRules.isThrow(packet.kind())) {
                actions.thrown[slot] = now;
                actions.caught[slot] = Long.MIN_VALUE;
            } else {
                actions.thrown[slot] = Long.MIN_VALUE;
                actions.caught[slot] = XuanlanMotionRules.isCatch(packet.kind()) ? now : Long.MIN_VALUE;
            }
        }
    }

    public static void tick(Level level) {
        XuanlanWeaponMotion.useLevel(level);
        if (level == null) {
            return;
        }
        long now = level.m_46467_();
        ACTIONS.values().removeIf(actions -> now < actions.touched || now - actions.touched > 80L);
    }

    public static XuanlanMotionRules.Pose sample(LivingEntity owner, ItemStack stack, float partial) {
        EasterBossEntity boss;
        float clock;
        ClientLevel level = Minecraft.m_91087_().f_91073_;
        XuanlanWeaponMotion.useLevel((Level)level);
        float f = clock = level == null ? 0.0f : (float)(level.m_46467_() % 24000L) + partial;
        if (owner == null || owner.m_9236_() != level) {
            return XuanlanMotionRules.sample(0.0f, -1.0f, -1.0f, -1.0f, clock);
        }
        if (owner instanceof EasterBossEntity && (boss = (EasterBossEntity)owner).getVariant() == 6 && owner.m_21205_() == stack) {
            CompoundTag cue = boss.getSkillCue();
            float age = (float)(level.m_46467_() - cue.m_128454_("start")) + partial;
            int duration = cue.m_128451_("duration");
            if (boss.m_6084_() && cue.m_128471_("beam") && age >= 0.0f && age < (float)duration) {
                boolean firing = cue.m_128471_("firing");
                return XuanlanMotionRules.sample(firing ? 20.0f : 20.0f * age / (float)Math.max(1, duration), firing ? age : -1.0f, -1.0f, -1.0f, clock);
            }
        }
        InteractionHand hand = owner.m_21205_() == stack ? InteractionHand.MAIN_HAND : (owner.m_21206_() == stack ? InteractionHand.OFF_HAND : null);
        boolean charging = owner.m_6117_() && owner.m_21211_() == stack && !XuanlanMoonWeaponItem.isWhite(stack);
        Actions actions = hand == null ? null : ACTIONS.get(new Key(owner.m_20148_(), hand));
        XuanlanMotionRules.Pose base = XuanlanMotionRules.sample(charging ? (float)owner.m_21252_() + partial : 0.0f, XuanlanWeaponMotion.age(actions == null ? Long.MIN_VALUE : actions.shot, (Level)level, partial), -1.0f, -1.0f, clock);
        if (actions == null) {
            return base;
        }
        float fold = 0.0f;
        float pulse = 0.0f;
        for (int slot = 0; slot < actions.thrown.length; ++slot) {
            XuanlanMotionRules.Pose blade = XuanlanMotionRules.sample(0.0f, -1.0f, XuanlanWeaponMotion.age(actions.thrown[slot], (Level)level, partial), XuanlanWeaponMotion.age(actions.caught[slot], (Level)level, partial), clock);
            fold += blade.fold() / (float)actions.thrown.length;
            pulse = Math.max(pulse, blade.catchPulse());
        }
        return new XuanlanMotionRules.Pose(base.charge(), base.recoil(), fold, pulse, clock);
    }

    private static float age(long start, Level level, float partial) {
        return start == Long.MIN_VALUE ? -1.0f : (float)(level.m_46467_() - start) + partial;
    }

    private record Key(UUID owner, InteractionHand hand) {
    }

    private static final class Actions {
        long shot = Long.MIN_VALUE;
        final long[] thrown = new long[]{Long.MIN_VALUE, Long.MIN_VALUE};
        final long[] caught = new long[]{Long.MIN_VALUE, Long.MIN_VALUE};
        long touched;

        private Actions() {
        }
    }
}

