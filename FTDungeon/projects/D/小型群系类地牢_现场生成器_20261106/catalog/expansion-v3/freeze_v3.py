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
from freeze_catalog import INDEX, digest, load, save, inside
from nbtio import Structure, plain

EXPECTED_ARTIFACT = "f7d73680b20d390a6f08a090fea66b66000deb33bc0a6556ad282340035c6188"
SELECTED = {1, 2, 3, 4, 7, 8, 9}
NORMALS = {"north": (0, 0, -1), "east": (1, 0, 0), "south": (0, 0, 1), "west": (-1, 0, 0)}
AIR = {"minecraft:air", "minecraft:cave_air", "minecraft:void_air"}


def copy_locked(source, relative, expected=None):
    source = Path(source)
    before = digest(source)
    if expected and before != expected:
        raise ValueError("source lock mismatch: " + str(source))
    target = ROOT / relative
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(source, target)
    if digest(target) != before or digest(source) != before:
        raise ValueError("changed during snapshot: " + str(source))
    return {"source": str(source), "path": relative, "sha256": before}


def state_at(structure, position):
    return {"position": list(position), "inside": inside(position, structure.size), "state": plain(structure.get(position).as_tag()) if inside(position, structure.size) else None}


def sample_port(raw, structure, number):
    facing = raw.get("facing", raw.get("side"))
    feet = raw.get("position", raw.get("feet"))
    width = raw["width"]
    height = raw.get("height", raw.get("minimum_height", raw.get("clearHeight")))
    if facing not in NORMALS or not isinstance(feet, list) or len(feet) != 3 or type(height) is not int:
        raise ValueError("missing original port semantics")
    normal = NORMALS[facing]
    lateral = 0 if facing in {"north", "south"} else 2
    sections = []
    for depth in range(7):
        columns = []
        for offset in range(-(width // 2), (width - 1) // 2 + 1):
            center = [feet[axis] - normal[axis] * depth for axis in range(3)]
            center[lateral] += offset
            cells = [state_at(structure, (center[0], center[1] + rise, center[2])) for rise in range(-2, height + 3)]
            aperture = [cell for cell in cells if feet[1] <= cell["position"][1] < feet[1] + height]
            columns.append({"offset": offset, "cells": cells, "declaredRectangularAir": all(cell["inside"] and cell["state"]["Name"] in AIR for cell in aperture)})
        sections.append({"inwardDepth": depth, "columns": columns})
    boundary = [cell for column in sections[0]["columns"] for cell in column["cells"] if feet[1] <= cell["position"][1] < feet[1] + height]
    water = any(cell["state"] and (cell["state"]["Name"] == "minecraft:water" or cell["state"].get("Properties", {}).get("waterlogged") == "true") for cell in boundary)
    mixed = water and any(cell["state"] and cell["state"]["Name"] in AIR for cell in boundary)
    port = {"id": f"p{number}_{facing}", "feet": dict(zip(("x", "y", "z"), feet)), "facing": facing, "width": width, "height": height, "medium": "wet" if water and not mixed else "dry" if not water else "UNRESOLVED_MIXED"}
    return {"port": port, "rawOriginal": raw, "mediumEvidence": {"sample": "all declared boundary aperture cells, not whole-room biome label", "waterPresent": water, "mixedAirWater": mixed, "roomFluidDoesNotImplyWetDoor": True}, "sections": sections, "rectangularBoundaryClear": all(column["declaredRectangularAir"] for column in sections[0]["columns"]), "rectangularFirstInwardClear": all(column["declaredRectangularAir"] for column in sections[1]["columns"]), "nativeCollisionOrStairTraversability": "NOT_CERTIFIED_BY_THIS_STATIC_SAMPLE"}


def validate_payload(payload, state):
    data = payload or {}
    name = state.name
    expected = "minecraft:chest" if name == "minecraft:chest" else "minecraft:barrel" if name == "minecraft:barrel" else "minecraft:banner" if name.endswith("_banner") else None
    if expected is None:
        return "unsupported BE state"
    if name == "minecraft:chest" and state.get("type") != "single":
        return "paired/non-single chest unsupported"
    if payload is not None and data.get("id") != expected:
        return "BE type/state mismatch"
    allowed = {"id", "x", "y", "z", "CustomName", "Patterns"} if expected.endswith("banner") else {"id", "x", "y", "z", "Items", "CustomName", "Lock"}
    if set(data) - allowed:
        return "unsupported fields: " + str(sorted(set(data) - allowed))
    if data.get("Items"):
        return "nonempty items need native registry/slot/count validation"
    if data.get("Patterns"):
        return "nonempty patterns need exact pattern/color policy check"
    if "CustomName" in data:
        try:
            custom = json.loads(data["CustomName"])
            if not isinstance(custom, dict) or set(custom) != {"text"} or not isinstance(custom["text"], str) or len(data["CustomName"]) > 1024:
                return "unsupported custom name"
        except (ValueError, TypeError):
            return "invalid custom name"
    if "Lock" in data and (not isinstance(data["Lock"], str) or len(data["Lock"]) > 128):
        return "invalid lock"
    return None


def main():
    frozen = {}
    for prefix in (CATALOG, CATALOG / "expansion-v2"):
        inventory = load(prefix / "SHA256SUMS.json")
        for record in inventory["files"]:
            if digest(prefix / record["path"]) != record["sha256"]:
                raise ValueError("prior frozen inventory differs: " + record["path"])
        for filename in ("ready.json", "progress.json", "SHA256SUMS.json"):
            frozen[str((prefix / filename).relative_to(CATALOG))] = digest(prefix / filename)
    runtime = PROJECT / "runtime/next"
    ready = load(runtime / "ready.json")
    if ready["artifactSha256"] != EXPECTED_ARTIFACT:
        raise ValueError("unexpected runtime capability contract")
    runtime_files = [copy_locked(runtime / path, "runtime-next-snapshot/" + path, expected) for path, expected in ready["files"].items()]
    runtime_files.append(copy_locked(runtime / "ready.json", "runtime-next-snapshot/ready.json"))
    runtime_text = (ROOT / "runtime-next-snapshot/src/main/java/cn/ft/biomedungeon/runtime/TemplateCatalog.java").read_text(encoding="utf-8")
    allowed = set(re.findall(r'"([a-z_]+)"', runtime_text.split("STATIC_BLOCKS = Set.of(", 1)[1].split(");", 1)[0]))
    fixture_records = load(runtime / "fixtures.json")
    save(ROOT / "runtime-contract.json", {"artifactSha256": EXPECTED_ARTIFACT, "snapshotFiles": runtime_files, "runtimeJournalVersion": ready["runtimeJournalVersion"], "staticBlocks": sorted(allowed), "beTypes": ["minecraft:chest single", "minecraft:barrel", "minecraft:banner"], "unsupported": ["entity payloads", "fluids/waterlogged/fire", "paired chests", "active/loot/commands/capability BE fields", "unreviewed palette states"], "evidenceStatus": "PRODUCER_WORLD_EVIDENCE_ONLY_NOT_INDEPENDENT_ACCEPTANCE", "producerFixtures": fixture_records})
    records, candidates = [], []
    for item in load(INDEX)["items"]:
        if item["group"] != 8 or item["image"] not in SELECTED:
            continue
        identifier = item["id"].lower().replace("-", "_")
        copy_record = copy_locked(item["candidate"], f"candidate-resources/data/biomedungeon/structures/{identifier}.nbt", item["sha256"])
        structure = Structure.read(ROOT / copy_record["path"])
        meta_path = Path(item["candidate"]).with_suffix(".json")
        meta = load(meta_path)
        if meta.get("sha256") != item["sha256"]:
            raise ValueError("unbound original metadata")
        evidence = [copy_locked(meta_path, f"evidence/{identifier}/original-metadata.json"), copy_locked(item["independent_evidence"], f"evidence/{identifier}/file-audit.json")]
        evidence.extend(copy_locked(path, f"evidence/{identifier}/acceptance-{number}" + Path(path).suffix) for number, path in enumerate(item["acceptance_records"]))
        for name in ("route-input.json", "independent-routes.json", "independent-native-frozen.json"):
            route_path = Path(item["independent_evidence"]).parent / name
            if route_path.exists():
                evidence.append(copy_locked(route_path, f"evidence/{identifier}/" + name))
        if item["sha256"] not in Path(item["independent_evidence"]).read_text(encoding="utf-8"):
            raise ValueError("independent source hash absent")
        raw_ports = meta.get("connections", meta.get("ports", []))
        ports = [sample_port(raw, structure, number) for number, raw in enumerate(raw_ports)]
        counts = Counter(block.name for block in structure.blocks.values())
        payloads = []
        for position, block in structure.blocks.items():
            if position in structure.block_entities or block.name in {"minecraft:chest", "minecraft:barrel"} or block.name.endswith("_banner"):
                original = plain(structure.block_entities[position]) if position in structure.block_entities else None
                payloads.append({"position": list(position), "state": plain(block.as_tag()), "originalNBT": original, "usesNativeDefault": original is None, "staticV2PolicyError": validate_payload(original, block)})
        unsupported = sorted(name for name in counts if name.split(":")[-1] not in allowed)
        fluids = {name: count for name, count in counts.items() if name in {"minecraft:water", "minecraft:lava", "minecraft:fire", "minecraft:soul_fire"}}
        waterlogged = sum(block.get("waterlogged") == "true" for block in structure.blocks.values())
        blockers = ["INDEPENDENT_EXPANSION_RUNTIME_ACCEPTANCE_PENDING"]
        if unsupported:
            blockers.append("OUTSIDE_V2_ALLOWLIST: " + ",".join(unsupported))
        if fluids or waterlogged:
            blockers.append("FLUID_TICK_CONTAINMENT_AND_VINE_WORLD_VALIDATION_PENDING")
        if any(payload["staticV2PolicyError"] for payload in payloads):
            blockers.append("PAYLOAD_POLICY_MISMATCH")
        if any(not port["rectangularFirstInwardClear"] for port in ports):
            blockers.append("STAIR_PROFILE_NEEDS_NATIVE_GEOMETRY_NOT_RECTANGULAR_ERASURE")
        constraints = []
        if len({port["port"]["feet"]["y"] for port in ports}) > 1:
            constraints.append("MULTILEVEL_ORIGINAL_PORTS_PRESERVE_ACTUAL_Y_ALIGNMENT_NOT_A_NEW_PAYLOAD_BLOCKER")
        if item["image"] in {1, 7}:
            constraints.append("ACTUAL_INWARD_STAIR_AND_NATIVE_BODY_SWEEP_REQUIRED_NO_AIR_CARVING")
        record = {"id":item["id"],"sha256":item["sha256"],"size":list(structure.size),"dataVersion":structure.data_version,"fullExplicitVolume":len(structure.blocks)==structure.size[0]*structure.size[1]*structure.size[2],"entityCount":len(structure.entities),"outsideV2Allowlist":unsupported,"fluidsAndFire":fluids,"waterloggedCells":waterlogged,"payloads":payloads,"ports":ports,"blockers":blockers,"geometryConstraints":constraints,"accepted":False,"sourceEvidence":evidence,"resource":copy_record}
        records.append(record)
        candidates.append({"id":identifier,"biome":"s8","resourceId":"biomedungeon:"+identifier,"sha256":item["sha256"],"size":dict(zip(("x","y","z"),structure.size)),"ports":[port["port"] for port in ports],"role":{7:"entrance",9:"terminal"}.get(item["image"],"normal"),"accepted":False,"independentSourceEvidence":evidence,"expansionBlockers":blockers})
    save(ROOT / "seven-original-room-review.json", records)
    save(ROOT / "candidate-manifest.json", {"schemaVersion":1,"scope":"UNACCEPTED_NON_CORRIDOR_ORIGINALS_NOT_FOR_RUNTIME_PACKAGING","templates":candidates})
    save(ROOT / "core-stairs-consumption.json", {"runtimeArtifactSha256":EXPECTED_ARTIFACT,"coordinates":"Original local source axes; inward depth = feet - outward normal * depth. cells include feetY-2 through feetY+height+2; no air-carving and no claimed native collision approximation.","semantics":"medium is boundary aperture water occupancy, not biome or internal fluid. Dry stairs stay dry; room-internal lava does not make a dry doorway wet.","rooms":[record for record in records if record["id"] in {"S8-01","S8-07"}],"requiredNativeChecks":["VoxelShape swept body path and step-up clearance at actual half-block support levels","full declared width vs usable walking lane; cannot assume rectangular full width at inward stair rise","preserve north/south feet height differences and rotate stair facing/shape correctly","used/unused aperture caps and corridor landing must not carve stairs or leave a one-block lip"],"nativeTraversability":"NOT_RUN_BY_CATALOG"})
    save(ROOT / "baseline-lock.json", {"priorFrozenFiles":frozen,"priorInventoriesVerified":True,"runtimeArtifactSha256":EXPECTED_ARTIFACT})
    print("PASS exact runtime-next contract snapshotted files=" + str(len(runtime_files)))
    print("PASS 7 non-corridor originals copied unchanged with accepted=false")
    for record in records:
        print(record["id"], "BE="+str(len(record["payloads"])), "outside="+str(record["outsideV2Allowlist"]), "blockers="+str(record["blockers"]))


if __name__ == "__main__":
    main()
