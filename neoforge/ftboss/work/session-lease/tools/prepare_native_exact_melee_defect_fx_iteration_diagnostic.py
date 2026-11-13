import hashlib
import json
import sys
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
tag = sys.argv[1]
assert len(tag) == 8 and all(character in "0123456789abcdef" for character in tag)
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):
    return json.loads(path.read_text(encoding="utf-8"))
parent_path = work / "reports/native-exact-melee-defect-fx-diagnostic-preparation-5fdaea2d.json"
parent = load(parent_path)
for name, expected in parent["inputHashes"].items():
    assert digest(Path(name)) == expected, Path(name).name
binding = work / ("reports/native-repair-ranged-use-candidate-" + tag + ".json")
candidate = load(binding)
assert candidate["classCount"] == 1381 and candidate["effectiveSourceCount"] == 716 and candidate["guardCount"] == 208
new_source = work / ("tests/exact-melee-defect-fx-diagnostic-" + tag + "/PositiveWeapon436.java")
new_launcher = work / ("tools/run_native_exact_melee_defect_fx_diagnostic_" + tag + ".py")
preparation = work / ("reports/native-exact-melee-defect-fx-diagnostic-preparation-" + tag + ".json")
output = work / ("reports/native-repair-runtime-exact-melee-defect-fx-diagnostic-" + tag + ".json")
source = Path(parent["newObserver"]).read_text(encoding="utf-8")
launcher = Path(parent["newLauncher"]).read_text(encoding="utf-8")
launcher = launcher.replace(Path(parent["newObserver"]).relative_to(work).as_posix(), new_source.relative_to(work).as_posix())
launcher = launcher.replace(parent_path.relative_to(work).as_posix(), preparation.relative_to(work).as_posix())
launcher = launcher.replace("reports/native-repair-runtime-exact-melee-defect-fx-diagnostic-5fdaea2d.json", output.relative_to(work).as_posix())
launcher = launcher.replace("reports/native-repair-ranged-use-candidate-5fdaea2d.json", binding.relative_to(work).as_posix())
launcher = launcher.replace("8fadc20fa732854fbca91dfb5cf0170fb4f42a7117381b96eff4a4af988e1cff", digest(binding))
helper = work / "tools/prepare_native_frost_ingress_runtime.py"
namespace = {"__file__": str(helper)}
exec(compile(helper.read_text(encoding="utf-8").split("candidate = work /")[0], str(helper), "exec"), namespace)
namespace["add"](new_source.relative_to(root).as_posix(), source)
namespace["add"](new_launcher.relative_to(root).as_posix(), launcher)
paths = [Path(__file__), parent_path, binding, Path(parent["newObserver"]), Path(parent["newLauncher"]), new_source, new_launcher, helper]
with preparation.open("x", encoding="utf-8") as stream:
    json.dump({"newObserver": str(new_source), "newLauncher": str(new_launcher), "frozenProduct": tag, "onlyActualSample": 13,
               "diagnosticOnly": True, "authorityFieldsWritten": False, "installable": False,
               "inputHashes": {str(path): digest(path) for path in paths}}, stream, ensure_ascii=False, indent=2)
print(json.dumps({"prepared": preparation.name, "diagnosticOnly": True}))
