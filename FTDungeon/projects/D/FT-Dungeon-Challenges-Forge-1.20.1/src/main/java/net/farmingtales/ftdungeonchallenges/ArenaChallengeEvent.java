package net.farmingtales.ftdungeonchallenges;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.Event;

public abstract class ArenaChallengeEvent extends Event {
    private final ServerLevel level;
    private final ArenaChallengeApi.Snapshot challenge;

    protected ArenaChallengeEvent(ServerLevel level, ArenaChallengeApi.Snapshot challenge) {
        this.level = level;
        this.challenge = challenge;
    }

    public ServerLevel level() { return level; }
    public ArenaChallengeApi.Snapshot challenge() { return challenge; }

    public static final class Started extends ArenaChallengeEvent {
        public Started(ServerLevel level, ArenaChallengeApi.Snapshot challenge) { super(level, challenge); }
    }

    public static final class WaveStarted extends ArenaChallengeEvent {
        public WaveStarted(ServerLevel level, ArenaChallengeApi.Snapshot challenge) { super(level, challenge); }
    }

    public static final class Finished extends ArenaChallengeEvent {
        private final ArenaChallengeApi.EndReason reason;

        public Finished(ServerLevel level, ArenaChallengeApi.Snapshot challenge, ArenaChallengeApi.EndReason reason) {
            super(level, challenge);
            this.reason = reason;
        }

        public ArenaChallengeApi.EndReason reason() { return reason; }
    }
}
