import collections
import hashlib
import json
import pathlib
import re
import sys

root = pathlib.Path(__file__).resolve().parents[3]
work = root / "work/session-boss"
history = pathlib.Path("D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022")
sys.path.insert(0, str(history / "tools"))
from weapon_dependency_frontier import references

receipt = root / "work/session-lease/reports/native-repair-runtime-exact-melee-configured-paid-849680bb.json"
data = json.loads(receipt.read_text(encoding="utf-8"))
proposal = pathlib.Path(data["attempt"]) / "ftboss-unpackaged-proposal"
classes = {path.relative_to(proposal).as_posix()[:-6]: path for path in proposal.rglob("*.class")}
graph = {name: sorted(references(path.read_bytes()) & classes.keys() - {name}) for name, path in classes.items()}
prefix = "net/farmingtales/ftboss/"
weapon_patterns = [
    r"item/(?:FoodWeapon|Owner|Native(?:Expected|Timer|Lifecycle|Item|Component|Nbt|Raw|Observation|Stack|Capability|Provider|Trident|Repair|Material|Map|ReadCut)|Sword(?:Cavity|Material|Native)|Weapon(?:Build|Durability|Construction|Bond)|Gourmet|Growth|Ranged)",
    r"transaction/", r"util/foundations/", r"util/(?:FoodWeapon|Sword(?:Cavity|Material|Native)|Weapon(?:Build|Durability|Construction|Bond)|NonManorComponentMigration)",
    r"network/(?:StatueMenu|NativeStatueCavity|FourWeapon)", r"client/(?:statue|weapon|gourmet)",
]
weapon = {name for name in classes if any(re.match(prefix + pattern, name) for pattern in weapon_patterns)}
boss = {name for name in classes if name.startswith(prefix + "boss/") or name.startswith(prefix + "client/model/") or name.startswith(prefix + "client/render/")}
boss_weapon_edges = {name: [target for target in graph[name] if target in weapon] for name in sorted(boss)}
boss_weapon_edges = {name: targets for name, targets in boss_weapon_edges.items() if targets}
bootstrap = {name: graph[name] for name in sorted(classes) if name in [prefix + "FTBossMod", prefix + "item/FTItems"]}
inventory = json.loads((history / "reports/migration-inventory.json").read_text(encoding="utf-8"))
missing_boss = [name for name in inventory["forgeOnlyTopLevelClassPaths"] if any(token in name for token in ["/boss/", "/client/model/", "/client/render/"])]
archive = work / "archives/weapons-paused-a08a7373/archive-receipt.json"
frozen = json.loads(archive.read_text(encoding="utf-8"))
assert frozen["failures"] == 0 and hashlib.sha256(pathlib.Path(frozen["archive"]).read_bytes()).hexdigest() == frozen["archiveSha256"]
output = work / "reports/split-boundary-initial.json"
output.parent.mkdir(parents=True, exist_ok=True)
report = {"inputApplication": str(proposal), "applicationClasses": len(classes), "conservativeWeaponCandidateClasses": len(weapon), "bossModelRendererClasses": len(boss), "directBossToWeaponCandidateEdges": boss_weapon_edges, "bootstrapDependencies": bootstrap, "weaponClassCandidates": sorted(weapon), "allApplicationClassReferences": graph, "frozenOriginalBossPathsAbsentFromInitialNfBaseline": missing_boss, "baselineMissingPathsAreNotCurrentFeatureAcceptance": True, "weaponArchive": str(archive), "bossIndependentJarAccepted": False, "weaponStandaloneJarAccepted": False, "deployed": False, "inputHashes": {str(receipt): hashlib.sha256(receipt.read_bytes()).hexdigest(), str(archive): hashlib.sha256(archive.read_bytes()).hexdigest(), str(pathlib.Path(__file__)): hashlib.sha256(pathlib.Path(__file__).read_bytes()).hexdigest(), str(history / "tools/weapon_dependency_frontier.py"): hashlib.sha256((history / "tools/weapon_dependency_frontier.py").read_bytes()).hexdigest()}}
with output.open("x", encoding="utf-8") as stream:
    json.dump(report, stream, ensure_ascii=False, indent=2)
print(json.dumps({"classes": len(classes), "weaponCandidates": len(weapon), "bossClasses": len(boss), "bossToWeaponCallers": len(boss_weapon_edges), "bootstrap": {name: len(targets) for name, targets in bootstrap.items()}}))
