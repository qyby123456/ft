import hashlib
import json
import re
import shutil
import sys
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parent
CATALOG = ROOT.parent
PROJECT = CATALOG.parent
sys.path.insert(0, str(CATALOG))
from nbtio import Structure, plain
from freeze_catalog import INDEX, DELIVERY, digest, load, save, read_port

BE_NAMES = {"chest", "trapped_chest", "barrel", "furnace", "blast_furnace", "smoker", "campfire", "soul_campfire", "brewing_stand", "spawner", "lectern", "enchanting_table"}


def snapshot(path, relative):
    destination = ROOT / relative
    destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(path, destination)
    if digest(path) != digest(destination):
        raise ValueError("snapshot changed during copy: " + str(path))
    return {"source": str(path), "path": relative, "sha256": digest(destination)}


def main():
    baseline = load(CATALOG / "SHA256SUMS.json")
    for record in baseline["files"]:
        if digest(CATALOG / record["path"]) != record["sha256"]:
            raise ValueError("frozen baseline already differs: " + record["path"])
    frozen = {name: digest(CATALOG / name) for name in ("ready.json", "progress.json", "SHA256SUMS.json", "manifest.json")}
    core_ready = load(PROJECT / "core/ready.json")
    core_records = []
    for record in core_ready["sources"]:
        source = PROJECT / "core" / record["path"]
        if digest(source) != record["sha256"]:
            raise ValueError("current core not matching ready: " + str(source))
        core_records.append(snapshot(source, "core-snapshot/" + source.name))
    core_records.append(snapshot(PROJECT / "core/ready.json", "core-snapshot/ready.json"))
    runtime_source = PROJECT / "runtime/src/main/java/cn/ft/biomedungeon/runtime/TemplateCatalog.java"
    runtime_record = snapshot(runtime_source, "runtime-snapshot/TemplateCatalog.java")
    runtime_text = runtime_source.read_text(encoding="utf-8")
    allow_section = runtime_text.split("STATIC_BLOCKS = Set.of(", 1)[1].split(");", 1)[0]
    allowed = set(re.findall(r'"([a-z_]+)"', allow_section))
    records = []
    for item in load(INDEX)["items"]:
        if item["image"] >= 10:
            continue
        source = Path(item["candidate"])
        if digest(source) != item["sha256"]:
            raise ValueError("source hash changed: " + item["id"])
        structure = Structure.read(source)
        states = Counter(block.name for block in structure.blocks.values())
        be_blocks = [{"position": list(position), "block": block.name, "hasExplicitPayload": position in structure.block_entities} for position, block in structure.blocks.items() if block.name.split(":")[-1] in BE_NAMES or block.name.endswith(("_banner", "_sign", "_hanging_sign")) or position in structure.block_entities]
        payloads = [{"position": list(position), "block": structure.get(position).name, "nbt": plain(payload)} for position, payload in structure.block_entities.items()]
        fluids = [{"name": name, "count": count} for name, count in states.items() if name in {"minecraft:lava", "minecraft:water", "minecraft:fire", "minecraft:soul_fire"}]
        waterlogged = sum(block.get("waterlogged") == "true" for block in structure.blocks.values())
        dynamic = [{"name": name, "count": count} for name, count in states.items() if name in {"minecraft:sand", "minecraft:red_sand", "minecraft:gravel", "minecraft:snow", "minecraft:ice", "minecraft:budding_amethyst", "minecraft:vine"} or name.endswith(("_leaves", "_concrete_powder"))]
        meta_path = source.with_suffix(".json")
        meta = load(meta_path) if meta_path.exists() else None
        raw_ports = meta.get("connections", meta.get("ports", [])) if meta else []
        port_checks = []
        if isinstance(raw_ports, list):
            for number, raw in enumerate(raw_ports):
                try:
                    port, checked = read_port(raw, structure, number)
                    port_checks.append({"port": port, "strictExistingMvpAperture": "PASS", "cellsChecked": len(checked)})
                except (ValueError, KeyError, TypeError, AttributeError) as error:
                    port_checks.append({"raw": raw, "strictExistingMvpAperture": "REJECT", "reason": str(error)})
        outside = sorted(name for name in states if name.split(":")[-1] not in allowed)
        requirements = []
        if be_blocks or payloads:
            requirements.append("BLOCK_ENTITY_PRESERVATION_ROTATION_READBACK_PERSISTENT_RESTORE")
        if structure.entities:
            requirements.append("ENTITY_UUID_ROTATION_OWNERSHIP_PERSISTENCE")
        if fluids or waterlogged:
            requirements.append("FLUID_CONTAINMENT_UPDATE_ORDER_TICK_PROPAGATION_WORLD_TEST")
        if dynamic:
            requirements.append("FALLING_RANDOM_NEIGHBOR_TICK_AND_ATTACHMENT_WORLD_TEST")
        if outside:
            requirements.append("NATIVE_BLOCK_STATE_ALLOWLIST_REVIEW")
        if not meta or meta.get("sha256") != item["sha256"]:
            requirements.append("MISSING_HASH_BOUND_ORIGINAL_INTERFACE_METADATA")
        if not port_checks or any(check["strictExistingMvpAperture"] != "PASS" for check in port_checks):
            requirements.append("ORIGINAL_INTERFACE_GEOMETRY_OR_SUPPORT_REVIEW")
        records.append({"id": item["id"], "group": item["group"], "image": item["image"], "intendedRole": {1:"altar_hall", 2:"library_storage", 3:"small_treasure_room", 4:"main_hall", 5:"straight_passage", 6:"corner_passage", 7:"entrance", 8:"garden", 9:"treasury"}[item["image"]], "source": str(source), "sha256": item["sha256"], "size": list(structure.size), "dataVersion": structure.data_version, "explicitCellCount": len(structure.blocks), "blockEntities": payloads, "blockEntityBlocks": be_blocks, "entities": [plain(entity) for entity in structure.entities], "fluidsAndFire": fluids, "waterloggedCells": waterlogged, "dynamicScreen": dynamic, "outsideCurrentRuntimeAllowlist": outside, "originalMetadata": str(meta_path) if meta else None, "originalMetadataSha256": digest(meta_path) if meta else None, "rawOriginalPorts": raw_ports, "strictMvpPortChecks": port_checks, "requirements": requirements, "compatibleWithUnchangedCurrentMvpStaticScreen": not requirements, "independentFileAudit": item["independent_evidence"], "acceptanceRecords": item["acceptance_records"], "acceptedForExpansionRuntime": False})
    save(ROOT / "all-72-expansion-audit.json", records)
    s8 = [record for record in records if record["group"] == 8]
    for record in s8:
        identifier = record["id"].lower().replace("-", "_")
        record["frozenEvidence"] = [snapshot(record["independentFileAudit"], f"evidence/{identifier}/file-audit.json")]
        for number, acceptance in enumerate(record["acceptanceRecords"]):
            record["frozenEvidence"].append(snapshot(acceptance, f"evidence/{identifier}/acceptance-{number}" + Path(acceptance).suffix))
        if record["originalMetadata"]:
            record["frozenEvidence"].append(snapshot(record["originalMetadata"], f"evidence/{identifier}/original-metadata.json"))
    save(ROOT / "s8-complete-biome-requirements.json", {"biome":"s8", "target":"all nine architectural roles including true entrance/main hall/treasury; no furniture deletion", "templates":s8, "currentPoolNotCompleteBiome":True})
    compatible = [record["id"] for record in records if record["compatibleWithUnchangedCurrentMvpStaticScreen"]]
    save(ROOT / "compatibility-summary.json", {"decodedSingleRooms":len(records), "currentRuntimeStaticScreenCompatibleIds":compatible, "additionalUnchangedTemplatesAdmitted":[], "promotion":"NONE: candidate inspection does not replace independent/native runtime validation", "s8RequiredIds":{"entrance":"S8-07", "mainHall":"S8-04", "treasury":"S8-09"}, "requirementCounts":dict(Counter(requirement for record in records for requirement in record["requirements"]))})
    save(ROOT / "snapshot-lock.json", {"baselineFrozenFiles":frozen, "baselineInventoryCheckedFiles":len(baseline["files"]), "currentCore":core_records, "runtime":runtime_record, "sourceIndex":snapshot(INDEX, "evidence/FINAL-80-SOURCE-INDEX.json"), "noProductionResourcesChanged":True})
    print("PASS decoded 72 source rooms; unchanged current-MVP static screen:", compatible)
    print("S8 complete role requirements frozen; no resources promoted; original baseline verified")


if __name__ == "__main__":
    main()
