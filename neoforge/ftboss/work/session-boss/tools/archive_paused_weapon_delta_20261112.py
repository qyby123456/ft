import datetime
import hashlib
import json
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parents[3]
WEAPON = ROOT / "work/session-lease"
BOSS = ROOT / "work/session-boss"
BASE = BOSS / "archives/weapons-paused-a08a7373"
OUTPUT = BOSS / "archives/weapons-paused-dd1ad841-20261112"

def load(path):
    return json.loads(Path(path).read_text(encoding="utf-8-sig"))

def digest(path):
    checksum = hashlib.sha256()
    with Path(path).open("rb") as stream:
        for block in iter(lambda: stream.read(1048576), b""):
            checksum.update(block)
    return checksum.hexdigest()

def save(path, value):
    with Path(path).open("x", encoding="utf-8") as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)

assert not OUTPUT.exists()
base_receipt = load(BASE / "archive-receipt.json")
assert base_receipt["failures"] == 0
for filename, key in [("weapons-source-evidence.zip", "archiveSha256"), ("manifest.jsonl", "manifestSha256"), ("external-dependencies.jsonl", "externalDependencyManifestSha256"), ("RESTORE.md", "restoreInstructionsSha256")]:
    assert digest(BASE / filename) == base_receipt[key], filename
base_index = {}
with (BASE / "manifest.jsonl").open(encoding="utf-8") as stream:
    for line in stream:
        entry = json.loads(line)
        if "workspaceRelativePath" in entry:
            base_index[entry["workspaceRelativePath"]] = entry["sha256"]
candidate_path = WEAPON / "reports/native-repair-ranged-use-candidate-dd1ad841.json"
candidate = load(candidate_path)
assert candidate["classCount"] == 1381 and candidate["failures"] == 0
for name, wanted in candidate["inputHashes"].items():
    assert digest(name) == wanted, name
attempt = Path(candidate["attempt"])
runtime_reports = [
    "native-repair-runtime-passive-root-signed-a08a7373.json",
    "native-repair-runtime-passive-root-completion-diagnostic-a08a7373.json",
    "native-repair-runtime-passive-root-completion-diagnostic2-a08a7373.json",
    "native-repair-runtime-passive-root-signed-dd1ad841.json",
    "native-repair-runtime-publishing-binding-diagnostic-dd1ad841.json",
    "native-repair-runtime-attribute-returns-dd1ad841.json"
]
selected = {Path(__file__), ROOT / "AGENTS.md", ROOT / "WRITE_OWNER.json", ROOT / "WRITE_OWNER.claim", WEAPON / "CURRENT_INCREMENTAL_BATCH.json", BOSS / "CURRENT_BOSS_BATCH.json"}
runtime_rows = []
for filename in runtime_reports:
    receipt_path = WEAPON / "reports" / filename
    receipt = load(receipt_path)
    runtime = Path(receipt["attempt"])
    terminal = load(runtime / "terminal308.json")
    assert terminal["exitCode"] == 0 and terminal["stopSent"] and terminal["completeSave"]
    for name, wanted in receipt["inputHashes"].items():
        assert digest(name) == wanted, name
    selected.add(receipt_path)
    selected.update(path for path in runtime.iterdir() if path.is_file())
    for relative in ["server/actual-storage-view308.json", "server/logs/latest.log", "server/logs/debug.log"]:
        path = runtime / relative
        if path.is_file(): selected.add(path)
    runtime_rows.append({"receipt": str(receipt_path), "attempt": str(runtime), "observerFailures": receipt["probe"]["failures"], "javaExitCode": 0, "completeSave": True})
for folder in ["tools", "reports", "tests", "proposal-native"]:
    for path in (WEAPON / folder).rglob("*"):
        if not path.is_file() or "__pycache__" in path.parts: continue
        relative = path.relative_to(ROOT).as_posix()
        if base_index.get(relative) != digest(path): selected.add(path)
for folder in [attempt, WEAPON / "build/publishing-success-proof-dd1ad841", WEAPON / "build/attribute-return-bytecode-dd1ad841"]:
    selected.update(path for path in folder.rglob("*") if path.is_file())
preflight = load(WEAPON / "reports/goal-resume-20261112-preflight.json")
formal_hashes = {}
for name, wanted in preflight["formalJarHashesBefore"].items():
    formal_hashes[name] = digest(name)
    assert formal_hashes[name] == wanted, "Formal instance changed"
