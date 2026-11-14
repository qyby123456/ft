import hashlib
import json
import os
import subprocess
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
hashes = {}
rebased_inputs = []

def digest(path):
    path = Path(path)
    value = hashlib.sha256(path.read_bytes()).hexdigest()
    hashes[str(path)] = value
    return value

def load(path):
    digest(path)
    return json.loads(path.read_text(encoding="utf-8"))

def frozen(data):
    replacements = {
        str(work / "reports/native-repair-ranged-use-candidate.json"): work / "reports/native-repair-ranged-use-candidate-41d445cf.json",
        str(work / "proposal-native/net/farmingtales/ftboss/item/OwnerFieldInstallationProof.java"): work / "build/native-repair-41d445cf/sources/net/farmingtales/ftboss/item/OwnerFieldInstallationProof.java"
    }
    for name, expected in data.get("inputHashes", {}).items():
        actual = digest(name)
        if actual != expected:
            assert name in replacements, Path(name).name
            replacement = replacements[name]
            assert digest(replacement) == expected, Path(name).name
            record = {"supersededMutableInput": name, "originalExpectedHash": expected, "currentHash": actual, "exactFrozenReplacement": str(replacement)}
            if record not in rebased_inputs:
                rebased_inputs.append(record)

prior_path = work / "reports/native-exact-melee-and-material-final-process-check-41d445cf.json"
prior = load(prior_path)
frozen(prior)
assert prior["failures"] == 0 and prior["actualRuntimeCount"] == 30
suffixes = [
    ("nineteen-original-rule-configured-paid-41d445cf", 1),
    ("nineteen-original-rule-configured-paid-input-validity-qualified-41d445cf", 1),
    ("stone-original-input-diagnostic-41d445cf", 1),
    ("configured-paid-849680bb", 0),
    ("configured-signed-849680bb", 1),
    ("defect-semantic-diagnostic-849680bb", 1),
    ("defect-semantic-consumer-qualified-diagnostic-849680bb", 1),
    ("defect-binding-diagnostic-849680bb", 1),
    ("configured-controls-849680bb", 0)
]
rows = []
for suffix, wanted in suffixes:
    path = work / ("reports/native-repair-runtime-exact-melee-" + suffix + ".json")
    data = load(path)
    frozen(data)
    attempt = Path(data["attempt"])
    terminal = load(attempt / "terminal308.json")
    process = load(attempt / "live-process308.json")
    assert data["exitCode"] == terminal["exitCode"] == 0
    assert data["probe"]["failures"] == terminal["probe"]["failures"] == wanted
    assert terminal["stopSent"] and terminal["completeSave"]
    log = attempt / "runtime308.log"
    text = log.read_bytes().decode("gb18030", errors="replace")
    assert "Stopping server" in text and "All dimensions are saved" in text
    digest(log)
    rows.append({"receipt": str(path), "attempt": str(attempt), "pid": process["pid"], "observerFailures": wanted,
                 "normalStop": True, "allDimensionsSaved": True, "javaExitCode": 0,
                 "diagnosticOrFailedMatrixNotAcceptedCombat": wanted == 1})
for name in ["native-exact-melee-configured-paid-focused-evidence-849680bb.json", "native-exact-melee-configured-controls-focused-evidence-849680bb.json",
             "native-exact-melee-defect-original-writer-diagnosis-849680bb.json", "native-remaining-original-interface-method-closure-static-audit-849680bb.json"]:
    evidence = load(work / "reports" / name)
    assert evidence["failures"] == 0
    frozen(evidence)
for name in ["build/exact-melee-nineteen-original-rule-configured-paid-precompile-41d445cf/terminal.json",
             "build/exact-melee-configured-controls-precompile-849680bb/terminal.json"]:
    precompile = load(work / name)
    assert precompile["exitCode"] == 0 and not precompile["serverJvmStarted"]
