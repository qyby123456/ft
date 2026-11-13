import hashlib
import json
import os
import shutil
import subprocess
import sys
import zipfile
from pathlib import Path

root = Path.cwd().resolve()
work = Path(__file__).resolve().parents[1]
assert root == work.parents[1]
plan_path = Path(sys.argv[1]).resolve()
assert plan_path.is_relative_to(work)
plan = json.loads(plan_path.read_text(encoding="utf-8"))
assert plan["batchId"] == "configured-nineteen-passive-hit-root-publication"
owned = str(work).replace("'", "''")
command = "$rows=@(Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe','python.exe','pythonw.exe') -and $_.CommandLine -like '*" + owned + "*' }); ConvertTo-Json -InputObject @($rows | Select-Object ProcessId,Name) -Compress"
process = subprocess.run(["powershell", "-NoProfile", "-Command", command], cwd=root, capture_output=True, text=True)
assert process.returncode == 0
processes = json.loads(process.stdout or "[]")
assert not [row for row in processes if int(row["ProcessId"]) != os.getpid()], processes

def digest(path):
    checksum = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            checksum.update(block)
    return checksum.hexdigest()

for name in plan["terminalRuntimeReceipts"]:
    receipt = json.loads((root / name).read_text(encoding="utf-8"))
    attempt = Path(receipt["attempt"]).resolve()
    assert attempt.is_relative_to(work)
    terminal = json.loads((attempt / "terminal308.json").read_text(encoding="utf-8"))
    assert receipt["exitCode"] == terminal["exitCode"] == 0 and terminal["completeSave"] and terminal["stopSent"]
    for reference, expected in receipt["inputHashes"].items():
        path = Path(reference).resolve()
        assert path.is_file() and digest(path) == expected, path.name
output = (root / plan["archiveDirectory"]).resolve()
assert output.is_relative_to(work / "archives") and not output.exists()
output.mkdir(parents=True)
archive = output / "artifacts.zip"
manifest = output / "manifest.jsonl"
dependencies = output / "external-dependencies.jsonl"
visited = set()
stored = set()
files = 0
external_files = 0
raw_bytes = 0
excluded = [Path(name).resolve() for name in plan.get("externalDependencyDirectories", [])]
for directory in excluded:
    assert directory.is_relative_to(root) and directory.is_dir() and not directory.is_symlink()
dependency_paths = set()
with dependencies.open("x", encoding="utf-8") as dependency_stream:
    for directory in excluded:
        for path in directory.rglob("*"):
            if path.is_file():
                assert not path.is_symlink() and path.resolve().is_relative_to(directory)
                dependency_stream.write(json.dumps({"originalPath": str(path.resolve()), "bytes": path.stat().st_size,
                                                     "sha256": digest(path), "retainedAtOriginalLocation": True}, ensure_ascii=False) + "\n")
                dependency_paths.add(str(path.resolve()))
                external_files += 1
    evidence_paths = [Path(name) for name in plan["artifactRoots"] if Path(name).resolve() == Path(plan["batchEvidence"]).resolve()]
    assert len(evidence_paths) == 1
    evidence = json.loads(evidence_paths[0].read_text(encoding="utf-8"))
    assert evidence["failures"] == 0
    for name, expected in evidence["inputHashes"].items():
        path = Path(name).resolve()
        assert path.is_file() and not path.is_symlink() and digest(path) == expected, path.name
        if str(path) in dependency_paths:
            continue
        dependency_stream.write(json.dumps({"originalPath": str(path), "bytes": path.stat().st_size,
                                             "sha256": expected, "retainedAtOriginalLocation": True,
                                             "role": "verified frozen evidence dependency; may also be archived"}, ensure_ascii=False) + "\n")
        dependency_paths.add(str(path))
        external_files += 1
