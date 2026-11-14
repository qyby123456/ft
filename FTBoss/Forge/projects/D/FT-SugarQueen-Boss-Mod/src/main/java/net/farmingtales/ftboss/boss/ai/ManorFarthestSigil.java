package net.farmingtales.ftboss.boss.ai;

import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.network.ManorFarthestSigilNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** 多人庄园场地远程攻击；三种虚空打击随机无重复，每段仅结算预警时锁定的最远玩家。 */
public final class ManorFarthestSigil {
    private static final String STATE = "ftboss:manor_farthest_sigil";
    private static final String[] LEGACY_SHOT = {"ftboss_manor_shot_due", "ftboss_manor_shot_next", "ftboss_manor_shot_target",
            "ftboss_manor_shot_x", "ftboss_manor_shot_y", "ftboss_manor_shot_z"};

    private ManorFarthestSigil() {}

    public static boolean tick(ServerLevel level, EasterBossEntity boss, List<ServerPlayer> supplied, Vec3 anchor) {
        CompoundTag data = boss.getPersistentData();
        UUID fight = ManorFight.fightId(boss);
        if (anchor == null || fight == null || !ManorFight.hasActive(fight) || boss.isMini() || boss.isGiant()
                || boss.getVariant() == EasterBossEntity.SPARROW_VARIANT) {
            cancel(level, boss, data);
            return false;
        }
        List<ServerPlayer> players = level.players().stream().filter(player -> valid(player, level, anchor)).toList();
        if (players.size() < 2) {
            cancel(level, boss, data);
            return false;
        }
        for (String key : LEGACY_SHOT) data.remove(key);
        if (!boss.isAlive() || boss.isDeadOrDying() || boss.isNoAi() || boss.isInvisible()
                || boss.isManorIntroHidden() || HiddenBossIntro.isActing(boss.getUUID())) {
            cancel(level, boss, data);
            return true;
        }
        long now = level.getGameTime();
        CompoundTag state = data.getCompound(STATE);
        if (data.contains(STATE) && (now < state.getLong("LastTick") || now - state.getLong("LastTick") > 2)) {
            cancel(level, boss, data);
            state = new CompoundTag();
        }
        if (!data.contains(STATE)) {
            state.putInt("Order", level.random.nextInt(6));
            state.putInt("Strike", 0);
            state.putLong("Next", now + ManorFarthestSigilRules.INITIAL_DELAY_TICKS);
            data.put(STATE, state);
        }
        state.putLong("LastTick", now);
        int strike = state.getInt("Strike");
        int order = state.getInt("Order");
        if (strike < 0 || strike >= 3 || order < 0 || order >= 6) {
            cancel(level, boss, data);
            return true;
        }
        int style = ManorFarthestSigilRules.style(order, strike);
        if (state.contains("Due")) {
            if (now < state.getLong("Due")) return true;
            Vec3 center = center(state);
            ManorFarthestSigilNetwork.show(level, boss, center, state.getDouble("Yaw"), style, 1);
            ServerPlayer target = state.hasUUID("Target") ? level.getServer().getPlayerList().getPlayer(state.getUUID("Target")) : null;
            if (valid(target, level, anchor) && ManorFarthestSigilRules.hits(style,
                    target.getX() - center.x, target.getY() - center.y, target.getZ() - center.z, state.getDouble("Yaw"))) {
                target.hurt(boss.damageSources().indirectMagic(boss, boss), boss.isImpossibleChallenge() ? 5 : 4);
            }
            level.playSound(null, center.x, center.y, center.z, SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 0.9F, 0.65F);
            state.remove("Due");
            state.remove("Target");
            strike++;
            if (strike == 3) {
                state.putInt("Order", level.random.nextInt(6));
                state.putInt("Strike", 0);
                state.putLong("Next", now + ManorFarthestSigilRules.COOLDOWN_TICKS);
            } else {
                state.putInt("Strike", strike);
                state.putLong("Next", now + ManorFarthestSigilRules.GAP_TICKS);
            }
            return true;
        }
        if (now < state.getLong("Next")) return true;
        UUID farthest = ManorFarthestSigilRules.farthest(players.stream().map(player ->
                new ManorFarthestSigilRules.Candidate(player.getUUID(), player.distanceToSqr(boss), true)).toList());
        ServerPlayer target = players.stream().filter(player -> player.getUUID().equals(farthest)).findFirst().orElse(null);
        if (target == null) return true;
        Vec3 center = groundAt(level, boss, target.position());
        double yaw = Math.atan2(center.z - boss.getZ(), center.x - boss.getX());
        state.putUUID("Target", target.getUUID());
        state.putDouble("X", center.x);
        state.putDouble("Y", center.y);
        state.putDouble("Z", center.z);
        state.putDouble("Yaw", yaw);
        state.putLong("Due", now + ManorFarthestSigilRules.WINDUP_TICKS);
        ManorFarthestSigilNetwork.show(level, boss, center, yaw, style, 0);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.HOSTILE, 0.8F, 0.7F);
        return true;
    }

    private static boolean valid(ServerPlayer player, ServerLevel level, Vec3 anchor) {
        return player != null && player.level() == level && player.isAlive() && !player.isRemoved()
                && !player.isSpectator() && !player.isCreative()
                && ManorFarthestSigilRules.insideArena(player.getX() - anchor.x, player.getY() - anchor.y, player.getZ() - anchor.z);
    }

    private static Vec3 groundAt(ServerLevel level, EasterBossEntity boss, Vec3 point) {
        var hit = level.clip(new ClipContext(point.add(0, 0.5, 0), point.add(0, -12, 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, boss));
        return hit.getType() == HitResult.Type.MISS ? point : hit.getLocation();
    }

    private static Vec3 center(CompoundTag state) {
        return new Vec3(state.getDouble("X"), state.getDouble("Y"), state.getDouble("Z"));
    }

    private static void cancel(ServerLevel level, EasterBossEntity boss, CompoundTag data) {
        if (!data.contains(STATE)) return;
        CompoundTag state = data.getCompound(STATE);
        ManorFarthestSigilNetwork.show(level, boss, center(state), 0, 0, 2);
        data.remove(STATE);
    }
}
