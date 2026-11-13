import hashlib
import json
import os
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parent


def extract(source, signature):
    start = source.index(signature)
    opening = source.index('{', start)
    depth = 1
    end = opening + 1
    while depth:
        depth += (source[end] == '{') - (source[end] == '}')
        end += 1
    return source[start:end]


def main():
    source_path = ROOT / 'src/main/java/cn/ft/biomedungeon/runtime/UnifiedEngine.java'
    source = source_path.read_text(encoding='utf-8')
    methods = {name: extract(source, signature) for name, signature in [('pause', 'String pause(String id)'), ('commit', 'String commit(String id,')]}
    prefix = '''import java.io.IOException;
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
'''
    suffix = '''
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
'''
    folder = ROOT / 'io-probe'
    folder.mkdir(exist_ok=True)
    target = folder / 'ActualCommandIoProbe.java'
    target.write_text(prefix + '\n'.join(methods.values()) + suffix, encoding='utf-8')
    (folder / 'extracted-methods.json').write_text(json.dumps({'sourceSha256': hashlib.sha256(source_path.read_bytes()).hexdigest(),
        'methodSha256': {name: hashlib.sha256(text.encode()).hexdigest() for name, text in methods.items()},
        'scope': 'Exact actual method control flow; I/O dependency injected, not Forge ticket/world simulation'}, indent=2), encoding='utf-8')
    java = Path(os.environ['APPDATA']) / '.minecraft/runtime/java-runtime-gamma-snapshot/bin'
    with (folder / 'result.log').open('w', encoding='utf-8') as output:
        subprocess.run([str(java / 'javac.exe'), '--release', '17', '-encoding', 'UTF-8', str(target)], check=True, stdout=output, stderr=subprocess.STDOUT)
        subprocess.run([str(java / 'java.exe'), '-cp', str(folder), 'ActualCommandIoProbe'], check=True, stdout=output, stderr=subprocess.STDOUT)
    print((folder / 'result.log').read_text(encoding='utf-8'))


if __name__ == '__main__':
    main()
