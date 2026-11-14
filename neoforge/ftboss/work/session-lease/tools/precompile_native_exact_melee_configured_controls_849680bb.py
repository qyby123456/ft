import hashlib
import json
import subprocess
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
hashes = {}

def digest(path):
    path = Path(path)
    value = hashlib.sha256(path.read_bytes()).hexdigest()
    hashes[str(path)] = value
    return value

def load(path):
    digest(path)
    return json.loads(path.read_text(encoding="utf-8"))

preparation = work / "reports/native-exact-melee-configured-controls-preparation-849680bb.json"
prepared = load(preparation)
for name, expected in prepared["inputHashes"].items():
    assert digest(name) == expected, Path(name).name
source = Path(prepared["newObserver"])
original_command = work / "build/repair-runtime-a124e67b/compile-command308.json"
command = load(original_command)
attempt = work / "build/exact-melee-configured-controls-precompile-849680bb"
assert not attempt.exists()
attempt.mkdir()
classes = attempt / "classes"
classes.mkdir()
command[command.index("-d") + 1] = str(classes)
assert command[10].endswith("exact-melee-configured-signed-849680bb\\PositiveWeapon436.java")
command[10] = str(source)
for value in command:
    if value.endswith(".java"):
        digest(value)
with (attempt / "compile.log").open("xb") as stream:
    result = subprocess.run(command, cwd=work.parent / "session-chain/runtime", stdout=stream, stderr=subprocess.STDOUT)
digest(attempt / "compile.log")
digest(Path(__file__))
for path in sorted(classes.rglob("*.class")):
    digest(path)
receipt = {
    "exitCode": result.returncode,
    "serverJvmStarted": False,
    "sourcePreparation": str(preparation),
    "commandDerivedFrom": str(original_command),
    "changes": {"outputDirectory": str(classes), "observerSource": str(source)},
    "runtimeAcceptance": False,
    "installable": False,
    "inputHashes": hashes
}
with (attempt / "terminal.json").open("x", encoding="utf-8") as stream:
    json.dump(receipt, stream, ensure_ascii=False, indent=2)
print(json.dumps({"precompile": attempt.name, "exitCode": result.returncode, "serverJvmStarted": False, "hashes": len(hashes)}))
assert result.returncode == 0
