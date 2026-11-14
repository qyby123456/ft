import argparse
import collections
import copy
import hashlib
import json
import math
import zipfile
from pathlib import Path


GEOMETRY_KEYS = ("points", "p", "vertices")
FACE_KEYS = ("faces", "lite", "liteFaces", "mainFaces", "beamFaces")
EXCLUDED = {"models", "blockstates", "lang", "animations", "food_socket_attributes"}


def subtract(first, second):
    return tuple(left - right for left, right in zip(first, second))


def cross(first, second):
    return (first[1] * second[2] - first[2] * second[1],
            first[2] * second[0] - first[0] * second[2],
            first[0] * second[1] - first[1] * second[0])


def dot(first, second):
    return sum(left * right for left, right in zip(first, second))


def freeze(value):
    return json.dumps(value, sort_keys=True, separators=(",", ":"))


def coordinates(face):
    if isinstance(face, list):
        return face if face and isinstance(face[0], list) else None
    if not isinstance(face, dict):
        return None
    for key in GEOMETRY_KEYS:
        points = face.get(key)
        if not isinstance(points, list) or not points:
            continue
        if isinstance(points[0], list):
            return points
        if key == "vertices" and len(points) == 12:
            return [points[offset:offset + 3] for offset in range(0, 12, 3)]
    return None


def polygon(face):
    points = coordinates(face)
    if points is None or len(points) != 4 or any(len(point) != 3 for point in points):
        return None
    vertices = []
    uv = face.get("uv") if isinstance(face, dict) else None
    colors = face.get("vertexColors") if isinstance(face, dict) else None
    if uv and not isinstance(uv[0], list):
        uv = [uv[offset:offset + 2] for offset in range(0, len(uv), 2)]
    for index, point in enumerate(points):
        attributes = tuple(uv[index]) if uv else ()
        if colors:
            attributes += tuple((colors[index] >> shift) & 255 for shift in (16, 8, 0))
        vertex = (tuple(point), attributes)
        if not vertices or vertices[-1][0] != vertex[0]:
            vertices.append(vertex)
        elif vertices[-1][1] != vertex[1]:
            return None
    if len(vertices) > 1 and vertices[-1][0] == vertices[0][0]:
        if vertices[-1][1] != vertices[0][1]:
            return None
        vertices.pop()
    return vertices


def normal(vertices):
    if len(vertices) < 3:
        return None
    for offset in range(1, len(vertices) - 1):
        result = cross(subtract(vertices[offset][0], vertices[0][0]),
                       subtract(vertices[offset + 1][0], vertices[0][0]))
        length = math.sqrt(dot(result, result))
        if length > 1e-14:
            return tuple(value / length for value in result)
    return None


def affine_attributes(vertices, candidates):
    origin = vertices[0]
    for offset in range(1, len(vertices) - 1):
        first = subtract(vertices[offset][0], origin[0])
        second = subtract(vertices[offset + 1][0], origin[0])
        first_squared, second_squared, product = dot(first, first), dot(second, second), dot(first, second)
        determinant = first_squared * second_squared - product * product
        if determinant <= 1e-24:
            continue
        for point, attributes in candidates:
            displacement = subtract(point, origin[0])
            first_product, second_product = dot(displacement, first), dot(displacement, second)
            first_weight = (first_product * second_squared - second_product * product) / determinant
            second_weight = (second_product * first_squared - first_product * product) / determinant
            for channel, value in enumerate(attributes):
                predicted = (origin[1][channel]
                             + first_weight * (vertices[offset][1][channel] - origin[1][channel])
                             + second_weight * (vertices[offset + 1][1][channel] - origin[1][channel]))
                if abs(predicted - value) > 1e-9:
                    return False
        return True
    return False


