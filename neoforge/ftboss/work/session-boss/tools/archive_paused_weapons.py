import datetime
import hashlib
import json
import os
import pathlib
import shutil
import zipfile

root = pathlib.Path(__file__).resolve().parents[3]
weapon = root / "work/session-lease"
output = root / "work/session-boss/archives/weapons-paused-a08a7373"
assert not output.exists()
output.mkdir(parents=True)

def digest(path):
    checksum = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            checksum.update(block)
    return checksum.hexdigest()

candidate_path = weapon / "reports/native-repair-ranged-use-candidate-a08a7373.json"
candidate = json.loads(candidate_path.read_text(encoding="utf-8"))
latest = pathlib.Path(candidate["attempt"])
assert latest.is_relative_to(root) and candidate["classCount"] == 1381
assert json.loads((latest / "asm.json").read_text(encoding="utf-8"))["failures"] == 0
selected = [root / name for name in ["AGENTS.md", "WRITE_OWNER.json", "WRITE_OWNER.claim", "HANDOFF_SESSION_CHAIN.md", "baseline.json"]]
for name in ["session-chain", "session-continuity", "session-projection", "session-lease"]:
    base = root / "work" / name
    for child in base.iterdir():
        if child.name in ["build", "archives", "debugger-preview"]:
            continue
        if child.name == "runtime":
            selected.extend(child / sub for sub in ["tests", "tools"] if (child / sub).exists())
        else:
            selected.append(child)
selected.extend(root / name for name in ["reports", "tools"])
selected.append(latest)
tags = ["dc604210", "2debe054", "f66758e3", "b1d8b7f9", "e47e94aa", "a7baeaac"]
selected.extend(weapon / ("build/native-repair-" + tag) for tag in tags)
selected.extend(path for path in (weapon / "build").iterdir() if path.name.startswith("passive-root-proof-"))
runtime_receipts = sorted((weapon / "reports").glob("native-repair-runtime-passive-root-*.json"))
runtime_rows = []
for receipt in runtime_receipts:
    data = json.loads(receipt.read_text(encoding="utf-8"))
    attempt = pathlib.Path(data["attempt"])
    terminal = json.loads((attempt / "terminal308.json").read_text(encoding="utf-8"))
    assert terminal["exitCode"] == 0 and terminal["stopSent"] and terminal["completeSave"]
    selected.append(attempt)
    runtime_rows.append({"receipt": str(receipt), "attempt": str(attempt), "observerFailures": data["probe"]["failures"], "completeSave": True, "javaExitCode": 0})
selected.append(weapon / "build/repair-runtime-22f4485f")
selected.append(pathlib.Path(__file__))
external = {}
for source in json.loads((latest / "complete-arguments316.json").read_text(encoding="utf-8")):
    path = pathlib.Path(source)
    if path.suffix == ".java":
        assert path.is_file()
        selected.append(path)
for source, expected in candidate["inputHashes"].items():
    path = pathlib.Path(source)
    assert path.is_file() and digest(path) == expected, path.name
    external[str(path.resolve())] = {"originalPath": str(path.resolve()), "bytes": path.stat().st_size, "sha256": expected, "retainedAtOriginalLocation": True}
for archive in (weapon / "archives").rglob("archive-receipt.json"):
    data = json.loads(archive.read_text(encoding="utf-8"))
    path = pathlib.Path(data["archive"])
    assert digest(path) == data["archiveSha256"]
    external[str(path.resolve())] = {"originalPath": str(path.resolve()), "bytes": path.stat().st_size, "sha256": data["archiveSha256"], "retainedAtOriginalLocation": True, "role": "previous immutable compressed batch; not duplicated"}
    selected.extend([archive, archive.parent / "manifest.jsonl", archive.parent / "external-dependencies.jsonl", archive.parent / "RESTORE.md"])
