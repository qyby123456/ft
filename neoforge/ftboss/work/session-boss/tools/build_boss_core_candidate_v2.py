import argparse
import hashlib
import json
from pathlib import Path
import re
import shutil
import sys
import uuid
import zipfile

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / "work/session-boss"
HISTORY = Path("D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022")
sys.path.insert(0, str(HISTORY / "tools"))
from weapon_dependency_frontier import references


def load(path):
    return json.loads(Path(path).read_text(encoding="utf-8"))


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def save(path, value):
    with Path(path).open("x", encoding="utf-8") as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)


parser = argparse.ArgumentParser()
parser.add_argument("compile_context", type=Path)
arguments = parser.parse_args()
context_path = arguments.compile_context.resolve()
context = load(context_path)
assert context["exitCode"] == 0
for name, wanted in context["sourceHashes"].items():
    assert digest(name) == wanted
original = Path(context["originalApplication"])
candidate_receipt = ROOT / "work/session-lease/reports/native-repair-ranged-use-candidate-a08a7373.json"
candidate = load(candidate_receipt)
assert candidate["failures"] == 0 and candidate["classCount"] == 1381
paused = Path(candidate["attempt"]) / "complete-classes"
compiled = Path(context["output"])
classes = {path.relative_to(original).as_posix()[:-6]: path for path in original.rglob("*.class")}
for overlay in [paused, compiled]:
    for path in overlay.rglob("*.class"):
        classes[path.relative_to(overlay).as_posix()[:-6]] = path
prefix = "net/farmingtales/ftboss/"
graph = {name: sorted(references(path.read_bytes()) & classes.keys() - {name}) for name, path in classes.items()}
initial_report = WORK / "reports/split-boundary-initial.json"
weapon = set(load(initial_report)["weaponClassCandidates"])
weapon |= {prefix + name for name in ["item/WeaponBossMaterialItems", "item/MaterialBowItems", "item/MaterialCrossbows", "client/model/MaterialCrossbowModel"]}
weapon |= {name for name in classes if name.startswith(prefix + "item/Native") or name.startswith(prefix + "util/WeaponEvolutionSkillPool")}
weapon |= {prefix + "mixin/" + name for name in ["NativeCompoundTagLoaderObservationMixin295", "NativeCrossbowArrowInvoker", "NativeDamagePhaseMixin", "NativeDamagePhasePlugin", "NativeMobEffectBlendFieldsMixin", "NativeMobEffectInstanceFieldsMixin", "NativePotionContentsFieldsMixin", "NativeReadCutProjectileMixin", "NativeReadCutServerPlayerMixin", "OwnerStatueWearInventoryMixin", "OwnerStatueWearItemStackMixin"]}
weapon |= {name for name in classes if name.split("$", 1)[0] in weapon}
protected = {name for name in classes if name.startswith(tuple(prefix + part for part in ["boss/", "entity/", "block/", "client/model/", "client/render/"]))}
protected -= weapon
protected |= {prefix + name for name in ["FTBossMod", "item/FTBossItems", "item/FTCreativeTabs", "skill/FTNetwork", "skill/FTNetworkRegistration", "api/OptionalWeaponModule", "api/OptionalWeaponModule$Service", "api/OptionalWeaponModule$1"]}
while True:
    more = {name for name, refs in graph.items() if set(refs) & weapon} - weapon - protected
    if not more:
        break
    weapon |= more
frontier = {name: sorted(set(graph[name]) & weapon) for name in sorted(protected) if set(graph[name]) & weapon}
core = set(classes) - weapon
attempt = WORK / "build" / ("boss-split-" + uuid.uuid4().hex[:8])
attempt.mkdir(parents=True)
plan = {"classes": len(classes), "coreClasses": len(core), "weaponClasses": len(weapon), "sharedFrontier": frontier, "core": sorted(core), "weapons": sorted(weapon), "classInputs": {name: {"path": str(path), "sha256": digest(path)} for name, path in sorted(classes.items())}, "originalApplication": str(original), "compileContext": str(context_path), "pausedCandidate": str(candidate_receipt), "noWeaponFunctionalDevelopment": True, "sourceGraphHeuristicIsNotRuntimeAcceptance": True, "bossIndependentLoadAccepted": False, "weaponStandaloneJarAccepted": False, "deployed": False}
save(attempt / "partition-plan.json", plan)
print(json.dumps({"attempt": str(attempt), "coreClasses": len(core), "weaponClasses": len(weapon), "sharedFrontier": {name.removeprefix(prefix): [ref.removeprefix(prefix) for ref in refs] for name, refs in frontier.items()}}), flush=True)
assert not frontier, "Core still directly depends on removed weapon classes"
proposal = attempt / "boss-core-proposal"
proposal.mkdir()
advanced_ids = {"ftboss:" + material + "_" + family for material in ["wooden", "stone", "iron", "golden", "diamond", "netherite"] for family in ["bow", "crossbow"]}
moved_resources = {}
filtered_tags = {}