def merge(first, second):
    first_normal, second_normal = normal(first), normal(second)
    if first_normal is None or second_normal is None or dot(first_normal, second_normal) < 1 - 1e-12:
        return None
    if any(abs(dot(subtract(vertex[0], first[0][0]), first_normal)) > 1e-9 for vertex in first + second):
        return None
    edges = {}
    for vertices in (first, second):
        for index, start in enumerate(vertices):
            end = vertices[(index + 1) % len(vertices)]
            reverse = (end, start)
            if reverse in edges:
                del edges[reverse]
            else:
                edges[(start, end)] = True
    if len(edges) != len(first) + len(second) - 2:
        return None
    following = {}
    for start, end in edges:
        if start in following:
            return None
        following[start] = end
    result = [next(iter(following))]
    while len(result) < len(edges):
        next_vertex = following.get(result[-1])
        if next_vertex is None or next_vertex in result:
            return None
        result.append(next_vertex)
    if following.get(result[-1]) != result[0]:
        return None
    changed = True
    while changed and len(result) > 3:
        changed = False
        for index, middle in enumerate(result):
            previous, after = result[index - 1], result[(index + 1) % len(result)]
            before_vector, after_vector = subtract(middle[0], previous[0]), subtract(after[0], middle[0])
            perpendicular = cross(before_vector, after_vector)
            if dot(before_vector, after_vector) > 0 and dot(perpendicular, perpendicular) <= 1e-24:
                result.pop(index)
                changed = True
                break
    if len(result) not in (3, 4):
        return None
    if any(dot(cross(subtract(result[(index + 1) % len(result)][0], vertex[0]),
                     subtract(result[(index + 2) % len(result)][0], result[(index + 1) % len(result)][0])),
               first_normal) <= 1e-15 for index, vertex in enumerate(result)):
        return None
    if result[0][1] and not affine_attributes(result, first + second):
        return None
    return result


def face_signature(face, allow_untextured):
    if isinstance(face, list):
        return "raw" if allow_untextured else None
    if "uv" not in face and "vertexColors" not in face and not allow_untextured:
        return None
    return freeze({key: value for key, value in face.items() if key not in (*GEOMETRY_KEYS, "uv", "vertexColors")})


def replace_polygon(face, vertices):
    padded = vertices + [vertices[-1]] * (4 - len(vertices))
    if isinstance(face, list):
        return [list(vertex[0]) for vertex in padded]
    result = copy.deepcopy(face)
    key = next(key for key in GEOMETRY_KEYS if key in face)
    points = [list(vertex[0]) for vertex in padded]
    result[key] = [coordinate for point in points for coordinate in point] if isinstance(face[key][0], (int, float)) else points
    if "uv" in face:
        uv = [list(vertex[1][:2]) for vertex in padded]
        result["uv"] = [coordinate for point in uv for coordinate in point] if isinstance(face["uv"][0], (int, float)) else uv
    if "vertexColors" in face:
        result["vertexColors"] = [int(vertex[1][-3]) << 16 | int(vertex[1][-2]) << 8 | int(vertex[1][-1]) for vertex in padded]
    return result


def optimize_faces(faces, allow_untextured=False, apply=False):
    active = {}
    edge_faces = collections.defaultdict(set)
    signatures = {}
    polygons = {}
    removed = merged = 0
    for index, face in enumerate(faces):
        vertices = polygon(face)
        if vertices is not None and normal(vertices) is None:
            removed += 1
            continue
        active[index] = face
        if vertices is None:
            continue
        signature = face_signature(face, allow_untextured)
        if signature is not None:
            polygons[index], signatures[index] = vertices, signature
            for edge_index, start in enumerate(vertices):
                end = vertices[(edge_index + 1) % len(vertices)]
                edge_faces[(signature, start, end)].add(index)
    pending = collections.deque(polygons)
    while pending:
        index = pending.popleft()
        if index not in active or index not in polygons:
            continue
        vertices, signature = polygons[index], signatures[index]
        partner = combined = None
        for edge_index, start in enumerate(vertices):
            end = vertices[(edge_index + 1) % len(vertices)]
            for candidate in sorted(edge_faces.get((signature, end, start), ())):
                if candidate == index or candidate not in active:
                    continue
                combined = merge(vertices, polygons[candidate])
                if combined:
                    partner = candidate
                    break
            if combined:
                break
        if partner is None:
            continue
        for old_index in (index, partner):
            old_vertices = polygons[old_index]
            for edge_index, start in enumerate(old_vertices):
                end = old_vertices[(edge_index + 1) % len(old_vertices)]
                edge_faces[(signature, start, end)].discard(old_index)
        del active[partner]
        polygons[index] = combined
        active[index] = replace_polygon(active[index], combined)
        for edge_index, start in enumerate(combined):
            end = combined[(edge_index + 1) % len(combined)]
            edge_faces[(signature, start, end)].add(index)
        pending.append(index)
        merged += 1
    result = [active[index] for index in sorted(active)]
    return (result if apply else faces), {"before": len(faces), "after": len(result), "degenerate": removed, "merged": merged}


