import gzip
import hashlib
import json
import zipfile
from datetime import datetime
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
INSTANCE = Path(r"C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2")
JAR_PATH = "deliverables/qys-event-forge-1.20.1-1.0.6.jar"
ZIP_NAME = "EventMod-complete-src-Forge-1.20.1.zip"


def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def digest(data):
    return hashlib.sha256(data).hexdigest().upper()


def file_hash(path):
    return digest(path.read_bytes())


def write_json(relative, content):
    target = (ROOT / relative).resolve()
    require(target.is_relative_to(ROOT), "Output path escaped export directory")
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(content, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def read_json(relative):
    return json.loads((ROOT / relative).read_text(encoding="utf-8-sig"))


def jar_info(relative):
    path = ROOT / relative
    with zipfile.ZipFile(path) as archive:
        names = archive.namelist()
        require(len(names) == len(set(names)), "Duplicate archive entries: " + relative)
        require(archive.testzip() is None, "Archive CRC failure: " + relative)
        all_entries = {entry.filename: digest(archive.read(entry)) for entry in archive.infolist()}
        files = {entry.filename: archive.read(entry) for entry in archive.infolist() if not entry.is_dir()}
    return {
        "path": relative,
        "bytes": path.stat().st_size,
        "sha256": file_hash(path),
        "allEntries": all_entries,
        "files": files,
    }


def jar_delta(before, after, include_directories=False):
    before_entries = before["allEntries"] if include_directories else {name: digest(data) for name, data in before["files"].items()}
    after_entries = after["allEntries"] if include_directories else {name: digest(data) for name, data in after["files"].items()}
    shared = before_entries.keys() & after_entries.keys()
    changed = sorted(name for name in shared if before_entries[name] != after_entries[name])
    return {
        "reference": before["path"],
        "referenceSha256": before["sha256"],
        "candidate": after["path"],
        "candidateSha256": after["sha256"],
        "added": sorted(after_entries.keys() - before_entries.keys()),
        "removed": sorted(before_entries.keys() - after_entries.keys()),
        "changed": changed,
        "unchanged": len(shared) - len(changed),
        "referenceEntries": len(before_entries),
        "candidateEntries": len(after_entries),
    }


def delivery_files():
    result = []
    for path in ROOT.rglob("*"):
        if not path.is_file():
            continue
        relative = path.relative_to(ROOT)
        parts = relative.parts
        if parts[0] in {"_work", "logs"} or relative.as_posix() in {ZIP_NAME, "SHA256SUMS.txt", "export-receipt.json"}:
            continue
        if parts[0] == "project" and len(parts) > 1 and parts[1] in {"build", ".gradle", "logs"}:
            continue
        if "__pycache__" in parts or (parts[0] == "tools" and "audit-classes" in parts):
            continue
        if relative.as_posix() == "audit/runtime-link/target.jar":
            continue
        result.append(path)
    return sorted(result, key=lambda path: path.relative_to(ROOT).as_posix())


def current_state(before):
    files = []
    changed = []
    for original in before["files"]:
        path = INSTANCE / original["relativePath"]
        record = {"relativePath": original["relativePath"], "exists": path.is_file(), "bytes": None, "sha256": None}
        if record["exists"]:
            record.update(bytes=path.stat().st_size, sha256=file_hash(path))
        if any(record[key] != original[key] for key in ("exists", "bytes", "sha256")):
            changed.append(record["relativePath"])
        files.append(record)
    event_changed = any("qys-event" in name for name in changed)
    require(not event_changed, "Observed Event package state changed during audit; re-review required")
    return {
        "recordedAt": datetime.now().astimezone().isoformat(),
        "files": files,
        "observedDrift": changed,
        "eventStateChanged": event_changed,
        "instanceWritesPerformedByThisRun": False,
        "stateChangesApproved": False,
        "expectedEventState": "disabled; physical correction pending explicit main-thread approval",
    }


def historical_evidence():
    name = "logs/2026-10-13-2.log.gz"
    path = INSTANCE / name
    with gzip.open(path, "rb") as stream:
        lines = stream.read(16 * 1024 * 1024).decode("gb18030", errors="replace").splitlines()
    selected = [{"line": number, "text": lines[number - 1]} for number in (410, 2937, 3220, 3263, 3784, 3866)]
    crash = INSTANCE / "crash-reports/crash-2026-10-12_22.08.31-fml.txt"
    crash_lines = crash.read_text(encoding="utf-8", errors="replace").splitlines()
    write_json("audit/historical-load-evidence.json", {
        "log": str(path), "sha256": file_hash(path), "lineNumbersAfterDecompression": True,
        "evidence": selected, "newCandidateGameTested": False,
        "crashReport": str(crash), "crashSha256": file_hash(crash),
        "crashEvidence": [{"line": number, "text": crash_lines[number - 1]} for number in (46, 65, 69, 75, 358)],
    })


def main():
    require(ROOT.parent == Path(r"D:\ft工程文件"), "Export must be in independent D engineering directory")
    observed = jar_info("reference/observed-active-262B3284.jar")
    baseline = jar_info("reference/independent-base-9FC66CD0.jar")
    canonical = jar_info("reference/canonical-build-83E6305F.jar")
    candidate = jar_info(JAR_PATH)
    prior_delta = jar_delta(baseline, observed, True)
    require(len(prior_delta["added"]) == 5 and prior_delta["unchanged"] == 112
            and not prior_delta["removed"] and not prior_delta["changed"], "Historical 112+5 provenance mismatch")
    delta = jar_delta(observed, candidate)
    require(not delta["added"] and not delta["removed"], "Candidate entry coverage changed")
    require(all(name.endswith(".class") for name in delta["changed"]), "Candidate resource changes detected")
    reproduction = read_json("audit/build-reproducibility.json")
    require(reproduction["identical"] and reproduction["firstSha256"] == candidate["sha256"]
            and reproduction["secondSha256"] == candidate["sha256"], "Clean-build artifact hash mismatch")
    for source in read_json("audit/build-inputs.json"):
        require(file_hash(ROOT / source["path"]) == source["sha256"], "Post-build source change: " + source["path"])
    comparisons = read_json("audit/bytecode-comparison.json")
    require(all(record["srgCodeAndDescriptorsMatch"] for record in comparisons), "SRG instruction comparison failed")
    require({record["className"].replace(".", "/") + ".class" for record in comparisons} == set(delta["changed"]),
            "Changed class comparison coverage mismatch")
    require(read_json("audit/resources-verification.json")["passed"], "Resource/model/texture validation failed")
    require(read_json("audit/runtime-link/verification/full-link-audit.json")["passed"], "Full link audit failed")
    require(read_json("audit/side-isolation.json")["passed"], "Side isolation audit failed")
    require(read_json("audit/core-facing-runtime.json")["passed"], "Core Mixin audit failed")
    require("SUMMARY pass=671 fail=0" in (ROOT / "audit/shop-delivery-regression.log").read_text(encoding="utf-8"), "Shop delivery regression failed")
    class_mapping = []
    resource_mapping = []
    for entry, content in sorted(candidate["files"].items()):
        if entry.endswith(".class"):
            outer = entry[:-6].split("$", 1)[0]
            source_path = "project/src/main/java/" + outer + ".java"
            require((ROOT / source_path).is_file(), "Class without full Java source: " + entry)
            class_mapping.append({"entry": entry, "entrySha256": digest(content), "source": source_path,
                                  "sourceSha256": file_hash(ROOT / source_path),
                                  "identicalToObservedClassBytes": content == observed["files"][entry]})
        else:
            source_path = "project/build.gradle" if entry == "META-INF/MANIFEST.MF" else "project/src/main/resources/" + entry
            require((ROOT / source_path).is_file(), "Resource without source: " + entry)
            if entry != "META-INF/MANIFEST.MF":
                require((ROOT / source_path).read_bytes() == content, "Resource source mismatch: " + entry)
            resource_mapping.append({"entry": entry, "entrySha256": digest(content), "source": source_path,
                                     "sourceSha256": file_hash(ROOT / source_path),
                                     "generatedManifest": entry == "META-INF/MANIFEST.MF",
                                     "identicalToObservedBytes": content == observed["files"][entry]})
    require(all(record["identicalToObservedBytes"] for record in resource_mapping), "Archived runtime resources changed")
    write_json("audit/independent-base-to-observed.json", prior_delta)
    write_json("audit/canonical-to-observed.json", jar_delta(canonical, observed))
    write_json("audit/unified-versus-observed.json", delta)
    write_json("audit/source-jar-map.json", {
        "status": "SEALED_OFFLINE_NOT_DEPLOYED", "expectedInstanceEventState": "disabled pending explicit correction approval",
        "candidate": {key: candidate[key] for key in ("path", "bytes", "sha256")},
        "observedUnapprovedActivePackage": {key: observed[key] for key in ("path", "bytes", "sha256")},
        "cleanBuildEvidence": "audit/build-reproducibility.json", "oldHybridJarByteRebuildClaimed": False,
        "allResourceBytesPreserved": True, "disassembledChangedClassCodeMatchesObserved": True,
        "classCount": len(class_mapping), "resourceAndManifestCount": len(resource_mapping),
        "classes": class_mapping, "resourcesAndManifest": resource_mapping,
    })
    source_paths = sorted((ROOT / "project/src").rglob("*"))
    source_files = [{"path": path.relative_to(ROOT).as_posix(), "bytes": path.stat().st_size, "sha256": file_hash(path)}
                    for path in source_paths if path.is_file()]
    write_json("audit/source-manifest.json", {
        "mainJava": len(list((ROOT / "project/src/main/java").rglob("*.java"))),
        "resources": len([path for path in (ROOT / "project/src/main/resources").rglob("*") if path.is_file()]),
        "testJava": len(list((ROOT / "project/src/test/java").rglob("*.java"))), "files": source_files,
    })
    write_json("audit/after-state.json", current_state(read_json("audit/before-state.json")))
    historical_evidence()
    files = delivery_files()
    archive_path = ROOT / ZIP_NAME
    with zipfile.ZipFile(archive_path, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        for path in files:
            entry = zipfile.ZipInfo(path.relative_to(ROOT).as_posix(), (1980, 1, 1, 0, 0, 0))
            entry.compress_type = zipfile.ZIP_DEFLATED
            entry.external_attr = 0o100644 << 16
            archive.writestr(entry, path.read_bytes())
    with zipfile.ZipFile(archive_path) as archive:
        require(archive.testzip() is None, "Export ZIP CRC failure")
        require(len(archive.namelist()) == len(files), "Export ZIP coverage mismatch")
        for path in files:
            relative = path.relative_to(ROOT).as_posix()
            require(digest(archive.read(relative)) == file_hash(path), "Export ZIP hash mismatch: " + relative)
    receipt = {
        "exportedAt": datetime.now().astimezone().isoformat(), "directory": str(ROOT),
        "status": "SEALED_OFFLINE_REVIEWED_NOT_DEPLOYED", "archive": ZIP_NAME,
        "archiveBytes": archive_path.stat().st_size, "archiveSha256": file_hash(archive_path),
        "archiveVerifiedFiles": len(files), "candidate": {key: candidate[key] for key in ("path", "bytes", "sha256")},
        "sourceToJarMapSha256": file_hash(ROOT / "audit/source-jar-map.json"),
        "checksums": "SHA256SUMS.txt", "checksumScope": "All sealed delivery files, ZIP and receipt; excludes checksums file itself, dependency caches and generated build outputs",
        "instanceWritesPerformed": False, "enablePerformed": False, "disablePerformed": False,
        "bossMerged": False, "dshUsed": False, "gameTested": False,
        "nextAction": "Main-thread review and explicit approval before any physical Event state correction",
    }
    write_json("export-receipt.json", receipt)
    checksum_paths = files + [archive_path, ROOT / "export-receipt.json"]
    sums = "".join(file_hash(path) + "  " + path.relative_to(ROOT).as_posix() + "\n" for path in sorted(checksum_paths))
    (ROOT / "SHA256SUMS.txt").write_text(sums, encoding="utf-8")
    for line in sums.splitlines():
        expected, relative = line.split("  ", 1)
        require(file_hash(ROOT / relative) == expected, "Final SHA256 mismatch: " + relative)
    print(json.dumps({"sealedJarSha256": candidate["sha256"], "sourceZipSha256": receipt["archiveSha256"],
                      "verifiedZipFiles": len(files), "verifiedChecksums": len(checksum_paths),
                      "mainJava": 32, "resources": 31, "testJava": 3, "deployed": False}, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
