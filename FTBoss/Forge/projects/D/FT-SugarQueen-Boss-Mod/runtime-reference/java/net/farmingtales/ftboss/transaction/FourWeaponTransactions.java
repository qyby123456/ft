/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class FourWeaponTransactions {
    private final Host host;
    private Request executing;
    private final UUID epoch = UUID.randomUUID();
    private final Map<UUID, Session> sessions = new HashMap<UUID, Session>();
    private final Map<UUID, Map<UUID, Saved>> replies = new HashMap<UUID, Map<UUID, Saved>>();
    private final Map<UUID, Long> sequences = new HashMap<UUID, Long>();
    private final int requestLimit;

    public static boolean menuOperation(Operation operation) {
        return operation.ordinal() >= Operation.MENU_PICKUP.ordinal();
    }

    public FourWeaponTransactions(Host host, int n) {
        this.host = Objects.requireNonNull(host);
        if (n < 1) {
            throw new IllegalArgumentException("limit");
        }
        this.requestLimit = n;
    }

    private static void text(String string) {
        if (string == null || string.isBlank() || string.length() > 256) {
            throw new IllegalArgumentException("invalid text");
        }
    }

    private Version version(State state, long l) {
        return new Version(this.epoch, l, state.cavity(), state.repair(), state.evolution(), state.fingerprint(), state.rulesFingerprint());
    }

    private boolean sameSource(Session session, State state) {
        return state != null && session.opened.binding().equals(state.binding()) && session.opened.principal() == state.principal() && session.opened.source() == state.source();
    }

    private Reply replay(UUID uUID, UUID uUID2, Object object) {
        Saved saved = (Saved)this.replies.getOrDefault(uUID, Map.of()).get(uUID2);
        return saved == null ? null : (saved.payload().equals(object) ? saved.reply() : new Reply(uUID2, Code.REQUEST_CONFLICT, null));
    }

    private boolean full(UUID uUID) {
        return this.replies.getOrDefault(uUID, Map.of()).size() >= this.requestLimit;
    }

    private Reply save(UUID uUID2, Object object, Reply reply) {
        this.replies.computeIfAbsent(uUID2, uUID -> new HashMap()).put(reply.request(), new Saved(object, reply));
        return reply;
    }

    private void forget(UUID uUID, Request request) {
        Saved saved;
        Map<UUID, Saved> map = this.replies.get(uUID);
        if (map != null && (saved = map.get(request.request())) != null && saved.payload().equals(request)) {
            map.remove(request.request());
        }
    }

    private void rollbackAfterFailure(Mutation mutation, Throwable throwable) {
        block3: {
            if (mutation == null) {
                return;
            }
            try {
                mutation.rollback();
            }
            catch (Error | RuntimeException throwable2) {
                if (throwable2 == throwable) break block3;
                throwable.addSuppressed(throwable2);
            }
        }
    }

    private State read(UUID uUID, Binding binding) {
        if (!uUID.equals(binding.owner())) {
            return null;
        }
        try {
            State state = this.host.read(uUID, binding);
            return state != null && binding.equals(state.binding()) ? state : null;
        }
        catch (RuntimeException runtimeException) {
            return null;
        }
    }

    private long sequence(State state) {
        long l = this.sequences.getOrDefault(state.binding().weapon(), -1L);
        if (l == Long.MAX_VALUE) {
            return -1L;
        }
        long l2 = l + 1L;
        this.sequences.put(state.binding().weapon(), l2);
        return l2;
    }

    private boolean currentSequence(Session session) {
        return session.version.sequence() == this.sequences.getOrDefault(session.opened.binding().weapon(), -1L).longValue();
    }

    private boolean rollback(Mutation mutation) {
        try {
            mutation.rollback();
            return true;
        }
        catch (RuntimeException runtimeException) {
            return false;
        }
    }

    public synchronized Reply open(UUID uUID, Open open) {
        Reply reply = this.replay(uUID, open.request(), open);
        if (reply != null) {
            return reply;
        }
        if (this.full(uUID)) {
            return new Reply(open.request(), Code.LIMIT, null);
        }
        State state = this.read(uUID, open.binding());
        if (state == null) {
            return this.save(uUID, open, new Reply(open.request(), Code.DENIED, null));
        }
        long l = this.sequence(state);
        if (l < 0L) {
            return this.save(uUID, open, new Reply(open.request(), Code.LIMIT, null));
        }
        Session session = new Session(state, this.version(state, l));
        this.sessions.put(uUID, session);
        return this.save(uUID, open, new Reply(open.request(), Code.OPENED, session.snapshot()));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public synchronized Reply submit(UUID uUID, Request request) {
        Reply reply = this.replay(uUID, request.request(), request);
        if (reply != null) {
            return reply;
        }
        if (this.full(uUID)) {
            return new Reply(request.request(), Code.LIMIT, null);
        }
        Session session = this.sessions.get(uUID);
        if (!(session != null && session.id.equals(request.session()) && request.binding().equals(session.opened.binding()) && uUID.equals(request.binding().owner()))) {
            return this.save(uUID, request, new Reply(request.request(), Code.CLOSED, null));
        }
        State state = this.read(uUID, request.binding());
        if (!this.sameSource(session, state)) {
            this.sessions.remove(uUID);
            return this.save(uUID, request, new Reply(request.request(), Code.CLOSED, null));
        }
        if (!(this.currentSequence(session) && session.version.equals(this.version(state, session.version.sequence())) && session.allowed.equals(state.allowed()))) {
            this.sessions.remove(uUID);
            return this.save(uUID, request, new Reply(request.request(), Code.STALE, null));
        }
        if (!request.expected().equals(session.version) || !request.credential().equals(session.credential)) {
            return this.save(uUID, request, new Reply(request.request(), Code.STALE, null));
        }
        session.credential = UUID.randomUUID();
        if (!state.allowed().contains((Object)request.operation())) {
            return this.save(uUID, request, new Reply(request.request(), Code.DENIED, session.snapshot()));
        }
        if (session.version.sequence() == Long.MAX_VALUE) {
            this.sessions.remove(uUID);
            return this.save(uUID, request, new Reply(request.request(), Code.LIMIT, null));
        }
        Mutation mutation = null;
        try {
            boolean bl;
            mutation = this.host.prepare(uUID, request, state);
            if (mutation == null) {
                return this.save(uUID, request, new Reply(request.request(), Code.DENIED, session.snapshot()));
            }
            this.executing = request;
            try {
                bl = mutation.commit();
            }
            finally {
                this.executing = null;
            }
            if (!bl) {
                boolean bl2 = this.rollback(mutation);
                this.sessions.remove(uUID);
                return this.save(uUID, request, new Reply(request.request(), bl2 ? Code.DENIED : Code.FAILED, null));
            }
            State state2 = this.read(uUID, request.binding());
            if (!this.sameSource(session, state2) || state.fingerprint().equals(state2.fingerprint()) && !FourWeaponTransactions.menuOperation(request.operation()) && request.operation() != Operation.OPEN_CONTAINER) {
                this.rollback(mutation);
                this.sessions.remove(uUID);
                return this.save(uUID, request, new Reply(request.request(), Code.FAILED, null));
            }
            session.version = this.version(state2, this.sequence(state2));
            session.allowed = state2.allowed();
            Reply reply2 = this.save(uUID, request, new Reply(request.request(), Code.COMMITTED, session.snapshot()));
            if (mutation instanceof TerminalMutation) {
                TerminalMutation terminalMutation = (TerminalMutation)mutation;
                terminalMutation.terminalCommit(state2);
            }
            return reply2;
        }
        catch (RuntimeException runtimeException) {
            this.sessions.remove(uUID);
            this.forget(uUID, request);
            this.rollbackAfterFailure(mutation, runtimeException);
            return this.save(uUID, request, new Reply(request.request(), Code.FAILED, null));
        }
        catch (Error error) {
            this.sessions.remove(uUID);
            this.forget(uUID, request);
            this.rollbackAfterFailure(mutation, error);
            throw error;
        }
    }

    public synchronized Snapshot authority(UUID uUID, UUID uUID2) {
        Session session = this.sessions.get(uUID);
        if (session == null || !session.id.equals(uUID2)) {
            return null;
        }
        State state = this.read(uUID, session.opened.binding());
        if (!(this.sameSource(session, state) && this.currentSequence(session) && session.version.equals(this.version(state, session.version.sequence())) && session.allowed.equals(state.allowed()))) {
            this.sessions.remove(uUID);
            return null;
        }
        return session.snapshot();
    }

    public synchronized Snapshot current(UUID uUID) {
        Session session = this.sessions.get(uUID);
        return session == null ? null : this.authority(uUID, session.id);
    }

    public synchronized boolean executing(UUID uUID, Operation operation) {
        return this.executing != null && this.executing.binding().owner().equals(uUID) && this.executing.operation() == operation;
    }

    public synchronized void close(UUID uUID, UUID uUID2) {
        Session session = this.sessions.get(uUID);
        if (session != null && session.id.equals(uUID2)) {
            this.sessions.remove(uUID);
        }
    }

    public synchronized void invalidate(UUID uUID) {
        this.sessions.remove(uUID);
    }

    public synchronized void validate(UUID uUID) {
        Session session = this.sessions.get(uUID);
        if (session != null) {
            this.authority(uUID, session.id);
        }
    }

    public synchronized void invalidateStatue(String string, long l, long l2) {
        this.sessions.values().removeIf(session -> session.opened.binding().dimension().equals(string) && (session.opened.binding().statue() == l || session.opened.binding().statue() == l2));
    }

    public synchronized void stop() {
        this.sessions.clear();
        this.replies.clear();
        this.sequences.clear();
    }

    public static enum Operation {
        REPLACE_PART,
        SELECT_SKILL,
        EVOLVE,
        START_REPAIR,
        SUBMIT_REPAIR,
        CLAIM_REPAIR,
        BUILD_CELL,
        REMOVE_CELL,
        OPEN_CONTAINER,
        MENU_PICKUP,
        MENU_QUICK_MOVE,
        MENU_SWAP,
        MENU_QUICK_CRAFT,
        MENU_PICKUP_ALL;

    }

    public static interface Host {
        public State read(UUID var1, Binding var2);

        public Mutation prepare(UUID var1, Request var2, State var3);
    }

    public record Version(UUID epoch, long sequence, int cavity, int repair, int evolution, String fingerprint, String rulesFingerprint) {
        public Version {
            Objects.requireNonNull(uUID);
            FourWeaponTransactions.text(string);
            FourWeaponTransactions.text(string2);
            if (l < 0L || n < 0 || n2 < 0 || n3 < 0 || n3 > 4) {
                throw new IllegalArgumentException("invalid composite version");
            }
        }
    }

    public record State(Binding binding, Object principal, Object source, int cavity, int repair, int evolution, String fingerprint, String rulesFingerprint, Set<Operation> allowed) {
        public State {
            Objects.requireNonNull(binding);
            Objects.requireNonNull(object);
            Objects.requireNonNull(object2);
            FourWeaponTransactions.text(string);
            FourWeaponTransactions.text(string2);
            set = Set.copyOf(set);
            if (n < 0 || n2 < 0 || n3 < 0 || n3 > 4) {
                throw new IllegalArgumentException("invalid state");
            }
        }
    }

    private static final class Session {
        private final UUID id = UUID.randomUUID();
        private final State opened;
        private UUID credential = UUID.randomUUID();
        private Version version;
        private Set<Operation> allowed;

        private Session(State state, Version version) {
            this.opened = state;
            this.version = version;
            this.allowed = state.allowed();
        }

        private Snapshot snapshot() {
            return new Snapshot(this.id, this.credential, this.opened.binding(), this.version, this.allowed);
        }
    }

    public record Binding(UUID owner, UUID weapon, String item, int hand, int inventorySlot, String dimension, long statue, String statueIdentity) {
        public Binding {
            Objects.requireNonNull(uUID);
            Objects.requireNonNull(uUID2);
            FourWeaponTransactions.text(string);
            FourWeaponTransactions.text(string2);
            FourWeaponTransactions.text(string3);
            if (n < 0 || n > 1 || n2 < 0 || n2 > 40 || n == 0 && n2 > 8 || n == 1 && n2 != 40) {
                throw new IllegalArgumentException("explicit held slot required");
            }
        }
    }

    private record Saved(Object payload, Reply reply) {
    }

    public record Reply(UUID request, Code code, Snapshot snapshot) {
        public Reply {
            Objects.requireNonNull(uUID);
            Objects.requireNonNull(code);
        }

        public boolean accepted() {
            return this.code == Code.COMMITTED;
        }
    }

    public static enum Code {
        OPENED,
        COMMITTED,
        DENIED,
        STALE,
        CLOSED,
        REQUEST_CONFLICT,
        LIMIT,
        FAILED;

    }

    public record Snapshot(UUID session, UUID credential, Binding binding, Version version, Set<Operation> allowed) {
        public Snapshot {
            Objects.requireNonNull(uUID);
            Objects.requireNonNull(uUID2);
            Objects.requireNonNull(binding);
            Objects.requireNonNull(version);
            set = Set.copyOf(set);
        }
    }

    public record Request(UUID request, UUID session, UUID credential, Binding binding, Version expected, Operation operation, int part, int value, String offeringFingerprint) {
        public Request {
            boolean bl;
            Objects.requireNonNull(uUID);
            Objects.requireNonNull(uUID2);
            Objects.requireNonNull(uUID3);
            Objects.requireNonNull(binding);
            Objects.requireNonNull(version);
            Objects.requireNonNull(operation);
            string = Objects.requireNonNull(string);
            if (n < -999 || n > 62 || n2 < 0 || n2 > 40 || string.length() > 256) {
                throw new IllegalArgumentException("out of range request");
            }
            switch (operation) {
                default: {
                    throw new IncompatibleClassChangeError();
                }
                case REPLACE_PART: {
                    boolean bl2;
                    if (n >= 0 && n < 3 && n2 < 36 && !string.isBlank()) {
                        bl2 = true;
                        break;
                    }
                    bl2 = false;
                    break;
                }
                case SELECT_SKILL: {
                    boolean bl2;
                    if (n == -1 && n2 < 8 && string.isEmpty()) {
                        bl2 = true;
                        break;
                    }
                    bl2 = false;
                    break;
                }
                case EVOLVE: {
                    boolean bl2;
                    if (n == -1 && n2 < 36 && !string.isBlank()) {
                        bl2 = true;
                        break;
                    }
                    bl2 = false;
                    break;
                }
                case SUBMIT_REPAIR: {
                    boolean bl2;
                    if (n == -1 && n2 < 3 && string.isEmpty()) {
                        bl2 = true;
                        break;
                    }
                    bl2 = false;
                    break;
                }
                case START_REPAIR: 
                case CLAIM_REPAIR: {
                    boolean bl2;
                    if (n == -1 && n2 == 0 && string.isEmpty()) {
                        bl2 = true;
                        break;
                    }
                    bl2 = false;
                    break;
                }
                case BUILD_CELL: {
                    boolean bl2;
                    if (n >= 0 && n < 27 && n2 < 36 && !string.isBlank()) {
                        bl2 = true;
                        break;
                    }
                    bl2 = false;
                    break;
                }
                case REMOVE_CELL: {
                    boolean bl2;
                    if (n >= 0 && n < 27 && n2 == 0 && string.isEmpty()) {
                        bl2 = true;
                        break;
                    }
                    bl2 = false;
                    break;
                }
                case OPEN_CONTAINER: {
                    boolean bl2;
                    if (n == -1 && n2 == 0 && string.isEmpty()) {
                        bl2 = true;
                        break;
                    }
                    bl2 = false;
                    break;
                }
                case MENU_PICKUP: 
                case MENU_PICKUP_ALL: {
                    boolean bl2;
                    if ((n >= 0 && n < 63 || n == -999) && n2 < 2 && string.isEmpty()) {
                        bl2 = true;
                        break;
                    }
                    bl2 = false;
                    break;
                }
                case MENU_QUICK_MOVE: {
                    boolean bl2;
                    if (n >= 0 && n < 63 && n2 < 2 && string.isEmpty()) {
                        bl2 = true;
                        break;
                    }
                    bl2 = false;
                    break;
                }
                case MENU_SWAP: {
                    boolean bl2;
                    if (n >= 0 && n < 63 && (n2 < 9 || n2 == 40) && string.isEmpty()) {
                        bl2 = true;
                        break;
                    }
                    bl2 = false;
                    break;
                }
                case MENU_QUICK_CRAFT: {
                    boolean bl2 = ((n2 & 3) == 1 ? n >= 0 && n < 63 : n == -999) && Set.of(Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(6)).contains(n2) && string.isEmpty() ? true : (bl = false);
                }
            }
            if (!bl) {
                throw new IllegalArgumentException("invalid operation arguments");
            }
        }
    }

    public static interface Mutation {
        public boolean commit();

        public void rollback();
    }

    public record Open(UUID request, Binding binding) {
        public Open {
            Objects.requireNonNull(uUID);
            Objects.requireNonNull(binding);
        }
    }

    public static interface TerminalMutation
    extends Mutation {
        public void terminalCommit(State var1);
    }
}

