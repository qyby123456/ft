import hashlib
import json
import shutil
from collections import Counter
from pathlib import Path

from nbtio import Structure
from inspect_sources import INDEX, DELIVERY

ROOT = Path(__file__).resolve().parent
SAFE_POOL = {"S8-05": "normal", "S8-06": "normal"}
STATIC_STATES = {"air", "amethyst_block", "amethyst_cluster", "calcite", "chain", "deepslate_brick_stairs", "deepslate_bricks", "deepslate_tiles", "lantern", "polished_deepslate", "smooth_quartz_stairs", "cobbled_deepslate"}
DIRECTIONS = {"north": (0, 0, -1), "east": (1, 0, 0), "south": (0, 0, 1), "west": (-1, 0, 0)}
AIR = {"minecraft:air", "minecraft:cave_air", "minecraft:void_air"}
SUPPORT = {"minecraft:" + name for name in STATIC_STATES if name not in {"air", "amethyst_cluster", "chain", "deepslate_brick_stairs", "lantern", "smooth_quartz_stairs"}}


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def load(path):
    return json.loads(Path(path).read_text(encoding="utf-8"))


def save(path, value):
    path = Path(path)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def vector(values):
    return dict(zip(("x", "y", "z"), values))


def inside(position, size):
    return all(type(coordinate) is int and 0 <= coordinate < limit for coordinate, limit in zip(position, size))


