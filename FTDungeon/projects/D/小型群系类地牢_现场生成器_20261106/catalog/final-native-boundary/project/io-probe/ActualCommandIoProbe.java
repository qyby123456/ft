import java.io.IOException;
public final class ActualCommandIoProbe {
    static final String CONFIRMATION = "CONFIRM_ISOLATED_AIRSPACE";
    static final class DungeonApi { enum Phase { VERIFYING } }
    static final class TemplateCatalog {
        static void require(boolean value, String reason) { if (!value) { throw new IllegalArgumentException(reason); } }
    }
    static final class Job {
        boolean commitRequested;
        String receiptHash = "";
        DungeonApi.Phase phase = DungeonApi.Phase.VERIFYING;
        String stage = "PAYLOAD_VERIFIED_ROUTE_INTERACTION_ACCEPTANCE_PENDING";
        int tickets;
    }
    final Job job = new Job();
    Job running;
    String loadFailure;
    int releaseCalls;
    int persistCalls;
    int buildCalls;
    boolean failPersist;
    boolean rejectReceipt;
    boolean sawCommitIntentAtPersist;
    static int assertions;
    Job get(String id) { return job; }
    String status(String id) { return "probe-status"; }
    void requireReady() { TemplateCatalog.require(loadFailure == null, "runtime disabled"); }
    String verifyReceipt(Job value, String hash) { TemplateCatalog.require(!rejectReceipt, "missing receipt"); return hash; }
    void build(String id, String confirmation) { buildCalls++; running = job; job.tickets = 3; }
    void persist(Job value) throws IOException {
        persistCalls++;
        sawCommitIntentAtPersist = value.commitRequested;
        if (failPersist) { throw new IOException("injected persistent journal I/O failure"); }
    }
    void release(Job value) { releaseCalls++; value.tickets = 0; }
String pause(String id) throws IOException {
        Job job = get(id);
        if (running == job) {
            job.commitRequested = false;
            job.receiptHash = "";
            try {
                persist(job);
            } catch (IOException exception) {
                loadFailure = "Pause journal persistence failed; runtime disabled: " + exception;
                throw exception;
            } finally {
                running = null;
                release(job);
            }
        }
        return "Paused without rollback. Use build with confirmation to revalidate and resume: " + status(id);
    }
String commit(String id, String evidenceSha256, String confirmation) throws Exception {
        requireReady();
        TemplateCatalog.require(confirmation.equals("CONFIRM_INDEPENDENT_EVIDENCE_COMMIT"), "Explicit commit confirmation required");
        Job job = get(id);
        TemplateCatalog.require(running == null && job.phase == DungeonApi.Phase.VERIFYING
                && job.stage.equals("PAYLOAD_VERIFIED_ROUTE_INTERACTION_ACCEPTANCE_PENDING"), "Commit requires idle payload-verified pending job");
        String receipt = verifyReceipt(job, evidenceSha256);
        build(id, CONFIRMATION);
        boolean admitted = false;
        try {
            job.receiptHash = receipt;
            job.commitRequested = true;
            persist(job);
            admitted = true;
        } catch (Exception exception) {
            job.commitRequested = false;
            job.receiptHash = "";
            loadFailure = "Commit journal persistence failed; runtime disabled: " + exception;
            throw exception;
        } finally {
            if (!admitted) {
                if (running == job) { running = null; }
                release(job);
            }
        }
        return "Independent evidence hash admitted by explicit operator command; full readback pending, NOT COMMITTED: " + id;
    }
    static void check(boolean condition, String description) {
        if (!condition) { throw new AssertionError(description); }
        assertions++;
    }
    public static void main(String[] arguments) throws Exception {
        ActualCommandIoProbe failedPause = new ActualCommandIoProbe();
        failedPause.running = failedPause.job;
        failedPause.job.tickets = 3;
        failedPause.job.commitRequested = true;
        failedPause.job.receiptHash = "previous";
        failedPause.failPersist = true;
        try { failedPause.pause("job"); throw new AssertionError("Expected pause IOException"); }
        catch (IOException expected) {}
        check(failedPause.running == null, "Pause failure clears running");
        check(failedPause.job.tickets == 0 && failedPause.releaseCalls == 1, "Pause failure releases tickets");
        check(!failedPause.job.commitRequested && failedPause.job.receiptHash.isEmpty(), "Pause failure revokes commit intent");
        check(failedPause.loadFailure != null, "Pause failure disables future commands");
        check(failedPause.persistCalls == 1, "Pause cleanup does not retry persist");
        try { failedPause.requireReady(); throw new AssertionError("Disabled runtime admitted work"); }
        catch (IllegalArgumentException expected) { assertions++; }
        ActualCommandIoProbe failedCommit = new ActualCommandIoProbe();
        failedCommit.failPersist = true;
        try { failedCommit.commit("job", "exact-hash-probe", "CONFIRM_INDEPENDENT_EVIDENCE_COMMIT"); throw new AssertionError("Expected commit IOException"); }
        catch (IOException expected) {}
        check(failedCommit.buildCalls == 1 && failedCommit.sawCommitIntentAtPersist, "Injection reached real post-build admission persist");
        check(failedCommit.running == null, "Rejected commit cannot remain running");
        check(failedCommit.job.tickets == 0 && failedCommit.releaseCalls == 1, "Rejected commit releases acquired tickets");
        check(!failedCommit.job.commitRequested && failedCommit.job.receiptHash.isEmpty(), "Rejected commit cannot later COMMIT");
        check(failedCommit.loadFailure != null && failedCommit.persistCalls == 1, "Commit disabled without another journal write");
        ActualCommandIoProbe paused = new ActualCommandIoProbe();
        paused.running = paused.job;
        paused.job.tickets = 3;
        paused.pause("job");
        check(paused.running == null && paused.job.tickets == 0 && paused.loadFailure == null, "Successful pause still works");
        ActualCommandIoProbe admitted = new ActualCommandIoProbe();
        admitted.commit("job", "exact-hash-probe", "CONFIRM_INDEPENDENT_EVIDENCE_COMMIT");
        check(admitted.running == admitted.job && admitted.job.tickets == 3, "Successful admission retains active verification");
        check(admitted.job.commitRequested && admitted.job.receiptHash.equals("exact-hash-probe"), "Successful explicit intent preserved");
        check(admitted.releaseCalls == 0 && admitted.loadFailure == null && admitted.persistCalls == 1, "Success avoids failure cleanup");
        ActualCommandIoProbe absent = new ActualCommandIoProbe();
        absent.rejectReceipt = true;
        try { absent.commit("job", "missing", "CONFIRM_INDEPENDENT_EVIDENCE_COMMIT"); throw new AssertionError("Missing receipt admitted"); }
        catch (IllegalArgumentException expected) {}
        check(absent.buildCalls == 0 && absent.running == null && absent.releaseCalls == 0, "Receipt rejection never starts work");
        check(absent.loadFailure == null && absent.persistCalls == 0, "Missing receipt is not an I/O shutdown");
        System.out.println("ACTUAL_EXTRACTED_COMMAND_IO_PASS assertions=" + assertions + " exactMethodBodies=true persistentIOException=true cleanupPersists=0 forgeWorldSimulation=false");
    }
}