def flat_faces(group):
    points = group["vertices"]
    if not points or not isinstance(points[0], (int, float)) or len(points) % 12:
        return None
    sizes = {"vertices": 12, "colors": 4, "alphas": 1, "materials": 1, "bones": 1, "pivots": 3, "uvs": 8}
    count = len(points) // 12
    if any(isinstance(value, list) and key not in sizes for key, value in group.items() if key != "origin"):
        return None
    if any(len(group[key]) != count * size for key, size in sizes.items() if key in group):
        raise ValueError("Unaligned flat mesh arrays")
    result = []
    for index in range(count):
        face = {"vertices": points[index * 12:(index + 1) * 12]}
        for key, size in sizes.items():
            if key == "vertices" or key not in group:
                continue
            values = group[key][index * size:(index + 1) * size]
            face[{"colors": "vertexColors", "uvs": "uv"}.get(key, key)] = values if size > 1 else values[0]
        result.append(face)
    return result


def store_flat(group, faces):
    sizes = {"vertices": "vertices", "colors": "vertexColors", "alphas": "alphas", "materials": "materials", "bones": "bones", "pivots": "pivots", "uvs": "uv"}
    for key, field in sizes.items():
        if key in group:
            group[key] = [value for face in faces for value in (face[field] if isinstance(face[field], list) else [face[field]])]


def process_resource(resource, apply=False):
    rows = []

    def visit(node, path):
        if isinstance(node, dict):
            if isinstance(node.get("vertices"), list) and "colors" in node:
                faces = flat_faces(node)
                if faces is not None:
                    optimized, row = optimize_faces(faces, allow_untextured="uvs" not in node and "materials" not in node, apply=apply)
                    rows.append({"path": path, "type": "flat_quads", **row})
                    if apply and row["after"] != row["before"]:
                        store_flat(node, optimized)
                    return
            for key, value in list(node.items()):
                child_path = path + "." + key
                if key in FACE_KEYS and isinstance(value, list) and value and coordinates(value[0]) is not None:
                    optimized, row = optimize_faces(value, apply=apply)
                    rows.append({"path": child_path, "type": "faces", **row})
                    if apply:
                        node[key] = optimized
                elif key == "faces" and isinstance(value, list) and value and "vertices" in node:
                    rows.append({"path": child_path, "type": "indexed_faces", "before": len(value), "after": len(value), "degenerate": 0, "merged": 0})
                elif key == "triangles" and isinstance(value, list):
                    rows.append({"path": child_path, "type": "triangles", "before": len(value), "after": len(value), "degenerate": 0, "merged": 0})
                elif isinstance(value, (list, dict)):
                    visit(value, child_path)
        elif isinstance(node, list):
            for index, value in enumerate(node):
                visit(value, path + f"[{index}]")

    visit(resource, "root")
    return rows


