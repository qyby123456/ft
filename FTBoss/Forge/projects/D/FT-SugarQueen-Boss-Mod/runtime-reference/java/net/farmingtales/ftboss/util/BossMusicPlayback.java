/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class BossMusicPlayback<Sound> {
    private static final int SWITCH_DELAY_TICKS = 2;
    private static final int STARTUP_TIMEOUT_TICKS = 100;
    private static final int HISTORY_LIMIT = 128;
    private final Backend<Sound> backend;
    private final Set<String> heard = new LinkedHashSet<String>();
    private long revision = -1L;
    private long clock;
    private long readyAt;
    private long startedAt;
    private String encounter;
    private List<String> playlist = List.of();
    private int next;
    private volatile Sound current;
    private boolean confirmed;

    public BossMusicPlayback(Backend<Sound> backend) {
        this.backend = backend;
    }

    public boolean accept(long incomingRevision, String incomingEncounter, List<String> tracks) {
        if (incomingRevision <= this.revision) {
            return false;
        }
        this.revision = incomingRevision;
        List unique = tracks.stream().distinct().limit(3L).toList();
        if (Objects.equals(this.encounter, incomingEncounter) && this.playlist.equals(unique)) {
            return false;
        }
        this.stopCurrent();
        this.encounter = incomingEncounter;
        this.playlist = unique;
        this.next = 0;
        this.readyAt = this.clock + 2L;
        return true;
    }

    public void tick() {
        ++this.clock;
        if (this.current != null) {
            if (this.backend.active(this.current)) {
                this.confirmed = true;
            } else if (this.confirmed || this.clock - this.startedAt >= 100L) {
                this.stopCurrent();
                this.readyAt = this.clock + 2L;
            }
            return;
        }
        if (this.clock < this.readyAt) {
            return;
        }
        while (this.next < this.playlist.size()) {
            String track;
            String key;
            if (!this.heard.add(key = this.encounter + "/" + (track = this.playlist.get(this.next++)))) continue;
            while (this.heard.size() > 128) {
                this.heard.remove(this.heard.iterator().next());
            }
            this.current = this.backend.create(track);
            if (this.current == null) continue;
            this.confirmed = false;
            this.startedAt = this.clock;
            this.backend.play(this.current);
            return;
        }
    }

    public boolean owns(Sound sound) {
        return this.current != null && this.current == sound;
    }

    public boolean busy() {
        return this.current != null || this.next < this.playlist.size();
    }

    private void stopCurrent() {
        Sound previous = this.current;
        this.current = null;
        this.confirmed = false;
        if (previous != null) {
            this.backend.stop(previous);
        }
    }

    public void suspend() {
        this.stopCurrent();
        this.playlist = List.of();
        this.next = 0;
    }

    public void clear() {
        this.suspend();
        this.heard.clear();
        this.encounter = null;
        this.revision = -1L;
        this.clock = 0L;
    }

    public static interface Backend<Sound> {
        public Sound create(String var1);

        public void play(Sound var1);

        public void stop(Sound var1);

        public boolean active(Sound var1);
    }
}

