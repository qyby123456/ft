import hashlib
import json
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parent.parent


def load(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def inventory(root):
    return [{"path": path.relative_to(root).as_posix(), "sha256": sha(path)} for path in sorted(root.rglob("*")) if path.is_file()]


def main():
    inputs = load(ROOT / "input-lock.json")
    progress = load(ROOT / "progress.json")
    assert progress["status"] != "RUNNING", "Wait for owned server sequence"
    protection = load(ROOT / "protection-result.json")
    cases = dict(progress["cases"], protection_piston=protection)
    ready = Path(inputs["runtimeReady"])
    assert sha(ready) == inputs["runtimeReadySha256"]
    for relative, expected in inputs["runtimeFiles"].items():
        assert sha(ready.parent / relative) == expected, "Frozen runtime changed"
    for record in inputs["helperFiles"]:
        assert sha(Path(record["path"])) == record["sha256"], "Read-only helper changed"
    assert inventory(Path(inputs["sourceWorld"])) == inputs["sourceWorldFiles"], "Source world touched"
    assert inventory(ROOT / "inputs/baseline/world") == inputs["baselineWorldFiles"], "Baseline touched"
    assert inventory(ROOT / "inputs/baseline/libraries") == inputs["libraryFiles"]
    expected = inputs["jarSha256"]
    assert sha(Path(inputs["jar"])) == expected
    process_list = []
    root = Path(progress["root"])
    for name, case in cases.items():
        run = root / name
        mods = list((run / "mods").glob("*.jar"))
        assert len(mods) == 1 and sha(mods[0]) == expected
        for process in case["processes"]:
            assert process["exitCode"] is not None
            assert Path(process["cwd"]).resolve() == run.resolve()
            assert "-Dftbd.v5World=" + str(run / "world") in process["command"]
            assert sha(Path(process["log"])) == process["logSha256"]
            process_list.append(dict(case=name, exactJarSha256=expected, **process))
    passed = [name for name, result in cases.items() if result["status"].startswith("PASS")]
    failed = [name for name in cases if name not in passed]
    recovered = cases.get("durable_checkpoint_forcekill", {})
    if recovered.get("readback"):
        observed = load(Path(recovered["readback"]))
        differences = observed["originalStateDifferences"]
        classification = {"wallUpPropertyOnly": 0, "supportedVineEvolutionInFluidRoom": 0, "unclassified": []}
        for difference in differences:
            expected_state, expected_pairs = difference["expected"]
            actual_state, actual_pairs = difference["actual"]
            expected_properties, actual_properties = dict(expected_pairs), dict(actual_pairs)
            if expected_state == actual_state == "minecraft:deepslate_brick_wall" and dict(expected_properties, up="false") == actual_properties and expected_properties.get("up") == "true":
                classification["wallUpPropertyOnly"] += 1
            elif difference["room"] == "s8_08" and expected_state == "minecraft:air" and actual_state == "minecraft:vine":
                classification["supportedVineEvolutionInFluidRoom"] += 1
            else:
                classification["unclassified"].append(difference)
        assert not classification["unclassified"], "Do not silently accept unexplained state differences"
        classification["liveNativeReadbackAnd2400TickObservationPassed"] = "observedTicks=2400" in recovered["finalStatus"] and "PAYLOAD_VERIFIED" in recovered["finalStatus"]
        assert classification["liveNativeReadbackAnd2400TickObservationPassed"]
    else:
        differences = None
        classification = None
    summary = {"status": "PASS_ALL_SIX_INDEPENDENT_INSTALLED_FAULT_CASES" if not failed else "FAULT_CASES_COMPLETE_WITH_FAILURES",
               "independentExecutor": inputs["independentExecutor"], "jarSha256": expected,
               "runtimeVersion": "runtime-v5-explicit-commit1-envelope-v3-derived07", "passedCases": passed, "failedCases": failed,
               "caseResults": {name: {key: value for key, value in case.items() if key != "processes"} for name, case in cases.items()},
               "processes": process_list, "maxOwnedServers": 1, "sourceWorldUnchanged": True, "frozenRuntime78Unchanged": True,
               "actualForceTermination": True, "powerloss": False, "actualCommitted": False,
               "recoveryOriginalStateDifferences": differences, "recoveryStateDifferenceClassification": classification,
               "productionPromotion": False, "completeGeneratorReady": False}
    (ROOT / "result.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    files = inventory(ROOT)
    files = [record for record in files if record["path"] != "ready.json"]
    result = {"status": summary["status"], "jarSha256": expected, "passedCases": passed, "failedCases": failed,
              "powerloss": False, "actualCommitted": False, "productionPromotion": False, "files": files}
    (ROOT / "ready.json").write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("READY", summary["status"], "files=" + str(len(files)), "SHA256=" + sha(ROOT / "ready.json"))


if __name__ == "__main__":
    main()
