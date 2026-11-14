package net.farmingtales.ftboss.util;

import java.util.UUID;

public final class ManorTeleportRules {
    /** Players may stay this far from the current boss before recall protection starts. */
    public static final double STRAY_FROM_BOSS = 76.0;
    /** Start the action-bar warning before the hard recall boundary. */
    public static final double RECALL_WARNING_DISTANCE = STRAY_FROM_BOSS - 8.0;
    /** Five-second grace period after a player crosses the recall boundary. */
    public static final int RECALL_COUNTDOWN_TICKS = 5 * 20;

    public enum Recall {
        NONE, PLAYER_TO_BOSS, BOSS_TO_PLAYER
    }

    private ManorTeleportRules() {}

    public static boolean isSummoner(UUID summonerId, UUID playerId) {
        return summonerId != null && summonerId.equals(playerId);
    }

    /**
     * 2026-09-19 防逃课扩容（用户口径 A）：拴绳对**全体参战者**生效。
     * 名单只认本场持久化的参战集合（确认挑战时写入、随存档保存），
     * 仍然**绝不**按"最近玩家"补位 —— 不在名单里的玩家（旁观、路人、旧存档无身份者）一概不动。
     */
    public static boolean isParticipant(java.util.Set<UUID> participants, UUID playerId) {
        return participants != null && playerId != null && participants.contains(playerId);
    }

    public static Recall recall(boolean sameDimension, double distanceToBossSquared,
                                double playerToAnchorSquared, double bossToAnchorSquared, boolean noTarget) {
        if (!sameDimension) return Recall.PLAYER_TO_BOSS;
        if (distanceToBossSquared <= STRAY_FROM_BOSS * STRAY_FROM_BOSS && !noTarget) return Recall.NONE;
        return playerToAnchorSquared > bossToAnchorSquared ? Recall.PLAYER_TO_BOSS : Recall.BOSS_TO_PLAYER;
    }
}
