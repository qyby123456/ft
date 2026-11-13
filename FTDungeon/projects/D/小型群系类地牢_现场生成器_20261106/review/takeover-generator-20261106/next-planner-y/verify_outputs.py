import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent
CHECKS = 0


def check(condition, message):
    global CHECKS
    CHECKS += 1
    if not condition:
        raise AssertionError(message)


def load(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def coords(vector):
    return tuple(vector[axis] for axis in ("x", "y", "z"))


def rotate(point, quarter):
    axis_x, axis_y, axis_z = point
    return ((axis_x, axis_y, axis_z), (-axis_z, axis_y, axis_x),
            (-axis_x, axis_y, -axis_z), (axis_z, axis_y, -axis_x))[quarter]


def plus(left, right):
    return tuple(first + second for first, second in zip(left, right))


def port_geometry(template, port, room):
    feet = coords(port["feet"])
    facing = port["facing"]
    along_x = facing in ("north", "south")
    surface = 0 if facing in ("north", "west") else template["size"]["x" if facing == "east" else "z"]
    transverse = feet[0 if along_x else 2] - (port["width"] - 1) // 2
    start = (transverse, feet[1], surface) if along_x else (surface, feet[1], transverse)
    end = (transverse + port["width"], feet[1], surface) if along_x else (surface, feet[1], transverse + port["width"])
    origin = coords(room["origin"])
    quarter = room["quarterTurns"]
    start = plus(origin, rotate(start, quarter))
    end = plus(origin, rotate(end, quarter))
    directions = ("north", "east", "south", "west")
    world_facing = directions[(directions.index(facing) + quarter) % 4]
    transverse_axis = 0 if world_facing in ("north", "south") else 2
    axial_axis = 2 if transverse_axis == 0 else 0
    low = min(start[transverse_axis], end[transverse_axis])
    surface = start[axial_axis]
    exterior = [0, origin[1] + feet[1], 0]
    exterior[transverse_axis] = low + (port["width"] - 1) // 2
    exterior[axial_axis] = surface - (1 if world_facing in ("north", "west") else 0)
    return world_facing, low, low + port["width"], surface, tuple(exterior)


def intersects(first, second):
    return all(first[0][axis] < second[1][axis] and second[0][axis] < first[1][axis] for axis in range(3))


def main():
    lock = load(ROOT / "inputs/source-lock.json")
    for record in lock["files"]:
        check(hashlib.sha256(Path(record["path"]).read_bytes()).hexdigest() == record["sha256"], "Frozen input changed: " + record["path"])
    templates = {room["id"]: room for room in lock["rooms"]}
    outcomes = load(ROOT / "results/real-nine-results.json")
    check(outcomes["successes"] == 128 and outcomes["failures"] == 0, "Unexpected seed outcomes")
    total_span = set()
    for result in outcomes["outcomes"]:
        plan = load(ROOT / ("results/plans/q%d-seed%d.json" % (result["rootQuarterTurns"], result["seed"])))
        check(len(plan["rooms"]) == 9 and len(plan["links"]) == 8, "Nine actual rooms and eight links")
        check({room["templateId"] for room in plan["rooms"]} == set(templates), "Unique coverage of original nine")
        check(plan["rooms"][0]["templateId"] == "s8_07" and plan["rooms"][-1]["templateId"] == "s8_09", "Source roles preserved")
        check(plan["rooms"][0]["quarterTurns"] == result["rootQuarterTurns"], "Real root rotation")
        nodes = {room["nodeId"]: room for room in plan["rooms"]}
        boxes = []
        for room in plan["rooms"]:
            size = coords(templates[room["templateId"]]["size"])
            corners = [plus(coords(room["origin"]), rotate((axis_x, axis_y, axis_z), room["quarterTurns"]))
                       for axis_x in (0, size[0]) for axis_y in (0, size[1]) for axis_z in (0, size[2])]
            expected = (tuple(min(corner[axis] for corner in corners) for axis in range(3)),
                        tuple(max(corner[axis] for corner in corners) for axis in range(3)))
            check(expected == (coords(room["bounds"]["min"]), coords(room["bounds"]["maxExclusive"])), "Independent actual bounds")
            boxes.append(expected)
        shell_boxes = []
        reached = {plan["rooms"][0]["nodeId"]}
        used = set()
        for link in plan["links"]:
            source = nodes[link["fromNode"]]
            target = nodes[link["toNode"]]
            source_template = templates[source["templateId"]]
            target_template = templates[target["templateId"]]
            source_port = next(port for port in source_template["ports"] if port["id"] == link["fromPort"])
            target_port = next(port for port in target_template["ports"] if port["id"] == link["toPort"])
            check(source_port["width"] == target_port["width"] == link["width"], "No3/5 width coercion")
            check(source_port["height"] == target_port["height"] == link["height"], "No height coercion")
            check(source_port["medium"] == target_port["medium"], "No dry/wet coercion")
            check(source["origin"]["y"] + source_port["feet"]["y"] == target["origin"]["y"] + target_port["feet"]["y"], "Exact recursive Y solve")
            start = port_geometry(source_template, source_port, source)
            end = port_geometry(target_template, target_port, target)
            check(start[1:3] == end[1:3], "Real full aperture span")
            directions = ("north", "east", "south", "west")
            check((directions.index(start[0]) - directions.index(end[0])) % 4 == 2, "Opposite normal")
            check(3 <= abs(start[3] - end[3]) <= 10, "True face gap")
            check([coords(point) for point in link["waypoints"]] == [start[4], end[4]], "Independent actual exterior feet")
            check(link["fromNode"] in reached and link["toNode"] not in reached, "Reachable tree")
            reached.add(link["toNode"])
            for key in ((link["fromNode"], link["fromPort"]), (link["toNode"], link["toPort"])):
                check(key not in used, "No reused port")
                used.add(key)
            first, last = start[4], end[4]
            along_x = first[0] != last[0]
            transverse_axis = 2 if along_x else 0
            axial_axis = 0 if along_x else 2
            lower = first[transverse_axis] - (link["width"] - 1) // 2 - 1
            low = [0, first[1] - 1, 0]
            high = [0, first[1] + link["height"] + 1, 0]
            low[transverse_axis] = lower
            high[transverse_axis] = lower + link["width"] + 2
            low[axial_axis] = min(first[axial_axis], last[axial_axis])
            high[axial_axis] = max(first[axial_axis], last[axial_axis]) + 1
            shell_boxes.append((tuple(low), tuple(high)))
        check(len(reached) == 9, "All nine geometrically returnable")
        check(shell_boxes == [(coords(shell["min"]), coords(shell["maxExclusive"])) for shell in plan["reservedShells"]], "Independent full floor/wall/head reservation reconstruction")
        boxes += shell_boxes
        for index, first in enumerate(boxes):
            check(-64 <= first[0][1] < first[1][1] <= 320, "Actual world height band")
            for second in boxes[index + 1:]:
                check(not intersects(first, second), "All3D rooms and shells pairwise disjoint")
        span = max(box[1][1] for box in boxes) - min(box[0][1] for box in boxes)
        check(span <= 64, "Whole graph height includes shells")
        total_span.add(span)
    output = {"result": "PASS", "checks": CHECKS, "plansIndependentlyChecked": 128,
              "graphHeights": sorted(total_span), "frozenInputsUnchanged": True,
              "method": "Separate Python equations; no invocation of Java geometry or planner", "nativeWorld": "NOT_RUN"}
    (ROOT / "results/independent-verification.json").write_text(json.dumps(output, indent=2) + "\n", encoding="utf-8")
    print("PASS independent Python verifier: %d checks across128 complete nine-room graphs; graph spans=%s" % (CHECKS, sorted(total_span)))


if __name__ == "__main__":
    main()
