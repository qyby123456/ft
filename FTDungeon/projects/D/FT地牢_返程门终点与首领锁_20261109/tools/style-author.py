import argparse
import copy
import json
import math
from pathlib import Path
import re
import sys


IDENTIFIER = re.compile(r"[a-z0-9_.-]+:[a-z0-9_./-]+\Z")
STATE = re.compile(r"([a-z0-9_.-]+:[a-z0-9_./-]+)(?:\[([a-z0-9_]+=[a-z0-9_.-]+(?:,[a-z0-9_]+=[a-z0-9_.-]+)*)\])?\Z")
BUILTINS = ("forest", "desert", "frost")
SPEC_KEYS = {"id", "title", "baseStyle", "icon", "capBlock", "barrierBlock", "palette", "enemy", "boss", "geometry", "roomCooldownTicks", "instanceCap"}


def unique_object(pairs):
    result = {}
    for key, value in pairs:
        if key in result:
            raise ValueError(f"Duplicate JSON key: {key}")
        result[key] = value
    return result


def read_json(value):
    raw = Path(value[1:]).read_text(encoding="utf-8-sig") if value.startswith("@") else value
    return json.loads(raw, object_pairs_hook=unique_object,
                      parse_constant=lambda token: fail(f"Non-finite JSON number: {token}"))


def fail(message):
    raise ValueError(message)


def mapping(value, label, allowed=None):
    if not isinstance(value, dict):
        fail(f"{label} must be a JSON object")
    if allowed is not None and set(value) - allowed:
        fail(f"Unknown {label} fields: {', '.join(sorted(set(value) - allowed))}")
    return value


def identifier(value, label):
    if not isinstance(value, str) or not IDENTIFIER.fullmatch(value):
        fail(f"{label} must be a namespaced ID, e.g. ftmossgen:obsidian")
    return value


def blockstate(value, label, solid=False):
    match = STATE.fullmatch(value) if isinstance(value, str) else None
    if match is None:
        fail(f"Invalid {label} blockstate syntax: {value!r}")
    material, properties = match.groups()
    property_pairs = properties.split(",") if properties else []
    property_names = [pair.split("=", 1)[0] for pair in property_pairs]
    if len(set(property_names)) != len(property_names):
        fail(f"Duplicate {label} blockstate property")
    if material in {"minecraft:air", "minecraft:cave_air", "minecraft:void_air", "minecraft:water", "minecraft:lava"} or "waterlogged=true" in property_pairs:
        fail(f"{label} cannot introduce AIR or fluid")
    if solid and material.endswith(("_slab", "_stairs", "_chest")):
        fail(f"{label} requires a full solid block without a block entity")
    return value


def number(value, label, minimum, maximum, integer=False):
    if isinstance(value, bool) or not isinstance(value, (int, float)):
        fail(f"{label} must be numeric")
    if integer and not isinstance(value, int):
        fail(f"{label} must be an integer")
    if not minimum <= value <= maximum or not math.isfinite(value):
        fail(f"{label} must be between {minimum} and {maximum}")


def text(value, label, limit=100):
    if not isinstance(value, str) or not value.strip() or len(value) > limit:
        fail(f"{label} must contain 1..{limit} characters")


def validate(style):
    identifier(style["id"], "id")
    if style["id"] in {f"ftmossgen:{name}" for name in BUILTINS}:
        fail("External style ID must differ from every builtin ID")
    text(style["title"], "title")
    identifier(style["icon"], "icon")
    for key in ("capBlock", "barrierBlock"):
        blockstate(style[key], key, solid=True)
    for source, target in mapping(style["palette"], "palette").items():
        blockstate(source, "palette source")
        blockstate(target, "palette target")
    for key in ("melee", "ranged", "bossType"):
        identifier(style[key], key)
    text(style["bossName"], "bossName", limit=1000)
    number(style["bossHealth"], "bossHealth", 1, 10000)
    number(style["bossDamage"], "bossDamage", 0, 1000)
    number(style["roomCooldownTicks"], "roomCooldownTicks", 20, 72000, integer=True)
    number(style["instanceCap"], "instanceCap", 8, 256, integer=True)
    if not isinstance(style["decorations"], list):
        fail("geometry must be a JSON array of decoration objects")
    for decoration in style["decorations"]:
        mapping(decoration, "geometry item", {"block", "mode", "role", "from", "to", "spacing"})
        if not {"block", "from", "to"} <= decoration.keys():
            fail("Each geometry item requires block/from/to")
        if decoration.get("mode", "air") != "air":
            fail("Geometry supports only mode=air")
        role = decoration.get("role", "all")
        if not isinstance(role, str) or (role not in {"all", "rooms", "room", "turn", "terminal", "promenade", "boss"}
                and not (re.fullmatch(r"prefix:[a-z0-9_/-]+", role) and ".." not in role)):
            fail("Invalid geometry role")
        blockstate(decoration["block"], "geometry block", solid=True)
        for key in ("from", "to"):
            vector = decoration[key]
            if not isinstance(vector, list) or len(vector) != 3:
                fail(f"Geometry {key} must be [x,y,z]")
            for coordinate in vector:
                number(coordinate, f"geometry {key}", 0, 63, integer=True)
        if any(start > end for start, end in zip(decoration["from"], decoration["to"])):
            fail("Geometry from cannot exceed to")
        number(decoration.get("spacing", 1), "geometry spacing", 1, 64, integer=True)