with manifest.open("x", encoding="utf-8") as manifest_stream, zipfile.ZipFile(archive, "x", compression=zipfile.ZIP_DEFLATED, compresslevel=1, allowZip64=True) as bundle:
    for name in [*plan["artifactRoots"], str(plan_path), str(Path(__file__))]:
        source = Path(name)
        if not source.is_absolute():
            source = root / source
        source = source.resolve()
        assert source.is_relative_to(root) and source.exists() and not source.is_symlink(), name
        paths = source.rglob("*") if source.is_dir() else [source]
        for path in paths:
            if not path.is_file() or any(path.is_relative_to(directory) for directory in excluded):
                continue
            assert not path.is_symlink() and path.resolve().is_relative_to(root)
            relative = path.relative_to(root).as_posix()
            if relative in visited:
                continue
            visited.add(relative)
            checksum = digest(path)
            member = "blobs/" + checksum
            size = path.stat().st_size
            if checksum not in stored:
                with path.open("rb") as original, bundle.open(member, "w", force_zip64=True) as compressed:
                    shutil.copyfileobj(original, compressed, length=1024 * 1024)
                stored.add(checksum)
            manifest_stream.write(json.dumps({"workspaceRelativePath": relative, "sha256": checksum,
                                               "bytes": size, "archiveMember": member}, ensure_ascii=False) + "\n")
            files += 1
            raw_bytes += size
    manifest_stream.flush()
    bundle.write(manifest, "manifest.jsonl")
    bundle.write(dependencies, "external-dependencies.jsonl")
    bundle.write(plan_path, "batch-plan.json")
with zipfile.ZipFile(archive) as bundle, manifest.open(encoding="utf-8") as manifest_stream:
    verified = set()
    for line in manifest_stream:
        record = json.loads(line)
        if record["archiveMember"] in verified:
            continue
        checksum = hashlib.sha256()
        size = 0
        with bundle.open(record["archiveMember"]) as stream:
            for block in iter(lambda: stream.read(1024 * 1024), b""):
                checksum.update(block)
                size += len(block)
        assert checksum.hexdigest() == record["sha256"] and size == record["bytes"]
        verified.add(record["archiveMember"])
restore = output / "RESTORE.md"
restore_text = "# Batch checkpoint restore\n\nThis is a bounded checkpoint archive, not an installable mod or full migration acceptance.\n\n1. Verify artifacts.zip and both JSONL manifests against archive-receipt.json.\n2. Restore into a NEW empty scratch workspace only. Never overwrite the active C workspace, historical D inputs or formal instances.\n3. For each manifest.jsonl record, read archiveMember from artifacts.zip, validate SHA256/bytes, then write under the new workspace at workspaceRelativePath after checking the resolved path stays inside that scratch workspace. Blob names are content-addressed; the ZIP paths themselves are not original file names.\n4. external-dependencies.jsonl lists retained original runtime dependency files not duplicated in this archive. Validate their hashes before reusing them; original absolute historical dependencies in frozen receipts stay read-only. This archive intentionally does not claim standalone relocation of absolute-path receipts.\n5. Read batch-plan.json and CURRENT_INCREMENTAL_BATCH.json. Reuse frozen evidence, retain failures, and run at most one heavy JVM. Failed/full-Session-incomplete worlds are never accepted signed restart seeds.\n\nNo original artifact, referenced temporary directory, baseline, receipt, world or failed evidence is removed by this archiver. Processes and file handles close on tool exit.\n"
with restore.open("x", encoding="utf-8") as stream:
    stream.write(restore_text)
receipt = {"failures": 0, "batchId": plan["batchId"], "checkpointOnly": not plan.get("batchComplete", False),
           "files": files, "uniqueBlobs": len(stored), "uncompressedBytes": raw_bytes, "externalDependencyFiles": external_files,
           "archive": str(archive), "archiveBytes": archive.stat().st_size, "archiveSha256": digest(archive),
           "manifestSha256": digest(manifest), "externalDependencyManifestSha256": digest(dependencies), "restoreInstructionsSha256": digest(restore),
           "allArchivedBlobsReadBackAndHashCheckedInStreamingMemory": True, "physicalScratchRestorePerformed": False, "inputPlanSha256": digest(plan_path),
           "ownedProcessesBeforeArchive": [], "originalFilesRemovedOrRelocated": False, "installable": False, "deployed": False}
with (output / "archive-receipt.json").open("x", encoding="utf-8") as stream:
    json.dump(receipt, stream, ensure_ascii=False, indent=2)
print(json.dumps({"archive": output.name, "files": files, "uniqueBlobs": len(stored), "archiveBytes": archive.stat().st_size, "failures": 0}))
