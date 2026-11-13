import json
from collections import Counter
from pathlib import Path

from nbtio import Structure

BASE = Path(r"D:\ft工程文件")
INDEX = BASE / "FT地牢_独立复核_20261104_01a1a685/FINAL-80-SOURCE-INDEX.json"
DELIVERY = BASE / "FT地牢_八组合并总复核_20261106/delivery-index.json"


def main():
    index = json.loads(INDEX.read_text(encoding="utf-8"))
    delivery = json.loads(DELIVERY.read_text(encoding="utf-8"))
    layouts = {group["group"]: json.loads((Path(group["directory"]) / "layout.json").read_text(encoding="utf-8")) for group in delivery["groups"]}
    reports = []
    for item in index["items"]:
        if item["image"] >= 10:
            continue
        structure = Structure.read(item["candidate"])
        module = next(module for module in layouts[item["group"]]["modules"] if module["image"] == item["image"])
        counts = Counter(block.name for block in structure.blocks.values())
        ports = []
        for port in module["ports"]:
            if "feet" not in port or "clearHeight" not in port:
                ports.append({**port, "actual": {"UNKNOWN_METADATA": 1}, "side": port.get("name", "unknown")})
                continue
            feet = port["feet"]
            positions = [(feet[0] + (offset if port["side"] in ("north", "south") else 0), feet[1] + rise, feet[2] + (offset if port["side"] in ("east", "west") else 0)) for offset in range(-(port["width"] // 2), (port["width"] - 1) // 2 + 1) for rise in range(port["clearHeight"])]
            ports.append({**port, "actual": dict(Counter(structure.get(position).name for position in positions))})
        reports.append({"id": item["id"], "size": structure.size, "entities": len(structure.entities), "fluids_and_heat": {name: count for name, count in counts.items() if name in ("minecraft:water", "minecraft:lava", "minecraft:fire", "minecraft:soul_fire")}, "ports": ports})
    Path(__file__).with_name("source-inspection.json").write_text(json.dumps(reports, ensure_ascii=False, indent=2), encoding="utf-8")
    for report in reports:
        print(report["id"], report["fluids_and_heat"], [(port["side"], port["actual"]) for port in report["ports"]])


if __name__ == "__main__":
    main()
