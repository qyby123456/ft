import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
sys.path.insert(0, str(ROOT.parent))
from freeze_catalog import digest, load, save


def main():
    tests = (ROOT / "tests.log").read_text(encoding="utf-8")
    if "Ran 6 tests" not in tests or "\nOK" not in tests:
        raise ValueError("passing actual test log required")
    inventory = [{"path":path.relative_to(ROOT).as_posix(),"sha256":digest(path),"bytes":path.stat().st_size} for path in sorted(ROOT.rglob("*")) if path.is_file() and path.name not in {"SHA256SUMS.json","ready.json","progress.json"} and "__pycache__" not in path.parts]
    save(ROOT / "SHA256SUMS.json", {"algorithm":"SHA-256","files":inventory,"excludes":["SHA256SUMS.json","ready.json","progress.json","__pycache__"]})
    contract = load(ROOT / "runtime-contract.json")
    ready = {"component":"catalog-expansion-v3","apiVersion":1,"ready":True,"status":"EXACT_V2_CONTRACT_AND_SEVEN_UNACCEPTED_ORIGINALS_REVIEW_READY","productionPromotion":False,"allCandidatesAccepted":False,"candidateRoomIds":[room["id"] for room in load(ROOT / "seven-original-room-review.json")],"runtimeArtifactSha256":contract["artifactSha256"],"runtimeSnapshotFileCount":len(contract["snapshotFiles"]),"runtimeEvidenceStatus":contract["evidenceStatus"],"manifest":"candidate-manifest.json","manifestSha256":digest(ROOT / "candidate-manifest.json"),"geometryEvidence":"core-stairs-consumption.json","geometryEvidenceSha256":digest(ROOT / "core-stairs-consumption.json"),"inventorySha256":digest(ROOT / "SHA256SUMS.json"),"tests":{"command":"python -B test_v3.py","result":"PASS 6 tests","logSha256":digest(ROOT / "tests.log")},"unchangedPriorBaselines":True,"limitations":["accepted=false pending independent runtime validation","S8-01 inward steps need native geometry, not rectangular erasure","S8-07 preserves Y2/Y4 and original open door; full native route validation still needed","S8-08 lava/vine/wall outside v2 contract; no fluid approval","no new catalog world run; producer logs are not independent acceptance"]}
    save(ROOT / "ready.json", ready)
    save(ROOT / "progress.json", {"component":ready["component"],"status":ready["status"],"readySha256":digest(ROOT / "ready.json"),"productionPromotion":False,"tests":"6/6 PASS","worldTests":"NOT_RUN_BY_CATALOG","runtimeArtifactSha256":ready["runtimeArtifactSha256"],"baselineAndExpansionV2Unchanged":True})
    print("PASS v3 inventory files=" + str(len(inventory)))
    print("readySHA256=" + digest(ROOT / "ready.json"))


if __name__ == "__main__":
    main()
