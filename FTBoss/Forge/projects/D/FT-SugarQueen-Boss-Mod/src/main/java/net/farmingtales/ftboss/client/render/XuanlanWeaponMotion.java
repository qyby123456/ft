package net.farmingtales.ftboss.client.render;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.farmingtales.ftboss.item.XuanlanMoonWeaponItem;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.skill.XuanlanFxPacket;
import net.farmingtales.ftboss.util.XuanlanMotionRules;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class XuanlanWeaponMotion {
    private record Key(UUID owner, InteractionHand hand) {}
    private static final class Actions {
        long shot = Long.MIN_VALUE;
        final long[] thrown = {Long.MIN_VALUE, Long.MIN_VALUE};
        final long[] caught = {Long.MIN_VALUE, Long.MIN_VALUE};
        long touched;
    }
    private static final Map<Key, Actions> ACTIONS = new LinkedHashMap<>();
    private static Level currentLevel;

    private XuanlanWeaponMotion() {}

    public static void useLevel(Level level) {
        if (currentLevel == level) return;
        ACTIONS.clear();
        currentLevel = level;
    }

    public static void receive(XuanlanFxPacket packet) {
        var level = Minecraft.getInstance().level;
        useLevel(level);
        if (level == null || !XuanlanMotionRules.action(packet.kind())
                || !level.dimension().location().equals(packet.dimension())) return;
        if (!(level.getEntity(packet.entityId()) instanceof LivingEntity owner)) return;
        Key key = new Key(owner.getUUID(), XuanlanMotionRules.offhand(packet.kind()) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (!ACTIONS.containsKey(key) && ACTIONS.size() >= 128) ACTIONS.remove(ACTIONS.keySet().iterator().next());
        Actions actions = ACTIONS.computeIfAbsent(key, ignored -> new Actions());
        long now = level.getGameTime();
        actions.touched = now;
        if (packet.kind() <= XuanlanMotionRules.SHOT_OFF) {
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
        useLevel(level);
        if (level == null) return;
        long now = level.getGameTime();
        ACTIONS.values().removeIf(actions -> now < actions.touched || now - actions.touched > 80);
    }

    public static XuanlanMotionRules.Pose sample(LivingEntity owner, ItemStack stack, float partial) {
        var level = Minecraft.getInstance().level;
        useLevel(level);
        float clock = level == null ? 0 : (level.getGameTime() % 24000) + partial;
        if (owner == null || owner.level() != level) return XuanlanMotionRules.sample(0, -1, -1, -1, clock);
        if (owner instanceof EasterBossEntity boss && boss.getVariant() == 6 && owner.getMainHandItem() == stack) {
            var cue = boss.getSkillCue();
            float age = level.getGameTime() - cue.getLong("start") + partial;
            int duration = cue.getInt("duration");
            if (boss.isAlive() && cue.getBoolean("beam") && age >= 0 && age < duration) {
                boolean firing = cue.getBoolean("firing");
                return XuanlanMotionRules.sample(firing ? 20 : 20 * age / Math.max(1, duration),
                        firing ? age : -1, -1, -1, clock);
            }
        }
        InteractionHand hand = owner.getMainHandItem() == stack ? InteractionHand.MAIN_HAND
                : owner.getOffhandItem() == stack ? InteractionHand.OFF_HAND : null;
        boolean charging = owner.isUsingItem() && owner.getUseItem() == stack && !XuanlanMoonWeaponItem.isWhite(stack);
        Actions actions = hand == null ? null : ACTIONS.get(new Key(owner.getUUID(), hand));
        var base = XuanlanMotionRules.sample(charging ? owner.getTicksUsingItem() + partial : 0,
                age(actions == null ? Long.MIN_VALUE : actions.shot, level, partial), -1, -1, clock);
        if (actions == null) return base;
        float fold = 0;
        float pulse = 0;
        for (int slot = 0; slot < actions.thrown.length; slot++) {
            var blade = XuanlanMotionRules.sample(0, -1,
                    age(actions.thrown[slot], level, partial), age(actions.caught[slot], level, partial), clock);
            fold += blade.fold() / actions.thrown.length;
            pulse = Math.max(pulse, blade.catchPulse());
        }
        return new XuanlanMotionRules.Pose(base.charge(), base.recoil(), fold, pulse, clock);
    }

    private static float age(long start, Level level, float partial) {
        return start == Long.MIN_VALUE ? -1 : level.getGameTime() - start + partial;
    }
}
