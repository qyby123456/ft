/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.Set;
import java.util.UUID;

public final class ManorTeleportRules {
    public static final double STRAY_FROM_BOSS = 76.0;
    public static final double RECALL_WARNING_DISTANCE = 68.0;
    public static final int RECALL_COUNTDOWN_TICKS = 100;

    private ManorTeleportRules() {
    }

    public static boolean isSummoner(UUID summonerId, UUID playerId) {
        return summonerId != null && summonerId.equals(playerId);
    }

    public static boolean isParticipant(Set<UUID> participants, UUID playerId) {
        return participants != null && playerId != null && participants.contains(playerId);
    }

    public static Recall recall(boolean sameDimension, double distanceToBossSquared, double playerToAnchorSquared, double bossToAnchorSquared, boolean noTarget) {
        if (!sameDimension) {
            return Recall.PLAYER_TO_BOSS;
        }
        if (distanceToBossSquared <= 5776.0 && !noTarget) {
            return Recall.NONE;
        }
        return playerToAnchorSquared > bossToAnchorSquared ? Recall.PLAYER_TO_BOSS : Recall.BOSS_TO_PLAYER;
    }

    public static enum Recall {
        NONE,
        PLAYER_TO_BOSS,
        BOSS_TO_PLAYER;

    }
}