def build_style(spec):
    mapping(spec, "spec", SPEC_KEYS)
    base = spec.get("baseStyle", "forest")
    if not isinstance(base, str):
        fail("baseStyle must be forest/desert/frost or its ftmossgen ID")
    base = base.removeprefix("ftmossgen:")
    if base not in BUILTINS:
        fail("baseStyle must be forest, desert or frost")
    builtin_path = Path(__file__).resolve().parent.parent / "src/main/resources/data/ftmossgen/styles" / f"{base}.json"
    style = copy.deepcopy(read_json("@" + str(builtin_path)))
    for key in ("id", "title", "icon", "capBlock", "barrierBlock", "roomCooldownTicks", "instanceCap"):
        if key in spec:
            style[key] = spec[key]
    if "id" not in spec:
        fail("An explicit new style id is required")
    if "palette" in spec:
        style["palette"].update(mapping(spec["palette"], "palette"))
    if "enemy" in spec:
        style.update(mapping(spec["enemy"], "enemy", {"melee", "ranged"}))
    if "boss" in spec:
        boss = mapping(spec["boss"], "boss", {"type", "name", "health", "damage"})
        for key, value in boss.items():
            style[{"type": "bossType", "name": "bossName", "health": "bossHealth", "damage": "bossDamage"}[key]] = value
    if "geometry" in spec:
        style["decorations"] = spec["geometry"]
    validate(style)
    return style


def main(argv=None):
    parser = argparse.ArgumentParser(description="Create an external dungeon style from a builtin JSON. Writes only --output, never overwrites. JSON arguments accept inline JSON or @path.")
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--spec", help="JSON object: id/title/baseStyle/icon/capBlock/barrierBlock/palette/enemy/boss/geometry/roomCooldownTicks/instanceCap")
    parser.add_argument("--id")
    parser.add_argument("--title")
    parser.add_argument("--base-style", "--baseStyle", dest="baseStyle")
    parser.add_argument("--icon")
    parser.add_argument("--cap-block", dest="capBlock")
    parser.add_argument("--barrier-block", dest="barrierBlock")
    parser.add_argument("--palette", help="JSON object of source blockstate -> target blockstate, merged into base palette")
    parser.add_argument("--enemy", help="JSON object with melee/ranged entity IDs")
    parser.add_argument("--boss", help="JSON object with type/name/health/damage")
    parser.add_argument("--geometry", help="JSON decoration array, replaces base decorations; [] removes them")
    args = parser.parse_args(argv)
    try:
        spec = mapping(read_json(args.spec), "spec", SPEC_KEYS) if args.spec is not None else {}
        for key in ("id", "title", "baseStyle", "icon", "capBlock", "barrierBlock", "palette", "enemy", "boss", "geometry"):
            value = getattr(args, key)
            if value is not None:
                spec[key] = read_json(value) if key in {"palette", "enemy", "boss", "geometry"} else value
        style = build_style(spec)
        output = args.output.expanduser().absolute()
        serialized = json.dumps(style, ensure_ascii=False, indent=2, allow_nan=False) + "\n"
        with output.open("x", encoding="utf-8", newline="\n") as stream:
            stream.write(serialized)
        print(f"Created {style['id']}: {output}")
        return 0
    except (ValueError, OSError) as failure:
        parser.exit(2, f"style-author: {failure}\n")


if __name__ == "__main__":
    sys.exit(main())
