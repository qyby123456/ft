import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def extract(source, signature):
    start = source.index(signature)
    opening = source.index("{", start)
    depth = 1
    end = opening + 1
    while depth:
        depth += (source[end] == "{") - (source[end] == "}")
        end += 1
    return source[start:end]


def main():
    source = (ROOT / "snapshot/main/java/cn/ft/biomedungeon/runtime/UnifiedEngine.java").read_text(encoding="utf-8")
    pause = extract(source, "String pause(String id)")
    commit = extract(source, "String commit(String id,")
    prefix = '''import java.io.IOException;
public final class CommandFailureReviewProbe {
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
    Job get(String id) { return job; }
    String status(String id) { return "stub-status"; }
    void requireReady() {}
    String verifyReceipt(Job value, String hash) { return hash; }
    void build(String id, String confirmation) { running = job; job.tickets = 1; }
    void persist(Job value) throws IOException { throw new IOException("injected journal write failure"); }
    void release(Job value) { releaseCalls++; value.tickets = 0; }
'''
    suffix = '''
    public static void main(String[] arguments) throws Exception {
        CommandFailureReviewProbe pauseProbe = new CommandFailureReviewProbe();
        pauseProbe.running = pauseProbe.job;
        pauseProbe.job.tickets = 1;
        try { pauseProbe.pause("job"); throw new AssertionError("Expected injected IOException"); }
        catch (IOException expected) {
            if (pauseProbe.running != pauseProbe.job || pauseProbe.job.tickets != 1 || pauseProbe.releaseCalls != 0) {
                throw new AssertionError("Pause branch changed; re-review finding");
            }
        }
        System.out.println("PAUSE_IO_EXCEPTION: running retained, ticket retained, releaseCalls=0");
        CommandFailureReviewProbe commitProbe = new CommandFailureReviewProbe();
        try {
            commitProbe.commit("job", "accepted-hash-stub", "CONFIRM_INDEPENDENT_EVIDENCE_COMMIT");
            throw new AssertionError("Expected injected IOException");
        } catch (IOException expected) {
            if (commitProbe.running != commitProbe.job || !commitProbe.job.commitRequested
                    || commitProbe.job.tickets != 1 || commitProbe.releaseCalls != 0) {
                throw new AssertionError("Commit branch changed; re-review finding");
            }
        }
        System.out.println("COMMIT_IO_EXCEPTION: running retained, commitRequested=true, ticket retained, releaseCalls=0");
        System.out.println("SOURCE_EXTRACTED_CONTROL_FLOW_ONLY: stubbed dependencies; no Forge or world execution");
    }
}
'''
    target = ROOT / "probe/CommandFailureReviewProbe.java"
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(prefix + pause + "\n" + commit + suffix, encoding="utf-8")
    records = [{"signature": name, "sha256": hashlib.sha256(text.encode()).hexdigest()}
               for name, text in (("pause", pause), ("commit", commit))]
    (ROOT / "probe/extracted-methods.json").write_text(json.dumps(records, indent=2) + "\n", encoding="utf-8")
    print("Extracted original pause/commit method text unchanged; dependencies stubbed, injected persist IOException")


if __name__ == "__main__":
    main()
