import java.io.IOException;
public final class IoFixCommandProbe {
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
    int releaseCalls;
    int persistCalls;
    int failMode = 1;
    String loadFailure;
    Job get(String id) { return job; }
    String status(String id) { return "stub-status"; }
    void requireReady() { TemplateCatalog.require(loadFailure == null, "runtime disabled"); }
    String verifyReceipt(Job value, String hash) { return hash; }
    void build(String id, String confirmation) { running = job; job.tickets = 1; }
    void persist(Job value) throws IOException { persistCalls++; if (failMode == 1) { throw new IOException("injected journal write failure"); } if (failMode == 2) { throw new IllegalStateException("injected journal encoding failure"); } }
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
    static int checks;
    static void check(boolean value, String reason) {
        checks++;
        if (!value) { throw new AssertionError(reason); }
    }
    static void disabled(IoFixCommandProbe probe) {
        check(probe.loadFailure != null, "Runtime must disable after persistence failure");
        try { probe.requireReady(); throw new AssertionError("Disabled runtime admitted work"); }
        catch (IllegalArgumentException expected) { checks++; }
    }
    static void cleaned(IoFixCommandProbe probe) {
        check(probe.running == null, "Active task must clear");
        check(probe.job.tickets == 0, "Tickets must clear");
        check(probe.releaseCalls == 1, "Release exactly once");
        check(!probe.job.commitRequested, "Commit intent must clear");
        check(probe.job.receiptHash.isEmpty(), "Receipt must clear");
        check(probe.persistCalls == 1, "Cleanup must not depend on a second journal write");
    }
    public static void main(String[] arguments) throws Exception {
        IoFixCommandProbe pauseProbe = new IoFixCommandProbe();
        pauseProbe.running = pauseProbe.job;
        pauseProbe.job.tickets = 3;
        pauseProbe.job.commitRequested = true;
        pauseProbe.job.receiptHash = "prior-receipt";
        try { pauseProbe.pause("job"); throw new AssertionError("Expected injected IOException"); }
        catch (IOException expected) { checks++; }
        cleaned(pauseProbe);
        disabled(pauseProbe);
        System.out.println("PAUSE_IO_FAILURE_PASS running=null tickets=0 intent=false receipt=empty disabled=true");
        for (int mode : new int[]{1, 2}) {
            IoFixCommandProbe commitProbe = new IoFixCommandProbe();
            commitProbe.failMode = mode;
            try {
                commitProbe.commit("job", "accepted-hash-stub", "CONFIRM_INDEPENDENT_EVIDENCE_COMMIT");
                throw new AssertionError("Expected injected persistence failure");
            } catch (IOException | IllegalStateException expected) { checks++; }
            cleaned(commitProbe);
            disabled(commitProbe);
            System.out.println("COMMIT_FAILURE_PASS mode=" + mode + " running=null tickets=0 intent=false receipt=empty disabled=true");
        }
        IoFixCommandProbe successPause = new IoFixCommandProbe();
        successPause.failMode = 0;
        successPause.running = successPause.job;
        successPause.job.tickets = 3;
        successPause.job.commitRequested = true;
        successPause.job.receiptHash = "prior-receipt";
        successPause.pause("job");
        cleaned(successPause);
        check(successPause.loadFailure == null, "Normal pause must remain enabled");
        System.out.println("PAUSE_SUCCESS_PASS cleanup=true disabled=false");
        IoFixCommandProbe idlePause = new IoFixCommandProbe();
        idlePause.pause("job");
        check(idlePause.persistCalls == 0 && idlePause.releaseCalls == 0 && idlePause.loadFailure == null, "Idle pause unchanged");
        System.out.println("PAUSE_IDLE_PASS no persistence or release");
        IoFixCommandProbe successCommit = new IoFixCommandProbe();
        successCommit.failMode = 0;
        String admitted = successCommit.commit("job", "accepted-hash-stub", "CONFIRM_INDEPENDENT_EVIDENCE_COMMIT");
        check(admitted.contains("NOT COMMITTED"), "Admission must still require final readback");
        check(successCommit.running == successCommit.job && successCommit.job.tickets == 1, "Normal commit keeps scheduled work");
        check(successCommit.job.commitRequested && successCommit.job.receiptHash.equals("accepted-hash-stub"), "Normal admission preserves intent and receipt");
        check(successCommit.releaseCalls == 0 && successCommit.persistCalls == 1 && successCommit.loadFailure == null, "Normal admission no premature cleanup");
        System.out.println("COMMIT_SUCCESS_PASS scheduled=true intent=true prematureRelease=false NOT_COMMITTED");
        IoFixCommandProbe rejected = new IoFixCommandProbe();
        try { rejected.commit("job", "hash-stub", "WRONG_CONFIRMATION"); throw new AssertionError("Expected rejection"); }
        catch (IllegalArgumentException expected) { checks++; }
        check(rejected.running == null && rejected.job.tickets == 0 && !rejected.job.commitRequested
                && rejected.persistCalls == 0 && rejected.releaseCalls == 0, "Precondition rejection must not acquire or persist");
        System.out.println("CONFIRMATION_REJECTION_PASS no activity or persistence");
        System.out.println("IOFIX_SOURCE_EXTRACTED_CONTROL_FLOW_PASS checks=" + checks + " cases=7 ForgeWorldTests=NOT_RUN actualCOMMITTED=NOT_EXECUTED");
    }
}
