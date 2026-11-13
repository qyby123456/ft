import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
sys.path.insert(0,str(ROOT.parent))
from freeze_catalog import digest,load,save


def main():
    log = (ROOT / "tests.log").read_text(encoding="utf-8")
    if "Ran 5 tests" not in log or "\nOK" not in log:
        raise ValueError("passing actual test logs required")
    inventory = [{"path":path.relative_to(ROOT).as_posix(),"sha256":digest(path),"bytes":path.stat().st_size} for path in sorted(ROOT.rglob("*")) if path.is_file() and path.name not in {"ready.json","progress.json","SHA256SUMS.json"} and "__pycache__" not in path.parts]
    save(ROOT / "SHA256SUMS.json",{"files":inventory,"algorithm":"SHA-256","excludes":["ready.json","progress.json","SHA256SUMS.json","__pycache__"]})
    ready = {"component":"catalog-acceptance-candidate-v4","apiVersion":1,"ready":True,"status":"PENDING_RELEASE_GATES_AND_FULL_ROUTE_INPUTS_READY","productionPromotion":False,"newPromotions":0,"parentAuthorization":"NOT_GRANTED_BY_CATALOG","candidates":9,"allNewCandidateEntriesAccepted":False,"routeCases":36,"originalTargetsPerRotation":148,"directedAndReturnCheckInputs":2200,"routeWorldChecks":"NOT_RUN_BY_CATALOG","independentV2FilesVerified":389,"releaseChecklist":"release-gate-candidates.json","evidenceLayers":"evidence-layers.json","routeInputs":"rotated-route-inputs.json","routeInputSha256":digest(ROOT / "rotated-route-inputs.json"),"pathMatrix":"path-check-matrix.json","pathMatrixSha256":digest(ROOT / "path-check-matrix.json"),"inventorySha256":digest(ROOT / "SHA256SUMS.json"),"tests":{"command":"python -B test_v4.py","result":"PASS 5 tests","logSha256":digest(ROOT / "tests.log")},"limitations":["independent V2 BE capability is not full route/interactions/installed-original approval","01 native stair path and08 independent fluid validation still required","new planner/fluid runtime versions need exact-hash independent reruns","no accepted flag changed; parent explicit scoped authorization required","no source targets removed; duplicate labels retained with unique target IDs"]}
    save(ROOT / "ready.json",ready)
    save(ROOT / "progress.json",{"component":ready["component"],"status":ready["status"],"readySha256":digest(ROOT / "ready.json"),"promotions":0,"tests":"5/5 PASS","worldChecks":"NOT_RUN_BY_CATALOG","priorBaselinesUnchanged":True})
    print("PASS v4 ready; inventory files="+str(len(inventory))+" readySHA256="+digest(ROOT / "ready.json"))


if __name__ == "__main__":
    main()
