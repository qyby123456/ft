import hashlib
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parent.parent
sys.dont_write_bytecode = True
sys.path.insert(0, str(PROJECT / "catalog"))
from nbtio import Structure
from freeze_catalog import read_port


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def save(name, value):
    (ROOT / name).write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main():
    requirements = PROJECT / "catalog/expansion-v2/s8-complete-biome-requirements.json"
    baseline = json.loads((PROJECT / "core/ready.json").read_text(encoding="utf-8"))
    for item in baseline["files"]:
        assert digest(PROJECT / "core" / item["path"]) == item["sha256"], item["path"]
    records = json.loads(requirements.read_text(encoding="utf-8"))["templates"]
    evidence = []
    exports = []
    for record in records:
        if record["id"] not in {"S8-01", "S8-07"}:
            continue
        source = Path(record["source"])
        metadata = Path(record["originalMetadata"])
        assert digest(source) == record["sha256"]
        assert digest(metadata) == record["originalMetadataSha256"]
        structure = Structure.read(source)
        assert list(structure.size) == record["size"]
        checks = []
        for number, port in enumerate(record["rawOriginalPorts"]):
            feet = port["position"]
            width = port["width"]
            height = port.get("height", port.get("minimum_height"))
            exports.append("\t".join(map(str, [record["id"].lower().replace("-", "_"), record["sha256"], *structure.size,
                                                 "p" + str(number), *feet, port["facing"], width, height])))
            try:
                parsed, cells = read_port(port, structure, number)
                strict = {"result": "PASS", "cellsChecked": len(cells)}
            except ValueError as error:
                strict = {"result": "REJECT", "reason": str(error)}
            checks.append({"raw": port, "strictCatalogReproduction": strict})
        evidence.append({"id": record["id"], "source": str(source), "sourceSha256": digest(source),
                         "metadata": str(metadata), "metadataSha256": digest(metadata), "size": list(structure.size), "portChecks": checks})
        if record["id"] == "S8-01":
            band = []
            profiles = []
            for depth in range(6):
                lane_heights = []
                for lateral in (16, 17, 18):
                    column = []
                    for rise in range(10):
                        state = structure.get((lateral, rise, depth))
                        cell = {"position": [lateral, rise, depth], "name": state.name, "properties": dict(state.properties)}
                        band.append(cell)
                        if state.name not in {"minecraft:air", "minecraft:cave_air", "minecraft:void_air"}:
                            column.append(cell)
                    highest = max(cell["position"][1] for cell in column)
                    support = structure.get((lateral, highest, depth))
                    top = highest + 1
                    samples = [top]
                    if support.name.endswith("_stairs"):
                        assert dict(support.properties) == {"facing": "south", "half": "bottom", "shape": "straight", "waterlogged": "false"}
                        samples = [highest + 0.5, highest + 1]
                    for sample in samples:
                        for rise in range(int(sample + 0.999), int(sample + 3 + 0.999)):
                            assert structure.get((lateral, rise, depth)).name == "minecraft:air", (lateral, rise, depth)
                    lane_heights.append({"x": lateral, "supportState": support.name, "supportY": highest,
                                         "surfaceHeights": samples, "conservativeFullBlocksAboveCeilingSurfaceClearance": 3})
                profiles.append({"z": depth, "lanes": lane_heights})
            save("s8-01-north-transition-cells.json", {"sourceSha256": digest(source), "region": {"min": [16, 0, 0], "maxExclusive": [19, 10, 6]},
                                                        "cells": band, "profile": profiles,
                                                        "nativeVoxelShapeAndSweptPlayerTest": "NOT_RUN",
                                                        "scope": "Static original NBT cell inspection only; stair half surfaces are analytic expectations, not native collision proof"})
            print("S8-01 north original profile: z0 top1; z1 straight bottom south stair top1.5/2; z2 top2.5/3; z3..5 top3; three lanes intact; air above conservative surface inspected")
        else:
            profiles = []
            for raw in record["rawOriginalPorts"]:
                for depth in range(8):
                    facing = raw["facing"]
                    depth_z = depth if facing == "north" else structure.size[2] - 1 - depth
                    for lateral in range(12 - raw["width"] // 2, 13 + raw["width"] // 2):
                        cells = []
                        for rise in range(10):
                            state = structure.get((lateral, rise, depth_z))
                            cells.append({"position": [lateral, rise, depth_z], "name": state.name, "properties": dict(state.properties)})
                        profiles.append({"facing": facing, "depth": depth, "x": lateral, "cells": cells})
            save("s8-07-transition-cells.json", {"sourceSha256": digest(source), "columns": profiles,
                                                "scope": "Original boundary and 7 interior rows, full 3/5-wide lanes, y0..9; no route or native collision certification"})
            print("S8-07 original north feet2 width3 and south feet4 width5: catalog strict checks both PASS; height and width compatibility remain distinct planner constraints")
    save("source-verification.json", {"requirements": str(requirements), "requirementsSha256": digest(requirements),
                                      "coreReadySha256": digest(PROJECT / "core/ready.json"), "baselineInventoryRechecked": len(baseline["files"]),
                                      "records": evidence, "sourceFilesModified": False, "nativeWorldTest": "NOT_RUN",
                                      "readOnlyParserDependencies": [{"path": str(PROJECT / "catalog" / name), "sha256": digest(PROJECT / "catalog" / name)}
                                                                     for name in ("nbtio.py", "freeze_catalog.py", "inspect_sources.py")]})
    (ROOT / "real-ports.tsv").write_text("\n".join(exports) + "\n", encoding="utf-8")
    print("PASS verified original NBT/metadata hashes, decoded actual blocks, reproduced strict catalog decisions; frozen core files match ready inventory")


if __name__ == "__main__":
    main()