OUTPUT.mkdir(parents=True)
base_snapshot = Path(load(BOSS / "CURRENT_BOSS_BATCH.json")["weaponSnapshotJar"])
snapshot = OUTPUT / "ftboss-weapons-1.21.1-PAUSED-SNAPSHOT-dd1ad841.jar"
replaced = []
with zipfile.ZipFile(base_snapshot) as before, zipfile.ZipFile(snapshot, "x", compression=zipfile.ZIP_DEFLATED) as after:
    assert "META-INF/neoforge.mods.toml" not in before.namelist()
    for entry in before.infolist():
        payload = before.read(entry.filename)
        if entry.filename in ["frozen-classes/net/farmingtales/ftboss/item/FoodWeaponFinalDamage.class", "frozen-classes/net/farmingtales/ftboss/item/NativeCapabilityInstallationProof170.class"]:
            payload = (attempt / "complete-classes" / entry.filename.removeprefix("frozen-classes/")).read_bytes()
            replaced.append(entry.filename)
        after.writestr(entry, payload)
    assert len(replaced) == 2
    after.writestr("PAUSED_STATE.json", json.dumps({"weaponDevelopmentPaused": True, "candidate": "dd1ad841", "installable": False, "runnableAddonAccepted": False, "completeSignedSessionAccepted": False, "recoverySnapshotOnly": True, "resumeOnlyOnExplicitUserInstruction": True}, indent=2))
with zipfile.ZipFile(base_snapshot) as before, zipfile.ZipFile(snapshot) as after:
    for name in before.namelist():
        expected = (attempt / "complete-classes" / name.removeprefix("frozen-classes/")).read_bytes() if name in replaced else before.read(name)
        assert after.read(name) == expected
    assert after.testzip() is None
selected.add(snapshot)
restore = OUTPUT / "RESTORE.md"
restore.write_text('# Paused weapon module delta checkpoint\n\nCurrent user instruction: weapon development and candidate validation are paused; Boss migration has priority. The earlier RESUMED_GOAL note is historical and does not authorize resumption.\n\nThis is a supplement to the frozen a08a7373 checkpoint. Verify both archive receipts and all manifests, then restore the base followed by this delta into a NEW empty scratch workspace, checking each blob SHA256, size and destination containment. Never overwrite historical D inputs or either formal instance. Existing historical worlds, mods, libraries and external dependencies remain at their original locations and must be verified from preserved receipts before reuse. They are referenced, not recopied.\n\nCandidate dd1ad841 restores one verified original successful-publication assignment. Compilation and directed ASM passed, but all six resumed complete Session probes remain failures. No real authenticated combat, nineteen-chain or restart acceptance is inferred. Read-only debugger runs were diagnostic only; the final debugger was stopped for this user-requested pause, native JVM completed its original assertions and saved normally. No authority fields were written. No product JVM was restarted.\n\nThe separate weapons JAR stores frozen classes under frozen-classes/ and has no mod entrypoint: it is a recovery snapshot, NOT an accepted runnable or installable addon. Boss-core remains independent of this paused file. Preserve sources and all failed evidence. Do not resume weapon fixes or tests until the user explicitly requests it. No originals were removed, moved or deployed.\n', encoding="utf-8")
selected.add(restore)
archive = OUTPUT / "weapons-source-evidence-delta.zip"
manifest = OUTPUT / "manifest.jsonl"
stored = set()
raw_bytes = 0
with zipfile.ZipFile(archive, "x", compression=zipfile.ZIP_DEFLATED, compresslevel=6) as bundle, manifest.open("x", encoding="utf-8") as stream:
    for path in sorted(selected, key=lambda value: str(value)):
        assert path.resolve().is_relative_to(ROOT) and path.is_file() and not path.is_symlink()
        checksum = digest(path)
        member = "blobs/" + checksum
        if checksum not in stored:
            bundle.write(path, member)
            stored.add(checksum)
        size = path.stat().st_size
        raw_bytes += size
        stream.write(json.dumps({"workspaceRelativePath": path.relative_to(ROOT).as_posix(), "archiveMember": member, "bytes": size, "sha256": checksum}, ensure_ascii=False) + chr(10))
with zipfile.ZipFile(archive) as bundle:
    for member in bundle.namelist():
        assert hashlib.sha256(bundle.read(member)).hexdigest() == member.removeprefix("blobs/")
receipt = {"failures": 0, "createdAt": datetime.datetime.now(datetime.timezone.utc).isoformat(), "weaponDevelopmentPaused": True, "baseArchiveReceipt": str(BASE / "archive-receipt.json"), "baseArchiveSha256": base_receipt["archiveSha256"], "baseArchiveAndManifestsReverified": True, "archive": str(archive), "archiveSha256": digest(archive), "archiveBytes": archive.stat().st_size, "manifestSha256": digest(manifest), "restoreInstructionsSha256": digest(restore), "files": len(selected), "uniqueBlobs": len(stored), "rawBytes": raw_bytes, "allArchivedBlobsReadBackAndHashVerified": True, "snapshotJar": str(snapshot), "snapshotJarSha256": digest(snapshot), "snapshotOriginalEntriesPreservedExceptTwoVerifiedClassUpdates": True, "weaponStandaloneRunnableJarAccepted": False, "fullSignedSessionAccepted": False, "installable": False, "runtimeRows": runtime_rows, "formalJarHashesUnchanged": formal_hashes, "deploymentPerformed": False, "originalFilesRemovedOrMoved": False, "scratchRetainedWithoutDeletionBypass": True}
save(OUTPUT / "archive-receipt.json", receipt)
print(json.dumps({"archive": str(archive), "files": len(selected), "uniqueBlobs": len(stored), "archiveBytes": archive.stat().st_size, "paused": True, "installable": False}))
