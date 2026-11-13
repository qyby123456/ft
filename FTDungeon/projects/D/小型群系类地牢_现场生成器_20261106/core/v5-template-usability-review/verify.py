import hashlib
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
sys.dont_write_bytecode = True
sys.path.insert(0, str(ROOT.parent / "next-planner-y"))
from verify_outputs import coords, rotate, plus, port_geometry, intersects


def load(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def main():
    assertions = 0

    def check(condition, message):
        nonlocal assertions
        assertions += 1
        if not condition:
            raise AssertionError(message)

    lock = load(ROOT / "inputs/input-lock.json")
    for record in lock["frozenInventory"] + lock["sourceResources"]:
        check(hashlib.sha256(Path(record["path"]).read_bytes()).hexdigest() == record["sha256"], "Frozen input drift: " + record["path"])
    check(hashlib.sha256(Path(lock["manifest"]).read_bytes()).hexdigest() == lock["manifestSha256"], "Manifest remains exact")
    templates = {item["id"]: item for item in load(ROOT / "inputs/candidate-manifest.json")["templates"]}
    check(len(templates) == 9 and all(item["accepted"] is False for item in templates.values()), "Nine unaccepted experimental inputs")
    matrix = load(ROOT / "results/matrix.json")
    check(matrix["runtimeVersion"] == lock["runtimeVersion"], "Exact runtime VERSION")
    check(matrix["requests"] == matrix["passed"] == 129 and matrix["failed"] == 0, "Full requested matrix plus original seed/ordinal")
    seed_ordinals = {}
    max_cells = 0
    for row in matrix["rows"]:
        seed_ordinals.setdefault(row["worldSeed"], set()).add(row["ordinal"])
        plan = load(ROOT / ("results/plans/seed%d-ordinal%d.json" % (row["worldSeed"], row["ordinal"])))
        check(len(plan["rooms"]) == 9 and len(plan["links"]) == 8, "Eight originals and one derived room")
        check({room["templateId"] for room in plan["rooms"]} == set(templates), "Each actual ID once")
        check(plan["rooms"][0]["templateId"] == "s8_07_widearch_side_doublegate_v5" and plan["rooms"][-1]["templateId"] == "s8_09", "Source roles")
        check(plan["rooms"][0]["quarterTurns"] == row["ordinal"] % 4, "Ordinal root rotation")
        nodes = {room["nodeId"]: room for room in plan["rooms"]}
        owned = []
        runtime_boxes = []
        for room in plan["rooms"]:
            size = coords(templates[room["templateId"]]["size"])
            corners = [plus(coords(room["origin"]), rotate((axis_x, axis_y, axis_z), room["quarterTurns"]))
                       for axis_x in (0, size[0]) for axis_y in (0, size[1]) for axis_z in (0, size[2])]
            box = (tuple(min(point[axis] for point in corners) for axis in range(3)), tuple(max(point[axis] for point in corners) for axis in range(3)))
            check(box == (coords(room["bounds"]["min"]), coords(room["bounds"]["maxExclusive"])), "True shifted eightcorner bounds")
            owned.append(box)
            runtime_boxes.append(box)
        reached = {plan["rooms"][0]["nodeId"]}
        used = set()
        for link in plan["links"]:
            source, target = nodes[link["fromNode"]], nodes[link["toNode"]]
            source_template, target_template = templates[source["templateId"]], templates[target["templateId"]]
            source_port = next(port for port in source_template["ports"] if port["id"] == link["fromPort"])
            target_port = next(port for port in target_template["ports"] if port["id"] == link["toPort"])
            check(source_port["width"] == target_port["width"] == link["width"], "width3/5 exact")
            check(source_port["height"] == target_port["height"] == link["height"] and source_port["medium"] == target_port["medium"], "height/medium exact")
            check(source["origin"]["y"] + source_port["feet"]["y"] == target["origin"]["y"] + target_port["feet"]["y"], "Recursive actual Y alignment")
            start, end = port_geometry(source_template, source_port, source), port_geometry(target_template, target_port, target)
            check(start[1:3] == end[1:3], "Actual aperture interval")
            directions = ("north", "east", "south", "west")
            check((directions.index(start[0]) - directions.index(end[0])) % 4 == 2, "Opposing faces")
            check([coords(point) for point in link["waypoints"]] == [start[4], end[4]], "Exterior seam feet")
            check(link["fromNode"] in reached and link["toNode"] not in reached, "Global connected tree")
            reached.add(link["toNode"])
            for key in ((link["fromNode"], link["fromPort"]), (link["toNode"], link["toPort"])):
                check(key not in used, "No reused portal")
                used.add(key)
            first, last = start[4], end[4]
            transverse, axial = (2, 0) if first[0] != last[0] else (0, 2)
            low = [0, first[1] - 1, 0]
            high = [0, first[1] + link["height"] + 1, 0]
            low[transverse] = first[transverse] - (link["width"] - 1) // 2 - 1
            high[transverse] = low[transverse] + link["width"] + 2
            low[axial] = min(first[axial], last[axial])
            high[axial] = max(first[axial], last[axial]) + 1
            owned.append((tuple(low), tuple(high)))
            for point in link["waypoints"]:
                axis_x, axis_y, axis_z = coords(point)
                runtime_boxes.append(((axis_x-8, axis_y-2, axis_z-8), (axis_x+9, axis_y+10, axis_z+9)))
        check(len(reached) == 9, "All nine geometrically returnable")
        for index, first in enumerate(owned):
            for second in owned[index+1:]:
                check(not intersects(first, second), "Fullshell3D collision separation")
        low = tuple(min(box[0][axis] for box in runtime_boxes) for axis in range(3))
        high = tuple(max(box[1][axis] for box in runtime_boxes) for axis in range(3))
        spans = [high[axis] - low[axis] for axis in range(3)]
        cells = (spans[0]+6)*(spans[1]+6)*(spans[2]+6)
        check(spans[0] <= 256 and spans[1] <= 96 and spans[2] <= 256 and cells <= 1500000, "Exact unchanged runtime envelope/operation budget")
        check(low[1]-3 >= 164 and high[1]+3 <= 316, "Enclosure worldheight")
        max_cells = max(cells, max_cells)
    check(set(seed_ordinals) == set(range(32)) | {267611119455246637}, "Exact seeds, no replacement")
    check(all(seed_ordinals[seed] == set(range(4)) for seed in range(32)), "32 worldSeeds x4 ordinals/root rotations")
    check(seed_ordinals[267611119455246637] == {3}, "Original failed seed and ordinal")
    summary = {"checks": assertions, "verifiedPlans": 129, "worldSeedsInMainMatrix": 32, "rootRotations": 4, "maxCells": max_cells,
               "runtimeVersion": lock["runtimeVersion"], "originalSeedOrdinalInNewV5Context": True,
               "worldAcceptance": "NOT_RUN", "productionPromotion": False}
    (ROOT / "results/independent-verification.json").write_text(json.dumps(summary, indent=2)+"\n", encoding="utf-8")
    print("PASS Python independent verification: " + json.dumps(summary))


if __name__ == "__main__":
    main()

