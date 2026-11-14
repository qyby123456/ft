package net.farmingtales.ftboss.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class BossMusicPlayback<Sound> {
    public interface Backend<Sound> {
        Sound create(String track);
        void play(Sound sound);
        void stop(Sound sound);
        boolean active(Sound sound);
    }

    private static final int SWITCH_DELAY_TICKS = 2;
    private static final int STARTUP_TIMEOUT_TICKS = 100;
    private static final int HISTORY_LIMIT = 128;
    private final Backend<Sound> backend;
    private final Set<String> heard = new LinkedHashSet<>();
    private long revision = -1;
    private long clock;
    private long readyAt;
    private long startedAt;
    private String encounter;
    private List<String> playlist = List.of();
    private int next;
    private volatile Sound current;
    private boolean confirmed;

    public BossMusicPlayback(Backend<Sound> backend) { this.backend = backend; }

    public boolean accept(long incomingRevision, String incomingEncounter, List<String> tracks) {
        if (incomingRevision <= revision) return false;
        revision = incomingRevision;
        List<String> unique = tracks.stream().distinct().limit(3).toList();
        if (Objects.equals(encounter, incomingEncounter) && playlist.equals(unique)) return false;
        stopCurrent();
        encounter = incomingEncounter;
        playlist = unique;
        next = 0;
        readyAt = clock + SWITCH_DELAY_TICKS;
        return true;
    }

    public void tick() {
        clock++;
        if (current != null) {
            if (backend.active(current)) confirmed = true;
            else if (confirmed || clock - startedAt >= STARTUP_TIMEOUT_TICKS) {
                stopCurrent();
                readyAt = clock + SWITCH_DELAY_TICKS;
            }
            return;
        }
        if (clock < readyAt) return;
        while (next < playlist.size()) {
            String track = playlist.get(next++);
            String key = encounter + "/" + track;
            if (!heard.add(key)) continue;
            while (heard.size() > HISTORY_LIMIT) heard.remove(heard.iterator().next());
            current = backend.create(track);
            if (current == null) continue;
            confirmed = false;
            startedAt = clock;
            backend.play(current);
            return;
        }
    }

    public boolean owns(Sound sound) { return current != null && current == sound; }

    public boolean busy() { return current != null || next < playlist.size(); }

    private void stopCurrent() {
        Sound previous = current;
        current = null;
        confirmed = false;
        if (previous != null) backend.stop(previous);
    }

    public void suspend() {
        stopCurrent();
        playlist = List.of();
        next = 0;
    }

    public void clear() {
        suspend();
        heard.clear();
        encounter = null;
        revision = -1;
        clock = 0;
    }
}