def resource_mentions(value):
    if isinstance(value, str):
        return value in advanced_ids
    if isinstance(value, dict):
        return any(resource_mentions(entry) for entry in value.values())
    if isinstance(value, list):
        return any(resource_mentions(entry) for entry in value)
    return False

for path in original.rglob("*"):
    if not path.is_file() or path.suffix == ".class" or path.name.endswith(".mixins.json") and path.name != "ftboss.mixins.json":
        continue
    relative = path.relative_to(original).as_posix()
    if path.suffix == ".json" and relative.startswith("data/"):
        value = load(path)
        if resource_mentions(value):
            if "/tags/" in relative and isinstance(value.get("values"), list):
                filtered = dict(value)
                filtered["values"] = [entry for entry in value["values"] if not resource_mentions(entry)]
                if filtered["values"]:
                    target = proposal / relative
                    target.parent.mkdir(parents=True, exist_ok=True)
                    target.write_text(json.dumps(filtered, ensure_ascii=False, indent=2), encoding="utf-8")
                    filtered_tags[relative] = {"originalSha256": digest(path), "coreSha256": digest(target)}
                    continue
            moved_resources[relative] = {"path": str(path), "sha256": digest(path)}
            continue
    target = proposal / path.relative_to(original)
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(path, target)
for name in sorted(core):
    target = proposal / (name + ".class")
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(classes[name], target)
metadata = proposal / "META-INF/neoforge.mods.toml"
text = metadata.read_text(encoding="utf-8")
text = re.sub(r'\[\[mixins\]\]\s*config\s*=\s*"[^"]+"\s*', "", text)
text += '\n[[mixins]]\nconfig = "ftboss.mixins.json"\n'
text = text.replace('version = "1.21.1-1.0.0"', 'version = "1.21.1-boss-split.1"')
metadata.write_text(text, encoding="utf-8")
jar = attempt / "ftboss-boss-core-1.21.1-split1-CANDIDATE.jar"
with zipfile.ZipFile(jar, "x", compression=zipfile.ZIP_DEFLATED) as stream:
    for path in sorted(proposal.rglob("*")):
        if path.is_file():
            stream.write(path, path.relative_to(proposal).as_posix())
with zipfile.ZipFile(jar) as stream:
    assert stream.testzip() is None
    assert sum(name.endswith(".class") for name in stream.namelist()) == len(core)
receipt = {"attempt": str(attempt), "proposal": str(proposal), "jar": str(jar), "jarSha256": digest(jar), "jarBytes": jar.stat().st_size, "coreClasses": len(core), "removedWeaponClasses": len(weapon), "coreToWeaponClassReferences": 0, "bossIndependentLoadAccepted": False, "weaponStandaloneJarAccepted": False, "installable": False, "deployed": False, "partitionPlanSha256": digest(attempt / "partition-plan.json"), "inputHashes": {str(path): digest(path) for path in [Path(__file__), context_path, candidate_receipt, initial_report, HISTORY / "tools/weapon_dependency_frontier.py"]}}
save(attempt / "resource-partition.json", {"advancedItemIds": sorted(advanced_ids), "movedResources": moved_resources, "filteredTags": filtered_tags})
receipt["resourcePartitionSha256"] = digest(attempt / "resource-partition.json")
save(attempt / "build-receipt.json", receipt)
print(json.dumps({key: receipt[key] for key in ["jar", "coreClasses", "removedWeaponClasses", "coreToWeaponClassReferences"]}), flush=True)