def read_port(raw, structure, number):
    facing = raw.get("facing", raw.get("side"))
    feet = raw.get("position", raw.get("feet"))
    width = raw["width"]
    height = raw.get("height", raw.get("clearHeight", raw.get("minimum_height")))
    if facing not in DIRECTIONS or not isinstance(feet, list) or len(feet) != 3:
        raise ValueError("missing original-source port coordinates/facing")
    if type(width) is not int or width % 2 != 1 or not 1 <= width <= 7:
        raise ValueError("even/asymmetric or unsupported width requires explicit future contract")
    if type(height) is not int or not 2 <= height <= 8 or not inside(feet, structure.size):
        raise ValueError("invalid height or feet")
    boundary_axis = 2 if facing in ("north", "south") else 0
    boundary = 0 if facing in ("north", "west") else structure.size[boundary_axis] - 1
    if feet[boundary_axis] != boundary:
        raise ValueError("port not at actual source boundary")
    lateral_axis = 0 if boundary_axis == 2 else 2
    outward = DIRECTIONS[facing]
    checked = []
    for depth in (0, 1):
        for offset in range(-(width // 2), width // 2 + 1):
            base = [feet[axis] - outward[axis] * depth for axis in range(3)]
            base[lateral_axis] += offset
            floor = (base[0], base[1] - 1, base[2])
            if not inside(floor, structure.size) or structure.get(floor).name not in SUPPORT:
                raise ValueError(f"unproven full-floor support at {floor}: {structure.get(floor).name}")
            for rise in range(height):
                position = (base[0], base[1] + rise, base[2])
                if not inside(position, structure.size) or structure.get(position).name not in AIR:
                    raise ValueError(f"source aperture obstructed at {position}: {structure.get(position).name}")
                checked.append(position)
    return {"id": f"p{number}_{facing}", "feet": vector(feet), "facing": facing, "width": width, "height": height, "medium": "dry"}, checked


def freeze_evidence(path, destination):
    destination = ROOT / "evidence" / destination
    destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(path, destination)
    return {"source": str(path), "snapshot": destination.relative_to(ROOT).as_posix(), "sha256": digest(destination)}


def build():
    index = load(INDEX)
    delivery = load(DELIVERY)
    accepted, rejected, audits = [], [], []
    source_records = []
    for item in sorted(index["items"], key=lambda entry: (entry["group"], entry["image"])):
        source = Path(item["candidate"])
        if item["image"] == 10:
            rejected.append({"id": item["id"], "reason": "OVERVIEW_NEVER_A_ROOM", "sha256": item["sha256"]})
            continue
        reasons = []
        actual_hash = digest(source)
        if actual_hash != item["sha256"] or not item["current_hash_matches_independent_evidence"]:
            reasons.append("SOURCE_HASH_OR_INDEPENDENT_INDEX_MISMATCH")
        structure = Structure.read(source)
        if len(structure.blocks) != structure.size[0] * structure.size[1] * structure.size[2]:
            reasons.append("NOT_FULL_EXPLICIT_VOLUME")
        if any(not inside(position, structure.size) for position in structure.blocks):
            reasons.append("OUT_OF_BOUNDS")
        counts = Counter(block.name for block in structure.blocks.values())
        fluids = {name: count for name, count in counts.items() if name in {"minecraft:water", "minecraft:lava", "minecraft:fire", "minecraft:soul_fire"}}
        if fluids or any(block.get("waterlogged") == "true" for block in structure.blocks.values()):
            reasons.append("FLUID_OR_FIRE_REQUIRES_WORLD_VALIDATION")
        if structure.entities:
            reasons.append("ENTITY_PAYLOAD_UNSUPPORTED_BY_RUNTIME_MVP")
        if structure.block_entities:
            reasons.append("BLOCK_ENTITY_PAYLOAD_UNSUPPORTED_BY_RUNTIME_MVP")
        unsupported = sorted(name for name in counts if name.removeprefix("minecraft:") not in STATIC_STATES)
        if unsupported:
            reasons.append("OUTSIDE_CONSERVATIVE_STATIC_STATE_ALLOWLIST")
        source_meta = source.with_suffix(".json")
        ports, aperture_checks = [], []
        meta = None
        if source_meta.exists():
            meta = load(source_meta)
            if meta.get("sha256") != actual_hash:
                reasons.append("SOURCE_METADATA_HASH_NOT_BOUND_TO_SOURCE")
            raw_ports = meta.get("connections", meta.get("ports", []))
            if not raw_ports:
                reasons.append("NO_SOURCE_PORT_METADATA")
            for number, raw in enumerate(raw_ports):
                try:
                    port, checked = read_port(raw, structure, number)
                    ports.append(port)
                    aperture_checks.extend(checked)
                except (ValueError, KeyError, TypeError, AttributeError) as error:
                    reasons.append("PORT_REJECTED: " + str(error))
        else:
            reasons.append("MISSING_HASH_BOUND_SOURCE_METADATA")
        evidence = Path(item["independent_evidence"])
        if not evidence.exists() or actual_hash not in evidence.read_text(encoding="utf-8"):
            reasons.append("INDEPENDENT_FILE_AUDIT_HASH_NOT_FOUND")
        records = [Path(path) for path in item["acceptance_records"]]
        if not any(path.exists() and actual_hash in path.read_text(encoding="utf-8") for path in records):
            reasons.append("EXPLICIT_ACCEPTANCE_RECORD_WITH_HASH_MISSING")
        if item["id"] not in SAFE_POOL:
            reasons.append("NOT_IN_FROZEN_MINIMAL_MVP_SELECTION")
        layout_path = Path(next(group["directory"] for group in delivery["groups"] if group["group"] == item["group"])) / "layout.json"
        module = next(module for module in load(layout_path)["modules"] if module["image"] == item["image"])
        report = {"id": item["id"], "sourceSha256": actual_hash, "size": vector(structure.size), "blocks": len(structure.blocks), "explicitAir": sum(counts[name] for name in AIR), "blockEntities": len(structure.block_entities), "entities": len(structure.entities), "fluids": fluids, "unsupportedBlockNames": unsupported, "portsReadFromSource": ports, "layoutPortsDiagnosticOnly": module["ports"], "layoutPath": str(layout_path), "portApertureCellsChecked": len(aperture_checks), "reasons": reasons}
        audits.append(report)
        if reasons:
            rejected.append({"id": item["id"], "sha256": actual_hash, "reasons": reasons})
            continue
        identifier = item["id"].lower().replace("-", "_")
        output = ROOT / "data/biomedungeon/structures" / (identifier + ".nbt")
        output.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(source, output)
        if digest(output) != actual_hash:
            raise ValueError("copied template hash mismatch")
        provenance = {"sourceRoomId": item["id"], "unchangedBytes": True, "source": str(source), "sourceSha256": actual_hash, "independentFileAudit": freeze_evidence(evidence, f"{identifier}/file-audit.json"), "sourceMetadata": freeze_evidence(source_meta, f"{identifier}/source-metadata.json"), "layout": freeze_evidence(layout_path, f"{identifier}/layout.json"), "acceptanceRecords": [freeze_evidence(path, f"{identifier}/acceptance-{number}{path.suffix}") for number, path in enumerate(records)]}
        routes = evidence.parent / "independent-routes.json"
        if routes.exists():
            provenance["priorIndependentRouteEvidence"] = freeze_evidence(routes, f"{identifier}/independent-routes.json")
        accepted.append({"id": identifier, "biome": "s8", "resourceId": "biomedungeon:" + identifier, "sha256": actual_hash, "size": vector(structure.size), "ports": ports, "role": SAFE_POOL[item["id"]], "accepted": True, "independentAcceptance": provenance})
        source_records.append(provenance)
    if set(template["independentAcceptance"]["sourceRoomId"] for template in accepted) != set(SAFE_POOL):
        save(ROOT / "source-audit.json", audits)
        save(ROOT / "rejections.json", rejected)
        raise ValueError("minimal pool failed real source checks; see source-audit.json")
    manifest = {"schemaVersion": 1, "templates": accepted}
    save(ROOT / "manifest.json", manifest)
    save(ROOT / "data/biomedungeon/manifest.json", manifest)
    save(ROOT / "source-audit.json", audits)
    save(ROOT / "rejections.json", {"entries": rejected, "globalExclusions": ["all second-round 11-20 candidates: no independent release", "all combined assembly NBTs: not individual rooms"], "exclusionIsNotRevocationOfPriorArchitecturalAcceptance": True})
    save(ROOT / "provenance.json", {"index": freeze_evidence(INDEX, "FINAL-80-SOURCE-INDEX.json"), "delivery": freeze_evidence(DELIVERY, "delivery-index.json"), "nbtioSource": r"C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\output\dungeon-expansion-20261101\tools\nbtio.py", "nbtioSha256": digest(ROOT / "nbtio.py"), "templates": source_records})
    print("FROZEN", len(accepted), "unchanged independently indexed source rooms; rejected/deferred", len(rejected))
    for template in accepted:
        print(template["id"], template["sha256"], template["ports"])


if __name__ == "__main__":
    build()
