from copy import deepcopy


FACE_AXES = {"down": (1, -1), "up": (1, 1), "north": (2, -1), "south": (2, 1), "west": (0, -1), "east": (0, 1)}
EPSILON = 1e-8


def subtract_rectangle(rectangle, cover):
    left, bottom, right, top = rectangle
    cover_left = max(left, cover[0])
    cover_bottom = max(bottom, cover[1])
    cover_right = min(right, cover[2])
    cover_top = min(top, cover[3])
    if cover_left >= cover_right - EPSILON or cover_bottom >= cover_top - EPSILON:
        return [rectangle]
    candidates = [(left, bottom, cover_left, top), (cover_right, bottom, right, top),
                  (cover_left, bottom, cover_right, cover_bottom), (cover_left, cover_top, cover_right, top)]
    return [candidate for candidate in candidates if candidate[2] - candidate[0] > EPSILON and candidate[3] - candidate[1] > EPSILON]


def face_coverage(element, direction, elements):
    if element.get("rotation"):
        return False, []
    axis, sign = FACE_AXES[direction]
    plane = element["to" if sign > 0 else "from"][axis]
    first, second = [coordinate for coordinate in range(3) if coordinate != axis]
    uncovered = [(element["from"][first], element["from"][second], element["to"][first], element["to"][second])]
    blockers = []
    for candidate in elements:
        if candidate["name"] == element["name"] or candidate.get("rotation"):
            continue
        minimum, maximum = candidate["from"][axis], candidate["to"][axis]
        if sign > 0:
            blocks = minimum <= plane + EPSILON and maximum > plane + EPSILON
        else:
            blocks = minimum < plane - EPSILON and maximum >= plane - EPSILON
        if not blocks:
            continue
        cover = (candidate["from"][first], candidate["from"][second], candidate["to"][first], candidate["to"][second])
        remaining = [piece for rectangle in uncovered for piece in subtract_rectangle(rectangle, cover)]
        if remaining != uncovered:
            blockers.append(candidate["name"])
        uncovered = remaining
        if not uncovered:
            return True, blockers
    return False, blockers


def optimize_elements(elements):
    optimized = deepcopy(elements)
    removed = []
    for element in optimized:
        for direction in list(element["faces"]):
            covered, blockers = face_coverage(element, direction, elements)
            if covered:
                del element["faces"][direction]
                removed.append({"element": element["name"], "face": direction, "opaque_blockers": blockers})
        if not element["faces"]:
            original = next(candidate for candidate in elements if candidate["name"] == element["name"])
            retained = next(iter(original["faces"]))
            element["faces"][retained] = deepcopy(original["faces"][retained])
            removed = [entry for entry in removed if not (entry["element"] == element["name"] and entry["face"] == retained)]
    original_faces = sum(len(element["faces"]) for element in elements)
    remaining_faces = sum(len(element["faces"]) for element in optimized)
    return optimized, {"original_elements": len(elements), "final_elements": len(optimized),
                       "original_quads": original_faces, "final_quads": remaining_faces,
                       "removed_quads": original_faces - remaining_faces,
                       "reduction_percent": round(100 * (original_faces - remaining_faces) / original_faces, 2),
                       "rotated_faces_preserved": True, "geometry_and_uv_unchanged": True, "removed_faces": removed}
