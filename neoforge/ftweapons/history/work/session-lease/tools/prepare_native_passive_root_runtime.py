import hashlib
import json
import sys
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
tag, mode = sys.argv[1:]
assert mode in ["signed", "controls"] and len(tag) == 8

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def load(path):
    return json.loads(path.read_text(encoding="utf-8"))

mutable = work / "reports/native-repair-ranged-use-candidate.json"
binding = work / ("reports/native-repair-ranged-use-candidate-" + tag + ".json")
if not binding.exists():
    assert Path(load(mutable)["attempt"]).name == "native-repair-" + tag
    with binding.open("xb") as stream:
        stream.write(mutable.read_bytes())
candidate = load(binding)
assert candidate["classCount"] == 1381 and candidate["effectiveSourceCount"] == 716 and candidate["guardCount"] == 208
parent_path = work / ("reports/native-exact-melee-configured-signed-preparation-849680bb.json" if mode == "signed"
                      else "reports/native-fx-owner-controls-preparation-d71b9448.json")
parent = load(parent_path)
for name, expected in parent["inputHashes"].items():
    assert digest(Path(name)) == expected, Path(name).name
source = Path(parent["newObserver"]).read_text(encoding="utf-8")
old_tag = "849680bb" if mode == "signed" else "d71b9448"
if mode == "signed":
    anchor = "for (int sample = 0; sample < 19; sample++) {"
    assert source.count(anchor) == 1
    source = source.replace(anchor, "for (int sample : new int[]{13}) {")
else:
    anchor = '                persistent.putLong("ftboss:weapon_fx_hit", server.overworld().getGameTime());'
    assert source.count(anchor) == 1
    source = source.replace(anchor, '                CompoundTag externalPassiveTimers = new CompoundTag();\n                externalPassiveTimers.putLong("defect", server.overworld().getGameTime());\n                persistent.put("FTBossIntrinsicPassiveTimers", externalPassiveTimers);')
    source = source.replace("externalSameFxKeyMutationRetiresOriginalDomainAndNeverReissuesSuccessor", "externalSamePassiveRootMutationRetiresOriginalDomainAndNeverReissuesSuccessor")
    source = source.replace("External same-key FX mutation", "External same-key passive root mutation")
observer = work / ("tests/native-passive-root-" + mode + "-" + tag + "/PositiveWeapon436.java")
launcher = work / ("tools/run_native_passive_root_" + mode + "_" + tag + ".py")
preparation = work / ("reports/native-passive-root-" + mode + "-preparation-" + tag + ".json")
output = work / ("reports/native-repair-runtime-passive-root-" + mode + "-" + tag + ".json")
text = Path(parent["newLauncher"]).read_text(encoding="utf-8")
text = text.replace(Path(parent["newObserver"]).relative_to(work).as_posix(), observer.relative_to(work).as_posix())
text = text.replace(parent_path.relative_to(work).as_posix(), preparation.relative_to(work).as_posix())
old_output = "reports/native-repair-runtime-exact-melee-configured-signed-849680bb.json" if mode == "signed" else "reports/native-repair-runtime-fx-owner-controls-d71b9448.json"
text = text.replace(old_output, output.relative_to(work).as_posix())
old_binding = work / ("reports/native-repair-ranged-use-candidate-" + old_tag + ".json")
text = text.replace(old_binding.relative_to(work).as_posix(), binding.relative_to(work).as_posix()).replace(digest(old_binding), digest(binding))
helper = work / "tools/prepare_native_frost_ingress_runtime.py"
namespace = {"__file__": str(helper)}
exec(compile(helper.read_text(encoding="utf-8").split("candidate = work /")[0], str(helper), "exec"), namespace)
namespace["add"](observer.relative_to(root).as_posix(), source)
namespace["add"](launcher.relative_to(root).as_posix(), text)
paths = [Path(__file__), parent_path, Path(parent["newObserver"]), Path(parent["newLauncher"]), binding, observer, launcher, helper]
with preparation.open("x", encoding="utf-8") as stream:
    json.dump({"newObserver": str(observer), "newLauncher": str(launcher), "onlyActualSample": 13,
               "completeOriginalStaticOrDynamicSessionAssertionsRetained": mode == "signed", "mode": mode,
               "wholeNineteenSignedOrRestartAccepted": False, "authorityFieldsWritten": False,
               "installable": False, "deployed": False, "inputHashes": {str(path): digest(path) for path in paths}}, stream, ensure_ascii=False, indent=2)
print(json.dumps({"prepared": preparation.name, "mode": mode, "fullOriginalSessionRequired": mode == "signed"}))