def optimize_maque_density(resource, apply=False):
    rows = []
    for index, model in enumerate(resource["models"]):
        full = collections.defaultdict(list)
        lite = collections.defaultdict(list)
        for face in model["faces"]:
            full[(face["part"], face["b"], face["m"], face["a"])].append(face)
        for face in model["lite"]:
            lite[(face["part"], face["b"], face["m"], face["a"])].append(face)
        if full.keys() != lite.keys():
            raise ValueError("Low-detail mesh loses an animated part or material")
        focal = max(lite, key=lambda key: len(lite[key]))
        replacements = {}
        maximum_error = 0.0
        for key, faces in full.items():
            if key == focal or key[2] >= 4 or len(faces) < 64 or len(lite[key]) > len(faces) * 0.85:
                continue
            def bounds(surfaces):
                points = [point for face in surfaces for point in face["p"]]
                return [extreme(point[axis] for point in points) for extreme in (min, max) for axis in range(3)]
            error = max(abs(first - second) for first, second in zip(bounds(faces), bounds(lite[key])))
            if error <= 0.004:
                replacements[key] = lite[key]
                maximum_error = max(maximum_error, error)
        result = []
        inserted = set()
        for face in model["faces"]:
            key = (face["part"], face["b"], face["m"], face["a"])
            if key not in replacements:
                result.append(face)
            elif key not in inserted:
                result.extend(replacements[key])
                inserted.add(key)
        if len(result) <= len(model["lite"]):
            raise ValueError("Runtime requires a strictly smaller fallback LOD")
        row = {"path": f"root.models[{index}].faces", "model": model["id"],
               "before": len(model["faces"]), "after": len(result), "replaced_parts": len(replacements),
               "part_bounds_delta": maximum_error}
        rows.append(row)
        if apply:
            model["faces"] = result
            for part in model.get("parts", []):
                part["faces"] = sum(1 for face in result if face["part"] == part["name"] and face["b"] == part["bone"])
    return rows


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("jar", type=Path)
    parser.add_argument("output", type=Path)
    parser.add_argument("--apply", action="store_true")
    parser.add_argument("--maque-density", action="store_true")
    arguments = parser.parse_args()
    arguments.output.mkdir(parents=True, exist_ok=True)
    rows = []
    density_rows = []
    with zipfile.ZipFile(arguments.jar) as archive:
        for name in archive.namelist():
            if not name.startswith("assets/ftboss/") or not name.endswith(".json") or name.split("/")[2] in EXCLUDED:
                continue
            resource = json.loads(archive.read(name))
            if name.split("/")[2] == "geo":
                for geometry in resource.get("minecraft:geometry", []):
                    count = sum(6 * len(bone.get("cubes", [])) for bone in geometry.get("bones", []))
                    rows.append({"resource": name, "path": geometry["description"]["identifier"], "type": "bedrock_cube_faces", "before": count, "after": count, "merged": 0, "degenerate": 0})
                continue
            resource_rows = process_resource(resource, arguments.apply)
            if arguments.maque_density and name in ("assets/ftboss/maque_arcane/arcane_models.json", "assets/ftboss/maque_arcane/support_models.json"):
                for row in optimize_maque_density(resource, arguments.apply):
                    row["resource"] = name
                    density_rows.append(row)
                    for mesh in resource_rows:
                        if mesh["path"] == row["path"]:
                            mesh["after"] = row["after"]
                            mesh["lod_reduction"] = row["before"] - row["after"]
            for row in resource_rows:
                row["resource"] = name
            rows.extend(resource_rows)
            if arguments.apply and any(row["before"] != row["after"] for row in resource_rows):
                target = arguments.output / "overlay" / name
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_text(json.dumps(resource, ensure_ascii=False, separators=(",", ":")) + "\n", encoding="utf-8")
    report = {"baseline_sha256": hashlib.sha256(arguments.jar.read_bytes()).hexdigest(),
              "totals": {key: sum(row[key] for row in rows) for key in ("before", "after", "degenerate", "merged")}, "meshes": rows,
              "density_changes": density_rows}
    (arguments.output / "mesh-audit.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report["totals"]))
    grouped = collections.defaultdict(collections.Counter)
    for row in rows:
        for key in ("before", "after", "degenerate", "merged"):
            grouped[row["resource"]][key] += row[key]
    for resource, counts in grouped.items():
        print(resource, dict(counts))


if __name__ == "__main__":
    main()
