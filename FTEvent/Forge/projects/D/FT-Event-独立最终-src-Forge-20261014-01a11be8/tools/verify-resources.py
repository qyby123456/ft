import json
import struct
import zlib
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / "project/src/main/resources"
INTEGRATION = ROOT / "integration/kubejs/assets"


def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def json_file(path):
    return json.loads(path.read_text(encoding="utf-8"))


def verify_png(path):
    content = path.read_bytes()
    require(content[:8] == b"\x89PNG\r\n\x1a\n", "Invalid PNG header: " + str(path))
    offset = 8
    ended = False
    while offset < len(content):
        length = struct.unpack_from(">I", content, offset)[0]
        chunk_type = content[offset + 4:offset + 8]
        end = offset + length + 12
        require(end <= len(content), "Truncated PNG chunk")
        actual = zlib.crc32(content[offset + 4:offset + 8 + length]) & 0xFFFFFFFF
        expected = struct.unpack_from(">I", content, offset + 8 + length)[0]
        require(actual == expected, "PNG chunk CRC mismatch: " + str(path))
        offset = end
        if chunk_type == b"IEND":
            ended = True
            break
    require(ended and offset == len(content), "PNG IEND mismatch")
    width, height = struct.unpack_from(">II", content, 16)
    return {"path": path.relative_to(ROOT).as_posix(), "width": width, "height": height, "crcPassed": True}


def verify_facing(path):
    variants = json_file(path)["variants"]
    directions = ("north", "east", "south", "west")
    require(set(variants) == {"facing=" + direction for direction in directions}, "Four-way blockstate mismatch")
    angles = [variants["facing=" + direction].get("y", 0) for direction in directions]
    require(set(angles) == {0, 90, 180, 270}, "Four-way rotations mismatch")
    return {"path": path.relative_to(ROOT).as_posix(), "directions": list(directions), "rotations": angles}


def main():
    require(ROOT.parent == Path(r"D:\ft工程文件"), "Resource verification must remain in D export directory")
    paths = list(RESOURCES.rglob("*.json")) + list(INTEGRATION.rglob("*.json"))
    for path in paths:
        json_file(path)
    pngs = [verify_png(path) for base in (RESOURCES, INTEGRATION) for path in sorted(base.rglob("*.png"))]
    facing = [verify_facing(RESOURCES / "assets/qys_event/blockstates" / (name + ".json"))
              for name in ("oracle", "weather_forecaster")]
    facing.append(verify_facing(INTEGRATION / "farmingtales/blockstates/wishing_well_core.json"))
    models = []
    for base in (RESOURCES / "assets", INTEGRATION):
        for path in sorted(base.glob("*/models/block/*.json")):
            model = json_file(path)
            for texture in model.get("textures", {}).values():
                if texture.startswith("#") or ":" not in texture:
                    continue
                namespace, resource = texture.split(":", 1)
                if namespace in {"qys_event", "farmingtales"}:
                    location = base / namespace / "textures" / (resource + ".png")
                    require(location.is_file(), "Missing exported texture: " + texture)
            faces = sum(len(element.get("faces", {})) for element in model.get("elements", []))
            models.append({"path": path.relative_to(ROOT).as_posix(), "faces": faces})
    report = {"passed": True, "parsedJsonFiles": len(paths), "pngs": pngs, "fourWayBlockstates": facing,
              "models": models, "visualGameRenderingTested": False}
    (ROOT / "audit/resources-verification.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"jsonFiles": len(paths), "pngFiles": len(pngs), "fourWayBlockstates": len(facing),
                      "textureClosurePassed": True, "modelFaces": models}, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