current = str(work).replace("'", "''")
forward = work.as_posix().replace("'", "''")
command = "$rows=@(Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe','python.exe','pythonw.exe') -and ($_.CommandLine -like '*" + current + "*' -or $_.CommandLine -like '*" + forward + "*') }); ConvertTo-Json -InputObject @($rows | Select-Object ProcessId,Name) -Compress"
result = subprocess.run(["powershell", "-NoProfile", "-Command", command], cwd=root, capture_output=True, text=True)
assert result.returncode == 0, result.stderr
processes = json.loads(result.stdout or "[]")
assert not [row for row in processes if int(row["ProcessId"]) != os.getpid()], processes
assert digest(root / "work/session-chain/reports/actual-current.json") == "1ec369cff511adab2c2c57d4e1d5cc89bca546a5ba9be4be9e70d0817dfd2243"
assert load(work / "reports/native-repair-ranged-use-candidate.json") == load(work / "reports/native-repair-ranged-use-candidate-849680bb.json")
arguments = load(work / "build/native-repair-849680bb/complete-arguments316.json")
effective = {}
normalized_source_differences = []
for name in arguments:
    source = Path(name)
    if source.suffix != ".java":
        continue
    text = source.read_text(encoding="utf-8")
    declaration = next(line for line in text.splitlines() if line.startswith("package "))
    key = declaration.removeprefix("package ").removesuffix(";").strip() + "." + source.name
    assert key not in effective
    effective[key] = source
assert len(effective) == 716
for source in (work / "proposal-native").rglob("*.java"):
    text = source.read_text(encoding="utf-8")
    declaration = next(line for line in text.splitlines() if line.startswith("package "))
    key = declaration.removeprefix("package ").removesuffix(";").strip() + "." + source.name
    staged_hash = digest(source)
    effective_hash = digest(effective[key])
    if staged_hash != effective_hash:
        assert source.name == "OwnerFieldInstallationProof.java" and text == effective[key].read_text(encoding="utf-8"), source.name
        normalized_source_differences.append({"stagedSource": str(source), "effectiveCompileSource": str(effective[key]),
                                              "stagedHash": staged_hash, "effectiveHash": effective_hash, "onlyUtf8LineEndingEncodingDiffers": True})
assert len(normalized_source_differences) == 1
assert len(rebased_inputs) == 2
digest(Path(__file__))
digest(root / "WRITE_OWNER.claim")
output = work / "reports/native-exact-melee-configured-final-process-check-849680bb.json"
with output.open("x", encoding="utf-8") as stream:
    json.dump({"failures": 0, "reusedPriorProcessAudit": str(prior_path), "actualRuntimeCount": prior["actualRuntimeCount"] + len(rows),
               "observerPassCount": prior["observerPassCount"] + sum(row["observerFailures"] == 0 for row in rows),
               "preservedFailureCount": prior["preservedFailureCount"] + sum(row["observerFailures"] == 1 for row in rows),
               "newRuntimes": rows, "allActualJavaRuntimesNormalSavedExitZero": True, "ownedJavaOrOtherPythonProcessesRemaining": False,
               "configuredNineteenPaidAndForeignControlsAccepted": True, "configuredNineteenSignedAndRestartAccepted": False,
               "originalDefectWriterGapStillOpenNotFixedByFixtureOrReset": True,
               "adoptedReceiptUnchanged": True, "stagedSourcesMatchActualEffective849CompileArgumentsAfterExplicitNewlineNormalization": True,
               "stagedGeneratedProofLineEndingOnlyDifference": normalized_source_differences,
               "priorSupersededMutableInputsVerifiedAgainstExactImmutable41Copies": rebased_inputs, "goalStillActive": True,
               "authenticatedMultiplayerAccepted": False, "installable": False, "deployed": False, "inputHashes": hashes}, stream, ensure_ascii=False, indent=2)
print(json.dumps({"report": output.name, "actualRuntimes": prior["actualRuntimeCount"] + len(rows), "observerPasses": prior["observerPassCount"] + 2,
                  "preservedFailures": prior["preservedFailureCount"] + 7, "hashes": len(hashes), "allNormalSavedJavaZero": True}))
