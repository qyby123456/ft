import hashlib
import json
import math
import sys
from pathlib import Path

from audit_s8_submission import decode, load


def main():
    root = Path(sys.argv[1]).resolve()
    manifest = load(root / "ready-for-review.json")
    candidate = root / "merged/s1-mossy-functional-11-20.nbt"
    assert hashlib.sha256(candidate.read_bytes()).hexdigest() == manifest["merged"]["sha256"]
    data, blocks = decode(candidate)
    empty = {"state": {"Name": "minecraft:air"}, "nbt": None}
    surfaces = load(root / "merged/corridor-surfaces.json")
    assert surfaces["source_sha256"] == manifest["merged"]["sha256"]
    cells = surfaces["surfaces"]
    assert len(cells) == 6407
    levels = {(cell["east"], cell["south"]): cell["feet"] for cell in cells}
    assert len(levels) == len(cells)
    targets = []
    for cell in cells:
        east, south, feet = cell["east"], cell["south"], cell["feet"]
        for height in range(math.ceil(feet), math.floor(feet) + 4):
            assert blocks.get((east, height, south), empty)["state"]["Name"] == "minecraft:air"
        floor = blocks[(east, math.floor(feet) if feet % 1 else int(feet) - 1, south)]["state"]
        assert floor["Name"] in {"minecraft:mossy_stone_bricks", "minecraft:stone_brick_slab"}
        if feet % 1:
            assert floor["Name"] == "minecraft:stone_brick_slab" and floor["Properties"]["type"] == "bottom"
        for step in ((1, 0), (0, 1)):
            neighbor = levels.get((east + step[0], south + step[1]))
            assert neighbor is None or abs(neighbor - feet) <= 0.5
        targets.append({"label": f"corridor-{east}-{south}", "position": [east, feet, south]})
    ports = []
    rooms = {room["id"]: room for room in manifest["rooms"]}
    for room in load(root / "merged/integrity.json")["rooms"]:
        folder = Path(rooms[room["id"]]["candidate"]).parent.name
        design = load(root / folder / "design.json")
        for port in design["ports"]:
            feet = [point + offset for point, offset in zip(port["feet"], room["offset"])]
            count = 0
            for across in range(-(port["width"] // 2), port["width"] // 2 + 1):
                for inward in range(-2, 3):
                    for rise in range(port["clearHeight"]):
                        point = (feet[0] + inward, feet[1] + rise, feet[2] + across) if port["side"] in {"west", "east"} else (feet[0] + across, feet[1] + rise, feet[2] + inward)
                        assert blocks.get(point, empty)["state"]["Name"] == "minecraft:air", (room["id"], port["side"], point)
                        count += 1
            ports.append({"room": room["id"], "side": port["side"], "air_cells": count})
    scenery_path = root / "scenery/s1-19-forest-depth.nbt"
    scenery, vegetation = decode(scenery_path)
    placement = load(root / "merged/scenery-placement.json")
    assert hashlib.sha256(scenery_path.read_bytes()).hexdigest() == placement["sha256"]
    retained, overridden = [], []
    for point, value in vegetation.items():
        if value["state"]["Name"] == "minecraft:air":
            continue
        world = tuple(point[axis] + placement["offset"][axis] for axis in range(3))
        (retained if blocks.get(world) == value else overridden).append(world)
    assert len(retained) >= placement["nonair_written"]
    assert scenery["size"][2] == placement["depth_blocks"] == 64
    route = load(root / "independent-qa/route-input.json")[-1]
    destination = root / "independent-corridor-qa"
    destination.mkdir()
    (destination / "route-input.json").write_text(json.dumps([dict(route, targets=targets)], ensure_ascii=False, indent=2), encoding="utf-8")
    (destination / "vanilla-block-tags.json").write_bytes((root / "independent-qa/vanilla-block-tags.json").read_bytes())
    report = {"stage": "INDEPENDENT_S1_FULL_CORRIDOR_ENVELOPE_AND_PACKAGED_SCENERY_CHECK", "source_sha256": manifest["merged"]["sha256"], "corridor_surface_cells": len(cells), "all_declared_air_height_floor_and_neighbor_steps_checked": True, "full_port_envelopes": ports, "scenery_source_sha256": placement["sha256"], "scenery_size": scenery["size"], "scenery_nonair_exactly_retained": len(retained), "scenery_nonair_overridden": len(overridden), "scenery_depth_extent_retained": max(point[2] for point in retained) - min(point[2] for point in retained) + 1, "limitations": ["Corridor cells derive from declared complete surface list; topology independently route-tested separately", "Scenery overlapping higher-priority module and corridor payload may be omitted; exact retained count recorded", "No dynamic world or player movement certification"], "accepted": False}
    (root / "independent-qa/corridor-scenery.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False))


if __name__ == "__main__":
    main()
