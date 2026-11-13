import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
sys.path.insert(0, str(ROOT.parent))
from freeze_catalog import digest, load, save


def main():
    tests = (ROOT / "tests.log").read_text(encoding="utf-8")
    smoke = (ROOT / "core-smoke-java17.log").read_text(encoding="utf-8")
    if "Ran 5 tests" not in tests or "\nOK" not in tests or "layoutVariants=32" not in smoke or 'openjdk version "17.' not in smoke:
        raise ValueError("passing actual logs required")
    lock = load(ROOT / "snapshot-lock.json")
    for path, expected in lock["baselineFrozenFiles"].items():
        if digest(ROOT.parent / path) != expected:
            raise ValueError("baseline changed: " + path)
    for record in load(ROOT.parent / "SHA256SUMS.json")["files"]:
        if digest(ROOT.parent / record["path"]) != record["sha256"]:
            raise ValueError("baseline inventory changed: " + record["path"])
    inventory = [{"path":path.relative_to(ROOT).as_posix(),"sha256":digest(path),"bytes":path.stat().st_size} for path in sorted(ROOT.rglob("*")) if path.is_file() and path.name not in {"ready.json","progress.json","SHA256SUMS.json"} and "__pycache__" not in path.parts]
    save(ROOT / "SHA256SUMS.json", {"algorithm":"SHA-256","files":inventory,"excludes":["ready.json","progress.json","SHA256SUMS.json","__pycache__"]})
    ready = {"component":"catalog-expansion-v2","apiVersion":1,"ready":True,"status":"REQUIREMENTS_AND_UNACCEPTED_CANDIDATES_READY","productionPromotion":False,"productionBaselineUnchanged":True,"decodedBaseRooms":72,"candidateCount":7,"candidateAccepted":False,"completeNineRoomBiomeReady":False,"newRuntimeCompatibleTemplates":0,"candidateManifest":"candidate-manifest.json","candidateManifestSha256":digest(ROOT / "candidate-manifest.json"),"allRoomAudit":"all-72-expansion-audit.json","fullBiomeRequirements":"s8-complete-biome-requirements.json","exactPayloadRequest":"block-entity-support-request.json","snapshotLock":"snapshot-lock.json","currentCoreSourceHashes":{record["path"]:record["sha256"] for record in lock["currentCore"]},"inventorySha256":digest(ROOT / "SHA256SUMS.json"),"tests":[{"command":"python -B test_expansion.py","result":"PASS 5 tests including actual 72-source SHA/payload reread","logSha256":digest(ROOT / "tests.log")},{"command":"javac --release 17 ...; Java17 -ea -cp test-classes CatalogCoreSmoke","result":"PASS current final-core snapshot, Java17.0.8, 32 seeds, 6 rooms/5 links, 32 layouts, deterministic reversed pool","logSha256":digest(ROOT / "core-smoke-java17.log")}],"limitations":["seven candidate originals are accepted=false and NOT_FOR_PRODUCTION","BE capability/allowlist plus isolated native/world validation required","full nine-room biome also needs S8-01 stair geometry and S8-08 lava/vine world support","new candidate world placement and gameplay NOT_RUN","no original bytes, baseline ready, resources or other component source changed"]}
    save(ROOT / "ready.json", ready)
    save(ROOT / "progress.json", {"component":"catalog-expansion-v2","status":ready["status"],"readySha256":digest(ROOT / "ready.json"),"baselineUnchanged":True,"sourceRoomsDecoded":72,"unacceptedCandidatesCopied":7,"newProductionTemplates":0,"tests":"5 Python tests + final-core Java17 32-seed smoke PASS","worldTests":"NOT_RUN","limitations":ready["limitations"]})
    print("PASS expansion delivery inventory files=" + str(len(inventory)))
    print("readySHA256=" + digest(ROOT / "ready.json"))


if __name__ == "__main__":
    main()
