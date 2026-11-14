package net.farmingtales.ftboss.util;

public final class QizaiBarrageRules {
    public static final String ID = "qizai_laser_barrage";
    public static final int MANA = 600;
    public static final int COOLDOWN_SECONDS = 90;
    public static final int MAX_CASTS = 8;
    public static final int ROUND_TICKS = QizaiLaserBarrageRules.roundDuration(QizaiLaserBarrageRules.ACTIVE_TICKS,
            QizaiLaserBarrageRules.WARNING_TICKS, QizaiLaserBarrageRules.BEAM_FADE);
    public static final int DURATION = QizaiLaserBarrageRules.castDuration(QizaiLaserBarrageRules.ACTIVE_TICKS,
            QizaiLaserBarrageRules.WARNING_TICKS, QizaiLaserBarrageRules.BEAM_FADE, QizaiLaserBarrageRules.RECOVERY);

    private QizaiBarrageRules() {}

    public static int spawnTick(int round, int index) {
        return round * ROUND_TICKS + QizaiLaserBarrageRules.roundSpawnTick(index,
                QizaiLaserBarrageRules.ACTIVE_TICKS, QizaiLaserBarrageRules.roundBeamCount(round));
    }

    public static int totalBeams() {
        int count = 0;
        for (int round = 0; round < QizaiLaserBarrageRules.ROUNDS; round++) count += QizaiLaserBarrageRules.roundBeamCount(round);
        return count;
    }
}
