from pathlib import Path

from freeze_catalog import ROOT, digest, load, save


def main():
    tests = (ROOT / "tests.log").read_text(encoding="utf-8")
    smoke = (ROOT / "core-smoke.log").read_text(encoding="utf-8")
    if "Ran 7 tests" not in tests or "\nOK" not in tests or "PASS real catalog + core snapshot" not in smoke:
        raise ValueError("successful real test logs required")
    excluded = {"ready.json", "progress.json", "SHA256SUMS.json"}
    entries = [{"path": path.relative_to(ROOT).as_posix(), "sha256": digest(path), "bytes": path.stat().st_size} for path in sorted(ROOT.rglob("*")) if path.is_file() and path.name not in excluded and "__pycache__" not in path.parts]
    save(ROOT / "SHA256SUMS.json", {"algorithm": "SHA-256", "excludes": sorted(excluded) + ["__pycache__"], "files": entries})
    manifest = load(ROOT / "manifest.json")
    ready = {"component": "catalog", "apiVersion": 1, "status": "READY_MINIMAL_REAL_POOL", "ready": True, "scope": "unchanged independently accepted architectural room templates; not runtime/world acceptance", "manifest": "manifest.json", "runtimeManifest": "data/biomedungeon/manifest.json", "runtimeResourceRoot": ".", "packageTree": "data", "manifestSha256": digest(ROOT / "manifest.json"), "inventory": "SHA256SUMS.json", "inventorySha256": digest(ROOT / "SHA256SUMS.json"), "biome": "s8", "templateCount": 2, "templateHashes": {template["id"]: template["sha256"] for template in manifest["templates"]}, "sourceHashes": {entry["path"]: entry["sha256"] for entry in entries if entry["path"].endswith((".py", ".java"))}, "tests": [{"command":"python -B test_catalog.py", "result":"PASS 7/7", "log":"tests.log", "sha256":digest(ROOT / "tests.log")}, {"command":"javac --release 17 -d test-classes core-snapshot/DungeonApi.java core-snapshot/DungeonGeometry.java core-snapshot/DungeonPlanner.java CatalogCoreSmoke.java; java -cp test-classes CatalogCoreSmoke", "result":"PASS 32 seeds x six rooms/five links; deterministic reversed pool; 32 layouts; both real room IDs", "log":"core-smoke.log", "sha256":digest(ROOT / "core-smoke.log"), "javaExecutionVersion":"25.0.2; compilation release target 17"}], "limitations": ["only biome s8, two normal room types; source reuse permitted", "no original entrance/treasure/combat certification", "runtime must close unused apertures and reserved envelope", "world/native rotated block-state/tick/restart tests NOT_RUN", "remaining 70 single-room sources and eight overviews excluded/deferred; all 11-20 rejected", "no source adapters or altered candidate resources"], "issues": "ISSUES.json"}
    save(ROOT / "ready.json", ready)
    save(ROOT / "progress.json", {"component": "catalog", "apiVersion": 1, "status": "READY_MINIMAL_REAL_POOL", "baseIndexEntries":80, "singleRoomNBTDecoded":72, "acceptedUnchangedTemplates":2, "excludedOrDeferredBaseEntries":78, "tests":"PASS: seven unit tests and 32-seed real-core smoke", "worldTests":"NOT_RUN", "readySha256":digest(ROOT / "ready.json"), "manifestSha256":ready["manifestSha256"], "inventorySha256":ready["inventorySha256"], "limitations":ready["limitations"]})
    for entry in entries:
        if digest(ROOT / entry["path"]) != entry["sha256"]:
            raise ValueError("post-inventory hash mismatch: " + entry["path"])
    print("READY catalog; templates=2 biome=s8 manifestSHA256=" + ready["manifestSha256"])
    print("Verified inventory files=" + str(len(entries)) + " inventorySHA256=" + ready["inventorySha256"])


if __name__ == "__main__":
    main()
