import hashlib
import json
import math
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parent.parent
RUNTIME = PROJECT / "runtime/next-unified"
CATALOG = PROJECT / "catalog/acceptance-candidate-v4"
ORIGINAL = Path(r"D:\ft工程文件\FT地牢_独立复核_20261104_01a1a685\IndependentRouteAudit.java")
sys.dont_write_bytecode = True
sys.path.insert(0, str(PROJECT / "catalog"))
from nbtio import Structure

CHECKS = 0
LOCKS = {}


def check(condition, message):
    global CHECKS
    CHECKS += 1
    if not condition:
        raise AssertionError(message)


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def lock(path, expected=None):
    actual = sha(path)
    check(expected is None or actual == expected, "Input drift: " + str(path))
    LOCKS[str(path)] = actual
    return path


def load(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def save(name, data):
    path = ROOT / name
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def vector(value):
    return tuple(value[axis] for axis in ("x", "y", "z")) if isinstance(value, dict) else tuple(value)


def rotate(point, quarter):
    axis_x, axis_y, axis_z = point
    return ((axis_x, axis_y, axis_z), (-axis_z, axis_y, axis_x),
            (-axis_x, axis_y, -axis_z), (axis_z, axis_y, -axis_x))[quarter % 4]


def plus(left, right):
    return tuple(first + second for first, second in zip(left, right))


def minus(left, right):
    return tuple(first - second for first, second in zip(left, right))


def old_center(point):
    rounded = tuple(math.floor(coordinate * 2 + 0.5) / 2 for coordinate in point)
    return plus(rounded, (0.5, 0, 0.5))


def runtime_center(point, origin, quarter):
    return plus(plus(minus(point, (0, 96, 0)), origin), rotate((0.5, 0, 0.5), quarter))


def place_cell(cell, origin, quarter):
    correction = ((0, 0, 0), (-1, 0, 0), (-1, 0, -1), (0, 0, -1))[quarter]
    return plus(plus(origin, rotate(cell, quarter)), correction)


def close(left, right):
    return all(abs(first - second) < 1e-7 for first, second in zip(left, right))


def main():
    runtime_ready = load(lock(RUNTIME / "ready.json"))
    check(runtime_ready["jarSha256"] == "e6ca2da6399ce75ca3dca73236557d002d3f3584c93a0772602279855894dbdd", "Requested exact runtime artifact")
    for name, digest in runtime_ready["files"].items():
        lock(RUNTIME / name, digest)
    for component in (PROJECT / "core", PROJECT / "core/next-stairs", PROJECT / "core/next-planner-y"):
        ready = load(lock(component / "ready.json"))
        for record in ready["files"]:
            lock(component / record["path"], record["sha256"])
    catalog_ready = load(lock(CATALOG / "ready.json"))
    lock(CATALOG / "SHA256SUMS.json", catalog_ready["inventorySha256"])
    for record in load(CATALOG / "SHA256SUMS.json")["files"]:
        lock(CATALOG / record["path"], record["sha256"])
    lock(ORIGINAL)
    old_text = ORIGINAL.read_text(encoding="utf-8")
    check('Math.round(coordinates.get(0).getAsDouble() * 2) + 1' in old_text, "Old X half-grid plus one half")
    check('Math.round(coordinates.get(1).getAsDouble() * 2)' in old_text, "Old Y half-grid without center offset")
    check('Math.round(coordinates.get(2).getAsDouble() * 2) + 1' in old_text, "Old Z half-grid plus one half")
    check('double east = position.eastHalf / 2.0' in old_text, "Old node units are half-block")
    native_source = RUNTIME / "src/main/java/cn/ft/biomedungeon/runtime/NativeRouteAudit.java"
    placement_source = RUNTIME / "src/main/java/cn/ft/biomedungeon/runtime/Placement.java"
    original_lines = old_text.splitlines()
    native_lines = native_source.read_text(encoding="utf-8").splitlines()
    placement_lines = placement_source.read_text(encoding="utf-8").splitlines()
    for name, lines, ranges in (("IndependentRouteAudit-snippets.txt", original_lines, [(34, 61), (66, 76), (93, 96), (120, 131)]),
                                ("NativeRouteAudit-snippets.txt", native_lines, [(34, 65), (69, 85)]),
                                ("Placement-snippets.txt", placement_lines, [(36, 46), (196, 206)])):
        (ROOT / "evidence" / name).write_text("\n".join(str(index) + ": " + lines[index - 1] for lower, upper in ranges for index in range(lower, min(upper, len(lines)) + 1)) + "\n", encoding="utf-8")
    matrix_path = RUNTIME / "src/main/resources/data/ftbd_candidate/path-matrix.json"
    cases_path = RUNTIME / "src/main/resources/data/ftbd_candidate/routes.json"
    check(matrix_path.read_bytes() == (CATALOG / "path-check-matrix.json").read_bytes(), "Packaged matrix preserves catalog bytes")
    check(cases_path.read_bytes() == (CATALOG / "rotated-route-inputs.json").read_bytes(), "Packaged cases preserve source coordinates")
    jar = RUNTIME / runtime_ready["jar"]
    with zipfile.ZipFile(jar) as archive:
        for resource in ("data/ftbd_candidate/path-matrix.json", "data/ftbd_candidate/routes.json"):
            check(archive.read(resource) == (RUNTIME / "src/main/resources" / resource).read_bytes(), "Exact JAR resource binding")
        classes = [{"entry": entry, "sha256": hashlib.sha256(archive.read(entry)).hexdigest()} for entry in (
            "cn/ft/biomedungeon/runtime/NativeRouteAudit.class", "cn/ft/biomedungeon/runtime/Placement.class", "cn/ft/biomedungeon/core/DungeonGeometry.class")]
    save("evidence/jar-bindings.json", {"artifact": str(jar), "sha256": sha(jar), "classEntries": classes,
                                        "note": "javap disassembly retained separately; no assertion of recompilation equality"})
    cases = load(cases_path)["cases"]
    by_case = {case["caseId"]: case for case in cases}
    matrix = load(matrix_path)["checks"]
    check(len(cases) == 36 and len(matrix) == 2200, "Full frozen required input matrix")
    structures = {}
    for room in load(CATALOG / "release-gate-candidates.json")["templates"]:
        resource = CATALOG / room["resource"]["path"]
        lock(resource, room["sourceSha256"])
        structures[room["id"]] = Structure.read(resource)
    endpoint_checks = []
    rows = []
    fractional = 0
    off_grid = 0
    for index, entry in enumerate(matrix):
        case = by_case[entry["caseId"]]
        quarter = case["quarterTurns"]
        structure = structures[case["templateId"]]
        origin = (1000, 200 + index % 5, -1000)
        for end in ("start", "end"):
            fixture = vector(entry[end])
            local_raw = rotate(minus(fixture, (0, 96, 0)), -quarter)
            fractional += any(value % 1 for value in local_raw)
            off_grid += any(value * 2 != round(value * 2) for value in local_raw)
            native = runtime_center(fixture, origin, quarter)
            expected = plus(origin, rotate(old_center(local_raw), quarter))
            check(close(native, expected), "True old endpoint center reproduced")
            inverse_center = rotate(minus(native, origin), -quarter)
            check(close(inverse_center, old_center(local_raw)), "Inverse recovers exact old center")
            recovered_raw = minus(inverse_center, (0.5, 0, 0.5))
            check(close(recovered_raw, local_raw), "Inverse does not move original route goal")
            cell = (math.floor(local_raw[0]), math.floor(local_raw[1]), math.floor(local_raw[2]))
            world_cell = place_cell(cell, origin, quarter)
            check(close(native, plus(world_cell, (0.5, 0, 0.5))), "Actual blockPosition cell center identity for integer source targets")
            corners = [plus(origin, rotate((axis_x, axis_y, axis_z), quarter))
                       for axis_x in (0, structure.size[0]) for axis_y in (0, structure.size[1]) for axis_z in (0, structure.size[2])]
            low = tuple(min(point[axis] for point in corners) for axis in range(3))
            high = tuple(max(point[axis] for point in corners) for axis in range(3))
            check(low[0] + 0.299 <= native[0] <= high[0] - 0.299 and low[2] + 0.299 <= native[2] <= high[2] - 0.299, "Full endpoint horizontal body fits rotated outer bounds")
            wrong_double_compensation = plus(native, ((0, 0, 0), (-1, 0, 0), (-1, 0, -1), (0, 0, -1))[quarter])
            check(quarter == 0 or not close(wrong_double_compensation, expected), "Do not add block origin compensation twice")
            rows.append("\t".join(map(str, [quarter, *origin, *local_raw, *fixture, *native])))
            endpoint_checks.append({"matrixIndex": index, "caseId": case["caseId"], "side": end, "sourceRaw": local_raw,
                                    "syntheticTestOrigin": origin, "fixture": fixture, "runtimeCenter": native, "oldAuditCenter": expected,
                                    "inverseOriginalRaw": recovered_raw, "placedCell": world_cell})
    save("evidence/all-4400-endpoints.json", endpoint_checks)
    (ROOT / "evidence/real-endpoints.tsv").write_text("\n".join(rows) + "\n", encoding="utf-8")
    actual = []
    stair_paths = []
    runtime_result = load(RUNTIME / "result.json")
    for report in runtime_result["nativeRouteReports"]:
        payload = load(RUNTIME / report["path"])
        for entry in payload["checks"]:
            quarter = int(entry["caseId"].rsplit("_r", 1)[1])
            origin = vector(entry["actualOrigin"])
            for side, key in (("start", "nativeStart"), ("end", "nativeEnd")):
                expected = runtime_center(vector(entry["sourceFrozenCheck"][side]), origin, quarter)
                check(close(vector(entry[key]), expected), "Actual frozen route report agrees with actual placement origin and center transform")
                inverse = minus(rotate(minus(vector(entry[key]), origin), -quarter), (0.5, 0, 0.5))
                original_raw = rotate(minus(vector(entry["sourceFrozenCheck"][side]), (0, 96, 0)), -quarter)
                check(close(inverse, original_raw), "Actual report native target inverts to unchanged source target")
            actual.append({"caseId": entry["caseId"], "from": entry["from"], "to": entry["to"], "actualOrigin": entry["actualOrigin"],
                           "nativeStart": entry["nativeStart"], "nativeEnd": entry["nativeEnd"], "status": entry["status"]})
            identifier = by_case[entry["caseId"]]["templateId"]
            if identifier in ("s8_01", "s8_07") and entry["from"] == "original-start" and entry["to"] == "original-000":
                source_structure = structures[identifier]
                vertices = []
                for point in entry["outbound"]:
                    local_center = rotate(minus(vector(point), origin), -quarter)
                    support_cell = (math.floor(local_center[0] + 1e-7), math.floor(local_center[1] - 1e-5), math.floor(local_center[2] + 1e-7))
                    state = source_structure.get(support_cell)
                    door_neighbors = []
                    for offset_x in (-1, 0, 1):
                        for offset_z in (-1, 0, 1):
                            cell = (math.floor(local_center[0] + 1e-7) + offset_x, math.floor(local_center[1] + 1e-7), math.floor(local_center[2] + 1e-7) + offset_z)
                            neighbor = source_structure.get(cell)
                            if neighbor.name.endswith("_door"):
                                door_neighbors.append({"localCell": cell, "name": neighbor.name, "properties": dict(neighbor.properties),
                                                       "placedCellMin": place_cell(cell, origin, quarter)})
                    vertices.append({"reportedNativeBodyCenter": point, "inverseSourceBodyCenter": local_center,
                                     "inverseRawIndexConvention": minus(local_center, (0.5, 0, 0.5)),
                                     "pointProbeBelowFeetCell": support_cell, "sourceProbeState": {"name": state.name, "properties": dict(state.properties)},
                                     "nearbyOriginalDoorStates": door_neighbors,
                                     "scope": "Actual reported path inverse + original state lookup; not recomputation of native support/body/headroom"})
                stair_paths.append({"caseId": entry["caseId"], "actualOrigin": entry["actualOrigin"], "vertices": vertices})
    save("evidence/actual-1100-reported-checks.json", actual)
    save("evidence/actual-stair-door-path-inverses.json", stair_paths)
    profiles = []
    for identifier, positions in (("s8_01", [(17, 1, 0), (17, 2, 1), (17, 3, 2), (17, 3, 4), (17, 6, 14)]),
                                  ("s8_07", [(12, 2, 0), (12, 3, 2), (12, 4, 3), (12, 4, 4), (12, 4, 7)])):
        structure = structures[identifier]
        for position in positions:
            head_center = old_center(position)
            cells = []
            for local_y in range(max(0, position[1] - 1), min(structure.size[1], position[1] + 3)):
                block = structure.get((position[0], local_y, position[2]))
                cells.append({"position": [position[0], local_y, position[2]], "name": block.name, "properties": dict(block.properties)})
            transformations = []
            for quarter in range(4):
                origin = (1000, 200, 1000)
                world = plus(origin, rotate(head_center, quarter))
                transformations.append({"q": quarter, "worldCenter": world, "inverseRaw": minus(rotate(minus(world, origin), -quarter), (0.5, 0, 0.5)),
                                        "sourceCellWorldMin": place_cell(position, origin, quarter)})
            is_original = any(tuple(target["sourcePosition"]) == position for target in by_case[identifier + "_r0"]["targets"]) or tuple(by_case[identifier + "_r0"]["sourceStart"]) == position
            profiles.append({"id": identifier, "sourceRaw": position, "isOriginalMatrixEndpoint": is_original,
                             "probeScope": "Original endpoint if flagged, otherwise explicit analytic stair/door cell probe; not an added/relocated route goal",
                             "oldBodyCenter": head_center, "sourceColumn": cells,
                             "allFourTransforms": transformations,
                             "nativeSupportOrHeadroom": "NOT_REEXECUTED; source states and coordinate covariance only, not collision certification"})
    save("evidence/stairs-door-and-platform-probes.json", profiles)
    fraction_contract = []
    for source in ((12.5, 4, 4.5), (12.25, 4, 4.25), (12.1, 4.1, 4.1)):
        for quarter in range(4):
            origin = (1000, 200, 1000)
            fixture = plus((0, 96, 0), rotate(source, quarter))
            runtime = runtime_center(fixture, origin, quarter)
            old = plus(origin, rotate(old_center(source), quarter))
            fraction_contract.append({"sourceSyntheticNotOriginal": source, "q": quarter, "runtime": runtime, "oldAudit": old,
                                      "agrees": close(runtime, old), "cause": "old Math.round(2*p)/2 vs runtime preserves source fractions"})
    save("evidence/fraction-domain-counterexamples.json", fraction_contract)
    save("input-lock.json", {"files": [{"path": path, "sha256": digest} for path, digest in sorted(LOCKS.items())]})
    save("result.json", {"component": "route-coordinate-review", "status": "INTEGER_AND_HALF_GRID_CENTER_CONVENTION_PROVED_NOT_WORLD_ACCEPTANCE",
                         "runtimeJarSha256": runtime_ready["jarSha256"], "pythonChecks": CHECKS,
                         "frozenMatrixChecks": len(matrix), "realEndpointOccurrences": len(endpoint_checks),
                         "selectedS8FractionalEndpointOccurrences": fractional, "selectedS8OffHalfGridEndpointOccurrences": off_grid,
                         "actualFrozenReportedRouteChecks": len(actual), "actualReportedEndpointCoordinatesChecked": len(actual) * 2,
                         "generalOffHalfGridCompatibility": "COUNTEREXAMPLES; do not claim old-rounding equivalence for arbitrary fractions",
                         "coordinateVerdict": "Offset matches old node center convention on all real selected endpoints; no proven half-block bug in current frozen matrix",
                         "requiredChecks": runtime_result["requiredV4Checks"], "nativeCompletedByRuntime": runtime_result["nativeRouteRoundTrips"],
                         "all36IndependentCasesCertified": runtime_result["all36IndependentCasesCertified"],
                         "nativeUIAndDoorInteractions": runtime_result["nativeUIAndDoorInteractions"],
                         "globalEntranceReturnRouteCertificate": runtime_result["globalEntranceReturnRouteCertificate"],
                         "fullDeclaredWidthStairClearance": runtime_result["fullDeclaredWidthStairClearance"],
                         "nativeWorldTestsByReview": "NOT_RUN", "productionPromotion": False, "completeGeneratorReady": False})
    print("PASS coordinate review: %d checks; 4400 real endpoint transforms/inverses + 2200 actual reported endpoints; no source targets moved" % CHECKS)
    print("SELECTED S8 fractional endpoints=%d off-half-grid=%d; general non-half-grid counterexamples are synthetic, not original failures" % (fractional, off_grid))
    print("LIMIT runtime native progress=1100/2200; all36=false; interactions/entrance-return/full-width not released")


if __name__ == "__main__":
    main()