archive_path = output / "weapons-source-evidence.zip"
visited = set()
stored = set()
files = 0
raw_bytes = 0
with (output / "manifest.jsonl").open("x", encoding="utf-8") as manifest, zipfile.ZipFile(archive_path, "x", compression=zipfile.ZIP_DEFLATED, compresslevel=1, allowZip64=True) as bundle:
    for source in selected:
        assert source.exists() and not source.is_symlink(), source.name
        if source.is_dir():
            paths = []
            for base, directories, names in os.walk(source):
                directories[:] = [name for name in directories if name not in ["libraries", "mods", "kubejs", "recipe-inputs", "isolated-home", "isolated-tmp", "__pycache__"]]
                paths.extend(pathlib.Path(base) / name for name in names)
        else:
            paths = [source]
        for path in paths:
            resolved = path.resolve()
            if str(resolved) in visited:
                continue
            assert path.is_file() and not path.is_symlink()
            visited.add(str(resolved))
            checksum = digest(path)
            member = "blobs/" + checksum
            size = path.stat().st_size
            if checksum not in stored:
                with path.open("rb") as original, bundle.open(member, "w", force_zip64=True) as compressed:
                    shutil.copyfileobj(original, compressed, length=1024 * 1024)
                stored.add(checksum)
            record = {"originalPath": str(resolved), "archiveMember": member, "bytes": size, "sha256": checksum}
            if resolved.is_relative_to(root):
                record["workspaceRelativePath"] = resolved.relative_to(root).as_posix()
            else:
                record["externalSourceSnapshot"] = True
            manifest.write(json.dumps(record, ensure_ascii=False) + "\n")
            files += 1
            raw_bytes += size
with zipfile.ZipFile(archive_path) as bundle:
    for member in bundle.namelist():
        assert member.startswith("blobs/") and len(member) == 70
        checksum = hashlib.sha256()
        with bundle.open(member) as stream:
            for block in iter(lambda: stream.read(1024 * 1024), b""):
                checksum.update(block)
        assert checksum.hexdigest() == member.removeprefix("blobs/")
with (output / "external-dependencies.jsonl").open("x", encoding="utf-8") as stream:
    for row in external.values():
        stream.write(json.dumps(row, ensure_ascii=False) + "\n")
restore = output / "RESTORE.md"
restore.write_text("# Paused weapon development checkpoint\n\nThis archive preserves unfinished weapon development; it is not a release JAR.\n\nVerify archive and manifests using archive-receipt.json. Restore blobs only into a NEW empty scratch workspace, checking SHA256, bytes and resolved destination containment. workspaceRelativePath identifies C workspace files. External source snapshots are read-only source inputs; never overwrite historical D files. Prior immutable batch ZIPs, runtime mods/libraries/KubeJS/recipe inputs and other historical dependencies remain at their original paths and are referenced by preserved receipts/manifests; this is not a standalone game installation. Verify these dependencies before reuse.\n\nLatest candidate a08a7373 compiled with 313 ASM checks passing, but full signed Session remains unaccepted. Retain all failed diagnostics and their normal-save receipts. Resume only on explicit user instruction; Boss migration has priority. No source, world or referenced evidence was deleted or moved.\n", encoding="utf-8")
receipt = {"failures": 0, "purpose": "user requested paused weapon development checkpoint before Boss-first split", "createdAt": datetime.datetime.now(datetime.timezone.utc).isoformat(), "latestCandidate": str(candidate_path), "weaponDevelopmentPaused": True, "fullSignedSessionAccepted": False, "files": files, "uniqueBlobs": len(stored), "rawBytes": raw_bytes, "archive": str(archive_path), "archiveBytes": archive_path.stat().st_size, "archiveSha256": digest(archive_path), "manifestSha256": digest(output / "manifest.jsonl"), "externalDependencyManifestSha256": digest(output / "external-dependencies.jsonl"), "restoreInstructionsSha256": digest(restore), "allArchivedBlobsReadBackAndHashVerified": True, "physicalRestorePerformed": False, "runtimeRows": runtime_rows, "originalFilesRemovedOrRelocated": False, "deploymentPerformed": False}
with (output / "archive-receipt.json").open("x", encoding="utf-8") as stream:
    json.dump(receipt, stream, ensure_ascii=False, indent=2)
print(json.dumps({"archive": output.name, "files": files, "uniqueBlobs": len(stored), "archiveBytes": receipt["archiveBytes"], "failures": 0}), flush=True)
