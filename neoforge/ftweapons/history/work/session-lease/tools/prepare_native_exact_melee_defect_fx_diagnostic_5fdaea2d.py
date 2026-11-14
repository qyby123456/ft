import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):
    return json.loads(path.read_text(encoding="utf-8"))
parent_path = work / "reports/native-exact-melee-defect-binding-diagnostic-preparation-849680bb.json"
parent = load(parent_path)
for name, expected in parent["inputHashes"].items():
    assert digest(Path(name)) == expected, Path(name).name
binding = work / "reports/native-repair-ranged-use-candidate-5fdaea2d.json"
candidate = load(binding)
assert candidate["classCount"] == 1381
source = Path(parent["newObserver"]).read_text(encoding="utf-8")
anchor = '                row.put("serverSignaturePresent", signed.getString("ServerSignature").length() == 64);'
assert source.count(anchor) == 1
source = source.replace(anchor, anchor + r'''
                Object domainAtEvent = ((net.farmingtales.ftboss.item.OwnerFieldEntityAccess)player).ftboss$ownerFieldDomain();
                row.put("diagnostic_ownerDomainPresentAtActualDamagePhase", domainAtEvent != null);
                if (domainAtEvent != null) row.put("diagnostic_ownerDomainRetiredAtActualDamagePhase", read(domainAtEvent, "retired"));
                Object persistentAtEvent = read(player, "persistentData");
                row.put("diagnostic_originalFxCooldownAtActualDamagePhase", persistentAtEvent instanceof CompoundTag tag ? tag.getLong("ftboss:weapon_fx_hit") : -1L);
                row.put("diagnostic_actualDamageTick", player.serverLevel().getGameTime());''')
new_source = work / "tests/exact-melee-defect-fx-diagnostic-5fdaea2d/PositiveWeapon436.java"
new_launcher = work / "tools/run_native_exact_melee_defect_fx_diagnostic_5fdaea2d.py"
preparation = work / "reports/native-exact-melee-defect-fx-diagnostic-preparation-5fdaea2d.json"
output = work / "reports/native-repair-runtime-exact-melee-defect-fx-diagnostic-5fdaea2d.json"
launcher = Path(parent["newLauncher"]).read_text(encoding="utf-8")
launcher = launcher.replace(Path(parent["newObserver"]).relative_to(work).as_posix(), new_source.relative_to(work).as_posix())
launcher = launcher.replace(parent_path.relative_to(work).as_posix(), preparation.relative_to(work).as_posix())
launcher = launcher.replace("reports/native-repair-runtime-exact-melee-defect-binding-diagnostic-849680bb.json", output.relative_to(work).as_posix())
launcher = launcher.replace("reports/native-repair-ranged-use-candidate-849680bb.json", binding.relative_to(work).as_posix())
launcher = launcher.replace("6c140dd7c8bcf09b7db276f192e21203f502f511b74f9f79618eca6dd7e8531e", digest(binding))
helper = work / "tools/prepare_native_frost_ingress_runtime.py"
namespace = {"__file__": str(helper)}
exec(compile(helper.read_text(encoding="utf-8").split("candidate = work /")[0], str(helper), "exec"), namespace)
namespace["add"](new_source.relative_to(root).as_posix(), source)
namespace["add"](new_launcher.relative_to(root).as_posix(), launcher)
paths = [Path(__file__), parent_path, binding, Path(parent["newObserver"]), Path(parent["newLauncher"]), new_source, new_launcher, helper]
with preparation.open("x", encoding="utf-8") as stream:
    json.dump({"newObserver": str(new_source), "newLauncher": str(new_launcher), "frozenProduct": "5fdaea2d", "onlyActualSample": 13,
               "diagnosticOnly": True, "authorityFieldsWritten": False, "installable": False,
               "inputHashes": {str(path): digest(path) for path in paths}}, stream, ensure_ascii=False, indent=2)
print(json.dumps({"prepared": preparation.name, "diagnosticOnly": True}))
